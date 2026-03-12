<template>
  <div class="dashboard-page">

    <!-- Header -->
    <div class="page-header">
      <div>
        <div class="page-kicker">System</div>
        <h1>Dashboard</h1>
        <p class="page-sub">Truy cập nhanh các module quản trị</p>
      </div>
      <button class="btn-reload" @click="reload">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/></svg>
        Làm mới
      </button>
    </div>

    <!-- Module grid -->
    <div class="modules-grid">
      <button
        v-for="c in cards"
        :key="c.title"
        class="module-card"
        @click="$router.push(c.to)"
      >
        <div class="module-icon" :style="{ background: c.color + '18', color: c.color }">
          <span>{{ c.icon }}</span>
        </div>
        <div class="module-info">
          <div class="module-title">{{ c.title }}</div>
          <div class="module-desc">{{ c.desc }}</div>
        </div>
        <svg class="module-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
      </button>
    </div>

    <!-- ══════════════════════════════════════════
         LOYALTY RESET SECTION
    ══════════════════════════════════════════ -->
    <div class="reset-card">
      <div class="reset-card__header">
        <div>
          <div class="section-label">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/></svg>
            Quản lý điểm thưởng
          </div>
          <div class="reset-card__title">Loyalty Reset</div>
          <div class="reset-card__sub">Thao tác hạ hạng & reset điểm thủ công — chỉ dành cho Admin</div>
        </div>
        <div class="reset-card__badge">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          Admin only
        </div>
      </div>

      <div class="reset-actions">

        <!-- Monthly Inactivity Check -->
        <div class="reset-action-box reset-action-box--amber">
          <div class="reset-action-box__icon">📅</div>
          <div class="reset-action-box__body">
            <div class="reset-action-box__title">Kiểm tra không hoạt động tháng</div>
            <div class="reset-action-box__desc">
              Hạ 1 bậc tier với khách hàng không có giao dịch trong <strong>30 ngày qua</strong>.
            </div>
            <div class="reset-action-box__rules">
              <span class="rule-chip">DIAMOND → PLATINUM</span>
              <span class="rule-chip">PLATINUM → GOLD</span>
              <span class="rule-chip">GOLD → SILVER</span>
              <span class="rule-chip">SILVER → BRONZE</span>
              <span class="rule-chip rule-chip--muted">BRONZE → giữ nguyên</span>
            </div>
          </div>
          <button
            class="reset-btn reset-btn--amber"
            :disabled="loadingMonthly"
            @click="confirmMonthly"
          >
            <svg v-if="!loadingMonthly" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/></svg>
            <span v-else class="btn-spinner"></span>
            {{ loadingMonthly ? "Đang chạy..." : "Chạy ngay" }}
          </button>
        </div>

        <!-- Year-End Reset -->
        <div class="reset-action-box reset-action-box--red">
          <div class="reset-action-box__icon">🎯</div>
          <div class="reset-action-box__body">
            <div class="reset-action-box__title">Reset cuối năm</div>
            <div class="reset-action-box__desc">
              Hạ 1 bậc tier <strong>toàn bộ</strong> khách hàng VIP và reset điểm về mức tối thiểu của hạng mới.
            </div>
            <div class="reset-action-box__rules">
              <span class="rule-chip">DIAMOND → PLATINUM · 10.000đ</span>
              <span class="rule-chip">PLATINUM → GOLD · 5.000đ</span>
              <span class="rule-chip">GOLD → SILVER · 2.000đ</span>
              <span class="rule-chip">SILVER → BRONZE · 1.000đ</span>
              <span class="rule-chip">BRONZE → BRONZE · 1.000đ</span>
            </div>
          </div>
          <button
            class="reset-btn reset-btn--red"
            :disabled="loadingYearEnd"
            @click="confirmYearEnd"
          >
            <svg v-if="!loadingYearEnd" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <span v-else class="btn-spinner"></span>
            {{ loadingYearEnd ? "Đang chạy..." : "Reset cuối năm" }}
          </button>
        </div>

      </div>

      <!-- Result message -->
      <transition name="result-fade">
        <div v-if="resetResult" class="reset-result" :class="'reset-result--' + resetResult.type">
          <svg v-if="resetResult.type === 'success'" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ resetResult.message }}
        </div>
      </transition>
    </div>

    <!-- Revenue section -->
    <div class="revenue-card">
      <div class="revenue-header">
        <div>
          <div class="section-label">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>
            Doanh thu theo khách hàng
          </div>
          <div class="revenue-count">{{ revenueList.length }} khách hàng</div>
        </div>
        <div class="total-box">
          <div class="total-label">Tổng doanh thu</div>
          <div class="total-value">{{ totalRevenue.toLocaleString() }} đ</div>
        </div>
      </div>

      <div v-if="loadingRevenue" class="loading-wrap">
        <div class="spinner" />
        <span>Đang tải...</span>
      </div>

      <div v-else-if="revenueList.length === 0" class="empty-state">
        Không có dữ liệu doanh thu
      </div>

      <div v-else class="revenue-table-wrap">
        <table class="revenue-table">
          <thead>
            <tr>
              <th style="width:48px">#</th>
              <th>Khách hàng</th>
              <th style="text-align:right">Doanh thu</th>
              <th style="text-align:right;width:120px">Tỷ trọng</th>
              <th style="width:180px">Tỷ lệ</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in revenueList" :key="idx" class="revenue-row">
              <td class="idx-cell">{{ idx + 1 }}</td>
              <td class="name-cell">{{ row.customerName }}</td>
              <td class="money-cell">{{ row.totalRevenue?.toLocaleString() }} đ</td>
              <td class="pct-cell">
                {{ totalRevenue ? ((row.totalRevenue / totalRevenue) * 100).toFixed(1) : 0 }}%
              </td>
              <td class="bar-cell">
                <div class="bar-track">
                  <div
                    class="bar-fill"
                    :style="{ width: totalRevenue ? ((row.totalRevenue / totalRevenue) * 100) + '%' : '0%' }"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         CONFIRM DIALOG
    ══════════════════════════════════════════ -->
    <Teleport to="body">
      <transition name="modal-fade">
        <div v-if="confirmDialog.show" class="modal-overlay" @click.self="confirmDialog.show = false">
          <div class="confirm-modal" :class="'confirm-modal--' + confirmDialog.type">
            <div class="confirm-modal__icon">{{ confirmDialog.icon }}</div>
            <div class="confirm-modal__title">{{ confirmDialog.title }}</div>
            <div class="confirm-modal__body">{{ confirmDialog.body }}</div>
            <div class="confirm-modal__warning">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              Thao tác này không thể hoàn tác!
            </div>
            <div class="confirm-modal__actions">
              <button class="confirm-cancel" @click="confirmDialog.show = false">Hủy</button>
              <button
                class="confirm-ok"
                :class="'confirm-ok--' + confirmDialog.type"
                @click="confirmDialog.onConfirm(); confirmDialog.show = false"
              >
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from "vue";
import { ordersApi } from "../../api/orders.api";
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";

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
  show: false,
  type: "amber",
  icon: "📅",
  title: "",
  body: "",
  onConfirm: () => {},
});

