package com.retailmanagement.repository;

import com.retailmanagement.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByVariant_IdOrderByEffectiveFromDesc(Integer variantId);
    Optional<PriceHistory> findFirstByVariant_IdAndEffectiveToIsNullOrderByEffectiveFromDesc(Integer variantId);
}
