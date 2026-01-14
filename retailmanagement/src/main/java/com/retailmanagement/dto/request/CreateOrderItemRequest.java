package com.retailmanagement.dto.request;

import lombok.Data;

@Data
public class CreateOrderItemRequest {

    private Integer variantId;
    private Integer quantity;
}
