<template>
  <div class="pos-root">

    <!-- ══════════════════════════════════════
         LEFT PANEL — Serial Search
    ══════════════════════════════════════ -->
    <div class="panel panel-left">

      <!-- Search bar -->
      <div class="search-bar">
        <div class="search-wrap" :class="{ focused: searchFocused }">
          <svg class="s-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="3" stroke="currentColor" stroke-width="1.8"/>
            <path d="M7 8h10M7 12h6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input
            ref="searchInput"
            v-model="searchQuery"
            class="s-input"
            placeholder="Nhập mã serial để tìm kiếm..."
            autocomplete="off"
            spellcheck="false"
            @focus="searchFocused = true"
            @blur="searchFocused = false"
            @input="onSearchInput"
            @keyup.enter="doSearch"
          />
          <span v-if="searchLoading" class="s-hint">Đang tìm...</span>
          <button v-if="searchQuery" class="s-clear" @click="clearSearch" tabindex="-1">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Search error -->
      <transition name="fade-down">
        <div v-if="searchError" class="error-bar">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke="currentColor" stroke-width="1"/>
          </svg>
          {{ searchError }}
        </div>
      </transition>

      <!-- Loading -->
      <div v-if="searchLoading" class="loading-state">
        <span class="spinner amber"></span>
        <span>Đang tra cứu serial...</span>
      </div>

      <!-- Results -->
      <div v-else-if="productGroups.length" class="results-scroll">
        <p class="results-meta">
          Đã tìm thấy serial —
          <strong>click để thêm vào đơn</strong>
        </p>

        <div
          v-for="product in productGroups"
          :key="product.id"
          class="product-card"
        >
          <!-- Product name -->
          <div class="product-header">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" stroke="currentColor" stroke-width="1.7"/>
            </svg>
            <span class="product-name">{{ product.name }}</span>
          </div>

          <!-- Variants -->
          <div
            v-for="variant in product.variants"
            :key="variant.id"
            class="variant-row"
          >
            <div class="variant-meta">
              <span class="variant-name">{{ variant.name }}</span>
              <span class="variant-price">{{ formatMoney(variant.price) }}</span>
              <!-- Remaining stock badge -->
              <span class="serial-badge" :class="variant.remainingCount > 0 ? 'serial-badge--ok' : 'serial-badge--out'">
                <span v-if="variant.serialsLoading" class="spinner amber tiny"></span>
                <template v-else>
                  <svg width="9" height="9" viewBox="0 0 24 24" fill="none">
                    <rect x="3" y="3" width="18" height="18" rx="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  {{ variant.remainingCount }} máy còn lại
                </template>
              </span>
            </div>

            <!-- Serial chip(s) -->
            <div v-if="variant.serials.length" class="serial-chips">
              <button
                v-for="s in variant.serials"
                :key="s.id"
                class="chip"
                :class="{ added: alreadyInCart(s.id) }"
                :disabled="alreadyInCart(s.id)"
                @click="addToCart(s, variant, product.name)"
              >
                <svg width="9" height="9" viewBox="0 0 24 24" fill="none">
                  <rect x="3" y="3" width="18" height="18" rx="3" stroke="currentColor" stroke-width="2"/>
                  <path d="M7 8h10M7 12h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                {{ serialCode(s) || '...' }}
                <svg v-if="alreadyInCart(s.id)" class="check-icon" width="9" height="9" viewBox="0 0 24 24" fill="none">
                  <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
            <p v-else-if="!variant.serialsLoading" class="no-serials">Hết hàng</p>
          </div>
        </div>
      </div>

      <!-- Idle / no result -->
      <div v-else-if="!searchLoading" class="empty-state">
        <div class="empty-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="3" stroke="currentColor" stroke-width="1.2"/>
            <path d="M7 8h10M7 12h7M7 16h4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
            <circle v-if="hasSearched" cx="19" cy="19" r="4" stroke="currentColor" stroke-width="1.2"/>
            <path v-if="hasSearched" d="M17 19h4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-title">{{ hasSearched ? 'Không tìm thấy serial' : 'Tra cứu theo mã serial' }}</p>
        <p class="empty-sub">{{ hasSearched ? 'Kiểm tra lại mã serial hoặc thử mã khác' : 'Nhập mã serial để tự động tìm kiếm' }}</p>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         RIGHT PANEL — Cart
    ══════════════════════════════════════ -->
    <div class="panel panel-right">

      <!-- Cart header -->
      <div class="cart-header">
        <h2 class="cart-title">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            <line x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="1.8"/>
          </svg>
          Đơn hàng
        </h2>
        <span class="cart-count">{{ cart.length }}</span>
        <button v-if="cart.length" class="btn-clear-cart" @click="handleClearCart">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
            <polyline points="3 6 5 6 21 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" stroke="currentColor" stroke-width="1.8"/>
            <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" stroke-width="1.8"/>
          </svg>
          Xóa đơn
        </button>
      </div>

      <!-- Cart items -->
      <div class="cart-body">
        <div v-if="!cart.length" class="cart-empty">
          <svg width="30" height="30" viewBox="0 0 24 24" fill="none" opacity="0.25">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" stroke="currentColor" stroke-width="1.5"/>
            <line x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          <p>Chưa có sản phẩm</p>
        </div>

        <transition-group name="cart-anim" tag="div" class="cart-list">
          <div v-for="item in cart" :key="item.serialId" class="cart-item">
            <div class="ci-info">
              <span class="ci-name">{{ item.productName }}</span>
              <span class="ci-variant">{{ item.variantName }}</span>
              <span class="ci-serial">{{ item.serialCode }}</span>
            </div>
            <div class="ci-right">
              <span class="ci-price">{{ formatMoney(item.price) }}</span>
              <button class="btn-remove" @click="removeFromCart(item.serialId)">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                  <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>
        </transition-group>
      </div>

      <!-- Customer (optional) -->
      <div class="customer-section">
        <div class="section-label">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>
          </svg>
          Khách hàng
          <span class="optional">(tuỳ chọn)</span>
        </div>
        <div class="cus-row">
          <div class="cus-input-wrap" :class="{ focused: cusFocused }">
            <input
              v-model="cusQuery"
              class="cus-input"
              placeholder="SĐT hoặc email..."
              @focus="cusFocused = true"
              @blur="cusFocused = false"
              @keyup.enter="lookupCustomer"
            />
          </div>
          <button class="btn-lookup" @click="lookupCustomer" :disabled="cusLoading || !cusQuery.trim()">
            <span v-if="!cusLoading">Tra</span>
            <span v-else class="spinner white tiny"></span>
          </button>
          <button v-if="foundCustomer" class="btn-remove-cus" @click="removeCustomer">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <transition name="fade-down">
          <div v-if="foundCustomer" class="cus-chip">
            <div class="cus-avatar">{{ initials(foundCustomer.fullName || foundCustomer.name) }}</div>
            <div class="cus-info">
              <span class="cus-name">{{ foundCustomer.fullName || foundCustomer.name }}</span>
              <span class="cus-meta">{{ foundCustomer.phone || foundCustomer.email }}</span>
            </div>
            <span v-if="foundCustomer.vipTier || foundCustomer.customerType === 'VIP'" class="cus-tier">
              {{ foundCustomer.vipTier || 'VIP' }}
            </span>
          </div>
        </transition>
        <p v-if="cusError" class="cus-error">{{ cusError }}</p>
      </div>

      <!-- Total -->
      <div class="total-section">
        <div class="total-row">
          <span class="total-label">Tổng cộng</span>
          <span class="total-value">{{ formatMoney(totalAmount) }}</span>
        </div>
        <p v-if="cart.length" class="total-sub">{{ cart.length }} sản phẩm</p>
      </div>

      <!-- Pay button -->
      <div class="pay-wrap">
        <transition name="fade-down">
          <div v-if="cart.length && !foundCustomer" class="cus-required">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/>
              <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke="currentColor" stroke-width="1"/>
            </svg>
            Vui lòng tra cứu và chọn khách hàng trước khi thanh toán
          </div>
        </transition>
        <button
          class="btn-pay"
          :class="{ disabled: !cart.length || !foundCustomer }"
          :disabled="!cart.length || !foundCustomer"
          @click="cashIn = totalAmount; showModal = true"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
            <rect x="1" y="4" width="22" height="16" rx="2" stroke="currentColor" stroke-width="1.8"/>
            <line x1="1" y1="10" x2="23" y2="10" stroke="currentColor" stroke-width="1.8"/>
          </svg>
          Thanh toán · {{ formatMoney(totalAmount) }}
        </button>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         PAYMENT MODAL
    ══════════════════════════════════════ -->
    <transition name="modal">
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="payment-modal">
          <div class="modal-head">
            <h3 class="modal-title">Thanh toán</h3>
            <button class="modal-close" @click="closeModal">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
                <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </button>
          </div>

          <div class="modal-amount-box">
            <span class="modal-amount-label">Số tiền thanh toán</span>
            <span class="modal-amount-value">{{ formatMoney(totalAmount) }}</span>
          </div>

          <!-- Method tabs -->
          <div class="method-tabs">
            <button
              v-for="m in methods"
              :key="m.value"
              class="method-tab"
              :class="{ active: payMethod === m.value }"
              @click="payMethod = m.value; cashIn = totalAmount"
            >
              <span>{{ m.icon }}</span>{{ m.label }}
            </button>
          </div>

          <!-- Cash detail -->
          <div v-if="payMethod === 'CASH'" class="method-body">
            <label class="field-lbl">Tiền khách đưa</label>
            <div class="cash-wrap">
              <input v-model.number="cashIn" type="number" class="cash-input" placeholder="0" @focus="$event.target.select()"/>
              <span class="cash-unit">₫</span>
            </div>
            <div class="quick-cash">
              <button v-for="q in quickOptions" :key="q" class="quick-btn" @click="cashIn = q">{{ formatShort(q) }}</button>
            </div>
            <!-- cashIn = 0 → khách đưa đúng số, không thừa không thiếu -->
            <div v-if="cashIn === 0 || cashIn === totalAmount" class="cash-change">
              <span>Tiền thừa trả lại</span>
              <strong>{{ formatMoney(0) }}</strong>
            </div>
            <div v-else-if="cashIn > totalAmount" class="cash-change">
              <span>Tiền thừa trả lại</span>
              <strong>{{ formatMoney(cashIn - totalAmount) }}</strong>
            </div>
            <div v-else-if="cashIn > 0 && cashIn < totalAmount" class="cash-lack">
              <span>Còn thiếu</span>
              <strong>{{ formatMoney(totalAmount - cashIn) }}</strong>
            </div>
          </div>

          <!-- Transfer -->
          <div v-else-if="payMethod === 'TRANSFER'" class="method-body">
            <div class="info-box">
              <span style="font-size:28px">📱</span>
              <div>
                <p class="info-title">Chuyển khoản / QR Pay</p>
                <p class="info-sub">Khách quét mã và chuyển đúng số tiền.<br/>Xác nhận sau khi nhận được tiền.</p>
              </div>
            </div>
          </div>

          <!-- Card -->
          <div v-else class="method-body">
            <div class="info-box">
              <span style="font-size:28px">💳</span>
              <div>
                <p class="info-title">Thẻ tín dụng / ghi nợ</p>
                <p class="info-sub">Cho khách quẹt thẻ qua máy POS.<br/>Xác nhận sau khi giao dịch thành công.</p>
              </div>
            </div>
          </div>

          <transition name="fade-down">
            <div v-if="payError" class="pay-error">{{ payError }}</div>
          </transition>

          <div class="modal-footer">
            <button
              class="btn-confirm"
              :disabled="payLoading || (payMethod === 'CASH' && cashIn > 0 && cashIn < totalAmount)"
              @click="confirmPayment"
            >
              <span v-if="!payLoading">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Xác nhận thanh toán
              </span>
              <span v-else class="spinner white"></span>
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ══════════════════════════════════════
         SUCCESS OVERLAY
    ══════════════════════════════════════ -->
    <transition name="modal">
      <div v-if="orderDone" class="modal-overlay">
        <div class="success-modal">
          <div class="success-icon">
            <svg width="44" height="44" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="#16a34a" stroke-width="1.8"/>
              <polyline points="7 12 10 15 17 9" stroke="#16a34a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3 class="success-title">Thanh toán thành công!</h3>
          <p class="success-id">{{ orderDone?.orderNumber ?? orderDone?.order_number ?? ('#' + orderDone?.id) }}</p>
          <p class="success-amount">{{ formatMoney(snapshotTotal) }}</p>
          <div v-if="payMethod === 'CASH' && cashIn > snapshotTotal" class="success-change">
            Tiền thừa: <strong>{{ formatMoney(cashIn - snapshotTotal) }}</strong>
          </div>
          <button class="btn-new" @click="resetAll">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
              <polyline points="1 4 1 10 7 10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M3.51 15a9 9 0 1 0 .49-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
            Đơn mới
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { productsApi } from "../../api/products.api";
import { serialsApi } from "../../api/serials.api";
import { ordersApi } from "../../api/orders.api";
import { paymentsApi } from "../../api/payments";
import { customersApi } from "../../api/customers.api";
import { confirmModal } from "../../ui/confirm";
import { toast } from "../../ui/toast";

