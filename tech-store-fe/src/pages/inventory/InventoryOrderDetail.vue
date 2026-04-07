<template>
  <div v-if="order" class="order-detail-container">
    <!-- PRINT ONLY SECTION -->
    <div class="print-section">
      <div class="print-header">
        <h1>PHIẾU GIAO HÀNG</h1>
        <p>Tech Store</p>
      </div>

      <div class="print-info">
        <div class="print-info-left">
          <strong>Thông tin khách hàng:</strong><br>
          {{ order.customerName }}<br>
          {{ order.customerPhone }}<br>
          {{ order.customerAddress }}
        </div>
        <div class="print-info-right">
          <strong>Mã đơn hàng:</strong> #{{ order.orderNumber }}<br>
          <strong>Ngày tạo:</strong> {{ formatDateTime(order.createdAt) }}<br>
          <strong>Trạng thái:</strong> {{ statusLabel(order.status) }}
        </div>
      </div>

      <table class="print-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>Tên sản phẩm</th>
            <th style="text-align: center;">SKU</th>
            <th style="text-align: center;">SL</th>
            <th style="text-align: right;">Đơn giá</th>
            <th style="text-align: right;">Thành tiền</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in order.items" :key="item.id || index">
            <td style="text-align: center;">{{ index + 1 }}</td>
            <td>{{ item.productName }}</td>
            <td style="text-align: center;">{{ item.sku || '-' }}</td>
            <td style="text-align: center;">{{ item.quantity }}</td>
            <td style="text-align: right;">{{ formatVND(item.price) }}</td>
            <td style="text-align: right;">{{ formatVND(item.quantity * item.price) }}</td>
          </tr>
        </tbody>
      </table>

      <div class="print-summary">
        <div class="print-summary-row">
          <span>Tạm tính:</span>
          <span>{{ formatVND(order.subtotal) }}</span>
        </div>
        <div class="print-summary-row" v-if="order.vipDiscount > 0">
          <span>Giảm giá VIP:</span>
          <span>-{{ formatVND(order.vipDiscount) }}</span>
        </div>
        <div class="print-summary-row" v-if="order.couponDiscount > 0">
          <span>Mã giảm giá ({{ order.couponCode }}):</span>
          <span>-{{ formatVND(order.couponDiscount) }}</span>
        </div>
        <div class="print-summary-row" v-if="order.spinDiscount > 0">
          <span>Giảm giá vòng quay:</span>
          <span>-{{ formatVND(order.spinDiscount) }}</span>
        </div>
        <div class="print-summary-row">
          <span>Phí vận chuyển:</span>
          <span>{{ order.shippingFee > 0 ? formatVND(order.shippingFee) : 'Miễn phí' }}</span>
        </div>
        <div class="print-summary-row print-total">
          <span>Tổng cộng:</span>
          <span>{{ formatVND(order.totalAmount) }}</span>
        </div>
      </div>

      <div class="print-footer">
        Cảm ơn quý khách đã mua sắm tại Tech Store!<br>
        Mọi thắc mắc xin vui lòng liên hệ hotline hỗ trợ.
      </div>
    </div>

    <!-- MAIN UI APP CONTENT -->
    <el-space direction="vertical" alignment="stretch" :size="20" style="width: 100%" class="no-print">
    <!-- BACK LINK -->
    <el-button link @click="router.back()">
      <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="icon-spacing">
        <path d="M9 2L4 7l5 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      Quay lại danh sách
    </el-button>

    <!-- PAGE HEADER -->
    <el-row type="flex" justify="space-between" align="bottom">
      <el-col :span="16">
        <el-space direction="vertical" alignment="start" :size="4">
          <el-text type="info" size="small">Chi tiết đơn hàng</el-text>
          <el-space :size="12">
            <h2>#{{ order.orderNumber }}</h2>
            <el-tag :type="statusType(order.status)" effect="light" round>
              {{ statusLabel(order.status) }}
            </el-tag>
          </el-space>
          <el-text type="info">Kho hàng · Quản lý đơn hàng</el-text>
        </el-space>
      </el-col>

      <!-- INVENTORY ACTIONS -->
      <el-col :span="8" style="text-align: right;">
        <el-space>
          <el-button @click="printOrder" plain>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" class="icon-spacing">
              <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M6 14h12v8H6z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            In phiếu
          </el-button>

          <el-button
            v-if="order.status === 'PAID' || (order.status === 'PENDING' && order.paymentMethod === 'CASH' && order.channel === 'ONLINE')"
            type="warning"
            plain
            :loading="loading"
            @click="markProcessing"
          >
            Đóng gói
          </el-button>

          <el-button
            v-if="order.status === 'PROCESSING'"
            type="primary"
            plain
            :loading="loading"
            @click="markShipping"
          >
            Chuyển giao vận
          </el-button>
        </el-space>
      </el-col>
    </el-row>

    <!-- CONTENT GRID -->
    <el-row :gutter="20">
      <!-- LEFT COLUMN -->
      <el-col :span="16">
        <!-- ORDER ITEMS TABLE -->
        <el-card shadow="never">
          <template #header>
            <el-space>
              <el-text tag="b">Sản phẩm đặt mua</el-text>
              <el-text type="info" size="small">({{ order.items?.length || 0 }} sản phẩm)</el-text>
            </el-space>
          </template>

          <el-table :data="order.items" style="width: 100%">
            <el-table-column label="Sản phẩm" min-width="200">
              <template #default="{ row }">
                <el-space>
                  <div class="product-thumb">
                    <!-- SVG Kept but CSS removed -->
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <rect x="2" y="2" width="12" height="12" rx="3" fill="currentColor" fill-opacity="0.2"/>
                      <path d="M5 8h6M8 5v6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <el-space direction="vertical" alignment="start" :size="0">
                    <el-text tag="b">{{ row.productName }}</el-text>
                    <el-text type="info" size="small" v-if="row.sku">SKU: {{ row.sku }}</el-text>
                  </el-space>
                </el-space>
              </template>
            </el-table-column>

            <el-table-column label="Đơn giá" width="140" align="right">
              <template #default="{ row }">
                <el-text>{{ formatVND(row.price) }}</el-text>
              </template>
            </el-table-column>

            <el-table-column label="Số lượng" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="info" effect="plain">{{ row.quantity }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column label="Thành tiền" width="150" align="right">
              <template #default="{ row }">
                <el-text tag="b">{{ formatVND(row.quantity * row.price) }}</el-text>
              </template>
            </el-table-column>
          </el-table>

          <!-- TOTAL -->
          <el-row justify="end" class="total-section">
            <el-col :span="8">
              <el-space direction="vertical" alignment="stretch" :size="8" style="width: 100%">
                <el-row justify="space-between">
                  <el-text type="info">Tạm tính</el-text>
                  <el-text>{{ formatVND(order.subtotal) }}</el-text>
                </el-row>
                <el-row justify="space-between" v-if="order.vipDiscount > 0">
                  <el-text type="info">Chiết khấu VIP</el-text>
                  <el-text type="success">-{{ formatVND(order.vipDiscount) }}</el-text>
                </el-row>
                <el-row justify="space-between" v-if="order.couponDiscount > 0">
                  <el-text type="info">Mã giảm giá ({{ order.couponCode }})</el-text>
                  <el-text type="success">-{{ formatVND(order.couponDiscount) }}</el-text>
                </el-row>
                <el-row justify="space-between" v-if="order.spinDiscount > 0">
                  <el-text type="info">Vòng quay may mắn</el-text>
                  <el-text type="warning">-{{ formatVND(order.spinDiscount) }}</el-text>
                </el-row>
                <el-row justify="space-between">
                  <el-text type="info">Phí vận chuyển</el-text>
                  <el-text>{{ order.shippingFee > 0 ? formatVND(order.shippingFee) : 'Miễn phí' }}</el-text>
                </el-row>
                <el-divider class="my-divider" />
                <el-row justify="space-between">
                  <el-text tag="b" size="large">Tổng cộng</el-text>
                  <el-text tag="b" size="large">{{ formatVND(order.totalAmount) }}</el-text>
                </el-row>
              </el-space>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- RIGHT COLUMN -->
      <el-col :span="8">
        <el-space direction="vertical" alignment="stretch" :size="16" style="width: 100%">
          <!-- ORDER INFO -->
          <el-card shadow="never">
            <template #header>
              <el-text tag="b">Thông tin đơn hàng</el-text>
            </template>
            <el-space direction="vertical" alignment="stretch" :size="12" style="width: 100%">
              <el-row justify="space-between">
                <el-text type="info">Mã đơn hàng</el-text>
                <el-text>#{{ order.orderNumber }}</el-text>
              </el-row>
              <el-row justify="space-between">
                <el-text type="info">Trạng thái</el-text>
                <el-tag :type="statusType(order.status)" effect="light" size="small" round>
                  {{ statusLabel(order.status) }}
                </el-tag>
              </el-row>
              <el-row justify="space-between">
                <el-text type="info">Ngày tạo</el-text>
                <el-text>{{ formatDateTime(order.createdAt) }}</el-text>
              </el-row>
            </el-space>
          </el-card>

          <!-- CUSTOMER INFO -->
          <el-card shadow="never">
            <template #header>
              <el-text tag="b">Thông tin khách hàng</el-text>
            </template>
            <el-space direction="vertical" alignment="stretch" :size="16" style="width: 100%">
              <el-space :size="12">
                <el-avatar :size="42">{{ initials(order.customerName) }}</el-avatar>
                <el-text tag="b" size="large">{{ order.customerName }}</el-text>
              </el-space>
              <el-space direction="vertical" alignment="stretch" :size="12" style="width: 100%">
                <el-row justify="space-between">
                  <el-text type="info">Số điện thoại</el-text>
                  <el-text>{{ order.customerPhone }}</el-text>
                </el-row>
                <el-row justify="space-between">
                  <el-text type="info">Địa chỉ</el-text>
                  <el-text class="text-right">{{ order.customerAddress }}</el-text>
                </el-row>
              </el-space>
            </el-space>
          </el-card>
        </el-space>
      </el-col>
    </el-row>
    </el-space>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { paymentsApi } from "../../api/payments";
import { ElMessage, ElMessageBox } from "element-plus";
import { formatVND, formatDateTime, initials } from "../../utils/format";

const route  = useRoute();
const router = useRouter();
const order  = ref(null);
const loading = ref(false);

const statusLabel = (s) => ({
  PAID: "Đã thanh toán",
  PROCESSING: "Đang xử lý",
  SHIPPING: "Đang giao",
  DONE: "Hoàn thành",
}[s] ?? s);

const statusType = (s) => ({
  PAID: "success",
  PROCESSING: "warning",
  SHIPPING: "primary",
  DONE: "info",
}[s] ?? "info");

const printOrder = () => {
  window.print();
};

const load = async () => {
  try {
    const res = await ordersApi.getById(route.params.orderId);
    order.value = res.data;
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "Không thể tải thông tin đơn hàng");
  }
};

