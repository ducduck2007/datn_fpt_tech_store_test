<template>
  <div class="payment-success-wrapper">
    <el-card class="box-card shadow-sm">
      <el-skeleton v-if="loading" :rows="8" animated />

      <template v-else>
        <div class="text-center mb-4">
          <div class="success-icon mb-3">
            <i class="bi bi-check-circle-fill"></i>
          </div>
          <div class="success-title">Payment Successful!</div>
          <div class="success-subtitle">
            Your payment has been processed successfully
          </div>
        </div>

        <el-divider />

        <div v-if="payment" class="payment-details">
          <div class="section-title mb-3">
            <i class="bi bi-receipt me-2"></i>Payment Details
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="Payment ID">
              <code>#{{ payment.id }}</code>
            </el-descriptions-item>
            
            <el-descriptions-item label="Order ID">
              <router-link
                :to="`/orders/${payment.orderId}`"
                class="text-primary text-decoration-none fw-bold"
              >
                #{{ payment.orderId }}
              </router-link>
            </el-descriptions-item>

            <el-descriptions-item label="Amount Paid">
              <span class="amount-paid">{{ formatCurrency(payment.amount) }}</span>
            </el-descriptions-item>

            <el-descriptions-item label="Payment Method">
              <el-tag :type="getMethodType(payment.method)" size="small">
                {{ formatMethod(payment.method) }}
              </el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="Transaction Reference">
              <code class="small">{{ payment.transactionRef || 'N/A' }}</code>
            </el-descriptions-item>

            <el-descriptions-item label="Status">
              <el-tag type="success" size="small">
                {{ payment.status }}
              </el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="Paid At" :span="2">
              {{ formatDate(payment.paidAt) }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="customerInfo" class="mt-4">
            <el-alert type="success" :closable="false" class="loyalty-alert">
              <template #title>
                <div class="d-flex align-items-center">
                  <i class="bi bi-star-fill me-2"></i>
                  <span>Loyalty Points Earned!</span>
                </div>
              </template>
              <div class="loyalty-content">
                <div class="mb-2">
                  <strong>Points Earned:</strong> +{{ pointsEarned }} points
                </div>
                <div class="mb-2">
                  <strong>Current Tier:</strong>
                  <el-tag :type="getTierType(customerInfo.vipTier)" size="small" class="ms-1">
                    {{ customerInfo.vipTierDisplay }}
                  </el-tag>
                </div>
                <div class="mb-2">
                  <strong>Total Points:</strong> {{ customerInfo.loyaltyPoints }} points
                </div>
                <div v-if="customerInfo.pointsToNextTier > 0">
                  <strong>Points to Next Tier:</strong> {{ customerInfo.pointsToNextTier }} points
                </div>
                <div v-else class="text-success fw-bold">
                  🎉 Congratulations! You've reached the maximum tier!
                </div>
              </div>
            </el-alert>
          </div>

          <div class="action-buttons mt-4 d-flex gap-2 justify-content-center flex-wrap">
            <el-button type="primary" @click="$router.push(`/orders/${payment.orderId}`)">
              <i class="bi bi-box-seam me-1"></i>
              View Order Details
            </el-button>
            <el-button @click="$router.push('/orders/new')">
              <i class="bi bi-plus-circle me-1"></i>
              Create New Order
            </el-button>
            <el-button @click="$router.push('/')">
              <i class="bi bi-house me-1"></i>
              Back to Home
            </el-button>
          </div>

          <div class="text-center mt-3">
            <el-button text @click="printReceipt">
              <i class="bi bi-printer me-1"></i>
              Print Receipt
            </el-button>
          </div>
        </div>

        <el-alert
          v-if="error"
          :title="error"
          type="error"
          show-icon
          class="mb-3"
        />
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
// Đảm bảo đường dẫn import đúng với dự án của bạn
import { paymentsApi } from "../../api/payments"; 
import { toast } from "../../ui/toast";

const route = useRoute();
const paymentId = computed(() => route.params.paymentId);

const loading = ref(false);
const error = ref("");
const payment = ref(null);
const customerInfo = ref(null);

// Tính toán điểm được cộng (Logic: 10,000 VND = 1 điểm)
const pointsEarned = computed(() => {
  if (!payment.value?.amount) return 0;
  return Math.floor(payment.value.amount / 10000);
});

async function loadPaymentDetails() {
  loading.value = true;
  error.value = "";

  try {
    const { data } = await paymentsApi.getById(paymentId.value);
    payment.value = data;

    // --- LOGIC LẤY THÔNG TIN KHÁCH HÀNG ---
    if (data.customerId) {
      /* TODO: Gọi API thực tế ở đây
       const customerRes = await customersApi.getById(data.customerId);
       customerInfo.value = customerRes.data;
      */

      // MOCK DATA (Dữ liệu giả lập để hiển thị giao diện)
      customerInfo.value = {
        loyaltyPoints: (pointsEarned.value || 0) + 100, // Giả sử cộng dồn vào điểm cũ
        vipTier: "BRONZE",
        vipTierDisplay: "Bronze Member",
        pointsToNextTier: 50,
      };
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Failed to load payment details";
    error.value = msg;
    toast(msg, "error");
  } finally {
    loading.value = false;
  }
}

// --- HELPER FUNCTIONS ---

function formatCurrency(amount) {
  if (!amount && amount !== 0) return "₫0";
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

function getTierType(tier) {
  const types = {
    BRONZE: "info",
    SILVER: "primary",
    GOLD: "warning",
    DIAMOND: "danger",
  };
  return types[tier] || "info";
}

function printReceipt() {
  window.print();
}

onMounted(() => {
  if (paymentId.value) {
    loadPaymentDetails();
  } else {
    error.value = "Payment ID not found.";
  }
});
</script>

<style scoped>
.payment-success-wrapper {
  padding-top: 40px;
  padding-bottom: 40px;
  max-width: 800px; /* Giới hạn chiều rộng để thẻ không bị quá to */
  margin: 0 auto;
}

.success-icon {
  font-size: 80px;
  color: #67c23a; /* Element Plus Success Color */
  animation: scaleIn 0.5s ease-out;
}

.success-title {
  font-size: 28px;
  font-weight: 900;
  color: #67c23a;
  margin-bottom: 8px;
}

.success-subtitle {
  font-size: 16px;
  color: rgba(15, 23, 42, 0.62);
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.amount-paid {
  font-size: 20px;
  font-weight: 900;
  color: #67c23a;
}

.loyalty-alert {
  border-left: 4px solid #67c23a;
}

.loyalty-content {
  margin-top: 12px;
  font-size: 14px;
}

code {
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  color: #c0392b;
  font-family: monospace;
}

/* Animations */
@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

/* Print Styles */
@media print {
  .payment-success-wrapper {
    padding: 0;
    max-width: 100%;
  }
  
  .action-buttons,
  .el-button,
  .success-icon {
    display: none !important;
  }
  
  .box-card {
    border: none;
    box-shadow: none !important;
  }
}
</style>