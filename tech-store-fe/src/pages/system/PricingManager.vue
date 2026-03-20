<template>
  <div class="pp-page">

    <!-- ── Header ── -->
    <header class="pp-header">
      <div class="pp-header__left">
        <div class="pp-eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="1" x2="12" y2="23"/>
            <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
          </svg>
          Hệ thống
        </div>
        <h1 class="pp-title">
          Quản lý giá <span class="pp-title__accent">&amp; Xung đột</span>
        </h1>
        <p class="pp-subtitle">Quản lý bảng giá, lịch sử và cảnh báo xung đột toàn hệ thống</p>
      </div>

      <div v-if="dashData" class="pp-header__right">
        <div class="pp-summary">
          <div class="pp-summary__item">
            <span class="pp-summary__dot" style="background:#16a34a"></span>
            <span class="pp-summary__count">{{ dashData.activePromotions ?? 0 }}</span>
            <span class="pp-summary__label">KM active</span>
          </div>
          <div class="pp-summary__item" :class="(dashData.variantsBelowCost ?? 0) > 0 ? 'pp-summary__item--red' : ''">
            <span class="pp-summary__dot" :style="{background:(dashData.variantsBelowCost ?? 0) > 0 ? '#dc2626' : '#9ca3af'}"></span>
            <span class="pp-summary__count">{{ dashData.variantsBelowCost ?? 0 }}</span>
            <span class="pp-summary__label">dưới giá nhập</span>
          </div>
          <div class="pp-summary__item" :class="(dashData.expiringIn3Days ?? 0) > 0 ? 'pp-summary__item--orange' : ''">
            <span class="pp-summary__dot" :style="{background:(dashData.expiringIn3Days ?? 0) > 0 ? '#d97706' : '#9ca3af'}"></span>
            <span class="pp-summary__count">{{ dashData.expiringIn3Days ?? 0 }}</span>
            <span class="pp-summary__label">sắp hết hạn</span>
          </div>
        </div>
      </div>
    </header>

    <!-- ── Tab bar ── -->
    <div class="pp-tabbar">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="pp-tab"
        :class="{ 'pp-tab--active': activeTab === t.key }"
        @click="activeTab = t.key"
      >
        <span class="pp-tab__icon">{{ t.icon }}</span>
        {{ t.label }}
      </button>
    </div>

    <!-- ════════════════════════════════════════
         TAB: ĐẶT GIÁ
    ═════════════════════════════════════════ -->
    <div v-show="activeTab === 'set'" class="pp-pane">
      <div class="pp-grid2">

        <!-- Card: Đặt giá variant -->
        <div class="pp-card">
          <div class="pp-card__head">
            <span class="pp-card__title">Đặt giá variant</span>
          </div>

          <div class="pp-card__body">
            <div class="pp-field">
              <label class="pp-field__label">Chọn variant theo sản phẩm</label>
              <div class="pp-picker-bar">
                <input
                  v-model.number="variantPickerProductId"
                  class="pp-input"
                  placeholder="Product ID"
                  style="width:130px"
                  type="number"
                  @keyup.enter="loadVariantsForPicker"
                />
                <button
                  class="pp-btn pp-btn--outline"
                  :class="{'pp-btn--loading': variantPickerLoading}"
                  @click="loadVariantsForPicker"
                  :disabled="variantPickerLoading"
                >
                  <svg v-if="!variantPickerLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                  </svg>
                  <span v-if="!variantPickerLoading">Tải</span>
                  <span v-else class="pp-spinner"></span>
                </button>
                <span v-if="setForm.variantId" class="pp-selected-badge">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  Variant #{{ setForm.variantId }}
                </span>
                <button v-if="setForm.variantId" class="pp-icon-btn" @click="setForm.variantId = null; variantList = []; variantPickerOpen = false; currentPricePreview = null;">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                </button>
              </div>

              <!-- Dropdown variant -->
              <div v-if="variantPickerOpen && variantList.length" class="pp-variant-dropdown">
                <div
                  v-for="v in variantList"
                  :key="v.id"
                  class="pp-variant-option"
                  :class="{
                    'pp-variant-option--inactive': !v.isActive,
                    'pp-variant-option--selected': setForm.variantId === v.id
                  }"
                  @click="selectVariant(v)"
                >
                  <div class="pp-variant-option__main">
                    <span class="pp-variant-option__name">{{ v.variantName }}</span>
                    <span class="pp-variant-option__sku">{{ v.sku }}</span>
                  </div>
                  <div class="pp-variant-option__meta">
                    <span :class="v.costPrice && v.price < v.costPrice ? 'pp-text--red' : ''">
                      {{ fmtMoney(v.price) }}
                    </span>
                    <span class="pp-variant-option__stock" :class="v.stockQuantity === 0 ? 'pp-text--red' : 'pp-text--muted'">
                      Tồn: {{ v.stockQuantity }}
                    </span>
                    <span v-if="!v.isActive" class="pp-tag pp-tag--red" style="font-size:9px">Inactive</span>
                  </div>
                </div>
              </div>
              <div v-else-if="variantPickerOpen && !variantList.length" class="pp-empty" style="padding:14px 0">
                Không có variant nào.
              </div>
            </div>

            <div class="pp-row2">
              <div class="pp-field">
                <label class="pp-field__label">Variant ID <span class="pp-required">*</span></label>
                <input
                  v-model.number="setForm.variantId"
                  class="pp-input"
                  placeholder="Hoặc nhập trực tiếp"
                  type="number"
                  @blur="loadCurrentPricePreview"
                />
              </div>
              <div class="pp-field">
                <label class="pp-field__label">Currency</label>
                <div class="pp-input pp-input--readonly">{{ setForm.currencyCode || 'VND' }}</div>
              </div>
            </div>

            <!-- Giá hiện tại preview -->
            <div v-if="currentPricePreview" class="pp-price-preview">
              <div class="pp-price-preview__item">
                <span class="pp-price-preview__label">Giá đang active</span>
                <span class="pp-price-preview__value" :class="currentPricePreview.belowCost ? 'pp-text--red' : 'pp-text--green'">
                  {{ fmtMoney(currentPricePreview.price) }}
                </span>
              </div>
              <div class="pp-price-preview__divider"></div>
              <div class="pp-price-preview__item">
                <span class="pp-price-preview__label">Giá nhập</span>
                <span class="pp-price-preview__value pp-mono">
                  {{ currentPricePreview.costPrice ? fmtMoney(currentPricePreview.costPrice) : '—' }}
                </span>
              </div>
              <template v-if="currentPricePreview.belowCost">
                <div class="pp-price-preview__divider"></div>
                <div class="pp-price-preview__item">
                  <span class="pp-price-preview__label">Cảnh báo</span>
                  <span class="pp-price-preview__warn">⚠️ Giá bán thấp hơn giá nhập</span>
                </div>
              </template>
            </div>

            <div class="pp-row2">
              <div class="pp-field">
                <label class="pp-field__label">Giá bán</label>
                <input v-model.number="setForm.price" class="pp-input pp-input--money" placeholder="42900000" type="number" />
                <span v-if="setForm.price" class="pp-field__hint">{{ fmtMoney(setForm.price) }}</span>
                <span v-if="setForm.price && setForm.costPrice && setForm.price < setForm.costPrice" class="pp-field__hint pp-text--red">⚠️ Giá bán thấp hơn giá nhập!</span>
              </div>
              <div class="pp-field">
                <label class="pp-field__label">Giá nhập</label>
                <input v-model.number="setForm.costPrice" class="pp-input pp-input--money" placeholder="36000000" type="number" />
                <span v-if="setForm.costPrice" class="pp-field__hint">{{ fmtMoney(setForm.costPrice) }}</span>
              </div>
            </div>

            <div class="pp-field">
              <label class="pp-field__label">Lý do</label>
              <input v-model="setForm.reason" class="pp-input" placeholder="MANUAL / PROMO_ENDED / PRICE_UPDATE" />
            </div>
          </div>

          <div class="pp-card__foot">
            <button class="pp-btn pp-btn--primary" :class="{'pp-btn--loading': setLoading}" @click="setPrice" :disabled="setLoading">
              <svg v-if="!setLoading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                <polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>
              </svg>
              <span v-if="!setLoading">Lưu giá</span>
              <span v-else class="pp-spinner pp-spinner--white"></span>
            </button>
          </div>
        </div>

        <!-- Card: Giá hiệu lực -->
        <div class="pp-card">
          <div class="pp-card__head">
            <span class="pp-card__title">Giá hiệu lực</span>
          </div>

          <div class="pp-card__body">
            <p class="pp-hint-text">Dùng Variant ID đã nhập bên trái.</p>
            <button class="pp-btn pp-btn--outline" :class="{'pp-btn--loading': effLoading}" @click="loadEffective" :disabled="effLoading">
              <svg v-if="!effLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
              </svg>
              <span v-if="!effLoading">Tải giá hiệu lực</span>
              <span v-else class="pp-spinner"></span>
            </button>

            <div v-if="effective" class="pp-result">
              <div class="pp-result__row">
                <span class="pp-result__key">SKU</span>
                <span class="pp-result__val pp-mono">{{ effective.sku ?? '—' }}</span>
              </div>
              <div class="pp-result__row">
                <span class="pp-result__key">Base price</span>
                <span class="pp-result__val">{{ fmtMoney(effective.price) }}</span>
              </div>
              <div class="pp-result__row pp-result__row--hl">
                <span class="pp-result__key">Final price</span>
                <span class="pp-result__val pp-text--green">{{ fmtMoney(effective.finalPrice) }}</span>
              </div>
              <div class="pp-result__row">
                <span class="pp-result__key">Discount</span>
                <span class="pp-result__val pp-text--red">-{{ fmtMoney((effective.price ?? 0) - (effective.finalPrice ?? 0)) }}</span>
              </div>
              <div class="pp-result__row">
                <span class="pp-result__key">Promotion</span>
                <span class="pp-result__val">
                  <span v-if="effective.promotionCode" class="pp-tag pp-tag--orange">{{ effective.promotionCode }}</span>
                  <span v-else class="pp-text--muted">none</span>
                </span>
              </div>
              <div v-if="effective.comboInfo" class="pp-result__row">
                <span class="pp-result__key">Combo</span>
                <span class="pp-result__val"><span class="pp-tag pp-tag--green">{{ effective.comboInfo }}</span></span>
              </div>
            </div>

            <div class="pp-divider"></div>

            <div class="pp-sub-head">
              <span class="pp-sub-title">Giá theo khách hàng</span>
            </div>
            <div class="pp-inline">
              <input v-model.number="customerIdForPrice" class="pp-input" placeholder="Customer ID" style="width:130px" type="number" />
              <button class="pp-btn pp-btn--outline" :class="{'pp-btn--loading': effCustLoading}" @click="loadEffectiveForCustomer" :disabled="effCustLoading">
                <span v-if="!effCustLoading">Tải</span>
                <span v-else class="pp-spinner"></span>
              </button>
            </div>
            <div v-if="effectiveForCustomer" class="pp-result">
              <div class="pp-result__row">
                <span class="pp-result__key">Variant</span>
                <span class="pp-result__val">{{ effectiveForCustomer.variantName ?? '—' }}</span>
              </div>
              <div class="pp-result__row">
                <span class="pp-result__key">Base price</span>
                <span class="pp-result__val">{{ fmtMoney(effectiveForCustomer.price) }}</span>
              </div>
              <div class="pp-result__row pp-result__row--hl">
                <span class="pp-result__key">Final price</span>
                <span class="pp-result__val pp-text--green">{{ fmtMoney(effectiveForCustomer.finalPrice) }}</span>
              </div>
              <div class="pp-result__row">
                <span class="pp-result__key">Promotion</span>
                <span class="pp-result__val">
                  <span v-if="effectiveForCustomer.promotionCode" class="pp-tag pp-tag--orange">{{ effectiveForCustomer.promotionCode }}</span>
                  <span v-else class="pp-text--muted">none</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ════════════════════════════════════════
         TAB: LỊCH SỬ GIÁ
    ═════════════════════════════════════════ -->
    <div v-show="activeTab === 'history'" class="pp-pane">

      <!-- Giá theo sản phẩm -->
      <div class="pp-card pp-mb">
        <div class="pp-card__head">
          <span class="pp-card__title">Giá theo sản phẩm</span>
        </div>
        <div class="pp-card__body">
          <div class="pp-inline">
            <input v-model.number="productId" class="pp-input" placeholder="Product ID" style="width:140px" type="number" />
            <button class="pp-btn pp-btn--primary" :class="{'pp-btn--loading': prodLoading}" @click="loadByProduct" :disabled="prodLoading">
              <span v-if="!prodLoading">Tải</span>
              <span v-else class="pp-spinner pp-spinner--white"></span>
            </button>
          </div>
          <div v-if="productPrices.length" class="pp-table-wrap pp-mt">
            <table class="pp-table">
              <thead>
                <tr>
                  <th>Variant ID</th>
                  <th>Tên variant</th>
                  <th>SKU</th>
                  <th>Giá nhập</th>
                  <th>Base price</th>
                  <th>Final price</th>
                  <th>Giảm giá</th>
                  <th>Promotion</th>
                  <th>Combo</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="row in productPrices"
                  :key="row.variantId"
                  class="pp-row"
                  :class="row.costPrice && row.price < row.costPrice ? 'pp-row--danger' : ''"
                >
                  <td><span class="pp-mono-id">{{ row.variantId }}</span></td>
                  <td>{{ row.variantName }}</td>
                  <td><span class="pp-mono pp-text--muted">{{ row.sku }}</span></td>
                  <td>
                    <span v-if="row.costPrice" class="pp-mono" :class="row.price < row.costPrice ? 'pp-text--red pp-bold' : 'pp-text--muted'">
                      {{ fmtMoney(row.costPrice) }}
                      <span v-if="row.price < row.costPrice" title="Giá bán thấp hơn giá nhập">⚠️</span>
                    </span>
                    <span v-else class="pp-text--muted">—</span>
                  </td>
                  <td>{{ fmtMoney(row.price) }}</td>
                  <td :class="row.finalPrice < row.price ? 'pp-text--green pp-bold' : ''">
                    {{ fmtMoney(row.finalPrice) }}
                    <span v-if="row.costPrice && row.price < row.costPrice" title="Giá bán thấp hơn giá nhập">⚠️</span>
                  </td>
                  <td>
                    <span v-if="row.discountAmount > 0" class="pp-text--red">-{{ fmtMoney(row.discountAmount) }}</span>
                    <span v-else class="pp-text--muted">—</span>
                  </td>
                  <td>
                    <span v-if="row.promotionCode" class="pp-tag pp-tag--orange">{{ row.promotionCode }}</span>
                    <span v-else class="pp-text--muted">—</span>
                  </td>
                  <td>
                    <span v-if="row.comboInfo" class="pp-tag pp-tag--green">{{ row.comboInfo }}</span>
                    <span v-else class="pp-text--muted">—</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Lịch sử giá variant -->
      <div class="pp-card">
        <div class="pp-card__head">
          <span class="pp-card__title">Lịch sử giá variant</span>
        </div>
        <div class="pp-card__body">
          <div class="pp-inline">
            <input v-model.number="histVariantId" class="pp-input" placeholder="Variant ID" style="width:140px" type="number" />
            <button class="pp-btn pp-btn--primary" :class="{'pp-btn--loading': histLoading}" @click="loadVariantHistory(0)" :disabled="histLoading">
              <span v-if="!histLoading">Tải lịch sử</span>
              <span v-else class="pp-spinner pp-spinner--white"></span>
            </button>
          </div>

          <div v-if="variantHistory.length" class="pp-table-wrap pp-mt">
            <table class="pp-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Giá bán</th>
                  <th>Giá nhập</th>
                  <th>Lý do</th>
                  <th>Hiệu lực từ</th>
                  <th>Hiệu lực đến</th>
                  <th>Người tạo</th>
                  <th style="text-align:center">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in variantHistory" :key="row.id" class="pp-row" :class="row.belowCost ? 'pp-row--danger' : ''">
                  <td><span class="pp-mono-id">{{ row.id }}</span></td>
                  <td>
                    <span class="pp-bold" :class="row.belowCost ? 'pp-text--red' : ''">
                      {{ fmtMoney(row.price) }}
                      <span v-if="row.belowCost" title="Giá bán thấp hơn giá nhập">⚠️</span>
                    </span>
                  </td>
                  <td>{{ row.costPrice ? fmtMoney(row.costPrice) : '—' }}</td>
                  <td><span class="pp-tag pp-tag--blue">{{ row.reason || '—' }}</span></td>
                  <td><span class="pp-mono pp-text--muted pp-small">{{ fmtDate(row.effectiveFrom) }}</span></td>
                  <td><span class="pp-mono pp-text--muted pp-small">{{ row.effectiveTo ? fmtDate(row.effectiveTo) : '—' }}</span></td>
                  <td>{{ row.createdByUsername || '—' }}</td>
                  <td>
                    <div class="pp-actions">
                      <button v-if="!row.effectiveTo" class="pp-action-btn pp-action-btn--edit" @click="openEdit(row)">Sửa</button>
                      <button class="pp-action-btn pp-action-btn--delete" @click="removeHistory(row)">Xóa</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <div v-if="histTotalPages > 1" class="pp-pagination">
              <button class="pp-btn pp-btn--ghost" :disabled="histPage === 0" @click="changeHistPage(histPage - 1)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
                Trước
              </button>
              <span class="pp-pagination__info">Trang {{ histPage + 1 }} / {{ histTotalPages }}</span>
              <button class="pp-btn pp-btn--ghost" :disabled="histPage >= histTotalPages - 1" @click="changeHistPage(histPage + 1)">
                Sau
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
              </button>
            </div>
          </div>
          <div v-else-if="histLoaded" class="pp-empty">Không có bản ghi nào.</div>
        </div>
      </div>
    </div>

    <!-- ════════════════════════════════════════
         TAB: CẢNH BÁO GIÁ
    ═════════════════════════════════════════ -->
    <div v-show="activeTab === 'warning'" class="pp-pane">

      <!-- Quét toàn hệ thống -->
      <div class="pp-card pp-mb">
        <div class="pp-card__head">
          <span class="pp-card__title">Quét toàn hệ thống — Giá bán &lt; Giá nhập</span>
          <button class="pp-btn pp-btn--orange pp-ml-auto" :class="{'pp-btn--loading': conflictsLoading}" @click="loadConflicts" :disabled="conflictsLoading">
            <svg v-if="!conflictsLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            <span v-if="!conflictsLoading">Quét toàn hệ thống</span>
            <span v-else class="pp-spinner pp-spinner--white"></span>
          </button>
        </div>

        <div class="pp-card__body">
          <template v-if="conflictsLoaded">
            <div v-if="!priceConflicts.length" class="pp-alert pp-alert--green">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              Không phát hiện variant nào có giá bán thấp hơn giá nhập.
            </div>
            <template v-else>
              <div class="pp-alert pp-alert--red">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                {{ priceConflicts.length }} variant có giá bán thấp hơn giá nhập
              </div>
              <div class="pp-table-wrap pp-mt">
                <table class="pp-table">
                  <thead>
                    <tr>
                      <th>Variant ID</th>
                      <th>Tên variant</th>
                      <th>SKU</th>
                      <th>Giá bán</th>
                      <th>Giá nhập</th>
                      <th>Chênh lệch</th>
                      <th>Currency</th>
                      <th style="text-align:center">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="row in priceConflicts" :key="row.variantId" class="pp-row pp-row--danger">
                      <td><span class="pp-mono-id">{{ row.variantId }}</span></td>
                      <td>{{ row.variantName }}</td>
                      <td><span class="pp-mono pp-text--muted">{{ row.sku }}</span></td>
                      <td><span class="pp-text--red pp-bold">{{ fmtMoney(row.price) }}</span></td>
                      <td>{{ fmtMoney(row.costPrice) }}</td>
                      <td><span class="pp-text--red">-{{ fmtMoney(row.diff) }}</span></td>
                      <td><span class="pp-mono pp-text--muted">{{ row.currencyCode }}</span></td>
                      <td style="text-align:center">
                        <button class="pp-action-btn pp-action-btn--edit" @click="openQuickFix(row)">✏️ Sửa giá</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </template>
          </template>
          <div v-else class="pp-empty">Nhấn "Quét toàn hệ thống" để bắt đầu kiểm tra.</div>
        </div>
      </div>

      <!-- Kiểm tra nhanh 1 variant -->
      <div class="pp-card">
        <div class="pp-card__head">
          <span class="pp-card__title">Kiểm tra nhanh 1 variant</span>
        </div>
        <div class="pp-card__body">
          <div class="pp-inline">
            <input v-model.number="costWarnVariantId" class="pp-input" placeholder="Variant ID" style="width:140px" type="number" />
            <button class="pp-btn pp-btn--orange" :class="{'pp-btn--loading': costWarnLoading}" @click="loadCostWarning" :disabled="costWarnLoading">
              <svg v-if="!costWarnLoading" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              <span v-if="!costWarnLoading">Kiểm tra</span>
              <span v-else class="pp-spinner pp-spinner--white"></span>
            </button>
          </div>
          <div v-if="costWarning" class="pp-mt">
            <div class="pp-alert" :class="costWarning.belowCost ? 'pp-alert--red' : 'pp-alert--green'">
              <svg v-if="!costWarning.belowCost" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              {{ costWarning.belowCost ? (costWarning.message || 'Giá bán THẤP hơn giá nhập!') : 'Giá bán hợp lệ' }}
            </div>
            <div class="pp-result">
              <div class="pp-result__row">
                <span class="pp-result__key">Variant ID</span>
                <span class="pp-result__val pp-mono">{{ costWarning.variantId }}</span>
              </div>
              <div class="pp-result__row">
                <span class="pp-result__key">Tên</span>
                <span class="pp-result__val">{{ costWarning.variantName ?? '—' }}</span>
              </div>
              <div class="pp-result__row">
                <span class="pp-result__key">Giá bán</span>
                <span class="pp-result__val" :class="costWarning.belowCost ? 'pp-text--red' : ''">{{ fmtMoney(costWarning.price) }}</span>
              </div>
              <div class="pp-result__row">
                <span class="pp-result__key">Giá nhập</span>
                <span class="pp-result__val">{{ costWarning.costPrice ? fmtMoney(costWarning.costPrice) : '—' }}</span>
              </div>
            </div>
            <button v-if="costWarning.belowCost" class="pp-btn pp-btn--orange pp-mt" @click="openQuickFix({ variantId: costWarning.variantId, variantName: costWarning.variantName, price: costWarning.price, costPrice: costWarning.costPrice })">
              ✏️ Sửa giá ngay
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ════════════════════════════════════════
         TAB: TÍNH GIÁ ĐƠN
    ═════════════════════════════════════════ -->
    <div v-show="activeTab === 'calculate'" class="pp-pane">
      <div class="pp-grid2">

        <!-- Card: Giỏ hàng -->
        <div class="pp-card">
          <div class="pp-card__head">
            <span class="pp-card__title">Giỏ hàng</span>
          </div>
          <div class="pp-card__body">
            <div class="pp-field">
              <label class="pp-field__label">
                Customer ID
                <span class="pp-text--muted" style="font-weight:400;text-transform:none;letter-spacing:0">(tùy chọn)</span>
              </label>
              <input v-model.number="calcForm.customerId" class="pp-input" placeholder="VD: 1 — Nguyễn Minh Tuấn VIP Gold" type="number" />
            </div>

            <div class="pp-field pp-mt">
              <label class="pp-field__label">Sản phẩm</label>
              <div class="pp-items">
                <div v-for="(item, idx) in calcForm.items" :key="idx" class="pp-item-row">
                  <span class="pp-item-num">{{ idx + 1 }}</span>
                  <input v-model.number="item.variantId" class="pp-input" placeholder="Variant ID" style="width:120px" type="number" />
                  <span class="pp-item-sep">×</span>
                  <input v-model.number="item.quantity" class="pp-input" placeholder="SL" style="width:60px" type="number" :min="1" />
                  <button class="pp-icon-btn" @click="calcForm.items.splice(idx, 1)">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  </button>
                </div>
              </div>
              <button class="pp-btn pp-btn--ghost pp-mt" @click="calcForm.items.push({ variantId: null, quantity: 1 })">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Thêm sản phẩm
              </button>
            </div>
          </div>
          <div class="pp-card__foot">
            <button class="pp-btn pp-btn--primary" :class="{'pp-btn--loading': calcLoading}" @click="calculateOrder" :disabled="calcLoading">
              <svg v-if="!calcLoading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <rect x="4" y="2" width="16" height="20" rx="2"/>
                <line x1="8" y1="6" x2="16" y2="6"/><line x1="8" y1="10" x2="16" y2="10"/><line x1="8" y1="14" x2="12" y2="14"/>
              </svg>
              <span v-if="!calcLoading">Tính giá</span>
              <span v-else class="pp-spinner pp-spinner--white"></span>
            </button>
          </div>
        </div>

        <!-- Card: Kết quả tính giá -->
        <div class="pp-card">
          <div class="pp-card__head">
            <span class="pp-card__title">Kết quả tính giá</span>
          </div>
          <div class="pp-card__body">
            <div v-if="calcResult">
              <div class="pp-calc-box">
                <div class="pp-calc-row">
                  <span>Tổng giá gốc</span>
                  <span>{{ fmtMoney(calcResult.subtotal) }}</span>
                </div>
                <div class="pp-calc-row pp-calc-row--disc">
                  <span>Giảm giá</span>
                  <span class="pp-text--red">-{{ fmtMoney(calcResult.discountTotal) }}</span>
                </div>
                <div v-if="calcResult.freeQty" class="pp-calc-row">
                  <span>Tặng kèm</span>
                  <span class="pp-tag pp-tag--green">{{ calcResult.freeQty }} sp miễn phí</span>
                </div>
                <div class="pp-calc-sep"></div>
                <div class="pp-calc-row pp-calc-row--total">
                  <span>Tổng thanh toán</span>
                  <span class="pp-text--green">{{ fmtMoney(calcResult.finalTotal) }}</span>
                </div>
              </div>

              <div v-if="calcResult.items?.length" class="pp-table-wrap pp-mt">
                <table class="pp-table pp-table--sm">
                  <thead>
                    <tr>
                      <th>Variant</th>
                      <th>Tên</th>
                      <th style="text-align:center">SL</th>
                      <th>Đơn giá</th>
                      <th>Sau KM</th>
                      <th>Thành tiền</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in calcResult.items" :key="item.variantId" class="pp-row">
                      <td><span class="pp-mono-id">{{ item.variantId }}</span></td>
                      <td>{{ item.variantName }}</td>
                      <td style="text-align:center">{{ item.quantity }}</td>
                      <td>{{ fmtMoney(item.unitPrice) }}</td>
                      <td :class="item.finalUnitPrice < item.unitPrice ? 'pp-text--green pp-bold' : ''">
                        {{ fmtMoney(item.finalUnitPrice) }}
                      </td>
                      <td class="pp-bold">{{ fmtMoney(item.lineTotal) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <div v-else class="pp-empty">Nhập sản phẩm và nhấn "Tính giá".</div>
          </div>
        </div>
      </div>
    </div>

    <!-- ════════════════════════════════════════
         TAB: DASHBOARD
    ═════════════════════════════════════════ -->
    <div v-show="activeTab === 'dashboard'" class="pp-pane">
      <div class="pp-toolbar pp-mb">
        <button class="pp-btn pp-btn--outline pp-ml-auto" :class="{'pp-btn--loading': dashLoading}" @click="loadDashboard" :disabled="dashLoading">
          <svg v-if="!dashLoading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
            <path d="M21 3v5h-5"/>
            <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
            <path d="M8 16H3v5"/>
          </svg>
          <span v-if="!dashLoading">Làm mới</span>
          <span v-else class="pp-spinner"></span>
        </button>
      </div>

      <template v-if="dashData">
        <div class="pp-stats4">
          <div class="pp-stat pp-stat--green">
            <div class="pp-stat__icon">🏷️</div>
            <div class="pp-stat__num">{{ dashData.activePromotions ?? 0 }}</div>
            <div class="pp-stat__label">KM đang chạy</div>
          </div>
          <div class="pp-stat" :class="(dashData.variantsBelowCost ?? 0) > 0 ? 'pp-stat--red' : 'pp-stat--dim'">
            <div class="pp-stat__icon">⚠️</div>
            <div class="pp-stat__num">{{ dashData.variantsBelowCost ?? 0 }}</div>
            <div class="pp-stat__label">Giá dưới giá nhập</div>
          </div>
          <div class="pp-stat" :class="(dashData.expiringIn3Days ?? 0) > 0 ? 'pp-stat--orange' : 'pp-stat--dim'">
            <div class="pp-stat__icon">⏰</div>
            <div class="pp-stat__num">{{ dashData.expiringIn3Days ?? 0 }}</div>
            <div class="pp-stat__label">Sắp hết hạn (3 ngày)</div>
          </div>
          <div class="pp-stat" :class="(dashData.conflictCount ?? 0) > 0 ? 'pp-stat--purple' : 'pp-stat--dim'">
            <div class="pp-stat__icon">🔀</div>
            <div class="pp-stat__num">{{ dashData.conflictCount ?? 0 }}</div>
            <div class="pp-stat__label">Xung đột KM</div>
          </div>
        </div>

        <div class="pp-card pp-mt">
          <div class="pp-card__head">
            <span class="pp-card__title">Chi tiết xung đột khuyến mãi</span>
            <span v-if="dashData.promotionConflicts?.length" class="pp-tag pp-tag--red">
              {{ dashData.promotionConflicts.length }}
            </span>
          </div>
          <div class="pp-card__body">
            <div v-if="dashData.promotionConflicts?.length" class="pp-table-wrap">
              <table class="pp-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Code</th>
                    <th>Tên chiến dịch</th>
                    <th>Lý do xung đột</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in dashData.promotionConflicts" :key="row.id" class="pp-row">
                    <td><span class="pp-mono-id">{{ row.id }}</span></td>
                    <td><span class="pp-tag pp-tag--orange">{{ row.code }}</span></td>
                    <td>{{ row.name }}</td>
                    <td><span class="pp-text--red">{{ row.conflictReason }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="pp-alert pp-alert--green">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              Không có xung đột khuyến mãi.
            </div>
          </div>
        </div>
      </template>
      <div v-else-if="!dashLoading" class="pp-empty pp-mt">Nhấn "Làm mới" để tải dữ liệu.</div>
    </div>

    <!-- ════════════════════════════════════════
         DIALOG: Sửa giá (dùng chung)
    ═════════════════════════════════════════ -->
    <div v-if="dlg.open" class="pp-overlay" @click.self="dlg.open = false">
      <div class="pp-dialog">
        <!-- Decorative band -->
        <div class="pp-dialog__band" :class="dlg.isQuickFix ? 'pp-dialog__band--orange' : 'pp-dialog__band--blue'">
          <div class="pp-dialog__band-circles">
            <span class="pp-band-circle pp-band-circle--1"/>
            <span class="pp-band-circle pp-band-circle--2"/>
            <span class="pp-band-circle pp-band-circle--3"/>
          </div>
          <div class="pp-dialog__icon-wrap">
            <div class="pp-dialog__icon-ring">
              <svg v-if="dlg.isQuickFix" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              <svg v-else width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
          </div>
        </div>

        <!-- Body -->
        <div class="pp-dialog__body">
          <div class="pp-dialog__badge" :class="dlg.isQuickFix ? 'pp-dialog__badge--orange' : 'pp-dialog__badge--blue'">
            {{ dlg.isQuickFix ? 'Sửa giá nhanh' : 'Cập nhật bản ghi' }}
          </div>
          <h2 class="pp-dialog__title">
            {{ dlg.isQuickFix ? 'Sửa giá nhanh' : 'Sửa bản ghi giá' }}
            <span class="pp-dialog__id pp-mono">#{{ dlg.isQuickFix ? dlg.variantId : dlg.id }}</span>
          </h2>

          <div v-if="dlg.isQuickFix" class="pp-dialog__notice pp-dialog__notice--orange">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/>
              <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            Giá bán hiện tại <strong>{{ fmtMoney(dlg.currentPrice) }}</strong> thấp hơn giá nhập <strong>{{ fmtMoney(dlg.currentCostPrice) }}</strong>
          </div>

          <div v-if="dlg.alert" class="pp-dialog__notice pp-dialog__notice--red">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {{ dlg.alert }}
          </div>

          <div class="pp-row2">
            <div class="pp-field">
              <label class="pp-field__label">Currency</label>
              <div class="pp-input pp-input--readonly">{{ dlg.form.currencyCode || 'VND' }}</div>
            </div>
            <div class="pp-field">
              <label class="pp-field__label">Giá bán mới</label>
              <input v-model.number="dlg.form.price" class="pp-input pp-input--money" type="number" />
              <span v-if="dlg.form.price" class="pp-field__hint">{{ fmtMoney(dlg.form.price) }}</span>
              <span v-if="dlg.form.price && dlg.form.costPrice && dlg.form.price < dlg.form.costPrice" class="pp-field__hint pp-text--red">⚠️ Vẫn thấp hơn giá nhập!</span>
            </div>
          </div>
          <div class="pp-row2">
            <div class="pp-field">
              <label class="pp-field__label">Giá nhập</label>
              <input v-model.number="dlg.form.costPrice" class="pp-input pp-input--money" type="number" />
              <span v-if="dlg.form.costPrice" class="pp-field__hint">{{ fmtMoney(dlg.form.costPrice) }}</span>
            </div>
            <div class="pp-field">
              <label class="pp-field__label">Lý do</label>
              <input v-model="dlg.form.reason" class="pp-input" />
            </div>
          </div>

          <div class="pp-dialog__actions">
            <button class="pp-btn pp-btn--ghost" @click="dlg.open = false">Hủy bỏ</button>
            <button
              class="pp-btn"
              :class="[dlg.isQuickFix ? 'pp-btn--confirm-orange' : 'pp-btn--confirm-blue', {'pp-btn--loading': dlg.loading}]"
              @click="dlg.isQuickFix ? saveQuickFix() : saveEdit()"
              :disabled="dlg.loading"
            >
              <span v-if="dlg.loading" class="pp-spinner pp-spinner--white"></span>
              <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                <polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>
              </svg>
              Lưu thay đổi
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { pricesApi } from "../../api/prices.api";
import { settingsApi } from "../../api/settings.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";
import "../../assets/styles/pricing-manager.css";

const activeTab = ref("set");
const tabs = [
  { key: "set", icon: "💲", label: "Đặt giá" },
  { key: "history", icon: "📋", label: "Lịch sử giá" },
  { key: "warning", icon: "🔔", label: "Cảnh báo giá" },
  { key: "calculate", icon: "🧮", label: "Tính giá đơn" },
  { key: "dashboard", icon: "📊", label: "Dashboard" },
];

// ─── DEFAULT CURRENCY (từ settings) ───
const defaultCurrency = ref("VND");

async function loadDefaultCurrency() {
  try {
    const r = await settingsApi.getDefaultCurrency();
    const raw = r?.data ?? null;
    const c =
      raw?.data?.currencyCode ??
      raw?.currencyCode ??
      raw?.data?.data?.currencyCode ??
      null;
    if (c) {
      defaultCurrency.value = String(c);
      setForm.currencyCode = String(c);
    }
  } catch {
    // Giữ nguyên "VND" mặc định nếu load thất bại
  }
}

// ─── ĐẶT GIÁ ───
const setLoading = ref(false);
const currentPricePreview = ref(null);
const setForm = reactive({
  variantId: null,
  currencyCode: "VND",
  price: 0,
  costPrice: 0,
  reason: "",
});

async function loadCurrentPricePreview() {
  if (!setForm.variantId) return;
  try {
    const r = await pricesApi.getEffective(setForm.variantId);
    const d = r?.data?.data ?? r?.data ?? null;
    // Lấy thêm costPrice từ history
    const h = await pricesApi.getVariantHistory(setForm.variantId);
    const hist =
      h?.data?.data?.content ?? h?.data?.content ?? h?.data?.data ?? [];
    const active = hist.find((x) => !x.effectiveTo);
    currentPricePreview.value = d
      ? {
          price: d.price,
          costPrice: active?.costPrice ?? d.costPrice ?? null,
          belowCost: active?.belowCost ?? false,
        }
      : null;
  } catch {
    currentPricePreview.value = null;
  }
}

async function setPrice() {
  if (!setForm.variantId) return toast("Vui lòng nhập Variant ID.", "warning");
  setLoading.value = true;
  try {
    await pricesApi.setVariantPrice(setForm.variantId, {
      currencyCode: setForm.currencyCode,
      price: setForm.price,
      costPrice: setForm.costPrice,
      reason: setForm.reason,
    });
    toast("Đã lưu giá.", "success");
    currentPricePreview.value = null;
    await loadDashboard(); // auto-refresh dashboard
  } catch {
    toast("Lưu giá thất bại.", "error");
  } finally {
    setLoading.value = false;
  }
}

// ─── GIÁ HIỆU LỰC ───
const effLoading = ref(false);
const effective = ref(null);
async function loadEffective() {
  if (!setForm.variantId) return toast("Vui lòng nhập Variant ID.", "warning");
  effLoading.value = true;
  try {
    const r = await pricesApi.getEffective(setForm.variantId);
    effective.value = r?.data?.data ?? r?.data ?? null;
  } catch {
    effective.value = null;
    toast("Tải thất bại.", "error");
  } finally {
    effLoading.value = false;
  }
}

const effCustLoading = ref(false);
const effectiveForCustomer = ref(null);
const customerIdForPrice = ref(null);
async function loadEffectiveForCustomer() {
  if (!setForm.variantId) return toast("Nhập Variant ID.", "warning");
  if (!customerIdForPrice.value) return toast("Nhập Customer ID.", "warning");
  effCustLoading.value = true;
  try {
    const r = await pricesApi.getEffectiveForCustomer(
      setForm.variantId,
      customerIdForPrice.value,
    );
    effectiveForCustomer.value = r?.data?.data ?? r?.data ?? null;
  } catch {
    effectiveForCustomer.value = null;
    toast("Tải thất bại.", "error");
  } finally {
    effCustLoading.value = false;
  }
}

// ─── GIÁ THEO SẢN PHẨM ───
const prodLoading = ref(false);
const productId = ref(null);
const productPrices = ref([]);
async function loadByProduct() {
  if (!productId.value) return toast("Nhập Product ID.", "warning");
  prodLoading.value = true;
  try {
    const r = await pricesApi.listByProduct(productId.value);
    productPrices.value = r?.data?.data ?? r?.data ?? [];
  } catch {
    productPrices.value = [];
    toast("Tải thất bại.", "error");
  } finally {
    prodLoading.value = false;
  }
}

// ─── LỊCH SỬ GIÁ (có phân trang) ───
const histLoading = ref(false);
const histLoaded = ref(false);
const histVariantId = ref(null);
const variantHistory = ref([]);
const histPage = ref(0);
const histTotalPages = ref(1);
const HIST_PAGE_SIZE = 20;

async function loadVariantHistory(page = 0) {
  if (!histVariantId.value) return toast("Nhập Variant ID.", "warning");
  histLoading.value = true;
  histLoaded.value = false;
  try {
    const r = await pricesApi.getVariantHistory(
      histVariantId.value,
      page,
      HIST_PAGE_SIZE,
    );
    const paged = r?.data?.data ?? r?.data ?? {};
    variantHistory.value = paged.content ?? paged ?? [];
    histTotalPages.value = paged.totalPages ?? 1;
    histPage.value = page;
    histLoaded.value = true;
  } catch {
    variantHistory.value = [];
    toast("Tải thất bại.", "error");
  } finally {
    histLoading.value = false;
  }
}

function changeHistPage(p) {
  loadVariantHistory(p);
}

// ─── EDIT DIALOG ───
const dlg = reactive({
  open: false,
  loading: false,
  alert: "",
  id: null,
  variantId: null,
  isQuickFix: false,
  currentPrice: null,
  currentCostPrice: null,
  form: { currencyCode: "VND", price: 0, costPrice: 0, reason: "" },
});

function openEdit(row) {
  dlg.open = true;
  dlg.alert = "";
  dlg.isQuickFix = false;
  dlg.id = row.id;
  dlg.form = {
    currencyCode: defaultCurrency.value || row.currencyCode || "VND",
    price: Number(row.price || 0),
    costPrice: Number(row.costPrice || 0),
    reason: row.reason || "",
  };
}

function openQuickFix(row) {
  dlg.open = true;
  dlg.alert = "";
  dlg.isQuickFix = true;
  dlg.variantId = row.variantId;
  dlg.currentPrice = row.price;
  dlg.currentCostPrice = row.costPrice;
  dlg.form = {
    currencyCode: defaultCurrency.value || "VND",
    price: Number(row.price || 0),
    costPrice: Number(row.costPrice || 0),
    reason: "PRICE_UPDATE",
  };
}

async function saveEdit() {
  dlg.loading = true;
  dlg.alert = "";
  try {
    await pricesApi.updateHistory(dlg.id, dlg.form);
    toast("Đã cập nhật.", "success");
    dlg.open = false;
    await loadVariantHistory(histPage.value);
    await loadDashboard();
  } catch (e) {
    dlg.alert = e?.response?.data?.message || e?.message || "Cập nhật thất bại";
  } finally {
    dlg.loading = false;
  }
}

async function saveQuickFix() {
  dlg.loading = true;
  dlg.alert = "";
  try {
    await pricesApi.setVariantPrice(dlg.variantId, dlg.form);
    toast("Đã cập nhật giá.", "success");
    dlg.open = false;
    await loadConflicts(); // refresh bảng xung đột
    await loadDashboard(); // refresh dashboard
  } catch (e) {
    dlg.alert = e?.response?.data?.message || e?.message || "Cập nhật thất bại";
  } finally {
    dlg.loading = false;
  }
}

async function removeHistory(row) {
  const ok = await confirmModal(
    `Xóa bản ghi #${row.id}?`,
    "Xác nhận",
    "Xóa",
    true,
  );
  if (!ok) return;
  try {
    await pricesApi.removeHistory(row.id);
    toast("Đã xóa.", "success");
    await loadVariantHistory(histPage.value);
    await loadDashboard();
  } catch {
    toast("Xóa thất bại.", "error");
  }
}

// ─── CẢNH BÁO + XUNG ĐỘT (gộp chung) ───
const costWarnLoading = ref(false);
const costWarnVariantId = ref(null);
const costWarning = ref(null);
async function loadCostWarning() {
  if (!costWarnVariantId.value) return toast("Nhập Variant ID.", "warning");
  costWarnLoading.value = true;
  try {
    const r = await pricesApi.getCostWarning(costWarnVariantId.value);
    costWarning.value = r?.data?.data ?? r?.data ?? null;
  } catch {
    costWarning.value = null;
    toast("Tải thất bại.", "error");
  } finally {
    costWarnLoading.value = false;
  }
}

const conflictsLoading = ref(false);
const conflictsLoaded = ref(false);
const priceConflicts = ref([]);
async function loadConflicts() {
  conflictsLoading.value = true;
  try {
    const r = await pricesApi.getConflicts();
    priceConflicts.value = r?.data?.data ?? r?.data ?? [];
    conflictsLoaded.value = true;
  } catch {
    priceConflicts.value = [];
    toast("Tải xung đột thất bại.", "error");
  } finally {
    conflictsLoading.value = false;
  }
}

// ─── TÍNH GIÁ ĐƠN ───
const calcLoading = ref(false);
const calcResult = ref(null);
const calcForm = reactive({
  customerId: null,
  items: [{ variantId: null, quantity: 1 }],
});
async function calculateOrder() {
  const items = calcForm.items.filter((i) => i.variantId);
  if (!items.length) return toast("Thêm ít nhất 1 sản phẩm.", "warning");
  calcLoading.value = true;
  try {
    const r = await pricesApi.calculateOrder(
      calcForm.customerId || null,
      items,
    );
    calcResult.value = r?.data?.data ?? r?.data ?? null;
  } catch (e) {
    toast(e?.response?.data?.message || "Tính giá thất bại.", "error");
  } finally {
    calcLoading.value = false;
  }
}

// ─── DASHBOARD ───
const dashLoading = ref(false);
const dashData = ref(null);
async function loadDashboard() {
  dashLoading.value = true;
  try {
    const r = await pricesApi.getDashboard();
    dashData.value = r?.data?.data ?? r?.data ?? null;
  } catch {
    toast("Tải dashboard thất bại.", "error");
  } finally {
    dashLoading.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadDashboard(), loadDefaultCurrency()]);
});

// ─── UTILS ───
function fmtMoney(val) {
  if (val == null) return "—";
  return Number(val).toLocaleString("vi-VN") + " ₫";
}
function fmtDate(iso) {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleString("vi-VN");
  } catch {
    return iso;
  }
}

