<template>
  <div class="spin-wheel-container">
    <!-- VIP Badge -->
    <div v-if="!status.isVip" class="not-vip-notice">
      <div class="icon">🔒</div>
      <h3>Tính năng VIP</h3>
      <p>Vòng quay may mắn chỉ dành cho khách hàng VIP</p>
      <p class="upgrade-hint">Nâng cấp lên VIP để nhận ưu đãi đặc biệt!</p>
    </div>

    <!-- Spin Wheel for VIP -->
    <div v-else class="vip-spin-section">
      <!-- Header -->
      <div class="wheel-header">
        <h2>🎡 Vòng Quay May Mắn VIP</h2>
        <p class="subtitle">Quay 1 lần mỗi tuần để nhận giảm giá bổ sung!</p>
      </div>

      <!-- Active Bonus Display -->
      <div v-if="status.currentBonus && status.currentBonus > 0" class="active-bonus">
        <div class="bonus-badge">
          <span class="bonus-icon">🎁</span>
          <div class="bonus-info">
            <div class="bonus-label">Ưu đãi hiện tại</div>
            <div class="bonus-value">{{ status.currentBonus }}% giảm giá</div>
            <div class="bonus-expires">
              Hết hạn: {{ formatDate(status.bonusExpiresAt) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Spin Wheel Canvas -->
      <div class="wheel-canvas-container">
        <canvas 
          ref="wheelCanvas" 
          width="400" 
          height="400"
          @click="handleSpin"
          :class="{ spinning: isSpinning, disabled: !status.canSpin }"
        ></canvas>
        
        <!-- Spin Button -->
        <button 
          v-if="status.canSpin"
          @click="handleSpin"
          :disabled="isSpinning"
          class="spin-button"
        >
          <span v-if="!isSpinning">{{ spinButtonText }}</span>
          <span v-else class="spinning-text">Đang quay...</span>
        </button>
        
        <div v-else class="already-spun-notice">
          <p>{{ status.message }}</p>
          <p class="next-spin-info">Quay lại vào thứ Hai tuần sau!</p>
        </div>
      </div>

      <!-- Result Modal -->
      <div v-if="showResult" class="result-modal" @click="closeResult">
        <div class="result-content" @click.stop>
          <div class="result-icon">🎉</div>
          <h3>Chúc Mừng!</h3>
          <div class="result-discount">
            {{ lastResult.discountBonus }}%
          </div>
          <p class="result-message">{{ lastResult.message }}</p>
          <p class="result-expires">
            Có hiệu lực đến: {{ formatDate(lastResult.expiresAt) }}
          </p>
          <button @click="closeResult" class="close-result-btn">
            Đóng
          </button>
        </div>
      </div>

      <!-- Spin History -->
      <div class="spin-history">
        <h3>Lịch Sử Quay Thưởng</h3>
        <div v-if="history.length === 0" class="no-history">
          Chưa có lịch sử quay thưởng
        </div>
        <div v-else class="history-list">
          <div 
            v-for="item in history" 
            :key="item.id"
            class="history-item"
            :class="getHistoryClass(item)"
          >
            <div class="history-icon">
              {{ getHistoryIcon(item.status) }}
            </div>
            <div class="history-details">
              <div class="history-discount">{{ item.discountBonus }}% giảm giá</div>
              <div class="history-date">{{ formatDate(item.spunAt) }}</div>
              <div class="history-status">{{ item.status }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { customersApi } from '../../api/customers.api';

export default {
  name: 'SpinWheel',
  
  data() {
    return {
      status: {
        isVip: false,
        canSpin: false,
        message: '',
        currentBonus: 0,
        bonusExpiresAt: null
      },
      prizes: [],
      history: [],
      isSpinning: false,
      showResult: false,
      lastResult: null,
      currentRotation: 0
    };
  },
  
  computed: {
    spinButtonText() {
      return this.isSpinning ? 'Đang quay...' : 'QUAY NGAY!';
    }
  },
  
  mounted() {
    this.loadData();
  },
  
  methods: {
    async loadData() {
      try {
        // Load status
        const statusRes = await customersApi.getSpinWheelStatus();
        this.status = statusRes.data;
        
        // Load prizes
        const prizesRes = await customersApi.getSpinWheelPrizes();
        this.prizes = prizesRes.data;
        
        // Load history
        const historyRes = await customersApi.getSpinWheelHistory();
        this.history = historyRes.data;
        
        // Draw wheel if VIP
        if (this.status.isVip) {
          this.$nextTick(() => {
            this.drawWheel();
          });
        }
      } catch (error) {
        console.error('Error loading spin wheel data:', error);
        alert('Không thể tải dữ liệu vòng quay');
      }
    },
    
    drawWheel() {
      const canvas = this.$refs.wheelCanvas;
      if (!canvas) return;
      
      const ctx = canvas.getContext('2d');
      const centerX = canvas.width / 2;
      const centerY = canvas.height / 2;
      const radius = 180;
      
      // Clear canvas
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      
      // Draw segments
      const colors = ['#FF6B6B', '#4ECDC4', '#FFE66D', '#95E1D3', '#FF8B94'];
      const numSegments = this.prizes.length;
      const anglePerSegment = (2 * Math.PI) / numSegments;
      
      this.prizes.forEach((prize, index) => {
        const startAngle = index * anglePerSegment + this.currentRotation;
        const endAngle = (index + 1) * anglePerSegment + this.currentRotation;
        
        // Draw segment
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, endAngle);
        ctx.closePath();
        ctx.fillStyle = colors[index % colors.length];
        ctx.fill();
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        // Draw text
        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.rotate(startAngle + anglePerSegment / 2);
        ctx.textAlign = 'center';
        ctx.fillStyle = '#fff';
        ctx.font = 'bold 20px Arial';
        ctx.fillText(prize.label, radius * 0.7, 5);
        ctx.restore();
      });
      
      // Draw center circle
      ctx.beginPath();
      ctx.arc(centerX, centerY, 30, 0, 2 * Math.PI);
      ctx.fillStyle = '#fff';
      ctx.fill();
      ctx.strokeStyle = '#333';
      ctx.lineWidth = 3;
      ctx.stroke();
      
      // Draw pointer at top
      ctx.beginPath();
      ctx.moveTo(centerX, 10);
      ctx.lineTo(centerX - 15, 40);
      ctx.lineTo(centerX + 15, 40);
      ctx.closePath();
      ctx.fillStyle = '#FF3838';
      ctx.fill();
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 2;
      ctx.stroke();
    },
    
    async handleSpin() {
      if (!this.status.canSpin || this.isSpinning) return;
      
      this.isSpinning = true;
      
      try {
        const response = await customersApi.spinWheel();
        this.lastResult = response.data;
        
        // Animate spin
        await this.animateSpin(response.data.discountBonus);
        
        // Show result
        this.showResult = true;
        
        // Reload status
        await this.loadData();
        
      } catch (error) {
        console.error('Spin error:', error);
        alert(error.response?.data?.error || 'Lỗi khi quay thưởng');
        this.isSpinning = false;
      }
    },
    
    async animateSpin(resultDiscount) {
      return new Promise(resolve => {
        const duration = 3000; // 3 seconds
        const startTime = Date.now();
        const startRotation = this.currentRotation;
        
        // Find target segment
        const targetIndex = this.prizes.findIndex(p => p.discount == resultDiscount);
        const anglePerSegment = (2 * Math.PI) / this.prizes.length;
        const targetAngle = targetIndex * anglePerSegment;
        
        // Add multiple full rotations for effect
        const totalRotation = (Math.PI * 2 * 5) + targetAngle; // 5 full spins + target
        
        const animate = () => {
          const now = Date.now();
          const elapsed = now - startTime;
          const progress = Math.min(elapsed / duration, 1);
          
          // Easing function for smooth stop
          const easeOut = 1 - Math.pow(1 - progress, 3);
          
          this.currentRotation = startRotation + (totalRotation * easeOut);
          this.drawWheel();
          
          if (progress < 1) {
            requestAnimationFrame(animate);
          } else {
            this.isSpinning = false;
            resolve();
          }
        };
        
        animate();
      });
    },
    
    closeResult() {
      this.showResult = false;
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    
    getHistoryClass(item) {
      if (item.isUsed) return 'used';
      if (new Date(item.expiresAt) < new Date()) return 'expired';
      return 'active';
    },
    
    getHistoryIcon(status) {
      if (status === 'Đã sử dụng') return '✅';
      if (status === 'Đã hết hạn') return '⏱️';
      return '🎁';
    }
  }
};
</script>

<style scoped>
.spin-wheel-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.not-vip-notice {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
}

.not-vip-notice .icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.not-vip-notice h3 {
  font-size: 28px;
  margin-bottom: 12px;
}

.not-vip-notice p {
  font-size: 16px;
  opacity: 0.9;
}

.upgrade-hint {
  margin-top: 20px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  display: inline-block;
}

.vip-spin-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.wheel-header {
  text-align: center;
  margin-bottom: 30px;
}

.wheel-header h2 {
  font-size: 32px;
  color: #2c3e50;
  margin-bottom: 8px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 16px;
}

.active-bonus {
  margin-bottom: 30px;
}

.bonus-badge {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.bonus-icon {
  font-size: 48px;
}

.bonus-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.bonus-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
}

.bonus-expires {
  font-size: 13px;
  opacity: 0.8;
}

.wheel-canvas-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  margin: 40px 0;
}

canvas {
  display: block;
  cursor: pointer;
  transition: transform 0.2s;
}

canvas:hover:not(.disabled) {
  transform: scale(1.02);
}

canvas.spinning {
  cursor: not-allowed;
}

canvas.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spin-button {
  padding: 16px 48px;
  font-size: 20px;
  font-weight: bold;
  color: white;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  border: none;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
}

.spin-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.5);
}

