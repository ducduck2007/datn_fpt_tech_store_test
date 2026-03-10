<template>
  <div class="inactive-wrapper">
    <!-- Header -->
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
        <button class="reload-btn" @click="loadAll" :disabled="loading">
          <svg v-if="!loading" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
          <span class="spinner-sm" v-else />
          Làm mới
        </button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tab-strip">
      <button
        v-for="tab in tabs"
        :key="tab.days"
        class="tab-btn"
        :class="{ active: activeTab === tab.days, [`tab-${tab.color}`]: true }"
        @click="switchTab(tab.days)"
      >
        <span class="tab-dot" :class="`dot-${tab.color}`" />
        <span class="tab-label">{{ tab.label }}</span>
        <span class="tab-count" :class="{ loaded: counts[tab.days] !== null }">
          {{ counts[tab.days] !== null ? counts[tab.days] : '—' }}
        </span>
      </button>
    </div>

    <!-- Stats Row -->
    <div class="stats-row" v-if="!loading && current.length > 0">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-val">{{ current.length }}</div>
        <div class="stat-lbl">Khách hàng</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-val gold">{{ totalPoints }}</div>
        <div class="stat-lbl">Tổng điểm tích lũy</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-val green">{{ formatMoneyShort(totalSpent) }}</div>
        <div class="stat-lbl">Tổng đã chi</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏅</div>
        <div class="stat-val blue">{{ topTier }}</div>
        <div class="stat-lbl">Tier cao nhất</div>
      </div>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar" v-if="!loading">
      <div class="search-wrap">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#aaa" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input v-model="q" placeholder="Tìm theo tên, email, SĐT..." class="search-input" />
        <button v-if="q" class="clear-btn" @click="q = ''">✕</button>
      </div>
      <select v-model="tierFilter" class="filter-select">
        <option value="">Tất cả Tier</option>
        <option v-for="t in tiers" :key="t" :value="t">{{ t }}</option>
      </select>
      <select v-model="sortBy" class="filter-select">
        <option value="spent">Chi tiêu nhiều nhất</option>
        <option value="points">Điểm nhiều nhất</option>
        <option value="name">Tên A→Z</option>
      </select>
    </div>

    <!-- Skeleton -->
    <div class="cards-grid" v-if="loading">
      <div class="skeleton-card" v-for="i in 6" :key="i">
        <div class="sk sk-avatar" />
        <div class="sk-lines">
          <div class="sk sk-line" style="width:70%" />
          <div class="sk sk-line" style="width:50%" />
          <div class="sk sk-line" style="width:40%" />
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div class="empty-state" v-if="!loading && filtered.length === 0">
      <div class="empty-icon">🎉</div>
      <div class="empty-title">Không có khách hàng nào!</div>
      <div class="empty-sub">Tất cả khách hàng đều hoạt động trong {{ activeTab }} ngày qua.</div>
    </div>

    <!-- Cards Grid -->
    <div class="cards-grid" v-if="!loading && paged.length > 0">
      <div
        class="cust-card"
        v-for="(row, idx) in paged"
        :key="row.id"
        :style="{ animationDelay: idx * 0.05 + 's' }"
      >
        <!-- Tier stripe -->
        <div class="tier-stripe" :class="'stripe-' + (row.raw?.vipTier || 'none').toLowerCase()" />

        <!-- Head -->
        <div class="card-head">
          <div class="avatar" :style="{ background: avatarColor(row.fullName) }">
            {{ initials(row.fullName) }}
          </div>
          <div class="card-name-block">
            <div class="card-name">{{ row.fullName }}</div>
            <div class="card-email">{{ row.email }}</div>
          </div>
          <span v-if="row.raw?.vipTier" class="tier-pill" :class="'pill-' + row.raw.vipTier.toLowerCase()">
            {{ row.raw.vipTier }}
          </span>
        </div>

        <!-- Divider -->
        <div class="card-divider" />

        <!-- Metrics -->
        <div class="card-metrics">
          <div class="metric">
            <div class="metric-val">{{ (row.loyaltyPoints || 0).toLocaleString() }}</div>
            <div class="metric-lbl">Điểm</div>
          </div>
          <div class="metric-sep" />
          <div class="metric">
            <div class="metric-val">{{ formatMoneyShort(row.raw?.totalSpent) }}</div>
            <div class="metric-lbl">Chi tiêu</div>
          </div>
          <div class="metric-sep" />
          <div class="metric">
            <div class="metric-val sm">{{ row.phone || '—' }}</div>
            <div class="metric-lbl">SĐT</div>
          </div>
        </div>

        <!-- Footer -->
        <div class="card-footer">
          <div class="last-order" :class="{ 'no-order': !row.raw?.lastOrderAt }">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            {{ row.raw?.lastOrderAt ? 'Đơn cuối: ' + relativeDate(row.raw.lastOrderAt) : 'Chưa có đơn hàng' }}
          </div>
          <button class="view-btn" @click="openDetail(row)">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          </button>
        </div>

        <!-- Hover badge -->
        <div class="inactive-badge">{{ activeTab }}+ ngày không GD</div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-row" v-if="!loading && filtered.length > pageSize">
      <button class="page-btn" :disabled="page === 1" @click="page--">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <button
        v-for="p in totalPages"
        :key="p"
        class="page-btn"
        :class="{ 'page-active': p === page }"
        @click="page = p"
      >{{ p }}</button>
      <button class="page-btn" :disabled="page === totalPages" @click="page++">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
      <span class="page-info">{{ (page-1)*pageSize+1 }}–{{ Math.min(page*pageSize, filtered.length) }} / {{ filtered.length }}</span>
    </div>

    <!-- Detail Modal -->
    <div class="modal-overlay" v-if="detail.open" @click.self="detail.open = false">
      <div class="modal-box" v-if="detail.customer">
        <button class="modal-close" @click="detail.open = false">✕</button>

        <div class="modal-avatar" :style="{ background: avatarColor(detail.customer.fullName) }">
          {{ initials(detail.customer.fullName) }}
        </div>
        <div class="modal-name">{{ detail.customer.fullName }}</div>
        <div class="modal-email">{{ detail.customer.email }}</div>

        <span v-if="detail.customer.raw?.vipTier" class="tier-pill lg" :class="'pill-' + detail.customer.raw.vipTier.toLowerCase()">
          {{ detail.customer.raw.vipTier }}
        </span>

        <div class="modal-stats">
          <div class="ms-item">
            <div class="ms-val gold">{{ (detail.customer.loyaltyPoints || 0).toLocaleString() }}</div>
            <div class="ms-lbl">Điểm tích lũy</div>
          </div>
          <div class="ms-item">
            <div class="ms-val green">{{ formatMoney(detail.customer.raw?.totalSpent) }}</div>
            <div class="ms-lbl">Tổng chi tiêu</div>
          </div>
        </div>

        <div class="modal-info">
          <div class="mi-row"><span class="mi-lbl">SĐT</span><span>{{ detail.customer.phone || '—' }}</span></div>
          <div class="mi-row"><span class="mi-lbl">Ngày sinh</span><span>{{ detail.customer.birthDate || '—' }}</span></div>
          <div class="mi-row"><span class="mi-lbl">Địa chỉ</span><span>{{ detail.customer.raw?.address || '—' }}</span></div>
          <div class="mi-row">
            <span class="mi-lbl">Đơn cuối</span>
            <span class="warn-text">{{ relativeDate(detail.customer.raw?.lastOrderAt) }}</span>
          </div>
          <div class="mi-row" v-if="detail.customer.notes">
            <span class="mi-lbl">Ghi chú</span><span>{{ detail.customer.notes }}</span>
          </div>
        </div>

        <div class="modal-alert">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          Khách hàng này chưa giao dịch trong <strong>{{ activeTab }}+ ngày</strong>. Cân nhắc liên hệ lại!
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from "vue";
import { customersApi } from "../../api/customers.api";

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
  { days: 30, label: "30 ngày", color: "yellow" },
  { days: 60, label: "60 ngày", color: "orange" },
  { days: 90, label: "90 ngày", color: "red" },
];

