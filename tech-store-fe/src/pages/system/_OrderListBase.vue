<template>
  <div>
    <el-card shadow="never" class="order-list-card">
      <!-- Header -->
      <div class="list-header">
        <div>
          <div class="kicker">Admin · Quản lý đơn hàng</div>
          <div class="title">{{ title }}</div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <el-tag
            v-if="rows.length"
            type="info"
            effect="plain"
            class="count-tag"
          >
            {{ rows.length }} đơn
          </el-tag>
          <el-button class="reload-btn" :loading="loading" @click="load">
            <svg
              v-if="!loading"
              width="13"
              height="13"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
              style="margin-right: 5px"
            >
              <polyline points="23 4 23 10 17 10" />
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
            </svg>
            Reload
          </el-button>
        </div>
      </div>

      <el-divider class="my-3" />

      <!-- Table -->
      <el-table
        :data="pagedRows"
        v-loading="loading"
        border
        stripe
        @row-click="goDetail"
        class="order-table"
        row-class-name="order-row"
      >
        <el-table-column prop="id" label="Mã đơn" width="90" align="center">
          <template #default="{ row }">
            <span class="cell-id">#{{ row.id }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Số đơn" width="150">
          <template #default="{ row }">
            <span class="cell-order-number">{{ row.orderNumber }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Trạng thái" width="155" align="center">
          <template #default="{ row }">
            <el-tag
              :type="statusMap[row.status]?.type ?? 'info'"
              effect="light"
              class="status-tag"
              round
            >
              {{ statusMap[row.status]?.label ?? row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Khách hàng" width="130" align="center">
          <template #default="{ row }">
            <span class="cell-mono">{{ row.customerId }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Thanh toán" width="160">
          <template #default="{ row }">
            <div class="cell-payment">
              <span class="payment-icon">{{
                paymentIcon(row.paymentMethod)
              }}</span>
              {{ formatPayment(row.paymentMethod) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Kênh" width="115" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.channel === 'ONLINE' ? 'primary' : 'warning'"
              effect="plain"
              size="small"
              class="channel-tag"
            >
              {{ row.channel === "ONLINE" ? "🌐 Online" : "🏪 Offline" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Thời gian tạo" min-width="160">
          <template #default="{ row }">
            <template v-if="row.createdAtDate">
              <div class="cell-date">
                <span class="date-main">{{ row.createdAtDate }}</span>
                <span class="date-time">{{ row.createdAtTime }}</span>
              </div>
            </template>
            <span v-else class="no-date">—</span>
          </template>
        </el-table-column>

        <el-table-column label="Tổng tiền" width="150" align="right">
          <template #default="{ row }">
            <span class="cell-amount">{{
              formatCurrency(row.totalAmount)
            }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Giảm giá" width="130" align="right">
          <template #default="{ row }">
            <span v-if="row.discountTotal" class="cell-discount">
              -{{ formatCurrency(row.discountTotal) }}
            </span>
            <span v-else class="no-date">—</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && rows.length === 0"
        description="Không có đơn hàng nào"
        :image-size="80"
        class="mt-4"
      />

      <!-- Pagination -->
      <div v-if="rows.length > 0" class="pagination-bar">
        <span class="pagination-info">
          Hiển thị {{ pageStart }}–{{ pageEnd }} / {{ rows.length }} đơn
        </span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="rows.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper"
          background
          class="pagination"
          @size-change="onSizeChange"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "../../ui/toast";

const props = defineProps({
  title: { type: String, required: true },
  endpoint: { type: String, required: true },
  loader: { type: Function, required: true },
});

const router = useRouter();
const loading = ref(false);
const rows = ref([]);

// ── Pagination state ───────────────────────────────────
const currentPage = ref(1);
const pageSize = ref(20);

const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return rows.value.slice(start, start + pageSize.value);
});

const pageStart = computed(() =>
  rows.value.length === 0 ? 0 : (currentPage.value - 1) * pageSize.value + 1,
);
const pageEnd = computed(() =>
  Math.min(currentPage.value * pageSize.value, rows.value.length),
);

function onSizeChange(val) {
  pageSize.value = val;
  currentPage.value = 1;
}
function onPageChange(val) {
  currentPage.value = val;
}

// ── Status config ──────────────────────────────────────
const statusMap = {
  PENDING: { label: "Chờ xử lý", type: "warning" },
  PROCESSING: { label: "Đang xử lý", type: "" },
  SHIPPING: { label: "Đang giao", type: "primary" },
  DELIVERED: { label: "Đã giao", type: "success" },
  PAID: { label: "Đã thanh toán", type: "success" },
  CANCELLED: { label: "Đã huỷ", type: "danger" },
};

// ── Format helpers ─────────────────────────────────────
function parseDate(val) {
  if (!val) return null;
  if (Array.isArray(val)) {
    const [y, mo, d, h = 0, min = 0] = val;
    return new Date(y, mo - 1, d, h, min);
  }
  const d = new Date(val);
  return isNaN(d.getTime()) ? null : d;
}

function formatCurrency(val) {
  if (val == null) return "—";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(val);
}

function formatDate(val) {
  const d = parseDate(val);
  if (!d) return null;
  return d.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function formatTime(val) {
  const d = parseDate(val);
  if (!d) return "";
  return d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
}

function formatPayment(val) {
  const map = {
    CASH: "Tiền mặt",
    CARD: "Thẻ",
    TRANSFER: "Chuyển khoản",
    COD: "COD",
  };
  return map[val] ?? val ?? "—";
}

function paymentIcon(val) {
  const map = { CASH: "💵", CARD: "💳", TRANSFER: "🏦", COD: "📦" };
  return map[val] ?? "💰";
}

// ── Data helpers ───────────────────────────────────────
function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  const root = payload?.data ?? payload;
  if (Array.isArray(root)) return root;
  for (const k of ["content", "items", "results", "rows", "list"]) {
    if (Array.isArray(root?.[k])) return root[k];
    if (Array.isArray(root?.data?.[k])) return root.data[k];
  }
  return [];
}

function normalize(list) {
  return (list || [])
    .map((o, idx) => {
      const raw = o?.createdAt ?? o?.createdDate ?? null;
      return {
        id: o?.id ?? o?.orderId ?? idx + 1,
        orderNumber: o?.orderNumber ?? o?.order_number ?? "—",
        status: o?.status ?? "—",
        customerId: o?.customerId ?? o?.customer?.id ?? "—",
        paymentMethod: o?.paymentMethod ?? "—",
        channel: o?.channel ?? "—",
        totalAmount: o?.totalAmount ?? o?.total_amount ?? null,
        discountTotal: o?.discountTotal ?? o?.discount_total ?? null,
        createdAtDate: formatDate(raw),
        createdAtTime: formatTime(raw),
        _sortKey: parseDate(raw)?.getTime() ?? 0, // 👈 thêm dòng này
      };
    })
    .sort((a, b) => b._sortKey - a._sortKey); // 👈 sort mới nhất lên đầu
}

// ── Load ───────────────────────────────────────────────
async function load() {
  loading.value = true;
  try {
    const res = await props.loader();
    rows.value = normalize(extractList(res?.data));
    currentPage.value = 1; // reset về trang 1 khi reload
  } catch {
    rows.value = [];
    toast("Không thể tải danh sách đơn hàng.", "error");
  } finally {
    loading.value = false;
  }
}

function goDetail(row) {
  const id = row?.id;
  if (!id) return;
  router.push(`/system/orders/${id}`);
}

defineExpose({ load });
onMounted(load);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");
* {
  font-family: "Inter", sans-serif;
}

.order-list-card {
  border-radius: 16px !important;
  border: 1.5px solid #e2e8f0 !important;
}

/* ── Header ──────────────────────────────────────────── */
.list-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.kicker {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: #94a3b8;
}
.title {
  font-weight: 800;
  font-size: 18px;
  color: #0f172a;
  letter-spacing: -0.3px;
}
.muted {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.count-tag {
  font-family: "Inter", sans-serif !important;
  font-weight: 600 !important;
  border-radius: 8px !important;
}
.reload-btn {
  font-family: "Inter", sans-serif !important;
  font-weight: 600 !important;
  font-size: 13px !important;
  border-radius: 9px !important;
  height: 36px !important;
  display: inline-flex !important;
  align-items: center !important;
}

/* ── Table ───────────────────────────────────────────── */
.order-table {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.order-row) {
  cursor: pointer;
  transition: background 0.15s;
}
:deep(.order-row:hover td) {
  background: #f0f7ff !important;
}

:deep(.el-table th) {
  background: #f8fafc !important;
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
:deep(.el-table td) {
  font-size: 13.5px;
  color: #1e293b;
  padding: 10px 12px !important;
}

/* ── Cells ───────────────────────────────────────────── */
.cell-id {
  font-weight: 700;
  font-size: 13px;
  color: #3b82f6;
  letter-spacing: 0.3px;
}
.cell-mono {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}
.cell-payment {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 500;
}
.payment-icon {
  font-size: 15px;
}

/* ── Date cell ───────────────────────────────────────── */
.cell-date {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.3;
}
.date-main {
  font-size: 13.5px;
  font-weight: 600;
  color: #1e293b;
}
.date-time {
  font-size: 11.5px;
  font-weight: 500;
  color: #94a3b8;
}
.no-date {
  color: #cbd5e1;
  font-size: 13px;
}

/* ── Tags ────────────────────────────────────────────── */
.status-tag {
  font-family: "Inter", sans-serif !important;
  font-size: 12px !important;
  font-weight: 600 !important;
}
.channel-tag {
  font-family: "Inter", sans-serif !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  border-radius: 6px !important;
}

.cell-order-number {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  letter-spacing: 0.2px;
}
.cell-amount {
  font-size: 13.5px;
  font-weight: 700;
  color: #0f172a;
}
.cell-discount {
  font-size: 13px;
  font-weight: 600;
  color: #ef4444;
}

/* ── Pagination ──────────────────────────────────────── */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid #f1f5f9;
}

.pagination-info {
  font-size: 12.5px;
  font-weight: 500;
  color: #94a3b8;
  white-space: nowrap;
}

.pagination {
  font-family: "Inter", sans-serif !important;
}

:deep(.el-pagination .el-pagination__sizes .el-select .el-input__inner),
:deep(.el-pagination button),
:deep(.el-pagination .el-pager li) {
  font-family: "Inter", sans-serif !important;
  font-size: 13px !important;
  font-weight: 500 !important;
  border-radius: 8px !important;
}

:deep(.el-pagination .el-pager li.is-active) {
  font-weight: 700 !important;
}
</style>
