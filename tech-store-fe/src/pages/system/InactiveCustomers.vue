<template>
  <div class="inactive-wrapper">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-badge">
          <span class="pulse-dot" />
          Monitoring
        </div>
        <h1 class="page-title">Khách Hàng Ngủ Đông</h1>
        <p class="page-sub">Theo dõi khách hàng chưa có giao dịch trong 30 · 60 · 90 ngày</p>
      </div>
      <div class="header-right">
        <el-button class="reload-btn" @click="loadAll" :loading="loading">
          <el-icon><Refresh /></el-icon>
          Làm mới
        </el-button>
      </div>
    </div>

    <!-- Time Window Tabs -->
    <div class="tab-strip">
      <button
        v-for="tab in tabs"
        :key="tab.days"
        class="tab-btn"
        :class="{ active: activeTab === tab.days, 'tab-danger': tab.days === 90, 'tab-warn': tab.days === 60, 'tab-info': tab.days === 30 }"
        @click="switchTab(tab.days)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
        <span class="tab-count" :class="{ loaded: counts[tab.days] !== null }">
          {{ counts[tab.days] !== null ? counts[tab.days] : '—' }}
        </span>
      </button>
    </div>

    <!-- Summary Stats -->
    <div class="stats-row" v-if="!loading && current.length > 0">
      <div class="stat-card">
        <div class="stat-val">{{ current.length }}</div>
        <div class="stat-lbl">Khách hàng</div>
      </div>
      <div class="stat-card">
        <div class="stat-val text-gold">{{ totalPoints }}</div>
        <div class="stat-lbl">Tổng điểm tích lũy</div>
      </div>
      <div class="stat-card">
        <div class="stat-val text-green">{{ formatMoney(totalSpent) }}</div>
        <div class="stat-lbl">Tổng đã chi</div>
      </div>
      <div class="stat-card">
        <div class="stat-val text-tier">{{ topTier }}</div>
        <div class="stat-lbl">Tier cao nhất</div>
      </div>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar" v-if="!loading">
      <el-input
        v-model="q"
        placeholder="Tìm theo tên, email, SĐT..."
        clearable
        class="search-input"
        :prefix-icon="Search"
      />
      <el-select v-model="tierFilter" placeholder="VIP Tier" clearable class="tier-select">
        <el-option v-for="t in tiers" :key="t" :label="t" :value="t">
          <el-tag :type="tierType(t)" size="small" effect="dark">{{ t }}</el-tag>
        </el-option>
      </el-select>
      <el-select v-model="sortBy" class="sort-select">
        <el-option label="Chi tiêu nhiều nhất" value="spent" />
        <el-option label="Điểm nhiều nhất" value="points" />
        <el-option label="Tên A→Z" value="name" />
      </el-select>
    </div>

    <!-- Empty State -->
    <div class="empty-state" v-if="!loading && filtered.length === 0">
      <div class="empty-icon">🎉</div>
      <div class="empty-title">Không có khách hàng nào!</div>
      <div class="empty-sub">Tất cả khách hàng đều hoạt động trong {{ activeTab }} ngày qua.</div>
    </div>

    <!-- Skeleton Loading -->
    <div class="skeleton-grid" v-if="loading">
      <div class="skeleton-card" v-for="i in 6" :key="i">
        <div class="sk sk-avatar" />
        <div class="sk-lines">
          <div class="sk sk-line w80" />
          <div class="sk sk-line w60" />
          <div class="sk sk-line w40" />
        </div>
      </div>
    </div>

    <!-- Customer Cards Grid -->
    <transition-group name="card-list" tag="div" class="cards-grid" v-if="!loading && paged.length > 0">
      <div
        class="cust-card"
        v-for="(row, idx) in paged"
        :key="row.id"
        :style="{ '--delay': idx * 0.04 + 's' }"
      >
        <!-- Top bar with tier color -->
        <div class="card-top-bar" :class="'bar-' + (row.raw?.vipTier || 'none').toLowerCase()" />

        <!-- Avatar + Name -->
        <div class="card-head">
          <div class="avatar" :style="{ background: avatarColor(row.fullName) }">
            {{ initials(row.fullName) }}
          </div>
          <div class="card-name-block">
            <div class="card-name">{{ row.fullName }}</div>
            <div class="card-email">{{ row.email }}</div>
          </div>
          <div class="card-tier-badge" v-if="row.raw?.vipTier">
            <el-tag :type="tierType(row.raw.vipTier)" size="small" effect="dark">
              {{ row.raw.vipTier }}
            </el-tag>
          </div>
        </div>

        <!-- Metrics -->
        <div class="card-metrics">
          <div class="metric">
            <span class="metric-icon">🏆</span>
            <span class="metric-val">{{ (row.loyaltyPoints || 0).toLocaleString() }}</span>
            <span class="metric-lbl">điểm</span>
          </div>
          <div class="metric-divider" />
          <div class="metric">
            <span class="metric-icon">💸</span>
            <span class="metric-val">{{ formatMoneyShort(row.raw?.totalSpent) }}</span>
            <span class="metric-lbl">chi tiêu</span>
          </div>
          <div class="metric-divider" />
          <div class="metric">
            <span class="metric-icon">📦</span>
            <span class="metric-val text-muted-val">{{ row.phone || '—' }}</span>
            <span class="metric-lbl">SĐT</span>
          </div>
        </div>

        <!-- Last Order -->
        <div class="card-footer">
          <div class="last-order" v-if="row.raw?.lastOrderAt">
            <el-icon><Clock /></el-icon>
            <span>Đơn cuối: <strong>{{ relativeDate(row.raw.lastOrderAt) }}</strong></span>
          </div>
          <div class="last-order no-order" v-else>
            <el-icon><Warning /></el-icon>
            <span>Chưa có đơn hàng nào</span>
          </div>

          <div class="card-actions">
            <el-tooltip content="Xem chi tiết">
              <el-button class="icon-btn" circle size="small" @click="openDetail(row)">
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>

        <!-- Inactive badge -->
        <div class="inactive-ribbon">
          <span>Không giao dịch {{ activeTab }}+ ngày</span>
        </div>
      </div>
    </transition-group>

    <!-- Pagination -->
    <div class="pagination-row" v-if="!loading && filtered.length > pageSize">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="pageSize"
        :total="filtered.length"
        :current-page="page"
        @current-change="v => page = v"
      />
      <span class="pagination-info">
        {{ (page - 1) * pageSize + 1 }}–{{ Math.min(page * pageSize, filtered.length) }} / {{ filtered.length }}
      </span>
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="detail.open" width="520px" class="detail-dialog" :title="''" align-center>
      <div v-if="detail.customer" class="detail-body">
        <div class="detail-avatar" :style="{ background: avatarColor(detail.customer.fullName) }">
          {{ initials(detail.customer.fullName) }}
        </div>
        <div class="detail-name">{{ detail.customer.fullName }}</div>
        <div class="detail-email">{{ detail.customer.email }}</div>

        <div class="detail-tier" v-if="detail.customer.raw?.vipTier">
          <el-tag :type="tierType(detail.customer.raw.vipTier)" effect="dark" size="large">
            {{ detail.customer.raw.vipTier }}
          </el-tag>
        </div>

        <div class="detail-stats">
          <div class="ds-item">
            <div class="ds-val text-gold">{{ (detail.customer.loyaltyPoints || 0).toLocaleString() }}</div>
            <div class="ds-lbl">Điểm tích lũy</div>
          </div>
          <div class="ds-item">
            <div class="ds-val text-green">{{ formatMoney(detail.customer.raw?.totalSpent) }}</div>
            <div class="ds-lbl">Tổng chi tiêu</div>
          </div>
        </div>

        <div class="detail-info-grid">
          <div class="di-row"><span class="di-lbl">SĐT</span><span>{{ detail.customer.phone || '—' }}</span></div>
          <div class="di-row"><span class="di-lbl">Ngày sinh</span><span>{{ detail.customer.birthDate || '—' }}</span></div>
          <div class="di-row"><span class="di-lbl">Địa chỉ</span><span>{{ detail.customer.raw?.address || '—' }}</span></div>
          <div class="di-row">
            <span class="di-lbl">Đơn cuối</span>
            <span class="text-warn">{{ relativeDate(detail.customer.raw?.lastOrderAt) }}</span>
          </div>
          <div class="di-row" v-if="detail.customer.notes">
            <span class="di-lbl">Ghi chú</span><span>{{ detail.customer.notes }}</span>
          </div>
        </div>

        <div class="detail-alert">
          <el-icon><WarnTriangleFilled /></el-icon>
          Khách hàng này chưa giao dịch trong <strong>{{ activeTab }}+ ngày</strong>. Cân nhắc liên hệ lại!
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from "vue";
import {
  Refresh, Search, View, Clock, Warning, WarnTriangleFilled
} from "@element-plus/icons-vue";
import { customersApi } from "../../api/customers.api";

