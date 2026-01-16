<template>
  <AuthLayout
    currentPortal="system"
    :title="'System portal'"
    :desc="'Đăng nhập dành cho nhân viên/quản trị. CUSTOMER sẽ bị chặn và chuyển về Customer portal.'"
    :hint="'Role-based routing: SYSTEM → /system/dashboard, CUSTOMER → /dashboard.'"
    tag-text="System"
    tag-type="success"
    kicker="System Portal"
    headline="Đăng nhập hệ thống"
    :onSwitch="handleSwitch"
    :showPortalSwitch="true"
  >
    <div class="d-grid gap-3">
      <div
        v-if="alert"
        class="alert alert-danger d-flex align-items-center gap-2 mb-0"
        role="alert"
      >
        <i class="bi bi-exclamation-triangle-fill"></i>
        <div style="white-space: pre-wrap">{{ alert }}</div>
      </div>

      <div class="intro">
        <div class="intro__title">Quản trị & vận hành</div>
        <div class="intro__sub">
          Dành cho nhân viên / quản trị viên. Hệ thống sẽ tự chặn role CUSTOMER.
        </div>
      </div>

      <form @submit.prevent class="d-grid gap-3">
        <div>
          <label class="form-label fw-bold"
            >Identifier (username hoặc email)</label
          >
          <input
            v-model="form.identifier"
            class="form-control form-control-lg ts-input"
            placeholder="vd: admin / staff@mail.com"
          />
        </div>

        <div>
          <label class="form-label fw-bold">Password</label>
          <div class="input-group input-group-lg">
            <input
              v-model="form.password"
              class="form-control ts-input"
              :type="showPass ? 'text' : 'password'"
              placeholder="Nhập mật khẩu"
            />
            <button
              class="btn btn-outline-secondary"
              type="button"
              @click="showPass = !showPass"
            >
              <i class="bi" :class="showPass ? 'bi-eye-slash' : 'bi-eye'"></i>
            </button>
          </div>
        </div>

        <div class="d-flex align-items-center justify-content-between">
          <div class="form-check">
            <input
              id="remember"
              class="form-check-input"
              type="checkbox"
              v-model="remember"
            />
            <label class="form-check-label" for="remember">Remember me</label>
          </div>

          <button type="button" class="btn btn-link p-0 fw-bold">
            Quên mật khẩu?
          </button>
        </div>

        <button
          class="btn btn-success btn-lg ts-btn w-100"
          type="button"
          :disabled="loading"
          @click="handleLogin"
        >
          <span
            v-if="loading"
            class="spinner-border spinner-border-sm me-2"
            aria-hidden="true"
          ></span>
          Login
        </button>

        <div class="switch text-center ts-muted">
          Bạn là khách hàng?
          <button
            type="button"
            class="btn btn-link fw-bold"
            @click="goCustomerLogin"
          >
            Đăng nhập khách hàng
          </button>
        </div>
      </form>
    </div>
  </AuthLayout>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import AuthLayout from "../../components/AuthLayout.vue";
import { toast } from "../../ui/toast";

import { authApi } from "../../api/auth.api";
import {
  setSession,
  setLastAuthResponse,
  clearSession,
  clearLastAuthResponse,
} from "../../stores/auth";

const router = useRouter();
const loading = ref(false);
const alert = ref("");
const remember = ref(true);
const showPass = ref(false);

const form = reactive({
  identifier: "",
  password: "",
});

function handleSwitch(v) {
  if (v === "customer") router.push("/");
}

function goCustomerLogin() {
  router.push("/");
}

function errToText(e) {
  const data = e?.response?.data;
  if (data)
    return typeof data === "string" ? data : JSON.stringify(data, null, 2);
  return e?.message || "Login failed";
}

async function handleLogin() {
  loading.value = true;
  alert.value = "";

  try {
    const res = await authApi.login({ ...form });
    const data = res?.data;

    const token = data?.data?.token;
    const user = data?.data?.user;

    setLastAuthResponse(data);

    const role = (user?.role || "").toUpperCase();

    if (role === "CUSTOMER") {
      clearSession();
      clearLastAuthResponse();
      toast(
        "CUSTOMER không được đăng nhập System portal → chuyển về Customer portal.",
        "danger"
      );
      router.replace("/");
      return;
    }

    setSession({ token, user });

    if (!remember.value) {
      // keep behavior (no changes)
    }

    toast("Đăng nhập SYSTEM thành công!", "success");
    router.replace("/system/dashboard");
  } catch (e) {
    alert.value = errToText(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.intro {
  display: grid;
  gap: 4px;
}
.intro__title {
  font-weight: 900;
  letter-spacing: -0.2px;
  font-size: 16px;
}
.intro__sub {
  font-size: 13px;
  color: rgba(15, 23, 42, 0.6);
  line-height: 1.55;
}
</style>
