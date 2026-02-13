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

    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;

    // ✅ ADD DISCOUNT BREAKDOWN
    private BigDecimal vipDiscountRate;
    private BigDecimal vipDiscount;
    private BigDecimal spinDiscountRate;
    private BigDecimal spinDiscount;

    private Instant createdAt;

    private List<CreateOrderResponse.Item> items;

    // Constructor WITHOUT discount breakdown (for backward compatibility)
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
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.channel = channel;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.customerId = customerId;
        this.customerName = customerName;
        this.staffId = staffId;
        this.staffUsername = staffUsername;
        this.notes = notes;
        this.subtotal = subtotal;
        this.discountTotal = discountTotal;
        this.taxTotal = taxTotal;
        this.shippingFee = shippingFee;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.items = items;

        // ✅ PARSE DISCOUNT FROM NOTES
        parseDiscountFromNotes();
    }

    // ✅ PARSE DISCOUNT BREAKDOWN FROM NOTES
    private void parseDiscountFromNotes() {
        if (notes == null || !notes.contains("Discount:")) {
            this.vipDiscountRate = BigDecimal.ZERO;
            this.vipDiscount = BigDecimal.ZERO;
            this.spinDiscountRate = BigDecimal.ZERO;
            this.spinDiscount = BigDecimal.ZERO;
            return;
        }

        try {
            // Extract VIP discount
            if (notes.contains("VIP:")) {
                String vipPart = notes.substring(notes.indexOf("VIP:"));

                // Extract VIP rate (e.g., "20.0%")
                int rateStart = vipPart.indexOf("VIP:") + 4;
                int rateEnd = vipPart.indexOf("%", rateStart);
                if (rateEnd > rateStart) {
                    String rateStr = vipPart.substring(rateStart, rateEnd).trim();
                    this.vipDiscountRate = new BigDecimal(rateStr);
                }

                // Extract VIP amount (e.g., "(-3,798,000)")
                int amountStart = vipPart.indexOf("(-") + 2;
                int amountEnd = vipPart.indexOf(")", amountStart);
                if (amountEnd > amountStart) {
                    String amountStr = vipPart.substring(amountStart, amountEnd)
                            .replace(",", "")
                            .trim();
                    this.vipDiscount = new BigDecimal(amountStr);
                }
            }

            // Extract Spin discount
            if (notes.contains("Spin:")) {
                String spinPart = notes.substring(notes.indexOf("Spin:"));

                // Extract Spin rate
                int rateStart = spinPart.indexOf("Spin:") + 5;
                int rateEnd = spinPart.indexOf("%", rateStart);
                if (rateEnd > rateStart) {
                    String rateStr = spinPart.substring(rateStart, rateEnd).trim();
                    this.spinDiscountRate = new BigDecimal(rateStr);
                }

                // Extract Spin amount
                int amountStart = spinPart.indexOf("(-") + 2;
                int amountEnd = spinPart.indexOf(")", amountStart);
                if (amountEnd > amountStart) {
                    String amountStr = spinPart.substring(amountStart, amountEnd)
                            .replace(",", "")
                            .trim();
                    this.spinDiscount = new BigDecimal(amountStr);
                }
            }
        } catch (Exception e) {
            // If parsing fails, default to zero
            this.vipDiscountRate = BigDecimal.ZERO;
            this.vipDiscount = BigDecimal.ZERO;
            this.spinDiscountRate = BigDecimal.ZERO;
            this.spinDiscount = BigDecimal.ZERO;
        }
    }
}