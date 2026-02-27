<!-- 
============================================================
FILE: src/pages/customer/CustomerHome.vue
CHANGES vs bản gốc:
  1. Thêm welcomeNotifications ref
  2. Thêm WELCOME banner UI (trước Birthday banner)
  3. loadNotifications: filter thêm WELCOME
  4. handleWelcomeShop handler mới
  5. notifIcon() helper cho dialog
  6. Toàn bộ CSS cho welcome-banner
============================================================
-->
<template>
  <div class="container-xl">
    <TierProgressBar 
      v-if="isCustomer" 
      ref="tierProgressRef"
      class="mb-4"
    />

    <!-- ✨ WELCOME Banner — Khách chưa mua lần nào -->
    <transition-group name="wb-list" tag="div">
      <div
        v-for="notif in welcomeNotifications"
        :key="notif.id"
        class="welcome-banner mb-4"
      >
        <!-- Ambient orbs -->
        <div class="wb-orb wb-orb--1"></div>
        <div class="wb-orb wb-orb--2"></div>
        <div class="wb-orb wb-orb--3"></div>

        <div class="wb-inner">
          <!-- Icon -->
          <div class="wb-icon-wrap">
            <span class="wb-emoji">🎁</span>
            <div class="wb-pulse-ring"></div>
          </div>

          <!-- Text -->
          <div class="wb-text">
            <div class="wb-badge">✨ Dành riêng cho bạn</div>
            <div class="wb-title">{{ notif.title }}</div>
            <div class="wb-message" v-html="formatMessage(notif.message)"></div>
          </div>

          <!-- Actions -->
          <div class="wb-actions">
            <button class="wb-btn-primary" @click="handleWelcomeShop(notif.id)">
              🛍️ Mua sắm ngay
            </button>
            <button class="wb-btn-secondary" @click="viewAllNotifications">
              Xem thông báo
            </button>
            <button class="wb-btn-close" @click="markNotificationAsRead(notif.id)" title="Đóng">
              ✕
            </button>
          </div>
        </div>

        <!-- Shimmer bar -->
        <div class="wb-bottom-bar"></div>
      </div>
    </transition-group>

    <!-- Birthday Notification Banner -->
    <div v-if="birthdayNotifications.length > 0" class="mb-4">
      <el-card 
        v-for="notif in birthdayNotifications" 
        :key="notif.id"
        shadow="always" 
        class="birthday-notification-card mb-3"
      >
        <div class="d-flex align-items-start gap-3">
          <div class="birthday-icon-large">🎂</div>
          <div class="flex-grow-1">
            <h3 class="mb-2">{{ notif.title }}</h3>
            <div class="birthday-message" v-html="formatMessage(notif.message)"></div>
            <div class="mt-3 d-flex gap-2">
              <el-button type="primary" size="large" @click="markNotificationAsRead(notif.id)">
                Cảm ơn! 🎉
              </el-button>
              <el-button size="large" @click="viewAllNotifications">
                Xem tất cả thông báo
              </el-button>
            </div>
          </div>
          <el-icon class="close-icon" @click="markNotificationAsRead(notif.id)" :size="20">
            <Close />
          </el-icon>
        </div>
      </el-card>
    </div>

    <!-- Purchase Reminder / Winback Banner -->
    <div v-if="reminderNotifications.length > 0" class="mb-4">
      <el-card
        v-for="notif in reminderNotifications"
        :key="notif.id"
        shadow="always"
        class="reminder-notification-card mb-3"
      >
        <div class="d-flex align-items-start gap-3">
          <div class="reminder-icon-large">
            {{ notif.type === 'WINBACK' ? '💝' : '🛒' }}
          </div>
          <div class="flex-grow-1">
            <h3 class="mb-2">{{ notif.title }}</h3>
            <div class="reminder-message" v-html="formatMessage(notif.message)"></div>
            <div class="mt-3 d-flex gap-2">
              <el-button type="primary" size="large" @click="handleReminderClick(notif.id)">
                Xem ngay 🛍️
              </el-button>
              <el-button size="large" @click="markNotificationAsRead(notif.id)">
                Để sau
              </el-button>
            </div>
          </div>
          <el-icon class="close-icon" @click="markNotificationAsRead(notif.id)" :size="20">
            <Close />
          </el-icon>
        </div>
      </el-card>
    </div>

    <!-- ✨ Spin Expiry Warning Banners -->
    <div v-if="spinExpiryBonuses.length > 0" class="mb-4">
      <div
        v-for="bonus in spinExpiryBonuses"
        :key="bonus.customerId"
        class="spin-expiry-banner mb-3"
        :class="getSpinUrgencyClass(bonus.hoursLeft)"
      >
        <div class="spin-expiry-banner__glow"></div>
        <div class="spin-expiry-banner__content">
          <div class="spin-expiry-banner__icon">
            <span class="spin-wheel-emoji">🎡</span>
            <div class="spin-urgency-ring" :class="getSpinUrgencyClass(bonus.hoursLeft)"></div>
          </div>
          <div class="spin-expiry-banner__text">
            <div class="spin-expiry-banner__title">Ưu đãi vòng quay sắp hết hạn!</div>
            <div class="spin-expiry-banner__body">
              Bạn có ưu đãi giảm
              <span class="spin-expiry-banner__discount">{{ bonus.discountBonus }}%</span>
              chưa sử dụng —
              <span class="spin-expiry-banner__urgency" :class="getSpinUrgencyClass(bonus.hoursLeft)">
                còn <b>{{ bonus.hoursLeft }} giờ</b> nữa hết hạn!
              </span>
            </div>
            <div class="spin-expiry-banner__expires">
              Hết hạn lúc: {{ formatDate(bonus.expiresAt) }}
            </div>
          </div>
          <div class="spin-expiry-banner__actions">
            <button class="spin-btn-use-now" @click="handleSpinExpiryUseNow(bonus)">Dùng ngay</button>
            <button class="spin-btn-dismiss" @click="dismissSpinExpiry(bonus.customerId)">✕</button>
          </div>
        </div>
        <div class="spin-expiry-progress">
          <div
            class="spin-expiry-progress__bar"
            :style="{ width: getSpinProgressWidth(bonus.hoursLeft) }"
            :class="getSpinUrgencyClass(bonus.hoursLeft)"
          ></div>
        </div>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12 col-lg-3">
        <el-card shadow="never">
          <div class="d-flex align-items-center justify-content-between">
            <div>
              <div class="kicker">Categories</div>
              <div class="title">Browse</div>
            </div>
            <el-switch v-model="activeOnly" active-text="Active only" @change="reloadAll" />
          </div>
          <el-divider />
          <el-menu :default-active="String(activeKey)" class="w-100" @select="onSelectCategory">
            <el-menu-item index="all">All</el-menu-item>
            <el-menu-item v-for="c in categories" :key="c.id" :index="String(c.id)">
              {{ c.name }}
            </el-menu-item>
          </el-menu>
        </el-card>
      </div>

      <div class="col-12 col-lg-9">
        <el-card shadow="never">
          <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
            <div>
              <div class="kicker">Products</div>
              <div class="title">{{ titleText }}</div>
              <div class="muted">
                Page: <b>{{ page + 1 }}</b>
                <span v-if="searchTerm"> • Search: <b>{{ searchTerm }}</b></span>
              </div>
            </div>

            <div class="d-flex align-items-center gap-2 flex-wrap">
              <el-button :loading="loading" @click="reloadProducts">Reload</el-button>

              <el-button v-if="isCustomer" type="info" plain @click="$router.push('/profile')">
                <el-icon class="me-1"><User /></el-icon>
                Profile
              </el-button>

              <button v-if="isCustomer" class="event-btn" @click="$router.push('/spin-wheel')">
                <span class="event-icon">🎡</span>
                <span class="event-text">Sự kiện</span>
                <span class="event-ping"></span>
              </button>

              <el-badge v-if="isCustomer" :value="unreadNotificationCount" :hidden="unreadNotificationCount === 0">
                <el-button type="warning" plain @click="viewAllNotifications">
                  <el-icon class="me-1"><Bell /></el-icon>
                  Thông báo
                </el-button>
              </el-badge>

              <el-badge v-if="isCustomer" :value="cartStore.count">
                <el-button type="success" plain @click="$router.push('/cart')">🛒 Giỏ hàng</el-button>
              </el-badge>

              <el-button type="primary" :disabled="!isCustomer" @click="$router.push('/orders/new')">
                Tạo đơn hàng
              </el-button>
            </div>
          </div>

          <el-divider />

          <el-skeleton v-if="loading" :rows="6" animated />
          <template v-else>
            <el-empty v-if="filteredProducts.length === 0" description="No products" />
            <div v-else class="row g-3">
              <div v-for="p in pagedClientProducts" :key="p.id" class="col-12 col-sm-6 col-xl-4">
                <el-card shadow="hover" class="h-100">
                  <template #header>
                    <div class="d-flex align-items-center justify-content-between">
                      <span class="fw-bold text-truncate" style="max-width: 220px" :title="p.name">{{ p.name }}</span>
                      <el-tag size="small" effect="light">Laptop</el-tag>
                    </div>
                  </template>
                  <div class="thumb"><img :src="p.imageUrl" :alt="p.name" /></div>
                  <div class="mt-2 muted" style="min-height: 38px">{{ p.description || "—" }}</div>
                  <div class="mt-3 d-flex align-items-center justify-content-between">
                    <div class="fw-bold">{{ p.priceText }}</div>
                    <el-button size="small" type="primary"
                      :disabled="!isCustomer || !p.defaultVariantId" @click="goOrder(p)">Order</el-button>
                  </div>
                  <div v-if="!p.defaultVariantId" class="mt-2 muted small">
                    (No variantId found in product payload)
                  </div>
                </el-card>
              </div>
            </div>

            <div class="d-flex justify-content-end mt-3">
              <el-pagination background layout="prev, pager, next" :page-size="clientPageSize"
                :total="filteredProducts.length" :current-page="clientPage"
                @current-change="(v) => (clientPage = v)" />
            </div>
            <div class="d-flex justify-content-center mt-2">
              <el-pagination background layout="prev, pager, next" :page-size="1"
                :total="Math.max(1, serverTotalPages)" :current-page="page + 1"
                @current-change="onServerPageChange" />
            </div>
          </template>
        </el-card>
      </div>
    </div>

    <!-- Notifications Dialog -->
    <el-dialog v-model="notificationsDialog" title="🔔 Thông báo của bạn" width="600px" @close="loadNotifications">
      <div v-if="allNotifications.length === 0" class="text-center py-4">
        <el-empty description="Không có thông báo nào" />
      </div>
      <div v-else class="notifications-list">
        <div
          v-for="notif in allNotifications"
          :key="notif.id"
          class="notification-item"
          :class="{ 'unread': !notif.isRead }"
        >
          <div class="d-flex align-items-start gap-3">
            <!-- icon tự động theo type, bao gồm WELCOME -->
            <div class="notif-icon">{{ notifIcon(notif.type) }}</div>
            <div class="flex-grow-1">
              <h5 class="mb-1">{{ notif.title }}</h5>
              <p class="mb-1" v-html="formatMessage(notif.message)"></p>
              <small class="text-muted">{{ formatDate(notif.createdAt) }}</small>
            </div>
            <el-button v-if="!notif.isRead" size="small" type="primary"
              @click="markNotificationAsRead(notif.id)">
              Đánh dấu đã đọc
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="notificationsDialog = false">Đóng</el-button>
        <el-button type="primary" @click="markAllAsRead">Đánh dấu tất cả đã đọc</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { categoriesApi } from "../../api/categories.api";
