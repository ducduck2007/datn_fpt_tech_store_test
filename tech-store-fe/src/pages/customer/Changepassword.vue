<!-- FILE: src/pages/customer/ChangePassword.vue -->
<template>
  <div class="cp-root">
    <!-- Background -->
    <div class="bg-layer">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- Card -->
    <div class="cp-card" :class="{ 'card-shake': shaking }">
      <!-- Header -->
      <div class="card-header">
        <div class="brand-badge">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>Security</span>
        </div>
        <h1 class="card-title">Change password</h1>
        <p class="card-subtitle">Keep your account safe with a strong password</p>
      </div>

      <!-- ══ FORCE BANNER — chỉ hiện khi admin tạo account ══ -->
      <div v-if="isForced" class="force-banner">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0">
          <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
          <line x1="12" y1="9" x2="12" y2="13" stroke-linecap="round"/>
          <line x1="12" y1="17" x2="12.01" y2="17" stroke-linecap="round"/>
        </svg>
        <div>
          Tài khoản của bạn cần đổi mật khẩu trước khi tiếp tục.
          Mật khẩu hiện tại là <strong>số điện thoại</strong> của bạn.
        </div>
      </div>

      <!-- Success State -->
      <transition name="alert-slide">
        <div v-if="success" class="success-alert">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Password changed successfully! Redirecting...</span>
        </div>
      </transition>

      <!-- Error Alert -->
      <transition name="alert-slide">
        <div v-if="alert" class="error-alert">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke="currentColor" stroke-width="1"/>
          </svg>
          <span>{{ alert }}</span>
        </div>
      </transition>

      <!-- Form -->
      <form class="cp-form" @submit.prevent="doChange">

        <!-- Current Password -->
        <div class="field-group">
          <label class="field-label">Current password</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'current', error: fieldErrors.current }">
            <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.currentPassword"
              :type="show.current ? 'text' : 'password'"
              class="field-input"
              placeholder="Enter current password"
              autocomplete="current-password"
              @focus="focusedField = 'current'"
              @blur="focusedField = ''; validateField('current')"
            />
            <button type="button" class="toggle-password" @click="show.current = !show.current" tabindex="-1">
              <svg v-if="!show.current" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
            <svg v-if="fieldErrors.current" class="field-error-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </div>
          <transition name="hint-slide">
            <p v-if="fieldErrors.current" class="field-hint-error">Current password is required</p>
          </transition>
        </div>

        <!-- Divider -->
        <div class="optional-divider">
          <span class="optional-line"></span>
          <span class="optional-label">New password</span>
          <span class="optional-line"></span>
        </div>

        <!-- New Password -->
        <div class="field-group">
          <label class="field-label">New password</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'newPass' }">
            <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input
              v-model="form.newPassword"
              :type="show.newPass ? 'text' : 'password'"
              class="field-input"
              placeholder="Create new password"
              autocomplete="new-password"
              @focus="focusedField = 'newPass'"
              @blur="focusedField = ''"
            />
            <button type="button" class="toggle-password" @click="show.newPass = !show.newPass" tabindex="-1">
              <svg v-if="!show.newPass" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <!-- Strength bar -->
          <div v-if="form.newPassword" class="strength-bar">
            <div class="strength-track">
              <div class="strength-fill" :style="{ width: strengthPct + '%' }" :data-level="strengthLevel"></div>
            </div>
            <span class="strength-label" :data-level="strengthLevel">{{ strengthText }}</span>
          </div>
        </div>

        <!-- Confirm New Password -->
        <div class="field-group">
          <label class="field-label">Confirm new password</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'confirm', error: passwordMismatch }">
            <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input
              v-model="form.confirmPassword"
              :type="show.confirm ? 'text' : 'password'"
              class="field-input"
              placeholder="Repeat new password"
              autocomplete="new-password"
              @focus="focusedField = 'confirm'"
              @blur="focusedField = ''"
            />
            <button type="button" class="toggle-password" @click="show.confirm = !show.confirm" tabindex="-1">
              <svg v-if="!show.confirm" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
            <svg v-if="passwordMismatch" class="field-error-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <svg v-else-if="form.confirmPassword && !passwordMismatch" class="field-ok-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
              <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <transition name="hint-slide">
            <p v-if="passwordMismatch" class="field-hint-error">Passwords do not match</p>
          </transition>
        </div>

        <!-- Requirements checklist -->
        <div v-if="form.newPassword" class="requirements">
          <div class="req-item" :class="{ met: form.newPassword.length >= 6 }">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            At least 6 characters
          </div>
          <div class="req-item" :class="{ met: /[A-Z]/.test(form.newPassword) }">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Uppercase letter
          </div>
          <div class="req-item" :class="{ met: /[0-9]/.test(form.newPassword) }">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Number
          </div>
          <div class="req-item" :class="{ met: /[^A-Za-z0-9]/.test(form.newPassword) }">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Special character
          </div>
        </div>

        <!-- Actions -->
        <div class="action-row">
          <!-- Ẩn nút Cancel khi force mode -->
          <button v-if="!isForced" type="button" class="cancel-btn" @click="resetForm">
            Cancel
          </button>
          <button type="submit" class="submit-btn" :class="{ loading }" :disabled="loading || !canSubmit">
            <span class="btn-content">
              <svg v-if="!loading" width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span v-else class="spinner"></span>
              {{ loading ? 'Updating...' : 'Update password' }}
            </span>
            <div class="btn-shimmer"></div>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { authApi } from "../../api/auth.api";
