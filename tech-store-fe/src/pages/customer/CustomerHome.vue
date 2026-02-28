<template>
  <div class="container-xl">
    <TierProgressBar 
      v-if="isCustomer" 
      ref="tierProgressRef"
      class="mb-4"
    />

    <!-- ✨ WELCOME Banner -->
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
            </div>
          </div>
          <el-icon class="close-icon" @click="markNotificationAsRead(notif.id)" :size="20">
            <Close />
          </el-icon>
        </div>
      </el-card>
    </div>

    <!-- Spin Expiry Warning Banners -->
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
          </div>
          <div class="spin-expiry-banner__text">
            <div class="spin-expiry-banner__title">Ưu đãi sắp hết hạn!</div>
            <div class="spin-expiry-banner__body">
              Giảm <span class="spin-expiry-banner__discount">{{ bonus.discountBonus }}%</span> — 
              còn <b>{{ bonus.hoursLeft }} giờ</b>!
            </div>
          </div>
          <div class="spin-expiry-banner__actions">
            <button class="spin-btn-use-now" @click="handleSpinExpiryUseNow(bonus)">Dùng ngay</button>
            <button class="spin-btn-dismiss" @click="dismissSpinExpiry(bonus.customerId)">✕</button>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-3">
      <!-- SIDEBAR CATEGORIES -->
      <div class="col-12 col-lg-3">
        <el-card shadow="never" class="category-card">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <div>
              <div class="kicker">Danh mục</div>
              <div class="title">Tìm kiếm</div>
            </div>
            <el-switch v-model="activeOnly" @change="reloadAll" />
          </div>
          <el-divider class="my-2" />
          <el-menu :default-active="String(activeKey)" class="border-0 custom-menu" @select="onSelectCategory">
            <el-menu-item index="all">
              <el-icon><Menu /></el-icon>
              <span>Tất cả sản phẩm</span>
            </el-menu-item>
            <el-menu-item v-for="c in categories" :key="c.id" :index="String(c.id)">
              <el-icon><ArrowRight /></el-icon>
              <span>{{ c.name }}</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </div>

      <!-- PRODUCT LIST -->
      <div class="col-12 col-lg-9">
        <el-card shadow="never">
          <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap mb-3">
            <div>
              <div class="kicker">Sản phẩm</div>
              <div class="title text-primary">{{ titleText }}</div>
              <div class="muted">
                Trang: <b>{{ page + 1 }}</b>
                <span v-if="searchTerm"> • Tìm kiếm: <b>{{ searchTerm }}</b></span>
              </div>
            </div>

            <div class="d-flex align-items-center gap-2 flex-wrap">
              <el-button :loading="loading" icon="Refresh" @click="reloadProducts">Tải lại</el-button>
              
              <button v-if="isCustomer" class="event-btn" @click="$router.push('/spin-wheel')">
                <span class="event-icon">🎡</span>
                <span>Sự kiện</span>
                <span class="event-ping"></span>
              </button>

              <el-badge v-if="isCustomer" :value="unreadNotificationCount" :hidden="unreadNotificationCount === 0">
                <el-button type="warning" plain icon="Bell" @click="viewAllNotifications">Thông báo</el-button>
              </el-badge>

              <el-badge v-if="isCustomer" :value="cartStore.count">
                <el-button type="success" plain @click="$router.push('/cart')">🛒 Giỏ hàng</el-button>
              </el-badge>

              <el-button type="primary" :disabled="!isCustomer" @click="$router.push('/orders/new')">
                Tạo đơn hàng
              </el-button>
            </div>
          </div>

          <el-divider class="my-3" />

          <el-skeleton v-if="loading" :rows="6" animated />
          <template v-else>
            <el-empty v-if="filteredProducts.length === 0" description="Không có sản phẩm nào" />
            <div v-else class="row g-3">
              <div v-for="p in pagedClientProducts" :key="p.id" class="col-12 col-sm-6 col-xl-4">
                <el-card shadow="hover" class="product-card h-100">
                  <div class="product-thumb">
                    <img :src="p.imageUrl" :alt="p.name" />
                    <div class="product-tag">Laptop</div>
                  </div>
                  <div class="product-body p-3">
                    <div class="fw-bold text-truncate mb-1" :title="p.name">{{ p.name }}</div>
                    <div class="product-desc text-muted mb-3">{{ p.description || "Chưa có mô tả" }}</div>
                    <div class="d-flex align-items-center justify-content-between">
                      <div class="product-price text-danger fw-bold">{{ p.priceText }}</div>
                      <el-button 
                        type="primary" 
                        size="small"
                        :disabled="!isCustomer || !p.defaultVariantId" 
                        @click="goOrder(p)"
                      >Mua ngay</el-button>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>

            <div class="d-flex justify-content-center mt-4">
              <el-pagination 
                background 
                layout="prev, pager, next" 
                :page-size="clientPageSize"
                :total="filteredProducts.length" 
                v-model:current-page="clientPage" 
              />
            </div>
          </template>
        </el-card>
      </div>
    </div>

    <!-- Notifications Dialog -->
    <el-dialog v-model="notificationsDialog" title="🔔 Thông báo của bạn" width="550px">
      <div v-if="allNotifications.length === 0" class="text-center py-4">
        <el-empty description="Không có thông báo" />
      </div>
      <div v-else class="notifications-list">
        <div v-for="notif in allNotifications" :key="notif.id" class="notification-item" :class="{ 'unread': !notif.isRead }">
          <div class="d-flex align-items-start gap-3">
            <div class="notif-icon">{{ notifIcon(notif.type) }}</div>
            <div class="flex-grow-1">
              <h6 class="mb-1 fw-bold">{{ notif.title }}</h6>
              <p class="mb-1 small" v-html="formatMessage(notif.message)"></p>
              <small class="text-muted">{{ formatDate(notif.createdAt) }}</small>
            </div>
            <el-button v-if="!notif.isRead" size="small" link type="primary" @click="markNotificationAsRead(notif.id)">Đọc</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { categoriesApi } from "../../api/categories.api";
