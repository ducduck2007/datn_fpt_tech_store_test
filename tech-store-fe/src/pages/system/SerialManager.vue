<template>
  <div class="sm-page">
    <div class="sm-inner">

      <!-- ── Header ── -->
      <header class="sm-header">
        <div class="sm-header__left">
          <div class="sm-eyebrow">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <rect x="2" y="5" width="20" height="14" rx="2"/>
              <path d="M2 10h20"/>
            </svg>
            Quản lý Kho
          </div>
          <h1 class="sm-title">Danh sách <span class="sm-title__accent">Số Seri</span></h1>
          <p class="sm-subtitle">Quản lý và theo dõi chi tiết từng sản phẩm trong hệ thống</p>
        </div>
        <div class="sm-header__actions">
          <button class="sm-btn sm-btn--outline" @click="load" :disabled="loading">
            <svg v-if="!loading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
              <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
              <path d="M8 16H3v5"/>
            </svg>
            <span v-if="loading" class="sm-spinner"></span>
            <span>{{ loading ? 'Đang tải…' : 'Tải lại' }}</span>
          </button>
        </div>
      </header>

      <!-- ── Stat cards ── -->
      <div class="sm-stats">
        <div class="sm-stat sm-stat--blue">
          <div class="sm-stat__icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/>
            </svg>
          </div>
          <div class="sm-stat__num">{{ total }}</div>
          <div class="sm-stat__label">Tổng Seri</div>
        </div>
        <div class="sm-stat sm-stat--green">
          <div class="sm-stat__icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
          </div>
          <div class="sm-stat__num">{{ countInStock }}</div>
          <div class="sm-stat__label">Trong kho</div>
        </div>
        <div class="sm-stat sm-stat--purple">
          <div class="sm-stat__icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
          </div>
          <div class="sm-stat__num">{{ countSold }}</div>
          <div class="sm-stat__label">Đã bán</div>
        </div>
        <div class="sm-stat sm-stat--red">
          <div class="sm-stat__icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          </div>
          <div class="sm-stat__num">{{ countFaulty }}</div>
          <div class="sm-stat__label">Lỗi / Hỏng</div>
        </div>
      </div>

      <!-- ── Filter panel ── -->
      <div class="sm-filter-panel">
        <div class="sm-filter-panel__head">
          <div class="sm-filter-panel__title">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
            </svg>
            Bộ lọc &amp; Tìm kiếm
          </div>
          <button
            class="sm-filter-clear"
            @click="clearFilters"
            :disabled="!keyword && !filterStatus"
          >Xóa bộ lọc</button>
        </div>
        <div class="sm-filter-fields">
          <!-- Search -->
          <div class="sm-filter-field sm-filter-field--search">
            <span class="sm-filter-field__label">Tìm kiếm</span>
            <div class="sm-search-wrap">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" class="sm-search-icon">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              <input
                v-model="keyword"
                class="sm-input"
                placeholder="Số Seri / IMEI…"
                @keyup.enter="load"
              />
              <button v-if="keyword" class="sm-search-clear" @click="keyword = ''; load()">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Status filter -->
          <div class="sm-filter-field">
            <span class="sm-filter-field__label">Trạng thái</span>
            <div class="sm-status-toggle">
              <button
                v-for="opt in statusOptions" :key="opt.value"
                class="sm-status-btn"
                :class="{ 'sm-status-btn--active': filterStatus === opt.value, [`sm-status-btn--${opt.color}`]: filterStatus === opt.value }"
                @click="filterStatus = opt.value; load()"
              >
                <span class="sm-status-dot" :class="`sm-status-dot--${opt.color}`"></span>
                {{ opt.label }}
              </button>
            </div>
          </div>

          <!-- Apply -->
          <div class="sm-filter-field sm-filter-field--action">
            <button class="sm-btn sm-btn--primary sm-btn--sm" @click="load">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              Áp dụng
            </button>
          </div>
        </div>
      </div>

      <!-- ── Table card ── -->
      <div class="sm-card">
        <!-- Toolbar -->
        <div class="sm-toolbar">
          <div class="sm-toolbar__left">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/>
            </svg>
            <span>Danh sách Số Seri</span>
            <span v-if="filterStatus" class="sm-active-filter-chip">
              {{ statusOptions.find(o => o.value === filterStatus)?.label }}
              <button class="sm-chip-remove" @click="filterStatus = ''; load()">×</button>
            </span>
          </div>
          <div class="sm-count">
            <span class="sm-count__num">{{ total }}</span>
            <span class="sm-count__label">seri</span>
          </div>
        </div>

        <!-- Table -->
        <div class="sm-table-wrap" :class="{ 'sm-table-wrap--loading': loading }">
          <div class="sm-loader-overlay" v-if="loading">
            <div class="sm-loader-ring"></div>
          </div>

          <table class="sm-table">
            <thead>
              <tr>
                <th style="width:64px; text-align:center">ID</th>
                <th style="min-width:200px">Số Seri / IMEI</th>
                <th style="min-width:260px">Thông tin sản phẩm</th>
                <th style="width:150px; text-align:center">Trạng thái</th>
                <th style="width:180px; text-align:center">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <!-- Empty -->
              <tr v-if="!loading && rows.length === 0">
                <td colspan="5" class="sm-empty">
                  <div class="sm-empty__inner">
                    <div class="sm-empty__icon">
                      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4">
                        <rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/>
                      </svg>
                    </div>
                    <p>Không tìm thấy Seri nào</p>
                  </div>
                </td>
              </tr>

              <tr v-for="row in rows" :key="row.id" class="sm-row">
                <!-- ID -->
                <td style="text-align:center">
                  <span class="sm-id">#{{ row.id }}</span>
                </td>

                <!-- Serial -->
                <td>
                  <div class="sm-serial-cell">
                    <span class="sm-serial-num">{{ row.serialNumber }}</span>
                    <button class="sm-copy-btn" @click="copySerial(row.serialNumber)" :title="'Sao chép ' + row.serialNumber">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                        <rect x="9" y="9" width="13" height="13" rx="2"/>
                        <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                      </svg>
                    </button>
                  </div>
                </td>

                <!-- Product info -->
                <td>
                  <div class="sm-product-cell">
                    <span class="sm-product-name">{{ row.productName }}</span>
                    <div class="sm-variant-tag">
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="2" y1="12" x2="22" y2="12"/>
                        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                      </svg>
                      {{ row.variantName }}
                    </div>
                  </div>
                </td>

                <!-- Status -->
                <td style="text-align:center">
                  <span class="sm-status-tag" :class="getStatusClass(row.status)">
                    <span class="sm-tag-dot"></span>
                    {{ formatStatus(row.status) }}
                  </span>
                </td>

                <!-- Actions -->
                <td style="text-align:center">
                  <div class="sm-action-group">
                    <button
                      v-for="opt in statusActionOptions(row.status)"
                      :key="opt.value"
                      class="sm-action-btn"
                      :class="`sm-action-btn--${opt.color}`"
                      @click="handleStatusUpdate(row, opt.value)"
                    >{{ opt.label }}</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="sm-pagination" v-if="total > 0">
          <span class="sm-pagination__info">
            {{ (page - 1) * 20 + 1 }}–{{ Math.min(page * 20, total) }} / {{ total }}
          </span>
          <div class="sm-pagination__controls">
            <button class="sm-page-btn" :disabled="page === 1" @click="page--; load()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="15 18 9 12 15 6"/>
              </svg>
            </button>
            <button
              v-for="p in pageNumbers"
              :key="p"
              class="sm-page-btn"
              :class="{ 'sm-page-btn--active': p === page }"
              @click="page = p; load()"
            >{{ p }}</button>
            <button class="sm-page-btn" :disabled="page * 20 >= total" @click="page++; load()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { serialsApi } from '../../api/serials.api';
