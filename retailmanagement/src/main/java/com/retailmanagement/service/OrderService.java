package com.retailmanagement.service;

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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CustomRes customerRepository;
    private final UserRepository userRepository;

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


    public CreateOrderResponse createOrder(CreateOrderRequest request, Integer userId) {

        // ===== LẤY CUSTOMER & USER =====
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ===== TẠO ORDER (SET ĐỦ @NotNull) =====
        Order order = new Order();
        order.setCustomer(customer);
        order.setUser(user);
        order.setChannel(request.getChannel());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus("PENDING");
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

        // ===== XỬ LÝ ITEMS =====
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CreateOrderResponse.Item> responseItems = new ArrayList<>();

        for (CreateOrderItemRequest itemReq : request.getItems()) {

            ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            int available = variant.getStockQuantity() - variant.getReservedQty();
            if (available < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough stock");
            }

            BigDecimal lineTotal =
                    variant.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            // ----- ORDER ITEM -----
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setProduct(variant.getProduct());
            item.setProductName(variant.getProduct().getName());
            item.setVariantName(variant.getVariantName());
            item.setSku(variant.getSku());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(variant.getPrice());
            item.setLineTotal(lineTotal);

            // FIX @NotNull
            item.setDiscount(BigDecimal.ZERO);
            item.setCreatedAt(Instant.now());

            orderItemRepository.save(item);

            subtotal = subtotal.add(lineTotal);

            // ----- RESERVE TỒN KHO -----
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

            // ----- MAP RESPONSE ITEM (PHẲNG) -----
            responseItems.add(new CreateOrderResponse.Item(
                    item.getProduct().getId(),      // productId
                    item.getVariant().getId(),      // variantId

                    item.getProductName(),
                    item.getVariantName(),
                    item.getSku(),

                    item.getQuantity(),

                    item.getUnitPrice(),             // price
                    item.getDiscount(),
                    item.getLineTotal()
            ));
        }

        // ===== CẬP NHẬT TOTAL =====
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);

        // ===== TRẢ RESPONSE ĐẦY ĐỦ (PHẲNG) =====
        return new CreateOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getPaymentStatus(),

                customer.getId(),
                customer.getName(),

                user.getId(),
                user.getUsername(),

                order.getSubtotal(),
                order.getDiscountTotal(),
                order.getTaxTotal(),
                order.getShippingFee(),
                order.getTotalAmount(),

                order.getCreatedAt(),
                responseItems
        );
    }

    public void updateOrder(Long orderId, UpdateOrderRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ được chỉnh sửa đơn PENDING");
        }

        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
    }

    /* =========================
       CANCEL ORDER + RELEASE STOCK
       ========================= */
    public void cancelOrder(Long orderId, Integer userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ được hủy đơn PENDING");
        }

        for (OrderItem item : order.getOrderItems()) {

            ProductVariant variant = item.getVariant();
            variant.setReservedQty(
                    variant.getReservedQty() - item.getQuantity()
            );
            variantRepository.save(variant);

            StockTransaction st = new StockTransaction();
            st.setVariant(variant);
            st.setQuantity(item.getQuantity());
            st.setType("RELEASE");
            st.setReferenceType("orders");
            st.setReferenceId(order.getId());
            st.setCreatedBy(order.getUser());
            st.setCreatedAt(Instant.now());

            stockTransactionRepository.save(st);
        }

        order.setStatus("CANCELLED");
        order.setCancelledAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
    }

    /* =========================
       DELETE ORDER
       ========================= */
    public void deleteOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ được xóa đơn PENDING");
        }

        orderItemRepository.deleteAll(order.getOrderItems());
        orderRepository.delete(order);
    }

    /* =========================
       RECALC TOTAL
       ========================= */
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

    /* =========================
       ORDER DETAIL
       ========================= */
    public OrderDetailResponse getOrderDetail(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<CreateOrderResponse.Item> items =
                order.getOrderItems().stream()
                        .map(i -> new CreateOrderResponse.Item(
                                i.getProduct().getId(),      // productId
                                i.getVariant().getId(),      // variantId

                                i.getProductName(),
                                i.getVariantName(),
                                i.getSku(),

                                i.getQuantity(),

                                i.getUnitPrice(),             // price
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

                order.getNotes(),                 // ✅ notes

                order.getSubtotal(),
                order.getDiscountTotal(),
                order.getTaxTotal(),
                order.getShippingFee(),
                order.getTotalAmount(),

                order.getCreatedAt(),
                items
        );


    }
}
