<!-- FILE: src/components/TierProgressBar.vue -->
<template>
  <div v-if="progress" class="tier-wrap">

    <!-- ── Compact bar (always visible) ─────────────────── -->
    <div class="tier-bar" @click="expanded = !expanded" :class="{ 'bar-expanded': expanded }">

      <!-- Left: badge + points -->
      <div class="bar-left">
        <div class="tier-pill" :style="tierPillStyle(progress.currentTier)">
          <span class="pill-dot" :style="{ background: getTierColor(progress.currentTier) }"></span>
          {{ progress.currentTierDisplay || 'Member' }}
        </div>
        <span class="bar-points">
          <strong>{{ progress.currentPoints?.toLocaleString() }}</strong>
          <span class="bar-pts-label">pts</span>
        </span>
      </div>

      <!-- Center: inline progress bar -->
      <div class="bar-center" v-if="progress.nextTier">
        <div class="inline-track">
          <div
            class="inline-fill"
            :style="{
              width: `${Math.min(progress.progressPercentage, 100)}%`,
              background: getProgressGradient(progress.progressPercentage)
            }"
          ></div>
        </div>
        <span class="bar-next" :style="{ color: getTierColor(progress.nextTier) }">
          {{ progress.nextTierDisplay }}
        </span>
      </div>
      <div class="bar-center" v-else>
        <span class="bar-max">🏆 Hạng cao nhất</span>
      </div>

      <!-- Right: discount + toggle -->
      <div class="bar-right">
        <div class="bar-discount">
          <span class="discount-val">{{ (progress.currentDiscountRate * 100).toFixed(0) }}%</span>
          <span class="discount-lbl">giảm giá</span>
        </div>
        <div class="expand-btn" :class="{ rotated: expanded }">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
            <polyline points="6 9 12 15 18 9" stroke="currentColor" stroke-width="1.8"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- ── Expanded detail panel ─────────────────────────── -->
    <transition name="expand">
      <div v-if="expanded" class="tier-detail">

        <!-- Upgrade alerts -->
        <div
          v-if="progress.willUpgradeToVip && progress.currentCustomerType === 'REGULAR'"
          class="alert alert-vip"
        >
          <span>👑</span>
          <span>
            Còn <strong>{{ progress.pointsNeededForVip?.toLocaleString() }} điểm</strong>
            (≈ {{ formatMoney(progress.amountNeededForVip) }}) nữa là trở thành <strong>VIP</strong>!
          </span>
          <div class="alert-pills">
            <span>✨ Giảm giá cao hơn</span>
            <span>🎁 Ưu đãi độc quyền</span>
            <span>⚡ Tích điểm x2</span>
          </div>
        </div>

        <div v-if="progress.isCloseToUpgrade && progress.nextTier" class="alert alert-fire">
          <span>🔥</span>
          <span>
            Còn <strong>{{ progress.pointsGap?.toLocaleString() }} điểm</strong>
            (≈ {{ formatMoney(progress.amountGapToNextTier) }}) nữa là lên hạng
            <strong :style="{ color: getTierColor(progress.nextTier) }">{{ progress.nextTierDisplay }}</strong>!
          </span>
        </div>

        <!-- Progress detail -->
        <div v-if="progress.nextTier" class="detail-progress">
          <div class="detail-prog-meta">
            <span>{{ progress.currentPoints?.toLocaleString() }} / {{ progress.nextTierMinPoints?.toLocaleString() }} điểm</span>
            <span :style="{ color: getProgressColor(progress.progressPercentage), fontWeight: 600 }">
              {{ progress.progressPercentage }}%
            </span>
          </div>
          <div class="detail-track">
            <div
              class="detail-fill"
              :style="{
                width: `${Math.min(progress.progressPercentage, 100)}%`,
                background: getProgressGradient(progress.progressPercentage)
              }"
            ></div>
          </div>
        </div>

        <!-- Benefits grid -->
        <div class="benefits-grid">
          <div class="benefit-item">
            <div class="ben-icon" style="background:rgba(79,110,247,0.08);color:#4f6ef7">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                  stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="ben-text">
              <span class="ben-label">Hạng hiện tại</span>
              <span class="ben-val">{{ progress.currentTierDisplay }}</span>
            </div>
          </div>

          <div class="benefit-item">
            <div class="ben-icon" style="background:rgba(16,185,129,0.08);color:#10b981">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"
                  stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="ben-text">
              <span class="ben-label">Giảm giá mỗi đơn</span>
              <span class="ben-val">{{ (progress.currentDiscountRate * 100).toFixed(0) }}%</span>
            </div>
          </div>

          <div v-if="progress.nextTier && progress.nextTierDiscountRate" class="benefit-item">
            <div class="ben-icon" style="background:rgba(245,158,11,0.08);color:#f59e0b">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="17 6 23 6 23 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="ben-text">
              <span class="ben-label">Sau khi lên hạng</span>
              <span class="ben-val" :style="{ color: getTierColor(progress.nextTier) }">
                {{ (progress.nextTierDiscountRate * 100).toFixed(0) }}% ↗
              </span>
            </div>
          </div>
        </div>

        <!-- Motivation -->
        <div class="motivation">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" style="flex-shrink:0;color:#4f6ef7;margin-top:1px">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
            <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          {{ progress.motivationMessage }}
        </div>

        <!-- Next tier benefits -->
        <div v-if="progress.nextTier && progress.tierBenefitMessage" class="next-benefits">
          <span class="next-ben-label">Lợi ích khi lên {{ progress.nextTierDisplay }}:&nbsp;</span>
          <span class="next-ben-text">{{ progress.tierBenefitMessage }}</span>
        </div>

      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import http from '../api/http';