import { toast } from "../../ui/toast";

const route  = useRoute();
const router = useRouter();

// Force mode: khi admin tạo account → redirect về đây với ?force=1
const isForced = computed(() => route.query.force === "1");

const loading      = ref(false);
const alert        = ref("");
const success      = ref(false);
const shaking      = ref(false);
const focusedField = ref("");

const form = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const show = reactive({
  current: false,
  newPass: false,
  confirm: false,
});

const fieldErrors = reactive({ current: false });

function validateField(field) {
  if (field === "current") {
    fieldErrors.current = form.currentPassword.trim().length === 0;
  }
}

const passwordMismatch = computed(() =>
  form.confirmPassword.length > 0 && form.newPassword !== form.confirmPassword
);

const strengthScore = computed(() => {
  const p = form.newPassword;
  if (!p) return 0;
  let s = 0;
  if (p.length >= 6)           s++;
  if (p.length >= 12)          s++;
  if (/[A-Z]/.test(p))         s++;
  if (/[0-9]/.test(p))         s++;
  if (/[^A-Za-z0-9]/.test(p)) s++;
  return s;
});

const strengthPct   = computed(() => (strengthScore.value / 5) * 100);
const strengthLevel = computed(() => {
  if (strengthScore.value <= 1) return "weak";
  if (strengthScore.value <= 3) return "fair";
  return "strong";
});
const strengthText  = computed(() => {
  if (strengthScore.value <= 1) return "Weak";
  if (strengthScore.value <= 3) return "Fair";
  return "Strong";
});

const canSubmit = computed(() =>
  form.currentPassword.trim().length > 0 &&
  form.newPassword.length >= 6 &&
  form.newPassword === form.confirmPassword
);

function triggerShake() {
  shaking.value = true;
  setTimeout(() => (shaking.value = false), 600);
}

function resetForm() {
  form.currentPassword = "";
  form.newPassword = "";
  form.confirmPassword = "";
  alert.value = "";
  success.value = false;
  fieldErrors.current = false;
}

function errToText(e) {
  const msg = e?.response?.data?.message || e?.message || "Failed to change password";
  return typeof msg === "string" ? msg : JSON.stringify(msg);
}

