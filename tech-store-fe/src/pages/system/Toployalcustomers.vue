<template>
  <div class="container-xl">
    <el-card shadow="never">

      <!-- ══════════════════════════════════════════
           HEADER
      ══════════════════════════════════════════ -->
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">
            <el-icon class="me-1"><Medal /></el-icon>
            Loyalty Analytics
          </div>
          <div class="title">Top Khách Hàng Trung Thành</div>
          <div class="muted">
            Đánh giá tổng hợp: điểm tích lũy · hạng VIP · chi tiêu · tần suất hoạt động
          </div>
        </div>
        <div class="d-flex gap-2 align-items-center">
          <el-select v-model="topN" style="width:110px" @change="loadCustomers">
            <el-option label="Top 5"  :value="5" />
            <el-option label="Top 10" :value="10" />
            <el-option label="Top 20" :value="20" />
            <el-option label="Top 50" :value="50" />
          </el-select>
          <el-button @click="loadData" :loading="loading">
            <el-icon class="me-1"><Refresh /></el-icon>
            Reload
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- ══════════════════════════════════════════
           STAT CARDS
           Nguồn: tính từ top-N list + spending-stats
      ══════════════════════════════════════════ -->
      <div class="row g-3 mb-4">
        <div class="col-6 col-md-3">
          <el-card shadow="hover" class="stat-card stat-card-purple">
            <div class="stat-icon"><el-icon :size="28"><Trophy /></el-icon></div>
            <div class="stat-content">
              <div class="stat-label">Tổng điểm (Top {{ topN }})</div>
              <div class="stat-value">{{ totalLoyaltyPoints.toLocaleString() }}</div>
              <div class="stat-source">Σ loyaltyPoints</div>
            </div>
          </el-card>
        </div>
        <div class="col-6 col-md-3">
          <el-card shadow="hover" class="stat-card stat-card-gold">
            <div class="stat-icon"><el-icon :size="28"><User /></el-icon></div>
            <div class="stat-content">
              <div class="stat-label">Khách VIP</div>
              <div class="stat-value">{{ vipCount }}</div>
              <div class="stat-source">customerType = VIP</div>
            </div>
          </el-card>
        </div>
        <div class="col-6 col-md-3">
          <el-card shadow="hover" class="stat-card stat-card-green">
            <div class="stat-icon"><el-icon :size="28"><TrendCharts /></el-icon></div>
            <div class="stat-content">
              <div class="stat-label">TB điểm / KH</div>
              <div class="stat-value">{{ avgPoints.toLocaleString() }}</div>
              <div class="stat-source">avg loyaltyPoints</div>
            </div>
          </el-card>
        </div>
        <div class="col-6 col-md-3">
          <el-card shadow="hover" class="stat-card stat-card-blue">
            <div class="stat-icon"><el-icon :size="28"><Coin /></el-icon></div>
            <div class="stat-content">
              <div class="stat-label">#1 Trung Thành</div>
              <div class="stat-value-sm">{{ sortedCustomers[0]?.fullName || '—' }}</div>
              <div class="stat-source">
                {{ sortedCustomers[0] ? (sortedCustomers[0].loyaltyPoints || 0).toLocaleString() + ' pts' : '' }}
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           TIER BREAKDOWN — spending-stats.byTier
      ══════════════════════════════════════════ -->
      <div class="mb-4" v-if="Object.keys(stats.byTier || {}).length">
        <div class="section-label mb-2">
          Phân bổ chi tiêu theo hạng VIP
        </div>
        <div class="tier-breakdown-grid">
          <div
            v-for="(td, tier) in stats.byTier"
            :key="tier"
            class="tier-breakdown-card"
            :class="`tbc-${tier.toLowerCase()}`"
          >
            <div class="tbc-header">
              <el-tag :type="getTierTagType(tier)" effect="dark" size="small">
                {{ TIER_MAP[tier]?.icon }} {{ tier }}
              </el-tag>
              <span class="tbc-count">{{ td.customerCount }} KH</span>
            </div>
            <div class="tbc-spent">{{ fmtShort(td.totalSpent) }}</div>
            <div class="tbc-avg">TB: {{ fmtShort(td.averageSpent) }}</div>
            <el-progress
              :percentage="stats.totalSpent ? Math.round((td.totalSpent / stats.totalSpent) * 100) : 0"
              :stroke-width="5"
              :color="TIER_MAP[tier]?.color"
              :show-text="true"
            />
          </div>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           CONTROLS: TIER FILTER + SORT
      ══════════════════════════════════════════ -->
      <div class="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
        <el-tabs v-model="activeTier" @tab-change="onTierChange" class="tier-filter-tabs">
          <el-tab-pane v-for="t in TIER_OPTIONS" :key="t.value" :name="t.value">
            <template #label>{{ t.icon }} {{ t.label }}</template>
          </el-tab-pane>
        </el-tabs>

        <div class="d-flex align-items-center gap-2">
          <span class="text-muted small">Sắp xếp:</span>
          <el-radio-group v-model="sortKey" size="small">
            <el-radio-button value="loyaltyPoints">Điểm</el-radio-button>
            <el-radio-button value="totalSpent">Chi tiêu</el-radio-button>
            <el-radio-button value="orderCount">Số lần mua</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           LEADERBOARD TABLE
           Cột khác biệt so với "By Spending Range":
           ✅ loyaltyPoints + progress bar trong tier
           ✅ pointsToNextTier (tiềm năng)
           ✅ discountRate (quyền lợi đang hưởng)
           ✅ lastOrderAt + lastLoginAt (còn active?)
           ✅ vipNote (ghi chú nội bộ)
      ══════════════════════════════════════════ -->
      <el-table
        v-loading="loading"
        :data="sortedCustomers"
        border
        stripe
        class="loyal-table"
        :row-class-name="rowClassName"
        highlight-current-row
      >

        <!-- Rank -->
        <el-table-column label="Rank" width="70" align="center" fixed>
          <template #default="{ $index }">
            <div :class="['rank-badge', getRankClass($index)]">
              <span v-if="$index === 0">🥇</span>
              <span v-else-if="$index === 1">🥈</span>
              <span v-else-if="$index === 2">🥉</span>
              <span v-else class="rank-num">{{ $index + 1 }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- Khách hàng -->
        <el-table-column label="Khách hàng" min-width="210" fixed>
          <template #default="{ row }">
            <div class="cus-cell">
              <el-avatar :size="38" :style="{ background: getAvatarColor(row.fullName) }">
                {{ getInitials(row.fullName) }}
              </el-avatar>
              <div class="cus-info">
                <div class="cus-name">{{ row.fullName }}</div>
                <div class="cus-sub">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- Hạng VIP -->
        <el-table-column label="Hạng VIP" width="130" align="center">
          <template #default="{ row }">
            <div class="tier-cell">
              <el-tag
                v-if="row.raw.vipTier"
                :type="getTierTagType(row.raw.vipTier)"
                effect="dark"
              >
                {{ TIER_MAP[row.raw.vipTier]?.icon }} {{ row.raw.vipTier }}
              </el-tag>
              <el-tag v-else type="info" effect="plain" size="small">Member</el-tag>
              <div class="tier-type-sub">{{ row.customerType }}</div>
            </div>
          </template>
        </el-table-column>

        <!-- ✅ Điểm tích lũy — metric CHÍNH của loyalty, KHÔNG có ở spending tab -->
        <el-table-column label="Điểm tích lũy" width="170" align="center" sortable prop="loyaltyPoints">
          <template #default="{ row }">
            <div class="points-cell">
              <el-tag type="warning" effect="plain" size="large">
                <el-icon class="me-1"><Coin /></el-icon>
                {{ (row.loyaltyPoints || 0).toLocaleString() }}
              </el-tag>
              <!-- Progress bar tiến độ trong hạng hiện tại -->
              <el-progress
                v-if="row.raw.vipTier"
                :percentage="getTierProgress(row.raw)"
                :stroke-width="4"
                :show-text="false"
                :color="TIER_MAP[row.raw.vipTier]?.color"
                class="pts-progress"
              />
            </div>
          </template>
        </el-table-column>

        <!-- ✅ Số lần mua — tần suất, thể hiện trung thành thực sự -->
        <!-- Nguồn: orderCount từ CustomerResponse (orderRepository.countByCustomerId) -->
        <el-table-column label="Số lần mua" width="130" align="center" sortable>
          <template #default="{ row }">
            <div class="order-count-cell">
              <div class="oc-num">{{ (row.raw.orderCount || 0).toLocaleString() }}</div>
              <div class="oc-label">đơn hàng</div>
              <!-- Màu badge theo tần suất -->
              <el-tag
                :type="getOrderFreqType(row.raw.orderCount)"
                effect="light"
                size="small"
                class="oc-freq"
              >
                {{ getOrderFreqLabel(row.raw.orderCount) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <!-- ✅ Còn thiếu bao nhiêu pts — chỉ số tiềm năng thăng hạng -->
        <el-table-column label="Còn thiếu" width="160" align="center">
          <template #default="{ row }">
            <div v-if="row.raw.pointsToNextTier > 0" class="next-tier-cell">
              <div class="next-pts">{{ row.raw.pointsToNextTier.toLocaleString() }} pts</div>
              <div class="next-label">để lên hạng tiếp</div>
            </div>
            <el-tag v-else type="success" effect="light" size="small">✓ Max tier</el-tag>
          </template>
        </el-table-column>

        <!-- Tổng chi tiêu + relative progress bar -->
        <el-table-column label="Tổng chi tiêu" width="200" align="right" sortable>
          <template #default="{ row }">
            <div class="spent-cell">
              <div class="spent-val">{{ formatCurrency(row.raw.totalSpent) }}</div>
              <el-progress
                :percentage="maxSpent ? Math.round((row.raw.totalSpent / maxSpent) * 100) : 0"
                :stroke-width="4"
                :show-text="false"
                :color="TIER_MAP[row.raw.vipTier]?.color || '#409EFF'"
                class="spent-progress"
              />
            </div>
          </template>
        </el-table-column>

        <!-- ✅ Ưu đãi đang hưởng — từ VipTier.getDiscountRate() -->
        <el-table-column label="Ưu đãi" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.raw.discountRate" type="success" effect="plain">
              -{{ (row.raw.discountRate * 100).toFixed(0) }}%
            </el-tag>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>

        <!-- ✅ Hoạt động — phân biệt KH active vs dormant (spending tab không có) -->
        <el-table-column label="Hoạt động" width="155" align="center">
          <template #default="{ row }">
            <div class="activity-cell">
              <div class="act-row">
                <span class="act-icon">📦</span>
                <span :class="['act-txt', getActivityClass(row.raw.lastOrderAt)]">
                  {{ fmtRelative(row.raw.lastOrderAt) }}
                </span>
              </div>
              <div class="act-row">
                <span class="act-icon">🔐</span>
                <span class="act-txt act-muted">{{ fmtRelative(row.raw.lastLoginAt) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- ✅ VIP Note — ghi chú nội bộ, chỉ admin/staff thấy -->
        <el-table-column label="⭐ VIP Note" min-width="150">
          <template #default="{ row }">
            <template v-if="row.raw.vipTier">
              <el-tooltip
                v-if="row.raw.vipNote"
                :content="row.raw.vipNote"
                placement="top"
                :show-after="200"
              >
                <div class="vipnote-chip">
                  <span>⭐</span>
                  <span class="vipnote-txt">
                    {{ row.raw.vipNote.substring(0, 22) }}{{ row.raw.vipNote.length > 22 ? '…' : '' }}
                  </span>
                </div>
              </el-tooltip>
              <span v-else class="text-muted small fst-italic">Chưa có note</span>
            </template>
            <span v-else class="text-muted small">—</span>
          </template>
        </el-table-column>

      </el-table>

      <el-empty
        v-if="!loading && sortedCustomers.length === 0"
        description="Không có dữ liệu"
        :image-size="80"
        class="mt-4"
      />

      <!-- ══════════════════════════════════════════
           FOOTER — giải thích sự khác biệt
      ══════════════════════════════════════════ -->
      <div class="mt-4">
        <el-alert type="info" :closable="false" show-icon>
          <template #default>
            <strong>Khác với "By Spending Range":</strong>
            Trang này đo <em>độ trung thành tổng hợp</em> —
            KH mua 1 lần 200M chưa chắc trung thành hơn KH mua 40 lần tổng 80M.
            Metric chính là <code>loyaltyPoints</code> (tần suất) +
            <code>vipTier</code> (hạng) + <code>lastOrderAt</code> (còn active?).
          </template>
        </el-alert>
      </div>

    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import {
  Refresh, Medal, Trophy, User,
  TrendCharts, Coin,
} from "@element-plus/icons-vue";
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
      // orderCount
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
// KHÔNG cần thay đổi backend gì cả.
// Dùng đúng 3 endpoint đã có trong CusController:
//
//   GET /api/auth/customers/top-spenders?limit=N
//       → listTopSpenders(limit)
//
//   GET /api/auth/customers/vip-tier/:tier/top-spenders?limit=N
//       → listTopSpendersByVipTier(tier, limit)
//
//   GET /api/auth/customers/spending-stats
//       → getSpendingStatistics()
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
  if (!lastOrderAt) return "act-inactive";
  const diff = (Date.now() - new Date(lastOrderAt)) / 86_400_000;
  if (diff < 7)  return "act-hot";
  if (diff < 30) return "act-warm";
  if (diff < 90) return "act-cool";
  return "act-inactive";
}

// Tần suất mua: badge màu theo số lần
function getOrderFreqType(count) {
  if (!count || count === 0) return "info";
  if (count >= 20) return "danger";   // VIP thường xuyên (đỏ nổi bật)
  if (count >= 10) return "warning";  // Khá thường xuyên
  if (count >= 5)  return "success";  // Trung bình
  return "info";                      // Mới mua ít
}

function getOrderFreqLabel(count) {
  if (!count || count === 0) return "Chưa mua";
  if (count >= 20) return "Rất thường xuyên";
  if (count >= 10) return "Thường xuyên";
  if (count >= 5)  return "Khá đều";
  return "Thỉnh thoảng";
}

function getTierTagType(tier) {
  return { BRONZE:"info", SILVER:"", GOLD:"warning", PLATINUM:"danger", DIAMOND:"success" }[tier] || "info";
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
  return p.length === 1 ? p[0].substring(0, 2).toUpperCase() : (p[0][0] + p[p.length-1][0]).toUpperCase();
}

function getAvatarColor(name) {
  const colors = ["#409EFF","#67C23A","#E6A23C","#F56C6C","#909399","#00d2d3","#e91e63","#9c27b0","#673ab7","#3f51b5"];
  const h = (name||"").split("").reduce((a, c) => a + c.charCodeAt(0), 0);
  return colors[h % colors.length];
}

function getRankClass(i) {
  return i === 0 ? "rank-gold" : i === 1 ? "rank-silver" : i === 2 ? "rank-bronze" : "";
}

function rowClassName({ rowIndex }) {
  if (rowIndex === 0) return "row-top1";
  if (rowIndex === 1) return "row-top2";
  if (rowIndex === 2) return "row-top3";
  return "";
}

onMounted(loadData);
</script>

<style scoped>
/* ── Base ── */
.kicker {
  display: flex; align-items: center;
  font-size: 12px; font-weight: 900;
  text-transform: uppercase; letter-spacing: 0.5px;
  color: #E6A23C; margin-bottom: 4px;
}
.title { font-weight: 900; font-size: 28px; margin-bottom: 4px; color: #303133; }
.muted { color: rgba(15,23,42,.62); font-size: 14px; }

/* ── Stat cards ── */
.stat-card {
  display: flex; align-items: center; gap: 14px;
  padding: 18px; border-radius: 10px;
  transition: transform .25s ease;
}
.stat-card:hover { transform: translateY(-3px); }
.stat-card-purple { background: linear-gradient(135deg,#667eea,#764ba2); color:#fff; }
.stat-card-gold   { background: linear-gradient(135deg,#f6d365,#fda085); color:#fff; }
.stat-card-green  { background: linear-gradient(135deg,#43e97b,#38f9d7); color:#1a4731; }
.stat-card-blue   { background: linear-gradient(135deg,#4facfe,#00f2fe); color:#fff; }
.stat-icon     { flex-shrink:0; opacity:.85; }
.stat-content  { flex:1; min-width:0; }
.stat-label    { font-size:12px; font-weight:600; opacity:.9; margin-bottom:3px; }
.stat-value    { font-size:22px; font-weight:900; line-height:1.1; }
.stat-value-sm { font-size:15px; font-weight:800; line-height:1.2; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.stat-source   { font-size:10px; opacity:.65; margin-top:2px; font-family:monospace; }

/* ── Tier breakdown ── */
.section-label  { font-size:13px; font-weight:700; color:#606266; text-transform:uppercase; letter-spacing:.5px; }
.section-source { font-size:11px; color:#c0c4cc; font-weight:400; font-family:monospace; margin-left:6px; }
.tier-breakdown-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(155px, 1fr));
  gap: 10px;
}
.tier-breakdown-card {
  border: 1px solid #e5e7eb; border-radius: 10px;
  padding: 12px 14px; background: #fff;
  transition: box-shadow .15s;
}
.tier-breakdown-card:hover { box-shadow: 0 3px 12px rgba(0,0,0,.08); }
.tbc-diamond  { border-color:#86efac; background:#f0fdf4; }
.tbc-platinum { border-color:#93c5fd; background:#eff6ff; }
.tbc-gold     { border-color:#fde68a; background:#fffbeb; }
.tbc-silver   { border-color:#d4d4d8; background:#f4f4f5; }
.tbc-bronze   { border-color:#fcd9a8; background:#fdf4eb; }
.tbc-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.tbc-count  { font-size:11px; color:#9ca3af; }
.tbc-spent  { font-size:18px; font-weight:800; color:#111827; margin-bottom:2px; }
.tbc-avg    { font-size:11px; color:#9ca3af; margin-bottom:8px; }

/* ── Tier filter tabs ── */
.tier-filter-tabs { margin-bottom: 0; }
:deep(.tier-filter-tabs .el-tabs__header) { margin-bottom: 0; }
:deep(.tier-filter-tabs .el-tabs__item)   { font-size:13px; font-weight:600; }

/* ── Table ── */
:deep(.row-top1 td) { background: linear-gradient(90deg,#fffef0,transparent 55%) !important; }
:deep(.row-top2 td) { background: linear-gradient(90deg,#fafaff,transparent 55%) !important; }
:deep(.row-top3 td) { background: linear-gradient(90deg,#fdf5ef,transparent 55%) !important; }
:deep(.loyal-table .el-table th)  { background:#f5f7fa; font-weight:700; font-size:12px; }
:deep(.loyal-table .el-table__row:hover td) { background:#f9f9ff !important; }

/* ── Rank ── */
.rank-badge {
  display:inline-flex; align-items:center; justify-content:center;
  width:36px; height:36px; border-radius:50%; font-size:18px; font-weight:900;
}
.rank-gold   { background:linear-gradient(135deg,#ffd700,#ffed4e); color:#8b6914; box-shadow:0 3px 10px rgba(255,215,0,.45); }
.rank-silver { background:linear-gradient(135deg,#c0c0c0,#e8e8e8); color:#5a5a5a; box-shadow:0 3px 10px rgba(192,192,192,.45); }
.rank-bronze { background:linear-gradient(135deg,#cd7f32,#e6a23c); color:#5c3d1f; box-shadow:0 3px 10px rgba(205,127,50,.45); }
.rank-num    { font-size:13px; font-weight:700; color:#909399; }

/* ── Customer cell ── */
.cus-cell { display:flex; align-items:center; gap:10px; }
.cus-info { display:flex; flex-direction:column; gap:1px; }
.cus-name { font-weight:700; color:#111827; font-size:14px; }
.cus-sub  { font-size:11px; color:#9ca3af; }

/* ── Tier cell ── */
.tier-cell     { display:flex; flex-direction:column; align-items:center; gap:4px; }
.tier-type-sub { font-size:10px; color:#c0c4cc; }

/* ── Points ── */
.points-cell  { display:flex; flex-direction:column; align-items:center; gap:5px; }
.pts-progress { width:110px; }

/* ── Next tier ── */
.next-tier-cell { display:flex; flex-direction:column; align-items:center; gap:2px; }
.next-pts   { font-weight:800; color:#374151; font-size:14px; }
.next-label { font-size:10px; color:#9ca3af; }

/* ── Spent ── */
.spent-cell     { display:flex; flex-direction:column; gap:5px; }
.spent-val      { font-size:14px; font-weight:700; color:#15803d; }
.spent-progress { width:130px; }

/* ── Order count cell ── */
.order-count-cell { display:flex; flex-direction:column; align-items:center; gap:3px; }
.oc-num   { font-size:20px; font-weight:900; color:#303133; line-height:1; }
.oc-label { font-size:10px; color:#c0c4cc; }
.oc-freq  { margin-top:2px; }

/* ── Activity ── */
.activity-cell { display:flex; flex-direction:column; gap:4px; }
.act-row   { display:flex; align-items:center; gap:5px; }
.act-icon  { font-size:12px; }
.act-txt   { font-size:12px; }
.act-muted { color:#c0c4cc; }
.act-hot      { color:#16a34a; font-weight:600; }
.act-warm     { color:#ca8a04; }
.act-cool     { color:#9ca3af; }
.act-inactive { color:#f87171; }

/* ── VIP Note chip ── */
.vipnote-chip {
  display:inline-flex; align-items:center; gap:5px;
  background:#fffbeb; border:1px solid #fde68a;
  padding:4px 9px; border-radius:6px; cursor:pointer;
  max-width:180px; transition:background .15s;
}
.vipnote-chip:hover { background:#fef3c7; }
.vipnote-txt { font-size:12px; color:#92400e; overflow:hidden; white-space:nowrap; }

/* ── Responsive ── */
@media (max-width: 900px) {
  .tier-breakdown-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 640px) {
  .title { font-size: 20px; }
  .tier-breakdown-grid { grid-template-columns: 1fr 1fr; }
}
</style>