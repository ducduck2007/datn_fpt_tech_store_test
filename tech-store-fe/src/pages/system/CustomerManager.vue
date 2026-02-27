<template>
  <div class="page-wrapper">
    <div class="page-inner">

      <!-- ══════════════════════════════════════════
           HEADER
      ══════════════════════════════════════════ -->
      <div class="page-header">
        <div class="header-left">
          <div class="header-eyebrow">Retail Management</div>
          <h1 class="header-title">Customers <span class="title-accent">&</span> Loyalty</h1>
          <p class="header-sub">{{ rows.length }} khách hàng · Base: <code>/api/auth/customers</code></p>
        </div>
        <div class="header-actions">
          <button class="btn-ghost" @click="load" :disabled="loading">
            <svg v-if="!loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
            <span v-if="loading" class="spin-icon">⟳</span>
            Reload
          </button>
          <button class="btn-primary" @click="openCreate">
            <span>+</span> Thêm khách hàng
          </button>
          <button class="btn-ghost" @click="openSummaryDialog">
  📊 Tổng hợp điểm
</button>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           STATS BAR
      ══════════════════════════════════════════ -->
      <div class="stats-bar">
        <div class="stat-pill">
          <span class="stat-num">{{ rows.length }}</span>
          <span class="stat-label">Tổng KH</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-pill">
          <span class="stat-num text-gold">{{ vipCount }}</span>
          <span class="stat-label">VIP</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-pill">
          <span class="stat-num text-blue">{{ totalPoints.toLocaleString() }}</span>
          <span class="stat-label">Tổng điểm</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-pill">
          <span class="stat-num text-green">{{ avgPoints.toLocaleString() }}</span>
          <span class="stat-label">TB điểm</span>
        </div>
        <div class="stat-divider" v-if="hasActiveFilters" />
        <div class="stat-pill active-filter-pill" v-if="hasActiveFilters">
          <span class="stat-num text-orange">{{ filtered.length }}</span>
          <span class="stat-label">Kết quả lọc</span>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           FILTERS
      ══════════════════════════════════════════ -->
      <div class="filter-panel">
        <div class="filter-row">
          <div class="filter-group filter-search">
            <label class="filter-label">Tìm kiếm</label>
            <div class="input-icon-wrap">
              <svg class="input-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              <input v-model="q" class="filter-input" placeholder="Tên / Email / SĐT..." />
            </div>
          </div>
          <div class="filter-group">
            <label class="filter-label">Loại KH</label>
            <select v-model="typeFilter" class="filter-select" @change="handleFilterChange">
              <option value="">Tất cả</option>
              <option value="REGULAR">REGULAR</option>
              <option value="VIP">VIP</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label">VIP Tier</label>
            <select v-model="vipTierFilter" class="filter-select" @change="handleFilterChange">
              <option value="">Tất cả Tier</option>
              <option value="BRONZE">🥉 BRONZE</option>
              <option value="SILVER">🥈 SILVER</option>
              <option value="GOLD">🥇 GOLD</option>
              <option value="PLATINUM">💠 PLATINUM</option>
              <option value="DIAMOND">💎 DIAMOND</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label">Hoạt động</label>
            <select v-model="activityFilter" class="filter-select" @change="handleFilterChange">
              <option value="">Tất cả</option>
              <option value="ACTIVE_30">Active 30 ngày</option>
            </select>
          </div>
          <div class="filter-group filter-action">
            <label class="filter-label">&nbsp;</label>
            <button class="btn-outline-sm" @click="clearAllFilters" :disabled="!hasActiveFilters">✕ Xóa filter</button>
          </div>
        </div>

        <div class="filter-row filter-row-points">
          <div class="filter-group">
            <label class="filter-label">Điểm tối thiểu</label>
            <input v-model.number="pointsMin" type="number" class="filter-input" placeholder="0" min="0" />
          </div>
          <div class="filter-group">
            <label class="filter-label">Điểm tối đa</label>
            <input v-model.number="pointsMax" type="number" class="filter-input" placeholder="999999" min="0" />
          </div>
          <div class="filter-group filter-action">
            <label class="filter-label">&nbsp;</label>
            <button class="btn-outline-sm btn-blue-sm" @click="applyPointsFilter" :disabled="!canApplyPointsFilter">⚡ Lọc điểm</button>
          </div>
          <div class="filter-group filter-action">
            <label class="filter-label">&nbsp;</label>
            <button class="btn-outline-sm" @click="clearPointsFilter" :disabled="!isPointsFilterApplied">✕ Reset</button>
          </div>
        </div>

        <div class="active-tags" v-if="hasActiveFilters">
          <span class="tag-label">Đang lọc:</span>
          <span class="filter-tag" v-if="typeFilter">
            Loại: {{ typeFilter }}<button @click="typeFilter = ''; handleFilterChange()">×</button>
          </span>
          <span class="filter-tag filter-tag-gold" v-if="vipTierFilter">
            Tier: {{ vipTierFilter }}<button @click="vipTierFilter = ''; handleFilterChange()">×</button>
          </span>
          <span class="filter-tag filter-tag-green" v-if="activityFilter === 'ACTIVE_30'">
            Active 30 ngày<button @click="activityFilter = ''; handleFilterChange()">×</button>
          </span>
          <span class="filter-tag filter-tag-blue" v-if="isPointsFilterApplied">
            Điểm: {{ appliedPointsMin }} – {{ appliedPointsMax === 999999 ? '∞' : appliedPointsMax }}
            <button @click="clearPointsFilter">×</button>
          </span>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           TABLE
      ══════════════════════════════════════════ -->
      <div class="table-card">
        <div class="table-loading" v-if="loading">
          <div class="loader-dots"><span/><span/><span/></div>
          <p>Đang tải dữ liệu...</p>
        </div>

        <div class="table-scroll" v-else>
          <table class="data-table">
            <thead>
              <tr>
                <th class="col-id">#</th>
                <th class="col-name">Khách hàng</th>
                <th class="col-contact">Email / SĐT</th>
                <th class="col-type">Loại</th>
                <th class="col-tier">VIP Tier</th>
                <th class="col-points">Điểm</th>
                <th class="col-spent">Chi tiêu</th>
                <th class="col-vipnote">⭐ VIP Note</th>
                <th class="col-notes">Ghi chú</th>
                <th class="col-actions">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="paged.length === 0">
                <td colspan="10" class="empty-row">
                  <div class="empty-state">
                    <span class="empty-icon">👤</span>
                    <p>Không tìm thấy khách hàng nào</p>
                  </div>
                </td>
              </tr>
              <tr v-for="row in paged" :key="row.id" class="data-row">

                <td class="col-id"><span class="id-badge">#{{ row.id }}</span></td>

                <td class="col-name">
                  <div class="customer-cell">
                    <div class="avatar" :style="{ background: getAvatarColor(row.fullName) }">{{ getInitials(row.fullName) }}</div>
                    <div class="customer-info">
                      <span class="customer-name">{{ row.fullName }}</span>
                      <span class="customer-dob" v-if="row.birthDate">{{ row.birthDate }}</span>
                    </div>
                  </div>
                </td>

                <td class="col-contact">
                  <div class="contact-cell">
                    <span class="contact-email">{{ row.email }}</span>
                    <span class="contact-phone" v-if="row.phone">{{ row.phone }}</span>
                  </div>
                </td>

                <td class="col-type">
                  <span :class="['type-badge', row.customerType === 'VIP' ? 'type-vip' : 'type-regular']">
                    {{ row.customerType || 'REGULAR' }}
                  </span>
                </td>

                <td class="col-tier">
                  <span v-if="row.raw.vipTier" :class="['tier-badge', `tier-${row.raw.vipTier.toLowerCase()}`]">
                    {{ tierIcon(row.raw.vipTier) }} {{ row.raw.vipTier }}
                  </span>
                  <span v-else class="text-muted-sm">—</span>
                </td>

                <td class="col-points">
                  <div class="points-cell">
                    <span class="points-num">{{ (row.loyaltyPoints || 0).toLocaleString() }}</span>
                    <div class="points-bar" v-if="row.raw.vipTier">
                      <div class="points-bar-fill" :style="{ width: getPointsProgress(row.raw) + '%', background: getTierColor(row.raw.vipTier) }" />
                    </div>
                  </div>
                </td>

                <td class="col-spent">
                  <span class="spent-amount">{{ formatCurrencyShort(row.raw.totalSpent) }}</span>
                </td>

                <!-- ⭐ VIP NOTE: CHỈ HIỆN KHI KHÁCH CÓ VIP TIER -->
                <td class="col-vipnote">
                  <template v-if="row.raw.vipTier">
                    <div v-if="row.raw.vipNote" class="vipnote-cell" @click="openVipNoteEdit(row)" title="Click để sửa">
                      <span class="vipnote-star">⭐</span>
                      <span class="vipnote-text">{{ row.raw.vipNote.substring(0, 30) }}{{ row.raw.vipNote.length > 30 ? '…' : '' }}</span>
                    </div>
                    <button v-else class="vipnote-add-btn" @click="openVipNoteEdit(row)">+ Thêm</button>
                  </template>
                  <span v-else class="text-muted-sm">—</span>
                </td>

                <td class="col-notes">
                  <span class="note-text" v-if="row.notes" :title="row.notes">
                    {{ row.notes.substring(0, 25) }}{{ row.notes.length > 25 ? '…' : '' }}
                  </span>
                  <span v-else class="text-muted-sm">—</span>
                </td>

                <td class="col-actions">
                  <div class="action-group">
                    <button class="action-btn action-view" @click="viewDetails(row)" title="Chi tiết">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    </button>
                    <button class="action-btn action-edit" @click="openEdit(row)" title="Chỉnh sửa">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </button>
                    <!-- Nút ⭐ chỉ hiện với khách có VIP Tier -->
                    <button v-if="row.raw.vipTier" class="action-btn action-star" @click="openVipNoteEdit(row)" title="VIP Note">⭐</button>
                    <button class="action-btn action-del" @click="remove(row)" title="Xóa">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
                    </button>
                  </div>
                </td>

              </tr>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          <span class="pagination-info">
            Hiển thị {{ Math.min((page - 1) * pageSize + 1, filtered.length) }}–{{ Math.min(page * pageSize, filtered.length) }} / {{ filtered.length }} kết quả
          </span>
          <div class="pagination">
            <button class="page-btn" :disabled="page === 1" @click="page--">‹</button>
            <button
              v-for="p in pageCount" :key="p"
              :class="['page-btn', { 'page-btn-active': p === page }]"
              @click="page = p"
              v-show="Math.abs(p - page) <= 2 || p === 1 || p === pageCount"
            >{{ p }}</button>
            <button class="page-btn" :disabled="page === pageCount" @click="page++">›</button>
          </div>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           CREATE / EDIT DIALOG
      ══════════════════════════════════════════ -->
      <Teleport to="body">
        <div class="modal-overlay" v-if="dlg.open" @click.self="dlg.open = false">
          <div class="modal-box modal-lg">
            <div class="modal-header">
              <h2 class="modal-title">{{ dlg.mode === 'create' ? '➕ Thêm khách hàng' : '✏️ Cập nhật khách hàng' }}</h2>
              <button class="modal-close" @click="dlg.open = false">×</button>
            </div>
            <div class="modal-alert" v-if="dlg.alert">⚠️ {{ dlg.alert }}</div>
            <div class="modal-body">
              <div class="form-grid">
                <div class="form-group">
                  <label class="form-label">Họ tên <span class="required">*</span></label>
                  <input v-model="dlg.form.fullName" class="form-input" placeholder="Nguyễn Văn A" />
                </div>
                <div class="form-group">
                  <label class="form-label">Loại khách hàng</label>
                  <select v-model="dlg.form.customerType" class="form-select">
                    <option value="REGULAR">REGULAR</option>
                    <option value="VIP">VIP</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Email <span class="required">*</span></label>
                  <input v-model="dlg.form.email" type="email" class="form-input" placeholder="email@example.com" />
                </div>
                <div class="form-group">
                  <label class="form-label">Số điện thoại <span class="required">*</span></label>
                  <input v-model="dlg.form.phone" class="form-input" placeholder="0912345678" />
                </div>
                <div class="form-group">
                  <label class="form-label">Ngày sinh</label>
                  <input v-model="dlg.form.birthDate" type="date" class="form-input" />
                </div>
                <div class="form-group">
                  <label class="form-label">Địa chỉ</label>
                  <input v-model="dlg.form.address" class="form-input" placeholder="123 Đường ABC, TP.HCM" />
                </div>
                <div class="form-group form-group-full">
                  <label class="form-label">Ghi chú thông thường</label>
                  <textarea v-model="dlg.form.notes" class="form-textarea" rows="2" placeholder="Ghi chú hiển thị công khai..." />
                </div>

                <!-- ⭐ VIP NOTE: chỉ hiện khi EDIT khách đã có VIP Tier -->
                <div class="form-group form-group-full vip-note-form-section" v-if="dlg.mode === 'edit' && dlg.form.vipTier">
                  <div class="vip-note-form-header">
                    <label class="form-label vip-label">
                      <span>⭐</span> Ghi chú VIP
                      <span class="vip-badge-label">Nội bộ · Chỉ Admin/Staff thấy</span>
                    </label>
                    <span class="vip-tier-indicator">{{ tierIcon(dlg.form.vipTier) }} {{ dlg.form.vipTier }}</span>
                  </div>
                  <textarea
                    v-model="dlg.form.vipNote"
                    class="form-textarea vip-textarea"
                    rows="3"
                    placeholder="VD: Thích gaming laptop, không thích hàng trưng bày, ưu tiên giao sáng..."
                    maxlength="500"
                  />
                  <div class="vip-note-hint">
                    🔒 Khách hàng <strong>không thể xem</strong> ghi chú này.
                    <span class="char-count">{{ (dlg.form.vipNote || '').length }}/500</span>
                  </div>
                </div>

                <!-- Thông báo khi tạo mới -->
                <div class="form-group form-group-full" v-if="dlg.mode === 'create'">
                  <div class="vip-note-unavailable">
                    <span>⭐</span>
                    <span>Ghi chú VIP sẽ khả dụng sau khi khách hàng đạt VIP Tier (BRONZE trở lên).</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="dlg.open = false">Hủy</button>
              <button class="btn-primary" :disabled="dlg.loading" @click="save">
                <span v-if="dlg.loading" class="spin-icon">⟳</span>
                {{ dlg.mode === 'create' ? 'Tạo khách hàng' : 'Lưu thay đổi' }}
              </button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- ══════════════════════════════════════════
           DETAILS DIALOG
      ══════════════════════════════════════════ -->
      <Teleport to="body">
        <div class="modal-overlay" v-if="detailsDialog.open" @click.self="detailsDialog.open = false">
          <div class="modal-box modal-lg" v-if="detailsDialog.customer">
            <div class="modal-header">
              <div class="details-header-info">
                <div class="details-avatar" :style="{ background: getAvatarColor(detailsDialog.customer.fullName) }">
                  {{ getInitials(detailsDialog.customer.fullName) }}
                </div>
                <div>
                  <h2 class="modal-title">{{ detailsDialog.customer.fullName }}</h2>
                  <p class="modal-subtitle">#{{ detailsDialog.customer.id }} · {{ detailsDialog.customer.email }}</p>
                </div>
              </div>
              <button class="modal-close" @click="detailsDialog.open = false">×</button>
            </div>
            <div class="modal-body">
              <div class="details-grid">
                <div class="detail-card">
                  <h3 class="detail-card-title">Thông tin cá nhân</h3>
                  <div class="detail-rows">
                    <div class="detail-row"><span class="detail-key">ID</span><span class="detail-val">#{{ detailsDialog.customer.id }}</span></div>
                    <div class="detail-row"><span class="detail-key">Họ tên</span><strong class="detail-val">{{ detailsDialog.customer.fullName }}</strong></div>
                    <div class="detail-row"><span class="detail-key">Email</span><span class="detail-val">{{ detailsDialog.customer.email }}</span></div>
                    <div class="detail-row"><span class="detail-key">SĐT</span><span class="detail-val">{{ detailsDialog.customer.phone || '—' }}</span></div>
                    <div class="detail-row"><span class="detail-key">Ngày sinh</span><span class="detail-val">{{ detailsDialog.customer.birthDate || '—' }}</span></div>
                    <div class="detail-row"><span class="detail-key">Địa chỉ</span><span class="detail-val">{{ detailsDialog.customer.address || '—' }}</span></div>
                  </div>
                </div>
                <div class="detail-card">
                  <h3 class="detail-card-title">Loyalty & VIP</h3>
                  <div class="detail-rows">
                    <div class="detail-row">
                      <span class="detail-key">Loại KH</span>
                      <span :class="['type-badge', detailsDialog.customer.customerType === 'VIP' ? 'type-vip' : 'type-regular']">{{ detailsDialog.customer.customerType }}</span>
                    </div>
                    <div class="detail-row">
                      <span class="detail-key">VIP Tier</span>
                      <span v-if="detailsDialog.customer.raw.vipTier" :class="['tier-badge', `tier-${detailsDialog.customer.raw.vipTier.toLowerCase()}`]">
                        {{ tierIcon(detailsDialog.customer.raw.vipTier) }} {{ detailsDialog.customer.raw.vipTier }}
                      </span>
                      <span v-else class="text-muted-sm">Chưa có Tier</span>
                    </div>
                    <div class="detail-row">
                      <span class="detail-key">Điểm tích lũy</span>
                      <strong class="detail-val text-blue">{{ (detailsDialog.customer.loyaltyPoints || 0).toLocaleString() }} pts</strong>
                    </div>
                    <div class="detail-row">
                      <span class="detail-key">Tổng chi tiêu</span>
                      <strong class="detail-val text-green">{{ formatCurrency(detailsDialog.customer.raw.totalSpent) }}</strong>
                    </div>
                  </div>
                </div>
                <div class="detail-card" v-if="detailsDialog.customer.notes">
                  <h3 class="detail-card-title">Ghi chú thông thường</h3>
                  <p class="detail-text">{{ detailsDialog.customer.notes }}</p>
                </div>

                <!-- ⭐ VIP NOTE CARD: CHỈ HIỆN KHI KHÁCH CÓ VIP TIER -->
                <div class="detail-card vip-note-detail-card" v-if="detailsDialog.customer.raw.vipTier">
                  <div class="vip-note-card-header">
                    <h3 class="vip-card-title">
                      ⭐ Ghi chú VIP
                      <span class="vip-internal-badge">Internal Only</span>
                    </h3>
                    <button class="btn-vip-edit" @click="openVipNoteEdit(detailsDialog.customer)">✏️ Sửa</button>
                  </div>
                  <p v-if="detailsDialog.customer.raw.vipNote" class="vip-note-content">
                    {{ detailsDialog.customer.raw.vipNote }}
                  </p>
                  <p v-else class="vip-note-empty">
                    Chưa có ghi chú VIP.
                    <button class="link-btn" @click="openVipNoteEdit(detailsDialog.customer)">Thêm ngay →</button>
                  </p>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="detailsDialog.open = false">Đóng</button>
              <button class="btn-primary" @click="openEdit(detailsDialog.customer)">✏️ Chỉnh sửa</button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- ══════════════════════════════════════════
           VIP NOTE QUICK-EDIT DIALOG ⭐
      ══════════════════════════════════════════ -->
      <Teleport to="body">
        <div class="modal-overlay" v-if="vipNoteDialog.open" @click.self="vipNoteDialog.open = false">
          <div class="modal-box modal-sm vip-note-modal">
            <div class="modal-header vip-modal-header">
              <div>
                <h2 class="modal-title">⭐ Ghi chú VIP</h2>
                <p class="modal-subtitle vip-subtitle" v-if="vipNoteDialog.customerName">
                  {{ tierIcon(vipNoteDialog.vipTier) }} {{ vipNoteDialog.vipTier }} · {{ vipNoteDialog.customerName }}
                </p>
              </div>
              <button class="modal-close" @click="vipNoteDialog.open = false">×</button>
            </div>
            <div class="modal-body">
              <div class="vip-modal-notice">
                <span class="notice-icon">🔒</span>
                <div>
                  <strong>Ghi chú nội bộ</strong> — Khách hàng <em>không thể xem</em> thông tin này.
                  Chỉ Admin và Staff có quyền xem và chỉnh sửa.
                </div>
              </div>
              <div class="vip-suggestions" v-if="!vipNoteDialog.note">
                <p class="suggestions-label">Gợi ý nhanh:</p>
                <div class="suggestion-chips">
                  <button v-for="s in vipSuggestions" :key="s" class="chip" @click="vipNoteDialog.note = s">{{ s }}</button>
                </div>
              </div>
              <textarea
                v-model="vipNoteDialog.note"
                class="form-textarea vip-textarea vip-textarea-lg"
                rows="5"
                placeholder="Nhập ghi chú nội bộ về khách VIP..."
                maxlength="500"
                ref="vipNoteTextarea"
              />
              <div class="vip-note-footer-row">
                <span class="char-count">{{ (vipNoteDialog.note || '').length }}/500</span>
                <button v-if="vipNoteDialog.note" class="link-btn text-danger" @click="vipNoteDialog.note = ''">Xóa nội dung</button>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="vipNoteDialog.open = false">Hủy</button>
              <button class="btn-vip-save" :disabled="vipNoteDialog.loading" @click="saveVipNote">
                <span v-if="vipNoteDialog.loading" class="spin-icon">⟳</span>
                ⭐ Lưu VIP Note
              </button>
            </div>
          </div>
        </div>
      </Teleport>
      <!-- ══════════════════════════════════════════
     LOYALTY SUMMARY DIALOG (Admin)
