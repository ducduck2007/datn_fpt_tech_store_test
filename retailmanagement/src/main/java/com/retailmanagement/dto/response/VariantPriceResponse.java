package com.retailmanagement.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VariantPriceResponse {

    private Integer variantId;
    private Integer productId;
    private String variantName;
    private String sku;

    private String currencyCode;

    // Giá gốc (trước KM, sau VIP tier nếu có)
    private BigDecimal price;
    private BigDecimal costPrice;

    // Thông tin khuyến mãi áp dụng
    private String promotionCode;
    private String promotionName;

    // Giá cuối cùng (sau khuyến mãi)
    private BigDecimal finalPrice;

    // Số tiền được giảm = price - finalPrice
    private BigDecimal discountAmount;

    // Thông tin combo (nếu có), e.g. "Mua 2 tặng 1"
    private String comboInfo;

    // ================================================================
    // Helper: tự tính discountAmount khi set finalPrice
    // ================================================================
    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
        if (this.price != null && finalPrice != null) {
            BigDecimal diff = this.price.subtract(finalPrice);
            this.discountAmount = diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }
    }
}