import { toast } from '../../ui/toast';

const loading = ref(false);
const rows    = ref([]);
const total   = ref(0);
const page    = ref(1);
const keyword = ref('');
const filterStatus = ref('');

const statusOptions = [
  { value: '',         label: 'Tất cả',    color: 'gray'   },
  { value: 'IN_STOCK', label: 'Trong kho', color: 'green'  },
  { value: 'SOLD',     label: 'Đã bán',    color: 'purple' },
  { value: 'FAULTY',   label: 'Lỗi / Hỏng',color: 'red'   },
];

const countInStock = computed(() => rows.value.filter(r => r.status === 'IN_STOCK').length);
const countSold    = computed(() => rows.value.filter(r => r.status === 'SOLD').length);
const countFaulty  = computed(() => rows.value.filter(r => r.status === 'FAULTY').length);

const pageNumbers = computed(() => {
  const totalPages = Math.ceil(total.value / 20);
  const p = page.value;
  const range = [];
  for (let i = Math.max(1, p - 2); i <= Math.min(totalPages, p + 2); i++) range.push(i);
  return range;
});

async function load() {
  loading.value = true;
  try {
    const res = await serialsApi.list({
      page: page.value - 1,
      keyword: keyword.value,
      status: filterStatus.value || undefined,
    });
    const data = res.data?.data || res.data;
    rows.value  = data.content;
    total.value = data.totalElements;
  } catch {
    toast('Lỗi tải dữ liệu Seri', 'error');
  } finally {
    loading.value = false;
  }
}

