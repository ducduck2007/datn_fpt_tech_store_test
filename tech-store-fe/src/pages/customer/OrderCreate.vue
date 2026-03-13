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
import "../../assets/styles/order-create.css";

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
