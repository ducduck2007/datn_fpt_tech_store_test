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

      <!-- [UPDATE] KHU VỰC BỘ LỌC (FILTER) -->
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

        <!-- [UPDATE] Hiển thị thông số kỹ thuật từ Description (hoặc attributes nếu có) -->
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

        <!-- [UPDATE] Cột thao tác (Edit/Delete) -->
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

    <!-- [UPDATE] DIALOG TẠO MỚI / CẬP NHẬT -->
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

        <!-- [UPDATE] Chọn Nhiều Danh Mục -->
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

        <!-- [UPDATE] Nhập Thuộc tính (Key - Value) -->
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

        <!-- [UPDATE] Upload Nhiều Ảnh -->
        <div class="col-12">
          <el-form-item label="Gallery Images">
            <input type="file" multiple accept="image/*" class="form-control" @change="onPickFiles" />
            <div class="mt-2 text-muted small" v-if="dlg.form.galleryImages.length > 0">
              Selected {{ dlg.form.galleryImages.length }} files.
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
import axios from 'axios'; // Dùng axios trực tiếp hoặc qua productsApi wrapper

const loading = ref(false);
const categories = ref([]);
const rows = ref([]);

// Filter params
const page = ref(0);
const totalElements = ref(0);
const keyword = ref("");
const categoryIds = ref([]); // [UPDATE] Đa chọn
const sortBy = ref("newest");

// --- Helper Functions ---
function normalizeProducts(list) {
  return (list || []).map((p) => ({
    ...p,
    // Fix đường dẫn ảnh nếu cần
    imageUrl: p.imageUrl ? (p.imageUrl.startsWith('http') ? p.imageUrl : `http://localhost:8080${p.imageUrl}`) : 'https://via.placeholder.com/150'
  }));
}

// --- API Calls ---
async function loadCategories() {
  try {
    const res = await categoriesApi.list(false);
    // Xử lý dữ liệu trả về tùy theo cấu trúc ApiResponse của bạn
    const data = res.data?.data || res.data || []; 
    categories.value = data;
  } catch (e) {
    console.error(e);
  }
}

async function load() {
  loading.value = true;
  try {
    // [UPDATE] Gọi API với các tham số mới của Backend Tuần 4
    // Chuẩn bị params
    const params = {
      page: page.value,
      keyword: keyword.value || undefined,
      sortBy: sortBy.value || undefined
    };
    
    // Xử lý list categoryIds -> chuỗi "1,2,3"
    if (categoryIds.value && categoryIds.value.length > 0) {
      params.categoryIds = categoryIds.value.join(',');
    }

    const res = await productsApi.list(params); // Đảm bảo api wrapper hỗ trợ truyền params object
    
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
  attributesList: [], // Danh sách tạm để nhập thuộc tính
  form: {
    name: "",
    sku: "",
    description: "",
    isVisible: true,
    categoryIds: [],
    galleryImages: [], // File objects
  },
});

function openCreateDialog() {
  dlg.isEdit = false;
  dlg.editId = null;
  dlg.attributesList = [{ name: "", value: "" }]; // Mặc định 1 dòng trống
  dlg.form = {
    name: "", sku: "", description: "", isVisible: true, 
    categoryIds: [], galleryImages: []
  };
  dlg.alert = "";
  dlg.open = true;
}

function onEdit(row) {
  // Logic hiển thị form edit (cần parse lại attributes nếu backend trả về JSON string)
  // Tạm thời chỉ load thông tin cơ bản
  dlg.isEdit = true;
  dlg.editId = row.id;
  dlg.form = {
    name: row.name,
    sku: row.sku,
    description: row.description, // Description này đã gộp text
    isVisible: row.isVisible,
    categoryIds: [], // Cần logic lấy category của sp (API detail)
    galleryImages: []
  };
  dlg.attributesList = []; // Reset attribute list vì nó đã gộp vào description
  dlg.open = true;
}

function addAttribute() {
  dlg.attributesList.push({ name: "", value: "" });
}

function removeAttribute(index) {
  dlg.attributesList.splice(index, 1);
}

function onPickFiles(e) {
  // [UPDATE] Hỗ trợ chọn nhiều file
  dlg.form.galleryImages = Array.from(e.target.files);
}

async function submitForm() {
  dlg.alert = "";
  if (!dlg.form.name || !dlg.form.sku) {
    dlg.alert = "Name and SKU are required.";
    return;
  }

  dlg.loading = true;
  try {
    // 1. Chuẩn bị FormData
    const formData = new FormData();
    formData.append("name", dlg.form.name);
    formData.append("sku", dlg.form.sku);
    formData.append("description", dlg.form.description || "");
    formData.append("isVisible", dlg.form.isVisible);

    // Append Category IDs
    dlg.form.categoryIds.forEach(id => {
      formData.append("categoryIds", id);
    });

    // Append Gallery Images
    dlg.form.galleryImages.forEach(file => {
      formData.append("galleryImages", file);
    });

    // [UPDATE] Chuyển đổi Attributes List -> JSON String
    const validAttrs = dlg.attributesList.filter(a => a.name && a.value);
    if (validAttrs.length > 0) {
      formData.append("attributes", JSON.stringify(validAttrs));
    }

    // 2. Gọi API
    if (dlg.isEdit) {
      // Gọi API PUT Update
      await axios.put(`http://localhost:8080/api/products/${dlg.editId}`, formData);
      toast("Product updated.", "success");
    } else {
      // Gọi API POST Create
      await axios.post(`http://localhost:8080/api/products`, formData);
      toast("Product created.", "success");
    }

    dlg.open = false;
    await load(); // Reload danh sách

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
```

---

### 🟢 Hướng dẫn Cập nhật API Wrapper (Quan trọng)

Để code Vue trên chạy được, bạn cần đảm bảo file `api/products.api.js` của bạn hỗ trợ truyền params.

Hãy mở file `api/products.api.js` và kiểm tra hàm `list`. Nó nên trông như thế này (sử dụng `axios`):

```javascript
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api'; // Hoặc cấu hình của bạn

export const productsApi = {
  // Hàm list nhận vào params object
  list(params) {
    return axios.get(`${BASE_URL}/products`, { params });
  },
  
  // Hàm create (nếu bạn dùng wrapper này trong submitForm)
  create(formData) {
    return axios.post(`${BASE_URL}/products`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  }
};