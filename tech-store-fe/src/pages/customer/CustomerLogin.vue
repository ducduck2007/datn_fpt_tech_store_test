<!-- FILE: src/pages/customer/CustomerLogin.vue -->
<!--
  FONT FIX: Thêm vào <head> của index.html:

  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;1,9..40,300&display=swap" rel="stylesheet">
-->
<template>
  <div class="login-root">
    <!-- Background -->
    <div class="bg-layer">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- Card -->
    <div class="login-card" :class="{ 'card-shake': shaking }">
      <!-- Header -->
      <div class="card-header">
        <div class="brand-badge">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M2 17l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
          </svg>
          <span>Cổng khách hàng</span>
        </div>
        <h1 class="card-title">Chào mừng trở lại</h1>
        <p class="card-subtitle">Đăng nhập vào tài khoản của bạn để tiếp tục</p>
      </div>

      <!-- Alert -->
      <transition name="alert-slide">
        <div v-if="alert" class="error-alert">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke="currentColor" stroke-width="1"/>
          </svg>
          <span>{{ alert }}</span>
        </div>
      </transition>

      <!-- Form -->
      <form class="login-form" @submit.prevent="doLogin">
        <div class="field-group">
          <label class="field-label">Email hoặc tên đăng nhập</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'identifier' }">
            <svg class="field-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            <input
              v-model="form.identifier"
              type="text"
              class="field-input"
              placeholder="email hoặc tên đăng nhập"
              autocomplete="username"
              @focus="focusedField = 'identifier'"
              @blur="focusedField = ''"
            />
          </div>
        </div>

        <div class="field-group">
          <label class="field-label">Mật khẩu</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'password' }">
            <svg class="field-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="field-input"
              placeholder="Mật khẩu của bạn"
              autocomplete="current-password"
              @focus="focusedField = 'password'"
              @blur="focusedField = ''"
            />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
              <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <button type="submit" class="login-btn" :class="{ loading }" :disabled="loading">
          <span class="btn-content">
            <svg v-if="!loading" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <polyline points="10 17 15 12 10 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="15" y1="12" x2="3" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span class="spinner" v-else></span>
            {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
          </span>
          <div class="btn-shimmer"></div>
        </button>
      </form>

      <!-- Footer links -->
      <div class="card-footer">
        <div class="footer-row">
          <span class="footer-text">Bạn chưa có tài khoản?</span>
          <button class="link-btn primary" @click="router.push('/register')">Tạo tài khoản</button>
        </div>
        <div class="divider-line">
          <span>hoặc</span>
        </div>
        <button class="link-btn system" @click="router.push('/system/login')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <line x1="8" y1="21" x2="16" y2="21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="12" y1="17" x2="12" y2="21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          System login
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
    if (role !== "CUSTOMER") {
      toast("Tài khoản này không phải khách hàng. Chuyển sang đăng nhập hệ thống.", "warning");
      return router.replace("/system/login");
    }

    auth.setLastAuthResponse(data);
    auth.setSession({ token, user });

    toast("Đăng nhập thành công.", "success");
    router.replace("/");
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
 * ✅ Thêm vào public/index.html (trong <head>):
 *
 *   <link rel="preconnect" href="https://fonts.googleapis.com">
 *   <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
 *   <link href="https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;1,9..40,300&display=swap" rel="stylesheet">
 */

/* ─── CSS Font Variables ───────────────────────────────── */
:root {
  --font-display: 'Instrument Serif', 'Georgia', 'Times New Roman', serif;
  --font-body:    'DM Sans', 'Helvetica Neue', Arial, sans-serif;
}

/* ─── Root ─────────────────────────────────────────────── */
.login-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2fa;
  font-family: var(--font-body);
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
  filter: blur(80px);
  opacity: 0.22;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #a5b8ff, transparent 70%);
  top: -150px;
  left: -100px;
  animation: drift1 12s ease-in-out infinite alternate;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #c4b5fd, transparent 70%);
  bottom: -120px;
  right: -80px;
  animation: drift2 15s ease-in-out infinite alternate;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #7dd3fc, transparent 70%);
  top: 40%;
  left: 60%;
  animation: drift1 18s ease-in-out infinite alternate-reverse;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,0,0,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.04) 1px, transparent 1px);
  background-size: 48px 48px;
}

@keyframes drift1 {
  from { transform: translate(0, 0) scale(1); }
  to   { transform: translate(40px, 30px) scale(1.1); }
}
@keyframes drift2 {
  from { transform: translate(0, 0) scale(1); }
  to   { transform: translate(-30px, -40px) scale(1.08); }
}

