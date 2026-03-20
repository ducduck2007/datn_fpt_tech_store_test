<template>
  <div class="crm2-page">
    <div class="crm2-inner">

      <!-- ── Header ── -->
      <header class="crm2-header">
        <div class="crm2-header__left">
          <div class="crm2-eyebrow">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            Quản lý Quan hệ Khách hàng
          </div>
          <h1 class="crm2-title">Quản lý <span class="crm2-title__accent">Khách hàng</span></h1>
          <p class="crm2-subtitle">{{ rows.length }} tài khoản đã đăng ký</p>
        </div>
        <div class="crm2-header__actions">
          <button class="crm2-btn crm2-btn--outline" :class="{'crm2-btn--loading': loading}" @click="reloadCurrentTab" :disabled="loading">
            <svg v-if="!loading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
              <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
              <path d="M8 16H3v5"/>
            </svg>
            <span v-if="!loading">Tải lại</span>
            <span v-else class="crm2-spinner"></span>
          </button>
          <button v-if="mainTab === 'customers'" class="crm2-btn crm2-btn--primary" @click="openCreate">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Thêm khách hàng
          </button>
          <button v-if="mainTab === 'customers'" class="crm2-btn crm2-btn--outline" @click="openSummaryDialog">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
            Tổng điểm
          </button>
        </div>
      </header>

      <!-- ── Tab bar ── -->
      <div class="crm2-tabbar">
        <button class="crm2-tab" :class="{'crm2-tab--active': mainTab === 'customers'}" @click="mainTab = 'customers'">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
          Khách hàng &amp; Thành viên
        </button>
        <button class="crm2-tab" :class="{'crm2-tab--active': mainTab === 'birthdays'}" @click="mainTab = 'birthdays'; loadBirthdayData()">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><rect x="8" y="2" width="8" height="5" rx="1"/><path d="M12 7v5"/></svg>
          Sinh nhật
        </button>
        <button class="crm2-tab" :class="{'crm2-tab--active': mainTab === 'inactive'}" @click="mainTab = 'inactive'; loadInactiveAll()">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          Không hoạt động
        </button>
        <button class="crm2-tab" :class="{'crm2-tab--active': mainTab === 'zeroorder'}" @click="mainTab = 'zeroorder'; loadZeroOrderData(); loadZeroOrderStats()">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
          Chưa mua hàng
        </button>
      </div>

      <!-- ══ TAB 1: CUSTOMERS ══ -->
      <template v-if="mainTab === 'customers'">

        <!-- Stat cards -->
        <div class="crm2-stats4">
          <div class="crm2-stat crm2-stat--blue">
            <div class="crm2-stat__num">{{ rows.length }}</div>
            <div class="crm2-stat__label">Tổng khách hàng</div>
          </div>
          <div class="crm2-stat crm2-stat--orange">
            <div class="crm2-stat__num">{{ vipCount }}</div>
            <div class="crm2-stat__label">Thành viên VIP</div>
          </div>
          <div class="crm2-stat crm2-stat--green">
            <div class="crm2-stat__num">{{ totalPoints.toLocaleString() }}</div>
            <div class="crm2-stat__label">Tổng điểm</div>
          </div>
          <div class="crm2-stat crm2-stat--dim">
            <div class="crm2-stat__num">{{ avgPoints.toLocaleString() }}</div>
            <div class="crm2-stat__label">Điểm TB</div>
          </div>
          <div class="crm2-stat crm2-stat--purple" v-if="hasActiveFilters">
            <div class="crm2-stat__num">{{ filtered.length }}</div>
            <div class="crm2-stat__label">Đã lọc</div>
          </div>
        </div>

        <!-- Filter panel -->
        <div class="crm2-filter-panel">
          <div class="crm2-filter-panel__head">
            <div class="crm2-filter-panel__title">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
              Bộ lọc
              <span v-if="hasActiveFilters" class="crm2-filter-badge">{{ [typeFilter,vipTierFilter,activityFilter,isPointsFilterApplied].filter(Boolean).length }} đang áp dụng</span>
            </div>
            <button class="crm2-filter-clear" @click="clearAllFilters" :disabled="!hasActiveFilters">Xóa tất cả</button>
          </div>
          <div class="crm2-filter-fields">
            <div class="crm2-filter-field">
              <span class="crm2-filter-field__label">Tìm kiếm</span>
              <input v-model="q" class="crm2-input" placeholder="Tên / Email / SĐT…" />
            </div>
            <div class="crm2-filter-field">
              <span class="crm2-filter-field__label">Loại</span>
              <select v-model="typeFilter" class="crm2-input crm2-select" @change="handleFilterChange">
                <option value="">Tất cả</option>
                <option value="REGULAR">Thường</option>
                <option value="VIP">VIP</option>
              </select>
            </div>
            <div class="crm2-filter-field">
              <span class="crm2-filter-field__label">Cấp VIP</span>
              <select v-model="vipTierFilter" class="crm2-input crm2-select" @change="handleFilterChange">
                <option value="">Tất cả cấp</option>
                <option value="BRONZE">Đồng</option>
                <option value="SILVER">Bạc</option>
                <option value="GOLD">Vàng</option>
                <option value="PLATINUM">Bạch kim</option>
                <option value="DIAMOND">Kim cương</option>
              </select>
            </div>
            <div class="crm2-filter-field">
              <span class="crm2-filter-field__label">Hoạt động</span>
              <select v-model="activityFilter" class="crm2-input crm2-select" @change="handleFilterChange">
                <option value="">Tất cả</option>
                <option value="ACTIVE_30">Hoạt động (30 ngày)</option>
              </select>
            </div>
          </div>
          <div class="crm2-filter-divider"></div>
          <div class="crm2-filter-points">
            <div class="crm2-filter-field">
              <span class="crm2-filter-field__label">Điểm tối thiểu</span>
              <input v-model.number="pointsMin" type="number" class="crm2-input" placeholder="0" min="0" />
            </div>
            <div class="crm2-filter-field">
              <span class="crm2-filter-field__label">Điểm tối đa</span>
              <input v-model.number="pointsMax" type="number" class="crm2-input" placeholder="999999" min="0" />
            </div>
            <div class="crm2-filter-field crm2-filter-field--actions">
              <button class="crm2-btn crm2-btn--primary" @click="applyPointsFilter" :disabled="!canApplyPointsFilter">Áp dụng</button>
              <button class="crm2-btn crm2-btn--ghost" @click="clearPointsFilter" :disabled="!isPointsFilterApplied">Reset</button>
            </div>
          </div>
          <div class="crm2-active-filters" v-if="hasActiveFilters">
            <span class="crm2-af-label">Đang hoạt động:</span>
            <span class="crm2-filter-chip" v-if="typeFilter">Loại: {{ typeFilter }}<button @click="typeFilter = ''; handleFilterChange()" class="crm2-chip-remove">×</button></span>
            <span class="crm2-filter-chip" v-if="vipTierFilter">Cấp: {{ vipTierFilter }}<button @click="vipTierFilter = ''; handleFilterChange()" class="crm2-chip-remove">×</button></span>
            <span class="crm2-filter-chip" v-if="activityFilter === 'ACTIVE_30'">HD 30 ngày<button @click="activityFilter = ''; handleFilterChange()" class="crm2-chip-remove">×</button></span>
            <span class="crm2-filter-chip" v-if="isPointsFilterApplied">Điểm: {{ appliedPointsMin }}–{{ appliedPointsMax === 999999 ? '∞' : appliedPointsMax }}<button @click="clearPointsFilter" class="crm2-chip-remove">×</button></span>
          </div>
        </div>

        <!-- Table -->
        <div class="crm2-card">
          <div class="crm2-toolbar">
            <div class="crm2-toolbar__left">
              <span class="crm2-count"><span class="crm2-count__num">{{ filtered.length }}</span><span class="crm2-count__label">/ {{ rows.length }} khách</span></span>
            </div>
          </div>
          <div class="crm2-table-wrap" :class="{'crm2-table-wrap--loading': loading}">
            <div v-if="loading" class="crm2-loader-overlay"><div class="crm2-loader-ring"></div></div>
            <table class="crm2-table">
              <thead>
                <tr>
                  <th style="width:70px">STT</th>
                  <th style="min-width:180px">Khách hàng</th>
                  <th style="min-width:160px">Liên hệ</th>
                  <th style="width:100px">Loại</th>
                  <th style="width:110px">Cấp</th>
                  <th style="width:130px">Điểm</th>
                  <th style="width:110px">Chi tiêu</th>
                  <th style="min-width:160px">Ghi chú VIP</th>
                  <th style="min-width:140px">Ghi chú</th>
                  <th style="width:180px;text-align:center">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="paged.length === 0 && !loading">
                  <td colspan="10" class="crm2-empty">
                    <div class="crm2-empty__inner">
                      <div class="crm2-empty__icon"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></div>
                      <p>Không tìm thấy khách hàng</p>
                    </div>
                  </td>
                </tr>
                <tr v-for="row in paged" :key="row.id" class="crm2-row">
                  <td><span class="crm2-mono-id">{{ row.id }}</span></td>
                  <td>
                    <div class="crm2-customer-cell">
                      <div class="crm2-avatar" :style="{ background: getAvatarColor(row.fullName) }">{{ getInitials(row.fullName) }}</div>
                      <div class="crm2-cust-info">
                        <span class="crm2-cust-name">{{ row.fullName }}</span>
                        <span class="crm2-cust-dob crm2-mono" v-if="row.birthDate">{{ row.birthDate }}</span>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="crm2-contact-cell">
                      <span class="crm2-contact-email">{{ row.email }}</span>
                      <span class="crm2-contact-phone crm2-mono" v-if="row.phone">{{ row.phone }}</span>
                    </div>
                  </td>
                  <td><span class="crm2-tag" :class="row.customerType === 'VIP' ? 'crm2-tag--orange' : 'crm2-tag--gray'">{{ row.customerType || 'REGULAR' }}</span></td>
                  <td>
                    <span v-if="row.raw.vipTier" class="crm2-tier-tag" :class="'crm2-tier--' + row.raw.vipTier.toLowerCase()">{{ row.raw.vipTier }}</span>
                    <span v-else class="crm2-text--muted">—</span>
                  </td>
                  <td>
                    <div class="crm2-points-cell">
                      <span class="crm2-points-num crm2-mono">{{ (row.loyaltyPoints || 0).toLocaleString() }}</span>
                      <div class="crm2-points-track" v-if="row.raw.vipTier">
                        <div class="crm2-points-fill" :style="{ width: getPointsProgress(row.raw) + '%', background: getTierColor(row.raw.vipTier) }"></div>
                      </div>
                    </div>
                  </td>
                  <td><span class="crm2-mono crm2-text--muted">{{ formatCurrencyShort(row.raw.totalSpent) }}</span></td>
                  <td>
                    <template v-if="row.raw.vipTier">
                      <div v-if="row.raw.vipNote" class="crm2-vipnote-cell" @click="openVipNoteEdit(row)">
                        <span class="crm2-vipnote-text">{{ row.raw.vipNote.substring(0, 28) }}{{ row.raw.vipNote.length > 28 ? '…' : '' }}</span>
                      </div>
                      <button v-else class="crm2-btn-ghost-sm" @click="openVipNoteEdit(row)">+ Thêm ghi chú</button>
                    </template>
                    <span v-else class="crm2-text--muted">—</span>
                  </td>
                  <td>
                    <span class="crm2-note-text" v-if="row.notes" :title="row.notes">{{ row.notes.substring(0, 22) }}{{ row.notes.length > 22 ? '…' : '' }}</span>
                    <span v-else class="crm2-text--muted">—</span>
                  </td>
                  <td>
                    <div class="crm2-actions">
                      <button class="crm2-action-btn crm2-action-btn--blue" @click="viewDetails(row)">Xem</button>
                      <button class="crm2-action-btn crm2-action-btn--edit" @click="openEdit(row)">Sửa</button>
                      <button v-if="row.raw.vipTier" class="crm2-action-btn crm2-action-btn--gold" @click="openVipNoteEdit(row)">VIP</button>
                      <button class="crm2-action-btn crm2-action-btn--delete" @click="remove(row)">Xóa</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="crm2-pagination">
            <span class="crm2-pagination__info">Hiển thị {{ Math.min((page-1)*pageSize+1, filtered.length) }}–{{ Math.min(page*pageSize, filtered.length) }} của {{ filtered.length }}</span>
            <div class="crm2-pagination__controls">
              <button class="crm2-page-btn" :disabled="page===1" @click="page--">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
              </button>
              <template v-for="p in pageCount" :key="p">
                <button v-show="Math.abs(p-page)<=2 || p===1 || p===pageCount" class="crm2-page-btn" :class="{'crm2-page-btn--active': p===page}" @click="page=p">{{ p }}</button>
              </template>
              <button class="crm2-page-btn" :disabled="page===pageCount" @click="page++">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- ══ TAB 2: BIRTHDAYS ══ -->
      <template v-if="mainTab === 'birthdays'">
        <div class="crm2-sub-tabs">
          <button class="crm2-sub-tab" :class="{'crm2-sub-tab--active': birthdayTab==='today'}" @click="birthdayTab='today'">Hôm nay</button>
          <button class="crm2-sub-tab" :class="{'crm2-sub-tab--active': birthdayTab==='monthly'}" @click="birthdayTab='monthly'; loadMonthlyBirthdays()">Tháng này</button>
          <button class="crm2-sub-tab" :class="{'crm2-sub-tab--active': birthdayTab==='statistics'}" @click="birthdayTab='statistics'">Thống kê</button>
          <button class="crm2-sub-tab" :class="{'crm2-sub-tab--active': birthdayTab==='upcoming'}" @click="birthdayTab='upcoming'">Sắp tới</button>
          <button class="crm2-sub-tab" :class="{'crm2-sub-tab--active': birthdayTab==='history'}" @click="birthdayTab='history'; loadNotificationHistory()">Lịch sử</button>
        </div>

        <div class="crm2-card">
          <div v-if="loading" class="crm2-loading-center"><span class="crm2-spinner"></span> Đang tải…</div>

          <!-- Today -->
          <template v-if="!loading && birthdayTab==='today'">
            <div class="crm2-bd-banner" :class="todayBirthdays.length>0 ? 'crm2-bd-banner--green' : 'crm2-bd-banner--blue'">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              {{ todayBirthdays.length>0 ? `${todayBirthdays.length} sinh nhật hôm nay` : 'Không có sinh nhật nào hôm nay' }}
            </div>
            <div class="crm2-table-wrap">
              <table class="crm2-table">
                <thead><tr><th>Khách hàng</th><th>Email</th><th>SĐT</th><th>Tuổi</th><th>Loại</th><th>Cấp</th><th style="text-align:center">Thao tác</th></tr></thead>
                <tbody>
                  <tr v-if="todayBirthdays.length===0"><td colspan="7" class="crm2-empty"><div class="crm2-empty__inner"><p>Không có sinh nhật nào hôm nay</p></div></td></tr>
                  <tr v-for="row in todayBirthdays" :key="row.id" class="crm2-row">
                    <td><strong>{{ row.name }}</strong></td>
                    <td><span class="crm2-text--muted">{{ row.email }}</span></td>
                    <td><span class="crm2-mono crm2-text--muted">{{ row.phone }}</span></td>
                    <td><span class="crm2-mono">{{ row.age }}</span></td>
                    <td><span class="crm2-tag" :class="row.customerType==='VIP'?'crm2-tag--orange':'crm2-tag--gray'">{{ row.customerType }}</span></td>
                    <td><span v-if="row.vipTier" class="crm2-tier-tag" :class="'crm2-tier--'+row.vipTier.toLowerCase()">{{ row.vipTier }}</span><span v-else class="crm2-text--muted">—</span></td>
                    <td style="text-align:center"><button class="crm2-btn crm2-btn--primary" style="padding:6px 14px;font-size:12px" @click="sendGreeting(row)">Gửi lời chúc</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- Monthly -->
          <template v-if="!loading && birthdayTab==='monthly'">
            <div class="crm2-bd-controls">
              <select v-model="selectedMonth" @change="loadMonthlyBirthdays" class="crm2-input crm2-select" style="min-width:150px">
                <option v-for="m in monthOptions" :key="m.value" :value="m.value">{{ m.label }}</option>
              </select>
              <div class="crm2-bd-banner crm2-bd-banner--blue" style="flex:1;margin:0" v-if="monthlyBirthdays.length>0">
                <strong>{{ monthlyBirthdays.length }}</strong> khách hàng tháng này
              </div>
            </div>
            <div class="crm2-table-wrap">
              <table class="crm2-table">
                <thead><tr><th>Khách hàng</th><th>Email</th><th>SĐT</th><th>Ngày</th><th>Tuổi</th><th>Loại</th><th style="text-align:center">Thao tác</th></tr></thead>
                <tbody>
                  <tr v-if="monthlyBirthdays.length===0"><td colspan="7" class="crm2-empty"><div class="crm2-empty__inner"><p>Không có sinh nhật nào trong tháng</p></div></td></tr>
                  <tr v-for="row in monthlyBirthdays" :key="row.id" class="crm2-row">
                    <td><strong>{{ row.name }}</strong></td>
                    <td><span class="crm2-text--muted">{{ row.email }}</span></td>
                    <td><span class="crm2-mono crm2-text--muted">{{ row.phone }}</span></td>
                    <td>{{ formatBirthdayDate(row) }}</td>
                    <td><span class="crm2-mono">{{ row.age }}</span></td>
                    <td><span class="crm2-tag" :class="row.customerType==='VIP'?'crm2-tag--orange':'crm2-tag--gray'">{{ row.customerType }}</span></td>
                    <td style="text-align:center"><button class="crm2-btn crm2-btn--primary" style="padding:6px 14px;font-size:12px" @click="sendGreeting(row)">Send</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- Statistics -->
          <template v-if="!loading && birthdayTab==='statistics'">
            <div class="crm2-table-wrap">
              <table class="crm2-table">
                <thead><tr><th>Tháng</th><th>Số lượng</th><th style="min-width:200px">Phân bổ</th><th style="text-align:center">Thao tác</th></tr></thead>
                <tbody>
                  <tr v-for="row in statsTableData" :key="row.month" class="crm2-row">
                    <td><strong>{{ row.monthLabel }}</strong></td>
                    <td><span class="crm2-tag" :class="row.count>0?'crm2-tag--blue':'crm2-tag--gray'">{{ row.count }}</span></td>
                    <td>
                      <div class="crm2-bar-wrap"><div class="crm2-bar crm2-bar--blue" :style="{ width: getBdBarPct(row.count)+'%' }"></div></div>
                    </td>
                    <td style="text-align:center"><button v-if="row.count>0" class="crm2-btn-ghost-sm" @click="viewMonthDetail(row.month)">Chi tiết</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- Upcoming -->
          <template v-if="!loading && birthdayTab==='upcoming'">
            <div class="crm2-bd-banner" :class="upcomingBirthdays.length>0?'crm2-bd-banner--orange':'crm2-bd-banner--blue'">
              {{ upcomingBirthdays.length>0 ? `${upcomingBirthdays.length} sắp tới trong 7 ngày` : 'Không có sinh nhật sắp tới' }}
            </div>
            <div class="crm2-table-wrap">
              <table class="crm2-table">
                <thead><tr><th>Khách hàng</th><th>Email</th><th>SĐT</th><th>Ngày</th><th>Còn lại</th><th>Loại</th><th style="text-align:center">Thao tác</th></tr></thead>
                <tbody>
                  <tr v-if="upcomingBirthdays.length===0"><td colspan="7" class="crm2-empty"><div class="crm2-empty__inner"><p>Không có sinh nhật sắp tới</p></div></td></tr>
                  <tr v-for="row in upcomingBirthdays" :key="row.id" class="crm2-row">
                    <td><strong>{{ row.name }}</strong></td>
                    <td><span class="crm2-text--muted">{{ row.email }}</span></td>
                    <td><span class="crm2-mono crm2-text--muted">{{ row.phone }}</span></td>
                    <td>{{ formatBirthdayDate(row) }}</td>
                    <td><span class="crm2-tag" :class="row.daysUntilBirthday<=3?'crm2-tag--red':'crm2-tag--orange'">{{ row.daysUntilBirthday }} ngày</span></td>
                    <td><span class="crm2-tag" :class="row.customerType==='VIP'?'crm2-tag--orange':'crm2-tag--gray'">{{ row.customerType }}</span></td>
                    <td style="text-align:center"><button class="crm2-btn crm2-btn--primary" style="padding:6px 14px;font-size:12px" @click="sendGreeting(row)">Send</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- History -->
          <template v-if="birthdayTab==='history'">
            <div class="crm2-bd-controls" style="justify-content:space-between">
              <span class="crm2-bd-banner crm2-bd-banner--blue" style="margin:0;flex:1" v-if="notificationHistory.length>0"><strong>{{ notificationHistory.length }}</strong> thông báo đã gửi</span>
              <button class="crm2-btn-ghost-sm" @click="loadNotificationHistory" :disabled="historyLoading">
                <span v-if="historyLoading" class="crm2-spinner" style="width:12px;height:12px"></span>
                Tải lại
              </button>
            </div>
            <div v-if="historyLoading" class="crm2-loading-center"><span class="crm2-spinner"></span> Đang tải…</div>
            <div class="crm2-table-wrap" v-else>
              <table class="crm2-table">
                <thead><tr><th>Khách hàng</th><th>Email</th><th>Tiêu đề</th><th>Nội dung</th><th>Trạng thái</th><th>Gửi lúc</th><th>Đọc lúc</th></tr></thead>
                <tbody>
                  <tr v-if="notificationHistory.length===0"><td colspan="7" class="crm2-empty"><div class="crm2-empty__inner"><p>Chưa có thông báo nào</p></div></td></tr>
                  <tr v-for="row in notificationHistory" :key="row.id" class="crm2-row">
                    <td><strong>{{ row.customerName }}</strong></td>
                    <td><span class="crm2-text--muted">{{ row.customerEmail }}</span></td>
                    <td>{{ row.title }}</td>
                    <td class="crm2-td-truncate" :title="row.message">{{ row.message }}</td>
                    <td><span class="crm2-tag" :class="row.isRead?'crm2-tag--green':'crm2-tag--orange'">{{ row.isRead ? 'Đã đọc' : 'Chưa đọc' }}</span></td>
                    <td><span class="crm2-mono crm2-text--muted" style="font-size:12px">{{ formatDateTime(row.createdAt) }}</span></td>
                    <td><span class="crm2-mono crm2-text--muted" style="font-size:12px">{{ row.readAt ? formatDateTime(row.readAt) : '—' }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </div>
      </template>

      <!-- ══ TAB 3: INACTIVE ══ -->
      <template v-if="mainTab === 'inactive'">
        <div class="crm2-seg-tabs">
          <button v-for="tab in inactiveTabs" :key="tab.days"
            class="crm2-seg-btn" :class="{'crm2-seg-btn--active': activeInactiveTab===tab.days}"
            @click="switchInactiveTab(tab.days)">
            <span class="crm2-seg-label">{{ tab.label }}</span>
            <span class="crm2-seg-count" :class="{'crm2-seg-count--loaded': inactiveCounts[tab.days]!==null}">
              {{ inactiveCounts[tab.days]!==null ? inactiveCounts[tab.days] : '—' }}
            </span>
          </button>
        </div>

        <div class="crm2-stats4" v-if="!loading && currentInactive.length>0">
          <div class="crm2-stat crm2-stat--blue">
            <div class="crm2-stat__num">{{ currentInactive.length }}</div>
            <div class="crm2-stat__label">Khách hàng</div>
          </div>
          <div class="crm2-stat crm2-stat--orange">
            <div class="crm2-stat__num">{{ inactiveTotalPoints }}</div>
            <div class="crm2-stat__label">Tổng điểm</div>
          </div>
          <div class="crm2-stat crm2-stat--green">
            <div class="crm2-stat__num">{{ formatMoneyShort(inactiveTotalSpent) }}</div>
            <div class="crm2-stat__label">Tổng chi tiêu</div>
          </div>
          <div class="crm2-stat crm2-stat--purple">
            <div class="crm2-stat__num">{{ inactiveTopTier }}</div>
            <div class="crm2-stat__label">Cấp cao nhất</div>
          </div>
        </div>

        <div class="crm2-filter-bar" v-if="!loading">
          <input v-model="inactiveQ" placeholder="Tìm tên, email, SĐT…" class="crm2-input" style="flex:1" />
          <select v-model="inactiveTierFilter" class="crm2-input crm2-select" style="max-width:160px">
            <option value="">Tất cả cấp</option>
            <option v-for="t in tiers" :key="t" :value="t">{{ t }}</option>
          </select>
          <select v-model="inactiveSortBy" class="crm2-input crm2-select" style="max-width:200px">
            <option value="spent">Chi tiêu nhiều nhất</option>
            <option value="points">Điểm nhiều nhất</option>
            <option value="name">Tên A→Z</option>
          </select>
        </div>

        <div v-if="loading" class="crm2-loading-center"><span class="crm2-spinner"></span> Đang tải…</div>

        <div class="crm2-empty" v-if="!loading && inactiveFiltered.length===0" style="padding:80px 20px">
          <div class="crm2-empty__inner">
            <div class="crm2-empty__icon"><svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="20 6 9 17 4 12"/></svg></div>
            <p style="font-size:16px;font-weight:700;color:var(--c-text)">Tốt cả</p>
            <p>Tất cả khách hàng đều hoạt động trong {{ activeInactiveTab }} ngày qua.</p>
          </div>
        </div>

        <div class="crm2-cards-grid" v-if="!loading && inactivePaged.length>0">
          <div class="crm2-cust-card" v-for="(row, idx) in inactivePaged" :key="row.id" :style="{ animationDelay: idx*0.04+'s' }">
            <div class="crm2-card-tier-bar" :class="'crm2-tier-bar--'+(row.raw?.vipTier||'none').toLowerCase()"></div>
            <div class="crm2-card-head">
              <div class="crm2-avatar" :style="{ background: getAvatarColor(row.fullName) }">{{ getInitials(row.fullName) }}</div>
              <div class="crm2-card-info">
                <div class="crm2-card-name">{{ row.fullName }}</div>
                <div class="crm2-card-email crm2-text--muted">{{ row.email }}</div>
              </div>
              <span v-if="row.raw?.vipTier" class="crm2-tier-tag crm2-tier-tag--sm" :class="'crm2-tier--'+row.raw.vipTier.toLowerCase()">{{ row.raw.vipTier }}</span>
            </div>
            <div class="crm2-card-divider"></div>
            <div class="crm2-card-metrics">
              <div class="crm2-cm"><div class="crm2-cm__val crm2-mono">{{ (row.loyaltyPoints||0).toLocaleString() }}</div><div class="crm2-cm__label">Điểm</div></div>
              <div class="crm2-cm-divider"></div>
              <div class="crm2-cm"><div class="crm2-cm__val crm2-mono">{{ formatMoneyShort(row.raw?.totalSpent) }}</div><div class="crm2-cm__label">Chi tiêu</div></div>
              <div class="crm2-cm-divider"></div>
              <div class="crm2-cm"><div class="crm2-cm__val crm2-mono" style="font-size:11px">{{ row.phone || '—' }}</div><div class="crm2-cm__label">SĐT</div></div>
            </div>
            <div class="crm2-card-foot">
              <span class="crm2-last-order" :class="{'crm2-last-order--none': !row.raw?.lastOrderAt}">
                {{ row.raw?.lastOrderAt ? 'Đơn cuối: '+relativeDate(row.raw.lastOrderAt) : 'Chưa có đơn hàng' }}
              </span>
              <button class="crm2-action-btn crm2-action-btn--blue" @click="openInactiveDetail(row)">Xem</button>
            </div>
          </div>
        </div>

        <div class="crm2-pagination" v-if="!loading && inactiveFiltered.length>inactivePageSize">
          <span class="crm2-pagination__info">{{ (inactivePage-1)*inactivePageSize+1 }}–{{ Math.min(inactivePage*inactivePageSize, inactiveFiltered.length) }} / {{ inactiveFiltered.length }}</span>
          <div class="crm2-pagination__controls">
            <button class="crm2-page-btn" :disabled="inactivePage===1" @click="inactivePage--">‹</button>
            <button v-for="p in inactiveTotalPages" :key="p" class="crm2-page-btn" :class="{'crm2-page-btn--active':p===inactivePage}" @click="inactivePage=p">{{ p }}</button>
            <button class="crm2-page-btn" :disabled="inactivePage===inactiveTotalPages" @click="inactivePage++">›</button>
          </div>
        </div>
      </template>

      <!-- ══ TAB 4: ZERO ORDER ══ -->
      <template v-if="mainTab === 'zeroorder'">

        <!-- Stats -->
        <div class="crm2-stats4">
          <div class="crm2-stat crm2-stat--dim">
            <div class="crm2-stat__num">{{ zeroOrderStats.registeredOver3Days ?? '—' }}</div>
            <div class="crm2-stat__label">Đăng ký ≥ 3 ngày</div>
          </div>
          <div class="crm2-stat crm2-stat--orange">
            <div class="crm2-stat__num">{{ zeroOrderStats.registeredOver7Days ?? '—' }}</div>
            <div class="crm2-stat__label">Đăng ký ≥ 7 ngày</div>
          </div>
          <div class="crm2-stat crm2-stat--red">
            <div class="crm2-stat__num">{{ zeroOrderStats.registeredOver30Days ?? '—' }}</div>
            <div class="crm2-stat__label">Đăng ký ≥ 30 ngày</div>
          </div>
          <div class="crm2-stat crm2-stat--blue">
            <div class="crm2-stat__num">{{ zoSelected.length }}</div>
            <div class="crm2-stat__label">Đang chọn</div>
          </div>
        </div>

        <!-- Filter + actions -->
        <div class="crm2-filter-panel" style="margin-bottom:16px">
          <div class="crm2-filter-panel__head">
            <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
              <span class="crm2-filter-panel__title">Đăng ký tối thiểu</span>
              <div style="display:flex;gap:6px">
                <button v-for="opt in zoDayOptions" :key="opt.value" class="crm2-page-btn" :class="{'crm2-page-btn--active': zoSelectedDays === opt.value}" @click="zoSelectDays(opt.value)">{{ opt.label }}</button>
              </div>
            </div>
            <div style="display:flex;gap:8px;align-items:center">
              <button class="crm2-btn crm2-btn--outline" @click="zoSelectAll">{{ zoIsAllSelected ? 'Bỏ chọn tất cả' : 'Chọn tất cả' }}</button>
              <button class="crm2-btn crm2-btn--primary" :disabled="zoSelected.length === 0 || zoSending" @click="openZoSendDialog">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                Gửi thông báo
                <span v-if="zoSelected.length > 0" class="crm2-count-badge">{{ zoSelected.length }}</span>
              </button>
            </div>
          </div>
          <div style="padding:12px 16px 16px">
            <span class="crm2-filter-field__label" style="display:block;margin-bottom:6px">Tìm kiếm</span>
            <input v-model="zoSearch" class="crm2-input" placeholder="Tên / Email / SĐT…" @input="zoPage=1" style="max-width:400px" />
          </div>
        </div>

        <!-- Table -->
        <div class="crm2-card">
          <div v-if="loading" class="crm2-loading-center"><span class="crm2-spinner"></span> Đang tải…</div>
          <div class="crm2-table-wrap" v-else>
            <table class="crm2-table">
              <thead>
                <tr>
                  <th style="width:44px;text-align:center"><input type="checkbox" :checked="zoIsAllSelected" @change="zoSelectAll" /></th>
                  <th>Khách hàng</th>
                  <th>Loại</th>
                  <th>Đăng ký lúc</th>
                  <th>Ngày chờ</th>
                  <th>Trạng thái</th>
                  <th style="text-align:center">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="zoFiltered.length === 0">
                  <td colspan="7" class="crm2-empty"><div class="crm2-empty__inner"><p>🎉 Tất cả khách đều đã mua hàng trong khoảng này!</p></div></td>
                </tr>
                <tr v-for="c in zoPaged" :key="c.id" class="crm2-row" :class="{'crm2-row--selected': zoSelected.includes(c.id)}">
                  <td style="text-align:center"><input type="checkbox" :value="c.id" v-model="zoSelected" /></td>
                  <td>
                    <div class="crm2-customer-cell">
                      <div class="crm2-avatar" :style="{ background: getAvatarColor(c.name) }">{{ getInitials(c.name) }}</div>
                      <div class="crm2-cust-info">
                        <span class="crm2-cust-name">{{ c.name }}</span>
                        <span class="crm2-contact-email">{{ c.email }}</span>
                        <span class="crm2-contact-phone crm2-mono" v-if="c.phone">{{ c.phone }}</span>
                      </div>
                    </div>
                  </td>
                  <td><span class="crm2-tag" :class="c.customerType === 'VIP' ? 'crm2-tag--orange' : 'crm2-tag--gray'">{{ c.customerType || 'REGULAR' }}</span></td>
                  <td>
                    <div class="crm2-mono" style="font-size:12px">{{ formatDate(c.createdAt) }}</div>
                    <div class="crm2-text--muted" style="font-size:11px">{{ relativeDate(c.createdAt) }}</div>
                  </td>
                  <td>
                    <span class="crm2-tag" :class="zoDaysSince(c.createdAt) >= 30 ? 'crm2-tag--red' : zoDaysSince(c.createdAt) >= 7 ? 'crm2-tag--orange' : 'crm2-tag--gray'">
                      {{ zoDaysSince(c.createdAt) }} ngày
                    </span>
                  </td>
                  <td><span class="crm2-text--muted" style="font-size:12.5px">{{ zoStatusLabel(c) }}</span></td>
                  <td style="text-align:center">
                    <button class="crm2-action-btn crm2-action-btn--blue" :disabled="zoSendingIds.has(c.id)" @click="zoSendSingle(c)">
                      <span v-if="zoSendingIds.has(c.id)" class="crm2-spinner" style="width:12px;height:12px;border-top-color:#fff"></span>
                      {{ zoSendingIds.has(c.id) ? '…' : '📨 Gửi' }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="crm2-pagination">
            <span class="crm2-pagination__info">
              {{ zoFiltered.length > 0 ? `${Math.min((zoPage-1)*pageSize+1, zoFiltered.length)}–${Math.min(zoPage*pageSize, zoFiltered.length)} của ${zoFiltered.length}` : '0 khách' }}
            </span>
            <div class="crm2-pagination__controls" v-if="zoPageCount > 1">
              <button class="crm2-page-btn" :disabled="zoPage===1" @click="zoPage--">‹</button>
              <button v-for="p in zoPageCount" :key="p" class="crm2-page-btn" :class="{'crm2-page-btn--active': p===zoPage}" v-show="Math.abs(p-zoPage)<=2 || p===1 || p===zoPageCount" @click="zoPage=p">{{ p }}</button>
              <button class="crm2-page-btn" :disabled="zoPage===zoPageCount" @click="zoPage++">›</button>
            </div>
          </div>
        </div>
      </template>

    </div>

    <!-- ══════════════════════════════════════
         DIALOG: Create / Edit
    ═══════════════════════════════════════ -->
    <Teleport to="body">
      <div class="crm2-overlay" v-if="dlg.open" @click.self="dlg.open=false">
        <div class="crm2-dialog crm2-dialog--lg">

          <div class="crm2-dialog__band" :class="dlg.mode==='create' ? 'crm2-dialog__band--blue' : 'crm2-dialog__band--orange'">
            <div class="crm2-dialog__band-circles"><span class="crm2-band-circle crm2-band-circle--1"/><span class="crm2-band-circle crm2-band-circle--2"/><span class="crm2-band-circle crm2-band-circle--3"/></div>
            <div class="crm2-dialog__icon-wrap"><div class="crm2-dialog__icon-ring">
              <svg v-if="dlg.mode==='create'" width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              <svg v-else width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            </div></div>
          </div>

          <div class="crm2-dialog__body">
            <div class="crm2-dialog__badge" :class="dlg.mode==='create'?'crm2-dialog__badge--blue':'crm2-dialog__badge--orange'">{{ dlg.mode==='create' ? 'Khách hàng mới' : 'Chỉnh sửa' }}</div>
            <div class="crm2-dialog__head-row">
              <h2 class="crm2-dialog__title">{{ dlg.mode==='create' ? 'Thêm khách hàng' : 'Sửa khách hàng' }}</h2>
              <button class="crm2-icon-btn" @click="dlg.open=false"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
            </div>

            <div v-if="dlg.alert" class="crm2-dialog__notice crm2-dialog__notice--red">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ dlg.alert }}
            </div>

            <div class="crm2-dlg-section">Thông tin cơ bản</div>
            <div class="crm2-row2">
              <div class="crm2-field"><label class="crm2-field__label">Họ và tên <span class="crm2-required">*</span></label><input v-model="dlg.form.fullName" class="crm2-input" placeholder="Nguyen Van A" /></div>
              <div class="crm2-field"><label class="crm2-field__label">Loại khách hàng</label>
                <select v-model="dlg.form.customerType" class="crm2-input crm2-select">
                  <option value="REGULAR">Thường</option>
                  <option value="VIP">VIP</option>
                </select>
              </div>
            </div>
            <div class="crm2-row2">
              <div class="crm2-field"><label class="crm2-field__label">Email <span class="crm2-required">*</span></label><input v-model="dlg.form.email" type="email" class="crm2-input" placeholder="email@example.com" /></div>
              <div class="crm2-field"><label class="crm2-field__label">SĐT <span class="crm2-required">*</span></label><input v-model="dlg.form.phone" class="crm2-input" placeholder="0912345678" /></div>
            </div>
            <div class="crm2-row2">
              <div class="crm2-field"><label class="crm2-field__label">Ngày sinh</label><input v-model="dlg.form.birthDate" type="date" class="crm2-input" /></div>
              <div class="crm2-field"><label class="crm2-field__label">Địa chỉ</label><input v-model="dlg.form.address" class="crm2-input" placeholder="123 Street, City" /></div>
            </div>
            <div class="crm2-field"><label class="crm2-field__label">Ghi chú</label><textarea v-model="dlg.form.notes" class="crm2-input crm2-textarea" rows="2" placeholder="General notes…"></textarea></div>

            <template v-if="dlg.mode==='edit' && dlg.form.vipTier">
              <div class="crm2-dlg-section">Ghi chú VIP <span class="crm2-internal-tag">Nội bộ · Chỉ admin</span></div>
              <div class="crm2-vip-note-block">
                <div class="crm2-vip-note-head">
                  <span class="crm2-tier-tag" :class="'crm2-tier--'+dlg.form.vipTier.toLowerCase()">{{ tierIcon(dlg.form.vipTier) }} {{ dlg.form.vipTier }}</span>
                </div>
                <textarea v-model="dlg.form.vipNote" class="crm2-input crm2-textarea crm2-vip-textarea" rows="3" placeholder="e.g. Prefers gaming laptops, no display units…" maxlength="500"></textarea>
                <div class="crm2-vip-note-hint">Khách hàng không thể xem ghi chú này. <span class="crm2-mono" style="font-size:11px">{{ (dlg.form.vipNote||'').length }}/500</span></div>
              </div>
            </template>
            <div v-if="dlg.mode==='create'" class="crm2-vip-unavail">Ghi chú VIP có sẵn khi khách hàng đạt cấp Đồng trở lên.</div>

            <div class="crm2-dialog__actions">
              <button class="crm2-btn crm2-btn--ghost" @click="dlg.open=false">Hủy bỏ</button>
              <button class="crm2-btn" :class="[dlg.mode==='create'?'crm2-btn--confirm-blue':'crm2-btn--confirm-orange', {'crm2-btn--loading': dlg.loading}]" @click="save" :disabled="dlg.loading">
                <span v-if="dlg.loading" class="crm2-spinner crm2-spinner--white"></span>
                <svg v-else-if="dlg.mode==='create'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/></svg>
                {{ dlg.mode==='create' ? 'Tạo mới' : 'Lưu thay đổi' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ══ DIALOG: Details ══ -->
    <Teleport to="body">
      <div class="crm2-overlay" v-if="detailsDialog.open" @click.self="detailsDialog.open=false">
        <div class="crm2-dialog crm2-dialog--lg" v-if="detailsDialog.customer">
          <div class="crm2-dialog__head-simple">
            <div class="crm2-details-hd">
              <div class="crm2-avatar crm2-avatar--lg" :style="{ background: getAvatarColor(detailsDialog.customer.fullName) }">{{ getInitials(detailsDialog.customer.fullName) }}</div>
              <div>
                <h2 class="crm2-dialog__title">{{ detailsDialog.customer.fullName }}</h2>
                <p class="crm2-dialog__sub">#{{ detailsDialog.customer.id }} · {{ detailsDialog.customer.email }}</p>
              </div>
            </div>
            <button class="crm2-icon-btn" @click="detailsDialog.open=false"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
          </div>
          <div class="crm2-dialog__body" style="padding-top:0">
            <div class="crm2-details-grid">
              <div class="crm2-detail-block">
                <div class="crm2-detail-block__head">Thông tin cá nhân</div>
                <div class="crm2-detail-rows">
                  <div class="crm2-detail-row"><span class="crm2-dk">Mã</span><span class="crm2-dv crm2-mono-id">#{{ detailsDialog.customer.id }}</span></div>
                  <div class="crm2-detail-row"><span class="crm2-dk">Tên</span><strong class="crm2-dv">{{ detailsDialog.customer.fullName }}</strong></div>
                  <div class="crm2-detail-row"><span class="crm2-dk">Email</span><span class="crm2-dv">{{ detailsDialog.customer.email }}</span></div>
                  <div class="crm2-detail-row"><span class="crm2-dk">SĐT</span><span class="crm2-dv crm2-mono">{{ detailsDialog.customer.phone||'—' }}</span></div>
                  <div class="crm2-detail-row"><span class="crm2-dk">Ngày sinh</span><span class="crm2-dv">{{ detailsDialog.customer.birthDate||'—' }}</span></div>
                  <div class="crm2-detail-row"><span class="crm2-dk">Địa chỉ</span><span class="crm2-dv">{{ detailsDialog.customer.address||'—' }}</span></div>
                </div>
              </div>
              <div class="crm2-detail-block">
                <div class="crm2-detail-block__head">Loyalty &amp; VIP</div>
                <div class="crm2-detail-rows">
                  <div class="crm2-detail-row"><span class="crm2-dk">Loại</span><span class="crm2-tag" :class="detailsDialog.customer.customerType==='VIP'?'crm2-tag--orange':'crm2-tag--gray'">{{ detailsDialog.customer.customerType }}</span></div>
                  <div class="crm2-detail-row"><span class="crm2-dk">Cấp</span>
                    <span v-if="detailsDialog.customer.raw.vipTier" class="crm2-tier-tag" :class="'crm2-tier--'+detailsDialog.customer.raw.vipTier.toLowerCase()">{{ detailsDialog.customer.raw.vipTier }}</span>
                    <span v-else class="crm2-text--muted">Chưa có cấp</span>
                  </div>
                  <div class="crm2-detail-row"><span class="crm2-dk">Điểm</span><strong class="crm2-dv crm2-text--blue">{{ (detailsDialog.customer.loyaltyPoints||0).toLocaleString() }} điểm</strong></div>
                  <div class="crm2-detail-row"><span class="crm2-dk">Tổng chi tiêu</span><strong class="crm2-dv crm2-text--green">{{ formatCurrency(detailsDialog.customer.raw.totalSpent) }}</strong></div>
                </div>
              </div>
              <div class="crm2-detail-block" v-if="detailsDialog.customer.notes">
                <div class="crm2-detail-block__head">Ghi chú</div>
                <p class="crm2-detail-body-text">{{ detailsDialog.customer.notes }}</p>
              </div>
              <div class="crm2-detail-block crm2-vip-note-block" v-if="detailsDialog.customer.raw.vipTier">
                <div class="crm2-detail-block__head">
                  Ghi chú VIP <span class="crm2-internal-tag">Chỉ nội bộ</span>
                  <button class="crm2-btn-ghost-sm" style="margin-left:auto" @click="openVipNoteEdit(detailsDialog.customer)">Sửa</button>
                </div>
                <p v-if="detailsDialog.customer.raw.vipNote" class="crm2-detail-body-text" style="color:var(--c-orange)">{{ detailsDialog.customer.raw.vipNote }}</p>
                <p v-else class="crm2-detail-body-text crm2-text--muted" style="font-style:italic">Chưa có ghi chú VIP. <button class="crm2-btn-link" @click="openVipNoteEdit(detailsDialog.customer)">Thêm một →</button></p>
              </div>
            </div>
            <div class="crm2-dialog__actions">
              <button class="crm2-btn crm2-btn--ghost" @click="detailsDialog.open=false">Đóng</button>
              <button class="crm2-btn crm2-btn--primary" @click="openEdit(detailsDialog.customer)">Sửa khách hàng</button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ══ DIALOG: VIP Note ══ -->
    <Teleport to="body">
      <div class="crm2-overlay" v-if="vipNoteDialog.open" @click.self="vipNoteDialog.open=false">
        <div class="crm2-dialog crm2-dialog--sm crm2-vip-modal">
          <div class="crm2-vip-band">
            <div class="crm2-vip-band__deco crm2-vip-band__deco--1"/>
            <div class="crm2-vip-band__deco crm2-vip-band__deco--2"/>
            <div class="crm2-vip-band__content">
              <div class="crm2-vip-band__top">
                <span class="crm2-vip-eyebrow">VIP Internal Note</span>
                <span class="crm2-vip-admin-badge">Admin only</span>
              </div>
              <div class="crm2-vip-band__title">Ghi chú VIP</div>
              <div class="crm2-vip-band__customer" v-if="vipNoteDialog.customerName">
                ♦ {{ vipNoteDialog.vipTier }} · {{ vipNoteDialog.customerName }}
              </div>
            </div>
            <button class="crm2-vip-close-btn" @click="vipNoteDialog.open=false">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <div class="crm2-vip-notice">
            🔒 Chỉ nội bộ — Khách hàng <strong>không thể xem</strong>. Chỉ Admin và Staff.
          </div>

          <div class="crm2-dialog__body">
            <div v-if="!vipNoteDialog.note" class="crm2-vip-suggestions">
              <p class="crm2-vip-suggestion-label">Gợi ý nhanh</p>
              <div class="crm2-vip-chips">
                <button v-for="s in vipSuggestions" :key="s" class="crm2-vip-chip" @click="vipNoteDialog.note = s">{{ s }}</button>
              </div>
            </div>
            <div class="crm2-vip-textarea-wrap">
              <textarea v-model="vipNoteDialog.note" class="crm2-vip-textarea" rows="5" placeholder="Ghi chú nội bộ về khách hàng VIP này…" maxlength="500" ref="vipNoteTextarea"></textarea>
              <div class="crm2-vip-textarea-foot">
                <button v-if="vipNoteDialog.note" class="crm2-btn-link" style="color:var(--c-red)" @click="vipNoteDialog.note = ''">Xóa nội dung</button>
                <span class="crm2-mono" style="font-size:11px;color:var(--c-subtle);margin-left:auto" :style="{color:(vipNoteDialog.note||'').length>450?'var(--c-red)':(vipNoteDialog.note||'').length>350?'var(--c-orange)':'var(--c-subtle)'}">{{ (vipNoteDialog.note||'').length }}/500</span>
              </div>
            </div>
            <div class="crm2-dialog__actions">
              <button class="crm2-btn crm2-btn--ghost" @click="vipNoteDialog.open=false">Hủy bỏ</button>
              <button class="crm2-btn crm2-btn--confirm-orange" :class="{'crm2-btn--loading': vipNoteDialog.loading}" @click="saveVipNote" :disabled="vipNoteDialog.loading">
                <span v-if="vipNoteDialog.loading" class="crm2-spinner crm2-spinner--white"></span>
                <span v-else>✦</span>
                Lưu ghi chú VIP
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ══ DIALOG: Loyalty Summary ══ -->
    <Teleport to="body">
      <div class="crm2-overlay" v-if="summaryDlg.open" @click.self="summaryDlg.open=false">
        <div class="crm2-dialog crm2-dialog--xl">
          <div class="crm2-dialog__head-simple" style="border-bottom:1px solid var(--c-border-light)">
            <div>
              <h2 class="crm2-dialog__title">Tổng điểm</h2>
              <p class="crm2-dialog__sub">Phân tích điểm khách hàng trên tất cả khách hàng</p>
            </div>
            <button class="crm2-icon-btn" @click="summaryDlg.open=false"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
          </div>

          <!-- Controls -->
          <div class="crm2-summary-controls">
            <div class="crm2-sc-left">
              <div class="crm2-ctrl-group">
                <span class="crm2-ctrl-label">Kỳ</span>
                <div class="crm2-toggle-set">
                  <button :class="['crm2-tgl', summaryDlg.mode==='monthly'&&'crm2-tgl--on']" @click="summaryDlg.mode='monthly'; loadAdminSummary()">Tháng</button>
                  <button :class="['crm2-tgl', summaryDlg.mode==='weekly'&&'crm2-tgl--on']" @click="summaryDlg.mode='weekly'; loadAdminSummary()">Tuần</button>
                </div>
              </div>
              <div class="crm2-ctrl-group">
                <span class="crm2-ctrl-label">{{ summaryDlg.mode==='weekly' ? 'Tuần' : 'Tháng' }}</span>
                <select class="crm2-input crm2-select" style="width:80px" v-model="summaryDlg.range" @change="loadAdminSummary">
                  <template v-if="summaryDlg.mode==='weekly'">
                    <option :value="4">4w</option><option :value="8">8w</option><option :value="12">12w</option><option :value="26">26w</option>
                  </template>
                  <template v-else>
                    <option :value="3">3m</option><option :value="6">6m</option><option :value="12">12m</option><option :value="24">24m</option>
                  </template>
                </select>
              </div>
              <div class="crm2-ctrl-group">
                <span class="crm2-ctrl-label">Xem</span>
                <div class="crm2-toggle-set">
                  <button :class="['crm2-tgl', summaryDlg.view==='overview'&&'crm2-tgl--on']" @click="summaryDlg.view='overview'">Tổng quan</button>
                  <button :class="['crm2-tgl', summaryDlg.view==='detail'&&'crm2-tgl--on']" @click="summaryDlg.view='detail'">Theo KH</button>
                </div>
              </div>
            </div>
            <button class="crm2-btn-ghost-sm" @click="loadAdminSummary" :disabled="summaryDlg.loading">
              <span v-if="summaryDlg.loading" class="crm2-spinner" style="width:12px;height:12px"></span> Tải lại
            </button>
          </div>

          <div class="crm2-dialog__body crm2-summary-body">
            <div v-if="summaryDlg.loading" class="crm2-loading-center"><span class="crm2-spinner"></span> Đang tải…</div>
            <div v-else-if="!summaryDlg.data.length" class="crm2-empty" style="padding:40px"><div class="crm2-empty__inner"><p>Không có dữ liệu trong kỳ này</p></div></div>
            <template v-else>
              <!-- KPI Row -->
              <div class="crm2-kpi-row">
                <div class="crm2-kpi crm2-kpi--green"><div class="crm2-kpi__val">+{{ adminSummaryTotals.earned.toLocaleString() }}</div><div class="crm2-kpi__label">Điểm kiếm được</div></div>
                <div class="crm2-kpi crm2-kpi--orange"><div class="crm2-kpi__val">-{{ adminSummaryTotals.deducted.toLocaleString() }}</div><div class="crm2-kpi__label">Điểm đã dùng</div></div>
                <div class="crm2-kpi" :class="adminSummaryTotals.net>=0?'crm2-kpi--blue':'crm2-kpi--red'"><div class="crm2-kpi__val">{{ adminSummaryTotals.net>=0?'+':'' }}{{ adminSummaryTotals.net.toLocaleString() }}</div><div class="crm2-kpi__label">Lý thuyết</div></div>
                <div class="crm2-kpi crm2-kpi--purple"><div class="crm2-kpi__val">{{ adminSummaryTotals.transactions.toLocaleString() }}</div><div class="crm2-kpi__label">Giao dịch</div></div>
              </div>

              <template v-if="summaryDlg.view==='overview'">
                <div class="crm2-period-list">
                  <div v-for="period in summaryDlg.data" :key="period.period" class="crm2-period-row">
                    <div class="crm2-period-meta">
                      <div class="crm2-period-label">{{ period.periodLabel }}</div>
                      <div class="crm2-period-dates crm2-mono crm2-text--muted">{{ formatAdminDate(period.periodStart) }} – {{ formatAdminDate(period.periodEnd) }}</div>
                    </div>
                    <div class="crm2-period-bars">
                      <div class="crm2-bar-row">
                        <span class="crm2-br-num crm2-text--green crm2-mono">+{{ period.totalPointsEarned.toLocaleString() }}</span>
                        <div class="crm2-bar-bg"><div class="crm2-bar crm2-bar--green" :style="{width:getAdminBarWidth(period.totalPointsEarned,'earn')+'%'}"></div></div>
                      </div>
                      <div class="crm2-bar-row">
                        <span class="crm2-br-num crm2-text--orange crm2-mono">-{{ period.totalPointsDeducted.toLocaleString() }}</span>
                        <div class="crm2-bar-bg"><div class="crm2-bar crm2-bar--orange" :style="{width:getAdminBarWidth(period.totalPointsDeducted,'deduct')+'%'}"></div></div>
                      </div>
                    </div>
                    <div class="crm2-period-net">
                      <span class="crm2-net-chip" :class="period.netPoints>=0?'crm2-net-chip--pos':'crm2-net-chip--neg'">{{ period.netPoints>=0?'+':'' }}{{ period.netPoints.toLocaleString() }}</span>
                      <span class="crm2-mono crm2-text--muted" style="font-size:11px">{{ period.totalTransactions }} gd</span>
                    </div>
                  </div>
                </div>
              </template>

              <template v-else>
                <div v-for="period in summaryDlg.data" :key="period.period" class="crm2-cust-period">
                  <div class="crm2-cust-period__head">
                    <span class="crm2-mono crm2-text--muted" style="font-weight:700">{{ period.periodLabel }}</span>
                    <div style="display:flex;align-items:center;gap:8px;font-size:11px;font-family:'JetBrains Mono',monospace">
                      <span class="crm2-text--green">+{{ period.totalPointsEarned.toLocaleString() }}</span>
                      <span class="crm2-text--muted">/</span>
                      <span class="crm2-text--orange">-{{ period.totalPointsDeducted.toLocaleString() }}</span>
                      <span class="crm2-text--muted">{{ period.totalTransactions }} txn</span>
                    </div>
                  </div>
                  <table class="crm2-table" v-if="period.customerBreakdown?.length">
                    <thead><tr><th>Khách hàng</th><th>Cấp</th><th style="text-align:right">Kiếm được</th><th style="text-align:right">Đã dùng</th><th style="text-align:right">Lý thuyết</th><th style="text-align:center">Gd</th></tr></thead>
                    <tbody>
                      <tr v-for="c in period.customerBreakdown" :key="c.customerId" class="crm2-row">
                        <td><div style="font-size:12px;font-weight:600">{{ c.customerName }}</div><div class="crm2-text--muted" style="font-size:11px">{{ c.customerEmail }}</div></td>
                        <td><span v-if="c.vipTier&&c.vipTier!=='Member'" class="crm2-tier-tag" :class="'crm2-tier--'+c.vipTier.toLowerCase()">{{ c.vipTier }}</span><span v-else class="crm2-text--muted">—</span></td>
                        <td style="text-align:right" class="crm2-text--green crm2-mono">+{{ c.pointsEarned.toLocaleString() }}</td>
                        <td style="text-align:right" class="crm2-text--orange crm2-mono">{{ c.pointsDeducted>0?'-':'' }}{{ c.pointsDeducted.toLocaleString() }}</td>
                        <td style="text-align:right"><span class="crm2-mono" :style="{color:c.netPoints>=0?'var(--c-green)':'var(--c-red)',fontWeight:700}">{{ c.netPoints>=0?'+':'' }}{{ c.netPoints.toLocaleString() }}</span></td>
                        <td style="text-align:center" class="crm2-text--muted crm2-mono">{{ c.transactionCount }}</td>
                      </tr>
                    </tbody>
                  </table>
                  <div v-else class="crm2-empty" style="padding:16px"><div class="crm2-empty__inner" style="gap:6px"><p>Không có giao dịch</p></div></div>
                </div>
              </template>
            </template>
          </div>
          <div class="crm2-dialog__actions" style="padding:14px 24px;border-top:1px solid var(--c-border-light)">
            <button class="crm2-btn crm2-btn--ghost" @click="summaryDlg.open=false">Đóng</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ══ DIALOG: Greeting ══ -->
    <Teleport to="body">
      <div class="crm2-overlay" v-if="greetingDialog.open" @click.self="greetingDialog.open=false">
        <div class="crm2-dialog crm2-dialog--sm">
          <div class="crm2-dialog__band crm2-dialog__band--green">
            <div class="crm2-dialog__band-circles"><span class="crm2-band-circle crm2-band-circle--1"/><span class="crm2-band-circle crm2-band-circle--2"/><span class="crm2-band-circle crm2-band-circle--3"/></div>
            <div class="crm2-dialog__icon-wrap"><div class="crm2-dialog__icon-ring">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            </div></div>
          </div>
          <div class="crm2-dialog__body">
            <div class="crm2-dialog__badge crm2-dialog__badge--green">Lời chúc sinh nhật</div>
            <div class="crm2-dialog__head-row">
              <h2 class="crm2-dialog__title">Gửi lời chúc</h2>
              <button class="crm2-icon-btn" @click="greetingDialog.open=false"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
            </div>
            <div v-if="greetingDialog.customer" class="crm2-dialog__notice" style="background:var(--c-blue-bg);color:var(--c-blue);border-color:var(--c-blue-border)">
              Gửi tới: <strong>{{ greetingDialog.customer.name }}</strong> ({{ greetingDialog.customer.email }})
            </div>
            <div class="crm2-field"><label class="crm2-field__label">Tin nhắn</label><textarea v-model="greetingDialog.form.message" class="crm2-input crm2-textarea" rows="8" placeholder="Viết lời chúc sinh nhật…"></textarea></div>
            <div class="crm2-dialog__actions">
              <button class="crm2-btn crm2-btn--ghost" @click="greetingDialog.open=false">Hủy bỏ</button>
              <button class="crm2-btn crm2-btn--confirm-green" :class="{'crm2-btn--loading': greetingDialog.sending}" @click="confirmSendGreeting" :disabled="greetingDialog.sending">
                <span v-if="greetingDialog.sending" class="crm2-spinner crm2-spinner--white"></span>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                Gửi lời chúc
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ══ DIALOG: Inactive Detail ══ -->
    <Teleport to="body">
      <div class="crm2-overlay" v-if="inactiveDetail.open" @click.self="inactiveDetail.open=false">
        <div class="crm2-dialog crm2-dialog--sm" v-if="inactiveDetail.customer" style="text-align:center">
          <div class="crm2-dialog__body">
            <div style="display:flex;justify-content:flex-end"><button class="crm2-icon-btn" @click="inactiveDetail.open=false"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>
            <div class="crm2-avatar crm2-avatar--xl" style="margin:0 auto 12px" :style="{background:getAvatarColor(inactiveDetail.customer.fullName)}">{{ getInitials(inactiveDetail.customer.fullName) }}</div>
            <div style="font-size:20px;font-weight:800;letter-spacing:-0.02em;margin-bottom:4px">{{ inactiveDetail.customer.fullName }}</div>
            <div class="crm2-text--muted" style="font-size:13px;margin-bottom:12px">{{ inactiveDetail.customer.email }}</div>
            <span v-if="inactiveDetail.customer.raw?.vipTier" class="crm2-tier-tag" :class="'crm2-tier--'+inactiveDetail.customer.raw.vipTier.toLowerCase()" style="font-size:13px;padding:4px 14px">{{ inactiveDetail.customer.raw.vipTier }}</span>
            <div class="crm2-stat-row">
              <div class="crm2-stat-mini crm2-stat-mini--orange"><div class="crm2-stat-mini__val">{{ (inactiveDetail.customer.loyaltyPoints||0).toLocaleString() }}</div><div class="crm2-stat-mini__label">Điểm</div></div>
              <div class="crm2-stat-mini crm2-stat-mini--green"><div class="crm2-stat-mini__val">{{ formatCurrency(inactiveDetail.customer.raw?.totalSpent) }}</div><div class="crm2-stat-mini__label">Chi tiêu</div></div>
            </div>
            <div class="crm2-detail-rows" style="margin-top:16px;text-align:left">
              <div class="crm2-detail-row"><span class="crm2-dk">SĐT</span><span class="crm2-dv crm2-mono">{{ inactiveDetail.customer.phone||'—' }}</span></div>
              <div class="crm2-detail-row"><span class="crm2-dk">Ngày sinh</span><span class="crm2-dv">{{ inactiveDetail.customer.birthDate||'—' }}</span></div>
              <div class="crm2-detail-row"><span class="crm2-dk">Địa chỉ</span><span class="crm2-dv">{{ inactiveDetail.customer.raw?.address||'—' }}</span></div>
              <div class="crm2-detail-row"><span class="crm2-dk">Đơn hàng cuối</span><span class="crm2-dv" style="color:var(--c-orange)">{{ relativeDate(inactiveDetail.customer.raw?.lastOrderAt) }}</span></div>
              <div class="crm2-detail-row" v-if="inactiveDetail.customer.notes"><span class="crm2-dk">Ghi chú</span><span class="crm2-dv">{{ inactiveDetail.customer.notes }}</span></div>
            </div>
            <div class="crm2-dialog__notice crm2-dialog__notice--red" style="margin-top:14px;text-align:left">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              Không có giao dịch trong <strong>{{ activeInactiveTab }}+ ngày</strong>. Cân nhắc liên hệ lại.
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ══ DIALOG: Zero Order Send ══ -->
    <Teleport to="body">
      <div class="crm2-overlay" v-if="zoDlg.open" @click.self="zoDlg.open=false">
        <div class="crm2-dialog crm2-dialog--lg">
          <div class="crm2-dialog__band crm2-dialog__band--blue">
            <div class="crm2-dialog__band-circles"><span class="crm2-band-circle crm2-band-circle--1"/><span class="crm2-band-circle crm2-band-circle--2"/><span class="crm2-band-circle crm2-band-circle--3"/></div>
            <div class="crm2-dialog__icon-wrap"><div class="crm2-dialog__icon-ring">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
            </div></div>
          </div>
          <div class="crm2-dialog__body">
            <div class="crm2-dialog__badge crm2-dialog__badge--blue">Gửi thông báo chào mừng</div>
            <div class="crm2-dialog__head-row">
              <h2 class="crm2-dialog__title">📨 Gửi thông báo<span class="crm2-tag crm2-tag--blue" style="font-size:12px;margin-left:10px">{{ zoSelected.length }} khách</span></h2>
              <button class="crm2-icon-btn" @click="zoDlg.open=false"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
            </div>

            <div class="crm2-field">
              <label class="crm2-field__label">Mẫu thông báo</label>
              <div class="crm2-zo-templates">
                <div v-for="tpl in zoTemplates" :key="tpl.id" class="crm2-zo-tpl" :class="{'crm2-zo-tpl--active': zoDlg.form.templateId === tpl.id}" @click="zoSelectTemplate(tpl)">
                  <div class="crm2-zo-tpl__icon">{{ tpl.icon }}</div>
                  <div class="crm2-zo-tpl__name">{{ tpl.name }}</div>
                  <div class="crm2-zo-tpl__desc">{{ tpl.desc }}</div>
                </div>
              </div>
            </div>

            <div class="crm2-field"><label class="crm2-field__label">Tiêu đề thông báo</label><input v-model="zoDlg.form.title" class="crm2-input" placeholder="Nhập tiêu đề…" /></div>
            <div class="crm2-field">
              <label class="crm2-field__label">Nội dung</label>
              <textarea v-model="zoDlg.form.message" class="crm2-input crm2-textarea" rows="5" placeholder="Nhập nội dung…"></textarea>
              <span style="font-size:11px;color:var(--c-muted);margin-top:4px;display:block">Dùng <code class="crm2-mono" style="background:var(--c-border-light);padding:1px 6px;border-radius:4px">{name}</code> để chèn tên khách</span>
            </div>

            <div class="crm2-zo-preview">
              <div class="crm2-zo-preview__label">👁 Xem trước</div>
              <div class="crm2-zo-preview__card">
                <span style="font-size:22px;flex-shrink:0">{{ zoDlg.currentTpl?.icon || '📢' }}</span>
                <div>
                  <div style="font-size:13px;font-weight:700;color:var(--c-text);margin-bottom:4px">{{ zoDlg.form.title || '(Chưa có tiêu đề)' }}</div>
                  <div style="font-size:12px;color:var(--c-muted);white-space:pre-line;line-height:1.5">{{ (zoDlg.form.message || '(Chưa có nội dung)').replace(/\{name\}/g, 'Nguyễn Văn A') }}</div>
                </div>
              </div>
            </div>

            <!-- Progress -->
            <div v-if="zoSending" class="crm2-zo-progress">
              <div class="crm2-zo-progress__fill" :style="{ width: `${Math.round((zoSentCount / zoSelected.length) * 100)}%` }"></div>
            </div>

            <div class="crm2-dialog__actions">
              <button class="crm2-btn crm2-btn--ghost" @click="zoDlg.open=false" :disabled="zoSending">Hủy bỏ</button>
              <button class="crm2-btn crm2-btn--confirm-blue" :class="{'crm2-btn--loading': zoSending}" :disabled="!zoDlg.form.title || !zoDlg.form.message || zoSending" @click="zoSendBulk">
                <span v-if="zoSending" class="crm2-spinner crm2-spinner--white"></span>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                {{ zoSending ? `Đang gửi (${zoSentCount}/${zoSelected.length})…` : `Gửi cho ${zoSelected.length} khách` }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";
import http from "../../api/http";

const mainTab = ref("customers");

function reloadCurrentTab() {
  if (mainTab.value === 'customers') load();
  else if (mainTab.value === 'birthdays') loadBirthdayData();
  else if (mainTab.value === 'inactive') loadInactiveAll();
  else if (mainTab.value === 'zeroorder') loadZeroOrderData();
}

const loading = ref(false);
const rows = ref([]);
const q = ref("");
const typeFilter = ref("");
const vipTierFilter = ref("");
const activityFilter = ref("");
const pointsMin = ref(null);
const pointsMax = ref(null);
const appliedPointsMin = ref(null);
const appliedPointsMax = ref(null);
const page = ref(1);
const pageSize = 10;
const vipNoteTextarea = ref(null);

const summaryDlg = reactive({ open: false, loading: false, mode: 'monthly', range: 6, view: 'overview', data: [] });
const adminSummaryTotals = computed(() => {
  const earned = summaryDlg.data.reduce((s, r) => s + (r.totalPointsEarned || 0), 0);
  const deducted = summaryDlg.data.reduce((s, r) => s + (r.totalPointsDeducted || 0), 0);
  const transactions = summaryDlg.data.reduce((s, r) => s + (r.totalTransactions || 0), 0);
  return { earned, deducted, net: earned - deducted, transactions };
});
function openSummaryDialog() { summaryDlg.open = true; if (!summaryDlg.data.length) loadAdminSummary(); }
async function loadAdminSummary() {
  summaryDlg.loading = true;
  try {
    const res = summaryDlg.mode === 'weekly' ? await customersApi.getLoyaltyWeeklySummaryAdmin(summaryDlg.range) : await customersApi.getLoyaltyMonthlySummaryAdmin(summaryDlg.range);
    const raw = res?.data;
    summaryDlg.data = Array.isArray(raw) ? raw : Array.isArray(raw?.data) ? raw.data : [];
  } catch { toast('Error loading summary', 'error'); summaryDlg.data = []; }
  finally { summaryDlg.loading = false; }
}
function getAdminBarWidth(value, type) {
  if (!summaryDlg.data.length) return 0;
  const maxVal = Math.max(...summaryDlg.data.map(r => type === 'earn' ? r.totalPointsEarned : r.totalPointsDeducted));
  return maxVal === 0 ? 0 : Math.round((value / maxVal) * 100);
}
function formatAdminDate(d) { if (!d) return '—'; return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }); }

const dlg = reactive({ open: false, mode: "create", loading: false, alert: "", id: null, form: { fullName: "", email: "", phone: "", birthDate: "", customerType: "REGULAR", address: "", notes: "", vipNote: "", vipTier: "" } });
const detailsDialog = reactive({ open: false, customer: null });
const vipNoteDialog = reactive({ open: false, loading: false, customerId: null, customerName: "", vipTier: "", note: "" });
const vipSuggestions = ["Prefers gaming high-end", "No display units", "Morning delivery preferred", "Corporate, needs VAT", "Slow to respond", "Top spender – prioritize"];

const hasActiveFilters = computed(() => !!(typeFilter.value || vipTierFilter.value || activityFilter.value || isPointsFilterApplied.value));
const isPointsFilterApplied = computed(() => appliedPointsMin.value !== null || appliedPointsMax.value !== null);
const canApplyPointsFilter = computed(() => pointsMin.value !== null || pointsMax.value !== null);
const vipCount = computed(() => rows.value.filter(r => r.customerType === 'VIP').length);
const totalPoints = computed(() => rows.value.reduce((s, r) => s + (r.loyaltyPoints || 0), 0));
const avgPoints = computed(() => !rows.value.length ? 0 : Math.round(totalPoints.value / rows.value.length));
const filtered = computed(() => { const kw = (q.value || "").trim().toLowerCase(); if (!kw) return rows.value; return rows.value.filter(r => `${r.fullName} ${r.email} ${r.phone}`.toLowerCase().includes(kw)); });
const pageCount = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)));
const paged = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize));

