<!-- \retail-fe\src\pages\customer\CustomerHome.vue -->
<template>
  <div class="landing">
    <!-- HERO -->
    <section class="hero">
      <el-row :gutter="16">
        <el-col :xs="24" :md="7">
          <el-card class="hero-card" shadow="hover">
            <div class="menu-hdr">
              <div>
                <div class="kicker">Danh mục</div>
                <div class="title">Chọn nhanh</div>
              </div>

              <el-button size="small" plain @click="reloadConsole"
                >Log console</el-button
              >
            </div>

            <el-menu
              class="cat-menu"
              :default-active="String(activeMenu)"
              @select="onSelectCategory"
            >
              <el-menu-item index="all">Tất cả</el-menu-item>
              <el-menu-item
                v-for="c in categories"
                :key="c.id"
                :index="String(c.id)"
              >
                {{ c.name }}
              </el-menu-item>
            </el-menu>

            <div class="mini-note">
              <div class="muted">
                * Trang demo sẽ tự gọi API:
                <b>/api/categories</b> và <b>/api/products</b>. Nếu backend
                thiếu GET sẽ fallback demo data.
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="17">
          <el-card class="hero-card hero-right" shadow="hover">
            <el-carousel height="290px" indicator-position="outside">
              <el-carousel-item v-for="s in slides" :key="s.title">
                <div
                  class="slide"
                  :style="{ backgroundImage: `url(${s.img})` }"
                >
                  <div class="overlay">
                    <div class="slide-kicker">{{ s.kicker }}</div>
                    <div class="slide-title">{{ s.title }}</div>
                    <div class="slide-desc">{{ s.desc }}</div>

                    <div class="slide-actions">
                      <el-button type="primary" @click="scrollTo('featured')"
                        >Xem sản phẩm</el-button
                      >
                      <el-button plain @click="scrollTo('benefits')"
                        >Dịch vụ</el-button
                      >
                    </div>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </el-card>
        </el-col>
      </el-row>

      <!-- Trust Bar -->
      <el-row class="trust" :gutter="12">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="trust-card" shadow="hover">
            <div class="t-ico">🚚</div>
            <div class="t-title">Giao nhanh</div>
            <div class="t-desc">Theo dõi đơn minh bạch</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="trust-card" shadow="hover">
            <div class="t-ico">🛡️</div>
            <div class="t-title">Chính hãng</div>
            <div class="t-desc">Bảo hành rõ ràng</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="trust-card" shadow="hover">
            <div class="t-ico">🔁</div>
            <div class="t-title">Đổi trả</div>
            <div class="t-desc">Linh hoạt theo chính sách</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="trust-card" shadow="hover">
            <div class="t-ico">🎧</div>
            <div class="t-title">Tư vấn</div>
            <div class="t-desc">Chọn đúng cấu hình</div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- FEATURED -->
    <section id="featured" class="section">
      <div class="section-hdr">
        <div>
          <div class="kicker">Tech Store</div>
          <div class="h">Sản phẩm demo</div>
          <div class="muted">
            Đang lọc: <b>{{ activeCategoryName }}</b>
            <span v-if="searchTerm">
              • Từ khóa: <b>{{ searchTerm }}</b></span
            >
          </div>
        </div>

        <div class="hdr-actions">
          <el-button :disabled="!canPrev" @click="prev">Prev</el-button>
          <el-button :disabled="!canNext" type="primary" plain @click="next"
            >Next</el-button
          >
        </div>
      </div>

      <el-skeleton v-if="loading" animated :rows="6" />

      <template v-else>
        <el-empty
          v-if="sliderProducts.length === 0"
          description="Không có sản phẩm phù hợp."
        />

        <el-row v-else :gutter="12">
          <el-col
            v-for="p in sliderProducts"
            :key="p.id"
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-card class="p-card" shadow="hover">
              <div class="p-top">
                <el-tag round effect="plain" type="info">Laptop</el-tag>
                <el-tag round effect="dark" type="primary">Demo</el-tag>
              </div>

              <div class="thumb">
                <img :src="p.img" :alt="p.name" />
              </div>

              <div class="p-title" :title="p.name">{{ p.name }}</div>

              <div class="p-meta" v-if="p.meta">{{ p.meta }}</div>
              <div class="p-meta muted" v-else>Mô tả / cấu hình demo</div>

              <div class="p-bottom">
                <div class="p-price">{{ p.price }}</div>
                <div class="p-sub ts-muted">Trả góp 0% • Hỗ trợ cài đặt</div>

                <div class="p-actions">
                  <el-button type="primary" plain disabled
                    >Xem chi tiết</el-button
                  >
                  <el-button disabled>Thêm giỏ</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- Pagination theo backend page -->
        <div class="pager" v-if="backendTotal > pageSize">
          <el-pagination
            background
            layout="prev, pager, next"
            :page-size="pageSize"
            :total="backendTotal"
            :current-page="backendPage + 1"
            @current-change="onPageChange"
          />
        </div>
      </template>
    </section>

    <!-- BENEFITS -->
    <section id="benefits" class="section">
      <div class="section-hdr">
        <div>
          <div class="kicker">Dịch vụ</div>
          <div class="h">Mua sắm yên tâm</div>
          <div class="muted">4 điểm mạnh mô phỏng theo landing page thật</div>
        </div>
      </div>

      <el-row :gutter="12">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="b-card" shadow="hover">
            <div class="b-ico">🚚</div>
            <div class="b-title">Giao hàng toàn quốc</div>
            <div class="muted">Nhận hàng nhanh, theo dõi đơn dễ dàng.</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="b-card" shadow="hover">
            <div class="b-ico">🔁</div>
            <div class="b-title">Đổi trả linh hoạt</div>
            <div class="muted">Chính sách đổi trả rõ ràng, minh bạch.</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="b-card" shadow="hover">
            <div class="b-ico">💳</div>
            <div class="b-title">Thanh toán tiện lợi</div>
            <div class="muted">Tiền mặt / chuyển khoản / POS (demo).</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="b-card" shadow="hover">
            <div class="b-ico">🎧</div>
            <div class="b-title">Hỗ trợ nhiệt tình</div>
            <div class="muted">Tư vấn cấu hình, bảo hành & sửa chữa.</div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- FOOTER -->
    <footer id="footer" class="footer">
      <div class="footer-inner">
        <div class="f-left">
          <div class="f-brand">Tech Store</div>
          <div class="muted">
            Demo landing page • Vue 3 + Element Plus • Backend Spring Boot
          </div>
        </div>

        <div class="f-right">
          <el-link :underline="false" @click="scrollTo('featured')"
            >Sản phẩm</el-link
          >
          <el-link :underline="false" @click="scrollTo('benefits')"
            >Dịch vụ</el-link
          >
          <el-link :underline="false" @click="scrollTo('footer')"
            >Liên hệ</el-link
          >
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import http from "../../api/http";
import {
  getLastAuthResponse,
  getRole,
  getToken,
  getUser,
} from "../../stores/auth";

