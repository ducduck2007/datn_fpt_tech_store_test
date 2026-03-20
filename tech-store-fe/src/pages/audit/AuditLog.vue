<template>
  <div class="al-page">

    <!-- ── Header ── -->
    <header class="al-header">
      <div class="al-header__left">
        <div class="al-eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
          Hệ thống
        </div>
        <h1 class="al-title">Audit <span class="al-title__accent">Logs</span></h1>
        <p class="al-subtitle">Lịch sử hoạt động và thay đổi toàn hệ thống</p>
      </div>

      <div class="al-header__right">
        <button class="al-btn al-btn--primary" @click="exportCsv">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          Export CSV
        </button>
      </div>
    </header>

    <!-- ── Filter Panel ── -->
    <div class="al-filter-panel">
      <div class="al-filter-panel__head">
        <div class="al-filter-panel__title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
          </svg>
          Bộ lọc
          <span v-if="activeFilterCount > 0" class="al-filter-badge">{{ activeFilterCount }} đang áp dụng</span>
        </div>
        <button v-if="activeFilterCount > 0" class="al-filter-clear" @click="resetFilter">Xoá tất cả</button>
      </div>

      <div class="al-filter-fields">
        <!-- Keyword -->
        <div class="al-filter-field">
          <span class="al-filter-field__label">Description</span>
          <input v-model="filter.keyword" class="al-input" placeholder="Từ khoá..." />
        </div>

        <!-- User ID -->
        <div class="al-filter-field">
          <span class="al-filter-field__label">User ID</span>
          <input v-model.number="filter.userId" class="al-input" placeholder="VD: 42" type="number" />
        </div>

        <!-- Module -->
        <div class="al-filter-field">
          <span class="al-filter-field__label">Module</span>
          <select v-model="filter.module" class="al-input al-select">
            <option value="">Tất cả module</option>
            <option value="USER">USER</option>
            <option value="CUSTOMER">CUSTOMER</option>
            <option value="ORDER">ORDER</option>
            <option value="PRODUCT">PRODUCT</option>
          </select>
        </div>

        <!-- Action -->
        <div class="al-filter-field">
          <span class="al-filter-field__label">Action</span>
          <select v-model="filter.action" class="al-input al-select">
            <option value="">Tất cả action</option>
            <option value="CREATE">CREATE</option>
            <option value="UPDATE">UPDATE</option>
            <option value="DELETE">DELETE</option>
            <option value="CHANGE_ROLE">CHANGE_ROLE</option>
          </select>
        </div>

        <!-- Date range -->
        <div class="al-filter-field al-filter-field--date">
          <span class="al-filter-field__label">Thời gian</span>
          <div class="al-date-range">
            <input type="date" class="al-input al-input--date" v-model="dateRange[0]" placeholder="Từ ngày" @change="syncDateRange" />
            <span class="al-date-range__sep">→</span>
            <input type="date" class="al-input al-input--date" v-model="dateRange[1]" placeholder="Đến ngày" @change="syncDateRange" />
          </div>
        </div>

        <!-- Actions -->
        <div class="al-filter-actions">
          <button class="al-btn al-btn--primary" @click="searchLogs">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
            Tìm kiếm
          </button>
          <button class="al-btn al-btn--orange" @click="loadThisWeek">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            Tuần này
          </button>
          <button class="al-btn al-btn--ghost" @click="resetFilter">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="1 4 1 10 7 10"/>
              <path d="M3.51 15a9 9 0 1 0 .49-4.9"/>
            </svg>
            Reset
          </button>
        </div>
      </div>
    </div>

    <!-- ── Table Card ── -->
    <div class="al-card">
      <!-- Toolbar -->
      <div class="al-toolbar">
        <div class="al-toolbar__left">
          <span class="al-count">
            <span class="al-count__num">{{ total }}</span>
            <span class="al-count__label">bản ghi</span>
          </span>
          <span v-if="loading" class="al-toolbar__loading">
            <span class="al-spinner"></span> Đang tải...
          </span>
        </div>
        <div class="al-toolbar__right">
          <span class="al-page-size-label">Hiển thị</span>
          <select v-model.number="size" class="al-size-select" @change="handleSizeChange(size)">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span class="al-page-size-label">/ trang</span>
        </div>
      </div>

      <!-- Table -->
      <div class="al-table-wrap" :class="{ 'al-table-wrap--loading': loading }">
        <div v-if="loading" class="al-loader-overlay">
          <div class="al-loader-ring"></div>
        </div>

        <table class="al-table">
          <thead>
            <tr>
              <th class="al-th-sort" style="width:80px" @click="triggerSort('id')">
                ID
                <span class="al-sort-icon" :class="getSortClass('id')">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="18 15 12 9 6 15"/></svg>
                </span>
              </th>
              <th style="min-width:180px">User</th>
              <th style="width:130px">Module</th>
              <th style="width:140px">Action</th>
              <th style="width:130px">Target Type</th>
              <th style="width:100px">Target ID</th>
              <th style="width:150px">IP Address</th>
              <th style="min-width:260px">Details</th>
              <th class="al-th-sort" style="width:180px" @click="triggerSort('createdAt')">
                Created At
                <span class="al-sort-icon" :class="getSortClass('createdAt')">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="18 15 12 9 6 15"/></svg>
                </span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && logs.length === 0">
              <td colspan="9" class="al-empty">
                <div class="al-empty__inner">
                  <div class="al-empty__icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                    </svg>
                  </div>
                  <p>Không tìm thấy bản ghi nào</p>
                  <button v-if="activeFilterCount > 0" class="al-clear-filter-btn" @click="resetFilter">Xóa bộ lọc</button>
                </div>
              </td>
            </tr>
            <tr v-for="row in logs" :key="row.id" class="al-row">
              <td><span class="al-mono-id">{{ row.id }}</span></td>
              <td>
                <div class="al-user-cell">
                  <span class="al-user-cell__name">{{ row.user?.username ?? '—' }}</span>
                  <span v-if="row.user?.id" class="al-user-cell__id al-mono">#{{ row.user.id }}</span>
                </div>
              </td>
              <td>
                <span class="al-tag al-tag--module" :class="getModuleClass(row.module)">{{ row.module ?? '—' }}</span>
              </td>
              <td>
                <span class="al-tag al-tag--action" :class="getActionClass(row.action)">{{ row.action ?? '—' }}</span>
              </td>
              <td><span class="al-text-cell al-mono">{{ row.targetType ?? '—' }}</span></td>
              <td><span class="al-mono-id">{{ row.targetId ?? '—' }}</span></td>
              <td><span class="al-ip al-mono">{{ row.ipAddress ?? '—' }}</span></td>
              <td>
                <span class="al-details" :title="row.detailsJson">{{ row.detailsJson ?? '—' }}</span>
              </td>
              <td><span class="al-date">{{ fmtDate(row.createdAt) }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="al-pagination" v-if="total > 0">
        <span class="al-pagination__info">
          {{ (page - 1) * size + 1 }}–{{ Math.min(page * size, total) }} / {{ total }} bản ghi
        </span>
        <div class="al-pagination__controls">
          <button class="al-page-btn" :disabled="page <= 1" @click="handlePageChange(page - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
          </button>

          <template v-for="p in pageButtons" :key="p">
            <span v-if="p === '...'" class="al-page-ellipsis">…</span>
            <button
              v-else
              class="al-page-btn"
              :class="{ 'al-page-btn--active': p === page }"
              @click="handlePageChange(p)"
            >{{ p }}</button>
          </template>

          <button class="al-page-btn" :disabled="page >= totalPages" @click="handlePageChange(page + 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import http from "../../api/http";
import { auditApi } from "../../api/audit.api";

/* =========================
   STATE
========================= */

const logs = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(5);
const loading = ref(false);

const sortBy = ref("createdAt");
const sortDir = ref("DESC");

const dateRange = ref([]);

const filter = reactive({
  userId: null,
  module: null,
  action: null,
  keyword: "",
  startDate: null,
  endDate: null,
});

/* =========================
   COMPUTED
========================= */

const totalPages = computed(() => Math.ceil(total.value / size.value) || 1);

const activeFilterCount = computed(() =>
  [filter.keyword, filter.userId, filter.module, filter.action, dateRange.value?.length].filter(Boolean).length
);

const pageButtons = computed(() => {
  const total = totalPages.value;
  const cur = page.value;
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
  const pages = [];
  pages.push(1);
  if (cur > 3) pages.push('...');
  for (let i = Math.max(2, cur - 1); i <= Math.min(total - 1, cur + 1); i++) pages.push(i);
  if (cur < total - 2) pages.push('...');
  pages.push(total);
  return pages;
});

/* =========================
   HELPERS
========================= */

function syncDateRange() {
  // vue v-model on array index requires a small nudge
  dateRange.value = [...dateRange.value];
}

function fmtDate(iso) {
  if (!iso) return '—';
  try { return new Date(iso).toLocaleString('vi-VN'); } catch { return iso; }
}

function getModuleClass(m) {
  return { USER: 'al-tag--blue', CUSTOMER: 'al-tag--green', ORDER: 'al-tag--orange', PRODUCT: 'al-tag--purple' }[m] || 'al-tag--gray';
}

function getActionClass(a) {
  return { CREATE: 'al-tag--green', UPDATE: 'al-tag--orange', DELETE: 'al-tag--red', CHANGE_ROLE: 'al-tag--purple' }[a] || 'al-tag--gray';
}

function getSortClass(col) {
  if (sortBy.value !== col) return 'al-sort-icon--none';
  return sortDir.value === 'ASC' ? 'al-sort-icon--asc' : 'al-sort-icon--desc';
}

function triggerSort(col) {
  if (sortBy.value === col) {
    handleSort({ prop: col, order: sortDir.value === 'ASC' ? 'descending' : 'ascending' });
  } else {
    handleSort({ prop: col, order: 'descending' });
  }
}

/* =========================
   API CALL
========================= */

async function fetchLogs() {
  loading.value = true;

  if (dateRange.value?.length === 2) {
    filter.startDate = dateRange.value[0];
    filter.endDate = dateRange.value[1];
  } else {
    filter.startDate = null;
    filter.endDate = null;
  }

  const payload = {
    userId: filter.userId || null,
    modules: filter.module ? [filter.module] : null,
    actions: filter.action ? [filter.action] : null,
    keyword: filter.keyword || null,
    startDate: filter.startDate || null,
    endDate: filter.endDate || null
  };

  try {
    const res = await http.post("/api/auth/audit-log/search", payload, {
      params: {
        page: page.value - 1,
        size: size.value,
        sortBy: sortBy.value,
        sortDir: sortDir.value,
      },
    });

    logs.value = res.data.content;
    total.value = res.data.totalElements;
  } finally {
    loading.value = false;
  }
}

function getWeekRange() {
  const now = new Date();
  const day = now.getDay() || 7;
  const monday = new Date(now);
  monday.setDate(now.getDate() - day + 1);
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  const format = (d) => d.toISOString().split("T")[0];
  return [format(monday), format(sunday)];
}

function loadThisWeek() {
  const [start, end] = getWeekRange();
  dateRange.value = [start, end];
  filter.startDate = start;
  filter.endDate = end;
  page.value = 1;
  fetchLogs();
}

/* =========================
   EVENTS
========================= */

function searchLogs() {
  page.value = 1;
  fetchLogs();
}

function resetFilter() {
  filter.userId = null;
  filter.module = null;
  filter.action = null;
  filter.keyword = "";
  dateRange.value = [];
  searchLogs();
}

function handlePageChange(p) {
  page.value = p;
  fetchLogs();
}

function handleSizeChange(s) {
  size.value = s;
  page.value = 1;
  fetchLogs();
}

function handleSort({ prop, order }) {
  if (!order) return;
  sortBy.value = prop;
  sortDir.value = order === "ascending" ? "ASC" : "DESC";
  fetchLogs();
}

/* =========================
   EXPORT CSV
========================= */

async function exportCsv() {
  try {
    if (dateRange.value?.length === 2) {
      filter.startDate = dateRange.value[0];
      filter.endDate = dateRange.value[1];
    }

    const payload = {
      userId: filter.userId || null,
      modules: filter.module ? [filter.module] : null,
      actions: filter.action ? [filter.action] : null,
      keyword: filter.keyword || null,
      startDate: filter.startDate || null,
      endDate: filter.endDate || null
    };

    const res = await auditApi.exportAdvanced(payload);

    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.download = "audit_logs.csv";
    link.click();
  } catch (err) {
    console.error("Export failed", err);
  }
}

/* =========================
   INIT
========================= */

onMounted(fetchLogs);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.al-page {
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
}

/* ── Header ──────────────────────────────── */
.al-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}
.al-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-muted);
  margin-bottom: 6px;
}
.al-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
  line-height: 1.2;
}
.al-title__accent { color: var(--c-blue); }
.al-subtitle { font-size: 13.5px; color: var(--c-muted); margin: 0; }
.al-header__right { flex-shrink: 0; padding-top: 6px; }

