package com.retailmanagement.service;

import com.retailmanagement.constants.OrderStatuses;
import com.retailmanagement.dto.request.CreateReturnRequest;
import com.retailmanagement.dto.response.ReturnResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerService customerService;
    private final StockTransactionRepository stockTransactionRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional
    public ReturnResponse createReturn(CreateReturnRequest req, Integer userId) {

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!OrderStatuses.DELIVERED.equals(order.getStatus())) {
            throw new RuntimeException("Order is not eligible for return");
        }

        OrderItem item = orderItemRepository.findById(req.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        // Tổng đã trả trước đó
        int returnedQty = returnRepository
                .findByOrderIdOrderByCreatedAtDesc(order.getId())
                .stream()
                .filter(r -> r.getOrderItem().getId().equals(item.getId()))
                .mapToInt(Return::getQuantity)
                .sum();

        if (returnedQty + req.getQuantity() > item.getQuantity()) {
            throw new RuntimeException("Return quantity exceeds purchased quantity");
        }

        Return ret = Return.builder()
                .order(order)
                .orderItem(item)
                .quantity(req.getQuantity())
                .reason(req.getReason())
                .refundAmount(req.getRefundAmount())
                .refundStatus("PENDING")
                .status("PENDING")
                .processedBy(userId)
                .createdAt(Instant.now())
                .build();

        returnRepository.save(ret);

        // ✅ update order status
        order.setStatus(OrderStatuses.RETURN_REQUESTED);
        orderRepository.save(order);

        return mapToResponse(ret);
    }

    @Transactional
    public ReturnResponse processReturn(Long returnId, String note, Integer staffId) {

        Return ret = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return not found"));

        ret.setStatus("APPROVED");
        ret.setRefundStatus("REFUNDED");
        ret.setRefundedAt(Instant.now());
        ret.setNote(note);
        ret.setProcessedBy(staffId);

        returnRepository.save(ret);

        updateOrderStatusAfterReturn(ret.getOrder());

        return mapToResponse(ret);
    }



    @Transactional
    public void approveReturn(Long returnId, Integer userId) {
        Return returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return not found"));

        if (!"PENDING".equals(returnEntity.getStatus())) {
            throw new IllegalStateException("Only pending returns can be approved");
        }

        Order order = returnEntity.getOrder();

        // 1. Update return
        returnEntity.setStatus("APPROVED");
        returnEntity.setRefundStatus("REFUNDED");
        returnEntity.setRefundedAt(Instant.now());
        returnEntity.setProcessedBy(userId);

        returnRepository.save(returnEntity);

        // 2. Restore stock
        restoreStockForReturn(returnEntity);

        // 3. Deduct loyalty
        if ("PAID".equals(order.getPaymentStatus()) && order.getCustomer() != null) {
            customerService.deductLoyaltyPoints(
                    order.getCustomer().getId(),
                    returnEntity.getRefundAmount(),
                    "RETURN",
                    "returns",
                    returnId
            );
        }

        // 4. ❗ UPDATE ORDER STATUS
        updateOrderStatusAfterReturn(order);
    }


    @Transactional
    public void rejectReturn(Long returnId, String reason, Integer userId) {
        Return returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return not found"));

        if (!"PENDING".equals(returnEntity.getStatus())) {
            throw new IllegalStateException("Only pending returns can be rejected");
        }

        Order order = returnEntity.getOrder();

        // 1. Update return
        returnEntity.setStatus("REJECTED");
        returnEntity.setNote(reason);
        returnEntity.setProcessedBy(userId);

        returnRepository.save(returnEntity);

        // 2. ❗ ROLLBACK ORDER STATUS
        order.setStatus(OrderStatuses.DELIVERED);
        orderRepository.save(order);
    }


    @Transactional
    public void cancelReturn(Long returnId, Integer userId) {
        Return returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return not found"));

        if (!"PENDING".equals(returnEntity.getStatus())) {
            throw new IllegalStateException("Only pending returns can be cancelled");
        }

        returnRepository.delete(returnEntity);
    }

    public List<ReturnResponse> getAllReturns() {
        return returnRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReturnResponse> getReturnsByStatus(String status) {
        return returnRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReturnResponse> getReturnsByOrder(Long orderId) {
        return returnRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReturnResponse> getReturnsByCustomer(Integer customerId) {
        return returnRepository.findByOrder_Customer_IdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReturnResponse getReturnDetail(Long returnId) {
        Return returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return not found"));
        return mapToResponse(returnEntity);
    }

    private void restoreStockForReturn(Return returnEntity) {
        OrderItem item = returnEntity.getOrderItem();

        StockTransaction tx = new StockTransaction();
        tx.setVariant(item.getVariant());
        tx.setQuantity(returnEntity.getQuantity());
        tx.setType("IN");
        tx.setNote("RETURN_ID_" + returnEntity.getId());
        tx.setReferenceType("returns");
        tx.setReferenceId(returnEntity.getId());
        tx.setCreatedAt(Instant.now());

        stockTransactionRepository.save(tx);

        // Cập nhật stock quantity
        ProductVariant variant = item.getVariant();
        variant.setStockQuantity(variant.getStockQuantity() + returnEntity.getQuantity());
        variantRepository.save(variant);
    }

    private void updateOrderStatusAfterReturn(Order order) {

        int totalOrdered = order.getOrderItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        int totalReturned = returnRepository
                .findByOrderIdOrderByCreatedAtDesc(order.getId())
                .stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .mapToInt(Return::getQuantity)
                .sum();

        if (totalReturned == totalOrdered) {
            order.setStatus(OrderStatuses.RETURNED);
        } else {
            order.setStatus(OrderStatuses.PARTIALLY_RETURNED);
        }

        order.setReturnedAt(Instant.now());
        orderRepository.save(order);
    }


    private ReturnResponse mapToResponse(Return r) {
        return ReturnResponse.builder()
                .id(r.getId())
                .orderId(r.getOrder().getId())
                .orderNumber(r.getOrder().getOrderNumber())
                .orderItemId(r.getOrderItem().getId())
                .productName(r.getOrderItem().getProductName())
                .variantName(r.getOrderItem().getVariantName())
                .quantity(r.getQuantity())
                .reason(r.getReason())
                .refundAmount(r.getRefundAmount())
                .refundMethod(r.getRefundMethod())
                .refundStatus(r.getRefundStatus())
                .status(r.getStatus())
                .note(r.getNote())
                .customerId(r.getOrder().getCustomer() != null ? r.getOrder().getCustomer().getId() : null)
                .customerName(r.getOrder().getCustomer() != null ? r.getOrder().getCustomer().getName() : null)
                .processedBy(r.getProcessedBy()) // ✅ Trả về Integer
                .createdAt(r.getCreatedAt())
                .refundedAt(r.getRefundedAt())
                .build();
    }
}