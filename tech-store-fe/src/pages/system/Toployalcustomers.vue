<template>
  <div class="ll-page">

    <!-- ── Header ── -->
    <header class="ll-header">
      <div class="ll-header__left">
        <div class="ll-eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
            <path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/>
            <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/>
            <path d="M18 2H6v7a6 6 0 0 0 12 0V2z"/>
          </svg>
          Loyalty Analytics
        </div>
        <h1 class="ll-title">Top Khách Hàng <span class="ll-title__accent">Trung Thành</span></h1>
        <p class="ll-subtitle">Đánh giá tổng hợp: điểm tích lũy · hạng VIP · chi tiêu · tần suất hoạt động</p>
      </div>

      <div class="ll-header__right">
        <div class="ll-topn-select">
          <span class="ll-topn-label">Hiển thị</span>
          <select v-model="topN" class="ll-select" @change="loadCustomers">
            <option :value="5">Top 5</option>
            <option :value="10">Top 10</option>
            <option :value="20">Top 20</option>
            <option :value="50">Top 50</option>
          </select>
        </div>
        <button class="ll-btn ll-btn--outline" :class="{'ll-btn--loading': loading}" @click="loadData" :disabled="loading">
          <svg v-if="!loading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
            <path d="M21 3v5h-5"/>
            <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
            <path d="M8 16H3v5"/>
          </svg>
          <span v-if="!loading">Reload</span>
          <span v-else class="ll-spinner"></span>
        </button>
      </div>
    </header>

    <!-- ── Stat cards ── -->
    <div class="ll-stats4">
      <div class="ll-stat ll-stat--purple">
        <div class="ll-stat__icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
            <path d="M4 22h16"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2z"/>
          </svg>
        </div>
        <div class="ll-stat__body">
          <div class="ll-stat__label">Tổng điểm (Top {{ topN }})</div>
          <div class="ll-stat__value">{{ totalLoyaltyPoints.toLocaleString() }}</div>
          <div class="ll-stat__source">Σ loyaltyPoints</div>
        </div>
      </div>
      <div class="ll-stat ll-stat--orange">
        <div class="ll-stat__icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
          </svg>
        </div>
        <div class="ll-stat__body">
          <div class="ll-stat__label">Khách VIP</div>
          <div class="ll-stat__value">{{ vipCount }}</div>
          <div class="ll-stat__source">customerType = VIP</div>
        </div>
      </div>
      <div class="ll-stat ll-stat--green">
        <div class="ll-stat__icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/>
            <line x1="6" y1="20" x2="6" y2="14"/>
          </svg>
        </div>
        <div class="ll-stat__body">
          <div class="ll-stat__label">TB điểm / KH</div>
          <div class="ll-stat__value">{{ avgPoints.toLocaleString() }}</div>
          <div class="ll-stat__source">avg loyaltyPoints</div>
        </div>
      </div>
      <div class="ll-stat ll-stat--blue">
        <div class="ll-stat__icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="1" x2="12" y2="23"/>
            <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
          </svg>
        </div>
        <div class="ll-stat__body">
          <div class="ll-stat__label">#1 Trung Thành</div>
          <div class="ll-stat__value ll-stat__value--sm">{{ sortedCustomers[0]?.fullName || '—' }}</div>
          <div class="ll-stat__source">{{ sortedCustomers[0] ? (sortedCustomers[0].loyaltyPoints || 0).toLocaleString() + ' pts' : '' }}</div>
        </div>
      </div>
    </div>

    <!-- ── Tier Breakdown ── -->
    <div class="ll-card" v-if="Object.keys(stats.byTier || {}).length">
      <div class="ll-card__head">
        <span class="ll-card__title">Phân bổ chi tiêu theo hạng VIP</span>
      </div>
      <div class="ll-card__body">
        <div class="ll-tier-grid">
          <div
            v-for="(td, tier) in stats.byTier"
            :key="tier"
            class="ll-tier-card"
            :class="'ll-tier-card--' + tier.toLowerCase()"
          >
            <div class="ll-tier-card__head">
              <span class="ll-tier-tag" :class="'ll-tier--' + tier.toLowerCase()">
                {{ TIER_MAP[tier]?.icon }} {{ tier }}
              </span>
              <span class="ll-tier-card__count ll-text--muted">{{ td.customerCount }} KH</span>
            </div>
            <div class="ll-tier-card__spent">{{ fmtShort(td.totalSpent) }}</div>
            <div class="ll-tier-card__avg ll-text--muted">TB: {{ fmtShort(td.averageSpent) }}</div>
            <div class="ll-bar-wrap">
              <div
                class="ll-bar"
                :class="'ll-bar--' + tier.toLowerCase()"
                :style="{ width: stats.totalSpent ? Math.round((td.totalSpent / stats.totalSpent) * 100) + '%' : '0%' }"
              ></div>
            </div>
            <span class="ll-tier-card__pct ll-mono ll-text--muted">
              {{ stats.totalSpent ? Math.round((td.totalSpent / stats.totalSpent) * 100) : 0 }}%
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Controls: Tier filter + Sort ── -->
    <div class="ll-card">
      <div class="ll-controls">
        <div class="ll-tier-tabs">
          <button
            v-for="t in TIER_OPTIONS"
            :key="t.value"
            class="ll-tier-tab"
            :class="{'ll-tier-tab--active': activeTier === t.value}"
            @click="onTierChange(t.value)"
          >
            {{ t.icon }} {{ t.label }}
          </button>
        </div>
        <div class="ll-sort-group">
          <span class="ll-sort-label ll-text--muted">Sắp xếp:</span>
          <div class="ll-sort-tabs">
            <button class="ll-sort-btn" :class="{'ll-sort-btn--active': sortKey==='loyaltyPoints'}" @click="sortKey='loyaltyPoints'">Điểm</button>
            <button class="ll-sort-btn" :class="{'ll-sort-btn--active': sortKey==='totalSpent'}" @click="sortKey='totalSpent'">Chi tiêu</button>
            <button class="ll-sort-btn" :class="{'ll-sort-btn--active': sortKey==='orderCount'}" @click="sortKey='orderCount'">Số lần mua</button>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="ll-table-wrap" :class="{'ll-table-wrap--loading': loading}">
        <div v-if="loading" class="ll-loader-overlay"><div class="ll-loader-ring"></div></div>

        <table class="ll-table">
          <thead>
            <tr>
              <th style="width:70px;text-align:center">Rank</th>
              <th style="min-width:200px">Khách hàng</th>
              <th style="width:130px;text-align:center">Hạng VIP</th>
              <th style="width:160px;text-align:center">Điểm tích lũy</th>
              <th style="width:130px;text-align:center">Số lần mua</th>
              <th style="width:150px;text-align:center">Còn thiếu</th>
              <th style="width:200px;text-align:right">Tổng chi tiêu</th>
              <th style="width:90px;text-align:center">Ưu đãi</th>
              <th style="width:160px;text-align:center">Hoạt động</th>
              <th style="min-width:150px">⭐ VIP Note</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && sortedCustomers.length === 0">
              <td colspan="10" class="ll-empty">
                <div class="ll-empty__inner">
                  <div class="ll-empty__icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
                      <path d="M18 2H6v7a6 6 0 0 0 12 0V2z"/>
                    </svg>
                  </div>
                  <p>Không có dữ liệu</p>
                </div>
              </td>
            </tr>
            <tr
              v-for="(row, idx) in sortedCustomers"
              :key="row.id"
              class="ll-row"
              :class="{
                'll-row--top1': idx === 0,
                'll-row--top2': idx === 1,
                'll-row--top3': idx === 2
              }"
            >
              <!-- Rank -->
              <td style="text-align:center">
                <div class="ll-rank" :class="getRankClass(idx)">
                  <span v-if="idx === 0" style="font-size:18px">🥇</span>
                  <span v-else-if="idx === 1" style="font-size:18px">🥈</span>
                  <span v-else-if="idx === 2" style="font-size:18px">🥉</span>
                  <span v-else class="ll-rank__num">{{ idx + 1 }}</span>
                </div>
              </td>

              <!-- Customer -->
              <td>
                <div class="ll-customer-cell">
                  <div class="ll-avatar" :style="{ background: getAvatarColor(row.fullName) }">{{ getInitials(row.fullName) }}</div>
                  <div class="ll-cust-info">
                    <span class="ll-cust-name">{{ row.fullName }}</span>
                    <span class="ll-cust-email ll-text--muted">{{ row.email }}</span>
                  </div>
                </div>
              </td>

              <!-- Tier -->
              <td style="text-align:center">
                <div class="ll-tier-cell">
                  <span v-if="row.raw.vipTier" class="ll-tier-tag" :class="'ll-tier--' + row.raw.vipTier.toLowerCase()">
                    {{ TIER_MAP[row.raw.vipTier]?.icon }} {{ row.raw.vipTier }}
                  </span>
                  <span v-else class="ll-tag ll-tag--gray" style="font-size:11px">Member</span>
                  <span class="ll-text--muted" style="font-size:10px;margin-top:2px">{{ row.customerType }}</span>
                </div>
              </td>

              <!-- Points -->
              <td style="text-align:center">
                <div class="ll-points-cell">
                  <span class="ll-points-val ll-mono">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="margin-right:3px"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
                    {{ (row.loyaltyPoints || 0).toLocaleString() }}
                  </span>
                  <div v-if="row.raw.vipTier" class="ll-pts-bar-wrap">
                    <div class="ll-pts-bar" :class="'ll-bar--' + row.raw.vipTier.toLowerCase()" :style="{ width: getTierProgress(row.raw) + '%' }"></div>
                  </div>
                </div>
              </td>

              <!-- Order count -->
              <td style="text-align:center">
                <div class="ll-oc-cell">
                  <div class="ll-oc-num">{{ (row.raw.orderCount || 0).toLocaleString() }}</div>
                  <div class="ll-text--muted" style="font-size:10px">đơn hàng</div>
                  <span class="ll-tag ll-tag--sm" :class="getOrderFreqTagClass(row.raw.orderCount)" style="margin-top:3px">
                    {{ getOrderFreqLabel(row.raw.orderCount) }}
                  </span>
                </div>
              </td>

              <!-- Next tier -->
              <td style="text-align:center">
                <div v-if="row.raw.pointsToNextTier > 0" class="ll-next-cell">
                  <div class="ll-next-pts ll-mono">{{ row.raw.pointsToNextTier.toLocaleString() }} pts</div>
                  <div class="ll-text--muted" style="font-size:10px">để lên hạng tiếp</div>
                </div>
                <span v-else class="ll-tag ll-tag--green" style="font-size:11px">✓ Max tier</span>
              </td>

              <!-- Total spent -->
              <td style="text-align:right">
                <div class="ll-spent-cell">
                  <div class="ll-spent-val">{{ formatCurrency(row.raw.totalSpent) }}</div>
                  <div class="ll-spent-bar-wrap">
                    <div
                      class="ll-spent-bar"
                      :class="row.raw.vipTier ? 'll-bar--' + row.raw.vipTier.toLowerCase() : 'll-bar--blue'"
                      :style="{ width: maxSpent ? Math.round((row.raw.totalSpent / maxSpent) * 100) + '%' : '0%' }"
                    ></div>
                  </div>
                </div>
              </td>

              <!-- Discount rate -->
              <td style="text-align:center">
                <span v-if="row.raw.discountRate" class="ll-tag ll-tag--green">
                  -{{ (row.raw.discountRate * 100).toFixed(0) }}%
                </span>
                <span v-else class="ll-text--muted">—</span>
              </td>

              <!-- Activity -->
              <td style="text-align:center">
                <div class="ll-activity-cell">
                  <div class="ll-act-row">
                    <span>📦</span>
                    <span class="ll-act-txt" :class="getActivityClass(row.raw.lastOrderAt)">
                      {{ fmtRelative(row.raw.lastOrderAt) }}
                    </span>
                  </div>
                  <div class="ll-act-row">
                    <span>🔐</span>
                    <span class="ll-act-txt ll-text--muted">{{ fmtRelative(row.raw.lastLoginAt) }}</span>
                  </div>
                </div>
              </td>

              <!-- VIP Note -->
              <td>
                <template v-if="row.raw.vipTier">
                  <div v-if="row.raw.vipNote" class="ll-vipnote-chip" :title="row.raw.vipNote">
                    <span>⭐</span>
                    <span class="ll-vipnote-txt">{{ row.raw.vipNote.substring(0, 22) }}{{ row.raw.vipNote.length > 22 ? '…' : '' }}</span>
                  </div>
                  <span v-else class="ll-text--muted" style="font-size:12px;font-style:italic">Chưa có note</span>
                </template>
                <span v-else class="ll-text--muted">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Footer info -->
      <div class="ll-footer-alert">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" style="flex-shrink:0">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <span>
          <strong>Khác với "By Spending Range":</strong>
          Trang này đo <em>độ trung thành tổng hợp</em> —
          KH mua 1 lần 200M chưa chắc trung thành hơn KH mua 40 lần tổng 80M.
          Metric chính là <code>loyaltyPoints</code> (tần suất) +
          <code>vipTier</code> (hạng) + <code>lastOrderAt</code> (còn active?).
        </span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";

