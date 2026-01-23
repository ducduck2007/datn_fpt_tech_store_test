package com.retailmanagement.repository;



import com.retailmanagement.entity.Order;
import com.retailmanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByOrderIdAndStatus(Long orderId, String status);
    long countByCreatedAtBetween(Instant start, Instant end);

    /**
     * Tìm tất cả orders của một customer
     */


    /**
     * Tìm orders theo status
     */
    List<Order> findByStatus(String status);


}