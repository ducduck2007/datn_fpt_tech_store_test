package com.retailmanagement.repository;

import com.retailmanagement.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query(value = "SELECT p.* FROM products p " +
            "INNER JOIN product_categories pc ON p.id = pc.product_id " +
            "WHERE pc.category_id = :categoryId",
            countQuery = "SELECT count(*) FROM products p " +
                    "INNER JOIN product_categories pc ON p.id = pc.product_id " +
                    "WHERE pc.category_id = :categoryId",
            nativeQuery = true)
    Page<Product> findByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);
}