import { productsApi } from "../../api/products.api";
import { useAuthStore } from "../../stores/auth";
import { toast } from "../../ui/toast";
import { User, Bell, Close } from '@element-plus/icons-vue';
import { useCartStore } from "../../stores/cart";
import http from "../../api/http";
import TierProgressBar from "../../components/TierProgressBar.vue";

const auth = useAuthStore();
const isCustomer = computed(() => auth.isCustomer);

const loading = ref(false);
const activeOnly = ref(true);
const categories = ref([]);
const products = ref([]);
const activeKey = ref("all");
const categoryId = ref(null);
const page = ref(0);
const serverTotalPages = ref(1);
const searchTerm = ref("");
const clientPage = ref(1);
const clientPageSize = 9;
const cartStore = useCartStore();

// ── Notifications ──────────────────────────────────────────────────
const welcomeNotifications   = ref([]);   // ← MỚI
const birthdayNotifications  = ref([]);
const reminderNotifications  = ref([]);
const allNotifications       = ref([]);
const unreadNotificationCount = ref(0);
const notificationsDialog    = ref(false);
const tierProgressRef        = ref(null);

// ── Spin expiry ────────────────────────────────────────────────────
const spinExpiryBonuses  = ref([]);
const spinExpiryDismissed = ref(new Set());

