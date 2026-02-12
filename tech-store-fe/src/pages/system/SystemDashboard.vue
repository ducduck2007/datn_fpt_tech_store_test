<!-- FILE: src/pages/system/SystemDashboard.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">System</div>
          <div class="title">Dashboard</div>
          <div class="muted">Quick access to admin modules</div>
        </div>
        <el-button @click="reload">Reload</el-button>
      </div>

      <el-divider />

      <div class="row g-3">
        <div
          class="col-12 col-md-6 col-xl-4"
          v-for="c in cards"
          :key="c.title"
        >
          <el-card
            shadow="hover"
            class="h-100"
            @click="$router.push(c.to)"
            style="cursor: pointer"
          >
            <div class="fw-bold">{{ c.title }}</div>
            <div class="muted mt-1">{{ c.desc }}</div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>

  <!-- REVENUE SECTION -->
  <el-card class="mt-4" shadow="never">
    <template #header>
      <div class="d-flex justify-content-between align-items-center w-100">
        <div>
          <div class="fw-bold">Doanh thu theo khách hàng</div>
          <div class="muted">
            {{ revenueList.length }} khách hàng
          </div>
        </div>

        <div class="total-revenue">
          {{ totalRevenue.toLocaleString() }} đ
        </div>
      </div>
    </template>

    <!-- TABLE -->
    <el-table
      :data="revenueList"
      stripe
      border
      v-loading="loadingRevenue"
      style="width:100%"
    >
      <el-table-column
        label="#"
        type="index"
        width="60"
      />

      <el-table-column
        prop="customerName"
        label="Khách hàng"
      />

      <el-table-column
        label="Doanh thu"
        align="right"
      >
        <template #default="scope">
          <span class="money">
            {{ scope.row.totalRevenue?.toLocaleString() }} đ
          </span>
        </template>
      </el-table-column>

      <el-table-column
        label="Tỷ trọng"
        align="right"
      >
        <template #default="scope">
          {{
            totalRevenue
              ? (
                  (scope.row.totalRevenue / totalRevenue) *
                  100
                ).toFixed(1)
              : 0
          }}%
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="Không có dữ liệu" />
      </template>
    </el-table>
  </el-card>
</template>

<script setup>
import { toast } from "../../ui/toast";
import { ref, onMounted, computed } from "vue";
import { ordersApi } from "../../api/orders.api";

const revenueList = ref([]);
const loadingRevenue = ref(false);
const revenueDateRange = ref([]);

const totalRevenue = computed(() =>
  revenueList.value.reduce(
    (sum, i) => sum + (i.totalRevenue || 0),
    0
  )
);

const loadRevenueByCustomer = async () => {
  loadingRevenue.value = true;
  try {
    let res;

    if (
      revenueDateRange.value &&
      revenueDateRange.value.length === 2
    ) {
      res = await ordersApi.getRevenueByCustomer(
        new Date(revenueDateRange.value[0]).toISOString(),
        new Date(revenueDateRange.value[1]).toISOString()
      );
    } else {
      res = await ordersApi.getRevenueByCustomer();
    }

    revenueList.value = res.data || [];
  } finally {
    loadingRevenue.value = false;
  }
};

onMounted(() => {
  loadRevenueByCustomer();
});

const cards = [
  { title: "Users", desc: "Manage system users & roles", to: "/system/users" },
  {
    title: "Customers",
    desc: "Customers & loyalty (VIP/REGULAR)",
    to: "/system/customers",
  },
  {
    title: "Categories",
    desc: "Create & manage product categories",
    to: "/system/categories",
  },
  { title: "Products", desc: "Create & list products", to: "/system/products" },
  {
    title: "Orders (New)",
    desc: "Process new orders",
    to: "/system/orders/new",
  },
  {
    title: "Orders (Processing)",
    desc: "Processing queue",
    to: "/system/orders/processing",
  },
  {
    title: "Pricing",
    desc: "Variant pricing & histories",
    to: "/system/pricing",
  },
  {
    title: "Promotions",
    desc: "Promo codes & campaigns",
    to: "/system/promotions",
  },
  {
    title: "Settings",
    desc: "Default currency",
    to: "/system/settings/currency",
  },
  {
  title: "Payment History",
  desc: "View all payment transactions",
  to: "/system/payments",
},
{
   title: "Birthday Manager",
  desc: "View all birthday",
  to: "/system/birthday",
},
{
   title: "Customer-spending",
  desc: "Customer Spending",
  to: "/system/customer-spending",
}
];

function reload() {
  toast("Dashboard refreshed.", "info");
}
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

.total-revenue {
  font-size: 20px;
  font-weight: 700;
  color: #2e7d32;
}

.money {
  font-weight: 600;
}
</style>
