<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Quản trị</div>
          <div class="title">Quản lý danh mục</div>
          <div class="muted">Quản lý cấu trúc danh mục sản phẩm</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="load" :loading="loading">Tải lại</el-button>
          <el-button type="primary" @click="openCreate">Thêm danh mục</el-button>
        </div>
      </div>

      <el-divider />

      <el-table 
        :data="treeData" 
        row-key="id" 
        border 
        :loading="loading" 
        style="width: 100%"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="name" label="Tên" min-width="220">
          <template #default="{ row }">
            <div class="fw-bold">{{ row.name }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID" width="70" align="center" />
        
        <el-table-column label="Image" width="100" align="center">
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

        <el-table-column prop="description" label="Mô tả" min-width="200" />

        <el-table-column label="Danh mục cha" min-width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.parentId" effect="plain">Thuộc ID: {{ row.parentId }}</el-tag>
            <span v-else class="text-muted small">- Gốc -</span>
          </template>
        </el-table-column>

        <el-table-column label="Trạng thái" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
              {{ row.isActive ? "Hoạt động" : "Ẩn" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="150" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">Sửa</el-button>
            <el-popconfirm title="Ẩn danh mục này?" @confirm="remove(row)">
              <template #reference>
                <el-button size="small" type="danger" plain>Xóa</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

    </el-card>

    <el-dialog
      v-model="dlg.open"
      :title="dlg.mode === 'create' ? 'Tạo danh mục' : 'Cập nhật danh mục'"
      width="600px"
      destroy-on-close
    >
      <el-alert v-if="dlg.alert" :title="dlg.alert" type="error" show-icon class="mb-3" />

      <el-form :model="dlg.form" label-position="top">
        <el-form-item label="Tên danh mục" required>
          <el-input v-model="dlg.form.name" placeholder="Nhập tên danh mục" />
        </el-form-item>

        <el-form-item label="Danh mục cha">
          <el-select v-model="dlg.form.parentId" placeholder="Chọn danh mục cha (tùy chọn)" clearable style="width: 100%">
            <el-option
              v-for="c in parentOptions"
              :key="c.id"
              :label="c.name"
              :value="c.id"
              :disabled="c.id === dlg.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Mô tả">
          <el-input v-model="dlg.form.description" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="Ảnh danh mục">
          <input type="file" accept="image/*" class="form-control" @change="onPickFile" />
          <div v-if="dlg.mode === 'edit' && !dlg.form.imageFile" class="mt-1 small text-muted">
            Để trống để giữ ảnh hiện tại.
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open = false">Hủy</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="save">
          {{ dlg.mode === 'create' ? 'Tạo' : 'Cập nhật' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import axios from "axios";
import { toast } from "../../ui/toast";

const BASE_URL = "http://localhost:8080/api/categories";

const loading = ref(false);
const treeData = ref([]); 
const parentOptions = ref([]); 

// --- Helper: Fix link ảnh ---
function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/150?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
}

// --- Tree Builder Logic ---
function buildCategoryTree(flatList) {
  const map = {};
  const roots = [];

  flatList.forEach((item) => {
    map[item.id] = { ...item, children: [] };
  });

  flatList.forEach((item) => {
    const node = map[item.id];
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(node);
    } else {
      roots.push(node);
    }
  });

  Object.values(map).forEach((node) => {
    if (node.children.length === 0) {
      delete node.children;
    }
  });

  return roots;
}

// --- API Calls ---
async function load() {
  loading.value = true;
  try {
    // Lấy toàn bộ danh sách (không phân trang) để dựng cây
    const res = await axios.get(BASE_URL, { params: { activeOnly: false } });
    const flatList = res.data?.data || [];
    
    parentOptions.value = flatList;
    treeData.value = buildCategoryTree(flatList);
  } catch (e) {
    console.error(e);
    toast("Failed to load categories.", "error");
  } finally {
    loading.value = false;
  }
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
}

function openEdit(row) {
  dlg.mode = "edit";
  dlg.id = row.id;
  dlg.form = {
    name: row.name,
    description: row.description,
    parentId: row.parentId,
    imageFile: null, 
  };
  dlg.alert = "";
  dlg.open = true;
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
      await axios.post(BASE_URL, formData);
      toast("Category created successfully.", "success");
    } else {
      await axios.put(`${BASE_URL}/${dlg.id}`, formData);
      toast("Category updated successfully.", "success");
    }

    dlg.open = false;
    load(); 
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
/* Giữ nguyên 100% style gốc của bạn */
.kicker { font-size: 12px; opacity: 0.75; font-weight: 900; text-transform: uppercase; }
.title { font-weight: 900; font-size: 18px; }
.muted { color: rgba(15, 23, 42, 0.62); font-size: 13px; }
.image-slot { display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399; }
</style>