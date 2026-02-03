package com.retailmanagement.controller;

import com.retailmanagement.dto.request.CreateOrderRequest;
import com.retailmanagement.dto.request.UpdateOrderRequest;
import com.retailmanagement.dto.response.CreateOrderResponse;
import com.retailmanagement.dto.response.OrderDetailResponse;
import com.retailmanagement.dto.response.OrderListResponse;
import com.retailmanagement.security.CustomUserDetails;
import com.retailmanagement.service.OrderQueryService;
import com.retailmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;

    // =========================================================
    // CREATE ORDER
    // =========================================================
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        CreateOrderResponse response =
                orderService.createOrder(request, user.getUserId());

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // LIST ORDERS (ADMIN / STAFF)
    // =========================================================

    // NEW = PENDING
    @GetMapping("/new")
    public ResponseEntity<List<OrderListResponse>> getNewOrders() {
        return ResponseEntity.ok(orderQueryService.getNewOrders());
    }

    // PROCESSING = SHIPPING
    @GetMapping("/processing")
    public ResponseEntity<List<OrderListResponse>> getProcessingOrders() {
        return ResponseEntity.ok(orderQueryService.getProcessingOrders());
    }

    @GetMapping("/paid")
    public ResponseEntity<List<OrderListResponse>> getPaidOrders() {
        return ResponseEntity.ok(orderQueryService.getPaidOrders());
    }

    @GetMapping("/delivered")
    public ResponseEntity<List<OrderListResponse>> getDeliveredOrders() {
        return ResponseEntity.ok(orderQueryService.getDeliveredOrders());
    }

    // =========================================================
    // LIST ORDERS BY CUSTOMER
    // =========================================================
    @GetMapping("/my")
    public ResponseEntity<List<OrderListResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                orderQueryService.getOrdersByCustomer(user.getCustomerId())
        );
    }


    // =========================================================
    // ORDER DETAIL
    // =========================================================
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrderDetail(orderId)
        );
    }

    // =========================================================
    // UPDATE ORDER (GENERAL)
    // =========================================================
    @PutMapping("/{orderId}")
    public ResponseEntity<Void> updateOrder(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderRequest request) {

        orderService.updateOrder(orderId, request);
        return ResponseEntity.ok().build();
    }

    // =========================================================
    // ORDER STATUS ACTIONS
    // =========================================================

    // PENDING -> SHIPPING
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<Void> markAsShipping(@PathVariable Long orderId) {
        orderService.markAsShipping(orderId);
        return ResponseEntity.ok().build();
    }

    // SHIPPING -> DELIVERED
    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<Void> markAsDelivered(@PathVariable Long orderId) {
        orderService.markAsDelivered(orderId);
        return ResponseEntity.ok().build();
    }

    // CANCEL ORDER
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {

        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok().build();
    }

    // =========================================================
    // DELETE ORDER (ADMIN ONLY – nếu bạn cho phép)
    // =========================================================
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
