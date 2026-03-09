<!-- 
============================================================
FILE: src/pages/customer/CustomerHome.vue
REDESIGNED: Premium warm editorial e-commerce aesthetic
============================================================
-->
<template>
  <div class="ch-root">
    <!-- ── Page background ── -->
    <div class="ch-bg">
      <div class="ch-bg__noise"></div>
      <div class="ch-bg__orb ch-bg__orb--1"></div>
      <div class="ch-bg__orb ch-bg__orb--2"></div>
    </div>

    <div class="ch-wrap">
      <!-- ── Tier Progress ── -->
      <TierProgressBar v-if="isCustomer" ref="tierProgressRef" class="ch-tier" />

      <!-- ══════════════════════════════════════════════
           WELCOME BANNER
      ══════════════════════════════════════════════ -->
      <transition-group name="banner-list" tag="div" class="ch-banners">
        <div v-for="notif in welcomeNotifications" :key="notif.id" class="wb-card">
          <div class="wb-card__glow"></div>
          <div class="wb-card__left">
            <span class="wb-card__emoji">🎁</span>
            <div class="wb-card__pulse"></div>
          </div>
          <div class="wb-card__body">
            <div class="wb-card__eyebrow">✨ Dành riêng cho bạn</div>
            <div class="wb-card__title">{{ notif.title }}</div>
            <div class="wb-card__msg" v-html="formatMessage(notif.message)"></div>
          </div>
          <div class="wb-card__actions">
            <button class="btn-pill btn-pill--gold" @click="handleWelcomeShop(notif.id)">🛍️ Mua ngay</button>
            <button class="btn-pill btn-pill--ghost" @click="viewAllNotifications">Thông báo</button>
            <button class="wb-card__close" @click="markNotificationAsRead(notif.id)">✕</button>
          </div>
        </div>
      </transition-group>

      <!-- ══════════════════════════════════════════════
           BIRTHDAY BANNER
      ══════════════════════════════════════════════ -->
      <transition-group name="banner-list" tag="div" class="ch-banners">
        <div v-for="notif in birthdayNotifications" :key="notif.id" class="bday-card">
          <div class="bday-card__confetti">
            <span v-for="i in 8" :key="i" class="confetti-dot" :style="confettiStyle(i)"></span>
          </div>
          <div class="bday-card__icon">🎂</div>
          <div class="bday-card__body">
            <div class="bday-card__title">{{ notif.title }}</div>
            <div class="bday-card__msg" v-html="formatMessage(notif.message)"></div>
            <div class="bday-card__actions">
              <button class="btn-pill btn-pill--rose" @click="markNotificationAsRead(notif.id)">Cảm ơn! 🎉</button>
              <button class="btn-pill btn-pill--ghost" @click="viewAllNotifications">Xem tất cả</button>
            </div>
          </div>
          <button class="card-close-btn" @click="markNotificationAsRead(notif.id)">✕</button>
        </div>
      </transition-group>

      <!-- ══════════════════════════════════════════════
           REMINDER / WINBACK BANNER
      ══════════════════════════════════════════════ -->
      <transition-group name="banner-list" tag="div" class="ch-banners">
        <div v-for="notif in reminderNotifications" :key="notif.id" class="remind-card">
          <div class="remind-card__icon">{{ notif.type === 'WINBACK' ? '💝' : '🛒' }}</div>
          <div class="remind-card__body">
            <div class="remind-card__title">{{ notif.title }}</div>
            <div class="remind-card__msg" v-html="formatMessage(notif.message)"></div>
            <div class="remind-card__actions">
              <button class="btn-pill btn-pill--amber" @click="handleReminderClick(notif.id)">Xem ngay 🛍️</button>
              <button class="btn-pill btn-pill--ghost" @click="markNotificationAsRead(notif.id)">Để sau</button>
            </div>
          </div>
          <button class="card-close-btn" @click="markNotificationAsRead(notif.id)">✕</button>
        </div>
      </transition-group>

      <!-- ══════════════════════════════════════════════
           SPIN EXPIRY BANNER
      ══════════════════════════════════════════════ -->
      <transition-group name="banner-list" tag="div" class="ch-banners">
        <div
          v-for="bonus in spinExpiryBonuses"
          :key="bonus.customerId"
          class="spin-card"
          :class="'spin-card--' + getSpinUrgencyLevel(bonus.hoursLeft)"
        >
          <div class="spin-card__track">
            <div class="spin-card__fill" :style="{ width: getSpinProgressWidth(bonus.hoursLeft) }"></div>
          </div>
          <div class="spin-card__inner">
            <div class="spin-card__wheel">🎡</div>
            <div class="spin-card__body">
              <div class="spin-card__title">Ưu đãi vòng quay sắp hết hạn!</div>
              <div class="spin-card__sub">
                Giảm <strong>{{ bonus.discountBonus }}%</strong> —
                còn <strong>{{ bonus.hoursLeft }} giờ</strong> · Hết hạn {{ formatDate(bonus.expiresAt) }}
              </div>
            </div>
            <div class="spin-card__actions">
              <button class="btn-pill btn-pill--spin" @click="handleSpinExpiryUseNow(bonus)">Dùng ngay</button>
              <button class="spin-card__close" @click="dismissSpinExpiry(bonus.customerId)">✕</button>
            </div>
          </div>
        </div>
      </transition-group>

      <!-- ══════════════════════════════════════════════
           MAIN LAYOUT
      ══════════════════════════════════════════════ -->
      <div class="ch-layout">

        <!-- ── Sidebar ── -->
        <aside class="ch-sidebar">
          <div class="sidebar-card">
            <div class="sidebar-card__head">
              <div class="sidebar-head__label">Danh mục</div>
              <label class="toggle-switch" title="Chỉ hiện active">
                <input type="checkbox" v-model="activeOnly" @change="reloadAll" />
                <span class="toggle-switch__track"></span>
              </label>
            </div>

            <nav class="cat-nav">
              <button
                class="cat-nav__item"
                :class="{ active: activeKey === 'all' }"
                @click="onSelectCategory('all')"
              >
                <span class="cat-nav__dot"></span>
                <span>Tất cả</span>
                <span class="cat-nav__arrow">›</span>
              </button>
              <button
                v-for="c in categories"
                :key="c.id"
                class="cat-nav__item"
                :class="{ active: String(activeKey) === String(c.id) }"
                @click="onSelectCategory(String(c.id))"
              >
                <span class="cat-nav__dot"></span>
                <span>{{ c.name }}</span>
                <span class="cat-nav__arrow">›</span>
              </button>
            </nav>
          </div>
        </aside>

        <!-- ── Main content ── -->
        <main class="ch-main">

          <!-- Toolbar -->
          <div class="ch-toolbar">
            <div class="ch-toolbar__left">
              <div class="ch-toolbar__eyebrow">Sản phẩm</div>
              <h1 class="ch-toolbar__title">{{ titleText }}</h1>
              <div class="ch-toolbar__meta">
                Trang {{ page + 1 }}
                <span v-if="searchTerm"> · "<em>{{ searchTerm }}</em>"</span>
              </div>
            </div>

            <div class="ch-toolbar__right">
              <button class="icon-btn" :class="{ spinning: loading }" @click="reloadProducts" title="Reload">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                  <path d="M3 3v5h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>

              <template v-if="isCustomer">
                <button class="toolbar-btn toolbar-btn--profile" @click="$router.push('/profile')">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/><circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/></svg>
                  Profile
                </button>
                <button class="toolbar-btn toolbar-btn--security" @click="$router.push('/change-password')">
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
    <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.8"/>
    <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
  </svg>
  Đổi mật khẩu
