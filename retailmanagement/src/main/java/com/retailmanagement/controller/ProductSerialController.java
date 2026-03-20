package com.retailmanagement.controller;

import com.retailmanagement.dto.request.ProductSerialRequest;
import com.retailmanagement.entity.ProductSerial;
import com.retailmanagement.entity.ProductVariant;
import com.retailmanagement.repository.ProductSerialRepository;
import com.retailmanagement.repository.ProductVariantRepository;
import com.retailmanagement.service.ProductSerialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/variants")
@RequiredArgsConstructor
public class ProductSerialController {

    private final ProductSerialRepository serialRepository;
    private final ProductSerialService productSerialService;
    private final ProductVariantRepository variantRepository;

    // 1. Lấy danh sách tất cả Seri của 1 Biến thể
    @GetMapping("/{variantId}/serials")
    public ResponseEntity<List<ProductSerial>> getSerialsByVariant(@PathVariable Integer variantId) {
        return ResponseEntity.ok(serialRepository.findByVariantId(variantId));
    }

    // 2. Thêm mới một danh sách các Số Seri vào kho (Nhập kho)
    @PostMapping("/{variantId}/serials")
    @Transactional
    public ResponseEntity<String> addSerialsToVariant(
            @PathVariable Integer variantId,
            @Valid @RequestBody ProductSerialRequest request) {

        List<ProductSerial> newSerials = new ArrayList<>();

        for (String sn : request.getSerialNumbers()) {
            if(sn == null || sn.trim().isEmpty()) continue;

            ProductSerial serial = new ProductSerial();
            serial.setVariantId(variantId);
            serial.setSerialNumber(sn.trim());
            serial.setStatus("IN_STOCK");
            newSerials.add(serial);
        }

        try {
            serialRepository.saveAll(newSerials);
            return ResponseEntity.ok("Đã thêm thành công " + newSerials.size() + " số Seri vào kho.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: Có thể số Seri đã bị trùng lặp trong hệ thống.");
        }
    }

    // 3. Xóa một số Seri cụ thể (Trường hợp nhập sai hoặc máy bị hỏng, trả về kho)
    @DeleteMapping("/serials/{serialId}")
    public ResponseEntity<String> deleteSerial(@PathVariable Long serialId) {
        serialRepository.deleteById(serialId);
        return ResponseEntity.ok("Đã xóa số Seri thành công.");
    }
    @PostMapping("/{variantId}/serials/generate")
    public ResponseEntity<?> generateSerials(
            @PathVariable Integer variantId,
            @RequestParam(defaultValue = "1") int quantity) {

        if (quantity < 1 || quantity > 500) {
            return ResponseEntity.badRequest().body("Số lượng phải từ 1 đến 500");
        }

        List<ProductSerial> generated = productSerialService.generateAndSave(variantId, quantity);

        return ResponseEntity.ok(Map.of(
                "message", "Đã gen thành công " + generated.size() + " serial",
                "serials", generated.stream().map(ProductSerial::getSerialNumber).toList()
        ));
    }

    @GetMapping("/serials/all")
    public ResponseEntity<?> getAllSerialsGlobal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        if (status != null && status.trim().isEmpty()) status = null;

        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").descending());
        Page<ProductSerial> serialPage = serialRepository.searchSerials(keyword, status, pageable);

        List<Map<String, Object>> content = serialPage.getContent().stream().map(s -> {
            ProductVariant v = variantRepository.findById(s.getVariantId()).orElse(null);
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", s.getId());
            item.put("serialNumber", s.getSerialNumber());
            item.put("status", s.getStatus());
            item.put("createdAt", s.getCreatedAt());
            item.put("variantName", v != null ? v.getVariantName() : "N/A");
            item.put("productName", (v != null && v.getProduct() != null) ? v.getProduct().getName() : "Không xác định");
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(content, pageable, serialPage.getTotalElements()));
    }

    @PutMapping("/serials/{id}/status")
    public ResponseEntity<?> updateSerialStatus(@PathVariable Long id, @RequestParam String status) {
        ProductSerial serial = serialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy số Seri ID: " + id));
        serial.setStatus(status);
        serialRepository.save(serial);
        ProductVariant variant = variantRepository.findById(serial.getVariantId()).orElse(null);
        if (variant != null) {
            int currentStock = serialRepository.countByVariantIdAndStatus(variant.getId(), "IN_STOCK");
            variant.setStockQuantity(currentStock);
            if (currentStock <= 0) {
                variant.setIsActive(false);
            } else {
                variant.setIsActive(true);
            }
            variantRepository.save(variant);
        }
        return ResponseEntity.ok("Đã cập nhật trạng thái Seri thành " + status + " và đồng bộ tồn kho tự động!");
    }
}