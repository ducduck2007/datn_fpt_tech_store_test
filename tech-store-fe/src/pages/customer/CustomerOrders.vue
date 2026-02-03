<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex justify-content-between align-items-center">
        <div>
          <div class="kicker">Customer</div>
          <div class="title">My Orders</div>
        </div>
      </div>

      <el-divider />

      <el-alert
        v-if="error"
        type="error"
        :title="error"
        show-icon
        class="mb-3"
      />

      <el-table
        :data="orders"
        v-loading="loading"
        border
        style="width: 100%"
      >
        <!-- Order number -->
        <el-table-column
          prop="orderNumber"
          label="Order No"
          min-width="160"
        />

        <!-- Created at -->
        <el-table-column
          label="Created At"
          width="170"
        >
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <!-- Payment -->
        <el-table-column
          prop="paymentMethod"
          label="Payment"
          width="120"
        />

        <!-- Channel -->
        <el-table-column
          prop="channel"
          label="Channel"
          width="120"
        />

        <!-- Total -->
        <el-table-column
          label="Total"
          width="150"
          align="right"
        >
          <template #default="{ row }">
            {{ formatMoney(row.totalAmount) }}
          </template>
        </el-table-column>

        <!-- Status -->
        <el-table-column
          label="Status"
          width="130"
        >
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- Actions -->
        <el-table-column
          label="Actions"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              size="small"
              @click="$router.push(`/orders/${row.orderId}`)"
            >
              View
            </el-button>

            <el-button
              v-if="canCancel(row.status)"
              size="small"
              type="danger"
              :loading="cancelingId === row.orderId"
              @click="onCancel(row.orderId)"
            >
              Cancel
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { onMounted, ref } from "vue";
import { ordersApi } from "../../api/orders.api";
import { ElMessageBox, ElMessage } from "element-plus";

const orders = ref([]);
const loading = ref(false);
const error = ref("");
const cancelingId = ref(null);

onMounted(loadOrders);

// =========================
// LOAD ORDERS
// =========================
async function loadOrders() {
  loading.value = true;
  error.value = "";
  try {
    const res = await ordersApi.getMyOrders();
    orders.value = res?.data || [];
  } catch (e) {
    error.value =
      e?.response?.data?.message || "Failed to load orders";
  } finally {
    loading.value = false;
  }
}

// =========================
// CANCEL – SAME AS DETAIL
// =========================
async function onCancel(orderId) {
  try {
    await ElMessageBox.confirm(
      "Are you sure you want to cancel this order?",
      "Confirm",
      { type: "warning" }
    );

    cancelingId.value = orderId;

    await ordersApi.cancel(orderId);

    ElMessage.success("Order cancelled successfully");

    await loadOrders();
  } catch (e) {
    if (e?.response?.data?.message) {
      ElMessage.error(e.response.data.message);
    }
  } finally {
    cancelingId.value = null;
  }
}

// =========================
// HELPERS
// =========================
function canCancel(status) {
  // KHỚP BACKEND (sửa nếu backend khác)
  return ["PENDING", "CONFIRMED"].includes(status);
}

function statusType(status) {
  const types = {
    PENDING: "warning",
    PAID: "success",
    CANCELLED: "danger",
    PROCESSING: "info",
  };
  return types[status] || "info";
}

function formatDate(date) {
  if (!date) return "-";
  return new Date(date).toLocaleString("vi-VN");
}

function formatMoney(value) {
  if (value == null) return "-";
  return Number(value).toLocaleString("vi-VN") + " ₫";
}
</script>
<style scoped>
.kicker {
  font-size: 12px;
  font-weight: 800;
  opacity: 0.7;
  text-transform: uppercase;
}
.title {
  font-size: 18px;
  font-weight: 900;
}
</style>
