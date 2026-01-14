<template>
  <div class="wrap">
    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <el-card class="card" shadow="hover">
          <template #header>
            <div class="hdr">
              <div>
                <div class="kicker">System Dashboard</div>
                <div class="h">Response</div>
              </div>
              <el-button type="primary" plain @click="reload">Reload</el-button>
            </div>
          </template>

          <el-empty
            v-if="!resp"
            description="Chưa có response (hãy login trước)"
          />
          <pre v-else class="pre">{{ pretty(resp) }}</pre>
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

          <div class="block">
            <div class="label">Access token</div>
            <el-input :model-value="token" readonly type="textarea" :rows="4" />
          </div>

          <div class="block">
            <div class="label">JWT payload (decoded)</div>
            <pre class="pre small">{{ pretty(jwtPayload) }}</pre>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import {
  getLastAuthResponse,
  getToken,
  getRole,
  decodeJwtPayload,
} from "../../stores/auth";

const resp = ref(null);
const token = computed(() => getToken());
const role = computed(() => (getRole() || "").toUpperCase());

const jwtPayload = computed(() => decodeJwtPayload(token.value));

function pretty(obj) {
  try {
    return JSON.stringify(obj, null, 2);
  } catch {
    return String(obj);
  }
}

function reload() {
  resp.value = getLastAuthResponse();
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
  font-weight: 800;
  font-size: 18px;
}
.block {
  display: grid;
  gap: 8px;
  margin-bottom: 14px;
}
.label {
  font-weight: 700;
}
.pre {
  margin: 0;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color);
  background: rgba(0, 0, 0, 0.03);
  max-height: 420px;
  overflow: auto;
}
.small {
  max-height: 240px;
}
</style>
