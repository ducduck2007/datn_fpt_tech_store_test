<template>
  <div class="glass-page">
    <!-- Ambient background orbs -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <div class="container-xl order-layout">
      <!-- LEFT COLUMN -->
      <div class="order-left">
        <div class="glass-card main-card">
          <!-- HEADER -->
          <div class="order-header">
            <div class="order-identity">
              <div class="kicker">
                <span class="kicker-dot"></span>
                Order
              </div>
              <div class="title">{{ detail?.orderNumber || `#${orderId}` }}</div>
              <div class="badge-row mt-2">
                <span :class="['status-pill', `status-${(detail?.status || '').toLowerCase()}`]">
                  <span class="pill-glow"></span>
                  {{ detail?.status }}
                </span>
                <span
                  v-if="isReturned(detail?.status)"
                  class="status-pill status-returned"
                >
                  <span class="pill-glow"></span>
                  Returned
                </span>
              </div>
            </div>

            <div class="order-actions">
              <button class="glass-btn ghost" @click="reload" :class="{ loading }">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
                Reload
              </button>

              <button
                v-if="detail?.status === 'PENDING' && detail?.paymentStatus === 'UNPAID' && detail?.paymentMethod !== 'CASH'"
                class="glass-btn primary"
                @click="openPaymentDialog"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                Thanh toán
              </button>

              <button
                v-if="detail?.status === 'PENDING' || detail?.status === 'PAID'"
                class="glass-btn danger"
                @click="showCancelDialog = true"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                Hủy đơn
              </button>

              <button
                v-if="detail?.status === 'SHIPPING'"
                class="glass-btn primary"
                :class="{ loading: deliveredLoading }"
                @click="confirmDelivered"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                Đã nhận hàng
              </button>

              <button
                v-if="detail?.status === 'DELIVERED' && !isReturned(detail?.status)"
                class="glass-btn warning"
                @click="showReturnDialog = true"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>
                Yêu cầu trả hàng
              </button>
            </div>
          </div>

          <!-- TIMELINE -->
          <div v-if="detail" class="timeline-wrap">
            <div
              v-for="(step, i) in timelineSteps"
              :key="step.key"
              class="timeline-item"
            >
              <div class="timeline-node" :class="{ active: step.active, current: step.current }">
                <div class="node-inner">
                  <span v-if="step.active && !step.current">✓</span>
                  <span v-else-if="step.current" class="node-pulse"></span>
                </div>
              </div>
              <div class="timeline-label" :class="{ active: step.active }">{{ step.label }}</div>
              <div
                v-if="i < timelineSteps.length - 1"
                class="timeline-connector"
                :class="{ active: timelineSteps[i+1]?.active }"
              ></div>
            </div>
          </div>

          <!-- SKELETON -->
          <div v-if="loading" class="skeleton-wrap">
            <div v-for="n in 4" :key="n" class="skeleton-line" :style="`width:${70 + Math.random()*25}%`"></div>
          </div>

          <template v-else-if="detail">
            <!-- INFO GRID -->
            <div class="info-grid">
              <!-- Customer -->
              <div class="glass-info-card">
                <div class="info-card-header">
                  <div class="info-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  </div>
                  <span>Thông tin khách hàng</span>
                </div>
                <div class="info-card-body">
                  <div class="info-row">
                    <span class="info-label">Tên</span>
                    <span class="info-value">{{ detail.customerName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">ID</span>
                    <span class="info-value">{{ detail.customerId }}</span>
                  </div>
                </div>
              </div>

              <!-- Payment & Delivery -->
              <div class="glass-info-card">
                <div class="info-card-header">
                  <div class="info-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                  </div>
                  <span>Thanh toán & Giao hàng</span>
                </div>
                <div class="info-card-body">
                  <div class="info-row">
                    <span class="info-label">Phương thức</span>
                    <span class="info-value">
                      <span class="method-tag">{{ detail.paymentMethod }}</span>
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Kênh</span>
                    <span class="info-value">{{ detail.channel }}</span>
                  </div>
                  <div v-if="detail.notes" class="notes-block">
                    <div class="notes-label">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                      Ghi chú
                    </div>
                    <div class="notes-content">
                      <div
                        v-for="(line, i) in parseNotes(detail.notes)"
                        :key="i"
                        class="notes-chip"
                        :class="line.type"
                      >
                        <span class="notes-chip-icon">{{ line.icon }}</span>
                        <span class="notes-chip-text">{{ line.text }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- ITEMS TABLE -->
            <div class="items-section">
              <div class="section-header">
                <h5>Chi tiết sản phẩm</h5>
                <span class="item-count">{{ detail.items?.length }} sản phẩm</span>
              </div>

              <div class="items-table-wrap">
                <table class="items-table">
                  <thead>
                    <tr>
                      <th>Sản phẩm</th>
                      <th class="center">Số lượng</th>
                      <th class="right">Đơn giá</th>
                      <th class="right">Giảm giá</th>
                      <th class="right">Tổng</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="row in detail.items" :key="row.sku" class="item-row">
                      <td>
                        <div class="product-name">{{ row.productName }}</div>
                        <div class="product-meta">{{ row.variantName }}</div>
                        <div class="product-sku">SKU: {{ row.sku }}</div>
                      </td>
                      <td class="center">
                        <span class="qty-badge">{{ row.quantity }}</span>
                      </td>
                      <td class="right">{{ formatMoney(row.price) }}</td>
                      <td class="right">
                        <span v-if="row.discount > 0" class="discount-val">-{{ formatMoney(row.discount) }}</span>
                        <span v-else class="text-muted">—</span>
                      </td>
                      <td class="right">
                        <strong class="line-total">{{ formatMoney(row.lineTotal) }}</strong>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- RIGHT COLUMN -->
      <div class="order-right" v-if="detail">
        <div class="glass-card totals-card">
          <div class="totals-header">Tóm tắt đơn hàng</div>

          <div class="totals-list">
            <div class="totals-row">
              <span>Tạm tính</span>
              <span>{{ formatMoney(detail.subtotal) }}</span>
            </div>
            <div class="totals-row discount-row">
              <span>Giảm giá</span>
              <span>-{{ formatMoney(detail.discountTotal) }}</span>
            </div>
            <div class="totals-row">
              <span>Phí ship</span>
              <span>{{ formatMoney(detail.shippingFee) }}</span>
            </div>
          </div>

          <div class="totals-divider"></div>

          <div class="totals-final">
            <span>Tổng cộng</span>
            <span class="final-amount">{{ formatMoney(detail.totalAmount) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ================================================================ -->
    <!-- PAYMENT DIALOG                                                    -->
    <!-- ================================================================ -->
    <transition name="modal-fade">
      <div v-if="showPaymentDialog" class="modal-backdrop" @click.self="showPaymentDialog = false">
        <div class="glass-modal">
          <div class="modal-header">
            <div class="modal-title">
              <span class="modal-icon">💳</span>
              Thanh toán đơn hàng
            </div>
            <button class="modal-close" @click="showPaymentDialog = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <div class="modal-body">
            <!-- Spin Loading -->
            <div v-if="spinStatus.loading" class="spin-loading">
              <div class="spin-ring"></div>
              <span>Đang kiểm tra ưu đãi vòng quay...</span>
            </div>

            <!-- Spin Bonus Banner -->
            <transition name="slide-down">
              <div v-if="!spinStatus.loading && spinStatus.hasActiveBonus" class="spin-banner">
                <div class="spin-banner-top">
                  <div class="spin-badge-icon">🎡</div>
                  <div>
                    <div class="spin-banner-title">Ưu đãi vòng quay đã áp dụng!</div>
                    <div class="spin-banner-sub">
                      Giảm thêm <strong>{{ spinStatus.bonusRate }}%</strong>
                      <span v-if="spinStatus.bonusExpiresAt"> · Hết hạn {{ formatExpiry(spinStatus.bonusExpiresAt) }}</span>
                    </div>
                  </div>
                  <div class="spin-rate-badge">-{{ spinStatus.bonusRate }}%</div>
                </div>
                <div class="spin-breakdown">
                  <div class="breakdown-row">
                    <span>Tạm tính</span>
                    <span>{{ formatMoney(detail?.subtotal) }}</span>
                  </div>
                  <div v-if="detail?.vipDiscount > 0" class="breakdown-row positive">
                    <span>🏆 Giảm VIP</span>
                    <span>-{{ formatMoney(detail.vipDiscount) }}</span>
                  </div>
                  <div class="breakdown-row positive">
                    <span>🎡 Giảm Spin ({{ spinStatus.bonusRate }}%)</span>
                    <span>-{{ formatMoney(estimatedSpinDiscount) }}</span>
                  </div>
                  <div class="breakdown-row total-row">
                    <span><strong>Tổng thanh toán</strong></span>
                    <span><strong>{{ formatMoney((detail?.totalAmount || 0) - estimatedSpinDiscount + (detail?.spinDiscount || 0)) }}</strong></span>
                  </div>
                </div>
              </div>
            </transition>

            <!-- No Spin -->
            <div v-if="!spinStatus.loading && !spinStatus.hasActiveBonus && spinStatus.checked" class="no-spin">
              <span>🎡</span> Khách hàng chưa có ưu đãi vòng quay tuần này
            </div>

            <!-- Info Alert -->
            <div class="info-alert">
              <div class="info-alert-title">Sau khi thanh toán thành công</div>
              <div class="info-alert-rows">
                <div class="alert-item">✅ Đơn hàng chuyển sang <strong>PAID</strong></div>
                <div class="alert-item">✅ Xuất kho tự động</div>
                <div class="alert-item">✅ <strong>Cộng điểm loyalty</strong> cho khách hàng</div>
              </div>
            </div>

            <!-- Amount -->
            <div class="form-field">
              <label>Số tiền thanh toán</label>
              <div class="amount-display">{{ formatMoney(detail?.totalAmount) }}</div>
            </div>

            <!-- BANK TRANSFER QR -->
            <div v-if="detail?.paymentMethod === 'TRANSFER'" class="qr-section">
              <div class="qr-label">Quét mã QR để thanh toán</div>
              <div class="qr-frame">
                <img :src="qrCodeUrl" v-if="qrCodeUrl" class="qr-img" />
              </div>
              <div class="qr-note">Sử dụng ứng dụng ngân hàng để quét mã QR</div>
            </div>

            <!-- CARD FORM -->
            <div v-if="detail?.paymentMethod === 'CARD'" class="card-form">
              <div class="form-field">
                <label>Loại thẻ</label>
                <select v-model="cardForm.type" class="glass-select">
                  <option value="VISA">VISA</option>
                  <option value="CREDIT">Credit Card</option>
                </select>
              </div>
              <div class="form-field">
                <label>Số thẻ</label>
                <input v-model="cardForm.number" class="glass-input" placeholder="1234 5678 1234 5678" />
              </div>
              <div class="form-field">
                <label>Tên chủ thẻ</label>
                <input v-model="cardForm.holder" class="glass-input" placeholder="NGUYEN VAN A" />
              </div>
              <div class="form-row">
                <div class="form-field">
                  <label>Expiry</label>
                  <input v-model="cardForm.expiry" class="glass-input" placeholder="MM/YY" />
                </div>
                <div class="form-field">
                  <label>CVV</label>
                  <input v-model="cardForm.cvv" class="glass-input" placeholder="123" />
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="glass-btn ghost" @click="showPaymentDialog = false">Hủy</button>
            <button class="glass-btn primary" @click="confirmPayment" :class="{ loading: paymentLoading }">
              Xác nhận thanh toán
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ================================================================ -->
    <!-- CANCEL DIALOG                                                     -->
    <!-- ================================================================ -->
    <transition name="modal-fade">
      <div v-if="showCancelDialog" class="modal-backdrop" @click.self="showCancelDialog = false">
        <div class="glass-modal modal-sm">
          <div class="modal-header">
            <div class="modal-title">
              <span class="modal-icon">❌</span>
              Hủy đơn hàng
            </div>
            <button class="modal-close" @click="showCancelDialog = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <div class="modal-body">
            <div :class="['warning-alert', detail?.paymentStatus === 'PAID' ? 'danger' : 'caution']">
              <div class="warning-title">{{ getCancelWarningTitle() }}</div>
              <div class="warning-body" v-html="getCancelWarningMessage()"></div>
            </div>
            <div class="form-field mt-3">
              <label>Lý do hủy <span class="optional">(không bắt buộc)</span></label>
              <textarea v-model="cancelReason" class="glass-textarea" rows="3" placeholder="Nhập lý do hủy đơn..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="glass-btn ghost" @click="showCancelDialog = false">Đóng</button>
            <button class="glass-btn danger" @click="confirmCancel" :class="{ loading: cancelLoading }">Xác nhận hủy</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ================================================================ -->
    <!-- RETURN DIALOG                                                     -->
    <!-- ================================================================ -->
    <transition name="modal-fade">
      <div v-if="showReturnDialog" class="modal-backdrop" @click.self="showReturnDialog = false">
        <div class="glass-modal">
          <div class="modal-header">
            <div class="modal-title">
              <span class="modal-icon">🔄</span>
              Yêu cầu trả hàng
            </div>
            <button class="modal-close" @click="showReturnDialog = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-field">
              <label>Chọn sản phẩm</label>
              <select v-model="returnForm.orderItemId" class="glass-select">
                <option value="" disabled>Chọn sản phẩm muốn trả</option>
                <option v-for="item in detail?.items" :key="item.productId" :value="item.productId">
                  {{ item.productName }} - {{ item.variantName }}
                </option>
              </select>
            </div>
            <div class="form-field">
              <label>Số lượng</label>
              <input type="number" v-model="returnForm.quantity" :min="1" :max="getMaxReturnQuantity()" class="glass-input" />
            </div>
            <div class="form-field">
              <label>Lý do trả hàng</label>
              <textarea v-model="returnForm.reason" class="glass-textarea" rows="3" placeholder="Nhập lý do..."></textarea>
            </div>
            <div class="form-field">
              <label>Số tiền hoàn</label>
              <div class="amount-display">{{ formatMoney(returnForm.refundAmount) }}</div>
            </div>
            <div class="info-alert warning-info">
              <div class="alert-item">⚠️ Điểm loyalty đã cộng sẽ bị trừ lại khi trả hàng được duyệt.</div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="glass-btn ghost" @click="showReturnDialog = false">Đóng</button>
            <button class="glass-btn primary" @click="submitReturn">Gửi yêu cầu</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { returnsApi } from "../../api/returns.api";
import { paymentsApi } from "../../api/payments";
import { spinWheelApi } from "../../api/spinWheel.api";
import { toast } from "../../ui/toast";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const cancelLoading = ref(false);
const paymentLoading = ref(false);
const detail = ref(null);
const orderId = computed(() => route.params.orderId);

const showCancelDialog = ref(false);
const cancelReason = ref("");
const showReturnDialog = ref(false);
const showPaymentDialog = ref(false);

const spinStatus = reactive({
  loading: false,
  checked: false,
  hasActiveBonus: false,
  bonusRate: 0,
  bonusExpiresAt: null,
});

const returnForm = reactive({ orderItemId: null, quantity: 1, reason: "", refundAmount: 0 });
const cardForm = reactive({ type: "VISA", number: "", holder: "", expiry: "", cvv: "" });
const qrCodeUrl = ref("");

// Timeline steps computed
const timelineSteps = computed(() => {
  if (!detail.value) return [];
  const s = detail.value.status;
  const isCash = detail.value.paymentMethod === 'CASH';
  const order = ['PENDING','PAID','PROCESSING','SHIPPING','DELIVERED'];
  const pos = order.indexOf(s);

  const steps = [
    { key: 'placed', label: 'Đặt hàng', statuses: ['PENDING','PAID','PROCESSING','SHIPPING','DELIVERED'] },
  ];
  if (!isCash) {
    steps.push({ key: 'paid', label: 'Thanh toán', statuses: ['PAID','PROCESSING','SHIPPING','DELIVERED'] });
  }
  steps.push(
    { key: 'processing', label: 'Chuẩn bị hàng', statuses: ['PROCESSING','SHIPPING','DELIVERED'] },
    { key: 'shipping', label: 'Đang chờ nhận hàng', statuses: ['SHIPPING','DELIVERED'] },
    { key: 'delivered', label: 'Đã giao', statuses: ['DELIVERED'] },
  );

  return steps.map((step, i) => ({
    ...step,
    active: step.statuses.includes(s),
    current: step.statuses[0] === s || (i > 0 && !steps[i].statuses.includes(s) && steps[i-1]?.statuses.includes(s))
  }));
});

// Parse notes into structured chips
function parseNotes(notes) {
  if (!notes) return [];
  const lines = notes.split('|').map(s => s.trim()).filter(Boolean);
  return lines.map(line => {
    if (line.toLowerCase().includes('vip')) return { type: 'vip', icon: '👑', text: line };
    if (line.toLowerCase().includes('mã') || line.toLowerCase().includes('discount')) return { type: 'discount', icon: '🏷️', text: line };
    if (line.toLowerCase().includes('giao') || line.toLowerCase().includes('địa chỉ')) return { type: 'delivery', icon: '📍', text: line };
    if (line.toLowerCase().includes('thanh toán')) return { type: 'payment', icon: '💳', text: line };
    return { type: 'default', icon: '📝', text: line };
  });
}

function generateFakeBankQR() {
  const amount = detail.value?.totalAmount || 0;
  const order = detail.value?.orderNumber || orderId.value;
  qrCodeUrl.value = `https://img.vietqr.io/image/VCB-1234567890-compact2.png?amount=${amount}&addInfo=ORDER${order}`;
}

async function openPaymentDialog() {
  showPaymentDialog.value = true;
  await fetchSpinStatus();
  if (detail.value?.paymentMethod === "TRANSFER") generateFakeBankQR();
}

const estimatedSpinDiscount = computed(() => {
  if (!detail.value?.subtotal || !spinStatus.bonusRate) return 0;
  if (detail.value?.spinDiscountRate > 0) return 0;
  return Math.round((detail.value.subtotal * spinStatus.bonusRate) / 100);
});

watch(() => returnForm.orderItemId, (newItemId) => {
  if (!newItemId || !detail.value?.items) return;
  const item = detail.value.items.find(i => i.productId === newItemId);
  if (item) { returnForm.quantity = 1; returnForm.refundAmount = item.price; }
});

watch(() => returnForm.quantity, (newQty) => {
  if (!returnForm.orderItemId || !detail.value?.items) return;
  const item = detail.value.items.find(i => i.productId === returnForm.orderItemId);
  if (item) returnForm.refundAmount = item.price * newQty;
});

function formatMoney(val) {
  if (val === null || val === undefined) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(val);
}

function formatExpiry(dateStr) {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" });
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
  spinStatus.loading = true; spinStatus.checked = false; spinStatus.hasActiveBonus = false;
  try {
    const res = await spinWheelApi.getStatus(customerId);
    const data = res?.data?.data ?? res?.data ?? res;
    const bonus = Number(data?.currentBonus ?? 0);
    if (bonus > 0) { spinStatus.hasActiveBonus = true; spinStatus.bonusRate = bonus; spinStatus.bonusExpiresAt = data?.bonusExpiresAt ?? null; }
  } catch (err) {
    console.warn("SpinWheel error:", err?.message);
  } finally {
    spinStatus.loading = false; spinStatus.checked = true;
  }
}

function getCancelWarningTitle() {
  return detail.value?.paymentStatus === "PAID" ? "⚠️ Cảnh báo: Hủy đơn đã thanh toán" : "Xác nhận hủy đơn";
}

function getCancelWarningMessage() {
  if (detail.value?.paymentStatus === "PAID") {
    const pts = Math.floor((detail.value.totalAmount || 0) / 10000);
    return `<p><strong>Nếu hủy đơn đã thanh toán:</strong></p><ul><li>❌ Trừ lại <strong>${pts} điểm</strong> loyalty</li><li>💰 Hoàn tiền cho khách</li><li>📦 Nhập lại kho</li></ul>`;
  }
  return `<p>Đơn chưa thanh toán sẽ được hủy miễn phí.</p>`;
}

function getMaxReturnQuantity() {
  if (!returnForm.orderItemId || !detail.value?.items) return 1;
  return detail.value.items.find(i => i.productId === returnForm.orderItemId)?.quantity || 1;
}

async function confirmPayment() {
  paymentLoading.value = true;
  try {
    await paymentsApi.create({ orderId: Number(orderId.value), method: detail.value.paymentMethod, transactionRef: `TXN-${Date.now()}` });
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

const deliveredLoading = ref(false);

const confirmDelivered = async () => {
  if (!confirm("Bạn đã nhận được hàng?\n\nSau khi xác nhận:\n- Đơn hàng sẽ hoàn tất\n- Người bán sẽ nhận được thanh toán\n- Bạn vẫn có thể gửi yêu cầu trả hàng nếu cần")) return;

  deliveredLoading.value = true;
  try {
    await ordersApi.markAsDelivered(orderId.value);
    toast("Đã xác nhận nhận hàng", "success");
    await reload();
  } catch (err) {
    toast("Không thể cập nhật trạng thái đơn hàng", "error");
  } finally {
    deliveredLoading.value = false;
  }
};

function isReturned(status) {
  return ["PARTIALLY_RETURNED", "RETURNED"].includes(status);
}

onMounted(() => reload());
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap');

/* ── RESET & ROOT ─────────────────────────────────────────────────── */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.glass-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8ecf8 0%, #f0f4ff 35%, #e6f0f8 65%, #ede8f8 100%);
  font-family: 'Inter', sans-serif;
  color: #1e293b;
  position: relative;
  overflow-x: hidden;
  padding: 32px 24px 60px;
}

/* ── AMBIENT ORBS ─────────────────────────────────────────────────── */
.orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(90px);
  pointer-events: none;
  z-index: 0;
  animation: drift 14s ease-in-out infinite alternate;
}
.orb-1 { width: 560px; height: 560px; background: radial-gradient(circle, rgba(99,102,241,0.18) 0%, transparent 70%); top: -140px; left: -120px; }
.orb-2 { width: 440px; height: 440px; background: radial-gradient(circle, rgba(16,185,129,0.13) 0%, transparent 70%); top: 40%; right: -100px; animation-delay: -5s; }
.orb-3 { width: 400px; height: 400px; background: radial-gradient(circle, rgba(139,92,246,0.12) 0%, transparent 70%); bottom: 8%; left: 28%; animation-delay: -9s; }

@keyframes drift {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(28px, 18px) scale(1.07); }
}

/* ── LAYOUT ───────────────────────────────────────────────────────── */
.container-xl { position: relative; z-index: 1; max-width: 1280px; margin: 0 auto; }

.order-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 20px;
  align-items: start;
}

.order-right { position: sticky; top: 24px; }

/* ── GLASS CARD ───────────────────────────────────────────────────── */
.glass-card {
  background: rgba(255,255,255,0.62);
  backdrop-filter: blur(24px) saturate(1.8);
  -webkit-backdrop-filter: blur(24px) saturate(1.8);
  border: 1px solid rgba(255,255,255,0.85);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 8px 32px rgba(99,102,241,0.08), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.9);
}

/* ── HEADER ───────────────────────────────────────────────────────── */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.kicker {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: 'Inter', sans-serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(99,102,241,1);
  margin-bottom: 4px;
}

.kicker-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #6366f1;
  box-shadow: 0 0 8px rgba(99,102,241,0.6);
  animation: pulse-dot 2s ease-in-out infinite;
}
@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 6px rgba(99,102,241,0.5); }
  50% { box-shadow: 0 0 14px rgba(99,102,241,0.8), 0 0 24px rgba(99,102,241,0.3); }
}

