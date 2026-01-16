<!-- src/pages/system/CustomerManager.vue -->
<template>
  <div class="cm">
    <div class="page-head">
      <div>
        <div class="kicker">Management</div>
        <div class="title">Customer Manager</div>
        <div class="sub ts-muted">Quản lý khách hàng (chỉ sửa/xóa).</div>
      </div>
    </div>

    <el-card class="card" shadow="hover">
      <template #header>
        <div class="hdr">
          <div class="h">Bộ lọc</div>
          <el-button text @click="resetFilters">Làm mới</el-button>
        </div>
      </template>

      <el-row :gutter="12">
        <el-col :xs="24" :md="10">
          <el-input
            v-model="searchQuery"
            placeholder="Tìm theo Tên, SĐT hoặc Email..."
            clearable
            :prefix-icon="Search"
          />
        </el-col>

        <el-col :xs="24" :md="7">
          <el-select
            v-model="filterType"
            placeholder="Loại khách"
            clearable
            style="width: 100%"
          >
            <el-option label="Tất cả" value="" />
            <el-option label="Khách thường (REGULAR)" value="REGULAR" />
            <el-option label="Khách VIP (VIP)" value="VIP" />
          </el-select>
        </el-col>

        <el-col :xs="24" :md="7" class="right-tools">
          <el-tag effect="plain" type="info"
            >Tổng: {{ filteredTotal }} khách</el-tag
          >
          <el-button :loading="loading" @click="fetchCustomers"
            >Tải lại</el-button
          >
        </el-col>
      </el-row>
    </el-card>

    <el-card class="card" shadow="hover">
      <template #header>
        <div class="hdr">
          <div class="h">Danh sách khách hàng</div>
          <div class="ts-muted small">
            Hiển thị: {{ pagedData.length }} / {{ filteredTotal }}
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="pagedData"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="Họ và Tên" min-width="180" />
        <el-table-column prop="email" label="Email" min-width="220" />
        <el-table-column prop="phone" label="SĐT" width="140" />

        <el-table-column label="Loại" width="140" align="center">
          <template #default="scope">
            <el-tag :type="getCustomerTypeTag(scope.row.customerType)">
              {{
                scope.row.customerTypeDisplay ||
                scope.row.customerType ||
                "REGULAR"
              }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Ngày sinh" width="140">
          <template #default="scope">
            <span class="ts-muted">{{ scope.row.dateOfBirth || "-" }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="totalSpent"
          label="Chi tiêu"
          width="140"
          align="right"
        >
          <template #default="scope">
            {{ formatCurrency(scope.row.totalSpent) }}
          </template>
        </el-table-column>

        <el-table-column label="Thao tác" width="190" align="center">
          <template #default="scope">
            <el-button
              size="small"
              :icon="Edit"
              @click="openEditModal(scope.row)"
              >Sửa</el-button
            >
            <el-button
              size="small"
              type="danger"
              :icon="Delete"
              @click="handleDelete(scope.row.id)"
            >
              Xóa
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="filteredTotal"
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
        />
      </div>
    </el-card>

    <!-- EDIT ONLY -->
    <el-dialog
      v-model="dialogVisible"
      title="Cập nhật Khách Hàng"
      width="720px"
      @close="resetForm"
    >
      <el-form
        ref="customerFormRef"
        :model="formData"
        :rules="rules"
        label-position="top"
        status-icon
      >
        <div class="form-grid">
          <el-form-item label="Họ và Tên" prop="fullName">
            <el-input v-model="formData.fullName" placeholder="Nhập họ tên" />
          </el-form-item>

          <el-form-item label="Loại khách" prop="customerType">
            <el-select v-model="formData.customerType" style="width: 100%">
              <el-option label="Khách thường (REGULAR)" value="REGULAR" />
              <el-option label="Khách VIP (VIP)" value="VIP" />
            </el-select>
          </el-form-item>

          <el-form-item label="Email" prop="email">
            <el-input v-model="formData.email" placeholder="example@mail.com" />
          </el-form-item>

          <el-form-item label="Số điện thoại" prop="phone">
            <el-input v-model="formData.phone" placeholder="09xxxxxxxx" />
          </el-form-item>

          <el-form-item label="Ngày sinh" prop="birthDate">
            <el-date-picker
              v-model="formData.birthDate"
              type="date"
              placeholder="Chọn ngày sinh"
              format="DD/MM/YYYY"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="Địa chỉ" prop="address">
            <el-input v-model="formData.address" placeholder="Nhập địa chỉ" />
          </el-form-item>

          <el-form-item label="Ghi chú" prop="notes" class="span-2">
            <el-input
              v-model="formData.notes"
              type="textarea"
              :rows="3"
              placeholder="Ghi chú thêm..."
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Hủy</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">
            Cập nhật
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Edit, Delete, Search } from "@element-plus/icons-vue";
import http from "../../api/http";

const API_URL = "/api/auth/customers";

const loading = ref(false);
const submitting = ref(false);
const tableData = ref([]);

const dialogVisible = ref(false);
const customerFormRef = ref(null);
const currentId = ref(null);

const searchQuery = ref("");
const filterType = ref("");
const page = ref(1);
const pageSize = ref(20);

const formData = reactive({
  fullName: "",
  email: "",
  phone: "",
  birthDate: null,
  address: "",
  customerType: "REGULAR",
  notes: "",
});

const rules = {
  fullName: [
    { required: true, message: "Vui lòng nhập họ tên", trigger: "blur" },
    { min: 2, message: "Tên phải có ít nhất 2 ký tự", trigger: "blur" },
  ],
  email: [
    { required: true, message: "Vui lòng nhập email", trigger: "blur" },
    { type: "email", message: "Email không hợp lệ", trigger: "blur" },
  ],
  phone: [
    { required: true, message: "Vui lòng nhập số điện thoại", trigger: "blur" },
    {
      pattern: /^[0-9]{10,11}$/,
      message: "Số điện thoại phải là số (10-11 số)",
      trigger: "blur",
    },
  ],
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

const pagedData = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredData.value.slice(start, start + pageSize.value);
});