function confirmMonthly() {
  Object.assign(confirmDialog, {
    show: true,
    type: "amber",
    icon: "📅",
    title: "Xác nhận chạy Monthly Inactivity Check?",
    body: "Tất cả khách hàng VIP không có giao dịch trong 30 ngày qua sẽ bị hạ 1 bậc tier và điểm sẽ được reset về mức tối thiểu của hạng mới.",
    onConfirm: runMonthlyReset,
  });
}

function confirmYearEnd() {
  Object.assign(confirmDialog, {
    show: true,
    type: "red",
    icon: "⚠️",
    title: "Xác nhận Reset Cuối Năm?",
    body: "TOÀN BỘ khách hàng có VIP tier sẽ bị hạ 1 bậc và điểm sẽ được reset. Đây là thao tác không thể hoàn tác!",
    onConfirm: runYearEndReset,
  });
}

// ── Cards ──────────────────────────────────────────────────────────────────
const cards = [
  { title: "Users",               desc: "Quản lý tài khoản & phân quyền",     to: "/system/users",                   icon: "👤", color: "#2563eb" },
  { title: "Customers",           desc: "Khách hàng & hạng thành viên",        to: "/system/customers",               icon: "🧑‍🤝‍🧑", color: "#16a34a" },
  { title: "Categories",          desc: "Danh mục sản phẩm",                   to: "/system/categories",              icon: "🗂️", color: "#d97706" },
  { title: "Products",            desc: "Tạo & quản lý sản phẩm",             to: "/system/products",                icon: "📦", color: "#7c3aed" },
  { title: "Orders — New",        desc: "Đơn hàng mới cần xử lý",             to: "/system/orders/new",              icon: "🆕", color: "#0891b2" },
  { title: "Orders — Processing", desc: "Hàng đợi đang xử lý",                to: "/system/orders/processing",       icon: "⚙️", color: "#ea580c" },
  { title: "Loyalty Summary",     desc: "Tổng hợp điểm thưởng",               to: "/system/Loyaltysummaryadmin",     icon: "⭐", color: "#ca8a04" },
  { title: "Pricing",             desc: "Giá biến thể & lịch sử",             to: "/system/pricing",                 icon: "💰", color: "#16a34a" },
  { title: "Promotions",          desc: "Mã giảm giá & chiến dịch",           to: "/system/promotions",              icon: "🎁", color: "#db2777" },
  { title: "Payment History",     desc: "Tất cả giao dịch thanh toán",         to: "/system/payments",                icon: "🧾", color: "#2563eb" },
  { title: "Customer Spending",   desc: "Chi tiêu theo khách hàng",            to: "/system/customer-spending",       icon: "📊", color: "#0891b2" },
  { title: "Settings",            desc: "Đơn vị tiền tệ mặc định",            to: "/system/settings/currency",       icon: "⚙️", color: "#6b7280" },
];
</script>

