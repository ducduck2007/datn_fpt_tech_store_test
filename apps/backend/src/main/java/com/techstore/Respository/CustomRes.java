package com.techstore.Respository;

import com.techstore.Entity.Customer;
import com.techstore.Entity.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CustomRes extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String Email);
    Optional<Customer> findByPhone(String phone);
    List<Customer> findAll();
    List<Customer> findByCustomerType(CustomerType type);
}
