<!-- \retail-fe\src\pages\system\SystemLogin.vue -->
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
  >
    <div class="auth">
      <el-alert
        v-if="alert"
        :title="alert"
        type="error"
        show-icon
        :closable="false"
        class="mb"
      />

      <div class="intro">
        <div class="intro__title">Quản trị & vận hành</div>
        <div class="intro__sub">
          Dành cho nhân viên / quản trị viên. Hệ thống sẽ tự chặn role CUSTOMER.
        </div>
      </div>

      <el-form class="form" :model="form" label-position="top" @submit.prevent>
        <el-form-item label="Identifier (username hoặc email)">
          <el-input
            v-model="form.identifier"
            placeholder="vd: admin / staff@mail.com"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item label="Password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            size="large"
            clearable
            placeholder="Nhập mật khẩu"
          />
        </el-form-item>

        <div class="row">
          <el-checkbox v-model="remember">Remember me</el-checkbox>
          <el-link type="primary" :underline="false" class="link"
            >Quên mật khẩu?</el-link
          >
        </div>

        <el-button
          type="success"
          size="large"
          class="w100 cta"
          :loading="loading"
          @click="handleLogin"
        >
          Login
        </el-button>

        <div class="switch ts-muted mt">
          Bạn là khách hàng?
          <el-link
            type="primary"
            :underline="false"
            class="link"
            @click="goCustomerLogin"
          >
            Customer Login
          </el-link>
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
const remember = ref(true);

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
      ElMessage.error(
        "CUSTOMER không được đăng nhập System portal → chuyển về Customer portal."
      );
      router.replace("/");
      return;
    }

    setSession({ token, user });

    if (!remember.value) {
      // tuỳ bạn xử lý remember
    }

    ElMessage.success("Đăng nhập SYSTEM thành công!");
    router.replace("/system/dashboard");
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
.mt {
  margin-top: 12px;
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

.w100 {
  width: 100%;
}

.cta {
  border-radius: 14px;
  font-weight: 900;
  letter-spacing: 0.1px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 2px 0 12px;
}

.link {
  font-weight: 800;
}

.switch {
  text-align: center;
  font-size: 13px;
}
</style>
