<template>
  <div class="db-page">
    <div class="db-inner">

      <!-- ── Header ── -->
      <header class="db-header">
        <div class="db-header__left">
          <div class="db-eyebrow">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <rect x="2" y="3" width="6" height="6" rx="1"/><rect x="9" y="3" width="6" height="6" rx="1"/><rect x="16" y="3" width="6" height="6" rx="1"/>
              <rect x="2" y="11" width="6" height="6" rx="1"/><rect x="9" y="11" width="6" height="6" rx="1"/>
            </svg>
            System
          </div>
          <h1 class="db-title">Admin <span class="db-title__accent">Dashboard</span></h1>
          <p class="db-subtitle">Truy cập nhanh các module quản trị</p>
        </div>
        <div class="db-header__actions">
          <button class="db-btn db-btn--outline" @click="reload" :disabled="loadingRevenue">
            <svg v-if="!loadingRevenue" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
              <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
              <path d="M8 16H3v5"/>
            </svg>
            <span v-if="loadingRevenue" class="db-spinner"></span>
            <span>{{ loadingRevenue ? 'Đang tải…' : 'Làm mới' }}</span>
          </button>
        </div>
      </header>

      <!-- ── Module cards ── -->
      <div class="db-modules-grid">
        <button
          v-for="c in cards"
          :key="c.title"
          class="db-module-card"
          @click="$router.push(c.to)"
        >
          <div class="db-module-icon" :style="{ background: c.color + '18', color: c.color }">
            <span>{{ c.icon }}</span>
          </div>
          <div class="db-module-info">
            <div class="db-module-title">{{ c.title }}</div>
            <div class="db-module-desc">{{ c.desc }}</div>
          </div>
          <svg class="db-module-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </button>
      </div>

      <!-- ══════════════════════════════════════
           LOYALTY RESET SECTION
      ══════════════════════════════════════ -->
      <div class="db-card">
        <!-- Card header -->
        <div class="db-card__header">
          <div>
            <div class="db-eyebrow">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/>
              </svg>
              Quản lý điểm thưởng
            </div>
            <h2 class="db-card__title">Loyalty Reset</h2>
            <p class="db-card__sub">Thao tác hạ hạng &amp; reset điểm thủ công — chỉ dành cho Admin</p>
          </div>
          <div class="db-admin-badge">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            Admin only
          </div>
        </div>

        <!-- Reset action boxes -->
        <div class="db-reset-grid">

          <!-- Monthly -->
          <div class="db-reset-box db-reset-box--orange">
            <div class="db-reset-box__emoji">📅</div>
            <div class="db-reset-box__body">
              <div class="db-reset-box__title">Kiểm tra không hoạt động tháng</div>
              <div class="db-reset-box__desc">
                Hạ 1 bậc tier với khách hàng không có giao dịch trong <strong>30 ngày qua</strong>.
              </div>
              <div class="db-rule-chips">
                <span class="db-rule-chip">DIAMOND → PLATINUM</span>
                <span class="db-rule-chip">PLATINUM → GOLD</span>
                <span class="db-rule-chip">GOLD → SILVER</span>
                <span class="db-rule-chip">SILVER → BRONZE</span>
                <span class="db-rule-chip db-rule-chip--muted">BRONZE → giữ nguyên</span>
              </div>
            </div>
            <button
              class="db-reset-btn db-reset-btn--orange"
              :disabled="loadingMonthly"
              @click="confirmMonthly"
            >
              <svg v-if="!loadingMonthly" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/>
              </svg>
              <span v-else class="db-spinner db-spinner--white"></span>
              {{ loadingMonthly ? 'Đang chạy…' : 'Chạy ngay' }}
            </button>
          </div>

          <!-- Year-End -->
          <div class="db-reset-box db-reset-box--red">
            <div class="db-reset-box__emoji">🎯</div>
            <div class="db-reset-box__body">
              <div class="db-reset-box__title">Reset cuối năm</div>
              <div class="db-reset-box__desc">
                Hạ 1 bậc tier <strong>toàn bộ</strong> khách hàng VIP và reset điểm về mức tối thiểu của hạng mới.
              </div>
              <div class="db-rule-chips">
                <span class="db-rule-chip">DIAMOND → PLATINUM · 10.000đ</span>
                <span class="db-rule-chip">PLATINUM → GOLD · 5.000đ</span>
                <span class="db-rule-chip">GOLD → SILVER · 2.000đ</span>
                <span class="db-rule-chip">SILVER → BRONZE · 1.000đ</span>
                <span class="db-rule-chip">BRONZE → BRONZE · 1.000đ</span>
              </div>
            </div>
            <button
              class="db-reset-btn db-reset-btn--red"
              :disabled="loadingYearEnd"
              @click="confirmYearEnd"
            >
              <svg v-if="!loadingYearEnd" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <span v-else class="db-spinner db-spinner--white"></span>
              {{ loadingYearEnd ? 'Đang chạy…' : 'Reset cuối năm' }}
            </button>
          </div>

        </div>

        <!-- Result message -->
        <transition name="db-result-fade">
          <div v-if="resetResult" class="db-result" :class="'db-result--' + resetResult.type">
            <svg v-if="resetResult.type === 'success'" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {{ resetResult.message }}
          </div>
        </transition>
      </div>

      <!-- ══════════════════════════════════════
           REVENUE SECTION
      ══════════════════════════════════════ -->
      <div class="db-card">
        <!-- Card header -->
        <div class="db-card__header">
          <div>
            <div class="db-eyebrow">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
              </svg>
              Doanh thu theo khách hàng
            </div>
            <h2 class="db-card__title">
              <span class="db-count-num">{{ revenueList.length }}</span>
              khách hàng
            </h2>
          </div>
          <div class="db-total-box">
            <div class="db-total-label">Tổng doanh thu</div>
            <div class="db-total-value">{{ totalRevenue.toLocaleString() }} đ</div>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="loadingRevenue" class="db-loading">
          <div class="db-loader-ring"></div>
          <span>Đang tải dữ liệu…</span>
        </div>

        <!-- Empty -->
        <div v-else-if="revenueList.length === 0" class="db-empty">
          <div class="db-empty__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4">
              <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
            </svg>
          </div>
          <p>Không có dữ liệu doanh thu</p>
        </div>

        <!-- Table -->
        <div v-else class="db-table-wrap">
          <table class="db-table">
            <thead>
              <tr>
                <th style="width:52px; text-align:center">#</th>
                <th>Khách hàng</th>
                <th style="text-align:right">Doanh thu</th>
                <th style="text-align:right; width:110px">Tỷ trọng</th>
                <th style="width:200px">Tỷ lệ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, idx) in revenueList" :key="idx" class="db-row">
                <td style="text-align:center">
                  <span class="db-idx" :class="idx < 3 ? 'db-idx--top' : ''">{{ idx + 1 }}</span>
                </td>
                <td>
                  <div class="db-customer-cell">
                    <div class="db-avatar" :style="{ background: getAvatarColor(row.customerName) }">
                      {{ (row.customerName || '?').charAt(0).toUpperCase() }}
                    </div>
                    <span class="db-customer-name">{{ row.customerName }}</span>
                  </div>
                </td>
                <td style="text-align:right">
                  <span class="db-money">{{ row.totalRevenue?.toLocaleString() }} đ</span>
                </td>
                <td style="text-align:right">
                  <span class="db-pct">
                    {{ totalRevenue ? ((row.totalRevenue / totalRevenue) * 100).toFixed(1) : 0 }}%
                  </span>
                </td>
                <td style="padding-right:20px">
                  <div class="db-bar-track">
                    <div
                      class="db-bar-fill"
                      :style="{ width: totalRevenue ? ((row.totalRevenue / totalRevenue) * 100) + '%' : '0%' }"
                    ></div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div><!-- /inner -->

    <!-- ══════════════════════════════════════
         CONFIRM DIALOG
    ══════════════════════════════════════ -->
    <Teleport to="body">
      <transition name="db-dlg-fade">
        <div v-if="confirmDialog.show" class="db-overlay" @click.self="confirmDialog.show = false">
          <div class="db-dialog" :class="'db-dialog--' + confirmDialog.type">

            <!-- Band -->
            <div class="db-dialog__band" :class="'db-dialog__band--' + confirmDialog.type">
              <div class="db-band-circles">
                <span class="db-band-c db-band-c--1"></span>
                <span class="db-band-c db-band-c--2"></span>
                <span class="db-band-c db-band-c--3"></span>
              </div>
              <div class="db-dialog__icon-ring">
                <span style="font-size:28px; line-height:1">{{ confirmDialog.icon }}</span>
              </div>
            </div>

            <div class="db-dialog__body">
              <span class="db-dialog__badge" :class="'db-dialog__badge--' + confirmDialog.type">
                {{ confirmDialog.type === 'red' ? 'Thao tác nguy hiểm' : 'Xác nhận thao tác' }}
              </span>
              <h2 class="db-dialog__title">{{ confirmDialog.title }}</h2>
              <p class="db-dialog__desc">{{ confirmDialog.body }}</p>
              <div class="db-dialog__notice db-dialog__notice--red">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                Thao tác này không thể hoàn tác!
              </div>
              <div class="db-dialog__actions">
                <button class="db-btn db-btn--ghost" @click="confirmDialog.show = false">Hủy bỏ</button>
                <button
                  class="db-btn"
                  :class="confirmDialog.type === 'red' ? 'db-btn--danger' : 'db-btn--orange'"
                  @click="confirmDialog.onConfirm(); confirmDialog.show = false"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  Xác nhận
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from "vue";
import { ordersApi }    from "../../api/orders.api";
import { customersApi } from "../../api/customers.api";
import { toast }        from "../../ui/toast";

