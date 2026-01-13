package com.techstore.Entity;


import lombok.Getter;

@Getter
public enum VipTier {
    BRONZE("BRONZE", 100, 499, 1.05),      // 100-499 điểm, giảm 5%
    SILVER("SILVER", 500, 999, 1.10),       // 500-999 điểm, giảm 10%
    GOLD("GOLD", 1000, 2499, 1.15),      // 1000-2499 điểm, giảm 15%
    PLATINUM("PLATINUM", 2500, 4999, 1.20), // 2500-4999 điểm, giảm 20%
    DIAMOND("DIAMOND", 5000, Integer.MAX_VALUE, 1.25); // 5000+ điểm, giảm 25%

    private final String displayName;
    private final Integer minPoints;
    private final Integer maxPoints;
    private final Double discountMultiplier;

    VipTier(String displayName, Integer minPoints, Integer maxPoints, Double discountMultiplier) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.discountMultiplier = discountMultiplier;
    }

    // Tự động xác định tier dựa trên điểm
    public static VipTier fromPoints(Integer points) {
        if (points == null || points < 100) {
            return null; // Khách thường
        }

        for (VipTier tier : values()) {
            if (points >= tier.minPoints && points <= tier.maxPoints) {
                return tier;
            }
        }

        return DIAMOND; // Mặc định cao nhất
    }

    // Kiểm tra có thể nâng hạng không
    public VipTier getNextTier() {
        VipTier[] tiers = values();
        int currentIndex = this.ordinal();
        if (currentIndex < tiers.length - 1) {
            return tiers[currentIndex + 1];
        }
        return null; // Đã max tier
    }

    // Điểm cần để lên hạng tiếp theo
    public Integer getPointsToNextTier() {
        VipTier next = getNextTier();
        return next != null ? next.minPoints : null;
    }
}