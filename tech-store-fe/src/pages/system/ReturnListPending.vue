<template>
  <el-card shadow="never">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <div class="kicker">System</div>
        <div class="title">Return Requests (RETURN_REQUESTED)</div>
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
      <el-table-column prop="id" label="Return ID" width="100" />
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

      <el-table-column prop="reason" label="Reason" />

      <el-table-column label="Actions" width="240" align="center">
        <template #default="{ row }">
          <el-button
            type="success"
            size="small"
            :loading="actionId === row.id"
            @click="approve(row.id)"
          >
            Approve
          </el-button>

          <el-button
            type="danger"
            size="small"
            :loading="actionId === row.id"
            @click="openReject(row.id)"
          >
            Reject
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Reject Dialog -->
    <el-dialog
      v-model="rejectDialog"
      title="Reject Return Request"
      width="450px"
    >
      <el-form>
        <el-form-item label="Reject reason">
          <el-input
            v-model="rejectReason"
            type="textarea"
            rows="4"
            placeholder="Enter reject reason"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rejectDialog = false">Cancel</el-button>
        <el-button
          type="danger"
          :loading="actionId === rejectId"
          @click="confirmReject"
        >
          Reject
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { returnsApi } from "../../api/returns.api";
import { toast } from "../../ui/toast";

const STATUS = "PENDING";

const returns = ref([]);
const loading = ref(false);
const actionId = ref(null);

// reject dialog
const rejectDialog = ref(false);
const rejectReason = ref("");
const rejectId = ref(null);

onMounted(load);

async function load() {
  loading.value = true;
  try {
    const res = await returnsApi.getByStatus(STATUS);
    returns.value = res?.data?.data || res?.data || [];
  } catch (e) {
    toast(
      e?.response?.data?.message || "Failed to load return requests",
      "error"
    );
  } finally {
    loading.value = false;
  }
}

async function approve(returnId) {
  actionId.value = returnId;
  try {
    await returnsApi.approve(returnId);
    toast("✅ Return approved", "success");
    load();
  } catch (e) {
    toast(e?.response?.data?.message || "Approve failed", "error");
  } finally {
    actionId.value = null;
  }
}

function openReject(id) {
  rejectId.value = id;
  rejectReason.value = "";
  rejectDialog.value = true;
}

async function confirmReject() {
  if (!rejectReason.value) return;

  actionId.value = rejectId.value;
  try {
    await returnsApi.reject(rejectId.value, rejectReason.value);
    toast("⛔ Return rejected", "warning");
    rejectDialog.value = false;
    load();
  } catch (e) {
    toast(e?.response?.data?.message || "Reject failed", "error");
  } finally {
    actionId.value = null;
  }
}

function formatMoney(val) {
  if (val == null) return "-";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(val);
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
