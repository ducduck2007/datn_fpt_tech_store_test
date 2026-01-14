package com.retailmanagement.controller;

import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.entity.Category;
import com.retailmanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.success("Tạo danh mục thành công", saved));
    }
}
