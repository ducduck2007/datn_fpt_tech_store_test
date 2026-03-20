<template>
  <el-container class="sys-layout">
    <!-- ═══════════════════════════════════
         SIDEBAR (fixed)
    ═══════════════════════════════════ -->
    <el-aside width="256px" class="sidebar">
      <!-- Brand -->
      <div class="sidebar-brand" @click="go('/system/dashboard')">
        <div class="brand-logo">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="8" fill="#3b82f6"/>
            <path d="M7 10h14M7 14h10M7 18h12" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="brand-text">
          <span class="brand-name">TechStore</span>
          <span class="brand-sub">System Portal</span>
        </div>
      </div>

      <!-- User pill -->
      <div class="role-badge">
        <div class="role-avatar">{{ initials(auth.displayName) }}</div>
        <div class="role-info">
          <span class="role-name text-truncate">{{ auth.displayName }}</span>
          <span class="role-tag">{{ (auth.role || "SYSTEM").toUpperCase() }}</span>
        </div>
      </div>

      <!-- Divider -->
      <div class="sidebar-divider"></div>

      <!-- Menu (scrollable riêng) -->
      <div class="sidebar-scroll">
      <el-menu
        :default-active="activePath"
        class="sidebar-menu"
        router
      >
        <!-- Dashboard -->
        <el-menu-item index="/system/dashboard" class="menu-item">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <rect x="1" y="1" width="5.5" height="5.5" rx="1.5" stroke="currentColor" stroke-width="1.4"/>
                  <rect x="8.5" y="1" width="5.5" height="5.5" rx="1.5" stroke="currentColor" stroke-width="1.4"/>
                  <rect x="1" y="8.5" width="5.5" height="5.5" rx="1.5" stroke="currentColor" stroke-width="1.4"/>
                  <rect x="8.5" y="8.5" width="5.5" height="5.5" rx="1.5" stroke="currentColor" stroke-width="1.4"/>
                </svg>
              </div>
              Dashboard
            </div>
          </template>
        </el-menu-item>

        <!-- Management -->
        <div class="nav-section-label">QUẢN LÝ</div>

        <el-sub-menu index="mgmt" class="sub-menu">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <circle cx="7.5" cy="4.5" r="2.5" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M1.5 13c0-3.31 2.69-6 6-6s6 2.69 6 6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                </svg>
              </div>
              Management
            </div>
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

        <!-- Orders -->
        <el-sub-menu index="orders" class="sub-menu">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <rect x="1" y="2" width="13" height="11" rx="2" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M1 6h13" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M5 10h5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                </svg>
              </div>
              Orders
            </div>
          </template>
          <el-menu-item index="/system/order-dashboard">Order Dashboard</el-menu-item>
          <el-menu-item index="/system/orders/filter">Order Filter</el-menu-item>
        </el-sub-menu>

        <!-- Returns -->
        <el-sub-menu index="returns" class="sub-menu">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <path d="M12 4H5a3 3 0 0 0 0 6h7" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                  <path d="M10 2l2 2-2 2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              Returns
            </div>
          </template>
          <el-menu-item index="/system/returns/pending">Pending</el-menu-item>
          <el-menu-item index="/system/returns/all">All Returns</el-menu-item>
        </el-sub-menu>

        <!-- Reports & Finance -->
        <div class="nav-section-label">BÁO CÁO & TÀI CHÍNH</div>

        <el-menu-item index="/system/reports" class="menu-item">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <path d="M1 11l3.5-4 3 3 3-5 3 3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              Reports
            </div>
          </template>
        </el-menu-item>

        <el-menu-item index="/system/pricing" class="menu-item">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <circle cx="7.5" cy="7.5" r="6" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M7.5 4.5v6M5.5 6a2 2 0 0 1 4 0c0 1.5-2 2-2 3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                </svg>
              </div>
              Pricing
            </div>
          </template>
        </el-menu-item>

        <el-menu-item index="/system/payments" class="menu-item">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <rect x="1" y="3" width="13" height="9" rx="2" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M1 6.5h13" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M4 9.5h3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                </svg>
              </div>
              Payment
            </div>
          </template>
        </el-menu-item>

        <el-menu-item index="/system/promotions" class="menu-item">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <path d="M2 2l11 11M9 2h4v4M6 13H2v-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              Promotions
            </div>
          </template>
        </el-menu-item>

        <!-- System -->
        <div class="nav-section-label">HỆ THỐNG</div>

        <el-sub-menu index="audits" class="sub-menu">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <path d="M7.5 1L2 4v4c0 3.31 2.35 6.41 5.5 7 3.15-.59 5.5-3.69 5.5-7V4L7.5 1Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
                  <path d="M5 7.5l2 2 3-3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              Audit
            </div>
          </template>
          <el-menu-item index="/system/audit-logs">Audit Logs</el-menu-item>
          <el-menu-item index="/system/audit-report">Audit Report</el-menu-item>
          <el-menu-item index="/system/security-logs">Security Logs</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="settings" class="sub-menu">
          <template #title>
            <div class="menu-item-inner">
              <div class="menu-icon">
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <circle cx="7.5" cy="7.5" r="2" stroke="currentColor" stroke-width="1.4"/>
                  <path d="M7.5 1v1.5M7.5 12.5V14M14 7.5h-1.5M2.5 7.5H1M12.36 3.14l-1.06 1.06M3.7 10.8l-1.06 1.06M12.36 11.86l-1.06-1.06M3.7 4.2L2.64 3.14" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                </svg>
              </div>
              Settings
            </div>
          </template>
          <el-menu-item index="/system/settings/currency">Currency</el-menu-item>
        </el-sub-menu>
      </el-menu>
      </div><!-- /sidebar-scroll -->

      <!-- Sidebar footer -->
      <div class="sidebar-footer">
        <button class="btn-customer-view" @click="go('/')">
          <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
            <circle cx="6.5" cy="4" r="2.5" stroke="currentColor" stroke-width="1.3"/>
            <path d="M1 12c0-3.03 2.46-5.5 5.5-5.5S12 8.97 12 12" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
          </svg>
          Customer View
        </button>
        <button class="logout-btn" @click="logout">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M5 2H2.5A1.5 1.5 0 0 0 1 3.5v7A1.5 1.5 0 0 0 2.5 12H5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
            <path d="M9 4.5L12 7l-3 2.5M5 7h7" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Đăng xuất
        </button>
      </div>
    </el-aside>

    <!-- ═══════════════════════════════════
         MAIN CONTENT (no topbar)
    ═══════════════════════════════════ -->
    <el-main class="content-main">
      <router-view />
    </el-main>
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