// ── State ──────────────────────────────────────────────
const loading = ref(false);
const data = reactive({ 30: [], 60: [], 90: [] });
const counts = reactive({ 30: null, 60: null, 90: null });
const activeTab = ref(30);
const q = ref("");
const tierFilter = ref("");
const sortBy = ref("spent");
const page = ref(1);
const pageSize = 9;

const tabs = [
  { days: 30, label: "30 ngày", icon: "🟡" },
  { days: 60, label: "60 ngày", icon: "🟠" },
  { days: 90, label: "90 ngày", icon: "🔴" },
];

const tiers = ["BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND"];

const detail = reactive({ open: false, customer: null });

// ── Current data ───────────────────────────────────────
const current = computed(() => data[activeTab.value] || []);

const filtered = computed(() => {
  let list = current.value;
  if (tierFilter.value) list = list.filter(r => r.raw?.vipTier === tierFilter.value);
  const kw = q.value.trim().toLowerCase();
  if (kw) list = list.filter(r => `${r.fullName} ${r.email} ${r.phone}`.toLowerCase().includes(kw));
  return [...list].sort((a, b) => {
    if (sortBy.value === "spent") return (b.raw?.totalSpent || 0) - (a.raw?.totalSpent || 0);
    if (sortBy.value === "points") return (b.loyaltyPoints || 0) - (a.loyaltyPoints || 0);
    return a.fullName.localeCompare(b.fullName);
  });
});

