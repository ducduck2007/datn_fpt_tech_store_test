<!-- 
============================================================
FILE: src/pages/customer/CustomerHome.vue
UPDATED: 
  - Remove redundant pagination (Unified Server-side pagination)
  - Fix Image URL (Backend absolute path)
  - Fix Category Filtering (Match 'categoryIds' param)
  - Preserved all existing Banner & Event logic
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
        <div class="wb-orb wb-orb--1"></div>
        <div class="wb-orb wb-orb--2"></div>
        <div class="wb-orb wb-orb--3"></div>

        <div class="wb-inner">
          <div class="wb-icon-wrap">
            <span class="wb-emoji">🎁</span>
            <div class="wb-pulse-ring"></div>
          </div>

          <div class="wb-text">
            <div class="wb-badge">✨ Dành riêng cho bạn</div>
            <div class="wb-title">{{ notif.title }}</div>
            <div class="wb-message" v-html="formatMessage(notif.message)"></div>
          </div>

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
                Trang: <b>{{ page + 1 }}</b>
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
            </div>
          </div>
          <el-divider />

          <el-skeleton v-if="loading" :rows="6" animated />
          <template v-else>
            <el-empty v-if="products.length === 0" description="No products" />
            <div v-else class="row g-3">
              <!-- Hiển thị toàn bộ products từ Server trả về -->
              <div v-for="p in products" :key="p.id" class="col-12 col-sm-6 col-xl-4">
                <el-card shadow="hover" class="h-100">
                  <template #header>
                    <div class="d-flex align-items-center justify-content-between">
                      <span class="fw-bold text-truncate" style="max-width: 220px" :title="p.name">{{ p.name }}</span>
                      <el-tag size="small" effect="light">Laptop</el-tag>
                    </div>
                  </template>
                  <div class="thumb"><img :src="p.imageUrl" :alt="p.name" /></div>
                  <div class="mt-2 muted product-desc">{{ p.description || "—" }}</div>
                  <div class="mt-3 d-flex align-items-center justify-content-between">
                    <div class="fw-bold text-danger">{{ p.priceText }}</div>
                    <el-button size="small" type="primary"
                      :disabled="!isCustomer || !p.defaultVariantId" @click="goOrder(p)">Order</el-button>
                  </div>
                </el-card>
              </div>
            </div>

            <!-- CHỈ GIỮ LẠI PHÂN TRANG NÀY (SERVER-SIDE) -->
            <div class="d-flex justify-content-center mt-5">
              <el-pagination 
                background 
                layout="total, prev, pager, next" 
                :page-size="20"
                :total="totalElements" 
                :current-page="page + 1"
                @current-change="onServerPageChange" 
              />
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
const cartStore = useCartStore();

const loading = ref(false);
const activeOnly = ref(true);
const categories = ref([]);
const products = ref([]);
const activeKey = ref("all");
const categoryId = ref(null);

// Logic phân trang chuẩn từ Server
const page = ref(0);
const totalElements = ref(0);
const searchTerm = ref("");

// Notifications refs
const welcomeNotifications   = ref([]);   
const birthdayNotifications  = ref([]);
const reminderNotifications  = ref([]);
const allNotifications       = ref([]);
const unreadNotificationCount = ref(0);
const notificationsDialog    = ref(false);
const tierProgressRef        = ref(null);
const spinExpiryBonuses      = ref([]);
const spinExpiryDismissed    = ref(new Set());

// --- FIX: Hàm xử lý URL ảnh tuyệt đối ---
function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/300?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
}

async function loadSpinExpiryBonuses() {
  if (!isCustomer.value) return;
  try {
    const res = await http.get("/api/spin-wheel/spin-expiry/preview?hours=24");
    const bonuses = res.data?.bonuses || [];
    spinExpiryBonuses.value = bonuses.filter(b => !spinExpiryDismissed.value.has(b.customerId));
  } catch (err) { console.error(err); }
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

// ── Product mapping ────────────────────────────────────────────────
function normalizeProducts(list) {
  return (list || []).map((p, idx) => {
    const id = p?.id ?? p?.productId ?? idx + 1;
    const name = p?.name ?? p?.title ?? `Product ${id}`;
    
    // FIX: Áp dụng fixImageUrl
    const imageUrl = fixImageUrl(p.imageUrl || p.thumbnailUrl);

    const price = p?.finalPrice ?? p?.price ?? p?.minPrice ?? null;
    const priceText = typeof price === "number"
        ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price)
        : price != null ? String(price) : "—";

    const variantId = p?.defaultVariantId ?? p?.variantId ?? p?.variants?.[0]?.id ?? null;

    return { 
      id, name, 
      description: p.description || "", 
      imageUrl, priceText, 
      defaultVariantId: variantId, 
      raw: p 
    };
  });
}

function normalizeCategories(list) {
  const unique = [];
  const names = new Set();
  (list || []).forEach(c => {
    if (!names.has(c.name)) {
      unique.push({
        id: c?.id ?? c?.categoryId ?? unique.length + 1,
        name: c?.name ?? c?.title ?? `Category ${unique.length + 1}`,
        raw: c,
      });
      names.add(c.name);
    }
  });
  return unique;
}

const titleText = computed(() => {
  if (!categoryId.value) return "All products";
  const c = categories.value.find(x => String(x.id) === String(categoryId.value));
  return c?.name || `Category #${categoryId.value}`;
});

// --- API Calls ---
async function reloadCategories() {
  try {
    const res = await categoriesApi.list(activeOnly.value);
    const root = res.data?.data || res.data || [];
    categories.value = normalizeCategories(Array.isArray(root) ? root : root.content);
  } catch (e) { categories.value = []; }
}

