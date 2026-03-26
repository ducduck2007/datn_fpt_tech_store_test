<template>
  <div class="container-xl py-4">
    <el-breadcrumb separator="/" class="mb-4">
      <el-breadcrumb-item :to="{ path: '/' }">Trang chủ</el-breadcrumb-item>
      <el-breadcrumb-item>Chi tiết sản phẩm</el-breadcrumb-item>
      <el-breadcrumb-item v-if="product" class="fw-bold">{{
        product.name
      }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-skeleton v-if="loading" :rows="10" animated />

    <div v-else-if="product" class="row g-4">
      <div class="col-12 col-md-5">
        <el-card shadow="never" class="border-0 bg-light text-center p-3 mb-3 position-relative">
          <el-image
            :src="mainImage"
            :preview-src-list="galleryImages"
            fit="contain"
            style="width: 100%; height: 350px; border-radius: 8px"
            :class="{ 'opacity-50': currentStock === 0 }"
          />
          <div v-if="currentStock === 0" class="out-of-stock-watermark">
            <span>HẾT HÀNG</span>
          </div>
        </el-card>

        <div class="d-flex gap-2 overflow-auto pb-2 custom-scrollbar">
          <div
            v-for="(img, idx) in galleryImages"
            :key="idx"
            class="thumbnail-wrapper"
            :class="{ active: mainImage === img }"
            @click="mainImage = img"
          >
            <el-image
              :src="img"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 6px"
            />
          </div>
        </div>
      </div>

      <div class="col-12 col-md-7">
        <div class="d-flex align-items-center gap-2 mb-2">
          <el-tag v-if="product.isNew" type="danger" effect="dark" round
            >NEW ARRIVAL</el-tag
          >
          <span class="text-muted small">SKU: {{ product.sku }}</span>
        </div>

        <h2 class="fw-bold mb-3" style="color: #1e293b">{{ product.name }}</h2>

        <div v-if="product.tags && product.tags.length > 0" class="mb-3">
          <el-tag
            v-for="(tag, idx) in product.tags"
            :key="idx"
            size="small"
            type="warning"
            effect="light"
            class="me-2"
          >
            {{ tag }}
          </el-tag>
        </div>

        <el-divider border-style="dashed" />

        <div class="mb-4">
          <div class="text-danger fw-bolder" style="font-size: 2rem">
            {{
              formatPrice(
                selectedVariant ? selectedVariant.price : product.minPrice,
              )
            }}
          </div>
        </div>

        <div v-if="variants.length > 0" class="mb-4">
          <h6 class="fw-bold mb-2">Chọn phiên bản / cấu hình:</h6>
          <div class="d-flex flex-wrap gap-2">
            <el-button
              v-for="v in variants"
              :key="v.id"
              :type="selectedVariant?.id === v.id ? 'primary' : 'default'"
              :plain="selectedVariant?.id !== v.id"
              class="variant-btn"
              @click="selectVariant(v)"
            >
              {{ v.variantName }}
            </el-button>
          </div>
        </div>

        <div class="bg-light p-3 rounded-3 mb-4 border">
          <div class="d-flex align-items-center justify-content-between mb-3">
            <span class="fw-bold text-muted">Trạng thái kho:</span>
            <el-tag
              :type="currentStock > 0 ? 'success' : 'danger'"
              effect="dark"
              size="large"
            >
              {{
                currentStock > 0 ? `Còn hàng (${currentStock} máy)` : "Hết hàng"
              }}
            </el-tag>
          </div>

          <div class="d-flex gap-3 align-items-center mt-4">
            <div style="width: 130px">
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="currentStock > 0 ? currentStock : 1"
                :disabled="currentStock === 0"
                style="width: 100%"
              />
            </div>
            <el-button
              type="primary"
              size="large"
              class="flex-grow-1 add-to-cart-btn"
              icon="ShoppingCart"
              :disabled="currentStock === 0 || !selectedVariant"
              :loading="addingToCart"
              @click="handleAddToCart"
            >
              THÊM VÀO GIỎ HÀNG
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="Không tìm thấy thông tin sản phẩm" />

    <div v-if="product" class="mt-5">
      <el-card shadow="never">
        <el-tabs v-model="activeTab" class="product-tabs">
          <el-tab-pane label="Đặc điểm nổi bật" name="description">
            <div
              class="p-3"
              style="white-space: pre-wrap; line-height: 1.8; color: #475569"
            >
              {{ displayDescription }}
            </div>
          </el-tab-pane>

          <el-tab-pane label="Thông số kỹ thuật" name="specs">
            <div class="p-3">
              <el-table
                :data="attributes"
                border
                stripe
                style="width: 100%; max-width: 600px"
              >
                <el-table-column
                  prop="name"
                  label="Thông số"
                  width="200"
                  class-name="fw-bold bg-light"
                />
                <el-table-column prop="value" label="Chi tiết" />
                <template #empty>
                  <el-empty
                    description="Chưa cập nhật thông số kỹ thuật"
                    :image-size="60"
                  />
                </template>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { productsApi } from "../../api/products.api";
import { useCartStore } from "../../stores/cart";
import { toast } from "../../ui/toast";

const route = useRoute();
const cartStore = useCartStore();

const loading = ref(true);
const addingToCart = ref(false);
const product = ref(null);
const variants = ref([]);
const attributes = ref([]);
const galleryImages = ref([]);
const mainImage = ref("");
const selectedVariant = ref(null);
const quantity = ref(1);
const activeTab = ref("description");

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// Tính toán tồn kho dựa trên biến thể đang chọn
const currentStock = computed(() => {
  if (!selectedVariant.value) return 0;
  return selectedVariant.value.stockQuantity || 0;
});

// TỰ ĐỘNG CẮT BỎ THÔNG SỐ KỸ THUẬT KHỎI PHẦN MÔ TẢ
const displayDescription = computed(() => {
  if (!product.value || !product.value.description) {
    return "Chưa có bài viết mô tả cho sản phẩm này.";
  }
  // Tách chuỗi tại chữ "--- THÔNG SỐ KỸ THUẬT ---" và chỉ lấy phần đằng trước nó
  return product.value.description.split("--- THÔNG SỐ KỸ THUẬT ---")[0].trim();
});

// Hàm format tiền tệ
function formatPrice(val) {
  if (val == null) return "Liên hệ";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(val);
}

// Xử lý đường dẫn ảnh tuyệt đối (Chống lỗi ảnh hỏng)
function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/600?text=No+Image";
  if (url.startsWith("http")) return url;
  return `${BASE_URL}${url}`;
}

