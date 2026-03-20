<template>
  <div class="pm2-page">

    <!-- ── Header ── -->
    <header class="pm2-header">
      <div class="pm2-header__left">
        <div class="pm2-eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
          Hệ thống
        </div>
        <h1 class="pm2-title">
          Quản lý <span class="pm2-title__accent">khuyến mãi</span>
        </h1>
        <p class="pm2-subtitle">Tạo và quản lý các chiến dịch khuyến mãi, xung đột và báo cáo</p>
      </div>

      <div class="pm2-header__right">
        <div class="pm2-header-actions">
          <button class="pm2-btn pm2-btn--outline" :class="{ 'pm2-btn--loading': conflictLoading }" @click="loadConflicts" :disabled="conflictLoading">
            <svg v-if="!conflictLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/>
              <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <span v-if="!conflictLoading">Conflicts</span>
            <span v-else class="pm2-spinner"></span>
          </button>
          <button class="pm2-btn pm2-btn--outline" :class="{ 'pm2-btn--loading': expiringLoading }" @click="loadExpiring" :disabled="expiringLoading">
            <svg v-if="!expiringLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
            </svg>
            <span v-if="!expiringLoading">Sắp hết hạn</span>
            <span v-else class="pm2-spinner"></span>
          </button>
          <button class="pm2-btn pm2-btn--outline" :class="{ 'pm2-btn--loading': activeReportLoading }" @click="loadActiveReport" :disabled="activeReportLoading">
            <svg v-if="!activeReportLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span v-if="!activeReportLoading">KM Active</span>
            <span v-else class="pm2-spinner"></span>
          </button>
          <button class="pm2-btn pm2-btn--outline" :class="{ 'pm2-btn--loading': reportLoading || summaryReportLoading }" @click="loadCombinedReport" :disabled="reportLoading || summaryReportLoading">
            <svg v-if="!reportLoading && !summaryReportLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/>
              <line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
            <span v-if="!reportLoading && !summaryReportLoading">Báo cáo</span>
            <span v-else class="pm2-spinner"></span>
          </button>
          <button class="pm2-btn pm2-btn--primary" @click="openCreate">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Tạo mới
          </button>
        </div>
      </div>
    </header>

    <!-- ── Validate bar ── -->
    <div id="prm-validate-bar" class="pm2-card pm2-validate-bar pm2-mb">
      <div class="pm2-validate-bar__label">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        Validate mã KM
      </div>
      <div class="pm2-validate-bar__inputs">
        <div class="pm2-vfield">
          <span class="pm2-vfield__pre">CODE</span>
          <input v-model="validateCode" class="pm2-vfield__input" placeholder="VD: GAMING10" />
        </div>
        <div class="pm2-vfield">
          <span class="pm2-vfield__pre">₫</span>
          <input v-model.number="validateTotal" class="pm2-vfield__input pm2-vfield__input--num" placeholder="Tổng đơn" type="number" />
        </div>
        <button class="pm2-btn pm2-btn--primary" :class="{ 'pm2-btn--loading': validateLoading }" @click="doValidate" :disabled="validateLoading">
          <svg v-if="!validateLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          <span v-if="!validateLoading">Kiểm tra</span>
          <span v-else class="pm2-spinner pm2-spinner--white"></span>
        </button>
        <button v-if="validateResult" class="pm2-icon-btn" @click="validateResult = null">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <transition name="pm2-fade">
        <div v-if="validateResult" class="pm2-validate-result" :class="validateResult.valid ? 'pm2-validate-result--ok' : 'pm2-validate-result--fail'">
          <template v-if="validateResult.valid">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            Hợp lệ — giảm <strong>{{ fmtMoney(validateResult.discountAmount) }}</strong>
            <span v-if="validateResult.promotionName" class="pm2-tag pm2-tag--orange" style="margin-left:6px">{{ validateResult.promotionName }}</span>
          </template>
          <template v-else>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            {{ validateResult.message || 'Mã không hợp lệ' }}
          </template>
        </div>
      </transition>
    </div>

    <!-- ── Conflicts panel ── -->
    <transition name="pm2-slide">
      <div v-if="conflicts !== null" class="pm2-card pm2-panel pm2-mb">
        <div class="pm2-panel__head">
          <div class="pm2-panel__head-left">
            <span class="pm2-panel__icon pm2-panel__icon--orange">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            </span>
            <span class="pm2-panel__title">Xung đột khuyến mãi</span>
            <span v-if="conflicts.length" class="pm2-tag pm2-tag--red">{{ conflicts.length }}</span>
          </div>
          <button class="pm2-icon-btn" @click="conflicts = null">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="pm2-panel__body">
          <div v-if="!conflicts.length" class="pm2-ok-msg">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            Không có xung đột
          </div>
          <div v-else class="pm2-table-wrap">
            <table class="pm2-table">
              <thead>
                <tr>
                  <th>Khuyến mãi 1</th>
                  <th>Khuyến mãi 2</th>
                  <th style="text-align:center">Stackable</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(r, idx) in conflicts" :key="idx" class="pm2-row">
                  <td><span class="pm2-code">{{ r.promotion1 }}</span></td>
                  <td><span class="pm2-code">{{ r.promotion2 }}</span></td>
                  <td style="text-align:center">
                    <span class="pm2-tag" :class="r.stackable ? 'pm2-tag--green' : 'pm2-tag--red'">
                      {{ r.stackable ? '✅ Có' : '❌ Không' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </transition>

    <!-- ── Expiring panel ── -->
    <transition name="pm2-slide">
      <div v-if="expiringRows !== null" class="pm2-card pm2-panel pm2-mb">
        <div class="pm2-panel__head">
          <div class="pm2-panel__head-left">
            <span class="pm2-panel__icon pm2-panel__icon--orange">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            </span>
            <span class="pm2-panel__title">Sắp hết hạn (3 ngày)</span>
            <span class="pm2-tag" :class="expiringRows.length ? 'pm2-tag--orange' : 'pm2-tag--green'">{{ expiringRows.length }}</span>
          </div>
          <button class="pm2-icon-btn" @click="expiringRows = null">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="pm2-panel__body">
          <div v-if="!expiringRows.length" class="pm2-ok-msg">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            Không có KM nào sắp hết hạn
          </div>
          <div v-else class="pm2-table-wrap">
            <table class="pm2-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Code</th>
                  <th>Tên</th>
                  <th>Hết hạn</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in expiringRows" :key="r.id" class="pm2-row">
                  <td><span class="pm2-mono-id">{{ r.id }}</span></td>
                  <td><span class="pm2-code">{{ r.code }}</span></td>
                  <td>{{ r.name }}</td>
                  <td><span class="pm2-tag pm2-tag--orange">{{ fmtDate(r.endDate) }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </transition>

    <!-- ── Active report panel ── -->
    <transition name="pm2-slide">
      <div v-if="activeReportData !== null" class="pm2-card pm2-panel pm2-mb">
        <div class="pm2-panel__head">
          <div class="pm2-panel__head-left">
            <span class="pm2-panel__icon pm2-panel__icon--green">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            </span>
            <span class="pm2-panel__title">Khuyến mãi đang áp dụng</span>
            <span class="pm2-tag pm2-tag--green">{{ Array.isArray(activeReportData) ? activeReportData.length : 0 }}</span>
          </div>
          <button class="pm2-icon-btn" @click="activeReportData = null">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="pm2-panel__body">
          <div v-if="!activeReportData?.length" class="pm2-ok-msg">Không có KM nào đang chạy</div>
          <div v-else class="pm2-table-wrap">
            <table class="pm2-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Code</th>
                  <th>Tên</th>
                  <th>Loại giảm</th>
                  <th>Hết hạn</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in activeReportData" :key="r.id" class="pm2-row">
                  <td><span class="pm2-mono-id">{{ r.id }}</span></td>
                  <td><span class="pm2-code">{{ r.code }}</span></td>
                  <td>{{ r.name }}</td>
                  <td><span class="pm2-tag pm2-tag--blue">{{ r.discountType }}</span></td>
                  <td><span class="pm2-tag pm2-tag--orange">{{ fmtDate(r.endDate) }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </transition>

    <!-- ── Combined report panel ── -->
    <transition name="pm2-slide">
      <div v-if="reportData !== null || summaryReportData !== null" class="pm2-card pm2-panel pm2-mb">
        <div class="pm2-panel__head">
          <div class="pm2-panel__head-left">
            <span class="pm2-panel__icon pm2-panel__icon--blue">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
            </span>
            <span class="pm2-panel__title">Báo cáo khuyến mãi</span>
            <div class="pm2-period-tabs">
              <button class="pm2-period-btn" :class="{ 'pm2-period-btn--active': reportPeriod === 'week' }" @click="reportPeriod = 'week'; loadCombinedReport();">Tuần</button>
              <button class="pm2-period-btn" :class="{ 'pm2-period-btn--active': reportPeriod === 'month' }" @click="reportPeriod = 'month'; loadCombinedReport();">Tháng</button>
            </div>
          </div>
          <button class="pm2-icon-btn" @click="reportData = null; summaryReportData = null;">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="pm2-panel__body">
          <!-- Stats -->
          <div class="pm2-report-section-label">Tổng quan</div>
          <div class="pm2-report-stats">
            <div class="pm2-rstat">
              <div class="pm2-rstat__num">{{ reportData?.total ?? 0 }}</div>
              <div class="pm2-rstat__label">Tổng KM</div>
            </div>
            <div class="pm2-rstat pm2-rstat--green">
              <div class="pm2-rstat__num">{{ reportData?.activeCount ?? 0 }}</div>
              <div class="pm2-rstat__label">Đang chạy</div>
            </div>
            <div class="pm2-rstat pm2-rstat--orange">
              <div class="pm2-rstat__num">{{ reportData?.comboCount ?? summaryReportData?.comboCount ?? 0 }}</div>
              <div class="pm2-rstat__label">Combo</div>
            </div>
            <div class="pm2-rstat pm2-rstat--blue">
              <div class="pm2-rstat__num">{{ reportData?.usageLimitedCount ?? summaryReportData?.usageLimitedCount ?? 0 }}</div>
              <div class="pm2-rstat__label">Giới hạn lượt</div>
            </div>
            <div class="pm2-rstat pm2-rstat--red pm2-rstat--clickable" title="Click để xem chi tiết" @click="openRedemptionAllDetail()">
              <div class="pm2-rstat__num pm2-rstat__num--underline">{{ summaryReportData?.totalRedemptions ?? reportData?.totalRedemptions ?? 0 }}</div>
              <div class="pm2-rstat__label">Tổng lượt dùng 🔍</div>
            </div>
          </div>

          <!-- Ratio bars -->
          <div class="pm2-report-section-label" style="margin-top:20px">Tỉ lệ hoạt động</div>
          <div class="pm2-ratios">
            <div class="pm2-ratio">
              <div class="pm2-ratio__label">
                <span>Đang chạy</span>
                <span class="pm2-ratio__val pm2-text--green">
                  {{ reportData?.total ? Math.round((reportData.activeCount / reportData.total) * 100) : 0 }}%
                </span>
              </div>
              <div class="pm2-ratio__bar">
                <div class="pm2-ratio__fill pm2-ratio__fill--green" :style="{ width: reportData?.total ? (reportData.activeCount / reportData.total) * 100 + '%' : '0%' }"></div>
              </div>
            </div>
            <div class="pm2-ratio">
              <div class="pm2-ratio__label">
                <span>Combo</span>
                <span class="pm2-ratio__val pm2-text--orange">
                  {{ reportData?.total ? Math.round(((reportData?.comboCount ?? 0) / reportData.total) * 100) : 0 }}%
                </span>
              </div>
              <div class="pm2-ratio__bar">
                <div class="pm2-ratio__fill pm2-ratio__fill--orange" :style="{ width: reportData?.total ? ((reportData?.comboCount ?? 0) / reportData.total) * 100 + '%' : '0%' }"></div>
              </div>
            </div>
            <div class="pm2-ratio">
              <div class="pm2-ratio__label">
                <span>Giới hạn lượt</span>
                <span class="pm2-ratio__val pm2-text--blue">
                  {{ reportData?.total ? Math.round(((reportData?.usageLimitedCount ?? 0) / reportData.total) * 100) : 0 }}%
                </span>
              </div>
              <div class="pm2-ratio__bar">
                <div class="pm2-ratio__fill pm2-ratio__fill--blue" :style="{ width: reportData?.total ? ((reportData?.usageLimitedCount ?? 0) / reportData.total) * 100 + '%' : '0%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- ── Main table ── -->
    <div id="prm-main-table" class="pm2-card">
      <!-- Toolbar -->
      <div class="pm2-toolbar">
        <div class="pm2-toolbar__left">
          <span class="pm2-count">
            <span class="pm2-count__num">{{ displayRows.length }}</span>
            <span class="pm2-count__label">/ {{ rows.length }} khuyến mãi</span>
          </span>
          <span v-if="!showInactive" class="pm2-tag pm2-tag--green" style="font-size:11px">🟢 Chỉ ACTIVE</span>
        </div>
        <div class="pm2-toolbar__right">
          <label class="pm2-switch">
            <input type="checkbox" v-model="showInactive" />
            <span class="pm2-switch__track"></span>
            <span class="pm2-switch__label" :class="{ 'pm2-text--muted': !showInactive }">
              {{ showInactive ? 'Hiện tất cả' : 'Ẩn km ff' }}
            </span>
          </label>
        </div>
      </div>

      <!-- Table -->
      <div class="pm2-table-wrap">
        <table class="pm2-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Code</th>
              <th>Tên chiến dịch</th>
              <th>Loại giảm</th>
              <th>Đối tượng</th>
              <th style="text-align:center">Giới hạn</th>
              <th style="text-align:center">Ưu tiên</th>
              <th>Thời gian</th>
              <th style="text-align:center">Trạng thái</th>
              <th style="text-align:center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="10" class="pm2-loading-row">
                <span class="pm2-spinner"></span> Đang tải...
              </td>
            </tr>
            <tr v-else-if="!rows.length">
              <td colspan="10" class="pm2-empty-row">Không có dữ liệu</td>
            </tr>
            <tr v-for="row in displayRows" :key="row.id" class="pm2-row">
              <td><span class="pm2-mono-id">{{ row.id }}</span></td>
              <td><span class="pm2-code">{{ row.code }}</span></td>
              <td class="pm2-name-cell">{{ row.name }}</td>
              <td>
                <template v-if="row.buyQty">
                  <span class="pm2-text--green pm2-bold">Mua {{ row.buyQty }} tặng {{ row.getQty }}</span>
                  <div class="pm2-disc-type">COMBO</div>
                </template>
                <template v-else>
                  <span class="pm2-bold">{{ row.discountValue }}{{ row.discountType === 'PERCENT' ? '%' : ' ₫' }}</span>
                  <div class="pm2-disc-type">{{ row.discountType }}</div>
                </template>
              </td>
              <td>
                <span v-if="row.vipTiers?.length" class="pm2-tag pm2-tag--purple">VIP {{ row.vipTiers.join(', ') }}</span>
                <span v-else-if="row.customerTypes?.length" class="pm2-tag pm2-tag--blue">{{ row.customerTypes.join(', ') }}</span>
                <span v-else class="pm2-text--muted pm2-small">All</span>
              </td>
              <td style="text-align:center">
                <span v-if="row.usageLimit" class="pm2-tag pm2-tag--blue">{{ row.usageLimit }}</span>
                <span v-else class="pm2-text--muted">∞</span>
              </td>
              <td style="text-align:center"><span class="pm2-mono pm2-bold">{{ row.priority }}</span></td>
              <td>
                <div class="pm2-dates">
                  <span>{{ fmtDate(row.startDate) }}</span>
                  <span class="pm2-dates__sep">→</span>
                  <span>{{ fmtDate(row.endDate) }}</span>
                </div>
              </td>
              <td style="text-align:center">
                <span class="pm2-status" :class="row.isActive ? 'pm2-status--on' : 'pm2-status--off'">
                  {{ row.isActive ? 'ACTIVE' : 'OFF' }}
                </span>
              </td>
              <td>
                <div class="pm2-actions">
                  <button class="pm2-action-btn pm2-action-btn--edit" @click="openEdit(row)">Sửa</button>
                  <button class="pm2-action-btn pm2-action-btn--blue" @click="openPreview(row)">Preview</button>
                  <button class="pm2-action-btn pm2-action-btn--purple" @click="openRedemptionDetail(row.id, row.code)">Lượt dùng</button>
                  <button class="pm2-action-btn pm2-action-btn--delete" @click="remove(row)">Xóa</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ── Preview panel ── -->
    <transition name="pm2-slide">
      <div v-if="previewPromo" class="pm2-card pm2-panel pm2-mt">
        <div class="pm2-panel__head">
          <div class="pm2-panel__head-left">
            <span class="pm2-panel__icon pm2-panel__icon--blue">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            </span>
            <span class="pm2-panel__title">Xem giá —</span>
            <span class="pm2-code">{{ previewPromo.code }}</span>
          </div>
          <div class="pm2-preview-inputs">
            <input v-model.number="previewProductId" class="pm2-input" placeholder="Product ID" style="width:120px" type="number" />
            <input v-model.number="previewCustomerId" class="pm2-input" placeholder="Customer ID (opt)" style="width:150px" type="number" />
            <button class="pm2-btn pm2-btn--primary" :class="{ 'pm2-btn--loading': previewLoading }" @click="loadPreviewPrices" :disabled="previewLoading">
              <span v-if="!previewLoading">Tải giá</span>
              <span v-else class="pm2-spinner pm2-spinner--white"></span>
            </button>
          </div>
          <button class="pm2-icon-btn" @click="previewPromo = null; previewPrices = [];">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="pm2-panel__body">
          <div v-if="previewPrices.length" class="pm2-table-wrap">
            <table class="pm2-table">
              <thead>
                <tr>
                  <th>Variant ID</th>
                  <th>Tên variant</th>
                  <th>SKU</th>
                  <th>Currency</th>
                  <th>Giá gốc</th>
                  <th>Giá sau KM</th>
                  <th>Mã KM áp dụng</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in previewPrices" :key="row.variantId" class="pm2-row">
                  <td><span class="pm2-mono-id">{{ row.variantId }}</span></td>
                  <td>{{ row.variantName }}</td>
                  <td><span class="pm2-mono pm2-text--muted">{{ row.sku }}</span></td>
                  <td><span class="pm2-mono pm2-text--muted">{{ row.currencyCode }}</span></td>
                  <td>{{ fmtMoney(row.price) }}</td>
                  <td :class="row.finalPrice < row.price ? 'pm2-text--green pm2-bold' : ''">{{ fmtMoney(row.finalPrice) }}</td>
                  <td>
                    <span v-if="row.promotionCode" class="pm2-tag pm2-tag--orange">{{ row.promotionCode }}</span>
                    <span v-else class="pm2-text--muted">—</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else-if="previewLoaded" class="pm2-empty">Không có dữ liệu</div>
        </div>
      </div>
    </transition>

    <!-- ════════════════════════════════════════
         DIALOG: Tạo / Sửa khuyến mãi
    ═════════════════════════════════════════ -->
    <div v-if="dlg.open" class="pm2-overlay" @click.self="dlg.open = false">
      <div class="pm2-dialog pm2-dialog--wide">

        <!-- Decorative band -->
        <div class="pm2-dialog__band" :class="dlg.mode === 'create' ? 'pm2-dialog__band--blue' : 'pm2-dialog__band--orange'">
          <div class="pm2-dialog__band-circles">
            <span class="pm2-band-circle pm2-band-circle--1"/>
            <span class="pm2-band-circle pm2-band-circle--2"/>
            <span class="pm2-band-circle pm2-band-circle--3"/>
          </div>
          <div class="pm2-dialog__icon-wrap">
            <div class="pm2-dialog__icon-ring">
              <svg v-if="dlg.mode === 'create'" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              <svg v-else width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
            </div>
          </div>
        </div>

        <!-- Body -->
        <div class="pm2-dialog__body">
          <div class="pm2-dialog__badge" :class="dlg.mode === 'create' ? 'pm2-dialog__badge--blue' : 'pm2-dialog__badge--orange'">
            {{ dlg.mode === 'create' ? 'Tạo mới' : 'Chỉnh sửa' }}
          </div>
          <div class="pm2-dialog__head-row">
            <h2 class="pm2-dialog__title">{{ dlg.mode === 'create' ? 'Tạo khuyến mãi mới' : 'Sửa khuyến mãi' }}</h2>
            <button class="pm2-icon-btn" @click="dlg.open = false">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <div v-if="dlg.alert" class="pm2-dialog__notice pm2-dialog__notice--red">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {{ dlg.alert }}
          </div>

          <!-- Section: Thông tin cơ bản -->
          <div class="pm2-dlg-section">Thông tin cơ bản</div>
          <div class="pm2-row2">
            <div class="pm2-field">
              <label class="pm2-field__label">Code <span class="pm2-required">*</span></label>
              <input v-model="dlg.form.code" class="pm2-input" :disabled="dlg.mode === 'edit'" placeholder="GAMING_Q2_2026" />
            </div>
            <div class="pm2-field">
              <label class="pm2-field__label">Tên chiến dịch <span class="pm2-required">*</span></label>
              <input v-model="dlg.form.name" class="pm2-input" placeholder="Sale Laptop Gaming Q2/2026" />
            </div>
          </div>
          <div class="pm2-field">
            <label class="pm2-field__label">Mô tả</label>
            <textarea v-model="dlg.form.description" class="pm2-input pm2-textarea" rows="2" placeholder="Mô tả ngắn..."></textarea>
          </div>

          <!-- Section: Giảm giá -->
          <div class="pm2-dlg-section">Giảm giá</div>
          <div class="pm2-row3">
            <div class="pm2-field">
              <label class="pm2-field__label">Loại giảm</label>
              <select v-model="dlg.form.discountType" class="pm2-input pm2-select">
                <option value="PERCENT">PERCENT (%)</option>
                <option value="AMOUNT">AMOUNT (₫)</option>
              </select>
            </div>
            <div class="pm2-field">
              <label class="pm2-field__label">Giá trị giảm</label>
              <input v-model.number="dlg.form.discountValue" class="pm2-input" type="number" :min="0" placeholder="10" />
            </div>
            <div class="pm2-field">
              <label class="pm2-field__label">Ưu tiên (priority)</label>
              <input v-model.number="dlg.form.priority" class="pm2-input" type="number" :min="0" placeholder="1" />
            </div>
          </div>

          <!-- Section: Combo -->
          <div class="pm2-dlg-section">🎁 Combo <span class="pm2-dlg-section__hint">(để trống nếu không phải combo)</span></div>
          <div class="pm2-row2">
            <div class="pm2-field">
              <label class="pm2-field__label">Số lượng mua (buyQty)</label>
              <input v-model.number="dlg.form.buyQty" class="pm2-input" type="number" :min="0" placeholder="VD: 2" />
            </div>
            <div class="pm2-field">
              <label class="pm2-field__label">Số lượng tặng (getQty)</label>
              <input v-model.number="dlg.form.getQty" class="pm2-input" type="number" :min="0" placeholder="VD: 1" />
            </div>
          </div>

          <!-- Section: Thời gian & giới hạn -->
          <div class="pm2-dlg-section">Thời gian &amp; Giới hạn</div>
          <div class="pm2-row3">
            <div class="pm2-field">
              <label class="pm2-field__label">Ngày bắt đầu</label>
              <input v-model="dlg.form.startDate" class="pm2-input" type="datetime-local" />
            </div>
            <div class="pm2-field">
              <label class="pm2-field__label">Ngày kết thúc</label>
              <input v-model="dlg.form.endDate" class="pm2-input" type="datetime-local" />
            </div>
            <div class="pm2-field">
              <label class="pm2-field__label">Giới hạn lượt dùng</label>
              <input v-model.number="dlg.form.usageLimit" class="pm2-input" type="number" :min="0" placeholder="Không giới hạn" />
            </div>
          </div>

          <!-- Section: Phạm vi -->
          <div class="pm2-dlg-section">Phạm vi áp dụng</div>
          <div class="pm2-row3">
            <div class="pm2-field">
              <label class="pm2-field__label">Scope</label>
              <select v-model="dlg.form.scope" class="pm2-input pm2-select">
                <option value="ALL">ALL</option>
                <option value="PRODUCT">PRODUCT</option>
                <option value="VARIANT">VARIANT</option>
              </select>
            </div>
            <div class="pm2-field pm2-field--toggle">
              <label class="pm2-field__label">Stackable</label>
              <label class="pm2-toggle">
                <input type="checkbox" v-model="dlg.form.stackable" />
                <span class="pm2-toggle__track"></span>
              </label>
            </div>
            <div class="pm2-field pm2-field--toggle">
              <label class="pm2-field__label">Kích hoạt</label>
              <label class="pm2-toggle">
                <input type="checkbox" v-model="dlg.form.isActive" />
                <span class="pm2-toggle__track"></span>
              </label>
            </div>
          </div>
          <div class="pm2-row2">
            <div class="pm2-field">
              <label class="pm2-field__label">Product IDs <span class="pm2-text--muted" style="font-weight:400;text-transform:none;letter-spacing:0">(cách nhau bởi dấu phẩy)</span></label>
              <input v-model="dlg.form.productIdsText" class="pm2-input" placeholder="1,2,3" />
            </div>
            <div class="pm2-field">
              <label class="pm2-field__label">Variant IDs <span class="pm2-text--muted" style="font-weight:400;text-transform:none;letter-spacing:0">(cách nhau bởi dấu phẩy)</span></label>
              <input v-model="dlg.form.variantIdsText" class="pm2-input" placeholder="10,11,12" />
            </div>
          </div>

          <!-- Section: Đối tượng -->
          <div class="pm2-dlg-section">👥 Đối tượng khách hàng <span class="pm2-dlg-section__hint">(để trống = tất cả)</span></div>
          <div class="pm2-row2">
            <div class="pm2-field">
              <label class="pm2-field__label">Loại khách hàng (customerTypes)</label>
              <div class="pm2-checkgroup">
                <label v-for="t in ['VIP', 'REGULAR', 'NEW']" :key="t" class="pm2-check">
                  <input type="checkbox" :value="t" v-model="dlg.form.customerTypes" />
                  <span>{{ t }}</span>
                </label>
              </div>
            </div>
            <div class="pm2-field">
              <label class="pm2-field__label">Hạng VIP (vipTiers)</label>
              <div class="pm2-checkgroup">
                <label v-for="t in ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM']" :key="t" class="pm2-check">
                  <input type="checkbox" :value="t" v-model="dlg.form.vipTiers" />
                  <span>{{ t }}</span>
                </label>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="pm2-dialog__actions">
            <button class="pm2-btn pm2-btn--ghost" @click="dlg.open = false">Hủy bỏ</button>
            <button class="pm2-btn" :class="[dlg.mode === 'create' ? 'pm2-btn--confirm-blue' : 'pm2-btn--confirm-orange', { 'pm2-btn--loading': dlg.loading }]" @click="save" :disabled="dlg.loading">
              <span v-if="dlg.loading" class="pm2-spinner pm2-spinner--white"></span>
              <svg v-else-if="dlg.mode === 'create'" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
              {{ dlg.mode === 'create' ? 'Tạo mới' : 'Lưu thay đổi' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ════════════════════════════════════════
         DIALOG: Lượt dùng
    ═════════════════════════════════════════ -->
    <div v-if="redemptionModal.open" class="pm2-overlay" @click.self="redemptionModal.open = false">
      <div class="pm2-dialog pm2-dialog--xl">

        <!-- Band -->
        <div class="pm2-dialog__band pm2-dialog__band--purple">
          <div class="pm2-dialog__band-circles">
            <span class="pm2-band-circle pm2-band-circle--1"/>
            <span class="pm2-band-circle pm2-band-circle--2"/>
            <span class="pm2-band-circle pm2-band-circle--3"/>
          </div>
          <div class="pm2-dialog__icon-wrap">
            <div class="pm2-dialog__icon-ring">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                <line x1="1" y1="10" x2="23" y2="10"/>
              </svg>
            </div>
          </div>
        </div>

        <!-- Body -->
        <div class="pm2-dialog__body">
          <div class="pm2-dialog__badge pm2-dialog__badge--purple">Chi tiết lượt dùng</div>
          <div class="pm2-dialog__head-row">
            <h2 class="pm2-dialog__title">
              <span class="pm2-code">{{ redemptionModal.promotionCode }}</span>
              <span class="pm2-tag pm2-tag--red" style="font-size:12px">{{ redemptionModal.rows.length }} lượt</span>
            </h2>
            <button class="pm2-icon-btn" @click="redemptionModal.open = false">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <div v-if="redemptionModal.loading" class="pm2-loading-center">
            <span class="pm2-spinner"></span> Đang tải...
          </div>
          <div v-else-if="!redemptionModal.rows.length" class="pm2-ok-msg">
            Chưa có lượt dùng nào
          </div>
          <div v-else class="pm2-table-wrap">
            <table class="pm2-table">
              <thead>
                <tr>
                  <th style="width:40px;text-align:center">#</th>
                  <th>Mã đơn</th>
                  <th>Khách hàng</th>
                  <th style="text-align:right">Giảm được</th>
                  <th style="text-align:right">Tổng đơn</th>
                  <th style="text-align:center">Thời gian</th>
                  <th style="text-align:center">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(r, idx) in redemptionModal.rows" :key="r.orderId" class="pm2-row">
                  <td style="text-align:center"><span class="pm2-mono pm2-text--muted">{{ idx + 1 }}</span></td>
                  <td><span class="pm2-code">{{ r.orderNumber }}</span></td>
                  <td>
                    <div class="pm2-customer-cell">
                      <span class="pm2-customer-cell__name">{{ r.customerName && r.customerName !== '—' ? r.customerName : '—' }}</span>
                      <span v-if="r.customerId" class="pm2-customer-cell__id pm2-mono">#{{ r.customerId }}</span>
                    </div>
                  </td>
                  <td style="text-align:right"><span class="pm2-text--green pm2-bold">{{ fmtMoney(r.discountTotal) }}</span></td>
                  <td style="text-align:right"><span class="pm2-bold">{{ fmtMoney(r.totalAmount) }}</span></td>
                  <td style="text-align:center">
                    <div class="pm2-time-cell">
                      <span class="pm2-time-cell__time">{{ r.usedAt ? new Date(r.usedAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : '—' }}</span>
                      <span class="pm2-time-cell__date pm2-text--muted">{{ r.usedAt ? new Date(r.usedAt).toLocaleDateString('vi-VN') : '' }}</span>
                    </div>
                  </td>
                  <td style="text-align:center">
                    <span
                      class="pm2-status"
                      :class="r.status === 'DELIVERED' ? 'pm2-status--on' : r.status === 'CANCELLED' ? 'pm2-status--off' : 'pm2-status--on'"
                      :style="r.status === 'PENDING' ? 'background:var(--pm2-orange-bg);color:var(--pm2-orange);border-color:var(--pm2-orange-border)' : r.status === 'CANCELLED' ? 'background:var(--pm2-red-bg);color:var(--pm2-red);border-color:var(--pm2-red-border)' : ''"
                    >{{ r.status }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="pm2-dialog__actions">
            <button class="pm2-btn pm2-btn--ghost" @click="redemptionModal.open = false">Đóng</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from "vue";
import { promotionsApi } from "../../api/promotions.api";
import { pricesApi } from "../../api/prices.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";
import "../../assets/styles/promotion-manager.css";

const loading = ref(false);
const activeOnly = ref(false);
const rows = ref([]);
const showInactive = ref(false);

const displayRows = computed(() =>
  showInactive.value ? rows.value : rows.value.filter((r) => r.isActive),
);

function extractList(p) {
  if (!p) return [];
  if (Array.isArray(p)) return p;
  const r = p?.data ?? p;
  if (Array.isArray(r)) return r;
  for (const k of ["content", "items", "results", "rows", "list"]) {
    if (Array.isArray(r?.[k])) return r[k];
    if (Array.isArray(r?.data?.[k])) return r.data[k];
  }
  return [];
}

function parseJ(json, key) {
  try {
    return JSON.parse(json)?.[key] ?? null;
  } catch {
    return null;
  }
}

function normalize(list) {
  return (list || []).map((p) => {
    const rules = parseJ(p?.rulesJson, null);
    const rulesObj = (() => {
      try {
        return p?.rulesJson ? JSON.parse(p.rulesJson) : {};
      } catch {
        return {};
      }
    })();
    const combo = rulesObj?.combo ?? null;
    const appObj = (() => {
      try {
        return p?.applicabilityJson ? JSON.parse(p.applicabilityJson) : {};
      } catch {
        return {};
      }
    })();

    return {
      id: p?.id ?? p?.promotionId,
      code: p?.code ?? "",
      name: p?.name ?? "",
      description: p?.description ?? "",
      discountType: p?.discountType ?? "",
      discountValue: p?.discountValue ?? 0,
      startDate: p?.startDate ?? "",
      endDate: p?.endDate ?? "",
      scope: appObj?.scope ?? p?.scope ?? "ALL",
      productIds: appObj?.product_ids ?? p?.productIds ?? [],
      variantIds: appObj?.variant_ids ?? p?.variantIds ?? [],
      priority: p?.priority ?? 0,
      stackable: !!p?.stackable,
      isActive: !!p?.isActive,
      buyQty: combo?.buy_qty ?? p?.buyQty ?? null,
      getQty: combo?.get_qty ?? p?.getQty ?? null,
      usageLimit: rulesObj?.usage_limit ?? p?.usageLimit ?? null,
      customerTypes: appObj?.customer_types ?? p?.customerTypes ?? [],
      vipTiers: appObj?.vip_tiers ?? p?.vipTiers ?? [],
    };
  });
}

async function load() {
  loading.value = true;
  try {
    const r = await promotionsApi.list(activeOnly.value);
    rows.value = normalize(extractList(r?.data)).sort(
      (a, b) => Number(b.isActive) - Number(a.isActive),
    );
  } catch {
    rows.value = [];
    toast("Tải thất bại.", "error");
  } finally {
    loading.value = false;
  }
}

// Validate
const validateCode = ref("");
const validateTotal = ref(0);
const validateLoading = ref(false);
const validateResult = ref(null);
async function doValidate() {
  if (!validateCode.value) return toast("Nhập mã KM.", "warning");
  validateLoading.value = true;
  validateResult.value = null;
  try {
    const r = await promotionsApi.validate(
      validateCode.value,
      validateTotal.value || 0,
    );
    validateResult.value = r?.data?.data ?? r?.data ?? null;
  } catch (e) {
    validateResult.value = {
      valid: false,
      message: e?.response?.data?.message || "Mã không hợp lệ",
    };
  } finally {
    validateLoading.value = false;
  }
}

// Conflicts
const conflictLoading = ref(false);
const conflicts = ref(null);
async function loadConflicts() {
  conflictLoading.value = true;
  try {
    const r = await promotionsApi.getConflicts();
    const raw = r?.data?.data ?? r?.data ?? [];
    conflicts.value = Array.isArray(raw) ? raw : [];
  } catch {
    toast("Tải conflicts thất bại.", "error");
  } finally {
    conflictLoading.value = false;
  }
}

// Active promotions report
const activeReportLoading = ref(false);
const activeReportData = ref(null);
async function loadActiveReport() {
  activeReportLoading.value = true;
  try {
    const r = await promotionsApi.getActiveReport();
    activeReportData.value = r?.data?.data ?? r?.data ?? [];
  } catch {
    toast("Tải báo cáo thất bại.", "error");
  } finally {
    activeReportLoading.value = false;
  }
}

// Summary report
const summaryReportLoading = ref(false);
const summaryReportData = ref(null);
const summaryPeriod = ref("month");
async function loadSummaryReport() {
  summaryReportLoading.value = true;
  try {
    const r = await promotionsApi.getSummaryReport(summaryPeriod.value);
    summaryReportData.value = r?.data?.data ?? r?.data ?? null;
  } catch {
    toast("Tải báo cáo tổng hợp thất bại.", "error");
  } finally {
    summaryReportLoading.value = false;
  }
}

// Preview
const previewPromo = ref(null);
const previewProductId = ref(null);
const previewCustomerId = ref(null);
const previewLoading = ref(false);
const previewPrices = ref([]);
const previewLoaded = ref(false);
function openPreview(row) {
  previewPromo.value = row;
  previewPrices.value = [];
  previewLoaded.value = false;
  previewProductId.value = null;
  previewCustomerId.value = null;
}
async function loadPreviewPrices() {
  if (!previewProductId.value) return toast("Nhập Product ID.", "warning");
  previewLoading.value = true;
  previewLoaded.value = false;
  try {
    const r = previewCustomerId.value
      ? await pricesApi.listByProductForCustomer(
          previewProductId.value,
          previewCustomerId.value,
        )
      : await pricesApi.listByProduct(previewProductId.value);
    previewPrices.value = r?.data?.data ?? r?.data ?? [];
    previewLoaded.value = true;
  } catch {
    previewPrices.value = [];
    toast("Tải giá thất bại.", "error");
  } finally {
    previewLoading.value = false;
  }
}

// Dialog
const dlg = reactive({
  open: false,
  mode: "create",
  loading: false,
  alert: "",
  id: null,
  form: {
    code: "",
    name: "",
    description: "",
    discountType: "PERCENT",
    discountValue: 0,
    startDate: "",
    endDate: "",
    scope: "ALL",
    productIdsText: "",
    variantIdsText: "",
    priority: 0,
    stackable: false,
    isActive: true,
    buyQty: null,
    getQty: null,
    usageLimit: null,
    customerTypes: [],
    vipTiers: [],
  },
});

function toLocalDT(iso) {
  if (!iso) return "";
  try {
    const s = String(iso);
    if (s.length >= 16 && !s.endsWith("Z") && !s.includes("+")) {
      return s.slice(0, 16);
    }
    const d = new Date(iso);
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  } catch {
    return "";
  }
}
function fromLocalDT(s) {
  if (!s) return "";
  return s.length === 16 ? s + ":00" : s;
}
function parseIds(t) {
  return String(t || "")
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean)
    .map(Number)
    .filter((n) => Number.isFinite(n));
}

function openCreate() {
  dlg.open = true;
  dlg.mode = "create";
  dlg.alert = "";
  dlg.id = null;
  dlg.form = {
    code: "",
    name: "",
    description: "",
    discountType: "PERCENT",
    discountValue: 0,
    startDate: "",
    endDate: "",
    scope: "ALL",
    productIdsText: "",
    variantIdsText: "",
    priority: 0,
    stackable: false,
    isActive: true,
    buyQty: null,
    getQty: null,
    usageLimit: null,
    customerTypes: [],
    vipTiers: [],
  };
}
function openEdit(row) {
  dlg.open = true;
  dlg.mode = "edit";
  dlg.alert = "";
  dlg.id = row.id;
  dlg.form = {
    code: row.code,
    name: row.name,
    description: row.description,
    discountType: row.discountType || "PERCENT",
    discountValue: Number(row.discountValue || 0),
    startDate: toLocalDT(row.startDate),
    endDate: toLocalDT(row.endDate),
    scope: row.scope || "ALL",
    productIdsText: (row.productIds || []).join(","),
    variantIdsText: (row.variantIds || []).join(","),
    priority: Number(row.priority || 0),
    stackable: !!row.stackable,
    isActive: !!row.isActive,
    buyQty: row.buyQty || null,
    getQty: row.getQty || null,
    usageLimit: row.usageLimit || null,
    customerTypes: [...(row.customerTypes || [])],
    vipTiers: [...(row.vipTiers || [])],
  };
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.code || !dlg.form.name) {
    dlg.alert = "Code và tên là bắt buộc.";
    return;
  }
  const payload = {
    code: dlg.form.code,
    name: dlg.form.name,
    description: dlg.form.description,
    discountType: dlg.form.discountType,
    discountValue: dlg.form.discountValue,
    startDate: fromLocalDT(dlg.form.startDate),
    endDate: fromLocalDT(dlg.form.endDate),
    scope: dlg.form.scope,
    productIds: parseIds(dlg.form.productIdsText),
    variantIds: parseIds(dlg.form.variantIdsText),
    priority: dlg.form.priority,
    stackable: dlg.form.stackable,
    isActive: dlg.form.isActive,
    ...(dlg.form.buyQty
      ? { buyQty: dlg.form.buyQty, getQty: dlg.form.getQty || 1 }
      : {}),
    ...(dlg.form.usageLimit ? { usageLimit: dlg.form.usageLimit } : {}),
    ...(dlg.form.customerTypes?.length
      ? { customerTypes: dlg.form.customerTypes }
      : {}),
    ...(dlg.form.vipTiers?.length ? { vipTiers: dlg.form.vipTiers } : {}),
  };
  dlg.loading = true;
  try {
    if (dlg.mode === "create") {
      await promotionsApi.create(payload);
      toast("Đã tạo khuyến mãi.", "success");
    } else {
      await promotionsApi.update(dlg.id, payload);
      toast("Đã cập nhật.", "success");
    }
    dlg.open = false;
    await load();
  } catch (e) {
    dlg.alert = e?.response?.data?.message || e?.message || "Lưu thất bại";
  } finally {
    dlg.loading = false;
  }
}

async function remove(row) {
  const ok = await confirmModal(
    `Xóa KM #${row.id} (${row.code})?`,
    "Xác nhận",
    "Xóa",
    true,
  );
  if (!ok) return;
  try {
    await promotionsApi.remove(row.id);
    toast("Đã xóa.", "success");
    await load();
  } catch {
    toast("Xóa thất bại.", "error");
  }
}

// Expiring
const expiringLoading = ref(false);
const expiringRows = ref(null);
async function loadExpiring() {
  expiringLoading.value = true;
  try {
    const r = await promotionsApi.getExpiring(3);
    expiringRows.value = r?.data?.data ?? r?.data ?? [];
  } catch {
    toast("Tải sắp hết hạn thất bại.", "error");
  } finally {
    expiringLoading.value = false;
  }
}

// Report
const reportLoading = ref(false);
const reportData = ref(null);
const reportPeriod = ref("month");
async function loadReport() {
  reportLoading.value = true;
  try {
    const r = await promotionsApi.getReport(reportPeriod.value);
    reportData.value = r?.data?.data ?? r?.data ?? {};
  } catch {
    toast("Tải báo cáo thất bại.", "error");
  } finally {
    reportLoading.value = false;
  }
}

async function loadCombinedReport() {
  reportLoading.value = true;
  summaryReportLoading.value = true;
  try {
    const [r1, r2] = await Promise.all([
      promotionsApi.getReport(reportPeriod.value),
      promotionsApi.getSummaryReport(reportPeriod.value),
    ]);
    reportData.value = r1?.data?.data ?? r1?.data ?? {};
    summaryReportData.value = r2?.data?.data ?? r2?.data ?? null;
  } catch {
    toast("Tải báo cáo thất bại.", "error");
  } finally {
    reportLoading.value = false;
    summaryReportLoading.value = false;
  }
}

function fmtDate(iso) {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleDateString("vi-VN");
  } catch {
    return iso;
  }
}
function fmtMoney(val) {
  if (val == null) return "—";
  return Number(val).toLocaleString("vi-VN") + " ₫";
}

// Redemption detail modal
const redemptionModal = reactive({
  open: false,
  loading: false,
  promotionCode: "",
  promotionId: null,
  rows: [],
});

async function openRedemptionDetail(promotionId, code) {
  redemptionModal.open = true;
  redemptionModal.loading = true;
  redemptionModal.promotionCode = code;
  redemptionModal.promotionId = promotionId;
  redemptionModal.rows = [];
  try {
    const r = await promotionsApi.getRedemptionDetails(promotionId);
    redemptionModal.rows = r?.data?.data ?? r?.data ?? [];
  } catch {
    toast("Tải chi tiết lượt dùng thất bại.", "error");
  } finally {
    redemptionModal.loading = false;
  }
}

onMounted(load);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.pm2-page {
  --pm2-bg:            #f6f7f9;
  --pm2-card:          #ffffff;
  --pm2-border:        #e4e7ec;
  --pm2-border-light:  #f0f2f5;
  --pm2-text:          #0f1117;
  --pm2-muted:         #6b7280;
  --pm2-subtle:        #9ca3af;
  --pm2-blue:          #2563eb;
  --pm2-blue-bg:       #eff6ff;
  --pm2-blue-border:   #bfdbfe;
  --pm2-green:         #16a34a;
  --pm2-green-bg:      #f0fdf4;
  --pm2-green-border:  #bbf7d0;
  --pm2-red:           #dc2626;
  --pm2-red-bg:        #fff1f2;
  --pm2-red-border:    #fecdd3;
  --pm2-orange:        #d97706;
  --pm2-orange-bg:     #fffbeb;
  --pm2-orange-border: #fde68a;
  --pm2-purple:        #7c3aed;
  --pm2-purple-bg:     #f5f3ff;
  --pm2-purple-border: #ddd6fe;
  --pm2-radius:        12px;
  --pm2-radius-sm:     8px;
  --pm2-shadow-sm:     0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --pm2-shadow-md:     0 4px 16px rgba(0,0,0,0.08), 0 2px 6px rgba(0,0,0,0.04);
  --pm2-shadow-lg:     0 24px 64px rgba(0,0,0,0.14), 0 8px 24px rgba(0,0,0,0.06);

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--pm2-bg);
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: var(--pm2-text);
  box-sizing: border-box;
}

/* ── Header ──────────────────────────────── */
.pm2-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}
.pm2-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--pm2-muted);
  margin-bottom: 6px;
}
.pm2-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
  line-height: 1.2;
}
.pm2-title__accent { color: var(--pm2-blue); }
.pm2-subtitle {
  font-size: 13.5px;
  color: var(--pm2-muted);
  margin: 0;
}
.pm2-header__right { flex-shrink: 0; padding-top: 4px; }

