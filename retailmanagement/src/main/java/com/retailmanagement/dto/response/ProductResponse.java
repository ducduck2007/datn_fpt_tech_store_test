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
    private String attributes;
    private java.util.List<ImageResponse> images;

    // Price information
    private BigDecimal minPrice;
    private String currencyCode;
    
    // Promotion information (shown when there's an active promotion)
    private BigDecimal promotionPrice;  // Final price after promotion
    private String promotionName;       // Name of the promotion
    private String promotionCode;       // Code of the promotion
    
    // Additional fields
    private Integer variantId;
    private Integer categoryId;
}