// ====== Auth: bỏ hiển thị JSON, chỉ log console ======
const isAuthed = computed(() => !!getToken());
const isCustomerAuthed = computed(
  () => isAuthed.value && (getRole() || "").toUpperCase() === "CUSTOMER"
);

function reloadConsole() {
  console.log("[AUTH] token?", !!getToken());
  console.log("[AUTH] role=", (getRole() || "").toUpperCase());
  console.log("[AUTH] user=", getUser());
  console.log("[AUTH] lastAuthResponse=", getLastAuthResponse());
}

// ====== Landing state ======
const loading = ref(false);
const categories = ref([]);
const products = ref([]);

const selectedCategoryId = ref(null); // null = all
const searchTerm = ref("");

const backendPage = ref(0);
const pageSize = ref(12);
const backendTotal = ref(0);

// slider next/prev
const perView = ref(3);
const sliderIndex = ref(0);

// slides
const slides = [
  {
    kicker: "Ưu đãi demo",
    title: "Laptop Gaming • Workstation",
    desc: "Tối ưu hiệu năng - tản nhiệt - build chất (demo).",
    img: "https://placehold.co/1200x600/png?text=Tech+Store+Hero+1",
  },
  {
    kicker: "Học tập / Văn phòng",
    title: "Mỏng nhẹ • Pin trâu",
    desc: "Phù hợp sinh viên và dân văn phòng (demo).",
    img: "https://placehold.co/1200x600/png?text=Tech+Store+Hero+2",
  },
  {
    kicker: "Bảo hành / Hỗ trợ",
    title: "Tư vấn cấu hình chuẩn nhu cầu",
    desc: "Gợi ý theo ngân sách, mục đích sử dụng (demo).",
    img: "https://placehold.co/1200x600/png?text=Tech+Store+Hero+3",
  },
];