import { productsApi } from "../../api/products.api";
import { useAuthStore } from "../../stores/auth";
import { toast } from "../../ui/toast";
import { Menu, ArrowRight, Bell, Close } from '@element-plus/icons-vue';
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
const searchTerm = ref("");
const clientPage = ref(1);
const clientPageSize = 9;

const welcomeNotifications = ref([]);
const birthdayNotifications = ref([]);
const allNotifications = ref([]);
const unreadNotificationCount = ref(0);
const notificationsDialog = ref(false);
const tierProgressRef = ref(null);
const spinExpiryBonuses = ref([]);

// --- Helper: URL ảnh ---
function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/300?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
}

// --- Logic Sản phẩm ---
function normalizeProducts(list) {
  return (list || []).map(p => ({
    id: p.id,
    name: p.name,
    description: p.description,
    imageUrl: fixImageUrl(p.imageUrl),
    priceText: p.minPrice ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(p.minPrice) : "Liên hệ",
    defaultVariantId: p.defaultVariantId || p.variants?.[0]?.id,
    raw: p
  }));
}

const titleText = computed(() => {
  if (!categoryId.value) return "Tất cả sản phẩm";
  return categories.value.find(c => String(c.id) === String(categoryId.value))?.name || "Danh mục";
});

const filteredProducts = computed(() => {
  const kw = (searchTerm.value || "").trim().toLowerCase();
  return products.value.filter(p => p.name.toLowerCase().includes(kw));
});

const pagedClientProducts = computed(() => {
  const start = (clientPage.value - 1) * clientPageSize;
  return filteredProducts.value.slice(start, start + clientPageSize);
});

