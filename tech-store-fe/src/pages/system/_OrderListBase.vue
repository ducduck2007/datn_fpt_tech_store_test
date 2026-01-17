<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">{{ title }}</div>
          <div class="muted">{{ endpoint }}</div>
        </div>
        <el-button @click="load" :loading="loading">Reload</el-button>
      </div>

      <el-divider />

      <el-table
        :data="rows"
        border
        :loading="loading"
        @row-click="goDetail"
        style="cursor: pointer"
      >
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column prop="status" label="Status" width="160" />
        <el-table-column prop="customerId" label="customerId" width="160" />
        <el-table-column
          prop="paymentMethod"
          label="paymentMethod"
          width="180"
        />
        <el-table-column prop="channel" label="channel" width="140" />
        <el-table-column prop="createdAt" label="createdAt" min-width="220" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "../../ui/toast";

const props = defineProps({
  title: { type: String, required: true },
  endpoint: { type: String, required: true },
  loader: { type: Function, required: true },
});

const router = useRouter();
const loading = ref(false);
const rows = ref([]);

function extractList(payload) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  const root = payload?.data ?? payload;
  if (Array.isArray(root)) return root;
  for (const k of ["content", "items", "results", "rows", "list"]) {
    if (Array.isArray(root?.[k])) return root[k];
    if (Array.isArray(root?.data?.[k])) return root.data[k];
  }
  return [];
}

function normalize(list) {
  return (list || []).map((o, idx) => ({
    id: o?.id ?? o?.orderId ?? idx + 1,
    status: o?.status ?? "—",
    customerId: o?.customerId ?? o?.customer?.id ?? "—",
    paymentMethod: o?.paymentMethod ?? "—",
    channel: o?.channel ?? "—",
    createdAt: o?.createdAt ?? o?.createdDate ?? "",
    raw: o,
  }));
}

async function load() {
  loading.value = true;
  try {
    const res = await props.loader();
    rows.value = normalize(extractList(res?.data));
  } catch {
    rows.value = [];
    toast("Failed to load orders.", "error");
  } finally {
    loading.value = false;
  }
}

function goDetail(row) {
  const id = row?.id;
  if (!id) return;
  router.push(`/system/orders/${id}`);
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
</style>
