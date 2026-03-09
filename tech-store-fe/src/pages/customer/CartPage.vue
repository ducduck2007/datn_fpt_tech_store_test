<!-- FILE: src/pages/customer/CustomerCart.vue -->
<template>
  <div class="cart-root">
    <!-- Background -->
    <div class="cart-bg">
      <div class="cart-bg__orb cart-bg__orb--1"></div>
      <div class="cart-bg__orb cart-bg__orb--2"></div>
      <div class="cart-bg__noise"></div>
    </div>

    <div class="cart-wrap">
      <!-- Header -->
      <div class="cart-header">
        <div>
          <div class="cart-header__eyebrow">Giỏ hàng</div>
          <h1 class="cart-header__title">
            Shopping Cart
            <span v-if="items.length" class="cart-header__count">{{
              items.length
            }}</span>
          </h1>
        </div>
        <button class="back-btn" @click="$router.back()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path
              d="M19 12H5M12 5l-7 7 7 7"
              stroke="currentColor"
              stroke-width="1.8"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          Tiếp tục mua
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="cart-skeleton">
        <div
          v-for="i in 3"
          :key="i"
          class="cart-skeleton__row"
          :style="{ '--d': i * 0.08 + 's' }"
        >
          <div class="sk sk--img"></div>
          <div class="sk-lines">
            <div class="sk sk--long"></div>
            <div class="sk sk--short"></div>
          </div>
          <div class="sk sk--price"></div>
          <div class="sk sk--qty"></div>
          <div class="sk sk--sub"></div>
          <div class="sk sk--btn"></div>
        </div>
      </div>

      <!-- Empty -->
      <div v-else-if="items.length === 0" class="cart-empty">
        <div class="cart-empty__bag">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
            <path
              d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"
              stroke="currentColor"
              stroke-width="1.2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <line
              x1="3"
              y1="6"
              x2="21"
              y2="6"
              stroke="currentColor"
              stroke-width="1.2"
              stroke-linecap="round"
            />
            <path
              d="M16 10a4 4 0 0 1-8 0"
              stroke="currentColor"
              stroke-width="1.2"
              stroke-linecap="round"
            />
          </svg>
        </div>
        <div class="cart-empty__title">Giỏ hàng trống</div>
        <div class="cart-empty__sub">Hãy thêm sản phẩm vào giỏ để tiếp tục</div>
        <button class="btn-shop-now" @click="$router.push('/')">
          Khám phá sản phẩm
        </button>
      </div>

      <!-- Cart content -->
      <div v-else class="cart-content">
        <!-- Left: items list -->
        <div class="cart-items">
          <!-- Table head -->
          <div class="cart-table-head">
            <span class="th-product">Sản phẩm</span>
            <span class="th-price">Đơn giá</span>
            <span class="th-qty">Số lượng</span>
            <span class="th-sub">Thành tiền</span>
            <span class="th-del"></span>
          </div>

          <!-- Items -->
          <transition-group name="item-list" tag="div" class="cart-item-list">
            <div
              v-for="(row, idx) in items"
              :key="row.cartItemId"
              class="cart-item"
              :style="{ '--delay': idx * 0.06 + 's' }"
            >
              <!-- Product thumb + name -->
              <div class="cart-item__product">
                <div class="cart-item__img-wrap">
                  <img
                    v-if="row.imageUrl"
                    :src="row.imageUrl"
                    :alt="row.productName"
                    class="cart-item__img"
                  />
                  <div v-else class="cart-item__img-placeholder">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                      <rect
                        x="3"
                        y="3"
                        width="18"
                        height="18"
                        rx="3"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                      <circle
                        cx="8.5"
                        cy="8.5"
                        r="1.5"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                      <path
                        d="M21 15l-5-5L5 21"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                  </div>
                </div>
                <div class="cart-item__info">
                  <div class="cart-item__name">{{ row.productName }}</div>
                  <div v-if="row.variantName" class="cart-item__variant">
                    {{ row.variantName }}
                  </div>
                </div>
              </div>

              <!-- Unit price -->
              <div class="cart-item__price">{{ formatMoney(row.price) }}</div>

              <!-- Quantity stepper -->
              <div class="cart-item__qty">
                <div class="qty-stepper">
                  <button
                    class="qty-stepper__btn"
                    :disabled="
                      row.quantity <= 1 || updatingId === row.cartItemId
                    "
                    @click="changeQty(row, -1)"
                  >
                    −
                  </button>
                  <span class="qty-stepper__val">{{ row.quantity }}</span>
                  <button
                    class="qty-stepper__btn"
                    :disabled="updatingId === row.cartItemId"
                    @click="changeQty(row, 1)"
                  >
                    +
                  </button>
                </div>
              </div>

              <!-- Subtotal -->
              <div class="cart-item__subtotal">
                {{ formatMoney((row.price || 0) * row.quantity) }}
              </div>

              <!-- Remove -->
              <div class="cart-item__remove">
                <button
                  class="remove-btn"
                  :class="{
                    'remove-btn--loading': removingId === row.cartItemId,
                  }"
                  :disabled="removingId === row.cartItemId"
                  @click="removeItem(row)"
                  title="Xóa"
                >
                  <span
                    v-if="removingId === row.cartItemId"
                    class="mini-spinner"
                  ></span>
                  <svg
                    v-else
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                  >
                    <polyline
                      points="3 6 5 6 21 6"
                      stroke="currentColor"
                      stroke-width="1.6"
                      stroke-linecap="round"
                    />
                    <path
                      d="M19 6l-1 14H6L5 6"
                      stroke="currentColor"
                      stroke-width="1.6"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M10 11v6M14 11v6"
                      stroke="currentColor"
                      stroke-width="1.6"
                      stroke-linecap="round"
                    />
                    <path
                      d="M9 6V4h6v2"
                      stroke="currentColor"
                      stroke-width="1.6"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </button>
              </div>
            </div>
          </transition-group>
        </div>

        <!-- Right: Summary -->
        <div class="cart-summary">
          <div class="summary-card">
            <div class="summary-card__title">Tổng đơn hàng</div>

            <div class="summary-lines">
              <div class="summary-line">
                <span>Tạm tính ({{ items.length }} sản phẩm)</span>
                <span>{{ formatMoney(subtotal) }}</span>
              </div>
              <div class="summary-line summary-line--muted">
                <span>Phí vận chuyển</span>
                <span>Tính khi đặt hàng</span>
              </div>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-total">
              <span>Tổng cộng</span>
              <span class="summary-total__amount">{{
                formatMoney(subtotal)
              }}</span>
            </div>

            <button
              class="checkout-btn"
              :disabled="!items.length"
              @click="checkout"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path
                  d="M5 12h14M12 5l7 7-7 7"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              Thanh toán ngay
              <div class="checkout-btn__shimmer"></div>
            </button>

            <button class="continue-btn" @click="$router.push('/')">
              Tiếp tục mua sắm
            </button>
          </div>

          <!-- Trust badges -->
          <div class="trust-badges">
            <div class="trust-badge">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path
                  d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linejoin="round"
                />
              </svg>
              Thanh toán bảo mật
            </div>
            <div class="trust-badge">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path
                  d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"
                  stroke="currentColor"
                  stroke-width="1.5"
                />
                <circle
                  cx="12"
                  cy="10"
                  r="3"
                  stroke="currentColor"
                  stroke-width="1.5"
                />
              </svg>
              Giao hàng toàn quốc
            </div>
            <div class="trust-badge">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <polyline
                  points="23 4 23 10 17 10"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                />
                <path
                  d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                />
              </svg>
              Đổi trả dễ dàng
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { cartApi } from "../../api/cart.api";
import { useRouter } from "vue-router";

