<template>
  <div class="sales-layout">
    <!-- ═══════════════════════════════
         SIDEBAR (fixed left)
    ═══════════════════════════════ -->
    <aside class="sidebar">
      <!-- Brand -->
      <div class="sidebar-brand">
        <div class="brand-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            <line x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="1.8"/>
            <path d="M16 10a4 4 0 0 1-8 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="brand-text">
          <span class="brand-name">TechStore</span>
          <span class="brand-sub">Sales POS</span>
        </div>
      </div>

      <!-- User pill -->
      <div class="user-pill">
        <div class="user-avatar">{{ initials(auth.displayName) }}</div>
        <div class="user-info">
          <span class="user-name">{{ auth.displayName }}</span>
          <span class="user-role">SALES</span>
        </div>
      </div>

      <div class="sidebar-divider"></div>

      <!-- Nav -->
      <nav class="sidebar-nav">
        <div class="nav-section-label">BÁN HÀNG</div>

        <router-link to="/sales/pos" class="nav-item">
          <div class="nav-icon">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
              <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.6"/>
              <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </div>
          <span>Bán hàng tại quầy</span>
        </router-link>

        <router-link to="/sales/pickup" class="nav-item">
          <div class="nav-icon">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
              <path d="M20 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z" stroke="currentColor" stroke-width="1.6"/>
              <path d="M16 3H8l-2 4h12l-2-4z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
              <path d="M12 12v4M10 14h4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </div>
          <span>Nhận hàng tại quầy</span>
        </router-link>

        <router-link to="/sales/history" class="nav-item">
          <div class="nav-icon">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.6"/>
              <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </div>
          <span>Lịch sử đơn hàng</span>
        </router-link>
      </nav>

      <!-- Footer -->
      <div class="sidebar-footer">
        <button class="logout-btn" @click="doLogout">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            <polyline points="16 17 21 12 16 7" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="21" y1="12" x2="9" y2="12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          Đăng xuất
        </button>
      </div>
    </aside>

    <!-- ═══════════════════════════════
         CONTENT (fixed right of sidebar)
    ═══════════════════════════════ -->
    <main class="sales-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";

const router = useRouter();
const auth = useAuthStore();

function initials(name = "") {
  return (name || "").split(" ").map((w) => w[0] || "").slice(0, 2).join("").toUpperCase() || "SL";
}

function doLogout() {
  auth.clearSession();
  router.replace("/sales/login");
}
</script>

<style scoped>
.sales-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  font-family: 'Inter', 'Segoe UI', system-ui, sans-serif;
}

/* ── Sidebar ── */
.sidebar {
  width: 220px;
  min-width: 220px;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background: #0f172a;
  display: flex;
  flex-direction: column;
  z-index: 100;
  border-right: 1px solid rgba(255,255,255,0.06);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 16px 14px;
  flex-shrink: 0;
}
.brand-icon {
  width: 34px; height: 34px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.brand-text { display: flex; flex-direction: column; gap: 2px; }
.brand-name { font-size: 15px; font-weight: 700; color: #f1f5f9; letter-spacing: -0.3px; line-height: 1; }
.brand-sub {
  font-size: 9.5px; font-weight: 700; color: #f59e0b;
  letter-spacing: 1px; text-transform: uppercase; line-height: 1;
}

.user-pill {
  display: flex; align-items: center; gap: 9px;
  margin: 0 12px 12px;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.07);
  border-radius: 8px; padding: 8px 10px; flex-shrink: 0;
}
.user-avatar {
  width: 28px; height: 28px; border-radius: 7px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 700; color: #fff; flex-shrink: 0;
}
.user-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.user-name {
  font-size: 12px; font-weight: 600; color: #e2e8f0;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.user-role { font-size: 9px; font-weight: 700; color: #f59e0b; letter-spacing: 0.8px; }

.sidebar-divider {
  height: 1px; background: rgba(255,255,255,0.06);
  margin: 0 12px 8px; flex-shrink: 0;
}

.sidebar-nav {
  flex: 1; overflow-y: auto; overflow-x: hidden;
  padding: 0 8px; scrollbar-width: none;
}
.sidebar-nav::-webkit-scrollbar { display: none; }

.nav-section-label {
  font-size: 9.5px; font-weight: 700;
  color: rgba(255,255,255,0.2);
  letter-spacing: 1.2px;
  padding: 10px 10px 5px;
}

.nav-item {
  display: flex; align-items: center; gap: 9px;
  padding: 9px 10px; border-radius: 7px; margin-bottom: 2px;
  color: #64748b; font-size: 13px; font-weight: 500;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
  border: 1px solid transparent;
}
.nav-item:hover { background: rgba(255,255,255,0.05); color: #94a3b8; }
.nav-item.router-link-active {
  background: rgba(245,158,11,0.14);
  border-color: rgba(245,158,11,0.22);
  color: #fbbf24;
}
.nav-item.router-link-active .nav-icon { color: #f59e0b; }
.nav-icon {
  width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; color: #475569; transition: color 0.15s;
}

.sidebar-footer {
  padding: 10px 12px 14px;
  border-top: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
}
.logout-btn {
  width: 100%;
  display: flex; align-items: center; justify-content: center; gap: 7px;
  padding: 9px 14px;
  background: rgba(239,68,68,0.08);
  border: 1px solid rgba(239,68,68,0.18);
  border-radius: 8px; color: #f87171;
  font-size: 12.5px; font-weight: 600; cursor: pointer;
  transition: background 0.15s, border-color 0.15s; font-family: inherit;
}
.logout-btn:hover { background: rgba(239,68,68,0.14); border-color: rgba(239,68,68,0.35); color: #fca5a5; }

/* ── Main content ── */
.sales-main {
  position: fixed;
  top: 0;
  left: 220px;
  right: 0;
  bottom: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #f4f6f8;
}
</style>