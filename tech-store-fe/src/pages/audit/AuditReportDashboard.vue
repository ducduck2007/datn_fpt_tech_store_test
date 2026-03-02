<template>
  <div class="audit-report">
    <!-- ===== SUMMARY ===== -->
    <el-card class="mb-20">
        <h3>Summery Report</h3>
      <el-row :gutter="20" class="mb-20">
        <el-col :span="6">
          <el-card>Total Logs: {{ summary.totalLogs }}</el-card>
        </el-col>

        <el-col :span="6">
          <el-card>Active Users: {{ summary.totalActiveUsers }}</el-card>
        </el-col>

        <el-col :span="6">
          <el-card>Errors: {{ summary.totalErrors }}</el-card>
        </el-col>

        <el-col :span="6">
          <el-card>Today Logs: {{ summary.todayLogs }}</el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb-20">
        <el-col :span="12">
          <el-card>Top Module: {{ summary.topModule }}</el-card>
        </el-col>

        <el-col :span="12">
          <el-card>Top User: {{ summary.topUserId }}</el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- ===== MODULE REPORT ===== -->
    <el-card class="mb-20">
      <h3>Report By Module</h3>
      <el-table :data="moduleReport" border>
        <el-table-column prop="module" label="Module" />
        <el-table-column prop="total" label="Total Logs" />
      </el-table>
    </el-card>

    <!-- ===== USER REPORT ===== -->
    <el-card>
      <h3>Report By User</h3>
      <el-table :data="userReport" border>
        <el-table-column prop="userId" label="User ID" />
        <el-table-column prop="totalActions" label="Total Actions" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { auditApi } from "../../api/audit.api";

const moduleReport = ref([]);
const userReport = ref([]);
const summary = ref({});

const loadReports = async () => {
  try {
    const [moduleRes, userRes, summaryRes] = await Promise.all([
      auditApi.reportByModule(),
      auditApi.reportByUser(),
      auditApi.reportSummary(),
    ]);

    moduleReport.value = moduleRes.data;
    userReport.value = userRes.data;
    summary.value = summaryRes.data;
  } catch (err) {
    console.error("Load report error", err);
  }
};

onMounted(loadReports);
</script>

<style scoped>
.audit-report {
  padding: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
