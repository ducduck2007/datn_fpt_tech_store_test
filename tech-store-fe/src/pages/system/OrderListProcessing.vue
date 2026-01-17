<template>
  <OrderListBase
    title="Orders (Processing)"
    endpoint="GET /api/orders/processing"
    :loader="ordersApi.listProcessing"
  />
</template>

<script setup>
import OrderListBase from "./_OrderListBase.vue";
import { ordersApi } from "../../api/orders.api";
</script>
