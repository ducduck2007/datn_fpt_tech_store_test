<!-- FILE: src/pages/system/PaymentManagement.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">System</div>
          <div class="title">Payment History</div>
          <div class="muted">View all payment transactions</div>
        </div>
        <el-button @click="loadPayments" :loading="loading">
          <i class="bi bi-arrow-clockwise me-1"></i>Reload
        </el-button>
      </div>

      <el-divider />

      <!-- Filters -->
      <div class="row g-3 mb-3">
        <div class="col-12 col-md-4">
          <el-input
            v-model="filters.search"
            placeholder="Search by Order ID or Transaction Ref..."
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <i class="bi bi-search"></i>
            </template>
          </el-input>
        </div>
        <div class="col-12 col-md-3">
          <el-select
            v-model="filters.status"
            placeholder="Filter by Status"
            clearable
            @change="applyFilters"
            style="width: 100%"
          >
            <el-option label="All Status" value="" />
            <el-option label="Paid" value="PAID" />
            <el-option label="Refunded" value="REFUNDED" />
            <el-option label="Pending" value="PENDING" />
            <el-option label="Failed" value="FAILED" />
          </el-select>
        </div>
        <div class="col-12 col-md-3">
          <el-select
            v-model="filters.method"
            placeholder="Filter by Method"
            clearable
            @change="applyFilters"
            style="width: 100%"
          >
            <el-option label="All Methods" value="" />
            <el-option label="Cash" value="CASH" />
            <el-option label="Bank Transfer" value="BANK_TRANSFER" />
            <el-option label="Credit Card" value="CREDIT_CARD" />
            <el-option label="E-Wallet" value="E_WALLET" />
          </el-select>
        </div>
      </div>

      <!-- Table -->
      <el-table
        :data="filteredPayments"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        
        <el-table-column prop="orderId" label="Order ID" width="100" sortable>
          <template #default="{ row }">
            <router-link
              :to="`/system/orders/${row.orderId}`"
              class="text-primary text-decoration-none"
            >
              #{{ row.orderId }}
            </router-link>
          </template>
        </el-table-column>

        <el-table-column prop="customerName" label="Customer" min-width="150">
          <template #default="{ row }">
            <div>{{ row.customerName || 'N/A' }}</div>
            <div class="text-muted small">ID: {{ row.customerId }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="amount" label="Amount" width="130" sortable>
          <template #default="{ row }">
            <div class="fw-bold">{{ formatCurrency(row.amount) }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="method" label="Method" width="140">
          <template #default="{ row }">
            <el-tag :type="getMethodType(row.method)" size="small">
              {{ formatMethod(row.method) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="transactionRef" label="Transaction Ref" min-width="150">
          <template #default="{ row }">
            <code class="small">{{ row.transactionRef || '-' }}</code>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="Status" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="paidAt" label="Paid At" width="160" sortable>
          <template #default="{ row }">
            {{ formatDate(row.paidAt) }}
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              @click="viewDetail(row)"
              text
            >
              <i class="bi bi-eye"></i>
            </el-button>
            <el-button
              v-if="row.status === 'PAID'"
              size="small"
              type="danger"
              @click="confirmRefund(row)"
              text
            >
              <i class="bi bi-arrow-counterclockwise"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Empty State -->
      <div v-if="!loading && filteredPayments.length === 0" class="text-center py-5">
        <i class="bi bi-inbox display-1 text-muted"></i>
        <div class="mt-3 text-muted">No payment records found</div>
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailDialog"
      title="Payment Details"
      width="600px"
    >
      <div v-if="selectedPayment" class="payment-detail">
        <div class="row g-3">
          <div class="col-6">
            <div class="detail-label">Payment ID</div>
            <div class="detail-value">#{{ selectedPayment.id }}</div>
          </div>
          <div class="col-6">
            <div class="detail-label">Order ID</div>
            <div class="detail-value">
              <router-link
                :to="`/system/orders/${selectedPayment.orderId}`"
                class="text-primary text-decoration-none"
              >
                #{{ selectedPayment.orderId }}
              </router-link>
            </div>
          </div>
          <div class="col-6">
            <div class="detail-label">Customer</div>
            <div class="detail-value">{{ selectedPayment.customerName }}</div>
            <div class="text-muted small">ID: {{ selectedPayment.customerId }}</div>
          </div>
          <div class="col-6">
            <div class="detail-label">Amount</div>
            <div class="detail-value fw-bold text-success">
              {{ formatCurrency(selectedPayment.amount) }}
            </div>
          </div>
          <div class="col-6">
            <div class="detail-label">Payment Method</div>
            <div class="detail-value">{{ formatMethod(selectedPayment.method) }}</div>
          </div>
          <div class="col-6">
            <div class="detail-label">Status</div>
            <div class="detail-value">
              <el-tag :type="getStatusType(selectedPayment.status)">
                {{ selectedPayment.status }}
              </el-tag>
            </div>
          </div>
          <div class="col-12">
            <div class="detail-label">Transaction Reference</div>
            <div class="detail-value">
              <code>{{ selectedPayment.transactionRef || 'N/A' }}</code>
            </div>
          </div>
          <div class="col-6">
            <div class="detail-label">Paid At</div>
            <div class="detail-value">{{ formatDate(selectedPayment.paidAt) }}</div>
          </div>
          <div class="col-6">
            <div class="detail-label">Created At</div>
            <div class="detail-value">{{ formatDate(selectedPayment.createdAt) }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialog = false">Close</el-button>
        <el-button
          v-if="selectedPayment?.status === 'PAID'"
          type="danger"
          @click="handleRefund(selectedPayment.id)"
          :loading="refunding"
        >
          Process Refund
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { paymentsApi } from "../../api/payments";
import { toast } from "../../ui/toast";
import { ElMessageBox } from "element-plus";

const loading = ref(false);
const refunding = ref(false);
const payments = ref([]);
const detailDialog = ref(false);
const selectedPayment = ref(null);

const filters = ref({
  search: "",
  status: "",
  method: "",
});

// Computed
const filteredPayments = computed(() => {
  let result = [...payments.value];

  // Search filter
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase();
    result = result.filter(
      (p) =>
        p.orderId?.toString().includes(search) ||
        p.transactionRef?.toLowerCase().includes(search) ||
        p.customerName?.toLowerCase().includes(search)
    );
  }

  // Status filter
  if (filters.value.status) {
    result = result.filter((p) => p.status === filters.value.status);
  }

  // Method filter
  if (filters.value.method) {
    result = result.filter((p) => p.method === filters.value.method);
  }

  return result;
});

// Methods
async function loadPayments() {
  loading.value = true;
  try {
    const { data } = await paymentsApi.getAll();
    payments.value = data || [];
    toast("Payments loaded successfully", "success");
  } catch (error) {
    console.error("Error loading payments:", error);
    toast("Failed to load payments", "error");
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  // Debounce could be added here if needed
}

function applyFilters() {
  // Filters are reactive, no action needed
}

function viewDetail(payment) {
  selectedPayment.value = payment;
  detailDialog.value = true;
}

async function confirmRefund(payment) {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to refund payment #${payment.id} (${formatCurrency(payment.amount)})?`,
      "Confirm Refund",
      {
        confirmButtonText: "Refund",
        cancelButtonText: "Cancel",
        type: "warning",
      }
    );
    await handleRefund(payment.id);
  } catch {
    // User cancelled
  }
}

async function handleRefund(paymentId) {
  refunding.value = true;
  try {
    await paymentsApi.refund(paymentId);
    toast("Payment refunded successfully", "success");
    detailDialog.value = false;
    await loadPayments();
  } catch (error) {
    console.error("Error refunding payment:", error);
    toast("Failed to refund payment", "error");
  } finally {
    refunding.value = false;
  }
}

function formatCurrency(amount) {
  if (!amount) return "₫0";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
}

function formatDate(date) {
  if (!date) return "N/A";
  return new Date(date).toLocaleString("vi-VN");
}

function formatMethod(method) {
  const methods = {
    CASH: "Cash",
    BANK_TRANSFER: "Bank Transfer",
    CREDIT_CARD: "Credit Card",
    E_WALLET: "E-Wallet",
  };
  return methods[method] || method;
}

function getMethodType(method) {
  const types = {
    CASH: "success",
    BANK_TRANSFER: "primary",
    CREDIT_CARD: "warning",
    E_WALLET: "info",
  };
  return types[method] || "info";
}

function getStatusType(status) {
  const types = {
    PAID: "success",
    REFUNDED: "info",
    PENDING: "warning",
    FAILED: "danger",
  };
  return types[status] || "info";
}

// Lifecycle
onMounted(() => {
  loadPayments();
});
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

.payment-detail {
  padding: 1rem 0;
}

.detail-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  text-transform: uppercase;
  font-weight: 600;
}

.detail-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

code {
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}
</style>