// --- API Calls ---
async function reloadCategories() {
  try {
    const res = await categoriesApi.list(activeOnly.value);
    const rawList = res.data?.data || res.data || [];
    // Loại bỏ các danh mục trùng tên nếu có
    const unique = [];
    const names = new Set();
    rawList.forEach(c => {
      if(!names.has(c.name)) {
        unique.push({ id: c.id, name: c.name });
        names.add(c.name);
      }
    });
    categories.value = unique;
  } catch (e) { toast("Lỗi tải danh mục", "error"); }
}

async function reloadProducts() {
  loading.value = true;
  try {
    const params = { 
      page: page.value,
      // QUAN TRỌNG: Đổi tên thành categoryIds để khớp với Backend
      categoryIds: categoryId.value ? String(categoryId.value) : undefined,
      isVisible: true // Chỉ hiện sản phẩm đang Active với khách
    };
    
    const res = await productsApi.list(params);
    const data = res.data?.data || res.data;
    products.value = normalizeProducts(data.content || []);
  } catch (e) { toast("Lỗi tải sản phẩm", "error"); }
  finally { loading.value = false; }
}

async function reloadAll() {
  await reloadCategories();
  await reloadProducts();
}

function onSelectCategory(key) {
  activeKey.value = key;
  categoryId.value = key === "all" ? null : Number(key);
  clientPage.value = 1;
  reloadProducts();
}

// --- Notifications & Events ---
async function loadNotifications() {
  if (!isCustomer.value) return;
  try {
    const res = await http.get("/api/auth/notifications/my?unreadOnly=true");
    const list = res.data || [];
    welcomeNotifications.value = list.filter(n => n.type === "WELCOME");
    birthdayNotifications.value = list.filter(n => n.type === "BIRTHDAY");
    const countRes = await http.get("/api/auth/notifications/my/unread-count");
    unreadNotificationCount.value = countRes.data?.unreadCount || 0;
  } catch (e) {}
}

async function markNotificationAsRead(id) {
  await http.put(`/api/auth/notifications/${id}/read`);
  loadNotifications();
}

function viewAllNotifications() {
  notificationsDialog.value = true;
  http.get("/api/auth/notifications/my").then(r => allNotifications.value = r.data || []);
}

onMounted(async () => {
  await reloadAll();
  if (isCustomer.value) {
    cartStore.refreshCount();
    loadNotifications();
  }
});

// Handle search event from header
window.addEventListener("products:search", (e) => {
  searchTerm.value = e.detail || "";
  clientPage.value = 1;
});
</script>

<style scoped>
.kicker { font-size: 11px; font-weight: 800; color: #64748b; text-transform: uppercase; }
.title { font-weight: 800; font-size: 18px; color: #1e293b; }
.muted { font-size: 12px; color: #94a3b8; }

.category-card { border-radius: 12px; }
.custom-menu .el-menu-item { height: 45px; line-height: 45px; border-radius: 8px; margin-bottom: 4px; }
.custom-menu .el-menu-item.is-active { background-color: #f1f5f9; font-weight: bold; }

.product-card { border-radius: 12px; overflow: hidden; transition: transform 0.2s; border: 1px solid #e2e8f0; }
.product-card:hover { transform: translateY(-4px); }
.product-thumb { position: relative; height: 180px; background: #f8fafc; }
.product-thumb img { width: 100%; height: 100%; object-fit: cover; }
.product-tag { position: absolute; top: 10px; right: 10px; background: #fff; padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: bold; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.product-desc { font-size: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 36px; }

.event-btn { background: linear-gradient(135deg, #f59e0b, #ef4444); color: #fff; border: none; padding: 6px 14px; border-radius: 8px; font-weight: 600; cursor: pointer; position: relative; }
.event-ping { position: absolute; top: -2px; right: -2px; width: 8px; height: 8px; background: #fff; border-radius: 50%; animation: ping 1.5s infinite; }

@keyframes ping { 0% { transform: scale(1); opacity: 1; } 100% { transform: scale(2); opacity: 0; } }
.notification-item { padding: 12px; border-bottom: 1px solid #f1f5f9; }
.notification-item.unread { background: #f0f9ff; }
</style>