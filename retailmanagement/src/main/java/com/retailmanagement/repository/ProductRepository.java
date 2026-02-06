package com.retailmanagement.repository;

import com.retailmanagement.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(
            value =
                    "SELECT p.* FROM products p " +
                            "INNER JOIN product_categories pc ON p.id = pc.product_id " +
                            "WHERE pc.category_id = :categoryId",
            countQuery =
                    "SELECT count(*) FROM products p " +
                            "INNER JOIN product_categories pc ON p.id = pc.product_id " +
                            "WHERE pc.category_id = :categoryId",
            nativeQuery = true
    )
    Page<Product> findByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);

    // Tìm kiếm Nâng cao: Tên, SKU, Thuộc tính, Lọc nhiều danh mục
    @Query(value = "SELECT DISTINCT p.* FROM products p " +
            "LEFT JOIN product_categories pc ON p.id = pc.product_id " +
            "WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "p.name LIKE :keyword% OR " +
            "p.sku LIKE :keyword% OR " +
            "p.attributes_json LIKE :keyword%) " +
            // Logic: Nếu hasCategory = 0 (false) -> bỏ qua lọc IN. Nếu = 1 (true) -> lọc IN
            "AND (:hasCategory = 0 OR pc.category_id IN (:categoryIds)) " +
            "AND (:isVisible IS NULL OR p.is_visible = :isVisible)",

            countQuery = "SELECT count(DISTINCT p.id) FROM products p " +
                    "LEFT JOIN product_categories pc ON p.id = pc.product_id " +
                    "WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "p.name LIKE :keyword% OR " +
                    "p.sku LIKE :keyword% OR " +
                    "p.attributes_json LIKE :keyword%) " +
                    "AND (:hasCategory = 0 OR pc.category_id IN (:categoryIds)) " +
                    "AND (:isVisible IS NULL OR p.is_visible = :isVisible)",
            nativeQuery = true
    )
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryIds") List<Integer> categoryIds,
                                 @Param("hasCategory") boolean hasCategory,
                                 @Param("isVisible") Boolean isVisible,
                                 Pageable pageable);
}
