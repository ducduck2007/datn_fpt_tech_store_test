<template>
  <div class="login-root">
    <!-- Background -->
    <div class="bg-layer">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
      <div class="bg-grid"></div>
      <div class="scan-line"></div>
    </div>

    <!-- Card -->
    <div class="login-card" :class="{ 'card-shake': shaking }">
      <!-- Accent top bar -->
      <div class="card-accent-bar"></div>

      <!-- Header -->
      <div class="card-header">
        <div class="brand-badge">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
          </svg>
          <span>Cổng hệ thống</span>
        </div>
        <h1 class="card-title">Truy cập <em>hệ thống</em></h1>
        <p class="card-subtitle">Xác thực nhân viên &amp; quản trị</p>
      </div>

      <!-- Alert -->
      <transition name="alert-slide">
        <div v-if="alert" class="error-alert">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <line x1="12" y1="9" x2="12" y2="13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="12" cy="17" r="0.5" fill="currentColor" stroke="currentColor" stroke-width="1"/>
          </svg>
          <span>{{ alert }}</span>
        </div>
      </transition>

      <!-- Form -->
      <form class="login-form" @submit.prevent="doLogin">
        <div class="field-group">
          <label class="field-label">Tên đăng nhập</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'identifier' }">
            <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
              <path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.identifier"
              type="text"
              class="field-input"
              placeholder="ví dụ: admin / staff@mail.com"
              autocomplete="username"
              @focus="focusedField = 'identifier'"
              @blur="focusedField = ''"
            />
          </div>
        </div>

        <div class="field-group">
          <label class="field-label">Mật khẩu</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'password' }">
            <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="12" cy="16" r="1" fill="currentColor"/>
            </svg>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="field-input"
              placeholder="••••••••••"
              autocomplete="current-password"
              @focus="focusedField = 'password'"
              @blur="focusedField = ''"
            />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
              <svg v-if="!showPassword" width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <button type="submit" class="login-btn" :class="{ loading }" :disabled="loading">
          <span class="btn-content">
            <svg v-if="!loading" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            </svg>
            <span v-else class="spinner"></span>
            {{ loading ? 'Đang xác thực...' : 'Đăng nhập' }}
          </span>
          <div class="btn-shimmer"></div>
        </button>
      </form>

      <!-- Footer -->
      <div class="card-footer">
        <div class="role-hint">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
            <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>HỆ THỐNG → Dashboard &nbsp;·&nbsp; SALES → Bán hàng &nbsp;·&nbsp; KHO → Đơn hàng</span>
        </div>
        <div class="divider-line"><span>hoặc</span></div>
        <button class="link-btn customer" @click="router.push('/login')">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          Đến trang đăng nhập khách hàng
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "../../ui/toast";
import { authApi } from "../../api/auth.api";
import { useAuthStore } from "../../stores/auth";

const router = useRouter();
const auth = useAuthStore();

const loading = ref(false);
const alert = ref("");
const shaking = ref(false);
const focusedField = ref("");
const showPassword = ref(false);

const form = reactive({
  identifier: "",
  password: "",
});

function triggerShake() {
  shaking.value = true;
  setTimeout(() => (shaking.value = false), 600);
}

function errToText(e) {
  const msg = e?.response?.data?.message || e?.message || "Đăng nhập thất bại";
  return typeof msg === "string" ? msg : JSON.stringify(msg);
}

function pickTokenUser(payload) {
  const root = payload?.data ?? payload;
  const token =
    root?.token ?? root?.data?.token ?? payload?.token ?? payload?.data?.data?.token;
  const user =
    root?.user ?? root?.data?.user ?? payload?.user ?? payload?.data?.data?.user;
  return { token, user };
}