const router = useRouter();

// ── Search (Serial-based) ────────────────────────────────────────
const searchInput  = ref(null);
const searchQuery  = ref("");
const searchFocused = ref(false);
const searchLoading = ref(false);
const searchError  = ref("");
const productGroups = ref([]);
const hasSearched  = ref(false);

const SOLD_STATUSES = ["sold", "used", "inactive", "disabled", "deleted", "reserved"];

// serialNumber là field chính theo API (SerialManager xác nhận)
const serialCode = (s) =>
  s.serialNumber ?? s.serialCode ?? s.serial_number ?? s.serial ?? s.code ?? "";

// Debounce timer cho auto-search
let searchTimer = null;

// Auto-search theo từng ký tự gõ (debounce 400ms)
function onSearchInput() {
  clearTimeout(searchTimer);
  if (!searchQuery.value.trim()) {
    productGroups.value = [];
    searchError.value = "";
    hasSearched.value = false;
    return;
  }
  searchTimer = setTimeout(() => doSearch(), 400);
}

/**
 * Tìm serial theo từ khoá — tự động gọi khi gõ (hoặc nhấn Enter).
 * Dùng serialsApi.list({ keyword }) → GET /api/products/variants/serials/all
 * Hỗ trợ response dạng array, { data: [] }, { content: [] }, { data: { content: [] } }
 */
