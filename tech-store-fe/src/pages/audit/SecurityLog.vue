<template>
  <div class="sl-page">

    <!-- ── Header ── -->
    <header class="sl-header">
      <div class="sl-header__left">
        <div class="sl-eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          Hệ thống
        </div>
        <h1 class="sl-title">Security <span class="sl-title__accent">Logs</span></h1>
        <p class="sl-subtitle">Lịch sử các sự kiện bảo mật và truy cập hệ thống</p>
      </div>
    </header>

    <!-- ── Filter Panel ── -->
    <div class="sl-filter-panel">
      <div class="sl-filter-panel__head">
        <div class="sl-filter-panel__title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
          </svg>
          Bộ lọc
          <span v-if="activeFilterCount > 0" class="sl-filter-badge">{{ activeFilterCount }} đang áp dụng</span>
        </div>
        <button v-if="activeFilterCount > 0" class="sl-filter-clear" @click="resetFilter">Xoá tất cả</button>
      </div>

      <div class="sl-filter-fields">
        <!-- Username -->
        <div class="sl-filter-field">
          <span class="sl-filter-field__label">Username</span>
          <input v-model="filter.username" class="sl-input" placeholder="VD: admin" />
        </div>

        <!-- IP Address -->
        <div class="sl-filter-field">
          <span class="sl-filter-field__label">IP Address</span>
          <input v-model="filter.ipAddress" class="sl-input" placeholder="VD: 192.168.1.1" />
        </div>

        <!-- Keyword -->
        <div class="sl-filter-field">
          <span class="sl-filter-field__label">Description</span>
          <input v-model="filter.keyword" class="sl-input" placeholder="Từ khoá..." />
        </div>

        <!-- Severity -->
        <div class="sl-filter-field">
          <span class="sl-filter-field__label">Severity</span>
          <select v-model="filter.severity" class="sl-input sl-select">
            <option value="">Tất cả severity</option>
            <option value="CRITICAL">CRITICAL</option>
            <option value="HIGH">HIGH</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="LOW">LOW</option>
          </select>
        </div>

        <!-- Status -->
        <div class="sl-filter-field">
          <span class="sl-filter-field__label">Status</span>
          <select v-model="filter.status" class="sl-input sl-select">
            <option value="">Tất cả status</option>
            <option value="SUCCESS">SUCCESS</option>
            <option value="FAILED">FAILED</option>
            <option value="BLOCKED">BLOCKED</option>
            <option value="401">401</option>
            <option value="403">403</option>
          </select>
        </div>

        <!-- Date range -->
        <div class="sl-filter-field sl-filter-field--date">
          <span class="sl-filter-field__label">Thời gian</span>
          <div class="sl-date-range">
            <input type="date" class="sl-input sl-input--date" v-model="dateRange[0]" @change="syncDateRange" />
            <span class="sl-date-range__sep">→</span>
            <input type="date" class="sl-input sl-input--date" v-model="dateRange[1]" @change="syncDateRange" />
          </div>
        </div>

        <!-- Actions -->
        <div class="sl-filter-actions">
          <button class="sl-btn sl-btn--primary" @click="searchLogs">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
            Tìm kiếm
          </button>
          <button class="sl-btn sl-btn--ghost" @click="resetFilter">
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
    <div class="sl-card">
      <!-- Toolbar -->
      <div class="sl-toolbar">
        <div class="sl-toolbar__left">
          <span class="sl-count">
            <span class="sl-count__num">{{ total }}</span>
            <span class="sl-count__label">bản ghi</span>
          </span>
          <span v-if="loading" class="sl-toolbar__loading">
            <span class="sl-spinner"></span> Đang tải...
          </span>
        </div>
        <div class="sl-toolbar__right">
          <span class="sl-size-label">Hiển thị</span>
          <select v-model.number="size" class="sl-size-select" @change="handleSizeChange(size)">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span class="sl-size-label">/ trang</span>
        </div>
      </div>

      <!-- Table -->
      <div class="sl-table-wrap" :class="{ 'sl-table-wrap--loading': loading }">
        <div v-if="loading" class="sl-loader-overlay">
          <div class="sl-loader-ring"></div>
        </div>

        <table class="sl-table">
          <thead>
            <tr>
              <th class="sl-th-sort" style="width:76px" @click="triggerSort('id')">
                ID
                <span class="sl-sort-icon" :class="getSortClass('id')">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="18 15 12 9 6 15"/></svg>
                </span>
              </th>
              <th style="width:150px">Username</th>
              <th style="min-width:180px">Action</th>
              <th style="width:140px">Target</th>
              <th style="width:110px">Target ID</th>
              <th style="width:150px">IP Address</th>
              <th style="width:120px">Severity</th>
              <th style="width:110px">Status</th>
              <th style="min-width:220px">Description</th>
              <th class="sl-th-sort" style="width:180px" @click="triggerSort('createdAt')">
                Created At
                <span class="sl-sort-icon" :class="getSortClass('createdAt')">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="18 15 12 9 6 15"/></svg>
                </span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && logs.length === 0">
              <td colspan="10" class="sl-empty">
                <div class="sl-empty__inner">
                  <div class="sl-empty__icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    </svg>
                  </div>
                  <p>Không tìm thấy bản ghi nào</p>
                  <button v-if="activeFilterCount > 0" class="sl-clear-filter-btn" @click="resetFilter">Xóa bộ lọc</button>
                </div>
              </td>
            </tr>
            <tr
              v-for="row in logs"
              :key="row.id"
              class="sl-row"
              :class="{ 'sl-row--critical': row.severity === 'CRITICAL', 'sl-row--high': row.severity === 'HIGH' }"
            >
              <td><span class="sl-mono-id">{{ row.id }}</span></td>
              <td>
                <div class="sl-user-cell">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="sl-user-cell__icon">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                  </svg>
                  <span class="sl-user-cell__name">{{ row.username ?? '—' }}</span>
                </div>
              </td>
              <td><span class="sl-action-text">{{ row.actionType ?? '—' }}</span></td>
              <td><span class="sl-mono sl-text--muted">{{ row.targetEntity ?? '—' }}</span></td>
              <td><span class="sl-mono-id">{{ row.targetId ?? '—' }}</span></td>
              <td><span class="sl-ip sl-mono">{{ row.ipAddress ?? '—' }}</span></td>
              <td>
                <span class="sl-severity-tag" :class="getSeverityClass(row.severity)">
                  <span class="sl-severity-tag__dot"></span>
                  {{ row.severity ?? '—' }}
                </span>
              </td>
              <td>
                <span class="sl-tag" :class="getStatusClass(row.status)">{{ row.status ?? '—' }}</span>
              </td>
              <td>
                <span class="sl-desc" :title="row.description">{{ row.description ?? '—' }}</span>
              </td>
              <td><span class="sl-date">{{ fmtDate(row.createdAt) }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="sl-pagination" v-if="total > 0">
        <span class="sl-pagination__info">
          {{ (page - 1) * size + 1 }}–{{ Math.min(page * size, total) }} / {{ total }} bản ghi
        </span>
        <div class="sl-pagination__controls">
          <button class="sl-page-btn" :disabled="page <= 1" @click="handlePageChange(page - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
          </button>
          <template v-for="p in pageButtons" :key="p">
            <span v-if="p === '...'" class="sl-page-ellipsis">…</span>
            <button v-else class="sl-page-btn" :class="{ 'sl-page-btn--active': p === page }" @click="handlePageChange(p)">{{ p }}</button>
          </template>
          <button class="sl-page-btn" :disabled="page >= totalPages" @click="handlePageChange(page + 1)">
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

/* Colors for severity */
function severityTagType(severity) {
  switch (severity) {
    case "CRITICAL": return "danger";
    case "HIGH":     return "warning";
    case "MEDIUM":   return "info";
    case "LOW":      return "success";
    default:         return "info";
  }
}

/* Colors for status */
function statusTagType(status) {
  switch (status) {
    case 200:
    case "SUCCESS":  return "success";
    case 401:
    case 403:
    case "FAILED":   return "danger";
    default:         return "info";
  }
}

/* ================= STATE ================= */

const logs    = ref([]);
const total   = ref(0);
const page    = ref(1);
const size    = ref(5);
const loading = ref(false);

const sortBy  = ref("createdAt");
const sortDir = ref("DESC");

const dateRange = ref([]);

const filter = reactive({
  username:  null,
  ipAddress: null,
  keyword:   null,
  severity:  null,
  status:    null,
  startDate: null,
  endDate:   null,
});

/* ================= COMPUTED ================= */

const totalPages = computed(() => Math.ceil(total.value / size.value) || 1);

const activeFilterCount = computed(() =>
  [filter.username, filter.ipAddress, filter.keyword, filter.severity, filter.status, dateRange.value?.length].filter(Boolean).length
);

const pageButtons = computed(() => {
  const tot = totalPages.value;
  const cur = page.value;
  if (tot <= 7) return Array.from({ length: tot }, (_, i) => i + 1);
  const pages = [1];
  if (cur > 3) pages.push('...');
  for (let i = Math.max(2, cur - 1); i <= Math.min(tot - 1, cur + 1); i++) pages.push(i);
  if (cur < tot - 2) pages.push('...');
  pages.push(tot);
  return pages;
});

/* ================= HELPERS ================= */

function syncDateRange() {
  dateRange.value = [...dateRange.value];
}

function fmtDate(iso) {
  if (!iso) return '—';
  try { return new Date(iso).toLocaleString('vi-VN'); } catch { return iso; }
}

function getSeverityClass(s) {
  return { CRITICAL: 'sl-severity--critical', HIGH: 'sl-severity--high', MEDIUM: 'sl-severity--medium', LOW: 'sl-severity--low' }[s] || 'sl-severity--low';
}

function getStatusClass(s) {
  if (s === 'SUCCESS' || s === 200)            return 'sl-tag--green';
  if (s === 'FAILED' || s === 401 || s === 403) return 'sl-tag--red';
  if (s === 'BLOCKED')                          return 'sl-tag--orange';
  return 'sl-tag--gray';
}

function getSortClass(col) {
  if (sortBy.value !== col) return 'sl-sort-icon--none';
  return sortDir.value === 'ASC' ? 'sl-sort-icon--asc' : 'sl-sort-icon--desc';
}

function triggerSort(col) {
  handleSort({ prop: col, order: sortBy.value === col && sortDir.value === 'ASC' ? 'descending' : 'ascending' });
}

/* ================= BUILD PAYLOAD ================= */

function buildPayload() {
  if (dateRange.value?.length === 2) {
    filter.startDate = dateRange.value[0] + "T00:00:00";
    filter.endDate   = dateRange.value[1] + "T23:59:59";
  } else {
    filter.startDate = null;
    filter.endDate   = null;
  }

  return {
    username:   filter.username  || null,
    ipAddress:  filter.ipAddress || null,
    keyword:    filter.keyword   || null,
    severities: filter.severity  ? [filter.severity] : null,
    statuses:   filter.status    ? [filter.status]   : null,
    startDate:  filter.startDate,
    endDate:    filter.endDate,
  };
}

/* ================= FETCH ================= */

async function fetchLogs() {
  loading.value = true;
  try {
    const payload = buildPayload();
    const res = await http.post("/api/auth/security-log/search", payload, {
      params: {
        page:    page.value - 1,
        size:    size.value,
        sortBy:  sortBy.value,
        sortDir: sortDir.value,
      },
    });
    logs.value  = res.data.content;
    total.value = res.data.totalElements;
  } catch (err) {
    console.error("Load failed:", err.response?.data || err);
  } finally {
    loading.value = false;
  }
}

/* ================= EVENTS ================= */

function searchLogs() { page.value = 1; fetchLogs(); }

function resetFilter() {
  filter.username  = null;
  filter.ipAddress = null;
  filter.keyword   = null;
  filter.severity  = null;
  filter.status    = null;
  dateRange.value  = [];
  page.value = 1;
  fetchLogs();
}

function handlePageChange(p) { page.value = p; fetchLogs(); }
function handleSizeChange(s) { size.value = s; page.value = 1; fetchLogs(); }

function handleSort({ prop, order }) {
  if (!order) return;
  sortBy.value  = prop;
  sortDir.value = order === "ascending" ? "ASC" : "DESC";
  fetchLogs();
}

/* ================= INIT ================= */

onMounted(fetchLogs);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.sl-page {
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
.sl-eyebrow {
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
.sl-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
  line-height: 1.2;
}
.sl-title__accent { color: var(--c-red); }
.sl-subtitle { font-size: 13.5px; color: var(--c-muted); margin: 0; }

/* ── Filter Panel ────────────────────────── */
.sl-filter-panel {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.sl-filter-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--c-border-light);
}
.sl-filter-panel__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-text);
}
.sl-filter-badge {
  background: var(--c-blue-bg);
  color: var(--c-blue);
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
}
.sl-filter-clear {
  font-size: 12px;
  color: var(--c-muted);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: color 0.15s;
  font-family: "Plus Jakarta Sans", sans-serif;
}
.sl-filter-clear:hover { color: var(--c-red); }

