package com.retailmanagement.service;

import com.retailmanagement.dto.request.UpsertPriceRequest;
import com.retailmanagement.dto.response.VariantPriceResponse;
import com.retailmanagement.entity.PriceHistory;
import com.retailmanagement.entity.ProductVariant;
import com.retailmanagement.entity.Promotion;
import com.retailmanagement.entity.User;
import com.retailmanagement.repository.PriceHistoryRepository;
import com.retailmanagement.repository.ProductVariantRepository;
import com.retailmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PricingService {

    private final ProductVariantRepository variantRepo;
    private final PriceHistoryRepository priceHistoryRepo;
    private final SettingService settingService;
    private final PromotionService promotionService;
    private final UserRepository userRepository;

    public PricingService(ProductVariantRepository variantRepo,
                          PriceHistoryRepository priceHistoryRepo,
                          SettingService settingService,
                          PromotionService promotionService,
                          UserRepository userRepository) {
        this.variantRepo = variantRepo;
        this.priceHistoryRepo = priceHistoryRepo;
        this.settingService = settingService;
        this.promotionService = promotionService;
        this.userRepository = userRepository;
    }

    @Transactional
    public PriceHistory setVariantPrice(Integer variantId, UpsertPriceRequest req, Integer userId) {
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant not found"));

        if (req == null) throw new IllegalArgumentException("Body rỗng");
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price phải >= 0");
        }
        if (req.getCostPrice() != null && req.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("costPrice phải >= 0");
        }

        User user = null;
        if (userId != null && userId > 0) {
            user = userRepository.findById(userId).orElse(null);
        }

        String currency = (req.getCurrencyCode() == null || req.getCurrencyCode().isBlank())
                ? settingService.getDefaultCurrency()
                : req.getCurrencyCode().trim().toUpperCase();

        Instant now = Instant.now();

        priceHistoryRepo.findFirstByVariant_IdAndEffectiveToIsNullOrderByEffectiveFromDesc(variantId)
                .ifPresent(cur -> {
                    cur.setEffectiveTo(now);
                    priceHistoryRepo.save(cur);
                });

        PriceHistory ph = new PriceHistory();
        ph.setVariant(v);
        ph.setCurrencyCode(currency);
        ph.setPrice(scaleMoney(req.getPrice()));
        ph.setCostPrice(req.getCostPrice() == null ? null : scaleMoney(req.getCostPrice()));
        ph.setReason(req.getReason() == null || req.getReason().isBlank() ? "MANUAL" : req.getReason().trim());
        ph.setEffectiveFrom(now);
        ph.setEffectiveTo(null);
        ph.setCreatedBy(user);
        ph.setCreatedAt(now);
        priceHistoryRepo.save(ph);

        v.setCurrencyCode(currency);
        v.setPrice(scaleMoney(req.getPrice()));
        v.setCostPrice(req.getCostPrice() == null ? null : scaleMoney(req.getCostPrice()));
        v.setUpdatedAt(now);
        variantRepo.save(v);

        return ph;
    }

    public List<VariantPriceResponse> listCurrentPricesByProduct(Integer productId) {
        List<ProductVariant> variants = variantRepo.findByProduct_Id(productId);
        LocalDateTime now = LocalDateTime.now();

        return variants.stream().map(v -> {
            VariantPriceResponse r = new VariantPriceResponse();
            r.setVariantId(v.getId());
            r.setProductId(v.getProduct().getId());
            r.setVariantName(v.getVariantName());
            r.setSku(v.getSku());
            r.setCurrencyCode(v.getCurrencyCode());
            r.setPrice(v.getPrice());
            r.setCostPrice(v.getCostPrice());

            Promotion best = promotionService.findBestPromotionForVariant(v, now);
            if (best != null) {
                r.setPromotionCode(best.getCode());
                r.setFinalPrice(promotionService.computeEffectiveUnitPrice(v.getPrice(), best));
            } else {
                r.setFinalPrice(v.getPrice());
            }
            return r;
        }).toList();
    }

    @Transactional
    public PriceHistory updateLatestHistory(Long priceHistoryId, UpsertPriceRequest req) {
        PriceHistory ph = priceHistoryRepo.findById(priceHistoryId)
                .orElseThrow(() -> new NoSuchElementException("Price history not found"));

        if (ph.getEffectiveTo() != null) {
            throw new IllegalArgumentException("Chỉ được sửa bản ghi giá hiện tại (effective_to = null)");
        }

        if (req == null) throw new IllegalArgumentException("Body rỗng");
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price phải >= 0");
        }
        if (req.getCostPrice() != null && req.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("costPrice phải >= 0");
        }

        String currency = (req.getCurrencyCode() == null || req.getCurrencyCode().isBlank())
                ? ph.getCurrencyCode()
                : req.getCurrencyCode().trim().toUpperCase();

        ph.setCurrencyCode(currency);
        ph.setPrice(scaleMoney(req.getPrice()));
        ph.setCostPrice(req.getCostPrice() == null ? null : scaleMoney(req.getCostPrice()));
        if (req.getReason() != null && !req.getReason().isBlank()) ph.setReason(req.getReason().trim());
        priceHistoryRepo.save(ph);

        ProductVariant v = ph.getVariant();
        v.setCurrencyCode(currency);
        v.setPrice(scaleMoney(req.getPrice()));
        v.setCostPrice(req.getCostPrice() == null ? null : scaleMoney(req.getCostPrice()));
        v.setUpdatedAt(Instant.now());
        variantRepo.save(v);

        return ph;
    }

    @Transactional
    public void deleteLatestAndRollback(Long priceHistoryId) {
        PriceHistory current = priceHistoryRepo.findById(priceHistoryId)
                .orElseThrow(() -> new NoSuchElementException("Price history not found"));

        if (current.getEffectiveTo() != null) {
            throw new IllegalArgumentException("Chỉ được xóa bản ghi giá hiện tại (effective_to = null)");
        }

        Integer variantId = current.getVariant().getId();
        priceHistoryRepo.delete(current);

        List<PriceHistory> histories = priceHistoryRepo.findByVariant_IdOrderByEffectiveFromDesc(variantId);

        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant not found"));

        if (histories.isEmpty()) {
            String currency = settingService.getDefaultCurrency();
            v.setCurrencyCode(currency);
            v.setPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            v.setCostPrice(null);
            v.setUpdatedAt(Instant.now());
            variantRepo.save(v);
            return;
        }

        PriceHistory latest = histories.get(0);
        latest.setEffectiveTo(null);
        priceHistoryRepo.save(latest);

        v.setCurrencyCode(latest.getCurrencyCode());
        v.setPrice(scaleMoney(latest.getPrice()));
        v.setCostPrice(latest.getCostPrice() == null ? null : scaleMoney(latest.getCostPrice()));
        v.setUpdatedAt(Instant.now());
        variantRepo.save(v);
    }

    public VariantPriceResponse getEffectivePrice(Integer variantId) {
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant not found"));

        LocalDateTime now = LocalDateTime.now();
        Promotion best = promotionService.findBestPromotionForVariant(v, now);

        VariantPriceResponse r = new VariantPriceResponse();
        r.setVariantId(v.getId());
        r.setProductId(v.getProduct().getId());
        r.setVariantName(v.getVariantName());
        r.setSku(v.getSku());
        r.setCurrencyCode(v.getCurrencyCode());
        r.setPrice(v.getPrice());
        r.setCostPrice(v.getCostPrice());

        if (best != null) {
            r.setPromotionCode(best.getCode());
            r.setFinalPrice(promotionService.computeEffectiveUnitPrice(v.getPrice(), best));
        } else {
            r.setFinalPrice(v.getPrice());
        }
        return r;
    }

    private static BigDecimal scaleMoney(BigDecimal v) {
        if (v == null) return null;
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
