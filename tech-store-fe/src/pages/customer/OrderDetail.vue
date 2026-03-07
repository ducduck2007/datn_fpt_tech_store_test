<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Order</div>
          <div class="title">{{ detail?.orderNumber || `#${orderId}` }}</div>
          <div class="muted">
            <el-tag :type="statusType" size="large">{{ detail?.status }}</el-tag>
            <el-tag class="ms-2" :type="paymentStatusType" size="large">
              Payment: {{ detail?.paymentStatus }}
            </el-tag>
            <el-tag v-if="isReturned(detail?.status)" type="warning" size="large" class="ms-2">
              Returned
            </el-tag>
          </div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="$router.push('/orders/new')">Create another</el-button>
          <el-button @click="reload" :loading="loading">Reload</el-button>

          <el-button
            v-if="detail?.status === 'PENDING' && detail?.paymentStatus === 'UNPAID'"
            type="primary"
            @click="openPaymentDialog"
          >
            <el-icon class="me-1"><CreditCard /></el-icon>
            Thanh toán
          </el-button>

          <el-button
            v-if="detail?.status === 'PENDING' || detail?.status === 'PAID' || detail?.status === 'SHIPPING'"
            type="danger"
            @click="showCancelDialog = true"
          >
            <el-icon class="me-1"><Close /></el-icon>
            Hủy đơn
          </el-button>

          <el-button
            v-if="detail?.status === 'DELIVERED' && !isReturned(detail?.status)"
            type="warning"
            @click="showReturnDialog = true"
          >
            <el-icon class="me-1"><RefreshLeft /></el-icon>
            Yêu cầu trả hàng
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- Order Info -->
      <el-skeleton v-if="loading" :rows="6" animated />
      <div v-else-if="detail" class="row g-3">
        <!-- Customer & Payment Info -->
        <div class="col-12 col-md-6">
          <div class="info-box">
            <h5>Thông tin khách hàng</h5>
            <p><strong>Tên:</strong> {{ detail.customerName }}</p>
            <p><strong>ID:</strong> {{ detail.customerId }}</p>
          </div>
        </div>

        <div class="col-12 col-md-6">
          <div class="info-box">
            <h5>Thanh toán & Giao hàng</h5>
            <p><strong>Phương thức:</strong> {{ detail.paymentMethod }}</p>
            <p><strong>Kênh:</strong> {{ detail.channel }}</p>
            <p><strong>Ghi chú:</strong> {{ detail.notes || "—" }}</p>
          </div>
        </div>

        <div v-if="hasDiscountInfo" class="col-12">
          <el-alert
            :title="`💰 Ưu đãi áp dụng: Tổng giảm ${formatMoney(detail.discountTotal)}`"
            type="success"
            :closable="false"
            show-icon
          >
            <div class="discount-details">
              <div v-if="detail.vipDiscountRate > 0" class="discount-item">
                <span class="discount-label">🏆 VIP {{ detail.vipDiscountRate }}%:</span>
                <span class="discount-value">-{{ formatMoney(detail.vipDiscount) }}</span>
              </div>
              <div v-if="detail.spinDiscountRate > 0" class="discount-item">
                <span class="discount-label">🎡 Vòng quay {{ detail.spinDiscountRate }}%:</span>
                <span class="discount-value">-{{ formatMoney(detail.spinDiscount) }}</span>
              </div>
              <!-- ✅ THÊM MỚI: Promotion code -->
              <div v-if="detail.promoCode" class="discount-item">
                <span class="discount-label">
                  🎁 Mã KM
                  <el-tag size="small" type="warning" class="ms-1">{{ detail.promoCode }}</el-tag>:
                </span>
                <span class="discount-value">-{{ formatMoney(detail.promoDiscount) }}</span>
              </div>
              <!-- ✅ THÊM MỚI: Combo info -->
              <div v-if="detail.comboInfo" class="discount-item combo-item">
                <span class="discount-label">🎀 Combo:</span>
                <span class="discount-value combo-value">{{ detail.comboInfo }}</span>
              </div>
            </div>
          </el-alert>
        </div>

        <!-- Items Table -->
        <div class="col-12">
          <h5 class="mb-2">Chi tiết sản phẩm</h5>
          <el-table :data="detail.items" border>
            <el-table-column label="Sản phẩm" min-width="200">
              <template #default="{ row }">
                <div class="fw-bold">{{ row.productName }}</div>
                <div class="muted small">{{ row.variantName }}</div>
                <div class="muted small">SKU: {{ row.sku }}</div>
              </template>
            </el-table-column>
            <el-table-column label="Số lượng" width="100" align="center">
              <template #default="{ row }">{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column label="Đơn giá" width="150" align="right">
              <template #default="{ row }">{{ formatMoney(row.price) }}</template>
            </el-table-column>
            <el-table-column label="Giảm giá" width="150" align="right">
              <template #default="{ row }">
                <span v-if="row.discount > 0" class="text-danger">-{{ formatMoney(row.discount) }}</span>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="Tổng" width="150" align="right">
              <template #default="{ row }">
                <strong>{{ formatMoney(row.lineTotal) }}</strong>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Totals -->
        <div class="col-12">
          <div class="totals-box">
            <div class="d-flex justify-content-between">
              <span>Tạm tính:</span>
              <strong>{{ formatMoney(detail.subtotal) }}</strong>
            </div>
            <div class="d-flex justify-content-between text-success">
              <span>Giảm giá:</span>
              <strong>- {{ formatMoney(detail.discountTotal) }}</strong>
            </div>

            <!-- VIP discount thực tế -->
            <div v-if="detail.vipDiscountRate > 0" class="d-flex justify-content-between ps-3 small">
              <span class="text-muted">└ VIP {{ detail.vipDiscountRate }}%:</span>
              <span class="text-muted">-{{ formatMoney(detail.vipDiscount) }}</span>
            </div>

            <!-- Spin discount thực tế (đã áp vào đơn) -->
            <div v-if="detail.spinDiscountRate > 0" class="d-flex justify-content-between ps-3 small">
              <span class="text-muted">└ 🎡 Spin {{ detail.spinDiscountRate }}%:</span>
              <span class="text-muted">-{{ formatMoney(detail.spinDiscount) }}</span>
            </div>

            <!-- Spin ước tính - CHỈ hiện khi đơn CHƯA có spin discount được áp -->
            <div
              v-if="spinStatus.hasActiveBonus && estimatedSpinDiscount > 0 && !(detail.spinDiscountRate > 0)"
              class="d-flex justify-content-between ps-3 small spin-estimated-row"
            >
              <span>└ 🎡 Spin {{ spinStatus.bonusRate }}% <em>(ước tính)</em>:</span>
              <span>-{{ formatMoney(estimatedSpinDiscount) }}</span>
            </div>

            <div class="d-flex justify-content-between">
              <span>Phí ship:</span>
              <strong>{{ formatMoney(detail.shippingFee) }}</strong>
            </div>

            <el-divider />

          <div class="d-flex justify-content-between fs-5 align-items-start">
  <span><strong>Tổng cộng:</strong></span>

  <!-- Khi có spin bonus chưa áp: gạch giá gốc, hiện giá ước tính -->
  <div
    v-if="spinStatus.hasActiveBonus && estimatedSpinDiscount > 0 && !(detail.spinDiscountRate > 0)"
    class="text-end"
  >
    <div class="text-muted text-decoration-line-through" style="font-size: 14px;">
      {{ formatMoney(detail.totalAmount) }}
    </div>
    <strong class="text-primary" style="font-size: 20px;">
      {{ formatMoney(detail.totalAmount - estimatedSpinDiscount) }}
    </strong>
    <div class="estimated-total-note mt-1">
      🎡 Ước tính sau giảm Spin {{ spinStatus.bonusRate }}% · Sẽ tính chính xác khi thanh toán
    </div>
  </div>

  <!-- Bình thường -->
  <strong v-else class="text-primary">{{ formatMoney(detail.totalAmount) }}</strong>