// ── Revenue ────────────────────────────────────────────────────────────────
const revenueList    = ref([]);
const loadingRevenue = ref(false);

const totalRevenue = computed(() =>
  revenueList.value.reduce((sum, i) => sum + (i.totalRevenue || 0), 0)
);

const loadRevenueByCustomer = async () => {
  loadingRevenue.value = true;
  try {
    const res = await ordersApi.getRevenueByCustomer();
    revenueList.value = res.data || [];
  } finally {
    loadingRevenue.value = false;
  }
};

onMounted(() => loadRevenueByCustomer());

function reload() {
  loadRevenueByCustomer();
  toast("Dashboard đã làm mới.", "info");
}

// ── Loyalty Reset ──────────────────────────────────────────────────────────
const loadingMonthly = ref(false);
const loadingYearEnd = ref(false);
const resetResult    = ref(null);
let resultTimer = null;

function showResult(type, message) {
  resetResult.value = { type, message };
  clearTimeout(resultTimer);
  resultTimer = setTimeout(() => (resetResult.value = null), 5000);
}

async function runMonthlyReset() {
  loadingMonthly.value = true;
  resetResult.value = null;
  try {
    const res = await customersApi.triggerMonthlyReset();
    showResult("success", res.data?.message || "Kiểm tra inactivity tháng hoàn tất!");
    toast("Monthly reset hoàn tất.", "success");
  } catch (e) {
    const msg = e?.response?.data?.message || "Có lỗi xảy ra khi chạy monthly reset.";
    showResult("error", msg);
    toast(msg, "error");
  } finally {
    loadingMonthly.value = false;
  }
}

