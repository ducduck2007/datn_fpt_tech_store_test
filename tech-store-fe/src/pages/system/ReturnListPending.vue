<template>
  <div class="rr-page">
    <!-- Header -->
    <div class="rr-header">
      <div class="rr-header__left">
        <h1 class="rr-header__title">Yêu cầu hoàn hàng</h1>
        <p class="rr-header__sub">
          Quản lý các yêu cầu hoàn hàng &amp; hoàn tiền đang chờ xử lý
        </p>
      </div>
      <button
        class="rr-btn rr-btn--outline"
        :class="{ 'rr-btn--loading': loading }"
        @click="load"
      >
        <svg
          v-if="!loading"
          width="16"
          height="16"
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
        <span class="rr-spinner" v-else />
        Tải lại
      </button>
    </div>

    <!-- Stats Bar -->
    <div class="rr-stats">
      <div class="rr-stat">
        <span class="rr-stat__num">{{ returns.length }}</span>
        <span class="rr-stat__label">Yêu cầu chờ duyệt</span>
      </div>
      <div class="rr-stat-divider" />
      <div class="rr-stat">
        <span class="rr-stat__num">{{ formatMoney(totalRefund) }}</span>
        <span class="rr-stat__label">Tổng giá trị hoàn tiền</span>
      </div>
      <div class="rr-stat-divider" />
      <div class="rr-stat">
        <span class="rr-stat__num rr-stat__num--orange">RETURN_REQUESTED</span>
        <span class="rr-stat__label">Bộ lọc hiện tại</span>
      </div>
    </div>

    <!-- Table Card -->
    <div class="rr-card">
      <div class="rr-table-wrap" :class="{ 'rr-table-wrap--loading': loading }">
        <div class="rr-loader-overlay" v-if="loading">
          <div class="rr-loader-ring" />
        </div>

        <table class="rr-table">
          <thead>
            <tr>
              <th>Mã hoàn hàng</th>
              <th>Đơn hàng</th>
              <th>Sản phẩm</th>
              <th>SL</th>
              <th>Hoàn tiền</th>
              <th>Lý do</th>
              <th style="text-align: center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && returns.length === 0">
              <td colspan="7" class="rr-empty">
                <div class="rr-empty__inner">
                  <svg
                    width="40"
                    height="40"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                    opacity="0.3"
                  >
                    <path
                      d="M21 10V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l2-1.14"
                    />
                    <path d="M16.5 9.4 7.55 4.24" />
                    <polyline points="3.29 7 12 12 20.71 7" />
                    <line x1="12" x2="12" y1="22" y2="12" />
                    <circle cx="18.5" cy="15.5" r="2.5" />
                    <path d="M20.27 17.27 22 19" />
                  </svg>
                  <p>Không có yêu cầu hoàn hàng nào</p>
                </div>
              </td>
            </tr>
            <tr v-for="row in returns" :key="row.id" class="rr-row">
              <td>
                <span class="rr-id">#{{ row.id }}</span>
              </td>
              <td>
                <span class="rr-order-num">{{ row.orderNumber }}</span>
                <span class="rr-order-id">Mã: {{ row.orderId }}</span>
              </td>
              <td>
                <div class="rr-product">
                  <span class="rr-product__name">{{ row.productName }}</span>
                  <span class="rr-product__variant" v-if="row.variantName">{{
                    row.variantName
                  }}</span>
                </div>
              </td>
              <td>
                <span class="rr-qty">x {{ row.quantity }}</span>
              </td>
              <td>
                <span class="rr-amount">{{
                  formatMoney(row.refundAmount)
                }}</span>
              </td>
              <td>
                <span class="rr-reason" :title="row.reason">{{
                  row.reason
                }}</span>
              </td>
              <td>
                <div class="rr-actions">
                  <button
                    class="rr-action-btn rr-action-btn--approve"
                    :class="{ 'rr-action-btn--busy': actionId === row.id }"
                    :disabled="!!actionId"
                    @click="openApprove(row.id)"
                  >
                    <svg
                      width="14"
                      height="14"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                    >
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                    Duyệt
                  </button>
                  <button
                    class="rr-action-btn rr-action-btn--reject"
                    :class="{ 'rr-action-btn--busy': actionId === row.id }"
                    :disabled="!!actionId"
                    @click="openReject(row.id)"
                  >
                    <svg
                      width="14"
                      height="14"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                    >
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                    Từ chối
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════
         APPROVE DIALOG — Redesigned
    ════════════════════════════════════════════ -->
    <Teleport to="body">
      <Transition name="rr-dialog-fade">
        <div
          v-if="approveDialog"
          class="rr-overlay"
          @click.self="approveDialog = false"
        >
          <div class="rr-dialog rr-dialog--approve">
            <!-- Decorative header band -->
            <div class="rr-dialog__band rr-dialog__band--green">
              <div class="rr-dialog__band-circles">
                <span class="rr-band-circle rr-band-circle--1" />
                <span class="rr-band-circle rr-band-circle--2" />
                <span class="rr-band-circle rr-band-circle--3" />
              </div>
              <div class="rr-dialog__icon-wrap">
                <div class="rr-dialog__icon-ring">
                  <svg
                    width="28"
                    height="28"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                  >
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </div>
              </div>
            </div>

            <!-- Body -->
            <div class="rr-dialog__body">
              <div class="rr-dialog__badge rr-dialog__badge--green">
                Xác nhận hành động
              </div>
              <h2 class="rr-dialog__title">Duyệt yêu cầu hoàn hàng?</h2>
              <p class="rr-dialog__desc">
                Yêu cầu
                <strong class="rr-dialog__highlight rr-dialog__highlight--green"
                  >#{{ approveId }}</strong
                >
                sẽ được chấp thuận và hệ thống sẽ tự động tiến hành hoàn tiền
                cho khách hàng.
              </p>

              <div class="rr-dialog__notice rr-dialog__notice--green">
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.2"
                >
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                Hành động này không thể hoàn tác sau khi xác nhận.
              </div>

              <div class="rr-dialog__actions">
                <button
                  class="rr-btn rr-btn--ghost"
                  @click="approveDialog = false"
                >
                  Hủy bỏ
                </button>
                <button
                  class="rr-btn rr-btn--confirm-green"
                  :class="{ 'rr-btn--loading': actionId === approveId }"
                  @click="confirmApprove"
                >
                  <span
                    class="rr-spinner rr-spinner--white"
                    v-if="actionId === approveId"
                  />
                  <svg
                    v-else
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                  >
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                  Xác nhận duyệt
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ═══════════════════════════════════════════
         REJECT DIALOG — Redesigned
    ════════════════════════════════════════════ -->
    <Teleport to="body">
      <Transition name="rr-dialog-fade">
        <div
          v-if="rejectDialog"
          class="rr-overlay"
          @click.self="rejectDialog = false"
        >
          <div class="rr-dialog rr-dialog--reject">
            <!-- Decorative header band -->
            <div class="rr-dialog__band rr-dialog__band--red">
              <div class="rr-dialog__band-circles">
                <span class="rr-band-circle rr-band-circle--1" />
                <span class="rr-band-circle rr-band-circle--2" />
                <span class="rr-band-circle rr-band-circle--3" />
              </div>
              <div class="rr-dialog__icon-wrap">
                <div class="rr-dialog__icon-ring">
                  <svg
                    width="28"
                    height="28"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                  >
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </div>
              </div>
            </div>

            <!-- Body -->
            <div class="rr-dialog__body">
              <div class="rr-dialog__badge rr-dialog__badge--red">
                Từ chối yêu cầu
              </div>
              <h2 class="rr-dialog__title">Từ chối hoàn hàng?</h2>
              <p class="rr-dialog__desc">
                Nhập lý do từ chối yêu cầu
                <strong class="rr-dialog__highlight rr-dialog__highlight--red"
                  >#{{ rejectId }}</strong
                >. Khách hàng sẽ nhận được thông báo kèm theo lý do này.
              </p>

              <div class="rr-dialog__field">
                <label class="rr-dialog__label">
                  Lý do từ chối
                  <span class="rr-required">*</span>
                </label>
                <textarea
                  v-model="rejectReason"
                  class="rr-textarea"
                  :class="{
                    'rr-textarea--error': rejectSubmitted && !rejectReason,
                  }"
                  rows="4"
                  placeholder="VD: Sản phẩm có dấu hiệu đã qua sử dụng, đã hết thời hạn đổi trả..."
                />
                <Transition name="rr-err-fade">
                  <span
                    v-if="rejectSubmitted && !rejectReason"
                    class="rr-field-error"
                  >
                    <svg
                      width="12"
                      height="12"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2.5"
                    >
                      <circle cx="12" cy="12" r="10" />
                      <line x1="12" y1="8" x2="12" y2="12" />
                      <line x1="12" y1="16" x2="12.01" y2="16" />
                    </svg>
                    Vui lòng nhập lý do từ chối.
                  </span>
                </Transition>
              </div>

              <div class="rr-dialog__actions">
                <button
                  class="rr-btn rr-btn--ghost"
                  @click="rejectDialog = false"
                >
                  Hủy bỏ
                </button>
                <button
                  class="rr-btn rr-btn--confirm-red"
                  :class="{ 'rr-btn--loading': actionId === rejectId }"
                  @click="confirmReject"
                >
                  <span
                    class="rr-spinner rr-spinner--white"
                    v-if="actionId === rejectId"
                  />
                  <svg
                    v-else
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                  >
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                  Xác nhận từ chối
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Toast -->
    <Teleport to="body">
      <Transition name="rr-toast-slide">
        <div v-if="toastMsg" class="rr-toast" :class="`rr-toast--${toastType}`">
          <svg
            v-if="toastType === 'success'"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <polyline points="20 6 9 17 4 12" />
          </svg>
          <svg
            v-else-if="toastType === 'error'"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <svg
            v-else
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path
              d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"
            />
            <line x1="12" y1="9" x2="12" y2="13" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
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