const birthdayTab = ref("today");
const selectedMonth = ref(new Date().getMonth() + 1);
const todayBirthdays = ref([]);
const monthlyBirthdays = ref([]);
const upcomingBirthdays = ref([]);
const monthlyStats = ref({});
const notificationHistory = ref([]);
const historyLoading = ref(false);
const greetingDialog = reactive({ open: false, customer: null, sending: false, form: { message: "" } });
const monthOptions = [
  {value:1,label:"January"},{value:2,label:"February"},{value:3,label:"March"},{value:4,label:"April"},
  {value:5,label:"May"},{value:6,label:"June"},{value:7,label:"July"},{value:8,label:"August"},
  {value:9,label:"September"},{value:10,label:"October"},{value:11,label:"November"},{value:12,label:"December"},
];
const statsTableData = computed(() => monthOptions.map(m => ({ month: m.value, monthLabel: m.label, count: monthlyStats.value[m.value] || 0 })));
const maxBdCount = computed(() => Math.max(...Object.values(monthlyStats.value || {}), 1));
function getBdBarPct(count) { return (count / maxBdCount.value) * 100; }

const inactiveData = reactive({ 30: [], 60: [], 90: [] });
const inactiveCounts = reactive({ 30: null, 60: null, 90: null });
const activeInactiveTab = ref(30);
const inactiveQ = ref("");
const inactiveTierFilter = ref("");
const inactiveSortBy = ref("spent");
const inactivePage = ref(1);
const inactivePageSize = 9;
const inactiveDetail = reactive({ open: false, customer: null });
const inactiveTabs = [{ days: 30, label: "30 days" }, { days: 60, label: "60 days" }, { days: 90, label: "90 days" }];
const tiers = ["BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND"];
const currentInactive = computed(() => inactiveData[activeInactiveTab.value] || []);
const inactiveFiltered = computed(() => {
  let list = currentInactive.value;
  if (inactiveTierFilter.value) list = list.filter(r => r.raw?.vipTier === inactiveTierFilter.value);
  const kw = inactiveQ.value.trim().toLowerCase();
  if (kw) list = list.filter(r => `${r.fullName} ${r.email} ${r.phone}`.toLowerCase().includes(kw));
  return [...list].sort((a, b) => {
    if (inactiveSortBy.value === "spent") return (b.raw?.totalSpent || 0) - (a.raw?.totalSpent || 0);
    if (inactiveSortBy.value === "points") return (b.loyaltyPoints || 0) - (a.loyaltyPoints || 0);
    return a.fullName.localeCompare(b.fullName);
  });
});
const inactivePaged = computed(() => { const s = (inactivePage.value - 1) * inactivePageSize; return inactiveFiltered.value.slice(s, s + inactivePageSize); });
const inactiveTotalPages = computed(() => Math.ceil(inactiveFiltered.value.length / inactivePageSize));
const inactiveTotalPoints = computed(() => currentInactive.value.reduce((s, r) => s + (r.loyaltyPoints || 0), 0).toLocaleString());
const inactiveTotalSpent = computed(() => currentInactive.value.reduce((s, r) => s + parseFloat(r.raw?.totalSpent || 0), 0));
const inactiveTopTier = computed(() => { const order = ["DIAMOND","PLATINUM","GOLD","SILVER","BRONZE"]; for (const t of order) { if (currentInactive.value.some(r => r.raw?.vipTier === t)) return t; } return "—"; });

