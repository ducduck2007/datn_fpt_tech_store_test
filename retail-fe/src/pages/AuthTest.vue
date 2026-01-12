<template>
  <div style="display: grid; gap: 16px">
    <!-- TOKEN -->
    <div style="padding: 12px; border: 1px solid #ddd">
      <div
        style="
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 8px;
        "
      >
        <strong>Session / Token</strong>
        <div style="display: flex; gap: 8px">
          <button @click="logout('Manual logout')" :disabled="!token">
            Logout
          </button>
          <button @click="clearResult">Clear Response</button>
        </div>
      </div>

      <div style="margin-top: 8px; word-break: break-all">
        <span v-if="token">{{ token }}</span>
        <span v-else style="color: #888">(Chưa có token)</span>
      </div>

      <div style="margin-top: 10px; display: grid; gap: 6px">
        <div>
          <strong>Idle logout:</strong>
          {{ IDLE_MINUTES }} phút không thao tác sẽ logout
        </div>
        <div>
          <strong>Token expires at:</strong>
          <span v-if="tokenExpiresAt">{{ tokenExpiresAt }}</span>
          <span v-else style="color: #888">(n/a)</span>
        </div>
      </div>

      <div style="margin-top: 12px">
        <strong>JWT payload (decode để xem nhanh)</strong>
        <pre
          style="
            background: #f7f7f7;
            padding: 12px;
            overflow: auto;
            max-height: 220px;
          "
          >{{ pretty(jwtPayload) }}</pre
        >
      </div>
    </div>

    <!-- REGISTER -->
    <div style="padding: 12px; border: 1px solid #ddd">
      <h3>Register (backend fix role = CUSTOMER)</h3>
      <div style="display: grid; gap: 8px">
        <input v-model="reg.username" placeholder="username" />
        <input v-model="reg.email" placeholder="email" />
        <input v-model="reg.password" type="password" placeholder="password" />
        <button :disabled="loading" @click="handleRegister">
          {{ loading ? "Loading..." : "Register" }}
        </button>
      </div>
      <div v-if="lastUser" style="margin-top: 10px">
        <strong>User trả về:</strong>
        <pre
          style="
            background: #f7f7f7;
            padding: 12px;
            overflow: auto;
            max-height: 180px;
          "
          >{{ pretty(lastUser) }}</pre
        >
      </div>
    </div>

    <!-- LOGIN -->
    <div style="padding: 12px; border: 1px solid #ddd">
      <h3>Login</h3>
      <div style="display: grid; gap: 8px">
        <input
          v-model="login.identifier"
          placeholder="identifier (username hoặc email)"
        />
        <input
          v-model="login.password"
          type="password"
          placeholder="password"
        />
        <button :disabled="loading" @click="handleLogin">
          {{ loading ? "Loading..." : "Login" }}
        </button>
      </div>
      <div v-if="lastUser" style="margin-top: 10px">
        <strong>User trả về:</strong>
        <pre
          style="
            background: #f7f7f7;
            padding: 12px;
            overflow: auto;
            max-height: 180px;
          "
          >{{ pretty(lastUser) }}</pre
        >
      </div>
    </div>

    <!-- TEST PROTECTED -->
    <div style="padding: 12px; border: 1px solid #ddd">
      <h3>Test Protected API (GET)</h3>
      <div style="display: grid; gap: 8px">
        <input
          v-model="protectedPath"
          placeholder="GET path (vd: /api/customer/profile)"
        />
        <button :disabled="loading" @click="handleTestProtected">
          {{ loading ? "Loading..." : "Send GET (Bearer token)" }}
        </button>
        <small style="color: #666">
          Nếu API trả 401, FE sẽ auto logout (xóa token).
        </small>
      </div>
    </div>

    <!-- RESPONSE -->
    <div style="padding: 12px; border: 1px solid #ddd">
      <h3>Response</h3>
      <div v-if="error" style="color: #b00020; white-space: pre-wrap">
        {{ error }}
      </div>
      <pre
        style="
          background: #f7f7f7;
          padding: 12px;
          overflow: auto;
          max-height: 360px;
        "
        >{{ pretty(result) }}</pre
      >
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  onMounted,
  onBeforeUnmount,
  watch,
} from "vue";
import { authApi } from "../api/auth.api";

const loading = ref(false);
const result = ref(null);
const error = ref("");

const token = ref(localStorage.getItem("access_token") || "");
const lastUser = ref(null);

const reg = reactive({ username: "", email: "", password: "" });
const login = reactive({ identifier: "", password: "" });

// đổi theo endpoint bạn có thật
const protectedPath = ref("/api/test/protected");

// ====== AUTO LOGOUT CONFIG ======
const IDLE_MINUTES = 30; // ✅ bạn đổi tuỳ ý
const IDLE_MS = IDLE_MINUTES * 60 * 1000;

let idleTimer = null;
let expTimer = null;

