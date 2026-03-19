<template>
  <div class="pickup-root">
    <!-- ── Header ── -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Nhận hàng tại quầy</h1>
        <p class="page-sub">Tìm đơn hàng theo số điện thoại hoặc email</p>
      </div>
    </div>

    <!-- ── Search ── -->
    <div class="search-section">
      <div class="search-bar">
        <div class="search-wrap" :class="{ focused: searchFocused }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" class="s-icon">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>
          </svg>
          <input
            ref="searchInput"
            v-model="query"
            class="s-input"
            placeholder="Nhập SĐT hoặc email khách hàng..."
            @focus="searchFocused = true"
            @blur="searchFocused = false"
            @keyup.enter="doSearch"
          />
          <button v-if="query" class="s-clear" @click="resetAll" tabindex="-1">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <button class="s-btn" @click="doSearch" :disabled="loading || !query.trim()">
          <span v-if="!loading">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="1.8"/>
              <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
            Tìm kiếm
          </span>
          <span v-else class="spinner-sm"></span>
        </button>
      </div>

      <!-- Error -->
      <transition name="fade-slide">
        <div v-if="errorMsg" class="error-bar">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke="currentColor" stroke-width="1"/>
          </svg>
          {{ errorMsg }}
        </div>
      </transition>
    </div>

    <!-- ── Results ── -->
    <transition name="fade-slide">
      <div v-if="customer" class="result-area">
        <!-- Customer card -->
        <div class="customer-card">
          <div class="cust-avatar">{{ initials(customer.fullName || customer.name) }}</div>
          <div class="cust-info">
            <h3 class="cust-name">{{ customer.fullName || customer.name }}</h3>
            <div class="cust-meta">
              <span v-if="customer.phone">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M22 16.92v3a2 2 0 0 1-2.18 2A19.79 19.79 0 0 1 3.08 4.18 2 2 0 0 1 5.07 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L9.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                {{ customer.phone }}
              </span>
              <span v-if="customer.email">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" stroke="currentColor" stroke-width="1.8"/><polyline points="22,6 12,13 2,6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                {{ customer.email }}
              </span>
            </div>
          </div>
          <span v-if="customer.vipTier" class="vip-badge">{{ customer.vipTier }}</span>
          <div class="cust-order-count">
            <span class="count-num">{{ paidOrders.length }}</span>
            <span class="count-label">đơn chờ lấy</span>
          </div>
        </div>

        <!-- Orders list -->
        <div v-if="paidOrders.length === 0" class="no-orders">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" opacity="0.25">
            <rect x="1" y="3" width="15" height="13" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <path d="M16 8h4a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="7" cy="19" r="2" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="17" cy="19" r="2" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          <p>Không có đơn hàng nào đang chờ lấy</p>
          <span>Chỉ hiển thị đơn đã thanh toán (PAID)</span>
        </div>

        <div v-else class="orders-grid">
          <div
            v-for="order in paidOrders"
            :key="orderKey(order)"
            class="order-card"
            :class="{ expanded: expandedOrder === orderKey(order) }"
          >
            <!-- Order summary row -->
            <div class="order-row" @click="toggleExpand(order)">
              <div class="order-info">
                <span class="order-id">{{ order.orderNumber ?? order.order_number ?? ('#' + order.id) }}</span>
                <span class="order-date">{{ formatDate(order.createdAt ?? order.orderDate ?? order.date) }}</span>
              </div>
              <div class="order-center">
                <span class="order-channel" :class="channelClass(order.channel)">
                  {{ channelLabel(order.channel) }}
                </span>
              </div>
              <div class="order-right">
                <span class="order-total">{{ formatMoney(order.totalAmount) }}</span>
                <span class="order-status paid">ĐÃ THANH TOÁN</span>
              </div>
              <svg
                class="expand-icon"
                :class="{ rotated: expandedOrder === orderKey(order) }"
                width="14" height="14" viewBox="0 0 24 24" fill="none"
              >
                <polyline points="6 9 12 15 18 9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>

            <!-- Expanded: items + confirm button -->
            <transition name="expand">
              <div v-if="expandedOrder === orderKey(order)" class="order-detail">
                <div class="order-items">
                  <div v-if="orderItems[orderKey(order)]" class="items-list">
                    <div v-for="item in orderItems[orderKey(order)]" :key="item.id" class="item-row">
                      <span class="item-name">{{ item.productName || item.variant?.product?.name }}</span>
                      <span class="item-variant">{{ item.variantName || item.variant?.name }}</span>
                      <span class="item-qty">x{{ item.quantity }}</span>
                      <div class="item-price-col">
                        <span v-if="item.discount && Number(item.discount) > 0" class="item-original-price">
                          {{ formatMoney(Number(item.unitPrice) * Number(item.quantity)) }}
                        </span>
                        <span class="item-price">
                          {{ formatMoney(item.lineTotal ?? item.totalPrice ?? (Number(item.unitPrice) * Number(item.quantity))) }}
                        </span>
                        <span v-if="item.discount && Number(item.discount) > 0" class="item-discount">
                          -{{ formatMoney(item.discount) }}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div v-else class="items-loading">
                    <span class="spinner-sm dark"></span>
                    <span>Đang tải...</span>
                  </div>
                </div>

                <!-- Notes -->
                <div v-if="order.notes" class="order-notes">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="1.8"/><polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="1.8"/></svg>
                  Ghi chú: {{ order.notes }}
                </div>

                <!-- Confirm pickup -->
                <div class="confirm-row">
                  <transition name="fade-slide">
                    <div v-if="confirmError[orderKey(order)]" class="confirm-error">
                      {{ confirmError[orderKey(order)] }}
                    </div>
                  </transition>
                  <button
                    class="btn-confirm"
                    :class="{ loading: confirmLoading[orderKey(order)] }"
                    @click="confirmPickup(order)"
                    :disabled="confirmLoading[orderKey(order)]"
                  >
                    <span v-if="!confirmLoading[orderKey(order)]">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                        <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                      Xác nhận đã giao hàng
                    </span>
                    <span v-else class="spinner-sm"></span>
                  </button>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </transition>

    <!-- ── Empty state ── -->
    <div v-if="!customer && !loading" class="empty-state">
      <div class="empty-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
          <path d="M20 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z" stroke="currentColor" stroke-width="1.3"/>
          <path d="M16 3H8l-2 4h12l-2-4z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
          <path d="M12 12v4M10 14h4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
        </svg>
      </div>
      <p class="empty-title">Tìm đơn hàng cần lấy</p>
      <p class="empty-sub">Nhập SĐT hoặc email của khách để tra cứu đơn đã thanh toán online</p>
    </div>

    <!-- ── Pickup Success Toast ── -->
    <transition name="toast">
      <div v-if="successMsg" class="toast-success">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/>
          <polyline points="7 12 10 15 17 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        {{ successMsg }}
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { customersApi } from "../../api/customers.api";
import { ordersApi } from "../../api/orders.api";

