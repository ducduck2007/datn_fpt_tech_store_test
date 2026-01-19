<!-- FILE: src/pages/system/UserManager.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">User Management</div>
          <div class="muted">Base: /api/auth/users</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="load" :loading="loading">Reload</el-button>
          <el-button type="primary" @click="openCreate">Add user</el-button>
        </div>
      </div>

      <el-divider />

      <el-table :data="rows" border :loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="username" label="Username" min-width="180" />
        <el-table-column prop="email" label="Email" min-width="220" />
        <el-table-column prop="role" label="Role" width="160" />
        <el-table-column label="Actions" width="220">
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
      :title="dlg.mode === 'create' ? 'Add User' : 'Update User'"
      width="560px"
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
          <el-form-item label="username">
            <el-input v-model="dlg.form.username" />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="email">
            <el-input v-model="dlg.form.email" />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6" v-if="dlg.mode === 'create'">
          <el-form-item label="password">
            <el-input
              v-model="dlg.form.password"
              type="password"
              show-password
            />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="role">
            <el-select v-model="dlg.form.role">
              <el-option label="ADMIN" value="ADMIN" />
              <el-option label="SALES" value="SALES" />
              <el-option label="INVENTORY" value="INVENTORY" />
            </el-select>
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
import { usersApi } from "../../api/users.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

const loading = ref(false);
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
  return (list || []).map((u) => ({
    id: u?.id ?? u?.userId,
    username: u?.username ?? u?.name ?? "",
    email: u?.email ?? "",
    role: (u?.role ?? "").toString(),
    raw: u,
  }));
}

async function load() {
  loading.value = true;
  try {
    const res = await usersApi.list();
    rows.value = normalize(extractList(res?.data));
  } catch (e) {
    rows.value = [];
    toast("Failed to load users.", "error");
  } finally {
    loading.value = false;
  }
}

const dlg = reactive({
  open: false,
  mode: "create", // create | edit
  loading: false,
  alert: "",
  id: null,
  form: {
    username: "",
    email: "",
    password: "",
    role: "STAFF",
  },
});

function openCreate() {
  dlg.open = true;
  dlg.mode = "create";
  dlg.alert = "";
  dlg.id = null;
  dlg.form = { username: "", email: "", password: "", role: "STAFF" };
}

function openEdit(row) {
  dlg.open = true;
  dlg.mode = "edit";
  dlg.alert = "";
  dlg.id = row?.id;
  dlg.form = {
    username: row?.username || "",
    email: row?.email || "",
    password: "",
    role: (row?.role || "STAFF").toUpperCase(),
  };
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.username || !dlg.form.email || !dlg.form.role) {
    dlg.alert = "username, email, role are required.";
    return;
  }
  if (dlg.mode === "create" && !dlg.form.password) {
    dlg.alert = "password is required for create.";
    return;
  }

  dlg.loading = true;
  try {
    if (dlg.mode === "create") {
      await usersApi.add({
        username: dlg.form.username,
        email: dlg.form.email,
        password: dlg.form.password,
        role: dlg.form.role,
      });
      toast("User created.", "success");
    } else {
      await usersApi.update(dlg.id, {
        username: dlg.form.username,
        email: dlg.form.email,
        role: dlg.form.role,
      });
      toast("User updated.", "success");
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
    `Delete user #${row?.id}?`,
    "Confirm",
    "Delete",
    true
  );
  if (!ok) return;

  try {
    await usersApi.remove(row.id);
    toast("User deleted.", "success");
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
