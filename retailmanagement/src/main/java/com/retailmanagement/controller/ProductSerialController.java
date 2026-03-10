package com.retailmanagement.controller;

import com.retailmanagement.dto.request.ProductSerialRequest;
import com.retailmanagement.entity.ProductSerial;
import com.retailmanagement.repository.ProductSerialRepository;
import com.retailmanagement.service.ProductSerialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/variants")
@RequiredArgsConstructor
public class ProductSerialController {

    private final ProductSerialRepository serialRepository;
    private final ProductSerialService productSerialService;

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
}