package com.retailmanagement.service;

import com.retailmanagement.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderCreatedEmail(Order order) {

        if (order.getCustomer() == null ||
                order.getCustomer().getEmail() == null) return;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(order.getCustomer().getEmail());
        message.setSubject("Xác nhận đơn hàng " + order.getOrderNumber());

        message.setText("""
                Xin chào %s,

                Đơn hàng %s đã được tạo thành công.

                Tổng tiền: %s VNĐ

                Cảm ơn bạn đã mua hàng!
                """.formatted(
                order.getCustomer().getName(),
                order.getOrderNumber(),
                order.getTotalAmount()
        ));

        mailSender.send(message);
    }
}
