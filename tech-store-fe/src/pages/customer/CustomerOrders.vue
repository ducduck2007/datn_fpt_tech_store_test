<template>
  <div class="orders-root">

    <!-- ── PAGE HEADER ────────────────────────────────────────── -->
    <div class="orders-body">
      <!-- ── PAGE TITLE ROW ─────────────────────────────────────── -->
      <div class="page-title-row">
        <div>
          <h1 class="page-title">Đơn hàng của tôi</h1>
          <p class="page-sub">Theo dõi và quản lý tất cả đơn hàng của bạn</p>
        </div>
        <button class="reload-btn" :class="{ 'reload-btn--spinning': loading }" @click="loadOrders">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          Tải lại
        </button>
      </div>

      <!-- ── STATS ROW ──────────────────────────────────────────── -->
      <div class="stats-row">
        <div class="stat-card" v-for="stat in stats" :key="stat.key"
          :class="`stat-card--${stat.color}`"
          :style="`--delay: ${stat.delay}ms`">
          <div class="stat-icon">{{ stat.icon }}</div>
          <div class="stat-val">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>

      <!-- ── FILTER TABS ─────────────────────────────────────────── -->
      <div class="filter-bar">
        <button
          v-for="tab in filterTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ 'filter-tab--active': activeFilter === tab.value }"
          @click="activeFilter = tab.value"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="filter-tab-count"
            :class="{ 'filter-tab-count--active': activeFilter === tab.value }">
            {{ tab.count }}
          </span>
        </button>
      </div>

      <!-- ── ERROR ──────────────────────────────────────────────── -->
      <div v-if="error" class="error-banner">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        {{ error }}
      </div>

      <!-- ── SKELETON ───────────────────────────────────────────── -->
      <div v-if="loading" class="skeleton-list">
        <div v-for="n in 4" :key="n" class="skeleton-card">
          <div class="sk-line sk-line--sm"></div>
          <div class="sk-line sk-line--md"></div>
          <div class="sk-line sk-line--lg"></div>
        </div>
      </div>

      <!-- ── EMPTY ──────────────────────────────────────────────── -->
      <div v-else-if="!filteredOrders.length" class="empty-state">
        <div class="empty-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round">
            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 01-8 0"/>
          </svg>
        </div>
        <div class="empty-title">
          {{ activeFilter === 'ALL' ? 'Bạn chưa có đơn hàng nào' : `Không có đơn "${getTabLabel(activeFilter)}"` }}
        </div>
        <div class="empty-sub">Hãy khám phá sản phẩm và đặt hàng ngay!</div>
        <button class="empty-cta" @click="$router.push('/')">Mua sắm ngay</button>
      </div>

      <!-- ── ORDER CARDS ────────────────────────────────────────── -->
      <div v-else class="order-list">
        <div
          v-for="(order, idx) in filteredOrders"
          :key="order.orderId"
          class="order-card"
          :style="`--i: ${idx}`"
        >
          <!-- Card top row -->
          <div class="order-card-top">
            <div class="order-card-left">
              <div class="order-number">{{ order.orderNumber }}</div>
              <div class="order-meta">
                <span class="order-meta-item">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                  </svg>
                  {{ formatDate(order.createdAt) }}
                </span>
                <span class="order-meta-dot">·</span>
                <span class="order-meta-item">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
                  </svg>
                  {{ order.paymentMethod || '—' }}
                </span>
                <span class="order-meta-dot">·</span>
                <span class="order-meta-item order-meta-channel">{{ order.channel || '—' }}</span>
              </div>
            </div>
            <div class="order-card-right">
              <span :class="['status-badge', `status-badge--${order.status?.toLowerCase()}`]">
                <span class="status-dot"></span>
                {{ statusLabel(order.status) }}
              </span>
            </div>
          </div>

          <!-- Divider -->
          <div class="order-card-divider"></div>

          <!-- Card bottom row -->
          <div class="order-card-bottom">
            <div class="order-amount-block">
              <span class="order-amount-label">Tổng cộng</span>
              <span class="order-amount">{{ formatMoney(order.totalAmount) }}</span>
            </div>

            <div class="order-actions">
              <button
                v-if="canCancel(order.status)"
                class="action-btn action-btn--cancel"
                :class="{ 'action-btn--loading': cancelingId === order.orderId }"
                :disabled="cancelingId === order.orderId"
                @click.stop="onCancel(order.orderId)"
              >
                <svg v-if="cancelingId !== order.orderId" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
                <svg v-else class="spin-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                Hủy đơn
              </button>

              <button
                class="action-btn action-btn--detail"
                @click="$router.push(`/orders/${order.orderId}`)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                Xem chi tiết
              </button>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";

