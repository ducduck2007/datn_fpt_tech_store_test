package com.retailmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderDetailResponse {

    private Long orderId;
    private String orderNumber;

    private String channel;           // ✅ thêm
    private String paymentMethod;     // ✅ thêm

    private String status;
    private String paymentStatus;

    private Integer customerId;
    private String customerName;

    private Integer staffId;
    private String staffUsername;

    private String notes;

    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;



    private Instant createdAt;
    private List<CreateOrderResponse.Item> items;
}


