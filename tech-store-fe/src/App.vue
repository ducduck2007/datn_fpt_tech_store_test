<template>
  <Analytics />
  <el-container class="app-shell">
    <!-- ─── Header ─────────────────────────────────────── -->
    <el-header v-if="showHeader" class="app-header">
      <div class="header-inner">
        <!-- Left: Brand + Search -->
        <div class="header-left">
          <div class="brand" role="button" @click="goHome" title="Tech Store">
            <div class="brand-mark">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.8"/>
                <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="brand-text">
              <span class="brand-name">Tech Store</span>
              <span class="brand-sub">Laptop · Gaming · Workstation</span>
            </div>
          </div>

          <div v-if="showSearch" class="search-wrap">
            <svg class="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="1.6"/>
              <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            <input
              v-model="q"
              class="search-input"
              placeholder="Tìm laptop, CPU, RAM…"
              @input="emitSearch"
            />
            <kbd v-if="!q" class="search-kbd">⌘K</kbd>
            <button v-else class="search-clear" @click="q = ''; emitSearch();">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
                <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
                <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Right: Controls -->
        <div class="header-right">
          <!-- Customer action buttons -->
          <template v-if="isAuthed && isCustomer">
            <div class="action-group">

              <!-- Sự kiện -->
              <button class="action-btn" @click="$router.push('/spin-wheel')" title="Sự kiện">
                <span class="action-btn__ping"></span>
                <span class="action-btn__icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.6"/>
                    <path d="M12 3v2M12 19v2M3 12h2M19 12h2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6"/>
                  </svg>
                </span>
                <span class="action-btn__label">Sự kiện</span>
              </button>

              <!-- Thông báo -->
              <button class="action-btn" @click="viewAllNotifications" title="Thông báo">
                <span class="action-btn__icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                  </svg>
                </span>
                <span class="action-btn__label">Thông báo</span>
                <span v-if="unreadNotificationCount > 0" class="action-btn__badge">
                  {{ unreadNotificationCount > 9 ? '9+' : unreadNotificationCount }}
                </span>
              </button>

              <!-- Giỏ hàng -->
              <button class="action-btn action-btn--cart" @click="$router.push('/cart')" title="Giỏ hàng">
                <span class="action-btn__icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                    <line x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                    <path d="M16 10a4 4 0 0 1-8 0" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span class="action-btn__label">Giỏ hàng</span>
                <span v-if="cartStore.count > 0" class="action-btn__badge action-btn__badge--cart">
                  {{ cartStore.count > 9 ? '9+' : cartStore.count }}
                </span>
              </button>

            </div>
          </template>

          <!-- Guest -->
          <template v-if="!isAuthed">
            <button class="btn-login" @click="goLogin">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                <polyline points="10 17 15 12 10 7" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="15" y1="12" x2="3" y2="12" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
              </svg>
              Đăng nhập
            </button>
          </template>

          <!-- Authed user box -->
          <template v-else>
            <el-dropdown trigger="click" placement="bottom-end">
              <div class="userbox">
                <div class="userbox__avatar">{{ avatarLetter }}</div>
                <div class="userbox__info">
                  <span class="userbox__name">{{ displayName }}</span>
                  <span class="userbox__role">{{ portalLabel }}</span>
                </div>
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" class="userbox__chevron">
                  <polyline points="6 9 12 15 18 9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>

              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <div class="dropdown-header">
                    <div class="dropdown-avatar">{{ avatarLetter }}</div>
                    <div class="dropdown-meta">
                      <div class="dropdown-name">{{ displayName }}</div>
                      <div class="dropdown-role-tag">{{ portalLabel }}</div>
                    </div>
                  </div>
                  <div class="dropdown-sep"></div>

                  <el-dropdown-item v-if="isSystemUser" @click="router.push('/system/dashboard')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                      <rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                      <rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                      <rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    System Dashboard
                  </el-dropdown-item>

                  <el-dropdown-item v-if="isCustomer" @click="router.push('/profile')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    Thông tin cá nhân
                  </el-dropdown-item>

                  <el-dropdown-item v-if="isCustomer" @click="router.push('/my-orders')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      <rect x="9" y="3" width="6" height="4" rx="1" stroke="currentColor" stroke-width="1.5"/>
                      <line x1="9" y1="12" x2="15" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      <line x1="9" y1="16" x2="13" y2="16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    Đơn hàng của tôi
                  </el-dropdown-item>

                  <el-dropdown-item v-if="isCustomer" @click="router.push('/change-password')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    Đổi mật khẩu
                  </el-dropdown-item>

                  <div class="dropdown-sep"></div>
                  <el-dropdown-item @click="logout" class="dropdown-logout">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      <polyline points="16 17 21 12 16 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                      <line x1="21" y1="12" x2="9" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
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

    <!-- ─── Footer ───────────────────────────────────────── -->
