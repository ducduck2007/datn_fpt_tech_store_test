<!-- FILE: src/pages/customer/CustomerRegister.vue -->
<template>
  <AuthLayout
    currentPortal="customer"
    :title="'Customer register'"
    :desc="'Create a customer account.'"
    tag-text="Customer"
    tag-type="primary"
    kicker="Customer Portal"
    headline="Register"
    :showPortalSwitch="false"
  >
    <el-form
      :model="form"
      label-position="top"
      class="d-grid gap-3"
      @submit.prevent
    >
      <el-alert v-if="alert" :title="alert" type="error" show-icon />

      <el-form-item label="Username">
        <el-input
          v-model="form.username"
          autocomplete="username"
          placeholder="username"
        />
      </el-form-item>

      <el-form-item label="Email">
        <el-input
          v-model="form.email"
          autocomplete="email"
          placeholder="user@mail.com"
        />
      </el-form-item>

      <el-form-item label="Password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>

      <el-form-item label="Confirm password">
        <el-input
          v-model="form.password2"
          type="password"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>

      <div class="d-flex align-items-center justify-content-between">
        <span class="text-muted">
          Already have an account?
          <el-link type="primary" @click="router.push('/login')">Login</el-link>
        </span>
      </div>

      <el-button
        type="primary"
        size="large"
        :loading="loading"
        @click="doRegister"
        >Create account</el-button
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

const router = useRouter();
const loading = ref(false);
const alert = ref("");

const form = reactive({
  username: "",
  email: "",
  password: "",
  password2: "",
});

function errToText(e) {
  const msg = e?.response?.data?.message || e?.message || "Register failed";
  return typeof msg === "string" ? msg : JSON.stringify(msg);
}

async function doRegister() {
  alert.value = "";
  if (!form.username || !form.email || !form.password) {
    alert.value = "Please fill all required fields.";
    return;
  }
  if (form.password !== form.password2) {
    alert.value = "Passwords do not match.";
    return;
  }

  loading.value = true;
  try {
    await authApi.register({
      username: form.username,
      email: form.email,
      password: form.password,
    });
    toast("Register successful. Please login.", "success");
    router.replace("/login");
  } catch (e) {
    alert.value = errToText(e);
  } finally {
    loading.value = false;
  }
}
</script>
