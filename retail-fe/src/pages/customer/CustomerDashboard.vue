<!-- \retail-fe\src\pages\customer\CustomerDashboard.vue -->
<template>
  <div class="wrap">
    <el-card class="card" shadow="hover">
      <template #header>
        <div class="hdr">
          <div>
            <div class="kicker">Customer Dashboard</div>
            <div class="h">Console mode</div>
          </div>
          <el-button type="primary" plain @click="reload"
            >Reload (log console)</el-button
          >
        </div>
      </template>

      <el-alert
        title="Đã bỏ hiển thị JSON trên UI. Mở DevTools (F12) → Console để xem user/response."
        type="info"
        show-icon
        :closable="false"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import {
  getLastAuthResponse,
  getToken,
  getRole,
  getUser,
} from "../../stores/auth";

function reload() {
  console.log("[CustomerDashboard] token?", !!getToken());
  console.log("[CustomerDashboard] role=", (getRole() || "").toUpperCase());
  console.log("[CustomerDashboard] user=", getUser());
  console.log("[CustomerDashboard] lastAuthResponse=", getLastAuthResponse());
}

onMounted(reload);
</script>

<style scoped>
.wrap {
  max-width: 1100px;
  margin: 0 auto;
}
.card {
  border-radius: 16px;
}
.hdr {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.kicker {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.h {
  font-weight: 900;
  font-size: 18px;
}
</style>