<footer v-if="showFooter" class="app-footer">
  <div class="footer-inner">

    <!-- Brand -->
    <div class="footer-brand">
      <div class="footer-brand__mark">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.8"/>
          <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
      </div>
      <span class="footer-brand__name">Tech Store</span>
    </div>

    <!-- Nav links -->
    <nav class="footer-nav">
      <a class="footer-nav__link" @click="$router.push('/')">Trang chủ</a>
      <a class="footer-nav__link" @click="$router.push('/spin-wheel')">Sự kiện</a>
      <a v-if="isAuthed && isCustomer" class="footer-nav__link" @click="$router.push('/my-orders')">Đơn hàng</a>
      <a v-if="isAuthed && isCustomer" class="footer-nav__link" @click="$router.push('/cart')">Giỏ hàng</a>
      <a v-if="isAuthed && isCustomer" class="footer-nav__link" @click="$router.push('/profile')">Tài khoản</a>
      <a v-if="!isAuthed" class="footer-nav__link" @click="$router.push('/login')">Đăng nhập</a>
    </nav>

    <!-- Copyright -->
    <div class="footer-copy">© {{ new Date().getFullYear() }} Tech Store. All rights reserved.</div>

  </div>
</footer>

    <!-- ─── Notification Modal ─────────────────────────── -->
    <Teleport to="body">
      <transition name="modal-fade">
        <div v-if="notificationsDialog" class="modal-overlay" @click.self="notificationsDialog = false">
          <div class="notif-modal">
            <div class="notif-modal__head">
              <div class="notif-modal__title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                </svg>
                Thông báo
              </div>
              <button class="notif-modal__close" @click="notificationsDialog = false">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
                  <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
                </svg>
              </button>
            </div>

            <div class="notif-modal__body">
              <div v-if="allNotifications.length === 0" class="notif-empty">
                <div class="notif-empty__icon">🔕</div>
                <div class="notif-empty__text">Chưa có thông báo nào</div>
              </div>
              <div v-else class="notif-list">
                <div
                  v-for="notif in allNotifications"
                  :key="notif.id"
                  class="notif-item"
                  :class="{ 'notif-item--unread': !notif.isRead }"
                >
                  <div class="notif-item__icon">{{ notifIcon(notif.type) }}</div>
                  <div class="notif-item__body">
                    <div class="notif-item__title">{{ notif.title }}</div>
                    <div class="notif-item__msg" v-html="formatMessage(notif.message)"></div>
                    <div class="notif-item__time">{{ formatDate(notif.createdAt) }}</div>
                  </div>
                  <button v-if="!notif.isRead" class="notif-item__read-btn" @click="markNotifAsRead(notif.id)" title="Đánh dấu đã đọc">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
                      <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>

            <div class="notif-modal__foot">
              <button class="btn-ghost" @click="notificationsDialog = false">Đóng</button>
              <button class="btn-primary" @click="markAllAsRead">Đánh dấu tất cả đã đọc</button>
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
import { clearLastAuthResponse, clearSession, useAuthStore } from "./stores/auth";
import { useCartStore } from "./stores/cart";
import http from "./api/http";
import { Analytics } from '@vercel/analytics/vue';

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
const portalLabel = computed(() => (isCustomer.value ? "Khách hàng" : "Hệ thống"));

