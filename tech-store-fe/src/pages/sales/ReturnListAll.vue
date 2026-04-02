<template>
  <div class="ar-page">
    <!-- Header -->
    <div class="ar-header">
      <div class="ar-header__left">
        <div class="ar-header__eyebrow">
          <svg
            width="12"
            height="12"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
          Bán hàng
        </div>
        <h1 class="ar-header__title">Tất cả hoàn hàng</h1>
        <p class="ar-header__sub">
          Lịch sử toàn bộ yêu cầu hoàn hàng &amp; trạng thái xử lý
        </p>
      </div>

      <div class="ar-header__right">
        <!-- Summary chips -->
        <div class="ar-summary">
          <div class="ar-summary__item" v-for="s in summary" :key="s.label">
            <span
              class="ar-summary__dot"
              :style="{ background: s.color }"
            ></span>
            <span class="ar-summary__count">{{ s.count }}</span>
            <span class="ar-summary__label">{{ s.label }}</span>
          </div>
        </div>
        <button
          class="ar-btn ar-btn--reload"
          :class="{ 'ar-btn--loading': loading }"
          @click="load"
          :disabled="loading"
        >
          <svg
            v-if="!loading"
            width="15"
            height="15"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.2"
          >
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8" />
            <path d="M21 3v5h-5" />
            <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16" />
            <path d="M8 16H3v5" />
          </svg>
          <span class="ar-spinner" v-else />
          Tải lại
        </button>
      </div>
    </div>

    <!-- Table card -->
    <div class="ar-card">
      <!-- Toolbar -->
      <div class="ar-toolbar">
        <div class="ar-search">
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.2"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
          <input
            v-model="search"
            class="ar-search__input"
            placeholder="Tìm mã đơn, sản phẩm, lý do..."
          />
          <button v-if="search" class="ar-search__clear" @click="search = ''">
            <svg
              width="12"
              height="12"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="ar-filters">
          <select v-model="filterStatus" class="ar-select">
            <option value="">Tất cả trạng thái</option>
            <option value="RETURN_REQUESTED">Return Requested</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="PENDING">Pending</option>
          </select>
          <select v-model="filterRefund" class="ar-select">
            <option value="">Tất cả hoàn tiền</option>
            <option value="PENDING">Pending</option>
            <option value="REFUNDED">Refunded</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>

        <div class="ar-count">
          <span class="ar-count__num">{{ filtered.length }}</span>
          <span class="ar-count__label">kết quả</span>
        </div>
      </div>

      <!-- Table wrap -->
      <div class="ar-table-wrap" :class="{ 'ar-table-wrap--loading': loading }">
        <div class="ar-loader-overlay" v-if="loading">
          <div class="ar-loader-ring" />
        </div>

        <table class="ar-table">
          <thead>
            <tr>
              <th style="width: 80px">Mã HH</th>
              <th style="width: 100px">Đơn hàng</th>
              <th style="min-width: 190px">Sản phẩm</th>
              <th style="width: 70px; text-align: center">SL</th>
              <th style="width: 145px; text-align: right">Hoàn tiền</th>
              <th style="width: 155px">Trạng thái</th>
              <th style="width: 155px">Hoàn tiền</th>
              <th style="min-width: 160px">Lý do</th>
              <th style="min-width: 160px">Ghi chú</th>
              <th style="width: 155px">Ngày tạo</th>
              <th style="width: 155px">Ngày xử lý</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && filtered.length === 0">
              <td colspan="11" class="ar-empty">
                <div class="ar-empty__inner">
                  <div class="ar-empty__icon">
                    <svg
                      width="32"
                      height="32"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.4"
                    >
                      <circle cx="11" cy="11" r="8" />
                      <path d="m21 21-4.35-4.35" />
                    </svg>
                  </div>
                  <p>Không tìm thấy kết quả nào</p>
                  <button
                    v-if="search || filterStatus || filterRefund"
                    class="ar-clear-filter"
                    @click="clearFilters"
                  >
                    Xóa bộ lọc
                  </button>
                </div>
              </td>
            </tr>
            <tr v-for="row in filtered" :key="row.id" class="ar-row">
              <td>
                <span class="ar-id">#{{ row.id }}</span>
              </td>
              <td>
                <div class="ar-order">
                  <span class="ar-order__num">{{ row.orderNumber }}</span>
                  <span class="ar-order__id">{{ row.orderId }}</span>
                </div>
              </td>
              <td>
                <div class="ar-product">
                  <span class="ar-product__name">{{ row.productName }}</span>
                  <span class="ar-product__variant" v-if="row.variantName">{{
                    row.variantName
                  }}</span>
                </div>
              </td>
              <td style="text-align: center">
                <span class="ar-qty">× {{ row.quantity }}</span>
              </td>
              <td style="text-align: right">
                <span class="ar-amount">{{
                  formatMoney(row.refundAmount)
                }}</span>
              </td>
              <td>
                <span
                  class="ar-tag"
                  :class="`ar-tag--${statusVariant(row.status)}`"
                >
                  <span class="ar-tag__dot"></span>
                  {{ statusLabel(row.status) }}
                </span>
              </td>
              <td>
                <span
                  class="ar-tag"
                  :class="`ar-tag--${refundVariant(row.refundStatus)}`"
                >
                  <span class="ar-tag__dot"></span>
                  {{ refundLabel(row.refundStatus) }}
                </span>
              </td>
              <td>
                <span class="ar-text-cell" :title="row.reason">{{
                  row.reason || "—"
                }}</span>
              </td>
              <td>
                <span
                  class="ar-text-cell ar-text-cell--muted"
                  :title="row.note"
                  >{{ row.note || "—" }}</span
                >
              </td>
              <td>
                <span class="ar-date">{{ formatDate(row.createdAt) }}</span>
              </td>
              <td>
                <span
                  class="ar-date"
                  :class="{ 'ar-date--muted': !row.refundedAt }"
                  >{{ formatDate(row.refundedAt) }}</span
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Footer bar -->
      <div class="ar-footer" v-if="!loading && filtered.length > 0">
        <span class="ar-footer__total">
          Tổng hoàn tiền:
          <strong>{{ formatMoney(totalRefund) }}</strong>
        </span>
        <span class="ar-footer__rows"
          >{{ filtered.length }} / {{ returns.length }} bản ghi</span
        >
      </div>
    </div>

    <!-- Toast -->
    <Teleport to="body">
      <Transition name="ar-toast-slide">
        <div v-if="toastMsg" class="ar-toast" :class="`ar-toast--${toastType}`">
          <svg
            v-if="toastType === 'error'"
            width="15"
            height="15"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          {{ toastMsg }}
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { returnsApi } from "../../api/returns.api";

