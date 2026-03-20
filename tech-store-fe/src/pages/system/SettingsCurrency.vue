<template>
  <div class="sc-page">

    <!-- ── Header ── -->
    <header class="sc-header">
      <div class="sc-header__left">
        <div class="sc-eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          Admin
        </div>
        <h1 class="sc-title">Settings — <span class="sc-title__accent">Default Currency</span></h1>
        <p class="sc-subtitle">Cấu hình đơn vị tiền tệ mặc định cho toàn hệ thống</p>
      </div>

      <div class="sc-header__right">
        <button class="sc-btn sc-btn--outline" :class="{ 'sc-btn--loading': loading }" @click="load" :disabled="loading">
          <svg v-if="!loading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
            <path d="M21 3v5h-5"/>
            <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
            <path d="M8 16H3v5"/>
          </svg>
          <span v-if="!loading">Reload</span>
          <span v-else class="sc-spinner"></span>
        </button>
      </div>
    </header>

    <!-- ── Main card ── -->
    <div class="sc-card">

      <!-- Form section -->
      <div class="sc-card__head">
        <span class="sc-card__title">Đơn vị tiền tệ mặc định</span>
      </div>

      <div class="sc-card__body">
        <div class="sc-form-row">
          <div class="sc-field">
            <label class="sc-field__label">Currency Code <span class="sc-required">*</span></label>
            <div class="sc-select-wrap">
              <select v-model="currencyCode" class="sc-select">
                <option value="VND">🇻🇳  VND — Việt Nam Đồng</option>
                <option value="USD">🇺🇸  USD — US Dollar</option>
                <option value="EUR">🇪🇺  EUR — Euro</option>
                <option value="JPY">🇯🇵  JPY — Japanese Yen</option>
                <option value="THB">🇹🇭  THB — Thai Baht</option>
              </select>
              <svg class="sc-select-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </div>
            <span class="sc-field__hint">Áp dụng cho toàn bộ trang web và tính giá</span>
          </div>

          <!-- Preview chip -->
          <div class="sc-preview-chip" v-if="currencyCode">
            <span class="sc-preview-chip__label">Hiện tại</span>
            <span class="sc-preview-chip__value">{{ currencyCode }}</span>
          </div>
        </div>

        <div class="sc-form-actions">
          <button class="sc-btn sc-btn--primary" :class="{ 'sc-btn--loading': saving }" @click="save" :disabled="saving">
            <svg v-if="!saving" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
              <polyline points="17 21 17 13 7 13 7 21"/>
              <polyline points="7 3 7 8 15 8"/>
            </svg>
            <span v-if="!saving">Lưu thay đổi</span>
            <span v-else class="sc-spinner sc-spinner--white"></span>
          </button>
        </div>
      </div>

      <!-- Divider -->
      <div class="sc-divider"></div>

      <!-- Raw JSON -->
      <div class="sc-card__head">
        <span class="sc-card__title">Raw Response</span>
        <span class="sc-json-badge">JSON</span>
      </div>
      <div class="sc-json-wrap">
        <pre class="sc-json">{{ raw ? JSON.stringify(raw, null, 2) : 'Chưa tải dữ liệu...' }}</pre>
      </div>

    </div>

  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { settingsApi } from "../../api/settings.api";
import { toast } from "../../ui/toast";

const loading = ref(false);
const saving  = ref(false);

const currencyCode = ref("VND");
const raw          = ref(null);

function pickCurrency(payload) {
  const root = payload?.data ?? payload;
  return (
    root?.currencyCode ??
    root?.data?.currencyCode ??
    root?.data?.data?.currencyCode ??
    null
  );
}

