package com.retailmanagement.repository;

import com.retailmanagement.entity.LoyaltyLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LoyaltyLedgerRepository extends JpaRepository<LoyaltyLedger, Long> {

    /**
     * Find all loyalty ledger entries for a specific customer
     */
    List<LoyaltyLedger> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    /**
     * Find loyalty ledger entries by reference (e.g., order or return)
     */
    List<LoyaltyLedger> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    /**
     * Find entries within a date range for reporting
     */
    @Query("SELECT l FROM LoyaltyLedger l WHERE l.customer.id = :customerId " +
            "AND l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    List<LoyaltyLedger> findByCustomerAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Calculate total points earned/spent by reason
     */
    @Query("SELECT SUM(l.pointsDelta) FROM LoyaltyLedger l " +
            "WHERE l.customer.id = :customerId AND l.reason = :reason")
    Integer sumPointsByCustomerAndReason(
            @Param("customerId") Integer customerId,
            @Param("reason") String reason
    );
}