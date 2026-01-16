<template>
  <div class="cm">
    <div class="page-head">
      <div>
        <div class="kicker">Management</div>
        <div class="title">Customer Manager</div>
        <div class="sub ts-muted">Quản lý khách hàng (chỉ sửa/xóa).</div>
      </div>
    </div>

    <!-- Filters -->
    <div class="card ts-card">
      <div class="card-header bg-transparent border-0 pt-4 px-4">
        <div class="hdr">
          <div class="h">Bộ lọc</div>
          <button
            class="btn btn-link p-0 fw-bold"
            type="button"
            @click="resetFilters"
          >
            Làm mới
          </button>
        </div>
      </div>

      <div class="card-body px-4 pb-4">
        <div class="row g-3 align-items-center">
          <div class="col-12 col-md-5">
            <div class="input-group">
              <span class="input-group-text"><i class="bi bi-search"></i></span>
              <input
                v-model="searchQuery"
                class="form-control ts-input"
                placeholder="Tìm theo Tên, SĐT hoặc Email..."
              />
            </div>
          </div>

          <div class="col-12 col-md-4">
            <select v-model="filterType" class="form-select ts-input">
              <option value="">Tất cả</option>
              <option value="REGULAR">Khách thường (REGULAR)</option>
              <option value="VIP">Khách VIP (VIP)</option>
            </select>
          </div>

          <div
            class="col-12 col-md-3 d-flex justify-content-md-end gap-2 align-items-center"
          >
            <span class="badge text-bg-info ts-pill"
              >Tổng: {{ filteredTotal }} khách</span
            >
            <button
              class="btn btn-outline-secondary ts-btn"
              :disabled="loading"
              @click="fetchCustomers"
            >
              <span
                v-if="loading"
                class="spinner-border spinner-border-sm me-2"
              ></span>
              Tải lại
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card ts-card">
      <div class="card-header bg-transparent border-0 pt-4 px-4">
        <div class="hdr">
          <div class="h">Danh sách khách hàng</div>
          <div class="ts-muted small">
            Hiển thị: {{ pagedData.length }} / {{ filteredTotal }}
          </div>
        </div>
      </div>

      <div class="card-body px-4 pb-4">
        <div v-if="loading" class="py-4 text-center ts-muted">
          <div class="spinner-border" role="status"></div>
          <div class="mt-2">Đang tải dữ liệu...</div>
        </div>

        <div v-else class="table-responsive">
          <table class="table table-striped table-bordered align-middle">
            <thead class="table-light">
              <tr>
                <th style="width: 70px" class="text-center">ID</th>
                <th>Họ và Tên</th>
                <th>Email</th>
                <th style="width: 140px">SĐT</th>
                <th style="width: 140px" class="text-center">Loại</th>
                <th style="width: 140px">Ngày sinh</th>
                <th style="width: 140px" class="text-end">Chi tiêu</th>
                <th style="width: 200px" class="text-center">Thao tác</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="row in pagedData" :key="row.id">
                <td class="text-center">{{ row.id }}</td>
                <td>{{ row.name }}</td>
                <td class="text-truncate" style="max-width: 320px">
                  {{ row.email }}
                </td>
                <td>{{ row.phone }}</td>
                <td class="text-center">
                  <span
                    class="badge ts-pill"
                    :class="getCustomerTypeBadge(row.customerType)"
                  >
                    {{
                      row.customerTypeDisplay || row.customerType || "REGULAR"
                    }}
                  </span>
                </td>
                <td class="ts-muted">{{ row.dateOfBirth || "-" }}</td>
                <td class="text-end">{{ formatCurrency(row.totalSpent) }}</td>
                <td class="text-center">
                  <div class="d-flex justify-content-center gap-2 flex-wrap">
                    <button
                      class="btn btn-sm btn-outline-primary ts-btn"
                      @click="openEditModal(row)"
                    >
                      <i class="bi bi-pencil-square me-1"></i> Sửa
                    </button>
                    <button
                      class="btn btn-sm btn-outline-danger ts-btn"
                      @click="handleDelete(row.id)"
                    >
                      <i class="bi bi-trash me-1"></i> Xóa
                    </button>
                  </div>
                </td>
              </tr>

              <tr v-if="pagedData.length === 0">
                <td colspan="8" class="text-center ts-muted py-4">
                  Không có dữ liệu.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div
          class="pager d-flex flex-column flex-md-row justify-content-md-end gap-2 align-items-md-center mt-3"
        >
          <div class="d-flex align-items-center gap-2">
            <span class="ts-muted small">Rows:</span>
            <select
              class="form-select form-select-sm ts-input"
              style="width: 110px"
              v-model.number="pageSize"
            >
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
          </div>

          <ul class="pagination mb-0">
            <li class="page-item" :class="{ disabled: page <= 1 }">
              <button
                class="page-link"
                @click="page = Math.max(1, page - 1)"
                :disabled="page <= 1"
              >
                Prev
              </button>
            </li>

            <li class="page-item disabled">
              <span class="page-link">Page {{ page }} / {{ maxPage }}</span>
            </li>

            <li class="page-item" :class="{ disabled: page >= maxPage }">
              <button
                class="page-link"
                @click="page = Math.min(maxPage, page + 1)"
                :disabled="page >= maxPage"
              >
                Next
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- EDIT MODAL -->
    <div
      class="modal fade"
      id="customerEditModal"
      tabindex="-1"
      aria-hidden="true"
      ref="modalEl"
    >
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Cập nhật Khách Hàng</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
              @click="resetForm"
            ></button>
          </div>

          <div class="modal-body">
            <div
              v-if="formAlert"
              class="alert alert-danger d-flex align-items-center gap-2"
              role="alert"
            >
              <i class="bi bi-exclamation-triangle-fill"></i>
              <div>{{ formAlert }}</div>
            </div>

            <div class="row g-3">
              <div class="col-12 col-md-6">
                <label class="form-label fw-bold">Họ và Tên</label>
                <input
                  v-model="formData.fullName"
                  class="form-control ts-input"
                  placeholder="Nhập họ tên"
                />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label fw-bold">Loại khách</label>
                <select
                  v-model="formData.customerType"
                  class="form-select ts-input"
                >
                  <option value="REGULAR">Khách thường (REGULAR)</option>
                  <option value="VIP">Khách VIP (VIP)</option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label fw-bold">Email</label>
                <input
                  v-model="formData.email"
                  class="form-control ts-input"
                  placeholder="example@mail.com"
                />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label fw-bold">Số điện thoại</label>
                <input
                  v-model="formData.phone"
                  class="form-control ts-input"
                  placeholder="09xxxxxxxx"
                />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label fw-bold">Ngày sinh</label>
                <!-- keep value-format: YYYY-MM-DD -->
                <input
                  v-model="formData.birthDate"
                  class="form-control ts-input"
                  type="date"
                />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label fw-bold">Địa chỉ</label>
                <input
                  v-model="formData.address"
                  class="form-control ts-input"
                  placeholder="Nhập địa chỉ"
                />
              </div>

              <div class="col-12">
                <label class="form-label fw-bold">Ghi chú</label>
                <textarea
                  v-model="formData.notes"
                  class="form-control ts-input"
                  rows="3"
                  placeholder="Ghi chú thêm..."
                ></textarea>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button
              class="btn btn-outline-secondary ts-btn"
              data-bs-dismiss="modal"
              type="button"
              @click="resetForm"
            >
              Hủy
            </button>
            <button
              class="btn btn-primary ts-btn"
              type="button"
              :disabled="submitting"
              @click="submitForm"
            >
              <span
                v-if="submitting"
                class="spinner-border spinner-border-sm me-2"
              ></span>
              Cập nhật
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  onMounted,
  computed,
  watch,
  nextTick,
  onBeforeUnmount,
} from "vue";
import http from "../../api/http";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

