package com.techstore.Controller;

import com.techstore.Entity.Customer;
import com.techstore.Entity.CustomerType;
import com.techstore.Response.CustomerResponse;
import com.techstore.Service.CustomerService;
import com.techstore.auth.dto.CustomerRequest;
import jakarta.validation.Valid;
import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}