══════════════════════════════════════════ -->
<Teleport to="body">
  <div class="modal-overlay" v-if="summaryDlg.open" @click.self="summaryDlg.open = false">
    <div class="modal-box modal-xl">
      <div class="modal-header">
        <div>
          <h2 class="modal-title">📊 Tổng hợp điểm Loyalty</h2>
          <p class="modal-subtitle">Thống kê điểm tích lũy của tất cả khách hàng</p>
        </div>
        <button class="modal-close" @click="summaryDlg.open = false">×</button>
      </div>

      <!-- Controls -->
      <div class="summary-controls">
        <div class="summary-controls-left">
          <div class="control-group">
            <span class="control-label">Chu kỳ:</span>
            <div class="toggle-group">
              <button :class="['toggle-btn', summaryDlg.mode === 'monthly' && 'toggle-active']"
                @click="summaryDlg.mode = 'monthly'; loadAdminSummary()">Tháng</button>
              <button :class="['toggle-btn', summaryDlg.mode === 'weekly' && 'toggle-active']"
                @click="summaryDlg.mode = 'weekly'; loadAdminSummary()">Tuần</button>
            </div>
          </div>
          <div class="control-group">
            <span class="control-label">{{ summaryDlg.mode === 'weekly' ? 'Số tuần:' : 'Số tháng:' }}</span>
            <select class="filter-select control-select" v-model="summaryDlg.range" @change="loadAdminSummary">
              <template v-if="summaryDlg.mode === 'weekly'">
                <option :value="4">4 tuần</option>
                <option :value="8">8 tuần</option>
                <option :value="12">12 tuần</option>
                <option :value="26">26 tuần</option>
              </template>
              <template v-else>
                <option :value="3">3 tháng</option>
                <option :value="6">6 tháng</option>
                <option :value="12">12 tháng</option>
                <option :value="24">24 tháng</option>
              </template>
            </select>
          </div>
          <div class="control-group">
            <span class="control-label">Xem:</span>
            <div class="toggle-group">
              <button :class="['toggle-btn', summaryDlg.view === 'overview' && 'toggle-active']"
                @click="summaryDlg.view = 'overview'">Tổng quan</button>
              <button :class="['toggle-btn', summaryDlg.view === 'detail' && 'toggle-active']"
                @click="summaryDlg.view = 'detail'">Chi tiết KH</button>
            </div>
          </div>
        </div>
        <button class="btn-ghost" style="font-size:12px;padding:6px 12px;" @click="loadAdminSummary" :disabled="summaryDlg.loading">
          <span v-if="summaryDlg.loading" class="spin-icon">⟳</span>
          <span v-else>↺</span> Tải lại
        </button>
      </div>

      <div class="modal-body summary-modal-body">
        <!-- Loading -->
        <div v-if="summaryDlg.loading" class="summary-loading">
          <div class="loader-dots"><span/><span/><span/></div>
          <p>Đang tải dữ liệu...</p>
        </div>

        <!-- Empty -->
        <div v-else-if="!summaryDlg.data.length" class="summary-empty">
          <span style="font-size:36px">📊</span>
          <p>Chưa có dữ liệu trong khoảng thời gian này</p>
        </div>

        <template v-else>
          <!-- Totals row -->
          <div class="summary-totals-row">
            <div class="summary-total-card earn">
              <div class="stc-value">+{{ adminSummaryTotals.earned.toLocaleString() }}</div>
              <div class="stc-label">Tổng điểm cộng</div>
            </div>
            <div class="summary-total-card deduct">
              <div class="stc-value">-{{ adminSummaryTotals.deducted.toLocaleString() }}</div>
              <div class="stc-label">Tổng điểm trừ</div>
            </div>
            <div class="summary-total-card net" :class="adminSummaryTotals.net >= 0 ? 'net-positive' : 'net-negative'">
              <div class="stc-value">{{ adminSummaryTotals.net >= 0 ? '+' : '' }}{{ adminSummaryTotals.net.toLocaleString() }}</div>
              <div class="stc-label">Net điểm</div>
            </div>
            <div class="summary-total-card tx">
              <div class="stc-value">{{ adminSummaryTotals.transactions.toLocaleString() }}</div>
              <div class="stc-label">Tổng giao dịch</div>
            </div>
          </div>

          <!-- VIEW: Tổng quan theo kỳ -->
          <template v-if="summaryDlg.view === 'overview'">
            <div class="summary-period-list">
              <div v-for="period in summaryDlg.data" :key="period.period" class="summary-period-row">
                <div class="period-info">
                  <div class="period-label">{{ period.periodLabel }}</div>
                  <div class="period-dates">{{ formatAdminDate(period.periodStart) }} – {{ formatAdminDate(period.periodEnd) }}</div>
                </div>
                <div class="period-bars">
                  <div class="bar-row">
                    <span class="bar-label earn-label">+{{ period.totalPointsEarned.toLocaleString() }}</span>
                    <div class="bar-track">
                      <div class="bar-fill bar-earn"
                        :style="{ width: getAdminBarWidth(period.totalPointsEarned, 'earn') + '%' }" />
                    </div>
                  </div>
                  <div class="bar-row">
                    <span class="bar-label deduct-label">-{{ period.totalPointsDeducted.toLocaleString() }}</span>
                    <div class="bar-track">
                      <div class="bar-fill bar-deduct"
                        :style="{ width: getAdminBarWidth(period.totalPointsDeducted, 'deduct') + '%' }" />
                    </div>
                  </div>
                </div>
                <div class="period-net">
                  <span class="net-badge" :class="period.netPoints >= 0 ? 'net-pos' : 'net-neg'">
                    {{ period.netPoints >= 0 ? '+' : '' }}{{ period.netPoints.toLocaleString() }}
                  </span>
                  <span class="period-tx">{{ period.totalTransactions }} GD</span>
                </div>
              </div>
            </div>
          </template>

          <!-- VIEW: Chi tiết theo KH -->
          <template v-else>
            <div v-for="period in summaryDlg.data" :key="period.period" class="customer-period-block">
              <div class="customer-period-header">
                <span class="customer-period-title">{{ period.periodLabel }}</span>
                <div class="customer-period-meta">
                  <span class="meta-earn">+{{ period.totalPointsEarned.toLocaleString() }}</span>
                  <span class="meta-sep">/</span>
                  <span class="meta-deduct">-{{ period.totalPointsDeducted.toLocaleString() }}</span>
                  <span class="meta-tx">{{ period.totalTransactions }} GD</span>
                </div>
              </div>
              <table class="customer-detail-table" v-if="period.customerBreakdown?.length">
                <thead>
                  <tr>
                    <th>Khách hàng</th>
                    <th>VIP Tier</th>
                    <th style="text-align:right">Cộng</th>
                    <th style="text-align:right">Trừ</th>
                    <th style="text-align:right">Net</th>
                    <th style="text-align:center">GD</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="c in period.customerBreakdown" :key="c.customerId">
                    <td>
                      <div class="cdt-name">{{ c.customerName }}</div>
                      <div class="cdt-email">{{ c.customerEmail }}</div>
                    </td>
                    <td>
                      <span v-if="c.vipTier && c.vipTier !== 'Member'"
                        :class="['tier-badge', `tier-${c.vipTier.toLowerCase()}`]">
                        {{ tierIcon(c.vipTier) }} {{ c.vipTier }}
                      </span>
                      <span v-else class="text-muted-sm">—</span>
                    </td>
                    <td style="text-align:right;color:#67c23a;font-weight:600">+{{ c.pointsEarned.toLocaleString() }}</td>
                    <td style="text-align:right;color:#e6a23c;font-weight:600">
                      {{ c.pointsDeducted > 0 ? '-' : '' }}{{ c.pointsDeducted.toLocaleString() }}
                    </td>
                    <td style="text-align:right">
                      <span :style="{ color: c.netPoints >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 700 }">
                        {{ c.netPoints >= 0 ? '+' : '' }}{{ c.netPoints.toLocaleString() }}
                      </span>
                    </td>
                    <td style="text-align:center;color:#909399">{{ c.transactionCount }}</td>
                  </tr>
                </tbody>
              </table>
              <div v-else class="cdt-empty">Không có giao dịch</div>
            </div>
          </template>
        </template>
      </div>

      <div class="modal-footer">
        <button class="btn-ghost" @click="summaryDlg.open = false">Đóng</button>
      </div>
    </div>
  </div>