const router = useRouter();
const items = ref([]);
const loading = ref(false);
const updatingId = ref(null);
const removingId = ref(null);

const subtotal = computed(() =>
  items.value.reduce((sum, i) => sum + (i.price || 0) * (i.quantity || 1), 0),
);

async function loadCart() {
  loading.value = true;
  try {
    const res = await cartApi.getItems();
    items.value = res.data || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function changeQty(item, delta) {
  const newQty = Math.max(1, item.quantity + delta);
  if (newQty === item.quantity) return;
  updatingId.value = item.cartItemId;
  try {
    item.quantity = newQty;
    await cartApi.updateQuantity(item.cartItemId, newQty);
  } catch (e) {
    item.quantity -= delta; // revert
  } finally {
    updatingId.value = null;
  }
}

async function updateQty(item) {
  updatingId.value = item.cartItemId;
  try {
    await cartApi.updateQuantity(item.cartItemId, item.quantity);
  } finally {
    updatingId.value = null;
  }
}

async function removeItem(item) {
  removingId.value = item.cartItemId;
  try {
    await cartApi.remove(item.cartItemId);
    await loadCart();
  } finally {
    removingId.value = null;
  }
}

function checkout() {
  router.push("/orders/new");
}

function formatMoney(value) {
  if (value == null) return "—";
  return Number(value).toLocaleString("vi-VN") + " ₫";
}

onMounted(loadCart);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

/* ── Root ── */
.cart-root {
  min-height: 100vh;
  font-family: "Inter", sans-serif;
  color: #0F172A;
  position: relative;
}

/* ── Background ── */
.cart-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.cart-bg__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.09;
}

.cart-bg__orb--1 {
  width: 700px;
  height: 500px;
  background: radial-gradient(ellipse, #f59e0b, transparent 65%);
  top: -200px;
  right: -100px;
  animation: orb-drift 20s ease-in-out infinite alternate;
}

.cart-bg__orb--2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #ec4899, transparent 65%);
  bottom: -150px;
  left: -80px;
  animation: orb-drift 26s ease-in-out infinite alternate-reverse;
}

.cart-bg__noise {
  position: absolute;
  inset: 0;
  opacity: 0.02;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-size: 200px;
}

@keyframes orb-drift {
  from {
    transform: translate(0, 0);
  }
  to {
    transform: translate(30px, 40px);
  }
}

/* ── Wrap ── */
.cart-wrap {
  position: relative;
  z-index: 1;
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px 80px;
}

/* ── Header ── */
.cart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
  animation: fade-up 0.5s ease both;
}

