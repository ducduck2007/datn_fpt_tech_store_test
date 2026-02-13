package com.retailmanagement.constants;

import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.CustomerType;
import com.retailmanagement.entity.VipTier;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TierUpgradeNotificationScheduler {

    private final CustomRes customerRepository;
    private final NotificationService notificationService;

    /**
     * Chạy mỗi ngày lúc 9h sáng để kiểm tra và gửi thông báo lên hạng
     * Cron: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkAndSendTierUpgradeNotifications() {
        System.out.println("🔔 Bắt đầu kiểm tra và gửi thông báo lên hạng...");

        List<Customer> activeCustomers = customerRepository.findAll().stream()
                .filter(Customer::getIsActive)
                .toList();

        int sentCount = 0;
        int vipAlertCount = 0;

        for (Customer customer : activeCustomers) {
            try {
                int currentPoints = customer.getLoyaltyPoints() != null
                        ? customer.getLoyaltyPoints() : 0;
                VipTier currentTier = customer.getVipTier();
                CustomerType currentType = customer.getCustomerType();

                // 1. Kiểm tra lên hạng VIP Tier
                VipTier nextTier = getNextTier(currentTier);
                if (nextTier != null) {
                    int pointsGap = nextTier.getMinPoints() - currentPoints;

                    // Tính % tiến trình
                    int tierRange = currentTier != null
                            ? nextTier.getMinPoints() - currentTier.getMinPoints()
                            : nextTier.getMinPoints();

                    double progress = tierRange > 0
                            ? (double)(tierRange - pointsGap) / tierRange * 100
                            : 0;

                    // Gửi thông báo nếu đã đạt >= 80% (trong 20% cuối)
                    if (progress >= 80 && pointsGap > 0) {
                        notificationService.createTierUpgradeNotification(
                                customer.getId(),
                                nextTier.getDisplayName(),
                                pointsGap
                        );
                        sentCount++;
                        System.out.println("✅ Gửi thông báo lên hạng cho: " + customer.getName());
                    }
                }

                // 2. Kiểm tra lên VIP (từ REGULAR)
                if (currentType == CustomerType.REGULAR) {
                    int goldMinPoints = VipTier.GOLD.getMinPoints(); // 5000

                    // Gửi cảnh báo nếu đã đạt >= 70% điểm cần thiết
                    if (currentPoints >= goldMinPoints * 0.7 && currentPoints < goldMinPoints) {
                        notificationService.createUpgradeToVipNotification(
                                customer.getId(),
                                goldMinPoints - currentPoints
                        );
                        vipAlertCount++;
                        System.out.println("👑 Gửi cảnh báo VIP cho: " + customer.getName());
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Lỗi khi xử lý customer " + customer.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("✅ Hoàn thành! Đã gửi " + sentCount + " thông báo lên hạng và "
                + vipAlertCount + " cảnh báo VIP");
    }

    /**
     * Chạy mỗi 4 giờ để gửi thông báo cho khách đang online
     */
    @Scheduled(fixedRate = 14400000) // 4 giờ = 4 * 60 * 60 * 1000 ms
    public void sendReminderForActiveCustomers() {
        System.out.println("🔔 Gửi thông báo nhắc nhở cho khách hàng đang hoạt động...");

        // Logic tương tự nhưng chỉ cho khách hàng có last_login_at trong 24h
        // Tránh spam bằng cách kiểm tra đã gửi trong 7 ngày chưa
    }

    /**
     * Lấy hạng tiếp theo
     */
    private VipTier getNextTier(VipTier currentTier) {
        if (currentTier == null) {
            return VipTier.BRONZE;
        }

        VipTier[] tiers = VipTier.values();
        int currentIndex = currentTier.ordinal();

        if (currentIndex < tiers.length - 1) {
            return tiers[currentIndex + 1];
        }

        return null; // Đã ở hạng cao nhất
    }
}