</Teleport>

    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

// ══════════════════════════════
// STATE
// ══════════════════════════════
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

// ── Admin Loyalty Summary ─────────────────────────────────────────────────────
const summaryDlg = reactive({
  open: false,
  loading: false,
  mode: 'monthly',   // 'weekly' | 'monthly'
  range: 6,
  view: 'overview',  // 'overview' | 'detail'
  data: [],
});

const adminSummaryTotals = computed(() => {
  const earned       = summaryDlg.data.reduce((s, r) => s + (r.totalPointsEarned   || 0), 0);
  const deducted     = summaryDlg.data.reduce((s, r) => s + (r.totalPointsDeducted || 0), 0);
  const transactions = summaryDlg.data.reduce((s, r) => s + (r.totalTransactions   || 0), 0);
  return { earned, deducted, net: earned - deducted, transactions };
});

function openSummaryDialog() {
  summaryDlg.open = true;
  if (!summaryDlg.data.length) loadAdminSummary();
}

async function loadAdminSummary() {
  summaryDlg.loading = true;
  try {
    const res = summaryDlg.mode === 'weekly'
      ? await customersApi.getLoyaltyWeeklySummaryAdmin(summaryDlg.range)
      : await customersApi.getLoyaltyMonthlySummaryAdmin(summaryDlg.range);
    const raw = res?.data;
    summaryDlg.data = Array.isArray(raw) ? raw
      : Array.isArray(raw?.data) ? raw.data : [];
  } catch (e) {
    toast('Lỗi khi tải tổng hợp điểm', 'error');
    summaryDlg.data = [];
  } finally {
    summaryDlg.loading = false;
  }
}

