<template>
  <div class="scm-root">

    <!-- ══════════════════════════════════════
         HEADER
    ══════════════════════════════════════ -->
    <div class="page-header">
      <div class="page-header__left">
        <div class="page-eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          Quản lý khách hàng
        </div>
        <h1 class="page-title">Khách hàng tại quầy</h1>
        <p class="page-sub">Tìm khách hàng hiện có hoặc đăng ký mới khi mua tại cửa hàng</p>
      </div>
      <div class="page-header__actions">
        <button class="hdr-btn hdr-btn--amber" @click="openCreateModal">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Đăng ký khách mới
        </button>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         MAIN CONTENT (2 columns)
    ══════════════════════════════════════ -->
    <div class="scm-body">

      <!-- ── LEFT: Search panel ── -->
      <div class="panel-left">

        <!-- Search box -->
        <div class="search-card">
          <div class="search-card__title">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35" stroke-linecap="round"/>
            </svg>
            Tìm khách hàng
          </div>
          <div class="search-bar">
            <div class="search-wrap" :class="{ focused: searchFocused }">
              <svg class="s-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>
              </svg>
              <input
                ref="searchInput"
                v-model="query"
                class="s-input"
                placeholder="Tên / SĐT / Email..."
                autocomplete="off"
                spellcheck="false"
                @focus="searchFocused = true"
                @blur="searchFocused = false"
                @keyup.enter="doSearch"
                @input="onQueryInput"
              />
              <button v-if="query" class="s-clear" @click="resetSearch" tabindex="-1">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                  <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                  <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
            <button class="s-btn" @click="doSearch" :disabled="searchLoading || !query.trim()">
              <span v-if="!searchLoading">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="1.8"/>
                  <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
                Tìm
              </span>
              <span v-else class="spinner-sm"></span>
            </button>
          </div>

          <!-- Search error -->
          <transition name="fade-slide">
            <div v-if="searchError" class="error-bar">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/>
                <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke="currentColor" stroke-width="1"/>
              </svg>
              {{ searchError }}
            </div>
          </transition>
        </div>

        <!-- Search results list -->
        <div v-if="searchDone" class="results-section">

          <!-- No results → prompt to create -->
          <div v-if="!searchResults.length && !searchLoading" class="no-result-card">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.4">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <p>Không tìm thấy khách hàng với "<strong>{{ lastQuery }}</strong>"</p>
            <span>Khách hàng chưa có tài khoản?</span>
            <button class="hdr-btn hdr-btn--amber" style="margin-top:4px" @click="openCreateModal(lastQuery)">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              Đăng ký ngay
            </button>
          </div>

          <!-- Result list -->
          <div v-else class="result-list">
            <div class="result-meta">
              <strong>{{ searchResults.length }}</strong> kết quả cho "{{ lastQuery }}"
            </div>
            <div
              v-for="cust in searchResults"
              :key="cust.id"
              class="result-row"
              :class="{ 'result-row--selected': selected?.id === cust.id }"
              @click="selectCustomer(cust)"
            >
              <div class="r-avatar" :class="cust.customerType === 'VIP' ? 'r-avatar--vip' : ''">
                {{ initials(cust.fullName) }}
              </div>
              <div class="r-info">
                <div class="r-name">{{ cust.fullName }}</div>
                <div class="r-meta">
                  <span v-if="cust.phone">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.89 12 19.79 19.79 0 0 1 1.87 3.18 2 2 0 0 1 3.84 1h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 8.91a16 16 0 0 0 6 6l.9-1.04a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                    </svg>
                    {{ cust.phone }}
                  </span>
                  <span v-if="cust.email">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                      <polyline points="22,6 12,13 2,6"/>
                    </svg>
                    {{ cust.email }}
                  </span>
                </div>
              </div>
              <div class="r-right">
                <span v-if="cust.customerType === 'VIP'" class="vip-badge">VIP</span>
                <span class="r-points">{{ (cust.loyaltyPoints ?? 0).toLocaleString() }} pts</span>
              </div>
            </div>
          </div>

        </div>

        <!-- Initial empty state -->
        <div v-else class="empty-hint">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#e2e8f0" stroke-width="1.3">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35" stroke-linecap="round"/>
          </svg>
          <span>Nhập tên, SĐT hoặc email để tìm kiếm</span>
        </div>
      </div>

      <!-- ── RIGHT: Selected customer detail ── -->
      <div class="panel-right">

        <!-- No selection -->
        <div v-if="!selected" class="detail-placeholder">
          <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="#e2e8f0" stroke-width="1.2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <p>Chọn khách hàng từ kết quả tìm kiếm</p>
          <span>Thông tin chi tiết sẽ hiển thị tại đây</span>
        </div>

        <!-- Customer detail card -->
        <template v-else>
          <div class="detail-card">
            <!-- Card header -->
            <div class="detail-card__head" :class="selected.customerType === 'VIP' ? 'detail-card__head--vip' : ''">
              <div class="detail-avatar" :class="selected.customerType === 'VIP' ? 'detail-avatar--vip' : ''">
                {{ initials(selected.fullName) }}
              </div>
              <div class="detail-identity">
                <div class="detail-name">{{ selected.fullName }}</div>
                <div class="detail-badges">
                  <span v-if="selected.customerType === 'VIP'" class="badge badge--vip">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                    VIP
                  </span>
                  <span v-else class="badge badge--regular">Thường</span>
                  <span class="badge badge--id">ID #{{ selected.id }}</span>
                </div>
              </div>
              <button class="detail-close" @click="selected = null" title="Đóng">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>

            <!-- Points strip -->
            <div class="points-strip">
              <div class="points-item">
                <span class="points-val">{{ (selected.loyaltyPoints ?? 0).toLocaleString() }}</span>
                <span class="points-label">Điểm tích lũy</span>
              </div>
              <div class="points-divider"></div>
              <div class="points-item">
                <span class="points-val">{{ selected.totalOrders ?? '—' }}</span>
                <span class="points-label">Đơn hàng</span>
              </div>
              <div class="points-divider"></div>
              <div class="points-item">
                <span class="points-val">{{ formatMoney(selected.totalSpent) }}</span>
                <span class="points-label">Tổng chi tiêu</span>
              </div>
            </div>

            <!-- Info fields -->
            <div class="detail-fields">
              <div class="field-row" v-if="selected.phone">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.89 12 19.79 19.79 0 0 1 1.87 3.18 2 2 0 0 1 3.84 1h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 8.91a16 16 0 0 0 6 6l.9-1.04a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Số điện thoại</span>
                  <span class="field-val">{{ selected.phone }}</span>
                </div>
              </div>
              <div class="field-row" v-if="selected.email">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Email</span>
                  <span class="field-val">{{ selected.email }}</span>
                </div>
              </div>
              <div class="field-row" v-if="selected.dateOfBirth">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Ngày sinh</span>
                  <span class="field-val">{{ formatDate(selected.dateOfBirth) }}</span>
                </div>
              </div>
              <div class="field-row" v-if="selected.address">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Địa chỉ</span>
                  <span class="field-val">{{ selected.address }}</span>
                </div>
              </div>
              <div class="field-row" v-if="selected.createdAt">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Ngày đăng ký</span>
                  <span class="field-val">{{ formatDate(selected.createdAt) }}</span>
                </div>
              </div>
            </div>

            <!-- VIP note if any -->
            <div v-if="selected.vipNote" class="vip-note-strip">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              <span>{{ selected.vipNote }}</span>
            </div>

            <!-- Action buttons -->
            <div class="detail-actions">
              <button class="act-btn act-btn--primary" @click="linkToPos" :disabled="posLinking">
                <span v-if="!posLinking">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                    <rect x="2" y="3" width="20" height="14" rx="2"/>
                    <path d="M8 21h8M12 17v4" stroke-linecap="round"/>
                  </svg>
                  Dùng cho đơn POS
                </span>
                <span v-else class="spinner-sm spinner-sm--white"></span>
              </button>
              <button class="act-btn act-btn--outline" @click="copyPhone(selected.phone)" v-if="selected.phone">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                </svg>
                Copy SĐT
              </button>
            </div>
          </div>
        </template>

      </div>
    </div>

    <!-- ══════════════════════════════════════
         CREATE CUSTOMER MODAL
    ══════════════════════════════════════ -->
    <Teleport to="body">
    <transition name="modal-fade">
      <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
        <div class="modal">

          <!-- Modal header -->
          <div class="modal-head">
            <div class="modal-head__left">
              <div class="modal-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                  <line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/>
                </svg>
              </div>
              <div>
                <div class="modal-title">Đăng ký khách hàng mới</div>
                <div class="modal-sub">Tạo tài khoản nhanh cho khách mua tại cửa hàng</div>
              </div>
            </div>
            <button class="modal-close" @click="closeModal">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <!-- Modal body -->
          <div class="modal-body">

            <!-- Error -->
            <transition name="fade-slide">
              <div v-if="formError" class="form-error-bar">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12" stroke-linecap="round"/>
                  <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke-width="1"/>
                </svg>
                {{ formError }}
              </div>
            </transition>

            <!-- Required section -->
            <div class="form-section-label">Thông tin bắt buộc</div>

            <div class="form-row">
              <div class="form-group form-group--full">
                <label class="form-label">Họ và tên <span class="req">*</span></label>
                <input
                  v-model="form.fullName"
                  class="form-input"
                  :class="{ 'form-input--error': v.fullName }"
                  placeholder="Nguyễn Văn A"
                  @input="v.fullName = ''"
                />
                <span v-if="v.fullName" class="form-hint form-hint--err">{{ v.fullName }}</span>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Số điện thoại <span class="req">*</span></label>
                <input
                  v-model="form.phone"
                  class="form-input"
                  :class="{ 'form-input--error': v.phone }"
                  placeholder="09xxxxxxxx"
                  type="tel"
                  @input="v.phone = ''"
                />
                <span v-if="v.phone" class="form-hint form-hint--err">{{ v.phone }}</span>
              </div>
              <div class="form-group">
                <label class="form-label">Mật khẩu <span class="req">*</span></label>
                <div class="pw-wrap">
                  <input
                    v-model="form.password"
                    class="form-input"
                    :class="{ 'form-input--error': v.password }"
                    :type="showPw ? 'text' : 'password'"
                    placeholder="Tối thiểu 6 ký tự"
                    @input="v.password = ''"
                  />
                  <button class="pw-eye" type="button" @click="showPw = !showPw" tabindex="-1">
                    <svg v-if="!showPw" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                    </svg>
                    <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </button>
                </div>
                <span v-if="v.password" class="form-hint form-hint--err">{{ v.password }}</span>
              </div>
            </div>

            Optional section
            <div class="form-section-label optional-label">
              Thông tin bổ sung
              <button class="toggle-optional" @click="showOptional = !showOptional">
                {{ showOptional ? 'Thu gọn' : 'Mở rộng' }}
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" :style="{ transform: showOptional ? 'rotate(180deg)' : '', transition: 'transform 0.2s' }">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </button>
            </div>

            <transition name="expand">
              <div v-if="showOptional" class="optional-fields">
                <div class="form-row">
                  <div class="form-group form-group--full">
                    <label class="form-label">Email</label>
                    <input
                      v-model="form.email"
                      class="form-input"
                      :class="{ 'form-input--error': v.email }"
                      placeholder="example@email.com"
                      type="email"
                      @input="v.email = ''"
                    />
                    <span v-if="v.email" class="form-hint form-hint--err">{{ v.email }}</span>
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label">Ngày sinh</label>
                    <input v-model="form.dateOfBirth" class="form-input" type="date"/>
                  </div>
                  <div class="form-group">
                    <label class="form-label">Giới tính</label>
                    <select v-model="form.gender" class="form-input form-select">
                      <option value="">Không xác định</option>
                      <option value="MALE">Nam</option>
                      <option value="FEMALE">Nữ</option>
                      <option value="OTHER">Khác</option>
                    </select>
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group form-group--full">
                    <label class="form-label">Địa chỉ</label>
                    <input v-model="form.address" class="form-input" placeholder="Số nhà, đường, phường/xã, quận/huyện..."/>
                  </div>
                </div>
              </div>
            </transition>

            <!-- Notice -->
            <div class="form-notice">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12" stroke-linecap="round"/>
                <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke-width="1"/>
              </svg>
              Tài khoản sẽ được tạo với vai trò <strong>Khách hàng</strong>. Khách có thể đăng nhập và xem lịch sử mua hàng sau khi đăng ký.
            </div>
          </div>

          <!-- Modal footer -->
          <div class="modal-foot">
            <button class="foot-btn foot-btn--cancel" @click="closeModal" :disabled="createLoading">Hủy</button>
            <button class="foot-btn foot-btn--create" @click="doCreate" :disabled="createLoading">
              <span v-if="!createLoading">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                  <line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/>
                </svg>
                Đăng ký khách hàng
              </span>
              <span v-else class="spinner-sm spinner-sm--white"></span>
            </button>
          </div>
        </div>
      </div>
    </transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { customersApi } from "../../api/customers.api";
