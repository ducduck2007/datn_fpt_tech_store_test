package com.retailmanagement.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.retailmanagement.entity.Order;
import com.retailmanagement.entity.OrderItem;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

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
                table.addCell(item.getUnitPrice().toString());
                table.addCell(item.getLineTotal().toString());
            }

            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "TOTAL: " + order.getTotalAmount() + " VND"
            ));

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate PDF", e);
        }
    }
}
