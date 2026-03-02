<template>
  <div v-if="order">
    <h2>Order Detail</h2>

    <p>Order: {{ order.orderNumber }}</p>
    <p>Status: {{ order.status }}</p>

    <el-button
      v-if="order.status === 'PAID'"
      type="warning"
      @click="markProcessing"
    >
      Processing
    </el-button>

    <el-button
      v-if="order.status === 'PROCESSING'"
      type="primary"
      @click="markShipping"
    >
      Shipping
    </el-button>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { ordersApi } from "../../api/orders.api";

const route = useRoute();
const order = ref(null);

const load = async () => {
  const res = await ordersApi.getById(route.params.orderId);
  order.value = res.data;
};

const markProcessing = async () => {
  await ordersApi.markAsProcessing(order.value.orderId);
  load();
};

const markShipping = async () => {
  await ordersApi.markAsShipping(order.value.orderId);
  load();
};

onMounted(load);
</script>