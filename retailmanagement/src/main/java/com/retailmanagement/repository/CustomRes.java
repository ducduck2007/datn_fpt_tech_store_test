package com.retailmanagement.repository;

import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.CustomerType;
import com.retailmanagement.entity.VipTier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query(value = "SELECT * FROM customers WHERE is_active = 1 " +
            "AND MONTH(date_of_birth) = MONTH(GETDATE()) " +
            "AND DAY(date_of_birth) = DAY(GETDATE())",
            nativeQuery = true)
    List<Customer> findCustomersWithBirthdayToday();

    /**
     * Tìm khách hàng có sinh nhật trong tháng cụ thể
     */
    @Query(value = "SELECT * FROM customers WHERE is_active = 1 " +
            "AND MONTH(date_of_birth) = :month " +
            "ORDER BY DAY(date_of_birth)",
            nativeQuery = true)
    List<Customer> findCustomersWithBirthdayInMonth(@Param("month") int month);

    /**
     * Tìm khách hàng có sinh nhật trong khoảng thời gian
     */
    @Query(value = "SELECT * FROM customers WHERE is_active = 1 " +
            "AND ((MONTH(date_of_birth) = :startMonth AND DAY(date_of_birth) >= :startDay) " +
            "     OR (MONTH(date_of_birth) = :endMonth AND DAY(date_of_birth) <= :endDay) " +
            "     OR (MONTH(date_of_birth) > :startMonth AND MONTH(date_of_birth) < :endMonth)) " +
            "ORDER BY MONTH(date_of_birth), DAY(date_of_birth)",
            nativeQuery = true)
    List<Customer> findCustomersWithBirthdayBetween(
            @Param("startMonth") int startMonth,
            @Param("startDay") int startDay,
            @Param("endMonth") int endMonth,
            @Param("endDay") int endDay
    );

    /**
     * Đếm số khách hàng có sinh nhật trong tháng
     */
    @Query(value = "SELECT COUNT(*) FROM customers WHERE is_active = 1 " +
            "AND MONTH(date_of_birth) = :month",
            nativeQuery = true)
    long countCustomersWithBirthdayInMonth(@Param("month") int month);


    @Query("SELECT c FROM Customer c WHERE c.isActive = true " +
            "AND c.loyaltyPoints >= :minPoints " +
            "AND c.loyaltyPoints <= :maxPoints " +
            "ORDER BY c.loyaltyPoints DESC")
    List<Customer> findByLoyaltyPointsBetween(
            @Param("minPoints") int minPoints,
            @Param("maxPoints") int maxPoints
    );
    @Query("SELECT c FROM Customer c WHERE c.isActive = true " +
            "AND c.vipTier = :vipTier " +
            "ORDER BY c.loyaltyPoints DESC")
    List<Customer> findByVipTier(@Param("vipTier") VipTier vipTier);

    @Query("SELECT c FROM Customer c WHERE c.isActive = true " +
            "AND c.vipTier = :vipTier " +
            "AND c.loyaltyPoints >= :minPoints " +
            "AND c.loyaltyPoints <= :maxPoints " +
            "ORDER BY c.loyaltyPoints DESC")
    List<Customer> findByVipTierAndLoyaltyPointsBetween(
            @Param("vipTier") VipTier vipTier,
            @Param("minPoints") int minPoints,
            @Param("maxPoints") int maxPoints
    );
    // Add these methods to CustomRes.java interface

    /**
     * Tìm khách hàng theo khoảng chi tiêu
     */
    @Query("SELECT c FROM Customer c WHERE c.isActive = true " +
            "AND c.totalSpent >= :minSpent " +
            "AND c.totalSpent <= :maxSpent " +
            "ORDER BY c.totalSpent DESC")
    List<Customer> findByTotalSpentBetween(
            @Param("minSpent") BigDecimal minSpent,
            @Param("maxSpent") BigDecimal maxSpent
    );

    /**
     * Tìm top N khách hàng theo tổng chi tiêu
     */
    @Query("SELECT c FROM Customer c WHERE c.isActive = true " +
            "ORDER BY c.totalSpent DESC")
    List<Customer> findTopByTotalSpent(Pageable pageable);

    /**
     * Tìm top N khách hàng theo VIP tier và chi tiêu
     */
    @Query("SELECT c FROM Customer c WHERE c.isActive = true " +
            "AND c.vipTier = :vipTier " +
            "ORDER BY c.totalSpent DESC")
    List<Customer> findTopByVipTierAndTotalSpent(
            @Param("vipTier") VipTier vipTier,
            Pageable pageable
    );

    /**
     * Lấy tổng chi tiêu của tất cả khách hàng
     */
    @Query("SELECT SUM(c.totalSpent) FROM Customer c WHERE c.isActive = true")
    BigDecimal getTotalSpentAllCustomers();

    /**
     * Lấy tổng chi tiêu theo VIP tier
     */
    @Query("SELECT SUM(c.totalSpent) FROM Customer c WHERE c.isActive = true " +
            "AND c.vipTier = :vipTier")
    BigDecimal getTotalSpentByVipTier(@Param("vipTier") VipTier vipTier);

    /**
     * Đếm khách hàng theo khoảng chi tiêu
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.isActive = true " +
            "AND c.totalSpent >= :minSpent " +
            "AND c.totalSpent <= :maxSpent")
    long countByTotalSpentBetween(
            @Param("minSpent") BigDecimal minSpent,
            @Param("maxSpent") BigDecimal maxSpent
    );

}
