<template>
  <div class="container-xl py-4">
    <!-- Breadcrumb -->
    <el-breadcrumb separator="/" class="mb-4">
      <el-breadcrumb-item :to="{ path: '/' }">Trang chủ</el-breadcrumb-item>
      <el-breadcrumb-item>Chi tiết sản phẩm</el-breadcrumb-item>
      <el-breadcrumb-item v-if="product" class="fw-bold">{{ product.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- Loading Skeleton -->
    <el-skeleton v-if="loading" :rows="10" animated />

    <!-- Product Content -->
    <div v-else-if="product" class="row g-4">
      <!-- Cột Trái: Thư viện ảnh -->
      <div class="col-12 col-md-5">
        <el-card shadow="never" class="border-0 bg-light text-center p-3 mb-3">
          <el-image 
            :src="mainImage" 
            :preview-src-list="galleryImages"
            fit="contain" 
            style="width: 100%; height: 350px; border-radius: 8px;"
          />
        </el-card>
        
        <!-- Ảnh Thumbnails -->
        <div class="d-flex gap-2 overflow-auto pb-2 custom-scrollbar">
          <div 
            v-for="(img, idx) in galleryImages" 
            :key="idx"
            class="thumbnail-wrapper"
            :class="{ 'active': mainImage === img }"
            @click="mainImage = img"
          >
            <el-image :src="img" fit="cover" style="width: 60px; height: 60px; border-radius: 6px;" />
          </div>
        </div>
      </div>

      <!-- Cột Phải: Thông tin & Đặt hàng -->
      <div class="col-12 col-md-7">
        <div class="d-flex align-items-center gap-2 mb-2">
          <el-tag v-if="product.isNew" type="danger" effect="dark" round>NEW ARRIVAL</el-tag>
          <span class="text-muted small">SKU: {{ product.sku }}</span>
        </div>
        
        <h2 class="fw-bold mb-3" style="color: #1e293b;">{{ product.name }}</h2>
        
        <div v-if="product.tags && product.tags.length > 0" class="mb-3">
           <el-tag v-for="(tag, idx) in product.tags" :key="idx" size="small" type="warning" effect="light" class="me-2">
             {{ tag }}
           </el-tag>
        </div>

        <el-divider border-style="dashed" />

        <!-- Hiển thị Giá (Lấy theo biến thể đang chọn) -->
        <div class="mb-4">
          <div class="text-danger fw-bolder" style="font-size: 2rem;">
            {{ formatPrice(selectedVariant ? selectedVariant.price : product.minPrice) }}
          </div>
        </div>

        <!-- Chọn Cấu hình / Biến thể -->
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

        <!-- Trạng thái Kho & Hành động mua -->
        <div class="bg-light p-3 rounded-3 mb-4 border">
          <div class="d-flex align-items-center justify-content-between mb-3">
            <span class="fw-bold text-muted">Trạng thái kho:</span>
            <el-tag :type="currentStock > 0 ? 'success' : 'danger'" effect="dark" size="large">
              {{ currentStock > 0 ? `Còn hàng (${currentStock} máy)` : 'Hết hàng' }}
            </el-tag>
          </div>

          <div class="d-flex gap-3 align-items-center mt-4">
            <div style="width: 130px;">
              <el-input-number v-model="quantity" :min="1" :max="currentStock > 0 ? currentStock : 1" :disabled="currentStock === 0" style="width: 100%" />
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

    <!-- Thông tin lỗi nếu không tìm thấy SP -->
    <el-empty v-else description="Không tìm thấy thông tin sản phẩm" />

    <!-- Phần Tab Mô tả & Thông số -->
    <div v-if="product" class="mt-5">
      <el-card shadow="never">
        <el-tabs v-model="activeTab" class="product-tabs">
          <el-tab-pane label="Đặc điểm nổi bật" name="description">
            <div class="p-3" style="white-space: pre-wrap; line-height: 1.8; color: #475569;">
              {{ product.description || 'Chưa có bài viết mô tả cho sản phẩm này.' }}
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="Thông số kỹ thuật" name="specs">
            <div class="p-3">
              <el-table :data="attributes" border stripe style="width: 100%; max-width: 600px;">
                <el-table-column prop="name" label="Thông số" width="200" class-name="fw-bold bg-light" />
                <el-table-column prop="value" label="Chi tiết" />
                <template #empty>
                  <el-empty description="Chưa cập nhật thông số kỹ thuật" :image-size="60" />
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
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { productsApi } from '../../api/products.api';
import { useCartStore } from '../../stores/cart';
import { toast } from '../../ui/toast';

const route = useRoute();
const cartStore = useCartStore();

const loading = ref(true);
const addingToCart = ref(false);
const product = ref(null);
const variants = ref([]);
const attributes = ref([]);
const galleryImages = ref([]);
const mainImage = ref('');
const selectedVariant = ref(null);
const quantity = ref(1);
const activeTab = ref('description');

// Tính toán tồn kho dựa trên biến thể đang chọn
const currentStock = computed(() => {
  if (!selectedVariant.value) return 0;
  return selectedVariant.value.stockQuantity || 0;
});

// Hàm format tiền tệ
function formatPrice(val) {
  if (val == null) return 'Liên hệ';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

// Xử lý đường dẫn ảnh tuyệt đối (Chống lỗi ảnh hỏng)
function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/600?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
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
      const sortedImages = [...data.images].sort((a, b) => (b.isPrimary ? 1 : 0) - (a.isPrimary ? 1 : 0));
      galleryImages.value = sortedImages.map(img => fixImageUrl(img.url));
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
          attributes.value = Object.keys(parsed).map(key => ({ name: key, value: parsed[key] }));
        }
      } catch (e) {
        attributes.value = [];
      }
    }

    // Lấy danh sách Biến thể (Variants) - Nếu API getById trả về kèm variants thì lấy luôn, nếu không thì gọi API rời (dự phòng)
    if (data.variants && data.variants.length > 0) {
        variants.value = data.variants.filter(v => v.isActive);
    } else {
        // Fallback: Nếu backend ko nhúng sẵn variant vào product detail, ta gọi lấy rời (giống bên Admin)
        const varRes = await fetch(`http://localhost:8080/api/products/${productId}/variants`).then(r => r.json());
        variants.value = (varRes || []).filter(v => v.isActive);
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
.thumbnail-wrapper {
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  padding: 2px;
  transition: all 0.2s ease;
}
.thumbnail-wrapper:hover {
  border-color: #cbd5e1;
}
.thumbnail-wrapper.active {
  border-color: #3b82f6; /* Màu xanh khi được chọn */
}
.custom-scrollbar::-webkit-scrollbar {
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}
.variant-btn {
  border-radius: 8px;
  font-weight: 600;
  padding: 10px 20px;
  height: auto;
}
.add-to-cart-btn {
  font-weight: bold;
  letter-spacing: 0.5px;
  border-radius: 8px;
}
.product-tabs :deep(.el-tabs__item) {
  font-weight: 600;
  font-size: 15px;
}
</style>