const returns = ref([]);
const loading = ref(false);
const search = ref("");
const filterStatus = ref("");
const filterRefund = ref("");
const toastMsg = ref("");
const toastType = ref("error");
let toastTimer = null;

onMounted(load);

async function load() {
  loading.value = true;
  try {
    const res = await returnsApi.getAll();
    returns.value = res?.data?.data || res?.data || [];
  } catch (e) {
    showToast(
      e?.response?.data?.message || "Không thể tải danh sách hoàn hàng",
      "error",
    );
  } finally {
    loading.value = false;
  }
}

const filtered = computed(() => {
  return returns.value.filter((r) => {
    const q = search.value.toLowerCase();
    if (
      q &&
      ![r.orderNumber, r.productName, r.reason, r.note, String(r.id)].some(
        (v) => v?.toLowerCase().includes(q),
      )
    )
      return false;
    if (filterStatus.value && r.status !== filterStatus.value) return false;
    if (filterRefund.value && r.refundStatus !== filterRefund.value)
      return false;
    return true;
  });
});

const totalRefund = computed(() =>
  filtered.value.reduce((s, r) => s + (r.refundAmount || 0), 0),
);

const summary = computed(() => {
  const all = returns.value;
  return [
    {
      label: "Approved",
      count: all.filter((r) => r.status === "APPROVED").length,
      color: "#16a34a",
    },
    {
      label: "Pending",
      count: all.filter(
        (r) => r.status === "RETURN_REQUESTED" || r.status === "PENDING",
      ).length,
      color: "#f59e0b",
    },
    {
      label: "Rejected",
      count: all.filter((r) => r.status === "REJECTED").length,
      color: "#dc2626",
    },
  ];
});

function clearFilters() {
  search.value = "";
  filterStatus.value = "";
  filterRefund.value = "";
}

function formatMoney(val) {
  if (val == null) return "—";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(val);
}

function formatDate(val) {
  if (!val) return "—";
  return new Date(val).toLocaleString("vi-VN");
}

function statusVariant(s) {
  return (
    {
      APPROVED: "green",
      REJECTED: "red",
      RETURN_REQUESTED: "orange",
      PENDING: "orange",
    }[s] || "gray"
  );
}
function statusLabel(s) {
  return (
    {
      APPROVED: "Đã duyệt",
      REJECTED: "Từ chối",
      RETURN_REQUESTED: "Chờ duyệt",
      PENDING: "Chờ xử lý",
    }[s] || s
  );
}
function refundVariant(s) {
  return { REFUNDED: "green", FAILED: "red", PENDING: "orange" }[s] || "gray";
}
function refundLabel(s) {
  return (
    { REFUNDED: "Đã hoàn", FAILED: "Thất bại", PENDING: "Chờ hoàn" }[s] || s
  );
}

