<template>
  <div class="auth">
    <header class="topbar">
      <div class="topbar__inner">
        <div class="logo">
          <span class="logo__dot"></span>
          <span class="logo__text">Tech Store</span>
        </div>
        <el-tag :type="tagType" effect="dark">{{ tagText }}</el-tag>
      </div>
    </header>

    <div class="shell">
      <section class="promo">
        <div class="promo__card">
          <div class="kicker">{{ kicker }}</div>
          <h1 class="headline">{{ headline }}</h1>
          <p class="desc">{{ desc }}</p>
          <p v-if="hint" class="hint">{{ hint }}</p>

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
            </div>
          </template>

          <slot />
        </el-card>
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
  background: linear-gradient(
    180deg,
    rgba(64, 158, 255, 0.1),
    rgba(255, 255, 255, 0)
  );
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
}
.logo__text {
  font-weight: 800;
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
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(0, 0, 0, 0.06);
}
.kicker {
  font-size: 12px;
  opacity: 0.7;
  font-weight: 700;
}
.headline {
  margin: 8px 0;
  font-size: 28px;
}
.desc {
  margin: 0;
  opacity: 0.85;
}
.hint {
  margin: 10px 0 0;
  opacity: 0.7;
  font-size: 13px;
}
.switch {
  margin-top: 14px;
}
.panel__card {
  border-radius: 18px;
}
.panel__title {
  font-weight: 800;
}
</style>
