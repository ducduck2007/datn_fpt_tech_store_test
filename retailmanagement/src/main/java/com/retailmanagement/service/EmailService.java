package com.retailmanagement.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.retailmanagement.entity.Order;
import com.retailmanagement.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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

            String htmlContent = buildOrderConfirmationEmail(order);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("TechStore <noreply@nguyenduc.me>")
                    .to(order.getCustomer().getEmail())
                    .subject("✅ Xác nhận đơn hàng #" + order.getOrderNumber())
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendSecurityAlertToAdmins(
            List<String> emails,
            String subject,
            String content
    ) {
        try {
            if (emails == null || emails.isEmpty()) return;

            // Tránh duplicate email
            emails = emails.stream().distinct().toList();

            Resend resend = new Resend(apiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Security <security@nguyenduc.me>")
                    .to(emails.toArray(new String[0]))
                    .subject(subject)
                    .html(content)
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

    // ── Build item rows ───────────────────────────────────────────────────
    private String buildItemRows(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (OrderItem item : order.getOrderItems()) {
            String unitPrice = fmt(item.getUnitPrice()) + " ₫";
            String lineTotal = fmt(item.getLineTotal()) + " ₫";
            sb.append("""
              <tr>
                <td style="padding:12px 0;border-bottom:1px solid #f1f5f9;
                            font-size:14px;color:#334155;line-height:1.5;">
                  <span style="font-weight:600;color:#0f172a;">%s</span>
                  %s
                </td>
                <td style="padding:12px 0;border-bottom:1px solid #f1f5f9;
                            text-align:center;font-size:13px;color:#64748b;white-space:nowrap;">
                  x%d
                </td>
                <td style="padding:12px 0;border-bottom:1px solid #f1f5f9;
                            text-align:right;font-size:13px;color:#64748b;white-space:nowrap;">
                  %s
                </td>
                <td style="padding:12px 0;border-bottom:1px solid #f1f5f9;
                            text-align:right;font-size:13px;font-weight:700;
                            color:#0f172a;white-space:nowrap;">
                  %s
                </td>
              </tr>
            """.formatted(
                    item.getProductName(),
                    "",
                    item.getQuantity(),
                    unitPrice,
                    lineTotal
            ));
        }
        return sb.toString();
    }

    // ── Build discount rows ───────────────────────────────────────────────
    private String buildDiscountRows(Order order) {
        StringBuilder sb = new StringBuilder();
        if (order.getSubtotal() != null) {
            sb.append(summaryRow("Tạm tính", fmt(order.getSubtotal()) + " ₫", "#475569", false));
        }
        if (isPositive(order.getShippingFee())) {
            sb.append(summaryRow("Phí vận chuyển", fmt(order.getShippingFee()) + " ₫", "#475569", false));
        }
        if (isPositive(order.getVipDiscount())) {
            sb.append(summaryRow("Giảm VIP", "- " + fmt(order.getVipDiscount()) + " ₫", "#16a34a", false));
        }
        if (isPositive(order.getSpinDiscount())) {
            sb.append(summaryRow("Giảm Vòng quay", "- " + fmt(order.getSpinDiscount()) + " ₫", "#16a34a", false));
        }
        if (order.getAppliedPromotionCode() != null) {
            sb.append(summaryRow("Mã khuyến mãi", order.getAppliedPromotionCode(), "#16a34a", false));
        }
        if (isPositive(order.getDiscountTotal())) {
            sb.append(summaryRow("Tổng giảm", "- " + fmt(order.getDiscountTotal()) + " ₫", "#16a34a", true));
        }
        return sb.toString();
    }

    private String summaryRow(String label, String value, String valueColor, boolean topBorder) {
        String borderStyle = topBorder ? "border-top:1px solid #e2e8f0;margin-top:8px;padding-top:8px;" : "";
        return """
          <tr>
            <td style="padding:5px 0;font-size:13px;color:#64748b;%s">%s</td>
            <td style="padding:5px 0;font-size:13px;color:%s;text-align:right;font-weight:600;%s">%s</td>
          </tr>
        """.formatted(borderStyle, label, valueColor, borderStyle, value);
    }

    // ── Main HTML builder ─────────────────────────────────────────────────
    private String buildOrderConfirmationEmail(Order order) {
        String customerName   = safe(order.getCustomer().getName(), "Khách hàng");
        String customerEmail  = safe(order.getCustomer().getEmail(), "—");
        String customerPhone  = safe(order.getCustomer().getPhone() != null ? order.getCustomer().getPhone() : null, "—");
        String customerAddr   = safe(order.getCustomer().getAddress() != null ? order.getCustomer().getAddress() : null, "—");
        String paymentMethod  = safe(order.getPaymentMethod(), "—");
        String orderNote      = order.getNotes() != null && !order.getNotes().isBlank()
                ? "<tr><td colspan=\"2\" style=\"padding:10px 0 4px;\"><div style=\"border-left:3px solid #0ea5e9;padding-left:12px;font-size:13px;color:#475569;background:#f0f9ff;padding:10px 12px;border-radius:6px;\"><strong>Ghi chú:</strong> " + order.getNotes() + "</div></td></tr>"
                : "";

        String createdAt = order.getCreatedAt() != null
                ? order.getCreatedAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"))
                : "—";

        String itemRows    = buildItemRows(order);
        String discountRows = buildDiscountRows(order);
        String totalAmount  = fmt(order.getTotalAmount());

        return """
        <!DOCTYPE html>
        <html lang="vi">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
          <title>Xác nhận đơn hàng</title>
        </head>
        <body style="margin:0;padding:0;background:#f0f2f5;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0f2f5;padding:40px 0;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0"
                     style="max-width:600px;width:100%%;background:#fff;border-radius:16px;
                            overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                <!-- HEADER -->
                <tr>
                  <td style="background:linear-gradient(135deg,#0f172a 0%%,#1e3a5f 60%%,#0ea5e9 100%%);
                              padding:40px 48px;text-align:center;">
                    <p style="margin:0 0 12px;font-size:13px;letter-spacing:4px;text-transform:uppercase;
                               color:#7dd3fc;font-weight:600;">TECHSTORE</p>
                    <h1 style="margin:0;font-size:26px;font-weight:700;color:#fff;line-height:1.2;">
                      Đơn hàng đã được xác nhận ✅
                    </h1>
                    <p style="margin:10px 0 0;font-size:14px;color:#94a3b8;">
                      Cảm ơn bạn đã tin tưởng mua sắm tại TechStore
                    </p>
                  </td>
                </tr>

                <!-- BADGE -->
                <tr>
                  <td style="padding:28px 48px 0;text-align:center;">
                    <div style="display:inline-block;background:#f0fdf4;border:1.5px solid #bbf7d0;
                                border-radius:100px;padding:10px 24px;">
                      <span style="font-size:14px;font-weight:600;color:#16a34a;">
                        ✓ &nbsp;Thanh toán thành công — %s
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
                      Đơn hàng <strong>#%s</strong> của bạn đã được tiếp nhận lúc <strong>%s</strong>.
                      Chúng tôi sẽ thông báo ngay khi hàng được giao đến bạn.
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
                        <td style="padding:4px 20px 12px;">
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
                              <td style="padding:6px 0;font-size:13px;color:#0f172a;text-align:right;font-weight:500;max-width:260px;">%s</td>
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

                <!-- ORDER SUMMARY -->
                <tr>
                  <td style="padding:24px 48px 0;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td width="50%%"></td>
                        <td width="50%%">
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
                           style="background:linear-gradient(135deg,#0f172a,#1e3a5f);border-radius:10px;">
                      <tr>
                        <td style="padding:16px 20px;font-size:13px;font-weight:700;color:#94a3b8;
                                    text-transform:uppercase;letter-spacing:1px;">
                          Tổng thanh toán
                        </td>
                        <td style="padding:16px 20px;text-align:right;">
                          <span style="font-size:22px;font-weight:800;color:#fff;">%s</span>
                          <span style="font-size:15px;font-weight:600;color:#7dd3fc;margin-left:4px;">₫</span>
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
                      <a href="mailto:support@nguyenduc.me" style="color:#0ea5e9;text-decoration:none;">
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
                createdAt,
                customerName,
                order.getOrderNumber(),
                createdAt,
                orderNote,
                customerEmail,
                customerPhone,
                customerAddr,
                paymentMethod,
                itemRows,
                discountRows,
                totalAmount
        );
    }
    @Async
    public void sendWelcomePasswordEmail(String toEmail, String fullName, String phone, String rawPassword) {
        try {
            if (toEmail == null || toEmail.isBlank()) return;

            Resend resend = new Resend(apiKey);

            String html = buildWelcomeEmail(fullName, phone, rawPassword);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("TechStore <noreply@nguyenduc.me>")
                    .to(toEmail)
                    .subject("🎉 Chào mừng bạn đến với TechStore — Thông tin đăng nhập")
                    .html(html)
                    .build();

            resend.emails().send(params);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildWelcomeEmail(String fullName, String phone, String rawPassword) {
        String safeName = (fullName != null && !fullName.isBlank()) ? fullName : "Khách hàng";
        String safePhone = (phone != null && !phone.isBlank()) ? phone : "—";

        return """
    <!DOCTYPE html>
    <html lang="vi">
    <head>
      <meta charset="UTF-8"/>
      <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
      <title>Chào mừng đến TechStore</title>
    </head>
    <body style="margin:0;padding:0;background:#f0f2f5;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
      <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0f2f5;padding:40px 0;">
        <tr><td align="center">
          <table width="600" cellpadding="0" cellspacing="0"
                 style="max-width:600px;width:100%%;background:#fff;border-radius:16px;
                        overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">

            <!-- HEADER -->
            <tr>
              <td style="background:linear-gradient(135deg,#0f172a 0%%,#1e3a5f 60%%,#0ea5e9 100%%);
                          padding:40px 48px;text-align:center;">
                <p style="margin:0 0 12px;font-size:13px;letter-spacing:4px;text-transform:uppercase;
                           color:#7dd3fc;font-weight:600;">TECHSTORE</p>
                <h1 style="margin:0;font-size:26px;font-weight:700;color:#fff;line-height:1.2;">
                  Chào mừng bạn! 🎉
                </h1>
                <p style="margin:10px 0 0;font-size:14px;color:#94a3b8;">
                  Tài khoản của bạn đã được tạo thành công
                </p>
              </td>
            </tr>

            <!-- GREETING -->
            <tr>
              <td style="padding:32px 48px 0;">
                <p style="margin:0;font-size:16px;color:#334155;line-height:1.6;">
                  Xin chào <strong style="color:#0f172a;">%s</strong>,
                </p>
                <p style="margin:10px 0 0;font-size:15px;color:#475569;line-height:1.7;">
                  Nhân viên TechStore vừa tạo tài khoản cho bạn. Dưới đây là thông tin đăng nhập của bạn.
                  Vui lòng <strong>đổi mật khẩu</strong> sau khi đăng nhập lần đầu để bảo mật tài khoản.
                </p>
              </td>
            </tr>

            <!-- CREDENTIALS BOX -->
            <tr>
              <td style="padding:28px 48px 0;">
                <table width="100%%" cellpadding="0" cellspacing="0"
                       style="background:#f8fafc;border:1.5px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                  <tr>
                    <td style="padding:14px 20px;border-bottom:1px solid #e2e8f0;">
                      <p style="margin:0;font-size:11px;font-weight:700;letter-spacing:2px;
                                 text-transform:uppercase;color:#94a3b8;">THÔNG TIN ĐĂNG NHẬP</p>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:8px 20px 16px;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td style="padding:10px 0;font-size:13px;color:#64748b;border-bottom:1px solid #f1f5f9;">
                            Tên đăng nhập
                          </td>
                          <td style="padding:10px 0;font-size:15px;font-weight:700;color:#0f172a;
                                      text-align:right;border-bottom:1px solid #f1f5f9;letter-spacing:0.5px;">
                            %s
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:10px 0;font-size:13px;color:#64748b;">
                            Mật khẩu tạm thời
                          </td>
                          <td style="padding:10px 0;text-align:right;">
                            <span style="font-size:17px;font-weight:800;color:#0f172a;
                                          font-family:monospace;letter-spacing:2px;
                                          background:#fef9c3;padding:4px 12px;border-radius:6px;
                                          border:1px solid #fde68a;">
                              %s
                            </span>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>

            <!-- WARNING -->
            <tr>
              <td style="padding:20px 48px 0;">
                <table width="100%%" cellpadding="0" cellspacing="0"
                       style="background:#fff7ed;border:1.5px solid #fed7aa;border-radius:10px;">
                  <tr>
                    <td style="padding:14px 18px;">
                      <p style="margin:0;font-size:13px;color:#c2410c;line-height:1.6;">
                        ⚠️ <strong>Lưu ý bảo mật:</strong> Đây là mật khẩu tạm thời do nhân viên tạo.
                        Vui lòng đăng nhập và đổi mật khẩu ngay để bảo vệ tài khoản của bạn.
                        Không chia sẻ thông tin này với bất kỳ ai.
                      </p>
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
                  <a href="mailto:support@nguyenduc.me" style="color:#0ea5e9;text-decoration:none;">
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
    """.formatted(safeName, safePhone, rawPassword);
    }
}