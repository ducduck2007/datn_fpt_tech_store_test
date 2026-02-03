package com.retailmanagement.service;


import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.Notification;
import com.retailmanagement.entity.NotificationType;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomRes customerRepository;

    /**
     * Tạo thông báo sinh nhật cho khách hàng
     */
    @Transactional
    public void createBirthdayNotification(Customer customer) {
        // Kiểm tra xem đã có thông báo sinh nhật cho khách hàng này trong năm nay chưa
        LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime yearEnd = LocalDate.now().withDayOfYear(1).plusYears(1).atStartOfDay();

        boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(),
                NotificationType.BIRTHDAY,
                yearStart,
                yearEnd
        );

        if (!exists) {
            Notification notification = Notification.builder()
                    .customer(customer)
                    .type(NotificationType.BIRTHDAY)
                    .title("🎂 Chúc mừng sinh nhật!")
                    .message(String.format(
                            "Chúc mừng sinh nhật %s! 🎉 Chúc bạn một ngày thật vui vẻ và hạnh phúc. " +
                                    "Đừng quên check voucher sinh nhật đặc biệt của bạn nhé!",
                            customer.getName()
                    ))
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Tự động gửi thông báo sinh nhật cho tất cả khách hàng có sinh nhật hôm nay
     * Hàm này nên được gọi bởi Scheduled Task mỗi ngày
     */
    @Transactional
    public void sendBirthdayNotifications() {
        List<Customer> birthdayCustomers = customerRepository.findCustomersWithBirthdayToday();

        for (Customer customer : birthdayCustomers) {
            if (customer.getIsActive()) {
                createBirthdayNotification(customer);
            }
        }

        // Log số lượng thông báo đã tạo
        System.out.println("📨 Đã gửi " + birthdayCustomers.size() + " thông báo sinh nhật");
    }

    /**
     * Lấy danh sách thông báo chưa đọc của khách hàng
     */
    public List<Notification> getUnreadNotifications(Integer customerId) {
        return notificationRepository.findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);
    }

    /**
     * Lấy tất cả thông báo của khách hàng
     */
    public List<Notification> getAllNotifications(Integer customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Đánh dấu thông báo đã đọc
     */
    @Transactional
    public void markAsRead(Long notificationId, Integer customerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (!notification.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền đọc thông báo này");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }
    @Transactional
    public void createCustomBirthdayNotification(Integer customerId, String customMessage) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        if (!customer.getIsActive()) {
            throw new RuntimeException("Khách hàng không hoạt động");
        }

        Notification notification = Notification.builder()
                .customer(customer)
                .type(NotificationType.BIRTHDAY)
                .title("🎂 Lời chúc sinh nhật từ Admin")
                .message(customMessage)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @Transactional
    public void markAllAsRead(Integer customerId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        if (!unreadNotifications.isEmpty()) {
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    public long countUnread(Integer customerId) {
        return notificationRepository.countByCustomerIdAndIsReadFalse(customerId);
    }

    /**
     * Xóa thông báo (soft delete)
     */
    @Transactional
    public void deleteNotification(Long notificationId, Integer customerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (!notification.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền xóa thông báo này");
        }

        notificationRepository.delete(notification);
    }
}
