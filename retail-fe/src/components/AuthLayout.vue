<template>
  <div class="auth">
    <div class="auth__bg" />

    <div class="auth__shell">
      <!-- Left / Hero -->
      <section class="auth__hero">
        <div class="auth__brand">
          <span class="auth__dot"></span>
          <span class="auth__name">Retail FE</span>
          <el-tag class="auth__tag" :type="tagType" effect="dark">{{
            tagText
          }}</el-tag>
        </div>

        <h1 class="auth__title">{{ title }}</h1>
        <p class="auth__desc">{{ desc }}</p>

        <div class="auth__stats">
          <div class="auth__stat">
            <div class="auth__statNum">JWT</div>
            <div class="auth__statText">Role-based routing</div>
          </div>
          <div class="auth__stat">
            <div class="auth__statNum">401</div>
            <div class="auth__statText">Auto logout</div>
          </div>
          <div class="auth__stat">
            <div class="auth__statNum">UI</div>
            <div class="auth__statText">Element Plus</div>
          </div>
        </div>

        <div class="auth__hint">
          <el-icon class="auth__hintIcon"><InfoFilled /></el-icon>
          <span>{{ hint }}</span>
        </div>
      </section>

      <!-- Right / Form -->
      <section class="auth__panel">
        <el-card class="auth__card" shadow="never">
          <div class="auth__cardHeader">
            <div>
              <div class="auth__kicker">{{ kicker }}</div>
              <div class="auth__headline">{{ headline }}</div>
            </div>

            <el-segmented
              v-if="showSwitch"
              v-model="seg"
              :options="segOptions"
              size="default"
              class="auth__seg"
              @change="onSwitch(seg)"
            />
          </div>

          <el-divider class="auth__divider" />

          <slot />
        </el-card>

        <div class="auth__footer">
          <span>© {{ new Date().getFullYear() }} Retail FE</span>
          <span class="auth__dotSep">•</span>
          <span>Secure login</span>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from "vue";
import { InfoFilled } from "@element-plus/icons-vue";

const props = defineProps({
  title: String,
  desc: String,
  hint: String,
  tagText: { type: String, default: "Portal" },
  tagType: { type: String, default: "primary" },

  kicker: String,
  headline: String,

  showSwitch: { type: Boolean, default: true },
  currentPortal: { type: String, default: "customer" }, // "customer" | "system"
  onSwitch: { type: Function, default: () => {} },
});

const segOptions = [
  { label: "Customer", value: "customer" },
  { label: "System", value: "system" },
];
const seg = ref(props.currentPortal);

watchEffect(() => {
  seg.value = props.currentPortal;
});
</script>

<style scoped>
.auth {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}
.auth__bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(
      900px 600px at 20% 20%,
      rgba(64, 158, 255, 0.22),
      transparent 60%
    ),
    radial-gradient(
      900px 600px at 90% 30%,
      rgba(103, 194, 58, 0.18),
      transparent 55%
    ),
    radial-gradient(
      900px 600px at 40% 90%,
      rgba(230, 162, 60, 0.14),
      transparent 60%
    ),
    linear-gradient(180deg, #0b1220 0%, #0b0f14 100%);
}

.auth__shell {
  position: relative;
  z-index: 1;
  max-width: 1080px;
  margin: 0 auto;
  padding: 56px 18px;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 18px;
  align-items: stretch;
}

.auth__hero {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 22px;
  padding: 26px 26px 22px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(10px);
  color: rgba(255, 255, 255, 0.92);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.auth__brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.auth__dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #409eff;
  box-shadow: 0 0 18px rgba(64, 158, 255, 0.7);
}
.auth__name {
  font-weight: 800;
  letter-spacing: 0.2px;
}
.auth__tag {
  margin-left: auto;
}

.auth__title {
  margin: 18px 0 8px;
  font-size: 32px;
  line-height: 1.15;
  letter-spacing: -0.6px;
}
.auth__desc {
  margin: 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  line-height: 1.6;
  max-width: 46ch;
}

.auth__stats {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.auth__stat {
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 12px 12px;
  background: rgba(0, 0, 0, 0.18);
}
.auth__statNum {
  font-weight: 900;
  font-size: 16px;
  letter-spacing: 0.2px;
}
.auth__statText {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.68);
}

.auth__hint {
  margin-top: 14px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 12px;
}
.auth__hintIcon {
  margin-top: 2px;
}

.auth__panel {
  display: grid;
  align-content: start;
  gap: 12px;
}
.auth__card {
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.92);
}

.auth__cardHeader {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.auth__kicker {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
  font-weight: 700;
}
.auth__headline {
  margin-top: 2px;
  font-size: 18px;
  font-weight: 900;
  letter-spacing: -0.2px;
}
.auth__seg {
  min-width: 220px;
}

.auth__divider {
  margin: 14px 0 10px;
}

.auth__footer {
  color: rgba(255, 255, 255, 0.65);
  font-size: 12px;
  display: flex;
  gap: 8px;
  justify-content: center;
}
.auth__dotSep {
  opacity: 0.6;
}

@media (max-width: 980px) {
  .auth__shell {
    grid-template-columns: 1fr;
  }
  .auth__hero {
    display: none;
  }
  .auth__seg {
    min-width: 180px;
  }
}
</style>
