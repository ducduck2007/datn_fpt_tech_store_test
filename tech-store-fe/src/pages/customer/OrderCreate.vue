<!-- \tech-store-fe\src\pages\customer\OrderCreate.vue -->
<template>
  <div class="checkout-root">
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
                <div class="cart-item-qty"><span class="qty-x">×</span><span class="qty-num">{{ item.quantity }}</span></div>
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
              <div class="delivery-opt-radio"><div class="radio-dot" v-if="deliveryMethod === 'STORE'"></div></div>
              <div class="delivery-opt-icon">🏬</div>
              <div class="delivery-opt-body">
                <div class="delivery-opt-title">Nhận tại cửa hàng</div>
                <div class="delivery-opt-desc">Đến nhận trực tiếp tại quầy — không mất phí</div>
              </div>
              <div class="delivery-opt-price free-label">Miễn phí</div>
            </label>
            <label class="delivery-opt" :class="{ 'delivery-opt--active': deliveryMethod === 'HOME' }">
              <input type="radio" value="HOME" v-model="deliveryMethod" class="sr-only"/>
              <div class="delivery-opt-radio"><div class="radio-dot" v-if="deliveryMethod === 'HOME'"></div></div>
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
                <polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
            </div>
            <h2 class="co-card-title">Ghi chú đơn hàng</h2>
            <span class="co-optional">Tùy chọn</span>
          </div>
          <textarea v-model="form.notes" class="note-textarea" rows="3" placeholder="Ví dụ: Gọi trước khi giao, để hàng ở bảo vệ..."/>
        </div>
      </div>

      <!-- ══ RIGHT COLUMN ══════════════════════════════════════════ -->
      <div class="checkout-right">

        <!-- PAYMENT METHOD -->
        <div class="co-card">
          <div class="co-card-head">
            <div class="co-card-head-icon">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
              </svg>
            </div>
            <h2 class="co-card-title">Phương thức thanh toán</h2>
          </div>
          <div class="payment-methods">
            <label v-for="opt in paymentOptions" :key="opt.value" class="pm-opt" :class="{ 'pm-opt--active': form.paymentMethod === opt.value }">
              <input type="radio" :value="opt.value" v-model="form.paymentMethod" class="sr-only"/>
              <div class="pm-opt-radio"><div class="radio-dot" v-if="form.paymentMethod === opt.value"></div></div>
              <span class="pm-opt-icon">{{ opt.icon }}</span>
              <span class="pm-opt-label">{{ opt.label }}</span>
              <span v-if="form.paymentMethod === opt.value" class="pm-check">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
              </span>
            </label>
          </div>
          <transition name="slide-fade">
            <div v-if="form.paymentMethod === 'TRANSFER'" class="payment-notice">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              Mã QR thanh toán sẽ hiển thị sau khi đặt hàng
            </div>
          </transition>
          <transition name="slide-fade">
            <div v-if="form.paymentMethod === 'CARD'" class="payment-notice">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              Thông tin thẻ sẽ được nhập ở bước tiếp theo
            </div>
          </transition>
        </div>

        <!-- ══ VOUCHER PICKER ═════════════════════════════════════ -->
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

          <!-- Selected voucher -->
          <div v-if="selectedVoucher" class="voucher-applied">
            <div class="voucher-applied-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <div class="voucher-applied-body">
              <div class="voucher-applied-row1">
                <span class="voucher-applied-code">{{ selectedVoucher.code }}</span>
                <span class="voucher-applied-save">−{{ formatMoney(promoResult?.discountAmount) }}</span>
              </div>
              <div class="voucher-applied-name">{{ selectedVoucher.name }}</div>
            </div>
            <button class="voucher-applied-change" @click="openVoucherModal">Đổi mã</button>
          </div>

          <!-- No voucher -->
          <button v-else class="voucher-trigger" @click="openVoucherModal">
            <span class="voucher-trigger-ticket">🎟️</span>
            <span class="voucher-trigger-body">
              <span class="voucher-trigger-title">Chọn mã giảm giá</span>
              <span class="voucher-trigger-sub" v-if="availableVouchers.length > 0">{{ availableVouchers.length }} mã có thể sử dụng</span>
              <span class="voucher-trigger-sub" v-else>Xem các mã khuyến mãi có sẵn</span>
            </span>
            <svg class="voucher-trigger-arrow" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
          </button>

          <transition name="msg-fade">
            <div v-if="promoError" class="promo-msg promo-msg--err">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/></svg>
              {{ promoError }}
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
              <div v-if="selectedVoucher && promoResult" class="summary-row summary-row--discount">
                <span>
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="vertical-align:-1px">
                    <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                  </svg>
                  Mã <code class="promo-code-inline">{{ selectedVoucher.code }}</code>
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
            <div v-if="selectedVoucher && promoResult" class="savings-banner">
              🎉 Bạn tiết kiệm được <strong>{{ formatMoney(promoResult.discountAmount) }}</strong> cho đơn hàng này!
            </div>
          </transition>
        </div>

        <!-- ERROR -->
        <transition name="msg-fade">
          <div v-if="alert" class="alert-error">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {{ alert }}
          </div>
        </transition>

        <!-- SUBMIT -->
        <button class="submit-btn" :class="{ 'submit-btn--loading': loading, 'submit-btn--disabled': form.items.length === 0 }" :disabled="loading || form.items.length === 0" @click="submit">
          <template v-if="!loading">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            Đặt hàng ngay · {{ formatMoney(totalAfterDiscount) }}
          </template>
          <template v-else>
            <svg class="spin-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 12a9 9 0 11-6.219-8.56"/></svg>
            Đang xử lý đơn hàng...
          </template>
        </button>
      </div>
    </div>

    <!-- ══ VOUCHER MODAL ═════════════════════════════════════════ -->
    <teleport to="body">
      <transition name="modal-fade">
        <div v-if="voucherModalOpen" class="vm-overlay" @click.self="voucherModalOpen = false">
          <div class="vm-dialog">
            <!-- Header -->
            <div class="vm-header">
              <div class="vm-header-left">
                <span class="vm-header-icon">🎟️</span>
                <div>
                  <div class="vm-header-title">Chọn Voucher</div>
                  <div class="vm-header-sub">{{ availableVouchers.length }} mã khả dụng</div>
                </div>
              </div>
              <button class="vm-close" @click="voucherModalOpen = false">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>

            <!-- Loading -->
            <div v-if="voucherLoading" class="vm-loading">
              <svg class="spin-icon" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--em)" stroke-width="2.5"><path d="M21 12a9 9 0 11-6.219-8.56"/></svg>
              <span>Đang tải mã khuyến mãi...</span>
            </div>

            <!-- Empty -->
            <div v-else-if="availableVouchers.length === 0" class="vm-empty">
              <div class="vm-empty-art">
                <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="var(--ink4)" stroke-width="1.2">
                  <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                  <line x1="7" y1="7" x2="7.01" y2="7"/>
                  <line x1="4" y1="20" x2="20" y2="4" stroke-dasharray="3,3" opacity="0.4"/>
                </svg>
              </div>
              <div class="vm-empty-title">Chưa có mã nào</div>
              <div class="vm-empty-desc">Hiện không có mã khuyến mãi phù hợp cho bạn</div>
            </div>

            <!-- Voucher list -->
            <div v-else class="vm-body">
              <div
                v-for="v in availableVouchers"
                :key="v.id"
                class="vc"
                :class="{
                  'vc--selected': tempSelectedId === v.id,
                  'vc--disabled': !isVoucherApplicable(v)
                }"
                @click="isVoucherApplicable(v) && selectVoucher(v.id)"
              >
                <!-- Ticket left -->
                <div class="vc-left" :class="vcColorClass(v)">
                  <div class="vc-left-val" v-if="!v.isCombo">
                    <span class="vc-left-num">{{ v.discountType === 'PERCENT' ? v.discountValue : formatShort(v.discountValue) }}</span>
                    <span class="vc-left-unit">{{ v.discountType === 'PERCENT' ? '%' : '₫' }}</span>
                  </div>
                  <div class="vc-left-val" v-else>
                    <span class="vc-left-num" style="font-size:14px">{{ v.buyQty }}+{{ v.getQty }}</span>
                    <span class="vc-left-unit">COMBO</span>
                  </div>
                  <!-- Scalloped edge -->
                  <div class="vc-scallop">
                    <div class="vc-scallop-dot"></div>
                    <div class="vc-scallop-dot"></div>
                    <div class="vc-scallop-dot"></div>
                    <div class="vc-scallop-dot"></div>
                    <div class="vc-scallop-dot"></div>
                  </div>
                </div>

                <!-- Ticket right -->
                <div class="vc-right">
                  <div class="vc-right-top">
                    <div class="vc-code">{{ v.code }}</div>
                    <!-- Selected indicator -->
                    <div class="vc-check" :class="{ 'vc-check--on': tempSelectedId === v.id }">
                      <svg v-if="tempSelectedId === v.id" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                    </div>
                  </div>
                  <div class="vc-name">{{ v.name }}</div>
                  <div class="vc-meta">
                    <span v-if="Number(v.minOrderAmount) > 0">Đơn từ {{ formatMoney(v.minOrderAmount) }}</span>
                    <span v-else>Không giới hạn đơn</span>
                    <span class="vc-meta-dot">·</span>
                    <span>HSD: {{ formatDate(v.endDate) }}</span>
                  </div>
                  <div v-if="!isVoucherApplicable(v)" class="vc-warn">
                    Cần thêm {{ formatMoney(Number(v.minOrderAmount) - subtotal) }} nữa
                  </div>
                </div>
              </div>
            </div>

            <!-- Footer -->
            <div class="vm-footer">
              <button class="vm-btn-clear" @click="clearAndClose" :class="{ 'vm-btn-clear--has': tempSelectedId || selectedVoucher }">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                Không dùng mã
              </button>
              <button
                class="vm-btn-apply"
                :class="{ 'vm-btn-apply--active': tempSelectedId }"
                :disabled="voucherLoading || !tempSelectedId"
                @click="applySelectedVoucher"
              >
                <template v-if="!voucherLoading">
                  Áp dụng
                  <span v-if="tempSelectedId && tempPreview" class="vm-btn-preview">− {{ formatMoney(tempPreview) }}</span>
                </template>
                <svg v-else class="spin-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5"><path d="M21 12a9 9 0 11-6.219-8.56"/></svg>
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { customersApi } from "../../api/customers.api";
import { cartApi } from "../../api/cart.api";
import { promotionsApi } from "../../api/promotions.api";
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

