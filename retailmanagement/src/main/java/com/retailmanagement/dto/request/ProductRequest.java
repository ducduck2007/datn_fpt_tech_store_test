package com.retailmanagement.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductRequest {
    private String name;
    private String sku;
    private String description;
    private Boolean isVisible;

    private Integer categoryId;

    private MultipartFile imageFile;
}
