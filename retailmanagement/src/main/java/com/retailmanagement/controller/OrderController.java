package com.retailmanagement.controller;

import com.retailmanagement.dto.request.CreateOrderRequest;
import com.retailmanagement.dto.request.UpdateOrderRequest;
import com.retailmanagement.dto.response.CreateOrderResponse;
import com.retailmanagement.dto.response.OrderDetailResponse;
import com.retailmanagement.dto.response.OrderListResponse;
import com.retailmanagement.dto.response.RevenueByCustomerResponse;
import com.retailmanagement.security.service.CustomUserDetails;
import com.retailmanagement.service.OrderQueryService;
import com.retailmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/shipping")
    public ResponseEntity<List<OrderListResponse>> getShippingOrders() {
        return ResponseEntity.ok(orderQueryService.getShippingOrders());
    }

    // =========================================================
    // LIST ORDERS BY CUSTOMER
    // =========================================================
    @GetMapping("/my-orders")
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
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                orderService.getOrderDetail(orderId, user)
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
    // PAID -> PROCESSING
    @PutMapping("/{orderId}/process")
    public ResponseEntity<Void> markAsProcessing(@PathVariable Long orderId) {
        orderService.markAsProcessing(orderId);
        return ResponseEntity.ok().build();
    }

    // PROCESSING -> SHIPPING
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
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body) {

        String reason = (body != null && body.containsKey("reason"))
                ? body.get("reason")
                : "Customer cancelled";

        orderService.cancelOrder(orderId, reason);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đơn hàng đã được hủy thành công",
                "orderId", orderId
        ));
    }

    // =========================================================
    // DELETE ORDER (ADMIN ONLY – nếu bạn cho phép)
    // =========================================================
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<OrderListResponse>> getOrdersByDate(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return ResponseEntity.ok(
                orderQueryService.getOrdersByDate(from, to)
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<List<OrderListResponse>> filterOrders(
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                orderQueryService.filterOrders(customerId, from, to, channel, status)
        );
    }

    @GetMapping("/revenue-by-customer")
    public ResponseEntity<List<RevenueByCustomerResponse>> revenueByCustomer() {
        return ResponseEntity.ok(
                orderQueryService.getRevenueByCustomer()
        );
    }

    // =========================================================
// LIST ORDERS BY STAFF
// =========================================================
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<OrderListResponse>> getOrdersByStaff(
            @PathVariable Integer staffId
    ) {
        return ResponseEntity.ok(
                orderQueryService.getOrdersByStaff(staffId)
        );
    }

    @GetMapping("/{orderId}/pdf")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long orderId
    ) {

        byte[] pdf = orderQueryService.generateOrderPdf(orderId);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=invoice_" + orderId + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }


}