const voucherModalOpen = ref(false);
const voucherLoading = ref(false);
const availableVouchers = ref([]);
const selectedVoucher = ref(null);
const tempSelectedId = ref(null);
const promoResult = ref(null);
const promoError = ref("");

const paymentOptions = [
  { value: "CASH", label: "Tiền mặt", icon: "💵" },
  { value: "TRANSFER", label: "Chuyển khoản", icon: "🏦" },
  { value: "CARD", label: "Thẻ tín dụng", icon: "💳" },
];

const subtotal = computed(() =>
  form.items.reduce((sum, i) => {
    const price = i.price ?? i.unitPrice ?? 0;
    return sum + Number(price) * (i.quantity ?? 1);
  }, 0)
);

const promoDiscount = computed(() =>
  selectedVoucher.value && promoResult.value ? Number(promoResult.value.discountAmount ?? 0) : 0
);

const shippingFee = computed(() => (deliveryMethod.value === "HOME" ? 70000 : 0));

const totalAfterDiscount = computed(() =>
  Math.max(0, subtotal.value - promoDiscount.value + shippingFee.value)
);

// Preview discount amount when hovering/selecting in modal
const tempPreview = computed(() => {
  if (!tempSelectedId.value) return null;
  const v = availableVouchers.value.find((x) => x.id === tempSelectedId.value);
  if (!v) return null;
  if (v.isCombo) return null;
  if (v.discountType === "PERCENT") {
    return Math.min(subtotal.value, Math.round((subtotal.value * Number(v.discountValue)) / 100));
  }
  return Math.min(subtotal.value, Number(v.discountValue));
});

