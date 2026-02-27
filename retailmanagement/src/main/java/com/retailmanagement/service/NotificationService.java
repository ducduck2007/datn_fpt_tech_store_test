package com.retailmanagement.service;

import com.retailmanagement.dto.request.SendNotificationRequest;
import com.retailmanagement.dto.response.CustomerBirthdayResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.NotificationRepository;
import com.retailmanagement.repository.PromotionRepository;
import com.retailmanagement.repository.PromotionRedemptionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomRes customerRepository;
    private final SpinWheelService spinWheelService;
    // ✅ THÊM MỚI: để tạo & kiểm tra voucher sinh nhật
    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository promotionRedemptionRepository;

    // ----------------------------------------------------------------
    // Constants cho voucher sinh nhật
    // ----------------------------------------------------------------
    private static final String  BIRTHDAY_VOUCHER_CODE      = "BIRTHDAY250K";
    private static final BigDecimal BIRTHDAY_DISCOUNT_AMOUNT = new BigDecimal("250000");
    private static final BigDecimal BIRTHDAY_MIN_ORDER       = new BigDecimal("1000000");
    private static final int     BIRTHDAY_VALID_DAYS         = 7;   // hiệu lực 7 ngày

    public NotificationService(
            NotificationRepository notificationRepository,
            CustomRes customerRepository,
            @Lazy SpinWheelService spinWheelService,
            PromotionRepository promotionRepository,                    // ✅ THÊM
            PromotionRedemptionRepository promotionRedemptionRepository // ✅ THÊM
    ) {
        this.notificationRepository          = notificationRepository;
        this.customerRepository              = customerRepository;
        this.spinWheelService                = spinWheelService;
        this.promotionRepository             = promotionRepository;
        this.promotionRedemptionRepository   = promotionRedemptionRepository;
    }

    // ================================================================
    // ✅ HELPER CHUNG
    // ================================================================
    @Transactional
    public void createAndSaveNotification(
            Integer customerId,
            NotificationType type,
            String title,
            String message
    ) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng: " + customerId));

        Notification notification = Notification.builder()
                .customer(customer)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    // ================================================================
    // ✅ WELCOME — gửi thủ công từ admin (ZeroOrderCustomers page)
    //    POST /api/auth/notifications/send
    // ================================================================
    @Transactional
    public Map<String, Object> sendToCustomers(SendNotificationRequest request) {
        int success = 0;
        int fail    = 0;
        List<String> errors = new ArrayList<>();

        NotificationType type;
        try {
            type = NotificationType.valueOf(request.getType());
        } catch (Exception e) {
            type = NotificationType.WELCOME;
        }

        for (Integer customerId : request.getCustomerIds()) {
            try {
                createAndSaveNotification(
                        customerId,
                        type,
                        request.getTitle(),
                        request.getMessage()
                );
                success++;
            } catch (Exception e) {
                fail++;
                errors.add("Customer " + customerId + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("fail",    fail);
        result.put("total",   request.getCustomerIds().size());
        if (!errors.isEmpty()) result.put("errors", errors);
        return result;
    }

    // ================================================================
    // ✅ WELCOME — tự động scheduled (chạy mỗi ngày 9h sáng)
    // ================================================================
    @Transactional
    public void sendWelcomeToZeroOrderCustomers() {
        LocalDateTime registeredBefore = LocalDateTime.now().minusDays(3);
        List<Customer> zeroOrderCustomers = customerRepository.findZeroOrderCustomers(registeredBefore);

        int sent    = 0;
        int skipped = 0;

        for (Customer customer : zeroOrderCustomers) {
            if (!customer.getIsActive()) { skipped++; continue; }

            boolean alreadySent = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                    customer.getId(),
                    NotificationType.WELCOME,
                    LocalDateTime.now().minusDays(7),
                    LocalDateTime.now()
            );
            if (alreadySent) { skipped++; continue; }

            long daysRegistered = ChronoUnit.DAYS.between(
                    customer.getCreatedAt(), LocalDateTime.now()
            );

            String title;
            String message;

            if (daysRegistered <= 7) {
                title = "🎉 Chào mừng bạn đến với cửa hàng!";
                message = String.format(
                        "Xin chào %s! 👋\n\n" +
                                "Cảm ơn bạn đã đăng ký tài khoản. Bạn chưa có đơn hàng nào — " +
                                "hãy khám phá ngay các sản phẩm laptop & phụ kiện chất lượng cao nhé!\n\n" +
                                "🛍️ Đặt đơn đầu tiên để nhận ưu đãi dành cho khách mới! 💻",
                        customer.getName()
                );
            } else if (daysRegistered <= 30) {
                title = "💰 Ưu đãi đặc biệt chỉ dành riêng cho bạn!";
                message = String.format(
                        "Xin chào %s!\n\n" +
                                "Bạn đã đăng ký %d ngày rồi nhưng chưa đặt đơn hàng nào. " +
                                "Đừng bỏ lỡ!\n\n" +
                                "🎁 Chúng tôi dành tặng ưu đãi GIẢM GIÁ ĐẶC BIỆT cho lần mua đầu tiên của bạn!\n\n" +
                                "⏰ Ưu đãi có hạn — Xem ngay để không bỏ lỡ! 🚀",
                        customer.getName(), daysRegistered
                );
            } else {
                title = "🤝 Chúng tôi muốn hỗ trợ bạn tìm sản phẩm phù hợp!";
                message = String.format(
                        "Xin chào %s!\n\n" +
                                "Bạn đã đăng ký tài khoản hơn 1 tháng nhưng chưa thực hiện đơn hàng nào. " +
                                "Có thể bạn chưa tìm được sản phẩm phù hợp?\n\n" +
                                "💬 Đội ngũ tư vấn của chúng tôi sẵn sàng giúp bạn chọn " +
                                "chiếc laptop hoàn hảo nhất!\n\n" +
                                "✅ Tư vấn miễn phí  ✅ Bảo hành chính hãng  ✅ Giao hàng nhanh\n\n" +
                                "Liên hệ ngay với chúng tôi nhé! 📞",
                        customer.getName()
                );
            }

            createAndSaveNotification(customer.getId(), NotificationType.WELCOME, title, message);
            sent++;
        }

        System.out.printf("✅ sendWelcomeToZeroOrderCustomers: sent=%d, skipped=%d%n", sent, skipped);
    }

    // ================================================================
    // ✅ BIRTHDAY — CÓ CẬP NHẬT: tặng kèm voucher BIRTHDAY250K
    // ================================================================

    /**
     * Đảm bảo promotion BIRTHDAY250K tồn tại và còn hiệu lực.
     * Nếu chưa có hoặc đã hết hạn → tạo mới với window 7 ngày từ hôm nay.
     * Dùng chung 1 code cho tất cả khách sinh nhật hôm nay.
     */
    private Promotion ensureBirthdayVoucherExists() {
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(BIRTHDAY_VALID_DAYS);

        Optional<Promotion> existing = promotionRepository.findByCode(BIRTHDAY_VOUCHER_CODE);

        if (existing.isPresent()) {
            Promotion p = existing.get();
            // Còn hiệu lực → dùng lại, không tạo mới
            if (p.getIsActive() && p.getEndDate().isAfter(now)) {
                return p;
            }
            // Hết hạn → gia hạn thêm 7 ngày từ hôm nay
            p.setStartDate(now);
            p.setEndDate(endDate);
            p.setIsActive(true);
            Promotion updated = promotionRepository.save(p);
            System.out.printf("♻️ [BIRTHDAY VOUCHER] Gia hạn %s → %s%n", now, endDate);
            return updated;
        }

        // Chưa tồn tại → tạo mới
        Promotion voucher = Promotion.builder()
                .code(BIRTHDAY_VOUCHER_CODE)
                .name("🎂 Voucher sinh nhật — Giảm 250.000đ")
                .description("Ưu đãi sinh nhật: giảm 250.000đ cho đơn hàng từ 1.000.000đ. " +
                        "Hiệu lực 7 ngày kể từ ngày sinh nhật.")
                .discountType("AMOUNT")
                .discountValue(BIRTHDAY_DISCOUNT_AMOUNT)
                .minOrderAmount(BIRTHDAY_MIN_ORDER)
                .applicabilityJson("{\"scope\":\"ALL\"}")
                .rulesJson(null)  // không giới hạn số lần dùng (mỗi khách tự dùng 1 lần)
                .priority(10)     // ưu tiên cao hơn promo thường
                .stackable(false)
                .startDate(now)
                .endDate(endDate)
                .isActive(true)
                .createdBy(null)  // SYSTEM
                .createdAt(now)
                .build();

        Promotion saved = promotionRepository.save(voucher);

        // Khởi tạo redemption counter
        PromotionRedemption redemption = new PromotionRedemption();
        redemption.setPromotionId(saved.getId());
        redemption.setUsedCount(0L);
        redemption.setUpdatedAt(now);
        promotionRedemptionRepository.save(redemption);

        System.out.printf("✅ [BIRTHDAY VOUCHER] Tạo mới %s, hiệu lực đến %s%n",
                BIRTHDAY_VOUCHER_CODE, endDate);
        return saved;
    }

    /**
     * Tạo thông báo sinh nhật kèm voucher cho 1 khách.
     * Chỉ gửi 1 lần / năm (kiểm tra theo năm hiện tại).
     */
    @Transactional
    public void createBirthdayNotification(Customer customer) {
        LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime yearEnd   = LocalDate.now().withDayOfYear(1).plusYears(1).atStartOfDay();

        boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(), NotificationType.BIRTHDAY, yearStart, yearEnd
        );
        if (exists) return; // Đã gửi trong năm nay rồi

        // ✅ Đảm bảo voucher BIRTHDAY250K tồn tại / còn hạn
        Promotion voucher = ensureBirthdayVoucherExists();

        // Tính ngày hết hạn voucher để hiển thị trong thông báo
        String expiryDisplay = voucher.getEndDate()
                .toLocalDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Notification notification = Notification.builder()
                .customer(customer)
                .type(NotificationType.BIRTHDAY)
                .title("🎂 Chúc mừng sinh nhật " + customer.getName() + "!")
                .message(String.format(
                        "🎉 Sinh nhật vui vẻ, %s!\n\n" +
                                "Nhân ngày đặc biệt này, cửa hàng gửi tặng bạn voucher ưu đãi:\n\n" +
                                "🎁 Mã voucher: %s\n" +
                                "💰 Giảm: 250.000đ\n" +
                                "🛒 Đơn tối thiểu: 1.000.000đ\n" +
                                "⏳ Hết hạn: %s\n\n" +
                                "Nhập mã khi thanh toán để áp dụng ưu đãi nhé! 🛍️",
                        customer.getName(),
                        BIRTHDAY_VOUCHER_CODE,
                        expiryDisplay
                ))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        System.out.printf("🎂 [BIRTHDAY] Đã gửi thông báo + voucher %s cho khách #%d (%s)%n",
                BIRTHDAY_VOUCHER_CODE, customer.getId(), customer.getName());
    }

    /**
     * Job chính: quét khách sinh nhật hôm nay và gửi thông báo + voucher.
     * Được gọi bởi BirthdayScheduler lúc 6:00 sáng.
     */
    @Transactional
    public void sendBirthdayNotifications() {
        List<Customer> birthdayCustomers = customerRepository.findCustomersWithBirthdayToday();

        int sent    = 0;
        int skipped = 0;

        for (Customer customer : birthdayCustomers) {
            if (!customer.getIsActive()) { skipped++; continue; }
            createBirthdayNotification(customer);
            sent++;
        }

        System.out.printf("📨 [BIRTHDAY JOB] sent=%d, skipped=%d (inactive), total=%d%n",
                sent, skipped, birthdayCustomers.size());
    }

    // ----------------------------------------------------------------
    // Các method birthday còn lại — GIỮ NGUYÊN
    // ----------------------------------------------------------------

    @Transactional
    public void createCustomBirthdayNotification(Integer customerId, String customMessage) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        if (!customer.getIsActive()) throw new RuntimeException("Khách hàng không hoạt động");

        Notification notification = Notification.builder()
                .customer(customer)
                .type(NotificationType.BIRTHDAY)
                .title("🎂 Lời chúc sinh nhật từ Admin")
                .message(customMessage)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

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

    // ================================================================
    // READ / DELETE / COUNT — GIỮ NGUYÊN
    // ================================================================

    public List<Notification> getUnreadNotifications(Integer customerId) {
        return notificationRepository.findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);
    }

    public List<Notification> getAllNotifications(Integer customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public long countUnread(Integer customerId) {
        return notificationRepository.countByCustomerIdAndIsReadFalse(customerId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Integer customerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
        if (!notification.getCustomer().getId().equals(customerId))
            throw new RuntimeException("Bạn không có quyền đọc thông báo này");
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Integer customerId) {
        List<Notification> unread = notificationRepository
                .findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);
        for (Notification n : unread) { n.setIsRead(true); n.setReadAt(LocalDateTime.now()); }
        if (!unread.isEmpty()) notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Integer customerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
        if (!notification.getCustomer().getId().equals(customerId))
            throw new RuntimeException("Bạn không có quyền xóa thông báo này");
        notificationRepository.delete(notification);
    }

    // ================================================================
    // BIRTHDAY CALENDAR — GIỮ NGUYÊN
    // ================================================================

    public List<CustomerBirthdayResponse> getBirthdaysByMonth(int month) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        List<Customer> customers = customerRepository.findCustomersWithBirthdayInMonth(month);
        return customers.stream()
                .map(this::mapToResponse)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CustomerBirthdayResponse::getBirthDay))
                .collect(Collectors.toList());
    }

    public List<CustomerBirthdayResponse> getUpcomingBirthdays(int days) {
        LocalDate today   = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        List<Customer> customers = customerRepository.findCustomersWithBirthdayBetween(
                today.getMonthValue(), today.getDayOfMonth(),
                endDate.getMonthValue(), endDate.getDayOfMonth());
        return customers.stream()
                .map(this::mapToResponse)
                .filter(c -> c != null && c.getDaysUntilBirthday() != null && c.getDaysUntilBirthday() <= days)
                .sorted(Comparator.comparing(CustomerBirthdayResponse::getDaysUntilBirthday))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics() {
        Map<Integer, Long> monthlyCount = new HashMap<>();
        for (int month = 1; month <= 12; month++)
            monthlyCount.put(month, customerRepository.countCustomersWithBirthdayInMonth(month));
        long totalCustomers        = customerRepository.count();
        long customersWithBirthday = monthlyCount.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Object> stats = new HashMap<>();
        stats.put("monthlyCount",             monthlyCount);
        stats.put("totalCustomers",           totalCustomers);
        stats.put("customersWithBirthday",    customersWithBirthday);
        stats.put("customersWithoutBirthday", totalCustomers - customersWithBirthday);
        return stats;
    }

    public Map<String, Object> getMonthlyStatistics(int month) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        List<CustomerBirthdayResponse> birthdays = getBirthdaysByMonth(month);
        Map<String, Long> byType = birthdays.stream()
                .collect(Collectors.groupingBy(c -> c.getCustomerType().name(), Collectors.counting()));
        Map<String, Long> byTier = birthdays.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getVipTier() != null ? c.getVipTier().name() : "NONE",
                        Collectors.counting()));
        Map<String, Object> stats = new HashMap<>();
        stats.put("month", month); stats.put("totalCount", birthdays.size());
        stats.put("byCustomerType", byType); stats.put("byVipTier", byTier);
        stats.put("birthdays", birthdays);
        return stats;
    }

    // ================================================================
    // VIP TIER UPGRADE — GIỮ NGUYÊN
    // ================================================================

    @Transactional
    public void createTierUpgradeNotification(Integer customerId, String tierName, int pointsGap) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customerId, NotificationType.VIP_TIER_UPGRADE, weekAgo, LocalDateTime.now());
        if (exists) return;

        String message = pointsGap <= 500
                ? String.format("🔥 Bạn chỉ còn thiếu %,d điểm nữa là lên hạng %s! Hoàn tất một đơn hàng nhỏ để nhận ưu đãi tốt hơn ngay!", pointsGap, tierName)
                : String.format("⭐ Bạn sắp đạt hạng %s với %,d điểm nữa! Tiếp tục mua sắm để tận hưởng nhiều đặc quyền hơn.", tierName, pointsGap);

        createAndSaveNotification(customerId, NotificationType.VIP_TIER_UPGRADE, "🎯 Bạn sắp lên hạng!", message);
    }

    @Transactional
    public void createUpgradeToVipNotification(Integer customerId, int pointsNeeded) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        if (customer.getCustomerType() != CustomerType.REGULAR) return;

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customerId, NotificationType.VIP_TIER_UPGRADE, weekAgo, LocalDateTime.now());
        if (exists) return;

        String message = String.format(
                "👑 Chỉ còn %,d điểm nữa, bạn sẽ trở thành khách hàng VIP! " +
                        "Khách VIP được giảm giá cao hơn, ưu đãi độc quyền và nhiều đặc quyền khác. Mua sắm ngay để nâng cấp!",
                pointsNeeded);
        createAndSaveNotification(customerId, NotificationType.VIP_TIER_UPGRADE, "👑 Sắp trở thành VIP!", message);
    }

    @Transactional
    public void checkAndSendTierUpgradeNotifications() {
        List<Customer> customers = customerRepository.findAll().stream().filter(Customer::getIsActive).toList();
        for (Customer customer : customers) {
            int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
            VipTier nextTier  = getNextTier(customer.getVipTier());
            if (nextTier != null) {
                int pointsGap  = nextTier.getMinPoints() - currentPoints;
                int tierRange  = customer.getVipTier() != null
                        ? nextTier.getMinPoints() - customer.getVipTier().getMinPoints()
                        : nextTier.getMinPoints();
                double progress = tierRange > 0 ? (double)(tierRange - pointsGap) / tierRange * 100 : 0;
                if (progress >= 80 && pointsGap > 0)
                    createTierUpgradeNotification(customer.getId(), nextTier.getDisplayName(), pointsGap);
            }
            if (customer.getCustomerType() == CustomerType.REGULAR) {
                int goldMinPoints = VipTier.GOLD.getMinPoints();
                if (currentPoints >= goldMinPoints * 0.7 && currentPoints < goldMinPoints)
                    createUpgradeToVipNotification(customer.getId(), goldMinPoints - currentPoints);
            }
        }
    }

    private VipTier getNextTier(VipTier currentTier) {
        if (currentTier == null) return VipTier.BRONZE;
        VipTier[] tiers = VipTier.values();
        int idx = currentTier.ordinal();
        return idx < tiers.length - 1 ? tiers[idx + 1] : null;
    }

    // ================================================================
    // PURCHASE REMINDER — GIỮ NGUYÊN
    // ================================================================

    @Transactional
    public void sendPurchaseReminders() {
        LocalDateTime now         = LocalDateTime.now();
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        List<Customer> cold30  = customerRepository.findCustomersForPurchaseReminder(now.minusDays(30),  now.minusDays(60),  twoWeeksAgo);
        List<Customer> cold60  = customerRepository.findCustomersForPurchaseReminder(now.minusDays(60),  now.minusDays(120), twoWeeksAgo);
        List<Customer> winback = customerRepository.findCustomersForPurchaseReminder(now.minusDays(120), now.minusDays(365), twoWeeksAgo);

        cold30.forEach(c  -> createPurchaseReminderNotification(c, "COLD_30"));
        cold60.forEach(c  -> createPurchaseReminderNotification(c, "COLD_60"));
        winback.forEach(c -> createPurchaseReminderNotification(c, "WINBACK"));
    }

    @Transactional
    public void createPurchaseReminderNotification(Customer customer, String segment) {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);
        boolean alreadySent = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(), NotificationType.PURCHASE_REMINDER, twoWeeksAgo, LocalDateTime.now());
        boolean alreadySentWinback = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(), NotificationType.WINBACK, twoWeeksAgo, LocalDateTime.now());
        if (alreadySent || alreadySentWinback) return;

        long daysSinceLastOrder = ChronoUnit.DAYS.between(customer.getLastOrderAt(), LocalDateTime.now());
        String title; String message; NotificationType type;

        switch (segment) {
            case "COLD_30" -> {
                type    = NotificationType.PURCHASE_REMINDER;
                title   = "👀 Có laptop mới về rồi bạn ơi!";
                message = String.format("Chào %s! Đã %d ngày rồi bạn chưa ghé thăm chúng tôi. Nhiều dòng laptop mới vừa về kho, cùng xem nhé!", customer.getName(), daysSinceLastOrder);
            }
            case "COLD_60" -> {
                type    = NotificationType.PURCHASE_REMINDER;
                title   = "🎁 Ưu đãi riêng dành cho bạn – chỉ còn 48h!";
                message = String.format("Chào %s! Chúng tôi nhớ bạn quá. Nhân dịp bạn chưa ghé lâu (%d ngày), shop gửi tặng bạn ưu đãi độc quyền. Xem ngay trước khi hết hạn!", customer.getName(), daysSinceLastOrder);
            }
            default -> {
                type    = NotificationType.WINBACK;
                title   = "💝 Chúng tôi nhớ bạn – Quà tặng đặc biệt!";
                message = String.format("Chào %s! Đã hơn %d ngày rồi bạn chưa ghé shop. Chúng tôi có quà tặng đặc biệt dành riêng cho bạn. Hãy quay lại để nhận ngay nhé! 🎉", customer.getName(), daysSinceLastOrder);
            }
        }

        Notification notification = Notification.builder()
                .customer(customer).type(type).title(title).message(message)
                .isRead(false).createdAt(LocalDateTime.now()).build();
        notificationRepository.save(notification);
    }

    public List<Notification> getPurchaseReminderHistory() {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(NotificationType.PURCHASE_REMINDER);
    }

    // ================================================================
    // SPIN EXPIRY WARNING — GIỮ NGUYÊN
    // ================================================================

    @Transactional
    public void sendSpinExpiryWarnings() {
        List<Map<String, Object>> expiringList = spinWheelService.getExpiringBonuses(24);
        int sent = 0;
        for (Map<String, Object> item : expiringList) {
            Integer customerId = (Integer) item.get("customerId");
            BigDecimal bonus   = (BigDecimal) item.get("discountBonus");
            long hoursLeft     = (long) item.get("hoursLeft");

            boolean alreadySent = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtAfter(
                    customerId, NotificationType.SPIN_EXPIRY_WARNING, LocalDateTime.now().minusHours(12));
            if (alreadySent) continue;

            String title   = "🎡 Ưu đãi vòng quay sắp hết hạn!";
            String message = String.format(
                    "Bạn còn ưu đãi giảm %.0f%% từ vòng quay chưa sử dụng. Còn %d giờ nữa là hết hạn — hãy đặt hàng ngay!",
                    bonus, hoursLeft);
            createAndSaveNotification(customerId, NotificationType.SPIN_EXPIRY_WARNING, title, message);
            sent++;
        }
        System.out.println("✅ Đã gửi " + sent + " cảnh báo spin sắp hết hạn");
    }

    // ================================================================
    // PRIVATE HELPER — GIỮ NGUYÊN
    // ================================================================

    private CustomerBirthdayResponse mapToResponse(Customer customer) {
        LocalDate birthDate = customer.getDateOfBirth();
        if (birthDate == null) return null;
        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();
        LocalDate nextBirthday = LocalDate.of(today.getYear(), birthDate.getMonth(), birthDate.getDayOfMonth());
        if (nextBirthday.isBefore(today)) { nextBirthday = nextBirthday.plusYears(1); age++; }
        long daysUntil = ChronoUnit.DAYS.between(today, nextBirthday);
        return CustomerBirthdayResponse.builder()
                .id(Long.valueOf(customer.getId())).customerId(customer.getId())
                .name(customer.getName()).email(customer.getEmail()).phone(customer.getPhone())
                .dateOfBirth(birthDate)
                .birthdayDisplay(String.format("%02d/%02d/%d", birthDate.getDayOfMonth(), birthDate.getMonthValue(), birthDate.getYear()))
                .birthMonth(birthDate.getMonthValue()).birthDay(birthDate.getDayOfMonth())
                .age(age).daysUntilBirthday(daysUntil).isBirthdayToday(daysUntil == 0)
                .customerType(customer.getCustomerType()).vipTier(customer.getVipTier())
                .loyaltyPoints(customer.getLoyaltyPoints()).totalSpent(customer.getTotalSpent())
                .build();
    }
}