</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- ================================================================ -->
    <!-- Payment Dialog                                                    -->
    <!-- ================================================================ -->
    <el-dialog
      v-model="showPaymentDialog"
      title="💳 Thanh toán đơn hàng"
      width="520px"
      :close-on-click-modal="false"
    >
      <!-- Loading skeleton khi đang fetch spin -->
      <div v-if="spinStatus.loading" class="spin-loading-wrap mb-3">
        <div class="spin-loading-inner">
          <span class="spin-loading-icon">🎡</span>
          <span class="spin-loading-text">Đang kiểm tra ưu đãi vòng quay...</span>
        </div>
      </div>

      <!-- Spin Bonus Banner -->
      <transition name="spin-bonus-fade">
        <div v-if="!spinStatus.loading && spinStatus.hasActiveBonus" class="spin-bonus-banner mb-3">
          <div class="spin-bonus-inner">
            <div class="spin-bonus-left">
              <div class="spin-bonus-icon">🎡</div>
              <div>
                <div class="spin-bonus-title">Mã giảm giá vòng quay đã được áp dụng!</div>
                <div class="spin-bonus-sub">
                  Giảm thêm
                  <strong class="spin-bonus-rate">{{ spinStatus.bonusRate }}%</strong>
                  <span v-if="spinStatus.bonusExpiresAt">
                    · Hết hạn {{ formatExpiry(spinStatus.bonusExpiresAt) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="spin-bonus-badge">-{{ spinStatus.bonusRate }}%</div>
          </div>

          <div class="spin-bonus-breakdown">
            <div class="breakdown-row">
              <span class="breakdown-label">Tạm tính</span>
              <span>{{ formatMoney(detail?.subtotal) }}</span>
            </div>
            <div v-if="detail?.vipDiscount > 0" class="breakdown-row">
              <span class="breakdown-label">🏆 Giảm VIP</span>
              <span class="text-success">-{{ formatMoney(detail.vipDiscount) }}</span>
            </div>
            <div class="breakdown-row">
              <span class="breakdown-label">🎡 Giảm Spin ({{ spinStatus.bonusRate }}%)</span>
              <span class="text-success">-{{ formatMoney(estimatedSpinDiscount) }}</span>
            </div>
            <div class="breakdown-row breakdown-total">
              <span class="breakdown-label"><strong>Tổng thanh toán</strong></span>
              <span><strong>{{ formatMoney((detail?.totalAmount || 0) - estimatedSpinDiscount + (detail?.spinDiscount || 0)) }}</strong></span>
            </div>
          </div>
        </div>
      </transition>

      <!-- Không có spin bonus -->
      <transition name="spin-bonus-fade">
        <div
          v-if="!spinStatus.loading && !spinStatus.hasActiveBonus && spinStatus.checked"
          class="spin-no-bonus mb-3"
        >
          <span class="spin-no-bonus-icon">🎡</span>
          <span class="spin-no-bonus-text">Khách hàng chưa có ưu đãi vòng quay tuần này</span>
        </div>
      </transition>

      <!-- Thông tin thanh toán -->
      <el-alert title="Thông tin thanh toán" type="info" :closable="false" class="mb-3">
        <p>Sau khi thanh toán thành công:</p>
        <ul class="mb-0">
          <li>✅ Đơn hàng sẽ chuyển sang trạng thái <strong>PAID</strong></li>
          <li>✅ Xuất kho tự động</li>
          <li>✅ <strong class="text-success">Cộng điểm loyalty</strong> cho khách hàng</li>
        </ul>
      </el-alert>

      <el-form :model="paymentForm" label-position="top">
        <el-form-item label="Số tiền thanh toán">
          <el-input :value="formatMoney(detail?.totalAmount)" disabled size="large" />
        </el-form-item>

        <el-form-item label="Phương thức thanh toán" required>
          <el-select v-model="paymentForm.method" placeholder="Chọn phương thức" class="w-100" size="large">
            <el-option label="💵 Tiền mặt" value="CASH" />
            <el-option label="🏦 Chuyển khoản" value="BANK_TRANSFER" />
            <el-option label="💳 Thẻ tín dụng" value="CREDIT_CARD" />
            <el-option label="📱 Ví điện tử" value="E_WALLET" />
          </el-select>
        </el-form-item>

        <el-form-item label="Mã giao dịch (tùy chọn)">
          <el-input v-model="paymentForm.transactionRef" placeholder="Ví dụ: TXN-123456" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showPaymentDialog = false" size="large">Hủy</el-button>
        <el-button
          type="primary"
          @click="confirmPayment"
          :loading="paymentLoading"
          :disabled="!paymentForm.method"
          size="large"
        >
          Xác nhận thanh toán
        </el-button>
      </template>
    </el-dialog>

    <!-- Cancel Dialog -->
    <el-dialog v-model="showCancelDialog" title="❌ Hủy đơn hàng" width="550px">
      <el-alert
        :title="getCancelWarningTitle()"
        :type="detail?.paymentStatus === 'PAID' ? 'error' : 'warning'"
        show-icon
        :closable="false"
        class="mb-3"
      >
        <div v-html="getCancelWarningMessage()"></div>
      </el-alert>
      <el-form>
        <el-form-item label="Lý do hủy">
          <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="4"
            placeholder="Vui lòng nhập lý do hủy đơn (không bắt buộc)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCancelDialog = false">Đóng</el-button>
        <el-button type="danger" @click="confirmCancel" :loading="cancelLoading">Xác nhận hủy</el-button>
      </template>
    </el-dialog>

    <!-- Return Dialog -->
    <el-dialog v-model="showReturnDialog" title="🔄 Yêu cầu trả hàng" width="600px">
      <el-form :model="returnForm" label-position="top">
        <el-form-item label="Chọn sản phẩm">
          <el-select v-model="returnForm.orderItemId" placeholder="Chọn sản phẩm muốn trả" class="w-100">
            <el-option
              v-for="item in detail?.items"
              :key="item.productId"
              :label="`${item.productName} - ${item.variantName}`"
              :value="item.productId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Số lượng">
          <el-input-number v-model="returnForm.quantity" :min="1" :max="getMaxReturnQuantity()" class="w-100" />
        </el-form-item>
        <el-form-item label="Lý do trả hàng">
          <el-input v-model="returnForm.reason" type="textarea" :rows="3" placeholder="Nhập lý do trả hàng..." />
        </el-form-item>
        <el-form-item label="Số tiền hoàn">
          <el-input v-model="returnForm.refundAmount" disabled>
            <template #prefix>₫</template>
          </el-input>
        </el-form-item>
        <el-alert title="⚠️ Lưu ý" type="info" show-icon :closable="false">
          Điểm loyalty đã được cộng sẽ bị trừ lại khi trả hàng được duyệt.
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showReturnDialog = false">Đóng</el-button>
        <el-button type="primary" @click="submitReturn">Gửi yêu cầu</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi }    from "../../api/orders.api";
import { returnsApi }   from "../../api/returns.api";
import { paymentsApi }  from "../../api/payments";
import { spinWheelApi } from "../../api/spinWheel.api";
import { useAuthStore } from "../../stores/auth";
import { toast }        from "../../ui/toast";
import { Close, CreditCard, RefreshLeft } from "@element-plus/icons-vue";

const route  = useRoute();
const router = useRouter();
const auth   = useAuthStore();

const loading        = ref(false);
const cancelLoading  = ref(false);
const paymentLoading = ref(false);
const detail         = ref(null);
const orderId        = computed(() => route.params.orderId);

const showCancelDialog  = ref(false);
const cancelReason      = ref("");
const showReturnDialog  = ref(false);
const showPaymentDialog = ref(false);

const spinStatus = reactive({
  loading:        false,
  checked:        false,
  hasActiveBonus: false,
  bonusRate:      0,
  bonusExpiresAt: null,
});

const returnForm  = reactive({ orderItemId: null, quantity: 1, reason: "", refundAmount: 0 });
const paymentForm = reactive({ method: "CASH", transactionRef: "" });

const statusType = computed(() => {
  const s = detail.value?.status;
  if (s === "DELIVERED") return "success";
  if (s === "SHIPPING")  return "warning";
  if (s === "CANCELLED") return "danger";
  if (s === "PAID")      return "success";
  return "info";
});

const paymentStatusType = computed(() => {
  const ps = detail.value?.paymentStatus;
  if (ps === "PAID")    return "success";
  if (ps === "PARTIAL") return "warning";
  return "info";
});

const hasDiscountInfo = computed(() =>
  detail.value && (
    (detail.value.vipDiscountRate  && detail.value.vipDiscountRate  > 0) ||
    (detail.value.spinDiscountRate && detail.value.spinDiscountRate > 0) ||
    !!detail.value.promoCode
  )
);

const spinAlreadyApplied = computed(() =>
  detail.value?.spinDiscountRate > 0
);

const estimatedSpinDiscount = computed(() => {
  if (!detail.value?.subtotal || !spinStatus.bonusRate) return 0;
  if (spinAlreadyApplied.value) return 0;
  return Math.round(detail.value.subtotal * spinStatus.bonusRate / 100);
});

watch(() => returnForm.orderItemId, (newItemId) => {
  if (!newItemId || !detail.value?.items) return;
  const item = detail.value.items.find((i) => i.productId === newItemId);
  if (item) { returnForm.quantity = 1; returnForm.refundAmount = item.price; }
});

watch(() => returnForm.quantity, (newQty) => {
  if (!returnForm.orderItemId || !detail.value?.items) return;
  const item = detail.value.items.find((i) => i.productId === returnForm.orderItemId);
  if (item) returnForm.refundAmount = item.price * newQty;
});

function formatMoney(val) {
  if (val === null || val === undefined) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(val);
}

function formatExpiry(dateStr) {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleString("vi-VN", {
    day: "2-digit", month: "2-digit", year: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
}

async function reload() {
  loading.value = true;
  try {
    const res = await ordersApi.getById(orderId.value);
    detail.value = res?.data?.data || res?.data;
  } catch (e) {
    toast("Không thể tải chi tiết đơn hàng", "error");
  } finally {
    loading.value = false;
  }
}

async function fetchSpinStatus() {
  const customerId = detail.value?.customerId;
  if (!customerId) return;

  spinStatus.loading        = true;
  spinStatus.checked        = false;
  spinStatus.hasActiveBonus = false;
  spinStatus.bonusRate      = 0;
  spinStatus.bonusExpiresAt = null;

  try {
    const res  = await spinWheelApi.getStatus(customerId);
    const data = res?.data?.data ?? res?.data ?? res;

    const bonus = Number(data?.currentBonus ?? 0);

    if (bonus > 0) {
      spinStatus.hasActiveBonus = true;
      spinStatus.bonusRate      = bonus;
      spinStatus.bonusExpiresAt = data?.bonusExpiresAt ?? null;
    }
  } catch (err) {
    console.warn("🎡 [SpinWheel] error:", err?.response?.status, err?.message);
  } finally {
    spinStatus.loading = false;
    spinStatus.checked = true;
  }
}

async function openPaymentDialog() {
  showPaymentDialog.value = true;
  await fetchSpinStatus();
}

function getCancelWarningTitle() {
  return detail.value?.paymentStatus === "PAID"
    ? "⚠️ Cảnh báo: Hủy đơn đã thanh toán"
    : "Xác nhận hủy đơn";
}

function getCancelWarningMessage() {
  if (detail.value?.paymentStatus === "PAID") {
    const pts = Math.floor((detail.value.totalAmount || 0) / 10000);
    return `
      <p><strong>Đơn hàng đã thanh toán. Nếu hủy:</strong></p>
      <ul>
        <li>❌ Điểm loyalty đã cộng sẽ bị trừ lại: <strong class="text-danger">${pts} điểm</strong></li>
        <li>💰 Số tiền sẽ được hoàn trả</li>
        <li>📦 Sản phẩm sẽ được nhập lại kho</li>
      </ul>
      <p class="mb-0 mt-2"><em>Lưu ý: Không có phí phạt khi hủy đơn.</em></p>
    `;
  }
  return `<p><strong>Xác nhận hủy đơn hàng này?</strong></p><p class="mb-0">Đơn hàng chưa thanh toán sẽ được hủy miễn phí.</p>`;
}

function getMaxReturnQuantity() {
  if (!returnForm.orderItemId || !detail.value?.items) return 1;
  return detail.value.items.find((i) => i.productId === returnForm.orderItemId)?.quantity || 1;
}

async function confirmPayment() {
  if (!paymentForm.method) return;
  paymentLoading.value = true;
  try {
    await paymentsApi.create({
      orderId:        Number(orderId.value),
      method:         paymentForm.method,
      transactionRef: paymentForm.transactionRef || `TXN-${Date.now()}`,
    });
    toast("✅ Thanh toán thành công!", "success");
    showPaymentDialog.value = false;
    await reload();
  } catch (e) {
    toast(e?.response?.data?.message || "Lỗi thanh toán", "error");
  } finally {
    paymentLoading.value = false;
  }
}
async function confirmCancel() {
  cancelLoading.value = true;
  try {
    await ordersApi.cancel(orderId.value, cancelReason.value);
    toast("✅ Đã hủy đơn hàng thành công.", "success");
    showCancelDialog.value = false;
    await reload();
  } catch (e) {
    toast("Lỗi khi hủy đơn", "error");
  } finally {
    cancelLoading.value = false;
  }
}

async function submitReturn() {
  if (!returnForm.orderItemId || !returnForm.reason) return;
  try {
    await returnsApi.create({ orderId: Number(orderId.value), ...returnForm });
    toast("✅ Đã gửi yêu cầu trả hàng", "success");
    showReturnDialog.value = false;
  } catch (e) {
    toast("Lỗi khi gửi yêu cầu", "error");
  }
}

function isReturned(status) {
  return ["PARTIALLY_RETURNED", "RETURNED"].includes(status);
}

onMounted(() => reload());
</script>

<style scoped>
.info-box { padding: 16px; background: #f5f7fa; border-radius: 8px; }
.info-box h5 { margin-bottom: 12px; color: #2c3e50; }
.info-box p  { margin: 8px 0; }

.totals-box { padding: 20px; background: #f8f9fa; border-radius: 8px; }
.totals-box > div { padding: 8px 0; }

.kicker { font-size: 12px; opacity: 0.75; font-weight: 900; text-transform: uppercase; }
.title  { font-weight: 900; font-size: 18px; }
.muted  { color: rgba(15, 23, 42, 0.62); font-size: 13px; }
.small  { font-size: 12px; }

.discount-details { display: flex; flex-direction: column; gap: 8px; margin-top: 8px; }
.discount-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: rgba(255,255,255,0.6);
  border-radius: 6px;
}
.discount-label { font-weight: 600; color: #2c3e50; }
.discount-value { font-weight: 700; color: #67c23a; }

/* ── Spin Loading ─────────────────────────────────────────── */
.spin-loading-wrap {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 12px 16px;
}
.spin-loading-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #888;
  font-size: 14px;
}
.spin-loading-icon {
  font-size: 20px;
  animation: spin-rotate 1.2s linear infinite;
}
@keyframes spin-rotate {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* ── No Bonus Notice ──────────────────────────────────────── */
.spin-no-bonus {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
  font-size: 13px;
  color: #909399;
}
.spin-no-bonus-icon { font-size: 18px; }

/* ── Spin Bonus Banner ────────────────────────────────────── */
.spin-bonus-banner {
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #f0a500;
  background: linear-gradient(135deg, #fffbec 0%, #fff8db 100%);
  box-shadow: 0 4px 16px rgba(240, 165, 0, 0.18);
}
.spin-bonus-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 10px;
  gap: 12px;
}
.spin-bonus-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.spin-bonus-icon {
  font-size: 28px;
  line-height: 1;
  animation: spin-pulse 2s ease-in-out infinite;
}
@keyframes spin-pulse {
  0%, 100% { transform: rotate(0deg)   scale(1); }
  50%       { transform: rotate(15deg) scale(1.1); }
}
.spin-bonus-title { font-weight: 700; font-size: 14px; color: #7c5200; }
.spin-bonus-sub   { font-size: 12px; color: #9d6f0a; margin-top: 2px; }
.spin-bonus-rate  { color: #e67e00; font-size: 15px; }
.spin-bonus-badge {
  background: linear-gradient(135deg, #f0a500, #e67e00);
  color: white;
  font-weight: 900;
  font-size: 18px;
  padding: 6px 14px;
  border-radius: 20px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(230, 126, 0, 0.4);
}

.spin-bonus-breakdown {
  border-top: 1px solid #f0d58a;
  padding: 10px 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.breakdown-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 2px 0;
  color: #5a3d00;
}
.breakdown-label { opacity: 0.85; }
.breakdown-total {
  border-top: 1px dashed #f0d58a;
  margin-top: 4px;
  padding-top: 6px;
  font-size: 14px;
  color: #3d2800;
}

/* ── Spin Estimated Row (totals box) ─────────────────────── */
.spin-estimated-row {
  color: #e67e00;
  font-style: italic;
}
.spin-estimated-row span { opacity: 1 !important; color: #e67e00; }

.estimated-total-row {
  margin-top: 8px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #fffbec, #fff3cd);
  border: 1.5px dashed #f0a500;
  border-radius: 8px;
}
.estimated-total-label {
  font-size: 14px;
  color: #7c5200;
  font-weight: 600;
}
.estimated-total-value {
  font-size: 15px;
  color: #e67e00;
}
.estimated-total-note {
  font-size: 11px;
  color: #9d6f0a;
  margin-top: 4px;
  font-style: italic;
}

/* ── Transition ───────────────────────────────────────────── */
.spin-bonus-fade-enter-active,
.spin-bonus-fade-leave-active { transition: all 0.35s ease; }
.spin-bonus-fade-enter-from,
.spin-bonus-fade-leave-to     { opacity: 0; transform: translateY(-8px); }
</style>