function isVoucherApplicable(v) {
  const min = Number(v.minOrderAmount ?? 0);
  return !(min > 0 && subtotal.value < min);
}

function vcColorClass(v) {
  if (v.isCombo) return "vc-left--combo";
  if (v.discountType === "PERCENT") return "vc-left--percent";
  return "vc-left--amount";
}

function selectVoucher(id) {
  tempSelectedId.value = tempSelectedId.value === id ? null : id;
}

async function loadAvailableVouchers() {
  voucherLoading.value = true;
  try {
    const res = await promotionsApi.getAvailable();
    availableVouchers.value = res?.data?.data ?? res?.data ?? [];
  } catch {
    availableVouchers.value = [];
  } finally {
    voucherLoading.value = false;
  }
}

function openVoucherModal() {
  tempSelectedId.value = selectedVoucher.value?.id ?? null;
  voucherModalOpen.value = true;
  loadAvailableVouchers();
}

function clearAndClose() {
  selectedVoucher.value = null;
  promoResult.value = null;
  promoError.value = "";
  tempSelectedId.value = null;
  voucherModalOpen.value = false;
}

async function applySelectedVoucher() {
  if (!tempSelectedId.value) return;
  const voucher = availableVouchers.value.find((v) => v.id === tempSelectedId.value);
  if (!voucher) return;

  promoError.value = "";
  voucherLoading.value = true;
  try {
    const res = await ordersApi.validatePromoCode(voucher.code, subtotal.value);
    const data = res?.data?.data ?? res?.data ?? res;
    if (data?.valid) {
      selectedVoucher.value = voucher;
      promoResult.value = data;
      promoError.value = "";
      voucherModalOpen.value = false;
      toast(`✅ Áp dụng mã "${voucher.code}" — giảm ${formatMoney(data.discountAmount)}`, "success");
    } else {
      promoError.value = data?.message || "Mã không hợp lệ";
      selectedVoucher.value = null;
      promoResult.value = null;
      voucherModalOpen.value = false;
    }
  } catch (e) {
    promoError.value = e?.response?.data?.message || "Không thể kiểm tra mã";
    selectedVoucher.value = null;
    promoResult.value = null;
    voucherModalOpen.value = false;
  } finally {
    voucherLoading.value = false;
  }
}