const searchInput = ref(null);
const query = ref("");
const searchFocused = ref(false);
const loading = ref(false);
const errorMsg = ref("");
const customer = ref(null);
const paidOrders = ref([]);
const expandedOrder = ref(null);
const orderItems = ref({}); // { orderId: [...items] }
const confirmLoading = ref({});
const confirmError = ref({});
const successMsg = ref("");

async function doSearch() {
  const q = query.value.trim().toLowerCase();
  if (!q) return;
  loading.value = true;
  errorMsg.value = "";
  customer.value = null;
  paidOrders.value = [];
  expandedOrder.value = null;
  orderItems.value = {};

  try {
    // Lấy tất cả customer rồi filter (TODO: thêm endpoint search riêng phía backend)
    const cusRes = await customersApi.listAll();
    const cusList = cusRes?.data?.data ?? cusRes?.data ?? [];
    const found = cusList.find(
      (c) =>
        (c.phone || "").includes(q) ||
        (c.email || "").toLowerCase().includes(q)
    );
    if (!found) {
      errorMsg.value = "Không tìm thấy khách hàng với SĐT hoặc email này.";
      return;
    }
    customer.value = found;

    // Lấy đơn hàng đã thanh toán của khách
    const ordRes = await ordersApi.filter(found.id, null, null, null, "PAID");
    const rawOrders = ordRes?.data?.data ?? ordRes?.data?.content ?? ordRes?.data ?? [];
    const orders = Array.isArray(rawOrders) ? rawOrders : [];
    // Lọc đơn online (không phải IN_STORE)
    paidOrders.value = orders.filter(
      (o) => !o.channel || o.channel !== "IN_STORE"
    );
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || "Lỗi tìm kiếm. Thử lại.";
  } finally {
    loading.value = false;
  }
}