import { confirmModal } from "../../ui/confirm";
import { toast } from "../../ui/toast";

const router = useRouter();

// ── State ─────────────────────────────────────────
const query         = ref("");
const lastQuery     = ref("");
const searchFocused = ref(false);
const searchLoading = ref(false);
const searchError   = ref("");
const searchResults = ref([]);
const searchDone    = ref(false);
const selected      = ref(null);

const showModal     = ref(false);
const showOptional  = ref(false);
const showPw        = ref(false);
const createLoading = ref(false);
const formError     = ref("");
const posLinking    = ref(false);

const form = reactive({
  fullName: "", phone: "", password: "",
  email: "", dateOfBirth: "", gender: "", address: "",
});

const v = reactive({
  fullName: "", phone: "", password: "", email: "",
});

// ── Helpers ───────────────────────────────────────
function initials(name = "") {
  return (name || "").split(" ").map(w => w[0] || "").filter(Boolean).slice(-2).join("").toUpperCase() || "KH";
}
function formatDate(str) {
  if (!str) return "—";
  const d = new Date(str);
  return isNaN(d) ? str : d.toLocaleDateString("vi-VN");
}
function formatMoney(val) {
  if (val === undefined || val === null) return "—";
  return Number(val).toLocaleString("vi-VN") + "₫";
}

