<template>
  <div class="promo-banner" @mouseenter="pauseAuto" @mouseleave="resumeAuto">
    <div class="slides-track" :style="{ transform: `translateX(-${current * 100}%)` }">
      <div
        v-for="(slide, i) in slides"
        :key="i"
        class="slide"
        :style="{ background: slide.bg }"
      >
        <div class="slide-inner">
          <div class="slide-content">
            <span class="slide-tag">{{ slide.tag }}</span>
            <h2 class="slide-title">{{ slide.title }}</h2>
            <p class="slide-sub">{{ slide.sub }}</p>
            <div class="slide-actions">
              <button class="btn-primary" @click="slide.action">{{ slide.cta }}</button>
              <button v-if="slide.cta2" class="btn-ghost" @click="slide.action2">{{ slide.cta2 }}</button>
            </div>
          </div>
          <div class="slide-visual">
            <div class="visual-glow" :style="{ background: slide.glowColor }"></div>
            <img :src="slide.img" :alt="slide.title" class="slide-img" loading="lazy" />
            <div class="badge-float" v-if="slide.badge">
              <span class="badge-val">{{ slide.badge.val }}</span>
              <span class="badge-label">{{ slide.badge.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <button class="nav-btn nav-prev" @click="prev" aria-label="Previous">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
    </button>
    <button class="nav-btn nav-next" @click="next" aria-label="Next">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
    </button>

    <div class="dots">
      <button
        v-for="(_, i) in slides"
        :key="i"
        class="dot"
        :class="{ active: i === current }"
        @click="goTo(i)"
      />
    </div>

    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressWidth + '%', transition: progressTransition }"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const DURATION = 5000;

const slides = [
  {
    tag: "🔥 HOT DEAL",
    title: "Laptop Gaming RTX 4060",
    sub: "Chinh phục mọi tựa game — hiệu năng đỉnh cao, giá không tưởng",
    cta: "Mua ngay",
    cta2: "Xem tất cả",
    bg: "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%)",
    glowColor: "radial-gradient(circle, rgba(99,102,241,0.35) 0%, transparent 70%)",
    img: "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=500&q=80",
    badge: { val: "-25%", label: "GIẢM" },
    action: () => router.push("/"),
    action2: () => router.push("/"),
  },
  {
    tag: "✨ MỚI VỀ",
    title: "MacBook Pro M3 Pro",
    sub: "Chip Apple M3 Pro — mỏng nhẹ, pin 22 giờ, hiệu năng pro thực thụ",
    cta: "Khám phá",
    cta2: null,
    bg: "linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)",
    glowColor: "radial-gradient(circle, rgba(14,165,233,0.3) 0%, transparent 70%)",
    img: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&q=80",
    badge: { val: "NEW", label: "2024" },
    action: () => router.push("/"),
    action2: null,
  },
  {
    tag: "⚡ FLASH SALE",
    title: "Laptop Văn Phòng Dell XPS",
    sub: "Thiết kế sang trọng, siêu mỏng — đồng hành cùng mọi chuyến công tác",
    cta: "Mua ngay",
    cta2: "So sánh",
    bg: "linear-gradient(135deg, #0d1b2a 0%, #1b263b 50%, #415a77 100%)",
    glowColor: "radial-gradient(circle, rgba(251,146,60,0.25) 0%, transparent 70%)",
    img: "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=500&q=80",
    badge: { val: "TRẢ GÓP", label: "0%" },
    action: () => router.push("/"),
    action2: () => router.push("/"),
  },
];

const current = ref(0);
const progressWidth = ref(0);
const progressTransition = ref("none");
let timer = null;
let startTime = null;
let elapsed = 0;

function next() { goTo((current.value + 1) % slides.length); }
function prev() { goTo((current.value - 1 + slides.length) % slides.length); }

function goTo(i) {
  current.value = i;
  resetProgress();
  startProgress();
}

function resetProgress() {
  progressTransition.value = "none";
  progressWidth.value = 0;
  elapsed = 0;
}

function startProgress() {
  startTime = performance.now();
  timer = requestAnimationFrame(tick);
}

function tick(now) {
  elapsed = now - startTime;
  const pct = Math.min((elapsed / DURATION) * 100, 100);
  progressTransition.value = "none";
  progressWidth.value = pct;
  if (elapsed >= DURATION) {
    next();
  } else {
    timer = requestAnimationFrame(tick);
  }
}

function pauseAuto() {
  cancelAnimationFrame(timer);
}

function resumeAuto() {
  startTime = performance.now() - elapsed;
  timer = requestAnimationFrame(tick);
}

onMounted(() => startProgress());
onBeforeUnmount(() => cancelAnimationFrame(timer));
</script>

<style scoped>
.promo-banner {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 32px;
  height: 320px;
  user-select: none;
}

@media (max-width: 768px) {
  .promo-banner { height: auto; min-height: 420px; }
}

.slides-track {
  display: flex;
  height: 100%;
  transition: transform 0.55s cubic-bezier(0.77, 0, 0.175, 1);
}

.slide {
  flex: 0 0 100%;
  height: 100%;
  min-height: 320px;
  position: relative;
}

.slide-inner {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  height: 100%;
  padding: 36px 48px;
  gap: 24px;
}

@media (max-width: 768px) {
  .slide-inner {
    grid-template-columns: 1fr;
    padding: 28px 24px;
    text-align: center;
  }
}

.slide-content { z-index: 2; }

.slide-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #fbbf24;
  background: rgba(251,191,36,0.12);
  border: 1px solid rgba(251,191,36,0.25);
  border-radius: 99px;
  padding: 4px 12px;
  margin-bottom: 14px;
}

