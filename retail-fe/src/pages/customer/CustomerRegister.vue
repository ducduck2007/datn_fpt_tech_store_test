<!-- \retail-fe\src\pages\customer\CustomerRegister.vue -->
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
    <div class="auth">
      <el-alert v-if="alert" :title="alert" type="error" show-icon class="mb" />

      <div class="intro">
        <div class="intro__title">Tạo tài khoản Tech Store</div>
        <div class="intro__sub">
          Nhận ưu đãi cá nhân hoá, theo dõi đơn hàng và tích điểm thành viên.
        </div>
      </div>

      <el-form class="form" :model="form" label-position="top" @submit.prevent>
        <el-form-item label="Username">
          <el-input
            v-model="form.username"
            size="large"
            clearable
            placeholder="vd: nguyenvana"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item label="Email">
          <el-input
            v-model="form.email"
            size="large"
            clearable
            placeholder="vd: user@mail.com"
            autocomplete="email"
          />
        </el-form-item>

        <el-form-item label="Mật khẩu">
          <el-input
            v-model="form.password"
            size="large"
            clearable
            placeholder="Tối thiểu 8 ký tự (khuyến nghị)"
            type="password"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>

        <el-form-item label="Nhập lại mật khẩu">
          <el-input
            v-model="form.password2"
            size="large"
            clearable
            placeholder="Nhập lại mật khẩu"
            type="password"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>

        <div class="row">
          <div class="hint">
            <span class="ts-muted">Bạn đã có tài khoản?</span>
            <el-button link type="primary" class="link" @click="goLogin">
              Đăng nhập
            </el-button>
          </div>
        </div>

        <el-button
          class="w100 cta"
          type="primary"
          size="large"
          :loading="loading"
          @click="doRegister"
        >
          Tạo tài khoản
        </el-button>

        <div class="foot ts-muted">
          Bằng việc đăng ký, bạn đồng ý với <b>điều khoản</b> và
          <b>chính sách</b> (demo).
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
  color: rgba(15, 23, 42, 0.58);
}
</style>