const paged = computed(() => {
  const s = (page.value - 1) * pageSize;
  return filtered.value.slice(s, s + pageSize);
});

const totalPoints = computed(() =>
  current.value.reduce((s, r) => s + (r.loyaltyPoints || 0), 0).toLocaleString()
);
const totalSpent = computed(() =>
  current.value.reduce((s, r) => s + parseFloat(r.raw?.totalSpent || 0), 0)
);
const topTier = computed(() => {
  const order = ["DIAMOND", "PLATINUM", "GOLD", "SILVER", "BRONZE"];
  for (const t of order) {
    if (current.value.some(r => r.raw?.vipTier === t)) return t;
  }
  return "—";
});

// ── API ────────────────────────────────────────────────
function normalize(list) {
  return (list || []).map(c => ({
    id: c.id,
    fullName: c.name ?? c.fullName ?? "",
    email: c.email ?? "",
    phone: c.phone ?? "",
    birthDate: c.dateOfBirth ?? c.birthDate ?? "",
    customerType: c.customerType ?? "REGULAR",
    loyaltyPoints: c.loyaltyPoints ?? 0,
    notes: c.notes ?? "",
    raw: c,
  }));
}

async function fetchFor(days) {
  try {
    const res = await customersApi.listInactiveTransaction(days);
    const arr = Array.isArray(res?.data) ? res.data : (res?.data?.content ?? []);
    data[days] = normalize(arr);
    counts[days] = data[days].length;
  } catch {
    data[days] = [];
    counts[days] = 0;
  }
}

async function loadAll() {
  loading.value = true;
  await Promise.all([fetchFor(30), fetchFor(60), fetchFor(90)]);
  loading.value = false;
}

function switchTab(days) {
  activeTab.value = days;
  page.value = 1;
  q.value = "";
  tierFilter.value = "";
}

// ── Helpers ────────────────────────────────────────────
function tierType(tier) {
  return { BRONZE: "info", SILVER: "", GOLD: "warning", PLATINUM: "danger", DIAMOND: "success" }[tier] || "info";
}

function avatarColor(name) {
  const palette = ["#e07a5f","#3d405b","#81b29a","#f2cc8f","#264653","#2a9d8f","#e76f51","#457b9d"];
  const h = (name || "?").split("").reduce((a, c) => a + c.charCodeAt(0), 0);
  return palette[h % palette.length];
}

function initials(name) {
  if (!name) return "?";
  const p = name.trim().split(" ");
  return p.length === 1 ? p[0].slice(0, 2).toUpperCase() : (p[0][0] + p[p.length - 1][0]).toUpperCase();
}

function formatMoney(val) {
  if (!val) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(val);
}

function formatMoneyShort(val) {
  if (!val) return "0";
  const n = parseFloat(val);
  if (n >= 1e9) return (n / 1e9).toFixed(1) + " tỷ";
  if (n >= 1e6) return (n / 1e6).toFixed(1) + " tr";
  if (n >= 1e3) return (n / 1e3).toFixed(0) + "K";
  return n.toLocaleString();
}

