<template>
  <div class="pm-wrap">
    <!-- Page header -->
    <div class="pm-header">
      <div>
        <div class="pm-kicker">Admin · Pricing</div>
        <div class="pm-title">Quản lý giá</div>
      </div>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" class="pm-tabs">
      <!-- ═══ TAB 1: Đặt giá variant ═══ -->
      <el-tab-pane label="Đặt giá variant" name="set">
        <div class="pm-section-grid">
          <div class="pm-panel">
            <div class="pm-panel-head">
              <span class="pm-panel-title">Đặt giá</span>
              <span class="pm-endpoint"
                >POST /api/prices/variants/{variantId}</span
              >
            </div>
            <el-form :model="setForm" label-position="top" class="pm-form">
              <div class="pm-form-row">
                <el-form-item label="Variant ID">
                  <el-input
                    v-model.number="setForm.variantId"
                    placeholder="VD: 5"
                  />
                </el-form-item>
                <el-form-item label="Currency">
                  <el-input
                    v-model="setForm.currencyCode"
                    placeholder="VND / USD"
                  />
                </el-form-item>
              </div>
              <div class="pm-form-row">
                <el-form-item label="Giá bán (price)">
                  <el-input-number
                    v-model="setForm.price"
                    :min="0"
                    :max="999999999"
                    class="w-100"
                    :controls="false"
                  />
                </el-form-item>
                <el-form-item label="Giá nhập (costPrice)">
                  <el-input-number
                    v-model="setForm.costPrice"
                    :min="0"
                    :max="999999999"
                    class="w-100"
                    :controls="false"
                  />
                </el-form-item>
              </div>
              <el-form-item label="Lý do (reason)">
                <el-input
                  v-model="setForm.reason"
                  placeholder="MANUAL, PROMO_ENDED..."
                />
              </el-form-item>
              <div class="pm-form-footer">
                <el-button
                  type="primary"
                  :loading="setLoading"
                  @click="setPrice"
                  >Lưu giá</el-button
                >
              </div>
            </el-form>
          </div>

          <div class="pm-panel">
            <div class="pm-panel-head">
              <span class="pm-panel-title">Giá hiệu lực (combo)</span>
              <span class="pm-endpoint">GET /effective</span>
            </div>
            <div class="pm-hint">Sử dụng Variant ID đã nhập bên trái.</div>
            <div class="pm-action-row">
              <el-button :loading="effLoading" @click="loadEffective"
                >Tải giá hiệu lực</el-button
              >
            </div>
            <div v-if="effective" class="pm-result-box mt-2">
              <div class="pm-result-grid">
                <div class="pm-result-item">
                  <div class="pm-result-label">SKU</div>
                  <div class="pm-result-value">
                    {{ effective.data?.sku ?? effective.sku ?? "—" }}
                  </div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Base price</div>
                  <div class="pm-result-value">
                    {{ fmtMoney(effective.data?.price ?? effective.price) }}
                  </div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Final price</div>
                  <div class="pm-result-value pm-result-highlight">
                    {{
                      fmtMoney(
                        effective.data?.finalPrice ?? effective.finalPrice,
                      )
                    }}
                  </div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Promotion</div>
                  <div class="pm-result-value">
                    <el-tag
                      v-if="
                        effective.data?.promotionCode ?? effective.promotionCode
                      "
                      type="warning"
                      size="small"
                    >
                      {{
                        effective.data?.promotionCode ?? effective.promotionCode
                      }}
                    </el-tag>
                    <span v-else class="pm-muted">—</span>
                  </div>
                </div>
              </div>
            </div>

            <el-divider />

            <div class="pm-panel-head" style="margin-bottom: 12px">
              <span class="pm-panel-title">Giá cuối cùng cho khách</span>
              <span class="pm-endpoint">GET /effective/customer/{id}</span>
            </div>
            <div class="pm-action-row">
              <el-input
                v-model.number="customerIdForPrice"
                placeholder="Customer ID"
                style="width: 150px"
              />
              <el-button
                :loading="effCustLoading"
                @click="loadEffectiveForCustomer"
                >Tải</el-button
              >
            </div>
            <div v-if="effectiveForCustomer" class="pm-result-box mt-2">
              <div class="pm-result-grid">
                <div class="pm-result-item">
                  <div class="pm-result-label">Variant</div>
                  <div class="pm-result-value">
                    {{
                      effectiveForCustomer.data?.variantName ??
                      effectiveForCustomer.variantName ??
                      "—"
                    }}
                  </div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Base price</div>
                  <div class="pm-result-value">
                    {{
                      fmtMoney(
                        effectiveForCustomer.data?.price ??
                          effectiveForCustomer.price,
                      )
                    }}
                  </div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Final price</div>
                  <div class="pm-result-value pm-result-highlight">
                    {{
                      fmtMoney(
                        effectiveForCustomer.data?.finalPrice ??
                          effectiveForCustomer.finalPrice,
                      )
                    }}
                  </div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Promotion</div>
                  <div class="pm-result-value">
                    <el-tag
                      v-if="
                        effectiveForCustomer.data?.promotionCode ??
                        effectiveForCustomer.promotionCode
                      "
                      type="warning"
                      size="small"
                    >
                      {{
                        effectiveForCustomer.data?.promotionCode ??
                        effectiveForCustomer.promotionCode
                      }}
                    </el-tag>
                    <span v-else class="pm-muted">—</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ═══ TAB 2: Lịch sử giá ═══ -->
      <el-tab-pane label="Lịch sử giá" name="history">
        <div class="pm-section-label">Giá áp dụng chiến dịch theo product</div>
        <div class="pm-endpoint mb-2">GET /api/prices/products/{productId}</div>
        <div class="pm-action-row mb-3">
          <el-input
            v-model.number="productId"
            placeholder="Product ID"
            style="width: 160px"
          />
          <el-button
            :loading="prodLoading"
            @click="loadByProduct"
            type="primary"
            plain
            >Tải</el-button
          >
        </div>
        <el-table
          v-if="productPrices.length"
          :data="productPrices"
          border
          size="small"
          class="pm-table mb-4"
        >
          <el-table-column prop="variantId" label="Variant ID" width="100" />
          <el-table-column
            prop="variantName"
            label="Tên variant"
            min-width="160"
          />
          <el-table-column prop="sku" label="SKU" width="150" />
          <el-table-column label="Base price" width="130">
            <template #default="{ row }">{{ fmtMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column label="Final price" width="130">
            <template #default="{ row }">
              <span
                :class="row.finalPrice < row.price ? 'pm-price-discount' : ''"
                >{{ fmtMoney(row.finalPrice) }}</span
              >
            </template>
          </el-table-column>
          <el-table-column label="Promotion" width="130">
            <template #default="{ row }">
              <el-tag v-if="row.promotionCode" type="warning" size="small">{{
                row.promotionCode
              }}</el-tag>
              <span v-else class="pm-muted">—</span>
            </template>
          </el-table-column>
        </el-table>

        <el-divider />

        <div class="pm-section-label">Lịch sử giá theo variant</div>
        <div class="pm-endpoint mb-2">
          GET /api/prices/variants/{variantId}/history
        </div>
        <div class="pm-action-row mb-3">
          <el-input
            v-model.number="histVariantId"
            placeholder="Variant ID"
            style="width: 160px"
          />
          <el-button
            :loading="histLoading"
            @click="loadVariantHistory"
            type="primary"
            plain
            >Tải</el-button
          >
        </div>
        <el-table
          v-if="variantHistory.length"
          :data="variantHistory"
          border
          size="small"
          class="pm-table"
        >
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="Giá bán" width="130">
            <template #default="{ row }">{{ fmtMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column label="Giá nhập" width="130">
            <template #default="{ row }">{{
              row.costPrice ? fmtMoney(row.costPrice) : "—"
            }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="Lý do" min-width="120" />
          <el-table-column label="Hiệu lực từ" width="160">
            <template #default="{ row }">{{
              fmtDate(row.effectiveFrom)
            }}</template>
          </el-table-column>
          <el-table-column label="Hiệu lực đến" width="160">
            <template #default="{ row }">{{
              row.effectiveTo ? fmtDate(row.effectiveTo) : "—"
            }}</template>
          </el-table-column>
          <el-table-column
            prop="createdByUsername"
            label="Người tạo"
            width="110"
          />
          <el-table-column label="Thao tác" width="140" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEdit(row)">Sửa</el-button>
              <el-button
                size="small"
                type="danger"
                plain
                @click="removeHistory(row)"
                >Xóa</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <el-empty
          v-else-if="histLoaded"
          description="Không có dữ liệu"
          :image-size="60"
        />
      </el-tab-pane>

      <!-- ═══ TAB 3: Cảnh báo & Cập nhật ═══ -->
      <el-tab-pane label="Cảnh báo & Cập nhật" name="warning">
        <div class="pm-section-grid">
          <div class="pm-panel">
            <div class="pm-panel-head">
              <span class="pm-panel-title">Cảnh báo giá thấp hơn giá nhập</span>
              <span class="pm-endpoint">GET /cost-warning</span>
            </div>
            <div class="pm-action-row">
              <el-input
                v-model.number="costWarnVariantId"
                placeholder="Variant ID"
                style="width: 160px"
              />
              <el-button
                :loading="costWarnLoading"
                @click="loadCostWarning"
                type="warning"
                plain
                >Kiểm tra</el-button
              >
            </div>
            <div v-if="costWarning" class="mt-3">
              <el-alert
                v-if="costWarning.belowCost"
                :title="costWarning.message || '⚠️ Giá bán thấp hơn giá nhập!'"
                type="warning"
                show-icon
                :closable="false"
              />
              <el-alert
                v-else
                title="✅ Giá bán hợp lệ (không thấp hơn giá nhập)"
                type="success"
                show-icon
                :closable="false"
              />
              <div class="pm-result-box mt-2">
                <div class="pm-result-grid">
                  <div class="pm-result-item">
                    <div class="pm-result-label">Variant ID</div>
                    <div class="pm-result-value">
                      {{ costWarning.variantId }}
                    </div>
                  </div>
                  <div class="pm-result-item">
                    <div class="pm-result-label">Giá bán</div>
                    <div class="pm-result-value">
                      {{ fmtMoney(costWarning.price) }}
                    </div>
                  </div>
                  <div class="pm-result-item">
                    <div class="pm-result-label">Giá nhập</div>
                    <div class="pm-result-value">
                      {{
                        costWarning.costPrice
                          ? fmtMoney(costWarning.costPrice)
                          : "—"
                      }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="pm-panel">
            <div class="pm-panel-head">
              <span class="pm-panel-title"
                >Cập nhật giá khi hết khuyến mãi</span
              >
              <span class="pm-endpoint">PUT /api/prices/history/{id}</span>
            </div>
            <el-form :model="promoEndForm" label-position="top" class="pm-form">
              <div class="pm-form-row">
                <el-form-item label="History ID">
                  <el-input
                    v-model.number="promoEndForm.id"
                    placeholder="ID bản ghi"
                  />
                </el-form-item>
                <el-form-item label="Giá mới (price)">
                  <el-input-number
                    v-model="promoEndForm.price"
                    :min="0"
                    :max="999999999"
                    class="w-100"
                    :controls="false"
                  />
                </el-form-item>
              </div>
              <el-form-item label="Lý do (reason)">
                <el-input v-model="promoEndForm.reason" />
              </el-form-item>
              <div class="pm-form-footer">
                <el-button
                  type="primary"
                  :loading="promoEndLoading"
                  @click="submitPromoEnd"
                  >Cập nhật giá</el-button
                >
              </div>
            </el-form>
            <div v-if="promoEndResult" class="pm-result-box mt-3">
              <div class="pm-result-grid">
                <div class="pm-result-item">
                  <div class="pm-result-label">ID</div>
                  <div class="pm-result-value">{{ promoEndResult.id }}</div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Giá mới</div>
                  <div class="pm-result-value pm-result-highlight">
                    {{ fmtMoney(promoEndResult.price) }}
                  </div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Lý do</div>
                  <div class="pm-result-value">{{ promoEndResult.reason }}</div>
                </div>
                <div class="pm-result-item">
                  <div class="pm-result-label">Người tạo</div>
                  <div class="pm-result-value">
                    {{ promoEndResult.createdByUsername ?? "—" }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Edit dialog -->
    <el-dialog
      v-model="dlg.open"
      title="Sửa bản ghi giá"
      width="560px"
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
        <div class="pm-form-row">
          <el-form-item label="Currency">
            <el-input v-model="dlg.form.currencyCode" />
          </el-form-item>
          <el-form-item label="Giá bán">
            <el-input-number
              v-model="dlg.form.price"
              :min="0"
              :max="999999999"
              class="w-100"
              :controls="false"
            />
          </el-form-item>
        </div>
        <div class="pm-form-row">
          <el-form-item label="Giá nhập">
            <el-input-number
              v-model="dlg.form.costPrice"
              :min="0"
              :max="999999999"
              class="w-100"
              :controls="false"
            />
          </el-form-item>
          <el-form-item label="Lý do">
            <el-input v-model="dlg.form.reason" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dlg.open = false">Hủy</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="saveEdit"
          >Lưu</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { pricesApi } from "../../api/prices.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";

const activeTab = ref("set");

const setLoading = ref(false);
const setForm = reactive({
  variantId: null,
  currencyCode: "VND",
  price: 0,
  costPrice: 0,
  reason: "",
});

async function setPrice() {
  if (!setForm.variantId) return toast("Vui lòng nhập Variant ID.", "warning");
  setLoading.value = true;
  try {
    await pricesApi.setVariantPrice(setForm.variantId, {
      currencyCode: setForm.currencyCode,
      price: setForm.price,
      costPrice: setForm.costPrice,
      reason: setForm.reason,
    });
    toast("Đã lưu giá.", "success");
  } catch {
    toast("Lưu giá thất bại.", "error");
  } finally {
    setLoading.value = false;
  }
}

const effLoading = ref(false);
const effective = ref(null);
async function loadEffective() {
  if (!setForm.variantId) return toast("Vui lòng nhập Variant ID.", "warning");
  effLoading.value = true;
  try {
    const res = await pricesApi.getEffective(setForm.variantId);
    effective.value = res?.data ?? null;
  } catch {
    effective.value = null;
    toast("Tải thất bại.", "error");
  } finally {
    effLoading.value = false;
  }
}

const effCustLoading = ref(false);
const effectiveForCustomer = ref(null);
const customerIdForPrice = ref(null);
async function loadEffectiveForCustomer() {
  if (!setForm.variantId) return toast("Vui lòng nhập Variant ID.", "warning");
  if (!customerIdForPrice.value)
    return toast("Vui lòng nhập Customer ID.", "warning");
  effCustLoading.value = true;
  try {
    const res = await pricesApi.getEffectiveForCustomer(
      setForm.variantId,
      customerIdForPrice.value,
    );
    effectiveForCustomer.value = res?.data ?? null;
  } catch {
    effectiveForCustomer.value = null;
    toast("Tải thất bại.", "error");
  } finally {
    effCustLoading.value = false;
  }
}

const prodLoading = ref(false);
const productId = ref(null);
const productPrices = ref([]);
async function loadByProduct() {
  if (!productId.value) return toast("Vui lòng nhập Product ID.", "warning");
  prodLoading.value = true;
  try {
    const res = await pricesApi.listByProduct(productId.value);
    productPrices.value = res?.data?.data ?? res?.data ?? [];
  } catch {
    productPrices.value = [];
    toast("Tải thất bại.", "error");
  } finally {
    prodLoading.value = false;
  }
}

const histLoading = ref(false);
const histLoaded = ref(false);
const histVariantId = ref(null);
const variantHistory = ref([]);
async function loadVariantHistory() {
  if (!histVariantId.value)
    return toast("Vui lòng nhập Variant ID.", "warning");
  histLoading.value = true;
  histLoaded.value = false;
  try {
    const res = await pricesApi.getVariantHistory(histVariantId.value);
    variantHistory.value = res?.data?.data ?? res?.data ?? [];
    histLoaded.value = true;
  } catch {
    variantHistory.value = [];
    toast("Tải thất bại.", "error");
  } finally {
    histLoading.value = false;
  }
}

const dlg = reactive({
  open: false,
  loading: false,
  alert: "",
  id: null,
  form: { currencyCode: "VND", price: 0, costPrice: 0, reason: "" },
});
function openEdit(row) {
  dlg.open = true;
  dlg.alert = "";
  dlg.id = row.id;
  dlg.form = {
    currencyCode: row.currencyCode || "VND",
    price: Number(row.price || 0),
    costPrice: Number(row.costPrice || 0),
    reason: row.reason || "",
  };
}
async function saveEdit() {
  dlg.loading = true;
  dlg.alert = "";
  try {
    await pricesApi.updateHistory(dlg.id, dlg.form);
    toast("Đã cập nhật.", "success");
    dlg.open = false;
    await loadVariantHistory();
  } catch (e) {
    dlg.alert = e?.response?.data?.message || e?.message || "Cập nhật thất bại";
  } finally {
    dlg.loading = false;
  }
}
async function removeHistory(row) {
  const ok = await confirmModal(
    `Xóa bản ghi #${row.id}?`,
    "Xác nhận",
    "Xóa",
    true,
  );
  if (!ok) return;
  try {
    await pricesApi.removeHistory(row.id);
    toast("Đã xóa.", "success");
    await loadVariantHistory();
  } catch {
    toast("Xóa thất bại.", "error");
  }
}

const costWarnLoading = ref(false);
const costWarnVariantId = ref(null);
const costWarning = ref(null);
async function loadCostWarning() {
  if (!costWarnVariantId.value)
    return toast("Vui lòng nhập Variant ID.", "warning");
  costWarnLoading.value = true;
  try {
    const res = await pricesApi.getCostWarning(costWarnVariantId.value);
    costWarning.value = res?.data?.data ?? res?.data ?? null;
  } catch {
    costWarning.value = null;
    toast("Tải thất bại.", "error");
  } finally {
    costWarnLoading.value = false;
  }
}

const promoEndLoading = ref(false);
const promoEndResult = ref(null);
const promoEndForm = reactive({ id: null, price: 0, reason: "PROMO_ENDED" });
async function submitPromoEnd() {
  if (!promoEndForm.id) return toast("Vui lòng nhập History ID.", "warning");
  promoEndLoading.value = true;
  try {
    const res = await pricesApi.updateHistory(promoEndForm.id, {
      price: promoEndForm.price,
      reason: promoEndForm.reason,
    });
    promoEndResult.value = res?.data?.data ?? res?.data ?? null;
    toast("Đã cập nhật giá.", "success");
  } catch (e) {
    toast(e?.response?.data?.message || "Cập nhật thất bại.", "error");
  } finally {
    promoEndLoading.value = false;
  }
}

function fmtMoney(val) {
  if (val == null) return "—";
  return Number(val).toLocaleString("vi-VN") + " ₫";
}
function fmtDate(iso) {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleString("vi-VN");
  } catch {
    return iso;
  }
}
</script>

<style scoped>
.pm-wrap {
  padding: 0 4px;
}

.pm-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 16px;
}
.pm-kicker {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: rgba(15, 23, 42, 0.45);
  margin-bottom: 2px;
}
.pm-title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.3px;
  color: #0f172a;
}