const progress = ref(null);
const expanded = ref(false);

const tierColors = {
  BRONZE:   '#cd7f32',
  SILVER:   '#8b9eb5',
  GOLD:     '#f59e0b',
  PLATINUM: '#64748b',
  DIAMOND:  '#38bdf8',
};

function getTierColor(tier) {
  return tierColors[tier] || '#6b7280';
}

function tierPillStyle(tier) {
  const c = getTierColor(tier);
  return { background: `${c}14`, border: `1px solid ${c}30`, color: c };
}

function getProgressColor(pct) {
  if (pct >= 80) return '#10b981';
  if (pct >= 50) return '#4f6ef7';
  if (pct >= 25) return '#f59e0b';
  return '#94a3b8';
}

function getProgressGradient(pct) {
  if (pct >= 80) return 'linear-gradient(90deg,#34d399,#10b981)';
  if (pct >= 50) return 'linear-gradient(90deg,#818cf8,#4f6ef7)';
  if (pct >= 25) return 'linear-gradient(90deg,#fcd34d,#f59e0b)';
  return 'linear-gradient(90deg,#cbd5e1,#94a3b8)';
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
.tier-wrap {
  width: 100%;
  font-family: 'DM Sans', sans-serif;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(0,0,0,0.07);
  box-shadow: 0 2px 10px rgba(0,0,0,0.04);
}

/* ── Compact bar ───────────────────────────────────────── */
.tier-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 16px;
  background: rgba(255,255,255,0.92);
  cursor: pointer;
  user-select: none;
  transition: background 0.15s;
  border-radius: 14px;
}

.tier-bar:hover { background: rgba(255,255,255,1); }
.bar-expanded { border-radius: 14px 14px 0 0; border-bottom: 1px solid rgba(0,0,0,0.06); }

.bar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.tier-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 11px;
  border-radius: 100px;
  font-size: 11.5px;
  font-weight: 600;
  white-space: nowrap;
}

.pill-dot { width: 5px; height: 5px; border-radius: 50%; flex-shrink: 0; }

