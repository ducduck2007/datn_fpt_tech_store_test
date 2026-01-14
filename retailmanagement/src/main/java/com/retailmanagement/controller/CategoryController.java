package com.retailmanagement.controller;

import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.entity.Category;
import com.retailmanagement.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());
        category.setIsActive(true);
        category.setDisplayOrder(0);

        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.success("Tạo danh mục thành công", saved));
    }
}