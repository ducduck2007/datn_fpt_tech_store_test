package com.retailmanagement.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.retailmanagement.entity.Order;
import com.retailmanagement.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    private static final Color DARK   = new Color(0x0f, 0x17, 0x2a);
    private static final Color MUTED  = new Color(0x64, 0x74, 0x8b);
    private static final Color BORDER = new Color(0xe2, 0xe8, 0xf0);
    private static final Color GREEN  = new Color(0x16, 0xa3, 0x4a);

    private static final Font F_BRAND    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  16, DARK);
    private static final Font F_TITLE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  22, DARK);
    private static final Font F_LABEL    = FontFactory.getFont(FontFactory.HELVETICA,        8, MUTED);
    private static final Font F_VALUE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, DARK);
    private static final Font F_TH       = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8, MUTED);
    private static final Font F_TD       = FontFactory.getFont(FontFactory.HELVETICA,         9, DARK);
    private static final Font F_TD_MUTED = FontFactory.getFont(FontFactory.HELVETICA,         9, MUTED);
    private static final Font F_SUM_L    = FontFactory.getFont(FontFactory.HELVETICA,         9, MUTED);
    private static final Font F_SUM_V    = FontFactory.getFont(FontFactory.HELVETICA,         9, DARK);
    private static final Font F_DISCOUNT = FontFactory.getFont(FontFactory.HELVETICA,         9, GREEN);
    private static final Font F_TOTAL_L  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, Color.WHITE);
    private static final Font F_TOTAL_V  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  12, Color.WHITE);
    private static final Font F_FOOTER   = FontFactory.getFont(FontFactory.HELVETICA,       7.5f, MUTED);

    private String money(BigDecimal v) {
        if (v == null) return "0 ₫";
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + " ₫";
    }

    private PdfPCell bare(Phrase ph, int align) {
        PdfPCell c = new PdfPCell(ph);
        c.setHorizontalAlignment(align);
        c.setBackgroundColor(Color.WHITE);
        c.setPadding(0);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private PdfPCell row(Phrase ph, int align) {
        PdfPCell c = new PdfPCell(ph);
        c.setHorizontalAlignment(align);
        c.setBackgroundColor(Color.WHITE);
        c.setPadding(9);
        c.setPaddingLeft(0); c.setPaddingRight(0);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColorBottom(BORDER);
        c.setBorderWidthBottom(0.5f);
        return c;
    }

    public byte[] generateOrderPdf(Order order) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            float m = 48;
            Document doc = new Document(PageSize.A4, m, m, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            String date = order.getCreatedAt() != null
                    ? order.getCreatedAt()
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "—";

            // ── 1. Header: Brand | INVOICE ───────────────────────
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setSpacingAfter(6);
            header.addCell(bare(new Phrase("TechStore", F_BRAND), Element.ALIGN_LEFT));
            header.addCell(bare(new Phrase("HÓA ĐƠN", F_TITLE), Element.ALIGN_RIGHT));
            doc.add(header);

            // ── Divider ──────────────────────────────────────────
            PdfPTable hr = new PdfPTable(1);
            hr.setWidthPercentage(100);
            hr.setSpacingAfter(20);
            PdfPCell hrCell = new PdfPCell(new Phrase(" "));
            hrCell.setBorder(Rectangle.BOTTOM);
            hrCell.setBorderColorBottom(DARK);
            hrCell.setBorderWidthBottom(1.5f);
            hrCell.setPadding(0);
            hr.addCell(hrCell);
            doc.add(hr);

            // ── 2. Meta: order# / date / customer ───────────────
            PdfPTable meta = new PdfPTable(3);
            meta.setWidthPercentage(100);
            meta.setSpacingAfter(28);

            String[][] fields = {
                    {"Mã đơn hàng", "#" + order.getOrderNumber()},
                    {"Ngày tạo",    date},
                    {"Khách hàng",  order.getCustomer().getName()}
            };
            for (String[] f : fields) {
                PdfPTable stack = new PdfPTable(1);
                stack.addCell(bare(new Phrase(f[0], F_LABEL), Element.ALIGN_LEFT));
                PdfPCell valCell = bare(new Phrase(f[1], F_VALUE), Element.ALIGN_LEFT);
                valCell.setPaddingTop(4);
                stack.addCell(valCell);
                PdfPCell w = new PdfPCell(stack);
                w.setBorder(Rectangle.NO_BORDER);
                w.setPadding(0);
                meta.addCell(w);
            }
            doc.add(meta);

            // ── 3. Items table ───────────────────────────────────
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 0.8f, 1.6f, 1.6f});
            table.setSpacingAfter(20);

            String[] ths = {"SẢN PHẨM", "SL", "ĐƠN GIÁ", "THÀNH TIỀN"};
            int[]    als = {Element.ALIGN_LEFT, Element.ALIGN_CENTER,
                    Element.ALIGN_RIGHT, Element.ALIGN_RIGHT};
            for (int i = 0; i < ths.length; i++) {
                PdfPCell th = new PdfPCell(new Phrase(ths[i], F_TH));
                th.setHorizontalAlignment(als[i]);
                th.setBackgroundColor(Color.WHITE);
                th.setPaddingTop(0); th.setPaddingBottom(8);
                th.setPaddingLeft(0); th.setPaddingRight(0);
                th.setBorder(Rectangle.BOTTOM);
                th.setBorderColorBottom(MUTED);
                th.setBorderWidthBottom(0.75f);
                table.addCell(th);
            }

            for (OrderItem item : order.getOrderItems()) {
                table.addCell(row(new Phrase(item.getProductName(), F_TD), Element.ALIGN_LEFT));
                table.addCell(row(new Phrase(String.valueOf(item.getQuantity()), F_TD_MUTED), Element.ALIGN_CENTER));
                table.addCell(row(new Phrase(money(item.getUnitPrice()), F_TD_MUTED), Element.ALIGN_RIGHT));
                table.addCell(row(new Phrase(money(item.getLineTotal()), F_TD), Element.ALIGN_RIGHT));
            }
            doc.add(table);

            // ── 4. Summary (right half) ──────────────────────────
            PdfPTable sw = new PdfPTable(2);
            sw.setWidthPercentage(100);
            sw.setWidths(new float[]{1f, 1.1f});
            sw.setSpacingAfter(20);
            sw.addCell(bare(new Phrase(""), Element.ALIGN_LEFT));

            PdfPTable si = new PdfPTable(2);
            si.setWidthPercentage(100);

            if (order.getSubtotal() != null)
                addRow(si, "Tạm tính", money(order.getSubtotal()), F_SUM_V, false);

            if (order.getVipDiscount() != null &&
                    order.getVipDiscount().compareTo(BigDecimal.ZERO) > 0)
                addRow(si, "Giảm VIP", "- " + money(order.getVipDiscount()), F_DISCOUNT, false);

            if (order.getSpinDiscount() != null &&
                    order.getSpinDiscount().compareTo(BigDecimal.ZERO) > 0)
                addRow(si, "Giảm Vòng quay", "- " + money(order.getSpinDiscount()), F_DISCOUNT, false);

            if (order.getAppliedPromotionCode() != null)
                addRow(si, "Mã KM", order.getAppliedPromotionCode(), F_DISCOUNT, false);

            if (order.getDiscountTotal() != null &&
                    order.getDiscountTotal().compareTo(BigDecimal.ZERO) > 0)
                addRow(si, "Tổng giảm", "- " + money(order.getDiscountTotal()), F_DISCOUNT, true);

            PdfPCell sh = new PdfPCell(si);
            sh.setBorder(Rectangle.NO_BORDER);
            sh.setPadding(0);
            sw.addCell(sh);
            doc.add(sw);

            // ── 5. Total bar ─────────────────────────────────────
            PdfPTable total = new PdfPTable(2);
            total.setWidthPercentage(100);
            total.setSpacingAfter(32);

            PdfPCell tl = new PdfPCell(new Phrase("TỔNG THANH TOÁN", F_TOTAL_L));
            tl.setBackgroundColor(DARK);
            tl.setHorizontalAlignment(Element.ALIGN_LEFT);
            tl.setBorder(Rectangle.NO_BORDER);
            tl.setPadding(14); tl.setPaddingLeft(16);

            PdfPCell tv = new PdfPCell(new Phrase(money(order.getTotalAmount()), F_TOTAL_V));
            tv.setBackgroundColor(DARK);
            tv.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tv.setBorder(Rectangle.NO_BORDER);
            tv.setPadding(14); tv.setPaddingRight(16);

            total.addCell(tl);
            total.addCell(tv);
            doc.add(total);

            // ── 6. Footer ────────────────────────────────────────
            PdfPTable footer = new PdfPTable(1);
            footer.setWidthPercentage(100);
            PdfPCell fc = new PdfPCell(
                    new Phrase("TechStore  ·  support@nguyenduc.me  ·  Cảm ơn bạn đã mua hàng!", F_FOOTER));
            fc.setHorizontalAlignment(Element.ALIGN_CENTER);
            fc.setBorder(Rectangle.TOP);
            fc.setBorderColorTop(BORDER);
            fc.setBorderWidthTop(0.75f);
            fc.setPaddingTop(12); fc.setPaddingBottom(4);
            footer.addCell(fc);
            doc.add(footer);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate PDF", e);
        }
    }

    private void addRow(PdfPTable t, String label, String value, Font vFont, boolean topBorder) {
        PdfPCell lc = new PdfPCell(new Phrase(label, F_SUM_L));
        lc.setHorizontalAlignment(Element.ALIGN_LEFT);
        lc.setBackgroundColor(Color.WHITE);
        lc.setPaddingTop(topBorder ? 10 : 6); lc.setPaddingBottom(6);
        lc.setPaddingLeft(0); lc.setPaddingRight(8);
        if (topBorder) {
            lc.setBorder(Rectangle.TOP);
            lc.setBorderColorTop(BORDER);
            lc.setBorderWidthTop(0.5f);
        } else {
            lc.setBorder(Rectangle.NO_BORDER);
        }

        PdfPCell vc = new PdfPCell(new Phrase(value, vFont));
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBackgroundColor(Color.WHITE);
        vc.setPaddingTop(topBorder ? 10 : 6); vc.setPaddingBottom(6);
        vc.setPaddingLeft(8); vc.setPaddingRight(0);
        if (topBorder) {
            vc.setBorder(Rectangle.TOP);
            vc.setBorderColorTop(BORDER);
            vc.setBorderWidthTop(0.5f);
        } else {
            vc.setBorder(Rectangle.NO_BORDER);
        }

        t.addCell(lc);
        t.addCell(vc);
    }
}