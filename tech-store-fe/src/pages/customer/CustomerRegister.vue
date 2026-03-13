<!-- FILE: src/pages/customer/CustomerRegister.vue -->
<template>
  <div class="register-root">
    <!-- Background -->
    <div class="bg-layer">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- Card -->
    <div class="register-card" :class="{ 'card-shake': shaking }">
      <!-- Header -->
      <div class="card-header">
        <div class="brand-badge">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M2 17l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
          </svg>
          <span>Cổng khách hàng</span>
        </div>
        <h1 class="card-title">Tạo tài khoản</h1>
        <p class="card-subtitle">Tham gia cùng chúng tôi — chỉ mất một phút</p>
      </div>

      <!-- Alert -->
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
      <form class="register-form" @submit.prevent="doRegister">
        <!-- Row: Username + Email -->
        <div class="field-row">
          <div class="field-group">
            <label class="field-label">Tên đăng nhập</label>
            <div class="field-wrapper" :class="{ focused: focusedField === 'username', error: fieldErrors.username }">
              <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <input
                v-model="form.username"
                type="text"
                class="field-input"
                placeholder="tên đăng nhập"
                autocomplete="username"
                @focus="focusedField = 'username'"
                @blur="focusedField = ''; validateField('username')"
              />
              <svg v-if="fieldErrors.username" class="field-error-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
                <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <svg v-else-if="form.username && !fieldErrors.username" class="field-ok-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>

          <div class="field-group">
            <label class="field-label">Email</label>
            <div class="field-wrapper" :class="{ focused: focusedField === 'email', error: fieldErrors.email }">
              <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" stroke="currentColor" stroke-width="1.5"/>
                <polyline points="22,6 12,13 2,6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <input
                v-model="form.email"
                type="email"
                class="field-input"
                placeholder="email@domain.com"
                autocomplete="email"
                @focus="focusedField = 'email'"
                @blur="focusedField = ''; validateField('email')"
              />
              <svg v-if="fieldErrors.email" class="field-error-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
                <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <svg v-else-if="form.email && !fieldErrors.email" class="field-ok-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>

        <!-- Phone -->
        <div class="field-group">
          <label class="field-label">Số điện thoại</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'phone', error: fieldErrors.phone }">
            <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.72 12 19.79 19.79 0 0 1 1.65 3.38 2 2 0 0 1 3.62 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 9.91a16 16 0 0 0 6.29 6.29l.91-.91a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.phone"
              type="tel"
              class="field-input"
              placeholder="0912 345 678"
              autocomplete="tel"
              @focus="focusedField = 'phone'"
              @blur="focusedField = ''; validateField('phone')"
            />
            <svg v-if="fieldErrors.phone" class="field-error-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <svg v-else-if="form.phone && !fieldErrors.phone" class="field-ok-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
              <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <transition name="hint-slide">
            <p v-if="fieldErrors.phone" class="field-hint-error">Vui lòng nhập số điện thoại hợp lệ</p>
          </transition>
        </div>

        <!-- Password -->
        <div class="field-group">
          <label class="field-label">Mật khẩu</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'password' }">
            <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="field-input"
              placeholder="Tạo mật khẩu"
              autocomplete="new-password"
              @focus="focusedField = 'password'"
              @blur="focusedField = ''"
            />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
              <svg v-if="!showPassword" width="14" height="14" viewBox="0 0 24 24" fill="none">
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
          <div v-if="form.password" class="strength-bar">
            <div class="strength-track">
              <div class="strength-fill" :style="{ width: strengthPct + '%' }" :data-level="strengthLevel"></div>
            </div>
            <span class="strength-label" :data-level="strengthLevel">{{ strengthText }}</span>
          </div>
        </div>

        <!-- Confirm Password -->
        <div class="field-group">
          <label class="field-label">Xác nhận mật khẩu</label>
          <div class="field-wrapper" :class="{ focused: focusedField === 'password2', error: passwordMismatch }">
            <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input
              v-model="form.password2"
              :type="showPassword2 ? 'text' : 'password'"
              class="field-input"
              placeholder="Nhập lại mật khẩu"
              autocomplete="new-password"
              @focus="focusedField = 'password2'"
              @blur="focusedField = ''"
            />
            <button type="button" class="toggle-password" @click="showPassword2 = !showPassword2" tabindex="-1">
              <svg v-if="!showPassword2" width="14" height="14" viewBox="0 0 24 24" fill="none">
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
            <svg v-else-if="form.password2 && form.password === form.password2" class="field-ok-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
              <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <transition name="hint-slide">
            <p v-if="passwordMismatch" class="field-hint-error">Mật khẩu không khớp</p>
          </transition>
        </div>

        <!-- ───── Optional fields divider ───── -->
        <div class="optional-divider">
          <span class="optional-line"></span>
          <span class="optional-label">Thông tin tùy chọn</span>
          <span class="optional-line"></span>
        </div>

        <!-- Row: Date of Birth + Address -->
        <div class="field-row">
          <div class="field-group">
            <label class="field-label">
              Ngày sinh
              <span class="optional-badge">tùy chọn</span>
            </label>
            <div class="field-wrapper" :class="{ focused: focusedField === 'dateOfBirth' }">
              <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/>
                <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <input
                v-model="form.dateOfBirth"
                type="date"
                class="field-input date-input"
                :max="today"
                @focus="focusedField = 'dateOfBirth'"
                @blur="focusedField = ''"
              />
            </div>
          </div>

          <div class="field-group">
            <label class="field-label">
              Địa chỉ
              <span class="optional-badge">tùy chọn</span>
            </label>
            <div class="field-wrapper" :class="{ focused: focusedField === 'address' }">
              <svg class="field-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <input
                v-model="form.address"
                type="text"
                class="field-input"
                placeholder="Địa chỉ của bạn"
                autocomplete="street-address"
                @focus="focusedField = 'address'"
                @blur="focusedField = ''"
              />
            </div>
          </div>
        </div>

        <button type="submit" class="register-btn" :class="{ loading }" :disabled="loading">
          <span class="btn-content">
            <svg v-if="!loading" width="15" height="15" viewBox="0 0 24 24" fill="none">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
              <line x1="19" y1="8" x2="19" y2="14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="22" y1="11" x2="16" y2="11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span v-else class="spinner"></span>
            {{ loading ? 'Đang tạo tài khoản...' : 'Tạo tài khoản' }}
          </span>
          <div class="btn-shimmer"></div>
        </button>
      </form>

      <!-- Footer -->
      <div class="card-footer">
        <span class="footer-text">Đã có tài khoản?</span>
        <button class="link-btn" @click="router.push('/login')">Đăng nhập</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { toast } from "../../ui/toast";
