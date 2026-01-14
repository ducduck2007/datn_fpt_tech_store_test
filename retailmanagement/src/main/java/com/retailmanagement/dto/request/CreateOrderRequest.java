package com.retailmanagement.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    private Integer customerId;
    private String paymentMethod; // CASH / BANK / COD
    private String channel;        // ONLINE / OFFLINE
    private String notes;

    private List<CreateOrderItemRequest> items;
}

