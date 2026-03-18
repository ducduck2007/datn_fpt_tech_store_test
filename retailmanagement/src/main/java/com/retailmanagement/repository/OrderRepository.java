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
                WHERE o.status = 'DELIVERED'
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

    @Query(value = """
            SELECT channel, SUM(total_amount) AS revenue
            FROM orders
            WHERE status = 'DELIVERED'
            GROUP BY channel
            """, nativeQuery = true)
    List<Object[]> revenueByChannel();

    @Query(value = """
            SELECT CAST(created_at AS date) AS date,
                   SUM(total_amount)        AS revenue
            FROM orders
            WHERE status = 'DELIVERED'
            GROUP BY CAST(created_at AS date)
            ORDER BY CAST(created_at AS date)
            """, nativeQuery = true)
    List<Object[]> revenueByDate();

    @Query(value = """
            SELECT u.username, COUNT(o.id) AS orders
            FROM orders o
            JOIN users u ON o.user_id = u.id
            WHERE u.role = 'SALES'
            GROUP BY u.username
            ORDER BY COUNT(o.id) DESC
            """, nativeQuery = true)
    List<Object[]> ordersByStaff();

    List<Order> findByAppliedPromotionCodeOrderByCreatedAtDesc(String appliedPromotionCode);

    /**
     * Tổng hợp doanh thu theo trạng thái đơn hàng.
     * Trả về: [status, orderCount, totalRevenue]
     */
    @Query(value = """
    SELECT status,
           COUNT(id)          AS orderCount,
           SUM(total_amount)  AS totalRevenue
    FROM orders
    GROUP BY status
    ORDER BY COUNT(id) DESC
    """, nativeQuery = true)
    List<Object[]> revenueByStatus();
}