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
        <div class="intro__title">Tạo tài khoản Tech Store</div>
        <div class="intro__sub">
          Nhận ưu đãi cá nhân hoá, theo dõi đơn hàng và tích điểm thành viên.
        </div>
      </div>

      <form @submit.prevent class="d-grid gap-3">
        <div>
          <label class="form-label fw-bold">Username</label>
          <input
            v-model="form.username"
            class="form-control form-control-lg ts-input"
            placeholder="vd: nguyenvana"
            autocomplete="username"
          />
        </div>

        <div>
          <label class="form-label fw-bold">Email</label>
          <input
            v-model="form.email"
            class="form-control form-control-lg ts-input"
            placeholder="vd: user@mail.com"
            autocomplete="email"
          />
        </div>

        <div>
          <label class="form-label fw-bold">Mật khẩu</label>
          <input
            v-model="form.password"
            class="form-control form-control-lg ts-input"
            type="password"
            placeholder="Tối thiểu 8 ký tự (khuyến nghị)"
            autocomplete="new-password"
          />
        </div>

        <div>
          <label class="form-label fw-bold">Nhập lại mật khẩu</label>
          <input
            v-model="form.password2"
            class="form-control form-control-lg ts-input"
            type="password"
            placeholder="Nhập lại mật khẩu"
            autocomplete="new-password"
          />
        </div>

        <div class="d-flex align-items-center justify-content-between">
          <div class="ts-muted">
            Bạn đã có tài khoản?
            <button
              type="button"
              class="btn btn-link p-0 fw-bold"
              @click="goLogin"
            >
              Đăng nhập
            </button>
          </div>
        </div>

        <button
          class="btn btn-primary btn-lg ts-btn w-100"
          type="button"
          :disabled="loading"
          @click="doRegister"
        >
          <span
            v-if="loading"
            class="spinner-border spinner-border-sm me-2"
            aria-hidden="true"
          ></span>
          Tạo tài khoản
        </button>

        <div class="foot ts-muted text-center">
          Bằng việc đăng ký, bạn đồng ý với <b>điều khoản</b> và
          <b>chính sách</b> (demo).
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

    toast("Đăng ký thành công! Vui lòng đăng nhập.", "success");
    router.replace("/login");
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
  margin-top: 6px;
  font-size: 12px;
}
</style>
