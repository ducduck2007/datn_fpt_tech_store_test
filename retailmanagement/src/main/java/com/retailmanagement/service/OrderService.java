package com.retailmanagement.service;

import com.retailmanagement.constants.OrderStatuses;
import com.retailmanagement.audit.Audit;
import com.retailmanagement.audit.AuditAction;
import com.retailmanagement.audit.AuditModule;
import com.retailmanagement.audit.TargetType;
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
import java.time.*;
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
        private BigDecimal vipDiscountRate   = BigDecimal.ZERO;  // % (chỉ cho Platinum/Diamond)
        private BigDecimal vipDiscount       = BigDecimal.ZERO;  // Số tiền giảm VIP
        private BigDecimal spinDiscountRate  = BigDecimal.ZERO;  // % spin
        private BigDecimal spinDiscount      = BigDecimal.ZERO;  // Số tiền giảm spin
        private BigDecimal totalDiscount     = BigDecimal.ZERO;
        private boolean    hasSpinBonus      = false;
        private String     discountType      = "NONE";           // "FIXED" | "PERCENTAGE" | "NONE"
    }

    private final OrderRepository           orderRepository;
    private final OrderItemRepository       orderItemRepository;
    private final ProductVariantRepository  variantRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CustomRes                 customerRepository;
    private final UserRepository            userRepository;
    private final CustomerService           customerService;
    private final SpinWheelService          spinWheelService;

    // ================================================================
    // ORDER NUMBER GENERATOR
    // ================================================================
    private String generateOrderNumber() {
        LocalDate today = LocalDate.now();
        Instant start = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end   = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        long countToday = orderRepository.countByCreatedAtBetween(start, end);
        return "ORD-" + today.format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%06d", countToday + 1);
    }

    // ================================================================
    // DISCOUNT CALCULATION
    //
    // Luật mới:
    //   BRONZE / SILVER / GOLD  → giảm số tiền cố định (100k / 200k / 500k)
    //                             CHỈ áp dụng khi subtotal >= 5.000.000
    //   PLATINUM / DIAMOND      → giảm theo % (3% / 5%)
    //                             CHỈ áp dụng khi subtotal >= 10.000.000
    // ================================================================
    private DiscountCalculation calculateDiscount(Customer customer, BigDecimal subtotal) {
        DiscountCalculation result = new DiscountCalculation();

        // ── 1. VIP discount ──────────────────────────────────────────
        BigDecimal vipDiscount    = BigDecimal.ZERO;
        BigDecimal vipRate        = BigDecimal.ZERO;   // chỉ có giá trị cho Platinum/Diamond
        String     discountType   = "NONE";

        VipTier tier = customer.getVipTier();
        if (tier != null) {
            BigDecimal minOrder = tier.getMinOrderToApply();

            if (subtotal.compareTo(minOrder) >= 0) {
                if (tier.isPercentageDiscount()) {
                    // PLATINUM / DIAMOND: theo %
                    vipRate    = BigDecimal.valueOf(tier.getDiscountRate() * 100);  // e.g. 3.0 or 5.0
                    vipDiscount = subtotal
                            .multiply(BigDecimal.valueOf(tier.getDiscountRate()))
                            .setScale(0, RoundingMode.HALF_UP);
                    discountType = "PERCENTAGE";
                } else {
                    // BRONZE / SILVER / GOLD: số tiền cố định
                    vipDiscount  = tier.getDiscountAmount();
                    discountType = "FIXED";
                }
            }
            // else: đơn không đủ điều kiện → không giảm
        }

        // ── 2. Spin Wheel discount (luôn theo %) ────────────────────
        BigDecimal spinDiscount = BigDecimal.ZERO;
        BigDecimal spinRate     = BigDecimal.ZERO;
        boolean    hasSpinBonus = false;

        if (customer.getSpinDiscountBonus() != null
                && customer.getSpinDiscountBonus().compareTo(BigDecimal.ZERO) > 0) {
            spinRate    = customer.getSpinDiscountBonus();
            spinDiscount = subtotal
                    .multiply(spinRate.divide(BigDecimal.valueOf(100)))
                    .setScale(0, RoundingMode.HALF_UP);
            hasSpinBonus = true;
        }

        // ── 3. Tổng ─────────────────────────────────────────────────
        result.setVipDiscountRate(vipRate);
        result.setVipDiscount(vipDiscount);
        result.setSpinDiscountRate(spinRate);
        result.setSpinDiscount(spinDiscount);
        result.setTotalDiscount(vipDiscount.add(spinDiscount));
        result.setHasSpinBonus(hasSpinBonus);
        result.setDiscountType(discountType);

        return result;
    }

    // ================================================================
    // CREATE ORDER
    // ================================================================
    @Audit(module = AuditModule.ORDER, action = AuditAction.CREATE, targetType = TargetType.ORDER)
    public CreateOrderResponse createOrder(CreateOrderRequest request, Integer userId) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
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

        // ── Pass 1: tính subtotal ───────────────────────────────────
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            subtotal = subtotal.add(
                    variant.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()))
            );
        }

        // ── Tính discount ───────────────────────────────────────────
        DiscountCalculation discountCalc = calculateDiscount(customer, subtotal);

        /*
         * Để phân bổ giảm giá về từng dòng sản phẩm ta dùng tỷ lệ:
         *   totalDiscountRate = totalDiscount / subtotal
         * Nếu discount là FIXED (e.g. 100k), tỷ lệ này vẫn tính đúng.
         */
        BigDecimal totalDiscountRate = BigDecimal.ZERO;
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            totalDiscountRate = discountCalc.getTotalDiscount()
                    .divide(subtotal, 4, RoundingMode.HALF_UP);
        }

        // ── Pass 2: tạo order items ─────────────────────────────────
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

        // ── Cập nhật tổng đơn ───────────────────────────────────────
        order.setSubtotal(subtotal);
        order.setDiscountTotal(discountCalc.getTotalDiscount());
        order.setTotalAmount(
                subtotal.subtract(discountCalc.getTotalDiscount()).add(order.getShippingFee())
        );

        // ── Ghi chú chiết khấu ──────────────────────────────────────
        StringBuilder notes = new StringBuilder();
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            notes.append(order.getNotes()).append(" | ");
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
            // Tier có nhưng đơn không đủ điều kiện
            notes.append("VIP ").append(tier.getDisplayName())
                    .append(": không áp dụng (đơn < ").append(formatMoney(tier.getMinOrderToApply())).append(")");
        }

        if (discountCalc.isHasSpinBonus()) {
            if (notes.length() > 0) notes.append(" | ");
            notes.append("Spin: -").append(String.format("%.0f%%", discountCalc.getSpinDiscountRate()))
                    .append(" (-").append(formatMoney(discountCalc.getSpinDiscount())).append(")");
        }

        order.setNotes(notes.toString());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        // ── Đánh dấu spin bonus đã dùng ────────────────────────────
        if (discountCalc.isHasSpinBonus()) {
            spinWheelService.useBonus(customer.getId(), order.getId());
        }

        // ── Build response ──────────────────────────────────────────
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

        return response;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,d", amount.longValue());
    }

    // ================================================================
    // UPDATE ORDER
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
    // CANCEL ORDER
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
    // DELETE ORDER
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
    // GET ORDER DETAIL
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
    // STATUS TRANSITIONS
    // ================================================================
    @Transactional
    public void markAsShipping(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatuses.SHIPPING);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    @Transactional
    public void markAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!OrderStatuses.SHIPPING.equals(order.getStatus()))
            throw new IllegalStateException("Only SHIPPING orders can be delivered");
        order.setStatus(OrderStatuses.DELIVERED);
        order.setDeliveredAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    // ================================================================
    // HELPERS
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