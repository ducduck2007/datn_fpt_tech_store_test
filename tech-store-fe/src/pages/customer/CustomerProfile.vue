<template>
  <div class="profile-container">
    <div class="container-xl py-4">
      <el-skeleton v-if="loading" :rows="8" animated />

      <template v-else-if="customer">
        <el-card shadow="never" class="mb-3">
          <div class="d-flex align-items-center justify-content-between flex-wrap gap-3">
            <div class="d-flex align-items-center gap-3">
              <div class="profile-avatar">
                {{ customer.name?.charAt(0) || 'U' }}
              </div>
              <div>
                <h1 class="mb-2 fw-bold text-dark">{{ customer.name }}</h1>
                <div class="d-flex gap-2 flex-wrap">
                  <el-tag :type="getTierTagType(customer.vipTier)" effect="dark">
                    {{ customer.vipTierDisplay }}
                  </el-tag>
                  <el-tag type="info">
                    {{ customer.customerTypeDisplay }}
                  </el-tag>
                </div>
              </div>
            </div>

            <div v-if="!editing">
              <el-button type="primary" @click="startEdit">
                Chỉnh sửa
              </el-button>
            </div>
            <div v-else class="d-flex gap-2">
              <el-button type="success" :loading="saving" @click="handleSave">
                {{ saving ? 'Đang lưu...' : 'Lưu' }}
              </el-button>
              <el-button @click="handleCancel" :disabled="saving">
                Hủy
              </el-button>
            </div>
          </div>
        </el-card>

        <div class="row g-3">
          <div class="col-12 col-lg-4">
            <el-card shadow="hover" class="mb-3">
              <div class="d-flex align-items-center gap-2 mb-3">
                <el-icon :size="24" color="#f59e0b"><Star /></el-icon>
                <h3 class="mb-0 fw-bold">Điểm tích lũy</h3>
              </div>
              <div class="text-center py-3">
                <div class="points-display">
                  {{ customer.loyaltyPoints?.toLocaleString() || 0 }}
                </div>
                <div class="muted">điểm</div>
              </div>
              <el-divider />
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small text-muted">Lên hạng tiếp:</span>
                <span class="fw-bold text-primary">
                  {{ customer.pointsToNextTier > 0 
                    ? `${customer.pointsToNextTier} điểm`
                    : 'Hạng cao nhất' }}
                </span>
              </div>
              <el-progress
                v-if="customer.pointsToNextTier > 0"
                :percentage="getProgressPercent()"
                :stroke-width="8"
                :show-text="false"
              />
            </el-card>

            <el-card shadow="hover" class="mb-3 discount-card">
              <div class="d-flex align-items-center gap-2 mb-3">
                <el-icon :size="24"><Present /></el-icon>
                <h3 class="mb-0 fw-bold">Ưu đãi của bạn</h3>
              </div>
              <div class="text-center py-3">
                <div class="discount-display">
                  {{ customer.discountRate || 0 }}%
                </div>
                <div class="discount-desc">Giảm giá mọi đơn hàng</div>
              </div>
            </el-card>

            <el-card shadow="hover">
              <div class="d-flex align-items-center gap-2 mb-3">
                <el-icon :size="24" color="#9333ea"><Wallet /></el-icon>
                <h3 class="mb-0 fw-bold">Tổng chi tiêu</h3>
              </div>
              <div class="total-spent">
                {{ formatCurrency(customer.totalSpent) }}
              </div>
            </el-card>
          </div>

          <div class="col-12 col-lg-8">
            <el-card shadow="never" class="mb-3 text-dark">
              <h2 class="mb-4 fw-bold">Thông tin cá nhân</h2>
              <el-form label-position="top" :model="formData">
                <div class="row g-3">
                  <div class="col-12 col-md-6">
                    <el-form-item label="Họ và tên">
                      <el-input v-if="editing" v-model="formData.fullName" />
                      <div v-else class="form-static">{{ customer.name }}</div>
                    </el-form-item>
                  </div>
                  <div class="col-12 col-md-6">
                    <el-form-item label="Email">
                      <el-input v-if="editing" v-model="formData.email" />
                      <div v-else class="form-static">{{ customer.email }}</div>
                    </el-form-item>
                  </div>
                  <div class="col-12 col-md-6">
                    <el-form-item label="Số điện thoại">
                      <el-input v-if="editing" v-model="formData.phone" />
                      <div v-else class="form-static">{{ customer.phone }}</div>
                    </el-form-item>
                  </div>
                  <div class="col-12 col-md-6">
                    <el-form-item label="Ngày sinh">
                      <el-date-picker
                        v-if="editing"
                        v-model="formData.birthDate"
                        type="date"
                        format="DD/MM/YYYY"
                        value-format="YYYY-MM-DD"
                        style="width: 100%"
                      />
                      <div v-else class="form-static">{{ formatDate(customer.dateOfBirth) }}</div>
                    </el-form-item>
                  </div>
                </div>
              </el-form>
            </el-card>

            <el-card shadow="never" class="text-dark">
              <h2 class="mb-4 fw-bold">Thông tin tài khoản</h2>
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="small fw-bold text-muted mb-2 d-block">Hạng thành viên</label>
                  <el-tag :type="getTierTagType(customer.vipTier)" size="large" effect="dark">
                    {{ customer.vipTierDisplay }}
                  </el-tag>
                </div>
                <div class="col-12 col-md-6">
                  <label class="small fw-bold text-muted mb-2 d-block">Ngày tạo</label>
                  <div class="form-static-info">{{ formatDateTime(customer.createdAt) }}</div>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Star, Present, Wallet } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { customersApi } from '../../api/customers.api';

