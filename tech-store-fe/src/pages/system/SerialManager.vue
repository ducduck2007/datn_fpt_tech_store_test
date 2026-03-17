<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <div class="kicker">Quản lý Kho</div>
          <div class="title">Danh sách Số Seri</div>
          <div class="muted">Quản lý và theo dõi chi tiết từng sản phẩm trong hệ thống</div>
        </div>
        <el-radio-group v-model="filterStatus" @change="load" size="small">
          <el-radio-button label="">Tất cả</el-radio-button>
          <el-radio-button label="IN_STOCK">Trong kho</el-radio-button>
          <el-radio-button label="SOLD">Đã bán</el-radio-button>
          <el-radio-button label="FAULTY">Lỗi / Hỏng</el-radio-button>
        </el-radio-group>
      </div>

      <div class="row g-3 mb-4">
        <div class="col-md-4">
          <el-input 
            v-model="keyword" 
            placeholder="Tìm kiếm theo số Seri / IMEI..." 
            clearable 
            @clear="load"
            @keyup.enter="load"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="col-md-2">
          <el-button type="primary" @click="load" icon="Filter">Lọc dữ liệu</el-button>
        </div>
      </div>

      <el-table :data="rows" border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        
        <el-table-column label="Số Seri / IMEI" min-width="180">
          <template #default="{ row }">
            <span class="fw-bold font-monospace text-primary">{{ row.serialNumber }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Thông tin sản phẩm" min-width="250">
          <template #default="{ row }">
            <div class="fw-bold">{{ row.productName }}</div>
            <div class="small text-muted">Phân loại: {{ row.variantName }}</div>
          </template>
        </el-table-column>

        <el-table-column label="Trạng thái" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" effect="dark" size="small">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Thao tác" width="200" align="center">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd) => handleStatusUpdate(row, cmd)">
              <el-button size="small">
                Đổi trạng thái <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="IN_STOCK" :disabled="row.status === 'IN_STOCK'">Đánh dấu: Trong kho</el-dropdown-item>
                  <el-dropdown-item command="FAULTY" :disabled="row.status === 'FAULTY'" divided>Đánh dấu: Lỗi / Hỏng</el-dropdown-item>
                  <el-dropdown-item command="SOLD" :disabled="row.status === 'SOLD'">Đánh dấu: Đã bán</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="d-flex justify-content-center mt-4">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="20"
          v-model:current-page="page"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { serialsApi } from '../../api/serials.api';
import { toast } from '../../ui/toast';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const page = ref(1);
const keyword = ref('');
const filterStatus = ref('');

async function load() {
  loading.value = true;
  try {
    const res = await serialsApi.list({
      page: page.value - 1,
      keyword: keyword.value,
      status: filterStatus.value || undefined
    });
    const data = res.data?.data || res.data;
    rows.value = data.content;
    total.value = data.totalElements;
  } catch (e) {
    toast("Lỗi tải dữ liệu Seri", "error");
  } finally {
    loading.value = false;
  }
}

function onPageChange() {
  load();
}

async function handleStatusUpdate(row, newStatus) {
  try {
    await serialsApi.updateStatus(row.id, newStatus);
    toast(`Đã đổi trạng thái Seri sang ${formatStatus(newStatus)}`, "success");
    load();
  } catch (e) {
    toast("Cập nhật thất bại", "error");
  }
}

// Hàm lấy màu sắc cho Tag
function getStatusTag(status) {
  switch (status) {
    case 'IN_STOCK': return 'success';
    case 'SOLD': return 'info';
    case 'FAULTY': return 'danger';
    default: return 'warning';
  }
}

// Hàm dịch chữ tiếng Anh từ Database sang tiếng Việt hiển thị cho đẹp
function formatStatus(status) {
  switch (status) {
    case 'IN_STOCK': return 'Trong kho';
    case 'SOLD': return 'Đã bán';
    case 'FAULTY': return 'Lỗi / Hỏng';
    default: return status;
  }
}

onMounted(load);
</script>

<style scoped>
.kicker { font-size: 11px; font-weight: 800; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; }
.title { font-weight: 800; font-size: 22px; color: #1e293b; margin-bottom: 4px; }
.muted { color: #64748b; font-size: 13px; }
.font-monospace { font-family: 'Courier New', Courier, monospace; letter-spacing: 1px; }
</style>