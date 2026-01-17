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

        <el-descriptions v-if="order" :column="2" border>
          <el-descriptions-item label="id">{{
            order.id ?? order.orderId ?? orderId
          }}</el-descriptions-item>
          <el-descriptions-item label="status">{{
            order.status ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="customerId">{{
            order.customerId ?? order.customer?.id ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="paymentMethod">{{
            order.paymentMethod ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="channel">{{
            order.channel ?? "—"
          }}</el-descriptions-item>
          <el-descriptions-item label="notes">{{
            order.notes ?? "—"
          }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="h mb-2">Items</div>
        <el-table :data="items" border>
          <el-table-column prop="variantId" label="variantId" width="220" />
          <el-table-column prop="quantity" label="quantity" width="160" />
          <el-table-column prop="price" label="price" />
        </el-table>

        <el-divider />

        <el-collapse>
          <el-collapse-item title="Raw JSON">
            <pre class="json">{{ JSON.stringify(raw, null, 2) }}</pre>
          </el-collapse-item>
        </el-collapse>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { toast } from "../../ui/toast";

const route = useRoute();
const orderId = computed(() => route.params.orderId);

const loading = ref(false);
const error = ref("");
const raw = ref(null);
const order = ref(null);

function pickOrder(payload) {
  const root = payload?.data ?? payload;
  return root?.data ?? root;
}

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