// ===== helpers =====
function scrollTo(id) {
  const el = document.getElementById(id);
  if (!el) return;
  el.scrollIntoView({ behavior: "smooth", block: "start" });
}

function formatVnd(v) {
  if (typeof v === "number" && Number.isFinite(v)) {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(v);
  }
  if (typeof v === "string" && v.trim()) return v;
  return "Liên hệ";
}

function extractList(payload) {
  // cố gắng “chịu được mọi kiểu response”
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;

  const p = payload?.data ?? payload;
  if (Array.isArray(p)) return p;

  if (Array.isArray(p?.content)) return p.content;
  if (Array.isArray(p?.items)) return p.items;
  if (Array.isArray(p?.results)) return p.results;

  if (Array.isArray(p?.data?.content)) return p.data.content;
  if (Array.isArray(p?.data?.items)) return p.data.items;

  return [];
}

function normalizeProducts(list) {
  return (list || []).map((p, i) => {
    const id = p?.id ?? p?.productId ?? p?.code ?? i + 1;
    const name = p?.name ?? p?.title ?? p?.productName ?? `Product ${id}`;

    // meta: cố gắng ghép vài thông tin nếu backend có, nếu không thì demo
    const cpu = p?.cpu ?? p?.specs?.cpu;
    const ram = p?.ram ?? p?.specs?.ram;
    const ssd = p?.ssd ?? p?.specs?.ssd;
    const meta =
      [cpu, ram, ssd].filter(Boolean).join(" • ") ||
      (p?.description ? String(p.description).slice(0, 60) : "");

    const price = formatVnd(
      p?.finalPrice ?? p?.price ?? p?.basePrice ?? p?.minPrice
    );
    const img =
      p?.imageUrl ??
      p?.thumbnailUrl ??
      `https://placehold.co/640x420/png?text=${encodeURIComponent(name)}`;

    return { id, name, meta, price, img, raw: p };
  });
}

function normalizeCategories(list) {
  return (list || []).map((c, i) => ({
    id: c?.id ?? c?.categoryId ?? i + 1,
    name: c?.name ?? c?.title ?? `Category ${i + 1}`,
    raw: c,
  }));
}

function setResponsivePerView() {
  const w = window.innerWidth || 1200;
  perView.value = w < 640 ? 1 : w < 980 ? 2 : 3;
}

// ===== fetch from backend (fallback demo) =====
async function fetchCategories() {
  try {
    const res = await http.get("/api/categories");
    const raw = res?.data;
    const list = extractList(raw);
    const norm = normalizeCategories(list);
    categories.value = norm.length ? norm : demoCategories();
  } catch (e) {
    categories.value = demoCategories();
  }
}

async function fetchProducts() {
  loading.value = true;
  try {
    const params = {
      page: backendPage.value,
      size: pageSize.value,
    };
    if (selectedCategoryId.value) params.categoryId = selectedCategoryId.value;

    const res = await http.get("/api/products", { params });
    const raw = res?.data;

    // total: cố gắng lấy nếu backend có paging
    const p = raw?.data ?? raw;
    backendTotal.value =
      p?.totalElements ??
      p?.total ??
      p?.data?.totalElements ??
      p?.data?.total ??
      0;

    const list = extractList(raw);
    const norm = normalizeProducts(list);

    // nếu backend trả list nhưng không có total -> set theo list để không hiện pagination ảo
    if (!backendTotal.value) backendTotal.value = norm.length;

    products.value = norm.length ? norm : demoProducts();
  } catch (e) {
    products.value = demoProducts();
    backendTotal.value = products.value.length;
  } finally {
    loading.value = false;
  }
}

