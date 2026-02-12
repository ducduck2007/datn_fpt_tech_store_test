import { createRouter, createWebHistory } from "vue-router";
import { getRole, getToken } from "../stores/auth";

// ===== Customer =====
import CustomerHome from "../pages/customer/CustomerHome.vue";
import CustomerLogin from "../pages/customer/CustomerLogin.vue";
import CustomerRegister from "../pages/customer/CustomerRegister.vue";
import CustomerOrderCreate from "../pages/customer/OrderCreate.vue";
import CustomerOrderDetail from "../pages/customer/OrderDetail.vue";
import CustomerProfile from "../pages/customer/CustomerProfile.vue";
import CartPage from "../pages/customer/CartPage.vue";
import CustomerOrders from "../pages/customer/CustomerOrders.vue";

// ===== System (Admin) =====
import SystemLogin from "../pages/system/SystemLogin.vue";
import SystemShell from "../pages/system/SystemShell.vue";
import SystemDashboard from "../pages/system/SystemDashboard.vue";
import UserManager from "../pages/system/UserManager.vue";
import CustomerManager from "../pages/system/CustomerManager.vue";
import CategoryManager from "../pages/system/CategoryManager.vue";
import ProductManager from "../pages/system/ProductManager.vue";
import OrderListNew from "../pages/system/OrderListNew.vue";
import OrderListProcessing from "../pages/system/OrderListProcessing.vue";
import OrderListPaid from "../pages/system/OrderListPaid.vue";
import OrderListDelivered from "../pages/system/OrderListDelivered.vue";
import OrderListShipping from "../pages/system/OrderListShipping.vue";
import ReturnListPending from "../pages/system/ReturnListPending.vue";
import ReturnListAll from "../pages/system/ReturnListAll.vue";
import OrderFilter from "../pages/system/OrderFilter.vue";
import SystemOrderDetail from "../pages/system/OrderDetail.vue";
import PricingManager from "../pages/system/PricingManager.vue";
import PromotionManager from "../pages/system/PromotionManager.vue";
import SettingsCurrency from "../pages/system/SettingsCurrency.vue";
import PaymentManagement from "../pages/system/PaymentManagement.vue";
import PaymentSuccess from "../pages/customer/PaymentSuccess.vue";
import Birthday from "../pages/system/BirthdayManager.vue"
import Customerspendinganalytics from "../pages/system/Customerspendinganalytics.vue";
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
    meta: { portal: "customer", hideHeader: true },
  },
  {
    path: "/register",
    name: "register",
    component: CustomerRegister,
    meta: { portal: "customer", hideHeader: true },
  },
  {
    path: "/profile",
    name: "customer-profile",
    component: CustomerProfile,
    meta: { portal: "customer", requiresAuth: true },
  },
  {
    path: "/orders/new",
    name: "customer-order-create",
    component: CustomerOrderCreate,
    meta: { portal: "customer", requiresAuth: true },
  },
  {
    path: "/orders/:orderId",
    name: "customer-order-detail",
    component: CustomerOrderDetail,
    meta: { portal: "customer", requiresAuth: true },
  },
  {
    path: "/payment/success/:paymentId",
    name: "payment-success",
    component: PaymentSuccess,
    meta: { portal: "customer", requiresAuth: true },
  },
  {
    path: "/cart",
    name: "cart",
    component: CartPage,
    meta: { portal: "customer", requiresAuth: true },
  },
  {
    path: "/my-orders",
    name: "my orders",
    component: CustomerOrders,
    meta: { portal: "customer", requiresAuth: true },
  },

  // ===== SYSTEM LOGIN =====
  {
    path: "/system/login",
    name: "system-login",
    component: SystemLogin,
    meta: { portal: "system", hideHeader: true },
  },

  // ===== SYSTEM AREA =====
  {
    path: "/system",
    component: SystemShell,
    meta: { portal: "system", requiresAuth: true },
    children: [
      { path: "", redirect: "/system/dashboard" },
      {
        path: "dashboard",
        name: "system-dashboard",
        component: SystemDashboard,
        meta: { title: "Dashboard" },
      },

      // Admin modules
      {
        path: "users",
        name: "system-users",
        component: UserManager,
        meta: { title: "User Management" },
      },
       {
     path: "customer-spending",
        name: "system-customers-spend",
        component: Customerspendinganalytics,
        meta: { title: "Customers & Loyalty" },
      },
      {
        path: "customers",
        name: "system-customers",
        component: CustomerManager,
        meta: { title: "Customers & Loyalty" },
      },
      {
        path: "categories",
        name: "system-categories",
        component: CategoryManager,
        meta: { title: "Categories" },
      },
      {
        path: "products",
        name: "system-products",
        component: ProductManager,
        meta: { title: "Products" },
      },

      // Orders
      {
        path: "orders/new",
        name: "system-orders-new",
        component: OrderListNew,
        meta: { title: "Orders (New)" },
      },
      {
        path: "orders/processing",
        name: "system-orders-processing",
        component: OrderListProcessing,
        meta: { title: "Orders (Processing)" },
      },
      {
        path: "orders/paid",
        name: "system-orders-paid",
        component: OrderListPaid,
        meta: { title: "Orders (Paid)" },
      },
      {
        path: "orders/delivered",
        name: "system-orders-delivered",
        component: OrderListDelivered,
        meta: { title: "Orders (Delivered)" },
      },
      {
        path: "orders/shipping",
        name: "system-orders-shipping",
        component: OrderListShipping,
        meta: { title: "Orders (Shipping)" },
      },
      {
        path: "returns/pending",
        name: "system-returns-pending",
        component: ReturnListPending,
        meta: { title: "Returns (Pending)" },
      },
      {
        path: "orders/filter",
        name: "order-filter",
        component: OrderFilter,
        meta: { title: "Order Filter" },
      },
      {
        path: "returns/all",
        name: "system-returns-all",
        component: ReturnListAll,
        meta: { title: "All Returns" },
      },

      {
        path: "orders/:orderId",
        name: "system-order-detail",
        component: SystemOrderDetail,
        meta: { title: "Return Requests" },
      },

      // Pricing / Promotions / Settings
      {
        path: "pricing",
        name: "system-pricing",
        component: PricingManager,
        meta: { title: "Pricing" },
      },
      {
        path: "promotions",
        name: "system-promotions",
        component: PromotionManager,
        meta: { title: "Promotions" },
      },
      {
        path: "settings/currency",
        name: "system-settings-currency",
        component: SettingsCurrency,
        meta: { title: "Settings - Currency" },
      },
      // Payments
      {
        path: "payments",
        name: "system-payments",
        component: PaymentManagement,
        meta: { title: "Payment History" },
      },
      {
        path: "birthday",
        name: "Birthday Manager",
        component: Birthday,
        meta: { title: "Settings - Birthday Manager" },
      },
    ],
  },

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

  // 1) Route requires auth
  if (to.meta?.requiresAuth && !isAuthed) {
    return isSystemRoute ? "/system/login" : "/login";
  }

  // 2) Logged-in user goes to login/register pages
  if (isAuthed) {
    if (!isSystemRoute && (to.path === "/login" || to.path === "/register")) {
      return isCustomer ? "/" : "/system/dashboard";
    }
    if (isSystemRoute && to.path === "/system/login") {
      return !isCustomer ? "/system/dashboard" : "/";
    }
  }

  // 3) Cross-portal protection
  if (isAuthed) {
    if (!isSystemRoute && !isCustomer) return "/system/dashboard";
    if (isSystemRoute && isCustomer) return "/";
  }

  return true;
});

export default router;
