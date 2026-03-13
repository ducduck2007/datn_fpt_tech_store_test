<template>
  <el-container class="app-shell">
    <!-- ─── Header ─────────────────────────────────────── -->
    <el-header v-if="showHeader" class="app-header">
      <div class="header-inner">
        <!-- Left: Brand + Search -->
        <div class="header-left">
          <div class="brand" role="button" @click="goHome" title="Tech Store">
            <div class="brand-dot"></div>
            <div class="brand-text">
              <span class="brand-name">Tech Store</span>
              <span class="brand-sub">Laptop · Gaming · Workstation</span>
            </div>
          </div>

          <div v-if="showSearch" class="search-wrap">
            <svg
              class="search-icon"
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
            >
              <circle
                cx="11"
                cy="11"
                r="8"
                stroke="currentColor"
                stroke-width="1.5"
              />
              <path
                d="M21 21l-4.35-4.35"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
              />
            </svg>
            <input
              v-model="q"
              class="search-input"
              placeholder="Search laptop, CPU, RAM…"
              @input="emitSearch"
            />
            <kbd v-if="!q" class="search-kbd">⌘K</kbd>
            <button
              v-else
              class="search-clear"
              @click="
                q = '';
                emitSearch();
              "
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <line
                  x1="18"
                  y1="6"
                  x2="6"
                  y2="18"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
                <line
                  x1="6"
                  y1="6"
                  x2="18"
                  y2="18"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </button>
          </div>
        </div>

        <!-- Right: Controls -->
        <div class="header-right">
          <!-- Customer action buttons -->
          <template v-if="isAuthed && isCustomer">
            <button
              class="hdr-btn hdr-btn--event"
              @click="$router.push('/spin-wheel')"
            >
              <span>🎡</span>
              Sự kiện
              <span class="event-ping"></span>
            </button>

            <button
              class="hdr-btn hdr-btn--notif"
              @click="viewAllNotifications"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path
                  d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                />
                <path
                  d="M13.73 21a2 2 0 0 1-3.46 0"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                />
              </svg>
              Thông báo
              <span v-if="unreadNotificationCount > 0" class="notif-badge">{{
                unreadNotificationCount
              }}</span>
            </button>

            <button
              class="hdr-btn hdr-btn--cart"
              @click="$router.push('/cart')"
            >
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none">
                <path
                  d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <line
                  x1="3"
                  y1="6"
                  x2="21"
                  y2="6"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                />
                <path
                  d="M16 10a4 4 0 0 1-8 0"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              Giỏ hàng
              <span v-if="cartStore.count > 0" class="notif-badge">{{
                cartStore.count
              }}</span>
            </button>
          </template>

          <!-- Guest -->
          <template v-if="!isAuthed">
            <button class="btn-login" @click="goLogin">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <path
                  d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                />
                <polyline
                  points="10 17 15 12 10 7"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <line
                  x1="15"
                  y1="12"
                  x2="3"
                  y2="12"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                />
              </svg>
              Đăng nhập
            </button>
          </template>

          <!-- Authed user box -->
          <template v-else>
            <el-dropdown trigger="click" placement="bottom-end">
              <div class="userbox">
                <div class="avatar">{{ avatarLetter }}</div>
                <span class="username">{{ displayName }}</span>
                <svg
                  width="12"
                  height="12"
                  viewBox="0 0 24 24"
                  fill="none"
                  class="chevron"
                >
                  <polyline
                    points="6 9 12 15 18 9"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>

              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <div class="dropdown-user-info">
                    <div class="dropdown-avatar">{{ avatarLetter }}</div>
                    <div>
                      <div class="dropdown-name">{{ displayName }}</div>
                      <div class="dropdown-role">{{ portalLabel }}</div>
                    </div>
                  </div>
                  <div class="dropdown-divider"></div>

                  <el-dropdown-item
                    v-if="isSystemUser"
                    @click="router.push('/system/dashboard')"
                  >
                    <!-- <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <rect
                        x="3"
                        y="3"
                        width="7"
                        height="7"
                        rx="1"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                      <rect
                        x="14"
                        y="3"
                        width="7"
                        height="7"
                        rx="1"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                      <rect
                        x="3"
                        y="14"
                        width="7"
                        height="7"
                        rx="1"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                      <rect
                        x="14"
                        y="14"
                        width="7"
                        height="7"
                        rx="1"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                    </svg> -->
                    System Dashboard
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isCustomer"
                    @click="router.push('/profile')"
                  >
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <path
                        d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                      />
                      <circle
                        cx="12"
                        cy="7"
                        r="4"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                    </svg>
                    Thông tin cá nhân
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isCustomer"
                    @click="router.push('/my-orders')"
                  >
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <path
                        d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                      <line
                        x1="3"
                        y1="6"
                        x2="21"
                        y2="6"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                    </svg>
                    Đơn hàng
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isCustomer"
                    @click="router.push('/change-password')"
                  >
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <rect
                        x="3"
                        y="11"
                        width="18"
                        height="11"
                        rx="2"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                      <path
                        d="M7 11V7a5 5 0 0 1 10 0v4"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                      />
                    </svg>
                    Đổi mật khẩu
                  </el-dropdown-item>
                  <div class="dropdown-divider"></div>
                  <el-dropdown-item @click="logout" class="dropdown-logout">
                    <!-- <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <path
                        d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                      />
                      <polyline
                        points="16 17 21 12 16 7"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                      <line
                        x1="21"
                        y1="12"
                        x2="9"
                        y2="12"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                      />
                    </svg> -->
                    Đăng xuất
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </el-header>

    <!-- ─── Main ───────────────────────────────────────── -->
    <el-main class="app-main">
      <router-view />
    </el-main>

    <!-- ─── Notification Modal ─────────────────────────── -->
    <Teleport to="body">
      <transition name="modal-fade">
        <div
          v-if="notificationsDialog"
          class="modal-overlay"
          @click.self="notificationsDialog = false"
        >
          <div class="notif-modal">
            <div class="notif-modal__head">
              <div class="notif-modal__title">🔔 Thông báo</div>
              <button
                class="notif-modal__close"
                @click="notificationsDialog = false"
              >
                ✕
              </button>
            </div>

            <div class="notif-modal__body">
              <div v-if="allNotifications.length === 0" class="notif-empty">
                <div class="notif-empty__icon">🔕</div>
                <div>Không có thông báo nào</div>
              </div>
              <div v-else class="notif-list">
                <div
                  v-for="notif in allNotifications"
                  :key="notif.id"
                  class="notif-item"
                  :class="{ 'notif-item--unread': !notif.isRead }"
                >
                  <div class="notif-item__icon">
                    {{ notifIcon(notif.type) }}
                  </div>
                  <div class="notif-item__body">
                    <div class="notif-item__title">{{ notif.title }}</div>
                    <div
                      class="notif-item__msg"
                      v-html="formatMessage(notif.message)"
                    ></div>
                    <div class="notif-item__time">
                      {{ formatDate(notif.createdAt) }}
                    </div>
                  </div>
                  <button
                    v-if="!notif.isRead"
                    class="notif-item__read-btn"
                    @click="markNotifAsRead(notif.id)"
                  >
                    ✓
                  </button>
                </div>
              </div>
            </div>

            <div class="notif-modal__foot">
              <button class="btn-ghost" @click="notificationsDialog = false">
                Đóng
              </button>
              <button class="btn-primary" @click="markAllAsRead">
                Đánh dấu tất cả
              </button>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "./ui/toast";
