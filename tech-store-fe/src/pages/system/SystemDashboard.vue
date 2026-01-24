<!-- FILE: src/pages/system/SystemDashboard.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">System</div>
          <div class="title">Dashboard</div>
          <div class="muted">Quick access to admin modules</div>
        </div>
        <el-button @click="reload">Reload</el-button>
      </div>

      <el-divider />

      <div class="row g-3">
        <div class="col-12 col-md-6 col-xl-4" v-for="c in cards" :key="c.title">
          <el-card
            shadow="hover"
            class="h-100"
            @click="$router.push(c.to)"
            style="cursor: pointer"
          >
            <div class="fw-bold">{{ c.title }}</div>
            <div class="muted mt-1">{{ c.desc }}</div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { toast } from "../../ui/toast";

const cards = [
  { title: "Users", desc: "Manage system users & roles", to: "/system/users" },
  {
    title: "Customers",
    desc: "Customers & loyalty (VIP/REGULAR)",
    to: "/system/customers",
  },
  {
    title: "Categories",
    desc: "Create & manage product categories",
    to: "/system/categories",
  },
  { title: "Products", desc: "Create & list products", to: "/system/products" },
  {
    title: "Orders (New)",
    desc: "Process new orders",
    to: "/system/orders/new",
  },
  {
    title: "Orders (Processing)",
    desc: "Processing queue",
    to: "/system/orders/processing",
  },
  {
    title: "Pricing",
    desc: "Variant pricing & histories",
    to: "/system/pricing",
  },
  {
    title: "Promotions",
    desc: "Promo codes & campaigns",
    to: "/system/promotions",
  },
  {
    title: "Settings",
    desc: "Default currency",
    to: "/system/settings/currency",
  },
  {
  title: "Payment History",
  desc: "View all payment transactions",
  to: "/system/payments",
}
];

function reload() {
  toast("Dashboard refreshed.", "info");
}
</script>

<style scoped>
.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
}
.title {
  font-weight: 900;
  font-size: 18px;
}
.muted {
  color: rgba(15, 23, 42, 0.62);
  font-size: 13px;
}
</style>
