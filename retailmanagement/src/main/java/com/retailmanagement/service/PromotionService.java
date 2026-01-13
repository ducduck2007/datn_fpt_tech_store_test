package com.retailmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanagement.dto.request.PromotionRequest;
import com.retailmanagement.entity.ProductVariant;
import com.retailmanagement.entity.Promotion;
import com.retailmanagement.repository.ProductVariantRepository;
import com.retailmanagement.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promoRepo;
    private final ProductVariantRepository variantRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PromotionService(PromotionRepository promoRepo, ProductVariantRepository variantRepo) {
        this.promoRepo = promoRepo;
        this.variantRepo = variantRepo;
    }

    // JSON format: {"scope":"ALL"} or {"product_ids":[1,2]} or {"variant_ids":[10,11]}
    public static class Applicability {
        public String scope;
        public List<Integer> product_ids;
        public List<Integer> variant_ids;
    }

    private String buildApplicabilityJson(PromotionRequest req) {
        try {
            Applicability a = new Applicability();
            String scope = (req.getScope() == null) ? "ALL" : req.getScope().trim().toUpperCase();
            a.scope = scope;
            a.product_ids = req.getProductIds();
            a.variant_ids = req.getVariantIds();
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

    private boolean dateOverlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return !s1.isAfter(e2) && !e1.isBefore(s2);
    }

    private boolean applicabilityOverlap(Applicability a, Applicability b, Integer newVariantId, Integer newProductId) {
        String sa = (a.scope == null) ? "ALL" : a.scope.toUpperCase();
        String sb = (b.scope == null) ? "ALL" : b.scope.toUpperCase();

        if ("ALL".equals(sa) || "ALL".equals(sb)) return true;

        Set<Integer> aProducts = a.product_ids == null ? Set.of() : new HashSet<>(a.product_ids);
        Set<Integer> bProducts = b.product_ids == null ? Set.of() : new HashSet<>(b.product_ids);

        Set<Integer> aVariants = a.variant_ids == null ? Set.of() : new HashSet<>(a.variant_ids);
        Set<Integer> bVariants = b.variant_ids == null ? Set.of() : new HashSet<>(b.variant_ids);

        // PRODUCT vs PRODUCT
        if (!aProducts.isEmpty() && !bProducts.isEmpty()) {
            return !Collections.disjoint(aProducts, bProducts);
        }

        // VARIANT vs VARIANT
        if (!aVariants.isEmpty() && !bVariants.isEmpty()) {
            return !Collections.disjoint(aVariants, bVariants);
        }

        // PRODUCT vs VARIANT => map variant->product
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
            if (applicabilityOverlap(newApp, oldApp, null, null)) {
                conflicts.add(p.getCode());
            }
        }

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Trùng khuyến mãi theo thời gian/phạm vi với: " + String.join(", ", conflicts));
        }
    }

    @Transactional
    public Promotion create(PromotionRequest req, Integer createdBy) {
        if (req.getCode() == null || req.getCode().isBlank()) throw new IllegalArgumentException("code không được rỗng");
        if (req.getName() == null || req.getName().isBlank()) throw new IllegalArgumentException("name không được rỗng");
        if (req.getStartDate() == null || req.getEndDate() == null) throw new IllegalArgumentException("startDate/endDate bắt buộc");
        if (!req.getStartDate().isBefore(req.getEndDate())) throw new IllegalArgumentException("startDate phải < endDate");

        promoRepo.findByCode(req.getCode().trim().toUpperCase()).ifPresent(x -> {
            throw new IllegalArgumentException("Promo code đã tồn tại");
        });

        Promotion p = new Promotion();
        p.setCode(req.getCode().trim().toUpperCase());
        p.setName(req.getName().trim());
        p.setDescription(req.getDescription());
        p.setDiscountType(req.getDiscountType() == null ? "PERCENT" : req.getDiscountType().trim().toUpperCase());
        p.setDiscountValue(req.getDiscountValue());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setPriority(req.getPriority() == null ? 0 : req.getPriority());
        p.setStackable(req.getStackable() != null && req.getStackable());
        p.setIsActive(req.getIsActive() == null || req.getIsActive());
        p.setApplicabilityJson(buildApplicabilityJson(req));
        p.setCreatedBy(createdBy);
        p.setCreatedAt(LocalDateTime.now());

        assertNoDuplicatePromotion(p, null);

        return promoRepo.save(p);
    }

    public List<Promotion> list(Boolean activeOnly) {
        List<Promotion> all = promoRepo.findAll();
        if (activeOnly != null && activeOnly) {
            return all.stream().filter(Promotion::getIsActive).toList();
        }
        return all;
    }

    @Transactional
    public Promotion update(Integer id, PromotionRequest req) {
        Promotion p = promoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Promotion not found"));

        if (req.getName() != null) p.setName(req.getName().trim());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getDiscountType() != null) p.setDiscountType(req.getDiscountType().trim().toUpperCase());
        if (req.getDiscountValue() != null) p.setDiscountValue(req.getDiscountValue());
        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) p.setEndDate(req.getEndDate());
        if (req.getPriority() != null) p.setPriority(req.getPriority());
        if (req.getStackable() != null) p.setStackable(req.getStackable());
        if (req.getIsActive() != null) p.setIsActive(req.getIsActive());

        // cập nhật phạm vi áp dụng nếu có gửi scope/productIds/variantIds
        if (req.getScope() != null || req.getProductIds() != null || req.getVariantIds() != null) {
            p.setApplicabilityJson(buildApplicabilityJson(req));
        }

        if (!p.getStartDate().isBefore(p.getEndDate())) throw new IllegalArgumentException("startDate phải < endDate");
        assertNoDuplicatePromotion(p, p.getId());

        return promoRepo.save(p);
    }

    @Transactional
    public void delete(Integer id) {
        Promotion p = promoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Promotion not found"));
        // soft delete theo schema: is_active
        p.setIsActive(false);
        promoRepo.save(p);
    }

    public Promotion findBestPromotionForVariant(ProductVariant v, LocalDateTime at) {
        List<Promotion> active = promoRepo
                .findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(at, at);

        BigDecimal base = v.getPrice();
        Promotion best = null;
        BigDecimal bestFinal = base;

        for (Promotion p : active) {
            Applicability app = parseApplicability(p.getApplicabilityJson());
            if (!isApplicable(app, v)) continue;

            BigDecimal finalPrice = applyDiscount(base, p.getDiscountType(), p.getDiscountValue());
            if (finalPrice.compareTo(bestFinal) < 0) {
                bestFinal = finalPrice;
                best = p;
            } else if (finalPrice.compareTo(bestFinal) == 0 && best != null) {
                if (p.getPriority() > best.getPriority()) best = p;
            }
        }
        return best;
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

    public BigDecimal applyDiscount(BigDecimal base, String type, BigDecimal value) {
        if (base == null) return BigDecimal.ZERO;
        if (value == null) return base;

        String t = (type == null) ? "PERCENT" : type.toUpperCase();
        if ("AMOUNT".equals(t)) {
            BigDecimal r = base.subtract(value);
            return r.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : r;
        }

        // PERCENT
        BigDecimal pct = value;
        if (pct.compareTo(BigDecimal.ZERO) < 0) pct = BigDecimal.ZERO;
        if (pct.compareTo(new BigDecimal("100")) > 0) pct = new BigDecimal("100");

        BigDecimal discount = base.multiply(pct).divide(new BigDecimal("100"));
        BigDecimal r = base.subtract(discount);
        return r.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : r;
    }
}
