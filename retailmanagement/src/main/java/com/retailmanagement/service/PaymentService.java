package com.retailmanagement.service;

import com.retailmanagement.dto.request.PaymentRequest;
import com.retailmanagement.dto.response.PaymentItem;
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
    private final ImageRepository imageRepository; // THÊM MỚI

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
     * Lấy tất cả payments - THÔNG TIN CƠ BẢN
     */
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ========== THÊM MỚI: PHẦN CHI TIẾT ==========

    /**
     * Lấy chi tiết payment ĐẦY ĐỦ (kèm items)
     */
    @Transactional
    public PaymentResponse getPaymentDetail(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));
        return mapToDetailResponse(payment);
    }

    /**
     * Map Payment → Response CHI TIẾT (có items)
     */
    private PaymentResponse mapToDetailResponse(Payment payment) {
        Order order = payment.getOrder();
        Customer customer = order.getCustomer();

        // Lấy danh sách items
        List<PaymentResponse.PaymentItem> items = order.getOrderItems().stream()
                .map(this::mapToPaymentItem)
                .collect(Collectors.toList());

        PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder()
                // Fields cũ
                .id(payment.getId())
                .orderId(order.getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt());

        // Customer info
        if (customer != null) {
            builder.customerId(customer.getId())
                    .customerName(customer.getName())
                    .customerEmail(customer.getEmail())
                    .customerPhone(customer.getPhone())
                    .customerType(customer.getCustomerType().name())
                    .vipTier(customer.getVipTier() != null ? customer.getVipTier().name() : null);
        }

        // Order info
        builder.orderNumber(order.getOrderNumber())
                .orderStatus(order.getStatus())
                .channel(order.getChannel())
                .subtotal(order.getSubtotal())
                .discountTotal(order.getDiscountTotal())
                .taxTotal(order.getTaxTotal())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .appliedPromotionCode(order.getAppliedPromotionCode())
                .notes(order.getNotes())
                .items(items); // DANH SÁCH SẢN PHẨM

        return builder.build();
    }

    /**
     * Map OrderItem → PaymentItem
     */
    private PaymentResponse.PaymentItem mapToPaymentItem(OrderItem item) {
        String imageUrl = null;

        // Lấy ảnh từ variant hoặc product
        if (item.getVariant() != null) {
            imageUrl = imageRepository.findFirstByProductIdAndIsPrimaryTrue(item.getVariant().getId())
                    .map(Image::getUrl)
                    .orElse(null);
        }

        if (imageUrl == null && item.getProduct() != null) {
            imageUrl = imageRepository.findFirstByProductIdAndIsPrimaryTrue(item.getProduct().getId())
                    .map(Image::getUrl)
                    .orElse(null);
        }

        return PaymentResponse.PaymentItem.builder()
                .itemId(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantName(item.getVariantName())
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discount(item.getDiscount())
                .lineTotal(item.getLineTotal())
                .imageUrl(imageUrl)
                .build();
    }
    // ========== KẾT THÚC PHẦN THÊM MỚI ==========

    /**
     * Map Payment entity sang PaymentResponse (CƠ BẢN - không có items)
     */
    private PaymentResponse mapToResponse(Payment payment) {
        Order order = payment.getOrder();

        PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder()
                .id(payment.getId())
                .orderId(order.getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt());

        // Thêm thông tin customer
        Customer customer = order.getCustomer();
        if (customer != null) {
            builder.customerId(customer.getId())
                    .customerName(customer.getName());
        }

        return builder.build();
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