.cart-header__eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #3B82F6;
  margin-bottom: 5px;
}

.cart-header__title {
  font-family: "Inter", sans-serif;
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
  letter-spacing: -0.02em;
  display: flex;
  align-items: center;
  gap: 12px;
}

.cart-header__count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #3B82F6;
  color: white;
  font-family: "Inter", sans-serif;
  font-size: 13px;
  font-weight: 700;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  vertical-align: middle;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 16px;
  border-radius: 10px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  font-family: "Inter", sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: #64748B;
  cursor: pointer;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06), 0 1px 3px rgba(0,0,0,0.05);
  transition: all 0.15s;
  white-space: nowrap;
}

.back-btn:hover {
  color: #0F172A;
  border-color: rgba(15, 23, 42, 0.15);
  transform: translateX(-2px);
}

/* ── Skeleton ── */
.cart-skeleton {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.cart-skeleton__row {
  display: grid;
  grid-template-columns: 2fr 1fr 1.2fr 1fr 1.2fr auto;
  gap: 16px;
  align-items: center;
  padding: 18px 22px;
  background: #FFFFFF;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  animation: fade-up 0.4s ease both;
  animation-delay: var(--d, 0s);
}

.sk {
  border-radius: 8px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 300%;
  animation: shimmer 1.6s infinite;
}

.sk--img {
  width: 56px;
  height: 56px;
  border-radius: 10px;
}
.sk-lines {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.sk--long {
  height: 13px;
  width: 85%;
}
.sk--short {
  height: 10px;
  width: 55%;
}
.sk--price {
  height: 13px;
  width: 70%;
}
.sk--qty {
  height: 34px;
  width: 100px;
  border-radius: 10px;
}
.sk--sub {
  height: 13px;
  width: 80%;
}
.sk--btn {
  height: 32px;
  width: 36px;
  border-radius: 8px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* ── Empty ── */
.cart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 24px;
  animation: fade-up 0.5s ease both;
}

.cart-empty__bag {
  color: rgba(15, 23, 42, 0.15);
  margin-bottom: 20px;
}

.cart-empty__title {
  font-family: "Inter", sans-serif;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}

.cart-empty__sub {
  font-size: 14px;
  color: #64748B;
  margin-bottom: 28px;
}

.btn-shop-now {
  padding: 12px 28px;
  border-radius: 100px;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  color: white;
  border: none;
  font-family: "Inter", sans-serif;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
  transition:
    transform 0.15s,
    box-shadow 0.15s;
}

.btn-shop-now:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.35);
}

/* ── Cart Layout ── */
.cart-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 860px) {
  .cart-content {
    grid-template-columns: 1fr;
  }
}

/* ── Items ── */
.cart-items {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06), 0 1px 3px rgba(0,0,0,0.05);
  animation: fade-up 0.5s ease both;
}

