<template>
  <div>
    <h2>Paid Orders</h2>

    <el-table :data="orders" border>

      <el-table-column prop="orderNumber" label="Order"/>
      <el-table-column prop="status" label="Status"/>

      <el-table-column label="Actions">
        <template #default="{ row }">

          <el-button
            type="warning"
            @click="markProcessing(row.orderId)"
          >
            Processing
          </el-button>

          <el-button @click="goDetail(row.orderId)">
            Detail
          </el-button>

        </template>
      </el-table-column>

    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";

const router = useRouter();
const orders = ref([]);

const loadOrders = async () => {
  const res = await ordersApi.listPaid();
  orders.value = res.data;
};

const markProcessing = async (orderId) => {
  await ordersApi.markAsProcessing(orderId);
  loadOrders();
};

const goDetail = (orderId) => {
  router.push(`/inventory/orders/${orderId}`);
};

onMounted(loadOrders);
</script>