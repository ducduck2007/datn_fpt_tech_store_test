package com.retailmanagement.service;

import com.retailmanagement.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private final PdfService pdfService;
    private final NotificationService notificationService;

    public void sendDeliveredEmail(Order order) {

        if (order.getCustomer() == null ||
                order.getCustomer().getEmail() == null) {
            return;
        }

        byte[] pdf = pdfService.generateOrderPdf(order);

        notificationService.sendOrderDeliveredEmail(
                order.getCustomer().getEmail(),
                order,
                pdf
        );
    }
}