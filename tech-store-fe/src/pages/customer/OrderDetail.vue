<template>
  <div class="container-xl">
    <el-card shadow="never">
      <!-- Header -->
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Order</div>
          <div class="title">{{ detail?.orderNumber || `#${orderId}` }}</div>
          <div class="muted">
            <el-tag :type="statusType" size="large">{{ detail?.status }}</el-tag>
            <el-tag class="ms-2" :type="paymentStatusType" size="large">
              Payment: {{ detail?.paymentStatus }}
            </el-tag>
            <el-tag
              v-if="isReturned(detail?.status)"
              type="warning"
              size="large"
              class="ms-2"
            >
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
            @click="showPaymentDialog = true"
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

        <!-- Discount Breakdown Alert -->
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
              <template #default="{ row }">
                {{ row.quantity }}
              </template>
            </el-table-column>

            <el-table-column label="Đơn giá" width="150" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.price) }}
              </template>
            </el-table-column>

            <el-table-column label="Giảm giá" width="150" align="right">
              <template #default="{ row }">
                <span v-if="row.discount > 0" class="text-danger">
                  -{{ formatMoney(row.discount) }}
                </span>
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
            <div v-if="detail.vipDiscountRate > 0" class="d-flex justify-content-between ps-3 small">
              <span class="text-muted">└ VIP {{ detail.vipDiscountRate }}%:</span>
              <span class="text-muted">-{{ formatMoney(detail.vipDiscount) }}</span>
            </div>
            <div v-if="detail.spinDiscountRate > 0" class="d-flex justify-content-between ps-3 small">
              <span class="text-muted">└ Spin {{ detail.spinDiscountRate }}%:</span>
              <span class="text-muted">-{{ formatMoney(detail.spinDiscount) }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span>Phí ship:</span>
              <strong>{{ formatMoney(detail.shippingFee) }}</strong>
            </div>
            <el-divider />
            <div class="d-flex justify-content-between fs-5">
              <span><strong>Tổng cộng:</strong></span>
              <strong class="text-primary">{{ formatMoney(detail.totalAmount) }}</strong>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Payment Dialog -->
    <el-dialog
      v-model="showPaymentDialog"
      title="💳 Thanh toán đơn hàng"
      width="500px"
    >
      <el-alert
        title="Thông tin thanh toán"
        type="info"
        :closable="false"
        class="mb-3"
      >
        <p>Sau khi thanh toán thành công:</p>
        <ul class="mb-0">
          <li>✅ Đơn hàng sẽ chuyển sang trạng thái <strong>PAID</strong></li>
          <li>✅ Xuất kho tự động</li>
          <li>✅ <strong class="text-success">Cộng điểm loyalty</strong> cho bạn</li>
        </ul>
      </el-alert>

      <el-form :model="paymentForm" label-position="top">
        <el-form-item label="Số tiền thanh toán">
          <el-input
            :value="formatMoney(detail?.totalAmount)"
            disabled
            size="large"
          />
        </el-form-item>

        <el-form-item label="Phương thức thanh toán" required>
          <el-select
            v-model="paymentForm.method"
            placeholder="Chọn phương thức"
            class="w-100"
            size="large"
          >
            <el-option label="💵 Tiền mặt" value="CASH" />
            <el-option label="🏦 Chuyển khoản" value="BANK_TRANSFER" />
            <el-option label="💳 Thẻ tín dụng" value="CREDIT_CARD" />
            <el-option label="📱 Ví điện tử" value="E_WALLET" />
          </el-select>
        </el-form-item>

        <el-form-item label="Mã giao dịch (tùy chọn)">
          <el-input
            v-model="paymentForm.transactionRef"
            placeholder="Ví dụ: TXN-123456"
          />
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
        <el-button
          type="danger"
          @click="confirmCancel"
          :loading="cancelLoading"
        >
          Xác nhận hủy
        </el-button>
      </template>
    </el-dialog>

    <!-- Return Dialog -->
    <el-dialog
      v-model="showReturnDialog"
      title="🔄 Yêu cầu trả hàng"
      width="600px"
    >
      <el-form :model="returnForm" label-position="top">
        <el-form-item label="Chọn sản phẩm">
          <el-select
            v-model="returnForm.orderItemId"
            placeholder="Chọn sản phẩm muốn trả"
            class="w-100"
          >
            <el-option
              v-for="item in detail?.items"
              :key="item.productId"
              :label="`${item.productName} - ${item.variantName}`"
              :value="item.productId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Số lượng">
          <el-input-number
            v-model="returnForm.quantity"
            :min="1"
            :max="getMaxReturnQuantity()"
            class="w-100"
          />
        </el-form-item>

        <el-form-item label="Lý do trả hàng">
          <el-input
            v-model="returnForm.reason"
            type="textarea"
            :rows="3"
            placeholder="Nhập lý do trả hàng..."
          />
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
import { ordersApi } from "../../api/orders.api";
import { returnsApi } from "../../api/returns.api";
import { paymentsApi } from "../../api/payments";
import { useAuthStore } from "../../stores/auth";
import { toast } from "../../ui/toast";
import { Close, CreditCard, RefreshLeft } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const loading = ref(false);
const cancelLoading = ref(false);
const paymentLoading = ref(false);
const detail = ref(null);
const orderId = computed(() => route.params.orderId);

const showCancelDialog = ref(false);
const cancelReason = ref("");
const showReturnDialog = ref(false);
const showPaymentDialog = ref(false);

const returnForm = reactive({
  orderItemId: null,
  quantity: 1,
  reason: "",
  refundAmount: 0,
});

const paymentForm = reactive({
  method: "CASH",
  transactionRef: "",
});

const statusType = computed(() => {
  const s = detail.value?.status;
  if (s === "DELIVERED") return "success";
  if (s === "SHIPPING") return "warning";
  if (s === "CANCELLED") return "danger";
  if (s === "PAID") return "success";
  return "info";
});

const paymentStatusType = computed(() => {
  const ps = detail.value?.paymentStatus;
  if (ps === "PAID") return "success";
  if (ps === "PARTIAL") return "warning";
  return "info";
});

const hasDiscountInfo = computed(() => {
  return detail.value && (
    (detail.value.vipDiscountRate && detail.value.vipDiscountRate > 0) ||
    (detail.value.spinDiscountRate && detail.value.spinDiscountRate > 0)
  );
});

watch(
  () => returnForm.orderItemId,
  (newItemId) => {
    if (!newItemId || !detail.value?.items) return;
    const item = detail.value.items.find((i) => i.productId === newItemId);
    if (item) {
      returnForm.quantity = 1;
      returnForm.refundAmount = item.price;
    }
  }
);

watch(
  () => returnForm.quantity,
  (newQty) => {
    if (!returnForm.orderItemId || !detail.value?.items) return;
    const item = detail.value.items.find(
      (i) => i.productId === returnForm.orderItemId
    );
    if (item) {
      returnForm.refundAmount = item.price * newQty;
    }
  }
);

function formatMoney(val) {
  if (!val) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(val);
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

function getCancelWarningTitle() {
  if (detail.value?.paymentStatus === "PAID") {
    return "⚠️ Cảnh báo: Hủy đơn đã thanh toán";
  }
  return "Xác nhận hủy đơn";
}

function getCancelWarningMessage() {
  if (detail.value?.paymentStatus === "PAID") {
    const totalAmount = detail.value.totalAmount || 0;
    const penaltyAmount = totalAmount * 0.1;
    const penaltyPoints = Math.floor(penaltyAmount / 10000);
    return `
      <p><strong>Đơn hàng đã thanh toán. Nếu hủy sẽ bị phạt:</strong></p>
      <ul>
        <li>❌ Trừ điểm loyalty: <strong class="text-danger">${Math.floor(totalAmount / 10000)} điểm</strong></li>
        <li>⚠️ Phạt 10% giá trị: <strong class="text-danger">${penaltyPoints} điểm</strong></li>
      </ul>
    `;
  }
  return "<p>Bạn có chắc chắn muốn hủy đơn hàng này không?</p>";
}

function getMaxReturnQuantity() {
  if (!returnForm.orderItemId || !detail.value?.items) return 1;
  const item = detail.value.items.find(
    (i) => i.productId === returnForm.orderItemId
  );
  return item?.quantity || 1;
}

async function confirmPayment() {
  if (!paymentForm.method) return;
  paymentLoading.value = true;
  try {
    const payload = {
      orderId: Number(orderId.value),
      method: paymentForm.method,
      transactionRef: paymentForm.transactionRef || `TXN-${Date.now()}`,
    };
    await paymentsApi.create(payload);
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
    await returnsApi.create({
      orderId: Number(orderId.value),
      ...returnForm,
    });
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
.info-box {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.info-box h5 {
  margin-bottom: 12px;
  color: #2c3e50;
}

.info-box p {
  margin: 8px 0;
}

.totals-box {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.totals-box > div {
  padding: 8px 0;
}

.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
}

.title {
  font-weight: 900;
  font-size: 18px;
}

.muted {
  color: rgba(15, 23, 42, 0.62);
  font-size: 13px;
}

.small {
  font-size: 12px;
}

/* Discount Details */
.discount-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.discount-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 6px;
}

.discount-label {
  font-weight: 600;
  color: #2c3e50;
}

.discount-value {
  font-weight: 700;
  color: #67c23a;
}
</style>