import { authApi } from "../../api/auth.api";

const router = useRouter();
const loading = ref(false);
const alert = ref("");
const shaking = ref(false);
const focusedField = ref("");
const showPassword = ref(false);
const showPassword2 = ref(false);

// Today's date as yyyy-MM-dd for max attribute on date input
const today = new Date().toISOString().split("T")[0];

const form = reactive({
  username: "",
  email: "",
  phone: "",         // required
  password: "",
  password2: "",
  dateOfBirth: "",   // optional – LocalDate on backend
  address: "",       // optional
});

const fieldErrors = reactive({
  username: false,
  email: false,
  phone: false,
});

function validateField(field) {
  if (field === "username") {
    fieldErrors.username = form.username.trim().length > 0 && form.username.trim().length < 3;
  }
  if (field === "email") {
    fieldErrors.email = form.email.length > 0 && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email);
  }
  if (field === "phone") {
    fieldErrors.phone = form.phone.length > 0 && !/^[0-9+\s\-().]{7,15}$/.test(form.phone.trim());
  }
}

const passwordMismatch = computed(() =>
  form.password2.length > 0 && form.password !== form.password2
);

const strengthScore = computed(() => {
  const p = form.password;
  if (!p) return 0;
  let s = 0;
  if (p.length >= 8) s++;
  if (p.length >= 12) s++;
  if (/[A-Z]/.test(p)) s++;
  if (/[0-9]/.test(p)) s++;
  if (/[^A-Za-z0-9]/.test(p)) s++;
  return s;
});

const strengthPct = computed(() => (strengthScore.value / 5) * 100);
const strengthLevel = computed(() => {
  if (strengthScore.value <= 1) return "weak";
  if (strengthScore.value <= 3) return "fair";
  return "strong";
});
const strengthText = computed(() => {
  if (strengthScore.value <= 1) return "Yếu";
  if (strengthScore.value <= 3) return "Trung bình";
  return "Mạnh";
});

function triggerShake() {
  shaking.value = true;
  setTimeout(() => (shaking.value = false), 600);
}

