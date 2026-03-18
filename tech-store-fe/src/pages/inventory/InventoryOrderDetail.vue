<template>
  <div class="page" v-if="order">
    <!-- BACK LINK -->
    <div class="back-link" @click="router.back()">
      <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
        <path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      Quay lại danh sách
    </div>

    <!-- PAGE HEADER -->
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-kicker">
          <span class="kicker-dot" :class="`dot-${order.status.toLowerCase()}`"></span>
          Chi tiết đơn hàng
        </div>
        <div class="title-row">
          <h1 class="page-title">#{{ order.orderNumber }}</h1>
          <span :class="['status-badge', `status-${order.status.toLowerCase()}`]">
            <span class="status-dot"></span>
            {{ statusLabel(order.status) }}
          </span>
        </div>
        <p class="page-desc">Kho hàng · Quản lý đơn hàng</p>
      </div>

      <!-- INVENTORY ACTIONS -->
      <div class="action-group">
        <el-button
          v-if="order.status === 'PAID'"
          class="btn-action btn-amber"
          :loading="loading"
          @click="markProcessing"
        >
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" style="margin-right:6px">
            <circle cx="7" cy="7" r="5.5" stroke="currentColor" stroke-width="1.4"/>
            <path d="M7 4v3l2 2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
          Bắt đầu xử lý
        </el-button>

        <el-button
          v-if="order.status === 'PROCESSING'"
          class="btn-action btn-blue"
          :loading="loading"
          @click="markShipping"
        >
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" style="margin-right:6px">
            <path d="M1 5h8v5H1zM9 7l3 1v2H9" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
            <circle cx="3.5" cy="10.5" r="1" stroke="currentColor" stroke-width="1.2"/>
            <circle cx="10.5" cy="10.5" r="1" stroke="currentColor" stroke-width="1.2"/>
          </svg>
          Chuyển giao vận
        </el-button>
      </div>
    </div>

    <!-- CONTENT GRID -->
    <div class="content-grid">
      <!-- LEFT COLUMN -->
      <div class="col-main">

        <!-- ORDER ITEMS TABLE -->
        <el-card shadow="never" class="section-card">
          <div class="section-header">
            <div class="section-title-group">
              <div class="section-icon section-icon-blue">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <rect x="1" y="1" width="12" height="12" rx="2" stroke="#2563eb" stroke-width="1.3"/>
                  <path d="M4 5h6M4 7.5h4M4 10h5" stroke="#2563eb" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <div class="section-title">Sản phẩm đặt mua</div>
                <div class="section-sub">{{ order.items?.length || 0 }} sản phẩm</div>
              </div>
            </div>
          </div>

          <el-table
            :data="order.items"
            style="width: 100%"
            class="items-table"
            :row-class-name="() => 'item-row'"
          >
            <el-table-column label="Sản phẩm" min-width="200">
              <template #default="{ row }">
                <div class="product-cell">
                  <div class="product-thumb">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <rect x="2" y="2" width="12" height="12" rx="3" fill="#e0e7ff"/>
                      <path d="M5 8h6M8 5v6" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <div class="product-info">
                    <span class="product-name">{{ row.productName }}</span>
                    <span class="product-sku" v-if="row.sku">SKU: {{ row.sku }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Đơn giá" width="140" align="right">
              <template #default="{ row }">
                <span class="price-cell">{{ formatVND(row.price) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="Số lượng" width="100" align="center">
              <template #default="{ row }">
                <span class="qty-badge">{{ row.quantity }}</span>
              </template>
            </el-table-column>

            <el-table-column label="Thành tiền" width="150" align="right">
              <template #default="{ row }">
                <span class="subtotal-cell">{{ formatVND(row.quantity * row.price) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- TOTAL -->
          <div class="total-row">
            <div class="total-inner">
              <div class="total-line">
                <span class="total-label">Tạm tính</span>
                <span class="total-value-light">{{ formatVND(order.totalAmount) }}</span>
              </div>
              <div class="total-line">
                <span class="total-label">Phí vận chuyển</span>
                <span class="total-value-light">Miễn phí</span>
              </div>
              <div class="total-divider"></div>
              <div class="total-line">
                <span class="total-label-bold">Tổng cộng</span>
                <span class="total-value-bold">{{ formatVND(order.totalAmount) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- RIGHT COLUMN -->
      <div class="col-side">

        <!-- ORDER INFO -->
        <el-card shadow="never" class="section-card">
          <div class="section-header">
            <div class="section-title-group">
              <div class="section-icon section-icon-gray">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <circle cx="7" cy="7" r="5.5" stroke="#64748b" stroke-width="1.3"/>
                  <path d="M7 4v3.5H9" stroke="#64748b" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <div class="section-title">Thông tin đơn hàng</div>
              </div>
            </div>
          </div>
          <div class="info-list">
            <div class="info-row">
              <span class="info-key">Mã đơn hàng</span>
              <span class="info-val">#{{ order.orderNumber }}</span>
            </div>
            <div class="info-row">
              <span class="info-key">Trạng thái</span>
              <span :class="['status-badge-sm', `status-${order.status.toLowerCase()}`]">
                {{ statusLabel(order.status) }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-key">Ngày tạo</span>
              <span class="info-val">{{ formatDate(order.createdAt) }}</span>
            </div>
          </div>
        </el-card>

        <!-- CUSTOMER INFO -->
        <el-card shadow="never" class="section-card">
          <div class="section-header">
            <div class="section-title-group">
              <div class="section-icon section-icon-purple">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <circle cx="7" cy="4.5" r="2.5" stroke="#7c3aed" stroke-width="1.3"/>
                  <path d="M2 12c0-2.76 2.24-5 5-5s5 2.24 5 5" stroke="#7c3aed" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <div class="section-title">Thông tin khách hàng</div>
              </div>
            </div>
          </div>
          <div class="customer-profile">
            <div class="customer-avatar-lg">{{ initials(order.customerName) }}</div>
            <div class="customer-details">
              <span class="customer-name-lg">{{ order.customerName }}</span>
            </div>
          </div>
          <div class="info-list">
            <div class="info-row">
              <span class="info-key">Số điện thoại</span>
              <span class="info-val">{{ order.customerPhone }}</span>
            </div>
            <div class="info-row">
              <span class="info-key">Địa chỉ</span>
              <span class="info-val info-val-wrap">{{ order.customerAddress }}</span>
            </div>
          </div>
        </el-card>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";

const route  = useRoute();
const router = useRouter();
const order  = ref(null);
const loading = ref(false);

const statusLabel = (s) => ({
  PAID: "Đã thanh toán",
  PROCESSING: "Đang xử lý",
  SHIPPING: "Đang giao",
  DONE: "Hoàn thành",
}[s] ?? s);

const load = async () => {
  const res = await ordersApi.getById(route.params.orderId);
  order.value = res.data;
};

const markProcessing = async () => {
  loading.value = true;
  try {
    await ordersApi.markAsProcessing(order.value.orderId);
    await load();
  } finally {
    loading.value = false;
  }
};

const markShipping = async () => {
  loading.value = true;
  try {
    await ordersApi.markAsShipping(order.value.orderId);
    await load();
  } finally {
    loading.value = false;
  }
};

const formatVND = (v) =>
  v ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(v) : "—";

const formatDate = (d) =>
  d ? new Date(d).toLocaleString("vi-VN") : "—";

const initials = (name) =>
  name ? name.split(" ").slice(-2).map(n => n[0]).join("").toUpperCase() : "?";

onMounted(load);
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }

/* BACK */
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.15s;
}

.back-link:hover { color: #2563eb; }

/* PAGE HEADER */
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.page-kicker {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.kicker-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
}

.dot-paid       { background: #16a34a; }
.dot-processing { background: #d97706; }
.dot-shipping   { background: #2563eb; }

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.5px;
}

.page-desc { font-size: 13.5px; color: #64748b; margin: 4px 0 0; }

/* STATUS BADGES */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 20px;
}

.status-badge-sm {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  font-weight: 600;
  padding: 3px 9px;
  border-radius: 20px;
}

.status-paid {
  background: #dcfce7;
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.status-processing {
  background: #fef3c7;
  color: #b45309;
  border: 1px solid #fde68a;
}

.status-shipping {
  background: #dbeafe;
  color: #1d4ed8;
  border: 1px solid #bfdbfe;
}

.status-dot {
  width: 5px; height: 5px;
  border-radius: 50%;
  background: currentColor;
}

/* ACTION BUTTONS */
.action-group { display: flex; gap: 8px; flex-wrap: wrap; }

.btn-action {
  font-size: 13px !important;
  font-weight: 600 !important;
  border-radius: 8px !important;
  padding: 0 16px !important;
  height: 38px !important;
  display: inline-flex;
  align-items: center;
}

.btn-amber {
  background: #f59e0b !important;
  border-color: #f59e0b !important;
  color: #fff !important;
}

.btn-amber:hover {
  background: #d97706 !important;
  border-color: #d97706 !important;
}

.btn-blue {
  background: #2563eb !important;
  border-color: #2563eb !important;
  color: #fff !important;
}

.btn-blue:hover {
  background: #1d4ed8 !important;
  border-color: #1d4ed8 !important;
}

/* GRID */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

@media (max-width: 900px) {
  .content-grid { grid-template-columns: 1fr; }
}

/* SECTION CARD */
.section-card {
  border-radius: 12px !important;
  border: 1px solid #e2e8f0 !important;
  margin-bottom: 16px;
}

.section-card :deep(.el-card__body) { padding: 0; }

.section-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.section-title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  width: 32px; height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.section-icon-blue   { background: #dbeafe; }
.section-icon-gray   { background: #f1f5f9; }
.section-icon-purple { background: #ede9fe; }

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.section-sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 1px;
}

/* ITEMS TABLE */
.items-table :deep(.el-table__header) th {
  background: #f8fafc !important;
  color: #64748b;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.items-table :deep(.item-row td) {
  border-bottom: 1px solid #f1f5f9;
  padding: 14px 12px;
}

.items-table :deep(.item-row:last-child td) {
  border-bottom: none;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-thumb {
  width: 36px; height: 36px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-name {
  font-size: 13.5px;
  font-weight: 600;
  color: #1e293b;
}

.product-sku {
  font-size: 11.5px;
  color: #94a3b8;
  font-family: 'Courier New', monospace;
}

.price-cell {
  font-size: 13px;
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

.qty-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.subtotal-cell {
  font-size: 13.5px;
  font-weight: 700;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

/* TOTAL */
.total-row {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
}

.total-inner {
  width: 260px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.total-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label { font-size: 13px; color: #64748b; }
.total-value-light { font-size: 13px; color: #374151; font-variant-numeric: tabular-nums; }

.total-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 4px 0;
}

.total-label-bold { font-size: 14px; font-weight: 700; color: #0f172a; }
.total-value-bold { font-size: 16px; font-weight: 700; color: #0f172a; font-variant-numeric: tabular-nums; }

/* INFO LIST */
.info-list {
  padding: 4px 20px 16px;
  display: flex;
  flex-direction: column;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid #f8fafc;
}

.info-row:last-child { border-bottom: none; }

.info-key {
  font-size: 12.5px;
  color: #94a3b8;
  font-weight: 500;
  flex-shrink: 0;
}

.info-val {
  font-size: 13px;
  color: #1e293b;
  font-weight: 600;
  text-align: right;
}

.info-val-wrap {
  text-align: right;
  word-break: break-word;
  max-width: 160px;
  line-height: 1.5;
}

/* CUSTOMER PROFILE */
.customer-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px 8px;
}

.customer-avatar-lg {
  width: 42px; height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  color: #7c3aed;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.customer-name-lg {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.col-side .section-card { margin-bottom: 16px; }
.col-side .section-card:last-child { margin-bottom: 0; }
</style>