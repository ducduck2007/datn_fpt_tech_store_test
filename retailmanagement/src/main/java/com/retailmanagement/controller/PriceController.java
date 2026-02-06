package com.retailmanagement.controller;

import com.retailmanagement.dto.request.UpsertPriceRequest;
import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.dto.response.VariantPriceResponse;
import com.retailmanagement.entity.PriceHistory;
import com.retailmanagement.service.PricingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PricingService pricingService;

    public PriceController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * Set price for a variant (base price table)
     */
    @PostMapping("/variants/{variantId}")
    public ApiResponse<PriceHistory> setVariantPrice(@PathVariable Integer variantId,
                                                     @RequestBody UpsertPriceRequest req) {
        PriceHistory ph = pricingService.setVariantPrice(variantId, req, 0);
        return ApiResponse.success(ph);
    }

    /**
     * List all variant prices for a product (base prices only)
     */
    @GetMapping("/products/{productId}")
    public ApiResponse<List<VariantPriceResponse>> listByProduct(@PathVariable Integer productId) {
        return ApiResponse.success(pricingService.listCurrentPricesByProduct(productId));
    }

    /**
     * List variant prices for a product with customer group pricing applied
     */
    @GetMapping("/products/{productId}/customer/{customerId}")
    public ApiResponse<List<VariantPriceResponse>> listByProductForCustomer(
            @PathVariable Integer productId,
            @PathVariable Integer customerId) {
        return ApiResponse.success(pricingService.listCurrentPricesByProductForCustomer(productId, customerId));
    }

    /**
     * Update price history record
     */
    @PutMapping("/history/{id}")
    public ApiResponse<PriceHistory> updateLatest(@PathVariable Long id, @RequestBody UpsertPriceRequest req) {
        return ApiResponse.success(pricingService.updateLatestHistory(id, req));
    }

    /**
     * Delete latest price and rollback to previous
     */
    @DeleteMapping("/history/{id}")
    public ApiResponse<String> deleteLatest(@PathVariable Long id) {
        pricingService.deleteLatestAndRollback(id);
        return ApiResponse.success("OK");
    }

    /**
     * Get effective price for a variant (base price without customer context)
     */
    @GetMapping("/variants/{variantId}/effective")
    public ApiResponse<VariantPriceResponse> getEffective(@PathVariable Integer variantId) {
        return ApiResponse.success(pricingService.getEffectivePrice(variantId));
    }

    /**
     * Get effective price for a variant with customer-specific pricing
     * Returns final price after:
     * 1. Customer group/tier base discount
     * 2. Best applicable promotion
     */
    @GetMapping("/variants/{variantId}/effective/customer/{customerId}")
    public ApiResponse<VariantPriceResponse> getEffectiveForCustomer(
            @PathVariable Integer variantId,
            @PathVariable Integer customerId) {
        return ApiResponse.success(pricingService.getEffectivePriceForCustomer(variantId, customerId));
    }
}
