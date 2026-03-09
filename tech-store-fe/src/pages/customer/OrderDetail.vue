<template>
  <div class="container-xl order-layout">
    <div class="order-left">
      <el-card shadow="never" class="order-card">
        <!-- HEADER -->
        <div class="order-header">
          <div>
            <div class="kicker">Order</div>
            <div class="title">{{ detail?.orderNumber || `#${orderId}` }}</div>

            <div class="muted mt-1">
              <el-tag :type="statusType" size="large">
                {{ detail?.status }}
              </el-tag>

              <!-- <el-tag class="ms-2" :type="paymentStatusType" size="large">
                Payment: {{ detail?.paymentStatus }}
              </el-tag> -->

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

          <div class="order-actions">
            <el-button @click="reload" :loading="loading"> Reload </el-button>

            <el-button
              v-if="
                detail?.status === 'PENDING' &&
                detail?.paymentStatus === 'UNPAID' &&
                detail?.paymentMethod !== 'CASH'
              "
              type="primary"
              @click="openPaymentDialog"
            >
              <el-icon class="me-1"><CreditCard /></el-icon>
              Thanh toán
            </el-button>

            <el-button
              v-if="
                detail?.status === 'PENDING' ||
                detail?.status === 'PAID' ||
                detail?.status === 'SHIPPING'
              "
              type="danger"
              @click="showCancelDialog = true"
            >
              <el-icon class="me-1"><Close /></el-icon>
              Hủy đơn
            </el-button>

            <el-button
              v-if="
                detail?.status === 'DELIVERED' && !isReturned(detail?.status)
              "
              type="warning"
              @click="showReturnDialog = true"
            >
              <el-icon class="me-1"><RefreshLeft /></el-icon>
              Yêu cầu trả hàng
            </el-button>
          </div>
        </div>

        <el-divider />

        <!-- ORDER TRACKING -->
        <div v-if="detail" class="order-timeline">
          <!-- ① Đặt hàng ─────────────────────────── -->
          <div class="timeline-step">
            <div
              class="timeline-dot"
              :class="{
                active: [
                  'PENDING',
                  'PAID',
                  'PROCESSING',
                  'SHIPPING',
                  'DELIVERED',
                ].includes(detail.status),
              }"
            ></div>
            <div class="timeline-label">Đặt hàng</div>
          </div>

          <!-- ② Thanh toán (ẩn khi CASH) ─────────── -->
          <template v-if="detail.paymentMethod !== 'CASH'">
            <div
              class="timeline-line"
              :class="{
                'line-active': [
                  'PAID',
                  'PROCESSING',
                  'SHIPPING',
                  'DELIVERED',
                ].includes(detail.status),
              }"
            ></div>
            <div class="timeline-step">
              <div
                class="timeline-dot"
                :class="{
                  active: [
                    'PAID',
                    'PROCESSING',
                    'SHIPPING',
                    'DELIVERED',
                  ].includes(detail.status),
                }"
              ></div>
              <div class="timeline-label">Thanh toán</div>
            </div>
          </template>

          <!-- ③ Chuẩn bị hàng ─────────────────────── -->
          <div
            class="timeline-line"
            :class="{
              'line-active': ['PROCESSING', 'SHIPPING', 'DELIVERED'].includes(
                detail.status,
              ),
            }"
          ></div>
          <div class="timeline-step">
            <div
              class="timeline-dot"
              :class="{
                active: ['PROCESSING', 'SHIPPING', 'DELIVERED'].includes(
                  detail.status,
                ),
              }"
            ></div>
            <div class="timeline-label">Chuẩn bị hàng</div>
          </div>

          <!-- ④ Đang giao ─────────────────────────── -->
          <div
            class="timeline-line"
            :class="{
              'line-active': ['SHIPPING', 'DELIVERED'].includes(detail.status),
            }"
          ></div>
          <div class="timeline-step">
            <div
              class="timeline-dot"
              :class="{
                active: ['SHIPPING', 'DELIVERED'].includes(detail.status),
              }"
            ></div>
            <div class="timeline-label">Đang giao</div>
          </div>

          <!-- ⑤ Đã giao ──────────────────────────── -->
          <div
            class="timeline-line"
            :class="{ 'line-active': detail.status === 'DELIVERED' }"
          ></div>
          <div class="timeline-step">
            <div
              class="timeline-dot"
              :class="{ active: detail.status === 'DELIVERED' }"
            ></div>
            <div class="timeline-label">Đã giao</div>
          </div>
        </div>

        <!-- CONTENT -->
        <el-skeleton v-if="loading" :rows="6" animated />

        <div v-else-if="detail" class="row g-4 mt-2">
          <!-- CUSTOMER -->
          <div class="col-12 col-md-6">
            <div class="info-box">
              <h5>Thông tin khách hàng</h5>

              <p><strong>Tên:</strong> {{ detail.customerName }}</p>

              <p><strong>ID:</strong> {{ detail.customerId }}</p>
            </div>
          </div>

          <!-- PAYMENT -->
          <div class="col-12 col-md-6">
            <div class="info-box">
              <h5>Thanh toán & Giao hàng</h5>

              <p><strong>Phương thức:</strong> {{ detail.paymentMethod }}</p>

              <p><strong>Kênh:</strong> {{ detail.channel }}</p>

              <p><strong>Ghi chú:</strong> {{ detail.notes || "—" }}</p>
            </div>
          </div>

          <!-- ITEMS -->
          <div class="col-12">
            <h5 class="section-title">Chi tiết sản phẩm</h5>

            <el-table :data="detail.items" border class="order-table">
              <el-table-column label="Sản phẩm" min-width="240">
                <template #default="{ row }">
                  <div class="product-cell">
                    <div class="product-info">
                      <div class="product-name">
                        {{ row.productName }}
                      </div>

                      <div class="product-meta">
                        {{ row.variantName }}
                      </div>

                      <div class="product-meta">SKU: {{ row.sku }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="Số lượng" width="110" align="center">
                <template #default="{ row }">
                  {{ row.quantity }}
                </template>
              </el-table-column>

              <el-table-column label="Đơn giá" width="160" align="right">
                <template #default="{ row }">
                  {{ formatMoney(row.price) }}
                </template>
              </el-table-column>

              <el-table-column label="Giảm giá" width="160" align="right">
                <template #default="{ row }">
                  <span v-if="row.discount > 0" class="discount-text">
                    -{{ formatMoney(row.discount) }}
                  </span>

                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>

              <el-table-column label="Tổng" width="170" align="right">
                <template #default="{ row }">
                  <strong>{{ formatMoney(row.lineTotal) }}</strong>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-card>
    </div>

    <!-- RIGHT SIDE -->
    <div class="order-right" v-if="detail">
      <div class="totals-box">
        <div class="total-row">
          <span>Tạm tính</span>
          <strong>{{ formatMoney(detail.subtotal) }}</strong>
        </div>

        <div class="total-row discount">
          <span>Giảm giá</span>
          <strong>- {{ formatMoney(detail.discountTotal) }}</strong>
        </div>

        <div class="total-row">
          <span>Phí ship</span>
          <strong>{{ formatMoney(detail.shippingFee) }}</strong>
        </div>

        <el-divider />

        <div class="total-final">
          <span>Tổng cộng</span>
          <strong class="total-price">
            {{ formatMoney(detail.totalAmount) }}
          </strong>
        </div>
      </div>
    </div>

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
          <span class="spin-loading-text"
            >Đang kiểm tra ưu đãi vòng quay...</span
          >
        </div>
      </div>

      <!-- Spin Bonus Banner -->
      <transition name="spin-bonus-fade">
        <div
          v-if="!spinStatus.loading && spinStatus.hasActiveBonus"
          class="spin-bonus-banner mb-3"
        >
          <div class="spin-bonus-inner">
            <div class="spin-bonus-left">
              <div class="spin-bonus-icon">🎡</div>
              <div>
                <div class="spin-bonus-title">
                  Mã giảm giá vòng quay đã được áp dụng!
                </div>
                <div class="spin-bonus-sub">
                  Giảm thêm
                  <strong class="spin-bonus-rate"
                    >{{ spinStatus.bonusRate }}%</strong
                  >
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
              <span class="text-success"
                >-{{ formatMoney(detail.vipDiscount) }}</span
              >
            </div>
            <div class="breakdown-row">
              <span class="breakdown-label"
                >🎡 Giảm Spin ({{ spinStatus.bonusRate }}%)</span
              >
              <span class="text-success"
                >-{{ formatMoney(estimatedSpinDiscount) }}</span
              >
            </div>
            <div class="breakdown-row breakdown-total">
              <span class="breakdown-label"
                ><strong>Tổng thanh toán</strong></span
              >
              <span
                ><strong>{{
                  formatMoney(
                    (detail?.totalAmount || 0) -
                      estimatedSpinDiscount +
                      (detail?.spinDiscount || 0),
                  )
                }}</strong></span
              >
            </div>
          </div>
        </div>
      </transition>

      <!-- Không có spin bonus -->
      <transition name="spin-bonus-fade">
        <div
          v-if="
            !spinStatus.loading &&
            !spinStatus.hasActiveBonus &&
            spinStatus.checked
          "
          class="spin-no-bonus mb-3"
        >
          <span class="spin-no-bonus-icon">🎡</span>
          <span class="spin-no-bonus-text"
            >Khách hàng chưa có ưu đãi vòng quay tuần này</span
          >
        </div>
      </transition>

      <!-- Thông tin thanh toán -->
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
          <li>
            ✅ <strong class="text-success">Cộng điểm loyalty</strong> cho khách
            hàng
          </li>
        </ul>
      </el-alert>

      <!-- Số tiền -->
      <el-form label-position="top">
        <el-form-item label="Số tiền thanh toán">
          <el-input
            :value="formatMoney(detail?.totalAmount)"
            disabled
            size="large"
          />
        </el-form-item>

        <!-- ================= BANK TRANSFER ================= -->
        <div v-if="detail?.paymentMethod === 'TRANSFER'" class="qr-box">
          <el-alert
            title="Thanh toán bằng chuyển khoản"
            type="success"
            :closable="false"
            class="mb-3"
          />

          <div class="qr-payment">
            <img :src="qrCodeUrl" class="qr-img" v-if="qrCodeUrl" />

            <p class="qr-note">
              Quét mã QR bằng ứng dụng ngân hàng để thanh toán
            </p>
          </div>
        </div>

        <!-- ================= CREDIT CARD ================= -->
        <el-form
          v-if="detail?.paymentMethod === 'CARD'"
          ref="cardFormRef"
          :model="cardForm"
          :rules="cardRules"
          label-position="top"
        >
          <el-form-item label="Loại thẻ" prop="type">
            <el-select v-model="cardForm.type" class="w-100">
              <el-option label="VISA" value="VISA" />
              <el-option label="Credit Card" value="CREDIT" />
            </el-select>
          </el-form-item>

          <el-form-item label="Số thẻ" prop="number">
            <el-input
              v-model="cardForm.number"
              placeholder="1234567812345678"
            />
          </el-form-item>

          <el-form-item label="Tên chủ thẻ" prop="holder">
            <el-input v-model="cardForm.holder" />
          </el-form-item>

          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="Expiry" prop="expiry">
                <el-input v-model="cardForm.expiry" placeholder="MM/YY" />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="CVV" prop="cvv">
                <el-input v-model="cardForm.cvv" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-form>

      <template #footer>
        <el-button @click="showPaymentDialog = false" size="large"
          >Hủy</el-button
        >
        <el-button
          type="primary"
          @click="confirmPayment"
          :loading="paymentLoading"
          :disabled="false"
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
        <el-button type="danger" @click="confirmCancel" :loading="cancelLoading"
          >Xác nhận hủy</el-button
        >
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
import { spinWheelApi } from "../../api/spinWheel.api";
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

const cardFormRef = ref(null);
const cardRules = {
  type: [{ required: true, message: "Chọn loại thẻ", trigger: "change" }],

  number: [
    { required: true, message: "Nhập số thẻ", trigger: "blur" },
    {
      pattern: /^[0-9]{16}$/,
      message: "Số thẻ phải gồm 16 chữ số",
      trigger: "blur",
    },
  ],

  holder: [{ required: true, message: "Nhập tên chủ thẻ", trigger: "blur" }],

  expiry: [
    { required: true, message: "Nhập ngày hết hạn", trigger: "blur" },
    {
      pattern: /^(0[1-9]|1[0-2])\/[0-9]{2}$/,
      message: "Định dạng MM/YY",
      trigger: "blur",
    },
  ],

  cvv: [
    { required: true, message: "Nhập CVV", trigger: "blur" },
    {
      pattern: /^[0-9]{3,4}$/,
      message: "CVV phải 3 hoặc 4 số",
      trigger: "blur",
    },
  ],
};

const showCancelDialog = ref(false);
const cancelReason = ref("");
const showReturnDialog = ref(false);
const showPaymentDialog = ref(false);

// ── Spin Bonus State ──────────────────────────────────────────────────
const spinStatus = reactive({
  loading: false,
  checked: false,
  hasActiveBonus: false,
  bonusRate: 0,
  bonusExpiresAt: null,
});

const returnForm = reactive({
  orderItemId: null,
  quantity: 1,
  reason: "",
  refundAmount: 0,
});
const cardForm = reactive({
  type: "VISA",
  number: "",
  holder: "",
  expiry: "",
  cvv: "",
});

const qrCodeUrl = ref("");

function generateFakeBankQR() {
  const amount = detail.value?.totalAmount || 0;
  const order = detail.value?.orderNumber || orderId.value;

  const bank = "VCB";
  const account = "1234567890"; // fake account

  qrCodeUrl.value = `https://img.vietqr.io/image/${bank}-${account}-compact2.png?amount=${amount}&addInfo=ORDER${order}`;
}

async function openPaymentDialog() {
  showPaymentDialog.value = true;
  await fetchSpinStatus();
  if (detail.value.paymentMethod === "TRANSFER") {
    generateFakeBankQR();
  }
}

// ── Computed ──────────────────────────────────────────────────────────
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

const hasDiscountInfo = computed(
  () =>
    detail.value &&
    ((detail.value.vipDiscountRate && detail.value.vipDiscountRate > 0) ||
      (detail.value.spinDiscountRate && detail.value.spinDiscountRate > 0)),
);

// Spin đã được áp vào đơn rồi hay chưa
const spinAlreadyApplied = computed(() => detail.value?.spinDiscountRate > 0);

// Tính spin discount ước tính - chỉ dùng khi spin CHƯA được áp vào đơn
const estimatedSpinDiscount = computed(() => {
  if (!detail.value?.subtotal || !spinStatus.bonusRate) return 0;
  if (spinAlreadyApplied.value) return 0; // đã áp rồi → không ước tính nữa
  return Math.round((detail.value.subtotal * spinStatus.bonusRate) / 100);
});

// ── Watchers ──────────────────────────────────────────────────────────
watch(
  () => returnForm.orderItemId,
  (newItemId) => {
    if (!newItemId || !detail.value?.items) return;
    const item = detail.value.items.find((i) => i.productId === newItemId);
    if (item) {
      returnForm.quantity = 1;
      returnForm.refundAmount = item.price;
    }
  },
);

watch(
  () => returnForm.quantity,
  (newQty) => {
    if (!returnForm.orderItemId || !detail.value?.items) return;
    const item = detail.value.items.find(
      (i) => i.productId === returnForm.orderItemId,
    );
    if (item) returnForm.refundAmount = item.price * newQty;
  },
);

// ── Helpers ───────────────────────────────────────────────────────────
function formatMoney(val) {
  if (val === null || val === undefined) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(val);
}

function formatExpiry(dateStr) {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

// ── Data loading ──────────────────────────────────────────────────────
async function reload() {
  loading.value = true;
  try {
    const res = await ordersApi.getById(orderId.value);
    detail.value = res?.data?.data || res?.data;
    console.log("PAYMENT METHOD:", detail.value?.paymentMethod);
  } catch (e) {
    toast("Không thể tải chi tiết đơn hàng", "error");
  } finally {
    loading.value = false;
  }
}

// ── Spin Bonus ────────────────────────────────────────────────────────
async function fetchSpinStatus() {
  const customerId = detail.value?.customerId;
  if (!customerId) return;

  spinStatus.loading = true;
  spinStatus.checked = false;
  spinStatus.hasActiveBonus = false;
  spinStatus.bonusRate = 0;
  spinStatus.bonusExpiresAt = null;

  try {
    const res = await spinWheelApi.getStatus(customerId);
    const data = res?.data?.data ?? res?.data ?? res;

    const bonus = Number(data?.currentBonus ?? 0);

    if (bonus > 0) {
      spinStatus.hasActiveBonus = true;
      spinStatus.bonusRate = bonus;
      spinStatus.bonusExpiresAt = data?.bonusExpiresAt ?? null;
    }
  } catch (err) {
    console.warn("🎡 [SpinWheel] error:", err?.response?.status, err?.message);
  } finally {
    spinStatus.loading = false;
    spinStatus.checked = true;
  }
}

// ── Cancel ────────────────────────────────────────────────────────────
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
  return (
    detail.value.items.find((i) => i.productId === returnForm.orderItemId)
      ?.quantity || 1
  );
}

// ── Payment ───────────────────────────────────────────────────────────
async function confirmPayment() {
  if (detail.value?.paymentMethod === "CARD") {
    const valid = await cardFormRef.value.validate().catch(() => false);

    if (!valid) {
      toast("Vui lòng nhập đúng thông tin thẻ", "error");
      return;
    }
  }

  paymentLoading.value = true;

  try {
    await paymentsApi.create({
      orderId: Number(orderId.value),
      method: detail.value.paymentMethod,
      transactionRef: `TXN-${Date.now()}`,
    });

    toast("Thanh toán thành công", "success");

    showPaymentDialog.value = false;

    await reload();
  } catch (e) {
    toast("Lỗi thanh toán", "error");
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
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

.order-page {
  padding: 20px 0;
  font-family: "Inter", sans-serif;
}

.order-card {
  border-radius: 12px;
  padding: 8px;
}

/* HEADER */

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.kicker {
  font-size: 12px;
  text-transform: uppercase;
  font-weight: 900;
  opacity: 0.7;
}

.title {
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
}

.muted {
  font-size: 13px;
  color: #64748b;
}

/* ORDER TRACKING */

.order-tracking {
  display: flex;
  align-items: center;
  margin: 20px 0 10px;
}

.track-step {
  text-align: center;
  width: 120px;
}

.track-icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: auto;
  font-weight: 700;
}

.track-icon.active {
  background: #3b82f6;
  color: white;
}

.track-label {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.track-line {
  flex: 1;
  height: 3px;
  background: #e5e7eb;
}

/* INFO BOX */

.info-box {
  padding: 20px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow:
    0 2px 16px rgba(0, 0, 0, 0.06),
    0 1px 3px rgba(0, 0, 0, 0.05);
}

.info-box h5 {
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
}

.info-box p {
  margin-bottom: 6px;
  color: #374151;
}

/* TABLE */

.order-table {
  border-radius: 10px;
  overflow: hidden;
}

.product-name {
  font-weight: 700;
  color: #0f172a;
}

.product-meta {
  font-size: 12px;
  color: #94a3b8;
}

.discount-text {
  color: #dc2626;
  font-weight: 600;
}

/* TOTAL BOX */

.totals-box {
  width: 100%;
  background: #ffffff;
  border-radius: 16px;
  padding: 26px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
}

.totals-box strong {
  font-weight: 700;
}

.totals-box .fs-5 strong {
  font-size: 22px;
  color: #3b82f6;
}

/* TOTAL ROW */

.total-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 14px;
  color: #374151;
}

.total-row.discount {
  color: #16a34a;
}

.total-final {
  display: flex;
  justify-content: space-between;
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
}

.total-price {
  color: #3b82f6;
}

.section-title {
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 12px;
}

/* TIMELINE */

.order-timeline {
  display: flex;
  align-items: center;
  margin: 28px 0 10px;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 140px;
  position: relative;
}

.timeline-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #e2e8f0;
  transition: all 0.3s;
}

.timeline-dot.active {
  background: #3b82f6;
  box-shadow: 0 0 0 6px rgba(59, 130, 246, 0.15);
}

.timeline-label {
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.timeline-line {
  flex: 1;
  height: 3px;
  background: #e5e7eb;
}

/* TABLE (element overrides) */

.el-table {
  border-radius: 10px;
  overflow: hidden;
}

.el-table th {
  background: #f8fafc;
  font-weight: 700;
  font-size: 13px;
  color: #64748b;
}

.el-table td {
  font-size: 14px;
  color: #374151;
}

/* LAYOUT */

.order-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 24px;
  align-items: start;
}

.order-left {
  min-width: 0;
}

.order-right {
  position: sticky;
  align-self: start;
}

.container-xl {
  max-width: 1280px;
}

.qr-payment {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 16px;
}
.qr-img {
  width: 440px;
  border-radius: 8px;
  border: 1px solid #eee;
}
.qr-note {
  font-size: 13px;
  color: #888;
  text-align: center;
}
</style>