function demoCategories() {
  return [
    { id: 1, name: "Laptop Gaming" },
    { id: 2, name: "Ultrabook" },
    { id: 3, name: "Workstation" },
    { id: 4, name: "Văn phòng" },
  ];
}

function demoProducts() {
  const arr = [
    {
      id: 1,
      name: "Laptop Gaming A1",
      meta: "i7-13620H • 16GB • 512GB",
      price: "25,990,000₫",
    },
    {
      id: 2,
      name: "Ultrabook B2",
      meta: "i5-1335U • 16GB • 1TB",
      price: "21,490,000₫",
    },
    {
      id: 3,
      name: "Workstation C3",
      meta: "Ryzen 7 7840HS • 32GB • 1TB",
      price: "32,990,000₫",
    },
    {
      id: 4,
      name: "Student D4",
      meta: "i5-1240P • 8GB • 256GB",
      price: "14,990,000₫",
    },
    {
      id: 5,
      name: "Creator E5",
      meta: "i7-13700H • 32GB • 1TB",
      price: "39,990,000₫",
    },
    {
      id: 6,
      name: "Office F6",
      meta: "Ryzen 5 7530U • 16GB • 512GB",
      price: "16,990,000₫",
    },
    { id: 7, name: "Gaming G7", meta: "i9 • 32GB • 1TB", price: "49,990,000₫" },
    {
      id: 8,
      name: "Ultrabook H8",
      meta: "i7 • 16GB • 512GB",
      price: "27,990,000₫",
    },
    {
      id: 9,
      name: "Office I9",
      meta: "i5 • 16GB • 512GB",
      price: "18,990,000₫",
    },
  ];

  return arr.map((p) => ({
    ...p,
    img: `https://placehold.co/640x420/png?text=${encodeURIComponent(p.name)}`,
  }));
}

// ===== category + search + slider =====
const activeMenu = computed(() =>
  selectedCategoryId.value ? selectedCategoryId.value : "all"
);

const activeCategoryName = computed(() => {
  if (!selectedCategoryId.value) return "Tất cả";
  const found = categories.value.find((x) => x.id === selectedCategoryId.value);
  return found?.name || `#${selectedCategoryId.value}`;
});

const filteredProducts = computed(() => {
  const kw = (searchTerm.value || "").trim().toLowerCase();
  let list = products.value.slice();

  // local filter keyword
  if (kw) {
    list = list.filter((p) => {
      const hay = `${p.name} ${p.meta || ""}`.toLowerCase();
      return hay.includes(kw);
    });
  }
  return list;
});

const sliderProducts = computed(() => {
  const list = filteredProducts.value;
  const start = Math.max(
    0,
    Math.min(sliderIndex.value, Math.max(0, list.length - 1))
  );
  return list.slice(start, start + perView.value);
});

const canPrev = computed(() => sliderIndex.value > 0);
const canNext = computed(
  () => sliderIndex.value + perView.value < filteredProducts.value.length
);

function prev() {
  sliderIndex.value = Math.max(0, sliderIndex.value - perView.value);
}
function next() {
  sliderIndex.value = Math.min(
    Math.max(0, filteredProducts.value.length - perView.value),
    sliderIndex.value + perView.value
  );
}

async function onSelectCategory(index) {
  sliderIndex.value = 0;
  backendPage.value = 0;

  if (index === "all") selectedCategoryId.value = null;
  else selectedCategoryId.value = Number(index);

  // gọi backend theo categoryId nếu có
  await fetchProducts();
  scrollTo("featured");
}

async function onPageChange(page1Based) {
  backendPage.value = Math.max(0, (page1Based || 1) - 1);
  sliderIndex.value = 0;
  await fetchProducts();
  scrollTo("featured");
}

