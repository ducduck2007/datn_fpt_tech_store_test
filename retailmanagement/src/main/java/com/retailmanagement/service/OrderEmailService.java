package com.retailmanagement.service;

import com.retailmanagement.entity.Order;
import com.retailmanagement.entity.OrderItem;
import com.resend.Resend;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

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
                    order.getCustomer().getEmail() == null) return;

            byte[] pdf = pdfService.generateOrderPdf(order);

            String htmlContent = buildDeliveredEmail(order);

            Resend resend = new Resend(apiKey);

            Attachment attachment = Attachment.builder()
                    .fileName("invoice-" + order.getOrderNumber() + ".pdf")
                    .content(Base64.getEncoder().encodeToString(pdf))
                    .build();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("TechStore <noreply@nguyenduc.me>")
                    .to(order.getCustomer().getEmail())
                    .subject("📦 Đơn hàng đã giao thành công #" + order.getOrderNumber())
                    .html(htmlContent)
                    .attachments(new Attachment[]{attachment})
                    .build();

            resend.emails().send(params);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private String fmt(BigDecimal v) {
        if (v == null) return "0";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(v);
    }

    private boolean isPositive(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    private String safe(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private String buildItemRows(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : order.getOrderItems()) {
            sb.append("""
              <tr>
                <td style="padding:12px 0;border-bottom:1px solid #f1f5f9;font-size:14px;color:#334155;line-height:1.5;">
                  <span style="font-weight:600;color:#0f172a;">%s</span>
                  %s
                </td>
                <td style="padding:12px 0;border-bottom:1px solid #f1f5f9;text-align:center;
                            font-size:13px;color:#64748b;white-space:nowrap;">x%d</td>
                <td style="padding:12px 0;border-bottom:1px solid #f1f5f9;text-align:right;
                            font-size:13px;color:#64748b;white-space:nowrap;">%s ₫</td>
                <td style="padding:12px 0;border-bottom:1px solid #f1f5f9;text-align:right;
                            font-size:13px;font-weight:700;color:#16a34a;white-space:nowrap;">%s ₫</td>
              </tr>
            """.formatted(
                    item.getProductName(),
                    "",
                    item.getQuantity(),
                    fmt(item.getUnitPrice()),
                    fmt(item.getLineTotal())
            ));
        }
        return sb.toString();
    }

    private String buildSummaryRows(Order order) {
        StringBuilder sb = new StringBuilder();
        if (order.getSubtotal() != null)
            sb.append(sRow("Tạm tính",        fmt(order.getSubtotal()) + " ₫",           "#475569", false));
        if (isPositive(order.getShippingFee()))
            sb.append(sRow("Phí vận chuyển",  fmt(order.getShippingFee()) + " ₫",        "#475569", false));
        if (isPositive(order.getVipDiscount()))
            sb.append(sRow("Giảm VIP",        "- " + fmt(order.getVipDiscount()) + " ₫", "#16a34a", false));
        if (isPositive(order.getSpinDiscount()))
            sb.append(sRow("Giảm Vòng quay",  "- " + fmt(order.getSpinDiscount()) + " ₫","#16a34a", false));
        if (order.getAppliedPromotionCode() != null)
            sb.append(sRow("Mã KM",           order.getAppliedPromotionCode(),             "#16a34a", false));
        if (isPositive(order.getDiscountTotal()))
            sb.append(sRow("Tổng giảm",       "- " + fmt(order.getDiscountTotal()) + " ₫","#16a34a", true));
        return sb.toString();
    }

    private String sRow(String label, String value, String color, boolean topBorder) {
        String border = topBorder ? "border-top:1px solid #e2e8f0;padding-top:10px;margin-top:4px;" : "";
        return """
          <tr>
            <td style="padding:5px 0;font-size:13px;color:#64748b;%s">%s</td>
            <td style="padding:5px 0;font-size:13px;color:%s;text-align:right;font-weight:600;%s">%s</td>
          </tr>
        """.formatted(border, label, color, border, value);
    }

    // ── Main HTML builder ─────────────────────────────────────────────────
    private String buildDeliveredEmail(Order order) {
        String customerName  = safe(order.getCustomer().getName(),  "Khách hàng");
        String customerEmail = safe(order.getCustomer().getEmail(), "—");
        String customerPhone = safe(order.getCustomer().getPhone() != null ? order.getCustomer().getPhone() : null, "—");
        String customerAddr  = safe(order.getCustomer().getAddress() != null ? order.getCustomer().getAddress() : null, "—");
        String paymentMethod = safe(order.getPaymentMethod(), "—");

        String deliveredAt = order.getUpdatedAt() != null
                ? order.getUpdatedAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"))
                : "—";

        String itemRows    = buildItemRows(order);
        String summaryRows = buildSummaryRows(order);
        String total       = fmt(order.getTotalAmount());
        String invoiceName = "invoice-" + order.getOrderNumber() + ".pdf";
        String orderNote   = order.getNotes() != null && !order.getNotes().isBlank()
                ? "<tr><td colspan=\"2\" style=\"padding:6px 0 2px;\"><div style=\"border-left:3px solid #16a34a;padding:10px 12px;background:#f0fdf4;border-radius:6px;font-size:13px;color:#475569;\"><strong>Ghi chú:</strong> " + order.getNotes() + "</div></td></tr>"
                : "";

        return """
        <!DOCTYPE html>
        <html lang="vi">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
          <title>Đơn hàng đã giao</title>
        </head>
        <body style="margin:0;padding:0;background:#f0f2f5;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0f2f5;padding:40px 0;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0"
                     style="max-width:600px;width:100%%;background:#fff;border-radius:16px;
                            overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                <!-- HEADER -->
                <tr>
                  <td style="background:linear-gradient(135deg,#052e16 0%%,#14532d 60%%,#16a34a 100%%);
                              padding:40px 48px;text-align:center;">
                    <p style="margin:0 0 12px;font-size:13px;letter-spacing:4px;text-transform:uppercase;
                               color:#86efac;font-weight:600;">TECHSTORE</p>
                    <h1 style="margin:0;font-size:26px;font-weight:700;color:#fff;line-height:1.2;">
                      Đơn hàng đã giao thành công 📦
                    </h1>
                    <p style="margin:10px 0 0;font-size:14px;color:#a7f3d0;">
                      Cảm ơn bạn đã tin tưởng mua sắm tại TechStore
                    </p>
                  </td>
                </tr>

                <!-- BADGE + TIME -->
                <tr>
                  <td style="padding:28px 48px 0;text-align:center;">
                    <div style="display:inline-block;background:#f0fdf4;border:1.5px solid #bbf7d0;
                                border-radius:100px;padding:10px 24px;">
                      <span style="font-size:14px;font-weight:600;color:#16a34a;">
                        ✓ &nbsp;Giao hàng thành công — %s
                      </span>
                    </div>
                  </td>
                </tr>

                <!-- GREETING -->
                <tr>
                  <td style="padding:28px 48px 0;">
                    <p style="margin:0;font-size:16px;color:#334155;line-height:1.6;">
                      Xin chào <strong style="color:#0f172a;">%s</strong>,
                    </p>
                    <p style="margin:10px 0 0;font-size:15px;color:#475569;line-height:1.7;">
                      Đơn hàng <strong>#%s</strong> của bạn đã được giao thành công.
                      Hóa đơn chi tiết (<strong>%s</strong>) đã được đính kèm trong email này.
                    </p>
                  </td>
                </tr>

                <!-- CUSTOMER INFO -->
                <tr>
                  <td style="padding:24px 48px 0;">
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#f8fafc;border:1.5px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                      <tr>
                        <td style="padding:14px 20px;border-bottom:1px solid #e2e8f0;">
                          <p style="margin:0;font-size:11px;font-weight:700;letter-spacing:2px;
                                     text-transform:uppercase;color:#94a3b8;">THÔNG TIN KHÁCH HÀNG</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:4px 20px 14px;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            %s
                            <tr>
                              <td style="padding:6px 0;font-size:13px;color:#64748b;">Email</td>
                              <td style="padding:6px 0;font-size:13px;color:#0f172a;text-align:right;font-weight:500;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:6px 0;font-size:13px;color:#64748b;">Điện thoại</td>
                              <td style="padding:6px 0;font-size:13px;color:#0f172a;text-align:right;font-weight:500;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:6px 0;font-size:13px;color:#64748b;">Địa chỉ giao hàng</td>
                              <td style="padding:6px 0;font-size:13px;color:#0f172a;text-align:right;font-weight:500;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:6px 0;font-size:13px;color:#64748b;">Phương thức thanh toán</td>
                              <td style="padding:6px 0;font-size:13px;color:#0f172a;text-align:right;font-weight:500;">%s</td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- ORDER ITEMS -->
                <tr>
                  <td style="padding:24px 48px 0;">
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#f8fafc;border:1.5px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                      <tr>
                        <td style="padding:14px 20px;border-bottom:1px solid #e2e8f0;">
                          <p style="margin:0;font-size:11px;font-weight:700;letter-spacing:2px;
                                     text-transform:uppercase;color:#94a3b8;">SẢN PHẨM ĐÃ ĐẶT</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:0 20px;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <th style="padding:10px 0 6px;font-size:11px;color:#94a3b8;text-align:left;font-weight:600;border-bottom:1px solid #e2e8f0;">Sản phẩm</th>
                              <th style="padding:10px 0 6px;font-size:11px;color:#94a3b8;text-align:center;font-weight:600;border-bottom:1px solid #e2e8f0;">SL</th>
                              <th style="padding:10px 0 6px;font-size:11px;color:#94a3b8;text-align:right;font-weight:600;border-bottom:1px solid #e2e8f0;">Đơn giá</th>
                              <th style="padding:10px 0 6px;font-size:11px;color:#94a3b8;text-align:right;font-weight:600;border-bottom:1px solid #e2e8f0;">Thành tiền</th>
                            </tr>
                            %s
                          </table>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- SUMMARY + INVOICE BADGE -->
                <tr>
                  <td style="padding:20px 48px 0;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                      <tr>
                        <!-- Invoice badge trái -->
                        <td width="48%%" valign="top">
                          <div style="background:#f0fdf4;border:1.5px solid #bbf7d0;border-radius:10px;
                                      padding:14px 16px;display:inline-block;">
                            <p style="margin:0 0 4px;font-size:11px;color:#86efac;font-weight:700;letter-spacing:1px;text-transform:uppercase;">Hóa đơn đính kèm</p>
                            <p style="margin:0;font-size:13px;color:#16a34a;font-weight:600;">📎 &nbsp;%s</p>
                          </div>
                        </td>
                        <!-- Summary phải -->
                        <td width="4%%"></td>
                        <td width="48%%" valign="top">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            %s
                          </table>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- TOTAL -->
                <tr>
                  <td style="padding:16px 48px 0;">
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:linear-gradient(135deg,#052e16,#14532d);border-radius:10px;">
                      <tr>
                        <td style="padding:16px 20px;font-size:13px;font-weight:700;color:#86efac;
                                    text-transform:uppercase;letter-spacing:1px;">
                          Tổng thanh toán
                        </td>
                        <td style="padding:16px 20px;text-align:right;">
                          <span style="font-size:22px;font-weight:800;color:#fff;">%s</span>
                          <span style="font-size:15px;font-weight:600;color:#86efac;margin-left:4px;">₫</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- DIVIDER -->
                <tr>
                  <td style="padding:32px 48px 0;">
                    <hr style="border:none;border-top:1px solid #e2e8f0;margin:0;"/>
                  </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                  <td style="padding:24px 48px 36px;text-align:center;">
                    <p style="margin:0 0 6px;font-size:13px;font-weight:600;color:#475569;">
                      TechStore — Nơi công nghệ gặp gỡ niềm tin
                    </p>
                    <p style="margin:0;font-size:12px;color:#94a3b8;line-height:1.8;">
                      Nếu bạn có thắc mắc, hãy liên hệ
                      <a href="mailto:support@nguyenduc.me" style="color:#16a34a;text-decoration:none;">
                        support@nguyenduc.me
                      </a><br/>
                      Email này được gửi tự động, vui lòng không trả lời trực tiếp.
                    </p>
                    <p style="margin:16px 0 0;font-size:11px;color:#cbd5e1;">
                      © 2025 TechStore. All rights reserved.
                    </p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(
                deliveredAt,
                customerName,
                order.getOrderNumber(),
                invoiceName,
                orderNote,
                customerEmail,
                customerPhone,
                customerAddr,
                paymentMethod,
                itemRows,
                invoiceName,
                summaryRows,
                total
        );
    }
}