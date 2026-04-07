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
            <th style="text-align:center">SKU</th>
            <th style="text-align:center">SL</th>
            <th style="text-align:right">Đơn giá</th>
            <th style="text-align:right">Thành tiền</th>
            <th>Serial Number</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in order.items" :key="item.id || index">
            <td style="text-align:center">{{ index + 1 }}</td>
            <td>{{ item.productName }}</td>
            <td style="text-align:center">{{ item.sku || '-' }}</td>
            <td style="text-align:center">{{ item.quantity }}</td>
            <td style="text-align:right">{{ formatVND(item.unitPrice) }}</td>
            <td style="text-align:right">{{ formatVND(item.quantity * item.unitPrice) }}</td>
            <td>{{ (item.serialNumbers || []).join(', ') || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <div class="print-summary">
        <div class="print-summary-row">
          <span>Tạm tính:</span><span>{{ formatVND(order.subtotal) }}</span>
        </div>
        <div class="print-summary-row" v-if="order.discountTotal > 0">
          <span>Giảm giá:</span><span>-{{ formatVND(order.discountTotal) }}</span>
        </div>
        <div class="print-summary-row">
          <span>Phí vận chuyển:</span>
          <span>{{ order.shippingFee > 0 ? formatVND(order.shippingFee) : 'Miễn phí' }}</span>
        </div>
        <div class="print-summary-row print-total">
          <span>Tổng cộng:</span><span>{{ formatVND(order.totalAmount) }}</span>
        </div>
      </div>
      <div class="print-footer">
        Cảm ơn quý khách đã mua sắm tại Tech Store!<br>
        Mọi thắc mắc xin vui lòng liên hệ hotline hỗ trợ.
      </div>
    </div>

    <!-- MAIN UI APP CONTENT -->
    <el-space direction="vertical" alignment="stretch" :size="20" style="width:100%" class="no-print">
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

        <el-col :span="8" style="text-align:right">
          <el-space>
            <el-button @click="printOrder" plain>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" class="icon-spacing">
                <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M6 14h12v8H6z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              In phiếu
            </el-button>

            <!-- Đóng gói: chỉ enable khi đủ serial -->
            <el-tooltip
              v-if="order.status === 'PAID'"
              :content="allSerialsAssigned ? '' : `Cần gán đủ serial (${totalAssigned}/${totalRequired})`"
              placement="top"
              :disabled="allSerialsAssigned"
            >
              <span>
                <el-button
                  type="warning"
                  plain
                  :loading="loading"
                  :disabled="!allSerialsAssigned"
                  @click="markProcessing"
                >
                  Đóng gói
                </el-button>
              </span>
            </el-tooltip>

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
          <el-space direction="vertical" alignment="stretch" :size="16" style="width:100%">
            <!-- ORDER ITEMS TABLE -->
            <el-card shadow="never">
              <template #header>
                <el-space>
                  <el-text tag="b">Sản phẩm đặt mua</el-text>
                  <el-text type="info" size="small">({{ order.items?.length || 0 }} sản phẩm)</el-text>
                </el-space>
              </template>

              <el-table :data="order.items" style="width:100%">
                <el-table-column label="Sản phẩm" min-width="200">
                  <template #default="{ row }">
                    <el-space direction="vertical" alignment="start" :size="0">
                      <el-text tag="b">{{ row.productName }}</el-text>
                      <el-text type="info" size="small" v-if="row.variantName">{{ row.variantName }}</el-text>
                      <el-text type="info" size="small" v-if="row.sku">SKU: {{ row.sku }}</el-text>
                    </el-space>
                  </template>
                </el-table-column>

                <el-table-column label="Đơn giá" width="140" align="right">
                  <template #default="{ row }">
                    <el-text>{{ formatVND(row.unitPrice) }}</el-text>
                  </template>
                </el-table-column>

                <el-table-column label="Số lượng" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag type="info" effect="plain">{{ row.quantity }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="Thành tiền" width="150" align="right">
                  <template #default="{ row }">
                    <el-text tag="b">{{ formatVND(row.quantity * row.unitPrice) }}</el-text>
                  </template>
                </el-table-column>
              </el-table>

              <!-- TOTAL -->
              <el-row justify="end" class="total-section">
                <el-col :span="8">
                  <el-space direction="vertical" alignment="stretch" :size="8" style="width:100%">
                    <el-row justify="space-between">
                      <el-text type="info">Tạm tính</el-text>
                      <el-text>{{ formatVND(order.subtotal) }}</el-text>
                    </el-row>
                    <el-row justify="space-between" v-if="order.discountTotal > 0">
                      <el-text type="info">Giảm giá</el-text>
                      <el-text type="success">-{{ formatVND(order.discountTotal) }}</el-text>
                    </el-row>
                    <el-row justify="space-between">
                      <el-text type="info">Phí vận chuyển</el-text>
                      <el-text>{{ order.shippingFee > 0 ? formatVND(order.shippingFee) : 'Miễn phí' }}</el-text>
                    </el-row>
                    <el-divider class="my-divider"/>
                    <el-row justify="space-between">
                      <el-text tag="b" size="large">Tổng cộng</el-text>
                      <el-text tag="b" size="large">{{ formatVND(order.totalAmount) }}</el-text>
                    </el-row>
                  </el-space>
                </el-col>
              </el-row>
            </el-card>

            <!-- ===================== SERIAL ASSIGNMENT CARD ===================== -->
            <el-card shadow="never" v-if="order.status === 'PAID'" class="serial-card">
              <template #header>
                <el-row justify="space-between" align="middle">
                  <el-space>
                    <el-text tag="b">Gán Serial Number</el-text>
                    <el-tag
                      :type="allSerialsAssigned ? 'success' : 'warning'"
                      effect="light"
                      round
                    >
                      {{ totalAssigned }}/{{ totalRequired }} serial
                    </el-tag>
                  </el-space>
                  <el-text v-if="allSerialsAssigned" type="success" size="small">
                    ✓ Đã đủ serial — có thể đóng gói
                  </el-text>
                  <el-text v-else type="warning" size="small">
                    Scan hoặc nhập serial cho từng sản phẩm
                  </el-text>
                </el-row>
              </template>

              <div
                v-for="(item, idx) in order.items"
                :key="item.id"
                class="serial-row"
              >
                <!-- Item header -->
                <el-row justify="space-between" align="middle" style="margin-bottom:8px">
                  <el-space>
                    <el-text tag="b">{{ item.productName }}</el-text>
                    <el-text type="info" size="small" v-if="item.variantName">
                      ({{ item.variantName }})
                    </el-text>
                  </el-space>
                  <el-tag
                    :type="(item.serialNumbers?.length || 0) >= item.quantity ? 'success' : 'danger'"
                    effect="light"
                    size="small"
                  >
                    {{ item.serialNumbers?.length || 0 }}/{{ item.quantity }} serial
                  </el-tag>
                </el-row>

                <!-- Assigned serials -->
                <el-space wrap style="margin-bottom:10px" v-if="item.serialNumbers?.length">
                  <el-tag
                    v-for="sn in item.serialNumbers"
                    :key="sn"
                    closable
                    type="success"
                    effect="light"
                    :disable-transitions="false"
                    @close="handleRemoveSerial(item, sn)"
                  >
                    {{ sn }}
                  </el-tag>
                </el-space>

                <!-- Input new serial (chỉ hiện khi chưa đủ) -->
                <el-space v-if="(item.serialNumbers?.length || 0) < item.quantity">
                  <el-input
                    v-model="serialInputs[item.id]"
                    :placeholder="`Serial ${(item.serialNumbers?.length || 0) + 1}/${item.quantity} — nhập hoặc scan...`"
                    style="width:320px"
                    size="large"
                    clearable
                    @keyup.enter="handleAssignSerial(item)"
                    :ref="el => { if (el) serialInputRefs[item.id] = el }"
                  />
                  <el-button
                    type="primary"
                    size="large"
                    :loading="assigningItemId === item.id"
                    @click="handleAssignSerial(item)"
                  >
                    Gán
                  </el-button>
                </el-space>
                <el-text v-else type="success" size="small">✓ Đã đủ serial</el-text>

                <el-divider
                  v-if="idx < order.items.length - 1"
                  style="margin:14px 0"
                />
              </div>
            </el-card>
            <!-- ================================================================= -->

          </el-space>
        </el-col>

        <!-- RIGHT COLUMN -->
        <el-col :span="8">
          <el-space direction="vertical" alignment="stretch" :size="16" style="width:100%">
            <!-- ORDER INFO -->
            <el-card shadow="never">
              <template #header>
                <el-text tag="b">Thông tin đơn hàng</el-text>
              </template>
              <el-space direction="vertical" alignment="stretch" :size="12" style="width:100%">
                <el-row justify="space-between">
                  <el-text type="info">Mã đơn hàng</el-text>
                  <el-text>#{{ order.orderNumber }}</el-text>
                </el-row>
                <el-row justify="space-between">
                  <el-text type="info">Kênh bán</el-text>
                  <el-tag size="small" effect="plain">{{ order.channel || '-' }}</el-tag>
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
              <el-space direction="vertical" alignment="stretch" :size="16" style="width:100%">
                <el-space :size="12">
                  <el-avatar :size="42">{{ initials(order.customerName) }}</el-avatar>
                  <el-text tag="b" size="large">{{ order.customerName }}</el-text>
                </el-space>
                <el-space direction="vertical" alignment="stretch" :size="12" style="width:100%">
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

            <!-- SERIAL SUMMARY (khi đã PROCESSING trở lên) -->
            <el-card shadow="never" v-if="order.status !== 'PAID' && hasAnySerials">
              <template #header>
                <el-text tag="b">Serial đã gán</el-text>
              </template>
              <el-space direction="vertical" alignment="stretch" :size="10" style="width:100%">
                <div v-for="item in order.items" :key="item.id">
                  <el-text size="small" type="info">{{ item.productName }}</el-text>
                  <el-space wrap style="margin-top:4px">
                    <el-tag
                      v-for="sn in (item.serialNumbers || [])"
                      :key="sn"
                      type="success"
                      effect="light"
                      size="small"
                    >
                      {{ sn }}
                    </el-tag>
                    <el-text v-if="!item.serialNumbers?.length" type="info" size="small">—</el-text>
                  </el-space>
                </div>
              </el-space>
            </el-card>
          </el-space>
        </el-col>
      </el-row>
    </el-space>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { ElMessage, ElMessageBox } from "element-plus";
import { formatVND, formatDateTime, initials } from "../../utils/format";

const route  = useRoute();
const router = useRouter();
const order  = ref(null);
const loading = ref(false);

// Serial assignment state
const serialInputs    = ref({});   // { [itemId]: string }
const serialInputRefs = ref({});   // { [itemId]: el }
const assigningItemId = ref(null);

// ── Computed ──────────────────────────────────────────────────────────────────

const allSerialsAssigned = computed(() => {
  if (!order.value?.items?.length) return false;
  // OFFLINE: serials gán lúc tạo đơn → không cần gán thêm
  if (order.value.channel === "OFFLINE") return true;
  return order.value.items.every(
    item => (item.serialNumbers?.length || 0) >= item.quantity
  );
});

const totalAssigned = computed(() =>
  order.value?.items?.reduce((s, i) => s + (i.serialNumbers?.length || 0), 0) ?? 0
);

const totalRequired = computed(() =>
  order.value?.items?.reduce((s, i) => s + i.quantity, 0) ?? 0
);

const hasAnySerials = computed(() =>
  order.value?.items?.some(i => i.serialNumbers?.length > 0)
);

// ── Helpers ───────────────────────────────────────────────────────────────────

const statusLabel = (s) => ({
  PAID: "Đã thanh toán",
  PROCESSING: "Đang xử lý",
  SHIPPING: "Đang giao",
  DONE: "Hoàn thành",
  DELIVERED: "Đã giao",
}[s] ?? s);

const statusType = (s) => ({
  PAID: "success",
  PROCESSING: "warning",
  SHIPPING: "primary",
  DONE: "info",
  DELIVERED: "info",
}[s] ?? "info");

const printOrder = () => window.print();

// ── Data loading ──────────────────────────────────────────────────────────────

const load = async () => {
  try {
    const res = await ordersApi.getById(route.params.orderId);
    order.value = res.data;
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "Không thể tải thông tin đơn hàng");
  }
};

// ── Serial actions ────────────────────────────────────────────────────────────

const handleAssignSerial = async (item) => {
  const sn = serialInputs.value[item.id]?.trim();
  if (!sn) {
    ElMessage.warning("Vui lòng nhập serial number");
    return;
  }
  try {
    assigningItemId.value = item.id;
    await ordersApi.assignSerial(order.value.orderId, item.id, sn);
    ElMessage.success(`Đã gán serial: ${sn}`);
    serialInputs.value[item.id] = "";
    await load();
    // Auto-focus lại input của item này nếu vẫn còn cần gán
    const updatedItem = order.value.items.find(i => i.id === item.id);
    if (updatedItem && (updatedItem.serialNumbers?.length || 0) < updatedItem.quantity) {
      setTimeout(() => serialInputRefs.value[item.id]?.focus(), 100);
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "Không thể gán serial");
  } finally {
    assigningItemId.value = null;
  }
};

const handleRemoveSerial = async (item, serialNumber) => {
  try {
    await ElMessageBox.confirm(
      `Bỏ gán serial "${serialNumber}"?`,
      "Xác nhận",
      { type: "warning", confirmButtonText: "Bỏ gán", cancelButtonText: "Hủy" }
    );
    await ordersApi.removeSerial(order.value.orderId, item.id, serialNumber);
    ElMessage.success("Đã bỏ gán serial");
    await load();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.response?.data?.message || "Không thể xóa serial");
    }
  }
};