const avatarLetter = computed(() => {
  const s = String(displayName.value || "U").trim();
  return (s[0] || "U").toUpperCase();
});

const showHeader = computed(() => route.meta?.hideHeader !== true);
const showSearch = computed(
  () => route.name === "home" && (route.meta?.portal || "customer") === "customer",
);

const showFooter = computed(
  () => route.meta?.hideHeader !== true && (route.meta?.portal || "customer") === "customer",
);

function emitSearch() {
  window.dispatchEvent(new CustomEvent("products:search", { detail: q.value }));
}
function goHome() {
  if (!isAuthed.value) return router.push("/");
  return isCustomer.value ? router.push("/") : router.push("/system/dashboard");
}
function goLogin() { router.push("/login"); }
async function logout() {
  clearSession(); clearLastAuthResponse();
  toast("Đã đăng xuất.", "success");
  router.push(isSystemUser.value ? "/system/login" : "/login");
}

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
  } catch { toast("Không thể đánh dấu đã đọc", "error"); }
}
async function markAllAsRead() {
  try {
    await http.put("/api/auth/notifications/read-all");
    await Promise.all([loadAllNotifications(), loadNotificationCount()]);
    toast("Đã đánh dấu tất cả đã đọc", "success");
  } catch { toast("Không thể cập nhật", "error"); }
}

function notifIcon(type) {
  return ({ WELCOME:"🎁", BIRTHDAY:"🎂", PURCHASE_REMINDER:"🛒", WINBACK:"💝", VIP_TIER_UPGRADE:"⭐", SPIN_EXPIRY_WARNING:"🎡" }[type] ?? "🔔");
}
function formatMessage(msg) { return msg ? msg.replace(/\n/g, "<br>") : ""; }
function formatDate(d) { return new Date(d).toLocaleDateString("vi-VN"); }

function onAutoLogout(e) {
  clearSession(); clearLastAuthResponse();
  toast(`Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.`, "warning");
  router.push("/login");
}

let notifInterval = null;
watch(isCustomer, (val) => {
  if (val) {
    cartStore.refreshCount();
    loadNotificationCount();
    notifInterval = setInterval(loadNotificationCount, 30000);
  } else {
    if (notifInterval) { clearInterval(notifInterval); notifInterval = null; }
    unreadNotificationCount.value = 0;
  }
}, { immediate: true });

onMounted(() => {
  window.addEventListener("auth:logout", onAutoLogout);
  window.addEventListener("notifications:updated", loadNotificationCount);
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
@import url("https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:ital,wght@0,300;0,400;0,500;0,600;0,700;1,400&display=swap");

/* ── Reset & Base ───────────────────────────────────────── */
*, *::before, *::after { box-sizing: border-box; }

.app-shell {
  min-height: 100vh;
  background: #f0f2fa;
  font-family: "Be Vietnam Pro", sans-serif;
}

/* ── Header Shell ───────────────────────────────────────── */
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px !important;
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 0 rgba(0,0,0,0.04), 0 4px 24px rgba(0,0,0,0.04);
}

.header-inner {
  width: 100%;
  max-width: 1360px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

/* ── Brand ──────────────────────────────────────────────── */
.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
  text-decoration: none;
  transition: opacity 0.18s;
}
.brand:hover { opacity: 0.72; }

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(29,78,216,0.3);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1;
}
.brand-name {
  font-size: 14.5px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.3px;
}
.brand-sub {
  font-size: 10px;
  color: #9ca3af;
  font-weight: 400;
  letter-spacing: 0.02em;
}

