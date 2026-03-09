<template>
  <div class="order-page">
    <!-- Header -->
    <div class="order-header">
      <button class="back-btn" @click="$router.push('/')">
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
        >
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
            <button class="btn-outline" @click="$router.push('/')">
              Khám phá sản phẩm
            </button>
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
            <input
              v-model="delivery.address"
              class="form-input"
              placeholder="Số nhà, đường, quận..."
            />
          </div>

          <div class="form-group">
            <label>Ghi chú</label>
            <textarea
              v-model="delivery.note"
              class="form-input"
              rows="3"
              placeholder="Ví dụ: gọi trước khi giao"
            />
          </div>
        </div>
      </div>

      <!-- RIGHT -->
      <div class="order-right">
        <!-- PAYMENT -->
        <div class="section-card">
          <div class="section-label">Phương thức thanh toán</div>

          <div class="payment-options">
            <label
              v-for="opt in paymentOptions"
              :key="opt.value"
              class="payment-opt"
              :class="{ active: form.paymentMethod === opt.value }"
            >
              <input
                type="radio"
                :value="opt.value"
                v-model="form.paymentMethod"
              />

              <span class="opt-icon">{{ opt.icon }}</span>

              <span class="opt-label">{{ opt.label }}</span>

              <span v-if="form.paymentMethod === opt.value" class="check-mark">
                ✓
              </span>
            </label>
          </div>
        </div>

        <!-- DELIVERY -->
        <div class="section-card">
          <div class="section-label">Hình thức giao hàng</div>

          <div class="payment-options">
            <label
              class="payment-opt"
              :class="{ active: deliveryMethod === 'STORE' }"
            >
              <input type="radio" value="STORE" v-model="deliveryMethod" />
              <span class="opt-icon">🏬</span>
              <span class="opt-label">Nhận tại cửa hàng</span>
              <span v-if="deliveryMethod === 'STORE'" class="check-mark"
                >✓</span
              >
            </label>

            <label
              class="payment-opt"
              :class="{ active: deliveryMethod === 'HOME' }"
            >
              <input type="radio" value="HOME" v-model="deliveryMethod" />
              <span class="opt-icon">🚚</span>
              <span class="opt-label">Giao tại nhà</span>
              <span v-if="deliveryMethod === 'HOME'" class="check-mark">✓</span>
            </label>
          </div>
        </div>

        <!-- NOTES -->
        <div class="section-card">
          <div class="section-label">Ghi chú (tùy chọn)</div>

          <textarea v-model="form.notes" class="notes-input" rows="3" />
        </div>

        <div v-if="alert" class="alert-box">
          {{ alert }}
        </div>

        <button class="submit-btn" :disabled="loading" @click="submit">
          <span v-if="!loading"> Đặt hàng ngay </span>

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
import { onMounted, reactive, ref } from "vue";
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

const delivery = reactive({
  address: "",
  note: "",
});

const form = reactive({
  customerId: null,
  paymentMethod: "CASH",
  channel: "ONLINE",
  notes: "",
  items: [],
});

const paymentOptions = [
  { value: "CASH", label: "Tiền mặt", icon: "💵" },
  { value: "TRANSFER", label: "Chuyển khoản", icon: "🏦" },
  { value: "CARD", label: "Thẻ tín dụng", icon: "💳" },
];

function pickOrderId(payload) {
  const root = payload?.data ?? payload;

  return (
    root?.id ?? root?.orderId ?? root?.data?.id ?? root?.data?.orderId ?? null
  );
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

  if (form.paymentMethod === "TRANSFER") {
    notes += "\nThanh toán chuyển khoản";
  }

  if (form.paymentMethod === "CARD") {
    notes += "\nThanh toán thẻ";
  }

  if (deliveryMethod.value === "HOME") {
    notes += `
Giao tại nhà
Địa chỉ: ${delivery.address}
Ghi chú: ${delivery.note}`;
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
    const res = await ordersApi.create({
      customerId: Number(form.customerId),
      paymentMethod: form.paymentMethod,
      channel: form.channel,
      notes: buildNotes(),
      items: form.items,
    });

    const orderId = pickOrderId(res?.data);

    toast("Đặt hàng thành công! 🎉", "success");

    await cartStore.clearCart();

    if (orderId) {
      router.push(`/orders/${orderId}`);
    } else {
      toast("Không tìm thấy mã đơn hàng", "warning");
    }
  } catch (e) {
    alert.value = "Đặt hàng thất bại";
  }

  loading.value = false;
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
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1.5px solid #E5E7EB;
  border-radius: 50px;
  background: #FFFFFF;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.back-btn:hover {
  border-color: #3B82F6;
  color: #3B82F6;
  background: #EFF6FF;
}

