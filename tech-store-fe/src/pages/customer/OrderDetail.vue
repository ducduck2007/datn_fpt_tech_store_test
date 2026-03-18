<template>
  <div class="glass-page">
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <div class="container-xl order-layout">
      <!-- LEFT COLUMN -->
      <div class="order-left">
        <div class="glass-card main-card">
          <!-- HEADER -->
          <div class="order-header">
            <div class="order-identity">
              <div class="kicker">
                <span class="kicker-dot"></span>
                Order
              </div>
              <div class="title">
                {{ detail?.orderNumber || `#${orderId}` }}
              </div>
              <div class="badge-row mt-2">
                <span :class="['status-pill', `status-${(detail?.status || '').toLowerCase()}`]">
                  <span class="pill-glow"></span>
                  {{ detail?.status }}
                </span>
                <span v-if="isReturned(detail?.status)" class="status-pill status-returned">
                  <span class="pill-glow"></span>
                  Returned
                </span>
              </div>


            </div>

            <div class="order-actions">
              <button class="glass-btn ghost" @click="reload" :class="{ loading }">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="23 4 23 10 17 10" />
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                </svg>
                Reload
              </button>

              <button
                v-if="detail?.status === 'PENDING' && detail?.paymentStatus === 'UNPAID' && detail?.paymentMethod !== 'CASH'"
                class="glass-btn primary"
                @click="openPaymentDialog"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <rect x="1" y="4" width="22" height="16" rx="2" />
                  <line x1="1" y1="10" x2="23" y2="10" />
                </svg>
                Thanh toán
              </button>

              <button
                v-if="detail?.status === 'PENDING' || detail?.status === 'PAID'"
                class="glass-btn danger"
                @click="showCancelDialog = true"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
                Hủy đơn
              </button>

              <button
                v-if="detail?.status === 'SHIPPING'"
                class="glass-btn primary"
                :class="{ loading: deliveredLoading }"
                @click="confirmDelivered"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                Đã nhận hàng
              </button>

              <button
                v-if="detail?.status === 'DELIVERED' && !isReturned(detail?.status)"
                class="glass-btn warning"
                :class="{ 'btn-disabled': !isReturnWindowOpen }"
                :disabled="!isReturnWindowOpen"
                :title="!isReturnWindowOpen ? 'Đã quá 24h kể từ khi nhận hàng, không thể yêu cầu trả hàng' : 'Yêu cầu trả hàng'"
                @click="isReturnWindowOpen && (showReturnDialog = true)"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="1 4 1 10 7 10" />
                  <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
                </svg>
                Yêu cầu trả hàng
              </button>
            </div>
          </div>

          <!-- ── TIMELINE V2 ────────────────────────────────────────── -->
          <div v-if="detail" class="timeline-track">
            <div class="timeline-progress-bg">
              <div class="timeline-progress-fill" :style="`width: ${timelineProgressPercent}%`"></div>
            </div>
            <div
              v-for="(step, i) in timelineSteps"
              :key="step.key"
              class="tl-step"
              :class="{
                'tl-done': step.active && !step.current,
                'tl-current': step.current,
                'tl-pending': !step.active && !step.current,
              }"
            >
              <div class="tl-node">
                <div class="tl-node-ring" v-if="step.current"></div>
                <div class="tl-node-inner">
                  <svg v-if="step.active && !step.current" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                  <span v-else-if="step.current" class="tl-pulse-dot"></span>
                  <span v-else class="tl-step-num">{{ i + 1 }}</span>
                </div>
              </div>
              <div class="tl-label-wrap">
                <span class="tl-label">{{ step.label }}</span>
                <span v-if="step.current" class="tl-current-badge">Hiện tại</span>
              </div>
            </div>
          </div>

          <!-- SKELETON -->
          <div v-if="loading" class="skeleton-wrap">
            <div v-for="n in 4" :key="n" class="skeleton-line" :style="`width:${70 + Math.random() * 25}%`"></div>
          </div>

          <template v-else-if="detail">
            <!-- INFO GRID -->
            <div class="info-grid">
              <!-- Customer -->
              <div class="glass-info-card">
                <div class="info-card-header">
                  <div class="info-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                      <circle cx="12" cy="7" r="4" />
                    </svg>
                  </div>
                  <span>Thông tin khách hàng</span>
                </div>
                <div class="info-card-body">
                  <div class="info-row">
                    <span class="info-label">Tên</span>
                    <span class="info-value">{{ detail.customerName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">ID</span>
                    <span class="info-value">{{ detail.customerId }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Email</span>
                    <span class="info-value">{{ detail.customerEmail }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Phone</span>
                    <span class="info-value">{{ detail.customerPhone }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Address</span>
                    <span class="info-value">{{ detail.customerAddress }}</span>
                  </div>
                </div>
              </div>

              <!-- Payment & Delivery -->
              <div class="glass-info-card">
                <div class="info-card-header">
                  <div class="info-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="1" y="4" width="22" height="16" rx="2" />
                      <line x1="1" y1="10" x2="23" y2="10" />
                    </svg>
                  </div>
                  <span>Thanh toán & Giao hàng</span>
                </div>
                <div class="info-card-body">
                  <div class="info-row">
                    <span class="info-label">Phương thức</span>
                    <span class="info-value">
                      <span class="method-tag">{{ detail.paymentMethod }}</span>
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Kênh</span>
                    <span class="info-value">{{ detail.channel }}</span>
                  </div>
                  <!-- Hình thức giao hàng (đọc từ notes) -->
                  <div class="info-row">
                    <span class="info-label">Giao hàng</span>
                    <span class="info-value">
                      <span :class="['delivery-tag', isHomeDelivery ? 'delivery-home' : 'delivery-store']">
                        {{ isHomeDelivery ? '🚚 Giao tại nhà' : '🏬 Nhận tại cửa hàng' }}
                      </span>
                    </span>
                  </div>

                  <!-- ── NOTES V2 ────────────────────────────────── -->
                  <div v-if="detail.notes" class="notes-v2">
                    <div class="notes-v2-header">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                        <polyline points="14 2 14 8 20 8"/>
                      </svg>
                      <span>Ghi chú đơn hàng</span>
                      <span class="notes-v2-count">{{ parseNotes(detail.notes).length }}</span>
                    </div>
                    <div class="notes-v2-list">
                      <div
                        v-for="(line, i) in parseNotes(detail.notes)"
                        :key="i"
                        class="notes-v2-item"
                        :class="`notes-v2-${line.type}`"
                        :style="`animation-delay: ${i * 60}ms`"
                      >
                        <div class="notes-v2-left">
                          <div class="notes-v2-icon-wrap">
                            <span class="notes-v2-icon">{{ line.icon }}</span>
                          </div>
                          <div class="notes-v2-accent-bar"></div>
                        </div>
                        <div class="notes-v2-body">
                          <span class="notes-v2-type-tag">{{ getNoteTypeLabel(line.type) }}</span>
                          <p class="notes-v2-text">{{ line.text }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- ── END NOTES V2 ─────────────────────────────── -->
                </div>
              </div>
            </div>

            <!-- ITEMS TABLE -->
            <div class="items-section">
              <div class="section-header">
                <h5>Chi tiết sản phẩm</h5>
                <span class="item-count">{{ detail.items?.length }} sản phẩm</span>
              </div>
              <div class="items-card-list">
                <div
                  v-for="(row, index) in detail.items"
                  :key="row.sku"
                  class="item-card"
                  :style="`--i: ${index}`"
                >
                  <div class="item-card-left">
                    <div class="item-index">{{ index + 1 }}</div>
                    <div class="item-info">
                      <div class="item-name">{{ row.productName }}</div>
                      <div class="item-variant">{{ row.variantName }}</div>
                      <div class="item-sku">{{ row.sku }}</div>
                    </div>
                  </div>
                  <div class="item-card-right">
                    <div class="item-stat">
                      <span class="stat-label">Số lượng</span>
                      <span class="qty-badge">× {{ row.quantity }}</span>
                    </div>
                    <div class="item-stat">
                      <span class="stat-label">Đơn giá</span>
                      <span class="stat-value">{{ formatMoney(row.price) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- RIGHT COLUMN -->
      <div class="order-right" v-if="detail">
        <div class="glass-card totals-card">
          <div class="totals-header">Tóm tắt đơn hàng</div>

          <div class="totals-list">
            <div class="totals-row">
              <span>Tạm tính</span>
              <span>{{ formatMoney(detail.subtotal) }}</span>
            </div>
            <div v-if="detail.vipDiscount > 0" class="totals-row discount-row">
              <span>👑 Giảm VIP</span>
              <span>-{{ formatMoney(detail.vipDiscount) }}</span>
            </div>
            <div v-if="detail.discountTotal - (detail.vipDiscount || 0) > 0" class="totals-row discount-row">
              <span>🏷️ Mã giảm giá</span>
              <span>-{{ formatMoney(detail.discountTotal - (detail.vipDiscount || 0)) }}</span>
            </div>

            <!-- Phí ship: chỉ hiện khi giao tại nhà -->
            <div v-if="isHomeDelivery" class="totals-row shipping-row">
              <span>🚚 Phí ship</span>
              <span>{{ formatMoney(detail.shippingFee > 0 ? detail.shippingFee : 15000) }}</span>
            </div>
            <!-- Khi nhận tại cửa hàng: hiện badge miễn phí -->
            <div v-else class="totals-row free-row">
              <span>🏬 Nhận tại cửa hàng</span>
              <span class="free-badge">Miễn phí</span>
            </div>
          </div>

          <div class="totals-divider"></div>

          <div class="totals-final">
            <span>Tổng cộng</span>
            <span class="final-amount">{{ formatMoney(detail.totalAmount) }}</span>
          </div>
        </div>

        <!-- ── TIMESTAMPS CARD ──────────────────────────────────── -->
        <div class="glass-card timestamps-card">
          <div class="timestamps-card-header">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Thời gian đơn hàng
          </div>

          <!-- Tạo đơn -->
          <div class="ts-card-row">
            <div class="ts-card-icon ts-card-icon--create">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
            </div>
            <div class="ts-card-info">
              <span class="ts-card-label">Tạo đơn</span>
              <span class="ts-card-value">{{ formatDateTime(detail.createdAt) }}</span>
            </div>
          </div>

          <!-- Thanh toán — hiện khi có paidAt -->
          <template v-if="detail.paidAt">
            <div class="ts-card-connector"></div>
            <div class="ts-card-row ts-card-row--active">
              <div class="ts-card-icon ts-card-icon--paid">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <rect x="1" y="4" width="22" height="16" rx="2"/>
                  <line x1="1" y1="10" x2="23" y2="10"/>
                </svg>
              </div>
              <div class="ts-card-info">
                <span class="ts-card-label">Thanh toán</span>
                <span class="ts-card-value ts-card-value--paid">{{ formatDateTime(detail.paidAt) }}</span>
              </div>
            </div>
          </template>

          <!-- Đã giao — hiện khi không bị hủy -->
          <template v-if="!detail.cancelledAt">
            <div class="ts-card-connector"></div>
            <div class="ts-card-row" :class="{ 'ts-card-row--active': detail.deliveredAt }">
              <div class="ts-card-icon" :class="detail.deliveredAt ? 'ts-card-icon--delivered' : 'ts-card-icon--pending'">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M5 12l5 5L20 7"/>
                </svg>
              </div>
              <div class="ts-card-info">
                <span class="ts-card-label">Đã giao hàng</span>
                <span class="ts-card-value" :class="{ 'ts-card-value--muted': !detail.deliveredAt }">
                  {{ detail.deliveredAt ? formatDateTime(detail.deliveredAt) : 'Chưa giao' }}
                </span>
                <span v-if="detail.deliveredAt && isReturnWindowOpen" class="ts-card-badge ts-card-badge--warn">
                  <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                  </svg>
                  Còn {{ returnTimeLeft }} để trả hàng
                </span>
                <span v-if="detail.deliveredAt && !isReturnWindowOpen && detail.status === 'DELIVERED'" class="ts-card-badge ts-card-badge--expired">
                  <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                  Hết hạn trả hàng
                </span>
              </div>
            </div>
          </template>

          <!-- Đã hủy — chỉ hiện khi có cancelledAt -->
          <template v-if="detail.cancelledAt">
            <div class="ts-card-connector ts-card-connector--cancelled"></div>
            <div class="ts-card-row ts-card-row--cancelled">
              <div class="ts-card-icon ts-card-icon--cancelled">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </div>
              <div class="ts-card-info">
                <span class="ts-card-label">Đã hủy đơn</span>
                <span class="ts-card-value ts-card-value--cancelled">{{ formatDateTime(detail.cancelledAt) }}</span>
              </div>
            </div>
          </template>
        </div>
        <!-- ── END TIMESTAMPS CARD ──────────────────────────────── -->
      </div>
    </div>

    <!-- ================================================================ -->
    <!-- PAYMENT DIALOG                                                    -->
    <!-- ================================================================ -->
    <transition name="modal-fade">
      <div v-if="showPaymentDialog" class="modal-backdrop" @click.self="showPaymentDialog = false">
        <div class="glass-modal">
          <div class="modal-header">
            <div class="modal-title">
              <span class="modal-icon">💳</span>
              Thanh toán đơn hàng
            </div>
            <button class="modal-close" @click="showPaymentDialog = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div v-if="spinStatus.loading" class="spin-loading">
              <div class="spin-ring"></div>
              <span>Đang kiểm tra ưu đãi vòng quay...</span>
            </div>
            <transition name="slide-down">
              <div v-if="!spinStatus.loading && spinStatus.hasActiveBonus" class="spin-banner">
                <div class="spin-banner-top">
                  <div class="spin-badge-icon">🎡</div>
                  <div>
                    <div class="spin-banner-title">Ưu đãi vòng quay đã áp dụng!</div>
                    <div class="spin-banner-sub">
                      Giảm thêm <strong>{{ spinStatus.bonusRate }}%</strong>
                      <span v-if="spinStatus.bonusExpiresAt"> · Hết hạn {{ formatExpiry(spinStatus.bonusExpiresAt) }}</span>
                    </div>
                  </div>
                  <div class="spin-rate-badge">-{{ spinStatus.bonusRate }}%</div>
                </div>
                <div class="spin-breakdown">
                  <div class="breakdown-row">
                    <span>Tạm tính</span><span>{{ formatMoney(detail?.subtotal) }}</span>
                  </div>
                  <div v-if="detail?.vipDiscount > 0" class="breakdown-row positive">
                    <span>🏆 Giảm VIP</span><span>-{{ formatMoney(detail.vipDiscount) }}</span>
                  </div>
                  <div class="breakdown-row positive">
                    <span>🎡 Giảm Spin ({{ spinStatus.bonusRate }}%)</span>
                    <span>-{{ formatMoney(estimatedSpinDiscount) }}</span>
                  </div>
                  <div class="breakdown-row total-row">
                    <span><strong>Tổng thanh toán</strong></span>
                    <span><strong>{{ formatMoney((detail?.totalAmount || 0) - estimatedSpinDiscount + (detail?.spinDiscount || 0)) }}</strong></span>
                  </div>
                </div>
              </div>
            </transition>
            <div v-if="!spinStatus.loading && !spinStatus.hasActiveBonus && spinStatus.checked" class="no-spin">
              <span>🎡</span> Khách hàng chưa có ưu đãi vòng quay tuần này
            </div>
            <div class="info-alert">
              <div class="info-alert-title">Sau khi thanh toán thành công</div>
              <div class="info-alert-rows">
                <div class="alert-item">✅ Đơn hàng chuyển sang <strong>PAID</strong></div>
                <div class="alert-item">✅ Xuất kho tự động</div>
                <div class="alert-item">✅ <strong>Cộng điểm loyalty</strong> cho khách hàng</div>
              </div>
            </div>
            <div class="form-field">
              <label>Số tiền thanh toán</label>
              <div class="amount-display">{{ formatMoney(detail?.totalAmount) }}</div>
            </div>
            <div v-if="detail?.paymentMethod === 'TRANSFER'" class="qr-section">
              <div class="qr-label">Quét mã QR để thanh toán</div>
              <div class="qr-frame">
                <img :src="qrCodeUrl" v-if="qrCodeUrl" class="qr-img" />
              </div>
              <div class="qr-note">Sử dụng ứng dụng ngân hàng để quét mã QR</div>
            </div>
            <div v-if="detail?.paymentMethod === 'CARD'" class="card-form">
              <div class="form-field">
                <label>Loại thẻ</label>
                <select v-model="cardForm.type" class="glass-select">
                  <option value="VISA">VISA</option>
                  <option value="CREDIT">Credit Card</option>
                </select>
              </div>
              <div class="form-field">
                <label>Số thẻ</label>
                <input v-model="cardForm.number" class="glass-input" placeholder="1234 5678 1234 5678" />
              </div>
              <div class="form-field">
                <label>Tên chủ thẻ</label>
                <input v-model="cardForm.holder" class="glass-input" placeholder="NGUYEN VAN A" />
              </div>
              <div class="form-row">
                <div class="form-field">
                  <label>Expiry</label>
                  <input v-model="cardForm.expiry" class="glass-input" placeholder="MM/YY" />
                </div>
                <div class="form-field">
                  <label>CVV</label>
                  <input v-model="cardForm.cvv" class="glass-input" placeholder="123" />
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="glass-btn ghost" @click="showPaymentDialog = false">Hủy</button>
            <button class="glass-btn primary" @click="confirmPayment" :class="{ loading: paymentLoading }">
              Xác nhận thanh toán
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ================================================================ -->
    <!-- CANCEL DIALOG                                                     -->
    <!-- ================================================================ -->
    <transition name="modal-fade">
      <div v-if="showCancelDialog" class="modal-backdrop" @click.self="showCancelDialog = false">
        <div class="glass-modal modal-sm">
          <div class="modal-header">
            <div class="modal-title"><span class="modal-icon">❌</span> Hủy đơn hàng</div>
            <button class="modal-close" @click="showCancelDialog = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div :class="['warning-alert', detail?.paymentStatus === 'PAID' ? 'danger' : 'caution']">
              <div class="warning-title">{{ getCancelWarningTitle() }}</div>
              <div class="warning-body" v-html="getCancelWarningMessage()"></div>
            </div>
            <div class="form-field mt-3">
              <label>Lý do hủy <span class="optional">(không bắt buộc)</span></label>
              <textarea v-model="cancelReason" class="glass-textarea" rows="3" placeholder="Nhập lý do hủy đơn..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="glass-btn ghost" @click="showCancelDialog = false">Đóng</button>
            <button class="glass-btn danger" @click="confirmCancel" :class="{ loading: cancelLoading }">Xác nhận hủy</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ================================================================ -->
    <!-- RETURN DIALOG                                                     -->
    <!-- ================================================================ -->
    <transition name="modal-fade">
      <div v-if="showReturnDialog" class="modal-backdrop" @click.self="showReturnDialog = false">
        <div class="glass-modal">
          <div class="modal-header">
            <div class="modal-title"><span class="modal-icon">🔄</span> Yêu cầu trả hàng</div>
            <button class="modal-close" @click="showReturnDialog = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-field">
              <label>Chọn sản phẩm</label>
              <select v-model="returnForm.orderItemId" class="glass-select">
                <option value="" disabled>Chọn sản phẩm muốn trả</option>
                <option v-for="item in detail?.items" :key="item.productId" :value="item.productId">
                  {{ item.productName }} - {{ item.variantName }}
                </option>
              </select>
            </div>
            <div class="form-field">
              <label>Số lượng</label>
              <input type="number" v-model="returnForm.quantity" :min="1" :max="getMaxReturnQuantity()" class="glass-input" />
            </div>
            <div class="form-field">
              <label>Lý do trả hàng</label>
              <textarea v-model="returnForm.reason" class="glass-textarea" rows="3" placeholder="Nhập lý do..."></textarea>
            </div>
            <div class="form-field">
              <label>Số tiền hoàn</label>
              <div class="amount-display">{{ formatMoney(returnForm.refundAmount) }}</div>
            </div>
            <div class="info-alert warning-info">
              <div class="alert-item">⚠️ Điểm loyalty đã cộng sẽ bị trừ lại khi trả hàng được duyệt.</div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="glass-btn ghost" @click="showReturnDialog = false">Đóng</button>
            <button class="glass-btn primary" @click="submitReturn">Gửi yêu cầu</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ================================================================ -->
    <!-- DELIVERED CONFIRM DIALOG                                          -->
    <!-- ================================================================ -->
    <transition name="modal-fade">
      <div v-if="showDeliveredDialog" class="modal-backdrop" @click.self="showDeliveredDialog = false">
        <div class="glass-modal modal-sm">
          <div class="modal-header">
            <div class="modal-title"><span class="modal-icon">📦</span> Xác nhận đã nhận hàng</div>
            <button class="modal-close" @click="showDeliveredDialog = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="delivered-hero">
              <div class="delivered-hero-icon">✅</div>
              <div class="delivered-hero-text">
                <p class="delivered-hero-title">Bạn đã nhận được hàng?</p>
                <p class="delivered-hero-sub">Hành động này không thể hoàn tác. Vui lòng kiểm tra hàng trước khi xác nhận.</p>
              </div>
            </div>
            <div class="delivered-checklist">
              <div class="delivered-checklist-header">Sau khi xác nhận</div>
              <div class="delivered-checklist-items">
                <div class="dc-item dc-green">
                  <div class="dc-dot"></div>
                  <span>Đơn hàng chuyển sang trạng thái <strong>DELIVERED</strong></span>
                </div>
                <div class="dc-item dc-green">
                  <div class="dc-dot"></div>
                  <span>Người bán nhận được thanh toán</span>
                </div>
                <div class="dc-item dc-amber">
                  <div class="dc-dot"></div>
                  <span>Bạn vẫn có thể gửi <strong>yêu cầu trả hàng</strong> nếu cần</span>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="glass-btn ghost" @click="showDeliveredDialog = false">Chưa nhận</button>
            <button class="glass-btn primary" :class="{ loading: deliveredLoading }" @click="doConfirmDelivered">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12" />
              </svg>
              Xác nhận nhận hàng
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { returnsApi } from "../../api/returns.api";
import { paymentsApi } from "../../api/payments";
import { spinWheelApi } from "../../api/spinWheel.api";
import { toast } from "../../ui/toast";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const cancelLoading = ref(false);
const paymentLoading = ref(false);
const detail = ref(null);
const orderId = computed(() => route.params.orderId);

const showCancelDialog = ref(false);
const cancelReason = ref("");
const showReturnDialog = ref(false);
const showPaymentDialog = ref(false);

const spinStatus = reactive({
  loading: false,
  checked: false,
  hasActiveBonus: false,
  bonusRate: 0,
  bonusExpiresAt: null,
});

const returnForm = reactive({
  orderItemId: null,
  quantity: 1,
  reason: "",
  refundAmount: 0,
});
const cardForm = reactive({
  type: "VISA",
  number: "",
  holder: "",
  expiry: "",
  cvv: "",
});
const qrCodeUrl = ref("");

// ── Detect delivery method từ notes ──────────────────────────────
// buildNotes() bên create ghi: "Giao tại nhà" hoặc "Nhận tại cửa hàng"
const isHomeDelivery = computed(() => {
  if (!detail.value?.notes) return false;
  return detail.value.notes.toLowerCase().includes("giao tại nhà");
});

// ── Timeline steps ────────────────────────────────────────────────
const timelineSteps = computed(() => {
  if (!detail.value) return [];
  const s = detail.value.status;
  const isCash = detail.value.paymentMethod === "CASH";

  const steps = [
    { key: "placed", label: "Đặt hàng", statuses: ["PENDING", "PAID", "PROCESSING", "SHIPPING", "DELIVERED"] },
  ];
  if (!isCash) {
    steps.push({ key: "paid", label: "Thanh toán", statuses: ["PAID", "PROCESSING", "SHIPPING", "DELIVERED"] });
  }
  steps.push(
    { key: "processing", label: "Chuẩn bị hàng", statuses: ["PROCESSING", "SHIPPING", "DELIVERED"] },
    { key: "shipping",   label: "Đang chờ nhận hàng", statuses: ["SHIPPING", "DELIVERED"] },
    { key: "delivered",  label: "Đã giao", statuses: ["DELIVERED"] },
  );

  return steps.map((step) => ({
    ...step,
    active: step.statuses.includes(s),
    current: step.statuses[0] === s,
  }));
});

// ── Timeline progress percentage ─────────────────────────────────
const timelineProgressPercent = computed(() => {
  if (!detail.value || !timelineSteps.value.length) return 0;
  const total = timelineSteps.value.length;
  const currentIdx = timelineSteps.value.findIndex((s) => s.current);
  if (currentIdx === -1) return timelineSteps.value.every((s) => s.active) ? 100 : 0;
  return Math.round((currentIdx / (total - 1)) * 100);
});

// ── Return window: 24h từ delivered_at ───────────────────────────
const isReturnWindowOpen = computed(() => {
  if (detail.value?.status !== "DELIVERED") return false;
  const deliveredAt = detail.value?.deliveredAt;
  if (!deliveredAt) return true;
  const elapsed = Date.now() - new Date(deliveredAt).getTime();
  return elapsed < 24 * 60 * 60 * 1000;
});

// ── Đếm ngược thời gian còn lại trong cửa sổ 24h ────────────────
const returnTimeLeft = computed(() => {
  const deliveredAt = detail.value?.deliveredAt;
  if (!deliveredAt) return "";
  const remaining = 24 * 60 * 60 * 1000 - (Date.now() - new Date(deliveredAt).getTime());
  if (remaining <= 0) return "";
  const h = Math.floor(remaining / (1000 * 60 * 60));
  const m = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60));
  return h > 0 ? `${h}h ${m}p` : `${m} phút`;
});

// ── Parse notes (pipe-separated) ─────────────────────────────────
function parseNotes(notes) {
  if (!notes) return [];
  return notes
    .split("|")
    .map((s) => s.trim())
    .filter(Boolean)
    .map((line) => {
      if (line.toLowerCase().includes("vip"))
        return { type: "vip", icon: "👑", text: line };
      if (line.toLowerCase().includes("mã") || line.toLowerCase().includes("discount"))
        return { type: "discount", icon: "🏷️", text: line };
      if (line.toLowerCase().includes("giao") || line.toLowerCase().includes("địa chỉ") || line.toLowerCase().includes("nhận tại"))
        return { type: "delivery", icon: "📍", text: line };
      if (line.toLowerCase().includes("thanh toán"))
        return { type: "payment", icon: "💳", text: line };
      return { type: "default", icon: "📝", text: line };
    });
}

function getNoteTypeLabel(type) {
  const map = { vip: "VIP", discount: "Giảm giá", delivery: "Giao hàng", payment: "Thanh toán", default: "Ghi chú" };
  return map[type] || "Ghi chú";
}

// ── Format datetime "HH:mm · DD/MM/YYYY" ─────────────────────────
function formatDateTime(dateStr) {
  if (!dateStr) return "—";
  const d = new Date(dateStr);
  const time = d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
  const date = d.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
  return `${time} · ${date}`;
}

function generateFakeBankQR() {
  const amount = detail.value?.totalAmount || 0;
  const order = detail.value?.orderNumber || orderId.value;
  qrCodeUrl.value = `https://img.vietqr.io/image/VCB-1234567890-compact2.png?amount=${amount}&addInfo=ORDER${order}`;
}

async function openPaymentDialog() {
  showPaymentDialog.value = true;
  await fetchSpinStatus();
  if (detail.value?.paymentMethod === "TRANSFER") generateFakeBankQR();
}

const estimatedSpinDiscount = computed(() => {
  if (!detail.value?.subtotal || !spinStatus.bonusRate) return 0;
  if (detail.value?.spinDiscountRate > 0) return 0;
  return Math.round((detail.value.subtotal * spinStatus.bonusRate) / 100);
});

watch(() => returnForm.orderItemId, (newItemId) => {
  if (!newItemId || !detail.value?.items) return;
  const item = detail.value.items.find((i) => i.productId === newItemId);
  if (item) { returnForm.quantity = 1; returnForm.refundAmount = item.price; }
});

watch(() => returnForm.quantity, (newQty) => {
  if (!returnForm.orderItemId || !detail.value?.items) return;
  const item = detail.value.items.find((i) => i.productId === returnForm.orderItemId);
  if (item) returnForm.refundAmount = item.price * newQty;
});

function formatMoney(val) {
  if (val === null || val === undefined) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(val);
}

function formatExpiry(dateStr) {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" });
}

async function reload() {
  loading.value = true;
  try {
    const res = await ordersApi.getById(orderId.value);
    detail.value = res?.data?.data || res?.data;
  } catch (e) {
    toast("Không thể tải chi tiết đơn hàng", "error");
  } finally {
    loading.value = false;
  }
}

async function fetchSpinStatus() {
  const customerId = detail.value?.customerId;
  if (!customerId) return;
  spinStatus.loading = true;
  spinStatus.checked = false;
  spinStatus.hasActiveBonus = false;
  try {
    const res = await spinWheelApi.getStatus(customerId);
    const data = res?.data?.data ?? res?.data ?? res;
    const bonus = Number(data?.currentBonus ?? 0);
    if (bonus > 0) {
      spinStatus.hasActiveBonus = true;
      spinStatus.bonusRate = bonus;
      spinStatus.bonusExpiresAt = data?.bonusExpiresAt ?? null;
    }
  } catch (err) {
    console.warn("SpinWheel error:", err?.message);
  } finally {
    spinStatus.loading = false;
    spinStatus.checked = true;
  }
}

function getCancelWarningTitle() {
  return detail.value?.paymentStatus === "PAID"
    ? "⚠️ Cảnh báo: Hủy đơn đã thanh toán"
    : "Xác nhận hủy đơn";
}

function getCancelWarningMessage() {
  if (detail.value?.paymentStatus === "PAID") {
    const pts = Math.floor((detail.value.totalAmount || 0) / 10000);
    return `<p><strong>Nếu hủy đơn đã thanh toán:</strong></p><ul><li>❌ Trừ lại <strong>${pts} điểm</strong> loyalty</li><li>💰 Hoàn tiền cho khách</li><li>📦 Nhập lại kho</li></ul>`;
  }
  return `<p>Đơn chưa thanh toán sẽ được hủy miễn phí.</p>`;
}

function getMaxReturnQuantity() {
  if (!returnForm.orderItemId || !detail.value?.items) return 1;
  return detail.value.items.find((i) => i.productId === returnForm.orderItemId)?.quantity || 1;
}

async function confirmPayment() {
  paymentLoading.value = true;
  try {
    await paymentsApi.create({ orderId: Number(orderId.value), method: detail.value.paymentMethod, transactionRef: `TXN-${Date.now()}` });
    toast("Thanh toán thành công", "success");
    showPaymentDialog.value = false;
    await reload();
  } catch (e) {
    toast("Lỗi thanh toán", "error");
  } finally {
    paymentLoading.value = false;
  }
}

async function confirmCancel() {
  cancelLoading.value = true;
  try {
    await ordersApi.cancel(orderId.value, cancelReason.value);
    toast("✅ Đã hủy đơn hàng thành công.", "success");
    showCancelDialog.value = false;
    await reload();
  } catch (e) {
    toast("Lỗi khi hủy đơn", "error");
  } finally {
    cancelLoading.value = false;
  }
}

async function submitReturn() {
  if (!returnForm.orderItemId || !returnForm.reason) return;
  try {
    await returnsApi.create({ orderId: Number(orderId.value), ...returnForm });
    toast("✅ Đã gửi yêu cầu trả hàng", "success");
    showReturnDialog.value = false;
  } catch (e) {
    toast("Lỗi khi gửi yêu cầu", "error");
  }
}

const deliveredLoading = ref(false);
const showDeliveredDialog = ref(false);

const confirmDelivered = () => { showDeliveredDialog.value = true; };

const doConfirmDelivered = async () => {
  deliveredLoading.value = true;
  showDeliveredDialog.value = false;
  try {
    await ordersApi.markAsDelivered(orderId.value);
    toast("Đã xác nhận nhận hàng", "success");
    await reload();
  } catch (err) {
    toast("Không thể cập nhật trạng thái đơn hàng", "error");
  } finally {
    deliveredLoading.value = false;
  }
};

function isReturned(status) {
  return ["PARTIALLY_RETURNED", "RETURNED"].includes(status);
}

onMounted(() => reload());
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap");

*,*::before,*::after { box-sizing: border-box; margin: 0; padding: 0; }

.glass-page {
  min-height: 100vh;
  font-family: "Inter", sans-serif;
  color: #1e293b;
  position: relative;
  overflow-x: hidden;
  padding: 32px 24px 60px;
}

.container-xl { position: relative; z-index: 1; max-width: 1280px; margin: 0 auto; }

.order-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 20px;
  align-items: start;
}

.order-right { position: sticky; top: 24px; }

.glass-card {
  background: rgba(255,255,255,0.62);
  backdrop-filter: blur(24px) saturate(1.8);
  -webkit-backdrop-filter: blur(24px) saturate(1.8);
  border: 1px solid rgba(255,255,255,0.85);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 8px 32px rgba(99,102,241,0.08), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.9);
}

/* ── HEADER ── */
.order-header { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 16px; }

.kicker { display: flex; align-items: center; gap: 6px; font-size: 11px; font-weight: 700; letter-spacing: 0.12em; text-transform: uppercase; color: #6366f1; margin-bottom: 4px; }
.kicker-dot { width: 6px; height: 6px; border-radius: 50%; background: #6366f1; box-shadow: 0 0 8px rgba(99,102,241,0.6); animation: pulse-dot 2s ease-in-out infinite; }
@keyframes pulse-dot { 0%,100% { box-shadow: 0 0 6px rgba(99,102,241,0.5); } 50% { box-shadow: 0 0 14px rgba(99,102,241,0.8), 0 0 24px rgba(99,102,241,0.3); } }

.title { font-size: 26px; font-weight: 900; color: #0f172a; letter-spacing: -0.02em; }

.badge-row { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; }

.status-pill { position: relative; display: inline-flex; align-items: center; padding: 5px 14px; border-radius: 100px; font-size: 12px; font-weight: 600; letter-spacing: 0.04em; overflow: hidden; }
.pill-glow { position: absolute; inset: 0; border-radius: 100px; opacity: 0.15; }
.status-pending  { background: rgba(99,102,241,0.1);  border: 1px solid rgba(99,102,241,0.3);  color: #4338ca; }
.status-paid     { background: rgba(16,185,129,0.1);  border: 1px solid rgba(16,185,129,0.3);  color: #065f46; }
.status-shipping { background: rgba(245,158,11,0.1);  border: 1px solid rgba(245,158,11,0.3);  color: #92400e; }
.status-delivered{ background: rgba(16,185,129,0.1);  border: 1px solid rgba(16,185,129,0.3);  color: #065f46; }
.status-cancelled{ background: rgba(239,68,68,0.1);   border: 1px solid rgba(239,68,68,0.3);   color: #991b1b; }
.status-returned { background: rgba(245,158,11,0.1);  border: 1px solid rgba(245,158,11,0.3);  color: #92400e; }

/* ── BUTTONS ── */
.order-actions { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }

.glass-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px; border-radius: 12px; font-size: 13.5px; font-weight: 500;
  border: 1px solid transparent; cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4,0,0.2,1);
  position: relative; overflow: hidden; backdrop-filter: blur(10px);
}
.glass-btn::before { content: ""; position: absolute; inset: 0; opacity: 0; transition: opacity 0.2s; background: rgba(255,255,255,0.06); }
.glass-btn:hover::before { opacity: 1; }
.glass-btn:active { transform: scale(0.97); }
.glass-btn.ghost   { background: rgba(255,255,255,0.6); border-color: rgba(0,0,0,0.1); color: #475569; }
.glass-btn.primary { background: linear-gradient(135deg,#6366f1,#4f46e5); border-color: rgba(99,102,241,0.3); color: #fff; box-shadow: 0 4px 16px rgba(99,102,241,0.35); }
.glass-btn.danger  { background: linear-gradient(135deg,#ef4444,#dc2626); border-color: rgba(239,68,68,0.3); color: #fff; box-shadow: 0 4px 16px rgba(239,68,68,0.25); }
.glass-btn.warning { background: linear-gradient(135deg,#f59e0b,#d97706); border-color: rgba(245,158,11,0.3); color: #fff; box-shadow: 0 4px 12px rgba(245,158,11,0.25); }
.glass-btn.loading { opacity: 0.7; pointer-events: none; }
.glass-btn.btn-disabled, .glass-btn:disabled { opacity: 0.42; cursor: not-allowed; pointer-events: none; filter: grayscale(0.45); box-shadow: none; }

/* ── TIMESTAMPS CARD ── */
.timestamps-card {
  padding: 20px 22px;
  margin-top: 14px;
}

.timestamps-card-header {
  display: flex; align-items: center; gap: 6px;
  font-size: 10.5px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.09em; color: #94a3b8;
  margin-bottom: 18px;
}
.timestamps-card-header svg { color: #cbd5e1; flex-shrink: 0; }

.ts-card-row {
  display: flex; align-items: flex-start; gap: 12px;
}
.ts-card-row--active .ts-card-info .ts-card-value { color: #065f46; }

.ts-card-connector {
  width: 2px; height: 18px;
  background: linear-gradient(to bottom, rgba(99,102,241,0.2), rgba(203,213,225,0.3));
  border-radius: 1px;
  margin: 5px 0 5px 14px;
}

.ts-card-icon {
  width: 28px; height: 28px; flex-shrink: 0;
  border-radius: 8px; display: flex; align-items: center; justify-content: center;
}
.ts-card-icon--create {
  background: rgba(99,102,241,0.08);
  border: 1px solid rgba(99,102,241,0.18);
  color: #6366f1;
}
.ts-card-icon--paid {
  background: rgba(14,165,233,0.08);
  border: 1px solid rgba(14,165,233,0.2);
  color: #0ea5e9;
}
.ts-card-icon--delivered {
  background: rgba(16,185,129,0.08);
  border: 1px solid rgba(16,185,129,0.2);
  color: #10b981;
}
.ts-card-icon--pending {
  background: rgba(203,213,225,0.2);
  border: 1px solid rgba(203,213,225,0.4);
  color: #cbd5e1;
}
.ts-card-icon--cancelled {
  background: rgba(239,68,68,0.08);
  border: 1px solid rgba(239,68,68,0.2);
  color: #ef4444;
}

.ts-card-value--paid     { color: #0369a1; }
.ts-card-value--cancelled { color: #b91c1c; }

.ts-card-connector--cancelled {
  background: linear-gradient(to bottom, rgba(203,213,225,0.3), rgba(239,68,68,0.25));
}

.ts-card-row--cancelled .ts-card-label { color: #ef4444; }

.ts-card-info {
  display: flex; flex-direction: column; gap: 3px;
  padding-top: 2px; flex: 1;
}
.ts-card-label {
  font-size: 10.5px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.08em; color: #94a3b8;
}
.ts-card-value {
  font-size: 13px; font-weight: 600; color: #1e293b; line-height: 1.4;
}
.ts-card-value--muted { color: #cbd5e1; font-weight: 400; font-style: italic; }

.ts-card-badge {
  display: inline-flex; align-items: center; gap: 4px;
  margin-top: 4px; padding: 3px 9px;
  border-radius: 20px; font-size: 11px; font-weight: 600;
  width: fit-content;
}
.ts-card-badge--warn {
  color: #d97706;
  background: rgba(245,158,11,0.08);
  border: 1px solid rgba(245,158,11,0.22);
}
.ts-card-badge--expired {
  color: #ef4444;
  background: rgba(239,68,68,0.07);
  border: 1px solid rgba(239,68,68,0.18);
}

/* ── DELIVERY TAG (inline trong info card) ── */
.delivery-tag {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 3px 10px; border-radius: 6px;
  font-size: 11.5px; font-weight: 600; letter-spacing: 0.03em;
}
.delivery-home {
  background: rgba(245,158,11,0.08);
  border: 1px solid rgba(245,158,11,0.22);
  color: #92400e;
}
.delivery-store {
  background: rgba(99,102,241,0.07);
  border: 1px solid rgba(99,102,241,0.2);
  color: #4338ca;
}

/* ── TIMELINE V2 ── */
.timeline-track {
  position: relative; display: flex; align-items: flex-start; justify-content: space-between;
  margin: 28px 0 4px; padding: 20px 28px 18px;
  background: rgba(255,255,255,0.5); border: 1px solid rgba(255,255,255,0.85);
  border-radius: 16px; overflow: visible;
}
.timeline-progress-bg { position: absolute; top: 39px; left: 52px; right: 52px; height: 3px; background: rgba(226,232,240,0.8); border-radius: 100px; z-index: 0; }
.timeline-progress-fill { height: 100%; border-radius: 100px; background: linear-gradient(90deg,#a5b4fc,#6366f1,#4f46e5); transition: width 0.6s cubic-bezier(0.4,0,0.2,1); box-shadow: 0 0 8px rgba(99,102,241,0.4); }
.tl-step { display: flex; flex-direction: column; align-items: center; gap: 9px; flex: 1; position: relative; z-index: 1; min-width: 72px; }
.tl-node { position: relative; width: 38px; height: 38px; display: flex; align-items: center; justify-content: center; }
.tl-node-ring { position: absolute; inset: -4px; border-radius: 50%; border: 2px solid rgba(99,102,241,0.3); animation: tl-ring-expand 1.8s ease-in-out infinite; }
@keyframes tl-ring-expand { 0%,100% { transform: scale(1); opacity: 0.7; } 50% { transform: scale(1.22); opacity: 0; } }
.tl-node-inner { width: 38px; height: 38px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); }
.tl-done    .tl-node-inner { background: linear-gradient(135deg,#a5b4fc,#6366f1); color: #fff; box-shadow: 0 4px 12px rgba(99,102,241,0.35); }
.tl-current .tl-node-inner { background: linear-gradient(135deg,#6366f1,#4f46e5); color: #fff; box-shadow: 0 4px 16px rgba(99,102,241,0.45), 0 0 0 4px rgba(99,102,241,0.15); }
.tl-pending .tl-node-inner { background: rgba(255,255,255,0.7); border: 1.5px solid rgba(203,213,225,0.9); color: #94a3b8; }
.tl-pulse-dot { width: 8px; height: 8px; border-radius: 50%; background: #fff; animation: tl-pd 1.4s ease-in-out infinite; }
@keyframes tl-pd { 0%,100% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.3); opacity: 0.7; } }
.tl-step-num { font-size: 11px; font-weight: 600; color: #cbd5e1; }
.tl-label-wrap { display: flex; flex-direction: column; align-items: center; gap: 4px; text-align: center; }
.tl-label { font-size: 11px; font-weight: 500; white-space: nowrap; color: #94a3b8; transition: color 0.2s; }
.tl-done .tl-label, .tl-current .tl-label { color: #1e293b; font-weight: 600; }
.tl-current-badge { font-size: 9px; font-weight: 700; letter-spacing: 0.07em; text-transform: uppercase; padding: 2px 8px; border-radius: 100px; background: rgba(99,102,241,0.1); border: 1px solid rgba(99,102,241,0.25); color: #4f46e5; white-space: nowrap; }

/* ── INFO GRID ── */
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 24px; }
.glass-info-card { background: rgba(255,255,255,0.55); border: 1px solid rgba(255,255,255,0.85); border-radius: 14px; padding: 18px 20px; transition: border-color 0.2s, box-shadow 0.2s; }
.glass-info-card:hover { border-color: rgba(99,102,241,0.2); box-shadow: 0 4px 16px rgba(99,102,241,0.08); }
.info-card-header { display: flex; align-items: center; gap: 8px; font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.07em; margin-bottom: 14px; }
.info-icon { width: 28px; height: 28px; background: rgba(99,102,241,0.08); border: 1px solid rgba(99,102,241,0.15); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #6366f1; }
.info-card-body { display: flex; flex-direction: column; gap: 10px; }
.info-row { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.info-label { font-size: 13px; color: #94a3b8; flex-shrink: 0; }
.info-value { font-size: 13.5px; color: #1e293b; font-weight: 500; text-align: right; }
.method-tag { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11.5px; font-weight: 600; background: rgba(99,102,241,0.08); border: 1px solid rgba(99,102,241,0.2); color: #4f46e5; letter-spacing: 0.04em; }

/* ── NOTES V2 ── */
.notes-v2 { margin-top: 14px; border-top: 1px solid rgba(0,0,0,0.06); padding-top: 14px; }
.notes-v2-header { display: flex; align-items: center; gap: 6px; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; color: #94a3b8; margin-bottom: 10px; }
.notes-v2-count { margin-left: auto; font-size: 10px; font-weight: 700; padding: 1px 7px; border-radius: 20px; background: rgba(148,163,184,0.12); border: 1px solid rgba(148,163,184,0.2); color: #94a3b8; }
.notes-v2-list { display: flex; flex-direction: column; gap: 6px; }
.notes-v2-item { display: flex; align-items: stretch; border-radius: 10px; overflow: hidden; border: 1px solid rgba(0,0,0,0.06); background: rgba(255,255,255,0.55); animation: note-slide-in 0.3s cubic-bezier(0.4,0,0.2,1) both; }
@keyframes note-slide-in { from { opacity: 0; transform: translateX(-6px); } to { opacity: 1; transform: translateX(0); } }
.notes-v2-left { display: flex; flex-direction: column; align-items: center; width: 40px; flex-shrink: 0; padding: 10px 0; gap: 5px; }
.notes-v2-icon-wrap { width: 26px; height: 26px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 13px; background: rgba(0,0,0,0.04); }
.notes-v2-icon { font-size: 13px; line-height: 1; }
.notes-v2-accent-bar { flex: 1; width: 2px; border-radius: 1px; background: rgba(0,0,0,0.08); }
.notes-v2-body { flex: 1; padding: 10px 14px 10px 6px; display: flex; flex-direction: column; gap: 3px; justify-content: center; }
.notes-v2-type-tag { font-size: 9.5px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; color: #94a3b8; }
.notes-v2-text { font-size: 12.5px; color: #475569; line-height: 1.5; margin: 0; }
.notes-v2-vip      { border-color: rgba(245,158,11,0.2) !important; background: rgba(255,251,235,0.7) !important; }
.notes-v2-vip      .notes-v2-icon-wrap { background: rgba(245,158,11,0.12); }
.notes-v2-vip      .notes-v2-accent-bar { background: linear-gradient(to bottom,#f59e0b,rgba(245,158,11,0.15)); }
.notes-v2-vip      .notes-v2-type-tag { color: #b45309; }
.notes-v2-vip      .notes-v2-text { color: #78350f; }
.notes-v2-discount { border-color: rgba(16,185,129,0.2) !important; background: rgba(236,253,245,0.6) !important; }
.notes-v2-discount .notes-v2-icon-wrap { background: rgba(16,185,129,0.1); }
.notes-v2-discount .notes-v2-accent-bar { background: linear-gradient(to bottom,#10b981,rgba(16,185,129,0.15)); }
.notes-v2-discount .notes-v2-type-tag { color: #047857; }
.notes-v2-discount .notes-v2-text { color: #065f46; }
.notes-v2-delivery { border-color: rgba(99,102,241,0.18) !important; background: rgba(238,242,255,0.6) !important; }
.notes-v2-delivery .notes-v2-icon-wrap { background: rgba(99,102,241,0.08); }
.notes-v2-delivery .notes-v2-accent-bar { background: linear-gradient(to bottom,#6366f1,rgba(99,102,241,0.12)); }
.notes-v2-delivery .notes-v2-type-tag { color: #4338ca; }
.notes-v2-delivery .notes-v2-text { color: #3730a3; }
.notes-v2-payment  { border-color: rgba(14,165,233,0.2) !important; background: rgba(240,249,255,0.6) !important; }
.notes-v2-payment  .notes-v2-icon-wrap { background: rgba(14,165,233,0.1); }
.notes-v2-payment  .notes-v2-accent-bar { background: linear-gradient(to bottom,#0ea5e9,rgba(14,165,233,0.12)); }
.notes-v2-payment  .notes-v2-type-tag { color: #0369a1; }
.notes-v2-payment  .notes-v2-text { color: #075985; }
.notes-v2-default  .notes-v2-type-tag { color: #64748b; }

/* ── ITEMS ── */
.items-section { margin-top: 24px; }
.section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.section-header h5 { font-size: 11px; font-weight: 800; color: #1e293b; text-transform: uppercase; letter-spacing: 0.08em; }
.item-count { font-size: 11.5px; padding: 3px 10px; background: rgba(99,102,241,0.08); border: 1px solid rgba(99,102,241,0.18); border-radius: 20px; color: #4f46e5; font-weight: 600; }
.items-card-list { display: flex; flex-direction: column; gap: 10px; }
.item-card { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 16px 20px; border-radius: 14px; background: rgba(255,255,255,0.5); border: 1px solid rgba(255,255,255,0.9); box-shadow: 0 2px 8px rgba(99,102,241,0.04); transition: transform 0.2s cubic-bezier(0.4,0,0.2,1), box-shadow 0.2s, background 0.2s, border-color 0.2s; animation: card-in 0.35s cubic-bezier(0.4,0,0.2,1) both; animation-delay: calc(var(--i) * 60ms); }
@keyframes card-in { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.item-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(99,102,241,0.1), 0 2px 8px rgba(0,0,0,0.05); background: rgba(255,255,255,0.75); border-color: rgba(99,102,241,0.2); }
.item-card-left { display: flex; align-items: center; gap: 14px; flex: 1; min-width: 0; }
.item-index { width: 30px; height: 30px; border-radius: 10px; background: rgba(99,102,241,0.07); border: 1px solid rgba(99,102,241,0.15); color: #6366f1; font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: background 0.2s, color 0.2s; }
.item-card:hover .item-index { background: rgba(99,102,241,0.12); color: #4f46e5; }
.item-info { min-width: 0; display: flex; flex-direction: column; gap: 3px; }
.item-name { font-size: 14px; font-weight: 600; color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-variant { font-size: 12px; color: #94a3b8; }
.item-sku { font-size: 10.5px; color: #cbd5e1; font-family: "Courier New", monospace; letter-spacing: 0.04em; }
.item-card-right { display: flex; align-items: center; gap: 24px; flex-shrink: 0; }
.item-stat { display: flex; flex-direction: column; align-items: flex-end; gap: 3px; }
.stat-label { font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.08em; color: #cbd5e1; }
.stat-value { font-size: 13.5px; font-weight: 500; color: #64748b; }

/* ── TOTALS CARD ── */
.totals-card { padding: 24px; }
.totals-header { font-size: 11px; font-weight: 700; letter-spacing: 0.1em; text-transform: uppercase; color: #94a3b8; margin-bottom: 20px; }
.totals-list { display: flex; flex-direction: column; gap: 2px; }
.totals-row { display: flex; justify-content: space-between; padding: 9px 0; font-size: 14px; color: #64748b; border-bottom: 1px solid rgba(0,0,0,0.05); }
.totals-row:last-child { border-bottom: none; }
.discount-row { color: #059669; }
.shipping-row { color: #92400e; }
.free-row { color: #4338ca; }
.free-badge { font-size: 11px; font-weight: 700; padding: 2px 10px; border-radius: 20px; background: rgba(16,185,129,0.08); border: 1px solid rgba(16,185,129,0.2); color: #065f46; }
.totals-divider { height: 1px; background: rgba(0,0,0,0.07); margin: 16px 0; }
.totals-final { display: flex; justify-content: space-between; align-items: baseline; padding-top: 4px; }
.totals-final span:first-child { font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: #64748b; }
.final-amount { font-size: 24px; font-weight: 900; background: linear-gradient(135deg,#6366f1,#0ea5e9); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }

/* ── SKELETON ── */
.skeleton-wrap { padding: 24px 0; display: flex; flex-direction: column; gap: 12px; }
.skeleton-line { height: 14px; border-radius: 7px; background: linear-gradient(90deg,rgba(0,0,0,0.04) 25%,rgba(0,0,0,0.08) 50%,rgba(0,0,0,0.04) 75%); background-size: 400% 100%; animation: shimmer 1.5s infinite; }
@keyframes shimmer { 0% { background-position: 100% 0; } 100% { background-position: -100% 0; } }

/* ── MODAL ── */
.modal-backdrop { position: fixed; inset: 0; background: rgba(15,23,42,0.35); backdrop-filter: blur(8px); z-index: 1000; display: flex; align-items: center; justify-content: center; padding: 20px; }
.glass-modal { background: rgba(255,255,255,0.82); backdrop-filter: blur(28px) saturate(2); -webkit-backdrop-filter: blur(28px) saturate(2); border: 1px solid rgba(255,255,255,0.95); border-radius: 22px; width: 100%; max-width: 520px; max-height: 85vh; overflow-y: auto; box-shadow: 0 24px 64px rgba(99,102,241,0.15), 0 8px 24px rgba(0,0,0,0.1), inset 0 1px 0 rgba(255,255,255,1); }
.modal-sm { max-width: 480px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 22px 26px 18px; border-bottom: 1px solid rgba(0,0,0,0.06); }
.modal-title { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: 700; color: #0f172a; }
.modal-icon { font-size: 20px; }
.modal-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; border-radius: 8px; background: rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.07); color: #94a3b8; cursor: pointer; transition: all 0.2s; }
.modal-close:hover { background: rgba(239,68,68,0.08); border-color: rgba(239,68,68,0.2); color: #ef4444; }
.modal-body { padding: 20px 26px; display: flex; flex-direction: column; gap: 16px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 18px 26px; border-top: 1px solid rgba(0,0,0,0.06); }

/* ── SPIN BANNER ── */
.spin-loading { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-radius: 12px; background: rgba(99,102,241,0.05); border: 1px solid rgba(99,102,241,0.15); font-size: 13px; color: #4f46e5; }
.spin-ring { width: 18px; height: 18px; border: 2px solid rgba(99,102,241,0.2); border-top-color: #6366f1; border-radius: 50%; animation: spin 0.8s linear infinite; flex-shrink: 0; }
@keyframes spin { to { transform: rotate(360deg); } }
.spin-banner { border-radius: 14px; background: rgba(16,185,129,0.05); border: 1px solid rgba(16,185,129,0.2); overflow: hidden; }
.spin-banner-top { display: flex; align-items: center; gap: 12px; padding: 14px 16px; }
.spin-badge-icon { font-size: 24px; flex-shrink: 0; }
.spin-banner-title { font-size: 13.5px; font-weight: 600; color: #065f46; margin-bottom: 3px; }
.spin-banner-sub { font-size: 12.5px; color: #047857; }
.spin-rate-badge { margin-left: auto; padding: 6px 14px; border-radius: 100px; background: rgba(16,185,129,0.12); border: 1px solid rgba(16,185,129,0.25); color: #065f46; font-size: 14px; font-weight: 800; white-space: nowrap; }
.spin-breakdown { padding: 12px 16px; border-top: 1px solid rgba(16,185,129,0.1); display: flex; flex-direction: column; gap: 6px; background: rgba(255,255,255,0.4); }
.breakdown-row { display: flex; justify-content: space-between; font-size: 12.5px; color: #64748b; padding: 3px 0; }
.breakdown-row.positive { color: #059669; }
.breakdown-row.total-row { padding-top: 8px; margin-top: 4px; border-top: 1px solid rgba(0,0,0,0.06); color: #0f172a; font-size: 13.5px; }
.no-spin { display: flex; align-items: center; gap: 8px; padding: 10px 14px; border-radius: 10px; background: rgba(0,0,0,0.03); border: 1px solid rgba(0,0,0,0.06); font-size: 12.5px; color: #94a3b8; }

/* ── INFO ALERT ── */
.info-alert { padding: 14px 16px; border-radius: 12px; background: rgba(99,102,241,0.05); border: 1px solid rgba(99,102,241,0.15); }
.info-alert-title { font-size: 12.5px; font-weight: 700; color: #4f46e5; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.06em; }
.info-alert-rows { display: flex; flex-direction: column; gap: 5px; }
.alert-item { font-size: 13px; color: #475569; }
.warning-info { background: rgba(245,158,11,0.05); border-color: rgba(245,158,11,0.2); }
.warning-info .alert-item { color: #78350f; }

/* ── FORM ELEMENTS ── */
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field label { font-size: 12px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.07em; }
.optional { font-weight: 400; color: #94a3b8; text-transform: none; letter-spacing: 0; }
.glass-input, .glass-select, .glass-textarea { width: 100%; padding: 10px 14px; background: rgba(255,255,255,0.7); border: 1px solid rgba(0,0,0,0.1); border-radius: 10px; color: #1e293b; font-family: "Inter", sans-serif; font-size: 14px; outline: none; transition: border-color 0.2s, box-shadow 0.2s; }
.glass-input:focus, .glass-select:focus, .glass-textarea:focus { border-color: rgba(99,102,241,0.5); box-shadow: 0 0 0 3px rgba(99,102,241,0.1); background: rgba(255,255,255,0.95); }
.glass-select { appearance: none; cursor: pointer; }
.glass-textarea { resize: vertical; min-height: 80px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.amount-display { padding: 12px 16px; background: rgba(99,102,241,0.06); border: 1px solid rgba(99,102,241,0.18); border-radius: 10px; font-size: 18px; font-weight: 800; color: #4f46e5; }
.mt-3 { margin-top: 12px; }

/* ── WARNING ALERT ── */
.warning-alert { padding: 14px 16px; border-radius: 12px; }
.warning-alert.danger  { background: rgba(239,68,68,0.05); border: 1px solid rgba(239,68,68,0.2); }
.warning-alert.caution { background: rgba(245,158,11,0.06); border: 1px solid rgba(245,158,11,0.2); }
.warning-title { font-size: 13px; font-weight: 700; color: #b91c1c; margin-bottom: 8px; }
.warning-alert.caution .warning-title { color: #92400e; }
.warning-body { font-size: 13px; color: #64748b; line-height: 1.6; }
.warning-body ul { padding-left: 16px; margin-top: 6px; }
.warning-body li { margin-bottom: 4px; }

/* ── QR ── */
.qr-section { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 8px 0; }
.qr-label { font-size: 12px; color: #64748b; text-transform: uppercase; letter-spacing: 0.07em; font-weight: 600; }
.qr-frame { padding: 12px; background: white; border-radius: 14px; box-shadow: 0 8px 24px rgba(99,102,241,0.12), 0 2px 8px rgba(0,0,0,0.08); }
.qr-img { width: 220px; display: block; border-radius: 6px; }
.qr-note { font-size: 12px; color: #475569; text-align: center; }

/* ── TRANSITIONS ── */
.modal-fade-enter-active, .modal-fade-leave-active { transition: all 0.25s cubic-bezier(0.4,0,0.2,1); }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
.modal-fade-enter-from .glass-modal, .modal-fade-leave-to .glass-modal { transform: scale(0.95) translateY(10px); }
.slide-down-enter-active { transition: all 0.3s cubic-bezier(0.4,0,0.2,1); }
.slide-down-enter-from { opacity: 0; transform: translateY(-8px); }

/* ── DELIVERED DIALOG ── */
.delivered-hero { display: flex; align-items: flex-start; gap: 14px; padding: 16px; border-radius: 14px; background: rgba(99,102,241,0.04); border: 1px solid rgba(99,102,241,0.12); }
.delivered-hero-icon { font-size: 32px; line-height: 1; flex-shrink: 0; margin-top: 2px; }
.delivered-hero-title { font-size: 15px; font-weight: 700; color: #0f172a; margin-bottom: 5px; }
.delivered-hero-sub { font-size: 13px; color: #64748b; line-height: 1.55; }
.delivered-checklist { border-radius: 12px; background: rgba(255,255,255,0.55); border: 1px solid rgba(0,0,0,0.07); overflow: hidden; }
.delivered-checklist-header { padding: 10px 16px; font-size: 10.5px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.09em; color: #94a3b8; background: rgba(0,0,0,0.02); border-bottom: 1px solid rgba(0,0,0,0.05); }
.delivered-checklist-items { display: flex; flex-direction: column; padding: 6px 0; }
.dc-item { display: flex; align-items: center; gap: 12px; padding: 10px 16px; font-size: 13px; color: #475569; transition: background 0.15s; }
.dc-item:hover { background: rgba(0,0,0,0.02); }
.dc-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dc-green .dc-dot { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.4); }
.dc-amber .dc-dot { background: #f59e0b; box-shadow: 0 0 6px rgba(245,158,11,0.4); }
.dc-green { color: #065f46; }
.dc-amber { color: #78350f; }

/* ── RESPONSIVE ── */
@media (max-width: 960px) {
  .order-layout { grid-template-columns: 1fr; }
  .order-right { position: static; }
}
@media (max-width: 640px) {
  .glass-page { padding: 16px 14px 40px; }
  .glass-card { padding: 18px; }
  .info-grid { grid-template-columns: 1fr; }
  .title { font-size: 20px; }
  .timeline-track { padding: 16px; }
  .tl-step { min-width: 60px; }
  .tl-label { font-size: 10px; }
  .item-card { flex-direction: column; align-items: flex-start; }
  .item-card-right { width: 100%; justify-content: space-between; padding-top: 12px; border-top: 1px solid rgba(0,0,0,0.05); flex-wrap: wrap; gap: 12px; }

}
</style>