/* ── Search Bar ─────────────────────────────────────────── */
.search-wrap {
  display: flex;
  align-items: center;
  gap: 9px;
  height: 40px;
  width: 310px;
  padding: 0 14px;
  background: #f8fafc;
  border: 1.5px solid rgba(29, 78, 216, 0.15);
  border-radius: 100px;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s, width 0.3s cubic-bezier(.4,0,.2,1);
}
.search-wrap:focus-within {
  width: 360px;
  border-color: rgba(29, 78, 216, 0.5);
  background: #ffffff;
  box-shadow: 0 0 0 3.5px rgba(29, 78, 216, 0.08), 0 2px 12px rgba(29,78,216,0.08);
}
.search-icon {
  color: #9ca3af;
  flex-shrink: 0;
  transition: color 0.2s;
}
.search-wrap:focus-within .search-icon {
  color: #6b7280;
}
.search-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 13.5px;
  font-weight: 400;
  color: #111827;
  min-width: 0;
}
.search-input::placeholder {
  color: #b0b7c3;
}
.search-kbd {
  font-size: 10.5px;
  color: #9ca3af;
  background: rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 6px;
  padding: 2px 6px;
  flex-shrink: 0;
  pointer-events: none;
  font-family: "Be Vietnam Pro", sans-serif;
  letter-spacing: -0.02em;
}
.search-clear {
  background: rgba(0, 0, 0, 0.06);
  border: none;
  padding: 0;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.15s, color 0.15s;
}
.search-clear:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #374151;
}

/* ── Header Right ───────────────────────────────────────── */
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

/* ── Action Group (3 buttons container) ─────────────────── */
.action-group {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(29, 78, 216, 0.05);
  border: 1.5px solid rgba(29, 78, 216, 0.12);
  border-radius: 100px;
  padding: 4px;
}

/* ── Action Buttons ─────────────────────────────────────── */
.action-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 34px;
  padding: 0 13px;
  border-radius: 100px;
  border: none;
  background: transparent;
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s, color 0.15s, box-shadow 0.15s;
  outline: none;
}
.action-btn:hover {
  background: #ffffff;
  color: #111827;
  box-shadow: 0 1px 6px rgba(0,0,0,0.08);
}

.action-btn__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-btn__label {
  line-height: 1;
}

/* Cart button — filled blue */
.action-btn--cart {
  background: #1d4ed8;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(29, 78, 216, 0.28);
}
.action-btn--cart:hover {
  background: #1e40af;
  color: #ffffff;
  box-shadow: 0 4px 16px rgba(29, 78, 216, 0.38);
}

/* Badge */
.action-btn__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ef4444;
  color: #ffffff;
  font-size: 9.5px;
  font-weight: 700;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 100px;
  line-height: 1;
  letter-spacing: -0.02em;
}
.action-btn--cart .action-btn__badge {
  background: rgba(255,255,255,0.25);
  color: #ffffff;
}

/* Ping dot for event */
.action-btn__ping {
  position: absolute;
  top: 5px;
  right: 10px;
  width: 7px;
  height: 7px;
  background: #ef4444;
  border-radius: 50%;
  border: 1.5px solid #ffffff;
  animation: ping-dot 2s ease-in-out infinite;
}
@keyframes ping-dot {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.5; }
}

/* ── Login Button ───────────────────────────────────────── */
.btn-login {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 38px;
  padding: 0 18px;
  border: none;
  border-radius: 100px;
  background: #1d4ed8;
  color: #ffffff;
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s, box-shadow 0.18s;
  box-shadow: 0 2px 12px rgba(29, 78, 216, 0.3);
}
.btn-login:hover {
  background: #1e40af;
  transform: translateY(-1px);
  box-shadow: 0 5px 18px rgba(29, 78, 216, 0.4);
}
.btn-login:active { transform: translateY(0); }

/* ── Userbox ────────────────────────────────────────────── */
.userbox {
  display: flex;
  align-items: center;
  gap: 9px;
  height: 42px;
  padding: 4px 12px 4px 5px;
  border-radius: 100px;
  border: 1.5px solid rgba(29, 78, 216, 0.18);
  background: #ffffff;
  cursor: pointer;
  user-select: none;
  transition: border-color 0.18s, box-shadow 0.18s, background 0.15s;
  box-shadow: 0 1px 4px rgba(29,78,216,0.06);
}
.userbox:hover {
  border-color: rgba(29, 78, 216, 0.38);
  box-shadow: 0 3px 14px rgba(29,78,216,0.14);
  background: #fafcff;
}