.slide-title {
  font-size: clamp(22px, 3vw, 34px);
  font-weight: 800;
  color: #f8fafc;
  line-height: 1.2;
  margin: 0 0 12px;
  letter-spacing: -0.02em;
}

.slide-sub {
  font-size: 14px;
  color: rgba(248,250,252,0.65);
  line-height: 1.6;
  margin: 0 0 24px;
  max-width: 380px;
}

.slide-actions { display: flex; gap: 12px; flex-wrap: wrap; }

.btn-primary {
  padding: 10px 22px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.15s, transform 0.1s;
}
.btn-primary:hover { opacity: 0.9; transform: translateY(-1px); }
.btn-primary:active { transform: translateY(0); }

.btn-ghost {
  padding: 10px 22px;
  background: rgba(255,255,255,0.08);
  color: rgba(248,250,252,0.85);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-ghost:hover { background: rgba(255,255,255,0.14); }

.slide-visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 240px;
}

@media (max-width: 768px) {
  .slide-visual { height: 180px; }
}

.visual-glow {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.slide-img {
  width: 100%;
  max-width: 340px;
  height: 220px;
  object-fit: cover;
  border-radius: 12px;
  position: relative;
  z-index: 1;
  box-shadow: 0 20px 60px rgba(0,0,0,0.4);
}

@media (max-width: 768px) {
  .slide-img { height: 160px; max-width: 260px; }
}

.badge-float {
  position: absolute;
  top: 12px; right: 12px;
  z-index: 2;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  border-radius: 12px;
  padding: 8px 14px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(239,68,68,0.4);
}
.badge-val { display: block; font-size: 18px; font-weight: 900; color: #fff; line-height: 1; }
.badge-label { display: block; font-size: 9px; font-weight: 700; letter-spacing: 0.1em; color: rgba(255,255,255,0.85); text-transform: uppercase; margin-top: 2px; }

.nav-btn {
  position: absolute;
  top: 50%; transform: translateY(-50%);
  width: 40px; height: 40px;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.15);
  color: #fff;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s;
  z-index: 10;
  backdrop-filter: blur(8px);
}
.nav-btn:hover { background: rgba(255,255,255,0.2); }
.nav-prev { left: 16px; }
.nav-next { right: 16px; }

.dots {
  position: absolute;
  bottom: 16px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 6px;
  z-index: 10;
}

.dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: rgba(255,255,255,0.35);
  border: none;
  cursor: pointer;
  transition: width 0.25s, background 0.2s;
  padding: 0;
}
.dot.active { width: 24px; border-radius: 4px; background: #fff; }

.progress-bar {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 3px;
  background: rgba(255,255,255,0.1);
  z-index: 10;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}
</style>