// ─── VARIANT PICKER ───
const variantPickerProductId = ref(null);
const variantPickerLoading = ref(false);
const variantList = ref([]);
const variantPickerOpen = ref(false);

async function loadVariantsForPicker() {
  if (!variantPickerProductId.value)
    return toast("Nhập Product ID.", "warning");
  variantPickerLoading.value = true;
  variantList.value = [];
  try {
    const r = await pricesApi.getVariantsByProduct(
      variantPickerProductId.value,
    );
    variantList.value = r?.data ?? [];
    variantPickerOpen.value = true;
  } catch {
    toast("Không tải được danh sách variant.", "error");
  } finally {
    variantPickerLoading.value = false;
  }
}

function selectVariant(v) {
  setForm.variantId = v.id;
  variantPickerOpen.value = false;
  loadCurrentPricePreview();
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.pp-page {
  --c-bg:            #f6f7f9;
  --c-card:          #ffffff;
  --c-border:        #e4e7ec;
  --c-border-light:  #f0f2f5;
  --c-text:          #0f1117;
  --c-muted:         #6b7280;
  --c-subtle:        #9ca3af;
  --c-blue:          #2563eb;
  --c-blue-bg:       #eff6ff;
  --c-blue-border:   #bfdbfe;
  --c-green:         #16a34a;
  --c-green-bg:      #f0fdf4;
  --c-green-border:  #bbf7d0;
  --c-red:           #dc2626;
  --c-red-bg:        #fff1f2;
  --c-red-border:    #fecdd3;
  --c-orange:        #d97706;
  --c-orange-bg:     #fffbeb;
  --c-orange-border: #fde68a;
  --c-purple:        #7c3aed;
  --c-purple-bg:     #f5f3ff;
  --c-purple-border: #ddd6fe;
  --radius:          12px;
  --radius-sm:       8px;
  --shadow-sm:       0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-md:       0 4px 16px rgba(0,0,0,0.08), 0 2px 6px rgba(0,0,0,0.04);
  --shadow-lg:       0 24px 64px rgba(0,0,0,0.14), 0 8px 24px rgba(0,0,0,0.06);

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--c-bg);
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: var(--c-text);
  box-sizing: border-box;
}