import {
  clearLastAuthResponse,
  clearSession,
  useAuthStore,
} from "./stores/auth";
import { useCartStore } from "./stores/cart";
import http from "./api/http";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const cartStore = useCartStore();

const q = ref("");
const unreadNotificationCount = ref(0);
const notificationsDialog = ref(false);
const allNotifications = ref([]);

const isAuthed = computed(() => auth.isAuthed);
const isCustomer = computed(() => auth.isCustomer);
const isSystemUser = computed(() => isAuthed.value && !isCustomer.value);
const displayName = computed(() => auth.displayName);
const portalLabel = computed(() => (isCustomer.value ? "Customer" : "System"));

const avatarLetter = computed(() => {
  const s = String(displayName.value || "U").trim();
  return (s[0] || "U").toUpperCase();
});

const showHeader = computed(() => route.meta?.hideHeader !== true);
const showSearch = computed(
  () =>
    route.name === "home" && (route.meta?.portal || "customer") === "customer",
);

function emitSearch() {
  window.dispatchEvent(new CustomEvent("products:search", { detail: q.value }));
}

function goHome() {
  if (!isAuthed.value) return router.push("/");
  return isCustomer.value ? router.push("/") : router.push("/system/dashboard");
}

function goLogin() {
  router.push("/login");
}

