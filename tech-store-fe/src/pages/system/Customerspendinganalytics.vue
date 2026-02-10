<template>
  <div class="container-xl">
    <el-card shadow="never">
      <!-- Header -->
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Analytics</div>
          <div class="title">Customer Spending Analytics</div>
          <div class="muted">Analyze customer spending patterns and top performers</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="loadAll" :loading="loading">
            <el-icon class="me-1"><Refresh /></el-icon>
            Reload
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- Statistics Overview -->
      <div class="row g-3 mb-4">
        <div class="col-12 col-md-3">
          <el-card shadow="hover" class="stat-card stat-card-primary">
            <div class="stat-icon">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">Total Spent</div>
              <div class="stat-value">{{ formatCurrency(stats.totalSpent) }}</div>
            </div>
          </el-card>
        </div>

        <div class="col-12 col-md-3">
          <el-card shadow="hover" class="stat-card stat-card-success">
            <div class="stat-icon">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">Total Customers</div>
              <div class="stat-value">{{ stats.totalCustomers?.toLocaleString() || 0 }}</div>
            </div>
          </el-card>
        </div>

        <div class="col-12 col-md-3">
          <el-card shadow="hover" class="stat-card stat-card-warning">
            <div class="stat-icon">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">Average Spent</div>
              <div class="stat-value">{{ formatCurrency(stats.averageSpent) }}</div>
            </div>
          </el-card>
        </div>

        <div class="col-12 col-md-3">
          <el-card shadow="hover" class="stat-card stat-card-danger">
            <div class="stat-icon">
              <el-icon :size="32"><Trophy /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">Top Spender</div>
              <div class="stat-value-small">
                {{ topSpenders[0]?.fullName || 'N/A' }}
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- Top Spenders Tab -->
        <el-tab-pane label="Top Spenders" name="topSpenders">
          <div class="mb-3">
            <div class="row g-3 align-items-end">
              <div class="col-12 col-md-3">
                <label class="form-label text-muted small">Limit</label>
                <el-input-number
                  v-model="topSpendersLimit"
                  :min="5"
                  :max="100"
                  :step="5"
                  controls-position="right"
                  style="width: 100%"
                />
              </div>

              <div class="col-12 col-md-3">
                <label class="form-label text-muted small">VIP Tier Filter</label>
                <el-select
                  v-model="topSpendersTier"
                  placeholder="All Tiers"
                  clearable
                  style="width: 100%"
                >
                  <el-option label="BRONZE" value="BRONZE">
                    <el-tag type="info" size="small">BRONZE</el-tag>
                  </el-option>
                  <el-option label="SILVER" value="SILVER">
                    <el-tag type="" size="small">SILVER</el-tag>
                  </el-option>
                  <el-option label="GOLD" value="GOLD">
                    <el-tag type="warning" size="small">GOLD</el-tag>
                  </el-option>
                  <el-option label="PLATINUM" value="PLATINUM">
                    <el-tag type="danger" size="small">PLATINUM</el-tag>
                  </el-option>
                  <el-option label="DIAMOND" value="DIAMOND">
                    <el-tag type="success" size="small">DIAMOND</el-tag>
                  </el-option>
                </el-select>
              </div>

              <div class="col-12 col-md-3">
                <el-button type="primary" @click="loadTopSpenders" :loading="loadingTopSpenders">
                  <el-icon class="me-1"><Search /></el-icon>
                  Load Top Spenders
                </el-button>
              </div>
            </div>
          </div>

          <!-- Top Spenders Table -->
          <el-table
            :data="topSpenders"
            border
            :loading="loadingTopSpenders"
            stripe
            class="top-spenders-table"
          >
            <el-table-column label="Rank" width="80" align="center">
              <template #default="{ $index }">
                <div class="rank-badge" :class="getRankClass($index)">
                  <el-icon v-if="$index === 0"><Trophy /></el-icon>
                  <el-icon v-else-if="$index === 1"><Medal /></el-icon>
                  <el-icon v-else-if="$index === 2"><Medal /></el-icon>
                  <span v-else>{{ $index + 1 }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Customer" min-width="250">
              <template #default="{ row }">
                <div class="d-flex align-items-center gap-3">
                  <el-avatar :size="40" :style="{ backgroundColor: getAvatarColor(row.fullName) }">
                    {{ getInitials(row.fullName) }}
                  </el-avatar>
                  <div>
                    <div class="fw-semibold">{{ row.fullName }}</div>
                    <div class="text-muted small">{{ row.email }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="VIP Tier" width="120" align="center">
              <template #default="{ row }">
                <el-tag
                  v-if="row.raw.vipTier"
                  :type="getTierTagType(row.raw.vipTier)"
                  effect="dark"
                >
                  {{ row.raw.vipTier }}
                </el-tag>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>

            <el-table-column label="Total Spent" width="200" align="right">
              <template #default="{ row }">
                <div class="spending-amount">
                  {{ formatCurrency(row.raw.totalSpent) }}
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Loyalty Points" width="140" align="center">
              <template #default="{ row }">
                <el-tag type="warning" effect="plain" size="large">
                  <el-icon class="me-1"><Coin /></el-icon>
                  {{ row.loyaltyPoints }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="Customer Type" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.customerType === 'VIP' ? 'warning' : 'info'">
                  {{ row.customerType }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Spending Range Tab -->
        <el-tab-pane label="By Spending Range" name="spendingRange">
          <div class="mb-3">
            <div class="row g-3 align-items-end">
              <div class="col-12 col-md-3">
                <label class="form-label text-muted small">Min Spending (VNĐ)</label>
                <el-input-number
                  v-model="spendingMin"
                  :min="0"
                  :step="1000000"
                  controls-position="right"
                  style="width: 100%"
                  :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="(value) => value.replace(/,/g, '')"
                />
              </div>

              <div class="col-12 col-md-3">
                <label class="form-label text-muted small">Max Spending (VNĐ)</label>
                <el-input-number
                  v-model="spendingMax"
                  :min="0"
                  :step="1000000"
                  controls-position="right"
                  style="width: 100%"
                  :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="(value) => value.replace(/,/g, '')"
                />
              </div>

              <div class="col-12 col-md-3">
                <el-button type="primary" @click="loadBySpendingRange" :loading="loadingSpendingRange">
                  <el-icon class="me-1"><Filter /></el-icon>
                  Apply Filter
                </el-button>
              </div>

              <div class="col-12 col-md-3">
                <el-button @click="clearSpendingRange">
                  <el-icon class="me-1"><Close /></el-icon>
                  Clear
                </el-button>
              </div>
            </div>

            <!-- Quick Range Buttons -->
            <div class="mt-3">
              <label class="form-label text-muted small">Quick Ranges:</label>
              <div class="d-flex gap-2 flex-wrap">
                <el-button size="small" @click="setQuickRange(0, 1000000)">
                  Under 1M
                </el-button>
                <el-button size="small" @click="setQuickRange(1000000, 5000000)">
                  1M - 5M
                </el-button>
                <el-button size="small" @click="setQuickRange(5000000, 10000000)">
                  5M - 10M
                </el-button>
                <el-button size="small" @click="setQuickRange(10000000, 50000000)">
                  10M - 50M
                </el-button>
                <el-button size="small" @click="setQuickRange(50000000, 999999999)">
                  Over 50M
                </el-button>
              </div>
            </div>
          </div>

          <!-- Applied Range Display -->
          <div class="mb-3" v-if="appliedSpendingRange.min !== null || appliedSpendingRange.max !== null">
            <el-alert type="info" :closable="false">
              <template #default>
                <div class="d-flex align-items-center gap-2">
                  <strong>Active Range:</strong>
                  <span>
                    {{ formatCurrency(appliedSpendingRange.min || 0) }}
                    -
                    {{ formatCurrency(appliedSpendingRange.max || 999999999) }}
                  </span>
                  <el-tag type="info" size="small">
                    {{ customersBySpending.length }} customers found
                  </el-tag>
                </div>
              </template>
            </el-alert>
          </div>

          <!-- Customers by Spending Table -->
          <el-table
            :data="pagedSpending"
            border
            :loading="loadingSpendingRange"
            stripe
          >
            <el-table-column prop="id" label="ID" width="80" />

            <el-table-column label="Customer" min-width="250">
              <template #default="{ row }">
                <div class="d-flex align-items-center gap-3">
                  <el-avatar :size="36" :style="{ backgroundColor: getAvatarColor(row.fullName) }">
                    {{ getInitials(row.fullName) }}
                  </el-avatar>
                  <div>
                    <div class="fw-semibold">{{ row.fullName }}</div>
                    <div class="text-muted small">{{ row.email }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Phone" width="140">
              <template #default="{ row }">
                <span class="text-muted">{{ row.phone || '—' }}</span>
              </template>
            </el-table-column>

            <el-table-column label="VIP Tier" width="120" align="center">
              <template #default="{ row }">
                <el-tag
                  v-if="row.raw.vipTier"
                  :type="getTierTagType(row.raw.vipTier)"
                  effect="dark"
                  size="small"
                >
                  {{ row.raw.vipTier }}
                </el-tag>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>

            <el-table-column label="Total Spent" width="180" align="right" sortable>
              <template #default="{ row }">
                <div class="spending-amount">
                  {{ formatCurrency(row.raw.totalSpent) }}
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Points" width="120" align="center" sortable>
              <template #default="{ row }">
                <el-tag type="warning" effect="plain">
                  <el-icon class="me-1"><Coin /></el-icon>
                  {{ row.loyaltyPoints }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="Type" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.customerType === 'VIP' ? 'warning' : 'info'" size="small">
                  {{ row.customerType }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="d-flex justify-content-between align-items-center mt-3">
            <div class="text-muted small">
              Showing {{ (spendingPage - 1) * pageSize + 1 }} 
              to {{ Math.min(spendingPage * pageSize, customersBySpending.length) }} 
              of {{ customersBySpending.length }} results
            </div>
            <el-pagination
              background
              layout="prev, pager, next, jumper"
              :page-size="pageSize"
              :total="customersBySpending.length"
              :current-page="spendingPage"
              @current-change="(v) => (spendingPage = v)"
            />
          </div>
        </el-tab-pane>

        <!-- Spending Statistics Tab -->
        <el-tab-pane label="Statistics" name="statistics">
          <!-- Spending Ranges Chart -->
          <div class="row g-3 mb-4">
            <div class="col-12">
              <el-card shadow="hover">
                <template #header>
                  <div class="fw-bold">Distribution by Spending Range</div>
                </template>
                <div class="spending-ranges">
                  <div
                    v-for="(count, range) in stats.spendingRanges"
                    :key="range"
                    class="spending-range-item"
                  >
                    <div class="range-label">{{ formatRangeLabel(range) }}</div>
                    <div class="range-progress">
                      <el-progress
                        :percentage="getPercentage(count, stats.totalCustomers)"
                        :color="getProgressColor(range)"
                      />
                    </div>
                    <div class="range-count">
                      <el-tag type="info" size="small">{{ count }} customers</el-tag>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>

          <!-- By VIP Tier Statistics -->
          <div class="row g-3">
            <div
              v-for="(tierData, tier) in stats.byTier"
              :key="tier"
              class="col-12 col-md-6 col-lg-4"
            >
              <el-card shadow="hover" class="tier-stat-card">
                <template #header>
                  <div class="d-flex align-items-center justify-content-between">
                    <el-tag :type="getTierTagType(tier)" effect="dark">
                      {{ tier }}
                    </el-tag>
                    <span class="text-muted small">{{ tierData.customerCount }} customers</span>
                  </div>
                </template>
                <div class="tier-stats">
                  <div class="tier-stat-item">
                    <div class="stat-label-small">Total Spent</div>
                    <div class="stat-value-medium">
                      {{ formatCurrency(tierData.totalSpent) }}
                    </div>
                  </div>
                  <el-divider />
                  <div class="tier-stat-item">
                    <div class="stat-label-small">Average Spent</div>
                    <div class="stat-value-medium">
                      {{ formatCurrency(tierData.averageSpent) }}
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  Refresh,
  Search,
  Filter,
  Close,
  Money,
  User,
  TrendCharts,
  Trophy,
  Medal,
  Coin,
  Calendar,
} from "@element-plus/icons-vue";
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";

// ===========================
// STATE
// ===========================
const loading = ref(false);
const loadingTopSpenders = ref(false);
const loadingSpendingRange = ref(false);

const activeTab = ref("topSpenders");

// Top Spenders
const topSpenders = ref([]);
const topSpendersLimit = ref(10);
const topSpendersTier = ref("");

// Spending Range
const customersBySpending = ref([]);
const spendingMin = ref(null);
const spendingMax = ref(null);
const appliedSpendingRange = reactive({
  min: null,
  max: null,
});
const spendingPage = ref(1);
const pageSize = 10;

// Statistics
const stats = ref({
  totalSpent: 0,
  totalCustomers: 0,
  averageSpent: 0,
  byTier: {},
  spendingRanges: {},
});

// ===========================
// COMPUTED
// ===========================
const pagedSpending = computed(() => {
  const start = (spendingPage.value - 1) * pageSize;
  return customersBySpending.value.slice(start, start + pageSize);
});

// ===========================
// HELPER FUNCTIONS
// ===========================
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
  return (list || []).map((c) => ({
    id: c?.id ?? c?.customerId,
    fullName: c?.fullName ?? c?.name ?? "",
    email: c?.email ?? "",
    phone: c?.phone ?? "",
    customerType: (c?.customerType ?? "REGULAR").toString().toUpperCase(),
    loyaltyPoints: c?.loyaltyPoints ?? 0,
    raw: c,
  }));
}

function formatCurrency(value) {
  if (!value) return "0 VNĐ";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
}

function getTierTagType(tier) {
  const types = {
    BRONZE: "info",
    SILVER: "",
    GOLD: "warning",
    PLATINUM: "danger",
    DIAMOND: "success",
  };
  return types[tier] || "info";
}

function getInitials(name) {
  if (!name) return "?";
  const parts = name.trim().split(" ");
  if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function getAvatarColor(name) {
  const colors = [
    "#409EFF",
    "#67C23A",
    "#E6A23C",
    "#F56C6C",
    "#909399",
    "#00d2d3",
    "#e91e63",
    "#9c27b0",
    "#673ab7",
    "#3f51b5",
  ];
  const hash = name.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0);
  return colors[hash % colors.length];
}

function getRankClass(index) {
  if (index === 0) return "rank-gold";
  if (index === 1) return "rank-silver";
  if (index === 2) return "rank-bronze";
  return "";
}

function formatRangeLabel(range) {
  const labels = {
    under1M: "Under 1M VNĐ",
    "1M-5M": "1M - 5M VNĐ",
    "5M-10M": "5M - 10M VNĐ",
    "10M-50M": "10M - 50M VNĐ",
    over50M: "Over 50M VNĐ",
  };
  return labels[range] || range;
}

function getPercentage(count, total) {
  if (!total) return 0;
  return Math.round((count / total) * 100);
}

function getProgressColor(range) {
  const colors = {
    under1M: "#909399",
    "1M-5M": "#409EFF",
    "5M-10M": "#67C23A",
    "10M-50M": "#E6A23C",
    over50M: "#F56C6C",
  };
  return colors[range] || "#409EFF";
}

// ===========================
// API FUNCTIONS
// ===========================
async function loadTopSpenders() {
  loadingTopSpenders.value = true;
  try {
    let res;
    if (topSpendersTier.value) {
      res = await customersApi.listTopSpendersByVipTier(
        topSpendersTier.value,
        topSpendersLimit.value
      );
    } else {
      res = await customersApi.listTopSpenders(topSpendersLimit.value);
    }
    topSpenders.value = normalize(extractList(res?.data));
  } catch (error) {
    console.error("Load top spenders error:", error);
    toast("Failed to load top spenders.", "error");
  } finally {
    loadingTopSpenders.value = false;
  }
}

async function loadBySpendingRange() {
  if (spendingMin.value === null && spendingMax.value === null) {
    toast("Please enter at least one spending value", "warning");
    return;
  }

  if (
    spendingMin.value !== null &&
    spendingMax.value !== null &&
    spendingMin.value > spendingMax.value
  ) {
    toast("Min spending cannot be greater than max spending", "error");
    return;
  }

  loadingSpendingRange.value = true;
  try {
    const min = spendingMin.value || 0;
    const max = spendingMax.value || 999999999;

    const res = await customersApi.listBySpendingRange(min, max);
    customersBySpending.value = normalize(extractList(res?.data));

    appliedSpendingRange.min = min;
    appliedSpendingRange.max = max;
    spendingPage.value = 1;

    toast(
      `Found ${customersBySpending.value.length} customers in range`,
      "success"
    );
  } catch (error) {
    console.error("Load by spending range error:", error);
    toast("Failed to load customers by spending range.", "error");
  } finally {
    loadingSpendingRange.value = false;
  }
}

async function loadStatistics() {
  loading.value = true;
  try {
    const res = await customersApi.getSpendingStatistics();
    stats.value = res?.data || {};
  } catch (error) {
    console.error("Load statistics error:", error);
    toast("Failed to load statistics.", "error");
  } finally {
    loading.value = false;
  }
}

function clearSpendingRange() {
  spendingMin.value = null;
  spendingMax.value = null;
  appliedSpendingRange.min = null;
  appliedSpendingRange.max = null;
  customersBySpending.value = [];
}

function setQuickRange(min, max) {
  spendingMin.value = min;
  spendingMax.value = max;
  loadBySpendingRange();
}

async function loadAll() {
  await Promise.all([loadTopSpenders(), loadStatistics()]);
}

function handleTabChange(tab) {
  if (tab === "topSpenders" && topSpenders.value.length === 0) {
    loadTopSpenders();
  } else if (tab === "statistics" && Object.keys(stats.value.byTier || {}).length === 0) {
    loadStatistics();
  }
}

// ===========================
// LIFECYCLE
// ===========================
onMounted(() => {
  loadAll();
});
</script>

<style scoped>
.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #409eff;
}

.title {
  font-weight: 900;
  font-size: 28px;
  margin-bottom: 4px;
  color: #303133;
}

.muted {
  color: rgba(15, 23, 42, 0.62);
  font-size: 14px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 4px;
  display: block;
}

/* Stat Cards */
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card-success {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-card-warning {
  background: linear-gradient(135deg, #fad0c4 0%, #ffd1ff 100%);
  color: #303133;
}

.stat-card-danger {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #303133;
}

.stat-icon {
  flex-shrink: 0;
  opacity: 0.8;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 4px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 900;
  line-height: 1;
}

.stat-value-small {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.2;
}

/* Rank Badges */
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-weight: 900;
  font-size: 16px;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #8b6914;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #5a5a5a;
  box-shadow: 0 4px 12px rgba(192, 192, 192, 0.4);
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #e6a23c 100%);
  color: #5c3d1f;
  box-shadow: 0 4px 12px rgba(205, 127, 50, 0.4);
}

/* Spending Amount */
.spending-amount {
  font-size: 16px;
  font-weight: 700;
  color: #67c23a;
}

/* Spending Ranges */
.spending-ranges {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.spending-range-item {
  display: grid;
  grid-template-columns: 150px 1fr 150px;
  gap: 16px;
  align-items: center;
}

.range-label {
  font-weight: 600;
  color: #606266;
}

.range-progress {
  flex: 1;
}

.range-count {
  text-align: right;
}

/* Tier Stat Cards */
.tier-stat-card {
  height: 100%;
}

.tier-stats {
  display: flex;
  flex-direction: column;
}

.tier-stat-item {
  text-align: center;
}

.stat-label-small {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value-medium {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

/* Top Spenders Table */
.top-spenders-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* Responsive */
@media (max-width: 768px) {
  .spending-range-item {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .range-count {
    text-align: left;
  }

  .stat-value {
    font-size: 20px;
  }

  .title {
    font-size: 22px;
  }
}

:deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}
</style>