.pm2-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

/* ── Card ────────────────────────────────── */
.pm2-card {
  background: var(--pm2-card);
  border: 1px solid var(--pm2-border);
  border-radius: var(--pm2-radius);
  box-shadow: var(--pm2-shadow-sm);
  overflow: hidden;
}

/* ── Validate bar ────────────────────────── */
.pm2-validate-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  flex-wrap: wrap;
}
.pm2-validate-bar__label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--pm2-text);
  white-space: nowrap;
}
.pm2-validate-bar__inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}

.pm2-vfield {
  display: flex;
  align-items: center;
  border: 1.5px solid var(--pm2-border);
  border-radius: var(--pm2-radius-sm);
  overflow: hidden;
  background: var(--pm2-card);
  transition: border-color 0.15s;
}
.pm2-vfield:focus-within { border-color: var(--pm2-blue); }
.pm2-vfield__pre {
  padding: 0 10px;
  font-size: 10.5px;
  font-weight: 700;
  color: var(--pm2-subtle);
  background: var(--pm2-border-light);
  border-right: 1px solid var(--pm2-border);
  height: 100%;
  display: flex;
  align-items: center;
  letter-spacing: 0.05em;
  white-space: nowrap;
}
.pm2-vfield__input {
  padding: 8px 12px;
  font-size: 13.5px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: none;
  outline: none;
  background: transparent;
  color: var(--pm2-text);
  min-width: 0;
}
.pm2-vfield__input--num { font-family: "JetBrains Mono", monospace; width: 120px; }
.pm2-vfield__input::placeholder { color: var(--pm2-subtle); }