function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  const root = payload?.data ?? payload;
  if (Array.isArray(root)) return root;
  for (const k of ["content","items","results","rows","list"]) { if (Array.isArray(root?.[k])) return root[k]; if (Array.isArray(root?.data?.[k])) return root.data[k]; }
  return [];
}
function normalize(list) {
  return (list || []).map(c => ({ id: c?.id ?? c?.customerId, fullName: c?.fullName ?? c?.name ?? "", email: c?.email ?? "", phone: c?.phone ?? "", birthDate: c?.birthDate ?? c?.dateOfBirth ?? "", customerType: (c?.customerType ?? "REGULAR").toString().toUpperCase(), loyaltyPoints: c?.loyaltyPoints ?? 0, address: c?.address ?? "", notes: c?.notes ?? "", vipNote: c?.vipNote ?? "", raw: c }));
}
function tierIcon(tier) { return { BRONZE: 'B', SILVER: 'S', GOLD: 'G', PLATINUM: 'P', DIAMOND: 'D' }[tier] || ''; }
function getTierColor(tier) { return { BRONZE: '#cd7f32', SILVER: '#94a3b8', GOLD: '#f0b429', PLATINUM: '#818cf8', DIAMOND: '#34d399' }[tier] || '#4b5563'; }
function getPointsProgress(customer) {
  const map = { BRONZE: [0,500], SILVER: [500,1500], GOLD: [1500,3000], PLATINUM: [3000,6000], DIAMOND: [6000,10000] };
  const r = map[customer.vipTier]; if (!r) return 0;
  return Math.min(100, Math.max(0, ((customer.loyaltyPoints - r[0]) / (r[1] - r[0])) * 100));
}
function getInitials(name) {
  if (!name) return '?';
  const p = name.trim().split(' ');
  return p.length === 1 ? p[0].substring(0, 2).toUpperCase() : (p[0][0] + p[p.length - 1][0]).toUpperCase();
}
function getAvatarColor(name) {
  const colors = ['#1e40af','#065f46','#7c2d12','#581c87','#0e7490','#a21caf','#b45309','#0f766e','#1d4ed8','#be123c'];
  const hash = (name || '').split('').reduce((a, c) => a + c.charCodeAt(0), 0);
  return colors[hash % colors.length];
}
function formatCurrency(v) { if (!v) return '0 ₫'; return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v); }
function formatCurrencyShort(v) {
  if (!v || v === 0) return '—';
  if (v >= 1_000_000_000) return (v / 1_000_000_000).toFixed(1) + 'B';
  if (v >= 1_000_000) return (v / 1_000_000).toFixed(1) + 'M';
  if (v >= 1_000) return (v / 1_000).toFixed(0) + 'K';
  return v + '₫';
}
const zoCustomers   = ref([]);
const zoStats       = ref({});
const zeroOrderStats = zoStats;
const zoSelected    = ref([]);
const zoSendingIds  = ref(new Set());
const zoSending     = ref(false);
const zoSentCount   = ref(0);
const zoSearch      = ref("");
const zoSelectedDays = ref(3);
const zoPage        = ref(1);

