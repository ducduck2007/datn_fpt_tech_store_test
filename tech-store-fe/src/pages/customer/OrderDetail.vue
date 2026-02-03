<!-- FILE: src/pages/customer/OrderDetail.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Customer</div>
          <div class="title">Order Detail #{{ orderId }}</div>
          <div class="muted">GET /api/orders/{{ orderId }}</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="$router.push('/orders/new')"
            >Create another</el-button
          >
          <el-button @click="reload" :loading="loading">Reload</el-button>

          <el-button
            type="danger"
            v-if="canCancel"
            @click="handleCancel"
          >
            Hủy đơn hàng
          </el-button>

          
          <!-- Nút thanh toán - chỉ hiện khi order chưa thanh toán -->
          <el-button
            v-if="order && order.status === 'PENDING'"
            type="primary"
            @click="handlePayment"
            :loading="paymentLoading"
          >
            <i class="bi bi-credit-card me-1"></i>
            Pay Now
          </el-button>
        </div>
      </div>

      <el-divider />

      <el-skeleton v-if="loading" :rows="6" animated />
      <template v-else>
        <el-alert
          v-if="error"
          :title="error"
          type="error"
          show-icon
          class="mb-3"
        />

        <!-- Status Badge -->
        <div v-if="order" class="mb-3">
          <el-tag
            :type="getStatusType(order.status)"
            size="large"
          >
            {{ order.status }}
          </el-tag>
          <el-tag
            v-if="order.paymentStatus"
            :type="getPaymentStatusType(order.paymentStatus)"
            size="large"
            class="ms-2"
          >
            Payment: {{ order.paymentStatus }}
          </el-tag>
        </div>

        <el-descriptions v-if="order" :column="2" border>
          <el-descriptions-item label="Order ID">{{
            order.id ?? order.orderId ?? orderId
          }}</el-descriptions-item>
          <el-descriptions-item label="Status">{{
            order.status ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="Customer ID">{{
            order.customerId ?? order.customer?.id ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="Payment Method">{{
            order.paymentMethod ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="Channel">{{
            order.channel ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="Total Amount">
            <span class="fw-bold text-primary">
              {{ formatCurrency(order.totalAmount) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="Notes" :span="2">{{
            order.notes ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="Created At">{{
            formatDate(order.createdAt)
          }}</el-descriptions-item>
          <el-descriptions-item label="Paid At">{{
            order.paidAt ? formatDate(order.paidAt) : "—"
          }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="h mb-2">Order Items</div>
        <el-table :data="items" border>
          <el-table-column prop="variantId" label="Variant ID" width="120" />
          <el-table-column prop="productName" label="Product" min-width="200" />
          <el-table-column prop="quantity" label="Quantity" width="100" align="center" />
          <el-table-column prop="price" label="Unit Price" width="150">
            <template #default="{ row }">
              {{ formatCurrency(row.price) }}
            </template>
          </el-table-column>
          <el-table-column label="Subtotal" width="150">
            <template #default="{ row }">
              {{ formatCurrency(row.price * row.quantity) }}
            </template>
          </el-table-column>
        </el-table>

        <el-divider />

        <el-collapse>
          <el-collapse-item title="Raw JSON">
            <pre class="json">{{ JSON.stringify(raw, null, 2) }}</pre>
          </el-collapse-item>
        </el-collapse>
      </template>
    </el-card>

    <!-- Payment Dialog -->
    <el-dialog
      v-model="paymentDialog"
      title="Process Payment"
      width="500px"
    >
      <el-form :model="paymentForm" label-width="140px">
        <el-form-item label="Order ID">
          <el-input :value="orderId" disabled />
        </el-form-item>
        <el-form-item label="Amount">
          <el-input :value="formatCurrency(order?.totalAmount)" disabled />
        </el-form-item>
        <el-form-item label="Payment Method" required>
          <el-select v-model="paymentForm.method" placeholder="Select payment method" style="width: 100%">
            <el-option label="Cash" value="CASH" />
            <el-option label="Bank Transfer" value="BANK_TRANSFER" />
            <el-option label="Credit Card" value="CREDIT_CARD" />
            <el-option label="E-Wallet" value="E_WALLET" />
          </el-select>
        </el-form-item>
        <el-form-item label="Transaction Ref">
          <el-input v-model="paymentForm.transactionRef" placeholder="Optional transaction reference" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentDialog = false">Cancel</el-button>
        <el-button
          type="primary"
          @click="confirmPayment"
          :loading="paymentLoading"
          :disabled="!paymentForm.method"
        >
          Confirm Payment
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { paymentsApi } from "../../api/payments";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

const route = useRoute();
const router = useRouter();
const orderId = computed(() => route.params.orderId);



const loading = ref(false);
const paymentLoading = ref(false);
const error = ref("");
const raw = ref(null);
const order = ref(null);
const paymentDialog = ref(false);

const paymentForm = ref({
  method: "CASH",
  transactionRef: "",
});

function pickOrder(payload) {
  const root = payload?.data ?? payload;
  return root?.data ?? root;
}

const canCancel = computed(() => {
  return ['PENDING'].includes(order.value?.status)
})

const fetchOrder = async () => {
  const res = await ordersApi.getById(route.params.orderId)
  order.value = res.data
}

const handleCancel = async () => {
  const ok = await confirmModal(
    "Bạn có chắc chắn muốn hủy đơn hàng này?",
    "Xác nhận hủy đơn",
    "Hủy đơn",
    true
  );

  if (!ok) return;

  try {
    await ordersApi.cancel(order.value.orderId);

    toast("Đơn hàng đã được hủy", "success");

    // load lại chi tiết đơn
    await fetchOrder();
  } catch (err) {
    toast("Hủy đơn thất bại, vui lòng thử lại", "error");
  }
};



const items = computed(() => {
  const o = order.value || {};
  const list = o.items ?? o.orderItems ?? o.lines ?? [];
  return Array.isArray(list) ? list : [];
});

async function reload() {
  loading.value = true;
  error.value = "";
  try {
    const res = await ordersApi.getById(orderId.value);
    raw.value = res?.data ?? null;
    order.value = pickOrder(res?.data);
  } catch (e) {
    const msg =
      e?.response?.data?.message || e?.message || "Failed to load order";
    error.value = typeof msg === "string" ? msg : JSON.stringify(msg);
    toast("Failed to load order.", "error");
  } finally {
    loading.value = false;
  }
}

function handlePayment() {
  if (!order.value) return;
  
  // Reset form
  paymentForm.value = {
    method: "CASH",
    transactionRef: `TXN-${Date.now()}`,
  };
  
  paymentDialog.value = true;
}

async function confirmPayment() {
  if (!paymentForm.value.method) {
    toast("Please select a payment method", "warning");
    return;
  }

  paymentLoading.value = true;
  try {
    const payload = {
      orderId: parseInt(orderId.value),
      method: paymentForm.value.method,
      transactionRef: paymentForm.value.transactionRef || `TXN-${Date.now()}`,
    };

    const { data } = await paymentsApi.create(payload);
    
    toast("Payment processed successfully!", "success");
    paymentDialog.value = false;
    
    // Chuyển đến trang Payment Success với payment ID
    router.push(`/payment/success/${data.id}`);
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Payment failed";
    toast(msg, "error");
    console.error("Payment error:", e);
  } finally {
    paymentLoading.value = false;
  }
}

function formatCurrency(amount) {
  if (!amount) return "₫0";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
}

function formatDate(date) {
  if (!date) return "N/A";
  return new Date(date).toLocaleString("vi-VN");
}

function getStatusType(status) {
  const types = {
    PENDING: "warning",
    PAID: "success",
    CANCELLED: "danger",
    PROCESSING: "info",
  };
  return types[status] || "info";
}

function getPaymentStatusType(status) {
  const types = {
    UNPAID: "warning",
    PAID: "success",
    REFUNDED: "danger",
  };
  return types[status] || "info";
}

onMounted(reload);
</script>

<style scoped>
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
.h {
  font-weight: 900;
  font-size: 14px;
}
.json {
  margin: 0;
  font-size: 12px;
  background: rgba(2, 6, 23, 0.04);
  border: 1px solid rgba(2, 6, 23, 0.08);
  border-radius: 10px;
  padding: 12px;
  overflow: auto;
}
</style>