<style scoped>
* { box-sizing: border-box; }

.sys-layout {
  height: 100vh;
  background: #f1f5f9;
  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
  display: flex;
  overflow: hidden; /* Ngăn layout tổng bị scroll */
}

/* ═══════════════════════════════════
   SIDEBAR — fixed, không scroll ngoài
═══════════════════════════════════ */
.sidebar {
  position: fixed !important;
  top: 0;
  left: 0;
  height: 100vh;
  width: 256px !important;
  background: #0f172a;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* Sidebar không scroll — chỉ .sidebar-scroll cuộn */
  border-right: 1px solid rgba(255,255,255,0.05);
  z-index: 100;
}

/* Vùng menu cuộn độc lập, footer không bị đẩy xuống */
.sidebar-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
}
.sidebar-scroll::-webkit-scrollbar { display: none; }

.sidebar::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 200px;
  background: linear-gradient(180deg, rgba(59,130,246,0.1) 0%, transparent 100%);
  pointer-events: none;
  z-index: 0;
}

/* BRAND */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  cursor: pointer;
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}
.sidebar-brand:hover .brand-name { color: #93c5fd; }
.brand-logo { flex-shrink: 0; filter: drop-shadow(0 0 8px rgba(59,130,246,0.4)); }
.brand-text { display: flex; flex-direction: column; gap: 1px; }
.brand-name {
  font-size: 16px; font-weight: 700; color: #f8fafc;
  letter-spacing: -0.3px; transition: color 0.15s;
}
.brand-sub { font-size: 11px; color: #64748b; font-weight: 500; letter-spacing: 0.5px; }

/* USER PILL */
.role-badge {
  display: flex; align-items: center; gap: 9px;
  margin: 0 14px 12px;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px; padding: 9px 11px; flex-shrink: 0;
}
.role-avatar {
  width: 30px; height: 30px; border-radius: 7px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 700; color: white; flex-shrink: 0;
}
.role-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.role-name {
  font-size: 12.5px; font-weight: 600; color: #e2e8f0;
  display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.role-tag {
  font-size: 9.5px; font-weight: 700; color: #22d3ee; letter-spacing: 0.8px;
  background: rgba(34,211,238,0.1); border: 1px solid rgba(34,211,238,0.2);
  padding: 1px 6px; border-radius: 4px; display: inline-block; width: fit-content;
}

/* DIVIDER */
.sidebar-divider {
  height: 1px; background: rgba(255,255,255,0.06);
  margin: 0 14px 8px; flex-shrink: 0;
}

/* NAV SECTION LABELS */
.nav-section-label {
  font-size: 9.5px; font-weight: 700; color: #334155;
  letter-spacing: 1.2px; padding: 10px 20px 5px; flex-shrink: 0;
}

/* ─── MENU ─── */
.sidebar-menu {
  border: none !important;
  background: transparent !important;
  padding: 0 8px;
  flex: 1;
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: 7px; margin-bottom: 1px;
  height: auto !important; padding: 0 !important;
  line-height: normal !important;
  background: transparent !important; color: #94a3b8 !important;
}
.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255,255,255,0.06) !important;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(59,130,246,0.16) !important;
  border: 1px solid rgba(59,130,246,0.28) !important;
}
.sidebar-menu :deep(.el-menu-item.is-active .menu-item-inner) { color: #93c5fd; }
.sidebar-menu :deep(.el-menu-item.is-active .menu-icon) { color: #3b82f6; }

.sidebar-menu :deep(.el-sub-menu__title) {
  border-radius: 7px !important; height: auto !important;
  line-height: normal !important; padding: 0 !important;
  background: transparent !important; color: #94a3b8 !important; margin-bottom: 1px;
}
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255,255,255,0.06) !important; color: #e2e8f0 !important;
}
.sidebar-menu :deep(.el-sub-menu__icon-arrow) { color: #475569 !important; right: 12px; }
.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important; padding: 2px 0 4px 8px;
}
.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  font-size: 13.5px !important; color: #94a3b8 !important;
  padding: 9px 10px 9px 36px !important; height: auto !important;
  min-height: 38px !important; line-height: 1.4 !important;
  border-radius: 6px !important; margin-bottom: 2px;
}
.sidebar-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(255,255,255,0.06) !important; color: #e2e8f0 !important;
}
.sidebar-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(59,130,246,0.14) !important;
  border: 1px solid rgba(59,130,246,0.24) !important; color: #93c5fd !important;
}

