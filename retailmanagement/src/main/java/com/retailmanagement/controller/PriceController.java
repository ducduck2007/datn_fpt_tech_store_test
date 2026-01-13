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

    // Tạo/đổi giá variant + ghi history
    @PostMapping("/variants/{variantId}")
    public ApiResponse<PriceHistory> setVariantPrice(@PathVariable Integer variantId,
                                                     @RequestBody UpsertPriceRequest req) {
        // nếu bạn có auth: lấy userId từ SecurityContext, tạm set null/0
        PriceHistory ph = pricingService.setVariantPrice(variantId, req, 0);
        return ApiResponse.success(ph);
    }

    // Danh sách giá theo sản phẩm (kèm finalPrice sau promo)
    @GetMapping("/products/{productId}")
    public ApiResponse<List<VariantPriceResponse>> listByProduct(@PathVariable Integer productId) {
        return ApiResponse.success(pricingService.listCurrentPricesByProduct(productId));
    }

    // Sửa bản ghi giá hiện tại
    @PutMapping("/history/{id}")
    public ApiResponse<PriceHistory> updateLatest(@PathVariable Long id, @RequestBody UpsertPriceRequest req) {
        return ApiResponse.success(pricingService.updateLatestHistory(id, req));
    }

    // Xóa bản ghi giá hiện tại và rollback
    @DeleteMapping("/history/{id}")
    public ApiResponse<String> deleteLatest(@PathVariable Long id) {
        pricingService.deleteLatestAndRollback(id);
        return ApiResponse.success("OK");
    }

    // Giá hiện tại sau khuyến mãi theo variant
    @GetMapping("/variants/{variantId}/effective")
    public ApiResponse<VariantPriceResponse> getEffective(@PathVariable Integer variantId) {
        return ApiResponse.success(pricingService.getEffectivePrice(variantId));
    }
}
