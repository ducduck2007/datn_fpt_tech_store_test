<template>
  <div class="ls-root">

    <!-- ── SIDEBAR ──────────────────────────────────────────────── -->
    <aside class="ls-sidebar">
      <div class="ls-brand">
        <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
          <rect width="28" height="28" rx="8" fill="#111827"/>
          <rect x="6" y="6" width="7" height="7" rx="2" fill="#3b82f6"/>
          <rect x="15" y="6" width="7" height="7" rx="2" fill="#3b82f6" opacity=".4"/>
          <rect x="6" y="15" width="7" height="7" rx="2" fill="#3b82f6" opacity=".4"/>
          <rect x="15" y="15" width="7" height="7" rx="2" fill="#3b82f6" opacity=".2"/>
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
        <span class="ls-status-dot" :class="loading ? 'syncing' : 'live'"></span>
        <span>{{ loading ? 'Syncing…' : 'Live' }}</span>
      </div>
    </aside>

    <!-- ── MAIN ─────────────────────────────────────────────────── -->
    <main class="ls-main">

      <!-- Topbar -->
      <header class="ls-topbar">
        <div class="ls-topbar-left">
          <div class="ls-breadcrumb">Analytics <span>›</span> Loyalty <span>›</span> {{ mode === 'monthly' ? 'Monthly' : 'Weekly' }}</div>
          <h1 class="ls-page-title">{{ currentTab.label }}</h1>
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
            <svg :class="{ spin: loading }" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M23 4v6h-6M1 20v-6h6"/>
              <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
            </svg>
            {{ loading ? 'Loading…' : 'Refresh' }}
          </button>
        </div>
      </header>

      <!-- Error -->
      <div v-if="error" class="ls-alert-error">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        {{ error }}
      </div>

      <!-- Skeleton -->
      <div v-if="loading" class="ls-body">
        <div class="ls-kpi-row">
          <div v-for="i in 4" :key="i" class="ls-skeleton" style="height:100px; border-radius:14px"/>
        </div>
        <div style="display:grid;grid-template-columns:2fr 1fr;gap:14px;margin-top:14px">
          <div class="ls-skeleton" style="height:280px;border-radius:14px"/>
          <div class="ls-skeleton" style="height:280px;border-radius:14px"/>
        </div>
      </div>

      <template v-else-if="data.length">
        <!-- KPI Row -->
        <div class="ls-body">
          <div class="ls-kpi-row">
            <div v-for="k in kpis" :key="k.key" class="ls-kpi" :style="`--accent:${k.color}`">
              <div class="ls-kpi-top">
                <span class="ls-kpi-icon" :style="`background:${k.color}18;color:${k.color}`" v-html="k.icon"></span>
                <span class="ls-kpi-delta" :class="k.deltaClass">{{ k.delta }}</span>
              </div>
              <div class="ls-kpi-value">{{ k.value }}</div>
              <div class="ls-kpi-label">{{ k.label }}</div>
              <div class="ls-kpi-spark">
                <svg viewBox="0 0 80 32" preserveAspectRatio="none">
                  <polygon :points="sparkArea(k.key)" :fill="k.color" opacity=".08"/>
                  <polyline :points="sparkPoints(k.key)" fill="none" :stroke="k.color" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
          </div>

          <!-- Tab Content -->
          <Transition name="tab-fade" mode="out-in">

            <!-- OVERVIEW -->
            <div v-if="activeTab === 'overview'" key="overview">
              <div class="ls-chart-row">
                <div class="ls-card ls-chart-main">
                  <div class="ls-card-header">
                    <div>
                      <div class="ls-card-title">Earn vs Deduct</div>
                      <div class="ls-card-sub">Điểm tích lũy &amp; khấu trừ theo kỳ</div>
                    </div>
                    <div class="ls-legend">
                      <span class="ls-legend-dot" style="background:#3b82f6"></span><span>Earn</span>
                      <span class="ls-legend-dot" style="background:#ef4444"></span><span>Deduct</span>
                      <span class="ls-legend-dot" style="background:#f59e0b;border-radius:50%"></span><span>Net</span>
                    </div>
                  </div>
                  <div class="ls-canvas-wrap" style="height:240px">
                    <canvas ref="comboChartRef"></canvas>
                  </div>
                </div>

                <div class="ls-card ls-chart-side">
                  <div class="ls-card-header">
                    <div>
                      <div class="ls-card-title">Tier Distribution</div>
                      <div class="ls-card-sub">Điểm cộng theo hạng VIP</div>
                    </div>
                  </div>
                  <div class="ls-canvas-wrap" style="height:160px">
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

              <div class="ls-card" style="margin-top:14px">
                <div class="ls-card-header">
                  <div>
                    <div class="ls-card-title">Net Points Trend</div>
                    <div class="ls-card-sub">Điểm thuần qua từng kỳ</div>
                  </div>
                </div>
                <div class="ls-canvas-wrap" style="height:120px">
                  <canvas ref="netChartRef"></canvas>
                </div>
              </div>
            </div>

            <!-- BREAKDOWN -->
            <div v-else-if="activeTab === 'table'" key="table">
              <div class="ls-table-layout" :class="{ 'show-detail': !!selected }">
                <div class="ls-card">
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

                <Transition name="slide-in">
                  <div v-if="selected" class="ls-card ls-breakdown">
                    <div class="ls-card-header">
                      <div>
                        <div class="ls-card-title">{{ selected.periodLabel }}</div>
                        <div class="ls-card-sub">{{ selected.customerBreakdown?.length ?? 0 }} khách hàng</div>
                      </div>
                      <button class="ls-close-btn" @click="selectedPeriod = null">✕</button>
                    </div>
                    <div class="ls-mini-stats">
                      <div class="ls-mini-stat">
                        <div class="ls-mini-label">Earn</div>
                        <div class="ls-mini-val earn">+{{ fmt(selected.totalPointsEarned) }}</div>
                      </div>
                      <div class="ls-mini-stat">
                        <div class="ls-mini-label">Deduct</div>
                        <div class="ls-mini-val deduct">-{{ fmt(selected.totalPointsDeducted) }}</div>
                      </div>
                      <div class="ls-mini-stat">
                        <div class="ls-mini-label">Net</div>
                        <div class="ls-mini-val" :class="selected.netPoints >= 0 ? 'net-pos' : 'net-neg'">
                          {{ selected.netPoints >= 0 ? '+' : '' }}{{ fmt(selected.netPoints) }}
                        </div>
                      </div>
                      <div class="ls-mini-stat">
                        <div class="ls-mini-label">GD</div>
                        <div class="ls-mini-val muted">{{ selected.totalTransactions }}</div>
                      </div>
                    </div>
                    <div class="ls-breakdown-scroll">
                      <table v-if="selected.customerBreakdown?.length" class="ls-btable">
                        <thead>
                          <tr><th>Khách hàng</th><th>Hạng</th><th>Earn</th><th>Deduct</th><th>Net</th><th>GD</th></tr>
                        </thead>
                        <tbody>
                          <tr v-for="(r, i) in selected.customerBreakdown" :key="i">
                            <td>
                              <div class="ls-cust-cell">
                                <div class="ls-cust-avatar" :style="`background:${avatarColor(r.customerName)}`">
                                  {{ initials(r.customerName) }}
                                </div>
                                <div>
                                  <div class="ls-cust-name">{{ r.customerName }}</div>
                                  <div class="ls-cust-email">{{ r.customerEmail }}</div>
                                </div>
                              </div>
                            </td>
                            <td>
                              <span class="ls-tier-chip" :class="'tier-' + (r.vipTier || 'member').toLowerCase()">
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

            <!-- RANKING -->
            <div v-else-if="activeTab === 'ranking'" key="ranking">
              <div class="ls-chart-row">
                <div class="ls-card" style="flex:1.3">
                  <div class="ls-card-header">
                    <div>
                      <div class="ls-card-title">Top Earners</div>
                      <div class="ls-card-sub">Khách hàng tích điểm nhiều nhất</div>
                    </div>
                  </div>
                  <div class="ls-canvas-wrap" style="height:280px">
                    <canvas ref="topEarnerChartRef"></canvas>
                  </div>
                </div>
                <div class="ls-card" style="flex:1">
                  <div class="ls-card-header">
                    <div>
                      <div class="ls-card-title">Earn Rate %</div>
                      <div class="ls-card-sub">Tỷ lệ Earn / (Earn + Deduct)</div>
                    </div>
                  </div>
                  <div class="ls-canvas-wrap" style="height:280px">
                    <canvas ref="rateChartRef"></canvas>
                  </div>
                </div>
              </div>
            </div>

          </Transition>
        </div>
      </template>

      <!-- Empty -->
      <div v-else-if="!loading" class="ls-empty">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5">
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

