<template>
  <el-card shadow="never">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <div class="kicker">System</div>
        <div class="title">All Returns</div>
      </div>
      <el-button @click="load" :loading="loading">Reload</el-button>
    </div>

    <el-divider />

    <el-table
      :data="returns"
      v-loading="loading"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="Return ID" width="90" />
      <el-table-column prop="orderId" label="Order ID" width="100" />
      <el-table-column prop="orderNumber" label="Order No" width="160" />

      <el-table-column prop="productName" label="Product" min-width="180" />
      <el-table-column prop="variantName" label="Variant" min-width="140" />

      <el-table-column prop="quantity" label="Qty" width="80" align="center" />

      <el-table-column label="Refund Amount" width="160" align="right">
        <template #default="{ row }">
          {{ formatMoney(row.refundAmount) }}
        </template>
      </el-table-column>

      <el-table-column label="Status" width="160">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="Refund Status" width="160">
        <template #default="{ row }">
          <el-tag :type="refundStatusType(row.refundStatus)">
            {{ row.refundStatus }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="reason" label="Reason" />

      <el-table-column prop="note" label="Staff Note" />

      <el-table-column label="Created At" width="170">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="Processed At" width="170">
        <template #default="{ row }">
          {{ formatDate(row.refundedAt) }}
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { returnsApi } from "../../api/returns.api";
import { toast } from "../../ui/toast";

const returns = ref([]);
const loading = ref(false);

onMounted(load);

async function load() {
  loading.value = true;
  try {
    const res = await returnsApi.getAll();
    returns.value = res?.data?.data || res?.data || [];
  } catch (e) {
    toast(
      e?.response?.data?.message || "Failed to load returns",
      "error"
    );
  } finally {
    loading.value = false;
  }
}

function formatMoney(val) {
  if (val == null) return "-";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(val);
}

function formatDate(val) {
  if (!val) return "-";
  return new Date(val).toLocaleString("vi-VN");
}

function statusType(status) {
  const map = {
    PENDING: "warning",
    RETURN_REQUESTED: "warning",
    APPROVED: "success",
    REJECTED: "danger",
  };
  return map[status] || "info";
}

function refundStatusType(status) {
  const map = {
    PENDING: "warning",
    REFUNDED: "success",
    FAILED: "danger",
  };
  return map[status] || "info";
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