async function loadSpinExpiryBonuses() {
  if (!isCustomer.value) return;
  try {
    const res = await http.get("/api/spin-wheel/spin-expiry/preview?hours=24");
    const bonuses = res.data?.bonuses || [];
    spinExpiryBonuses.value = bonuses.filter(b => !spinExpiryDismissed.value.has(b.customerId));
  } catch (err) { console.error("Failed to load spin expiry bonuses:", err); }
}

function dismissSpinExpiry(customerId) {
  spinExpiryDismissed.value.add(customerId);
  spinExpiryBonuses.value = spinExpiryBonuses.value.filter(b => b.customerId !== customerId);
}

function handleSpinExpiryUseNow(bonus) {
  dismissSpinExpiry(bonus.customerId);
  window.location.href = "/orders/new";
}

function getSpinUrgencyClass(hoursLeft) {
  if (hoursLeft <= 3) return "spin-critical";
  if (hoursLeft <= 12) return "spin-warning";
  return "spin-normal";
}

function getSpinProgressWidth(hoursLeft) {
  return `${Math.max(0, Math.min(100, (hoursLeft / 24) * 100))}%`;
}

// ── Product helpers ────────────────────────────────────────────────
function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  const root = payload?.data ?? payload;
  if (Array.isArray(root)) return root;
  const candidates = [root, root?.data, root?.data?.data, payload?.data, payload?.data?.data].filter(Boolean);
  for (const c of candidates) {
    if (Array.isArray(c)) return c;
    for (const k of ["content", "items", "results", "rows", "list"])
      if (Array.isArray(c?.[k])) return c[k];
  }
  return [];
}

