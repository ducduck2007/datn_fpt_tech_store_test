<template>
  <div style="max-width: 600px; margin: 80px auto; text-align: center; padding: 0 20px;">
    <el-card shadow="never" style="border-radius: 16px; padding: 20px;">
      <div v-if="status === 'success'">
        <el-icon :size="80" color="#67C23A"><CircleCheckFilled /></el-icon>
        <h2 style="margin-top: 20px; font-size: 24px;">Thanh toán thành công!</h2>
        <p style="color: #666; margin-bottom: 30px;">Cảm ơn bạn. Đơn hàng của bạn đã được thanh toán và đang chờ xử lý.</p>
      </div>
      
      <div v-else>
        <el-icon :size="80" color="#F56C6C"><CircleCloseFilled /></el-icon>
        <h2 style="margin-top: 20px; font-size: 24px;">Thanh toán thất bại</h2>
        <p style="color: #666; margin-bottom: 30px;">Giao dịch không thành công hoặc đã bị hủy. Vui lòng thử lại.</p>
      </div>

      <el-descriptions :column="1" border size="small" style="margin-bottom: 30px;">
        <el-descriptions-item label="Mã đơn hàng">{{ resultData.vnp_TxnRef }}</el-descriptions-item>
        <el-descriptions-item label="Số tiền">{{ formatMoney(resultData.vnp_Amount / 100) }}</el-descriptions-item>
        <el-descriptions-item label="Nội dung">{{ resultData.vnp_OrderInfo }}</el-descriptions-item>
      </el-descriptions>

      <el-space>
        <el-button type="primary" @click="$router.push('/')">Về trang chủ</el-button>
        <el-button plain @click="$router.push('/my-orders')">Xem đơn hàng</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue';

const route = useRoute();
const status = ref('error');
const resultData = ref({});

function formatMoney(val) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(val);
}

onMounted(() => {
  resultData.value = route.query;
  // vnp_ResponseCode = 00 là mã thành công của VNPay
  if (route.query.vnp_ResponseCode === '00') {
    status.value = 'success';
  }
});
</script>