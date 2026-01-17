<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Promotions</div>
          <div class="muted">Base: /api/promotions</div>
        </div>
        <div class="d-flex gap-2 align-items-center">
          <el-switch
            v-model="activeOnly"
            active-text="Active only"
            @change="load"
          />
          <el-button @click="load" :loading="loading">Reload</el-button>
          <el-button type="primary" @click="openCreate">Create</el-button>
        </div>
      </div>

      <el-divider />

      <el-table :data="rows" border :loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="code" label="Code" width="160" />
        <el-table-column prop="name" label="Name" min-width="220" />
        <el-table-column prop="discountType" label="Type" width="140" />
        <el-table-column prop="discountValue" label="Value" width="120" />
        <el-table-column prop="isActive" label="Active" width="110">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'" effect="light">{{
              row.isActive ? "YES" : "NO"
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">Edit</el-button>
            <el-button size="small" type="danger" plain @click="remove(row)"
              >Delete</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dlg.open"
      :title="dlg.mode === 'create' ? 'Create Promotion' : 'Update Promotion'"
      width="920px"
    >
      <el-alert
        v-if="dlg.alert"
        :title="dlg.alert"
        type="error"
        show-icon
        class="mb-3"
      />

      <el-form :model="dlg.form" label-position="top" class="row g-3">
        <div class="col-12 col-md-4">
          <el-form-item label="code">
            <el-input v-model="dlg.form.code" />
          </el-form-item>
        </div>
        <div class="col-12 col-md-8">
          <el-form-item label="name">
            <el-input v-model="dlg.form.name" />
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

        <div class="col-12 col-md-4">
          <el-form-item label="discountType">
            <el-select v-model="dlg.form.discountType">
              <el-option label="PERCENT" value="PERCENT" />
              <el-option label="FIXED" value="FIXED" />
            </el-select>
          </el-form-item>
        </div>
        <div class="col-12 col-md-4">
          <el-form-item label="discountValue">
            <el-input-number
              v-model="dlg.form.discountValue"
              :min="0"
              :max="999999999"
              class="w-100"
            />
          </el-form-item>
        </div>
        <div class="col-12 col-md-4">
          <el-form-item label="priority">
            <el-input-number
              v-model="dlg.form.priority"
              :min="0"
              :max="9999"
              class="w-100"
            />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="startDate">
            <el-date-picker
              v-model="dlg.form.startDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-100"
            />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="endDate">
            <el-date-picker
              v-model="dlg.form.endDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-100"
            />
          </el-form-item>
        </div>

        <div class="col-12 col-md-4">
          <el-form-item label="scope">
            <el-select v-model="dlg.form.scope">
              <el-option label="ALL" value="ALL" />
              <el-option label="PRODUCT" value="PRODUCT" />
              <el-option label="VARIANT" value="VARIANT" />
            </el-select>
          </el-form-item>
        </div>

        <div class="col-12 col-md-4">
          <el-form-item label="stackable">
            <el-switch v-model="dlg.form.stackable" />
          </el-form-item>
        </div>

        <div class="col-12 col-md-4">
          <el-form-item label="isActive">
            <el-switch v-model="dlg.form.isActive" />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="productIds (comma-separated)">
            <el-input v-model="dlg.form.productIdsText" placeholder="1,2,3" />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="variantIds (comma-separated)">
            <el-input
              v-model="dlg.form.variantIdsText"
              placeholder="10,11,12"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open = false">Cancel</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="save">
          {{ dlg.mode === "create" ? "Create" : "Update" }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { promotionsApi } from "../../api/promotions.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

const loading = ref(false);
const activeOnly = ref(false);
const rows = ref([]);

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
  return (list || []).map((p) => ({
    id: p?.id ?? p?.promotionId,
    code: p?.code ?? "",
    name: p?.name ?? "",
    description: p?.description ?? "",
    discountType: p?.discountType ?? "",
    discountValue: p?.discountValue ?? 0,
    startDate: p?.startDate ?? "",
    endDate: p?.endDate ?? "",
    scope: p?.scope ?? "",
    productIds: Array.isArray(p?.productIds) ? p.productIds : [],
    variantIds: Array.isArray(p?.variantIds) ? p.variantIds : [],
    priority: p?.priority ?? 0,
    stackable: !!p?.stackable,
    isActive: !!p?.isActive,
    raw: p,
  }));
}

async function load() {
  loading.value = true;
  try {
    const res = await promotionsApi.list(activeOnly.value);
    rows.value = normalize(extractList(res?.data));
  } catch {
    rows.value = [];
    toast("Failed to load promotions.", "error");
  } finally {
    loading.value = false;
  }
}

const dlg = reactive({
  open: false,
  mode: "create",
  loading: false,
  alert: "",
  id: null,
  form: {
    code: "",
    name: "",
    description: "",
    discountType: "PERCENT",
    discountValue: 0,
    startDate: "",
    endDate: "",
    scope: "ALL",
    productIdsText: "",
    variantIdsText: "",
    priority: 0,
    stackable: false,
    isActive: true,
  },
});

function parseIds(text) {
  return String(text || "")
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean)
    .map((x) => Number(x))
    .filter((n) => Number.isFinite(n));
}

function openCreate() {
  dlg.open = true;
  dlg.mode = "create";
  dlg.alert = "";
  dlg.id = null;
  dlg.form = {
    code: "",
    name: "",
    description: "",
    discountType: "PERCENT",
    discountValue: 0,
    startDate: "",
    endDate: "",
    scope: "ALL",
    productIdsText: "",
    variantIdsText: "",
    priority: 0,
    stackable: false,
    isActive: true,
  };
}

function openEdit(row) {
  dlg.open = true;
  dlg.mode = "edit";
  dlg.alert = "";
  dlg.id = row.id;
  dlg.form = {
    code: row.code,
    name: row.name,
    description: row.description,
    discountType: row.discountType || "PERCENT",
    discountValue: Number(row.discountValue || 0),
    startDate: row.startDate || "",
    endDate: row.endDate || "",
    scope: row.scope || "ALL",
    productIdsText: (row.productIds || []).join(","),
    variantIdsText: (row.variantIds || []).join(","),
    priority: Number(row.priority || 0),
    stackable: !!row.stackable,
    isActive: !!row.isActive,
  };
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.code || !dlg.form.name) {
    dlg.alert = "code and name are required.";
    return;
  }

  const payload = {
    code: dlg.form.code,
    name: dlg.form.name,
    description: dlg.form.description,
    discountType: dlg.form.discountType,
    discountValue: dlg.form.discountValue,
    startDate: dlg.form.startDate,
    endDate: dlg.form.endDate,
    scope: dlg.form.scope,
    productIds: parseIds(dlg.form.productIdsText),
    variantIds: parseIds(dlg.form.variantIdsText),
    priority: dlg.form.priority,
    stackable: dlg.form.stackable,
    isActive: dlg.form.isActive,
  };

  dlg.loading = true;
  try {
    if (dlg.mode === "create") {
      await promotionsApi.create(payload);
      toast("Promotion created.", "success");
    } else {
      await promotionsApi.update(dlg.id, payload);
      toast("Promotion updated.", "success");
    }
    dlg.open = false;
    await load();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Save failed";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

async function remove(row) {
  const ok = await confirmModal(
    `Delete promotion #${row.id}?`,
    "Confirm",
    "Delete",
    true
  );
  if (!ok) return;

  try {
    await promotionsApi.remove(row.id);
    toast("Promotion deleted.", "success");
    await load();
  } catch {
    toast("Delete failed.", "error");
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
