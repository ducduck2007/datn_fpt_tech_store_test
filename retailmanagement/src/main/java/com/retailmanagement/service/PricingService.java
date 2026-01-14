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
                          PromotionService promotionService, UserRepository userRepository) {
        this.variantRepo = variantRepo;
        this.priceHistoryRepo = priceHistoryRepo;
        this.settingService = settingService;
        this.promotionService = promotionService;
        this.userRepository = userRepository;
    }

    // 1) Tạo/đổi giá (và tạo history)
    @Transactional
    public PriceHistory setVariantPrice(Integer variantId, UpsertPriceRequest req, Integer userId) {
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price phải >= 0");
        }

        String currency = (req.getCurrencyCode() == null || req.getCurrencyCode().isBlank())
                ? settingService.getDefaultCurrency()
                : req.getCurrencyCode().trim().toUpperCase();

        Instant now = Instant.now();

        // close current history
        priceHistoryRepo.findFirstByVariant_IdAndEffectiveToIsNullOrderByEffectiveFromDesc(variantId)
                .ifPresent(cur -> {
                    cur.setEffectiveTo(now);
                    priceHistoryRepo.save(cur);
                });

        // create new history
        PriceHistory ph = new PriceHistory();
        ph.setVariant(v);
        ph.setCurrencyCode(currency);
        ph.setPrice(req.getPrice());
        ph.setCostPrice(req.getCostPrice());
        ph.setReason(req.getReason() == null ? "MANUAL" : req.getReason());
        ph.setEffectiveFrom(now);
        ph.setEffectiveTo(null);
        ph.setCreatedBy(user);
        ph.setCreatedAt(now);
        priceHistoryRepo.save(ph);

        // update current price on variant
        v.setCurrencyCode(currency);
        v.setPrice(req.getPrice());
        v.setCostPrice(req.getCostPrice());
        v.setUpdatedAt(now);
        variantRepo.save(v);

        return ph;
    }

    // 2) Danh sách giá theo sản phẩm (current)
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
                r.setFinalPrice(promotionService.applyDiscount(v.getPrice(), best.getDiscountType(), best.getDiscountValue()));
            } else {
                r.setFinalPrice(v.getPrice());
            }
            return r;
        }).toList();
    }

    // 3) Chỉnh sửa giá: chỉ cho phép chỉnh sửa record mới nhất (effective_to = null)
    @Transactional
    public PriceHistory updateLatestHistory(Long priceHistoryId, UpsertPriceRequest req) {
        PriceHistory ph = priceHistoryRepo.findById(priceHistoryId)
                .orElseThrow(() -> new NoSuchElementException("Price history not found"));

        if (ph.getEffectiveTo() != null) {
            throw new IllegalArgumentException("Chỉ được sửa bản ghi giá hiện tại (effective_to = null)");
        }

        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price phải >= 0");
        }

        String currency = (req.getCurrencyCode() == null || req.getCurrencyCode().isBlank())
                ? ph.getCurrencyCode()
                : req.getCurrencyCode().trim().toUpperCase();

        ph.setCurrencyCode(currency);
        ph.setPrice(req.getPrice());
        ph.setCostPrice(req.getCostPrice());
        ph.setReason(req.getReason() == null ? ph.getReason() : req.getReason());
        priceHistoryRepo.save(ph);

        // sync variant current price
        ProductVariant v = ph.getVariant();
        v.setCurrencyCode(currency);
        v.setPrice(req.getPrice());
        v.setCostPrice(req.getCostPrice());
        v.setUpdatedAt(Instant.now());
        variantRepo.save(v);

        return ph;
    }

    // 4) Xóa giá: chỉ cho phép xóa bản ghi mới nhất và rollback về bản trước
    @Transactional
    public void deleteLatestAndRollback(Long priceHistoryId) {
        PriceHistory current = priceHistoryRepo.findById(priceHistoryId)
                .orElseThrow(() -> new NoSuchElementException("Price history not found"));

        if (current.getEffectiveTo() != null) {
            throw new IllegalArgumentException("Chỉ được xóa bản ghi giá hiện tại (effective_to = null)");
        }

        Integer variantId = current.getVariant().getId();

        priceHistoryRepo.delete(current);

        // rollback: lấy bản mới nhất còn lại
        List<PriceHistory> histories = priceHistoryRepo.findByVariant_IdOrderByEffectiveFromDesc(variantId);
        if (histories.isEmpty()) {
            throw new IllegalStateException("Không còn lịch sử giá để rollback");
        }

        PriceHistory latest = histories.get(0);
        latest.setEffectiveTo(null);
        priceHistoryRepo.save(latest);

        ProductVariant v = latest.getVariant();
        v.setCurrencyCode(latest.getCurrencyCode());
        v.setPrice(latest.getPrice());
        v.setCostPrice(latest.getCostPrice());
        v.setUpdatedAt(Instant.now());
        variantRepo.save(v);
    }

    // 5) Giá hiện tại sau khuyến mãi theo variant
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
            r.setFinalPrice(promotionService.applyDiscount(v.getPrice(), best.getDiscountType(), best.getDiscountValue()));
        } else {
            r.setFinalPrice(v.getPrice());
        }
        return r;
    }
}
