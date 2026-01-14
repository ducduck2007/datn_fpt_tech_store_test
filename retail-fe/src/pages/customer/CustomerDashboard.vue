<template>
  <div class="wrap">
    <el-row :gutter="16">
      <el-col :xs="24" :md="16">
        <el-card class="card" shadow="hover">
          <template #header>
            <div class="hdr">
              <div>
                <div class="kicker">Customer Dashboard</div>
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

      <el-col :xs="24" :md="8">
        <el-card class="card" shadow="hover">
          <div class="h2">Gợi ý</div>
          <p class="muted">
            Trang này tạm thời chỉ hiển thị Response sau khi đăng nhập thành
            công.
          </p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getLastAuthResponse } from "../../stores/auth";

const resp = ref(null);

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
.h2 {
  font-weight: 800;
  margin-bottom: 6px;
}
.muted {
  color: var(--el-text-color-secondary);
  margin: 0;
}
.pre {
  margin: 0;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color);
  background: rgba(0, 0, 0, 0.03);
  max-height: 520px;
  overflow: auto;
}
</style>