</button>
                <button class="toolbar-btn toolbar-btn--event" @click="$router.push('/spin-wheel')">
                  <span>🎡</span> Sự kiện
                  <span class="event-ping"></span>
                </button>

                <button class="toolbar-btn toolbar-btn--notif" @click="viewAllNotifications">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/><path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                  Thông báo
                  <span v-if="unreadNotificationCount > 0" class="notif-badge">{{ unreadNotificationCount }}</span>
                </button>

                <button class="toolbar-btn toolbar-btn--cart" @click="$router.push('/cart')">
                  🛒 Giỏ hàng
                  <span v-if="cartStore.count > 0" class="notif-badge">{{ cartStore.count }}</span>
                </button>
              </template>
            </div>
          </div>

          <!-- Divider -->
          <div class="ch-divider"></div>

          <!-- Product grid -->
          <div v-if="loading" class="product-skeleton-grid">
            <div v-for="i in 6" :key="i" class="skeleton-card">
              <div class="skeleton-img"></div>
              <div class="skeleton-line skeleton-line--long"></div>
              <div class="skeleton-line skeleton-line--short"></div>
              <div class="skeleton-line skeleton-line--price"></div>
            </div>
          </div>

          <template v-else>
            <div v-if="products.length === 0" class="ch-empty">
              <div class="ch-empty__icon">🔍</div>
              <div class="ch-empty__title">Không có sản phẩm nào</div>
              <div class="ch-empty__sub">Thử đổi danh mục hoặc từ khóa khác</div>
            </div>

            <div v-else class="product-grid">
              <div v-for="(p, idx) in products" :key="p.id" class="product-card" :style="{ '--delay': idx * 0.04 + 's' }">
                <div class="product-card__img-wrap">
                  <img :src="p.imageUrl" :alt="p.name" class="product-card__img" loading="lazy" />
                  <div v-if="p.isNew" class="product-card__new-badge">NEW</div>
                  <button
                    class="product-card__quick-add"
                    :disabled="!isCustomer || !p.defaultVariantId"
                    @click="goOrder(p)"
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><line x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/><path d="M16 10a4 4 0 0 1-8 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                    Thêm vào giỏ
                  </button>
                </div>
                <div class="product-card__body">
                  <div class="product-card__name">{{ p.name }}</div>
                  <div class="product-card__desc">{{ p.description || "—" }}</div>
                  <div class="product-card__footer">
                    <div class="product-card__price">{{ p.priceText }}</div>
                    <button
                      class="product-card__order-btn"
                      :disabled="!isCustomer || !p.defaultVariantId"
                      @click="goOrder(p)"
                    >Order</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div class="ch-pagination">
              <button
                class="page-btn"
                :disabled="page === 0"
                @click="onServerPageChange(page)"
              >‹</button>
              <span class="page-info">{{ page + 1 }} / {{ totalPages }}</span>
              <button
                class="page-btn"
                :disabled="page + 1 >= totalPages"
                @click="onServerPageChange(page + 2)"
              >›</button>
            </div>
          </template>
        </main>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════
         NOTIFICATIONS DIALOG
    ══════════════════════════════════════════════ -->
    <Teleport to="body">
      <transition name="modal-fade">
        <div v-if="notificationsDialog" class="modal-overlay" @click.self="notificationsDialog = false">
          <div class="notif-modal">
            <div class="notif-modal__head">
              <div class="notif-modal__title">🔔 Thông báo</div>
              <button class="notif-modal__close" @click="notificationsDialog = false">✕</button>
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
                  <div class="notif-item__icon">{{ notifIcon(notif.type) }}</div>
                  <div class="notif-item__body">
                    <div class="notif-item__title">{{ notif.title }}</div>
                    <div class="notif-item__msg" v-html="formatMessage(notif.message)"></div>
                    <div class="notif-item__time">{{ formatDate(notif.createdAt) }}</div>
                  </div>
                  <button
                    v-if="!notif.isRead"
                    class="notif-item__read-btn"
                    @click="markNotificationAsRead(notif.id)"
                  >✓</button>
                </div>
              </div>
            </div>

            <div class="notif-modal__foot">
              <button class="btn-pill btn-pill--ghost" @click="notificationsDialog = false">Đóng</button>
              <button class="btn-pill btn-pill--primary" @click="markAllAsRead">Đánh dấu tất cả</button>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { categoriesApi } from "../../api/categories.api";
