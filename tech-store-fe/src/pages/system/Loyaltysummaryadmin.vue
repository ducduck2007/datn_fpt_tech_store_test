<template>
  <div class="ls-root">

    <!-- ══ SIDEBAR ══════════════════════════════════════════════════════ -->
    <aside class="ls-sidebar">
      <div class="ls-brand">
        <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
          <rect x="1" y="1" width="9" height="9" rx="2" fill="#00d4aa"/>
          <rect x="12" y="1" width="9" height="9" rx="2" fill="#00d4aa" opacity=".4"/>
          <rect x="1" y="12" width="9" height="9" rx="2" fill="#00d4aa" opacity=".4"/>
          <rect x="12" y="12" width="9" height="9" rx="2" fill="#00d4aa" opacity=".2"/>
        </svg>
        <span class="ls-brand-name">LoyaltyOS</span>
      </div>

      <nav class="ls-nav">
        <button
          v-for="tab in tabs" :key="tab.id"
          class="ls-nav-item"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          <span class="ls-nav-icon" v-html="tab.icon"></span>
          <span class="ls-nav-label">{{ tab.label }}</span>
          <span v-if="activeTab === tab.id" class="ls-nav-pip"></span>
        </button>
      </nav>

      <div class="ls-sidebar-footer">
        <div class="ls-status-dot" :class="loading ? 'syncing' : 'live'"></div>
        <span>{{ loading ? 'Syncing…' : 'Live' }}</span>
      </div>
    </aside>

    <!-- ══ MAIN ══════════════════════════════════════════════════════════ -->
    <main class="ls-main">

      <!-- Top bar -->
      <header class="ls-topbar">
        <div class="ls-topbar-left">
          <h1 class="ls-page-title">{{ currentTab.label }}</h1>
          <div class="ls-breadcrumb">
            Analytics <span class="ls-bc-sep">›</span> Loyalty
            <span class="ls-bc-sep">›</span>
            {{ mode === 'monthly' ? 'Monthly' : 'Weekly' }}
          </div>
        </div>
        <div class="ls-topbar-right">
          <div class="ls-pill-toggle">
            <button v-for="m in modes" :key="m.value"
              :class="{ active: mode === m.value }"
              @click="setMode(m.value)">{{ m.label }}</button>
          </div>
          <select v-model="range" class="ls-select">
            <option v-for="n in rangeOptions" :key="n" :value="n">
              {{ n }} {{ mode === 'monthly' ? 'tháng' : 'tuần' }}
            </option>
          </select>
          <button class="ls-btn-refresh" :disabled="loading" @click="load">
            <svg :class="{ spin: loading }" width="13" height="13" viewBox="0 0 24 24"
              fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M23 4v6h-6M1 20v-6h6"/>
              <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
            </svg>
            {{ loading ? 'Loading' : 'Refresh' }}
          </button>
        </div>
      </header>

      <!-- Error -->
      <div v-if="error" class="ls-alert-error">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        {{ error }}
      </div>

      <!-- Skeleton -->
      <div v-if="loading" class="ls-skeleton-wrap">
        <div v-for="i in 4" :key="i" class="ls-skeleton-card"></div>
        <div class="ls-skeleton-chart"></div>
        <div class="ls-skeleton-chart"></div>
      </div>

      <template v-else-if="data.length">

        <!-- ── KPI ROW ── -->
        <div class="ls-kpi-row">
          <div v-for="k in kpis" :key="k.key" class="ls-kpi" :style="`--c:${k.color}`">
            <div class="ls-kpi-top">
              <span class="ls-kpi-label">{{ k.label }}</span>
              <span class="ls-kpi-delta" :class="k.deltaClass">{{ k.delta }}</span>
            </div>
            <div class="ls-kpi-value">{{ k.value }}</div>
            <div class="ls-kpi-sparkline">
              <svg :viewBox="`0 0 80 32`" preserveAspectRatio="none">
                <polygon :points="sparkArea(k.key)" :fill="k.color" opacity=".1" />
                <polyline :points="sparkPoints(k.key)" fill="none"
                  :stroke="k.color" stroke-width="1.6"
                  stroke-linecap="round" stroke-linejoin="round" opacity=".9"/>
              </svg>
            </div>
          </div>
        </div>

        <!-- ── TAB CONTENT ── -->
        <Transition name="tab-fade" mode="out-in">

          <!-- OVERVIEW TAB -->
          <div v-if="activeTab === 'overview'" key="overview" class="ls-content">
            <div class="ls-chart-row">
              <!-- Main combo chart -->
              <div class="ls-chart-card ls-chart-main">
                <div class="ls-chart-header">
                  <div>
                    <div class="ls-chart-title">Earn vs Deduct</div>
                    <div class="ls-chart-sub">Điểm tích lũy &amp; khấu trừ theo kỳ</div>
                  </div>
                  <div class="ls-legend">
                    <span class="ls-legend-item"><i style="background:#00d4aa"></i><span>Earn</span></span>
                    <span class="ls-legend-item"><i style="background:#ff6b6b"></i><span>Deduct</span></span>
                    <span class="ls-legend-item"><i style="background:#ffd93d;border-radius:50%"></i><span>Net</span></span>
                  </div>
                </div>
                <div class="ls-canvas-wrap" style="height:260px">
                  <canvas ref="comboChartRef"></canvas>
                </div>
              </div>

              <!-- Donut chart -->
              <div class="ls-chart-card ls-chart-donut">
                <div class="ls-chart-header">
                  <div>
                    <div class="ls-chart-title">Tier Distribution</div>
                    <div class="ls-chart-sub">Điểm cộng theo hạng VIP</div>
                  </div>
                </div>
                <div class="ls-canvas-wrap" style="height:180px">
                  <canvas ref="donutChartRef"></canvas>
                </div>
                <div class="ls-donut-legend">
                  <div v-for="(t, i) in tierDonutData" :key="t.tier" class="ls-donut-row">
                    <span class="ls-donut-dot" :style="`background:${TIER_COLORS[i % TIER_COLORS.length]}`"></span>
                    <span class="ls-donut-name">{{ t.tier }}</span>
                    <span class="ls-donut-val">{{ fmt(t.value) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Net trend -->
            <div class="ls-chart-card" style="margin-top:16px">
              <div class="ls-chart-header">
                <div>
                  <div class="ls-chart-title">Net Points Trend</div>
                  <div class="ls-chart-sub">Điểm thuần qua từng kỳ</div>
                </div>
              </div>
              <div class="ls-canvas-wrap" style="height:130px">
                <canvas ref="netChartRef"></canvas>
              </div>
            </div>
          </div>

          <!-- BREAKDOWN TAB -->
          <div v-else-if="activeTab === 'table'" key="table" class="ls-content">
            <div class="ls-table-layout" :class="{ 'show-detail': !!selected }">

              <!-- Period list -->
              <div class="ls-card ls-period-list">
                <div class="ls-card-header">
                  <span class="ls-card-title">Danh sách kỳ</span>
                  <span class="ls-badge">{{ data.length }} kỳ</span>
                </div>
                <div class="ls-period-head">
                  <span>Kỳ</span><span>Earn</span><span>Deduct</span><span>Net</span><span>GD</span>
                </div>
                <div v-for="item in data" :key="item.period"
                  class="ls-period-row"
                  :class="{ active: selectedPeriod === item.period }"
                  @click="togglePeriod(item.period)">
                  <div>
                    <div class="ls-pr-label">{{ item.periodLabel }}</div>
                    <div class="ls-pr-range">{{ item.periodStart }} – {{ item.periodEnd }}</div>
                  </div>
                  <span class="ls-num earn">+{{ fmt(item.totalPointsEarned) }}</span>
                  <span class="ls-num deduct">-{{ fmt(item.totalPointsDeducted) }}</span>
                  <span class="ls-num" :class="item.netPoints >= 0 ? 'net-pos' : 'net-neg'">
                    {{ item.netPoints >= 0 ? '+' : '' }}{{ fmt(item.netPoints) }}
                  </span>
                  <span class="ls-num muted">{{ item.totalTransactions }}</span>
                </div>
              </div>

              <!-- Detail panel -->
              <Transition name="slide-in">
                <div v-if="selected" class="ls-card ls-breakdown">
                  <div class="ls-card-header">
                    <div>
                      <div class="ls-card-title">{{ selected.periodLabel }}</div>
                      <div class="ls-card-sub">{{ selected.customerBreakdown?.length ?? 0 }} khách hàng</div>
                    </div>
                    <button class="ls-close-btn" @click="selectedPeriod = null">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none"
                        stroke="currentColor" stroke-width="2.5">
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                    </button>
                  </div>

                  <div class="ls-mini-stats">
                    <div class="ls-mini-stat">
                      <span class="ls-mini-label">Earn</span>
                      <span class="ls-mini-val earn">+{{ fmt(selected.totalPointsEarned) }}</span>
                    </div>
                    <div class="ls-mini-stat">
                      <span class="ls-mini-label">Deduct</span>
                      <span class="ls-mini-val deduct">-{{ fmt(selected.totalPointsDeducted) }}</span>
                    </div>
                    <div class="ls-mini-stat">
                      <span class="ls-mini-label">Net</span>
                      <span class="ls-mini-val"
                        :class="selected.netPoints >= 0 ? 'net-pos' : 'net-neg'">
                        {{ selected.netPoints >= 0 ? '+' : '' }}{{ fmt(selected.netPoints) }}
                      </span>
                    </div>
                    <div class="ls-mini-stat">
                      <span class="ls-mini-label">GD</span>
                      <span class="ls-mini-val muted">{{ selected.totalTransactions }}</span>
                    </div>
                  </div>

                  <div class="ls-breakdown-scroll">
                    <table v-if="selected.customerBreakdown?.length" class="ls-btable">
                      <thead>
                        <tr>
                          <th>Khách hàng</th><th>Hạng</th>
                          <th>Earn</th><th>Deduct</th><th>Net</th><th>GD</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(r, i) in selected.customerBreakdown" :key="i">
                          <td>
                            <div class="ls-cust-name">{{ r.customerName }}</div>
                            <div class="ls-cust-email">{{ r.customerEmail }}</div>
                          </td>
                          <td>
                            <span class="ls-tier-chip"
                              :style="`--tc:${tierColor(r.vipTier)}`">
                              {{ r.vipTier || 'Member' }}
                            </span>
                          </td>
                          <td class="ls-num earn">+{{ fmt(r.pointsEarned) }}</td>
                          <td class="ls-num deduct">-{{ fmt(r.pointsDeducted) }}</td>
                          <td class="ls-num" :class="r.netPoints >= 0 ? 'net-pos' : 'net-neg'">
                            {{ r.netPoints >= 0 ? '+' : '' }}{{ fmt(r.netPoints) }}
                          </td>
                          <td class="ls-num muted">{{ r.transactionCount }}</td>
                        </tr>
                      </tbody>
                    </table>
                    <div v-else class="ls-empty-small">Không có dữ liệu.</div>
                  </div>
                </div>
              </Transition>
            </div>
          </div>

          <!-- RANKING TAB -->
          <div v-else-if="activeTab === 'ranking'" key="ranking" class="ls-content">
            <div class="ls-chart-row">
              <div class="ls-chart-card" style="flex:1.3">
                <div class="ls-chart-header">
                  <div>
                    <div class="ls-chart-title">Top Earners</div>
                    <div class="ls-chart-sub">Khách hàng tích điểm nhiều nhất</div>
                  </div>
                </div>
                <div class="ls-canvas-wrap" style="height:280px">
                  <canvas ref="topEarnerChartRef"></canvas>
                </div>
              </div>
              <div class="ls-chart-card" style="flex:1">
                <div class="ls-chart-header">
                  <div>
                    <div class="ls-chart-title">Earn Rate %</div>
                    <div class="ls-chart-sub">Tỷ lệ Earn / (Earn + Deduct)</div>
                  </div>
                </div>
                <div class="ls-canvas-wrap" style="height:280px">
                  <canvas ref="rateChartRef"></canvas>
                </div>
              </div>
            </div>
          </div>

        </Transition>
      </template>

      <!-- Empty state -->
      <div v-else-if="!loading" class="ls-empty">
        <svg width="38" height="38" viewBox="0 0 24 24" fill="none"
          stroke="#374151" stroke-width="1.5">
          <rect x="3" y="3" width="18" height="18" rx="3"/>
          <path d="M9 9h6M9 12h6M9 15h4"/>
        </svg>
        <p>Không có dữ liệu trong khoảng thời gian này.</p>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import { customersApi } from '../../api/customers.api'

// ─── Chart.js ────────────────────────────────────────────────────────────────
const getChart = () => (typeof Chart !== 'undefined' ? Chart : null)

// ─── Constants ────────────────────────────────────────────────────────────────
const TIER_COLORS = ['#00d4aa', '#ffd93d', '#ff6b6b', '#a78bfa', '#60a5fa', '#fb923c']
const TIER_META = {
  BRONZE: '#cd7f32', SILVER: '#9ca3af', GOLD: '#f59e0b',
  PLATINUM: '#6366f1', DIAMOND: '#06b6d4', Member: '#6b7280',
}
const tierColor = (t) => TIER_META[t] ?? TIER_META.Member

const ICON_GRID = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>`
const ICON_TABLE = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 3H5a2 2 0 00-2 2v4m6-6h10a2 2 0 012 2v4M9 3v18m0 0h10a2 2 0 002-2V9M9 21H5a2 2 0 01-2-2V9m0 0h18"/></svg>`
const ICON_TREND = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></svg>`

const tabs = [
  { id: 'overview', label: 'Overview',  icon: ICON_GRID  },
  { id: 'table',    label: 'Breakdown', icon: ICON_TABLE },
  { id: 'ranking',  label: 'Ranking',   icon: ICON_TREND },
]
const modes = [
  { value: 'monthly', label: 'Monthly' },
  { value: 'weekly',  label: 'Weekly'  },
]

// ─── State ────────────────────────────────────────────────────────────────────
const mode           = ref('monthly')
const range          = ref(12)
const data           = ref([])
const loading        = ref(false)
const error          = ref(null)
const selectedPeriod = ref(null)
const activeTab      = ref('overview')

const comboChartRef     = ref(null)
const donutChartRef     = ref(null)
const netChartRef       = ref(null)
const topEarnerChartRef = ref(null)
const rateChartRef      = ref(null)

let charts = {}

// ─── Computed ─────────────────────────────────────────────────────────────────
const rangeOptions = computed(() =>
  mode.value === 'monthly' ? [3, 6, 12, 24] : [4, 8, 12, 24, 52]
)
const currentTab = computed(() => tabs.find(t => t.id === activeTab.value))
const selected   = computed(() => data.value.find(d => d.period === selectedPeriod.value))

const totalEarned   = computed(() => data.value.reduce((s, d) => s + (d.totalPointsEarned   || 0), 0))
const totalDeducted = computed(() => data.value.reduce((s, d) => s + (d.totalPointsDeducted || 0), 0))
const totalTx       = computed(() => data.value.reduce((s, d) => s + (d.totalTransactions   || 0), 0))
const netTotal      = computed(() => totalEarned.value - totalDeducted.value)

const tierDonutData = computed(() => {
  const map = {}
  data.value.forEach(p => {
    ;(p.customerBreakdown || []).forEach(c => {
      const t = c.vipTier || 'Member'
      map[t] = (map[t] || 0) + (c.pointsEarned || 0)
    })
  })
  return Object.entries(map)
    .map(([tier, value]) => ({ tier, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 6)
})

const topEarners = computed(() => {
  const map = {}
  data.value.forEach(p => {
    ;(p.customerBreakdown || []).forEach(c => {
      if (!map[c.customerName]) map[c.customerName] = { earn: 0, deduct: 0 }
      map[c.customerName].earn   += c.pointsEarned   || 0
      map[c.customerName].deduct += c.pointsDeducted || 0
    })
  })
  return Object.entries(map)
    .map(([name, v]) => ({ name, ...v }))
    .sort((a, b) => b.earn - a.earn)
    .slice(0, 8)
})

// ─── KPI sparkline ────────────────────────────────────────────────────────────
function sparkPoints(key) {
  const vals = [...data.value].reverse().map(d => d[key] || 0)
  const max = Math.max(...vals, 1)
  return vals.map((v, i) => {
    const x = (i / Math.max(vals.length - 1, 1)) * 80
    const y = 32 - (v / max) * 28
    return `${x},${y}`
  }).join(' ')
}
function sparkArea(key) {
  const vals = [...data.value].reverse().map(d => d[key] || 0)
  const max = Math.max(...vals, 1)
  const pts = vals.map((v, i) => {
    const x = (i / Math.max(vals.length - 1, 1)) * 80
    const y = 32 - (v / max) * 28
    return `${x},${y}`
  })
  return `0,32 ${pts.join(' ')} 80,32`
}

function calcDelta(key) {
  if (data.value.length < 2) return null
  const cur  = data.value[0][key] || 0
  const prev = data.value[1][key] || 1
  const pct  = ((cur - prev) / Math.abs(prev) * 100).toFixed(1)
  return { val: pct, pos: cur >= prev }
}

const kpis = computed(() => {
  const dE = calcDelta('totalPointsEarned')
  const dD = calcDelta('totalPointsDeducted')
  const dT = calcDelta('totalTransactions')
  return [
    {
      key: 'totalPointsEarned', label: 'Total Earned', color: '#00d4aa',
      value: '+' + fmt(totalEarned.value),
      delta: dE ? (dE.pos ? '↑' : '↓') + Math.abs(dE.val) + '%' : '—',
      deltaClass: dE?.pos ? 'up' : 'dn',
    },
    {
      key: 'totalPointsDeducted', label: 'Total Deducted', color: '#ff6b6b',
      value: '-' + fmt(totalDeducted.value),
      delta: dD ? (dD.pos ? '↑' : '↓') + Math.abs(dD.val) + '%' : '—',
      deltaClass: !dD?.pos ? 'up' : 'dn',
    },
    {
      key: 'netPoints', label: 'Net Points', color: '#ffd93d',
      value: (netTotal.value >= 0 ? '+' : '') + fmt(netTotal.value),
      delta: '—', deltaClass: '',
    },
    {
      key: 'totalTransactions', label: 'Transactions', color: '#a78bfa',
      value: fmt(totalTx.value),
      delta: dT ? (dT.pos ? '↑' : '↓') + Math.abs(dT.val) + '%' : '—',
      deltaClass: dT?.pos ? 'up' : 'dn',
    },
  ]
})

// ─── Helpers ──────────────────────────────────────────────────────────────────
const fmt = (n) => (n ?? 0).toLocaleString('vi-VN')
const setMode = (m) => { mode.value = m; range.value = 12 }
const togglePeriod = (p) => { selectedPeriod.value = selectedPeriod.value === p ? null : p }

// ─── Load ─────────────────────────────────────────────────────────────────────
async function load() {
  loading.value = true
  error.value = null
  selectedPeriod.value = null
  try {
    const res = mode.value === 'weekly'
      ? await customersApi.getLoyaltyWeeklySummaryAdmin(range.value)
      : await customersApi.getLoyaltyMonthlySummaryAdmin(range.value)
    data.value = (res.data ?? []).reverse()
    await nextTick()
    buildCharts()
  } catch {
    error.value = 'Không thể tải dữ liệu. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}

// ─── Chart builder ────────────────────────────────────────────────────────────
function destroyAll() {
  Object.values(charts).forEach(c => c?.destroy())
  charts = {}
}

const FONT  = "'IBM Plex Mono', monospace"
const GRID  = 'rgba(255,255,255,0.04)'
const TICK  = '#4b5563'
const TT_BASE = {
  backgroundColor: '#0d1117',
  borderColor: '#1f2937',
  borderWidth: 1,
  titleColor: '#9ca3af',
  bodyColor: '#e5e7eb',
  titleFont: { family: FONT, size: 11 },
  bodyFont:  { family: FONT, size: 11 },
  padding: 10,
}

function buildCharts() {
  destroyAll()
  const C = getChart()
  if (!C) { console.warn('Chart.js not found'); return }

  const reversed = [...data.value].reverse()
  const labels   = reversed.map(d => d.periodLabel)
  const earned   = reversed.map(d => d.totalPointsEarned   || 0)
  const deducted = reversed.map(d => d.totalPointsDeducted || 0)
  const net      = reversed.map(d => d.netPoints            || 0)

  const xScale = {
    grid: { color: GRID },
    ticks: { color: TICK, font: { family: FONT, size: 10 }, maxRotation: 45 },
  }
  const yScale = {
    grid: { color: GRID },
    ticks: { color: TICK, font: { family: FONT, size: 10 }, callback: v => fmt(v) },
    border: { display: false },
  }

  // ── 1. Combo bar + line ──
  if (comboChartRef.value) {
    charts.combo = new C(comboChartRef.value, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          { label: 'Earn',   data: earned,   backgroundColor: 'rgba(0,212,170,0.72)', borderRadius: 3, borderSkipped: false, order: 2 },
          { label: 'Deduct', data: deducted, backgroundColor: 'rgba(255,107,107,0.7)', borderRadius: 3, borderSkipped: false, order: 2 },
          {
            label: 'Net', data: net, type: 'line',
            borderColor: '#ffd93d', backgroundColor: 'rgba(255,217,61,0.06)',
            borderWidth: 2, pointRadius: 3, pointBackgroundColor: '#ffd93d',
            tension: 0.4, fill: false, order: 1,
          },
        ],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: { legend: { display: false }, tooltip: { ...TT_BASE, callbacks: { label: ctx => ` ${ctx.dataset.label}: ${fmt(ctx.raw)}` } } },
        scales: { x: xScale, y: yScale },
      },
    })
  }

  // ── 2. Donut ──
  if (donutChartRef.value && tierDonutData.value.length) {
    charts.donut = new C(donutChartRef.value, {
      type: 'doughnut',
      data: {
        labels: tierDonutData.value.map(t => t.tier),
        datasets: [{
          data: tierDonutData.value.map(t => t.value),
          backgroundColor: TIER_COLORS,
          borderWidth: 2,
          borderColor: '#0d1117',
        }],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        cutout: '70%',
        plugins: { legend: { display: false }, tooltip: { ...TT_BASE, callbacks: { label: ctx => ` ${ctx.label}: ${fmt(ctx.raw)}` } } },
      },
    })
  }

  // ── 3. Net area ──
  if (netChartRef.value) {
    charts.net = new C(netChartRef.value, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Net', data: net,
            borderColor: '#00d4aa', backgroundColor: (ctx) => {
              const g = ctx.chart.ctx.createLinearGradient(0, 0, 0, 130)
              g.addColorStop(0, 'rgba(0,212,170,0.18)')
              g.addColorStop(1, 'rgba(0,212,170,0)')
              return g
            },
            borderWidth: 1.8, fill: true, tension: 0.4,
            pointRadius: 3, pointBackgroundColor: '#00d4aa', pointBorderColor: '#0d1117', pointBorderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: { legend: { display: false }, tooltip: { ...TT_BASE, callbacks: { label: ctx => ` Net: ${fmt(ctx.raw)}` } } },
        scales: {
          x: { display: false },
          y: { grid: { color: GRID }, ticks: { color: TICK, font: { family: FONT, size: 10 }, callback: v => fmt(v) }, border: { display: false } },
        },
      },
    })
  }

  // ── 4. Top earners horizontal bar ──
  if (topEarnerChartRef.value && topEarners.value.length) {
    charts.topEarner = new C(topEarnerChartRef.value, {
      type: 'bar',
      data: {
        labels: topEarners.value.map(e => e.name.length > 16 ? e.name.slice(0, 16) + '…' : e.name),
        datasets: [
          { label: 'Earn',   data: topEarners.value.map(e => e.earn),   backgroundColor: 'rgba(0,212,170,0.75)',  borderRadius: 3, borderSkipped: false },
          { label: 'Deduct', data: topEarners.value.map(e => e.deduct), backgroundColor: 'rgba(255,107,107,0.6)', borderRadius: 3, borderSkipped: false },
        ],
      },
      options: {
        indexAxis: 'y',
        responsive: true, maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: { legend: { display: false }, tooltip: { ...TT_BASE, callbacks: { label: ctx => ` ${ctx.dataset.label}: ${fmt(ctx.raw)}` } } },
        scales: {
          x: { grid: { color: GRID }, ticks: { color: TICK, font: { family: FONT, size: 10 }, callback: v => fmt(v) }, border: { display: false } },
          y: { grid: { display: false }, ticks: { color: '#d1d5db', font: { family: "'Outfit', sans-serif", size: 12 } } },
        },
      },
    })
  }

  // ── 5. Earn rate line ──
  if (rateChartRef.value) {
    const rate = reversed.map(d => {
      const total = (d.totalPointsEarned || 0) + (d.totalPointsDeducted || 0)
      return total > 0 ? +((d.totalPointsEarned / total) * 100).toFixed(1) : 0
    })
    charts.rate = new C(rateChartRef.value, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Earn Rate %', data: rate,
          borderColor: '#a78bfa',
          backgroundColor: (ctx) => {
            const g = ctx.chart.ctx.createLinearGradient(0, 0, 0, 280)
            g.addColorStop(0, 'rgba(167,139,250,0.15)')
            g.addColorStop(1, 'rgba(167,139,250,0)')
            return g
          },
          borderWidth: 2, fill: true, tension: 0.4,
          pointRadius: 4, pointBackgroundColor: '#a78bfa',
          pointBorderColor: '#0d1117', pointBorderWidth: 2,
        }],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { ...TT_BASE, callbacks: { label: ctx => ` Earn Rate: ${ctx.raw}%` } } },
        scales: {
          x: xScale,
          y: { min: 0, max: 100, grid: { color: GRID }, ticks: { color: TICK, font: { family: FONT, size: 10 }, callback: v => v + '%' }, border: { display: false } },
        },
      },
    })
  }
}

// Rebuild when switching to ranking tab (canvas mounts after transition)
watch(activeTab, async () => {
  await nextTick()
  if (data.value.length) buildCharts()
})

watch([mode, range], load, { immediate: true })
onBeforeUnmount(destroyAll)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&family=IBM+Plex+Mono:wght@400;500&display=swap');

/* ══ VARIABLES ══════════════════════════════════════════════════════ */
.ls-root {
  --bg0: #07090e;
  --bg1: #0d1117;
  --bg2: #111827;
  --bg3: #1a2235;
  --border: #1f2937;
  --border-hi: #2d3748;
  --text-1: #f0f4f8;
  --text-2: #9ca3af;
  --text-3: #4b5563;
  --accent: #00d4aa;
  --red: #ff6b6b;
  --yellow: #ffd93d;
  --purple: #a78bfa;
}

/* ══ ROOT LAYOUT ══════════════════════════════════════════════════════ */
.ls-root {
  display: flex;
  min-height: 100vh;
  background: var(--bg0);
  font-family: 'Outfit', sans-serif;
  color: var(--text-1);
  box-sizing: border-box;
}
*, *::before, *::after { box-sizing: border-box; }

/* ══ SIDEBAR ══════════════════════════════════════════════════════════ */
.ls-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--bg1);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  padding: 24px 0;
}
.ls-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px 24px;
  border-bottom: 1px solid var(--border);
}
.ls-brand-name {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: .04em;
}
.ls-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.ls-nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: var(--text-2);
  font-family: 'Outfit', sans-serif;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all .15s;
  text-align: left;
  width: 100%;
}
.ls-nav-item:hover  { background: var(--bg2); color: var(--text-1); }
.ls-nav-item.active { background: rgba(0,212,170,.08); color: var(--accent); }
.ls-nav-icon { flex-shrink: 0; opacity: .7; }
.ls-nav-item.active .ls-nav-icon { opacity: 1; }
.ls-nav-label { flex: 1; }
.ls-nav-pip {
  width: 5px; height: 5px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 6px var(--accent);
}
.ls-sidebar-footer {
  padding: 16px 20px 0;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: var(--text-3);
  letter-spacing: .06em;
  text-transform: uppercase;
}
.ls-status-dot { width: 7px; height: 7px; border-radius: 50%; }
.ls-status-dot.live    { background: var(--accent); box-shadow: 0 0 6px var(--accent); }
.ls-status-dot.syncing { background: var(--yellow); animation: blink 1s ease infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.3} }

/* ══ MAIN ══════════════════════════════════════════════════════════════ */
.ls-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

/* ── Topbar ── */
.ls-topbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 22px 28px 18px;
  border-bottom: 1px solid var(--border);
  background: var(--bg1);
  flex-wrap: wrap;
  gap: 14px;
}
.ls-page-title { font-size: 20px; font-weight: 700; margin: 0 0 4px; letter-spacing: -.02em; }
.ls-breadcrumb { font-size: 11px; color: var(--text-3); letter-spacing: .06em; text-transform: uppercase; }
.ls-bc-sep { margin: 0 5px; opacity: .35; }
.ls-topbar-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.ls-pill-toggle {
  display: flex;
  background: var(--bg2);
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
  padding: 3px;
  gap: 2px;
}
.ls-pill-toggle button {
  background: transparent;
  border: none;
  padding: 5px 14px;
  font-family: 'Outfit', sans-serif;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-2);
  cursor: pointer;
  border-radius: 5px;
  transition: all .15s;
  letter-spacing: .03em;
}
.ls-pill-toggle button.active { background: var(--accent); color: #000; font-weight: 700; }

.ls-select {
  background: var(--bg2);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text-1);
  padding: 7px 12px;
  font-family: 'IBM Plex Mono', monospace;
  font-size: 12px;
  cursor: pointer;
  outline: none;
}
.ls-select:focus { border-color: var(--accent); }

.ls-btn-refresh {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text-2);
  padding: 7px 14px;
  font-family: 'Outfit', sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;
  letter-spacing: .03em;
}
.ls-btn-refresh:hover:not(:disabled) { border-color: var(--accent); color: var(--accent); }
.ls-btn-refresh:disabled { opacity: .4; cursor: not-allowed; }
@keyframes spin-r { to { transform: rotate(360deg); } }
.spin { animation: spin-r .8s linear infinite; }

/* ── Alert / Empty ── */
.ls-alert-error {
  margin: 14px 28px 0;
  padding: 10px 16px;
  background: rgba(255,107,107,.07);
  border: 1px solid rgba(255,107,107,.28);
  border-radius: 8px;
  color: #fca5a5;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.ls-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: var(--text-3);
  gap: 12px;
  font-size: 14px;
}

/* ── Skeleton ── */
.ls-skeleton-wrap {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  padding: 20px 28px;
}
@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}
.ls-skeleton-card,
.ls-skeleton-chart {
  background: linear-gradient(90deg, var(--bg2) 0%, var(--bg3) 50%, var(--bg2) 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease infinite;
  border-radius: 10px;
}
.ls-skeleton-card  { height: 100px; }
.ls-skeleton-chart { height: 240px; grid-column: span 2; }

/* ── KPI Row ── */
.ls-kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  padding: 20px 28px 0;
}
.ls-kpi {
  background: var(--bg1);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px 18px 0;
  overflow: hidden;
  transition: border-color .2s;
}
.ls-kpi:hover { border-color: var(--border-hi); }
.ls-kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.ls-kpi-label {
  font-size: 10px;
  color: var(--text-2);
  text-transform: uppercase;
  letter-spacing: .1em;
  font-weight: 600;
}
.ls-kpi-delta {
  font-size: 10px;
  font-family: 'IBM Plex Mono', monospace;
  padding: 2px 6px;
  border-radius: 4px;
}
.ls-kpi-delta.up { background: rgba(0,212,170,.1);  color: var(--accent); }
.ls-kpi-delta.dn { background: rgba(255,107,107,.1); color: var(--red); }
.ls-kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--c, var(--accent));
  font-family: 'IBM Plex Mono', monospace;
  letter-spacing: -.02em;
  line-height: 1;
  margin-bottom: 10px;
}
.ls-kpi-sparkline { height: 36px; margin: 0 -18px; }
.ls-kpi-sparkline svg { width: 100%; height: 100%; display: block; }

/* ── Content ── */
.ls-content { padding: 18px 28px 28px; }

/* ── Chart cards ── */
.ls-chart-row { display: flex; gap: 16px; align-items: stretch; }
.ls-chart-card {
  background: var(--bg1);
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
  flex: 1;
}
.ls-chart-main  { flex: 2; }
.ls-chart-donut { flex: 1; display: flex; flex-direction: column; }
.ls-chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 14px 18px 12px;
  border-bottom: 1px solid var(--border);
}
.ls-chart-title { font-size: 13px; font-weight: 600; }
.ls-chart-sub   { font-size: 11px; color: var(--text-3); margin-top: 3px; }
.ls-canvas-wrap { padding: 16px 18px; position: relative; }
.ls-canvas-wrap canvas { width: 100% !important; height: 100% !important; display: block; }

.ls-legend { display: flex; gap: 14px; align-items: center; }
.ls-legend-item {
  display: flex; align-items: center; gap: 5px;
  font-size: 11px; color: var(--text-2);
  font-family: 'IBM Plex Mono', monospace;
}
.ls-legend-item i { width: 10px; height: 10px; border-radius: 2px; display: inline-block; }

.ls-donut-legend { padding: 0 18px 16px; display: flex; flex-direction: column; gap: 6px; }
.ls-donut-row { display: flex; align-items: center; gap: 8px; font-size: 12px; }
.ls-donut-dot  { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.ls-donut-name { flex: 1; color: var(--text-2); }
.ls-donut-val  { font-family: 'IBM Plex Mono', monospace; font-size: 11px; }

/* ── Table tab ── */
.ls-table-layout { display: grid; grid-template-columns: 1fr; gap: 16px; }
.ls-table-layout.show-detail { grid-template-columns: 1fr 1fr; }

.ls-card { background: var(--bg1); border: 1px solid var(--border); border-radius: 10px; overflow: hidden; }
.ls-card-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
}
.ls-card-title { font-size: 13px; font-weight: 600; }
.ls-card-sub   { font-size: 11px; color: var(--text-3); margin-top: 2px; }
.ls-badge {
  font-size: 11px;
  font-family: 'IBM Plex Mono', monospace;
  background: var(--bg2);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 2px 10px;
  color: var(--text-2);
}

.ls-period-head {
  display: grid;
  grid-template-columns: 1fr 100px 100px 100px 60px;
  padding: 8px 18px;
  font-size: 10px; color: var(--text-3);
  letter-spacing: .08em; text-transform: uppercase; font-weight: 600;
  background: var(--bg0);
  border-bottom: 1px solid var(--border);
}
.ls-period-row {
  display: grid;
  grid-template-columns: 1fr 100px 100px 100px 60px;
  align-items: center;
  padding: 13px 18px;
  border-bottom: 1px solid rgba(31,41,55,.6);
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: background .12s, border-color .12s;
}
.ls-period-row:last-child { border-bottom: none; }
.ls-period-row:hover  { background: rgba(17,24,39,.7); }
.ls-period-row.active { background: rgba(0,212,170,.04); border-left-color: var(--accent); }
.ls-pr-label { font-size: 13px; color: var(--text-1); font-weight: 600; }
.ls-pr-range { font-size: 10px; color: var(--text-3); margin-top: 2px; font-family: 'IBM Plex Mono', monospace; }

/* Numbers */
.ls-num { font-family: 'IBM Plex Mono', monospace; font-size: 12px; }
.earn    { color: var(--accent); }
.deduct  { color: var(--red); }
.net-pos { color: #60a5fa; font-weight: 700; }
.net-neg { color: var(--red); font-weight: 700; }
.muted   { color: var(--text-3); }

/* Breakdown panel */
.ls-breakdown { display: flex; flex-direction: column; max-height: 620px; position: sticky; top: 0; }
.ls-close-btn {
  background: var(--bg2); border: 1px solid var(--border);
  border-radius: 6px; color: var(--text-2); cursor: pointer;
  padding: 6px; display: flex; transition: all .15s;
}
.ls-close-btn:hover { color: var(--text-1); border-color: var(--border-hi); }

.ls-mini-stats {
  display: grid; grid-template-columns: repeat(4, 1fr);
  border-bottom: 1px solid var(--border);
}
.ls-mini-stat { padding: 12px 16px; border-right: 1px solid var(--border); }
.ls-mini-stat:last-child { border-right: none; }
.ls-mini-label { font-size: 9px; color: var(--text-3); letter-spacing: .1em; text-transform: uppercase; }
.ls-mini-val   { font-family: 'IBM Plex Mono', monospace; font-size: 15px; font-weight: 700; margin-top: 4px; }

.ls-breakdown-scroll { overflow-y: auto; flex: 1; }
.ls-btable { width: 100%; border-collapse: collapse; font-size: 12px; }
.ls-btable th {
  padding: 8px 12px; text-align: left;
  color: var(--text-3); font-size: 10px;
  letter-spacing: .08em; text-transform: uppercase; font-weight: 600;
  border-bottom: 1px solid var(--border);
  background: var(--bg0);
  position: sticky; top: 0; z-index: 1;
}
.ls-btable td {
  padding: 10px 12px;
  border-bottom: 1px solid rgba(31,41,55,.45);
  vertical-align: middle;
}
.ls-btable tr:last-child td { border-bottom: none; }
.ls-btable tr:hover td { background: var(--bg2); }
.ls-cust-name  { font-size: 12px; color: var(--text-1); font-weight: 500; white-space: nowrap; }
.ls-cust-email { font-size: 10px; color: var(--text-3); font-family: 'IBM Plex Mono', monospace; margin-top: 2px; }

.ls-tier-chip {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px; font-weight: 700;
  letter-spacing: .05em; text-transform: uppercase;
  color: var(--tc);
  background: color-mix(in srgb, var(--tc) 12%, transparent);
  border: 1px solid color-mix(in srgb, var(--tc) 28%, transparent);
}
.ls-empty-small { padding: 24px 18px; color: var(--text-3); font-size: 13px; }

/* ── Transitions ── */
.tab-fade-enter-active, .tab-fade-leave-active { transition: opacity .18s, transform .18s; }
.tab-fade-enter-from, .tab-fade-leave-to       { opacity: 0; transform: translateY(8px); }

.slide-in-enter-active, .slide-in-leave-active { transition: opacity .2s, transform .2s; }
.slide-in-enter-from { opacity: 0; transform: translateX(18px); }
.slide-in-leave-to   { opacity: 0; transform: translateX(18px); }

/* ── Responsive ── */
@media (max-width: 1100px) {
  .ls-kpi-row   { grid-template-columns: repeat(2, 1fr); }
  .ls-chart-row { flex-direction: column; }
}
@media (max-width: 768px) {
  .ls-sidebar { display: none; }
  .ls-kpi-row { grid-template-columns: 1fr 1fr; padding: 14px; }
  .ls-content { padding: 14px; }
  .ls-table-layout.show-detail { grid-template-columns: 1fr; }
  .ls-topbar { padding: 16px; }
}
</style>