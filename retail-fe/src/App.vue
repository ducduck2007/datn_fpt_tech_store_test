<!-- \retail-fe\src\App.vue -->
<template>
  <el-container class="app-shell">
    <el-header v-if="showHeader" class="app-header">
      <div class="header-inner">
        <div class="left">
          <div class="brand" @click="goHome" title="Tech Store">
            <span class="dot"></span>
            <div class="brand-text">
              <div class="brand-name">Tech Store</div>
              <div class="brand-sub">Laptop • Gaming • Workstation</div>
            </div>
          </div>

          <!-- Navbar (chỉ hiện ở landing / customer) -->
          <div v-if="showLandingNav" class="landing-nav">
            <el-input
              v-model="q"
              class="search"
              size="small"
              clearable
              placeholder="Tìm laptop, CPU, RAM..."
            />
            <div class="links">
              <el-link
                class="navlink"
                :underline="false"
                @click="scrollTo('featured')"
                >Sản phẩm</el-link
              >
              <el-link
                class="navlink"
                :underline="false"
                @click="scrollTo('benefits')"
                >Dịch vụ</el-link
              >
              <el-link
                class="navlink"
                :underline="false"
                @click="scrollTo('footer')"
                >Liên hệ</el-link
              >
            </div>
          </div>
        </div>

        <div class="right">
          <template v-if="isAuthed">
            <el-tag v-if="role" effect="dark" type="info" class="role-tag">{{
              role
            }}</el-tag>

            <div class="userbox" v-if="userLabel">
              <el-avatar class="avatar" :size="28">
                {{ (userLabel || "U").slice(0, 1).toUpperCase() }}
              </el-avatar>
              <span class="user"
                >Xin chào, <b>{{ userLabel }}</b></span
              >
            </div>

            <el-button
              type="danger"
              plain
              :loading="isLoggingOut"
              @click="logout"
            >
              Đăng xuất
            </el-button>
          </template>

          <template v-else>
            <el-button type="primary" @click="goLogin">Đăng nhập</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  clearSession,
  clearLastAuthResponse,
  getToken,
  getRole,
  getUser,
} from "./stores/auth";

const router = useRouter();
const route = useRoute();
const isLoggingOut = ref(false);

const isAuthed = computed(() => !!getToken());
const role = computed(() => (getRole() || "").toUpperCase());
const user = computed(() => getUser());

const userLabel = computed(() => {
  const u = user.value || {};
  return u.username || u.email || u.name || "";
});

const showHeader = computed(() => !route.meta?.hideHeader);
const showLandingNav = computed(
  () => route.meta?.portal === "customer" && route.path === "/"
);

// ===== Search -> bắn event cho CustomerHome lọc =====
const q = ref("");
let _t = null;
watch(q, (v) => {
  clearTimeout(_t);
  _t = setTimeout(() => {
    window.dispatchEvent(
      new CustomEvent("products:search", { detail: v || "" })
    );
  }, 200);
});

function scrollTo(id) {
  const el = document.getElementById(id);
  if (!el) return;
  el.scrollIntoView({ behavior: "smooth", block: "start" });
}

function goHome() {
  if (!isAuthed.value) return router.push("/");
  return router.push(role.value === "CUSTOMER" ? "/" : "/system/dashboard");
}

function goLogin() {
  if (route.path.startsWith("/system")) return router.push("/system/login");
  return router.push("/login");
}

async function doLogout(autoReason = "") {
  clearSession();
  clearLastAuthResponse();

  if (autoReason)
    ElMessage.warning("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
  else ElMessage.success("Đã đăng xuất.");

  if (route.path.startsWith("/system")) return router.replace("/system/login");
  return router.replace("/login");
}

async function logout() {
  try {
    await ElMessageBox.confirm("Bạn muốn đăng xuất?", "Xác nhận", {
      confirmButtonText: "Đăng xuất",
      cancelButtonText: "Hủy",
      type: "warning",
      closeOnClickModal: false,
      closeOnPressEscape: true,
    });
    await doLogout("");
  } catch {
    // cancel
  }
}

function onAutoLogout(e) {
  doLogout(e?.detail || "401");
}

onMounted(() => window.addEventListener("auth:logout", onAutoLogout));
onBeforeUnmount(() => window.removeEventListener("auth:logout", onAutoLogout));
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.92) 0%,
    rgba(246, 247, 251, 0.98) 35%,
    rgba(246, 247, 251, 1) 100%
  );
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.78);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--el-color-primary);
  box-shadow: 0 0 18px rgba(37, 99, 235, 0.38);
}

.brand-text {
  display: grid;
  line-height: 1.05;
}
.brand-name {
  font-weight: 900;
  letter-spacing: -0.2px;
}
.brand-sub {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
  font-weight: 600;
}

.landing-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.search {
  width: 340px;
}

.links {
  display: flex;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.65);
}

.navlink {
  padding: 2px 8px;
  border-radius: 999px;
}
.navlink:hover {
  background: rgba(37, 99, 235, 0.08);
}

/* Responsive: ẩn nav khi màn nhỏ */
@media (max-width: 900px) {
  .landing-nav {
    display: none;
  }
  .brand-sub {
    display: none;
  }
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.role-tag {
  border-radius: 999px;
}

.userbox {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.65);
}

.avatar {
  background: rgba(37, 99, 235, 0.12);
  color: rgba(29, 78, 216, 1);
  font-weight: 900;
}

.user {
  font-size: 13px;
  color: rgba(15, 23, 42, 0.8);
}

.app-main {
  padding: 22px 16px;
}
</style>