async function load() {
  loading.value = true;
  try {
    const res = await settingsApi.getDefaultCurrency();
    raw.value = res?.data ?? null;
    const c = pickCurrency(res?.data);
    if (c) currencyCode.value = String(c);
  } catch {
    toast("Failed to load default currency.", "error");
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (!currencyCode.value) return toast("currencyCode is required.", "warning");
  saving.value = true;
  try {
    await settingsApi.setDefaultCurrency(currencyCode.value);
    toast("Default currency updated.", "success");
    await load();
  } catch {
    toast("Update failed.", "error");
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.sc-page {
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
  --radius:          12px;
  --radius-sm:       8px;
  --shadow-sm:       0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--c-bg);
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: var(--c-text);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 800px;
}

/* ── Header ──────────────────────────────── */
.sc-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}
.sc-eyebrow {
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
.sc-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
  line-height: 1.2;
}
.sc-title__accent { color: var(--c-blue); }
.sc-subtitle { font-size: 13.5px; color: var(--c-muted); margin: 0; }
.sc-header__right { flex-shrink: 0; padding-top: 6px; }

/* ── Card ────────────────────────────────── */
.sc-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.sc-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
}
.sc-card__title {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--c-text);
}
.sc-card__body {
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.sc-divider {
  height: 1px;
  background: var(--c-border-light);
}

/* ── Form ────────────────────────────────── */
.sc-form-row {
  display: flex;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}
.sc-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  min-width: 200px;
  max-width: 320px;
}
.sc-field__label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--c-muted);
}
.sc-required { color: #dc2626; margin-left: 2px; }
.sc-field__hint {
  font-size: 11.5px;
  color: var(--c-muted);
}

/* Select */
.sc-select-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.sc-select {
  width: 100%;
  padding: 10px 36px 10px 12px;
  font-size: 13.5px;
  font-family: "Plus Jakarta Sans", sans-serif;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-card);
  color: var(--c-text);
  appearance: none;
  cursor: pointer;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.sc-select:focus {
  border-color: var(--c-blue);
  box-shadow: 0 0 0 3px rgba(37,99,235,0.08);
}
.sc-select-arrow {
  position: absolute;
  right: 10px;
  pointer-events: none;
  color: var(--c-subtle);
}

/* Preview chip */
.sc-preview-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 20px;
  background: var(--c-blue-bg);
  border: 1px solid var(--c-blue-border);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.sc-preview-chip__label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--c-blue);
}
.sc-preview-chip__value {
  font-family: "JetBrains Mono", monospace;
  font-size: 18px;
  font-weight: 800;
  color: var(--c-blue);
  letter-spacing: 0.05em;
}

.sc-form-actions {
  display: flex;
  justify-content: flex-end;
}

/* ── JSON section ────────────────────────── */
.sc-json-badge {
  font-family: "JetBrains Mono", monospace;
  font-size: 10px;
  font-weight: 600;
  color: var(--c-muted);
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  padding: 2px 8px;
  border-radius: 5px;
}
.sc-json-wrap { padding: 0; }
.sc-json {
  margin: 0;
  font-family: "JetBrains Mono", monospace;
  font-size: 12px;
  line-height: 1.7;
  color: var(--c-muted);
  background: #fafbfc;
  border: none;
  padding: 16px 20px;
  overflow-x: auto;
  white-space: pre;
}

/* ── Buttons ─────────────────────────────── */
.sc-btn {
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
}
.sc-btn--primary {
  background: var(--c-blue);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,0.22);
}
.sc-btn--primary:hover { background: #1d4ed8; transform: translateY(-1px); box-shadow: 0 4px 14px rgba(37,99,235,0.3); }
.sc-btn--outline {
  background: var(--c-card);
  color: var(--c-text);
  border: 1.5px solid var(--c-border);
}
.sc-btn--outline:hover { border-color: var(--c-subtle); background: #f9fafb; }
.sc-btn--loading, .sc-btn:disabled { opacity: 0.65; pointer-events: none; transform: none !important; }

/* ── Spinner ─────────────────────────────── */
.sc-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.12);
  border-top-color: var(--c-muted);
  border-radius: 50%;
  animation: sc-spin 0.6s linear infinite;
  flex-shrink: 0;
}
.sc-spinner--white {
  border-color: rgba(255,255,255,0.25);
  border-top-color: #fff;
}
@keyframes sc-spin { to { transform: rotate(360deg); } }
</style>