// ── Search ────────────────────────────────────────
let debounceTimer = null;
function onQueryInput() {
  clearTimeout(debounceTimer);
  if (!query.value.trim()) {
    searchDone.value = false;
    searchResults.value = [];
    searchError.value = "";
  }
}

async function doSearch() {
  const q = query.value.trim();
  if (!q) return;
  lastQuery.value = q;
  searchLoading.value = true;
  searchError.value = "";
  searchDone.value = false;
  selected.value = null;
  try {
    const res = await customersApi.listAll();
    const all = Array.isArray(res) ? res : (res?.data?.data ?? res?.data ?? res?.customers ?? []);

    const lower = q.toLowerCase();
    searchResults.value = all.filter(c =>
      (c.fullName  ?? "").toLowerCase().includes(lower) ||
      (c.phone     ?? "").includes(q) ||
      (c.email     ?? "").toLowerCase().includes(lower)
    );
    searchDone.value = true;

    if (!searchResults.value.length) {
      toast(`Không tìm thấy khách hàng với "${q}"`, "warning");
    }
  } catch (e) {
    searchError.value = e?.response?.data?.message || e?.message || "Lỗi tìm kiếm. Vui lòng thử lại.";
    toast("Lỗi tìm kiếm. Vui lòng thử lại.", "error");
  } finally {
    searchLoading.value = false;
  }
}