// ✅ IMPORTANT: use Bootstrap Modal via ESM import (works in Vite)
import { Modal } from "bootstrap";

const API_URL = "/api/auth/customers";

const loading = ref(false);
const submitting = ref(false);
const tableData = ref([]);

const currentId = ref(null);

const searchQuery = ref("");
const filterType = ref("");
const page = ref(1);
const pageSize = ref(20);

const modalEl = ref(null);
let modalInstance = null;

const formAlert = ref("");

const formData = reactive({
  fullName: "",
  email: "",
  phone: "",
  birthDate: null, // YYYY-MM-DD
  address: "",
  customerType: "REGULAR",
  notes: "",
});

const rules = {
  fullName: { required: true, min: 2, message: "Tên phải có ít nhất 2 ký tự" },
  email: { required: true, email: true, message: "Email không hợp lệ" },
  phone: {
    required: true,
    phone: true,
    message: "Số điện thoại phải là số (10-11 số)",
  },
};

const filteredData = computed(() => {
  const q = (searchQuery.value || "").trim().toLowerCase();
  return tableData.value.filter((item) => {
    const name = (item.name || "").toLowerCase();
    const email = (item.email || "").toLowerCase();
    const phone = item.phone || "";
    const matchText =
      !q || name.includes(q) || email.includes(q) || phone.includes(q);
    const matchType =
      !filterType.value || item.customerType === filterType.value;
    return matchText && matchType;
  });
});