.sl-filter-fields {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1.5fr auto;
  align-items: end;
}
.sl-filter-field {
  padding: 12px 16px;
  border-right: 1px solid var(--c-border-light);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.sl-filter-field__label {
  font-size: 11px;
  font-weight: 700;
  color: var(--c-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
}
.sl-filter-actions {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.sl-date-range {
  display: flex;
  align-items: center;
  gap: 5px;
}
.sl-date-range__sep { font-size: 12px; color: var(--c-subtle); flex-shrink: 0; }
.sl-input--date { flex: 1; min-width: 0; padding: 8px 8px; font-size: 12px; }

/* ── Card ────────────────────────────────── */
.sl-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* ── Toolbar ─────────────────────────────── */
.sl-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
  gap: 12px;
  flex-wrap: wrap;
}
.sl-toolbar__left  { display: flex; align-items: center; gap: 12px; }
.sl-toolbar__right { display: flex; align-items: center; gap: 8px; }
.sl-toolbar__loading {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12.5px;
  color: var(--c-muted);
  font-weight: 600;
}
.sl-count { display: flex; align-items: baseline; gap: 4px; }
.sl-count__num   { font-size: 18px; font-weight: 800; }
.sl-count__label { font-size: 12px; color: var(--c-subtle); font-weight: 600; }
.sl-size-label   { font-size: 12.5px; color: var(--c-muted); font-weight: 600; }
.sl-size-select {
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
.sl-table-wrap {
  overflow-x: auto;
  position: relative;
  min-height: 200px;
}
.sl-table-wrap--loading { pointer-events: none; }

.sl-loader-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255,255,255,0.75);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.sl-loader-ring {
  width: 36px; height: 36px;
  border: 3px solid var(--c-border);
  border-top-color: var(--c-blue);
  border-radius: 50%;
  animation: sl-spin 0.7s linear infinite;
}

.sl-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.sl-table thead tr { border-bottom: 2px solid var(--c-border-light); }
.sl-table th {
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
.sl-th-sort {
  cursor: pointer;
  user-select: none;
  transition: color 0.15s;
}
.sl-th-sort:hover { color: var(--c-text); }
.sl-sort-icon {
  display: inline-flex;
  align-items: center;
  margin-left: 4px;
  transition: opacity 0.15s, transform 0.15s;
}
.sl-sort-icon--none { opacity: 0.25; }
.sl-sort-icon--asc  { opacity: 1; transform: rotate(0deg);   color: var(--c-blue); }
.sl-sort-icon--desc { opacity: 1; transform: rotate(180deg); color: var(--c-blue); }

.sl-table td {
  padding: 12px 14px;
  vertical-align: middle;
  border-bottom: 1px solid var(--c-border-light);
}
.sl-row { transition: background 0.12s; }
.sl-row:hover { background: #fafbfc; }
.sl-row:last-child td { border-bottom: none; }

/* Critical / High row highlight */
.sl-row--critical { background: #fff5f5 !important; }
.sl-row--critical:hover { background: #ffecec !important; }
.sl-row--high { background: #fffdf0 !important; }
.sl-row--high:hover { background: #fffae0 !important; }

/* ── Cell types ──────────────────────────── */
.sl-mono-id {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-blue);
  background: var(--c-blue-bg);
  padding: 2px 7px;
  border-radius: 5px;
}
.sl-mono       { font-family: "JetBrains Mono", monospace; font-size: 12px; }
.sl-text--muted { color: var(--c-muted); }

.sl-user-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.sl-user-cell__icon { color: var(--c-subtle); flex-shrink: 0; }
.sl-user-cell__name { font-weight: 600; font-size: 13px; }

.sl-action-text {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--c-text);
}

.sl-ip {
  font-size: 12px;
  color: var(--c-muted);
  white-space: nowrap;
}

.sl-desc {
  display: block;
  font-size: 12.5px;
  color: var(--c-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 260px;
}

.sl-date {
  font-size: 12.5px;
  color: var(--c-muted);
  white-space: nowrap;
}

/* ── Severity tag ────────────────────────── */
.sl-severity-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  border: 1px solid transparent;
  white-space: nowrap;
}
.sl-severity-tag__dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
.sl-severity--critical {
  background: #450a0a; color: #fecaca; border-color: #991b1b;
}
.sl-severity--critical .sl-severity-tag__dot { background: #f87171; box-shadow: 0 0 4px #f87171; }
.sl-severity--high {
  background: var(--c-orange-bg); color: #92400e; border-color: var(--c-orange-border);
}
.sl-severity--high .sl-severity-tag__dot { background: var(--c-orange); }
.sl-severity--medium {
  background: var(--c-blue-bg); color: var(--c-blue); border-color: var(--c-blue-border);
}
.sl-severity--medium .sl-severity-tag__dot { background: var(--c-blue); }
.sl-severity--low {
  background: var(--c-green-bg); color: var(--c-green); border-color: var(--c-green-border);
}
.sl-severity--low .sl-severity-tag__dot { background: var(--c-green); }

/* ── Status tag ──────────────────────────── */
.sl-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  border: 1px solid transparent;
  white-space: nowrap;
}
.sl-tag--green  { background: var(--c-green-bg);  color: var(--c-green);  border-color: var(--c-green-border); }
.sl-tag--red    { background: var(--c-red-bg);    color: var(--c-red);    border-color: var(--c-red-border); }
.sl-tag--orange { background: var(--c-orange-bg); color: var(--c-orange); border-color: var(--c-orange-border); }
.sl-tag--gray   { background: var(--c-gray-bg);   color: var(--c-muted);  border-color: var(--c-gray-border); }

/* ── Empty state ─────────────────────────── */
.sl-empty { text-align: center; padding: 64px 20px !important; }
.sl-empty__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.sl-empty__icon {
  width: 56px; height: 56px;
  border-radius: 50%;
  background: var(--c-border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-subtle);
}
.sl-empty__inner p { color: var(--c-muted); font-size: 14px; margin: 0; }
.sl-clear-filter-btn {
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
.sl-clear-filter-btn:hover { background: var(--c-border-light); }

/* ── Pagination ──────────────────────────── */
.sl-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid var(--c-border-light);
  background: #fafbfc;
  flex-wrap: wrap;
  gap: 10px;
}
.sl-pagination__info { font-size: 12.5px; color: var(--c-muted); font-weight: 600; }
.sl-pagination__controls { display: flex; align-items: center; gap: 4px; }
.sl-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px; height: 32px;
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
.sl-page-btn:hover:not(:disabled) { border-color: var(--c-blue); color: var(--c-blue); }
.sl-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.sl-page-btn--active {
  background: var(--c-blue);
  border-color: var(--c-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,0.25);
}
.sl-page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px; height: 32px;
  font-size: 13px;
  color: var(--c-subtle);
}

/* ── Buttons ─────────────────────────────── */
.sl-btn {
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
.sl-btn--primary {
  background: var(--c-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,0.22);
}
.sl-btn--primary:hover { background: #1d4ed8; transform: translateY(-1px); }
.sl-btn--ghost {
  background: transparent;
  color: var(--c-muted);
  border: 1.5px solid var(--c-border);
}
.sl-btn--ghost:hover { background: var(--c-border-light); color: var(--c-text); }

/* ── Input ───────────────────────────────── */
.sl-input {
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
.sl-input:focus { border-color: var(--c-blue); background: #fff; border-radius: var(--radius-sm); }
.sl-input::placeholder { color: var(--c-subtle); }
.sl-select { cursor: pointer; }

/* ── Spinner ─────────────────────────────── */
.sl-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.12);
  border-top-color: var(--c-muted);
  border-radius: 50%;
  animation: sl-spin 0.6s linear infinite;
  flex-shrink: 0;
}
@keyframes sl-spin { to { transform: rotate(360deg); } }

/* ── Responsive ──────────────────────────── */
@media (max-width: 1200px) {
  .sl-filter-fields { grid-template-columns: 1fr 1fr 1fr 1fr; }
  .sl-filter-field { border-bottom: 1px solid var(--c-border-light); }
  .sl-filter-actions { grid-column: span 4; }
}
@media (max-width: 800px) {
  .sl-page { padding: 20px 16px 40px; }
  .sl-filter-fields { grid-template-columns: 1fr 1fr; }
  .sl-filter-actions { grid-column: span 2; }
}
</style>