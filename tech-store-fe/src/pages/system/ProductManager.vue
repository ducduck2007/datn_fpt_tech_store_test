<template>
  <div class="pm-page">

    <!-- ── Header ─────────────────────────────────────────────────── -->
    <div class="pm-header">
      <div class="pm-header__left">
        <div class="pm-header__eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <rect x="2" y="3" width="6" height="6" rx="1"/><rect x="9" y="3" width="6" height="6" rx="1"/><rect x="16" y="3" width="6" height="6" rx="1"/>
            <rect x="2" y="11" width="6" height="6" rx="1"/><rect x="9" y="11" width="6" height="6" rx="1"/><rect x="16" y="11" width="6" height="6" rx="1"/>
          </svg>
          Quản trị
        </div>
        <h1 class="pm-header__title">Quản lý sản phẩm</h1>
        <p class="pm-header__sub">Hỗ trợ: Biến thể · Tồn kho · Đa danh mục · Sắp xếp · Thùng rác · Thẻ</p>
      </div>
      <div class="pm-header__actions">
        <div class="pm-view-toggle">
          <button class="pm-view-btn" :class="{ active: viewMode === 'active' }" @click="viewMode = 'active'; load()">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/>
            </svg>
            Hoạt động
          </button>
          <button class="pm-view-btn" :class="{ active: viewMode === 'trash' }" @click="viewMode = 'trash'; load()">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
              <path d="M10 11v6M14 11v6"/>
            </svg>
            Thùng rác
          </button>
          
        </div>

        <button class="pm-btn pm-btn--outline" @click="load" :disabled="loading">
          <svg :class="{ 'pm-spin': loading }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M23 4v6h-6M1 20v-6h6"/>
            <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
          </svg>
          {{ loading ? 'Đang tải…' : 'Tải lại' }}
        </button>
        <button v-if="viewMode === 'active'" class="pm-btn pm-btn--outline" @click="showImport = true">
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
    <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
    <polyline points="14 2 14 8 20 8"/>
    <line x1="12" y1="18" x2="12" y2="12"/>
    <line x1="9" y1="15" x2="15" y2="15"/>
  </svg>
  Import Excel