function pickTotalPages(payload) {
  const root = payload?.data ?? payload;
  const tp = root?.totalPages ?? root?.data?.totalPages ?? payload?.totalPages ?? payload?.data?.totalPages ?? null;
  return typeof tp === "number" && tp > 0 ? tp : 1;
}

function getDefaultVariantId(raw) {
  const v = raw?.defaultVariantId ?? raw?.variantId ?? raw?.variants?.[0]?.id
          ?? raw?.variants?.[0]?.variantId ?? raw?.variant?.id ?? null;
  return v != null ? Number(v) : null;
}

function normalizeProducts(list) {
  return (list || []).map((p, idx) => {
    const id = p?.id ?? p?.productId ?? idx + 1;
    const name = p?.name ?? p?.title ?? `Product ${id}`;
    const description = p?.description ?? p?.shortDescription ?? "";
    const imageUrl = p?.imageUrl || p?.thumbnailUrl
        || `https://placehold.co/640x420/png?text=${encodeURIComponent(name)}`;
    const price = p?.finalPrice ?? p?.price ?? p?.minPrice ?? null;
    const priceText = typeof price === "number"
        ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price)
        : price != null ? String(price) : "—";
    return { id, name, description, imageUrl, priceText, defaultVariantId: getDefaultVariantId(p), raw: p };
  });
}

function normalizeCategories(list) {
  return (list || []).map((c, idx) => ({
    id: c?.id ?? c?.categoryId ?? idx + 1,
    name: c?.name ?? c?.title ?? `Category ${idx + 1}`,
    raw: c,
  }));
}

const titleText = computed(() => {
  if (!categoryId.value) return "All products";
  const c = categories.value.find(x => String(x.id) === String(categoryId.value));
  return c?.name || `Category #${categoryId.value}`;
});

const filteredProducts = computed(() => {
  const kw = (searchTerm.value || "").trim().toLowerCase();
  const list = products.value || [];
  if (!kw) return list;
  return list.filter(p => `${p.name} ${p.description || ""}`.toLowerCase().includes(kw));
});

const pagedClientProducts = computed(() => {
  const start = (clientPage.value - 1) * clientPageSize;
  return filteredProducts.value.slice(start, start + clientPageSize);
});

function onSelectCategory(key) {
  activeKey.value = key;
  categoryId.value = key === "all" ? null : Number(key);
  page.value = 0; clientPage.value = 1;
  reloadProducts();
}

async function goOrder(p) {
  try {
    await cartStore.addToCart(p.defaultVariantId, 1);
    toast("Đã thêm vào giỏ hàng", "success");
    if (tierProgressRef.value) tierProgressRef.value.refresh();
  } catch (e) {
    toast("Không thể thêm vào giỏ hàng", "error");
    console.error(e);
  }
}

async function reloadCategories() {
  try {
    const res = await categoriesApi.list(activeOnly.value);
    categories.value = normalizeCategories(extractList(res?.data));
  } catch (e) { categories.value = []; toast("Failed to load categories.", "error"); }
}

