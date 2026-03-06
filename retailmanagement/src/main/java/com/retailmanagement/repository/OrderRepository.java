package com.retailmanagement.repository;

import com.retailmanagement.dto.response.RevenueByCustomerResponse;
import com.retailmanagement.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);


    long countByCustomerId(Integer customerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId " +
            "AND o.createdAt > :cutoff " +
            "AND o.status IN :statuses")
    long countByCustomerIdAndCreatedAtAfterAndStatusIn(
            @Param("customerId") Integer customerId,
            @Param("cutoff") Instant cutoff,
            @Param("statuses") List<String> statuses
    );

    long countByCustomerIdAndStatus(Integer customerId, String status);

    @Query("""
            SELECT o.channel, SUM(o.totalAmount)
            FROM Order o
            WHERE o.status = 'DELIVERED'
            GROUP BY o.channel
            """)
    List<Object[]> revenueByChannel();


    @Query("""
            SELECT CAST(o.createdAt AS date), SUM(o.totalAmount)
            FROM Order o
            WHERE o.status = 'DELIVERED'
            GROUP BY CAST(o.createdAt AS date)
            ORDER BY CAST(o.createdAt AS date)
            """)
    List<Object[]> revenueByDate();


    @Query("""
            SELECT u.username, COUNT(o.id)
            FROM Order o
            JOIN o.user u
            GROUP BY u.username
            ORDER BY COUNT(o.id) DESC
            """)
    List<Object[]> ordersByStaff();
}

