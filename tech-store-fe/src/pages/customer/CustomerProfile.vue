<template>
  <div class="profile-container">
    <div class="container-xl py-4">
      <el-skeleton v-if="loading" :rows="8" animated />

      <template v-else-if="customer">
        <el-card shadow="never" class="mb-3">
          <div
            class="d-flex align-items-center justify-content-between flex-wrap gap-3"
          >
            <div class="d-flex align-items-center gap-3">
              <div class="profile-avatar">
                {{ customer.name?.charAt(0) || "U" }}
              </div>
              <div>
                <h1 class="mb-2 fw-bold text-dark">{{ customer.name }}</h1>
                <div class="d-flex gap-2 flex-wrap">
                  <el-tag
                    :type="getTierTagType(customer.vipTier)"
                    effect="dark"
                  >
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
                {{ saving ? "Đang lưu..." : "Lưu" }}
              </el-button>
              <el-button @click="handleCancel" :disabled="saving"
                >Hủy</el-button
              >
            </div>
          </div>
        </el-card>

        <el-tabs v-model="activeTab" class="mb-3">
          <!-- ═══════════════════════════════════════════════════════════
               TAB: DASHBOARD (MỚI)
          ════════════════════════════════════════════════════════════ -->
          <el-tab-pane name="dashboard">
            <template #label>
              <span class="d-flex align-items-center gap-1">
                <el-icon><DataAnalysis /></el-icon> Dashboard
              </span>
            </template>

            <el-skeleton v-if="dashboardLoading" :rows="10" animated />

            <template v-else-if="myDashboard">
              <!-- ── Hàng 1: 4 KPI cards ── -->
              <el-row :gutter="16" class="mb-4">
                <el-col :xs="12" :sm="6">
                  <div class="kpi-card kpi-points">
                    <div class="kpi-icon">
                      <el-icon :size="28"><Star /></el-icon>
                    </div>
                    <div class="kpi-body">
                      <div class="kpi-value">
                        {{
                          (
                            myDashboard.summary?.loyaltyPoints || 0
                          ).toLocaleString()
                        }}
                      </div>
                      <div class="kpi-label">Điểm tích lũy</div>
                    </div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="6">
                  <div class="kpi-card kpi-spent">
                    <div class="kpi-icon">
                      <el-icon :size="28"><Wallet /></el-icon>
                    </div>
                    <div class="kpi-body">
                      <div class="kpi-value">
                        {{
                          formatCurrencyShort(myDashboard.summary?.totalSpent)
                        }}
                      </div>
                      <div class="kpi-label">Tổng chi tiêu</div>
                    </div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="6">
                  <div class="kpi-card kpi-orders">
                    <div class="kpi-icon">
                      <el-icon :size="28"><ShoppingCart /></el-icon>
                    </div>
                    <div class="kpi-body">
                      <div class="kpi-value">
                        {{ myDashboard.summary?.totalOrders || 0 }}
                      </div>
                      <div class="kpi-label">Đơn hàng</div>
                    </div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="6">
                  <div class="kpi-card kpi-discount">
                    <div class="kpi-icon">
                      <el-icon :size="28"><Present /></el-icon>
                    </div>
                    <div class="kpi-body">
                      <div class="kpi-value">
                        {{ myDashboard.summary?.discountDisplay || "0%" }}
                      </div>
                      <div class="kpi-label">Ưu đãi hiện tại</div>
                    </div>
                  </div>
                </el-col>
              </el-row>

              <!-- ── Hàng 2: VIP Journey + Spin Wheel ── -->
              <el-row :gutter="16" class="mb-4">
                <!-- VIP Journey -->
                <el-col :xs="24" :md="14">
                  <el-card shadow="hover" class="vip-journey-card h-100">
                    <div class="d-flex align-items-center gap-2 mb-4">
                      <el-icon :size="22" color="#f59e0b"><Trophy /></el-icon>
                      <h3 class="mb-0 fw-bold">Hành trình VIP</h3>
                    </div>

                    <!-- Current tier hero -->
                    <div class="vip-hero mb-4">
                      <div class="vip-hero-tier">
                        <el-tag
                          :type="
                            getTierTagType(myDashboard.vipJourney?.currentTier)
                          "
                          effect="dark"
                          size="large"
                          class="vip-tier-big"
                        >
                          {{
                            myDashboard.vipJourney?.currentTierDisplay ||
                            "Member"
                          }}
                        </el-tag>
                      </div>
                      <div class="vip-hero-points">
                        <span class="vip-pts-current">{{
                          (
                            myDashboard.vipJourney?.currentPoints || 0
                          ).toLocaleString()
                        }}</span>
                        <span class="vip-pts-label"> điểm</span>
                      </div>
                    </div>

                    <!-- Progress bar tới tier tiếp theo -->
                    <div v-if="myDashboard.vipJourney?.nextTier" class="mb-3">
                      <div class="d-flex justify-content-between mb-2">
                        <span class="small text-muted">
                          Cần thêm
                          <strong class="text-primary"
                            >{{
                              (
                                myDashboard.vipJourney?.pointsToNext || 0
                              ).toLocaleString()
                            }}
                            điểm</strong
                          >
                          để lên
                          <strong>{{
                            myDashboard.vipJourney?.nextTierDisplay
                          }}</strong>
                        </span>
                        <span class="small fw-bold text-primary"
                          >{{
                            myDashboard.vipJourney?.progressPercent || 0
                          }}%</span
                        >
                      </div>
                      <div class="vip-progress-track">
                        <div
                          class="vip-progress-fill"
                          :style="{
                            width:
                              (myDashboard.vipJourney?.progressPercent || 0) +
                              '%',
                          }"
                        />
                      </div>
                    </div>
                    <div v-else class="mb-3">
                      <div class="vip-max-badge">
                        <el-icon color="#f59e0b"><Star /></el-icon> Bạn đang ở
                        hạng cao nhất!
                      </div>
                    </div>

                    <!-- Milestones -->
                    <div class="vip-milestones">
                      <div
                        v-for="m in myDashboard.vipJourney?.milestones"
                        :key="m.tier"
                        class="vip-milestone"
                        :class="{ achieved: m.achieved }"
                      >
                        <div
                          class="milestone-dot"
                          :class="m.achieved ? 'dot-achieved' : 'dot-pending'"
                        >
                          <el-icon v-if="m.achieved" :size="12" color="white"
                            ><Check
                          /></el-icon>
                        </div>
                        <div class="milestone-info">
                          <span class="milestone-tier">{{
                            m.tierDisplay
                          }}</span>
                          <span class="milestone-pts"
                            >{{ m.requiredPoints.toLocaleString() }} điểm</span
                          >
                        </div>
                        <div
                          v-if="m.achieved && m.achievedAt"
                          class="milestone-date"
                        >
                          {{ m.achievedAt }}
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>

                <!-- Spin Wheel + Spin Bonus -->
                <el-col :xs="24" :md="10">
                  <el-card shadow="hover" class="spin-dash-card h-100">
                    <div class="d-flex align-items-center gap-2 mb-3">
                      <el-icon :size="22" color="#67c23a"><Trophy /></el-icon>
                      <h3 class="mb-0 fw-bold">Vòng quay may mắn</h3>
                    </div>

                    <!-- Spin bonus active -->
                    <div
                      v-if="myDashboard.spinWheel?.hasBonus"
                      class="spin-bonus-banner mb-3"
                    >
                      <div class="spin-bonus-icon">🎁</div>
                      <div>
                        <div class="spin-bonus-value">
                          -{{ myDashboard.spinWheel?.currentBonus }}%
                        </div>
                        <div class="spin-bonus-label">
                          Bonus đang chờ áp dụng
                        </div>
                      </div>
                    </div>

                    <div class="spin-status-grid">
                      <div class="spin-status-item">
                        <div
                          class="spin-status-val"
                          :class="
                            myDashboard.spinWheel?.canSpin
                              ? 'text-success'
                              : 'text-muted'
                          "
                        >
                          {{
                            myDashboard.spinWheel?.canSpin
                              ? "✓ Sẵn sàng"
                              : "✗ Chưa thể"
                          }}
                        </div>
                        <div class="spin-status-key">Trạng thái</div>
                      </div>
                      <div class="spin-status-item">
                        <div class="spin-status-val">
                          {{ myDashboard.spinWheel?.spinsRemaining || 0 }}
                        </div>
                        <div class="spin-status-key">Lượt còn lại</div>
                      </div>
                    </div>

                    <div
                      v-if="myDashboard.spinWheel?.nextSpinAt"
                      class="spin-next-time mt-2"
                    >
                      <el-icon color="#909399"><Clock /></el-icon>
                      <span
                        >Quay lại lúc:
                        {{ myDashboard.spinWheel?.nextSpinAt }}</span
                      >
                    </div>

                    <el-divider />

                    <!-- Avg order -->
                    <div class="avg-order-section">
                      <div class="avg-label">Trung bình mỗi đơn</div>
                      <div class="avg-value">
                        {{ formatCurrency(myDashboard.summary?.avgOrderValue) }}
                      </div>
                    </div>

                    <!-- Last order -->
                    <div
                      class="last-order-section mt-3"
                      v-if="myDashboard.summary?.lastOrderAt"
                    >
                      <div class="avg-label">Đơn hàng gần nhất</div>
                      <div class="last-order-date">
                        {{ formatDateTime(myDashboard.summary?.lastOrderAt) }}
                      </div>
                    </div>

                    <!-- Spin bonus note -->
                    <div
                      v-if="
                        !myDashboard.spinWheel?.hasBonus &&
                        !myDashboard.spinWheel?.canSpin
                      "
                      class="spin-empty-note mt-3"
                    >
                      <el-icon color="#c0c4cc"><InfoFilled /></el-icon>
                      <span>Hiện chưa có bonus spin</span>
                    </div>
                  </el-card>
                </el-col>
              </el-row>

              <!-- ── Hàng 3: Recent Payments + Recent Loyalty + Promotions ── -->
              <el-row :gutter="16" class="mb-4">
                <!-- Recent Payments -->
                <el-col :xs="24" :md="12">
                  <el-card shadow="hover">
                    <div
                      class="d-flex align-items-center justify-content-between mb-3"
                    >
                      <div class="d-flex align-items-center gap-2">
                        <el-icon :size="18" color="#409eff"
                          ><CreditCard
                        /></el-icon>
                        <h4 class="mb-0 fw-bold">Giao dịch gần đây</h4>
                      </div>
                      <el-button
                        size="small"
                        text
                        type="primary"
                        @click="activeTab = 'payments'"
                        >Xem tất cả</el-button
                      >
                    </div>
                    <el-empty
                      v-if="!myDashboard.recentPayments?.length"
                      description="Chưa có giao dịch"
                      :image-size="60"
                    />
                    <div v-else class="recent-list">
                      <div
                        v-for="p in myDashboard.recentPayments"
                        :key="p.paymentId"
                        class="recent-item"
                      >
                        <div class="recent-item-left">
                          <div class="recent-item-icon payment-icon">
                            <el-icon color="#409eff"><CreditCard /></el-icon>
                          </div>
                          <div>
                            <div class="recent-item-title">
                              Đơn #{{ p.orderId }}
                            </div>
                            <div class="recent-item-sub">
                              {{ p.method }} · {{ p.paidAt }}
                            </div>
                          </div>
                        </div>
                        <div class="recent-item-right">
                          <div class="recent-item-amount">
                            {{ formatCurrency(p.amount) }}
                          </div>
                          <el-tag
                            :type="getPaymentStatusType(p.status)"
                            size="small"
                            >{{ p.status }}</el-tag
                          >
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>

                <!-- Recent Loyalty History -->
                <el-col :xs="24" :md="12">
                  <el-card shadow="hover">
                    <div
                      class="d-flex align-items-center justify-content-between mb-3"
                    >
                      <div class="d-flex align-items-center gap-2">
                        <el-icon :size="18" color="#f59e0b"><Star /></el-icon>
                        <h4 class="mb-0 fw-bold">Điểm gần đây</h4>
                      </div>
                      <el-button
                        size="small"
                        text
                        type="primary"
                        @click="activeTab = 'loyalty'"
                        >Xem tất cả</el-button
                      >
                    </div>
                    <el-empty
                      v-if="!myDashboard.recentLoyaltyHistory?.length"
                      description="Chưa có lịch sử điểm"
                      :image-size="60"
                    />
                    <div v-else class="recent-list">
                      <div
                        v-for="h in myDashboard.recentLoyaltyHistory"
                        :key="h.id"
                        class="recent-item"
                      >
                        <div class="recent-item-left">
                          <div
                            class="recent-item-icon"
                            :class="
                              h.pointsDelta > 0 ? 'earn-icon' : 'deduct-icon'
                            "
                          >
                            <el-icon
                              :color="h.pointsDelta > 0 ? '#67c23a' : '#e6a23c'"
                            >
                              <component
                                :is="
                                  h.pointsDelta > 0 ? 'CirclePlus' : 'Remove'
                                "
                              />
                            </el-icon>
                          </div>
                          <div>
                            <div class="recent-item-title">
                              {{
                                h.transactionTypeDisplay || h.transactionType
                              }}
                            </div>
                            <div class="recent-item-sub">
                              {{ formatDateTime(h.createdAt) }}
                            </div>
                          </div>
                        </div>
                        <div class="recent-item-right">
                          <div
                            class="recent-pts"
                            :class="
                              h.pointsDelta > 0 ? 'pts-earn' : 'pts-deduct'
                            "
                          >
                            {{ h.pointsDelta > 0 ? "+" : ""
                            }}{{ h.pointsDelta }}
                          </div>
                          <div class="recent-pts-label">điểm</div>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>

              <!-- ── Hàng 4: Recent Promotions ── -->
              <el-row v-if="myDashboard.recentPromotions?.length" :gutter="16">
                <el-col :xs="24">
                  <el-card shadow="hover">
                    <div
                      class="d-flex align-items-center justify-content-between mb-3"
                    >
                      <div class="d-flex align-items-center gap-2">
                        <el-icon :size="18" color="#9333ea"><Ticket /></el-icon>
                        <h4 class="mb-0 fw-bold">Ưu đãi gần đây</h4>
                      </div>
                      <el-button
                        size="small"
                        text
                        type="primary"
                        @click="activeTab = 'promotions'"
                        >Xem tất cả</el-button
                      >
                    </div>
                    <div class="promo-dash-list">
                      <div
                        v-for="p in myDashboard.recentPromotions"
                        :key="`dash-promo-${p.orderId}-${p.promotionCode}`"
                        class="promo-dash-item"
                      >
                        <div
                          class="promo-dash-icon"
                          :class="
                            p.type === 'SPIN_WHEEL' ? 'spin-icon' : 'code-icon'
                          "
                        >
                          <el-icon
                            ><component
                              :is="
                                p.type === 'SPIN_WHEEL' ? 'Trophy' : 'Ticket'
                              "
                          /></el-icon>
                        </div>
                        <div class="promo-dash-info">
                          <div class="promo-dash-name">
                            {{ p.promotionName }}
                          </div>
                          <div class="promo-dash-meta">
                            <el-tag
                              size="small"
                              :type="
                                p.type === 'SPIN_WHEEL' ? 'success' : 'primary'
                              "
                            >
                              {{
                                p.type === "SPIN_WHEEL"
                                  ? "Spin"
                                  : p.promotionCode
                              }}
                            </el-tag>
                            <el-tag
                              size="small"
                              :type="getPromoStatusTagType(p.status)"
                              >{{ p.status }}</el-tag
                            >
                          </div>
                        </div>
                        <div class="promo-dash-value">
                          <span class="promo-dash-discount">
                            {{
                              p.discountType === "PERCENT" ||
                              p.discountType === "percent"
                                ? `${p.discountValue}%`
                                : formatCurrencyShort(p.discountValue)
                            }}
                          </span>
                          <span class="promo-dash-unit">giảm</span>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </template>

            <!-- Empty / error state -->
            <el-empty v-else description="Không tải được dữ liệu dashboard">
              <el-button type="primary" @click="loadMyDashboard"
                >Thử lại</el-button
              >
            </el-empty>
          </el-tab-pane>

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
                    <div class="points-display">
                      {{ customer.loyaltyPoints?.toLocaleString() || 0 }}
                    </div>
                    <div class="muted">điểm</div>
                  </div>
                  <el-divider />
                  <div
                    class="d-flex justify-content-between align-items-center mb-2"
                  >
                    <span class="small text-muted">Lên hạng tiếp:</span>
                    <span class="fw-bold text-primary">
                      {{
                        customer.pointsToNextTier > 0
                          ? `${customer.pointsToNextTier} điểm`
                          : "Hạng cao nhất"
                      }}
                    </span>
                  </div>
                  <el-progress
                    v-if="customer.pointsToNextTier > 0"
                    :percentage="getProgressPercent()"
                    :stroke-width="8"
                    :show-text="false"
                  />
                </el-card>

                <el-card shadow="hover" class="mb-3 discount-card">
                  <div class="d-flex align-items-center gap-2 mb-3">
                    <el-icon :size="24"><Present /></el-icon>
                    <h3 class="mb-0 fw-bold">Ưu đãi của bạn</h3>
                  </div>
                  <div class="text-center py-3">
                    <div class="discount-display">
                      {{ customer.discountRate || 0 }}
                    </div>
                    <div class="discount-desc">Giảm giá mọi đơn hàng</div>
                  </div>
                </el-card>

                <el-card shadow="hover">
                  <div class="d-flex align-items-center gap-2 mb-3">
                    <el-icon :size="24" color="#9333ea"><Wallet /></el-icon>
                    <h3 class="mb-0 fw-bold">Tổng chi tiêu</h3>
                  </div>
                  <div class="total-spent">
                    {{ formatCurrency(customer.totalSpent) }}
                  </div>
                </el-card>
              </div>

              <div class="col-12 col-lg-8">
                <el-card shadow="never" class="mb-3 text-dark">
                  <h2 class="mb-4 fw-bold">Thông tin cá nhân</h2>
                  <el-form label-position="top" :model="formData">
                    <div class="row g-3">
                      <div class="col-12 col-md-6">
                        <el-form-item label="Họ và tên">
                          <el-input
                            v-if="editing"
                            v-model="formData.fullName"
                          />
                          <div v-else class="form-static">
                            {{ customer.name }}
                          </div>
                        </el-form-item>
                      </div>
                      <div class="col-12 col-md-6">
                        <el-form-item label="Email">
                          <el-input v-if="editing" v-model="formData.email" />
                          <div v-else class="form-static">
                            {{ customer.email }}
                          </div>
                        </el-form-item>
                      </div>
                      <div class="col-12 col-md-6">
                        <el-form-item label="Số điện thoại">
                          <el-input v-if="editing" v-model="formData.phone" />
                          <div v-else class="form-static">
                            {{ customer.phone }}
                          </div>
                        </el-form-item>
                      </div>
                      <div class="col-12 col-md-6">
                        <el-form-item label="Ngày sinh">
                          <el-date-picker
                            v-if="editing"
                            v-model="formData.birthDate"
                            type="date"
                            format="DD/MM/YYYY"
                            value-format="YYYY-MM-DD"
                            style="width: 100%"
                          />
                          <div v-else class="form-static">
                            {{ formatDate(customer.dateOfBirth) }}
                          </div>
                        </el-form-item>
                      </div>
                      <div class="col-12">
                        <el-form-item label="Địa chỉ">
                          <el-input
                            v-if="editing"
                            v-model="formData.address"
                            placeholder="Nhập địa chỉ"
                          />
                          <div v-else class="form-static">
                            {{ customer.address || "—" }}
                          </div>
                        </el-form-item>
                      </div>
                      <div class="col-12">
                        <el-form-item label="Ghi chú">
                          <el-input
                            v-if="editing"
                            v-model="formData.notes"
                            type="textarea"
                            :rows="3"
                          />
                          <div v-else class="form-static notes-static">
                            {{ customer.notes || "—" }}
                          </div>
                        </el-form-item>
                      </div>
                    </div>
                  </el-form>
                </el-card>

                <el-card shadow="never" class="text-dark">
                  <h2 class="mb-4 fw-bold">Thông tin tài khoản</h2>
                  <div class="row g-3">
                    <div class="col-12 col-md-6">
                      <label class="small fw-bold text-muted mb-2 d-block"
                        >Hạng thành viên</label
                      >
                      <el-tag
                        :type="getTierTagType(customer.vipTier)"
                        size="large"
                        effect="dark"
                        >{{ customer.vipTierDisplay }}</el-tag
                      >
                    </div>
                    <div class="col-12 col-md-6">
                      <label class="small fw-bold text-muted mb-2 d-block"
                        >Ngày tạo</label
                      >
                      <div class="form-static-info">
                        {{ formatDateTime(customer.createdAt) }}
                      </div>
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
                    <el-icon :size="32" class="stats-icon"
                      ><CirclePlus
                    /></el-icon>
                    <div>
                      <div class="stats-value">
                        +{{ loyaltyStats.totalEarned }}
                      </div>
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
                      <div class="stats-value">
                        {{ loyaltyStats.totalDeducted }}
                      </div>
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
                      <div class="stats-value">
                        {{ loyaltyStats.totalTransactions }}
                      </div>
                      <div class="stats-label">Giao dịch</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-card shadow="never">
              <div
                class="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-3"
              >
                <div>
                  <h3 class="mb-2 fw-bold">
                    <el-icon class="me-2"><TrendCharts /></el-icon>
                    Lịch sử giao dịch điểm
                  </h3>
                  <div
                    class="d-flex gap-2 flex-wrap"
                    v-if="activeHistoryFilters.length"
                  >
                    <el-tag
                      v-for="filter in activeHistoryFilters"
                      :key="filter.type"
                      :type="filter.tagType"
                      closable
                      @close="removeHistoryFilter(filter.type)"
                    >
                      {{ filter.label }}
                    </el-tag>
                  </div>
                </div>
                <div class="d-flex gap-2 flex-wrap">
                  <el-select
                    v-model="historyTransactionFilter"
                    placeholder="Loại giao dịch"
                    clearable
                    style="width: 160px"
                    @change="filterLoyaltyHistory"
                  >
                    <el-option label="Cộng điểm" value="EARN"
                      ><el-tag type="success" size="small"
                        >Cộng điểm</el-tag
                      ></el-option
                    >
                    <el-option label="Trừ điểm" value="DEDUCT"
                      ><el-tag type="warning" size="small"
                        >Trừ điểm</el-tag
                      ></el-option
                    >
                    <el-option label="Phạt" value="PENALTY"
                      ><el-tag type="danger" size="small"
                        >Phạt</el-tag
                      ></el-option
                    >
                    <el-option label="Thăng hạng" value="TIER_UPGRADE"
                      ><el-tag type="success" size="small"
                        >Thăng hạng</el-tag
                      ></el-option
                    >
                    <el-option label="Hạ hạng" value="TIER_DOWNGRADE"
                      ><el-tag type="info" size="small"
                        >Hạ hạng</el-tag
                      ></el-option
                    >
                  </el-select>
                  <el-date-picker
                    v-model="historyDateRange"
                    type="daterange"
                    range-separator="-"
                    start-placeholder="Từ ngày"
                    end-placeholder="Đến ngày"
                    format="DD/MM/YYYY"
                    value-format="YYYY-MM-DD"
                    style="width: 280px"
                    @change="filterLoyaltyHistory"
                  />
                  <el-button
                    @click="clearHistoryFilters"
                    v-if="hasActiveHistoryFilters"
                    ><el-icon><Close /></el-icon> Xóa bộ lọc</el-button
                  >
                  <el-button
                    @click="loadLoyaltyHistory"
                    :loading="historyLoading"
                    ><el-icon><Refresh /></el-icon> Tải lại</el-button
                  >
                </div>
              </div>

              <el-divider />
              <el-skeleton v-if="historyLoading" :rows="5" animated />
              <el-empty
                v-else-if="!filteredLoyaltyHistory.length"
                description="Không có giao dịch nào"
              >
                <el-button
                  type="primary"
                  @click="clearHistoryFilters"
                  v-if="hasActiveHistoryFilters"
                  >Xóa bộ lọc</el-button
                >
              </el-empty>

              <el-timeline v-else>
                <el-timeline-item
                  v-for="item in filteredLoyaltyHistory"
                  :key="item.id"
                  :timestamp="formatDateTime(item.createdAt)"
                  placement="top"
                  :type="getTimelineType(item.transactionType)"
                >
                  <el-card
                    class="history-card"
                    :class="getHistoryCardClass(item.transactionType)"
                  >
                    <div class="history-card-content">
                      <div class="history-icon-wrapper">
                        <el-icon
                          :size="28"
                          :color="getTransactionIconColor(item.transactionType)"
                        >
                          <component
                            :is="getTransactionIcon(item.transactionType)"
                          />
                        </el-icon>
                      </div>
                      <div class="history-details flex-grow-1">
                        <div class="d-flex align-items-center gap-2 mb-1">
                          <el-tag
                            :type="getTransactionTagType(item.transactionType)"
                            effect="dark"
                          >
                            {{ item.transactionTypeDisplay }}
                          </el-tag>
                          <span class="text-muted small" v-if="item.reason">{{
                            item.reason
                          }}</span>
                        </div>
                        <p class="mb-2 history-note">{{ item.note }}</p>
                        <div
                          v-if="item.tierBefore || item.tierAfter"
                          class="tier-change-indicator"
                        >
                          <el-tag
                            size="small"
                            :type="getTierTagType(item.tierBefore)"
                            effect="plain"
                          >
                            {{ item.tierBeforeDisplay || "Member" }}
                          </el-tag>
                          <el-icon class="mx-2" :size="16"><Right /></el-icon>
                          <el-tag
                            size="small"
                            :type="getTierTagType(item.tierAfter)"
                            effect="dark"
                          >
                            {{ item.tierAfterDisplay || "Member" }}
                          </el-tag>
                        </div>
                        <div
                          v-if="item.referenceType && item.referenceId"
                          class="mt-2"
                        >
                          <el-button
                            size="small"
                            type="primary"
                            plain
                            round
                            class="order-bill-btn"
                            @click="
                              viewOrderBill(item.referenceId, item.pointsDelta)
                            "
                          >
                            <el-icon class="me-1"><ShoppingCart /></el-icon>
                            Đơn hàng #{{ item.referenceId }}
                          </el-button>
                        </div>
                      </div>
                      <div class="history-points">
                        <div
                          class="points-badge"
                          :class="
                            item.pointsDelta > 0
                              ? 'points-positive'
                              : 'points-negative'
                          "
                        >
                          <span class="points-sign">{{
                            item.pointsDelta > 0 ? "+" : ""
                          }}</span>
                          <span class="points-value">{{
                            Math.abs(item.pointsDelta)
                          }}</span>
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
              <div
                class="d-flex justify-content-between align-items-center mb-3"
              >
                <h3 class="mb-0 fw-bold">
                  <el-icon class="me-2"><Trophy /></el-icon>Lịch sử thay đổi
                  hạng VIP
                </h3>
                <el-button @click="loadTierHistory" :loading="tierLoading"
                  ><el-icon><Refresh /></el-icon> Tải lại</el-button
                >
              </div>
              <el-skeleton v-if="tierLoading" :rows="5" animated />
              <el-empty
                v-else-if="!tierHistory.length"
                description="Chưa có lịch sử thay đổi hạng"
              />
              <div v-else class="tier-history-list">
                <el-card
                  v-for="item in tierHistory"
                  :key="item.id"
                  class="mb-3 tier-history-card"
                  :class="
                    item.transactionType === 'TIER_UPGRADE'
                      ? 'upgrade'
                      : 'downgrade'
                  "
                >
                  <div class="d-flex align-items-start gap-3">
                    <div class="tier-icon">
                      <el-icon
                        :size="32"
                        :color="
                          item.transactionType === 'TIER_UPGRADE'
                            ? '#67c23a'
                            : '#f56c6c'
                        "
                      >
                        <component
                          :is="
                            item.transactionType === 'TIER_UPGRADE'
                              ? 'TopRight'
                              : 'BottomRight'
                          "
                        />
                      </el-icon>
                    </div>
                    <div class="flex-grow-1">
                      <div
                        class="d-flex justify-content-between align-items-center mb-2"
                      >
                        <h5 class="mb-0">
                          <el-tag
                            :type="
                              item.transactionType === 'TIER_UPGRADE'
                                ? 'success'
                                : 'danger'
                            "
                            effect="dark"
                            >{{ item.transactionTypeDisplay }}</el-tag
                          >
                        </h5>
                        <span class="text-muted small">{{
                          formatDateTime(item.createdAt)
                        }}</span>
                      </div>
                      <div class="tier-change-display mb-2">
                        <el-tag
                          size="large"
                          :type="getTierTagType(item.tierBefore)"
                          effect="dark"
                          >{{ item.tierBeforeDisplay || "Member" }}</el-tag
                        >
                        <el-icon :size="20" class="mx-2"><Right /></el-icon>
                        <el-tag
                          size="large"
                          :type="getTierTagType(item.tierAfter)"
                          effect="dark"
                          >{{ item.tierAfterDisplay || "Member" }}</el-tag
                        >
                      </div>
                      <p class="mb-0 text-muted">{{ item.note }}</p>
                    </div>
                  </div>
                </el-card>
              </div>
            </el-card>
          </el-tab-pane>

          <!-- TAB: LỊCH SỬ ƯU ĐÃI -->
          <el-tab-pane name="promotions">
            <template #label>
              <span class="d-flex align-items-center gap-1">
                <el-icon><Ticket /></el-icon> Lịch sử ưu đãi
              </span>
            </template>

            <el-row :gutter="16" class="mb-3">
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card promo-total-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><Ticket /></el-icon>
                    <div>
                      <div class="stats-value">{{ promoStats.total }}</div>
                      <div class="stats-label">Tổng ưu đãi</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card promo-used-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"
                      ><CircleCheck
                    /></el-icon>
                    <div>
                      <div class="stats-value">{{ promoStats.used }}</div>
                      <div class="stats-label">Đã sử dụng</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card promo-saved-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><Money /></el-icon>
                    <div>
                      <div class="stats-value stats-value-small">
                        {{ formatCurrencyShort(promoStats.totalSaved) }}
                      </div>
                      <div class="stats-label">Tổng tiết kiệm</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-card shadow="never">
              <div
                class="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-3"
              >
                <h3 class="mb-0 fw-bold">
                  <el-icon class="me-2"><Ticket /></el-icon>
                  Danh sách ưu đãi đã nhận
                </h3>
                <div class="d-flex gap-2 flex-wrap">
                  <el-select
                    v-model="promoTypeFilter"
                    placeholder="Loại ưu đãi"
                    clearable
                    style="width: 160px"
                    @change="filterPromoHistory"
                  >
                    <el-option label="Mã khuyến mãi" value="PROMOTION_CODE">
                      <el-tag type="primary" size="small">Mã KM</el-tag>
                    </el-option>
                    <el-option label="Vòng quay" value="SPIN_WHEEL">
                      <el-tag type="success" size="small">Spin</el-tag>
                    </el-option>
                  </el-select>
                  <el-select
                    v-model="promoStatusFilter"
                    placeholder="Trạng thái"
                    clearable
                    style="width: 150px"
                    @change="filterPromoHistory"
                  >
                    <el-option label="Đã sử dụng" value="Đã sử dụng" />
                    <el-option label="Đang hoạt động" value="Đang hoạt động" />
                    <el-option label="Đã hết hạn" value="Đã hết hạn" />
                  </el-select>
                  <el-button
                    @click="loadPromotionHistory"
                    :loading="promoLoading"
                  >
                    <el-icon><Refresh /></el-icon> Tải lại
                  </el-button>
                </div>
              </div>

              <el-divider />
              <el-skeleton v-if="promoLoading" :rows="5" animated />
              <el-empty
                v-else-if="!filteredPromoHistory.length"
                description="Không có ưu đãi nào"
              />

              <div v-else class="promo-history-list">
                <el-card
                  v-for="item in filteredPromoHistory"
                  :key="`${item.type}-${item.orderId}-${item.promotionCode}`"
                  class="mb-3 promo-history-card"
                  :class="getPromoCardClass(item)"
                  shadow="hover"
                >
                  <div class="promo-card-content">
                    <div
                      class="promo-icon-wrapper"
                      :class="
                        item.type === 'SPIN_WHEEL' ? 'spin-icon' : 'code-icon'
                      "
                    >
                      <el-icon :size="28">
                        <component
                          :is="item.type === 'SPIN_WHEEL' ? 'Trophy' : 'Ticket'"
                        />
                      </el-icon>
                    </div>
                    <div class="promo-details flex-grow-1">
                      <div
                        class="d-flex align-items-center gap-2 mb-1 flex-wrap"
                      >
                        <el-tag
                          :type="
                            item.type === 'SPIN_WHEEL' ? 'success' : 'primary'
                          "
                          effect="dark"
                          size="small"
                        >
                          {{
                            item.type === "SPIN_WHEEL"
                              ? "🎡 Vòng quay"
                              : "🎟 Mã KM"
                          }}
                        </el-tag>
                        <el-tag
                          :type="getPromoStatusTagType(item.status)"
                          size="small"
                          >{{ item.status }}</el-tag
                        >
                        <el-tag
                          type="info"
                          effect="plain"
                          size="small"
                          v-if="item.type === 'PROMOTION_CODE'"
                          >{{ item.promotionCode }}</el-tag
                        >
                      </div>
                      <div class="promo-name fw-bold mb-1">
                        {{ item.promotionName }}
                      </div>
                      <div class="d-flex gap-3 flex-wrap promo-meta">
                        <span class="promo-meta-item">
                          <el-icon color="#67c23a"><Money /></el-icon>
                          Giảm:
                          <strong class="text-success">{{
                            item.discountType === "PERCENT" ||
                            item.discountType === "percent"
                              ? `${item.discountValue}%`
                              : formatCurrency(item.discountValue)
                          }}</strong>
                        </span>
                        <span class="promo-meta-item" v-if="item.discountTotal">
                          <el-icon color="#409eff"><Wallet /></el-icon>
                          Tiết kiệm:
                          <strong class="text-primary">{{
                            formatCurrency(item.discountTotal)
                          }}</strong>
                        </span>
                        <span class="promo-meta-item" v-if="item.expiresAt">
                          <el-icon color="#e6a23c"><Clock /></el-icon>
                          HSD: {{ formatDate(item.expiresAt) }}
                        </span>
                        <span class="promo-meta-item" v-if="item.usedAt">
                          <el-icon color="#909399"><Timer /></el-icon>
                          {{
                            item.status === "Đã sử dụng"
                              ? "Dùng lúc"
                              : "Nhận lúc"
                          }}: {{ formatDateTime(item.usedAt) }}
                        </span>
                      </div>
                      <div v-if="item.orderId" class="mt-2">
                        <el-button
                          size="small"
                          type="primary"
                          plain
                          round
                          class="order-bill-btn"
                          @click="viewOrderBill(item.orderId, 0)"
                        >
                          <el-icon class="me-1"><ShoppingCart /></el-icon>
                          Xem đơn #{{ item.orderId }}
                        </el-button>
                      </div>
                    </div>
                    <div class="promo-discount-badge">
                      <div
                        class="discount-big"
                        :class="
                          item.type === 'SPIN_WHEEL'
                            ? 'spin-color'
                            : 'code-color'
                        "
                      >
                        {{
                          item.discountType === "PERCENT" ||
                          item.discountType === "percent"
                            ? `${item.discountValue}%`
                            : formatCurrencyShort(item.discountValue)
                        }}
                      </div>
                      <div class="discount-label">giảm giá</div>
                    </div>
                  </div>
                </el-card>
              </div>
            </el-card>
          </el-tab-pane>

          <!-- TAB: TỔNG HỢP ĐIỂM -->
          <el-tab-pane name="summary">
            <template #label>
              <span class="d-flex align-items-center gap-1">
                <el-icon><TrendCharts /></el-icon> Tổng hợp điểm
              </span>
            </template>

            <div class="d-flex align-items-center gap-3 mb-4 flex-wrap">
              <div class="d-flex align-items-center gap-2">
                <span class="fw-bold text-muted small">Chu kỳ:</span>
                <el-radio-group
                  v-model="summaryMode"
                  size="small"
                  @change="loadLoyaltySummary"
                >
                  <el-radio-button value="weekly">Theo tuần</el-radio-button>
                  <el-radio-button value="monthly">Theo tháng</el-radio-button>
                </el-radio-group>
              </div>
              <div class="d-flex align-items-center gap-2">
                <span class="fw-bold text-muted small">Hiển thị:</span>
                <el-select
                  v-model="summaryRange"
                  style="width: 130px"
                  size="small"
                  @change="loadLoyaltySummary"
                >
                  <template v-if="summaryMode === 'weekly'">
                    <el-option label="4 tuần" :value="4" />
                    <el-option label="8 tuần" :value="8" />
                    <el-option label="12 tuần" :value="12" />
                    <el-option label="26 tuần" :value="26" />
                  </template>
                  <template v-else>
                    <el-option label="3 tháng" :value="3" />
                    <el-option label="6 tháng" :value="6" />
                    <el-option label="12 tháng" :value="12" />
                    <el-option label="24 tháng" :value="24" />
                  </template>
                </el-select>
              </div>
              <el-button
                size="small"
                @click="loadLoyaltySummary"
                :loading="summaryLoading"
              >
                <el-icon><Refresh /></el-icon> Tải lại
              </el-button>
            </div>

            <el-skeleton v-if="summaryLoading" :rows="6" animated />
            <el-empty
              v-else-if="!loyaltySummary.length"
              description="Chưa có dữ liệu tổng hợp"
            />

            <template v-else>
              <el-row :gutter="16" class="mb-4">
                <el-col :xs="24" :sm="8">
                  <el-card shadow="hover" class="stats-card earn-card">
                    <div class="stats-content">
                      <el-icon :size="32" class="stats-icon"
                        ><CirclePlus
                      /></el-icon>
                      <div>
                        <div class="stats-value">
                          +{{ summaryTotals.earned.toLocaleString() }}
                        </div>
                        <div class="stats-label">Tổng điểm cộng</div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :xs="24" :sm="8">
                  <el-card shadow="hover" class="stats-card deduct-card">
                    <div class="stats-content">
                      <el-icon :size="32" class="stats-icon"
                        ><Remove
                      /></el-icon>
                      <div>
                        <div class="stats-value">
                          {{ summaryTotals.deducted.toLocaleString() }}
                        </div>
                        <div class="stats-label">Tổng điểm trừ</div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :xs="24" :sm="8">
                  <el-card shadow="hover" class="stats-card transactions-card">
                    <div class="stats-content">
                      <el-icon :size="32" class="stats-icon"
                        ><TrendCharts
                      /></el-icon>
                      <div>
                        <div
                          class="stats-value"
                          :class="
                            summaryTotals.net >= 0
                              ? 'text-success'
                              : 'text-danger'
                          "
                        >
                          {{ summaryTotals.net >= 0 ? "+" : ""
                          }}{{ summaryTotals.net.toLocaleString() }}
                        </div>
                        <div class="stats-label">Net điểm</div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>

              <el-card shadow="never">
                <h4 class="mb-3 fw-bold">
                  Chi tiết
                  {{ summaryMode === "weekly" ? "theo tuần" : "theo tháng" }}
                </h4>
                <el-table :data="loyaltySummary" stripe style="width: 100%">
                  <el-table-column label="Kỳ" min-width="160">
                    <template #default="{ row }">
                      <div class="fw-bold">{{ row.periodLabel }}</div>
                      <div class="text-muted" style="font-size: 11px">
                        {{ formatDate(row.periodStart) }} –
                        {{ formatDate(row.periodEnd) }}
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="Điểm cộng" width="130" align="right">
                    <template #default="{ row }"
                      ><span class="fw-bold text-success"
                        >+{{ row.totalPointsEarned.toLocaleString() }}</span
                      ></template
                    >
                  </el-table-column>
                  <el-table-column label="Điểm trừ" width="130" align="right">
                    <template #default="{ row }"
                      ><span class="fw-bold text-warning"
                        >{{ row.totalPointsDeducted > 0 ? "-" : ""
                        }}{{ row.totalPointsDeducted.toLocaleString() }}</span
                      ></template
                    >
                  </el-table-column>
                  <el-table-column label="Net" width="120" align="right">
                    <template #default="{ row }">
                      <el-tag
                        :type="row.netPoints >= 0 ? 'success' : 'danger'"
                        effect="dark"
                        size="small"
                        >{{ row.netPoints >= 0 ? "+" : ""
                        }}{{ row.netPoints.toLocaleString() }}</el-tag
                      >
                    </template>
                  </el-table-column>
                  <el-table-column label="GD" width="90" align="center">
                    <template #default="{ row }"
                      ><span class="text-muted">{{
                        row.totalTransactions
                      }}</span></template
                    >
                  </el-table-column>
                  <el-table-column label="Biểu đồ" min-width="160">
                    <template #default="{ row }">
                      <div class="summary-bar-wrap">
                        <div
                          class="summary-bar-earn"
                          :style="{
                            width:
                              getSummaryBarWidth(
                                row.totalPointsEarned,
                                'earn',
                              ) + '%',
                          }"
                          :title="`+${row.totalPointsEarned} điểm cộng`"
                        />
                        <div
                          class="summary-bar-deduct"
                          :style="{
                            width:
                              getSummaryBarWidth(
                                row.totalPointsDeducted,
                                'deduct',
                              ) + '%',
                          }"
                          :title="`-${row.totalPointsDeducted} điểm trừ`"
                        />
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </template>
          </el-tab-pane>

          <!-- TAB: LỊCH SỬ GIAO DỊCH -->
          <el-tab-pane label="Lịch sử giao dịch" name="payments">
            <el-row :gutter="16" class="mb-3">
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card payment-success-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"
                      ><CircleCheck
                    /></el-icon>
                    <div>
                      <div class="stats-value">
                        {{ paymentStats.totalSuccess }}
                      </div>
                      <div class="stats-label">Giao dịch thành công</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card shadow="hover" class="stats-card payment-amount-card">
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"><Money /></el-icon>
                    <div>
                      <div class="stats-value stats-value-small">
                        {{ formatCurrencyShort(paymentStats.totalAmount) }}
                      </div>
                      <div class="stats-label">Tổng thanh toán</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="8">
                <el-card
                  shadow="hover"
                  class="stats-card payment-refunded-card"
                >
                  <div class="stats-content">
                    <el-icon :size="32" class="stats-icon"
                      ><RefreshLeft
                    /></el-icon>
                    <div>
                      <div class="stats-value">
                        {{ paymentStats.totalRefunded }}
                      </div>
                      <div class="stats-label">Đã hoàn tiền</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-card shadow="never">
              <div
                class="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-3"
              >
                <div>
                  <h3 class="mb-2 fw-bold">
                    <el-icon class="me-2"><CreditCard /></el-icon>Danh sách giao
                    dịch
                  </h3>
                  <div
                    class="d-flex gap-2 flex-wrap"
                    v-if="activePaymentFilters.length"
                  >
                    <el-tag
                      v-for="filter in activePaymentFilters"
                      :key="filter.type"
                      :type="filter.tagType"
                      closable
                      @close="removePaymentFilter(filter.type)"
                      >{{ filter.label }}</el-tag
                    >
                  </div>
                </div>
                <div class="d-flex gap-2 flex-wrap">
                  <el-select
                    v-model="paymentStatusFilter"
                    placeholder="Trạng thái"
                    clearable
                    style="width: 160px"
                    @change="filterPayments"
                  >
                    <el-option label="Thành công" value="SUCCESS"
                      ><el-tag type="success" size="small"
                        >SUCCESS</el-tag
                      ></el-option
                    >
                    <el-option label="Đang chờ" value="PENDING"
                      ><el-tag type="warning" size="small"
                        >PENDING</el-tag
                      ></el-option
                    >
                    <el-option label="Đã hoàn tiền" value="REFUNDED"
                      ><el-tag type="info" size="small"
                        >REFUNDED</el-tag
                      ></el-option
                    >
                  </el-select>
                  <el-select
                    v-model="paymentMethodFilter"
                    placeholder="Phương thức"
                    clearable
                    style="width: 160px"
                    @change="filterPayments"
                  >
                    <el-option label="Tiền mặt" value="CASH" />
                    <el-option label="Chuyển khoản" value="BANK_TRANSFER" />
                    <el-option label="Thẻ tín dụng" value="CREDIT_CARD" />
                    <el-option label="Ví điện tử" value="E_WALLET" />
                  </el-select>
                  <el-date-picker
                    v-model="paymentDateRange"
                    type="daterange"
                    range-separator="-"
                    start-placeholder="Từ ngày"
                    end-placeholder="Đến ngày"
                    format="DD/MM/YYYY"
                    value-format="YYYY-MM-DD"
                    style="width: 280px"
                    @change="filterPayments"
                  />
                  <el-switch
                    v-model="showDeletedPayments"
                    active-text="Hiện đã xóa"
                    @change="loadPayments"
                  />
                  <el-button
                    @click="clearPaymentFilters"
                    v-if="hasActivePaymentFilters"
                    ><el-icon><Close /></el-icon> Xóa bộ lọc</el-button
                  >
                  <el-button @click="loadPayments" :loading="paymentsLoading"
                    ><el-icon><Refresh /></el-icon> Tải lại</el-button
                  >
                </div>
              </div>

              <el-divider />
              <el-skeleton v-if="paymentsLoading" :rows="5" animated />
              <el-empty
                v-else-if="!filteredPayments.length"
                description="Không có giao dịch nào"
              >
                <el-button
                  type="primary"
                  @click="clearPaymentFilters"
                  v-if="hasActivePaymentFilters"
                  >Xóa bộ lọc</el-button
                >
              </el-empty>

              <el-table
                v-else
                :data="paginatedPayments"
                style="width: 100%"
                stripe
              >
                <el-table-column prop="id" label="ID" width="80" sortable />
                <el-table-column label="Mã GD" width="150">
                  <template #default="{ row }"
                    ><span class="text-muted small">{{
                      row.transactionRef
                    }}</span></template
                  >
                </el-table-column>
                <el-table-column label="Đơn hàng" width="150">
                  <template #default="{ row }"
                    ><span class="fw-bold">#{{ row.orderId }}</span></template
                  >
                </el-table-column>
                <el-table-column
                  label="Số tiền"
                  width="150"
                  sortable
                  prop="amount"
                >
                  <template #default="{ row }"
                    ><span class="fw-bold">{{
                      formatCurrency(row.amount)
                    }}</span></template
                  >
                </el-table-column>
                <el-table-column label="Phương thức" width="140">
                  <template #default="{ row }"
                    ><el-tag size="small" type="info">{{
                      getPaymentMethodLabel(row.method)
                    }}</el-tag></template
                  >
                </el-table-column>
                <el-table-column label="Trạng thái" width="120">
                  <template #default="{ row }"
                    ><el-tag
                      :type="getPaymentStatusType(row.status)"
                      size="small"
                      >{{ row.status }}</el-tag
                    ></template
                  >
                </el-table-column>
                <el-table-column
                  label="Thời gian"
                  width="180"
                  sortable
                  prop="createdAt"
                >
                  <template #default="{ row }">{{
                    formatDateTime(row.createdAt)
                  }}</template>
                </el-table-column>
                <el-table-column
                  label="Trạng thái"
                  width="100"
                  v-if="showDeletedPayments"
                >
                  <template #default="{ row }">
                    <el-tag v-if="row.deletedAt" type="danger" size="small"
                      >Đã xóa</el-tag
                    >
                    <el-tag v-else type="success" size="small"
                      >Hoạt động</el-tag
                    >
                  </template>
                </el-table-column>
                <el-table-column label="Thao tác" width="200" fixed="right">
                  <template #default="{ row }">
                    <div class="d-flex gap-1">
                      <el-button
                        size="small"
                        type="primary"
                        text
                        @click="viewPaymentDetail(row)"
                        ><el-icon><View /></el-icon> Chi tiết</el-button
                      >
                      <el-popconfirm
                        v-if="!row.deletedAt"
                        title="Xác nhận xóa giao dịch?"
                        confirm-button-text="Xóa"
                        cancel-button-text="Hủy"
                        @confirm="softDeletePayment(row.id)"
                      >
                        <template #reference
                          ><el-button size="small" type="danger" text
                            ><el-icon><Delete /></el-icon></el-button
                        ></template>
                      </el-popconfirm>
                      <el-popconfirm
                        v-else
                        title="Khôi phục giao dịch?"
                        confirm-button-text="Khôi phục"
                        cancel-button-text="Hủy"
                        @confirm="restorePayment(row.id)"
                      >
                        <template #reference
                          ><el-button size="small" type="success" text
                            ><el-icon><RefreshLeft /></el-icon></el-button
                        ></template>
                      </el-popconfirm>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <div
                class="d-flex justify-content-center mt-4"
                v-if="filteredPayments.length"
              >
                <el-pagination
                  v-model:current-page="paymentCurrentPage"
                  v-model:page-size="paymentPageSize"
                  :page-sizes="[10, 20, 50]"
                  :total="filteredPayments.length"
                  layout="total, sizes, prev, pager, next"
                />
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </template>
    </div>

    <!-- ===== ORDER BILL DIALOG ===== -->
    <el-dialog
      v-model="showOrderBillDialog"
      width="700px"
      :close-on-click-modal="true"
      class="order-bill-dialog"
    >
      <template #header>
        <div class="bill-dialog-header">
          <div class="bill-header-left">
            <el-icon :size="20" color="#409eff"><ShoppingCart /></el-icon>
            <span class="bill-title">Chi tiết đơn hàng</span>
            <el-tag type="primary" effect="dark" size="small" v-if="orderBill"
              >#{{ orderBill.id }}</el-tag
            >
          </div>
          <el-tag type="info" size="small" v-if="orderBill">{{
            orderBill.orderNumber
          }}</el-tag>
        </div>
      </template>
      <el-skeleton v-if="loadingOrderBill" :rows="8" animated />
      <div v-else-if="orderBill" class="bill-body">
        <div class="points-earned-banner">
          <el-icon :size="18" color="#f59e0b"><Star /></el-icon>
          <span>Đơn hàng này tích lũy</span>
          <span class="points-earned-value"
            >+{{ Math.abs(currentBillPoints).toLocaleString() }} điểm</span
          >
          <span class="points-rate">(10.000 VNĐ = 1 điểm)</span>
        </div>
        <div class="bill-meta">
          <div class="bill-meta-item">
            <span class="bill-meta-label">Ngày đặt</span
            ><span class="bill-meta-value">{{
              formatDateTime(orderBill.createdAt)
            }}</span>
          </div>
          <div class="bill-meta-item">
            <span class="bill-meta-label">Trạng thái</span
            ><el-tag
              :type="getOrderStatusType(orderBill.status)"
              size="small"
              >{{ orderBill.status }}</el-tag
            >
          </div>
          <div class="bill-meta-item">
            <span class="bill-meta-label">Thanh toán</span
            ><el-tag
              :type="orderBill.paymentStatus === 'PAID' ? 'success' : 'warning'"
              size="small"
              >{{ orderBill.paymentStatus }}</el-tag
            >
          </div>
          <div class="bill-meta-item">
            <span class="bill-meta-label">Kênh</span
            ><span class="bill-meta-value">{{ orderBill.channel || "—" }}</span>
          </div>
        </div>
        <el-divider />
        <div class="bill-items">
          <div class="bill-items-header">
            <span>Sản phẩm</span><span style="text-align: center">SL</span
            ><span style="text-align: right">Đơn giá</span
            ><span style="text-align: right">Giảm giá</span
            ><span style="text-align: right">Thành tiền</span>
          </div>
          <div
            v-for="(item, i) in orderBill.items"
            :key="i"
            class="bill-item-row"
          >
            <div>
              <div class="item-product-name">{{ item.productName }}</div>
              <div class="item-variant-name">{{ item.variantName }}</div>
              <div class="item-sku">SKU: {{ item.sku }}</div>
            </div>
            <div style="text-align: center">
              <span class="qty-badge">{{ item.quantity }}</span>
            </div>
            <div style="text-align: right; color: #606266">
              {{ formatCurrency(item.unitPrice) }}
            </div>
            <div style="text-align: right" class="text-success">
              {{
                item.discount > 0 ? "-" + formatCurrency(item.discount) : "—"
              }}
            </div>
            <div style="text-align: right; font-weight: 700">
              {{ formatCurrency(item.lineTotal) }}
            </div>
          </div>
        </div>
        <el-divider />
        <div class="bill-totals">
          <div class="bill-total-row">
            <span>Tạm tính</span
            ><span>{{ formatCurrency(orderBill.subtotal) }}</span>
          </div>
          <div
            class="bill-total-row text-success"
            v-if="orderBill.discountTotal > 0"
          >
            <span>Giảm giá</span
            ><span>-{{ formatCurrency(orderBill.discountTotal) }}</span>
          </div>
          <div class="bill-total-row" v-if="orderBill.taxTotal > 0">
            <span>Thuế</span
            ><span>{{ formatCurrency(orderBill.taxTotal) }}</span>
          </div>
          <div class="bill-total-row" v-if="orderBill.shippingFee > 0">
            <span>Phí vận chuyển</span
            ><span>{{ formatCurrency(orderBill.shippingFee) }}</span>
          </div>
          <div class="bill-total-row bill-grand-total">
            <span>Tổng cộng</span
            ><span>{{ formatCurrency(orderBill.totalAmount) }}</span>
          </div>
          <div class="bill-points-row">
            <span
              ><el-icon
                color="#f59e0b"
                style="vertical-align: middle; margin-right: 4px"
                ><Star /></el-icon
              >Điểm tích lũy được</span
            ><span class="points-earned-inline"
              >+{{ Math.abs(currentBillPoints).toLocaleString() }} điểm</span
            >
          </div>
        </div>
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
    <el-dialog
      v-model="showPaymentDetailDialog"
      title="Chi tiết giao dịch"
      width="800px"
    >
      <el-skeleton v-if="loadingPaymentDetail" :rows="8" animated />
      <div v-else-if="selectedPayment" class="payment-detail">
        <el-descriptions :column="2" border class="mb-3">
          <el-descriptions-item label="Mã GD"
            ><span class="fw-bold">{{
              selectedPayment.transactionRef
            }}</span></el-descriptions-item
          >
          <el-descriptions-item label="Số tiền"
            ><span class="fw-bold text-success">{{
              formatCurrency(selectedPayment.amount)
            }}</span></el-descriptions-item
          >
          <el-descriptions-item label="Phương thức"
            ><el-tag size="small">{{
              getPaymentMethodLabel(selectedPayment.method)
            }}</el-tag></el-descriptions-item
          >
          <el-descriptions-item label="Trạng thái"
            ><el-tag :type="getPaymentStatusType(selectedPayment.status)">{{
              selectedPayment.status
            }}</el-tag></el-descriptions-item
          >
          <el-descriptions-item label="Thời gian">{{
            formatDateTime(selectedPayment.paidAt)
          }}</el-descriptions-item>
        </el-descriptions>
        <h4 class="mb-3">Thông tin đơn hàng</h4>
        <el-descriptions :column="2" border class="mb-3">
          <el-descriptions-item label="Mã đơn"
            ><span class="fw-bold"
              >#{{ selectedPayment.orderId }}</span
            ></el-descriptions-item
          >
          <el-descriptions-item label="Trạng thái đơn"
            ><el-tag :type="getOrderStatusType(selectedPayment.orderStatus)">{{
              selectedPayment.orderStatus
            }}</el-tag></el-descriptions-item
          >
        </el-descriptions>
        <div v-if="selectedPayment.items && selectedPayment.items.length">
          <h4 class="mb-3">Sản phẩm</h4>
          <el-table :data="selectedPayment.items" border class="mb-3">
            <el-table-column label="Sản phẩm" min-width="200">
              <template #default="{ row }"
                ><div class="fw-bold">{{ row.productName }}</div>
                <div class="text-muted small">
                  {{ row.variantName }}
                </div></template
              >
            </el-table-column>
            <el-table-column
              label="SL"
              width="80"
              align="center"
              prop="quantity"
            />
            <el-table-column label="Đơn giá" width="130" align="right">
              <template #default="{ row }">{{
                formatCurrency(row.unitPrice)
              }}</template>
            </el-table-column>
            <el-table-column label="Tổng" width="150" align="right">
              <template #default="{ row }"
                ><strong>{{ formatCurrency(row.lineTotal) }}</strong></template
              >
            </el-table-column>
          </el-table>
          <div class="payment-totals">
            <div class="d-flex justify-content-between mb-2">
              <span>Tạm tính:</span
              ><strong>{{ formatCurrency(selectedPayment.subtotal) }}</strong>
            </div>
            <div
              class="d-flex justify-content-between mb-2 text-success"
              v-if="selectedPayment.discountTotal > 0"
            >
              <span>Giảm giá (VIP + Spin):</span
              ><strong
                >-{{ formatCurrency(selectedPayment.discountTotal) }}</strong
              >
            </div>
            <div class="d-flex justify-content-between mb-2">
              <span>Phí ship:</span
              ><strong>{{
                formatCurrency(selectedPayment.shippingFee)
              }}</strong>
            </div>
            <el-divider />
            <div class="d-flex justify-content-between fs-5">
              <span class="fw-bold">Tổng cộng:</span
              ><strong class="text-primary">{{
                formatCurrency(selectedPayment.amount)
              }}</strong>
            </div>
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
import { ref, onMounted, computed } from "vue";
import {
  Star,
  Present,
  Wallet,
  TrendCharts,
  Trophy,
  Refresh,
  Right,
  TopRight,
  BottomRight,
  CirclePlus,
  Remove,
  List,
  Close,
  CircleCheck,
  Money,
  RefreshLeft,
  CreditCard,
  View,
  Delete,
  ShoppingCart,
  InfoFilled,
  Ticket,
  Clock,
  Timer,
  DataAnalysis,
  Check,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { customersApi } from "../../api/customers.api";
import { ordersApi } from "../../api/orders.api";

const router = useRouter();

const loading = ref(true);
const editing = ref(false);
const saving = ref(false);
const customer = ref(null);
const activeTab = ref("dashboard");

// ── Dashboard state (MỚI) ─────────────────────────────────────────────────────
const dashboardLoading = ref(false);
const myDashboard = ref(null);

const historyLoading = ref(false);
const loyaltyHistory = ref([]);
const historyTransactionFilter = ref("");
const historyDateRange = ref(null);

const tierLoading = ref(false);
const tierHistory = ref([]);

const promoLoading = ref(false);
const promotionHistory = ref([]);
const promoTypeFilter = ref("");
const promoStatusFilter = ref("");

const paymentsLoading = ref(false);
const payments = ref([]);
const showDeletedPayments = ref(false);
const paymentStatusFilter = ref("");
const paymentMethodFilter = ref("");
const paymentDateRange = ref(null);
const paymentCurrentPage = ref(1);
const paymentPageSize = ref(10);
const showPaymentDetailDialog = ref(false);
const selectedPayment = ref(null);
const loadingPaymentDetail = ref(false);

const showOrderBillDialog = ref(false);
const loadingOrderBill = ref(false);
const orderBill = ref(null);
const currentBillPoints = ref(0);

const summaryLoading = ref(false);
const loyaltySummary = ref([]);
const summaryMode = ref("monthly");
const summaryRange = ref(6);

const formData = ref({
  fullName: "",
  email: "",
  phone: "",
  address: "",
  notes: "",
  birthDate: "",
});

// ── Computed ──────────────────────────────────────────────────────────────────
const loyaltyStats = computed(() => {
  const earned = loyaltyHistory.value
    .filter((h) => h.pointsDelta > 0)
    .reduce((s, h) => s + h.pointsDelta, 0);
  const deducted = loyaltyHistory.value
    .filter((h) => h.pointsDelta < 0)
    .reduce((s, h) => s + Math.abs(h.pointsDelta), 0);
  return {
    totalEarned: earned.toLocaleString(),
    totalDeducted: deducted.toLocaleString(),
    totalTransactions: loyaltyHistory.value.length,
  };
});

const summaryTotals = computed(() => {
  const earned = loyaltySummary.value.reduce(
    (s, r) => s + (r.totalPointsEarned || 0),
    0,
  );
  const deducted = loyaltySummary.value.reduce(
    (s, r) => s + (r.totalPointsDeducted || 0),
    0,
  );
  return { earned, deducted, net: earned - deducted };
});

const promoStats = computed(() => {
  const used = promotionHistory.value.filter(
    (p) => p.status === "Đã sử dụng",
  ).length;
  const totalSaved = promotionHistory.value
    .filter((p) => p.discountTotal)
    .reduce((s, p) => s + (p.discountTotal || 0), 0);
  return { total: promotionHistory.value.length, used, totalSaved };
});

const filteredPromoHistory = computed(() => {
  let result = [...promotionHistory.value];
  if (promoTypeFilter.value)
    result = result.filter((p) => p.type === promoTypeFilter.value);
  if (promoStatusFilter.value)
    result = result.filter((p) => p.status === promoStatusFilter.value);
  return result;
});

const paymentStats = computed(() => {
  const active = payments.value.filter((p) => !p.deletedAt);
  const success = active.filter((p) => p.status === "SUCCESS");
  const refunded = active.filter((p) => p.status === "REFUNDED");
  return {
    totalSuccess: success.length,
    totalAmount: success.reduce((s, p) => s + (p.amount || 0), 0),
    totalRefunded: refunded.length,
  };
});

const filteredLoyaltyHistory = computed(() => {
  let result = [...loyaltyHistory.value];
  if (historyTransactionFilter.value)
    result = result.filter(
      (h) => h.transactionType === historyTransactionFilter.value,
    );
  if (historyDateRange.value?.length === 2) {
    const [start, end] = historyDateRange.value;
    result = result.filter((h) => {
      const d = new Date(h.createdAt);
      return d >= new Date(start) && d <= new Date(end + "T23:59:59");
    });
  }
  return result;
});

const filteredPayments = computed(() => {
  let result = [...payments.value];
  if (!showDeletedPayments.value) result = result.filter((p) => !p.deletedAt);
  if (paymentStatusFilter.value)
    result = result.filter((p) => p.status === paymentStatusFilter.value);
  if (paymentMethodFilter.value)
    result = result.filter((p) => p.method === paymentMethodFilter.value);
  if (paymentDateRange.value?.length === 2) {
    const [start, end] = paymentDateRange.value;
    result = result.filter((p) => {
      const d = new Date(p.createdAt);
      return d >= new Date(start) && d <= new Date(end + "T23:59:59");
    });
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
    const labels = {
      EARN: "Cộng điểm",
      DEDUCT: "Trừ điểm",
      PENALTY: "Phạt",
      TIER_UPGRADE: "Thăng hạng",
      TIER_DOWNGRADE: "Hạ hạng",
    };
    filters.push({
      type: "transaction",
      label: labels[historyTransactionFilter.value],
      tagType: getTransactionTagType(historyTransactionFilter.value),
    });
  }
  if (historyDateRange.value) {
    const [s, e] = historyDateRange.value;
    filters.push({
      type: "date",
      label: `${formatDate(s)} - ${formatDate(e)}`,
      tagType: "info",
    });
  }
  return filters;
});

const activePaymentFilters = computed(() => {
  const filters = [];
  if (paymentStatusFilter.value)
    filters.push({
      type: "status",
      label: `Trạng thái: ${paymentStatusFilter.value}`,
      tagType: getPaymentStatusType(paymentStatusFilter.value),
    });
  if (paymentMethodFilter.value)
    filters.push({
      type: "method",
      label: `PT: ${getPaymentMethodLabel(paymentMethodFilter.value)}`,
      tagType: "info",
    });
  if (paymentDateRange.value) {
    const [s, e] = paymentDateRange.value;
    filters.push({
      type: "date",
      label: `${formatDate(s)} - ${formatDate(e)}`,
      tagType: "info",
    });
  }
  return filters;
});

const hasActiveHistoryFilters = computed(
  () => !!(historyTransactionFilter.value || historyDateRange.value),
);
const hasActivePaymentFilters = computed(
  () =>
    !!(
      paymentStatusFilter.value ||
      paymentMethodFilter.value ||
      paymentDateRange.value
    ),
);

// ── Helpers ───────────────────────────────────────────────────────────────────
const unwrap = (response) => {
  const d = response?.data;
  if (d && typeof d === "object" && "data" in d) return d.data;
  return d ?? response;
};

// ── Data loading ──────────────────────────────────────────────────────────────
const loadCustomerData = async () => {
  loading.value = true;
  try {
    const response = await customersApi.getProfile();
    const c = unwrap(response);
    if (!c) {
      ElMessage.error("Không tìm thấy thông tin khách hàng");
      return;
    }
    customer.value = c;
    formData.value = {
      fullName: c.name || "",
      email: c.email || "",
      phone: c.phone || "",
      address: c.address || "",
      notes: c.notes || "",
      birthDate: c.dateOfBirth || "",
    };
  } catch (error) {
    if (error.response?.status === 401) {
      ElMessage.warning("Vui lòng đăng nhập lại");
      router.push("/login");
    } else if (error.response?.status === 404)
      ElMessage.error("Không tìm thấy thông tin khách hàng.");
    else
      ElMessage.error(
        "Lỗi khi tải dữ liệu: " +
          (error.response?.data?.message || error.message),
      );
  } finally {
    loading.value = false;
  }
};

// ── Dashboard loader (MỚI) ────────────────────────────────────────────────────
const loadMyDashboard = async () => {
  dashboardLoading.value = true;
  try {
    myDashboard.value = unwrap(await customersApi.getMyDashboard());
  } catch (e) {
    ElMessage.error(
      "Lỗi khi tải dashboard: " + (e.response?.data?.message || e.message),
    );
  } finally {
    dashboardLoading.value = false;
  }
};

const loadLoyaltyHistory = async () => {
  if (!customer.value?.id) return;
  historyLoading.value = true;
  try {
    loyaltyHistory.value =
      unwrap(await customersApi.getLoyaltyHistory(customer.value.id)) || [];
  } catch {
    ElMessage.error("Lỗi khi tải lịch sử điểm");
  } finally {
    historyLoading.value = false;
  }
};

const loadTierHistory = async () => {
  if (!customer.value?.id) return;
  tierLoading.value = true;
  try {
    tierHistory.value =
      unwrap(await customersApi.getTierHistory(customer.value.id)) || [];
  } catch {
    ElMessage.error("Lỗi khi tải lịch sử thăng hạng");
  } finally {
    tierLoading.value = false;
  }
};

const loadPromotionHistory = async () => {
  if (!customer.value?.id) return;
  promoLoading.value = true;
  try {
    promotionHistory.value =
      unwrap(await customersApi.getPromotionHistory(customer.value.id)) || [];
  } catch {
    ElMessage.error("Lỗi khi tải lịch sử ưu đãi");
  } finally {
    promoLoading.value = false;
  }
};

const loadPayments = async () => {
  if (!customer.value?.id) return;
  paymentsLoading.value = true;
  try {
    payments.value =
      unwrap(
        await customersApi.getPayments(
          customer.value.id,
          showDeletedPayments.value,
        ),
      ) || [];
  } catch {
    ElMessage.error("Lỗi khi tải lịch sử giao dịch");
  } finally {
    paymentsLoading.value = false;
  }
};

const loadLoyaltySummary = async () => {
  if (!customer.value?.id) return;
  summaryLoading.value = true;
  try {
    const res =
      summaryMode.value === "weekly"
        ? await customersApi.getLoyaltyWeeklySummaryMe(summaryRange.value)
        : await customersApi.getLoyaltyMonthlySummaryMe(summaryRange.value);
    loyaltySummary.value = unwrap(res) || [];
  } catch {
    ElMessage.error("Lỗi khi tải tổng hợp điểm");
  } finally {
    summaryLoading.value = false;
  }
};

const getSummaryBarWidth = (value, type) => {
  if (!loyaltySummary.value.length) return 0;
  const maxVal = Math.max(
    ...loyaltySummary.value.map((r) =>
      type === "earn" ? r.totalPointsEarned : r.totalPointsDeducted,
    ),
  );
  return maxVal === 0 ? 0 : Math.round((value / maxVal) * 100);
};

const viewOrderBill = async (orderId, pointsDelta) => {
  showOrderBillDialog.value = true;
  loadingOrderBill.value = true;
  orderBill.value = null;
  currentBillPoints.value = pointsDelta;
  try {
    orderBill.value = unwrap(await ordersApi.getOrderDetail(orderId));
  } catch {
    ElMessage.error("Lỗi khi tải chi tiết đơn hàng");
    showOrderBillDialog.value = false;
  } finally {
    loadingOrderBill.value = false;
  }
};

// ── Filter helpers ────────────────────────────────────────────────────────────
const filterLoyaltyHistory = () => {};
const filterPromoHistory = () => {};
const filterPayments = () => {
  paymentCurrentPage.value = 1;
};
const removeHistoryFilter = (type) => {
  if (type === "transaction") historyTransactionFilter.value = "";
  if (type === "date") historyDateRange.value = null;
};
const removePaymentFilter = (type) => {
  if (type === "status") paymentStatusFilter.value = "";
  if (type === "method") paymentMethodFilter.value = "";
  if (type === "date") paymentDateRange.value = null;
  filterPayments();
};
const clearHistoryFilters = () => {
  historyTransactionFilter.value = "";
  historyDateRange.value = null;
};
const clearPaymentFilters = () => {
  paymentStatusFilter.value = "";
  paymentMethodFilter.value = "";
  paymentDateRange.value = null;
  paymentCurrentPage.value = 1;
};

// ── Payment actions ───────────────────────────────────────────────────────────
const viewPaymentDetail = async (payment) => {
  selectedPayment.value = payment;
  showPaymentDetailDialog.value = true;
  loadingPaymentDetail.value = true;
  try {
    selectedPayment.value =
      unwrap(await customersApi.getPaymentDetail(payment.id)) || payment;
  } catch {
    ElMessage.error("Lỗi khi tải chi tiết giao dịch");
  } finally {
    loadingPaymentDetail.value = false;
  }
};
const softDeletePayment = async (id) => {
  try {
    await customersApi.softDeletePayment(id);
    ElMessage.success("Đã xóa giao dịch");
    loadPayments();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "Lỗi khi xóa giao dịch");
  }
};
const restorePayment = async (id) => {
  try {
    await customersApi.restorePayment(id);
    ElMessage.success("Đã khôi phục giao dịch");
    loadPayments();
  } catch (error) {
    ElMessage.error(
      error.response?.data?.message || "Lỗi khi khôi phục giao dịch",
    );
  }
};

