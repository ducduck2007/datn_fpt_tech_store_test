package com.retailmanagement.service;


import com.retailmanagement.dto.response.CustomerBirthdayResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    // Method này phải có trong NotificationService
    public void sendCustomBirthdayGreeting(Integer customerId, String customMessage) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Notification notification = Notification.builder()
                .customer(customer)
                .type(NotificationType.BIRTHDAY)
                .title("🎉 Lời chúc sinh nhật từ Admin")
                .message(customMessage)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    public List<Notification> getBirthdayNotificationHistory() {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(NotificationType.BIRTHDAY);
    }

    public List<Notification> getBirthdayNotificationHistory(LocalDateTime from, LocalDateTime to) {
        return notificationRepository.findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                NotificationType.BIRTHDAY, from, to);
    }



    /**
     * Lấy khách hàng có sinh nhật trong tháng
     */
    public List<CustomerBirthdayResponse> getBirthdaysByMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        List<Customer> customers = customerRepository.findCustomersWithBirthdayInMonth(month);
        return customers.stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(CustomerBirthdayResponse::getBirthDay))
                .collect(Collectors.toList());
    }

    /**
     * Lấy sinh nhật sắp tới trong N ngày
     */
    public List<CustomerBirthdayResponse> getUpcomingBirthdays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        List<Customer> customers = customerRepository.findCustomersWithBirthdayBetween(
                today.getMonthValue(),
                today.getDayOfMonth(),
                endDate.getMonthValue(),
                endDate.getDayOfMonth()
        );

        return customers.stream()
                .map(this::mapToResponse)
                .filter(c -> c.getDaysUntilBirthday() != null && c.getDaysUntilBirthday() <= days)
                .sorted(Comparator.comparing(CustomerBirthdayResponse::getDaysUntilBirthday))
                .collect(Collectors.toList());
    }

    /**
     * Lấy thống kê sinh nhật theo tháng
     */
    public Map<String, Object> getStatistics() {
        Map<Integer, Long> monthlyCount = new HashMap<>();

        // Đếm số khách hàng có sinh nhật trong từng tháng
        for (int month = 1; month <= 12; month++) {
            long count = customerRepository.countCustomersWithBirthdayInMonth(month);
            monthlyCount.put(month, count);
        }

        // Tính tổng số khách hàng
        long totalCustomers = customerRepository.count();

        // Tính tổng số khách hàng có ngày sinh
        long customersWithBirthday = monthlyCount.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("monthlyCount", monthlyCount);
        stats.put("totalCustomers", totalCustomers);
        stats.put("customersWithBirthday", customersWithBirthday);
        stats.put("customersWithoutBirthday", totalCustomers - customersWithBirthday);

        return stats;
    }

    /**
     * Lấy thống kê chi tiết theo tháng
     */
    public Map<String, Object> getMonthlyStatistics(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        List<CustomerBirthdayResponse> birthdays = getBirthdaysByMonth(month);

        // Phân loại theo CustomerType
        Map<String, Long> byType = birthdays.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCustomerType().name(),
                        Collectors.counting()
                ));

        // Phân loại theo VipTier
        Map<String, Long> byTier = birthdays.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getVipTier() != null ? c.getVipTier().name() : "NONE",
                        Collectors.counting()
                ));

        Map<String, Object> stats = new HashMap<>();
        stats.put("month", month);
        stats.put("totalCount", birthdays.size());
        stats.put("byCustomerType", byType);
        stats.put("byVipTier", byTier);
        stats.put("birthdays", birthdays);

        return stats;
    }

    /**
     * Map Customer entity sang CustomerBirthdayResponse DTO
     */
    private CustomerBirthdayResponse mapToResponse(Customer customer) {
        LocalDate birthDate = customer.getDateOfBirth();
        if (birthDate == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        // Tính tuổi
        int age = Period.between(birthDate, today).getYears();

        // Tính sinh nhật tiếp theo
        LocalDate nextBirthday = LocalDate.of(today.getYear(), birthDate.getMonth(), birthDate.getDayOfMonth());
        if (nextBirthday.isBefore(today)) {
            nextBirthday = nextBirthday.plusYears(1);
            age++; // Tuổi sẽ đạt được vào sinh nhật tiếp theo
        }

        // Tính số ngày còn lại
        long daysUntil = ChronoUnit.DAYS.between(today, nextBirthday);
        boolean isToday = daysUntil == 0;

        return CustomerBirthdayResponse.builder()
                .id(Long.valueOf(customer.getId()))
                .customerId(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(birthDate)
                .birthdayDisplay(String.format("%02d/%02d/%d",
                        birthDate.getDayOfMonth(),
                        birthDate.getMonthValue(),
                        birthDate.getYear()))
                .birthMonth(birthDate.getMonthValue())
                .birthDay(birthDate.getDayOfMonth())
                .age(age)
                .daysUntilBirthday(daysUntil)
                .isBirthdayToday(isToday)
                .customerType(customer.getCustomerType())
                .vipTier(customer.getVipTier())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .totalSpent(customer.getTotalSpent())
                .build();
    }
    @Transactional
    public void createTierUpgradeNotification(Integer customerId, String tierName, int pointsGap) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Kiểm tra xem đã có thông báo tương tự trong 7 ngày chưa
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(),
                NotificationType.VIP_TIER_UPGRADE,
                weekAgo,
                LocalDateTime.now()
        );

        if (!exists) {
            String message;
            if (pointsGap <= 500) {
                message = String.format(
                        "🔥 Bạn chỉ còn thiếu %,d điểm nữa là lên hạng %s! " +
                                "Hoàn tất một đơn hàng nhỏ để nhận ưu đãi tốt hơn ngay!",
                        pointsGap, tierName
                );
            } else {
                message = String.format(
                        "⭐ Bạn sắp đạt hạng %s với %,d điểm nữa! " +
                                "Tiếp tục mua sắm để tận hưởng nhiều đặc quyền hơn.",
                        pointsGap, tierName
                );
            }

            Notification notification = Notification.builder()
                    .customer(customer)
                    .type(NotificationType.VIP_TIER_UPGRADE)
                    .title("🎯 Bạn sắp lên hạng!")
                    .message(message)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Tạo thông báo lên VIP
     */
    @Transactional
    public void createUpgradeToVipNotification(Integer customerId, int pointsNeeded) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Chỉ gửi nếu khách hàng chưa phải VIP
        if (customer.getCustomerType() == CustomerType.REGULAR) {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                    customer.getId(),
                    NotificationType.VIP_TIER_UPGRADE,
                    weekAgo,
                    LocalDateTime.now()
            );

            if (!exists) {
                String message = String.format(
                        "👑 Chỉ còn %,d điểm nữa, bạn sẽ trở thành khách hàng VIP! " +
                                "Khách VIP được giảm giá cao hơn, ưu đãi độc quyền và nhiều đặc quyền khác. " +
                                "Mua sắm ngay để nâng cấp!",
                        pointsNeeded
                );

                Notification notification = Notification.builder()
                        .customer(customer)
                        .type(NotificationType.VIP_TIER_UPGRADE)
                        .title("👑 Sắp trở thành VIP!")
                        .message(message)
                        .isRead(false)
                        .build();

                notificationRepository.save(notification);
            }
        }
    }

    /**
     * Tự động kiểm tra và gửi thông báo lên hạng
     * (Gọi định kỳ hoặc sau mỗi giao dịch)
     */
    @Transactional
    public void checkAndSendTierUpgradeNotifications() {
        // Tìm khách hàng sắp lên hạng (trong 20% cuối)
        List<Customer> customers = customerRepository.findAll().stream()
                .filter(Customer::getIsActive)
                .toList();

        for (Customer customer : customers) {
            int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
            VipTier currentTier = customer.getVipTier();

            // Kiểm tra lên hạng VIP tier
            VipTier nextTier = getNextTier(currentTier);
            if (nextTier != null) {
                int pointsGap = nextTier.getMinPoints() - currentPoints;
                int tierRange = currentTier != null
                        ? nextTier.getMinPoints() - currentTier.getMinPoints()
                        : nextTier.getMinPoints();

                double progress = tierRange > 0
                        ? (double)(tierRange - pointsGap) / tierRange * 100
                        : 0;

                // Nếu đã hoàn thành >= 80% (trong 20% cuối)
                if (progress >= 80 && pointsGap > 0) {
                    createTierUpgradeNotification(customer.getId(), nextTier.getDisplayName(), pointsGap);
                }
            }

            // Kiểm tra lên VIP
            if (customer.getCustomerType() == CustomerType.REGULAR) {
                int goldMinPoints = VipTier.GOLD.getMinPoints();
                if (currentPoints >= goldMinPoints * 0.7 && currentPoints < goldMinPoints) {
                    // Đã đạt 70% điểm cần thiết để lên VIP
                    createUpgradeToVipNotification(customer.getId(), goldMinPoints - currentPoints);
                }
            }
        }
    }

    /**
     * Helper method: Lấy hạng tiếp theo
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

        return null;
    }
}