import { productsApi } from "../../api/products.api";
import { useAuthStore } from "../../stores/auth";
import { toast } from "../../ui/toast";
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

const page = ref(0);
const totalElements = ref(0);
const searchTerm = ref("");
const totalPages = computed(() => Math.max(1, Math.ceil(totalElements.value / 20)));

const welcomeNotifications   = ref([]);
const birthdayNotifications  = ref([]);
const reminderNotifications  = ref([]);
const allNotifications       = ref([]);
const unreadNotificationCount = ref(0);
const notificationsDialog    = ref(false);
const tierProgressRef        = ref(null);
const spinExpiryBonuses      = ref([]);
const spinExpiryDismissed    = ref(new Set());

function confettiStyle(i) {
  const colors = ['#f59e0b','#ef4444','#8b5cf6','#3b82f6','#10b981','#ec4899','#f97316','#06b6d4'];
  return {
    '--c': colors[i % colors.length],
    '--delay': (i * 0.13) + 's',
    '--x': (Math.sin(i * 1.2) * 60) + 'px',
  };
}

function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/300?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
}

async function loadSpinExpiryBonuses() {
  if (!isCustomer.value) return;
  try {
    const customerId = auth.user?.customerId || auth.customerId;
    if (!customerId) return;
    const res = await http.get(`/api/spin-wheel/status/${customerId}`);
    const data = res.data?.data ?? res.data ?? res;
    const bonus = Number(data?.currentBonus ?? data?.spinDiscountBonus ?? 0);
    const expiresAt = data?.bonusExpiresAt ?? data?.spinBonusExpiresAt ?? null;
    if (bonus > 0 && expiresAt) {
      const hoursLeft = Math.floor((new Date(expiresAt) - new Date()) / (1000 * 60 * 60));
      spinExpiryBonuses.value = (hoursLeft > 0 && hoursLeft <= 24)
        ? [{ customerId, discountBonus: bonus, hoursLeft, expiresAt }]
        : [];
    } else {
      spinExpiryBonuses.value = [];
    }
  } catch { spinExpiryBonuses.value = []; }
}

function dismissSpinExpiry(customerId) {
  spinExpiryDismissed.value.add(customerId);
  spinExpiryBonuses.value = spinExpiryBonuses.value.filter(b => b.customerId !== customerId);
}

function handleSpinExpiryUseNow(bonus) {
  dismissSpinExpiry(bonus.customerId);
  window.location.href = "/orders/new";
}

function getSpinUrgencyLevel(hoursLeft) {
  if (hoursLeft <= 3) return "critical";
  if (hoursLeft <= 12) return "warning";
  return "normal";
}

function getSpinProgressWidth(hoursLeft) {
  return `${Math.max(0, Math.min(100, (hoursLeft / 24) * 100))}%`;
}

function normalizeProducts(list) {
  return (list || []).map((p, idx) => {
    const id = p?.id ?? p?.productId ?? idx + 1;
    const price = p?.finalPrice ?? p?.price ?? p?.minPrice ?? null;
    return {
      id,
      name: p?.name ?? p?.title ?? `Product ${id}`,
      description: p.description || "",
      imageUrl: fixImageUrl(p.imageUrl || p.thumbnailUrl),
      priceText: typeof price === "number"
        ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price)
        : price != null ? String(price) : "—",
      defaultVariantId: p?.defaultVariantId ?? p?.variantId ?? p?.variants?.[0]?.id ?? null,
      isNew: p.isNew,
      raw: p,
    };
  });
}