.header-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.step-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #3B82F6;
  background: #EFF6FF;
  padding: 2px 10px;
  border-radius: 50px;
  width: fit-content;
}
.header-title h1 {
  font-size: 22px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
}

/* Layout */
.order-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}
@media (max-width: 768px) {
  .order-layout {
    grid-template-columns: 1fr;
  }
}

/* Cards */
.section-card {
  background: #FFFFFF;
  border: 1.5px solid #E5E7EB;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
}
.section-label {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: #64748B;
  margin-bottom: 18px;
}

/* Empty cart */
.empty-cart {
  text-align: center;
  padding: 40px 0 20px;
  color: #94A3B8;
}
.empty-icon {
  font-size: 40px;
  margin-bottom: 10px;
}
.empty-cart p {
  font-size: 14px;
  margin-bottom: 16px;
}
.btn-outline {
  padding: 8px 20px;
  border: 1.5px solid #3B82F6;
  border-radius: 50px;
  color: #3B82F6;
  background: #FFFFFF;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-outline:hover {
  background: #EFF6FF;
}

/* Items */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #F8FAFC;
  border-radius: 12px;
  border: 1px solid #F1F5F9;
  transition: border-color 0.15s;
}
.item-row:hover {
  border-color: #BFDBFE;
}
.item-icon {
  width: 40px;
  height: 40px;
  background: #EFF6FF;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3B82F6;
  flex-shrink: 0;
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: 14px;
  font-weight: 600;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-meta {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 2px;
}
.item-qty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  flex-shrink: 0;
}
.qty-label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  color: #94A3B8;
  letter-spacing: 0.06em;
}
.qty-value {
  font-size: 18px;
  font-weight: 800;
  color: #0F172A;
  line-height: 1;
}

/* Payment options */
.payment-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.payment-opt {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 2px solid #F1F5F9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
  position: relative;
}
.payment-opt input {
  display: none;
}
.payment-opt:hover {
  border-color: #BFDBFE;
  background: #F8FAFF;
}
.payment-opt.active {
  border-color: #3B82F6;
  background: #EFF6FF;
}
.opt-icon {
  font-size: 20px;
}
.opt-label {
  font-size: 14px;
  font-weight: 600;
  color: #0F172A;
  flex: 1;
}
.check-mark {
  font-size: 13px;
  font-weight: 800;
  color: white;
  background: #3B82F6;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Notes */
.notes-input {
  width: 100%;
  border: 1.5px solid #E5E7EB;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  font-family: "Inter", sans-serif;
  resize: none;
  color: #374151;
  transition: border-color 0.15s;
  box-sizing: border-box;
}
.notes-input:focus {
  outline: none;
  border-color: #3B82F6;
}
.notes-input::placeholder {
  color: #CBD5E1;
}

/* Alert */
.alert-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 12px;
  font-size: 13px;
  color: #DC2626;
  margin-bottom: 16px;
}

/* Submit */
.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #1E293B, #0F172A);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  font-family: "Inter", sans-serif;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.3);
}
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}
.submit-btn span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spinner-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.spinner {
  width: 16px;
  height: 16px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.order-note {
  font-size: 11px;
  color: #94A3B8;
  text-align: center;
  margin-top: 12px;
  line-height: 1.5;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}

.form-group label {
  font-size: 12px;
  font-weight: 600;
  color: #64748B;
}

.form-input {
  width: 100%;
  border: 1.5px solid #E5E7EB;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  font-family: "Inter", sans-serif;
  transition: border-color 0.15s;
}

.form-input:focus {
  outline: none;
  border-color: #3B82F6;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.vietqr-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.vietqr-img {
  width: 210px;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
}

.qr-info {
  font-size: 13px;
  color: #374151;
  text-align: center;
}
</style>
