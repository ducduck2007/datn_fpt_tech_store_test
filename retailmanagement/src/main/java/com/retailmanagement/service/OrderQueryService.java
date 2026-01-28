package com.retailmanagement.service;

import com.retailmanagement.constants.OrderStatuses;
import com.retailmanagement.dto.response.OrderListResponse;
import com.retailmanagement.entity.Order;
import com.retailmanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public List<OrderListResponse> getNewOrders() {
        return orderRepository.findByStatusOrderByCreatedAtDesc(
                        OrderStatuses.PENDING
                ).stream()
                .map(this::toOrderListResponse)
                .toList();
    }


    public List<OrderListResponse> getProcessingOrders() {
        return orderRepository.findByStatusOrderByCreatedAtDesc(
                        OrderStatuses.SHIPPING
                ).stream()
                .map(this::toOrderListResponse)
                .toList();
    }

    public List<OrderListResponse> getPaidOrders() {
        return orderRepository.findByStatusOrderByCreatedAtDesc(
                        OrderStatuses.PAID
                ).stream()
                .map(this::toOrderListResponse)
                .toList();
    }

    public List<OrderListResponse> getDeliveredOrders() {
        return orderRepository.findByStatusOrderByCreatedAtDesc(
                        OrderStatuses.DELIVERED
                ).stream()
                .map(this::toOrderListResponse)
                .toList();
    }

    public List<OrderListResponse> getOrdersByCustomer(Integer customerId) {
        return orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toOrderListResponse)
                .toList();
    }


    private OrderListResponse toOrderListResponse(Order order) {
        OrderListResponse dto = new OrderListResponse();
        dto.setOrderId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

}