const zoDayOptions = [
  { value: 1,  label: "≥ 1 ngày" },
  { value: 3,  label: "≥ 3 ngày" },
  { value: 7,  label: "≥ 7 ngày" },
  { value: 14, label: "≥ 14 ngày" },
  { value: 30, label: "≥ 30 ngày" },
];

const zoTemplates = [
  { id: "welcome", icon: "🎉", name: "Chào mừng", desc: "Mời mua lần đầu với ưu đãi", title: "🎉 Chào mừng bạn đến với cửa hàng!", message: `Xin chào {name}! 👋\n\nCảm ơn bạn đã đăng ký tài khoản.\n\n🛍️ Đặt đơn hàng đầu tiên ngay để nhận ưu đãi đặc biệt dành cho khách mới!` },
  { id: "offer", icon: "💰", name: "Ưu đãi hấp dẫn", desc: "Voucher giảm giá kèm deadline", title: "💰 Ưu đãi đặc biệt chỉ dành riêng cho bạn!", message: `Xin chào {name}!\n\nBạn chưa thực hiện đơn hàng nào.\n\n🎁 Chúng tôi tặng bạn ưu đãi GIẢM GIÁ ĐẶC BIỆT cho lần mua đầu tiên!\n\n⏰ Ưu đãi có hạn — Đặt hàng ngay!` },
  { id: "nudge", icon: "🤝", name: "Hỗ trợ tư vấn", desc: "Mời chat với tư vấn viên", title: "🤝 Chúng tôi sẵn sàng hỗ trợ bạn!", message: `Xin chào {name}!\n\nBạn đang phân vân chọn sản phẩm?\n\n💬 Đội ngũ tư vấn luôn sẵn sàng giúp bạn.\n✅ Tư vấn miễn phí · Bảo hành chính hãng · Giao hàng nhanh` },
  { id: "custom", icon: "✏️", name: "Tùy chỉnh", desc: "Soạn nội dung riêng", title: "", message: "" },
];