async function logout() {
  clearSession();
  clearLastAuthResponse();
  toast("Logged out.", "success");
  router.push(isSystemUser.value ? "/system/login" : "/login");
}

// ── Notifications ─────────────────────────────────────────
async function loadNotificationCount() {
  if (!isCustomer.value) return;
  try {
    const res = await http.get("/api/auth/notifications/my/unread-count");
    unreadNotificationCount.value = res.data?.unreadCount || 0;
  } catch {}
}

async function loadAllNotifications() {
  try {
    const res = await http.get("/api/auth/notifications/my");
    allNotifications.value = res.data || [];
  } catch {}
}

function viewAllNotifications() {
  notificationsDialog.value = true;
  loadAllNotifications();
}

async function markNotifAsRead(id) {
  try {
    await http.put(`/api/auth/notifications/${id}/read`);
    await Promise.all([loadAllNotifications(), loadNotificationCount()]);
  } catch {
    toast("Không thể đánh dấu đã đọc", "error");
  }
}

async function markAllAsRead() {
  try {
    await http.put("/api/auth/notifications/read-all");
    await Promise.all([loadAllNotifications(), loadNotificationCount()]);
    toast("Đã đánh dấu tất cả đã đọc", "success");
  } catch {
    toast("Không thể đánh dấu tất cả", "error");
  }
}

function notifIcon(type) {
  return (
    {
      WELCOME: "🎁",
      BIRTHDAY: "🎂",
      PURCHASE_REMINDER: "🛒",
      WINBACK: "💝",
      VIP_TIER_UPGRADE: "⭐",
      SPIN_EXPIRY_WARNING: "🎡",
    }[type] ?? "🔔"
  );
}

function formatMessage(msg) {
  return msg ? msg.replace(/\n/g, "<br>") : "";
}
function formatDate(d) {
  return new Date(d).toLocaleDateString("vi-VN");
}

// ── Auth & lifecycle ──────────────────────────────────────
function onAutoLogout(e) {
  clearSession();
  clearLastAuthResponse();
  toast(
    `Session expired (${e?.detail || "401"}). Please login again.`,
    "warning",
  );
  router.push("/login");
}

let notifInterval = null;

watch(
  isCustomer,
  (val) => {
    if (val) {
      cartStore.refreshCount();
      loadNotificationCount();
      notifInterval = setInterval(loadNotificationCount, 30000);
    } else {
      if (notifInterval) {
        clearInterval(notifInterval);
        notifInterval = null;
      }
      unreadNotificationCount.value = 0;
    }
  },
  { immediate: true },
);

onMounted(() => {
  window.addEventListener("auth:logout", onAutoLogout);
  // CustomerHome dispatches after banner mark-as-read
  window.addEventListener("notifications:updated", loadNotificationCount);
  // CustomerHome banner buttons trigger open
  window.addEventListener("header:open-notifications", viewAllNotifications);
});

onBeforeUnmount(() => {
  window.removeEventListener("auth:logout", onAutoLogout);
  window.removeEventListener("notifications:updated", loadNotificationCount);
  window.removeEventListener("header:open-notifications", viewAllNotifications);
  if (notifInterval) clearInterval(notifInterval);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;1,9..40,300&display=swap");

.app-shell {
  min-height: 100vh;
  background: #f0f2fa;
  font-family: "DM Sans", sans-serif;
}

/* ─── Header ────────────────────────────────────────────── */
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px !important;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.07);
  box-shadow: 0 1px 12px rgba(0, 0, 0, 0.04);
}

.header-inner {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

/* ─── Brand ─────────────────────────────────────────────── */
.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
  transition: opacity 0.15s;
}
.brand:hover {
  opacity: 0.78;
}

.brand-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #4f6ef7;
  box-shadow: 0 0 12px rgba(79, 110, 247, 0.5);
  flex-shrink: 0;
  animation: dotPulse 3s ease-in-out infinite;
}

