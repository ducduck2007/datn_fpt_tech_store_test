<template>
  <div class="crm-root">
    <div class="crm-inner">

      <!-- PAGE HEADER -->
      <header class="crm-header">
        <div class="header-left">
          <p class="header-eyebrow">Quản lý Quan hệ Khách hàng</p>
          <h1 class="header-title">Quản lý <span class="title-line">Khách hàng</span></h1>
          <p class="header-meta">{{ rows.length }} tài khoản đã đăng ký</p>
        </div>
        <div class="header-actions">
          <button class="btn-outline" @click="reloadCurrentTab" :disabled="loading">
            <span v-if="loading" class="spin-icon">↻</span>
            <span v-else>↻</span>
            Tải lại
          </button>
          <button v-if="mainTab === 'customers'" class="btn-solid" @click="openCreate">
            + Thêm khách hàng
          </button>
          <button v-if="mainTab === 'customers'" class="btn-outline" @click="openSummaryDialog">
            Tổng điểm
          </button>
        </div>
      </header>

      <!-- NAVIGATION TABS -->
      <nav class="crm-nav">
        <button :class="['crm-nav-tab', { active: mainTab === 'customers' }]" @click="mainTab = 'customers'">
          Khách hàng &amp; Thành viên
        </button>
        <button :class="['crm-nav-tab', { active: mainTab === 'birthdays' }]" @click="mainTab = 'birthdays'; loadBirthdayData()">
          Sinh nhật
        </button>
        <button :class="['crm-nav-tab', { active: mainTab === 'inactive' }]" @click="mainTab = 'inactive'; loadInactiveAll()">
          Không hoạt động
        </button>
        <div class="nav-underline" />
      </nav>

      <!-- ══ TAB 1: CUSTOMERS ══ -->
      <template v-if="mainTab === 'customers'">

        <!-- STAT CARDS -->
        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-label">Tổng khách hàng</span>
            <span class="stat-value">{{ rows.length }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">Thành viên VIP</span>
            <span class="stat-value accent-gold">{{ vipCount }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">Tổng điểm</span>
            <span class="stat-value accent-blue">{{ totalPoints.toLocaleString() }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">Điểm TB</span>
            <span class="stat-value">{{ avgPoints.toLocaleString() }}</span>
          </div>
          <div class="stat-card" v-if="hasActiveFilters">
            <span class="stat-label">Đã lọc</span>
            <span class="stat-value accent-teal">{{ filtered.length }}</span>
          </div>
        </div>

        <!-- FILTERS -->
        <div class="filter-panel">
          <div class="filter-panel-head">
            <span class="filter-panel-title">Bộ lọc</span>
            <button class="btn-link" @click="clearAllFilters" :disabled="!hasActiveFilters">Xóa tất cả</button>
          </div>
          <div class="filter-grid">
            <div class="field-group">
              <label class="field-label">Tìm kiếm</label>
              <input v-model="q" class="field-input" placeholder="Tên / Email / SĐT…" />
            </div>
            <div class="field-group">
              <label class="field-label">Loại</label>
              <select v-model="typeFilter" class="field-select" @change="handleFilterChange">
                <option value="">Tất cả</option>
                <option value="REGULAR">Thường</option>
                <option value="VIP">VIP</option>
              </select>
            </div>
            <div class="field-group">
              <label class="field-label">Cấp VIP</label>
              <select v-model="vipTierFilter" class="field-select" @change="handleFilterChange">
                <option value="">Tất cả cấp</option>
                <option value="BRONZE">Đồng</option>
                <option value="SILVER">Bạc</option>
                <option value="GOLD">Vàng</option>
                <option value="PLATINUM">Bạch kim</option>
                <option value="DIAMOND">Kim cương</option>
              </select>
            </div>
            <div class="field-group">
              <label class="field-label">Hoạt động</label>
              <select v-model="activityFilter" class="field-select" @change="handleFilterChange">
                <option value="">Tất cả</option>
                <option value="ACTIVE_30">Hoạt động (30 ngày)</option>
              </select>
            </div>
          </div>
          <div class="filter-divider" />
          <div class="filter-grid filter-grid-points">
            <div class="field-group">
              <label class="field-label">Điểm tối thiểu</label>
              <input v-model.number="pointsMin" type="number" class="field-input" placeholder="0" min="0" />
            </div>
            <div class="field-group">
              <label class="field-label">Điểm tối đa</label>
              <input v-model.number="pointsMax" type="number" class="field-input" placeholder="999999" min="0" />
            </div>
            <div class="field-group field-group-actions">
              <button class="btn-solid" @click="applyPointsFilter" :disabled="!canApplyPointsFilter">Áp dụng</button>
              <button class="btn-outline" @click="clearPointsFilter" :disabled="!isPointsFilterApplied">Reset</button>
            </div>
          </div>
          <div class="active-filters" v-if="hasActiveFilters">
            <span class="af-label">Đang hoạt động:</span>
            <span class="filter-chip" v-if="typeFilter">Loại: {{ typeFilter }}<button @click="typeFilter = ''; handleFilterChange()" class="chip-remove">×</button></span>
            <span class="filter-chip" v-if="vipTierFilter">Cấp: {{ vipTierFilter }}<button @click="vipTierFilter = ''; handleFilterChange()" class="chip-remove">×</button></span>
            <span class="filter-chip" v-if="activityFilter === 'ACTIVE_30'">HD 30 ngày<button @click="activityFilter = ''; handleFilterChange()" class="chip-remove">×</button></span>
            <span class="filter-chip" v-if="isPointsFilterApplied">Điểm: {{ appliedPointsMin }}–{{ appliedPointsMax === 999999 ? '∞' : appliedPointsMax }}<button @click="clearPointsFilter" class="chip-remove">×</button></span>
          </div>
        </div>

        <!-- TABLE -->
        <div class="table-panel">
          <div class="table-loading" v-if="loading">
            <div class="loader-dots"><span/><span/><span/></div>
            <p>Đang tải dữ liệu…</p>
          </div>
          <div class="table-scroll" v-else>
            <table class="crm-table">
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Khách hàng</th>
                  <th>Liên hệ</th>
                  <th>Loại</th>
                  <th>Cấp</th>
                  <th>Điểm</th>
                  <th>Chi tiêu</th>
                  <th>Ghi chú VIP</th>
                  <th>Ghi chú</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="paged.length === 0">
                  <td colspan="10" class="empty-row">
                    <p class="empty-msg">Không tìm thấy khách hàng</p>
                  </td>
                </tr>
                <tr v-for="row in paged" :key="row.id" class="crm-row">
                  <td><span class="id-mono">#{{ row.id }}</span></td>
                  <td>
                    <div class="customer-cell">
                      <div class="avatar" :style="{ background: getAvatarColor(row.fullName) }">{{ getInitials(row.fullName) }}</div>
                      <div class="cust-info">
                        <span class="cust-name">{{ row.fullName }}</span>
                        <span class="cust-dob" v-if="row.birthDate">{{ row.birthDate }}</span>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="contact-cell">
                      <span class="contact-email">{{ row.email }}</span>
                      <span class="contact-phone" v-if="row.phone">{{ row.phone }}</span>
                    </div>
                  </td>
                  <td>
                    <span :class="['badge-type', row.customerType === 'VIP' ? 'badge-vip' : 'badge-reg']">
                      {{ row.customerType || 'REGULAR' }}
                    </span>
                  </td>
                  <td>
                    <span v-if="row.raw.vipTier" :class="['badge-tier', `tier-${row.raw.vipTier.toLowerCase()}`]">
                      {{ row.raw.vipTier }}
                    </span>
                    <span v-else class="muted">—</span>
                  </td>
                  <td>
                    <div class="points-cell">
                      <span class="points-num">{{ (row.loyaltyPoints || 0).toLocaleString() }}</span>
                      <div class="points-track" v-if="row.raw.vipTier">
                        <div class="points-fill" :style="{ width: getPointsProgress(row.raw) + '%', background: getTierColor(row.raw.vipTier) }" />
                      </div>
                    </div>
                  </td>
                  <td><span class="spent-val">{{ formatCurrencyShort(row.raw.totalSpent) }}</span></td>
                  <td>
                    <template v-if="row.raw.vipTier">
                      <div v-if="row.raw.vipNote" class="vipnote-cell" @click="openVipNoteEdit(row)">
                        <span class="vipnote-text">{{ row.raw.vipNote.substring(0, 28) }}{{ row.raw.vipNote.length > 28 ? '…' : '' }}</span>
                      </div>
                      <button v-else class="btn-ghost-sm" @click="openVipNoteEdit(row)">Thêm ghi chú</button>
                    </template>
                    <span v-else class="muted">—</span>
                  </td>
                  <td>
                    <span class="note-text" v-if="row.notes" :title="row.notes">
                      {{ row.notes.substring(0, 22) }}{{ row.notes.length > 22 ? '…' : '' }}
                    </span>
                    <span v-else class="muted">—</span>
                  </td>
                  <td>
                    <div class="action-row">
                      <button class="act-btn" @click="viewDetails(row)" title="Xem">Xem</button>
                      <button class="act-btn" @click="openEdit(row)" title="Sửa">Sửa</button>
                      <button v-if="row.raw.vipTier" class="act-btn act-gold" @click="openVipNoteEdit(row)" title="Ghi chú VIP">VIP</button>
                      <button class="act-btn act-danger" @click="remove(row)" title="Xóa">Xóa</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="table-footer">
            <span class="pager-info">Hiển thị {{ Math.min((page-1)*pageSize+1, filtered.length) }}–{{ Math.min(page*pageSize, filtered.length) }} của {{ filtered.length }}</span>
            <div class="pager">
              <button class="pager-btn" :disabled="page===1" @click="page--">‹</button>
              <button v-for="p in pageCount" :key="p"
                :class="['pager-btn', { 'pager-active': p===page }]"
                @click="page=p"
                v-show="Math.abs(p-page)<=2 || p===1 || p===pageCount">{{ p }}</button>
              <button class="pager-btn" :disabled="page===pageCount" @click="page++">›</button>
            </div>
          </div>
        </div>
      </template>

      <!-- ══ TAB 2: BIRTHDAYS ══ -->
      <template v-if="mainTab === 'birthdays'">
        <div class="sub-nav">
          <button :class="['sub-nav-btn', { active: birthdayTab==='today' }]" @click="birthdayTab='today'">Hôm nay</button>
          <button :class="['sub-nav-btn', { active: birthdayTab==='monthly' }]" @click="birthdayTab='monthly'; loadMonthlyBirthdays()">Tháng này</button>
          <button :class="['sub-nav-btn', { active: birthdayTab==='statistics' }]" @click="birthdayTab='statistics'">Thống kê</button>
          <button :class="['sub-nav-btn', { active: birthdayTab==='upcoming' }]" @click="birthdayTab='upcoming'">Sắp tới</button>
          <button :class="['sub-nav-btn', { active: birthdayTab==='history' }]" @click="birthdayTab='history'; loadNotificationHistory()">Lịch sử</button>
        </div>

        <div class="table-panel" style="margin-top:0">
          <div class="table-loading" v-if="loading"><div class="loader-dots"><span/><span/><span/></div><p>Đang tải…</p></div>

          <!-- Today -->
          <template v-if="!loading && birthdayTab==='today'">
            <div class="bd-banner" :class="todayBirthdays.length>0 ? 'bd-green' : 'bd-blue'">
              {{ todayBirthdays.length>0 ? `${todayBirthdays.length} sinh nhật hôm nay` : 'Không có sinh nhật nào hôm nay' }}
            </div>
            <div class="table-scroll">
              <table class="crm-table">
                <thead><tr><th>Khách hàng</th><th>Email</th><th>SĐT</th><th>Tuổi</th><th>Loại</th><th>Cấp</th><th>Thao tác</th></tr></thead>
                <tbody>
                  <tr v-if="todayBirthdays.length===0"><td colspan="7" class="empty-row"><p class="empty-msg">Không có sinh nhật nào hôm nay</p></td></tr>
                  <tr v-for="row in todayBirthdays" :key="row.id" class="crm-row">
                    <td><strong>{{ row.name }}</strong></td>
                    <td>{{ row.email }}</td>
                    <td>{{ row.phone }}</td>
                    <td>{{ row.age }}</td>
                    <td><span :class="['badge-type', row.customerType==='VIP'?'badge-vip':'badge-reg']">{{ row.customerType }}</span></td>
                    <td><span v-if="row.vipTier" :class="['badge-tier',`tier-${row.vipTier.toLowerCase()}`]">{{ row.vipTier }}</span><span v-else class="muted">—</span></td>
                    <td><button class="btn-solid btn-sm" @click="sendGreeting(row)">Gửi lời chúc</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- Monthly -->
          <template v-if="!loading && birthdayTab==='monthly'">
            <div class="bd-controls">
              <select v-model="selectedMonth" @change="loadMonthlyBirthdays" class="field-select" style="min-width:150px">
                <option v-for="m in monthOptions" :key="m.value" :value="m.value">{{ m.label }}</option>
              </select>
              <div class="bd-banner bd-blue" style="flex:1;margin:0" v-if="monthlyBirthdays.length>0">
                <strong>{{ monthlyBirthdays.length }}</strong> khách hàng tháng này
              </div>
            </div>
            <div class="table-scroll">
              <table class="crm-table">
                <thead><tr><th>Khách hàng</th><th>Email</th><th>SĐT</th><th>Ngày</th><th>Tuổi</th><th>Loại</th><th>Thao tác</th></tr></thead>
                <tbody>
                  <tr v-if="monthlyBirthdays.length===0"><td colspan="7" class="empty-row"><p class="empty-msg">Không có sinh nhật nào trong tháng</p></td></tr>
                  <tr v-for="row in monthlyBirthdays" :key="row.id" class="crm-row">
                    <td><strong>{{ row.name }}</strong></td>
                    <td>{{ row.email }}</td>
                    <td>{{ row.phone }}</td>
                    <td>{{ formatBirthdayDate(row) }}</td>
                    <td>{{ row.age }}</td>
                    <td><span :class="['badge-type', row.customerType==='VIP'?'badge-vip':'badge-reg']">{{ row.customerType }}</span></td>
                    <td><button class="btn-solid btn-sm" @click="sendGreeting(row)">Send</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- Statistics -->
          <template v-if="!loading && birthdayTab==='statistics'">
            <div class="table-scroll">
              <table class="crm-table">
                <thead><tr><th>Tháng</th><th>Số lượng</th><th>Phân bổ</th><th>Thao tác</th></tr></thead>
                <tbody>
                  <tr v-for="row in statsTableData" :key="row.month" class="crm-row">
                    <td><strong>{{ row.monthLabel }}</strong></td>
                    <td><span :class="['badge-type', row.count>0?'badge-vip':'badge-reg']">{{ row.count }}</span></td>
                    <td style="min-width:200px">
                      <div class="bar-track-h"><div class="bar-fill-h" :style="{ width: getBdBarPct(row.count)+'%' }" /></div>
                    </td>
                    <td><button v-if="row.count>0" class="btn-ghost-sm" @click="viewMonthDetail(row.month)">Chi tiết</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- Upcoming -->
          <template v-if="!loading && birthdayTab==='upcoming'">
            <div class="bd-banner" :class="upcomingBirthdays.length>0?'bd-amber':'bd-blue'">
              {{ upcomingBirthdays.length>0 ? `${upcomingBirthdays.length} sắp tới trong 7 ngày` : 'Không có sinh nhật sắp tới' }}
            </div>
            <div class="table-scroll">
              <table class="crm-table">
                <thead><tr><th>Khách hàng</th><th>Email</th><th>SĐT</th><th>Ngày</th><th>Còn lại</th><th>Loại</th><th>Thao tác</th></tr></thead>
                <tbody>
                  <tr v-if="upcomingBirthdays.length===0"><td colspan="7" class="empty-row"><p class="empty-msg">Không có sinh nhật sắp tới</p></td></tr>
                  <tr v-for="row in upcomingBirthdays" :key="row.id" class="crm-row">
                    <td><strong>{{ row.name }}</strong></td>
                    <td>{{ row.email }}</td>
                    <td>{{ row.phone }}</td>
                    <td>{{ formatBirthdayDate(row) }}</td>
                    <td><span :class="['badge-type', row.daysUntilBirthday<=3?'badge-vip':'badge-reg']">{{ row.daysUntilBirthday }} ngày</span></td>
                    <td><span :class="['badge-type', row.customerType==='VIP'?'badge-vip':'badge-reg']">{{ row.customerType }}</span></td>
                    <td><button class="btn-solid btn-sm" @click="sendGreeting(row)">Send</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <!-- History -->
          <template v-if="birthdayTab==='history'">
            <div class="bd-controls" style="justify-content:space-between">
              <span class="bd-banner bd-blue" style="margin:0;flex:1" v-if="notificationHistory.length>0"><strong>{{ notificationHistory.length }}</strong> thông báo đã gửi</span>
              <button class="btn-ghost-sm" @click="loadNotificationHistory" :disabled="historyLoading">
                <span v-if="historyLoading" class="spin-icon">↻</span> Tải lại
              </button>
            </div>
            <div v-if="historyLoading" class="table-loading"><div class="loader-dots"><span/><span/><span/></div><p>Đang tải…</p></div>
            <div class="table-scroll" v-else>
              <table class="crm-table">
                <thead><tr><th>Khách hàng</th><th>Email</th><th>Tiêu đề</th><th>Nội dung</th><th>Trạng thái</th><th>Gửi lúc</th><th>Đọc lúc</th></tr></thead>
                <tbody>
                  <tr v-if="notificationHistory.length===0"><td colspan="7" class="empty-row"><p class="empty-msg">Chưa có thông báo nào</p></td></tr>
                  <tr v-for="row in notificationHistory" :key="row.id" class="crm-row">
                    <td><strong>{{ row.customerName }}</strong></td>
                    <td>{{ row.customerEmail }}</td>
                    <td>{{ row.title }}</td>
                    <td class="td-truncate" :title="row.message">{{ row.message }}</td>
                    <td><span :class="['badge-type', row.isRead?'badge-vip':'badge-reg']">{{ row.isRead ? 'Đã đọc' : 'Chưa đọc' }}</span></td>
                    <td class="mono-sm">{{ formatDateTime(row.createdAt) }}</td>
                    <td class="mono-sm">{{ row.readAt ? formatDateTime(row.readAt) : '—' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </div>
      </template>

      <!-- ══ TAB 3: INACTIVE ══ -->
      <template v-if="mainTab === 'inactive'">
        <div class="seg-tabs">
          <button v-for="tab in inactiveTabs" :key="tab.days"
            :class="['seg-btn', { active: activeInactiveTab===tab.days }]"
            @click="switchInactiveTab(tab.days)">
            <span class="seg-label">{{ tab.label }}</span>
            <span class="seg-count" :class="{ loaded: inactiveCounts[tab.days]!==null }">
              {{ inactiveCounts[tab.days]!==null ? inactiveCounts[tab.days] : '—' }}
            </span>
          </button>
        </div>

        <div class="inactive-stats" v-if="!loading && currentInactive.length>0">
          <div class="ist-card"><div class="ist-val">{{ currentInactive.length }}</div><div class="ist-lbl">Khách hàng</div></div>
          <div class="ist-card"><div class="ist-val gold">{{ inactiveTotalPoints }}</div><div class="ist-lbl">Tổng điểm</div></div>
          <div class="ist-card"><div class="ist-val green">{{ formatMoneyShort(inactiveTotalSpent) }}</div><div class="ist-lbl">Tổng chi tiêu</div></div>
          <div class="ist-card"><div class="ist-val blue">{{ inactiveTopTier }}</div><div class="ist-lbl">Cấp cao nhất</div></div>
        </div>

        <div class="filter-bar" v-if="!loading">
          <input v-model="inactiveQ" placeholder="Tìm tên, email, SĐT…" class="field-input" style="flex:1" />
          <select v-model="inactiveTierFilter" class="field-select">
            <option value="">Tất cả cấp</option>
            <option v-for="t in tiers" :key="t" :value="t">{{ t }}</option>
          </select>
          <select v-model="inactiveSortBy" class="field-select">
            <option value="spent">Chi tiêu nhiều nhất</option>
            <option value="points">Điểm nhiều nhất</option>
            <option value="name">Tên A→Z</option>
          </select>
        </div>

        <div class="table-loading" v-if="loading"><div class="loader-dots"><span/><span/><span/></div><p>Đang tải…</p></div>

        <div class="empty-state" v-if="!loading && inactiveFiltered.length===0">
          <div class="empty-title">Tốt cả</div>
          <div class="empty-sub">Tất cả khách hàng đều hoạt động trong {{ activeInactiveTab }} ngày qua.</div>
        </div>

        <div class="cards-grid" v-if="!loading && inactivePaged.length>0">
          <div class="cust-card" v-for="(row, idx) in inactivePaged" :key="row.id" :style="{ animationDelay: idx*0.04+'s' }">
            <div class="card-tier-bar" :class="'cb-'+(row.raw?.vipTier||'none').toLowerCase()" />
            <div class="card-head">
              <div class="avatar" :style="{ background: getAvatarColor(row.fullName) }">{{ getInitials(row.fullName) }}</div>
              <div class="card-info">
                <div class="card-name">{{ row.fullName }}</div>
                <div class="card-email">{{ row.email }}</div>
              </div>
              <span v-if="row.raw?.vipTier" :class="['badge-tier', `tier-${row.raw.vipTier.toLowerCase()}`]" style="flex-shrink:0">{{ row.raw.vipTier }}</span>
            </div>
            <div class="card-sep" />
            <div class="card-metrics">
              <div class="cm"><div class="cm-val">{{ (row.loyaltyPoints||0).toLocaleString() }}</div><div class="cm-lbl">Điểm</div></div>
              <div class="cm-div" />
              <div class="cm"><div class="cm-val">{{ formatMoneyShort(row.raw?.totalSpent) }}</div><div class="cm-lbl">Chi tiêu</div></div>
              <div class="cm-div" />
              <div class="cm"><div class="cm-val sm">{{ row.phone || '—' }}</div><div class="cm-lbl">SĐT</div></div>
            </div>
            <div class="card-foot">
              <span class="last-order-txt" :class="{ 'lo-none': !row.raw?.lastOrderAt }">
                {{ row.raw?.lastOrderAt ? 'Đơn hàng cuối: '+relativeDate(row.raw.lastOrderAt) : 'Chưa có đơn hàng' }}
              </span>
              <button class="act-btn" @click="openInactiveDetail(row)">Xem</button>
            </div>
          </div>
        </div>

        <div class="pager-row" v-if="!loading && inactiveFiltered.length>inactivePageSize">
          <button class="pager-btn" :disabled="inactivePage===1" @click="inactivePage--">‹</button>
          <button v-for="p in inactiveTotalPages" :key="p" class="pager-btn" :class="{'pager-active':p===inactivePage}" @click="inactivePage=p">{{ p }}</button>
          <button class="pager-btn" :disabled="inactivePage===inactiveTotalPages" @click="inactivePage++">›</button>
          <span class="pager-info">{{ (inactivePage-1)*inactivePageSize+1 }}–{{ Math.min(inactivePage*inactivePageSize, inactiveFiltered.length) }} / {{ inactiveFiltered.length }}</span>
        </div>
      </template>

    </div><!-- /crm-inner -->

    <!-- ══════════ DIALOGS ══════════ -->

    <!-- CREATE / EDIT -->
    <Teleport to="body">
      <div class="overlay" v-if="dlg.open" @click.self="dlg.open=false">
        <div class="modal modal-lg">
          <div class="modal-hd">
            <h2 class="modal-ttl">{{ dlg.mode==='create' ? 'Khách hàng mới' : 'Sửa khách hàng' }}</h2>
            <button class="modal-x" @click="dlg.open=false">×</button>
          </div>
          <div class="modal-alert" v-if="dlg.alert">{{ dlg.alert }}</div>
          <div class="modal-bd">
            <div class="form-grid">
              <div class="field-group">
                <label class="field-label">Họ và tên <span class="req">*</span></label>
                <input v-model="dlg.form.fullName" class="field-input" placeholder="Nguyen Van A" />
              </div>
              <div class="field-group">
                <label class="field-label">Loại khách hàng</label>
                <select v-model="dlg.form.customerType" class="field-select">
                  <option value="REGULAR">Thường</option>
                  <option value="VIP">VIP</option>
                </select>
              </div>
              <div class="field-group">
                <label class="field-label">Email <span class="req">*</span></label>
                <input v-model="dlg.form.email" type="email" class="field-input" placeholder="email@example.com" />
              </div>
              <div class="field-group">
                <label class="field-label">SĐT <span class="req">*</span></label>
                <input v-model="dlg.form.phone" class="field-input" placeholder="0912345678" />
              </div>
              <div class="field-group">
                <label class="field-label">Ngày sinh</label>
                <input v-model="dlg.form.birthDate" type="date" class="field-input" />
              </div>
              <div class="field-group">
                <label class="field-label">Địa chỉ</label>
                <input v-model="dlg.form.address" class="field-input" placeholder="123 Street, City" />
              </div>
              <div class="field-group full-col">
                <label class="field-label">Ghi chú</label>
                <textarea v-model="dlg.form.notes" class="field-textarea" rows="2" placeholder="General notes…" />
              </div>
              <div class="field-group full-col vip-note-block" v-if="dlg.mode==='edit' && dlg.form.vipTier">
                <div class="vipn-head">
                  <label class="field-label">Ghi chú VIP <span class="internal-tag">Nội bộ · Chỉ admin</span></label>
                  <span class="tier-indicator">{{ tierIcon(dlg.form.vipTier) }} {{ dlg.form.vipTier }}</span>
                </div>
                <textarea v-model="dlg.form.vipNote" class="field-textarea vip-textarea" rows="3" placeholder="e.g. Prefers gaming laptops, no display units…" maxlength="500" />
                <div class="vipn-hint">Khách hàng không thể xem ghi chú này. <span class="char-count">{{ (dlg.form.vipNote||'').length }}/500</span></div>
              </div>
              <div class="field-group full-col" v-if="dlg.mode==='create'">
                <p class="vip-unavail">Ghi chú VIP có sẵn khi khách hàng đạt cấp Đồng trở lên.</p>
              </div>
            </div>
          </div>
          <div class="modal-ft">
            <button class="btn-outline" @click="dlg.open=false">Hủy</button>
            <button class="btn-solid" :disabled="dlg.loading" @click="save">
              <span v-if="dlg.loading" class="spin-icon">↻</span>
              {{ dlg.mode==='create' ? 'Tạo' : 'Lưu thay đổi' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- DETAILS -->
    <Teleport to="body">
      <div class="overlay" v-if="detailsDialog.open" @click.self="detailsDialog.open=false">
        <div class="modal modal-lg" v-if="detailsDialog.customer">
          <div class="modal-hd">
            <div class="details-hd-info">
              <div class="avatar lg" :style="{ background: getAvatarColor(detailsDialog.customer.fullName) }">{{ getInitials(detailsDialog.customer.fullName) }}</div>
              <div>
                <h2 class="modal-ttl">{{ detailsDialog.customer.fullName }}</h2>
                <p class="modal-sub">#{{ detailsDialog.customer.id }} · {{ detailsDialog.customer.email }}</p>
              </div>
            </div>
            <button class="modal-x" @click="detailsDialog.open=false">×</button>
          </div>
          <div class="modal-bd">
            <div class="details-grid">
              <div class="detail-block">
                <h3 class="detail-block-ttl">Thông tin cá nhân</h3>
                <div class="detail-rows">
                  <div class="detail-row"><span class="dk">Mã</span><span class="dv">#{{ detailsDialog.customer.id }}</span></div>
                  <div class="detail-row"><span class="dk">Tên</span><strong class="dv">{{ detailsDialog.customer.fullName }}</strong></div>
                  <div class="detail-row"><span class="dk">Email</span><span class="dv">{{ detailsDialog.customer.email }}</span></div>
                  <div class="detail-row"><span class="dk">SĐT</span><span class="dv">{{ detailsDialog.customer.phone||'—' }}</span></div>
                  <div class="detail-row"><span class="dk">Ngày sinh</span><span class="dv">{{ detailsDialog.customer.birthDate||'—' }}</span></div>
                  <div class="detail-row"><span class="dk">Địa chỉ</span><span class="dv">{{ detailsDialog.customer.address||'—' }}</span></div>
                </div>
              </div>
              <div class="detail-block">
                <h3 class="detail-block-ttl">Loyalty &amp; VIP</h3>
                <div class="detail-rows">
                  <div class="detail-row"><span class="dk">Loại</span><span :class="['badge-type',detailsDialog.customer.customerType==='VIP'?'badge-vip':'badge-reg']">{{ detailsDialog.customer.customerType }}</span></div>
                  <div class="detail-row">
                    <span class="dk">Cấp</span>
                    <span v-if="detailsDialog.customer.raw.vipTier" :class="['badge-tier',`tier-${detailsDialog.customer.raw.vipTier.toLowerCase()}`]">{{ detailsDialog.customer.raw.vipTier }}</span>
                    <span v-else class="muted">Chưa có cấp</span>
                  </div>
                  <div class="detail-row"><span class="dk">Điểm</span><strong class="dv" style="color:#7dd3fc">{{ (detailsDialog.customer.loyaltyPoints||0).toLocaleString() }} điểm</strong></div>
                  <div class="detail-row"><span class="dk">Tổng chi tiêu</span><strong class="dv" style="color:#6ee7b7">{{ formatCurrency(detailsDialog.customer.raw.totalSpent) }}</strong></div>
                </div>
              </div>
              <div class="detail-block" v-if="detailsDialog.customer.notes">
                <h3 class="detail-block-ttl">Ghi chú</h3>
                <p class="detail-body-text">{{ detailsDialog.customer.notes }}</p>
              </div>
              <div class="detail-block vip-note-block" v-if="detailsDialog.customer.raw.vipTier">
                <div class="vipn-head">
                  <h3 class="detail-block-ttl" style="margin:0">Ghi chú VIP <span class="internal-tag">Chỉ nội bộ</span></h3>
                  <button class="btn-ghost-sm" @click="openVipNoteEdit(detailsDialog.customer)">Sửa</button>
                </div>
                <p v-if="detailsDialog.customer.raw.vipNote" class="vip-note-body">{{ detailsDialog.customer.raw.vipNote }}</p>
                <p v-else class="vip-note-empty">Chưa có ghi chú VIP. <button class="btn-link" @click="openVipNoteEdit(detailsDialog.customer)">Thêm một →</button></p>
              </div>
            </div>
          </div>
          <div class="modal-ft">
            <button class="btn-outline" @click="detailsDialog.open=false">Đóng</button>
            <button class="btn-solid" @click="openEdit(detailsDialog.customer)">Sửa khách hàng</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- VIP NOTE DIALOG -->
    <Teleport to="body">
      <div class="overlay" v-if="vipNoteDialog.open" @click.self="vipNoteDialog.open=false">
        <div class="modal modal-sm">
          <div class="modal-hd vip-modal-hd">
            <div>
              <h2 class="modal-ttl">Ghi chú VIP</h2>
              <p class="modal-sub" v-if="vipNoteDialog.customerName">{{ vipNoteDialog.vipTier }} · {{ vipNoteDialog.customerName }}</p>
            </div>
            <button class="modal-x" @click="vipNoteDialog.open=false">×</button>
          </div>
          <div class="modal-bd">
            <div class="vip-notice">
              <strong>Chỉ nội bộ</strong> — Khách hàng không thể xem. Chỉ Admin và Staff có thể xem.
            </div>
            <div class="suggestion-section" v-if="!vipNoteDialog.note">
              <p class="suggestion-label">Gợi ý nhanh</p>
              <div class="chips">
                <button v-for="s in vipSuggestions" :key="s" class="chip" @click="vipNoteDialog.note=s">{{ s }}</button>
              </div>
            </div>
            <textarea v-model="vipNoteDialog.note" class="field-textarea" rows="5" placeholder="Ghi chú nội bộ về khách hàng VIP này…" maxlength="500" ref="vipNoteTextarea" />
            <div class="vipn-foot">
              <span class="char-count">{{ (vipNoteDialog.note||'').length }}/500</span>
              <button v-if="vipNoteDialog.note" class="btn-link" style="color:#f87171" @click="vipNoteDialog.note=''">Xóa</button>
            </div>
          </div>
          <div class="modal-ft">
            <button class="btn-outline" @click="vipNoteDialog.open=false">Hủy</button>
            <button class="btn-gold" :disabled="vipNoteDialog.loading" @click="saveVipNote">
              <span v-if="vipNoteDialog.loading" class="spin-icon">↻</span> Lưu ghi chú VIP
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- LOYALTY SUMMARY -->
    <Teleport to="body">
      <div class="overlay" v-if="summaryDlg.open" @click.self="summaryDlg.open=false">
        <div class="modal modal-xl">
          <div class="modal-hd">
            <div>
              <h2 class="modal-ttl">Tổng điểm</h2>
              <p class="modal-sub">Phân tích điểm khách hàng trên tất cả khách hàng</p>
            </div>
            <button class="modal-x" @click="summaryDlg.open=false">×</button>
          </div>
          <div class="summary-controls">
            <div class="sc-left">
              <div class="ctrl-group">
                <span class="ctrl-lbl">Kỳ</span>
                <div class="toggle-set">
                  <button :class="['tgl',summaryDlg.mode==='monthly'&&'tgl-on']" @click="summaryDlg.mode='monthly'; loadAdminSummary()">Hàng tháng</button>
                  <button :class="['tgl',summaryDlg.mode==='weekly'&&'tgl-on']" @click="summaryDlg.mode='weekly'; loadAdminSummary()">Hàng tuần</button>
                </div>
              </div>
              <div class="ctrl-group">
                <span class="ctrl-lbl">{{ summaryDlg.mode==='weekly' ? 'Tuần' : 'Tháng' }}</span>
                <select class="field-select ctrl-select" v-model="summaryDlg.range" @change="loadAdminSummary">
                  <template v-if="summaryDlg.mode==='weekly'">
                    <option :value="4">4w</option><option :value="8">8w</option><option :value="12">12w</option><option :value="26">26w</option>
                  </template>
                  <template v-else>
                    <option :value="3">3m</option><option :value="6">6m</option><option :value="12">12m</option><option :value="24">24m</option>
                  </template>
                </select>
              </div>
              <div class="ctrl-group">
                <span class="ctrl-lbl">Xem</span>
                <div class="toggle-set">
                  <button :class="['tgl',summaryDlg.view==='overview'&&'tgl-on']" @click="summaryDlg.view='overview'">Tổng quan</button>
                  <button :class="['tgl',summaryDlg.view==='detail'&&'tgl-on']" @click="summaryDlg.view='detail'">Theo khách hàng</button>
                </div>
              </div>
            </div>
            <button class="btn-ghost-sm" @click="loadAdminSummary" :disabled="summaryDlg.loading">
              <span v-if="summaryDlg.loading" class="spin-icon">↻</span> Tải lại
            </button>
          </div>
          <div class="modal-bd summary-bd">
            <div v-if="summaryDlg.loading" class="table-loading"><div class="loader-dots"><span/><span/><span/></div><p>Đang tải…</p></div>
            <div v-else-if="!summaryDlg.data.length" class="empty-state"><p>Không có dữ liệu trong kỳ này</p></div>
            <template v-else>
              <div class="summary-kpis">
                <div class="kpi earn"><div class="kpi-val">+{{ adminSummaryTotals.earned.toLocaleString() }}</div><div class="kpi-lbl">Điểm kiếm được</div></div>
                <div class="kpi deduct"><div class="kpi-val">-{{ adminSummaryTotals.deducted.toLocaleString() }}</div><div class="kpi-lbl">Điểm đã dùng</div></div>
                <div class="kpi net" :class="adminSummaryTotals.net>=0?'kpi-pos':'kpi-neg'"><div class="kpi-val">{{ adminSummaryTotals.net>=0?'+':'' }}{{ adminSummaryTotals.net.toLocaleString() }}</div><div class="kpi-lbl">Lý thuyết</div></div>
                <div class="kpi tx"><div class="kpi-val">{{ adminSummaryTotals.transactions.toLocaleString() }}</div><div class="kpi-lbl">Giao dịch</div></div>
              </div>
              <template v-if="summaryDlg.view==='overview'">
                <div class="period-list">
                  <div v-for="period in summaryDlg.data" :key="period.period" class="period-row">
                    <div class="pr-meta">
                      <div class="pr-label">{{ period.periodLabel }}</div>
                      <div class="pr-dates">{{ formatAdminDate(period.periodStart) }} – {{ formatAdminDate(period.periodEnd) }}</div>
                    </div>
                    <div class="pr-bars">
                      <div class="bar-row-h">
                        <span class="br-num earn-num">+{{ period.totalPointsEarned.toLocaleString() }}</span>
                        <div class="bar-bg"><div class="bar-fg bar-earn" :style="{width:getAdminBarWidth(period.totalPointsEarned,'earn')+'%'}" /></div>
                      </div>
                      <div class="bar-row-h">
                        <span class="br-num deduct-num">-{{ period.totalPointsDeducted.toLocaleString() }}</span>
                        <div class="bar-bg"><div class="bar-fg bar-deduct" :style="{width:getAdminBarWidth(period.totalPointsDeducted,'deduct')+'%'}" /></div>
                      </div>
                    </div>
                    <div class="pr-net">
                      <span class="net-chip" :class="period.netPoints>=0?'nc-pos':'nc-neg'">{{ period.netPoints>=0?'+':'' }}{{ period.netPoints.toLocaleString() }}</span>
                      <span class="pr-tx">{{ period.totalTransactions }} gd</span>
                    </div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div v-for="period in summaryDlg.data" :key="period.period" class="cust-period-block">
                  <div class="cpb-head">
                    <span class="cpb-title">{{ period.periodLabel }}</span>
                    <div class="cpb-meta">
                      <span class="earn-num">+{{ period.totalPointsEarned.toLocaleString() }}</span>
                      <span class="muted">/</span>
                      <span class="deduct-num">-{{ period.totalPointsDeducted.toLocaleString() }}</span>
                      <span class="muted tx-count">{{ period.totalTransactions }} txn</span>
                    </div>
                  </div>
                  <table class="crm-table cdt" v-if="period.customerBreakdown?.length">
                    <thead><tr><th>Khách hàng</th><th>Cấp</th><th style="text-align:right">Kiếm được</th><th style="text-align:right">Đã dùng</th><th style="text-align:right">Lý thuyết</th><th style="text-align:center">Gd</th></tr></thead>
                    <tbody>
                      <tr v-for="c in period.customerBreakdown" :key="c.customerId" class="crm-row">
                        <td><div class="cdt-name">{{ c.customerName }}</div><div class="cdt-email">{{ c.customerEmail }}</div></td>
                        <td><span v-if="c.vipTier&&c.vipTier!=='Member'" :class="['badge-tier',`tier-${c.vipTier.toLowerCase()}`]">{{ c.vipTier }}</span><span v-else class="muted">—</span></td>
                        <td style="text-align:right" class="earn-num">+{{ c.pointsEarned.toLocaleString() }}</td>
                        <td style="text-align:right" class="deduct-num">{{ c.pointsDeducted>0?'-':'' }}{{ c.pointsDeducted.toLocaleString() }}</td>
                        <td style="text-align:right"><span :style="{color:c.netPoints>=0?'#6ee7b7':'#f87171',fontWeight:700}">{{ c.netPoints>=0?'+':'' }}{{ c.netPoints.toLocaleString() }}</span></td>
                        <td style="text-align:center" class="muted">{{ c.transactionCount }}</td>
                      </tr>
                    </tbody>
                  </table>
                  <div v-else class="cdt-empty">Không có giao dịch</div>
                </div>
              </template>
            </template>
          </div>
          <div class="modal-ft"><button class="btn-outline" @click="summaryDlg.open=false">Đóng</button></div>
        </div>
      </div>
    </Teleport>

    <!-- GREETING DIALOG -->
    <Teleport to="body">
      <div class="overlay" v-if="greetingDialog.open" @click.self="greetingDialog.open=false">
        <div class="modal modal-sm">
          <div class="modal-hd">
            <h2 class="modal-ttl">Gửi lời chúc sinh nhật</h2>
            <button class="modal-x" @click="greetingDialog.open=false">×</button>
          </div>
          <div class="modal-bd">
            <div class="bd-banner bd-blue" style="margin-bottom:14px" v-if="greetingDialog.customer">
              To: <strong>{{ greetingDialog.customer.name }}</strong> ({{ greetingDialog.customer.email }})
            </div>
            <div class="field-group">
              <label class="field-label">Tin nhắn</label>
              <textarea v-model="greetingDialog.form.message" class="field-textarea" rows="8" placeholder="Viết lời chúc sinh nhật…" />
            </div>
          </div>
          <div class="modal-ft">
            <button class="btn-outline" @click="greetingDialog.open=false">Hủy</button>
            <button class="btn-solid" :disabled="greetingDialog.sending" @click="confirmSendGreeting">
              <span v-if="greetingDialog.sending" class="spin-icon">↻</span> Gửi lời chúc
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- INACTIVE DETAIL -->
    <Teleport to="body">
      <div class="overlay" v-if="inactiveDetail.open" @click.self="inactiveDetail.open=false">
        <div class="modal modal-sm text-center" v-if="inactiveDetail.customer">
          <button class="modal-x" @click="inactiveDetail.open=false">×</button>
          <div class="modal-bd">
            <div class="avatar xl mx-auto" :style="{background:getAvatarColor(inactiveDetail.customer.fullName)}">{{ getInitials(inactiveDetail.customer.fullName) }}</div>
            <div class="modal-name">{{ inactiveDetail.customer.fullName }}</div>
            <div class="muted" style="font-size:13px;margin-bottom:12px">{{ inactiveDetail.customer.email }}</div>
            <span v-if="inactiveDetail.customer.raw?.vipTier" :class="['badge-tier',`tier-${inactiveDetail.customer.raw.vipTier.toLowerCase()}`]" style="font-size:13px;padding:4px 12px">{{ inactiveDetail.customer.raw.vipTier }}</span>
            <div class="modal-stat-row">
              <div class="ms"><div class="ms-val gold">{{ (inactiveDetail.customer.loyaltyPoints||0).toLocaleString() }}</div><div class="ms-lbl">Điểm</div></div>
              <div class="ms"><div class="ms-val green">{{ formatCurrency(inactiveDetail.customer.raw?.totalSpent) }}</div><div class="ms-lbl">Chi tiêu</div></div>
            </div>
            <div class="detail-rows" style="margin-top:16px;text-align:left">
              <div class="detail-row"><span class="dk">SĐT</span><span class="dv">{{ inactiveDetail.customer.phone||'—' }}</span></div>
              <div class="detail-row"><span class="dk">Ngày sinh</span><span class="dv">{{ inactiveDetail.customer.birthDate||'—' }}</span></div>
              <div class="detail-row"><span class="dk">Địa chỉ</span><span class="dv">{{ inactiveDetail.customer.raw?.address||'—' }}</span></div>
              <div class="detail-row"><span class="dk">Đơn hàng cuối</span><span class="dv" style="color:#fb923c">{{ relativeDate(inactiveDetail.customer.raw?.lastOrderAt) }}</span></div>
              <div class="detail-row" v-if="inactiveDetail.customer.notes"><span class="dk">Ghi chú</span><span class="dv">{{ inactiveDetail.customer.notes }}</span></div>
            </div>
            <div class="inactive-warning">
              Không có giao dịch trong <strong>{{ activeInactiveTab }}+ ngày</strong>. Cân nhắc liên hệ lại.
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
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

/* ═══════════════════════════
   ROOT & LAYOUT
═══════════════════════════ */
.crm-root {
  font-family: 'Inter', system-ui, sans-serif;
  background: #ffffff;
  min-height: 100vh;
  padding: 40px 32px;
  color: #1e293b;
}
.crm-inner {
  max-width: 1440px;
  margin: 0 auto;
}

/* ═══════════════════════════
   HEADER
═══════════════════════════ */
.crm-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 36px;
  padding: 32px 36px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
}
.header-eyebrow {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: #4b5563;
  margin-bottom: 10px;
}
.header-title {
  font-family: 'Inter', sans-serif;
  font-size: 38px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.1;
  letter-spacing: -0.5px;
}
.title-line {
  color: #7dd3fc;
}
.header-meta {
  font-size: 13px;
  color: #4b5563;
  margin-top: 8px;
  font-weight: 400;
  letter-spacing: 0.3px;
}
.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* ═══════════════════════════
   BUTTONS
═══════════════════════════ */
.btn-solid {
  background: #f8fafc;
  color: #0a0c10;
  border: none;
  padding: 9px 20px;
  border-radius: 2px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  transition: all 0.15s;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.2px;
}
.btn-solid:hover { background: #cbd5e1; }
.btn-solid:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-solid.btn-sm { padding: 6px 14px; font-size: 12px; }

.btn-outline {
  background: transparent;
  color: #94a3b8;
  border: 1px solid #e5e7eb;
  padding: 9px 20px;
  border-radius: 2px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  transition: all 0.15s;
  font-family: 'Inter', sans-serif;
}
.btn-outline:hover { border-color: #334155; color: #1e293b; }
.btn-outline:disabled { opacity: 0.3; cursor: not-allowed; }

.btn-gold {
  background: linear-gradient(135deg, #d97706, #b45309);
  color: #fff;
  border: none;
  padding: 9px 20px;
  border-radius: 2px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  transition: all 0.15s;
  font-family: 'Inter', sans-serif;
}
.btn-gold:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-link {
  background: none;
  border: none;
  color: #7dd3fc;
  font-size: 12px;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  text-decoration: underline;
  padding: 0;
}
.btn-ghost-sm {
  background: transparent;
  color: #64748b;
  border: 1px solid #e5e7eb;
  padding: 5px 11px;
  border-radius: 2px;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.12s;
  font-family: 'Inter', sans-serif;
}
.btn-ghost-sm:hover { border-color: #334155; color: #94a3b8; }
.btn-ghost-sm:disabled { opacity: 0.3; cursor: not-allowed; }

/* ═══════════════════════════
   NAV TABS
═══════════════════════════ */
.crm-nav {
  display: flex;
  gap: 0;
  margin-bottom: 28px;
  border-bottom: 1px solid #1e2330;
  position: relative;
}
.crm-nav-tab {
  background: transparent;
  border: none;
  color: #4b5563;
  padding: 13px 24px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.3px;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.15s;
}
.crm-nav-tab:hover { color: #94a3b8; }
.crm-nav-tab.active {
  color: #1e293b;
  border-bottom-color: #7dd3fc;
}

/* ═══════════════════════════
   STATS GRID
═══════════════════════════ */
.stats-grid {
  display: flex;
  gap: 1px;
  margin-bottom: 24px;
  background: #1e2330;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}
.stat-card {
  flex: 1;
  background: #f8fafc;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stat-label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: #334155;
}
.stat-value {
  font-family: 'Inter', sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
  letter-spacing: -0.5px;
}
.accent-gold { color: #fbbf24; }
.accent-blue { color: #7dd3fc; }
.accent-teal { color: #6ee7b7; }

/* ═══════════════════════════
   FILTER PANEL
═══════════════════════════ */
.filter-panel {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 20px 24px;
  margin-bottom: 20px;
}
.filter-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.filter-panel-title {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: #334155;
}
.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.filter-grid-points {
  grid-template-columns: 1fr 1fr auto;
  margin-top: 16px;
}
.filter-divider { height: 1px; background: #1a1f2b; margin: 16px 0 0; }
.field-group { display: flex; flex-direction: column; gap: 6px; }
.field-group-actions { display: flex; flex-direction: row; gap: 8px; align-items: flex-end; }
.field-label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: #374151;
}
.req { color: #f87171; }
.field-input, .field-select, .field-textarea {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 2px;
  padding: 9px 12px;
  font-size: 13px;
  color: #1e293b;
  font-family: 'Inter', sans-serif;
  outline: none;
  transition: border-color 0.15s;
  width: 100%;
}
.field-input::placeholder { color: #2d3748; }
.field-input:focus, .field-select:focus, .field-textarea:focus {
  border-color: #334155;
  background: #0d0f14;
}
.field-select { cursor: pointer; }
.field-select option { background: #f8fafc; }
.field-textarea { resize: vertical; min-height: 80px; }

.active-filters {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #1a1f2b;
}
.af-label { font-size: 10px; font-weight: 700; letter-spacing: 1px; color: #374151; text-transform: uppercase; }
.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #161b25;
  border: 1px solid #e5e7eb;
  color: #94a3b8;
  padding: 3px 8px 3px 10px;
  border-radius: 2px;
  font-size: 11px;
  font-weight: 500;
}
.chip-remove { background: none; border: none; color: #4b5563; cursor: pointer; font-size: 14px; padding: 0; margin-left: 2px; line-height: 1; }
.chip-remove:hover { color: #1e293b; }

/* ═══════════════════════════
   TABLE
═══════════════════════════ */
.table-panel {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}
.table-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  gap: 14px;
  color: #2d3748;
  font-size: 13px;
}
.loader-dots { display: flex; gap: 6px; }
.loader-dots span { width: 7px; height: 7px; border-radius: 50%; background: #7dd3fc; animation: db 1.2s infinite; }
.loader-dots span:nth-child(2) { animation-delay: 0.2s; }
.loader-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes db { 0%,80%,100%{transform:scale(0.7);opacity:0.3} 40%{transform:scale(1);opacity:1} }

.table-scroll { overflow-x: auto; }
.crm-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.crm-table thead tr { border-bottom: 1px solid #1a1f2b; }
.crm-table th {
  padding: 11px 14px;
  text-align: left;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  color: #2d3748;
  white-space: nowrap;
  background: #ffffff;
}
.crm-row { border-bottom: 1px solid #0d0f14; transition: background 0.1s; }
.crm-row:hover { background: #11141c; }
.crm-row:last-child { border-bottom: none; }
.crm-table td { padding: 11px 14px; vertical-align: middle; }

.id-mono { font-family: 'Inter', monospace; font-size: 11px; color: #2d3748; }
.customer-cell { display: flex; align-items: center; gap: 10px; }
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255,255,255,0.9);
  font-family: 'Inter', sans-serif;
  font-weight: 700;
  font-size: 11px;
  flex-shrink: 0;
}
.avatar.lg { width: 44px; height: 44px; font-size: 15px; border-radius: 4px; }
.avatar.xl { width: 64px; height: 64px; font-size: 20px; border-radius: 4px; margin-bottom: 14px; }
.mx-auto { margin: 0 auto; }
.cust-info { display: flex; flex-direction: column; gap: 1px; }
.cust-name { font-weight: 500; color: #1e293b; font-size: 13px; }
.cust-dob { font-size: 11px; color: #2d3748; font-family: 'Inter', monospace; }
.contact-cell { display: flex; flex-direction: column; gap: 2px; }
.contact-email { color: #64748b; font-size: 12px; }
.contact-phone { color: #334155; font-size: 11px; font-family: 'Inter', monospace; }
.muted { color: #1e2a38; font-size: 12px; }

/* Badges */
.badge-type {
  padding: 2px 8px;
  border-radius: 2px;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
}
.badge-vip { background: #1c1500; color: #fbbf24; border: 1px solid #2d1f00; }
.badge-reg { background: #111827; color: #374151; border: 1px solid #e5e7eb; }

.badge-tier {
  padding: 2px 9px;
  border-radius: 2px;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  white-space: nowrap;
}
.tier-bronze { background: #1a0f00; color: #cd7f32; border: 1px solid #2a1a00; }
.tier-silver { background: #111827; color: #94a3b8; border: 1px solid #e5e7eb; }
.tier-gold { background: #1c1500; color: #fbbf24; border: 1px solid #2d1f00; }
.tier-platinum { background: #0d0f1e; color: #818cf8; border: 1px solid #1a1c3a; }
.tier-diamond { background: #031a12; color: #34d399; border: 1px solid #052e20; }

.points-cell { display: flex; flex-direction: column; gap: 4px; }
.points-num { font-family: 'Inter', monospace; font-size: 12px; font-weight: 500; color: #7dd3fc; }
.points-track { height: 2px; background: #1a1f2b; width: 70px; border-radius: 1px; overflow: hidden; }
.points-fill { height: 100%; border-radius: 1px; transition: width 0.3s; }
.spent-val { font-family: 'Inter', monospace; font-size: 11px; color: #374151; }

/* VIP Note cell */
.vipnote-cell {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 2px;
  background: #160e00;
  border: 1px solid #2a1a00;
  max-width: 180px;
  transition: all 0.12s;
}
.vipnote-cell:hover { background: #1e1500; }
.vipnote-text { font-size: 11px; color: #a16207; line-height: 1.4; word-break: break-word; }
.note-text { font-size: 11px; color: #334155; }

/* Action buttons */
.action-row { display: flex; gap: 4px; }
.act-btn {
  background: transparent;
  border: 1px solid #1a1f2b;
  color: #374151;
  padding: 4px 10px;
  border-radius: 2px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  text-transform: uppercase;
  transition: all 0.12s;
  font-family: 'Inter', sans-serif;
}
.act-btn:hover { border-color: #334155; color: #94a3b8; }
.act-gold { color: #92400e; border-color: #2a1a00; }
.act-gold:hover { background: #160e00; color: #fbbf24; border-color: #fbbf24; }
.act-danger:hover { border-color: #7f1d1d; color: #f87171; }

.empty-row { padding: 60px; text-align: center; }
.empty-msg { color: #1e2330; font-size: 13px; }

/* Table footer */
.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid #0d0f14;
  background: #ffffff;
  flex-wrap: wrap;
  gap: 8px;
}
.pager-info { font-size: 11px; color: #1e2a38; font-family: 'Inter', monospace; }
.pager { display: flex; gap: 3px; }
.pager-btn {
  min-width: 30px;
  height: 30px;
  border: 1px solid #1a1f2b;
  background: transparent;
  border-radius: 2px;
  font-size: 12px;
  cursor: pointer;
  color: #374151;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Inter', monospace;
  transition: all 0.12s;
  padding: 0 6px;
}
.pager-btn:hover:not(:disabled) { border-color: #334155; color: #94a3b8; }
.pager-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.pager-active { background: #f8fafc !important; color: #0a0c10 !important; border-color: #f8fafc !important; font-weight: 700; }

/* ═══════════════════════════
   BIRTHDAY SUBTAB
═══════════════════════════ */
.sub-nav {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #1e2330;
  margin-bottom: 0;
}
.sub-nav-btn {
  background: transparent;
  border: none;
  color: #334155;
  padding: 11px 20px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 0.5px;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.12s;
  font-family: 'Inter', sans-serif;
  text-transform: uppercase;
  font-size: 10px;
  letter-spacing: 1px;
}
.sub-nav-btn:hover { color: #64748b; }
.sub-nav-btn.active { color: #1e293b; border-bottom-color: #1e293b; }

.bd-banner {
  padding: 10px 16px;
  font-size: 12px;
  font-weight: 500;
  margin: 14px 16px;
  border-radius: 2px;
}
.bd-green { background: #031a0e; border: 1px solid #052e20; color: #34d399; }
.bd-blue { background: #040e1c; border: 1px solid #0a1e38; color: #7dd3fc; }
.bd-amber { background: #160a00; border: 1px solid #2a1400; color: #fbbf24; }
.bd-controls { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-bottom: 1px solid #0d0f14; }
.bar-track-h { background: #1a1f2b; border-radius: 2px; height: 16px; overflow: hidden; }
.bar-fill-h { background: #7dd3fc; height: 100%; border-radius: 2px; min-width: 2px; transition: width 0.4s; }

/* ═══════════════════════════
   INACTIVE / SEGMENTS
═══════════════════════════ */
.seg-tabs {
  display: flex;
  gap: 0;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
  background: #1e2330;
}
.seg-btn {
  flex: 1;
  background: #ffffff;
  border: none;
  color: #334155;
  padding: 14px 20px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: 'Inter', sans-serif;
  transition: all 0.12s;
  letter-spacing: 0.5px;
}
.seg-btn:hover { background: #0d0f14; color: #64748b; }
.seg-btn.active { background: #f8fafc; color: #1e293b; }
.seg-label { font-size: 12px; font-weight: 500; }
.seg-count {
  padding: 2px 8px;
  border-radius: 2px;
  font-size: 11px;
  font-weight: 700;
  background: #1e2330;
  color: #374151;
  font-family: 'Inter', monospace;
  transition: all 0.2s;
}
.seg-count.loaded { background: #f8fafc; color: #0a0c10; }

.inactive-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  margin-bottom: 20px;
  background: #1e2330;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}
.ist-card { background: #f8fafc; padding: 18px 22px; }
.ist-val { font-family: 'Inter', sans-serif; font-size: 24px; font-weight: 700; color: #1e293b; }
.ist-val.gold { color: #fbbf24; }
.ist-val.green { color: #6ee7b7; font-size: 18px; }
.ist-val.blue { color: #7dd3fc; }
.ist-lbl { font-size: 10px; letter-spacing: 1px; text-transform: uppercase; color: #2d3748; font-weight: 600; margin-top: 4px; }

.filter-bar { display: flex; gap: 10px; margin-bottom: 20px; align-items: center; }

.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}
.cust-card {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.2s;
  animation: fadeUp 0.35s ease both;
}
@keyframes fadeUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.cust-card:hover { border-color: #2d3748; background: #11141c; }
.card-tier-bar { height: 2px; width: 100%; }
.cb-bronze { background: #cd7f32; } .cb-silver { background: #94a3b8; } .cb-gold { background: #fbbf24; } .cb-platinum { background: #818cf8; } .cb-diamond { background: #34d399; } .cb-none { background: #1e2330; }
.card-head { display: flex; align-items: center; gap: 10px; padding: 14px; }
.card-info { flex: 1; min-width: 0; }
.card-name { font-size: 13px; font-weight: 500; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-email { font-size: 11px; color: #334155; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 2px; }
.card-sep { height: 1px; background: #0d0f14; margin: 0 14px; }
.card-metrics { display: flex; align-items: center; justify-content: space-around; padding: 12px 14px; }
.cm { display: flex; flex-direction: column; align-items: center; gap: 3px; }
.cm-val { font-family: 'Inter', monospace; font-size: 13px; font-weight: 500; color: #94a3b8; }
.cm-val.sm { font-size: 11px; color: #334155; }
.cm-lbl { font-size: 9px; color: #2d3748; text-transform: uppercase; letter-spacing: 0.8px; font-weight: 600; }
.cm-div { width: 1px; height: 28px; background: #0d0f14; }
.card-foot { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px 14px; border-top: 1px solid #0d0f14; }
.last-order-txt { font-size: 11px; color: #334155; }
.lo-none { color: #7f1d1d; }

.empty-state { text-align: center; padding: 80px 20px; }
.empty-title { font-family: 'Inter', sans-serif; font-size: 22px; font-weight: 700; color: #1e2330; margin-bottom: 8px; }
.empty-sub { color: #1e2330; font-size: 13px; }

.pager-row { display: flex; justify-content: center; align-items: center; gap: 4px; margin-bottom: 24px; }

/* ═══════════════════════════
   MODALS
═══════════════════════════ */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.modal {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
}
.modal-lg { max-width: 760px; }
.modal-sm { max-width: 480px; }
.modal-xl { max-width: 1000px; }
.text-center { text-align: center; }
.modal-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #1a1f2b;
  flex-shrink: 0;
}
.modal-ttl {
  font-family: 'Inter', sans-serif;
  font-size: 19px;
  font-weight: 700;
  color: #1e293b;
}
.modal-sub { font-size: 11px; color: #374151; margin-top: 3px; font-family: 'Inter', monospace; }
.modal-x {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #374151;
  line-height: 1;
  padding: 2px 4px;
  transition: color 0.1s;
}
.modal-x:hover { color: #94a3b8; }
.modal-alert {
  background: #1a0a0a;
  border-left: 2px solid #f87171;
  color: #f87171;
  padding: 10px 24px;
  font-size: 12px;
}
.modal-bd { padding: 20px 24px; flex: 1; }
.modal-ft {
  padding: 14px 24px;
  border-top: 1px solid #1a1f2b;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-shrink: 0;
}
.modal-name { font-family: 'Inter', sans-serif; font-size: 20px; font-weight: 700; color: #1e293b; margin-bottom: 4px; }

/* Forms */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.full-col { grid-column: 1 / -1; }

/* VIP note form */
.vip-note-block {
  border: 1px solid #2a1a00;
  border-radius: 2px;
  padding: 14px;
  background: #0d0800;
}
.vipn-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.internal-tag {
  display: inline-block;
  background: #1a0f00;
  color: #78350f;
  border: 1px solid #2a1a00;
  padding: 1px 7px;
  border-radius: 2px;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  margin-left: 8px;
}
.tier-indicator { font-size: 11px; color: #92400e; font-weight: 600; font-family: 'Inter', monospace; }
.vip-textarea { border-color: #2a1a00; background: #0a0600; color: #d97706; }
.vip-textarea:focus { border-color: #92400e; }
.vipn-hint { font-size: 10px; color: #374151; margin-top: 6px; display: flex; justify-content: space-between; align-items: center; }
.char-count { font-family: 'Inter', monospace; font-size: 10px; color: #2d3748; }
.vip-unavail { font-size: 11px; color: #2d3748; padding: 10px 14px; border: 1px solid #e5e7eb; border-radius: 2px; background: #ffffff; }

/* Details */
.details-hd-info { display: flex; align-items: center; gap: 12px; }
.details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.detail-block { border: 1px solid #1a1f2b; border-radius: 2px; overflow: hidden; }
.detail-block-ttl {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: #2d3748;
  background: #ffffff;
  padding: 10px 14px;
  border-bottom: 1px solid #1a1f2b;
}
.detail-rows { padding: 6px 14px; }
.detail-row { display: flex; justify-content: space-between; align-items: center; padding: 7px 0; border-bottom: 1px solid #0d0f14; font-size: 12px; }
.detail-row:last-child { border-bottom: none; }
.dk { color: #2d3748; font-weight: 500; }
.dv { color: #94a3b8; text-align: right; font-size: 12px; }
.detail-body-text { padding: 12px 14px; font-size: 12px; color: #4b5563; }
.vip-note-body { padding: 12px 14px; font-size: 12px; color: #92400e; line-height: 1.6; }
.vip-note-empty { padding: 12px 14px; font-size: 12px; color: #374151; font-style: italic; }
.vipn-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; }

/* VIP Note modal */
.vip-modal-hd { background: #0d0800; border-bottom-color: #2a1a00; }
.vip-notice {
  background: #0d0800;
  border: 1px solid #2a1a00;
  border-radius: 2px;
  padding: 10px 14px;
  font-size: 12px;
  color: #78350f;
  margin-bottom: 14px;
  line-height: 1.5;
}
.suggestion-section { margin-bottom: 12px; }
.suggestion-label { font-size: 9px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: #2d3748; margin-bottom: 8px; }
.chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  padding: 4px 10px;
  border-radius: 2px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.12s;
  font-family: 'Inter', sans-serif;
}
.chip:hover { border-color: #2a1a00; color: #92400e; background: #0d0800; }

/* Summary dialog */
.summary-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  border-bottom: 1px solid #1a1f2b;
  background: #ffffff;
  gap: 12px;
  flex-wrap: wrap;
}
.sc-left { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
.ctrl-group { display: flex; align-items: center; gap: 8px; }
.ctrl-lbl { font-size: 10px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: #2d3748; white-space: nowrap; }
.ctrl-select { width: auto; min-width: 80px; }
.toggle-set { display: flex; border: 1px solid #e5e7eb; border-radius: 2px; overflow: hidden; }
.tgl {
  padding: 6px 14px;
  font-size: 11px;
  font-weight: 600;
  border: none;
  background: #ffffff;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  color: #374151;
  transition: all 0.12s;
  letter-spacing: 0.3px;
}
.tgl:hover { background: #0d0f14; color: #64748b; }
.tgl-on { background: #f8fafc !important; color: #0a0c10 !important; font-weight: 700; }
.summary-bd { max-height: 60vh; overflow-y: auto; }
.summary-kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  margin-bottom: 24px;
  background: #1e2330;
  border: 1px solid #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}
.kpi { background: #ffffff; padding: 16px 18px; }
.kpi-val { font-family: 'Inter', sans-serif; font-size: 22px; font-weight: 700; color: #1e293b; }
.kpi-lbl { font-size: 9px; letter-spacing: 1.2px; text-transform: uppercase; color: #2d3748; font-weight: 600; margin-top: 4px; }
.earn .kpi-val { color: #6ee7b7; } .deduct .kpi-val { color: #fb923c; } .kpi-pos .kpi-val { color: #7dd3fc; } .kpi-neg .kpi-val { color: #f87171; } .tx .kpi-val { color: #a78bfa; }
.earn-num { color: #6ee7b7; font-weight: 600; }
.deduct-num { color: #fb923c; font-weight: 600; }

.period-list { display: flex; flex-direction: column; gap: 6px; }
.period-row {
  display: grid;
  grid-template-columns: 150px 1fr 110px;
  gap: 16px;
  align-items: center;
  background: #ffffff;
  border: 1px solid #1a1f2b;
  border-radius: 2px;
  padding: 12px 16px;
  transition: background 0.1s;
}
.period-row:hover { background: #0d0f14; }
.pr-label { font-weight: 600; font-size: 13px; color: #1e293b; }
.pr-dates { font-size: 10px; color: #2d3748; font-family: 'Inter', monospace; margin-top: 2px; }
.pr-bars { display: flex; flex-direction: column; gap: 5px; }
.bar-row-h { display: flex; align-items: center; gap: 8px; }
.br-num { font-size: 11px; font-weight: 600; font-family: 'Inter', monospace; width: 80px; text-align: right; }
.bar-bg { flex: 1; height: 6px; background: #1a1f2b; border-radius: 1px; overflow: hidden; }
.bar-fg { height: 100%; border-radius: 1px; transition: width 0.4s ease; min-width: 2px; }
.bar-earn { background: #6ee7b7; } .bar-deduct { background: #fb923c; }
.pr-net { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.net-chip {
  font-family: 'Inter', monospace;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 2px;
}
.nc-pos { background: #031a0e; color: #6ee7b7; border: 1px solid #052e20; }
.nc-neg { background: #1a0a0a; color: #f87171; border: 1px solid #2d1010; }
.pr-tx { font-size: 10px; color: #2d3748; font-family: 'Inter', monospace; }

.cust-period-block { margin-bottom: 16px; border: 1px solid #1a1f2b; border-radius: 2px; overflow: hidden; }
.cpb-head { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px; background: #ffffff; border-bottom: 1px solid #1a1f2b; }
.cpb-title { font-weight: 600; font-size: 12px; color: #94a3b8; font-family: 'Inter', monospace; }
.cpb-meta { display: flex; align-items: center; gap: 8px; font-size: 11px; font-family: 'Inter', monospace; }
.tx-count { background: #1e2330; color: #374151; padding: 1px 6px; border-radius: 2px; }
.crm-table.cdt .cdt-name { font-size: 12px; font-weight: 500; color: #94a3b8; }
.crm-table.cdt .cdt-email { font-size: 10px; color: #2d3748; }
.cdt-empty { padding: 14px; text-align: center; color: #1e2330; font-size: 12px; }
.mono-sm { font-family: 'Inter', monospace; font-size: 11px; color: #334155; }
.td-truncate { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Inactive detail modal */
.modal-stat-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin: 16px 0; }
.ms { background: #ffffff; border: 1px solid #e5e7eb; border-radius: 2px; padding: 14px; text-align: center; }
.ms-val { font-family: 'Inter', sans-serif; font-size: 18px; font-weight: 700; color: #1e293b; margin-bottom: 4px; }
.ms-val.gold { color: #fbbf24; } .ms-val.green { color: #6ee7b7; font-size: 14px; }
.ms-lbl { font-size: 10px; color: #2d3748; text-transform: uppercase; letter-spacing: 0.8px; font-weight: 600; }
.inactive-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #1a0a0a;
  border: 1px solid #2d1010;
  border-radius: 2px;
  font-size: 12px;
  color: #f87171;
  margin-top: 14px;
  text-align: left;
}

/* Misc */
.spin-icon { display: inline-block; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 1100px) { .cards-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .crm-root { padding: 16px; }
  .form-grid { grid-template-columns: 1fr; }
  .full-col { grid-column: 1; }
  .details-grid { grid-template-columns: 1fr; }
  .filter-grid { grid-template-columns: 1fr 1fr; }
  .filter-grid-points { grid-template-columns: 1fr 1fr; }
  .stats-grid { flex-wrap: wrap; }
  .inactive-stats { grid-template-columns: 1fr 1fr; }
  .summary-kpis { grid-template-columns: 1fr 1fr; }
  .cards-grid { grid-template-columns: 1fr; }
  .period-row { grid-template-columns: 1fr; }
  .seg-tabs { flex-wrap: wrap; }
}
</style>