function getAdminBarWidth(value, type) {
  if (!summaryDlg.data.length) return 0;
  const maxVal = Math.max(
    ...summaryDlg.data.map(r =>
      type === 'earn' ? r.totalPointsEarned : r.totalPointsDeducted
    )
  );
  return maxVal === 0 ? 0 : Math.round((value / maxVal) * 100);
}

function formatAdminDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}
const dlg = reactive({
  open: false, mode: "create", loading: false, alert: "", id: null,
  form: { fullName: "", email: "", phone: "", birthDate: "", customerType: "REGULAR", address: "", notes: "", vipNote: "", vipTier: "" },
});

const detailsDialog = reactive({ open: false, customer: null });

const vipNoteDialog = reactive({
  open: false, loading: false,
  customerId: null, customerName: "", vipTier: "", note: "",
});

const vipSuggestions = [
  "Thích dòng gaming cao cấp",
  "Không thích hàng trưng bày",
  "Ưu tiên giao hàng buổi sáng",
  "Khách doanh nghiệp, cần VAT",
  "Phản hồi chậm → review xấu",
  "Top spender, ưu tiên tư vấn trực tiếp",
];

// ══════════════════════════════
// COMPUTED
// ══════════════════════════════
const hasActiveFilters = computed(() =>
  !!(typeFilter.value || vipTierFilter.value || activityFilter.value || isPointsFilterApplied.value)
);
const isPointsFilterApplied = computed(() => appliedPointsMin.value !== null || appliedPointsMax.value !== null);
const canApplyPointsFilter = computed(() => pointsMin.value !== null || pointsMax.value !== null);
const vipCount = computed(() => rows.value.filter(r => r.customerType === 'VIP').length);
const totalPoints = computed(() => rows.value.reduce((s, r) => s + (r.loyaltyPoints || 0), 0));
const avgPoints = computed(() => !rows.value.length ? 0 : Math.round(totalPoints.value / rows.value.length));
const filtered = computed(() => {
  const kw = (q.value || "").trim().toLowerCase();
  if (!kw) return rows.value;
  return rows.value.filter(r => `${r.fullName} ${r.email} ${r.phone}`.toLowerCase().includes(kw));
});
const pageCount = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)));
const paged = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize));