@keyframes dotPulse {
  0%,
  100% {
    box-shadow: 0 0 12px rgba(79, 110, 247, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(79, 110, 247, 0.8);
  }
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
  gap: 1px;
}
.brand-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a1d2e;
  letter-spacing: -0.3px;
}
.brand-sub {
  font-size: 10.5px;
  color: rgba(20, 25, 60, 0.4);
  font-weight: 400;
}

/* ─── Search ────────────────────────────────────────────── */
.search-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 10px;
  padding: 0 12px;
  height: 38px;
  width: 300px;
  transition:
    border-color 0.2s,
    background 0.2s,
    box-shadow 0.2s;
}
.search-wrap:focus-within {
  border-color: rgba(79, 110, 247, 0.45);
  background: rgba(79, 110, 247, 0.04);
  box-shadow: 0 0 0 3px rgba(79, 110, 247, 0.08);
}
.search-icon {
  color: rgba(20, 25, 60, 0.3);
  flex-shrink: 0;
}
.search-wrap:focus-within .search-icon {
  color: rgba(79, 110, 247, 0.7);
}
.search-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-family: "DM Sans", sans-serif;
  font-size: 13.5px;
  color: #1a1d2e;
  min-width: 0;
}
.search-input::placeholder {
  color: rgba(20, 25, 60, 0.3);
}
.search-kbd {
  font-size: 10px;
  color: rgba(20, 25, 60, 0.3);
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 5px;
  padding: 1px 5px;
  flex-shrink: 0;
  pointer-events: none;
}
.search-clear {
  background: none;
  border: none;
  padding: 2px;
  cursor: pointer;
  color: rgba(20, 25, 60, 0.35);
  display: flex;
  align-items: center;
  transition: color 0.15s;
}
.search-clear:hover {
  color: rgba(20, 25, 60, 0.7);
}

/* ─── Right side ────────────────────────────────────────── */
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* ─── Header action buttons ─────────────────────────────── */
.hdr-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 13px;
  border-radius: 50px;
  border: 1.5px solid #e5e7eb;
  background: #ffffff;
  font-family: "DM Sans", sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  cursor: pointer;
  white-space: nowrap;
  transition:
    background 0.15s,
    border-color 0.15s,
    box-shadow 0.15s,
    transform 0.1s;
}
.hdr-btn:hover {
  background: #f8fafc;
  border-color: #4f6ef7;
  color: #4f6ef7;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(79, 110, 247, 0.12);
}
.hdr-btn--cart {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);
}
.hdr-btn--cart:hover {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.event-ping {
  position: absolute;
  top: -3px;
  right: -3px;
  width: 8px;
  height: 8px;
  background: #ef4444;
  border-radius: 50%;
  animation: ping-anim 1.5s ease-in-out infinite;
}
@keyframes ping-anim {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.8);
    opacity: 0;
  }
}

.notif-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 100px;
  line-height: 1;
}

/* ─── Login button ──────────────────────────────────────── */
.btn-login {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 9px;
  background: linear-gradient(135deg, #4f6ef7 0%, #6c4fee 100%);
  color: #fff;
  font-family: "DM Sans", sans-serif;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition:
    transform 0.15s,
    box-shadow 0.2s;
  box-shadow: 0 3px 14px rgba(79, 110, 247, 0.28);
}
.btn-login:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(79, 110, 247, 0.38);
}
.btn-login:active {
  transform: translateY(0);
}

/* ─── User box ──────────────────────────────────────────── */
.userbox {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px 5px 5px;
  border-radius: 100px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  user-select: none;
  transition:
    border-color 0.15s,
    background 0.15s,
    box-shadow 0.15s;
}
.userbox:hover {
  border-color: rgba(79, 110, 247, 0.3);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    rgba(79, 110, 247, 0.15),
    rgba(108, 79, 238, 0.15)
  );
  color: #4f6ef7;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.username {
  font-size: 13.5px;
  font-weight: 500;
  color: #1a1d2e;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chevron {
  color: rgba(20, 25, 60, 0.35);
  flex-shrink: 0;
}

