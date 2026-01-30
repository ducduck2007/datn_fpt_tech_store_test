package com.retailmanagement.repository;

import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface CustomRes extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String Email);
    Optional<Customer> findByPhone(String phone);
    List<Customer> findAll();
    List<Customer> findByCustomerType(CustomerType type);
    Optional<Customer> findByName(String name);

    Optional<Customer> findById(Integer id);

    @Query("SELECT c FROM Customer c WHERE c.lastLoginAt >= :since AND c.isActive = true ORDER BY c.lastLoginAt DESC")
    List<Customer> findActiveCustomersSince(@Param("since") LocalDateTime since);
}