const STATUS = "PENDING";

const returns = ref([]);
const loading = ref(false);
const actionId = ref(null);

const approveDialog = ref(false);
const approveId = ref(null);

const rejectDialog = ref(false);
const rejectReason = ref("");
const rejectId = ref(null);
const rejectSubmitted = ref(false);

const toastMsg = ref("");
const toastType = ref("success");
let toastTimer = null;

const totalRefund = computed(() =>
  returns.value.reduce((s, r) => s + (r.refundAmount || 0), 0),
);

onMounted(load);

async function load() {
  loading.value = true;
  try {
    const res = await returnsApi.getByStatus(STATUS);
    returns.value = res?.data?.data || res?.data || [];
  } catch (e) {
    showToast(
      e?.response?.data?.message || "Không thể tải danh sách yêu cầu hoàn hàng",
      "error",
    );
  } finally {
    loading.value = false;
  }
}

function openApprove(id) {
  approveId.value = id;
  approveDialog.value = true;
}

async function confirmApprove() {
  actionId.value = approveId.value;
  try {
    await returnsApi.approve(approveId.value);
    showToast("Đã duyệt yêu cầu hoàn hàng thành công", "success");
    approveDialog.value = false;
    load();
  } catch (e) {
    showToast(e?.response?.data?.message || "Duyệt yêu cầu thất bại", "error");
  } finally {
    actionId.value = null;
  }
}

