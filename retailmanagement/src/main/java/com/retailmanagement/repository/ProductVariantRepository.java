package com.retailmanagement.repository;

import com.retailmanagement.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    List<ProductVariant> findByProduct_Id(Integer productId);

    // Thay thế findAll() trong getAllConflictsBelowCost() và getDashboard()
    // Chỉ lấy các variant có giá bán thực sự thấp hơn giá nhập
    @Query("SELECT v FROM ProductVariant v WHERE v.costPrice IS NOT NULL AND v.price IS NOT NULL AND v.price < v.costPrice")
    List<ProductVariant> findVariantsBelowCost();
    // Đếm số lượng biến thể có tồn kho = 0
    long countByStockQuantityLessThanEqual(Integer stockQuantity);
}