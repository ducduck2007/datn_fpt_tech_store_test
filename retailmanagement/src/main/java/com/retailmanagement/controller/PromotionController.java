package com.retailmanagement.controller;

import com.retailmanagement.dto.request.PromotionRequest;
import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.entity.Promotion;
import com.retailmanagement.service.PromotionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * Create a new promotion
     * Supports:
     * - Percentage/Amount discounts
     * - Buy X Get Y combos
     * - Usage limits
     * - Priority levels
     * - Customer group restrictions
     */
    @PostMapping
    public ApiResponse<Promotion> create(@RequestBody PromotionRequest req) {
        return ApiResponse.success(promotionService.create(req, 0));
    }

    /**
     * List promotions
     * @param activeOnly - if true, only return currently active promotions
     */
    @GetMapping
    public ApiResponse<List<Promotion>> list(@RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.success(promotionService.list(activeOnly));
    }

    /**
     * Get promotion details by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<Promotion> getById(@PathVariable Integer id) {
        return ApiResponse.success(promotionService.list(null).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Promotion not found")));
    }

    /**
     * Update an existing promotion
     * All fields are optional - only provided fields will be updated
     */
    @PutMapping("/{id}")
    public ApiResponse<Promotion> update(@PathVariable Integer id, @RequestBody PromotionRequest req) {
        return ApiResponse.success(promotionService.update(id, req));
    }

    /**
     * Soft delete a promotion (sets isActive = false)
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        promotionService.delete(id);
        return ApiResponse.success("Promotion deactivated successfully");
    }

    /**
     * Record redemption (increment usage counter)
     * Used when a promotion is applied to an order
     */
    @PostMapping("/{id}/redeem")
    public ApiResponse<String> recordRedemption(@PathVariable Integer id) {
        promotionService.recordRedemption(id, 1);
        return ApiResponse.success("Redemption recorded");
    }
}
