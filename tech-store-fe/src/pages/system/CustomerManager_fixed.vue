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
        <button :class="['crm-nav-tab', { active: mainTab === 'zeroOrder' }]" @click="mainTab = 'zeroOrder'; loadZeroOrderData()">
          Khách chưa mua
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

      <!-- ══ TAB 4: ZERO ORDER ══ -->
      <template v-if="mainTab === 'zeroOrder'">
        
        <!-- HEADER -->
        <div class="zo-header">
          <div class="zo-header-left">
            <div class="zo-eyebrow">Sales Intelligence</div>
            <h1 class="zo-title">
              <span class="zo-title-icon">🎯</span>
              Khách chưa mua đơn nào
            </h1>
            <p class="zo-subtitle">
              Phát hiện &amp; chuyển đổi Zero-Order Users — những người đã đăng ký nhưng chưa từng mua hàng
            </p>
          </div>
          <div class="zo-header-actions">
            <button class="zo-btn zo-btn--ghost" @click="loadZeroOrderData" :disabled="zeroOrderLoading">
              <span>↻</span> Làm mới
            </button>
            <button
              class="zo-btn zo-btn--outline"
              :disabled="filteredZeroOrderCustomers.length === 0"
              @click="selectAllZeroOrder"
            >
              {{ isZeroOrderAllSelected ? '☑ Bỏ chọn tất cả' : '☐ Chọn tất cả' }}
            </button>
            <button
              class="zo-btn zo-btn--primary"
              :disabled="zeroOrderSelected.length === 0 || zeroOrderSending"
              @click="openZeroOrderSendDialog"
            >
              <span>📨</span>
              Gửi thông báo
              <span v-if="zeroOrderSelected.length > 0" class="zo-btn__badge">{{ zeroOrderSelected.length }}</span>
            </button>
          </div>
        </div>

        <!-- STATS CARDS -->
        <div class="zo-stats">
          <div
            v-for="(s, i) in zeroOrderStatCards"
            :key="i"
            class="zo-stat"
            :class="`zo-stat--${s.color}`"
            :style="{ animationDelay: `${i * 70}ms` }"
          >
            <div class="zo-stat__icon">{{ s.icon }}</div>
            <div class="zo-stat__body">
              <div class="zo-stat__value">
                <span v-if="zeroOrderStatsLoading" class="zo-skeleton zo-skeleton--num"></span>
                <template v-else>{{ s.value }}</template>
              </div>
              <div class="zo-stat__label">{{ s.label }}</div>
              <div class="zo-stat__hint">{{ s.hint }}</div>
            </div>
          </div>
        </div>

        <!-- FILTER BAR -->
        <div class="zo-filters">
          <div class="zo-filters__group">
            <span class="zo-filters__label">Đăng ký tối thiểu</span>
            <div class="zo-pills">
              <button
                v-for="opt in zeroOrderDayOptions"
                :key="opt.value"
                class="zo-pill"
                :class="{ active: zeroOrderSelectedDays === opt.value }"
                @click="selectZeroOrderDays(opt.value)"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>
          <div class="zo-filters__sep"></div>
          <div class="zo-search-wrap">
            <span class="zo-search-icon">🔍</span>
            <input
              v-model="zeroOrderSearch"
              class="zo-search"
              placeholder="Tìm tên, email, SĐT..."
              @input="zeroOrderPage = 1"
            />
            <button v-if="zeroOrderSearch" class="zo-search-clear" @click="zeroOrderSearch = ''">✕</button>
          </div>
          <div class="zo-filters__count">
            <span class="zo-count-num">{{ filteredZeroOrderCustomers.length }}</span>
            <span class="zo-count-label">khách</span>
          </div>
        </div>

        <!-- TABLE -->
        <div class="zo-table-wrap">
          <!-- Loading skeleton -->
          <div v-if="zeroOrderLoading" class="zo-loading">
            <div v-for="i in 6" :key="i" class="zo-row-skeleton" :style="{ animationDelay: `${i*60}ms` }">
              <div class="zo-skeleton zo-skeleton--check"></div>
              <div class="zo-skeleton zo-skeleton--avatar"></div>
              <div class="zo-skeleton zo-skeleton--text"></div>
              <div class="zo-skeleton zo-skeleton--sm"></div>
              <div class="zo-skeleton zo-skeleton--sm"></div>
              <div class="zo-skeleton zo-skeleton--sm"></div>
              <div class="zo-skeleton zo-skeleton--btn"></div>
            </div>
          </div>

          <!-- Empty -->
          <div v-else-if="filteredZeroOrderCustomers.length === 0" class="zo-empty">
            <div class="zo-empty__icon">🎉</div>
            <div class="zo-empty__title">Không có khách nào!</div>
            <div class="zo-empty__sub">Tất cả khách trong khoảng thời gian này đều đã mua hàng.</div>
          </div>

          <!-- Table -->
          <table v-else class="zo-table">
            <thead>
              <tr>
                <th class="th-check">
                  <input
                    type="checkbox"
                    class="zo-checkbox"
                    :checked="isZeroOrderAllSelected"
                    @change="selectAllZeroOrder"
                  />
                </th>
                <th>Khách hàng</th>
                <th>Loại</th>
                <th>Đăng ký lúc</th>
                <th>Ngày chờ</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(c, idx) in pagedZeroOrderCustomers"
                :key="c.id"
                class="zo-row"
                :class="[getZeroOrderRowUrgency(c), { 'zo-row--selected': zeroOrderSelected.includes(c.id) }]"
                :style="{ animationDelay: `${idx * 35}ms` }"
              >
                <td class="td-check">
                  <input type="checkbox" class="zo-checkbox" :value="c.id" v-model="zeroOrderSelected" />
                </td>

                <td class="td-info">
                  <div class="zo-customer">
                    <div class="zo-avatar" :style="{ background: getAvatarColor(c.name) }">
                      {{ getInitials(c.name) }}
                    </div>
                    <div class="zo-customer__text">
                      <div class="zo-customer__name">{{ c.name }}</div>
                      <div class="zo-customer__email">{{ c.email }}</div>
                      <div class="zo-customer__phone">{{ c.phone || '—' }}</div>
                    </div>
                  </div>
                </td>

                <td class="td-type">
                  <span class="zo-type-badge" :class="c.customerType?.toLowerCase()">
                    {{ c.customerType === 'VIP' ? '⭐ VIP' : '👤 Regular' }}
                  </span>
                </td>

                <td class="td-date">
                  <div class="zo-date">{{ fmtDate(c.createdAt) }}</div>
                  <div class="zo-date-rel">{{ fmtRelative(c.createdAt) }}</div>
                </td>

                <td class="td-wait">
                  <div class="zo-wait" :class="waitClass(daysSince(c.createdAt))">
                    <span class="zo-wait__num">{{ daysSince(c.createdAt) }}</span>
                    <span class="zo-wait__unit">ngày</span>
                  </div>
                </td>

                <td class="td-status">
                  <div class="zo-status">
                    <span class="zo-status__dot" :class="statusDotClass(c)"></span>
                    <span class="zo-status__text">{{ statusLabel(c) }}</span>
                  </div>
                </td>

                <td class="td-actions">
                  <button
                    class="zo-act-btn zo-act-btn--send"
                    :disabled="zeroOrderSendingIds.has(c.id)"
                    @click="sendZeroOrderSingle(c)"
                    title="Gửi thông báo Welcome"
                  >
                    <span v-if="zeroOrderSendingIds.has(c.id)" class="zo-spinner"></span>
                    <span v-else>📨</span>
                    {{ zeroOrderSendingIds.has(c.id) ? 'Đang gửi...' : 'Gửi' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Pagination -->
          <div v-if="filteredZeroOrderCustomers.length > zeroOrderPageSize" class="zo-pagination">
            <button class="zo-page-btn" :disabled="zeroOrderPage === 1" @click="zeroOrderPage--">‹</button>
            <button
              v-for="p in zeroOrderTotalPages"
              :key="p"
              class="zo-page-btn"
              :class="{ active: zeroOrderPage === p }"
              @click="zeroOrderPage = p"
            >{{ p }}</button>
            <button class="zo-page-btn" :disabled="zeroOrderPage === zeroOrderTotalPages" @click="zeroOrderPage++">›</button>
            <span class="zo-page-info">
              {{ (zeroOrderPage-1)*zeroOrderPageSize + 1 }}–{{ Math.min(zeroOrderPage*zeroOrderPageSize, filteredZeroOrderCustomers.length) }}
              / {{ filteredZeroOrderCustomers.length }}
            </span>
          </div>
        </div>

        <!-- SEND DIALOG (bulk) -->
        <div v-if="zeroOrderSendDialog" class="zo-overlay" @click.self="zeroOrderSendDialog = false">
          <div class="zo-dialog">
            <div class="zo-dialog__header">
              <div class="zo-dialog__title">📨 Gửi thông báo chào mừng</div>
              <button class="zo-dialog__close" @click="zeroOrderSendDialog = false">✕</button>
            </div>

            <div class="zo-dialog__body">
              <!-- Target summary -->
              <div class="zo-target-summary">
                <div class="zo-target-summary__icon">👥</div>
                <div>
                  <div class="zo-target-summary__num">{{ zeroOrderSelected.length }}</div>
                  <div class="zo-target-summary__label">khách được chọn sẽ nhận thông báo</div>
                </div>
              </div>

              <!-- Template selector -->
              <div class="zo-field">
                <label class="zo-label">Mẫu thông báo</label>
                <div class="zo-template-grid">
                  <div
                    v-for="tpl in zeroOrderTemplates"
                    :key="tpl.id"
                    class="zo-template-card"
                    :class="{ active: zeroOrderForm.templateId === tpl.id }"
                    @click="selectZeroOrderTemplate(tpl)"
                  >
                    <div class="zo-template-card__icon">{{ tpl.icon }}</div>
                    <div class="zo-template-card__name">{{ tpl.name }}</div>
                    <div class="zo-template-card__desc">{{ tpl.desc }}</div>
                  </div>
                </div>
              </div>

              <!-- Title -->
              <div class="zo-field">
                <label class="zo-label">Tiêu đề thông báo</label>
                <input v-model="zeroOrderForm.title" class="zo-input" placeholder="Nhập tiêu đề..." />
              </div>

              <!-- Message -->
              <div class="zo-field">
                <label class="zo-label">Nội dung</label>
                <textarea
                  v-model="zeroOrderForm.message"
                  class="zo-textarea"
                  rows="5"
                  placeholder="Nhập nội dung thông báo..."
                ></textarea>
                <div class="zo-field__hint">
                  Dùng <code>{name}</code> để chèn tên khách hàng tự động
                </div>
              </div>

              <!-- Preview -->
              <div class="zo-preview">
                <div class="zo-preview__label">👁 Xem trước — như khách sẽ thấy</div>
                <div class="zo-preview__card">
                  <div class="zo-preview__icon">{{ currentZeroOrderTemplate?.icon || '📢' }}</div>
                  <div>
                    <div class="zo-preview__title">{{ zeroOrderForm.title || '(Chưa có tiêu đề)' }}</div>
                    <div class="zo-preview__msg">
                      {{ (zeroOrderForm.message || '(Chưa có nội dung)').replace(/\{name\}/g, 'Nguyễn Văn A') }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="zo-dialog__footer">
              <button class="zo-btn zo-btn--ghost" @click="zeroOrderSendDialog = false" :disabled="zeroOrderSending">Hủy</button>
              <button
                class="zo-btn zo-btn--primary"
                :disabled="!zeroOrderForm.title || !zeroOrderForm.message || zeroOrderSending"
                @click="sendZeroOrderBulk"
              >
                <span v-if="zeroOrderSending" class="zo-spinner"></span>
                <span v-else>📨</span>
                {{ zeroOrderSending
                  ? `Đang gửi (${zeroOrderSentCount}/${zeroOrderSelected.length})...`
                  : `Gửi cho ${zeroOrderSelected.length} khách` }}
              </button>
            </div>

            <!-- Progress bar when sending -->
            <div v-if="zeroOrderSending" class="zo-send-progress">
              <div
                class="zo-send-progress__bar"
                :style="{ width: `${Math.round((zeroOrderSentCount / zeroOrderSelected.length) * 100)}%` }"
              ></div>
            </div>
          </div>
        </div>

        <!-- TOAST -->
        <transition name="zo-toast-fade">
          <div v-if="zeroOrderResultToast.show" class="zo-toast" :class="`zo-toast--${zeroOrderResultToast.type}`">
            <span class="zo-toast__icon">{{ zeroOrderResultToast.type === 'success' ? '✅' : '⚠️' }}</span>
            <span class="zo-toast__msg">{{ zeroOrderResultToast.msg }}</span>
          </div>
        </transition>
      </template>

    </div><!-- /crm-inner -->
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";
import http from "../../api/http";

// ── Zero Order State ──────────────────────────────────────────────────────
const zeroOrderLoading       = ref(false);
const zeroOrderStatsLoading  = ref(false);
const zeroOrderSending       = ref(false);
const zeroOrderSendDialog    = ref(false);
const zeroOrderCustomers     = ref([]);
const zeroOrderStatsData     = ref({});
const zeroOrderSelected      = ref([]);
const zeroOrderSendingIds    = ref(new Set());
const zeroOrderSentCount     = ref(0);
const zeroOrderSearch        = ref("");
const zeroOrderSelectedDays  = ref(3);
const zeroOrderPage          = ref(1);
const zeroOrderPageSize      = 15;

const zeroOrderResultToast = ref({ show: false, type: "success", msg: "" });

const zeroOrderForm = ref({
  templateId: "welcome",
  title: "",
  message: "",
});

// ── Zero Order Templates ──────────────────────────────────────────────────────
const zeroOrderTemplates = [
  {
    id: "welcome",
    icon: "🎉",
    name: "Chào mừng",
    desc: "Mời mua lần đầu với ưu đãi",
    title: "🎉 Chào mừng bạn đến với cửa hàng!",
    message: `Xin chào {name}! 👋\n\nCảm ơn bạn đã đăng ký tài khoản tại cửa hàng chúng tôi.\n\n🛍️ Đặt đơn hàng đầu tiên ngay hôm nay để nhận ưu đãi đặc biệt dành cho khách mới!\n\nHãy khám phá ngay các sản phẩm laptop & phụ kiện chất lượng cao nhé! 💻`,
  },
  {
    id: "offer",
    icon: "💰",
    name: "Ưu đãi hấp dẫn",
    desc: "Voucher giảm giá kèm deadline",
    title: "💰 Ưu đãi đặc biệt chỉ dành riêng cho bạn!",
    message: `Xin chào {name}!\n\nChúng tôi nhận thấy bạn chưa thực hiện đơn hàng nào.\n\n🎁 Chúng tôi tặng bạn ưu đãi GIẢM GIÁ ĐẶC BIỆT cho lần mua đầu tiên!\n\n⏰ Ưu đãi có hạn — Đặt hàng ngay để không bỏ lỡ!\n\nKhám phá ngay sản phẩm tại cửa hàng của chúng tôi 🚀`,
  },
  {
    id: "nudge",
    icon: "🤝",
    name: "Hỗ trợ tư vấn",
    desc: "Mời chat với tư vấn viên",
    title: "🤝 Chúng tôi sẵn sàng hỗ trợ bạn!",
    message: `Xin chào {name}!\n\nBạn đang phân vân chưa biết chọn sản phẩm nào?\n\n💬 Đội ngũ tư vấn của chúng tôi luôn sẵn sàng giúp bạn tìm được chiếc laptop phù hợp nhất với nhu cầu và ngân sách.\n\n✅ Tư vấn miễn phí\n✅ Bảo hành chính hãng\n✅ Giao hàng nhanh\n\nLiên hệ ngay với chúng tôi nhé! 📞`,
  },
  {
    id: "custom",
    icon: "✏️",
    name: "Tùy chỉnh",
    desc: "Soạn nội dung riêng",
    title: "",
    message: "",
  },
];

const currentZeroOrderTemplate = computed(() =>
  zeroOrderTemplates.find((t) => t.id === zeroOrderForm.value.templateId)
);

// ── Zero Order Filter options ─────────────────────────────────────────────────
const zeroOrderDayOptions = [
  { value: 1,  label: "≥ 1 ngày" },
  { value: 3,  label: "≥ 3 ngày" },
  { value: 7,  label: "≥ 7 ngày" },
  { value: 14, label: "≥ 14 ngày" },
  { value: 30, label: "≥ 30 ngày" },
];

// ── Zero Order Stat cards ─────────────────────────────────────────────────────
const zeroOrderStatCards = computed(() => [
  {
    icon: "⏳",
    color: "blue",
    value: zeroOrderStatsData.value.registeredOver3Days ?? "—",
    label: "Đăng ký ≥ 3 ngày",
    hint: "Chưa mua bất kỳ đơn nào",
  },
  {
    icon: "📅",
    color: "amber",
    value: zeroOrderStatsData.value.registeredOver7Days ?? "—",
    label: "Đăng ký ≥ 7 ngày",
    hint: "Cần hỗ trợ tư vấn",
  },
  {
    icon: "🚨",
    color: "red",
    value: zeroOrderStatsData.value.registeredOver30Days ?? "—",
    label: "Đăng ký ≥ 30 ngày",
    hint: "Nguy cơ mất khách cao",
  },
  {
    icon: "✅",
    color: "green",
    value: zeroOrderSelected.value.length,
    label: "Đang chọn",
    hint: "Sẵn sàng gửi thông báo",
  },
]);

// ── Zero Order Computed ───────────────────────────────────────────────────────
const filteredZeroOrderCustomers = computed(() => {
  const kw = zeroOrderSearch.value.trim().toLowerCase();
  if (!kw) return zeroOrderCustomers.value;
  return zeroOrderCustomers.value.filter((c) =>
    `${c.name} ${c.email} ${c.phone ?? ""}`.toLowerCase().includes(kw)
  );
});

const zeroOrderTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredZeroOrderCustomers.value.length / zeroOrderPageSize))
);

