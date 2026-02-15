package com.retailmanagement.repository;

import com.retailmanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ============================================================
    // STANDARD QUERIES (automatically filtered by @Where annotation)
    // ============================================================

    /**
     * Find all active payments by order ID
     * @Where annotation automatically filters deleted_at IS NULL
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Find active payments by order ID and status
     */
    List<Payment> findByOrderIdAndStatus(Long orderId, String status);

    /**
     * Count active payments created between dates
     */
    long countByCreatedAtBetween(Instant start, Instant end);

    // ============================================================
    // SOFT DELETE QUERIES (explicit deleted_at handling)
    // ============================================================

    /**
     * Find ALL payments by order ID (including soft-deleted)
     * Must use native query to bypass @Where filter
     */
    @Query(value = "SELECT * FROM payments WHERE order_id = :orderId", nativeQuery = true)
    List<Payment> findAllByOrderIdIncludingDeleted(@Param("orderId") Long orderId);

    /**
     * Find only soft-deleted payments by order ID
     */
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.deletedAt IS NOT NULL")
    List<Payment> findDeletedByOrderId(@Param("orderId") Long orderId);

    /**
     * Find active payment by ID (explicit check)
     */
    @Query("SELECT p FROM Payment p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Payment> findActiveById(@Param("id") Long id);

    /**
     * Find soft-deleted payment by ID
     */
    @Query("SELECT p FROM Payment p WHERE p.id = :id AND p.deletedAt IS NOT NULL")
    Optional<Payment> findDeletedById(@Param("id") Long id);

    /**
     * Find all soft-deleted payments
     */
    @Query("SELECT p FROM Payment p WHERE p.deletedAt IS NOT NULL ORDER BY p.deletedAt DESC")
    List<Payment> findAllDeleted();

    /**
     * Find soft-deleted payments within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.deletedAt BETWEEN :start AND :end ORDER BY p.deletedAt DESC")
    List<Payment> findDeletedBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Count soft-deleted payments
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.deletedAt IS NOT NULL")
    long countDeleted();

    /**
     * Count active (not deleted) payments
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.deletedAt IS NULL")
    long countActive();

    // ============================================================
    // BULK SOFT DELETE OPERATIONS
    // ============================================================

    /**
     * Soft delete payments older than specified date
     */
    @Modifying
    @Query("UPDATE Payment p SET p.deletedAt = :now, p.deletedBy = (SELECT u FROM User u WHERE u.id = :userId) " +
            "WHERE p.createdAt < :beforeDate AND p.deletedAt IS NULL")
    int softDeleteOlderThan(@Param("beforeDate") Instant beforeDate,
                            @Param("userId") Integer userId,
                            @Param("now") Instant now);

    /**
     * Soft delete all REFUNDED payments older than specified date
     */
    @Modifying
    @Query("UPDATE Payment p SET p.deletedAt = :now, p.deletedBy = (SELECT u FROM User u WHERE u.id = :userId) " +
            "WHERE p.status = 'REFUNDED' AND p.createdAt < :beforeDate AND p.deletedAt IS NULL")
    int softDeleteRefundedOlderThan(@Param("beforeDate") Instant beforeDate,
                                    @Param("userId") Integer userId,
                                    @Param("now") Instant now);

    /**
     * Restore soft-deleted payment
     */
    @Modifying
    @Query("UPDATE Payment p SET p.deletedAt = NULL, p.deletedBy = NULL WHERE p.id = :id")
    int restoreById(@Param("id") Long id);

    /**
     * Permanently delete soft-deleted payments older than specified date
     */
    @Modifying
    @Query("DELETE FROM Payment p WHERE p.deletedAt IS NOT NULL AND p.deletedAt < :beforeDate")
    int permanentlyDeleteOlderThan(@Param("beforeDate") Instant beforeDate);
    @Query(value = "SELECT p.* FROM payments p " +
            "JOIN orders o ON p.order_id = o.id " +
            "WHERE o.customer_id = :customerId " +
            "ORDER BY p.created_at DESC",
            nativeQuery = true)
    List<Payment> findAllByCustomerIdIncludingDeleted(@Param("customerId") Integer customerId);

    /**
     * Find only soft-deleted payments by customer ID
     */
    @Query("SELECT p FROM Payment p " +
            "WHERE p.order.customer.id = :customerId " +
            "AND p.deletedAt IS NOT NULL " +
            "ORDER BY p.deletedAt DESC")
    List<Payment> findDeletedByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Find active payments by customer ID (explicit check)
     */
    @Query("SELECT p FROM Payment p " +
            "WHERE p.order.customer.id = :customerId " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findActiveByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Count active payments by customer
     */
    @Query("SELECT COUNT(p) FROM Payment p " +
            "WHERE p.order.customer.id = :customerId " +
            "AND p.deletedAt IS NULL")
    long countActiveByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Count deleted payments by customer
     */
    @Query("SELECT COUNT(p) FROM Payment p " +
            "WHERE p.order.customer.id = :customerId " +
            "AND p.deletedAt IS NOT NULL")
    long countDeletedByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Get total amount paid by customer (active payments only)
     */
    @Query("SELECT SUM(p.amount) FROM Payment p " +
            "WHERE p.order.customer.id = :customerId " +
            "AND p.status = 'SUCCESS' " +
            "AND p.deletedAt IS NULL")
    BigDecimal getTotalAmountByCustomerId(@Param("customerId") Integer customerId);
}