package com.retailmanagement.repository;

import com.retailmanagement.entity.ProductSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {

    // Lấy toàn bộ số Seri của 1 Biến thể cụ thể
    List<ProductSerial> findByVariantId(Integer variantId);

    // Đếm số lượng máy thực tế đang ở trong kho của 1 Biến thể
    int countByVariantIdAndStatus(Integer variantId, String status);

    // Xóa toàn bộ Seri thuộc về 1 biến thể (khi xóa biến thể)
    void deleteByVariantId(Integer variantId);
    boolean existsBySerialNumber(String serialNumber);
    List<ProductSerial> findByVariantIdAndStatus(Integer variantId, String status);
}