/* ─── Card ──────────────────────────────────────────────── */
.login-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 24px;
  padding: 40px;
  backdrop-filter: blur(24px);
  box-shadow:
    0 0 0 1px rgba(255,255,255,0.9) inset,
    0 32px 64px rgba(100, 120, 200, 0.12),
    0 0 80px rgba(79, 110, 247, 0.06);
  animation: cardEnter 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(32px) scale(0.96); }
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
  margin-bottom: 32px;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background: rgba(79, 110, 247, 0.08);
  border: 1px solid rgba(79, 110, 247, 0.2);
  color: #4f6ef7;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  padding: 5px 12px;
  border-radius: 100px;
  margin-bottom: 20px;
  font-family: var(--font-body);
}

.card-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 400;
  color: #1a1d2e;
  margin: 0 0 8px;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.card-subtitle {
  font-size: 14px;
  color: rgba(20, 25, 60, 0.45);
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.01em;
  font-family: var(--font-body);
}

/* ─── Error Alert ───────────────────────────────────────── */
.error-alert {
  display: flex;
  align-items: center;
  gap: 9px;
  background: rgba(239, 68, 68, 0.07);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #dc2626;
  font-size: 13px;
  padding: 11px 14px;
  border-radius: 12px;
  margin-bottom: 20px;
  font-family: var(--font-body);
}

.alert-slide-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.2, 0.64, 1); }
.alert-slide-leave-active { transition: all 0.2s ease; }
.alert-slide-enter-from  { opacity: 0; transform: translateY(-8px); }
.alert-slide-leave-to    { opacity: 0; transform: translateY(-4px); }

/* ─── Form ──────────────────────────────────────────────── */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.field-label {
  font-size: 12.5px;
  font-weight: 500;
  color: rgba(20, 25, 60, 0.5);
  letter-spacing: 0.02em;
  text-transform: uppercase;
  font-family: var(--font-body);
}

.field-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.09);
  border-radius: 12px;
  padding: 0 14px;
  height: 48px;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.field-wrapper.focused {
  border-color: rgba(79, 110, 247, 0.5);
  background: rgba(79, 110, 247, 0.04);
  box-shadow: 0 0 0 3px rgba(79, 110, 247, 0.1);
}

.field-icon {
  color: rgba(20, 25, 60, 0.3);
  flex-shrink: 0;
  transition: color 0.2s;
}

.field-wrapper.focused .field-icon {
  color: rgba(79, 110, 247, 0.8);
}

.field-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #1a1d2e;
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 400;
  letter-spacing: 0.01em;
  min-width: 0;
}

.field-input::placeholder {
  color: rgba(20, 25, 60, 0.28);
}

.toggle-password {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: rgba(20, 25, 60, 0.28);
  display: flex;
  align-items: center;
  transition: color 0.2s;
  flex-shrink: 0;
}

.toggle-password:hover {
  color: rgba(20, 25, 60, 0.6);
}

/* ─── Button ────────────────────────────────────────────── */
.login-btn {
  position: relative;
  margin-top: 8px;
  height: 50px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #4f6ef7 0%, #6c4fee 100%);
  color: #fff;
  font-family: var(--font-body);
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.01em;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.15s, opacity 0.15s, box-shadow 0.2s;
  box-shadow: 0 4px 24px rgba(79, 110, 247, 0.25);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 32px rgba(79, 110, 247, 0.35);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.6;
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
  background: linear-gradient(105deg, transparent 40%, rgba(255,255,255,0.2) 50%, transparent 60%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

.login-btn:hover .btn-shimmer {
  transform: translateX(100%);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ─── Footer ────────────────────────────────────────────── */
.card-footer {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.footer-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.footer-text {
  font-size: 13px;
  color: rgba(20, 25, 60, 0.4);
  font-family: var(--font-body);
}

.link-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 500;
  padding: 0;
  transition: color 0.15s;
}

.link-btn.primary {
  color: #4f6ef7;
}

.link-btn.primary:hover {
  color: #2f4fd4;
}

.link-btn.system {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(20, 25, 60, 0.35);
  font-size: 12.5px;
  font-weight: 400;
}

.link-btn.system:hover {
  color: rgba(20, 25, 60, 0.65);
}

.divider-line {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 10px;
  font-size: 11px;
  color: rgba(20, 25, 60, 0.22);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  font-family: var(--font-body);
}

.divider-line::before,
.divider-line::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(0, 0, 0, 0.07);
}
</style>