// ── Edit actions ──────────────────────────────────────────────────────────────
const startEdit = () => {
  editing.value = true;
};
const handleCancel = () => {
  editing.value = false;
  loadCustomerData();
};
const handleSave = async () => {
  saving.value = true;
  try {
    await customersApi.update(customer.value.id, formData.value);
    await loadCustomerData();
    editing.value = false;
    ElMessage.success("Cập nhật thành công!");
  } catch (error) {
    ElMessage.error(
      "Lỗi khi lưu dữ liệu: " +
        (error.response?.data?.message || error.message),
    );
  } finally {
    saving.value = false;
  }
};

// ── Display helpers ───────────────────────────────────────────────────────────
const getTierTagType = (tier) =>
  ({
    BRONZE: "warning",
    SILVER: "info",
    GOLD: "warning",
    PLATINUM: "primary",
    DIAMOND: "danger",
  })[tier] || "info";
const getTransactionTagType = (type) =>
  ({
    EARN: "success",
    DEDUCT: "warning",
    PENALTY: "danger",
    TIER_UPGRADE: "success",
    TIER_DOWNGRADE: "info",
  })[type] || "info";
const getTimelineType = (type) =>
  ({
    EARN: "success",
    DEDUCT: "warning",
    PENALTY: "danger",
    TIER_UPGRADE: "success",
    TIER_DOWNGRADE: "info",
  })[type] || "primary";