// ══════════════════════════════════════
// CONSTANTS
// ══════════════════════════════════════
const TIER_MAP = {
  DIAMOND:  { icon: "💎", color: "#22c55e", minPts: 15000, maxPts: 20000 },
  PLATINUM: { icon: "💠", color: "#409EFF", minPts: 10000, maxPts: 15000 },
  GOLD:     { icon: "🥇", color: "#E6A23C", minPts: 5000,  maxPts: 10000 },
  SILVER:   { icon: "🥈", color: "#909399", minPts: 2000,  maxPts: 5000  },
  BRONZE:   { icon: "🥉", color: "#cd7f32", minPts: 500,   maxPts: 2000  },
};

const TIER_OPTIONS = [
  { value: "ALL",      icon: "🏆", label: "Tất cả"  },
  { value: "DIAMOND",  icon: "💎", label: "Diamond" },
  { value: "PLATINUM", icon: "💠", label: "Platinum"},
  { value: "GOLD",     icon: "🥇", label: "Gold"    },
  { value: "SILVER",   icon: "🥈", label: "Silver"  },
  { value: "BRONZE",   icon: "🥉", label: "Bronze"  },
];

// ══════════════════════════════════════
// STATE
// ══════════════════════════════════════
const loading    = ref(false);
const customers  = ref([]);
const activeTier = ref("ALL");
const topN       = ref(10);
const sortKey    = ref("loyaltyPoints");

