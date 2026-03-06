package com.retailmanagement.service;

import com.retailmanagement.dto.request.UpsertPriceRequest;
import com.retailmanagement.dto.response.PriceHistoryResponse;
import com.retailmanagement.dto.response.VariantPriceResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("price phải >= 0");
        if (req.getCostPrice() != null && req.getCostPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("costPrice phải >= 0");

        User user = null;
        if (userId != null && userId > 0)
            user = userRepository.findById(userId).orElse(null);

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

    public VariantPriceResponse getEffectivePriceForCustomer(Integer variantId, Integer customerId) {
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant not found"));

        Customer customer = customerId != null
                ? customerRepository.findById(customerId).orElse(null)
                : null;

        LocalDateTime now = LocalDateTime.now();
        BigDecimal basePrice = getBasePriceForCustomer(v, customer);
        Promotion best = promotionService.findBestPromotionForVariantAndCustomer(v, customer, now);

        return buildVariantPriceResponse(v, basePrice, best);
    }

    private BigDecimal getBasePriceForCustomer(ProductVariant variant, Customer customer) {
        BigDecimal basePrice = variant.getPrice();
        if (customer == null) return basePrice;

        VipTier tier = customer.getVipTier();
        if (tier != null && tier.getDiscountRate() != null && tier.getDiscountRate() > 0) {
            BigDecimal multiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(tier.getDiscountRate()));
            basePrice = basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return basePrice;
    }

    public List<VariantPriceResponse> listCurrentPricesByProduct(Integer productId) {
        return listCurrentPricesByProductForCustomer(productId, null);
    }

    public List<VariantPriceResponse> listCurrentPricesByProductForCustomer(Integer productId, Integer customerId) {
        List<ProductVariant> variants = variantRepo.findByProduct_Id(productId);
        LocalDateTime now = LocalDateTime.now();

        Customer customer = customerId != null
                ? customerRepository.findById(customerId).orElse(null)
                : null;
        final Customer finalCustomer = customer;

        return variants.stream().map(v -> {
            BigDecimal basePrice = getBasePriceForCustomer(v, finalCustomer);
            Promotion best = promotionService.findBestPromotionForVariantAndCustomer(v, finalCustomer, now);
            return buildVariantPriceResponse(v, basePrice, best);
        }).toList();
    }

    // ================================================================
    // ✅ Helper build VariantPriceResponse đầy đủ field
    // ================================================================
    private VariantPriceResponse buildVariantPriceResponse(ProductVariant v, BigDecimal basePrice, Promotion best) {
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
            r.setPromotionName(best.getName());                                       // ✅ thêm
            r.setFinalPrice(promotionService.computeEffectiveUnitPrice(basePrice, best));
            // ✅ Combo info
            PromotionService.Rules rules = promotionService.parseRulesPublic(best.getRulesJson());
            if (rules != null && rules.combo != null
                    && rules.combo.buy_qty != null && rules.combo.get_qty != null) {
                r.setComboInfo("Mua " + rules.combo.buy_qty + " tặng " + rules.combo.get_qty);
            }
        } else {
            r.setFinalPrice(basePrice);
        }
        return r;
    }

    @Transactional
    public PriceHistoryResponse updateLatestHistory(Long priceHistoryId, UpsertPriceRequest req) {
        PriceHistory ph = priceHistoryRepo.findById(priceHistoryId)
                .orElseThrow(() -> new NoSuchElementException("Price history not found"));

        if (ph.getEffectiveTo() != null)
            throw new IllegalArgumentException("Chỉ được sửa bản ghi giá hiện tại (effective_to = null)");
        if (req == null) throw new IllegalArgumentException("Body rỗng");
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("price phải >= 0");
        if (req.getCostPrice() != null && req.getCostPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("costPrice phải >= 0");

        String currency = (req.getCurrencyCode() == null || req.getCurrencyCode().isBlank())
                ? ph.getCurrencyCode()
                : req.getCurrencyCode().trim().toUpperCase();

        ph.setCurrencyCode(currency);
        ph.setPrice(scaleMoney(req.getPrice()));
        ph.setCostPrice(req.getCostPrice() == null ? null : scaleMoney(req.getCostPrice()));
        if (req.getReason() != null && !req.getReason().isBlank())
            ph.setReason(req.getReason().trim());
        priceHistoryRepo.save(ph);

        ProductVariant v = ph.getVariant();
        v.setCurrencyCode(currency);
        v.setPrice(scaleMoney(req.getPrice()));
        v.setCostPrice(req.getCostPrice() == null ? null : scaleMoney(req.getCostPrice()));
        v.setUpdatedAt(Instant.now());
        variantRepo.save(v);

        return toPriceHistoryResponse(ph);
    }

    @Transactional
    public void deleteLatestAndRollback(Long priceHistoryId) {
        PriceHistory current = priceHistoryRepo.findById(priceHistoryId)
                .orElseThrow(() -> new NoSuchElementException("Price history not found"));

        if (current.getEffectiveTo() != null)
            throw new IllegalArgumentException("Chỉ được xóa bản ghi giá hiện tại (effective_to = null)");

        Integer variantId = current.getVariant().getId();
        priceHistoryRepo.delete(current);

        List<PriceHistory> histories = priceHistoryRepo.findByVariant_IdOrderByEffectiveFromDesc(variantId);
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant not found"));

        if (histories.isEmpty()) {
            v.setCurrencyCode(settingService.getDefaultCurrency());
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

    public List<PriceHistoryResponse> getPriceHistory(Integer variantId) {
        return priceHistoryRepo.findByVariant_IdOrderByEffectiveFromDesc(variantId)
                .stream()
                .map(this::toPriceHistoryResponse)
                .toList();
    }

    public Map<String, Object> checkPriceBelowCost(Integer variantId) {
        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Variant not found"));
        Map<String, Object> result = new HashMap<>();
        result.put("variantId", variantId);
        result.put("price", v.getPrice());
        result.put("costPrice", v.getCostPrice());
        boolean warning = v.getCostPrice() != null
                && v.getPrice() != null
                && v.getPrice().compareTo(v.getCostPrice()) < 0;
        result.put("belowCost", warning);
        if (warning) result.put("message", "⚠️ Giá bán thấp hơn giá nhập!");
        return result;
    }

    // ================================================================
    // ✅ THÊM MỚI: Danh sách tất cả variant có giá < giá nhập (1.9)
    // ================================================================
    public List<Map<String, Object>> getAllConflictsBelowCost() {
        return variantRepo.findAll().stream()
                .filter(v -> v.getCostPrice() != null
                        && v.getPrice() != null
                        && v.getPrice().compareTo(v.getCostPrice()) < 0)
                .map(v -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("variantId", v.getId());
                    item.put("variantName", v.getVariantName());
                    item.put("sku", v.getSku());
                    item.put("productId", v.getProduct().getId());
                    item.put("price", v.getPrice());
                    item.put("costPrice", v.getCostPrice());
                    item.put("diff", v.getCostPrice().subtract(v.getPrice()));
                    return item;
                })
                .collect(Collectors.toList());
    }

    // ================================================================
    // ✅ THÊM MỚI: Tính giá cuối cho nhiều items (3.4)
    //   Input: List<{variantId, quantity}>
    //   Output: tổng subtotal, discountTotal, finalTotal + breakdown
    // ================================================================
    public Map<String, Object> calculateOrderPrice(List<Map<String, Object>> items, Integer customerId) {
        Customer customer = customerId != null
                ? customerRepository.findById(customerId).orElse(null)
                : null;
        LocalDateTime now = LocalDateTime.now();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        List<Map<String, Object>> lineItems = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Integer variantId = (Integer) item.get("variantId");
            Integer qty = (Integer) item.get("quantity");
            if (variantId == null || qty == null || qty <= 0) continue;

            ProductVariant v = variantRepo.findById(variantId).orElse(null);
            if (v == null) continue;

            BigDecimal basePrice = getBasePriceForCustomer(v, customer);
            Promotion best = promotionService.findBestPromotionForVariantAndCustomer(v, customer, now);
            BigDecimal finalUnitPrice = best != null
                    ? promotionService.computeEffectiveUnitPrice(basePrice, best)
                    : basePrice;

            // Xử lý combo: số lượng tặng miễn phí
            int freeQty = 0;
            PromotionService.Rules rules = best != null
                    ? promotionService.parseRulesPublic(best.getRulesJson())
                    : null;
            if (rules != null && rules.combo != null
                    && rules.combo.buy_qty != null && rules.combo.get_qty != null) {
                int sets = qty / rules.combo.buy_qty;
                freeQty = sets * rules.combo.get_qty;
            }

            BigDecimal lineSubtotal = basePrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal lineFinal = finalUnitPrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal lineDiscount = lineSubtotal.subtract(lineFinal);

            subtotal = subtotal.add(lineSubtotal);
            discountTotal = discountTotal.add(lineDiscount);

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("variantId", variantId);
            line.put("variantName", v.getVariantName());
            line.put("quantity", qty);
            line.put("unitPrice", basePrice);
            line.put("finalUnitPrice", finalUnitPrice);
            line.put("freeQty", freeQty);
            line.put("lineSubtotal", lineSubtotal);
            line.put("lineDiscount", lineDiscount);
            line.put("lineFinal", lineFinal);
            if (best != null) {
                line.put("promotionCode", best.getCode());
                line.put("promotionName", best.getName());
            }
            lineItems.add(line);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("subtotal", subtotal);
        result.put("discountTotal", discountTotal);
        result.put("finalTotal", subtotal.subtract(discountTotal));
        result.put("items", lineItems);
        return result;
    }

    public Map<String, Object> getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromos = promotionService.list(true);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("activePromotions", activePromos.size());

        List<ProductVariant> allVariants = variantRepo.findAll();
        long belowCostCount = allVariants.stream()
                .filter(v -> v.getCostPrice() != null
                        && v.getPrice() != null
                        && v.getPrice().compareTo(v.getCostPrice()) < 0)
                .count();
        dashboard.put("variantsBelowCost", belowCostCount);
        dashboard.put("totalVariants", allVariants.size());

        long expiringIn3Days = activePromos.stream()
                .filter(p -> p.getEndDate().isBefore(now.plusDays(3)))
                .count();
        dashboard.put("expiringIn3Days", expiringIn3Days);

        // ✅ THÊM: conflict count
        dashboard.put("conflictCount", getAllConflictsBelowCost().size());

        // ✅ THÊM: promotion conflicts
        dashboard.put("promotionConflicts", promotionService.detectConflicts().size());

        return dashboard;
    }

    // ================================================================
    // Helpers
    // ================================================================
    private PriceHistoryResponse toPriceHistoryResponse(PriceHistory ph) {
        PriceHistoryResponse dto = new PriceHistoryResponse();
        dto.setId(ph.getId());
        dto.setVariantId(ph.getVariant().getId());
        dto.setCurrencyCode(ph.getCurrencyCode());
        dto.setPrice(ph.getPrice());
        dto.setCostPrice(ph.getCostPrice());
        dto.setReason(ph.getReason());
        dto.setEffectiveFrom(ph.getEffectiveFrom());
        dto.setEffectiveTo(ph.getEffectiveTo());
        dto.setCreatedByUsername(ph.getCreatedBy() != null ? ph.getCreatedBy().getUsername() : null);
        dto.setCreatedAt(ph.getCreatedAt());
        return dto;
    }

    private static BigDecimal scaleMoney(BigDecimal v) {
        if (v == null) return null;
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}