// nhận search từ App.vue
function onSearchEvent(e) {
  searchTerm.value = (e?.detail || "").toString();
  sliderIndex.value = 0;
}

onMounted(async () => {
  setResponsivePerView();
  window.addEventListener("resize", setResponsivePerView);
  window.addEventListener("products:search", onSearchEvent);

  // log auth (không hiển thị JSON trên UI)
  if (isCustomerAuthed.value || isAuthed.value) reloadConsole();

  await fetchCategories();
  await fetchProducts();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", setResponsivePerView);
  window.removeEventListener("products:search", onSearchEvent);
});
</script>

<style scoped>
.landing {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  margin-bottom: 16px;
}

.hero-card {
  border-radius: 18px;
}

.hero-right :deep(.el-card__body) {
  padding: 12px;
}

.menu-hdr {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  letter-spacing: 0.2px;
  text-transform: uppercase;
}

.title {
  font-weight: 900;
  font-size: 16px;
  margin-top: 2px;
}

.cat-menu {
  border-right: 0;
}

.mini-note {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(15, 23, 42, 0.16);
}

.slide {
  height: 290px;
  border-radius: 16px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.overlay {
  position: absolute;
  inset: 0;
  padding: 18px;
  display: grid;
  align-content: end;
  gap: 6px;
  background: linear-gradient(
    180deg,
    rgba(2, 6, 23, 0.06),
    rgba(2, 6, 23, 0.62)
  );
  color: #fff;
}

.slide-kicker {
  font-size: 12px;
  opacity: 0.95;
  font-weight: 900;
  letter-spacing: 0.2px;
  text-transform: uppercase;
}

.slide-title {
  font-size: 24px;
  font-weight: 900;
  letter-spacing: -0.3px;
}

.slide-desc {
  opacity: 0.92;
  max-width: 560px;
  line-height: 1.55;
}

.slide-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

/* Trust bar */
.trust {
  margin-top: 12px;
}
.trust-card {
  border-radius: 16px;
}
.trust-card :deep(.el-card__body) {
  padding: 14px;
  display: grid;
  gap: 4px;
}
.t-ico {
  font-size: 22px;
}
.t-title {
  font-weight: 900;
}
.t-desc {
  font-size: 13px;
  color: rgba(15, 23, 42, 0.62);
}

.section {
  margin-top: 18px;
}

.section-hdr {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.hdr-actions {
  display: flex;
  gap: 8px;
}

.h {
  font-weight: 900;
  font-size: 18px;
  letter-spacing: -0.2px;
}

.muted {
  color: rgba(15, 23, 42, 0.62);
  font-size: 13px;
}

.p-card {
  border-radius: 16px;
  height: 100%;
}

.p-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.thumb {
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.1);
  background: rgba(2, 6, 23, 0.03);
}

.thumb img {
  width: 100%;
  height: 178px;
  object-fit: cover;
  display: block;
}

.p-title {
  font-weight: 900;
  margin-top: 10px;
  line-height: 1.25;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;
}

.p-meta {
  margin-top: 6px;
  font-size: 13px;
  opacity: 0.9;
  min-height: 18px;
}

.p-bottom {
  margin-top: 10px;
  display: grid;
  gap: 6px;
}

.p-price {
  font-weight: 900;
  font-size: 18px;
  letter-spacing: -0.2px;
}

.p-sub {
  font-size: 12px;
}

.p-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}

/* Benefits */
.b-card {
  border-radius: 16px;
  text-align: center;
}
.b-ico {
  font-size: 28px;
  margin-bottom: 8px;
}
.b-title {
  font-weight: 900;
  margin-bottom: 4px;
}

/* Footer */
.footer {
  margin-top: 18px;
  padding: 18px 12px 26px;
  border-top: 1px solid rgba(15, 23, 42, 0.1);
}
.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.f-brand {
  font-weight: 900;
}
.f-right {
  display: flex;
  gap: 12px;
}

/* Mobile tweaks */
@media (max-width: 640px) {
  .slide {
    height: 250px;
  }
  .slide-title {
    font-size: 20px;
  }
}
</style>
