<template>
  <div class="page">

    <!-- Header -->
    <div class="page-header">
      <div>
        <div class="page-kicker">Admin</div>
        <h1>Quản lý người dùng</h1>
        <p class="page-sub">{{ rows.length }} tài khoản trong hệ thống</p>
      </div>
      <div class="header-actions">
        <button class="btn-outline" :disabled="loading" @click="load">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/></svg>
          Làm mới
        </button>
        <button class="btn-primary" @click="openCreate">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Thêm người dùng
        </button>
      </div>
    </div>

    <!-- Table card -->
    <div class="table-card">
      <div v-if="loading" class="loading-wrap">
        <div class="spinner" /> Đang tải...
      </div>

      <div v-else-if="rows.length === 0" class="empty-state">
        Không có người dùng nào
      </div>

      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th style="width:60px">ID</th>
              <th>Username</th>
              <th>Email</th>
              <th style="width:130px">Vai trò</th>
              <th style="width:140px;text-align:right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id" class="data-row">
              <td class="id-cell">#{{ row.id }}</td>
              <td class="name-cell">
                <div class="user-avatar">{{ row.username?.[0]?.toUpperCase() }}</div>
                {{ row.username }}
              </td>
              <td class="email-cell">{{ row.email }}</td>
              <td>
                <span class="role-tag" :class="roleClass(row.role)">{{ row.role || "—" }}</span>
              </td>
              <td class="actions-cell">
                <button class="btn-edit" @click="openEdit(row)">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  Sửa
                </button>
                <button class="btn-delete" @click="remove(row)">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/></svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Dialog -->
    <Teleport to="body">
      <div v-if="dlg.open" class="modal-backdrop" @click.self="dlg.open = false">
        <div class="modal-box">
          <div class="modal-header">
            <h3>{{ dlg.mode === 'create' ? '➕ Thêm người dùng' : '✏️ Cập nhật người dùng' }}</h3>
            <button class="modal-close" @click="dlg.open = false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <div v-if="dlg.alert" class="alert-box">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {{ dlg.alert }}
          </div>

          <div class="modal-body">
            <div class="form-grid">
              <div class="form-group">
                <label>Username *</label>
                <input class="field" v-model="dlg.form.username" placeholder="Nhập username..." />
              </div>
              <div class="form-group">
                <label>Email *</label>
                <input class="field" v-model="dlg.form.email" type="email" placeholder="Nhập email..." />
              </div>
              <div v-if="dlg.mode === 'create'" class="form-group">
                <label>Mật khẩu *</label>
                <input class="field" v-model="dlg.form.password" type="password" placeholder="Nhập mật khẩu..." />
              </div>
              <div class="form-group">
                <label>Vai trò *</label>
                <select class="field" v-model="dlg.form.role">
                  <option value="ADMIN">ADMIN</option>
                  <option value="SALES">SALES</option>
                  <option value="INVENTORY">INVENTORY</option>
                </select>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-outline" @click="dlg.open = false">Hủy</button>
            <button class="btn-primary" :disabled="dlg.loading" @click="save">
              <span v-if="dlg.loading" class="spinner-sm" />
              {{ dlg.mode === 'create' ? 'Tạo tài khoản' : 'Lưu thay đổi' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

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
  return (list || []).map((u) => {
    const rawRole = u?.role?.name ?? u?.role?.roleName ?? u?.role ?? "";
    return {
      id: u?.id ?? u?.userId,
      username: u?.username ?? u?.name ?? "",
      email: u?.email ?? "",
      role: String(rawRole).toUpperCase(),
      raw: u,
    };
  });
}

function roleClass(role) {
  const map = {
    ADMIN:     "admin",
    SALES:     "sales",
    INVENTORY: "inventory",
    CUSTOMER:  "customer",
  };
  return map[String(role || "").toUpperCase()] ?? "unknown";
}

async function load() {
  loading.value = true;
  try {
    const res = await usersApi.list();
    rows.value = normalize(extractList(res?.data));
  } catch (e) {
    rows.value = [];
    toast("Không thể tải danh sách người dùng.", "error");
  } finally {
    loading.value = false;
  }
}

const dlg = reactive({
  open: false, mode: "create", loading: false, alert: "", id: null,
  originalRole: "", // ✅ lưu role gốc để so sánh, tránh gọi API thừa
  form: { username: "", email: "", password: "", role: "ADMIN" },
});

function openCreate() {
  Object.assign(dlg, { open: true, mode: "create", alert: "", id: null, originalRole: "" });
  dlg.form = { username: "", email: "", password: "", role: "ADMIN" };
}

function openEdit(row) {
  const rawRole = row?.raw?.role?.name ?? row?.raw?.role?.roleName ?? row?.role ?? "ADMIN";
  const role = String(rawRole).toUpperCase();
  // ✅ lưu originalRole để so sánh trong save()
  Object.assign(dlg, { open: true, mode: "edit", alert: "", id: row?.id, originalRole: role });
  dlg.form = {
    username: row?.username || "",
    email: row?.email || "",
    password: "",
    role,
  };
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.username || !dlg.form.email || !dlg.form.role) {
    dlg.alert = "Username, email và vai trò là bắt buộc.";
    return;
  }
  if (dlg.mode === "create" && !dlg.form.password) {
    dlg.alert = "Mật khẩu là bắt buộc khi tạo mới.";
    return;
  }

  dlg.loading = true;
  try {
    if (dlg.mode === "create") {
      // Tạo mới: role gửi trong body, backend xử lý 1 lần
      await usersApi.add({
        username: dlg.form.username,
        email: dlg.form.email,
        password: dlg.form.password,
        role: dlg.form.role,
      });
      toast("Tạo tài khoản thành công!", "success");

    } else {
      // ✅ Bước 1: update username + email (endpoint /update)
      await usersApi.update(dlg.id, {
        username: dlg.form.username,
        email: dlg.form.email,
      });

      // ✅ Bước 2: update role — chỉ gọi khi role thay đổi
      // (backend sẽ throw "Role không thay đổi" nếu giống nhau → tránh lỗi thừa)
      if (dlg.form.role !== dlg.originalRole) {
        await usersApi.updateRole(dlg.id, dlg.form.role);
      }

      toast("Cập nhật thành công!", "success");
    }

    dlg.open = false;
    await load();

  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Lưu thất bại";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

async function remove(row) {
  const ok = await confirmModal(`Xóa người dùng #${row?.id}?`, "Xác nhận", "Xóa", true);
  if (!ok) return;
  try {
    await usersApi.remove(row.id);
    toast("Đã xóa người dùng.", "success");
    await load();
  } catch {
    toast("Xóa thất bại.", "error");
  }
}

onMounted(load);
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  font-family: 'Be Vietnam Pro', 'Segoe UI', sans-serif;
}

/* Header */
.page-header { display: flex; align-items: flex-start; justify-content: space-between; flex-wrap: wrap; gap: 16px; margin-bottom: 24px; }
.page-kicker { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .08em; color: #2563eb; background: #eff6ff; padding: 2px 10px; border-radius: 50px; display: inline-block; margin-bottom: 6px; }
.page-header h1 { font-size: 22px; font-weight: 800; color: #111827; margin: 0 0 4px; }
.page-sub { font-size: 13px; color: #6b7280; margin: 0; }
.header-actions { display: flex; gap: 10px; flex-wrap: wrap; }

/* Buttons */
.btn-primary {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 9px 18px; background: #111827; color: white;
  border: none; border-radius: 50px; font-size: 13px; font-weight: 700;
  font-family: inherit; cursor: pointer; transition: all .15s;
}
.btn-primary:hover:not(:disabled) { background: #2563eb; }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
.btn-outline {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 9px 18px; background: white; color: #374151;
  border: 1.5px solid #e5e7eb; border-radius: 50px; font-size: 13px; font-weight: 600;
  font-family: inherit; cursor: pointer; transition: all .15s;
}
.btn-outline:hover:not(:disabled) { border-color: #2563eb; color: #2563eb; background: #eff6ff; }
.btn-outline:disabled { opacity: .5; cursor: not-allowed; }

/* Table card */
.table-card { background: white; border: 1.5px solid #f0f0f0; border-radius: 16px; overflow: hidden; }
.loading-wrap { display: flex; align-items: center; gap: 10px; padding: 48px; justify-content: center; color: #9ca3af; font-size: 13px; }
.spinner { width: 18px; height: 18px; border: 2px solid #e5e7eb; border-top-color: #2563eb; border-radius: 50%; animation: spin .7s linear infinite; flex-shrink: 0; }
.spinner-sm { width: 14px; height: 14px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin .7s linear infinite; flex-shrink: 0; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { text-align: center; padding: 48px; font-size: 13px; color: #d1d5db; }
.table-wrap { overflow-x: auto; }

/* Table */
.data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.data-table thead tr { background: #f9fafb; }
.data-table th { padding: 11px 16px; text-align: left; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .06em; color: #9ca3af; border-bottom: 1px solid #f3f4f6; white-space: nowrap; }
.data-row { border-bottom: 1px solid #f9fafb; transition: background .12s; }
.data-row:last-child { border-bottom: none; }
.data-row:hover { background: #f8faff; }
.data-table td { padding: 12px 16px; }
.id-cell { color: #d1d5db; font-weight: 700; font-size: 12px; }
.name-cell { display: flex; align-items: center; gap: 10px; font-weight: 600; color: #111827; }
.user-avatar {
  width: 30px; height: 30px; border-radius: 50%; background: #eff6ff;
  color: #2563eb; font-size: 12px; font-weight: 800;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.email-cell { color: #6b7280; }

/* Role tags */
.role-tag { font-size: 11px; font-weight: 700; padding: 3px 10px; border-radius: 50px; }
.role-tag.admin     { background: #fef2f2; color: #dc2626; }
.role-tag.sales     { background: #dcfce7; color: #16a34a; }
.role-tag.inventory { background: #eff6ff; color: #2563eb; }
.role-tag.customer  { background: #fefce8; color: #ca8a04; }
.role-tag.unknown   { background: #f3f4f6; color: #9ca3af; }

.actions-cell { text-align: right; }
.btn-edit {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 5px 12px; background: #f9fafb; border: 1.5px solid #e5e7eb;
  border-radius: 50px; font-size: 12px; font-weight: 600; color: #374151;
  cursor: pointer; font-family: inherit; transition: all .12s; margin-right: 6px;
}
.btn-edit:hover { border-color: #2563eb; color: #2563eb; background: #eff6ff; }
.btn-delete {
  display: inline-flex; align-items: center; justify-content: center;
  width: 30px; height: 30px; background: #fef2f2; border: 1.5px solid #fecaca;
  border-radius: 50%; color: #dc2626; cursor: pointer; transition: all .12s;
}
.btn-delete:hover { background: #fee2e2; border-color: #fca5a5; }

/* Modal */
.modal-backdrop { position: fixed; inset: 0; z-index: 1000; background: rgba(0,0,0,.45); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; }
.modal-box { background: white; border-radius: 20px; width: min(540px, 92vw); box-shadow: 0 32px 80px rgba(0,0,0,.18); animation: popIn .22s cubic-bezier(.34,1.56,.64,1); }
@keyframes popIn { from{transform:scale(.92);opacity:0} to{transform:scale(1);opacity:1} }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 22px 24px 0; }
.modal-header h3 { font-size: 17px; font-weight: 800; color: #111827; margin: 0; }
.modal-close { background: #f3f4f6; border: none; border-radius: 50%; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #6b7280; transition: all .12s; }
.modal-close:hover { background: #fee2e2; color: #dc2626; }
.modal-body { padding: 20px 24px; }
.modal-footer { padding: 0 24px 22px; display: flex; justify-content: flex-end; gap: 10px; }

/* Form */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
@media (max-width: 480px) { .form-grid { grid-template-columns: 1fr; } }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 12px; font-weight: 700; color: #374151; }
.field {
  width: 100%; padding: 9px 12px; border: 1.5px solid #e5e7eb;
  border-radius: 10px; font-size: 13px; font-family: inherit; color: #111827;
  background: white; transition: border-color .15s; box-sizing: border-box;
}
.field:focus { outline: none; border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,.08); }

/* Alert */
.alert-box { display: flex; align-items: center; gap: 8px; margin: 12px 24px 0; padding: 10px 14px; background: #fef2f2; border: 1px solid #fecaca; border-radius: 10px; font-size: 13px; color: #dc2626; }
</style>