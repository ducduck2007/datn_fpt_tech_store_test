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
    @Query(value = """
    SELECT p.*, 
           (SELECT MIN(pv.price) FROM product_variants pv 
            WHERE pv.product_id = p.id AND pv.is_active = 1) as min_price
    FROM products p 
    WHERE 
    (:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword% OR p.sku LIKE %:keyword%) 
    
    AND (:hasCategory = 0 OR EXISTS (
            SELECT 1 FROM product_categories pc 
            WHERE pc.product_id = p.id AND pc.category_id IN (:categoryIds)
        )) 
            
    AND (:isVisible IS NULL OR p.is_visible = :isVisible)
    
    AND (:tagId = -1 OR EXISTS (
            SELECT 1 FROM product_tags pt 
            WHERE pt.product_id = p.id AND pt.tag_id = :tagId
        ))
            
    AND (:inStockOnly = 0 OR (
            SELECT COALESCE(SUM(v.stock_quantity), 0) 
            FROM product_variants v 
            WHERE v.product_id = p.id AND v.is_active = 1
        ) > 0)
    """,
            countQuery = """
    SELECT count(*) FROM products p 
    WHERE 
    (:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword% OR p.sku LIKE %:keyword%) 
    AND (:hasCategory = 0 OR EXISTS (
            SELECT 1 FROM product_categories pc 
            WHERE pc.product_id = p.id AND pc.category_id IN (:categoryIds)
        )) 
    AND (:isVisible IS NULL OR p.is_visible = :isVisible)
    AND (:tagId = -1 OR EXISTS (
            SELECT 1 FROM product_tags pt 
            WHERE pt.product_id = p.id AND pt.tag_id = :tagId
        ))
    AND (:inStockOnly = 0 OR (
            SELECT COALESCE(SUM(v.stock_quantity), 0) 
            FROM product_variants v 
            WHERE v.product_id = p.id AND v.is_active = 1
        ) > 0)
    """,
            nativeQuery = true
    )
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryIds") List<Integer> categoryIds,
                                 @Param("hasCategory") boolean hasCategory,
                                 @Param("isVisible") Boolean isVisible,
                                 @Param("inStockOnly") boolean inStockOnly,
                                 @Param("tagId") Integer tagId,
                                 Pageable pageable);

    // Tìm danh sách sản phẩm trong thùng rác (đã xóa mềm)
    Page<Product> findByIsVisibleFalse(Pageable pageable);
}
