<template>
  <div class="container-xl">
    <el-card shadow="never">
      <!-- Header -->
      <div class="d-flex align-items-end justify-content-between gap-2 flex-wrap">
        <div>
          <div class="kicker">Admin</div>
          <div class="title">🎂 Quản lý sinh nhật khách hàng</div>
          <div class="muted">Theo dõi và chúc mừng khách hàng</div>
        </div>
        <div class="d-flex gap-2">
          <el-button @click="loadData" :loading="loading">
            <el-icon class="me-1"><Refresh /></el-icon>
            Reload
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- Tabs -->
      <el-tabs v-model="viewMode" @tab-change="handleTabChange">
        <!-- Tab 1: Hôm nay -->
        <el-tab-pane label="📅 Hôm nay" name="today">
          <div class="mb-3">
            <el-alert
              v-if="todayBirthdays.length > 0"
              type="success"
              show-icon
              :closable="false"
            >
              <template #title>
                Hôm nay có <strong>{{ todayBirthdays.length }}</strong> khách hàng sinh nhật 🎉
              </template>
            </el-alert>
            <el-alert v-else type="info" show-icon :closable="false">
              <template #title>Hôm nay không có khách hàng nào sinh nhật</template>
            </el-alert>
          </div>

          <el-skeleton v-if="loading" :rows="5" animated />

          <el-table v-else :data="todayBirthdays" border>
            <el-table-column prop="name" label="Tên khách hàng" min-width="180" />
            <el-table-column prop="email" label="Email" min-width="200" />
            <el-table-column prop="phone" label="Số điện thoại" width="140" />
            <el-table-column prop="age" label="Tuổi" width="80" align="center" />
            <el-table-column prop="customerType" label="Loại KH" width="120">
              <template #default="{ row }">
                <el-tag :type="row.customerType === 'VIP' ? 'warning' : 'info'" size="small">
                  {{ row.customerType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="vipTier" label="Hạng VIP" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.vipTier" :type="getTierType(row.vipTier)" size="small">
                  {{ row.vipTier }}
                </el-tag>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="Hành động" width="150" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="sendGreeting(row)">
                  <el-icon class="me-1"><Calendar /></el-icon>
                  Gửi lời chúc
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 2: Theo tháng -->
        <el-tab-pane label="🗓️ Theo tháng" name="monthly">
          <div class="mb-3 d-flex justify-content-between align-items-center">
            <el-select v-model="selectedMonth" @change="loadMonthlyBirthdays" style="width: 200px">
              <el-option
                v-for="month in monthOptions"
                :key="month.value"
                :label="month.label"
                :value="month.value"
              />
            </el-select>

            <el-alert
              v-if="monthlyBirthdays.length > 0"
              type="info"
              show-icon
              :closable="false"
            >
              <template #title>
                Có <strong>{{ monthlyBirthdays.length }}</strong> khách hàng sinh nhật trong tháng này
              </template>
            </el-alert>
          </div>

          <el-skeleton v-if="loading" :rows="5" animated />

          <el-empty v-else-if="monthlyBirthdays.length === 0" description="Không có sinh nhật nào trong tháng này" />

          <el-table v-else :data="monthlyBirthdays" border>
            <el-table-column prop="name" label="Tên khách hàng" min-width="180" />
            <el-table-column prop="email" label="Email" min-width="200" />
            <el-table-column prop="phone" label="Số điện thoại" width="140" />
            <el-table-column label="Ngày sinh" width="180">
              <template #default="{ row }">
                {{ formatBirthdayDate(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="age" label="Tuổi" width="80" align="center" />
            <el-table-column prop="customerType" label="Loại KH" width="120">
              <template #default="{ row }">
                <el-tag :type="row.customerType === 'VIP' ? 'warning' : 'info'" size="small">
                  {{ row.customerType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Hành động" width="150" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="sendGreeting(row)">
                  Gửi lời chúc
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 3: Thống kê -->
        <el-tab-pane label="📊 Thống kê" name="statistics">
          <div class="mb-3">
            <el-alert type="info" show-icon :closable="false">
              <template #title>Thống kê sinh nhật theo tháng trong năm</template>
            </el-alert>
          </div>

          <el-skeleton v-if="loading" :rows="5" animated />

          <el-table v-else :data="statsTableData" border>
            <el-table-column prop="month" label="Tháng" width="120">
              <template #default="{ row }">
                <strong>{{ row.monthLabel }}</strong>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="Số lượng khách hàng" width="200" align="center">
              <template #default="{ row }">
                <el-tag :type="row.count > 0 ? 'success' : 'info'">
                  {{ row.count }} người
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Biểu đồ" min-width="300">
              <template #default="{ row }">
                <div class="progress" style="height: 20px">
                  <div
                    class="progress-bar bg-primary"
                    :style="{ width: getPercentage(row.count) + '%' }"
                  >
                    {{ row.count > 0 ? row.count : '' }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="Hành động" width="150" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.count > 0"
                  type="primary"
                  size="small"
                  @click="viewMonthDetail(row.month)"
                >
                  Xem chi tiết
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 4: Sắp tới (7 ngày) -->
        <el-tab-pane label="⏰ Sắp tới (7 ngày)" name="upcoming">
          <div class="mb-3">
            <el-alert
              v-if="upcomingBirthdays.length > 0"
              type="warning"
              show-icon
              :closable="false"
            >
              <template #title>
                Có <strong>{{ upcomingBirthdays.length }}</strong> khách hàng sắp sinh nhật trong 7 ngày tới
              </template>
            </el-alert>
            <el-alert v-else type="info" show-icon :closable="false">
              <template #title>Không có sinh nhật nào trong 7 ngày tới</template>
            </el-alert>
          </div>

          <el-skeleton v-if="loading" :rows="5" animated />

          <el-table v-else :data="upcomingBirthdays" border>
            <el-table-column prop="name" label="Tên khách hàng" min-width="180" />
            <el-table-column prop="email" label="Email" min-width="200" />
            <el-table-column prop="phone" label="Số điện thoại" width="140" />
            <el-table-column label="Ngày sinh" width="220">
              <template #default="{ row }">
                {{ formatBirthdayDate(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="daysUntilBirthday" label="Còn lại" width="100" align="center" sortable>
              <template #default="{ row }">
                <el-tag :type="row.daysUntilBirthday <= 3 ? 'danger' : 'warning'" size="small">
                  {{ row.daysUntilBirthday }} ngày
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="customerType" label="Loại KH" width="120">
              <template #default="{ row }">
                <el-tag :type="row.customerType === 'VIP' ? 'warning' : 'info'" size="small">
                  {{ row.customerType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Hành động" width="150" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="sendGreeting(row)">
                  Gửi lời chúc
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 5: Lịch sử gửi -->
        <el-tab-pane label="📜 Lịch sử gửi" name="history">
          <div class="mb-3 d-flex justify-content-between align-items-center">
            <el-alert
              v-if="notificationHistory.length > 0"
              type="info"
              show-icon
              :closable="false"
            >
              <template #title>
                Đã gửi <strong>{{ notificationHistory.length }}</strong> thông báo sinh nhật
              </template>
            </el-alert>
            
            <el-button @click="loadNotificationHistory" :loading="historyLoading">
              <el-icon class="me-1"><Refresh /></el-icon>
              Tải lại
            </el-button>
          </div>

          <el-skeleton v-if="historyLoading" :rows="5" animated />

          <el-empty 
            v-else-if="notificationHistory.length === 0" 
            description="Chưa có thông báo nào được gửi" 
          />

          <el-table
            v-else
            :data="notificationHistory"
            border
            default-sort="{prop: 'createdAt', order: 'descending'}"
          >
            <el-table-column prop="customerName" label="Khách hàng" min-width="180" />
            <el-table-column prop="customerEmail" label="Email" min-width="200" />
            <el-table-column prop="title" label="Tiêu đề" min-width="250">
              <template #default="{ row }">
                <div class="d-flex align-items-center gap-2">
                  <span>{{ row.icon }}</span>
                  <span>{{ row.title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="Nội dung" min-width="300">
              <template #default="{ row }">
                <el-popover
                  placement="top"
                  :width="400"
                  trigger="hover"
                >
                  <template #reference>
                    <div class="message-preview">
                      {{ row.message.substring(0, 50) }}{{ row.message.length > 50 ? '...' : '' }}
                    </div>
                  </template>
                  <div class="message-full" v-html="formatMessage(row.message)"></div>
                </el-popover>
              </template>
            </el-table-column>
            <el-table-column prop="isRead" label="Trạng thái" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isRead ? 'success' : 'info'" size="small">
                  {{ row.isRead ? 'Đã đọc' : 'Chưa đọc' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="Thời gian gửi" width="180" sortable>
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="readAt" label="Thời gian đọc" width="180">
              <template #default="{ row }">
                {{ row.readAt ? formatDateTime(row.readAt) : '—' }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Dialog gửi lời chúc -->
    <el-dialog v-model="greetingDialog.open" title="🎂 Gửi lời chúc sinh nhật" width="600px">
      <div v-if="greetingDialog.customer" class="mb-3">
        <el-alert type="info" :closable="false">
          <template #title>
            Gửi lời chúc đến: <strong>{{ greetingDialog.customer.name }}</strong>
            ({{ greetingDialog.customer.email }})
          </template>
        </el-alert>
      </div>

      <el-form label-position="top">
        <el-form-item label="Nội dung lời chúc">
          <el-input
            v-model="greetingDialog.form.message"
            type="textarea"
            :rows="8"
            placeholder="Nhập nội dung lời chúc sinh nhật..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="greetingDialog.open = false">Hủy</el-button>
        <el-button
          type="primary"
          :loading="greetingDialog.sending"
          @click="confirmSendGreeting"
        >
          <el-icon class="me-1"><Calendar /></el-icon>
          Gửi lời chúc
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { Refresh, Calendar } from "@element-plus/icons-vue";
import http from "../../api/http";
import { toast } from "../../ui/toast";

const loading = ref(false);
const historyLoading = ref(false);
const viewMode = ref("today");
const selectedMonth = ref(new Date().getMonth() + 1);
const currentMonth = ref(new Date().getMonth() + 1);

const todayBirthdays = ref([]);
const monthlyBirthdays = ref([]);
const upcomingBirthdays = ref([]);
const monthlyStats = ref({});
const notificationHistory = ref([]);

const monthOptions = [
  { value: 1, label: "Tháng 1" },
  { value: 2, label: "Tháng 2" },
  { value: 3, label: "Tháng 3" },
  { value: 4, label: "Tháng 4" },
  { value: 5, label: "Tháng 5" },
  { value: 6, label: "Tháng 6" },
  { value: 7, label: "Tháng 7" },
  { value: 8, label: "Tháng 8" },
  { value: 9, label: "Tháng 9" },
  { value: 10, label: "Tháng 10" },
  { value: 11, label: "Tháng 11" },
  { value: 12, label: "Tháng 12" },
];

const greetingDialog = reactive({
  open: false,
  customer: null,
  sending: false,
  form: {
    message: "",
  },
});

const statsTableData = computed(() => {
  return monthOptions.map((month) => ({
    month: month.value,
    monthLabel: month.label,
    count: monthlyStats.value[month.value] || 0,
  }));
});

const maxCount = computed(() => {
  return Math.max(...Object.values(monthlyStats.value), 1);
});

function getPercentage(count) {
  return (count / maxCount.value) * 100;
}

function getTierType(tier) {
  const types = {
    BRONZE: 'info',
    SILVER: '',
    GOLD: 'warning',
    PLATINUM: 'danger',
    DIAMOND: 'success'
  };
  return types[tier] || 'info';
}

async function loadData() {
  loading.value = true;
  try {
    await Promise.all([
      loadTodayBirthdays(),
      loadStatistics(),
      loadUpcomingBirthdays(),
    ]);
    if (viewMode.value === "monthly") {
      await loadMonthlyBirthdays();
    }
    if (viewMode.value === "history") {
      await loadNotificationHistory();
    }
  } catch (error) {
    console.error("Load error:", error);
    toast("Không thể tải dữ liệu", "error");
  } finally {
    loading.value = false;
  }
}

async function loadTodayBirthdays() {
  const res = await http.get("/api/auth/admin/birthdays/today");
  todayBirthdays.value = res.data || [];
}

async function loadMonthlyBirthdays() {
  loading.value = true;
  try {
    const res = await http.get(`/api/auth/admin/birthdays/month/${selectedMonth.value}`);
    monthlyBirthdays.value = res.data || [];
  } catch (error) {
    console.error("Load monthly error:", error);
    toast("Không thể tải dữ liệu tháng", "error");
  } finally {
    loading.value = false;
  }
}

async function loadStatistics() {
  const res = await http.get("/api/auth/admin/birthdays/statistics");
  monthlyStats.value = res.data?.monthlyCount || {};
}

async function loadUpcomingBirthdays() {
  const res = await http.get("/api/auth/admin/birthdays/upcoming?days=7");
  upcomingBirthdays.value = res.data || [];
}

async function loadNotificationHistory() {
  historyLoading.value = true;
  try {
    const res = await http.get("/api/auth/admin/birthdays/notification-history");
    notificationHistory.value = res.data || [];
  } catch (error) {
    console.error("Load history error:", error);
    toast("Không thể tải lịch sử thông báo", "error");
  } finally {
    historyLoading.value = false;
  }
}

function handleTabChange(name) {
  if (name === "monthly" && monthlyBirthdays.value.length === 0) {
    loadMonthlyBirthdays();
  }
  if (name === "history" && notificationHistory.value.length === 0) {
    loadNotificationHistory();
  }
}

function viewMonthDetail(month) {
  selectedMonth.value = parseInt(month);
  viewMode.value = "monthly";
  loadMonthlyBirthdays();
}

function sendGreeting(customer) {
  greetingDialog.open = true;
  greetingDialog.customer = customer;
  greetingDialog.form.message = `Chúc mừng sinh nhật ${customer.name}! 🎉

Chúc bạn một tuổi mới tràn đầy sức khỏe, hạnh phúc và thành công. Cảm ơn bạn đã luôn tin tưởng và đồng hành cùng chúng tôi!

🎁 Đặc biệt dành tặng bạn voucher sinh nhật với ưu đãi hấp dẫn!`;
}

async function confirmSendGreeting() {
  if (!greetingDialog.form.message.trim()) {
    toast("Vui lòng nhập nội dung lời chúc", "warning");
    return;
  }

  try {
    greetingDialog.sending = true;
    
    const response = await http.post(
      `/api/auth/admin/birthdays/send-greeting/${greetingDialog.customer.id}`,
      { message: greetingDialog.form.message }
    );

    if (response.data.status === 'success') {
      toast(`Đã gửi lời chúc đến ${greetingDialog.customer.name}! 🎉`, "success");
      greetingDialog.open = false;
      greetingDialog.customer = null;
      greetingDialog.form.message = "";
      
      if (viewMode.value === 'history') {
        loadNotificationHistory();
      }
    } else {
      toast(response.data.message || "Không thể gửi lời chúc", "error");
    }
  } catch (error) {
    console.error("Send greeting error:", error);
    toast(error.response?.data?.message || "Không thể gửi lời chúc", "error");
  } finally {
    greetingDialog.sending = false;
  }
}

function formatBirthdayDate(customer) {
  const months = [
    "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
    "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12",
  ];

  if (customer.isBirthdayToday) {
    return "🎂 Hôm nay!";
  }

  return `${customer.birthDay} ${months[customer.birthMonth - 1]} (còn ${customer.daysUntilBirthday} ngày)`;
}

function formatMessage(message) {
  return message.replace(/\n/g, '<br>');
}

function formatDateTime(dateString) {
  if (!dateString) return '—';
  return new Date(dateString).toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.kicker {
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 4px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.muted {
  color: #909399;
  font-size: 14px;
}

.message-preview {
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

.message-preview:hover {
  color: #409eff;
}

.message-full {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>