async function doSearch() {
  const q = searchQuery.value.trim();
  if (!q) {
    searchError.value = "Vui lòng nhập mã serial cần tìm.";
    return;
  }
  hasSearched.value = true;
  searchLoading.value = true;
  searchError.value = "";
  productGroups.value = [];

  try {
    // Gọi API list với keyword search — parse theo đúng cấu trúc của SerialManager
    const res = await serialsApi.list({ keyword: q, size: 50 });
    const data = res.data?.data ?? res.data;
    // Hỗ trợ paginated (content) và array thẳng
    const items = Array.isArray(data?.content) ? data.content
                : Array.isArray(data)           ? data
                : [];

    if (!items.length) {
      searchError.value = `Không tìm thấy serial "${q}" trong kho hàng.`;
      return;
    }

    // Lọc serial còn khả dụng
    const available = items.filter((s) => {
      const st = (s.status || "").toLowerCase();
      return !SOLD_STATUSES.includes(st);
    });

    if (!available.length) {
      searchError.value = `Serial "${q}" đã được bán hoặc không còn khả dụng.`;
      return;
    }

    // Nhóm serial theo product → variant
    // Dùng productName làm key vì API không trả productId trong list endpoint
    const groupMap = {};
    for (const serial of available) {
      const productId   = serial.productId   ?? serial.product?.id
                       ?? serial.variant?.productId ?? serial.variant?.product?.id ?? null;
      const productName = serial.productName ?? serial.product?.name ?? serial.variant?.productName ?? "Sản phẩm";
      const variantId   = serial.variantId   ?? serial.variant?.id   ?? null;
      const variantName = serial.variantName ?? serial.variant?.name ?? "Mặc định";
      // Giá: ưu tiên các field phổ biến, fallback 0
      const price = serial.price ?? serial.salePrice ?? serial.sellingPrice
                  ?? serial.variant?.price ?? serial.variant?.salePrice ?? serial.variant?.sellingPrice
                  ?? serial.variantPrice ?? serial.variantSalePrice
                  ?? serial.productVariant?.price ?? serial.productVariant?.salePrice
                  ?? 0;

      // Group key theo tên sản phẩm (an toàn hơn dùng id vì id có thể undefined)
      const groupKey   = productName;
      const variantKey = variantName; // group variant theo tên

      if (!groupMap[groupKey]) {
        groupMap[groupKey] = { id: productId, name: productName, variantMap: {} };
      }
      const vm = groupMap[groupKey].variantMap;
      if (!vm[variantKey]) {
        vm[variantKey] = {
          id: variantId,
          name: variantName,
          price,
          remainingCount: 0,
          serials: [],
          serialsLoading: false,
          _needsPrice: variantId != null, // serial API không trả giá → luôn fetch từ variant API
        };
      }
      vm[variantKey].serials.push(serial);
      vm[variantKey].remainingCount++;
    }

    const groups = Object.values(groupMap).map((g) => ({
      id: g.id,
      name: g.name,
      variants: Object.values(g.variantMap),
    }));

    productGroups.value = groups;

    // ── Fetch giá: copy y chang logic code cũ ──────────────────────
    // Code cũ: productsApi.list() → lấy productId → getVariants(productId) → lấy giá từ variant
    // Serial API không bao giờ trả giá, nên phải đi qua productsApi như code cũ.
    await Promise.all(
      groups.map(async (group) => {
        try {
          // Bước 1: lấy productId — dùng group.id nếu có, không thì search theo tên (y chang code cũ)
          let productId = group.id;

          if (!productId) {
            const pRes = await productsApi.list({ keyword: group.name, name: group.name, q: group.name, search: group.name });
            const pRaw = pRes?.data;
            const pList = Array.isArray(pRaw) ? pRaw
                        : Array.isArray(pRaw?.data) ? pRaw.data
                        : Array.isArray(pRaw?.content) ? pRaw.content
                        : Array.isArray(pRaw?.items) ? pRaw.items : [];
            const pMatch = pList.find((p) => p.name === group.name) ?? pList[0] ?? null;
            if (pMatch) { productId = pMatch.id; group.id = pMatch.id; }
          }

          if (!productId) return;

          // Bước 2: getVariants(productId)
          const vRes = await productsApi.getVariants(productId);
          const vRaw = vRes?.data;
          const vList = Array.isArray(vRaw) ? vRaw
                      : Array.isArray(vRaw?.data) ? vRaw.data
                      : Array.isArray(vRaw?.content) ? vRaw.content : [];

          // Bước 3: match variant theo id → variantName → name (case-insensitive) → fallback 1 phần tử
          for (const variant of group.variants) {
            const sName = variant.name;
            const vFound = vList.find((v) => v.id === variant.id)
                        ?? vList.find((v) => (v.variantName ?? v.name ?? v.variant_name ?? "") === sName)
                        ?? vList.find((v) => {
                             const vn = (v.variantName ?? v.name ?? v.variant_name ?? "").trim().toLowerCase();
                             return vn && vn === sName.trim().toLowerCase();
                           })
                        ?? (vList.length === 1 ? vList[0] : null);
            const p = vFound ? (vFound.price ?? vFound.salePrice ?? vFound.sellingPrice ?? 0) : 0;
            if (vFound) {
              variant.price = p;
              if (!variant.id) variant.id = vFound.id;
            }
          }
        } catch { /* giữ giá 0 nếu lỗi */ }
      })
    );
  } catch (e) {
    searchError.value = e?.response?.data?.message || `Không tìm thấy serial "${q}". Kiểm tra lại mã.`;
  } finally {
    searchLoading.value = false;
  }
}

