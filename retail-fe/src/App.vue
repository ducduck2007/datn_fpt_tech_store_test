<template>
  <router-view />
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import {
  clearSession,
  clearLastAuthResponse,
  getToken,
  getRole,
} from "./stores/auth";

const router = useRouter();
const token = computed(() => getToken());

function goHome() {
  const role = (getRole() || "").toUpperCase();
  if (token.value) {
    router.push(role === "CUSTOMER" ? "/dashboard" : "/system/dashboard");
  } else {
    router.push("/");
  }
}

function logout(reason = "Logout") {
  clearSession();
  clearLastAuthResponse();

  // đưa về đúng portal login
  const isSystem = window.location.pathname.startsWith("/system");
  router.replace(isSystem ? "/system/login" : "/");
}

function onAutoLogout(e) {
  logout(e?.detail || "401 Unauthorized");
}

onMounted(() => {
  window.addEventListener("auth:logout", onAutoLogout);
});

onBeforeUnmount(() => {
  window.removeEventListener("auth:logout", onAutoLogout);
});
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color);
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.9),
    rgba(255, 255, 255, 0.6)
  );
  backdrop-filter: blur(8px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
  cursor: pointer;
  user-select: none;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--el-color-primary);
  box-shadow: 0 0 16px rgba(64, 158, 255, 0.6);
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-main {
  padding: 24px 16px;
}
</style>