<style scoped>
.dashboard-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  font-family: 'Be Vietnam Pro', 'Segoe UI', sans-serif;
}

/* ── Header ── */
.page-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  flex-wrap: wrap; gap: 16px; margin-bottom: 28px;
}
.page-kicker {
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .08em; color: #2563eb; background: #eff6ff;
  padding: 2px 10px; border-radius: 50px; display: inline-block; margin-bottom: 6px;
}
.page-header h1 { font-size: 24px; font-weight: 800; color: #111827; margin: 0 0 4px; }
.page-sub { font-size: 13px; color: #6b7280; margin: 0; }
.btn-reload {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 18px; border: 1.5px solid #e5e7eb;
  border-radius: 50px; background: white; font-size: 13px;
  color: #374151; cursor: pointer; font-family: inherit;
  transition: all .15s; white-space: nowrap;
}
.btn-reload:hover { border-color: #2563eb; color: #2563eb; background: #eff6ff; }

/* ── Modules grid ── */
.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
  margin-bottom: 28px;
}
.module-card {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 18px;
  background: white; border: 1.5px solid #f3f4f6;
  border-radius: 14px; cursor: pointer; text-align: left;
  transition: all .15s; font-family: inherit; width: 100%;
}
.module-card:hover {
  border-color: #bfdbfe; background: #f8faff;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(37,99,235,.08);
}
.module-icon {
  width: 42px; height: 42px; border-radius: 11px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.module-info { flex: 1; min-width: 0; }
.module-title { font-size: 13px; font-weight: 700; color: #111827; }
.module-desc  { font-size: 12px; color: #9ca3af; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.module-arrow { color: #d1d5db; flex-shrink: 0; transition: transform .15s, color .15s; }
.module-card:hover .module-arrow { color: #2563eb; transform: translateX(3px); }

/* ══════════════════════════════════════════
   LOYALTY RESET CARD
══════════════════════════════════════════ */
.reset-card {
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 18px;
  padding: 24px;
  margin-bottom: 24px;
}

.reset-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.reset-card__title {
  font-size: 18px;
  font-weight: 800;
  color: #111827;
  margin-bottom: 4px;
}

.reset-card__sub {
  font-size: 12.5px;
  color: #9ca3af;
}

.reset-card__badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  font-size: 11px;
  font-weight: 700;
  padding: 5px 12px;
  border-radius: 50px;
  white-space: nowrap;
  flex-shrink: 0;
}

.reset-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

@media (max-width: 768px) {
  .reset-actions { grid-template-columns: 1fr; }
}

.reset-action-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px;
  border-radius: 14px;
  border: 1.5px solid;
}

.reset-action-box--amber {
  background: #fffbeb;
  border-color: #fde68a;
}

.reset-action-box--red {
  background: #fff5f5;
  border-color: #fecaca;
}

.reset-action-box__icon {
  font-size: 28px;
  line-height: 1;
}

.reset-action-box__title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 5px;
}

.reset-action-box__desc {
  font-size: 12.5px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 10px;
}

.reset-action-box__desc strong {
  color: #374151;
}

.reset-action-box__rules {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.rule-chip {
  font-size: 10.5px;
  font-weight: 600;
  padding: 3px 9px;
  border-radius: 50px;
  background: rgba(0,0,0,0.06);
  color: #374151;
  white-space: nowrap;
}

.rule-chip--muted {
  opacity: 0.55;
}

.reset-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 9px 20px;
  border: none;
  border-radius: 10px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.15s, transform 0.1s, box-shadow 0.15s;
  align-self: flex-start;
  white-space: nowrap;
}

.reset-btn:hover:not(:disabled) { transform: translateY(-1px); }
.reset-btn:active:not(:disabled) { transform: translateY(0); }
.reset-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.reset-btn--amber {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  box-shadow: 0 4px 14px rgba(245,158,11,0.3);
}

.reset-btn--red {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  box-shadow: 0 4px 14px rgba(239,68,68,0.3);
}

.btn-spinner {
  width: 13px; height: 13px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}

/* Result message */
.reset-result {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-top: 16px;
  padding: 11px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
}

.reset-result--success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #15803d;
}

.reset-result--error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
}

.result-fade-enter-active { transition: all 0.35s cubic-bezier(0.34, 1.3, 0.64, 1); }
.result-fade-leave-active { transition: all 0.2s ease; }
.result-fade-enter-from  { opacity: 0; transform: translateY(-6px); }
.result-fade-leave-to    { opacity: 0; }

/* ── Revenue card ── */
.revenue-card {
  background: white; border: 1.5px solid #f0f0f0;
  border-radius: 18px; padding: 24px; overflow: hidden;
}
.revenue-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  flex-wrap: wrap; gap: 16px; margin-bottom: 20px;
}
.section-label {
  display: flex; align-items: center; gap: 7px;
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .07em; color: #9ca3af; margin-bottom: 4px;
}
.revenue-count { font-size: 22px; font-weight: 800; color: #111827; }
.total-box { text-align: right; }
.total-label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .07em; color: #9ca3af; margin-bottom: 4px; }
.total-value { font-size: 24px; font-weight: 900; color: #16a34a; }

.loading-wrap { display: flex; align-items: center; gap: 10px; padding: 40px 0; justify-content: center; color: #9ca3af; font-size: 13px; }
.spinner { width: 20px; height: 20px; border: 2.5px solid #e5e7eb; border-top-color: #2563eb; border-radius: 50%; animation: spin .7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { text-align: center; padding: 48px 0; font-size: 13px; color: #d1d5db; }

.revenue-table-wrap { overflow-x: auto; border-radius: 12px; border: 1px solid #f3f4f6; }
.revenue-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.revenue-table thead tr { background: #f9fafb; }
.revenue-table th {
  padding: 10px 16px; text-align: left;
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .06em; color: #9ca3af; white-space: nowrap;
  border-bottom: 1px solid #f3f4f6;
}
.revenue-row { border-bottom: 1px solid #f9fafb; transition: background .12s; }
.revenue-row:last-child { border-bottom: none; }
.revenue-row:hover { background: #f8faff; }
.revenue-table td { padding: 12px 16px; }
.idx-cell   { color: #d1d5db; font-weight: 700; }
.name-cell  { font-weight: 600; color: #111827; }
.money-cell { text-align: right; font-weight: 700; color: #111827; white-space: nowrap; }
.pct-cell   { text-align: right; color: #6b7280; font-weight: 600; }
.bar-cell   { padding-right: 20px; }
.bar-track  { height: 6px; background: #f3f4f6; border-radius: 99px; overflow: hidden; }
.bar-fill   { height: 100%; background: linear-gradient(90deg, #2563eb, #60a5fa); border-radius: 99px; transition: width .4s ease; min-width: 2px; }

/* ══════════════════════════════════════════
   CONFIRM MODAL
══════════════════════════════════════════ */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}

.modal-fade-enter-active { transition: all 0.25s cubic-bezier(0.34, 1.3, 0.64, 1); }
.modal-fade-leave-active { transition: all 0.18s ease; }
.modal-fade-enter-from  { opacity: 0; }
.modal-fade-leave-to    { opacity: 0; }

.confirm-modal {
  background: white;
  border-radius: 20px;
  padding: 32px 28px 24px;
  width: 100%;
  max-width: 420px;
  text-align: center;
  box-shadow: 0 24px 60px rgba(0,0,0,0.15);
  animation: modal-pop 0.25s cubic-bezier(0.34,1.4,0.64,1);
}

@keyframes modal-pop {
  from { transform: scale(0.93); opacity: 0; }
  to   { transform: scale(1);    opacity: 1; }
}

.confirm-modal__icon  { font-size: 44px; margin-bottom: 14px; line-height: 1; }
.confirm-modal__title { font-size: 16px; font-weight: 800; color: #111827; margin-bottom: 10px; }
.confirm-modal__body  { font-size: 13.5px; color: #6b7280; line-height: 1.6; margin-bottom: 14px; }

.confirm-modal__warning {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #dc2626;
  background: #fef2f2;
  border: 1px solid #fecaca;
  padding: 6px 14px;
  border-radius: 50px;
  margin-bottom: 22px;
}

.confirm-modal__actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.confirm-cancel {
  padding: 9px 24px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  background: white;
  font-family: inherit;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  transition: all .15s;
}
.confirm-cancel:hover { background: #f9fafb; border-color: #d1d5db; color: #374151; }

.confirm-ok {
  padding: 9px 24px;
  border: none;
  border-radius: 10px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: white;
  cursor: pointer;
  transition: all .15s;
}
.confirm-ok:hover { transform: translateY(-1px); }

.confirm-ok--amber { background: linear-gradient(135deg, #f59e0b, #d97706); box-shadow: 0 4px 12px rgba(245,158,11,0.3); }
.confirm-ok--red   { background: linear-gradient(135deg, #ef4444, #dc2626); box-shadow: 0 4px 12px rgba(239,68,68,0.3); }
</style>