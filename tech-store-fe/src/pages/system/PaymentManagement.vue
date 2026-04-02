<!-- FILE: src/pages/system/PaymentManagement.vue -->
<template>
  <div class="pm-root">

    <!-- ── Page Header ── -->
    <el-card shadow="never" class="pm-header-card">
      <el-row justify="space-between" align="middle" :gutter="24">
        <el-col :span="8">
          <div class="pm-eyebrow">Hệ thống · Tài chính</div>
          <h1 class="pm-title">Lịch sử Thanh toán</h1>
          <p class="pm-sub">Theo dõi và quản lý tất cả giao dịch thanh toán</p>
          <el-button plain :loading="loading" @click="loadPayments">
            <el-icon v-if="!loading"><Refresh /></el-icon>
            Tải lại
          </el-button>
        </el-col>
        <el-col :span="16">
          <el-row :gutter="10" justify="end">
            <el-col :span="4" v-for="stat in statCards" :key="stat.label">
              <div :class="['pm-stat-card', `pm-stat-card--${stat.color}`]">
                <div :class="['pm-stat-icon', `pm-stat-icon--${stat.color}`]">
                  <el-icon :size="18"><component :is="stat.icon" /></el-icon>
                </div>
                <div class="pm-stat-value">{{ stat.value }}</div>
                <div class="pm-stat-label">{{ stat.label }}</div>
              </div>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </el-card>

    <!-- ── Main Card ── -->
    <el-card shadow="never" class="pm-main-card">

      <!-- Filters -->
      <el-row :gutter="12" align="middle" class="pm-filters">
        <el-col :span="10">
          <el-input v-model="filters.search" placeholder="Tìm theo Mã đơn, Ref, hoặc Khách hàng…" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-select v-model="filters.status" placeholder="Trạng thái" clearable @change="applyFilters" style="width:100%">
            <el-option label="Tất cả trạng thái" value="" />
            <el-option label="Đã thanh toán" value="PAID" />
            <el-option label="Hoàn tiền" value="REFUNDED" />
            <el-option label="Chờ xử lý" value="PENDING" />
            <el-option label="Thất bại" value="FAILED" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="filters.method" placeholder="Phương thức" clearable @change="applyFilters" style="width:100%">
            <el-option label="Tất cả phương thức" value="" />
            <el-option label="Tiền mặt" value="CASH" />
            <el-option label="Chuyển khoản" value="BANK_TRANSFER" />
            <el-option label="Thẻ tín dụng" value="CREDIT_CARD" />
            <el-option label="Ví điện tử" value="E_WALLET" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-tag v-if="filteredPayments.length !== payments.length" type="info" size="small">
            {{ filteredPayments.length }} / {{ payments.length }} kết quả
          </el-tag>
        </el-col>
      </el-row>

      <!-- Table -->
      <el-table :data="filteredPayments" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="72" sortable>
          <template #default="{ row }">
            <span class="pm-id">#{{ row.id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="orderId" label="Đơn hàng" width="140" sortable>
          <template #default="{ row }">
            <div class="pm-order-num">{{ row.orderNumber }}</div>
            <div class="pm-order-sub">Đơn #{{ row.orderId }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="customerName" label="Khách hàng" min-width="180">
          <template #default="{ row }">
            <el-space>
              <el-avatar :size="32" class="pm-avatar">{{ (row.customerName || 'U')[0].toUpperCase() }}</el-avatar>
              <div>
                <div class="pm-customer-name">{{ row.customerName || 'N/A' }}</div>
                <div class="pm-customer-id">ID {{ row.customerId }}</div>
              </div>
            </el-space>
          </template>
        </el-table-column>

        <el-table-column prop="amount" label="Số tiền" width="140" sortable align="right">
          <template #default="{ row }">
            <span class="pm-amount">{{ formatCurrency(row.amount) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="method" label="Phương thức" width="150">
          <template #default="{ row }">
            <el-space>
              <el-icon class="pm-method-icon"><component :is="getMethodIconComponent(row.method)" /></el-icon>
              <span>{{ formatMethod(row.method) }}</span>
            </el-space>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="Trạng thái" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="dark">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="paidAt" label="Thời gian" width="160" sortable>
          <template #default="{ row }">
            <div class="pm-date">{{ formatDateShort(row.paidAt) }}</div>
            <div class="pm-time">{{ formatTime(row.paidAt) }}</div>
          </template>
        </el-table-column>

        <el-table-column label="" width="110" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              plain size="small" type="primary"
              @click="viewDetail(row.id)"
              :loading="loadingDetail && selectedPaymentId === row.id"
            >
              <el-icon><View /></el-icon>
              Chi tiết
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Empty State -->
      <el-empty v-if="!loading && filteredPayments.length === 0" description="Không tìm thấy giao dịch nào" />
    </el-card>

    <!-- ── Detail Dialog ── -->
    <el-dialog v-model="detailDialog" width="860px" top="4vh">
      <template #header>
        <el-space>
          <el-icon class="color-primary"><CreditCard /></el-icon>
          <span class="pm-dialog-title">Thanh toán #{{ selectedPayment?.id }}</span>
          <el-tag v-if="selectedPayment" :type="statusTagType(selectedPayment.status)" size="small" effect="dark">
            {{ formatStatus(selectedPayment.status) }}
          </el-tag>
        </el-space>
      </template>

      <div v-if="selectedPayment" class="pm-detail">
        <el-row :gutter="16">
          <!-- Payment Info -->
          <el-col :span="12">
            <el-card shadow="never" class="pm-detail-card">
              <template #header>
                <el-space class="color-primary">
                  <el-icon><Tickets /></el-icon><strong>Thông tin thanh toán</strong>
                </el-space>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="Đơn hàng">
                  <router-link :to="`/system/orders/${selectedPayment.orderId}`" class="pm-link">
                    {{ selectedPayment.orderNumber }}
                  </router-link>
                </el-descriptions-item>
                <el-descriptions-item label="Số tiền">
                  <strong class="pm-amount">{{ formatCurrency(selectedPayment.amount) }}</strong>
                </el-descriptions-item>
                <el-descriptions-item label="Phương thức">
                  <el-space>
                    <el-icon class="pm-method-icon"><component :is="getMethodIconComponent(selectedPayment.method)" /></el-icon>
                    {{ formatMethod(selectedPayment.method) }}
                  </el-space>
                </el-descriptions-item>
                <el-descriptions-item label="Kênh">{{ selectedPayment.channel || '—' }}</el-descriptions-item>
                <el-descriptions-item label="Mã giao dịch">
                  <el-tag type="info" size="small" effect="plain">{{ selectedPayment.transactionRef || '—' }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Trạng thái đơn">
                  <el-tag size="small" :type="selectedPayment.orderStatus === 'DELIVERED' ? 'success' : 'warning'" effect="dark">
                    {{ selectedPayment.orderStatus }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>

          <!-- Customer Info -->
          <el-col :span="12">
            <el-card shadow="never" class="pm-detail-card">
              <template #header>
                <el-space class="color-primary">
                  <el-icon><User /></el-icon><strong>Khách hàng</strong>
                </el-space>
              </template>
              <el-space direction="vertical" :size="10" alignment="flex-start" style="width:100%">
                <el-space>
                  <el-avatar :size="44" class="pm-avatar pm-avatar--lg">
                    {{ (selectedPayment.customerName || 'U')[0].toUpperCase() }}
                  </el-avatar>
                  <div>
                    <div class="pm-customer-name pm-customer-name--lg">{{ selectedPayment.customerName }}</div>
                    <div class="pm-customer-meta"><el-icon><Message /></el-icon> {{ selectedPayment.customerEmail || '—' }}</div>
                    <div class="pm-customer-meta"><el-icon><Phone /></el-icon> {{ selectedPayment.customerPhone || '—' }}</div>
                  </div>
                </el-space>
                <el-space wrap>
                  <el-tag size="small" type="info">{{ selectedPayment.customerType || 'REGULAR' }}</el-tag>
                  <el-tag v-if="selectedPayment.vipTier" size="small" type="warning" effect="dark">{{ selectedPayment.vipTier }}</el-tag>
                </el-space>
              </el-space>
            </el-card>
          </el-col>
        </el-row>

        <!-- Items Table -->
        <el-card v-if="selectedPayment.items && selectedPayment.items.length > 0" shadow="never" class="pm-detail-card">
          <template #header>
            <el-space class="color-primary">
              <el-icon><ShoppingBag /></el-icon><strong>Sản phẩm đã mua</strong>
            </el-space>
          </template>
          <el-table :data="selectedPayment.items" border size="small">
            <el-table-column label="" width="72">
              <template #default="{ row }">
                <img v-if="row.imageUrl" :src="row.imageUrl" class="pm-item-img" @error="handleImageError" />
                <el-icon v-else :size="24" class="pm-item-placeholder"><Picture /></el-icon>
              </template>
            </el-table-column>
            <el-table-column label="Sản phẩm" min-width="200">
              <template #default="{ row }">
                <div class="pm-item-name">{{ row.productName }}</div>
                <div v-if="row.variantName" class="pm-item-variant">{{ row.variantName }}</div>
                <div v-if="row.sku" class="pm-item-sku">SKU: {{ row.sku }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="SL" width="72" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="primary" effect="plain">{{ row.quantity }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Đơn giá" width="120" align="right">
              <template #default="{ row }">{{ formatCurrency(row.unitPrice) }}</template>
            </el-table-column>
            <el-table-column label="Giảm giá" width="110" align="right">
              <template #default="{ row }">
                <el-text v-if="row.discount > 0" type="danger">-{{ formatCurrency(row.discount) }}</el-text>
                <el-text v-else type="info">—</el-text>
              </template>
            </el-table-column>
            <el-table-column label="Thành tiền" width="130" align="right">
              <template #default="{ row }">
                <strong class="color-primary">{{ formatCurrency(row.lineTotal) }}</strong>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- Summary + Notes -->
        <el-row :gutter="16">
          <el-col v-if="selectedPayment.notes" :span="12">
            <el-card shadow="never" class="pm-detail-card">
              <template #header>
                <el-space class="color-primary"><el-icon><Document /></el-icon><strong>Ghi chú</strong></el-space>
              </template>
              <p class="pm-notes">{{ selectedPayment.notes }}</p>
            </el-card>
          </el-col>
          <el-col :span="selectedPayment.notes ? 12 : 24">
            <el-card shadow="never" class="pm-detail-card pm-summary-card">
              <template #header>
                <el-space class="color-primary"><el-icon><Coin /></el-icon><strong>Tóm tắt đơn hàng</strong></el-space>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="Tạm tính">{{ formatCurrency(selectedPayment.subtotal) }}</el-descriptions-item>
                <el-descriptions-item v-if="selectedPayment.discountTotal > 0" label="Giảm giá">
                  <el-text type="danger">-{{ formatCurrency(selectedPayment.discountTotal) }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item v-if="selectedPayment.taxTotal > 0" label="Thuế">{{ formatCurrency(selectedPayment.taxTotal) }}</el-descriptions-item>
                <el-descriptions-item v-if="selectedPayment.shippingFee > 0" label="Vận chuyển">{{ formatCurrency(selectedPayment.shippingFee) }}</el-descriptions-item>
                <el-descriptions-item label="Tổng cộng">
                  <strong class="pm-total-value">{{ formatCurrency(selectedPayment.totalAmount) }}</strong>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <template #footer>
        <el-button @click="detailDialog = false">Đóng</el-button>
        <el-button
          v-if="selectedPayment?.status === 'PAID'"
          type="danger"
          @click="handleRefund(selectedPayment.id)"
          :loading="refunding"
        >
          <el-icon><RefreshLeft /></el-icon>
          Hoàn tiền
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { paymentsApi } from "../../api/payments";
import { toast } from "../../ui/toast";
import { ElMessageBox } from "element-plus";
import {
  Refresh, Search, View, CreditCard, User, Message, Phone,
  ShoppingBag, Picture, Document, Coin, Tickets, RefreshLeft,
  Wallet, Check, Timer, Money, OfficeBuilding,
} from "@element-plus/icons-vue";

const loading = ref(false);
const loadingDetail = ref(false);
const refunding = ref(false);
const payments = ref([]);
const detailDialog = ref(false);
const selectedPayment = ref(null);
const selectedPaymentId = ref(null);

const filters = ref({ search: "", status: "", method: "" });

const filteredPayments = computed(() => {
  let result = [...payments.value];
  if (filters.value.search) {
    const s = filters.value.search.toLowerCase();
    result = result.filter(
      (p) =>
        p.orderId?.toString().includes(s) ||
        p.orderNumber?.toLowerCase().includes(s) ||
        p.transactionRef?.toLowerCase().includes(s) ||
        p.customerName?.toLowerCase().includes(s)
    );
  }
  if (filters.value.status) result = result.filter((p) => p.status === filters.value.status);
  if (filters.value.method) result = result.filter((p) => p.method === filters.value.method);
  return result;
});

const statusCount = (s) => payments.value.filter((p) => p.status === s).length;
const totalRevenue = computed(() =>
  payments.value.filter((p) => p.status === "PAID").reduce((sum, p) => sum + (p.amount || 0), 0)
);

const statCards = computed(() => [
  { label: "Tổng giao dịch", value: payments.value.length, icon: "Tickets", color: "blue" },
  { label: "Đã thanh toán", value: statusCount("PAID"), icon: "Check", color: "green" },
  { label: "Chờ xử lý", value: statusCount("PENDING"), icon: "Timer", color: "amber" },
  { label: "Hoàn tiền", value: statusCount("REFUNDED"), icon: "RefreshLeft", color: "red" },
  { label: "Doanh thu", value: formatCurrencyShort(totalRevenue.value), icon: "Wallet", color: "purple" },
]);

async function loadPayments() {
  loading.value = true;
  try {
    const { data } = await paymentsApi.getAll();
    payments.value = data || [];
    toast("Tải dữ liệu thành công", "success");
  } catch (e) {
    toast("Không thể tải dữ liệu", "error");
  } finally {
    loading.value = false;
  }
}

function applyFilters() {}

async function viewDetail(paymentId) {
  selectedPaymentId.value = paymentId;
  loadingDetail.value = true;
  try {
    const { data } = await paymentsApi.getDetail(paymentId);
    selectedPayment.value = data;
    detailDialog.value = true;
  } catch (e) {
    toast("Không thể tải chi tiết thanh toán", "error");
  } finally {
    loadingDetail.value = false;
    selectedPaymentId.value = null;
  }
}

async function handleRefund(paymentId) {
  try {
    await ElMessageBox.confirm(
      `Xác nhận hoàn tiền cho giao dịch #${paymentId}? Hành động này không thể hoàn tác.`,
      "Xác nhận hoàn tiền",
      { confirmButtonText: "Hoàn tiền", cancelButtonText: "Hủy", type: "warning" }
    );
  } catch { return; }
  refunding.value = true;
  try {
    await paymentsApi.refund(paymentId);
    toast("Hoàn tiền thành công", "success");
    detailDialog.value = false;
    await loadPayments();
  } catch (e) {
    toast("Không thể xử lý hoàn tiền", "error");
  } finally {
    refunding.value = false;
  }
}

function handleImageError(e) { e.target.style.display = "none"; }

function statusTagType(s) {
  return { PAID: "success", REFUNDED: "primary", PENDING: "warning", FAILED: "danger" }[s] || "info";
}

function getMethodIconComponent(m) {
  return { CASH: "Money", BANK_TRANSFER: "OfficeBuilding", CREDIT_CARD: "CreditCard", E_WALLET: "Wallet" }[m] || "CreditCard";
}

function formatCurrency(v) {
  if (!v) return "₫0";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(v);
}
function formatCurrencyShort(v) {
  if (!v) return "₫0";
  if (v >= 1_000_000_000) return `₫${(v / 1_000_000_000).toFixed(1)}B`;
  if (v >= 1_000_000) return `₫${(v / 1_000_000).toFixed(1)}M`;
  return formatCurrency(v);
}
function formatDateShort(d) {
  if (!d) return "—";
  return new Date(d).toLocaleDateString("vi-VN");
}
function formatTime(d) {
  if (!d) return "";
  return new Date(d).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
}
function formatMethod(m) {
  return { CASH: "Tiền mặt", BANK_TRANSFER: "Chuyển khoản", CREDIT_CARD: "Thẻ tín dụng", E_WALLET: "Ví điện tử" }[m] || m;
}
function formatStatus(s) {
  return { PAID: "Đã thanh toán", REFUNDED: "Hoàn tiền", PENDING: "Chờ xử lý", FAILED: "Thất bại" }[s] || s;
}

loadPayments();
</script>

<style scoped>
/* ── Root ── */
.pm-root {
  min-height: 100vh;
  padding: 28px 32px 48px;
}

/* ── Header Card ── */
.pm-header-card { margin-bottom: 20px; }

.pm-eyebrow {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--el-color-primary);
  margin-bottom: 4px;
}

.pm-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f1c35;
  margin: 0 0 4px;
}

.pm-sub {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 14px;
}

/* ── Stat Cards ── */
.pm-stat-card {
  border-radius: 10px;
  padding: 12px 14px;
  border: 1px solid transparent;
  text-align: left;
}
.pm-stat-card--blue   { background: #eff6ff; border-color: #bfdbfe; }
.pm-stat-card--green  { background: #f0fdf4; border-color: #bbf7d0; }
.pm-stat-card--amber  { background: #fffbeb; border-color: #fde68a; }
.pm-stat-card--red    { background: #fff1f2; border-color: #fecdd3; }
.pm-stat-card--purple { background: #faf5ff; border-color: #e9d5ff; }

.pm-stat-icon {
  width: 34px; height: 34px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 8px;
}
.pm-stat-icon--blue   { background: #dbeafe; color: #1d4ed8; }
.pm-stat-icon--green  { background: #dcfce7; color: #15803d; }
.pm-stat-icon--amber  { background: #fef3c7; color: #b45309; }
.pm-stat-icon--red    { background: #fee2e2; color: #dc2626; }
.pm-stat-icon--purple { background: #ede9fe; color: #7c3aed; }

.pm-stat-value {
  font-size: 20px;
  font-weight: 800;
  color: #0f1c35;
  line-height: 1.1;
}
.pm-stat-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 500;
  margin-top: 2px;
  white-space: nowrap;
}

/* ── Main Card ── */
.pm-main-card { overflow: hidden; }
.pm-filters { margin-bottom: 16px; }

/* ── Table cells ── */
.pm-id { font-size: 12px; font-weight: 700; color: #6b7fa3; }

.pm-order-num { font-weight: 700; font-size: 13px; color: #1e3a5f; }
.pm-order-sub { font-size: 11px; color: #94a3b8; margin-top: 2px; }

.pm-avatar { background: linear-gradient(135deg, #3b82f6, #1d4ed8) !important; color: #fff !important; }
.pm-avatar--lg { width: 44px !important; height: 44px !important; font-size: 18px !important; }

.pm-customer-name { font-size: 13px; font-weight: 600; color: #1e3a5f; }
.pm-customer-name--lg { font-size: 15px; font-weight: 700; margin-bottom: 4px; }
.pm-customer-id { font-size: 11px; color: #94a3b8; }
.pm-customer-meta { font-size: 12px; color: #64748b; margin-bottom: 2px; display: flex; align-items: center; gap: 4px; }

.pm-amount { font-weight: 700; font-size: 14px; color: var(--el-color-primary); }
.pm-method-icon { color: var(--el-color-primary); }
.pm-date { font-size: 13px; font-weight: 500; color: #374151; }
.pm-time { font-size: 11px; color: #9ca3af; }

/* ── Detail Dialog ── */
.pm-dialog-title { font-size: 16px; font-weight: 700; }

.pm-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pm-detail-card { border: 1px solid #e6edf8 !important; }

.pm-summary-card :deep(.el-descriptions__label) { white-space: nowrap; }

.pm-link { color: var(--el-color-primary); font-weight: 700; text-decoration: none; }
.pm-link:hover { text-decoration: underline; }

.pm-notes { font-size: 13px; color: #475569; line-height: 1.6; margin: 0; }

.pm-item-img { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; }
.pm-item-placeholder { color: #cbd5e1; }
.pm-item-name { font-size: 13px; font-weight: 600; color: #1e3a5f; }
.pm-item-variant { font-size: 11px; color: #64748b; margin-top: 2px; }
.pm-item-sku { font-size: 11px; color: #94a3b8; font-family: monospace; }

.pm-total-value { font-size: 18px; font-weight: 800; color: var(--el-color-primary); }

/* ── Utilities ── */
.color-primary { color: var(--el-color-primary); }
</style>