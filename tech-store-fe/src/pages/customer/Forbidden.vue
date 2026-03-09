<!-- FILE: src/pages/Forbidden.vue -->
<template>
  <div class="forbidden-root">
    <div class="bg-layer">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="forbidden-card">
      <div class="code-badge">403</div>

      <div class="lock-wrap">
        <svg class="lock-icon" width="48" height="48" viewBox="0 0 24 24" fill="none">
          <rect x="3" y="11" width="18" height="11" rx="2.5"
            stroke="currentColor" stroke-width="1.5"/>
          <path d="M7 11V7a5 5 0 0 1 10 0v4"
            stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <circle cx="12" cy="16.5" r="1.25" fill="currentColor"/>
        </svg>
        <div class="lock-ring ring-1"></div>
        <div class="lock-ring ring-2"></div>
      </div>

      <h1 class="title">Access Denied</h1>
      <p class="subtitle">
        You don't have permission to view this page.<br>
        This incident may be logged.
      </p>

      <div class="divider"></div>

      <div class="actions">
        <button class="btn-primary" @click="goBack">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M19 12H5M12 5l-7 7 7 7"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Go Back
        </button>
        <button class="btn-ghost" @click="goHome">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
              stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <polyline points="9 22 9 12 15 12 15 22"
              stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
          </svg>
          Home
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goBack() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.replace("/");
  }
}

function goHome() {
  router.replace("/");
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;1,9..40,300&display=swap');

.forbidden-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2fa;
  font-family: 'DM Sans', sans-serif;
  position: fixed;
  inset: 0;
  z-index: 9999;
  overflow: hidden;
  padding: 24px;
}

/* ─── Background ─── */
.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.2;
}

.orb-1 {
  width: 480px; height: 480px;
  background: radial-gradient(circle, #ffb3b3, transparent 70%);
  top: -160px; right: -80px;
  animation: drift1 14s ease-in-out infinite alternate;
}

.orb-2 {
  width: 360px; height: 360px;
  background: radial-gradient(circle, #ffd6a5, transparent 70%);
  bottom: -120px; left: -80px;
  animation: drift2 17s ease-in-out infinite alternate;
}

.bg-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(0,0,0,0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.035) 1px, transparent 1px);
  background-size: 48px 48px;
}

@keyframes drift1 {
  from { transform: translate(0, 0); }
  to   { transform: translate(-30px, 40px); }
}
@keyframes drift2 {
  from { transform: translate(0, 0); }
  to   { transform: translate(30px, -30px); }
}

/* ─── Card ─── */
.forbidden-card {
  position: relative;
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 24px;
  padding: 44px 40px 40px;
  backdrop-filter: blur(24px);
  box-shadow:
    0 0 0 1px rgba(255,255,255,0.9) inset,
    0 32px 64px rgba(200, 80, 80, 0.08),
    0 0 80px rgba(255, 100, 80, 0.05);
  text-align: center;
  animation: cardEnter 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(28px) scale(0.96); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* ─── 403 Badge ─── */
.code-badge {
  display: inline-block;
  font-family: 'DM Sans', monospace;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #e05555;
  background: rgba(224, 85, 85, 0.08);
  border: 1px solid rgba(224, 85, 85, 0.2);
  padding: 4px 14px;
  border-radius: 100px;
  margin-bottom: 24px;
}

/* ─── Lock icon ─── */
.lock-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.lock-icon {
  color: #e05555;
  position: relative;
  z-index: 1;
  animation: lockPulse 3s ease-in-out infinite;
}

.lock-ring {
  position: absolute;
  border-radius: 50%;
  border: 1.5px solid rgba(224, 85, 85, 0.15);
  animation: ripple 3s ease-out infinite;
}

.ring-1 {
  width: 72px; height: 72px;
  animation-delay: 0s;
}

.ring-2 {
  width: 96px; height: 96px;
  animation-delay: 0.6s;
}

@keyframes lockPulse {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.04); }
}

@keyframes ripple {
  0%   { transform: scale(0.8); opacity: 0.6; }
  100% { transform: scale(1.4); opacity: 0; }
}

/* ─── Text ─── */
.title {
  font-family: 'Instrument Serif', serif;
  font-size: 28px;
  font-weight: 400;
  color: #1a1d2e;
  margin: 0 0 10px;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.subtitle {
  font-size: 13.5px;
  color: rgba(20, 25, 60, 0.45);
  margin: 0;
  font-weight: 300;
  line-height: 1.7;
}

/* ─── Divider ─── */
.divider {
  height: 1px;
  background: rgba(0,0,0,0.07);
  margin: 28px 0;
}

/* ─── Actions ─── */
.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 42px;
  padding: 0 20px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #e05555 0%, #c93a3a 100%);
  color: #fff;
  font-family: 'DM Sans', sans-serif;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.2s;
  box-shadow: 0 4px 16px rgba(224, 85, 85, 0.25);
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(224, 85, 85, 0.35);
}

.btn-primary:active { transform: translateY(0); }

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 42px;
  padding: 0 20px;
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 10px;
  background: rgba(0,0,0,0.03);
  color: rgba(20, 25, 60, 0.55);
  font-family: 'DM Sans', sans-serif;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-ghost:hover {
  background: rgba(0,0,0,0.06);
  color: rgba(20, 25, 60, 0.8);
  border-color: rgba(0,0,0,0.15);
}
</style>