const stats = ref({
  totalSpent: 0,
  totalCustomers: 0,
  averageSpent: 0,
  byTier: {},
  spendingRanges: {},
});

// ══════════════════════════════════════
// COMPUTED
// ══════════════════════════════════════
const sortedCustomers = computed(() =>
  [...customers.value].sort((a, b) => {
    let va, vb;
    if (sortKey.value === "loyaltyPoints") {
      va = a.loyaltyPoints || 0;
      vb = b.loyaltyPoints || 0;
    } else if (sortKey.value === "totalSpent") {
      va = a.raw.totalSpent || 0;
      vb = b.raw.totalSpent || 0;
    } else {
      va = a.raw.orderCount || 0;
      vb = b.raw.orderCount || 0;
    }
    return vb - va;
  })
);

const maxSpent = computed(() =>
  Math.max(...customers.value.map(c => c.raw.totalSpent || 0), 1)
);

const totalLoyaltyPoints = computed(() =>
  customers.value.reduce((s, c) => s + (c.loyaltyPoints || 0), 0)
);

const vipCount = computed(() =>
  customers.value.filter(c => c.customerType === "VIP").length
);

const avgPoints = computed(() => {
  if (!customers.value.length) return 0;
  return Math.round(totalLoyaltyPoints.value / customers.value.length);
});

