<template>
  <OrderListBase
    title="Orders (Shipping)"
    endpoint="GET /api/orders/shipping"
    :loader="ordersApi.listShipping"
    :actions="actions"
  />
</template>

<script setup>
import OrderListBase from "./_OrderListBase.vue";
import { ordersApi } from "../../api/orders.api";
import { toast } from "../../ui/toast";

const actions = [
  {
    label: "Mark Delivered",
    type: "success",
    async onClick(row, reload) {
      try {
        await ordersApi.markAsDelivered(row.orderId);
        toast("Đã đánh dấu đơn hàng là DELIVERED", "success");
        reload();
      } catch (e) {
        toast("Không thể cập nhật trạng thái", "error");
      }
    },
  },
];
</script>
