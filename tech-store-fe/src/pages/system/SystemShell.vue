<!-- FILE: src/pages/system/SystemShell.vue -->
<template>
  <el-container class="sys">
    <el-aside width="260px" class="aside">
      <div class="brand" @click="go('/system/dashboard')">
        <div class="brand-name">Tech Store</div>
        <div class="brand-sub">System Portal</div>
      </div>

      <div class="pill">
        <div class="pill-k">Signed in as</div>
        <div class="pill-v text-truncate">{{ auth.displayName }}</div>
        <el-tag class="mt-2" type="success" effect="light">{{
          (auth.role || "SYSTEM").toUpperCase()
        }}</el-tag>
      </div>

      <el-menu :default-active="activePath" class="menu" router>
        <el-menu-item index="/system/dashboard">Dashboard</el-menu-item>

        <el-sub-menu index="mgmt">
          <template #title>Management</template>
          <el-menu-item index="/system/users">Users</el-menu-item>
          <el-menu-item index="/system/customers">Customers</el-menu-item>
          <el-menu-item index="/system/customer-spending">Customers - Spending</el-menu-item>
          <el-menu-item index="/system/birthday">Birthday Manager</el-menu-item>
          <el-menu-item index="/system/categories">Categories</el-menu-item>
          <el-menu-item index="/system/products">Products</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="orders">
          <template #title>Orders</template>
          <el-menu-item index="/system/orders/new">New</el-menu-item>
          <el-menu-item index="/system/orders/processing"
            >Processing</el-menu-item
          >
          <el-menu-item index="/system/orders/paid"> Paid </el-menu-item>
          <el-menu-item index="/system/orders/delivered">
            Delivered
          </el-menu-item>
          <el-menu-item index="/system/orders/shipping">Shipping</el-menu-item>
          <el-menu-item index="/system/orders/filter">
            Order Filter
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="returns">
          <template #title>Returns</template>
          <el-menu-item index="/system/returns/pending"> Pending </el-menu-item>
          <el-menu-item index="/system/returns/all"> All Returns </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/system/pricing">Pricing</el-menu-item>
        <el-menu-item index="/system/payments">Payment</el-menu-item>
        <el-menu-item index="/system/promotions">Promotions</el-menu-item>

        <el-sub-menu index="settings">
          <template #title>Settings</template>
          <el-menu-item index="/system/settings/currency"
            >Currency</el-menu-item
          >
        </el-sub-menu>
      </el-menu>

      <div class="aside-foot">
        <el-button type="danger" plain class="w-100" @click="logout"
          >Logout</el-button
        >
      </div>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="d-flex align-items-center justify-content-between w-100">
          <div class="page-title">{{ pageTitle }}</div>
          <div class="d-flex align-items-center gap-2">
            <el-button @click="go('/')" plain>Go to Customer</el-button>
          </div>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { confirmModal } from "../../ui/confirm";
import { toast } from "../../ui/toast";
import {
  clearLastAuthResponse,
  clearSession,
  useAuthStore,
} from "../../stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const activePath = computed(() => route.path);
const pageTitle = computed(() => route.meta?.title || "System");

function go(path) {
  if (path && path !== route.path) router.push(path);
}

async function logout() {
  const ok = await confirmModal(
    "Do you want to log out?",
    "Confirm",
    "Log out",
    true,
  );
  if (!ok) return;

  clearSession();
  clearLastAuthResponse();
  toast("Logged out.", "success");
  router.push("/system/login");
}
</script>

<style scoped>
.sys {
  min-height: 100vh;
  background: #f6f7fb;
}

.aside {
  background: #0b1220;
  color: rgba(255, 255, 255, 0.92);
  padding: 14px 12px;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.brand {
  padding: 10px 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  cursor: pointer;
}
.brand-name {
  font-weight: 900;
  letter-spacing: -0.2px;
}
.brand-sub {
  font-size: 12px;
  opacity: 0.7;
}

.pill {
  margin-top: 12px;
  padding: 10px 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
}
.pill-k {
  font-size: 12px;
  opacity: 0.7;
}
.pill-v {
  font-weight: 800;
}

.menu {
  margin-top: 12px;
  border-right: none;
}

.aside-foot {
  margin-top: 14px;
}

.topbar {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  display: flex;
  align-items: center;
}

.page-title {
  font-weight: 900;
  letter-spacing: -0.2px;
}

.main {
  padding: 16px;
}
</style>
