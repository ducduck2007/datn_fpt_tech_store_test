package com.retailmanagement.service;

import com.retailmanagement.entity.Order;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.Base64;

@EnableAsync
@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private final PdfService pdfService;

    @Value("${resend.api.key}")
    private String apiKey;

    @Async
    public void sendDeliveredEmail(Order order) {

        try {

            if (order.getCustomer() == null ||
                    order.getCustomer().getEmail() == null) {
                return;
            }

            byte[] pdf = pdfService.generateOrderPdf(order);

            String content = """
                    Xin chào %s,

                    Đơn hàng %s của bạn đã được giao thành công.

                    Tổng tiền: %s VNĐ

                    Hóa đơn được đính kèm trong email.

                    Cảm ơn bạn đã mua hàng!
                    """.formatted(
                    order.getCustomer().getName(),
                    order.getOrderNumber(),
                    order.getTotalAmount()
            );

            Resend resend = new Resend(apiKey);

            Attachment attachment = Attachment.builder()
                    .fileName("invoice-" + order.getOrderNumber() + ".pdf")
                    .content(Base64.getEncoder().encodeToString(pdf))
                    .build();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("TechStore <noreply@nguyenduc.me>")
                    .to(order.getCustomer().getEmail())
                    .subject("Đơn hàng đã giao - " + order.getOrderNumber())
                    .text(content)
                    .attachments(new Attachment[]{attachment})
                    .build();

            resend.emails().send(params);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}