<!-- FILE: src/pages/system/PaymentManagement.vue -->
<template>
  <div class="pm-root">

    <!-- ── Page Header ── -->
    <div class="pm-header">
      <!-- Left: Title block -->
      <div class="pm-header__left">
        <div class="pm-header__eyebrow">
          <i class="bi bi-grid-3x3-gap-fill me-1"></i>Hệ thống · Tài chính
        </div>
        <h1 class="pm-header__title">Lịch sử Thanh toán</h1>
        <p class="pm-header__sub">Theo dõi và quản lý tất cả giao dịch thanh toán</p>
        <el-button class="pm-btn-reload" @click="loadPayments" :loading="loading" round>
          <i class="bi bi-arrow-clockwise me-1"></i>Tải lại
        </el-button>
      </div>

      <!-- Right: Stat Cards -->
      <div class="pm-stats">
        <div class="pm-stat pm-stat--blue">
          <div class="pm-stat__icon-wrap pm-stat__icon-wrap--blue">
            <i class="bi bi-receipt-cutoff"></i>
          </div>
          <div class="pm-stat__body">
            <div class="pm-stat__value">{{ payments.length }}</div>
            <div class="pm-stat__label">Tổng giao dịch</div>
          </div>
        </div>

        <div class="pm-stat pm-stat--green">
          <div class="pm-stat__icon-wrap pm-stat__icon-wrap--green">
            <i class="bi bi-check-circle-fill"></i>
          </div>
          <div class="pm-stat__body">
            <div class="pm-stat__value">{{ statusCount('PAID') }}</div>
            <div class="pm-stat__label">Đã thanh toán</div>
          </div>
        </div>

        <div class="pm-stat pm-stat--amber">
          <div class="pm-stat__icon-wrap pm-stat__icon-wrap--amber">
            <i class="bi bi-hourglass-split"></i>
          </div>
          <div class="pm-stat__body">
            <div class="pm-stat__value">{{ statusCount('PENDING') }}</div>
            <div class="pm-stat__label">Chờ xử lý</div>
          </div>
        </div>

        <div class="pm-stat pm-stat--red">
          <div class="pm-stat__icon-wrap pm-stat__icon-wrap--red">
            <i class="bi bi-arrow-return-left"></i>
          </div>
          <div class="pm-stat__body">
            <div class="pm-stat__value">{{ statusCount('REFUNDED') }}</div>
            <div class="pm-stat__label">Hoàn tiền</div>
          </div>
        </div>

        <div class="pm-stat pm-stat--purple">
          <div class="pm-stat__icon-wrap pm-stat__icon-wrap--purple">
            <i class="bi bi-wallet2"></i>
          </div>
          <div class="pm-stat__body">
            <div class="pm-stat__value pm-stat__value--sm">{{ formatCurrencyShort(totalRevenue) }}</div>
            <div class="pm-stat__label">Doanh thu</div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Main Card ── -->
    <div class="pm-card">

      <!-- Filters -->
      <div class="pm-filters">
        <el-input
          v-model="filters.search"
          placeholder="Tìm theo Mã đơn, Ref, hoặc Khách hàng…"
          clearable
          class="pm-filter__search"
        >
          <template #prefix>
            <i class="bi bi-search pm-filter__icon"></i>
          </template>
        </el-input>

        <el-select
          v-model="filters.status"
          placeholder="Trạng thái"
          clearable
          @change="applyFilters"
          class="pm-filter__select"
        >
          <el-option label="Tất cả trạng thái" value="" />
          <el-option label="Đã thanh toán" value="PAID" />
          <el-option label="Hoàn tiền" value="REFUNDED" />
          <el-option label="Chờ xử lý" value="PENDING" />
          <el-option label="Thất bại" value="FAILED" />
        </el-select>

        <el-select
          v-model="filters.method"
          placeholder="Phương thức"
          clearable
          @change="applyFilters"
          class="pm-filter__select"
        >
          <el-option label="Tất cả phương thức" value="" />
          <el-option label="Tiền mặt" value="CASH" />
          <el-option label="Chuyển khoản" value="BANK_TRANSFER" />
          <el-option label="Thẻ tín dụng" value="CREDIT_CARD" />
          <el-option label="Ví điện tử" value="E_WALLET" />
        </el-select>

        <div class="pm-filter__count" v-if="filteredPayments.length !== payments.length">
          <el-tag type="info" size="small" round>
            {{ filteredPayments.length }} / {{ payments.length }} kết quả
          </el-tag>
        </div>
      </div>

      <!-- Table -->
      <el-table
        :data="filteredPayments"
        v-loading="loading"
        stripe
        class="pm-table"
        :header-cell-style="tableHeaderStyle"
        :row-class-name="tableRowClass"
      >
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
            <div class="pm-customer">
              <div class="pm-customer__avatar">
                {{ (row.customerName || 'U')[0].toUpperCase() }}
              </div>
              <div>
                <div class="pm-customer__name">{{ row.customerName || 'N/A' }}</div>
                <div class="pm-customer__id">ID {{ row.customerId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="amount" label="Số tiền" width="140" sortable align="right">
          <template #default="{ row }">
            <span class="pm-amount">{{ formatCurrency(row.amount) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="method" label="Phương thức" width="150">
          <template #default="{ row }">
            <div class="pm-method-cell">
              <i :class="['pm-method-icon', getMethodIcon(row.method)]"></i>
              <span class="pm-method-label">{{ formatMethod(row.method) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="Trạng thái" width="130" align="center">
          <template #default="{ row }">
            <span :class="['pm-status', `pm-status--${row.status?.toLowerCase()}`]">
              {{ formatStatus(row.status) }}
            </span>
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
              class="pm-btn-detail"
              size="small"
              @click="viewDetail(row.id)"
              :loading="loadingDetail && selectedPaymentId === row.id"
            >
              <i class="bi bi-eye"></i>
              Chi tiết
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Empty State -->
      <div v-if="!loading && filteredPayments.length === 0" class="pm-empty">
        <div class="pm-empty__icon"><i class="bi bi-receipt"></i></div>
        <div class="pm-empty__title">Không tìm thấy giao dịch nào</div>
        <div class="pm-empty__sub">Thử thay đổi bộ lọc hoặc tải lại dữ liệu</div>
      </div>
    </div>

    <!-- ── Detail Dialog ── -->
    <el-dialog
      v-model="detailDialog"
      width="860px"
      top="4vh"
      class="pm-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="pm-dialog__header">
          <div>
            <div class="pm-dialog__eyebrow">Chi tiết giao dịch</div>
            <div class="pm-dialog__title">
              Thanh toán #{{ selectedPayment?.id }}
              <span
                v-if="selectedPayment"
                :class="['pm-status', `pm-status--${selectedPayment.status?.toLowerCase()}`]"
                style="vertical-align: middle; margin-left: 8px; font-size: 12px;"
              >{{ formatStatus(selectedPayment.status) }}</span>
            </div>
          </div>
          <button class="pm-dialog__close" @click="detailDialog = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
      </template>

      <div v-if="selectedPayment" class="pm-detail">

        <!-- Top row: Payment + Customer -->
        <div class="pm-detail__grid">

          <!-- Payment Info -->
          <div class="pm-detail__section">
            <div class="pm-detail__section-title">
              <i class="bi bi-receipt-cutoff"></i> Thông tin thanh toán
            </div>
            <div class="pm-kv-list">
              <div class="pm-kv">
                <span class="pm-kv__k">Đơn hàng</span>
                <span class="pm-kv__v">
                  <router-link :to="`/system/orders/${selectedPayment.orderId}`" class="pm-link">
                    {{ selectedPayment.orderNumber }}
                  </router-link>
                </span>
              </div>
              <div class="pm-kv">
                <span class="pm-kv__k">Số tiền</span>
                <span class="pm-kv__v pm-kv__v--amount">{{ formatCurrency(selectedPayment.amount) }}</span>
              </div>
              <div class="pm-kv">
                <span class="pm-kv__k">Phương thức</span>
                <span class="pm-kv__v">
                  <i :class="['me-1', getMethodIcon(selectedPayment.method)]"></i>
                  {{ formatMethod(selectedPayment.method) }}
                </span>
              </div>
              <div class="pm-kv">
                <span class="pm-kv__k">Kênh</span>
                <span class="pm-kv__v">{{ selectedPayment.channel || '—' }}</span>
              </div>
              <div class="pm-kv">
                <span class="pm-kv__k">Mã giao dịch</span>
                <code class="pm-code">{{ selectedPayment.transactionRef || '—' }}</code>
              </div>
              <div class="pm-kv">
                <span class="pm-kv__k">Trạng thái đơn</span>
                <el-tag size="small" :type="selectedPayment.orderStatus === 'DELIVERED' ? 'success' : 'warning'">
                  {{ selectedPayment.orderStatus }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- Customer Info -->
          <div class="pm-detail__section">
            <div class="pm-detail__section-title">
              <i class="bi bi-person-circle"></i> Khách hàng
            </div>
            <div class="pm-customer-card">
              <div class="pm-customer-card__avatar">
                {{ (selectedPayment.customerName || 'U')[0].toUpperCase() }}
              </div>
              <div class="pm-customer-card__info">
                <div class="pm-customer-card__name">{{ selectedPayment.customerName }}</div>
                <div class="pm-customer-card__meta">
                  <i class="bi bi-envelope me-1"></i>{{ selectedPayment.customerEmail || '—' }}
                </div>
                <div class="pm-customer-card__meta">
                  <i class="bi bi-phone me-1"></i>{{ selectedPayment.customerPhone || '—' }}
                </div>
                <div class="pm-customer-card__tags">
                  <el-tag size="small" type="info">{{ selectedPayment.customerType || 'REGULAR' }}</el-tag>
                  <el-tag v-if="selectedPayment.vipTier" size="small" type="warning" class="ms-1">
                    {{ selectedPayment.vipTier }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Items Table -->
        <div
          class="pm-detail__section pm-detail__section--full"
          v-if="selectedPayment.items && selectedPayment.items.length > 0"
        >
          <div class="pm-detail__section-title">
            <i class="bi bi-bag-check"></i> Sản phẩm đã mua
          </div>
          <el-table :data="selectedPayment.items" class="pm-items-table" border>
            <el-table-column label="" width="72">
              <template #default="{ row }">
                <img
                  v-if="row.imageUrl"
                  :src="row.imageUrl"
                  class="pm-item-img"
                  @error="handleImageError"
                />
                <div v-else class="pm-item-img pm-item-img--placeholder">
                  <i class="bi bi-image"></i>
                </div>
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
                <span class="pm-qty">{{ row.quantity }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Đơn giá" width="120" align="right">
              <template #default="{ row }">{{ formatCurrency(row.unitPrice) }}</template>
            </el-table-column>
            <el-table-column label="Giảm giá" width="110" align="right">
              <template #default="{ row }">
                <span v-if="row.discount > 0" class="pm-discount">-{{ formatCurrency(row.discount) }}</span>
                <span v-else class="pm-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="Thành tiền" width="130" align="right">
              <template #default="{ row }">
                <span class="pm-line-total">{{ formatCurrency(row.lineTotal) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Summary -->
        <div class="pm-detail__grid pm-detail__grid--bottom">
          <div class="pm-detail__section" v-if="selectedPayment.notes">
            <div class="pm-detail__section-title"><i class="bi bi-sticky me-1"></i>Ghi chú</div>
            <p class="pm-notes">{{ selectedPayment.notes }}</p>
          </div>
          <div class="pm-detail__section pm-summary">
            <div class="pm-detail__section-title"><i class="bi bi-calculator"></i> Tóm tắt đơn hàng</div>
            <div class="pm-summary__rows">
              <div class="pm-summary__row">
                <span>Tạm tính</span><span>{{ formatCurrency(selectedPayment.subtotal) }}</span>
              </div>
              <div class="pm-summary__row" v-if="selectedPayment.discountTotal > 0">
                <span>Giảm giá</span>
                <span class="pm-discount">-{{ formatCurrency(selectedPayment.discountTotal) }}</span>
              </div>
              <div class="pm-summary__row" v-if="selectedPayment.taxTotal > 0">
                <span>Thuế</span><span>{{ formatCurrency(selectedPayment.taxTotal) }}</span>
              </div>
              <div class="pm-summary__row" v-if="selectedPayment.shippingFee > 0">
                <span>Vận chuyển</span><span>{{ formatCurrency(selectedPayment.shippingFee) }}</span>
              </div>
            </div>
            <div class="pm-summary__total">
              <span>Tổng cộng</span>
              <span class="pm-summary__total-value">{{ formatCurrency(selectedPayment.totalAmount) }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="pm-dialog__footer">
          <el-button class="pm-btn-close" @click="detailDialog = false" round>
            Đóng
          </el-button>
          <el-button
            v-if="selectedPayment?.status === 'PAID'"
            type="danger"
            round
            @click="handleRefund(selectedPayment.id)"
            :loading="refunding"
            class="pm-btn-refund"
          >
            <i class="bi bi-arrow-return-left me-1"></i>Hoàn tiền
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { paymentsApi } from "../../api/payments";
import { toast } from "../../ui/toast";
import { ElMessageBox } from "element-plus";

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

const tableHeaderStyle = () => ({
  background: "#f0f4fa",
  color: "#4b5c78",
  fontWeight: "700",
  fontSize: "12px",
  textTransform: "uppercase",
  letterSpacing: "0.5px",
  borderBottom: "2px solid #dde5f0",
});

const tableRowClass = ({ rowIndex }) => (rowIndex % 2 === 0 ? "pm-row-even" : "pm-row-odd");

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
function getMethodIcon(m) {
  return { CASH: "bi bi-cash-coin", BANK_TRANSFER: "bi bi-bank2", CREDIT_CARD: "bi bi-credit-card", E_WALLET: "bi bi-wallet2" }[m] || "bi bi-credit-card";
}

loadPayments();
</script>

<style scoped>
/* ── Root ── */
.pm-root {
  min-height: 100vh;
  background: #f0f4fa;
  padding: 28px 32px 48px;
  font-family: 'DM Sans', 'Be Vietnam Pro', system-ui, sans-serif;
}

/* ── Header ── */
.pm-header {
  background: #ffffff;
  border-radius: 14px;
  padding: 24px 28px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(30,64,175,0.07), 0 4px 16px rgba(30,64,175,0.07);
  border: 1px solid #e8eef8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}
.pm-header__left {
  flex-shrink: 0;
}
.pm-header__eyebrow {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #3b82f6;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}
.pm-header__title {
  font-size: 22px;
  font-weight: 800;
  color: #0f1c35;
  margin: 0 0 4px;
  line-height: 1.2;
}
.pm-header__sub {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 14px;
}
.pm-btn-reload {
  background: #eff6ff !important;
  border: 1px solid #bfdbfe !important;
  color: #1d4ed8 !important;
  font-weight: 700 !important;
  font-size: 13px !important;
  transition: all 0.15s;
}
.pm-btn-reload:hover {
  background: #dbeafe !important;
  border-color: #93c5fd !important;
}

/* ── Stat Cards ── */
.pm-stats {
  display: flex;
  gap: 10px;
  flex: 1;
  justify-content: flex-end;
  flex-wrap: wrap;
}
.pm-stat {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-radius: 12px;
  min-width: 120px;
  border: 1px solid transparent;
  transition: transform 0.15s, box-shadow 0.15s;
}
.pm-stat:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.pm-stat--blue   { background: #eff6ff; border-color: #bfdbfe; }
.pm-stat--green  { background: #f0fdf4; border-color: #bbf7d0; }
.pm-stat--amber  { background: #fffbeb; border-color: #fde68a; }
.pm-stat--red    { background: #fff1f2; border-color: #fecdd3; }
.pm-stat--purple { background: #faf5ff; border-color: #e9d5ff; }

.pm-stat__icon-wrap {
  width: 38px; height: 38px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px;
  flex-shrink: 0;
}
.pm-stat__icon-wrap--blue   { background: #dbeafe; color: #1d4ed8; }
.pm-stat__icon-wrap--green  { background: #dcfce7; color: #15803d; }
.pm-stat__icon-wrap--amber  { background: #fef3c7; color: #b45309; }
.pm-stat__icon-wrap--red    { background: #fee2e2; color: #dc2626; }
.pm-stat__icon-wrap--purple { background: #ede9fe; color: #7c3aed; }

/* .pm-stat__body {} */
.pm-stat__value {
  font-size: 20px;
  font-weight: 800;
  color: #0f1c35;
  line-height: 1;
}
.pm-stat__value--sm { font-size: 15px; }
.pm-stat__label {
  font-size: 11px;
  color: #64748b;
  font-weight: 500;
  margin-top: 3px;
  white-space: nowrap;
}

/* ── Main Card ── */
.pm-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 4px rgba(30,64,175,0.06), 0 4px 20px rgba(30,64,175,0.08);
  overflow: hidden;
}

/* ── Filters ── */
.pm-filters {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #edf1f7;
  flex-wrap: wrap;
}
.pm-filter__search { flex: 1; min-width: 220px; }
.pm-filter__select { width: 160px; }
.pm-filter__icon { color: #6b7fa3; }
.pm-filter__count { margin-left: auto; }

/* ── Table ── */
.pm-table { border: none !important; }
.pm-table :deep(td) {
  border-bottom: 1px solid #f0f4fa !important;
  padding: 12px 16px !important;
}
.pm-table :deep(th) { padding: 10px 16px !important; }
.pm-table :deep(.pm-row-even td) { background: #fff !important; }
.pm-table :deep(.pm-row-odd td) { background: #fafbfe !important; }
.pm-table :deep(tr:hover td) { background: #eef3fd !important; }

.pm-id { font-size: 12px; font-weight: 700; color: #6b7fa3; }
.pm-order-num { font-weight: 700; font-size: 13px; color: #1e3a5f; }
.pm-order-sub { font-size: 11px; color: #94a3b8; margin-top: 2px; }

.pm-customer { display: flex; align-items: center; gap: 10px; }
.pm-customer__avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #fff;
  font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.pm-customer__name { font-size: 13px; font-weight: 600; color: #1e3a5f; }
.pm-customer__id { font-size: 11px; color: #94a3b8; }

.pm-amount { font-weight: 700; font-size: 14px; color: #1d4ed8; }

.pm-method-cell { display: flex; align-items: center; gap: 7px; }
.pm-method-icon { font-size: 15px; color: #3b82f6; }
.pm-method-label { font-size: 13px; color: #374151; font-weight: 500; }

.pm-status {
  display: inline-flex; align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px; font-weight: 700;
  letter-spacing: 0.5px; text-transform: uppercase;
}
.pm-status--paid { background: #dcfce7; color: #15803d; }
.pm-status--refunded { background: #dbeafe; color: #1d4ed8; }
.pm-status--pending { background: #fef3c7; color: #b45309; }
.pm-status--failed { background: #fee2e2; color: #dc2626; }

.pm-date { font-size: 13px; font-weight: 500; color: #374151; }
.pm-time { font-size: 11px; color: #9ca3af; }

.pm-btn-detail {
  background: #eff6ff !important;
  border: 1px solid #bfdbfe !important;
  color: #2563eb !important;
  font-weight: 600 !important;
  font-size: 12px !important;
  border-radius: 6px !important;
  padding: 5px 12px !important;
  transition: all 0.15s;
}
.pm-btn-detail:hover {
  background: #dbeafe !important;
  border-color: #93c5fd !important;
}

/* ── Empty ── */
.pm-empty {
  padding: 64px 0;
  text-align: center;
}
.pm-empty__icon {
  font-size: 48px; color: #cbd5e1; margin-bottom: 12px;
}
.pm-empty__title { font-size: 16px; font-weight: 700; color: #475569; }
.pm-empty__sub { font-size: 13px; color: #94a3b8; margin-top: 4px; }

/* ── Dialog ── */
.pm-dialog :deep(.el-dialog) {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(30, 64, 175, 0.18) !important;
}
.pm-dialog :deep(.el-dialog__header) {
  padding: 0 !important; margin: 0 !important;
}
.pm-dialog :deep(.el-dialog__body) {
  padding: 0 !important;
}
.pm-dialog :deep(.el-dialog__footer) {
  padding: 0 !important;
  border-top: 1px solid #edf1f7;
}

.pm-dialog__header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 28px;
  background: linear-gradient(135deg, #1a56db, #1d4ed8);
}
.pm-dialog__eyebrow {
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 1px; color: rgba(255,255,255,0.6); margin-bottom: 2px;
}
.pm-dialog__title {
  font-size: 18px; font-weight: 800; color: #fff;
}
.pm-dialog__close {
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 8px;
  color: #fff;
  width: 32px; height: 32px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px;
  transition: background 0.15s;
}
.pm-dialog__close:hover { background: rgba(255,255,255,0.28); }

/* ── Detail Body ── */
.pm-detail {
  max-height: 65vh;
  overflow-y: auto;
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.pm-detail__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.pm-detail__grid--bottom {
  grid-template-columns: 1fr 1fr;
}
.pm-detail__section {
  background: #f8faff;
  border: 1px solid #e6edf8;
  border-radius: 10px;
  padding: 16px 18px;
}
.pm-detail__section--full { grid-column: 1 / -1; }
.pm-detail__section-title {
  font-size: 11px; font-weight: 800;
  text-transform: uppercase; letter-spacing: 0.7px;
  color: #3b82f6; margin-bottom: 14px;
  display: flex; align-items: center; gap: 6px;
}

.pm-kv-list { display: flex; flex-direction: column; gap: 9px; }
.pm-kv { display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
.pm-kv__k { color: #6b7fa3; font-weight: 500; }
.pm-kv__v { color: #1e3a5f; font-weight: 600; text-align: right; }
.pm-kv__v--amount { color: #1d4ed8; font-size: 15px; font-weight: 800; }
.pm-link { color: #2563eb; font-weight: 700; text-decoration: none; }
.pm-link:hover { text-decoration: underline; }
.pm-code {
  background: #eef2fa; padding: 2px 7px;
  border-radius: 5px; font-size: 12px; color: #374151;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

/* Customer Card */
.pm-customer-card { display: flex; gap: 14px; }
.pm-customer-card__avatar {
  width: 44px; height: 44px; border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #fff; font-size: 18px; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.pm-customer-card__name { font-size: 15px; font-weight: 700; color: #1e3a5f; margin-bottom: 4px; }
.pm-customer-card__meta { font-size: 12px; color: #64748b; margin-bottom: 3px; }
.pm-customer-card__tags { margin-top: 8px; }

/* Items table */
.pm-items-table :deep(th) {
  background: #f0f4fa !important;
  font-size: 11px !important; font-weight: 700 !important;
  text-transform: uppercase; letter-spacing: 0.5px; color: #4b5c78 !important;
}
.pm-item-img {
  width: 48px; height: 48px; object-fit: cover; border-radius: 6px;
  border: 1px solid #e2e8f0;
}
.pm-item-img--placeholder {
  background: #f1f5f9;
  display: flex; align-items: center; justify-content: center;
  color: #cbd5e1; font-size: 20px;
}
.pm-item-name { font-size: 13px; font-weight: 600; color: #1e3a5f; }
.pm-item-variant { font-size: 11px; color: #64748b; margin-top: 2px; }
.pm-item-sku { font-size: 11px; color: #94a3b8; }
.pm-qty {
  background: #eff6ff; color: #1d4ed8;
  padding: 2px 8px; border-radius: 999px;
  font-size: 12px; font-weight: 700;
}
.pm-discount { color: #dc2626; font-weight: 600; }
.pm-muted { color: #94a3b8; }
.pm-line-total { font-weight: 700; color: #1e3a5f; }

/* Summary */
.pm-summary__rows { display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px; }
.pm-summary__row {
  display: flex; justify-content: space-between;
  font-size: 13px; color: #475569;
}
.pm-summary__total {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 10px;
  border-top: 2px solid #dde5f0;
  font-size: 15px; font-weight: 700; color: #1e3a5f;
}
.pm-summary__total-value { font-size: 18px; font-weight: 900; color: #1d4ed8; }

.pm-notes { font-size: 13px; color: #475569; line-height: 1.6; }

/* Dialog Footer */
.pm-dialog__footer {
  padding: 16px 28px !important;
  display: flex; justify-content: flex-end; gap: 10px;
}
.pm-btn-close {
  background: #f1f5f9 !important;
  border-color: #cbd5e1 !important;
  color: #475569 !important;
  font-weight: 600 !important;
}
.pm-btn-refund { font-weight: 700 !important; }
</style>