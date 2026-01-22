package com.retailmanagement.repository;

import com.retailmanagement.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    List<Order> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    long countByCreatedAtBetween(Instant start, Instant end);
}