const getTransactionIcon = (type) =>
  ({
    EARN: "CirclePlus",
    DEDUCT: "Remove",
    PENALTY: "Warning",
    TIER_UPGRADE: "TopRight",
    TIER_DOWNGRADE: "BottomRight",
  })[type] || "More";
const getTransactionIconColor = (type) =>
  ({
    EARN: "#67c23a",
    DEDUCT: "#e6a23c",
    PENALTY: "#f56c6c",
    TIER_UPGRADE: "#67c23a",
    TIER_DOWNGRADE: "#909399",
  })[type] || "#409eff";
const getHistoryCardClass = (type) =>
  ({
    EARN: "history-earn",
    DEDUCT: "history-deduct",
    PENALTY: "history-penalty",
    TIER_UPGRADE: "history-upgrade",
    TIER_DOWNGRADE: "history-downgrade",
  })[type] || "";
const getPaymentStatusType = (status) =>
  ({
    SUCCESS: "success",
    PENDING: "warning",
    REFUNDED: "info",
    FAILED: "danger",
  })[status] || "info";
const getPaymentMethodLabel = (method) =>
  ({
    CASH: "Tiền mặt",
    BANK_TRANSFER: "Chuyển khoản",
    CREDIT_CARD: "Thẻ tín dụng",
    E_WALLET: "Ví điện tử",
  })[method] || method;