const filteredTotal = computed(() => filteredData.value.length);

const maxPage = computed(() =>
  Math.max(1, Math.ceil(filteredTotal.value / pageSize.value))
);

const pagedData = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredData.value.slice(start, start + pageSize.value);
});

watch([filteredTotal, pageSize], () => {
  if (page.value > maxPage.value) page.value = maxPage.value;
});

async function fetchCustomers() {
  loading.value = true;
  try {
    const res = await http.get(API_URL);
    const data = Array.isArray(res.data) ? res.data : [];
    tableData.value = data.filter((c) => c?.isActive === true);

    if (page.value > maxPage.value) page.value = maxPage.value;
  } catch (err) {
    console.error(err);
    toast("Không thể tải danh sách khách hàng", "danger");
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  searchQuery.value = "";
  filterType.value = "";
  page.value = 1;
}

async function openEditModal(row) {
  if (!row) return;

  // fill form
  currentId.value = row.id;

  formData.fullName = row.name || "";
  formData.email = row.email || "";
  formData.phone = row.phone || "";
  formData.birthDate = row.dateOfBirth || null;
  formData.address = row.address || "";
  formData.customerType = row.customerType || "REGULAR";
  formData.notes = row.notes || "";

  formAlert.value = "";

  // ✅ ensure ref is ready
  await nextTick();

  if (!modalEl.value) {
    console.warn(
      "customerEditModal ref is null. Modal element not found in DOM."
    );
    return;
  }

  // ✅ create/show modal reliably in Vite
  modalInstance = Modal.getOrCreateInstance(modalEl.value, {
    backdrop: "static",
    keyboard: true,
  });
  modalInstance.show();
}

function validateForm() {
  formAlert.value = "";

  const name = (formData.fullName || "").trim();
  if (!name) return (formAlert.value = "Vui lòng nhập họ tên"), false;
  if (name.length < rules.fullName.min)
    return (formAlert.value = rules.fullName.message), false;

  const email = (formData.email || "").trim();
  if (!email) return (formAlert.value = "Vui lòng nhập email"), false;
  const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  if (!emailOk) return (formAlert.value = rules.email.message), false;

  const phone = (formData.phone || "").trim();
  if (!phone) return (formAlert.value = "Vui lòng nhập số điện thoại"), false;
  const phoneOk = /^[0-9]{10,11}$/.test(phone);
  if (!phoneOk) return (formAlert.value = rules.phone.message), false;

  return true;
}

async function submitForm() {
  if (!validateForm()) return;

  submitting.value = true;
  try {
    await http.put(`${API_URL}/${currentId.value}`, formData);
    toast("Cập nhật thành công!", "success");
    modalInstance?.hide();
    await fetchCustomers();
  } catch (error) {
    console.error(error);
    const msg = error?.response?.data?.message || "Có lỗi xảy ra";
    toast(msg, "danger");
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id) {
  const ok = await confirmModal(
    "Bạn có chắc chắn muốn xóa khách hàng này không?",
    "Cảnh báo",
    "Xóa",
    true
  );
  if (!ok) return;

  try {
    await http.delete(`${API_URL}/${id}`);
    toast("Đã xóa thành công", "success");
    await fetchCustomers();
  } catch {
    toast("Xóa thất bại", "danger");
  }
}

function resetForm() {
  formAlert.value = "";
  // optional: clear form fields if you want
  // currentId.value = null;
}

function formatCurrency(value) {
  if (value === null || value === undefined) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
}

function getCustomerTypeBadge(type) {
  return type === "VIP" ? "text-bg-warning" : "text-bg-success";
}

// ✅ reset when modal fully closes (nice UX)
function onHidden() {
  resetForm();
}

onMounted(async () => {
  await fetchCustomers();
  await nextTick();

  if (modalEl.value) {
    modalEl.value.addEventListener("hidden.bs.modal", onHidden);
  }
});

onBeforeUnmount(() => {
  if (modalEl.value) {
    modalEl.value.removeEventListener("hidden.bs.modal", onHidden);
  }
});
</script>

<style scoped>
.cm {
  display: grid;
  gap: 16px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding: 4px 2px;
}

.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
}
.title {
  font-weight: 950;
  font-size: 22px;
  letter-spacing: -0.3px;
}
.sub {
  font-size: 13px;
}

.hdr {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.h {
  font-weight: 900;
  font-size: 16px;
}
.small {
  font-size: 12px;
}

.pager {
  gap: 10px;
}
</style>
