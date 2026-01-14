package com.retailmanagement.controller;

import com.retailmanagement.dto.request.CreateOrderRequest;
import com.retailmanagement.dto.response.CreateOrderResponse;
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
}

