<template>
  <div class="sales-return">
    <!-- ══════════ HEADER ══════════ -->
    <el-row align="middle" justify="space-between" class="header">
      <el-space :size="12">
        <el-tooltip content="Quay lại tìm đơn khác" placement="bottom">
          <el-button v-if="order" :icon="ArrowLeft" @click="reset" link>Tìm đơn khác</el-button>
        </el-tooltip>
        <el-space direction="vertical" :size="0">
          <el-text tag="b" size="large">TRẢ HÀNG TẠI QUẦY</el-text>
          <el-text type="info" size="small">
            <span v-if="order">
              Đang xem: <strong>{{ order.orderNumber || `#${order.id}` }}</strong>
              <el-tag size="small" :type="orderStatusType(order.status)" plain class="ml-8">{{ order.status }}</el-tag>
            </span>
            <span v-else>Xử lý đổi trả và hoàn tiền khách hàng</span>
          </el-text>
        </el-space>
      </el-space>
      <el-tooltip v-if="order" content="Tải lại thông tin đơn" placement="left">
        <el-button :icon="Refresh" circle @click="handleSearch" plain />
      </el-tooltip>
    </el-row>

    <!-- ══════════ STEP PROGRESS ══════════ -->
    <el-steps v-if="order" :active="currentStep" finish-status="success" class="steps-bar">
      <el-step title="Tìm đơn" description="Nhập mã đơn" />
      <el-step title="Chọn hàng trả" description="Chọn sản phẩm + lý do" />
      <el-step title="Thẩm định" description="SALE/Admin duyệt" />
      <el-step title="Hoàn tất" description="Hoàn tiền khách" />
    </el-steps>

    <!-- ══════════ SEARCH SECTION ══════════ -->
    <el-card v-if="!order" shadow="never">
      <el-space direction="vertical" fill :size="20" class="search-wrap">
        <el-space direction="vertical" :size="4" align="center">
          <el-text tag="h2" style="margin: 0">Bắt đầu xử lý trả hàng</el-text>
          <el-text type="info">Nhập mã đơn hàng (ID số hoặc mã ORD-xxx) để tra cứu</el-text>
        </el-space>
        <el-input
          v-model="searchId"
          placeholder="Nhập Order ID hoặc Order Number..."
          @keyup.enter="handleSearch"
          size="large"
          style="max-width: 520px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
          <template #append>
            <el-button @click="handleSearch" :loading="loading" plain>KIỂM TRA</el-button>
          </template>
        </el-input>
      </el-space>
    </el-card>

    <!-- ══════════ WORKFLOW ══════════ -->
    <div v-if="order">
      <el-row :gutter="20">
        <!-- ─── LEFT COL: Order Info & Items ─── -->
        <el-col :span="16">

          <!-- Order Overview -->
          <el-card shadow="never" class="mb-20">
            <template #header>
              <el-text tag="b">Thông tin đơn hàng</el-text>
            </template>
            <el-descriptions :column="3" size="small" border>
              <el-descriptions-item label="Mã đơn">
                <el-text tag="b">{{ order.orderNumber || `#${order.id}` }}</el-text>
              </el-descriptions-item>
              <el-descriptions-item label="Tổng tiền">
                <el-text type="primary" tag="b">{{ formatMoney(order.totalAmount) }}</el-text>
              </el-descriptions-item>
              <el-descriptions-item label="Thanh toán">
                <el-tag :type="order.paymentStatus === 'PAID' ? 'success' : 'warning'" plain size="small">
                  {{ order.paymentStatus || '—' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Ngày tạo">{{ formatDate(order.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="Kênh">{{ order.channel || '—' }}</el-descriptions-item>
              <el-descriptions-item label="Phương thức TT">{{ order.paymentMethod || '—' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- Status Alert -->
          <el-alert
            v-if="existingReturn?.status === 'COMPLETED'"
            title="ĐÃ HOÀN TẤT TRẢ HÀNG"
            type="success"
            description="Hệ thống đã ghi nhận hoàn tiền. Không cần xử lý thêm."
            show-icon :closable="false" class="mb-20"
          />
          <el-alert
            v-else-if="existingReturn?.status === 'PENDING'"
            title="CÓ YÊU CẦU ĐANG CHỜ DUYỆT"
            type="warning"
            description="Đang thẩm định. Kiểm tra panel bên phải để duyệt hoặc từ chối."
            show-icon :closable="false" class="mb-20"
          />
          <el-alert
            v-else-if="existingReturn?.status === 'REJECTED'"
            title="YÊU CẦU TRƯỚC ĐÃ BỊ TỪ CHỐI"
            type="error"
            :description="'Lý do: ' + (existingReturn.note || '—')"
            show-icon :closable="false" class="mb-20"
          />
          <!-- Đơn chưa DELIVERED → không đủ điều kiện trả hàng -->
          <el-alert
            v-else-if="!existingReturn && order?.status !== 'DELIVERED'"
            title="CHƯA ĐỦ ĐIỀU KIỆN TRẢ HÀNG"
            type="warning"
            :description="`Trạng thái đơn hiện tại: ${order?.status}. Chỉ có thể trả hàng sau khi đơn đã DELIVERED.`"
            show-icon :closable="false" class="mb-20"
          />
          <!-- Đơn DELIVERED, chưa có return → sẵn sàng -->
          <el-alert
            v-else-if="!existingReturn && order?.status === 'DELIVERED'"
            title="SẴN SÀNG TẠO YÊU CẦU"
            type="info"
            description="Đơn hàng đủ điều kiện. Chọn sản phẩm cần trả bên dưới."
            show-icon :closable="false" class="mb-20"
          />

          <!-- Items Table -->
          <el-card shadow="never">
            <template #header>
              <el-row justify="space-between" align="middle">
                <el-text tag="b">
                  Danh sách sản phẩm
                  <el-tag size="small" plain>{{ (order.items || []).length }} món</el-tag>
                </el-text>
                <el-text v-if="selectedItems.length > 0" type="warning" size="small">
                  Đang chọn {{ selectedItems.length }} / {{ (order.items || []).length }} sản phẩm
                </el-text>
              </el-row>
            </template>

            <el-empty v-if="!(order.items || []).length" description="Không có sản phẩm trong đơn hàng" />

            <el-table
              v-else
              :data="order.items || []"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="50" :selectable="canSelectItem" />

              <el-table-column label="Sản phẩm" min-width="200">
                <template #default="{ row }">
                  <el-space>
                    <el-image :src="row.imageUrl" class="product-thumb" fit="cover" />
                    <el-space direction="vertical" :size="0" align="start">
                      <el-text tag="b">{{ row.productName }}</el-text>
                      <el-text size="small" type="info">{{ row.variantName }}</el-text>
                    </el-space>
                  </el-space>
                </template>
              </el-table-column>

              <el-table-column label="SL mua" width="80" align="center" prop="quantity" />

              <el-table-column label="Đơn giá" width="120" align="right">
                <template #default="{ row }">{{ formatMoney(row.price) }}</template>
              </el-table-column>
<el-table-column label="Serial" min-width="160">
  <template #default="{ row }">
    <div v-if="row.serialNumbers && row.serialNumbers.length > 0">
      <el-tag
        v-for="sn in row.serialNumbers"
        :key="sn"
        size="small"
        type="info"
        effect="plain"
        style="margin: 2px; font-family: monospace; font-size: 11px;"
      >
        {{ sn }}
      </el-tag>
    </div>
    <el-text v-else type="info" size="small">—</el-text>
  </template>
</el-table-column>
              <!-- Quantity to return — only show when creating new return -->
              <el-table-column v-if="canCreateReturn" label="SL trả" width="120" align="center">
                <template #default="{ row }">
                  <el-input-number
                    v-if="isSelected(row)"
                    v-model="returnQuantities[row.id]"
                    :min="1"
                    :max="row.quantity"
                    size="small"
                    controls-position="right"
                    style="width: 90px"
                  />
                  <el-text v-else type="info" size="small">—</el-text>
                </template>
              </el-table-column>

              <el-table-column label="Thành tiền" width="130" align="right">
                <template #default="{ row }">{{ formatMoney(row.price * row.quantity) }}</template>
              </el-table-column>
            </el-table>

            <!-- Return Creation Form -->
            <div v-if="canCreateReturn" class="footer-actions">
              <el-divider />

              <!-- Live refund estimate -->
              <el-row justify="space-between" align="middle" class="mb-10" v-if="selectedItems.length > 0">
                <el-text size="small" type="info">
                  {{ selectedItems.length }} sản phẩm được chọn
                </el-text>
                <el-space>
                  <el-text size="small" type="info">Ước tính hoàn:</el-text>
                  <el-text type="danger" tag="b" size="large">{{ formatMoney(estimatedRefund) }}</el-text>
                </el-space>
              </el-row>

              <el-form label-position="top">
                <el-form-item
                  label="Lý do trả hàng"
                  :error="reasonError"
                  required
                >
                  <el-input
                    v-model="returnReason"
                    type="textarea"
                    :rows="2"
                    placeholder="Nhập lý do trả hàng... (bắt buộc)"
                    @input="reasonError = ''"
                    :class="{ 'is-error': reasonError }"
                  />
                </el-form-item>
              </el-form>

              <el-button
                type="warning"
                size="large"
                style="width: 100%; margin-top: 10px"
                :disabled="selectedItems.length === 0"
                :loading="submitting"
                @click="initReturn"
                plain
              >
                KHỞI TẠO YÊU CẦU TRẢ HÀNG
                {{ selectedItems.length > 0 ? `(${selectedItems.length} SP — ${formatMoney(estimatedRefund)})` : '' }}
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- ─── RIGHT COL: Customer + Action Panels ─── -->
        <el-col :span="8">

          <!-- Customer -->
          <el-card shadow="never" class="mb-20">
            <template #header><el-text tag="b">Khách hàng</el-text></template>
            <el-space>
              <el-avatar :size="40">{{ initials(order.customerName) }}</el-avatar>
              <el-space direction="vertical" :size="0" align="start">
                <el-text tag="b">{{ order.customerName || '—' }}</el-text>
                <el-text type="info" size="small">{{ order.customerPhone || '—' }}</el-text>
              </el-space>
            </el-space>
          </el-card>

          <!-- REJECTED -->
          <el-card v-if="existingReturn?.status === 'REJECTED'" shadow="never" class="mb-20">
            <template #header>
              <el-row justify="space-between" align="middle">
                <el-text tag="b">KẾT QUẢ XỬ LÝ</el-text>
                <el-tag type="danger" plain>TỪ CHỐI</el-tag>
              </el-row>
            </template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="Lý do từ chối">{{ existingReturn.note || '—' }}</el-descriptions-item>
              <el-descriptions-item label="Người xử lý">{{ existingReturn.processedByName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="Thời gian">{{ formatDate(existingReturn.createdAt) }}</el-descriptions-item>
            </el-descriptions>
            <el-button
              type="warning"
              style="width: 100%; margin-top: 16px"
              @click="existingReturn = null"
              plain
            >
              Tạo yêu cầu mới
            </el-button>
          </el-card>

          <!-- PENDING Appraisal -->
          <el-card v-if="existingReturn?.status === 'PENDING'" shadow="never" class="mb-20">
            <template #header>
              <el-row justify="space-between" align="middle">
                <el-text tag="b">XỬ LÝ THẨM ĐỊNH</el-text>
                <el-tag type="warning" plain>CHỜ DUYỆT</el-tag>
              </el-row>
            </template>

            <el-skeleton v-if="!returnDetail" :rows="5" animated />
            <div v-else>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="Sản phẩm">{{ returnDetail.productName }}</el-descriptions-item>
                <el-descriptions-item label="Số lượng">{{ returnDetail.quantity }}</el-descriptions-item>
                <el-descriptions-item label="Lý do">"{{ returnDetail.reason }}"</el-descriptions-item>
                <el-descriptions-item label="Hoàn tiền dự kiến">
                  <el-text type="danger" tag="b">{{ formatMoney(returnDetail.refundAmount) }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item v-if="returnDetail.refundMethod" label="Phương thức hoàn">
                  {{ returnDetail.refundMethod }}
                </el-descriptions-item>
                <el-descriptions-item v-if="returnDetail.refundStatus" label="Trạng thái hoàn">
                  {{ returnDetail.refundStatus }}
                </el-descriptions-item>
                <el-descriptions-item v-if="returnDetail.note" label="Ghi chú">
                  {{ returnDetail.note }}
                </el-descriptions-item>
                <el-descriptions-item label="Ngày yêu cầu">
                  {{ formatDate(returnDetail.createdAt) }}
                </el-descriptions-item>
              </el-descriptions>

              <el-divider />
              <el-text size="small" type="info" class="mb-10" block>Hành động thẩm định:</el-text>
              <el-button
                type="success"
                size="large"
                style="width: 100%"
                @click="confirmApprove"
                :loading="submitting"
                plain
              >
                ✓ DUYỆT YÊU CẦU
              </el-button>
              <el-button
                type="danger"
                link
                style="width: 100%; margin-top: 8px"
                @click="showRejectPrompt"
              >
                Từ chối yêu cầu
              </el-button>
            </div>
          </el-card>

          <!-- COMPLETED -->
          <el-card v-if="existingReturn?.status === 'COMPLETED'" shadow="never">
            <el-result icon="success" title="Hoàn tất trả hàng">
              <template #sub-title>
                <el-descriptions :column="1" border size="small" class="mt-10">
                  <el-descriptions-item v-if="returnDetail?.refundAmount" label="Đã hoàn tiền">
                    <el-text type="success" tag="b">{{ formatMoney(returnDetail.refundAmount) }}</el-text>
                  </el-descriptions-item>
                  <el-descriptions-item v-if="returnDetail?.refundMethod" label="Phương thức">
                    {{ returnDetail.refundMethod }}
                  </el-descriptions-item>
                  <el-descriptions-item v-if="returnDetail?.processedByName" label="Người duyệt">
                    {{ returnDetail.processedByName }}
                  </el-descriptions-item>
                  <el-descriptions-item v-if="returnDetail?.refundedAt" label="Ngày hoàn tiền">
                    {{ formatDate(returnDetail.refundedAt) }}
                  </el-descriptions-item>
                </el-descriptions>
              </template>
              <template #extra>
                <el-button @click="reset" plain>Xử lý đơn khác</el-button>
              </template>
            </el-result>
          </el-card>

        </el-col>
      </el-row>
    </div>

    <!-- ══════════ REJECT DIALOG ══════════ -->
    <el-dialog v-model="rejectDialog.show" title="Từ chối yêu cầu trả hàng" width="420px">
      <el-alert
        title="Hành động này sẽ từ chối toàn bộ yêu cầu"
        type="warning"
        show-icon :closable="false"
        class="mb-20"
      />
      <el-form label-position="top">
        <el-form-item label="Lý do từ chối (bắt buộc)">
          <el-input
            v-model="rejectDialog.reason"
            type="textarea"
            :rows="3"
            placeholder="Nhập lý do rõ ràng để thông báo cho khách hàng..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog.show = false" plain>Hủy</el-button>
        <el-button
          type="danger"
          @click="confirmReject"
          :loading="submitting"
          :disabled="!rejectDialog.reason.trim()"
          plain
        >
          Xác nhận từ chối
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import { Search, Refresh, ArrowLeft } from '@element-plus/icons-vue';
import { ordersApi } from '../../api/orders.api';
import { returnsApi } from '../../api/returns.api';
import { toast } from '../../ui/toast';
import { confirmModal } from '../../ui/confirm';

// ── State ────────────────────────────────────────────────────────────
const searchId = ref('');
const loading = ref(false);
const submitting = ref(false);
const order = ref(null);
const existingReturn = ref(null);
const returnDetail = ref(null);
const selectedItems = ref([]);
const returnReason = ref('');
const reasonError = ref('');
const returnQuantities = reactive({}); // { itemId: quantity }
const rejectDialog = reactive({ show: false, reason: '' });

// ── Computed ─────────────────────────────────────────────────────────

/** Bước hiện tại trong luồng — kết hợp cả existingReturn.status và order.status */
const currentStep = computed(() => {
  if (!order.value) return 0;
  const retStatus = existingReturn.value?.status;
  const ordStatus = order.value?.status;

  // Ưu tiên theo return status (chính xác hơn)
  if (retStatus === 'COMPLETED') return 3;
  if (retStatus === 'PENDING')   return 2;

  // Fallback: nhìn vào order status khi existingReturn chưa load hoặc null
  if (ordStatus === 'RETURNED' || ordStatus === 'PARTIALLY_RETURNED') return 3;
  if (ordStatus === 'RETURN_REQUESTED') return 2;

  return 1; // NONE hoặc REJECTED → bước chọn hàng
});

/** Có thể tạo yêu cầu mới không:
 * - Không có yêu cầu hiện tại (hoặc REJECTED)
 * - Đơn đã DELIVERED (backend bắt buộc)
 */
const canCreateReturn = computed(() => {
  const noActiveReturn = !existingReturn.value || existingReturn.value.status === 'REJECTED';
  const isDelivered = order.value?.status === 'DELIVERED';
  return order.value && noActiveReturn && isDelivered;
});

/** Tổng tiền hoàn dự kiến */
const estimatedRefund = computed(() => {
  if (!order.value?.items) return 0;
  return selectedItems.value.reduce((sum, item) => {
    const k = getItemKey(item);
    const qty = (k != null ? returnQuantities[k] : null) ?? item.quantity;
    return sum + (item.price ?? 0) * qty;
  }, 0);
});

// ── Helpers ───────────────────────────────────────────────────────────
function isSelected(row) {
  return selectedItems.value.some(i => i.id === row.id);
}
const formatMoney = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);
const formatDate = (d) => d ? new Date(d).toLocaleString('vi-VN') : '—';
const orderStatusType = (s) => ({ PENDING: 'warning', PAID: 'primary', PROCESSING: '', SHIPPING: 'primary', DELIVERED: 'success', CANCELLED: 'danger' }[s] || 'info');
const initials = (name = '') => (name || '').split(' ').map(n => n[0]).filter(Boolean).join('').toUpperCase().slice(0, 2) || '?';

// ── Helpers for item ID ───────────────────────────────────────────────
/** Lấy ID thực sự của order item — hỗ trợ nhiều field name từ backend */
function getItemKey(item) {
  return item.id ?? item.orderItemId ?? item.itemId ?? null;
}

async function handleSearch() {
  if (!searchId.value.trim()) return;
  loading.value = true;
  order.value = null;
  existingReturn.value = null;
  returnDetail.value = null;
  selectedItems.value = [];
  returnReason.value = '';
  reasonError.value = '';
  Object.keys(returnQuantities).forEach(k => delete returnQuantities[k]);

  try {
    const input = searchId.value.trim();
    const isNumeric = /^\d+$/.test(input);
    const isOrderNumber = input.toUpperCase().startsWith('ORD-');
    const isSerial = !isNumeric && !isOrderNumber;

    let orderData = null;

    if (isNumeric) {
      const res = await ordersApi.getById(input);
      orderData = res?.data?.data ?? res?.data ?? null;

    } else if (isOrderNumber) {
      try {
        const res = await ordersApi.getByOrderNumber(input);
        const candidate = res?.data?.data ?? res?.data ?? null;
        if (candidate) {
          candidate.id = candidate.id ?? candidate.orderId;
          orderData = candidate;
        }
      } catch {
        const res = await ordersApi.filter(null, null, null, null, null);
        const raw = res?.data?.data ?? res?.data ?? [];
        const list = raw?.content ?? (Array.isArray(raw) ? raw : []);
        const found = list.find(o =>
          (o.orderNumber || '').toLowerCase() === input.toLowerCase()
        ) ?? null;
        if (found?.id) {
          const detailRes = await ordersApi.getById(found.id);
          orderData = detailRes?.data?.data ?? detailRes?.data ?? found;
        }
      }

    } else if (isSerial) {
      try {
        const res = await ordersApi.findBySerial(input);
        const candidate = res?.data?.data ?? res?.data ?? null;
        if (candidate) {
          candidate.id = candidate.id ?? candidate.orderId;
          orderData = candidate;
          toast(`Tìm thấy đơn hàng từ serial: ${input}`, 'info');
        }
      } catch (e) {
        if (e?.response?.status === 404) {
          throw new Error(`Serial "${input}" chưa được gán với đơn hàng nào`);
        }
        throw new Error(`Không tìm thấy serial: "${input}"`);
      }
    }

    // ... phần còn lại giữ nguyên

    if (!orderData) throw new Error(`Không tìm thấy đơn hàng: "${input}"`);

    const orderId = orderData.id ?? orderData.orderId;
    if (!orderId) throw new Error('Đơn hàng không có ID hợp lệ');
    orderData.id = orderId;
    order.value = orderData;

    // Init quantities map
    (orderData.items || []).forEach(item => {
      const k = getItemKey(item);
      if (k != null) returnQuantities[k] = item.quantity;
    });

    // Check existing return
    try {
      const retRes = await returnsApi.getByOrder(orderId);
      const retData = retRes?.data?.data ?? retRes?.data ?? null;
      const singleReturn = Array.isArray(retData)
        ? retData.sort((a, b) => b.id - a.id)[0]
        : retData;
      existingReturn.value = singleReturn || null;
      if (singleReturn?.status === 'PENDING')   fetchReturnDetail(singleReturn.id);
      if (singleReturn?.status === 'COMPLETED') fetchReturnDetail(singleReturn.id);
    } catch (retErr) {
      if (retErr?.response?.status !== 404) {
        toast('Không thể kiểm tra trạng thái trả hàng', 'warning');
      }
      existingReturn.value = null;
    }

  } catch (err) {
    toast(err.response?.data?.message || err.message || 'Lỗi hệ thống', 'error');
    order.value = null;
  } finally {
    loading.value = false;
  }
}

// ── Create Return ─────────────────────────────────────────────────────
async function initReturn() {
  if (selectedItems.value.length === 0) return;
  if (!returnReason.value.trim()) {
    reasonError.value = 'Vui lòng nhập lý do trả hàng';
    return;
  }

  // Kiểm tra đơn hàng đã được giao (DELIVERED) — backend bắt buộc
  if (order.value?.status !== 'DELIVERED') {
    toast('Chỉ có thể tạo yêu cầu trả hàng khi đơn đã giao (DELIVERED)', 'warning');
    return;
  }

  // Kiểm tra items có ID hợp lệ
  const invalidItems = selectedItems.value.filter(i => !getItemKey(i));
  if (invalidItems.length > 0) {
    toast(`Không xác định được mã sản phẩm: ${invalidItems.map(i => i.productName).join(', ')}`, 'error');
    return;
  }

  // Confirm dialog
  const summary = selectedItems.value
    .map(i => {
      const k = getItemKey(i);
      const qty = (k != null ? returnQuantities[k] : null) ?? i.quantity;
      return `• ${i.productName} × ${qty} — ${formatMoney((i.price ?? 0) * qty)}`;
    })
    .join('\n');
  const ok = await confirmModal(
    `Xác nhận tạo yêu cầu trả hàng?\n\n${summary}\n\nTổng hoàn dự kiến: ${formatMoney(estimatedRefund.value)}`,
    'Khởi tạo trả hàng'
  );
  if (!ok) return;

  submitting.value = true;
  let successCount = 0;
  let lastError = null;

  try {
    // Backend nhận 1 item/request (đối chiếu CreateReturnRequest DTO)
    // Nếu nhiều items được chọn → gọi API nhiều lần tuần tự
    for (const item of selectedItems.value) {
      const k = getItemKey(item);
      const qty = (k != null ? returnQuantities[k] : null) ?? item.quantity;
      const refundAmount = (item.price ?? 0) * qty;

      const payload = {
        orderId: order.value.id,
        orderItemId: k,            // Long — ID của OrderItem
        quantity: qty,             // Integer
        reason: returnReason.value.trim(),
        refundAmount: refundAmount // BigDecimal
      };

      try {
        await returnsApi.create(payload);
        successCount++;
      } catch (itemErr) {
        lastError = itemErr;
        console.error('Lỗi tạo yêu cầu cho item', item.productName, itemErr?.response?.data);
      }
    }

    if (successCount > 0) {
      toast(
        successCount === selectedItems.value.length
          ? 'Đã khởi tạo yêu cầu thành công'
          : `Đã tạo ${successCount}/${selectedItems.value.length} yêu cầu. Có lỗi xảy ra.`,
        successCount === selectedItems.value.length ? 'success' : 'warning'
      );
      if (searchId.value) handleSearch();
    } else {
      toast(lastError?.response?.data?.message || 'Tất cả yêu cầu đều thất bại', 'error');
    }
  } finally {
    submitting.value = false;
  }
}

// ── Fetch Detail ──────────────────────────────────────────────────────
async function fetchReturnDetail(id) {
  try {
    const res = await returnsApi.getDetail(id);
    returnDetail.value = res?.data?.data || res?.data;
  } catch {
    toast('Không thể tải chi tiết yêu cầu', 'error');
  }
}

// ── Approve ───────────────────────────────────────────────────────────
async function confirmApprove() {
  const ok = await confirmModal('Xác nhận DUYỆT yêu cầu trả hàng này?', 'Duyệt trả hàng');
  if (!ok) return;
  submitting.value = true;
  try {
    await returnsApi.approve(existingReturn.value.id);
    toast('Đã phê duyệt thành công', 'success');
    if (searchId.value) handleSearch();
  } catch (err) {
    toast(err.response?.data?.message || 'Lỗi khi duyệt', 'error');
  } finally {
    submitting.value = false;
  }
}

// ── Reject ────────────────────────────────────────────────────────────
function showRejectPrompt() {
  rejectDialog.reason = '';
  rejectDialog.show = true;
}

async function confirmReject() {
  if (!rejectDialog.reason.trim()) return;
  submitting.value = true;
  try {
    await returnsApi.reject(existingReturn.value.id, rejectDialog.reason);
    toast('Đã từ chối yêu cầu', 'info');
    rejectDialog.show = false;
    if (searchId.value) handleSearch();
  } catch (err) {
    toast(err.response?.data?.message || 'Lỗi khi từ chối', 'error');
  } finally {
    submitting.value = false;
  }
}

// ── Misc ──────────────────────────────────────────────────────────────
function handleSelectionChange(val) {
  selectedItems.value = val;
  // Init quantity cho items mới được chọn
  val.forEach(item => {
    const key = getItemKey(item);
    if (key !== null && returnQuantities[key] === undefined) {
      returnQuantities[key] = item.quantity;
    }
  });
}

function canSelectItem(row) {
  return !existingReturn.value || existingReturn.value.status === 'REJECTED';
}

function reset() {
  order.value = null;
  existingReturn.value = null;
  returnDetail.value = null;
  selectedItems.value = [];
  returnReason.value = '';
  reasonError.value = '';
  searchId.value = '';
  Object.keys(returnQuantities).forEach(k => delete returnQuantities[k]);
}
</script>

<style scoped>
.sales-return { padding: 20px; min-height: 100vh; background: #f5f7fa; }
.header { padding: 10px 0; margin-bottom: 16px; }
.steps-bar { margin-bottom: 24px; background: #fff; padding: 20px 24px; border-radius: 4px; border: 1px solid #dcdfe6; }
.search-wrap { text-align: center; padding: 60px 40px; }
.mb-20 { margin-bottom: 20px; }
.mb-10 { margin-bottom: 10px; }
.mt-10 { margin-top: 10px; }
.ml-8 { margin-left: 8px; }
.product-thumb { width: 40px; height: 40px; flex-shrink: 0; }
.footer-actions { margin-top: 24px; }
</style>
