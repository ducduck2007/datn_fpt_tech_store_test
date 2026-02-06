<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Categories Management</div>
          <div class="muted">Manage product categories hierarchy</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="load" :loading="loading">Reload</el-button>
          <el-button type="primary" @click="openCreate">Add Category</el-button>
        </div>
      </div>

      <el-divider />

      <!-- Bảng Danh sách -->
      <el-table :data="rows" border :loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        
        <el-table-column label="Image" width="100">
          <template #default="{ row }">
            <el-image 
              style="width: 70px; height: 50px; border-radius: 4px"
              :src="fixImageUrl(row.imageUrl)" 
              :preview-src-list="[fixImageUrl(row.imageUrl)]"
              fit="cover" 
            >
              <template #error>
                <div class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="Name" min-width="180">
          <template #default="{ row }">
            <div class="fw-bold">{{ row.name }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="Description" min-width="200" />

        <el-table-column label="Parent Category" min-width="150">
          <template #default="{ row }">
            <el-tag v-if="row.parentName" effect="plain">{{ row.parentName }}</el-tag>
            <span v-else class="text-muted small">- Root -</span>
          </template>
        </el-table-column>

        <el-table-column label="Status" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
              {{ row.isActive ? "Active" : "Hidden" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="150" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">Edit</el-button>
            <el-popconfirm title="Hide this category?" @confirm="remove(row)">
              <template #reference>
                <el-button size="small" type="danger" plain>Delete</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- Phân trang -->
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

    <!-- Dialog Thêm / Sửa -->
    <el-dialog
      v-model="dlg.open"
      :title="dlg.mode === 'create' ? 'Create Category' : 'Update Category'"
      width="600px"
    >
      <el-alert v-if="dlg.alert" :title="dlg.alert" type="error" show-icon class="mb-3" />

      <el-form :model="dlg.form" label-position="top">
        <el-form-item label="Category Name" required>
          <el-input v-model="dlg.form.name" placeholder="Enter category name" />
        </el-form-item>

        <el-form-item label="Parent Category">
          <el-select v-model="dlg.form.parentId" placeholder="Select parent (optional)" clearable style="width: 100%">
            <el-option
              v-for="c in parentOptions"
              :key="c.id"
              :label="c.name"
              :value="c.id"
              :disabled="c.id === dlg.id" 
            />
            <!-- disabled: Không cho chọn chính mình làm cha -->
          </el-select>
        </el-form-item>

        <el-form-item label="Description">
          <el-input v-model="dlg.form.description" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="Category Image">
          <input type="file" accept="image/*" class="form-control" @change="onPickFile" />
          <div v-if="dlg.mode === 'edit' && !dlg.form.imageFile" class="mt-1 small text-muted">
            Leave empty to keep current image.
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open = false">Cancel</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="save">
          {{ dlg.mode === 'create' ? 'Create' : 'Update' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import axios from "axios";
import { toast } from "../../ui/toast";

// Cấu hình URL cơ sở (Sửa lại port nếu cần)
const BASE_URL = "http://localhost:8080/api/categories";

const loading = ref(false);
const rows = ref([]);
const parentOptions = ref([]); // Danh sách để chọn danh mục cha

const page = ref(0);
const totalElements = ref(0);

// --- Helper: Fix link ảnh ---
function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/150?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
}

// --- API Calls ---

// 1. Load danh sách (Có phân trang - dùng cho bảng)
async function load() {
  loading.value = true;
  try {
    // Gọi API paging mới làm ở CategoryController
    const res = await axios.get(`${BASE_URL}/paging`, {
      params: { page: page.value, size: 20 }
    });
    
    // Cấu trúc trả về: { success: true, data: { content: [], totalElements: ... } }
    const pageData = res.data?.data || {};
    rows.value = pageData.content || [];
    totalElements.value = pageData.totalElements || 0;
  } catch (e) {
    console.error(e);
    toast("Failed to load categories.", "error");
  } finally {
    loading.value = false;
  }
}

// 2. Load danh sách cha (Không phân trang - dùng cho Dropdown)
async function loadParents() {
  try {
    const res = await axios.get(BASE_URL, { params: { activeOnly: true } });
    parentOptions.value = res.data?.data || [];
  } catch (e) {
    console.error(e);
  }
}

function onPageChange(val) {
  page.value = val - 1;
  load();
}

// --- Create / Edit Logic ---
const dlg = reactive({
  open: false,
  mode: "create",
  loading: false,
  alert: "",
  id: null,
  form: {
    name: "",
    description: "",
    parentId: null,
    imageFile: null,
  },
});

function openCreate() {
  dlg.mode = "create";
  dlg.id = null;
  dlg.form = { name: "", description: "", parentId: null, imageFile: null };
  dlg.alert = "";
  dlg.open = true;
  loadParents(); // Reload list cha mới nhất
}

function openEdit(row) {
  dlg.mode = "edit";
  dlg.id = row.id;
  dlg.form = {
    name: row.name,
    description: row.description,
    parentId: row.parentId, // ID cha hiện tại
    imageFile: null, // Reset file input
  };
  dlg.alert = "";
  dlg.open = true;
  loadParents();
}

function onPickFile(e) {
  const file = e.target.files[0];
  if (file) {
    dlg.form.imageFile = file;
  }
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.name) {
    dlg.alert = "Category name is required.";
    return;
  }

  dlg.loading = true;
  try {
    // Dùng FormData để gửi file
    const formData = new FormData();
    formData.append("name", dlg.form.name);
    formData.append("description", dlg.form.description || "");
    
    if (dlg.form.parentId) {
      formData.append("parentId", dlg.form.parentId);
    }
    
    if (dlg.form.imageFile) {
      formData.append("imageFile", dlg.form.imageFile);
    }

    if (dlg.mode === "create") {
      await axios.post(BASE_URL, formData); // Tự động nhận diện Multipart
      toast("Category created successfully.", "success");
    } else {
      await axios.put(`${BASE_URL}/${dlg.id}`, formData);
      toast("Category updated successfully.", "success");
    }

    dlg.open = false;
    load(); // Refresh bảng
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Save failed";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

// --- Delete Logic ---
async function remove(row) {
  try {
    await axios.delete(`${BASE_URL}/${row.id}`);
    toast("Category hidden (soft deleted).", "success");
    load();
  } catch (e) {
    console.error(e);
    toast("Delete failed.", "error");
  }
}

onMounted(() => {
  load();
});
</script>

<style scoped>
.kicker { font-size: 12px; opacity: 0.75; font-weight: 900; text-transform: uppercase; }
.title { font-weight: 900; font-size: 18px; }
.muted { color: rgba(15, 23, 42, 0.62); font-size: 13px; }
.image-slot { display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399; }
</style>