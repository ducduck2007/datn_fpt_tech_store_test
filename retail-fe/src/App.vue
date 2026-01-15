<template>
  <el-container class="app-shell">
    <el-header v-if="showHeader" class="app-header">
      <div class="brand" @click="goHome">
        <span class="dot"></span>
        <span class="brand-name">Tech Store</span>
      </div>

      <div class="right">
        <template v-if="isAuthed">
          <el-tag v-if="role" effect="dark" type="info">{{ role }}</el-tag>
          <span class="user" v-if="userLabel">{{ userLabel }}</span>

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
    </el-header>

    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
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

const isAuthed = computed(() => !!getToken()); // ✅ giờ đã reactive
const role = computed(() => (getRole() || "").toUpperCase());
const user = computed(() => getUser());

const userLabel = computed(() => {
  const u = user.value || {};
  return u.username || u.email || u.name || "";
});

const showHeader = computed(() => !route.meta?.hideHeader);

function goHome() {
  if (!isAuthed.value) return router.push("/");
  return router.push(role.value === "CUSTOMER" ? "/" : "/system/dashboard");
}

function goLogin() {
  // nếu đang ở system pages mà chưa login -> đưa về system/login
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
  background: #f6f7fb;
}
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.92);
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--el-color-primary);
  box-shadow: 0 0 16px rgba(64, 158, 255, 0.35);
}
.brand-name {
  font-weight: 900;
  letter-spacing: -0.2px;
}
.right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
}
.app-main {
  padding: 22px 16px;
}
</style>