const router      = useRouter();
const orders      = ref([]);
const loading     = ref(false);
const error       = ref("");
const cancelingId = ref(null);
const activeFilter = ref("ALL");

onMounted(loadOrders);

// ── Load ──────────────────────────────────────────────────────────
async function loadOrders() {
  loading.value = true;
  error.value   = "";
  try {
    const res    = await ordersApi.getMyOrders();
    orders.value = res?.data || [];
  } catch (e) {
    error.value = e?.response?.data?.message || "Không tải được đơn hàng";
  } finally {
    loading.value = false;
  }
}

// ── Cancel ────────────────────────────────────────────────────────
async function onCancel(orderId) {
  const confirmed = confirm("Bạn có chắc muốn hủy đơn hàng này?");
  if (!confirmed) return;
  try {
    cancelingId.value = orderId;
    await ordersApi.cancel(orderId);
    await loadOrders();
  } catch (e) {
    error.value = e?.response?.data?.message || "Hủy đơn thất bại";
  } finally {
    cancelingId.value = null;
  }
}

// ── Helpers ───────────────────────────────────────────────────────
const STATUS_LABELS = {
  PENDING:    "Chờ xử lý",
  CONFIRMED:  "Đã xác nhận",
  PROCESSING: "Đang xử lý",
  PAID:       "Đã thanh toán",
  SHIPPING:   "Đang giao",
  DELIVERED:  "Đã giao",
  CANCELLED:  "Đã hủy",
  PARTIALLY_RETURNED: "Trả 1 phần",
  RETURNED:   "Đã trả hàng",
};

function statusLabel(s) { return STATUS_LABELS[s] || s; }
function canCancel(s)   { return ["PENDING", "CONFIRMED", "PAID"].includes(s); }
function countByStatus(s) { return orders.value.filter(o => o.status === s).length; }

