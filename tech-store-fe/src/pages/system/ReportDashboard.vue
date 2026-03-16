<template>
  <div class="reports-root">
    <!-- Page Header -->
    <div class="reports-header">
      <div class="header-left">
        <h1 class="header-title">Reports Dashboard</h1>
        <p class="header-sub">Tổng quan doanh thu & hiệu suất kinh doanh</p>
      </div>
      <div class="header-right">
        <div class="header-date">
          <i class="el-icon"
            ><svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" /></svg
          ></i>
          {{ todayFormatted }}
        </div>
      </div>
    </div>

    <!-- KPI Summary Strip -->
    <div class="kpi-strip" v-if="channelData.length">
      <div class="kpi-card kpi-total">
        <div class="kpi-icon kpi-icon--primary">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
        <div class="kpi-info">
          <span class="kpi-label">Tổng doanh thu</span>
          <span class="kpi-value">{{ formatMoney(totalRevenue) }}</span>
          <span class="kpi-badge badge-up">↑ All channels</span>
        </div>
      </div>

      <div class="kpi-card" v-for="ch in channelData" :key="ch.channel">
        <div
          class="kpi-icon"
          :class="
            ch.channel === 'ONLINE' ? 'kpi-icon--online' : 'kpi-icon--offline'
          "
        >
          <svg
            v-if="ch.channel === 'ONLINE'"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <circle cx="12" cy="12" r="10" />
            <path
              d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"
            />
          </svg>
          <svg
            v-else
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        </div>
        <div class="kpi-info">
          <span class="kpi-label">{{ ch.channel }}</span>
          <span class="kpi-value">{{ formatMoney(ch.revenue) }}</span>
          <span
            class="kpi-badge"
            :class="ch.channel === 'ONLINE' ? 'badge-online' : 'badge-offline'"
          >
            {{ ((ch.revenue / totalRevenue) * 100).toFixed(1) }}% tổng
          </span>
        </div>
      </div>

      <div class="kpi-card kpi-orders" v-if="staffData.length">
        <div class="kpi-icon kpi-icon--orders">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
            <line x1="3" y1="6" x2="21" y2="6" />
            <path d="M16 10a4 4 0 0 1-8 0" />
          </svg>
        </div>
        <div class="kpi-info">
          <span class="kpi-label">Tổng đơn hàng</span>
          <span class="kpi-value">{{ totalOrders }}</span>
          <span class="kpi-badge badge-up"
            >{{ staffData.length }} nhân viên</span
          >
        </div>
      </div>
    </div>

    <!-- Main Grid -->
    <div class="reports-grid">
      <!-- Revenue by Channel -->
      <section class="report-section section-channel">
        <div class="section-header">
          <div class="section-title-group">
            <span class="section-icon icon-channel">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
              </svg>
            </span>
            <div>
              <h3 class="section-title">Doanh thu theo kênh</h3>
              <p class="section-desc">Revenue by Channel</p>
            </div>
          </div>
        </div>

        <div class="channel-bars" v-if="channelData.length && totalRevenue">
          <div
            class="channel-bar-row"
            v-for="ch in channelData"
            :key="ch.channel"
          >
            <div class="bar-meta">
              <span class="bar-label">
                <span
                  class="bar-dot"
                  :class="
                    ch.channel === 'ONLINE' ? 'dot-online' : 'dot-offline'
                  "
                ></span>
                {{ ch.channel }}
              </span>
              <span class="bar-amount">{{ formatMoney(ch.revenue) }}</span>
            </div>
            <div class="bar-track">
              <div
                class="bar-fill"
                :class="
                  ch.channel === 'ONLINE' ? 'fill-online' : 'fill-offline'
                "
                :style="{ width: (ch.revenue / totalRevenue) * 100 + '%' }"
              ></div>
            </div>
            <span class="bar-pct"
              >{{ ((ch.revenue / totalRevenue) * 100).toFixed(1) }}%</span
            >
          </div>
        </div>

        <el-table :data="channelData" :show-header="true" class="reports-table">
          <el-table-column prop="channel" label="Kênh bán hàng">
            <template #default="{ row }">
              <div class="cell-channel">
                <span
                  class="channel-badge"
                  :class="
                    row.channel === 'ONLINE'
                      ? 'badge-channel-online'
                      : 'badge-channel-offline'
                  "
                >
                  {{ row.channel }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="revenue" label="Doanh thu" align="right">
            <template #default="{ row }">
              <span class="cell-revenue">{{ formatMoney(row.revenue) }} ₫</span>
            </template>
          </el-table-column>
          <el-table-column label="Tỷ trọng" align="right" width="120">
            <template #default="{ row }">
              <span class="cell-pct"
                >{{ ((row.revenue / totalRevenue) * 100).toFixed(1) }}%</span
              >
            </template>
          </el-table-column>
        </el-table>
      </section>

      <!-- Orders by Staff -->
      <section class="report-section section-staff">
        <div class="section-header">
          <div class="section-title-group">
            <span class="section-icon icon-staff">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </span>
            <div>
              <h3 class="section-title">Hiệu suất nhân viên</h3>
              <p class="section-desc">Orders by Staff</p>
            </div>
          </div>
        </div>

        <div class="staff-list">
          <div class="staff-row" v-for="(s, idx) in staffData" :key="s.staff">
            <div class="staff-rank" :class="`rank-${idx + 1}`">
              {{ idx + 1 }}
            </div>
            <div class="staff-avatar">{{ getInitials(s.staff) }}</div>
            <div class="staff-info">
              <span class="staff-name">{{ s.staff }}</span>
            </div>
            <div class="staff-orders-wrap">
              <div class="staff-bar-track">
                <div
                  class="staff-bar-fill"
                  :style="{
                    width: (s.orders / staffData[0].orders) * 100 + '%',
                  }"
                ></div>
              </div>
              <span class="staff-orders-count">{{ s.orders }} đơn</span>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- Revenue by Date -->
    <section class="report-section section-date">
      <div class="section-header">
        <div class="section-title-group">
          <span class="section-icon icon-date">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
          </span>
          <div>
            <h3 class="section-title">Doanh thu theo ngày</h3>
            <p class="section-desc">Revenue by Date</p>
          </div>
        </div>
        <div class="section-actions">
          <span class="record-count"
            >{{ filteredDateData.length }} / {{ dateData.length }} bản ghi</span
          >
        </div>
      </div>

      <!-- Date Filter Bar -->
      <div class="date-filter-bar">
        <div class="filter-group">
          <label class="filter-label">Từ ngày</label>
          <el-date-picker
            v-model="filterFrom"
            type="date"
            placeholder="Ngày bắt đầu"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
            size="small"
            class="filter-picker"
            clearable
          />
        </div>
        <div class="filter-sep">→</div>
        <div class="filter-group">
          <label class="filter-label">Đến ngày</label>
          <el-date-picker
            v-model="filterTo"
            type="date"
            placeholder="Ngày kết thúc"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
            size="small"
            class="filter-picker"
            clearable
          />
        </div>
        <div class="filter-quick">
          <button
            v-for="q in quickRanges"
            :key="q.label"
            class="quick-btn"
            :class="{ active: activeQuick === q.label }"
            @click="applyQuick(q)"
          >
            {{ q.label }}
          </button>
        </div>
        <button
          class="filter-clear-btn"
          @click="clearFilter"
          v-if="filterFrom || filterTo"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            width="13"
            height="13"
          >
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
          Xoá lọc
        </button>
      </div>

      <!-- Filtered Revenue Summary -->
      <div class="filter-summary" v-if="filterFrom || filterTo">
        <span class="filter-summary-label">Doanh thu khoảng đã chọn:</span>
        <span class="filter-summary-value"
          >{{ formatMoney(filteredTotal) }} ₫</span
        >
      </div>

      <el-table
        :data="filteredDateData"
        class="reports-table table-date"
        :max-height="460"
      >
        <el-table-column type="index" label="#" width="56" />
        <el-table-column prop="date" label="Ngày" min-width="180">
          <template #default="{ row }">
            <span class="cell-date">{{ formatDate(row.date) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="revenue" label="Doanh thu" align="right">
          <template #default="{ row }">
            <span class="cell-revenue">{{ formatMoney(row.revenue) }} ₫</span>
          </template>
        </el-table-column>
        <el-table-column label="So sánh TB" align="right" width="140">
          <template #default="{ row }">
            <span
              class="cell-vs-avg"
              :class="row.revenue >= avgRevenue ? 'vs-up' : 'vs-down'"
            >
              {{ row.revenue >= avgRevenue ? "▲" : "▼" }}
              {{
                Math.abs(
                  ((row.revenue - avgRevenue) / avgRevenue) * 100,
                ).toFixed(0)
              }}%
            </span>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import reportsApi from "../../api/report.api";

const channelData = ref([]);
const dateData = ref([]);
const staffData = ref([]);

// Date filter
const filterFrom = ref(null);
const filterTo = ref(null);
const activeQuick = ref(null);

const todayFormatted = new Date().toLocaleDateString("vi-VN", {
  weekday: "long",
  year: "numeric",
  month: "long",
  day: "numeric",
});

const quickRanges = [
  {
    label: "Tháng này",
    getDates: () => {
      const n = new Date();
      return {
        from: `${n.getFullYear()}-${String(n.getMonth() + 1).padStart(2, "0")}-01`,
        to: toYMD(n),
      };
    },
  },
  {
    label: "Tháng trước",
    getDates: () => {
      const n = new Date();
      const m = new Date(n.getFullYear(), n.getMonth() - 1, 1);
      const last = new Date(n.getFullYear(), n.getMonth(), 0);
      return { from: toYMD(m), to: toYMD(last) };
    },
  },
  {
    label: "30 ngày",
    getDates: () => {
      const n = new Date();
      const f = new Date(n);
      f.setDate(f.getDate() - 29);
      return { from: toYMD(f), to: toYMD(n) };
    },
  },
  {
    label: "7 ngày",
    getDates: () => {
      const n = new Date();
      const f = new Date(n);
      f.setDate(f.getDate() - 6);
      return { from: toYMD(f), to: toYMD(n) };
    },
  },
];

function toYMD(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function applyQuick(q) {
  activeQuick.value = q.label;
  const { from, to } = q.getDates();
  filterFrom.value = from;
  filterTo.value = to;
}

function clearFilter() {
  filterFrom.value = null;
  filterTo.value = null;
  activeQuick.value = null;
}

const totalRevenue = computed(() =>
  channelData.value.reduce((s, r) => s + (r.revenue || 0), 0),
);

const totalOrders = computed(() =>
  staffData.value.reduce((s, r) => s + (r.orders || 0), 0),
);

const avgRevenue = computed(() =>
  dateData.value.length
    ? dateData.value.reduce((s, r) => s + r.revenue, 0) / dateData.value.length
    : 0,
);

const filteredDateData = computed(() => {
  let data = dateData.value;
  if (filterFrom.value) {
    data = data.filter((r) => {
      const d = r.date ? r.date.substring(0, 10) : "";
      return d >= filterFrom.value;
    });
  }
  if (filterTo.value) {
    data = data.filter((r) => {
      const d = r.date ? r.date.substring(0, 10) : "";
      return d <= filterTo.value;
    });
  }
  return data;
});

const filteredTotal = computed(() =>
  filteredDateData.value.reduce((s, r) => s + (r.revenue || 0), 0),
);

function formatMoney(v) {
  return new Intl.NumberFormat("vi-VN").format(v);
}

function formatDate(d) {
  if (!d) return d;
  const date = new Date(d);
  if (isNaN(date)) return d;
  return date.toLocaleDateString("vi-VN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

function getInitials(name) {
  if (!name) return "?";
  const parts = name.split(/[@.\s]/).filter(Boolean);
  return parts
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join("");
}

onMounted(async () => {
  const [ch, dt, st] = await Promise.all([
    reportsApi.revenueByChannel(),
    reportsApi.revenueByDate(),
    reportsApi.ordersByStaff(),
  ]);
  channelData.value = ch.data;
  dateData.value = dt.data;
  staffData.value = st.data;
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Root ─────────────────────────────────────────── */
.reports-root {
  --c-bg: #f5f6fa;
  --c-surface: #ffffff;
  --c-border: #e8eaf0;
  --c-text: #1a1d2e;
  --c-muted: #6b7280;
  --c-primary: #f97316; /* orange accent — e-commerce warmth */
  --c-primary-lt: #fff7ed;
  --c-online: #0ea5e9;
  --c-online-lt: #e0f2fe;
  --c-offline: #8b5cf6;
  --c-offline-lt: #ede9fe;
  --c-up: #10b981;
  --c-up-lt: #d1fae5;
  --c-down: #ef4444;
  --c-down-lt: #fee2e2;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
  --r: 12px;

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--c-bg);
  padding: 28px 32px 48px;
  min-height: 100vh;
  color: var(--c-text);
}

/* ── Header ───────────────────────────────────────── */
.reports-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 28px;
}

.header-eyebrow {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-primary);
  margin-bottom: 6px;
}

.header-eyebrow .dot {
  width: 6px;
  height: 6px;
  background: var(--c-primary);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

.header-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--c-text);
  margin: 0 0 4px;
  line-height: 1.1;
}

.header-sub {
  font-size: 13px;
  color: var(--c-muted);
  margin: 0;
}

.header-date {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-muted);
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: 8px;
  padding: 8px 14px;
  box-shadow: var(--shadow-sm);
}

.header-date .el-icon {
  width: 14px;
  height: 14px;
  display: inline-flex;
}

.header-date .el-icon svg {
  width: 14px;
  height: 14px;
}

/* ── KPI Strip ────────────────────────────────────── */
.kpi-strip {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 28px;
}

.kpi-card {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--r);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  transition:
    box-shadow 0.2s,
    transform 0.2s;
}

.kpi-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-icon svg {
  width: 20px;
  height: 20px;
}

.kpi-icon--primary {
  background: var(--c-primary-lt);
  color: var(--c-primary);
}
.kpi-icon--online {
  background: var(--c-online-lt);
  color: var(--c-online);
}
.kpi-icon--offline {
  background: var(--c-offline-lt);
  color: var(--c-offline);
}
.kpi-icon--orders {
  background: var(--c-up-lt);
  color: var(--c-up);
}

.kpi-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.kpi-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--c-muted);
}

