<!-- FILE: src/pages/system/CustomerManager.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Customers & Loyalty</div>
          <div class="muted">Base: /api/auth/customers</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="load" :loading="loading">Reload</el-button>
          <el-button type="primary" @click="openCreate">Add customer</el-button>
        </div>
      </div>

      <el-divider />

      <div class="row g-3">
        <div class="col-12 col-md-4">
          <el-input
            v-model="q"
            placeholder="Search name / email / phone"
            clearable
          />
        </div>
        <div class="col-12 col-md-3">
          <el-select
            v-model="typeFilter"
            placeholder="Type"
            clearable
            @change="load"
          >
            <el-option label="REGULAR" value="REGULAR" />
            <el-option label="VIP" value="VIP" />
          </el-select>
        </div>
      </div>

      <el-divider />

      <el-table :data="paged" border :loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="fullName" label="Full name" min-width="220" />
        <el-table-column prop="email" label="Email" min-width="220" />
        <el-table-column prop="phone" label="Phone" width="160" />
        <el-table-column prop="customerType" label="Type" width="140">
          <template #default="{ row }">
            <el-tag
              :type="row.customerType === 'VIP' ? 'warning' : 'info'"
              effect="light"
            >
              {{ row.customerType || "REGULAR" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loyaltyPoints" label="Points" width="100" sortable align="center">
          <template #default="{ row }">
            <span class="fw-bold text-primary">{{ row.loyaltyPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="birthDate" label="Birth date" width="160" />
        <el-table-column label="Actions" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">Edit</el-button>
            <el-button size="small" type="danger" plain @click="remove(row)"
              >Delete</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <div class="d-flex justify-content-end mt-3">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filtered.length"
          :current-page="page"
          @current-change="(v) => (page = v)"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dlg.open"
      :title="dlg.mode === 'create' ? 'Add Customer' : 'Update Customer'"
      width="720px"
    >
      <el-alert
        v-if="dlg.alert"
        :title="dlg.alert"
        type="error"
        show-icon
        class="mb-3"
      />

      <el-form :model="dlg.form" label-position="top" class="row g-3">
        <div class="col-12 col-md-6">
          <el-form-item label="fullName">
            <el-input v-model="dlg.form.fullName" />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="customerType">
            <el-select v-model="dlg.form.customerType">
              <el-option label="REGULAR" value="REGULAR" />
              <el-option label="VIP" value="VIP" />
            </el-select>
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="email">
            <el-input v-model="dlg.form.email" />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="phone">
            <el-input v-model="dlg.form.phone" />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="birthDate">
            <el-date-picker
              v-model="dlg.form.birthDate"
              type="date"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="address">
            <el-input v-model="dlg.form.address" />
          </el-form-item>
        </div>

        <div class="col-12">
          <el-form-item label="notes">
            <el-input v-model="dlg.form.notes" type="textarea" :rows="2" />
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
import { computed, onMounted, reactive, ref } from "vue";
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

const loading = ref(false);
const rows = ref([]);

const q = ref("");
const typeFilter = ref("");
const page = ref(1);
const pageSize = 10;

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
    id: c?.id ?? c?.customerId,
    fullName: c?.fullName ?? c?.name ?? "",
    email: c?.email ?? "",
    phone: c?.phone ?? "",
    birthDate: c?.birthDate ?? "",
    customerType: (c?.customerType ?? "REGULAR").toString().toUpperCase(),
    loyaltyPoints: c?.loyaltyPoints ?? 0,
    address: c?.address ?? "",
    notes: c?.notes ?? "",
    raw: c,
  }));
}

async function load() {
  loading.value = true;
  try {
    const res = typeFilter.value
      ? await customersApi.listByType(typeFilter.value)
      : await customersApi.listAll();

    rows.value = normalize(extractList(res?.data));
    page.value = 1;
  } catch {
    rows.value = [];
    toast("Failed to load customers.", "error");
  } finally {
    loading.value = false;
  }
}

const filtered = computed(() => {
  const kw = (q.value || "").trim().toLowerCase();
  if (!kw) return rows.value;
  return rows.value.filter((r) =>
    `${r.fullName} ${r.email} ${r.phone}`.toLowerCase().includes(kw)
  );
});

const paged = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filtered.value.slice(start, start + pageSize);
});

const dlg = reactive({
  open: false,
  mode: "create",
  loading: false,
  alert: "",
  id: null,
  form: {
    fullName: "",
    email: "",
    phone: "",
    birthDate: "",
    customerType: "REGULAR",
    address: "",
    notes: "",
  },
});

function openCreate() {
  dlg.open = true;
  dlg.mode = "create";
  dlg.alert = "";
  dlg.id = null;
  dlg.form = {
    fullName: "",
    email: "",
    phone: "",
    birthDate: "",
    customerType: "REGULAR",
    address: "",
    notes: "",
  };
}

function openEdit(row) {
  dlg.open = true;
  dlg.mode = "edit";
  dlg.alert = "";
  dlg.id = row?.id;
  dlg.form = {
    fullName: row?.fullName || "",
    email: row?.email || "",
    phone: row?.phone || "",
    birthDate: row?.birthDate || "",
    customerType: row?.customerType || "REGULAR",
    address: row?.address || "",
    notes: row?.notes || "",
  };
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.fullName || !dlg.form.email || !dlg.form.phone) {
    dlg.alert = "fullName, email, phone are required.";
    return;
  }

  dlg.loading = true;
  try {
    if (dlg.mode === "create") {
      await customersApi.create({ ...dlg.form });
      toast("Customer created.", "success");
    } else {
      // Partial CustomerRequest allowed
      await customersApi.update(dlg.id, { ...dlg.form });
      toast("Customer updated.", "success");
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
    `Delete customer #${row?.id}?`,
    "Confirm",
    "Delete",
    true
  );
  if (!ok) return;

  try {
    await customersApi.remove(row.id);
    toast("Customer deleted.", "success");
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
