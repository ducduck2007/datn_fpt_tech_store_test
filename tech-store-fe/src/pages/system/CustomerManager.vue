<template>
  <div class="container-xl">
    <el-card shadow="never">
      <!-- Header -->
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Customers & Loyalty</div>
          <div class="muted">Base: /api/auth/customers</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="load" :loading="loading">
            <el-icon class="me-1"><Refresh /></el-icon>
            Reload
          </el-button>
          <el-button type="primary" @click="openCreate">
            <el-icon class="me-1"><Plus /></el-icon>
            Add customer
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- ✅ FILTERS ROW 1 -->
      <div class="row g-3 mb-3">
        <!-- Search -->
        <div class="col-12 col-md-3">
          <label class="form-label text-muted small">Search</label>
          <el-input
            v-model="q"
            placeholder="Name / Email / Phone"
            clearable
            :prefix-icon="Search"
          />
        </div>

        <!-- Customer Type Filter -->
        <div class="col-12 col-md-2">
          <label class="form-label text-muted small">Customer Type</label>
          <el-select
            v-model="typeFilter"
            placeholder="All Types"
            clearable
            @change="handleFilterChange"
          >
            <el-option label="REGULAR" value="REGULAR" />
            <el-option label="VIP" value="VIP" />
          </el-select>
        </div>

        <!-- VIP Tier Filter -->
        <div class="col-12 col-md-2">
          <label class="form-label text-muted small">VIP Tier</label>
          <el-select
            v-model="vipTierFilter"
            placeholder="All Tiers"
            clearable
            @change="handleFilterChange"
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

        <!-- Activity Filter -->
        <div class="col-12 col-md-2">
          <label class="form-label text-muted small">Activity</label>
          <el-select
            v-model="activityFilter"
            placeholder="All Activity"
            clearable
            @change="handleFilterChange"
          >
            <el-option label="Active (30 days)" value="ACTIVE_30">
              <span class="d-flex align-items-center gap-2">
                <el-icon><Calendar /></el-icon>
                <span>Active (30 days)</span>
              </span>
            </el-option>
          </el-select>
        </div>

        <!-- Quick Actions -->
        <div class="col-12 col-md-3 d-flex align-items-end">
          <el-button @click="clearAllFilters" :disabled="!hasActiveFilters">
            <el-icon class="me-1"><Close /></el-icon>
            Clear Filters
          </el-button>
        </div>
      </div>

      <!-- ✅ FILTERS ROW 2: Points Range -->
      <div class="row g-3 mb-3">
        <div class="col-12 col-md-3">
          <label class="form-label text-muted small">Min Points</label>
          <el-input-number
            v-model="pointsMin"
            :min="0"
            :max="999999"
            placeholder="Min"
            controls-position="right"
            style="width: 100%"
          />
        </div>
        <div class="col-12 col-md-3">
          <label class="form-label text-muted small">Max Points</label>
          <el-input-number
            v-model="pointsMax"
            :min="0"
            :max="999999"
            placeholder="Max"
            controls-position="right"
            style="width: 100%"
          />
        </div>
        <div class="col-12 col-md-3 d-flex align-items-end">
          <el-button 
            type="primary" 
            @click="applyPointsFilter" 
            :disabled="!canApplyPointsFilter"
          >
            <el-icon class="me-1"><Filter /></el-icon>
            Apply Points Filter
          </el-button>
        </div>
        <div class="col-12 col-md-3 d-flex align-items-end">
          <el-button 
            @click="clearPointsFilter" 
            :disabled="!pointsMin && !pointsMax"
          >
            <el-icon class="me-1"><Close /></el-icon>
            Clear Points
          </el-button>
        </div>
      </div>

      <!-- ✅ ACTIVE FILTERS DISPLAY -->
      <div class="mb-3" v-if="hasActiveFilters">
        <div class="d-flex align-items-center gap-2 flex-wrap">
          <span class="text-muted small">Active filters:</span>
          
          <el-tag 
            v-if="typeFilter" 
            type="info" 
            closable 
            @close="typeFilter = ''; handleFilterChange()"
          >
            Type: {{ typeFilter }}
          </el-tag>
          
          <el-tag 
            v-if="vipTierFilter" 
            :type="getTierTagType(vipTierFilter)" 
            closable 
            @close="vipTierFilter = ''; handleFilterChange()"
          >
            VIP: {{ vipTierFilter }}
          </el-tag>
          
          <el-tag 
            v-if="activityFilter === 'ACTIVE_30'" 
            type="success" 
            closable 
            @close="activityFilter = ''; handleFilterChange()"
          >
            <el-icon class="me-1"><Calendar /></el-icon>
            Active (30 days)
          </el-tag>
          
          <el-tag 
            v-if="isPointsFilterApplied" 
            type="warning" 
            closable 
            @close="clearPointsFilter"
          >
            <el-icon class="me-1"><Coin /></el-icon>
            Points: {{ appliedPointsMin }} - {{ appliedPointsMax === 999999 ? '∞' : appliedPointsMax }}
          </el-tag>
        </div>
      </div>

      <el-divider />

      <!-- ✅ STATISTICS -->
      <div class="mb-3">
        <el-alert
          :title="getStatisticsTitle"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="d-flex gap-3 flex-wrap">
              <span><strong>{{ rows.length }}</strong> customers found</span>
              <span class="text-muted">|</span>
              <span class="text-muted">Total Points: <strong>{{ totalPoints }}</strong></span>
              <span class="text-muted">|</span>
              <span class="text-muted">Avg Points: <strong>{{ avgPoints }}</strong></span>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- ✅ TABLE -->
      <el-table :data="paged" border :loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" fixed />
        
        <el-table-column prop="fullName" label="Full Name" min-width="200" fixed>
          <template #default="{ row }">
            <div class="d-flex align-items-center gap-2">
              <el-avatar :size="32" :style="{ backgroundColor: getAvatarColor(row.fullName) }">
                {{ getInitials(row.fullName) }}
              </el-avatar>
              <span class="fw-semibold">{{ row.fullName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="email" label="Email" min-width="220">
          <template #default="{ row }">
            <span class="text-muted">{{ row.email }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="phone" label="Phone" width="140">
          <template #default="{ row }">
            <span class="text-muted">{{ row.phone || '—' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="customerType" label="Type" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.customerType === 'VIP' ? 'warning' : 'info'"
              effect="light"
              size="small"
            >
              {{ row.customerType || "REGULAR" }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="VIP Tier" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              v-if="row.raw.vipTier" 
              :type="getTierTagType(row.raw.vipTier)" 
              size="small"
              effect="dark"
            >
              {{ row.raw.vipTier }}
            </el-tag>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="loyaltyPoints" label="Points" width="120" sortable align="center">
          <template #default="{ row }">
            <div class="d-flex flex-column align-items-center">
              <span class="fw-bold text-primary fs-6">{{ row.loyaltyPoints }}</span>
              <el-progress 
                v-if="row.raw.vipTier"
                :percentage="getPointsProgress(row.raw)" 
                :stroke-width="4"
                :show-text="false"
                :color="getProgressColor(row.raw.vipTier)"
              />
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="birthDate" label="Birth Date" width="140" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ row.birthDate || '—' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="notes" label="Notes" min-width="180">
          <template #default="{ row }">
            <el-tooltip 
              v-if="row.notes && row.notes.length > 30"
              :content="row.notes"
              placement="top"
            >
              <span class="text-muted small">{{ row.notes.substring(0, 30) }}...</span>
            </el-tooltip>
            <span v-else class="text-muted small">{{ row.notes || '—' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="Actions" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="openEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" type="primary" @click="viewDetails(row)">
                <el-icon><View /></el-icon>
              </el-button>
              <el-button size="small" type="danger" @click="remove(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- ✅ PAGINATION -->
      <div class="d-flex justify-content-between align-items-center mt-3">
        <div class="text-muted small">
          Showing {{ (page - 1) * pageSize + 1 }} to {{ Math.min(page * pageSize, filtered.length) }} of {{ filtered.length }} results
        </div>
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :page-size="pageSize"
          :total="filtered.length"
          :current-page="page"
          @current-change="(v) => (page = v)"
        />
      </div>
    </el-card>

    <!-- ✅ CREATE/EDIT DIALOG -->
    <el-dialog
      v-model="dlg.open"
      :title="dlg.mode === 'create' ? '➕ Add New Customer' : '✏️ Update Customer'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-alert
        v-if="dlg.alert"
        :title="dlg.alert"
        type="error"
        show-icon
        class="mb-3"
        closable
      />

      <el-form :model="dlg.form" label-position="top" class="row g-3">
        <div class="col-12 col-md-6">
          <el-form-item label="Full Name" required>
            <el-input 
              v-model="dlg.form.fullName" 
              placeholder="Enter full name"
              :prefix-icon="User"
            />
          </el-form-item>
        </div>
        
        <div class="col-12 col-md-6">
          <el-form-item label="Customer Type">
            <el-select v-model="dlg.form.customerType" style="width: 100%">
              <el-option label="REGULAR" value="REGULAR">
                <el-tag type="info" size="small">REGULAR</el-tag>
              </el-option>
              <el-option label="VIP" value="VIP">
                <el-tag type="warning" size="small">VIP</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="Email" required>
            <el-input 
              v-model="dlg.form.email" 
              type="email"
              placeholder="email@example.com"
              :prefix-icon="Message"
            />
          </el-form-item>
        </div>
        
        <div class="col-12 col-md-6">
          <el-form-item label="Phone" required>
            <el-input 
              v-model="dlg.form.phone" 
              placeholder="0912345678"
              :prefix-icon="Phone"
            />
          </el-form-item>
        </div>

        <div class="col-12 col-md-6">
          <el-form-item label="Birth Date">
            <el-date-picker
              v-model="dlg.form.birthDate"
              type="date"
              placeholder="Select date"
              value-format="YYYY-MM-DD"
              style="width: 100%"
              :prefix-icon="Calendar"
            />
          </el-form-item>
        </div>
        
        <div class="col-12 col-md-6">
          <el-form-item label="Address">
            <el-input 
              v-model="dlg.form.address" 
              placeholder="Enter address"
              :prefix-icon="Location"
            />
          </el-form-item>
        </div>

        <div class="col-12">
          <el-form-item label="Notes">
            <el-input 
              v-model="dlg.form.notes" 
              type="textarea" 
              :rows="3"
              placeholder="Additional notes about the customer..."
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="d-flex justify-content-end gap-2">
          <el-button @click="dlg.open = false">
            <el-icon class="me-1"><Close /></el-icon>
            Cancel
          </el-button>
          <el-button type="primary" :loading="dlg.loading" @click="save">
            <el-icon class="me-1"><Check /></el-icon>
            {{ dlg.mode === "create" ? "Create" : "Update" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ✅ DETAILS DIALOG -->
    <el-dialog
      v-model="detailsDialog.open"
      title="👤 Customer Details"
      width="900px"
    >
      <div v-if="detailsDialog.customer" class="row g-3">
        <!-- Customer Info Card -->
        <div class="col-12 col-md-6">
          <el-card shadow="never" class="h-100">
            <template #header>
              <div class="fw-bold">Customer Information</div>
            </template>
            <div class="details-grid">
              <div class="detail-item">
                <span class="detail-label">ID:</span>
                <span class="detail-value">#{{ detailsDialog.customer.id }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Name:</span>
                <span class="detail-value fw-bold">{{ detailsDialog.customer.fullName }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Email:</span>
                <span class="detail-value">{{ detailsDialog.customer.email }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Phone:</span>
                <span class="detail-value">{{ detailsDialog.customer.phone || '—' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Birth Date:</span>
                <span class="detail-value">{{ detailsDialog.customer.birthDate || '—' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Address:</span>
                <span class="detail-value">{{ detailsDialog.customer.address || '—' }}</span>
              </div>
            </div>
          </el-card>
        </div>

        <!-- Loyalty Info Card -->
        <div class="col-12 col-md-6">
          <el-card shadow="never" class="h-100">
            <template #header>
              <div class="fw-bold">Loyalty Information</div>
            </template>
            <div class="details-grid">
              <div class="detail-item">
                <span class="detail-label">Type:</span>
                <el-tag :type="detailsDialog.customer.customerType === 'VIP' ? 'warning' : 'info'">
                  {{ detailsDialog.customer.customerType }}
                </el-tag>
              </div>
              <div class="detail-item">
                <span class="detail-label">VIP Tier:</span>
                <el-tag 
                  v-if="detailsDialog.customer.raw.vipTier"
                  :type="getTierTagType(detailsDialog.customer.raw.vipTier)"
                >
                  {{ detailsDialog.customer.raw.vipTier }}
                </el-tag>
                <span v-else class="text-muted">—</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Points:</span>
                <span class="detail-value fw-bold text-primary fs-5">
                  {{ detailsDialog.customer.loyaltyPoints }}
                </span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Total Spent:</span>
                <span class="detail-value">
                  {{ formatCurrency(detailsDialog.customer.raw.totalSpent) }}
                </span>
              </div>
            </div>
          </el-card>
        </div>

        <!-- Notes Card -->
        <div class="col-12" v-if="detailsDialog.customer.notes">
          <el-card shadow="never">
            <template #header>
              <div class="fw-bold">Notes</div>
            </template>
            <p class="mb-0">{{ detailsDialog.customer.notes }}</p>
          </el-card>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailsDialog.open = false">Close</el-button>
        <el-button type="primary" @click="openEdit(detailsDialog.customer)">
          <el-icon class="me-1"><Edit /></el-icon>
          Edit Customer
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { 
  Calendar, Check, Close, Filter, Refresh, Plus, Search,
  User, Message, Phone as PhoneIcon, Location, Edit, Delete, View,
  Coin
} from '@element-plus/icons-vue';
import { customersApi } from "../../api/customers.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

// ===========================
// STATE
// ===========================
const loading = ref(false);
const rows = ref([]);

const q = ref("");
const typeFilter = ref("");
const vipTierFilter = ref("");
const activityFilter = ref("");
const pointsMin = ref(null);
const pointsMax = ref(null);

// Applied points filter (for display in tags)
const appliedPointsMin = ref(null);
const appliedPointsMax = ref(null);

const page = ref(1);
const pageSize = 10;

const dlg = reactive({
  open: false,
  mode: "create",
  loading: false,
  alert: "",
  id: null,
  form: {
    fullName: "",
    email: "",
    phone: "",
    birthDate: "",
    customerType: "REGULAR",
    address: "",
    notes: "",
  },
});

const detailsDialog = reactive({
  open: false,
  customer: null,
});

// ===========================
// COMPUTED
// ===========================
const hasActiveFilters = computed(() => {
  return typeFilter.value || vipTierFilter.value || activityFilter.value || isPointsFilterApplied.value;
});

const isPointsFilterApplied = computed(() => {
  return appliedPointsMin.value !== null || appliedPointsMax.value !== null;
});

const canApplyPointsFilter = computed(() => {
  return pointsMin.value !== null || pointsMax.value !== null;
});

const getStatisticsTitle = computed(() => {
  if (activityFilter.value === 'ACTIVE_30') return 'Active Customers (Last 30 Days)';
  if (vipTierFilter.value) return `VIP ${vipTierFilter.value} Customers`;
  if (isPointsFilterApplied.value) return 'Filtered by Points Range';
  if (typeFilter.value) return `${typeFilter.value} Customers`;
  return 'All Customers';
});

const totalPoints = computed(() => {
  return rows.value.reduce((sum, r) => sum + (r.loyaltyPoints || 0), 0).toLocaleString();
});

const avgPoints = computed(() => {
  if (rows.value.length === 0) return 0;
  const total = rows.value.reduce((sum, r) => sum + (r.loyaltyPoints || 0), 0);
  return Math.round(total / rows.value.length).toLocaleString();
});

const filtered = computed(() => {
  const kw = (q.value || "").trim().toLowerCase();
  if (!kw) return rows.value;
  return rows.value.filter((r) =>
    `${r.fullName} ${r.email} ${r.phone}`.toLowerCase().includes(kw)
  );
});

const paged = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filtered.value.slice(start, start + pageSize);
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
    birthDate: c?.birthDate ?? c?.dateOfBirth ?? "",
    customerType: (c?.customerType ?? "REGULAR").toString().toUpperCase(),
    loyaltyPoints: c?.loyaltyPoints ?? 0,
    address: c?.address ?? "",
    notes: c?.notes ?? "",
    raw: c,
  }));
}

function getTierTagType(tier) {
  const types = {
    BRONZE: 'info',
    SILVER: '',
    GOLD: 'warning',
    PLATINUM: 'danger',
    DIAMOND: 'success'
  };
  return types[tier] || 'info';
}

function getProgressColor(tier) {
  const colors = {
    BRONZE: '#909399',
    SILVER: '#C0C4CC',
    GOLD: '#E6A23C',
    PLATINUM: '#F56C6C',
    DIAMOND: '#67C23A'
  };
  return colors[tier] || '#409EFF';
}

function getPointsProgress(customer) {
  if (!customer.vipTier) return 0;
  
  const tierThresholds = {
    BRONZE: { min: 0, max: 500 },
    SILVER: { min: 500, max: 1500 },
    GOLD: { min: 1500, max: 3000 },
    PLATINUM: { min: 3000, max: 6000 },
    DIAMOND: { min: 6000, max: 10000 }
  };
  
  const threshold = tierThresholds[customer.vipTier];
  if (!threshold) return 0;
  
  const points = customer.loyaltyPoints || 0;
  const range = threshold.max - threshold.min;
  const progress = ((points - threshold.min) / range) * 100;
  
  return Math.min(Math.max(progress, 0), 100);
}

function getInitials(name) {
  if (!name) return '?';
  const parts = name.trim().split(' ');
  if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function getAvatarColor(name) {
  const colors = [
    '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
    '#00d2d3', '#e91e63', '#9c27b0', '#673ab7', '#3f51b5'
  ];
  const hash = name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
  return colors[hash % colors.length];
}

function formatCurrency(value) {
  if (!value) return '0 VNĐ';
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(value);
}

// ===========================
// API FUNCTIONS
// ===========================
async function load() {
  loading.value = true;
  try {
    let res;
    
    // Priority 1: Points range with VIP tier
    if (isPointsFilterApplied.value && vipTierFilter.value) {
      res = await customersApi.listByVipTierAndPoints(
        vipTierFilter.value,
        appliedPointsMin.value || 0,
        appliedPointsMax.value || 999999
      );
    }
    // Priority 2: Points range only
    else if (isPointsFilterApplied.value) {
      res = await customersApi.listByPointsRange(
        appliedPointsMin.value || 0,
        appliedPointsMax.value || 999999
      );
    }
    // Priority 3: VIP tier only
    else if (vipTierFilter.value) {
      res = await customersApi.listByVipTier(vipTierFilter.value);
    }
    // Priority 4: Activity filter
    else if (activityFilter.value === 'ACTIVE_30') {
      res = await customersApi.listActiveLast30Days();
    }
    // Priority 5: Type filter
    else if (typeFilter.value) {
      res = await customersApi.listByType(typeFilter.value);
    }
    // Default: All customers
    else {
      res = await customersApi.listAll();
    }

    rows.value = normalize(extractList(res?.data));
    page.value = 1;
  } catch (error) {
    console.error("Load error:", error);
    rows.value = [];
    toast("Failed to load customers.", "error");
  } finally {
    loading.value = false;
  }
}

function handleFilterChange() {
  // When selecting activity/type/tier filter, clear points filter
  if (activityFilter.value || typeFilter.value || vipTierFilter.value) {
    appliedPointsMin.value = null;
    appliedPointsMax.value = null;
    pointsMin.value = null;
    pointsMax.value = null;
  }
  load();
}

function applyPointsFilter() {
  if (!canApplyPointsFilter.value) {
    toast("Please enter at least one point value", "warning");
    return;
  }
  
  // Validate range
  if (pointsMin.value !== null && pointsMax.value !== null && pointsMin.value > pointsMax.value) {
    toast("Min points cannot be greater than max points", "error");
    return;
  }
  
  // Apply the filter
  appliedPointsMin.value = pointsMin.value;
  appliedPointsMax.value = pointsMax.value;
  
  // Clear other filters when applying points filter
  activityFilter.value = "";
  typeFilter.value = "";
  
  load();
}

function clearPointsFilter() {
  pointsMin.value = null;
  pointsMax.value = null;
  appliedPointsMin.value = null;
  appliedPointsMax.value = null;
  load();
}

function clearAllFilters() {
  typeFilter.value = "";
  vipTierFilter.value = "";
  activityFilter.value = "";
  pointsMin.value = null;
  pointsMax.value = null;
  appliedPointsMin.value = null;
  appliedPointsMax.value = null;
  load();
}

// ===========================
// DIALOG FUNCTIONS
// ===========================
function openCreate() {
  dlg.open = true;
  dlg.mode = "create";
  dlg.alert = "";
  dlg.id = null;
  dlg.form = {
    fullName: "",
    email: "",
    phone: "",
    birthDate: "",
    customerType: "REGULAR",
    address: "",
    notes: "",
  };
}

function openEdit(row) {
  dlg.open = true;
  dlg.mode = "edit";
  dlg.alert = "";
  dlg.id = row?.id;
  dlg.form = {
    fullName: row?.fullName || "",
    email: row?.email || "",
    phone: row?.phone || "",
    birthDate: row?.birthDate || "",
    customerType: row?.customerType || "REGULAR",
    address: row?.address || "",
    notes: row?.notes || "",
  };
  
  // Close details dialog if open
  detailsDialog.open = false;
}

function viewDetails(row) {
  detailsDialog.customer = row;
  detailsDialog.open = true;
}

async function save() {
  dlg.alert = "";
  
  // Validation
  if (!dlg.form.fullName || !dlg.form.email || !dlg.form.phone) {
    dlg.alert = "Full name, email, and phone are required.";
    return;
  }

  dlg.loading = true;
  try {
    if (dlg.mode === "create") {
      await customersApi.create({ ...dlg.form });
      toast("Customer created successfully!", "success");
    } else {
      await customersApi.update(dlg.id, { ...dlg.form });
      toast("Customer updated successfully!", "success");
    }
    dlg.open = false;
    await load();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Save failed";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

async function remove(row) {
  const ok = await confirmModal(
    `Are you sure you want to delete customer "${row?.fullName}"?`,
    "Confirm Delete",
    "Delete",
    true
  );
  if (!ok) return;

  try {
    await customersApi.remove(row.id);
    toast("Customer deleted successfully!", "success");
    await load();
  } catch {
    toast("Delete failed.", "error");
  }
}

// ===========================
// LIFECYCLE
// ===========================
onMounted(load);
</script>

<style scoped>
.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.title {
  font-weight: 900;
  font-size: 24px;
  margin-bottom: 4px;
}

.muted {
  color: rgba(15, 23, 42, 0.62);
  font-size: 13px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 4px;
  display: block;
}

.details-grid {
  display: grid;
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.detail-value {
  color: #303133;
  font-size: 14px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.el-pagination) {
  padding: 0;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style>