/* ── Filter Panel ────────────────────────── */
.al-filter-panel {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.al-filter-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--c-border-light);
}
.al-filter-panel__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-text);
}
.al-filter-badge {
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
}
.al-filter-clear {
  font-size: 12px;
  color: var(--c-muted);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: color 0.15s;
  font-family: "Plus Jakarta Sans", sans-serif;
}
.al-filter-clear:hover { color: var(--c-red); }

.al-filter-fields {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1.6fr auto;
  align-items: end;
  gap: 0;
}
.al-filter-field {
  padding: 12px 16px;
  border-right: 1px solid var(--c-border-light);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.al-filter-field--date { min-width: 0; }
.al-filter-field__label {
  font-size: 11px;
  font-weight: 700;
  color: var(--c-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
}
.al-filter-actions {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

/* Date range */
.al-date-range {
  display: flex;
  align-items: center;
  gap: 6px;
}
.al-date-range__sep { font-size: 12px; color: var(--c-subtle); flex-shrink: 0; }
.al-input--date { flex: 1; min-width: 0; padding: 8px 10px; font-size: 12.5px; }

/* ── Table Card ──────────────────────────── */
.al-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* ── Toolbar ─────────────────────────────── */
.al-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
  gap: 12px;
  flex-wrap: wrap;
}
.al-toolbar__left  { display: flex; align-items: center; gap: 12px; }
.al-toolbar__right { display: flex; align-items: center; gap: 8px; }
.al-toolbar__loading {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12.5px;
  color: var(--c-muted);
  font-weight: 600;
}
.al-count { display: flex; align-items: baseline; gap: 4px; }
.al-count__num   { font-size: 18px; font-weight: 800; }
.al-count__label { font-size: 12px; color: var(--c-subtle); font-weight: 600; }

.al-page-size-label { font-size: 12.5px; color: var(--c-muted); font-weight: 600; }
.al-size-select {
  padding: 5px 8px;
  font-size: 12.5px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-card);
  color: var(--c-text);
  cursor: pointer;
  outline: none;
}

/* ── Table ───────────────────────────────── */
.al-table-wrap {
  overflow-x: auto;
  position: relative;
  min-height: 200px;
}
.al-table-wrap--loading { pointer-events: none; }

.al-loader-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255,255,255,0.75);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.al-loader-ring {
  width: 36px; height: 36px;
  border: 3px solid var(--c-border);
  border-top-color: var(--c-blue);
  border-radius: 50%;
  animation: al-spin 0.7s linear infinite;
}

