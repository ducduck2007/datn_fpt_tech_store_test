package com.retailmanagement.controller;

import com.retailmanagement.dto.request.ProductRequest;
import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.dto.response.ProductResponse;
import com.retailmanagement.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> createProduct(@ModelAttribute ProductRequest request) throws IOException {
        productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm thành công", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer categoryId
    ) {
        Page<ProductResponse> products = productService.getProducts(page, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", products));
    }
}