.pm2-validate-result {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border-radius: var(--pm2-radius-sm);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid transparent;
}
.pm2-validate-result--ok   { background: var(--pm2-green-bg); color: var(--pm2-green); border-color: var(--pm2-green-border); }
.pm2-validate-result--fail { background: var(--pm2-red-bg); color: var(--pm2-red); border-color: var(--pm2-red-border); }

/* ── Panels ──────────────────────────────── */
.pm2-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 20px;
  border-bottom: 1px solid var(--pm2-border-light);
  background: #fafbfc;
  gap: 12px;
}
.pm2-panel__head-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}
.pm2-panel__icon {
  width: 28px; height: 28px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.pm2-panel__icon--green  { background: var(--pm2-green-bg);  color: var(--pm2-green); }
.pm2-panel__icon--orange { background: var(--pm2-orange-bg); color: var(--pm2-orange); }
.pm2-panel__icon--blue   { background: var(--pm2-blue-bg);   color: var(--pm2-blue); }
.pm2-panel__icon--red    { background: var(--pm2-red-bg);    color: var(--pm2-red); }
.pm2-panel__title {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--pm2-text);
}
.pm2-panel__body { padding: 16px 20px; }

.pm2-preview-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* Period tabs */
.pm2-period-tabs {
  display: flex;
  gap: 2px;
  background: var(--pm2-border-light);
  border-radius: 6px;
  padding: 3px;
  margin-left: 4px;
}
.pm2-period-btn {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 700;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  background: transparent;
  color: var(--pm2-muted);
  transition: all 0.15s;
  font-family: "Plus Jakarta Sans", sans-serif;
}
.pm2-period-btn--active {
  background: var(--pm2-card);
  color: var(--pm2-text);
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

/* Report stats */
.pm2-report-section-label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--pm2-subtle);
  margin-bottom: 10px;
}
.pm2-report-stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.pm2-rstat {
  flex: 1;
  min-width: 100px;
  background: var(--pm2-border-light);
  border: 1px solid var(--pm2-border);
  border-radius: var(--pm2-radius-sm);
  padding: 14px 16px;
  transition: transform 0.15s, box-shadow 0.15s;
}
.pm2-rstat:hover { transform: translateY(-1px); box-shadow: var(--pm2-shadow-sm); }
.pm2-rstat--green  { border-top: 2px solid var(--pm2-green); }
.pm2-rstat--orange { border-top: 2px solid var(--pm2-orange); }
.pm2-rstat--blue   { border-top: 2px solid var(--pm2-blue); }
.pm2-rstat--red    { border-top: 2px solid var(--pm2-red); }
.pm2-rstat--clickable { cursor: pointer; }
.pm2-rstat__num {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.1;
  color: var(--pm2-text);
}
.pm2-rstat--green .pm2-rstat__num  { color: var(--pm2-green); }
.pm2-rstat--orange .pm2-rstat__num { color: var(--pm2-orange); }
.pm2-rstat--blue .pm2-rstat__num   { color: var(--pm2-blue); }
.pm2-rstat--red .pm2-rstat__num    { color: var(--pm2-red); }
.pm2-rstat__num--underline { text-decoration: underline dotted; }
.pm2-rstat__label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--pm2-subtle);
  margin-top: 4px;
}

