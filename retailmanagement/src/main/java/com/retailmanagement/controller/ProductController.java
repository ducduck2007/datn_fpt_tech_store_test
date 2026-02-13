package com.retailmanagement.controller;

import com.retailmanagement.dto.request.ProductRequest;
import com.retailmanagement.dto.response.ProductResponse;
import com.retailmanagement.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<Integer> categoryIds,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy
    ) {
        return ResponseEntity.ok(productService.getProducts(page, categoryIds, keyword, sortBy));
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

    @GetMapping("/trash")
    public ResponseEntity<Page<ProductResponse>> getTrash(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(productService.getTrashProducts(page));
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreProduct(@PathVariable Integer id) {
        productService.restoreProduct(id);
        return ResponseEntity.ok("Khôi phục sản phẩm thành công");
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<?> hardDeleteProduct(@PathVariable Integer id) {
        productService.hardDeleteProduct(id);
        return ResponseEntity.ok("Đã xóa vĩnh viễn sản phẩm");
    }

    @PutMapping("/{id}/images/{imageId}/primary")
    public ResponseEntity<?> setPrimaryImage(@PathVariable Integer id, @PathVariable Long imageId) {
        productService.setPrimaryImage(id, imageId);
        return ResponseEntity.ok("Đã đặt làm ảnh đại diện thành công");
    }
}