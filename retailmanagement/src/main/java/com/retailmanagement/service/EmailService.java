package com.retailmanagement.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.retailmanagement.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@EnableAsync
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Async
    public void sendOrderCreatedEmail(Order order) {

        try {

            if (order.getCustomer() == null ||
                    order.getCustomer().getEmail() == null) return;

            Resend resend = new Resend(apiKey);

            String content = """
                Xin chào %s,

                Đơn hàng %s đã được tạo thành công.

                Tổng tiền: %s VNĐ

                Cảm ơn bạn đã mua hàng!
                """.formatted(
                    order.getCustomer().getName(),
                    order.getOrderNumber(),
                    order.getTotalAmount()
            );

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("TechStore <noreply@nguyenduc.me>")
                    .to(order.getCustomer().getEmail())
                    .subject("Xác nhận đơn hàng " + order.getOrderNumber())
                    .text(content)
                    .build();

            resend.emails().send(params);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}