.userbox__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%);
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  letter-spacing: -0.02em;
}

.userbox__info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1;
}
.userbox__name {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.01em;
}
.userbox__role {
  font-size: 10.5px;
  color: #9ca3af;
  font-weight: 400;
}

.userbox__chevron {
  color: #d1d5db;
  flex-shrink: 0;
  margin-left: 2px;
  transition: transform 0.2s;
}

/* ── Dropdown Menu ──────────────────────────────────────── */
:deep(.user-dropdown) {
  padding: 6px;
  border-radius: 16px;
  border: 1.5px solid rgba(29, 78, 216, 0.1);
  box-shadow: 0 16px 48px rgba(29, 78, 216, 0.1), 0 4px 16px rgba(0,0,0,0.06);
  min-width: 210px;
  font-family: "Be Vietnam Pro", sans-serif;
}
:deep(.user-dropdown .el-dropdown-menu__item) {
  border-radius: 9px;
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 13.5px;
  font-weight: 400;
  color: rgba(29, 78, 216, 0.75);
  padding: 9px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: background 0.13s, color 0.13s;
  line-height: 1.2;
}
:deep(.user-dropdown .el-dropdown-menu__item:hover) {
  background: rgba(29, 78, 216, 0.06);
  color: #1d4ed8;
}
:deep(.user-dropdown .dropdown-logout) {
  color: rgba(220, 38, 38, 0.72);
}
:deep(.user-dropdown .dropdown-logout:hover) {
  background: rgba(220, 38, 38, 0.06);
  color: #dc2626;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 10px 12px 12px;
}
.dropdown-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%);
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.dropdown-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.dropdown-name {
  font-size: 13.5px;
  font-weight: 600;
  color: #1d4ed8;
  letter-spacing: -0.01em;
  line-height: 1.2;
}
.dropdown-role-tag {
  display: inline-flex;
  align-items: center;
  font-size: 10.5px;
  font-weight: 500;
  color: rgba(29, 78, 216, 0.55);
  background: rgba(29, 78, 216, 0.07);
  padding: 2px 7px;
  border-radius: 100px;
  letter-spacing: 0.02em;
  width: fit-content;
}
.dropdown-sep {
  height: 1px;
  background: rgba(29, 78, 216, 0.08);
  margin: 4px 0;
}