const zoDlg = reactive({
  open: false,
  form: { templateId: "welcome", title: "", message: "" },
  get currentTpl() { return zoTemplates.find(t => t.id === this.form.templateId); },
});

const zoFiltered = computed(() => {
  const kw = zoSearch.value.trim().toLowerCase();
  if (!kw) return zoCustomers.value;
  return zoCustomers.value.filter(c => `${c.name} ${c.email} ${c.phone ?? ""}`.toLowerCase().includes(kw));
});
const zoPageCount  = computed(() => Math.max(1, Math.ceil(zoFiltered.value.length / pageSize)));
const zoPaged      = computed(() => zoFiltered.value.slice((zoPage.value - 1) * pageSize, zoPage.value * pageSize));
const zoIsAllSelected = computed(() => zoFiltered.value.length > 0 && zoFiltered.value.every(c => zoSelected.value.includes(c.id)));

function zoDaysSince(dateStr) { if (!dateStr) return 0; return Math.floor((Date.now() - new Date(dateStr).getTime()) / 86400000); }
function formatDate(d) { if (!d) return "—"; return new Date(d).toLocaleDateString("vi-VN"); }
function zoStatusLabel(c) {
  const d = zoDaysSince(c.createdAt);
  if (d >= 30) return "Nguy cơ cao 🚨";
  if (d >= 7)  return "Cần tư vấn 📞";
  return "Mới đăng ký 🌱";
}

function zoSelectDays(d) { zoSelectedDays.value = d; zoPage.value = 1; zoSelected.value = []; loadZeroOrderData(); }
function zoSelectAll() { if (zoIsAllSelected.value) { zoSelected.value = []; } else { zoSelected.value = zoFiltered.value.map(c => c.id); } }
function zoSelectTemplate(tpl) { zoDlg.form.templateId = tpl.id; if (tpl.id !== "custom") { zoDlg.form.title = tpl.title; zoDlg.form.message = tpl.message; } }
function openZoSendDialog() { zoSelectTemplate(zoTemplates[0]); zoDlg.open = true; }

async function loadZeroOrderData() {
  loading.value = true; zoSelected.value = [];
  try { const res = await customersApi.listZeroOrder(zoSelectedDays.value); zoCustomers.value = Array.isArray(res.data) ? res.data : (res.data?.data ?? []); }
  catch { toast("Không thể tải danh sách chưa mua", "error"); zoCustomers.value = []; }
  finally { loading.value = false; }
}
async function loadZeroOrderStats() {
  try { const res = await customersApi.getZeroOrderStats(); zoStats.value = res.data ?? {}; } catch { /* silent */ }
}

async function zoSendSingle(customer) {
  const s = new Set(zoSendingIds.value); s.add(customer.id); zoSendingIds.value = s;
  try {
    await http.post("/api/auth/notifications/send", { customerIds: [customer.id], title: `🎉 Xin chào ${customer.name}!`, message: zoTemplates[0].message.replace(/\{name\}/g, customer.name), type: "WELCOME" });
    toast(`Đã gửi tới ${customer.name}`, "success");
  } catch (e) { toast(e?.response?.data?.message || "Gửi thất bại", "error"); }
  finally { const s2 = new Set(zoSendingIds.value); s2.delete(customer.id); zoSendingIds.value = s2; }
}