const getOrderStatusType = (status) =>
  ({
    PENDING: "warning",
    CONFIRMED: "primary",
    PROCESSING: "primary",
    SHIPPING: "primary",
    DELIVERED: "success",
    CANCELLED: "danger",
  })[status] || "info";
const getPromoStatusTagType = (status) =>
  ({
    "Đã sử dụng": "success",
    "Đang hoạt động": "primary",
    "Đã hết hạn": "info",
  })[status] || "info";
const getPromoCardClass = (item) => {
  if (item.status === "Đã hết hạn") return "promo-expired";
  if (item.type === "SPIN_WHEEL") return "promo-spin";
  return "promo-code";
};

const formatCurrency = (val) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    val || 0,
  );
const formatCurrencyShort = (val) => {
  if (val >= 1e9) return (val / 1e9).toFixed(1) + "B";
  if (val >= 1e6) return (val / 1e6).toFixed(1) + "M";
  if (val >= 1e3) return (val / 1e3).toFixed(1) + "K";
  return formatCurrency(val);
};
const formatDate = (d) => (d ? new Date(d).toLocaleDateString("vi-VN") : "—");
const formatDateTime = (d) =>
  d
    ? new Date(d).toLocaleString("vi-VN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      })
    : "—";
const getProgressPercent = () => {
  if (!customer.value) return 0;
  const total =
    (customer.value.loyaltyPoints || 0) +
    (customer.value.pointsToNextTier || 0);
  return total === 0
    ? 0
    : Math.min(100, Math.round((customer.value.loyaltyPoints / total) * 100));
};

onMounted(async () => {
  await loadCustomerData();
  // Load dashboard trước để tab đầu tiên hiển thị ngay
  loadMyDashboard();
  if (customer.value?.id) {
    loadLoyaltyHistory();
    loadTierHistory();
    loadPromotionHistory();
    loadPayments();
    loadLoyaltySummary();
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap");

/* ══════════════════════════════════════════════
   BASE & LAYOUT
══════════════════════════════════════════════ */
.profile-container {
  min-height: 100vh;
  padding: 24px 0 40px;
  font-family: "Inter", sans-serif;
}

.container-xl {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ── Profile header card ─────────────────────────────────────────────────── */
.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.35);
  flex-shrink: 0;
  letter-spacing: -0.5px;
}

/* ── Shared text helpers ────────────────────────────────────────────────── */
.points-display {
  font-size: 48px;
  font-weight: 900;
  color: #f59e0b;
  line-height: 1;
}
.muted {
  color: #94a3b8;
  font-size: 14px;
}
.total-spent {
  font-size: 32px;
  font-weight: 800;
  color: #9333ea;
}
.text-dark {
  color: #0f172a;
}

/* ── Info-tab cards ─────────────────────────────────────────────────────── */
.discount-card {
  background: linear-gradient(135deg, #16a34a, #15803d);
  color: #fff;
  border: none;
}
.discount-display {
  font-size: 48px;
  font-weight: 900;
  color: #fff;
  line-height: 1;
}
.discount-desc {
  color: rgba(255, 255, 255, 0.88);
  font-size: 14px;
  margin-top: 8px;
}

.form-static,
.form-static-info {
  padding: 9px 12px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  min-height: 40px;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #374151;
}
.notes-static {
  white-space: pre-wrap;
  word-wrap: break-word;
  min-height: 64px;
  align-items: flex-start;
  padding-top: 10px;
}

/* ══════════════════════════════════════════════
   KPI CARDS
══════════════════════════════════════════════ */
.kpi-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 16px;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  height: 100%;
}
.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
}

.kpi-icon {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
}
.kpi-body {
  flex: 1;
  min-width: 0;
}
.kpi-value {
  font-size: 26px;
  font-weight: 900;
  line-height: 1.1;
}
.kpi-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 700;
  margin-top: 4px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.kpi-points {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border: 1px solid #fde68a;
}
.kpi-points .kpi-icon {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
}
.kpi-points .kpi-value {
  color: #d97706;
}

.kpi-spent {
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #ddd6fe;
}
.kpi-spent .kpi-icon {
  background: rgba(147, 51, 234, 0.12);
  color: #9333ea;
}
.kpi-spent .kpi-value {
  color: #7c3aed;
}

.kpi-orders {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border: 1px solid #bfdbfe;
}
.kpi-orders .kpi-icon {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}
.kpi-orders .kpi-value {
  color: #2563eb;
}

.kpi-discount {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border: 1px solid #bbf7d0;
}
.kpi-discount .kpi-icon {
  background: rgba(22, 163, 74, 0.12);
  color: #16a34a;
}
.kpi-discount .kpi-value {
  color: #16a34a;
}

/* ══════════════════════════════════════════════
   VIP JOURNEY
══════════════════════════════════════════════ */
.vip-journey-card {
  border-radius: 16px;
}

.vip-hero {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #fffbeb, #fef9ec);
  border: 1px solid #fde68a;
  border-radius: 14px;
  flex-wrap: wrap;
}
.vip-hero-tier {
  flex-shrink: 0;
}
.vip-hero-points {
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex: 1;
}
.vip-tier-big {
  font-size: 14px !important;
  padding: 8px 20px !important;
  font-weight: 700 !important;
}
.vip-pts-current {
  font-size: 36px;
  font-weight: 900;
  color: #d97706;
}
.vip-pts-label {
  font-size: 15px;
  color: #92400e;
  font-weight: 600;
}

.vip-progress-wrap {
  margin-bottom: 20px;
}
.vip-progress-track {
  height: 10px;
  background: #e2e8f0;
  border-radius: 5px;
  overflow: hidden;
}
.vip-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
  border-radius: 5px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.vip-max-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border: 1px solid #fde68a;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #d97706;
}

.vip-milestones {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.vip-milestone {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  transition:
    background 0.15s,
    border-color 0.15s;
}
.vip-milestone:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
}
.vip-milestone.achieved {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.milestone-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.dot-achieved {
  background: #16a34a;
}
.dot-pending {
  background: #e2e8f0;
  border: 2px solid #cbd5e1;
}

.milestone-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.milestone-tier {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}
.milestone-pts {
  font-size: 11px;
  color: #94a3b8;
}
.milestone-date {
  font-size: 11px;
  color: #16a34a;
  font-weight: 700;
  white-space: nowrap;
}

/* ══════════════════════════════════════════════
   SPIN DASH CARD
══════════════════════════════════════════════ */
.spin-dash-card {
  border-radius: 16px;
}

.spin-bonus-banner {
  display: flex;
  align-items: center;
  gap: 14px;
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border: 1px solid #6ee7b7;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}
.spin-bonus-icon {
  font-size: 30px;
  flex-shrink: 0;
}
.spin-bonus-body {
  display: flex;
  flex-direction: column;
}
.spin-bonus-value {
  font-size: 24px;
  font-weight: 900;
  color: #16a34a;
  line-height: 1.1;
}
.spin-bonus-label {
  font-size: 11px;
  color: #4ade80;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-top: 2px;
}

.spin-status-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 12px;
}
.spin-status-item {
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  padding: 12px 14px;
  text-align: center;
}
.spin-status-val {
  font-size: 14px;
  font-weight: 800;
}
.spin-status-key {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 3px;
  font-weight: 500;
}

.spin-next-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #94a3b8;
  padding: 6px 0;
}
.spin-empty-note {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #cbd5e1;
  padding: 8px 0;
}

/* Avg order & last order */
.avg-order-section,
.last-order-section {
  padding: 14px 16px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
}
.avg-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}
.avg-value {
  font-size: 22px;
  font-weight: 800;
  color: #3b82f6;
}
.last-order-date {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

/* ══════════════════════════════════════════════
   RECENT LISTS
══════════════════════════════════════════════ */
.recent-list {
  display: flex;
  flex-direction: column;
}
.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.1s;
}
.recent-item:last-child {
  border-bottom: none;
}