function selectVariant(v) {
  selectedVariant.value = v;
  quantity.value = 1; // Reset số lượng khi đổi cấu hình
}

async function loadProductData() {
  const productId = route.params.id;
  if (!productId) return;

  loading.value = true;
  try {
    const res = await productsApi.get(productId);
    const data = res.data?.data || res.data;

    product.value = data;

    // Xử lý danh sách ảnh
    if (data.images && data.images.length > 0) {
      // Sắp xếp ảnh chính lên đầu
      const sortedImages = [...data.images].sort(
        (a, b) => (b.isPrimary ? 1 : 0) - (a.isPrimary ? 1 : 0),
      );
      galleryImages.value = sortedImages.map((img) => fixImageUrl(img.url));
      mainImage.value = galleryImages.value[0];
    } else {
      mainImage.value = fixImageUrl(null);
      galleryImages.value = [mainImage.value];
    }

    // Xử lý thông số kỹ thuật (Attributes JSON)
    if (data.attributes) {
      try {
        const parsed = JSON.parse(data.attributes);
        // Kiểm tra xem nó là mảng [{name, value}] (như lúc thêm mới) hay Object {Key: Value}
        if (Array.isArray(parsed)) {
          attributes.value = parsed;
        } else {
          attributes.value = Object.keys(parsed).map((key) => ({
            name: key,
            value: parsed[key],
          }));
        }
      } catch (e) {
        attributes.value = [];
      }
    }

    // Lấy danh sách Biến thể (Variants)
    if (data.variants && data.variants.length > 0) {
      variants.value = data.variants.filter((v) => v.isActive);
    } else {
      const varRes = await fetch(
        `${BASE_URL}/api/products/${productId}/variants`,
      ).then((r) => r.json());
      variants.value = (varRes || []).filter((v) => v.isActive);
    }

    // Mặc định chọn biến thể đầu tiên
    if (variants.value.length > 0) {
      selectVariant(variants.value[0]);
    }
  } catch (error) {
    toast("Lỗi khi tải dữ liệu sản phẩm", "error");
    console.error(error);
  } finally {
    loading.value = false;
  }
}

