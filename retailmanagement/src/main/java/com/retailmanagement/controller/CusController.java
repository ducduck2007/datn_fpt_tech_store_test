package com.retailmanagement.controller;


import com.retailmanagement.dto.request.CustomerRequest;
import com.retailmanagement.dto.response.CustomerResponse;
import com.retailmanagement.entity.CustomerType;
import com.retailmanagement.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/{type}")
    public ResponseEntity<List<CustomerResponse>> findByCustomerType(@Valid @PathVariable CustomerType type) {
        return ResponseEntity.status(HttpStatus.OK).body(cusservice.findbyCustomerType(type));
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

}
