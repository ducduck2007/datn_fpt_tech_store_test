package com.retailmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderResponse {

    private Long orderId;
    private String orderNumber;
    private String status;
    private String paymentStatus;

    // Customer (flatten)
    private Integer customerId;
    private String customerName;

    // Staff (flatten)
    private Integer staffId;
    private String staffUsername;

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;

    private Instant createdAt;

    // Items (list đơn giản)
    private List<Item> items;

    // Discount breakdown
    private BigDecimal vipDiscountRate;
    private BigDecimal vipDiscount;
    private BigDecimal spinDiscountRate;
    private BigDecimal spinDiscount;
    private boolean hasSpinBonus;

    // ✅ THÊM MỚI: promotion code discount
    private String promoCode;
    private BigDecimal promoDiscount;

    @Data
    @AllArgsConstructor
    public static class Item {

        private Integer productId;
        private Integer variantId;

        private String productName;
        private String variantName;
        private String sku;

        private Integer quantity;

        private BigDecimal price;
        private BigDecimal discount;
        private BigDecimal lineTotal;
    }
}