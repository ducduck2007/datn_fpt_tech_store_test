<template>
  <div class="ls-root">

    <!-- ── HEADER ───────────────────────────────────────────────── -->
    <header class="ls-header">
      <!-- Row 1: Brand + Controls -->
      <div class="ls-header-top">
        <div class="ls-header-left">
          <div class="ls-brand">
            <svg width="26" height="26" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="8" fill="#0f1117"/>
              <rect x="6" y="6" width="7" height="7" rx="2" fill="#3b82f6"/>
              <rect x="15" y="6" width="7" height="7" rx="2" fill="#3b82f6" opacity=".4"/>
              <rect x="6" y="15" width="7" height="7" rx="2" fill="#3b82f6" opacity=".4"/>
              <rect x="15" y="15" width="7" height="7" rx="2" fill="#3b82f6" opacity=".2"/>
            </svg>
            <span class="ls-brand-name">LoyaltyOS</span>
          </div>
          <div class="ls-header-divider"></div>
          <div>
            <div class="ls-breadcrumb">Phân tích <span>›</span> Loyalty <span>›</span> {{ mode === 'monthly' ? 'Hàng tháng' : 'Hàng tuần' }}</div>
            <h1 class="ls-page-title">{{ currentTab.label }}</h1>
          </div>
        </div>
        <div class="ls-topbar-right">
          <div class="ls-status-pill" :class="loading ? 'syncing' : 'live'">
            <span class="ls-status-dot"></span>
            <span>{{ loading ? 'Đang đồng bộ…' : 'Trực tiếp' }}</span>
          </div>
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
            {{ loading ? 'Đang tải…' : 'Làm mới' }}
          </button>
        </div>
      </div>

      <!-- Row 2: Tab Nav -->
      <nav class="ls-tab-nav">
        <button
          v-for="tab in tabs" :key="tab.id"
          class="ls-tab-item"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          <span class="ls-tab-icon" v-html="tab.icon"></span>
          <span>{{ tab.label }}</span>
        </button>
      </nav>
    </header>

    <!-- ── MAIN ─────────────────────────────────────────────────── -->
    <main class="ls-main">

      <!-- Error -->
      <div v-if="error" class="ls-alert-error">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        {{ error }}
      </div>

      <!-- Skeleton -->
      <div v-if="loading" class="ls-body">
        <div class="ls-kpi-row">
          <div v-for="i in 4" :key="i" class="ls-skeleton" style="height:100px; border-radius:12px"/>
        </div>
        <div style="display:grid;grid-template-columns:2fr 1fr;gap:14px;margin-top:14px">
          <div class="ls-skeleton" style="height:280px;border-radius:12px"/>
          <div class="ls-skeleton" style="height:280px;border-radius:12px"/>
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

            <!-- OVERVIEW -->
            <div v-if="activeTab === 'overview'" key="overview">
              <div class="ls-chart-row">
                <div class="ls-card ls-chart-main">
                  <div class="ls-card-header">
                    <div>
                      <div class="ls-card-title">Tích lũy vs Khấu trừ</div>
                      <div class="ls-card-sub">Điểm tích lũy &amp; khấu trừ theo kỳ</div>
                    </div>
                    <div class="ls-legend">
                      <span class="ls-legend-dot" style="background:#3b82f6"></span><span>Tích lũy</span>
                      <span class="ls-legend-dot" style="background:#ef4444"></span><span>Khấu trừ</span>
                      <span class="ls-legend-dot" style="background:#f59e0b;border-radius:50%"></span><span>Thuần</span>
                    </div>
                  </div>
                  <div class="ls-canvas-wrap" style="height:240px">
                    <canvas ref="comboChartRef"></canvas>
                  </div>
                </div>

                <div class="ls-card ls-chart-side">
                  <div class="ls-card-header">
                    <div>
                      <div class="ls-card-title">Phân phối Hạng</div>
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
                    <div class="ls-card-title">Xu hướng Điểm Thuần</div>
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
                    <span>Kỳ</span><span>Tích lũy</span><span>Khấu trừ</span><span>Thuần</span><span>Giao dịch</span>
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
                        <div class="ls-mini-label">Tích lũy</div>
                        <div class="ls-mini-val earn">+{{ fmt(selected.totalPointsEarned) }}</div>
                      </div>
                      <div class="ls-mini-stat">
                        <div class="ls-mini-label">Khấu trừ</div>
                        <div class="ls-mini-val deduct">-{{ fmt(selected.totalPointsDeducted) }}</div>
                      </div>
                      <div class="ls-mini-stat">
                        <div class="ls-mini-label">Thuần</div>
                        <div class="ls-mini-val" :class="selected.netPoints >= 0 ? 'net-pos' : 'net-neg'">
                          {{ selected.netPoints >= 0 ? '+' : '' }}{{ fmt(selected.netPoints) }}
                        </div>
                      </div>
                      <div class="ls-mini-stat">
                        <div class="ls-mini-label">Giao dịch</div>
                        <div class="ls-mini-val muted">{{ selected.totalTransactions }}</div>
                      </div>
                    </div>
                    <div class="ls-breakdown-scroll">
                      <table v-if="selected.customerBreakdown?.length" class="ls-btable">
                        <thead>
                          <tr><th>Khách hàng</th><th>Hạng</th><th>Tích lũy</th><th>Khấu trừ</th><th>Thuần</th><th>Giao dịch</th></tr>
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
                      <div class="ls-card-title">Top Tích điểm</div>
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
                      <div class="ls-card-title">Tỷ lệ Tích lũy %</div>
                      <div class="ls-card-sub">Tỷ lệ Tích lũy / (Tích lũy + Khấu trừ)</div>
                    </div>
                  </div>
                  <div class="ls-canvas-wrap" style="height:280px">
                    <canvas ref="rateChartRef"></canvas>
                  </div>
                </div>
              </div>
            </div>

            <!-- SPENDING ANALYSIS -->
            <div v-else-if="activeTab === 'spending'" key="spending">
              <div class="sa-root">

                <!-- ── Header ─────────────────────────────────────────────── -->
                <div class="sa-header">
                  <div class="sa-header-left">
                    <div class="sa-kicker">Phân tích</div>
                    <h1 class="sa-title">Phân tích Chi tiêu Khách hàng</h1>
                    <p class="sa-sub">Phân tích xu hướng chi tiêu và khách hàng hàng đầu</p>
                  </div>
                  <button class="sa-btn-reload" @click="loadAllSpending" :disabled="loadingSpending">
                    <svg :class="{ spin: loadingSpending }" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <path d="M23 4v6h-6M1 20v-6h6"/>
                      <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
                    </svg>
                    Tải lại
                  </button>
                </div>

                <!-- ── KPI Cards ───────────────────────────────────────────── -->
                <div class="sa-kpi-row">
                  <div class="sa-kpi" style="--c:#3b82f6;--bg:#eff6ff">
                    <div class="sa-kpi-icon" style="background:#eff6ff;color:#3b82f6">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    </div>
                    <div class="sa-kpi-body">
                      <div class="sa-kpi-label">Tổng Chi tiêu</div>
                      <div class="sa-kpi-value">{{ formatCurrency(spendingStats.totalSpent) }}</div>
                    </div>
                  </div>
                  <div class="sa-kpi" style="--c:#10b981;--bg:#ecfdf5">
                    <div class="sa-kpi-icon" style="background:#ecfdf5;color:#10b981">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
                    </div>
                    <div class="sa-kpi-body">
                      <div class="sa-kpi-label">Tổng Khách hàng</div>
                      <div class="sa-kpi-value">{{ (spendingStats.totalCustomers || 0).toLocaleString() }}</div>
                    </div>
                  </div>
                  <div class="sa-kpi" style="--c:#f59e0b;--bg:#fffbeb">
                    <div class="sa-kpi-icon" style="background:#fffbeb;color:#f59e0b">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></svg>
                    </div>
                    <div class="sa-kpi-body">
                      <div class="sa-kpi-label">Chi tiêu TB</div>
                      <div class="sa-kpi-value">{{ formatCurrency(spendingStats.averageSpent) }}</div>
                    </div>
                  </div>
                  <div class="sa-kpi" style="--c:#8b5cf6;--bg:#f5f3ff">
                    <div class="sa-kpi-icon" style="background:#f5f3ff;color:#8b5cf6">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9H4.5a2.5 2.5 0 010-5H6m12 0h1.5a2.5 2.5 0 010 5H18M18 2H6v7a6 6 0 0012 0V2z"/></svg>
                    </div>
                    <div class="sa-kpi-body">
                      <div class="sa-kpi-label">Chi tiêu Nhiều nhất</div>
                      <div class="sa-kpi-value sm">{{ topSpenders[0]?.fullName || 'N/A' }}</div>
                    </div>
                  </div>
                </div>

                <!-- ── Tabs ────────────────────────────────────────────────── -->
                <div class="sa-tabs">
                  <button v-for="tab in spendingTabList" :key="tab.id"
                    class="sa-tab" :class="{ active: spendingActiveTab === tab.id }"
                    @click="switchSpendingTab(tab.id)">
                    <span v-html="tab.icon"></span>
                    {{ tab.label }}
                  </button>
                </div>

                <!-- ── Tab: Top Spenders ───────────────────────────────────── -->
                <div v-if="spendingActiveTab === 'topSpenders'" class="sa-panel">
                  <div class="sa-filter-bar">
                    <div class="sa-filter-group">
                      <label class="sa-label">Giới hạn</label>
                      <div class="sa-number-input">
                        <button @click="topSpendersLimit = Math.max(5, topSpendersLimit - 5)">−</button>
                        <span>{{ topSpendersLimit }}</span>
                        <button @click="topSpendersLimit = Math.min(100, topSpendersLimit + 5)">+</button>
                      </div>
                    </div>
                    <div class="sa-filter-group">
                      <label class="sa-label">Hạng VIP</label>
                      <select v-model="topSpendersTier" class="sa-select">
                        <option value="">Tất cả hạng</option>
                        <option v-for="t in tiers" :key="t" :value="t">{{ t }}</option>
                      </select>
                    </div>
                    <button class="sa-btn-primary" @click="loadTopSpenders" :disabled="loadingTopSpenders">
                      <span v-if="loadingTopSpenders" class="spinner-sm"></span>
                      <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                      Tải
                    </button>
                  </div>

                  <div class="sa-table-wrap">
                    <div v-if="loadingTopSpenders" class="sa-table-skeleton">
                      <div v-for="i in 5" :key="i" class="sk-row">
                        <div class="sk sk-rank"></div>
                        <div class="sk-user"><div class="sk sk-av"></div><div class="sk-lines"><div class="sk sk-l w70"></div><div class="sk sk-l w50"></div></div></div>
                        <div class="sk sk-pill"></div>
                        <div class="sk sk-money"></div>
                        <div class="sk sk-pill"></div>
                      </div>
                    </div>
                    <table v-else-if="topSpenders.length" class="sa-table">
                      <thead>
                        <tr>
                          <th style="width:70px;text-align:center">Hạng</th>
                          <th>Khách hàng</th>
                          <th style="width:110px;text-align:center">Hạng VIP</th>
                          <th style="width:180px;text-align:right">Tổng Chi tiêu</th>
                          <th style="width:130px;text-align:center">Điểm</th>
                          <th style="width:110px;text-align:center">Loại</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(row, idx) in topSpenders" :key="row.id">
                          <td style="text-align:center">
                            <div class="rank-badge" :class="['rank-' + getRankKey(idx)]">
                              <svg v-if="idx < 3" width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M6 9H4.5a2.5 2.5 0 010-5H6m12 0h1.5a2.5 2.5 0 010 5H18M4 22h16M18 2H6v7a6 6 0 0012 0V2z"/></svg>
                              <span v-else>{{ idx + 1 }}</span>
                            </div>
                          </td>
                          <td>
                            <div class="cust-cell">
                              <div class="cust-av" :style="`background:${avatarColor(row.fullName)}`">{{ initials(row.fullName) }}</div>
                              <div>
                                <div class="cust-name">{{ row.fullName }}</div>
                                <div class="cust-email">{{ row.email }}</div>
                              </div>
                            </div>
                          </td>
                          <td style="text-align:center">
                            <span v-if="row.raw.vipTier" class="tier-chip" :class="'tier-' + row.raw.vipTier.toLowerCase()">{{ row.raw.vipTier }}</span>
                            <span v-else class="muted-dash">—</span>
                          </td>
                          <td style="text-align:right">
                            <span class="spend-amount">{{ formatCurrency(row.raw.totalSpent) }}</span>
                          </td>
                          <td style="text-align:center">
                            <span class="points-badge">⭐ {{ row.loyaltyPoints }}</span>
                          </td>
                          <td style="text-align:center">
                            <span class="type-chip" :class="row.customerType === 'VIP' ? 'type-vip' : 'type-reg'">{{ row.customerType }}</span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                    <div v-else class="sa-empty">
                      <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                      <p>Chưa có dữ liệu</p>
                    </div>
                  </div>
                </div>

                <!-- ── Tab: Spending Range ─────────────────────────────────── -->
                <div v-else-if="spendingActiveTab === 'spendingRange'" class="sa-panel">
                  <div class="sa-filter-bar wrap">
                    <div class="sa-filter-group">
                      <label class="sa-label">Chi tiêu Tối thiểu (₫)</label>
                      <input type="number" v-model.number="spendingMin" class="sa-input" placeholder="0" min="0" step="1000000"/>
                    </div>
                    <div class="sa-filter-group">
                      <label class="sa-label">Chi tiêu Tối đa (₫)</label>
                      <input type="number" v-model.number="spendingMax" class="sa-input" placeholder="999,999,999" min="0" step="1000000"/>
                    </div>
                    <div class="sa-filter-actions">
                      <button class="sa-btn-primary" @click="loadBySpendingRange" :disabled="loadingSpendingRange">
                        <span v-if="loadingSpendingRange" class="spinner-sm"></span>
                        <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 9 11 13 15 22 22 3"/></svg>
                        Áp dụng
                      </button>
                      <button class="sa-btn-ghost" @click="clearSpendingRange">Xóa</button>
                    </div>
                  </div>

                  <!-- Quick Ranges -->
                  <div class="sa-quick-ranges">
                    <span class="sa-label">Nhanh:</span>
                    <button v-for="q in quickRanges" :key="q.label" class="sa-quick-btn" @click="setQuickRange(q.min, q.max)">
                      {{ q.label }}
                    </button>
                  </div>

                  <!-- Active Range Alert -->
                  <div class="sa-range-alert" v-if="appliedSpendingRange.min !== null || appliedSpendingRange.max !== null">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    <div class="sa-table-wrap">
                      <div v-if="loadingSpendingRange" class="sa-table-skeleton">
                        <div v-for="i in 5" :key="i" class="sk-row">
                          <div class="sk sk-id"></div>
                          <div class="sk-user"><div class="sk sk-av"></div><div class="sk-lines"><div class="sk sk-l w70"></div><div class="sk sk-l w50"></div></div></div>
                          <div class="sk sk-pill"></div>
                          <div class="sk sk-money"></div>
                        </div>
                      </div>
                      <div v-else-if="pagedSpending.length">
                        <table class="sa-table">
                          <thead>
                            <tr>
                              <th style="width:60px">ID</th>
                              <th>Khách hàng</th>
                              <th style="width:110px;text-align:center">Hạng VIP</th>
                              <th style="width:180px;text-align:right">Tổng Chi tiêu</th>
                              <th style="width:130px;text-align:center">Điểm</th>
                              <th style="width:110px;text-align:center">Loại</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(row, idx) in pagedSpending" :key="row.id">
                              <td style="text-align:center">
                                <div class="rank-badge" :class="['rank-' + getRankKey(idx)]">
                                  <svg v-if="idx < 3" width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M6 9H4.5a2.5 2.5 0 010-5H6m12 0h1.5a2.5 2.5 0 010 5H18M4 22h16M18 2H6v7a6 6 0 0012 0V2z"/></svg>
                                  <span v-else>{{ idx + 1 }}</span>
                                </div>
                              </td>
                              <td>
                                <div class="cust-cell">
                                  <div class="cust-av" :style="`background:${avatarColor(row.fullName)}`">{{ initials(row.fullName) }}</div>
                                  <div>
                                    <div class="cust-name">{{ row.fullName }}</div>
                                    <div class="cust-email">{{ row.email }}</div>
                                  </div>
                                </div>
                              </td>
                              <td style="text-align:center">
                                <span v-if="row.raw.vipTier" class="tier-chip" :class="'tier-' + row.raw.vipTier.toLowerCase()">{{ row.raw.vipTier }}</span>
                                <span v-else class="muted-dash">—</span>
                              </td>
                              <td style="text-align:right">
                                <span class="spend-amount">{{ formatCurrency(row.raw.totalSpent) }}</span>
                              </td>
                              <td style="text-align:center">
                                <span class="points-badge">⭐ {{ row.loyaltyPoints }}</span>
                              </td>
                              <td style="text-align:center">
                                <span class="type-chip" :class="row.customerType === 'VIP' ? 'type-vip' : 'type-reg'">{{ row.customerType }}</span>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                      <div v-else class="sa-empty">
                        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                        <p>Chưa có dữ liệu</p>
                      </div>
                    </div>
                  </div>
                </div>

              </div>
            </div>

        </div>
      </template>

      <!-- Empty -->
      <div v-else class="ls-empty">
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
import { ref, computed, watch, nextTick, onBeforeUnmount, reactive } from 'vue'
import { customersApi } from '../../api/customers.api'
import { toast } from '../../ui/toast'
import Chart from 'chart.js/auto'


