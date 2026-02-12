<template>
  <el-container class="app-shell">
    <el-header v-if="showHeader" class="app-header">
      <div
        class="container-xl h-100 d-flex align-items-center justify-content-between gap-3"
      >
        <div class="d-flex align-items-center gap-3 min-w-0">
          <div
            class="brand d-flex align-items-center gap-2"
            role="button"
            @click="goHome"
            title="Tech Store"
          >
            <span class="dot"></span>
            <div class="brand-text">
              <div class="brand-name">Tech Store</div>
              <div class="brand-sub">Laptop • Gaming • Workstation</div>
            </div>
          </div>

          <div
            v-if="showSearch"
            class="d-none d-lg-flex align-items-center gap-2"
          >
            <el-input
              v-model="q"
              class="search"
              placeholder="Search laptop, CPU, RAM..."
              clearable
              @input="emitSearch"
            />
          </div>
        </div>

        <div class="d-flex align-items-center gap-2">
          <el-tag v-if="isAuthed" :type="portalTagType" effect="light">
            {{ portalLabel }}
          </el-tag>

          <template v-if="!isAuthed">
            <el-button type="primary" @click="goLogin">Login</el-button>
          </template>

          <template v-else>
            <el-dropdown>
              <span class="userbox d-flex align-items-center gap-2">
                <span class="avatar">{{ avatarLetter }}</span>
                <span class="d-none d-md-inline">{{ displayName }}</span>
                <i class="bi bi-chevron-down"></i>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="isSystemUser"
                    @click="router.push('/system/dashboard')"
                  >
                    System Dashboard
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isCustomer"
                    @click="router.push('/orders/new')"
                  >
                    Create Order
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="isCustomer"
                    @click="router.push('/orders/my-orders')"
                  >
                    My Orders
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="logout">
                    Logout
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "./ui/toast";
import {
  clearLastAuthResponse,
  clearSession,
  useAuthStore,
} from "./stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const q = ref("");

const isAuthed = computed(() => auth.isAuthed);
const isCustomer = computed(() => auth.isCustomer);
const isSystemUser = computed(() => isAuthed.value && !isCustomer.value);
const displayName = computed(() => auth.displayName);

const avatarLetter = computed(() => {
  const s = String(displayName.value || "U").trim();
  return (s[0] || "U").toUpperCase();
});

const showHeader = computed(() => route.meta?.hideHeader !== true);
const showSearch = computed(
  () =>
    route.name === "home" && (route.meta?.portal || "customer") === "customer"
);

const portalLabel = computed(() => (isCustomer.value ? "Customer" : "System"));
const portalTagType = computed(() =>
  isCustomer.value ? "primary" : "success"
);

function emitSearch() {
  window.dispatchEvent(new CustomEvent("products:search", { detail: q.value }));
}

function goHome() {
  if (!isAuthed.value) return router.push("/");
  return isCustomer.value ? router.push("/") : router.push("/system/dashboard");
}

function goLogin() {
  router.push("/login");
}

async function logout() {
  clearSession();
  clearLastAuthResponse();
  toast("Logged out.", "success");
  router.push(isSystemUser.value ? "/system/login" : "/login");
}

function onAutoLogout(e) {
  clearSession();
  clearLastAuthResponse();
  toast(
    `Session expired (${e?.detail || "401"}). Please login again.`,
    "warning"
  );
  router.push("/login");
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
  height: 64px;
  display: flex;
  align-items: center;
}

.brand {
  user-select: none;
  white-space: nowrap;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--ts-primary);
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

.search {
  width: 340px;
}

.userbox {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.65);
  cursor: pointer;
}
.avatar {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.12);
  color: rgba(29, 78, 216, 1);
  font-weight: 900;
}

.app-main {
  padding: 22px 16px;
}
</style>
