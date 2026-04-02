<template>
  <el-container style="height: 100vh; overflow: hidden;">

    <!-- ═══════════════════════════════════
         SIDEBAR
    ═══════════════════════════════════ -->
    <el-aside
      width="256px"
      style="position: fixed; top: 0; left: 0; height: 100vh; background: #0f172a;
             display: flex; flex-direction: column; overflow: hidden;
             border-right: 1px solid rgba(255,255,255,0.06); z-index: 100;"
    >
      <!-- Brand -->
      <el-button
        text
        @click="go('/system/dashboard')"
        style="padding: 20px 20px 16px; height: auto; justify-content: flex-start; width: 100%;"
      >
        <el-space :size="10">
          <el-avatar
            :size="36"
            shape="square"
            style="background: #3b82f6; border-radius: 8px; flex-shrink: 0;"
          >
            <el-icon :size="18"><Monitor /></el-icon>
          </el-avatar>
          <el-space direction="vertical" :size="1">
            <el-text tag="b" style="color: #f8fafc; font-size: 15px; letter-spacing: -0.3px;">TechStore</el-text>
            <el-text size="small" style="color: #64748b; letter-spacing: 0.5px;">System Portal</el-text>
          </el-space>
        </el-space>
      </el-button>

      <!-- User pill -->
      <el-card
        shadow="never"
        style="margin: 0 14px 12px; background: rgba(255,255,255,0.05);
               border: 1px solid rgba(255,255,255,0.08); border-radius: 8px;"
        :body-style="{ padding: '9px 11px' }"
      >
        <el-space :size="9">
          <el-avatar
            :size="30"
            shape="square"
            style="background: linear-gradient(135deg, #3b82f6, #6366f1); border-radius: 7px;
                   flex-shrink: 0; font-size: 10px; font-weight: 700;"
          >
            {{ initials(auth.displayName) }}
          </el-avatar>
          <el-space direction="vertical" :size="3" style="min-width: 0;">
            <el-text
              size="small"
              style="color: #e2e8f0; font-weight: 600; display: block;
                     overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 148px;"
            >{{ auth.displayName }}</el-text>
            <el-tag
              size="small"
              effect="plain"
              style="color: #93c5fd; background: rgba(59,130,246,0.12);
                     border-color: rgba(59,130,246,0.28); font-size: 9px; letter-spacing: 0.8px;
                     height: 18px; padding: 0 6px;"
            >{{ (auth.role || "SYSTEM").toUpperCase() }}</el-tag>
          </el-space>
        </el-space>
      </el-card>

      <el-divider style="margin: 0 0 4px; border-color: rgba(255,255,255,0.06);" />

      <!-- Scrollable menu -->
      <el-scrollbar style="flex: 1;">
        <el-menu
          router
          :default-active="activePath"
          background-color="#0f172a"
          text-color="#94a3b8"
          active-text-color="#93c5fd"
          style="border: none; padding: 0 8px;"
        >
          <!-- Dashboard -->
          <el-menu-item index="/system/dashboard">
            <el-icon><Grid /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>

          <!-- Quản lý -->
          <el-menu-item-group title="QUẢN LÝ">
            <el-sub-menu index="mgmt">
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>Management</span>
              </template>
              <el-menu-item index="/system/users">Users</el-menu-item>
              <el-menu-item index="/system/customers">Customers</el-menu-item>
              <el-menu-item index="/system/Toployalcustomers">Top Loyal Customers</el-menu-item>
              <el-menu-item index="/system/Loyaltysummaryadmin">Loyalty Summary</el-menu-item>
              <el-menu-item index="/system/categories">Categories</el-menu-item>
              <el-menu-item index="/system/products">Products</el-menu-item>
              <el-menu-item index="/system/serials">Serials</el-menu-item>
              <el-menu-item index="/system/dashboard-products">Báo cáo Sản phẩm</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="orders">
              <template #title>
                <el-icon><List /></el-icon>
                <span>Orders</span>
              </template>
              <el-menu-item index="/system/order-dashboard">Order Dashboard</el-menu-item>
              <el-menu-item index="/system/orders/filter">Order Filter</el-menu-item>
            </el-sub-menu>

          </el-menu-item-group>

          <!-- Báo cáo & Tài chính -->
          <el-menu-item-group title="BÁO CÁO & TÀI CHÍNH">
            <el-menu-item index="/system/reports">
              <el-icon><DataLine /></el-icon>
              <span>Reports</span>
            </el-menu-item>
            <el-menu-item index="/system/pricing">
              <el-icon><Money /></el-icon>
              <span>Pricing</span>
            </el-menu-item>
            <el-menu-item index="/system/payments">
              <el-icon><CreditCard /></el-icon>
              <span>Payment</span>
            </el-menu-item>
            <el-menu-item index="/system/promotions">
              <el-icon><Discount /></el-icon>
              <span>Promotions</span>
            </el-menu-item>
          </el-menu-item-group>

          <!-- Hệ thống -->
          <el-menu-item-group title="HỆ THỐNG">
            <el-sub-menu index="audits">
              <template #title>
                <el-icon><Lock /></el-icon>
                <span>Security</span>
              </template>
              <el-menu-item index="/system/audit-dashboard">Audit Dashboard</el-menu-item>
              <el-menu-item index="/system/audit-logs">Audit Logs</el-menu-item>
              <el-menu-item index="/system/user-login-logs">User Login Logs</el-menu-item>
              <el-menu-item index="/system/audit-report">Audit Report</el-menu-item>
              <el-menu-item index="/system/security-logs">Security Logs</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="settings">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>Settings</span>
              </template>
              <el-menu-item index="/system/settings/currency">Currency</el-menu-item>
            </el-sub-menu>
          </el-menu-item-group>
        </el-menu>
      </el-scrollbar>

      <!-- Sidebar footer -->
      <el-space
        direction="vertical"
        fill
        :size="6"
        style="padding: 10px 14px 14px; border-top: 1px solid rgba(255,255,255,0.06);"
      >
        <el-button type="danger" plain @click="logout" style="width: 100%;">
          <el-icon><SwitchButton /></el-icon>
          Đăng xuất
        </el-button>
      </el-space>
    </el-aside>

    <!-- ═══════════════════════════════════
         MAIN CONTENT
    ═══════════════════════════════════ -->
    <el-main style="margin-left: 256px; height: 100vh; overflow-y: auto; padding: 24px;">
      <router-view />
    </el-main>

  </el-container>
</template>

<script setup>
import {
  CreditCard, DataLine, Discount, Grid, List, Lock,
  Monitor, Money, RefreshLeft, Setting, SwitchButton, User, UserFilled,
} from "@element-plus/icons-vue";
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { confirmModal } from "../../ui/confirm";
import { toast } from "../../ui/toast";
import {
  clearLastAuthResponse,
  clearSession,
  useAuthStore,
} from "../../stores/auth";

const route  = useRoute();
const router = useRouter();
const auth   = useAuthStore();

const activePath = computed(() => route.path);

const initials = (name) =>
  name ? name.split(" ").slice(-2).map(n => n[0]).join("").toUpperCase() : "SY";

function go(path) {
  if (path && path !== route.path) router.push(path);
}

async function logout() {
  const ok = await confirmModal("Do you want to log out?", "Confirm", "Log out", true);
  if (!ok) return;
  clearSession();
  clearLastAuthResponse();
  toast("Logged out.", "success");
  router.push("/system/login");
}
</script>

<style></style>