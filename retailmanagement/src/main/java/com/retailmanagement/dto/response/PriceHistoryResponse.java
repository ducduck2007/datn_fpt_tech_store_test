package com.retailmanagement.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PriceHistoryResponse {
    private Long id;
    private Integer variantId;
    private String currencyCode;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String reason;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private String createdByUsername;
    private Instant createdAt;
}