async function doChange() {
  alert.value = "";
  success.value = false;

  if (!form.currentPassword.trim()) {
    alert.value = "Please enter your current password.";
    triggerShake();
    return;
  }
  if (form.newPassword.length < 6) {
    alert.value = "New password must be at least 6 characters.";
    triggerShake();
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    alert.value = "Passwords do not match.";
    triggerShake();
    return;
  }
  if (form.newPassword === form.currentPassword) {
    alert.value = "New password must be different from current password.";
    triggerShake();
    return;
  }

  loading.value = true;
  try {
    await authApi.changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
    });

    // ← Xóa flag mustChangePassword
    localStorage.removeItem("mustChangePassword");

    success.value = true;
    toast("Password updated successfully!", "success");
    resetForm();

    // Redirect về trang chủ sau 1.5s
    setTimeout(() => router.replace("/"), 1500);

  } catch (e) {
    alert.value = errToText(e);
    triggerShake();
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap");

.cp-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Inter", sans-serif;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

.bg-layer { position: absolute; inset: 0; pointer-events: none; }

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.09;
}
.orb-1 { width:500px;height:500px;background:radial-gradient(circle,#3B82F6,transparent 70%);top:-150px;left:-100px;animation:drift1 12s ease-in-out infinite alternate; }
.orb-2 { width:400px;height:400px;background:radial-gradient(circle,#7c3aed,transparent 70%);bottom:-120px;right:-80px;animation:drift2 15s ease-in-out infinite alternate; }
.orb-3 { width:300px;height:300px;background:radial-gradient(circle,#0ea5e9,transparent 70%);top:40%;left:60%;animation:drift1 18s ease-in-out infinite alternate-reverse; }
.bg-grid { position:absolute;inset:0;background-image:linear-gradient(rgba(15,23,42,.04) 1px,transparent 1px),linear-gradient(90deg,rgba(15,23,42,.04) 1px,transparent 1px);background-size:48px 48px; }

@keyframes drift1 { from{transform:translate(0,0) scale(1)} to{transform:translate(40px,30px) scale(1.1)} }
@keyframes drift2 { from{transform:translate(0,0) scale(1)} to{transform:translate(-30px,-40px) scale(1.08)} }

.cp-card {
  position: relative;
  width: 100%;
  max-width: 440px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 2px 16px rgba(0,0,0,.06), 0 1px 3px rgba(0,0,0,.05);
  animation: cardEnter 0.6s cubic-bezier(0.34,1.56,0.64,1) both;
}

@keyframes cardEnter { from{opacity:0;transform:translateY(32px) scale(0.96)} to{opacity:1;transform:translateY(0) scale(1)} }
.card-shake { animation: shake 0.5s cubic-bezier(0.36,0.07,0.19,0.97); }
@keyframes shake { 0%,100%{transform:translateX(0)} 15%{transform:translateX(-8px)} 30%{transform:translateX(7px)} 45%{transform:translateX(-5px)} 60%{transform:translateX(4px)} 75%{transform:translateX(-2px)} }

.card-header { margin-bottom: 24px; }
.brand-badge { display:inline-flex;align-items:center;gap:7px;background:#EFF6FF;border:1px solid rgba(59,130,246,.25);color:#3B82F6;font-size:11px;font-weight:600;letter-spacing:.06em;text-transform:uppercase;padding:5px 12px;border-radius:100px;margin-bottom:18px; }
.card-title { font-size:28px;font-weight:800;color:#0F172A;margin:0 0 7px;line-height:1.2;letter-spacing:-.02em; }
.card-subtitle { font-size:13.5px;color:#64748B;margin:0;font-weight:400; }

/* ── Force Banner ── */
.force-banner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-left: 4px solid #f59e0b;
  color: #92400e;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.55;
  margin-bottom: 20px;
}

.force-banner strong { font-weight: 700; }

.error-alert, .success-alert { display:flex;align-items:center;gap:9px;font-size:13px;padding:11px 14px;border-radius:12px;margin-bottom:20px; }
.error-alert   { background:#FEF2F2;border:1px solid #FECACA;color:#DC2626; }
.success-alert { background:#F0FDF4;border:1px solid #BBF7D0;color:#16A34A; }

.alert-slide-enter-active { transition:all .3s cubic-bezier(0.34,1.2,0.64,1); }
.alert-slide-leave-active { transition:all .2s ease; }
.alert-slide-enter-from   { opacity:0;transform:translateY(-8px); }
.alert-slide-leave-to     { opacity:0;transform:translateY(-4px); }

.cp-form { display:flex;flex-direction:column;gap:14px; }
.field-group { display:flex;flex-direction:column;gap:6px; }
.field-label { font-size:12px;font-weight:600;color:#64748B;letter-spacing:.05em;text-transform:uppercase; }

.field-wrapper { display:flex;align-items:center;gap:9px;background:#F8FAFC;border:1.5px solid #E5E7EB;border-radius:11px;padding:0 13px;height:46px;transition:border-color .2s,background .2s,box-shadow .2s; }
.field-wrapper.focused { border-color:#3B82F6;background:#EFF6FF;box-shadow:0 0 0 3px rgba(59,130,246,.1); }
.field-wrapper.error   { border-color:#FCA5A5;background:#FEF2F2; }

.field-icon { color:#94A3B8;flex-shrink:0;transition:color .2s; }
.field-wrapper.focused .field-icon { color:#3B82F6; }
.field-wrapper.error   .field-icon { color:#DC2626; }

.field-input { flex:1;background:transparent;border:none;outline:none;color:#0F172A;font-family:"Inter",sans-serif;font-size:13.5px;font-weight:400;min-width:0; }
.field-input::placeholder { color:#CBD5E1; }

.field-error-icon { color:#DC2626;flex-shrink:0; }
.field-ok-icon    { color:#16A34A;flex-shrink:0; }
.field-hint-error { font-size:11.5px;color:#DC2626;margin:0;padding-left:2px; }

.hint-slide-enter-active { transition:all .2s ease; }
.hint-slide-leave-active { transition:all .15s ease; }
.hint-slide-enter-from   { opacity:0;transform:translateY(-4px); }
.hint-slide-leave-to     { opacity:0; }

.toggle-password { background:none;border:none;padding:3px;cursor:pointer;color:#94A3B8;display:flex;align-items:center;transition:color .2s;flex-shrink:0; }
.toggle-password:hover { color:#64748B; }

.strength-bar { display:flex;align-items:center;gap:10px;margin-top:2px; }
.strength-track { flex:1;height:3px;background:#E2E8F0;border-radius:10px;overflow:hidden; }
.strength-fill { height:100%;border-radius:10px;transition:width .4s ease,background-color .4s ease; }
.strength-fill[data-level="weak"]   { background:#EF4444; }
.strength-fill[data-level="fair"]   { background:#F59E0B; }
.strength-fill[data-level="strong"] { background:#16A34A; }
.strength-label { font-size:11px;font-weight:600;min-width:36px;text-align:right; }
.strength-label[data-level="weak"]   { color:#EF4444; }
.strength-label[data-level="fair"]   { color:#F59E0B; }
.strength-label[data-level="strong"] { color:#16A34A; }

.requirements { display:grid;grid-template-columns:1fr 1fr;gap:6px 12px;padding:14px;background:#F8FAFC;border:1px solid #E5E7EB;border-radius:10px; }
.req-item { display:flex;align-items:center;gap:6px;font-size:11.5px;color:#94A3B8;transition:color .25s; }
.req-item svg { flex-shrink:0;opacity:.4;transition:opacity .25s; }
.req-item.met { color:#16A34A; }
.req-item.met svg { opacity:1; }

.optional-divider { display:flex;align-items:center;gap:10px;margin:2px 0; }
.optional-line { flex:1;height:1px;background:#E5E7EB; }
.optional-label { font-size:11px;color:#94A3B8;letter-spacing:.06em;text-transform:uppercase;font-weight:500;white-space:nowrap; }

.action-row { display:flex;gap:10px;margin-top:6px; }

.cancel-btn { flex:0 0 auto;height:50px;padding:0 22px;border:1.5px solid #E5E7EB;border-radius:12px;background:#FFFFFF;color:#64748B;font-family:"Inter",sans-serif;font-size:14px;font-weight:500;cursor:pointer;transition:background .2s,color .2s,border-color .2s; }
.cancel-btn:hover { background:#F1F5F9;color:#0F172A;border-color:rgba(15,23,42,.15); }

.submit-btn { position:relative;flex:1;height:50px;border:none;border-radius:12px;background:linear-gradient(135deg,#1E293B,#0F172A);color:#fff;font-family:"Inter",sans-serif;font-size:15px;font-weight:600;letter-spacing:.01em;cursor:pointer;overflow:hidden;transition:transform .15s,box-shadow .2s,opacity .2s;box-shadow:0 6px 24px rgba(15,23,42,.2); }
.submit-btn:hover:not(:disabled) { background:linear-gradient(135deg,#2563EB,#1D4ED8);transform:translateY(-1px);box-shadow:0 8px 25px rgba(59,130,246,.3); }
.submit-btn:active:not(:disabled) { transform:translateY(0); }
.submit-btn:disabled { opacity:.45;cursor:not-allowed; }

.btn-content { position:relative;z-index:1;display:flex;align-items:center;justify-content:center;gap:8px; }
.btn-shimmer { position:absolute;inset:0;background:linear-gradient(105deg,transparent 40%,rgba(255,255,255,.1) 50%,transparent 60%);transform:translateX(-100%);transition:transform .6s; }
.submit-btn:hover:not(:disabled) .btn-shimmer { transform:translateX(100%); }

.spinner { width:15px;height:15px;border:2px solid rgba(255,255,255,.3);border-top-color:white;border-radius:50%;animation:spin .7s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }

@media (max-width: 480px) {
  .cp-card { padding:28px 20px; }
  .requirements { grid-template-columns:1fr; }
}
</style>