const jwtPayload = computed(() => decodeJwtPayload(token.value));

const tokenExpiresAt = computed(() => {
  const exp = jwtPayload.value?.exp; // seconds
  if (!exp) return "";
  const d = new Date(exp * 1000);
  return d.toLocaleString();
});

// ====== helpers ======
function pretty(obj) {
  try {
    return JSON.stringify(obj, null, 2);
  } catch {
    return String(obj);
  }
}

function normalizeError(e) {
  const data = e?.response?.data;
  const status = e?.response?.status;
  if (data) return `HTTP ${status}\n${pretty(data)}`;
  return e?.message || "Unknown error";
}

function decodeJwtPayload(jwt) {
  try {
    if (!jwt) return null;
    const parts = jwt.split(".");
    if (parts.length < 2) return null;

    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(
      base64.length + ((4 - (base64.length % 4)) % 4),
      "="
    );
    const json = atob(padded);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function setToken(t) {
  token.value = t || "";
  if (t) localStorage.setItem("access_token", t);
  else localStorage.removeItem("access_token");
}

function clearResult() {
  result.value = null;
  error.value = "";
}

// ✅ Logout “chuẩn FE”
function logout(reason = "Logout") {
  setToken("");
  lastUser.value = null;
  clearTimers();

  result.value = { message: "Logged out", reason };
  error.value = "";
}

// ====== idle + exp timers ======
function clearTimers() {
  if (idleTimer) clearTimeout(idleTimer);
  if (expTimer) clearTimeout(expTimer);
  idleTimer = null;
  expTimer = null;
}

function resetIdleTimer() {
  if (!token.value) return; // chỉ tính idle khi đang login
  if (idleTimer) clearTimeout(idleTimer);
  idleTimer = setTimeout(() => {
    logout(`Idle ${IDLE_MINUTES} minutes`);
  }, IDLE_MS);
}

// auto logout khi token hết hạn (dựa exp)
function scheduleTokenExpiryLogout() {
  if (!token.value) return;

  const exp = jwtPayload.value?.exp; // seconds
  if (!exp) return;

  const msLeft = exp * 1000 - Date.now();
  if (msLeft <= 0) {
    logout("Token expired");
    return;
  }

  if (expTimer) clearTimeout(expTimer);
  expTimer = setTimeout(() => logout("Token expired"), msLeft);
}

function attachActivityListeners() {
  const events = [
    "mousemove",
    "mousedown",
    "keydown",
    "scroll",
    "touchstart",
    "click",
  ];
  events.forEach((ev) =>
    window.addEventListener(ev, resetIdleTimer, { passive: true })
  );
  return () =>
    events.forEach((ev) => window.removeEventListener(ev, resetIdleTimer));
}

// ====== API actions ======
async function handleRegister() {
  loading.value = true;
  error.value = "";
  result.value = null;
  lastUser.value = null;

  try {
    const res = await authApi.register({ ...reg });
    result.value = res.data;

    const t = res?.data?.data?.token;
    const u = res?.data?.data?.user;
    if (u) lastUser.value = u;
    if (t) setToken(t);
  } catch (e) {
    error.value = normalizeError(e);
  } finally {
    loading.value = false;
  }
}

async function handleLogin() {
  loading.value = true;
  error.value = "";
  result.value = null;
  lastUser.value = null;

  try {
    const res = await authApi.login({ ...login });
    result.value = res.data;

    const t = res?.data?.data?.token;
    const u = res?.data?.data?.user;
    if (u) lastUser.value = u;
    if (t) setToken(t);
  } catch (e) {
    error.value = normalizeError(e);
  } finally {
    loading.value = false;
  }
}

async function handleTestProtected() {
  loading.value = true;
  error.value = "";
  result.value = null;

  try {
    const path = protectedPath.value.trim();
    const res = await authApi.testProtected(path);
    result.value = res.data;
  } catch (e) {
    error.value = normalizeError(e);
  } finally {
    loading.value = false;
  }
}

// ====== lifecycle ======
let detach = null;

onMounted(() => {
  // lắng nghe logout event từ http.js (401)
  window.addEventListener("auth:logout", (e) => {
    logout(e?.detail || "401 Unauthorized");
  });

  detach = attachActivityListeners();

  // nếu đã có token từ trước (localStorage) thì set timer ngay
  if (token.value) {
    resetIdleTimer();
    scheduleTokenExpiryLogout();
  }
});

onBeforeUnmount(() => {
  clearTimers();
  if (detach) detach();
});

// mỗi lần token đổi => reschedule
watch(token, () => {
  clearTimers();
  if (token.value) {
    resetIdleTimer();
    scheduleTokenExpiryLogout();
  }
});
</script>

<style scoped>
input {
  padding: 8px;
  border: 1px solid #bbb;
  border-radius: 4px;
}
button {
  padding: 8px;
  border: 1px solid #333;
  border-radius: 4px;
  cursor: pointer;
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
