<template>
  <div class="checkout-root">

    <!-- ── MAIN LAYOUT ───────────────────────────────────────────── -->
    <div class="checkout-body">

      <!-- ══ LEFT COLUMN ══════════════════════════════════════════ -->
      <div class="checkout-left">

        <!-- CART ITEMS -->
        <div class="co-card">
          <div class="co-card-head">
            <div class="co-card-head-icon">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 001.95-1.57L23 6H6"/>
              </svg>
            </div>
            <h2 class="co-card-title">Sản phẩm đã chọn</h2>
            <span v-if="form.items.length > 0" class="co-badge">{{ form.items.length }}</span>
          </div>

          <div v-if="form.items.length === 0" class="cart-empty">
            <div class="cart-empty-icon">🛒</div>
            <p class="cart-empty-text">Giỏ hàng của bạn đang trống</p>
            <button class="btn-go-shop" @click="$router.push('/')">Khám phá ngay</button>
          </div>

          <div v-else class="cart-items">
            <div v-for="(item, idx) in form.items" :key="idx" class="cart-item" :style="`--delay:${idx * 60}ms`">
              <div class="cart-item-thumb">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.35">
                  <rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/>
                </svg>
              </div>
              <div class="cart-item-info">
                <div class="cart-item-name">{{ item.productName }}</div>
                <div class="cart-item-sub">Variant #{{ item.variantId }}</div>
              </div>
              <div class="cart-item-right">
                <div class="cart-item-qty">
                  <span class="qty-x">×</span>
                  <span class="qty-num">{{ item.quantity }}</span>
                </div>
                <div class="cart-item-price">{{ formatMoney((item.price ?? item.unitPrice ?? 0) * (item.quantity ?? 1)) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- DELIVERY METHOD -->
        <div class="co-card">
          <div class="co-card-head">
            <div class="co-card-head-icon">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <rect x="1" y="3" width="15" height="13" rx="1"/>
                <path d="M16 8h4l3 3v5h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
              </svg>
            </div>
            <h2 class="co-card-title">Hình thức giao hàng</h2>
          </div>

          <div class="delivery-options">
            <label class="delivery-opt" :class="{ 'delivery-opt--active': deliveryMethod === 'STORE' }">
              <input type="radio" value="STORE" v-model="deliveryMethod" class="sr-only"/>
              <div class="delivery-opt-radio">
                <div class="radio-dot" v-if="deliveryMethod === 'STORE'"></div>
              </div>
              <div class="delivery-opt-icon">🏬</div>
              <div class="delivery-opt-body">
                <div class="delivery-opt-title">Nhận tại cửa hàng</div>
                <div class="delivery-opt-desc">Đến nhận trực tiếp tại quầy — không mất phí</div>
              </div>
              <div class="delivery-opt-price free-label">Miễn phí</div>
            </label>

            <label class="delivery-opt" :class="{ 'delivery-opt--active': deliveryMethod === 'HOME' }">
              <input type="radio" value="HOME" v-model="deliveryMethod" class="sr-only"/>
              <div class="delivery-opt-radio">
                <div class="radio-dot" v-if="deliveryMethod === 'HOME'"></div>
              </div>
              <div class="delivery-opt-icon">🚚</div>
              <div class="delivery-opt-body">
                <div class="delivery-opt-title">Giao tại nhà</div>
                <div class="delivery-opt-desc">Giao hàng nhanh 2–4 giờ trong nội thành</div>
              </div>
              <div class="delivery-opt-price">+70.000 ₫</div>
            </label>
          </div>
        </div>

        <!-- NOTE -->
        <div class="co-card">
          <div class="co-card-head">
            <div class="co-card-head-icon">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
            </div>
            <h2 class="co-card-title">Ghi chú đơn hàng</h2>
            <span class="co-optional">Tùy chọn</span>
          </div>
          <textarea
            v-model="form.notes"
            class="note-textarea"
            rows="3"
            placeholder="Ví dụ: Gọi trước khi giao, để hàng ở bảo vệ..."
          />
        </div>

      </div>

      <!-- ══ RIGHT COLUMN ══════════════════════════════════════════ -->
      <div class="checkout-right">

        <!-- PAYMENT METHOD -->
        <div class="co-card">
          <div class="co-card-head">
            <div class="co-card-head-icon">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <rect x="1" y="4" width="22" height="16" rx="2"/>
                <line x1="1" y1="10" x2="23" y2="10"/>
              </svg>
            </div>
            <h2 class="co-card-title">Phương thức thanh toán</h2>
          </div>

          <div class="payment-methods">
            <label
              v-for="opt in paymentOptions"
              :key="opt.value"
              class="pm-opt"
              :class="{ 'pm-opt--active': form.paymentMethod === opt.value }"
            >
              <input type="radio" :value="opt.value" v-model="form.paymentMethod" class="sr-only"/>
              <div class="pm-opt-radio">
                <div class="radio-dot" v-if="form.paymentMethod === opt.value"></div>
              </div>
              <span class="pm-opt-icon">{{ opt.icon }}</span>
              <span class="pm-opt-label">{{ opt.label }}</span>
              <span v-if="form.paymentMethod === opt.value" class="pm-check">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </span>
            </label>
          </div>

          <transition name="slide-fade">
            <div v-if="form.paymentMethod === 'TRANSFER'" class="payment-notice">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              Mã QR thanh toán sẽ hiển thị sau khi đặt hàng
            </div>
          </transition>
          <transition name="slide-fade">
            <div v-if="form.paymentMethod === 'CARD'" class="payment-notice">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              Thông tin thẻ sẽ được nhập ở bước tiếp theo
            </div>
          </transition>
        </div>

        <!-- PROMO CODE -->
        <div class="co-card">
          <div class="co-card-head">
            <div class="co-card-head-icon">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                <line x1="7" y1="7" x2="7.01" y2="7"/>
              </svg>
            </div>
            <h2 class="co-card-title">Mã khuyến mãi</h2>
          </div>

          <div class="promo-wrap">
            <div class="promo-field" :class="{ 'promo-field--applied': promoApplied, 'promo-field--error': promoError }">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="promo-field-icon">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                <line x1="7" y1="7" x2="7.01" y2="7"/>
              </svg>
              <input
                v-model="promoInput"
                class="promo-field-input"
                placeholder="Nhập mã giảm giá..."
                :disabled="promoApplied"
                @keydown.enter="handlePromo"
                @input="promoError = ''"
              />
              <button v-if="promoApplied" class="promo-clear" @click="clearPromo">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <button
              class="promo-apply-btn"
              :class="{ 'promo-apply-btn--applied': promoApplied, 'promo-apply-btn--loading': promoLoading }"
              :disabled="promoLoading || promoApplied || !promoInput.trim()"
              @click="handlePromo"
            >
              <svg v-if="promoLoading" class="spin-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M21 12a9 9 0 11-6.219-8.56"/>
              </svg>
              <span v-else-if="promoApplied">✓ Đã áp dụng</span>
              <span v-else>Áp dụng</span>
            </button>
          </div>

          <transition name="msg-fade">
            <div v-if="promoError" class="promo-status promo-status--err">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
              </svg>
              {{ promoError }}
            </div>
          </transition>
          <transition name="msg-fade">
            <div v-if="promoApplied && promoResult" class="promo-status promo-status--ok">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              Tiết kiệm <strong>{{ formatMoney(promoResult.discountAmount) }}</strong>
              <span v-if="promoResult.promotionName" class="promo-name">{{ promoResult.promotionName }}</span>
            </div>
          </transition>
        </div>

        <!-- ORDER SUMMARY -->
        <div class="co-card summary-card">
          <div class="co-card-head">
            <div class="co-card-head-icon">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/>
                <line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>
              </svg>
            </div>
            <h2 class="co-card-title">Tóm tắt đơn hàng</h2>
          </div>

          <div class="summary-rows">
            <div class="summary-row">
              <span>Tạm tính <span class="summary-count">({{ form.items.length }} sản phẩm)</span></span>
              <span>{{ formatMoney(subtotal) }}</span>
            </div>
            <transition name="row-slide">
              <div v-if="promoApplied && promoResult" class="summary-row summary-row--discount">
                <span>
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="vertical-align:-1px">
                    <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                  </svg>
                  Mã <code class="promo-code-inline">{{ promoInput.toUpperCase() }}</code>
                </span>
                <span class="discount-amount">−{{ formatMoney(promoResult.discountAmount) }}</span>
              </div>
            </transition>
            <div class="summary-row" :class="deliveryMethod === 'HOME' ? 'summary-row--ship' : 'summary-row--free'">
              <span>{{ deliveryMethod === 'HOME' ? '🚚 Phí vận chuyển' : '🏬 Nhận tại cửa hàng' }}</span>
              <span v-if="deliveryMethod === 'HOME'" class="ship-fee">+{{ formatMoney(70000) }}</span>
              <span v-else class="free-chip">Miễn phí</span>
            </div>
          </div>

          <div class="summary-sep"></div>

          <div class="summary-total-row">
            <div>
              <div class="summary-total-label">Tổng thanh toán</div>
              <div class="summary-total-note">Đã bao gồm VAT (nếu có)</div>
            </div>
            <div class="summary-total-amount">{{ formatMoney(totalAfterDiscount) }}</div>
          </div>

          <transition name="msg-fade">
            <div v-if="promoApplied && promoResult" class="savings-banner">
              🎉 Bạn tiết kiệm được <strong>{{ formatMoney(promoResult.discountAmount) }}</strong> cho đơn hàng này!
            </div>
          </transition>
        </div>

        

        <!-- ERROR -->
        <transition name="msg-fade">
          <div v-if="alert" class="alert-error">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {{ alert }}
          </div>
        </transition>

        <!-- SUBMIT -->
        <button
          class="submit-btn"
          :class="{ 'submit-btn--loading': loading, 'submit-btn--disabled': form.items.length === 0 }"
          :disabled="loading || form.items.length === 0"
          @click="submit"
        >
          <template v-if="!loading">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
            Đặt hàng ngay · {{ formatMoney(totalAfterDiscount) }}
          </template>
          <template v-else>
            <svg class="spin-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M21 12a9 9 0 11-6.219-8.56"/>
            </svg>
            Đang xử lý đơn hàng...
          </template>
        </button>



      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
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

const form = reactive({
  customerId: null,
  paymentMethod: "CASH",
  channel: "ONLINE",
  notes: "",
  items: [],
});

const promoInput   = ref("");
const promoLoading = ref(false);
const promoApplied = ref(false);
const promoError   = ref("");
const promoResult  = ref(null);

const ALL_PAYMENT_OPTIONS = [
  { value: "CASH",     label: "Tiền mặt",     icon: "💵" },
  { value: "TRANSFER", label: "Chuyển khoản", icon: "🏦" },
  { value: "CARD",     label: "Thẻ tín dụng", icon: "💳" },
];

// Ẩn Tiền mặt khi chọn Giao tại nhà
const paymentOptions = computed(() =>
  deliveryMethod.value === "HOME"
    ? ALL_PAYMENT_OPTIONS.filter(o => o.value !== "CASH")
    : ALL_PAYMENT_OPTIONS
);

// Khi chuyển sang Giao tại nhà: đặt mặc định là Chuyển khoản
watch(deliveryMethod, (val) => {
  if (val === "HOME" && form.paymentMethod === "CASH") {
    form.paymentMethod = "TRANSFER";
  }
});

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

const shippingFee = computed(() => deliveryMethod.value === "HOME" ? 70000 : 0);

const totalAfterDiscount = computed(() =>
  Math.max(0, subtotal.value - promoDiscount.value + shippingFee.value)
);

function buildNotes() {
  const parts = [];
  if (form.notes?.trim()) parts.push(form.notes.trim());
  if (form.paymentMethod === "TRANSFER") parts.push("Thanh toán chuyển khoản");
  else if (form.paymentMethod === "CARD") parts.push("Thanh toán thẻ tín dụng");
  parts.push(deliveryMethod.value === "HOME" ? "Giao tại nhà" : "Nhận tại cửa hàng");
  return parts.join(" | ");
}

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
      promoError.value   = data?.message || "Mã không hợp lệ";
      promoApplied.value = false;
      promoResult.value  = null;
    }
  } catch (e) {
    promoError.value   = e?.response?.data?.message || "Không thể kiểm tra mã, thử lại sau";
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

function pickOrderId(payload) {
  const root = payload?.data ?? payload;
  return root?.id ?? root?.orderId ?? root?.data?.id ?? root?.data?.orderId ?? null;
}

async function submit() {
  alert.value = "";
  loading.value = true;
  try {
    const payload = {
      customerId:    Number(form.customerId),
      paymentMethod: form.paymentMethod,
      channel:       form.channel,
      notes:         buildNotes(),
      items:         form.items,
      shippingFee:   shippingFee.value,
    };
    if (promoApplied.value && promoInput.value.trim()) {
      payload.promotionCode = promoInput.value.trim().toUpperCase();
    }
    const res = await ordersApi.create(payload);
    const orderId = pickOrderId(res?.data);
    toast("Đặt hàng thành công! 🎉", "success");
    await cartStore.clearCart();
    if (orderId) router.push(`/orders/${orderId}`);
    else toast("Không tìm thấy mã đơn hàng", "warning");
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "";
    alert.value = msg || "Đặt hàng thất bại";
  } finally {
    loading.value = false;
  }
}

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
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;0,800;1,400&display=swap");

/* ── VARIABLES ─────────────────────────────────────────────────── */
.checkout-root {
  --em:     #1d6fd8;
  --em-dk:  #1558b0;
  --em-lt:  #eff4fe;
  --em-md:  #a8c8f8;
  --ink:    #111827;
  --ink2:   #374151;
  --ink3:   #6b7280;
  --ink4:   #9ca3af;
  --ink5:   #d1d5db;
  --bg:     #f1f3f6;
  --card:   #ffffff;
  --border: #e2e6ea;
  --red:    #dc2626;
  --amber:  #d97706;
  --rad:    14px;
  --rad-sm: 10px;
  --sha:    0 1px 2px rgba(0,0,0,0.05), 0 3px 12px rgba(0,0,0,0.04);

  min-height: 100vh;
  background: var(--bg);
  font-family: "Plus Jakarta Sans", sans-serif;
  color: var(--ink);
}

*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }



