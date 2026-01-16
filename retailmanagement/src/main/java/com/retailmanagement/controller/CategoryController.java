package com.retailmanagement.controller;

import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.dto.response.CategoryResponse;
import com.retailmanagement.entity.Category;
import com.retailmanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ✅ Added: category list for product filter UI
    // activeOnly=true by default to avoid showing disabled categories
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories(
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        List<Category> categories = activeOnly
                ? categoryRepository.findByIsActiveTrueOrderByDisplayOrderAscNameAsc()
                : categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();

        List<CategoryResponse> result = categories.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục thành công", result));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .displayOrder(c.getDisplayOrder())
                .isActive(c.getIsActive())
                .build();
    }
}