// ── Order status actions ───────────────────────────────────────────────────────

const markProcessing = async () => {
  if (!allSerialsAssigned.value) {
    ElMessage.warning(`Cần gán đủ serial trước khi đóng gói (${totalAssigned.value}/${totalRequired.value})`);
    return;
  }
  try {
    await ElMessageBox.confirm(
      "Xác nhận đóng gói đơn hàng này?",
      "Xác nhận xử lý",
      { confirmButtonText: "Đồng ý", cancelButtonText: "Hủy", type: "warning" }
    );
    loading.value = true;
    await ordersApi.markAsProcessing(order.value.orderId);
    ElMessage.success("Đã chuyển sang trạng thái Đang xử lý");
    await load();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.response?.data?.message || "Có lỗi xảy ra");
    }
  } finally {
    loading.value = false;
  }
};

const markShipping = async () => {
  try {
    await ElMessageBox.confirm(
      "Chuyển đơn hàng cho đơn vị vận chuyển?",
      "Xác nhận giao vận",
      { confirmButtonText: "Đồng ý", cancelButtonText: "Hủy", type: "primary" }
    );
    loading.value = true;
    await ordersApi.markAsShipping(order.value.orderId);
    ElMessage.success("Đã chuyển sang trạng thái Đang giao hàng");
    await load();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.response?.data?.message || "Có lỗi xảy ra");
    }
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
h2 { margin: 0; }
.icon-spacing { margin-right: 6px; }
.total-section { margin-top: 16px; }
.my-divider { margin: 8px 0; }
.text-right { text-align: right; max-width: 160px; }