async function reloadProducts() {
  loading.value = true;
  try {
    // FIX: Sử dụng 'categoryIds' và tham số keyword từ header
    const params = { 
        page: page.value,
        categoryIds: categoryId.value ? String(categoryId.value) : undefined,
        keyword: searchTerm.value || undefined
    };
    const res = await productsApi.list(params);
    const data = res.data?.data || res.data;
    
    products.value = normalizeProducts(data.content || []);
    totalElements.value = data.totalElements || 0;
  } catch (e) {
    products.value = [];
    toast("Failed to load products.", "error");
  } finally { loading.value = false; }
}

async function reloadAll() {
  page.value = 0;
  await reloadCategories();
  await reloadProducts();
}

function onSelectCategory(key) {
  activeKey.value = key;
  categoryId.value = key === "all" ? null : Number(key);
  page.value = 0; 
  reloadProducts();
}

function onServerPageChange(page1Based) {
  page.value = Math.max(0, Number(page1Based || 1) - 1);
  reloadProducts();
}

async function goOrder(p) {
  try {
    await cartStore.addToCart(p.defaultVariantId, 1);
    toast("Đã thêm vào giỏ hàng", "success");
    if (tierProgressRef.value) tierProgressRef.value.refresh();
  } catch (e) { toast("Không thể thêm vào giỏ hàng", "error"); }
}

// ── Notifications Logic ──────────────────────────────────────────
async function loadNotifications() {
  if (!isCustomer.value) return;
  try {
    const unreadRes = await http.get("/api/auth/notifications/my?unreadOnly=true");
    const unreadNotifs = unreadRes.data || [];

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
  } catch (error) { console.error(error); }
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

async function handleWelcomeShop(notificationId) {
  await markNotificationAsRead(notificationId);
  document.querySelector('.col-12.col-lg-9')?.scrollIntoView({ behavior: "smooth", block: "start" });
}

function viewAllNotifications() {
  notificationsDialog.value = true;
  loadNotifications();
}

function notifIcon(type) {
  const map = {
    WELCOME: "🎁", BIRTHDAY: "🎂", PURCHASE_REMINDER: "🛒",
    WINBACK: "💝", VIP_TIER_UPGRADE: "⭐", SPIN_EXPIRY_WARNING: "🎡",
  };
  return map[type] ?? "🔔";
}

function formatMessage(message) {
  if (!message) return "";
  return message.replace(/\n/g, "<br>");
}

function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString("vi-VN");
}

onMounted(async () => {
  window.addEventListener("products:search", (e) => {
    searchTerm.value = String(e?.detail || "");
    page.value = 0;
    reloadProducts();
  });
  
  await reloadAll();
  
  if (isCustomer.value) {
    cartStore.refreshCount();
    loadNotifications();
    loadSpinExpiryBonuses();
    const notifInterval = setInterval(loadNotifications, 30000);
    onBeforeUnmount(() => clearInterval(notifInterval));
  }
});
</script>

<style scoped>
.kicker { font-size: 12px; opacity: 0.75; font-weight: 900; text-transform: uppercase; }
.title  { font-weight: 900; font-size: 18px; }
.muted  { color: rgba(15, 23, 42, 0.62); font-size: 13px; }
.thumb  { border-radius: 12px; overflow: hidden; border: 1px solid rgba(15,23,42,0.1); background: rgba(2,6,23,0.03); }
.thumb img { width: 100%; height: 180px; object-fit: cover; display: block; }

/* FIX: Line-clamp chuẩn hiện đại */
.product-desc { 
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2; 
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
  font-size: 12px;
  color: #64748b;
}

/* Banner Styles */
.welcome-banner { position: relative; border-radius: 18px; overflow: hidden; background: linear-gradient(135deg, #13111c 0%, #1e1b4b 50%, #1a1035 100%); border: 1px solid rgba(139, 92, 246, 0.25); box-shadow: 0 8px 32px rgba(99, 102, 241, 0.2); animation: wb-in 0.55s both; }
.wb-orb { position: absolute; border-radius: 50%; pointer-events: none; animation: wb-float 7s infinite; }
.wb-orb--1 { width: 220px; height: 220px; top: -80px; left: -50px; background: radial-gradient(circle, rgba(99,102,241,0.3) 0%, transparent 65%); }
.wb-inner { position: relative; z-index: 1; display: flex; align-items: center; gap: 18px; padding: 20px 22px 16px; flex-wrap: wrap; }
.wb-emoji { font-size: 42px; filter: drop-shadow(0 0 14px rgba(251, 191, 36, 0.65)); animation: wb-bounce 2.8s infinite; }
.wb-btn-primary { padding: 10px 22px; border-radius: 10px; border: none; font-weight: 700; cursor: pointer; background: linear-gradient(135deg, #fbbf24, #f59e0b); }

.event-btn { position: relative; display: inline-flex; align-items: center; gap: 6px; padding: 7px 16px; border: none; border-radius: 8px; background: linear-gradient(135deg, #f59e0b, #ef4444); color: #fff; font-size: 13px; font-weight: 600; cursor: pointer; }
.event-ping { position: absolute; top: -3px; right: -3px; width: 10px; height: 10px; border-radius: 50%; background: #fff; animation: ping 1.5s infinite; }

@keyframes ping { 0%,100% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.6); opacity: 0; } }
@keyframes wb-float { 0%,100% { transform: translate(0,0); } 50% { transform: translate(6px,-14px); } }
</style>