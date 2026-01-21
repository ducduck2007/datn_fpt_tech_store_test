package com.retailmanagement.entity;

import lombok.Getter;

@Getter
public enum VipTier {
    // Sửa discountRate thành dạng thập phân: 0.05 = 5%
    BRONZE("BRONZE", 100, 499, 0.05),      
    SILVER("SILVER", 500, 999, 0.10),       
    GOLD("GOLD", 1000, 2499, 0.15),      
    PLATINUM("PLATINUM", 2500, 4999, 0.20), 
    DIAMOND("DIAMOND", 5000, Integer.MAX_VALUE, 0.25); 

    private final String displayName;
    private final Integer minPoints;
    private final Integer maxPoints;
    private final Double discountRate; // Đổi tên cho rõ nghĩa

    VipTier(String displayName, Integer minPoints, Integer maxPoints, Double discountRate) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.discountRate = discountRate;
    }

    public static VipTier fromPoints(Integer points) {
        if (points == null || points < 100) return null; // < 100 điểm là khách thường
        for (VipTier tier : values()) {
            if (points >= tier.minPoints && points <= tier.maxPoints) {
                return tier;
            }
        }
        return DIAMOND;
    }

    // Lấy mốc điểm của hạng tiếp theo
    public Integer getNextTierThreshold() {
        VipTier[] tiers = values();
        int currentIndex = this.ordinal();
        if (currentIndex < tiers.length - 1) {
            return tiers[currentIndex + 1].minPoints;
        }
        return null; // Đã max cấp
    }
}