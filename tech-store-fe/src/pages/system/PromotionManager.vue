<template>
  <div class="prm">
    <!-- Header -->
    <header class="prm-header">
      <div>
        <div class="prm-breadcrumb">SYSTEM / PROMOTIONS</div>
        <h1 class="prm-title">
          Quản lý <span class="prm-title-accent">khuyến mãi</span>
        </h1>
      </div>
      <div class="prm-header-actions">
        <button
          class="prm-btn prm-btn-ghost"
          :class="{ loading: conflictLoading }"
          @click="loadConflicts"
          :disabled="conflictLoading"
        >
          <span v-if="!conflictLoading">⚠️ Conflicts</span>
          <span v-else class="prm-spin"></span>
        </button>
        <button
          class="prm-btn prm-btn-ghost"
          :class="{ loading: expiringLoading }"
          @click="loadExpiring"
          :disabled="expiringLoading"
        >
          <span v-if="!expiringLoading">⏰ Sắp hết hạn</span>
          <span v-else class="prm-spin"></span>
        </button>
        <button
          class="prm-btn prm-btn-ghost"
          :class="{ loading: activeReportLoading }"
          @click="loadActiveReport"
          :disabled="activeReportLoading"
        >
          <span v-if="!activeReportLoading">📋 KM Active</span>
          <span v-else class="prm-spin"></span>
        </button>
        <button
          class="prm-btn prm-btn-ghost"
          :class="{ loading: reportLoading || summaryReportLoading }"
          @click="loadCombinedReport"
          :disabled="reportLoading || summaryReportLoading"
        >
          <span v-if="!reportLoading && !summaryReportLoading">📊 Báo cáo</span>
          <span v-else class="prm-spin"></span>
        </button>
        <button class="prm-btn prm-btn-primary" @click="openCreate">
          + Tạo mới
        </button>
      </div>
    </header>

    <!-- Quick Navigator -->
    <QuickNav page="promotion" :onTriggerAction="handleQuickNav" />

    <!-- Validate bar -->
    <div id="prm-validate-bar" class="prm-validate-bar">
      <div class="prm-validate-label">🔍 Validate mã KM</div>
      <div class="prm-validate-inputs">
        <div class="prm-vfield">
          <span class="prm-vfield-pre">CODE</span>
          <input
            v-model="validateCode"
            class="prm-vin"
            placeholder="VD: GAMING10"
          />
        </div>
        <div class="prm-vfield">
          <span class="prm-vfield-pre">₫</span>
          <input
            v-model.number="validateTotal"
            class="prm-vin prm-vin-num"
            placeholder="Tổng đơn"
            type="number"
          />
        </div>
        <button
          class="prm-btn prm-btn-outline"
          :class="{ loading: validateLoading }"
          @click="doValidate"
          :disabled="validateLoading"
        >
          <span v-if="!validateLoading">Kiểm tra</span
          ><span v-else class="prm-spin"></span>
        </button>
        <button
          v-if="validateResult"
          class="prm-btn-icon"
          @click="validateResult = null"
        >
          ✕
        </button>
      </div>
      <transition name="fade">
        <div
          v-if="validateResult"
          class="prm-validate-result"
          :class="validateResult.valid ? 'vr-ok' : 'vr-fail'"
        >
          <span v-if="validateResult.valid"
            >✅ Hợp lệ — giảm
            <strong>{{ fmtMoney(validateResult.discountAmount) }}</strong>
            <span
              v-if="validateResult.promotionName"
              class="prm-tag prm-tag-y"
              style="margin-left: 6px"
              >{{ validateResult.promotionName }}</span
            >
          </span>
          <span v-else
            >❌ {{ validateResult.message || "Mã không hợp lệ" }}</span
          >
        </div>
      </transition>
    </div>

    <!-- Conflicts panel -->
    <transition name="slide">
      <div v-if="conflicts !== null" class="prm-panel prm-panel-conflict">
        <div class="prm-panel-head">
          <span>⚠️ Xung đột khuyến mãi</span>
          <span v-if="conflicts.length" class="prm-tag prm-tag-r">{{
            conflicts.length
          }}</span>
          <button class="prm-btn-icon ml-auto" @click="conflicts = null">
            ✕
          </button>
        </div>
        <div v-if="!conflicts.length" class="prm-ok-msg">
          ✅ Không có xung đột
        </div>
        <div v-else class="prm-tw mt2">
          <table class="prm-tbl">
            <thead>
              <tr>
                <th>Khuyến mãi 1</th>
                <th>Khuyến mãi 2</th>
                <th>Stackable</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(r, idx) in conflicts" :key="idx">
                <td>
                  <span class="prm-tag prm-tag-y">{{ r.promotion1 }}</span>
                </td>
                <td>
                  <span class="prm-tag prm-tag-y">{{ r.promotion2 }}</span>
                </td>
                <td class="center">
                  <span
                    class="prm-tag"
                    :class="r.stackable ? 'prm-tag-g' : 'prm-tag-r'"
                  >
                    {{ r.stackable ? "✅ Có" : "❌ Không" }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </transition>

    <!-- Expiring panel -->
    <transition name="slide">
      <div v-if="expiringRows !== null" class="prm-panel prm-panel-expire">
        <div class="prm-panel-head">
          <span>⏰ Sắp hết hạn (3 ngày)</span>
          <span
            class="prm-tag"
            :class="expiringRows.length ? 'prm-tag-o' : 'prm-tag-g'"
            >{{ expiringRows.length }}</span
          >
          <button class="prm-btn-icon ml-auto" @click="expiringRows = null">
            ✕
          </button>
        </div>
        <div v-if="!expiringRows.length" class="prm-ok-msg">
          ✅ Không có KM nào sắp hết hạn
        </div>
        <div v-else class="prm-tw mt2">
          <table class="prm-tbl">
            <thead>
              <tr>
                <th>ID</th>
                <th>Code</th>
                <th>Tên</th>
                <th>Hết hạn</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in expiringRows" :key="r.id">
                <td class="mono dim">{{ r.id }}</td>
                <td>
                  <span class="prm-code">{{ r.code }}</span>
                </td>
                <td>{{ r.name }}</td>
                <td>
                  <span class="prm-tag prm-tag-o">{{
                    fmtDate(r.endDate)
                  }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </transition>

    <!-- Active report panel -->
    <transition name="slide">
      <div v-if="activeReportData !== null" class="prm-panel">
        <div class="prm-panel-head">
          <span>📋 Khuyến mãi đang áp dụng</span>
          <span class="prm-tag prm-tag-g">{{
            Array.isArray(activeReportData) ? activeReportData.length : 0
          }}</span>
          <button class="prm-btn-icon ml-auto" @click="activeReportData = null">
            ✕
          </button>
        </div>
        <div v-if="!activeReportData?.length" class="prm-ok-msg">
          Không có KM nào đang chạy
        </div>
        <div v-else class="prm-tw mt2">
          <table class="prm-tbl">
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
              <tr v-for="r in activeReportData" :key="r.id">
                <td class="mono dim">{{ r.id }}</td>
                <td>
                  <span class="prm-code">{{ r.code }}</span>
                </td>
                <td>{{ r.name }}</td>
                <td>
                  <span class="prm-tag prm-tag-b">{{ r.discountType }}</span>
                </td>
                <td>
                  <span class="prm-tag prm-tag-o">{{
                    fmtDate(r.endDate)
                  }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </transition>

    <!-- Combined report panel -->
    <transition name="slide">
      <div
        v-if="reportData !== null || summaryReportData !== null"
        class="prm-panel prm-panel-report"
      >
        <div class="prm-panel-head">
          <span>📊 Báo cáo khuyến mãi</span>
          <div class="prm-period-tabs">
            <button
              class="prm-period-btn"
              :class="{ active: reportPeriod === 'week' }"
              @click="
                reportPeriod = 'week';
                loadCombinedReport();
              "
            >
              Tuần
            </button>
            <button
              class="prm-period-btn"
              :class="{ active: reportPeriod === 'month' }"
              @click="
                reportPeriod = 'month';
                loadCombinedReport();
              "
            >
              Tháng
            </button>
          </div>
          <button
            class="prm-btn-icon ml-auto"
            @click="
              reportData = null;
              summaryReportData = null;
            "
          >
            ✕
          </button>
        </div>

        <!-- Section 1: Tổng quan -->
        <div class="prm-report-section-label">Tổng quan</div>
        <div class="prm-report-stats prm-report-stats-main">
          <div class="prm-rstat">
            <div class="prm-rstat-n">{{ reportData?.total ?? 0 }}</div>
            <div class="prm-rstat-l">Tổng KM</div>
          </div>
          <div class="prm-rstat prm-rstat-g">
            <div class="prm-rstat-n">{{ reportData?.activeCount ?? 0 }}</div>
            <div class="prm-rstat-l">Đang chạy</div>
          </div>
          <div class="prm-rstat prm-rstat-o">
            <div class="prm-rstat-n">
              {{ reportData?.comboCount ?? summaryReportData?.comboCount ?? 0 }}
            </div>
            <div class="prm-rstat-l">Combo</div>
          </div>
          <div class="prm-rstat prm-rstat-b">
            <div class="prm-rstat-n">
              {{
                reportData?.usageLimitedCount ??
                summaryReportData?.usageLimitedCount ??
                0
              }}
            </div>
            <div class="prm-rstat-l">Giới hạn lượt</div>
          </div>
          <div class="prm-rstat prm-rstat-r">
            <div class="prm-rstat-n">
              {{
                summaryReportData?.totalRedemptions ??
                reportData?.totalRedemptions ??
                0
              }}
            </div>
            <div class="prm-rstat-l">Tổng lượt dùng</div>
          </div>
        </div>

        <!-- Section 2: Tỉ lệ -->
        <div class="prm-report-section-label" style="margin-top: 16px">
          Tỉ lệ hoạt động
        </div>
        <div class="prm-report-ratios">
          <div class="prm-ratio-item">
            <div class="prm-ratio-label">
              <span>Đang chạy</span>
              <span class="prm-ratio-val clr-green">
                {{
                  reportData?.total
                    ? Math.round(
                        (reportData.activeCount / reportData.total) * 100,
                      )
                    : 0
                }}%
              </span>
            </div>
            <div class="prm-ratio-bar">
              <div
                class="prm-ratio-fill prm-ratio-fill-g"
                :style="{
                  width: reportData?.total
                    ? (reportData.activeCount / reportData.total) * 100 + '%'
                    : '0%',
                }"
              ></div>
            </div>
          </div>
          <div class="prm-ratio-item">
            <div class="prm-ratio-label">
              <span>Combo</span>
              <span class="prm-ratio-val" style="color: var(--orange-text)">
                {{
                  reportData?.total
                    ? Math.round(
                        ((reportData?.comboCount ?? 0) / reportData.total) *
                          100,
                      )
                    : 0
                }}%
              </span>
            </div>
            <div class="prm-ratio-bar">
              <div
                class="prm-ratio-fill prm-ratio-fill-o"
                :style="{
                  width: reportData?.total
                    ? ((reportData?.comboCount ?? 0) / reportData.total) * 100 +
                      '%'
                    : '0%',
                }"
              ></div>
            </div>
          </div>
          <div class="prm-ratio-item">
            <div class="prm-ratio-label">
              <span>Giới hạn lượt</span>
              <span class="prm-ratio-val" style="color: var(--accent)">
                {{
                  reportData?.total
                    ? Math.round(
                        ((reportData?.usageLimitedCount ?? 0) /
                          reportData.total) *
                          100,
                      )
                    : 0
                }}%
              </span>
            </div>
            <div class="prm-ratio-bar">
              <div
                class="prm-ratio-fill prm-ratio-fill-b"
                :style="{
                  width: reportData?.total
                    ? ((reportData?.usageLimitedCount ?? 0) /
                        reportData.total) *
                        100 +
                      '%'
                    : '0%',
                }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- Main table -->
    <div id="prm-main-table" class="prm-table-card">
      <div class="prm-table-toolbar">
        <div class="prm-toolbar-left">
          <span class="prm-table-count">
            {{ displayRows.length }}
            <span class="prm-table-count-sub"
              >/ {{ rows.length }} khuyến mãi</span
            >
          </span>
          <span
            v-if="!showInactive"
            class="prm-tag prm-tag-g"
            style="font-size: 11px"
          >
            🟢 Đang lọc: Chỉ ACTIVE
          </span>
        </div>
        <div class="prm-toolbar-right">
          <label class="prm-switch">
            <input type="checkbox" v-model="showInactive" />
            <span class="prm-switch-track"></span>
            <span
              class="prm-switch-lbl"
              :style="!showInactive ? 'color:#6b7280' : ''"
            >
              {{ showInactive ? "Hiện tất cả" : "Ẩn km ff" }}
            </span>
          </label>
        </div>
      </div>
      <div class="prm-tw">
        <table class="prm-tbl">
          <thead>
            <tr>
              <th>ID</th>
              <th>Code</th>
              <th>Tên chiến dịch</th>
              <th>Loại giảm</th>
              <th>Đối tượng</th>
              <th>Giới hạn</th>
              <th>Ưu tiên</th>
              <th>Thời gian</th>
              <th>Trạng thái</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="10" class="prm-loading-row">
                <span class="prm-spin"></span> Đang tải...
              </td>
            </tr>
            <tr v-else-if="!rows.length">
              <td colspan="10" class="prm-empty-row">Không có dữ liệu</td>
            </tr>
            <tr v-for="row in displayRows" :key="row.id">
              <td class="mono dim">{{ row.id }}</td>
              <td>
                <span class="prm-code">{{ row.code }}</span>
              </td>
              <td class="prm-name-cell">{{ row.name }}</td>
              <td>
                <template v-if="row.buyQty">
                  <span class="prm-disc clr-green"
                    >Mua {{ row.buyQty }} tặng {{ row.getQty }}</span
                  >
                  <div class="prm-disc-type">COMBO</div>
                </template>
                <template v-else>
                  <span class="prm-disc"
                    >{{ row.discountValue
                    }}{{ row.discountType === "PERCENT" ? "%" : " ₫" }}</span
                  >
                  <div class="prm-disc-type">{{ row.discountType }}</div>
                </template>
              </td>
              <td>
                <span v-if="row.vipTiers?.length" class="prm-tag prm-tag-p"
                  >VIP {{ row.vipTiers.join(", ") }}</span
                >
                <span
                  v-else-if="row.customerTypes?.length"
                  class="prm-tag prm-tag-b"
                  >{{ row.customerTypes.join(", ") }}</span
                >
                <span v-else class="dim small">All</span>
              </td>
              <td class="center">
                <span v-if="row.usageLimit" class="prm-tag prm-tag-b">{{
                  row.usageLimit
                }}</span>
                <span v-else class="dim">∞</span>
              </td>
              <td class="center mono">{{ row.priority }}</td>
              <td>
                <div class="prm-dates">
                  <span>{{ fmtDate(row.startDate) }}</span>
                  <span class="prm-dates-sep">→</span>
                  <span>{{ fmtDate(row.endDate) }}</span>
                </div>
              </td>
              <td class="center">
                <span
                  class="prm-status"
                  :class="row.isActive ? 'prm-status-on' : 'prm-status-off'"
                >
                  {{ row.isActive ? "ACTIVE" : "OFF" }}
                </span>
              </td>
              <td>
                <div class="prm-acts">
                  <button class="prm-xs prm-xs-o" @click="openEdit(row)">
                    Sửa
                  </button>
                  <button class="prm-xs prm-xs-b" @click="openPreview(row)">
                    Preview
                  </button>
                  <button class="prm-xs prm-xs-d" @click="remove(row)">
                    Xóa
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Preview panel -->
    <transition name="slide">
      <div v-if="previewPromo" class="prm-panel mt3">
        <div class="prm-panel-head">
          <span
            >Xem giá —
            <span class="prm-code">{{ previewPromo.code }}</span></span
          >
          <code class="prm-badge">GET /prices/products/{id}</code>
          <div class="prm-preview-inputs">
            <input
              v-model.number="previewProductId"
              class="prm-in"
              placeholder="Product ID"
              style="width: 120px"
              type="number"
            />
            <input
              v-model.number="previewCustomerId"
              class="prm-in"
              placeholder="Customer ID (opt)"
              style="width: 150px"
              type="number"
            />
            <button
              class="prm-btn prm-btn-primary"
              :class="{ loading: previewLoading }"
              @click="loadPreviewPrices"
              :disabled="previewLoading"
            >
              <span v-if="!previewLoading">Tải giá</span
              ><span v-else class="prm-spin"></span>
            </button>
          </div>
          <button
            class="prm-btn-icon ml-auto"
            @click="
              previewPromo = null;
              previewPrices = [];
            "
          >
            ✕
          </button>
        </div>
        <div v-if="previewPrices.length" class="prm-tw mt2">
          <table class="prm-tbl">
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
              <tr v-for="row in previewPrices" :key="row.variantId">
                <td class="mono dim">{{ row.variantId }}</td>
                <td>{{ row.variantName }}</td>
                <td class="mono dim">{{ row.sku }}</td>
                <td class="mono dim">{{ row.currencyCode }}</td>
                <td>{{ fmtMoney(row.price) }}</td>
                <td :class="row.finalPrice < row.price ? 'clr-green bold' : ''">
                  {{ fmtMoney(row.finalPrice) }}
                </td>
                <td>
                  <span v-if="row.promotionCode" class="prm-tag prm-tag-y">{{
                    row.promotionCode
                  }}</span
                  ><span v-else class="dim">—</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else-if="previewLoaded" class="prm-empty mt2">
          Không có dữ liệu
        </div>
      </div>
    </transition>

    <!-- Create/Edit Dialog -->
    <div v-if="dlg.open" class="prm-overlay" @click.self="dlg.open = false">
      <div class="prm-dialog">
        <div class="prm-dlg-head">
          <span class="prm-dlg-title">{{
            dlg.mode === "create" ? "+ Tạo khuyến mãi" : "Sửa khuyến mãi"
          }}</span>
          <button class="prm-dlg-close" @click="dlg.open = false">✕</button>
        </div>
        <div class="prm-dlg-body">
          <div v-if="dlg.alert" class="prm-alert prm-alert-r mb3">
            {{ dlg.alert }}
          </div>

          <div class="prm-dlg-section">THÔNG TIN CƠ BẢN</div>
          <div class="pm-row2 mb2">
            <div class="pm-field">
              <label class="pm-lbl">Code <span class="pm-req">*</span></label>
              <input
                v-model="dlg.form.code"
                class="pm-in"
                :disabled="dlg.mode === 'edit'"
                placeholder="GAMING_Q2_2026"
              />
            </div>
            <div class="pm-field">
              <label class="pm-lbl"
                >Tên chiến dịch <span class="pm-req">*</span></label
              >
              <input
                v-model="dlg.form.name"
                class="pm-in"
                placeholder="Sale Laptop Gaming Q2/2026"
              />
            </div>
          </div>
          <div class="pm-field mb2">
            <label class="pm-lbl">Mô tả</label>
            <textarea
              v-model="dlg.form.description"
              class="pm-in pm-textarea"
              rows="2"
              placeholder="Mô tả ngắn..."
            ></textarea>
          </div>

          <div class="prm-dlg-section">GIẢM GIÁ</div>
          <div class="pm-row3 mb2">
            <div class="pm-field">
              <label class="pm-lbl">Loại giảm</label>
              <select v-model="dlg.form.discountType" class="pm-in pm-select">
                <option value="PERCENT">PERCENT (%)</option>
                <option value="AMOUNT">AMOUNT (₫)</option>
              </select>
            </div>
            <div class="pm-field">
              <label class="pm-lbl">Giá trị giảm</label>
              <input
                v-model.number="dlg.form.discountValue"
                class="pm-in"
                type="number"
                :min="0"
                placeholder="10"
              />
            </div>
            <div class="pm-field">
              <label class="pm-lbl">Ưu tiên (priority)</label>
              <input
                v-model.number="dlg.form.priority"
                class="pm-in"
                type="number"
                :min="0"
                placeholder="1"
              />
            </div>
          </div>

          <div class="prm-dlg-section">
            🎁 COMBO (để trống nếu không phải combo)
          </div>
          <div class="pm-row2 mb2">
            <div class="pm-field">
              <label class="pm-lbl">Số lượng mua (buyQty)</label>
              <input
                v-model.number="dlg.form.buyQty"
                class="pm-in"
                type="number"
                :min="0"
                placeholder="VD: 2"
              />
            </div>
            <div class="pm-field">
              <label class="pm-lbl">Số lượng tặng (getQty)</label>
              <input
                v-model.number="dlg.form.getQty"
                class="pm-in"
                type="number"
                :min="0"
                placeholder="VD: 1"
              />
            </div>
          </div>

          <div class="prm-dlg-section">THỜI GIAN & GIỚI HẠN</div>
          <div class="pm-row3 mb2">
            <div class="pm-field">
              <label class="pm-lbl">Ngày bắt đầu</label>
              <input
                v-model="dlg.form.startDate"
                class="pm-in"
                type="datetime-local"
              />
            </div>
            <div class="pm-field">
              <label class="pm-lbl">Ngày kết thúc</label>
              <input
                v-model="dlg.form.endDate"
                class="pm-in"
                type="datetime-local"
              />
            </div>
            <div class="pm-field">
              <label class="pm-lbl">Giới hạn lượt dùng</label>
              <input
                v-model.number="dlg.form.usageLimit"
                class="pm-in"
                type="number"
                :min="0"
                placeholder="Không giới hạn"
              />
            </div>
          </div>

          <div class="prm-dlg-section">PHẠM VI ÁP DỤNG</div>
          <div class="pm-row3 mb2">
            <div class="pm-field">
              <label class="pm-lbl">Scope</label>
              <select v-model="dlg.form.scope" class="pm-in pm-select">
                <option value="ALL">ALL</option>
                <option value="PRODUCT">PRODUCT</option>
                <option value="VARIANT">VARIANT</option>
              </select>
            </div>
            <div class="pm-field pm-field-toggle">
              <label class="pm-lbl">Stackable</label>
              <label class="prm-tog"
                ><input type="checkbox" v-model="dlg.form.stackable" /><span
                  class="prm-tog-t"
                ></span
              ></label>
            </div>
            <div class="pm-field pm-field-toggle">
              <label class="pm-lbl">Kích hoạt</label>
              <label class="prm-tog"
                ><input type="checkbox" v-model="dlg.form.isActive" /><span
                  class="prm-tog-t"
                ></span
              ></label>
            </div>
          </div>
          <div class="pm-row2 mb2">
            <div class="pm-field">
              <label class="pm-lbl"
                >Product IDs
                <span class="pm-dim">(cách nhau bởi dấu phẩy)</span></label
              >
              <input
                v-model="dlg.form.productIdsText"
                class="pm-in"
                placeholder="1,2,3"
              />
            </div>
            <div class="pm-field">
              <label class="pm-lbl"
                >Variant IDs
                <span class="pm-dim">(cách nhau bởi dấu phẩy)</span></label
              >
              <input
                v-model="dlg.form.variantIdsText"
                class="pm-in"
                placeholder="10,11,12"
              />
            </div>
          </div>

          <div class="prm-dlg-section">
            👥 ĐỐI TƯỢNG KHÁCH HÀNG (để trống = tất cả)
          </div>
          <div class="pm-row2">
            <div class="pm-field">
              <label class="pm-lbl">Loại khách hàng (customerTypes)</label>
              <div class="prm-checkgroup">
                <label
                  v-for="t in ['VIP', 'REGULAR', 'NEW']"
                  :key="t"
                  class="prm-check"
                >
                  <input
                    type="checkbox"
                    :value="t"
                    v-model="dlg.form.customerTypes"
                  /><span>{{ t }}</span>
                </label>
              </div>
            </div>
            <div class="pm-field">
              <label class="pm-lbl">Hạng VIP (vipTiers)</label>
              <div class="prm-checkgroup">
                <label
                  v-for="t in ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM']"
                  :key="t"
                  class="prm-check"
                >
                  <input
                    type="checkbox"
                    :value="t"
                    v-model="dlg.form.vipTiers"
                  /><span>{{ t }}</span>
                </label>
              </div>
            </div>
          </div>
        </div>
        <div class="prm-dlg-foot">
          <button class="prm-btn prm-btn-ghost" @click="dlg.open = false">
            Hủy
          </button>
          <button
            class="prm-btn prm-btn-primary"
            :class="{ loading: dlg.loading }"
            @click="save"
            :disabled="dlg.loading"
          >
            <span v-if="!dlg.loading">{{
              dlg.mode === "create" ? "Tạo mới" : "Lưu thay đổi"
            }}</span
            ><span v-else class="prm-spin"></span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, nextTick, computed } from "vue";
