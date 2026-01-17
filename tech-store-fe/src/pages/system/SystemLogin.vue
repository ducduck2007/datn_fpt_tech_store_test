<!-- FILE: src/pages/system/SystemLogin.vue -->
<template>
  <AuthLayout
    currentPortal="system"
    :title="'System portal'"
    :desc="'Login for staff/admin. CUSTOMER accounts will be redirected to Customer portal.'"
    :hint="'Role-based routing: SYSTEM → /system/dashboard, CUSTOMER → /'"
    tag-text="System"
    tag-type="success"
    kicker="System Portal"
    headline="System Login"
    :showPortalSwitch="false"
  >
    <el-form
      :model="form"
      label-position="top"
      class="d-grid gap-3"
      @submit.prevent
    >
      <el-alert v-if="alert" :title="alert" type="error" show-icon />

      <el-form-item label="Identifier (username or email)">
        <el-input
          v-model="form.identifier"
          autocomplete="username"
          placeholder="admin / staff@mail.com"
        />
      </el-form-item>

      <el-form-item label="Password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          autocomplete="current-password"
        />
      </el-form-item>

      <div class="d-flex align-items-center justify-content-between">
        <el-link type="primary" @click="$router.push('/login')"
          >Customer login</el-link
        >
      </div>

      <el-button type="success" size="large" :loading="loading" @click="doLogin"
        >Login</el-button
      >
    </el-form>
  </AuthLayout>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import AuthLayout from "../../components/AuthLayout.vue";
import { toast } from "../../ui/toast";
import { authApi } from "../../api/auth.api";
import { useAuthStore } from "../../stores/auth";

const router = useRouter();
const auth = useAuthStore();

const loading = ref(false);
const alert = ref("");

const form = reactive({
  identifier: "",
  password: "",
});

function errToText(e) {
  const msg = e?.response?.data?.message || e?.message || "Login failed";
  return typeof msg === "string" ? msg : JSON.stringify(msg);
}

function pickTokenUser(payload) {
  const root = payload?.data ?? payload;
  const token =
    root?.token ??
    root?.data?.token ??
    payload?.token ??
    payload?.data?.data?.token;
  const user =
    root?.user ??
    root?.data?.user ??
    payload?.user ??
    payload?.data?.data?.user;
  return { token, user };
}

async function doLogin() {
  loading.value = true;
  alert.value = "";
  try {
    const res = await authApi.login({ ...form });
    const data = res?.data;
    const { token, user } = pickTokenUser(data);

    const role = String(user?.role || "").toUpperCase();
    if (role === "CUSTOMER") {
      toast(
        "This is a CUSTOMER account. Redirecting to Customer portal.",
        "warning"
      );
      return router.replace("/login");
    }

    auth.setLastAuthResponse(data);
    auth.setSession({ token, user });

    toast("System login successful.", "success");
    router.replace("/system/dashboard");
  } catch (e) {
    alert.value = errToText(e);
  } finally {
    loading.value = false;
  }
}
</script>
