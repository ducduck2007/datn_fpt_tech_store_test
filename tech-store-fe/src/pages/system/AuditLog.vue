<template>
  <div class="audit-page">

    <!-- ===== HEADER ===== -->
    <div class="page-header">
      <div class="title">Audit Logs</div>

      <div class="actions">
        <el-button type="success" @click="exportCsv">
          Export CSV
        </el-button>
      </div>
    </div>

    <!-- ===== FILTER PANEL ===== -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form">

        <el-input
          v-model="filter.keyword"
          placeholder="Keyword"
          style="width: 180px"
        />

        <el-input
          v-model.number="filter.userId"
          placeholder="User ID"
          style="width: 120px"
        />

        <el-select
          v-model="filter.module"
          placeholder="Module"
          clearable
          style="width: 160px"
        >
          <el-option label="USER" value="USER" />
          <el-option label="CUSTOMER" value="CUSTOMER" />
          <el-option label="ORDER" value="ORDER" />
          <el-option label="PRODUCT" value="PRODUCT" />
        </el-select>

        <el-select
          v-model="filter.action"
          placeholder="Action"
          clearable
          style="width: 160px"
        >
          <el-option label="CREATE" value="CREATE" />
          <el-option label="UPDATE" value="UPDATE" />
          <el-option label="DELETE" value="DELETE" />
          <el-option label="CHANGE_ROLE" value="CHANGE_ROLE" />
        </el-select>

        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="to"
          start-placeholder="From"
          end-placeholder="To"
          value-format="YYYY-MM-DD"
        />

        <el-button type="primary" @click="searchLogs">
          Search
        </el-button>

        <el-button @click="resetFilter">
          Reset
        </el-button>

      </el-form>
    </el-card>

    <!-- ===== TABLE ===== -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="logs"
        border
        style="width: 100%"
        @sort-change="handleSort"
      >

        <el-table-column prop="id" label="ID" width="90" sortable="custom" />

        <el-table-column prop="userId" label="User" width="90" />

        <el-table-column prop="module" label="Module" width="140" />

        <el-table-column prop="action" label="Action" width="150" />

        <el-table-column prop="targetType" label="Target Type" width="140" />

        <el-table-column prop="targetId" label="Target ID" width="110" />

        <el-table-column prop="ipAddress" label="IP Address" width="160" />

        <el-table-column prop="detailsJson" label="Details" min-width="250" />

        <el-table-column
          prop="createdAt"
          label="Created At"
          width="200"
          sortable="custom"
        />

      </el-table>

      <!-- ===== PAGINATION ===== -->
      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="page"
          :page-size="size"
          :page-sizes="[5,10,20,50]"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>

    </el-card>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import http from "../../api/http";

/* =========================
   STATE
========================= */

const logs = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(5);
const loading = ref(false);

const sortBy = ref("createdAt");
const sortDir = ref("DESC");

const dateRange = ref([]);

const filter = reactive({
  userId: null,
  module: null,
  action: null,
  keyword: "",
  startDate: null,
  endDate: null
});

/* =========================
   API CALL
========================= */

async function fetchLogs() {
  loading.value = true;

  if (dateRange.value?.length === 2) {
    filter.startDate = dateRange.value[0];
    filter.endDate = dateRange.value[1];
  } else {
    filter.startDate = null;
    filter.endDate = null;
  }

  try {
    const res = await http.post(
      "/api/auth/audit-log/search",
      filter,
      {
        params: {
          page: page.value - 1,
          size: size.value,
          sortBy: sortBy.value,
          sortDir: sortDir.value
        }
      }
    );

    logs.value = res.data.content;
    total.value = res.data.totalElements;

  } finally {
    loading.value = false;
  }
}

/* =========================
   EVENTS
========================= */

function searchLogs() {
  page.value = 1;
  fetchLogs();
}

function resetFilter() {
  filter.userId = null;
  filter.module = null;
  filter.action = null;
  filter.keyword = "";
  dateRange.value = [];
  searchLogs();
}

function handlePageChange(p) {
  page.value = p;
  fetchLogs();
}

function handleSizeChange(s) {
  size.value = s;
  page.value = 1;
  fetchLogs();
}

function handleSort({ prop, order }) {
  if (!order) return;

  sortBy.value = prop;
  sortDir.value = order === "ascending" ? "ASC" : "DESC";
  fetchLogs();
}

/* =========================
   EXPORT CSV
========================= */

function exportCsv() {
  window.open("/api/auth/audit-log/export", "_blank");
}

/* =========================
   INIT
========================= */

onMounted(fetchLogs);
</script>

<style scoped>
.audit-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 20px;
  font-weight: 800;
}

.filter-card {
  border-radius: 10px;
}

.filter-form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>