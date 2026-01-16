import { createRouter, createWebHistory } from "vue-router";
import { getToken, getRole } from "../stores/auth";

// --- Customer Imports ---
import CustomerHome from "../pages/customer/CustomerHome.vue";
import CustomerLogin from "../pages/customer/CustomerLogin.vue";
import CustomerRegister from "../pages/customer/CustomerRegister.vue";

// --- System Imports ---
import SystemLogin from "../pages/system/SystemLogin.vue";
import SystemDashboard from "../pages/system/SystemDashboard.vue";
// 1. Import Component Quản lý khách hàng
import CustomerManager from "../pages/system/CustomerManager.vue"; 

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

  // Redirect dashboard cũ về home (nếu cần)
  { path: "/dashboard", redirect: "/" },

  // ===== SYSTEM PORTAL (Admin/Staff) =====
  {
    path: "/system/login",
    name: "system-login",
    component: SystemLogin,
    meta: { portal: "system", hideHeader: true },
  },
  {
    path: "/system/dashboard",
    name: "system-dashboard",
    component: SystemDashboard,
    meta: { portal: "system", requiresAuth: true },
  },
  // 2. Thêm Route Quản lý khách hàng vào đây
  {
    path: "/system/customers",
    name: "system-customers",
    component: CustomerManager,
    meta: { 
        portal: "system", 
        requiresAuth: true // Bắt buộc đăng nhập
    },
  },

  // fallback
  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// --- Navigation Guard (Giữ nguyên logic của bạn) ---
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
      if (isCustomer) return "/";
      return "/system/dashboard";
    }
    if (isSystemRoute && to.path === "/system/login") {
      if (!isCustomer) return "/system/dashboard";
      return "/";
    }
  }

  // 3) Chặn cross-portal
  if (isAuthed) {
    // Admin/Staff vào trang Customer => cho về Dashboard Admin
    if (!isSystemRoute && !isCustomer) return "/system/dashboard";

    // Customer vào trang System => đá về Home
    if (isSystemRoute && isCustomer) return "/";
  }

  return true;
});

export default router;