async function fetchCustomers() {
  loading.value = true;
  try {
    const res = await http.get(API_URL);
    const data = Array.isArray(res.data) ? res.data : [];
    tableData.value = data.filter((c) => c?.isActive === true);

    const maxPage = Math.max(
      1,
      Math.ceil(filteredTotal.value / pageSize.value)
    );
    if (page.value > maxPage) page.value = maxPage;
  } catch (err) {
    console.error(err);
    ElMessage.error("Không thể tải danh sách khách hàng");
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  searchQuery.value = "";
  filterType.value = "";
  page.value = 1;
}

function openEditModal(row) {
  currentId.value = row.id;

  formData.fullName = row.name || "";
  formData.email = row.email || "";
  formData.phone = row.phone || "";
  formData.birthDate = row.dateOfBirth || null;
  formData.address = row.address || "";
  formData.customerType = row.customerType || "REGULAR";
  formData.notes = row.notes || "";

  dialogVisible.value = true;
}

async function submitForm() {
  if (!customerFormRef.value) return;

  await customerFormRef.value.validate(async (valid) => {
    if (!valid) return;

    submitting.value = true;
    try {
      await http.put(`${API_URL}/${currentId.value}`, formData);
      ElMessage.success("Cập nhật thành công!");
      dialogVisible.value = false;
      await fetchCustomers();
    } catch (error) {
      const msg = error?.response?.data?.message || "Có lỗi xảy ra";
      ElMessage.error(msg);
    } finally {
      submitting.value = false;
    }
  });
}

function handleDelete(id) {
  ElMessageBox.confirm(
    "Bạn có chắc chắn muốn xóa khách hàng này không?",
    "Cảnh báo",
    {
      confirmButtonText: "Xóa",
      cancelButtonText: "Hủy",
      type: "warning",
    }
  )
    .then(async () => {
      try {
        await http.delete(`${API_URL}/${id}`);
        ElMessage.success("Đã xóa thành công");
        await fetchCustomers();
      } catch {
        ElMessage.error("Xóa thất bại");
      }
    })
    .catch(() => {});
}

function resetForm() {
  if (customerFormRef.value) customerFormRef.value.resetFields();
}

function formatCurrency(value) {
  if (value === null || value === undefined) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
}

function getCustomerTypeTag(type) {
  return type === "VIP" ? "warning" : "success";
}

onMounted(fetchCustomers);
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

.card {
  border-radius: 18px;
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

.right-tools {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  align-items: center;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.span-2 {
  grid-column: span 2;
}

@media (max-width: 720px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  .span-2 {
    grid-column: span 1;
  }
  .right-tools {
    justify-content: flex-start;
  }
}
</style>
