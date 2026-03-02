package com.retailmanagement.service;

import com.retailmanagement.audit.Audit;
import com.retailmanagement.audit.AuditAction;
import com.retailmanagement.audit.AuditModule;
import com.retailmanagement.audit.TargetType;
import com.retailmanagement.constants.OrderStatuses;
import com.retailmanagement.dto.request.CreateOrderItemRequest;
import com.retailmanagement.dto.request.CreateOrderRequest;
import com.retailmanagement.dto.request.UpdateOrderRequest;
import com.retailmanagement.dto.response.CreateOrderResponse;
import com.retailmanagement.dto.response.OrderDetailResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    // ================================================================
    // INNER CLASS: Discount Breakdown
    // ================================================================
    @lombok.Data
    private static class DiscountCalculation {
        private BigDecimal vipDiscountRate      = BigDecimal.ZERO;
        private BigDecimal vipDiscount          = BigDecimal.ZERO;
        private BigDecimal spinDiscountRate     = BigDecimal.ZERO;
        private BigDecimal spinDiscount         = BigDecimal.ZERO;
        // ✅ THÊM MỚI: promotion code discount
        private BigDecimal promoDiscount        = BigDecimal.ZERO;
        private String     promoCode            = null;
        private Integer    promotionId          = null;

        private BigDecimal totalDiscount        = BigDecimal.ZERO;
        private boolean    hasSpinBonus         = false;
        private String     discountType         = "NONE";
    }

    private final OrderRepository            orderRepository;
    private final OrderItemRepository        orderItemRepository;
    private final ProductVariantRepository   variantRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CustomRes                  customerRepository;
    private final UserRepository             userRepository;
    private final CustomerService            customerService;
    private final SpinWheelService           spinWheelService;
    private final EmailService               emailService;
    private final OrderEmailService          orderEmailService;
    private final LoyaltyResetService loyaltyResetService;
    // ✅ THÊM MỚI
    private final PromotionRepository        promotionRepository;
    private final PromotionService           promotionService;

    // ================================================================
    // ORDER NUMBER GENERATOR
    // ================================================================
    private String generateOrderNumber() {
        LocalDate today = LocalDate.now();
        Instant start = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        long countToday = orderRepository.countByCreatedAtBetween(start, end);
        return "ORD-" + today.format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%06d", countToday + 1);
    }

    // ================================================================
    // DISCOUNT CALCULATION — VIP + Spin (giữ nguyên logic cũ)
    // ================================================================
    private DiscountCalculation calculateDiscount(Customer customer, BigDecimal subtotal) {
        DiscountCalculation result = new DiscountCalculation();
        System.out.println("🔍 DEBUG calculateDiscount:");
        System.out.println("   customerId = " + customer.getId());
        System.out.println("   spinDiscountBonus = " + customer.getSpinDiscountBonus());
        System.out.println("   vipTier = " + customer.getVipTier());
        System.out.println("   subtotal = " + subtotal);

        // ── 1. VIP discount ──────────────────────────────────────────
        BigDecimal vipDiscount  = BigDecimal.ZERO;
        BigDecimal vipRate      = BigDecimal.ZERO;
        String     discountType = "NONE";

        VipTier tier = customer.getVipTier();
        if (tier != null) {
            BigDecimal minOrder = tier.getMinOrderToApply();
            if (subtotal.compareTo(minOrder) >= 0) {
                if (tier.isPercentageDiscount()) {
                    vipRate     = BigDecimal.valueOf(tier.getDiscountRate() * 100);
                    vipDiscount = subtotal
                            .multiply(BigDecimal.valueOf(tier.getDiscountRate()))
                            .setScale(0, RoundingMode.HALF_UP);
                    discountType = "PERCENTAGE";
                } else {
                    vipDiscount  = tier.getDiscountAmount();
                    discountType = "FIXED";
                }
            }
        }

        // ── 2. Spin Wheel discount ────────────────────────────────────
        BigDecimal spinDiscount = BigDecimal.ZERO;
        BigDecimal spinRate = BigDecimal.ZERO;
        boolean hasSpinBonus = false;

        if (customer.getSpinDiscountBonus() != null
                && customer.getSpinDiscountBonus().compareTo(BigDecimal.ZERO) > 0) {
            spinRate     = customer.getSpinDiscountBonus();
            spinDiscount = subtotal
                    .multiply(spinRate.divide(BigDecimal.valueOf(100)))
                    .setScale(0, RoundingMode.HALF_UP);
            hasSpinBonus = true;
        }

        result.setVipDiscountRate(vipRate);
        result.setVipDiscount(vipDiscount);
        result.setSpinDiscountRate(spinRate);
        result.setSpinDiscount(spinDiscount);
        // totalDiscount sẽ được cộng thêm promoDiscount bên ngoài
        result.setTotalDiscount(vipDiscount.add(spinDiscount));
        result.setHasSpinBonus(hasSpinBonus);
        result.setDiscountType(discountType);

        return result;
    }

    // ================================================================
    // ✅ THÊM MỚI: VALIDATE & APPLY PROMOTION CODE
    //
    // Trả về số tiền được giảm (0 nếu không hợp lệ).
    // Ném RuntimeException nếu mã không tồn tại / hết hạn / không đủ đk.
    // ================================================================
    private BigDecimal applyPromotionCode(
            String promoCode,
            Customer customer,
            BigDecimal subtotal,
            DiscountCalculation discountResult
    ) {
        if (promoCode == null || promoCode.isBlank()) return BigDecimal.ZERO;

        String code = promoCode.trim().toUpperCase();

        // 1. Tìm promotion trong DB
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá '" + code + "' không tồn tại"));

        // 2. Kiểm tra còn hiệu lực
        LocalDateTime now = LocalDateTime.now();
        if (!promotion.getIsActive()
                || promotion.getStartDate().isAfter(now)
                || promotion.getEndDate().isBefore(now)) {
            throw new RuntimeException("Mã giảm giá '" + code + "' đã hết hạn hoặc chưa có hiệu lực");
        }

        // 3. Kiểm tra đơn tối thiểu (minOrderAmount)
        if (promotion.getMinOrderAmount() != null
                && subtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new RuntimeException(String.format(
                    "Mã '%s' yêu cầu đơn hàng tối thiểu %s (đơn hiện tại: %s)",
                    code,
                    formatMoney(promotion.getMinOrderAmount()),
                    formatMoney(subtotal)
            ));
        }

        // 4. Kiểm tra usage limit
        if (!promotionService.isUsableByLimit(promotion)) {
            throw new RuntimeException("Mã giảm giá '" + code + "' đã đạt giới hạn sử dụng");
        }

        // 5. Tính tiền giảm
        BigDecimal promoDiscount = promotionService.applyDiscount(
                subtotal,
                promotion.getDiscountType(),
                promotion.getDiscountValue()
        );
        // Số tiền giảm không được vượt quá subtotal
        if (promoDiscount.compareTo(subtotal) > 0) {
            promoDiscount = subtotal;
        }

        // 6. Lưu vào discountResult để dùng sau
        discountResult.setPromoDiscount(promoDiscount);
        discountResult.setPromoCode(code);
        discountResult.setPromotionId(promotion.getId());

        return promoDiscount;
    }

    // ================================================================
    // CREATE ORDER
    // ================================================================
    @Audit(module = AuditModule.ORDER, action = AuditAction.CREATE, targetType = TargetType.ORDER)
    public CreateOrderResponse createOrder(CreateOrderRequest request, Integer userId) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        System.out.println("🔍 Customer fetched: id=" + customer.getId()
                + ", spinBonus=" + customer.getSpinDiscountBonus());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ── Init order ──────────────────────────────────────────────
        Order order = new Order();
        order.setCustomer(customer);
        order.setUser(user);
        order.setChannel(request.getChannel());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatuses.PENDING);
        order.setPaymentStatus("UNPAID");
        order.setNotes(request.getNotes());
        order.setSubtotal(BigDecimal.ZERO);
        order.setDiscountTotal(BigDecimal.ZERO);
        order.setTaxTotal(BigDecimal.ZERO);
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        order = orderRepository.save(order);


        // ── Pass 1: tính subtotal ────────────────────────────────────
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            subtotal = subtotal.add(
                    variant.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()))
            );
        }

        // ── Tính VIP + Spin discount ─────────────────────────────────
        DiscountCalculation discountCalc = calculateDiscount(customer, subtotal);

        // ── ✅ THÊM MỚI: Áp promotion code (nếu có) ─────────────────
        // validate + tính promoDiscount, ghi vào discountCalc
        String promoCode = request.getPromotionCode(); // cần thêm field này vào CreateOrderRequest
        if (promoCode != null && !promoCode.isBlank()) {
            BigDecimal promoDiscount = applyPromotionCode(promoCode, customer, subtotal, discountCalc);
            // Cộng vào tổng discount
            discountCalc.setTotalDiscount(
                    discountCalc.getTotalDiscount().add(promoDiscount)
            );
            // Lưu mã vào order để truy vết
            order.setAppliedPromotionCode(discountCalc.getPromoCode());
        }

        // ── Tỷ lệ discount phân bổ về từng dòng ─────────────────────
        BigDecimal totalDiscountRate = BigDecimal.ZERO;
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            totalDiscountRate = discountCalc.getTotalDiscount()
                    .divide(subtotal, 4, RoundingMode.HALF_UP);
        }

        // ── Pass 2: tạo order items ──────────────────────────────────
        List<CreateOrderResponse.Item> responseItems = new ArrayList<>();

        for (CreateOrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            int available = variant.getStockQuantity() - variant.getReservedQty();
            if (available < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough stock for " + variant.getVariantName());
            }

            BigDecimal lineSubtotal = variant.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            BigDecimal lineDiscount = lineSubtotal
                    .multiply(totalDiscountRate)
                    .setScale(0, RoundingMode.HALF_UP);
            BigDecimal lineTotal = lineSubtotal.subtract(lineDiscount);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setProduct(variant.getProduct());
            item.setProductName(variant.getProduct().getName());
            item.setVariantName(variant.getVariantName());
            item.setSku(variant.getSku());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(variant.getPrice());
            item.setDiscount(lineDiscount);
            item.setLineTotal(lineTotal);
            item.setCreatedAt(Instant.now());
            orderItemRepository.save(item);

            variant.setReservedQty(variant.getReservedQty() + itemReq.getQuantity());
            variantRepository.save(variant);

            StockTransaction st = new StockTransaction();
            st.setVariant(variant);
            st.setQuantity(itemReq.getQuantity());
            st.setType("RESERVE");
            st.setReferenceType("orders");
            st.setReferenceId(order.getId());
            st.setCreatedBy(user);
            st.setCreatedAt(Instant.now());
            stockTransactionRepository.save(st);

            responseItems.add(new CreateOrderResponse.Item(
                    item.getProduct().getId(),
                    item.getVariant().getId(),
                    item.getProductName(),
                    item.getVariantName(),
                    item.getSku(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getDiscount(),
                    item.getLineTotal()
            ));
        }

        // ── Cập nhật tổng đơn ────────────────────────────────────────
        order.setSubtotal(subtotal);
        order.setDiscountTotal(discountCalc.getTotalDiscount());
        order.setTotalAmount(
                subtotal.subtract(discountCalc.getTotalDiscount()).add(order.getShippingFee())
        );
        order.setSpinDiscountRate(discountCalc.getSpinDiscountRate());
        order.setSpinDiscount(discountCalc.getSpinDiscount());

        // ── Ghi chú chiết khấu ───────────────────────────────────────
        StringBuilder notes = new StringBuilder();
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            notes.append(request.getNotes()).append(" | ");
        }

        VipTier tier = customer.getVipTier();
        if (tier != null && discountCalc.getVipDiscount().compareTo(BigDecimal.ZERO) > 0) {
            if ("FIXED".equals(discountCalc.getDiscountType())) {
                notes.append("VIP ").append(tier.getDisplayName())
                        .append(": -").append(formatMoney(discountCalc.getVipDiscount()))
                        .append(" (đơn từ ").append(formatMoney(tier.getMinOrderToApply())).append(")");
            } else {
                notes.append("VIP ").append(tier.getDisplayName())
                        .append(": -").append(String.format("%.0f%%", discountCalc.getVipDiscountRate()))
                        .append(" (-").append(formatMoney(discountCalc.getVipDiscount())).append(")");
            }
        } else if (tier != null) {
            notes.append("VIP ").append(tier.getDisplayName())
                    .append(": không áp dụng (đơn < ").append(formatMoney(tier.getMinOrderToApply())).append(")");
        }

        if (discountCalc.isHasSpinBonus()) {
            if (notes.length() > 0) notes.append(" | ");
            notes.append("Spin: -").append(String.format("%.0f%%", discountCalc.getSpinDiscountRate()))
                    .append(" (-").append(formatMoney(discountCalc.getSpinDiscount())).append(")");
        }

        // ✅ THÊM MỚI: ghi chú promotion code
        if (discountCalc.getPromoCode() != null) {
            if (notes.length() > 0) notes.append(" | ");
            notes.append("Mã: ").append(discountCalc.getPromoCode())
                    .append(": -").append(formatMoney(discountCalc.getPromoDiscount()));
        }

        order.setNotes(notes.toString());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        // ── Đánh dấu spin bonus đã dùng ─────────────────────────────
        if (discountCalc.isHasSpinBonus()) {
            spinWheelService.useBonus(customer.getId(), order.getId());
        }

        // ── ✅ THÊM MỚI: Ghi nhận promotion đã được sử dụng ─────────
        if (discountCalc.getPromotionId() != null) {
            promotionService.recordRedemption(discountCalc.getPromotionId(), 1L);
        }

        // ── Build response ───────────────────────────────────────────
        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setCustomerId(customer.getId());
        response.setCustomerName(customer.getName());
        response.setStaffId(user.getId());
        response.setStaffUsername(user.getUsername());
        response.setSubtotal(order.getSubtotal());
        response.setDiscountTotal(order.getDiscountTotal());
        response.setTaxTotal(order.getTaxTotal());
        response.setShippingFee(order.getShippingFee());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(responseItems);
        response.setVipDiscountRate(discountCalc.getVipDiscountRate());
        response.setVipDiscount(discountCalc.getVipDiscount());
        response.setSpinDiscountRate(discountCalc.getSpinDiscountRate());
        response.setSpinDiscount(discountCalc.getSpinDiscount());
        response.setHasSpinBonus(discountCalc.isHasSpinBonus());
        // ✅ THÊM MỚI vào response (cần thêm field vào CreateOrderResponse)
        response.setPromoCode(discountCalc.getPromoCode());
        response.setPromoDiscount(discountCalc.getPromoDiscount());

        emailService.sendOrderCreatedEmail(order);

        return response;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        // Dùng Locale.US đ ể đảm bảo luôn dùng dấu phẩy, không phụ thuộc JVM locale
        return String.format(java.util.Locale.US, "%,d", amount.longValue());
    }
    // ================================================================
    // UPDATE ORDER — GIỮ NGUYÊN
    // ================================================================
    @Audit(module = AuditModule.ORDER, action = AuditAction.UPDATE, targetType = TargetType.ORDER)
    public void updateOrder(Long orderId, UpdateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!OrderStatuses.PENDING.equals(order.getStatus())) {
            throw new RuntimeException("Chỉ được chỉnh sửa đơn PENDING");
        }
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    // ================================================================
    // CANCEL ORDER — GIỮ NGUYÊN
    // ================================================================
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (OrderStatuses.CANCELLED.equals(order.getStatus()))
            throw new IllegalStateException("Order already cancelled");
        if (OrderStatuses.DELIVERED.equals(order.getStatus()))
            throw new IllegalStateException("Delivered order cannot be cancelled");

        order.getOrderItems().forEach(item -> {
            ProductVariant variant = variantRepository.findById(item.getVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + item.getVariant().getId()));
            variant.setReservedQty(Math.max(0, variant.getReservedQty() - item.getQuantity()));
            variantRepository.save(variant);
        });

        order.setStatus(OrderStatuses.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setNotes(reason);

        restoreStock(order);

        if ("PAID".equals(order.getPaymentStatus()) && order.getCustomer() != null) {
            customerService.deductLoyaltyPoints(
                    order.getCustomer().getId(),
                    order.getTotalAmount(),
                    "CANCEL_ORDER",
                    "orders",
                    orderId
            );
        }

        orderRepository.save(order);
    }

    // ================================================================
    // DELETE ORDER — GIỮ NGUYÊN
    // ================================================================
    @Audit(module = AuditModule.ORDER, action = AuditAction.DELETE, targetType = TargetType.ORDER)
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!OrderStatuses.PENDING.equals(order.getStatus()))
            throw new RuntimeException("Chỉ được xóa đơn PENDING");
        orderItemRepository.deleteAll(order.getOrderItems());
        orderRepository.delete(order);
    }

    // ================================================================
    // GET ORDER DETAIL — GIỮ NGUYÊN
    // ================================================================
    public OrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<CreateOrderResponse.Item> items = order.getOrderItems().stream()
                .map(i -> new CreateOrderResponse.Item(
                        i.getProduct().getId(),
                        i.getVariant().getId(),
                        i.getProductName(),
                        i.getVariantName(),
                        i.getSku(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getDiscount(),
                        i.getLineTotal()
                )).toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getChannel(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getUser().getId(),
                order.getUser().getUsername(),
                order.getNotes(),
                order.getSubtotal(),
                order.getDiscountTotal(),
                order.getTaxTotal(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }

    // ================================================================
    // STATUS TRANSITIONS — GIỮ NGUYÊN
    // ================================================================
    @Transactional
    public void markAsProcessing(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ✅ chỉ cho phép PAID -> PROCESSING
        if (!OrderStatuses.PAID.equals(order.getStatus())) {
            throw new IllegalStateException("Only PAID orders can be processed");
        }

        order.setStatus(OrderStatuses.PROCESSING);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
    }

    @Transactional
    public void markAsShipping(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        // ✅ chỉ cho phép PROCESSING -> SHIPPING
        if (!OrderStatuses.PROCESSING.equals(order.getStatus())) {
            throw new IllegalStateException("Only PAID orders can be processed");
        }
        order.setStatus(OrderStatuses.SHIPPING);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    @Transactional
    public void markAsDelivered(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getDeliveredAt() != null) {
            return;
        }

        if (!OrderStatuses.SHIPPING.equals(order.getStatus()))
            throw new IllegalStateException("Only SHIPPING orders can be delivered");

        // ===== 1. Convert RESERVED -> SOLD =====
        for (OrderItem item : order.getOrderItems()) {

            ProductVariant variant = variantRepository.findById(
                    item.getVariant().getId()
            ).orElseThrow();

            int newReserved =
                    Math.max(0, variant.getReservedQty() - item.getQuantity());

            int newStock =
                    Math.max(0, variant.getStockQuantity() - item.getQuantity());

            variant.setReservedQty(newReserved);
            variant.setStockQuantity(newStock);

            variantRepository.save(variant);

            // stock transaction
            StockTransaction tx = new StockTransaction();
            tx.setVariant(variant);
            tx.setQuantity(-item.getQuantity());
            tx.setType("STOCK_OUT");
            tx.setReferenceType("orders");
            tx.setReferenceId(order.getId());
            tx.setCreatedAt(Instant.now());
            tx.setCreatedBy(order.getUser());

            stockTransactionRepository.save(tx);
        }

        // ===== 2. Update order =====
        order.setStatus(OrderStatuses.DELIVERED);
        order.setDeliveredAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);

        // ===== 3. Loyalty earn (Revenue finalized) =====
        if (order.getCustomer() != null &&
                "PAID".equals(order.getPaymentStatus())) {

            customerService.addLoyaltyPoints(
                    order.getCustomer().getId(),
                    order.getTotalAmount()
            );
        }
        orderEmailService.sendDeliveredEmail(order);
    }

    // ================================================================
    // HELPERS — GIỮ NGUYÊN
    // ================================================================
    private void recalcOrderTotal(Order order) {
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal
                .subtract(order.getDiscountTotal())
                .add(order.getTaxTotal())
                .add(order.getShippingFee()));
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            StockTransaction tx = new StockTransaction();
            tx.setVariant(item.getVariant());
            tx.setQuantity(item.getQuantity());
            tx.setType("IN");
            tx.setNote("CANCEL_ORDER");
            tx.setCreatedAt(Instant.now());
            stockTransactionRepository.save(tx);
        }
    }
}