async function doLogin() {
  loading.value = true;
  alert.value = "";
  try {
    const res = await authApi.login({ ...form });
    const data = res?.data;
    const { token, user } = pickTokenUser(data);

    const rawRole = user?.role?.name ?? user?.role?.roleName ?? user?.role ?? "";
  const role = String(rawRole).toUpperCase();
    if (role === "CUSTOMER") {
      toast("Đây là tài khoản KHÁCH HÀNG. Chuyển hướng tới cổng khách hàng.", "warning");
      return router.replace("/login");
    }

    auth.setLastAuthResponse(data);
    auth.setSession({ token, user });

    toast("Đăng nhập hệ thống thành công.", "success");

    if (role === "INVENTORY") {
      router.replace("/inventory/orders/paid");
    } else if (role === "SALES") {
      router.replace("/sales/pos");
    } else {
      router.replace("/system/dashboard");
    }
  } catch (e) {
    alert.value = errToText(e);
    triggerShake();
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
/*
 * ⚠️  @import Google Fonts đã bị XÓA khỏi đây.
 *
 * Lý do: @import trong <style scoped> được Vite xử lý muộn hơn,
 * gây ra FOUT (Flash of Unstyled Text) hoặc font không load được.
 *
 * ✅ Thay vào đó, thêm vào public/index.html (trong <head>):
 *
 *   <link rel="preconnect" href="https://fonts.googleapis.com">
 *   <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
 *   <link href="https://fonts.googleapis.com/css2?family=Syne:wght@400;500;600;700&family=DM+Mono:ital,wght@0,300;0,400;1,300&display=swap" rel="stylesheet">
 */

/* ─── CSS Font Variables ───────────────────────────────── */
:root {
  --font-display: 'Syne', 'Outfit', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --font-mono:    'DM Mono', 'Fira Code', 'Cascadia Code', ui-monospace, 'Courier New', monospace;
}

/* ─── Root ─────────────────────────────────────────────── */
.login-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef4f0;
  font-family: var(--font-display);
  position: relative;
  overflow: hidden;
  padding: 24px;
}

/* ─── Background ────────────────────────────────────────── */
.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.2;
}

.orb-1 {
  width: 600px;
  height: 400px;
  background: radial-gradient(ellipse, #6ee7b7, transparent 70%);
  top: -160px;
  right: -100px;
  animation: drift1 14s ease-in-out infinite alternate;
}

.orb-2 {
  width: 450px;
  height: 450px;
  background: radial-gradient(circle, #a7f3d0, transparent 70%);
  bottom: -150px;
  left: -80px;
  animation: drift2 17s ease-in-out infinite alternate;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, #34d399, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.12;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,180,100,0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,180,100,0.05) 1px, transparent 1px);
  background-size: 40px 40px;
}

.scan-line {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(0, 160, 90, 0.018) 2px,
    rgba(0, 160, 90, 0.018) 4px
  );
  pointer-events: none;
}

@keyframes drift1 {
  from { transform: translate(0, 0); }
  to   { transform: translate(-40px, 50px); }
}
@keyframes drift2 {
  from { transform: translate(0, 0); }
  to   { transform: translate(35px, -45px); }
}

/* ─── Card ──────────────────────────────────────────────── */
.login-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(0, 160, 90, 0.14);
  border-radius: 20px;
  padding: 0 36px 36px;
  backdrop-filter: blur(32px);
  box-shadow:
    0 0 0 1px rgba(255,255,255,0.9) inset,
    0 40px 80px rgba(0, 80, 40, 0.1),
    0 0 60px rgba(0, 180, 100, 0.06);
  animation: cardEnter 0.55s cubic-bezier(0.34, 1.4, 0.64, 1) both;
  overflow: hidden;
}

.card-accent-bar {
  height: 3px;
  background: linear-gradient(90deg, transparent, #00c878 30%, #00b4d8 70%, transparent);
  margin: 0 -36px 36px;
  opacity: 0.7;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(28px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.card-shake {
  animation: shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97);
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  15%       { transform: translateX(-8px); }
  30%       { transform: translateX(7px); }
  45%       { transform: translateX(-5px); }
  60%       { transform: translateX(4px); }
  75%       { transform: translateX(-2px); }
}

/* ─── Header ────────────────────────────────────────────── */
.card-header {
  margin-bottom: 28px;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background: rgba(0, 180, 100, 0.08);
  border: 1px solid rgba(0, 180, 100, 0.22);
  color: #00965a;
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  padding: 5px 12px;
  border-radius: 100px;
  margin-bottom: 20px;
  font-family: var(--font-mono);
}

.card-title {
  font-size: 30px;
  font-weight: 700;
  color: #0f2318;
  margin: 0 0 8px;
  line-height: 1.2;
  letter-spacing: -0.03em;
  font-family: var(--font-display);
}

.card-title em {
  font-style: italic;
  color: #00965a;
  font-weight: 600;
}

.card-subtitle {
  font-size: 13px;
  color: rgba(15, 35, 24, 0.4);
  margin: 0;
  font-weight: 400;
  letter-spacing: 0.01em;
  font-family: var(--font-mono);
}

/* ─── Error Alert ───────────────────────────────────────── */
.error-alert {
  display: flex;
  align-items: center;
  gap: 9px;
  background: rgba(220, 38, 38, 0.06);
  border: 1px solid rgba(220, 38, 38, 0.18);
  color: #b91c1c;
  font-size: 12px;
  padding: 11px 14px;
  border-radius: 10px;
  margin-bottom: 20px;
  font-family: var(--font-mono);
}

.alert-slide-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.2, 0.64, 1); }
.alert-slide-leave-active { transition: all 0.2s ease; }
.alert-slide-enter-from  { opacity: 0; transform: translateY(-8px); }
.alert-slide-leave-to    { opacity: 0; transform: translateY(-4px); }