.recent-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}
.recent-item-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.payment-icon {
  background: rgba(59, 130, 246, 0.1);
}
.earn-icon {
  background: rgba(22, 163, 74, 0.1);
}
.deduct-icon {
  background: rgba(245, 158, 11, 0.1);
}

.recent-item-info {
  min-width: 0;
}
.recent-item-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recent-item-sub {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}
.recent-item-right {
  text-align: right;
  flex-shrink: 0;
  padding-left: 12px;
}
.recent-item-amount {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.recent-pts {
  font-size: 17px;
  font-weight: 900;
  line-height: 1;
}
.pts-earn {
  color: #16a34a;
}
.pts-deduct {
  color: #f59e0b;
}
.recent-pts-label {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 2px;
}

/* ══════════════════════════════════════════════
   PROMO DASH LIST
══════════════════════════════════════════════ */
.promo-dash-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.promo-dash-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
  transition:
    background 0.15s,
    border-color 0.15s,
    transform 0.15s;
}
.promo-dash-item:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
  transform: translateX(2px);
}
.promo-dash-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.promo-dash-info {
  flex: 1;
  min-width: 0;
}
.promo-dash-name {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.promo-dash-meta {
  display: flex;
  gap: 6px;
  margin-top: 5px;
  flex-wrap: wrap;
}
.promo-dash-value {
  text-align: right;
  flex-shrink: 0;
}
.promo-dash-discount {
  font-size: 18px;
  font-weight: 900;
  color: #3b82f6;
  display: block;
  line-height: 1.1;
}
.promo-dash-unit {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 1px;
}

/* ══════════════════════════════════════════════
   STATS CARDS
══════════════════════════════════════════════ */
.stats-card {
  border-radius: 14px;
  transition:
    transform 0.25s,
    box-shadow 0.25s;
}
.stats-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.09);
}
.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stats-icon {
  flex-shrink: 0;
}
.stats-value {
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
  margin-bottom: 4px;
}
.stats-value-small {
  font-size: 22px;
}
.stats-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.earn-card {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
}
.earn-card .stats-value {
  color: #16a34a;
}