/* ── BODY ──────────────────────────────────────────────────────── */
.checkout-body {
  max-width: 1140px; margin: 0 auto;
  padding: 28px 24px 64px;
  display: grid;
  grid-template-columns: 1fr 396px;
  gap: 20px;
  align-items: start;
}

/* ── CARD ──────────────────────────────────────────────────────── */
.co-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--rad);
  padding: 22px 24px;
  margin-bottom: 14px;
  box-shadow: var(--sha);
}
.co-card:last-of-type { margin-bottom: 0; }

.co-card-head {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 18px;
}
.co-card-head-icon {
  width: 34px; height: 34px;
  background: var(--em-lt); border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  color: var(--em); flex-shrink: 0;
}
.co-card-title { font-size: 14.5px; font-weight: 700; color: var(--ink); flex: 1; }
.co-badge {
  padding: 2px 9px; border-radius: 20px;
  background: var(--em); color: #fff;
  font-size: 11.5px; font-weight: 700;
}
.co-optional { font-size: 11.5px; color: var(--ink4); font-weight: 500; }

/* ── CART ──────────────────────────────────────────────────────── */
.cart-empty { text-align: center; padding: 28px 0; }
.cart-empty-icon { font-size: 44px; margin-bottom: 12px; }
.cart-empty-text { font-size: 14px; color: var(--ink3); margin-bottom: 16px; }
.btn-go-shop {
  padding: 10px 26px; background: var(--em); color: #fff;
  border: none; border-radius: 8px; font-size: 13.5px;
  font-family: "Plus Jakarta Sans", sans-serif; font-weight: 600;
  cursor: pointer; transition: background 0.15s;
}
.btn-go-shop:hover { background: var(--em-dk); }

