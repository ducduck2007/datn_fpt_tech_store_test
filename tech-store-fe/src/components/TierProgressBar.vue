<!-- FILE: src/components/TierProgressBar.vue -->
<template>
  <div v-if="progress" class="tier-progress-container">

    <!-- Alert: Sắp lên VIP -->
    <div
      v-if="progress.willUpgradeToVip && progress.currentCustomerType === 'REGULAR'"
      class="compact-alert vip-alert mb-2"
    >
      👑 Còn <b>{{ progress.pointsNeededForVip?.toLocaleString() }} điểm</b>
      (≈ {{ formatMoney(progress.amountNeededForVip) }}) nữa là trở thành <b>VIP</b>!
      <span class="benefit-pills">
        <span>✨ Giảm giá cao hơn</span>
        <span>🎁 Ưu đãi độc quyền</span>
        <span>⚡ Tích điểm x2</span>
      </span>
    </div>

    <!-- Alert: Sắp lên hạng -->
    <div
      v-if="progress.isCloseToUpgrade && progress.nextTier"
      class="compact-alert fire-alert mb-2"
    >
      🔥 Còn <b>{{ progress.pointsGap?.toLocaleString() }} điểm</b>
      (≈ {{ formatMoney(progress.amountGapToNextTier) }}) nữa là lên hạng
      <b :style="{ color: getTierColor(progress.nextTier) }">{{ progress.nextTierDisplay }}</b>!
    </div>

    <!-- Main Card -->
    <div class="tier-card">

      <!-- Row 1: Tier badge + Điểm + Next tier -->
      <div class="tier-top">
        <div class="left-info">
          <div class="tier-badge" :style="{ backgroundColor: getTierColor(progress.currentTier) }">
            {{ progress.currentTierDisplay || 'Member' }}
          </div>
          <div class="points-block">
            <span class="points-val">{{ progress.currentPoints?.toLocaleString() }}</span>
            <span class="points-lbl">điểm tích lũy</span>
          </div>
        </div>

        <div v-if="progress.nextTier" class="right-info">
          <span class="next-lbl">Hạng tiếp theo:</span>
          <span class="next-val" :style="{ color: getTierColor(progress.nextTier) }">
            {{ progress.nextTierDisplay }}
          </span>
        </div>
        <div v-else class="max-badge">🏆 Hạng cao nhất</div>
      </div>

      <!-- Row 2: Progress bar -->
      <div v-if="progress.nextTier" class="progress-section">
        <div class="progress-labels-row">
          <span>Tiến độ: <b>{{ progress.progressPercentage }}%</b></span>
          <span :style="{ color: getTierColor(progress.nextTier), fontWeight: 600 }">
            {{ progress.nextTierDisplay }}
          </span>
        </div>
        <div class="progress-track">
          <div
            class="progress-fill"
            :style="{
              width: `${Math.min(progress.progressPercentage, 100)}%`,
              backgroundColor: getProgressColor(progress.progressPercentage)
            }"
          >
            <span v-if="progress.progressPercentage > 12" class="fill-text">
              {{ progress.progressPercentage }}%
            </span>
          </div>
        </div>
        <div class="progress-pts-row">
          <span>{{ progress.currentPoints?.toLocaleString() }} điểm</span>
          <span>{{ progress.nextTierMinPoints?.toLocaleString() }} điểm</span>
        </div>
      </div>

      <!-- Row 3: Đãi ngộ hiện tại -->
      <div class="current-benefits-row">
        <div class="cb-title">🎁 Đãi ngộ của bạn hiện tại:</div>
        <div class="cb-items">
          <div class="cb-item">
            <span class="cb-icon">💰</span>
            <span>Giảm giá: <b>{{ (progress.currentDiscountRate * 100).toFixed(0) }}%</b> trên mỗi đơn hàng</span>
          </div>
        
          <div v-if="progress.tierBenefitMessage && !progress.nextTier" class="cb-item">
            <span class="cb-icon">✨</span>
            <span>{{ progress.tierBenefitMessage }}</span>
          </div>
          <div class="cb-item">
            <span class="cb-icon">🏅</span>
            <span>Hạng: <b>{{ progress.currentTierDisplay }}</b></span>
          </div>
        </div>
      </div>

      <!-- Row 4: Motivation -->
      <div class="motivation-row">
        ⭐ {{ progress.motivationMessage }}
      </div>

      <!-- Row 5: Benefits + Discount khi lên hạng -->
      <div v-if="progress.nextTier" class="benefits-row">
        <div class="benefits-text">
          <b>Lợi ích khi lên {{ progress.nextTierDisplay }}:</b> {{ progress.tierBenefitMessage }}
        </div>
        <div v-if="progress.nextTierDiscountRate" class="discount-row">
          <span class="discount-cur">
            Giảm giá hiện tại: <b>{{ (progress.currentDiscountRate * 100).toFixed(0) }}%</b>
          </span>
          <span class="discount-arrow">→</span>
          <span class="discount-next" :style="{ color: getTierColor(progress.nextTier) }">
            Sau khi lên hạng: <b>{{ (progress.nextTierDiscountRate * 100).toFixed(0) }}% ↗</b>
          </span>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import http from '../api/http';