.deduct-card {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
}
.deduct-card .stats-value {
  color: #f59e0b;
}

.transactions-card {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
}
.transactions-card .stats-value {
  color: #3b82f6;
}

.payment-success-card {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
}
.payment-success-card .stats-value {
  color: #16a34a;
}

.payment-amount-card {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
}
.payment-amount-card .stats-value {
  color: #3b82f6;
}

.payment-refunded-card {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
}
.payment-refunded-card .stats-value {
  color: #f59e0b;
}

.promo-total-card {
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
}
.promo-total-card .stats-value {
  color: #9333ea;
}

.promo-used-card {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
}
.promo-used-card .stats-value {
  color: #16a34a;
}

.promo-saved-card {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
}
.promo-saved-card .stats-value {
  color: #3b82f6;
}

/* ══════════════════════════════════════════════
   PROMO HISTORY
══════════════════════════════════════════════ */
.promo-history-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.promo-history-card {
  border-radius: 12px;
  transition:
    transform 0.25s,
    box-shadow 0.25s;
}
.promo-history-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}
.promo-history-card.promo-code {
  border-left: 4px solid #3b82f6;
}
.promo-history-card.promo-spin {
  border-left: 4px solid #16a34a;
}
.promo-history-card.promo-expired {
  border-left: 4px solid #cbd5e1;
  opacity: 0.72;
}

