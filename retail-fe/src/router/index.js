import { createRouter, createWebHistory } from "vue-router";
import { getToken, getRole } from "../stores/auth";

import CustomerLogin from "../pages/customer/CustomerLogin.vue";
import CustomerDashboard from "../pages/customer/CustomerDashboard.vue";
import SystemLogin from "../pages/system/SystemLogin.vue";
import SystemDashboard from "../pages/system/SystemDashboard.vue";

const routes = [
  {
    path: "/",
    name: "customer-login",
    component: CustomerLogin,
    meta: { portal: "customer" },
  },
  {
    path: "/dashboard",
    name: "customer-dashboard",
    component: CustomerDashboard,
    meta: { requiresAuth: true, portal: "customer" },
  },

  {
    path: "/system/login",
    name: "system-login",
    component: SystemLogin,
    meta: { portal: "system" },
  },
  {
    path: "/system/dashboard",
    name: "system-dashboard",
    component: SystemDashboard,
    meta: { requiresAuth: true, portal: "system" },
  },

  // fallback
  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const token = getToken();
  const role = (getRole() || "").toUpperCase();
  const isCustomer = role === "CUSTOMER";
  const isSystemRoute =
    to.meta?.portal === "system" || to.path.startsWith("/system");

  // 1) Route cần auth mà chưa login
  if (to.meta?.requiresAuth && !token) {
    return isSystemRoute ? "/system/login" : "/";
  }

  // 2) Đã login nhưng vào sai portal theo role
  if (token) {
    if (!isSystemRoute && !isCustomer) {
      // user role system mà vào portal customer
      return "/system/dashboard";
    }
    if (isSystemRoute && isCustomer) {
      // customer vào portal system
      return "/dashboard";
    }
  }

  return true;
});

export default router;