function normalizeCategories(list) {
  const unique = [], names = new Set();
  (list || []).forEach(c => {
    if (!names.has(c.name)) {
      unique.push({ id: c?.id ?? unique.length + 1, name: c?.name ?? `Category ${unique.length + 1}`, raw: c });
      names.add(c.name);
    }
  });
  return unique;
}

const titleText = computed(() => {
  if (!categoryId.value) return "Tất cả sản phẩm";
  return categories.value.find(x => String(x.id) === String(categoryId.value))?.name || `Danh mục #${categoryId.value}`;
});

async function reloadCategories() {
  try {
    const res = await categoriesApi.list(activeOnly.value);
    const root = res.data?.data || res.data || [];
    categories.value = normalizeCategories(Array.isArray(root) ? root : root.content);
  } catch { categories.value = []; }
}

async function reloadProducts() {
  loading.value = true;
  try {
    const params = {
      page: page.value,
      categoryIds: categoryId.value ? String(categoryId.value) : undefined,
      keyword: searchTerm.value || undefined,
      isFaulty: false,
    };
    const res = await productsApi.list(params);
    const data = res.data?.data || res.data;
    products.value = normalizeProducts(data.content || []);
    totalElements.value = data.totalElements || 0;
  } catch {
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
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function goOrder(p) {
  try {
    await cartStore.addToCart(p.defaultVariantId, 1);
    toast("Đã thêm vào giỏ hàng", "success");
    if (tierProgressRef.value) tierProgressRef.value.refresh();
  } catch { toast("Không thể thêm vào giỏ hàng", "error"); }
}

async function loadNotifications() {
  if (!isCustomer.value) return;
  try {
    const unreadRes = await http.get("/api/auth/notifications/my?unreadOnly=true");
    const unreadNotifs = unreadRes.data || [];
    welcomeNotifications.value  = unreadNotifs.filter(n => n.type === "WELCOME");
    birthdayNotifications.value = unreadNotifs.filter(n => n.type === "BIRTHDAY");
    reminderNotifications.value = unreadNotifs.filter(n => ["PURCHASE_REMINDER","WINBACK"].includes(n.type));
    const countRes = await http.get("/api/auth/notifications/my/unread-count");
    unreadNotificationCount.value = countRes.data?.unreadCount || 0;
    if (notificationsDialog.value) {
      const allRes = await http.get("/api/auth/notifications/my");
      allNotifications.value = allRes.data || [];
    }
  } catch (e) { console.error(e); }
}

async function markNotificationAsRead(id) {
  try {
    await http.put(`/api/auth/notifications/${id}/read`);
    await loadNotifications();
  } catch { toast("Không thể đánh dấu đã đọc", "error"); }
}

async function markAllAsRead() {
  try {
    await http.put("/api/auth/notifications/read-all");
    await loadNotifications();
    toast("Đã đánh dấu tất cả đã đọc", "success");
  } catch { toast("Không thể đánh dấu tất cả", "error"); }
}

async function handleReminderClick(id) {
  await markNotificationAsRead(id);
  window.scrollTo({ top: 400, behavior: "smooth" });
}

async function handleWelcomeShop(id) {
  await markNotificationAsRead(id);
  document.querySelector('.ch-main')?.scrollIntoView({ behavior: "smooth", block: "start" });
}

function viewAllNotifications() {
  notificationsDialog.value = true;
  loadNotifications();
}

function notifIcon(type) {
  return { WELCOME:"🎁", BIRTHDAY:"🎂", PURCHASE_REMINDER:"🛒", WINBACK:"💝", VIP_TIER_UPGRADE:"⭐", SPIN_EXPIRY_WARNING:"🎡" }[type] ?? "🔔";
}

function formatMessage(msg) {
  return msg ? msg.replace(/\n/g, "<br>") : "";
}

function formatDate(d) {
  return new Date(d).toLocaleDateString("vi-VN");
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
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap');

/* ══════════════════════════════════════════════
   CSS VARIABLES
══════════════════════════════════════════════ */
:root {
  --ch-bg:       #faf8f5;
  --ch-surface:  #ffffff;
  --ch-border:   rgba(15,10,5,0.08);
  --ch-text:     #1a1410;
  --ch-muted:    #7a6e65;
  --ch-gold:     #c9922b;
  --ch-gold-lt:  #fdf3e3;
  --ch-rose:     #d94f6e;
  --ch-amber:    #d97706;
  --ch-green:    #1a7a4a;
  --ch-radius:   16px;
  --ch-shadow:   0 2px 16px rgba(15,10,5,0.07), 0 1px 3px rgba(15,10,5,0.05);
  --ch-shadow-lg:0 8px 40px rgba(15,10,5,0.1), 0 2px 8px rgba(15,10,5,0.06);
}

/* ══════════════════════════════════════════════
   ROOT & BG
══════════════════════════════════════════════ */
.ch-root {
  min-height: 100vh;
  background: var(--ch-bg);
  font-family: 'Plus Jakarta Sans', sans-serif;
  color: var(--ch-text);
  position: relative;
}

.ch-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.ch-bg__noise {
  position: absolute;
  inset: 0;
  opacity: 0.025;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  background-size: 200px;
}

.ch-bg__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.12;
}

.ch-bg__orb--1 {
  width: 600px; height: 600px;
  background: radial-gradient(circle, #f59e0b, transparent 65%);
  top: -200px; right: -100px;
  animation: orb-drift 20s ease-in-out infinite alternate;
}

.ch-bg__orb--2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, #ec4899, transparent 65%);
  bottom: -150px; left: -80px;
  animation: orb-drift 25s ease-in-out infinite alternate-reverse;
}

@keyframes orb-drift {
  from { transform: translate(0,0); }
  to   { transform: translate(30px, 40px); }
}

/* ══════════════════════════════════════════════
   WRAP & TIER
══════════════════════════════════════════════ */
.ch-wrap {
  position: relative;
  z-index: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 28px 24px 60px;
}

.ch-tier { margin-bottom: 20px; }

/* ══════════════════════════════════════════════
   BANNERS COMMON
══════════════════════════════════════════════ */
.ch-banners { display: flex; flex-direction: column; gap: 12px; margin-bottom: 20px; }

.banner-list-enter-active { transition: all 0.45s cubic-bezier(0.34, 1.4, 0.64, 1); }
.banner-list-leave-active { transition: all 0.3s ease; }
.banner-list-enter-from   { opacity: 0; transform: translateY(-12px); }
.banner-list-leave-to     { opacity: 0; transform: translateY(-6px); }

/* ── Welcome Banner ── */
.wb-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1e1635 0%, #2d1f5e 50%, #1a1530 100%);
  border: 1px solid rgba(167, 139, 250, 0.2);
  border-radius: 20px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  box-shadow: 0 8px 32px rgba(99,102,241,0.15);
}

.wb-card__glow {
  position: absolute;
  top: -60px; left: -60px;
  width: 200px; height: 200px;
  background: radial-gradient(circle, rgba(167,139,250,0.25), transparent 65%);
  border-radius: 50%;
  pointer-events: none;
}

.wb-card__left { position: relative; flex-shrink: 0; }

.wb-card__emoji {
  font-size: 44px;
  display: block;
  filter: drop-shadow(0 0 16px rgba(251,191,36,0.7));
  animation: emoji-bounce 2.5s ease-in-out infinite;
}

.wb-card__pulse {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  border: 2px solid rgba(251,191,36,0.3);
  animation: pulse-ring 2.5s ease-in-out infinite;
}

@keyframes emoji-bounce {
  0%,100% { transform: translateY(0); }
  50%      { transform: translateY(-5px); }
}

@keyframes pulse-ring {
  0%,100% { transform: scale(1); opacity: 0.5; }
  50%      { transform: scale(1.3); opacity: 0; }
}

.wb-card__body { flex: 1; min-width: 0; color: #fff; }

.wb-card__eyebrow {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(251,191,36,0.85);
  margin-bottom: 5px;
}

.wb-card__title {
  font-family: 'Playfair Display', serif;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 6px;
}

.wb-card__msg {
  font-size: 13.5px;
  color: rgba(255,255,255,0.65);
  line-height: 1.5;
}

.wb-card__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.wb-card__close {
  background: rgba(255,255,255,0.1);
  border: none;
  color: rgba(255,255,255,0.5);
  width: 28px; height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s;
}

.wb-card__close:hover { background: rgba(255,255,255,0.2); }

/* ── Birthday Banner ── */
.bday-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #fff5f7, #ffe8ed);
  border: 1px solid rgba(217, 79, 110, 0.2);
  border-radius: 20px;
  padding: 22px 24px;
  display: flex;
  align-items: flex-start;
  gap: 18px;
  box-shadow: 0 4px 24px rgba(217,79,110,0.1);
}

.confetti-dot {
  position: absolute;
  width: 8px; height: 8px;
  background: var(--c);
  border-radius: 2px;
  top: 10px;
  left: 20%;
  animation: confetti-fall 2s ease-out var(--delay) forwards;
  opacity: 0;
}

@keyframes confetti-fall {
  0%   { opacity: 1; transform: translate(0, 0) rotate(0deg); }
  100% { opacity: 0; transform: translate(var(--x), 60px) rotate(360deg); }
}

.bday-card__icon { font-size: 40px; flex-shrink: 0; animation: emoji-bounce 3s ease-in-out infinite; }

.bday-card__title {
  font-family: 'Playfair Display', serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--ch-rose);
  margin-bottom: 6px;
}

