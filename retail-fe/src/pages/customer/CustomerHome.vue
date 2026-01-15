<template>
  <div class="wrap">
    <el-row :gutter="16">
      <el-col :xs="24" :md="10" v-if="isCustomerAuthed">
        <el-card class="card" shadow="hover">
          <template #header>
            <div class="hdr">
              <div>
                <div class="kicker">Customer</div>
                <div class="h">Thông tin khách hàng</div>
              </div>
              <el-button type="primary" plain @click="reload">Reload</el-button>
            </div>
          </template>

          <div class="block">
            <div class="label">User</div>
            <pre class="pre">{{ pretty(user) }}</pre>
          </div>

          <div class="block">
            <div class="label">Response</div>
            <pre class="pre">{{ pretty(lastResponse) }}</pre>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="isCustomerAuthed ? 14 : 24">
        <el-card class="card" shadow="hover">
          <template #header>
            <div class="hdr">
              <div>
                <div class="kicker">Tech Store</div>
                <div class="h">Sản phẩm (demo)</div>
              </div>
            </div>
          </template>

          <el-row :gutter="12">
            <el-col
              v-for="p in products"
              :key="p.id"
              :xs="24"
              :sm="12"
              :md="12"
              :lg="8"
            >
              <el-card class="p-card" shadow="never">
                <div class="p-title">{{ p.name }}</div>
                <div class="p-meta">
                  {{ p.cpu }} • {{ p.ram }} • {{ p.ssd }}
                </div>
                <div class="p-price">{{ p.price }}</div>
                <el-button class="mt" type="primary" plain disabled>
                  Xem chi tiết
                </el-button>
              </el-card>
            </el-col>
          </el-row>

          <el-alert
            v-if="!isAuthed"
            class="mt"
            title="Bạn đang xem ở chế độ khách. Nhấn Đăng nhập để xem thông tin tài khoản."
            type="info"
            show-icon
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import {
  getLastAuthResponse,
  getRole,
  getToken,
  getUser,
} from "../../stores/auth";

const isAuthed = computed(() => !!getToken());
const isCustomerAuthed = computed(
  () => isAuthed.value && (getRole() || "").toUpperCase() === "CUSTOMER"
);

const user = ref(getUser());
const lastResponse = ref(getLastAuthResponse());

function reload() {
  user.value = getUser();
  lastResponse.value = getLastAuthResponse();
}

function pretty(v) {
  return JSON.stringify(v ?? null, null, 2);
}

const products = [
  {
    id: 1,
    name: "Laptop Gaming A1",
    cpu: "i7-13620H",
    ram: "16GB",
    ssd: "512GB",
    price: "25,990,000₫",
  },
  {
    id: 2,
    name: "Ultrabook B2",
    cpu: "i5-1335U",
    ram: "16GB",
    ssd: "1TB",
    price: "21,490,000₫",
  },
  {
    id: 3,
    name: "Workstation C3",
    cpu: "Ryzen 7 7840HS",
    ram: "32GB",
    ssd: "1TB",
    price: "32,990,000₫",
  },
  {
    id: 4,
    name: "Student D4",
    cpu: "i5-1240P",
    ram: "8GB",
    ssd: "256GB",
    price: "14,990,000₫",
  },
  {
    id: 5,
    name: "Creator E5",
    cpu: "i7-13700H",
    ram: "32GB",
    ssd: "1TB",
    price: "39,990,000₫",
  },
  {
    id: 6,
    name: "Office F6",
    cpu: "Ryzen 5 7530U",
    ram: "16GB",
    ssd: "512GB",
    price: "16,990,000₫",
  },
];
</script>

<style scoped>
.wrap {
  max-width: 1200px;
  margin: 0 auto;
}
.card {
  border-radius: 16px;
}
.hdr {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.kicker {
  font-size: 12px;
  opacity: 0.7;
  font-weight: 700;
}
.h {
  font-size: 18px;
  font-weight: 900;
}
.block {
  display: grid;
  gap: 8px;
  margin-bottom: 14px;
}
.label {
  font-weight: 800;
}
.pre {
  margin: 0;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color);
  background: rgba(0, 0, 0, 0.03);
  max-height: 380px;
  overflow: auto;
}
.p-card {
  border-radius: 14px;
}
.p-title {
  font-weight: 900;
  margin-bottom: 6px;
}
.p-meta {
  font-size: 13px;
  opacity: 0.7;
  margin-bottom: 10px;
}
.p-price {
  font-size: 16px;
  font-weight: 900;
}
.mt {
  margin-top: 10px;
}
</style>
