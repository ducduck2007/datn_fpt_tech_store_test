package com.retailmanagement.constants;

import com.retailmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduler tự động gửi thông báo sinh nhật mỗi ngày
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayScheduler {

    private final NotificationService notificationService;

    /**
     * Chạy mỗi ngày lúc 6:00 sáng để gửi thông báo sinh nhật
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void sendDailyBirthdayNotifications() {
        log.info("🎂 Bắt đầu kiểm tra và gửi thông báo sinh nhật - {}", LocalDateTime.now());

        try {
            notificationService.sendBirthdayNotifications();
            log.info("✅ Hoàn thành gửi thông báo sinh nhật");
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi thông báo sinh nhật: {}", e.getMessage(), e);
        }
    }

    /**
     * Test scheduler - chạy mỗi 10 phút (để test)
     * Uncomment để test, comment lại khi production
     */
    // @Scheduled(cron = "0 */10 * * * *")
    public void sendBirthdayNotificationsEvery10Minutes() {
        log.info("🧪 [TEST] Gửi thông báo sinh nhật - {}", LocalDateTime.now());
        try {
            notificationService.sendBirthdayNotifications();
        } catch (Exception e) {
            log.error("❌ [TEST] Lỗi: {}", e.getMessage());
        }
    }
}