// ══════════════════════════════
// HELPERS
// ══════════════════════════════
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
    birthDate: c?.birthDate ?? c?.dateOfBirth ?? "",
    customerType: (c?.customerType ?? "REGULAR").toString().toUpperCase(),
    loyaltyPoints: c?.loyaltyPoints ?? 0,
    address: c?.address ?? "",
    notes: c?.notes ?? "",
    vipNote: c?.vipNote ?? "",
    raw: c,
  }));
}

function tierIcon(tier) {
  return { BRONZE: '🥉', SILVER: '🥈', GOLD: '🥇', PLATINUM: '💠', DIAMOND: '💎' }[tier] || '';
}
function getTierColor(tier) {
  return { BRONZE: '#cd7f32', SILVER: '#a8a9ad', GOLD: '#f0b429', PLATINUM: '#5b8dee', DIAMOND: '#22c55e' }[tier] || '#6b7280';
}
function getPointsProgress(customer) {
  const map = { BRONZE:[0,500], SILVER:[500,1500], GOLD:[1500,3000], PLATINUM:[3000,6000], DIAMOND:[6000,10000] };
  const r = map[customer.vipTier]; if (!r) return 0;
  return Math.min(100, Math.max(0, ((customer.loyaltyPoints - r[0]) / (r[1] - r[0])) * 100));
}
function getInitials(name) {
  if (!name) return '?';
  const p = name.trim().split(' ');
  return p.length === 1 ? p[0].substring(0, 2).toUpperCase() : (p[0][0] + p[p.length-1][0]).toUpperCase();
}
function getAvatarColor(name) {
  const colors = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#06b6d4','#f97316','#84cc16','#ec4899','#6366f1'];
  const hash = (name || '').split('').reduce((a, c) => a + c.charCodeAt(0), 0);
  return colors[hash % colors.length];
}
function formatCurrency(v) {
  if (!v) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
}
function formatCurrencyShort(v) {
  if (!v || v === 0) return '—';
  if (v >= 1_000_000_000) return (v / 1_000_000_000).toFixed(1) + ' tỷ';
  if (v >= 1_000_000) return (v / 1_000_000).toFixed(1) + 'M';
  if (v >= 1_000) return (v / 1_000).toFixed(0) + 'K';
  return v + ' ₫';
}

// ══════════════════════════════
// API / DATA
// ══════════════════════════════
async function load() {
  loading.value = true;
  try {
    let res;
    if (isPointsFilterApplied.value && vipTierFilter.value)
      res = await customersApi.listByVipTierAndPoints(vipTierFilter.value, appliedPointsMin.value || 0, appliedPointsMax.value || 999999);
    else if (isPointsFilterApplied.value)
      res = await customersApi.listByPointsRange(appliedPointsMin.value || 0, appliedPointsMax.value || 999999);
    else if (vipTierFilter.value)
      res = await customersApi.listByVipTier(vipTierFilter.value);
    else if (activityFilter.value === 'ACTIVE_30')
      res = await customersApi.listActiveLast30Days();
    else if (typeFilter.value)
      res = await customersApi.listByType(typeFilter.value);
    else
      res = await customersApi.listAll();
    rows.value = normalize(extractList(res?.data));
    page.value = 1;
  } catch (e) {
    console.error(e);
    rows.value = [];
    toast("Không thể tải dữ liệu khách hàng", "error");
  } finally {
    loading.value = false;
  }
}

function handleFilterChange() {
  if (activityFilter.value || typeFilter.value || vipTierFilter.value) {
    appliedPointsMin.value = null; appliedPointsMax.value = null;
    pointsMin.value = null; pointsMax.value = null;
  }
  load();
}

function applyPointsFilter() {
  if (!canApplyPointsFilter.value) { toast("Nhập ít nhất 1 giá trị điểm", "warning"); return; }
  if (pointsMin.value !== null && pointsMax.value !== null && pointsMin.value > pointsMax.value) {
    toast("Điểm tối thiểu không được lớn hơn tối đa", "error"); return;
  }
  appliedPointsMin.value = pointsMin.value;
  appliedPointsMax.value = pointsMax.value;
  activityFilter.value = ""; typeFilter.value = "";
  load();
}

function clearPointsFilter() {
  pointsMin.value = null; pointsMax.value = null;
  appliedPointsMin.value = null; appliedPointsMax.value = null;
  load();
}

function clearAllFilters() {
  typeFilter.value = ""; vipTierFilter.value = ""; activityFilter.value = "";
  pointsMin.value = null; pointsMax.value = null;
  appliedPointsMin.value = null; appliedPointsMax.value = null;
  load();
}

// ══════════════════════════════
// DIALOG — CREATE / EDIT
// ══════════════════════════════
function openCreate() {
  dlg.open = true; dlg.mode = "create"; dlg.alert = ""; dlg.id = null;
  dlg.form = { fullName: "", email: "", phone: "", birthDate: "", customerType: "REGULAR", address: "", notes: "", vipNote: "", vipTier: "" };
}

