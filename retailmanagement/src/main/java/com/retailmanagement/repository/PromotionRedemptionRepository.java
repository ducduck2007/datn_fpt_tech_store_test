package com.retailmanagement.repository;

import com.retailmanagement.entity.PromotionRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionRedemptionRepository extends JpaRepository<PromotionRedemption, Long> {
    Optional<PromotionRedemption> findByPromotionId(Integer promotionId);
}
