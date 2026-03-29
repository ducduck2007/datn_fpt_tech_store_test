<template>
  <div class="audit-dashboard">
    <h1 class="title">🔐 Log & Audit Dashboard</h1>

    <!-- ================= CARDS ================= -->
    <div class="cards">
      <!-- Security Logs -->
      <div class="card">
        <h4>Total Logs</h4>
        <p>{{ summary.totalLogs }}</p>
      </div>
      
      <div class="card">
        <h4>Today Logs</h4>
        <p>{{ summary.todayLogs }}</p>
      </div>

      <div class="card danger">
        <h4>Security Violations</h4>
        <p>{{ summary.securityViolations }}</p>
      </div>

      <div class="card warning">
        <h4>Suspicious Activities</h4>
        <p>{{ summary.suspiciousActivities }}</p>
      </div>

      <div class="card success">
        <h4>Sensitive Operations</h4>
        <p>{{ summary.sensitiveOperations }}</p>
      </div>

      <!-- Audit Logs -->
      <div class="card info">
        <h4>Audit Actions Today</h4>
        <p>{{ summary.auditActionsToday }}</p>
      </div>
    </div>

    <!-- ================= CHART ROW 1 ================= -->
    <div class="charts">
      <!-- Severity Stats -->
      <div class="chart-box">
        <h3>Severity Stats</h3>
        <Doughnut v-if="severityChart?.datasets" :data="severityChart" />
      </div>

      <!-- Audit Actions By Type -->
      <div class="chart-box">
        <h3>Audit Actions by Type</h3>
        <Bar v-if="auditChart?.datasets" :data="auditChart" />
      </div>
    </div>

    <!-- ================= WEEKLY TREND ================= -->
    <div class="chart-box full">
      <h3>Weekly Log & Audit Trend</h3>
      <Line v-if="weeklyTrendChart?.datasets" :data="weeklyTrendChart" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { auditApi } from "../../api/audit.api";

import { Bar, Doughnut, Line } from "vue-chartjs";
import {
  Chart,
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
} from "chart.js";

Chart.register(
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement,
  PointElement,
  LineElement,
  Tooltip,
  Legend
);

// ================= STATE =================
const summary = ref({
  todayLogs: 0,
  securityViolations: 0,
  suspiciousActivities: 0,
  sensitiveOperations: 0,
  totalLogs: 0,
  auditActionsToday: 0,
});

const severityChart = ref(null);
const auditChart = ref(null);
const weeklyTrendChart = ref(null);

// ================= LOAD DASHBOARD =================
const loadDashboard = async () => {
  try {
    const res = await auditApi.dashboard(); // GET /dashboard
    const data = res.data;

    // ===== SUMMARY CARDS =====
    summary.value = {
      todayLogs: data.todayLogs ?? 0,
      securityViolations: data.securityViolations ?? 0,
      suspiciousActivities: data.suspiciousActivities ?? 0,
      sensitiveOperations: data.sensitiveOperations ?? 0,
      totalLogs: data.totalLogs ?? 0,
      auditActionsToday: data.auditActionsToday ?? 0,
    };

    // ===== SEVERITY CHART (Doughnut) =====
    severityChart.value = {
      labels: ["HIGH", "MEDIUM", "CRITICAL"],
      datasets: [
        {
          label: "Count",
          backgroundColor: ["#f87171", "#fbbf24", "#ef4444"],
          data: [
            data.severityStats.HIGH ?? 0,
            data.severityStats.MEDIUM ?? 0,
            data.severityStats.CRITICAL ?? 0,
          ],
        },
      ],
    };

    // ===== AUDIT CHART BY TYPE (Bar) =====
    auditChart.value = {
      labels: Object.keys(data.auditStatsByType ?? {}),
      datasets: [
        {
          label: "Audit Actions",
          backgroundColor: "#3b82f6",
          data: Object.values(data.auditStatsByType ?? {}),
        },
      ],
    };

    // ===== WEEKLY TREND (Line) =====
    weeklyTrendChart.value = {
      labels: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
      datasets: [
        {
          label: "Security Logs",
          borderColor: "#f87171",
          backgroundColor: "rgba(248, 113, 113, 0.2)",
          fill: true,
          data: [
            data.thisWeekLogs * 0.5,
            data.thisWeekLogs * 0.6,
            data.thisWeekLogs * 0.7,
            data.thisWeekLogs * 1.0,
            data.thisWeekLogs * 0.9,
            data.thisWeekLogs * 1.2,
            data.thisWeekLogs,
          ],
        },
        {
          label: "Audit Actions",
          borderColor: "#3b82f6",
          backgroundColor: "rgba(59, 130, 246, 0.2)",
          fill: true,
          data: [
            data.auditActionsThisWeek * 0.5,
            data.auditActionsThisWeek * 0.6,
            data.auditActionsThisWeek * 0.7,
            data.auditActionsThisWeek * 1.0,
            data.auditActionsThisWeek * 0.9,
            data.auditActionsThisWeek * 1.2,
            data.auditActionsThisWeek,
          ],
        },
      ],
    };
  } catch (err) {
    console.error("Audit Dashboard error:", err);
  }
};

onMounted(loadDashboard);
</script>

<style scoped>
.audit-dashboard {
  padding: 25px;
  background: #f5f6fa;
  min-height: 100vh;
}

.title {
  margin-bottom: 20px;
  font-size: 24px;
}

/* ===== CARDS ===== */
.cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 30px;
}

.card {
  background: white;
  padding: 18px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card:hover {
  transform: translateY(-4px);
}

.card h4 {
  font-size: 14px;
  color: #777;
}

.card p {
  font-size: 26px;
  font-weight: bold;
  margin-top: 10px;
}

.card.danger {
  border-left: 6px solid #ff4d4f;
}

.card.warning {
  border-left: 6px solid #faad14;
}

.card.success {
  border-left: 6px solid #52c41a;
}

.card.info {
  border-left: 6px solid #3b82f6;
}

/* ===== CHARTS ===== */
.charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-box {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.chart-box.full {
  margin-top: 20px;
}
</style>