function openEdit(row) {
  dlg.open = true; dlg.mode = "edit"; dlg.alert = ""; dlg.id = row?.id;
  dlg.form = {
    fullName: row?.fullName || "",
    email: row?.email || "",
    phone: row?.phone || "",
    birthDate: row?.birthDate || "",
    customerType: row?.customerType || "REGULAR",
    address: row?.address || "",
    notes: row?.notes || "",
    vipNote: row?.raw?.vipNote || "",
    vipTier: row?.raw?.vipTier || "",  // ← dùng để v-if hiện/ẩn field VIP Note
  };
  detailsDialog.open = false;
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.fullName || !dlg.form.email || !dlg.form.phone) {
    dlg.alert = "Họ tên, email và SĐT là bắt buộc."; return;
  }
  dlg.loading = true;
  try {
    const payload = { ...dlg.form };
    delete payload.vipTier; // vipTier là read-only, không gửi lên server
    if (dlg.mode === "create") {
      await customersApi.create(payload);
      toast("Tạo khách hàng thành công!", "success");
    } else {
      await customersApi.update(dlg.id, payload);
      toast("Cập nhật thành công!", "success");
    }
    dlg.open = false;
    await load();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Lưu thất bại";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

async function remove(row) {
  const ok = await confirmModal(`Xóa khách hàng "${row?.fullName}"?`, "Xác nhận xóa", "Xóa", true);
  if (!ok) return;
  try {
    await customersApi.remove(row.id);
    toast("Đã xóa khách hàng!", "success");
    await load();
  } catch { toast("Xóa thất bại.", "error"); }
}

// ══════════════════════════════
// DIALOG — DETAILS
// ══════════════════════════════
function viewDetails(row) {
  detailsDialog.customer = row;
  detailsDialog.open = true;
}

// ══════════════════════════════
// VIP NOTE ⭐
// Guard: chỉ mở được với khách có vipTier
// ══════════════════════════════
function openVipNoteEdit(row) {
  if (!row.raw?.vipTier) return; // không làm gì nếu chưa có tier
  vipNoteDialog.customerId = row.id;
  vipNoteDialog.customerName = row.fullName;
  vipNoteDialog.vipTier = row.raw.vipTier;
  vipNoteDialog.note = row.raw?.vipNote || "";
  vipNoteDialog.open = true;
  nextTick(() => vipNoteTextarea.value?.focus());
}

async function saveVipNote() {
  vipNoteDialog.loading = true;
  try {
    await customersApi.updateVipNote(vipNoteDialog.customerId, vipNoteDialog.note);
    toast("VIP Note đã được lưu! ⭐", "success");
    vipNoteDialog.open = false;
    await load();
    if (detailsDialog.open && detailsDialog.customer?.id === vipNoteDialog.customerId) {
      detailsDialog.customer.raw.vipNote = vipNoteDialog.note;
      detailsDialog.customer.vipNote = vipNoteDialog.note;
    }
  } catch { toast("Lưu VIP Note thất bại", "error"); }
  finally { vipNoteDialog.loading = false; }
}

onMounted(load);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;500;600;700&family=IBM+Plex+Mono:wght@400;500&display=swap');
*, *::before, *::after { box-sizing: border-box; }

.page-wrapper { font-family: 'IBM Plex Sans', sans-serif; background: #f4f5f7; min-height: 100vh; padding: 24px; }
.page-inner { max-width: 1600px; margin: 0 auto; }

/* HEADER */
.page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; flex-wrap: wrap; margin-bottom: 20px; }
.header-eyebrow { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 1.5px; color: #6b7280; margin-bottom: 4px; }
.header-title { font-size: 28px; font-weight: 700; color: #111827; margin: 0 0 4px; line-height: 1.2; }
.title-accent { color: #3b82f6; }
.header-sub { font-size: 12px; color: #9ca3af; margin: 0; font-family: 'IBM Plex Mono', monospace; }
.header-sub code { background: #e5e7eb; padding: 1px 6px; border-radius: 4px; font-size: 11px; }
.header-actions { display: flex; gap: 8px; align-items: center; }

/* BUTTONS */
.btn-primary { background: #1d4ed8; color: #fff; border: none; padding: 9px 18px; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 6px; transition: background 0.15s; font-family: 'IBM Plex Sans', sans-serif; }
.btn-primary:hover { background: #1e40af; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-ghost { background: transparent; color: #374151; border: 1px solid #d1d5db; padding: 8px 16px; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; display: flex; align-items: center; gap: 6px; transition: all 0.15s; font-family: 'IBM Plex Sans', sans-serif; }
.btn-ghost:hover { background: #f9fafb; border-color: #9ca3af; }
.btn-ghost:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-outline-sm { background: transparent; color: #374151; border: 1px solid #d1d5db; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer; transition: all 0.15s; white-space: nowrap; font-family: 'IBM Plex Sans', sans-serif; }
.btn-outline-sm:hover { background: #f3f4f6; }
.btn-outline-sm:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-blue-sm { color: #1d4ed8; border-color: #bfdbfe; background: #eff6ff; }
.btn-blue-sm:hover { background: #dbeafe; }
.btn-vip-save { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; border: none; padding: 9px 20px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 6px; transition: all 0.15s; font-family: 'IBM Plex Sans', sans-serif; box-shadow: 0 2px 8px rgba(245,158,11,0.35); }
.btn-vip-save:hover { background: linear-gradient(135deg, #d97706, #b45309); }
.btn-vip-save:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-vip-edit { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.15s; font-family: 'IBM Plex Sans', sans-serif; }
.btn-vip-edit:hover { background: #fde68a; }
.link-btn { background: none; border: none; color: #3b82f6; font-size: 12px; cursor: pointer; padding: 0; font-family: 'IBM Plex Sans', sans-serif; text-decoration: underline; }
.text-danger { color: #ef4444; }

/* STATS BAR */
.stats-bar { display: flex; align-items: center; background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 12px 20px; margin-bottom: 16px; flex-wrap: wrap; gap: 4px; }
.stat-pill { display: flex; flex-direction: column; align-items: center; padding: 0 20px; }
.stat-num { font-size: 22px; font-weight: 700; color: #111827; line-height: 1.2; font-family: 'IBM Plex Mono', monospace; }
.stat-label { font-size: 11px; color: #9ca3af; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-divider { width: 1px; height: 36px; background: #e5e7eb; flex-shrink: 0; }
.text-gold { color: #f59e0b; }
.text-blue { color: #3b82f6; }
.text-green { color: #10b981; }
.text-orange { color: #f97316; }

/* FILTERS */
.filter-panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 16px 20px; margin-bottom: 16px; }
.filter-row { display: flex; gap: 12px; flex-wrap: wrap; align-items: flex-end; margin-bottom: 12px; }
.filter-row:last-child { margin-bottom: 0; }
.filter-row-points { padding-top: 12px; border-top: 1px dashed #e5e7eb; }
.filter-group { display: flex; flex-direction: column; gap: 4px; min-width: 140px; flex: 1; }
.filter-group.filter-action { flex: 0; min-width: auto; }
.filter-group.filter-search { flex: 2; }
.filter-label { font-size: 11px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; }
.filter-input { border: 1px solid #d1d5db; border-radius: 7px; padding: 7px 10px; font-size: 13px; font-family: 'IBM Plex Sans', sans-serif; background: #fff; color: #111827; outline: none; transition: border-color 0.15s; width: 100%; }
.filter-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.input-icon-wrap { position: relative; }
.input-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #9ca3af; pointer-events: none; }
.input-icon-wrap .filter-input { padding-left: 30px; }
.filter-select { border: 1px solid #d1d5db; border-radius: 7px; padding: 7px 10px; font-size: 13px; font-family: 'IBM Plex Sans', sans-serif; background: #fff; color: #111827; outline: none; cursor: pointer; width: 100%; transition: border-color 0.15s; }
.filter-select:focus { border-color: #3b82f6; }
.active-tags { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; margin-top: 10px; padding-top: 10px; border-top: 1px solid #f3f4f6; }
.tag-label { font-size: 11px; color: #9ca3af; font-weight: 600; }
.filter-tag { display: inline-flex; align-items: center; gap: 4px; background: #f3f4f6; color: #374151; border: 1px solid #d1d5db; padding: 3px 8px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.filter-tag button { background: none; border: none; cursor: pointer; color: inherit; padding: 0; font-size: 13px; line-height: 1; margin-left: 2px; }
.filter-tag-gold { background: #fffbeb; color: #92400e; border-color: #fde68a; }
.filter-tag-green { background: #f0fdf4; color: #15803d; border-color: #bbf7d0; }
.filter-tag-blue { background: #eff6ff; color: #1e40af; border-color: #bfdbfe; }

/* TABLE */
.table-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; overflow: hidden; }
.table-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px; gap: 12px; color: #9ca3af; font-size: 13px; }
.loader-dots { display: flex; gap: 6px; }
.loader-dots span { width: 8px; height: 8px; border-radius: 50%; background: #3b82f6; animation: dotBounce 1.2s infinite; }
.loader-dots span:nth-child(2) { animation-delay: 0.2s; }
.loader-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotBounce { 0%,80%,100%{transform:scale(0.8);opacity:0.5} 40%{transform:scale(1.2);opacity:1} }
.table-scroll { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.data-table thead tr { background: #f9fafb; border-bottom: 1px solid #e5e7eb; }
.data-table th { padding: 10px 12px; text-align: left; font-size: 11px; font-weight: 700; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; white-space: nowrap; }
.data-row { border-bottom: 1px solid #f3f4f6; transition: background 0.1s; }
.data-row:hover { background: #fafbff; }
.data-row:last-child { border-bottom: none; }
.data-table td { padding: 10px 12px; vertical-align: middle; }
.col-id { width: 60px; } .col-name { min-width: 200px; } .col-contact { min-width: 200px; }
.col-type { width: 100px; } .col-tier { width: 130px; } .col-points { width: 120px; }
.col-spent { width: 100px; } .col-vipnote { min-width: 160px; } .col-notes { min-width: 140px; } .col-actions { width: 130px; }
.id-badge { font-family: 'IBM Plex Mono', monospace; font-size: 11px; color: #9ca3af; background: #f3f4f6; padding: 2px 6px; border-radius: 4px; }
.customer-cell { display: flex; align-items: center; gap: 10px; }
.avatar { width: 34px; height: 34px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 12px; flex-shrink: 0; }
.customer-info { display: flex; flex-direction: column; gap: 1px; }
.customer-name { font-weight: 600; color: #111827; }
.customer-dob { font-size: 11px; color: #9ca3af; font-family: 'IBM Plex Mono', monospace; }
.contact-cell { display: flex; flex-direction: column; gap: 2px; }
.contact-email { color: #374151; font-size: 12px; }
.contact-phone { color: #9ca3af; font-size: 11px; font-family: 'IBM Plex Mono', monospace; }
.type-badge { padding: 3px 8px; border-radius: 20px; font-size: 11px; font-weight: 700; }
.type-vip { background: #fef3c7; color: #92400e; }
.type-regular { background: #f3f4f6; color: #6b7280; }
.tier-badge { padding: 3px 8px; border-radius: 6px; font-size: 11px; font-weight: 700; white-space: nowrap; }
.tier-bronze { background: #fdf4eb; color: #92400e; border: 1px solid #fcd9a8; }
.tier-silver { background: #f4f4f5; color: #52525b; border: 1px solid #d4d4d8; }
.tier-gold { background: #fffbeb; color: #92400e; border: 1px solid #fde68a; }
.tier-platinum { background: #eff6ff; color: #1e40af; border: 1px solid #bfdbfe; }
.tier-diamond { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }
.points-cell { display: flex; flex-direction: column; gap: 4px; }
.points-num { font-family: 'IBM Plex Mono', monospace; font-weight: 600; color: #1d4ed8; font-size: 13px; }
.points-bar { height: 3px; background: #e5e7eb; border-radius: 2px; overflow: hidden; width: 80px; }
.points-bar-fill { height: 100%; border-radius: 2px; transition: width 0.3s; }
.spent-amount { font-family: 'IBM Plex Mono', monospace; font-size: 12px; color: #374151; font-weight: 500; }

/* VIP NOTE cell */
.vipnote-cell { display: flex; align-items: flex-start; gap: 4px; cursor: pointer; padding: 4px 6px; border-radius: 6px; background: #fffbeb; border: 1px solid #fde68a; transition: all 0.15s; max-width: 200px; }
.vipnote-cell:hover { background: #fef3c7; }
.vipnote-star { font-size: 12px; flex-shrink: 0; line-height: 1.4; }
.vipnote-text { font-size: 12px; color: #78350f; line-height: 1.4; word-break: break-word; }
.vipnote-add-btn { background: none; border: 1px dashed #fde68a; color: #f59e0b; padding: 4px 8px; border-radius: 6px; font-size: 11px; cursor: pointer; transition: all 0.15s; font-family: 'IBM Plex Sans', sans-serif; font-weight: 600; }
.vipnote-add-btn:hover { background: #fffbeb; border-color: #f59e0b; }
.note-text { font-size: 12px; color: #6b7280; }
.text-muted-sm { font-size: 12px; color: #d1d5db; }

/* Actions */
.action-group { display: flex; gap: 4px; }
.action-btn { width: 28px; height: 28px; border: 1px solid #e5e7eb; background: #fff; border-radius: 6px; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 13px; transition: all 0.15s; color: #6b7280; }
.action-btn:hover { border-color: #9ca3af; }
.action-view:hover { background: #eff6ff; border-color: #bfdbfe; color: #1d4ed8; }
.action-edit:hover { background: #f0fdf4; border-color: #bbf7d0; color: #15803d; }
.action-star:hover { background: #fffbeb; border-color: #fde68a; }
.action-del:hover { background: #fef2f2; border-color: #fecaca; color: #ef4444; }
.empty-row { padding: 60px; text-align: center; }
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #9ca3af; }
.empty-icon { font-size: 36px; }
.empty-state p { font-size: 14px; }
.table-footer { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-top: 1px solid #f3f4f6; background: #fafafa; flex-wrap: wrap; gap: 8px; }
.pagination-info { font-size: 12px; color: #9ca3af; }
.pagination { display: flex; gap: 4px; }
.page-btn { min-width: 32px; height: 32px; border: 1px solid #e5e7eb; background: #fff; border-radius: 6px; font-size: 13px; cursor: pointer; display: flex; align-items: center; justify-content: center; font-family: 'IBM Plex Sans', sans-serif; transition: all 0.15s; color: #374151; padding: 0 6px; }
.page-btn:hover:not(:disabled) { background: #f3f4f6; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-btn-active { background: #1d4ed8 !important; color: #fff !important; border-color: #1d4ed8; font-weight: 700; }

/* MODALS */
.modal-overlay { position: fixed; inset: 0; background: rgba(17,24,39,0.5); backdrop-filter: blur(2px); z-index: 1000; display: flex; align-items: center; justify-content: center; padding: 20px; }
.modal-box { background: #fff; border-radius: 12px; width: 100%; max-height: 90vh; overflow-y: auto; box-shadow: 0 25px 60px rgba(0,0,0,0.2); display: flex; flex-direction: column; }
.modal-lg { max-width: 780px; }
.modal-sm { max-width: 500px; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 18px 24px; border-bottom: 1px solid #f3f4f6; flex-shrink: 0; }
.modal-title { font-size: 18px; font-weight: 700; color: #111827; margin: 0; }
.modal-subtitle { font-size: 12px; color: #9ca3af; margin: 2px 0 0; font-family: 'IBM Plex Mono', monospace; }
.modal-close { background: none; border: none; font-size: 22px; cursor: pointer; color: #9ca3af; line-height: 1; padding: 0; }
.modal-close:hover { color: #111827; }
.modal-alert { background: #fef2f2; border-left: 3px solid #ef4444; color: #991b1b; padding: 10px 24px; font-size: 13px; }
.modal-body { padding: 20px 24px; flex: 1; }
.modal-footer { padding: 14px 24px; border-top: 1px solid #f3f4f6; display: flex; justify-content: flex-end; gap: 8px; flex-shrink: 0; }

/* Form */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group-full { grid-column: 1 / -1; }
.form-label { font-size: 12px; font-weight: 600; color: #374151; }
.required { color: #ef4444; }
.form-input, .form-select, .form-textarea { border: 1px solid #d1d5db; border-radius: 7px; padding: 8px 11px; font-size: 13px; font-family: 'IBM Plex Sans', sans-serif; color: #111827; outline: none; transition: border-color 0.15s; background: #fff; width: 100%; }
.form-input:focus, .form-select:focus, .form-textarea:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.form-textarea { resize: vertical; }

/* VIP Note form section */
.vip-note-form-section { border: 1px solid #fde68a; border-radius: 8px; padding: 14px; background: #fffbf0; }
.vip-note-form-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.vip-tier-indicator { font-size: 12px; font-weight: 700; color: #92400e; background: #fef3c7; border: 1px solid #fde68a; padding: 2px 8px; border-radius: 6px; }
.vip-label { display: flex; align-items: center; gap: 6px; color: #92400e; }
.vip-badge-label { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; padding: 1px 6px; border-radius: 4px; font-size: 10px; font-weight: 700; text-transform: uppercase; }
.vip-textarea { border-color: #fde68a; background: #fffbeb; }
.vip-textarea:focus { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245,158,11,0.15); }
.vip-note-hint { font-size: 11px; color: #6b7280; display: flex; justify-content: space-between; align-items: center; margin-top: 6px; padding: 6px 8px; background: #fef9ec; border-radius: 5px; }
.char-count { font-size: 11px; color: #9ca3af; font-family: 'IBM Plex Mono', monospace; }
.vip-note-unavailable { display: flex; align-items: center; gap: 8px; background: #f9fafb; border: 1px dashed #d1d5db; border-radius: 8px; padding: 10px 14px; font-size: 12px; color: #9ca3af; font-style: italic; }

/* Details dialog */
.details-header-info { display: flex; align-items: center; gap: 12px; }
.details-avatar { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 16px; flex-shrink: 0; }
.details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-card { border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden; }
.detail-card-title { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: #6b7280; background: #f9fafb; padding: 10px 14px; margin: 0; border-bottom: 1px solid #e5e7eb; }
.detail-rows { padding: 8px 14px; }
.detail-row { display: flex; justify-content: space-between; align-items: center; padding: 7px 0; border-bottom: 1px solid #f9fafb; font-size: 13px; }
.detail-row:last-child { border-bottom: none; }
.detail-key { color: #6b7280; font-weight: 500; }
.detail-val { color: #111827; text-align: right; }
.detail-text { padding: 12px 14px; font-size: 13px; color: #374151; margin: 0; }

/* VIP Note detail card */
.vip-note-detail-card { grid-column: 1 / -1; border-color: #fde68a; background: #fffbeb; }
.vip-note-card-header { display: flex; align-items: center; justify-content: space-between; background: #fef3c7; padding: 10px 14px; border-bottom: 1px solid #fde68a; }
.vip-card-title { display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: #92400e; margin: 0; }
.vip-internal-badge { background: #f59e0b; color: #fff; padding: 1px 7px; border-radius: 4px; font-size: 9px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; }
.vip-note-content { padding: 14px; color: #78350f; font-size: 13px; margin: 0; line-height: 1.6; }
.vip-note-empty { padding: 14px; color: #a16207; font-size: 13px; font-style: italic; margin: 0; }

/* VIP Note Quick-Edit Dialog */
.vip-note-modal .modal-header { background: linear-gradient(135deg, #fffbeb, #fef3c7); border-bottom: 1px solid #fde68a; }
.vip-modal-header .modal-title { color: #92400e; }
.vip-subtitle { color: #a16207 !important; font-weight: 600; }
.vip-modal-notice { display: flex; gap: 10px; align-items: flex-start; background: #fef9c3; border: 1px solid #fde047; border-radius: 8px; padding: 12px 14px; font-size: 12px; color: #713f12; margin-bottom: 14px; line-height: 1.5; }
.notice-icon { font-size: 18px; flex-shrink: 0; }
.vip-suggestions { margin-bottom: 12px; }
.suggestions-label { font-size: 11px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px; }
.suggestion-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip { background: #f9fafb; border: 1px solid #e5e7eb; padding: 4px 10px; border-radius: 20px; font-size: 11px; color: #374151; cursor: pointer; transition: all 0.15s; font-family: 'IBM Plex Sans', sans-serif; }
.chip:hover { background: #fffbeb; border-color: #fde68a; color: #92400e; }
.vip-textarea-lg { min-height: 100px; }
.vip-note-footer-row { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; }

/* Misc */
.spin-icon { display: inline-block; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .page-wrapper { padding: 12px; }
  .form-grid { grid-template-columns: 1fr; }
  .form-group-full { grid-column: 1; }
  .details-grid { grid-template-columns: 1fr; }
  .vip-note-detail-card { grid-column: 1; }
  .filter-row { flex-direction: column; }
  .filter-group { min-width: 100%; }
}
/* Summary Dialog */
.modal-xl { max-width: 1000px; }
.summary-controls {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 24px; border-bottom: 1px solid #f3f4f6;
  background: #fafafa; gap: 12px; flex-wrap: wrap;
}
.summary-controls-left { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.control-group { display: flex; align-items: center; gap: 8px; }
.control-label { font-size: 12px; font-weight: 600; color: #6b7280; white-space: nowrap; }
.control-select { padding: 5px 8px; font-size: 12px; min-width: 100px; }
.toggle-group { display: flex; border: 1px solid #d1d5db; border-radius: 6px; overflow: hidden; }
.toggle-btn { padding: 5px 12px; font-size: 12px; font-weight: 500; border: none; background: #fff; cursor: pointer; font-family: 'IBM Plex Sans', sans-serif; color: #374151; transition: all 0.15s; }
.toggle-btn:hover { background: #f3f4f6; }
.toggle-active { background: #1d4ed8 !important; color: #fff !important; }
.summary-modal-body { max-height: 60vh; overflow-y: auto; }
.summary-loading { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 40px; color: #9ca3af; }
.summary-empty { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 40px; color: #9ca3af; }

/* Totals row */
.summary-totals-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 20px; }
.summary-total-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 14px 16px; text-align: center; }
.summary-total-card.earn { background: #f0fdf4; border-color: #bbf7d0; }
.summary-total-card.deduct { background: #fff7ed; border-color: #fed7aa; }
.summary-total-card.net-positive { background: #eff6ff; border-color: #bfdbfe; }
.summary-total-card.net-negative { background: #fef2f2; border-color: #fecaca; }
.summary-total-card.tx { background: #f5f3ff; border-color: #ddd6fe; }
.stc-value { font-size: 20px; font-weight: 800; font-family: 'IBM Plex Mono', monospace; color: #111827; }
.earn .stc-value { color: #15803d; }
.deduct .stc-value { color: #c2410c; }
.net-positive .stc-value { color: #1d4ed8; }
.net-negative .stc-value { color: #dc2626; }
.tx .stc-value { color: #7c3aed; }
.stc-label { font-size: 11px; color: #9ca3af; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-top: 2px; }

/* Period list */
.summary-period-list { display: flex; flex-direction: column; gap: 10px; }
.summary-period-row { display: grid; grid-template-columns: 160px 1fr 120px; gap: 16px; align-items: center; background: #fff; border: 1px solid #f3f4f6; border-radius: 8px; padding: 12px 16px; transition: background 0.15s; }
.summary-period-row:hover { background: #fafbff; }
.period-label { font-weight: 700; font-size: 13px; color: #111827; }
.period-dates { font-size: 11px; color: #9ca3af; font-family: 'IBM Plex Mono', monospace; margin-top: 2px; }
.period-bars { display: flex; flex-direction: column; gap: 5px; }
.bar-row { display: flex; align-items: center; gap: 8px; }
.bar-label { font-size: 11px; font-weight: 700; font-family: 'IBM Plex Mono', monospace; width: 80px; text-align: right; }
.earn-label { color: #15803d; }
.deduct-label { color: #c2410c; }
.bar-track { flex: 1; height: 7px; background: #f3f4f6; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 0.4s ease; min-width: 2px; }
.bar-earn { background: linear-gradient(90deg, #22c55e, #86efac); }
.bar-deduct { background: linear-gradient(90deg, #f97316, #fcd34d); }
.period-net { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.net-badge { font-size: 13px; font-weight: 800; font-family: 'IBM Plex Mono', monospace; padding: 3px 8px; border-radius: 6px; }
.net-pos { background: #dcfce7; color: #15803d; }
.net-neg { background: #fee2e2; color: #dc2626; }
.period-tx { font-size: 11px; color: #9ca3af; }

/* Customer detail table */
.customer-period-block { margin-bottom: 20px; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden; }
.customer-period-header { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px; background: #f9fafb; border-bottom: 1px solid #e5e7eb; }
.customer-period-title { font-weight: 700; font-size: 13px; color: #111827; }
.customer-period-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; font-family: 'IBM Plex Mono', monospace; }
.meta-earn { color: #15803d; font-weight: 700; }
.meta-deduct { color: #c2410c; font-weight: 700; }
.meta-sep { color: #d1d5db; }
.meta-tx { color: #9ca3af; background: #f3f4f6; padding: 1px 6px; border-radius: 4px; }
.customer-detail-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.customer-detail-table thead tr { background: #f9fafb; }
.customer-detail-table th { padding: 8px 12px; font-size: 10px; font-weight: 700; text-transform: uppercase; color: #6b7280; letter-spacing: 0.5px; text-align: left; border-bottom: 1px solid #e5e7eb; }
.customer-detail-table tbody tr { border-bottom: 1px solid #f3f4f6; transition: background 0.1s; }
.customer-detail-table tbody tr:hover { background: #fafbff; }
.customer-detail-table tbody tr:last-child { border-bottom: none; }
.customer-detail-table td { padding: 8px 12px; }
.cdt-name { font-weight: 600; color: #111827; }
.cdt-email { font-size: 11px; color: #9ca3af; }
.cdt-empty { padding: 16px; text-align: center; color: #9ca3af; font-size: 12px; font-style: italic; }

@media (max-width: 768px) {
  .summary-totals-row { grid-template-columns: 1fr 1fr; }
  .summary-period-row { grid-template-columns: 1fr; }
}
</style>