// ══════════════════════════════════════
// API
// ══════════════════════════════════════
async function loadCustomers() {
  loading.value = true;
  try {
    const res = activeTier.value === "ALL"
      ? await customersApi.listTopSpenders(topN.value)
      : await customersApi.listTopSpendersByVipTier(activeTier.value, topN.value);
    customers.value = normalize(extractList(res?.data));
  } catch (e) {
    console.error(e);
    toast("Không thể tải danh sách loyal customers", "error");
  } finally {
    loading.value = false;
  }
}

async function loadStats() {
  try {
    const res = await customersApi.getSpendingStatistics();
    stats.value = res?.data || {};
  } catch (e) {
    console.error(e);
  }
}

async function loadData() {
  await Promise.all([loadCustomers(), loadStats()]);
}

function onTierChange(tier) {
  activeTier.value = tier;
  loadCustomers();
}

// ══════════════════════════════════════
// HELPERS
// ══════════════════════════════════════
function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  const root = payload?.data ?? payload;
  if (Array.isArray(root)) return root;
  for (const k of ["content", "items", "results", "rows", "list"]) {
    if (Array.isArray(root?.[k])) return root[k];
    if (Array.isArray(root?.data?.[k])) return root.data[k];
  }
  return [];
}

function normalize(list) {
  return (list || []).map(c => ({
    id:            c?.id ?? c?.customerId,
    fullName:      c?.fullName ?? c?.name ?? "",
    email:         c?.email ?? "",
    phone:         c?.phone ?? "",
    customerType:  (c?.customerType ?? "REGULAR").toString().toUpperCase(),
    loyaltyPoints: c?.loyaltyPoints ?? 0,
    raw: c,
  }));
}

function formatCurrency(v) {
  if (!v) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(v);
}

function fmtShort(v) {
  if (!v || v === 0) return "—";
  if (v >= 1_000_000_000) return (v / 1_000_000_000).toFixed(1) + " tỷ";
  if (v >= 1_000_000)     return (v / 1_000_000).toFixed(1) + "M";
  if (v >= 1_000)         return (v / 1_000).toFixed(0) + "K";
  return v + " ₫";
}

function fmtRelative(s) {
  if (!s) return "—";
  const diff = (Date.now() - new Date(s)) / 86_400_000;
  if (diff < 1)  return "Hôm nay";
  if (diff < 2)  return "Hôm qua";
  if (diff < 7)  return `${Math.floor(diff)} ngày trước`;
  if (diff < 30) return `${Math.floor(diff / 7)} tuần trước`;
  return `${Math.floor(diff / 30)} tháng trước`;
}

