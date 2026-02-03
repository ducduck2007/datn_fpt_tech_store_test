<!-- FILE: src/pages/customer/OrderCreate.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Customer</div>
          <div class="title">Create Order</div>
          <div class="muted">POST /api/orders</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="$router.push('/')">Back</el-button>
          <el-button type="primary" :loading="loading" :disabled="loading" @click="submit"
            >Submit</el-button
          >
        </div>
      </div>

      <el-divider />

      <el-alert
        v-if="alert"
        :title="alert"
        type="error"
        show-icon
        class="mb-3"
      />

      <el-form
        :model="form"
        label-position="top"
        class="row g-3"
        @submit.prevent
      >
        <div class="col-12 col-md-4">
          <el-form-item label="customerId">
            <el-input
              v-model.number="form.customerId"
              placeholder="Your customerId"
            />
          </el-form-item>
        </div>

        <div class="col-12 col-md-4">
          <el-form-item label="paymentMethod">
            <el-select v-model="form.paymentMethod" placeholder="Select">
              <el-option label="CASH" value="CASH" />
              <el-option label="TRANSFER" value="TRANSFER" />
              <el-option label="CARD" value="CARD" />
            </el-select>
          </el-form-item>
        </div>

        <div class="col-12 col-md-4">
          <el-form-item label="channel">
            <el-select v-model="form.channel" placeholder="Select">
              <el-option label="ONLINE" value="ONLINE" />
              <el-option label="STORE" value="STORE" />
              <el-option label="PHONE" value="PHONE" />
            </el-select>
          </el-form-item>
        </div>

        <div class="col-12">
          <el-form-item label="notes">
            <el-input
              v-model="form.notes"
              type="textarea"
              :rows="2"
              placeholder="Optional notes"
            />
          </el-form-item>
        </div>
      </el-form>

      <el-divider />

      <div class="d-flex align-items-center justify-content-between">
        <div class="h">Items (from cart)</div>
      </div>

      <el-table :data="form.items" class="mt-2" border>
        <el-table-column label="variantId" width="200">
          <template #default="{ row }">
            <div>
              <div class="fw-bold">{{ row.productName }}</div>
              <div class="muted small">Variant ID: {{ row.variantId }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="quantity" width="220">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="999" disabled/>
          </template>
        </el-table-column>
      </el-table>


      <el-divider />

      <el-collapse>
        <el-collapse-item title="Payload preview">
          <pre class="json">{{ payloadPreview }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { useAuthStore } from "../../stores/auth";
import { toast } from "../../ui/toast";
import { customersApi } from "../../api/customers.api";
import { cartApi } from "../../api/cart.api";
import { useCartStore } from "../../stores/cart";

const router = useRouter();

const cartStore = useCartStore();

const loading = ref(false);
const alert = ref("");

const form = reactive({
  customerId: null,
  paymentMethod: "CASH",
  channel: "ONLINE",
  notes: "",
  items: [],
});

function pickOrderId(payload) {
  const root = payload?.data ?? payload;
  return (
    root?.id ?? root?.data?.id ?? root?.orderId ?? root?.data?.orderId ?? null
  );
}


const payloadPreview = computed(() => JSON.stringify(form, null, 2));

async function submit() {
  alert.value = "";
  if (!form.customerId) {
    alert.value = "customerId is required.";
    return;
  }
  const cleanedItems = (form.items || [])
    .filter((x) => x && x.variantId && x.quantity)
    .map((x) => ({
      variantId: Number(x.variantId),
      quantity: Number(x.quantity),
    }));

  if (cleanedItems.length === 0) {
    alert.value = "At least 1 item with variantId + quantity is required.";
    return;
  }

  loading.value = true;
  try {
    const res = await ordersApi.create({
      customerId: Number(form.customerId),
      paymentMethod: form.paymentMethod,
      channel: form.channel,
      notes: form.notes || "",
      items: cleanedItems,
    });

    const orderId = pickOrderId(res?.data);
    toast("Order created.", "success");

    if (orderId) {
    await cartStore.refreshCount();
    await cartStore.clearCart();
    router.push(`/orders/${orderId}`);
    }
    else toast("Created, but cannot detect orderId from response.", "warning");
  } catch (e) {
    const msg =
      e?.response?.data?.message || e?.message || "Create order failed";
    alert.value = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    // =========================
    // 1. LOAD CUSTOMER PROFILE
    // =========================
    const profileRes = await customersApi.getProfile();
    const customerId =
      profileRes?.data?.id ??
      profileRes?.data?.data?.id ??
      null;

    if (!customerId) {
      alert.value = "Cannot detect customerId. Please login again.";
      return;
    }
    form.customerId = Number(customerId);

    // =========================
    // 2. LOAD CART ITEMS
    // =========================
    const cartRes = await cartApi.getItems();
    const cartItems = Array.isArray(cartRes?.data)
      ? cartRes.data
      : cartRes?.data?.data || [];

    if (cartItems.length === 0) {
      alert.value = "Your cart is empty.";
      return;
    }

    form.items = cartItems.map(item => ({
      variantId: item.variantId,
      quantity: item.quantity,
      productName: item.productName, // nếu BE có
    }));
  } catch (e) {
    alert.value = "Failed to load order data.";
  }
});

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
