package com.retailmanagement.service;

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
    public ReturnResponse createReturn(CreateReturnRequest request, Integer userId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        if (!order.getId().equals(orderItem.getOrder().getId())) {
            throw new RuntimeException("Order item does not belong to this order");
        }

        if (request.getQuantity() > orderItem.getQuantity()) {
            throw new RuntimeException("Return quantity exceeds order quantity");
        }

        Return returnEntity = new Return();
        returnEntity.setOrder(order);
        returnEntity.setOrderItem(orderItem);
        returnEntity.setQuantity(request.getQuantity());
        returnEntity.setReason(request.getReason());
        returnEntity.setRefundAmount(request.getRefundAmount() != null
                ? request.getRefundAmount()
                : orderItem.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        returnEntity.setRefundMethod(order.getPaymentMethod());
        returnEntity.setRefundStatus("PENDING");
        returnEntity.setStatus("PENDING");
        returnEntity.setCreatedAt(Instant.now());

        Return saved = returnRepository.save(returnEntity);
        return mapToResponse(saved);
    }

    @Transactional
    public void approveReturn(Long returnId, Integer userId) {
        Return returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return not found"));

        if (!"PENDING".equals(returnEntity.getStatus())) {
            throw new IllegalStateException("Only pending returns can be approved");
        }

        Order order = returnEntity.getOrder();

        // 1. Cập nhật trạng thái
        returnEntity.setStatus("APPROVED");
        returnEntity.setRefundStatus("REFUNDED");
        returnEntity.setRefundedAt(Instant.now());
        returnEntity.setProcessedBy(userId);

        // 2. Hoàn kho
        restoreStockForReturn(returnEntity);

        // 3. TRỪ ĐIỂM (nếu đơn đã thanh toán)
        if ("PAID".equals(order.getPaymentStatus()) && order.getCustomer() != null) {
            customerService.deductLoyaltyPoints(
                    order.getCustomer().getId(),
                    returnEntity.getRefundAmount(),
                    "RETURN",
                    "returns",
                    returnId
            );
        }

        returnRepository.save(returnEntity);
    }

    @Transactional
    public void rejectReturn(Long returnId, String reason, Integer userId) {
        Return returnEntity = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return not found"));

        if (!"PENDING".equals(returnEntity.getStatus())) {
            throw new IllegalStateException("Only pending returns can be rejected");
        }

        returnEntity.setStatus("REJECTED");
        returnEntity.setNote(reason);
        returnEntity.setProcessedBy(userId); // ✅ CHỈ TRUYỀN userId (Integer)

        returnRepository.save(returnEntity);
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