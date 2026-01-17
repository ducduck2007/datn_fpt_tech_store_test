<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Products</div>
          <div class="muted">
            GET /api/products • POST /api/products (multipart/form-data)
          </div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="load" :loading="loading">Reload</el-button>
          <el-button type="primary" @click="dlg.open = true"
            >Add product</el-button
          >
        </div>
      </div>

      <el-divider />

      <div class="row g-3">
        <div class="col-12 col-md-4">
          <el-select
            v-model="categoryId"
            clearable
            placeholder="Filter by category"
            @change="onFilter"
          >
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </div>
      </div>

      <el-divider />

      <el-table :data="rows" border :loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="Name" min-width="220" />
        <el-table-column prop="sku" label="SKU" width="160" />
        <el-table-column prop="isVisible" label="Visible" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isVisible ? 'success' : 'info'" effect="light">
              {{ row.isVisible ? "YES" : "NO" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Image" width="140">
          <template #default="{ row }">
            <img
              :src="row.imageUrl"
              alt=""
              style="
                width: 110px;
                height: 64px;
                object-fit: cover;
                border-radius: 10px;
              "
            />
          </template>
        </el-table-column>
        <el-table-column prop="categoryId" label="categoryId" width="140" />
        <el-table-column
          prop="defaultVariantId"
          label="variantId"
          width="140"
        />
      </el-table>

      <div class="d-flex justify-content-center mt-3">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="1"
          :total="Math.max(1, totalPages)"
          :current-page="page + 1"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dlg.open" title="Create Product" width="820px">
      <el-alert
        v-if="dlg.alert"
        :title="dlg.alert"
        type="error"
        show-icon
        class="mb-3"
      />

      <el-form :model="dlg.form" label-position="top" class="row g-3">
        <div class="col-12 col-md-6">
          <el-form-item label="name">
            <el-input v-model="dlg.form.name" />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="sku">
            <el-input v-model="dlg.form.sku" />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="categoryId">
            <el-select
              v-model="dlg.form.categoryId"
              placeholder="Select category"
            >
              <el-option
                v-for="c in categories"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="isVisible">
            <el-switch v-model="dlg.form.isVisible" />
          </el-form-item>
        </div>

        <div class="col-12">
          <el-form-item label="description">
            <el-input
              v-model="dlg.form.description"
              type="textarea"
              :rows="2"
            />
          </el-form-item>
        </div>

        <div class="col-12">
          <el-form-item label="imageFile">
            <input
              type="file"
              accept="image/*"
              class="form-control"
              @change="onPickFile"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open = false">Cancel</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="create"
          >Create</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { categoriesApi } from "../../api/categories.api";
import { productsApi } from "../../api/products.api";
import { toast } from "../../ui/toast";

const loading = ref(false);

const categories = ref([]);
const rows = ref([]);

const page = ref(0);
const totalPages = ref(1);
const categoryId = ref(null);

function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  const root = payload?.data ?? payload;
  if (Array.isArray(root)) return root;
  for (const k of ["content", "items", "results", "rows", "list"]) {
    if (Array.isArray(root?.[k])) return root[k];
    if (Array.isArray(root?.data?.[k])) return root.data[k];
  }
  return [];
}

function pickTotalPages(payload) {
  const root = payload?.data ?? payload;
  return root?.totalPages ?? root?.data?.totalPages ?? 1;
}

function getDefaultVariantId(raw) {
  const v =
    raw?.defaultVariantId ??
    raw?.variantId ??
    raw?.variants?.[0]?.id ??
    raw?.variants?.[0]?.variantId ??
    raw?.variant?.id ??
    null;
  return v != null ? Number(v) : null;
}

function normalizeProducts(list) {
  return (list || []).map((p, idx) => ({
    id: p?.id ?? p?.productId ?? idx + 1,
    name: p?.name ?? p?.title ?? "",
    sku: p?.sku ?? p?.code ?? "",
    description: p?.description ?? "",
    isVisible: !!(p?.isVisible ?? p?.visible ?? true),
    categoryId: p?.categoryId ?? p?.category?.id ?? null,
    imageUrl:
      p?.imageUrl ||
      p?.thumbnailUrl ||
      `https://placehold.co/640x420/png?text=${encodeURIComponent(p?.name ?? "Product")}`,
    defaultVariantId: getDefaultVariantId(p),
    raw: p,
  }));
}

async function loadCategories() {
  try {
    const res = await categoriesApi.list(false);
    const list = extractList(res?.data);
    categories.value = (list || []).map((c, idx) => ({
      id: c?.id ?? c?.categoryId ?? idx + 1,
      name: c?.name ?? c?.title ?? "",
      raw: c,
    }));
  } catch {
    categories.value = [];
  }
}

async function load() {
  loading.value = true;
  try {
    const res = await productsApi.list({
      page: page.value,
      categoryId: categoryId.value ?? undefined,
    });
    totalPages.value = Math.max(1, Number(pickTotalPages(res?.data) || 1));
    rows.value = normalizeProducts(extractList(res?.data));
  } catch {
    rows.value = [];
    totalPages.value = 1;
    toast("Failed to load products.", "error");
  } finally {
    loading.value = false;
  }
}

function onPageChange(page1Based) {
  page.value = Math.max(0, Number(page1Based || 1) - 1);
  load();
}

function onFilter() {
  page.value = 0;
  load();
}

const dlg = reactive({
  open: false,
  loading: false,
  alert: "",
  form: {
    name: "",
    sku: "",
    description: "",
    isVisible: true,
    categoryId: null,
    imageFile: null,
  },
});

function onPickFile(e) {
  const f = e?.target?.files?.[0] || null;
  dlg.form.imageFile = f;
}

async function create() {
  dlg.alert = "";
  if (!dlg.form.name || !dlg.form.sku || !dlg.form.categoryId) {
    dlg.alert = "name, sku, categoryId are required.";
    return;
  }

  dlg.loading = true;
  try {
    await productsApi.create(dlg.form);
    toast("Product created.", "success");
    dlg.open = false;
    dlg.form = {
      name: "",
      sku: "",
      description: "",
      isVisible: true,
      categoryId: null,
      imageFile: null,
    };
    await load();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Create failed";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

onMounted(async () => {
  await loadCategories();
  await load();
});
</script>

<style scoped>
.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
}
.title {
  font-weight: 900;
  font-size: 18px;
}
.muted {
  color: rgba(15, 23, 42, 0.62);
  font-size: 13px;
}
</style>
