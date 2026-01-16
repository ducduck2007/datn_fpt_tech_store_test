package com.retailmanagement.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Integer id;
    private String name;
    private String sku;
    private String description;
    private Boolean isVisible;
    private String imageUrl;
    private LocalDateTime createdAt;

    // ✅ Added for "Display current price in product list"
    // We return the MIN current price among variants (common storefront behavior).
    private BigDecimal minPrice;
    private String currencyCode;
}