function errToText(e) {
  const msg = e?.response?.data?.message || e?.message || "Đăng ký thất bại";
  return typeof msg === "string" ? msg : JSON.stringify(msg);
}

async function doRegister() {
  alert.value = "";
  if (!form.username || !form.email || !form.phone || !form.password) {
    alert.value = "Vui lòng điền tất cả các trường bắt buộc.";
    triggerShake();
    return;
  }
  if (fieldErrors.phone) {
    alert.value = "Vui lòng nhập số điện thoại hợp lệ.";
    triggerShake();
    return;
  }
  if (form.password !== form.password2) {
    alert.value = "Mật khẩu không khớp.";
    triggerShake();
    return;
  }

  loading.value = true;
  try {
    const payload = {
      username: form.username,
      email: form.email,
      phone: form.phone.trim(),
      password: form.password,
      // Only include optional fields when they have a value
      ...(form.dateOfBirth && { dateOfBirth: form.dateOfBirth }),
      ...(form.address.trim() && { address: form.address.trim() }),
    };
    await authApi.register(payload);
    toast("Tạo tài khoản thành công! Vui lòng đăng nhập.", "success");
    router.replace("/login");
  } catch (e) {
    alert.value = errToText(e);
    triggerShake();
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;1,9..40,300&display=swap');

/* ─── Root ─────────────────────────────────────────────── */
.register-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2fa;
  font-family: 'DM Sans', sans-serif;
  position: fixed;   /* ← đổi từ relative */
  inset: 0;          /* ← thêm dòng này */
  z-index: 9999;     /* ← thêm dòng này, phủ lên toàn bộ layout */
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
  width: 500px; height: 500px;
  background: radial-gradient(circle, #a5b8ff, transparent 70%);
  top: -150px; left: -100px;
  animation: drift1 12s ease-in-out infinite alternate;
}

.orb-2 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, #c4b5fd, transparent 70%);
  bottom: -120px; right: -80px;
  animation: drift2 15s ease-in-out infinite alternate;
}

.orb-3 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, #7dd3fc, transparent 70%);
  top: 40%; left: 60%;
  animation: drift1 18s ease-in-out infinite alternate-reverse;
}

.bg-grid {
  position: absolute; inset: 0;
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
.register-card {
  position: relative;
  width: 100%;
  max-width: 480px;
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

.card-shake { animation: shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97); }

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  15%       { transform: translateX(-8px); }
  30%       { transform: translateX(7px); }
  45%       { transform: translateX(-5px); }
  60%       { transform: translateX(4px); }
  75%       { transform: translateX(-2px); }
}

/* ─── Header ────────────────────────────────────────────── */
.card-header { margin-bottom: 28px; }

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
  margin-bottom: 18px;
}

.card-title {
  font-family: 'Instrument Serif', serif;
  font-size: 30px;
  font-weight: 400;
  color: #1a1d2e;
  margin: 0 0 7px;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.card-subtitle {
  font-size: 13.5px;
  color: rgba(20, 25, 60, 0.45);
  margin: 0;
  font-weight: 300;
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
}

.alert-slide-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.2, 0.64, 1); }
.alert-slide-leave-active { transition: all 0.2s ease; }
.alert-slide-enter-from  { opacity: 0; transform: translateY(-8px); }
.alert-slide-leave-to    { opacity: 0; transform: translateY(-4px); }

/* ─── Form ──────────────────────────────────────────────── */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  font-weight: 500;
  color: rgba(20, 25, 60, 0.5);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ─── Optional Badge ────────────────────────────────────── */
.optional-badge {
  font-size: 9.5px;
  font-weight: 400;
  letter-spacing: 0.04em;
  text-transform: lowercase;
  color: rgba(20, 25, 60, 0.35);
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.08);
  padding: 1px 6px;
  border-radius: 100px;
}

/* ─── Optional Divider ──────────────────────────────────── */
.optional-divider {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 2px 0;
}

.optional-line {
  flex: 1;
  height: 1px;
  background: rgba(0, 0, 0, 0.08);
}

.optional-label {
  font-size: 11px;
  color: rgba(20, 25, 60, 0.3);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  font-weight: 400;
  white-space: nowrap;
}

/* ─── Field Wrapper ─────────────────────────────────────── */
.field-wrapper {
  display: flex;
  align-items: center;
  gap: 9px;
  background: rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.09);
  border-radius: 11px;
  padding: 0 13px;
  height: 46px;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.field-wrapper.focused {
  border-color: rgba(79, 110, 247, 0.5);
  background: rgba(79, 110, 247, 0.04);
  box-shadow: 0 0 0 3px rgba(79, 110, 247, 0.1);
}

