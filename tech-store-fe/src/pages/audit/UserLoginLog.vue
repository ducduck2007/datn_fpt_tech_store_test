```vue
<template>
  <div class="al-page">
    <!-- HEADER -->
    <header class="al-header">
      <div>
        <div class="al-eyebrow">Hệ thống</div>
        <h1 class="al-title">
          User Login <span class="al-title__accent">Logs</span>
        </h1>
        <p class="al-subtitle">Theo dõi hoạt động đăng nhập hệ thống</p>
      </div>

      <button class="al-btn al-btn--primary" @click="exportCsv">
        Export CSV
      </button>
    </header>

    <!-- FILTER -->
    <div class="al-filter-panel">
      <div class="al-filter-panel__head">
        <div class="al-filter-panel__title">
          Bộ lọc
          <span v-if="activeFilterCount > 0" class="al-filter-badge">
            {{ activeFilterCount }} đang áp dụng
          </span>
        </div>

        <button
          v-if="activeFilterCount > 0"
          class="al-filter-clear"
          @click="resetFilter"
        >
          Xoá tất cả
        </button>
      </div>

      <div class="al-filter-fields">
        <div class="al-filter-field">
          <span class="al-filter-field__label">Username</span>
          <input v-model="filter.username" class="al-input" placeholder="VD: admin"/>
        </div>

        <div class="al-filter-field">
          <span class="al-filter-field__label">IP Address</span>
          <input
            v-model="filter.ipAddress"
            class="al-input"
            placeholder="192.168..."
          />
        </div>

        <div class="al-filter-field">
          <span class="al-filter-field__label">Status</span>
          <select v-model="filter.success" class="al-input">
            <option value="">All</option>
            <option :value="true">SUCCESS</option>
            <option :value="false">FAILED</option>
          </select>
        </div>

        <div class="al-filter-field">
          <span class="al-filter-field__label">From</span>
          <input type="date" v-model="filter.fromDate" class="al-input" />
        </div>

        <div class="al-filter-field">
          <span class="al-filter-field__label">To</span>
          <input type="date" v-model="filter.toDate" class="al-input" />
        </div>

        <div class="al-filter-actions">
          <button class="al-btn al-btn--primary" @click="searchLogs">
            Tìm kiếm
          </button>

          <button class="al-btn al-btn--ghost" @click="resetFilter">
            Reset
          </button>
        </div>
      </div>
    </div>

    <!-- TABLE -->
    <div class="al-card">
      <!-- TOOLBAR -->
      <div class="al-toolbar">
        <div class="al-toolbar__left">
          <span class="al-count">
            <span class="al-count__num">{{ total }}</span>
            <span class="al-count__label">bản ghi</span>
          </span>

          <span v-if="loading" class="al-toolbar__loading"> Đang tải... </span>
        </div>

        <div class="al-toolbar__right">
          <span>Hiển thị</span>
          <select
            v-model.number="size"
            class="al-size-select"
            @change="handleSizeChange(size)"
          >
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select>
          <span>/ trang</span>
        </div>
      </div>

      <!-- TABLE -->
      <div class="al-table-wrap">
        <table class="al-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>User</th>
              <th>Status</th>
              <th>IP</th>
              <th>User Agent</th>
              <th>Login Time</th>
            </tr>
          </thead>

          <tbody>
            <tr v-if="logs.length === 0 && !loading">
              <td colspan="6" class="al-empty">Không có dữ liệu</td>
            </tr>

            <tr v-for="log in logs" :key="log.id">
              <td>
                <span class="al-mono-id">{{ log.id }}</span>
              </td>

              <td>
                <div class="al-user-cell">
                  <span>{{ log.username }}</span>
                  <small>#{{ log.userId }}</small>
                </div>
              </td>

              <td>
                <span
                  class="al-tag"
                  :class="log.success ? 'al-tag--green' : 'al-tag--red'"
                >
                  {{ log.success ? "SUCCESS" : "FAILED" }}
                </span>
              </td>

              <td class="al-mono">
                {{ log.ipAddress }}
              </td>

              <td class="al-details">
                {{ log.userAgent }}
              </td>

              <td class="al-date">
                {{ fmtDate(log.createdAt) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="al-pagination" v-if="total > 0">
        <span class="al-pagination__info">
          {{ (page - 1) * size + 1 }}–{{ Math.min(page * size, total) }} /
          {{ total }} bản ghi
        </span>
        <div class="al-pagination__controls">
          <button
            class="al-page-btn"
            :disabled="page <= 1"
            @click="handlePageChange(page - 1)"
          >
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
            >
              <polyline points="15 18 9 12 15 6" />
            </svg>
          </button>

          <template v-for="p in pageButtons" :key="p">
            <span v-if="p === '...'" class="al-page-ellipsis">…</span>
            <button
              v-else
              class="al-page-btn"
              :class="{ 'al-page-btn--active': p === page }"
              @click="handlePageChange(p)"
            >
              {{ p }}
            </button>
          </template>

          <button
            class="al-page-btn"
            :disabled="page >= totalPages"
            @click="handlePageChange(page + 1)"
          >
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
            >
              <polyline points="9 18 15 12 9 6" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { userLoginApi } from "../../api/userLogin.api";

/* ===============================
   STATE
================================ */
const logs = ref([]);
const loading = ref(false);

const page = ref(1);
const size = ref(10);
const total = ref(0);
const totalPages = ref(1);

const filter = ref({
  username: "",
  success: "",
  ipAddress: "",
  fromDate: "",
  toDate: "",
});

/* ===============================
   LOAD DATA
================================ */
async function loadLogs() {
  loading.value = true;

  try {
    const res = await userLoginApi.filter(
      filter.value,
      page.value - 1,
      size.value,
    );

    logs.value = res.data.content;
    total.value = res.data.totalElements;
    totalPages.value = res.data.totalPages;
  } finally {
    loading.value = false;
  }
}

onMounted(loadLogs);

/* ===============================
   SEARCH
================================ */
function searchLogs() {
  page.value = 1;
  loadLogs();
}

function resetFilter() {
  filter.value = {
    username: "",
    success: "",
    ipAddress: "",
    fromDate: "",
    toDate: "",
  };
  searchLogs();
}

/* ===============================
   PAGINATION
================================ */
function handlePageChange(p) {
  if (p < 1 || p > totalPages.value) return;
  page.value = p;
  loadLogs();
}

function handleSizeChange(val) {
  size.value = val;
  page.value = 1;
  loadLogs();
}

const pageButtons = computed(() => {
  const pages = [];
  for (let i = 1; i <= totalPages.value; i++) pages.push(i);
  return pages;
});

/* ===============================
   EXPORT CSV
================================ */
async function exportCsv() {
  const res = await userLoginApi.exportCsv(filter.value);

  const url = window.URL.createObjectURL(new Blob([res.data]));

  const a = document.createElement("a");
  a.href = url;
  a.download = "user_login_logs.csv";
  a.click();
}

/* ===============================
   HELPERS
================================ */
function fmtDate(date) {
  if (!date) return "";
  return new Date(date).toLocaleString();
}

const activeFilterCount = computed(
  () => Object.values(filter.value).filter((v) => v !== "").length,
);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.al-page {
  --c-bg: #f6f7f9;
  --c-card: #ffffff;
  --c-border: #e4e7ec;
  --c-border-light: #f0f2f5;
  --c-text: #0f1117;
  --c-muted: #6b7280;
  --c-subtle: #9ca3af;
  --c-blue: #2563eb;
  --c-blue-bg: #eff6ff;
  --c-blue-border: #bfdbfe;
  --c-green: #16a34a;
  --c-green-bg: #f0fdf4;
  --c-green-border: #bbf7d0;
  --c-red: #dc2626;
  --c-red-bg: #fff1f2;
  --c-red-border: #fecdd3;
  --c-orange: #d97706;
  --c-orange-bg: #fffbeb;
  --c-orange-border: #fde68a;
  --c-purple: #7c3aed;
  --c-purple-bg: #f5f3ff;
  --c-purple-border: #ddd6fe;
  --c-gray-bg: #f3f4f6;
  --c-gray-border: #e5e7eb;
  --radius: 12px;
  --radius-sm: 8px;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08), 0 2px 6px rgba(0, 0, 0, 0.04);

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
.al-title__accent {
  color: var(--c-blue);
}
.al-subtitle {
  font-size: 13.5px;
  color: var(--c-muted);
  margin: 0;
}
.al-header__right {
  flex-shrink: 0;
  padding-top: 6px;
}

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
.al-filter-clear:hover {
  color: var(--c-red);
}

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
.al-filter-field--date {
  min-width: 0;
}
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
.al-date-range__sep {
  font-size: 12px;
  color: var(--c-subtle);
  flex-shrink: 0;
}
.al-input--date {
  flex: 1;
  min-width: 0;
  padding: 8px 10px;
  font-size: 12.5px;
}

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
.al-toolbar__left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.al-toolbar__right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.al-toolbar__loading {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12.5px;
  color: var(--c-muted);
  font-weight: 600;
}
.al-count {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.al-count__num {
  font-size: 18px;
  font-weight: 800;
}
.al-count__label {
  font-size: 12px;
  color: var(--c-subtle);
  font-weight: 600;
}

.al-page-size-label {
  font-size: 12.5px;
  color: var(--c-muted);
  font-weight: 600;
}
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
.al-table-wrap--loading {
  pointer-events: none;
}

.al-loader-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.al-loader-ring {
  width: 36px;
  height: 36px;
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
.al-table thead tr {
  border-bottom: 2px solid var(--c-border-light);
}
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
.al-th-sort:hover {
  color: var(--c-text);
}
.al-sort-icon {
  display: inline-flex;
  align-items: center;
  margin-left: 4px;
  opacity: 0.3;
  transition:
    opacity 0.15s,
    transform 0.15s;
}
.al-sort-icon--none {
  opacity: 0.25;
}
.al-sort-icon--asc {
  opacity: 1;
  transform: rotate(0deg);
  color: var(--c-blue);
}
.al-sort-icon--desc {
  opacity: 1;
  transform: rotate(180deg);
  color: var(--c-blue);
}

.al-table td {
  padding: 12px 14px;
  vertical-align: middle;
  border-bottom: 1px solid var(--c-border-light);
}
.al-row {
  transition: background 0.12s;
}
.al-row:hover {
  background: #fafbfc;
}
.al-row:last-child td {
  border-bottom: none;
}

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
.al-mono {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
}

.al-user-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.al-user-cell__name {
  font-weight: 600;
  font-size: 13px;
}
.al-user-cell__id {
  font-size: 10.5px;
  color: var(--c-subtle);
}

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
.al-tag--blue {
  background: var(--c-blue-bg);
  color: var(--c-blue);
  border-color: var(--c-blue-border);
}
.al-tag--green {
  background: var(--c-green-bg);
  color: var(--c-green);
  border-color: var(--c-green-border);
}
.al-tag--red {
  background: var(--c-red-bg);
  color: var(--c-red);
  border-color: var(--c-red-border);
}
.al-tag--orange {
  background: var(--c-orange-bg);
  color: var(--c-orange);
  border-color: var(--c-orange-border);
}
.al-tag--purple {
  background: var(--c-purple-bg);
  color: var(--c-purple);
  border-color: var(--c-purple-border);
}
.al-tag--gray {
  background: var(--c-gray-bg);
  color: var(--c-muted);
  border-color: var(--c-gray-border);
}

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
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--c-border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-subtle);
}
.al-empty__inner p {
  color: var(--c-muted);
  font-size: 14px;
  margin: 0;
}
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
.al-clear-filter-btn:hover {
  background: var(--c-border-light);
}

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
.al-page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.al-page-btn--active {
  background: var(--c-blue);
  border-color: var(--c-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
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
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.22);
}
.al-btn--primary:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.3);
}
.al-btn--orange {
  background: var(--c-orange);
  color: #fff;
  box-shadow: 0 2px 8px rgba(217, 119, 6, 0.2);
}
.al-btn--orange:hover {
  background: #b45309;
  transform: translateY(-1px);
}
.al-btn--ghost {
  background: transparent;
  color: var(--c-muted);
  border: 1.5px solid var(--c-border);
}
.al-btn--ghost:hover {
  background: var(--c-border-light);
  color: var(--c-text);
}

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
.al-input:focus {
  border-color: var(--c-blue);
  background: #fff;
  border-radius: var(--radius-sm);
}
.al-input::placeholder {
  color: var(--c-subtle);
}
.al-select {
  cursor: pointer;
}

/* ── Spinner ─────────────────────────────── */
.al-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(0, 0, 0, 0.12);
  border-top-color: var(--c-muted);
  border-radius: 50%;
  animation: al-spin 0.6s linear infinite;
  flex-shrink: 0;
}
@keyframes al-spin {
  to {
    transform: rotate(360deg);
  }
}

/* ── Responsive ──────────────────────────── */
@media (max-width: 1100px) {
  .al-filter-fields {
    grid-template-columns: 1fr 1fr 1fr;
  }
  .al-filter-field {
    border-bottom: 1px solid var(--c-border-light);
  }
  .al-filter-actions {
    grid-column: span 3;
  }
}
@media (max-width: 700px) {
  .al-page {
    padding: 20px 16px 40px;
  }
  .al-filter-fields {
    grid-template-columns: 1fr 1fr;
  }
  .al-filter-actions {
    grid-column: span 2;
  }
}
</style>