/* ─── Form ──────────────────────────────────────────────── */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.field-label {
  font-size: 11px;
  font-weight: 600;
  color: rgba(0, 140, 80, 0.65);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  font-family: var(--font-mono);
}

.field-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 10px;
  padding: 0 14px;
  height: 48px;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.field-wrapper.focused {
  border-color: rgba(0, 180, 100, 0.45);
  background: rgba(0, 180, 100, 0.04);
  box-shadow: 0 0 0 3px rgba(0, 180, 100, 0.08);
}

.field-icon {
  color: rgba(15, 35, 24, 0.25);
  flex-shrink: 0;
  transition: color 0.2s;
}

.field-wrapper.focused .field-icon {
  color: rgba(0, 160, 90, 0.75);
}

.field-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #0f2318;
  font-family: var(--font-mono);
  font-size: 13.5px;
  font-weight: 300;
  letter-spacing: 0.02em;
  min-width: 0;
}

.field-input::placeholder {
  color: rgba(15, 35, 24, 0.25);
}

.toggle-password {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: rgba(15, 35, 24, 0.25);
  display: flex;
  align-items: center;
  transition: color 0.2s;
  flex-shrink: 0;
}

.toggle-password:hover {
  color: rgba(0, 160, 90, 0.7);
}

/* ─── Button ────────────────────────────────────────────── */
.login-btn {
  position: relative;
  margin-top: 8px;
  height: 50px;
  border: 1px solid rgba(0, 160, 90, 0.35);
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(0, 180, 100, 0.12) 0%, rgba(0, 160, 80, 0.08) 100%);
  color: #007a50;
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.15s, box-shadow 0.2s, background 0.2s;
  box-shadow: 0 4px 24px rgba(0, 180, 100, 0.1);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  background: linear-gradient(135deg, rgba(0, 180, 100, 0.18) 0%, rgba(0, 160, 80, 0.13) 100%);
  box-shadow: 0 8px 32px rgba(0, 180, 100, 0.18), 0 0 0 1px rgba(0,180,100,0.4) inset;
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 35%, rgba(0,200,120,0.12) 50%, transparent 65%);
  transform: translateX(-100%);
  transition: transform 0.7s;
}

.login-btn:hover .btn-shimmer {
  transform: translateX(100%);
}

.spinner {
  width: 15px;
  height: 15px;
  border: 1.5px solid rgba(0,160,90,0.3);
  border-top-color: #007a50;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ─── Footer ────────────────────────────────────────────── */
.card-footer {
  margin-top: 26px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.role-hint {
  display: flex;
  align-items: center;
  gap: 7px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: rgba(0, 140, 80, 0.45);
  letter-spacing: 0.03em;
  text-align: center;
}

.divider-line {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 10px;
  font-family: var(--font-mono);
  font-size: 10px;
  color: rgba(15, 35, 24, 0.2);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.divider-line::before,
.divider-line::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(0, 0, 0, 0.07);
}

.link-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-family: var(--font-display);
  transition: color 0.15s;
  padding: 0;
}

.link-btn.customer {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(15, 35, 24, 0.35);
  font-size: 12.5px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.link-btn.customer:hover {
  color: rgba(15, 35, 24, 0.65);
}
</style>