async function reloadProducts() {
  loading.value = true;
  try {
    const res = await productsApi.list({ page: page.value, categoryId: categoryId.value ?? undefined });
    serverTotalPages.value = pickTotalPages(res?.data);
    products.value = normalizeProducts(extractList(res?.data));
  } catch (e) {
    products.value = []; serverTotalPages.value = 1;
    toast("Failed to load products.", "error");
  } finally { loading.value = false; }
}

async function reloadAll() {
  await reloadCategories();
  await reloadProducts();
}

function onServerPageChange(page1Based) {
  page.value = Math.max(0, Number(page1Based || 1) - 1);
  clientPage.value = 1;
  reloadProducts();
}

// ── Notifications ──────────────────────────────────────────────────
async function loadNotifications() {
  if (!isCustomer.value) return;
  try {
    const unreadRes = await http.get("/api/auth/notifications/my?unreadOnly=true");
    const unreadNotifs = unreadRes.data || [];

    // ← THÊM: tách WELCOME ra banner riêng
    welcomeNotifications.value  = unreadNotifs.filter(n => n.type === "WELCOME");
    birthdayNotifications.value = unreadNotifs.filter(n => n.type === "BIRTHDAY");
    reminderNotifications.value = unreadNotifs.filter(
      n => n.type === "PURCHASE_REMINDER" || n.type === "WINBACK"
    );

    const countRes = await http.get("/api/auth/notifications/my/unread-count");
    unreadNotificationCount.value = countRes.data?.unreadCount || 0;

    if (notificationsDialog.value) {
      const allRes = await http.get("/api/auth/notifications/my");
      allNotifications.value = allRes.data || [];
    }
  } catch (error) { console.error("Load notifications error:", error); }
}

async function markNotificationAsRead(notificationId) {
  try {
    await http.put(`/api/auth/notifications/${notificationId}/read`);
    await loadNotifications();
  } catch (error) { toast("Không thể đánh dấu đã đọc", "error"); }
}

async function markAllAsRead() {
  try {
    await http.put("/api/auth/notifications/read-all");
    await loadNotifications();
    toast("Đã đánh dấu tất cả đã đọc", "success");
  } catch (error) { toast("Không thể đánh dấu tất cả", "error"); }
}

async function handleReminderClick(notificationId) {
  await markNotificationAsRead(notificationId);
  window.scrollTo({ top: 400, behavior: "smooth" });
}

// ← THÊM: handler cho WELCOME banner "Mua sắm ngay"
async function handleWelcomeShop(notificationId) {
  await markNotificationAsRead(notificationId);
  // Scroll nhẹ xuống danh sách sản phẩm
  document.querySelector('.col-12.col-lg-9')?.scrollIntoView({ behavior: "smooth", block: "start" });
}

function viewAllNotifications() {
  notificationsDialog.value = true;
  loadNotifications();
}

// ← THÊM: map type → emoji cho dialog
function notifIcon(type) {
  const map = {
    WELCOME:             "🎁",
    BIRTHDAY:            "🎂",
    PURCHASE_REMINDER:   "🛒",
    WINBACK:             "💝",
    VIP_TIER_UPGRADE:    "⭐",
    SPIN_EXPIRY_WARNING: "🎡",
  };
  return map[type] ?? "🔔";
}

function formatMessage(message) {
  if (!message) return "";
  return message.replace(/\n/g, "<br>");
}

function formatDate(dateString) {
  const date = new Date(dateString);
  const now  = new Date();
  const diffMs    = now - date;
  const diffMins  = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays  = Math.floor(diffMs / 86400000);
  if (diffMins  < 1)  return "Vừa xong";
  if (diffMins  < 60) return `${diffMins} phút trước`;
  if (diffHours < 24) return `${diffHours} giờ trước`;
  if (diffDays  < 7)  return `${diffDays} ngày trước`;
  return date.toLocaleDateString("vi-VN");
}

function onSearchEvent(e) {
  searchTerm.value = String(e?.detail || "");
  clientPage.value = 1;
}

onMounted(async () => {
  window.addEventListener("products:search", onSearchEvent);
  await reloadAll();
  if (isCustomer.value) {
    cartStore.refreshCount();
    loadNotifications();
    loadSpinExpiryBonuses();

    const notifInterval = setInterval(loadNotifications, 30000);
    const spinInterval  = setInterval(loadSpinExpiryBonuses, 10 * 60 * 1000);

    onBeforeUnmount(() => {
      clearInterval(notifInterval);
      clearInterval(spinInterval);
    });
  }
});

onBeforeUnmount(() => {
  window.removeEventListener("products:search", onSearchEvent);
});
</script>

