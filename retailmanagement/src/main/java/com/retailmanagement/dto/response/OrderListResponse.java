package com.retailmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;


@Data
@AllArgsConstructor
public class OrderListResponse {

    private Long orderId;
    private String orderNumber;

    private Integer customerId;      // ✅ thêm
    private String customerName;

    private String staffName;

    private String channel;           // ✅ thêm
    private String paymentMethod;     // ✅ thêm

    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private Instant createdAt;

    public OrderListResponse() {

    }
}