function openReject(id) {
  rejectId.value = id;
  rejectReason.value = "";
  rejectSubmitted.value = false;
  rejectDialog.value = true;
}

async function confirmReject() {
  rejectSubmitted.value = true;
  if (!rejectReason.value.trim()) return;
  actionId.value = rejectId.value;
  try {
    await returnsApi.reject(rejectId.value, rejectReason.value);
    showToast("Đã từ chối yêu cầu hoàn hàng", "warning");
    rejectDialog.value = false;
    load();
  } catch (e) {
    showToast(
      e?.response?.data?.message || "Từ chối yêu cầu thất bại",
      "error",
    );
  } finally {
    actionId.value = null;
  }
}

function formatMoney(val) {
  if (val == null) return "—";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(val);
}

function showToast(msg, type = "success") {
  clearTimeout(toastTimer);
  toastMsg.value = msg;
  toastType.value = type;
  toastTimer = setTimeout(() => (toastMsg.value = ""), 3500);
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

.rr-page {
  --c-bg: #f6f7f9;
  --c-card: #ffffff;
  --c-border: #e4e7ec;
  --c-border-light: #f0f2f5;
  --c-text: #0f1117;
  --c-muted: #6b7280;
  --c-subtle: #9ca3af;
  --c-green: #16a34a;
  --c-green-bg: #f0fdf4;
  --c-green-border: #bbf7d0;
  --c-green-hover: #15803d;
  --c-green-dark: #052e16;
  --c-red: #dc2626;
  --c-red-bg: #fff1f2;
  --c-red-border: #fecdd3;
  --c-red-hover: #b91c1c;
  --c-red-dark: #450a0a;
  --c-blue: #2563eb;
  --c-blue-bg: #eff6ff;
  --c-orange: #ea580c;
  --radius: 12px;
  --radius-sm: 8px;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08), 0 2px 6px rgba(0, 0, 0, 0.04);
  --shadow-lg: 0 24px 64px rgba(0, 0, 0, 0.14), 0 8px 24px rgba(0, 0, 0, 0.06);

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--c-bg);
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: var(--c-text);
  box-sizing: border-box;
}