.spin-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.spinning-text {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.already-spun-notice {
  text-align: center;
  color: #7f8c8d;
}

.already-spun-notice p {
  margin: 8px 0;
}

.next-spin-info {
  font-size: 14px;
  color: #95a5a6;
}

/* Result Modal */
.result-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s;
}

.result-content {
  background: white;
  padding: 40px;
  border-radius: 20px;
  text-align: center;
  max-width: 400px;
  animation: scaleIn 0.3s;
}

.result-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: bounce 0.6s;
}

.result-content h3 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 20px;
}

.result-discount {
  font-size: 64px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 16px;
}

.result-message {
  font-size: 18px;
  color: #34495e;
  margin-bottom: 12px;
}

.result-expires {
  font-size: 14px;
  color: #95a5a6;
  margin-bottom: 24px;
}

.close-result-btn {
  padding: 12px 36px;
  font-size: 16px;
  font-weight: bold;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s;
}

.close-result-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* History */
.spin-history {
  margin-top: 40px;
}

.spin-history h3 {
  font-size: 22px;
  color: #2c3e50;
  margin-bottom: 20px;
}

.no-history {
  text-align: center;
  color: #95a5a6;
  padding: 40px;
}

.history-list {
  display: grid;
  gap: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  background: #f8f9fa;
  transition: all 0.2s;
}

.history-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.history-item.active {
  background: linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%);
}

.history-item.used {
  background: #e8f5e9;
}

.history-item.expired {
  background: #ffebee;
  opacity: 0.7;
}

.history-icon {
  font-size: 32px;
}

.history-details {
  flex: 1;
}

.history-discount {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}

.history-date {
  font-size: 13px;
  color: #7f8c8d;
  margin-top: 4px;
}

.history-status {
  font-size: 12px;
  color: #95a5a6;
  margin-top: 4px;
}
</style>