.promo-card-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.promo-icon-wrapper {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}
.code-icon {
  background: linear-gradient(135deg, #eff6ff, #bfdbfe);
  color: #3b82f6;
}
.spin-icon {
  background: linear-gradient(135deg, #f0fdf4, #bbf7d0);
  color: #16a34a;
}

.promo-details {
  flex: 1;
  min-width: 0;
}
.promo-name {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}
.promo-meta {
  font-size: 13px;
  color: #374151;
}
.promo-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.promo-discount-badge {
  flex-shrink: 0;
  text-align: center;
  min-width: 80px;
}
.discount-big {
  font-size: 26px;
  font-weight: 900;
  line-height: 1;
}
.code-color {
  color: #3b82f6;
}
.spin-color {
  color: #16a34a;
}
.discount-label {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}

/* ══════════════════════════════════════════════
   HISTORY TIMELINE
══════════════════════════════════════════════ */
.history-card {
  transition:
    transform 0.25s,
    box-shadow 0.25s;
  border-radius: 12px;
}
.history-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.history-card-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.history-icon-wrapper {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.1);
}
.history-earn .history-icon-wrapper {
  background: rgba(22, 163, 74, 0.1);
}
.history-deduct .history-icon-wrapper {
  background: rgba(245, 158, 11, 0.1);
}
.history-penalty .history-icon-wrapper {
  background: rgba(239, 68, 68, 0.1);
}
.history-upgrade .history-icon-wrapper {
  background: rgba(22, 163, 74, 0.1);
}
.history-downgrade .history-icon-wrapper {
  background: rgba(100, 116, 139, 0.1);
}

.history-details {
  flex: 1;
  min-width: 0;
}
.history-note {
  color: #374151;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}
.tier-change-indicator {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
.history-points {
  flex-shrink: 0;
}

.points-badge {
  padding: 10px 16px;
  border-radius: 10px;
  text-align: center;
  min-width: 108px;
}
.points-positive {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border: 2px solid #16a34a;
}
.points-negative {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border: 2px solid #f59e0b;
}
.points-sign {
  font-size: 14px;
  font-weight: 600;
}
.points-value {
  font-size: 24px;
  font-weight: 900;
  margin: 0 4px;
}
.points-positive .points-value {
  color: #16a34a;
}
.points-negative .points-value {
  color: #f59e0b;
}
.points-unit {
  font-size: 12px;
  color: #94a3b8;
}

.order-bill-btn {
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s;
}
.order-bill-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.28);
}

/* ══════════════════════════════════════════════
   TIER HISTORY
══════════════════════════════════════════════ */
.tier-history-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.tier-history-card {
  transition:
    transform 0.25s,
    box-shadow 0.25s;
  border-radius: 12px;
}
.tier-history-card.upgrade {
  border-left: 4px solid #16a34a;
}
.tier-history-card.downgrade {
  border-left: 4px solid #ef4444;
}
.tier-history-card:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.tier-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: rgba(22, 163, 74, 0.1);
  flex-shrink: 0;
}
.tier-history-card.downgrade .tier-icon {
  background: rgba(239, 68, 68, 0.1);
}

