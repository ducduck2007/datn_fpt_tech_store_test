<template>
  <div class="auth">
    <header class="topbar">
      <div class="topbar__inner">
        <div class="logo">
          <span class="logo__dot"></span>
          <div class="logo__txt">
            <div class="logo__text">Tech Store</div>
            <div class="logo__sub">Secure Access</div>
          </div>
        </div>
        <el-tag :type="tagType" effect="dark" class="pill">{{
          tagText
        }}</el-tag>
      </div>
    </header>

    <div class="shell">
      <section class="promo">
        <div class="promo__card">
          <div class="kicker">{{ kicker }}</div>
          <h1 class="headline">{{ headline }}</h1>
          <p class="desc">{{ desc }}</p>
          <p v-if="hint" class="hint">{{ hint }}</p>

          <div class="promo__badges">
            <el-tag round effect="plain" type="info">Bảo mật JWT</el-tag>
            <el-tag round effect="plain" type="info">Role-based Routing</el-tag>
            <el-tag round effect="plain" type="info">Element Plus UI</el-tag>
          </div>

          <div v-if="showPortalSwitch" class="switch">
            <el-segmented
              v-model="seg"
              :options="segOptions"
              @change="onSwitch?.(seg)"
            />
          </div>
        </div>
      </section>

      <section class="panel">
        <el-card class="panel__card" shadow="hover">
          <template #header>
            <div class="panel__hdr">
              <div class="panel__title">{{ title }}</div>
              <div class="panel__sub">Vui lòng nhập thông tin để tiếp tục</div>
            </div>
          </template>

          <slot />
        </el-card>

        <div class="panel__foot">
          <span class="ts-muted"
            >© {{ new Date().getFullYear() }} Tech Store</span
          >
          <span class="dot">•</span>
          <span class="ts-muted">Premium Laptop Commerce UI</span>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from "vue";

const props = defineProps({
  currentPortal: { type: String, default: "customer" },
  title: { type: String, default: "" },
  desc: { type: String, default: "" },
  hint: { type: String, default: "" },
  tagText: { type: String, default: "Portal" },
  tagType: { type: String, default: "info" },
  kicker: { type: String, default: "" },
  headline: { type: String, default: "" },
  onSwitch: { type: Function, default: null },

  // NEW
  showPortalSwitch: { type: Boolean, default: false },
});

const segOptions = [
  { label: "Customer", value: "customer" },
  { label: "System", value: "system" },
];

const seg = ref(props.currentPortal);
watchEffect(() => (seg.value = props.currentPortal));
</script>

<style scoped>
.auth {
  min-height: 100vh;
  padding-bottom: 18px;
  background: radial-gradient(
      1100px 520px at 10% -15%,
      rgba(37, 99, 235, 0.18),
      transparent 60%
    ),
    radial-gradient(
      900px 520px at 95% 0%,
      rgba(6, 182, 212, 0.14),
      transparent 55%
    ),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(246, 247, 251, 1));
}

.topbar {
  padding: 16px;
}

.topbar__inner {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo__dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--el-color-primary);
  box-shadow: 0 0 18px rgba(37, 99, 235, 0.35);
}
.logo__txt {
  line-height: 1.05;
}
.logo__text {
  font-weight: 900;
}
.logo__sub {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
  font-weight: 600;
}
.pill {
  border-radius: 999px;
}

.shell {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 16px 24px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 16px;
}

@media (max-width: 920px) {
  .shell {
    grid-template-columns: 1fr;
  }
}

.promo__card {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 10px 26px rgba(2, 6, 23, 0.06);
}

.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  letter-spacing: 0.2px;
  text-transform: uppercase;
}

.headline {
  margin: 10px 0 8px;
  font-size: 30px;
  letter-spacing: -0.4px;
}

.desc {
  margin: 0;
  opacity: 0.9;
  line-height: 1.6;
}

.hint {
  margin: 10px 0 0;
  opacity: 0.7;
  font-size: 13px;
  line-height: 1.55;
}

.promo__badges {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.switch {
  margin-top: 14px;
}

.panel__card {
  border-radius: 20px;
}

.panel__hdr {
  display: grid;
  gap: 2px;
}
.panel__title {
  font-weight: 900;
}
.panel__sub {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.58);
}

.panel__foot {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
}
.dot {
  opacity: 0.35;
}
</style>
