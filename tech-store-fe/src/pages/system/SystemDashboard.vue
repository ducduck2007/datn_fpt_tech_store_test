<template>
  <div style="background: #f6f7f9; min-height: 100vh; padding: 32px 40px 60px;">
    <div style="max-width: 1440px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px;">

      <!-- Header -->
      <el-row align="middle" justify="space-between" style="flex-wrap: wrap; gap: 16px;">
        <el-space direction="vertical" :size="4">
          <el-text size="small" type="info" style="text-transform: uppercase; letter-spacing: 0.08em; font-weight: 700;">Hệ thống</el-text>
          <el-text tag="div" style="font-size: 28px; font-weight: 800; letter-spacing: -0.03em; line-height: 1.2;">
            Admin <el-text tag="span" type="primary" style="font-size: inherit; font-weight: inherit;">Dashboard</el-text>
          </el-text>
          <el-text type="info">Truy cập nhanh các module quản trị</el-text>
        </el-space>
        <el-button plain @click="reload">
          <el-icon><Refresh /></el-icon> Làm mới
        </el-button>
      </el-row>

      <!-- Module cards -->
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px;">
        <el-card
          v-for="c in cards"
          :key="c.title"
          shadow="never"
          :body-style="{ padding: '16px 18px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '14px', transition: 'all 0.15s' }"
          class="module-card"
          @click="$router.push(c.to)"
        >
          <div :style="`width:44px;height:44px;border-radius:11px;background:${c.color}18;color:${c.color};display:flex;align-items:center;justify-content:center;font-size:20px;flex-shrink:0;`">
            {{ c.icon }}
          </div>
          <div style="flex:1; min-width:0;">
            <el-text tag="div" style="font-size:13.5px; font-weight:700;">{{ c.title }}</el-text>
            <el-text size="small" type="info" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis; display:block;">{{ c.desc }}</el-text>
          </div>
          <el-icon style="color: var(--el-text-color-placeholder); flex-shrink:0;"><ArrowRight /></el-icon>
        </el-card>
      </div>

      <!-- Loyalty Reset -->
      <el-card shadow="never">
        <template #header>
          <el-row align="middle" justify="space-between" style="flex-wrap: wrap; gap: 12px;">
            <el-space direction="vertical" :size="4">
              <el-text size="small" type="info" style="text-transform: uppercase; letter-spacing: 0.08em; font-weight: 700;">Quản lý điểm thưởng</el-text>
              <el-text tag="div" style="font-size:18px; font-weight:800; letter-spacing:-0.02em;">Loyalty Reset</el-text>
              <el-text size="small" type="info">Thao tác hạ hạng &amp; reset điểm thủ công — chỉ dành cho Admin</el-text>
            </el-space>
            <el-tag type="danger" effect="plain" round>Admin only</el-tag>
          </el-row>
        </template>

        <el-row :gutter="14" style="margin-bottom: 16px;">
          <!-- Monthly -->
          <el-col :xs="24" :sm="12">
            <el-card
              shadow="never"
              :body-style="{ padding: '18px' }"
              style="background: #fffbeb; border-color: #fde68a;"
            >
              <el-space direction="vertical" fill :size="12" style="width:100%;">
                <div style="font-size:28px; line-height:1;">📅</div>
                <el-space direction="vertical" :size="6">
                  <el-text tag="div" style="font-size:14px; font-weight:700;">Kiểm tra không hoạt động tháng</el-text>
                  <el-text size="small" type="info" style="line-height:1.65;">
                    Hạ 1 bậc tier với khách hàng không có giao dịch trong <strong>30 ngày qua</strong>.
                  </el-text>
                </el-space>
                <el-space wrap :size="5">
                  <el-tag v-for="chip in monthlyChips" :key="chip.text"
                    size="small" effect="plain"
                    :type="chip.muted ? 'info' : ''"
                    :style="chip.muted ? 'opacity:0.5' : ''"
                  >{{ chip.text }}</el-tag>
                </el-space>
                <el-button
                  type="warning"
                  :loading="loadingMonthly"
                  @click="confirmMonthly"
                  style="align-self:flex-start;"
                >
                  <el-icon v-if="!loadingMonthly"><Refresh /></el-icon>
                  {{ loadingMonthly ? 'Đang chạy…' : 'Chạy ngay' }}
                </el-button>
              </el-space>
            </el-card>
          </el-col>

          <!-- Year-End -->
          <el-col :xs="24" :sm="12">
            <el-card
              shadow="never"
              :body-style="{ padding: '18px' }"
              style="background: #fff1f2; border-color: #fecdd3;"
            >
              <el-space direction="vertical" fill :size="12" style="width:100%;">
                <div style="font-size:28px; line-height:1;">🎯</div>
                <el-space direction="vertical" :size="6">
                  <el-text tag="div" style="font-size:14px; font-weight:700;">Reset cuối năm</el-text>
                  <el-text size="small" type="info" style="line-height:1.65;">
                    Hạ 1 bậc tier <strong>toàn bộ</strong> khách hàng VIP và reset điểm về mức tối thiểu của hạng mới.
                  </el-text>
                </el-space>
                <el-space wrap :size="5">
                  <el-tag v-for="chip in yearEndChips" :key="chip" size="small" effect="plain">{{ chip }}</el-tag>
                </el-space>
                <el-button
                  type="danger"
                  :loading="loadingYearEnd"
                  @click="confirmYearEnd"
                  style="align-self:flex-start;"
                >
                  <el-icon v-if="!loadingYearEnd"><Warning /></el-icon>
                  {{ loadingYearEnd ? 'Đang chạy…' : 'Reset cuối năm' }}
                </el-button>
              </el-space>
            </el-card>
          </el-col>
        </el-row>

        <!-- Result -->
        <transition name="fade">
          <el-alert
            v-if="resetResult"
            :type="resetResult.type === 'success' ? 'success' : 'error'"
            :title="resetResult.message"
            show-icon
            :closable="false"
          />
        </transition>
      </el-card>

    </div>
  </div>
