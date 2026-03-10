package com.retailmanagement.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.retailmanagement.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

            String formattedAmount = NumberFormat
                    .getNumberInstance(new Locale("vi", "VN"))
                    .format(order.getTotalAmount());

            String htmlContent = buildOrderConfirmationEmail(
                    order.getCustomer().getName(),
                    order.getOrderNumber(),
                    formattedAmount
            );

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

    private String buildOrderConfirmationEmail(String customerName,
                                               String orderNumber,
                                               String totalAmount) {
        return """
        <!DOCTYPE html>
        <html lang="vi">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>Xác nhận đơn hàng</title>
        </head>
        <body style="margin:0;padding:0;background-color:#f0f2f5;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">

          <!-- Wrapper -->
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f0f2f5;padding:40px 0;">
            <tr>
              <td align="center">
                <table width="600" cellpadding="0" cellspacing="0"
                       style="max-width:600px;width:100%%;background:#ffffff;border-radius:16px;
                              overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                  <!-- ── HEADER ── -->
                  <tr>
                    <td style="background:linear-gradient(135deg,#0f172a 0%%,#1e3a5f 60%%,#0ea5e9 100%%);
                                padding:40px 48px;text-align:center;">
                      <p style="margin:0 0 12px;font-size:13px;letter-spacing:4px;
                                 text-transform:uppercase;color:#7dd3fc;font-weight:600;">
                        TECHSTORE
                      </p>
                      <h1 style="margin:0;font-size:28px;font-weight:700;color:#ffffff;
                                  letter-spacing:-0.5px;line-height:1.2;">
                        Đơn hàng đã được xác nhận
                      </h1>
                      <p style="margin:12px 0 0;font-size:14px;color:#94a3b8;">
                        Cảm ơn bạn đã tin tưởng mua sắm tại TechStore
                      </p>
                    </td>
                  </tr>

                  <!-- ── SUCCESS BADGE ── -->
                  <tr>
                    <td style="padding:32px 48px 0;text-align:center;">
                      <div style="display:inline-block;background:#f0fdf4;
                                  border:1.5px solid #bbf7d0;border-radius:100px;
                                  padding:10px 24px;">
                        <span style="font-size:14px;font-weight:600;color:#16a34a;">
                          ✓ &nbsp;Thanh toán thành công
                        </span>
                      </div>
                    </td>
                  </tr>

                  <!-- ── GREETING ── -->
                  <tr>
                    <td style="padding:28px 48px 0;">
                      <p style="margin:0;font-size:16px;color:#334155;line-height:1.6;">
                        Xin chào <strong style="color:#0f172a;">%s</strong>,
                      </p>
                      <p style="margin:10px 0 0;font-size:15px;color:#475569;line-height:1.7;">
                        Đơn hàng của bạn đã được tiếp nhận và đang được xử lý.
                        Chúng tôi sẽ thông báo ngay khi hàng được giao đến bạn.
                      </p>
                    </td>
                  </tr>

                  <!-- ── ORDER INFO CARD ── -->
                  <tr>
                    <td style="padding:28px 48px 0;">
                      <table width="100%%" cellpadding="0" cellspacing="0"
                             style="background:#f8fafc;border:1.5px solid #e2e8f0;
                                    border-radius:12px;overflow:hidden;">
                        <tr>
                          <td style="padding:20px 24px;border-bottom:1px solid #e2e8f0;">
                            <p style="margin:0;font-size:11px;font-weight:700;letter-spacing:2px;
                                       text-transform:uppercase;color:#94a3b8;">
                              CHI TIẾT ĐƠN HÀNG
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:0 24px;">
                            <table width="100%%" cellpadding="0" cellspacing="0">
                              <tr>
                                <td style="padding:16px 0;border-bottom:1px solid #f1f5f9;">
                                  <span style="font-size:13px;color:#64748b;">Mã đơn hàng</span>
                                </td>
                                <td style="padding:16px 0;border-bottom:1px solid #f1f5f9;
                                            text-align:right;">
                                  <span style="font-size:13px;font-weight:700;color:#0f172a;
                                               background:#eff6ff;color:#1d4ed8;padding:3px 10px;
                                               border-radius:6px;">
                                    #%s
                                  </span>
                                </td>
                              </tr>
                              <tr>
                                <td style="padding:16px 0;">
                                  <span style="font-size:13px;color:#64748b;">Tổng thanh toán</span>
                                </td>
                                <td style="padding:16px 0;text-align:right;">
                                  <span style="font-size:20px;font-weight:800;color:#0ea5e9;">
                                    %s <span style="font-size:14px;font-weight:600;color:#64748b;">₫</span>
                                  </span>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <!-- ── DIVIDER ── -->
                  <tr>
                    <td style="padding:36px 48px 0;">
                      <hr style="border:none;border-top:1px solid #e2e8f0;margin:0;" />
                    </td>
                  </tr>

                  <!-- ── FOOTER ── -->
                  <tr>
                    <td style="padding:24px 48px 36px;text-align:center;">
                      <p style="margin:0 0 6px;font-size:13px;font-weight:600;color:#475569;">
                        TechStore — Nơi công nghệ gặp gỡ niềm tin
                      </p>
                      <p style="margin:0;font-size:12px;color:#94a3b8;line-height:1.8;">
                        Nếu bạn có thắc mắc, hãy liên hệ
                        <a href="mailto:support@nguyenduc.me"
                           style="color:#0ea5e9;text-decoration:none;">support@nguyenduc.me</a>
                        <br/>
                        Email này được gửi tự động, vui lòng không trả lời trực tiếp.
                      </p>
                      <p style="margin:16px 0 0;font-size:11px;color:#cbd5e1;">
                        © 2025 TechStore. All rights reserved.
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>

        </body>
        </html>
        """.formatted(customerName, orderNumber, totalAmount);
    }
}