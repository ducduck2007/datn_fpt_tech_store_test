<template>
  <div class="layout">
    <!-- SIDEBAR -->
    <aside class="aside">
      <div class="brand" role="button" @click="go('/system/dashboard')">
        <div class="logo">Tech Store</div>
        <div class="brand-sub">System Portal</div>
      </div>

      <div class="user-pill">
        <div class="user-pill__left">
          <div class="user-pill__k">Current role</div>
          <div class="user-pill__v">{{ role || "N/A" }}</div>
        </div>
        <span class="badge text-bg-success ts-pill text-uppercase">{{
          role || "n/a"
        }}</span>
      </div>

      <nav class="menu">
        <div class="menu-sec">Navigation</div>

        <button
          class="menu-item"
          :class="{ active: activeMenu === '/system/dashboard' }"
          @click="go('/system/dashboard')"
        >
          <i class="bi bi-speedometer2 me-2"></i> Dashboard
        </button>

        <div class="menu-sec">Management</div>

        <button
          class="menu-item"
          :class="{ active: activeMenu === '/system/customers' }"
          @click="go('/system/customers')"
        >
          <i class="bi bi-people me-2"></i> Customers
        </button>

        <div class="menu-sec">Account</div>

        <button class="menu-item danger" @click="logout">
          <i class="bi bi-box-arrow-right me-2"></i> Logout
        </button>
      </nav>

      <div class="aside-foot ts-muted">
        <div>Console-first demo</div>
        <div class="small">DevTools → Console for payload details</div>
      </div>
    </aside>

    <!-- MAIN -->
    <div class="main">
      <header class="topbar">
        <div class="topbar__left">
          <div class="kicker">System</div>
          <div class="title">{{ pageTitle }}</div>
          <div class="sub ts-muted">Quản trị hệ thống</div>
        </div>

        <div class="topbar__right">
          <button class="btn btn-outline-primary ts-btn" @click="reload">
            <i class="bi bi-arrow-repeat me-2"></i> Reload (log console)
          </button>
        </div>
      </header>

      <main class="content">
        <div :class="['grid', { 'grid--single': !showTokenPanel }]">
          <!-- PAGE CONTENT -->
          <section class="left">
            <router-view />
          </section>

          <!-- RIGHT PANEL: Session/Token (conditional) -->
          <aside v-if="showTokenPanel" class="right">
            <div class="card ts-card sticky">
              <div class="card-header bg-transparent border-0 pt-4 px-4">
                <div class="hdr">
                  <div class="h">Session / Token</div>
                  <span class="badge text-bg-success ts-pill text-uppercase">{{
                    role || "n/a"
                  }}</span>
                </div>
              </div>

              <div class="card-body px-4 pb-4">
                <div class="stats mb-3">
                  <div class="stat">
                    <div class="stat__k ts-muted">Token</div>
                    <div class="stat__v">
                      <span v-if="token" class="badge text-bg-success ts-pill"
                        >Available</span
                      >
                      <span v-else class="badge text-bg-danger ts-pill"
                        >Missing</span
                      >
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
                  <textarea
                    class="form-control ts-input"
                    :value="token"
                    rows="10"
                    readonly
                  ></textarea>
                  <div class="ts-muted small mt-1">
                    (Readonly) — phục vụ debug nhanh.
                  </div>
                </div>

                <div class="divider"></div>

                <div class="d-grid gap-2">
                  <button
                    class="btn btn-outline-primary ts-btn w-100"
                    @click="reload"
                  >
                    Log to console
                  </button>
                  <button
                    class="btn btn-outline-danger ts-btn w-100"
                    @click="logout"
                  >
                    Logout
                  </button>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { confirmModal } from "../../ui/confirm";
import { toast } from "../../ui/toast";
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

async function logout() {
  const ok = await confirmModal(
    "Bạn muốn đăng xuất?",
    "Xác nhận",
    "Đăng xuất",
    true
  );
  if (!ok) return;

  clearSession?.();
  toast("Đã đăng xuất.", "success");
  router.push("/system/login");
}

function go(path) {
  if (path && path !== route.path) router.push(path);
}

onMounted(reload);
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  background: #f6f7fb;
}

.aside {
  background: #0b1220;
  color: rgba(255, 255, 255, 0.92);
  padding: 14px 12px;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: auto;
}

.main {
  min-width: 0;
}

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

.content {
  padding: 18px;
}

.brand {
  padding: 10px 10px 8px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  margin-bottom: 12px;
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

.menu {
  display: grid;
  gap: 6px;
}

.menu-item {
  text-align: left;
  border: 0;
  width: 100%;
  padding: 10px 12px;
  border-radius: 12px;
  background: transparent;
  color: rgba(255, 255, 255, 0.9);
}
.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}
.menu-item.active {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}
.menu-item.danger:hover {
  background: rgba(239, 68, 68, 0.18);
}

.menu-sec {
  margin: 12px 10px 2px;
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

/* Grid: content + token panel */
.grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 16px;
  align-items: start;
}
.grid--single {
  grid-template-columns: 1fr;
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
.sticky {
  position: sticky;
  top: 14px;
}

/* Responsive */
@media (max-width: 1100px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .aside {
    position: static;
    height: auto;
  }
  .grid {
    grid-template-columns: 1fr;
  }
  .sticky {
    position: static;
  }
}
</style>
