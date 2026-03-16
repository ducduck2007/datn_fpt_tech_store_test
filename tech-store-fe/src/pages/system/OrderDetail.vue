<template>
  <div class="order-page container-xl">
    <el-card shadow="never" class="order-card">
      <!-- Header -->
      <div
        class="order-header d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="order-kicker">
            <span class="kicker-dot"></span>
            Đơn hàng
          </div>
          <div class="order-id">{{ detail?.orderNumber || `#${orderId}` }}</div>
          <div class="badges-row mt-2">
            <el-tag :type="statusType" size="large" class="status-badge">
              <span class="badge-dot"></span>
              {{ detail?.status }}
            </el-tag>
            <el-tag
              class="ms-2 payment-badge"
              :type="paymentStatusType"
              size="large"
            >
              💳 {{ detail?.paymentStatus }}
            </el-tag>
            <el-tag
              v-if="isReturned(detail?.status)"
              type="warning"
              size="large"
              class="ms-2"
            >
              ↩ Hoàn trả
            </el-tag>
          </div>
        </div>
        <el-button @click="reload" :loading="loading" class="reload-btn">
          <span v-if="!loading">↻ Reload</span>
        </el-button>
      </div>

      <div class="section-divider"></div>

      <!-- Skeleton -->
      <el-skeleton v-if="loading" :rows="6" animated />

      <div v-else-if="detail" class="row g-4">
        <!-- Customer Info -->
        <div class="col-12 col-md-6">
          <div class="info-card">
            <div class="info-card-header">
              <span class="info-card-icon">👤</span>
              <h5>Thông tin khách hàng</h5>
            </div>
            <div class="info-row">
              <span class="info-label">Tên</span>
              <span class="info-value">{{ detail.customerName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">ID</span>
              <span class="info-value mono">{{ detail.customerId }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Email</span>
              <span class="info-value">{{ detail.customerEmail }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Phone</span>
              <span class="info-value">{{ detail.customerPhone }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Address</span>
              <span class="info-value">{{ detail.customerAddress }}</span>
            </div>
          </div>
        </div>

        <!-- Payment & Delivery Info -->
        <div class="col-12 col-md-6">
          <div class="info-card">
            <div class="info-card-header">
              <span class="info-card-icon">🚚</span>
              <h5>Thanh toán &amp; Giao hàng</h5>
            </div>
            <div class="info-row">
              <span class="info-label">Phương thức</span>
              <span class="info-value">{{ detail.paymentMethod }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Kênh</span>
              <span class="info-value">{{ detail.channel }}</span>
            </div>
            <div class="info-row align-items-start">
              <span class="info-label">Ghi chú</span>
              <div v-if="detail.notes" class="notes-list">
                <div
                  v-for="(note, i) in categorizedNotes"
                  :key="i"
                  class="note-chip"
                  :class="note.type"
                >
                  <span class="note-chip-icon">{{ note.icon }}</span>
                  <span class="note-chip-text">{{ note.text }}</span>
                </div>
              </div>
              <span v-else class="info-value muted">—</span>
            </div>
          </div>
        </div>

        <!-- Discount Breakdown -->
        <div v-if="hasDiscount" class="col-12">
          <div class="discount-card">
            <div class="discount-card-header">
              <div class="discount-header-left">
                <div class="discount-header-icon">🎁</div>
                <div>
                  <div class="discount-card-title">Ưu đãi áp dụng</div>
                  <div class="discount-card-sub">
                    Tổng tiết kiệm:
                    <strong>{{ formatMoney(detail.discountTotal) }}</strong>
                  </div>
                </div>
              </div>
              <div class="discount-total-badge">
                -{{ formatMoney(detail.discountTotal) }}
              </div>
            </div>

            <div class="discount-items-grid">
              <!-- VIP % Discount -->
              <div
                v-if="
                  detail.vipDiscountRate &&
                  detail.vipDiscountRate > 0 &&
                  vipPercentAmount > 0
                "
                class="discount-pill vip"
              >
                <div class="pill-icon">⭐</div>
                <div class="pill-body">
                  <div class="pill-label">VIP {{ vipTierName }}</div>
                  <div class="pill-rate">{{ detail.vipDiscountRate }}%</div>
                  <div class="pill-amount">
                    -{{ formatMoney(vipPercentAmount) }}
                  </div>
                  <div class="pill-note">Giảm theo % hạng VIP</div>
                </div>
              </div>

              <!-- VIP Bonus -->
              <div v-if="parsedVipBonus" class="discount-pill vip-bonus">
                <div class="pill-icon">🏆</div>
                <div class="pill-body">
                  <div class="pill-label">
                    VIP {{ parsedVipBonus.tier }}
                    <span class="pill-tag">Đơn lớn</span>
                  </div>
                  <div class="pill-amount">
                    -{{ formatMoney(parsedVipBonus.amount) }}
                  </div>
                  <div class="pill-note">
                    Đơn từ {{ formatMoney(parsedVipBonus.threshold) }}
                  </div>
                </div>
              </div>

              <!-- Spin Wheel -->
              <div
                v-if="detail.spinDiscount && detail.spinDiscount > 0"
                class="discount-pill spin"
              >
                <div class="pill-icon">🎡</div>
                <div class="pill-body">
                  <div class="pill-label">
                    Vòng quay
                    <span class="pill-tag spin-tag">1 lần dùng</span>
                  </div>
                  <div class="pill-rate">{{ detail.spinDiscountRate }}%</div>
                  <div class="pill-amount">
                    -{{ formatMoney(detail.spinDiscount) }}
                  </div>
                  <div class="pill-note">
                    {{
                      detail.hasSpinBonus ? "✅ Đã áp dụng" : "Khấu trừ khi TT"
                    }}
                  </div>
                </div>
              </div>

              <!-- Coupon -->
              <div v-if="effectiveCoupon" class="discount-pill coupon">
                <div class="pill-icon">🎟️</div>
                <div class="pill-body">
                  <div class="pill-label">
                    Mã giảm giá
                    <span class="pill-tag coupon-tag">{{
                      effectiveCoupon.code || "Đã áp dụng"
                    }}</span>
                  </div>
                  <div v-if="effectiveCoupon.rate" class="pill-rate">
                    {{ effectiveCoupon.rate }}%
                  </div>
                  <div class="pill-amount">
                    -{{ formatMoney(effectiveCoupon.amount) }}
                  </div>
                  <div class="pill-note">Ưu đãi từ mã khuyến mãi</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Products Table -->
        <div class="col-12">
          <div class="table-section">
            <div class="table-header-row">
              <span class="table-section-title">📦 Chi tiết sản phẩm</span>
              <span class="table-count"
                >{{ detail.items?.length || 0 }} sản phẩm</span
              >
            </div>
            <el-table :data="detail.items" class="products-table">
              <el-table-column label="Sản phẩm" min-width="220">
                <template #default="{ row }">
                  <div class="product-cell">
                    <div class="product-name">{{ row.productName }}</div>
                    <div class="product-variant">{{ row.variantName }}</div>
                    <div class="product-sku">SKU: {{ row.sku }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="Số lượng" width="100" align="center">
                <template #default="{ row }">
                  <span class="qty-badge">{{ row.quantity }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Đơn giá" width="160" align="right">
                <template #default="{ row }">
                  <span class="price-cell">{{ formatMoney(row.price) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- Totals -->
        <div class="col-12">
          <div class="totals-card">
            <div class="totals-title">Tổng thanh toán</div>
            <div class="totals-rows">
              <div class="totals-row">
                <span>Tạm tính</span>
                <span>{{ formatMoney(detail.subtotal) }}</span>
              </div>

              <div
                v-if="
                  detail.vipDiscountRate &&
                  detail.vipDiscountRate > 0 &&
                  vipPercentAmount > 0
                "
                class="totals-row discount-row"
              >
                <span
                  >⭐ Giảm VIP {{ vipTierName }} ({{
                    detail.vipDiscountRate
                  }}%)</span
                >
                <span class="discount-value"
                  >-{{ formatMoney(vipPercentAmount) }}</span
                >
              </div>

              <div
                v-if="parsedVipBonus"
                class="totals-row discount-row vip-bonus-row"
              >
                <span
                  >🏆 Ưu đãi VIP {{ parsedVipBonus.tier }}
                  <span class="small muted"
                    >(từ {{ formatMoney(parsedVipBonus.threshold) }})</span
                  ></span
                >
                <span class="discount-value"
                  >-{{ formatMoney(parsedVipBonus.amount) }}</span
                >
              </div>

              <div
                v-if="detail.spinDiscount && detail.spinDiscount > 0"
                class="totals-row discount-row spin-row"
              >
                <span>🎡 Vòng quay ({{ detail.spinDiscountRate }}%)</span>
                <span class="discount-value"
                  >-{{ formatMoney(detail.spinDiscount) }}</span
                >
              </div>

              <div
                v-if="effectiveCoupon"
                class="totals-row discount-row coupon-row"
              >
                <span
                  >🎟️ Mã <strong>{{ effectiveCoupon.code }}</strong></span
                >
                <span class="discount-value"
                  >-{{ formatMoney(effectiveCoupon.amount) }}</span
                >
              </div>

              <div class="totals-row">
                <span>Phí giao hàng</span>
                <span>{{ formatMoney(detail.shippingFee) }}</span>
              </div>
            </div>

            <div class="totals-final">
              <span>Tổng cộng</span>
              <span class="final-amount">{{
                formatMoney(detail.totalAmount)
              }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";

const route = useRoute();

const loading = ref(false);
const detail = ref(null);
const vipTierName = ref("");
const orderId = computed(() => route.params.orderId);

const statusType = computed(() => {
  const s = detail.value?.status;
  if (s === "DELIVERED") return "success";
  if (s === "SHIPPING") return "warning";
  if (s === "CANCELLED") return "danger";
  if (s === "PAID") return "success";
  return "info";
});

const paymentStatusType = computed(() => {
  const ps = detail.value?.paymentStatus;
  if (ps === "PAID") return "success";
  if (ps === "PARTIAL") return "warning";
  return "info";
});

const vipPercentAmount = computed(() => {
  const total = detail.value?.vipDiscount || 0;
  const bonus = parsedVipBonus.value?.amount || 0;
  return Math.max(0, total - bonus);
});

const hasDiscount = computed(() => {
  return (
    vipPercentAmount.value > 0 ||
    !!parsedVipBonus.value ||
    (detail.value?.spinDiscount && detail.value.spinDiscount > 0) ||
    !!effectiveCoupon.value
  );
});

const parsedNotes = computed(() => {
  if (!detail.value?.notes) return [];
  return detail.value.notes
    .split("|")
    .map((n) => n.trim())
    .filter(Boolean);
});

const categorizedNotes = computed(() => {
  return parsedNotes.value.map((note) => {
    const lower = note.toLowerCase();
    if (/^vip\s+/i.test(note)) {
      return { text: note, type: "note-vip", icon: "⭐" };
    }
    if (/^[Mm]ã[:\s]/.test(note)) {
      return { text: note, type: "note-coupon", icon: "🎟️" };
    }
    if (lower.includes("ship") || lower.includes("giao")) {
      return { text: note, type: "note-ship", icon: "🚚" };
    }
    return { text: note, type: "note-default", icon: "📌" };
  });
});

function noteTagType(note) {
  const lower = note.toLowerCase();
  if (lower.includes("vip")) return "purple";
  if (lower.includes("mã") || lower.includes("code")) return "success";
  if (lower.includes("ship") || lower.includes("giao")) return "warning";
  return "info";
}

const parsedVipBonus = computed(() => {
  if (!detail.value?.notes) return null;
  const segments = detail.value.notes.split("|").map((s) => s.trim());
  for (const seg of segments) {
    const match = seg.match(
      /^VIP\s+([A-Za-zÀ-ỹ]+)[:\s]+([-\d,\.]+)\s*\(đơn từ\s*([\d,\.]+)\)/i,
    );
    if (match) {
      const tier = match[1].trim();
      const amount = Math.abs(parseInt(match[2].replace(/[,\.]/g, ""), 10));
      const threshold = parseInt(match[3].replace(/[,\.]/g, ""), 10);
      if (!isNaN(amount) && amount > 0) return { tier, amount, threshold };
    }
  }
  return null;
});

const parsedCoupon = computed(() => {
  if (!detail.value?.notes) return null;
  if (detail.value.couponDiscount && detail.value.couponDiscount > 0)
    return null;
  const segments = detail.value.notes.split("|").map((s) => s.trim());
  for (const seg of segments) {
    const match = seg.match(/^[Mm]ã[:\s]+([A-Za-z0-9_\-]+)[:\s]+([-\d,\.]+)/);
    if (match) {
      const code = match[1].trim();
      const rawNum = match[2].replace(/[,\.]/g, "").replace(/[^\d\-]/g, "");
      const amount = Math.abs(parseInt(rawNum, 10));
      if (!isNaN(amount) && amount > 0) return { code, amount };
    }
  }
  return null;
});

const effectiveCoupon = computed(() => {
  if (detail.value?.couponDiscount && detail.value.couponDiscount > 0) {
    return {
      code: detail.value.couponCode || "",
      amount: detail.value.couponDiscount,
      rate: detail.value.couponDiscountRate || null,
    };
  }
  if (parsedCoupon.value)
    return {
      code: parsedCoupon.value.code,
      amount: parsedCoupon.value.amount,
      rate: null,
    };
  return null;
});

function formatMoney(val) {
  if (!val) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(val);
}

async function reload() {
  loading.value = true;
  try {
    const res = await ordersApi.getById(orderId.value);
    detail.value = res?.data?.data || res?.data;
    if (detail.value?.customerId) {
      try {
        const customerRes = await customersApi.getById(detail.value.customerId);
        const customer = customerRes?.data?.data || customerRes?.data;
        vipTierName.value = customer?.vipTierDisplay || "";
      } catch (e) {
        console.error("Error loading customer:", e);
      }
    }
  } catch (e) {
    toast("Không thể tải chi tiết đơn hàng", "error");
  } finally {
    loading.value = false;
  }
}

function isReturned(status) {
  return ["PARTIALLY_RETURNED", "RETURNED"].includes(status);
}

onMounted(() => reload());
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,400&display=swap");

/* ─── Design Tokens ─────────────────────────────────────────── */
:root {
  --blue-50: #eff6ff;
  --blue-100: #dbeafe;
  --blue-200: #bfdbfe;
  --blue-400: #60a5fa;
  --blue-500: #3b82f6;
  --blue-600: #2563eb;
  --blue-700: #1d4ed8;
  --blue-800: #1e40af;
  --blue-900: #1e3a8a;
}

/* ─── Page Shell ────────────────────────────────────────────── */
.order-page {
  padding: 28px 16px;
  min-height: 100vh;
  font-family:
    "Be Vietnam Pro",
    system-ui,
    -apple-system,
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Áp font cho toàn bộ card và con cháu */
.order-page :deep(*) {
  font-family:
    "Be Vietnam Pro",
    system-ui,
    -apple-system,
    sans-serif;
}

.order-card {
  border-radius: 16px !important;
  border: 1px solid #dbeafe !important;
  box-shadow: 0 4px 24px rgba(37, 99, 235, 0.07) !important;
  overflow: hidden;
}

/* ─── Header ────────────────────────────────────────────────── */
.order-header {
  padding: 8px 4px 4px;
}

.order-kicker {
  display: flex;
  align-items: center;
  gap: 6px;
  text-transform: uppercase;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #2563eb;
  margin-bottom: 4px;
}

.kicker-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3b82f6;
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.75);
  }
}

.order-id {
  font-size: 28px;
  font-weight: 800;
  color: #1e3a8a;
  letter-spacing: -0.5px;
}

.badges-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.reload-btn {
  background: #2563eb !important;
  color: #fff !important;
  border: none !important;
  border-radius: 10px !important;
  font-weight: 600 !important;
  padding: 10px 22px !important;
  letter-spacing: 0.02em;
  transition:
    background 0.2s,
    transform 0.15s;
}
.reload-btn:hover {
  background: #1d4ed8 !important;
  transform: translateY(-1px);
}

/* ─── Section Divider ───────────────────────────────────────── */
.section-divider {
  height: 1px;
  background: linear-gradient(90deg, #dbeafe 0%, #bfdbfe 50%, #dbeafe 100%);
  margin: 20px 0;
  border-radius: 1px;
}

/* ─── Info Cards ────────────────────────────────────────────── */
.info-card {
  background: #f8faff;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  padding: 18px 20px;
  height: 100%;
}

.info-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.info-card-icon {
  font-size: 20px;
}

.info-card-header h5 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1e40af;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 0;
  border-bottom: 1px solid #eff6ff;
  font-size: 14px;
}
.info-row:last-child {
  border-bottom: none;
}

.info-label {
  min-width: 110px;
  color: #64748b;
  font-weight: 500;
  font-size: 13px;
}

.info-value {
  color: #1e3a8a;
  font-weight: 600;
}

.info-value.mono {
  font-family: monospace;
  font-size: 12px;
  background: #dbeafe;
  padding: 2px 8px;
  border-radius: 5px;
}

.notes-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.note-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 11px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  width: fit-content;
  max-width: 100%;
}

.note-chip-icon {
  font-size: 13px;
  flex-shrink: 0;
}

.note-chip.note-default {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.note-chip.note-vip {
  background: #f5f3ff;
  color: #5b21b6;
  border: 1px solid #ddd6fe;
}

.note-chip.note-coupon {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.note-chip.note-ship {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
}

/* ─── Discount Card ─────────────────────────────────────────── */
.discount-card {
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 60%);
  border: 2px solid #bfdbfe;
  border-radius: 14px;
  padding: 20px 22px;
  position: relative;
  overflow: hidden;
}

.discount-card::before {
  content: "";
  position: absolute;
  top: -30px;
  right: -30px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(37, 99, 235, 0.06);
  pointer-events: none;
}

.discount-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.discount-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.discount-header-icon {
  font-size: 28px;
  line-height: 1;
}

.discount-card-title {
  font-size: 15px;
  font-weight: 800;
  color: #1e40af;
}

.discount-card-sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.discount-total-badge {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-size: 16px;
  font-weight: 800;
  padding: 6px 18px;
  border-radius: 50px;
  letter-spacing: -0.3px;
}

/* ─── Discount Pills ────────────────────────────────────────── */
.discount-items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.discount-pill {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: white;
  border: 1px solid #e2e8f0;
  transition:
    box-shadow 0.2s,
    transform 0.2s;
}
.discount-pill:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.12);
}

.discount-pill.vip {
  border-left: 4px solid #7c3aed;
}
.discount-pill.vip-bonus {
  border-left: 4px solid #2563eb;
}
.discount-pill.spin {
  border-left: 4px solid #f59e0b;
}
.discount-pill.coupon {
  border-left: 4px solid #10b981;
}

.pill-icon {
  font-size: 26px;
  line-height: 1;
  flex-shrink: 0;
}

.pill-body {
  flex: 1;
}

.pill-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
}

.pill-tag {
  font-size: 10px;
  background: #dbeafe;
  color: #1d4ed8;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 700;
  letter-spacing: 0.02em;
}
.pill-tag.spin-tag {
  background: #fef3c7;
  color: #b45309;
}
.pill-tag.coupon-tag {
  background: #d1fae5;
  color: #065f46;
}

.pill-rate {
  font-size: 24px;
  font-weight: 900;
  color: #1e3a8a;
  line-height: 1;
  margin-bottom: 2px;
}

.pill-amount {
  font-size: 16px;
  font-weight: 800;
  color: #16a34a;
  margin-bottom: 2px;
}

.pill-note {
  font-size: 11px;
  color: #94a3b8;
}

/* ─── Products Table ────────────────────────────────────────── */
.table-section {
  background: #f8faff;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  overflow: hidden;
}

.table-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px 10px;
}

.table-section-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e40af;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.table-count {
  font-size: 12px;
  background: #dbeafe;
  color: #1d4ed8;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 600;
}

.products-table {
  background: transparent !important;
}

:deep(.products-table th.el-table__cell) {
  background: #eff6ff !important;
  color: #1d4ed8 !important;
  font-weight: 700 !important;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

:deep(.products-table td.el-table__cell) {
  border-color: #dbeafe !important;
  background: white;
}

:deep(.products-table tr:hover > td.el-table__cell) {
  background: #eff6ff !important;
}

.product-name {
  font-weight: 700;
  color: #1e3a8a;
  font-size: 14px;
}
.product-variant {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}
.product-sku {
  font-size: 11px;
  color: #94a3b8;
  font-family: monospace;
  margin-top: 2px;
}

.qty-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 800;
  border-radius: 8px;
  font-size: 14px;
}

.price-cell {
  font-weight: 700;
  color: #1e40af;
  font-size: 14px;
}

/* ─── Totals ────────────────────────────────────────────────── */
.totals-card {
  background: white;
  border: 2px solid #dbeafe;
  border-radius: 14px;
  overflow: hidden;
}

.totals-title {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  padding: 12px 22px;
}

.totals-rows {
  padding: 12px 22px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.totals-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #475569;
  border-bottom: 1px dashed #e2e8f0;
}
.totals-row:last-child {
  border-bottom: none;
}

.discount-row {
  color: #16a34a;
}
.vip-bonus-row {
  color: #2563eb;
}
.spin-row {
  color: #b45309;
}
.coupon-row {
  color: #059669;
}

.discount-value {
  font-weight: 700;
}

.totals-final {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 22px;
  background: #eff6ff;
  border-top: 2px solid #bfdbfe;
}

.totals-final span:first-child {
  font-size: 15px;
  font-weight: 700;
  color: #1e3a8a;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.final-amount {
  font-size: 24px;
  font-weight: 900;
  color: #1d4ed8;
  letter-spacing: -0.5px;
}

/* ─── Misc ──────────────────────────────────────────────────── */
.muted {
  color: #94a3b8;
}
.small {
  font-size: 12px;
}
</style>