function formatDate(d) {
  if (!d) return "—";
  return new Date(d).toLocaleString("vi-VN", {
    day: "2-digit", month: "2-digit", year: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
}
function formatMoney(v) {
  if (v == null) return "—";
  return Number(v).toLocaleString("vi-VN") + " ₫";
}

// ── Stats ─────────────────────────────────────────────────────────
const stats = computed(() => [
  { key: "total",      icon: "📦", color: "blue",   label: "Tổng đơn",    value: orders.value.length,                                         delay: 0   },
  { key: "pending",    icon: "⏳", color: "amber",  label: "Chờ xử lý",   value: countByStatus("PENDING"),                                    delay: 60  },
  { key: "processing", icon: "⚙️", color: "indigo", label: "Đang xử lý",  value: countByStatus("CONFIRMED") + countByStatus("PROCESSING"),    delay: 120 },
  { key: "shipping",   icon: "🚚", color: "cyan",   label: "Đang giao",   value: countByStatus("SHIPPING"),                                   delay: 180 },
  { key: "done",       icon: "✅", color: "green",  label: "Đã giao",     value: countByStatus("DELIVERED"),                                  delay: 240 },
  { key: "cancelled",  icon: "❌", color: "red",    label: "Đã hủy",      value: countByStatus("CANCELLED"),                                  delay: 300 },
]);

// ── Filter tabs ────────────────────────────────────────────────────
const filterTabs = computed(() => [
  { value: "ALL",       label: "Tất cả",     count: orders.value.length },
  { value: "PENDING",   label: "Chờ xử lý",  count: countByStatus("PENDING") },
  { value: "PAID",      label: "Đã thanh toán", count: countByStatus("PAID") },
  { value: "SHIPPING",  label: "Đang giao",  count: countByStatus("SHIPPING") },
  { value: "DELIVERED", label: "Đã giao",    count: countByStatus("DELIVERED") },
  { value: "CANCELLED", label: "Đã hủy",     count: countByStatus("CANCELLED") },
]);

const filteredOrders = computed(() => {
  if (activeFilter.value === "ALL") return orders.value;
  return orders.value.filter(o => o.status === activeFilter.value);
});

function getTabLabel(v) {
  return filterTabs.value.find(t => t.value === v)?.label || v;
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap");

/* ── VARIABLES ─────────────────────────────────────────────────── */
.orders-root {
  --blue:    #1d6fd8;
  --blue-dk: #1558b0;
  --blue-lt: #eff4fe;
  --blue-md: #a8c8f8;
  --ink:     #111827;
  --ink2:    #374151;
  --ink3:    #6b7280;
  --ink4:    #9ca3af;
  --ink5:    #d1d5db;
  --bg:      #f1f3f6;
  --card:    #ffffff;
  --border:  #e2e6ea;
  --rad:     14px;
  --rad-sm:  10px;
  --sha:     0 1px 3px rgba(0,0,0,0.06), 0 4px 14px rgba(0,0,0,0.05);

  min-height: 100vh;
  background: var(--bg);
  font-family: "Plus Jakarta Sans", sans-serif;
  color: var(--ink);
}

*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

/* ── PAGE TITLE ROW ────────────────────────────────────────────── */
.page-title-row {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; margin-bottom: 20px;
}
.page-title {
  font-size: 20px; font-weight: 800; color: var(--ink);
  letter-spacing: -0.02em; line-height: 1.2;
}
.page-sub {
  font-size: 13px; color: var(--ink4); margin-top: 3px; font-weight: 400;
}

.reload-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px; border-radius: var(--rad-sm);
  border: 1.5px solid var(--border); background: var(--card);
  font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 13px; font-weight: 600; color: var(--ink2);
  cursor: pointer; transition: all 0.15s;
}
.reload-btn:hover { border-color: var(--blue); color: var(--blue); background: var(--blue-lt); }
.reload-btn--spinning svg { animation: spin 0.7s linear infinite; }

/* ── BODY ──────────────────────────────────────────────────────── */
.orders-body {
  max-width: 1140px; margin: 0 auto;
  padding: 24px 32px 60px;
}

/* ── STATS ROW ─────────────────────────────────────────────────── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--rad);
  padding: 16px 14px;
  display: flex; flex-direction: column; align-items: center;
  gap: 4px; text-align: center;
  box-shadow: var(--sha);
  transition: transform 0.2s, box-shadow 0.2s;
  animation: card-in 0.4s ease both;
  animation-delay: var(--delay, 0ms);
  cursor: default;
}
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.09); }

@keyframes card-in { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: none; } }

.stat-icon { font-size: 20px; line-height: 1; margin-bottom: 4px; }
.stat-val  { font-size: 22px; font-weight: 800; line-height: 1.1; }
.stat-label{ font-size: 10.5px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.06em; color: var(--ink4); margin-top: 2px; }

.stat-card--blue   { border-top: 3px solid #1d6fd8; }
.stat-card--blue   .stat-val { color: #1d6fd8; }
.stat-card--amber  { border-top: 3px solid #d97706; }
.stat-card--amber  .stat-val { color: #d97706; }
.stat-card--indigo { border-top: 3px solid #7c3aed; }
.stat-card--indigo .stat-val { color: #7c3aed; }
.stat-card--cyan   { border-top: 3px solid #0891b2; }
.stat-card--cyan   .stat-val { color: #0891b2; }
.stat-card--green  { border-top: 3px solid #16a34a; }
.stat-card--green  .stat-val { color: #16a34a; }
.stat-card--red    { border-top: 3px solid #dc2626; }
.stat-card--red    .stat-val { color: #dc2626; }

/* ── FILTER BAR ────────────────────────────────────────────────── */
.filter-bar {
  display: flex; align-items: center; gap: 4px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--rad);
  padding: 6px;
  margin-bottom: 18px;
  box-shadow: var(--sha);
  overflow-x: auto;
  scrollbar-width: none;
}
.filter-bar::-webkit-scrollbar { display: none; }

.filter-tab {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px; border: none;
  font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 13px; font-weight: 500; color: var(--ink3);
  background: transparent; cursor: pointer; white-space: nowrap;
  transition: all 0.15s;
}
.filter-tab:hover { background: var(--bg); color: var(--ink); }
.filter-tab--active {
  background: var(--blue-lt);
  color: var(--blue);
  font-weight: 700;
}

.filter-tab-count {
  padding: 1px 7px; border-radius: 20px;
  font-size: 11px; font-weight: 700;
  background: var(--ink5); color: var(--ink3);
}
.filter-tab-count--active {
  background: var(--blue);
  color: #fff;
}

/* ── ERROR ─────────────────────────────────────────────────────── */
.error-banner {
  display: flex; align-items: center; gap: 8px;
  padding: 13px 16px; margin-bottom: 16px;
  background: #fef2f2; border: 1px solid #fecaca;
  border-radius: var(--rad-sm); color: #dc2626;
  font-size: 13px; font-weight: 500;
}

/* ── SKELETON ──────────────────────────────────────────────────── */
.skeleton-list { display: flex; flex-direction: column; gap: 12px; }
.skeleton-card {
  background: var(--card); border: 1px solid var(--border);
  border-radius: var(--rad); padding: 22px 24px;
  display: flex; flex-direction: column; gap: 12px;
}
.sk-line {
  border-radius: 6px; height: 13px;
  background: linear-gradient(90deg, #f1f3f6 25%, #e8eaed 50%, #f1f3f6 75%);
  background-size: 400% 100%;
  animation: shimmer 1.4s infinite;
}
.sk-line--sm { width: 30%; }
.sk-line--md { width: 55%; }
.sk-line--lg { width: 70%; }
@keyframes shimmer { 0% { background-position: 100% 0; } 100% { background-position: -100% 0; } }

/* ── EMPTY ─────────────────────────────────────────────────────── */
.empty-state {
  background: var(--card); border: 1px solid var(--border);
  border-radius: var(--rad); padding: 56px 24px;
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  text-align: center;
}
.empty-icon {
  width: 80px; height: 80px; border-radius: 50%;
  background: var(--blue-lt); border: 1px solid var(--blue-md);
  display: flex; align-items: center; justify-content: center;
  color: var(--blue); margin-bottom: 6px;
}
.empty-title { font-size: 16px; font-weight: 700; color: var(--ink); }
.empty-sub   { font-size: 13.5px; color: var(--ink4); }
.empty-cta {
  margin-top: 8px; padding: 10px 28px;
  background: var(--blue); color: #fff; border: none;
  border-radius: var(--rad-sm); font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 13.5px; font-weight: 600; cursor: pointer;
  transition: background 0.15s;
}
.empty-cta:hover { background: var(--blue-dk); }

/* ── ORDER LIST ────────────────────────────────────────────────── */
.order-list { display: flex; flex-direction: column; gap: 12px; }

.order-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--rad);
  padding: 20px 24px;
  box-shadow: var(--sha);
  transition: border-color 0.15s, box-shadow 0.2s, transform 0.15s;
  animation: card-in 0.35s ease both;
  animation-delay: calc(var(--i) * 50ms);
  cursor: pointer;
}
.order-card:hover {
  border-color: var(--blue-md);
  box-shadow: 0 4px 20px rgba(29,111,216,0.1);
  transform: translateY(-1px);
}