const progress = ref(null);

const tierColors = {
  BRONZE: '#cd7f32',
  SILVER: '#aaa',
  GOLD: '#f59e0b',
  PLATINUM: '#94a3b8',
  DIAMOND: '#38bdf8'
};

function getTierColor(tier) {
  return tierColors[tier] || '#6b7280';
}

function getProgressColor(pct) {
  if (pct >= 80) return '#10b981';
  if (pct >= 50) return '#3b82f6';
  if (pct >= 25) return '#f59e0b';
  return '#6b7280';
}

function formatMoney(amount) {
  if (!amount) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

async function loadTierProgress() {
  try {
    const res = await http.get('/api/auth/tier-progress/me');
    progress.value = res.data;
  } catch (e) {
    console.error('Load tier progress error:', e);
  }
}

onMounted(loadTierProgress);
defineExpose({ refresh: loadTierProgress });
</script>

<style scoped>
.tier-progress-container {
  width: 100%;
}

/* Alerts */
.compact-alert {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 13px;
}

.vip-alert {
  background: #f5f3ff;
  border: 1px solid #c4b5fd;
  color: #5b21b6;
}

.fire-alert {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #92400e;
}

.benefit-pills {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.benefit-pills span {
  background: rgba(139, 92, 246, 0.15);
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
}

/* Main Card */
.tier-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* Top row */
.tier-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.left-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tier-badge {
  padding: 4px 14px;
  border-radius: 20px;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  white-space: nowrap;
  box-shadow: 0 1px 4px rgba(0,0,0,0.2);
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.points-block {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.points-val {
  font-size: 22px;
  font-weight: 700;
  color: #3b82f6;
  line-height: 1;
}

.points-lbl {
  font-size: 12px;
  color: #6b7280;
}

.right-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.next-lbl {
  font-size: 11px;
  color: #9ca3af;
}

.next-val {
  font-size: 15px;
  font-weight: 700;
}

.max-badge {
  font-size: 14px;
  font-weight: 600;
  color: #f59e0b;
}

/* Progress */
.progress-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-labels-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #4b5563;
}

.progress-track {
  height: 10px;
  background: #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.6s ease-out;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 6px;
}

.fill-text {
  color: #fff;
  font-size: 10px;
  font-weight: 700;
}

.progress-pts-row {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #9ca3af;
}

/* Motivation */
.motivation-row {
  background: linear-gradient(135deg, #dbeafe, #e0e7ff);
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #1e40af;
}

/* Benefits */
.benefits-row {
  border-top: 1px solid #f3f4f6;
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.benefits-text {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
}

.discount-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  font-size: 13px;
}

.discount-cur { color: #6b7280; }
.discount-arrow { color: #10b981; font-weight: bold; font-size: 16px; }

/* Current benefits */
.current-benefits-row {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cb-title {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
}

.cb-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cb-item {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  padding: 3px 12px;
  font-size: 12px;
  color: #4b5563;
  white-space: nowrap;
}

.cb-icon {
  font-size: 13px;
}

@media (max-width: 480px) {
  .tier-top { flex-direction: column; align-items: flex-start; }
  .right-info { align-items: flex-start; }
  .discount-row { flex-direction: column; gap: 4px; }
}
</style>