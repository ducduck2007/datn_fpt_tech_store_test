<template>
  <div class="promo-wrap">
    <!-- Page header -->
    <div class="promo-header">
      <div>
        <div class="promo-kicker">Admin · Promotions</div>
        <div class="promo-title">Quản lý khuyến mãi</div>
      </div>
      <div class="promo-header-actions">
        <el-switch
          v-model="activeOnly"
          active-text="Active only"
          @change="load"
        />
        <el-button @click="load" :loading="loading" plain>Reload</el-button>
        <el-button @click="loadConflicts" :loading="conflictLoading" plain
          >⚠️ Conflicts</el-button
        >
        <el-button @click="loadExpiring" :loading="expiringLoading" plain>
          ⏰ Sắp hết hạn
        </el-button>
        <el-button @click="loadReport" :loading="reportLoading" plain>
          📊 Báo cáo
        </el-button>
        <el-button type="primary" @click="openCreate">+ Tạo mới</el-button>
      </div>
    </div>

    <!-- Conflicts panel -->
    <transition name="slide-down">
      <div v-if="conflicts !== null" class="promo-conflicts">
        <el-alert
          v-if="conflicts.length === 0"
          title="Không có xung đột khuyến mãi"
          type="success"
          show-icon
          :closable="true"
          @close="conflicts = null"
        />
        <template v-else>
          <div class="promo-section-label">
            ⚠️ Xung đột khuyến mãi
            <el-tag type="danger" size="small" round>{{
              conflicts.length
            }}</el-tag>
            <el-button
              text
              size="small"
              @click="conflicts = null"
              style="margin-left: auto"
              >Ẩn</el-button
            >
          </div>
          <el-table :data="conflicts" border size="small" class="promo-table">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="code" label="Code" width="140" />
            <el-table-column prop="name" label="Name" min-width="200" />
            <el-table-column
              prop="conflictReason"
              label="Lý do xung đột"
              min-width="200"
            />
          </el-table>
        </template>
      </div>
    </transition>

    <!-- Main card -->
    <div class="promo-card">
      <el-table :data="rows" border :loading="loading" class="promo-table">
        <el-table-column prop="id" label="ID" width="75" />
        <el-table-column prop="code" label="Code" width="140">
          <template #default="{ row }">
            <span class="promo-code">{{ row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Tên chiến dịch" min-width="200" />
        <el-table-column label="Giảm giá" width="120">
          <template #default="{ row }">
            <span class="promo-discount">
              {{ row.discountValue
              }}{{ row.discountType === "PERCENT" ? "%" : " ₫" }}
            </span>
            <div class="promo-type-badge">{{ row.discountType }}</div>
          </template>
        </el-table-column>
        <el-table-column
          prop="priority"
          label="Ưu tiên"
          width="85"
          align="center"
        />
        <el-table-column label="Thời gian" width="230">
          <template #default="{ row }">
            <div class="promo-date-range">
              <span>{{ fmtDate(row.startDate) }}</span>
              <span class="promo-date-sep">→</span>
              <span>{{ fmtDate(row.endDate) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="isActive"
          label="Trạng thái"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.isActive ? 'success' : 'info'"
              effect="light"
              size="small"
            >
              {{ row.isActive ? "Đang chạy" : "Tắt" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Thao tác" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">Sửa</el-button>
            <el-button
              size="small"
              type="primary"
              plain
              @click="openPreview(row)"
              >Preview</el-button
            >
            <el-button size="small" type="danger" plain @click="remove(row)"
              >Xóa</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Preview panel -->
    <transition name="slide-down">
      <div v-if="previewPromo" class="promo-card mt-3">
        <div class="promo-preview-head">
          <div>
            <div class="promo-section-label" style="margin-bottom: 2px">
              Xem giá chiến dịch:
              <el-tag type="warning" size="small">{{
                previewPromo.code
              }}</el-tag>
            </div>
            <div class="promo-endpoint">
              GET /api/prices/products/{productId}
            </div>
          </div>
          <div class="promo-preview-inputs">
            <el-input
              v-model.number="previewProductId"
              placeholder="Product ID"
              style="width: 130px"
            />
            <el-input
              v-model.number="previewCustomerId"
              placeholder="Customer ID (tùy chọn)"
              style="width: 180px"
            />
            <el-button
              type="primary"
              :loading="previewLoading"
              @click="loadPreviewPrices"
              >Tải giá</el-button
            >
            <el-button
              @click="
                previewPromo = null;
                previewPrices = [];
              "
              plain
              >Đóng</el-button
            >
          </div>
        </div>
        <el-table
          v-if="previewPrices.length"
          :data="previewPrices"
          border
          class="promo-table mt-3"
          size="small"
        >
          <el-table-column prop="variantId" label="Variant ID" width="90" />
          <el-table-column
            prop="variantName"
            label="Tên variant"
            min-width="160"
          />
          <el-table-column prop="sku" label="SKU" width="150" />
          <el-table-column prop="currencyCode" label="Currency" width="90" />
          <el-table-column label="Giá gốc" width="130">
            <template #default="{ row }">{{ fmtMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column label="Giá sau KM" width="140">
            <template #default="{ row }">
              <span
                :class="
                  row.finalPrice < row.price ? 'promo-price-discount' : ''
                "
              >
                {{ fmtMoney(row.finalPrice) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="Mã KM áp dụng" width="140">
            <template #default="{ row }">
              <el-tag v-if="row.promotionCode" type="warning" size="small">{{
                row.promotionCode
              }}</el-tag>
              <span v-else class="promo-muted">—</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty
          v-else-if="previewLoaded"
          description="Không có dữ liệu"
          :image-size="60"
        />
      </div>
    </transition>

    <!-- Expiring panel -->
    <transition name="slide-down">
      <div v-if="expiringRows !== null" class="promo-conflicts">
        <div class="promo-section-label">
          ⏰ Khuyến mãi sắp hết hạn (trong 3 ngày)
          <el-tag
            :type="expiringRows.length ? 'warning' : 'success'"
            size="small"
            round
          >
            {{ expiringRows.length }}
          </el-tag>
          <el-button
            text
            size="small"
            @click="expiringRows = null"
            style="margin-left: auto"
            >Ẩn</el-button
          >
        </div>
        <el-alert
          v-if="expiringRows.length === 0"
          title="Không có khuyến mãi nào sắp hết hạn"
          type="success"
          show-icon
          :closable="false"
        />
        <el-table
          v-else
          :data="expiringRows"
          border
          size="small"
          class="promo-table"
        >
          <el-table-column prop="id" label="ID" width="75" />
          <el-table-column prop="code" label="Code" width="140">
            <template #default="{ row }"
              ><span class="promo-code">{{ row.code }}</span></template
            >
          </el-table-column>
          <el-table-column prop="name" label="Tên" min-width="200" />
          <el-table-column label="Hết hạn" width="170">
            <template #default="{ row }">
              <el-tag type="warning" size="small">{{
                fmtDate(row.endDate)
              }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </transition>

    <!-- Report panel -->
    <transition name="slide-down">
      <div v-if="reportData !== null" class="promo-card mt-3">
        <div class="promo-section-label">
          📊 Báo cáo khuyến mãi
          <el-select
            v-model="reportPeriod"
            size="small"
            @change="loadReport"
            style="width: 100px; margin-left: 8px"
          >
            <el-option label="Tuần" value="week" />
            <el-option label="Tháng" value="month" />
          </el-select>
          <el-button
            text
            size="small"
            @click="reportData = null"
            style="margin-left: auto"
            >Ẩn</el-button
          >
        </div>
        <div class="promo-report-grid mt-2">
          <div class="promo-stat-card">
            <div class="promo-stat-value">{{ reportData.total ?? 0 }}</div>
            <div class="promo-stat-label">Tổng KM</div>
          </div>
          <div class="promo-stat-card">
            <div class="promo-stat-value text-success">
              {{ reportData.activeCount ?? 0 }}
            </div>
            <div class="promo-stat-label">Đang chạy</div>
          </div>
          <div class="promo-stat-card">
            <div class="promo-stat-value text-warning">
              {{ reportData.comboCount ?? 0 }}
            </div>
            <div class="promo-stat-label">Combo</div>
          </div>
          <div class="promo-stat-card">
            <div class="promo-stat-value text-danger">
              {{ reportData.totalRedemptions ?? 0 }}
            </div>
            <div class="promo-stat-label">Lượt dùng</div>
          </div>
        </div>
      </div>
    </transition>

    <!-- Create / Update dialog -->
    <el-dialog
      v-model="dlg.open"
      :title="dlg.mode === 'create' ? '+ Tạo khuyến mãi' : 'Sửa khuyến mãi'"
      width="860px"
      align-center
    >
      <el-alert
        v-if="dlg.alert"
        :title="dlg.alert"
        type="error"
        show-icon
        :closable="false"
        class="mb-3"
      />

      <el-form :model="dlg.form" label-position="top">
        <div class="dlg-grid-4">
          <el-form-item label="Code" style="grid-column: span 1">
            <el-input v-model="dlg.form.code" :disabled="dlg.mode === 'edit'" />
          </el-form-item>
          <el-form-item label="Tên chiến dịch" style="grid-column: span 3">
            <el-input v-model="dlg.form.name" />
          </el-form-item>
        </div>

        <el-form-item label="Mô tả">
          <el-input v-model="dlg.form.description" type="textarea" :rows="2" />
        </el-form-item>

        <div class="dlg-grid-3">
          <el-form-item label="Loại giảm giá">
            <el-select v-model="dlg.form.discountType" class="w-100">
              <el-option label="PERCENT (%)" value="PERCENT" />
              <el-option label="FIXED (₫)" value="FIXED" />
            </el-select>
          </el-form-item>
          <el-form-item label="Giá trị giảm">
            <el-input-number
              v-model="dlg.form.discountValue"
              :min="0"
              :max="999999999"
              class="w-100"
              :controls="false"
            />
          </el-form-item>
          <el-form-item label="Ưu tiên (priority)">
            <el-input-number
              v-model="dlg.form.priority"
              :min="0"
              :max="9999"
              class="w-100"
              :controls="false"
            />
          </el-form-item>
        </div>

        <div class="dlg-grid-2">
          <el-form-item label="Ngày bắt đầu">
            <el-date-picker
              v-model="dlg.form.startDate"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              class="w-100"
            />
          </el-form-item>
          <el-form-item label="Ngày kết thúc">
            <el-date-picker
              v-model="dlg.form.endDate"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              class="w-100"
            />
          </el-form-item>
        </div>

        <div class="dlg-grid-3">
          <el-form-item label="Phạm vi (scope)">
            <el-select v-model="dlg.form.scope" class="w-100">
              <el-option label="ALL — Tất cả" value="ALL" />
              <el-option label="PRODUCT — Theo sản phẩm" value="PRODUCT" />
              <el-option label="VARIANT — Theo biến thể" value="VARIANT" />
            </el-select>
          </el-form-item>
          <el-form-item label="Stackable (cộng dồn)">
            <el-switch v-model="dlg.form.stackable" />
          </el-form-item>
          <el-form-item label="Kích hoạt">
            <el-switch v-model="dlg.form.isActive" />
          </el-form-item>
        </div>

        <div class="dlg-grid-2">
          <el-form-item label="Product IDs (cách nhau bởi dấu phẩy)">
            <el-input v-model="dlg.form.productIdsText" placeholder="1,2,3" />
          </el-form-item>
          <el-form-item label="Variant IDs (cách nhau bởi dấu phẩy)">
            <el-input
              v-model="dlg.form.variantIdsText"
              placeholder="10,11,12"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open = false">Hủy</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="save">
          {{ dlg.mode === "create" ? "Tạo mới" : "Lưu thay đổi" }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { promotionsApi } from "../../api/promotions.api";
import { pricesApi } from "../../api/prices.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

const loading = ref(false);
const activeOnly = ref(false);
const rows = ref([]);

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
  return (list || []).map((p) => ({
    id: p?.id ?? p?.promotionId,
    code: p?.code ?? "",
    name: p?.name ?? "",
    description: p?.description ?? "",
    discountType: p?.discountType ?? "",
    discountValue: p?.discountValue ?? 0,
    startDate: p?.startDate ?? "",
    endDate: p?.endDate ?? "",
    scope: parseScope(p?.applicabilityJson) ?? p?.scope ?? "ALL",
    productIds:
      parseApplicability(p?.applicabilityJson, "product_ids") ??
      p?.productIds ??
      [],
    variantIds:
      parseApplicability(p?.applicabilityJson, "variant_ids") ??
      p?.variantIds ??
      [],
    priority: p?.priority ?? 0,
    stackable: !!p?.stackable,
    isActive: !!p?.isActive,
    raw: p,
  }));
}

function parseScope(json) {
  try {
    return JSON.parse(json)?.scope ?? null;
  } catch {
    return null;
  }
}
function parseApplicability(json, key) {
  try {
    return JSON.parse(json)?.[key] ?? null;
  } catch {
    return null;
  }
}

async function load() {
  loading.value = true;
  try {
    const res = await promotionsApi.list(activeOnly.value);
    rows.value = normalize(extractList(res?.data));
  } catch {
    rows.value = [];
    toast("Tải thất bại.", "error");
  } finally {
    loading.value = false;
  }
}

const conflictLoading = ref(false);
const conflicts = ref(null);
async function loadConflicts() {
  conflictLoading.value = true;
  try {
    const res = await promotionsApi.getConflicts();
    conflicts.value = res?.data?.data ?? res?.data ?? [];
  } catch {
    toast("Tải conflicts thất bại.", "error");
  } finally {
    conflictLoading.value = false;
  }
}

const previewPromo = ref(null);
const previewProductId = ref(null);
const previewCustomerId = ref(null);
const previewLoading = ref(false);
const previewPrices = ref([]);
const previewLoaded = ref(false);

function openPreview(row) {
  previewPromo.value = row;
  previewPrices.value = [];
  previewLoaded.value = false;
  previewProductId.value = null;
  previewCustomerId.value = null;
}

async function loadPreviewPrices() {
  if (!previewProductId.value)
    return toast("Vui lòng nhập Product ID.", "warning");
  previewLoading.value = true;
  previewLoaded.value = false;
  try {
    let res;
    if (previewCustomerId.value) {
      res = await pricesApi.listByProductForCustomer(
        previewProductId.value,
        previewCustomerId.value,
      );
    } else {
      res = await pricesApi.listByProduct(previewProductId.value);
    }
    previewPrices.value = res?.data?.data ?? res?.data ?? [];
    previewLoaded.value = true;
  } catch {
    previewPrices.value = [];
    toast("Tải giá thất bại.", "error");
  } finally {
    previewLoading.value = false;
  }
}

const dlg = reactive({
  open: false,
  mode: "create",
  loading: false,
  alert: "",
  id: null,
  form: {
    code: "",
    name: "",
    description: "",
    discountType: "PERCENT",
    discountValue: 0,
    startDate: "",
    endDate: "",
    scope: "ALL",
    productIdsText: "",
    variantIdsText: "",
    priority: 0,
    stackable: false,
    isActive: true,
  },
});

function parseIds(text) {
  return String(text || "")
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean)
    .map((x) => Number(x))
    .filter((n) => Number.isFinite(n));
}

function openCreate() {
  dlg.open = true;
  dlg.mode = "create";
  dlg.alert = "";
  dlg.id = null;
  dlg.form = {
    code: "",
    name: "",
    description: "",
    discountType: "PERCENT",
    discountValue: 0,
    startDate: "",
    endDate: "",
    scope: "ALL",
    productIdsText: "",
    variantIdsText: "",
    priority: 0,
    stackable: false,
    isActive: true,
  };
}

function openEdit(row) {
  dlg.open = true;
  dlg.mode = "edit";
  dlg.alert = "";
  dlg.id = row.id;
  dlg.form = {
    code: row.code,
    name: row.name,
    description: row.description,
    discountType: row.discountType || "PERCENT",
    discountValue: Number(row.discountValue || 0),
    startDate: row.startDate || "",
    endDate: row.endDate || "",
    scope: row.scope || "ALL",
    productIdsText: (row.productIds || []).join(","),
    variantIdsText: (row.variantIds || []).join(","),
    priority: Number(row.priority || 0),
    stackable: !!row.stackable,
    isActive: !!row.isActive,
  };
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.code || !dlg.form.name) {
    dlg.alert = "Code và tên là bắt buộc.";
    return;
  }
  const payload = {
    code: dlg.form.code,
    name: dlg.form.name,
    description: dlg.form.description,
    discountType: dlg.form.discountType,
    discountValue: dlg.form.discountValue,
    startDate: dlg.form.startDate,
    endDate: dlg.form.endDate,
    scope: dlg.form.scope,
    productIds: parseIds(dlg.form.productIdsText),
    variantIds: parseIds(dlg.form.variantIdsText),
    priority: dlg.form.priority,
    stackable: dlg.form.stackable,
    isActive: dlg.form.isActive,
  };
  dlg.loading = true;
  try {
    if (dlg.mode === "create") {
      await promotionsApi.create(payload);
      toast("Đã tạo khuyến mãi.", "success");
    } else {
      await promotionsApi.update(dlg.id, payload);
      toast("Đã cập nhật.", "success");
    }
    dlg.open = false;
    await load();
  } catch (e) {
    dlg.alert = e?.response?.data?.message || e?.message || "Lưu thất bại";
  } finally {
    dlg.loading = false;
  }
}

async function remove(row) {
  const ok = await confirmModal(
    `Xóa khuyến mãi #${row.id} (${row.code})?`,
    "Xác nhận",
    "Xóa",
    true,
  );
  if (!ok) return;
  try {
    await promotionsApi.remove(row.id);
    toast("Đã xóa.", "success");
    await load();
  } catch {
    toast("Xóa thất bại.", "error");
  }
}

function fmtDate(iso) {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleDateString("vi-VN");
  } catch {
    return iso;
  }
}
function fmtMoney(val) {
  if (val == null) return "—";
  return Number(val).toLocaleString("vi-VN") + " ₫";
}

const expiringLoading = ref(false);
const expiringRows = ref(null);
async function loadExpiring() {
  expiringLoading.value = true;
  try {
    const res = await promotionsApi.getExpiring(3);
    expiringRows.value = res?.data?.data ?? res?.data ?? [];
  } catch {
    toast("Tải danh sách sắp hết hạn thất bại.", "error");
  } finally {
    expiringLoading.value = false;
  }
}

const reportLoading = ref(false);
const reportData = ref(null);
const reportPeriod = ref("month");
async function loadReport() {
  reportLoading.value = true;
  try {
    const res = await promotionsApi.getReport(reportPeriod.value);
    reportData.value = res?.data?.data ?? res?.data ?? {};
  } catch {
    toast("Tải báo cáo thất bại.", "error");
  } finally {
    reportLoading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.promo-wrap {
  padding: 0 4px;
}

.promo-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.promo-kicker {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: rgba(15, 23, 42, 0.45);
  margin-bottom: 2px;
}
.promo-title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.3px;
  color: #0f172a;
}
.promo-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.promo-conflicts {
  margin-bottom: 12px;
}
.promo-section-label {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.promo-card {
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 12px;
  padding: 16px 20px;
}
.mt-3 {
  margin-top: 12px;
}
.mb-3 {
  margin-bottom: 12px;
}

.promo-endpoint {
  font-size: 11px;
  font-family: "SFMono-Regular", Consolas, monospace;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.07);
  border-radius: 6px;
  padding: 2px 8px;
}

.promo-code {
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.08);
  border-radius: 5px;
  padding: 2px 6px;
}
.promo-discount {
  font-size: 15px;
  font-weight: 800;
  color: #dc2626;
}
.promo-type-badge {
  font-size: 10px;
  color: rgba(15, 23, 42, 0.45);
  font-weight: 600;
  margin-top: 1px;
}
.promo-date-range {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}
.promo-date-sep {
  color: rgba(15, 23, 42, 0.3);
}
.promo-table {
  border-radius: 8px;
  overflow: hidden;
}
.promo-price-discount {
  color: #16a34a;
  font-weight: 700;
}
.promo-muted {
  color: rgba(15, 23, 42, 0.4);
  font-size: 12px;
}

.promo-preview-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}
.promo-preview-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* Dialog grids */
.dlg-grid-4 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 12px;
}
.dlg-grid-3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
}
.dlg-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
@media (max-width: 700px) {
  .dlg-grid-4,
  .dlg-grid-3,
  .dlg-grid-2 {
    grid-template-columns: 1fr;
  }
}

/* Transition */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