const pagedZeroOrderCustomers = computed(() => {
  const start = (zeroOrderPage.value - 1) * zeroOrderPageSize;
  return filteredZeroOrderCustomers.value.slice(start, start + zeroOrderPageSize);
});

const isZeroOrderAllSelected = computed(() =>
  filteredZeroOrderCustomers.value.length > 0 &&
  filteredZeroOrderCustomers.value.every((c) => zeroOrderSelected.value.includes(c.id))
);

// ── Zero Order Load data ──────────────────────────────────────────────────────
async function loadZeroOrderData() {
  zeroOrderLoading.value = true;
  zeroOrderSelected.value = [];
  try {
    const res = await customersApi.listZeroOrder(zeroOrderSelectedDays.value);
    zeroOrderCustomers.value = Array.isArray(res.data) ? res.data : (res.data?.data ?? []);
  } catch (e) {
    console.error("Failed to load zero-order customers:", e);
    zeroOrderCustomers.value = [];
  } finally {
    zeroOrderLoading.value = false;
  }
}

async function loadZeroOrderStats() {
  zeroOrderStatsLoading.value = true;
  try {
    const res = await customersApi.getZeroOrderStats();
    zeroOrderStatsData.value = res.data ?? {};
  } catch (e) {
    console.error("Failed to load stats:", e);
  } finally {
    zeroOrderStatsLoading.value = false;
  }
}

