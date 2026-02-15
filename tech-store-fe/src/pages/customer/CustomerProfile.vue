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

          <!-- ✅ TAB LỊCH SỬ ĐIỂM - ENHANCED -->
          <el-tab-pane label="Lịch sử điểm" name="loyalty">
            <!-- Statistics Summary -->
            <el-row :gutter="16" class="mb-3">
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card earn-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><CirclePlus /></el-icon>
                    <div>
                      <div class="stats-value">+{{ loyaltyStats.totalEarned }}</div>
                      <div class="stats-label">Tổng điểm cộng</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card deduct-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><Remove /></el-icon>
                    <div>
                      <div class="stats-value">{{ loyaltyStats.totalDeducted }}</div>
                      <div class="stats-label">Tổng điểm trừ</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card transactions-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><List /></el-icon>
                    <div>
                      <div class="stats-value">{{ loyaltyStats.totalTransactions }}</div>
                      <div class="stats-label">Giao dịch</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-card shadow="never">
              <!-- Header with filters -->
              <div class="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-3">
                <div>
                  <h3 class="mb-2 fw-bold">
                    <el-icon class="me-2"><TrendCharts /></el-icon>
                    Lịch sử giao dịch điểm
                  </h3>
                  <div class="d-flex gap-2 flex-wrap" v-if="activeHistoryFilters.length">
                    <el-tag 
                      v-for="filter in activeHistoryFilters" 
                      :key="filter.type"
                      :type="filter.tagType"
                      closable
                      @close="removeHistoryFilter(filter.type)"
                    >
                      {{ filter.label }}
                    </el-tag>
                  </div>
                </div>
                
                <div class="d-flex gap-2 flex-wrap">
                  <el-select 
                    v-model="historyTransactionFilter" 
                    placeholder="Loại giao dịch"
                    clearable
                    style="width: 160px"
                    @change="filterLoyaltyHistory"
                  >
                    <el-option label="Cộng điểm" value="EARN">
                      <el-tag type="success" size="small">Cộng điểm</el-tag>
                    </el-option>
                    <el-option label="Trừ điểm" value="DEDUCT">
                      <el-tag type="warning" size="small">Trừ điểm</el-tag>
                    </el-option>
                    <el-option label="Phạt" value="PENALTY">
                      <el-tag type="danger" size="small">Phạt</el-tag>
                    </el-option>
                    <el-option label="Thăng hạng" value="TIER_UPGRADE">
                      <el-tag type="success" size="small">Thăng hạng</el-tag>
                    </el-option>
                    <el-option label="Hạ hạng" value="TIER_DOWNGRADE">
                      <el-tag type="info" size="small">Hạ hạng</el-tag>
                    </el-option>
                  </el-select>

                  <el-date-picker
                    v-model="historyDateRange"
                    type="daterange"
                    range-separator="-"
                    start-placeholder="Từ ngày"
                    end-placeholder="Đến ngày"
                    format="DD/MM/YYYY"
                    value-format="YYYY-MM-DD"
                    style="width: 280px"
                    @change="filterLoyaltyHistory"
                  />

                  <el-button @click="clearHistoryFilters" v-if="hasActiveHistoryFilters">
                    <el-icon><Close /></el-icon>
                    Xóa bộ lọc
                  </el-button>

                  <el-button @click="loadLoyaltyHistory" :loading="historyLoading">
                    <el-icon><Refresh /></el-icon>
                    Tải lại
                  </el-button>
                </div>
              </div>

              <el-divider />

              <el-skeleton v-if="historyLoading" :rows="5" animated />
              
              <el-empty v-else-if="!filteredLoyaltyHistory.length" description="Không có giao dịch nào">
                <el-button type="primary" @click="clearHistoryFilters" v-if="hasActiveHistoryFilters">
                  Xóa bộ lọc
                </el-button>
              </el-empty>

              <!-- Timeline with enhanced cards -->
              <el-timeline v-else>
                <el-timeline-item
                  v-for="item in filteredLoyaltyHistory"
                  :key="item.id"
                  :timestamp="formatDateTime(item.createdAt)"
                  placement="top"
                  :type="getTimelineType(item.transactionType)"
                >
                  <el-card class="history-card" :class="getHistoryCardClass(item.transactionType)">
                    <div class="history-card-content">
                      <!-- Left: Icon & Type -->
                      <div class="history-icon-wrapper">
                        <el-icon :size="28" :color="getTransactionIconColor(item.transactionType)">
                          <component :is="getTransactionIcon(item.transactionType)" />
                        </el-icon>
                      </div>

                      <!-- Middle: Details -->
                      <div class="history-details flex-grow-1">
                        <div class="d-flex align-items-center gap-2 mb-1">
                          <el-tag :type="getTransactionTagType(item.transactionType)" effect="dark">
                            {{ item.transactionTypeDisplay }}
                          </el-tag>
                          <span class="text-muted small" v-if="item.reason">{{ item.reason }}</span>
                        </div>
                        
                        <p class="mb-2 history-note">{{ item.note }}</p>
                        
                        <!-- Tier change indicator -->
                        <div v-if="item.tierBefore || item.tierAfter" class="tier-change-indicator">
                          <el-tag size="small" :type="getTierTagType(item.tierBefore)" effect="plain">
                            {{ item.tierBeforeDisplay || 'Member' }}
                          </el-tag>
                          <el-icon class="mx-2" :size="16">
                            <Right />
                          </el-icon>
                          <el-tag size="small" :type="getTierTagType(item.tierAfter)" effect="dark">
                            {{ item.tierAfterDisplay || 'Member' }}
                          </el-tag>
                        </div>

                        <!-- Reference info -->
                        <div v-if="item.referenceType" class="reference-info mt-2">
                          <el-tag size="small" type="info" effect="plain">
                            <el-icon class="me-1"><Link /></el-icon>
                            {{ item.referenceType }}: #{{ item.referenceId }}
                          </el-tag>
                        </div>
                      </div>

                      <!-- Right: Points -->
                      <div class="history-points">
                        <div 
                          class="points-badge"
                          :class="item.pointsDelta > 0 ? 'points-positive' : 'points-negative'"
                        >
                          <span class="points-sign">{{ item.pointsDelta > 0 ? '+' : '' }}</span>
                          <span class="points-value">{{ Math.abs(item.pointsDelta) }}</span>
                          <span class="points-unit">điểm</span>
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
                          <Right />
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
import { ref, onMounted, computed } from 'vue';
import { 
  Star, Present, Wallet, TrendCharts, Trophy, Refresh, 
  Right, TopRight, BottomRight, CirclePlus, Remove, List,
  Close, Link
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

// ✅ State cho lịch sử
const historyLoading = ref(false);
const tierLoading = ref(false);
const loyaltyHistory = ref([]);
const tierHistory = ref([]);

// ✅ Filters cho lịch sử
const historyTransactionFilter = ref('');
const historyDateRange = ref(null);

const formData = ref({
  fullName: '',
  email: '',
  phone: '',
  address: '',
  notes: '',
  birthDate: ''
});

// ✅ Computed: Loyalty Statistics
const loyaltyStats = computed(() => {
  const earned = loyaltyHistory.value
    .filter(h => h.pointsDelta > 0)
    .reduce((sum, h) => sum + h.pointsDelta, 0);
  
  const deducted = loyaltyHistory.value
    .filter(h => h.pointsDelta < 0)
    .reduce((sum, h) => sum + Math.abs(h.pointsDelta), 0);
  
  return {
    totalEarned: earned.toLocaleString(),
    totalDeducted: deducted.toLocaleString(),
    totalTransactions: loyaltyHistory.value.length
  };
});

// ✅ Computed: Filtered History
const filteredLoyaltyHistory = computed(() => {
  let result = [...loyaltyHistory.value];
  
  // Filter by transaction type
  if (historyTransactionFilter.value) {
    result = result.filter(h => h.transactionType === historyTransactionFilter.value);
  }
  
  // Filter by date range
  if (historyDateRange.value && historyDateRange.value.length === 2) {
    const [start, end] = historyDateRange.value;
    result = result.filter(h => {
      const date = new Date(h.createdAt);
      return date >= new Date(start) && date <= new Date(end + 'T23:59:59');
    });
  }
  
  return result;
});

// ✅ Computed: Active Filters Display
const activeHistoryFilters = computed(() => {
  const filters = [];
  
  if (historyTransactionFilter.value) {
    const labels = {
      EARN: 'Cộng điểm',
      DEDUCT: 'Trừ điểm',
      PENALTY: 'Phạt',
      TIER_UPGRADE: 'Thăng hạng',
      TIER_DOWNGRADE: 'Hạ hạng'
    };
    filters.push({
      type: 'transaction',
      label: labels[historyTransactionFilter.value],
      tagType: getTransactionTagType(historyTransactionFilter.value)
    });
  }
  
  if (historyDateRange.value) {
    const [start, end] = historyDateRange.value;
    filters.push({
      type: 'date',
      label: `${formatDate(start)} - ${formatDate(end)}`,
      tagType: 'info'
    });
  }
  
  return filters;
});

const hasActiveHistoryFilters = computed(() => {
  return historyTransactionFilter.value || historyDateRange.value;
});

// ✅ Load customer data
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

// ✅ Load lịch sử điểm
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

// ✅ Load lịch sử thăng hạng
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

// ✅ Filter functions
const filterLoyaltyHistory = () => {
  // Filtering is handled by computed property
};

const removeHistoryFilter = (type) => {
  if (type === 'transaction') {
    historyTransactionFilter.value = '';
  } else if (type === 'date') {
    historyDateRange.value = null;
  }
};

const clearHistoryFilters = () => {
  historyTransactionFilter.value = '';
  historyDateRange.value = null;
};

// ✅ Edit functions
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

// ✅ Helper functions
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

const getTransactionIcon = (type) => {
  const icons = {
    EARN: 'CirclePlus',
    DEDUCT: 'Remove',
    PENALTY: 'Warning',
    TIER_UPGRADE: 'TopRight',
    TIER_DOWNGRADE: 'BottomRight'
  };
  return icons[type] || 'More';
};

const getTransactionIconColor = (type) => {
  const colors = {
    EARN: '#67c23a',
    DEDUCT: '#e6a23c',
    PENALTY: '#f56c6c',
    TIER_UPGRADE: '#67c23a',
    TIER_DOWNGRADE: '#909399'
  };
  return colors[type] || '#409eff';
};

const getHistoryCardClass = (type) => {
  const classes = {
    EARN: 'history-earn',
    DEDUCT: 'history-deduct',
    PENALTY: 'history-penalty',
    TIER_UPGRADE: 'history-upgrade',
    TIER_DOWNGRADE: 'history-downgrade'
  };
  return classes[type] || '';
};

const formatCurrency = (val) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
};

