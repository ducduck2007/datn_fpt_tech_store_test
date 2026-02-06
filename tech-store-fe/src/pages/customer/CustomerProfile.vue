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

        <!-- ✅ THÊM TABS -->
        <el-tabs v-model="activeTab" class="mb-3">
          <el-tab-pane label="Thông tin" name="info">
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
                      
                      <div class="col-12">
                        <el-form-item label="Địa chỉ">
                          <el-input 
                            v-if="editing" 
                            v-model="formData.address"
                            placeholder="Nhập địa chỉ của bạn"
                          />
                          <div v-else class="form-static">{{ customer.address || '—' }}</div>
                        </el-form-item>
                      </div>

                      <div class="col-12">
                        <el-form-item label="Ghi chú">
                          <el-input 
                            v-if="editing" 
                            v-model="formData.notes"
                            type="textarea"
                            :rows="3"
                            placeholder="Ghi chú cá nhân (ví dụ: sở thích, yêu cầu đặc biệt...)"
                          />
                          <div v-else class="form-static notes-static">
                            {{ customer.notes || '—' }}
                          </div>
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
          </el-tab-pane>

          <!-- ✅ TAB LỊCH SỬ ĐIỂM -->
          <el-tab-pane label="Lịch sử điểm" name="loyalty">
            <el-card shadow="never">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h3 class="mb-0 fw-bold">
                  <el-icon class="me-2"><TrendCharts /></el-icon>
                  Lịch sử giao dịch điểm
                </h3>
                <el-button @click="loadLoyaltyHistory" :loading="historyLoading">
                  <el-icon><Refresh /></el-icon>
                  Tải lại
                </el-button>
              </div>

              <el-skeleton v-if="historyLoading" :rows="5" animated />
              
              <el-empty v-else-if="!loyaltyHistory.length" description="Chưa có lịch sử giao dịch" />

              <el-timeline v-else>
                <el-timeline-item
                  v-for="item in loyaltyHistory"
                  :key="item.id"
                  :timestamp="formatDateTime(item.createdAt)"
                  placement="top"
                  :type="getTimelineType(item.transactionType)"
                  :icon="getTimelineIcon(item.transactionType)"
                >
                  <el-card>
                    <div class="d-flex justify-content-between align-items-start">
                      <div>
                        <div class="d-flex align-items-center gap-2 mb-2">
                          <el-tag :type="getTransactionTagType(item.transactionType)" size="large">
                            {{ item.transactionTypeDisplay }}
                          </el-tag>
                          <span 
                            class="points-change fw-bold"
                            :class="item.pointsDelta > 0 ? 'text-success' : 'text-danger'"
                          >
                            {{ item.pointsDelta > 0 ? '+' : '' }}{{ item.pointsDelta }} điểm
                          </span>
                        </div>
                        <p class="mb-1 text-muted small">{{ item.note }}</p>
                        <div v-if="item.tierBefore || item.tierAfter" class="tier-change mt-2">
                          <el-tag size="small" :type="getTierTagType(item.tierBefore)">
                            {{ item.tierBeforeDisplay || 'Member' }}
                          </el-tag>
                          <el-icon class="mx-2"><Right /></el-icon>
                          <el-tag size="small" :type="getTierTagType(item.tierAfter)">
                            {{ item.tierAfterDisplay || 'Member' }}
                          </el-tag>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </el-card>
          </el-tab-pane>

          <!-- ✅ TAB LỊCH SỬ THĂNG HẠNG -->
          <el-tab-pane label="Lịch sử thăng hạng" name="tier">
            <el-card shadow="never">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h3 class="mb-0 fw-bold">
                  <el-icon class="me-2"><Trophy /></el-icon>
                  Lịch sử thay đổi hạng VIP
                </h3>
                <el-button @click="loadTierHistory" :loading="tierLoading">
                  <el-icon><Refresh /></el-icon>
                  Tải lại
                </el-button>
              </div>

              <el-skeleton v-if="tierLoading" :rows="5" animated />
              
              <el-empty v-else-if="!tierHistory.length" description="Chưa có lịch sử thay đổi hạng" />

              <div v-else class="tier-history-list">
                <el-card 
                  v-for="item in tierHistory" 
                  :key="item.id"
                  class="mb-3 tier-history-card"
                  :class="item.transactionType === 'TIER_UPGRADE' ? 'upgrade' : 'downgrade'"
                >
                  <div class="d-flex align-items-start gap-3">
                    <div class="tier-icon">
                      <el-icon 
                        :size="32" 
                        :color="item.transactionType === 'TIER_UPGRADE' ? '#67c23a' : '#f56c6c'"
                      >
                        <component :is="item.transactionType === 'TIER_UPGRADE' ? 'TopRight' : 'BottomRight'" />
                      </el-icon>
                    </div>
                    <div class="flex-grow-1">
                      <div class="d-flex justify-content-between align-items-center mb-2">
                        <h5 class="mb-0">
                          <el-tag 
                            :type="item.transactionType === 'TIER_UPGRADE' ? 'success' : 'danger'"
                            effect="dark"
                          >
                            {{ item.transactionTypeDisplay }}
                          </el-tag>
                        </h5>
                        <span class="text-muted small">{{ formatDateTime(item.createdAt) }}</span>
                      </div>
                      
                      <div class="tier-change-display mb-2">
                        <el-tag size="large" :type="getTierTagType(item.tierBefore)" effect="dark">
                          {{ item.tierBeforeDisplay || 'Member' }}
                        </el-tag>
                        <el-icon :size="20" class="mx-2">
                          <component :is="item.transactionType === 'TIER_UPGRADE' ? 'Right' : 'Right'" />
                        </el-icon>
                        <el-tag size="large" :type="getTierTagType(item.tierAfter)" effect="dark">
                          {{ item.tierAfterDisplay || 'Member' }}
                        </el-tag>
                      </div>
                      
                      <p class="mb-0 text-muted">{{ item.note }}</p>
                    </div>
                  </div>
                </el-card>
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { 
  Star, Present, Wallet, TrendCharts, Trophy, Refresh, 
  Right, TopRight, BottomRight 
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { customersApi } from '../../api/customers.api';

const router = useRouter();
const loading = ref(true);
const editing = ref(false);
const saving = ref(false);
const customer = ref(null);
const activeTab = ref('info');

// ✅ THÊM: State cho lịch sử
const historyLoading = ref(false);
const tierLoading = ref(false);
const loyaltyHistory = ref([]);
const tierHistory = ref([]);

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
    const currentCustomer = response?.data?.data || response?.data;
    
    if (!currentCustomer) {
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
    
    if (error.response?.status === 401) {
      ElMessage.warning('Vui lòng đăng nhập lại');
      router.push('/login');
    } else if (error.response?.status === 404) {
      ElMessage.error('Không tìm thấy thông tin khách hàng.');
    } else {
      ElMessage.error('Lỗi khi tải dữ liệu: ' + (error.response?.data?.message || error.message));
    }
  } finally {
    loading.value = false;
  }
};