const router = useRouter();
const loading = ref(true);
const editing = ref(false);
const saving = ref(false);
const customer = ref(null);

const formData = ref({
  fullName: '',
  email: '',
  phone: '',
  address: '',
  notes: '',
  birthDate: ''
});

const loadCustomerData = async () => {
  loading.value = true;
  try {
    const response = await customersApi.getProfile();
    
    // Debug response
    console.log('API Response:', response);
    console.log('Response data:', response?.data);
    
    // Xử lý response tùy theo cấu trúc
    const currentCustomer = response?.data?.data || response?.data;
    
    if (!currentCustomer) {
      console.error('Không có dữ liệu khách hàng trong response');
      ElMessage.error('Không tìm thấy thông tin khách hàng');
      return;
    }
    
    customer.value = currentCustomer;
    formData.value = {
      fullName: currentCustomer.name || '',
      email: currentCustomer.email || '',
      phone: currentCustomer.phone || '',
      address: currentCustomer.address || '',
      notes: currentCustomer.notes || '',
      birthDate: currentCustomer.dateOfBirth || ''
    };
    
  } catch (error) {
    console.error('Error loading customer data:', error);
    console.error('Error response:', error.response);
    
    if (error.response?.status === 401) {
      ElMessage.warning('Vui lòng đăng nhập lại');
      // Redirect to login
      router.push('/login');
    } else if (error.response?.status === 404) {
      ElMessage.error('Không tìm thấy thông tin khách hàng. Vui lòng liên hệ quản trị viên.');
    } else {
      ElMessage.error('Lỗi khi tải dữ liệu: ' + (error.response?.data?.message || error.message));
    }
  } finally {
    loading.value = false;
  }
};

const startEdit = () => { editing.value = true; };
const handleCancel = () => { editing.value = false; loadCustomerData(); };

const handleSave = async () => {
  saving.value = true;
  try {
    await customersApi.update(customer.value.id, formData.value);
    await loadCustomerData();
    editing.value = false;
    ElMessage.success('Cập nhật thành công!');
  } catch (error) {
    console.error('Error saving customer data:', error);
    ElMessage.error('Lỗi khi lưu dữ liệu: ' + (error.response?.data?.message || error.message));
  } finally {
    saving.value = false;
  }
};

const getTierTagType = (tier) => {
  const types = { BRONZE: 'warning', SILVER: 'info', GOLD: 'warning', PLATINUM: 'primary', DIAMOND: 'danger' };
  return types[tier] || 'info';
};

const formatCurrency = (val) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
};

const formatDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—';
const formatDateTime = (d) => d ? new Date(d).toLocaleString('vi-VN') : '—';

const getProgressPercent = () => {
  if (!customer.value) return 0;
  const total = (customer.value.loyaltyPoints || 0) + (customer.value.pointsToNextTier || 0);
  return total === 0 ? 0 : Math.min(100, Math.round((customer.value.loyaltyPoints / total) * 100));
};

onMounted(() => {
  console.log('Profile component mounted');
  loadCustomerData();
});
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 20px 0;
}
.profile-avatar {
  width: 80px; height: 80px; border-radius: 50%;
  background: #409eff; color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 32px; font-weight: bold;
}
.points-display { 
  font-size: 48px; 
  font-weight: 900; 
  color: #f59e0b; 
}
.discount-card { 
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%); 
  color: white; 
  border: none;
}
.discount-display {
  font-size: 48px;
  font-weight: 900;
  color: white;
}
.discount-desc {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin-top: 8px;
}
.total-spent { 
  font-size: 32px; 
  font-weight: bold; 
  color: #9333ea; 
}
.muted {
  color: #909399;
  font-size: 14px;
}
.form-static, .form-static-info {
  padding: 8px 12px; 
  background: #f5f7fa; 
  border-radius: 4px; 
  min-height: 40px;
  display: flex;
  align-items: center;
}
.text-dark { color: #303133; }
</style>