/* ── Header ──────────────────────── */
.rr-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
}
.rr-header__eyebrow {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-muted);
  margin-bottom: 6px;
}
.rr-status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f59e0b;
  box-shadow: 0 0 0 2px #fef3c7;
  animation: rr-pulse 2s infinite;
}
@keyframes rr-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 2px #fef3c7;
  }
  50% {
    box-shadow: 0 0 0 5px #fde68a;
  }
}
.rr-header__title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
}
.rr-header__sub {
  font-size: 13.5px;
  color: var(--c-muted);
  margin: 0;
}

/* ── Stats ───────────────────────── */
.rr-stats {
  display: flex;
  align-items: center;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 16px 28px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}
.rr-stat {
  padding: 4px 24px;
}
.rr-stat:first-child {
  padding-left: 0;
}
.rr-stat__num {
  display: block;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.1;
}
.rr-stat__num--orange {
  font-size: 13px;
  color: var(--c-orange);
  font-family: "JetBrains Mono", monospace;
}
.rr-stat__label {
  display: block;
  font-size: 11px;
  color: var(--c-subtle);
  font-weight: 600;
  margin-top: 3px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.rr-stat-divider {
  width: 1px;
  height: 36px;
  background: var(--c-border);
  flex-shrink: 0;
}

/* ── Table ───────────────────────── */
.rr-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.rr-table-wrap {
  overflow-x: auto;
  position: relative;
}
.rr-table-wrap--loading {
  pointer-events: none;
}
.rr-loader-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  backdrop-filter: blur(2px);
}
.rr-loader-ring {
  width: 36px;
  height: 36px;
  border: 3px solid var(--c-border);
  border-top-color: var(--c-blue);
  border-radius: 50%;
  animation: rr-spin 0.7s linear infinite;
}
.rr-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13.5px;
}
.rr-table thead tr {
  border-bottom: 2px solid var(--c-border-light);
}
.rr-table th {
  padding: 13px 16px;
  text-align: left;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--c-subtle);
  background: #fafbfc;
  white-space: nowrap;
}
.rr-table td {
  padding: 14px 16px;
  vertical-align: middle;
  border-bottom: 1px solid var(--c-border-light);
}
.rr-row {
  transition: background 0.15s;
}
.rr-row:hover {
  background: #fafbfc;
}
.rr-row:last-child td {
  border-bottom: none;
}
.rr-id {
  font-family: "JetBrains Mono", monospace;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--c-blue);
  background: var(--c-blue-bg);
  padding: 3px 8px;
  border-radius: 6px;
}
.rr-order-num {
  display: block;
  font-weight: 700;
  font-size: 13px;
}
.rr-order-id {
  display: block;
  font-size: 11.5px;
  color: var(--c-subtle);
  font-family: "JetBrains Mono", monospace;
  margin-top: 2px;
}
.rr-product__name {
  display: block;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
.rr-product__variant {
  display: inline-block;
  margin-top: 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--c-muted);
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  padding: 2px 7px;
  border-radius: 20px;
}
.rr-qty {
  font-family: "JetBrains Mono", monospace;
  font-weight: 600;
  font-size: 13px;
  color: var(--c-muted);
}
.rr-amount {
  font-weight: 700;
  white-space: nowrap;
}
.rr-reason {
  font-size: 13px;
  color: var(--c-muted);
  max-width: 180px;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rr-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}