/* ── Header ──────────────────────────────── */
.pp-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}
.pp-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-muted);
  margin-bottom: 6px;
}
.pp-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
  line-height: 1.2;
}
.pp-title__accent {
  color: var(--c-blue);
}
.pp-subtitle {
  font-size: 13.5px;
  color: var(--c-muted);
  margin: 0;
}
.pp-header__right {
  flex-shrink: 0;
  padding-top: 4px;
}

/* Summary pills */
.pp-summary {
  display: flex;
  gap: 6px;
}
.pp-summary__item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 20px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  font-size: 12px;
  transition: border-color 0.15s;
}
.pp-summary__item--red   { border-color: var(--c-red-border); background: var(--c-red-bg); }
.pp-summary__item--orange { border-color: var(--c-orange-border); background: var(--c-orange-bg); }
.pp-summary__dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.pp-summary__count {
  font-size: 13px;
  font-weight: 800;
}
.pp-summary__label {
  color: var(--c-muted);
  font-weight: 600;
}

/* ── Tab bar ─────────────────────────────── */
.pp-tabbar {
  display: flex;
  gap: 2px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 5px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
  overflow-x: auto;
}
.pp-tab {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--c-muted);
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
  font-family: "Plus Jakarta Sans", sans-serif;
}
.pp-tab:hover {
  background: var(--c-border-light);
  color: var(--c-text);
}
.pp-tab--active {
  background: var(--c-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,0.25);
}
.pp-tab__icon { font-size: 14px; }

