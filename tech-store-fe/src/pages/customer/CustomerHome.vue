<!-- FILE: src/pages/customer/CustomerHome.vue -->
<template>
  <div class="container-xl">
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
              <el-button 
                type="primary" 
                size="large"
                @click="markNotificationAsRead(notif.id)"
              >
                Cảm ơn! 🎉
              </el-button>
              <el-button 
                size="large"
                @click="viewAllNotifications"
              >
                Xem tất cả thông báo
              </el-button>
            </div>
          </div>
          <el-icon 
            class="close-icon" 
            @click="markNotificationAsRead(notif.id)"
            :size="20"
          >
            <Close />
          </el-icon>
        </div>
      </el-card>
    </div>

    <div class="row g-3">
      <div class="col-12 col-lg-3">
        <el-card shadow="never">
          <div class="d-flex align-items-center justify-content-between">
            <div>
              <div class="kicker">Categories</div>
              <div class="title">Browse</div>
            </div>
            <el-switch
              v-model="activeOnly"
              active-text="Active only"
              @change="reloadAll"
            />
          </div>

          <el-divider />

          <el-menu
            :default-active="String(activeKey)"
            class="w-100"
            @select="onSelectCategory"
          >
            <el-menu-item index="all">All</el-menu-item>
            <el-menu-item
              v-for="c in categories"
              :key="c.id"
              :index="String(c.id)"
            >
              {{ c.name }}
            </el-menu-item>
          </el-menu>
        </el-card>
      </div>

      <div class="col-12 col-lg-9">
        <el-card shadow="never">
          <div
            class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
          >
            <div>
              <div class="kicker">Products</div>
              <div class="title">{{ titleText }}</div>
              <div class="muted">
                Page: <b>{{ page + 1 }}</b>
                <span v-if="searchTerm">
                  • Search: <b>{{ searchTerm }}</b></span
                >
              </div>
            </div>

            <div class="d-flex align-items-center gap-2 flex-wrap">
              <el-button :loading="loading" @click="reloadProducts"
                >Reload</el-button
              >
              <el-button
                v-if="isCustomer"
                type="info"
                @click="$router.push('/profile')"
              >
                <el-icon class="me-1"><User /></el-icon>
                Profile
              </el-button>

              <!-- Notification Badge -->
              <el-badge
                v-if="isCustomer"
                :value="unreadNotificationCount"
                :hidden="unreadNotificationCount === 0"
                class="me-1"
              >
                <el-button
                  type="warning"
                  @click="viewAllNotifications"
                >
                  <el-icon class="me-1"><Bell /></el-icon>
                  Thông báo
                </el-button>
              </el-badge>

              <el-badge
                v-if="isCustomer"
                :value="cartStore.count"
                class="me-1"
              >
                <el-button
                  type="success"
                  @click="$router.push('/cart')"
                >
                  Cart
                </el-button>
              </el-badge>

              <el-button
                type="primary"
                :disabled="!isCustomer"
                @click="$router.push('/orders/new')"
              >
                Create Order
              </el-button>
            </div>
          </div>

          <el-divider />

          <el-skeleton v-if="loading" :rows="6" animated />
          <template v-else>
            <el-empty
              v-if="filteredProducts.length === 0"
              description="No products"
            />

            <div v-else class="row g-3">
              <div
                v-for="p in pagedClientProducts"
                :key="p.id"
                class="col-12 col-sm-6 col-xl-4"
              >
                <el-card shadow="hover" class="h-100">
                  <template #header>
                    <div
                      class="d-flex align-items-center justify-content-between"
                    >
                      <span
                        class="fw-bold text-truncate"
                        style="max-width: 220px"
                        :title="p.name"
                        >{{ p.name }}</span
                      >
                      <el-tag size="small" effect="light">Laptop</el-tag>
                    </div>
                  </template>

                  <div class="thumb">
                    <img :src="p.imageUrl" :alt="p.name" />
                  </div>

                  <div class="mt-2 muted" style="min-height: 38px">
                    {{ p.description || "—" }}
                  </div>

                  <div
                    class="mt-3 d-flex align-items-center justify-content-between"
                  >
                    <div class="fw-bold">{{ p.priceText }}</div>
                    <el-button
                      size="small"
                      type="primary"
                      :disabled="!isCustomer || !p.defaultVariantId"
                      @click="goOrder(p)"
                    >
                      Order
                    </el-button>
                  </div>

                  <div v-if="!p.defaultVariantId" class="mt-2 muted small">
                    (No variantId found in product payload)
                  </div>
                </el-card>
              </div>
            </div>

            <div class="d-flex justify-content-end mt-3">
              <el-pagination
                background
                layout="prev, pager, next"
                :page-size="clientPageSize"
                :total="filteredProducts.length"
                :current-page="clientPage"
                @current-change="(v) => (clientPage = v)"
              />
            </div>

            <div class="d-flex justify-content-center mt-2">
              <el-pagination
                background
                layout="prev, pager, next"
                :page-size="1"
                :total="Math.max(1, serverTotalPages)"
                :current-page="page + 1"
                @current-change="onServerPageChange"
              />
            </div>
          </template>
        </el-card>
      </div>
    </div>

    <!-- Notifications Dialog -->
    <el-dialog 
      v-model="notificationsDialog" 
      title="🔔 Thông báo của bạn"
      width="600px"
      @close="loadNotifications"
    >
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
            <div class="notif-icon">{{ notif.icon }}</div>
            <div class="flex-grow-1">
              <h5 class="mb-1">{{ notif.title }}</h5>
              <p class="mb-1" v-html="formatMessage(notif.message)"></p>
              <small class="text-muted">{{ formatDate(notif.createdAt) }}</small>
            </div>
            <el-button 
              v-if="!notif.isRead"
              size="small" 
              type="primary"
              @click="markNotificationAsRead(notif.id)"
            >
              Đánh dấu đã đọc
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="notificationsDialog = false">Đóng</el-button>
        <el-button type="primary" @click="markAllAsRead">
          Đánh dấu tất cả đã đọc
        </el-button>
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