.kpi-value {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--c-text);
  font-family: "JetBrains Mono", monospace;
  white-space: normal;
  word-break: break-all;
  line-height: 1.3;
}

.kpi-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 600;
  border-radius: 20px;
  padding: 2px 8px;
  margin-top: 2px;
}

.badge-up {
  background: var(--c-up-lt);
  color: var(--c-up);
}
.badge-online {
  background: var(--c-online-lt);
  color: var(--c-online);
}
.badge-offline {
  background: var(--c-offline-lt);
  color: var(--c-offline);
}

/* ── Grid ─────────────────────────────────────────── */
.reports-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
  margin-bottom: 20px;
}

/* ── Section Shell ────────────────────────────────── */
.report-section {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--r);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--c-border);
}

.section-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  width: 38px;
  height: 38px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.section-icon svg {
  width: 17px;
  height: 17px;
}

.icon-channel {
  background: #fff7ed;
  color: var(--c-primary);
}
.icon-staff {
  background: #ede9fe;
  color: #8b5cf6;
}
.icon-date {
  background: #e0f2fe;
  color: var(--c-online);
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--c-text);
  margin: 0 0 2px;
}

.section-desc {
  font-size: 11px;
  color: var(--c-muted);
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 500;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-count {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-muted);
  background: var(--c-bg);
  border: 1px solid var(--c-border);
  border-radius: 6px;
  padding: 4px 10px;
}