function selectZeroOrderDays(d) {
  zeroOrderSelectedDays.value = d;
  zeroOrderPage.value = 1;
  zeroOrderSelected.value = [];
  loadZeroOrderData();
}

function selectAllZeroOrder() {
  if (isZeroOrderAllSelected.value) {
    zeroOrderSelected.value = [];
  } else {
    zeroOrderSelected.value = filteredZeroOrderCustomers.value.map((c) => c.id);
  }
}

// ── Zero Order Template ───────────────────────────────────────────────────────
function selectZeroOrderTemplate(tpl) {
  zeroOrderForm.value.templateId = tpl.id;
  if (tpl.id !== "custom") {
    zeroOrderForm.value.title   = tpl.title;
    zeroOrderForm.value.message = tpl.message;
  }
}

function openZeroOrderSendDialog() {
  selectZeroOrderTemplate(zeroOrderTemplates[0]);
  zeroOrderSendDialog.value = true;
}

// ── Zero Order Send single ────────────────────────────────────────────────────
async function sendZeroOrderSingle(customer) {
  const newSet = new Set(zeroOrderSendingIds.value);
  newSet.add(customer.id);
  zeroOrderSendingIds.value = newSet;
  try {
    await http.post("/api/auth/notifications/send", {
      customerIds: [customer.id],
      title:   `🎉 Xin chào ${customer.name}!`,
      message: zeroOrderTemplates[0].message.replace(/\{name\}/g, customer.name),
      type:    "WELCOME",
    });
    toast("success", `✅ Đã gửi thông báo tới ${customer.name}`);
  } catch (e) {
    toast("error", `Gửi thất bại: ${e?.response?.data?.message ?? e.message}`);
  } finally {
    const s = new Set(zeroOrderSendingIds.value);
    s.delete(customer.id);
    zeroOrderSendingIds.value = s;
  }
}