const getChart = () => Chart

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
const ICON_SPENDING = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9H4.5a2.5 2.5 0 010-5H6m12 0h1.5a2.5 2.5 0 010 5H18M18 2H6v7a6 6 0 0012 0V2z"/></svg>`

const tabs = [
  { id: 'overview', label: 'Tổng quan',  icon: ICON_OVERVIEW },
  { id: 'table',    label: 'Chi tiết', icon: ICON_TABLE    },
  { id: 'ranking',  label: 'Xếp hạng',   icon: ICON_TREND    },
  { id: 'spending', label: 'Phân tích Chi tiêu', icon: ICON_SPENDING },
]
const modes = [
  { value: 'monthly', label: 'Hàng tháng' },
  { value: 'weekly',  label: 'Hàng tuần'  },
]

// ── Spending Analysis State ───────────────────────────────────────
const loadingSpending         = ref(false);
const loadingTopSpenders      = ref(false);
const loadingSpendingRange    = ref(false);
const spendingActiveTab       = ref("topSpenders");

const topSpenders            = ref([]);
const topSpendersLimit       = ref(10);
const topSpendersTier        = ref("");

const customersBySpending    = ref([]);
const spendingMin            = ref(null);
const spendingMax            = ref(null);
const appliedSpendingRange   = reactive({ min: null, max: null });
const spendingPage           = ref(1);
const pageSize               = 10;

