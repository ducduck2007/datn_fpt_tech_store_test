package com.retailmanagement.service;

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
        return mapToResponse(
                orderRepository.findByStatusOrderByCreatedAtDesc("PENDING")
        );
    }

    public List<OrderListResponse> getProcessingOrders() {
        return mapToResponse(
                orderRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of("PAID", "SHIPPING")
                )
        );
    }

    private List<OrderListResponse> mapToResponse(List<Order> orders) {
        return orders.stream()
                .map(o -> new OrderListResponse(
                                o.getId(),
                                o.getOrderNumber(),

                                o.getCustomer().getId(),          // customerId
                                o.getCustomer().getName(),

                                o.getUser().getUsername(),

                                o.getChannel(),                   // channel
                                o.getPaymentMethod(),             // paymentMethod

                                o.getTotalAmount(),
                                o.getStatus(),
                                o.getPaymentStatus(),
                                o.getCreatedAt()
                        )
                )
                .toList();
    }
}