function clearSearch() {
  searchQuery.value = "";
  searchError.value = "";
  hasSearched.value = false;
  productGroups.value = [];
  searchInput.value?.focus();
}

// ── Cart ────────────────────────────────────────────────────────
const cart = ref([]);

function addToCart(serial, variant, productName) {
  const sid = serial.id ?? serial.serialId;
  if (alreadyInCart(sid)) {
    toast("Serial này đã có trong đơn hàng", "warning");
    return;
  }
  cart.value.push({
    serialId:    sid,
    serialCode:  serialCode(serial),
    variantId:   variant.id,
    productName,
    variantName: variant.name,
    price:       variant.price,
  });
  toast(`Đã thêm serial ${serialCode(serial)} vào đơn`, "success");
}

function alreadyInCart(sid) {
  return cart.value.some((i) => i.serialId === sid);
}

function removeFromCart(sid) {
  cart.value = cart.value.filter((i) => i.serialId !== sid);
}

async function handleClearCart() {
  if (!cart.value.length) return;
  const ok = await confirmModal(
    `Xóa tất cả ${cart.value.length} sản phẩm khỏi đơn hàng?`,
    "Xóa đơn hàng",
    "Xóa đơn",
    true
  );
  if (ok) {
    cart.value = [];
    toast("Đã xóa đơn hàng", "info");
  }
}

const totalAmount = computed(() => cart.value.reduce((s, i) => s + i.price, 0));

// ── Customer lookup ──────────────────────────────────────────────
const cusQuery     = ref("");
const cusFocused   = ref(false);
const cusLoading   = ref(false);
const cusError     = ref("");
const foundCustomer = ref(null);

async function lookupCustomer() {
  const q = cusQuery.value.trim().toLowerCase();
  if (!q) return;
  cusLoading.value = true;
  cusError.value = "";
  foundCustomer.value = null;
  try {
    const res = await customersApi.listAll();
    const list = res?.data?.data ?? res?.data ?? [];
    const found = list.find(
      (c) =>
        (c.phone || "").includes(q) ||
        (c.email || "").toLowerCase().includes(q) ||
        (c.fullName || c.name || "").toLowerCase().includes(q)
    );
    if (found) {
      foundCustomer.value = found;
      toast(`Đã tìm thấy: ${found.fullName || found.name}`, "success");
    } else {
      cusError.value = "Không tìm thấy khách hàng.";
    }
  } catch {
    cusError.value = "Lỗi tra cứu. Vui lòng thử lại.";
  } finally {
    cusLoading.value = false;
  }
}

