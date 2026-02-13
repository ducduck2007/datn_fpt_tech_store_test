<!-- FILE: src/components/TierProgressBar.vue -->
<template>
  <div v-if="progress" class="tier-progress-container">
    <!-- VIP Upgrade Alert - Hiển thị trước khi gần lên hạng -->
    <el-card 
      v-if="progress.willUpgradeToVip && progress.currentCustomerType === 'REGULAR'"
      shadow="always"
      class="vip-upgrade-card mb-3"
    >
      <div class="d-flex align-items-start gap-3">
        <div class="upgrade-icon-large">👑</div>
        <div class="flex-grow-1">
          <h3 class="mb-2 upgrade-title">Sắp trở thành VIP!</h3>
          <p class="upgrade-message mb-2">
            Chỉ còn <span class="highlight-points">{{ progress.pointsNeededForVip?.toLocaleString() }} điểm</span> 
            nữa là bạn trở thành <span class="highlight-vip">KHÁCH HÀNG VIP</span>!
          </p>
          <p class="upgrade-amount mb-3">
            ≈ {{ formatMoney(progress.amountNeededForVip) }}
          </p>
          <div class="vip-benefits">
            <div class="benefit-item">✨ Giảm giá cao hơn</div>
            <div class="benefit-item">🎁 Ưu đãi độc quyền</div>
            <div class="benefit-item">⚡ Tích điểm nhanh gấp đôi</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Close to Upgrade Alert -->
    <el-card 
      v-if="progress.isCloseToUpgrade && progress.nextTier"
      shadow="always"
      class="close-upgrade-card mb-3"
    >
      <div class="d-flex align-items-start gap-3">
        <div class="upgrade-icon-large">🔥</div>
        <div class="flex-grow-1">
          <h3 class="mb-2 upgrade-title">Bạn sắp lên hạng rồi!</h3>
          <p class="upgrade-message mb-2">
            Chỉ còn <span class="highlight-points">{{ progress.pointsGap?.toLocaleString() }} điểm</span> 
            nữa là bạn sẽ lên hạng <span class="highlight-tier">{{ progress.nextTierDisplay }}</span>!
          </p>
          <p class="upgrade-amount">
            ≈ {{ formatMoney(progress.amountGapToNextTier) }}
          </p>
        </div>
      </div>
    </el-card>

    <!-- Main Progress Card -->
    <el-card shadow="hover" class="tier-progress-card">
      <div class="progress-header">
        <div class="current-info">
          <div class="tier-badge" :style="{ backgroundColor: getTierColor(progress.currentTier) }">
            {{ progress.currentTierDisplay || 'Member' }}
          </div>
          <div class="points-display">
            <div class="points-value">{{ progress.currentPoints?.toLocaleString() }}</div>
            <div class="points-label">Điểm tích lũy</div>
          </div>
        </div>
        
        <div v-if="progress.nextTier" class="next-tier-info">
          <div class="next-label">Hạng tiếp theo:</div>
          <div class="next-tier-badge" :style="{ color: getTierColor(progress.nextTier) }">
            {{ progress.nextTierDisplay }}
          </div>
        </div>
      </div>

      <!-- Progress Bar -->
      <div v-if="progress.nextTier" class="progress-section">
        <div class="progress-labels">
          <span>Tiến độ: {{ progress.progressPercentage }}%</span>
          <span class="next-tier-label" :style="{ color: getTierColor(progress.nextTier) }">
            {{ progress.nextTierDisplay }}
          </span>
        </div>
        
        <div class="progress-bar-container">
          <div 
            class="progress-bar-fill"
            :style="{ 
              width: `${Math.min(progress.progressPercentage, 100)}%`,
              backgroundColor: getProgressColor(progress.progressPercentage)
            }"
          >
            <span v-if="progress.progressPercentage > 15" class="progress-text">
              {{ progress.progressPercentage }}%
            </span>
          </div>
        </div>

        <div class="progress-points-labels">
          <span>{{ progress.currentPoints?.toLocaleString() }} điểm</span>
          <span>{{ progress.nextTierMinPoints?.toLocaleString() }} điểm</span>
        </div>
      </div>

      <!-- Motivation Message -->
      <div class="motivation-message">
        <el-icon class="me-2"><StarFilled /></el-icon>
        {{ progress.motivationMessage }}
      </div>

      <!-- Benefits Section -->
      <div v-if="progress.nextTier" class="benefits-section">
        <div class="benefits-title">
          Lợi ích khi lên hạng {{ progress.nextTierDisplay }}:
        </div>
        <div class="benefits-content">
          {{ progress.tierBenefitMessage }}
        </div>
        
        <div v-if="progress.nextTierDiscountRate" class="discount-comparison">
          <div class="discount-item">
            <span class="discount-label">Giảm giá hiện tại:</span>
            <span class="discount-value current">
              {{ (progress.currentDiscountRate * 100).toFixed(0) }}%
            </span>
          </div>
          <div class="discount-arrow">→</div>
          <div class="discount-item">
            <span class="discount-label">Giảm giá mới:</span>
            <span 
              class="discount-value next"
              :style="{ color: getTierColor(progress.nextTier) }"
            >
              {{ (progress.nextTierDiscountRate * 100).toFixed(0) }}% ↗
            </span>
          </div>
        </div>
      </div>

      <!-- Max Tier Achievement -->
      <div v-if="!progress.nextTier" class="max-tier-section">
        <div class="trophy-icon">🏆</div>
        <h3 class="max-tier-title">Chúc mừng!</h3>
        <p class="max-tier-message">
          Bạn đã đạt hạng cao nhất với mức ưu đãi tốt nhất
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { StarFilled } from '@element-plus/icons-vue';
import http from '../api/http';

