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

      <!-- Table - CHỈ HIỂN THỊ THÔNG TIN CƠ BẢN -->
      <el-table
        :data="filteredPayments"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        
        <el-table-column prop="orderId" label="Order" width="120" sortable>
          <template #default="{ row }">
            <div class="fw-bold">#{{ row.orderId }}</div>
            <div class="text-muted small">{{ row.orderNumber }}</div>
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

        <el-table-column label="Actions" width="140" fixed="right">
          <template #default="{ row }">
            <!-- NÚT VIEW DETAIL - LOAD CHI TIẾT KHI CLICK -->
            <el-button
              size="small"
              type="primary"
              @click="viewDetail(row.id)"
              :loading="loadingDetail && selectedPaymentId === row.id"
            >
              <i class="bi bi-eye me-1"></i>Detail
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

    <!-- Detail Dialog - CHỈ HIỂN THỊ KHI CLICK VIEW DETAIL -->
    <el-dialog
      v-model="detailDialog"
      title="Payment Details"
      width="900px"
      top="5vh"
    >
      <div v-if="selectedPayment" class="payment-detail">
        <!-- Payment & Order Info -->
        <div class="row g-3 mb-4">
          <div class="col-12">
            <h6 class="fw-bold mb-3">
              <i class="bi bi-receipt-cutoff me-2"></i>Payment Information
            </h6>
          </div>
          <div class="col-6 col-md-3">
            <div class="detail-label">Payment ID</div>
            <div class="detail-value">#{{ selectedPayment.id }}</div>
          </div>
          <div class="col-6 col-md-3">
            <div class="detail-label">Order Number</div>
            <div class="detail-value">
              <router-link
                :to="`/system/orders/${selectedPayment.orderId}`"
                class="text-primary text-decoration-none"
              >
                {{ selectedPayment.orderNumber }}
              </router-link>
            </div>
          </div>
          <div class="col-6 col-md-3">
            <div class="detail-label">Amount</div>
            <div class="detail-value fw-bold text-success">
              {{ formatCurrency(selectedPayment.amount) }}
            </div>
          </div>
          <div class="col-6 col-md-3">
            <div class="detail-label">Status</div>
            <div class="detail-value">
              <el-tag :type="getStatusType(selectedPayment.status)">
                {{ selectedPayment.status }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- Customer Info -->
        <div class="row g-3 mb-4">
          <div class="col-12">
            <h6 class="fw-bold mb-3">
              <i class="bi bi-person me-2"></i>Customer Information
            </h6>
          </div>
          <div class="col-6">
            <div class="detail-label">Name</div>
            <div class="detail-value">{{ selectedPayment.customerName }}</div>
          </div>
          <div class="col-6">
            <div class="detail-label">Email</div>
            <div class="detail-value">{{ selectedPayment.customerEmail || 'N/A' }}</div>
          </div>
          <div class="col-6">
            <div class="detail-label">Phone</div>
            <div class="detail-value">{{ selectedPayment.customerPhone || 'N/A' }}</div>
          </div>
          <div class="col-6">
            <div class="detail-label">Type / Tier</div>
            <div class="detail-value">
              <el-tag size="small" type="info" class="me-1">
                {{ selectedPayment.customerType || 'REGULAR' }}
              </el-tag>
              <el-tag v-if="selectedPayment.vipTier" size="small" type="warning">
                {{ selectedPayment.vipTier }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- Purchased Items - CHỈ HIỂN THỊ KHI ĐÃ LOAD DETAIL -->
        <div class="row g-3 mb-4" v-if="selectedPayment.items && selectedPayment.items.length > 0">
          <div class="col-12">
            <h6 class="fw-bold mb-3">
              <i class="bi bi-bag-check me-2"></i>Purchased Items
            </h6>
          </div>
          <div class="col-12">
            <el-table :data="selectedPayment.items" border style="width: 100%">
              <el-table-column label="Image" width="80">
                <template #default="{ row }">
                  <img 
                    v-if="row.imageUrl" 
                    :src="row.imageUrl" 
                    class="item-image"
                    @error="handleImageError"
                  />
                  <div v-else class="item-image-placeholder">
                    <i class="bi bi-image"></i>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column label="Product" min-width="200">
                <template #default="{ row }">
                  <div class="fw-bold">{{ row.productName }}</div>
                  <div class="text-muted small" v-if="row.variantName">
                    {{ row.variantName }}
                  </div>
                  <div class="text-muted small" v-if="row.sku">
                    SKU: {{ row.sku }}
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="quantity" label="Qty" width="80" align="center">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.quantity }}</el-tag>
                </template>
              </el-table-column>
              
              <el-table-column label="Unit Price" width="120" align="right">
                <template #default="{ row }">
                  {{ formatCurrency(row.unitPrice) }}
                </template>
              </el-table-column>
              
              <el-table-column label="Discount" width="120" align="right">
                <template #default="{ row }">
                  <span v-if="row.discount > 0" class="text-danger">
                    -{{ formatCurrency(row.discount) }}
                  </span>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>
              
              <el-table-column label="Total" width="130" align="right">
                <template #default="{ row }">
                  <div class="fw-bold">{{ formatCurrency(row.lineTotal) }}</div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- Order Summary -->
        <div class="row g-3">
          <div class="col-12 col-md-6">
            <h6 class="fw-bold mb-3">
              <i class="bi bi-info-circle me-2"></i>Additional Details
            </h6>
            <div class="mb-2">
              <div class="detail-label">Payment Method</div>
              <div class="detail-value">{{ formatMethod(selectedPayment.method) }}</div>
            </div>
            <div class="mb-2">
              <div class="detail-label">Transaction Ref</div>
              <div class="detail-value">
                <code>{{ selectedPayment.transactionRef || 'N/A' }}</code>
              </div>
            </div>
            <div class="mb-2">
              <div class="detail-label">Channel</div>
              <div class="detail-value">{{ selectedPayment.channel || 'N/A' }}</div>
            </div>
            <div class="mb-2">
              <div class="detail-label">Order Status</div>
              <div class="detail-value">
                <el-tag :type="selectedPayment.orderStatus === 'DELIVERED' ? 'success' : 'warning'">
                  {{ selectedPayment.orderStatus }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="col-12 col-md-6">
            <h6 class="fw-bold mb-3">
              <i class="bi bi-calculator me-2"></i>Order Summary
            </h6>
            <div class="summary-row">
              <span>Subtotal:</span>
              <span>{{ formatCurrency(selectedPayment.subtotal) }}</span>
            </div>
            <div class="summary-row" v-if="selectedPayment.discountTotal > 0">
              <span>Discount:</span>
              <span class="text-danger">-{{ formatCurrency(selectedPayment.discountTotal) }}</span>
            </div>
            <div class="summary-row" v-if="selectedPayment.taxTotal > 0">
              <span>Tax:</span>
              <span>{{ formatCurrency(selectedPayment.taxTotal) }}</span>
            </div>
            <div class="summary-row" v-if="selectedPayment.shippingFee > 0">
              <span>Shipping:</span>
              <span>{{ formatCurrency(selectedPayment.shippingFee) }}</span>
            </div>
            <el-divider class="my-2" />
            <div class="summary-row fw-bold fs-5">
              <span>Total:</span>
              <span class="text-success">{{ formatCurrency(selectedPayment.totalAmount) }}</span>
            </div>
          </div>

          <div class="col-12" v-if="selectedPayment.notes">
            <div class="detail-label">Notes</div>
            <div class="detail-value">{{ selectedPayment.notes }}</div>
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
import { ref, computed } from "vue";
import { paymentsApi } from "../../api/payments";
import { toast } from "../../ui/toast";
import { ElMessageBox } from "element-plus";

const loading = ref(false);
const loadingDetail = ref(false);
const refunding = ref(false);
const payments = ref([]);
const detailDialog = ref(false);
const selectedPayment = ref(null);
const selectedPaymentId = ref(null);

const filters = ref({
  search: "",
  status: "",
  method: "",
});

// Computed
const filteredPayments = computed(() => {
  let result = [...payments.value];

  if (filters.value.search) {
    const search = filters.value.search.toLowerCase();
    result = result.filter(
      (p) =>
        p.orderId?.toString().includes(search) ||
        p.orderNumber?.toLowerCase().includes(search) ||
        p.transactionRef?.toLowerCase().includes(search) ||
        p.customerName?.toLowerCase().includes(search)
    );
  }

  if (filters.value.status) {
    result = result.filter((p) => p.status === filters.value.status);
  }

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

function applyFilters() {
  // Filters are reactive, no action needed
}

// VIEW DETAIL - LOAD CHI TIẾT KHI CLICK
async function viewDetail(paymentId) {
  selectedPaymentId.value = paymentId;
  loadingDetail.value = true;
  try {
    const { data } = await paymentsApi.getDetail(paymentId);
    selectedPayment.value = data;
    detailDialog.value = true;
  } catch (error) {
    console.error("Error loading payment detail:", error);
    toast("Failed to load payment detail", "error");
  } finally {
    loadingDetail.value = false;
    selectedPaymentId.value = null;
  }
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

function handleImageError(e) {
  e.target.style.display = 'none';
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

// Auto load on mount
loadPayments();
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
  max-height: 70vh;
  overflow-y: auto;
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

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.item-image-placeholder {
  width: 60px;
  height: 60px;
  background: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 24px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
}

code {
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}
</style>