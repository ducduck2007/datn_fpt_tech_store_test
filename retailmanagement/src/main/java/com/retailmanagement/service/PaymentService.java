package com.retailmanagement.service;

import com.retailmanagement.dto.request.PaymentRequest;
import com.retailmanagement.dto.response.PaymentResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CustomerService customerService;

    /**
     * Tạo payment và xử lý thanh toán đơn hàng
     * - Tự động lấy số tiền từ order.totalAmount
     * - Tạo bản ghi payment
     * - Cập nhật trạng thái order thành PAID
     * - Xuất kho (EXPORT) và giải phóng reserved
     * - Cộng điểm loyalty và totalSpent cho customer
     */
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, Integer userId) {

        // 1. Kiểm tra order tồn tại
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // 2. Kiểm tra trạng thái order
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể thanh toán đơn hàng ở trạng thái PENDING");
        }

        // 3. Tự động lấy số tiền cần thanh toán = totalAmount của order
        BigDecimal paymentAmount = order.getTotalAmount();

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền đơn hàng không hợp lệ");
        }

        // 4. Tạo payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(paymentAmount); // Tự động lấy từ order
        payment.setMethod(request.getMethod());
        payment.setTransactionRef(request.getTransactionRef());
        payment.setStatus("SUCCESS"); // Thanh toán ảo luôn thành công
        payment.setPaidAt(Instant.now());
        payment.setCreatedAt(Instant.now());

        payment = paymentRepository.save(payment);

        // 5. Cập nhật trạng thái order thành PAID

        order.setPaymentStatus("PAID");
        order.setStatus("PAID");
        order.setPaidAt(Instant.now());

        // 6. XỬ LÝ XUẤT KHO (EXPORT)
        processStockExport(order, userId);

        // 7. CỘNG ĐIỂM VÀ TOTALSPENT CHO CUSTOMER
        if (order.getCustomer() != null) {
            customerService.addLoyaltyPoints(
                    order.getCustomer().getId(),
                    order.getTotalAmount()
            );

            // Cập nhật last_order_at
            order.getCustomer().setLastOrderAt(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        // 8. Trả response
        return mapToResponse(payment);
    }

    /**
     * Xử lý xuất kho sau khi thanh toán thành công
     * - Giảm stock_quantity
     * - Giảm reserved_qty
     * - Tạo EXPORT transaction
     */
    private void processStockExport(Order order, Integer userId) {

        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = item.getVariant();

            // 1. Kiểm tra tồn kho
            int availableStock = variant.getStockQuantity() - variant.getReservedQty();
            if (availableStock < 0) {
                throw new RuntimeException(
                        "Lỗi tồn kho: variant " + variant.getSku() + " không đủ hàng"
                );
            }

            // 2. Xuất kho: giảm stock_quantity
            variant.setStockQuantity(
                    variant.getStockQuantity() - item.getQuantity()
            );

            // 3. Giải phóng reserved
            variant.setReservedQty(
                    variant.getReservedQty() - item.getQuantity()
            );

            variantRepository.save(variant);

            // 4. Tạo EXPORT transaction
            StockTransaction exportTx = new StockTransaction();
            exportTx.setVariant(variant);
            exportTx.setQuantity(-item.getQuantity()); // Số âm = xuất
            exportTx.setType("EXPORT");
            exportTx.setReferenceType("orders");
            exportTx.setReferenceId(order.getId());
            exportTx.setNote("Xuất kho sau thanh toán đơn " + order.getOrderNumber());

            if (userId != null) {
                exportTx.setCreatedBy(order.getUser());
            }

            exportTx.setCreatedAt(Instant.now());

            stockTransactionRepository.save(exportTx);
        }
    }

    /**
     * Lấy danh sách payment của một order
     */
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một payment
     */
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));
        return mapToResponse(payment);
    }

    /**
     * Map Payment entity sang PaymentResponse
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * Hủy payment (hoàn tiền)
     */
    @Transactional
    public void refundPayment(Long paymentId, Integer userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));

        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new RuntimeException("Chỉ có thể hoàn tiền cho payment đã thành công");
        }

        Order order = payment.getOrder();

        if (!"PAID".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể hoàn tiền cho đơn hàng đã thanh toán");
        }

        // Cập nhật status payment
        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);

        // Cập nhật lại order về PENDING
        order.setPaymentStatus("UNPAID");
        order.setStatus("PENDING");
        order.setPaidAt(null);

        // Hoàn lại tồn kho (IMPORT)
        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = item.getVariant();

            // Hoàn lại stock_quantity
            variant.setStockQuantity(
                    variant.getStockQuantity() + item.getQuantity()
            );

            // Hoàn lại reserved
            variant.setReservedQty(
                    variant.getReservedQty() + item.getQuantity()
            );

            variantRepository.save(variant);

            // Tạo RETURN transaction
            StockTransaction returnTx = new StockTransaction();
            returnTx.setVariant(variant);
            returnTx.setQuantity(item.getQuantity()); // Số dương = nhập
            returnTx.setType("RETURN");
            returnTx.setReferenceType("orders");
            returnTx.setReferenceId(order.getId());
            returnTx.setNote("Hoàn kho do refund payment " + payment.getId());

            if (userId != null) {
                returnTx.setCreatedBy(order.getUser());
            }

            returnTx.setCreatedAt(Instant.now());
            stockTransactionRepository.save(returnTx);
        }

        orderRepository.save(order);
    }
}