/* ── Channel Bar Visualization ────────────────────── */
.channel-bars {
  padding: 20px 24px 4px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.channel-bar-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bar-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bar-label {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text);
}

.bar-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-online {
  background: var(--c-online);
}
.dot-offline {
  background: var(--c-offline);
}

.bar-amount {
  font-family: "JetBrains Mono", monospace;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-text);
}

.bar-track {
  height: 8px;
  background: var(--c-bg);
  border-radius: 99px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 99px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.fill-online {
  background: linear-gradient(90deg, var(--c-online), #38bdf8);
}
.fill-offline {
  background: linear-gradient(90deg, var(--c-offline), #a78bfa);
}

.bar-pct {
  font-size: 11px;
  font-weight: 700;
  color: var(--c-muted);
  text-align: right;
}

/* ── Tables ───────────────────────────────────────── */
.reports-table {
  font-family: "Plus Jakarta Sans", sans-serif !important;
}

.reports-table :deep(th.el-table__cell) {
  background: var(--c-bg) !important;
  border-bottom: 1px solid var(--c-border) !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.07em !important;
  color: var(--c-muted) !important;
  padding: 10px 16px !important;
}

.reports-table :deep(td.el-table__cell) {
  border-bottom: 1px solid #f3f4f6 !important;
  padding: 12px 16px !important;
  font-size: 13px !important;
}

.reports-table :deep(tr:hover > td.el-table__cell) {
  background: #fafbff !important;
}

.reports-table :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

.cell-date {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  color: var(--c-text);
  font-weight: 500;
}

.cell-revenue {
  font-family: "JetBrains Mono", monospace;
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text);
}

.cell-pct {
  font-size: 12px;
  font-weight: 700;
  color: var(--c-muted);
}

.cell-vs-avg {
  font-size: 12px;
  font-weight: 700;
  border-radius: 5px;
  padding: 3px 8px;
  display: inline-block;
}

.vs-up {
  background: var(--c-up-lt);
  color: var(--c-up);
}
.vs-down {
  background: var(--c-down-lt);
  color: var(--c-down);
}

.channel-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  border-radius: 6px;
  padding: 3px 10px;
}

.badge-channel-online {
  background: var(--c-online-lt);
  color: var(--c-online);
}
.badge-channel-offline {
  background: var(--c-offline-lt);
  color: var(--c-offline);
}

/* ── Staff Section ────────────────────────────────── */
.staff-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.staff-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 12px;
  border-radius: 10px;
  transition: background 0.15s;
}

