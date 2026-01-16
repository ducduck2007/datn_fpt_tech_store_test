<!-- src/pages/system/SystemShell.vue -->
<template>
  <el-container class="layout">
    <!-- ===== SIDEBAR ===== -->
    <el-aside class="aside" width="260px">
      <div class="brand" @click="go('/system/dashboard')">
        <div class="logo">Tech Store</div>
        <div class="brand-sub">System Portal</div>
      </div>

      <div class="user-pill">
        <div class="user-pill__left">
          <div class="user-pill__k">Current role</div>
          <div class="user-pill__v">{{ role || "N/A" }}</div>
        </div>
        <el-tag type="success" effect="dark" class="role-tag">{{
          role || "n/a"
        }}</el-tag>
      </div>

      <el-menu
        class="menu"
        :default-active="activeMenu"
        :collapse-transition="false"
        @select="onSelect"
      >
        <el-menu-item index="/system/dashboard">Dashboard</el-menu-item>

        <div class="menu-sec">Management</div>
        <el-menu-item index="/system/customers">Customers</el-menu-item>

        <div class="menu-sec">Account</div>
        <el-menu-item index="__logout">Logout</el-menu-item>
      </el-menu>

      <div class="aside-foot ts-muted">
        <div>Console-first demo</div>
        <div class="small">DevTools → Console for payload details</div>
      </div>
    </el-aside>

    <!-- ===== MAIN ===== -->
    <el-container class="main">
      <el-header class="topbar" height="72px">
        <div class="topbar__left">
          <div class="kicker">System</div>
          <div class="title">{{ pageTitle }}</div>
          <div class="sub ts-muted">Quản trị hệ thống</div>
        </div>

        <div class="topbar__right">
          <el-button type="primary" plain @click="reload">
            Reload (log console)
          </el-button>
        </div>
      </el-header>

      <el-main class="content">
        <div :class="['grid', { 'grid--single': !showTokenPanel }]">
          <!-- PAGE CONTENT -->
          <section class="left">
            <router-view />
          </section>

          <!-- ✅ RIGHT PANEL: Session/Token (conditional) -->
          <aside v-if="showTokenPanel" class="right">
            <el-card class="card sticky" shadow="hover">
              <template #header>
                <div class="hdr">
                  <div class="h">Session / Token</div>
                  <el-tag type="success" effect="dark">{{
                    role || "n/a"
                  }}</el-tag>
                </div>
              </template>

              <div class="stats">
                <div class="stat">
                  <div class="stat__k ts-muted">Token</div>
                  <div class="stat__v">
                    <el-tag v-if="token" type="success" effect="plain"
                      >Available</el-tag
                    >
                    <el-tag v-else type="danger" effect="plain">Missing</el-tag>
                  </div>
                </div>

                <div class="stat">
                  <div class="stat__k ts-muted">Length</div>
                  <div class="stat__v">
                    <b>{{ token ? token.length : 0 }}</b>
                  </div>
                </div>
              </div>

              <div class="block">
                <div class="label">Access token</div>
                <el-input
                  :model-value="token"
                  readonly
                  type="textarea"
                  :rows="10"
                />
                <div class="ts-muted small">
                  (Readonly) — phục vụ debug nhanh.
                </div>
              </div>

              <div class="divider"></div>

              <div class="actions">
                <el-button type="primary" plain class="w100" @click="reload">
                  Log to console
                </el-button>
                <el-button type="danger" plain class="w100" @click="logout">
                  Logout
                </el-button>
              </div>
            </el-card>
          </aside>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  getLastAuthResponse,
  getToken,
  getRole,
  decodeJwtPayload,
  clearSession,
} from "../../stores/auth";

const route = useRoute();
const router = useRouter();

const token = computed(() => getToken());
const role = computed(() => (getRole() || "").toUpperCase());

const activeMenu = computed(() => route.path || "/system/dashboard");
const pageTitle = computed(() => route.meta?.title || "Dashboard");

/**
 * Route meta:
 *  - showTokenPanel: false => hide the right Session/Token panel
 * Default is true.
 */
const showTokenPanel = computed(() => route.meta?.showTokenPanel !== false);

function reload() {
  console.log("[SystemShell] token?", !!getToken());
  console.log("[SystemShell] role=", (getRole() || "").toUpperCase());
  console.log("[SystemShell] lastAuthResponse=", getLastAuthResponse());
  console.log(
    "[SystemShell] jwtPayload(decoded)=",
    decodeJwtPayload(getToken())
  );
}

function logout() {
  clearSession?.();
  router.push("/system/login");
}

function go(path) {
  if (path && path !== route.path) router.push(path);
}

function onSelect(key) {
  if (key === "__logout") return logout();
  return go(key);
}

onMounted(reload);
</script>

<style scoped>
/* ===== Layout ===== */
.layout {
  min-height: 100vh;
  background: #f6f7fb;
}

.aside {
  background: #0b1220;
  color: rgba(255, 255, 255, 0.92);
  padding: 14px 12px;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.main {
  min-width: 0;
}

/* ===== Topbar ===== */
.topbar {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 18px;
}

.topbar__left {
  display: grid;
  gap: 2px;
}

.topbar__right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.content {
  padding: 18px;
}

/* ===== Sidebar ===== */
.brand {
  padding: 10px 10px 8px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  margin-bottom: 12px;
  cursor: pointer;
}
.logo {
  font-weight: 950;
  letter-spacing: -0.3px;
  font-size: 18px;
}
.brand-sub {
  font-size: 12px;
  opacity: 0.75;
  margin-top: 2px;
}

.user-pill {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  margin-bottom: 10px;
}
.user-pill__k {
  font-size: 11px;
  opacity: 0.75;
}
.user-pill__v {
  font-size: 13px;
  font-weight: 800;
  margin-top: 2px;
}
.role-tag {
  border-radius: 999px;
}

.menu {
  border-right: none;
  background: transparent;
}
.menu :deep(.el-menu-item) {
  border-radius: 12px;
  margin: 4px 6px;
  color: rgba(255, 255, 255, 0.9);
}
.menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}
.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu-sec {
  margin: 12px 10px 6px;
  font-size: 11px;
  opacity: 0.7;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.aside-foot {
  margin-top: 14px;
  padding: 10px 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
}

.small {
  font-size: 12px;
}

/* ===== Headings ===== */
.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
}
.title {
  font-weight: 950;
  font-size: 22px;
  letter-spacing: -0.3px;
}
.sub {
  font-size: 13px;
}

/* ===== Grid: content + token panel ===== */
.grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 16px;
  align-items: start;
}

/* ✅ when token panel hidden (Dashboard & Customers per meta) */
.grid--single {
  grid-template-columns: 1fr;
}

.card {
  border-radius: 18px;
}

.hdr {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.h {
  font-weight: 900;
  font-size: 16px;
}

/* Token panel */
.stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 12px;
}

.stat {
  padding: 10px 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.6);
}
.stat__k {
  font-size: 12px;
  margin-bottom: 4px;
}
.stat__v {
  font-size: 14px;
}

.block {
  display: grid;
  gap: 8px;
}

.label {
  font-weight: 800;
}

.divider {
  height: 1px;
  background: rgba(15, 23, 42, 0.08);
  margin: 14px 0;
}

.actions {
  display: grid;
  gap: 10px;
}

.w100 {
  width: 100%;
}

.sticky {
  position: sticky;
  top: 14px;
}

/* Responsive */
@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
  .sticky {
    position: static;
  }
}
</style>
