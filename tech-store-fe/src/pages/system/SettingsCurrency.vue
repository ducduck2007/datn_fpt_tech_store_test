<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Settings - Default Currency</div>
          <div class="muted">GET/PUT /api/settings/currency/default</div>
        </div>
        <el-button @click="load" :loading="loading">Reload</el-button>
      </div>

      <el-divider />

      <el-form label-position="top" class="row g-3">
        <div class="col-12 col-md-4">
          <el-form-item label="currencyCode">
            <el-select v-model="currencyCode" placeholder="Select currency">
              <el-option label="VND" value="VND" />
              <el-option label="USD" value="USD" />
              <el-option label="EUR" value="EUR" />
              <el-option label="JPY" value="JPY" />
              <el-option label="THB" value="THB" />
            </el-select>
          </el-form-item>
        </div>
        <div class="col-12 col-md-8 d-flex align-items-end justify-content-end">
          <el-button type="primary" :loading="saving" @click="save"
            >Save</el-button
          >
        </div>
      </el-form>

      <el-divider />

      <pre class="json">{{ JSON.stringify(raw, null, 2) }}</pre>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { settingsApi } from "../../api/settings.api";
import { toast } from "../../ui/toast";

const loading = ref(false);
const saving = ref(false);

const currencyCode = ref("VND");
const raw = ref(null);

function pickCurrency(payload) {
  const root = payload?.data ?? payload;
  return (
    root?.currencyCode ??
    root?.data?.currencyCode ??
    root?.data?.data?.currencyCode ??
    null
  );
}

async function load() {
  loading.value = true;
  try {
    const res = await settingsApi.getDefaultCurrency();
    raw.value = res?.data ?? null;
    const c = pickCurrency(res?.data);
    if (c) currencyCode.value = String(c);
  } catch {
    toast("Failed to load default currency.", "error");
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (!currencyCode.value) return toast("currencyCode is required.", "warning");
  saving.value = true;
  try {
    await settingsApi.setDefaultCurrency(currencyCode.value);
    toast("Default currency updated.", "success");
    await load();
  } catch {
    toast("Update failed.", "error");
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
}
.title {
  font-weight: 900;
  font-size: 18px;
}
.muted {
  color: rgba(15, 23, 42, 0.62);
  font-size: 13px;
}
.json {
  margin: 0;
  font-size: 12px;
  background: rgba(2, 6, 23, 0.04);
  border: 1px solid rgba(2, 6, 23, 0.08);
  border-radius: 10px;
  padding: 12px;
  overflow: auto;
}
</style>