.cart-table-head {
  display: grid;
  grid-template-columns: 2.5fr 1fr 1.3fr 1fr 40px;
  gap: 12px;
  align-items: center;
  padding: 13px 22px;
  background: linear-gradient(to bottom, #F8FAFC, #F1F5F9);
  border-bottom: 1px solid #E5E7EB;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #64748B;
}

.cart-item-list {
  display: flex;
  flex-direction: column;
}

.cart-item {
  display: grid;
  grid-template-columns: 2.5fr 1fr 1.3fr 1fr 40px;
  gap: 12px;
  align-items: center;
  padding: 16px 22px;
  border-bottom: 1px solid #E5E7EB;
  transition: background 0.15s;
  animation: fade-in-row 0.4s ease both;
  animation-delay: var(--delay, 0s);
}

.cart-item:last-child {
  border-bottom: none;
}
.cart-item:hover {
  background: #F8FAFC;
}

@keyframes fade-in-row {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Item list transitions */
.item-list-enter-active {
  transition: all 0.35s cubic-bezier(0.34, 1.2, 0.64, 1);
}
.item-list-leave-active {
  transition: all 0.25s ease;
}
.item-list-enter-from {
  opacity: 0;
  transform: translateX(-12px);
}
.item-list-leave-to {
  opacity: 0;
  transform: translateX(12px);
  height: 0;
  padding: 0;
}

/* Product cell */
.cart-item__product {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.cart-item__img-wrap {
  width: 58px;
  height: 58px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #F1F5F9;
  border: 1px solid #E5E7EB;
}

.cart-item__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cart-item__img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(15, 23, 42, 0.2);
}

.cart-item__info {
  min-width: 0;
}

.cart-item__name {
  font-size: 13.5px;
  font-weight: 600;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cart-item__variant {
  font-size: 11.5px;
  color: #64748B;
  margin-top: 3px;
}

/* Price */
.cart-item__price {
  font-size: 13px;
  color: #64748B;
  font-variant-numeric: tabular-nums;
}

/* Qty stepper */
.qty-stepper {
  display: inline-flex;
  align-items: center;
  background: #F1F5F9;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  overflow: hidden;
}

.qty-stepper__btn {
  width: 30px;
  height: 34px;
  border: none;
  background: none;
  font-size: 16px;
  color: #0F172A;
  cursor: pointer;
  transition: background 0.12s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qty-stepper__btn:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}
.qty-stepper__btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.qty-stepper__val {
  min-width: 32px;
  text-align: center;
  font-size: 13.5px;
  font-weight: 600;
  color: #0F172A;
  padding: 0 4px;
}

/* Subtotal */
.cart-item__subtotal {
  font-size: 14px;
  font-weight: 700;
  color: #3B82F6;
  font-variant-numeric: tabular-nums;
  text-align: right;
}

/* Remove */
.remove-btn {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #fff5f5;
  border: 1px solid rgba(239, 68, 68, 0.15);
  color: #dc2626;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.remove-btn:hover:not(:disabled) {
  background: #fee2e2;
  border-color: rgba(239, 68, 68, 0.3);
  transform: scale(1.08);
}

.remove-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.remove-btn--loading {
  opacity: 0.6;
}

.mini-spinner {
  width: 12px;
  height: 12px;
  border: 1.5px solid rgba(220, 38, 38, 0.3);
  border-top-color: #dc2626;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ── Summary ── */
.cart-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 24px;
  animation: fade-up 0.55s ease both 0.1s;
}

.summary-card {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 20px;
  padding: 26px 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06), 0 1px 3px rgba(0,0,0,0.05);
}

.summary-card__title {
  font-family: "Inter", sans-serif;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.summary-lines {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  font-size: 13.5px;
  color: #0F172A;
}

.summary-line--muted span {
  color: #64748B;
  font-size: 13px;
}

.summary-divider {
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    #E5E7EB 20%,
    #E5E7EB 80%,
    transparent
  );
  margin: 6px 0 16px;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 22px;
  font-weight: 600;
  font-size: 14px;
}

.summary-total__amount {
  font-family: "Inter", sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: #3B82F6;
  font-variant-numeric: tabular-nums;
}

.checkout-btn {
  position: relative;
  overflow: hidden;
  width: 100%;
  padding: 14px;
  border-radius: 14px;
  border: none;
  background: linear-gradient(135deg, #1E293B, #0F172A);
  color: white;
  font-family: "Inter", sans-serif;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.2);
  transition:
    transform 0.15s,
    box-shadow 0.15s;
  margin-bottom: 10px;
  letter-spacing: 0.01em;
}

.checkout-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 32px rgba(15, 23, 42, 0.25);
}

.checkout-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.checkout-btn__shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    105deg,
    transparent 35%,
    rgba(255, 255, 255, 0.1) 50%,
    transparent 65%
  );
  transform: translateX(-100%);
  transition: transform 0.65s;
}

.checkout-btn:hover .checkout-btn__shimmer {
  transform: translateX(100%);
}

.continue-btn {
  width: 100%;
  padding: 11px;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  background: transparent;
  color: #64748B;
  font-family: "Inter", sans-serif;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.continue-btn:hover {
  background: #F1F5F9;
  color: #0F172A;
  border-color: rgba(15, 23, 42, 0.12);
}

/* Trust badges */
.trust-badges {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 16px;
  padding: 16px 18px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06), 0 1px 3px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  gap: 11px;
}

.trust-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12.5px;
  color: #64748B;
}

.trust-badge svg {
  color: #3B82F6;
  flex-shrink: 0;
}

/* ── Animations ── */
@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ── Responsive ── */
@media (max-width: 640px) {
  .cart-table-head {
    display: none;
  }
  .cart-item {
    grid-template-columns: 1fr auto;
    grid-template-rows: auto auto;
    gap: 10px;
    padding: 14px 16px;
  }
  .cart-item__product {
    grid-column: 1 / -1;
  }
  .cart-item__price {
    display: none;
  }
  .cart-item__subtotal {
    font-size: 13px;
  }
}
</style>