.al-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.al-table thead tr { border-bottom: 2px solid var(--c-border-light); }
.al-table th {
  padding: 11px 14px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--c-subtle);
  background: #fafbfc;
  white-space: nowrap;
  position: sticky;
  top: 0;
}
.al-th-sort {
  cursor: pointer;
  user-select: none;
  transition: color 0.15s;
}
.al-th-sort:hover { color: var(--c-text); }
.al-sort-icon {
  display: inline-flex;
  align-items: center;
  margin-left: 4px;
  opacity: 0.3;
  transition: opacity 0.15s, transform 0.15s;
}
.al-sort-icon--none  { opacity: 0.25; }
.al-sort-icon--asc   { opacity: 1; transform: rotate(0deg); color: var(--c-blue); }
.al-sort-icon--desc  { opacity: 1; transform: rotate(180deg); color: var(--c-blue); }

.al-table td {
  padding: 12px 14px;
  vertical-align: middle;
  border-bottom: 1px solid var(--c-border-light);
}
.al-row { transition: background 0.12s; }
.al-row:hover { background: #fafbfc; }
.al-row:last-child td { border-bottom: none; }

/* Cell types */
.al-mono-id {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-blue);
  background: var(--c-blue-bg);
  padding: 2px 7px;
  border-radius: 5px;
}
.al-mono { font-family: "JetBrains Mono", monospace; font-size: 12px; }