function buildNotes() {
  const parts = [];
  if (form.notes?.trim()) parts.push(form.notes.trim());
  if (form.paymentMethod === "TRANSFER") parts.push("Thanh toán chuyển khoản");
  else if (form.paymentMethod === "CARD") parts.push("Thanh toán thẻ tín dụng");
  parts.push(deliveryMethod.value === "HOME" ? "Giao tại nhà" : "Nhận tại cửa hàng");
  return parts.join(" | ");
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
      customerId: Number(form.customerId),
      paymentMethod: form.paymentMethod,
      channel: form.channel,
      notes: buildNotes(),
      items: form.items,
      shippingFee: shippingFee.value,
    };
    if (selectedVoucher.value) payload.promotionCode = selectedVoucher.value.code;
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

function formatDiscount(v) {
  if (v.isCombo) return `Mua ${v.buyQty} tặng ${v.getQty}`;
  if (v.discountType === "PERCENT") return `${v.discountValue}%`;
  return formatMoney(v.discountValue);
}

function formatShort(val) {
  const n = Number(val);
  if (n >= 1000000) return Math.round(n / 1000000) + "M";
  if (n >= 1000) return Math.round(n / 1000) + "K";
  return n;
}

function formatDate(iso) {
  if (!iso) return "—";
  try { return new Date(iso).toLocaleDateString("vi-VN"); } catch { return iso; }
}

function formatMoney(value) {
  if (value == null) return "—";
  return Number(value).toLocaleString("vi-VN") + " ₫";
}