async function runYearEndReset() {
  loadingYearEnd.value = true;
  resetResult.value = null;
  try {
    const res = await customersApi.triggerYearEndReset();
    showResult("success", res.data?.message || "Reset cuối năm hoàn tất!");
    toast("Year-end reset hoàn tất.", "success");
  } catch (e) {
    const msg = e?.response?.data?.message || "Có lỗi xảy ra khi chạy year-end reset.";
    showResult("error", msg);
    toast(msg, "error");
  } finally {
    loadingYearEnd.value = false;
  }
}

// ── Confirm Dialog ─────────────────────────────────────────────────────────
const confirmDialog = reactive({
  show: false, type: "orange", icon: "📅",
  title: "", body: "", onConfirm: () => {},
});

function confirmMonthly() {
  Object.assign(confirmDialog, {
    show: true, type: "orange", icon: "📅",
    title: "Xác nhận chạy Monthly Inactivity Check?",
    body: "Tất cả khách hàng VIP không có giao dịch trong 30 ngày qua sẽ bị hạ 1 bậc tier và điểm sẽ được reset về mức tối thiểu của hạng mới.",
    onConfirm: runMonthlyReset,
  });
}

function confirmYearEnd() {
  Object.assign(confirmDialog, {
    show: true, type: "red", icon: "⚠️",
    title: "Xác nhận Reset Cuối Năm?",
    body: "TOÀN BỘ khách hàng có VIP tier sẽ bị hạ 1 bậc và điểm sẽ được reset. Đây là thao tác không thể hoàn tác!",
    onConfirm: runYearEndReset,
  });
}

