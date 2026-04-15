<template>
  <el-carousel height="300px" :interval="5000" arrow="always" indicator-position="inside"
    style="border-radius: 4px; overflow: hidden; margin-bottom: 24px;">
    <el-carousel-item v-for="(slide, i) in slides" :key="i">
      <div :style="{ background: slide.bg, height: '100%', display: 'flex', alignItems: 'center' }">
        <el-row align="middle" style="width: 100%; padding: 0 48px;" :gutter="24">
          <!-- Content -->
          <el-col :xs="24" :sm="14">
            <el-space direction="vertical" :size="12" alignment="flex-start">
              <!-- Tag -->
              <el-tag :type="slide.tagType" effect="plain" size="default" :round="false">
                {{ slide.tag }}
              </el-tag>
              <span
                style="font-size: clamp(20px, 2.8vw, 32px); font-weight: 800; color: #f8fafc; line-height: 1.2; display: block;">
                {{ slide.title }}
              </span>
              <span
                style="font-size: 14px; color: rgba(248,250,252,0.65); line-height: 1.6; display: block; max-width: 380px;">
                {{ slide.sub }}
              </span>
              <el-space wrap :size="12">
                <el-button type="primary" size="large" color="#6366f1" @click="slide.action">
                  {{ slide.cta }}
                </el-button>
                <el-button v-if="slide.cta2" size="large" @click="slide.action2"
                  style="background: rgba(255,255,255,0.1); border-color: rgba(255,255,255,0.25); color: #f8fafc;">
                  {{ slide.cta2 }}
                </el-button>
              </el-space>
            </el-space>
          </el-col>

          <!-- Image -->
          <el-col :xs="0" :sm="10"
            style="display: flex; align-items: center; justify-content: center; height: 260px; position: relative;">
            <el-image :src="slide.img" :alt="slide.title" fit="cover"
              style="width: 100%; max-width: 320px; height: 210px; border-radius: 4px; box-shadow: 0 20px 60px rgba(0,0,0,0.4);" />
          </el-col>
        </el-row>
      </div>
    </el-carousel-item>
  </el-carousel>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

const slides = [
  {
    tag: "🔥 HOT DEAL",
    tagType: "warning",
    title: "MSI Katana 15 B13V",
    sub: "Chinh phục mọi tựa game — hiệu năng đỉnh cao, giá không tưởng",
    cta: "Khám phá sản phẩm",
    cta2: null,
    bg: "#0f172a",
    img: "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=500&q=80",
    badge: { val: "-25%", label: "GIẢM" },
    action: () => router.push("/product/11"),
    action2: null,
  },
  {
    tag: "🔥 HOT DEAL",
    tagType: "warning",
    title: "MacBook Pro M3 Pro",
    sub: "Chip Apple M3 Pro — mỏng nhẹ, pin 22 giờ, hiệu năng pro thực thụ",
    cta: "Khám phá sản phẩm",
    cta2: null,
    bg: "#16213e",
    img: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&q=80",
    badge: { val: "NEW", label: "2024" },
    action: () => router.push("/product/10"),
    action2: null,
  },
  {
    tag: "🔥 HOT DEAL",
    tagType: "warning",
    title: "Laptop Dell XPS",
    sub: "Thiết kế sang trọng, siêu mỏng — đồng hành cùng mọi chuyến công tác",
    cta: "Khám phá sản phẩm",
    cta2: null,
    bg: "#1b263b",
    img: "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=500&q=80",
    badge: { val: "0%", label: "TRẢ GÓP" },
    action: () => router.push("/product/1"), 
    action2: null,
  },
];
</script>