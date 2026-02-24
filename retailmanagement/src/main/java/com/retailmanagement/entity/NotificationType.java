package com.retailmanagement.entity;

import lombok.Getter;

@Getter
public enum NotificationType {
    PURCHASE_REMINDER("Nhắc mua hàng", "🛒"),
    WINBACK("Khách hàng cũ", "💝"),
    BIRTHDAY("Sinh nhật", "🎂"),
    ORDER_STATUS("Trạng thái đơn hàng", "📦"),
    PROMOTION("Khuyến mãi", "🎁"),
    LOYALTY_POINTS("Điểm tích lũy", "⭐"),
    VIP_TIER_UPGRADE("Nâng hạng VIP", "👑"),
    SYSTEM("Hệ thống", "🔔");

    private final String displayName;
    private final String icon;

    NotificationType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
}