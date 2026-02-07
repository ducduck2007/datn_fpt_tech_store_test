<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Products Management</div>
          <div class="muted">
            Supports: Search, Multi-Category Filter, Sort, Soft Delete
          </div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="load" :loading="loading">Reload</el-button>
          <el-button type="primary" @click="openCreateDialog">Add product</el-button>
        </div>
      </div>

      <el-divider />

      <!-- KHU VỰC BỘ LỌC (FILTER) -->
      <div class="row g-3">
        <!-- 1. Ô Tìm kiếm (Search) -->
        <div class="col-12 col-md-4">
          <el-input
            v-model="keyword"
            placeholder="Search by Name, SKU, Attributes..."
            clearable
            @clear="onFilter"
            @keyup.enter="onFilter"
          >
            <template #append>
              <el-button @click="onFilter">Search</el-button>
            </template>
          </el-input>
        </div>

        <!-- 2. Chọn Nhiều Danh mục (Multi-Select) -->
        <div class="col-12 col-md-4">
          <el-select
            v-model="categoryIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            clearable
            placeholder="Filter by categories"
            @change="onFilter"
            style="width: 100%"
          >
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </div>

        <!-- 3. Sắp xếp (Sort) -->
        <div class="col-12 col-md-4">
          <el-select v-model="sortBy" placeholder="Sort by" @change="onFilter" style="width: 100%">
            <el-option label="Newest" value="newest" />
            <el-option label="Oldest" value="oldest" />
            <el-option label="Name A-Z" value="name_asc" />
            <el-option label="Name Z-A" value="name_desc" />
          </el-select>
        </div>
      </div>

      <el-divider />

      <!-- DANH SÁCH SẢN PHẨM -->
      <el-table :data="rows" border :loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        
        <el-table-column label="Image" width="100">
          <template #default="{ row }">
            <el-image 
              style="width: 70px; height: 50px; border-radius: 4px"
              :src="row.imageUrl" 
              :preview-src-list="[row.imageUrl]"
              fit="cover" 
            />
          </template>
        </el-table-column>

        <el-table-column prop="name" label="Name" min-width="180">
          <template #default="{ row }">
            <div class="fw-bold">{{ row.name }}</div>
            <div class="small text-muted">{{ row.sku }}</div>
          </template>
        </el-table-column>

        <el-table-column label="Specs" min-width="200">
          <template #default="{ row }">
            <div class="text-truncate-3" style="font-size: 12px; white-space: pre-wrap;">
              {{ row.description }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="isVisible" label="Status" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isVisible ? 'success' : 'danger'" size="small">
              {{ row.isVisible ? "Active" : "Hidden" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="140" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="onEdit(row)">Edit</el-button>
            <el-popconfirm title="Hide this product?" @confirm="onDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">Delete</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="d-flex justify-content-center mt-3">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="20"
          :total="totalElements"
          :current-page="page + 1"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <!-- DIALOG TẠO MỚI / CẬP NHẬT -->
    <el-dialog v-model="dlg.open" :title="dlg.isEdit ? 'Update Product' : 'Create Product'" width="800px">
      <el-alert v-if="dlg.alert" :title="dlg.alert" type="error" show-icon class="mb-3" />

      <el-form :model="dlg.form" label-position="top" class="row g-3">
        <div class="col-md-6">
          <el-form-item label="Name" required>
            <el-input v-model="dlg.form.name" />
          </el-form-item>
        </div>
        <div class="col-md-6">
          <el-form-item label="SKU" required>
            <el-input v-model="dlg.form.sku" />
          </el-form-item>
        </div>

        <div class="col-md-6">
          <el-form-item label="Categories">
            <el-select v-model="dlg.form.categoryIds" multiple placeholder="Select categories" style="width: 100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </div>

        <div class="col-md-6">
          <el-form-item label="Visibility">
            <el-switch v-model="dlg.form.isVisible" active-text="Show" inactive-text="Hide" />
          </el-form-item>
        </div>

        <!-- Nhập Thuộc tính (Key - Value) -->
        <div class="col-12">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <label class="el-form-item__label m-0">Attributes (Specs)</label>
            <el-button size="small" @click="addAttribute">Add Attribute</el-button>
          </div>
          <div v-for="(attr, index) in dlg.attributesList" :key="index" class="d-flex gap-2 mb-2">
            <el-input v-model="attr.name" placeholder="Name (e.g. RAM)" style="flex: 1" />
            <el-input v-model="attr.value" placeholder="Value (e.g. 16GB)" style="flex: 1" />
            <el-button type="danger" icon="Delete" circle @click="removeAttribute(index)" />
          </div>
        </div>

        <div class="col-12">
          <el-form-item label="Description">
            <el-input v-model="dlg.form.description" type="textarea" :rows="3" placeholder="General description..." />
          </el-form-item>
        </div>

        <!-- [SỬA] Hiển thị và Xóa Ảnh Cũ (Gallery) -->
        <div class="col-12" v-if="dlg.isEdit && dlg.existingImages.length > 0">
          <label class="el-form-item__label">Current Images</label>
          <div class="d-flex gap-2 flex-wrap">
            <div v-for="img in dlg.existingImages" :key="img.id" class="position-relative" style="width: 100px; height: 100px">
              <el-image 
                :src="fixImageUrl(img.url)" 
                style="width: 100%; height: 100%; border-radius: 4px; border: 1px solid #ddd" 
                fit="cover"
              />
              <!-- Nút Xóa Ảnh: Gọi hàm markImageForDelete -->
              <el-button 
                type="danger" 
                icon="Delete" 
                circle 
                size="small" 
                class="position-absolute top-0 end-0 m-1"
                style="padding: 4px; min-height: auto;"
                @click="markImageForDelete(img.id)"
              />
              <!-- Nhãn MAIN cho ảnh chính -->
              <div v-if="img.isPrimary" class="position-absolute bottom-0 start-0 bg-success text-white px-1 small" style="font-size: 10px; border-radius: 0 4px 0 0">MAIN</div>
            </div>
          </div>
        </div>

        <!-- Upload Nhiều Ảnh Mới -->
        <div class="col-12">
          <el-form-item label="Upload New Images">
            <input type="file" multiple accept="image/*" class="form-control" @change="onPickFiles" />
            <div class="mt-2 text-muted small" v-if="dlg.form.galleryImages.length > 0">
              Selected {{ dlg.form.galleryImages.length }} new files.
            </div>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open = false">Cancel</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="submitForm">
          {{ dlg.isEdit ? 'Update' : 'Create' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { categoriesApi } from "../../api/categories.api";
import { productsApi } from "../../api/products.api";
import { toast } from "../../ui/toast";
import axios from 'axios'; 

const loading = ref(false);
const categories = ref([]);
const rows = ref([]);

// Filter params
const page = ref(0);
const totalElements = ref(0);
const keyword = ref("");
const categoryIds = ref([]); 
const sortBy = ref("newest");

// --- Helper Functions ---
function normalizeProducts(list) {
  return (list || []).map((p) => ({
    ...p,
    imageUrl: fixImageUrl(p.imageUrl)
  }));
}

function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/150?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
}

// --- API Calls ---
async function loadCategories() {
  try {
    const res = await categoriesApi.list(false);
    const data = res.data?.data || res.data || []; 
    categories.value = data;
  } catch (e) {
    console.error(e);
  }
}

async function load() {
  loading.value = true;
  try {
    const params = {
      page: page.value,
      keyword: keyword.value || undefined,
      sortBy: sortBy.value || undefined
    };
    
    if (categoryIds.value && categoryIds.value.length > 0) {
      params.categoryIds = categoryIds.value.join(',');
    }

    const res = await productsApi.list(params); 
    
    const pageData = res.data?.data || res.data;
    rows.value = normalizeProducts(pageData.content || []);
    totalElements.value = pageData.totalElements || 0;
    
  } catch (e) {
    console.error(e);
    toast("Failed to load products.", "error");
  } finally {
    loading.value = false;
  }
}

function onFilter() {
  page.value = 0;
  load();
}

function onPageChange(val) {
  page.value = val - 1;
  load();
}

// --- Create / Edit Dialog ---
const dlg = reactive({
  open: false,
  isEdit: false,
  loading: false,
  alert: "",
  editId: null,
  attributesList: [], 
  existingImages: [], // [THÊM] Chứa danh sách ảnh cũ lấy từ API
  idsToDelete: [],    // [THÊM] Chứa ID ảnh muốn xóa
  form: {
    name: "",
    sku: "",
    description: "",
    isVisible: true,
    categoryIds: [],
    galleryImages: [], 
  },
});

function openCreateDialog() {
  dlg.isEdit = false;
  dlg.editId = null;
  dlg.attributesList = [{ name: "", value: "" }];
  dlg.existingImages = []; // Reset ảnh cũ
  dlg.idsToDelete = [];    // Reset list xóa
  dlg.form = {
    name: "", sku: "", description: "", isVisible: true, 
    categoryIds: [], galleryImages: []
  };
  dlg.alert = "";
  dlg.open = true;
}

// [SỬA] Hàm Edit: Gọi API lấy chi tiết để hiển thị ảnh cũ
async function onEdit(row) {
  dlg.isEdit = true;
  dlg.editId = row.id;
  dlg.idsToDelete = [];
  dlg.existingImages = [];
  
  dlg.open = true;
  dlg.loading = true; 

  try {
    // Gọi API lấy chi tiết sản phẩm
    const res = await productsApi.get(row.id);
    const data = res.data?.data || res.data;

    dlg.form = {
      name: data.name,
      sku: data.sku,
      description: data.description, 
      isVisible: data.isVisible,
      // Fix: API trả về categoryId đơn hoặc list, ở đây giả sử đơn, cần xử lý thành mảng
      categoryIds: data.categoryId ? [data.categoryId] : [], 
      galleryImages: []
    };
    
    // [QUAN TRỌNG] Gán danh sách ảnh cũ từ API vào biến state
    dlg.existingImages = data.images || [];
    
    dlg.attributesList = []; 

  } catch(e) {
    console.error(e);
    toast("Failed to load details", "error");
  } finally {
    dlg.loading = false;
  }
}

// [SỬA] Hàm đánh dấu xóa ảnh
function markImageForDelete(imageId) {
  // Thêm ID vào danh sách cần xóa
  dlg.idsToDelete.push(imageId);
  // Ẩn ảnh đó khỏi giao diện ngay lập tức để người dùng thấy là đã xóa
  dlg.existingImages = dlg.existingImages.filter(img => img.id !== imageId);
}

function addAttribute() {
  dlg.attributesList.push({ name: "", value: "" });
}

function removeAttribute(index) {
  dlg.attributesList.splice(index, 1);
}

function onPickFiles(e) {
  dlg.form.galleryImages = Array.from(e.target.files);
}

// [SỬA] Hàm Submit: Gửi idsToDelete lên Server
async function submitForm() {
  dlg.alert = "";
  if (!dlg.form.name || !dlg.form.sku) {
    dlg.alert = "Name and SKU are required.";
    return;
  }

  dlg.loading = true;
  try {
    const formData = new FormData();
    formData.append("name", dlg.form.name);
    formData.append("sku", dlg.form.sku);
    formData.append("description", dlg.form.description || "");
    formData.append("isVisible", dlg.form.isVisible);

    dlg.form.categoryIds.forEach(id => {
      formData.append("categoryIds", id);
    });

    dlg.form.galleryImages.forEach(file => {
      formData.append("galleryImages", file);
    });

    // [QUAN TRỌNG] Gửi danh sách ID ảnh cần xóa lên Backend
    dlg.idsToDelete.forEach(id => {
      formData.append("idsToDelete", id);
    });

    const validAttrs = dlg.attributesList.filter(a => a.name && a.value);
    if (validAttrs.length > 0) {
      formData.append("attributes", JSON.stringify(validAttrs));
    }

    if (dlg.isEdit) {
      await axios.put(`http://localhost:8080/api/products/${dlg.editId}`, formData);
      toast("Product updated.", "success");
    } else {
      await axios.post(`http://localhost:8080/api/products`, formData);
      toast("Product created.", "success");
    }

    dlg.open = false;
    await load(); 

  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Operation failed";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

async function onDelete(id) {
  try {
    await axios.delete(`http://localhost:8080/api/products/${id}`);
    toast("Product hidden (soft deleted).", "success");
    await load();
  } catch (e) {
    toast("Delete failed.", "error");
  }
}

onMounted(async () => {
  await loadCategories();
  await load();
});
</script>

<style scoped>
.text-truncate-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.kicker { font-size: 12px; opacity: 0.75; font-weight: 900; text-transform: uppercase; }
.title { font-weight: 900; font-size: 18px; }
.muted { color: rgba(15, 23, 42, 0.62); font-size: 13px; }
</style>