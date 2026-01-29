package com.retailmanagement.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentItem {
    private Long itemId;
    private Integer productId;
    private String productName;
    private Integer variantId;
    private String variantName;
    private String sku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal lineTotal;
    private String imageUrl;
}
