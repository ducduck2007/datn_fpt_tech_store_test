package com.retailmanagement.controller;

import com.retailmanagement.dto.response.CustomerBirthdayResponse;
import com.retailmanagement.dto.response.CustomerResponse;
import com.retailmanagement.entity.Customer;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth/admin/birthdays")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chỉ admin mới truy cập được
public class BirthdayAdminController {

    private final CustomRes customerRepository;
    private final NotificationService notificationService;

    /**
     * Lấy danh sách khách hàng có sinh nhật hôm nay
     */
    @GetMapping("/today")
    public ResponseEntity<List<CustomerBirthdayResponse>> getBirthdaysToday() {
        List<Customer> customers = customerRepository.findCustomersWithBirthdayToday();
        List<CustomerBirthdayResponse> response = customers.stream()
                .map(this::mapToBirthdayResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách khách hàng có sinh nhật trong tháng
     * @param month Tháng (1-12)
     */
    @GetMapping("/month/{month}")
    public ResponseEntity<List<CustomerBirthdayResponse>> getBirthdaysByMonth(
            @PathVariable int month
    ) {
        if (month < 1 || month > 12) {
            throw new RuntimeException("Tháng phải từ 1 đến 12");
        }

        List<Customer> customers = customerRepository.findCustomersWithBirthdayInMonth(month);
        List<CustomerBirthdayResponse> response = customers.stream()
                .map(this::mapToBirthdayResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thống kê sinh nhật theo tất cả các tháng
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBirthdayStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // Thống kê theo từng tháng
        Map<Integer, Long> monthlyStats = new HashMap<>();
        Map<Integer, List<CustomerBirthdayResponse>> monthlyCustomers = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            long count = customerRepository.countCustomersWithBirthdayInMonth(month);
            List<Customer> customers = customerRepository.findCustomersWithBirthdayInMonth(month);

            monthlyStats.put(month, count);
            monthlyCustomers.put(month, customers.stream()
                    .map(this::mapToBirthdayResponse)
                    .collect(Collectors.toList()));
        }

        statistics.put("monthlyCount", monthlyStats);
        statistics.put("monthlyCustomers", monthlyCustomers);
        statistics.put("totalCustomersWithBirthday",
                customerRepository.count());

        return ResponseEntity.ok(statistics);
    }

    /**
     * Lấy sinh nhật trong khoảng thời gian
     */
    @GetMapping("/range")
    public ResponseEntity<List<CustomerBirthdayResponse>> getBirthdaysByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Customer> customers = customerRepository.findCustomersWithBirthdayBetween(
                startDate.getMonthValue(),
                startDate.getDayOfMonth(),
                endDate.getMonthValue(),
                endDate.getDayOfMonth()
        );

        List<CustomerBirthdayResponse> response = customers.stream()
                .map(this::mapToBirthdayResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách sinh nhật sắp tới (7 ngày tới)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<CustomerBirthdayResponse>> getUpcomingBirthdays(
            @RequestParam(defaultValue = "7") int days
    ) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        List<Customer> customers = customerRepository.findCustomersWithBirthdayBetween(
                today.getMonthValue(),
                today.getDayOfMonth(),
                endDate.getMonthValue(),
                endDate.getDayOfMonth()
        );

        List<CustomerBirthdayResponse> response = customers.stream()
                .map(this::mapToBirthdayResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Gửi lời chúc sinh nhật cho khách hàng
     */
    @PostMapping("/send-greeting/{customerId}")
    public ResponseEntity<Map<String, String>> sendBirthdayGreeting(
            @PathVariable Integer customerId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                throw new RuntimeException("Nội dung lời chúc không được để trống");
            }

            // Gọi service để tạo thông báo
            notificationService.createCustomBirthdayNotification(customerId, message);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đã gửi lời chúc sinh nhật thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // Helper method
    private CustomerBirthdayResponse mapToBirthdayResponse(Customer customer) {
        LocalDate birthday = customer.getDateOfBirth();
        LocalDate today = LocalDate.now();

        // Tính ngày sinh nhật năm nay
        LocalDate birthdayThisYear = birthday.withYear(today.getYear());

        // Nếu sinh nhật năm nay đã qua, tính cho năm sau
        if (birthdayThisYear.isBefore(today)) {
            birthdayThisYear = birthdayThisYear.plusYears(1);
        }

        // Tính số ngày đến sinh nhật tiếp theo
        long daysUntilBirthday = today.until(birthdayThisYear).getDays();

        // Tính tuổi (tuổi năm nay hoặc năm sau)
        int age = today.getYear() - birthday.getYear();
        if (birthdayThisYear.isAfter(today)) {
            age++; // Tuổi sẽ đạt được vào sinh nhật tiếp theo
        }

        return CustomerBirthdayResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(birthday)
                .birthdayDisplay(birthday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .birthMonth(birthday.getMonthValue())
                .birthDay(birthday.getDayOfMonth())
                .age(age)
                .daysUntilBirthday(daysUntilBirthday)
                .isBirthdayToday(daysUntilBirthday == 0)
                .customerType(customer.getCustomerType())
                .vipTier(customer.getVipTier())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .totalSpent(customer.getTotalSpent())
                .build();
    }
}