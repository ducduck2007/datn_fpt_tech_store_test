<!-- FILE: src/pages/system/CategoryManager.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Categories</div>
          <div class="muted">Base: /api/categories</div>
        </div>
        <div class="d-flex gap-2 align-items-center">
          <el-switch
            v-model="activeOnly"
            active-text="Active only"
            @change="load"
          />
          <el-button @click="load" :loading="loading">Reload</el-button>
        </div>
      </div>

      <el-divider />

      <el-form :model="form" label-position="top" class="row g-3">
        <div class="col-12 col-md-4">
          <el-form-item label="name">
            <el-input v-model="form.name" />
          </el-form-item>
        </div>
        <div class="col-12 col-md-4">
          <el-form-item label="displayOrder">
            <el-input-number v-model="form.displayOrder" :min="0" :max="9999" />
          </el-form-item>
        </div>
        <div class="col-12 col-md-4">
          <el-form-item label="isActive">
            <el-switch v-model="form.isActive" />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="imageUrl">
            <el-input v-model="form.imageUrl" placeholder="https://..." />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="parent (optional)">
            <el-select
              v-model="form.parentId"
              clearable
              placeholder="Select parent"
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

        <div class="col-12">
          <el-form-item label="description">
            <el-input v-model="form.description" type="textarea" :rows="2" />
          </el-form-item>
        </div>

        <div class="col-12 d-flex justify-content-end">
          <el-button type="primary" :loading="saving" @click="create"
            >Create</el-button
          >
        </div>
      </el-form>

      <el-divider />

      <el-table :data="categories" border :loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="Name" min-width="220" />
        <el-table-column prop="displayOrder" label="Order" width="110" />
        <el-table-column prop="isActive" label="Active" width="110">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'" effect="light">
              {{ row.isActive ? "YES" : "NO" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="imageUrl" label="ImageUrl" min-width="260" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { categoriesApi } from "../../api/categories.api";
import { toast } from "../../ui/toast";

const loading = ref(false);
const saving = ref(false);
const activeOnly = ref(false);

const categories = ref([]);

const form = reactive({
  name: "",
  description: "",
  imageUrl: "",
  displayOrder: 0,
  isActive: true,
  parentId: null,
});

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

function normalize(list) {
  return (list || []).map((c) => ({
    id: c?.id ?? c?.categoryId,
    name: c?.name ?? c?.title ?? "",
    description: c?.description ?? "",
    imageUrl: c?.imageUrl ?? "",
    displayOrder: c?.displayOrder ?? 0,
    isActive: !!c?.isActive,
    parent: c?.parent ?? null,
    raw: c,
  }));
}

async function load() {
  loading.value = true;
  try {
    const res = await categoriesApi.list(activeOnly.value);
    categories.value = normalize(extractList(res?.data));
  } catch {
    categories.value = [];
    toast("Failed to load categories.", "error");
  } finally {
    loading.value = false;
  }
}

async function create() {
  if (!form.name) return toast("name is required.", "warning");

  saving.value = true;
  try {
    const payload = {
      name: form.name,
      description: form.description,
      imageUrl: form.imageUrl,
      displayOrder: Number(form.displayOrder || 0),
      isActive: !!form.isActive,
    };
    if (form.parentId) payload.parent = { id: Number(form.parentId) };

    await categoriesApi.create(payload);
    toast("Category created.", "success");

    form.name = "";
    form.description = "";
    form.imageUrl = "";
    form.displayOrder = 0;
    form.isActive = true;
    form.parentId = null;

    await load();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Create failed";
    toast(typeof msg === "string" ? msg : JSON.stringify(msg), "error");
  } finally {
    saving.value = false;
  }
}

onMounted(load);
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
