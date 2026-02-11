package com.retailmanagement.repository;

import com.retailmanagement.dto.response.RevenueByCustomerResponse;
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

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    @Query("""
    SELECT o
    FROM Order o
    WHERE o.createdAt BETWEEN :from AND :to
    ORDER BY o.createdAt DESC
""")
    List<Order> findOrdersByDateRange(
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
    SELECT o
    FROM Order o
    WHERE o.customer.id = :customerId
      AND o.createdAt BETWEEN :from AND :to
    ORDER BY o.createdAt DESC
""")
    List<Order> findOrdersByCustomerAndDate(
            @Param("customerId") Integer customerId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
    SELECT new com.retailmanagement.dto.response.RevenueByCustomerResponse(
        c.id,
        c.name,
        SUM(oi.lineTotal)
    )
    FROM Order o
    JOIN o.customer c
    JOIN OrderItem oi ON oi.order = o
    WHERE o.status = 'PAID'
    GROUP BY c.id, c.name
    ORDER BY SUM(oi.lineTotal) DESC
""")
    List<RevenueByCustomerResponse> getRevenueByCustomer();

}

