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

    @PostMapping
    public ApiResponse<Promotion> create(@RequestBody PromotionRequest req) {
        return ApiResponse.success(promotionService.create(req, 0));
    }

    // activeOnly=true => chỉ lấy khuyến mãi đang hoạt động theo thời gian + isActive=true
    @GetMapping
    public ApiResponse<List<Promotion>> list(@RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.success(promotionService.list(activeOnly));
    }

    @PutMapping("/{id}")
    public ApiResponse<Promotion> update(@PathVariable Integer id, @RequestBody PromotionRequest req) {
        return ApiResponse.success(promotionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        promotionService.delete(id);
        return ApiResponse.success("OK");
    }
}