</button>
        <button v-if="viewMode === 'active'" class="pm-btn pm-btn--primary" @click="openCreateDialog">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Thêm sản phẩm
        </button>
      </div>
    </div>

    <!-- ── Filter Panel ────────────────────────────────────────────── -->
    <div v-if="viewMode === 'active'" class="pm-filter-panel">
      <div class="pm-filter-header">
        <div class="pm-filter-title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
          </svg>
          Bộ lọc
          <span v-if="activeFilterCount > 0" class="pm-filter-badge">{{ activeFilterCount }} đang áp dụng</span>
        </div>
        <button v-if="activeFilterCount > 0" class="pm-filter-clear" @click="clearFilters">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
          Xoá tất cả
        </button>
      </div>

      <div class="pm-filter-body">
        <!-- Row 1: Tìm kiếm / Danh mục / Thẻ / Sắp xếp -->
        <div class="pm-filter-row">
          <div class="pm-ff pm-ff--lg">
            <span class="pm-fl">Tìm kiếm</span>
            <div class="pm-search-wrap">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" class="pm-search-icon">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              <input v-model="keyword" class="pm-search-input" placeholder="Tên sản phẩm / SKU…" @keyup.enter="onFilter" />
              <button v-if="keyword" class="pm-search-clear" @click="keyword = ''; onFilter()">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="pm-ff">
            <span class="pm-fl">Danh mục</span>
            <select v-model="categoryIds" multiple class="pm-select pm-select--multi" @change="onFilter">
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>

          <div class="pm-ff">
            <span class="pm-fl">Thẻ chiến dịch</span>
            <select v-model="filterTagId" class="pm-select" @change="onFilter">
              <option :value="null">Tất cả thẻ</option>
              <option v-for="t in tags" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </div>

          <div class="pm-ff">
            <span class="pm-fl">Sắp xếp</span>
            <select v-model="sortBy" class="pm-select" @change="onFilter">
              <option value="recently_updated">Mới cập nhật</option>
              <option value="newest_arrival">Ngày nhập mới nhất</option>
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
              <option value="best_selling">Bán chạy nhất</option>
              <option value="price_asc">Giá: Thấp → Cao</option>
              <option value="price_desc">Giá: Cao → Thấp</option>
              <option value="name_asc">Tên: A → Z</option>
              <option value="name_desc">Tên: Z → A</option>
            </select>
          </div>
        </div>

        <!-- Row 2: Ngày nhập / Sản phẩm mới / Chất lượng / Checkbox + Lọc -->
        <div class="pm-filter-row pm-filter-row--sep">
          <div class="pm-ff pm-ff--lg">
            <span class="pm-fl">Ngày nhập kho</span>
            <div class="pm-date-range">
              <input type="date" v-model="dateRange[0]" class="pm-date-input" @change="onFilter" />
              <span class="pm-date-sep">→</span>
              <input type="date" v-model="dateRange[1]" class="pm-date-input" @change="onFilter" />
            </div>
          </div>

          <div class="pm-ff">
            <span class="pm-fl">Sản phẩm mới</span>
            <select v-model="filterIsNew" class="pm-select" @change="onFilter">
              <option :value="null">Tất cả</option>
              <option :value="true">Chỉ hàng mới</option>
              <option :value="false">Hàng thường</option>
            </select>
          </div>

          <div class="pm-ff">
            <span class="pm-fl">Chất lượng</span>
            <select v-model="filterIsFaulty" class="pm-select" @change="onFilter">
              <option :value="null">Tất cả</option>
              <option :value="true">Bị lỗi (ẩn)</option>
              <option :value="false">Chất lượng tốt</option>
            </select>
          </div>

          <div class="pm-ff pm-ff--actions">
            <label class="pm-checkbox-label">
              <input type="checkbox" v-model="inStockOnly" class="pm-checkbox" @change="onFilter" />
              Chỉ còn hàng
            </label>
            <button class="pm-btn pm-btn--primary pm-btn--sm pm-btn--apply" @click="onFilter">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              Lọc
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Batch action bar ────────────────────────────────────────── -->
    <Transition name="pm-slide-down">
      <div v-if="selectedIds.length > 0 && viewMode === 'active'" class="pm-batch-bar">
        <div class="pm-batch-bar__left">
          <div class="pm-batch-dot"></div>
          <span class="pm-batch-count">Đã chọn <strong>{{ selectedIds.length }}</strong> sản phẩm</span>
        </div>
        <div class="pm-batch-bar__actions">
          <button class="pm-btn pm-btn--outline pm-btn--sm" @click="openBatchUpdateDialog">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
            Sửa hàng loạt
          </button>
          <button class="pm-btn pm-btn--danger pm-btn--sm" :disabled="isBatchDeleting" @click="confirmBatchDelete">
            <span class="pm-spinner pm-spinner--white" v-if="isBatchDeleting"/>
            <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
            </svg>
            Xóa hàng loạt
          </button>
        </div>
      </div>
    </Transition>

    <!-- ── Table Card ──────────────────────────────────────────────── -->
    <div class="pm-card">
      <div class="pm-toolbar">
        <div class="pm-toolbar__info">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="3" width="6" height="6" rx="1"/><rect x="9" y="3" width="6" height="6" rx="1"/>
            <rect x="16" y="3" width="6" height="6" rx="1"/><rect x="2" y="11" width="6" height="6" rx="1"/>
          </svg>
          {{ viewMode === 'active' ? 'Sản phẩm hoạt động' : 'Thùng rác' }}
        </div>
        <span class="pm-count">
          <span class="pm-count__num">{{ totalElements }}</span>
          <span class="pm-count__label">sản phẩm</span>
        </span>
      </div>

      <div class="pm-table-wrap" :class="{ 'pm-table-wrap--loading': loading }">
        <div class="pm-loader-overlay" v-if="loading">
          <div class="pm-loader-ring"/>
        </div>

        <table class="pm-table">
          <thead>
            <tr>
              <th v-if="viewMode === 'active'" style="width:44px; text-align:center">
                <input type="checkbox" class="pm-checkbox" :checked="selectedIds.length === rows.length && rows.length > 0" @change="toggleSelectAll" />
              </th>
              <th style="width:60px; text-align:center">ID</th>
              <th style="width:88px; text-align:center">Ảnh</th>
              <th style="min-width:260px">Thông tin sản phẩm</th>
              <th style="min-width:200px">Mô tả</th>
              <th style="width:130px; text-align:center">Ngày nhập</th>
              <th style="width:140px; text-align:center">Trạng thái</th>
              <th style="width:200px; text-align:center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && rows.length === 0">
              <td :colspan="viewMode === 'active' ? 8 : 7" class="pm-empty">
                <div class="pm-empty__inner">
                  <div class="pm-empty__icon">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4">
                      <rect x="2" y="3" width="6" height="6" rx="1"/><rect x="9" y="3" width="6" height="6" rx="1"/>
                      <rect x="16" y="3" width="6" height="6" rx="1"/><rect x="2" y="11" width="6" height="6" rx="1"/>
                    </svg>
                  </div>
                  <p>{{ viewMode === 'trash' ? 'Thùng rác trống' : 'Chưa có sản phẩm nào' }}</p>
                </div>
              </td>
            </tr>

            <tr v-for="row in rows" :key="row.id" class="pm-row" :class="{ 'pm-row--selected': selectedIds.includes(row.id), 'pm-row--faulty': row.isFaulty }">
              <td v-if="viewMode === 'active'" style="text-align:center">
                <input type="checkbox" class="pm-checkbox" :checked="selectedIds.includes(row.id)" @change="toggleRowSelect(row.id)" />
              </td>
              <td style="text-align:center">
                <span class="pm-id">#{{ row.id }}</span>
              </td>
              <td style="text-align:center">
                <div class="pm-img-wrap">
                  <img v-if="row.imageUrl" :src="row.imageUrl" class="pm-img" :alt="row.name" />
                  <div v-else class="pm-img-empty">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
                      <polyline points="21 15 16 10 5 21"/>
                    </svg>
                  </div>
                </div>
              </td>
              <td>
                <div class="pm-product-info">
                  <div class="pm-product-name-row">
                    <span class="pm-product-name">{{ row.name }}</span>
                    <span v-if="row.isNew" class="pm-badge pm-badge--green">MỚI</span>
                    <span v-if="row.isFaulty" class="pm-badge pm-badge--red">LỖI</span>
                  </div>
                  <div class="pm-product-sku">
                    <span class="pm-sku-label">SKU</span>
                    <span class="pm-sku-val">{{ row.sku }}</span>
                  </div>
                  <div v-if="row.tags?.length" class="pm-tag-row">
                    <span v-for="(tagName, idx) in row.tags" :key="idx" class="pm-tag pm-tag--orange">{{ tagName }}</span>
                  </div>
                  <div class="pm-product-meta">
                    <span v-if="row.minPrice" class="pm-price">{{ formatCurrency(row.minPrice) }}</span>
                    <span v-if="viewMode === 'active'" class="pm-stock-tag" :class="row.inStock ? 'pm-stock-tag--in' : 'pm-stock-tag--out'">
                      <span class="pm-tag__dot"></span>
                      {{ row.inStock ? `Còn hàng (${row.totalStock || 0})` : 'Hết hàng' }}
                    </span>
                  </div>
                </div>
              </td>
              <td>
                <span class="pm-desc">{{ row.description || '—' }}</span>
              </td>
              <td style="text-align:center">
                <span class="pm-date">{{ formatDate(row.createdAt) }}</span>
              </td>
              <td style="text-align:center">
                <div v-if="viewMode === 'active'" class="pm-switch-wrap">
                  <label class="pm-switch">
                    <input type="checkbox" :checked="row.isVisible" @change="toggleProductStatus(row)" />
                    <span class="pm-switch__track">
                      <span class="pm-switch__thumb"></span>
                    </span>
                    <span class="pm-switch__label">{{ row.isVisible ? 'ON' : 'OFF' }}</span>
                  </label>
                </div>
                <span v-else class="pm-tag pm-tag--gray">
                  <span class="pm-tag__dot"></span>
                  Thùng rác
                </span>
              </td>
              <td style="text-align:center">
                <div v-if="viewMode === 'active'" class="pm-dropdown-wrap">
                  <button class="pm-action-btn pm-action-btn--variant" @click="openVariantDrawer(row)">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/>
                      <path d="M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z"/>
                    </svg>
                    Biến thể
                  </button>
                  <div class="pm-action-group">
                    <button class="pm-action-btn pm-action-btn--edit" @click="onEdit(row)">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                      </svg>
                      Sửa
                    </button>
                    <button class="pm-action-btn pm-action-btn--delete" @click="openDeleteConfirm(row)">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                      </svg>
                      {{ row.isFaulty ? 'Xóa hẳn' : 'Xóa' }}
                    </button>
                  </div>
                </div>
                <div v-else>
                  <button class="pm-btn pm-btn--restore pm-btn--sm" @click="onRestore(row.id)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <path d="M1 4v6h6M23 20v-6h-6"/>
                      <path d="M20.49 9A9 9 0 005.64 5.64L1 10M23 14l-4.64 4.36A9 9 0 013.51 15"/>
                    </svg>
                    Khôi phục
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pm-pagination" v-if="totalElements > 0">
        <span class="pm-page-info">{{ page * 20 + 1 }}–{{ Math.min((page + 1) * 20, totalElements) }} / {{ totalElements }}</span>
        <div class="pm-page-btns">
          <button class="pm-page-btn" :disabled="page === 0" @click="onPageChange(page)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
          </button>
          <button v-for="p in pageNumbers" :key="p" class="pm-page-btn" :class="{ active: p - 1 === page }" @click="onPageChange(p)">{{ p }}</button>
          <button class="pm-page-btn" :disabled="(page + 1) * 20 >= totalElements" @click="onPageChange(page + 2)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- ══ DIALOG: XÁC NHẬN XÓA ══ -->
    <Teleport to="body">
      <Transition name="pm-dialog-fade">
        <div v-if="deleteDlg.open" class="pm-overlay" @click.self="deleteDlg.open = false">
          <div class="pm-dialog pm-dialog--sm">
            <div class="pm-dialog__band pm-dialog__band--red">
              <div class="pm-band-circles"><span class="pm-band-c pm-band-c--1"/><span class="pm-band-c pm-band-c--2"/><span class="pm-band-c pm-band-c--3"/></div>
              <div class="pm-dialog__icon-ring">
                <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                  <path d="M10 11v6M14 11v6"/>
                </svg>
              </div>
            </div>
            <div class="pm-dialog__body">
              <span class="pm-dialog__badge pm-dialog__badge--red">{{ deleteDlg.isFaulty ? 'Xóa vĩnh viễn' : 'Chuyển thùng rác' }}</span>
              <h2 class="pm-dialog__title">{{ deleteDlg.isFaulty ? 'Xóa vĩnh viễn?' : 'Xóa sản phẩm?' }}</h2>
              <p class="pm-dialog__desc">
                Sản phẩm <strong class="pm-dialog__hi--red">{{ deleteDlg.name }}</strong>
                {{ deleteDlg.isFaulty ? 'sẽ bị xóa vĩnh viễn khỏi hệ thống.' : 'sẽ được chuyển vào thùng rác.' }}
              </p>
              <div class="pm-dialog__notice pm-dialog__notice--red">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                {{ deleteDlg.isFaulty ? 'Không thể hoàn tác sau khi xác nhận.' : 'Có thể khôi phục từ thùng rác.' }}
              </div>
              <div class="pm-dialog__actions">
                <button class="pm-btn pm-btn--ghost" @click="deleteDlg.open = false">Hủy bỏ</button>
                <button class="pm-btn pm-btn--danger" @click="confirmDelete">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/></svg>
                  Xác nhận
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ══ DIALOG: XÁC NHẬN XÓA HÀNG LOẠT ══ -->
    <Teleport to="body">
      <Transition name="pm-dialog-fade">
        <div v-if="batchDeleteDlg" class="pm-overlay" @click.self="batchDeleteDlg = false">
          <div class="pm-dialog pm-dialog--sm">
            <div class="pm-dialog__band pm-dialog__band--red">
              <div class="pm-band-circles"><span class="pm-band-c pm-band-c--1"/><span class="pm-band-c pm-band-c--2"/><span class="pm-band-c pm-band-c--3"/></div>
              <div class="pm-dialog__icon-ring">
                <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                </svg>
              </div>
            </div>
            <div class="pm-dialog__body">
              <span class="pm-dialog__badge pm-dialog__badge--red">Xóa hàng loạt</span>
              <h2 class="pm-dialog__title">Xóa {{ selectedIds.length }} sản phẩm?</h2>
              <p class="pm-dialog__desc">Tất cả sản phẩm đã chọn sẽ được chuyển vào thùng rác.</p>
              <div class="pm-dialog__notice pm-dialog__notice--red">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                Có thể khôi phục từng sản phẩm từ thùng rác.
              </div>
              <div class="pm-dialog__actions">
                <button class="pm-btn pm-btn--ghost" @click="batchDeleteDlg = false">Hủy bỏ</button>
                <button class="pm-btn pm-btn--danger" :class="{ 'pm-btn--loading': isBatchDeleting }" @click="handleBatchDelete">
                  <span class="pm-spinner pm-spinner--white" v-if="isBatchDeleting"/>
                  <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/></svg>
                  Xác nhận
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ══ DIALOG: BATCH UPDATE ══ -->
    <Teleport to="body">
      <Transition name="pm-dialog-fade">
        <div v-if="batchDlg.open" class="pm-overlay" @click.self="batchDlg.open = false">
          <div class="pm-dialog">
            <div class="pm-dialog__band pm-dialog__band--blue">
              <div class="pm-band-circles"><span class="pm-band-c pm-band-c--1"/><span class="pm-band-c pm-band-c--2"/><span class="pm-band-c pm-band-c--3"/></div>
              <div class="pm-dialog__icon-ring">
                <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </div>
            </div>
            <div class="pm-dialog__body">
              <span class="pm-dialog__badge pm-dialog__badge--blue">Cập nhật hàng loạt</span>
              <h2 class="pm-dialog__title">Cập nhật {{ selectedIds.length }} sản phẩm</h2>
              <div class="pm-dialog__notice pm-dialog__notice--blue">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                Chỉ những trường có giá trị mới được áp dụng.
              </div>
              <div class="pm-form">
                <div class="pm-field">
                  <label class="pm-field__label">Trạng thái hiển thị</label>
                  <select v-model="batchDlg.form.isVisible" class="pm-select">
                    <option :value="null">Không thay đổi</option>
                    <option :value="true">Bật hiển thị (ON)</option>
                    <option :value="false">Tắt hiển thị (OFF)</option>
                  </select>
                </div>
                <div class="pm-field">
                  <label class="pm-field__label">Nhãn hàng mới</label>
                  <select v-model="batchDlg.form.isNew" class="pm-select">
                    <option :value="null">Không thay đổi</option>
                    <option :value="true">Đánh dấu Hàng Mới</option>
                    <option :value="false">Gỡ nhãn Mới</option>
                  </select>
                </div>
                <div class="pm-field">
                  <label class="pm-field__label">Trạng thái lỗi</label>
                  <select v-model="batchDlg.form.isFaulty" class="pm-select">
                    <option :value="null">Không thay đổi</option>
                    <option :value="true">Đánh dấu Lỗi</option>
                    <option :value="false">Hàng Tốt</option>
                  </select>
                </div>
                <div class="pm-field">
                  <label class="pm-field__label">Gắn Tags</label>
                  <select v-model="batchDlg.form.tagIds" multiple class="pm-select pm-select--multi">
                    <option v-for="t in tags" :key="t.id" :value="t.id">{{ t.name }}</option>
                  </select>
                </div>
              </div>
              <div class="pm-dialog__actions">
                <button class="pm-btn pm-btn--ghost" @click="batchDlg.open = false">Hủy bỏ</button>
                <button class="pm-btn pm-btn--primary" :class="{ 'pm-btn--loading': batchDlg.loading }" @click="submitBatchUpdate">
                  <span class="pm-spinner pm-spinner--white" v-if="batchDlg.loading"/>
                  <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  Áp dụng thay đổi
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ══ DRAWER: BIẾN THỂ ══ -->
    <Teleport to="body">
      <Transition name="pm-drawer-slide">
        <div v-if="vr.open" class="pm-drawer-backdrop" @click.self="vr.open = false">
          <div class="pm-drawer">
            <div class="pm-drawer__header">
              <div>
                <div class="pm-drawer__eyebrow">Quản lý biến thể</div>
                <h2 class="pm-drawer__title">{{ vr.productName }}</h2>
              </div>
              <button class="pm-drawer__close" @click="vr.open = false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="pm-drawer__body">
              <div class="pm-drawer-section">
                <div class="pm-drawer-section__title">Các biến thể hiện có</div>
                <div class="pm-table-wrap" :class="{ 'pm-table-wrap--loading': vr.loading }">
                  <div class="pm-loader-overlay" v-if="vr.loading"><div class="pm-loader-ring"/></div>
                  <table class="pm-table">
                    <thead>
                      <tr>
                        <th>Tên biến thể</th>
                        <th style="width:110px">SKU</th>
                        <th style="width:130px; text-align:right">Giá / Tồn</th>
                        <th style="width:160px; text-align:center">Hành động</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-if="!vr.loading && vr.variants.length === 0">
                        <td colspan="4" class="pm-empty"><div class="pm-empty__inner"><p>Chưa có biến thể</p></div></td>
                      </tr>
                      <tr v-for="v in vr.variants" :key="v.id" class="pm-row">
                        <td><span class="pm-product-name" style="font-size:13px">{{ v.variantName }}</span></td>
                        <td><span class="pm-sku-val" style="font-size:12px">{{ v.sku }}</span></td>
                        <td style="text-align:right">
                          <div class="pm-price" style="font-size:13px">{{ formatCurrency(v.price) }}</div>
                          <div style="font-size:11px; color:#6b7280; margin-top:2px">
                            Tồn: <span :style="v.stockQuantity > 0 ? 'color:#16a34a;font-weight:700' : 'color:#dc2626;font-weight:700'">{{ v.stockQuantity }}</span>
                          </div>
                        </td>
                        <td style="text-align:center">
                          <div class="pm-action-group">
                            <button class="pm-action-btn pm-action-btn--variant" @click="openSerialDialog(v)" style="font-size:11px; padding:5px 9px">Số Seri</button>
                            <button class="pm-action-btn pm-action-btn--edit" @click="editVariant(v)">
                              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                              Sửa
                            </button>
                            <button class="pm-action-btn pm-action-btn--delete" @click="deleteVariant(v.id)">
                              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/></svg>
                              Xóa
                            </button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <div class="pm-drawer-section pm-drawer-section--form">
                <div class="pm-drawer-section__title" :class="vr.isEdit ? 'pm-drawer-section__title--edit' : 'pm-drawer-section__title--add'">
                  {{ vr.isEdit ? 'Cập nhật biến thể' : 'Thêm biến thể mới' }}
                </div>
                <div class="pm-form pm-form--grid">
                  <div class="pm-field">
                    <label class="pm-field__label">Tên biến thể <span class="pm-required">*</span></label>
                    <input v-model="vr.form.variantName" class="pm-input" placeholder="vd: Đỏ, 16GB…" />
                  </div>
                  <div class="pm-field">
                    <label class="pm-field__label">SKU <span class="pm-required">*</span></label>
                    <input v-model="vr.form.sku" class="pm-input" />
                  </div>
                  <div class="pm-field">
                    <label class="pm-field__label">Giá (VND) <span class="pm-required">*</span></label>
                    <input type="number" v-model="vr.form.price" class="pm-input" min="0" />
                  </div>
                  <div class="pm-field">
                    <label class="pm-field__label">Tồn kho <span class="pm-field__hint-inline">(tự động bởi Seri)</span></label>
                    <input type="number" v-model="vr.form.stockQuantity" class="pm-input" disabled />
                  </div>
                </div>
                <div class="pm-attr-section">
                  <div class="pm-attr-header">
                    <span class="pm-field__label">Dynamic Attributes</span>
                    <button class="pm-btn pm-btn--outline pm-btn--xs" @click="addVariantAttr">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                      Thêm
                    </button>
                  </div>
                  <div v-for="(attr, i) in vr.attrsList" :key="i" class="pm-attr-row">
                    <input v-model="attr.key" class="pm-input" placeholder="Key (e.g. Color)" />
                    <input v-model="attr.value" class="pm-input" placeholder="Value" />
                    <button class="pm-icon-btn pm-icon-btn--red" @click="removeVariantAttr(i)">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    </button>
                  </div>
                </div>
                <div class="pm-form-footer">
                  <button v-if="vr.isEdit" class="pm-btn pm-btn--ghost pm-btn--sm" @click="resetVariantForm">Hủy</button>
                  <button class="pm-btn pm-btn--primary pm-btn--sm" :class="{ 'pm-btn--loading': vr.saving }" @click="saveVariant">
                    <span class="pm-spinner pm-spinner--white" v-if="vr.saving"/>
                    <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                    {{ vr.isEdit ? 'Lưu thay đổi' : 'Thêm biến thể' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ══ DIALOG: SERIAL NUMBERS ══ -->
    <Teleport to="body">
      <Transition name="pm-dialog-fade">
        <div v-if="serialDlg.open" class="pm-overlay" @click.self="serialDlg.open = false">
          <div class="pm-dialog pm-dialog--lg">
            <div class="pm-dialog__band pm-dialog__band--blue">
              <div class="pm-band-circles"><span class="pm-band-c pm-band-c--1"/><span class="pm-band-c pm-band-c--2"/><span class="pm-band-c pm-band-c--3"/></div>
              <div class="pm-dialog__icon-ring">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/>
                </svg>
              </div>
            </div>
            <div class="pm-dialog__body">
              <span class="pm-dialog__badge pm-dialog__badge--blue">Quản lý Seri</span>
              <h2 class="pm-dialog__title">{{ serialDlg.variantName }}</h2>
              <div class="pm-serial-controls">
                <div class="pm-field" style="flex:0 0 160px">
                  <label class="pm-field__label">Số lượng nhập kho</label>
                  <input type="number" v-model="serialDlg.genQuantity" class="pm-input" min="1" max="500" />
                </div>
                <div style="padding-top:24px">
                  <button class="pm-btn pm-btn--primary pm-btn--sm" :disabled="serialDlg.adding" @click="generateSerials(serialDlg.genQuantity)">
                    <span class="pm-spinner pm-spinner--white" v-if="serialDlg.adding"/>
                    <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 3l14 9-14 9V3z"/></svg>
                    Gen Serial tự động
                  </button>
                </div>
              </div>
              <div class="pm-serial-label">Danh sách máy trong kho</div>
              <div class="pm-table-wrap pm-table-wrap--serial" :class="{ 'pm-table-wrap--loading': serialDlg.loading }">
                <div class="pm-loader-overlay" v-if="serialDlg.loading"><div class="pm-loader-ring"/></div>
                <table class="pm-table">
                  <thead>
                    <tr>
                      <th style="width:50px; text-align:center">#</th>
                      <th>Số Seri / IMEI</th>
                      <th style="width:120px; text-align:center">Trạng thái</th>
                      <th style="width:90px; text-align:center">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="!serialDlg.loading && serialDlg.list.length === 0">
                      <td colspan="4" class="pm-empty"><div class="pm-empty__inner"><p>Chưa có seri nào</p></div></td>
                    </tr>
                    <tr v-for="(s, idx) in serialDlg.list" :key="s.id" class="pm-row">
                      <td style="text-align:center"><span class="pm-muted-num">{{ idx + 1 }}</span></td>
                      <td><span class="pm-serial-num">{{ s.serialNumber }}</span></td>
                      <td style="text-align:center">
                        <span class="pm-tag" :class="s.status === 'IN_STOCK' ? 'pm-tag--green' : s.status === 'SOLD' ? 'pm-tag--gray' : 'pm-tag--red'">
                          <span class="pm-tag__dot"></span>
                          {{ s.status === 'IN_STOCK' ? 'Trong kho' : s.status }}
                        </span>
                      </td>
                      <td style="text-align:center">
                        <button class="pm-action-btn pm-action-btn--delete" :disabled="s.status !== 'IN_STOCK'" @click="deleteSerial(s.id)">Xóa</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="pm-dialog__actions">
                <button class="pm-btn pm-btn--ghost" @click="serialDlg.open = false">Đóng</button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ══ DIALOG: THÊM / SỬA SẢN PHẨM ══ -->
    <Teleport to="body">
      <Transition name="pm-dialog-fade">
        <div v-if="dlg.open" class="pm-overlay" @click.self="dlg.open = false">
          <div class="pm-dialog pm-dialog--xl">
            <div class="pm-dialog__band pm-dialog__band--blue">
              <div class="pm-band-circles"><span class="pm-band-c pm-band-c--1"/><span class="pm-band-c pm-band-c--2"/><span class="pm-band-c pm-band-c--3"/></div>
              <div class="pm-dialog__icon-ring">
                <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <rect x="2" y="3" width="6" height="6" rx="1"/><rect x="9" y="3" width="6" height="6" rx="1"/><rect x="16" y="3" width="6" height="6" rx="1"/>
                </svg>
              </div>
            </div>
            <div class="pm-dialog__body">
              <span class="pm-dialog__badge pm-dialog__badge--blue">{{ dlg.isEdit ? 'Cập nhật' : 'Tạo mới' }}</span>
              <h2 class="pm-dialog__title">{{ dlg.isEdit ? 'Thông tin sản phẩm' : 'Thêm sản phẩm mới' }}</h2>
              <div class="pm-alert" v-if="dlg.alert">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                {{ dlg.alert }}
              </div>
              <div class="pm-tab-nav">
                <button class="pm-tab-item" :class="{ active: dlg.activeTab === 'info' }" @click="dlg.activeTab = 'info'">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  Thông tin cơ bản
                </button>
                <button v-if="dlg.isEdit" class="pm-tab-item" :class="{ active: dlg.activeTab === 'history' }" @click="dlg.activeTab = 'history'">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                  Lịch sử thay đổi
                </button>
              </div>

              <div v-if="dlg.activeTab === 'info'" class="pm-tab-content">
                <div class="pm-form pm-form--grid">
                  <div class="pm-field">
                    <label class="pm-field__label">Tên sản phẩm <span class="pm-required">*</span></label>
                    <input v-model="dlg.form.name" class="pm-input" placeholder="Nhập tên sản phẩm" />
                  </div>
                  <div class="pm-field">
                    <label class="pm-field__label">SKU chính <span class="pm-required">*</span></label>
                    <input v-model="dlg.form.sku" class="pm-input" />
                  </div>
                  <div class="pm-field">
                    <label class="pm-field__label">Danh mục</label>
                    <select v-model="dlg.form.categoryIds" multiple class="pm-select pm-select--multi">
                      <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                    </select>
                  </div>
                  <div class="pm-field">
                    <label class="pm-field__label">Thẻ chiến dịch</label>
                    <select v-model="dlg.form.tagIds" multiple class="pm-select pm-select--multi">
                      <option v-for="t in tags" :key="t.id" :value="t.id">{{ t.name }}</option>
                    </select>
                  </div>
                </div>
                <div class="pm-toggle-row">
                  <label class="pm-toggle-item">
                    <span class="pm-toggle-label">Đánh dấu Mới</span>
                    <label class="pm-switch">
                      <input type="checkbox" v-model="dlg.form.isNew" />
                      <span class="pm-switch__track"><span class="pm-switch__thumb"></span></span>
                      <span class="pm-switch__label">{{ dlg.form.isNew ? 'Mới' : 'Thường' }}</span>
                    </label>
                  </label>
                  <label class="pm-toggle-item">
                    <span class="pm-toggle-label">Đánh dấu Lỗi (Ẩn)</span>
                    <label class="pm-switch pm-switch--red">
                      <input type="checkbox" v-model="dlg.form.isFaulty" />
                      <span class="pm-switch__track"><span class="pm-switch__thumb"></span></span>
                      <span class="pm-switch__label">{{ dlg.form.isFaulty ? 'Lỗi' : 'Tốt' }}</span>
                    </label>
                  </label>
                </div>
                <div class="pm-field" style="margin-top:14px">
                  <label class="pm-field__label">Mô tả</label>
                  <textarea v-model="dlg.form.description" class="pm-textarea" rows="3" />
                </div>
                <div class="pm-attr-section" style="margin-top:14px">
                  <div class="pm-attr-header">
                    <span class="pm-field__label">Thông số kỹ thuật</span>
                    <button class="pm-btn pm-btn--outline pm-btn--xs" @click="addAttribute">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                      Thêm
                    </button>
                  </div>
                  <div v-for="(attr, i) in dlg.attributesList" :key="i" class="pm-attr-row">
                    <input v-model="attr.name" class="pm-input" placeholder="Label (e.g. RAM)" />
                    <input v-model="attr.value" class="pm-input" placeholder="Value" />
                    <button class="pm-icon-btn pm-icon-btn--red" @click="removeAttribute(i)">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    </button>
                  </div>
                </div>
                <div v-if="dlg.isEdit && dlg.existingImages.length > 0" style="margin-top:14px">
                  <label class="pm-field__label" style="display:block; margin-bottom:10px">Thư viện ảnh</label>
                  <div class="pm-image-gallery">
                    <div v-for="img in dlg.existingImages" :key="img.id" class="pm-image-item">
                      <img :src="fixImageUrl(img.url)" class="pm-gallery-img" :class="{ 'pm-gallery-img--primary': img.isPrimary }" />
                      <div v-if="img.isPrimary" class="pm-gallery-badge">MAIN</div>
                      <div class="pm-gallery-actions">
                        <button v-if="!img.isPrimary" class="pm-gallery-btn pm-gallery-btn--star" @click="setPrimaryImage(img.id)">
                          <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                        </button>
                        <button class="pm-gallery-btn pm-gallery-btn--del" @click="markImageForDelete(img.id)">
                          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="pm-field" style="margin-top:14px">
                  <label class="pm-field__label">Upload ảnh mới</label>
                  <label class="pm-file-label">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                      <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
                      <polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
                    </svg>
                    {{ dlg.form.galleryImages?.length ? `${dlg.form.galleryImages.length} file đã chọn` : 'Chọn ảnh (nhiều file)…' }}
                    <input type="file" multiple accept="image/*" style="display:none" @change="onPickFiles" />
                  </label>
                </div>
              </div>

              <div v-else-if="dlg.activeTab === 'history'" class="pm-tab-content">
                <div v-if="dlg.historyLoading" class="pm-history-loading">
                  <div class="pm-loader-ring pm-loader-ring--center"/>
                  <span>Đang tải lịch sử…</span>
                </div>
                <div v-else-if="dlg.history.length === 0" class="pm-empty" style="padding: 40px 20px">
                  <div class="pm-empty__inner">
                    <div class="pm-empty__icon">
                      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    </div>
                    <p>Chưa có bản ghi thay đổi nào</p>
                  </div>
                </div>
                <div v-else class="pm-timeline">
                  <div v-for="(log, i) in dlg.history" :key="i" class="pm-timeline-item">
                    <div class="pm-timeline-dot" :class="'pm-timeline-dot--' + getLogType(log.severity)"></div>
                    <div class="pm-timeline-content">
                      <div class="pm-timeline-header">
                        <div class="pm-timeline-user">
                          <div class="pm-avatar">{{ (log.username || 'S').charAt(0).toUpperCase() }}</div>
                          <span class="pm-timeline-username">{{ log.username || 'System' }}</span>
                        </div>
                        <div class="pm-timeline-meta">
                          <span class="pm-tag pm-tag--log" :class="'pm-tag--log-' + getLogType(log.severity)">{{ log.actionType || 'UPDATE' }}</span>
                          <span class="pm-timeline-time">{{ formatDate(log.createdAt) }}</span>
                        </div>
                      </div>
                      <p class="pm-timeline-desc">{{ log.description }}</p>
                      <div class="pm-timeline-ip">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
                        IP: {{ log.ipAddress || 'N/A' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="pm-dialog__actions">
                <button class="pm-btn pm-btn--ghost" @click="dlg.open = false">Hủy bỏ</button>
                <button v-if="dlg.activeTab === 'info'" class="pm-btn pm-btn--primary" :class="{ 'pm-btn--loading': dlg.loading }" @click="submitForm">
                  <span class="pm-spinner pm-spinner--white" v-if="dlg.loading"/>
                  <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  {{ dlg.isEdit ? 'Cập nhật sản phẩm' : 'Tạo sản phẩm' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
<ProductImportDialog
  :open="showImport"
  @close="showImport = false"
  @imported="load()"
/>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from "vue";
import { categoriesApi } from "../../api/categories.api";
import { productsApi } from "../../api/products.api";
import { toast } from "../../ui/toast";
import axios from 'axios';
import http from "../../api/http";
import ProductImportDialog from '../../components/Productimportdialog.vue'

const BASE_URL_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const loading = ref(false);
const categories = ref([]);
const rows = ref([]);
const tags = ref([]);
const filterTagId = ref(null);
const viewMode = ref('active');
const page = ref(0);
const totalElements = ref(0);
const keyword = ref("");
const categoryIds = ref([]);
const sortBy = ref("newest_arrival");
const inStockOnly = ref(false);
const selectedIds = ref([]);
const isBatchDeleting = ref(false);
const dateRange = ref([]);
const filterIsNew = ref(null);
const filterIsFaulty = ref(null);
const showImport = ref(false)
const deleteDlg = reactive({ open: false, row: null, name: '', isFaulty: false });
const batchDeleteDlg = ref(false);

const activeFilterCount = computed(() =>
  [keyword.value, categoryIds.value?.length, filterTagId.value, inStockOnly.value || null, dateRange.value?.length, filterIsNew.value, filterIsFaulty.value]
    .filter(v => v !== null && v !== undefined && v !== '' && v !== false && v !== 0).length
);

const pageNumbers = computed(() => {
  const total = Math.ceil(totalElements.value / 20);
  const p = page.value + 1;
  const range = [];
  for (let i = Math.max(1, p - 2); i <= Math.min(total, p + 2); i++) range.push(i);
  return range;
});

function clearFilters() {
  keyword.value = ''; categoryIds.value = []; filterTagId.value = null;
  sortBy.value = 'newest_arrival'; inStockOnly.value = false;
  dateRange.value = []; filterIsNew.value = null; filterIsFaulty.value = null;
  onFilter();
}
function toggleSelectAll(e) {
  if (e.target.checked) selectedIds.value = rows.value.map(r => r.id);
  else selectedIds.value = [];
}
function toggleRowSelect(id) {
  const i = selectedIds.value.indexOf(id);
  if (i >= 0) selectedIds.value.splice(i, 1);
  else selectedIds.value.push(id);
}
function openDeleteConfirm(row) {
  deleteDlg.row = row; deleteDlg.name = row.name; deleteDlg.isFaulty = row.isFaulty; deleteDlg.open = true;
}
async function confirmDelete() {
  await onDelete(deleteDlg.row);
  deleteDlg.open = false;
}
function confirmBatchDelete() { batchDeleteDlg.value = true; }

function formatDate(dateStr) {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleString("vi-VN", { dateStyle: 'short', timeStyle: 'short' });
}
function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/150?text=No+Image";
  if (url.startsWith("http")) return url;
  return `${BASE_URL_API}${url}`;
}
function normalizeProducts(list) {
  return (list || []).map((p) => ({ ...p, imageUrl: fixImageUrl(p.imageUrl) }));
}
function formatCurrency(val) {
  if (!val) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

async function loadCategories() {
  try {
    const res = await categoriesApi.list(false);
    categories.value = res.data?.data || res.data || [];
  } catch (e) { console.error(e); }
}
async function loadTags() {
  try {
    const res = await http.get('/api/tags');
    tags.value = res.data?.data || res.data || [];
  } catch (e) { console.error("Lỗi lấy tag:", e); }
}

const serialDlg = reactive({
  open: false, variantId: null, variantName: "", list: [], inputText: "", genQuantity: 1, loading: false, adding: false
});

async function load() {
  loading.value = true;
  try {
    let res;
    if (viewMode.value === 'trash') {
      res = await axios.get(`${BASE_URL_API}/api/products/trash`, { params: { page: page.value } });
    } else {
      const params = {
        page: page.value, keyword: keyword.value || undefined, sortBy: sortBy.value || undefined,
        inStockOnly: inStockOnly.value, tagId: filterTagId.value || undefined,
        startDate: dateRange.value?.[0] ? `${dateRange.value[0]}T00:00:00` : undefined,
        endDate: dateRange.value?.[1] ? `${dateRange.value[1]}T23:59:59` : undefined,
        isNew: filterIsNew.value === null ? undefined : filterIsNew.value,
        isFaulty: filterIsFaulty.value === null ? undefined : filterIsFaulty.value
      };
      if (categoryIds.value?.length > 0) params.categoryIds = categoryIds.value.join(',');
      res = await productsApi.list(params);
    }
    const pageData = res.data?.data || res.data;
    rows.value = normalizeProducts(pageData.content || []);
    totalElements.value = pageData.totalElements || 0;
  } catch (e) {
    toast("Không tải được sản phẩm.", "error");
  } finally {
    loading.value = false;
  }
}

function onFilter() { page.value = 0; load(); }
function onPageChange(val) { page.value = val - 1; load(); }

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return;
  isBatchDeleting.value = true;
  try {
    await productsApi.batchDelete(selectedIds.value);
    toast(`Đã chuyển ${selectedIds.value.length} sản phẩm vào thùng rác`, "success");
    selectedIds.value = [];
    batchDeleteDlg.value = false;
    await load();
  } catch (e) {
    toast("Lỗi khi xóa hàng loạt", "error");
  } finally {
    isBatchDeleting.value = false;
  }
}

async function toggleProductStatus(row) {
  try {
    if (row.isVisible) {
      await axios.put(`${BASE_URL_API}/api/products/${row.id}/restore`);
      toast(`Đã bật hiển thị: ${row.name}`, "success");
    } else {
      await axios.delete(`${BASE_URL_API}/api/products/${row.id}`);
      toast(`Đã tắt (ẩn) sản phẩm: ${row.name}`, "warning");
    }
  } catch (e) {
    toast("Lỗi cập nhật trạng thái", "error");
    row.isVisible = !row.isVisible;
  }
}

const batchDlg = reactive({
  open: false, loading: false,
  form: { isVisible: null, isNew: null, isFaulty: null, tagIds: [] }
});
function openBatchUpdateDialog() {
  batchDlg.form = { isVisible: null, isNew: null, isFaulty: null, tagIds: [] };
  batchDlg.open = true;
}
async function submitBatchUpdate() {
  batchDlg.loading = true;
  try {
    const payload = {
      ids: selectedIds.value, isVisible: batchDlg.form.isVisible,
      isNew: batchDlg.form.isNew, isFaulty: batchDlg.form.isFaulty,
      tagIds: batchDlg.form.tagIds.length > 0 ? batchDlg.form.tagIds : null
    };
    await productsApi.batchUpdate(payload);
    toast("Cập nhật hàng loạt thành công!", "success");
    batchDlg.open = false; selectedIds.value = []; await load();
  } catch (e) {
    toast("Lỗi cập nhật hàng loạt", "error");
  } finally {
    batchDlg.loading = false;
  }
}

const vr = reactive({
  open: false, productId: null, productName: "", variants: [], loading: false, saving: false, isEdit: false, editId: null,
  form: { variantName: "", sku: "", price: 0, stockQuantity: 0, isActive: true }, attrsList: [{ key: "", value: "" }]
});

async function openVariantDrawer(row) {
  vr.productId = row.id; vr.productName = row.name; vr.open = true;
  resetVariantForm(); await loadVariants();
}
async function loadVariants() {
  vr.loading = true;
  try {
    const res = await axios.get(`${BASE_URL_API}/api/products/${vr.productId}/variants`);
    vr.variants = res.data || [];
  } catch (e) { toast("Tải biến thể thất bại", "error"); }
  vr.loading = false;
}
function resetVariantForm() {
  vr.isEdit = false; vr.editId = null;
  vr.form = { variantName: "", sku: "", price: 0, stockQuantity: 0, isActive: true };
  vr.attrsList = [{ key: "", value: "" }];
}
function addVariantAttr() { vr.attrsList.push({ key: "", value: "" }); }
function removeVariantAttr(index) { vr.attrsList.splice(index, 1); }
function editVariant(row) {
  vr.isEdit = true; vr.editId = row.id;
  vr.form = { variantName: row.variantName, sku: row.sku, price: row.price, stockQuantity: row.stockQuantity, isActive: row.isActive };
  try {
    const obj = JSON.parse(row.attributesJson || '{}');
    const keys = Object.keys(obj);
    vr.attrsList = keys.length > 0 ? keys.map(k => ({ key: k, value: obj[k] })) : [{ key: "", value: "" }];
  } catch { vr.attrsList = [{ key: "", value: "" }]; }
}
async function saveVariant() {
  if (!vr.form.variantName || !vr.form.sku) return toast("Vui lòng điền các trường bắt buộc", "warning");
  vr.saving = true;
  try {
    const attrObj = {};
    vr.attrsList.forEach(item => { if (item.key && item.value) attrObj[item.key.trim()] = item.value.trim(); });
    const payload = { ...vr.form, attributesJson: Object.keys(attrObj).length > 0 ? JSON.stringify(attrObj) : null };
    if (vr.isEdit) await axios.put(`${BASE_URL_API}/api/products/variants/${vr.editId}`, payload);
    else await axios.post(`${BASE_URL_API}/api/products/${vr.productId}/variants`, payload);
    toast("Thành công", "success");
    resetVariantForm(); await loadVariants(); load();
  } catch { toast("Thất bại", "error"); }
  vr.saving = false;
}
async function deleteVariant(id) {
  try { await axios.delete(`${BASE_URL_API}/api/products/variants/${id}`); toast("Đã xóa", "success"); await loadVariants(); load(); }
  catch { toast("Thất bại", "error"); }
}
async function openSerialDialog(row) {
  serialDlg.variantId = row.id; serialDlg.variantName = row.variantName;
  serialDlg.inputText = ""; serialDlg.open = true; await loadSerials();
}
async function loadSerials() {
  serialDlg.loading = true;
  try {
    const res = await http.get(`/api/products/variants/${serialDlg.variantId}/serials`);
    serialDlg.list = res.data || [];
  } catch (e) { toast("Lỗi tải danh sách seri", "error"); }
  finally { serialDlg.loading = false; }
}
async function deleteSerial(serialId) {
  try {
    await http.delete(`/api/products/variants/serials/${serialId}`);
    toast("Xóa Seri thành công", "success");
    await loadSerials(); await loadVariants(); await load();
  } catch (e) { toast("Xóa thất bại", "error"); }
}
async function generateSerials(quantity = 1) {
  try {
    const res = await http.post(`/api/products/variants/${serialDlg.variantId}/serials/generate`, null, { params: { quantity } });
    toast(`Đã gen ${res.data?.serials?.length || quantity} serial thành công`, "success");
    await loadSerials(); await loadVariants(); await load();
  } catch (e) { toast("Gen serial thất bại", "error"); }
}
async function onRestore(id) {
  try { await axios.put(`${BASE_URL_API}/api/products/${id}/restore`); toast("Khôi phục thành công!", "success"); load(); }
  catch { toast("Khôi phục thất bại", "error"); }
}
async function onDelete(row) {
  try {
    if (row.isFaulty) {
      await axios.delete(`${BASE_URL_API}/api/products/${row.id}/hard`);
      toast("Đã xóa vĩnh viễn sản phẩm lỗi.", "success");
    } else {
      await axios.delete(`${BASE_URL_API}/api/products/${row.id}`);
      toast("Đã chuyển vào thùng rác.", "success");
    }
    load();
  } catch { toast("Xóa thất bại.", "error"); }
}
async function setPrimaryImage(imgId) {
  try {
    await axios.put(`${BASE_URL_API}/api/products/${dlg.editId}/images/${imgId}/primary`);
    toast("Cập nhật ảnh chính thành công", "success");
    const res = await productsApi.get(dlg.editId);
    dlg.existingImages = (res.data?.data || res.data).images || [];
  } catch { toast("Thất bại", "error"); }
}

const dlg = reactive({
  open: false, isEdit: false, loading: false, alert: "", editId: null, attributesList: [], existingImages: [], idsToDelete: [],
  activeTab: 'info', history: [], historyLoading: false,
  form: { name: "", sku: "", description: "", isVisible: true, categoryIds: [], galleryImages: [], tagIds: [], isNew: true, isFaulty: false },
});

function getLogType(severity) {
  if (severity === 'HIGH' || severity === 'CRITICAL') return 'danger';
  if (severity === 'MEDIUM') return 'warning';
  return 'primary';
}
function openCreateDialog() {
  dlg.isEdit = false; dlg.editId = null; dlg.attributesList = [{ name: "", value: "" }];
  dlg.existingImages = []; dlg.idsToDelete = []; dlg.activeTab = 'info';
  dlg.form = { name: "", sku: "", description: "", isVisible: true, categoryIds: [], galleryImages: [], tagIds: [], isNew: true, isFaulty: false };
  dlg.alert = ""; dlg.open = true;
}
async function onEdit(row) {
  dlg.isEdit = true; dlg.editId = row.id; dlg.idsToDelete = []; dlg.existingImages = [];
  dlg.activeTab = 'info'; dlg.open = true; dlg.loading = true; dlg.historyLoading = true;
  try {
    const [detailRes, historyRes] = await Promise.all([
      productsApi.get(row.id),
      productsApi.getHistory(row.id).catch(() => ({ data: [] }))
    ]);
    const data = detailRes.data?.data || detailRes.data;
    dlg.form = {
      name: data.name, sku: data.sku, description: data.description, isVisible: data.isVisible,
      categoryIds: data.categoryId ? [data.categoryId] : [], galleryImages: [], tagIds: [],
      isNew: data.isNew, isFaulty: data.isFaulty
    };
    if (data.tags?.length > 0) {
      dlg.form.tagIds = data.tags.map(name => tags.value.find(t => t.name === name)?.id).filter(id => id);
    }
    dlg.existingImages = data.images || [];
    try {
      const attrs = JSON.parse(data.attributes);
      dlg.attributesList = Array.isArray(attrs) ? attrs : [];
    } catch { dlg.attributesList = []; }
    dlg.history = historyRes.data || [];
  } catch { toast("Tải dữ liệu thất bại", "error"); }
  finally { dlg.loading = false; dlg.historyLoading = false; }
}
function markImageForDelete(imageId) {
  dlg.idsToDelete.push(imageId);
  dlg.existingImages = dlg.existingImages.filter(img => img.id !== imageId);
}
function addAttribute() { dlg.attributesList.push({ name: "", value: "" }); }
function removeAttribute(index) { dlg.attributesList.splice(index, 1); }
function onPickFiles(e) { dlg.form.galleryImages = Array.from(e.target.files); }

async function submitForm() {
  if (!dlg.form.name || !dlg.form.sku) return toast("Vui lòng nhập Tên và SKU", "warning");
  dlg.loading = true;
  try {
    const formData = new FormData();
    formData.append("name", String(dlg.form.name || ""));
    formData.append("sku", String(dlg.form.sku || ""));
    formData.append("description", String(dlg.form.description || ""));
    formData.append("isVisible", String(dlg.form.isVisible));
    formData.append("isNew", String(dlg.form.isNew));
    formData.append("isFaulty", String(dlg.form.isFaulty));
    if (Array.isArray(dlg.form.categoryIds) && dlg.form.categoryIds.length > 0)
      dlg.form.categoryIds.forEach(id => formData.append("categoryIds", id));
    if (Array.isArray(dlg.form.tagIds) && dlg.form.tagIds.length > 0)
      dlg.form.tagIds.forEach(id => formData.append("tagIds", id));
    if (Array.isArray(dlg.idsToDelete) && dlg.idsToDelete.length > 0)
      dlg.idsToDelete.forEach(id => formData.append("idsToDelete", id));
    if (dlg.form.galleryImages && dlg.form.galleryImages.length > 0)
      for (let i = 0; i < dlg.form.galleryImages.length; i++) {
        let file = dlg.form.galleryImages[i];
        if (file instanceof File || file instanceof Blob) formData.append("galleryImages", file);
      }
    const validAttrs = dlg.attributesList.filter(a => a.name?.trim() && a.value?.trim());
    if (validAttrs.length > 0) formData.append("attributes", JSON.stringify(validAttrs));
    if (dlg.isEdit) await productsApi.update(dlg.editId, formData);
    else await productsApi.create(formData);
    dlg.open = false; await load(); toast("Thành công", "success");
  } catch (e) {
    console.error("LỖI GỬI LÊN:", e);
    toast("Lỗi: " + (e.response?.data?.message || e.message), "error");
  } finally { dlg.loading = false; }
}

onMounted(async () => {
  await loadCategories();
  await loadTags();
  await load();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

.pm-page {
  font-family: 'Plus Jakarta Sans', sans-serif;
  background: #f6f7f9;
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: #0f1117;
  box-sizing: border-box;
}
*, *::before, *::after { box-sizing: border-box; }

/* ── Header ── */
.pm-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 20px; margin-bottom: 22px; flex-wrap: wrap;
}
.pm-header__eyebrow {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 11px; font-weight: 700; letter-spacing: 0.08em;
  text-transform: uppercase; color: #6b7280; margin-bottom: 6px;
}
.pm-header__title { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; margin: 0 0 4px; }
.pm-header__sub { font-size: 13px; color: #6b7280; margin: 0; font-weight: 500; }
.pm-header__actions { display: flex; align-items: center; gap: 8px; flex-shrink: 0; padding-top: 4px; flex-wrap: wrap; }

.pm-view-toggle {
  display: flex; background: #f6f7f9; border: 1px solid #e4e7ec;
  border-radius: 8px; padding: 3px; gap: 2px;
}
.pm-view-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; border: none; background: transparent;
  font-family: 'Plus Jakarta Sans', sans-serif; font-size: 12.5px; font-weight: 700;
  color: #6b7280; cursor: pointer; border-radius: 6px; transition: all 0.15s;
}
.pm-view-btn:hover { color: #0f1117; background: #ffffff; }
.pm-view-btn.active { background: #0f1117; color: white; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }

/* ── Buttons ── */
.pm-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px; font-size: 13.5px; font-weight: 700;
  border-radius: 8px; border: none; cursor: pointer;
  transition: all 0.18s ease; font-family: 'Plus Jakarta Sans', sans-serif; white-space: nowrap;
}
.pm-btn--xs { padding: 5px 10px; font-size: 12px; }
.pm-btn--sm { padding: 7px 14px; font-size: 13px; }
.pm-btn--outline { background: #ffffff; border: 1.5px solid #e4e7ec; color: #0f1117; }
.pm-btn--outline:hover:not(:disabled) { border-color: #9ca3af; background: #f9fafb; }
.pm-btn--outline:disabled { opacity: 0.5; cursor: not-allowed; }
.pm-btn--primary { background: linear-gradient(135deg, #1d4ed8, #2563eb); color: #fff; box-shadow: 0 4px 12px rgba(37,99,235,0.3); }
.pm-btn--primary:hover:not(:disabled) { background: linear-gradient(135deg, #1e40af, #1d4ed8); transform: translateY(-1px); box-shadow: 0 6px 18px rgba(37,99,235,0.35); }
.pm-btn--primary:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
.pm-btn--danger { background: linear-gradient(135deg, #b91c1c, #dc2626); color: #fff; box-shadow: 0 4px 12px rgba(220,38,38,0.3); }
.pm-btn--danger:hover { background: linear-gradient(135deg, #991b1b, #b91c1c); transform: translateY(-1px); }
.pm-btn--ghost { background: transparent; border: 1.5px solid #e4e7ec; color: #6b7280; padding: 10px 20px; border-radius: 8px; }
.pm-btn--ghost:hover { background: #f0f2f5; color: #0f1117; }
.pm-btn--restore { background: #f0fdf4; border: 1.5px solid #bbf7d0; color: #16a34a; font-weight: 700; border-radius: 8px; }
.pm-btn--restore:hover { background: #16a34a; color: #fff; border-color: #16a34a; }
.pm-btn--loading { opacity: 0.8; pointer-events: none; transform: none !important; }
@keyframes spin-r { to { transform: rotate(360deg); } }
.pm-spin { animation: spin-r 0.7s linear infinite; }

/* ══════════════════════════════════════════════════════════
   FILTER PANEL – REDESIGN
══════════════════════════════════════════════════════════ */
.pm-filter-panel {
  background: #ffffff;
  border: 1px solid #e4e7ec;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  margin-bottom: 16px;
  overflow: hidden;
}
.pm-filter-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 18px;
  border-bottom: 1px solid #f0f2f5;
  background: #fafbfc;
}
.pm-filter-title {
  display: flex; align-items: center; gap: 7px;
  font-size: 12.5px; font-weight: 700; color: #0f1117;
}
.pm-filter-badge {
  background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe;
  font-size: 10.5px; font-weight: 700; padding: 2px 8px; border-radius: 999px;
}
.pm-filter-clear {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 11.5px; color: #9ca3af; background: none; border: none;
  cursor: pointer; padding: 4px 8px; border-radius: 6px;
  font-family: 'Plus Jakarta Sans', sans-serif; font-weight: 600; transition: all 0.15s;
}
.pm-filter-clear:hover { color: #dc2626; background: #fff1f2; }

.pm-filter-body { padding: 14px 18px; display: flex; flex-direction: column; gap: 10px; }

.pm-filter-row {
  display: grid;
  grid-template-columns: 1.8fr 1fr 1fr 1fr;
  gap: 10px;
  align-items: end;
}
.pm-filter-row--sep {
  padding-top: 10px;
  border-top: 1px dashed #e4e7ec;
}

/* Filter Field */
.pm-ff { display: flex; flex-direction: column; gap: 5px; }
.pm-ff--actions {
  display: flex; flex-direction: row;
  align-items: center; gap: 10px;
  padding-bottom: 1px;
}
.pm-fl {
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: 0.07em;
  color: #9ca3af;
}

/* Search */
.pm-search-wrap { position: relative; display: flex; align-items: center; }
.pm-search-icon { position: absolute; left: 10px; color: #9ca3af; pointer-events: none; flex-shrink: 0; }
.pm-search-input {
  width: 100%; padding: 7px 30px 7px 32px;
  font-size: 13px; font-family: 'Plus Jakarta Sans', sans-serif;
  border: 1.5px solid #e4e7ec; border-radius: 8px;
  background: #f9fafb; color: #0f1117; outline: none; transition: border-color 0.15s;
}
.pm-search-input:focus { border-color: #2563eb; background: #fff; }
.pm-search-clear {
  position: absolute; right: 8px; background: none; border: none;
  cursor: pointer; color: #9ca3af; padding: 2px;
  display: flex; align-items: center;
}
.pm-search-clear:hover { color: #0f1117; }

/* Select */
.pm-select {
  width: 100%; padding: 7px 10px; font-size: 13px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  border: 1.5px solid #e4e7ec; border-radius: 8px;
  background: #f9fafb; color: #0f1117;
  outline: none; cursor: pointer; transition: border-color 0.15s;
}
.pm-select:focus { border-color: #2563eb; }
.pm-select--multi { min-height: 34px; }

/* Date range */
.pm-date-range { display: flex; align-items: center; gap: 6px; }
.pm-date-input {
  flex: 1; padding: 7px 8px; font-size: 12px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  border: 1.5px solid #e4e7ec; border-radius: 8px;
  background: #f9fafb; color: #0f1117;
  outline: none; transition: border-color 0.15s;
}
.pm-date-input:focus { border-color: #2563eb; }
.pm-date-sep { font-size: 12px; color: #9ca3af; font-weight: 700; flex-shrink: 0; }

/* Checkbox */
.pm-checkbox { width: 15px; height: 15px; accent-color: #2563eb; cursor: pointer; flex-shrink: 0; }
.pm-checkbox-label {
  display: flex; align-items: center; gap: 7px;
  font-size: 12.5px; font-weight: 600; color: #0f1117;
  cursor: pointer; white-space: nowrap;
}
.pm-checkbox-custom { display: none; }

/* Apply button */
.pm-btn--apply { flex-shrink: 0; margin-left: auto; }

/* ── Batch bar ── */
.pm-batch-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 20px; background: #eff6ff;
  border: 1px solid #bfdbfe; border-radius: 12px;
  margin-bottom: 14px; gap: 12px; flex-wrap: wrap;
}
.pm-batch-bar__left { display: flex; align-items: center; gap: 10px; }
.pm-batch-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #2563eb; box-shadow: 0 0 6px #2563eb;
  animation: blink 1.2s ease infinite;
}
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0.4} }
.pm-batch-count { font-size: 13.5px; font-weight: 600; color: #2563eb; }
.pm-batch-count strong { font-weight: 800; }
.pm-batch-bar__actions { display: flex; gap: 8px; }

/* ── Card ── */
.pm-card {
  background: #ffffff; border: 1px solid #e4e7ec;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  overflow: hidden;
}
.pm-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 13px 20px; border-bottom: 1px solid #f0f2f5; background: #fafbfc;
}
.pm-toolbar__info {
  display: flex; align-items: center; gap: 7px;
  font-size: 12.5px; font-weight: 700; color: #6b7280;
}
.pm-count { display: flex; align-items: baseline; gap: 4px; }
.pm-count__num { font-size: 16px; font-weight: 800; color: #0f1117; }
.pm-count__label { font-size: 12px; color: #9ca3af; font-weight: 600; }

/* ── Table ── */
.pm-table-wrap { overflow-x: auto; position: relative; min-height: 120px; }
.pm-table-wrap--loading { pointer-events: none; }
.pm-table-wrap--serial { max-height: 360px; overflow-y: auto; }
.pm-loader-overlay {
  position: absolute; inset: 0; background: rgba(255,255,255,0.75);
  backdrop-filter: blur(2px); display: flex; align-items: center; justify-content: center; z-index: 10;
}
.pm-loader-ring {
  width: 36px; height: 36px; border: 3px solid #e4e7ec;
  border-top-color: #2563eb; border-radius: 50%; animation: spin-r 0.7s linear infinite;
}
.pm-loader-ring--center { margin: 0 auto; }
.pm-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.pm-table thead tr { border-bottom: 2px solid #f0f2f5; }
.pm-table th {
  padding: 11px 14px; text-align: left; font-size: 10.5px; font-weight: 700;
  letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af;
  background: #fafbfc; white-space: nowrap;
}
.pm-table td { padding: 12px 14px; vertical-align: middle; border-bottom: 1px solid #f0f2f5; }
.pm-row { transition: background 0.12s; }
.pm-row:hover { background: #fafbfc; }
.pm-row:last-child td { border-bottom: none; }
.pm-row--selected { background: #eff6ff !important; }
.pm-row--faulty { opacity: 0.75; }

.pm-id {
  font-family: 'JetBrains Mono', monospace; font-size: 11.5px; font-weight: 600;
  color: #2563eb; background: #eff6ff; padding: 2px 7px; border-radius: 5px;
}
.pm-img-wrap { display: flex; justify-content: center; }
.pm-img { width: 68px; height: 50px; border-radius: 8px; object-fit: cover; border: 1px solid #f0f2f5; }
.pm-img-empty {
  width: 68px; height: 50px; border-radius: 8px; background: #f0f2f5;
  border: 1px dashed #e4e7ec; display: flex; align-items: center;
  justify-content: center; color: #9ca3af;
}
.pm-product-info { display: flex; flex-direction: column; gap: 4px; }
.pm-product-name-row { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.pm-product-name { font-size: 13.5px; font-weight: 800; color: #2563eb; }
.pm-badge {
  font-size: 10px; font-weight: 800; padding: 2px 7px;
  border-radius: 999px; letter-spacing: 0.05em; border: 1px solid transparent;
}
.pm-badge--green { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }
.pm-badge--red  { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.pm-product-sku { display: flex; align-items: center; gap: 5px; }
.pm-sku-label { font-size: 10px; font-weight: 700; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.06em; }
.pm-sku-val { font-family: 'JetBrains Mono', monospace; font-size: 11.5px; color: #6b7280; font-weight: 500; }
.pm-tag-row { display: flex; gap: 4px; flex-wrap: wrap; }
.pm-product-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-top: 2px; }
.pm-price { font-size: 13px; font-weight: 800; color: #dc2626; font-family: 'JetBrains Mono', monospace; }
.pm-stock-tag {
  display: inline-flex; align-items: center; gap: 4px; font-size: 11px;
  font-weight: 700; padding: 2px 8px; border-radius: 999px; border: 1px solid transparent;
}
.pm-stock-tag--in  { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }
.pm-stock-tag--out { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }

.pm-tag {
  display: inline-flex; align-items: center; gap: 5px; padding: 4px 10px;
  border-radius: 999px; font-size: 11.5px; font-weight: 700;
  white-space: nowrap; border: 1px solid transparent;
}
.pm-tag__dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.pm-tag--green { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }
.pm-tag--green .pm-tag__dot { background: #16a34a; }
.pm-tag--red   { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.pm-tag--red   .pm-tag__dot { background: #dc2626; }
.pm-tag--orange { background: #fffbeb; color: #d97706; border-color: #fde68a; font-size: 10.5px; padding: 2px 7px; }
.pm-tag--gray  { background: #f3f4f6; color: #6b7280; border-color: #e4e7ec; }
.pm-tag--gray  .pm-tag__dot { background: #9ca3af; }

.pm-desc {
  font-size: 12.5px; color: #6b7280; overflow: hidden;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical;
  line-clamp: 3;
  max-width: 220px; line-height: 1.5;
}
.pm-date { font-size: 12px; color: #6b7280; white-space: nowrap; font-weight: 500; }
.pm-muted-num { font-size: 12px; color: #9ca3af; font-family: 'JetBrains Mono', monospace; }
.pm-serial-num { font-family: 'JetBrains Mono', monospace; font-size: 12.5px; font-weight: 500; color: #0f1117; }

/* Switch */
.pm-switch-wrap { display: flex; justify-content: center; }
.pm-switch { display: inline-flex; align-items: center; gap: 8px; cursor: pointer; }
.pm-switch input { display: none; }
.pm-switch__track {
  width: 40px; height: 22px; border-radius: 999px; background: #e5e7eb;
  position: relative; transition: background 0.2s; flex-shrink: 0;
}
.pm-switch input:checked + .pm-switch__track { background: #16a34a; }
.pm-switch--red input:checked + .pm-switch__track { background: #dc2626; }
.pm-switch__thumb {
  position: absolute; top: 3px; left: 3px; width: 16px; height: 16px;
  border-radius: 50%; background: white; transition: transform 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}
.pm-switch input:checked + .pm-switch__track .pm-switch__thumb { transform: translateX(18px); }
.pm-switch__label { font-size: 11.5px; font-weight: 700; color: #6b7280; min-width: 26px; }

/* Action buttons */
.pm-dropdown-wrap { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.pm-action-group { display: flex; gap: 5px; justify-content: center; }
.pm-action-btn {
  display: inline-flex; align-items: center; gap: 4px; padding: 5px 10px;
  font-size: 11.5px; font-weight: 700; border: 1.5px solid transparent;
  border-radius: 8px; cursor: pointer; transition: all 0.18s ease;
  font-family: 'Plus Jakarta Sans', sans-serif; white-space: nowrap;
}
.pm-action-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none !important; }
.pm-action-btn--variant  { background: #f5f3ff; color: #7c3aed; border-color: #ddd6fe; width: 100%; justify-content: center; }
.pm-action-btn--variant:hover  { background: #7c3aed; color: white; border-color: #7c3aed; }
.pm-action-btn--edit     { background: #eff6ff; color: #2563eb; border-color: #bfdbfe; }
.pm-action-btn--edit:hover     { background: #2563eb; color: white; border-color: #2563eb; transform: translateY(-1px); }
.pm-action-btn--delete   { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.pm-action-btn--delete:hover:not(:disabled) { background: #dc2626; color: white; border-color: #dc2626; transform: translateY(-1px); }

/* Empty */
.pm-empty { text-align: center; padding: 56px 20px !important; }
.pm-empty__inner { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.pm-empty__icon {
  width: 52px; height: 52px; border-radius: 50%; background: #f0f2f5;
  display: flex; align-items: center; justify-content: center; color: #9ca3af;
}
.pm-empty__inner p { color: #6b7280; font-size: 14px; margin: 0; font-weight: 500; }

/* Pagination */
.pm-pagination {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-top: 1px solid #f0f2f5; background: #fafbfc;
}
.pm-page-info { font-size: 12.5px; color: #9ca3af; font-weight: 600; }
.pm-page-btns { display: flex; align-items: center; gap: 4px; }
.pm-page-btn {
  min-width: 34px; height: 34px; display: inline-flex; align-items: center; justify-content: center;
  border: 1.5px solid #e4e7ec; border-radius: 8px; background: #ffffff;
  font-family: 'Plus Jakarta Sans', sans-serif; font-size: 13px; font-weight: 700;
  color: #0f1117; cursor: pointer; transition: all 0.15s; padding: 0 6px;
}
.pm-page-btn:hover:not(:disabled) { border-color: #2563eb; color: #2563eb; }
.pm-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.pm-page-btn.active { background: #0f1117; border-color: #0f1117; color: white; }

/* ── Dialog ── */
.pm-overlay {
  position: fixed; inset: 0; background: rgba(10,12,20,0.28);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; padding: 20px;
}
.pm-dialog {
  background: #fff; border-radius: 20px; width: 100%; max-width: 480px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.13), 0 8px 24px rgba(0,0,0,0.06);
  overflow: hidden; max-height: 90vh; overflow-y: auto;
}
.pm-dialog--sm { max-width: 420px; }
.pm-dialog--lg { max-width: 600px; }
.pm-dialog--xl { max-width: 800px; }

.pm-dialog__band {
  position: relative; height: 110px; display: flex;
  align-items: center; justify-content: center; overflow: hidden;
}
.pm-dialog__band--blue { background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 60%, #60a5fa 100%); }
.pm-dialog__band--red  { background: linear-gradient(135deg, #b91c1c 0%, #dc2626 60%, #f87171 100%); }
.pm-band-circles { position: absolute; inset: 0; pointer-events: none; }
.pm-band-c { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.1); }
.pm-band-c--1 { width: 130px; height: 130px; top: -40px; right: -25px; }
.pm-band-c--2 { width: 70px;  height: 70px;  bottom: -15px; left: 20px; }
.pm-band-c--3 { width: 45px;  height: 45px;  top: 12px; left: 48%; background: rgba(255,255,255,0.07); }
.pm-dialog__icon-ring {
  position: relative; z-index: 1; width: 60px; height: 60px;
  background: rgba(255,255,255,0.18); border: 2px solid rgba(255,255,255,0.35);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  color: #fff; backdrop-filter: blur(4px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.14), inset 0 1px 0 rgba(255,255,255,0.3);
}
.pm-dialog__body { padding: 26px 30px 28px; display: flex; flex-direction: column; gap: 14px; }
.pm-dialog__badge {
  display: inline-flex; align-self: flex-start;
  font-size: 10.5px; font-weight: 800; letter-spacing: 0.1em; text-transform: uppercase;
  padding: 3px 10px; border-radius: 999px;
}
.pm-dialog__badge--blue { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.pm-dialog__badge--red  { background: #fff1f2; color: #dc2626; border: 1px solid #fecdd3; }
.pm-dialog__title { font-size: 20px; font-weight: 800; letter-spacing: -0.03em; margin: 0; line-height: 1.2; }
.pm-dialog__desc { font-size: 14px; color: #6b7280; line-height: 1.65; margin: 0; }
.pm-dialog__hi--red {
  font-weight: 700; font-family: 'JetBrains Mono', monospace; font-size: 13px;
  color: #dc2626; background: #fff1f2; padding: 1px 6px; border-radius: 5px;
}
.pm-dialog__notice {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px;
  border-radius: 8px; font-size: 12.5px; font-weight: 600; line-height: 1.4;
}
.pm-dialog__notice--red  { background: #fff1f2; color: #b91c1c; border: 1px solid #fecdd3; }
.pm-dialog__notice--blue { background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; }
.pm-dialog__actions { display: flex; gap: 10px; justify-content: flex-end; padding-top: 4px; }

.pm-alert {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px;
  background: #fff1f2; border: 1px solid #fecdd3;
  border-radius: 8px; font-size: 13px; font-weight: 600; color: #dc2626;
}

/* Tab nav */
.pm-tab-nav { display: flex; gap: 3px; border-bottom: 1px solid #f0f2f5; margin-bottom: 16px; }
.pm-tab-item {
  display: inline-flex; align-items: center; gap: 7px; padding: 10px 16px;
  border: none; border-bottom: 2.5px solid transparent; background: transparent;
  font-family: 'Plus Jakarta Sans', sans-serif; font-size: 13px; font-weight: 700;
  color: #6b7280; cursor: pointer; transition: all 0.15s; white-space: nowrap; margin-bottom: -1px;
}
.pm-tab-item:hover { color: #0f1117; }
.pm-tab-item.active { color: #2563eb; border-bottom-color: #2563eb; }
.pm-tab-content { overflow-y: auto; max-height: 480px; }

/* Form */
.pm-form { display: flex; flex-direction: column; gap: 14px; }
.pm-form--grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.pm-field { display: flex; flex-direction: column; gap: 6px; }
.pm-field__label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #0f1117; }
.pm-field__hint-inline { font-size: 10px; font-weight: 500; color: #9ca3af; font-style: italic; text-transform: none; letter-spacing: 0; }
.pm-required { color: #dc2626; margin-left: 2px; }
.pm-input, .pm-textarea {
  width: 100%; padding: 9px 12px; font-size: 13.5px;
  font-family: 'Plus Jakarta Sans', sans-serif; border: 1.5px solid #e4e7ec;
  border-radius: 8px; color: #0f1117; background: #fafbfc;
  outline: none; transition: border-color 0.15s, box-shadow 0.15s;
}
.pm-input:focus, .pm-textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37,99,235,0.08);
  background: #fff;
}
.pm-input:disabled { background: #f0f2f5; color: #9ca3af; cursor: not-allowed; }
.pm-textarea { resize: vertical; line-height: 1.5; }

.pm-toggle-row { display: flex; gap: 20px; margin-top: 4px; flex-wrap: wrap; }
.pm-toggle-item { display: flex; flex-direction: column; gap: 8px; }
.pm-toggle-label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #0f1117; }

.pm-attr-section { display: flex; flex-direction: column; gap: 8px; }
.pm-attr-header { display: flex; align-items: center; justify-content: space-between; }
.pm-attr-row { display: flex; gap: 8px; align-items: center; }
.pm-icon-btn {
  width: 32px; height: 32px; display: inline-flex; align-items: center;
  justify-content: center; border: none; border-radius: 8px;
  cursor: pointer; transition: all 0.15s; flex-shrink: 0;
}
.pm-icon-btn--red { background: #fff1f2; color: #dc2626; }
.pm-icon-btn--red:hover { background: #dc2626; color: white; }

.pm-image-gallery {
  display: flex; gap: 10px; flex-wrap: wrap; padding: 14px;
  background: #f6f7f9; border-radius: 12px; border: 1px solid #e4e7ec;
}
.pm-image-item { position: relative; }
.pm-gallery-img { width: 96px; height: 96px; border-radius: 8px; object-fit: cover; border: 2px solid #e4e7ec; display: block; }
.pm-gallery-img--primary { border-color: #16a34a; }
.pm-gallery-badge {
  position: absolute; top: 0; left: 0; background: #16a34a; color: white;
  font-size: 9px; font-weight: 800; padding: 2px 6px;
  border-radius: 8px 0 8px 0; letter-spacing: 0.05em;
}
.pm-gallery-actions { display: flex; justify-content: center; gap: 6px; margin-top: 6px; }
.pm-gallery-btn {
  width: 28px; height: 28px; border-radius: 50%; border: 1.5px solid #e4e7ec;
  display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.15s;
}
.pm-gallery-btn--star { background: #fffbeb; color: #d97706; border-color: #fde68a; }
.pm-gallery-btn--star:hover { background: #d97706; color: white; border-color: #d97706; }
.pm-gallery-btn--del  { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.pm-gallery-btn--del:hover  { background: #dc2626; color: white; border-color: #dc2626; }

.pm-file-label {
  display: inline-flex; align-items: center; gap: 8px; padding: 9px 14px;
  border: 1.5px dashed #e4e7ec; border-radius: 8px;
  background: #fafbfc; font-size: 13px; font-weight: 600; color: #6b7280;
  cursor: pointer; transition: all 0.15s; width: 100%;
}
.pm-file-label:hover { border-color: #2563eb; color: #2563eb; background: #eff6ff; }

.pm-serial-controls { display: flex; align-items: flex-end; gap: 14px; flex-wrap: wrap; }
.pm-serial-label {
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.07em; color: #9ca3af; margin-bottom: 8px;
}
.pm-form-footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }

/* History timeline */
.pm-history-loading { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 40px; color: #6b7280; font-size: 13px; }
.pm-timeline { display: flex; flex-direction: column; gap: 0; padding: 4px 0; }
.pm-timeline-item { display: flex; gap: 14px; padding-bottom: 20px; position: relative; }
.pm-timeline-item::before { content: ''; position: absolute; left: 6px; top: 22px; bottom: 0; width: 1px; background: #f0f2f5; }
.pm-timeline-item:last-child::before { display: none; }
.pm-timeline-dot {
  width: 14px; height: 14px; border-radius: 50%; flex-shrink: 0;
  margin-top: 4px; border: 2px solid white; box-shadow: 0 0 0 1px #e4e7ec;
}
.pm-timeline-dot--primary { background: #2563eb; }
.pm-timeline-dot--warning  { background: #d97706; }
.pm-timeline-dot--danger   { background: #dc2626; }
.pm-timeline-content {
  flex: 1; background: #ffffff; border: 1px solid #e4e7ec;
  border-radius: 12px; padding: 14px 16px; transition: all 0.15s;
}
.pm-timeline-content:hover { border-color: #d1d5db; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.pm-timeline-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.pm-timeline-user { display: flex; align-items: center; gap: 8px; }
.pm-avatar {
  width: 26px; height: 26px; border-radius: 8px; background: #eff6ff;
  color: #2563eb; display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 800; flex-shrink: 0;
}
.pm-timeline-username { font-size: 13.5px; font-weight: 700; color: #0f1117; }
.pm-timeline-meta { display: flex; align-items: center; gap: 8px; }
.pm-timeline-time { font-size: 11px; font-weight: 600; color: #9ca3af; font-family: 'JetBrains Mono', monospace; }
.pm-tag--log { font-weight: 800; font-size: 10.5px; padding: 2px 8px; border-radius: 999px; }
.pm-tag--log-primary { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.pm-tag--log-warning  { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }
.pm-tag--log-danger   { background: #fff1f2; color: #dc2626; border: 1px solid #fecdd3; }
.pm-timeline-desc { margin: 8px 0 0; color: #4a5568; font-size: 13px; line-height: 1.55; white-space: pre-wrap; }
.pm-timeline-ip {
  display: inline-flex; align-items: center; gap: 5px; margin-top: 8px;
  padding: 3px 10px; background: #f6f7f9; border: 1px solid #f0f2f5;
  border-radius: 6px; font-size: 11px; color: #9ca3af; font-weight: 600;
}

/* ── Drawer ── */
.pm-drawer-backdrop {
  position: fixed; inset: 0; background: rgba(10,12,20,0.22);
  z-index: 999; display: flex; justify-content: flex-end;
}
.pm-drawer {
  width: 58%; max-width: 860px; background: #ffffff;
  height: 100%; display: flex; flex-direction: column;
  box-shadow: -8px 0 40px rgba(0,0,0,0.12); border-left: 1px solid #e4e7ec;
}
.pm-drawer__header {
  display: flex; align-items: flex-start; justify-content: space-between;
  padding: 22px 28px 20px; border-bottom: 1px solid #e4e7ec;
  flex-shrink: 0; background: #fafbfc;
}
.pm-drawer__eyebrow { font-size: 11px; font-weight: 700; letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af; margin-bottom: 4px; }
.pm-drawer__title { font-size: 20px; font-weight: 800; letter-spacing: -0.02em; margin: 0; }
.pm-drawer__close {
  width: 32px; height: 32px; border: 1.5px solid #e4e7ec; border-radius: 8px;
  background: #ffffff; color: #6b7280; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.15s; flex-shrink: 0;
}
.pm-drawer__close:hover { border-color: #dc2626; color: #dc2626; background: #fff1f2; }
.pm-drawer__body { flex: 1; overflow-y: auto; padding: 24px 28px; display: flex; flex-direction: column; gap: 24px; }
.pm-drawer-section { display: flex; flex-direction: column; gap: 14px; }
.pm-drawer-section--form { padding: 20px; background: #f6f7f9; border: 1px solid #e4e7ec; border-radius: 12px; }
.pm-drawer-section__title { font-size: 13px; font-weight: 800; color: #0f1117; letter-spacing: -0.01em; }
.pm-drawer-section__title--edit { color: #2563eb; }
.pm-drawer-section__title--add  { color: #16a34a; }

/* ── Spinner ── */
.pm-spinner {
  display: inline-block; width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.1); border-top-color: currentColor;
  border-radius: 50%; animation: spin-r 0.6s linear infinite; flex-shrink: 0;
}
.pm-spinner--white { border-color: rgba(255,255,255,0.25); border-top-color: #fff; }

/* ── Transitions ── */
.pm-dialog-fade-enter-active, .pm-dialog-fade-leave-active { transition: opacity 0.22s ease; }
.pm-dialog-fade-enter-active .pm-dialog,
.pm-dialog-fade-leave-active .pm-dialog { transition: transform 0.28s cubic-bezier(0.34,1.4,0.64,1), opacity 0.22s ease; }
.pm-dialog-fade-enter-from, .pm-dialog-fade-leave-to { opacity: 0; }
.pm-dialog-fade-enter-from .pm-dialog { transform: scale(0.9) translateY(20px); opacity: 0; }
.pm-dialog-fade-leave-to  .pm-dialog { transform: scale(0.96) translateY(8px); opacity: 0; }

.pm-drawer-slide-enter-active, .pm-drawer-slide-leave-active { transition: opacity 0.22s ease; }
.pm-drawer-slide-enter-active .pm-drawer,
.pm-drawer-slide-leave-active .pm-drawer { transition: transform 0.3s cubic-bezier(0.4,0,0.2,1); }
.pm-drawer-slide-enter-from, .pm-drawer-slide-leave-to { opacity: 0; }
.pm-drawer-slide-enter-from .pm-drawer { transform: translateX(100%); }
.pm-drawer-slide-leave-to   .pm-drawer { transform: translateX(100%); }

.pm-slide-down-enter-active, .pm-slide-down-leave-active { transition: all 0.2s ease; }
.pm-slide-down-enter-from, .pm-slide-down-leave-to { opacity: 0; transform: translateY(-8px); }

/* ── Responsive ── */
@media (max-width: 1200px) {
  .pm-filter-row { grid-template-columns: 1fr 1fr; }
  .pm-filter-row--sep { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 900px) {
  .pm-page { padding: 16px 16px 40px; }
  .pm-header { flex-direction: column; }
  .pm-form--grid { grid-template-columns: 1fr; }
  .pm-drawer { width: 92%; }
  .pm-dialog--xl { max-width: 96vw; }
}
@media (max-width: 640px) {
  .pm-filter-row,
  .pm-filter-row--sep { grid-template-columns: 1fr; }
  .pm-ff--actions { flex-wrap: wrap; }
}
</style>