<style scoped>
.kicker { font-size: 12px; opacity: 0.75; font-weight: 900; text-transform: uppercase; }
.title  { font-weight: 900; font-size: 18px; }
.muted  { color: rgba(15, 23, 42, 0.62); font-size: 13px; }
.thumb  {
  border-radius: 12px; overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.1);
  background: rgba(2, 6, 23, 0.03);
}
.thumb img { width: 100%; height: 180px; object-fit: cover; display: block; }
.small { font-size: 12px; }

/* ================================================================
   ✨ WELCOME BANNER
================================================================ */
.welcome-banner {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(135deg, #13111c 0%, #1e1b4b 50%, #1a1035 100%);
  border: 1px solid rgba(139, 92, 246, 0.25);
  box-shadow:
    0 8px 32px rgba(99, 102, 241, 0.2),
    0 1px 0 rgba(255, 255, 255, 0.05) inset;
  animation: wb-in 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

/* Orbs */
.wb-orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  animation: wb-float 7s ease-in-out infinite;
}
.wb-orb--1 {
  width: 220px; height: 220px; top: -80px; left: -50px;
  background: radial-gradient(circle, rgba(99,102,241,0.3) 0%, transparent 65%);
}
.wb-orb--2 {
  width: 140px; height: 140px; bottom: -40px; right: 80px;
  background: radial-gradient(circle, rgba(236,72,153,0.2) 0%, transparent 65%);
  animation-delay: -3.5s;
}
.wb-orb--3 {
  width: 80px; height: 80px; top: 10px; right: 200px;
  background: radial-gradient(circle, rgba(251,191,36,0.15) 0%, transparent 65%);
  animation-delay: -1.5s; animation-duration: 5s;
}

/* Inner layout */
.wb-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 22px 16px;
  flex-wrap: wrap;
}

/* Icon */
.wb-icon-wrap {
  position: relative;
  flex-shrink: 0;
  width: 60px; height: 60px;
  display: flex; align-items: center; justify-content: center;
}
.wb-emoji {
  font-size: 42px; line-height: 1;
  filter: drop-shadow(0 0 14px rgba(251, 191, 36, 0.65));
  animation: wb-bounce 2.8s ease-in-out infinite;
  display: block;
}
.wb-pulse-ring {
  position: absolute; inset: -8px; border-radius: 50%;
  border: 2px solid rgba(251, 191, 36, 0.45);
  animation: wb-ring 2.2s ease-in-out infinite;
}

/* Text */
.wb-text { flex: 1; min-width: 200px; }
.wb-badge {
  display: inline-block;
  background: rgba(251, 191, 36, 0.12);
  border: 1px solid rgba(251, 191, 36, 0.28);
  color: #fbbf24;
  font-size: 10.5px; font-weight: 800;
  text-transform: uppercase; letter-spacing: .1em;
  padding: 2px 11px; border-radius: 20px; margin-bottom: 7px;
}
.wb-title {
  font-size: 17px; font-weight: 800; color: #fff;
  margin: 0 0 6px; line-height: 1.3;
}
.wb-message {
  font-size: 13.5px; color: rgba(255,255,255,0.7);
  line-height: 1.6;
}

/* Actions */
.wb-actions {
  display: flex; align-items: center; gap: 10px;
  flex-shrink: 0; flex-wrap: wrap;
}
.wb-btn-primary {
  padding: 10px 22px; border-radius: 10px; border: none;
  font-size: 13.5px; font-weight: 700; cursor: pointer;
  color: #1a1035;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  box-shadow: 0 3px 14px rgba(251,191,36,0.45);
  transition: all .18s; white-space: nowrap;
}
.wb-btn-primary:hover  { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(251,191,36,.55); }
.wb-btn-primary:active { transform: translateY(0); }

.wb-btn-secondary {
  padding: 9px 18px; border-radius: 10px;
  border: 1.5px solid rgba(255,255,255,0.18);
  background: rgba(255,255,255,0.07);
  color: rgba(255,255,255,0.8);
  font-size: 13px; font-weight: 600; cursor: pointer;
  transition: all .18s; white-space: nowrap;
}
.wb-btn-secondary:hover { background: rgba(255,255,255,0.14); border-color: rgba(255,255,255,0.3); }