/* ─── Dropdown ──────────────────────────────────────────── */
:deep(.user-dropdown) {
  padding: 6px;
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.07);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  min-width: 200px;
}
:deep(.user-dropdown .el-dropdown-menu__item) {
  border-radius: 8px;
  font-family: "DM Sans", sans-serif;
  font-size: 13.5px;
  color: rgba(20, 25, 60, 0.7);
  padding: 8px 10px;
  display: flex;
  align-items: center;
  gap: 9px;
  transition:
    background 0.15s,
    color 0.15s;
}
:deep(.user-dropdown .el-dropdown-menu__item:hover) {
  background: rgba(79, 110, 247, 0.06);
  color: #1a1d2e;
}
:deep(.user-dropdown .dropdown-logout) {
  color: rgba(220, 38, 38, 0.75);
}
:deep(.user-dropdown .dropdown-logout:hover) {
  background: rgba(220, 38, 38, 0.06);
  color: #dc2626;
}

.dropdown-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 10px 8px;
}
.dropdown-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    rgba(79, 110, 247, 0.15),
    rgba(108, 79, 238, 0.15)
  );
  color: #4f6ef7;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.dropdown-name {
  font-size: 13.5px;
  font-weight: 600;
  color: #1a1d2e;
  line-height: 1.2;
}
.dropdown-role {
  font-size: 11px;
  color: rgba(20, 25, 60, 0.4);
  letter-spacing: 0.03em;
  text-transform: uppercase;
}
.dropdown-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
  margin: 4px 0;
}

/* ─── Notification Modal ────────────────────────────────── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(6px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.modal-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.2, 0.64, 1);
}
.modal-fade-leave-active {
  transition: all 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.notif-modal {
  background: #ffffff;
  border-radius: 24px;
  width: 100%;
  max-width: 560px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.18);
  overflow: hidden;
  animation: modal-in 0.3s cubic-bezier(0.34, 1.3, 0.64, 1);
}
@keyframes modal-in {
  from {
    transform: scale(0.95) translateY(10px);
    opacity: 0;
  }
  to {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}

.notif-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 24px 18px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}
.notif-modal__title {
  font-family: "DM Sans", sans-serif;
  font-size: 20px;
  font-weight: 700;
}
.notif-modal__close {
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.06);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 13px;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.notif-modal__close:hover {
  background: rgba(0, 0, 0, 0.12);
}

.notif-modal__body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
}

.notif-empty {
  padding: 40px;
  text-align: center;
  color: #64748b;
}
.notif-empty__icon {
  font-size: 40px;
  margin-bottom: 10px;
  opacity: 0.4;
}

.notif-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 12px;
  border-radius: 12px;
  transition: background 0.15s;
}
.notif-item:hover {
  background: #f8fafc;
}
.notif-item--unread {
  background: #eff6ff;
}
.notif-item--unread:hover {
  background: #dbeafe;
}
.notif-item__icon {
  font-size: 24px;
  flex-shrink: 0;
  margin-top: 1px;
}
.notif-item__title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 3px;
  color: #0f172a;
}
.notif-item__msg {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 4px;
}
.notif-item__time {
  font-size: 11.5px;
  color: rgba(100, 116, 139, 0.6);
}
.notif-item__read-btn {
  flex-shrink: 0;
  margin-left: auto;
  width: 28px;
  height: 28px;
  background: white;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 50%;
  font-size: 13px;
  color: #16a34a;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    background 0.15s,
    border-color 0.15s;
}
.notif-item__read-btn:hover {
  background: #16a34a;
  color: white;
  border-color: #16a34a;
}

.notif-modal__foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}
.btn-ghost {
  padding: 8px 18px;
  border-radius: 100px;
  border: none;
  background: rgba(0, 0, 0, 0.06);
  color: #0f172a;
  font-family: "DM Sans", sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-ghost:hover {
  background: rgba(0, 0, 0, 0.1);
}
.btn-primary {
  padding: 8px 18px;
  border-radius: 100px;
  border: none;
  background: linear-gradient(135deg, #16a34a, #15803d);
  color: #fff;
  font-family: "DM Sans", sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(22, 163, 74, 0.25);
  transition:
    transform 0.15s,
    box-shadow 0.15s;
}
.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(22, 163, 74, 0.32);
}

/* ─── Main ──────────────────────────────────────────────── */
.app-main {
  padding: 24px 20px;
}
</style>