// ✅ THÊM: Load lịch sử điểm
const loadLoyaltyHistory = async () => {
  if (!customer.value?.id) return;
  
  historyLoading.value = true;
  try {
    const { data } = await customersApi.getLoyaltyHistory(customer.value.id);
    loyaltyHistory.value = data || [];
  } catch (error) {
    console.error('Error loading loyalty history:', error);
    ElMessage.error('Lỗi khi tải lịch sử điểm');
  } finally {
    historyLoading.value = false;
  }
};

// ✅ THÊM: Load lịch sử thăng hạng
const loadTierHistory = async () => {
  if (!customer.value?.id) return;
  
  tierLoading.value = true;
  try {
    const { data } = await customersApi.getTierHistory(customer.value.id);
    tierHistory.value = data || [];
  } catch (error) {
    console.error('Error loading tier history:', error);
    ElMessage.error('Lỗi khi tải lịch sử thăng hạng');
  } finally {
    tierLoading.value = false;
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
  const types = { 
    BRONZE: 'warning', 
    SILVER: 'info', 
    GOLD: 'warning', 
    PLATINUM: 'primary', 
    DIAMOND: 'danger' 
  };
  return types[tier] || 'info';
};

// ✅ THÊM: Helper functions cho timeline
const getTransactionTagType = (type) => {
  const types = {
    EARN: 'success',
    DEDUCT: 'warning',
    PENALTY: 'danger',
    TIER_UPGRADE: 'success',
    TIER_DOWNGRADE: 'info'
  };
  return types[type] || 'info';
};

const getTimelineType = (type) => {
  const types = {
    EARN: 'success',
    DEDUCT: 'warning',
    PENALTY: 'danger',
    TIER_UPGRADE: 'success',
    TIER_DOWNGRADE: 'info'
  };
  return types[type] || 'primary';
};

const getTimelineIcon = (type) => {
  const icons = {
    EARN: 'CirclePlus',
    DEDUCT: 'Remove',
    PENALTY: 'Warning',
    TIER_UPGRADE: 'TopRight',
    TIER_DOWNGRADE: 'BottomRight'
  };
  return icons[type] || 'More';
};

const formatCurrency = (val) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
};

const formatDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—';
const formatDateTime = (d) => {
  if (!d) return '—';
  return new Date(d).toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getProgressPercent = () => {
  if (!customer.value) return 0;
  const total = (customer.value.loyaltyPoints || 0) + (customer.value.pointsToNextTier || 0);
  return total === 0 ? 0 : Math.min(100, Math.round((customer.value.loyaltyPoints / total) * 100));
};

onMounted(async () => {
  console.log('Profile component mounted');
  await loadCustomerData();
  
  // Load lịch sử khi có customer
  if (customer.value?.id) {
    loadLoyaltyHistory();
    loadTierHistory();
  }
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
.notes-static {
  white-space: pre-wrap;
  word-wrap: break-word;
  min-height: 60px;
  align-items: flex-start;
}
.text-dark { color: #303133; }

/* ✅ STYLES MỚI CHO LỊCH SỬ */
.points-change {
  font-size: 18px;
}

.tier-change {
  display: flex;
  align-items: center;
}

.tier-history-card {
  transition: all 0.3s;
}

.tier-history-card.upgrade {
  border-left: 4px solid #67c23a;
}

.tier-history-card.downgrade {
  border-left: 4px solid #f56c6c;
}

.tier-history-card:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.tier-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(103, 194, 58, 0.1);
}

.tier-history-card.downgrade .tier-icon {
  background: rgba(245, 108, 108, 0.1);
}

.tier-change-display {
  display: flex;
  align-items: center;
}
</style>