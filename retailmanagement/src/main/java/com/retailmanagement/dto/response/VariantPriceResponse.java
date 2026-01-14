package com.retailmanagement.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VariantPriceResponse {

    private Integer variantId;
    private Integer productId;
    private String variantName;
    private String sku;

    private String currencyCode;
    private BigDecimal price;
    private BigDecimal costPrice;

    // promo
    private String promotionCode;
    private BigDecimal finalPrice;
}
