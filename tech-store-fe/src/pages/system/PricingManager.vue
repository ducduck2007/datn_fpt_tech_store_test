<template>
  <div class="container-xl">
    <el-card shadow="never">
      <div
        class="d-flex align-items-end justify-content-between gap-2 flex-wrap"
      >
        <div>
          <div class="kicker">Admin</div>
          <div class="title">Pricing</div>
          <div class="muted">Base: /api/prices</div>
        </div>
      </div>

      <el-divider />

      <div class="row g-3">
        <div class="col-12 col-lg-6">
          <el-card shadow="never">
            <div class="fw-bold">Set price for variant</div>
            <div class="muted">POST /api/prices/variants/{variantId}</div>

            <el-divider />

            <el-form :model="setForm" label-position="top" class="row g-3">
              <div class="col-12 col-md-6">
                <el-form-item label="variantId">
                  <el-input v-model.number="setForm.variantId" />
                </el-form-item>
              </div>
              <div class="col-12 col-md-6">
                <el-form-item label="currencyCode">
                  <el-input
                    v-model="setForm.currencyCode"
                    placeholder="VND / USD..."
                  />
                </el-form-item>
              </div>
              <div class="col-12 col-md-6">
                <el-form-item label="price">
                  <el-input-number
                    v-model="setForm.price"
                    :min="0"
                    :max="999999999"
                    class="w-100"
                  />
                </el-form-item>
              </div>
              <div class="col-12 col-md-6">
                <el-form-item label="costPrice">
                  <el-input-number
                    v-model="setForm.costPrice"
                    :min="0"
                    :max="999999999"
                    class="w-100"
                  />
                </el-form-item>
              </div>
              <div class="col-12">
                <el-form-item label="reason">
                  <el-input v-model="setForm.reason" />
                </el-form-item>
              </div>
              <div class="col-12 d-flex justify-content-end">
                <el-button
                  type="primary"
                  :loading="setLoading"
                  @click="setPrice"
                  >Submit</el-button
                >
              </div>
            </el-form>

            <el-divider />

            <div class="d-flex align-items-center justify-content-between">
              <div class="fw-bold">Effective price</div>
              <el-button :loading="effLoading" @click="loadEffective"
                >Load</el-button
              >
            </div>
            <pre class="json mt-2">{{
              JSON.stringify(effective, null, 2)
            }}</pre>
          </el-card>
        </div>

        <div class="col-12 col-lg-6">
          <el-card shadow="never">
            <div class="fw-bold">Price history by product</div>
            <div class="muted">GET /api/prices/products/{productId}</div>

            <el-divider />

            <div class="d-flex gap-2">
              <el-input v-model.number="productId" placeholder="productId" />
              <el-button :loading="histLoading" @click="loadHistory"
                >Load</el-button
              >
            </div>

            <el-divider />

            <el-table :data="history" border :loading="histLoading">
              <el-table-column prop="id" label="ID" width="90" />
              <el-table-column prop="variantId" label="variantId" width="140" />
              <el-table-column
                prop="currencyCode"
                label="currency"
                width="120"
              />
              <el-table-column prop="price" label="price" width="140" />
              <el-table-column prop="costPrice" label="costPrice" width="140" />
              <el-table-column prop="reason" label="reason" min-width="200" />
              <el-table-column label="Actions" width="220">
                <template #default="{ row }">
                  <el-button size="small" @click="openEdit(row)"
                    >Edit</el-button
                  >
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    @click="remove(row)"
                    >Delete</el-button
                  >
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dlg.open" title="Update Price History" width="640px">
      <el-alert
        v-if="dlg.alert"
        :title="dlg.alert"
        type="error"
        show-icon
        class="mb-3"
      />

      <el-form :model="dlg.form" label-position="top" class="row g-3">
        <div class="col-12 col-md-6">
          <el-form-item label="currencyCode">
            <el-input v-model="dlg.form.currencyCode" />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="price">
            <el-input-number
              v-model="dlg.form.price"
              :min="0"
              :max="999999999"
              class="w-100"
            />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="costPrice">
            <el-input-number
              v-model="dlg.form.costPrice"
              :min="0"
              :max="999999999"
              class="w-100"
            />
          </el-form-item>
        </div>
        <div class="col-12 col-md-6">
          <el-form-item label="reason">
            <el-input v-model="dlg.form.reason" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open = false">Cancel</el-button>
        <el-button type="primary" :loading="dlg.loading" @click="saveEdit"
          >Update</el-button
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