/* Card top */
.order-card-top {
  display: flex; align-items: flex-start;
  justify-content: space-between; gap: 16px;
}
.order-number {
  font-size: 14.5px; font-weight: 700; color: var(--ink);
  letter-spacing: -0.01em; margin-bottom: 6px;
  font-family: "Plus Jakarta Sans", monospace;
}
.order-meta {
  display: flex; align-items: center; flex-wrap: wrap; gap: 6px;
}
.order-meta-item {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 12px; color: var(--ink4); font-weight: 500;
}
.order-meta-dot { color: var(--ink5); font-size: 12px; }
.order-meta-channel {
  padding: 1px 8px; border-radius: 20px;
  background: var(--bg); border: 1px solid var(--border);
  font-size: 11px; font-weight: 600; color: var(--ink3); text-transform: uppercase;
}

/* Status badges */
.status-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 5px 13px; border-radius: 20px;
  font-size: 12px; font-weight: 600;
  white-space: nowrap; flex-shrink: 0;
}
.status-dot {
  width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0;
}

.status-badge--pending     { background: #fffbeb; border: 1px solid #fde68a; color: #92400e; }
.status-badge--pending     .status-dot { background: #f59e0b; box-shadow: 0 0 5px rgba(245,158,11,0.5); }
.status-badge--paid        { background: #eff4fe; border: 1px solid var(--blue-md); color: #1d4ed8; }
.status-badge--paid        .status-dot { background: var(--blue); }
.status-badge--confirmed   { background: #f5f3ff; border: 1px solid #ddd6fe; color: #5b21b6; }
.status-badge--confirmed   .status-dot { background: #7c3aed; }
.status-badge--processing  { background: #f5f3ff; border: 1px solid #ddd6fe; color: #5b21b6; }
.status-badge--processing  .status-dot { background: #7c3aed; animation: pulse-dot 1.5s ease-in-out infinite; }
.status-badge--shipping    { background: #ecfeff; border: 1px solid #a5f3fc; color: #164e63; }
.status-badge--shipping    .status-dot { background: #0891b2; animation: pulse-dot 1.5s ease-in-out infinite; }
.status-badge--delivered   { background: #f0fdf4; border: 1px solid #bbf7d0; color: #14532d; }
.status-badge--delivered   .status-dot { background: #16a34a; }
.status-badge--cancelled   { background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; }
.status-badge--cancelled   .status-dot { background: #dc2626; }
.status-badge--returned    { background: #fff7ed; border: 1px solid #fed7aa; color: #7c2d12; }
.status-badge--returned    .status-dot { background: #ea580c; }
.status-badge--partially_returned { background: #fff7ed; border: 1px solid #fed7aa; color: #7c2d12; }
.status-badge--partially_returned .status-dot { background: #ea580c; }

@keyframes pulse-dot { 0%,100% { opacity: 1; } 50% { opacity: 0.4; transform: scale(0.8); } }

/* Card divider */
.order-card-divider {
  height: 1px; background: var(--border);
  margin: 16px 0;
}

/* Card bottom */
.order-card-bottom {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
}
.order-amount-block { display: flex; flex-direction: column; gap: 2px; }
.order-amount-label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.07em; color: var(--ink4); }
.order-amount { font-size: 18px; font-weight: 800; color: var(--blue); letter-spacing: -0.02em; }

.order-actions { display: flex; align-items: center; gap: 8px; }

.action-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: var(--rad-sm);
  font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 13px; font-weight: 600; cursor: pointer;
  transition: all 0.15s; border: 1.5px solid;
}
.action-btn--detail {
  background: var(--blue); color: #fff; border-color: var(--blue);
  box-shadow: 0 2px 8px rgba(29,111,216,0.25);
}
.action-btn--detail:hover { background: var(--blue-dk); border-color: var(--blue-dk); transform: translateY(-1px); box-shadow: 0 4px 14px rgba(29,111,216,0.3); }

.action-btn--cancel {
  background: #fff; color: #dc2626; border-color: #fca5a5;
}
.action-btn--cancel:hover { background: #fef2f2; border-color: #dc2626; }
.action-btn--cancel:disabled { opacity: 0.6; cursor: not-allowed; }
.action-btn--loading { opacity: 0.7; pointer-events: none; }

/* ── ANIMATIONS ────────────────────────────────────────────────── */
.spin-icon { animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── RESPONSIVE ────────────────────────────────────────────────── */
@media (max-width: 1024px) {
  .stats-row { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 720px) {
  .orders-body  { padding: 16px 16px 48px; }
  .page-title   { font-size: 17px; }
  .stats-row    { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .stat-val     { font-size: 18px; }
  .order-card   { padding: 16px; }
  .order-card-bottom { flex-direction: column; align-items: flex-start; gap: 12px; }
  .order-actions { width: 100%; }
  .action-btn   { flex: 1; justify-content: center; }
  .order-amount { font-size: 16px; }
}
@media (max-width: 480px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .filter-tab { font-size: 12px; padding: 7px 12px; }
}
</style>