onMounted(async () => {
  try {
    const profile = await customersApi.getProfile();
    form.customerId = profile?.data?.id;
    const cart = await cartApi.getItems();
    form.items = cart?.data ?? [];
  } catch {
    alert.value = "Không thể tải dữ liệu đơn hàng";
  }
  loadAvailableVouchers();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap");

.checkout-root {
  --em: #1d6fd8; --em-dk: #1558b0; --em-lt: #eff4fe; --em-md: #a8c8f8;
  --ink: #111827; --ink2: #374151; --ink3: #6b7280; --ink4: #9ca3af; --ink5: #d1d5db;
  --bg: #f1f3f6; --card: #ffffff; --border: #e2e6ea;
  --red: #dc2626; --amber: #d97706; --green: #16a34a;
  --rad: 14px; --rad-sm: 10px;
  --sha: 0 1px 2px rgba(0,0,0,.05), 0 3px 12px rgba(0,0,0,.04);
  min-height: 100vh; background: var(--bg);
  font-family: "Plus Jakarta Sans", system-ui, sans-serif; color: var(--ink);
}
*,*::before,*::after{box-sizing:border-box;margin:0;padding:0}

.checkout-body{max-width:1140px;margin:0 auto;padding:28px 24px 64px;display:grid;grid-template-columns:1fr 396px;gap:20px;align-items:start}

/* ── Card ──────── */
.co-card{background:var(--card);border:1px solid var(--border);border-radius:var(--rad);padding:22px 24px;margin-bottom:14px;box-shadow:var(--sha)}
.co-card:last-of-type{margin-bottom:0}
.co-card-head{display:flex;align-items:center;gap:10px;margin-bottom:18px}
.co-card-head-icon{width:34px;height:34px;background:var(--em-lt);border-radius:9px;display:flex;align-items:center;justify-content:center;color:var(--em);flex-shrink:0}
.co-card-title{font-size:14.5px;font-weight:700;color:var(--ink);flex:1}
.co-badge{padding:2px 9px;border-radius:20px;background:var(--em);color:#fff;font-size:11.5px;font-weight:700}
.co-optional{font-size:11.5px;color:var(--ink4);font-weight:500}

/* ── Cart ──────── */
.cart-empty{text-align:center;padding:28px 0}
.cart-empty-icon{font-size:44px;margin-bottom:12px}
.cart-empty-text{font-size:14px;color:var(--ink3);margin-bottom:16px}
.btn-go-shop{padding:10px 26px;background:var(--em);color:#fff;border:none;border-radius:8px;font-size:13.5px;font-family:inherit;font-weight:600;cursor:pointer;transition:background .15s}
.btn-go-shop:hover{background:var(--em-dk)}
.cart-items{display:flex;flex-direction:column;gap:8px}
.cart-item{display:flex;align-items:center;gap:14px;padding:13px 14px;border-radius:var(--rad-sm);border:1px solid var(--border);background:#f8f9fb;animation:item-in .28s ease both;animation-delay:var(--delay,0ms)}
@keyframes item-in{from{opacity:0;transform:translateY(5px)}to{opacity:1;transform:none}}
.cart-item:hover{border-color:var(--em-md)}
.cart-item-thumb{width:50px;height:50px;flex-shrink:0;background:var(--card);border:1px solid var(--border);border-radius:8px;display:flex;align-items:center;justify-content:center}
.cart-item-info{flex:1;min-width:0}
.cart-item-name{font-size:13.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.cart-item-sub{font-size:11.5px;color:var(--ink4);margin-top:2px}
.cart-item-right{display:flex;flex-direction:column;align-items:flex-end;gap:4px;flex-shrink:0}
.cart-item-qty{display:flex;align-items:center;gap:3px}
.qty-x{font-size:11px;color:var(--ink4)}.qty-num{font-size:13px;font-weight:600;color:var(--ink2)}
.cart-item-price{font-size:14px;font-weight:700;color:var(--em)}

/* ── Delivery ──── */
.delivery-options{display:flex;flex-direction:column;gap:10px}
.delivery-opt{display:flex;align-items:center;gap:14px;padding:15px 16px;border-radius:var(--rad-sm);border:2px solid var(--border);cursor:pointer;background:var(--card);transition:all .15s}
.delivery-opt:hover{border-color:var(--em-md);background:var(--em-lt)}
.delivery-opt--active{border-color:var(--em);background:var(--em-lt)}
.delivery-opt-radio{width:18px;height:18px;border-radius:50%;border:2px solid var(--ink5);flex-shrink:0;display:flex;align-items:center;justify-content:center;transition:border-color .15s}
.delivery-opt--active .delivery-opt-radio{border-color:var(--em)}
.radio-dot{width:8px;height:8px;border-radius:50%;background:var(--em)}
.delivery-opt-icon{font-size:22px}
.delivery-opt-body{flex:1}
.delivery-opt-title{font-size:13.5px;font-weight:600}
.delivery-opt-desc{font-size:12px;color:var(--ink3);margin-top:2px}
.delivery-opt-price{font-size:13.5px;font-weight:700;color:var(--ink2);flex-shrink:0}
.free-label{color:var(--em)}

/* ── Note ──────── */
.note-textarea{width:100%;min-height:82px;padding:12px 14px;resize:vertical;border:1.5px solid var(--border);border-radius:var(--rad-sm);font-family:inherit;font-size:13.5px;color:var(--ink);outline:none;background:#f8f9fb;transition:all .15s;line-height:1.6}
.note-textarea::placeholder{color:var(--ink4)}
.note-textarea:focus{border-color:var(--em);background:var(--card);box-shadow:0 0 0 3px rgba(29,111,216,.1)}

/* ── Payment ───── */
.payment-methods{display:flex;flex-direction:column;gap:8px}
.pm-opt{display:flex;align-items:center;gap:12px;padding:13px 14px;border-radius:var(--rad-sm);border:2px solid var(--border);cursor:pointer;background:var(--card);transition:all .15s}
.pm-opt:hover{border-color:var(--em-md);background:var(--em-lt)}
.pm-opt--active{border-color:var(--em);background:var(--em-lt)}
.pm-opt-radio{width:18px;height:18px;border-radius:50%;border:2px solid var(--ink5);flex-shrink:0;display:flex;align-items:center;justify-content:center;transition:border-color .15s}
.pm-opt--active .pm-opt-radio{border-color:var(--em)}
.pm-opt-icon{font-size:18px;flex-shrink:0}
.pm-opt-label{font-size:13.5px;font-weight:500;flex:1}
.pm-check{width:20px;height:20px;border-radius:50%;background:var(--em);color:#fff;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.payment-notice{display:flex;align-items:center;gap:7px;margin-top:12px;padding:10px 13px;border-radius:8px;font-size:12.5px;font-weight:500;background:#eff6ff;border:1px solid #bfdbfe;color:#1d4ed8}

/* ══ VOUCHER INLINE ═════════════════════════════════════════════ */
.voucher-applied{display:flex;align-items:center;gap:12px;padding:14px 16px;border-radius:var(--rad-sm);background:linear-gradient(135deg,#ecfdf5,#d1fae5);border:2px solid #6ee7b7}
.voucher-applied-icon{width:32px;height:32px;border-radius:50%;background:#10b981;flex-shrink:0;display:flex;align-items:center;justify-content:center}
.voucher-applied-body{flex:1;min-width:0}
.voucher-applied-row1{display:flex;align-items:center;justify-content:space-between;gap:8px}
.voucher-applied-code{font-size:13.5px;font-weight:700;color:#065f46;letter-spacing:.04em}
.voucher-applied-save{font-size:14px;font-weight:800;color:#059669}
.voucher-applied-name{font-size:12px;color:#047857;margin-top:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.voucher-applied-change{padding:6px 14px;border-radius:6px;background:#fff;border:1.5px solid #6ee7b7;color:#059669;font-family:inherit;font-size:12px;font-weight:600;cursor:pointer;flex-shrink:0;transition:all .15s}
.voucher-applied-change:hover{background:#d1fae5;border-color:#34d399}

.voucher-trigger{width:100%;display:flex;align-items:center;gap:14px;padding:16px;border-radius:var(--rad-sm);border:2px dashed var(--border);background:transparent;cursor:pointer;transition:all .15s;font-family:inherit;text-align:left}
.voucher-trigger:hover{border-color:var(--em);background:var(--em-lt)}
.voucher-trigger-ticket{font-size:28px;flex-shrink:0}
.voucher-trigger-body{flex:1;display:flex;flex-direction:column;gap:2px}
.voucher-trigger-title{font-size:13.5px;font-weight:600;color:var(--ink)}
.voucher-trigger-sub{font-size:12px;color:var(--ink3)}
.voucher-trigger-arrow{color:var(--ink4);flex-shrink:0;transition:transform .15s}
.voucher-trigger:hover .voucher-trigger-arrow{transform:translateX(2px);color:var(--em)}

.promo-msg{display:flex;align-items:center;gap:6px;margin-top:10px;font-size:12.5px;font-weight:500}
.promo-msg--err{color:var(--red)}
.promo-code-inline{padding:1px 7px;background:var(--em-lt);border-radius:4px;font-family:monospace;font-size:12px;color:var(--em);font-weight:700}

/* ── Summary ───── */
.summary-rows{display:flex;flex-direction:column}
.summary-row{display:flex;justify-content:space-between;align-items:center;padding:10px 0;font-size:13.5px;color:var(--ink2);border-bottom:1px dashed var(--border)}
.summary-row:last-child{border-bottom:none}
.summary-count{font-size:12px;color:var(--ink4)}
.summary-row--discount{color:var(--em)}
.discount-amount{color:var(--em);font-weight:700}
.summary-row--free{color:var(--em)}
.free-chip{padding:2px 10px;border-radius:20px;background:var(--em-lt);border:1px solid var(--em-md);font-size:11.5px;font-weight:700;color:var(--em)}
.summary-row--ship{color:var(--amber)}
.ship-fee{font-weight:700;color:var(--amber)}
.summary-sep{height:1px;background:var(--border);margin:14px 0}
.summary-total-row{display:flex;justify-content:space-between;align-items:center}
.summary-total-label{font-size:14px;font-weight:700}
.summary-total-note{font-size:11.5px;color:var(--ink4);margin-top:2px}
.summary-total-amount{font-size:27px;font-weight:800;color:var(--em);letter-spacing:-.02em}
.savings-banner{margin-top:14px;padding:10px 14px;background:linear-gradient(135deg,#fef9c3,#fef3c7);border:1px solid #fcd34d;border-radius:8px;font-size:13px;color:#92400e;font-weight:500}

/* ── Submit ────── */
.submit-btn{width:100%;height:54px;background:var(--em);border:none;border-radius:var(--rad-sm);color:#fff;cursor:pointer;font-family:inherit;font-size:15px;font-weight:700;display:flex;align-items:center;justify-content:center;gap:10px;transition:all .2s cubic-bezier(.4,0,.2,1);box-shadow:0 4px 18px rgba(29,111,216,.28);margin-top:15px}
.submit-btn:hover:not(:disabled){background:var(--em-dk);transform:translateY(-1px);box-shadow:0 6px 24px rgba(29,111,216,.35)}
.submit-btn:active:not(:disabled){transform:translateY(0)}
.submit-btn--disabled{opacity:.5;cursor:not-allowed;box-shadow:none}
.submit-btn--loading{opacity:.8;cursor:not-allowed}

.alert-error{display:flex;align-items:center;gap:8px;padding:12px 14px;margin-bottom:12px;background:#fef2f2;border:1px solid #fecaca;border-radius:var(--rad-sm);color:var(--red);font-size:13px;font-weight:500}

/* ══ VOUCHER MODAL ══════════════════════════════════════════════ */
.vm-overlay{position:fixed;inset:0;z-index:9999;background:rgba(0,0,0,.5);backdrop-filter:blur(4px);display:flex;align-items:center;justify-content:center;padding:20px}
.vm-dialog{background:#fff;border-radius:18px;width:100%;max-width:480px;max-height:82vh;display:flex;flex-direction:column;box-shadow:0 25px 70px rgba(0,0,0,.18);overflow:hidden}

/* Header */
.vm-header{display:flex;align-items:center;justify-content:space-between;padding:20px 22px 16px;border-bottom:1px solid var(--border)}
.vm-header-left{display:flex;align-items:center;gap:12px}
.vm-header-icon{font-size:28px}
.vm-header-title{font-size:16px;font-weight:800;color:var(--ink)}
.vm-header-sub{font-size:12px;color:var(--ink3);margin-top:1px}
.vm-close{width:36px;height:36px;border-radius:10px;background:var(--bg);border:none;cursor:pointer;display:flex;align-items:center;justify-content:center;color:var(--ink3);transition:all .15s}
.vm-close:hover{background:var(--border);color:var(--ink)}

/* Loading / Empty */
.vm-loading{padding:56px 24px;text-align:center;display:flex;flex-direction:column;align-items:center;gap:14px;color:var(--ink3);font-size:14px}
.vm-empty{padding:56px 24px;text-align:center}
.vm-empty-art{margin-bottom:16px}
.vm-empty-title{font-size:15px;font-weight:700;color:var(--ink2);margin-bottom:4px}
.vm-empty-desc{font-size:13px;color:var(--ink4)}

/* Body (scrollable list) */
.vm-body{flex:1;overflow-y:auto;padding:14px 18px;display:flex;flex-direction:column;gap:10px}

/* ── Voucher Card (ticket style) ───────────────────────────────── */
.vc{display:flex;border-radius:12px;overflow:hidden;cursor:pointer;transition:all .18s ease;border:2px solid var(--border);background:#fff;position:relative}
.vc:hover{border-color:var(--em-md);box-shadow:0 2px 12px rgba(29,111,216,.08)}
.vc--selected{border-color:var(--em);background:var(--em-lt);box-shadow:0 0 0 3px rgba(29,111,216,.12)}
.vc--disabled{opacity:.5;cursor:not-allowed;border-color:var(--border)!important;background:#fafafa!important;box-shadow:none!important}

/* Left stripe (ticket stub) */
.vc-left{width:76px;flex-shrink:0;display:flex;align-items:center;justify-content:center;position:relative;padding:16px 6px}
.vc-left--percent{background:linear-gradient(145deg,#f97316,#ea580c)}
.vc-left--amount{background:linear-gradient(145deg,#1d6fd8,#1558b0)}
.vc-left--combo{background:linear-gradient(145deg,#8b5cf6,#7c3aed)}
.vc-left-val{display:flex;flex-direction:column;align-items:center;color:#fff}
.vc-left-num{font-size:22px;font-weight:800;line-height:1}
.vc-left-unit{font-size:10px;font-weight:700;opacity:.85;margin-top:2px;letter-spacing:.04em}

/* Scalloped edge between left and right */
.vc-scallop{position:absolute;right:-5px;top:0;bottom:0;width:10px;display:flex;flex-direction:column;justify-content:space-evenly;align-items:center;z-index:1}
.vc-scallop-dot{width:10px;height:10px;border-radius:50%;background:#fff}
.vc--selected .vc-scallop-dot{background:var(--em-lt)}

/* Right content */
.vc-right{flex:1;padding:12px 14px 12px 16px;min-width:0;display:flex;flex-direction:column;justify-content:center}
.vc-right-top{display:flex;align-items:center;justify-content:space-between;gap:8px}
.vc-code{font-size:13.5px;font-weight:700;color:var(--ink);letter-spacing:.04em}
.vc-name{font-size:12px;color:var(--ink2);margin-top:3px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.vc-meta{display:flex;align-items:center;gap:4px;margin-top:6px;font-size:11px;color:var(--ink4)}
.vc-meta-dot{color:var(--ink5)}
.vc-warn{font-size:11px;color:var(--amber);margin-top:4px;font-weight:600}

/* Check circle */
.vc-check{width:22px;height:22px;border-radius:50%;border:2px solid var(--ink5);flex-shrink:0;display:flex;align-items:center;justify-content:center;transition:all .15s}
.vc-check--on{background:var(--em);border-color:var(--em)}

/* Footer */
.vm-footer{display:flex;gap:10px;padding:14px 18px;border-top:1px solid var(--border);background:#fafbfc}
.vm-btn-clear{flex:0 0 auto;height:44px;padding:0 16px;border-radius:10px;background:transparent;border:1.5px solid var(--border);font-family:inherit;font-size:12.5px;font-weight:600;color:var(--ink3);cursor:pointer;transition:all .15s;display:flex;align-items:center;gap:6px}
.vm-btn-clear:hover{border-color:var(--ink4);color:var(--ink2)}
.vm-btn-clear--has{border-color:var(--red);color:var(--red)}
.vm-btn-clear--has:hover{background:#fef2f2}

.vm-btn-apply{flex:1;height:44px;border-radius:10px;background:var(--ink5);border:none;color:#fff;font-family:inherit;font-size:14px;font-weight:700;cursor:not-allowed;transition:all .18s;display:flex;align-items:center;justify-content:center;gap:8px}
.vm-btn-apply--active{background:var(--em);cursor:pointer;box-shadow:0 4px 14px rgba(29,111,216,.25)}
.vm-btn-apply--active:hover{background:var(--em-dk)}
.vm-btn-preview{font-size:12px;font-weight:600;opacity:.85;background:rgba(255,255,255,.2);padding:2px 8px;border-radius:6px}

/* ── Animations ──── */
.spin-icon{animation:spin .7s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
.slide-fade-enter-active{transition:all .22s ease}.slide-fade-enter-from{opacity:0;transform:translateY(-5px)}.slide-fade-leave-active{transition:all .15s ease}.slide-fade-leave-to{opacity:0}
.msg-fade-enter-active{transition:all .2s ease}.msg-fade-enter-from{opacity:0;transform:translateY(4px)}.msg-fade-leave-active{transition:all .15s ease}.msg-fade-leave-to{opacity:0}
.row-slide-enter-active{transition:opacity .25s ease}.row-slide-enter-from{opacity:0}.row-slide-leave-active{transition:opacity .15s ease}.row-slide-leave-to{opacity:0}
.modal-fade-enter-active{transition:all .25s cubic-bezier(.4,0,.2,1)}.modal-fade-enter-from{opacity:0}.modal-fade-enter-from .vm-dialog{transform:translateY(16px) scale(.97)}.modal-fade-leave-active{transition:all .15s ease}.modal-fade-leave-to{opacity:0}

.sr-only{position:absolute;width:1px;height:1px;overflow:hidden;clip:rect(0,0,0,0)}

@media(max-width:880px){.checkout-body{grid-template-columns:1fr}.checkout-right{order:-1}.summary-total-amount{font-size:23px}}
@media(max-width:600px){.checkout-body{padding:14px 14px 48px}.co-card{padding:18px}.vm-overlay{padding:10px;align-items:flex-end}.vm-dialog{max-height:90vh;border-radius:18px 18px 0 0}.vc-left{width:64px}}
</style>