.al-user-cell { display: flex; flex-direction: column; gap: 2px; }
.al-user-cell__name { font-weight: 600; font-size: 13px; }
.al-user-cell__id { font-size: 10.5px; color: var(--c-subtle); }

.al-ip {
  font-size: 12px;
  color: var(--c-muted);
  white-space: nowrap;
}

.al-text-cell {
  font-size: 12.5px;
  color: var(--c-muted);
}

.al-details {
  display: block;
  font-family: "JetBrains Mono", monospace;
  font-size: 11.5px;
  color: var(--c-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}

.al-date {
  font-size: 12.5px;
  color: var(--c-muted);
  white-space: nowrap;
}

/* Tags */
.al-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.03em;
  border: 1px solid transparent;
  white-space: nowrap;
}
.al-tag--blue   { background: var(--c-blue-bg);   color: var(--c-blue);   border-color: var(--c-blue-border); }
.al-tag--green  { background: var(--c-green-bg);  color: var(--c-green);  border-color: var(--c-green-border); }
.al-tag--red    { background: var(--c-red-bg);    color: var(--c-red);    border-color: var(--c-red-border); }
.al-tag--orange { background: var(--c-orange-bg); color: var(--c-orange); border-color: var(--c-orange-border); }
.al-tag--purple { background: var(--c-purple-bg); color: var(--c-purple); border-color: var(--c-purple-border); }
.al-tag--gray   { background: var(--c-gray-bg);   color: var(--c-muted);  border-color: var(--c-gray-border); }