/* Serial card */
.serial-card :deep(.el-card__body) { padding: 20px; }
.serial-row { padding: 4px 0; }

@media screen { .print-section { display: none; } }

@media print {
  .no-print { display: none !important; }
  @page { size: A4; margin: 0; }
  .print-section {
    display: block;
    font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
    color: #000;
    padding: 15mm;
  }
  .print-header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #000; padding-bottom: 15px; }
  .print-header h1 { font-size: 24px; margin: 0 0 5px 0; text-transform: uppercase; }
  .print-info { display: flex; justify-content: space-between; margin-bottom: 30px; line-height: 1.5; }
  .print-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }
  .print-table th, .print-table td { padding: 10px; text-align: left; }
  .print-table th { background-color: #f9f9f9 !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; border: 1px solid #000; }
  .print-table td { border-bottom: 1px dashed #ccc; }
  .print-summary { width: 300px; float: right; margin-bottom: 30px; }
  .print-summary-row { display: flex; justify-content: space-between; margin-bottom: 8px; }
  .print-total { font-weight: bold; font-size: 1.2em; border-top: 1px solid #000; padding-top: 8px; margin-top: 8px; }
  .print-footer { clear: both; text-align: center; margin-top: 50px; font-style: italic; }
}
</style>

<style>
@media print {
  .el-aside, .el-header, .el-footer { display: none !important; }
  .el-main { margin-left: 0 !important; padding: 0 !important; overflow: visible !important; height: auto !important; }
  .el-container { height: auto !important; overflow: visible !important; }
  body, html, #app { height: auto !important; overflow: visible !important; background: white !important; }
}
</style>