const markProcessing = async () => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn bắt đầu xử lý đơn hàng này?',
      'Xác nhận xử lý',
      {
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy',
        type: 'warning',
      }
    );
    loading.value = true;
    
    // Bridge: Auto-pay for CASH orders that are still PENDING
    if (order.value.status === 'PENDING' && order.value.paymentMethod === 'CASH') {
      await paymentsApi.create({ 
        orderId: order.value.orderId, 
        method: 'CASH', 
        transactionRef: 'COD-DETAIL-CONFIRM' 
      });
    }

    await ordersApi.markAsProcessing(order.value.orderId);
    ElMessage.success("Đã chuyển đơn hàng sang trạng thái Đang xử lý");
    await load();
  } catch (error) {
    if (error !== 'cancel') {
        ElMessage.error(error.response?.data?.message || "Có lỗi xảy ra khi cập nhật trạng thái");
    }
  } finally {
    loading.value = false;
  }
};

const markShipping = async () => {
  try {
    await ElMessageBox.confirm(
      'Bạn có chắc chắn muốn chuyển đơn hàng này cho đơn vị vận chuyển?',
      'Xác nhận giao vận',
      {
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy',
        type: 'primary',
      }
    );
    loading.value = true;
    await ordersApi.markAsShipping(order.value.orderId);
    ElMessage.success("Đã chuyển đơn hàng sang trạng thái Đang giao hàng");
    await load();
  } catch (error) {
    if (error !== 'cancel') {
        ElMessage.error(error.response?.data?.message || "Có lỗi xảy ra khi cập nhật trạng thái");
    }
  } finally {
    loading.value = false;
  }
};

