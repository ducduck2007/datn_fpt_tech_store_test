<template>
  <div class="page">
    <!-- PAGE HEADER -->
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-kicker">
          <span class="kicker-dot"></span>
          Inventory
        </div>
        <h1 class="page-title">Đơn đã thanh toán</h1>
        <p class="page-desc">Các đơn hàng chờ kho xử lý đóng gói</p>
      </div>
      <div class="page-header-right">
        <el-button class="btn-outline" @click="loadOrders">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" style="margin-right:6px">
            <path d="M12 7A5 5 0 1 1 7 2a5 5 0 0 1 3.54 1.46M12 2v3.5H8.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Làm mới
        </el-button>
      </div>
    </div>

    <!-- STATS ROW -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-green">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M3 9l4 4 8-8" stroke="#16a34a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ orders.length }}</span>
          <span class="stat-label">Đơn chờ xử lý</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-blue">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <rect x="2" y="3" width="14" height="12" rx="2" stroke="#2563eb" stroke-width="1.5"/>
            <path d="M2 7h14" stroke="#2563eb" stroke-width="1.5"/>
            <path d="M6 11h6" stroke="#2563eb" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ formatVND(totalAmount) }}</span>
          <span class="stat-label">Tổng giá trị</span>
        </div>
      </div>
    </div>

    <!-- TABLE CARD -->
    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <el-input
          v-model="search"
          placeholder="Tìm mã đơn hàng..."
          clearable
          class="search-input"
        >
          <template #prefix>
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <circle cx="6" cy="6" r="4" stroke="#9ca3af" stroke-width="1.5"/>
              <path d="M10 10l2 2" stroke="#9ca3af" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </template>
        </el-input>
        <el-tag type="warning" class="count-tag" effect="light">
          {{ filteredOrders.length }} đơn hàng
        </el-tag>
      </div>

      <el-table
        :data="filteredOrders"
        style="width: 100%"
        class="orders-table"
        :row-class-name="() => 'table-row'"
        empty-text="Không có đơn hàng nào"
      >
        <!-- Order number -->
        <el-table-column label="Mã đơn hàng" min-width="160">
          <template #default="{ row }">
            <div class="order-num-cell">
              <span class="order-hash">#</span>
              <span class="order-num">{{ row.orderNumber }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- Customer -->
        <el-table-column prop="customerName" label="Khách hàng" min-width="160">
          <template #default="{ row }">
            <div class="customer-cell">
              <div class="customer-avatar">{{ initials(row.customerName) }}</div>
              <span>{{ row.customerName }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- Amount -->
        <el-table-column label="Giá trị" min-width="130" align="right">
          <template #default="{ row }">
            <span class="amount-cell">{{ formatVND(row.totalAmount) }}</span>
          </template>
        </el-table-column>

        <!-- Status -->
        <el-table-column label="Trạng thái" width="140" align="center">
          <template #default>
            <span class="status-badge status-paid">
              <span class="status-dot"></span>
              Đã thanh toán
            </span>
          </template>
        </el-table-column>

        <!-- Date -->
        <el-table-column prop="createdAt" label="Ngày tạo" width="140">
          <template #default="{ row }">
            <span class="date-cell">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>

        <!-- Actions -->
        <el-table-column label="Thao tác" width="200" align="center">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button
                size="small"
                class="btn-processing"
                @click="markProcessing(row.orderId)"
                :loading="loadingId === row.orderId"
              >
                Xử lý ngay
              </el-button>
              <el-button
                size="small"
                class="btn-ghost"
                @click="goDetail(row.orderId)"
              >
                Chi tiết
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";

const router = useRouter();
const orders = ref([]);
const search = ref("");
const loadingId = ref(null);

const filteredOrders = computed(() =>
  search.value
    ? orders.value.filter(o =>
        o.orderNumber?.toLowerCase().includes(search.value.toLowerCase())
      )
    : orders.value
);

const totalAmount = computed(() =>
  orders.value.reduce((sum, o) => sum + (o.totalAmount || 0), 0)
);

const loadOrders = async () => {
  const res = await ordersApi.listPaid();
  orders.value = res.data;
};

const markProcessing = async (orderId) => {
  loadingId.value = orderId;
  try {
    await ordersApi.markAsProcessing(orderId);
    await loadOrders();
  } finally {
    loadingId.value = null;
  }
};

const goDetail = (orderId) => router.push(`/inventory/orders/${orderId}`);

const formatVND = (v) =>
  v ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(v) : "—";

const formatDate = (d) =>
  d ? new Date(d).toLocaleDateString("vi-VN") : "—";

const initials = (name) =>
  name ? name.split(" ").slice(-2).map(n => n[0]).join("").toUpperCase() : "?";

onMounted(loadOrders);
</script>

<style scoped>
/* PAGE */
.page { display: flex; flex-direction: column; gap: 20px; }

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
  color: #2563eb;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.kicker-dot {
  width: 6px; height: 6px;
  background: #2563eb;
  border-radius: 50%;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px;
  letter-spacing: -0.5px;
}

.page-desc {
  font-size: 13.5px;
  color: #64748b;
  margin: 0;
}

/* STATS */
.stats-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 200px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.stat-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-green { background: #dcfce7; }
.stat-icon-blue  { background: #dbeafe; }

.stat-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.1;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

/* TABLE CARD */
.table-card {
  border-radius: 12px !important;
  border: 1px solid #e2e8f0 !important;
}

.table-card :deep(.el-card__body) { padding: 0; }

.table-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.search-input { width: 240px; }

.count-tag {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  border-radius: 6px;
}

/* TABLE */
.orders-table :deep(.el-table__header) th {
  background: #f8fafc !important;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  border-bottom: 1px solid #e2e8f0;
}

.orders-table :deep(.table-row td) {
  border-bottom: 1px solid #f1f5f9;
  padding: 14px 12px;
}

.orders-table :deep(.table-row:hover td) {
  background: #f8fafc !important;
}

/* CELLS */
.order-num-cell {
  display: flex;
  align-items: center;
  gap: 2px;
  font-weight: 600;
  color: #1e293b;
}

.order-hash { color: #94a3b8; font-size: 13px; }
.order-num { font-size: 14px; }

.customer-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: #374151;
}

.customer-avatar {
  width: 28px; height: 28px;
  border-radius: 6px;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  color: #4f46e5;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.amount-cell {
  font-size: 13.5px;
  font-weight: 600;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.date-cell {
  font-size: 12.5px;
  color: #64748b;
}

/* STATUS BADGE */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.status-paid {
  background: #dcfce7;
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.status-dot {
  width: 5px; height: 5px;
  border-radius: 50%;
  background: currentColor;
}

/* ACTIONS */
.action-cell {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.btn-processing {
  background: #f59e0b !important;
  border-color: #f59e0b !important;
  color: #fff !important;
  font-weight: 600 !important;
  font-size: 12px !important;
  border-radius: 6px !important;
  padding: 0 12px !important;
}

.btn-processing:hover {
  background: #d97706 !important;
  border-color: #d97706 !important;
}

.btn-ghost {
  background: #f1f5f9 !important;
  border-color: #e2e8f0 !important;
  color: #475569 !important;
  font-weight: 500 !important;
  font-size: 12px !important;
  border-radius: 6px !important;
}

.btn-ghost:hover {
  background: #e2e8f0 !important;
}

.btn-outline {
  border-color: #e2e8f0 !important;
  color: #374151 !important;
  font-size: 13px !important;
  border-radius: 8px !important;
}
</style>