// Notification states
const birthdayNotifications = ref([]);
const allNotifications = ref([]);
const unreadNotificationCount = ref(0);
const notificationsDialog = ref(false);

function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  const root = payload?.data ?? payload;
  if (Array.isArray(root)) return root;
  const candidates = [
    root,
    root?.data,
    root?.data?.data,
    payload?.data,
    payload?.data?.data,
  ].filter(Boolean);
  for (const c of candidates) {
    if (Array.isArray(c)) return c;
    for (const k of ["content", "items", "results", "rows", "list"]) {
      if (Array.isArray(c?.[k])) return c[k];
    }
  }
  return [];
}

function pickTotalPages(payload) {
  const root = payload?.data ?? payload;
  const totalPages =
    root?.totalPages ??
    root?.data?.totalPages ??
    payload?.totalPages ??
    payload?.data?.totalPages ??
    null;
  return typeof totalPages === "number" && totalPages > 0 ? totalPages : 1;
}

function getDefaultVariantId(raw) {
  const v =
    raw?.defaultVariantId ??
    raw?.variantId ??
    raw?.variants?.[0]?.id ??
    raw?.variants?.[0]?.variantId ??
    raw?.variant?.id ??
    null;
  return v != null ? Number(v) : null;
}

function normalizeProducts(list) {
  return (list || []).map((p, idx) => {
    const id = p?.id ?? p?.productId ?? idx + 1;
    const name = p?.name ?? p?.title ?? `Product ${id}`;
    const description = p?.description ?? p?.shortDescription ?? "";
    const imageUrl =
      p?.imageUrl ||
      p?.thumbnailUrl ||
      `https://placehold.co/640x420/png?text=${encodeURIComponent(name)}`;

    const price = p?.finalPrice ?? p?.price ?? p?.minPrice ?? null;

    const priceText =
      typeof price === "number"
        ? new Intl.NumberFormat("vi-VN", {
            style: "currency",
            currency: "VND",
          }).format(price)
        : price != null
          ? String(price)
          : "—";

    return {
      id,
      name,
      description,
      imageUrl,
      priceText,
      defaultVariantId: getDefaultVariantId(p),
      raw: p,
    };
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
  const c = categories.value.find(
    (x) => String(x.id) === String(categoryId.value)
  );
  return c?.name || `Category #${categoryId.value}`;
});

const filteredProducts = computed(() => {
  const kw = (searchTerm.value || "").trim().toLowerCase();
  const list = products.value || [];
  if (!kw) return list;
  return list.filter((p) =>
    `${p.name} ${p.description || ""}`.toLowerCase().includes(kw)
  );
});

const pagedClientProducts = computed(() => {
  const start = (clientPage.value - 1) * clientPageSize;
  return filteredProducts.value.slice(start, start + clientPageSize);
});

function onSelectCategory(key) {
  activeKey.value = key;
  categoryId.value = key === "all" ? null : Number(key);
  page.value = 0;
  clientPage.value = 1;
  reloadProducts();
}

async function goOrder(p) {
  try {
    await cartStore.addToCart(p.defaultVariantId, 1);
    toast("Đã thêm vào giỏ hàng", "success");
  } catch (e) {
    toast("Không thể thêm vào giỏ hàng", "error");
    console.error(e);
  }
}

async function reloadCategories() {
  try {
    const res = await categoriesApi.list(activeOnly.value);
    categories.value = normalizeCategories(extractList(res?.data));
  } catch (e) {
    categories.value = [];
    toast("Failed to load categories.", "error");
  }
}

async function reloadProducts() {
  loading.value = true;
  try {
    const res = await productsApi.list({
      page: page.value,
      categoryId: categoryId.value ?? undefined,
    });

    serverTotalPages.value = pickTotalPages(res?.data);
    const list = extractList(res?.data);
    products.value = normalizeProducts(list);
  } catch (e) {
    products.value = [];
    serverTotalPages.value = 1;
    toast("Failed to load products.", "error");
  } finally {
    loading.value = false;
  }
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

// Notification functions
async function loadNotifications() {
  if (!isCustomer.value) return;
  
  try {
    // Load unread birthday notifications
    const unreadRes = await http.get("/api/auth/notifications/my?unreadOnly=true");
    const unreadNotifs = unreadRes.data || [];
    birthdayNotifications.value = unreadNotifs.filter(n => n.type === 'BIRTHDAY');
    
    // Load count
    const countRes = await http.get("/api/auth/notifications/my/unread-count");
    unreadNotificationCount.value = countRes.data?.unreadCount || 0;
    
    // Load all notifications if dialog is open
    if (notificationsDialog.value) {
      const allRes = await http.get("/api/auth/notifications/my");
      allNotifications.value = allRes.data || [];
    }
  } catch (error) {
    console.error("Load notifications error:", error);
  }
}

async function markNotificationAsRead(notificationId) {
  try {
    await http.put(`/api/auth/notifications/${notificationId}/read`);
    await loadNotifications();
    toast("Đã đánh dấu đã đọc", "success");
  } catch (error) {
    console.error("Mark as read error:", error);
    toast("Không thể đánh dấu đã đọc", "error");
  }
}

async function markAllAsRead() {
  try {
    await http.put("/api/auth/notifications/read-all");
    await loadNotifications();
    toast("Đã đánh dấu tất cả đã đọc", "success");
  } catch (error) {
    console.error("Mark all as read error:", error);
    toast("Không thể đánh dấu tất cả", "error");
  }
}

function viewAllNotifications() {
  notificationsDialog.value = true;
  loadNotifications();
}

function formatMessage(message) {
  return message.replace(/\n/g, '<br>');
}

function formatDate(dateString) {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'Vừa xong';
  if (diffMins < 60) return `${diffMins} phút trước`;
  if (diffHours < 24) return `${diffHours} giờ trước`;
  if (diffDays < 7) return `${diffDays} ngày trước`;
  
  return date.toLocaleDateString('vi-VN');
}

// receive search from App.vue
function onSearchEvent(e) {
  searchTerm.value = String(e?.detail || "");
  clientPage.value = 1;
}

onMounted(async () => {
  window.addEventListener("products:search", onSearchEvent);
  await reloadAll();
  
  // Load notifications and cart
  if (isCustomer.value) {
    cartStore.refreshCount();
    loadNotifications();
    
    // Auto refresh notifications every 30 seconds
    const notifInterval = setInterval(loadNotifications, 30000);
    
    // Cleanup on unmount
    onBeforeUnmount(() => {
      clearInterval(notifInterval);
    });
  }
});

onBeforeUnmount(() => {
  window.removeEventListener("products:search", onSearchEvent);
});
</script>

<style scoped>
.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
}
.title {
  font-weight: 900;
  font-size: 18px;
}
.muted {
  color: rgba(15, 23, 42, 0.62);
  font-size: 13px;
}
.thumb {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.1);
  background: rgba(2, 6, 23, 0.03);
}
.thumb img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}
.small {
  font-size: 12px;
}

/* Birthday Notification Styles */
.birthday-notification-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  animation: slideIn 0.5s ease-out;
}

.birthday-notification-card :deep(.el-card__body) {
  padding: 24px;
}

.birthday-icon-large {
  font-size: 64px;
  line-height: 1;
  animation: bounce 2s infinite;
}

.birthday-message {
  font-size: 16px;
  line-height: 1.6;
  white-space: pre-line;
}

.close-icon {
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.3s;
}

.close-icon:hover {
  opacity: 1;
}

@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* Notifications Dialog Styles */
.notifications-list {
  max-height: 500px;
  overflow-y: auto;
}

.notification-item {
  padding: 16px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
  border-left: 3px solid #409eff;
}

.notif-icon {
  font-size: 32px;
  line-height: 1;
}
</style>