.staff-row:hover {
  background: var(--c-bg);
}

.staff-rank {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--c-bg);
  color: var(--c-muted);
  border: 1px solid var(--c-border);
}

.rank-1 {
  background: #fef9c3;
  color: #a16207;
  border-color: #fde68a;
}
.rank-2 {
  background: #f1f5f9;
  color: #475569;
  border-color: #cbd5e1;
}
.rank-3 {
  background: #fff7ed;
  color: #c2410c;
  border-color: #fed7aa;
}

.staff-avatar {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.staff-info {
  flex: 1;
  min-width: 0;
}

.staff-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.staff-orders-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.staff-bar-track {
  width: 80px;
  height: 5px;
  background: var(--c-bg);
  border-radius: 99px;
  overflow: hidden;
}

.staff-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #8b5cf6, #a78bfa);
  border-radius: 99px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.staff-orders-count {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text);
  white-space: nowrap;
}

/* ── Date Filter Bar ──────────────────────────────── */
.date-filter-bar {
  display: flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px 24px;
  background: var(--c-bg);
  border-bottom: 1px solid var(--c-border);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.filter-label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--c-muted);
}

.filter-picker {
  width: 160px !important;
}

.filter-sep {
  font-size: 16px;
  color: var(--c-muted);
  padding-bottom: 4px;
  align-self: flex-end;
}