const setLoading = ref(false);
const effLoading = ref(false);
const histLoading = ref(false);

const effective = ref(null);

const setForm = reactive({
  variantId: null,
  currencyCode: "VND",
  price: 0,
  costPrice: 0,
  reason: "",
});

async function setPrice() {
  if (!setForm.variantId) return toast("variantId is required.", "warning");
  setLoading.value = true;
  try {
    await pricesApi.setVariantPrice(setForm.variantId, {
      currencyCode: setForm.currencyCode,
      price: setForm.price,
      costPrice: setForm.costPrice,
      reason: setForm.reason,
    });
    toast("Price set.", "success");
  } catch {
    toast("Set price failed.", "error");
  } finally {
    setLoading.value = false;
  }
}

async function loadEffective() {
  if (!setForm.variantId) return toast("variantId is required.", "warning");
  effLoading.value = true;
  try {
    const res = await pricesApi.getEffective(setForm.variantId);
    effective.value = res?.data ?? null;
  } catch {
    effective.value = null;
    toast("Load effective price failed.", "error");
  } finally {
    effLoading.value = false;
  }
}

const productId = ref(null);
const history = ref([]);

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
  return (list || []).map((h) => ({
    id: h?.id ?? h?.historyId,
    variantId: h?.variantId ?? h?.variant?.id,
    currencyCode: h?.currencyCode ?? "",
    price: h?.price ?? 0,
    costPrice: h?.costPrice ?? 0,
    reason: h?.reason ?? "",
    raw: h,
  }));
}

async function loadHistory() {
  if (!productId.value) return toast("productId is required.", "warning");
  histLoading.value = true;
  try {
    const res = await pricesApi.listByProduct(productId.value);
    history.value = normalize(extractList(res?.data));
  } catch {
    history.value = [];
    toast("Load history failed.", "error");
  } finally {
    histLoading.value = false;
  }
}

const dlg = reactive({
  open: false,
  loading: false,
  alert: "",
  id: null,
  form: {
    currencyCode: "VND",
    price: 0,
    costPrice: 0,
    reason: "",
  },
});

function openEdit(row) {
  dlg.open = true;
  dlg.alert = "";
  dlg.id = row.id;
  dlg.form = {
    currencyCode: row.currencyCode,
    price: Number(row.price || 0),
    costPrice: Number(row.costPrice || 0),
    reason: row.reason || "",
  };
}

async function saveEdit() {
  dlg.loading = true;
  dlg.alert = "";
  try {
    await pricesApi.updateHistory(dlg.id, {
      currencyCode: dlg.form.currencyCode,
      price: dlg.form.price,
      costPrice: dlg.form.costPrice,
      reason: dlg.form.reason,
    });
    toast("History updated.", "success");
    dlg.open = false;
    await loadHistory();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Update failed";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

async function remove(row) {
  const ok = await confirmModal(
    `Delete history #${row.id}?`,
    "Confirm",
    "Delete",
    true
  );
  if (!ok) return;

  try {
    await pricesApi.removeHistory(row.id);
    toast("History deleted.", "success");
    await loadHistory();
  } catch {
    toast("Delete failed.", "error");
  }
}
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
.json {
  margin: 0;
  font-size: 12px;
  background: rgba(2, 6, 23, 0.04);
  border: 1px solid rgba(2, 6, 23, 0.08);
  border-radius: 10px;
  padding: 12px;
  overflow: auto;
}
</style>
