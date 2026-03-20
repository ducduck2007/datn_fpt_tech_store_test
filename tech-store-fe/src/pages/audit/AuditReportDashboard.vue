<template>
  <div class="ar2-page">

    <!-- ── Header ── -->
    <header class="ar2-header">
      <div class="ar2-eyebrow">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="18" y1="20" x2="18" y2="10"/>
          <line x1="12" y1="20" x2="12" y2="4"/>
          <line x1="6" y1="20" x2="6" y2="14"/>
        </svg>
        Hệ thống
      </div>
      <h1 class="ar2-title">Audit <span class="ar2-title__accent">Report</span></h1>
      <p class="ar2-subtitle">Tổng hợp thống kê hoạt động hệ thống theo module và người dùng</p>
    </header>

    <!-- ── Summary Stats ── -->
    <div class="ar2-card ar2-mb">
      <div class="ar2-card__head">
        <span class="ar2-card__title">Summary Report</span>
      </div>
      <div class="ar2-card__body">
        <!-- Row 1: 4 stats -->
        <div class="ar2-stats4">
          <div class="ar2-stat ar2-stat--blue">
            <div class="ar2-stat__icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="ar2-stat__num">{{ summary.totalLogs ?? '—' }}</div>
            <div class="ar2-stat__label">Total Logs</div>
          </div>

          <div class="ar2-stat ar2-stat--green">
            <div class="ar2-stat__icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <div class="ar2-stat__num">{{ summary.totalActiveUsers ?? '—' }}</div>
            <div class="ar2-stat__label">Active Users</div>
          </div>

          <div class="ar2-stat ar2-stat--red">
            <div class="ar2-stat__icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
            <div class="ar2-stat__num">{{ summary.totalErrors ?? '—' }}</div>
            <div class="ar2-stat__label">Errors</div>
          </div>

          <div class="ar2-stat ar2-stat--orange">
            <div class="ar2-stat__icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
            </div>
            <div class="ar2-stat__num">{{ summary.todayLogs ?? '—' }}</div>
            <div class="ar2-stat__label">Today Logs</div>
          </div>
        </div>

        <!-- Row 2: top cards -->
        <div class="ar2-top-row">
          <div class="ar2-top-card">
            <div class="ar2-top-card__label">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              Top Module
            </div>
            <div class="ar2-top-card__value">
              <span v-if="summary.topModule" class="ar2-tag ar2-tag--blue">{{ summary.topModule }}</span>
              <span v-else class="ar2-text--muted">—</span>
            </div>
          </div>

          <div class="ar2-top-card">
            <div class="ar2-top-card__label">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              Top User
            </div>
            <div class="ar2-top-card__value">
              <span v-if="summary.topUserId" class="ar2-mono-id">{{ summary.topUserId }}</span>
              <span v-else class="ar2-text--muted">—</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Module & User Reports ── -->
    <div class="ar2-grid2">

      <!-- Module Report -->
      <div class="ar2-card">
        <div class="ar2-card__head">
          <span class="ar2-card__title">Report by Module</span>
          <span v-if="moduleReport.length" class="ar2-count-badge">{{ moduleReport.length }} module</span>
        </div>
        <div class="ar2-table-wrap">
          <table class="ar2-table">
            <thead>
              <tr>
                <th>Module</th>
                <th style="text-align:right">Total Logs</th>
                <th style="width:160px">Tỉ lệ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!moduleReport.length">
                <td colspan="3" class="ar2-empty">Không có dữ liệu</td>
              </tr>
              <tr v-for="row in moduleReport" :key="row.module" class="ar2-row">
                <td>
                  <span class="ar2-tag" :class="getModuleClass(row.module)">{{ row.module }}</span>
                </td>
                <td style="text-align:right">
                  <span class="ar2-bold ar2-mono">{{ row.total?.toLocaleString('vi-VN') ?? '—' }}</span>
                </td>
                <td>
                  <div class="ar2-bar-wrap">
                    <div class="ar2-bar" :class="getModuleBarClass(row.module)" :style="{ width: getBarWidth(row.total, moduleReport) }"></div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- User Report -->
      <div class="ar2-card">
        <div class="ar2-card__head">
          <span class="ar2-card__title">Report by User</span>
          <span v-if="userReport.length" class="ar2-count-badge">{{ userReport.length }} user</span>
        </div>
        <div class="ar2-table-wrap">
          <table class="ar2-table">
            <thead>
              <tr>
                <th>User ID</th>
                <th style="text-align:right">Total Actions</th>
                <th style="width:160px">Tỉ lệ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!userReport.length">
                <td colspan="3" class="ar2-empty">Không có dữ liệu</td>
              </tr>
              <tr v-for="row in userReport" :key="row.userId" class="ar2-row">
                <td><span class="ar2-mono-id">#{{ row.userId }}</span></td>
                <td style="text-align:right">
                  <span class="ar2-bold ar2-mono">{{ row.totalActions?.toLocaleString('vi-VN') ?? '—' }}</span>
                </td>
                <td>
                  <div class="ar2-bar-wrap">
                    <div class="ar2-bar ar2-bar--purple" :style="{ width: getBarWidth(row.totalActions, userReport, 'totalActions') }"></div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { auditApi } from "../../api/audit.api";

