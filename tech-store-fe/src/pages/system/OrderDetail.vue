<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Order Detail #{{ orderId }}</div>
          <div class="muted">GET /api/orders/{{ orderId }}</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="reload" :loading="loading">Reload</el-button>
          <el-button type="danger" plain :loading="acting" @click="remove"
            >Delete</el-button
          >
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

        <div class="row g-3">
          <div class="col-12 col-md-4">
            <el-form-item label="paymentMethod">
              <el-select v-model="edit.paymentMethod" placeholder="Select">
                <el-option label="CASH" value="CASH" />
                <el-option label="TRANSFER" value="TRANSFER" />
                <el-option label="CARD" value="CARD" />
              </el-select>
            </el-form-item>
          </div>
          <div class="col-12 col-md-8">
            <el-form-item label="notes">
              <el-input v-model="edit.notes" placeholder="Notes" />
            </el-form-item>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2">
          <el-button :loading="acting" @click="update">Update</el-button>
          <el-button type="warning" plain :loading="acting" @click="cancel"
            >Cancel order</el-button
          >
        </div>

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
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

const route = useRoute();
const router = useRouter();
const orderId = computed(() => route.params.orderId);

const loading = ref(false);
const acting = ref(false);
const error = ref("");

const raw = ref(null);
const order = ref(null);

const edit = reactive({
  paymentMethod: "CASH",
  notes: "",
});

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

    edit.paymentMethod = order.value?.paymentMethod ?? "CASH";
    edit.notes = order.value?.notes ?? "";
  } catch (e) {
    const msg =
      e?.response?.data?.message || e?.message || "Failed to load order";
    error.value = typeof msg === "string" ? msg : JSON.stringify(msg);
    toast("Failed to load order.", "error");
  } finally {
    loading.value = false;
  }
}

async function update() {
  acting.value = true;
  try {
    await ordersApi.update(orderId.value, {
      paymentMethod: edit.paymentMethod,
      notes: edit.notes || "",
    });
    toast("Order updated.", "success");
    await reload();
  } catch {
    toast("Update failed.", "error");
  } finally {
    acting.value = false;
  }
}

async function cancel() {
  const ok = await confirmModal(
    `Cancel order #${orderId.value}?`,
    "Confirm",
    "Cancel",
    true
  );
  if (!ok) return;

  acting.value = true;
  try {
    await ordersApi.cancel(orderId.value);
    toast("Order cancelled.", "success");
    await reload();
  } catch {
    toast("Cancel failed.", "error");
  } finally {
    acting.value = false;
  }
}

async function remove() {
  const ok = await confirmModal(
    `Delete order #${orderId.value}?`,
    "Confirm",
    "Delete",
    true
  );
  if (!ok) return;

  acting.value = true;
  try {
    await ordersApi.remove(orderId.value);
    toast("Order deleted.", "success");
    router.push("/system/orders/new");
  } catch {
    toast("Delete failed.", "error");
  } finally {
    acting.value = false;
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
