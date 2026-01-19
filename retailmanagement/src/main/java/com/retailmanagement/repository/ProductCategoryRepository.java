package com.retailmanagement.repository;

import com.retailmanagement.entity.ProductCategory;
import com.retailmanagement.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
    Optional<ProductCategory>
    findFirstById_ProductIdAndIsPrimaryTrue(Integer productId);
}