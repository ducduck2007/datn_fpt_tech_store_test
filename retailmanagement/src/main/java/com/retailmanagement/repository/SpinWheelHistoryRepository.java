package com.retailmanagement.repository;

import com.retailmanagement.entity.SpinWheelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpinWheelHistoryRepository extends JpaRepository<SpinWheelHistory, Long> {

    /**
     * Check if customer has already spun this week
     */
    Optional<SpinWheelHistory> findByCustomerIdAndWeekStartDate(Integer customerId, LocalDate weekStartDate);

    /**
     * Get customer's spin history
     */
    List<SpinWheelHistory> findByCustomerIdOrderBySpunAtDesc(Integer customerId);

    /**
     * Get active (unused and not expired) bonuses for a customer
     */
    @Query("SELECT s FROM SpinWheelHistory s WHERE s.customer.id = :customerId " +
            "AND s.isUsed = false AND s.expiresAt > :now ORDER BY s.spunAt DESC")
    List<SpinWheelHistory> findActiveBonus(@Param("customerId") Integer customerId,
                                           @Param("now") LocalDateTime now);

    /**
     * Get most recent active bonus
     */
    @Query("SELECT s FROM SpinWheelHistory s WHERE s.customer.id = :customerId " +
            "AND s.isUsed = false AND s.expiresAt > :now ORDER BY s.spunAt DESC")
    Optional<SpinWheelHistory> findMostRecentActiveBonus(@Param("customerId") Integer customerId,
                                                         @Param("now") LocalDateTime now);

    /**
     * Count spins in current week
     */
    @Query("SELECT COUNT(s) FROM SpinWheelHistory s WHERE s.customer.id = :customerId " +
            "AND s.weekStartDate = :weekStart")
    long countByCustomerAndWeek(@Param("customerId") Integer customerId,
                                @Param("weekStart") LocalDate weekStart);
}