function getActivityClass(lastOrderAt) {
  if (!lastOrderAt) return "ll-act--inactive";
  const diff = (Date.now() - new Date(lastOrderAt)) / 86_400_000;
  if (diff < 7)  return "ll-act--hot";
  if (diff < 30) return "ll-act--warm";
  if (diff < 90) return "ll-act--cool";
  return "ll-act--inactive";
}

function getOrderFreqTagClass(count) {
  if (!count || count === 0) return "ll-tag--gray";
  if (count >= 20) return "ll-tag--red";
  if (count >= 10) return "ll-tag--orange";
  if (count >= 5)  return "ll-tag--green";
  return "ll-tag--blue";
}

function getOrderFreqLabel(count) {
  if (!count || count === 0) return "Chưa mua";
  if (count >= 20) return "Rất thường xuyên";
  if (count >= 10) return "Thường xuyên";
  if (count >= 5)  return "Khá đều";
  return "Thỉnh thoảng";
}

function getTierProgress(customer) {
  const tier = TIER_MAP[customer.vipTier];
  if (!tier) return 0;
  const range = tier.maxPts - tier.minPts;
  const current = (customer.loyaltyPoints || 0) - tier.minPts;
  return Math.min(100, Math.max(0, Math.round((current / range) * 100)));
}

function getInitials(name) {
  if (!name) return "?";
  const p = name.trim().split(" ");
  return p.length === 1 ? p[0].substring(0, 2).toUpperCase() : (p[0][0] + p[p.length - 1][0]).toUpperCase();
}

function getAvatarColor(name) {
  const colors = ["#1e40af","#065f46","#7c2d12","#581c87","#0e7490","#a21caf","#b45309","#0f766e","#1d4ed8","#be123c"];
  const h = (name || "").split("").reduce((a, c) => a + c.charCodeAt(0), 0);
  return colors[h % colors.length];
}

function getRankClass(i) {
  return i === 0 ? "ll-rank--gold" : i === 1 ? "ll-rank--silver" : i === 2 ? "ll-rank--bronze" : "";
}

onMounted(loadData);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.ll-page {
  --c-bg:            #f6f7f9;
  --c-card:          #ffffff;
  --c-border:        #e4e7ec;
  --c-border-light:  #f0f2f5;
  --c-text:          #0f1117;
  --c-muted:         #6b7280;
  --c-subtle:        #9ca3af;
  --c-blue:          #2563eb;
  --c-blue-bg:       #eff6ff;
  --c-blue-border:   #bfdbfe;
  --c-green:         #16a34a;
  --c-green-bg:      #f0fdf4;
  --c-green-border:  #bbf7d0;
  --c-red:           #dc2626;
  --c-red-bg:        #fff1f2;
  --c-red-border:    #fecdd3;
  --c-orange:        #d97706;
  --c-orange-bg:     #fffbeb;
  --c-orange-border: #fde68a;
  --c-purple:        #7c3aed;
  --c-purple-bg:     #f5f3ff;
  --c-purple-border: #ddd6fe;
  --c-gray-bg:       #f3f4f6;
  --c-gray-border:   #e5e7eb;
  --radius:          12px;
  --radius-sm:       8px;
  --shadow-sm:       0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-md:       0 4px 16px rgba(0,0,0,0.08), 0 2px 6px rgba(0,0,0,0.04);

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--c-bg);
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: var(--c-text);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

/* ── Header ──────────────────────────────── */
.ll-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; flex-wrap: wrap; }
.ll-eyebrow { display: inline-flex; align-items: center; gap: 5px; font-size: 11px; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; color: var(--c-orange); margin-bottom: 6px; }
.ll-title { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; margin: 0 0 4px; line-height: 1.2; }
.ll-title__accent { color: var(--c-blue); }
.ll-subtitle { font-size: 13.5px; color: var(--c-muted); margin: 0; }
.ll-header__right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; padding-top: 6px; }
.ll-topn-select { display: flex; align-items: center; gap: 8px; }
.ll-topn-label { font-size: 12.5px; color: var(--c-muted); font-weight: 600; white-space: nowrap; }

/* ── Stat cards ──────────────────────────── */
.ll-stats4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
@media (max-width:900px) { .ll-stats4 { grid-template-columns: 1fr 1fr; } }

