<!-- src/App.vue -->
<template>
  <div class="app-shell">
    <!-- Sticky Navbar -->
    <header v-if="showHeader" class="app-header">
      <div
        class="container-xl h-100 d-flex align-items-center justify-content-between gap-3"
      >
        <div class="d-flex align-items-center gap-3 min-w-0">
          <!-- Brand -->
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

          <!-- Landing nav (only on customer home) -->
          <div
            v-if="showLandingNav"
            class="landing-nav d-none d-lg-flex align-items-center gap-2"
          >
            <input
              v-model="q"
              class="form-control form-control-sm ts-input search"
              placeholder="Tìm laptop, CPU, RAM..."
            />
            <div class="links d-flex align-items-center gap-1">
              <button
                class="btn btn-sm btn-light ts-pill"
                @click="scrollTo('featured')"
              >
                Sản phẩm
              </button>
              <button
                class="btn btn-sm btn-light ts-pill"
                @click="scrollTo('benefits')"
              >
                Dịch vụ
              </button>
              <button
                class="btn btn-sm btn-light ts-pill"
                @click="scrollTo('footer')"
              >
                Liên hệ
              </button>
            </div>
          </div>
        </div>

        <div class="d-flex align-items-center gap-2 flex-shrink-0">
          <template v-if="isAuthed">
            <span
              v-if="role"
              class="badge text-bg-secondary ts-pill text-uppercase"
              >{{ role }}</span
            >

            <div
              v-if="userLabel"
              class="userbox d-none d-md-flex align-items-center gap-2"
            >
              <div class="avatar">
                {{ (userLabel || "U").slice(0, 1).toUpperCase() }}
              </div>
              <span class="small">
                Xin chào, <b>{{ userLabel }}</b>
              </span>
            </div>

            <button
              class="btn btn-outline-danger ts-btn"
              :disabled="isLoggingOut"
              @click="logout"
            >
              <span
                v-if="isLoggingOut"
                class="spinner-border spinner-border-sm me-2"
                aria-hidden="true"
              ></span>
              Đăng xuất
            </button>
          </template>

          <template v-else>
            <button class="btn btn-primary ts-btn" @click="goLogin">
              Đăng nhập
            </button>
          </template>
        </div>
      </div>
    </header>

    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "./ui/toast";
import { confirmModal } from "./ui/confirm";

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

// ===== Search -> dispatch event for CustomerHome =====
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
    toast("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", "warning");
  else toast("Đã đăng xuất.", "success");

  if (route.path.startsWith("/system")) return router.replace("/system/login");
  return router.replace("/login");
}

async function logout() {
  const ok = await confirmModal(
    "Bạn muốn đăng xuất?",
    "Xác nhận",
    "Đăng xuất",
    true
  );
  if (!ok) return;
  isLoggingOut.value = true;
  try {
    await doLogout("");
  } finally {
    isLoggingOut.value = false;
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
  height: 64px;
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
