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

    @PostMapping("/variants/{variantId}")
    public ApiResponse<PriceHistory> setVariantPrice(@PathVariable Integer variantId,
                                                     @RequestBody UpsertPriceRequest req) {
        PriceHistory ph = pricingService.setVariantPrice(variantId, req, 0);
        return ApiResponse.success(ph);
    }

    @GetMapping("/products/{productId}")
    public ApiResponse<List<VariantPriceResponse>> listByProduct(@PathVariable Integer productId) {
        return ApiResponse.success(pricingService.listCurrentPricesByProduct(productId));
    }

    @PutMapping("/history/{id}")
    public ApiResponse<PriceHistory> updateLatest(@PathVariable Long id, @RequestBody UpsertPriceRequest req) {
        return ApiResponse.success(pricingService.updateLatestHistory(id, req));
    }

    @DeleteMapping("/history/{id}")
    public ApiResponse<String> deleteLatest(@PathVariable Long id) {
        pricingService.deleteLatestAndRollback(id);
        return ApiResponse.success("OK");
    }

    @GetMapping("/variants/{variantId}/effective")
    public ApiResponse<VariantPriceResponse> getEffective(@PathVariable Integer variantId) {
        return ApiResponse.success(pricingService.getEffectivePrice(variantId));
    }
}
