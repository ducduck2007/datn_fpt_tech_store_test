<template>
  <div class="dashboard-container container-xl pb-5">
    <div class="page-header d-flex justify-content-between align-items-center mb-4">
      <div>
        <div class="text-uppercase fw-bold text-primary mb-1" style="font-size: 12px; letter-spacing: 1px;">Analytics Overview</div>
        <h2 class="m-0 fw-bolder text-dark">Báo Cáo Tổng Quan Sản Phẩm</h2>
        <p class="text-muted mt-1 mb-0" style="font-size: 14px;">Dữ liệu được cập nhật theo thời gian thực từ hệ thống kho hàng và đơn hàng.</p>
      </div>
      <el-button type="primary" size="large" icon="Refresh" :loading="loading" @click="loadData" round class="shadow-sm px-4">Làm mới dữ liệu</el-button>
      <el-button type="success" @click="downloadExcel" :loading="exporting" icon="Download" round>Xuất Excel</el-button>
    </div>

    <el-row :gutter="24" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="custom-stat-card card-primary">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <p class="stat-label">SẢN PHẨM ĐANG KINH DOANH</p>
              <h3 class="stat-value">{{ stats.totalActiveProducts }}</h3>
            </div>
            <div class="stat-icon-wrapper bg-primary-light text-primary">
              <el-icon><Goods /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="custom-stat-card card-success">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <p class="stat-label">TỔNG SỐ MÁY TRONG KHO</p>
              <h3 class="stat-value">{{ stats.totalSerialsInStock }}</h3>
            </div>
            <div class="stat-icon-wrapper bg-success-light text-success">
              <el-icon><Box /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="custom-stat-card card-warning">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <p class="stat-label">BIẾN THỂ BÁO HẾT HÀNG</p>
              <h3 class="stat-value">{{ stats.outOfStockVariants }}</h3>
            </div>
            <div class="stat-icon-wrapper bg-warning-light text-warning">
              <el-icon><Warning /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="custom-stat-card card-danger">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <p class="stat-label">MÁY BỊ LỖI (FAULTY)</p>
              <h3 class="stat-value">{{ stats.totalSerialsFaulty }}</h3>
            </div>
            <div class="stat-icon-wrapper bg-danger-light text-danger">
              <el-icon><CircleClose /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card border-0 shadow-sm rounded-4 mt-2">
      <template #header>
        <div class="d-flex justify-content-between align-items-center py-2">
          <div class="d-flex align-items-center">
            <div class="icon-square bg-warning-light text-warning me-3">
              <el-icon><Trophy /></el-icon>
            </div>
            <h4 class="m-0 fw-bolder text-dark">Top 10 Sản Phẩm Bán Chạy Nhất</h4>
          </div>
          <el-tag type="info" effect="plain" class="border-0 bg-light text-muted fw-bold">Xếp hạng theo số lượng</el-tag>
        </div>
      </template>

      <el-table :data="stats.topSellingProducts" v-loading="loading" style="width: 100%" class="custom-table" :row-class-name="tableRowClassName">
        
        <el-table-column label="Hạng" width="100" align="center">
          <template #default="scope">
            <div :class="['rank-badge', `rank-${scope.$index + 1}`]">
              {{ scope.$index + 1 }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="productName" label="Tên sản phẩm" min-width="350">
          <template #default="{ row }">
            <div class="product-name-cell">
              <span class="fw-bold text-dark fs-6">{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="totalSold" label="Đã bán" width="180" align="center">
          <template #default="{ row }">
            <div class="sold-badge">
              <el-icon class="me-2"><ShoppingCart /></el-icon>
              <span class="fw-bold">{{ row.totalSold }}</span> <span class="small opacity-75 ms-1">chiếc</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="totalRevenue" label="Tổng doanh thu mang lại" width="250" align="right">
          <template #default="{ row }">
            <span class="revenue-text">{{ formatCurrency(row.totalRevenue) }}</span>
          </template>
        </el-table-column>

      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { productsApi } from '../../api/products.api';
import { toast } from '../../ui/toast';
// Đảm bảo import đủ các Icon hiển thị trên thẻ
import { Goods, Box, Warning, CircleClose, Trophy, ShoppingCart, Refresh } from '@element-plus/icons-vue';

const loading = ref(false);
const stats = ref({
  totalActiveProducts: 0,
  totalSerialsInStock: 0,
  outOfStockVariants: 0,
  totalSerialsFaulty: 0,
  topSellingProducts: []
});

function formatCurrency(val) {
  if (!val) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

async function loadData() {
  loading.value = true;
  try {
    const res = await productsApi.getDashboardStats();
    stats.value = res.data;
  } catch (e) {
    toast("Không thể tải dữ liệu thống kê", "error");
  } finally {
    loading.value = false;
  }
}

const exporting = ref(false);

async function downloadExcel() {
  exporting.value = true;
  try {
    const res = await productsApi.exportReport();
    
    // Logic tạo file và kích hoạt tải xuống trình duyệt
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `Bao_Cao_San_Pham_${new Date().getTime()}.xlsx`);
    document.body.appendChild(link);
    link.click();
    
    // Dọn dẹp
    link.remove();
    window.URL.revokeObjectURL(url);
    
    toast("Xuất báo cáo Excel thành công!", "success");
  } catch (error) {
    console.error(error);
    toast("Lỗi khi xuất file Excel", "error");
  } finally {
    exporting.value = false;
  }
}

// Hàm gán class cho các dòng Top 1,2,3 để bôi màu highlight
function tableRowClassName({ rowIndex }) {
  if (rowIndex === 0) return 'highlight-row top-1-row';
  if (rowIndex === 1) return 'highlight-row top-2-row';
  if (rowIndex === 2) return 'highlight-row top-3-row';
  return '';
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.dashboard-container {
  padding-top: 10px;
}

/* --- STAT CARDS --- */
.custom-stat-card {
  border: none;
  border-radius: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}
.custom-stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.08) !important;
}
.stat-label {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}
.stat-value {
  font-size: 32px;
  font-weight: 900;
  color: #1e293b;
  margin: 0;
  line-height: 1;
}
.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

/* Các viền màu nhấn cho thẻ */
.card-primary { border-bottom: 4px solid #3b82f6; }
.card-success { border-bottom: 4px solid #10b981; }
.card-warning { border-bottom: 4px solid #f59e0b; }
.card-danger { border-bottom: 4px solid #ef4444; }

.bg-primary-light { background-color: #eff6ff; }
.bg-success-light { background-color: #ecfdf5; }
.bg-warning-light { background-color: #fffbeb; }
.bg-danger-light { background-color: #fef2f2; }

/* --- ICON SQUARE TRÊN TIÊU ĐỀ BẢNG --- */
.icon-square {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

/* --- TABLE STYLING --- */
.custom-table :deep(.el-table__header th) {
  background-color: #f8fafc;
  color: #475569;
  font-weight: 700;
  text-transform: uppercase;
  font-size: 13px;
  padding: 14px 0;
}

/* Rank Badges */
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-weight: 800;
  font-size: 14px;
  color: #64748b;
  background-color: #f1f5f9;
}
.rank-1 { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); color: white; box-shadow: 0 4px 10px rgba(246, 211, 101, 0.4); transform: scale(1.15); }
.rank-2 { background: linear-gradient(135deg, #e2e2e2 0%, #9ca3af 100%); color: white; box-shadow: 0 4px 10px rgba(156, 163, 175, 0.4); transform: scale(1.05); }
.rank-3 { background: linear-gradient(135deg, #d4af37 0%, #aa6c39 100%); color: white; box-shadow: 0 4px 10px rgba(212, 175, 55, 0.4); transform: scale(1.05); }

/* Highlight Top 3 Rows */
:deep(.top-1-row) { background-color: #fffbf0 !important; }
:deep(.top-2-row) { background-color: #f8fafc !important; }
:deep(.top-3-row) { background-color: #fdfaf6 !important; }

/* Số lượng bán & Doanh thu */
.sold-badge {
  display: inline-flex;
  align-items: center;
  background-color: #ecfdf5;
  color: #059669;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 14px;
}
.revenue-text {
  font-size: 16px;
  font-weight: 800;
  color: #10b981; /* Màu xanh lá cho dòng tiền */
}
.product-name-cell {
  padding: 8px 0;
}
</style>