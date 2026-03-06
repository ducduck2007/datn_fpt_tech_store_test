package com.retailmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {

    private Long orderId;
    private String orderNumber;
    private String channel;
    private String paymentMethod;
    private String status;
    private String paymentStatus;

    private Integer customerId;
    private String customerName;

    private Integer staffId;
    private String staffUsername;

    private String notes;

    // Discount breakdown fields
    private BigDecimal vipDiscountRate  = BigDecimal.ZERO;
    private BigDecimal vipDiscount      = BigDecimal.ZERO;
    private BigDecimal spinDiscountRate = BigDecimal.ZERO;
    private BigDecimal spinDiscount     = BigDecimal.ZERO;

    // ✅ THÊM: Promotion code discount
    private String     promoCode        = null;
    private BigDecimal promoDiscount    = BigDecimal.ZERO;

    // ✅ THÊM: Combo info (parse từ appliedPromotionJson)
    private String     comboInfo        = null;  // e.g. "Mua 2 tặng 1"

    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;

    private Instant createdAt;
    private List<CreateOrderResponse.Item> items;

    // ================================================================
    // Constructor (backward compatible) — used by OrderService
    // ================================================================
    public OrderDetailResponse(
            Long orderId,
            String orderNumber,
            String channel,
            String paymentMethod,
            String status,
            String paymentStatus,
            Integer customerId,
            String customerName,
            Integer staffId,
            String staffUsername,
            String notes,
            BigDecimal subtotal,
            BigDecimal discountTotal,
            BigDecimal taxTotal,
            BigDecimal shippingFee,
            BigDecimal totalAmount,
            Instant createdAt,
            List<CreateOrderResponse.Item> items
    ) {
        this.orderId       = orderId;
        this.orderNumber   = orderNumber;
        this.channel       = channel;
        this.paymentMethod = paymentMethod;
        this.status        = status;
        this.paymentStatus = paymentStatus;
        this.customerId    = customerId;
        this.customerName  = customerName;
        this.staffId       = staffId;
        this.staffUsername = staffUsername;
        this.notes         = notes;
        this.subtotal      = subtotal;
        this.discountTotal = discountTotal;
        this.taxTotal      = taxTotal;
        this.shippingFee   = shippingFee;
        this.totalAmount   = totalAmount;
        this.createdAt     = createdAt;
        this.items         = items;

        parseDiscountFromNotes();
    }

    // ================================================================
    // Constructor đầy đủ với promoCode + appliedPromotionJson
    // ================================================================
    public OrderDetailResponse(
            Long orderId,
            String orderNumber,
            String channel,
            String paymentMethod,
            String status,
            String paymentStatus,
            Integer customerId,
            String customerName,
            Integer staffId,
            String staffUsername,
            String notes,
            BigDecimal subtotal,
            BigDecimal discountTotal,
            BigDecimal taxTotal,
            BigDecimal shippingFee,
            BigDecimal totalAmount,
            Instant createdAt,
            List<CreateOrderResponse.Item> items,
            String promoCode,
            String appliedPromotionJson
    ) {
        this(orderId, orderNumber, channel, paymentMethod, status, paymentStatus,
                customerId, customerName, staffId, staffUsername, notes,
                subtotal, discountTotal, taxTotal, shippingFee, totalAmount, createdAt, items);
        this.promoCode = promoCode;
        this.comboInfo = parseComboInfo(appliedPromotionJson);
    }

    // ================================================================
    // Parse combo info từ appliedPromotionJson
    // Format JSON: {"code":"X","discountType":"COMBO","buyQty":2,"getQty":1,...}
    // ================================================================
    private String parseComboInfo(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            if (json.contains("\"buy_qty\"") || json.contains("buyQty")) {
                // Tìm buy_qty
                Integer buy = extractJsonInt(json, "buy_qty");
                if (buy == null) buy = extractJsonInt(json, "buyQty");
                Integer get = extractJsonInt(json, "get_qty");
                if (get == null) get = extractJsonInt(json, "getQty");
                if (buy != null && get != null) {
                    return "Mua " + buy + " tặng " + get;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Integer extractJsonInt(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        if (end == start) return null;
        try { return Integer.parseInt(json.substring(start, end)); } catch (Exception e) { return null; }
    }

    // ================================================================
    // Parse discount breakdown từ notes
    // ================================================================
    private void parseDiscountFromNotes() {
        this.vipDiscountRate  = BigDecimal.ZERO;
        this.vipDiscount      = BigDecimal.ZERO;
        this.spinDiscountRate = BigDecimal.ZERO;
        this.spinDiscount     = BigDecimal.ZERO;
        this.promoDiscount    = BigDecimal.ZERO;

        if (notes == null || notes.isBlank()) return;

        String[] segments = notes.split("\\|");

        for (String seg : segments) {
            String s = seg.trim();

            // ── VIP segment ──────────────────────────────────────────
            if (s.startsWith("VIP ") && !s.contains("không áp dụng")) {
                if (s.contains("%")) {
                    int pctIdx = s.indexOf('%');
                    int dashIdx = s.lastIndexOf('-', pctIdx);
                    if (dashIdx >= 0) {
                        String rateStr = s.substring(dashIdx + 1, pctIdx).trim();
                        this.vipDiscountRate = parseSafe(rateStr);
                    }
                    this.vipDiscount = extractParenAmount(s);
                } else if (s.contains("-")) {
                    int dashIdx = s.lastIndexOf('-');
                    int endIdx = s.indexOf('(', dashIdx);
                    String amtStr = endIdx > dashIdx
                            ? s.substring(dashIdx + 1, endIdx).trim()
                            : s.substring(dashIdx + 1).trim();
                    amtStr = amtStr.replace(",", "").replace(".", "");
                    this.vipDiscount = parseSafe(amtStr);
                }
            }

            // ── Spin segment ─────────────────────────────────────────
            if (s.startsWith("Spin:")) {
                if (s.contains("%")) {
                    int pctIdx = s.indexOf('%');
                    int dashIdx = s.lastIndexOf('-', pctIdx);
                    if (dashIdx >= 0) {
                        String rateStr = s.substring(dashIdx + 1, pctIdx).trim();
                        this.spinDiscountRate = parseSafe(rateStr);
                    }
                    this.spinDiscount = extractParenAmount(s);
                }
            }

            // ── Promo code segment ───────────────────────────────────
            // Format: "Mã: SUMMER10: -50,000"
            if (s.startsWith("Mã:")) {
                int lastDash = s.lastIndexOf('-');
                if (lastDash >= 0) {
                    String amtStr = s.substring(lastDash + 1).trim()
                            .replace(",", "").replace(".", "");
                    this.promoDiscount = parseSafe(amtStr);
                }
                // Extract code between "Mã: " and ":"
                String after = s.substring(3).trim(); // remove "Mã:"
                int colonIdx = after.indexOf(':');
                if (colonIdx > 0) {
                    this.promoCode = after.substring(0, colonIdx).trim();
                }
            }
        }
    }

    private BigDecimal extractParenAmount(String s) {
        int openParen  = s.lastIndexOf("(-");
        int closeParen = s.lastIndexOf(')');
        if (openParen >= 0 && closeParen > openParen) {
            String raw = s.substring(openParen + 2, closeParen)
                    .replace(".", "")
                    .replace(",", "")
                    .trim();
            return parseSafe(raw);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal parseSafe(String s) {
        try {
            String cleaned = s.trim().replaceAll("[^0-9]", "");
            if (cleaned.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}