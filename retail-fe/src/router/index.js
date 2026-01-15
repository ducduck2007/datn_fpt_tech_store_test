import { createRouter, createWebHistory } from "vue-router";
import { getToken, getRole } from "../stores/auth";

import CustomerHome from "../pages/customer/CustomerHome.vue";
import CustomerLogin from "../pages/customer/CustomerLogin.vue";
import CustomerRegister from "../pages/customer/CustomerRegister.vue";

import SystemLogin from "../pages/system/SystemLogin.vue";
import SystemDashboard from "../pages/system/SystemDashboard.vue";

const routes = [
  // ===== CUSTOMER PORTAL =====
  {
    path: "/",
    name: "home",
    component: CustomerHome,
    meta: { portal: "customer" },
  },
  {
    path: "/login",
    name: "login",
    component: CustomerLogin,
    meta: { portal: "customer" },
  },
  {
    path: "/register",
    name: "register",
    component: CustomerRegister,
    meta: { portal: "customer" },
  },

  // giữ tương thích nếu bạn đang dùng /dashboard trước đó
  { path: "/dashboard", redirect: "/" },

  // ===== SYSTEM PORTAL =====
  {
    path: "/system/login",
    name: "system-login",
    component: SystemLogin,
    meta: { portal: "system", hideHeader: true }, // admin login chỉ có form
  },
  {
    path: "/system/dashboard",
    name: "system-dashboard",
    component: SystemDashboard,
    meta: { portal: "system", requiresAuth: true },
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
  const isAuthed = !!token;

  const portal = to.meta?.portal || "customer";
  const isSystemRoute = portal === "system";
  const isCustomer = role === "CUSTOMER";

  // 1) Route cần auth mà chưa login
  if (to.meta?.requiresAuth && !isAuthed) {
    return isSystemRoute ? "/system/login" : "/login";
  }

  // 2) Đã login rồi thì chặn vào trang login/register
  if (isAuthed) {
    if (!isSystemRoute && (to.path === "/login" || to.path === "/register")) {
      // customer portal login/register
      if (isCustomer) return "/";
      return "/system/dashboard";
    }
    if (isSystemRoute && to.path === "/system/login") {
      // system portal login
      if (!isCustomer) return "/system/dashboard";
      return "/";
    }
  }

  // 3) Chặn cross-portal
  if (isAuthed) {
    // system user vào customer portal => chuyển sang system dashboard
    if (!isSystemRoute && !isCustomer) return "/system/dashboard";

    // customer vào system portal => chuyển về home
    if (isSystemRoute && isCustomer) return "/";
  }

  return true;
});

export default router;
