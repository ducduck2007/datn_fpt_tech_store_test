<template>
  <div class="container-xl">
    <el-card shadow="never" class="orders-card">

      <!-- ── Header ── -->
      <div class="page-header">
        <div>
          <div class="kicker">Khách hàng</div>
          <div class="title">Đơn hàng của tôi</div>
        </div>
        <el-button :loading="loading" @click="loadOrders">
          <el-icon><Refresh /></el-icon> Tải lại
        </el-button>
      </div>

      <el-divider />

      <!-- ── Error ── -->
      <el-alert
        v-if="error"
        type="error"
        :title="error"
        show-icon
        class="mb-3"
      />

      <!-- ── Stats row ── -->
      <div class="stats-row mb-4">
        <div class="stat-pill stat-total">
          <span class="stat-val">{{ orders.length }}</span>
          <span class="stat-key">Tổng đơn</span>
        </div>
        <div class="stat-pill stat-pending">
          <span class="stat-val">{{ countByStatus('PENDING') }}</span>
          <span class="stat-key">Chờ xử lý</span>
        </div>
        <div class="stat-pill stat-processing">
          <span class="stat-val">{{ countByStatus('CONFIRMED') + countByStatus('PROCESSING') }}</span>
          <span class="stat-key">Đang xử lý</span>
        </div>
        <div class="stat-pill stat-done">
          <span class="stat-val">{{ countByStatus('DELIVERED') }}</span>
          <span class="stat-key">Đã giao</span>
        </div>
        <div class="stat-pill stat-cancelled">
          <span class="stat-val">{{ countByStatus('CANCELLED') }}</span>
          <span class="stat-key">Đã hủy</span>
        </div>
      </div>

      <!-- ── Table ── -->
      <el-skeleton v-if="loading" :rows="6" animated />

      <el-empty
        v-else-if="!orders.length"
        description="Bạn chưa có đơn hàng nào"
        :image-size="80"
      />

      <el-table
        v-else
        :data="orders"
        border
        stripe
        style="width: 100%"
        :header-cell-style="headerCellStyle"
      >
        <!-- Order number -->
        <el-table-column label="Mã đơn" min-width="170">
          <template #default="{ row }">
            <div class="order-num">{{ row.orderNumber }}</div>
          </template>
        </el-table-column>

        <!-- Created at -->
        <el-table-column label="Ngày đặt" width="170">
          <template #default="{ row }">
            <span class="cell-date">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>

        <!-- Payment -->
        <el-table-column label="Thanh toán" width="130">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">
              {{ row.paymentMethod || '—' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- Channel -->
        <el-table-column label="Kênh" width="110">
          <template #default="{ row }">
            <span class="cell-muted">{{ row.channel || '—' }}</span>
          </template>
        </el-table-column>

        <!-- Total -->
        <el-table-column label="Tổng tiền" width="160" align="right">
          <template #default="{ row }">
            <span class="cell-amount">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>

        <!-- Status -->
        <el-table-column label="Trạng thái" width="140">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="dark" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- Actions -->
        <el-table-column label="Thao tác" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button
                size="small"
                type="primary"
                plain
                @click="$router.push(`/orders/${row.orderId}`)"
              >
                <el-icon><View /></el-icon> Chi tiết
              </el-button>
              <el-button
                v-if="canCancel(row.status)"
                size="small"
                type="danger"
                plain
                :loading="cancelingId === row.orderId"
                @click="onCancel(row.orderId)"
              >
                <el-icon v-if="cancelingId !== row.orderId"><Close /></el-icon>
                Hủy
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { Refresh, View, Close } from "@element-plus/icons-vue";
import { ordersApi } from "../../api/orders.api";
import { ElMessageBox, ElMessage } from "element-plus";

const orders     = ref([]);
const loading    = ref(false);
const error      = ref("");
const cancelingId = ref(null);

onMounted(loadOrders);

// ── Load ──────────────────────────────────────────────────────────────────────
async function loadOrders() {
  loading.value = true;
  error.value   = "";
  try {
    const res  = await ordersApi.getMyOrders();
    orders.value = res?.data || [];
  } catch (e) {
    error.value = e?.response?.data?.message || "Không tải được đơn hàng";
  } finally {
    loading.value = false;
  }
}

// ── Cancel ────────────────────────────────────────────────────────────────────
async function onCancel(orderId) {
  try {
    await ElMessageBox.confirm(
      "Bạn có chắc muốn hủy đơn hàng này không?",
      "Xác nhận hủy đơn",
      { type: "warning", confirmButtonText: "Hủy đơn", cancelButtonText: "Giữ lại" }
    );
    cancelingId.value = orderId;
    await ordersApi.cancel(orderId);
    ElMessage.success("Đã hủy đơn hàng thành công");
    await loadOrders();
  } catch (e) {
    if (e?.response?.data?.message) ElMessage.error(e.response.data.message);
  } finally {
    cancelingId.value = null;
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
const STATUS_LABELS = {
  PENDING:    "Chờ xử lý",
  CONFIRMED:  "Đã xác nhận",
  PROCESSING: "Đang xử lý",
  SHIPPING:   "Đang giao",
  DELIVERED:  "Đã giao",
  CANCELLED:  "Đã hủy",
  PAID:       "Đã thanh toán",
};

const STATUS_TYPES = {
  PENDING:    "warning",
  CONFIRMED:  "primary",
  PROCESSING: "primary",
  SHIPPING:   "primary",
  DELIVERED:  "success",
  CANCELLED:  "danger",
  PAID:       "success",
};

function statusLabel(status) { return STATUS_LABELS[status] || status; }
function statusType(status)  { return STATUS_TYPES[status]  || "info";  }
function canCancel(status)   { return ["PENDING", "CONFIRMED"].includes(status); }
function countByStatus(s)    { return orders.value.filter(o => o.status === s).length; }

function formatDate(date) {
  if (!date) return "—";
  return new Date(date).toLocaleString("vi-VN", {
    year: "numeric", month: "2-digit", day: "2-digit",
    hour: "2-digit", minute: "2-digit",
  });
}
function formatMoney(value) {
  if (value == null) return "—";
  return Number(value).toLocaleString("vi-VN") + " ₫";
}

const headerCellStyle = {
  background: "#F8FAFC",
  color: "#64748B",
  fontSize: "11px",
  fontWeight: "700",
  textTransform: "uppercase",
  letterSpacing: "0.05em",
  fontFamily: "Inter, sans-serif",
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap");

/* ── Container ──────────────────────────────────────────────────────────────── */
.container-xl {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
  font-family: "Inter", sans-serif;
}

.orders-card { border-radius: 16px; }

/* ── Page header ────────────────────────────────────────────────────────────── */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.kicker {
  font-size: 11px;
  font-weight: 800;
  color: #94A3B8;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 4px;
}
.title {
  font-size: 22px;
  font-weight: 900;
  color: #0F172A;
  line-height: 1.2;
}

/* ── Stats row ──────────────────────────────────────────────────────────────── */
.stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.stat-pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 18px;
  border-radius: 12px;
  min-width: 90px;
  border: 1px solid transparent;
  transition: transform 0.2s;
}
.stat-pill:hover { transform: translateY(-2px); }
.stat-val {
  font-size: 22px;
  font-weight: 900;
  line-height: 1.1;
}
.stat-key {
  font-size: 11px;
  font-weight: 600;
  margin-top: 3px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.stat-total     { background: linear-gradient(135deg, #EFF6FF, #DBEAFE); border-color: #BFDBFE; }
.stat-total     .stat-val { color: #2563EB; }
.stat-total     .stat-key { color: #64748B; }

.stat-pending   { background: linear-gradient(135deg, #FFFBEB, #FEF3C7); border-color: #FDE68A; }
.stat-pending   .stat-val { color: #D97706; }
.stat-pending   .stat-key { color: #92400E; }

.stat-processing { background: linear-gradient(135deg, #F5F3FF, #EDE9FE); border-color: #DDD6FE; }
.stat-processing .stat-val { color: #7C3AED; }
.stat-processing .stat-key { color: #5B21B6; }

.stat-done      { background: linear-gradient(135deg, #F0FDF4, #DCFCE7); border-color: #BBF7D0; }
.stat-done      .stat-val { color: #16A34A; }
.stat-done      .stat-key { color: #14532D; }

.stat-cancelled { background: linear-gradient(135deg, #FFF1F2, #FFE4E6); border-color: #FECDD3; }
.stat-cancelled .stat-val { color: #DC2626; }
.stat-cancelled .stat-key { color: #991B1B; }

/* ── Table cells ────────────────────────────────────────────────────────────── */
.order-num {
  font-weight: 700;
  color: #0F172A;
  font-size: 13px;
  font-family: "Inter", monospace;
}
.cell-date   { font-size: 13px; color: #374151; }
.cell-muted  { font-size: 13px; color: #94A3B8; }
.cell-amount {
  font-size: 14px;
  font-weight: 700;
  color: #2563EB;
}

/* ── Action buttons ─────────────────────────────────────────────────────────── */
.action-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
}

/* ── Responsive ─────────────────────────────────────────────────────────────── */
@media (max-width: 768px) {
  .container-xl { padding: 16px; }
  .title        { font-size: 18px; }
  .stats-row    { gap: 8px; }
  .stat-pill    { min-width: 72px; padding: 8px 12px; }
  .stat-val     { font-size: 18px; }
}
</style>