// ── Zero Order Send bulk ──────────────────────────────────────────────────────
async function sendZeroOrderBulk() {
  if (!zeroOrderForm.value.title || !zeroOrderForm.value.message) return;
  zeroOrderSending.value  = true;
  zeroOrderSentCount.value = 0;

  const ids    = [...zeroOrderSelected.value];
  const batchSize = 10;
  let success  = 0;
  let fail     = 0;

  for (let i = 0; i < ids.length; i += batchSize) {
    const chunk = ids.slice(i, i + batchSize);
    const chunkCustomers = zeroOrderCustomers.value.filter((c) => chunk.includes(c.id));

    // Personalize mỗi khách — gửi song song trong batch
    const promises = chunkCustomers.map(async (c) => {
      try {
        await http.post("/api/auth/notifications/send", {
          customerIds: [c.id],
          title:   zeroOrderForm.value.title,
          message: zeroOrderForm.value.message.replace(/\{name\}/g, c.name),
          type:    "WELCOME",
        });
        success++;
      } catch {
        fail++;
      } finally {
        zeroOrderSentCount.value++;
      }
    });

    await Promise.all(promises);
  }

  zeroOrderSending.value   = false;
  zeroOrderSendDialog.value = false;
  zeroOrderSelected.value   = [];

  if (fail === 0) {
    toast("success", `✅ Đã gửi thành công ${success} thông báo!`);
  } else {
    toast("error", `⚠️ Thành công: ${success} | Thất bại: ${fail}`);
  }

  await loadZeroOrderData();
}

