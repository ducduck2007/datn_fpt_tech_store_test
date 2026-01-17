<template>
  <OrderListBase
    title="Orders (New)"
    endpoint="GET /api/orders/new"
    :loader="ordersApi.listNew"
  />
</template>

<script setup>
import OrderListBase from "./_OrderListBase.vue";
import { ordersApi } from "../../api/orders.api";
</script>
