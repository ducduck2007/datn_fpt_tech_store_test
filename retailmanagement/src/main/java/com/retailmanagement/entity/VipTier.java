package com.retailmanagement.entity;

import lombok.Getter;

@Getter
public enum VipTier {
    // Nâng mức điểm lên cao hơn
    BRONZE("BRONZE", 1000   , 1999, 0.03),       // Từ 500 điểm mới lên Bronze
    SILVER("SILVER", 2000, 4999, 0.05),      // 2000 điểm lên Silver
    GOLD("GOLD", 5000, 9999, 0.07),          // 5000 điểm lên Gold
    PLATINUM("PLATINUM", 10000, 19999, 0.10), // 10000 điểm lên Platinum
    DIAMOND("DIAMOND", 20000, Integer.MAX_VALUE, 0.15); // 20000 điểm mới đạt Diamond

    private final String displayName;
    private final Integer minPoints;
    private final Integer maxPoints;
    private final Double discountRate;

    VipTier(String displayName, Integer minPoints, Integer maxPoints, Double discountRate) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.discountRate = discountRate;
    }

    public static VipTier fromPoints(Integer points) {
        // Cần cập nhật lại điều kiện check ở đây tương ứng với BRONZE minPoints
        if (points == null || points < 500) return null;

        for (VipTier tier : values()) {
            if (points >= tier.minPoints && points <= tier.maxPoints) {
                return tier;
            }
        }
        return DIAMOND;
    }

    public Integer getNextTierThreshold() {
        VipTier[] tiers = values();
        int currentIndex = this.ordinal();
        if (currentIndex < tiers.length - 1) {
            return tiers[currentIndex + 1].minPoints;
        }
        return null;
    }
}