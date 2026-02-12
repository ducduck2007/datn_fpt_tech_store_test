<template>
  <div class="container-xl">
    <el-card shadow="never">
      <template #header>
        <div class="fw-bold">Lọc đơn hàng</div>
      </template>

      <!-- FILTER -->
      <el-row :gutter="16" class="mb-3">
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-col>

        <el-col :span="8">
          <el-select
            v-model="customerId"
            placeholder="Chọn khách hàng"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="c in customers"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-col>

        <el-col :span="4">
          <el-button type="primary" @click="handleFilter"> Lọc </el-button>
        </el-col>
      </el-row>

      <!-- RESULT -->
      <OrderListBase
        :key="tableKey"
        title="Kết quả lọc đơn hàng"
        :loader="loadOrders"
        endpoint="/api/orders"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ordersApi } from "../../api/orders.api";
import { customersApi } from "../../api/customers.api";
import OrderListBase from "./_OrderListBase.vue";

const dateRange = ref([]);
const customerId = ref(null);

const customers = ref([]);
const orders = ref([]);
const loading = ref(false);

const tableKey = ref(0);

const handleFilter = () => {
  tableKey.value++;
};

const loadCustomers = async () => {
  const res = await customersApi.listAll();
  customers.value = res.data || [];
};

const loadOrders = async () => {
  console.log("=== LOAD ORDERS START ===");

  try {
    let res;

    if (dateRange.value?.length === 2 && customerId.value) {
      const from = dateRange.value[0] + "T00:00:00Z";
      const to = dateRange.value[1] + "T23:59:59Z";
      res = await ordersApi.filter(customerId.value, from, to);
    } else if (dateRange.value?.length === 2) {
      const from = dateRange.value[0] + "T00:00:00Z";
      const to = dateRange.value[1] + "T23:59:59Z";
      res = await ordersApi.getByDate(from, to);
    } else if (customerId.value) {
      res = await ordersApi.getByCustomer(customerId.value);
    } else {
      return { data: [] };
    }

    console.log("RETURNING AXIOS RESPONSE:", res);
    return res; // ⚠ phải return nguyên Axios response
  } catch (err) {
    console.error(err);
    return { data: [] };
  }
};

onMounted(() => {
  loadCustomers();
});
</script>