const progress = ref(null);

const tierColors = {
  BRONZE: '#cd7f32',
  SILVER: '#c0c0c0',
  GOLD: '#ffd700',
  PLATINUM: '#e5e4e2',
  DIAMOND: '#b9f2ff'
};

function getTierColor(tier) {
  return tierColors[tier] || '#6b7280';
}

function getProgressColor(percentage) {
  if (percentage >= 80) return '#10b981'; // Green
  if (percentage >= 50) return '#3b82f6'; // Blue
  if (percentage >= 25) return '#f59e0b'; // Orange
  return '#6b7280'; // Gray
}

function formatMoney(amount) {
  if (!amount) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount);
}

async function loadTierProgress() {
  try {
    const response = await http.get('/api/auth/tier-progress/me');
    progress.value = response.data;
  } catch (error) {
    console.error('Load tier progress error:', error);
  }
}

onMounted(() => {
  loadTierProgress();
});

// Expose refresh method
defineExpose({
  refresh: loadTierProgress
});
</script>

<style scoped>
.tier-progress-container {
  margin-bottom: 20px;
}

/* VIP Upgrade Card */
.vip-upgrade-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  animation: slideIn 0.5s ease-out;
}

.vip-upgrade-card :deep(.el-card__body) {
  padding: 24px;
  color: white;
}

.upgrade-icon-large {
  font-size: 64px;
  line-height: 1;
  animation: bounce 2s infinite;
}

.upgrade-title {
  color: white;
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.upgrade-message {
  font-size: 16px;
  line-height: 1.6;
  margin: 0;
}

.highlight-points {
  font-weight: bold;
  font-size: 20px;
  color: #ffd700;
}

.highlight-vip {
  font-weight: bold;
  font-size: 18px;
  text-decoration: underline;
}

.upgrade-amount {
  font-size: 14px;
  opacity: 0.9;
}

.vip-benefits {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.benefit-item {
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

/* Close to Upgrade Card */
.close-upgrade-card {
  background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
  border: none;
  animation: pulse 2s infinite;
}

.close-upgrade-card :deep(.el-card__body) {
  padding: 20px;
  color: white;
}

.highlight-tier {
  font-weight: bold;
  font-size: 18px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

/* Main Progress Card */
.tier-progress-card {
  border-radius: 12px;
  overflow: hidden;
}

.tier-progress-card :deep(.el-card__body) {
  padding: 24px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.current-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tier-badge {
  padding: 8px 20px;
  border-radius: 20px;
  color: white;
  font-weight: bold;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.points-display {
  text-align: left;
}

.points-value {
  font-size: 32px;
  font-weight: bold;
  color: #3b82f6;
  line-height: 1;
}

.points-label {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.next-tier-info {
  text-align: right;
}

.next-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.next-tier-badge {
  font-size: 18px;
  font-weight: bold;
}

/* Progress Section */
.progress-section {
  margin: 20px 0;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 8px;
  color: #4b5563;
}

.next-tier-label {
  font-weight: bold;
}

.progress-bar-container {
  height: 24px;
  background: #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.progress-bar-fill {
  height: 100%;
  border-radius: 12px;
  transition: width 0.7s ease-out, background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
}

.progress-text {
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.progress-points-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6b7280;
  margin-top: 8px;
}

/* Motivation Message */
.motivation-message {
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  padding: 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1e40af;
  display: flex;
  align-items: center;
  margin: 20px 0;
}

/* Benefits Section */
.benefits-section {
  border-top: 1px solid #e5e7eb;
  padding-top: 20px;
  margin-top: 20px;
}

.benefits-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.benefits-content {
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

.discount-comparison {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.discount-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.discount-label {
  font-size: 12px;
  color: #6b7280;
}

.discount-value {
  font-size: 16px;
  font-weight: bold;
}

.discount-value.current {
  color: #4b5563;
}

.discount-value.next {
  font-size: 20px;
}

.discount-arrow {
  font-size: 24px;
  color: #10b981;
}

/* Max Tier Section */
.max-tier-section {
  text-align: center;
  padding: 40px 20px;
}

.trophy-icon {
  font-size: 80px;
  margin-bottom: 16px;
}

.max-tier-title {
  font-size: 28px;
  font-weight: bold;
  color: #f59e0b;
  margin-bottom: 12px;
}

.max-tier-message {
  font-size: 16px;
  color: #6b7280;
}

/* Animations */
@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .progress-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .next-tier-info {
    text-align: left;
  }
  
  .discount-comparison {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .discount-arrow {
    transform: rotate(90deg);
  }
  
  .points-value {
    font-size: 24px;
  }
  
  .upgrade-icon-large {
    font-size: 48px;
  }
}
</style>