// Helper: dùng orderNumber làm key duy nhất, tránh undefined id
function orderKey(order) {
  return order.orderNumber ?? order.order_number ?? String(order.id ?? Math.random());
}

async function toggleExpand(order) {
  const key = orderKey(order);
  if (expandedOrder.value === key) {
    expandedOrder.value = null;
    return;
  }
  expandedOrder.value = key;
  if (!orderItems.value[key]) {
    try {
      const oid = order.id ?? order.orderId;
      const res = await ordersApi.getOrderDetail(oid);
      const detail = res?.data?.data ?? res?.data;
      orderItems.value[key] = detail?.orderItems ?? detail?.items ?? detail?.orderItemList ?? [];
    } catch {
      orderItems.value[key] = [];
    }
  }
}

async function confirmPickup(order) {
  const key = orderKey(order);
  const oid = order.id ?? order.orderId;
  const displayId = order.orderNumber ?? order.order_number ?? ('#' + order.id);
  confirmLoading.value = { ...confirmLoading.value, [key]: true };
  confirmError.value = { ...confirmError.value, [key]: "" };
  try {
    // State machine: PAID → PROCESSING → SHIPPING → DELIVERED
    await ordersApi.markAsProcessing(oid).catch(() => {});
    await ordersApi.markAsShipping(oid).catch(() => {});
    await ordersApi.markAsDelivered(oid);
    paidOrders.value = paidOrders.value.filter((o) => orderKey(o) !== key);
    expandedOrder.value = null;
    showSuccess(`Đơn ${displayId} đã xác nhận giao thành công!`);
  } catch (e) {
    confirmError.value = {
      ...confirmError.value,
      [key]: e?.response?.data?.message || "Xác nhận thất bại.",
    };
  } finally {
    confirmLoading.value = { ...confirmLoading.value, [key]: false };
  }
}

function showSuccess(msg) {
  successMsg.value = msg;
  setTimeout(() => (successMsg.value = ""), 4000);
}

function resetAll() {
  query.value = "";
  errorMsg.value = "";
  customer.value = null;
  paidOrders.value = [];
  expandedOrder.value = null;
  orderItems.value = {};
  searchInput.value?.focus();
}

// ── Helpers ──────────────────────────────────────────────────────
function formatMoney(v) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(v || 0);
}
function formatDate(d) {
  if (!d) return "—";
  return new Date(d).toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}
function initials(name = "") {
  return (name || "")
    .split(" ")
    .map((w) => w[0])
    .slice(0, 2)
    .join("")
    .toUpperCase() || "KH";
}
function channelLabel(ch) {
  return { ONLINE: "Online", IN_STORE: "Tại quầy", PHONE: "Điện thoại" }[ch] || ch || "Online";
}
function channelClass(ch) {
  return { ONLINE: "ch-online", IN_STORE: "ch-store", PHONE: "ch-phone" }[ch] || "ch-online";
}

onMounted(() => searchInput.value?.focus());
</script>

<style scoped>
.pickup-root {
  padding: 24px 28px;
  min-height: 100%;
  background: #f4f6f8;
  font-family: 'Inter', 'Segoe UI', system-ui, sans-serif;
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
}

