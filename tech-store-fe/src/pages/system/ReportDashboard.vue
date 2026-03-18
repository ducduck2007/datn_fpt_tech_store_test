<template>
  <div class="reports-root">

    <!-- ── Page Header ───────────────────────────── -->
    <header class="rp-header">
      <div class="rp-header__left">
        <h1 class="rp-header__title">Tổng quan kinh doanh</h1>
        <p class="rp-header__sub">Doanh thu · Hiệu suất · Trạng thái đơn hàng</p>
      </div>
      <div class="rp-header__right">
        <div class="rp-date-pill">
          <svg class="rp-date-pill__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          {{ todayFormatted }}
        </div>
      </div>
    </header>

    <!-- ── KPI Strip ─────────────────────────────── -->
    <div class="kpi-strip" v-if="channelData.length">
      <div class="kpi-card kpi-card--primary">
        <div class="kpi-card__icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div class="kpi-card__body">
          <span class="kpi-card__label">Tổng doanh thu</span>
          <span class="kpi-card__value">{{ formatMoney(totalRevenue) }}&thinsp;₫</span>
          <span class="kpi-tag kpi-tag--green">↑ All channels</span>
        </div>
      </div>

      <div class="kpi-card" v-for="ch in channelData" :key="ch.channel"
        :class="ch.channel === 'ONLINE' ? 'kpi-card--online' : 'kpi-card--offline'">
        <div class="kpi-card__icon">
          <svg v-if="ch.channel === 'ONLINE'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
        </div>
        <div class="kpi-card__body">
          <span class="kpi-card__label">{{ ch.channel }}</span>
          <span class="kpi-card__value">{{ formatMoney(ch.revenue) }}&thinsp;₫</span>
          <span class="kpi-tag" :class="ch.channel === 'ONLINE' ? 'kpi-tag--blue' : 'kpi-tag--purple'">
            {{ ((ch.revenue / totalRevenue) * 100).toFixed(1) }}% tổng
          </span>
        </div>
      </div>

      <div class="kpi-card kpi-card--orders" v-if="staffData.length">
        <div class="kpi-card__icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 0 1-8 0"/>
          </svg>
        </div>
        <div class="kpi-card__body">
          <span class="kpi-card__label">Tổng đơn hàng</span>
          <span class="kpi-card__value">{{ totalOrders }}</span>
          <span class="kpi-tag kpi-tag--green">{{ staffData.length }} nhân viên</span>
        </div>
      </div>
    </div>

    <!-- ── Row 1: Channel + Staff ─────────────────── -->
    <div class="rp-grid rp-grid--2col">

      <!-- Revenue by Channel -->
      <section class="rp-card">
        <div class="rp-card__header">
          <div class="rp-card__title-group">
            <span class="rp-icon rp-icon--orange">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
              </svg>
            </span>
            <div>
              <h3 class="rp-card__title">Doanh thu theo kênh</h3>
              <p class="rp-card__desc">Revenue by Channel</p>
            </div>
          </div>
        </div>

        <div class="channel-viz" v-if="channelData.length && totalRevenue">
          <div class="channel-viz__row" v-for="ch in channelData" :key="ch.channel">
            <div class="channel-viz__top">
              <span class="channel-viz__label">
                <span class="ch-dot" :class="ch.channel === 'ONLINE' ? 'ch-dot--online' : 'ch-dot--offline'"></span>
                <span class="ch-badge" :class="ch.channel === 'ONLINE' ? 'ch-badge--online' : 'ch-badge--offline'">
                  {{ ch.channel }}
                </span>
              </span>
              <div class="channel-viz__nums">
                <span class="channel-viz__amount">{{ formatMoney(ch.revenue) }}&thinsp;₫</span>
                <span class="channel-viz__pct">{{ ((ch.revenue / totalRevenue) * 100).toFixed(1) }}%</span>
              </div>
            </div>
            <div class="progress-track">
              <div class="progress-fill"
                :class="ch.channel === 'ONLINE' ? 'progress-fill--online' : 'progress-fill--offline'"
                :style="{ width: (ch.revenue / totalRevenue) * 100 + '%' }">
              </div>
            </div>
          </div>
        </div>

        <el-table :data="channelData" :show-header="true" class="rp-table">
          <el-table-column prop="channel" label="Kênh bán hàng">
            <template #default="{ row }">
              <span class="ch-badge" :class="row.channel === 'ONLINE' ? 'ch-badge--online' : 'ch-badge--offline'">
                {{ row.channel }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="revenue" label="Doanh thu" align="right">
            <template #default="{ row }">
              <span class="mono-val">{{ formatMoney(row.revenue) }}&thinsp;₫</span>
            </template>
          </el-table-column>
          <el-table-column label="Tỷ trọng" align="right" width="110">
            <template #default="{ row }">
              <span class="pct-val">{{ ((row.revenue / totalRevenue) * 100).toFixed(1) }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <!-- Orders by Staff -->
      <section class="rp-card">
        <div class="rp-card__header">
          <div class="rp-card__title-group">
            <span class="rp-icon rp-icon--purple">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </span>
            <div>
              <h3 class="rp-card__title">Hiệu suất nhân viên</h3>
              <p class="rp-card__desc">Orders by Staff</p>
            </div>
          </div>
          <span class="rp-badge-count" v-if="staffData.length">{{ staffData.length }} nhân viên</span>
        </div>

        <div class="staff-list">
          <div class="staff-item" v-for="(s, idx) in staffData" :key="s.staff">
            <div class="staff-item__rank" :class="`rank-${Math.min(idx + 1, 3)}`">{{ idx + 1 }}</div>
            <div class="staff-item__avatar">{{ getInitials(s.staff) }}</div>
            <div class="staff-item__info">
              <span class="staff-item__name">{{ s.staff }}</span>
              <div class="staff-item__bar-wrap">
                <div class="staff-bar-track">
                  <div class="staff-bar-fill" :style="{ width: (s.orders / staffData[0].orders) * 100 + '%' }"></div>
                </div>
              </div>
            </div>
            <span class="staff-item__count">{{ s.orders }}&thinsp;đơn</span>
          </div>
        </div>
      </section>

    </div>

    <!-- ── Row 2: Revenue by Date ────────────────── -->
    <section class="rp-card rp-card--full">
      <div class="rp-card__header">
        <div class="rp-card__title-group">
          <span class="rp-icon rp-icon--blue">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </span>
          <div>
            <h3 class="rp-card__title">Doanh thu theo ngày</h3>
            <p class="rp-card__desc">Revenue by Date</p>
          </div>
        </div>
        <span class="rp-badge-count">{{ filteredDateData.length }} / {{ dateData.length }} bản ghi</span>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar">
        <div class="filter-bar__pickers">
          <div class="filter-field">
            <label class="filter-field__label">Từ ngày</label>
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
          <span class="filter-bar__arrow">→</span>
          <div class="filter-field">
            <label class="filter-field__label">Đến ngày</label>
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
        </div>

        <div class="filter-bar__quick">
          <button
            v-for="q in quickRanges"
            :key="q.label"
            class="quick-btn"
            :class="{ 'quick-btn--active': activeQuick === q.label }"
            @click="applyQuick(q)"
          >{{ q.label }}</button>
        </div>

        <button class="clear-btn" v-if="filterFrom || filterTo" @click="clearFilter">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="11" height="11">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
          Xoá lọc
        </button>

        <div class="filter-total" v-if="filterFrom || filterTo">
          <span class="filter-total__label">Tổng khoảng chọn</span>
          <span class="filter-total__value">{{ formatMoney(filteredTotal) }}&thinsp;₫</span>
        </div>
      </div>

      <el-table :data="filteredDateData" class="rp-table" :max-height="440">
        <el-table-column type="index" label="#" width="52"/>
        <el-table-column prop="date" label="Ngày" min-width="180">
          <template #default="{ row }">
            <span class="mono-val mono-val--sm">{{ formatDate(row.date) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="revenue" label="Doanh thu" align="right">
          <template #default="{ row }">
            <span class="mono-val">{{ formatMoney(row.revenue) }}&thinsp;₫</span>
          </template>
        </el-table-column>
        <el-table-column label="vs Trung bình" align="right" width="140">
          <template #default="{ row }">
            <span class="vs-avg" :class="row.revenue >= avgRevenue ? 'vs-avg--up' : 'vs-avg--down'">
              {{ row.revenue >= avgRevenue ? '▲' : '▼' }}
              {{ Math.abs(((row.revenue - avgRevenue) / avgRevenue) * 100).toFixed(0) }}%
            </span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <!-- ── Row 3: Status + Returns ───────────────── -->
    <div class="rp-grid rp-grid--2col">

      <!-- Revenue by Status -->
      <section class="rp-card">
        <div class="rp-card__header">
          <div class="rp-card__title-group">
            <span class="rp-icon rp-icon--amber">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
            </span>
            <div>
              <h3 class="rp-card__title">Trạng thái đơn hàng</h3>
              <p class="rp-card__desc">Revenue by Order Status</p>
            </div>
          </div>
        </div>

        <div class="status-bars" v-if="statusData.length">
          <div class="status-bar-row" v-for="s in statusData" :key="s.status">
            <div class="status-bar-row__meta">
              <span class="status-chip" :class="`status-chip--${s.status.toLowerCase()}`">
                {{ statusLabel(s.status) }}
              </span>
              <span class="status-bar-row__orders">{{ s.orderCount }} đơn</span>
              <span class="status-bar-row__revenue">{{ formatMoney(s.totalRevenue) }}&thinsp;₫</span>
            </div>
            <div class="progress-track">
              <div class="progress-fill status-fill"
                :class="`status-fill--${s.status.toLowerCase()}`"
                :style="{ width: totalStatusRevenue ? (s.totalRevenue / totalStatusRevenue) * 100 + '%' : '0%' }">
              </div>
            </div>
          </div>
        </div>

        <el-table :data="statusData" class="rp-table" v-if="statusData.length">
          <el-table-column label="Trạng thái" min-width="150">
            <template #default="{ row }">
              <span class="status-chip" :class="`status-chip--${row.status.toLowerCase()}`">
                {{ statusLabel(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="Số đơn" align="right" width="90">
            <template #default="{ row }">
              <span class="mono-val mono-val--sm">{{ row.orderCount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Doanh thu" align="right">
            <template #default="{ row }">
              <span class="mono-val">{{ formatMoney(row.totalRevenue) }}&thinsp;₫</span>
            </template>
          </el-table-column>
          <el-table-column label="Tỷ trọng" align="right" width="100">
            <template #default="{ row }">
              <span class="pct-val">
                {{ totalStatusRevenue ? ((row.totalRevenue / totalStatusRevenue) * 100).toFixed(1) : 0 }}%
              </span>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-state">Chưa có dữ liệu</div>
      </section>

      <!-- Returns by Product -->
      <section class="rp-card">
        <div class="rp-card__header">
          <div class="rp-card__title-group">
            <span class="rp-icon rp-icon--red">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="1 4 1 10 7 10"/>
                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>
              </svg>
            </span>
            <div>
              <h3 class="rp-card__title">Trả hàng theo sản phẩm</h3>
              <p class="rp-card__desc">Returns by Product (excl. Rejected)</p>
            </div>
          </div>
          <span class="rp-badge-count">{{ returnsData.length }} sản phẩm</span>
        </div>

        <el-table :data="returnsData" class="rp-table" :max-height="440" v-if="returnsData.length">
          <el-table-column type="index" label="#" width="48"/>
          <el-table-column label="Sản phẩm" min-width="200">
            <template #default="{ row }">
              <div class="return-cell">
                <span class="return-cell__name">{{ row.productName }}</span>
                <span class="return-cell__variant" v-if="row.variantName">{{ row.variantName }}</span>
                <span class="return-cell__sku" v-if="row.sku">{{ row.sku }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="Lần trả" align="center" width="80">
            <template #default="{ row }">
              <span class="return-badge">{{ row.returnCount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Tổng SL" align="right" width="80">
            <template #default="{ row }">
              <span class="mono-val mono-val--sm">{{ row.totalQty }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Hoàn tiền" align="right" min-width="140">
            <template #default="{ row }">
              <span class="return-refund">{{ formatMoney(row.totalRefund) }}&thinsp;₫</span>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-state">Không có dữ liệu trả hàng</div>
      </section>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import reportsApi from "../../api/report.api";

const channelData = ref([]);
const dateData     = ref([]);
const staffData    = ref([]);
const statusData   = ref([]);
const returnsData  = ref([]);

const filterFrom  = ref(null);
const filterTo    = ref(null);
const activeQuick = ref(null);

const todayFormatted = new Date().toLocaleDateString("vi-VN", {
  weekday: "long", year: "numeric", month: "long", day: "numeric",
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
      const n = new Date(), f = new Date(n);
      f.setDate(f.getDate() - 29);
      return { from: toYMD(f), to: toYMD(n) };
    },
  },
  {
    label: "7 ngày",
    getDates: () => {
      const n = new Date(), f = new Date(n);
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
  if (filterFrom.value) data = data.filter(r => (r.date?.substring(0, 10) ?? "") >= filterFrom.value);
  if (filterTo.value)   data = data.filter(r => (r.date?.substring(0, 10) ?? "") <= filterTo.value);
  return data;
});
const filteredTotal = computed(() =>
  filteredDateData.value.reduce((s, r) => s + (r.revenue || 0), 0),
);
const totalStatusRevenue = computed(() =>
  statusData.value.reduce((s, r) => s + Number(r.totalRevenue || 0), 0),
);

const STATUS_LABEL_MAP = {
  PENDING: "Chờ xử lý", CONFIRMED: "Đã xác nhận", PROCESSING: "Đang xử lý",
  PAID: "Đã thanh toán", SHIPPING: "Đang giao", DELIVERED: "Đã giao",
  CANCELLED: "Đã hủy", RETURNED: "Đã trả hàng", PARTIALLY_RETURNED: "Trả một phần",
};
function statusLabel(s) { return STATUS_LABEL_MAP[s] || s; }
function formatMoney(v) { return new Intl.NumberFormat("vi-VN").format(v); }
function formatDate(d) {
  if (!d) return d;
  const date = new Date(d);
  if (isNaN(date)) return d;
  return date.toLocaleDateString("vi-VN", { year: "numeric", month: "2-digit", day: "2-digit" });
}
function getInitials(name) {
  if (!name) return "?";
  return name.split(/[@.\s]/).filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join("");
}

onMounted(async () => {
  const [ch, dt, st, sv, rt] = await Promise.all([
    reportsApi.revenueByChannel(),
    reportsApi.revenueByDate(),
    reportsApi.ordersByStaff(),
    reportsApi.revenueByStatus(),
    reportsApi.returnsByProduct(),
  ]);
  channelData.value = ch.data;
  dateData.value    = dt.data;
  staffData.value   = st.data;
  statusData.value  = sv.data;
  returnsData.value = rt.data;
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap");

/* ══════════════════════════════════════════════════
   TOKENS
══════════════════════════════════════════════════ */
.reports-root {
  --bg:         #f4f5f9;
  --surface:    #ffffff;
  --border:     #e9eaf0;
  --border-lt:  #f1f2f7;
  --text:       #18192b;
  --muted:      #717585;
  --muted-lt:   #a0a3b1;

  --orange:     #f97316;
  --orange-lt:  #fff7ed;
  --orange-md:  #ffedd5;

  --blue:       #0ea5e9;
  --blue-lt:    #e0f2fe;

  --purple:     #8b5cf6;
  --purple-lt:  #ede9fe;

  --green:      #10b981;
  --green-lt:   #d1fae5;

  --red:        #ef4444;
  --red-lt:     #fee2e2;

  --amber:      #f59e0b;
  --amber-lt:   #fffbeb;

  --sh-xs: 0 1px 2px rgba(0,0,0,.04);
  --sh-sm: 0 1px 4px rgba(0,0,0,.06), 0 2px 8px rgba(0,0,0,.04);
  --sh-md: 0 4px 20px rgba(0,0,0,.08);
  --r-sm: 8px;
  --r:    12px;
  --r-lg: 16px;

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--bg);
  color: var(--text);
  padding: 28px 28px 56px;
  min-height: 100vh;
}

/* ══════════════════════════════════════════════════
   HEADER
══════════════════════════════════════════════════ */
.rp-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border);
}

.rp-header__eyebrow {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: .1em;
  text-transform: uppercase;
  color: var(--orange);
  margin-bottom: 6px;
}
.eyebrow-dot {
  width: 6px; height: 6px;
  background: var(--orange);
  border-radius: 50%;
  animation: blink 2.4s ease-in-out infinite;
}
@keyframes blink { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:.4;transform:scale(.7)} }

.rp-header__title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -.025em;
  color: var(--text);
  margin: 0 0 4px;
  line-height: 1.15;
}
.rp-header__sub {
  font-size: 12.5px;
  color: var(--muted);
  margin: 0;
  font-weight: 500;
}

.rp-date-pill {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--muted);
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 99px;
  padding: 8px 16px;
  box-shadow: var(--sh-xs);
  white-space: nowrap;
}
.rp-date-pill__icon {
  width: 13px; height: 13px;
  flex-shrink: 0;
  color: var(--orange);
}

/* ══════════════════════════════════════════════════
   KPI STRIP
══════════════════════════════════════════════════ */
.kpi-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 24px;
}

.kpi-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 16px 18px 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--sh-sm);
  transition: box-shadow .2s, transform .2s;
  position: relative;
  overflow: hidden;
  min-width: 0;
}
.kpi-card::before {
  content: "";
  position: absolute;
  left: 0; top: 12px; bottom: 12px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--border);
}
.kpi-card--primary::before { background: var(--orange); }
.kpi-card--online::before  { background: var(--blue); }
.kpi-card--offline::before { background: var(--purple); }
.kpi-card--orders::before  { background: var(--green); }
.kpi-card:hover { box-shadow: var(--sh-md); transform: translateY(-2px); }

.kpi-card__icon {
  width: 42px; height: 42px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.kpi-card__icon svg { width: 19px; height: 19px; }

.kpi-card--primary .kpi-card__icon { background: var(--orange-lt); color: var(--orange); }
.kpi-card--online  .kpi-card__icon { background: var(--blue-lt);   color: var(--blue); }
.kpi-card--offline .kpi-card__icon { background: var(--purple-lt); color: var(--purple); }
.kpi-card--orders  .kpi-card__icon { background: var(--green-lt);  color: var(--green); }

.kpi-card__body {
  display: flex; flex-direction: column;
  gap: 2px;
  min-width: 0; flex: 1;
  overflow: hidden;
}
.kpi-card__label {
  font-size: 10px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .08em;
  color: var(--muted);
  white-space: nowrap;
}
.kpi-card__value {
  font-family: "JetBrains Mono", monospace;
  font-size: 15px; font-weight: 700;
  color: var(--text);
  letter-spacing: -.015em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}
.kpi-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 10px; font-weight: 700;
  border-radius: 99px;
  padding: 2px 8px;
  margin-top: 3px;
  width: fit-content;
  white-space: nowrap;
}
.kpi-tag--green  { background: var(--green-lt);  color: var(--green); }
.kpi-tag--blue   { background: var(--blue-lt);   color: var(--blue); }
.kpi-tag--purple { background: var(--purple-lt); color: var(--purple); }

/* ══════════════════════════════════════════════════
   LAYOUT
══════════════════════════════════════════════════ */
.rp-grid {
  display: grid;
  gap: 18px;
  margin-bottom: 18px;
}
.rp-grid--2col {
  grid-template-columns: 1fr 1fr;
}

/* ══════════════════════════════════════════════════
   CARD SHELL
══════════════════════════════════════════════════ */
.rp-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  overflow: hidden;
  box-shadow: var(--sh-sm);
}
.rp-card--full { margin-bottom: 18px; }

.rp-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px 15px;
  border-bottom: 1px solid var(--border-lt);
  background: var(--surface);
}
.rp-card__title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}
.rp-card__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 2px;
}
.rp-card__desc {
  font-size: 10.5px;
  color: var(--muted-lt);
  margin: 0;
  text-transform: uppercase;
  letter-spacing: .06em;
  font-weight: 600;
}

.rp-icon {
  width: 36px; height: 36px;
  border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.rp-icon svg { width: 16px; height: 16px; }
.rp-icon--orange { background: var(--orange-lt); color: var(--orange); }
.rp-icon--blue   { background: var(--blue-lt);   color: var(--blue); }
.rp-icon--purple { background: var(--purple-lt); color: var(--purple); }
.rp-icon--amber  { background: var(--amber-lt);  color: var(--amber); }
.rp-icon--red    { background: var(--red-lt);    color: var(--red); }

.rp-badge-count {
  font-size: 11px; font-weight: 600;
  color: var(--muted);
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 4px 10px;
  white-space: nowrap;
}

/* ══════════════════════════════════════════════════
   CHANNEL VIZ
══════════════════════════════════════════════════ */
.channel-viz {
  padding: 18px 22px 8px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.channel-viz__row { display: flex; flex-direction: column; gap: 7px; }
.channel-viz__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.channel-viz__label {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ch-dot {
  width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0;
}
.ch-dot--online  { background: var(--blue); }
.ch-dot--offline { background: var(--purple); }

.ch-badge {
  display: inline-block;
  font-size: 11px; font-weight: 700;
  letter-spacing: .04em;
  border-radius: 5px;
  padding: 2px 8px;
}
.ch-badge--online  { background: var(--blue-lt);   color: var(--blue); }
.ch-badge--offline { background: var(--purple-lt); color: var(--purple); }

.channel-viz__nums { display: flex; align-items: center; gap: 12px; }
.channel-viz__amount {
  font-family: "JetBrains Mono", monospace;
  font-size: 13px; font-weight: 600; color: var(--text);
}
.channel-viz__pct {
  font-size: 11.5px; font-weight: 700;
  color: var(--muted);
  min-width: 42px; text-align: right;
}

/* Progress bars (shared) */
.progress-track {
  height: 7px;
  background: var(--bg);
  border-radius: 99px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 99px;
  transition: width 1s cubic-bezier(.4,0,.2,1);
}
.progress-fill--online  { background: linear-gradient(90deg, var(--blue),   #38bdf8); }
.progress-fill--offline { background: linear-gradient(90deg, var(--purple), #a78bfa); }

/* ══════════════════════════════════════════════════
   TABLES
══════════════════════════════════════════════════ */
.rp-table { font-family: "Plus Jakarta Sans", sans-serif !important; }

.rp-table :deep(th.el-table__cell) {
  background: #f8f9fc !important;
  border-bottom: 1px solid var(--border) !important;
  font-size: 10.5px !important;
  font-weight: 700 !important;
  text-transform: uppercase !important;
  letter-spacing: .07em !important;
  color: var(--muted) !important;
  padding: 10px 16px !important;
}
.rp-table :deep(td.el-table__cell) {
  border-bottom: 1px solid var(--border-lt) !important;
  padding: 11px 16px !important;
  font-size: 13px !important;
}
.rp-table :deep(tr:hover > td.el-table__cell) {
  background: #fafbff !important;
}
.rp-table :deep(.el-table__body-wrapper) { overflow-y: auto; }

.mono-val {
  font-family: "JetBrains Mono", monospace;
  font-size: 13px; font-weight: 600; color: var(--text);
}
.mono-val--sm { font-size: 12px; }
.pct-val { font-size: 12px; font-weight: 700; color: var(--muted); }
.vs-avg {
  font-size: 11.5px; font-weight: 700;
  border-radius: 5px;
  padding: 2px 7px;
  display: inline-block;
}
.vs-avg--up   { background: var(--green-lt); color: var(--green); }
.vs-avg--down { background: var(--red-lt);   color: var(--red); }

/* ══════════════════════════════════════════════════
   STAFF LIST
══════════════════════════════════════════════════ */
.staff-list {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.staff-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 9px;
  transition: background .15s;
}
.staff-item:hover { background: var(--bg); }

.staff-item__rank {
  width: 22px; height: 22px;
  border-radius: 50%;
  font-size: 11px; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  background: var(--bg);
  color: var(--muted);
  border: 1px solid var(--border);
}
.rank-1 { background: #fef9c3; color: #a16207; border-color: #fde68a; }
.rank-2 { background: #f1f5f9; color: #475569; border-color: #cbd5e1; }
.rank-3 { background: #fff7ed; color: #c2410c; border-color: #fed7aa; }

.staff-item__avatar {
  width: 34px; height: 34px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.staff-item__info {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 5px;
}
.staff-item__name {
  font-size: 13px; font-weight: 600; color: var(--text);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.staff-item__bar-wrap { width: 100%; }

.staff-bar-track {
  width: 100%; height: 4px;
  background: var(--bg);
  border-radius: 99px;
  overflow: hidden;
}
.staff-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--purple), #a78bfa);
  border-radius: 99px;
  transition: width 1s cubic-bezier(.4,0,.2,1);
}
.staff-item__count {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px; font-weight: 600;
  color: var(--text);
  white-space: nowrap;
  flex-shrink: 0;
}

/* ══════════════════════════════════════════════════
   FILTER BAR
══════════════════════════════════════════════════ */
.filter-bar {
  display: flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 12px 14px;
  padding: 14px 22px;
  background: #f8f9fc;
  border-bottom: 1px solid var(--border);
}
.filter-bar__pickers {
  display: flex; align-items: flex-end; gap: 10px;
}
.filter-field { display: flex; flex-direction: column; gap: 5px; }
.filter-field__label {
  font-size: 10px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .07em;
  color: var(--muted);
}
.filter-picker { width: 155px !important; }

.filter-bar__arrow {
  font-size: 15px; color: var(--muted-lt);
  padding-bottom: 4px;
}
.filter-bar__quick {
  display: flex; align-items: center; gap: 6px;
  flex-wrap: wrap;
}
.quick-btn {
  height: 30px; padding: 0 11px;
  border-radius: 6px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--muted);
  font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 11.5px; font-weight: 600;
  cursor: pointer;
  transition: all .15s;
  white-space: nowrap;
}
.quick-btn:hover {
  border-color: var(--blue); color: var(--blue);
  background: var(--blue-lt);
}
.quick-btn--active {
  border-color: var(--blue);
  background: var(--blue);
  color: #fff;
}
.clear-btn {
  display: flex; align-items: center; gap: 5px;
  height: 30px; padding: 0 11px;
  border-radius: 6px;
  border: 1px solid var(--red);
  background: var(--red-lt);
  color: var(--red);
  font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 11.5px; font-weight: 600;
  cursor: pointer;
  transition: all .15s;
}
.clear-btn:hover { background: var(--red); color: #fff; }

.filter-total {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  background: var(--orange-md);
  border: 1px solid #fed7aa;
  border-radius: 7px;
  padding: 5px 12px;
}
.filter-total__label {
  font-size: 11px; font-weight: 600; color: #9a3412;
}
.filter-total__value {
  font-family: "JetBrains Mono", monospace;
  font-size: 14px; font-weight: 700;
  color: var(--orange);
}

/* ══════════════════════════════════════════════════
   STATUS BARS
══════════════════════════════════════════════════ */
.status-bars {
  padding: 14px 22px 10px;
  display: flex;
  flex-direction: column;
  gap: 11px;
  border-bottom: 1px solid var(--border-lt);
}
.status-bar-row { display: flex; flex-direction: column; gap: 6px; }
.status-bar-row__meta {
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
}
.status-bar-row__orders {
  font-size: 11.5px; font-weight: 500; color: var(--muted);
  margin-left: auto;
}
.status-bar-row__revenue {
  font-family: "JetBrains Mono", monospace;
  font-size: 12.5px; font-weight: 600; color: var(--text);
}

/* Status chips */
.status-chip {
  display: inline-block;
  padding: 2px 9px;
  border-radius: 99px;
  font-size: 11px; font-weight: 700;
  letter-spacing: .03em;
  white-space: nowrap;
  border: 1px solid transparent;
}
.status-chip--pending            { background: #fffbeb; color: #92400e; border-color: #fde68a; }
.status-chip--confirmed          { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
.status-chip--processing         { background: #f5f3ff; color: #6d28d9; border-color: #ddd6fe; }
.status-chip--paid               { background: #f0fdf4; color: #15803d; border-color: #bbf7d0; }
.status-chip--shipping           { background: #ecfeff; color: #0e7490; border-color: #a5f3fc; }
.status-chip--delivered          { background: #f0fdf4; color: #15803d; border-color: #86efac; }
.status-chip--cancelled          { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.status-chip--returned           { background: #fff7ed; color: #c2410c; border-color: #fed7aa; }
.status-chip--partially_returned { background: #fff7ed; color: #c2410c; border-color: #fed7aa; }

/* Status fill colors */
.status-fill { transition: width 1s cubic-bezier(.4,0,.2,1); }
.status-fill--delivered          { background: linear-gradient(90deg, #10b981, #34d399); }
.status-fill--paid               { background: linear-gradient(90deg, #0ea5e9, #38bdf8); }
.status-fill--pending            { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.status-fill--processing         { background: linear-gradient(90deg, #8b5cf6, #a78bfa); }
.status-fill--shipping           { background: linear-gradient(90deg, #0891b2, #22d3ee); }
.status-fill--confirmed          { background: linear-gradient(90deg, #2563eb, #60a5fa); }
.status-fill--cancelled          { background: linear-gradient(90deg, #ef4444, #f87171); }
.status-fill--returned           { background: linear-gradient(90deg, #ea580c, #fb923c); }
.status-fill--partially_returned { background: linear-gradient(90deg, #ea580c, #fb923c); }

/* ══════════════════════════════════════════════════
   RETURNS TABLE
══════════════════════════════════════════════════ */
.return-cell { display: flex; flex-direction: column; gap: 2px; }
.return-cell__name {
  font-size: 13px; font-weight: 600; color: var(--text);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  max-width: 260px;
}
.return-cell__variant { font-size: 11px; color: var(--muted); }
.return-cell__sku {
  font-family: "JetBrains Mono", monospace;
  font-size: 10px; color: #94a3b8; letter-spacing: .04em;
}
.return-badge {
  display: inline-flex; align-items: center; justify-content: center;
  width: 26px; height: 26px;
  border-radius: 50%;
  background: var(--red-lt); color: #b91c1c;
  font-size: 12px; font-weight: 700;
}
.return-refund {
  font-family: "JetBrains Mono", monospace;
  font-size: 13px; font-weight: 600;
  color: var(--red);
}

/* ══════════════════════════════════════════════════
   EMPTY STATE
══════════════════════════════════════════════════ */
.empty-state {
  padding: 40px;
  text-align: center;
  font-size: 13px;
  color: var(--muted-lt);
}

/* ══════════════════════════════════════════════════
   RESPONSIVE
══════════════════════════════════════════════════ */
@media (max-width: 1100px) {
  .rp-grid--2col { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .reports-root { padding: 14px 14px 40px; }
  .kpi-strip { grid-template-columns: 1fr 1fr; }
  .kpi-card__value { font-size: 13px; }
  .rp-header__right { display: none; }
  .filter-bar { flex-direction: column; align-items: stretch; }
  .filter-total { margin-left: 0; }
}
</style>