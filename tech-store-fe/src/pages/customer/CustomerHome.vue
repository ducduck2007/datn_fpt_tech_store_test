<!-- FILE: src/pages/customer/CustomerHome.vue -->
<template>
  <div class="container-xl">
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
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { categoriesApi } from "../../api/categories.api";
import { productsApi } from "../../api/products.api";
import { useAuthStore } from "../../stores/auth";
import { toast } from "../../ui/toast";
import { User } from '@element-plus/icons-vue';
import { useCartStore } from "../../stores/cart";

const auth = useAuthStore();
const isCustomer = computed(() => auth.isCustomer);

const loading = ref(false);
const activeOnly = ref(true);

const categories = ref([]);
const products = ref([]);

const activeKey = ref("all");
const categoryId = ref(null);

const page = ref(0); // server page (0-based)
const serverTotalPages = ref(1); // unknown; fallback

const searchTerm = ref("");
const clientPage = ref(1);
const clientPageSize = 9;

const cartStore = useCartStore();

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



function routerPush(path) {
  // avoid importing router directly (keep page self-contained)
  window.history.pushState({}, "", path);
  window.dispatchEvent(new PopStateEvent("popstate"));
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

// receive search from App.vue
function onSearchEvent(e) {
  searchTerm.value = String(e?.detail || "");
  clientPage.value = 1;
}

onMounted(async () => {
  window.addEventListener("products:search", onSearchEvent);
  await reloadAll();
});

onBeforeUnmount(() => {
  window.removeEventListener("products:search", onSearchEvent);
});

onMounted(() => {
  // ADD: load badge khi vào trang
  cartStore.refreshCount();
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
</style>