function resetSearch() {
  query.value = ""; lastQuery.value = "";
  searchResults.value = []; searchDone.value = false;
  searchError.value = ""; selected.value = null;
}

function selectCustomer(cust) {
  selected.value = cust;
}

// ── Create ────────────────────────────────────────
function openCreateModal(prefill = "") {
  resetForm();
  if (typeof prefill === "string" && prefill) {
    if (/^[0-9+\s-]{7,}$/.test(prefill))  form.phone    = prefill;
    else if (prefill.includes("@"))         form.email    = prefill;
    else                                    form.fullName = prefill;
  }
  showModal.value = true;
}

function closeModal() { if (createLoading.value) return; showModal.value = false; }

function resetForm() {
  Object.assign(form, { fullName: "", phone: "", password: "", email: "", dateOfBirth: "", gender: "", address: "" });
  Object.assign(v,    { fullName: "", phone: "", password: "", email: "" });
  formError.value = ""; showPw.value = false; showOptional.value = false;
}

function validateForm() {
  let ok = true;
  if (!form.fullName.trim())                                            { v.fullName  = "Họ tên không được để trống"; ok = false; }
  if (!form.phone.trim())                                               { v.phone     = "Số điện thoại không được để trống"; ok = false; }
  else if (!/^[0-9+\s-]{7,15}$/.test(form.phone.trim()))               { v.phone     = "Số điện thoại không hợp lệ"; ok = false; }
  if (!form.password)                                                    { v.password  = "Mật khẩu không được để trống"; ok = false; }
  else if (form.password.length < 6)                                    { v.password  = "Mật khẩu tối thiểu 6 ký tự"; ok = false; }
  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))   { v.email     = "Email không hợp lệ"; ok = false; }
  return ok;
}

async function doCreate() {
  formError.value = "";
  if (!validateForm()) return;
  createLoading.value = true;
  try {
    const payload = {
      fullName: form.fullName.trim(),
      phone:    form.phone.trim(),
      password: form.password,
      role:     "CUSTOMER",
      ...(form.email       && { email:       form.email.trim() }),
      ...(form.dateOfBirth && { dateOfBirth: form.dateOfBirth }),
      ...(form.gender      && { gender:      form.gender }),
      ...(form.address     && { address:     form.address.trim() }),
    };
    const created = await customersApi.create(payload);
    showModal.value = false;
    selected.value  = created?.data ?? created;
    toast(`Đã đăng ký thành công cho ${form.fullName}!`, "success");
    if (searchDone.value) searchResults.value.unshift(selected.value);
  } catch (e) {
    formError.value = e?.response?.data?.message || e?.message || "Không thể tạo tài khoản. Vui lòng thử lại.";
    toast(formError.value, "error");
  } finally {
    createLoading.value = false;
  }
}

