<!-- \retail-fe\src\pages\customer\CustomerLogin.vue -->
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
    <div class="auth">
      <el-alert v-if="alert" :title="alert" type="error" show-icon class="mb" />

      <div class="intro">
        <div class="intro__title">Chào mừng bạn quay lại</div>
        <div class="intro__sub">
          Đăng nhập để xem đơn hàng, tích điểm và nhận ưu đãi theo tài khoản.
        </div>
      </div>

      <el-form class="form" :model="form" label-position="top" @submit.prevent>
        <el-form-item label="Email hoặc Username">
          <el-input
            v-model="form.identifier"
            size="large"
            clearable
            placeholder="vd: nguyenvana / user@mail.com"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item label="Mật khẩu">
          <el-input
            v-model="form.password"
            size="large"
            clearable
            placeholder="Nhập mật khẩu của bạn"
            type="password"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <div class="row">
          <div class="hint">
            <span class="ts-muted">Bạn chưa có tài khoản?</span>
            <el-button link type="primary" class="link" @click="goRegister">
              Đăng ký ngay
            </el-button>
          </div>
        </div>

        <el-button
          class="w100 cta"
          type="primary"
          size="large"
          :loading="loading"
          @click="doLogin"
        >
          Đăng nhập
        </el-button>

        <div class="foot ts-muted">
          <span class="dot">•</span> Bảo mật phiên đăng nhập
          <span class="dot">•</span> Hỗ trợ tư vấn cấu hình
          <span class="dot">•</span> Đổi trả theo chính sách
        </div>

        <div class="switch">
          <span class="ts-muted">Bạn là nhân viên/quản trị?</span>
          <el-button
            link
            type="success"
            class="link"
            @click="$router.push('/system/login')"
          >
            Đăng nhập hệ thống
          </el-button>
        </div>
      </el-form>
    </div>
  </AuthLayout>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import AuthLayout from "../../components/AuthLayout.vue";

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
      ElMessage.warning(
        "Tài khoản không phải CUSTOMER. Vui lòng đăng nhập ở System portal."
      );
      return router.replace("/system/login");
    }

    setSession({ token, user });
    ElMessage.success("Đăng nhập thành công!");
    router.replace("/"); // ✅ về home
  } catch (e) {
    alert.value = errToText(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth {
  display: grid;
  gap: 12px;
}

.mb {
  margin-bottom: 6px;
}

.intro {
  display: grid;
  gap: 4px;
  margin-bottom: 2px;
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

.form :deep(.el-form-item__label) {
  font-weight: 800;
  color: rgba(15, 23, 42, 0.82);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 2px 0 12px;
}

.hint {
  display: flex;
  align-items: center;
  gap: 6px;
}

.w100 {
  width: 100%;
}

.cta {
  border-radius: 14px;
  font-weight: 900;
  letter-spacing: 0.1px;
}

.link {
  font-weight: 900;
}

.foot {
  margin-top: 10px;
  font-size: 12px;
  text-align: center;
}
.dot {
  opacity: 0.35;
  padding: 0 6px;
}

.switch {
  margin-top: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
</style>