// ── Zero Order Toast ──────────────────────────────────────────────────────────
function showToast(type, msg) {
  zeroOrderResultToast.value = { show: true, type, msg };
  setTimeout(() => { zeroOrderResultToast.value.show = false; }, 4500);
}

// ── Zero Order Helpers ────────────────────────────────────────────────────────
function daysSince(dateStr) {
  if (!dateStr) return 0;
  return Math.floor((Date.now() - new Date(dateStr).getTime()) / 86400000);
}

function fmtDate(d) {
  if (!d) return "—";
  return new Date(d).toLocaleDateString("vi-VN");
}

function fmtRelative(d) {
  if (!d) return "";
  const days = daysSince(d);
  if (days === 0) return "Hôm nay";
  if (days === 1) return "Hôm qua";
  return `${days} ngày trước`;
}

function getZeroOrderRowUrgency(c) {
  const d = daysSince(c.createdAt);
  if (d >= 30) return "zo-row--critical";
  if (d >= 7)  return "zo-row--warning";
  return "";
}

function waitClass(days) {
  if (days >= 30) return "zo-wait--critical";
  if (days >= 7)  return "zo-wait--warning";
  return "zo-wait--normal";
}

function statusDotClass(c) {
  const d = daysSince(c.createdAt);
  if (d >= 30) return "dot--red";
  if (d >= 7)  return "dot--amber";
  return "dot--green";
}

function statusLabel(c) {
  const d = daysSince(c.createdAt);
  if (d >= 30) return "Nguy cơ cao 🚨";
  if (d >= 7)  return "Cần tư vấn 📞";
  return "Mới đăng ký 🌱";
}

// ── Existing CustomerManager code continues here ─────────────────────────────────--
const mainTab = ref("customers");

