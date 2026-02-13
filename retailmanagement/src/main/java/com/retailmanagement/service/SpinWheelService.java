package com.retailmanagement.service;

import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.CustomerType;
import com.retailmanagement.entity.SpinWheelHistory;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.SpinWheelHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SpinWheelService {

    private final SpinWheelHistoryRepository spinWheelHistoryRepository;
    private final CustomRes customerRepository;

    // Possible discount bonuses with their weights
    private static final Map<BigDecimal, Integer> DISCOUNT_WEIGHTS = new LinkedHashMap<>();
    static {
        DISCOUNT_WEIGHTS.put(BigDecimal.valueOf(1.00), 30);  // 1% - 30% chance
        DISCOUNT_WEIGHTS.put(BigDecimal.valueOf(2.00), 25);  // 2% - 25% chance
        DISCOUNT_WEIGHTS.put(BigDecimal.valueOf(3.00), 20);  // 3% - 20% chance
        DISCOUNT_WEIGHTS.put(BigDecimal.valueOf(5.00), 15);  // 5% - 15% chance
        DISCOUNT_WEIGHTS.put(BigDecimal.valueOf(10.00), 10); // 10% - 10% chance
    }

    /**
     * Get the Monday of current week
     */
    private LocalDate getWeekStartDate() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY);
    }

    /**
     * Check if customer can spin this week
     */
    public Map<String, Object> getSpinStatus(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Map<String, Object> status = new HashMap<>();

        // Check if customer is VIP
        boolean isVip = customer.getCustomerType() == CustomerType.VIP;
        status.put("isVip", isVip);

        if (!isVip) {
            status.put("canSpin", false);
            status.put("message", "Chỉ khách hàng VIP mới được quay thưởng");
            return status;
        }

        // Check if already spun this week
        LocalDate weekStart = getWeekStartDate();
        Optional<SpinWheelHistory> thisWeekSpin =
                spinWheelHistoryRepository.findByCustomerIdAndWeekStartDate(customerId, weekStart);

        if (thisWeekSpin.isPresent()) {
            SpinWheelHistory spin = thisWeekSpin.get();
            status.put("canSpin", false);
            status.put("message", "Bạn đã quay thưởng tuần này");
            status.put("lastSpin", Map.of(
                    "spunAt", spin.getSpunAt(),
                    "discountBonus", spin.getDiscountBonus(),
                    "expiresAt", spin.getExpiresAt()
            ));

            // Check if bonus is still active
            if (!spin.getIsUsed() && spin.getExpiresAt().isAfter(LocalDateTime.now())) {
                status.put("activeBonus", spin.getDiscountBonus());
            }
        } else {
            status.put("canSpin", true);
            status.put("message", "Bạn có thể quay thưởng tuần này!");
        }

        // Get active bonus if any
        Optional<SpinWheelHistory> activeBonus =
                spinWheelHistoryRepository.findMostRecentActiveBonus(customerId, LocalDateTime.now());

        if (activeBonus.isPresent()) {
            status.put("currentBonus", activeBonus.get().getDiscountBonus());
            status.put("bonusExpiresAt", activeBonus.get().getExpiresAt());
        } else {
            status.put("currentBonus", BigDecimal.ZERO);
        }

        return status;
    }

    /**
     * Generate random discount based on weights
     */
    private BigDecimal generateRandomDiscount() {
        int totalWeight = DISCOUNT_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
        Random random = new Random();
        int randomValue = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (Map.Entry<BigDecimal, Integer> entry : DISCOUNT_WEIGHTS.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        return BigDecimal.valueOf(1.00); // Fallback
    }

    /**
     * Perform the spin
     */
    @Transactional
    public Map<String, Object> spin(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Verify customer is VIP
        if (customer.getCustomerType() != CustomerType.VIP) {
            throw new RuntimeException("Only VIP customers can spin the wheel");
        }

        // Check if already spun this week
        LocalDate weekStart = getWeekStartDate();
        Optional<SpinWheelHistory> existingSpin =
                spinWheelHistoryRepository.findByCustomerIdAndWeekStartDate(customerId, weekStart);

        if (existingSpin.isPresent()) {
            throw new RuntimeException("You have already spun this week");
        }

        // Generate random discount
        BigDecimal discountBonus = generateRandomDiscount();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(7); // Bonus valid for 1 week

        // Save spin history
        SpinWheelHistory spinHistory = SpinWheelHistory.builder()
                .customer(customer)
                .discountBonus(discountBonus)
                .weekStartDate(weekStart)
                .spunAt(now)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        spinWheelHistoryRepository.save(spinHistory);

        // Update customer's current bonus
        customer.setSpinDiscountBonus(discountBonus);
        customerRepository.save(customer);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("discountBonus", discountBonus);
        result.put("expiresAt", expiresAt);
        result.put("message", String.format("Chúc mừng! Bạn nhận được %.0f%% giảm giá bổ sung", discountBonus));

        return result;
    }

    /**
     * Get customer's spin history
     */
    public List<Map<String, Object>> getSpinHistory(Integer customerId) {
        List<SpinWheelHistory> history =
                spinWheelHistoryRepository.findByCustomerIdOrderBySpunAtDesc(customerId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (SpinWheelHistory spin : history) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", spin.getId());
            item.put("discountBonus", spin.getDiscountBonus());
            item.put("spunAt", spin.getSpunAt());
            item.put("expiresAt", spin.getExpiresAt());
            item.put("isUsed", spin.getIsUsed());
            item.put("usedOrderId", spin.getUsedOrderId());
            item.put("weekStartDate", spin.getWeekStartDate());

            // Status
            if (spin.getIsUsed()) {
                item.put("status", "Đã sử dụng");
            } else if (spin.getExpiresAt().isBefore(LocalDateTime.now())) {
                item.put("status", "Đã hết hạn");
            } else {
                item.put("status", "Đang hoạt động");
            }

            result.add(item);
        }

        return result;
    }

    /**
     * Mark bonus as used (called when order is placed)
     */
    @Transactional
    public void useBonus(Integer customerId, Long orderId) {
        Optional<SpinWheelHistory> activeBonus =
                spinWheelHistoryRepository.findMostRecentActiveBonus(customerId, LocalDateTime.now());

        if (activeBonus.isPresent()) {
            SpinWheelHistory bonus = activeBonus.get();
            bonus.setIsUsed(true);
            bonus.setUsedOrderId(orderId);
            spinWheelHistoryRepository.save(bonus);

            // Clear customer's bonus
            Customer customer = customerRepository.findById(customerId).orElseThrow();
            customer.setSpinDiscountBonus(BigDecimal.ZERO);
            customerRepository.save(customer);
        }
    }

    /**
     * Get all possible prizes for display on wheel
     */
    public List<Map<String, Object>> getPrizeOptions() {
        List<Map<String, Object>> prizes = new ArrayList<>();
        for (Map.Entry<BigDecimal, Integer> entry : DISCOUNT_WEIGHTS.entrySet()) {
            Map<String, Object> prize = new HashMap<>();
            prize.put("discount", entry.getKey());
            prize.put("weight", entry.getValue());
            prize.put("label", String.format("%.0f%%", entry.getKey()));
            prizes.add(prize);
        }
        return prizes;
    }
}