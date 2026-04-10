<template>
  <footer class="rp-footer">
    <div class="footer-top">
      <div class="footer-inner footer-grid">

        <div class="footer-brand">
          <div class="brand-logo">
            <span class="logo-icon">⚡</span>
            <span class="logo-text">RetailPro <span class="logo-accent">TechStore</span></span>
          </div>
          <p class="brand-desc">
            Hệ thống bán lẻ laptop chính hãng hàng đầu — chất lượng được kiểm định,
            giá cả minh bạch, dịch vụ tận tâm.
          </p>
          <div class="social-links">
            <a href="#" class="social-btn" aria-label="Facebook">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M18 2h-3a5 5 0 00-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 011-1h3z"/></svg>
            </a>
            <a href="#" class="social-btn" aria-label="Zalo">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15v-4H7l5-8v4h4l-5 8z"/></svg>
            </a>
            <a href="#" class="social-btn" aria-label="YouTube">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M22.54 6.42a2.78 2.78 0 00-1.94-1.96C18.88 4 12 4 12 4s-6.88 0-8.6.46A2.78 2.78 0 001.46 6.42 29 29 0 001 12a29 29 0 00.46 5.58A2.78 2.78 0 003.4 19.54C5.12 20 12 20 12 20s6.88 0 8.6-.46a2.78 2.78 0 001.94-1.96A29 29 0 0023 12a29 29 0 00-.46-5.58zM9.75 15.02V8.98L15.5 12l-5.75 3.02z"/></svg>
            </a>
            <a href="#" class="social-btn" aria-label="TikTok">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M19.59 6.69a4.83 4.83 0 01-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 01-2.88 2.5 2.89 2.89 0 01-2.89-2.89 2.89 2.89 0 012.89-2.89c.28 0 .54.04.79.1V9.01a6.33 6.33 0 00-.79-.05 6.34 6.34 0 00-6.34 6.34 6.34 6.34 0 006.34 6.34 6.34 6.34 0 006.33-6.34v-7a8.16 8.16 0 004.77 1.52v-3.4a4.85 4.85 0 01-1-.73z"/></svg>
            </a>
          </div>
        </div>

        <div class="footer-col">
          <h3 class="col-title">Sản phẩm</h3>
          <ul class="col-links">
            <li><router-link to="/">Laptop Gaming</router-link></li>
            <li><router-link to="/">Laptop Văn phòng</router-link></li>
            <li><router-link to="/">MacBook</router-link></li>
            <li><router-link to="/">Laptop học sinh – SV</router-link></li>
            <li><router-link to="/">Laptop đồ họa</router-link></li>
            <li><router-link to="/">Phụ kiện & Linh kiện</router-link></li>
          </ul>
        </div>

        <div class="footer-col">
          <h3 class="col-title">Hỗ trợ</h3>
          <ul class="col-links">
            <li><a href="#">Tra cứu đơn hàng</a></li>
            <li><a href="#">Chính sách đổi trả</a></li>
            <li><a href="#">Bảo hành sản phẩm</a></li>
            <li><a href="#">Hướng dẫn mua hàng</a></li>
            <li><a href="#">Câu hỏi thường gặp</a></li>
            <li><a href="#">Liên hệ hỗ trợ</a></li>
          </ul>
        </div>

        <div class="footer-col">
          <h3 class="col-title">Liên hệ</h3>
          <ul class="contact-list">
            <li>
              <span class="contact-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
              </span>
              <span>Số 605 Kim Ngưu, Hà Nội</span>
            </li>
            <li>
              <span class="contact-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81 19.79 19.79 0 01.07 1.18 2 2 0 012 0h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L6.09 7.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z"/></svg>
              </span>
              <span>1900 1234</span>
            </li>
            <li>
              <span class="contact-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
              </span>
              <span>support@retailpro.vn</span>
            </li>
            <li>
              <span class="contact-icon">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </span>
              <span>T2 – T7: 8:00 – 21:00</span>
            </li>
          </ul>
          <div class="hotline-badge">
            <span class="hotline-label">Hotline miễn phí</span>
            <span class="hotline-num">1900 1234</span>
          </div>
        </div>

      </div>
    </div>

    <div class="footer-trust">
      <div class="footer-inner trust-row">
        <div class="trust-item">
          <span class="trust-icon">🔒</span>
          <div>
            <div class="trust-title">Thanh toán an toàn</div>
            <div class="trust-sub">SSL 256-bit</div>
          </div>
        </div>
        <div class="trust-item">
          <span class="trust-icon">🚚</span>
          <div>
            <div class="trust-title">Giao hàng toàn quốc</div>
            <div class="trust-sub">2 – 5 ngày làm việc</div>
          </div>
        </div>
        <div class="trust-item">
          <span class="trust-icon">🔄</span>
          <div>
            <div class="trust-title">Đổi trả miễn phí</div>
            <div class="trust-sub">Trong vòng 30 ngày</div>
          </div>
        </div>
        <div class="trust-item">
          <span class="trust-icon">🛡️</span>
          <div>
            <div class="trust-title">Bảo hành chính hãng</div>
            <div class="trust-sub">12 – 36 tháng</div>
          </div>
        </div>
        <div class="trust-item">
          <span class="trust-icon">💳</span>
          <div>
            <div class="trust-title">Trả góp 0%</div>
            <div class="trust-sub">Mọi ngân hàng lớn</div>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="footer-inner bottom-row">
        <span class="copyright">© {{ year }} RetailPro TechStore — DATN FPT University</span>
        <div class="legal-links">
          <a href="#">Điều khoản sử dụng</a>
          <span class="sep">·</span>
          <a href="#">Chính sách bảo mật</a>
          <span class="sep">·</span>
          <a href="#">Sitemap</a>
        </div>
        <div class="payment-icons">
          <span class="pay-badge">VISA</span>
          <span class="pay-badge">MasterCard</span>
          <span class="pay-badge">MoMo</span>
          <span class="pay-badge">ZaloPay</span>
          <span class="pay-badge">VNPay</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear();