.tier-change-display {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

/* ══════════════════════════════════════════════
   PAYMENT DETAIL
══════════════════════════════════════════════ */
.payment-detail {
  padding: 10px 0;
}
.payment-totals {
  background: #f1f5f9;
  padding: 18px 20px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

/* ══════════════════════════════════════════════
   ORDER BILL DIALOG
══════════════════════════════════════════════ */
.order-bill-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
}
.order-bill-dialog :deep(.el-dialog__header) {
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
  padding: 18px 24px 14px;
  margin-right: 0;
}
.order-bill-dialog :deep(.el-dialog__body) {
  padding: 0;
}
.order-bill-dialog :deep(.el-dialog__footer) {
  background: #f8fafc;
  border-top: 1px solid #e5e7eb;
  padding: 14px 24px;
}

.bill-body {
  padding: 20px 24px;
}

.bill-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.bill-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.bill-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.points-earned-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border: 1px solid #fcd34d;
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 18px;
  font-size: 14px;
  color: #92400e;
  flex-wrap: wrap;
}
.points-earned-value {
  font-size: 18px;
  font-weight: 900;
  color: #d97706;
}
.points-rate {
  font-size: 12px;
  color: #b45309;
  opacity: 0.8;
}

.bill-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  background: #f8fafc;
  border-radius: 12px;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  margin-bottom: 16px;
}
.bill-meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.bill-meta-label {
  font-size: 10.5px;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.bill-meta-value {
  font-size: 14px;
  color: #0f172a;
  font-weight: 500;
}

.bill-items {
  font-size: 13px;
}
.bill-items-header {
  display: grid;
  grid-template-columns: 1fr 50px 120px 110px 120px;
  gap: 8px;
  padding: 10px 12px;
  background: #f1f5f9;
  border-radius: 8px;
  font-weight: 700;
  color: #64748b;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}
.bill-item-row {
  display: grid;
  grid-template-columns: 1fr 50px 120px 110px 120px;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid #f1f5f9;
  align-items: center;
  transition: background 0.12s;
}
.bill-item-row:hover {
  background: #f8fafc;
}
.bill-item-row:last-child {
  border-bottom: none;
}

.item-product-name {
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-variant-name {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 3px;
}
.item-sku {
  font-size: 11px;
  color: #cbd5e1;
  margin-top: 2px;
}

.qty-badge {
  display: inline-block;
  background: #eff6ff;
  color: #3b82f6;
  border-radius: 6px;
  padding: 3px 10px;
  font-weight: 700;
  font-size: 13px;
}

.bill-totals {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px 18px;
  border: 1px solid #e5e7eb;
}
.bill-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
  color: #374151;
}
.bill-grand-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 18px;
  font-weight: 900;
  color: #0f172a;
  border-top: 2px solid #e5e7eb;
  margin-top: 10px;
  padding-top: 12px;
}
.bill-grand-total span:last-child {
  color: #3b82f6;
}

.bill-points-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0 0;
  font-size: 13px;
  color: #92400e;
  border-top: 1px dashed #fcd34d;
  margin-top: 10px;
}
.points-earned-inline {
  font-size: 15px;
  font-weight: 900;
  color: #d97706;
}

.bill-notes {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 10px;
  padding: 12px 16px;
}
.bill-notes-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #0369a1;
  margin-bottom: 6px;
}
.bill-notes-content {
  font-size: 13px;
  color: #0c4a6e;
  line-height: 1.6;
}

/* ── SIMPLE NEUTRAL UI OVERRIDES ─────────────────────────────────────────── */
.profile-container .profile-avatar {
  background: #e2e8f0 !important;
  color: #1f2937 !important;
  box-shadow: none !important;
}
.profile-container .kpi-card,
.profile-container .vip-journey-card,
.profile-container .spin-dash-card,
.profile-container .stats-card,
.profile-container .recent-item,
.profile-container .promo-dash-item,
.profile-container .history-card,
.profile-container .tier-history-card,
.profile-container .promo-history-card,
.profile-container .bill-meta,
.profile-container .bill-items-header,
.profile-container .bill-item-row,
.profile-container .bill-totals,
.profile-container .bill-notes {
  background: #ffffff !important;
  border: 1px solid #d1d5db !important;
  box-shadow: none !important;
}
.profile-container .discount-card {
  background: #f8fafc !important;
  color: #0f172a !important;
}
.profile-container .vip-hero,
.profile-container .spin-bonus-banner,
.profile-container .spin-status-item,
.profile-container .avg-order-section,
.profile-container .last-order-section,
.profile-container .promo-dash-item {
  background: #f8fafc !important;
  border-color: #e2e8f0 !important;
}
.profile-container .kpi-icon,
.profile-container .recent-item-icon,
.profile-container .promo-dash-icon,
.profile-container .stats-icon,
.profile-container .history-icon-wrapper,
.profile-container .tier-icon,
.profile-container .spin-bonus-icon,
.profile-container .vip-max-badge el-icon,
.profile-container .spin-next-time el-icon,
.profile-container .spin-empty-note el-icon,
.profile-container h3 .el-icon,
.profile-container h4 .el-icon,
.profile-container .d-flex.align-items-center.gap-2 .el-icon,
.profile-container .order-bill-btn el-icon,
.profile-container .bill-notes-label el-icon {
  display: none !important;
}
.profile-container .kpi-value,
.profile-container .stats-value,
.profile-container .points-display,
.profile-container .promo-dash-discount,
.profile-container .promo-dash-name,
.profile-container .recent-item-title {
  color: #111827 !important;
}
.profile-container .kpi-label,
.profile-container .avg-label,
.profile-container .spin-status-key,
.profile-container .recent-item-sub,
.profile-container .promo-dash-unit,
.profile-container .muted {
  color: #6b7280 !important;
}
.profile-container .kpi-card:hover,
.profile-container .stats-card:hover,
.profile-container .promo-dash-item:hover,
.profile-container .history-card:hover,
.profile-container .tier-history-card:hover,
.profile-container .promo-history-card:hover {
  transform: none !important;
  box-shadow: none !important;
}
.profile-container :deep(.el-tag) {
  background: #f1f5f9 !important;
  color: #334155 !important;
  border: none !important;
}
.profile-container :deep(.el-button) {
  border-radius: 6px !important;
}
.profile-container :deep(.el-button--primary) {
  background-color: #2563eb !important;
  border-color: #2563eb !important;
}

/* ══════════════════════════════════════════════
   SUMMARY BARS
══════════════════════════════════════════════ */
.summary-bar-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  padding: 2px 0;
}
.summary-bar-earn {
  height: 6px;
  background: linear-gradient(90deg, #16a34a, #4ade80);
  border-radius: 3px;
  min-width: 3px;
  transition: width 0.4s ease;
}
.summary-bar-deduct {
  height: 6px;
  background: linear-gradient(90deg, #f59e0b, #fcd34d);
  border-radius: 3px;
  min-width: 3px;
  transition: width 0.4s ease;
}

/* ══════════════════════════════════════════════
   ELEMENT PLUS OVERRIDES (scoped :deep)
══════════════════════════════════════════════ */
/* Card padding tighter cho info section */
.profile-container :deep(.el-card__body) {
  padding: 20px;
}

/* Tab label font */
.profile-container :deep(.el-tabs__item) {
  font-family: "Inter", sans-serif;
  font-weight: 600;
  font-size: 14px;
}

/* Timeline */
.profile-container :deep(.el-timeline-item__timestamp) {
  font-size: 11.5px;
  color: #94a3b8;
  font-weight: 500;
}

/* Table header */
.profile-container :deep(.el-table th) {
  background: #f8fafc;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

/* Divider tighter */
.profile-container :deep(.el-divider--horizontal) {
  margin: 16px 0;
}

/* ══════════════════════════════════════════════
   RESPONSIVE
══════════════════════════════════════════════ */
@media (max-width: 768px) {
  .container-xl {
    padding: 0 16px;
  }
  .profile-container {
    padding: 16px 0 32px;
  }

  .profile-avatar {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }

  .kpi-card {
    padding: 14px 12px;
  }
  .kpi-value {
    font-size: 20px;
  }
  .kpi-icon {
    width: 42px;
    height: 42px;
  }
  .kpi-label {
    font-size: 10px;
  }

  .vip-hero {
    gap: 12px;
    padding: 12px;
  }
  .vip-pts-current {
    font-size: 28px;
  }
  .vip-milestone {
    flex-wrap: wrap;
  }

  .spin-status-grid {
    grid-template-columns: 1fr 1fr;
  }

  .stats-content {
    flex-direction: column;
    text-align: center;
  }

  .history-card-content {
    flex-direction: column;
  }
  .history-points {
    width: 100%;
  }
  .points-badge {
    width: 100%;
    box-sizing: border-box;
  }

  .promo-card-content {
    flex-direction: column;
  }
  .promo-discount-badge {
    width: 100%;
    text-align: left;
  }

  .bill-items-header,
  .bill-item-row {
    grid-template-columns: 1fr 40px 110px;
  }
  .bill-meta {
    grid-template-columns: 1fr;
  }
  .bill-body {
    padding: 16px;
  }

  .tier-change-display {
    flex-wrap: wrap;
  }

  .promo-history-card:hover,
  .history-card:hover,
  .tier-history-card:hover {
    transform: none;
  }
}

@media (max-width: 480px) {
  .kpi-card {
    padding: 12px 10px;
    gap: 10px;
  }
  .kpi-icon {
    display: none;
  }
  .kpi-value {
    font-size: 18px;
  }
}
</style>