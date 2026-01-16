<template>
  <div class="landing container-xl">
    <!-- HERO -->
    <section class="hero">
      <div class="row g-3">
        <!-- Left: Categories -->
        <div class="col-12 col-md-4 col-lg-3">
          <div class="card ts-card h-100">
            <div class="card-body">
              <div
                class="d-flex align-items-start justify-content-between gap-2 mb-2"
              >
                <div>
                  <div class="kicker">Danh mục</div>
                  <div class="title">Chọn nhanh</div>
                </div>

                <button
                  class="btn btn-sm btn-outline-primary ts-btn"
                  @click="reloadConsole"
                >
                  Log console
                </button>
              </div>

              <div class="list-group">
                <button
                  class="list-group-item list-group-item-action"
                  :class="{ active: String(activeMenu) === 'all' }"
                  @click="onSelectCategory('all')"
                >
                  Tất cả
                </button>

                <button
                  v-for="c in categories"
                  :key="c.id"
                  class="list-group-item list-group-item-action"
                  :class="{ active: String(activeMenu) === String(c.id) }"
                  @click="onSelectCategory(String(c.id))"
                >
                  {{ c.name }}
                </button>
              </div>

              <div class="mini-note mt-3 pt-3">
                <div class="muted">
                  * Trang demo sẽ tự gọi API:
                  <b>/api/categories</b> và <b>/api/products</b>. Nếu backend
                  thiếu GET sẽ fallback demo data.
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Hero Carousel -->
        <div class="col-12 col-md-8 col-lg-9">
          <div class="card ts-card hero-right">
            <div class="card-body p-2">
              <div
                id="heroCarousel"
                class="carousel slide"
                data-bs-ride="carousel"
              >
                <div class="carousel-indicators">
                  <button
                    v-for="(_, i) in slides"
                    :key="i"
                    type="button"
                    data-bs-target="#heroCarousel"
                    :data-bs-slide-to="i"
                    :class="{ active: i === 0 }"
                    :aria-current="i === 0 ? 'true' : 'false'"
                    :aria-label="`Slide ${i + 1}`"
                  ></button>
                </div>

                <div class="carousel-inner">
                  <div
                    v-for="(s, i) in slides"
                    :key="s.title"
                    class="carousel-item"
                    :class="{ active: i === 0 }"
                  >
                    <div
                      class="slide"
                      :style="{ backgroundImage: `url(${s.img})` }"
                    >
                      <div class="overlay">
                        <div class="slide-kicker">{{ s.kicker }}</div>
                        <div class="slide-title">{{ s.title }}</div>
                        <div class="slide-desc">{{ s.desc }}</div>

                        <div class="slide-actions">
                          <button
                            class="btn btn-primary ts-btn"
                            @click="scrollTo('featured')"
                          >
                            Xem sản phẩm
                          </button>
                          <button
                            class="btn btn-outline-light ts-btn"
                            @click="scrollTo('benefits')"
                          >
                            Dịch vụ
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <button
                  class="carousel-control-prev"
                  type="button"
                  data-bs-target="#heroCarousel"
                  data-bs-slide="prev"
                >
                  <span
                    class="carousel-control-prev-icon"
                    aria-hidden="true"
                  ></span>
                  <span class="visually-hidden">Previous</span>
                </button>
                <button
                  class="carousel-control-next"
                  type="button"
                  data-bs-target="#heroCarousel"
                  data-bs-slide="next"
                >
                  <span
                    class="carousel-control-next-icon"
                    aria-hidden="true"
                  ></span>
                  <span class="visually-hidden">Next</span>
                </button>
              </div>
            </div>
          </div>

          <hr />
        </div>
      </div>

      <!-- Trust Bar -->
      <div class="row g-3 mt-1 trust">
        <div
          class="col-12 col-sm-6 col-lg-3"
          v-for="t in trustItems"
          :key="t.title"
        >
          <div class="card ts-card h-100">
            <div class="card-body d-grid gap-1">
              <div class="t-ico">{{ t.ico }}</div>
              <div class="t-title">{{ t.title }}</div>
              <div class="t-desc">{{ t.desc }}</div>
            </div>
          </div>
        </div>
      </div>
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

        <!-- Header arrows (control carousel) -->
        <div class="hdr-actions">
          <button
            class="btn btn-outline-secondary ts-btn"
            :disabled="!canPrev"
            @click="prev"
          >
            Prev
          </button>
          <button
            class="btn btn-outline-primary ts-btn"
            :disabled="!canNext"
            @click="next"
          >
            Next
          </button>
        </div>
      </div>

      <!-- Loading skeleton -->
      <div v-if="loading" class="row g-3">
        <div v-for="i in perView * 2" :key="i" :class="productColClass">
          <div class="card ts-card">
            <div class="card-body">
              <div class="placeholder-glow">
                <div class="placeholder col-6 mb-2"></div>
                <div class="placeholder col-12" style="height: 178px"></div>
                <div class="placeholder col-10 mt-3"></div>
                <div class="placeholder col-7 mt-2"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template v-else>
        <div v-if="productSlides.length === 0" class="text-center py-5">
          <div class="ts-muted">Không có sản phẩm phù hợp.</div>
        </div>

        <!-- ✅ Product carousel -->
        <div
          v-else
          id="productCarousel"
          ref="productCarouselEl"
          class="carousel slide product-carousel"
          data-bs-touch="true"
          data-bs-interval="false"
          data-bs-wrap="false"
        >
          <!-- Dots -->
          <div class="carousel-indicators">
            <button
              v-for="(_, i) in productSlides"
              :key="i"
              type="button"
              data-bs-target="#productCarousel"
              :data-bs-slide-to="i"
              :class="{ active: i === 0 }"
              :aria-current="i === 0 ? 'true' : 'false'"
              :aria-label="`Slide ${i + 1}`"
            ></button>
          </div>

          <div class="carousel-inner">
            <div
              v-for="(group, i) in productSlides"
              :key="i"
              class="carousel-item"
              :class="{ active: i === 0 }"
            >
              <div class="product-slide px-1 px-md-4">
                <div class="row g-3">
                  <div v-for="p in group" :key="p.id" :class="productColClass">
                    <div
                      class="card ts-card h-100"
                      style="border-bottom-left-radius: 0"
                    >
                      <div class="card-body">
                        <div
                          class="d-flex align-items-center justify-content-between mb-2"
                        >
                          <span class="badge text-bg-info ts-pill">Laptop</span>
                          <span class="badge text-bg-primary ts-pill"
                            >Demo</span
                          >
                        </div>

                        <div class="thumb">
                          <img :src="p.img" :alt="p.name" />
                        </div>

                        <div class="p-title mt-3" :title="p.name">
                          {{ p.name }}
                        </div>

                        <div class="p-meta" v-if="p.meta">{{ p.meta }}</div>
                        <div class="p-meta muted" v-else>
                          Mô tả / cấu hình demo
                        </div>

                        <div class="p-bottom">
                          <div class="p-price">{{ p.price }}</div>
                          <div class="p-sub ts-muted">
                            Trả góp 0% • Hỗ trợ cài đặt
                          </div>

                          <div class="d-flex gap-2 mt-2">
                            <button
                              class="btn btn-outline-primary ts-btn flex-fill"
                              disabled
                            >
                              Xem chi tiết
                            </button>
                            <button
                              class="btn btn-outline-secondary ts-btn flex-fill"
                              disabled
                            >
                              Thêm giỏ
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- /col -->
                </div>
              </div>
            </div>
          </div>

          <!-- Arrows -->
          <button
            class="carousel-control-prev"
            type="button"
            data-bs-target="#productCarousel"
            data-bs-slide="prev"
            :disabled="!canPrev"
            :class="{ 'pe-none opacity-50': !canPrev }"
            aria-label="Previous"
          >
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Previous</span>
          </button>

          <button
            class="carousel-control-next"
            type="button"
            data-bs-target="#productCarousel"
            data-bs-slide="next"
            :disabled="!canNext"
            :class="{ 'pe-none opacity-50': !canNext }"
            aria-label="Next"
          >
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Next</span>
          </button>
        </div>

        <!-- Backend pagination (kept) -->
        <nav v-if="backendTotal > pageSize" class="pager mt-3">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: backendPage <= 0 }">
              <button
                class="page-link"
                @click="onPageChange(backendPage)"
                :disabled="backendPage <= 0"
              >
                Prev
              </button>
            </li>

            <li class="page-item disabled">
              <span class="page-link">
                Page {{ backendPage + 1 }} / {{ totalPages }}
              </span>
            </li>

            <li
              class="page-item"
              :class="{ disabled: backendPage + 1 >= totalPages }"
            >
              <button
                class="page-link"
                @click="onPageChange(backendPage + 2)"
                :disabled="backendPage + 1 >= totalPages"
              >
                Next
              </button>
            </li>
          </ul>
        </nav>
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

      <div class="row g-3">
        <div
          class="col-12 col-sm-6 col-lg-3"
          v-for="b in benefitItems"
          :key="b.title"
        >
          <div class="card ts-card h-100 text-center">
            <div class="card-body">
              <div class="b-ico">{{ b.ico }}</div>
              <div class="b-title">{{ b.title }}</div>
              <div class="muted">{{ b.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- FOOTER -->
    <footer id="footer" class="footer">
      <div
        class="d-flex flex-column flex-md-row align-items-start align-items-md-center justify-content-between gap-3"
      >
        <div>
          <div class="f-brand">Tech Store</div>
          <div class="muted">
            Demo landing page • Vue 3 + Bootstrap 5 • Backend Spring Boot
          </div>
        </div>

        <div class="d-flex gap-2">
          <button
            class="btn btn-sm btn-light ts-pill"
            @click="scrollTo('featured')"
          >
            Sản phẩm
          </button>
          <button
            class="btn btn-sm btn-light ts-pill"
            @click="scrollTo('benefits')"
          >
            Dịch vụ
          </button>
          <button
            class="btn btn-sm btn-light ts-pill"
            @click="scrollTo('footer')"
          >
            Liên hệ
          </button>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
  nextTick,
} from "vue";
import http from "../../api/http";
import {
  getLastAuthResponse,
  getRole,
  getToken,
  getUser,
} from "../../stores/auth";

// ===== Auth: console-only (kept) =====
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

// ===== Landing state =====
const loading = ref(false);
const categories = ref([]);
const products = ref([]);

const selectedCategoryId = ref(null); // null = all
const searchTerm = ref("");

const backendPage = ref(0);
const pageSize = ref(12);
const backendTotal = ref(0);

// ✅ responsive items per slide (desktop = 4)
const perView = ref(4);

// ===== Product carousel (Bootstrap) =====
const productCarouselEl = ref(null);
const activeSlide = ref(0);

let productCarouselInstance = null;
let carouselCtor = null;
let onSlidHandler = null;

async function getCarouselCtor() {
  if (carouselCtor) return carouselCtor;

  if (typeof window !== "undefined" && window.bootstrap?.Carousel) {
    carouselCtor = window.bootstrap.Carousel;
    return carouselCtor;
  }

  try {
    const mod = await import("bootstrap/js/dist/carousel");
    carouselCtor = mod?.default || mod?.Carousel || null;
    return carouselCtor;
  } catch {}

  try {
    const bs = await import("bootstrap");
    carouselCtor = bs?.Carousel || null;
    return carouselCtor;
  } catch {}

  return null;
}

async function ensureProductCarousel() {
  if (!productCarouselEl.value) return;

  const Ctor = await getCarouselCtor();
  if (!Ctor) return;

  productCarouselInstance =
    Ctor.getOrCreateInstance?.(productCarouselEl.value, {
      interval: false,
      ride: false,
      touch: true,
      wrap: false,
    }) ||
    new Ctor(productCarouselEl.value, {
      interval: false,
      ride: false,
      touch: true,
      wrap: false,
    });

  onSlidHandler = (e) => {
    activeSlide.value = typeof e?.to === "number" ? e.to : 0;
  };
  productCarouselEl.value.addEventListener("slid.bs.carousel", onSlidHandler);
}

function disposeProductCarousel() {
  if (productCarouselEl.value && onSlidHandler) {
    productCarouselEl.value.removeEventListener(
      "slid.bs.carousel",
      onSlidHandler
    );
  }
  onSlidHandler = null;
  productCarouselInstance?.dispose?.();
  productCarouselInstance = null;
}

async function resetProductCarouselToStart() {
  activeSlide.value = 0;
  await nextTick();
  productCarouselInstance?.to?.(0);
}

function prev() {
  productCarouselInstance?.prev?.();
}
function next() {
  productCarouselInstance?.next?.();
}

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

const trustItems = [
  { ico: "🚚", title: "Giao nhanh", desc: "Theo dõi đơn minh bạch" },
  { ico: "🛡️", title: "Chính hãng", desc: "Bảo hành rõ ràng" },
  { ico: "🔁", title: "Đổi trả", desc: "Linh hoạt theo chính sách" },
  { ico: "🎧", title: "Tư vấn", desc: "Chọn đúng cấu hình" },
];

const benefitItems = [
  {
    ico: "🚚",
    title: "Giao hàng toàn quốc",
    desc: "Nhận hàng nhanh, theo dõi đơn dễ dàng.",
  },
  {
    ico: "🔁",
    title: "Đổi trả linh hoạt",
    desc: "Chính sách đổi trả rõ ràng, minh bạch.",
  },
  {
    ico: "💳",
    title: "Thanh toán tiện lợi",
    desc: "Tiền mặt / chuyển khoản / POS (demo).",
  },
  {
    ico: "🎧",
    title: "Hỗ trợ nhiệt tình",
    desc: "Tư vấn cấu hình, bảo hành & sửa chữa.",
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

/**
 * ✅ Robust list extractor:
 * - supports common wrappers: data/content/items/results, nested data/data, result/items, etc.
 * - safe deep search fallback (limited depth + nodes) to prevent weird objects from breaking it
 */
function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;

  const preferredArrayKeys = ["content", "items", "results", "rows", "list"];

  const tryPick = (obj) => {
    if (!obj || typeof obj !== "object") return null;
    for (const k of preferredArrayKeys) {
      if (Array.isArray(obj[k])) return obj[k];
    }
    return null;
  };

  // Common unwrap candidates (Axios + typical API envelopes)
  const candidates = [
    payload,
    payload?.data,
    payload?.data?.data,
    payload?.result,
    payload?.result?.data,
    payload?.payload,
    payload?.payload?.data,
  ].filter(Boolean);

  for (const c of candidates) {
    if (Array.isArray(c)) return c;
    const picked = tryPick(c);
    if (picked) return picked;
  }

  // Safe deep search fallback
  const seen = new Set();
  const queue = [{ v: payload, d: 0 }];
  const MAX_DEPTH = 4;
  const MAX_NODES = 60;

  let processed = 0;
  while (queue.length && processed < MAX_NODES) {
    const { v, d } = queue.shift();
    processed++;

    if (!v || typeof v !== "object") continue;
    if (seen.has(v)) continue;
    seen.add(v);

    const picked = tryPick(v);
    if (picked) return picked;

    if (d >= MAX_DEPTH) continue;

    // prioritize exploring common wrapper keys first
    const keys = Object.keys(v);
    const orderedKeys = [
      ...preferredArrayKeys.filter((k) => keys.includes(k)),
      ...keys.filter((k) => !preferredArrayKeys.includes(k)),
    ];

    for (const k of orderedKeys) {
      const child = v[k];
      if (!child) continue;
      if (Array.isArray(child)) {
        // If we reach here, it's an array but not under preferred keys.
        // Still return it as a last resort.
        return child;
      }
      if (typeof child === "object") queue.push({ v: child, d: d + 1 });
    }
  }

  return [];
}

function normalizeProducts(list) {
  return (list || []).map((p, i) => {
    const id = p?.id ?? p?.productId ?? p?.code ?? i + 1;
    const name = p?.name ?? p?.title ?? p?.productName ?? `Product ${id}`;

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

// ✅ perView now supports 4 cards on desktop
function setResponsivePerView() {
  const w = window.innerWidth || 1200;

  // Align with Bootstrap-ish breakpoints:
  // <576: 1 | <992: 2 | <1200: 3 | >=1200: 4
  perView.value = w < 576 ? 1 : w < 992 ? 2 : w < 1200 ? 3 : 4;
}

// ===== fetch from backend (fallback demo) =====
async function fetchCategories() {
  try {
    const res = await http.get("/api/categories");
    const list = extractList(res?.data);
    const norm = normalizeCategories(list);
    categories.value = norm.length ? norm : demoCategories();
  } catch {
    categories.value = demoCategories();
  }
}

async function fetchProducts() {
  loading.value = true;
  try {
    const params = { page: backendPage.value, size: pageSize.value };
    if (selectedCategoryId.value) params.categoryId = selectedCategoryId.value;

    const res = await http.get("/api/products", { params });
    const raw = res?.data;

    // total extraction (more tolerant)
    const p = raw?.data ?? raw;
    backendTotal.value =
      p?.totalElements ??
      p?.total ??
      p?.data?.totalElements ??
      p?.data?.total ??
      raw?.totalElements ??
      raw?.total ??
      0;

    const list = extractList(raw);
    const norm = normalizeProducts(list);

    if (!backendTotal.value) backendTotal.value = norm.length;

    // ✅ Avoid “All < Laptop” due to bad parsing -> demo fallback:
    // If backend says there are items (total>0) but we extracted none,
    // keep empty + warn instead of silently replacing with demo.
    if (backendTotal.value > 0 && norm.length === 0) {
      console.warn(
        "[Landing] /api/products returned total > 0 but list extraction got 0 items. Check API response shape.",
        raw
      );
      products.value = [];
      return;
    }

    products.value = norm.length ? norm : demoProducts();
  } catch {
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

// ===== category + search + carousel slides =====
const activeMenu = computed(() =>
  selectedCategoryId.value ? selectedCategoryId.value : "all"
);

const activeCategoryName = computed(() => {
  if (!selectedCategoryId.value) return "Tất cả";
  const found = categories.value.find(
    (x) => String(x.id) === String(selectedCategoryId.value)
  );
  return found?.name || `#${selectedCategoryId.value}`;
});

const filteredProducts = computed(() => {
  const kw = (searchTerm.value || "").trim().toLowerCase();
  let list = products.value.slice();
  if (kw) {
    list = list.filter((p) =>
      `${p.name} ${p.meta || ""}`.toLowerCase().includes(kw)
    );
  }
  return list;
});

const productSlides = computed(() => {
  const list = filteredProducts.value || [];
  const size = Math.max(1, perView.value || 4);

  const out = [];
  for (let i = 0; i < list.length; i += size) out.push(list.slice(i, i + size));
  return out;
});

// ✅ Make Bootstrap grid match perView (4 => col-lg-3)
const productColClass = computed(() => {
  if (perView.value === 1) return "col-12";
  if (perView.value === 2) return "col-12 col-md-6";
  if (perView.value === 3) return "col-12 col-sm-6 col-lg-4";
  return "col-12 col-sm-6 col-lg-3"; // 4 cards
});

const canPrev = computed(() => activeSlide.value > 0);
const canNext = computed(
  () => activeSlide.value < productSlides.value.length - 1
);

const totalPages = computed(() =>
  Math.max(1, Math.ceil((backendTotal.value || 0) / (pageSize.value || 12)))
);

async function onSelectCategory(index) {
  backendPage.value = 0;

  if (index === "all") selectedCategoryId.value = null;
  else selectedCategoryId.value = Number(index);

  await fetchProducts();
  await resetProductCarouselToStart();
  scrollTo("featured");
}

async function onPageChange(page1Based) {
  backendPage.value = Math.max(0, (page1Based || 1) - 1);

  await fetchProducts();
  await resetProductCarouselToStart();
  scrollTo("featured");
}

// receive search from App.vue
function onSearchEvent(e) {
  searchTerm.value = (e?.detail || "").toString();
  resetProductCarouselToStart();
}

// If perView changes (resize) or filter changes -> regroup slides -> reset to slide 0
watch([perView, filteredProducts], async () => {
  if (activeSlide.value > productSlides.value.length - 1) activeSlide.value = 0;
  await resetProductCarouselToStart();
});

onMounted(async () => {
  setResponsivePerView();
  window.addEventListener("resize", setResponsivePerView);
  window.addEventListener("products:search", onSearchEvent);

  if (isCustomerAuthed.value || isAuthed.value) reloadConsole();

  await fetchCategories();
  await fetchProducts();

  await nextTick();
  await ensureProductCarousel();
  await resetProductCarouselToStart();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", setResponsivePerView);
  window.removeEventListener("products:search", onSearchEvent);
  disposeProductCarousel();
});
</script>

<style scoped>
.landing {
  max-width: 1200px;
}

.hero {
  margin-bottom: 16px;
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

.mini-note {
  border-top: 1px dashed rgba(15, 23, 42, 0.16);
}

.slide {
  min-height: 360px;
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
  flex-wrap: wrap;
}

/* Trust bar */
.trust .card-body {
  padding: 14px;
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

.b-ico {
  font-size: 28px;
  margin-bottom: 8px;
}
.b-title {
  font-weight: 900;
  margin-bottom: 4px;
}

.footer {
  margin-top: 18px;
  padding: 18px 12px 26px;
  border-top: 1px solid rgba(15, 23, 42, 0.1);
}
.f-brand {
  font-weight: 900;
}

/* ✅ Product carousel visuals */
.product-carousel {
  position: relative;
}

.product-carousel .carousel-inner {
  padding-top: 24px; /* room for indicators */
}

.product-carousel .carousel-control-prev,
.product-carousel .carousel-control-next {
  width: 56px;
  opacity: 1;
}

.product-carousel .carousel-control-prev-icon,
.product-carousel .carousel-control-next-icon {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background-color: rgba(2, 6, 23, 0.55);
  background-size: 18px 18px;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.16);
}

.product-carousel .carousel-indicators [data-bs-target] {
  width: 8px;
  height: 8px;
  border-radius: 999px;
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
