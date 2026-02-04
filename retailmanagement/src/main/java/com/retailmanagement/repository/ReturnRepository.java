package com.retailmanagement.repository;

import com.retailmanagement.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    List<Return> findByStatusOrderByCreatedAtDesc(String status);
    List<Return> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<Return> findByOrder_Customer_IdOrderByCreatedAtDesc(Integer customerId);
}