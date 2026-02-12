<template>
  <OrderListBase
    title="Orders (Paid)"
    endpoint="GET /api/orders/paid"
    :loader="ordersApi.listPaid"
  />
</template>

<script setup>
import OrderListBase from "./_OrderListBase.vue";
import { ordersApi } from "../../api/orders.api";
</script>