// ── Avatar color helper ────────────────────────────────────────────────────
const AVATAR_COLORS = ["#2563eb","#16a34a","#d97706","#7c3aed","#0891b2","#db2777","#dc2626","#0f766e"];
function getAvatarColor(name) {
  if (!name) return "#6b7280";
  return AVATAR_COLORS[name.charCodeAt(0) % AVATAR_COLORS.length];
}

// ── Cards ──────────────────────────────────────────────────────────────────
const cards = [
  { title: "Users",               desc: "Quản lý tài khoản & phân quyền",   to: "/system/users",               icon: "👤", color: "#2563eb" },
  { title: "Customers",           desc: "Khách hàng & hạng thành viên",      to: "/system/customers",           icon: "🧑‍🤝‍🧑", color: "#16a34a" },
  { title: "Categories",          desc: "Danh mục sản phẩm",                 to: "/system/categories",          icon: "🗂️", color: "#d97706" },
  { title: "Products",            desc: "Tạo & quản lý sản phẩm",           to: "/system/products",            icon: "📦", color: "#7c3aed" },
  { title: "Orders — New",        desc: "Đơn hàng mới cần xử lý",           to: "/system/orders/new",          icon: "🆕", color: "#0891b2" },
  { title: "Orders — Processing", desc: "Hàng đợi đang xử lý",              to: "/system/orders/processing",   icon: "⚙️", color: "#ea580c" },
  { title: "Loyalty Summary",     desc: "Tổng hợp điểm thưởng",             to: "/system/Loyaltysummaryadmin", icon: "⭐", color: "#ca8a04" },
  { title: "Pricing",             desc: "Giá biến thể & lịch sử",           to: "/system/pricing",             icon: "💰", color: "#16a34a" },
  { title: "Promotions",          desc: "Mã giảm giá & chiến dịch",         to: "/system/promotions",          icon: "🎁", color: "#db2777" },
  { title: "Payment History",     desc: "Tất cả giao dịch thanh toán",       to: "/system/payments",            icon: "🧾", color: "#2563eb" },
  { title: "Customer Spending",   desc: "Chi tiêu theo khách hàng",          to: "/system/customer-spending",   icon: "📊", color: "#0891b2" },
  { title: "Settings",            desc: "Đơn vị tiền tệ mặc định",          to: "/system/settings/currency",   icon: "⚙️", color: "#6b7280" },
];
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

/*
  HARDCODE MAP — không dùng var()
  bg:#f6f7f9  card:#ffffff  border:#e4e7ec  border-light:#f0f2f5
  text:#0f1117  muted:#6b7280  subtle:#9ca3af
  blue:#2563eb / bg:#eff6ff / bdr:#bfdbfe
  green:#16a34a / bg:#f0fdf4 / bdr:#bbf7d0
  red:#dc2626 / bg:#fff1f2 / bdr:#fecdd3
  orange:#d97706 / bg:#fffbeb / bdr:#fde68a
  purple:#7c3aed / bg:#f5f3ff / bdr:#ddd6fe
  radius:12px  radius-sm:8px
  shadow-sm: 0 1px 3px rgba(0,0,0,.06),0 1px 2px rgba(0,0,0,.04)
  shadow-lg: 0 24px 64px rgba(0,0,0,.14),0 8px 24px rgba(0,0,0,.06)
*/

.db-page {
  font-family: 'Plus Jakarta Sans', sans-serif;
  background: #f6f7f9;
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: #0f1117;
  box-sizing: border-box;
}
*, *::before, *::after { box-sizing: border-box; }
.db-inner { max-width: 1440px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }

/* ── Header ── */
.db-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; flex-wrap: wrap; }
.db-eyebrow { display: inline-flex; align-items: center; gap: 5px; font-size: 11px; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; color: #6b7280; margin-bottom: 6px; }
.db-title { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; margin: 0 0 4px; line-height: 1.2; }
.db-title__accent { color: #2563eb; }
.db-subtitle { font-size: 13.5px; color: #6b7280; margin: 0; }
.db-header__actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding-top: 6px; }

/* ── Buttons ── */
.db-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px; font-size: 13px; font-weight: 700;
  border-radius: 8px; border: none; cursor: pointer;
  transition: all 0.18s ease; font-family: 'Plus Jakarta Sans', sans-serif; white-space: nowrap;
}
.db-btn--outline { background: #ffffff; color: #0f1117; border: 1.5px solid #e4e7ec; }
.db-btn--outline:hover:not(:disabled) { border-color: #9ca3af; background: #f9fafb; }
.db-btn--outline:disabled { opacity: 0.5; cursor: not-allowed; }
.db-btn--ghost { background: transparent; color: #6b7280; border: 1.5px solid #e4e7ec; }
.db-btn--ghost:hover { background: #f0f2f5; color: #0f1117; }
.db-btn--danger { background: linear-gradient(135deg, #b91c1c, #dc2626); color: #fff; box-shadow: 0 4px 12px rgba(220,38,38,0.3); }
.db-btn--danger:hover { background: linear-gradient(135deg, #991b1b, #b91c1c); transform: translateY(-1px); }
.db-btn--orange { background: linear-gradient(135deg, #b45309, #d97706); color: #fff; box-shadow: 0 4px 12px rgba(217,119,6,0.3); }
.db-btn--orange:hover { background: linear-gradient(135deg, #92400e, #b45309); transform: translateY(-1px); }

/* ── Module cards ── */
.db-modules-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px;
}
.db-module-card {
  display: flex; align-items: center; gap: 14px; padding: 16px 18px;
  background: #ffffff; border: 1.5px solid #e4e7ec; border-radius: 12px;
  cursor: pointer; text-align: left; transition: all 0.15s;
  font-family: 'Plus Jakarta Sans', sans-serif; width: 100%;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}
.db-module-card:hover {
  border-color: #bfdbfe; background: #f8faff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(37,99,235,0.08), 0 2px 6px rgba(0,0,0,0.04);
}
.db-module-icon {
  width: 44px; height: 44px; border-radius: 11px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.db-module-info { flex: 1; min-width: 0; }
.db-module-title { font-size: 13.5px; font-weight: 700; color: #0f1117; }
.db-module-desc { font-size: 12px; color: #9ca3af; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.db-module-arrow { color: #d1d5db; flex-shrink: 0; transition: transform 0.15s, color 0.15s; }
.db-module-card:hover .db-module-arrow { color: #2563eb; transform: translateX(3px); }

/* ── Card container ── */
.db-card {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}
.db-card__header {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 16px; padding: 22px 24px 18px; flex-wrap: wrap;
  border-bottom: 1px solid #f0f2f5;
}
.db-card__title { font-size: 18px; font-weight: 800; color: #0f1117; margin: 4px 0 3px; letter-spacing: -0.02em; }
.db-card__sub { font-size: 12.5px; color: #9ca3af; margin: 0; }

/* Admin badge */
.db-admin-badge {
  display: inline-flex; align-items: center; gap: 6px;
  background: #fff1f2; border: 1px solid #fecdd3; color: #dc2626;
  font-size: 11px; font-weight: 700; padding: 5px 12px;
  border-radius: 999px; white-space: nowrap; flex-shrink: 0;
}

/* ── Reset grid ── */
.db-reset-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; padding: 20px 24px; }
@media (max-width: 768px) { .db-reset-grid { grid-template-columns: 1fr; } }

.db-reset-box {
  display: flex; flex-direction: column; gap: 12px;
  padding: 18px; border-radius: 12px; border: 1.5px solid;
}
.db-reset-box--orange { background: #fffbeb; border-color: #fde68a; }
.db-reset-box--red    { background: #fff1f2; border-color: #fecdd3; }
.db-reset-box__emoji  { font-size: 28px; line-height: 1; }
.db-reset-box__body   { flex: 1; }
.db-reset-box__title  { font-size: 14px; font-weight: 700; color: #0f1117; margin-bottom: 6px; }
.db-reset-box__desc   { font-size: 12.5px; color: #6b7280; line-height: 1.65; margin-bottom: 12px; }
.db-reset-box__desc strong { color: #374151; }

.db-rule-chips { display: flex; flex-wrap: wrap; gap: 5px; }
.db-rule-chip {
  font-size: 10.5px; font-weight: 600; padding: 3px 9px; border-radius: 999px;
  background: rgba(0,0,0,0.06); color: #374151; white-space: nowrap;
}
.db-rule-chip--muted { opacity: 0.5; }

/* Reset buttons */
.db-reset-btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 7px;
  padding: 9px 20px; border: none; border-radius: 8px;
  font-family: 'Plus Jakarta Sans', sans-serif; font-size: 13px; font-weight: 700;
  cursor: pointer; transition: all 0.15s; align-self: flex-start; white-space: nowrap;
}
.db-reset-btn:hover:not(:disabled) { transform: translateY(-1px); }
.db-reset-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none !important; }
.db-reset-btn--orange { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; box-shadow: 0 4px 14px rgba(245,158,11,0.3); }
.db-reset-btn--red    { background: linear-gradient(135deg, #ef4444, #dc2626); color: #fff; box-shadow: 0 4px 14px rgba(239,68,68,0.3); }

/* Result message */
.db-result {
  display: flex; align-items: center; gap: 9px; margin: 0 24px 20px;
  padding: 11px 16px; border-radius: 8px; font-size: 13px; font-weight: 600;
  border: 1px solid transparent;
}
.db-result--success { background: #f0fdf4; border-color: #bbf7d0; color: #15803d; }
.db-result--error   { background: #fff1f2; border-color: #fecdd3; color: #dc2626; }
.db-result-fade-enter-active { transition: all 0.3s cubic-bezier(0.34,1.3,0.64,1); }
.db-result-fade-leave-active { transition: all 0.2s ease; }
.db-result-fade-enter-from { opacity: 0; transform: translateY(-6px); }
.db-result-fade-leave-to   { opacity: 0; }

/* ── Revenue totals ── */
.db-count-num { font-size: 22px; font-weight: 800; letter-spacing: -0.03em; color: #0f1117; }
.db-total-box  { text-align: right; flex-shrink: 0; }
.db-total-label { font-size: 10.5px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.07em; color: #9ca3af; margin-bottom: 4px; }
.db-total-value { font-size: 22px; font-weight: 800; color: #16a34a; font-family: 'JetBrains Mono', monospace; letter-spacing: -0.02em; }

/* Loading / empty */
.db-loading { display: flex; align-items: center; gap: 10px; padding: 48px; justify-content: center; color: #9ca3af; font-size: 13.5px; }
.db-loader-ring { width: 24px; height: 24px; border: 3px solid #e4e7ec; border-top-color: #2563eb; border-radius: 50%; animation: db-spin 0.7s linear infinite; }
@keyframes db-spin { to { transform: rotate(360deg); } }
.db-empty { text-align: center; padding: 56px 20px; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.db-empty__icon { width: 54px; height: 54px; border-radius: 50%; background: #f0f2f5; display: flex; align-items: center; justify-content: center; color: #9ca3af; }
.db-empty p { color: #6b7280; font-size: 14px; margin: 0; font-weight: 500; }

/* ── Revenue table ── */
.db-table-wrap { overflow-x: auto; }
.db-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.db-table thead tr { border-bottom: 2px solid #f0f2f5; }
.db-table th {
  padding: 11px 16px; text-align: left; font-size: 10.5px; font-weight: 700;
  letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af; background: #fafbfc; white-space: nowrap;
}
.db-table td { padding: 13px 16px; vertical-align: middle; border-bottom: 1px solid #f0f2f5; }
.db-row { transition: background 0.12s; }
.db-row:hover { background: #fafbfc; }
.db-row:last-child td { border-bottom: none; }

.db-idx {
  display: inline-flex; align-items: center; justify-content: center;
  width: 26px; height: 26px; border-radius: 6px; font-size: 11.5px; font-weight: 700;
  background: #f0f2f5; color: #9ca3af; font-family: 'JetBrains Mono', monospace;
}
.db-idx--top { background: #eff6ff; color: #2563eb; }

.db-customer-cell { display: flex; align-items: center; gap: 10px; }
.db-avatar {
  width: 32px; height: 32px; border-radius: 8px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 12px; font-weight: 700;
}
.db-customer-name { font-weight: 600; font-size: 13.5px; color: #0f1117; }
.db-money { font-family: 'JetBrains Mono', monospace; font-size: 13px; font-weight: 700; color: #0f1117; white-space: nowrap; }
.db-pct   { font-size: 12.5px; font-weight: 700; color: #6b7280; font-family: 'JetBrains Mono', monospace; }
.db-bar-track { height: 6px; background: #f0f2f5; border-radius: 999px; overflow: hidden; }
.db-bar-fill  { height: 100%; background: linear-gradient(90deg, #2563eb, #60a5fa); border-radius: 999px; transition: width 0.4s ease; min-width: 2px; }

/* ── Dialog ── */
.db-overlay {
  position: fixed; inset: 0; background: rgba(10,12,20,0.28);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; padding: 20px; backdrop-filter: blur(2px);
}
.db-dialog {
  background: #fff; border-radius: 20px; width: 100%; max-width: 460px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.14), 0 8px 24px rgba(0,0,0,0.06);
  overflow: hidden;
}

/* Band */
.db-dialog__band {
  position: relative; height: 110px;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.db-dialog__band--orange { background: linear-gradient(135deg, #b45309, #d97706 60%, #fbbf24); }
.db-dialog__band--red    { background: linear-gradient(135deg, #b91c1c, #dc2626 60%, #f87171); }
.db-band-circles { position: absolute; inset: 0; pointer-events: none; }
.db-band-c { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.1); }
.db-band-c--1 { width: 130px; height: 130px; top: -40px; right: -25px; }
.db-band-c--2 { width: 70px;  height: 70px;  bottom: -15px; left: 20px; }
.db-band-c--3 { width: 45px;  height: 45px;  top: 12px; left: 48%; background: rgba(255,255,255,0.07); }
.db-dialog__icon-ring {
  position: relative; z-index: 1; width: 64px; height: 64px;
  background: rgba(255,255,255,0.18); border: 2px solid rgba(255,255,255,0.35);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(4px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.14), inset 0 1px 0 rgba(255,255,255,0.3);
}
.db-dialog__body { padding: 24px 28px 26px; display: flex; flex-direction: column; gap: 12px; }
.db-dialog__badge {
  display: inline-flex; align-self: flex-start;
  font-size: 10.5px; font-weight: 800; letter-spacing: 0.1em; text-transform: uppercase;
  padding: 3px 10px; border-radius: 999px;
}
.db-dialog__badge--orange { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }
.db-dialog__badge--red    { background: #fff1f2; color: #dc2626; border: 1px solid #fecdd3; }
.db-dialog__title { font-size: 18px; font-weight: 800; letter-spacing: -0.02em; margin: 0; }
.db-dialog__desc  { font-size: 13.5px; color: #6b7280; line-height: 1.65; margin: 0; }
.db-dialog__notice {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px;
  border-radius: 8px; font-size: 12.5px; font-weight: 600; line-height: 1.4;
}
.db-dialog__notice--red { background: #fff1f2; color: #b91c1c; border: 1px solid #fecdd3; }
.db-dialog__actions { display: flex; gap: 10px; justify-content: flex-end; padding-top: 4px; border-top: 1px solid #f0f2f5; margin-top: 2px; }

/* Spinner */
.db-spinner {
  display: inline-block; width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.1); border-top-color: #6b7280;
  border-radius: 50%; animation: db-spin 0.6s linear infinite; flex-shrink: 0;
}
.db-spinner--white { border-color: rgba(255,255,255,0.25); border-top-color: #fff; }

/* Dialog transition */
.db-dlg-fade-enter-active, .db-dlg-fade-leave-active { transition: opacity 0.22s ease; }
.db-dlg-fade-enter-active .db-dialog, .db-dlg-fade-leave-active .db-dialog { transition: transform 0.28s cubic-bezier(0.34,1.4,0.64,1), opacity 0.22s ease; }
.db-dlg-fade-enter-from, .db-dlg-fade-leave-to { opacity: 0; }
.db-dlg-fade-enter-from .db-dialog { transform: scale(0.9) translateY(20px); opacity: 0; }
.db-dlg-fade-leave-to  .db-dialog  { transform: scale(0.96) translateY(8px); opacity: 0; }

/* Responsive */
@media (max-width: 900px) {
  .db-page { padding: 20px 16px 40px; }
  .db-modules-grid { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 600px) {
  .db-modules-grid { grid-template-columns: 1fr; }
}
</style>