/* Ratios */
.pm2-ratios { display: flex; flex-direction: column; gap: 12px; }
.pm2-ratio__label {
  display: flex;
  justify-content: space-between;
  font-size: 12.5px;
  font-weight: 600;
  margin-bottom: 5px;
}
.pm2-ratio__val { font-weight: 800; }
.pm2-ratio__bar {
  height: 6px;
  background: var(--pm2-border);
  border-radius: 999px;
  overflow: hidden;
}
.pm2-ratio__fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.5s ease;
}
.pm2-ratio__fill--green  { background: var(--pm2-green); }
.pm2-ratio__fill--orange { background: var(--pm2-orange); }
.pm2-ratio__fill--blue   { background: var(--pm2-blue); }

/* ── Toolbar ─────────────────────────────── */
.pm2-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 20px;
  border-bottom: 1px solid var(--pm2-border-light);
  background: #fafbfc;
  gap: 12px;
  flex-wrap: wrap;
}
.pm2-toolbar__left  { display: flex; align-items: center; gap: 10px; }
.pm2-toolbar__right { display: flex; align-items: center; gap: 10px; }
.pm2-count { display: flex; align-items: baseline; gap: 4px; }
.pm2-count__num   { font-size: 18px; font-weight: 800; }
.pm2-count__label { font-size: 12px; color: var(--pm2-subtle); font-weight: 600; }

