<template>
  <AuthLayout
    currentPortal="customer"
    :title="'Customer login'"
    :desc="'Đăng nhập dành cho khách hàng (CUSTOMER).'"
    tag-text="Customer"
    tag-type="primary"
    kicker="Customer Portal"
    headline="Đăng nhập"
    :showPortalSwitch="false"
  >
    <div class="d-grid gap-3">
      <div
        v-if="alert"
        class="alert alert-danger d-flex align-items-center gap-2 mb-0"
        role="alert"
      >
        <i class="bi bi-exclamation-triangle-fill"></i>
        <div>{{ alert }}</div>
      </div>

      <div class="intro">
        <div class="intro__title">Chào mừng bạn quay lại</div>
        <div class="intro__sub">
          Đăng nhập để xem đơn hàng, tích điểm và nhận ưu đãi theo tài khoản.
        </div>
      </div>

      <form @submit.prevent class="d-grid gap-3">
        <div>
          <label class="form-label fw-bold">Email hoặc Username</label>
          <input
            v-model="form.identifier"
            class="form-control form-control-lg ts-input"
            placeholder="vd: nguyenvana / user@mail.com"
            autocomplete="username"
          />
        </div>

        <div>
          <label class="form-label fw-bold">Mật khẩu</label>
          <div class="input-group input-group-lg">
            <input
              v-model="form.password"
              class="form-control ts-input"
              :type="showPass ? 'text' : 'password'"
              placeholder="Nhập mật khẩu của bạn"
              autocomplete="current-password"
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
          <div class="ts-muted">
            Bạn chưa có tài khoản?
            <button
              type="button"
              class="btn btn-link p-0 fw-bold"
              @click="goRegister"
            >
              Đăng ký ngay
            </button>
          </div>
        </div>

        <button
          class="btn btn-primary btn-lg ts-btn w-100"
          type="button"
          :disabled="loading"
          @click="doLogin"
        >
          <span
            v-if="loading"
            class="spinner-border spinner-border-sm me-2"
            aria-hidden="true"
          ></span>
          Đăng nhập
        </button>

        <div class="foot ts-muted text-center">
          <span class="dot">•</span> Bảo mật phiên đăng nhập
          <span class="dot">•</span> Hỗ trợ tư vấn cấu hình
          <span class="dot">•</span> Đổi trả theo chính sách
        </div>

        <div class="switch text-center">
          <span class="ts-muted">Bạn là nhân viên/quản trị?</span>
          <button
            type="button"
            class="btn btn-link fw-bold"
            @click="$router.push('/system/login')"
          >
            Đăng nhập hệ thống
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
const showPass = ref(false);

const form = reactive({
  identifier: "",
  password: "",
});

function errToText(e) {
  const msg = e?.response?.data?.message || e?.message || "Đăng nhập thất bại";
  return typeof msg === "string" ? msg : JSON.stringify(msg);
}

function goRegister() {
  router.push("/register");
}

async function doLogin() {
  loading.value = true;
  alert.value = "";

  try {
    const res = await authApi.login({ ...form });
    const data = res?.data;

    const token = data?.data?.token;
    const user = data?.data?.user;

    setLastAuthResponse(data);

    const role = (user?.role || "").toUpperCase();
    if (role !== "CUSTOMER") {
      clearSession();
      clearLastAuthResponse();
      toast(
        "Tài khoản không phải CUSTOMER. Vui lòng đăng nhập ở System portal.",
        "warning"
      );
      return router.replace("/system/login");
    }

    setSession({ token, user });
    toast("Đăng nhập thành công!", "success");
    router.replace("/"); // back home
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
.foot {
  font-size: 12px;
}
.dot {
  opacity: 0.35;
  padding: 0 6px;
}
</style>
