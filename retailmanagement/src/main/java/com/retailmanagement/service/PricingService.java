package com.retailmanagement.service;

import com.retailmanagement.dto.request.UpsertPriceRequest;
import com.retailmanagement.dto.response.VariantPriceResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
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
    private final CustomRes customerRepository;

    public PricingService(ProductVariantRepository variantRepo,
                          PriceHistoryRepository priceHistoryRepo,
                          SettingService settingService,
                          PromotionService promotionService,
                          UserRepository userRepository,
                          CustomRes customerRepository) {
        this.variantRepo = variantRepo;
        this.priceHistoryRepo = priceHistoryRepo;
        this.settingService = settingService;
        this.promotionService = promotionService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
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

    /**
     * Get effective price for a variant considering customer group
     */
    public VariantPriceResponse getEffectivePriceForCustomer(Integer variantId, Integer customerId) {
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant not found"));

        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId).orElse(null);
        }

        LocalDateTime now = LocalDateTime.now();
        
        // Get base price (considering customer group pricing if implemented)
        BigDecimal basePrice = getBasePriceForCustomer(v, customer);

        // Find best promotion applicable to this customer
        Promotion best = promotionService.findBestPromotionForVariantAndCustomer(v, customer, now);

        VariantPriceResponse r = new VariantPriceResponse();
        r.setVariantId(v.getId());
        r.setProductId(v.getProduct().getId());
        r.setVariantName(v.getVariantName());
        r.setSku(v.getSku());
        r.setCurrencyCode(v.getCurrencyCode());
        r.setPrice(basePrice);
        r.setCostPrice(v.getCostPrice());

        if (best != null) {
            r.setPromotionCode(best.getCode());
            r.setFinalPrice(promotionService.computeEffectiveUnitPrice(basePrice, best));
        } else {
            r.setFinalPrice(basePrice);
        }
        return r;
    }

    /**
     * Get base price considering customer type/tier discounts
     */
    private BigDecimal getBasePriceForCustomer(ProductVariant variant, Customer customer) {
        BigDecimal basePrice = variant.getPrice();
        
        if (customer == null) {
            return basePrice;
        }

        // Apply VIP tier discount if applicable
        VipTier tier = customer.getVipTier();
        if (tier != null && tier.getDiscountRate() != null && tier.getDiscountRate() > 0) {
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(tier.getDiscountRate())
            );
            basePrice = basePrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }

        return basePrice;
    }

    public List<VariantPriceResponse> listCurrentPricesByProduct(Integer productId) {
        return listCurrentPricesByProductForCustomer(productId, null);
    }

    /**
     * List prices by product, optionally for a specific customer
     */
    public List<VariantPriceResponse> listCurrentPricesByProductForCustomer(Integer productId, Integer customerId) {
        List<ProductVariant> variants = variantRepo.findByProduct_Id(productId);
        LocalDateTime now = LocalDateTime.now();

        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId).orElse(null);
        }

        final Customer finalCustomer = customer;

        return variants.stream().map(v -> {
            BigDecimal basePrice = getBasePriceForCustomer(v, finalCustomer);
            
            VariantPriceResponse r = new VariantPriceResponse();
            r.setVariantId(v.getId());
            r.setProductId(v.getProduct().getId());
            r.setVariantName(v.getVariantName());
            r.setSku(v.getSku());
            r.setCurrencyCode(v.getCurrencyCode());
            r.setPrice(basePrice);
            r.setCostPrice(v.getCostPrice());

            Promotion best = promotionService.findBestPromotionForVariantAndCustomer(v, finalCustomer, now);
            if (best != null) {
                r.setPromotionCode(best.getCode());
                r.setFinalPrice(promotionService.computeEffectiveUnitPrice(basePrice, best));
            } else {
                r.setFinalPrice(basePrice);
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
        return getEffectivePriceForCustomer(variantId, null);
    }

    private static BigDecimal scaleMoney(BigDecimal v) {
        if (v == null) return null;
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
