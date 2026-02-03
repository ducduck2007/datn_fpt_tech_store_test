<!-- FILE: src/pages/system/BirthdayManager.vue -->
<template>
  <div class="container-xl">
    <el-card shadow="never">
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
          <el-button type="primary" @click="viewMode = 'upcoming'">
            <el-icon class="me-1"><Calendar /></el-icon>
            Sắp tới
          </el-button>
        </div>
      </div>

      <el-divider />

      <!-- Tabs chọn chế độ xem -->
      <el-tabs v-model="viewMode" @tab-change="handleTabChange">
        <el-tab-pane label="📅 Hôm nay" name="today">
          <el-alert
            v-if="todayBirthdays.length > 0"
            type="success"
            show-icon
            :closable="false"
            class="mb-3"
          >
            <template #title>
              <strong>{{ todayBirthdays.length }}</strong> khách hàng có sinh nhật hôm nay!
            </template>
          </el-alert>
          <el-empty v-else description="Không có khách hàng nào sinh nhật hôm nay" />

          <div v-if="todayBirthdays.length > 0" class="row g-3">
            <div
              v-for="customer in todayBirthdays"
              :key="customer.id"
              class="col-12 col-md-6 col-lg-4"
            >
              <CustomerBirthdayCard :customer="customer" @send-greeting="sendGreeting" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="🗓️ Theo tháng" name="monthly">
          <div class="row g-3 mb-3">
            <div class="col-12 col-md-4">
              <el-select
                v-model="selectedMonth"
                placeholder="Chọn tháng"
                @change="loadMonthlyBirthdays"
                class="w-100"
              >
                <el-option
                  v-for="m in monthOptions"
                  :key="m.value"
                  :label="m.label"
                  :value="m.value"
                />
              </el-select>
            </div>
          </div>

          <el-alert
            v-if="monthlyBirthdays.length > 0"
            type="info"
            show-icon
            :closable="false"
            class="mb-3"
          >
            <template #title>
              Tháng {{ selectedMonth }}: <strong>{{ monthlyBirthdays.length }}</strong> khách hàng
            </template>
          </el-alert>

          <el-table
            :data="monthlyBirthdays"
            border
            :loading="loading"
            default-sort="{prop: 'birthDay', order: 'ascending'}"
          >
            <el-table-column prop="birthDay" label="Ngày" width="80" sortable />
            <el-table-column prop="name" label="Tên khách hàng" min-width="180" />
            <el-table-column prop="email" label="Email" min-width="200" />
            <el-table-column prop="phone" label="SĐT" width="140" />
            <el-table-column prop="age" label="Tuổi" width="80" align="center" />
            <el-table-column prop="daysUntilBirthday" label="Còn" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isBirthdayToday" type="success" effect="dark">
                  Hôm nay!
                </el-tag>
                <span v-else>{{ row.daysUntilBirthday }} ngày</span>
              </template>
            </el-table-column>
            <el-table-column prop="customerType" label="Loại KH" width="120">
              <template #default="{ row }">
                <el-tag :type="row.customerType === 'VIP' ? 'warning' : 'info'" effect="light">
                  {{ row.customerType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Hành động" width="120" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="sendGreeting(row)">
                  Chúc mừng
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="📊 Thống kê" name="statistics">
          <div class="row g-3">
            <div
              v-for="(stat, month) in monthlyStats"
              :key="month"
              class="col-6 col-sm-4 col-md-3 col-lg-2"
            >
              <el-card
                shadow="hover"
                :class="{ 'border-primary': month == currentMonth }"
                @click="viewMonthDetail(month)"
                style="cursor: pointer"
              >
                <div class="text-center">
                  <div class="month-name">Tháng {{ month }}</div>
                  <div class="count">{{ stat }}</div>
                  <div class="label">khách hàng</div>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="⏰ Sắp tới (7 ngày)" name="upcoming">
          <el-alert
            v-if="upcomingBirthdays.length > 0"
            type="warning"
            show-icon
            :closable="false"
            class="mb-3"
          >
            <template #title>
              <strong>{{ upcomingBirthdays.length }}</strong> khách hàng sẽ sinh nhật trong 7 ngày tới
            </template>
          </el-alert>

          <el-timeline v-if="upcomingBirthdays.length > 0">
            <el-timeline-item
              v-for="customer in upcomingBirthdays"
              :key="customer.id"
              :timestamp="formatBirthdayDate(customer)"
              placement="top"
              :type="customer.isBirthdayToday ? 'success' : 'primary'"
            >
              <el-card>
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <h4>{{ customer.name }}</h4>
                    <p class="mb-1 text-muted">{{ customer.email }} • {{ customer.phone }}</p>
                    <el-tag
                      size="small"
                      :type="customer.customerType === 'VIP' ? 'warning' : 'info'"
                    >
                      {{ customer.customerType }}
                    </el-tag>
                    <el-tag size="small" class="ms-2">
                      {{ customer.age }} tuổi
                    </el-tag>
                  </div>
                  <div>
                    <el-button type="primary" @click="sendGreeting(customer)">
                      Chúc mừng
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="Không có sinh nhật nào sắp tới" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Dialog gửi lời chúc -->
    <el-dialog v-model="greetingDialog.open" title="🎂 Gửi lời chúc sinh nhật" width="600px">
      <el-form :model="greetingDialog.form" label-position="top">
        <el-form-item label="Khách hàng">
          <el-input :value="greetingDialog.customer?.name" disabled />
        </el-form-item>
        <el-form-item label="Lời chúc">
          <el-input
            v-model="greetingDialog.form.message"
            type="textarea"
            :rows="5"
            placeholder="Nhập lời chúc sinh nhật..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="greetingDialog.open = false">Hủy</el-button>
        <el-button 
          type="primary" 
          @click="confirmSendGreeting"
          :loading="greetingDialog.sending"
        >
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
import { ElMessageBox } from 'element-plus';

const loading = ref(false);
const viewMode = ref("today");
const selectedMonth = ref(new Date().getMonth() + 1);
const currentMonth = new Date().getMonth() + 1;

const todayBirthdays = ref([]);
const monthlyBirthdays = ref([]);
const upcomingBirthdays = ref([]);
const monthlyStats = ref({});

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

function handleTabChange(name) {
  if (name === "monthly" && monthlyBirthdays.value.length === 0) {
    loadMonthlyBirthdays();
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
    "Tháng 1",
    "Tháng 2",
    "Tháng 3",
    "Tháng 4",
    "Tháng 5",
    "Tháng 6",
    "Tháng 7",
    "Tháng 8",
    "Tháng 9",
    "Tháng 10",
    "Tháng 11",
    "Tháng 12",
  ];

  if (customer.isBirthdayToday) {
    return "🎂 Hôm nay!";
  }

  return `${customer.birthDay} ${months[customer.birthMonth - 1]} (còn ${customer.daysUntilBirthday} ngày)`;
}

onMounted(() => {
  loadData();
});
</script>

<script>
// Customer Birthday Card Component
import { defineComponent, h } from "vue";
import { ElCard, ElTag, ElButton, ElIcon } from "element-plus";
import { User, Phone, Message } from "@element-plus/icons-vue";

const CustomerBirthdayCard = defineComponent({
  name: "CustomerBirthdayCard",
  props: {
    customer: {
      type: Object,
      required: true,
    },
  },
  emits: ['send-greeting'],
  setup(props, { emit }) {
    return () =>
      h(
        ElCard,
        { shadow: "hover", class: "h-100 birthday-card" },
        {
          default: () => [
            h("div", { class: "text-center" }, [
              h("div", { class: "birthday-icon" }, "🎂"),
              h("h4", { class: "mt-2 mb-1" }, props.customer.name),
              h("p", { class: "text-muted small mb-2" }, [
                h("strong", {}, `${props.customer.age} tuổi`),
                " • ",
                props.customer.birthdayDisplay,
              ]),
              h("div", { class: "mb-2" }, [
                h(
                  ElTag,
                  {
                    type: props.customer.customerType === "VIP" ? "warning" : "info",
                    size: "small",
                    effect: "light",
                  },
                  () => props.customer.customerType
                ),
              ]),
              h("div", { class: "small text-muted mb-3" }, [
                h("div", {}, props.customer.email),
                h("div", {}, props.customer.phone),
              ]),
              h(
                ElButton,
                {
                  type: "primary",
                  size: "default",
                  onClick: () => emit('send-greeting', props.customer)
                },
                () => "Gửi lời chúc 🎁"
              ),
            ]),
          ],
        }
      );
  },
});

export { CustomerBirthdayCard };
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

.month-name {
  font-weight: 600;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.count {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
}

.label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.border-primary {
  border: 2px solid #409eff;
}

.birthday-card {
  transition: all 0.3s ease;
}

.birthday-card:hover {
  transform: translateY(-4px);
}

.birthday-icon {
  font-size: 48px;
  line-height: 1;
}
</style>