.bday-card__msg { font-size: 13.5px; color: #6b3a4b; line-height: 1.5; margin-bottom: 14px; }
.bday-card__actions { display: flex; gap: 10px; }

/* ── Reminder Banner ── */
.remind-card {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border: 1px solid rgba(217, 119, 6, 0.2);
  border-radius: 20px;
  padding: 18px 22px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(217,119,6,0.08);
}

.remind-card__icon { font-size: 32px; flex-shrink: 0; }
.remind-card__title { font-size: 15px; font-weight: 600; color: #92400e; margin-bottom: 4px; }
.remind-card__msg   { font-size: 13px; color: #a16207; margin-bottom: 12px; }
.remind-card__actions { display: flex; gap: 10px; }

/* ── Spin Expiry Banner ── */
.spin-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  border: 1px solid rgba(99,102,241,0.2);
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(99,102,241,0.12);
}

.spin-card--critical { border-color: rgba(239,68,68,0.35); }
.spin-card--warning  { border-color: rgba(251,191,36,0.35); }

.spin-card__inner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 22px;
  flex-wrap: wrap;
}

.spin-card__wheel { font-size: 32px; animation: spin-slow 6s linear infinite; flex-shrink: 0; }
@keyframes spin-slow { to { transform: rotate(360deg); } }

.spin-card__title { font-size: 14px; font-weight: 600; color: #e2e8f0; }
.spin-card__sub   { font-size: 12.5px; color: rgba(255,255,255,0.5); margin-top: 3px; }
.spin-card__sub strong { color: #fbbf24; }

.spin-card__actions { display: flex; align-items: center; gap: 10px; margin-left: auto; }

.spin-card__close {
  background: rgba(255,255,255,0.08);
  border: none;
  color: rgba(255,255,255,0.4);
  width: 26px; height: 26px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 11px;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s;
}

.spin-card__close:hover { background: rgba(255,255,255,0.15); }

.spin-card__track {
  height: 3px;
  background: rgba(255,255,255,0.07);
}

.spin-card__fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: width 0.5s ease;
}

.spin-card--critical .spin-card__fill { background: linear-gradient(90deg, #ef4444, #f87171); }
.spin-card--warning  .spin-card__fill { background: linear-gradient(90deg, #f59e0b, #fbbf24); }

/* ── Shared close button ── */
.card-close-btn {
  flex-shrink: 0;
  background: rgba(0,0,0,0.06);
  border: none;
  color: rgba(0,0,0,0.35);
  width: 28px; height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s;
}

.card-close-btn:hover { background: rgba(0,0,0,0.12); }

/* ══════════════════════════════════════════════
   PILL BUTTONS
══════════════════════════════════════════════ */
.btn-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 100px;
  border: none;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.15s;
  white-space: nowrap;
}

.btn-pill:hover { transform: translateY(-1px); }
.btn-pill:active { transform: translateY(0); }

.btn-pill--gold    { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; box-shadow: 0 4px 14px rgba(245,158,11,0.3); }
.btn-pill--ghost   { background: rgba(255,255,255,0.15); color: rgba(255,255,255,0.8); backdrop-filter: blur(8px); }
.btn-pill--primary { background: linear-gradient(135deg, #1a7a4a, #16a34a); color: #fff; box-shadow: 0 4px 14px rgba(26,122,74,0.25); }
.btn-pill--rose    { background: linear-gradient(135deg, #d94f6e, #be185d); color: #fff; box-shadow: 0 4px 14px rgba(217,79,110,0.25); }
.btn-pill--amber   { background: linear-gradient(135deg, #d97706, #b45309); color: #fff; box-shadow: 0 4px 14px rgba(217,119,6,0.25); }
.btn-pill--spin    { background: linear-gradient(135deg, #6366f1, #7c3aed); color: #fff; box-shadow: 0 4px 14px rgba(99,102,241,0.3); }

/* light ghost variant */
.btn-pill--ghost-light {
  background: rgba(0,0,0,0.06);
  color: var(--ch-text);
}

/* ══════════════════════════════════════════════
   LAYOUT
══════════════════════════════════════════════ */
.ch-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 900px) {
  .ch-layout { grid-template-columns: 1fr; }
  .ch-sidebar { display: none; }
}

/* ══════════════════════════════════════════════
   SIDEBAR
══════════════════════════════════════════════ */
.sidebar-card {
  background: var(--ch-surface);
  border: 1px solid var(--ch-border);
  border-radius: var(--ch-radius);
  overflow: hidden;
  box-shadow: var(--ch-shadow);
  position: sticky;
  top: 24px;
}

.sidebar-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--ch-border);
}

.sidebar-head__label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--ch-muted);
}

/* Toggle switch */
.toggle-switch { position: relative; display: flex; align-items: center; cursor: pointer; }
.toggle-switch input { position: absolute; opacity: 0; width: 0; height: 0; }
.toggle-switch__track {
  width: 34px; height: 18px;
  background: var(--ch-border);
  border-radius: 100px;
  transition: background 0.2s;
  position: relative;
}
.toggle-switch__track::after {
  content: '';
  position: absolute;
  top: 2px; left: 2px;
  width: 14px; height: 14px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 1px 4px rgba(0,0,0,0.15);
  transition: transform 0.2s;
}
.toggle-switch input:checked ~ .toggle-switch__track { background: var(--ch-gold); }
.toggle-switch input:checked ~ .toggle-switch__track::after { transform: translateX(16px); }

/* Category nav */
.cat-nav { padding: 10px 0; }

.cat-nav__item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: none;
  border: none;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13.5px;
  color: var(--ch-muted);
  cursor: pointer;
  text-align: left;
  transition: background 0.15s, color 0.15s;
  border-radius: 0;
}

.cat-nav__item:hover { background: rgba(201,146,43,0.06); color: var(--ch-text); }

.cat-nav__item.active {
  background: var(--ch-gold-lt);
  color: var(--ch-gold);
  font-weight: 600;
}

.cat-nav__dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: currentColor;
  flex-shrink: 0;
  opacity: 0.4;
  transition: opacity 0.15s;
}

.cat-nav__item.active .cat-nav__dot { opacity: 1; }

.cat-nav__arrow {
  margin-left: auto;
  font-size: 16px;
  opacity: 0;
  transition: opacity 0.15s, transform 0.15s;
}

.cat-nav__item:hover .cat-nav__arrow,
.cat-nav__item.active .cat-nav__arrow { opacity: 1; transform: translateX(2px); }

/* ══════════════════════════════════════════════
   TOOLBAR
══════════════════════════════════════════════ */
.ch-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.ch-toolbar__eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--ch-muted);
  margin-bottom: 4px;
}

.ch-toolbar__title {
  font-family: 'Playfair Display', serif;
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 3px;
  line-height: 1.2;
}

.ch-toolbar__meta { font-size: 12.5px; color: var(--ch-muted); }
.ch-toolbar__meta em { font-style: italic; color: var(--ch-gold); }

.ch-toolbar__right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* Icon btn (reload) */
.icon-btn {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: var(--ch-surface);
  border: 1px solid var(--ch-border);
  color: var(--ch-muted);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}

.icon-btn:hover { color: var(--ch-text); border-color: rgba(15,10,5,0.15); }
.icon-btn.spinning svg { animation: spin-icon 0.7s linear infinite; }
@keyframes spin-icon { to { transform: rotate(360deg); } }

/* Toolbar buttons */
.toolbar-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 10px;
  border: 1px solid var(--ch-border);
  background: var(--ch-surface);
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: var(--ch-text);
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, box-shadow 0.15s, transform 0.1s;
  white-space: nowrap;
}

.toolbar-btn:hover { background: #f5f0eb; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(15,10,5,0.08); }

.toolbar-btn--event {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  border-color: rgba(245,158,11,0.3);
  color: #92400e;
}

.toolbar-btn--cart {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  border-color: rgba(16,185,129,0.3);
  color: #065f46;
}

.toolbar-btn--notif { color: var(--ch-amber); }

.event-ping {
  position: absolute;
  top: -3px; right: -3px;
  width: 8px; height: 8px;
  background: #ef4444;
  border-radius: 50%;
  animation: ping-anim 1.5s ease-in-out infinite;
}

@keyframes ping-anim {
  0%,100% { transform: scale(1); opacity: 1; }
  50%      { transform: scale(1.8); opacity: 0; }
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

/* ── Divider ── */
.ch-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--ch-border) 20%, var(--ch-border) 80%, transparent);
  margin: 20px 0;
}

/* ══════════════════════════════════════════════
   SKELETON
══════════════════════════════════════════════ */
.product-skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.skeleton-card {
  background: var(--ch-surface);
  border-radius: var(--ch-radius);
  padding: 16px;
  border: 1px solid var(--ch-border);
}

.skeleton-img {
  width: 100%; height: 170px;
  background: linear-gradient(90deg, #f0ebe4 25%, #e8e1d8 50%, #f0ebe4 75%);
  background-size: 200%;
  animation: shimmer 1.5s infinite;
  border-radius: 10px;
  margin-bottom: 14px;
}

.skeleton-line {
  height: 12px;
  background: linear-gradient(90deg, #f0ebe4 25%, #e8e1d8 50%, #f0ebe4 75%);
  background-size: 200%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
  margin-bottom: 8px;
}

.skeleton-line--long  { width: 75%; }
.skeleton-line--short { width: 50%; }
.skeleton-line--price { width: 40%; margin-top: 12px; height: 16px; }

@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ══════════════════════════════════════════════
   EMPTY STATE
══════════════════════════════════════════════ */
.ch-empty {
  padding: 60px 20px;
  text-align: center;
  color: var(--ch-muted);
}

.ch-empty__icon { font-size: 48px; margin-bottom: 12px; opacity: 0.5; }
.ch-empty__title { font-size: 16px; font-weight: 600; margin-bottom: 6px; color: var(--ch-text); }
.ch-empty__sub   { font-size: 13px; }

/* ══════════════════════════════════════════════
   PRODUCT GRID
══════════════════════════════════════════════ */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.product-card {
  background: var(--ch-surface);
  border: 1px solid var(--ch-border);
  border-radius: var(--ch-radius);
  overflow: hidden;
  box-shadow: var(--ch-shadow);
  display: flex;
  flex-direction: column;
  transition: transform 0.25s cubic-bezier(0.34, 1.4, 0.64, 1), box-shadow 0.25s;
  animation: card-in 0.5s cubic-bezier(0.34, 1.2, 0.64, 1) both;
  animation-delay: var(--delay, 0s);
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--ch-shadow-lg);
}

.product-card__img-wrap {
  position: relative;
  overflow: hidden;
  background: #f5f1eb;
}

.product-card__img {
  width: 100%;
  height: 185px;
  object-fit: cover;
  display: block;
  transition: transform 0.4s ease;
}

.product-card:hover .product-card__img { transform: scale(1.04); }

.product-card__new-badge {
  position: absolute;
  top: 10px; left: 10px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 0.1em;
  padding: 3px 8px;
  border-radius: 100px;
  box-shadow: 0 2px 8px rgba(239,68,68,0.35);
}

.product-card__quick-add {
  position: absolute;
  bottom: -44px;
  left: 0; right: 0;
  background: rgba(15,10,5,0.88);
  color: white;
  border: none;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  transition: bottom 0.25s cubic-bezier(0.34, 1.2, 0.64, 1);
  backdrop-filter: blur(4px);
}

.product-card:hover .product-card__quick-add { bottom: 0; }
.product-card__quick-add:disabled { opacity: 0.4; cursor: not-allowed; }

.product-card__body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-card__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ch-text);
  margin-bottom: 5px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.product-card__desc {
  font-size: 12px;
  color: var(--ch-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  flex: 1;
}

.product-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--ch-border);
}

.product-card__price {
  font-size: 14px;
  font-weight: 700;
  color: var(--ch-rose);
  font-variant-numeric: tabular-nums;
}

.product-card__order-btn {
  padding: 5px 14px;
  background: var(--ch-text);
  color: white;
  border: none;
  border-radius: 8px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
}

.product-card__order-btn:hover   { background: #3b2a1a; transform: scale(1.03); }
.product-card__order-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }

/* ══════════════════════════════════════════════
   PAGINATION
══════════════════════════════════════════════ */
.ch-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
}

