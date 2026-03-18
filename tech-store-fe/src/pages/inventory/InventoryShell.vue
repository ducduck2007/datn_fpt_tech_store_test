<template>
  <el-container class="sys-layout">
    <!-- ═══════════════════════════════════
         SIDEBAR (fixed)
    ═══════════════════════════════════ -->
    <el-aside width="256px" class="sidebar">
      <!-- Brand -->
      <div class="sidebar-brand">
        <div class="brand-logo">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="8" fill="#3b82f6"/>
            <path d="M7 10h14M7 14h10M7 18h12" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="brand-text">
          <span class="brand-name">TechStore</span>
          <span class="brand-sub">Inventory Portal</span>
        </div>
      </div>

      <!-- Role badge -->
      <div class="role-badge">
        <span class="role-dot"></span>
        <span class="role-label">INVENTORY STAFF</span>
      </div>

      <!-- Divider -->
      <div class="sidebar-divider"></div>

      <!-- Nav label + Menu (scrollable riêng) -->
      <div class="sidebar-scroll">
        <div class="nav-section-label">QUẢN LÝ ĐƠN HÀNG</div>

        <el-menu
          router
          :default-active="$route.path"
          class="sidebar-menu"
        >
          <el-menu-item index="/inventory/orders/paid" class="menu-item">
            <template #title>
              <div class="menu-item-inner">
                <div class="menu-icon">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h12M2 8h8M2 12h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <span>Đơn đã thanh toán</span>
                <span class="menu-badge paid-badge">Mới</span>
              </div>
            </template>
          </el-menu-item>

          <el-menu-item index="/inventory/orders/processing" class="menu-item">
            <template #title>
              <div class="menu-item-inner">
                <div class="menu-icon">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M8 5v3l2 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <span>Đang xử lý</span>
              </div>
            </template>
          </el-menu-item>
        </el-menu>
      </div><!-- /sidebar-scroll -->

      <!-- Sidebar footer — ghim cứng dưới cùng -->
      <div class="sidebar-footer">
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
import { useRouter } from "vue-router";
import { confirmModal } from "../../ui/confirm";
import { toast } from "../../ui/toast";
import { clearLastAuthResponse, clearSession } from "../../stores/auth";

const router = useRouter();

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
  overflow: hidden;
}

/* ═══════════════════════════════════
   SIDEBAR — fixed, không scroll tổng
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

.sidebar::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 200px;
  background: linear-gradient(180deg, rgba(59,130,246,0.12) 0%, transparent 100%);
  pointer-events: none;
}

/* BRAND */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  flex-shrink: 0;
}

.brand-logo { flex-shrink: 0; filter: drop-shadow(0 0 8px rgba(59,130,246,0.4)); }
.brand-text { display: flex; flex-direction: column; gap: 1px; }
.brand-name { font-size: 16px; font-weight: 700; color: #f8fafc; letter-spacing: -0.3px; }
.brand-sub  { font-size: 11px; color: #64748b; font-weight: 500; letter-spacing: 0.5px; }

/* ROLE BADGE */
.role-badge {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0 16px 12px;
  background: rgba(59,130,246,0.12);
  border: 1px solid rgba(59,130,246,0.2);
  border-radius: 6px;
  padding: 7px 10px;
  flex-shrink: 0;
}

.role-dot {
  width: 6px; height: 6px;
  background: #22d3ee;
  border-radius: 50%;
  box-shadow: 0 0 6px #22d3ee;
  animation: pulse-dot 2s ease-in-out infinite;
}
@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.5; }
}

.role-label { font-size: 10px; font-weight: 700; color: #7dd3fc; letter-spacing: 1px; }

/* DIVIDER */
.sidebar-divider {
  height: 1px;
  background: rgba(255,255,255,0.06);
  margin: 0 16px 8px;
  flex-shrink: 0;
}

/* ─── Vùng cuộn riêng cho menu ─── */
.sidebar-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
}
.sidebar-scroll::-webkit-scrollbar { display: none; }

/* NAV SECTION LABEL */
.nav-section-label {
  font-size: 10px; font-weight: 700; color: #475569;
  letter-spacing: 1.2px; padding: 8px 20px 8px;
}

/* MENU */
.sidebar-menu {
  border: none !important;
  background: transparent !important;
  padding: 0 10px;
}

.sidebar-menu :deep(.el-menu-item) {
  border-radius: 8px; margin-bottom: 2px;
  height: auto; padding: 0 !important;
  line-height: normal; background: transparent !important;
}
.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255,255,255,0.06) !important;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(59,130,246,0.18) !important;
  border: 1px solid rgba(59,130,246,0.3) !important;
}
.sidebar-menu :deep(.el-menu-item.is-active .menu-item-inner) { color: #93c5fd; }
.sidebar-menu :deep(.el-menu-item.is-active .menu-icon) { color: #3b82f6; }

.menu-item-inner {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; color: #94a3b8;
  font-size: 13.5px; font-weight: 500;
  width: 100%; transition: color 0.15s;
}
.menu-icon {
  width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; color: #64748b; transition: color 0.15s;
}
.menu-item-inner:hover { color: #e2e8f0; }
.menu-item-inner:hover .menu-icon { color: #94a3b8; }

/* BADGE */
.menu-badge {
  margin-left: auto; font-size: 9px; font-weight: 700;
  padding: 2px 6px; border-radius: 4px; letter-spacing: 0.5px;
}
.paid-badge {
  background: rgba(34,197,94,0.15); color: #4ade80;
  border: 1px solid rgba(34,197,94,0.25);
}

/* ─── FOOTER — ghim dưới cùng ─── */
.sidebar-footer {
  padding: 10px 14px 14px;
  border-top: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
}

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
  margin-left: 256px;
  padding: 24px;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
}
.content-main::-webkit-scrollbar { display: none; }
</style>