.cart-items { display: flex; flex-direction: column; gap: 8px; }

.cart-item {
  display: flex; align-items: center; gap: 14px;
  padding: 13px 14px; border-radius: var(--rad-sm);
  border: 1px solid var(--border); background: #f8f9fb;
  transition: border-color 0.15s, box-shadow 0.15s;
  animation: item-in 0.28s ease both;
  animation-delay: var(--delay, 0ms);
}
@keyframes item-in { from { opacity:0; transform:translateY(5px); } to { opacity:1; transform:none; } }
.cart-item:hover { border-color: var(--em-md); box-shadow: 0 2px 10px rgba(13,122,85,0.06); }

.cart-item-thumb {
  width: 50px; height: 50px; flex-shrink: 0;
  background: var(--card); border: 1px solid var(--border);
  border-radius: 8px; display: flex; align-items: center; justify-content: center;
}
.cart-item-info { flex: 1; min-width: 0; }
.cart-item-name { font-size: 13.5px; font-weight: 600; color: var(--ink); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cart-item-sub  { font-size: 11.5px; color: var(--ink4); margin-top: 2px; }
.cart-item-right { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; flex-shrink: 0; }
.cart-item-qty  { display: flex; align-items: center; gap: 3px; }
.qty-x   { font-size: 11px; color: var(--ink4); }
.qty-num { font-size: 13px; font-weight: 600; color: var(--ink2); }
.cart-item-price { font-size: 14px; font-weight: 700; color: var(--em); }

/* ── DELIVERY ──────────────────────────────────────────────────── */
.delivery-options { display: flex; flex-direction: column; gap: 10px; }

.delivery-opt {
  display: flex; align-items: center; gap: 14px;
  padding: 15px 16px; border-radius: var(--rad-sm);
  border: 2px solid var(--border);
  cursor: pointer; background: var(--card);
  transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;
}
.delivery-opt:hover { border-color: var(--em-md); background: var(--em-lt); }
.delivery-opt--active { border-color: var(--em); background: var(--em-lt); box-shadow: 0 0 0 3px rgba(13,122,85,0.07); }

.delivery-opt-radio {
  width: 18px; height: 18px; border-radius: 50%;
  border: 2px solid var(--ink5); flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  transition: border-color 0.15s;
}
.delivery-opt--active .delivery-opt-radio { border-color: var(--em); }
.radio-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--em); }
.delivery-opt-icon { font-size: 22px; }
.delivery-opt-body { flex: 1; }
.delivery-opt-title { font-size: 13.5px; font-weight: 600; color: var(--ink); }
.delivery-opt-desc  { font-size: 12px; color: var(--ink3); margin-top: 2px; }
.delivery-opt-price { font-size: 13.5px; font-weight: 700; color: var(--ink2); flex-shrink: 0; }
.free-label { color: var(--em); }