.rr-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 13px;
  font-size: 12.5px;
  font-weight: 700;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
  white-space: nowrap;
}
.rr-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.rr-action-btn--approve {
  background: var(--c-green-bg);
  color: var(--c-green);
  border: 1.5px solid var(--c-green-border);
}
.rr-action-btn--approve:not(:disabled):hover {
  background: var(--c-green);
  color: #fff;
  border-color: var(--c-green);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.25);
}
.rr-action-btn--reject {
  background: var(--c-red-bg);
  color: var(--c-red);
  border: 1.5px solid var(--c-red-border);
}
.rr-action-btn--reject:not(:disabled):hover {
  background: var(--c-red);
  color: #fff;
  border-color: var(--c-red);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.25);
}
.rr-empty {
  text-align: center;
  padding: 60px 20px !important;
}
.rr-empty__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.rr-empty__inner p {
  color: var(--c-subtle);
  font-size: 14px;
  margin: 0;
}

/* ── Global Buttons ──────────────── */
.rr-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 20px;
  font-size: 13.5px;
  font-weight: 700;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
}
.rr-btn--outline {
  background: var(--c-card);
  border: 1.5px solid var(--c-border);
  color: var(--c-text);
}
.rr-btn--outline:hover {
  border-color: #9ca3af;
  background: #f9fafb;
}
.rr-btn--ghost {
  background: transparent;
  color: var(--c-muted);
  border: 1.5px solid var(--c-border);
  padding: 10px 20px;
  border-radius: var(--radius-sm);
}
.rr-btn--ghost:hover {
  background: var(--c-border-light);
  color: var(--c-text);
}
.rr-btn--loading {
  opacity: 0.8;
  pointer-events: none;
}