/* ── Pane ────────────────────────────────── */
.pp-pane { animation: pp-fadein 0.18s ease; }
@keyframes pp-fadein { from { opacity:0; transform:translateY(6px); } to { opacity:1; transform:none; } }

/* ── Grid ────────────────────────────────── */
.pp-grid2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
@media (max-width: 960px) {
  .pp-grid2 { grid-template-columns: 1fr; }
}

/* ── Card ────────────────────────────────── */
.pp-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.pp-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
  flex-wrap: wrap;
}
.pp-card__title {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--c-text);
}
.pp-card__body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
}
.pp-card__foot {
  padding: 14px 20px;
  border-top: 1px solid var(--c-border-light);
  background: #fafbfc;
  display: flex;
  justify-content: flex-end;
}

/* Sub-head inside card body */
.pp-sub-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--c-border-light);
}
.pp-sub-title {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--c-text);
}

/* ── Tags ────────────────────────────────── */
.pp-tag {
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
.pp-tag--green  { background: var(--c-green-bg); color: var(--c-green); border-color: var(--c-green-border); }
.pp-tag--red    { background: var(--c-red-bg); color: var(--c-red); border-color: var(--c-red-border); }
.pp-tag--orange { background: var(--c-orange-bg); color: var(--c-orange); border-color: var(--c-orange-border); }
.pp-tag--blue   { background: var(--c-blue-bg); color: var(--c-blue); border-color: var(--c-blue-border); }

/* ── Fields ──────────────────────────────── */
.pp-field {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.pp-field__label {
  font-size: 11px;
  font-weight: 700;
  color: var(--c-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.pp-field__hint {
  font-size: 11.5px;
  color: var(--c-muted);
  font-weight: 500;
}
.pp-required { color: var(--c-red); margin-left: 2px; }

.pp-row2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

/* ── Inputs ──────────────────────────────── */
.pp-input {
  padding: 9px 12px;
  font-size: 13.5px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-card);
  color: var(--c-text);
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
  width: 100%;
  box-sizing: border-box;
}
.pp-input:focus {
  border-color: var(--c-blue);
  box-shadow: 0 0 0 3px rgba(37,99,235,0.08);
}
.pp-input::placeholder { color: var(--c-subtle); }
.pp-input--readonly {
  background: var(--c-border-light);
  color: var(--c-muted);
  cursor: default;
  pointer-events: none;
}
.pp-input--money {
  font-family: "JetBrains Mono", monospace;
  font-size: 13px;
  letter-spacing: 0.02em;
}

/* ── Picker bar ──────────────────────────── */
.pp-picker-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.pp-selected-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 700;
  color: var(--c-green);
  background: var(--c-green-bg);
  border: 1px solid var(--c-green-border);
  padding: 4px 10px;
  border-radius: 20px;
}

/* Variant dropdown */
.pp-variant-dropdown {
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-top: 4px;
  background: var(--c-card);
  box-shadow: var(--shadow-md);
}
.pp-variant-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--c-border-light);
  transition: background 0.1s;
  gap: 12px;
}
.pp-variant-option:last-child { border-bottom: none; }
.pp-variant-option:hover { background: var(--c-border-light); }
.pp-variant-option--selected { background: var(--c-blue-bg); }
.pp-variant-option--inactive { opacity: 0.55; }
.pp-variant-option__main { display: flex; flex-direction: column; gap: 2px; }
.pp-variant-option__name { font-size: 13px; font-weight: 600; }
.pp-variant-option__sku { font-size: 11.5px; color: var(--c-subtle); font-family: "JetBrains Mono", monospace; }
.pp-variant-option__meta { display: flex; align-items: center; gap: 10px; font-size: 12.5px; font-weight: 600; flex-shrink: 0; }
.pp-variant-option__stock { font-size: 11.5px; color: var(--c-muted); }