function removeCustomer() {
  foundCustomer.value = null;
  cusQuery.value = "";
  cusError.value = "";
}

// ── Payment ──────────────────────────────────────────────────────
const showModal    = ref(false);
const payMethod    = ref("CASH");
const cashIn       = ref(0);
const payLoading   = ref(false);
const payError     = ref("");
const orderDone    = ref(null);
const snapshotTotal = ref(0);

const methods = [
  { value: "CASH",        label: "Tiền mặt",          icon: "💵" },
  { value: "TRANSFER",    label: "Chuyển khoản",       icon: "📱" },
  { value: "CREDIT_CARD", label: "Thẻ tín dụng",       icon: "💳" },
];

const quickOptions = computed(() => {
  const t = totalAmount.value;
  const ceil = (n) => Math.ceil(n / 10000) * 10000;
  const base = ceil(t);
  return [...new Set([base, base + 50000, base + 100000, base + 200000])].filter((v) => v >= t);
});

function closeModal() {
  if (payLoading.value) return;
  showModal.value = false;
  payError.value = "";
}

async function confirmPayment() {
  payLoading.value = true;
  payError.value = "";
  snapshotTotal.value = totalAmount.value;
  // cashIn = 0 nghĩa là khách đưa đúng số tiền (option 1)
  if (cashIn.value === 0) cashIn.value = snapshotTotal.value;
  // ── VALIDATE variantId ──────────────────────────────────────────
  const invalidItem = cart.value.find((i) => !i.variantId);
  if (invalidItem) {
    payError.value = `Không tìm được mã variant cho "${invalidItem.variantName}". Vui lòng xóa và thêm lại sản phẩm.`;
    payLoading.value = false;
    return;
  }
  // ───────────────────────────────────────────────────────────────
  try {
    // 1. Tạo đơn hàng (PENDING)
    const oRes = await ordersApi.create({
      customerId:    foundCustomer.value?.id || null,
      paymentMethod: payMethod.value,
      channel:       "OFFLINE",
      notes:         "POS - Bán tại quầy",
      items: cart.value.map((i) => ({
        variantId: i.variantId,
        quantity:  1,
      })),
    });

    const raw     = oRes?.data;
    const order   = raw?.data ?? raw?.order ?? raw;
    const orderId = order?.id ?? order?.orderId ?? raw?.id ?? raw?.orderId;
    console.log("[POS] order response:", JSON.stringify(order));

    if (!orderId) throw new Error("Không lấy được mã đơn hàng từ server.");

    // 2. Tạo payment
    const now = new Date().toISOString();
    await paymentsApi.create({
      orderId,
      method:        payMethod.value,
      amount:        snapshotTotal.value,
      paymentStatus: "PAID",
      paidAt:        now,
    });

    // 3. Cập nhật trạng thái đơn hàng
    await ordersApi.markAsProcessing(orderId).catch(() => {});
    await ordersApi.markAsShipping(orderId).catch(() => {});
    await ordersApi.markAsDelivered(orderId).catch(() => {});

    // 4. Cập nhật serial → SOLD
    await Promise.allSettled(
      cart.value.map((item) => serialsApi.updateStatus(item.serialId, "SOLD"))
    );

    orderDone.value = order;
    showModal.value = false;
    toast("Thanh toán thành công!", "success");
  } catch (e) {
    payError.value = e?.response?.data?.message || e?.message || "Tạo đơn thất bại. Thử lại.";
    toast(payError.value, "error");
  } finally {
    payLoading.value = false;
  }
}

function resetAll() {
  cart.value = [];
  foundCustomer.value = null;
  cusQuery.value = "";
  cusError.value = "";
  payMethod.value = "CASH";
  cashIn.value = 0;
  payError.value = "";
  orderDone.value = null;
  clearSearch();
  searchInput.value?.focus();
}


// ── Helpers ──────────────────────────────────────────────────────
function formatMoney(v) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND", maximumFractionDigits: 0 }).format(v || 0);
}
function formatShort(v) {
  if (v >= 1_000_000) return (v / 1_000_000).toFixed(v % 1_000_000 === 0 ? 0 : 1) + "tr";
  if (v >= 1_000)     return (v / 1_000).toFixed(0) + "k";
  return String(v);
}
function initials(name = "") {
  return (name || "").split(" ").map((w) => w[0]).slice(0, 2).join("").toUpperCase() || "KH";
}

// ── Keyboard shortcuts ───────────────────────────────────────────
function onKey(e) {
  if (e.key === "F2")     { e.preventDefault(); searchInput.value?.focus(); }
  if (e.key === "Escape" && showModal.value) closeModal();
}

onMounted(() => {
  window.addEventListener("keydown", onKey);
  searchInput.value?.focus();

  // Đọc khách hàng được gán từ trang SalesCustomerManager
  try {
    const saved = sessionStorage.getItem("pos_customer");
    if (saved) {
      const cust = JSON.parse(saved);
      foundCustomer.value = cust;
      cusQuery.value = cust.phone || cust.email || cust.fullName || "";
      sessionStorage.removeItem("pos_customer");
      toast(`Đã gán khách hàng: ${cust.fullName || cust.name}`, "success");
    }
  } catch { /* ignore */ }
});

onUnmounted(() => {
  window.removeEventListener("keydown", onKey);
  clearTimeout(searchTimer);
});
</script>

<style scoped>
/* ── Root ── */
.pos-root {
  display: flex;
  height: 100%;
  width: 100%;
  background: #f1f5f9;
  font-family: 'Inter', 'Segoe UI', system-ui, sans-serif;
  overflow: hidden;
}

/* ── Panels ── */
.panel { display: flex; flex-direction: column; overflow: hidden; }