async function zoSendBulk() {
  if (!zoDlg.form.title || !zoDlg.form.message) return;
  zoSending.value = true; zoSentCount.value = 0;
  const ids = [...zoSelected.value]; let success = 0, fail = 0;
  for (let i = 0; i < ids.length; i += 10) {
    const chunk = ids.slice(i, i + 10);
    const chunkCustomers = zoCustomers.value.filter(c => chunk.includes(c.id));
    await Promise.all(chunkCustomers.map(async c => {
      try { await http.post("/api/auth/notifications/send", { customerIds: [c.id], title: zoDlg.form.title, message: zoDlg.form.message.replace(/\{name\}/g, c.name), type: "WELCOME" }); success++; }
      catch { fail++; }
      finally { zoSentCount.value++; }
    }));
  }
  zoSending.value = false; zoDlg.open = false; zoSelected.value = [];
  toast(fail === 0 ? `✅ Đã gửi thành công ${success} thông báo!` : `⚠️ Thành công: ${success} | Thất bại: ${fail}`, fail === 0 ? "success" : "error");
  await loadZeroOrderData();
}
function formatMoneyShort(val) {
  if (!val) return "0"; const n = parseFloat(val);
  if (n >= 1e9) return (n / 1e9).toFixed(1) + "B"; if (n >= 1e6) return (n / 1e6).toFixed(1) + "M"; if (n >= 1e3) return (n / 1e3).toFixed(0) + "K";
  return n.toLocaleString();
}
function relativeDate(iso) {
  if (!iso) return "Never";
  const diff = Math.floor((Date.now() - new Date(iso).getTime()) / 86400000);
  if (diff === 0) return "Today"; if (diff === 1) return "Yesterday"; if (diff < 30) return `${diff}d ago`;
  if (diff < 365) return `${Math.floor(diff / 30)}mo ago`; return `${Math.floor(diff / 365)}yr ago`;
}
function formatBirthdayDate(customer) {
  if (customer.isBirthdayToday) return "Today";
  return `${customer.birthDay} ${monthOptions[customer.birthMonth - 1]?.label} (${customer.daysUntilBirthday}d)`;
}
function formatDateTime(dateString) { if (!dateString) return '—'; return new Date(dateString).toLocaleString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }); }

async function load() {
  loading.value = true;
  try {
    let res;
    if (isPointsFilterApplied.value && vipTierFilter.value) res = await customersApi.listByVipTierAndPoints(vipTierFilter.value, appliedPointsMin.value || 0, appliedPointsMax.value || 999999);
    else if (isPointsFilterApplied.value) res = await customersApi.listByPointsRange(appliedPointsMin.value || 0, appliedPointsMax.value || 999999);
    else if (vipTierFilter.value) res = await customersApi.listByVipTier(vipTierFilter.value);
    else if (activityFilter.value === 'ACTIVE_30') res = await customersApi.listActiveLast30Days();
    else if (typeFilter.value) res = await customersApi.listByType(typeFilter.value);
    else res = await customersApi.listAll();
    rows.value = normalize(extractList(res?.data)); page.value = 1;
  } catch (e) { rows.value = []; toast("Failed to load customers", "error"); }
  finally { loading.value = false; }
}
function handleFilterChange() {
  if (activityFilter.value || typeFilter.value || vipTierFilter.value) { appliedPointsMin.value = null; appliedPointsMax.value = null; pointsMin.value = null; pointsMax.value = null; }
  load();
}
function applyPointsFilter() {
  if (!canApplyPointsFilter.value) { toast("Enter at least one points value", "warning"); return; }
  if (pointsMin.value !== null && pointsMax.value !== null && pointsMin.value > pointsMax.value) { toast("Min cannot exceed max", "error"); return; }
  appliedPointsMin.value = pointsMin.value; appliedPointsMax.value = pointsMax.value; activityFilter.value = ""; typeFilter.value = ""; load();
}
function clearPointsFilter() { pointsMin.value = null; pointsMax.value = null; appliedPointsMin.value = null; appliedPointsMax.value = null; load(); }
function clearAllFilters() { typeFilter.value = ""; vipTierFilter.value = ""; activityFilter.value = ""; pointsMin.value = null; pointsMax.value = null; appliedPointsMin.value = null; appliedPointsMax.value = null; load(); }
function openCreate() { dlg.open = true; dlg.mode = "create"; dlg.alert = ""; dlg.id = null; dlg.form = { fullName: "", email: "", phone: "", birthDate: "", customerType: "REGULAR", address: "", notes: "", vipNote: "", vipTier: "" }; }
function openEdit(row) { dlg.open = true; dlg.mode = "edit"; dlg.alert = ""; dlg.id = row?.id; dlg.form = { fullName: row?.fullName||"", email: row?.email||"", phone: row?.phone||"", birthDate: row?.birthDate||"", customerType: row?.customerType||"REGULAR", address: row?.address||"", notes: row?.notes||"", vipNote: row?.raw?.vipNote||"", vipTier: row?.raw?.vipTier||"" }; detailsDialog.open = false; }
async function save() {
  dlg.alert = "";
  if (!dlg.form.fullName || !dlg.form.email || !dlg.form.phone) { dlg.alert = "Name, email and phone are required."; return; }
  dlg.loading = true;
  try {
    const payload = { ...dlg.form }; delete payload.vipTier;
    if (dlg.mode === "create") { await customersApi.create(payload); toast("Customer created!", "success"); }
    else { await customersApi.update(dlg.id, payload); toast("Updated!", "success"); }
    dlg.open = false; await load();
  } catch (e) { const msg = e?.response?.data?.message || e?.message || "Save failed"; dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg); }
  finally { dlg.loading = false; }
}
async function remove(row) {
  const ok = await confirmModal(`Delete "${row?.fullName}"?`, "Confirm Delete", "Delete", true);
  if (!ok) return;
  try { await customersApi.remove(row.id); toast("Deleted!", "success"); await load(); }
  catch { toast("Delete failed.", "error"); }
}
function viewDetails(row) { detailsDialog.customer = row; detailsDialog.open = true; }
function openVipNoteEdit(row) {
  if (!row.raw?.vipTier) return;
  vipNoteDialog.customerId = row.id; vipNoteDialog.customerName = row.fullName; vipNoteDialog.vipTier = row.raw.vipTier; vipNoteDialog.note = row.raw?.vipNote || ""; vipNoteDialog.open = true; nextTick(() => vipNoteTextarea.value?.focus());
}
async function saveVipNote() {
  vipNoteDialog.loading = true;
  try {
    await customersApi.updateVipNote(vipNoteDialog.customerId, vipNoteDialog.note); toast("VIP Note saved.", "success"); vipNoteDialog.open = false; await load();
    if (detailsDialog.open && detailsDialog.customer?.id === vipNoteDialog.customerId) { detailsDialog.customer.raw.vipNote = vipNoteDialog.note; }
  } catch { toast("Failed to save VIP Note", "error"); }
  finally { vipNoteDialog.loading = false; }
}
async function loadBirthdayData() {
  loading.value = true;
  try {
    await Promise.all([
      http.get("/api/auth/admin/birthdays/today").then(r => { todayBirthdays.value = r.data || []; }),
      http.get("/api/auth/admin/birthdays/statistics").then(r => { monthlyStats.value = r.data?.monthlyCount || {}; }),
      http.get("/api/auth/admin/birthdays/upcoming?days=7").then(r => { upcomingBirthdays.value = r.data || []; }),
    ]);
  } catch { toast("Failed to load birthday data", "error"); }
  finally { loading.value = false; }
}
async function loadMonthlyBirthdays() {
  loading.value = true;
  try { const res = await http.get(`/api/auth/admin/birthdays/month/${selectedMonth.value}`); monthlyBirthdays.value = res.data || []; }
  catch { toast("Failed to load monthly data", "error"); }
  finally { loading.value = false; }
}
async function loadNotificationHistory() {
  historyLoading.value = true;
  try { const res = await http.get("/api/auth/admin/birthdays/notification-history"); notificationHistory.value = res.data || []; }
  catch { toast("Failed to load history", "error"); }
  finally { historyLoading.value = false; }
}
function viewMonthDetail(month) { selectedMonth.value = parseInt(month); birthdayTab.value = "monthly"; loadMonthlyBirthdays(); }
function sendGreeting(customer) {
  greetingDialog.open = true; greetingDialog.customer = customer;
  greetingDialog.form.message = `Happy Birthday, ${customer.name}!\n\nWishing you a wonderful year ahead. Thank you for your continued trust and loyalty.\n\nAs a special gift, enjoy an exclusive birthday voucher with great offers!`;
}
async function confirmSendGreeting() {
  if (!greetingDialog.form.message.trim()) { toast("Please enter a message", "warning"); return; }
  greetingDialog.sending = true;
  try {
    const response = await http.post(`/api/auth/admin/birthdays/send-greeting/${greetingDialog.customer.id}`, { message: greetingDialog.form.message });
    if (response.data.status === 'success') { toast(`Greeting sent to ${greetingDialog.customer.name}`, "success"); greetingDialog.open = false; if (birthdayTab.value === 'history') loadNotificationHistory(); }
    else { toast(response.data.message || "Could not send", "error"); }
  } catch (e) { toast(e.response?.data?.message || "Could not send greeting", "error"); }
  finally { greetingDialog.sending = false; }
}
function normalizeInactive(list) {
  return (list || []).map(c => ({ id: c.id, fullName: c.name ?? c.fullName ?? "", email: c.email ?? "", phone: c.phone ?? "", birthDate: c.dateOfBirth ?? c.birthDate ?? "", customerType: c.customerType ?? "REGULAR", loyaltyPoints: c.loyaltyPoints ?? 0, notes: c.notes ?? "", raw: c }));
}
async function fetchInactiveFor(days) {
  try {
    const res = await customersApi.listInactiveTransaction(days);
    const arr = Array.isArray(res?.data) ? res.data : (res?.data?.content ?? []);
    inactiveData[days] = normalizeInactive(arr); inactiveCounts[days] = inactiveData[days].length;
  } catch { inactiveData[days] = []; inactiveCounts[days] = 0; }
}
async function loadInactiveAll() { loading.value = true; await Promise.all([fetchInactiveFor(30), fetchInactiveFor(60), fetchInactiveFor(90)]); loading.value = false; }
function switchInactiveTab(days) { activeInactiveTab.value = days; inactivePage.value = 1; inactiveQ.value = ""; inactiveTierFilter.value = ""; }
function openInactiveDetail(row) { inactiveDetail.customer = row; inactiveDetail.open = true; }
onMounted(load);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/*
  HARDCODE MAP (không dùng var()):
  bg:#f6f7f9  card:#ffffff  border:#e4e7ec  border-light:#f0f2f5
  text:#0f1117  muted:#6b7280  subtle:#9ca3af
  blue:#2563eb  blue-bg:#eff6ff  blue-border:#bfdbfe
  green:#16a34a  green-bg:#f0fdf4  green-border:#bbf7d0
  red:#dc2626    red-bg:#fff1f2   red-border:#fecdd3
  orange:#d97706 orange-bg:#fffbeb orange-border:#fde68a
  purple:#7c3aed purple-bg:#f5f3ff purple-border:#ddd6fe
  gray-bg:#f3f4f6  gray-border:#e5e7eb
  radius:12px  radius-sm:8px
  shadow-sm: 0 1px 3px rgba(0,0,0,.06), 0 1px 2px rgba(0,0,0,.04)
  shadow-md: 0 4px 16px rgba(0,0,0,.08), 0 2px 6px rgba(0,0,0,.04)
  shadow-lg: 0 24px 64px rgba(0,0,0,.14), 0 8px 24px rgba(0,0,0,.06)
*/

.crm2-page {
  font-family: "Plus Jakarta Sans", sans-serif;
  background: #f6f7f9;
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: #0f1117;
  box-sizing: border-box;
}
.crm2-inner { max-width: 1440px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }

/* ── Header ──────────────────────────────── */
.crm2-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; flex-wrap: wrap; }
.crm2-eyebrow { display: inline-flex; align-items: center; gap: 5px; font-size: 11px; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; color: #6b7280; margin-bottom: 6px; }
.crm2-title { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; margin: 0 0 4px; line-height: 1.2; }
.crm2-title__accent { color: #2563eb; }
.crm2-subtitle { font-size: 13.5px; color: #6b7280; margin: 0; }
.crm2-header__actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding-top: 6px; }

/* ── Tab bar ─────────────────────────────── */
.crm2-tabbar {
  display: flex; gap: 2px; background: #ffffff;
  border: 1px solid #e4e7ec; border-radius: 12px; padding: 5px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  overflow-x: auto;
}
.crm2-tab {
  display: inline-flex; align-items: center; gap: 7px; padding: 8px 16px;
  font-size: 13px; font-weight: 600; border: none; border-radius: 8px;
  background: transparent; color: #6b7280; cursor: pointer;
  transition: all 0.15s; white-space: nowrap; font-family: "Plus Jakarta Sans", sans-serif;
}
.crm2-tab:hover { background: #f0f2f5; color: #0f1117; }
.crm2-tab--active { background: #2563eb; color: #fff; box-shadow: 0 2px 8px rgba(37,99,235,0.25); }

/* ── Stats grid ──────────────────────────── */
.crm2-stats4 { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 14px; }
.crm2-stat {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 8px; padding: 16px;
  display: flex; flex-direction: column; gap: 6px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  transition: transform 0.15s;
}
.crm2-stat:hover { transform: translateY(-1px); }
.crm2-stat--blue   { border-top: 3px solid #2563eb; }
.crm2-stat--green  { border-top: 3px solid #16a34a; }
.crm2-stat--orange { border-top: 3px solid #d97706; }
.crm2-stat--red    { border-top: 3px solid #dc2626; }
.crm2-stat--purple { border-top: 3px solid #7c3aed; }
.crm2-stat--dim    { border-top: 3px solid #e4e7ec; }
.crm2-stat__num { font-size: 24px; font-weight: 800; letter-spacing: -0.03em; line-height: 1; }
.crm2-stat--blue   .crm2-stat__num { color: #2563eb; }
.crm2-stat--green  .crm2-stat__num { color: #16a34a; }
.crm2-stat--orange .crm2-stat__num { color: #d97706; }
.crm2-stat--red    .crm2-stat__num { color: #dc2626; }
.crm2-stat--purple .crm2-stat__num { color: #7c3aed; }
.crm2-stat__label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: #6b7280; }

/* ── Filter panel ────────────────────────── */
.crm2-filter-panel {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}
.crm2-filter-panel__head { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px; border-bottom: 1px solid #f0f2f5; }
.crm2-filter-panel__title { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 500; color: #0f1117; }
.crm2-filter-badge { background: #eff6ff; color: #2563eb; font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 999px; }
.crm2-filter-clear { font-size: 12px; color: #6b7280; background: none; border: none; cursor: pointer; padding: 0; transition: color 0.15s; font-family: "Plus Jakarta Sans", sans-serif; }
.crm2-filter-clear:hover { color: #dc2626; }
.crm2-filter-clear:disabled { opacity: 0.4; pointer-events: none; }
.crm2-filter-fields { display: grid; grid-template-columns: repeat(4, 1fr); }
.crm2-filter-field { padding: 12px 16px; border-right: 1px solid #f0f2f5; display: flex; flex-direction: column; gap: 6px; }
.crm2-filter-field--actions { flex-direction: row; align-items: flex-end; gap: 8px; }
.crm2-filter-field__label { font-size: 11px; font-weight: 700; color: #6b7280; text-transform: uppercase; letter-spacing: 0.05em; }
.crm2-filter-divider { height: 1px; background: #f0f2f5; }
.crm2-filter-points { display: grid; grid-template-columns: 1fr 1fr auto; }
.crm2-filter-bar { display: flex; gap: 10px; align-items: center; }
.crm2-active-filters { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding: 10px 16px; border-top: 1px solid #f0f2f5; }
.crm2-af-label { font-size: 10px; font-weight: 700; letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af; }
.crm2-filter-chip {
  display: inline-flex; align-items: center; gap: 4px;
  background: #eff6ff; border: 1px solid #bfdbfe; color: #2563eb;
  padding: 3px 8px 3px 10px; border-radius: 20px; font-size: 11.5px; font-weight: 600;
}
.crm2-chip-remove { background: none; border: none; color: #2563eb; cursor: pointer; font-size: 14px; padding: 0 2px; line-height: 1; opacity: 0.6; }
.crm2-chip-remove:hover { opacity: 1; }

/* ── Card ────────────────────────────────── */
.crm2-card {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  overflow: hidden;
}

/* ── Toolbar ─────────────────────────────── */
.crm2-toolbar { display: flex; align-items: center; justify-content: space-between; padding: 13px 20px; border-bottom: 1px solid #f0f2f5; background: #fafbfc; }
.crm2-toolbar__left { display: flex; align-items: center; gap: 10px; }
.crm2-count { display: flex; align-items: baseline; gap: 4px; }
.crm2-count__num { font-size: 18px; font-weight: 800; }
.crm2-count__label { font-size: 12px; color: #9ca3af; font-weight: 600; }
.crm2-count-badge { background: #fff; color: #2563eb; border-radius: 20px; padding: 1px 7px; font-size: 11px; font-weight: 800; margin-left: 4px; }

/* ── Table ───────────────────────────────── */
.crm2-table-wrap { overflow-x: auto; position: relative; min-height: 100px; }
.crm2-table-wrap--loading { pointer-events: none; }
.crm2-loader-overlay { position: absolute; inset: 0; background: rgba(255,255,255,0.75); backdrop-filter: blur(2px); display: flex; align-items: center; justify-content: center; z-index: 10; }
.crm2-loader-ring { width: 36px; height: 36px; border: 3px solid #e4e7ec; border-top-color: #2563eb; border-radius: 50%; animation: crm2-spin 0.7s linear infinite; }
.crm2-loading-center { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 48px; color: #6b7280; font-size: 13.5px; }

.crm2-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.crm2-table thead tr { border-bottom: 2px solid #f0f2f5; }
.crm2-table th { padding: 11px 14px; text-align: left; font-size: 10.5px; font-weight: 700; letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af; background: #fafbfc; white-space: nowrap; }
.crm2-table td { padding: 12px 14px; vertical-align: middle; border-bottom: 1px solid #f0f2f5; }
.crm2-row { transition: background 0.12s; }
.crm2-row:hover { background: #fafbfc; }
.crm2-row:last-child td { border-bottom: none; }
.crm2-row--selected { background: #eff6ff !important; }
.crm2-td-truncate { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Cell types */
.crm2-mono-id { font-family: "JetBrains Mono", monospace; font-size: 12px; font-weight: 600; color: #2563eb; background: #eff6ff; padding: 2px 7px; border-radius: 5px; }
.crm2-mono { font-family: "JetBrains Mono", monospace; font-size: 12px; }
.crm2-text--muted  { color: #6b7280; }
.crm2-text--blue   { color: #2563eb; }
.crm2-text--green  { color: #16a34a; }
.crm2-text--orange { color: #d97706; }
.crm2-text--red    { color: #dc2626; }

.crm2-customer-cell { display: flex; align-items: center; gap: 10px; }
.crm2-avatar { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: rgba(255,255,255,0.9); font-family: "Plus Jakarta Sans", sans-serif; font-weight: 700; font-size: 11px; flex-shrink: 0; }
.crm2-avatar--lg { width: 44px; height: 44px; font-size: 15px; }
.crm2-avatar--xl { width: 64px; height: 64px; font-size: 20px; }
.crm2-cust-info { display: flex; flex-direction: column; gap: 1px; }
.crm2-cust-name { font-weight: 600; font-size: 13px; }
.crm2-cust-dob { font-size: 11px; color: #9ca3af; }
.crm2-contact-cell { display: flex; flex-direction: column; gap: 2px; }
.crm2-contact-email { font-size: 12.5px; color: #6b7280; }
.crm2-contact-phone { font-size: 11.5px; color: #6b7280; }

.crm2-points-cell { display: flex; flex-direction: column; gap: 4px; }
.crm2-points-num { font-size: 12.5px; font-weight: 700; color: #2563eb; }
.crm2-points-track { height: 3px; background: #e4e7ec; width: 70px; border-radius: 2px; overflow: hidden; }
.crm2-points-fill { height: 100%; border-radius: 2px; transition: width 0.3s; }

.crm2-vipnote-cell { cursor: pointer; padding: 4px 8px; border-radius: 8px; background: #fffbeb; border: 1px solid #fde68a; max-width: 180px; transition: all 0.12s; }
.crm2-vipnote-cell:hover { background: #fef3c7; }
.crm2-vipnote-text { font-size: 11.5px; color: #d97706; line-height: 1.4; word-break: break-word; }
.crm2-note-text { font-size: 12.5px; color: #6b7280; }

/* Tags */
.crm2-tag { display: inline-flex; align-items: center; padding: 3px 9px; border-radius: 20px; font-size: 11px; font-weight: 700; border: 1px solid transparent; white-space: nowrap; }
.crm2-tag--blue   { background: #eff6ff; color: #2563eb; border-color: #bfdbfe; }
.crm2-tag--green  { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }
.crm2-tag--red    { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.crm2-tag--orange { background: #fffbeb; color: #d97706; border-color: #fde68a; }
.crm2-tag--purple { background: #f5f3ff; color: #7c3aed; border-color: #ddd6fe; }
.crm2-tag--gray   { background: #f3f4f6; color: #6b7280; border-color: #e5e7eb; }

/* Tier tags */
.crm2-tier-tag { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 800; letter-spacing: 0.05em; border: 1px solid transparent; white-space: nowrap; text-transform: uppercase; }
.crm2-tier-tag--sm { font-size: 10px; padding: 2px 8px; }
.crm2-tier--bronze   { background: #fef3c7; color: #92400e; border-color: #fde68a; }
.crm2-tier--silver   { background: #f3f4f6; color: #374151; border-color: #e5e7eb; }
.crm2-tier--gold     { background: #fef9c3; color: #a16207; border-color: #fde047; }
.crm2-tier--platinum { background: #f5f3ff; color: #7c3aed; border-color: #ddd6fe; }
.crm2-tier--diamond  { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }

/* Action buttons */
.crm2-actions { display: flex; gap: 4px; flex-wrap: wrap; }
.crm2-action-btn { display: inline-flex; align-items: center; gap: 4px; padding: 5px 10px; font-size: 11px; font-weight: 700; border-radius: 6px; border: 1.5px solid transparent; cursor: pointer; transition: all 0.15s ease; font-family: "Plus Jakarta Sans", sans-serif; white-space: nowrap; }
.crm2-action-btn--edit   { background: #eff6ff; color: #2563eb; border-color: #bfdbfe; }
.crm2-action-btn--edit:hover   { background: #2563eb; color: #fff; border-color: #2563eb; transform: translateY(-1px); }
.crm2-action-btn--blue   { background: #eff6ff; color: #2563eb; border-color: #bfdbfe; }
.crm2-action-btn--blue:hover   { background: #2563eb; color: #fff; border-color: #2563eb; transform: translateY(-1px); }
.crm2-action-btn--gold   { background: #fffbeb; color: #d97706; border-color: #fde68a; }
.crm2-action-btn--gold:hover   { background: #d97706; color: #fff; border-color: #d97706; transform: translateY(-1px); }
.crm2-action-btn--delete { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.crm2-action-btn--delete:hover { background: #dc2626; color: #fff; border-color: #dc2626; transform: translateY(-1px); }
.crm2-action-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none !important; }

/* Empty state */
.crm2-empty { text-align: center; padding: 60px 20px !important; }
.crm2-empty__inner { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.crm2-empty__icon { width: 56px; height: 56px; border-radius: 50%; background: #f0f2f5; display: flex; align-items: center; justify-content: center; color: #9ca3af; }
.crm2-empty__inner p { color: #6b7280; font-size: 14px; margin: 0; }

/* Pagination */
.crm2-pagination { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-top: 1px solid #f0f2f5; background: #fafbfc; flex-wrap: wrap; gap: 10px; }
.crm2-pagination__info { font-size: 12.5px; color: #6b7280; font-weight: 600; }
.crm2-pagination__controls { display: flex; align-items: center; gap: 4px; }
.crm2-page-btn { display: inline-flex; align-items: center; justify-content: center; min-width: 32px; height: 32px; padding: 0 8px; font-size: 13px; font-weight: 600; border-radius: 8px; border: 1.5px solid #e4e7ec; background: #ffffff; color: #6b7280; cursor: pointer; transition: all 0.15s; font-family: "Plus Jakarta Sans", sans-serif; }
.crm2-page-btn:hover:not(:disabled) { border-color: #2563eb; color: #2563eb; }
.crm2-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.crm2-page-btn--active { background: #2563eb; border-color: #2563eb; color: #fff; box-shadow: 0 2px 8px rgba(37,99,235,0.25); }

/* Buttons */
.crm2-btn { display: inline-flex; align-items: center; gap: 7px; padding: 9px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; border: none; cursor: pointer; transition: all 0.18s ease; font-family: "Plus Jakarta Sans", sans-serif; white-space: nowrap; }
.crm2-btn--primary { background: #2563eb; color: #fff; box-shadow: 0 2px 8px rgba(37,99,235,0.22); }
.crm2-btn--primary:hover { background: #1d4ed8; transform: translateY(-1px); }
.crm2-btn--outline { background: #ffffff; color: #0f1117; border: 1.5px solid #e4e7ec; }
.crm2-btn--outline:hover { border-color: #9ca3af; background: #f9fafb; }
.crm2-btn--ghost { background: transparent; color: #6b7280; border: 1.5px solid #e4e7ec; }
.crm2-btn--ghost:hover { background: #f0f2f5; color: #0f1117; }
.crm2-btn--loading, .crm2-btn:disabled { opacity: 0.65; pointer-events: none; transform: none !important; }
.crm2-btn--confirm-blue   { background: linear-gradient(135deg, #1d4ed8, #2563eb); color: #fff; box-shadow: 0 4px 14px rgba(37,99,235,0.35); padding: 11px 22px; }
.crm2-btn--confirm-blue:hover { background: linear-gradient(135deg, #1e3a8a, #1d4ed8); transform: translateY(-1px); }
.crm2-btn--confirm-orange { background: linear-gradient(135deg, #b45309, #d97706); color: #fff; box-shadow: 0 4px 14px rgba(217,119,6,0.35); padding: 11px 22px; }
.crm2-btn--confirm-orange:hover { background: linear-gradient(135deg, #92400e, #b45309); transform: translateY(-1px); }
.crm2-btn--confirm-green  { background: linear-gradient(135deg, #15803d, #16a34a); color: #fff; box-shadow: 0 4px 14px rgba(22,163,74,0.35); padding: 11px 22px; }
.crm2-btn--confirm-green:hover { background: linear-gradient(135deg, #14532d, #15803d); transform: translateY(-1px); }

.crm2-btn-ghost-sm { background: transparent; color: #6b7280; border: 1.5px solid #e4e7ec; padding: 5px 11px; border-radius: 8px; font-size: 11.5px; font-weight: 600; cursor: pointer; transition: all 0.12s; font-family: "Plus Jakarta Sans", sans-serif; display: inline-flex; align-items: center; gap: 5px; }
.crm2-btn-ghost-sm:hover { border-color: #9ca3af; color: #0f1117; }
.crm2-btn-ghost-sm:disabled { opacity: 0.4; cursor: not-allowed; }
.crm2-btn-link { background: none; border: none; color: #2563eb; font-size: 12px; cursor: pointer; font-family: "Plus Jakarta Sans", sans-serif; text-decoration: underline; padding: 0; }

.crm2-icon-btn { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; border: none; background: #f0f2f5; border-radius: 6px; cursor: pointer; color: #6b7280; flex-shrink: 0; transition: all 0.15s; }
.crm2-icon-btn:hover { background: #fff1f2; color: #dc2626; }

/* Input */
.crm2-input { padding: 9px 12px; font-size: 13px; font-family: "Plus Jakarta Sans", sans-serif; border: 1.5px solid #e4e7ec; border-radius: 8px; background: #ffffff; color: #0f1117; outline: none; transition: border-color 0.15s, box-shadow 0.15s; width: 100%; box-sizing: border-box; }
.crm2-input:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,0.08); }
.crm2-input::placeholder { color: #9ca3af; }
.crm2-select { cursor: pointer; }
.crm2-textarea { resize: vertical; line-height: 1.6; min-height: 80px; }

/* ── Birthday section ────────────────────── */
.crm2-sub-tabs {
  display: flex; gap: 2px; background: #ffffff;
  border: 1px solid #e4e7ec; border-radius: 12px; padding: 5px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  overflow-x: auto;
}
.crm2-sub-tab { display: inline-flex; align-items: center; padding: 7px 14px; font-size: 12px; font-weight: 600; border: none; border-radius: 8px; background: transparent; color: #6b7280; cursor: pointer; transition: all 0.15s; font-family: "Plus Jakarta Sans", sans-serif; white-space: nowrap; }
.crm2-sub-tab:hover { background: #f0f2f5; color: #0f1117; }
.crm2-sub-tab--active { background: #2563eb; color: #fff; }

.crm2-bd-banner { display: flex; align-items: center; gap: 9px; padding: 10px 20px; font-size: 13px; font-weight: 600; border-bottom: 1px solid #f0f2f5; }
.crm2-bd-banner--green  { background: #f0fdf4; color: #16a34a; border-bottom-color: #bbf7d0; }
.crm2-bd-banner--blue   { background: #eff6ff; color: #2563eb; border-bottom-color: #bfdbfe; }
.crm2-bd-banner--orange { background: #fffbeb; color: #d97706; border-bottom-color: #fde68a; }
.crm2-bd-controls { display: flex; align-items: center; gap: 12px; padding: 12px 20px; border-bottom: 1px solid #f0f2f5; }

/* ── Bar ─────────────────────────────────── */
.crm2-bar-wrap { height: 6px; background: #e4e7ec; border-radius: 999px; overflow: hidden; }
.crm2-bar { height: 100%; border-radius: 999px; transition: width 0.4s ease; min-width: 4px; }
.crm2-bar--blue   { background: #2563eb; }
.crm2-bar--green  { background: #16a34a; }
.crm2-bar--orange { background: #d97706; }

/* ── Inactive segment tabs ───────────────── */
.crm2-seg-tabs {
  display: flex; gap: 2px; background: #ffffff;
  border: 1px solid #e4e7ec; border-radius: 12px; padding: 5px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  overflow-x: auto;
}
.crm2-seg-btn { flex: 1; display: flex; align-items: center; justify-content: center; gap: 10px; padding: 10px 16px; font-size: 13px; font-weight: 600; border: none; border-radius: 8px; background: transparent; color: #6b7280; cursor: pointer; transition: all 0.15s; font-family: "Plus Jakarta Sans", sans-serif; }
.crm2-seg-btn:hover { background: #f0f2f5; color: #0f1117; }
.crm2-seg-btn--active { background: #ffffff; color: #0f1117; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.crm2-seg-label { font-size: 13px; }
.crm2-seg-count { padding: 2px 9px; border-radius: 20px; font-size: 11.5px; font-weight: 700; background: #f0f2f5; color: #9ca3af; font-family: "JetBrains Mono", monospace; transition: all 0.2s; }
.crm2-seg-count--loaded { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }

/* ── Inactive cards ──────────────────────── */
.crm2-cards-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
@media (max-width:1100px) { .crm2-cards-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width:700px)  { .crm2-cards-grid { grid-template-columns: 1fr; } }

.crm2-cust-card {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 12px; overflow: hidden;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  animation: crm2-fadeUp 0.3s ease both;
}
.crm2-cust-card:hover {
  border-color: #bfdbfe; transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08), 0 2px 6px rgba(0,0,0,0.04);
}
@keyframes crm2-fadeUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.crm2-card-tier-bar { height: 3px; width: 100%; }
.crm2-tier-bar--bronze   { background: #cd7f32; }
.crm2-tier-bar--silver   { background: #94a3b8; }
.crm2-tier-bar--gold     { background: #f0b429; }
.crm2-tier-bar--platinum { background: #7c3aed; }
.crm2-tier-bar--diamond  { background: #16a34a; }
.crm2-tier-bar--none     { background: #e4e7ec; }

.crm2-card-head { display: flex; align-items: center; gap: 10px; padding: 14px; }
.crm2-card-info { flex: 1; min-width: 0; }
.crm2-card-name { font-size: 13px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.crm2-card-email { font-size: 11.5px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 2px; }
.crm2-card-divider { height: 1px; background: #f0f2f5; margin: 0 14px; }
.crm2-card-metrics { display: flex; align-items: center; justify-content: space-around; padding: 12px 14px; }
.crm2-cm { display: flex; flex-direction: column; align-items: center; gap: 3px; }
.crm2-cm__val { font-size: 13px; font-weight: 600; color: #0f1117; }
.crm2-cm__label { font-size: 9px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 700; }
.crm2-cm-divider { width: 1px; height: 28px; background: #f0f2f5; }
.crm2-card-foot { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px 14px; border-top: 1px solid #f0f2f5; }
.crm2-last-order { font-size: 11.5px; color: #6b7280; }
.crm2-last-order--none { color: #dc2626; }

/* ── Zero Order templates ────────────────── */
.crm2-zo-templates { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; margin-top: 8px; }
.crm2-zo-tpl { padding: 12px 14px; border: 1.5px solid #e4e7ec; border-radius: 8px; cursor: pointer; transition: all 0.15s; }
.crm2-zo-tpl:hover { border-color: #bfdbfe; background: #eff6ff; }
.crm2-zo-tpl--active { border-color: #2563eb; background: #eff6ff; }
.crm2-zo-tpl__icon { font-size: 20px; margin-bottom: 4px; }
.crm2-zo-tpl__name { font-size: 12px; font-weight: 700; color: #0f1117; }
.crm2-zo-tpl__desc { font-size: 11px; color: #6b7280; margin-top: 2px; }

.crm2-zo-preview { background: #f0f2f5; border: 1px dashed #bfdbfe; border-radius: 8px; padding: 14px; }
.crm2-zo-preview__label { font-size: 10px; font-weight: 700; letter-spacing: 0.1em; text-transform: uppercase; color: #9ca3af; margin-bottom: 10px; }
.crm2-zo-preview__card { background: #ffffff; border: 1px solid #e4e7ec; border-radius: 8px; padding: 12px; display: flex; gap: 12px; align-items: flex-start; }

.crm2-zo-progress { height: 4px; background: #f0f2f5; overflow: hidden; margin: 0 -24px; }
.crm2-zo-progress__fill { height: 100%; background: #2563eb; transition: width 0.4s ease; }

/* ══════════════════════════════════════════
   DIALOGS
══════════════════════════════════════════ */
.crm2-overlay { position: fixed; inset: 0; background: rgba(10,12,20,0.28); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 20px; backdrop-filter: blur(2px); }
.crm2-dialog {
  background: #fff; border-radius: 20px; width: 100%;
  box-shadow: 0 24px 64px rgba(0,0,0,0.14), 0 8px 24px rgba(0,0,0,0.06);
  overflow: hidden; max-height: 90vh; display: flex; flex-direction: column;
}
.crm2-dialog--sm { max-width: 480px; }
.crm2-dialog--lg { max-width: 760px; }
.crm2-dialog--xl { max-width: 1000px; }

/* Band */
.crm2-dialog__band { position: relative; height: 100px; display: flex; align-items: center; justify-content: center; overflow: hidden; flex-shrink: 0; }
.crm2-dialog__band--blue   { background: linear-gradient(135deg, #1d4ed8, #2563eb 60%, #60a5fa); }
.crm2-dialog__band--orange { background: linear-gradient(135deg, #b45309, #d97706 60%, #fbbf24); }
.crm2-dialog__band--green  { background: linear-gradient(135deg, #15803d, #16a34a 60%, #4ade80); }
.crm2-dialog__band--purple { background: linear-gradient(135deg, #5b21b6, #7c3aed 60%, #a78bfa); }
.crm2-dialog__band-circles { position: absolute; inset: 0; pointer-events: none; }
.crm2-band-circle { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.1); }
.crm2-band-circle--1 { width: 120px; height: 120px; top: -30px; right: -20px; }
.crm2-band-circle--2 { width: 70px;  height: 70px;  bottom: -15px; left: 20px; }
.crm2-band-circle--3 { width: 45px;  height: 45px;  top: 10px; left: 50%; margin-left: -22px; background: rgba(255,255,255,0.07); }
.crm2-dialog__icon-wrap { position: relative; z-index: 1; }
.crm2-dialog__icon-ring { width: 56px; height: 56px; background: rgba(255,255,255,0.2); border: 2px solid rgba(255,255,255,0.4); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; backdrop-filter: blur(4px); box-shadow: 0 8px 32px rgba(0,0,0,0.15), inset 0 1px 0 rgba(255,255,255,0.3); }

/* Dialog body */
.crm2-dialog__body { padding: 24px 28px; display: flex; flex-direction: column; gap: 14px; overflow-y: auto; flex: 1; }
.crm2-dialog__head-simple { display: flex; align-items: center; justify-content: space-between; padding: 20px 28px; border-bottom: 1px solid #f0f2f5; gap: 12px; flex-shrink: 0; }
.crm2-dialog__head-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
.crm2-details-hd { display: flex; align-items: center; gap: 12px; }
.crm2-dialog__badge { display: inline-flex; align-self: flex-start; font-size: 10.5px; font-weight: 800; letter-spacing: 0.1em; text-transform: uppercase; padding: 4px 10px; border-radius: 20px; }
.crm2-dialog__badge--blue   { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.crm2-dialog__badge--orange { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }
.crm2-dialog__badge--green  { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.crm2-dialog__title { font-size: 20px; font-weight: 800; letter-spacing: -0.03em; margin: 0; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.crm2-dialog__sub { font-size: 12px; color: #6b7280; margin: 3px 0 0; font-family: "JetBrains Mono", monospace; }
.crm2-dialog__notice { display: flex; align-items: center; gap: 8px; padding: 10px 14px; border-radius: 10px; font-size: 12.5px; font-weight: 600; line-height: 1.5; border: 1px solid transparent; }
.crm2-dialog__notice--red { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.crm2-dialog__actions { display: flex; gap: 10px; justify-content: flex-end; padding-top: 4px; border-top: 1px solid #f0f2f5; margin-top: 4px; }

/* Dialog form sections */
.crm2-dlg-section { font-size: 10.5px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; color: #6b7280; padding-bottom: 8px; border-bottom: 1px solid #f0f2f5; margin-top: 4px; }
.crm2-row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.crm2-field { display: flex; flex-direction: column; gap: 5px; }
.crm2-field__label { font-size: 11px; font-weight: 700; color: #6b7280; text-transform: uppercase; letter-spacing: 0.05em; }
.crm2-required { color: #dc2626; margin-left: 2px; }

/* Details grid */
.crm2-details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.crm2-detail-block { border: 1px solid #e4e7ec; border-radius: 8px; overflow: hidden; }
.crm2-detail-block__head { display: flex; align-items: center; gap: 8px; font-size: 10.5px; font-weight: 700; letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af; background: #fafbfc; padding: 10px 14px; border-bottom: 1px solid #f0f2f5; }
.crm2-detail-rows { padding: 4px 14px; }
.crm2-detail-row { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #f0f2f5; font-size: 12.5px; }
.crm2-detail-row:last-child { border-bottom: none; }
.crm2-dk { color: #6b7280; font-weight: 600; }
.crm2-dv { color: #0f1117; text-align: right; font-size: 12.5px; }
.crm2-detail-body-text { padding: 12px 14px; font-size: 12.5px; color: #6b7280; line-height: 1.6; }

/* VIP note block */
.crm2-vip-note-block { border-color: #fde68a; }
.crm2-vip-note-head { padding: 10px 14px; background: #fffbeb; border-bottom: 1px solid #fde68a; }
.crm2-internal-tag { display: inline-block; background: #fffbeb; color: #d97706; border: 1px solid #fde68a; padding: 1px 7px; border-radius: 20px; font-size: 9px; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; margin-left: 6px; }
.crm2-vip-textarea { border-color: #fde68a; background: #fffbeb; color: #92400e; }
.crm2-vip-textarea:focus { border-color: #d97706; box-shadow: 0 0 0 3px rgba(217,119,6,0.08); }
.crm2-vip-note-hint { font-size: 10.5px; color: #6b7280; margin-top: 5px; display: flex; justify-content: space-between; }
.crm2-vip-unavail { font-size: 11.5px; color: #6b7280; padding: 10px 14px; border: 1px solid #e4e7ec; border-radius: 8px; background: #f0f2f5; }

/* VIP modal */
.crm2-vip-modal { border: 1px solid #fde68a; }
.crm2-vip-band { position: relative; background: linear-gradient(135deg, #451a03, #78350f 50%, #92400e); padding: 22px 24px 18px; overflow: hidden; display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; flex-shrink: 0; }
.crm2-vip-band__deco { position: absolute; border-radius: 50%; border: 1px solid rgba(205,127,50,0.15); background: rgba(205,127,50,0.06); pointer-events: none; }
.crm2-vip-band__deco--1 { width: 120px; height: 120px; top: -20px; right: -20px; }
.crm2-vip-band__deco--2 { width: 60px;  height: 60px;  top: 10px;  right: 30px; }
.crm2-vip-band__content { flex: 1; position: relative; z-index: 1; }
.crm2-vip-band__top { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.crm2-vip-eyebrow { font-size: 10px; letter-spacing: 2.5px; text-transform: uppercase; color: #d97706; font-weight: 500; }
.crm2-vip-admin-badge { background: rgba(205,127,50,0.15); border: 1px solid rgba(205,127,50,0.3); color: #fbbf24; font-size: 9px; letter-spacing: 1.5px; text-transform: uppercase; padding: 2px 8px; border-radius: 20px; font-weight: 700; }
.crm2-vip-band__title { font-size: 22px; color: #fef3c7; font-weight: 800; letter-spacing: -0.02em; }
.crm2-vip-band__customer { margin-top: 6px; font-size: 12px; color: #d97706; letter-spacing: 0.3px; }
.crm2-vip-close-btn { position: relative; z-index: 1; background: rgba(255,255,255,0.1); border: 1px solid rgba(205,127,50,0.25); color: #d97706; width: 30px; height: 30px; border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: all 0.15s; }
.crm2-vip-close-btn:hover { background: rgba(205,127,50,0.2); color: #fbbf24; }

.crm2-vip-notice { background: #fffbeb; border-bottom: 1px solid #fde68a; padding: 9px 24px; font-size: 12px; color: #92400e; display: flex; align-items: center; gap: 8px; flex-shrink: 0; }

.crm2-vip-suggestions { margin-bottom: 4px; }
.crm2-vip-suggestion-label { font-size: 10px; letter-spacing: 1.5px; text-transform: uppercase; color: #9ca3af; font-weight: 700; margin-bottom: 8px; }
.crm2-vip-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.crm2-vip-chip { background: #ffffff; border: 1.5px solid #e4e7ec; color: #6b7280; padding: 5px 12px; border-radius: 20px; font-size: 12px; cursor: pointer; font-family: "Plus Jakarta Sans", sans-serif; transition: all 0.15s; }
.crm2-vip-chip:hover { border-color: #fde68a; color: #92400e; background: #fffbeb; }

.crm2-vip-textarea-wrap { position: relative; }
.crm2-vip-textarea { width: 100%; background: #fffdf7; border: 1.5px solid #fde68a; border-radius: 8px; padding: 12px 14px; font-size: 13px; color: #0f1117; font-family: "Plus Jakarta Sans", sans-serif; resize: vertical; min-height: 120px; box-sizing: border-box; outline: none; line-height: 1.6; transition: border-color 0.15s; }
.crm2-vip-textarea:focus { border-color: #d97706; background: #fffcf0; }
.crm2-vip-textarea::placeholder { color: #9ca3af; }
.crm2-vip-textarea-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; min-height: 20px; }

/* Summary controls */
.crm2-summary-controls { display: flex; align-items: center; justify-content: space-between; padding: 12px 24px; border-bottom: 1px solid #f0f2f5; background: #fafbfc; gap: 12px; flex-wrap: wrap; flex-shrink: 0; }
.crm2-sc-left { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
.crm2-ctrl-group { display: flex; align-items: center; gap: 8px; }
.crm2-ctrl-label { font-size: 10.5px; font-weight: 700; letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af; white-space: nowrap; }
.crm2-toggle-set { display: flex; border: 1px solid #e4e7ec; border-radius: 8px; overflow: hidden; }
.crm2-tgl { padding: 6px 14px; font-size: 12px; font-weight: 600; border: none; background: #ffffff; cursor: pointer; font-family: "Plus Jakarta Sans", sans-serif; color: #6b7280; transition: all 0.12s; }
.crm2-tgl:hover { background: #f0f2f5; }
.crm2-tgl--on { background: #2563eb !important; color: #fff !important; }

.crm2-summary-body { max-height: 60vh; overflow-y: auto; }

/* KPI row */
.crm2-kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 20px; }
.crm2-kpi { background: #f0f2f5; border: 1px solid #e4e7ec; border-radius: 8px; padding: 16px; }
.crm2-kpi--green  { border-top: 3px solid #16a34a; }
.crm2-kpi--orange { border-top: 3px solid #d97706; }
.crm2-kpi--blue   { border-top: 3px solid #2563eb; }
.crm2-kpi--red    { border-top: 3px solid #dc2626; }
.crm2-kpi--purple { border-top: 3px solid #7c3aed; }
.crm2-kpi__val { font-size: 22px; font-weight: 800; letter-spacing: -0.02em; line-height: 1; }
.crm2-kpi--green  .crm2-kpi__val { color: #16a34a; }
.crm2-kpi--orange .crm2-kpi__val { color: #d97706; }
.crm2-kpi--blue   .crm2-kpi__val { color: #2563eb; }
.crm2-kpi--red    .crm2-kpi__val { color: #dc2626; }
.crm2-kpi--purple .crm2-kpi__val { color: #7c3aed; }
.crm2-kpi__label { font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.07em; color: #9ca3af; margin-top: 6px; }

/* Period list */
.crm2-period-list { display: flex; flex-direction: column; gap: 6px; }
.crm2-period-row { display: grid; grid-template-columns: 150px 1fr 110px; gap: 16px; align-items: center; background: #f0f2f5; border: 1px solid #e4e7ec; border-radius: 8px; padding: 12px 16px; transition: background 0.1s; }
.crm2-period-row:hover { background: #ffffff; }
.crm2-period-label { font-weight: 700; font-size: 13px; }
.crm2-period-dates { font-size: 10.5px; margin-top: 2px; }
.crm2-period-bars { display: flex; flex-direction: column; gap: 5px; }
.crm2-bar-row { display: flex; align-items: center; gap: 8px; }
.crm2-br-num { font-size: 11px; font-weight: 600; font-family: "JetBrains Mono", monospace; width: 80px; text-align: right; }
.crm2-bar-bg { flex: 1; height: 6px; background: #e4e7ec; border-radius: 999px; overflow: hidden; }
.crm2-period-net { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.crm2-net-chip { font-family: "JetBrains Mono", monospace; font-size: 12px; font-weight: 700; padding: 3px 8px; border-radius: 20px; }
.crm2-net-chip--pos { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.crm2-net-chip--neg { background: #fff1f2; color: #dc2626; border: 1px solid #fecdd3; }

.crm2-cust-period { margin-bottom: 16px; border: 1px solid #e4e7ec; border-radius: 8px; overflow: hidden; }
.crm2-cust-period__head { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px; background: #fafbfc; border-bottom: 1px solid #f0f2f5; }

/* Inactive detail mini stats */
.crm2-stat-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin: 16px 0; }
.crm2-stat-mini { background: #f0f2f5; border: 1px solid #e4e7ec; border-radius: 8px; padding: 14px; text-align: center; }
.crm2-stat-mini--orange { border-top: 3px solid #d97706; }
.crm2-stat-mini--green  { border-top: 3px solid #16a34a; }
.crm2-stat-mini__val   { font-size: 18px; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 4px; }
.crm2-stat-mini--orange .crm2-stat-mini__val { color: #d97706; }
.crm2-stat-mini--green  .crm2-stat-mini__val { color: #16a34a; }
.crm2-stat-mini__label { font-size: 10px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.07em; font-weight: 700; }

/* Spinner */
.crm2-spinner { display: inline-block; width: 14px; height: 14px; border: 2px solid rgba(0,0,0,0.12); border-top-color: #6b7280; border-radius: 50%; animation: crm2-spin 0.6s linear infinite; flex-shrink: 0; }
.crm2-spinner--white { border-color: rgba(255,255,255,0.25); border-top-color: #fff; }
@keyframes crm2-spin { to { transform: rotate(360deg); } }

/* Responsive */
@media (max-width: 900px) {
  .crm2-page { padding: 20px 16px 40px; }
  .crm2-filter-fields { grid-template-columns: 1fr 1fr; }
  .crm2-filter-points { grid-template-columns: 1fr 1fr; }
  .crm2-row2 { grid-template-columns: 1fr; }
  .crm2-details-grid { grid-template-columns: 1fr; }
  .crm2-kpi-row { grid-template-columns: 1fr 1fr; }
  .crm2-period-row { grid-template-columns: 1fr; }
}
</style>