</script>

<style scoped>
.rp-footer {
  margin-top: 64px;
  background: #0f172a;
  color: #cbd5e1;
}

.footer-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
}

/* ── Top Section ─── */
.footer-top { padding: 56px 0 40px; }
.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: 48px;
}

@media (max-width: 1024px) {
  .footer-grid { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 600px) {
  .footer-grid { grid-template-columns: 1fr; }
}

/* Brand */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.logo-icon { font-size: 22px; }
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.02em;
}
.logo-accent { color: #818cf8; }

.brand-desc {
  font-size: 13.5px;
  line-height: 1.7;
  color: #94a3b8;
  margin: 0 0 20px;
  max-width: 300px;
}

.social-links { display: flex; gap: 10px; }
.social-btn {
  width: 36px; height: 36px;
  border-radius: 8px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.08);
  color: #94a3b8;
  display: flex; align-items: center; justify-content: center;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}
.social-btn:hover { background: rgba(99,102,241,0.2); color: #818cf8; }

/* Columns */
.col-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #f8fafc;
  margin: 0 0 16px;
}

.col-links { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 9px; }
.col-links a {
  font-size: 13.5px;
  color: #94a3b8;
  text-decoration: none;
  transition: color 0.15s;
}
.col-links a:hover { color: #e2e8f0; }

/* Contact */
.contact-list { list-style: none; padding: 0; margin: 0 0 16px; display: flex; flex-direction: column; gap: 10px; }
.contact-list li { display: flex; align-items: flex-start; gap: 8px; font-size: 13.5px; color: #94a3b8; }
.contact-icon { color: #6366f1; flex-shrink: 0; margin-top: 1px; }

.hotline-badge {
  display: inline-flex;
  flex-direction: column;
  background: linear-gradient(135deg, rgba(99,102,241,0.15), rgba(139,92,246,0.1));
  border: 1px solid rgba(99,102,241,0.25);
  border-radius: 10px;
  padding: 10px 16px;
}
.hotline-label { font-size: 10px; font-weight: 600; letter-spacing: 0.08em; text-transform: uppercase; color: #818cf8; margin-bottom: 2px; }
.hotline-num { font-size: 18px; font-weight: 700; color: #f8fafc; letter-spacing: 0.02em; }

/* ── Trust Bar ─── */
.footer-trust {
  border-top: 1px solid rgba(255,255,255,0.07);
  border-bottom: 1px solid rgba(255,255,255,0.07);
  padding: 20px 0;
  background: rgba(255,255,255,0.02);
}
.trust-row {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}
.trust-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.trust-icon { font-size: 22px; }
.trust-title { font-size: 13px; font-weight: 600; color: #e2e8f0; line-height: 1.3; }
.trust-sub { font-size: 11.5px; color: #64748b; }

/* ── Bottom ─── */
.footer-bottom { padding: 18px 0; }
.bottom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.copyright { font-size: 12.5px; color: #475569; }

.legal-links { display: flex; align-items: center; gap: 8px; }
.legal-links a { font-size: 12.5px; color: #475569; text-decoration: none; transition: color 0.15s; }
.legal-links a:hover { color: #94a3b8; }
.sep { color: #334155; }

.payment-icons { display: flex; gap: 6px; flex-wrap: wrap; }
.pay-badge {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.05em;
  color: #64748b;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 5px;
  padding: 3px 8px;
}
</style>