.ll-stat { display: flex; align-items: center; gap: 16px; padding: 18px; border-radius: var(--radius); color: #fff; transition: transform 0.2s; }
.ll-stat:hover { transform: translateY(-2px); }
.ll-stat--purple { background: linear-gradient(135deg, #5b21b6, #7c3aed 60%, #a78bfa); }
.ll-stat--orange { background: linear-gradient(135deg, #b45309, #d97706 60%, #fbbf24); }
.ll-stat--green  { background: linear-gradient(135deg, #14532d, #16a34a 60%, #4ade80); }
.ll-stat--blue   { background: linear-gradient(135deg, #1d4ed8, #2563eb 60%, #60a5fa); }
.ll-stat__icon { flex-shrink: 0; opacity: 0.85; }
.ll-stat__body { flex: 1; min-width: 0; }
.ll-stat__label { font-size: 12px; font-weight: 600; opacity: 0.88; margin-bottom: 4px; }
.ll-stat__value { font-size: 22px; font-weight: 800; letter-spacing: -0.02em; line-height: 1.1; }
.ll-stat__value--sm { font-size: 15px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ll-stat__source { font-size: 10px; opacity: 0.65; margin-top: 3px; font-family: "JetBrains Mono", monospace; }

/* ── Card ────────────────────────────────── */
.ll-card { background: var(--c-card); border: 1px solid var(--c-border); border-radius: var(--radius); box-shadow: var(--shadow-sm); overflow: hidden; }
.ll-card__head { display: flex; align-items: center; gap: 10px; padding: 14px 20px; border-bottom: 1px solid var(--c-border-light); background: #fafbfc; }
.ll-card__title { font-size: 13.5px; font-weight: 700; color: var(--c-text); }
.ll-card__body { padding: 20px; }

/* ── Tier breakdown ──────────────────────── */
.ll-tier-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
.ll-tier-card { border: 1px solid var(--c-border); border-radius: var(--radius-sm); padding: 14px; background: var(--c-card); transition: box-shadow 0.15s; }
.ll-tier-card:hover { box-shadow: var(--shadow-md); }
.ll-tier-card--diamond  { border-color: var(--c-green-border);  background: var(--c-green-bg); }
.ll-tier-card--platinum { border-color: var(--c-blue-border);   background: var(--c-blue-bg); }
.ll-tier-card--gold     { border-color: var(--c-orange-border); background: var(--c-orange-bg); }
.ll-tier-card--silver   { border-color: var(--c-gray-border);   background: var(--c-gray-bg); }
.ll-tier-card--bronze   { border-color: #fcd9a8; background: #fdf4eb; }
.ll-tier-card__head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.ll-tier-card__count { font-size: 11px; }
.ll-tier-card__spent { font-size: 18px; font-weight: 800; color: var(--c-text); margin-bottom: 2px; }
.ll-tier-card__avg { font-size: 11px; margin-bottom: 8px; }
.ll-tier-card__pct { font-size: 11px; display: block; margin-top: 4px; }

/* ── Controls ────────────────────────────── */
.ll-controls { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; border-bottom: 1px solid var(--c-border-light); background: #fafbfc; flex-wrap: wrap; gap: 12px; }

.ll-tier-tabs { display: flex; gap: 2px; flex-wrap: wrap; }
.ll-tier-tab { display: inline-flex; align-items: center; gap: 5px; padding: 6px 14px; font-size: 12.5px; font-weight: 600; border: 1.5px solid var(--c-border); border-radius: 20px; background: var(--c-card); color: var(--c-muted); cursor: pointer; transition: all 0.15s; font-family: "Plus Jakarta Sans", sans-serif; white-space: nowrap; }
.ll-tier-tab:hover { border-color: var(--c-blue); color: var(--c-blue); }
.ll-tier-tab--active { background: var(--c-blue); border-color: var(--c-blue); color: #fff; }

.ll-sort-group { display: flex; align-items: center; gap: 10px; }
.ll-sort-label { font-size: 12.5px; font-weight: 600; }
.ll-sort-tabs { display: flex; border: 1px solid var(--c-border); border-radius: var(--radius-sm); overflow: hidden; }
.ll-sort-btn { padding: 6px 14px; font-size: 12px; font-weight: 600; border: none; background: var(--c-card); color: var(--c-muted); cursor: pointer; font-family: "Plus Jakarta Sans", sans-serif; transition: all 0.12s; }
.ll-sort-btn:hover { background: var(--c-border-light); color: var(--c-text); }
.ll-sort-btn--active { background: var(--c-blue); color: #fff; }

/* ── Table ───────────────────────────────── */
.ll-table-wrap { overflow-x: auto; position: relative; min-height: 200px; }
.ll-table-wrap--loading { pointer-events: none; }
.ll-loader-overlay { position: absolute; inset: 0; background: rgba(255,255,255,0.75); backdrop-filter: blur(2px); display: flex; align-items: center; justify-content: center; z-index: 10; }
.ll-loader-ring { width: 36px; height: 36px; border: 3px solid var(--c-border); border-top-color: var(--c-blue); border-radius: 50%; animation: ll-spin 0.7s linear infinite; }

.ll-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.ll-table thead tr { border-bottom: 2px solid var(--c-border-light); }
.ll-table th { padding: 11px 14px; text-align: left; font-size: 10.5px; font-weight: 700; letter-spacing: 0.07em; text-transform: uppercase; color: var(--c-subtle); background: #fafbfc; white-space: nowrap; }
.ll-table td { padding: 12px 14px; vertical-align: middle; border-bottom: 1px solid var(--c-border-light); }
.ll-row { transition: background 0.12s; }
.ll-row:hover { background: #fafbfc; }
.ll-row:last-child td { border-bottom: none; }
.ll-row--top1 { background: linear-gradient(90deg, #fffef7, transparent 60%); }
.ll-row--top2 { background: linear-gradient(90deg, #f8f8ff, transparent 60%); }
.ll-row--top3 { background: linear-gradient(90deg, #fdf9f5, transparent 60%); }

/* Rank badge */
.ll-rank { display: inline-flex; align-items: center; justify-content: center; width: 36px; height: 36px; border-radius: 50%; font-size: 18px; font-weight: 800; }
.ll-rank--gold   { background: linear-gradient(135deg, #ffd700, #ffed4e); box-shadow: 0 3px 10px rgba(255,215,0,0.4); }
.ll-rank--silver { background: linear-gradient(135deg, #c0c0c0, #e8e8e8); box-shadow: 0 3px 10px rgba(192,192,192,0.4); }
.ll-rank--bronze { background: linear-gradient(135deg, #cd7f32, #e6a23c); box-shadow: 0 3px 10px rgba(205,127,50,0.4); }
.ll-rank__num { font-size: 13px; font-weight: 700; color: var(--c-subtle); }

/* Customer cell */
.ll-customer-cell { display: flex; align-items: center; gap: 10px; }
.ll-avatar { width: 36px; height: 36px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; color: rgba(255,255,255,0.9); font-family: "Plus Jakarta Sans", sans-serif; font-weight: 700; font-size: 12px; flex-shrink: 0; }
.ll-cust-info { display: flex; flex-direction: column; gap: 1px; }
.ll-cust-name { font-weight: 700; font-size: 13.5px; }
.ll-cust-email { font-size: 11px; }

/* Tier cell */
.ll-tier-cell { display: flex; flex-direction: column; align-items: center; gap: 4px; }

/* Points cell */
.ll-points-cell { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.ll-points-val { display: inline-flex; align-items: center; font-size: 13px; font-weight: 700; color: var(--c-orange); background: var(--c-orange-bg); border: 1px solid var(--c-orange-border); padding: 3px 9px; border-radius: 20px; }
.ll-pts-bar-wrap { width: 100px; height: 4px; background: var(--c-border); border-radius: 999px; overflow: hidden; }
.ll-pts-bar { height: 100%; border-radius: 999px; transition: width 0.4s ease; }

/* Order count cell */
.ll-oc-cell { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.ll-oc-num { font-size: 20px; font-weight: 800; letter-spacing: -0.02em; line-height: 1; }

/* Next tier cell */
.ll-next-cell { display: flex; flex-direction: column; align-items: center; gap: 3px; }
.ll-next-pts { font-size: 13px; font-weight: 800; color: var(--c-text); }

/* Spent cell */
.ll-spent-cell { display: flex; flex-direction: column; gap: 5px; }
.ll-spent-val { font-size: 13.5px; font-weight: 700; color: var(--c-green); white-space: nowrap; }
.ll-spent-bar-wrap { width: 130px; height: 4px; background: var(--c-border); border-radius: 999px; overflow: hidden; margin-left: auto; }
.ll-spent-bar { height: 100%; border-radius: 999px; transition: width 0.4s ease; }

/* Activity cell */
.ll-activity-cell { display: flex; flex-direction: column; gap: 5px; }
.ll-act-row { display: flex; align-items: center; gap: 5px; }
.ll-act-txt { font-size: 12px; }
.ll-act--hot      { color: var(--c-green); font-weight: 600; }
.ll-act--warm     { color: var(--c-orange); }
.ll-act--cool     { color: var(--c-subtle); }
.ll-act--inactive { color: var(--c-red); }

/* VIP note chip */
.ll-vipnote-chip { display: inline-flex; align-items: center; gap: 5px; background: var(--c-orange-bg); border: 1px solid var(--c-orange-border); padding: 4px 9px; border-radius: var(--radius-sm); cursor: pointer; max-width: 190px; transition: background 0.15s; }
.ll-vipnote-chip:hover { background: #fef3c7; }
.ll-vipnote-txt { font-size: 12px; color: var(--c-orange); overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }

/* Empty state */
.ll-empty { text-align: center; padding: 60px 20px !important; }
.ll-empty__inner { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.ll-empty__icon { width: 56px; height: 56px; border-radius: 50%; background: var(--c-border-light); display: flex; align-items: center; justify-content: center; color: var(--c-subtle); }
.ll-empty__inner p { color: var(--c-muted); font-size: 14px; margin: 0; }

/* Footer alert */
.ll-footer-alert { display: flex; align-items: flex-start; gap: 10px; padding: 13px 20px; border-top: 1px solid var(--c-border-light); background: var(--c-blue-bg); color: var(--c-blue); font-size: 12.5px; line-height: 1.6; }
.ll-footer-alert code { background: rgba(37,99,235,0.1); padding: 1px 5px; border-radius: 4px; font-family: "JetBrains Mono", monospace; font-size: 11.5px; }

/* Tier tags */
.ll-tier-tag { display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 800; letter-spacing: 0.04em; border: 1px solid transparent; white-space: nowrap; text-transform: uppercase; }
.ll-tier--diamond  { background: var(--c-green-bg);  color: var(--c-green);  border-color: var(--c-green-border); }
.ll-tier--platinum { background: var(--c-blue-bg);   color: var(--c-blue);   border-color: var(--c-blue-border); }
.ll-tier--gold     { background: var(--c-orange-bg); color: var(--c-orange); border-color: var(--c-orange-border); }
.ll-tier--silver   { background: var(--c-gray-bg);   color: var(--c-muted);  border-color: var(--c-gray-border); }
.ll-tier--bronze   { background: #fef3c7; color: #92400e; border-color: #fde68a; }

/* Status tags */
.ll-tag { display: inline-flex; align-items: center; padding: 3px 9px; border-radius: 20px; font-size: 11.5px; font-weight: 700; border: 1px solid transparent; white-space: nowrap; }
.ll-tag--sm { font-size: 10.5px; padding: 2px 7px; }
.ll-tag--green  { background: var(--c-green-bg);  color: var(--c-green);  border-color: var(--c-green-border); }
.ll-tag--red    { background: var(--c-red-bg);    color: var(--c-red);    border-color: var(--c-red-border); }
.ll-tag--orange { background: var(--c-orange-bg); color: var(--c-orange); border-color: var(--c-orange-border); }
.ll-tag--blue   { background: var(--c-blue-bg);   color: var(--c-blue);   border-color: var(--c-blue-border); }
.ll-tag--gray   { background: var(--c-gray-bg);   color: var(--c-muted);  border-color: var(--c-gray-border); }

/* Bars */
.ll-bar-wrap { height: 6px; background: var(--c-border); border-radius: 999px; overflow: hidden; }
.ll-bar { height: 100%; border-radius: 999px; transition: width 0.4s ease; min-width: 4px; }
.ll-bar--diamond  { background: var(--c-green); }
.ll-bar--platinum { background: var(--c-blue); }
.ll-bar--gold     { background: var(--c-orange); }
.ll-bar--silver   { background: var(--c-subtle); }
.ll-bar--bronze   { background: #cd7f32; }
.ll-bar--blue     { background: var(--c-blue); }

/* Buttons */
.ll-btn { display: inline-flex; align-items: center; gap: 7px; padding: 9px 16px; font-size: 13px; font-weight: 700; border-radius: var(--radius-sm); border: none; cursor: pointer; transition: all 0.18s ease; font-family: "Plus Jakarta Sans", sans-serif; white-space: nowrap; }
.ll-btn--outline { background: var(--c-card); color: var(--c-text); border: 1.5px solid var(--c-border); }
.ll-btn--outline:hover { border-color: var(--c-subtle); background: #f9fafb; }
.ll-btn--loading, .ll-btn:disabled { opacity: 0.65; pointer-events: none; }

/* Select */
.ll-select { padding: 8px 12px; font-size: 13px; font-family: "Plus Jakarta Sans", sans-serif; border: 1.5px solid var(--c-border); border-radius: var(--radius-sm); background: var(--c-card); color: var(--c-text); cursor: pointer; outline: none; transition: border-color 0.15s; }
.ll-select:focus { border-color: var(--c-blue); }

/* Utility */
.ll-mono { font-family: "JetBrains Mono", monospace; }
.ll-text--muted { color: var(--c-muted); }

/* Spinner */
.ll-spinner { display: inline-block; width: 14px; height: 14px; border: 2px solid rgba(0,0,0,0.12); border-top-color: var(--c-muted); border-radius: 50%; animation: ll-spin 0.6s linear infinite; flex-shrink: 0; }
@keyframes ll-spin { to { transform: rotate(360deg); } }

@media (max-width: 700px) {
  .ll-page { padding: 20px 16px 40px; }
  .ll-tier-grid { grid-template-columns: 1fr 1fr; }
}
</style>