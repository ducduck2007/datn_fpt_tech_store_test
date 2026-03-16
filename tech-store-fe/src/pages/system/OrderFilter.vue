<template>
  <div class="container-xl">
    <!-- ── Filter Panel ── -->
    <div class="filter-panel">
      <!-- Header -->
      <div class="fp-header">
        <div class="fp-title">
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
          </svg>
          Bộ lọc
          <span v-if="activeFilterCount > 0" class="fp-badge">
            {{ activeFilterCount }} đang áp dụng
          </span>
        </div>
        <button v-if="activeFilterCount > 0" class="fp-clear" @click="clearAll">
          Xoá tất cả
        </button>
      </div>

      <!-- Fields row -->
      <div class="fp-fields">
        <div class="fp-field">
          <span class="fp-field-label">Thời gian</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            value-format="YYYY-MM-DD"
            class="fp-datepicker"
          />
        </div>

        <div class="fp-field">
          <span class="fp-field-label">Khách hàng</span>
          <el-select
            v-model="customerId"
            placeholder="Tất cả khách hàng"
            clearable
            filterable
            class="fp-select"
          >
            <el-option
              v-for="c in customers"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </div>

        <div class="fp-field">
          <span class="fp-field-label">Kênh bán</span>
          <el-select
            v-model="channel"
            placeholder="Tất cả kênh"
            clearable
            class="fp-select"
          >
            <el-option label="Online" value="ONLINE" />
            <el-option label="Offline" value="OFFLINE" />
          </el-select>
        </div>

        <div class="fp-field">
          <span class="fp-field-label">Trạng thái</span>
          <el-select
            v-model="orderStatus"
            placeholder="Tất cả trạng thái"
            clearable
            class="fp-select"
          >
            <el-option
              v-for="s in statusOptions"
              :key="s.value"
              :label="s.label"
              :value="s.value"
            />
          </el-select>
        </div>

        <div class="fp-action">
          <el-button class="fp-btn" @click="handleFilter">
            <svg
              width="13"
              height="13"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.35-4.35" />
            </svg>
            Lọc
          </el-button>
        </div>
      </div>
    </div>

    <!-- ── Result table ── -->
    <div class="mt-4">
      <OrderListBase
        :key="tableKey"
        title="Kết quả lọc đơn hàng"
        :loader="loadOrders"
        endpoint="/api/orders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ordersApi } from "../../api/orders.api";
import { customersApi } from "../../api/customers.api";
import OrderListBase from "./_OrderListBase.vue";

const dateRange = ref([]);
const customerId = ref(null);
const channel = ref(null);
const orderStatus = ref(null);
const customers = ref([]);
const tableKey = ref(0);

const statusOptions = [
  { value: "PENDING", label: "Chờ xử lý" },
  { value: "PROCESSING", label: "Đang xử lý" },
  { value: "SHIPPING", label: "Đang giao" },
  { value: "DELIVERED", label: "Đã giao" },
  { value: "PAID", label: "Đã thanh toán" },
  { value: "CANCELLED", label: "Đã huỷ" },
];

const activeFilterCount = computed(
  () =>
    [
      dateRange.value?.length,
      customerId.value,
      channel.value,
      orderStatus.value,
    ].filter(Boolean).length,
);

const clearAll = () => {
  dateRange.value = [];
  customerId.value = null;
  channel.value = null;
  orderStatus.value = null;
  tableKey.value++;
};

const handleFilter = () => {
  tableKey.value++;
};

const loadCustomers = async () => {
  const res = await customersApi.listAll();
  customers.value = res.data || [];
};

const loadOrders = async () => {
  try {
    const from = dateRange.value?.[0]
      ? dateRange.value[0] + "T00:00:00Z"
      : null;
    const to = dateRange.value?.[1] ? dateRange.value[1] + "T23:59:59Z" : null;
    return await ordersApi.filter(
      customerId.value || null,
      from,
      to,
      channel.value || null,
      orderStatus.value || null,
    );
  } catch (err) {
    console.error(err);
    return { data: [] };
  }
};

onMounted(loadCustomers);
</script>

<style scoped>
.filter-panel {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 10px;
  overflow: hidden;
}

/* Header */
.fp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}
.fp-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}
.fp-badge {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 999px;
}
.fp-clear {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: color 0.15s;
}
.fp-clear:hover {
  color: var(--el-color-danger);
}

/* Fields row */
.fp-fields {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr auto;
  align-items: center;
}
.fp-field {
  padding: 12px 16px;
  border-right: 1px solid var(--el-border-color-light);
}
.fp-field-label {
  display: block;
  font-size: 11px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 6px;
}
.fp-datepicker,
.fp-select {
  width: 100%;
}

:deep(.fp-datepicker .el-input__wrapper),
:deep(.fp-select .el-select__wrapper) {
  box-shadow: none !important;
  border: none !important;
  padding: 0;
  background: transparent;
  font-size: 13px;
}

.fp-action {
  padding: 0 16px;
}
.fp-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--el-color-primary) !important;
  color: #fff !important;
  border: none !important;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  padding: 8px 16px;
  white-space: nowrap;
  height: 36px;
}
.fp-btn:hover {
  opacity: 0.88;
}

/* Responsive */
@media (max-width: 900px) {
  .fp-fields {
    grid-template-columns: 1fr 1fr;
  }
  .fp-field {
    border-bottom: 1px solid var(--el-border-color-light);
  }
  .fp-action {
    padding: 12px 16px;
    grid-column: span 2;
  }
}
</style>