.wb-btn-close {
  width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(255,255,255,0.06);
  color: rgba(255,255,255,0.45); font-size: 12px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
}
.wb-btn-close:hover { background: rgba(255,255,255,0.16); color: #fff; }

/* Bottom shimmer bar */
.wb-bottom-bar {
  height: 3px;
  background: linear-gradient(90deg,
    transparent 0%, rgba(99,102,241,0.8) 20%,
    rgba(251,191,36,0.8) 50%,
    rgba(236,72,153,0.8) 80%, transparent 100%);
  background-size: 200% 100%;
  animation: wb-shimmer 3s linear infinite;
}

/* List transition */
.wb-list-enter-active { transition: all .5s cubic-bezier(0.34,1.56,0.64,1); }
.wb-list-leave-active  { transition: all .3s ease-in; }
.wb-list-enter-from    { opacity: 0; transform: translateY(-22px) scale(0.97); }
.wb-list-leave-to      { opacity: 0; transform: translateX(20px) scale(0.97); }
.wb-list-move          { transition: transform .4s ease; }

/* Keyframes */
@keyframes wb-in {
  from { opacity: 0; transform: translateY(-16px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes wb-float {
  0%,100% { transform: translate(0,0) scale(1); }
  50%     { transform: translate(6px,-14px) scale(1.05); }
}
@keyframes wb-bounce {
  0%,100% { transform: translateY(0) rotate(0deg); }
  35%     { transform: translateY(-8px) rotate(-6deg); }
  65%     { transform: translateY(-4px) rotate(4deg); }
}
@keyframes wb-ring {
  0%,100% { transform: scale(1); opacity: 0.8; }
  50%     { transform: scale(1.22); opacity: 0.15; }
}
@keyframes wb-shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}
/* ================================================================ */

/* Event Button */
.event-btn {
  position: relative; display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 16px; border: none; border-radius: 8px;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  color: #fff; font-size: 13px; font-weight: 600; cursor: pointer;
  transition: opacity .2s, transform .15s;
  box-shadow: 0 2px 8px rgba(239,68,68,.35);
}
.event-btn:hover  { opacity: .92; transform: translateY(-1px); }
.event-btn:active { transform: translateY(0); }
.event-icon { font-size: 16px; line-height: 1; }
.event-text { white-space: nowrap; }
.event-ping {
  position: absolute; top: -3px; right: -3px;
  width: 10px; height: 10px; border-radius: 50%; background: #fff;
  animation: ping 1.5s ease-in-out infinite;
}
@keyframes ping {
  0%,100% { transform: scale(1); opacity: 1; }
  50%     { transform: scale(1.6); opacity: 0; }
}

/* Birthday Notification */
.birthday-notification-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white; border: none; animation: slideIn 0.5s ease-out;
}
.birthday-notification-card :deep(.el-card__body) { padding: 24px; }
.birthday-icon-large { font-size: 64px; line-height: 1; animation: bounce 2s infinite; }
.birthday-message { font-size: 16px; line-height: 1.6; white-space: pre-line; }

/* Reminder / Winback */
.reminder-notification-card {
  background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
  color: white; border: none; animation: slideIn 0.5s ease-out;
}
.reminder-notification-card :deep(.el-card__body) { padding: 24px; }
.reminder-icon-large { font-size: 64px; line-height: 1; animation: bounce 2s infinite; }
.reminder-message { font-size: 16px; line-height: 1.6; white-space: pre-line; }

.close-icon { cursor: pointer; opacity: .8; transition: opacity .3s; }
.close-icon:hover { opacity: 1; }

/* Spin Expiry Banner */
.spin-expiry-banner {
  position: relative; border-radius: 14px; overflow: hidden;
  background: #0f0f1a; border: 1px solid rgba(255,255,255,.08);
  box-shadow: 0 4px 20px rgba(0,0,0,.35);
  animation: slideIn .45s ease-out; transition: transform .2s, box-shadow .2s;
}
.spin-expiry-banner:hover { transform: translateY(-2px); box-shadow: 0 8px 28px rgba(0,0,0,.45); }
.spin-expiry-banner.spin-normal  { border-color: rgba(99,179,237,.3); }
.spin-expiry-banner.spin-warning { border-color: rgba(251,191,36,.4); }
.spin-expiry-banner.spin-critical {
  border-color: rgba(239,68,68,.55);
  animation: slideIn .45s ease-out, criticalGlow 2s ease-in-out infinite;
}
@keyframes criticalGlow {
  0%,100% { box-shadow: 0 4px 20px rgba(239,68,68,.15); }
  50%     { box-shadow: 0 4px 32px rgba(239,68,68,.45); }
}
.spin-expiry-banner__glow { position: absolute; inset: 0; pointer-events: none; }
.spin-normal  .spin-expiry-banner__glow { background: radial-gradient(ellipse at 8% 50%, rgba(99,179,237,.07) 0%, transparent 55%); }
.spin-warning .spin-expiry-banner__glow { background: radial-gradient(ellipse at 8% 50%, rgba(251,191,36,.09) 0%, transparent 55%); }
.spin-critical .spin-expiry-banner__glow { background: radial-gradient(ellipse at 8% 50%, rgba(239,68,68,.11) 0%, transparent 55%); }
.spin-expiry-banner__content {
  display: flex; align-items: center; gap: 16px; padding: 16px 18px 13px;
}
.spin-expiry-banner__icon {
  position: relative; flex-shrink: 0; width: 52px; height: 52px;
  display: flex; align-items: center; justify-content: center;
}
.spin-wheel-emoji {
  font-size: 40px; line-height: 1; display: block;
  filter: drop-shadow(0 0 8px rgba(251,191,36,.55));
}
.spin-urgency-ring {
  position: absolute; inset: -5px; border-radius: 50%;
  border: 2px solid transparent; animation: ringPulse 2s ease-in-out infinite;
}
.spin-urgency-ring.spin-normal  { border-color: rgba(99,179,237,.5); }
.spin-urgency-ring.spin-warning { border-color: rgba(251,191,36,.65); }
.spin-urgency-ring.spin-critical { border-color: rgba(239,68,68,.75); }
@keyframes ringPulse {
  0%,100% { transform: scale(1); opacity: 1; }
  50%     { transform: scale(1.14); opacity: .35; }
}
.spin-expiry-banner__text { flex: 1; min-width: 0; }
.spin-expiry-banner__title { font-size: 14.5px; font-weight: 700; color: #f0f0ff; margin-bottom: 4px; }
.spin-expiry-banner__body  { font-size: 13px; color: rgba(240,240,255,.7); line-height: 1.55; }
.spin-expiry-banner__discount { font-weight: 800; font-size: 15px; color: #fbbf24; }
.spin-expiry-banner__urgency { display: inline; color: rgba(240,240,255,.85); }
.spin-expiry-banner__urgency.spin-critical { color: #f87171; }
.spin-expiry-banner__urgency.spin-warning  { color: #fbbf24; }
.spin-expiry-banner__expires { font-size: 11px; color: rgba(240,240,255,.38); margin-top: 5px; }
.spin-expiry-banner__actions { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.spin-btn-use-now {
  padding: 9px 18px; border-radius: 8px; border: none; font-size: 13px; font-weight: 700;
  cursor: pointer; color: #fff;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  box-shadow: 0 2px 10px rgba(239,68,68,.35);
  transition: opacity .2s, transform .15s; white-space: nowrap;
}
.spin-btn-use-now:hover  { opacity: .88; transform: translateY(-1px); }
.spin-btn-use-now:active { transform: translateY(0); }
.spin-btn-dismiss {
  width: 28px; height: 28px; border-radius: 50%;
  border: 1px solid rgba(255,255,255,.12); background: rgba(255,255,255,.06);
  color: rgba(255,255,255,.45); font-size: 11px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: background .2s, color .2s;
}
.spin-btn-dismiss:hover { background: rgba(255,255,255,.14); color: #fff; }
.spin-expiry-progress { height: 3px; background: rgba(255,255,255,.05); }
.spin-expiry-progress__bar {
  height: 100%; transition: width .6s ease; border-radius: 0 2px 2px 0;
}
.spin-expiry-progress__bar.spin-normal  { background: linear-gradient(90deg, #63b3ed, #90cdf4); }
.spin-expiry-progress__bar.spin-warning { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.spin-expiry-progress__bar.spin-critical { background: linear-gradient(90deg, #ef4444, #f87171); }

@keyframes slideIn {
  from { transform: translateY(-18px); opacity: 0; }
  to   { transform: translateY(0); opacity: 1; }
}
@keyframes bounce {
  0%,100% { transform: translateY(0); }
  50%     { transform: translateY(-10px); }
}

/* Notifications Dialog */
.notifications-list { max-height: 500px; overflow-y: auto; }
.notification-item {
  padding: 16px; border-bottom: 1px solid #eee; transition: background-color .3s;
}
.notification-item:hover { background-color: #f5f7fa; }
.notification-item.unread { background-color: #ecf5ff; border-left: 3px solid #409eff; }
.notif-icon { font-size: 32px; line-height: 1; }
</style>