function onPageChange() { load(); }

async function handleStatusUpdate(row, newStatus) {
  try {
    await serialsApi.updateStatus(row.id, newStatus);
    toast(`Đã đổi trạng thái Seri sang ${formatStatus(newStatus)}`, 'success');
    load();
  } catch {
    toast('Cập nhật thất bại', 'error');
  }
}

function clearFilters() {
  keyword.value = '';
  filterStatus.value = '';
  page.value = 1;
  load();
}

function copySerial(serial) {
  navigator.clipboard?.writeText(serial);
  toast('Đã sao chép Seri', 'success');
}

function getStatusClass(status) {
  switch (status) {
    case 'IN_STOCK': return 'sm-status-tag--green';
    case 'SOLD':     return 'sm-status-tag--purple';
    case 'FAULTY':   return 'sm-status-tag--red';
    default:         return 'sm-status-tag--gray';
  }
}

function formatStatus(status) {
  switch (status) {
    case 'IN_STOCK': return 'Trong kho';
    case 'SOLD':     return 'Đã bán';
    case 'FAULTY':   return 'Lỗi / Hỏng';
    default:         return status;
  }
}

function statusActionOptions(currentStatus) {
  return [
    { value: 'IN_STOCK', label: 'Nhập kho',  color: 'green'  },
    { value: 'FAULTY',   label: 'Đánh lỗi',  color: 'red'    },
    { value: 'SOLD',     label: 'Đã bán',     color: 'purple' },
  ].filter(o => o.value !== currentStatus);
}

onMounted(load);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

/* ─────────────────────────────────────────
   HARDCODE COLOR MAP (không dùng var())
   bg:#f6f7f9  card:#fff  border:#e4e7ec
   border-light:#f0f2f5  text:#0f1117
   muted:#6b7280  subtle:#9ca3af
   blue:#2563eb / bg:#eff6ff / bdr:#bfdbfe
   green:#16a34a / bg:#f0fdf4 / bdr:#bbf7d0
   red:#dc2626 / bg:#fff1f2 / bdr:#fecdd3
   orange:#d97706 / bg:#fffbeb / bdr:#fde68a
   purple:#7c3aed / bg:#f5f3ff / bdr:#ddd6fe
   radius:12px  radius-sm:8px
───────────────────────────────────────── */

.sm-page {
  font-family: 'Plus Jakarta Sans', sans-serif;
  background: #f6f7f9;
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: #0f1117;
  box-sizing: border-box;
}
*, *::before, *::after { box-sizing: border-box; }

.sm-inner { max-width: 1440px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }

