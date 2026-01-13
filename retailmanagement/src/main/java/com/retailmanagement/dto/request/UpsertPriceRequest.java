package com.retailmanagement.dto.request;

import java.math.BigDecimal;

public class UpsertPriceRequest {
    private String currencyCode;   // nếu null -> dùng DEFAULT_CURRENCY
    private BigDecimal price;
    private BigDecimal costPrice;
    private String reason;         // MANUAL / PROMO ...

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