/* Switch */
.pm2-switch {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.pm2-switch input { display: none; }
.pm2-switch__track {
  width: 36px; height: 20px;
  background: var(--pm2-border);
  border-radius: 999px;
  position: relative;
  transition: background 0.2s;
  flex-shrink: 0;
}
.pm2-switch__track::after {
  content: '';
  position: absolute;
  top: 3px; left: 3px;
  width: 14px; height: 14px;
  background: #fff;
  border-radius: 50%;
  transition: transform 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.15);
}
.pm2-switch input:checked ~ .pm2-switch__track { background: var(--pm2-blue); }
.pm2-switch input:checked ~ .pm2-switch__track::after { transform: translateX(16px); }
.pm2-switch__label { font-size: 12.5px; font-weight: 600; color: var(--pm2-text); }

/* ── Table ───────────────────────────────── */
.pm2-table-wrap {
  overflow-x: auto;
}
.pm2-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.pm2-table thead tr { border-bottom: 2px solid var(--pm2-border-light); }
.pm2-table th {
  padding: 11px 14px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--pm2-subtle);
  background: #fafbfc;
  white-space: nowrap;
}
.pm2-table td {
  padding: 12px 14px;
  vertical-align: middle;
  border-bottom: 1px solid var(--pm2-border-light);
}
.pm2-row { transition: background 0.12s; }
.pm2-row:hover { background: #fafbfc; }
.pm2-row:last-child td { border-bottom: none; }

/* Cell types */
.pm2-mono-id {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  font-weight: 600;
  color: var(--pm2-blue);
  background: var(--pm2-blue-bg);
  padding: 2px 7px;
  border-radius: 5px;
}
.pm2-code {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  font-weight: 600;
  color: var(--pm2-text);
  background: var(--pm2-border-light);
  border: 1px solid var(--pm2-border);
  padding: 2px 8px;
  border-radius: 5px;
}
.pm2-name-cell {
  font-weight: 600;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pm2-disc-type {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: var(--pm2-subtle);
  margin-top: 2px;
  text-transform: uppercase;
}
.pm2-dates {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  white-space: nowrap;
  color: var(--pm2-muted);
}
.pm2-dates__sep { color: var(--pm2-subtle); }

.pm2-status {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.05em;
  border: 1px solid transparent;
  white-space: nowrap;
}
.pm2-status--on  { background: var(--pm2-green-bg); color: var(--pm2-green); border-color: var(--pm2-green-border); }
.pm2-status--off { background: var(--pm2-border-light); color: var(--pm2-subtle); border-color: var(--pm2-border); }

/* Customer cell */
.pm2-customer-cell { display: flex; flex-direction: column; gap: 2px; }
.pm2-customer-cell__name { font-weight: 600; font-size: 13px; }
.pm2-customer-cell__id { font-size: 10.5px; color: var(--pm2-subtle); }

/* Time cell */
.pm2-time-cell { display: flex; flex-direction: column; align-items: center; gap: 1px; }
.pm2-time-cell__time { font-size: 12px; font-weight: 600; }
.pm2-time-cell__date { font-size: 10.5px; }

/* Action buttons */
.pm2-actions { display: flex; gap: 5px; flex-wrap: wrap; }
.pm2-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  font-size: 11.5px;
  font-weight: 700;
  border-radius: 6px;
  border: 1.5px solid transparent;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
  white-space: nowrap;
}
.pm2-action-btn--edit {
  background: var(--pm2-blue-bg); color: var(--pm2-blue); border-color: var(--pm2-blue-border);
}
.pm2-action-btn--edit:hover {
  background: var(--pm2-blue); color: #fff; border-color: var(--pm2-blue);
  transform: translateY(-1px); box-shadow: 0 3px 10px rgba(37,99,235,0.2);
}
.pm2-action-btn--blue {
  background: var(--pm2-blue-bg); color: var(--pm2-blue); border-color: var(--pm2-blue-border);
}
.pm2-action-btn--blue:hover {
  background: var(--pm2-blue); color: #fff; border-color: var(--pm2-blue);
  transform: translateY(-1px);
}
.pm2-action-btn--purple {
  background: var(--pm2-purple-bg); color: var(--pm2-purple); border-color: var(--pm2-purple-border);
}
.pm2-action-btn--purple:hover {
  background: var(--pm2-purple); color: #fff; border-color: var(--pm2-purple);
  transform: translateY(-1px); box-shadow: 0 3px 10px rgba(124,58,237,0.2);
}
.pm2-action-btn--delete {
  background: var(--pm2-red-bg); color: var(--pm2-red); border-color: var(--pm2-red-border);
}
.pm2-action-btn--delete:hover {
  background: var(--pm2-red); color: #fff; border-color: var(--pm2-red);
  transform: translateY(-1px); box-shadow: 0 3px 10px rgba(220,38,38,0.2);
}

.pm2-loading-row, .pm2-empty-row, .pm2-empty {
  text-align: center;
  padding: 48px 20px !important;
  color: var(--pm2-muted);
  font-size: 13.5px;
}
.pm2-loading-row { display: flex; align-items: center; justify-content: center; gap: 8px; }
.pm2-loading-center {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px;
  color: var(--pm2-muted);
  font-size: 13.5px;
}

/* Tags */
.pm2-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  border: 1px solid transparent;
  white-space: nowrap;
}
.pm2-tag--green  { background: var(--pm2-green-bg);  color: var(--pm2-green);  border-color: var(--pm2-green-border); }
.pm2-tag--red    { background: var(--pm2-red-bg);    color: var(--pm2-red);    border-color: var(--pm2-red-border); }
.pm2-tag--orange { background: var(--pm2-orange-bg); color: var(--pm2-orange); border-color: var(--pm2-orange-border); }
.pm2-tag--blue   { background: var(--pm2-blue-bg);   color: var(--pm2-blue);   border-color: var(--pm2-blue-border); }
.pm2-tag--purple { background: var(--pm2-purple-bg); color: var(--pm2-purple); border-color: var(--pm2-purple-border); }

