package com.retailmanagement.service;

import com.retailmanagement.constants.OrderStatuses;
import com.retailmanagement.dto.response.OrderListResponse;
import com.retailmanagement.dto.response.RevenueByCustomerResponse;
import com.retailmanagement.entity.Order;
import com.retailmanagement.entity.Return;
import com.retailmanagement.repository.OrderRepository;
import com.retailmanagement.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final ModelMapper modelMapper;

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

    public List<OrderListResponse> getShippingOrders() {
        return orderRepository.findByStatusOrderByCreatedAtDesc(
                        OrderStatuses.SHIPPING
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

        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getName()); // nếu có

        dto.setChannel(order.getChannel());               // ✅ THÊM
        dto.setPaymentMethod(order.getPaymentMethod());   // ✅ THÊM

        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());   // nếu cần
        dto.setCreatedAt(order.getCreatedAt());

        return dto;
    }

    public BigDecimal getNetRevenue() {

        BigDecimal totalOrders = orderRepository.findAll()
                .stream()
                .filter(o -> !OrderStatuses.CANCELLED.equals(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal refunded = returnRepository.findAll()
                .stream()
                .filter(r -> "REFUNDED".equals(r.getRefundStatus()))
                .map(Return::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalOrders.subtract(refunded);
    }

    public List<OrderListResponse> getOrdersByDate(Instant from, Instant to) {

        List<Order> orders = orderRepository.findOrdersByDateRange(from, to);

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderListResponse.class))
                .toList();
    }

    public List<OrderListResponse> filterOrders(
            Integer customerId,
            Instant from,
            Instant to
    ) {

        List<Order> orders =
                orderRepository.findOrdersByCustomerAndDate(customerId, from, to);

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderListResponse.class))
                .toList();
    }

    public List<RevenueByCustomerResponse> getRevenueByCustomer() {
        return orderRepository.getRevenueByCustomer();
    }

}