.panel-left {
  flex: 1;
  padding: 20px;
  gap: 14px;
  background: #f1f5f9;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #e2e8f0 transparent;
}
.panel-left::-webkit-scrollbar { width: 4px; }
.panel-left::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

.panel-right {
  width: 360px;
  min-width: 360px;
  background: #ffffff;
  border-left: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── Search bar ── */
.search-bar { display: flex; gap: 9px; flex-shrink: 0; margin-bottom: 4px; }
.search-wrap {
  flex: 1; display: flex; align-items: center; gap: 9px;
  background: #fff; border: 1.5px solid #e2e8f0;
  border-radius: 10px; padding: 0 13px; height: 46px;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.search-wrap.focused { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245,158,11,0.1); }
.s-icon { color: #94a3b8; flex-shrink: 0; }
.search-wrap.focused .s-icon { color: #f59e0b; }
.s-input {
  flex: 1; background: transparent; border: none; outline: none;
  color: #0f172a; font-size: 14px; font-family: inherit; min-width: 0;
}
.s-input::placeholder { color: #cbd5e1; }
.s-hint { font-size: 11px; color: #94a3b8; background: #f1f5f9; padding: 2px 6px; border-radius: 4px; flex-shrink: 0; font-family: monospace; }
.s-clear { background: none; border: none; cursor: pointer; color: #94a3b8; display: flex; padding: 3px; transition: color 0.15s; }
.s-clear:hover { color: #475569; }

.error-bar {
  display: flex; align-items: center; gap: 8px;
  background: #fef2f2; border: 1px solid #fecaca;
  color: #dc2626; font-size: 12.5px; padding: 10px 14px; border-radius: 8px;
}

.loading-state {
  display: flex; align-items: center; gap: 10px;
  padding: 16px; color: #64748b; font-size: 13px;
}

/* ── Results ── */
.results-scroll { display: flex; flex-direction: column; gap: 12px; }
.results-meta { font-size: 12.5px; color: #64748b; margin: 0; }
.results-meta strong { color: #0f172a; }

.product-card {
  background: #fff; border: 1px solid #e2e8f0;
  border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.product-header {
  display: flex; align-items: center; gap: 8px;
  padding: 11px 16px; background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}
.product-name { font-size: 14px; font-weight: 700; color: #0f172a; }

.variant-row {
  padding: 12px 16px;
  border-bottom: 1px solid #f8fafc;
}
.variant-row:last-child { border-bottom: none; }
.variant-meta {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 8px; flex-wrap: wrap;
}
.variant-name { font-size: 13px; font-weight: 600; color: #334155; }
.variant-price { font-size: 13px; font-weight: 700; color: #d97706; }

/* Serial / stock badge */
.serial-badge {
  margin-left: auto; font-size: 11px;
  background: #f1f5f9; padding: 3px 9px; border-radius: 10px;
  display: flex; align-items: center; gap: 5px; font-weight: 600;
  color: #64748b;
}
.serial-badge--ok  { background: #dcfce7; color: #15803d; }
.serial-badge--out { background: #fef2f2; color: #dc2626; }

.serial-chips { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 4px 10px;
  background: #f8fafc; border: 1.5px solid #e2e8f0;
  border-radius: 6px; color: #334155;
  font-size: 11.5px; font-weight: 600;
  font-family: 'JetBrains Mono', 'Fira Code', ui-monospace, monospace;
  letter-spacing: 0.04em; cursor: pointer;
  transition: all 0.15s;
}
.chip:hover:not(:disabled):not(.added) {
  background: #fffbeb; border-color: #fde68a; color: #b45309;
}
.chip.added { background: #dcfce7; border-color: #86efac; color: #15803d; cursor: default; }
.chip:disabled { cursor: not-allowed; }
.check-icon { flex-shrink: 0; }

.no-serials { font-size: 12px; color: #94a3b8; margin: 4px 0 0; font-style: italic; }

/* ── Empty ── */
.empty-state {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 10px; text-align: center; padding: 40px;
}
.empty-icon { color: #cbd5e1; margin-bottom: 8px; }
.empty-title { font-size: 15px; font-weight: 600; color: #64748b; margin: 0; }
.empty-sub { font-size: 13px; color: #94a3b8; margin: 0; }

/* ── Cart panel ── */
.cart-header {
  display: flex; align-items: center; gap: 8px;
  padding: 14px 16px 12px; border-bottom: 1px solid #f1f5f9; flex-shrink: 0;
}
.cart-title {
  display: flex; align-items: center; gap: 7px;
  font-size: 14px; font-weight: 700; color: #0f172a; margin: 0;
}
.cart-count {
  font-size: 11px; font-weight: 700; color: #64748b;
  background: #f1f5f9; border-radius: 20px; padding: 1px 8px;
}
.btn-clear-cart {
  margin-left: auto; display: flex; align-items: center; gap: 5px;
  background: none; border: none; color: #94a3b8;
  font-size: 12px; cursor: pointer; padding: 4px 8px; border-radius: 6px;
  transition: all 0.15s; font-family: inherit;
}
.btn-clear-cart:hover { background: #fef2f2; color: #dc2626; }

.cart-body {
  flex: 1; overflow-y: auto;
  scrollbar-width: thin; scrollbar-color: #f1f5f9 transparent;
}
.cart-body::-webkit-scrollbar { width: 4px; }
.cart-body::-webkit-scrollbar-thumb { background: #f1f5f9; border-radius: 4px; }

.cart-empty {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 8px; min-height: 100px;
  color: #cbd5e1; font-size: 13px;
}

.cart-list { display: flex; flex-direction: column; }
.cart-item {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 11px 16px; border-bottom: 1px solid #f8fafc;
  transition: background 0.15s;
}
.cart-item:hover { background: #fafafa; }
.ci-info { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.ci-name { font-size: 13px; font-weight: 600; color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ci-variant { font-size: 11.5px; color: #64748b; }
.ci-serial { font-size: 10.5px; font-family: monospace; color: #d97706; letter-spacing: 0.04em; }
.ci-right { display: flex; flex-direction: column; align-items: flex-end; gap: 5px; flex-shrink: 0; }
.ci-price { font-size: 13px; font-weight: 700; color: #0f172a; }
.btn-remove {
  background: none; border: none; color: #cbd5e1; cursor: pointer;
  padding: 3px; display: flex; border-radius: 5px; transition: all 0.15s;
}
.btn-remove:hover { background: #fef2f2; color: #dc2626; }

/* Cart item animation */
.cart-anim-enter-active { transition: all 0.22s ease; }
.cart-anim-leave-active { transition: all 0.18s ease; }
.cart-anim-enter-from { opacity: 0; transform: translateX(12px); }
.cart-anim-leave-to { opacity: 0; transform: translateX(-12px); }

/* ── Customer section ── */
.customer-section {
  padding: 12px 16px; border-top: 1px solid #f1f5f9;
  display: flex; flex-direction: column; gap: 7px; flex-shrink: 0;
}
.section-label {
  display: flex; align-items: center; gap: 6px;
  font-size: 11px; font-weight: 600; color: #94a3b8;
  letter-spacing: 0.08em; text-transform: uppercase;
}
.optional { font-size: 10px; font-weight: 400; color: #cbd5e1; text-transform: none; letter-spacing: 0; }
.cus-row { display: flex; gap: 7px; }
.cus-input-wrap {
  flex: 1; background: #f8fafc; border: 1px solid #e2e8f0;
  border-radius: 8px; height: 36px;
  display: flex; align-items: center; padding: 0 11px;
  transition: border-color 0.2s;
}
.cus-input-wrap.focused { border-color: #f59e0b; }
.cus-input {
  flex: 1; background: transparent; border: none; outline: none;
  color: #0f172a; font-size: 13px; font-family: inherit;
}
.cus-input::placeholder { color: #cbd5e1; }
.btn-lookup {
  height: 36px; padding: 0 13px;
  background: #fef3c7; border: 1px solid #fde68a;
  border-radius: 8px; color: #b45309;
  font-size: 12.5px; font-weight: 600; cursor: pointer;
  transition: all 0.15s; font-family: inherit;
  display: flex; align-items: center; justify-content: center; min-width: 40px;
}
.btn-lookup:hover:not(:disabled) { background: #fde68a; }
.btn-lookup:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-remove-cus {
  height: 36px; width: 36px;
  background: #f8fafc; border: 1px solid #e2e8f0;
  border-radius: 8px; color: #94a3b8; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s; flex-shrink: 0;
}
.btn-remove-cus:hover { background: #fef2f2; color: #dc2626; border-color: #fecaca; }
.cus-chip {
  display: flex; align-items: center; gap: 9px;
  background: #fffbeb; border: 1px solid #fde68a;
  border-radius: 8px; padding: 7px 11px;
}
.cus-avatar {
  width: 26px; height: 26px; border-radius: 6px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  display: flex; align-items: center; justify-content: center;
  font-size: 9.5px; font-weight: 700; color: #fff; flex-shrink: 0;
}
.cus-info { flex: 1; display: flex; flex-direction: column; gap: 1px; min-width: 0; }
.cus-name { font-size: 12px; font-weight: 600; color: #0f172a; }
.cus-meta { font-size: 11px; color: #64748b; }
.cus-tier {
  font-size: 9.5px; font-weight: 700; color: #b45309;
  background: #fef3c7; border: 1px solid #fde68a;
  padding: 2px 7px; border-radius: 10px; flex-shrink: 0;
}
.cus-error { font-size: 12px; color: #dc2626; margin: 0; }

/* ── Total ── */
.total-section {
  padding: 12px 16px 8px; border-top: 1px solid #e2e8f0;
  flex-shrink: 0; background: #fafafa;
}
.total-row { display: flex; align-items: baseline; justify-content: space-between; }
.total-label { font-size: 13px; font-weight: 600; color: #64748b; }
.total-value { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; }
.total-sub { font-size: 11px; color: #94a3b8; margin: 2px 0 0; text-align: right; }

/* Pay button */
.pay-wrap { padding: 10px 14px 14px; flex-shrink: 0; display: flex; flex-direction: column; gap: 8px; }
.cus-required {
  display: flex; align-items: center; gap: 7px;
  background: #fef3c7; border: 1px solid #fde68a;
  color: #92400e; font-size: 12px; font-weight: 500;
  padding: 8px 12px; border-radius: 8px;
}
.btn-pay {
  width: 100%; height: 50px;
  display: flex; align-items: center; justify-content: center; gap: 9px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none; border-radius: 10px; color: #fff;
  font-size: 14px; font-weight: 800; cursor: pointer;
  transition: opacity 0.15s, transform 0.1s;
  font-family: inherit; box-shadow: 0 4px 14px rgba(245,158,11,0.3);
}
.btn-pay:hover:not(.disabled) { opacity: 0.9; transform: translateY(-1px); }
.btn-pay.disabled { background: #e2e8f0; color: #94a3b8; box-shadow: none; cursor: not-allowed; }

/* ── Payment modal ── */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.35); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 200; padding: 20px;
}
.payment-modal {
  background: #fff; border: 1px solid #e2e8f0;
  border-radius: 16px; width: 100%; max-width: 430px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.12); overflow: hidden;
}
.modal-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px 14px; border-bottom: 1px solid #f1f5f9;
}
.modal-title { font-size: 16px; font-weight: 700; color: #0f172a; margin: 0; }
.modal-close {
  background: none; border: none; color: #94a3b8; cursor: pointer;
  display: flex; padding: 4px; border-radius: 6px; transition: all 0.15s;
}
.modal-close:hover { background: #f1f5f9; color: #475569; }
.modal-amount-box {
  display: flex; flex-direction: column; align-items: center; gap: 3px;
  padding: 18px 20px; background: #fffbeb; border-bottom: 1px solid #fef3c7;
}
.modal-amount-label { font-size: 11.5px; color: #92400e; font-weight: 500; }
.modal-amount-value { font-size: 30px; font-weight: 800; color: #b45309; letter-spacing: -1px; }

.method-tabs { display: flex; gap: 8px; padding: 14px 20px 10px; }
.method-tab {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 9px 8px; background: #f8fafc; border: 1px solid #e2e8f0;
  border-radius: 9px; color: #64748b; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all 0.15s; font-family: inherit;
}
.method-tab:hover { background: #f1f5f9; }
.method-tab.active { background: #fef3c7; border-color: #fde68a; color: #b45309; }

.method-body { padding: 4px 20px 12px; }
.field-lbl { font-size: 11px; font-weight: 600; color: #64748b; letter-spacing: 0.07em; text-transform: uppercase; display: block; margin-bottom: 7px; }
.cash-wrap {
  display: flex; align-items: center; gap: 8px;
  background: #f8fafc; border: 1.5px solid #e2e8f0;
  border-radius: 9px; height: 48px; padding: 0 13px;
  transition: border-color 0.2s;
}
.cash-wrap:focus-within { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245,158,11,0.08); }
.cash-input {
  flex: 1; background: transparent; border: none; outline: none;
  color: #0f172a; font-size: 20px; font-weight: 700;
  text-align: right; font-family: inherit;
}
.cash-unit { font-size: 14px; font-weight: 600; color: #94a3b8; }
.quick-cash { display: flex; gap: 6px; margin-top: 9px; flex-wrap: wrap; }
.quick-btn {
  padding: 5px 11px; background: #f8fafc; border: 1px solid #e2e8f0;
  border-radius: 6px; color: #475569; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all 0.15s; font-family: inherit;
}
.quick-btn:hover { background: #fef3c7; border-color: #fde68a; color: #b45309; }
.cash-change {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 10px; padding: 9px 13px;
  background: #dcfce7; border: 1px solid #bbf7d0; border-radius: 8px;
  font-size: 12.5px; font-weight: 600; color: #15803d;
}
.cash-lack {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 10px; padding: 9px 13px;
  background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px;
  font-size: 12.5px; font-weight: 600; color: #dc2626;
}
.info-box {
  display: flex; align-items: center; gap: 14px;
  padding: 14px; background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px;
}
.info-title { font-size: 13.5px; font-weight: 700; color: #0f172a; margin: 0 0 3px; }
.info-sub { font-size: 12px; color: #64748b; margin: 0; line-height: 1.5; }

.pay-error {
  margin: 0 20px 8px; padding: 9px 13px;
  background: #fef2f2; border: 1px solid #fecaca;
  border-radius: 8px; color: #dc2626; font-size: 12.5px;
}
.modal-footer { padding: 8px 20px 18px; }
.btn-confirm {
  width: 100%; height: 48px;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none; border-radius: 10px; color: #fff;
  font-size: 14px; font-weight: 800; cursor: pointer;
  transition: opacity 0.15s; font-family: inherit;
  box-shadow: 0 3px 12px rgba(245,158,11,0.3);
}
.btn-confirm span { display: flex; align-items: center; gap: 8px; }
.btn-confirm:hover:not(:disabled) { opacity: 0.9; }
.btn-confirm:disabled { opacity: 0.35; cursor: not-allowed; box-shadow: none; }

/* ── Success modal ── */
.success-modal {
  background: #fff; border: 1px solid #bbf7d0;
  border-radius: 16px; padding: 36px 28px; text-align: center;
  width: 100%; max-width: 340px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}
.success-icon { margin-bottom: 14px; }
.success-title { font-size: 19px; font-weight: 800; color: #0f172a; margin: 0 0 6px; }
.success-id { font-size: 13px; color: #64748b; margin: 0 0 4px; }
.success-amount { font-size: 26px; font-weight: 800; color: #16a34a; margin: 0 0 10px; }
.success-change { font-size: 13px; color: #64748b; margin-bottom: 22px; }
.success-change strong { color: #0f172a; }
.btn-new {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 11px 26px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none; border-radius: 10px; color: #fff;
  font-size: 13.5px; font-weight: 700; cursor: pointer;
  transition: opacity 0.15s; font-family: inherit;
  box-shadow: 0 3px 12px rgba(245,158,11,0.25);
}
.btn-new:hover { opacity: 0.9; }

/* ── Spinner ── */
.spinner {
  display: inline-block; width: 14px; height: 14px;
  border-radius: 50%; animation: spin 0.6s linear infinite;
}
.spinner.white { border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; }
.spinner.amber { border: 2px solid rgba(245,158,11,0.25); border-top-color: #d97706; }
.spinner.tiny { width: 11px; height: 11px; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Transitions ── */
.fade-down-enter-active { transition: all 0.22s ease; }
.fade-down-leave-active { transition: all 0.15s ease; }
.fade-down-enter-from { opacity: 0; transform: translateY(-5px); }
.fade-down-leave-to { opacity: 0; }
.modal-enter-active, .modal-leave-active { transition: opacity 0.22s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-active .payment-modal,
.modal-enter-active .success-modal { transition: transform 0.25s cubic-bezier(0.34,1.3,0.64,1); }
.modal-enter-from .payment-modal,
.modal-enter-from .success-modal { transform: scale(0.95) translateY(10px); }
</style>