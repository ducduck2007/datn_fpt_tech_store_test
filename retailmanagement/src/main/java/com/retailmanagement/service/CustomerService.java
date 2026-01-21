package com.retailmanagement.service;

import com.retailmanagement.dto.request.CustomerRequest;
import com.retailmanagement.dto.response.CustomerResponse;
import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.CustomerType;
import com.retailmanagement.entity.VipTier;
import com.retailmanagement.repository.CustomRes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    @Autowired
    private final CustomRes customRes;

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
                .name(customerRequest.getFullName())
                .email(customerRequest.getEmail())
                .phone(customerRequest.getPhone())
                .dateOfBirth(customerRequest.getBirthDate())
                .address(customerRequest.getAddress())
                .notes(customerRequest.getNotes())
                .customerType(type)
                .vipTier(null)              // Mặc định chưa có hạng (Khách thường)
                .totalSpent(BigDecimal.ZERO)
                .loyaltyPoints(0)           // Điểm bắt đầu = 0
                .isActive(true)
                .build();

        Customer saved = customRes.save(customer);
        return mapToResponse(saved);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        VipTier currentTier = customer.getVipTier();
        int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;

        // --- Logic tính điểm lên hạng ---
        Integer pointsToNext = 0;

        if (currentTier == null) {
            // Nếu chưa có hạng (Khách thường), đích đến là BRONZE
            pointsToNext = VipTier.BRONZE.getMinPoints() - currentPoints;
        } else {
            // Nếu đã có hạng, lấy mốc của hạng tiếp theo
            Integer nextThreshold = currentTier.getNextTierThreshold();
            if (nextThreshold != null) {
                pointsToNext = nextThreshold - currentPoints;
            } else {
                pointsToNext = 0; // Đã là Max Level (DIAMOND)
            }
        }
        // Đảm bảo không hiển thị số âm
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
                .vipTierDisplay(currentTier != null ? currentTier.getDisplayName() : "Member") // Hiển thị Member nếu null
                .loyaltyPoints(currentPoints)
                .pointsToNextTier(pointsToNext)
                .discountRate(currentTier != null ? currentTier.getDiscountRate() : 0.0) // 0.0 nếu là khách thường

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

    // --- MỚI: Hàm xử lý cộng điểm khi mua hàng ---
    @Transactional
    public void addLoyaltyPoints(Integer customerId, BigDecimal orderTotal) {
        Customer customer = customRes.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // 1. Quy đổi điểm: Ví dụ 10,000 VND = 1 điểm
        int pointsEarned = orderTotal.divide(BigDecimal.valueOf(10000)).intValue();
        if (pointsEarned <= 0) return;

        // 2. Cộng điểm và tổng chi tiêu
        int currentPoints = customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints();
        int newTotalPoints = currentPoints + pointsEarned;

        customer.setLoyaltyPoints(newTotalPoints);

        BigDecimal currentSpent = customer.getTotalSpent() == null ? BigDecimal.ZERO : customer.getTotalSpent();
        customer.setTotalSpent(currentSpent.add(orderTotal));

        // 3. Tự động cập nhật hạng dựa trên điểm mới
        // Hàm fromPoints trong Enum sẽ trả về hạng tương ứng
        VipTier newTier = VipTier.fromPoints(newTotalPoints);
        customer.setVipTier(newTier);

        // (Tuỳ chọn) Nếu hạng thay đổi, có thể log hoặc gửi thông báo tại đây

        customRes.save(customer);
    }
    // ---------------------------------------------

    public List<CustomerResponse> findAll() {
        return customRes.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<CustomerResponse> findbyCustomerType(CustomerType type) {
        return customRes.findByCustomerType(type).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public void deleteById(int id) {
        Customer customer = customRes.findById(id).orElseThrow();
        customer.setIsActive(false);
        customRes.save(customer);
    }

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

}