function showToast(msg, type = "error") {
  clearTimeout(toastTimer);
  toastMsg.value = msg;
  toastType.value = type;
  toastTimer = setTimeout(() => (toastMsg.value = ""), 3500);
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

.ar-page {
  --c-bg: #f6f7f9;
  --c-card: #ffffff;
  --c-border: #e4e7ec;
  --c-border-light: #f0f2f5;
  --c-text: #0f1117;
  --c-muted: #6b7280;
  --c-subtle: #9ca3af;
  --c-blue: #2563eb;
  --c-blue-bg: #eff6ff;
  --c-green: #16a34a;
  --c-green-bg: #f0fdf4;
  --c-green-border: #bbf7d0;
  --c-red: #dc2626;
  --c-red-bg: #fff1f2;
  --c-red-border: #fecdd3;
  --c-orange: #d97706;
  --c-orange-bg: #fffbeb;
  --c-orange-border: #fde68a;
  --c-gray-bg: #f3f4f6;
  --c-gray-border: #e5e7eb;
  --radius: 12px;
  --radius-sm: 8px;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--c-bg);
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: var(--c-text);
  box-sizing: border-box;
}

/* ── Header ──────────────────────── */
.ar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}
.ar-header__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-muted);
  margin-bottom: 6px;
}
.ar-header__title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
}
.ar-header__sub {
  font-size: 13.5px;
  color: var(--c-muted);
  margin: 0;
}

.ar-header__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

/* Summary chips */
.ar-summary {
  display: flex;
  gap: 6px;
}
.ar-summary__item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 20px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  font-size: 12px;
}
.ar-summary__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.ar-summary__count {
  font-weight: 800;
  font-size: 13px;
}
.ar-summary__label {
  color: var(--c-muted);
  font-weight: 600;
}

/* Reload button */
.ar-btn--reload {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  font-size: 13.5px;
  font-weight: 700;
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--c-border);
  background: var(--c-card);
  color: var(--c-text);
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
}
.ar-btn--reload:hover {
  border-color: #9ca3af;
  background: #f9fafb;
}
.ar-btn--reload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.ar-btn--loading {
  opacity: 0.7;
  pointer-events: none;
}

/* ── Card ────────────────────────── */
.ar-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* ── Toolbar ─────────────────────── */
.ar-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
  flex-wrap: wrap;
}

.ar-search {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 220px;
}
.ar-search svg {
  position: absolute;
  left: 11px;
  color: var(--c-subtle);
  pointer-events: none;
}
.ar-search__input {
  width: 100%;
  padding: 8px 32px 8px 34px;
  font-size: 13.5px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-card);
  color: var(--c-text);
  outline: none;
  transition: border-color 0.15s;
}
.ar-search__input:focus {
  border-color: var(--c-blue);
}
.ar-search__input::placeholder {
  color: var(--c-subtle);
}
.ar-search__clear {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--c-subtle);
  padding: 2px;
  display: flex;
  align-items: center;
}
.ar-search__clear:hover {
  color: var(--c-text);
}

.ar-filters {
  display: flex;
  gap: 8px;
}
.ar-select {
  padding: 8px 12px;
  font-size: 13px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-card);
  color: var(--c-text);
  cursor: pointer;
  outline: none;
  transition: border-color 0.15s;
}
.ar-select:focus {
  border-color: var(--c-blue);
}

.ar-count {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-left: auto;
  flex-shrink: 0;
}
.ar-count__num {
  font-size: 16px;
  font-weight: 800;
}
.ar-count__label {
  font-size: 12px;
  color: var(--c-subtle);
  font-weight: 600;
}

/* ── Table ───────────────────────── */
.ar-table-wrap {
  overflow-x: auto;
  position: relative;
  min-height: 200px;
}
.ar-table-wrap--loading {
  pointer-events: none;
}

.ar-loader-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.ar-loader-ring {
  width: 36px;
  height: 36px;
  border: 3px solid var(--c-border);
  border-top-color: var(--c-blue);
  border-radius: 50%;
  animation: ar-spin 0.7s linear infinite;
}

.ar-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.ar-table thead tr {
  border-bottom: 2px solid var(--c-border-light);
}
.ar-table th {
  padding: 11px 14px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--c-subtle);
  background: #fafbfc;
  white-space: nowrap;
  position: sticky;
  top: 0;
}
.ar-table td {
  padding: 13px 14px;
  vertical-align: middle;
  border-bottom: 1px solid var(--c-border-light);
}
.ar-row {
  transition: background 0.12s;
}
.ar-row:hover {
  background: #fafbfc;
}
.ar-row:last-child td {
  border-bottom: none;
}