/* Ok msg */
.pm2-ok-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--pm2-green-bg);
  color: var(--pm2-green);
  border: 1px solid var(--pm2-green-border);
  border-radius: var(--pm2-radius-sm);
  font-size: 13px;
  font-weight: 600;
}

/* Buttons */
.pm2-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  font-size: 13px;
  font-weight: 700;
  border-radius: var(--pm2-radius-sm);
  border: none;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
  white-space: nowrap;
}
.pm2-btn--primary {
  background: var(--pm2-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,0.22);
}
.pm2-btn--primary:hover { background: #1d4ed8; transform: translateY(-1px); }
.pm2-btn--outline {
  background: var(--pm2-card);
  color: var(--pm2-text);
  border: 1.5px solid var(--pm2-border);
}
.pm2-btn--outline:hover { border-color: var(--pm2-subtle); background: #f9fafb; }
.pm2-btn--ghost {
  background: transparent;
  color: var(--pm2-muted);
  border: 1.5px solid var(--pm2-border);
}
.pm2-btn--ghost:hover { background: var(--pm2-border-light); color: var(--pm2-text); }
.pm2-btn--loading, .pm2-btn:disabled { opacity: 0.65; pointer-events: none; transform: none !important; }
.pm2-btn--confirm-blue {
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #fff;
  box-shadow: 0 4px 14px rgba(37,99,235,0.35);
  padding: 11px 22px;
}
.pm2-btn--confirm-blue:hover { background: linear-gradient(135deg, #1e3a8a, #1d4ed8); transform: translateY(-1px); }
.pm2-btn--confirm-orange {
  background: linear-gradient(135deg, #b45309, #d97706);
  color: #fff;
  box-shadow: 0 4px 14px rgba(217,119,6,0.35);
  padding: 11px 22px;
}
.pm2-btn--confirm-orange:hover { background: linear-gradient(135deg, #92400e, #b45309); transform: translateY(-1px); }

/* Icon btn */
.pm2-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border: none;
  background: var(--pm2-border-light);
  border-radius: 6px;
  cursor: pointer;
  color: var(--pm2-muted);
  flex-shrink: 0;
  transition: all 0.15s;
}
.pm2-icon-btn:hover { background: var(--pm2-red-bg); color: var(--pm2-red); }

/* Inputs */
.pm2-input {
  padding: 9px 12px;
  font-size: 13.5px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: 1.5px solid var(--pm2-border);
  border-radius: var(--pm2-radius-sm);
  background: var(--pm2-card);
  color: var(--pm2-text);
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
  width: 100%;
  box-sizing: border-box;
}
.pm2-input:focus { border-color: var(--pm2-blue); box-shadow: 0 0 0 3px rgba(37,99,235,0.08); }
.pm2-input::placeholder { color: var(--pm2-subtle); }
.pm2-input:disabled { background: var(--pm2-border-light); color: var(--pm2-muted); cursor: not-allowed; }
.pm2-select { cursor: pointer; }
.pm2-textarea { resize: vertical; line-height: 1.5; }

/* ════════════════════════════════════════
   DIALOG
════════════════════════════════════════ */
.pm2-overlay {
  position: fixed;
  inset: 0;
  background: rgba(10,12,20,0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(2px);
}
.pm2-dialog {
  background: #fff;
  border-radius: 20px;
  width: 100%;
  max-width: 520px;
  box-shadow: var(--pm2-shadow-lg);
  overflow: hidden;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}
.pm2-dialog--wide { max-width: 720px; }
.pm2-dialog--xl   { max-width: 1000px; }

/* Band */
.pm2-dialog__band {
  position: relative;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}
.pm2-dialog__band--blue   { background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 60%, #60a5fa 100%); }
.pm2-dialog__band--orange { background: linear-gradient(135deg, #b45309 0%, #d97706 60%, #fbbf24 100%); }
.pm2-dialog__band--purple { background: linear-gradient(135deg, #5b21b6 0%, #7c3aed 60%, #a78bfa 100%); }

.pm2-dialog__band-circles { position: absolute; inset: 0; pointer-events: none; }
.pm2-band-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
}
.pm2-band-circle--1 { width:120px; height:120px; top:-30px; right:-20px; }
.pm2-band-circle--2 { width:70px;  height:70px;  bottom:-15px; left:20px; }
.pm2-band-circle--3 { width:45px;  height:45px;  top:10px; left:50%; margin-left:-22px; background: rgba(255,255,255,0.07); }

.pm2-dialog__icon-wrap { position: relative; z-index: 1; }
.pm2-dialog__icon-ring {
  width: 56px; height: 56px;
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  backdrop-filter: blur(4px);
  box-shadow: 0 8px 32px rgba(0,0,0,0.15), inset 0 1px 0 rgba(255,255,255,0.3);
}

/* Dialog body */
.pm2-dialog__body {
  padding: 24px 28px 26px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
  flex: 1;
}
.pm2-dialog__head-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.pm2-dialog__badge {
  display: inline-flex;
  align-self: flex-start;
  font-size: 10.5px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  padding: 4px 10px;
  border-radius: 20px;
}
.pm2-dialog__badge--blue   { background: var(--pm2-blue-bg);   color: var(--pm2-blue);   border: 1px solid var(--pm2-blue-border); }
.pm2-dialog__badge--orange { background: var(--pm2-orange-bg); color: var(--pm2-orange); border: 1px solid var(--pm2-orange-border); }
.pm2-dialog__badge--purple { background: var(--pm2-purple-bg); color: var(--pm2-purple); border: 1px solid var(--pm2-purple-border); }

.pm2-dialog__title {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pm2-dialog__notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 12.5px;
  font-weight: 600;
  line-height: 1.5;
  border: 1px solid transparent;
}
.pm2-dialog__notice--red { background: var(--pm2-red-bg); color: var(--pm2-red); border-color: var(--pm2-red-border); }

.pm2-dialog__actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 4px;
  border-top: 1px solid var(--pm2-border-light);
  margin-top: 4px;
}

/* Dialog sections */
.pm2-dlg-section {
  font-size: 10.5px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--pm2-muted);
  padding-bottom: 8px;
  border-bottom: 1px solid var(--pm2-border-light);
  margin-top: 4px;
}
.pm2-dlg-section__hint {
  font-size: 10px;
  font-weight: 500;
  text-transform: none;
  letter-spacing: 0;
  color: var(--pm2-subtle);
  margin-left: 4px;
}

/* Fields */
.pm2-field { display: flex; flex-direction: column; gap: 5px; }
.pm2-field__label {
  font-size: 11px;
  font-weight: 700;
  color: var(--pm2-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.pm2-required { color: var(--pm2-red); margin-left: 2px; }
.pm2-field--toggle { flex-direction: row; align-items: center; gap: 10px; }

/* Checkgroup */
.pm2-checkgroup {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.pm2-check {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  padding: 5px 10px;
  border: 1.5px solid var(--pm2-border);
  border-radius: 6px;
  transition: all 0.15s;
}
.pm2-check:hover { border-color: var(--pm2-blue); }
.pm2-check input { accent-color: var(--pm2-blue); }

/* Toggle */
.pm2-toggle { display: inline-flex; align-items: center; cursor: pointer; }
.pm2-toggle input { display: none; }
.pm2-toggle__track {
  width: 36px; height: 20px;
  background: var(--pm2-border);
  border-radius: 999px;
  position: relative;
  transition: background 0.2s;
}
.pm2-toggle__track::after {
  content: '';
  position: absolute;
  top: 3px; left: 3px;
  width: 14px; height: 14px;
  background: #fff;
  border-radius: 50%;
  transition: transform 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.15);
}
.pm2-toggle input:checked ~ .pm2-toggle__track { background: var(--pm2-blue); }
.pm2-toggle input:checked ~ .pm2-toggle__track::after { transform: translateX(16px); }

/* Grid rows */
.pm2-row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.pm2-row3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px; }

/* Utility */
.pm2-mono  { font-family: "JetBrains Mono", monospace; }
.pm2-bold  { font-weight: 700; }
.pm2-small { font-size: 11.5px; }
.pm2-text--red    { color: var(--pm2-red); }
.pm2-text--green  { color: var(--pm2-green); }
.pm2-text--blue   { color: var(--pm2-blue); }
.pm2-text--orange { color: var(--pm2-orange); }
.pm2-text--muted  { color: var(--pm2-muted); }
.pm2-mb { margin-bottom: 16px; }
.pm2-mt { margin-top: 16px; }

/* Spinner */
.pm2-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.12);
  border-top-color: var(--pm2-muted);
  border-radius: 50%;
  animation: pm2-spin 0.6s linear infinite;
  flex-shrink: 0;
}
.pm2-spinner--white { border-color: rgba(255,255,255,0.25); border-top-color: #fff; }
@keyframes pm2-spin { to { transform: rotate(360deg); } }

/* Transitions */
.pm2-slide-enter-active { transition: all 0.25s cubic-bezier(0.34, 1.2, 0.64, 1); }
.pm2-slide-leave-active { transition: all 0.18s ease; }
.pm2-slide-enter-from  { opacity: 0; transform: translateY(-10px); }
.pm2-slide-leave-to    { opacity: 0; transform: translateY(-6px); }

.pm2-fade-enter-active { transition: all 0.2s ease; }
.pm2-fade-leave-active { transition: all 0.15s ease; }
.pm2-fade-enter-from, .pm2-fade-leave-to { opacity: 0; transform: translateY(-4px); }
</style>