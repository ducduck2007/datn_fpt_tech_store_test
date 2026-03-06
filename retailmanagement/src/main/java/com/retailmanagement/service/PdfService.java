package com.retailmanagement.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.retailmanagement.entity.Order;
import com.retailmanagement.entity.OrderItem;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PdfService {

    private String money(BigDecimal value) {
        if (value == null) return "0";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(value);
    }

    public byte[] generateOrderPdf(Order order) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            document.add(new Paragraph("INVOICE"));
            document.add(new Paragraph("Order: " + order.getOrderNumber()));
            document.add(new Paragraph("Customer: " +
                    order.getCustomer().getName()));
            document.add(new Paragraph("Date: " +
                    order.getCreatedAt()));

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);

            table.addCell("Product");
            table.addCell("Qty");
            table.addCell("Price");
            table.addCell("Total");

            for (OrderItem item : order.getOrderItems()) {
                table.addCell(item.getProductName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(money(item.getUnitPrice()));
                table.addCell(money(item.getLineTotal()));
            }

            document.add(table);

            document.add(new Paragraph(" "));

            // Subtotal
            if (order.getSubtotal() != null) {
                document.add(new Paragraph(
                        "Subtotal: " + money(order.getSubtotal()) + " VND"
                ));
            }

            // VIP Discount
            if (order.getVipDiscount() != null &&
                    order.getVipDiscount().compareTo(BigDecimal.ZERO) > 0) {

                document.add(new Paragraph(
                        "VIP Discount: -" + money(order.getVipDiscount()) + " VND"
                ));
            }

            // Spin Discount
            if (order.getSpinDiscount() != null &&
                    order.getSpinDiscount().compareTo(BigDecimal.ZERO) > 0) {

                document.add(new Paragraph(
                        "Spin Discount: -" + money(order.getSpinDiscount()) + " VND"
                ));
            }

            // Promotion Code
            if (order.getAppliedPromotionCode() != null) {

                document.add(new Paragraph(
                        "Promotion Code: " + order.getAppliedPromotionCode()
                ));
            }

            // Total Discount
            if (order.getDiscountTotal() != null) {
                document.add(new Paragraph(
                        "Total Discount: -" + money(order.getDiscountTotal()) + " VND"
                ));
            }

            document.add(new Paragraph(" "));

            // Final Total
            document.add(new Paragraph(
                    "FINAL TOTAL: " + money(order.getTotalAmount()) + " VND"
            ));

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate PDF", e);
        }
    }
}