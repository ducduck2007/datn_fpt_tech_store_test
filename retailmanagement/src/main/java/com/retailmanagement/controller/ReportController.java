package com.retailmanagement.controller;

import com.retailmanagement.service.PromotionService;
import com.retailmanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final PromotionService promotionService;

    @GetMapping("/revenue-by-channel")
    public ResponseEntity<?> revenueByChannel() {
        return ResponseEntity.ok(reportService.revenueByChannel());
    }

    @GetMapping("/revenue-by-date")
    public ResponseEntity<?> revenueByDate() {
        return ResponseEntity.ok(reportService.revenueByDate());
    }

    @GetMapping("/orders-by-staff")
    public ResponseEntity<?> ordersByStaff() {
        return ResponseEntity.ok(reportService.ordersByStaff());
    }

    /**
     * Báo cáo khuyến mãi đang áp dụng
     * GET /api/reports/promotions/active
     */
    @GetMapping("/promotions/active")
    public ResponseEntity<?> activePromotions() {
        return ResponseEntity.ok(promotionService.list(true));
    }

    /**
     * Báo cáo giá & KM theo tuần/tháng
     * GET /api/reports/promotions/summary?period=week|month
     */
    @GetMapping("/promotions/summary")
    public ResponseEntity<?> promotionSummary(
            @RequestParam(required = false, defaultValue = "month") String period) {
        return ResponseEntity.ok(promotionService.getReport(period));
    }

    /**
     * Cảnh báo xung đột khuyến mãi
     * GET /api/reports/promotions/conflicts
     */
    @GetMapping("/promotions/conflicts")
    public ResponseEntity<?> promotionConflicts() {
        return ResponseEntity.ok(promotionService.detectConflicts());
    }

    /**
     * Tổng hợp doanh thu theo trạng thái đơn hàng.
     * GET /api/reports/revenue-by-status
     * Response: [{ status, orderCount, totalRevenue }]
     */
    @GetMapping("/revenue-by-status")
    public ResponseEntity<?> revenueByStatus() {
        return ResponseEntity.ok(reportService.revenueByStatus());
    }

    /**
     * Thống kê trả hàng theo sản phẩm (loại trừ REJECTED).
     * GET /api/reports/returns-by-product
     * Response: [{ productName, sku, variantName, returnCount, totalQty, totalRefund }]
     */
    @GetMapping("/returns-by-product")
    public ResponseEntity<?> returnsByProduct() {
        return ResponseEntity.ok(reportService.returnStatsByProduct());
    }

    /**
     * ✅ THÊM MỚI: Doanh thu theo tuần (DELIVERED).
     * GET /api/reports/revenue-by-week
     * Response: [{ weekStart, revenue }]
     */
    @GetMapping("/revenue-by-week")
    public ResponseEntity<?> revenueByWeek() {
        return ResponseEntity.ok(reportService.revenueByWeek());
    }

    /**
     * ✅ THÊM MỚI: Doanh thu theo tháng (DELIVERED).
     * GET /api/reports/revenue-by-month
     * Response: [{ monthStart, revenue }]
     */
    @GetMapping("/revenue-by-month")
    public ResponseEntity<?> revenueByMonth() {
        return ResponseEntity.ok(reportService.revenueByMonth());
    }

    /**
     * ✅ THÊM MỚI: Top sản phẩm bán chạy theo số lượng (DELIVERED).
     * GET /api/reports/top-selling-products
     * Response: [{ productName, sku, variantName, totalQty, totalRevenue }]
     */
    @GetMapping("/top-selling-products")
    public ResponseEntity<?> topSellingProducts() {
        return ResponseEntity.ok(reportService.topSellingProducts());
    }
}