<template>
  <OrderListBase
    title="Orders (Delivered)"
    endpoint="GET /api/orders/delivered"
    :loader="ordersApi.listDelivered"
  />
</template>

<script setup>
import OrderListBase from "./_OrderListBase.vue";
import { ordersApi } from "../../api/orders.api";
</script>
