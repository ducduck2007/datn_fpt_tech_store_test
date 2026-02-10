package com.retailmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanagement.dto.request.PromotionRequest;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promoRepo;
    private final ProductVariantRepository variantRepo;
    private final PromotionRedemptionRepository redemptionRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PromotionService(PromotionRepository promoRepo,
                            ProductVariantRepository variantRepo,
                            PromotionRedemptionRepository redemptionRepo) {
        this.promoRepo = promoRepo;
        this.variantRepo = variantRepo;
        this.redemptionRepo = redemptionRepo;
    }

    // =========================
    // JSON models
    // =========================
    public static class Applicability {
        public String scope; // ALL / PRODUCT / VARIANT
        public List<Integer> product_ids;
        public List<Integer> variant_ids;
        public List<String> customer_types; // REGULAR, VIP
        public List<String> vip_tiers; // BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
    }

    public static class Rules {
        public Integer usage_limit;
        public Combo combo;
    }

    public static class Combo {
        public Integer buy_qty;
        public Integer get_qty;
    }

    private String buildApplicabilityJson(PromotionRequest req) {
        try {
            Applicability a = new Applicability();
            String scope = (req.getScope() == null) ? "ALL" : req.getScope().trim().toUpperCase();
            a.scope = scope;
            a.product_ids = req.getProductIds();
            a.variant_ids = req.getVariantIds();

            // ✅ ADD VIP SUPPORT
            a.customer_types = req.getCustomerTypes();
            a.vip_tiers = req.getVipTiers();

            return objectMapper.writeValueAsString(a);
        } catch (Exception e) {
            throw new IllegalArgumentException("applicabilityJson không hợp lệ");
        }
    }

    private Applicability parseApplicability(String json) {
        try {
            if (json == null || json.isBlank()) {
                Applicability a = new Applicability();
                a.scope = "ALL";
                return a;
            }
            return objectMapper.readValue(json, Applicability.class);
        } catch (Exception e) {
            Applicability a = new Applicability();
            a.scope = "ALL";
            return a;
        }
    }

    private String buildRulesJson(PromotionRequest req) {
        try {
            Rules r = new Rules();

            if (req.getUsageLimit() != null) {
                if (req.getUsageLimit() <= 0) throw new IllegalArgumentException("usageLimit phải > 0 hoặc null");
                r.usage_limit = req.getUsageLimit();
            }

            Integer buy = req.getBuyQty();
            Integer get = req.getGetQty();
            boolean hasCombo = buy != null || get != null;
            if (hasCombo) {
                if (buy == null || get == null) throw new IllegalArgumentException("combo buyQty/getQty phải đủ");
                if (buy <= 0 || get <= 0) throw new IllegalArgumentException("combo buyQty/getQty phải > 0");
                Combo c = new Combo();
                c.buy_qty = buy;
                c.get_qty = get;
                r.combo = c;
            }

            if (r.usage_limit == null && r.combo == null) return null;
            return objectMapper.writeValueAsString(r);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            throw new IllegalArgumentException("rulesJson không hợp lệ");
        }
    }

    private Rules parseRules(String json) {
        try {
            if (json == null || json.isBlank()) return new Rules();
            return objectMapper.readValue(json, Rules.class);
        } catch (Exception e) {
            return new Rules();
        }
    }

    // =========================
    // Duplicate/overlap checks
    // =========================
    private boolean dateOverlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return !s1.isAfter(e2) && !e1.isBefore(s2);
    }

    private boolean applicabilityOverlap(Applicability a, Applicability b) {
        String sa = (a.scope == null) ? "ALL" : a.scope.toUpperCase();
        String sb = (b.scope == null) ? "ALL" : b.scope.toUpperCase();
        if ("ALL".equals(sa) || "ALL".equals(sb)) return true;

        Set<Integer> aProducts = a.product_ids == null ? Set.of() : new HashSet<>(a.product_ids);
        Set<Integer> bProducts = b.product_ids == null ? Set.of() : new HashSet<>(b.product_ids);

        Set<Integer> aVariants = a.variant_ids == null ? Set.of() : new HashSet<>(a.variant_ids);
        Set<Integer> bVariants = b.variant_ids == null ? Set.of() : new HashSet<>(b.variant_ids);

        if (!aProducts.isEmpty() && !bProducts.isEmpty()) {
            return !Collections.disjoint(aProducts, bProducts);
        }

        if (!aVariants.isEmpty() && !bVariants.isEmpty()) {
            return !Collections.disjoint(aVariants, bVariants);
        }

        if (!aProducts.isEmpty() && !bVariants.isEmpty()) {
            List<ProductVariant> vs = variantRepo.findAllById(bVariants);
            Set<Integer> bVariantProducts = vs.stream().map(v -> v.getProduct().getId()).collect(Collectors.toSet());
            return !Collections.disjoint(aProducts, bVariantProducts);
        }

        if (!bProducts.isEmpty() && !aVariants.isEmpty()) {
            List<ProductVariant> vs = variantRepo.findAllById(aVariants);
            Set<Integer> aVariantProducts = vs.stream().map(v -> v.getProduct().getId()).collect(Collectors.toSet());
            return !Collections.disjoint(bProducts, aVariantProducts);
        }

        return false;
    }

    private void assertNoDuplicatePromotion(Promotion draft, Integer ignoreId) {
        LocalDateTime s = draft.getStartDate();
        LocalDateTime e = draft.getEndDate();

        List<Promotion> candidates = (ignoreId == null)
                ? promoRepo.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(e, s)
                : promoRepo.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(e, s, ignoreId);

        Applicability newApp = parseApplicability(draft.getApplicabilityJson());

        List<String> conflicts = new ArrayList<>();
        for (Promotion p : candidates) {
            if (!dateOverlap(p.getStartDate(), p.getEndDate(), s, e)) continue;

            Applicability oldApp = parseApplicability(p.getApplicabilityJson());
            if (applicabilityOverlap(newApp, oldApp)) conflicts.add(p.getCode());
        }

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Trùng khuyến mãi theo thời gian/phạm vi với: " + String.join(", ", conflicts));
        }
    }

    // =========================
    // CRUD
    // =========================
    @Transactional
    public Promotion create(PromotionRequest req, Integer createdBy) {
        if (req == null) throw new IllegalArgumentException("Body rỗng");
        if (req.getCode() == null || req.getCode().isBlank()) throw new IllegalArgumentException("code không được rỗng");
        if (req.getName() == null || req.getName().isBlank()) throw new IllegalArgumentException("name không được rỗng");
        if (req.getStartDate() == null || req.getEndDate() == null) throw new IllegalArgumentException("startDate/endDate bắt buộc");
        if (!req.getStartDate().isBefore(req.getEndDate())) throw new IllegalArgumentException("startDate phải < endDate");

        String code = req.getCode().trim().toUpperCase();
        promoRepo.findByCode(code).ifPresent(x -> { throw new IllegalArgumentException("Promo code đã tồn tại"); });

        String discountType = req.getDiscountType() == null ? "PERCENT" : req.getDiscountType().trim().toUpperCase();
        BigDecimal discountValue = req.getDiscountValue();

        String rulesJson = buildRulesJson(req);
        Rules rules = parseRules(rulesJson);
        boolean hasCombo = rules != null && rules.combo != null;

        if (hasCombo) {
            if (discountValue != null && discountValue.compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("Khuyến mãi combo (mua X tặng Y) không dùng discountValue");
            }
        } else {
            if (discountValue == null) throw new IllegalArgumentException("discountValue bắt buộc (trừ combo)");
            if (discountValue.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("discountValue phải >= 0");
            if (!"PERCENT".equals(discountType) && !"AMOUNT".equals(discountType)) {
                throw new IllegalArgumentException("discountType chỉ hỗ trợ PERCENT hoặc AMOUNT");
            }
            if ("PERCENT".equals(discountType) && discountValue.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("discountValue PERCENT tối đa 100");
            }
        }

        Promotion p = new Promotion();
        p.setCode(code);
        p.setName(req.getName().trim());
        p.setDescription(req.getDescription());
        p.setDiscountType(discountType);
        p.setDiscountValue(discountValue == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : discountValue.setScale(2, RoundingMode.HALF_UP));
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setPriority(req.getPriority() == null ? 0 : req.getPriority());
        p.setStackable(req.getStackable() != null && req.getStackable());
        p.setIsActive(req.getIsActive() == null || req.getIsActive());
        p.setApplicabilityJson(buildApplicabilityJson(req));
        p.setRulesJson(rulesJson);
        p.setCreatedBy(createdBy);
        p.setCreatedAt(LocalDateTime.now());

        assertNoDuplicatePromotion(p, null);

        Promotion saved = promoRepo.save(p);

        redemptionRepo.findByPromotionId(saved.getId()).orElseGet(() -> {
            PromotionRedemption r = new PromotionRedemption();
            r.setPromotionId(saved.getId());
            r.setUsedCount(0L);
            r.setUpdatedAt(LocalDateTime.now());
            return redemptionRepo.save(r);
        });

        return saved;
    }

    public List<Promotion> list(Boolean activeOnly) {
        if (activeOnly != null && activeOnly) {
            LocalDateTime now = LocalDateTime.now();
            return promoRepo.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now);
        }
        return promoRepo.findAll();
    }

    @Transactional
    public Promotion update(Integer id, PromotionRequest req) {
        Promotion p = promoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Promotion not found"));
        if (req == null) throw new IllegalArgumentException("Body rỗng");

        if (req.getName() != null) p.setName(req.getName().trim());
        if (req.getDescription() != null) p.setDescription(req.getDescription());

        if (req.getDiscountType() != null) p.setDiscountType(req.getDiscountType().trim().toUpperCase());
        if (req.getDiscountValue() != null) {
            if (req.getDiscountValue().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("discountValue phải >= 0");
            p.setDiscountValue(req.getDiscountValue().setScale(2, RoundingMode.HALF_UP));
        }

        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) p.setEndDate(req.getEndDate());
        if (req.getPriority() != null) p.setPriority(req.getPriority());
        if (req.getStackable() != null) p.setStackable(req.getStackable());
        if (req.getIsActive() != null) p.setIsActive(req.getIsActive());

        if (req.getScope() != null || req.getProductIds() != null || req.getVariantIds() != null) {
            p.setApplicabilityJson(buildApplicabilityJson(req));
        }

        if (req.getUsageLimit() != null || req.getBuyQty() != null || req.getGetQty() != null) {
            p.setRulesJson(buildRulesJson(req));
        }

        if (!p.getStartDate().isBefore(p.getEndDate())) throw new IllegalArgumentException("startDate phải < endDate");

        String dt = p.getDiscountType() == null ? "PERCENT" : p.getDiscountType().trim().toUpperCase();
        if (!"PERCENT".equals(dt) && !"AMOUNT".equals(dt)) {
            throw new IllegalArgumentException("discountType chỉ hỗ trợ PERCENT hoặc AMOUNT");
        }

        Rules rules = parseRules(p.getRulesJson());
        boolean hasCombo = rules != null && rules.combo != null;

        if (hasCombo) {
            if (p.getDiscountValue() != null && p.getDiscountValue().compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("Khuyến mãi combo (mua X tặng Y) không dùng discountValue");
            }
            if (rules.combo.buy_qty == null || rules.combo.get_qty == null || rules.combo.buy_qty <= 0 || rules.combo.get_qty <= 0) {
                throw new IllegalArgumentException("combo buyQty/getQty phải > 0");
            }
        } else {
            if (p.getDiscountValue() == null) throw new IllegalArgumentException("discountValue bắt buộc (trừ combo)");
            if ("PERCENT".equals(dt) && p.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("discountValue PERCENT tối đa 100");
            }
        }

        assertNoDuplicatePromotion(p, p.getId());
        return promoRepo.save(p);
    }

    @Transactional
    public void delete(Integer id) {
        Promotion p = promoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Promotion not found"));
        p.setIsActive(false);
        promoRepo.save(p);
    }

    // =========================
    // Pricing logic with customer group support
    // =========================
    
    /**
     * Find best promotion for variant without customer context (backwards compatibility)
     */
    public Promotion findBestPromotionForVariant(ProductVariant v, LocalDateTime at) {
        return findBestPromotionForVariantAndCustomer(v, null, at);
    }

    /**
     * Find best promotion considering customer type/tier
     * Priority mechanism: highest discount wins, if equal then highest priority number wins
     */
    public Promotion findBestPromotionForVariantAndCustomer(ProductVariant v, Customer customer, LocalDateTime at) {
        List<Promotion> active = promoRepo.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(at, at);

        BigDecimal base = v.getPrice() == null ? BigDecimal.ZERO : v.getPrice();
        Promotion best = null;
        BigDecimal bestFinal = base;

        for (Promotion p : active) {
            Applicability app = parseApplicability(p.getApplicabilityJson());
            
            // Check product/variant applicability
            if (!isApplicable(app, v)) continue;
            
            // Check customer group applicability
            if (!isApplicableToCustomer(app, customer)) continue;
            
            // Check usage limit
            if (!isUsableByLimit(p)) continue;

            BigDecimal finalPrice = computeEffectiveUnitPrice(base, p);

            // Priority mechanism: lower price wins, if equal then higher priority number wins
            if (finalPrice.compareTo(bestFinal) < 0) {
                bestFinal = finalPrice;
                best = p;
            } else if (finalPrice.compareTo(bestFinal) == 0 && best != null) {
                if (p.getPriority() > best.getPriority()) {
                    best = p;
                }
            }
        }
        return best;
    }

    /**
     * Check if promotion is applicable to specific customer group
     */
    private boolean isApplicableToCustomer(Applicability app, Customer customer) {
        // If no customer restrictions, apply to all
        if (app.customer_types == null && app.vip_tiers == null) {
            return true;
        }

        // If no customer provided but promotion has restrictions, don't apply
        if (customer == null) {
            return app.customer_types == null && app.vip_tiers == null;
        }

        // Check customer type
        if (app.customer_types != null && !app.customer_types.isEmpty()) {
            String customerTypeStr = customer.getCustomerType() != null 
                ? customer.getCustomerType().name() 
                : "REGULAR";
            if (!app.customer_types.contains(customerTypeStr)) {
                return false;
            }
        }

        // Check VIP tier
        if (app.vip_tiers != null && !app.vip_tiers.isEmpty()) {
            String tierStr = customer.getVipTier() != null 
                ? customer.getVipTier().name() 
                : null;
            if (tierStr == null || !app.vip_tiers.contains(tierStr)) {
                return false;
            }
        }

        return true;
    }

    public boolean isApplicable(Applicability app, ProductVariant v) {
        String scope = (app.scope == null) ? "ALL" : app.scope.toUpperCase();
        if ("ALL".equals(scope)) return true;

        Integer productId = v.getProduct().getId();
        Integer variantId = v.getId();

        if (app.product_ids != null && app.product_ids.contains(productId)) return true;
        if (app.variant_ids != null && app.variant_ids.contains(variantId)) return true;

        return false;
    }

    /**
     * Compute effective unit price with combo support
     */
    public BigDecimal computeEffectiveUnitPrice(BigDecimal base, Promotion p) {
        if (base == null) base = BigDecimal.ZERO;
        if (p == null) return money(base);

        Rules rules = parseRules(p.getRulesJson());
        if (rules != null && rules.combo != null && rules.combo.buy_qty != null && rules.combo.get_qty != null) {
            int buy = rules.combo.buy_qty;
            int get = rules.combo.get_qty;
            if (buy > 0 && get > 0) {
                BigDecimal numerator = base.multiply(BigDecimal.valueOf(buy));
                BigDecimal denom = BigDecimal.valueOf((long) buy + (long) get);
                BigDecimal r = numerator.divide(denom, 2, RoundingMode.HALF_UP);
                return r.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : r;
            }
        }

        return applyDiscount(base, p.getDiscountType(), p.getDiscountValue());
    }

    public BigDecimal applyDiscount(BigDecimal base, String type, BigDecimal value) {
        if (base == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (value == null) return money(base);

        String t = (type == null) ? "PERCENT" : type.toUpperCase();
        if ("AMOUNT".equals(t)) {
            BigDecimal r = base.subtract(value);
            return r.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : money(r);
        }

        BigDecimal pct = value;
        if (pct.compareTo(BigDecimal.ZERO) < 0) pct = BigDecimal.ZERO;
        if (pct.compareTo(new BigDecimal("100")) > 0) pct = new BigDecimal("100");

        BigDecimal discount = base.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal r = base.subtract(discount);
        return r.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : money(r);
    }

    // =========================
    // Usage limit
    // =========================
    public boolean isUsableByLimit(Promotion p) {
        if (p == null) return false;
        Rules rules = parseRules(p.getRulesJson());
        if (rules == null || rules.usage_limit == null) return true;

        int limit = rules.usage_limit;
        if (limit <= 0) return true;

        long used = redemptionRepo.findByPromotionId(p.getId()).map(PromotionRedemption::getUsedCount).orElse(0L);
        return used < limit;
    }

    @Transactional
    public void recordRedemption(Integer promotionId, long delta) {
        if (promotionId == null) return;
        if (delta <= 0) delta = 1;

        PromotionRedemption r = redemptionRepo.findByPromotionId(promotionId).orElseGet(() -> {
            PromotionRedemption x = new PromotionRedemption();
            x.setPromotionId(promotionId);
            x.setUsedCount(0L);
            x.setUpdatedAt(LocalDateTime.now());
            return x;
        });

        r.setUsedCount((r.getUsedCount() == null ? 0L : r.getUsedCount()) + delta);
        r.setUpdatedAt(LocalDateTime.now());
        redemptionRepo.save(r);
    }

    private static BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

}