function reloadCurrentTab() {
  if (mainTab.value === 'customers') load();
  else if (mainTab.value === 'birthdays') loadBirthdayData();
  else if (mainTab.value === 'inactive') loadInactiveAll();
  else if (mainTab.value === 'zeroOrder') loadZeroOrderData();
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
   ZERO ORDER STYLES
═══════════════════════════ */
/* ── BASE ─────────────────────────────────────────────────────── */
.zo-page {
  min-height: 100vh;
  background: #f8f9fc;
  padding: 28px 32px;
  font-family: 'Inter', system-ui, sans-serif;
  color: #1a1d2e;
}

/* ── HEADER ─────────────────────────────────────────────────────── */
.zo-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}
.zo-eyebrow {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .12em;
  text-transform: uppercase;
  color: #6366f1;
  margin-bottom: 6px;
}
.zo-title {
  font-size: 26px;
  font-weight: 900;
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #0f172a;
}
.zo-title__icon { font-size: 28px; }
.zo-subtitle { font-size: 13.5px; color: #64748b; margin: 0; }
.zo-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* ── BUTTONS ────────────────────────────────────────────────────── */
.zo-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border-radius: 9px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all .18s;
  white-space: nowrap;
}
.zo-btn:disabled { opacity: .45; cursor: not-allowed; pointer-events: none; }
.zo-btn--primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  box-shadow: 0 3px 12px rgba(99,102,241,.35);
}
.zo-btn--primary:hover { transform: translateY(-1px); box-shadow: 0 5px 18px rgba(99,102,241,.45); }
.zo-btn--ghost {
  background: #fff;
  color: #475569;
  border: 1.5px solid #e2e8f0;
}
.zo-btn--ghost:hover { background: #f1f5f9; }
.zo-btn--outline {
  background: transparent;
  color: #6366f1;
  border: 1.5px solid #6366f1;
}
.zo-btn--outline:hover { background: #eef2ff; }
.zo-btn__badge {
  background: #fff;
  color: #6366f1;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 800;
  padding: 1px 7px;
  min-width: 22px;
  text-align: center;
}

/* ── STATS ──────────────────────────────────────────────────────── */
.zo-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.zo-stat {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1.5px solid #e8eaf0;
  overflow: hidden;
  animation: zo-fade-up .5s both;
  transition: transform .2s, box-shadow .2s;
}
.zo-stat:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,.07); }
.zo-stat__icon  { font-size: 32px; line-height: 1; flex-shrink: 0; }
.zo-stat__value { font-size: 28px; font-weight: 900; line-height: 1; margin-bottom: 4px; }
.zo-stat__label { font-size: 13px; font-weight: 600; color: #374151; }
.zo-stat__hint  { font-size: 11.5px; color: #94a3b8; margin-top: 2px; }

.zo-stat--blue  { border-left: 4px solid #3b82f6; }
.zo-stat--blue .zo-stat__value  { color: #1d4ed8; }
.zo-stat--amber { border-left: 4px solid #f59e0b; }
.zo-stat--amber .zo-stat__value { color: #b45309; }
.zo-stat--red   { border-left: 4px solid #ef4444; }
.zo-stat--red .zo-stat__value   { color: #b91c1c; }
.zo-stat--green { border-left: 4px solid #10b981; }
.zo-stat--green .zo-stat__value { color: #065f46; }

/* ── FILTERS ────────────────────────────────────────────────────── */
.zo-filters {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border: 1.5px solid #e8eaf0;
  border-radius: 12px;
  padding: 12px 18px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.zo-filters__group  { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.zo-filters__label  {
  font-size: 12px; font-weight: 700; color: #64748b;
  text-transform: uppercase; letter-spacing: .05em; white-space: nowrap;
}
.zo-pills  { display: flex; gap: 6px; flex-wrap: wrap; }
.zo-pill {
  padding: 5px 13px;
  border-radius: 20px;
  font-size: 12.5px;
  font-weight: 600;
  border: 1.5px solid #e2e8f0;
  background: transparent;
  color: #475569;
  cursor: pointer;
  transition: all .15s;
}
.zo-pill:hover  { border-color: #6366f1; color: #6366f1; }
.zo-pill.active { background: #6366f1; border-color: #6366f1; color: #fff; }
.zo-filters__sep { width: 1px; height: 28px; background: #e2e8f0; flex-shrink: 0; }
.zo-search-wrap {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 180px;
  max-width: 300px;
}
.zo-search-icon { position: absolute; left: 11px; font-size: 14px; pointer-events: none; }
.zo-search {
  width: 100%;
  padding: 7px 32px 7px 32px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  color: #1a1d2e;
  outline: none;
  transition: border-color .15s;
  background: #f8fafc;
}
.zo-search:focus { border-color: #6366f1; background: #fff; }
.zo-search-clear {
  position: absolute; right: 9px;
  background: none; border: none; cursor: pointer;
  color: #94a3b8; font-size: 12px;
}
.zo-filters__count {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-left: auto;
}
.zo-count-num   { font-size: 22px; font-weight: 900; color: #6366f1; }
.zo-count-label { font-size: 12px; color: #94a3b8; }

/* ── TABLE ──────────────────────────────────────────────────────── */
.zo-table-wrap {
  background: #fff;
  border: 1.5px solid #e8eaf0;
  border-radius: 14px;
  overflow: hidden;
}
.zo-table { width: 100%; border-collapse: collapse; }
.zo-table thead th {
  background: #f8fafc;
  padding: 12px 14px;
  text-align: left;
  font-size: 11.5px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: .07em;
  color: #64748b;
  border-bottom: 1.5px solid #e8eaf0;
}
.th-check { width: 44px; text-align: center; }

.zo-row {
  transition: background .15s;
  animation: zo-row-in .3s both;
  border-bottom: 1px solid #f1f5f9;
}
.zo-row:last-child { border-bottom: none; }
.zo-row:hover     { background: #fafbff; }
.zo-row--selected { background: #eef2ff !important; }
.zo-row--warning  { border-left: 3px solid #f59e0b; }
.zo-row--critical { border-left: 3px solid #ef4444; }

.zo-table td { padding: 13px 14px; vertical-align: middle; }
.td-check    { text-align: center; }

/* Customer */
.zo-customer { display: flex; align-items: center; gap: 11px; }
.zo-avatar {
  width: 38px; height: 38px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 800; color: #fff; flex-shrink: 0;
}
.zo-customer__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.zo-customer__name  { font-size: 13.5px; font-weight: 700; color: #0f172a; }
.zo-customer__email { font-size: 12px; color: #64748b; }
.zo-customer__phone { font-size: 11.5px; color: #94a3b8; }

/* Type badge */
.zo-type-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
}
.zo-type-badge.vip     { background: #fef3c7; color: #92400e; }
.zo-type-badge.regular { background: #f1f5f9; color: #475569; }

/* Date */
.zo-date     { font-size: 13px; font-weight: 600; color: #374151; }
.zo-date-rel { font-size: 11.5px; color: #94a3b8; }

/* Wait */
.zo-wait {
  display: inline-flex;
  align-items: baseline;
  gap: 3px;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 800;
}
.zo-wait__num  { font-size: 16px; }
.zo-wait__unit { font-size: 11px; }
.zo-wait--normal   { background: #dcfce7; color: #166534; }
.zo-wait--warning  { background: #fef9c3; color: #854d0e; }
.zo-wait--critical { background: #fee2e2; color: #991b1b; }

/* Status */
.zo-status { display: flex; align-items: center; gap: 7px; }
.zo-status__dot {
  width: 8px; height: 8px; border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
  flex-shrink: 0;
}
.dot--green { background: #10b981; }
.dot--amber { background: #f59e0b; }
.dot--red   { background: #ef4444; }
.zo-status__text { font-size: 12.5px; font-weight: 600; color: #374151; }

/* Action btn */
.zo-act-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  border: none;
  transition: all .15s;
}
.zo-act-btn--send {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
}
.zo-act-btn--send:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(99,102,241,.4);
}
.zo-act-btn:disabled { opacity: .5; cursor: not-allowed; }

.zo-checkbox { width: 16px; height: 16px; cursor: pointer; accent-color: #6366f1; }

/* ── PAGINATION ─────────────────────────────────────────────────── */
.zo-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px;
  border-top: 1px solid #f1f5f9;
  flex-wrap: wrap;
}
.zo-page-btn {
  min-width: 34px; height: 34px;
  border-radius: 8px;
  border: 1.5px solid #e2e8f0;
  background: transparent;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;
  padding: 0 8px;
}
.zo-page-btn:hover:not(:disabled) { border-color: #6366f1; color: #6366f1; }
.zo-page-btn.active { background: #6366f1; border-color: #6366f1; color: #fff; }
.zo-page-btn:disabled { opacity: .4; cursor: not-allowed; }
.zo-page-info { font-size: 12.5px; color: #94a3b8; margin-left: 8px; }

/* ── EMPTY / LOADING ────────────────────────────────────────────── */
.zo-empty { text-align: center; padding: 64px 24px; }
.zo-empty__icon  { font-size: 56px; margin-bottom: 16px; }
.zo-empty__title { font-size: 18px; font-weight: 800; color: #0f172a; margin-bottom: 8px; }
.zo-empty__sub   { font-size: 14px; color: #64748b; }

.zo-loading { padding: 16px; }
.zo-row-skeleton {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
  animation: zo-fade-up .5s both;
}
.zo-skeleton {
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
  flex-shrink: 0;
}
.zo-skeleton--num    { width: 60px; height: 32px; }
.zo-skeleton--check  { width: 16px; height: 16px; border-radius: 4px; }
.zo-skeleton--avatar { width: 38px; height: 38px; border-radius: 50%; }
.zo-skeleton--text   { width: 160px; height: 14px; }
.zo-skeleton--sm     { width: 80px; height: 14px; }
.zo-skeleton--btn    { width: 70px; height: 30px; }

/* ── DIALOG ─────────────────────────────────────────────────────── */
.zo-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15,23,42,.55);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: zo-overlay-in .2s;
}
.zo-dialog {
  background: #fff;
  border-radius: 18px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 60px rgba(0,0,0,.25);
  animation: zo-dialog-in .25s cubic-bezier(.34,1.56,.64,1);
  position: relative;
}
.zo-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 24px 18px;
  border-bottom: 1.5px solid #f1f5f9;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
}
.zo-dialog__title { font-size: 18px; font-weight: 800; color: #0f172a; }
.zo-dialog__close {
  background: none; border: none; font-size: 18px; cursor: pointer;
  color: #94a3b8; padding: 4px 8px; border-radius: 6px; transition: background .15s;
}
.zo-dialog__close:hover { background: #f1f5f9; color: #475569; }
.zo-dialog__body {
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.zo-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1.5px solid #f1f5f9;
  position: sticky;
  bottom: 0;
  background: #fff;
}

/* Target summary */
.zo-target-summary {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #eef2ff;
  border: 1.5px solid #c7d2fe;
  border-radius: 12px;
  padding: 14px 18px;
}
.zo-target-summary__icon  { font-size: 28px; }
.zo-target-summary__num   { font-size: 26px; font-weight: 900; color: #4338ca; }
.zo-target-summary__label { font-size: 13px; color: #6366f1; }

/* Template grid */
.zo-template-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 8px;
}
.zo-template-card {
  border: 2px solid #e2e8f0;
  border-radius: 11px;
  padding: 13px 15px;
  cursor: pointer;
  transition: all .15s;
  background: #f8fafc;
}
.zo-template-card:hover  { border-color: #a5b4fc; background: #f5f3ff; }
.zo-template-card.active { border-color: #6366f1; background: #eef2ff; }
.zo-template-card__icon { font-size: 22px; margin-bottom: 5px; }
.zo-template-card__name { font-size: 13px; font-weight: 700; color: #1e293b; }
.zo-template-card__desc { font-size: 11.5px; color: #64748b; margin-top: 2px; }

/* Form */
.zo-field { display: flex; flex-direction: column; gap: 6px; }
.zo-label {
  font-size: 12.5px; font-weight: 700; color: #374151;
  text-transform: uppercase; letter-spacing: .05em;
}
.zo-input, .zo-textarea {
  width: 100%;
  padding: 10px 13px;
  border: 1.5px solid #e2e8f0;
  border-radius: 9px;
  font-size: 13.5px;
  color: #1a1d2e;
  outline: none;
  transition: border-color .15s;
  font-family: inherit;
  resize: vertical;
  background: #f8fafc;
  box-sizing: border-box;
}
.zo-input:focus, .zo-textarea:focus { border-color: #6366f1; background: #fff; }
.zo-field__hint { font-size: 11.5px; color: #94a3b8; }
.zo-field__hint code {
  background: #f1f5f9;
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 11px;
  color: #6366f1;
}

/* Preview */
.zo-preview {
  background: #f8fafc;
  border: 1.5px dashed #c7d2fe;
  border-radius: 12px;
  padding: 14px 16px;
}
.zo-preview__label {
  font-size: 11px; font-weight: 800; text-transform: uppercase;
  letter-spacing: .1em; color: #94a3b8; margin-bottom: 10px;
}
.zo-preview__card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
}
.zo-preview__icon  { font-size: 24px; flex-shrink: 0; }
.zo-preview__title { font-size: 13.5px; font-weight: 700; color: #0f172a; margin-bottom: 4px; }
.zo-preview__msg   { font-size: 12.5px; color: #64748b; white-space: pre-line; line-height: 1.5; }

/* Send progress */
.zo-send-progress { height: 4px; background: #e2e8f0; overflow: hidden; }
.zo-send-progress__bar {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: width .4s ease;
}

/* ── TOAST ──────────────────────────────────────────────────────── */
.zo-toast {
  position: fixed;
  bottom: 32px;
  right: 32px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 8px 30px rgba(0,0,0,.18);
  z-index: 9999;
  max-width: 380px;
}
.zo-toast--success { background: #dcfce7; color: #166534; border: 1.5px solid #bbf7d0; }
.zo-toast--error   { background: #fee2e2; color: #991b1b; border: 1.5px solid #fecaca; }
.zo-toast-fade-enter-active, .zo-toast-fade-leave-active { transition: all .35s; }
.zo-toast-fade-enter-from, .zo-toast-fade-leave-to { opacity: 0; transform: translateY(12px) scale(.96); }

/* ── SPINNER ────────────────────────────────────────────────────── */
.zo-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
  flex-shrink: 0;
}

/* ── ANIMATIONS ─────────────────────────────────────────────────── */
@keyframes zo-fade-up {
  from { opacity: 0; transform: translateY(14px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes zo-row-in {
  from { opacity: 0; transform: translateX(-8px); }
  to   { opacity: 1; transform: translateX(0); }
}
@keyframes zo-overlay-in { from { opacity: 0; } to { opacity: 1; } }
@keyframes zo-dialog-in {
  from { opacity: 0; transform: scale(.93) translateY(20px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}
@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: .5; transform: scale(.8); }
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
