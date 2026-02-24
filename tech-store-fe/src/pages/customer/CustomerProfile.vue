<template>
  <div class="profile-container">
    <div class="container-xl py-4">
      <el-skeleton v-if="loading" :rows="8" animated />

      <template v-else-if="customer">
        <el-card shadow="never" class="mb-3">
          <div class="d-flex align-items-center justify-content-between flex-wrap gap-3">
            <div class="d-flex align-items-center gap-3">
              <div class="profile-avatar">
                {{ customer.name?.charAt(0) || 'U' }}
              </div>
              <div>
                <h1 class="mb-2 fw-bold text-dark">{{ customer.name }}</h1>
                <div class="d-flex gap-2 flex-wrap">
                  <el-tag :type="getTierTagType(customer.vipTier)" effect="dark">
                    {{ customer.vipTierDisplay }}
                  </el-tag>
                  <el-tag type="info">
                    {{ customer.customerTypeDisplay }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div v-if="!editing">
              <el-button type="primary" @click="startEdit">Chỉnh sửa</el-button>
            </div>
            <div v-else class="d-flex gap-2">
              <el-button type="success" :loading="saving" @click="handleSave">
                {{ saving ? 'Đang lưu...' : 'Lưu' }}
              </el-button>
              <el-button @click="handleCancel" :disabled="saving">Hủy</el-button>
            </div>
          </div>
        </el-card>

        <el-tabs v-model="activeTab" class="mb-3">
          <!-- TAB: THÔNG TIN -->
          <el-tab-pane label="Thông tin" name="info">
            <div class="row g-3">
              <div class="col-12 col-lg-4">
                <el-card shadow="hover" class="mb-3">
                  <div class="d-flex align-items-center gap-2 mb-3">
                    <el-icon :size="24" color="#f59e0b"><Star /></el-icon>
                    <h3 class="mb-0 fw-bold">Điểm tích lũy</h3>
                  </div>
                  <div class="text-center py-3">
                    <div class="points-display">{{ customer.loyaltyPoints?.toLocaleString() || 0 }}</div>
                    <div class="muted">điểm</div>
                  </div>
                  <el-divider />
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="small text-muted">Lên hạng tiếp:</span>
                    <span class="fw-bold text-primary">
                      {{ customer.pointsToNextTier > 0 ? `${customer.pointsToNextTier} điểm` : 'Hạng cao nhất' }}
                    </span>
                  </div>
                  <el-progress v-if="customer.pointsToNextTier > 0" :percentage="getProgressPercent()" :stroke-width="8" :show-text="false" />
                </el-card>

                <el-card shadow="hover" class="mb-3 discount-card">
                  <div class="d-flex align-items-center gap-2 mb-3">
                    <el-icon :size="24"><Present /></el-icon>
                    <h3 class="mb-0 fw-bold">Ưu đãi của bạn</h3>
                  </div>
                  <div class="text-center py-3">
                    <div class="discount-display">{{ customer.discountRate || 0 }}%</div>
                    <div class="discount-desc">Giảm giá mọi đơn hàng</div>
                  </div>
                </el-card>

                <el-card shadow="hover">
                  <div class="d-flex align-items-center gap-2 mb-3">
                    <el-icon :size="24" color="#9333ea"><Wallet /></el-icon>
                    <h3 class="mb-0 fw-bold">Tổng chi tiêu</h3>
                  </div>
                  <div class="total-spent">{{ formatCurrency(customer.totalSpent) }}</div>
                </el-card>
              </div>

              <div class="col-12 col-lg-8">
                <el-card shadow="never" class="mb-3 text-dark">
                  <h2 class="mb-4 fw-bold">Thông tin cá nhân</h2>
                  <el-form label-position="top" :model="formData">
                    <div class="row g-3">
                      <div class="col-12 col-md-6">
                        <el-form-item label="Họ và tên">
                          <el-input v-if="editing" v-model="formData.fullName" />
                          <div v-else class="form-static">{{ customer.name }}</div>
                        </el-form-item>
                      </div>
                      <div class="col-12 col-md-6">
                        <el-form-item label="Email">
                          <el-input v-if="editing" v-model="formData.email" />
                          <div v-else class="form-static">{{ customer.email }}</div>
                        </el-form-item>
                      </div>
                      <div class="col-12 col-md-6">
                        <el-form-item label="Số điện thoại">
                          <el-input v-if="editing" v-model="formData.phone" />
                          <div v-else class="form-static">{{ customer.phone }}</div>
                        </el-form-item>
                      </div>
                      <div class="col-12 col-md-6">
                        <el-form-item label="Ngày sinh">
                          <el-date-picker v-if="editing" v-model="formData.birthDate" type="date" format="DD/MM/YYYY" value-format="YYYY-MM-DD" style="width:100%" />
                          <div v-else class="form-static">{{ formatDate(customer.dateOfBirth) }}</div>
                        </el-form-item>
                      </div>
                      <div class="col-12">
                        <el-form-item label="Địa chỉ">
                          <el-input v-if="editing" v-model="formData.address" placeholder="Nhập địa chỉ" />
                          <div v-else class="form-static">{{ customer.address || '—' }}</div>
                        </el-form-item>
                      </div>
                      <div class="col-12">
                        <el-form-item label="Ghi chú">
                          <el-input v-if="editing" v-model="formData.notes" type="textarea" :rows="3" />
                          <div v-else class="form-static notes-static">{{ customer.notes || '—' }}</div>
                        </el-form-item>
                      </div>
                    </div>
                  </el-form>
                </el-card>

                <el-card shadow="never" class="text-dark">
                  <h2 class="mb-4 fw-bold">Thông tin tài khoản</h2>
                  <div class="row g-3">
                    <div class="col-12 col-md-6">
                      <label class="small fw-bold text-muted mb-2 d-block">Hạng thành viên</label>
                      <el-tag :type="getTierTagType(customer.vipTier)" size="large" effect="dark">{{ customer.vipTierDisplay }}</el-tag>
                    </div>
                    <div class="col-12 col-md-6">
                      <label class="small fw-bold text-muted mb-2 d-block">Ngày tạo</label>
                      <div class="form-static-info">{{ formatDateTime(customer.createdAt) }}</div>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>

          <!-- TAB: LỊCH SỬ ĐIỂM -->
          <el-tab-pane label="Lịch sử điểm" name="loyalty">
            <el-row :gutter="16" class="mb-3">
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card earn-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><CirclePlus /></el-icon>
                    <div>
                      <div class="stats-value">+{{ loyaltyStats.totalEarned }}</div>
                      <div class="stats-label">Tổng điểm cộng</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card deduct-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><Remove /></el-icon>
                    <div>
                      <div class="stats-value">{{ loyaltyStats.totalDeducted }}</div>
                      <div class="stats-label">Tổng điểm trừ</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card transactions-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><List /></el-icon>
                    <div>
                      <div class="stats-value">{{ loyaltyStats.totalTransactions }}</div>
                      <div class="stats-label">Giao dịch</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-card shadow="never">
              <div class="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-3">
                <div>
                  <h3 class="mb-2 fw-bold">
                    <el-icon class="me-2"><TrendCharts /></el-icon>
                    Lịch sử giao dịch điểm
                  </h3>
                  <div class="d-flex gap-2 flex-wrap" v-if="activeHistoryFilters.length">
                    <el-tag v-for="filter in activeHistoryFilters" :key="filter.type" :type="filter.tagType" closable @close="removeHistoryFilter(filter.type)">
                      {{ filter.label }}
                    </el-tag>
                  </div>
                </div>
                <div class="d-flex gap-2 flex-wrap">
                  <el-select v-model="historyTransactionFilter" placeholder="Loại giao dịch" clearable style="width:160px" @change="filterLoyaltyHistory">
                    <el-option label="Cộng điểm" value="EARN"><el-tag type="success" size="small">Cộng điểm</el-tag></el-option>
                    <el-option label="Trừ điểm" value="DEDUCT"><el-tag type="warning" size="small">Trừ điểm</el-tag></el-option>
                    <el-option label="Phạt" value="PENALTY"><el-tag type="danger" size="small">Phạt</el-tag></el-option>
                    <el-option label="Thăng hạng" value="TIER_UPGRADE"><el-tag type="success" size="small">Thăng hạng</el-tag></el-option>
                    <el-option label="Hạ hạng" value="TIER_DOWNGRADE"><el-tag type="info" size="small">Hạ hạng</el-tag></el-option>
                  </el-select>
                  <el-date-picker v-model="historyDateRange" type="daterange" range-separator="-" start-placeholder="Từ ngày" end-placeholder="Đến ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" style="width:280px" @change="filterLoyaltyHistory" />
                  <el-button @click="clearHistoryFilters" v-if="hasActiveHistoryFilters"><el-icon><Close /></el-icon> Xóa bộ lọc</el-button>
                  <el-button @click="loadLoyaltyHistory" :loading="historyLoading"><el-icon><Refresh /></el-icon> Tải lại</el-button>
                </div>
              </div>

              <el-divider />
              <el-skeleton v-if="historyLoading" :rows="5" animated />
              <el-empty v-else-if="!filteredLoyaltyHistory.length" description="Không có giao dịch nào">
                <el-button type="primary" @click="clearHistoryFilters" v-if="hasActiveHistoryFilters">Xóa bộ lọc</el-button>
              </el-empty>

              <el-timeline v-else>
                <el-timeline-item
                  v-for="item in filteredLoyaltyHistory"
                  :key="item.id"
                  :timestamp="formatDateTime(item.createdAt)"
                  placement="top"
                  :type="getTimelineType(item.transactionType)"
                >
                  <el-card class="history-card" :class="getHistoryCardClass(item.transactionType)">
                    <div class="history-card-content">
                      <div class="history-icon-wrapper">
                        <el-icon :size="28" :color="getTransactionIconColor(item.transactionType)">
                          <component :is="getTransactionIcon(item.transactionType)" />
                        </el-icon>
                      </div>

                      <div class="history-details flex-grow-1">
                        <div class="d-flex align-items-center gap-2 mb-1">
                          <el-tag :type="getTransactionTagType(item.transactionType)" effect="dark">
                            {{ item.transactionTypeDisplay }}
                          </el-tag>
                          <span class="text-muted small" v-if="item.reason">{{ item.reason }}</span>
                        </div>

                        <p class="mb-2 history-note">{{ item.note }}</p>

                        <div v-if="item.tierBefore || item.tierAfter" class="tier-change-indicator">
                          <el-tag size="small" :type="getTierTagType(item.tierBefore)" effect="plain">
                            {{ item.tierBeforeDisplay || 'Member' }}
                          </el-tag>
                          <el-icon class="mx-2" :size="16"><Right /></el-icon>
                          <el-tag size="small" :type="getTierTagType(item.tierAfter)" effect="dark">
                            {{ item.tierAfterDisplay || 'Member' }}
                          </el-tag>
                        </div>

                        <!-- ✅ Nút xem bill đơn hàng -->
                        <div v-if="item.referenceType && item.referenceId" class="mt-2">
                          <el-button
                            size="small"
                            type="primary"
                            plain
                            round
                            class="order-bill-btn"
                            @click="viewOrderBill(item.referenceId, item.pointsDelta)"
                          >
                            <el-icon class="me-1"><ShoppingCart /></el-icon>
                            Đơn hàng #{{ item.referenceId }}
                          </el-button>
                        </div>
                      </div>

                      <div class="history-points">
                        <div class="points-badge" :class="item.pointsDelta > 0 ? 'points-positive' : 'points-negative'">
                          <span class="points-sign">{{ item.pointsDelta > 0 ? '+' : '' }}</span>
                          <span class="points-value">{{ Math.abs(item.pointsDelta) }}</span>
                          <span class="points-unit">điểm</span>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </el-card>
          </el-tab-pane>

          <!-- TAB: LỊCH SỬ THĂNG HẠNG -->
          <el-tab-pane label="Lịch sử thăng hạng" name="tier">
            <el-card shadow="never">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h3 class="mb-0 fw-bold"><el-icon class="me-2"><Trophy /></el-icon>Lịch sử thay đổi hạng VIP</h3>
                <el-button @click="loadTierHistory" :loading="tierLoading"><el-icon><Refresh /></el-icon> Tải lại</el-button>
              </div>
              <el-skeleton v-if="tierLoading" :rows="5" animated />
              <el-empty v-else-if="!tierHistory.length" description="Chưa có lịch sử thay đổi hạng" />
              <div v-else class="tier-history-list">
                <el-card v-for="item in tierHistory" :key="item.id" class="mb-3 tier-history-card" :class="item.transactionType === 'TIER_UPGRADE' ? 'upgrade' : 'downgrade'">
                  <div class="d-flex align-items-start gap-3">
                    <div class="tier-icon">
                      <el-icon :size="32" :color="item.transactionType === 'TIER_UPGRADE' ? '#67c23a' : '#f56c6c'">
                        <component :is="item.transactionType === 'TIER_UPGRADE' ? 'TopRight' : 'BottomRight'" />
                      </el-icon>
                    </div>
                    <div class="flex-grow-1">
                      <div class="d-flex justify-content-between align-items-center mb-2">
                        <h5 class="mb-0">
                          <el-tag :type="item.transactionType === 'TIER_UPGRADE' ? 'success' : 'danger'" effect="dark">{{ item.transactionTypeDisplay }}</el-tag>
                        </h5>
                        <span class="text-muted small">{{ formatDateTime(item.createdAt) }}</span>
                      </div>
                      <div class="tier-change-display mb-2">
                        <el-tag size="large" :type="getTierTagType(item.tierBefore)" effect="dark">{{ item.tierBeforeDisplay || 'Member' }}</el-tag>
                        <el-icon :size="20" class="mx-2"><Right /></el-icon>
                        <el-tag size="large" :type="getTierTagType(item.tierAfter)" effect="dark">{{ item.tierAfterDisplay || 'Member' }}</el-tag>
                      </div>
                      <p class="mb-0 text-muted">{{ item.note }}</p>
                    </div>
                  </div>
                </el-card>
              </div>
            </el-card>
          </el-tab-pane>

          <!-- TAB: LỊCH SỬ GIAO DỊCH -->
          <el-tab-pane label="Lịch sử giao dịch" name="payments">
            <el-row :gutter="16" class="mb-3">
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card payment-success-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><CircleCheck /></el-icon>
                    <div><div class="stats-value">{{ paymentStats.totalSuccess }}</div><div class="stats-label">Giao dịch thành công</div></div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card payment-amount-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><Money /></el-icon>
                    <div><div class="stats-value stats-value-small">{{ formatCurrencyShort(paymentStats.totalAmount) }}</div><div class="stats-label">Tổng thanh toán</div></div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card payment-refunded-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><RefreshLeft /></el-icon>
                    <div><div class="stats-value">{{ paymentStats.totalRefunded }}</div><div class="stats-label">Đã hoàn tiền</div></div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-card shadow="never">
              <div class="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-3">
                <div>
                  <h3 class="mb-2 fw-bold"><el-icon class="me-2"><CreditCard /></el-icon>Danh sách giao dịch</h3>
                  <div class="d-flex gap-2 flex-wrap" v-if="activePaymentFilters.length">
                    <el-tag v-for="filter in activePaymentFilters" :key="filter.type" :type="filter.tagType" closable @close="removePaymentFilter(filter.type)">{{ filter.label }}</el-tag>
                  </div>
                </div>
                <div class="d-flex gap-2 flex-wrap">
                  <el-select v-model="paymentStatusFilter" placeholder="Trạng thái" clearable style="width:160px" @change="filterPayments">
                    <el-option label="Thành công" value="SUCCESS"><el-tag type="success" size="small">SUCCESS</el-tag></el-option>
                    <el-option label="Đang chờ" value="PENDING"><el-tag type="warning" size="small">PENDING</el-tag></el-option>
                    <el-option label="Đã hoàn tiền" value="REFUNDED"><el-tag type="info" size="small">REFUNDED</el-tag></el-option>
                  </el-select>
                  <el-select v-model="paymentMethodFilter" placeholder="Phương thức" clearable style="width:160px" @change="filterPayments">
                    <el-option label="Tiền mặt" value="CASH" />
                    <el-option label="Chuyển khoản" value="BANK_TRANSFER" />
                    <el-option label="Thẻ tín dụng" value="CREDIT_CARD" />
                    <el-option label="Ví điện tử" value="E_WALLET" />
                  </el-select>
                  <el-date-picker v-model="paymentDateRange" type="daterange" range-separator="-" start-placeholder="Từ ngày" end-placeholder="Đến ngày" format="DD/MM/YYYY" value-format="YYYY-MM-DD" style="width:280px" @change="filterPayments" />
                  <el-switch v-model="showDeletedPayments" active-text="Hiện đã xóa" @change="loadPayments" />
                  <el-button @click="clearPaymentFilters" v-if="hasActivePaymentFilters"><el-icon><Close /></el-icon> Xóa bộ lọc</el-button>
                  <el-button @click="loadPayments" :loading="paymentsLoading"><el-icon><Refresh /></el-icon> Tải lại</el-button>
                </div>
              </div>

              <el-divider />
              <el-skeleton v-if="paymentsLoading" :rows="5" animated />
              <el-empty v-else-if="!filteredPayments.length" description="Không có giao dịch nào">
                <el-button type="primary" @click="clearPaymentFilters" v-if="hasActivePaymentFilters">Xóa bộ lọc</el-button>
              </el-empty>

              <el-table v-else :data="paginatedPayments" style="width:100%" stripe>
                <el-table-column prop="id" label="ID" width="80" sortable />
                <el-table-column label="Mã GD" width="150">
                  <template #default="{ row }"><span class="text-muted small">{{ row.transactionRef }}</span></template>
                </el-table-column>
                <el-table-column label="Đơn hàng" width="150">
                  <template #default="{ row }"><span class="fw-bold">#{{ row.orderId }}</span></template>
                </el-table-column>
                <el-table-column label="Số tiền" width="150" sortable prop="amount">
                  <template #default="{ row }"><span class="fw-bold">{{ formatCurrency(row.amount) }}</span></template>
                </el-table-column>
                <el-table-column label="Phương thức" width="140">
                  <template #default="{ row }"><el-tag size="small" type="info">{{ getPaymentMethodLabel(row.method) }}</el-tag></template>
                </el-table-column>
                <el-table-column label="Trạng thái" width="120">
                  <template #default="{ row }"><el-tag :type="getPaymentStatusType(row.status)" size="small">{{ row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column label="Thời gian" width="180" sortable prop="createdAt">
                  <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="Trạng thái" width="100" v-if="showDeletedPayments">
                  <template #default="{ row }">
                    <el-tag v-if="row.deletedAt" type="danger" size="small">Đã xóa</el-tag>
                    <el-tag v-else type="success" size="small">Hoạt động</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="Thao tác" width="200" fixed="right">
                  <template #default="{ row }">
                    <div class="d-flex gap-1">
                      <el-button size="small" type="primary" text @click="viewPaymentDetail(row)"><el-icon><View /></el-icon> Chi tiết</el-button>
                      <el-popconfirm v-if="!row.deletedAt" title="Xác nhận xóa giao dịch?" confirm-button-text="Xóa" cancel-button-text="Hủy" @confirm="softDeletePayment(row.id)">
                        <template #reference><el-button size="small" type="danger" text><el-icon><Delete /></el-icon></el-button></template>
                      </el-popconfirm>
                      <el-popconfirm v-else title="Khôi phục giao dịch?" confirm-button-text="Khôi phục" cancel-button-text="Hủy" @confirm="restorePayment(row.id)">
                        <template #reference><el-button size="small" type="success" text><el-icon><RefreshLeft /></el-icon></el-button></template>
                      </el-popconfirm>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <div class="d-flex justify-content-center mt-4" v-if="filteredPayments.length">
                <el-pagination v-model:current-page="paymentCurrentPage" v-model:page-size="paymentPageSize" :page-sizes="[10, 20, 50]" :total="filteredPayments.length" layout="total, sizes, prev, pager, next" />
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </template>
    </div>

    <!-- ===== ORDER BILL DIALOG ===== -->
    <el-dialog v-model="showOrderBillDialog" width="700px" :close-on-click-modal="true" class="order-bill-dialog">
      <template #header>
        <div class="bill-dialog-header">
          <div class="bill-header-left">
            <el-icon :size="20" color="#409eff"><ShoppingCart /></el-icon>
            <span class="bill-title">Chi tiết đơn hàng</span>
            <el-tag type="primary" effect="dark" size="small" v-if="orderBill">#{{ orderBill.id }}</el-tag>
          </div>
          <el-tag type="info" size="small" v-if="orderBill">{{ orderBill.orderNumber }}</el-tag>
        </div>
      </template>

      <el-skeleton v-if="loadingOrderBill" :rows="8" animated />

      <div v-else-if="orderBill" class="bill-body">
        <!-- Points banner -->
        <div class="points-earned-banner">
          <el-icon :size="18" color="#f59e0b"><Star /></el-icon>
          <span>Đơn hàng này tích lũy</span>
          <span class="points-earned-value">+{{ Math.abs(currentBillPoints).toLocaleString() }} điểm</span>
          <span class="points-rate">(10.000 VNĐ = 1 điểm)</span>
        </div>

        <!-- Meta -->
        <div class="bill-meta">
          <div class="bill-meta-item">
            <span class="bill-meta-label">Ngày đặt</span>
            <span class="bill-meta-value">{{ formatDateTime(orderBill.createdAt) }}</span>
          </div>
          <div class="bill-meta-item">
            <span class="bill-meta-label">Trạng thái</span>
            <el-tag :type="getOrderStatusType(orderBill.status)" size="small">{{ orderBill.status }}</el-tag>
          </div>
          <div class="bill-meta-item">
            <span class="bill-meta-label">Thanh toán</span>
            <el-tag :type="orderBill.paymentStatus === 'PAID' ? 'success' : 'warning'" size="small">{{ orderBill.paymentStatus }}</el-tag>
          </div>
          <div class="bill-meta-item">
            <span class="bill-meta-label">Kênh</span>
            <span class="bill-meta-value">{{ orderBill.channel || '—' }}</span>
          </div>
        </div>

        <el-divider />

        <!-- Items -->
        <div class="bill-items">
          <div class="bill-items-header">
            <span>Sản phẩm</span>
            <span style="text-align:center">SL</span>
            <span style="text-align:right">Đơn giá</span>
            <span style="text-align:right">Giảm giá</span>
            <span style="text-align:right">Thành tiền</span>
          </div>
          <div v-for="(item, i) in orderBill.items" :key="i" class="bill-item-row">
            <div>
              <div class="item-product-name">{{ item.productName }}</div>
              <div class="item-variant-name">{{ item.variantName }}</div>
              <div class="item-sku">SKU: {{ item.sku }}</div>
            </div>
            <div style="text-align:center"><span class="qty-badge">{{ item.quantity }}</span></div>
            <div style="text-align:right;color:#606266">{{ formatCurrency(item.unitPrice) }}</div>
            <div style="text-align:right" class="text-success">{{ item.discount > 0 ? '-' + formatCurrency(item.discount) : '—' }}</div>
            <div style="text-align:right;font-weight:700">{{ formatCurrency(item.lineTotal) }}</div>
          </div>
        </div>

        <el-divider />

        <!-- Totals -->
        <div class="bill-totals">
          <div class="bill-total-row">
            <span>Tạm tính</span><span>{{ formatCurrency(orderBill.subtotal) }}</span>
          </div>
          <div class="bill-total-row text-success" v-if="orderBill.discountTotal > 0">
            <span>Giảm giá</span><span>-{{ formatCurrency(orderBill.discountTotal) }}</span>
          </div>
          <div class="bill-total-row" v-if="orderBill.taxTotal > 0">
            <span>Thuế</span><span>{{ formatCurrency(orderBill.taxTotal) }}</span>
          </div>
          <div class="bill-total-row" v-if="orderBill.shippingFee > 0">
            <span>Phí vận chuyển</span><span>{{ formatCurrency(orderBill.shippingFee) }}</span>
          </div>
          <div class="bill-total-row bill-grand-total">
            <span>Tổng cộng</span><span>{{ formatCurrency(orderBill.totalAmount) }}</span>
          </div>
          <div class="bill-points-row">
            <span><el-icon color="#f59e0b" style="vertical-align:middle;margin-right:4px"><Star /></el-icon>Điểm tích lũy được</span>
            <span class="points-earned-inline">+{{ Math.abs(currentBillPoints).toLocaleString() }} điểm</span>
          </div>
        </div>

        <!-- Discount notes -->
        <div v-if="orderBill.notes" class="bill-notes mt-3">
          <div class="bill-notes-label">
            <el-icon><InfoFilled /></el-icon> Ghi chú chiết khấu
          </div>
          <div class="bill-notes-content">{{ orderBill.notes }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showOrderBillDialog = false">Đóng</el-button>
      </template>
    </el-dialog>

    <!-- Payment Detail Dialog -->
    <el-dialog v-model="showPaymentDetailDialog" title="Chi tiết giao dịch" width="800px">
      <el-skeleton v-if="loadingPaymentDetail" :rows="8" animated />
      <div v-else-if="selectedPayment" class="payment-detail">
        <el-descriptions :column="2" border class="mb-3">
          <el-descriptions-item label="Mã GD"><span class="fw-bold">{{ selectedPayment.transactionRef }}</span></el-descriptions-item>
          <el-descriptions-item label="Số tiền"><span class="fw-bold text-success">{{ formatCurrency(selectedPayment.amount) }}</span></el-descriptions-item>
          <el-descriptions-item label="Phương thức"><el-tag size="small">{{ getPaymentMethodLabel(selectedPayment.method) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="Trạng thái"><el-tag :type="getPaymentStatusType(selectedPayment.status)">{{ selectedPayment.status }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="Thời gian">{{ formatDateTime(selectedPayment.paidAt) }}</el-descriptions-item>
        </el-descriptions>
        <h4 class="mb-3">Thông tin đơn hàng</h4>
        <el-descriptions :column="2" border class="mb-3">
          <el-descriptions-item label="Mã đơn"><span class="fw-bold">#{{ selectedPayment.orderId }}</span></el-descriptions-item>
          <el-descriptions-item label="Trạng thái đơn"><el-tag :type="getOrderStatusType(selectedPayment.orderStatus)">{{ selectedPayment.orderStatus }}</el-tag></el-descriptions-item>
        </el-descriptions>
        <div v-if="selectedPayment.items && selectedPayment.items.length">
          <h4 class="mb-3">Sản phẩm</h4>
          <el-table :data="selectedPayment.items" border class="mb-3">
            <el-table-column label="Sản phẩm" min-width="200">
              <template #default="{ row }">
                <div class="fw-bold">{{ row.productName }}</div>
                <div class="text-muted small">{{ row.variantName }}</div>
              </template>
            </el-table-column>
            <el-table-column label="SL" width="80" align="center" prop="quantity" />
            <el-table-column label="Đơn giá" width="130" align="right">
              <template #default="{ row }">{{ formatCurrency(row.unitPrice) }}</template>
            </el-table-column>
            <el-table-column label="Tổng" width="150" align="right">
              <template #default="{ row }"><strong>{{ formatCurrency(row.lineTotal) }}</strong></template>
            </el-table-column>
          </el-table>
          <div class="payment-totals">
            <div class="d-flex justify-content-between mb-2"><span>Tạm tính:</span><strong>{{ formatCurrency(selectedPayment.subtotal) }}</strong></div>
            <div class="d-flex justify-content-between mb-2 text-success"><span>Giảm giá:</span><strong>-{{ formatCurrency(selectedPayment.discountTotal) }}</strong></div>
            <div class="d-flex justify-content-between mb-2"><span>Phí ship:</span><strong>{{ formatCurrency(selectedPayment.shippingFee) }}</strong></div>
            <el-divider />
            <div class="d-flex justify-content-between fs-5"><span class="fw-bold">Tổng cộng:</span><strong class="text-primary">{{ formatCurrency(selectedPayment.totalAmount) }}</strong></div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPaymentDetailDialog = false">Đóng</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import {
  Star, Present, Wallet, TrendCharts, Trophy, Refresh,
  Right, TopRight, BottomRight, CirclePlus, Remove, List,
  Close, CircleCheck, Money, RefreshLeft, CreditCard,
  View, Delete, ShoppingCart, InfoFilled
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { customersApi } from '../../api/customers.api';
import { ordersApi } from '../../api/orders.api';

const router = useRouter();

const loading        = ref(true);
const editing        = ref(false);
const saving         = ref(false);
const customer       = ref(null);
const activeTab      = ref('info');

const historyLoading           = ref(false);
const loyaltyHistory           = ref([]);
const historyTransactionFilter = ref('');
const historyDateRange         = ref(null);

const tierLoading = ref(false);
const tierHistory = ref([]);

const paymentsLoading         = ref(false);
const payments                = ref([]);
const showDeletedPayments     = ref(false);
const paymentStatusFilter     = ref('');
const paymentMethodFilter     = ref('');
const paymentDateRange        = ref(null);
const paymentCurrentPage      = ref(1);
const paymentPageSize         = ref(10);
const showPaymentDetailDialog = ref(false);
const selectedPayment         = ref(null);
const loadingPaymentDetail    = ref(false);

// Order bill
const showOrderBillDialog = ref(false);
const loadingOrderBill    = ref(false);
const orderBill           = ref(null);
const currentBillPoints   = ref(0);

const formData = ref({ fullName: '', email: '', phone: '', address: '', notes: '', birthDate: '' });

// ── Computed ──────────────────────────────────────────────────────────────────
const loyaltyStats = computed(() => {
  const earned   = loyaltyHistory.value.filter(h => h.pointsDelta > 0).reduce((s, h) => s + h.pointsDelta, 0);
  const deducted = loyaltyHistory.value.filter(h => h.pointsDelta < 0).reduce((s, h) => s + Math.abs(h.pointsDelta), 0);
  return { totalEarned: earned.toLocaleString(), totalDeducted: deducted.toLocaleString(), totalTransactions: loyaltyHistory.value.length };
});

const paymentStats = computed(() => {
  const active   = payments.value.filter(p => !p.deletedAt);
  const success  = active.filter(p => p.status === 'SUCCESS');
  const refunded = active.filter(p => p.status === 'REFUNDED');
  return { totalSuccess: success.length, totalAmount: success.reduce((s, p) => s + (p.amount || 0), 0), totalRefunded: refunded.length };
});

const filteredLoyaltyHistory = computed(() => {
  let result = [...loyaltyHistory.value];
  if (historyTransactionFilter.value) result = result.filter(h => h.transactionType === historyTransactionFilter.value);
  if (historyDateRange.value?.length === 2) {
    const [start, end] = historyDateRange.value;
    result = result.filter(h => { const d = new Date(h.createdAt); return d >= new Date(start) && d <= new Date(end + 'T23:59:59'); });
  }
  return result;
});

const filteredPayments = computed(() => {
  let result = [...payments.value];
  if (!showDeletedPayments.value) result = result.filter(p => !p.deletedAt);
  if (paymentStatusFilter.value)  result = result.filter(p => p.status === paymentStatusFilter.value);
  if (paymentMethodFilter.value)  result = result.filter(p => p.method === paymentMethodFilter.value);
  if (paymentDateRange.value?.length === 2) {
    const [start, end] = paymentDateRange.value;
    result = result.filter(p => { const d = new Date(p.createdAt); return d >= new Date(start) && d <= new Date(end + 'T23:59:59'); });
  }
  return result;
});

const paginatedPayments = computed(() => {
  const start = (paymentCurrentPage.value - 1) * paymentPageSize.value;
  return filteredPayments.value.slice(start, start + paymentPageSize.value);
});

const activeHistoryFilters = computed(() => {
  const filters = [];
  if (historyTransactionFilter.value) {
    const labels = { EARN: 'Cộng điểm', DEDUCT: 'Trừ điểm', PENALTY: 'Phạt', TIER_UPGRADE: 'Thăng hạng', TIER_DOWNGRADE: 'Hạ hạng' };
    filters.push({ type: 'transaction', label: labels[historyTransactionFilter.value], tagType: getTransactionTagType(historyTransactionFilter.value) });
  }
  if (historyDateRange.value) {
    const [s, e] = historyDateRange.value;
    filters.push({ type: 'date', label: `${formatDate(s)} - ${formatDate(e)}`, tagType: 'info' });
  }
  return filters;
});

const activePaymentFilters = computed(() => {
  const filters = [];
  if (paymentStatusFilter.value) filters.push({ type: 'status', label: `Trạng thái: ${paymentStatusFilter.value}`, tagType: getPaymentStatusType(paymentStatusFilter.value) });
  if (paymentMethodFilter.value) filters.push({ type: 'method', label: `PT: ${getPaymentMethodLabel(paymentMethodFilter.value)}`, tagType: 'info' });
  if (paymentDateRange.value) {
    const [s, e] = paymentDateRange.value;
    filters.push({ type: 'date', label: `${formatDate(s)} - ${formatDate(e)}`, tagType: 'info' });
  }
  return filters;
});

const hasActiveHistoryFilters = computed(() => !!(historyTransactionFilter.value || historyDateRange.value));
const hasActivePaymentFilters = computed(() => !!(paymentStatusFilter.value || paymentMethodFilter.value || paymentDateRange.value));

// ── Helpers ───────────────────────────────────────────────────────────────────
const unwrap = (response) => {
  const d = response?.data;
  if (d && typeof d === 'object' && 'data' in d) return d.data;
  return d ?? response;
};

// ── Data loading ──────────────────────────────────────────────────────────────
const loadCustomerData = async () => {
  loading.value = true;
  try {
    const response = await customersApi.getProfile();
    const c = unwrap(response);
    if (!c) { ElMessage.error('Không tìm thấy thông tin khách hàng'); return; }
    customer.value = c;
    formData.value = { fullName: c.name || '', email: c.email || '', phone: c.phone || '', address: c.address || '', notes: c.notes || '', birthDate: c.dateOfBirth || '' };
  } catch (error) {
    if (error.response?.status === 401) { ElMessage.warning('Vui lòng đăng nhập lại'); router.push('/login'); }
    else if (error.response?.status === 404) ElMessage.error('Không tìm thấy thông tin khách hàng.');
    else ElMessage.error('Lỗi khi tải dữ liệu: ' + (error.response?.data?.message || error.message));
  } finally { loading.value = false; }
};

const loadLoyaltyHistory = async () => {
  if (!customer.value?.id) return;
  historyLoading.value = true;
  try { loyaltyHistory.value = unwrap(await customersApi.getLoyaltyHistory(customer.value.id)) || []; }
  catch { ElMessage.error('Lỗi khi tải lịch sử điểm'); }
  finally { historyLoading.value = false; }
};

const loadTierHistory = async () => {
  if (!customer.value?.id) return;
  tierLoading.value = true;
  try { tierHistory.value = unwrap(await customersApi.getTierHistory(customer.value.id)) || []; }
  catch { ElMessage.error('Lỗi khi tải lịch sử thăng hạng'); }
  finally { tierLoading.value = false; }
};

const loadPayments = async () => {
  if (!customer.value?.id) return;
  paymentsLoading.value = true;
  try { payments.value = unwrap(await customersApi.getPayments(customer.value.id, showDeletedPayments.value)) || []; }
  catch { ElMessage.error('Lỗi khi tải lịch sử giao dịch'); }
  finally { paymentsLoading.value = false; }
};

// ✅ Xem bill đơn hàng
const viewOrderBill = async (orderId, pointsDelta) => {
  showOrderBillDialog.value = true;
  loadingOrderBill.value    = true;
  orderBill.value           = null;
  currentBillPoints.value   = pointsDelta;
  try {
    orderBill.value = unwrap(await ordersApi.getOrderDetail(orderId));
  } catch (error) {
    ElMessage.error('Lỗi khi tải chi tiết đơn hàng');
    showOrderBillDialog.value = false;
  } finally {
    loadingOrderBill.value = false;
  }
};

// ── Filter helpers ────────────────────────────────────────────────────────────
const filterLoyaltyHistory = () => {};
const filterPayments = () => { paymentCurrentPage.value = 1; };
const removeHistoryFilter = (type) => { if (type === 'transaction') historyTransactionFilter.value = ''; if (type === 'date') historyDateRange.value = null; };
const removePaymentFilter = (type) => { if (type === 'status') paymentStatusFilter.value = ''; if (type === 'method') paymentMethodFilter.value = ''; if (type === 'date') paymentDateRange.value = null; filterPayments(); };
const clearHistoryFilters = () => { historyTransactionFilter.value = ''; historyDateRange.value = null; };
const clearPaymentFilters = () => { paymentStatusFilter.value = ''; paymentMethodFilter.value = ''; paymentDateRange.value = null; paymentCurrentPage.value = 1; };

// ── Payment actions ───────────────────────────────────────────────────────────
const viewPaymentDetail = async (payment) => {
  selectedPayment.value = payment; showPaymentDetailDialog.value = true; loadingPaymentDetail.value = true;
  try { selectedPayment.value = unwrap(await customersApi.getPaymentDetail(payment.id)) || payment; }
  catch { ElMessage.error('Lỗi khi tải chi tiết giao dịch'); }
  finally { loadingPaymentDetail.value = false; }
};
const softDeletePayment = async (id) => {
  try { await customersApi.softDeletePayment(id); ElMessage.success('Đã xóa giao dịch'); loadPayments(); }
  catch (error) { ElMessage.error(error.response?.data?.message || 'Lỗi khi xóa giao dịch'); }
};
const restorePayment = async (id) => {
  try { await customersApi.restorePayment(id); ElMessage.success('Đã khôi phục giao dịch'); loadPayments(); }
  catch (error) { ElMessage.error(error.response?.data?.message || 'Lỗi khi khôi phục giao dịch'); }
};

// ── Edit actions ──────────────────────────────────────────────────────────────
const startEdit    = () => { editing.value = true; };
const handleCancel = () => { editing.value = false; loadCustomerData(); };
const handleSave = async () => {
  saving.value = true;
  try { await customersApi.update(customer.value.id, formData.value); await loadCustomerData(); editing.value = false; ElMessage.success('Cập nhật thành công!'); }
  catch (error) { ElMessage.error('Lỗi khi lưu dữ liệu: ' + (error.response?.data?.message || error.message)); }
  finally { saving.value = false; }
};

// ── Display helpers ───────────────────────────────────────────────────────────
const getTierTagType        = (tier)   => ({ BRONZE: 'warning', SILVER: 'info', GOLD: 'warning', PLATINUM: 'primary', DIAMOND: 'danger' }[tier] || 'info');
const getTransactionTagType = (type)   => ({ EARN: 'success', DEDUCT: 'warning', PENALTY: 'danger', TIER_UPGRADE: 'success', TIER_DOWNGRADE: 'info' }[type] || 'info');
const getTimelineType       = (type)   => ({ EARN: 'success', DEDUCT: 'warning', PENALTY: 'danger', TIER_UPGRADE: 'success', TIER_DOWNGRADE: 'info' }[type] || 'primary');
const getTransactionIcon    = (type)   => ({ EARN: 'CirclePlus', DEDUCT: 'Remove', PENALTY: 'Warning', TIER_UPGRADE: 'TopRight', TIER_DOWNGRADE: 'BottomRight' }[type] || 'More');
const getTransactionIconColor = (type) => ({ EARN: '#67c23a', DEDUCT: '#e6a23c', PENALTY: '#f56c6c', TIER_UPGRADE: '#67c23a', TIER_DOWNGRADE: '#909399' }[type] || '#409eff');
const getHistoryCardClass   = (type)   => ({ EARN: 'history-earn', DEDUCT: 'history-deduct', PENALTY: 'history-penalty', TIER_UPGRADE: 'history-upgrade', TIER_DOWNGRADE: 'history-downgrade' }[type] || '');
const getPaymentStatusType  = (status) => ({ SUCCESS: 'success', PENDING: 'warning', REFUNDED: 'info', FAILED: 'danger' }[status] || 'info');
const getPaymentMethodLabel = (method) => ({ CASH: 'Tiền mặt', BANK_TRANSFER: 'Chuyển khoản', CREDIT_CARD: 'Thẻ tín dụng', E_WALLET: 'Ví điện tử' }[method] || method);
const getOrderStatusType    = (status) => ({ PENDING: 'warning', CONFIRMED: 'primary', PROCESSING: 'primary', SHIPPING: 'primary', DELIVERED: 'success', CANCELLED: 'danger' }[status] || 'info');
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
const formatCurrencyShort = (val) => { if (val >= 1e9) return (val/1e9).toFixed(1)+'B'; if (val >= 1e6) return (val/1e6).toFixed(1)+'M'; if (val >= 1e3) return (val/1e3).toFixed(1)+'K'; return formatCurrency(val); };
const formatDate     = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—';
const formatDateTime = (d) => d ? new Date(d).toLocaleString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '—';
const getProgressPercent = () => { if (!customer.value) return 0; const total = (customer.value.loyaltyPoints||0)+(customer.value.pointsToNextTier||0); return total===0?0:Math.min(100,Math.round((customer.value.loyaltyPoints/total)*100)); };

onMounted(async () => {
  await loadCustomerData();
  if (customer.value?.id) { loadLoyaltyHistory(); loadTierHistory(); loadPayments(); }
});
</script>

<style scoped>
.profile-container { min-height: 100vh; background: #f0f2f5; padding: 20px 0; }
.profile-avatar { width:80px;height:80px;border-radius:50%;background:#409eff;color:white;display:flex;align-items:center;justify-content:center;font-size:32px;font-weight:bold; }
.points-display { font-size:48px;font-weight:900;color:#f59e0b; }
.muted { color:#909399;font-size:14px; }
.total-spent { font-size:32px;font-weight:bold;color:#9333ea; }
.discount-card { background:linear-gradient(135deg,#67c23a 0%,#5daf34 100%);color:white;border:none; }
.discount-display { font-size:48px;font-weight:900;color:white; }
.discount-desc { color:rgba(255,255,255,0.9);font-size:14px;margin-top:8px; }
.form-static,.form-static-info { padding:8px 12px;background:#f5f7fa;border-radius:4px;min-height:40px;display:flex;align-items:center; }
.notes-static { white-space:pre-wrap;word-wrap:break-word;min-height:60px;align-items:flex-start; }
.text-dark { color:#303133; }

.stats-card { border-radius:12px;transition:all 0.3s; }
.stats-card:hover { transform:translateY(-4px); }
.stats-content { display:flex;align-items:center;gap:16px; }
.stats-icon { flex-shrink:0; }
.stats-value { font-size:28px;font-weight:900;line-height:1;margin-bottom:4px; }
.stats-value-small { font-size:24px; }
.stats-label { font-size:13px;color:#909399;font-weight:600; }
.earn-card { background:linear-gradient(135deg,#e8f5e9 0%,#c8e6c9 100%); }
.earn-card .stats-value { color:#67c23a; }
.deduct-card { background:linear-gradient(135deg,#fff3e0 0%,#ffe0b2 100%); }
.deduct-card .stats-value { color:#e6a23c; }
.transactions-card { background:linear-gradient(135deg,#e3f2fd 0%,#bbdefb 100%); }
.transactions-card .stats-value { color:#409eff; }
.payment-success-card { background:linear-gradient(135deg,#e8f5e9 0%,#c8e6c9 100%); }
.payment-success-card .stats-value { color:#67c23a; }
.payment-amount-card { background:linear-gradient(135deg,#e3f2fd 0%,#bbdefb 100%); }
.payment-amount-card .stats-value { color:#409eff; }
.payment-refunded-card { background:linear-gradient(135deg,#fff3e0 0%,#ffe0b2 100%); }
.payment-refunded-card .stats-value { color:#e6a23c; }

.history-card { transition:all 0.3s;border-radius:12px; }
.history-card:hover { transform:translateX(4px);box-shadow:0 4px 12px rgba(0,0,0,0.1); }
.history-card-content { display:flex;align-items:flex-start;gap:16px; }
.history-icon-wrapper { flex-shrink:0;width:48px;height:48px;display:flex;align-items:center;justify-content:center;border-radius:50%;background:rgba(64,158,255,0.1); }
.history-earn .history-icon-wrapper { background:rgba(103,194,58,0.1); }
.history-deduct .history-icon-wrapper { background:rgba(230,162,60,0.1); }
.history-penalty .history-icon-wrapper { background:rgba(245,108,108,0.1); }
.history-upgrade .history-icon-wrapper { background:rgba(103,194,58,0.1); }
.history-downgrade .history-icon-wrapper { background:rgba(144,147,153,0.1); }
.history-details { flex:1;min-width:0; }
.history-note { color:#606266;font-size:14px;line-height:1.5;margin:0; }
.tier-change-indicator { display:flex;align-items:center;margin-top:8px; }
.history-points { flex-shrink:0; }
.points-badge { padding:8px 16px;border-radius:8px;text-align:center;min-width:100px; }
.points-positive { background:linear-gradient(135deg,#e8f5e9 0%,#c8e6c9 100%);border:2px solid #67c23a; }
.points-negative { background:linear-gradient(135deg,#fff3e0 0%,#ffe0b2 100%);border:2px solid #e6a23c; }
.points-sign { font-size:14px;font-weight:600; }
.points-value { font-size:24px;font-weight:900;margin:0 4px; }
.points-positive .points-value { color:#67c23a; }
.points-negative .points-value { color:#e6a23c; }
.points-unit { font-size:12px;color:#909399; }

/* Order bill button */
.order-bill-btn { border-radius:20px;font-size:12px;font-weight:600;transition:all 0.2s; }
.order-bill-btn:hover { transform:translateY(-1px);box-shadow:0 2px 8px rgba(64,158,255,0.35); }

.tier-history-card { transition:all 0.3s; }
.tier-history-card.upgrade { border-left:4px solid #67c23a; }
.tier-history-card.downgrade { border-left:4px solid #f56c6c; }
.tier-history-card:hover { transform:translateX(4px);box-shadow:0 2px 12px rgba(0,0,0,0.1); }
.tier-icon { display:flex;align-items:center;justify-content:center;width:50px;height:50px;border-radius:50%;background:rgba(103,194,58,0.1); }
.tier-history-card.downgrade .tier-icon { background:rgba(245,108,108,0.1); }
.tier-change-display { display:flex;align-items:center; }
.payment-detail { padding:10px 0; }
.payment-totals { background:#f5f7fa;padding:16px;border-radius:8px; }

/* ===== ORDER BILL DIALOG ===== */
.bill-dialog-header { display:flex;align-items:center;justify-content:space-between; }
.bill-header-left { display:flex;align-items:center;gap:8px; }
.bill-title { font-size:16px;font-weight:700;color:#303133; }

.points-earned-banner { display:flex;align-items:center;gap:8px;background:linear-gradient(135deg,#fffbeb 0%,#fef3c7 100%);border:1px solid #f59e0b;border-radius:10px;padding:10px 16px;margin-bottom:16px;font-size:14px;color:#92400e;flex-wrap:wrap; }
.points-earned-value { font-size:18px;font-weight:900;color:#f59e0b; }
.points-rate { font-size:12px;color:#b45309;opacity:0.8; }

.bill-meta { display:grid;grid-template-columns:repeat(2,1fr);gap:12px;background:#f8fafc;border-radius:10px;padding:12px 16px; }
.bill-meta-item { display:flex;flex-direction:column;gap:4px; }
.bill-meta-label { font-size:11px;font-weight:600;color:#909399;text-transform:uppercase;letter-spacing:0.5px; }
.bill-meta-value { font-size:14px;color:#303133;font-weight:500; }

.bill-items { font-size:13px; }
.bill-items-header { display:grid;grid-template-columns:1fr 50px 120px 110px 120px;gap:8px;padding:8px 12px;background:#f0f2f5;border-radius:6px;font-weight:700;color:#606266;font-size:12px;margin-bottom:4px; }
.bill-item-row { display:grid;grid-template-columns:1fr 50px 120px 110px 120px;gap:8px;padding:10px 12px;border-bottom:1px solid #f0f2f5;align-items:center;transition:background 0.15s; }
.bill-item-row:hover { background:#fafafa; }
.bill-item-row:last-child { border-bottom:none; }
.item-product-name { font-weight:600;color:#303133;white-space:nowrap;overflow:hidden;text-overflow:ellipsis; }
.item-variant-name { font-size:12px;color:#909399; }
.item-sku { font-size:11px;color:#c0c4cc; }
.qty-badge { display:inline-block;background:#e8f4fd;color:#409eff;border-radius:4px;padding:2px 8px;font-weight:700;font-size:13px; }

.bill-totals { background:#f8fafc;border-radius:10px;padding:14px 18px; }
.bill-total-row { display:flex;justify-content:space-between;align-items:center;padding:5px 0;font-size:14px;color:#606266; }
.bill-grand-total { font-size:18px;font-weight:900;color:#303133;border-top:2px solid #e4e7ed;margin-top:8px;padding-top:12px; }
.bill-grand-total span:last-child { color:#409eff; }
.bill-points-row { display:flex;justify-content:space-between;align-items:center;padding:8px 0 0;font-size:13px;color:#92400e;border-top:1px dashed #fcd34d;margin-top:8px; }
.points-earned-inline { font-size:15px;font-weight:900;color:#f59e0b; }

.bill-notes { background:#f0f9ff;border:1px solid #bae6fd;border-radius:8px;padding:10px 14px; }
.bill-notes-label { display:flex;align-items:center;gap:6px;font-size:12px;font-weight:700;color:#0369a1;margin-bottom:6px; }
.bill-notes-content { font-size:13px;color:#0c4a6e;line-height:1.6; }

@media (max-width:768px) {
  .stats-content { flex-direction:column;text-align:center; }
  .history-card-content { flex-direction:column; }
  .history-points { width:100%; }
  .points-badge { width:100%; }
  .bill-items-header,.bill-item-row { grid-template-columns:1fr 40px 100px; }
  .bill-meta { grid-template-columns:1fr; }
}
</style>