.page-btn {
  width: 38px; height: 38px;
  border-radius: 10px;
  background: var(--ch-surface);
  border: 1px solid var(--ch-border);
  font-size: 18px;
  color: var(--ch-text);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s, border-color 0.15s, transform 0.1s;
}

.page-btn:hover:not(:disabled) { background: var(--ch-gold-lt); border-color: var(--ch-gold); transform: scale(1.05); }
.page-btn:disabled { opacity: 0.35; cursor: not-allowed; }

.page-info {
  font-size: 13px;
  font-weight: 600;
  color: var(--ch-muted);
  min-width: 60px;
  text-align: center;
}

/* ══════════════════════════════════════════════
   NOTIFICATION MODAL
══════════════════════════════════════════════ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15,10,5,0.45);
  backdrop-filter: blur(6px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.modal-fade-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.2, 0.64, 1); }
.modal-fade-leave-active { transition: all 0.2s ease; }
.modal-fade-enter-from  { opacity: 0; }
.modal-fade-leave-to    { opacity: 0; }

.notif-modal {
  background: var(--ch-surface);
  border-radius: 24px;
  width: 100%;
  max-width: 560px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 24px 80px rgba(15,10,5,0.18);
  overflow: hidden;
  animation: modal-in 0.3s cubic-bezier(0.34, 1.3, 0.64, 1);
}

@keyframes modal-in {
  from { transform: scale(0.95) translateY(10px); opacity: 0; }
  to   { transform: scale(1) translateY(0); opacity: 1; }
}

.notif-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 24px 18px;
  border-bottom: 1px solid var(--ch-border);
}

.notif-modal__title {
  font-family: 'Playfair Display', serif;
  font-size: 20px;
  font-weight: 600;
}

.notif-modal__close {
  width: 32px; height: 32px;
  background: rgba(0,0,0,0.06);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 13px;
  color: var(--ch-muted);
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s;
}

.notif-modal__close:hover { background: rgba(0,0,0,0.12); }

.notif-modal__body { flex: 1; overflow-y: auto; padding: 16px 24px; }

.notif-empty {
  padding: 40px;
  text-align: center;
  color: var(--ch-muted);
}

.notif-empty__icon { font-size: 40px; margin-bottom: 10px; opacity: 0.4; }

.notif-list { display: flex; flex-direction: column; gap: 2px; }

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 12px;
  border-radius: 12px;
  transition: background 0.15s;
}
.toolbar-btn--security {
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  border-color: rgba(99, 102, 241, 0.3);
  color: #3730a3;
}

.notif-item:hover { background: #f9f6f2; }

.notif-item--unread { background: var(--ch-gold-lt); }
.notif-item--unread:hover { background: #faecd5; }

.notif-item__icon { font-size: 24px; flex-shrink: 0; margin-top: 1px; }

.notif-item__title { font-size: 14px; font-weight: 600; margin-bottom: 3px; }
.notif-item__msg   { font-size: 13px; color: var(--ch-muted); line-height: 1.5; margin-bottom: 4px; }
.notif-item__time  { font-size: 11.5px; color: rgba(122,110,101,0.6); }

.notif-item__read-btn {
  flex-shrink: 0;
  margin-left: auto;
  width: 28px; height: 28px;
  background: white;
  border: 1px solid var(--ch-border);
  border-radius: 50%;
  font-size: 13px;
  color: var(--ch-green);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s, border-color 0.15s;
}

.notif-item__read-btn:hover { background: var(--ch-green); color: white; border-color: var(--ch-green); }

.notif-modal__foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--ch-border);
}

.notif-modal__foot .btn-pill--ghost {
  background: rgba(0,0,0,0.06);
  color: var(--ch-text);
}
</style>