const tiers = ["BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND"];
const detail = reactive({ open: false, customer: null });

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

const totalPages = computed(() => Math.ceil(filtered.value.length / pageSize));

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

function avatarColor(name) {
  const palette = ["#3b82f6","#8b5cf6","#ec4899","#f59e0b","#10b981","#ef4444","#06b6d4","#6366f1"];
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
/* ── Base ──────────────────────────────────────────── */
.inactive-wrapper {
  font-family: 'Be Vietnam Pro', 'Segoe UI', sans-serif;
  padding: 32px 36px 80px;
  background: #f5f5f5;
  min-height: 100vh;
}

/* ── Header ────────────────────────────────────────── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  color: #6b7280;
  margin-bottom: 6px;
}

.pulse-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: #3b82f6;
  animation: pulse 1.8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.5); }
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  color: #111827;
  margin: 0 0 4px;
  letter-spacing: -0.5px;
}

.page-sub {
  font-size: 13px;
  color: #9ca3af;
  margin: 0;
}

.reload-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  background: white;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  transition: all .15s;
}
.reload-btn:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }
.reload-btn:disabled { opacity: .6; cursor: not-allowed; }

.spinner-sm {
  width: 14px; height: 14px;
  border: 2px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Tabs ──────────────────────────────────────────── */
.tab-strip {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  padding: 10px 20px;
  border-radius: 10px;
  border: 1.5px solid #e5e7eb;
  background: white;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  transition: all .15s;
}
.tab-btn:hover { border-color: #d1d5db; color: #111827; }

.tab-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-yellow { background: #f59e0b; }
.dot-orange { background: #f97316; }
.dot-red    { background: #ef4444; }

.tab-count {
  padding: 1px 8px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  background: #f3f4f6;
  color: #9ca3af;
  transition: all .2s;
}
.tab-count.loaded { background: #111827; color: white; }

.tab-btn.active {
  border-color: #111827;
  background: #111827;
  color: white;
}
.tab-btn.active .tab-count { background: white; color: #111827; }
.tab-btn.active .tab-dot { opacity: 1; }

/* ── Stats ─────────────────────────────────────────── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.stat-icon { font-size: 18px; margin-bottom: 2px; }
.stat-val {
  font-size: 20px;
  font-weight: 800;
  color: #111827;
  line-height: 1.2;
}
.stat-val.gold  { color: #d97706; }
.stat-val.green { color: #059669; font-size: 16px; }
.stat-val.blue  { color: #2563eb; }
.stat-lbl { font-size: 12px; color: #9ca3af; }

/* ── Filter ────────────────────────────────────────── */
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.search-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 12px;
  transition: border-color .15s;
}
.search-wrap:focus-within { border-color: #3b82f6; }

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  color: #111827;
  padding: 9px 0;
  background: transparent;
  font-family: inherit;
}
.search-input::placeholder { color: #d1d5db; }

.clear-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #9ca3af;
  font-size: 12px;
  padding: 2px 4px;
}
.clear-btn:hover { color: #374151; }

.filter-select {
  padding: 9px 12px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 13px;
  color: #374151;
  background: white;
  cursor: pointer;
  outline: none;
  font-family: inherit;
  transition: border-color .15s;
}
.filter-select:focus { border-color: #3b82f6; }

/* ── Cards ─────────────────────────────────────────── */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 24px;
}
@media (max-width: 1100px) { .cards-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 700px)  { .cards-grid { grid-template-columns: 1fr; } }

.cust-card {
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 16px;
  overflow: hidden;
  transition: all .2s;
  position: relative;
  animation: fadeUp .35s ease both;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

.cust-card:hover {
  border-color: #e5e7eb;
  box-shadow: 0 8px 24px rgba(0,0,0,.08);
  transform: translateY(-3px);
}

/* Tier stripe */
.tier-stripe { height: 4px; width: 100%; }
.stripe-bronze   { background: linear-gradient(90deg, #cd7f32, #f0a96a); }
.stripe-silver   { background: linear-gradient(90deg, #94a3b8, #cbd5e1); }
.stripe-gold     { background: linear-gradient(90deg, #f59e0b, #fde68a); }
.stripe-platinum { background: linear-gradient(90deg, #6366f1, #a5b4fc); }
.stripe-diamond  { background: linear-gradient(90deg, #3b82f6, #93c5fd); }
.stripe-none     { background: #f3f4f6; }

/* Card head */
.card-head {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 14px 14px 12px;
}

.avatar {
  width: 42px; height: 42px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 14px; color: white;
  flex-shrink: 0;
  letter-spacing: .5px;
}

.card-name-block { flex: 1; min-width: 0; }
.card-name {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-email {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.tier-pill {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  letter-spacing: .4px;
  text-transform: uppercase;
  flex-shrink: 0;
}
.tier-pill.lg { font-size: 13px; padding: 5px 14px; border-radius: 8px; }
.pill-bronze   { background: #fef3c7; color: #92400e; }
.pill-silver   { background: #f1f5f9; color: #475569; }
.pill-gold     { background: #fef9c3; color: #b45309; }
.pill-platinum { background: #ede9fe; color: #6d28d9; }
.pill-diamond  { background: #dbeafe; color: #1d4ed8; }

.card-divider { height: 1px; background: #f3f4f6; margin: 0 14px; }

/* Metrics */
.card-metrics {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 12px 14px;
}

.metric { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.metric-val {
  font-size: 14px;
  font-weight: 800;
  color: #111827;
}
.metric-val.sm { font-size: 12px; font-weight: 600; color: #6b7280; }
.metric-lbl {
  font-size: 10px;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: .4px;
}
.metric-sep { width: 1px; height: 30px; background: #f3f4f6; }

/* Footer */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px 13px;
  border-top: 1px solid #f3f4f6;
}

.last-order {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 500;
  color: #f97316;
}
.last-order.no-order { color: #ef4444; }

.view-btn {
  width: 30px; height: 30px;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  color: #6b7280;
  transition: all .15s;
}
.view-btn:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }

/* Hover badge */
.inactive-badge {
  position: absolute;
  top: 10px; right: -30px;
  background: #ef4444;
  color: white;
  font-size: 9px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  padding: 3px 28px;
  transform: rotate(35deg);
  white-space: nowrap;
  opacity: 0;
  transition: opacity .2s;
  pointer-events: none;
}
.cust-card:hover .inactive-badge { opacity: 1; }

/* ── Skeleton ──────────────────────────────────────── */
.skeleton-card {
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  gap: 14px;
  align-items: center;
}

.sk {
  border-radius: 8px;
  background: linear-gradient(90deg, #f3f4f6 25%, #e9eaec 50%, #f3f4f6 75%);
  background-size: 200%;
  animation: shimmer 1.4s infinite;
}
.sk-avatar { width: 42px; height: 42px; border-radius: 12px; flex-shrink: 0; }
.sk-lines { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.sk-line { height: 12px; }

@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}

/* ── Empty ─────────────────────────────────────────── */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}
.empty-icon { font-size: 52px; margin-bottom: 16px; }
.empty-title { font-size: 20px; font-weight: 800; color: #111827; margin-bottom: 8px; }
.empty-sub { color: #9ca3af; font-size: 14px; }

/* ── Pagination ────────────────────────────────────── */
.pagination-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
}

.page-btn {
  min-width: 34px; height: 34px;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
  padding: 0 8px;
}
.page-btn:hover:not(:disabled) { border-color: #3b82f6; color: #3b82f6; }
.page-btn:disabled { opacity: .4; cursor: not-allowed; }
.page-btn.page-active { background: #111827; border-color: #111827; color: white; }
.page-info { font-size: 12px; color: #9ca3af; margin-left: 8px; }

/* ── Modal ─────────────────────────────────────────── */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.4);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  animation: fadeIn .2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.modal-box {
  background: white;
  border-radius: 20px;
  padding: 32px 28px 24px;
  width: 460px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  animation: slideUp .25s cubic-bezier(.34,1.56,.64,1);
  text-align: center;
}
@keyframes slideUp {
  from { transform: translateY(20px) scale(.97); opacity: 0; }
  to   { transform: translateY(0) scale(1); opacity: 1; }
}

.modal-close {
  position: absolute; top: 16px; right: 16px;
  width: 30px; height: 30px;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  color: #6b7280;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
}
.modal-close:hover { border-color: #ef4444; color: #ef4444; }

.modal-avatar {
  width: 64px; height: 64px;
  border-radius: 16px;
  margin: 0 auto 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 800; color: white;
}
.modal-name { font-size: 20px; font-weight: 800; color: #111827; margin-bottom: 4px; }
.modal-email { font-size: 13px; color: #9ca3af; margin-bottom: 12px; }

.modal-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin: 16px 0;
}
.ms-item {
  background: #f9fafb;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 14px;
}
.ms-val { font-size: 18px; font-weight: 800; color: #111827; margin-bottom: 3px; }
.ms-val.gold  { color: #d97706; }
.ms-val.green { color: #059669; font-size: 14px; }
.ms-lbl { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: .4px; }

.modal-info { text-align: left; margin-bottom: 16px; }
.mi-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
  color: #374151;
}
.mi-row:last-child { border-bottom: none; }
.mi-lbl { font-weight: 600; color: #9ca3af; }
.warn-text { color: #f97316; font-weight: 600; }

.modal-alert {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 10px;
  font-size: 12px;
  color: #dc2626;
  text-align: left;
}
</style>