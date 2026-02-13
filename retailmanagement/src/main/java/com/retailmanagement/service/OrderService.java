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

    @lombok.Data
    private static class DiscountCalculation {
        private BigDecimal vipDiscountRate = BigDecimal.ZERO;
        private BigDecimal vipDiscount = BigDecimal.ZERO;
        private BigDecimal spinDiscountRate = BigDecimal.ZERO;
        private BigDecimal spinDiscount = BigDecimal.ZERO;
        private BigDecimal totalDiscount = BigDecimal.ZERO;
        private boolean hasSpinBonus = false;
    }

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CustomRes customerRepository;
    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final SpinWheelService spinWheelService;

    private String generateOrderNumber() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        Instant start = startOfDay.atZone(ZoneId.systemDefault()).toInstant();
        Instant end = endOfDay.atZone(ZoneId.systemDefault()).toInstant();

        long countToday = orderRepository.countByCreatedAtBetween(start, end);

        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String sequencePart = String.format("%06d", countToday + 1);

        return "ORD-" + datePart + "-" + sequencePart;
    }

    private DiscountCalculation calculateDiscount(Customer customer, BigDecimal subtotal) {
        DiscountCalculation result = new DiscountCalculation();

        // 1. VIP Tier Discount
        BigDecimal vipDiscountRate = BigDecimal.ZERO;
        BigDecimal vipDiscount = BigDecimal.ZERO;

        if (customer.getVipTier() != null) {
            vipDiscountRate = BigDecimal.valueOf(customer.getVipTier().getDiscountRate())
                    .multiply(BigDecimal.valueOf(100));
            vipDiscount = subtotal
                    .multiply(BigDecimal.valueOf(customer.getVipTier().getDiscountRate()))
                    .setScale(0, RoundingMode.HALF_UP);
        }

        // 2. Spin Wheel Discount
        BigDecimal spinDiscountRate = BigDecimal.ZERO;
        BigDecimal spinDiscount = BigDecimal.ZERO;
        boolean hasSpinBonus = false;

        if (customer.getSpinDiscountBonus() != null &&
                customer.getSpinDiscountBonus().compareTo(BigDecimal.ZERO) > 0) {
            spinDiscountRate = customer.getSpinDiscountBonus();
            spinDiscount = subtotal
                    .multiply(spinDiscountRate.divide(BigDecimal.valueOf(100)))
                    .setScale(0, RoundingMode.HALF_UP);
            hasSpinBonus = true;
        }

        // 3. Total discount
        BigDecimal totalDiscount = vipDiscount.add(spinDiscount);

        result.setVipDiscountRate(vipDiscountRate);
        result.setVipDiscount(vipDiscount);
        result.setSpinDiscountRate(spinDiscountRate);
        result.setSpinDiscount(spinDiscount);
        result.setTotalDiscount(totalDiscount);
        result.setHasSpinBonus(hasSpinBonus);

        return result;
    }

    @Audit(
            module = AuditModule.ORDER,
            action = AuditAction.CREATE,
            targetType = TargetType.ORDER
    )
    public CreateOrderResponse createOrder(CreateOrderRequest request, Integer userId) {

        // ===== GET CUSTOMER & USER =====
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ===== CREATE ORDER =====
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

        // ===== CALCULATE SUBTOTAL FIRST =====
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CreateOrderResponse.Item> responseItems = new ArrayList<>();

        // First pass: calculate subtotal
        for (CreateOrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            BigDecimal lineTotal = variant.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            subtotal = subtotal.add(lineTotal);
        }

        // ===== CALCULATE DISCOUNT =====
        DiscountCalculation discountCalc = calculateDiscount(customer, subtotal);

        // Calculate discount rate to apply per item
        BigDecimal totalDiscountRate = BigDecimal.ZERO;
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            totalDiscountRate = discountCalc.getTotalDiscount()
                    .divide(subtotal, 4, RoundingMode.HALF_UP);
        }

        // ===== PROCESS ITEMS WITH DISCOUNT =====
        for (CreateOrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            int available = variant.getStockQuantity() - variant.getReservedQty();
            if (available < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough stock for " + variant.getVariantName());
            }

            BigDecimal lineSubtotal = variant.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            // ✅ APPLY DISCOUNT TO THIS LINE
            BigDecimal lineDiscount = lineSubtotal
                    .multiply(totalDiscountRate)
                    .setScale(0, RoundingMode.HALF_UP);

            BigDecimal lineTotal = lineSubtotal.subtract(lineDiscount);

            // ----- CREATE ORDER ITEM -----
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setProduct(variant.getProduct());
            item.setProductName(variant.getProduct().getName());
            item.setVariantName(variant.getVariantName());
            item.setSku(variant.getSku());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(variant.getPrice());
            item.setDiscount(lineDiscount);  // ✅ SET DISCOUNT
            item.setLineTotal(lineTotal);
            item.setCreatedAt(Instant.now());

            orderItemRepository.save(item);

            // ----- RESERVE STOCK -----
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

            // ----- MAP RESPONSE ITEM -----
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

        // ===== UPDATE ORDER TOTALS =====
        order.setSubtotal(subtotal);
        order.setDiscountTotal(discountCalc.getTotalDiscount());

        BigDecimal finalTotal = subtotal
                .subtract(discountCalc.getTotalDiscount())
                .add(order.getShippingFee());

        order.setTotalAmount(finalTotal);

        // ✅ CREATE DETAILED NOTES
        StringBuilder notesBuilder = new StringBuilder();
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            notesBuilder.append(order.getNotes()).append(" | ");
        }

        notesBuilder.append("Discount: VIP: ")
                .append(String.format("%.1f%%", discountCalc.getVipDiscountRate()))
                .append(" (-")
                .append(formatMoney(discountCalc.getVipDiscount()))
                .append(")");

        if (discountCalc.isHasSpinBonus()) {
            notesBuilder.append(" | Spin: ")
                    .append(String.format("%.1f%%", discountCalc.getSpinDiscountRate()))
                    .append(" (-")
                    .append(formatMoney(discountCalc.getSpinDiscount()))
                    .append(")");
        }

        order.setNotes(notesBuilder.toString());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        // ✅ MARK SPIN BONUS AS USED (if applicable)
        if (discountCalc.isHasSpinBonus()) {
            spinWheelService.useBonus(customer.getId(), order.getId());
        }

        // ===== CREATE RESPONSE =====
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

        // ✅ ADD DISCOUNT BREAKDOWN
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

    @Audit(
            module = AuditModule.ORDER,
            action = AuditAction.UPDATE,
            targetType = TargetType.ORDER
    )
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

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (OrderStatuses.CANCELLED.equals(order.getStatus())) {
            throw new IllegalStateException("Order already cancelled");
        }

        if (OrderStatuses.DELIVERED.equals(order.getStatus())) {
            throw new IllegalStateException("Delivered order cannot be cancelled");
        }

        // 1. RELEASE RESERVED QTY
        order.getOrderItems().forEach(item -> {
            ProductVariant variant = variantRepository
                    .findById(item.getVariant().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Variant not found: " + item.getVariant().getId()));

            int currentReserved = variant.getReservedQty();
            int newReserved = Math.max(0, currentReserved - item.getQuantity());

            variant.setReservedQty(newReserved);
            variantRepository.save(variant);
        });

        // 2. UPDATE ORDER STATUS
        order.setStatus(OrderStatuses.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setNotes(reason);

        // 3. RESTORE STOCK
        restoreStock(order);

        // 4. DEDUCT LOYALTY POINTS + PENALTY (if paid)
        if ("PAID".equals(order.getPaymentStatus()) && order.getCustomer() != null) {
            customerService.deductLoyaltyPoints(
                    order.getCustomer().getId(),
                    order.getTotalAmount(),
                    "CANCEL_ORDER",
                    "orders",
                    orderId
            );

            BigDecimal penaltyAmount = order.getTotalAmount()
                    .multiply(BigDecimal.valueOf(0.10));

            customerService.deductLoyaltyPoints(
                    order.getCustomer().getId(),
                    penaltyAmount,
                    "CANCEL_PENALTY",
                    "orders",
                    orderId
            );
        }

        orderRepository.save(order);
    }

    @Audit(
            module = AuditModule.ORDER,
            action = AuditAction.DELETE,
            targetType = TargetType.ORDER
    )
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!OrderStatuses.PENDING.equals(order.getStatus())) {
            throw new RuntimeException("Chỉ được xóa đơn PENDING");
        }

        orderItemRepository.deleteAll(order.getOrderItems());
        orderRepository.delete(order);
    }

    private void recalcOrderTotal(Order order) {
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);
        order.setTotalAmount(
                subtotal
                        .subtract(order.getDiscountTotal())
                        .add(order.getTaxTotal())
                        .add(order.getShippingFee())
        );
    }

    public OrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<CreateOrderResponse.Item> items =
                order.getOrderItems().stream()
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
                        ))
                        .toList();

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

        if (!OrderStatuses.SHIPPING.equals(order.getStatus())) {
            throw new IllegalStateException("Only SHIPPING orders can be delivered");
        }

        order.setStatus(OrderStatuses.DELIVERED);
        order.setDeliveredAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
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