async function handleAddToCart() {
  if (!selectedVariant.value) {
    return toast("Vui lòng chọn cấu hình sản phẩm", "warning");
  }
  if (quantity.value > currentStock.value) {
    return toast("Số lượng vượt quá tồn kho", "warning");
  }

  addingToCart.value = true;
  try {
    await cartStore.addToCart(selectedVariant.value.id, quantity.value);
    toast(`Đã thêm ${quantity.value} máy vào giỏ hàng!`, "success");
  } catch (error) {
    toast("Không thể thêm vào giỏ hàng. Vui lòng đăng nhập.", "error");
  } finally {
    addingToCart.value = false;
  }
}

onMounted(() => {
  loadProductData();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap");

/* =========================================================
   [MỚI] THÊM CSS CHO WATERMARK HẾT HÀNG
   ========================================================= */
.out-of-stock-watermark {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.7);
  padding: 12px 24px;
  border-radius: 8px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  pointer-events: none;
  backdrop-filter: blur(4px);
  z-index: 10;
}
.out-of-stock-watermark span {
  color: white;
  font-family: "Inter", sans-serif;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 2px;
}

/* =========================================================
   BASE RESET & FONT
   ========================================================= */
* {
  font-family:
    "Inter",
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    sans-serif;
}

/* =========================================================
   BREADCRUMB
   ========================================================= */
:deep(.el-breadcrumb) {
  font-size: 13px;
  font-family: "Inter", sans-serif;
}

:deep(.el-breadcrumb__inner a),
:deep(.el-breadcrumb__inner.is-link) {
  color: #64748b;
  font-weight: 500;
  transition: color 0.2s;
}

:deep(.el-breadcrumb__inner a:hover) {
  color: #3b82f6;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #0f172a;
  font-weight: 600;
}

/* =========================================================
   GALLERY — ẢNH CHÍNH
   ========================================================= */
.gallery-card {
  border: none !important;
  background: #f8fafc !important;
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.06),
    0 4px 16px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s ease;
}

.gallery-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* =========================================================
   THUMBNAILS
   ========================================================= */
.thumbnail-wrapper {
  border: 2px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  padding: 3px;
  transition:
    border-color 0.18s ease,
    transform 0.18s ease;
  background: #f1f5f9;
  flex-shrink: 0;
}

.thumbnail-wrapper:hover {
  border-color: #94a3b8;
  transform: translateY(-2px);
}

.thumbnail-wrapper.active {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.custom-scrollbar::-webkit-scrollbar {
  height: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 99px;
}

/* =========================================================
   PRODUCT INFO — CỘT PHẢI
   ========================================================= */

/* Tag NEW ARRIVAL */
:deep(.el-tag--danger.el-tag--dark) {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
  font-family: "Inter", sans-serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.8px;
  padding: 4px 10px;
  border-radius: 99px;
}

/* Tên sản phẩm */
h2 {
  font-family: "Inter", sans-serif;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.3;
  letter-spacing: -0.5px;
}

/* SKU */
.text-muted.small {
  font-size: 12px;
  color: #94a3b8 !important;
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* Tags sản phẩm */
:deep(.el-tag--warning.el-tag--light) {
  background: #fefce8;
  border-color: #fde047;
  color: #854d0e;
  font-family: "Inter", sans-serif;
  font-weight: 600;
  font-size: 11.5px;
  border-radius: 6px;
}

/* Divider */
:deep(.el-divider--horizontal) {
  margin: 16px 0;
  border-color: #e2e8f0;
}

/* =========================================================
   PRICE
   ========================================================= */
.price-display {
  font-family: "Inter", sans-serif;
  font-size: 2.25rem;
  font-weight: 800;
  color: #ef4444;
  letter-spacing: -1px;
  line-height: 1;
}

/* =========================================================
   VARIANT BUTTONS
   ========================================================= */
.variant-btn {
  border-radius: 10px !important;
  font-family: "Inter", sans-serif !important;
  font-weight: 600 !important;
  font-size: 13.5px !important;
  padding: 10px 18px !important;
  height: auto !important;
  line-height: 1.4 !important;
  transition: all 0.2s ease !important;
  letter-spacing: 0.1px;
}

.variant-btn:not(.el-button--primary) {
  background: #f8fafc !important;
  border-color: #e2e8f0 !important;
  color: #475569 !important;
}

.variant-btn:not(.el-button--primary):hover {
  background: #eff6ff !important;
  border-color: #bfdbfe !important;
  color: #2563eb !important;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.12);
}

.variant-btn.el-button--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb) !important;
  border-color: transparent !important;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.35) !important;
  transform: translateY(-1px);
}

