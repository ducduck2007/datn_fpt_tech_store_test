package com.retailmanagement.controller;

import com.retailmanagement.dto.request.ProductRequest;
import com.retailmanagement.dto.response.ProductResponse;
import com.retailmanagement.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy
    ) {
        return ResponseEntity.ok(productService.getProducts(page, categoryId, keyword, sortBy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@ModelAttribute ProductRequest request) throws IOException {
        productService.createProduct(request);
        return ResponseEntity.ok("Thêm sản phẩm thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @ModelAttribute ProductRequest request) throws IOException {
        productService.updateProduct(id, request);
        return ResponseEntity.ok("Cập nhật sản phẩm thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.ok("Đã ẩn sản phẩm thành công (Soft Delete)");
    }
}