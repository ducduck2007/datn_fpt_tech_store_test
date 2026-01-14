package com.retailmanagement.repository;

import com.retailmanagement.entity.ProductCategory;
import com.retailmanagement.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
}