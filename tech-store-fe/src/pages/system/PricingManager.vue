<template>
  <div class="pm">
    <!-- Quick Navigator -->
    <QuickNav page="pricing" :onSwitchTab="handleQuickNavTab" />

    <!-- Header -->
    <header class="pm-header">
      <div class="pm-header-left">
        <div class="pm-breadcrumb">SYSTEM / PRICING</div>
        <h1 class="pm-title">Quản lý giá <span class="pm-title-accent">& Xung đột</span></h1>
      </div>
      <div v-if="dashData" class="pm-header-pills">
        <div class="pm-pill pm-pill-green">
          <span class="pm-pill-dot"></span>{{ dashData.activePromotions ?? 0 }} KM active
        </div>
        <div class="pm-pill" :class="(dashData.variantsBelowCost ?? 0) > 0 ? 'pm-pill-red' : 'pm-pill-dim'">
          <span class="pm-pill-dot"></span>{{ dashData.variantsBelowCost ?? 0 }} dưới giá nhập
        </div>
        <div class="pm-pill" :class="(dashData.expiringIn3Days ?? 0) > 0 ? 'pm-pill-orange' : 'pm-pill-dim'">
          <span class="pm-pill-dot"></span>{{ dashData.expiringIn3Days ?? 0 }} sắp hết hạn
        </div>
      </div>
    </header>

    <!-- Tab bar -->
    <div class="pm-tabbar">
      <button v-for="t in tabs" :key="t.key" class="pm-tab" :class="{ active: activeTab === t.key }" @click="activeTab = t.key">
        <span class="pm-tab-icon">{{ t.icon }}</span>{{ t.label }}
      </button>
    </div>

    <!-- ===== TAB: ĐẶT GIÁ ===== -->
    <div v-show="activeTab === 'set'" class="pm-pane">
      <div class="pm-grid2">
        <div class="pm-card">
          <div class="pm-card-head">
            <span class="pm-card-title">Đặt giá variant</span>
            <code class="pm-badge">POST /variants/{id}</code>
          </div>
          <div class="pm-fields">
            <div class="pm-row2">
              <div class="pm-field">
                <label class="pm-lbl">Variant ID <span class="pm-req">*</span></label>
                <input v-model.number="setForm.variantId" class="pm-in" placeholder="5" type="number" />
              </div>
              <div class="pm-field">
                <label class="pm-lbl">Currency</label>
                <input v-model="setForm.currencyCode" class="pm-in" placeholder="VND" />
              </div>
            </div>
            <div class="pm-row2">
              <div class="pm-field">
                <label class="pm-lbl">Giá bán</label>
                <input v-model.number="setForm.price" class="pm-in pm-in-money" placeholder="42900000" type="number" />
                <span v-if="setForm.price" class="pm-in-hint">{{ fmtMoney(setForm.price) }}</span>
              </div>
              <div class="pm-field">
                <label class="pm-lbl">Giá nhập</label>
                <input v-model.number="setForm.costPrice" class="pm-in pm-in-money" placeholder="36000000" type="number" />
                <span v-if="setForm.costPrice" class="pm-in-hint">{{ fmtMoney(setForm.costPrice) }}</span>
              </div>
            </div>
            <div class="pm-field">
              <label class="pm-lbl">Lý do</label>
              <input v-model="setForm.reason" class="pm-in" placeholder="MANUAL / PROMO_ENDED / PRICE_UPDATE" />
            </div>
          </div>
          <div class="pm-foot">
            <button class="pm-btn pm-btn-primary" :class="{ loading: setLoading }" @click="setPrice" :disabled="setLoading">
              <span v-if="!setLoading">💾 Lưu giá</span><span v-else class="pm-spin"></span>
            </button>
          </div>
        </div>

        <div class="pm-card">
          <div class="pm-card-head">
            <span class="pm-card-title">Giá hiệu lực</span>
            <code class="pm-badge">GET /effective</code>
          </div>
          <p id="p-anchor-effective" class="pm-hint-text">Dùng Variant ID đã nhập bên trái.</p>
          <button class="pm-btn pm-btn-outline" :class="{ loading: effLoading }" @click="loadEffective" :disabled="effLoading">
            <span v-if="!effLoading">⚡ Tải giá hiệu lực</span><span v-else class="pm-spin"></span>
          </button>
          <div v-if="effective" class="pm-result mt-3">
            <div class="pm-rrow"><span class="pm-rk">SKU</span><span class="pm-rv mono">{{ effective.data?.sku ?? effective.sku ?? "—" }}</span></div>
            <div class="pm-rrow"><span class="pm-rk">Base price</span><span class="pm-rv">{{ fmtMoney(effective.data?.price ?? effective.price) }}</span></div>
            <div class="pm-rrow pm-rrow-hl"><span class="pm-rk">Final price</span><span class="pm-rv clr-green">{{ fmtMoney(effective.data?.finalPrice ?? effective.finalPrice) }}</span></div>
            <div class="pm-rrow"><span class="pm-rk">Discount</span><span class="pm-rv clr-red">-{{ fmtMoney(effective.data?.discountAmount ?? effective.discountAmount) }}</span></div>
            <div class="pm-rrow"><span class="pm-rk">Promotion</span><span class="pm-rv">
              <span v-if="effective.data?.promotionCode ?? effective.promotionCode" class="pm-tag pm-tag-y">{{ effective.data?.promotionCode ?? effective.promotionCode }}</span>
              <span v-else class="pm-dim">none</span>
            </span></div>
            <div v-if="effective.data?.comboInfo ?? effective.comboInfo" class="pm-rrow">
              <span class="pm-rk">Combo</span>
              <span class="pm-rv"><span class="pm-tag pm-tag-g">Mua {{ (effective.data?.comboInfo ?? effective.comboInfo)?.buyQty }} tặng {{ (effective.data?.comboInfo ?? effective.comboInfo)?.getQty }}</span></span>
            </div>
          </div>

          <div class="pm-divider"></div>

          <div class="pm-card-head" style="margin-bottom:10px">
            <span id="p-anchor-custprice" class="pm-card-title" style="font-size:12px">Giá theo khách hàng</span>
            <code class="pm-badge">GET /effective/customer/{id}</code>
          </div>
          <div class="pm-inline">
            <input v-model.number="customerIdForPrice" class="pm-in" placeholder="Customer ID" style="width:130px" type="number" />
            <button class="pm-btn pm-btn-outline" :class="{ loading: effCustLoading }" @click="loadEffectiveForCustomer" :disabled="effCustLoading">
              <span v-if="!effCustLoading">Tải</span><span v-else class="pm-spin"></span>
            </button>
          </div>
          <div v-if="effectiveForCustomer" class="pm-result mt-2">
            <div class="pm-rrow"><span class="pm-rk">Variant</span><span class="pm-rv">{{ effectiveForCustomer.data?.variantName ?? effectiveForCustomer.variantName ?? "—" }}</span></div>
            <div class="pm-rrow"><span class="pm-rk">Base price</span><span class="pm-rv">{{ fmtMoney(effectiveForCustomer.data?.price ?? effectiveForCustomer.price) }}</span></div>
            <div class="pm-rrow pm-rrow-hl"><span class="pm-rk">Final price</span><span class="pm-rv clr-green">{{ fmtMoney(effectiveForCustomer.data?.finalPrice ?? effectiveForCustomer.finalPrice) }}</span></div>
            <div class="pm-rrow"><span class="pm-rk">Promotion</span><span class="pm-rv">
              <span v-if="effectiveForCustomer.data?.promotionCode ?? effectiveForCustomer.promotionCode" class="pm-tag pm-tag-y">{{ effectiveForCustomer.data?.promotionCode ?? effectiveForCustomer.promotionCode }}</span>
              <span v-else class="pm-dim">none</span>
            </span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== TAB: LỊCH SỬ GIÁ ===== -->
    <div v-show="activeTab === 'history'" class="pm-pane">
      <div id="p-anchor-prodprices" class="pm-card mb3">
        <div class="pm-card-head">
          <span class="pm-card-title">Giá theo sản phẩm</span>
          <code class="pm-badge">GET /products/{productId}</code>
        </div>
        <div class="pm-inline">
          <input v-model.number="productId" class="pm-in" placeholder="Product ID" style="width:140px" type="number" />
          <button class="pm-btn pm-btn-primary" :class="{ loading: prodLoading }" @click="loadByProduct" :disabled="prodLoading">
            <span v-if="!prodLoading">Tải</span><span v-else class="pm-spin"></span>
          </button>
        </div>
        <div v-if="productPrices.length" class="pm-tw mt3">
          <table class="pm-tbl">
            <thead><tr><th>Variant ID</th><th>Tên variant</th><th>SKU</th><th>Base price</th><th>Final price</th><th>Promotion</th></tr></thead>
            <tbody>
              <tr v-for="row in productPrices" :key="row.variantId">
                <td class="mono">{{ row.variantId }}</td>
                <td>{{ row.variantName }}</td>
                <td class="mono dim">{{ row.sku }}</td>
                <td>{{ fmtMoney(row.price) }}</td>
                <td :class="row.finalPrice < row.price ? 'clr-green bold' : ''">{{ fmtMoney(row.finalPrice) }}</td>
                <td><span v-if="row.promotionCode" class="pm-tag pm-tag-y">{{ row.promotionCode }}</span><span v-else class="pm-dim">—</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="pm-card">
        <div class="pm-card-head">
          <span class="pm-card-title" id="p-anchor-history">Lịch sử giá variant</span>
          <code class="pm-badge">GET /variants/{id}/history</code>
        </div>
        <div class="pm-inline">
          <input v-model.number="histVariantId" class="pm-in" placeholder="Variant ID" style="width:140px" type="number" />
          <button class="pm-btn pm-btn-primary" :class="{ loading: histLoading }" @click="loadVariantHistory" :disabled="histLoading">
            <span v-if="!histLoading">Tải lịch sử</span><span v-else class="pm-spin"></span>
          </button>
        </div>
        <div v-if="variantHistory.length" class="pm-tw mt3">
          <table class="pm-tbl">
            <thead><tr><th>ID</th><th>Giá bán</th><th>Giá nhập</th><th>Lý do</th><th>Hiệu lực từ</th><th>Hiệu lực đến</th><th>Người tạo</th><th>Action</th></tr></thead>
            <tbody>
              <tr v-for="row in variantHistory" :key="row.id">
                <td class="mono dim">{{ row.id }}</td>
                <td class="bold">{{ fmtMoney(row.price) }}</td>
                <td>{{ row.costPrice ? fmtMoney(row.costPrice) : "—" }}</td>
                <td><span class="pm-tag pm-tag-b">{{ row.reason || "—" }}</span></td>
                <td class="mono dim small">{{ fmtDate(row.effectiveFrom) }}</td>
                <td class="mono dim small">{{ row.effectiveTo ? fmtDate(row.effectiveTo) : "—" }}</td>
                <td>{{ row.createdByUsername || "—" }}</td>
                <td><div class="pm-acts"><button class="pm-xs pm-xs-o" @click="openEdit(row)">Sửa</button><button class="pm-xs pm-xs-d" @click="removeHistory(row)">Xóa</button></div></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else-if="histLoaded" class="pm-empty">Không có bản ghi nào.</div>
      </div>
    </div>

    <!-- ===== TAB: CẢNH BÁO ===== -->
    <div v-show="activeTab === 'warning'" class="pm-pane">
      <div class="pm-grid2">
        <div class="pm-card">
          <div class="pm-card-head">
            <span class="pm-card-title">Cảnh báo giá dưới giá nhập</span>
            <code class="pm-badge pm-badge-o">GET /cost-warning</code>
          </div>
          <div class="pm-inline">
            <input v-model.number="costWarnVariantId" class="pm-in" placeholder="Variant ID" style="width:140px" type="number" />
            <button class="pm-btn pm-btn-orange" :class="{ loading: costWarnLoading }" @click="loadCostWarning" :disabled="costWarnLoading">
              <span v-if="!costWarnLoading">⚠️ Kiểm tra</span><span v-else class="pm-spin"></span>
            </button>
          </div>
          <div v-if="costWarning" class="mt3">
            <div class="pm-alert" :class="costWarning.belowCost ? 'pm-alert-r' : 'pm-alert-g'">
              {{ costWarning.belowCost ? "⚠️ " + (costWarning.message || "Giá bán THẤP hơn giá nhập!") : "✅ Giá bán hợp lệ" }}
            </div>
            <div class="pm-result mt2">
              <div class="pm-rrow"><span class="pm-rk">Variant ID</span><span class="pm-rv mono">{{ costWarning.variantId }}</span></div>
              <div class="pm-rrow"><span class="pm-rk">Giá bán</span><span class="pm-rv" :class="costWarning.belowCost ? 'clr-red' : ''">{{ fmtMoney(costWarning.price) }}</span></div>
              <div class="pm-rrow"><span class="pm-rk">Giá nhập</span><span class="pm-rv">{{ costWarning.costPrice ? fmtMoney(costWarning.costPrice) : "—" }}</span></div>
            </div>
          </div>
        </div>

        <div id="p-anchor-promoend" class="pm-card">
          <div class="pm-card-head">
            <span class="pm-card-title">Cập nhật giá sau khuyến mãi</span>
            <code class="pm-badge">PUT /history/{id}</code>
          </div>
          <div class="pm-fields">
            <div class="pm-row2">
              <div class="pm-field">
                <label class="pm-lbl">History ID <span class="pm-req">*</span></label>
                <input v-model.number="promoEndForm.id" class="pm-in" placeholder="ID bản ghi" type="number" />
              </div>
              <div class="pm-field">
                <label class="pm-lbl">Giá mới</label>
                <input v-model.number="promoEndForm.price" class="pm-in pm-in-money" placeholder="19500000" type="number" />
                <span v-if="promoEndForm.price" class="pm-in-hint">{{ fmtMoney(promoEndForm.price) }}</span>
              </div>
            </div>
            <div class="pm-field">
              <label class="pm-lbl">Lý do</label>
              <input v-model="promoEndForm.reason" class="pm-in" placeholder="PROMO_ENDED" />
            </div>
          </div>
          <div class="pm-foot">
            <button class="pm-btn pm-btn-primary" :class="{ loading: promoEndLoading }" @click="submitPromoEnd" :disabled="promoEndLoading">
              <span v-if="!promoEndLoading">🔄 Cập nhật giá</span><span v-else class="pm-spin"></span>
            </button>
          </div>
          <div v-if="promoEndResult" class="pm-result mt3">
            <div class="pm-rrow pm-rrow-hl"><span class="pm-rk">Giá mới</span><span class="pm-rv clr-green">{{ fmtMoney(promoEndResult.price) }}</span></div>
            <div class="pm-rrow"><span class="pm-rk">Lý do</span><span class="pm-rv"><span class="pm-tag pm-tag-b">{{ promoEndResult.reason }}</span></span></div>
            <div class="pm-rrow"><span class="pm-rk">Người tạo</span><span class="pm-rv">{{ promoEndResult.createdByUsername ?? "—" }}</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== TAB: XUNG ĐỘT GIÁ ===== -->
    <div v-show="activeTab === 'conflicts'" class="pm-pane">
      <div class="pm-card">
        <div class="pm-card-head">
          <span class="pm-card-title">Variant có giá bán &lt; giá nhập</span>
          <code class="pm-badge pm-badge-r">GET /api/prices/conflicts</code>
          <button class="pm-btn pm-btn-orange ml-auto" :class="{ loading: conflictsLoading }" @click="loadConflicts" :disabled="conflictsLoading">
            <span v-if="!conflictsLoading">🔍 Quét toàn hệ thống</span><span v-else class="pm-spin"></span>
          </button>
        </div>
        <template v-if="conflictsLoaded">
          <div v-if="!priceConflicts.length" class="pm-alert pm-alert-g mt3">✅ Không phát hiện xung đột giá nào.</div>
          <template v-else>
            <div class="pm-alert pm-alert-r mt3">⚠️ {{ priceConflicts.length }} variant có giá bán thấp hơn giá nhập</div>
            <div class="pm-tw mt3">
              <table class="pm-tbl">
                <thead><tr><th>Variant ID</th><th>Tên variant</th><th>SKU</th><th>Giá bán</th><th>Giá nhập</th><th>Chênh lệch</th><th>Currency</th></tr></thead>
                <tbody>
                  <tr v-for="row in priceConflicts" :key="row.variantId" class="row-danger">
                    <td class="mono">{{ row.variantId }}</td>
                    <td>{{ row.variantName }}</td>
                    <td class="mono dim">{{ row.sku }}</td>
                    <td class="clr-red bold">{{ fmtMoney(row.price) }}</td>
                    <td>{{ fmtMoney(row.costPrice) }}</td>
                    <td class="clr-red">{{ fmtMoney(row.price - row.costPrice) }}</td>
                    <td class="mono dim">{{ row.currencyCode }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </template>
        <div v-else class="pm-empty mt3">Nhấn "Quét toàn hệ thống" để bắt đầu kiểm tra.</div>
      </div>
    </div>

    <!-- ===== TAB: TÍNH GIÁ ĐƠN ===== -->
    <div v-show="activeTab === 'calculate'" class="pm-pane">
      <div class="pm-grid2">
        <div class="pm-card">
          <div class="pm-card-head">
            <span class="pm-card-title">Giỏ hàng</span>
            <code class="pm-badge">POST /calculate-order</code>
          </div>
          <div class="pm-field">
            <label class="pm-lbl">Customer ID <span class="pm-dim">(tùy chọn)</span></label>
            <input v-model.number="calcForm.customerId" class="pm-in" placeholder="VD: 1 — Nguyễn Minh Tuấn VIP Gold" type="number" />
          </div>
          <div class="pm-field mt3">
            <label class="pm-lbl">Sản phẩm</label>
            <div class="pm-items">
              <div v-for="(item, idx) in calcForm.items" :key="idx" class="pm-item-row">
                <span class="pm-item-n">{{ idx + 1 }}</span>
                <input v-model.number="item.variantId" class="pm-in" placeholder="Variant ID" style="width:120px" type="number" />
                <span class="pm-item-sep">×</span>
                <input v-model.number="item.quantity" class="pm-in" placeholder="SL" style="width:60px" type="number" :min="1" />
                <button class="pm-icon-btn" @click="calcForm.items.splice(idx, 1)">✕</button>
              </div>
            </div>
            <button class="pm-btn pm-btn-ghost mt2" @click="calcForm.items.push({ variantId: null, quantity: 1 })">+ Thêm sản phẩm</button>
          </div>
          <div class="pm-foot">
            <button class="pm-btn pm-btn-primary" :class="{ loading: calcLoading }" @click="calculateOrder" :disabled="calcLoading">
              <span v-if="!calcLoading">🧮 Tính giá</span><span v-else class="pm-spin"></span>
            </button>
          </div>
        </div>

        <div class="pm-card">
          <div class="pm-card-head"><span class="pm-card-title">Kết quả tính giá</span></div>
          <div v-if="calcResult">
            <div class="pm-calc-box">
              <div class="pm-calc-r"><span>Tổng giá gốc</span><span>{{ fmtMoney(calcResult.subtotal) }}</span></div>
              <div class="pm-calc-r pm-calc-r-disc"><span>Giảm giá</span><span>-{{ fmtMoney(calcResult.discountTotal) }}</span></div>
              <div v-if="calcResult.freeQty" class="pm-calc-r"><span>Tặng kèm</span><span class="pm-tag pm-tag-g">{{ calcResult.freeQty }} sp miễn phí</span></div>
              <div class="pm-calc-sep"></div>
              <div class="pm-calc-r pm-calc-r-total"><span>Tổng thanh toán</span><span class="clr-green">{{ fmtMoney(calcResult.finalTotal) }}</span></div>
              <div v-if="calcResult.promotionCode" class="pm-calc-r"><span>Mã KM</span><span class="pm-tag pm-tag-y">{{ calcResult.promotionCode }}</span></div>
            </div>
            <div v-if="calcResult.items?.length" class="pm-tw mt3">
              <table class="pm-tbl pm-tbl-sm">
                <thead><tr><th>Variant</th><th>Tên</th><th>SL</th><th>Đơn giá</th><th>Sau KM</th><th>Thành tiền</th></tr></thead>
                <tbody>
                  <tr v-for="item in calcResult.items" :key="item.variantId">
                    <td class="mono dim">{{ item.variantId }}</td>
                    <td>{{ item.variantName }}</td>
                    <td class="center">{{ item.quantity }}</td>
                    <td>{{ fmtMoney(item.unitPrice) }}</td>
                    <td :class="item.finalUnitPrice < item.unitPrice ? 'clr-green bold' : ''">{{ fmtMoney(item.finalUnitPrice) }}</td>
                    <td class="bold">{{ fmtMoney(item.lineTotal) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div v-else class="pm-empty">Nhập sản phẩm và nhấn "Tính giá".</div>
        </div>
      </div>
    </div>

    <!-- ===== TAB: DASHBOARD ===== -->
    <div v-show="activeTab === 'dashboard'" class="pm-pane">
      <div class="pm-inline mb3">
        <code class="pm-badge">GET /api/prices/dashboard</code>
        <button class="pm-btn pm-btn-outline ml-auto" :class="{ loading: dashLoading }" @click="loadDashboard" :disabled="dashLoading">
          <span v-if="!dashLoading">🔄 Làm mới</span><span v-else class="pm-spin"></span>
        </button>
      </div>
      <template v-if="dashData">
        <div class="pm-stats4">
          <div class="pm-stat pm-stat-g"><div class="pm-stat-ico">🏷️</div><div class="pm-stat-n">{{ dashData.activePromotions ?? 0 }}</div><div class="pm-stat-l">KM đang chạy</div></div>
          <div class="pm-stat" :class="(dashData.variantsBelowCost ?? 0) > 0 ? 'pm-stat-r' : 'pm-stat-dim'"><div class="pm-stat-ico">⚠️</div><div class="pm-stat-n">{{ dashData.variantsBelowCost ?? 0 }}</div><div class="pm-stat-l">Giá dưới giá nhập</div></div>
          <div class="pm-stat" :class="(dashData.expiringIn3Days ?? 0) > 0 ? 'pm-stat-o' : 'pm-stat-dim'"><div class="pm-stat-ico">⏰</div><div class="pm-stat-n">{{ dashData.expiringIn3Days ?? 0 }}</div><div class="pm-stat-l">Sắp hết hạn (3 ngày)</div></div>
          <div class="pm-stat" :class="(dashData.conflictCount ?? 0) > 0 ? 'pm-stat-p' : 'pm-stat-dim'"><div class="pm-stat-ico">🔀</div><div class="pm-stat-n">{{ dashData.conflictCount ?? 0 }}</div><div class="pm-stat-l">Xung đột KM</div></div>
        </div>
        <div class="pm-card mt3">
          <div class="pm-card-head">
            <span class="pm-card-title">Chi tiết xung đột khuyến mãi</span>
            <span v-if="dashData.promotionConflicts?.length" class="pm-tag pm-tag-r">{{ dashData.promotionConflicts.length }}</span>
          </div>
          <div v-if="dashData.promotionConflicts?.length" class="pm-tw mt2">
            <table class="pm-tbl">
              <thead><tr><th>ID</th><th>Code</th><th>Tên chiến dịch</th><th>Lý do xung đột</th></tr></thead>
              <tbody>
                <tr v-for="row in dashData.promotionConflicts" :key="row.id">
                  <td class="mono dim">{{ row.id }}</td>
                  <td><span class="pm-tag pm-tag-y">{{ row.code }}</span></td>
                  <td>{{ row.name }}</td>
                  <td class="clr-red">{{ row.conflictReason }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="pm-alert pm-alert-g mt2">✅ Không có xung đột khuyến mãi.</div>
        </div>
      </template>
      <div v-else-if="!dashLoading" class="pm-empty mt3">Nhấn "Làm mới" để tải dữ liệu.</div>
    </div>

    <!-- Edit Dialog -->
    <div v-if="dlg.open" class="pm-overlay" @click.self="dlg.open = false">
      <div class="pm-dialog">
        <div class="pm-dlg-head">
          <span class="pm-dlg-title">Sửa bản ghi giá <span class="pm-tag pm-tag-b">#{{ dlg.id }}</span></span>
          <button class="pm-dlg-close" @click="dlg.open = false">✕</button>
        </div>
        <div v-if="dlg.alert" class="pm-alert pm-alert-r mb3">{{ dlg.alert }}</div>
        <div class="pm-row2 mb3">
          <div class="pm-field">
            <label class="pm-lbl">Currency</label>
            <input v-model="dlg.form.currencyCode" class="pm-in" />
          </div>
          <div class="pm-field">
            <label class="pm-lbl">Giá bán</label>
            <input v-model.number="dlg.form.price" class="pm-in pm-in-money" type="number" />
            <span v-if="dlg.form.price" class="pm-in-hint">{{ fmtMoney(dlg.form.price) }}</span>
          </div>
        </div>
        <div class="pm-row2">
          <div class="pm-field">
            <label class="pm-lbl">Giá nhập</label>
            <input v-model.number="dlg.form.costPrice" class="pm-in pm-in-money" type="number" />
            <span v-if="dlg.form.costPrice" class="pm-in-hint">{{ fmtMoney(dlg.form.costPrice) }}</span>
          </div>
          <div class="pm-field">
            <label class="pm-lbl">Lý do</label>
            <input v-model="dlg.form.reason" class="pm-in" />
          </div>
        </div>
        <div class="pm-dlg-foot">
          <button class="pm-btn pm-btn-ghost" @click="dlg.open = false">Hủy</button>
          <button class="pm-btn pm-btn-primary" :class="{ loading: dlg.loading }" @click="saveEdit" :disabled="dlg.loading">
            <span v-if="!dlg.loading">Lưu thay đổi</span><span v-else class="pm-spin"></span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, nextTick } from "vue";
import { pricesApi } from "../../api/prices.api";
import { toast } from "../../ui/toast";
import { confirmModal } from "../../ui/confirm";
import "../../assets/styles/pricing-manager.css";
import QuickNav from "../../components/QuickNav.vue";

const activeTab = ref("set");
const tabs = [
  { key: "set",       icon: "💲", label: "Đặt giá" },
  { key: "history",   icon: "📋", label: "Lịch sử giá" },
  { key: "warning",   icon: "🔔", label: "Cảnh báo" },
  { key: "conflicts", icon: "⚠️", label: "Xung đột giá" },
  { key: "calculate", icon: "🧮", label: "Tính giá đơn" },
  { key: "dashboard", icon: "📊", label: "Dashboard" },
];

const setLoading = ref(false);
const setForm = reactive({ variantId: null, currencyCode: "VND", price: 0, costPrice: 0, reason: "" });
async function setPrice() {
  if (!setForm.variantId) return toast("Vui lòng nhập Variant ID.", "warning");
  setLoading.value = true;
  try { await pricesApi.setVariantPrice(setForm.variantId, { currencyCode: setForm.currencyCode, price: setForm.price, costPrice: setForm.costPrice, reason: setForm.reason }); toast("Đã lưu giá.", "success"); }
  catch { toast("Lưu giá thất bại.", "error"); } finally { setLoading.value = false; }
}

const effLoading = ref(false);
const effective = ref(null);
async function loadEffective() {
  if (!setForm.variantId) return toast("Vui lòng nhập Variant ID.", "warning");
  effLoading.value = true;
  try { const r = await pricesApi.getEffective(setForm.variantId); effective.value = r?.data ?? null; }
  catch { effective.value = null; toast("Tải thất bại.", "error"); } finally { effLoading.value = false; }
}

const effCustLoading = ref(false);
const effectiveForCustomer = ref(null);
const customerIdForPrice = ref(null);
async function loadEffectiveForCustomer() {
  if (!setForm.variantId) return toast("Nhập Variant ID.", "warning");
  if (!customerIdForPrice.value) return toast("Nhập Customer ID.", "warning");
  effCustLoading.value = true;
  try { const r = await pricesApi.getEffectiveForCustomer(setForm.variantId, customerIdForPrice.value); effectiveForCustomer.value = r?.data ?? null; }
  catch { effectiveForCustomer.value = null; toast("Tải thất bại.", "error"); } finally { effCustLoading.value = false; }
}

const prodLoading = ref(false); const productId = ref(null); const productPrices = ref([]);
async function loadByProduct() {
  if (!productId.value) return toast("Nhập Product ID.", "warning");
  prodLoading.value = true;
  try { const r = await pricesApi.listByProduct(productId.value); productPrices.value = r?.data?.data ?? r?.data ?? []; }
  catch { productPrices.value = []; toast("Tải thất bại.", "error"); } finally { prodLoading.value = false; }
}

const histLoading = ref(false); const histLoaded = ref(false); const histVariantId = ref(null); const variantHistory = ref([]);
async function loadVariantHistory() {
  if (!histVariantId.value) return toast("Nhập Variant ID.", "warning");
  histLoading.value = true; histLoaded.value = false;
  try { const r = await pricesApi.getVariantHistory(histVariantId.value); variantHistory.value = r?.data?.data ?? r?.data ?? []; histLoaded.value = true; }
  catch { variantHistory.value = []; toast("Tải thất bại.", "error"); } finally { histLoading.value = false; }
}

const dlg = reactive({ open: false, loading: false, alert: "", id: null, form: { currencyCode: "VND", price: 0, costPrice: 0, reason: "" } });
function openEdit(row) { dlg.open = true; dlg.alert = ""; dlg.id = row.id; dlg.form = { currencyCode: row.currencyCode || "VND", price: Number(row.price||0), costPrice: Number(row.costPrice||0), reason: row.reason||"" }; }
async function saveEdit() {
  dlg.loading = true; dlg.alert = "";
  try { await pricesApi.updateHistory(dlg.id, dlg.form); toast("Đã cập nhật.", "success"); dlg.open = false; await loadVariantHistory(); }
  catch (e) { dlg.alert = e?.response?.data?.message || e?.message || "Cập nhật thất bại"; } finally { dlg.loading = false; }
}
async function removeHistory(row) {
  const ok = await confirmModal(`Xóa bản ghi #${row.id}?`, "Xác nhận", "Xóa", true);
  if (!ok) return;
  try { await pricesApi.removeHistory(row.id); toast("Đã xóa.", "success"); await loadVariantHistory(); }
  catch { toast("Xóa thất bại.", "error"); }
}

const costWarnLoading = ref(false); const costWarnVariantId = ref(null); const costWarning = ref(null);
async function loadCostWarning() {
  if (!costWarnVariantId.value) return toast("Nhập Variant ID.", "warning");
  costWarnLoading.value = true;
  try { const r = await pricesApi.getCostWarning(costWarnVariantId.value); costWarning.value = r?.data?.data ?? r?.data ?? null; }
  catch { costWarning.value = null; toast("Tải thất bại.", "error"); } finally { costWarnLoading.value = false; }
}

const promoEndLoading = ref(false); const promoEndResult = ref(null);
const promoEndForm = reactive({ id: null, price: 0, reason: "PROMO_ENDED" });
async function submitPromoEnd() {
  if (!promoEndForm.id) return toast("Nhập History ID.", "warning");
  promoEndLoading.value = true;
  try { const r = await pricesApi.updateHistory(promoEndForm.id, { price: promoEndForm.price, reason: promoEndForm.reason }); promoEndResult.value = r?.data?.data ?? r?.data ?? null; toast("Đã cập nhật giá.", "success"); }
  catch (e) { toast(e?.response?.data?.message || "Cập nhật thất bại.", "error"); } finally { promoEndLoading.value = false; }
}

const conflictsLoading = ref(false); const conflictsLoaded = ref(false); const priceConflicts = ref([]);
async function loadConflicts() {
  conflictsLoading.value = true;
  try { const r = await pricesApi.getConflicts(); priceConflicts.value = r?.data?.data ?? r?.data ?? []; conflictsLoaded.value = true; }
  catch { priceConflicts.value = []; toast("Tải xung đột thất bại.", "error"); } finally { conflictsLoading.value = false; }
}

const calcLoading = ref(false); const calcResult = ref(null);
const calcForm = reactive({ customerId: null, items: [{ variantId: null, quantity: 1 }] });
async function calculateOrder() {
  const items = calcForm.items.filter(i => i.variantId);
  if (!items.length) return toast("Thêm ít nhất 1 sản phẩm.", "warning");
  calcLoading.value = true;
  try { const r = await pricesApi.calculateOrder(calcForm.customerId || null, items); calcResult.value = r?.data?.data ?? r?.data ?? null; }
  catch (e) { toast(e?.response?.data?.message || "Tính giá thất bại.", "error"); } finally { calcLoading.value = false; }
}

const dashLoading = ref(false); const dashData = ref(null);
async function loadDashboard() {
  dashLoading.value = true;
  try { const r = await pricesApi.getDashboard(); dashData.value = r?.data?.data ?? r?.data ?? null; }
  catch { toast("Tải dashboard thất bại.", "error"); } finally { dashLoading.value = false; }
}

function handleQuickNavTab(tabKey, scrollId) {
  activeTab.value = tabKey;
  if (scrollId) {
    nextTick(() => {
      const el = document.getElementById(scrollId);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    });
  }
}

onMounted(loadDashboard);

function fmtMoney(val) { if (val == null) return "—"; return Number(val).toLocaleString("vi-VN") + " ₫"; }
function fmtDate(iso) { if (!iso) return "—"; try { return new Date(iso).toLocaleString("vi-VN"); } catch { return iso; } }
</script>