// ── Actions ───────────────────────────────────────

/**
 * Gán khách hàng đang chọn cho phiên POS, sau đó chuyển sang trang POS.
 * Trang POS sẽ tự đọc "pos_customer" từ sessionStorage trong onMounted.
 * Điều chỉnh route '/sales/pos' cho khớp với cấu hình router của dự án.
 */
async function linkToPos() {
  if (!selected.value) return;
  posLinking.value = true;
  try {
    sessionStorage.setItem("pos_customer", JSON.stringify(selected.value));
  } catch { /* ignore */ }
  toast(`Đã gán "${selected.value.fullName}" — đang chuyển sang POS...`, "success", { delay: 1500 });
  setTimeout(() => {
    router.push("/sales/pos"); // ← điều chỉnh route nếu cần
  }, 600);
}

async function copyPhone(phone) {
  if (!phone) return;
  try {
    await navigator.clipboard.writeText(phone);
    toast("Đã copy số điện thoại", "success");
  } catch {
    toast("Không thể copy số điện thoại", "error");
  }
}


</script>

<style scoped>
/* ══════════════════════════════════════════
   ROOT
══════════════════════════════════════════ */
.scm-root {
  display: flex; flex-direction: column;
  height: 100%; background: #f4f6f8;
  font-family: 'Inter', 'Segoe UI', system-ui, sans-serif;
  color: #0f172a;
}