/* ── Notification Modal ─────────────────────────────────── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(29, 78, 216, 0.15);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.modal-fade-enter-active { transition: all 0.28s cubic-bezier(0.34, 1.2, 0.64, 1); }
.modal-fade-leave-active { transition: all 0.18s ease; }
.modal-fade-enter-from,
.modal-fade-leave-to { opacity: 0; }

.notif-modal {
  background: #ffffff;
  border-radius: 24px;
  width: 100%;
  max-width: 540px;
  max-height: 78vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 32px 80px rgba(29, 78, 216, 0.14);
  overflow: hidden;
  animation: modal-in 0.28s cubic-bezier(0.34, 1.3, 0.64, 1);
}
@keyframes modal-in {
  from { transform: scale(0.96) translateY(12px); opacity: 0; }
  to   { transform: scale(1) translateY(0);      opacity: 1; }
}

.notif-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 18px;
  border-bottom: 1px solid rgba(29, 78, 216, 0.08);
}
.notif-modal__title {
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  gap: 9px;
  letter-spacing: -0.02em;
}
.notif-modal__close {
  width: 30px;
  height: 30px;
  background: rgba(29, 78, 216, 0.06);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  color: rgba(29, 78, 216, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}
.notif-modal__close:hover {
  background: rgba(29, 78, 216, 0.12);
  color: #1d4ed8;
}

.notif-modal__body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  scrollbar-width: thin;
  scrollbar-color: rgba(29,78,216,0.15) transparent;
}
.notif-modal__body::-webkit-scrollbar { width: 5px; }
.notif-modal__body::-webkit-scrollbar-track { background: transparent; }
.notif-modal__body::-webkit-scrollbar-thumb { background: rgba(29,78,216,0.12); border-radius: 100px; }

.notif-empty {
  padding: 48px 24px;
  text-align: center;
  color: rgba(29, 78, 216, 0.38);
  font-family: "Be Vietnam Pro", sans-serif;
}
.notif-empty__icon { font-size: 42px; margin-bottom: 12px; }
.notif-empty__text { font-size: 14px; font-weight: 500; }

.notif-list { display: flex; flex-direction: column; gap: 2px; }
.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 13px;
  padding: 13px 12px;
  border-radius: 14px;
  transition: background 0.14s;
}
.notif-item:hover { background: #f8fafc; }
.notif-item--unread { background: #f0f7ff; }
.notif-item--unread:hover { background: #e6f0fd; }

.notif-item__icon { font-size: 22px; flex-shrink: 0; margin-top: 2px; }
.notif-item__body { flex: 1; min-width: 0; }
.notif-item__title {
  font-size: 13.5px;
  font-weight: 600;
  color: #1d4ed8;
  margin-bottom: 4px;
  line-height: 1.3;
  font-family: "Be Vietnam Pro", sans-serif;
}
.notif-item__msg {
  font-size: 13px;
  color: rgb(65, 63, 63);
  line-height: 1.55;
  margin-bottom: 5px;
  font-family: "Be Vietnam Pro", sans-serif;
}
.notif-item__time {
  font-size: 11px;
  color: rgba(29, 78, 216, 0.32);
  font-family: "Be Vietnam Pro", sans-serif;
}
.notif-item__read-btn {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: #ffffff;
  border: 1.5px solid rgba(29, 78, 216, 0.12);
  border-radius: 50%;
  cursor: pointer;
  color: rgba(22, 163, 74, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  margin-top: 2px;
}
.notif-item__read-btn:hover {
  background: #16a34a;
  color: #ffffff;
  border-color: #16a34a;
}

.notif-modal__foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid rgba(29, 78, 216, 0.08);
}
.btn-ghost {
  height: 36px;
  padding: 0 18px;
  border-radius: 100px;
  border: 1.5px solid rgba(29, 78, 216, 0.15);
  background: transparent;
  color: rgba(29, 78, 216, 0.65);
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, color 0.15s;
}
.btn-ghost:hover {
  border-color: rgba(29, 78, 216, 0.35);
  background: rgba(29, 78, 216, 0.05);
  color: #1d4ed8;
}
.btn-primary {
  height: 36px;
  padding: 0 18px;
  border-radius: 100px;
  border: none;
  background: #1d4ed8;
  color: #ffffff;
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(29, 78, 216, 0.28);
  transition: background 0.15s, transform 0.15s, box-shadow 0.15s;
}
.btn-primary:hover {
  background: #1e40af;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(29, 78, 216, 0.38);
}

/* ── Main ───────────────────────────────────────────────── */
.app-main {
  padding: 24px 20px;
  font-family: "Be Vietnam Pro", sans-serif;
}

/* ── Footer ─────────────────────────────────────────────── */
.app-footer {
  background: #0f0f11;
  border-top: 1px solid #27272a;
}

.footer-inner {
  max-width: 1360px;
  margin: 0 auto;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.footer-brand__mark {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.footer-brand__name {
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 13.5px;
  font-weight: 700;
  color: #f4f4f5;
  letter-spacing: -0.3px;
}

.footer-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: center;
}
.footer-nav__link {
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: #71717a;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 6px;
  text-decoration: none;
  transition: color 0.15s, background 0.15s;
}
.footer-nav__link:hover {
  color: #e4e4e7;
  background: #27272a;
}

.footer-copy {
  font-family: "Be Vietnam Pro", sans-serif;
  font-size: 11.5px;
  color: #3f3f46;
  flex-shrink: 0;
}
</style>