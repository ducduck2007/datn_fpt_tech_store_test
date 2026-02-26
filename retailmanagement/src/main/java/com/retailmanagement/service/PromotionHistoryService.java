package com.retailmanagement.service;

import com.retailmanagement.dto.response.PromotionHistoryResponse;
import com.retailmanagement.entity.Order;
import com.retailmanagement.entity.Promotion;
import com.retailmanagement.entity.SpinWheelHistory;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.PromotionHistoryRepository;
import com.retailmanagement.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionHistoryService {

    private final PromotionHistoryRepository promotionHistoryRepository;
    private final PromotionRepository promotionRepository;
    private final CustomRes customerRepository;

    /**
     * Lấy toàn bộ lịch sử ưu đãi của một khách hàng,
     * bao gồm: mã khuyến mãi đã dùng trong đơn + lịch sử spin wheel.
     * Kết quả sắp xếp theo thời gian mới nhất trước.
     */
    public List<PromotionHistoryResponse> getPromotionHistory(Integer customerId) {
        // Kiểm tra khách tồn tại
        customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + customerId));

        List<PromotionHistoryResponse> result = new ArrayList<>();

        // ── 1. Lịch sử mã khuyến mãi từ đơn hàng ───────────────────
        List<Order> orders = promotionHistoryRepository.findOrdersWithPromotion(customerId);
        for (Order order : orders) {
            String code = order.getAppliedPromotionCode();

            // Tìm thông tin promotion theo code
            Promotion promotion = promotionRepository.findByCode(code).orElse(null);

            LocalDateTime usedAt = order.getCreatedAt() != null
                    ? order.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : null;

            result.add(PromotionHistoryResponse.builder()
                    .type("PROMOTION_CODE")
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .usedAt(usedAt)
                    .promotionCode(code)
                    .promotionName(promotion != null ? promotion.getName() : code)
                    .discountType(promotion != null ? promotion.getDiscountType() : "UNKNOWN")
                    .discountValue(promotion != null ? promotion.getDiscountValue() : null)
                    .discountTotal(order.getDiscountTotal())
                    .status("Đã sử dụng")
                    .expiresAt(promotion != null ? promotion.getEndDate() : null)
                    .build());
        }

        // ── 2. Lịch sử spin wheel ────────────────────────────────────
        List<SpinWheelHistory> spinHistories = promotionHistoryRepository.findSpinHistoryByCustomer(customerId);
        for (SpinWheelHistory spin : spinHistories) {
            String status;
            if (spin.getIsUsed()) {
                status = "Đã sử dụng";
            } else if (spin.getExpiresAt().isBefore(LocalDateTime.now())) {
                status = "Đã hết hạn";
            } else {
                status = "Đang hoạt động";
            }

            result.add(PromotionHistoryResponse.builder()
                    .type("SPIN_WHEEL")
                    .orderId(spin.getUsedOrderId())
                    .orderNumber(null) // không join order ở đây để tránh N+1
                    .usedAt(spin.getIsUsed() ? spin.getSpunAt() : null)
                    .promotionCode("SPIN_WHEEL")
                    .promotionName("Vòng quay may mắn")
                    .discountType("PERCENT")
                    .discountValue(spin.getDiscountBonus())
                    .discountTotal(null) // không biết số tiền vì có thể chưa dùng
                    .status(status)
                    .expiresAt(spin.getExpiresAt())
                    .build());
        }

        // ── 3. Sắp xếp theo thời gian mới nhất ──────────────────────
        result.sort(Comparator.comparing(
                r -> r.getUsedAt() != null ? r.getUsedAt() : LocalDateTime.MIN,
                Comparator.reverseOrder()
        ));

        return result;
    }
}