.bar-points { display: flex; align-items: baseline; gap: 3px; white-space: nowrap; }
.bar-points strong { font-size: 15px; font-weight: 700; color: #4f6ef7; letter-spacing: -0.3px; }
.bar-pts-label { font-size: 11px; color: rgba(20,25,60,0.38); }

.bar-center { flex: 1; display: flex; align-items: center; gap: 10px; min-width: 0; }

.inline-track {
  flex: 1;
  height: 5px;
  background: rgba(0,0,0,0.07);
  border-radius: 100px;
  overflow: hidden;
  min-width: 40px;
}

.inline-fill {
  height: 100%;
  border-radius: 100px;
  transition: width 0.7s cubic-bezier(0.34,1.2,0.64,1);
}

.bar-next { font-size: 12px; font-weight: 700; white-space: nowrap; flex-shrink: 0; }
.bar-max  { font-size: 12.5px; font-weight: 600; color: #f59e0b; }

.bar-right { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }

.bar-discount { display: flex; flex-direction: column; align-items: flex-end; line-height: 1.1; }
.discount-val { font-size: 14px; font-weight: 700; color: #10b981; }
.discount-lbl { font-size: 10px; color: rgba(20,25,60,0.35); letter-spacing: 0.03em; text-transform: uppercase; }

.expand-btn {
  width: 24px; height: 24px;
  border-radius: 6px;
  background: rgba(0,0,0,0.04);
  display: flex; align-items: center; justify-content: center;
  color: rgba(20,25,60,0.35);
  transition: transform 0.25s ease, background 0.15s;
  flex-shrink: 0;
}
.expand-btn.rotated { transform: rotate(180deg); }
.tier-bar:hover .expand-btn { background: rgba(0,0,0,0.07); }

/* ── Detail panel ──────────────────────────────────────── */
.tier-detail {
  background: rgba(248,249,253,0.95);
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-radius: 0 0 14px 14px;
}

.expand-enter-active { transition: all 0.28s cubic-bezier(0.34,1.2,0.64,1); overflow: hidden; }
.expand-leave-active { transition: all 0.2s ease; overflow: hidden; }
.expand-enter-from, .expand-leave-to { opacity: 0; max-height: 0; padding-top: 0; padding-bottom: 0; }
.expand-enter-to, .expand-leave-from { opacity: 1; max-height: 600px; }

/* Alerts */
.alert {
  display: flex; align-items: flex-start; flex-wrap: wrap;
  gap: 7px; padding: 9px 13px; border-radius: 10px;
  font-size: 12.5px; line-height: 1.55;
}
.alert-vip  { background: rgba(139,92,246,0.06); border: 1px solid rgba(139,92,246,0.18); color: #5b21b6; }
.alert-fire { background: rgba(245,158,11,0.06);  border: 1px solid rgba(245,158,11,0.2);  color: #92400e; }

.alert-pills { display: flex; gap: 5px; flex-wrap: wrap; width: 100%; }
.alert-pills span {
  background: rgba(139,92,246,0.1); border: 1px solid rgba(139,92,246,0.14);
  padding: 1px 9px; border-radius: 100px; font-size: 11px; font-weight: 500;
}

/* Progress detail */
.detail-progress { display: flex; flex-direction: column; gap: 5px; }
.detail-prog-meta { display: flex; justify-content: space-between; font-size: 11.5px; color: rgba(20,25,60,0.4); }
.detail-track { height: 7px; background: rgba(0,0,0,0.07); border-radius: 100px; overflow: hidden; }
.detail-fill  { height: 100%; border-radius: 100px; transition: width 0.7s cubic-bezier(0.34,1.2,0.64,1); }

/* Benefits */
.benefits-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.benefit-item {
  display: flex; align-items: center; gap: 9px;
  background: rgba(255,255,255,0.85); border: 1px solid rgba(0,0,0,0.06);
  border-radius: 10px; padding: 8px 12px; flex: 1; min-width: 130px;
}
.ben-icon  { width: 28px; height: 28px; border-radius: 7px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.ben-text  { display: flex; flex-direction: column; gap: 1px; }
.ben-label { font-size: 10px; color: rgba(20,25,60,0.35); text-transform: uppercase; letter-spacing: 0.04em; }
.ben-val   { font-size: 13px; font-weight: 600; color: #1a1d2e; }

/* Motivation */
.motivation {
  display: flex; align-items: flex-start; gap: 7px;
  background: rgba(79,110,247,0.05); border: 1px solid rgba(79,110,247,0.11);
  border-radius: 9px; padding: 9px 12px; font-size: 12.5px; color: #2d3a8c; line-height: 1.5;
}

/* Next benefits */
.next-benefits {
  display: flex; flex-wrap: wrap; gap: 4px; font-size: 12px;
  padding: 9px 12px; background: rgba(0,0,0,0.025);
  border-radius: 9px; border: 1px solid rgba(0,0,0,0.05); line-height: 1.55;
}
.next-ben-label { font-weight: 600; color: rgba(20,25,60,0.65); }
.next-ben-text  { color: rgba(20,25,60,0.45); }

/* Responsive */
@media (max-width: 540px) {
  .bar-center { display: none; }
  .benefits-grid { flex-direction: column; }
  .benefit-item { min-width: unset; }
}
</style>