const moduleReport = ref([]);
const userReport = ref([]);
const summary = ref({});

const loadReports = async () => {
  try {
    const [moduleRes, userRes, summaryRes] = await Promise.all([
      auditApi.reportByModule(),
      auditApi.reportByUser(),
      auditApi.reportSummary(),
    ]);

    moduleReport.value = moduleRes.data;
    userReport.value = userRes.data;
    summary.value = summaryRes.data;
  } catch (err) {
    console.error("Load report error", err);
  }
};

// ── Helpers (UI only) ──
function getModuleClass(m) {
  return { USER: 'ar2-tag--blue', CUSTOMER: 'ar2-tag--green', ORDER: 'ar2-tag--orange', PRODUCT: 'ar2-tag--purple' }[m] || 'ar2-tag--gray';
}
function getModuleBarClass(m) {
  return { USER: 'ar2-bar--blue', CUSTOMER: 'ar2-bar--green', ORDER: 'ar2-bar--orange', PRODUCT: 'ar2-bar--purple' }[m] || 'ar2-bar--gray';
}
function getBarWidth(val, list, key = 'total') {
  const max = Math.max(...list.map(r => r[key] ?? 0), 1);
  return Math.round((val / max) * 100) + '%';
}

onMounted(loadReports);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.ar2-page {
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
.ar2-eyebrow {
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
.ar2-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
  line-height: 1.2;
}
.ar2-title__accent { color: var(--c-blue); }
.ar2-subtitle { font-size: 13.5px; color: var(--c-muted); margin: 0; }

/* ── Card ────────────────────────────────── */
.ar2-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.ar2-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
}
.ar2-card__title {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--c-text);
}
.ar2-card__body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.ar2-count-badge {
  font-size: 11.5px;
  font-weight: 700;
  color: var(--c-muted);
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  padding: 2px 9px;
  border-radius: 20px;
}
.ar2-mb { margin-bottom: 0; }

/* ── Stats 4 ─────────────────────────────── */
.ar2-stats4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
@media (max-width: 900px) { .ar2-stats4 { grid-template-columns: 1fr 1fr; } }

.ar2-stat {
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 18px 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: transform 0.15s, box-shadow 0.15s;
}
.ar2-stat:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.ar2-stat--blue   { border-top: 3px solid var(--c-blue); }
.ar2-stat--green  { border-top: 3px solid var(--c-green); }
.ar2-stat--red    { border-top: 3px solid var(--c-red); }
.ar2-stat--orange { border-top: 3px solid var(--c-orange); }

.ar2-stat__icon {
  width: 36px; height: 36px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}