.pm-tabs {
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 12px;
  padding: 16px 20px 20px;
}

.pm-section-label {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.pm-endpoint {
  font-size: 11px;
  font-family: "SFMono-Regular", Consolas, monospace;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.07);
  border-radius: 6px;
  padding: 2px 8px;
}
.mb-2 {
  margin-bottom: 8px;
}
.mb-3 {
  margin-bottom: 12px;
}
.mb-4 {
  margin-bottom: 16px;
}
.mt-2 {
  margin-top: 8px;
}
.mt-3 {
  margin-top: 12px;
}

.pm-section-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 900px) {
  .pm-section-grid {
    grid-template-columns: 1fr;
  }
}

.pm-panel {
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.07);
  border-radius: 10px;
  padding: 16px 18px;
}
.pm-panel-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.pm-panel-title {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.pm-form {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.pm-form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
@media (max-width: 500px) {
  .pm-form-row {
    grid-template-columns: 1fr;
  }
}
.pm-form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.pm-action-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pm-result-box {
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  padding: 12px 14px;
}
.pm-result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 16px;
}
.pm-result-label {
  font-size: 11px;
  color: rgba(15, 23, 42, 0.5);
  margin-bottom: 2px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}
.pm-result-value {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}
.pm-result-highlight {
  color: #16a34a;
  font-size: 15px;
}

.pm-table {
  border-radius: 8px;
  overflow: hidden;
}
.pm-price-discount {
  color: #16a34a;
  font-weight: 700;
}
.pm-muted {
  color: rgba(15, 23, 42, 0.4);
  font-size: 12px;
}
.pm-hint {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.5);
  margin-bottom: 10px;
}
</style>
