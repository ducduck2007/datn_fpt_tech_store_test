<!-- \retail-fe\src\pages\system\SystemDashboard.vue -->
<template>
  <div class="wrap">
    <div class="page-hdr">
      <div>
        <div class="kicker">System</div>
        <div class="title">Dashboard</div>
        <div class="sub ts-muted">
          Màn hình demo kiểm tra session và token (console-first).
        </div>
      </div>

      <div class="actions">
        <el-tag type="success" effect="dark" class="pill">{{
          role || "n/a"
        }}</el-tag>
        <el-button type="primary" plain @click="reload"
          >Reload (log console)</el-button
        >
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <el-card class="card" shadow="hover">
          <template #header>
            <div class="hdr">
              <div>
                <div class="h">Console mode</div>
                <div class="ts-muted small">
                  Không hiển thị JSON/JWT payload trên UI. Mở DevTools → Console
                  để xem chi tiết.
                </div>
              </div>
              <el-tag round effect="plain" type="info">Safe UI</el-tag>
            </div>
          </template>

          <el-alert
            title="Đã bỏ hiển thị JSON Response/JWT payload trên UI. Mở DevTools → Console để xem."
            type="info"
            show-icon
            :closable="false"
          />

          <div class="note ts-muted">
            Tip: Token dài sẽ được hiển thị ở panel bên phải (readonly).
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card class="card" shadow="hover">
          <template #header>
            <div class="hdr">
              <div class="h">Session / Token</div>
              <el-tag type="success" effect="dark">{{ role || "n/a" }}</el-tag>
            </div>
          </template>

          <div class="stats">
            <div class="stat">
              <div class="stat__k ts-muted">Token</div>
              <div class="stat__v">
                <el-tag v-if="token" type="success" effect="plain"
                  >Available</el-tag
                >
                <el-tag v-else type="danger" effect="plain">Missing</el-tag>
              </div>
            </div>

            <div class="stat">
              <div class="stat__k ts-muted">Length</div>
              <div class="stat__v">
                <b>{{ token ? token.length : 0 }}</b>
              </div>
            </div>
          </div>

          <div class="block">
            <div class="label">Access token</div>
            <el-input :model-value="token" readonly type="textarea" :rows="6" />
            <div class="ts-muted small">
              (Readonly) — phục vụ debug nhanh trong demo.
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import {
  getLastAuthResponse,
  getToken,
  getRole,
  decodeJwtPayload,
} from "../../stores/auth";

const token = computed(() => getToken());
const role = computed(() => (getRole() || "").toUpperCase());

function reload() {
  console.log("[SystemDashboard] token?", !!getToken());
  console.log("[SystemDashboard] role=", (getRole() || "").toUpperCase());
  console.log("[SystemDashboard] lastAuthResponse=", getLastAuthResponse());
  console.log(
    "[SystemDashboard] jwtPayload(decoded)=",
    decodeJwtPayload(getToken())
  );
}

onMounted(reload);
</script>

<style scoped>
.wrap {
  max-width: 1100px;
  margin: 0 auto;
}

.page-hdr {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 4px 6px;
}

.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  letter-spacing: 0.2px;
  text-transform: uppercase;
}

.title {
  font-weight: 950;
  font-size: 22px;
  letter-spacing: -0.3px;
}

.sub {
  font-size: 13px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pill {
  border-radius: 999px;
}

.card {
  border-radius: 18px;
}

.hdr {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.h {
  font-weight: 900;
  font-size: 16px;
  letter-spacing: -0.2px;
}

.small {
  font-size: 12px;
}

.note {
  margin-top: 10px;
  font-size: 12px;
}

.stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 12px;
}

.stat {
  padding: 10px 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.6);
}
.stat__k {
  font-size: 12px;
  margin-bottom: 4px;
}
.stat__v {
  font-size: 14px;
}

.block {
  display: grid;
  gap: 8px;
}

.label {
  font-weight: 800;
}

@media (max-width: 640px) {
  .page-hdr {
    flex-direction: column;
    align-items: flex-start;
  }
  .actions {
    width: 100%;
    justify-content: space-between;
  }
  .stats {
    grid-template-columns: 1fr;
  }
}
</style>
