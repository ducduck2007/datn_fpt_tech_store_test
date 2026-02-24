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
    // Parse discount breakdown từ notes
    //
    // OrderService ghi notes theo format:
    //   "VIP Diamond: -5% (-1,500,000) | Spin: -3% (-900,000)"
    //   "VIP Bronze: -100,000 (đơn từ 5,000,000)"   ← FIXED amount
    //   "VIP Gold: không áp dụng (đơn < 5,000,000)"
    //
    // Regex-free parsing để tránh lỗi trên mọi JVM.
    // ================================================================
    private void parseDiscountFromNotes() {
        this.vipDiscountRate  = BigDecimal.ZERO;
        this.vipDiscount      = BigDecimal.ZERO;
        this.spinDiscountRate = BigDecimal.ZERO;
        this.spinDiscount     = BigDecimal.ZERO;

        if (notes == null || notes.isBlank()) return;

        // Tách từng segment bởi " | "
        String[] segments = notes.split("\\|");

        for (String seg : segments) {
            String s = seg.trim();

            // ── VIP segment ──────────────────────────────────────────
            if (s.startsWith("VIP ") && !s.contains("không áp dụng")) {

                // Case 1: Percentage — "VIP Diamond: -5% (-1,500,000)"
                if (s.contains("%")) {
                    // Extract rate: số trước dấu %
                    int pctIdx = s.indexOf('%');
                    // tìm số bắt đầu từ sau dấu '-' hoặc ':' gần nhất trước '%'
                    int dashIdx = s.lastIndexOf('-', pctIdx);
                    if (dashIdx >= 0) {
                        String rateStr = s.substring(dashIdx + 1, pctIdx).trim();
                        this.vipDiscountRate = parseSafe(rateStr);
                    }

                    // Extract amount: số trong ngoặc "(-X,XXX)"
                    this.vipDiscount = extractParenAmount(s);
                }
                // Case 2: Fixed amount — "VIP Bronze: -100,000 (đơn từ ...)"
                else if (s.contains("-")) {
                    int dashIdx = s.lastIndexOf('-');
                    // lấy đến khoảng trắng hoặc '(' tiếp theo
                    int endIdx = s.indexOf('(', dashIdx);
                    String amtStr = endIdx > dashIdx
                            ? s.substring(dashIdx + 1, endIdx).trim()
                            : s.substring(dashIdx + 1).trim();
                    amtStr = amtStr.replace(",", "").replace(".", "");
                    this.vipDiscount = parseSafe(amtStr);
                    // Fixed discount → vipDiscountRate = 0 (không có %)
                }
            }

            // ── Spin segment ─────────────────────────────────────────
            // Format: "Spin: -3% (-900,000)"
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
        }
    }

    /**
     * Lấy số trong ngoặc cuối cùng: "(-1,234,567)" → 1234567
     */
    private BigDecimal extractParenAmount(String s) {
        int openParen  = s.lastIndexOf("(-");
        int closeParen = s.lastIndexOf(')');
        if (openParen >= 0 && closeParen > openParen) {
            String raw = s.substring(openParen + 2, closeParen)
                    .replace(",", "")
                    .replace(".", "")
                    .trim();
            return parseSafe(raw);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal parseSafe(String s) {
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}