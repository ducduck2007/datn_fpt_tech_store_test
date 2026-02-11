package com.retailmanagement.controller;

import com.retailmanagement.dto.request.CreateReturnRequest;
import com.retailmanagement.dto.request.ProcessReturnRequest;
import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.dto.response.ReturnResponse;
import com.retailmanagement.service.ReturnService;
import com.retailmanagement.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    /**
     * =========================
     * CUSTOMER
     * =========================
     */

    // Tạo yêu cầu trả hàng
    @PostMapping
    public ApiResponse<ReturnResponse> createReturn(
            @RequestBody CreateReturnRequest request
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(
                returnService.createReturn(request, userId)
        );
    }

    // Xem lịch sử return của chính mình
    @GetMapping("/me")
    public ApiResponse<List<ReturnResponse>> getMyReturns() {
        Integer userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(
                returnService.getReturnsByCustomer(userId)
        );
    }

    // Customer hủy yêu cầu return (khi còn PENDING)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelReturn(
            @PathVariable Long id
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        returnService.cancelReturn(id, userId);
        return ApiResponse.success(null);
    }

    /**
     * =========================
     * ADMIN / SALES
     * =========================
     */

    // Duyệt return (APPROVE)
    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approveReturn(
            @PathVariable Long id
    ) {
        Integer staffId = SecurityUtil.getCurrentUserId();
        returnService.approveReturn(id, staffId);
        return ApiResponse.success(null);
    }

    // Từ chối return (REJECT)
    @PutMapping("/{id}/reject")
    public ApiResponse<Void> rejectReturn(
            @PathVariable Long id,
            @RequestBody ProcessReturnRequest request
    ) {
        Integer staffId = SecurityUtil.getCurrentUserId();
        returnService.rejectReturn(id, request.getNote(), staffId);
        return ApiResponse.success(null);
    }

    /**
     * (OPTIONAL – nếu bạn vẫn muốn dùng process chung)
     * Duyệt & hoàn tiền qua 1 endpoint
     */
    @PostMapping("/{id}/process")
    public ApiResponse<ReturnResponse> processReturn(
            @PathVariable Long id,
            @RequestBody ProcessReturnRequest request
    ) {
        Integer staffId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(
                returnService.processReturn(id, request.getNote(), staffId)
        );
    }

    /**
     * =========================
     * QUERY
     * =========================
     */

    // Danh sách tất cả return
    @GetMapping
    public ApiResponse<List<ReturnResponse>> getAllReturns() {
        return ApiResponse.success(returnService.getAllReturns());
    }

    // Danh sách return theo trạng thái (PENDING / APPROVED / REJECTED)
    @GetMapping("/status/{status}")
    public ApiResponse<List<ReturnResponse>> getReturnsByStatus(
            @PathVariable String status
    ) {
        return ApiResponse.success(
                returnService.getReturnsByStatus(status)
        );
    }

    // Danh sách return theo đơn hàng
    @GetMapping("/order/{orderId}")
    public ApiResponse<List<ReturnResponse>> getReturnsByOrder(
            @PathVariable Long orderId
    ) {
        return ApiResponse.success(
                returnService.getReturnsByOrder(orderId)
        );
    }

    // Chi tiết 1 return
    @GetMapping("/{id}")
    public ApiResponse<ReturnResponse> getReturnDetail(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                returnService.getReturnDetail(id)
        );
    }
}