.title {
  font-family: 'Inter', sans-serif;
  font-size: 26px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.badge-row { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; }

.status-pill {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding: 5px 14px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  overflow: hidden;
}
.pill-glow { position: absolute; inset: 0; border-radius: 100px; opacity: 0.15; }
.status-pending   { background: rgba(99,102,241,0.1); border: 1px solid rgba(99,102,241,0.3); color: #4338ca; }
.status-pending .pill-glow   { background: #6366f1; }
.status-paid      { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.3); color: #065f46; }
.status-paid .pill-glow      { background: #10b981; }
.status-shipping  { background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.3); color: #92400e; }
.status-shipping .pill-glow  { background: #f59e0b; }
.status-delivered { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.3); color: #065f46; }
.status-cancelled { background: rgba(239,68,68,0.1); border: 1px solid rgba(239,68,68,0.3); color: #991b1b; }
.status-returned  { background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.3); color: #92400e; }

/* ── BUTTONS ──────────────────────────────────────────────────────── */
.order-actions { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }

.glass-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border-radius: 12px;
  font-family: 'DM Sans', sans-serif;
  font-size: 13.5px;
  font-weight: 500;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(.4,0,.2,1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}
.glass-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.2s;
  background: rgba(255,255,255,0.06);
}
.glass-btn:hover::before { opacity: 1; }
.glass-btn:active { transform: scale(0.97); }

.glass-btn.ghost {
  background: rgba(255,255,255,0.6);
  border-color: rgba(0,0,0,0.1);
  color: #475569;
}
.glass-btn.primary {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border-color: rgba(99,102,241,0.3);
  color: #fff;
  box-shadow: 0 4px 16px rgba(99,102,241,0.35);
}
.glass-btn.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border-color: rgba(239,68,68,0.3);
  color: #fff;
  box-shadow: 0 4px 16px rgba(239,68,68,0.25);
}
.glass-btn.warning {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border-color: rgba(245,158,11,0.3);
  color: #fff;
  box-shadow: 0 4px 12px rgba(245,158,11,0.25);
}
.glass-btn.loading { opacity: 0.7; pointer-events: none; }

/* ── TIMELINE ─────────────────────────────────────────────────────── */
.timeline-wrap {
  display: flex;
  align-items: flex-start;
  margin: 28px 0 4px;
  padding: 20px 24px;
  background: rgba(255,255,255,0.5);
  border: 1px solid rgba(255,255,255,0.8);
  border-radius: 14px;
  overflow-x: auto;
  gap: 0;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
  min-width: 90px;
}

.timeline-node {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: rgba(0,0,0,0.04);
  border: 2px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #94a3b8;
  transition: all 0.3s;
  z-index: 1;
}
.timeline-node.active {
  background: rgba(99,102,241,0.12);
  border-color: #6366f1;
  color: #4f46e5;
  box-shadow: 0 0 0 4px rgba(99,102,241,0.12);
}
.timeline-node.current {
  background: #6366f1;
  border-color: #6366f1;
  box-shadow: 0 0 0 5px rgba(99,102,241,0.2), 0 0 16px rgba(99,102,241,0.35);
}

.node-pulse {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #fff;
  animation: pulse-dot 1.5s ease-in-out infinite;
}

.timeline-label {
  margin-top: 8px;
  font-size: 11.5px;
  color: #94a3b8;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
}
.timeline-label.active { color: #4f46e5; font-weight: 600; }

.timeline-connector {
  position: absolute;
  top: 13px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #e2e8f0;
  z-index: 0;
  transition: background 0.3s;
}
.timeline-connector.active { background: rgba(99,102,241,0.35); }

/* ── INFO GRID ────────────────────────────────────────────────────── */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 24px;
}

.glass-info-card {
  background: rgba(255,255,255,0.55);
  border: 1px solid rgba(255,255,255,0.85);
  border-radius: 14px;
  padding: 18px 20px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.glass-info-card:hover { border-color: rgba(99,102,241,0.2); box-shadow: 0 4px 16px rgba(99,102,241,0.08); }

.info-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Inter', sans-serif;
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  margin-bottom: 14px;
}
.info-icon {
  width: 28px; height: 28px;
  background: rgba(99,102,241,0.08);
  border: 1px solid rgba(99,102,241,0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
}

.info-card-body { display: flex; flex-direction: column; gap: 10px; }

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.info-label { font-size: 13px; color: #94a3b8; flex-shrink: 0; }
.info-value { font-size: 13.5px; color: #1e293b; font-weight: 500; text-align: right; }

.method-tag {
  display: inline-flex;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11.5px;
  font-weight: 600;
  background: rgba(99,102,241,0.08);
  border: 1px solid rgba(99,102,241,0.2);
  color: #4f46e5;
  letter-spacing: 0.04em;
}

/* ── NOTES BLOCK ─────────────────────────────────────────────────── */
.notes-block {
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.06);
}
.notes-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  margin-bottom: 8px;
}
.notes-content { display: flex; flex-direction: column; gap: 6px; }

.notes-chip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 12.5px;
  line-height: 1.5;
  background: rgba(255,255,255,0.6);
  border: 1px solid rgba(0,0,0,0.06);
}
.notes-chip-icon { flex-shrink: 0; font-size: 14px; }
.notes-chip-text { color: #475569; }

.notes-chip.vip     { background: rgba(245,158,11,0.08); border-color: rgba(245,158,11,0.2); }
.notes-chip.vip .notes-chip-text { color: #92400e; }
.notes-chip.discount { background: rgba(16,185,129,0.08); border-color: rgba(16,185,129,0.2); }
.notes-chip.discount .notes-chip-text { color: #065f46; }
.notes-chip.delivery { background: rgba(99,102,241,0.07); border-color: rgba(99,102,241,0.18); }
.notes-chip.delivery .notes-chip-text { color: #3730a3; }

/* ── ITEMS TABLE ──────────────────────────────────────────────────── */
.items-section { margin-top: 24px; }

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.section-header h5 {
  font-family: 'Inter', sans-serif;
  font-size: 13px;
  font-weight: 800;
  color: #1e293b;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.item-count {
  font-size: 11.5px;
  padding: 3px 9px;
  background: rgba(99,102,241,0.08);
  border: 1px solid rgba(99,102,241,0.18);
  border-radius: 20px;
  color: #4f46e5;
}

.items-table-wrap {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.85);
  background: rgba(255,255,255,0.45);
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13.5px;
}
.items-table thead tr {
  border-bottom: 1px solid rgba(0,0,0,0.06);
}
.items-table th {
  padding: 12px 16px;
  text-align: left;
  font-family: 'Inter', sans-serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: #94a3b8;
  background: rgba(248,250,252,0.6);
}
.items-table th.center { text-align: center; }
.items-table th.right { text-align: right; }

.item-row { border-bottom: 1px solid rgba(0,0,0,0.04); transition: background 0.15s; }
.item-row:last-child { border-bottom: none; }
.item-row:hover { background: rgba(99,102,241,0.03); }
.items-table td { padding: 14px 16px; vertical-align: middle; }
.items-table td.center { text-align: center; }
.items-table td.right { text-align: right; color: #64748b; }

.product-name { font-weight: 600; color: #0f172a; margin-bottom: 3px; }
.product-meta { font-size: 12px; color: #94a3b8; margin-bottom: 2px; }
.product-sku { font-size: 11px; color: #cbd5e1; font-family: 'Courier New', monospace; }

.qty-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border-radius: 8px;
  background: rgba(99,102,241,0.08);
  border: 1px solid rgba(99,102,241,0.18);
  color: #4f46e5;
  font-weight: 600;
  font-size: 13px;
}
.discount-val { color: #dc2626; font-weight: 600; }
.line-total { color: #0f172a; font-size: 14px; }
.text-muted { color: #cbd5e1; }

/* ── TOTALS CARD ──────────────────────────────────────────────────── */
.totals-card { padding: 24px; }

.totals-header {
  font-family: 'Inter', sans-serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #94a3b8;
  margin-bottom: 20px;
}

.totals-list { display: flex; flex-direction: column; gap: 2px; }

.totals-row {
  display: flex;
  justify-content: space-between;
  padding: 9px 0;
  font-size: 14px;
  color: #64748b;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}
.totals-row:last-child { border-bottom: none; }
.discount-row { color: #059669; }

.totals-divider {
  height: 1px;
  background: rgba(0,0,0,0.07);
  margin: 16px 0;
}

.totals-final {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding-top: 4px;
}
.totals-final span:first-child {
  font-family: 'Inter', sans-serif;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
}
.final-amount {
  font-family: 'Inter', sans-serif;
  font-size: 24px;
  font-weight: 900;
  background: linear-gradient(135deg, #6366f1, #0ea5e9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ── SKELETON ─────────────────────────────────────────────────────── */
.skeleton-wrap { padding: 24px 0; display: flex; flex-direction: column; gap: 12px; }
.skeleton-line {
  height: 14px;
  border-radius: 7px;
  background: linear-gradient(90deg, rgba(0,0,0,0.04) 25%, rgba(0,0,0,0.08) 50%, rgba(0,0,0,0.04) 75%);
  background-size: 400% 100%;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer { 0% { background-position: 100% 0; } 100% { background-position: -100% 0; } }

/* ── MODAL ────────────────────────────────────────────────────────── */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15,23,42,0.35);
  backdrop-filter: blur(8px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.glass-modal {
  background: rgba(255,255,255,0.82);
  backdrop-filter: blur(28px) saturate(2);
  -webkit-backdrop-filter: blur(28px) saturate(2);
  border: 1px solid rgba(255,255,255,0.95);
  border-radius: 22px;
  width: 100%;
  max-width: 520px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 24px 64px rgba(99,102,241,0.15), 0 8px 24px rgba(0,0,0,0.1), inset 0 1px 0 rgba(255,255,255,1);
}
.modal-sm { max-width: 480px; }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 26px 18px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
}
.modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: 'Inter', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}
.modal-icon { font-size: 20px; }
.modal-close {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 8px;
  background: rgba(0,0,0,0.04);
  border: 1px solid rgba(0,0,0,0.07);
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
}
.modal-close:hover { background: rgba(239,68,68,0.08); border-color: rgba(239,68,68,0.2); color: #ef4444; }

.modal-body { padding: 20px 26px; display: flex; flex-direction: column; gap: 16px; }
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 18px 26px;
  border-top: 1px solid rgba(0,0,0,0.06);
}

/* ── SPIN BANNER ──────────────────────────────────────────────────── */
.spin-loading {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(99,102,241,0.05);
  border: 1px solid rgba(99,102,241,0.15);
  font-size: 13px;
  color: #4f46e5;
}
.spin-ring {
  width: 18px; height: 18px;
  border: 2px solid rgba(99,102,241,0.2);
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  flex-shrink: 0;
}
@keyframes spin { to { transform: rotate(360deg); } }

.spin-banner {
  border-radius: 14px;
  background: rgba(16,185,129,0.05);
  border: 1px solid rgba(16,185,129,0.2);
  overflow: hidden;
}
.spin-banner-top {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
}
.spin-badge-icon { font-size: 24px; flex-shrink: 0; }
.spin-banner-title { font-size: 13.5px; font-weight: 600; color: #065f46; margin-bottom: 3px; }
.spin-banner-sub { font-size: 12.5px; color: #047857; }
.spin-rate-badge {
  margin-left: auto;
  padding: 6px 14px;
  border-radius: 100px;
  background: rgba(16,185,129,0.12);
  border: 1px solid rgba(16,185,129,0.25);
  color: #065f46;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 800;
  white-space: nowrap;
}

.spin-breakdown {
  padding: 12px 16px;
  border-top: 1px solid rgba(16,185,129,0.1);
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: rgba(255,255,255,0.4);
}
.breakdown-row {
  display: flex;
  justify-content: space-between;
  font-size: 12.5px;
  color: #64748b;
  padding: 3px 0;
}
.breakdown-row.positive { color: #059669; }
.breakdown-row.total-row {
  padding-top: 8px;
  margin-top: 4px;
  border-top: 1px solid rgba(0,0,0,0.06);
  color: #0f172a;
  font-size: 13.5px;
}

.no-spin {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(0,0,0,0.03);
  border: 1px solid rgba(0,0,0,0.06);
  font-size: 12.5px;
  color: #94a3b8;
}

/* ── INFO ALERT ───────────────────────────────────────────────────── */
.info-alert {
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(99,102,241,0.05);
  border: 1px solid rgba(99,102,241,0.15);
}
.info-alert-title {
  font-size: 12.5px;
  font-weight: 700;
  color: #4f46e5;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.info-alert-rows { display: flex; flex-direction: column; gap: 5px; }
.alert-item { font-size: 13px; color: #475569; }
.warning-info { background: rgba(245,158,11,0.05); border-color: rgba(245,158,11,0.2); }
.warning-info .alert-item { color: #78350f; }

/* ── FORM ELEMENTS ────────────────────────────────────────────────── */
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.07em;
}
.optional { font-weight: 400; color: #94a3b8; text-transform: none; letter-spacing: 0; }

.glass-input, .glass-select, .glass-textarea {
  width: 100%;
  padding: 10px 14px;
  background: rgba(255,255,255,0.7);
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 10px;
  color: #1e293b;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.glass-input:focus, .glass-select:focus, .glass-textarea:focus {
  border-color: rgba(99,102,241,0.5);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
  background: rgba(255,255,255,0.95);
}
.glass-select { appearance: none; cursor: pointer; }
.glass-textarea { resize: vertical; min-height: 80px; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.amount-display {
  padding: 12px 16px;
  background: rgba(99,102,241,0.06);
  border: 1px solid rgba(99,102,241,0.18);
  border-radius: 10px;
  font-family: 'Inter', sans-serif;
  font-size: 18px;
  font-weight: 800;
  color: #4f46e5;
}

.mt-3 { margin-top: 12px; }

/* ── WARNING ALERT ───────────────────────────────────────────────── */
.warning-alert {
  padding: 14px 16px;
  border-radius: 12px;
}
.warning-alert.danger {
  background: rgba(239,68,68,0.05);
  border: 1px solid rgba(239,68,68,0.2);
}
.warning-alert.caution {
  background: rgba(245,158,11,0.06);
  border: 1px solid rgba(245,158,11,0.2);
}
.warning-title {
  font-size: 13px;
  font-weight: 700;
  color: #b91c1c;
  margin-bottom: 8px;
}
.warning-alert.caution .warning-title { color: #92400e; }
.warning-body { font-size: 13px; color: #64748b; line-height: 1.6; }
.warning-body ul { padding-left: 16px; margin-top: 6px; }
.warning-body li { margin-bottom: 4px; }

/* ── QR ───────────────────────────────────────────────────────────── */
.qr-section { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 8px 0; }
.qr-label { font-size: 12px; color: #64748b; text-transform: uppercase; letter-spacing: 0.07em; font-weight: 600; }
.qr-frame {
  padding: 12px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(99,102,241,0.12), 0 2px 8px rgba(0,0,0,0.08);
}
.qr-img { width: 220px; display: block; border-radius: 6px; }
.qr-note { font-size: 12px; color: #475569; text-align: center; }

/* ── TRANSITIONS ──────────────────────────────────────────────────── */
.modal-fade-enter-active, .modal-fade-leave-active { transition: all 0.25s cubic-bezier(.4,0,.2,1); }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
.modal-fade-enter-from .glass-modal, .modal-fade-leave-to .glass-modal { transform: scale(0.95) translateY(10px); }

.slide-down-enter-active { transition: all 0.3s cubic-bezier(.4,0,.2,1); }
.slide-down-enter-from { opacity: 0; transform: translateY(-8px); }

/* ── RESPONSIVE ───────────────────────────────────────────────────── */
@media (max-width: 960px) {
  .order-layout { grid-template-columns: 1fr; }
  .order-right { position: static; }
}
@media (max-width: 640px) {
  .glass-page { padding: 16px 14px 40px; }
  .glass-card { padding: 18px; }
  .info-grid { grid-template-columns: 1fr; }
  .title { font-size: 20px; }
  .timeline-wrap { gap: 0; }
}
</style>