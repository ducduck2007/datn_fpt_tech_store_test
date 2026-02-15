package com.retailmanagement.service;

import com.retailmanagement.audit.Audit;
import com.retailmanagement.audit.AuditAction;
import com.retailmanagement.audit.AuditModule;
import com.retailmanagement.audit.TargetType;
import com.retailmanagement.dto.request.CustomerRequest;
import com.retailmanagement.dto.response.CustomerResponse;
import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.CustomerType;
import com.retailmanagement.entity.LoyaltyLedger;
import com.retailmanagement.entity.User;
import com.retailmanagement.entity.VipTier;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.LoyaltyLedgerRepository;
import com.retailmanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomRes customRes;
    private final LoyaltyLedgerRepository loyaltyLedgerRepository;
    private final UserRepository userRepository; // ✅ NEW: For User lookup

    /**
     * ✅ UPDATED: Save loyalty ledger with User relationship
     * @param createdBy User ID (nullable)
     */
    private void saveLoyaltyLedger(
            Customer customer,
            int pointsDelta,
            String transactionType,
            VipTier tierBefore,
            VipTier tierAfter,
            String reason,
            String note,
            String referenceType,
            Long referenceId,
            Integer createdBy) {

        // ✅ Lookup User if createdBy is provided
        User createdByUser = null;
        if (createdBy != null) {
            createdByUser = userRepository.findById(createdBy).orElse(null);
        }

        LoyaltyLedger ledger = LoyaltyLedger.builder()
                .customer(customer)
                .pointsDelta(pointsDelta)
                .transactionType(transactionType)
                .tierBefore(tierBefore != null ? tierBefore.name() : null)
                .tierAfter(tierAfter != null ? tierAfter.name() : null)
                .reason(reason)
                .note(note)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .createdBy(createdByUser) // ✅ CHANGED: Now User object instead of Integer
                .createdAt(Instant.now())
                .build();

        loyaltyLedgerRepository.save(ledger);

        System.out.println("✅ Saved loyalty ledger: " + transactionType + " | Points: " + pointsDelta +
                " | Tier: " + (tierBefore != null ? tierBefore.name() : "NULL") +
                " → " + (tierAfter != null ? tierAfter.name() : "NULL") +
                " | Created by: " + (createdByUser != null ? createdByUser.getUsername() : "SYSTEM"));
    }

    private String formatMoney(BigDecimal amount) {
        return String.format("%,d VNĐ", amount.longValue());
    }

    private String getDeductNote(String reason, BigDecimal amount) {
        return switch (reason) {
            case "CANCEL_ORDER" -> "Trừ điểm do hủy đơn: " + formatMoney(amount);
            case "RETURN" -> "Trừ điểm do trả hàng: " + formatMoney(amount);
            default -> "Trừ điểm: " + formatMoney(amount);
        };
    }

    @Audit(
            module = AuditModule.CUSTOMER,
            action = AuditAction.CREATE,
            targetType = TargetType.CUSTOMER
    )
    @Transactional
    public CustomerResponse create(CustomerRequest customerRequest) {
        if (customRes.findByEmail(customerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }
        if (customRes.findByPhone(customerRequest.getPhone()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống");
        }

        CustomerType type = (customerRequest.getCustomerType() != null)
                ? customerRequest.getCustomerType()
                : CustomerType.REGULAR;

        Customer customer = Customer.builder()
                .userId(customerRequest.getUserId())
                .name(customerRequest.getFullName())
                .email(customerRequest.getEmail())
                .phone(customerRequest.getPhone())
                .dateOfBirth(customerRequest.getBirthDate())
                .address(customerRequest.getAddress())
                .notes(customerRequest.getNotes())
                .customerType(type)
                .vipTier(null)
                .totalSpent(BigDecimal.ZERO)
                .loyaltyPoints(0)
                .isActive(true)
                .build();

        Customer saved = customRes.save(customer);
        return mapToResponse(saved);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        VipTier currentTier = customer.getVipTier();
        int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;

        Integer pointsToNext = 0;

        if (currentTier == null) {
            pointsToNext = VipTier.BRONZE.getMinPoints() - currentPoints;
        } else {
            Integer nextThreshold = currentTier.getNextTierThreshold();
            if (nextThreshold != null) {
                pointsToNext = nextThreshold - currentPoints;
            } else {
                pointsToNext = 0;
            }
        }
        if (pointsToNext < 0) pointsToNext = 0;

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(customer.getDateOfBirth())
                .customerType(customer.getCustomerType())
                .customerTypeDisplay(customer.getCustomerType().getDisplayname())
                .vipTier(currentTier)
                .vipTierDisplay(currentTier != null ? currentTier.getDisplayName() : "Member")
                .loyaltyPoints(currentPoints)
                .pointsToNextTier(pointsToNext)
                .discountRate(currentTier != null ? currentTier.getDiscountRate() : 0.0)
                .lastLoginAt(customer.getLastLoginAt())
                .totalSpent(customer.getTotalSpent())
                .lastOrderAt(customer.getLastOrderAt())
                .address(customer.getAddress())
                .notes(customer.getNotes())
                .segmentsJson(customer.getSegmentsJson())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    @Transactional
    public void addLoyaltyPoints(Integer customerId, BigDecimal orderTotal) {
        Customer customer = customRes.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        int pointsBefore = customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints();
        VipTier tierBefore = customer.getVipTier();

        int pointsEarned = orderTotal.divide(BigDecimal.valueOf(10000)).intValue();
        if (pointsEarned <= 0) return;

        int newTotalPoints = pointsBefore + pointsEarned;
        customer.setLoyaltyPoints(newTotalPoints);

        BigDecimal currentSpent = customer.getTotalSpent() == null ? BigDecimal.ZERO : customer.getTotalSpent();
        customer.setTotalSpent(currentSpent.add(orderTotal));

        VipTier newTier = VipTier.fromPoints(newTotalPoints);
        customer.setVipTier(newTier);

        if (newTier == null || newTier == VipTier.BRONZE || newTier == VipTier.SILVER) {
            customer.setCustomerType(CustomerType.REGULAR);
        } else {
            customer.setCustomerType(CustomerType.VIP);
        }

        customRes.save(customer);

        saveLoyaltyLedger(
                customer,
                pointsEarned,
                "EARN",
                tierBefore,
                newTier,
                "PURCHASE",
                "Cộng điểm từ thanh toán: " + formatMoney(orderTotal),
                "orders",
                null,
                null
        );

        if (tierBefore != newTier) {
            String transactionType = (newTier != null && (tierBefore == null ||
                    newTier.ordinal() > tierBefore.ordinal())) ? "TIER_UPGRADE" : "TIER_DOWNGRADE";

            String note = String.format("🎉 Thăng hạng từ %s → %s (Điểm: %d)",
                    tierBefore != null ? tierBefore.getDisplayName() : "Member",
                    newTier != null ? newTier.getDisplayName() : "Member",
                    newTotalPoints);

            saveLoyaltyLedger(
                    customer,
                    0,
                    transactionType,
                    tierBefore,
                    newTier,
                    "TIER_CHANGE",
                    note,
                    null,
                    null,
                    null
            );
        }
    }

    public List<CustomerResponse> findAll() {
        return customRes.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<CustomerResponse> findbyCustomerType(CustomerType type) {
        return customRes.findByCustomerType(type).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Audit(
            module = AuditModule.CUSTOMER,
            action = AuditAction.DELETE,
            targetType = TargetType.CUSTOMER
    )
    public void deleteById(int id) {
        Customer customer = customRes.findById(id).orElseThrow();
        customer.setIsActive(false);
        customRes.save(customer);
    }

    @Audit(
            module = AuditModule.CUSTOMER,
            action = AuditAction.UPDATE,
            targetType = TargetType.CUSTOMER
    )
    public CustomerResponse updateById(Integer id, CustomerRequest customerRequest) {
        Customer customer = customRes.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Khách hàng với id: " + id));

        if (customerRequest.getFullName() != null) {
            customer.setName(customerRequest.getFullName());
        }

        String oldEmail = customer.getEmail();
        if (customerRequest.getEmail() != null && !customerRequest.getEmail().equals(oldEmail)) {
            if (customRes.findByEmail(customerRequest.getEmail()).isPresent()) {
                throw new RuntimeException("Email này đã tồn tại trong hệ thống");
            }
            customer.setEmail(customerRequest.getEmail());
        }

        String oldPhone = customer.getPhone();
        if (customerRequest.getPhone() != null && !customerRequest.getPhone().equals(oldPhone)) {
            if (customRes.findByPhone(customerRequest.getPhone()).isPresent()) {
                throw new RuntimeException("Số điện thoại này đã tồn tại trong hệ thống");
            }
            customer.setPhone(customerRequest.getPhone());
        }

        if (customerRequest.getBirthDate() != null) {
            customer.setDateOfBirth(customerRequest.getBirthDate());
        }
        if (customerRequest.getAddress() != null) {
            customer.setAddress(customerRequest.getAddress());
        }
        if (customerRequest.getNotes() != null) {
            customer.setNotes(customerRequest.getNotes());
        }

        Customer updated = customRes.save(customer);
        return mapToResponse(updated);
    }

    public CustomerResponse findByEmail(String email) {
        System.out.println("DEBUG Service: Đang tìm khách hàng với email: " + email);

        Optional<Customer> customerOpt = customRes.findByEmail(email.trim());

        if (customerOpt.isEmpty()) {
            System.out.println("DEBUG Service: Không tìm thấy với email: " + email);
            customerOpt = customRes.findByName(email.trim());

            if (customerOpt.isEmpty()) {
                System.out.println("DEBUG Service: Cũng không tìm thấy với username: " + email);
                return null;
            }
        }

        Customer customer = customerOpt.get();
        System.out.println("DEBUG Service: Tìm thấy khách hàng: " + customer.getName() + ", email: " + customer.getEmail());

        return mapToResponse(customer);
    }

    public List<CustomerResponse> findActiveInLast30Days() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Customer> activeCustomers = customRes.findActiveCustomersSince(thirtyDaysAgo);

        return activeCustomers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deductLoyaltyPoints(Integer customerId, BigDecimal orderTotal,
                                    String reason, String referenceType, Long referenceId) {
        Customer customer = customRes.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        int pointsBefore = customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints();
        VipTier tierBefore = customer.getVipTier();

        // ✅ Calculate points to deduct (no penalty multiplication)
        int pointsToDeduct = orderTotal.divide(BigDecimal.valueOf(10000)).intValue();
        if (pointsToDeduct <= 0) return;

        int newTotalPoints = Math.max(0, pointsBefore - pointsToDeduct);
        customer.setLoyaltyPoints(newTotalPoints);

        // ✅ Always subtract from total spent when canceling/returning
        BigDecimal currentSpent = customer.getTotalSpent() == null ? BigDecimal.ZERO : customer.getTotalSpent();
        BigDecimal newSpent = currentSpent.subtract(orderTotal);
        customer.setTotalSpent(newSpent.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newSpent);

        VipTier newTier = VipTier.fromPoints(newTotalPoints);
        customer.setVipTier(newTier);

        if (newTier == null || newTier == VipTier.BRONZE || newTier == VipTier.SILVER) {
            customer.setCustomerType(CustomerType.REGULAR);
        } else {
            customer.setCustomerType(CustomerType.VIP);
        }

        customRes.save(customer);

        // ✅ Determine transaction type (no more PENALTY type)
        String transactionType = "DEDUCT";
        String note = getDeductNote(reason, orderTotal);

        saveLoyaltyLedger(
                customer,
                -pointsToDeduct,  // ✅ Simple negative points (no penalty)
                transactionType,
                tierBefore,
                newTier,
                reason,
                note,
                referenceType,
                referenceId,
                null
        );

        // Handle tier downgrade if applicable
        if (tierBefore != newTier) {
            String note2 = String.format("⚠️ Hạ hạng từ %s → %s do trừ điểm (Điểm còn: %d)",
                    tierBefore != null ? tierBefore.getDisplayName() : "Member",
                    newTier != null ? newTier.getDisplayName() : "Member",
                    newTotalPoints);

            saveLoyaltyLedger(
                    customer,
                    0,
                    "TIER_DOWNGRADE",
                    tierBefore,
                    newTier,
                    "TIER_CHANGE",
                    note2,
                    null,
                    null,
                    null
            );
        }
    }

    public List<CustomerResponse> findByPointsRange(int minPoints, int maxPoints) {
        List<Customer> customers = customRes.findByLoyaltyPointsBetween(minPoints, maxPoints);
        return customers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerResponse> findByVipTier(String tierName) {
        try {
            VipTier tier = VipTier.valueOf(tierName.toUpperCase());
            List<Customer> customers = customRes.findByVipTier(tier);
            return customers.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid VIP tier: " + tierName);
        }
    }

    public List<CustomerResponse> findByVipTierAndPointsRange(
            String tierName,
            int minPoints,
            int maxPoints
    ) {
        try {
            VipTier tier = VipTier.valueOf(tierName.toUpperCase());
            List<Customer> customers = customRes.findByVipTierAndLoyaltyPointsBetween(
                    tier, minPoints, maxPoints
            );
            return customers.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid VIP tier: " + tierName);
        }
    }

    public List<CustomerResponse> findBySpendingRange(BigDecimal minSpent, BigDecimal maxSpent) {
        List<Customer> customers = customRes.findByTotalSpentBetween(minSpent, maxSpent);
        return customers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerResponse> findTopSpenders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Customer> customers = customRes.findTopByTotalSpent(pageable);
        return customers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerResponse> findTopSpendersByVipTier(String tierName, int limit) {
        try {
            VipTier tier = VipTier.valueOf(tierName.toUpperCase());
            Pageable pageable = PageRequest.of(0, limit);
            List<Customer> customers = customRes.findTopByVipTierAndTotalSpent(tier, pageable);
            return customers.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid VIP tier: " + tierName);
        }
    }

    public Map<String, Object> getSpendingStatistics() {
        Map<String, Object> stats = new HashMap<>();

        BigDecimal totalSpent = customRes.getTotalSpentAllCustomers();
        stats.put("totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO);

        long totalCustomers = customRes.findAll().stream()
                .filter(Customer::getIsActive)
                .count();
        stats.put("totalCustomers", totalCustomers);

        BigDecimal avgSpent = BigDecimal.ZERO;
        if (totalCustomers > 0 && totalSpent != null) {
            avgSpent = totalSpent.divide(
                    BigDecimal.valueOf(totalCustomers),
                    2,
                    BigDecimal.ROUND_HALF_UP
            );
        }
        stats.put("averageSpent", avgSpent);

        Map<String, Object> tierStats = new HashMap<>();
        for (VipTier tier : VipTier.values()) {
            BigDecimal tierTotal = customRes.getTotalSpentByVipTier(tier);
            long tierCount = customRes.findByVipTier(tier).size();

            Map<String, Object> tierData = new HashMap<>();
            tierData.put("totalSpent", tierTotal != null ? tierTotal : BigDecimal.ZERO);
            tierData.put("customerCount", tierCount);
            tierData.put("averageSpent", tierCount > 0 && tierTotal != null
                    ? tierTotal.divide(BigDecimal.valueOf(tierCount), 2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO);

            tierStats.put(tier.name(), tierData);
        }
        stats.put("byTier", tierStats);

        Map<String, Long> spendingRanges = new HashMap<>();
        spendingRanges.put("under1M", customRes.countByTotalSpentBetween(
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000000)
        ));
        spendingRanges.put("1M-5M", customRes.countByTotalSpentBetween(
                BigDecimal.valueOf(1000000),
                BigDecimal.valueOf(5000000)
        ));
        spendingRanges.put("5M-10M", customRes.countByTotalSpentBetween(
                BigDecimal.valueOf(5000000),
                BigDecimal.valueOf(10000000)
        ));
        spendingRanges.put("10M-50M", customRes.countByTotalSpentBetween(
                BigDecimal.valueOf(10000000),
                BigDecimal.valueOf(50000000)
        ));
        spendingRanges.put("over50M", customRes.countByTotalSpentBetween(
                BigDecimal.valueOf(50000000),
                BigDecimal.valueOf(999999999)
        ));
        stats.put("spendingRanges", spendingRanges);

        return stats;
    }
}