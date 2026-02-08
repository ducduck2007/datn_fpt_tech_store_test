package com.retailmanagement.constants;

import com.retailmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduler tự động gửi thông báo sinh nhật mỗi ngày
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayScheduler {

    private final NotificationService notificationService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Chạy mỗi ngày lúc 6:00 sáng để gửi thông báo sinh nhật
     * Cron format: giây phút giờ ngày tháng thứ
     * 0 0 6 * * * = 6:00 AM mỗi ngày
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendDailyBirthdayNotifications() {
        String currentTime = LocalDateTime.now().format(formatter);
        log.info("═══════════════════════════════════════════════════");
        log.info("🎂 BẮT ĐẦU KIỂM TRA SINH NHẬT - {}", currentTime);
        log.info("═══════════════════════════════════════════════════");

        try {
            notificationService.sendBirthdayNotifications();
            log.info("✅ HOÀN THÀNH gửi thông báo sinh nhật - {}",
                    LocalDateTime.now().format(formatter));
        } catch (Exception e) {
            log.error("❌ LỖI khi gửi thông báo sinh nhật: {}", e.getMessage(), e);
        }

        log.info("═══════════════════════════════════════════════════\n");
    }



    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendBirthdayNotificationsEvery5Minutes() {
        String currentTime = LocalDateTime.now().format(formatter);
        log.info("🧪 [TEST MODE] Gửi thông báo sinh nhật - {}", currentTime);

        try {
            notificationService.sendBirthdayNotifications();
            log.info("✅ [TEST MODE] Hoàn thành - {}",
                    LocalDateTime.now().format(formatter));
        } catch (Exception e) {
            log.error("❌ [TEST MODE] Lỗi: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ KIỂM TRA HỆ THỐNG - Chạy mỗi 30 giây để đảm bảo scheduler hoạt động
     * Comment lại khi đã xác nhận scheduler hoạt động
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void healthCheck() {
        log.info("💓 Scheduler đang hoạt động - {}",
                LocalDateTime.now().format(formatter));
    }
}