/* Cell types */
.ar-id {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-blue);
  background: var(--c-blue-bg);
  padding: 2px 7px;
  border-radius: 5px;
}

.ar-order__num {
  display: block;
  font-weight: 700;
  font-size: 12.5px;
}
.ar-order__id {
  display: block;
  font-size: 11px;
  color: var(--c-subtle);
  font-family: "JetBrains Mono", monospace;
  margin-top: 2px;
}

.ar-product__name {
  display: block;
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 220px;
}
.ar-product__variant {
  display: inline-block;
  margin-top: 3px;
  font-size: 11px;
  font-weight: 600;
  color: var(--c-muted);
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  padding: 1px 7px;
  border-radius: 20px;
}

.ar-qty {
  font-family: "JetBrains Mono", monospace;
  font-size: 12.5px;
  color: var(--c-muted);
  font-weight: 500;
}
.ar-amount {
  font-weight: 700;
  font-size: 13px;
  white-space: nowrap;
}

/* Status tags */
.ar-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  white-space: nowrap;
  border: 1px solid transparent;
}
.ar-tag__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.ar-tag--green {
  background: var(--c-green-bg);
  color: var(--c-green);
  border-color: var(--c-green-border);
}
.ar-tag--green .ar-tag__dot {
  background: var(--c-green);
}
.ar-tag--red {
  background: var(--c-red-bg);
  color: var(--c-red);
  border-color: var(--c-red-border);
}
.ar-tag--red .ar-tag__dot {
  background: var(--c-red);
}
.ar-tag--orange {
  background: var(--c-orange-bg);
  color: var(--c-orange);
  border-color: var(--c-orange-border);
}
.ar-tag--orange .ar-tag__dot {
  background: var(--c-orange);
}
.ar-tag--gray {
  background: var(--c-gray-bg);
  color: var(--c-muted);
  border-color: var(--c-gray-border);
}
.ar-tag--gray .ar-tag__dot {
  background: var(--c-subtle);
}

.ar-text-cell {
  display: block;
  font-size: 13px;
  color: var(--c-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
.ar-text-cell--muted {
  color: var(--c-muted);
  font-style: italic;
}

.ar-date {
  font-size: 12.5px;
  color: var(--c-muted);
  white-space: nowrap;
}
.ar-date--muted {
  color: var(--c-subtle);
}

/* Empty state */
.ar-empty {
  text-align: center;
  padding: 64px 20px !important;
}
.ar-empty__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.ar-empty__icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--c-border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-subtle);
}
.ar-empty__inner p {
  color: var(--c-muted);
  font-size: 14px;
  margin: 0;
}
.ar-clear-filter {
  padding: 7px 16px;
  font-size: 13px;
  font-weight: 700;
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--c-border);
  background: var(--c-card);
  cursor: pointer;
  color: var(--c-text);
  font-family: "Plus Jakarta Sans", sans-serif;
  transition: all 0.15s;
}
.ar-clear-filter:hover {
  background: var(--c-border-light);
}

/* ── Footer ──────────────────────── */
.ar-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid var(--c-border-light);
  background: #fafbfc;
}
.ar-footer__total {
  font-size: 13px;
  color: var(--c-muted);
}
.ar-footer__total strong {
  color: var(--c-text);
  font-weight: 800;
}
.ar-footer__rows {
  font-size: 12px;
  color: var(--c-subtle);
  font-weight: 600;
}

/* ── Toast ───────────────────────── */
.ar-toast {
  position: fixed;
  bottom: 28px;
  right: 28px;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 13px 18px;
  border-radius: var(--radius-sm);
  font-size: 13.5px;
  font-weight: 600;
  font-family: "Plus Jakarta Sans", sans-serif;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  z-index: 9999;
}
.ar-toast--error {
  background: #450a0a;
  color: #fecaca;
}

/* ── Spinner ─────────────────────── */
.ar-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(0, 0, 0, 0.12);
  border-top-color: var(--c-muted);
  border-radius: 50%;
  animation: ar-spin 0.6s linear infinite;
  flex-shrink: 0;
}
@keyframes ar-spin {
  to {
    transform: rotate(360deg);
  }
}

/* ── Transitions ─────────────────── */
.ar-toast-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.ar-toast-slide-leave-active {
  transition: all 0.2s ease;
}
.ar-toast-slide-enter-from {
  transform: translateY(16px);
  opacity: 0;
}
.ar-toast-slide-leave-to {
  transform: translateY(8px);
  opacity: 0;
}
</style>
