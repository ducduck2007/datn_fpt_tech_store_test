<template>
  <AuthLayout
    currentPortal="customer"
    :title="'Customer portal'"
    :desc="'Đăng nhập dành cho khách hàng. Tài khoản role khác sẽ được chuyển sang System portal.'"
    :hint="'Nếu login đúng nhưng role khác CUSTOMER → sẽ redirect /system/login.'"
    tag-text="Customer"
    tag-type="primary"
    kicker="Customer Portal"
    headline="Đăng nhập khách hàng"
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
          placeholder="vd: john / john@gmail.com"
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
        type="primary"
        size="large"
        class="w100"
        :loading="loading"
        @click="handleLogin"
      >
        Login
      </el-button>

      <div class="muted mt">
        Bạn là nhân viên/quản trị?
        <el-link type="primary" @click="goSystemLogin">System Login</el-link>
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
  if (v === "system") router.push("/system/login");
}

function goSystemLogin() {
  router.push("/system/login");
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

    if (role !== "CUSTOMER") {
      clearSession();
      clearLastAuthResponse();
      ElMessage.error(
        "Tài khoản này không phải CUSTOMER → chuyển sang System portal."
      );
      router.replace("/system/login");
      return;
    }

    setSession({ token, user });

    if (!remember.value) {
      // nếu muốn “không remember” thì có thể đổi localStorage -> sessionStorage
      // (tạm thời giữ đơn giản)
    }

    ElMessage.success("Đăng nhập CUSTOMER thành công!");
    router.replace("/dashboard");
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