/* ── Header ── */
.page-header { display: flex; align-items: center; justify-content: space-between; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; margin: 0 0 4px; letter-spacing: -0.4px; }
.page-sub { font-size: 13px; color: #64748b; margin: 0; }

/* ── Search ── */
.search-section { display: flex; flex-direction: column; gap: 10px; }
.search-bar { display: flex; gap: 10px; max-width: 600px; }
.search-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #ffffff;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 0 14px;
  height: 48px;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.search-wrap.focused {
  border-color: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245,158,11,0.08);
}
.s-icon { color: #94a3b8; flex-shrink: 0; }
.search-wrap.focused .s-icon { color: #f59e0b; }
.s-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #0f172a;
  font-size: 14px;
  font-family: inherit;
}
.s-input::placeholder { color: #cbd5e1; }
.s-clear {
  background: none; border: none; cursor: pointer;
  color: #94a3b8; display: flex; padding: 3px; transition: color 0.15s;
}
.s-clear:hover { color: #475569; }
.s-btn {
  height: 48px;
  padding: 0 22px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.15s;
  font-family: inherit;
  display: flex;
  align-items: center;
  gap: 7px;
  white-space: nowrap;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(245,158,11,0.3);
}
.s-btn span { display: flex; align-items: center; gap: 7px; }
.s-btn:hover:not(:disabled) { opacity: 0.9; }
.s-btn:disabled { opacity: 0.35; cursor: not-allowed; box-shadow: none; }
.error-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  font-size: 13px;
  padding: 10px 14px;
  border-radius: 8px;
  max-width: 600px;
}

/* ── Customer card ── */
.result-area { display: flex; flex-direction: column; gap: 16px; }
.customer-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid #fde68a;
  border-radius: 12px;
  max-width: 700px;
  box-shadow: 0 2px 8px rgba(245,158,11,0.08);
}
.cust-avatar {
  width: 44px; height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 700; color: #fff;
  flex-shrink: 0;
}
.cust-info { flex: 1; display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.cust-name { font-size: 16px; font-weight: 700; color: #0f172a; margin: 0; }
.cust-meta { display: flex; gap: 14px; }
.cust-meta span {
  display: flex; align-items: center; gap: 5px;
  font-size: 12.5px; color: #64748b;
}
.vip-badge {
  font-size: 11px; font-weight: 700; color: #b45309;
  background: #fef3c7; border: 1px solid #fde68a;
  padding: 3px 10px; border-radius: 12px; flex-shrink: 0;
}
.cust-order-count {
  text-align: center;
  padding: 8px 16px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 10px;
  flex-shrink: 0;
}
.count-num { display: block; font-size: 22px; font-weight: 800; color: #d97706; }
.count-label { display: block; font-size: 10.5px; color: #92400e; font-weight: 500; }

/* ── No orders ── */
.no-orders {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  color: #94a3b8;
  text-align: center;
  max-width: 700px;
}
.no-orders p { font-size: 14px; font-weight: 600; color: #64748b; margin: 0; }
.no-orders span { font-size: 12.5px; }

/* ── Orders grid ── */
.orders-grid { display: flex; flex-direction: column; gap: 10px; max-width: 700px; }
.order-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.order-card:hover { border-color: #fde68a; box-shadow: 0 2px 8px rgba(245,158,11,0.08); }
.order-card.expanded { border-color: #fde68a; box-shadow: 0 4px 16px rgba(245,158,11,0.1); }

.order-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  cursor: pointer;
  user-select: none;
}
.order-row:hover { background: #fffbeb; }
.order-info { display: flex; flex-direction: column; gap: 3px; min-width: 80px; }
.order-id { font-size: 13.5px; font-weight: 700; color: #0f172a; font-family: monospace; }
.order-date { font-size: 11px; color: #94a3b8; }
.order-center { flex: 1; }
.order-channel {
  font-size: 11px; font-weight: 600;
  padding: 3px 9px; border-radius: 12px;
  letter-spacing: 0.03em;
}
.ch-online { background: #dbeafe; color: #1d4ed8; border: 1px solid #bfdbfe; }
.ch-store { background: #fef3c7; color: #b45309; border: 1px solid #fde68a; }
.ch-phone { background: #f3e8ff; color: #7c3aed; border: 1px solid #ddd6fe; }
.order-right { display: flex; flex-direction: column; align-items: flex-end; gap: 3px; }
.order-total { font-size: 14px; font-weight: 700; color: #0f172a; }
.order-status { font-size: 10px; font-weight: 700; letter-spacing: 0.08em; }
.order-status.paid { color: #16a34a; }
.expand-icon { color: #94a3b8; transition: transform 0.2s; flex-shrink: 0; }
.expand-icon.rotated { transform: rotate(180deg); }

/* ── Order detail ── */
.order-detail {
  border-top: 1px solid #f1f5f9;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #fafafa;
}
.order-items {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}
.items-list { display: flex; flex-direction: column; }
.item-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid #f8fafc;
}
.item-row:last-child { border-bottom: none; }
.item-name { flex: 1; font-size: 13px; font-weight: 600; color: #0f172a; }
.item-variant { font-size: 11.5px; color: #64748b; min-width: 80px; }
.item-qty { font-size: 12px; font-weight: 600; color: #94a3b8; min-width: 30px; text-align: center; }
.item-price { font-size: 13px; font-weight: 700; color: #0f172a; min-width: 90px; text-align: right; }
.item-price-col { display: flex; flex-direction: column; align-items: flex-end; min-width: 100px; gap: 1px; }
.item-original-price { font-size: 11px; color: #94a3b8; text-decoration: line-through; }
.item-discount { font-size: 11px; font-weight: 600; color: #16a34a; }
.items-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px;
  font-size: 13px;
  color: #94a3b8;
}
.order-notes {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12.5px;
  color: #64748b;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 8px 12px;
  border-radius: 7px;
}
.confirm-row { display: flex; flex-direction: column; gap: 8px; }
.confirm-error {
  font-size: 12.5px;
  color: #dc2626;
  padding: 8px 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 7px;
}
.btn-confirm {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  border-radius: 9px;
  color: white;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.15s;
  font-family: inherit;
  box-shadow: 0 2px 8px rgba(16,185,129,0.25);
}
.btn-confirm span { display: flex; align-items: center; gap: 8px; }
.btn-confirm:hover:not(:disabled) { opacity: 0.9; }
.btn-confirm:disabled { opacity: 0.4; cursor: not-allowed; box-shadow: none; }

/* ── Empty state ── */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #94a3b8;
  text-align: center;
  padding: 60px 40px;
}
.empty-icon { color: #cbd5e1; margin-bottom: 8px; }
.empty-title { font-size: 16px; font-weight: 600; color: #64748b; margin: 0; }
.empty-sub { font-size: 13px; color: #94a3b8; margin: 0; max-width: 400px; line-height: 1.5; }

/* ── Toast ── */
.toast-success {
  position: fixed;
  bottom: 28px;
  right: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #ecfdf5;
  border: 1px solid #6ee7b7;
  color: #065f46;
  font-size: 13.5px;
  font-weight: 600;
  padding: 12px 18px;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(16,185,129,0.15);
  z-index: 300;
}
.toast-enter-active { transition: all 0.3s cubic-bezier(0.34,1.3,0.64,1); }
.toast-leave-active { transition: all 0.2s ease; }
.toast-enter-from { opacity: 0; transform: translateY(16px) scale(0.95); }
.toast-leave-to { opacity: 0; transform: translateY(8px); }

/* ── Spinner ── */
.spinner-sm {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  display: inline-block;
}
.spinner-sm.dark {
  border-color: #e2e8f0;
  border-top-color: #64748b;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Transitions ── */
.fade-slide-enter-active { transition: all 0.25s ease; }
.fade-slide-leave-active { transition: all 0.15s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(-6px); }
.fade-slide-leave-to { opacity: 0; }
.expand-enter-active { transition: all 0.25s ease; }
.expand-leave-active { transition: all 0.2s ease; }
.expand-enter-from { opacity: 0; max-height: 0; }
.expand-leave-to { opacity: 0; max-height: 0; }
</style>
