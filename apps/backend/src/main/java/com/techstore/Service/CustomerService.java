package com.techstore.Service;

import com.techstore.Entity.Customer;
import com.techstore.Entity.CustomerType;
import com.techstore.Entity.VipTier;
import com.techstore.Response.CustomerResponse;
import com.techstore.Respository.CustomRes;
import com.techstore.auth.dto.CustomerRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CustomerService {
    @Autowired
    CustomRes customRes;

    @Transactional
    public CustomerResponse create(CustomerRequest customerRequest) {
        if ( customRes.findByEmail(customerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }
        if (customRes.findByPhone(customerRequest.getPhone()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống");
        }
        CustomerType type = (customerRequest.getCustomerType() != null)
                ? customerRequest.getCustomerType()
                : CustomerType.REGULAR;
        Customer customer = Customer.builder()
                .name(customerRequest.getFullName())                    // ✅ name (không phải fullName)
                .email(customerRequest.getEmail())
                .phone(customerRequest.getPhone())
                .dateOfBirth(customerRequest.getBirthDate())      // ✅ dateOfBirth (không phải birthDate)
                .address(customerRequest.getAddress())              // ✅ address
                .notes(customerRequest.getNotes())                  // ✅ notes (không phải note)
                .customerType(type)         // ✅ Mặc định REGULAR
                .vipTier(null)
                .totalSpent(BigDecimal.ZERO)// ✅ NULL cho khách thường
                .loyaltyPoints(0)                          // ✅ Bắt đầu = 0
                .isActive(true)
                .build();

        Customer saved = customRes.save(customer);
        return mapToResponse(saved);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        VipTier tier = customer.getVipTier();
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(customer.getDateOfBirth())
                .customerType(customer.getCustomerType())
                .customerTypeDisplay(customer.getCustomerType().getDisplayname())

                .vipTier(tier)
                .vipTierDisplay(tier != null ? tier.getDisplayName() : null)

                .loyaltyPoints(customer.getLoyaltyPoints())
                .pointsToNextTier(tier != null ? tier.getPointsToNextTier() : 100) // ví dụ: cần 100 để lên BRONZE
                .discountRate(tier != null ? tier.getDiscountMultiplier() : 1.0)   // khách thường = 1.0 (không giảm)

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
}