/* ── REDESIGNED DIALOG ───────────── */
.rr-overlay {
  position: fixed;
  inset: 0;
  background: rgba(10, 12, 20, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.rr-dialog {
  background: #ffffff;
  border-radius: 20px;
  width: 100%;
  max-width: 440px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  position: relative;
}

/* — Band (top colored strip) — */
.rr-dialog__band {
  position: relative;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.rr-dialog__band--green {
  background: linear-gradient(135deg, #16a34a 0%, #22c55e 60%, #4ade80 100%);
}
.rr-dialog__band--red {
  background: linear-gradient(135deg, #b91c1c 0%, #dc2626 60%, #f87171 100%);
}

/* Decorative circles inside the band */
.rr-dialog__band-circles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.rr-band-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}
.rr-band-circle--1 {
  width: 140px;
  height: 140px;
  top: -40px;
  right: -30px;
}
.rr-band-circle--2 {
  width: 80px;
  height: 80px;
  bottom: -20px;
  left: 20px;
}
.rr-band-circle--3 {
  width: 50px;
  height: 50px;
  top: 10px;
  left: 50%;
  margin-left: -25px;
  background: rgba(255, 255, 255, 0.07);
}

/* Icon ring inside band */
.rr-dialog__icon-wrap {
  position: relative;
  z-index: 1;
}
.rr-dialog__icon-ring {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  backdrop-filter: blur(4px);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* — Dialog body — */
.rr-dialog__body {
  padding: 28px 32px 28px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rr-dialog__badge {
  display: inline-flex;
  align-self: flex-start;
  font-size: 10.5px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  padding: 4px 10px;
  border-radius: 20px;
}
.rr-dialog__badge--green {
  background: var(--c-green-bg);
  color: var(--c-green);
  border: 1px solid var(--c-green-border);
}
.rr-dialog__badge--red {
  background: var(--c-red-bg);
  color: var(--c-red);
  border: 1px solid var(--c-red-border);
}

.rr-dialog__title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0;
  line-height: 1.2;
}

.rr-dialog__desc {
  font-size: 14px;
  color: var(--c-muted);
  line-height: 1.65;
  margin: 0;
}

.rr-dialog__highlight {
  font-weight: 700;
  font-family: "JetBrains Mono", monospace;
  font-size: 13px;
  padding: 1px 6px;
  border-radius: 5px;
}
.rr-dialog__highlight--green {
  color: var(--c-green);
  background: var(--c-green-bg);
}
.rr-dialog__highlight--red {
  color: var(--c-red);
  background: var(--c-red-bg);
}

/* Notice bar */
.rr-dialog__notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 12.5px;
  font-weight: 600;
  line-height: 1.4;
}
.rr-dialog__notice--green {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #bbf7d0;
}
.rr-dialog__notice--red {
  background: #fff1f2;
  color: #b91c1c;
  border: 1px solid #fecdd3;
}

/* Textarea field */
.rr-dialog__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.rr-dialog__label {
  font-size: 11.5px;
  font-weight: 700;
  color: var(--c-text);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.rr-required {
  color: var(--c-red);
  margin-left: 2px;
}
.rr-textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 13px;
  font-size: 13.5px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: 1.5px solid var(--c-border);
  border-radius: 10px;
  resize: vertical;
  color: var(--c-text);
  outline: none;
  transition:
    border-color 0.15s,
    box-shadow 0.15s;
  background: #fafbfc;
  line-height: 1.5;
}
.rr-textarea:focus {
  border-color: var(--c-red);
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.08);
  background: #fff;
}
.rr-textarea--error {
  border-color: var(--c-red);
  background: var(--c-red-bg);
}
.rr-field-error {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--c-red);
  font-weight: 600;
}

/* Actions row */
.rr-dialog__actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 4px;
}

/* Confirm buttons */
.rr-btn--confirm-green,
.rr-btn--confirm-red {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 11px 22px;
  font-size: 13.5px;
  font-weight: 700;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
  letter-spacing: -0.01em;
}
.rr-btn--confirm-green {
  background: linear-gradient(135deg, #16a34a, #22c55e);
  color: #fff;
  box-shadow: 0 4px 14px rgba(22, 163, 74, 0.35);
}
.rr-btn--confirm-green:hover {
  background: linear-gradient(135deg, #15803d, #16a34a);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(22, 163, 74, 0.4);
}
.rr-btn--confirm-red {
  background: linear-gradient(135deg, #b91c1c, #dc2626);
  color: #fff;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.35);
}
.rr-btn--confirm-red:hover {
  background: linear-gradient(135deg, #991b1b, #b91c1c);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.4);
}
.rr-btn--confirm-green.rr-btn--loading,
.rr-btn--confirm-red.rr-btn--loading {
  opacity: 0.75;
  pointer-events: none;
  transform: none;
}

/* ── Spinner ─────────────────────── */
.rr-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(0, 0, 0, 0.12);
  border-top-color: var(--c-muted);
  border-radius: 50%;
  animation: rr-spin 0.6s linear infinite;
  flex-shrink: 0;
}
.rr-spinner--white {
  border-color: rgba(255, 255, 255, 0.25);
  border-top-color: #fff;
}
@keyframes rr-spin {
  to {
    transform: rotate(360deg);
  }
}

/* ── Toast ───────────────────────── */
.rr-toast {
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
  box-shadow: var(--shadow-md);
  z-index: 9999;
  max-width: 360px;
}
.rr-toast--success {
  background: #052e16;
  color: #bbf7d0;
}
.rr-toast--error {
  background: #450a0a;
  color: #fecaca;
}
.rr-toast--warning {
  background: #431407;
  color: #fed7aa;
}

/* ── Transitions ─────────────────── */
.rr-dialog-fade-enter-active,
.rr-dialog-fade-leave-active {
  transition: opacity 0.22s ease;
}
.rr-dialog-fade-enter-active .rr-dialog,
.rr-dialog-fade-leave-active .rr-dialog {
  transition:
    transform 0.28s cubic-bezier(0.34, 1.4, 0.64, 1),
    opacity 0.22s ease;
}
.rr-dialog-fade-enter-from,
.rr-dialog-fade-leave-to {
  opacity: 0;
}
.rr-dialog-fade-enter-from .rr-dialog {
  transform: scale(0.9) translateY(24px);
  opacity: 0;
}
.rr-dialog-fade-leave-to .rr-dialog {
  transform: scale(0.96) translateY(8px);
  opacity: 0;
}

.rr-toast-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.rr-toast-slide-leave-active {
  transition: all 0.2s ease;
}
.rr-toast-slide-enter-from {
  transform: translateY(16px);
  opacity: 0;
}
.rr-toast-slide-leave-to {
  transform: translateY(8px);
  opacity: 0;
}

.rr-err-fade-enter-active {
  transition: all 0.2s ease;
}
.rr-err-fade-enter-from {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