.menu-item-inner {
  display: flex; align-items: center; gap: 9px;
  padding: 9px 10px; color: inherit;
  font-size: 13px; font-weight: 500;
  width: 100%; transition: color 0.15s;
}
.menu-icon {
  width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; color: #475569; transition: color 0.15s;
}

/* SIDEBAR FOOTER */
.sidebar-footer {
  padding: 10px 14px 14px;
  border-top: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* Customer View button */
.btn-customer-view {
  width: 100%;
  display: flex; align-items: center; justify-content: center; gap: 7px;
  padding: 9px 14px;
  background: rgba(59,130,246,0.08);
  border: 1px solid rgba(59,130,246,0.2);
  border-radius: 8px;
  color: #93c5fd;
  font-size: 13px; font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  font-family: inherit;
}
.btn-customer-view:hover {
  background: rgba(59,130,246,0.15);
  border-color: rgba(59,130,246,0.4);
  color: #bfdbfe;
}

/* Logout button */
.logout-btn {
  width: 100%;
  display: flex; align-items: center; justify-content: center; gap: 7px;
  padding: 9px 14px;
  background: rgba(239,68,68,0.08);
  border: 1px solid rgba(239,68,68,0.2);
  border-radius: 8px;
  color: #f87171;
  font-size: 13px; font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  font-family: inherit;
}
.logout-btn:hover {
  background: rgba(239,68,68,0.15);
  border-color: rgba(239,68,68,0.35);
  color: #fca5a5;
}

/* ═══════════════════════════════════
   CONTENT — offset sidebar, ẩn scrollbar
═══════════════════════════════════ */
.content-main {
  margin-left: 256px;      /* Đẩy nội dung sang phải tránh bị sidebar che */
  padding: 24px;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  /* Ẩn scrollbar nhưng vẫn scroll được */
  scrollbar-width: none;
}
.content-main::-webkit-scrollbar { display: none; }
</style>