/* ── NOTE ──────────────────────────────────────────────────────── */
.note-textarea {
  width: 100%; min-height: 82px; padding: 12px 14px; resize: vertical;
  border: 1.5px solid var(--border); border-radius: var(--rad-sm);
  font-family: "Plus Jakarta Sans", sans-serif; font-size: 13.5px;
  color: var(--ink); outline: none; background: #f8f9fb;
  transition: border-color 0.15s, box-shadow 0.15s; line-height: 1.6;
}
.note-textarea::placeholder { color: var(--ink4); }
.note-textarea:focus { border-color: var(--em); background: var(--card); box-shadow: 0 0 0 3px rgba(29,111,216,0.1); }

/* ── PAYMENT ───────────────────────────────────────────────────── */
.payment-methods { display: flex; flex-direction: column; gap: 8px; }

.pm-opt {
  display: flex; align-items: center; gap: 12px;
  padding: 13px 14px; border-radius: var(--rad-sm);
  border: 2px solid var(--border); cursor: pointer;
  background: var(--card); transition: all 0.15s;
}
.pm-opt:hover { border-color: var(--em-md); background: var(--em-lt); }
.pm-opt--active { border-color: var(--em); background: var(--em-lt); box-shadow: 0 0 0 3px rgba(13,122,85,0.07); }