let timer;
onMounted(() => {
  load();
});
</script>

<style scoped>
.icon-spacing {
  margin-right: 6px;
}
.product-thumb {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.total-section {
  margin-top: 16px;
}
.my-divider {
  margin: 8px 0;
}
.text-right {
  text-align: right;
  max-width: 160px;
}
h2 {
  margin: 0;
}

@media screen {
  .print-section {
    display: none;
  }
}

@media print {
  .no-print {
    display: none !important;
  }
  
  @page {
    size: A4;
    margin: 0;
  }
  
  .print-section {
    display: block;
    font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
    color: #000;
    padding: 15mm;
  }
  
  .print-header {
    text-align: center;
    margin-bottom: 30px;
    border-bottom: 2px solid #000;
    padding-bottom: 15px;
  }
  
  .print-header h1 {
    font-size: 24px;
    margin: 0 0 5px 0;
    text-transform: uppercase;
  }
  
  .print-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 30px;
    line-height: 1.5;
  }
  
  .print-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 30px;
  }
  
  .print-table th, .print-table td {
    padding: 10px;
    text-align: left;
  }
  
  .print-table th {
    background-color: #f9f9f9 !important;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
    border: 1px solid #000;
  }
  .print-table td {
    border-bottom: 1px dashed #ccc;
  }
  
  .print-summary {
    width: 300px;
    float: right;
    margin-bottom: 30px;
  }
  
  .print-summary-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
  }
  
  .print-total {
    font-weight: bold;
    font-size: 1.2em;
    border-top: 1px solid #000;
    padding-top: 8px;
    margin-top: 8px;
  }
  
  .print-footer {
    clear: both;
    text-align: center;
    margin-top: 50px;
    font-style: italic;
  }
}
</style>

<style>
@media print {
  .el-aside, .el-header, .el-footer {
    display: none !important;
  }
  .el-main {
    margin-left: 0 !important;
    padding: 0 !important;
    overflow: visible !important;
    height: auto !important;
  }
  .el-container {
    height: auto !important;
    overflow: visible !important;
  }
  body, html, #app {
    height: auto !important;
    overflow: visible !important;
    background: white !important;
  }
}
</style>