function relativeDate(iso) {
  if (!iso) return "Chưa có";
  const d = new Date(iso);
  const diff = Math.floor((Date.now() - d.getTime()) / 86400000);
  if (diff === 0) return "Hôm nay";
  if (diff === 1) return "Hôm qua";
  if (diff < 30) return `${diff} ngày trước`;
  if (diff < 365) return `${Math.floor(diff / 30)} tháng trước`;
  return `${Math.floor(diff / 365)} năm trước`;
}

function openDetail(row) {
  detail.customer = row;
  detail.open = true;
}

onMounted(loadAll);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:ital,wght@0,300;0,400;0,500;1,400&display=swap');

.inactive-wrapper {
  font-family: 'DM Sans', sans-serif;
  padding: 28px 32px;
  background: #f7f6f3;
  min-height: 100vh;
}

/* ── Header ───────────────────────────── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 28px;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: #e07a5f;
  margin-bottom: 6px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e07a5f;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.4); }
}

.page-title {
  font-family: 'Syne', sans-serif;
  font-size: 30px;
  font-weight: 800;
  color: #1a1a2e;
  margin: 0 0 4px;
  line-height: 1.1;
}

.page-sub {
  color: #888;
  font-size: 14px;
  margin: 0;
}

.reload-btn {
  font-family: 'Syne', sans-serif;
  font-weight: 600;
  border: 2px solid #1a1a2e;
  border-radius: 10px;
  background: #1a1a2e;
  color: #fff;
  padding: 10px 20px;
  transition: all 0.2s;
}
.reload-btn:hover {
  background: #2d2d4a;
  border-color: #2d2d4a;
}

/* ── Tabs ─────────────────────────────── */
.tab-strip {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 22px;
  border-radius: 14px;
  border: 2px solid #e8e6e0;
  background: #fff;
  cursor: pointer;
  font-family: 'Syne', sans-serif;
  font-weight: 600;
  font-size: 14px;
  color: #555;
  transition: all 0.2s;
}