.pm-opt-radio {
  width: 18px; height: 18px; border-radius: 50%;
  border: 2px solid var(--ink5); flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  transition: border-color 0.15s;
}
.pm-opt--active .pm-opt-radio { border-color: var(--em); }
.pm-opt-icon  { font-size: 18px; flex-shrink: 0; }
.pm-opt-label { font-size: 13.5px; font-weight: 500; color: var(--ink); flex: 1; }
.pm-check {
  width: 20px; height: 20px; border-radius: 50%;
  background: var(--em); color: #fff;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}

.payment-notice {
  display: flex; align-items: center; gap: 7px;
  margin-top: 12px; padding: 10px 13px; border-radius: 8px;
  font-size: 12.5px; font-weight: 500;
  background: #eff6ff; border: 1px solid #bfdbfe; color: #1d4ed8;
}

/* ── PROMO ─────────────────────────────────────────────────────── */
.promo-wrap { display: flex; gap: 8px; }

.promo-field {
  flex: 1; display: flex; align-items: center; gap: 8px;
  padding: 0 12px; height: 44px;
  border: 1.5px solid var(--border); border-radius: var(--rad-sm);
  background: #f8f9fb; transition: all 0.15s;
}
.promo-field:focus-within { border-color: var(--em); background: var(--card); box-shadow: 0 0 0 3px rgba(29,111,216,0.1); }
.promo-field--applied { border-color: var(--em); background: var(--em-lt); }
.promo-field--error   { border-color: var(--red); background: #fff5f5; }

.promo-field-icon { color: var(--ink4); flex-shrink: 0; }
.promo-field-input {
  flex: 1; border: none; background: transparent; outline: none;
  font-family: "Plus Jakarta Sans", sans-serif; font-size: 13.5px; color: var(--ink);
  font-weight: 600; letter-spacing: 0.05em; text-transform: uppercase;
}
.promo-field-input::placeholder { text-transform: none; font-weight: 400; color: var(--ink4); }
.promo-field-input:disabled { opacity: 0.7; }

.promo-clear {
  width: 22px; height: 22px; border-radius: 50%;
  background: var(--ink5); border: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: var(--ink3); transition: background 0.15s; flex-shrink: 0;
}
.promo-clear:hover { background: var(--ink4); color: #fff; }

.promo-apply-btn {
  height: 44px; padding: 0 20px; flex-shrink: 0;
  border: 1.5px solid var(--em); border-radius: var(--rad-sm);
  background: var(--em); color: #fff;
  font-family: "Plus Jakarta Sans", sans-serif; font-size: 13.5px; font-weight: 600;
  cursor: pointer; display: flex; align-items: center; gap: 6px;
  transition: all 0.15s; white-space: nowrap;
}
.promo-apply-btn:hover:not(:disabled) { background: var(--em-dk); border-color: var(--em-dk); }
.promo-apply-btn:disabled:not(.promo-apply-btn--applied) { opacity: 0.45; cursor: not-allowed; }
/* Đã áp dụng — outline style giống screenshot */
.promo-apply-btn--applied {
  background: #fff;
  color: var(--em);
  border-color: var(--em);
  font-weight: 600;
  cursor: default;
  pointer-events: none;
}

.promo-status { display: flex; align-items: center; gap: 6px; margin-top: 8px; font-size: 12.5px; font-weight: 500; }
.promo-status--ok  { color: var(--em); }
.promo-status--err { color: var(--red); }
.promo-name {
  padding: 1px 8px; border-radius: 20px;
  background: var(--em-lt); border: 1px solid var(--em-md);
  font-size: 11px; font-weight: 700; color: var(--em); margin-left: 4px;
}

/* ── SUMMARY ───────────────────────────────────────────────────── */
.summary-rows { display: flex; flex-direction: column; }
.summary-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 0; font-size: 13.5px; color: var(--ink2);
  border-bottom: 1px dashed var(--border);
}
.summary-row:last-child { border-bottom: none; }
.summary-count { font-size: 12px; color: var(--ink4); }
.summary-row--discount { color: var(--em); }
.discount-amount { color: var(--em); font-weight: 700; }
.promo-code-inline {
  padding: 1px 7px; background: var(--em-lt); border-radius: 4px;
  font-family: monospace; font-size: 12px; color: var(--em); font-weight: 700;
}
.summary-row--free { color: var(--em); }
.free-chip {
  padding: 2px 10px; border-radius: 20px;
  background: var(--em-lt); border: 1px solid var(--em-md);
  font-size: 11.5px; font-weight: 700; color: var(--em);
}
.summary-row--ship { color: var(--amber); }
.ship-fee { font-weight: 700; color: var(--amber); }

.summary-sep { height: 1px; background: var(--border); margin: 14px 0; }

.summary-total-row { display: flex; justify-content: space-between; align-items: center; }
.summary-total-label { font-size: 14px; font-weight: 700; color: var(--ink); }
.summary-total-note  { font-size: 11.5px; color: var(--ink4); margin-top: 2px; }
.summary-total-amount { font-size: 27px; font-weight: 800; color: var(--em); letter-spacing: -0.02em; }

.savings-banner {
  margin-top: 14px; padding: 10px 14px;
  background: linear-gradient(135deg, #fef9c3, #fef3c7);
  border: 1px solid #fcd34d; border-radius: 8px;
  font-size: 13px; color: #92400e; font-weight: 500;
}

/* ── SUBMIT ────────────────────────────────────────────────────── */
.submit-btn {
  width: 100%; height: 54px;
  background: var(--em); border: none; border-radius: var(--rad-sm);
  color: #fff; cursor: pointer;
  font-family: "Plus Jakarta Sans", sans-serif; font-size: 15px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; gap: 10px;
  transition: all 0.2s cubic-bezier(0.4,0,0.2,1);
  box-shadow: 0 4px 18px rgba(29,111,216,0.28);
  letter-spacing: 0.01em;
  margin-top: 15px;
}
.submit-btn:hover:not(:disabled) { background: var(--em-dk); transform: translateY(-1px); box-shadow: 0 6px 24px rgba(29,111,216,0.35); }
.submit-btn:active:not(:disabled) { transform: translateY(0); }
.submit-btn--disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none; }
.submit-btn--loading  { opacity: 0.8; cursor: not-allowed; }



/* ── ALERT ─────────────────────────────────────────────────────── */
.alert-error {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 14px; margin-bottom: 12px;
  background: #fef2f2; border: 1px solid #fecaca;
  border-radius: var(--rad-sm); color: var(--red); font-size: 13px; font-weight: 500;
}

/* ── ANIMATIONS ────────────────────────────────────────────────── */
.spin-icon { animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.slide-fade-enter-active { transition: all 0.22s ease; }
.slide-fade-enter-from   { opacity: 0; transform: translateY(-5px); }
.slide-fade-leave-active { transition: all 0.15s ease; }
.slide-fade-leave-to     { opacity: 0; }

.msg-fade-enter-active { transition: all 0.2s ease; }
.msg-fade-enter-from   { opacity: 0; transform: translateY(4px); }
.msg-fade-leave-active { transition: all 0.15s ease; }
.msg-fade-leave-to     { opacity: 0; }

.row-slide-enter-active { transition: opacity 0.25s ease; }
.row-slide-enter-from   { opacity: 0; }
.row-slide-leave-active { transition: opacity 0.15s ease; }
.row-slide-leave-to     { opacity: 0; }

/* ── UTILS ─────────────────────────────────────────────────────── */
.sr-only { position: absolute; width:1px; height:1px; overflow:hidden; clip:rect(0,0,0,0); }

/* ── RESPONSIVE ────────────────────────────────────────────────── */
@media (max-width: 880px) {
  .checkout-body { grid-template-columns: 1fr; }
  .checkout-right { order: -1; }
  .summary-total-amount { font-size: 23px; }
}
@media (max-width: 600px) {
  .checkout-topbar { padding: 0 16px; }
  .checkout-steps  { display: none; }
  .checkout-body   { padding: 14px 14px 48px; }
  .co-card         { padding: 18px; }
  .trust-badges    { flex-wrap: wrap; gap: 10px; justify-content: flex-start; }
}
</style>