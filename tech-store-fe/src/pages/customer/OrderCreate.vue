<template>
  <div class="order-page">
    <!-- Header -->
    <div class="order-header">
      <button class="back-btn" @click="$router.push('/')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M19 12H5M12 5l-7 7 7 7" />
        </svg>
        Tiếp tục mua sắm
      </button>
      <div class="header-title">
        <span class="step-badge">Bước cuối</span>
        <h1>Xác nhận đơn hàng</h1>
      </div>
    </div>

    <div class="order-layout">
      <!-- LEFT -->
      <div class="order-left">
        <!-- ITEMS -->
        <div class="section-card">
          <div class="section-label">Sản phẩm đã chọn</div>

          <div v-if="form.items.length === 0" class="empty-cart">
            <div class="empty-icon">🛒</div>
            <p>Giỏ hàng trống</p>
            <button class="btn-outline" @click="$router.push('/')">Khám phá sản phẩm</button>
          </div>

          <div v-else class="items-list">
            <div v-for="(item, idx) in form.items" :key="idx" class="item-row">
              <div class="item-icon">💻</div>
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-meta">Variant ID: {{ item.variantId }}</div>
              </div>
              <div class="item-qty">
                <span class="qty-label">SL</span>
                <span class="qty-value">{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- DELIVERY ADDRESS -->
        <div v-if="deliveryMethod === 'HOME'" class="section-card">
          <div class="section-label">Địa chỉ giao hàng</div>
          <div class="form-group">
            <label>Địa chỉ</label>
            <input v-model="delivery.address" class="form-input" placeholder="Số nhà, đường, quận..." />
          </div>
          <div class="form-group">
            <label>Ghi chú</label>
            <textarea v-model="delivery.note" class="form-input" rows="3" placeholder="Ví dụ: gọi trước khi giao" />
          </div>
        </div>
      </div>

      <!-- RIGHT -->
      <div class="order-right">
        <!-- PAYMENT -->
        <div class="section-card">
          <div class="section-label">Phương thức thanh toán</div>
          <div class="payment-options">
            <label v-for="opt in paymentOptions" :key="opt.value" class="payment-opt"
              :class="{ active: form.paymentMethod === opt.value }">
              <input type="radio" :value="opt.value" v-model="form.paymentMethod" />
              <span class="opt-icon">{{ opt.icon }}</span>
              <span class="opt-label">{{ opt.label }}</span>
              <span v-if="form.paymentMethod === opt.value" class="check-mark">✓</span>
            </label>
          </div>
        </div>

        <!-- DELIVERY -->
        <div class="section-card">
          <div class="section-label">Hình thức giao hàng</div>
          <div class="payment-options">
            <label class="payment-opt" :class="{ active: deliveryMethod === 'STORE' }">
              <input type="radio" value="STORE" v-model="deliveryMethod" />
              <span class="opt-icon">🏬</span>
              <span class="opt-label">Nhận tại cửa hàng</span>
              <span v-if="deliveryMethod === 'STORE'" class="check-mark">✓</span>
            </label>
            <label class="payment-opt" :class="{ active: deliveryMethod === 'HOME' }">
              <input type="radio" value="HOME" v-model="deliveryMethod" />
              <span class="opt-icon">🚚</span>
              <span class="opt-label">Giao tại nhà</span>
              <span v-if="deliveryMethod === 'HOME'" class="check-mark">✓</span>
            </label>
          </div>
        </div>

        <!-- PROMO CODE -->
        <div class="section-card">
          <div class="section-label">Mã khuyến mãi</div>

          <!-- Input row -->
          <div class="promo-row">
            <div class="promo-input-wrap" :class="{ 'promo-applied': promoApplied, 'promo-error': promoError }">
              <span class="promo-prefix">🏷️</span>
              <input
                v-model="promoInput"
                class="promo-input"
                placeholder="Nhập mã giảm giá..."
                :disabled="promoApplied"
                @keydown.enter="handlePromo"
                @input="promoError = ''"
              />
              <!-- Clear button when applied -->
              <button v-if="promoApplied" class="promo-clear-btn" @click="clearPromo" title="Xóa mã">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                  <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
            <button
              class="promo-btn"
              :class="{ 'promo-btn--loading': promoLoading, 'promo-btn--applied': promoApplied }"
              :disabled="promoLoading || promoApplied || !promoInput.trim()"
              @click="handlePromo"
            >
              <span v-if="promoLoading" class="spinner"></span>
              <span v-else-if="promoApplied">✓ Đã áp dụng</span>
              <span v-else>Áp dụng</span>
            </button>
          </div>

          <!-- Error message -->
          <div v-if="promoError" class="promo-msg promo-msg--error">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            {{ promoError }}
          </div>

          <!-- Success message -->
          <div v-if="promoApplied && promoResult" class="promo-msg promo-msg--success">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
              <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Giảm <strong>{{ formatMoney(promoResult.discountAmount) }}</strong>
            <span v-if="promoResult.promotionName" class="promo-name-tag">{{ promoResult.promotionName }}</span>
          </div>
        </div>

        <!-- ORDER SUMMARY -->
        <div class="section-card summary-card">
          <div class="section-label">Tóm tắt đơn hàng</div>
          <div class="summary-lines">
            <div class="summary-line">
              <span>Tạm tính</span>
              <span>{{ formatMoney(subtotal) }}</span>
            </div>
            <div v-if="promoApplied && promoResult" class="summary-line summary-line--discount">
              <span>Mã <code>{{ promoInput.toUpperCase() }}</code></span>
              <span class="discount-val">−{{ formatMoney(promoResult.discountAmount) }}</span>
            </div>
            <div class="summary-line summary-line--muted">
              <span>Phí vận chuyển</span>
              <span>Tính khi giao</span>
            </div>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-total">
            <span>Tổng cộng</span>
            <span class="summary-total-amount">{{ formatMoney(totalAfterDiscount) }}</span>
          </div>
        </div>

        <!-- NOTES -->
        <div class="section-card">
          <div class="section-label">Ghi chú (tùy chọn)</div>
          <textarea v-model="form.notes" class="notes-input" rows="3" />
        </div>

        <div v-if="alert" class="alert-box">{{ alert }}</div>

        <button class="submit-btn" :disabled="loading || form.items.length === 0" @click="submit">
          <span v-if="!loading">Đặt hàng ngay</span>
          <span v-else class="spinner-wrap">
            <span class="spinner" />
            Đang xử lý...
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { customersApi } from "../../api/customers.api";
import { cartApi } from "../../api/cart.api";
import { useCartStore } from "../../stores/cart";
import { toast } from "../../ui/toast";

const router = useRouter();
const cartStore = useCartStore();

const loading = ref(false);
const alert = ref("");
const deliveryMethod = ref("STORE");
const delivery = reactive({ address: "", note: "" });

const form = reactive({
  customerId: null,
  paymentMethod: "CASH",
  channel: "ONLINE",
  notes: "",
  items: [],
});

// ── Promo code state ──
const promoInput     = ref("");
const promoLoading   = ref(false);
const promoApplied   = ref(false);
const promoError     = ref("");
const promoResult    = ref(null); // { discountAmount, promotionName, ... }

const paymentOptions = [
  { value: "CASH",     label: "Tiền mặt",      icon: "💵" },
  { value: "TRANSFER", label: "Chuyển khoản",  icon: "🏦" },
  { value: "CARD",     label: "Thẻ tín dụng",  icon: "💳" },
];

// ── Computed ──
const subtotal = computed(() =>
  form.items.reduce((sum, i) => {
    const price = i.price ?? i.unitPrice ?? 0;
    return sum + Number(price) * (i.quantity ?? 1);
  }, 0)
);

const promoDiscount = computed(() =>
  promoApplied.value && promoResult.value
    ? Number(promoResult.value.discountAmount ?? 0)
    : 0
);

const totalAfterDiscount = computed(() =>
  Math.max(0, subtotal.value - promoDiscount.value)
);

// ── Promo handlers ──
async function handlePromo() {
  const code = promoInput.value.trim().toUpperCase();
  if (!code) return;

  promoError.value = "";
  promoLoading.value = true;

  try {
    const res = await ordersApi.validatePromoCode(code, subtotal.value);
    const data = res?.data ?? res;

    if (data?.valid) {
      promoApplied.value = true;
      promoResult.value  = data;
      toast(`✅ Mã "${code}" hợp lệ — giảm ${formatMoney(data.discountAmount)}`, "success");
    } else {
      promoError.value = data?.message || "Mã không hợp lệ";
      promoApplied.value = false;
      promoResult.value  = null;
    }
  } catch (e) {
    promoError.value = e?.response?.data?.message || "Không thể kiểm tra mã, thử lại sau";
    promoApplied.value = false;
    promoResult.value  = null;
  } finally {
    promoLoading.value = false;
  }
}

function clearPromo() {
  promoInput.value   = "";
  promoApplied.value = false;
  promoResult.value  = null;
  promoError.value   = "";
}

// ── Submit ──
function pickOrderId(payload) {
  const root = payload?.data ?? payload;
  return root?.id ?? root?.orderId ?? root?.data?.id ?? root?.data?.orderId ?? null;
}

function validateForm() {
  if (deliveryMethod.value === "HOME" && !delivery.address) {
    alert.value = "Vui lòng nhập địa chỉ giao hàng";
    return false;
  }
  return true;
}

function buildNotes() {
  let notes = form.notes || "";
  if (form.paymentMethod === "TRANSFER") notes += "\nThanh toán chuyển khoản";
  if (form.paymentMethod === "CARD")     notes += "\nThanh toán thẻ";
  if (deliveryMethod.value === "HOME") {
    notes += `\nGiao tại nhà\nĐịa chỉ: ${delivery.address}\nGhi chú: ${delivery.note}`;
  } else {
    notes += "\nNhận tại cửa hàng";
  }
  return notes;
}

async function submit() {
  alert.value = "";
  if (!validateForm()) return;

  loading.value = true;
  try {
    const payload = {
      customerId:    Number(form.customerId),
      paymentMethod: form.paymentMethod,
      channel:       form.channel,
      notes:         buildNotes(),
      items:         form.items,
    };

    // Gắn promotionCode nếu đã apply thành công
    if (promoApplied.value && promoInput.value.trim()) {
      payload.promotionCode = promoInput.value.trim().toUpperCase();
    }

    const res = await ordersApi.create(payload);
    const orderId = pickOrderId(res?.data);

    toast("Đặt hàng thành công! 🎉", "success");
    await cartStore.clearCart();

    if (orderId) {
      router.push(`/orders/${orderId}`);
    } else {
      toast("Không tìm thấy mã đơn hàng", "warning");
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "";
    alert.value = msg || "Đặt hàng thất bại";
  } finally {
    loading.value = false;
  }
}

// ── Mount ──
onMounted(async () => {
  try {
    const profile = await customersApi.getProfile();
    form.customerId = profile?.data?.id;

    const cart = await cartApi.getItems();
    form.items = cart?.data ?? [];
  } catch (e) {
    alert.value = "Không thể tải dữ liệu đơn hàng";
  }
});

function formatMoney(value) {
  if (value == null) return "—";
  return Number(value).toLocaleString("vi-VN") + " ₫";
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

.order-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  font-family: "Inter", sans-serif;
}

/* Header */
.order-header {
  display: flex; align-items: center; gap: 20px;
  margin-bottom: 32px; flex-wrap: wrap;
}
.back-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; border: 1.5px solid #E5E7EB; border-radius: 50px;
  background: #FFF; font-size: 13px; color: #374151;
  cursor: pointer; transition: all 0.15s; white-space: nowrap;
}
.back-btn:hover { border-color: #3B82F6; color: #3B82F6; background: #EFF6FF; }
.header-title { display: flex; flex-direction: column; gap: 2px; }
.step-badge {
  display: inline-block; font-size: 11px; font-weight: 700;
  text-transform: uppercase; letter-spacing: 0.08em;
  color: #3B82F6; background: #EFF6FF;
  padding: 2px 10px; border-radius: 50px; width: fit-content;
}
.header-title h1 { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0; }

/* Layout */
.order-layout {
  display: grid; grid-template-columns: 1fr 380px; gap: 24px; align-items: start;
}
@media (max-width: 768px) { .order-layout { grid-template-columns: 1fr; } }

/* Cards */
.section-card {
  background: #FFF; border: 1.5px solid #E5E7EB;
  border-radius: 16px; padding: 24px; margin-bottom: 16px;
}
.section-label {
  display: flex; align-items: center; gap: 7px;
  font-size: 12px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.07em; color: #64748B; margin-bottom: 18px;
}

/* Empty cart */
.empty-cart { text-align: center; padding: 40px 0 20px; color: #94A3B8; }
.empty-icon { font-size: 40px; margin-bottom: 10px; }
.empty-cart p { font-size: 14px; margin-bottom: 16px; }
.btn-outline {
  padding: 8px 20px; border: 1.5px solid #3B82F6; border-radius: 50px;
  color: #3B82F6; background: #FFF; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.15s;
}
.btn-outline:hover { background: #EFF6FF; }

/* Items */
.items-list { display: flex; flex-direction: column; gap: 12px; }
.item-row {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 16px; background: #F8FAFC;
  border-radius: 12px; border: 1px solid #F1F5F9; transition: border-color 0.15s;
}
.item-row:hover { border-color: #BFDBFE; }
.item-icon {
  width: 40px; height: 40px; background: #EFF6FF; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #3B82F6; flex-shrink: 0; font-size: 18px;
}
.item-info { flex: 1; min-width: 0; }
.item-name { font-size: 14px; font-weight: 600; color: #0F172A; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-meta { font-size: 12px; color: #94A3B8; margin-top: 2px; }
.item-qty { display: flex; flex-direction: column; align-items: center; gap: 1px; flex-shrink: 0; }
.qty-label { font-size: 10px; font-weight: 700; text-transform: uppercase; color: #94A3B8; letter-spacing: 0.06em; }
.qty-value { font-size: 18px; font-weight: 800; color: #0F172A; line-height: 1; }

/* Payment options */
.payment-options { display: flex; flex-direction: column; gap: 10px; }
.payment-opt {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; border: 2px solid #F1F5F9;
  border-radius: 12px; cursor: pointer; transition: all 0.15s; position: relative;
}
.payment-opt input { display: none; }
.payment-opt:hover { border-color: #BFDBFE; background: #F8FAFF; }
.payment-opt.active { border-color: #3B82F6; background: #EFF6FF; }
.opt-icon { font-size: 20px; }
.opt-label { font-size: 14px; font-weight: 600; color: #0F172A; flex: 1; }
.check-mark {
  font-size: 13px; font-weight: 800; color: white; background: #3B82F6;
  width: 22px; height: 22px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}

/* ── Promo code ── */
.promo-row { display: flex; gap: 8px; align-items: stretch; }

.promo-input-wrap {
  flex: 1; display: flex; align-items: center;
  background: #F8FAFC; border: 1.5px solid #E5E7EB;
  border-radius: 12px; overflow: hidden; transition: all 0.2s;
}
.promo-input-wrap:focus-within { border-color: #3B82F6; background: #FFF; box-shadow: 0 0 0 3px rgba(59,130,246,.1); }
.promo-input-wrap.promo-applied { border-color: #10B981; background: #F0FDF4; }
.promo-input-wrap.promo-error   { border-color: #EF4444; background: #FFF5F5; }

.promo-prefix { padding: 0 10px; font-size: 15px; flex-shrink: 0; }
.promo-input {
  flex: 1; border: none; background: transparent;
  font-family: "Inter", sans-serif; font-size: 13.5px; font-weight: 600;
  color: #0F172A; outline: none; padding: 11px 0;
  letter-spacing: 0.04em; text-transform: uppercase;
}
.promo-input::placeholder { text-transform: none; font-weight: 400; color: #94A3B8; }
.promo-input:disabled { color: #10B981; }

.promo-clear-btn {
  padding: 0 12px; background: none; border: none;
  color: #6EE7B7; cursor: pointer; transition: color 0.15s;
  display: flex; align-items: center;
}
.promo-clear-btn:hover { color: #059669; }

.promo-btn {
  padding: 0 18px; border-radius: 12px; border: none;
  background: #0F172A; color: white;
  font-family: "Inter", sans-serif; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; white-space: nowrap;
  display: flex; align-items: center; gap: 6px; min-width: 100px; justify-content: center;
}
.promo-btn:hover:not(:disabled) { background: #1E293B; transform: translateY(-1px); }
.promo-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
.promo-btn--applied { background: #10B981 !important; opacity: 1 !important; cursor: default !important; }
.promo-btn--loading { opacity: 0.8; }

.promo-msg {
  display: flex; align-items: center; gap: 6px;
  font-size: 12.5px; font-weight: 500; margin-top: 10px;
  padding: 8px 12px; border-radius: 8px;
}
.promo-msg--success { background: #F0FDF4; color: #059669; border: 1px solid #A7F3D0; }
.promo-msg--error   { background: #FEF2F2; color: #DC2626; border: 1px solid #FECACA; }
.promo-name-tag {
  display: inline-block; font-size: 10px; font-weight: 700;
  background: #D1FAE5; color: #059669; padding: 1px 7px;
  border-radius: 20px; margin-left: 6px; letter-spacing: 0.04em;
}

/* Summary card */
.summary-card { background: #FAFBFC; }
.summary-lines { display: flex; flex-direction: column; gap: 10px; }
.summary-line { display: flex; justify-content: space-between; font-size: 13.5px; color: #374151; }
.summary-line--discount { color: #059669; font-weight: 500; }
.summary-line--discount code {
  font-family: "DM Mono", monospace; font-size: 11px;
  background: #D1FAE5; padding: 1px 6px; border-radius: 4px;
}
.discount-val { font-weight: 700; }
.summary-line--muted span { color: #94A3B8; font-size: 13px; }
.summary-divider { height: 1px; background: #E5E7EB; margin: 12px 0; }
.summary-total { display: flex; justify-content: space-between; align-items: baseline; font-weight: 700; font-size: 14px; }
.summary-total-amount { font-size: 22px; font-weight: 800; color: #3B82F6; }

/* Notes */
.notes-input {
  width: 100%; border: 1.5px solid #E5E7EB; border-radius: 12px;
  padding: 12px 14px; font-size: 14px; font-family: "Inter", sans-serif;
  resize: none; color: #374151; transition: border-color 0.15s; box-sizing: border-box;
}
.notes-input:focus { outline: none; border-color: #3B82F6; }
.notes-input::placeholder { color: #CBD5E1; }

/* Alert */
.alert-box {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 16px; background: #FEF2F2;
  border: 1px solid #FECACA; border-radius: 12px;
  font-size: 13px; color: #DC2626; margin-bottom: 16px;
}

/* Submit */
.submit-btn {
  width: 100%; padding: 16px;
  background: linear-gradient(135deg, #1E293B, #0F172A);
  color: white; border: none; border-radius: 14px;
  font-size: 15px; font-weight: 700; font-family: "Inter", sans-serif;
  cursor: pointer; transition: all 0.2s;
  display: flex; align-items: center; justify-content: center; gap: 8px;
}
.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(59,130,246,.3);
}
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

.spinner-wrap { display: flex; align-items: center; gap: 8px; }
.spinner {
  width: 15px; height: 15px;
  border: 2px solid rgba(255,255,255,.3);
  border-top-color: white; border-radius: 50%;
  animation: spin 0.7s linear infinite; flex-shrink: 0;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Form */
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }
.form-group label { font-size: 12px; font-weight: 600; color: #64748B; }
.form-input {
  width: 100%; border: 1.5px solid #E5E7EB; border-radius: 10px;
  padding: 10px 12px; font-size: 14px; font-family: "Inter", sans-serif;
  transition: border-color 0.15s; box-sizing: border-box;
}
.form-input:focus { outline: none; border-color: #3B82F6; }
</style>