import { promotionsApi } from "../../api/promotions.api";
import { pricesApi } from "../../api/prices.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";
import "../../assets/styles/promotion-manager.css";
import QuickNav from "../../components/QuickNav.vue";

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
    const rules = parseJ(p?.rulesJson, null); // parse toàn bộ object
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
    // Backend trả "2026-01-01T00:00:00" (không có Z), cắt thẳng 16 ký tự
    // để tránh bị lệch múi giờ khi dùng toISOString()
    const s = String(iso);
    if (s.length >= 16 && !s.endsWith("Z") && !s.includes("+")) {
      return s.slice(0, 16);
    }
    // Có timezone info thì convert đúng cách
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

function handleQuickNav(actionKey, hint) {
  switch (actionKey) {
    case "openCreate":
      openCreate();
      if (hint === "combo") {
        // Scroll dialog to combo section after open
        nextTick(() => {
          const el = document.querySelector(".prm-dlg-body");
          if (el) el.scrollTo({ top: 400, behavior: "smooth" });
        });
      }
      if (hint === "vip") {
        nextTick(() => {
          const el = document.querySelector(".prm-dlg-body");
          if (el) el.scrollTo({ top: 9999, behavior: "smooth" });
        });
      }
      break;
    case "scrollTable":
      document
        .getElementById("prm-main-table")
        ?.scrollIntoView({ behavior: "smooth", block: "start" });
      break;
    case "scrollValidate":
      document
        .getElementById("prm-validate-bar")
        ?.scrollIntoView({ behavior: "smooth", block: "start" });
      break;
    case "loadConflicts":
      loadConflicts();
      break;
    case "loadExpiring":
      loadExpiring();
      break;
    case "loadActiveReport":
      loadActiveReport();
      break;
    case "loadReport":
      loadReport();
      break;
    case "loadSummaryReport":
      loadSummaryReport();
      break;
  }
}

onMounted(load);
</script>
