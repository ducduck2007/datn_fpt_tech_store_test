<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Products Management</div>
          <div class="muted">
            Supports: Variants, Inventory, Multi-Category, Sort, Trash Bin, Tags
          </div>
        </div>
        <div class="d-flex gap-2">
          <el-radio-group v-model="viewMode" size="small" @change="load" style="margin-right: 10px;">
            <el-radio-button label="active">Active</el-radio-button>
            <el-radio-button label="trash">Trash Bin</el-radio-button>
          </el-radio-group>

          <el-button @click="load" :loading="loading" icon="Refresh">Reload</el-button>
          
          <el-button v-if="viewMode === 'active'" type="primary" icon="Plus" @click="openCreateDialog">Add product</el-button>
        </div>
      </div>

      <el-divider />

      <!-- KHU VỰC BỘ LỌC (Chỉ hiện khi ở chế độ Active) -->
      <div class="row g-3" v-if="viewMode === 'active'">
        <div class="col-12 col-md-2">
          <el-input
            v-model="keyword"
            placeholder="Search by Name/SKU..."
            clearable
            @clear="onFilter"
            @keyup.enter="onFilter"
          >
            <template #append>
              <el-button @click="onFilter" icon="Search"></el-button>
            </template>
          </el-input>
        </div>

        <div class="col-12 col-md-2">
          <el-select
            v-model="categoryIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            clearable
            placeholder="Categories"
            @change="onFilter"
            style="width: 100%"
          >
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>

        <div class="col-12 col-md-2">
          <el-select
            v-model="filterTagId"
            clearable
            placeholder="Filter by Tag"
            @change="onFilter"
            style="width: 100%"
          >
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </div>

        <div class="col-12 col-md-3">
          <el-select v-model="sortBy" placeholder="Sort by" @change="onFilter" style="width: 100%">
            <el-option label="Newest (Mới nhất)" value="newest" />
            <el-option label="Oldest (Cũ nhất)" value="oldest" />
            <el-option label="Best Selling (Bán chạy)" value="best_selling" />
            <el-option label="Price: Low -> High" value="price_asc" />
            <el-option label="Price: High -> Low" value="price_desc" />
            <el-option label="Name: A -> Z" value="name_asc" />
            <el-option label="Name: Z -> A" value="name_desc" />
          </el-select>
        </div>

        <div class="col-12 col-md-3 d-flex align-items-center">
          <el-checkbox v-model="inStockOnly" @change="onFilter" border>
            <span class="text-success fw-bold">Only Show In Stock</span>
          </el-checkbox>
        </div>
      </div>

      <el-divider v-if="viewMode === 'active'" />

      <!-- DANH SÁCH SẢN PHẨM -->
      <el-table :data="rows" border :loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        
        <el-table-column label="Image" width="100" align="center">
          <template #default="{ row }">
            <el-image 
              style="width: 70px; height: 50px; border-radius: 4px; border: 1px solid #eee"
              :src="row.imageUrl" 
              :preview-src-list="[row.imageUrl]"
              fit="cover" 
            />
          </template>
        </el-table-column>

        <el-table-column prop="name" label="Product Info" min-width="250">
          <template #default="{ row }">
            <div class="fw-bold text-primary">{{ row.name }}</div>
            <div class="small text-muted mb-1">SKU: {{ row.sku }}</div>
            
            <div v-if="row.tags && row.tags.length > 0" class="mb-1">
              <el-tag v-for="(tagName, idx) in row.tags" :key="idx" size="small" type="warning" effect="dark" class="me-1">
                {{ tagName }}
              </el-tag>
            </div>
            
            <div class="d-flex gap-3 mt-1 align-items-center" style="font-size: 12px">
               <span v-if="row.minPrice" class="text-danger fw-bold">From: {{ formatCurrency(row.minPrice) }}</span>
               
               <el-tag :type="row.inStock ? 'success' : 'danger'" size="small" effect="plain" v-if="viewMode === 'active'">
                 {{ row.inStock ? `In Stock (${row.totalStock || 0})` : 'Out of Stock' }}
               </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Description" min-width="200">
          <template #default="{ row }">
            <div class="text-truncate-3" style="font-size: 12px; white-space: pre-wrap; color: #666">
              {{ row.description }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="isVisible" label="Status" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isVisible ? 'success' : 'info'" size="small">
              {{ row.isVisible ? "Active" : "In Trash" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <!-- CHẾ ĐỘ HOẠT ĐỘNG: Đầy đủ chức năng -->
            <div v-if="viewMode === 'active'">
              <el-button type="success" link size="small" icon="Connection" @click="openVariantDrawer(row)">Variants</el-button>
              <el-button type="primary" link size="small" icon="Edit" @click="onEdit(row)">Edit</el-button>
              <el-popconfirm title="Move to Trash Bin?" @confirm="onDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small" icon="Delete">Delete</el-button>
                </template>
              </el-popconfirm>
            </div>

            <!-- CHẾ ĐỘ THÙNG RÁC:-->
            <div v-else>
              <el-button 
                type="warning" 
                size="small" 
                icon="RefreshLeft" 
                @click="onRestore(row.id)"
                style="font-weight: bold"
              >
                Restore Product
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="d-flex justify-content-center mt-4">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :page-size="20"
          :total="totalElements"
          :current-page="page + 1"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <!-- DRAWER VARIANTS -->
    <el-drawer v-model="vr.open" :title="'Manage Variants: ' + vr.productName" size="50%" destroy-on-close>
      <div class="mb-4">
        <h6 class="fw-bold mb-3">Existing Variants</h6>
        <el-table :data="vr.variants" border size="small" v-loading="vr.loading">
          <el-table-column prop="variantName" label="Name" min-width="150" />
          <el-table-column prop="sku" label="SKU" width="120" />
          <el-table-column label="Price / Stock" width="160">
            <template #default="{ row }">
              <div class="text-danger fw-bold">{{ formatCurrency(row.price) }}</div>
              <div class="small">Stock: <span :class="row.stockQuantity > 0 ? 'text-success' : 'text-danger'">{{ row.stockQuantity }}</span></div>
            </template>
          </el-table-column>
          <el-table-column label="Actions" width="120" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="editVariant(row)">Edit</el-button>
              <el-popconfirm title="Delete variant?" @confirm="deleteVariant(row.id)">
                <template #reference><el-button type="danger" link size="small">Delete</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-divider />

      <div>
        <h6 class="fw-bold mb-3 text-primary">{{ vr.isEdit ? 'Update Variant' : 'Add New Variant' }}</h6>
        <el-form :model="vr.form" label-position="top" class="row g-2">
          <div class="col-md-6"><el-form-item label="Variant Name" required><el-input v-model="vr.form.variantName" placeholder="e.g. Red, 16GB..."/></el-form-item></div>
          <div class="col-md-6"><el-form-item label="SKU" required><el-input v-model="vr.form.sku" /></el-form-item></div>
          <div class="col-md-6"><el-form-item label="Price (VND)" required><el-input-number v-model="vr.form.price" style="width: 100%" :min="0"/></el-form-item></div>
          <div class="col-md-6"><el-form-item label="Stock Quantity" required><el-input-number v-model="vr.form.stockQuantity" style="width: 100%" :min="0"/></el-form-item></div>
          
          <div class="col-12 mt-2">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <label class="small fw-bold">Dynamic Attributes</label>
              <el-button size="small" icon="Plus" @click="addVariantAttr">Add</el-button>
            </div>
            <div v-for="(attr, index) in vr.attrsList" :key="index" class="d-flex gap-2 mb-2">
              <el-input v-model="attr.key" placeholder="Key (e.g. Color)" size="small" />
              <el-input v-model="attr.value" placeholder="Value" size="small" />
              <el-button type="danger" icon="Delete" circle size="small" @click="removeVariantAttr(index)" />
            </div>
          </div>

          <div class="col-12 mt-3 text-end">
            <el-button v-if="vr.isEdit" @click="resetVariantForm">Cancel</el-button>
            <el-button type="primary" :loading="vr.saving" @click="saveVariant">
              {{ vr.isEdit ? 'Save Changes' : 'Add Variant' }}
            </el-button>
          </div>
        </el-form>
      </div>
    </el-drawer>

    <!-- DIALOG PRODUCT -->
    <el-dialog v-model="dlg.open" :title="dlg.isEdit ? 'Update Product' : 'Create Product'" width="850px">
      <el-alert v-if="dlg.alert" :title="dlg.alert" type="error" show-icon class="mb-3" />

      <el-form :model="dlg.form" label-position="top" class="row g-3">
        <div class="col-md-6"><el-form-item label="Product Name" required><el-input v-model="dlg.form.name" /></el-form-item></div>
        <div class="col-md-6"><el-form-item label="Master SKU" required><el-input v-model="dlg.form.sku" /></el-form-item></div>

        <div class="col-md-6">
          <el-form-item label="Categories">
            <el-select v-model="dlg.form.categoryIds" multiple placeholder="Select categories" style="width: 100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </div>

        <div class="col-md-6">
          <el-form-item label="Campaign Tags">
            <el-select v-model="dlg.form.tagIds" multiple placeholder="Select tags" style="width: 100%">
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
        </div>

        <div class="col-md-12">
          <el-form-item label="Description">
            <el-input v-model="dlg.form.description" type="textarea" :rows="3" />
          </el-form-item>
        </div>

        <div class="col-12">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <label class="fw-bold">Specifications (Attributes)</label>
            <el-button size="small" icon="Plus" @click="addAttribute">Add Spec</el-button>
          </div>
          <div v-for="(attr, index) in dlg.attributesList" :key="index" class="d-flex gap-2 mb-2">
            <el-input v-model="attr.name" placeholder="Label (e.g. RAM)" />
            <el-input v-model="attr.value" placeholder="Value" />
            <el-button type="danger" icon="Delete" circle @click="removeAttribute(index)" />
          </div>
        </div>

        <!-- Gallery -->
        <div class="col-12" v-if="dlg.isEdit && dlg.existingImages.length > 0">
          <label class="fw-bold mb-2">Image Gallery</label>
          <div class="d-flex gap-2 flex-wrap p-3 border rounded bg-light">
            <div v-for="img in dlg.existingImages" :key="img.id" class="position-relative text-center" style="width: 110px;">
              <el-image 
                :src="fixImageUrl(img.url)" 
                style="width: 100px; height: 100px; border-radius: 8px; border: 2px solid"
                :style="{ borderColor: img.isPrimary ? '#67c23a' : '#dcdfe6' }"
                fit="cover"
              />
              <div class="d-flex justify-content-center gap-2 mt-2">
                 <el-button v-if="!img.isPrimary" type="warning" icon="Star" circle size="small" @click="setPrimaryImage(img.id)" />
                 <el-button type="danger" icon="Delete" circle size="small" @click="markImageForDelete(img.id)" />
              </div>
              <div v-if="img.isPrimary" class="position-absolute top-0 start-0 bg-success text-white px-2 small rounded-start">MAIN</div>
            </div>
          </div>
        </div>

        <div class="col-12">
          <el-form-item label="Upload New Media">
            <input type="file" multiple accept="image/*" class="form-control" @change="onPickFiles" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open = false">Cancel</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="submitForm">
          {{ dlg.isEdit ? 'Update Product' : 'Create Product' }}
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

const BASE_URL_API = 'http://localhost:8080/api/products';

const loading = ref(false);
const categories = ref([]);
const rows = ref([]);
const tags = ref([]);
const filterTagId = ref(null);
const viewMode = ref('active'); 
const page = ref(0);
const totalElements = ref(0);
const keyword = ref("");
const categoryIds = ref([]); 
const sortBy = ref("newest");
const inStockOnly = ref(false);

function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/150?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
}

function normalizeProducts(list) {
  return (list || []).map((p) => ({ ...p, imageUrl: fixImageUrl(p.imageUrl) }));
}

function formatCurrency(val) {
  if (!val) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

async function loadCategories() {
  try {
    const res = await categoriesApi.list(false);
    categories.value = res.data?.data || res.data || []; 
  } catch (e) { console.error(e); }
}

async function loadTags() {
  try {
    const res = await axios.get('http://localhost:8080/api/tags');
    tags.value = res.data?.data || res.data || [];
  } catch (e) { console.error("Lỗi lấy tag:", e); }
}

async function load() {
  loading.value = true;
  try {
    let res;
    if (viewMode.value === 'trash') {
        res = await axios.get(`${BASE_URL_API}/trash`, { params: { page: page.value } });
    } else {
        const params = {
          page: page.value,
          keyword: keyword.value || undefined,
          sortBy: sortBy.value || undefined,
          inStockOnly: inStockOnly.value,
          tagId: filterTagId.value || undefined
        };
        if (categoryIds.value?.length > 0) params.categoryIds = categoryIds.value.join(',');
        res = await productsApi.list(params); 
    }

    const pageData = res.data?.data || res.data;
    rows.value = normalizeProducts(pageData.content || []);
    totalElements.value = pageData.totalElements || 0;
  } catch (e) {
    toast("Failed to load products.", "error");
  } finally {
    loading.value = false;
  }
}

function onFilter() { page.value = 0; load(); }
function onPageChange(val) { page.value = val - 1; load(); }

// --- Variant Logic ---
const vr = reactive({
  open: false, productId: null, productName: "", variants: [], loading: false, saving: false, isEdit: false, editId: null,
  form: { variantName: "", sku: "", price: 0, stockQuantity: 0, isActive: true }, attrsList: [{key: "", value: ""}] 
});

async function openVariantDrawer(row) { 
  vr.productId = row.id; vr.productName = row.name; vr.open = true; 
  resetVariantForm(); 
  await loadVariants(); 
}

async function loadVariants() { 
  vr.loading = true; 
  try { 
    const res = await axios.get(`${BASE_URL_API}/${vr.productId}/variants`); 
    vr.variants = res.data || []; 
  } catch(e) { toast("Load variants failed", "error"); } 
  vr.loading = false; 
}

function resetVariantForm() { 
  vr.isEdit = false; vr.editId = null; 
  vr.form = { variantName: "", sku: "", price: 0, stockQuantity: 0, isActive: true }; 
  vr.attrsList = [{key: "", value: ""}]; 
}

function addVariantAttr() { vr.attrsList.push({key: "", value: ""}); }
function removeVariantAttr(index) { vr.attrsList.splice(index, 1); }

function editVariant(row) {
  vr.isEdit = true; vr.editId = row.id; 
  vr.form = { variantName: row.variantName, sku: row.sku, price: row.price, stockQuantity: row.stockQuantity, isActive: row.isActive };
  try { 
    const obj = JSON.parse(row.attributesJson || '{}'); 
    const keys = Object.keys(obj); 
    vr.attrsList = keys.length > 0 ? keys.map(k => ({key: k, value: obj[k]})) : [{key: "", value: ""}];
  } catch { vr.attrsList = [{key: "", value: ""}]; }
}

async function saveVariant() {
  if(!vr.form.variantName || !vr.form.sku) return toast("Fill required fields", "warning");
  vr.saving = true;
  try {
    const attrObj = {}; 
    vr.attrsList.forEach(item => { if(item.key && item.value) attrObj[item.key.trim()] = item.value.trim(); });
    const payload = { ...vr.form, attributesJson: Object.keys(attrObj).length > 0 ? JSON.stringify(attrObj) : null };
    
    if(vr.isEdit) await axios.put(`${BASE_URL_API}/variants/${vr.editId}`, payload);
    else await axios.post(`${BASE_URL_API}/${vr.productId}/variants`, payload);
    
    toast("Success", "success");
    resetVariantForm(); await loadVariants(); load(); 
  } catch { toast("Failed", "error"); }
  vr.saving = false;
}

async function deleteVariant(id) { 
  try { await axios.delete(`${BASE_URL_API}/variants/${id}`); toast("Deleted", "success"); await loadVariants(); load(); } 
  catch { toast("Failed", "error"); } 
}

// --- Product Actions ---
async function onRestore(id) { 
  try { await axios.put(`${BASE_URL_API}/${id}/restore`); toast("Restored!", "success"); load(); } 
  catch { toast("Restore failed", "error"); } 
}

async function onHardDelete(id) { 
  try { await axios.delete(`${BASE_URL_API}/${id}/hard`); toast("Deleted permanently", "success"); load(); } 
  catch { toast("Failed", "error"); } 
}

async function onDelete(id) { 
  try { await axios.delete(`${BASE_URL_API}/${id}`); toast("Moved to Trash.", "success"); load(); } 
  catch { toast("Delete failed.", "error"); } 
}

async function setPrimaryImage(imgId) { 
  try { 
    await axios.put(`${BASE_URL_API}/${dlg.editId}/images/${imgId}/primary`); 
    toast("Updated Primary Image", "success"); 
    const res = await productsApi.get(dlg.editId); 
    dlg.existingImages = (res.data?.data || res.data).images || []; 
  } catch { toast("Failed", "error"); } 
}

// --- Dialog Management ---
const dlg = reactive({
  open: false, isEdit: false, loading: false, alert: "", editId: null, attributesList: [], existingImages: [], idsToDelete: [], 
  form: { name: "", sku: "", description: "", isVisible: true, categoryIds: [], galleryImages: [], tagIds: [] },
});

function openCreateDialog() {
  dlg.isEdit = false; dlg.editId = null; dlg.attributesList = [{ name: "", value: "" }]; dlg.existingImages = []; dlg.idsToDelete = []; 
  dlg.form = { name: "", sku: "", description: "", isVisible: true, categoryIds: [], galleryImages: [], tagIds: [] };
  dlg.alert = ""; dlg.open = true;
}

async function onEdit(row) {
  dlg.isEdit = true; dlg.editId = row.id; dlg.idsToDelete = []; dlg.existingImages = []; dlg.open = true; dlg.loading = true; 
  try {
    const res = await productsApi.get(row.id);
    const data = res.data?.data || res.data;
    dlg.form = {
      name: data.name, sku: data.sku, description: data.description, isVisible: data.isVisible,
      categoryIds: data.categoryId ? [data.categoryId] : [], galleryImages: [], tagIds: []
    };
    if (data.tags?.length > 0) {
       dlg.form.tagIds = data.tags.map(name => tags.value.find(t => t.name === name)?.id).filter(id => id);
    }
    dlg.existingImages = data.images || [];
    try { 
      const attrs = JSON.parse(data.attributes); 
      dlg.attributesList = Array.isArray(attrs) ? attrs : []; 
    } catch { dlg.attributesList = []; }
  } catch { toast("Load details failed", "error"); } 
  finally { dlg.loading = false; }
}

function markImageForDelete(imageId) { 
  dlg.idsToDelete.push(imageId); 
  dlg.existingImages = dlg.existingImages.filter(img => img.id !== imageId); 
}
function addAttribute() { dlg.attributesList.push({ name: "", value: "" }); }
function removeAttribute(index) { dlg.attributesList.splice(index, 1); }
function onPickFiles(e) { dlg.form.galleryImages = Array.from(e.target.files); }

async function submitForm() {
  if (!dlg.form.name || !dlg.form.sku) return (dlg.alert = "Name/SKU required");
  dlg.loading = true;
  try {
    const formData = new FormData();
    formData.append("name", dlg.form.name); 
    formData.append("sku", dlg.form.sku); 
    formData.append("description", dlg.form.description || ""); 
    formData.append("isVisible", dlg.form.isVisible);
    
    dlg.form.categoryIds.forEach(id => formData.append("categoryIds", id));
    dlg.form.galleryImages.forEach(file => formData.append("galleryImages", file));
    dlg.idsToDelete.forEach(id => formData.append("idsToDelete", id));
    dlg.form.tagIds.forEach(id => formData.append("tagIds", id));

    const validAttrs = dlg.attributesList.filter(a => a.name && a.value);
    if (validAttrs.length > 0) formData.append("attributes", JSON.stringify(validAttrs));

    if (dlg.isEdit) await axios.put(`${BASE_URL_API}/${dlg.editId}`, formData); 
    else await axios.post(`${BASE_URL_API}`, formData);

    dlg.open = false; await load(); 
    toast("Success", "success");
  } catch (e) { 
    dlg.alert = e?.response?.data?.message || "Operation failed"; 
  } finally { dlg.loading = false; }
}

onMounted(async () => {
  await loadCategories();
  await loadTags();
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
.kicker { font-size: 11px; font-weight: 800; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; }
.title { font-weight: 800; font-size: 20px; color: #1e293b; }
.muted { color: #64748b; font-size: 13px; margin-top: 2px; }
</style>