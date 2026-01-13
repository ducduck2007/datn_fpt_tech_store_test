package com.retailmanagement.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PromotionRequest {
    private String code;
    private String name;
    private String description;

    private String discountType;   // PERCENT / AMOUNT
    private BigDecimal discountValue;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // apply:
    private String scope;          // ALL / PRODUCT / VARIANT
    private List<Integer> productIds;
    private List<Integer> variantIds;

    private Integer priority = 0;
    private Boolean stackable = false;
    private Boolean isActive = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public List<Integer> getProductIds() { return productIds; }
    public void setProductIds(List<Integer> productIds) { this.productIds = productIds; }
    public List<Integer> getVariantIds() { return variantIds; }
    public void setVariantIds(List<Integer> variantIds) { this.variantIds = variantIds; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Boolean getStackable() { return stackable; }
    public void setStackable(Boolean stackable) { this.stackable = stackable; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
}
