package com.retailmanagement.controller;

import com.retailmanagement.dto.request.PaymentRequest;
import com.retailmanagement.dto.response.PaymentResponse;
import com.retailmanagement.security.CustomUserDetails;
import com.retailmanagement.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo payment mới
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        PaymentResponse response = paymentService.createPayment(request, user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách payments theo order
     * GET /api/payments/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }

    /**
     * Lấy thông tin cơ bản 1 payment
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    /**
     * Lấy tất cả payments (cơ bản)
     * GET /api/payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * THÊM MỚI: Lấy chi tiết payment đầy đủ (kèm items)
     * GET /api/payments/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<PaymentResponse> getPaymentDetail(
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentService.getPaymentDetail(id));
    }

    /**
     * Hoàn tiền
     * PUT /api/payments/{id}/refund
     */
    @PutMapping("/{id}/refund")
    public ResponseEntity<Void> refundPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        paymentService.refundPayment(id, user.getUserId());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}/soft-delete")
    public ResponseEntity<String> softDeletePayment(@PathVariable Long id) {
        // TODO: Get current user ID from security context
        Integer userId = 1; // Placeholder

        String message = paymentService.softDeletePayment(id, userId);
        return ResponseEntity.ok(message);
    }

    /**
     * Restore a soft-deleted payment
     * POST /api/auth/payments/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<String> restorePayment(@PathVariable Long id) {
        String message = paymentService.restorePayment(id);
        return ResponseEntity.ok(message);
    }
}