package com.retailmanagement.dto.response;

import lombok.Data;
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
}
