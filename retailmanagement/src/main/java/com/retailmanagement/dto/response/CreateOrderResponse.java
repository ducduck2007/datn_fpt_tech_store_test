package com.retailmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
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

    @Data
    @AllArgsConstructor
    public static class Item {
        private String productName;
        private String variantName;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private BigDecimal lineTotal;
    }
}