</template>

<script setup>
import { ArrowRight, Refresh, Warning } from "@element-plus/icons-vue";
import { onUnmounted } from "vue";
import { ref } from "vue";
import { customersApi } from "../../api/customers.api";
import { confirmModal } from "../../ui/confirm";
import { toast } from "../../ui/toast";

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

onUnmounted(() => clearTimeout(resultTimer));

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

async function confirmMonthly() {
  const ok = await confirmModal(
    "Tất cả khách hàng VIP không có giao dịch trong 30 ngày qua sẽ bị hạ 1 bậc tier và điểm sẽ được reset về mức tối thiểu của hạng mới.",
    "Xác nhận chạy Monthly Inactivity Check?",
    "Chạy ngay",
    false,
  );
  if (ok) runMonthlyReset();
}

async function confirmYearEnd() {
  const ok = await confirmModal(
    "TOÀN BỘ khách hàng có VIP tier sẽ bị hạ 1 bậc và điểm sẽ được reset. Đây là thao tác không thể hoàn tác!",
    "Xác nhận Reset Cuối Năm?",
    "Reset cuối năm",
    true,
  );
  if (ok) runYearEndReset();
}

function reload() {
  toast("Dashboard đã làm mới.", "info");
}

// ── Static data ────────────────────────────────────────────────────────────
const monthlyChips = [
  { text: "DIAMOND → PLATINUM" },
  { text: "PLATINUM → GOLD" },
  { text: "GOLD → SILVER" },
  { text: "SILVER → BRONZE" },
  { text: "BRONZE → giữ nguyên", muted: true },
];

const yearEndChips = [
  "DIAMOND → PLATINUM · 10.000đ",
  "PLATINUM → GOLD · 5.000đ",
  "GOLD → SILVER · 2.000đ",
  "SILVER → BRONZE · 1.000đ",
  "BRONZE → BRONZE · 1.000đ",
];

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
.module-card:hover {
  border-color: #bfdbfe !important;
  background: #f8faff !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(37,99,235,0.08), 0 2px 6px rgba(0,0,0,0.04) !important;
}
.module-card:hover :deep(.el-icon.el-icon--right),
.module-card:hover :deep(svg) {
  color: #2563eb;
}

.fade-enter-active { transition: all 0.3s cubic-bezier(0.34,1.3,0.64,1); }
.fade-leave-active { transition: all 0.2s ease; }
.fade-enter-from   { opacity: 0; transform: translateY(-6px); }
.fade-leave-to     { opacity: 0; }

@media (max-width: 900px) {
  div[style*="padding: 32px 40px"] { padding: 20px 16px 40px !important; }
}
</style>