.ar2-stat--blue .ar2-stat__icon   { background: var(--c-blue-bg);   color: var(--c-blue); }
.ar2-stat--green .ar2-stat__icon  { background: var(--c-green-bg);  color: var(--c-green); }
.ar2-stat--red .ar2-stat__icon    { background: var(--c-red-bg);    color: var(--c-red); }
.ar2-stat--orange .ar2-stat__icon { background: var(--c-orange-bg); color: var(--c-orange); }

.ar2-stat__num {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1;
}
.ar2-stat--blue .ar2-stat__num   { color: var(--c-blue); }
.ar2-stat--green .ar2-stat__num  { color: var(--c-green); }
.ar2-stat--red .ar2-stat__num    { color: var(--c-red); }
.ar2-stat--orange .ar2-stat__num { color: var(--c-orange); }

.ar2-stat__label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--c-muted);
}

/* ── Top row ─────────────────────────────── */
.ar2-top-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
@media (max-width: 700px) { .ar2-top-row { grid-template-columns: 1fr; } }

.ar2-top-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 14px 18px;
}
.ar2-top-card__label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--c-muted);
  flex-shrink: 0;
}
.ar2-top-card__value {
  font-size: 14px;
  font-weight: 700;
}

/* ── Grid 2 ──────────────────────────────── */
.ar2-grid2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
@media (max-width: 860px) { .ar2-grid2 { grid-template-columns: 1fr; } }

/* ── Table ───────────────────────────────── */
.ar2-table-wrap { overflow-x: auto; }
.ar2-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.ar2-table thead tr { border-bottom: 2px solid var(--c-border-light); }
.ar2-table th {
  padding: 11px 14px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--c-subtle);
  background: #fafbfc;
  white-space: nowrap;
}
.ar2-table td {
  padding: 12px 14px;
  vertical-align: middle;
  border-bottom: 1px solid var(--c-border-light);
}
.ar2-row { transition: background 0.12s; }
.ar2-row:hover { background: #fafbfc; }
.ar2-row:last-child td { border-bottom: none; }

/* Bar */
.ar2-bar-wrap {
  height: 6px;
  background: var(--c-border);
  border-radius: 999px;
  overflow: hidden;
}
.ar2-bar {
  height: 100%;
  border-radius: 999px;
  transition: width 0.5s ease;
  min-width: 4px;
}
.ar2-bar--blue   { background: var(--c-blue); }
.ar2-bar--green  { background: var(--c-green); }
.ar2-bar--orange { background: var(--c-orange); }
.ar2-bar--purple { background: var(--c-purple); }
.ar2-bar--gray   { background: var(--c-subtle); }

/* Empty */
.ar2-empty {
  text-align: center;
  padding: 40px 20px !important;
  color: var(--c-muted);
  font-size: 13.5px;
}

/* Tags */
.ar2-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  border: 1px solid transparent;
  white-space: nowrap;
}
.ar2-tag--blue   { background: var(--c-blue-bg);   color: var(--c-blue);   border-color: var(--c-blue-border); }
.ar2-tag--green  { background: var(--c-green-bg);  color: var(--c-green);  border-color: var(--c-green-border); }
.ar2-tag--orange { background: var(--c-orange-bg); color: var(--c-orange); border-color: var(--c-orange-border); }
.ar2-tag--purple { background: var(--c-purple-bg); color: var(--c-purple); border-color: var(--c-purple-border); }
.ar2-tag--gray   { background: var(--c-gray-bg);   color: var(--c-muted);  border-color: var(--c-gray-border); }

/* Mono ID */
.ar2-mono-id {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-blue);
  background: var(--c-blue-bg);
  padding: 2px 7px;
  border-radius: 5px;
}
.ar2-mono  { font-family: "JetBrains Mono", monospace; }
.ar2-bold  { font-weight: 700; }
.ar2-text--muted { color: var(--c-muted); font-size: 13px; }
</style>