const getChart = () => (typeof Chart !== 'undefined' ? Chart : null)

// ── Constants ──────────────────────────────────────────────────────
const TIER_COLORS = ['#3b82f6','#f59e0b','#ef4444','#8b5cf6','#10b981','#f97316']
const TIER_META = {
  BRONZE: '#cd7f32', SILVER: '#9ca3af', GOLD: '#f59e0b',
  PLATINUM: '#8b5cf6', DIAMOND: '#3b82f6', Member: '#6b7280',
}

const ICON_OVERVIEW = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>`
const ICON_TABLE   = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 3H5a2 2 0 00-2 2v4m6-6h10a2 2 0 012 2v4M9 3v18m0 0h10a2 2 0 002-2V9M9 21H5a2 2 0 01-2-2V9m0 0h18"/></svg>`
const ICON_TREND   = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></svg>`
const ICON_EARN    = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>`
const ICON_DEDUCT  = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/></svg>`
const ICON_NET     = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>`
const ICON_TX      = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/></svg>`

const tabs = [
  { id: 'overview', label: 'Overview',  icon: ICON_OVERVIEW },
  { id: 'table',    label: 'Breakdown', icon: ICON_TABLE    },
  { id: 'ranking',  label: 'Ranking',   icon: ICON_TREND    },
]
const modes = [
  { value: 'monthly', label: 'Monthly' },
  { value: 'weekly',  label: 'Weekly'  },
]

// ── State ──────────────────────────────────────────────────────────
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

// ── Computed ───────────────────────────────────────────────────────
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
    { key: 'totalPointsEarned',   label: 'Total Earned',   color: '#3b82f6', icon: ICON_EARN,   value: '+' + fmt(totalEarned.value),   delta: dE ? (dE.pos ? '↑' : '↓') + Math.abs(dE.val) + '%' : '—', deltaClass: dE?.pos ? 'up' : 'dn' },
    { key: 'totalPointsDeducted', label: 'Total Deducted', color: '#ef4444', icon: ICON_DEDUCT, value: '-' + fmt(totalDeducted.value), delta: dD ? (dD.pos ? '↑' : '↓') + Math.abs(dD.val) + '%' : '—', deltaClass: !dD?.pos ? 'up' : 'dn' },
    { key: 'netPoints',           label: 'Net Points',     color: '#f59e0b', icon: ICON_NET,    value: (netTotal.value >= 0 ? '+' : '') + fmt(netTotal.value), delta: '—', deltaClass: '' },
    { key: 'totalTransactions',   label: 'Transactions',   color: '#8b5cf6', icon: ICON_TX,     value: fmt(totalTx.value), delta: dT ? (dT.pos ? '↑' : '↓') + Math.abs(dT.val) + '%' : '—', deltaClass: dT?.pos ? 'up' : 'dn' },
  ]
})

// ── Helpers ────────────────────────────────────────────────────────
const fmt = (n) => (n ?? 0).toLocaleString('vi-VN')
const setMode = (m) => { mode.value = m; range.value = 12 }
const togglePeriod = (p) => { selectedPeriod.value = selectedPeriod.value === p ? null : p }

function avatarColor(name) {
  const palette = ['#3b82f6','#8b5cf6','#ec4899','#f59e0b','#10b981','#ef4444','#06b6d4','#f97316']
  const h = (name || '?').split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  return palette[h % palette.length]
}
function initials(name) {
  if (!name) return '?'
  const p = name.trim().split(' ')
  return p.length === 1 ? p[0].slice(0, 2).toUpperCase() : (p[0][0] + p[p.length - 1][0]).toUpperCase()
}

// ── Load ───────────────────────────────────────────────────────────
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

// ── Charts ─────────────────────────────────────────────────────────
function destroyAll() {
  Object.values(charts).forEach(c => c?.destroy())
  charts = {}
}

const FONT = "'Be Vietnam Pro', 'Segoe UI', sans-serif"
const GRID = 'rgba(0,0,0,0.05)'
const TICK = '#9ca3af'
const TT = {
  backgroundColor: '#111827',
  borderColor: '#1f2937',
  borderWidth: 1,
  titleColor: '#9ca3af',
  bodyColor: '#f9fafb',
  titleFont: { family: FONT, size: 11 },
  bodyFont:  { family: FONT, size: 12 },
  padding: 12,
  cornerRadius: 8,
}

function buildCharts() {
  destroyAll()
  const C = getChart()
  if (!C) return

  const reversed = [...data.value].reverse()
  const labels   = reversed.map(d => d.periodLabel)
  const earned   = reversed.map(d => d.totalPointsEarned   || 0)
  const deducted = reversed.map(d => d.totalPointsDeducted || 0)
  const net      = reversed.map(d => d.netPoints            || 0)

  const xScale = {
    grid: { color: GRID },
    ticks: { color: TICK, font: { family: FONT, size: 11 }, maxRotation: 40 },
    border: { display: false },
  }
  const yScale = {
    grid: { color: GRID },
    ticks: { color: TICK, font: { family: FONT, size: 11 }, callback: v => fmt(v) },
    border: { display: false },
  }

  // 1. Combo
  if (comboChartRef.value) {
    charts.combo = new C(comboChartRef.value, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          { label: 'Earn',   data: earned,   backgroundColor: 'rgba(59,130,246,0.75)', borderRadius: 4, borderSkipped: false, order: 2 },
          { label: 'Deduct', data: deducted, backgroundColor: 'rgba(239,68,68,0.65)',  borderRadius: 4, borderSkipped: false, order: 2 },
          { label: 'Net', data: net, type: 'line', borderColor: '#f59e0b', backgroundColor: 'rgba(245,158,11,0.05)', borderWidth: 2, pointRadius: 3, pointBackgroundColor: '#f59e0b', tension: 0.4, fill: false, order: 1 },
        ],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: { legend: { display: false }, tooltip: { ...TT, callbacks: { label: ctx => ` ${ctx.dataset.label}: ${fmt(ctx.raw)}` } } },
        scales: { x: xScale, y: yScale },
      },
    })
  }

  // 2. Donut
  if (donutChartRef.value && tierDonutData.value.length) {
    charts.donut = new C(donutChartRef.value, {
      type: 'doughnut',
      data: {
        labels: tierDonutData.value.map(t => t.tier),
        datasets: [{ data: tierDonutData.value.map(t => t.value), backgroundColor: TIER_COLORS, borderWidth: 3, borderColor: '#ffffff' }],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        cutout: '72%',
        plugins: { legend: { display: false }, tooltip: { ...TT, callbacks: { label: ctx => ` ${ctx.label}: ${fmt(ctx.raw)}` } } },
      },
    })
  }

  // 3. Net trend
  if (netChartRef.value) {
    charts.net = new C(netChartRef.value, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Net', data: net,
          borderColor: '#3b82f6',
          backgroundColor: ctx => {
            const g = ctx.chart.ctx.createLinearGradient(0, 0, 0, 120)
            g.addColorStop(0, 'rgba(59,130,246,0.15)')
            g.addColorStop(1, 'rgba(59,130,246,0)')
            return g
          },
          borderWidth: 2, fill: true, tension: 0.4,
          pointRadius: 3, pointBackgroundColor: '#3b82f6', pointBorderColor: '#fff', pointBorderWidth: 2,
        }],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: { legend: { display: false }, tooltip: { ...TT, callbacks: { label: ctx => ` Net: ${fmt(ctx.raw)}` } } },
        scales: {
          x: { display: false },
          y: { grid: { color: GRID }, ticks: { color: TICK, font: { family: FONT, size: 10 }, callback: v => fmt(v) }, border: { display: false } },
        },
      },
    })
  }

  // 4. Top earners
  if (topEarnerChartRef.value && topEarners.value.length) {
    charts.topEarner = new C(topEarnerChartRef.value, {
      type: 'bar',
      data: {
        labels: topEarners.value.map(e => e.name.length > 16 ? e.name.slice(0, 16) + '…' : e.name),
        datasets: [
          { label: 'Earn',   data: topEarners.value.map(e => e.earn),   backgroundColor: 'rgba(59,130,246,0.75)',  borderRadius: 4, borderSkipped: false },
          { label: 'Deduct', data: topEarners.value.map(e => e.deduct), backgroundColor: 'rgba(239,68,68,0.60)',  borderRadius: 4, borderSkipped: false },
        ],
      },
      options: {
        indexAxis: 'y', responsive: true, maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: { legend: { display: false }, tooltip: { ...TT, callbacks: { label: ctx => ` ${ctx.dataset.label}: ${fmt(ctx.raw)}` } } },
        scales: {
          x: { grid: { color: GRID }, ticks: { color: TICK, font: { family: FONT, size: 11 }, callback: v => fmt(v) }, border: { display: false } },
          y: { grid: { display: false }, ticks: { color: '#374151', font: { family: FONT, size: 12 } } },
        },
      },
    })
  }

  // 5. Earn rate
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
          borderColor: '#8b5cf6',
          backgroundColor: ctx => {
            const g = ctx.chart.ctx.createLinearGradient(0, 0, 0, 280)
            g.addColorStop(0, 'rgba(139,92,246,0.12)')
            g.addColorStop(1, 'rgba(139,92,246,0)')
            return g
          },
          borderWidth: 2, fill: true, tension: 0.4,
          pointRadius: 4, pointBackgroundColor: '#8b5cf6', pointBorderColor: '#fff', pointBorderWidth: 2,
        }],
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { ...TT, callbacks: { label: ctx => ` Earn Rate: ${ctx.raw}%` } } },
        scales: {
          x: xScale,
          y: { min: 0, max: 100, grid: { color: GRID }, ticks: { color: TICK, font: { family: FONT, size: 11 }, callback: v => v + '%' }, border: { display: false } },
        },
      },
    })
  }
}

watch(activeTab, async () => { await nextTick(); if (data.value.length) buildCharts() })
watch([mode, range], load, { immediate: true })
onBeforeUnmount(destroyAll)
</script>

<style scoped>
/* ── Base ──────────────────────────────────────────────────────── */
.ls-root {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
  font-family: 'Be Vietnam Pro', 'Segoe UI', sans-serif;
  color: #111827;
  box-sizing: border-box;
}
*, *::before, *::after { box-sizing: border-box; }

/* ── Sidebar ───────────────────────────────────────────────────── */
.ls-sidebar {
  width: 216px;
  flex-shrink: 0;
  background: #111827;
  display: flex;
  flex-direction: column;
  padding: 24px 0;
  position: sticky;
  top: 0;
  height: 100vh;
}

.ls-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px 22px;
  border-bottom: 1px solid rgba(255,255,255,.06);
}
.ls-brand-name {
  font-size: 14px;
  font-weight: 800;
  color: #f9fafb;
  letter-spacing: .01em;
}

.ls-nav {
  flex: 1;
  padding: 16px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.ls-nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 9px 12px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: rgba(255,255,255,.45);
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all .15s;
  text-align: left;
  width: 100%;
}
.ls-nav-item:hover  { background: rgba(255,255,255,.06); color: rgba(255,255,255,.85); }
.ls-nav-item.active { background: rgba(59,130,246,.15); color: #93c5fd; }
.ls-nav-icon { flex-shrink: 0; }
.ls-nav-label { flex: 1; }
.ls-nav-pip {
  width: 5px; height: 5px;
  border-radius: 50%;
  background: #3b82f6;
  box-shadow: 0 0 6px #3b82f6;
}

.ls-sidebar-footer {
  padding: 16px 20px 0;
  border-top: 1px solid rgba(255,255,255,.06);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: rgba(255,255,255,.3);
  text-transform: uppercase;
  letter-spacing: .06em;
}
.ls-status-dot { width: 6px; height: 6px; border-radius: 50%; }
.ls-status-dot.live    { background: #10b981; box-shadow: 0 0 5px #10b981; }
.ls-status-dot.syncing { background: #f59e0b; animation: blink 1s ease infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.3} }

/* ── Main ──────────────────────────────────────────────────────── */
.ls-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

/* ── Topbar ────────────────────────────────────────────────────── */
.ls-topbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 22px 28px 20px;
  background: white;
  border-bottom: 1.5px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 14px;
  position: sticky;
  top: 0;
  z-index: 10;
}
.ls-breadcrumb {
  font-size: 11px;
  color: #9ca3af;
  letter-spacing: .05em;
  text-transform: uppercase;
  margin-bottom: 5px;
}
.ls-breadcrumb span { margin: 0 5px; opacity: .5; }
.ls-page-title {
  font-size: 20px;
  font-weight: 800;
  color: #111827;
  margin: 0;
  letter-spacing: -.3px;
}
.ls-topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.ls-pill-toggle {
  display: flex;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
}
.ls-pill-toggle button {
  background: transparent;
  border: none;
  padding: 5px 14px;
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  border-radius: 5px;
  transition: all .15s;
}
.ls-pill-toggle button.active { background: #111827; color: white; }

.ls-select {
  background: white;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  padding: 7px 12px;
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
  outline: none;
}
.ls-select:focus { border-color: #3b82f6; }

.ls-btn-refresh {
  display: flex;
  align-items: center;
  gap: 6px;
  background: white;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  padding: 7px 14px;
  font-family: inherit;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;
}
.ls-btn-refresh:hover:not(:disabled) { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }
.ls-btn-refresh:disabled { opacity: .45; cursor: not-allowed; }
@keyframes spin-r { to { transform: rotate(360deg); } }
.spin { animation: spin-r .7s linear infinite; }

/* ── Alert / Empty ─────────────────────────────────────────────── */
.ls-alert-error {
  margin: 14px 28px 0;
  padding: 10px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 10px;
  color: #dc2626;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.ls-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #9ca3af;
  gap: 12px;
  font-size: 14px;
}

/* ── Skeleton ──────────────────────────────────────────────────── */
.ls-skeleton {
  background: linear-gradient(90deg, #f3f4f6 25%, #e9eaec 50%, #f3f4f6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease infinite;
}
@keyframes shimmer { from { background-position: 200% 0; } to { background-position: -200% 0; } }

/* ── Body ──────────────────────────────────────────────────────── */
.ls-body { padding: 20px 28px 40px; }

/* ── KPI Row ───────────────────────────────────────────────────── */
.ls-kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.ls-kpi {
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 14px;
  padding: 16px 18px 0;
  overflow: hidden;
  transition: border-color .15s, box-shadow .15s;
}
.ls-kpi:hover { border-color: #e5e7eb; box-shadow: 0 4px 16px rgba(0,0,0,.06); }
.ls-kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.ls-kpi-icon {
  width: 30px; height: 30px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.ls-kpi-delta {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 20px;
}
.ls-kpi-delta.up { background: #dcfce7; color: #16a34a; }
.ls-kpi-delta.dn { background: #fee2e2; color: #dc2626; }
.ls-kpi-value {
  font-size: 22px;
  font-weight: 800;
  color: var(--accent, #111827);
  letter-spacing: -.5px;
  line-height: 1.1;
  margin-bottom: 3px;
}
.ls-kpi-label {
  font-size: 11px;
  color: #9ca3af;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: .05em;
  margin-bottom: 12px;
}
.ls-kpi-spark { height: 40px; margin: 0 -18px; }
.ls-kpi-spark svg { width: 100%; height: 100%; display: block; }

/* ── Cards ─────────────────────────────────────────────────────── */
.ls-card {
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 14px;
  overflow: hidden;
}
.ls-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px 18px 14px;
  border-bottom: 1px solid #f3f4f6;
}
.ls-card-title { font-size: 13px; font-weight: 700; color: #111827; }
.ls-card-sub   { font-size: 11px; color: #9ca3af; margin-top: 3px; }
.ls-canvas-wrap { padding: 16px 18px; position: relative; }
.ls-canvas-wrap canvas { width: 100% !important; height: 100% !important; display: block; }

.ls-legend {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 11px;
  color: #6b7280;
  font-weight: 500;
}
.ls-legend-dot { width: 10px; height: 10px; border-radius: 2px; display: inline-block; margin-right: 4px; }

.ls-chart-row { display: flex; gap: 14px; align-items: stretch; }
.ls-chart-main { flex: 2; }
.ls-chart-side { flex: 1; display: flex; flex-direction: column; }

.ls-donut-legend { padding: 0 18px 16px; display: flex; flex-direction: column; gap: 7px; }
.ls-donut-row { display: flex; align-items: center; gap: 8px; font-size: 12px; }
.ls-donut-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.ls-donut-name { flex: 1; color: #6b7280; font-size: 12px; }
.ls-donut-val { font-size: 12px; font-weight: 700; color: #111827; }

/* ── Table tab ─────────────────────────────────────────────────── */
.ls-badge {
  font-size: 11px;
  background: #f3f4f6;
  border-radius: 20px;
  padding: 2px 10px;
  color: #6b7280;
  font-weight: 600;
}

.ls-table-layout { display: grid; grid-template-columns: 1fr; gap: 16px; }
.ls-table-layout.show-detail { grid-template-columns: 1fr 1fr; }

.ls-period-head {
  display: grid;
  grid-template-columns: 1fr 100px 100px 100px 60px;
  padding: 8px 18px;
  font-size: 10px;
  color: #9ca3af;
  letter-spacing: .08em;
  text-transform: uppercase;
  font-weight: 700;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.ls-period-row {
  display: grid;
  grid-template-columns: 1fr 100px 100px 100px 60px;
  align-items: center;
  padding: 13px 18px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all .12s;
}
.ls-period-row:last-child { border-bottom: none; }
.ls-period-row:hover { background: #fafafa; }
.ls-period-row.active { background: #eff6ff; border-left-color: #3b82f6; }
.ls-pr-label { font-size: 13px; font-weight: 700; color: #111827; }
.ls-pr-range { font-size: 10px; color: #9ca3af; margin-top: 2px; }

/* Numbers */
.ls-num  { font-size: 12px; font-weight: 600; }
.earn    { color: #2563eb; }
.deduct  { color: #dc2626; }
.net-pos { color: #059669; font-weight: 700; }
.net-neg { color: #dc2626; font-weight: 700; }
.muted   { color: #9ca3af; }

/* Breakdown */
.ls-breakdown { display: flex; flex-direction: column; max-height: 640px; }
.ls-close-btn {
  width: 28px; height: 28px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  font-size: 11px;
  color: #6b7280;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
}
.ls-close-btn:hover { border-color: #ef4444; color: #ef4444; background: #fef2f2; }

.ls-mini-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-bottom: 1px solid #f0f0f0;
}
.ls-mini-stat {
  padding: 12px 16px;
  border-right: 1px solid #f0f0f0;
}
.ls-mini-stat:last-child { border-right: none; }
.ls-mini-label { font-size: 9px; color: #9ca3af; letter-spacing: .08em; text-transform: uppercase; font-weight: 700; }
.ls-mini-val   { font-size: 16px; font-weight: 800; margin-top: 4px; color: #111827; }

.ls-breakdown-scroll { overflow-y: auto; flex: 1; }
.ls-btable { width: 100%; border-collapse: collapse; font-size: 12px; }
.ls-btable th {
  padding: 8px 12px;
  text-align: left;
  color: #9ca3af;
  font-size: 10px;
  letter-spacing: .08em;
  text-transform: uppercase;
  font-weight: 700;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
  position: sticky; top: 0; z-index: 1;
}
.ls-btable td {
  padding: 10px 12px;
  border-bottom: 1px solid #f3f4f6;
  vertical-align: middle;
}
.ls-btable tr:last-child td { border-bottom: none; }
.ls-btable tr:hover td { background: #f9fafb; }

.ls-cust-cell { display: flex; align-items: center; gap: 8px; }
.ls-cust-avatar {
  width: 28px; height: 28px;
  border-radius: 8px;
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 800; color: white;
}
.ls-cust-name  { font-size: 12px; font-weight: 600; color: #111827; white-space: nowrap; }
.ls-cust-email { font-size: 10px; color: #9ca3af; margin-top: 1px; }

.ls-tier-chip {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .04em;
  text-transform: uppercase;
}
.tier-bronze   { background: #fef3c7; color: #92400e; }
.tier-silver   { background: #f1f5f9; color: #475569; }
.tier-gold     { background: #fef9c3; color: #b45309; }
.tier-platinum { background: #ede9fe; color: #6d28d9; }
.tier-diamond  { background: #dbeafe; color: #1d4ed8; }
.tier-member   { background: #f3f4f6; color: #6b7280; }

.ls-empty-small { padding: 24px 18px; color: #9ca3af; font-size: 13px; }

/* ── Transitions ───────────────────────────────────────────────── */
.tab-fade-enter-active, .tab-fade-leave-active { transition: opacity .15s, transform .15s; }
.tab-fade-enter-from, .tab-fade-leave-to { opacity: 0; transform: translateY(6px); }

.slide-in-enter-active, .slide-in-leave-active { transition: opacity .2s, transform .2s; }
.slide-in-enter-from { opacity: 0; transform: translateX(16px); }
.slide-in-leave-to   { opacity: 0; transform: translateX(16px); }

/* ── Responsive ────────────────────────────────────────────────── */
@media (max-width: 1100px) {
  .ls-kpi-row   { grid-template-columns: repeat(2, 1fr); }
  .ls-chart-row { flex-direction: column; }
}
@media (max-width: 768px) {
  .ls-sidebar { display: none; }
  .ls-body { padding: 14px; }
  .ls-kpi-row { grid-template-columns: 1fr 1fr; }
  .ls-table-layout.show-detail { grid-template-columns: 1fr; }
  .ls-topbar { padding: 16px; }
}
</style>