/* ── Price preview ───────────────────────── */
.pp-price-preview {
  display: flex;
  align-items: stretch;
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.pp-price-preview__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 16px;
  flex: 1;
}
.pp-price-preview__label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--c-subtle);
}
.pp-price-preview__value {
  font-size: 14px;
  font-weight: 700;
  font-family: "JetBrains Mono", monospace;
}
.pp-price-preview__warn {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-orange);
}
.pp-price-preview__divider {
  width: 1px;
  background: var(--c-border);
  flex-shrink: 0;
}

/* ── Result box ──────────────────────────── */
.pp-result {
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-top: 12px;
}
.pp-result__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 14px;
  border-bottom: 1px solid var(--c-border-light);
  gap: 12px;
}
.pp-result__row:last-child { border-bottom: none; }
.pp-result__row--hl { background: rgba(37,99,235,0.04); }
.pp-result__key {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--c-muted);
  flex-shrink: 0;
}
.pp-result__val {
  font-size: 13px;
  font-weight: 600;
  text-align: right;
}

/* ── Buttons ─────────────────────────────── */
.pp-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  font-size: 13.5px;
  font-weight: 700;
  border-radius: var(--radius-sm);
  border: none;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
  white-space: nowrap;
  text-decoration: none;
}
.pp-btn--primary {
  background: var(--c-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,0.22);
}
.pp-btn--primary:hover { background: #1d4ed8; transform: translateY(-1px); box-shadow: 0 4px 14px rgba(37,99,235,0.3); }
.pp-btn--outline {
  background: var(--c-card);
  color: var(--c-text);
  border: 1.5px solid var(--c-border);
}
.pp-btn--outline:hover { border-color: var(--c-subtle); background: #f9fafb; }
.pp-btn--ghost {
  background: transparent;
  color: var(--c-muted);
  border: 1.5px solid var(--c-border);
}
.pp-btn--ghost:hover { background: var(--c-border-light); color: var(--c-text); }
.pp-btn--orange {
  background: var(--c-orange);
  color: #fff;
  box-shadow: 0 2px 8px rgba(217,119,6,0.22);
}
.pp-btn--orange:hover { background: #b45309; transform: translateY(-1px); }
.pp-btn--loading, .pp-btn:disabled { opacity: 0.65; pointer-events: none; transform: none !important; }
.pp-btn--confirm-blue {
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #fff;
  box-shadow: 0 4px 14px rgba(37,99,235,0.35);
  padding: 11px 22px;
}
.pp-btn--confirm-blue:hover { background: linear-gradient(135deg, #1e3a8a, #1d4ed8); transform: translateY(-1px); }
.pp-btn--confirm-orange {
  background: linear-gradient(135deg, #b45309, #d97706);
  color: #fff;
  box-shadow: 0 4px 14px rgba(217,119,6,0.35);
  padding: 11px 22px;
}
.pp-btn--confirm-orange:hover { background: linear-gradient(135deg, #92400e, #b45309); transform: translateY(-1px); }
.pp-ml-auto { margin-left: auto; }

/* Icon btn */
.pp-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border: none;
  background: var(--c-border-light);
  border-radius: 6px;
  cursor: pointer;
  color: var(--c-muted);
  flex-shrink: 0;
  transition: all 0.15s;
}
.pp-icon-btn:hover { background: var(--c-red-bg); color: var(--c-red); }

/* ── Table ───────────────────────────────── */
.pp-table-wrap {
  overflow-x: auto;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
}
.pp-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.pp-table thead tr { border-bottom: 2px solid var(--c-border-light); }
.pp-table th {
  padding: 11px 14px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--c-subtle);
  background: #fafbfc;
  white-space: nowrap;
}
.pp-table td {
  padding: 12px 14px;
  vertical-align: middle;
  border-bottom: 1px solid var(--c-border-light);
}
.pp-row { transition: background 0.12s; }
.pp-row:hover { background: #fafbfc; }
.pp-row:last-child td { border-bottom: none; }
.pp-row--danger { background: #fff8f8 !important; }
.pp-row--danger:hover { background: #fff0f0 !important; }
.pp-table--sm td, .pp-table--sm th { padding: 9px 12px; }

/* Mono ID chip */
.pp-mono-id {
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-blue);
  background: var(--c-blue-bg);
  padding: 2px 7px;
  border-radius: 5px;
}

/* ── Action buttons ──────────────────────── */
.pp-actions {
  display: flex;
  gap: 6px;
  justify-content: center;
}
.pp-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 11px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 6px;
  border: 1.5px solid transparent;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: "Plus Jakarta Sans", sans-serif;
  white-space: nowrap;
}
.pp-action-btn--edit {
  background: var(--c-blue-bg);
  color: var(--c-blue);
  border-color: var(--c-blue-border);
}
.pp-action-btn--edit:hover {
  background: var(--c-blue);
  color: #fff;
  border-color: var(--c-blue);
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(37,99,235,0.2);
}
.pp-action-btn--delete {
  background: var(--c-red-bg);
  color: var(--c-red);
  border-color: var(--c-red-border);
}
.pp-action-btn--delete:hover {
  background: var(--c-red);
  color: #fff;
  border-color: var(--c-red);
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(220,38,38,0.2);
}

/* ── Alerts ──────────────────────────────── */
.pp-alert {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 11px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid transparent;
}
.pp-alert--green { background: var(--c-green-bg); color: var(--c-green); border-color: var(--c-green-border); }
.pp-alert--red   { background: var(--c-red-bg); color: var(--c-red); border-color: var(--c-red-border); }

/* ── Calc box ────────────────────────────── */
.pp-calc-box {
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.pp-calc-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  font-size: 13.5px;
  border-bottom: 1px solid var(--c-border-light);
  gap: 12px;
}
.pp-calc-row:last-child { border-bottom: none; }
.pp-calc-row--disc { color: var(--c-muted); }
.pp-calc-row--total {
  font-weight: 800;
  font-size: 15px;
  background: rgba(37,99,235,0.04);
}
.pp-calc-sep {
  height: 1px;
  background: var(--c-border);
  margin: 0;
}

/* ── Stats ───────────────────────────────── */
.pp-stats4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 960px) { .pp-stats4 { grid-template-columns: 1fr 1fr; } }
.pp-stat {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.15s, box-shadow 0.15s;
}
.pp-stat:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.pp-stat__icon { font-size: 22px; }
.pp-stat__num  { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; line-height: 1; }
.pp-stat__label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--c-muted);
}
.pp-stat--green  { border-top: 3px solid var(--c-green); }
.pp-stat--green .pp-stat__num  { color: var(--c-green); }
.pp-stat--red    { border-top: 3px solid var(--c-red); }
.pp-stat--red .pp-stat__num    { color: var(--c-red); }
.pp-stat--orange { border-top: 3px solid var(--c-orange); }
.pp-stat--orange .pp-stat__num { color: var(--c-orange); }
.pp-stat--purple { border-top: 3px solid var(--c-purple); }
.pp-stat--purple .pp-stat__num { color: var(--c-purple); }
.pp-stat--dim    { border-top: 3px solid var(--c-border); opacity: 0.7; }

/* ── Pagination ──────────────────────────── */
.pp-pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--c-border-light);
  background: #fafbfc;
}
.pp-pagination__info {
  font-size: 12.5px;
  color: var(--c-muted);
  font-weight: 600;
  margin: auto;
}

/* ── Inline & Toolbar ────────────────────── */
.pp-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.pp-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* ── Items (cart) ────────────────────────── */
.pp-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pp-item-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pp-item-num {
  width: 22px; height: 22px;
  background: var(--c-border-light);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: var(--c-muted);
  flex-shrink: 0;
}
.pp-item-sep {
  font-size: 14px;
  font-weight: 700;
  color: var(--c-subtle);
}

/* ── Hint text ───────────────────────────── */
.pp-hint-text {
  font-size: 12.5px;
  color: var(--c-muted);
  margin: 0;
}

/* ── Divider ─────────────────────────────── */
.pp-divider {
  height: 1px;
  background: var(--c-border-light);
}

/* ── Empty ───────────────────────────────── */
.pp-empty {
  text-align: center;
  padding: 40px 20px;
  color: var(--c-muted);
  font-size: 13.5px;
}

/* ── Utility ─────────────────────────────── */
.pp-mono  { font-family: "JetBrains Mono", monospace; }
.pp-bold  { font-weight: 700; }
.pp-small { font-size: 12px; }
.pp-text--red    { color: var(--c-red); }
.pp-text--green  { color: var(--c-green); }
.pp-text--muted  { color: var(--c-muted); }
.pp-text--normal { font-weight: 400; }
.pp-mb  { margin-bottom: 20px; }
.pp-mt  { margin-top: 14px; }

/* ── Spinner ─────────────────────────────── */
.pp-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.12);
  border-top-color: var(--c-muted);
  border-radius: 50%;
  animation: pp-spin 0.6s linear infinite;
  flex-shrink: 0;
}
.pp-spinner--white {
  border-color: rgba(255,255,255,0.25);
  border-top-color: #fff;
}
@keyframes pp-spin { to { transform: rotate(360deg); } }

