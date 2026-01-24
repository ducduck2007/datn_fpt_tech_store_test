package com.retailmanagement.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String sku;
    private String description;
    private Boolean isVisible;

    private Integer categoryId;

    private MultipartFile imageFile;

    private List<Integer> categoryIds;

    private List<AttributeRequest> attributes;

    private List<MultipartFile> galleryImages;

    private List<Long> idsToDelete;
}