/* =========================================================
   STOCK & CART BOX
   ========================================================= */
.stock-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 18px 20px;
}

.stock-label {
  font-family: "Inter", sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.6px;
}

:deep(.el-tag--success.el-tag--dark) {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border: none;
  font-family: "Inter", sans-serif;
  font-weight: 700;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 8px;
  letter-spacing: 0.2px;
}

:deep(.el-tag--danger.el-tag--dark[size="large"]) {
  background: linear-gradient(135deg, #f87171, #ef4444);
  border: none;
  border-radius: 8px;
  font-weight: 700;
}

/* Input số lượng */
:deep(.el-input-number) {
  border-radius: 10px;
  font-family: "Inter", sans-serif;
}

:deep(.el-input-number .el-input__inner) {
  font-family: "Inter", sans-serif;
  font-weight: 700;
  font-size: 15px;
  color: #0f172a;
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  background: #f1f5f9;
  border-color: #e2e8f0;
  color: #475569;
  transition: background 0.18s;
}

:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
  background: #e2e8f0;
  color: #0f172a;
}

/* Nút thêm giỏ hàng */
.add-to-cart-btn {
  font-family: "Inter", sans-serif !important;
  font-weight: 800 !important;
  font-size: 14px !important;
  letter-spacing: 1px !important;
  border-radius: 12px !important;
  height: 48px !important;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4) !important;
  transition: all 0.25s ease !important;
}

.add-to-cart-btn:not(:disabled):hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 10px 28px rgba(59, 130, 246, 0.5) !important;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%) !important;
}

.add-to-cart-btn:not(:disabled):active {
  transform: translateY(0) !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35) !important;
}

.add-to-cart-btn:disabled {
  background: #e2e8f0 !important;
  box-shadow: none !important;
  color: #94a3b8 !important;
}

/* =========================================================
   TABS — MÔ TẢ & THÔNG SỐ
   ========================================================= */
:deep(.el-card) {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.product-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #e2e8f0;
  height: 1px;
}

.product-tabs :deep(.el-tabs__item) {
  font-family: "Inter", sans-serif;
  font-weight: 600;
  font-size: 14.5px;
  color: #64748b;
  padding: 0 20px;
  height: 48px;
  transition: color 0.2s;
}

.product-tabs :deep(.el-tabs__item:hover) {
  color: #3b82f6;
}

.product-tabs :deep(.el-tabs__item.is-active) {
  color: #2563eb;
  font-weight: 700;
}

.product-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  height: 3px;
  border-radius: 99px;
}

/* Panel nội dung mô tả */
.product-tabs :deep(.el-tab-pane) .p-3 {
  font-family: "Inter", sans-serif;
  font-size: 14.5px;
  line-height: 1.9;
  color: #475569;
}

/* Bảng thông số */
:deep(.el-table) {
  font-family: "Inter", sans-serif;
  border-radius: 12px;
  overflow: hidden;
  font-size: 14px;
}

:deep(.el-table th) {
  background: #f8fafc !important;
  font-weight: 700;
  color: #374151;
  font-size: 13px;
  letter-spacing: 0.3px;
}

:deep(.el-table td) {
  color: #475569;
  font-weight: 500;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #f8fafc;
}

/* =========================================================
   SKELETON & EMPTY
   ========================================================= */
:deep(.el-skeleton__paragraph),
:deep(.el-skeleton__item) {
  border-radius: 8px;
}

:deep(.el-empty__description p) {
  font-family: "Inter", sans-serif;
  color: #94a3b8;
  font-size: 14px;
}
</style>