/* ══════════════════════════════════════════
   DIALOG
══════════════════════════════════════════ */
.pp-overlay {
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
.pp-dialog {
  background: #ffffff;
  border-radius: 20px;
  width: 100%;
  max-width: 460px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

/* Band */
.pp-dialog__band {
  position: relative;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.pp-dialog__band--blue   { background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 60%, #60a5fa 100%); }
.pp-dialog__band--orange { background: linear-gradient(135deg, #b45309 0%, #d97706 60%, #fbbf24 100%); }

.pp-dialog__band-circles { position: absolute; inset: 0; pointer-events: none; }
.pp-band-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
}
.pp-band-circle--1 { width:140px; height:140px; top:-40px; right:-30px; }
.pp-band-circle--2 { width:80px;  height:80px;  bottom:-20px; left:20px; }
.pp-band-circle--3 { width:50px;  height:50px;  top:10px; left:50%; margin-left:-25px; background: rgba(255,255,255,0.07); }

.pp-dialog__icon-wrap { position: relative; z-index: 1; }
.pp-dialog__icon-ring {
  width: 64px; height: 64px;
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
.pp-dialog__body {
  padding: 26px 30px 28px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.pp-dialog__badge {
  display: inline-flex;
  align-self: flex-start;
  font-size: 10.5px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  padding: 4px 10px;
  border-radius: 20px;
}
.pp-dialog__badge--blue   { background: var(--c-blue-bg);   color: var(--c-blue);   border: 1px solid var(--c-blue-border); }
.pp-dialog__badge--orange { background: var(--c-orange-bg); color: var(--c-orange); border: 1px solid var(--c-orange-border); }

.pp-dialog__title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.pp-dialog__id {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-muted);
  background: var(--c-border-light);
  padding: 2px 8px;
  border-radius: 6px;
}

/* Notice */
.pp-dialog__notice {
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
.pp-dialog__notice--orange { background: var(--c-orange-bg); color: #92400e; border-color: var(--c-orange-border); }
.pp-dialog__notice--red    { background: var(--c-red-bg);    color: var(--c-red);   border-color: var(--c-red-border); }

/* Actions */
.pp-dialog__actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 4px;
}
</style>