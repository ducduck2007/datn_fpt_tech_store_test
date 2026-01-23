package com.retailmanagement.controller;


import com.retailmanagement.dto.request.CustomerRequest;
import com.retailmanagement.dto.response.CustomerResponse;
import com.retailmanagement.entity.CustomerType;
import com.retailmanagement.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth/customers")
public class CusController {
    @Autowired
    private CustomerService cusservice;
    @PostMapping("")
    public ResponseEntity<CustomerResponse> addCustomer(@Valid @RequestBody CustomerRequest cus) {
       CustomerResponse response= cusservice.create(cus);
        return ResponseEntity.status(HttpStatus.CREATED).body(response) ;
    }
    @GetMapping("")
    public ResponseEntity<List<CustomerResponse>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(cusservice.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CustomerResponse> deleteCustomer(@Valid @PathVariable int id) {
        cusservice.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@Valid @PathVariable int id, @RequestBody CustomerRequest cus) {
        CustomerResponse up = cusservice.updateById(id, cus);
        return ResponseEntity.ok().body(up) ;
    }
    @PostMapping("/{id}/points")
    public ResponseEntity<String> addPoints(
            @PathVariable Integer id,
            @RequestBody Map<String, BigDecimal> payload) {

        BigDecimal amount = payload.get("amount");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount phải lớn hơn 0");
        }

        cusservice.addLoyaltyPoints(id, amount);
        return ResponseEntity.ok("Đã cộng điểm và cập nhật hạng thành công!");
    }

    // ĐƯA HÀM NÀY LÊN TRÊN
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("DEBUG: Không có authentication hoặc chưa xác thực");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }

        String currentUsername = authentication.getName();
        System.out.println("DEBUG: Username từ SecurityContext: " + currentUsername);
        System.out.println("DEBUG: Principal: " + authentication.getPrincipal());
        System.out.println("DEBUG: Authorities: " + authentication.getAuthorities());

        try {
            CustomerResponse profile = cusservice.findByEmail(currentUsername);

            if (profile == null) {
                System.out.println("DEBUG: Không tìm thấy khách hàng với email: " + currentUsername);

                // Thử tìm bằng username nếu không phải email
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Không tìm thấy thông tin khách hàng",
                                "username", currentUsername
                        ));
            }

            System.out.println("DEBUG: Tìm thấy khách hàng: " + profile.getEmail());
            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            System.out.println("DEBUG: Lỗi khi tìm khách hàng: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống", "message", e.getMessage()));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CustomerResponse>> findByCustomerType(@PathVariable CustomerType type) {
        return ResponseEntity.ok(cusservice.findbyCustomerType(type));
    }

    // ĐƯA HÀM NÀY XUỐNG DƯỚI VÀ SỬA PATH


}
