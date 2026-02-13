<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Products Management</div>
          <div class="muted">
            Supports: Search, Multi-Category Filter, Sort, Soft Delete & Trash
          </div>
        </div>
        <div class="d-flex gap-2">
          <!-- [MỚI] Bộ chuyển đổi: Danh sách chính <-> Thùng rác -->
          <el-radio-group v-model="viewMode" size="small" @change="load" style="margin-right: 10px;">
            <el-radio-button label="active">Active</el-radio-button>
            <el-radio-button label="trash">Trash Bin</el-radio-button>
          </el-radio-group>

          <el-button @click="load" :loading="loading">Reload</el-button>
          
          <!-- [MỚI] Chỉ hiện nút Add khi ở chế độ Active -->
          <el-button v-if="viewMode === 'active'" type="primary" @click="openCreateDialog">Add product</el-button>
        </div>
      </div>

      <el-divider />

      <!-- KHU VỰC BỘ LỌC (Chỉ hiện khi ở chế độ Active) -->
      <div class="row g-3" v-if="viewMode === 'active'">
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
            <el-option label="Newest (Mới nhất)" value="newest" />
            <el-option label="Oldest (Cũ nhất)" value="oldest" />
            <!-- [MỚI] Thêm các option sắp xếp tuần 5 -->
            <el-option label="Best Selling (Bán chạy)" value="best_selling" />
            <el-option label="Price: Low -> High" value="price_asc" />
            <el-option label="Price: High -> Low" value="price_desc" />
            <el-option label="Name: A -> Z" value="name_asc" />
            <el-option label="Name: Z -> A" value="name_desc" />
          </el-select>
        </div>
      </div>

      <el-divider v-if="viewMode === 'active'" />

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
            <!-- [MỚI] Hiển thị giá Min nếu có -->
            <div v-if="row.minPrice" class="small text-danger fw-bold">
               From: {{ formatCurrency(row.minPrice) }}
            </div>
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
              {{ row.isVisible ? "Active" : "Deleted" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="160" align="center">
          <template #default="{ row }">
            <!-- [MỚI] Chế độ Active: Hiện nút Sửa / Xóa mềm -->
            <div v-if="viewMode === 'active'">
              <el-button type="primary" link size="small" @click="onEdit(row)">Edit</el-button>
              <el-popconfirm title="Move to Trash?" @confirm="onDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small">Delete</el-button>
                </template>
              </el-popconfirm>
            </div>

            <!-- [MỚI] Chế độ Trash: Hiện nút Khôi phục / Xóa cứng -->
            <div v-else>
              <el-popconfirm title="Restore this product?" @confirm="onRestore(row.id)">
                <template #reference>
                  <el-button type="success" link size="small">Restore</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm title="Permanently delete?" @confirm="onHardDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small">Kill</el-button>
                </template>
              </el-popconfirm>
            </div>
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

        <!-- [MỚI] Hiển thị và Quản lý Gallery (Set Primary / Delete) -->
        <div class="col-12" v-if="dlg.isEdit && dlg.existingImages.length > 0">
          <label class="el-form-item__label">Gallery Manager</label>
          <div class="d-flex gap-2 flex-wrap p-2 border rounded bg-light">
            <div v-for="img in dlg.existingImages" :key="img.id" class="position-relative text-center" style="width: 110px;">
              <el-image 
                :src="fixImageUrl(img.url)" 
                style="width: 100px; height: 100px; border-radius: 4px; border: 2px solid"
                :style="{ borderColor: img.isPrimary ? '#67c23a' : '#dcdfe6' }"
                fit="cover"
              />
              
              <!-- Toolbar -->
              <div class="d-flex justify-content-between px-1 mt-1">
                 <!-- Nút Set Primary -->
                 <el-button 
                    v-if="!img.isPrimary"
                    type="warning" 
                    icon="Star" 
                    circle 
                    size="small" 
                    title="Set as Main"
                    @click="setPrimaryImage(img.id)"
                  />
                  
                  <!-- Nút Xóa -->
                  <el-button 
                    type="danger" 
                    icon="Delete" 
                    circle 
                    size="small" 
                    title="Delete"
                    @click="markImageForDelete(img.id)"
                  />
              </div>

              <!-- Nhãn MAIN -->
              <div v-if="img.isPrimary" class="position-absolute top-0 start-0 bg-success text-white px-1 small" style="font-size: 10px; border-radius: 4px 0 4px 0;">MAIN</div>
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

// [MỚI] Biến cấu hình URL API gốc (dùng cho các lệnh gọi axios trực tiếp)
const BASE_URL_API = 'http://localhost:8080/api/products';

const loading = ref(false);
const categories = ref([]);
const rows = ref([]);

// [MỚI] Biến viewMode: 'active' (DS chính) hoặc 'trash' (Thùng rác)
const viewMode = ref('active'); 

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

// [MỚI] Format tiền tệ VNĐ
function formatCurrency(val) {
  if (!val) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
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
    let res;
    
    // [MỚI] Logic phân luồng Active / Trash
    if (viewMode.value === 'trash') {
        // Gọi API thùng rác
        res = await axios.get(`${BASE_URL_API}/trash`, { params: { page: page.value } });
    } else {
        // Gọi API danh sách thường
        const params = {
          page: page.value,
          keyword: keyword.value || undefined,
          sortBy: sortBy.value || undefined
        };
        
        if (categoryIds.value && categoryIds.value.length > 0) {
          params.categoryIds = categoryIds.value.join(',');
        }
        res = await productsApi.list(params); 
    }

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

// --- [MỚI] Các hành động Thùng Rác / Restore ---
async function onRestore(id) {
    try {
        await axios.put(`${BASE_URL_API}/${id}/restore`);
        toast("Product restored!", "success");
        load();
    } catch(e) { toast("Restore failed", "error"); }
}

async function onHardDelete(id) {
    try {
        await axios.delete(`${BASE_URL_API}/${id}/hard`);
        toast("Product permanently deleted!", "success");
        load();
    } catch(e) { toast("Delete failed", "error"); }
}

// --- [MỚI] Hành động Gallery (Set Primary) ---
async function setPrimaryImage(imgId) {
    try {
        // Gọi API set primary
        await axios.put(`${BASE_URL_API}/${dlg.editId}/images/${imgId}/primary`);
        toast("Set as Main Image success", "success");
        // Reload lại form edit để cập nhật giao diện
        const res = await productsApi.get(dlg.editId);
        const data = res.data?.data || res.data;
        dlg.existingImages = data.images || [];
    } catch(e) {
        toast("Set primary failed", "error");
    }
}

// --- Create / Edit Dialog ---
const dlg = reactive({
  open: false,
  isEdit: false,
  loading: false,
  alert: "",
  editId: null,
  attributesList: [], 
  existingImages: [], // Chứa danh sách ảnh cũ lấy từ API
  idsToDelete: [],    // Chứa ID ảnh muốn xóa
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

// Hàm Edit: Gọi API lấy chi tiết
async function onEdit(row) {
  dlg.isEdit = true;
  dlg.editId = row.id;
  dlg.idsToDelete = [];
  dlg.existingImages = [];
  
  dlg.open = true;
  dlg.loading = true; 

  try {
    const res = await productsApi.get(row.id);
    const data = res.data?.data || res.data;

    dlg.form = {
      name: data.name,
      sku: data.sku,
      description: data.description, 
      isVisible: data.isVisible,
      categoryIds: data.categoryId ? [data.categoryId] : [], 
      galleryImages: []
    };
    
    // Gán danh sách ảnh cũ từ API
    dlg.existingImages = data.images || [];
    
    // Parse attributes từ JSON string
    if (data.attributes) {
      try {
        const attrs = JSON.parse(data.attributes);
        dlg.attributesList = Array.isArray(attrs) ? attrs : [];
      } catch {
        dlg.attributesList = [];
      }
    } else {
        dlg.attributesList = []; 
    }

  } catch(e) {
    console.error(e);
    toast("Failed to load details", "error");
  } finally {
    dlg.loading = false;
  }
}

// Hàm đánh dấu xóa ảnh
function markImageForDelete(imageId) {
  dlg.idsToDelete.push(imageId);
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

// Hàm Submit
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

    // Gửi danh sách ID ảnh cần xóa lên Backend
    dlg.idsToDelete.forEach(id => {
      formData.append("idsToDelete", id);
    });

    const validAttrs = dlg.attributesList.filter(a => a.name && a.value);
    if (validAttrs.length > 0) {
      formData.append("attributes", JSON.stringify(validAttrs));
    }

    if (dlg.isEdit) {
      await axios.put(`${BASE_URL_API}/${dlg.editId}`, formData);
      toast("Product updated.", "success");
    } else {
      await axios.post(`${BASE_URL_API}`, formData);
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
    // Gọi API xóa mềm (chuyển vào trash)
    await axios.delete(`${BASE_URL_API}/${id}`);
    toast("Moved to Trash.", "success");
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