.filter-quick {
  display: flex;
  align-items: center;
  gap: 6px;
  align-self: flex-end;
}

.quick-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 7px;
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-muted);
  font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}

.quick-btn:hover {
  border-color: var(--c-online);
  color: var(--c-online);
  background: var(--c-online-lt);
}

.quick-btn.active {
  border-color: var(--c-online);
  background: var(--c-online);
  color: #fff;
}

.filter-clear-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 12px;
  border-radius: 7px;
  border: 1px solid var(--c-down);
  background: var(--c-down-lt);
  color: var(--c-down);
  font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  align-self: flex-end;
  transition: all 0.15s;
}

.filter-clear-btn:hover {
  background: var(--c-down);
  color: #fff;
}

.filter-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 24px;
  border-bottom: 1px solid var(--c-border);
  background: #fffbeb;
}

.filter-summary-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-muted);
}

.filter-summary-value {
  font-family: "JetBrains Mono", monospace;
  font-size: 15px;
  font-weight: 700;
  color: var(--c-primary);
}

/* ── Date table full-width ────────────────────────── */
.section-date {
  margin-top: 0;
}

/* ── Responsive ───────────────────────────────────── */
@media (max-width: 1024px) {
  .reports-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .reports-root {
    padding: 16px;
  }
  .kpi-strip {
    grid-template-columns: 1fr 1fr;
  }
  .header-right {
    display: none;
  }
}
</style>
