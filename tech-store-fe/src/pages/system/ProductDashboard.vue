<template>
  <div class="pa-page">
    <div class="pa-inner">

      <!-- ── Header ── -->
      <header class="pa-header">
        <div class="pa-header__left">
          <div class="pa-eyebrow">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="20" x2="18" y2="10"/>
              <line x1="12" y1="20" x2="12" y2="4"/>
              <line x1="6"  y1="20" x2="6"  y2="14"/>
            </svg>
            Analytics Overview
          </div>
          <h1 class="pa-title">Báo Cáo <span class="pa-title__accent">Tổng Quan Sản Phẩm</span></h1>
          <p class="pa-subtitle">Dữ liệu được cập nhật theo thời gian thực từ hệ thống kho hàng và đơn hàng</p>
        </div>
        <div class="pa-header__actions">
          <button class="pa-btn pa-btn--outline" @click="loadData" :disabled="loading">
            <svg v-if="!loading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
              <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
              <path d="M8 16H3v5"/>
            </svg>
            <span v-if="loading" class="pa-spinner"></span>
            <span>{{ loading ? 'Đang tải…' : 'Làm mới' }}</span>
          </button>
          <button class="pa-btn pa-btn--primary" @click="downloadExcel" :disabled="exporting">
            <svg v-if="!exporting" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            <span v-if="exporting" class="pa-spinner pa-spinner--white"></span>
            <span>{{ exporting ? 'Đang xuất…' : 'Xuất Excel' }}</span>
          </button>
        </div>
      </header>

      <!-- ── Stat cards ── -->
      <div class="pa-stats">

        <div class="pa-stat pa-stat--blue">
          <div class="pa-stat__top">
            <div class="pa-stat__icon-wrap pa-stat__icon-wrap--blue">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="6" height="6" rx="1"/><rect x="9" y="3" width="6" height="6" rx="1"/><rect x="16" y="3" width="6" height="6" rx="1"/>
                <rect x="2" y="11" width="6" height="6" rx="1"/><rect x="9" y="11" width="6" height="6" rx="1"/><rect x="16" y="11" width="6" height="6" rx="1"/>
              </svg>
            </div>
          </div>
          <div class="pa-stat__num">{{ stats.totalActiveProducts }}</div>
          <div class="pa-stat__label">Sản phẩm đang kinh doanh</div>
        </div>

        <div class="pa-stat pa-stat--green">
          <div class="pa-stat__top">
            <div class="pa-stat__icon-wrap pa-stat__icon-wrap--green">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
            </div>
          </div>
          <div class="pa-stat__num">{{ stats.totalSerialsInStock }}</div>
          <div class="pa-stat__label">Tổng số máy trong kho</div>
        </div>

        <div class="pa-stat pa-stat--orange">
          <div class="pa-stat__top">
            <div class="pa-stat__icon-wrap pa-stat__icon-wrap--orange">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
            </div>
          </div>
          <div class="pa-stat__num">{{ stats.outOfStockVariants }}</div>
          <div class="pa-stat__label">Biến thể báo hết hàng</div>
        </div>

        <div class="pa-stat pa-stat--red">
          <div class="pa-stat__top">
            <div class="pa-stat__icon-wrap pa-stat__icon-wrap--red">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9"  y1="9" x2="15" y2="15"/>
              </svg>
            </div>
          </div>
          <div class="pa-stat__num">{{ stats.totalSerialsFaulty }}</div>
          <div class="pa-stat__label">Máy bị lỗi (Faulty)</div>
        </div>

      </div>

      <!-- ── Top selling table ── -->
      <div class="pa-card">

        <!-- Toolbar -->
        <div class="pa-toolbar">
          <div class="pa-toolbar__left">
            <div class="pa-trophy-icon">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                <path d="M6 9H4.5a2.5 2.5 0 010-5H6"/>
                <path d="M18 9h1.5a2.5 2.5 0 000-5H18"/>
                <path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/>
                <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/>
                <path d="M18 2H6v7a6 6 0 0012 0V2z"/>
              </svg>
            </div>
            <span class="pa-toolbar__title">Top 10 Sản Phẩm Bán Chạy Nhất</span>
          </div>
          <div class="pa-rank-badge-label">Xếp hạng theo số lượng</div>
        </div>

        <!-- Table -->
        <div class="pa-table-wrap" :class="{ 'pa-table-wrap--loading': loading }">
          <div class="pa-loader-overlay" v-if="loading">
            <div class="pa-loader-ring"></div>
          </div>

          <table class="pa-table">
            <thead>
              <tr>
                <th style="width:80px; text-align:center">Hạng</th>
                <th style="min-width:320px">Tên sản phẩm</th>
                <th style="width:180px; text-align:center">Đã bán</th>
                <th style="width:240px; text-align:right">Tổng doanh thu</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!loading && stats.topSellingProducts.length === 0">
                <td colspan="4" class="pa-empty">
                  <div class="pa-empty__inner">
                    <div class="pa-empty__icon">
                      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4">
                        <line x1="18" y1="20" x2="18" y2="10"/>
                        <line x1="12" y1="20" x2="12" y2="4"/>
                        <line x1="6"  y1="20" x2="6"  y2="14"/>
                      </svg>
                    </div>
                    <p>Chưa có dữ liệu thống kê</p>
                  </div>
                </td>
              </tr>

              <tr
                v-for="(row, idx) in stats.topSellingProducts"
                :key="idx"
                class="pa-row"
                :class="idx === 0 ? 'pa-row--gold' : idx === 1 ? 'pa-row--silver' : idx === 2 ? 'pa-row--bronze' : ''"
              >
                <!-- Rank -->
                <td style="text-align:center">
                  <div class="pa-rank" :class="idx === 0 ? 'pa-rank--1' : idx === 1 ? 'pa-rank--2' : idx === 2 ? 'pa-rank--3' : 'pa-rank--other'">
                    <span v-if="idx === 0">🥇</span>
                    <span v-else-if="idx === 1">🥈</span>
                    <span v-else-if="idx === 2">🥉</span>
                    <span v-else>{{ idx + 1 }}</span>
                  </div>
                </td>

                <!-- Product name -->
                <td>
                  <div class="pa-product-cell">
                    <span class="pa-product-name">{{ row.productName }}</span>
                  </div>
                </td>

                <!-- Sold -->
                <td style="text-align:center">
                  <span class="pa-sold-tag">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
                      <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>
                    </svg>
                    <strong>{{ row.totalSold }}</strong> chiếc
                  </span>
                </td>

                <!-- Revenue -->
                <td style="text-align:right; padding-right:20px">
                  <span class="pa-revenue">{{ formatCurrency(row.totalRevenue) }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { productsApi } from '../../api/products.api';
import { toast }       from '../../ui/toast';

const loading  = ref(false);
const exporting = ref(false);

const stats = ref({
  totalActiveProducts:  0,
  totalSerialsInStock:  0,
  outOfStockVariants:   0,
  totalSerialsFaulty:   0,
  topSellingProducts:   [],
});

function formatCurrency(val) {
  if (!val) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

async function loadData() {
  loading.value = true;
  try {
    const res = await productsApi.getDashboardStats();
    stats.value = res.data;
  } catch {
    toast('Không thể tải dữ liệu thống kê', 'error');
  } finally {
    loading.value = false;
  }
}

async function downloadExcel() {
  exporting.value = true;
  try {
    const res = await productsApi.exportReport();
    const url  = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `Bao_Cao_San_Pham_${Date.now()}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    toast('Xuất báo cáo Excel thành công!', 'success');
  } catch {
    toast('Lỗi khi xuất file Excel', 'error');
  } finally {
    exporting.value = false;
  }
}

onMounted(loadData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

/*
  HARDCODE MAP — không dùng var()
  bg:#f6f7f9  card:#ffffff  border:#e4e7ec  border-light:#f0f2f5
  text:#0f1117  muted:#6b7280  subtle:#9ca3af
  blue:#2563eb / bg:#eff6ff / bdr:#bfdbfe
  green:#16a34a / bg:#f0fdf4 / bdr:#bbf7d0
  red:#dc2626  / bg:#fff1f2 / bdr:#fecdd3
  orange:#d97706 / bg:#fffbeb / bdr:#fde68a
  radius:12px  radius-sm:8px
  shadow-sm: 0 1px 3px rgba(0,0,0,.06),0 1px 2px rgba(0,0,0,.04)
*/

.pa-page {
  font-family: 'Plus Jakarta Sans', sans-serif;
  background: #f6f7f9;
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: #0f1117;
  box-sizing: border-box;
}
*, *::before, *::after { box-sizing: border-box; }
.pa-inner { max-width: 1440px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }

/* ── Header ── */
.pa-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; flex-wrap: wrap; }
.pa-eyebrow {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 11px; font-weight: 700; letter-spacing: 0.08em;
  text-transform: uppercase; color: #6b7280; margin-bottom: 6px;
}
.pa-title { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; margin: 0 0 4px; line-height: 1.2; }
.pa-title__accent { color: #2563eb; }
.pa-subtitle { font-size: 13.5px; color: #6b7280; margin: 0; }
.pa-header__actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding-top: 6px; }

/* ── Buttons ── */
.pa-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px; font-size: 13px; font-weight: 700;
  border-radius: 8px; border: none; cursor: pointer;
  transition: all 0.18s ease; font-family: 'Plus Jakarta Sans', sans-serif; white-space: nowrap;
}
.pa-btn--outline { background: #ffffff; color: #0f1117; border: 1.5px solid #e4e7ec; }
.pa-btn--outline:hover:not(:disabled) { border-color: #9ca3af; background: #f9fafb; }
.pa-btn--outline:disabled { opacity: 0.5; cursor: not-allowed; }
.pa-btn--primary { background: linear-gradient(135deg, #1d4ed8, #2563eb); color: #fff; box-shadow: 0 4px 12px rgba(37,99,235,0.3); }
.pa-btn--primary:hover:not(:disabled) { background: linear-gradient(135deg, #1e40af, #1d4ed8); transform: translateY(-1px); box-shadow: 0 6px 18px rgba(37,99,235,0.35); }
.pa-btn--primary:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

/* ── Stat cards ── */
.pa-stats { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 14px; }
.pa-stat {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 12px;
  padding: 20px 18px 16px; display: flex; flex-direction: column; gap: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  transition: transform 0.15s, box-shadow 0.15s; position: relative; overflow: hidden;
}
.pa-stat:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08), 0 2px 6px rgba(0,0,0,0.04); }
.pa-stat--blue   { border-top: 3px solid #2563eb; }
.pa-stat--green  { border-top: 3px solid #16a34a; }
.pa-stat--orange { border-top: 3px solid #d97706; }
.pa-stat--red    { border-top: 3px solid #dc2626; }

.pa-stat__top { display: flex; justify-content: flex-end; }
.pa-stat__icon-wrap {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
}
.pa-stat__icon-wrap--blue   { background: #eff6ff; color: #2563eb; }
.pa-stat__icon-wrap--green  { background: #f0fdf4; color: #16a34a; }
.pa-stat__icon-wrap--orange { background: #fffbeb; color: #d97706; }
.pa-stat__icon-wrap--red    { background: #fff1f2; color: #dc2626; }

.pa-stat__num {
  font-size: 32px; font-weight: 800; letter-spacing: -0.04em; line-height: 1;
  font-family: 'JetBrains Mono', monospace;
}
.pa-stat--blue   .pa-stat__num { color: #2563eb; }
.pa-stat--green  .pa-stat__num { color: #16a34a; }
.pa-stat--orange .pa-stat__num { color: #d97706; }
.pa-stat--red    .pa-stat__num { color: #dc2626; }
.pa-stat__label { font-size: 11.5px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: #6b7280; }

/* ── Card ── */
.pa-card {
  background: #ffffff; border: 1px solid #e4e7ec; border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}

/* Toolbar */
.pa-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid #f0f2f5; background: #fafbfc;
}
.pa-toolbar__left { display: flex; align-items: center; gap: 10px; }
.pa-trophy-icon {
  width: 34px; height: 34px; border-radius: 8px;
  background: #fffbeb; color: #d97706;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid #fde68a;
}
.pa-toolbar__title { font-size: 14px; font-weight: 800; color: #0f1117; letter-spacing: -0.01em; }
.pa-rank-badge-label {
  font-size: 11px; font-weight: 700; letter-spacing: 0.05em;
  color: #9ca3af; background: #f0f2f5; border: 1px solid #e4e7ec;
  padding: 3px 10px; border-radius: 999px; text-transform: uppercase;
}

/* Table */
.pa-table-wrap { overflow-x: auto; position: relative; min-height: 120px; }
.pa-table-wrap--loading { pointer-events: none; }
.pa-loader-overlay {
  position: absolute; inset: 0; background: rgba(255,255,255,0.78);
  backdrop-filter: blur(2px); display: flex; align-items: center; justify-content: center; z-index: 10;
}
.pa-loader-ring {
  width: 36px; height: 36px; border: 3px solid #e4e7ec;
  border-top-color: #2563eb; border-radius: 50%; animation: pa-spin 0.7s linear infinite;
}
@keyframes pa-spin { to { transform: rotate(360deg); } }

.pa-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.pa-table thead tr { border-bottom: 2px solid #f0f2f5; }
.pa-table th {
  padding: 11px 16px; text-align: left; font-size: 10.5px; font-weight: 700;
  letter-spacing: 0.07em; text-transform: uppercase; color: #9ca3af; background: #fafbfc; white-space: nowrap;
}
.pa-table td { padding: 14px 16px; vertical-align: middle; border-bottom: 1px solid #f0f2f5; }
.pa-row { transition: background 0.12s; }
.pa-row:hover { background: #fafbfc; }
.pa-row:last-child td { border-bottom: none; }

/* Highlight top 3 */
.pa-row--gold   { background: #fffdf0; }
.pa-row--silver { background: #fafbfc; }
.pa-row--bronze { background: #fdfaf5; }
.pa-row--gold:hover   { background: #fffbeb; }
.pa-row--silver:hover { background: #f3f4f6; }
.pa-row--bronze:hover { background: #fef9f0; }

/* Rank badges */
.pa-rank {
  display: inline-flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 8px;
  font-size: 20px; margin: 0 auto;
}
.pa-rank--1 { font-size: 22px; }
.pa-rank--2 { font-size: 20px; }
.pa-rank--3 { font-size: 20px; }
.pa-rank--other {
  background: #f0f2f5; color: #9ca3af;
  font-size: 12.5px; font-weight: 800;
  font-family: 'JetBrains Mono', monospace;
}

/* Product cell */
.pa-product-cell { padding: 2px 0; }
.pa-product-name { font-size: 13.5px; font-weight: 700; color: #0f1117; }

/* Sold tag */
.pa-sold-tag {
  display: inline-flex; align-items: center; gap: 6px;
  background: #f0fdf4; color: #16a34a;
  border: 1px solid #bbf7d0;
  padding: 5px 12px; border-radius: 999px;
  font-size: 12.5px; font-weight: 600;
}
.pa-sold-tag strong { font-weight: 800; }

/* Revenue */
.pa-revenue {
  font-size: 14px; font-weight: 800; color: #16a34a;
  font-family: 'JetBrains Mono', monospace; letter-spacing: -0.01em;
}

/* Empty */
.pa-empty { text-align: center; padding: 60px 20px !important; }
.pa-empty__inner { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.pa-empty__icon {
  width: 54px; height: 54px; border-radius: 50%; background: #f0f2f5;
  display: flex; align-items: center; justify-content: center; color: #9ca3af;
}
.pa-empty__inner p { color: #6b7280; font-size: 14px; margin: 0; font-weight: 500; }

/* Spinner */
.pa-spinner {
  display: inline-block; width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.1); border-top-color: #6b7280;
  border-radius: 50%; animation: pa-spin 0.6s linear infinite; flex-shrink: 0;
}
.pa-spinner--white { border-color: rgba(255,255,255,0.25); border-top-color: #fff; }

/* Responsive */
@media (max-width: 900px) {
  .pa-page { padding: 20px 16px 40px; }
  .pa-stats { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 500px) {
  .pa-stats { grid-template-columns: 1fr; }
}
</style>