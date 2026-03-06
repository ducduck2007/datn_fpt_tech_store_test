package com.retailmanagement.controller;

import com.retailmanagement.dto.request.PromotionRequest;
import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.Promotion;
import com.retailmanagement.entity.User;
import com.retailmanagement.repository.PromotionRepository;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.UserRepository;
import com.retailmanagement.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor // ✅ FIX 1: dùng Lombok inject — không cần constructor thủ công
public class PromotionController {

    // ✅ FIX 2: khai báo đủ 3 field để @RequiredArgsConstructor inject
    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;
    private final CustomRes customerRepository; // ✅ FIX 3: dùng CustomRes (entity repo) thay CustomerService để lấy
                                                // Customer entity
    private final UserRepository userRepository;

    // ================================================================
    // ADMIN CRUD
    // ================================================================

    @PostMapping
    public ApiResponse<Promotion> create(@RequestBody PromotionRequest req) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User chưa đăng nhập");
        }

        String email = auth.getName();

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return ApiResponse.success(promotionService.create(req, user.getId()));
    }

    /**
     * List promotions
     * 
     * @param activeOnly - if true, only return currently active promotions
     */
    @GetMapping
    public ApiResponse<List<Promotion>> list(@RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.success(promotionService.list(activeOnly));
    }

   // ✅ FIX: dùng promoRepo.findById thay vì list().stream().filter()
    @GetMapping("/{id}")
    public ApiResponse<Promotion> getById(@PathVariable Integer id) {
        return ApiResponse.success(
            promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found: " + id))
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<Promotion> update(@PathVariable Integer id, @RequestBody PromotionRequest req) {
        return ApiResponse.success(promotionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        promotionService.delete(id);
        return ApiResponse.success("Promotion deactivated successfully");
    }

    // Dashboard: tổng hợp khuyến mãi theo tuần/tháng
    @GetMapping("/report")
    public ApiResponse<Map<String, Object>> getReport(
            @RequestParam(required = false) String period) { // "week" | "month"
        return ApiResponse.success(promotionService.getReport(period));
    }

    // Cảnh báo xung đột khuyến mãi
    @GetMapping("/conflicts")
    public ApiResponse<List<Map<String, Object>>> getConflicts() {
        return ApiResponse.success(promotionService.detectConflicts());
    }

    // Cảnh báo khuyến mãi sắp hết hạn (trong N ngày)
    @GetMapping("/expiring")
    public ApiResponse<List<Promotion>> getExpiring(
            @RequestParam(defaultValue = "3") int withinDays) {
        return ApiResponse.success(promotionService.getExpiringPromotions(withinDays));
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

    // ================================================================
    // ✅ VALIDATE MÃ — FE gọi trước khi tạo đơn / thanh toán
    // GET /api/promotions/validate?code=BIRTHDAY250K&orderTotal=1500000
    // ================================================================
    @GetMapping("/validate") // ✅ FIX: bỏ prefix "/api/promotions" thừa (class đã có @RequestMapping)
    public ResponseEntity<Map<String, Object>> validateCode(
            @RequestParam String code,
            @RequestParam(defaultValue = "0") BigDecimal orderTotal) {
        try {
            String upper = code.trim().toUpperCase();
            Promotion promo = promotionRepository.findByCode(upper).orElse(null);

            if (promo == null) {
                return ResponseEntity.ok(Map.of("valid", false, "message", "Mã '" + upper + "' không tồn tại"));
            }

            LocalDateTime now = LocalDateTime.now();
            if (!promo.getIsActive()
                    || promo.getStartDate().isAfter(now)
                    || promo.getEndDate().isBefore(now)) {
                return ResponseEntity
                        .ok(Map.of("valid", false, "message", "Mã '" + upper + "' đã hết hạn hoặc chưa có hiệu lực"));
            }

            if (promo.getMinOrderAmount() != null
                    && orderTotal.compareTo(promo.getMinOrderAmount()) < 0) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", String.format(
                                "Đơn hàng cần tối thiểu %,.0f đ (hiện tại: %,.0f đ)",
                                promo.getMinOrderAmount().doubleValue(),
                                orderTotal.doubleValue())));
            }

            BigDecimal discountAmount = promotionService.applyDiscount(
                    orderTotal, promo.getDiscountType(), promo.getDiscountValue());
            if (discountAmount.compareTo(orderTotal) > 0)
                discountAmount = orderTotal;

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Mã hợp lệ",
                    "code", upper,
                    "discountType", promo.getDiscountType(),
                    "discountValue", promo.getDiscountValue(),
                    "discountAmount", discountAmount,
                    "minOrderAmount", promo.getMinOrderAmount() != null ? promo.getMinOrderAmount() : 0,
                    "endDate", promo.getEndDate().toString()));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Lỗi kiểm tra mã: " + e.getMessage()));
        }
    }

    // ================================================================
    // ✅ CLAIM VOUCHER — Khách bấm "Nhận voucher"
    // POST /api/promotions/claim (được map qua /api/auth/... ở Security config)
    // Body: { "code": "BIRTHDAY250K" }
    // ================================================================

    // ================================================================
    // HELPER
    // ================================================================
    private Customer getCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return null;
        // ✅ FIX: customerRepository.findByEmail() trả về Customer entity trực tiếp
        return customerRepository.findByEmail(auth.getName()).orElse(null);
    }
}