.tab-btn:hover { border-color: #aaa; transform: translateY(-2px); }

.tab-icon { font-size: 18px; }
.tab-count {
  display: inline-block;
  min-width: 28px;
  text-align: center;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  background: #f0ede8;
  color: #888;
  transition: all 0.3s;
}
.tab-count.loaded { background: #1a1a2e; color: #fff; }

.tab-btn.active.tab-info  { border-color: #f2cc8f; background: #fffbf0; color: #b8860b; }
.tab-btn.active.tab-warn  { border-color: #e07a5f; background: #fff5f2; color: #c0392b; }
.tab-btn.active.tab-danger{ border-color: #c0392b; background: #fff0ee; color: #a61c00; box-shadow: 0 4px 16px rgba(192,57,43,0.15); }

/* ── Stats Row ────────────────────────── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 22px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px 20px;
  border: 2px solid #f0ede8;
  text-align: center;
}

.stat-val {
  font-family: 'Syne', sans-serif;
  font-size: 22px;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.stat-lbl { font-size: 12px; color: #aaa; font-weight: 500; }
.text-gold { color: #c9993b; }
.text-green { color: #2a9d8f; font-size: 16px !important; }
.text-tier { color: #e07a5f; }

/* ── Filter Bar ───────────────────────── */
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 22px;
  align-items: center;
}

.search-input { flex: 1; }
.tier-select { width: 160px; }
.sort-select { width: 200px; }

/* ── Cards Grid ───────────────────────── */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-bottom: 28px;
}

@media (max-width: 1100px) { .cards-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 700px)  { .cards-grid { grid-template-columns: 1fr; } }

.cust-card {
  background: #fff;
  border-radius: 18px;
  border: 2px solid #f0ede8;
  overflow: hidden;
  transition: all 0.25s;
  position: relative;
  animation: fadeUp 0.4s ease both;
  animation-delay: var(--delay);
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

.cust-card:hover {
  border-color: #1a1a2e;
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(26,26,46,0.10);
}

/* Top tier bar */
.card-top-bar { height: 5px; width: 100%; }
.bar-bronze   { background: linear-gradient(90deg, #cd7f32, #e8a87c); }
.bar-silver   { background: linear-gradient(90deg, #9e9e9e, #e0e0e0); }
.bar-gold     { background: linear-gradient(90deg, #f7c948, #ffe082); }
.bar-platinum { background: linear-gradient(90deg, #e53935, #ef9a9a); }
.bar-diamond  { background: linear-gradient(90deg, #43a047, #a5d6a7); }
.bar-none     { background: #f0ede8; }

/* Card head */
.card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 16px 10px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Syne', sans-serif;
  font-weight: 800;
  font-size: 15px;
  color: #fff;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.card-name-block { flex: 1; min-width: 0; }
.card-name { font-weight: 700; font-size: 15px; color: #1a1a2e; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-email { font-size: 12px; color: #aaa; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Metrics */
.card-metrics {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 10px 16px;
  background: #f9f8f5;
  border-top: 1px solid #f0ede8;
  border-bottom: 1px solid #f0ede8;
}

.metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.metric-icon { font-size: 15px; }
.metric-val { font-family: 'Syne', sans-serif; font-weight: 700; font-size: 14px; color: #1a1a2e; }
.metric-lbl { font-size: 10px; color: #aaa; text-transform: uppercase; letter-spacing: 0.5px; }
.text-muted-val { font-size: 12px !important; color: #666; }
.metric-divider { width: 1px; height: 32px; background: #e8e6e0; }

/* Footer */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px 12px;
}

.last-order {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #e07a5f;
  font-weight: 500;
}
.last-order.no-order { color: #c0392b; }

.card-actions { display: flex; gap: 6px; }
.icon-btn { border-color: #eee !important; }

/* Ribbon */
.inactive-ribbon {
  position: absolute;
  top: 14px;
  right: -22px;
  background: #c0392b;
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.4px;
  text-transform: uppercase;
  padding: 3px 28px;
  transform: rotate(35deg);
  transform-origin: center;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.2s;
}
.cust-card:hover .inactive-ribbon { opacity: 1; }

/* ── Skeleton ────────────────────────── */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-bottom: 28px;
}

.skeleton-card {
  background: #fff;
  border-radius: 18px;
  border: 2px solid #f0ede8;
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: center;
}

.sk { border-radius: 8px; background: linear-gradient(90deg, #f0ede8 25%, #e8e5de 50%, #f0ede8 75%); background-size: 200%; animation: shimmer 1.4s infinite; }
.sk-avatar { width: 44px; height: 44px; border-radius: 12px; flex-shrink: 0; }
.sk-lines { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.sk-line { height: 12px; }
.w80 { width: 80%; }
.w60 { width: 60%; }
.w40 { width: 40%; }

@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}

/* ── Empty ──────────────────────────── */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}
.empty-icon { font-size: 56px; margin-bottom: 16px; }
.empty-title { font-family: 'Syne', sans-serif; font-size: 22px; font-weight: 800; color: #1a1a2e; margin-bottom: 8px; }
.empty-sub { color: #aaa; font-size: 15px; }

/* ── Pagination ─────────────────────── */
.pagination-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
}
.pagination-info { font-size: 13px; color: #aaa; }

/* ── Detail Dialog ──────────────────── */
.detail-body { text-align: center; padding: 16px 8px 8px; }

.detail-avatar {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Syne', sans-serif;
  font-size: 24px;
  font-weight: 800;
  color: #fff;
}

.detail-name { font-family: 'Syne', sans-serif; font-size: 22px; font-weight: 800; color: #1a1a2e; }
.detail-email { color: #aaa; font-size: 14px; margin-bottom: 10px; }
.detail-tier { margin-bottom: 20px; }

.detail-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}
.ds-item { background: #f9f8f5; border-radius: 14px; padding: 14px; border: 1px solid #f0ede8; }
.ds-val { font-family: 'Syne', sans-serif; font-size: 20px; font-weight: 800; margin-bottom: 4px; }
.ds-lbl { font-size: 11px; color: #aaa; text-transform: uppercase; letter-spacing: 0.5px; }

.detail-info-grid { text-align: left; margin-bottom: 16px; }
.di-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0ede8;
  font-size: 14px;
}
.di-row:last-child { border-bottom: none; }
.di-lbl { color: #888; font-weight: 600; }
.text-warn { color: #e07a5f; font-weight: 600; }

.detail-alert {
  background: #fff5f2;
  border: 1.5px solid #e07a5f;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 13px;
  color: #c0392b;
  display: flex;
  align-items: center;
  gap: 8px;
  text-align: left;
}

/* Transitions */
.card-list-enter-active, .card-list-leave-active { transition: all 0.3s ease; }
.card-list-enter-from { opacity: 0; transform: translateY(12px); }
.card-list-leave-to { opacity: 0; transform: scale(0.95); }
</style>