/* ══════════════════════════════════════════
   HEADER
══════════════════════════════════════════ */
.page-header {
  display: flex; align-items: flex-end; justify-content: space-between;
  padding: 20px 24px 0; flex-shrink: 0; gap: 16px; flex-wrap: wrap;
}
.page-header__left { display: flex; flex-direction: column; gap: 3px; }
.page-eyebrow {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 11px; font-weight: 700; color: #f59e0b;
  text-transform: uppercase; letter-spacing: 0.08em;
}
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; margin: 0; letter-spacing: -0.4px; }
.page-sub { font-size: 13px; color: #64748b; margin: 0; }

.page-header__actions { display: flex; align-items: center; gap: 10px; padding-bottom: 2px; }

.hdr-btn {
  height: 38px; padding: 0 18px; border-radius: 9px;
  font-size: 13px; font-weight: 700; cursor: pointer; font-family: inherit;
  display: inline-flex; align-items: center; gap: 7px; transition: all 0.15s;
}
.hdr-btn--amber {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none; color: #fff;
  box-shadow: 0 2px 8px rgba(245,158,11,0.3);
}
.hdr-btn--amber:hover { opacity: 0.88; }
.hdr-btn--logout {
  background: #f8fafc; border: 1.5px solid #e2e8f0; color: #64748b;
}
.hdr-btn--logout:hover { background: #fef2f2; border-color: #fecaca; color: #dc2626; }

/* ══════════════════════════════════════════
   BODY LAYOUT
══════════════════════════════════════════ */
.scm-body {
  flex: 1; display: grid; grid-template-columns: 420px 1fr;
  gap: 20px; padding: 20px 24px 24px; overflow: hidden;
  min-height: 0;
}

/* ══════════════════════════════════════════
   SEARCH PANEL (LEFT)
══════════════════════════════════════════ */
.panel-left {
  display: flex; flex-direction: column; gap: 14px;
  overflow-y: auto; min-height: 0;
  scrollbar-width: thin; scrollbar-color: #e2e8f0 transparent;
}
.panel-left::-webkit-scrollbar { width: 4px; }
.panel-left::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

.search-card {
  background: #fff; border: 1px solid #e8edf2;
  border-radius: 14px; padding: 16px 18px;
  display: flex; flex-direction: column; gap: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04); flex-shrink: 0;
}
.search-card__title {
  display: flex; align-items: center; gap: 7px;
  font-size: 13px; font-weight: 700; color: #374151;
}
.search-bar { display: flex; gap: 8px; }
.search-wrap {
  flex: 1; display: flex; align-items: center; gap: 8px;
  background: #f4f6f8; border: 1.5px solid #e2e8f0;
  border-radius: 9px; padding: 0 12px; height: 40px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-wrap.focused { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245,158,11,0.08); background: #fff; }
.s-icon { color: #94a3b8; flex-shrink: 0; transition: color 0.15s; }
.search-wrap.focused .s-icon { color: #f59e0b; }
.s-input {
  flex: 1; background: transparent; border: none; outline: none;
  color: #0f172a; font-size: 13.5px; font-family: inherit; min-width: 0;
}
.s-input::placeholder { color: #c8d0da; }
.s-clear {
  background: none; border: none; cursor: pointer;
  color: #94a3b8; display: flex; padding: 3px; transition: color 0.15s;
}
.s-clear:hover { color: #475569; }
.s-btn {
  height: 40px; padding: 0 16px;
  background: #f59e0b; border: none; border-radius: 9px;
  color: #fff; font-size: 13px; font-weight: 700;
  cursor: pointer; font-family: inherit;
  display: inline-flex; align-items: center; gap: 6px;
  transition: all 0.15s; white-space: nowrap;
  box-shadow: 0 2px 6px rgba(245,158,11,0.28);
}
.s-btn:hover:not(:disabled) { background: #d97706; }
.s-btn:disabled { opacity: 0.4; cursor: not-allowed; box-shadow: none; }

.error-bar {
  display: flex; align-items: center; gap: 8px;
  background: #fef2f2; border: 1px solid #fecaca;
  color: #dc2626; font-size: 12.5px; padding: 10px 14px; border-radius: 8px;
}

/* ── Results ── */
.results-section { display: flex; flex-direction: column; gap: 10px; }
.no-result-card {
  background: #fff; border: 1px dashed #e2e8f0; border-radius: 12px;
  padding: 28px 20px; display: flex; flex-direction: column;
  align-items: center; gap: 8px; text-align: center; color: #94a3b8;
  font-size: 13.5px;
}
.no-result-card strong { color: #475569; }

.result-meta { font-size: 12.5px; color: #94a3b8; }
.result-meta strong { color: #374151; }

.result-list { display: flex; flex-direction: column; gap: 8px; }
.result-row {
  background: #fff; border: 1.5px solid #e8edf2;
  border-radius: 12px; padding: 12px 14px;
  display: flex; align-items: center; gap: 12px;
  cursor: pointer; transition: all 0.16s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.result-row:hover { border-color: #fde68a; background: #fffdf5; }
.result-row--selected { border-color: #f59e0b; background: #fffbeb; }

.r-avatar {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #64748b; flex-shrink: 0;
}
.r-avatar--vip { background: linear-gradient(135deg, #fef3c7, #fde68a); color: #b45309; }
.r-info { flex: 1; min-width: 0; }
.r-name { font-size: 13.5px; font-weight: 700; color: #0f172a; margin-bottom: 3px; }
.r-meta {
  display: flex; align-items: center; gap: 10px;
  font-size: 12px; color: #64748b; flex-wrap: wrap;
}
.r-meta span { display: flex; align-items: center; gap: 4px; }
.r-right { display: flex; flex-direction: column; align-items: flex-end; gap: 5px; flex-shrink: 0; }
.vip-badge {
  font-size: 9.5px; font-weight: 800; color: #b45309;
  background: #fef3c7; border: 1px solid #fde68a;
  padding: 2px 8px; border-radius: 10px; letter-spacing: 0.05em;
}
.r-points { font-size: 11.5px; font-weight: 600; color: #94a3b8; }

.empty-hint {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 40px 20px; text-align: center; color: #c8d0da; font-size: 13.5px;
}

/* ══════════════════════════════════════════
   DETAIL PANEL (RIGHT)
══════════════════════════════════════════ */
.panel-right {
  overflow-y: auto; min-height: 0;
  scrollbar-width: thin; scrollbar-color: #e2e8f0 transparent;
}
.panel-right::-webkit-scrollbar { width: 4px; }
.panel-right::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

.detail-placeholder {
  height: 100%; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 10px;
  color: #c8d0da; font-size: 14px; text-align: center;
  background: #fff; border: 1px solid #e8edf2; border-radius: 16px;
}
.detail-placeholder p { font-weight: 600; color: #94a3b8; margin: 0; }
.detail-placeholder span { font-size: 12.5px; }

.detail-card {
  background: #fff; border: 1px solid #e8edf2;
  border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  display: flex; flex-direction: column;
}

.detail-card__head {
  display: flex; align-items: center; gap: 14px;
  padding: 20px 20px 16px;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-bottom: 1px solid #f0f2f5;
}
.detail-card__head--vip {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border-bottom-color: #fde68a;
}
.detail-avatar {
  width: 48px; height: 48px; border-radius: 12px; flex-shrink: 0;
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 800; color: #64748b;
}
.detail-avatar--vip { background: linear-gradient(135deg, #fde68a, #f59e0b); color: #78350f; }
.detail-identity { flex: 1; min-width: 0; }
.detail-name { font-size: 17px; font-weight: 800; color: #0f172a; margin-bottom: 5px; }
.detail-badges { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.badge {
  font-size: 10px; font-weight: 700; padding: 2px 9px; border-radius: 10px;
  display: inline-flex; align-items: center; gap: 4px;
}
.badge--vip    { background: #fef3c7; color: #b45309; border: 1px solid #fde68a; }
.badge--regular{ background: #f1f5f9; color: #64748b; border: 1px solid #e2e8f0; }
.badge--id     { background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; font-size: 9.5px; }
.detail-close {
  background: none; border: none; color: #94a3b8; cursor: pointer;
  display: flex; padding: 4px; border-radius: 6px; transition: all 0.15s;
}
.detail-close:hover { background: rgba(0,0,0,0.06); color: #475569; }

/* Points strip */
.points-strip {
  display: flex; align-items: stretch;
  border-bottom: 1px solid #f0f2f5;
}
.points-item {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  padding: 14px 10px; gap: 4px;
}
.points-divider { width: 1px; background: #f0f2f5; flex-shrink: 0; }
.points-val { font-size: 18px; font-weight: 800; color: #0f172a; }
.points-label { font-size: 11px; color: #94a3b8; font-weight: 500; }

/* Info fields */
.detail-fields { display: flex; flex-direction: column; padding: 6px 0; }
.field-row {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 10px 20px; border-bottom: 1px solid #f8fafc;
}
.field-row:last-child { border-bottom: none; }
.field-icon {
  width: 28px; height: 28px; border-radius: 7px;
  background: #f4f6f8; display: flex; align-items: center; justify-content: center;
  color: #94a3b8; flex-shrink: 0; margin-top: 1px;
}
.field-content { display: flex; flex-direction: column; gap: 2px; }
.field-label { font-size: 10.5px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.06em; }
.field-val { font-size: 13.5px; font-weight: 500; color: #0f172a; }

/* VIP note */
.vip-note-strip {
  display: flex; align-items: center; gap: 8px;
  margin: 0 16px 0; padding: 10px 14px;
  background: #fffbeb; border: 1px solid #fde68a; border-radius: 8px;
  font-size: 12.5px; color: #92400e;
}

/* Action buttons */
.detail-actions {
  display: flex; gap: 10px; padding: 16px 20px 20px; flex-wrap: wrap;
}
.act-btn {
  height: 40px; padding: 0 20px; border-radius: 9px;
  font-size: 13px; font-weight: 700; cursor: pointer; font-family: inherit;
  display: inline-flex; align-items: center; gap: 7px; transition: all 0.15s;
  white-space: nowrap;
}
.act-btn--primary {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none; color: #fff; flex: 1;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(245,158,11,0.3);
  min-width: 160px;
}
.act-btn--primary:hover:not(:disabled) { opacity: 0.88; }
.act-btn--primary:disabled { opacity: 0.45; cursor: not-allowed; box-shadow: none; }
.act-btn--outline {
  background: #f4f6f8; border: 1.5px solid #e2e8f0; color: #475569;
}
.act-btn--outline:hover { border-color: #cbd5e1; background: #eef1f4; }

/* ══════════════════════════════════════════
   MODAL
══════════════════════════════════════════ */
.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.3); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 500; padding: 20px;
}
.modal {
  background: #fff; border: 1px solid #e8edf2;
  border-radius: 16px; width: 100%; max-width: 540px;
  max-height: 90vh; display: flex; flex-direction: column;
  box-shadow: 0 24px 48px rgba(0,0,0,0.14);
  position: fixed;         /* Thay đổi từ mặc định sang fixed */
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.modal-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 22px 14px;
  border-bottom: 1px solid #f0f2f5;
  flex-shrink: 0;
}
.modal-head__left { display: flex; align-items: center; gap: 12px; }
.modal-icon {
  width: 38px; height: 38px; border-radius: 10px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  display: flex; align-items: center; justify-content: center;
  color: #d97706; flex-shrink: 0;
}
.modal-title { font-size: 15px; font-weight: 800; color: #0f172a; }
.modal-sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.modal-close {
  width: 30px; height: 30px; border-radius: 50%;
  background: #f4f6f8; border: 1px solid #e2e8f0;
  color: #64748b; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.modal-close:hover { background: #e2e8f0; color: #0f172a; }
.modal-body { padding: 18px 22px; overflow-y: auto; display: flex; flex-direction: column; gap: 14px; }

/* Form */
.form-section-label {
  font-size: 10.5px; font-weight: 700; color: #9ca3af;
  text-transform: uppercase; letter-spacing: 0.08em;
  display: flex; align-items: center; justify-content: space-between;
}
.optional-label { margin-top: 2px; }
.toggle-optional {
  display: inline-flex; align-items: center; gap: 4px;
  background: none; border: none; color: #f59e0b;
  font-size: 11.5px; font-weight: 600; cursor: pointer;
  font-family: inherit; padding: 0; transition: color 0.15s;
}
.toggle-optional:hover { color: #d97706; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group--full { grid-column: 1 / -1; }
.form-label { font-size: 12px; font-weight: 600; color: #374151; }
.req { color: #dc2626; }
.form-input {
  height: 40px; padding: 0 12px;
  border: 1.5px solid #e2e8f0; border-radius: 8px;
  font-size: 13.5px; color: #0f172a; font-family: inherit;
  outline: none; background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.form-input:focus { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245,158,11,0.08); }
.form-input--error { border-color: #fca5a5; }
.form-input--error:focus { box-shadow: 0 0 0 3px rgba(239,68,68,0.08); }
.form-select { appearance: none; cursor: pointer; }
.form-hint { font-size: 11.5px; margin-top: 1px; }
.form-hint--err { color: #dc2626; }

.pw-wrap { position: relative; }
.pw-wrap .form-input { width: 100%; padding-right: 38px; box-sizing: border-box; }
.pw-eye {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  background: none; border: none; color: #94a3b8; cursor: pointer;
  display: flex; padding: 4px; transition: color 0.15s;
}
.pw-eye:hover { color: #475569; }

.optional-fields { display: flex; flex-direction: column; gap: 12px; }

.form-error-bar {
  display: flex; align-items: flex-start; gap: 9px;
  background: #fef2f2; border: 1px solid #fecaca;
  color: #dc2626; font-size: 13px;
  padding: 10px 14px; border-radius: 8px;
}

.form-notice {
  display: flex; align-items: flex-start; gap: 8px;
  background: #eff6ff; border: 1px solid #bfdbfe;
  color: #1d4ed8; font-size: 12px;
  padding: 10px 14px; border-radius: 8px; line-height: 1.5;
}

.modal-foot {
  display: flex; gap: 10px; justify-content: flex-end;
  padding: 14px 22px;
  border-top: 1px solid #f0f2f5;
  flex-shrink: 0;
}
.foot-btn {
  height: 40px; padding: 0 22px; border-radius: 9px;
  font-size: 13px; font-weight: 700; cursor: pointer;
  font-family: inherit; transition: all 0.15s;
  display: inline-flex; align-items: center; gap: 8px;
}
.foot-btn--cancel {
  background: #f4f6f8; border: 1px solid #e2e8f0; color: #475569;
}
.foot-btn--cancel:hover:not(:disabled) { border-color: #cbd5e1; }
.foot-btn--create {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none; color: #fff;
  box-shadow: 0 2px 8px rgba(245,158,11,0.28);
  min-width: 170px; justify-content: center;
}
.foot-btn--create:hover:not(:disabled) { opacity: 0.88; }
.foot-btn:disabled { opacity: 0.4; cursor: not-allowed; box-shadow: none; }

/* ══════════════════════════════════════════
   SPINNERS
══════════════════════════════════════════ */
.spinner-sm {
  width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.15);
  border-top-color: #f59e0b;
  border-radius: 50%;
  animation: scm-spin 0.6s linear infinite;
  display: inline-block;
}
.spinner-sm--white {
  border-color: rgba(255,255,255,0.3);
  border-top-color: #fff;
}
@keyframes scm-spin { to { transform: rotate(360deg); } }

/* ══════════════════════════════════════════
   TRANSITIONS
══════════════════════════════════════════ */
.fade-slide-enter-active { transition: all 0.22s ease; }
.fade-slide-leave-active { transition: all 0.14s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(-5px); }
.fade-slide-leave-to { opacity: 0; }

.expand-enter-active { transition: all 0.25s ease; overflow: hidden; }
.expand-leave-active { transition: all 0.18s ease; overflow: hidden; }
.expand-enter-from { opacity: 0; max-height: 0; }
.expand-enter-to { max-height: 400px; }
.expand-leave-to { opacity: 0; max-height: 0; }

.modal-fade-enter-active { transition: all 0.25s ease; }
.modal-fade-leave-active { transition: all 0.18s ease; }
.modal-fade-enter-from { opacity: 0; }
.modal-fade-leave-to { opacity: 0; }
.modal-fade-enter-active .modal { animation: modal-pop 0.28s cubic-bezier(0.34,1.3,0.64,1); }
@keyframes modal-pop {
  from { transform: scale(0.92) translateY(12px); opacity: 0; }
  to   { transform: scale(1) translateY(0); opacity: 1; }
}

/* ══════════════════════════════════════════
   RESPONSIVE
══════════════════════════════════════════ */
@media (max-width: 900px) {
  .scm-body { grid-template-columns: 1fr; }
  .panel-right { min-height: 300px; }
  .page-header { padding: 16px 18px 0; flex-wrap: wrap; }
  .scm-body { padding: 16px 18px 20px; }
  .form-row { grid-template-columns: 1fr; }
}
</style>