/* Empty state */
.al-empty {
  text-align: center;
  padding: 64px 20px !important;
}
.al-empty__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.al-empty__icon {
  width: 56px; height: 56px;
  border-radius: 50%;
  background: var(--c-border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-subtle);
}
.al-empty__inner p { color: var(--c-muted); font-size: 14px; margin: 0; }
.al-clear-filter-btn {
  padding: 7px 16px;
  font-size: 13px;
  font-weight: 700;
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--c-border);
  background: var(--c-card);
  cursor: pointer;
  color: var(--c-text);
  font-family: "Plus Jakarta Sans", sans-serif;
  transition: all 0.15s;
}
.al-clear-filter-btn:hover { background: var(--c-border-light); }

/* ── Pagination ──────────────────────────── */
.al-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid var(--c-border-light);
  background: #fafbfc;
  flex-wrap: wrap;
  gap: 10px;
}
.al-pagination__info {
  font-size: 12.5px;
  color: var(--c-muted);
  font-weight: 600;
}
.al-pagination__controls {
  display: flex;
  align-items: center;
  gap: 4px;
}
.al-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  font-size: 13px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--c-border);
  background: var(--c-card);
  color: var(--c-muted);
  cursor: pointer;
  transition: all 0.15s;
  font-family: "Plus Jakarta Sans", sans-serif;
}
.al-page-btn:hover:not(:disabled) {
  border-color: var(--c-blue);
  color: var(--c-blue);
}
.al-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.al-page-btn--active {
  background: var(--c-blue);
  border-color: var(--c-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,0.25);
}
.al-page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  font-size: 13px;
  color: var(--c-subtle);
}

/* ── Buttons ─────────────────────────────── */
.al-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  font-size: 13px;
  font-weight: 700;
  border-radius: var(--radius-sm);
  border: none;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
  white-space: nowrap;
}
.al-btn--primary {
  background: var(--c-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,0.22);
}
.al-btn--primary:hover { background: #1d4ed8; transform: translateY(-1px); box-shadow: 0 4px 14px rgba(37,99,235,0.3); }
.al-btn--orange {
  background: var(--c-orange);
  color: #fff;
  box-shadow: 0 2px 8px rgba(217,119,6,0.2);
}
.al-btn--orange:hover { background: #b45309; transform: translateY(-1px); }
.al-btn--ghost {
  background: transparent;
  color: var(--c-muted);
  border: 1.5px solid var(--c-border);
}
.al-btn--ghost:hover { background: var(--c-border-light); color: var(--c-text); }

/* ── Input ───────────────────────────────── */
.al-input {
  padding: 8px 12px;
  font-size: 13px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: 1.5px solid transparent;
  border-radius: 0;
  background: transparent;
  color: var(--c-text);
  outline: none;
  width: 100%;
  box-sizing: border-box;
  transition: border-color 0.15s;
}
.al-input:focus { border-color: var(--c-blue); background: #fff; border-radius: var(--radius-sm); }
.al-input::placeholder { color: var(--c-subtle); }
.al-select { cursor: pointer; }

/* ── Spinner ─────────────────────────────── */
.al-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.12);
  border-top-color: var(--c-muted);
  border-radius: 50%;
  animation: al-spin 0.6s linear infinite;
  flex-shrink: 0;
}
@keyframes al-spin { to { transform: rotate(360deg); } }

/* ── Responsive ──────────────────────────── */
@media (max-width: 1100px) {
  .al-filter-fields {
    grid-template-columns: 1fr 1fr 1fr;
  }
  .al-filter-field { border-bottom: 1px solid var(--c-border-light); }
  .al-filter-actions { grid-column: span 3; }
}
@media (max-width: 700px) {
  .al-page { padding: 20px 16px 40px; }
  .al-filter-fields { grid-template-columns: 1fr 1fr; }
  .al-filter-actions { grid-column: span 2; }
}
</style>