const spendingStats          = ref({ totalSpent: 0, totalCustomers: 0, averageSpent: 0, byTier: {}, spendingRanges: {} });

const spendingTabList = [
  { id: 'topSpenders',   label: 'Top Chi tiêu',    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9H4.5a2.5 2.5 0 010-5H6m12 0h1.5a2.5 2.5 0 010 5H18M18 2H6v7a6 6 0 0012 0V2z"/></svg>` },
  { id: 'spendingRange', label: 'Theo Khoảng Chi tiêu', icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 9 11 13 15 22 22 3"/></svg>` },
  { id: 'statistics',    label: 'Thống kê',       icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>` },
];

const quickRanges = [
  { label: 'Dưới 1M',   min: 0,        max: 1000000   },
  { label: '1M – 5M',    min: 1000000,  max: 5000000   },
  { label: '5M – 10M',   min: 5000000,  max: 10000000  },
  { label: '10M – 50M',  min: 10000000, max: 50000000  },
  { label: 'Trên 50M',   min: 50000000, max: 999999999 },
];

// ── Spending Analysis Computed ───────────────────────────────────
const pagedSpending = computed(() => {
  const s = (spendingPage.value - 1) * pageSize;
  return customersBySpending.value.slice(s, s + pageSize);
});
const totalSpendingPages = computed(() => Math.ceil(customersBySpending.value.length / pageSize));

// ── Spending Analysis Functions ───────────────────────────────────
function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  const root = payload?.data ?? payload;
  if (Array.isArray(root)) return root;
  for (const k of ["content", "items", "results", "rows", "list"]) {
    if (Array.isArray(root?.[k])) return root[k];
    if (Array.isArray(root?.data?.[k])) return root.data[k];
  }
  return [];
}

function normalize(list) {
  return (list || []).map(c => ({
    id: c?.id ?? c?.customerId,
    fullName: c?.fullName ?? c?.name ?? "",
    email: c?.email ?? "",
    phone: c?.phone ?? "",
    customerType: (c?.customerType ?? "REGULAR").toString().toUpperCase(),
    loyaltyPoints: c?.loyaltyPoints ?? 0,
    raw: c,
  }));
}

function formatCurrency(value) {
  if (!value) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function getRankKey(i) { return ['gold','silver','bronze'][i] || 'other'; }
function formatRangeLabel(r) {
  return { under1M: 'Dưới 1M', '1M-5M': '1M – 5M', '5M-10M': '5M – 10M', '10M-50M': '10M – 50M', over50M: 'Trên 50M' }[r] || r;
}
function getPercentage(count, total) { return total ? Math.round(count / total * 100) : 0; }
function getRangeColor(r) {
  return { under1M: '#9ca3af', '1M-5M': '#3b82f6', '5M-10M': '#10b981', '10M-50M': '#f59e0b', over50M: '#ef4444' }[r] || '#3b82f6';
}

async function loadTopSpenders() {
  loadingTopSpenders.value = true;
  try {
    const res = topSpendersTier.value
      ? await customersApi.listTopSpendersByVipTier(topSpendersTier.value, topSpendersLimit.value)
      : await customersApi.listTopSpenders(topSpendersLimit.value);
    topSpenders.value = normalize(extractList(res?.data));
  } catch { toast("Không thể tải danh sách chi tiêu hàng đầu.", "error"); }
  finally { loadingTopSpenders.value = false; }
}

async function loadBySpendingRange() {
  if (spendingMin.value === null && spendingMax.value === null) { toast("Nhập ít nhất một giá trị", "warning"); return; }
  if (spendingMin.value != null && spendingMax.value != null && spendingMin.value > spendingMax.value) { toast("Giá trị tối thiểu không thể vượt quá tối đa", "error"); return; }
  loadingSpendingRange.value = true;
  try {
    const min = spendingMin.value || 0;
    const max = spendingMax.value || 999999999;
    const res = await customersApi.listBySpendingRange(min, max);
    customersBySpending.value = normalize(extractList(res?.data));
    appliedSpendingRange.min = min;
    appliedSpendingRange.max = max;
    spendingPage.value = 1;
    toast(`Tìm thấy ${customersBySpending.value.length} khách hàng`, "success");
  } catch { toast("Không thể tải khách hàng.", "error"); }
  finally { loadingSpendingRange.value = false; }
}

async function loadStatistics() {
  loadingSpending.value = true;
  try { const res = await customersApi.getSpendingStatistics(); spendingStats.value = res?.data || {}; }
  catch { toast("Không thể tải thống kê.", "error"); }
  finally { loadingSpending.value = false; }
}

function clearSpendingRange() {
  spendingMin.value = null; spendingMax.value = null;
  appliedSpendingRange.min = null; appliedSpendingRange.max = null;
  customersBySpending.value = [];
}
function setQuickRange(min, max) { spendingMin.value = min; spendingMax.value = max; loadBySpendingRange(); }
async function loadAllSpending() { await Promise.all([loadTopSpenders(), loadStatistics()]); }
function switchSpendingTab(id) {
  spendingActiveTab.value = id;
  if (id === 'topSpenders' && !topSpenders.value.length) loadTopSpenders();
  if (id === 'statistics' && !Object.keys(spendingStats.value.byTier || {}).length) loadStatistics();
}

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
    { key: 'totalPointsEarned',   label: 'Tổng Tích lũy',   color: '#3b82f6', icon: ICON_EARN,   value: '+' + fmt(totalEarned.value),   delta: dE ? (dE.pos ? '↑' : '↓') + Math.abs(dE.val) + '%' : '—', deltaClass: dE?.pos ? 'up' : 'dn' },
    { key: 'totalPointsDeducted', label: 'Tổng Khấu trừ', color: '#ef4444', icon: ICON_DEDUCT, value: '-' + fmt(totalDeducted.value), delta: dD ? (!dD.pos ? '↑' : '↓') + Math.abs(dD.val) + '%' : '—', deltaClass: !dD?.pos ? 'up' : 'dn' },
    { key: 'netPoints',           label: 'Điểm Thuần',     color: '#f59e0b', icon: ICON_NET,    value: (netTotal.value >= 0 ? '+' : '') + fmt(netTotal.value), delta: '—', deltaClass: '' },
    { key: 'totalTransactions',   label: 'Giao dịch',   color: '#8b5cf6', icon: ICON_TX,     value: fmt(totalTx.value), delta: dT ? (dT.pos ? '↑' : '↓') + Math.abs(dT.val) + '%' : '—', deltaClass: dT?.pos ? 'up' : 'dn' },
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
console.log('keys:', Object.keys(data.value[0] || {}))
console.log('breakdown:', data.value[0]?.customerBreakdown)
setTimeout(() => buildCharts(), 50)
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

const FONT = "'Plus Jakarta Sans', 'Segoe UI', sans-serif"
const GRID = 'rgba(0,0,0,0.05)'
const TICK = '#9ca3af'
const TT = {
  backgroundColor: '#0f1117',
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
          { label: 'Tích lũy',   data: earned,   backgroundColor: 'rgba(59,130,246,0.75)', borderRadius: 4, borderSkipped: false, order: 2 },
          { label: 'Khấu trừ', data: deducted, backgroundColor: 'rgba(239,68,68,0.65)',  borderRadius: 4, borderSkipped: false, order: 2 },
          { label: 'Thuần', data: net, type: 'line', borderColor: '#f59e0b', backgroundColor: 'rgba(245,158,11,0.05)', borderWidth: 2, pointRadius: 3, pointBackgroundColor: '#f59e0b', tension: 0.4, fill: false, order: 1 },
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
          label: 'Thuần', data: net,
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
        plugins: { legend: { display: false }, tooltip: { ...TT, callbacks: { label: ctx => ` ${ctx.dataset.label}: ${fmt(ctx.raw)}` } } },
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
          { label: 'Tích lũy',   data: topEarners.value.map(e => e.earn),   backgroundColor: 'rgba(59,130,246,0.75)',  borderRadius: 4, borderSkipped: false },
          { label: 'Khấu trừ', data: topEarners.value.map(e => e.deduct), backgroundColor: 'rgba(239,68,68,0.60)',  borderRadius: 4, borderSkipped: false },
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
          label: 'Tỷ lệ Tích lũy %', data: rate,
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
        plugins: { legend: { display: false }, tooltip: { ...TT, callbacks: { label: ctx => ` Tỷ lệ: ${ctx.raw}%` } } },
        scales: {
          x: xScale,
          y: { min: 0, max: 100, grid: { color: GRID }, ticks: { color: TICK, font: { family: FONT, size: 11 }, callback: v => v + '%' }, border: { display: false } },
        },
      },
    })
  }
}
watch(activeTab, (newTab) => {
  if (!data.value.length) return
  const delay = newTab === 'ranking' ? 150 : 50
  setTimeout(() => buildCharts(), delay)
})
watch([mode, range], load, { immediate: true })
onBeforeUnmount(destroyAll)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

/* ── Variables & Base ──────────────────────────────────────────── */
.ls-root {
  --c-bg: #f6f7f9;
  --c-card: #ffffff;
  --c-border: #e4e7ec;
  --c-border-light: #f0f2f5;
  --c-text: #0f1117;
  --c-muted: #6b7280;
  --c-subtle: #9ca3af;
  --c-blue: #2563eb;
  --c-blue-bg: #eff6ff;
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
  --radius: 12px;
  --radius-sm: 8px;
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-md: 0 4px 16px rgba(0,0,0,0.08), 0 2px 6px rgba(0,0,0,0.04);

  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--c-bg);
  font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
  color: var(--c-text);
  box-sizing: border-box;
}
*, *::before, *::after { box-sizing: border-box; }

/* ── Header (replaces sidebar) ─────────────────────────────────── */
.ls-header {
  background: var(--c-card);
  border-bottom: 1px solid var(--c-border);
  position: sticky;
  top: 0;
  z-index: 20;
  box-shadow: var(--shadow-sm);
}

/* Row 1: brand + controls */
.ls-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 28px;
  gap: 16px;
  flex-wrap: wrap;
  border-bottom: 1px solid var(--c-border-light);
}
.ls-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.ls-header-divider {
  width: 1px;
  height: 32px;
  background: var(--c-border);
  flex-shrink: 0;
}

.ls-brand {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-shrink: 0;
}
.ls-brand-name {
  font-size: 14px;
  font-weight: 800;
  color: var(--c-text);
  letter-spacing: -0.02em;
}

.ls-breadcrumb {
  font-size: 11px;
  font-weight: 700;
  color: var(--c-subtle);
  letter-spacing: 0.07em;
  text-transform: uppercase;
  margin-bottom: 3px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.ls-breadcrumb span { opacity: 0.4; }
.ls-page-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--c-text);
  margin: 0;
  letter-spacing: -0.02em;
}

.ls-topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

/* Status pill */
.ls-status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 11px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  border: 1px solid var(--c-border);
  background: var(--c-bg);
  color: var(--c-muted);
}
.ls-status-pill.live   { background: var(--c-green-bg); border-color: var(--c-green-border); color: var(--c-green); }
.ls-status-pill.syncing { background: var(--c-orange-bg); border-color: var(--c-orange-border); color: var(--c-orange); }
.ls-status-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
  background: currentColor;
}
.ls-status-pill.live .ls-status-dot { box-shadow: 0 0 5px var(--c-green); }
.ls-status-pill.syncing .ls-status-dot { animation: blink 1s ease infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0.3} }

/* Row 2: Tab nav */
.ls-tab-nav {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 0 28px;
  overflow-x: auto;
}
.ls-tab-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 12px 16px;
  border: none;
  border-bottom: 2.5px solid transparent;
  background: transparent;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px;
  font-weight: 700;
  color: var(--c-muted);
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
  margin-bottom: -1px;
}
.ls-tab-item:hover { color: var(--c-text); }
.ls-tab-item.active {
  color: var(--c-blue);
  border-bottom-color: var(--c-blue);
}
.ls-tab-icon { flex-shrink: 0; opacity: 0.8; }
.ls-tab-item.active .ls-tab-icon { opacity: 1; }

/* ── Main ──────────────────────────────────────────────────────── */
.ls-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--c-bg);
}

/* Controls */
.ls-pill-toggle {
  display: flex;
  background: var(--c-bg);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 3px;
  gap: 2px;
}
.ls-pill-toggle button {
  background: transparent;
  border: none;
  padding: 5px 14px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 12px;
  font-weight: 700;
  color: var(--c-muted);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s;
}
.ls-pill-toggle button.active {
  background: var(--c-text);
  color: white;
  box-shadow: var(--shadow-sm);
}

.ls-select {
  background: var(--c-card);
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  color: var(--c-text);
  padding: 7px 12px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  outline: none;
  transition: border-color 0.15s;
}
.ls-select:focus { border-color: var(--c-blue); }

.ls-btn-refresh {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--c-card);
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  color: var(--c-text);
  padding: 7px 14px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.15s;
}
.ls-btn-refresh:hover:not(:disabled) { border-color: var(--c-blue); color: var(--c-blue); background: var(--c-blue-bg); }
.ls-btn-refresh:disabled { opacity: 0.45; cursor: not-allowed; }
@keyframes spin-r { to { transform: rotate(360deg); } }
.spin { animation: spin-r 0.7s linear infinite; }

/* ── Alerts / Empty ────────────────────────────────────────────── */
.ls-alert-error {
  margin: 16px 28px 0;
  padding: 11px 16px;
  background: var(--c-red-bg);
  border: 1px solid var(--c-red-border);
  border-radius: var(--radius-sm);
  color: var(--c-red);
  font-size: 13px;
  font-weight: 600;
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
  color: var(--c-subtle);
  gap: 12px;
  font-size: 14px;
  font-weight: 500;
}

/* ── Skeleton ──────────────────────────────────────────────────── */
.ls-skeleton {
  background: linear-gradient(90deg, var(--c-border-light) 25%, #e9eaec 50%, var(--c-border-light) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease infinite;
}
@keyframes shimmer { from { background-position: 200% 0; } to { background-position: -200% 0; } }

/* ── Body ──────────────────────────────────────────────────────── */
.ls-body { padding: 22px 28px 48px; }

/* ── KPI Row ───────────────────────────────────────────────────── */
.ls-kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.ls-kpi {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 16px 18px 0;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: border-color 0.15s, box-shadow 0.15s, transform 0.15s;
}
.ls-kpi:hover {
  border-color: #d1d5db;
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}
.ls-kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.ls-kpi-icon {
  width: 32px; height: 32px;
  border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.ls-kpi-delta {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
}
.ls-kpi-delta.up { background: var(--c-green-bg); color: var(--c-green); border: 1px solid var(--c-green-border); }
.ls-kpi-delta.dn { background: var(--c-red-bg); color: var(--c-red); border: 1px solid var(--c-red-border); }
.ls-kpi-value {
  font-size: 22px;
  font-weight: 800;
  color: var(--accent, var(--c-text));
  letter-spacing: -0.03em;
  line-height: 1.1;
  margin-bottom: 3px;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.ls-kpi-label {
  font-size: 10.5px;
  color: var(--c-subtle);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  margin-bottom: 12px;
}
.ls-kpi-spark { height: 40px; margin: 0 -18px; }
.ls-kpi-spark svg { width: 100%; height: 100%; display: block; }

/* ── Cards ─────────────────────────────────────────────────────── */
.ls-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.ls-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px 18px 14px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
}
.ls-card-title { font-size: 13px; font-weight: 700; color: var(--c-text); }
.ls-card-sub   { font-size: 11px; color: var(--c-subtle); margin-top: 3px; font-weight: 500; }
.ls-canvas-wrap { padding: 16px 18px; position: relative; }
.ls-canvas-wrap canvas { width: 100% !important; height: 100% !important; display: block; }

/* Legend */
.ls-legend {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 11px;
  color: var(--c-muted);
  font-weight: 600;
}
.ls-legend-dot { width: 8px; height: 8px; border-radius: 2px; display: inline-block; margin-right: 3px; }

/* Chart Layout */
.ls-chart-row { display: flex; gap: 14px; align-items: stretch; }
.ls-chart-main { flex: 2; }
.ls-chart-side { flex: 1; display: flex; flex-direction: column; }

/* Donut Legend */
.ls-donut-legend { padding: 0 18px 16px; display: flex; flex-direction: column; gap: 8px; }
.ls-donut-row { display: flex; align-items: center; gap: 8px; font-size: 12px; }
.ls-donut-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.ls-donut-name { flex: 1; color: var(--c-muted); font-size: 12px; font-weight: 500; }
.ls-donut-val { font-size: 12px; font-weight: 800; color: var(--c-text); font-family: 'JetBrains Mono', monospace; }

/* ── Table Tab ─────────────────────────────────────────────────── */
.ls-badge {
  font-size: 11px;
  font-weight: 700;
  background: var(--c-bg);
  border: 1px solid var(--c-border);
  border-radius: 999px;
  padding: 2px 10px;
  color: var(--c-muted);
}

.ls-table-layout { display: grid; grid-template-columns: 1fr; gap: 14px; }
.ls-table-layout.show-detail { grid-template-columns: 1fr 1fr; }

.ls-period-head {
  display: grid;
  grid-template-columns: 1fr 110px 110px 110px 70px;
  padding: 9px 18px;
  font-size: 10px;
  color: var(--c-subtle);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 700;
  background: #fafbfc;
  border-bottom: 1px solid var(--c-border-light);
}
.ls-period-row {
  display: grid;
  grid-template-columns: 1fr 110px 110px 110px 70px;
  align-items: center;
  padding: 13px 18px;
  border-bottom: 1px solid var(--c-border-light);
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.12s;
}
.ls-period-row:last-child { border-bottom: none; }
.ls-period-row:hover { background: #fafbfc; }
.ls-period-row.active { background: var(--c-blue-bg); border-left-color: var(--c-blue); }
.ls-pr-label { font-size: 13px; font-weight: 700; color: var(--c-text); }
.ls-pr-range { font-size: 10.5px; color: var(--c-subtle); margin-top: 2px; font-weight: 500; font-family: 'JetBrains Mono', monospace; }

/* Numbers */
.ls-num  { font-size: 12.5px; font-weight: 600; font-family: 'JetBrains Mono', monospace; }
.earn    { color: var(--c-blue); }
.deduct  { color: var(--c-red); }
.net-pos { color: var(--c-green); font-weight: 800; }
.net-neg { color: var(--c-red); font-weight: 800; }
.muted   { color: var(--c-subtle); }

/* Breakdown */
.ls-breakdown { display: flex; flex-direction: column; max-height: 640px; }
.ls-close-btn {
  width: 28px; height: 28px;
  background: var(--c-bg);
  border: 1px solid var(--c-border);
  border-radius: 6px;
  cursor: pointer;
  font-size: 11px;
  font-weight: 700;
  color: var(--c-muted);
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.ls-close-btn:hover { border-color: var(--c-red); color: var(--c-red); background: var(--c-red-bg); }

.ls-mini-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-bottom: 1px solid var(--c-border-light);
}
.ls-mini-stat {
  padding: 12px 16px;
  border-right: 1px solid var(--c-border-light);
}
.ls-mini-stat:last-child { border-right: none; }
.ls-mini-label {
  font-size: 9.5px;
  color: var(--c-subtle);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 700;
  margin-bottom: 4px;
}
.ls-mini-val   { font-size: 16px; font-weight: 800; color: var(--c-text); font-family: 'JetBrains Mono', monospace; }

.ls-breakdown-scroll { overflow-y: auto; flex: 1; }

.ls-btable { width: 100%; border-collapse: collapse; font-size: 12.5px; }
.ls-btable th {
  padding: 9px 12px;
  text-align: left;
  color: var(--c-subtle);
  font-size: 10px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 700;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
  white-space: nowrap;
  position: sticky; top: 0; z-index: 1;
}
.ls-btable td {
  padding: 11px 12px;
  border-bottom: 1px solid var(--c-border-light);
  vertical-align: middle;
}
.ls-btable tr:last-child td { border-bottom: none; }
.ls-btable tr:hover td { background: #fafbfc; }

.ls-cust-cell { display: flex; align-items: center; gap: 8px; }
.ls-cust-avatar {
  width: 30px; height: 30px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 800; color: white;
}
.ls-cust-name  { font-size: 12.5px; font-weight: 700; color: var(--c-text); white-space: nowrap; }
.ls-cust-email { font-size: 10.5px; color: var(--c-subtle); margin-top: 1px; }

/* Tier chips */
.ls-tier-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border: 1px solid transparent;
}
.tier-bronze   { background: #fef3c7; color: #92400e; border-color: #fde68a; }
.tier-silver   { background: #f1f5f9; color: #475569; border-color: #e2e8f0; }
.tier-gold     { background: #fef9c3; color: #b45309; border-color: #fde047; }
.tier-platinum { background: #ede9fe; color: #6d28d9; border-color: #c4b5fd; }
.tier-diamond  { background: var(--c-blue-bg); color: #1d4ed8; border-color: #bfdbfe; }
.tier-member   { background: var(--c-bg); color: var(--c-muted); border-color: var(--c-border); }

.ls-empty-small { padding: 28px 18px; color: var(--c-subtle); font-size: 13.5px; font-weight: 500; }

/* ── Transitions ───────────────────────────────────────────────── */
.tab-fade-enter-active, .tab-fade-leave-active { transition: opacity 0.15s, transform 0.15s; }
.tab-fade-enter-from, .tab-fade-leave-to { opacity: 0; transform: translateY(6px); }

.slide-in-enter-active, .slide-in-leave-active { transition: opacity 0.2s, transform 0.2s; }
.slide-in-enter-from { opacity: 0; transform: translateX(16px); }
.slide-in-leave-to   { opacity: 0; transform: translateX(16px); }

/* ── Responsive ────────────────────────────────────────────────── */
@media (max-width: 1100px) {
  .ls-kpi-row   { grid-template-columns: repeat(2, 1fr); }
  .ls-chart-row { flex-direction: column; }
}
@media (max-width: 768px) {
  .ls-header-top { padding: 12px 16px; }
  .ls-header-divider { display: none; }
  .ls-page-title { font-size: 15px; }
  .ls-tab-nav { padding: 0 16px; }
  .ls-body { padding: 14px; }
  .ls-kpi-row { grid-template-columns: 1fr 1fr; }
  .ls-table-layout.show-detail { grid-template-columns: 1fr; }
  .ls-topbar-right { gap: 6px; }
  .ls-status-pill { display: none; }
}

/* ══════════════════════════════════════════════════════
   SPENDING ANALYSIS
══════════════════════════════════════════════════════ */
.sa-root {
  padding: 0;
}

/* Header */
.sa-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 14px;
}

.sa-kicker {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-subtle);
  margin-bottom: 5px;
}
.sa-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--c-text);
  margin: 0 0 4px;
  letter-spacing: -0.03em;
}
.sa-sub { font-size: 13px; color: var(--c-muted); margin: 0; font-weight: 500; }

.sa-btn-reload {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-card);
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px; font-weight: 700; color: var(--c-text);
  cursor: pointer; transition: all 0.15s;
}
.sa-btn-reload:hover { border-color: var(--c-blue); color: var(--c-blue); background: var(--c-blue-bg); }
.sa-btn-reload:disabled { opacity: 0.5; cursor: not-allowed; }

/* SA KPI */
.sa-kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 18px;
}
.sa-kpi {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--shadow-sm);
  transition: all 0.15s;
}
.sa-kpi:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.sa-kpi-icon {
  width: 42px; height: 42px;
  border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.sa-kpi-label {
  font-size: 10.5px;
  color: var(--c-subtle);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  margin-bottom: 4px;
}
.sa-kpi-value { font-size: 19px; font-weight: 800; color: var(--c-text); letter-spacing: -0.03em; }
.sa-kpi-value.sm { font-size: 14px; letter-spacing: -0.01em; }

/* SA Tabs */
.sa-tabs {
  display: flex;
  gap: 3px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 4px;
  margin-bottom: 14px;
  width: fit-content;
  box-shadow: var(--shadow-sm);
}
.sa-tab {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 8px 18px;
  border-radius: var(--radius-sm);
  border: none; background: transparent;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px; font-weight: 700;
  color: var(--c-muted); cursor: pointer; transition: all 0.15s;
}
.sa-tab:hover { color: var(--c-text); background: var(--c-bg); }
.sa-tab.active { background: var(--c-text); color: white; box-shadow: var(--shadow-sm); }

/* SA Panel */
.sa-panel {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

/* Filter Bar */
.sa-filter-bar {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
  flex-wrap: wrap;
}
.sa-filter-bar.wrap { flex-wrap: wrap; }
.sa-filter-group { display: flex; flex-direction: column; gap: 6px; }
.sa-filter-actions { display: flex; gap: 8px; align-items: flex-end; }
.sa-label {
  font-size: 10.5px;
  font-weight: 700;
  color: var(--c-subtle);
  text-transform: uppercase;
  letter-spacing: 0.07em;
}

.sa-select {
  padding: 8px 12px;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: var(--c-text);
  background: var(--c-card);
  outline: none; cursor: pointer; min-width: 140px;
  transition: border-color 0.15s;
}
.sa-select:focus { border-color: var(--c-blue); }

.sa-input {
  padding: 8px 12px;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px; font-weight: 500; color: var(--c-text);
  background: var(--c-card); outline: none; width: 180px;
  transition: border-color 0.15s;
}
.sa-input:focus { border-color: var(--c-blue); }

.sa-number-input {
  display: flex; align-items: center;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--c-card);
}
.sa-number-input button {
  padding: 7px 12px;
  border: none; background: transparent;
  font-size: 16px; cursor: pointer; color: var(--c-muted);
  font-family: 'Plus Jakarta Sans', sans-serif;
  transition: background 0.1s;
}
.sa-number-input button:hover { background: var(--c-bg); color: var(--c-text); }
.sa-number-input span {
  padding: 0 12px;
  font-size: 13px; font-weight: 800; color: var(--c-text);
  min-width: 40px; text-align: center;
  font-family: 'JetBrains Mono', monospace;
}

.sa-btn-primary {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 9px 18px;
  background: var(--c-text); border: 1.5px solid var(--c-text);
  border-radius: var(--radius-sm); color: white;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px; font-weight: 700;
  cursor: pointer; transition: all 0.15s;
}
.sa-btn-primary:hover:not(:disabled) { background: #1f2937; border-color: #1f2937; }
.sa-btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.sa-btn-ghost {
  padding: 9px 18px;
  background: var(--c-card); border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm); color: var(--c-muted);
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px; font-weight: 700;
  cursor: pointer; transition: all 0.15s;
}
.sa-btn-ghost:hover { border-color: #d1d5db; color: var(--c-text); background: var(--c-bg); }

.spinner-sm {
  width: 13px; height: 13px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin-r 0.6s linear infinite;
  display: inline-block;
  flex-shrink: 0;
}

/* Quick ranges */
.sa-quick-ranges {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px 14px;
  border-bottom: 1px solid var(--c-border-light);
  flex-wrap: wrap;
}
.sa-quick-btn {
  padding: 4px 12px;
  border: 1.5px solid var(--c-border);
  border-radius: 999px;
  background: var(--c-card); color: var(--c-muted);
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 12px; font-weight: 700;
  cursor: pointer; transition: all 0.15s;
}
.sa-quick-btn:hover { border-color: var(--c-blue); color: var(--c-blue); background: var(--c-blue-bg); }

/* Range alert / container */
.sa-range-alert {
  padding: 14px 20px;
  background: var(--c-blue-bg);
  border-bottom: 1px solid #bfdbfe;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 600;
}
.sa-range-alert > svg { flex-shrink: 0; margin-top: 2px; }
.sa-range-alert .sa-table-wrap { flex: 1; }
.range-count-badge {
  margin-left: 4px; padding: 2px 10px;
  background: #dbeafe; border-radius: 999px;
  font-size: 11px; font-weight: 700; color: #1d4ed8;
}

/* SA Table */
.sa-table-wrap { overflow-x: auto; }
.sa-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.sa-table th {
  padding: 10px 16px;
  text-align: left;
  font-size: 10px; font-weight: 700;
  color: var(--c-subtle); text-transform: uppercase; letter-spacing: 0.08em;
  background: #fafbfc;
  border-bottom: 1px solid var(--c-border-light);
  white-space: nowrap;
}
.sa-table td {
  padding: 13px 16px;
  border-bottom: 1px solid var(--c-border-light);
  vertical-align: middle;
}
.sa-table tr:last-child td { border-bottom: none; }
.sa-table tbody tr:hover td { background: #fafbfc; }

/* Rank badge */
.rank-badge {
  width: 34px; height: 34px;
  border-radius: var(--radius-sm);
  display: inline-flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 12.5px;
  margin: 0 auto;
  font-family: 'JetBrains Mono', monospace;
}
.rank-gold   { background: #fef9c3; color: #b45309; }
.rank-silver { background: #f1f5f9; color: #475569; }
.rank-bronze { background: #fef3c7; color: #92400e; }
.rank-other  { background: var(--c-bg); color: var(--c-muted); font-size: 11px; }

/* Customer cell */
.cust-cell { display: flex; align-items: center; gap: 10px; }
.cust-av {
  width: 36px; height: 36px;
  border-radius: var(--radius-sm); flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 800; color: white;
}
.cust-av.sm { width: 30px; height: 30px; border-radius: 6px; font-size: 10px; }
.cust-name  { font-size: 13px; font-weight: 700; color: var(--c-text); }
.cust-email { font-size: 11px; color: var(--c-subtle); margin-top: 1px; }

/* Tier chip (sa context) */
.tier-chip {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 10px; font-weight: 700;
  text-transform: uppercase; letter-spacing: 0.05em;
  border: 1px solid transparent;
}

/* Type chip */
.type-chip {
  display: inline-block; padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px; font-weight: 700;
  border: 1px solid transparent;
}
.type-vip { background: #fef9c3; color: #b45309; border-color: #fde047; }
.type-reg { background: var(--c-bg); color: var(--c-muted); border-color: var(--c-border); }

/* Points & spend */
.points-badge {
  display: inline-block; padding: 3px 10px;
  background: var(--c-orange-bg);
  border: 1px solid var(--c-orange-border);
  border-radius: 999px;
  font-size: 12px; font-weight: 700; color: var(--c-orange);
}
.spend-amount {
  font-size: 14px; font-weight: 800; color: var(--c-green);
  font-family: 'JetBrains Mono', monospace;
}
.muted-text { color: var(--c-subtle); font-size: 12px; }
.muted-dash { color: #d1d5db; font-weight: 700; }

/* Skeleton (sa context) */
.sa-table-skeleton { padding: 8px 0; }
.sk-row {
  display: flex; align-items: center; gap: 16px;
  padding: 12px 20px; border-bottom: 1px solid var(--c-border-light);
}
.sk-user { display: flex; align-items: center; gap: 10px; flex: 1; }
.sk-lines { display: flex; flex-direction: column; gap: 6px; flex: 1; }
.sk {
  border-radius: 6px;
  background: linear-gradient(90deg, var(--c-border-light) 25%, #e9eaec 50%, var(--c-border-light) 75%);
  background-size: 200%;
  animation: shimmer 1.4s ease infinite;
}
.sk-rank  { width: 34px; height: 34px; border-radius: var(--radius-sm); }
.sk-av    { width: 36px; height: 36px; border-radius: var(--radius-sm); flex-shrink: 0; }
.sk-l     { height: 12px; }
.sk-pill  { width: 64px; height: 24px; border-radius: 999px; }
.sk-money { width: 100px; height: 18px; }
.sk-id    { width: 40px; height: 16px; }
.w70 { width: 70%; } .w50 { width: 50%; }

/* Empty (sa context) */
.sa-empty {
  padding: 60px 20px; text-align: center;
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  color: var(--c-subtle); font-size: 13.5px; font-weight: 500;
}
.sa-empty p { margin: 0; }

/* Pagination */
.sa-pagination {
  display: flex; align-items: center; justify-content: center;
  gap: 6px; padding: 16px;
  border-top: 1px solid var(--c-border-light);
}
.page-btn {
  min-width: 32px; height: 32px;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-card);
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px; font-weight: 700;
  color: var(--c-text); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  padding: 0 8px; transition: all 0.15s;
}
.page-btn:hover:not(:disabled) { border-color: var(--c-blue); color: var(--c-blue); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-btn.active { background: var(--c-text); border-color: var(--c-text); color: white; }
.page-info { font-size: 12px; color: var(--c-subtle); margin-left: 6px; font-weight: 600; }

/* SA Card (statistics) */
.sa-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.mb-14 { margin-bottom: 14px; }
.sa-card-header {
  padding: 14px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
}
.sa-card-title { font-size: 13px; font-weight: 700; color: var(--c-text); }
.sa-card-sub   { font-size: 11px; color: var(--c-subtle); margin-top: 2px; font-weight: 500; }

/* Range bars */
.sa-ranges { display: flex; flex-direction: column; gap: 14px; padding: 18px 20px; }
.sa-range-row {
  display: grid;
  grid-template-columns: 130px 1fr 50px 100px;
  align-items: center;
  gap: 14px;
}
.sa-range-label { font-size: 13px; font-weight: 600; color: var(--c-text); }
.sa-range-bar-wrap { background: var(--c-border-light); border-radius: 999px; height: 7px; overflow: hidden; }
.sa-range-bar { height: 100%; border-radius: 999px; transition: width 0.6s ease; min-width: 2px; }
.sa-range-pct { font-size: 12px; font-weight: 800; color: var(--c-text); text-align: right; font-family: 'JetBrains Mono', monospace; }
.sa-range-count { font-size: 12px; color: var(--c-subtle); text-align: right; font-weight: 600; }

/* Tier grid */
.sa-tier-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 4px;
}
.sa-tier-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 16px;
  transition: all 0.15s;
  box-shadow: var(--shadow-sm);
}
.sa-tier-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.sa-tier-card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.sa-tier-count { font-size: 12px; color: var(--c-subtle); font-weight: 600; }
.sa-tier-divider { height: 1px; background: var(--c-border-light); margin: 10px 0; }
.sa-tier-row { display: flex; align-items: center; justify-content: space-between; }
.sa-tier-lbl {
  font-size: 10px; color: var(--c-subtle); font-weight: 700;
  text-transform: uppercase; letter-spacing: 0.07em;
}
.sa-tier-val { font-size: 15px; font-weight: 800; color: var(--c-text); font-family: 'JetBrains Mono', monospace; }
.sa-tier-val.blue { color: var(--c-blue); }

/* SA Responsive */
@media (max-width: 1100px) {
  .sa-kpi-row { grid-template-columns: repeat(2, 1fr); }
  .sa-tier-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .sa-kpi-row { grid-template-columns: 1fr 1fr; }
  .sa-tier-grid { grid-template-columns: 1fr; }
  .sa-range-row { grid-template-columns: 1fr 1fr; }
  .sa-tabs { flex-wrap: wrap; }
  .sa-header { flex-direction: column; align-items: flex-start; }
}
</style>