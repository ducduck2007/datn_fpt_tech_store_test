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
    <el-alert
      v-if="alert"
      :title="alert"
      type="error"
      show-icon
      :closable="false"
      class="mb"
    />

    <el-form :model="form" label-position="top" @submit.prevent>
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
        />
      </el-form-item>

      <div class="row">
        <el-checkbox v-model="remember">Remember me</el-checkbox>
        <el-link type="primary" :underline="false">Quên mật khẩu?</el-link>
      </div>

      <el-button
        type="success"
        size="large"
        class="w100"
        :loading="loading"
        @click="handleLogin"
      >
        Login
      </el-button>

      <div class="muted mt">
        Bạn là khách hàng?
        <el-link type="primary" @click="goCustomerLogin"
          >Customer Login</el-link
        >
      </div>
    </el-form>
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
.mb {
  margin-bottom: 10px;
}
.mt {
  margin-top: 12px;
}
.w100 {
  width: 100%;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 0 14px;
}
.muted {
  color: rgba(0, 0, 0, 0.55);
  font-size: 13px;
}
</style>