const formatDate = (d) => {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('vi-VN');
};

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

/* ✅ STATS CARDS */
.stats-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.stats-card:hover {
  transform: translateY(-4px);
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  flex-shrink: 0;
}

.stats-value {
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 13px;
  color: #909399;
  font-weight: 600;
}

.earn-card {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.earn-card .stats-value {
  color: #67c23a;
}

.deduct-card {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
}

.deduct-card .stats-value {
  color: #e6a23c;
}

.transactions-card {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.transactions-card .stats-value {
  color: #409eff;
}

/* ✅ HISTORY CARDS */
.history-card {
  transition: all 0.3s;
  border-radius: 12px;
}

.history-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.history-card-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.history-icon-wrapper {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.1);
}

.history-earn .history-icon-wrapper {
  background: rgba(103, 194, 58, 0.1);
}

.history-deduct .history-icon-wrapper {
  background: rgba(230, 162, 60, 0.1);
}

.history-penalty .history-icon-wrapper {
  background: rgba(245, 108, 108, 0.1);
}

.history-upgrade .history-icon-wrapper {
  background: rgba(103, 194, 58, 0.1);
}

.history-downgrade .history-icon-wrapper {
  background: rgba(144, 147, 153, 0.1);
}

.history-details {
  flex: 1;
  min-width: 0;
}

.history-note {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.tier-change-indicator {
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.reference-info {
  display: flex;
  align-items: center;
}

.history-points {
  flex-shrink: 0;
}

.points-badge {
  padding: 8px 16px;
  border-radius: 8px;
  text-align: center;
  min-width: 100px;
}

.points-positive {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border: 2px solid #67c23a;
}

.points-negative {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border: 2px solid #e6a23c;
}

.points-sign {
  font-size: 14px;
  font-weight: 600;
}

.points-value {
  font-size: 24px;
  font-weight: 900;
  margin: 0 4px;
}

.points-positive .points-value {
  color: #67c23a;
}

.points-negative .points-value {
  color: #e6a23c;
}

.points-unit {
  font-size: 12px;
  color: #909399;
}

/* ✅ TIER HISTORY */
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

/* ✅ RESPONSIVE */
@media (max-width: 768px) {
  .stats-content {
    flex-direction: column;
    text-align: center;
  }
  
  .history-card-content {
    flex-direction: column;
  }
  
  .history-points {
    width: 100%;
  }
  
  .points-badge {
    width: 100%;
  }
}
</style>