.field-wrapper.error {
  border-color: rgba(239, 68, 68, 0.45);
  background: rgba(239, 68, 68, 0.04);
}

.field-icon {
  color: rgba(20, 25, 60, 0.28);
  flex-shrink: 0;
  transition: color 0.2s;
}

.field-wrapper.focused .field-icon { color: rgba(79, 110, 247, 0.8); }
.field-wrapper.error .field-icon   { color: rgba(239, 68, 68, 0.6); }

.field-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #1a1d2e;
  font-family: 'DM Sans', sans-serif;
  font-size: 13.5px;
  font-weight: 400;
  min-width: 0;
}

.field-input::placeholder { color: rgba(20, 25, 60, 0.28); }

/* Date input native picker styling */
.date-input::-webkit-calendar-picker-indicator {
  filter: invert(0.2);
  cursor: pointer;
  opacity: 0.45;
  transition: opacity 0.2s;
}
.date-input::-webkit-calendar-picker-indicator:hover { opacity: 0.8; }
.date-input::-webkit-datetime-edit { color: rgba(20, 25, 60, 0.5); }
.date-input::-webkit-datetime-edit-fields-wrapper { color: #1a1d2e; }

.field-error-icon { color: rgba(239, 68, 68, 0.7); flex-shrink: 0; }
.field-ok-icon    { color: rgba(16, 185, 129, 0.85); flex-shrink: 0; }

.field-hint-error {
  font-size: 11.5px;
  color: rgba(220, 38, 38, 0.8);
  margin: 0;
  padding-left: 2px;
}

.hint-slide-enter-active { transition: all 0.2s ease; }
.hint-slide-leave-active { transition: all 0.15s ease; }
.hint-slide-enter-from   { opacity: 0; transform: translateY(-4px); }
.hint-slide-leave-to     { opacity: 0; }

.toggle-password {
  background: none;
  border: none;
  padding: 3px;
  cursor: pointer;
  color: rgba(20, 25, 60, 0.28);
  display: flex;
  align-items: center;
  transition: color 0.2s;
  flex-shrink: 0;
}

.toggle-password:hover { color: rgba(20, 25, 60, 0.6); }

/* ─── Strength Bar ──────────────────────────────────────── */
.strength-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 2px;
}

.strength-track {
  flex: 1;
  height: 3px;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 10px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.4s ease, background-color 0.4s ease;
}

.strength-fill[data-level="weak"]   { background: #ef4444; }
.strength-fill[data-level="fair"]   { background: #f59e0b; }
.strength-fill[data-level="strong"] { background: #10b981; }

.strength-label {
  font-size: 11px;
  font-weight: 500;
  min-width: 36px;
  text-align: right;
}

.strength-label[data-level="weak"]   { color: #ef4444; }
.strength-label[data-level="fair"]   { color: #f59e0b; }
.strength-label[data-level="strong"] { color: #10b981; }

/* ─── Button ────────────────────────────────────────────── */
.register-btn {
  position: relative;
  margin-top: 6px;
  height: 50px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #4f6ef7 0%, #6c4fee 100%);
  color: #fff;
  font-family: 'DM Sans', sans-serif;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.01em;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.15s, box-shadow 0.2s;
  box-shadow: 0 4px 24px rgba(79, 110, 247, 0.25);
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 32px rgba(79, 110, 247, 0.35);
}

.register-btn:active:not(:disabled) { transform: translateY(0); }
.register-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-shimmer {
  position: absolute; inset: 0;
  background: linear-gradient(105deg, transparent 40%, rgba(255,255,255,0.2) 50%, transparent 60%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

.register-btn:hover .btn-shimmer { transform: translateX(100%); }

.spinner {
  width: 15px; height: 15px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* ─── Footer ────────────────────────────────────────────── */
.card-footer {
  margin-top: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.footer-text {
  font-size: 13px;
  color: rgba(20, 25, 60, 0.4);
}

.link-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-family: 'DM Sans', sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: #4f6ef7;
  padding: 0;
  transition: color 0.15s;
}

.link-btn:hover { color: #2f4fd4; }

/* ─── Responsive ────────────────────────────────────────── */
@media (max-width: 480px) {
  .field-row { grid-template-columns: 1fr; }
  .register-card { padding: 28px 24px; }
}
</style>