/* ── Header ── */
.sm-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; flex-wrap: wrap; }
.sm-eyebrow { display: inline-flex; align-items: center; gap: 5px; font-size: 11px; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; color: #6b7280; margin-bottom: 6px; }
.sm-title { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; margin: 0 0 4px; line-height: 1.2; }
.sm-title__accent { color: #2563eb; }
.sm-subtitle { font-size: 13.5px; color: #6b7280; margin: 0; }
.sm-header__actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding-top: 6px; }

/* ── Buttons ── */
.sm-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 16px; font-size: 13px; font-weight: 700;
  border-radius: 8px; border: none; cursor: pointer;
  transition: all 0.18s ease; font-family: 'Plus Jakarta Sans', sans-serif; white-space: nowrap;
}
.sm-btn--sm { padding: 7px 14px; font-size: 12.5px; }
.sm-btn--outline { background: #ffffff; color: #0f1117; border: 1.5px solid #e4e7ec; }
.sm-btn--outline:hover:not(:disabled) { border-color: #9ca3af; background: #f9fafb; }
.sm-btn--outline:disabled { opacity: 0.5; cursor: not-allowed; }
.sm-btn--primary { background: linear-gradient(135deg, #1d4ed8, #2563eb); color: #fff; box-shadow: 0 4px 12px rgba(37,99,235,0.3); }
.sm-btn--primary:hover:not(:disabled) { background: linear-gradient(135deg, #1e40af, #1d4ed8); transform: translateY(-1px); box-shadow: 0 6px 18px rgba(37,99,235,0.35); }

/* ── Stat cards ── */
.sm-stats { display: grid; grid-template-columns: repeat(auto-fill, minmax(170px, 1fr)); gap: 14px; }
.sm-stat {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 8px;
  padding: 18px 16px; display: flex; flex-direction: column; gap: 6px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  transition: transform 0.15s;
}
.sm-stat:hover { transform: translateY(-1px); }
.sm-stat--blue   { border-top: 3px solid #2563eb; }
.sm-stat--green  { border-top: 3px solid #16a34a; }
.sm-stat--red    { border-top: 3px solid #dc2626; }
.sm-stat--purple { border-top: 3px solid #7c3aed; }
.sm-stat__icon { display: flex; align-items: center; margin-bottom: 2px; }
.sm-stat--blue   .sm-stat__icon { color: #2563eb; }
.sm-stat--green  .sm-stat__icon { color: #16a34a; }
.sm-stat--red    .sm-stat__icon { color: #dc2626; }
.sm-stat--purple .sm-stat__icon { color: #7c3aed; }
.sm-stat__num { font-size: 26px; font-weight: 800; letter-spacing: -0.03em; line-height: 1; font-family: 'JetBrains Mono', monospace; }
.sm-stat--blue   .sm-stat__num { color: #2563eb; }
.sm-stat--green  .sm-stat__num { color: #16a34a; }
.sm-stat--red    .sm-stat__num { color: #dc2626; }
.sm-stat--purple .sm-stat__num { color: #7c3aed; }
.sm-stat__label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: #6b7280; }

/* ── Filter panel ── */
.sm-filter-panel {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}
.sm-filter-panel__head { display: flex; align-items: center; justify-content: space-between; padding: 10px 16px; border-bottom: 1px solid #f0f2f5; }
.sm-filter-panel__title { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 700; color: #0f1117; }
.sm-filter-clear { font-size: 12px; color: #9ca3af; background: none; border: none; cursor: pointer; padding: 0; transition: color 0.15s; font-family: 'Plus Jakarta Sans', sans-serif; font-weight: 600; }
.sm-filter-clear:hover:not(:disabled) { color: #dc2626; }
.sm-filter-clear:disabled { opacity: 0.4; cursor: not-allowed; }
.sm-filter-fields { display: grid; grid-template-columns: 1fr 1.6fr auto; align-items: end; }
.sm-filter-field { padding: 12px 16px; border-right: 1px solid #f0f2f5; display: flex; flex-direction: column; gap: 7px; }
.sm-filter-field:last-child { border-right: none; }
.sm-filter-field--action { justify-content: flex-end; padding-bottom: 14px; }
.sm-filter-field__label { font-size: 10.5px; font-weight: 700; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.06em; }

/* Search input */
.sm-search-wrap { position: relative; display: flex; align-items: center; }
.sm-search-icon { position: absolute; left: 10px; color: #9ca3af; pointer-events: none; }
.sm-input {
  width: 100%; padding: 8px 32px 8px 34px; font-size: 13px;
  font-family: 'Plus Jakarta Sans', sans-serif; border: 1.5px solid #e4e7ec;
  border-radius: 8px; background: #fafbfc; color: #0f1117; outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.sm-input:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,0.08); background: #fff; }
.sm-input::placeholder { color: #9ca3af; }
.sm-search-clear {
  position: absolute; right: 8px; background: none; border: none; cursor: pointer;
  color: #9ca3af; padding: 2px; display: flex; align-items: center; transition: color 0.15s;
}
.sm-search-clear:hover { color: #0f1117; }

/* Status toggle buttons */
.sm-status-toggle { display: flex; gap: 6px; flex-wrap: wrap; }
.sm-status-btn {
  display: inline-flex; align-items: center; gap: 6px; padding: 6px 13px;
  font-size: 12px; font-weight: 600; border: 1.5px solid #e4e7ec; border-radius: 999px;
  background: #ffffff; color: #6b7280; cursor: pointer;
  transition: all 0.15s; font-family: 'Plus Jakarta Sans', sans-serif;
}
.sm-status-btn:hover { border-color: #9ca3af; color: #0f1117; }
.sm-status-btn--active { font-weight: 700; }
.sm-status-btn--gray   { background: #f3f4f6;  color: #374151;  border-color: #e5e7eb; }
.sm-status-btn--green  { background: #f0fdf4;  color: #16a34a;  border-color: #bbf7d0; }
.sm-status-btn--purple { background: #f5f3ff;  color: #7c3aed;  border-color: #ddd6fe; }
.sm-status-btn--red    { background: #fff1f2;  color: #dc2626;  border-color: #fecdd3; }

.sm-status-dot { width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0; }
.sm-status-dot--gray   { background: #9ca3af; }
.sm-status-dot--green  { background: #16a34a; }
.sm-status-dot--purple { background: #7c3aed; }
.sm-status-dot--red    { background: #dc2626; }

/* ── Card ── */
.sm-card {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}
.sm-toolbar { display: flex; align-items: center; justify-content: space-between; padding: 13px 20px; border-bottom: 1px solid #f0f2f5; background: #fafbfc; }
.sm-toolbar__left { display: flex; align-items: center; gap: 8px; font-size: 12.5px; font-weight: 700; color: #6b7280; }
.sm-active-filter-chip {
  display: inline-flex; align-items: center; gap: 4px;
  background: #eff6ff; border: 1px solid #bfdbfe; color: #2563eb;
  padding: 2px 8px 2px 10px; border-radius: 999px; font-size: 11.5px; font-weight: 600;
}
.sm-chip-remove { background: none; border: none; color: #2563eb; cursor: pointer; font-size: 14px; padding: 0 2px; line-height: 1; opacity: 0.65; }
.sm-chip-remove:hover { opacity: 1; }
.sm-count { display: flex; align-items: baseline; gap: 4px; }
.sm-count__num { font-size: 17px; font-weight: 800; color: #0f1117; }
.sm-count__label { font-size: 12px; color: #9ca3af; font-weight: 600; }

/* ── Table ── */
.sm-table-wrap { overflow-x: auto; position: relative; min-height: 120px; }
.sm-table-wrap--loading { pointer-events: none; }
.sm-loader-overlay {
  position: absolute; inset: 0; background: rgba(255,255,255,0.78);
  backdrop-filter: blur(2px); display: flex; align-items: center; justify-content: center; z-index: 10;
}
.sm-loader-ring {
  width: 36px; height: 36px; border: 3px solid #e4e7ec;
  border-top-color: #2563eb; border-radius: 50%;
  animation: sm-spin 0.7s linear infinite;
}
@keyframes sm-spin { to { transform: rotate(360deg); } }

.sm-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.sm-table thead tr { border-bottom: 2px solid #f0f2f5; }
.sm-table th {
  padding: 11px 16px; text-align: left; font-size: 10.5px; font-weight: 700;
  letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af;
  background: #fafbfc; white-space: nowrap;
}
.sm-table td { padding: 13px 16px; vertical-align: middle; border-bottom: 1px solid #f0f2f5; }
.sm-row { transition: background 0.12s; }
.sm-row:hover { background: #fafbfc; }
.sm-row:last-child td { border-bottom: none; }

/* Cell styles */
.sm-id {
  font-family: 'JetBrains Mono', monospace; font-size: 11.5px; font-weight: 600;
  color: #2563eb; background: #eff6ff; padding: 2px 7px; border-radius: 5px;
}

.sm-serial-cell { display: flex; align-items: center; gap: 8px; }
.sm-serial-num {
  font-family: 'JetBrains Mono', monospace; font-size: 13px; font-weight: 600;
  color: #0f1117; letter-spacing: 0.04em;
}
.sm-copy-btn {
  display: inline-flex; align-items: center; justify-content: center;
  width: 24px; height: 24px; border: 1.5px solid #e4e7ec; border-radius: 6px;
  background: #f6f7f9; color: #9ca3af; cursor: pointer; flex-shrink: 0;
  transition: all 0.15s;
}
.sm-copy-btn:hover { background: #eff6ff; border-color: #bfdbfe; color: #2563eb; }

.sm-product-cell { display: flex; flex-direction: column; gap: 4px; }
.sm-product-name { font-size: 13.5px; font-weight: 700; color: #0f1117; }
.sm-variant-tag {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 11px; font-weight: 600; color: #7c3aed;
  background: #f5f3ff; border: 1px solid #ddd6fe;
  padding: 2px 8px; border-radius: 999px; width: fit-content;
}

/* Status tags */
.sm-status-tag {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 4px 11px; border-radius: 999px; font-size: 11.5px; font-weight: 700;
  white-space: nowrap; border: 1px solid transparent;
}
.sm-tag-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.sm-status-tag--green  { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }
.sm-status-tag--green  .sm-tag-dot { background: #16a34a; }
.sm-status-tag--purple { background: #f5f3ff; color: #7c3aed; border-color: #ddd6fe; }
.sm-status-tag--purple .sm-tag-dot { background: #7c3aed; }
.sm-status-tag--red    { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.sm-status-tag--red    .sm-tag-dot { background: #dc2626; }
.sm-status-tag--gray   { background: #f3f4f6; color: #6b7280; border-color: #e5e7eb; }
.sm-status-tag--gray   .sm-tag-dot { background: #9ca3af; }

/* Action buttons */
.sm-action-group { display: flex; gap: 5px; justify-content: center; flex-wrap: wrap; }
.sm-action-btn {
  display: inline-flex; align-items: center; gap: 4px; padding: 5px 10px;
  font-size: 11px; font-weight: 700; border: 1.5px solid transparent;
  border-radius: 7px; cursor: pointer; transition: all 0.15s ease;
  font-family: 'Plus Jakarta Sans', sans-serif; white-space: nowrap;
}
.sm-action-btn--green  { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }
.sm-action-btn--green:hover  { background: #16a34a; color: #fff; border-color: #16a34a; transform: translateY(-1px); }
.sm-action-btn--red    { background: #fff1f2; color: #dc2626; border-color: #fecdd3; }
.sm-action-btn--red:hover    { background: #dc2626; color: #fff; border-color: #dc2626; transform: translateY(-1px); }
.sm-action-btn--purple { background: #f5f3ff; color: #7c3aed; border-color: #ddd6fe; }
.sm-action-btn--purple:hover { background: #7c3aed; color: #fff; border-color: #7c3aed; transform: translateY(-1px); }

/* Empty */
.sm-empty { text-align: center; padding: 60px 20px !important; }
.sm-empty__inner { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.sm-empty__icon {
  width: 54px; height: 54px; border-radius: 50%; background: #f0f2f5;
  display: flex; align-items: center; justify-content: center; color: #9ca3af;
}
.sm-empty__inner p { color: #6b7280; font-size: 14px; margin: 0; font-weight: 500; }

/* Pagination */
.sm-pagination {
  display: flex; align-items: center; justify-content: space-between;
  padding: 13px 20px; border-top: 1px solid #f0f2f5; background: #fafbfc; flex-wrap: wrap; gap: 10px;
}
.sm-pagination__info { font-size: 12.5px; color: #9ca3af; font-weight: 600; }
.sm-pagination__controls { display: flex; align-items: center; gap: 4px; }
.sm-page-btn {
  min-width: 34px; height: 34px; display: inline-flex; align-items: center; justify-content: center;
  border: 1.5px solid #e4e7ec; border-radius: 8px; background: #ffffff;
  font-family: 'Plus Jakarta Sans', sans-serif; font-size: 13px; font-weight: 700;
  color: #0f1117; cursor: pointer; transition: all 0.15s; padding: 0 6px;
}
.sm-page-btn:hover:not(:disabled) { border-color: #2563eb; color: #2563eb; }
.sm-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.sm-page-btn--active { background: #0f1117; border-color: #0f1117; color: #ffffff; }

/* Spinner */
.sm-spinner {
  display: inline-block; width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.1); border-top-color: #6b7280;
  border-radius: 50%; animation: sm-spin 0.6s linear infinite; flex-shrink: 0;
}

/* Responsive */
@media (max-width: 900px) {
  .sm-page { padding: 20px 16px 40px; }
  .sm-filter-fields { grid-template-columns: 1fr; }
  .sm-filter-field { border-right: none; border-bottom: 1px solid #f0f2f5; }
  .sm-filter-field:last-child { border-bottom: none; }
}
</style>