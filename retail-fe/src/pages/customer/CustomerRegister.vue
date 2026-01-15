<template>
  <AuthLayout
    currentPortal="customer"
    :title="'Customer register'"
    :desc="'Tạo tài khoản khách hàng mới.'"
    tag-text="Customer"
    tag-type="primary"
    kicker="Customer Portal"
    headline="Đăng ký"
    :showPortalSwitch="false"
  >
    <el-alert v-if="alert" :title="alert" type="error" show-icon class="mb" />

    <el-form :model="form" label-position="top" @submit.prevent>
      <el-form-item label="Username">
        <el-input v-model="form.username" autocomplete="username" />
      </el-form-item>

      <el-form-item label="Email">
        <el-input v-model="form.email" autocomplete="email" />
      </el-form-item>

      <el-form-item label="Mật khẩu">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>

      <el-form-item label="Nhập lại mật khẩu">
        <el-input
          v-model="form.password2"
          type="password"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>

      <div class="row">
        <span class="muted">Bạn đã có tài khoản?</span>
        <el-button link type="primary" @click="goLogin">Đăng nhập</el-button>
      </div>

      <el-button
        class="w100"
        type="primary"
        :loading="loading"
        @click="doRegister"
      >
        Tạo tài khoản
      </el-button>
    </el-form>
  </AuthLayout>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import AuthLayout from "../../components/AuthLayout.vue";
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
  const msg = e?.response?.data?.message || e?.message || "Đăng ký thất bại";
  return typeof msg === "string" ? msg : JSON.stringify(msg);
}

function goLogin() {
  router.push("/login");
}

async function doRegister() {
  alert.value = "";
  if (!form.username || !form.email || !form.password) {
    alert.value = "Vui lòng nhập đầy đủ thông tin.";
    return;
  }
  if (form.password !== form.password2) {
    alert.value = "Mật khẩu nhập lại không khớp.";
    return;
  }

  loading.value = true;
  try {
    await authApi.register({
      username: form.username,
      email: form.email,
      password: form.password,
    });

    ElMessage.success("Đăng ký thành công! Vui lòng đăng nhập.");
    router.replace("/login");
  } catch (e) {
    alert.value = errToText(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.mb {
  margin-bottom: 12px;
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
