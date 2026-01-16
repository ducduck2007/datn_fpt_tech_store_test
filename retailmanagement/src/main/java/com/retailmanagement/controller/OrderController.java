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

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                orderService.createOrder(request, user.getUserId())
        );
    }




    @GetMapping("/new")
    public List<OrderListResponse> getNewOrders() {
        return orderQueryService.getNewOrders();
    }

    @GetMapping("/processing")
    public List<OrderListResponse> getProcessingOrders() {
        return orderQueryService.getProcessingOrders();
    }

    /* UPDATE */
    @PutMapping("/{orderId}")
    public ResponseEntity<Void> updateOrder(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderRequest request) {

        orderService.updateOrder(orderId, request);
        return ResponseEntity.ok().build();
    }

    /* CANCEL */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails user) {

        orderService.cancelOrder(orderId, user.getUserId());
        return ResponseEntity.ok().build();
    }

    /* DELETE */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {

        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /* DETAIL */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrderDetail(orderId)
        );
    }
}

