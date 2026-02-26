<!-- 
============================================================
FILE: src/pages/admin/ZeroOrderCustomers.vue
============================================================
-->
<template>
  <div class="zo-page">

    <!-- ═══════════════════════════════════════════════════
         HEADER
    ═══════════════════════════════════════════════════ -->
    <div class="zo-header">
      <div class="zo-header__left">
        <div class="zo-eyebrow">Sales Intelligence</div>
        <h1 class="zo-title">
          <span class="zo-title__icon">🎯</span>
          Khách chưa mua đơn nào
        </h1>
        <p class="zo-subtitle">
          Phát hiện &amp; chuyển đổi Zero-Order Users — những người đã đăng ký nhưng chưa từng mua hàng
        </p>
      </div>
      <div class="zo-header__actions">
        <button class="zo-btn zo-btn--ghost" @click="loadData" :disabled="loading">
          <span>↻</span> Làm mới
        </button>
        <button
          class="zo-btn zo-btn--outline"
          :disabled="filteredCustomers.length === 0"
          @click="selectAll"
        >
          {{ isAllSelected ? '☑ Bỏ chọn tất cả' : '☐ Chọn tất cả' }}
        </button>
        <button
          class="zo-btn zo-btn--primary"
          :disabled="selected.length === 0 || sending"
          @click="openSendDialog"
        >
          <span>📨</span>
          Gửi thông báo
          <span v-if="selected.length > 0" class="zo-btn__badge">{{ selected.length }}</span>
        </button>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════════
         STATS CARDS
    ═══════════════════════════════════════════════════ -->
    <div class="zo-stats">
      <div
        v-for="(s, i) in statCards"
        :key="i"
        class="zo-stat"
        :class="`zo-stat--${s.color}`"
        :style="{ animationDelay: `${i * 70}ms` }"
      >
        <div class="zo-stat__icon">{{ s.icon }}</div>
        <div class="zo-stat__body">
          <div class="zo-stat__value">
            <span v-if="statsLoading" class="zo-skeleton zo-skeleton--num"></span>
            <template v-else>{{ s.value }}</template>
          </div>
          <div class="zo-stat__label">{{ s.label }}</div>
          <div class="zo-stat__hint">{{ s.hint }}</div>
        </div>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════════
         FILTER BAR
    ═══════════════════════════════════════════════════ -->
    <div class="zo-filters">
      <div class="zo-filters__group">
        <span class="zo-filters__label">Đăng ký tối thiểu</span>
        <div class="zo-pills">
          <button
            v-for="opt in dayOptions"
            :key="opt.value"
            class="zo-pill"
            :class="{ active: selectedDays === opt.value }"
            @click="selectDays(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>
      <div class="zo-filters__sep"></div>
      <div class="zo-search-wrap">
        <span class="zo-search-icon">🔍</span>
        <input
          v-model="search"
          class="zo-search"
          placeholder="Tìm tên, email, SĐT..."
          @input="clientPage = 1"
        />
        <button v-if="search" class="zo-search-clear" @click="search = ''">✕</button>
      </div>
      <div class="zo-filters__count">
        <span class="zo-count-num">{{ filteredCustomers.length }}</span>
        <span class="zo-count-label">khách</span>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════════
         TABLE
    ═══════════════════════════════════════════════════ -->
    <div class="zo-table-wrap">

      <!-- Loading skeleton -->
      <div v-if="loading" class="zo-loading">
        <div v-for="i in 6" :key="i" class="zo-row-skeleton" :style="{ animationDelay: `${i*60}ms` }">
          <div class="zo-skeleton zo-skeleton--check"></div>
          <div class="zo-skeleton zo-skeleton--avatar"></div>
          <div class="zo-skeleton zo-skeleton--text"></div>
          <div class="zo-skeleton zo-skeleton--sm"></div>
          <div class="zo-skeleton zo-skeleton--sm"></div>
          <div class="zo-skeleton zo-skeleton--sm"></div>
          <div class="zo-skeleton zo-skeleton--btn"></div>
        </div>
      </div>

      <!-- Empty -->
      <div v-else-if="filteredCustomers.length === 0" class="zo-empty">
        <div class="zo-empty__icon">🎉</div>
        <div class="zo-empty__title">Không có khách nào!</div>
        <div class="zo-empty__sub">Tất cả khách trong khoảng thời gian này đều đã mua hàng.</div>
      </div>

      <!-- Table -->
      <table v-else class="zo-table">
        <thead>
          <tr>
            <th class="th-check">
              <input
                type="checkbox"
                class="zo-checkbox"
                :checked="isAllSelected"
                @change="selectAll"
              />
            </th>
            <th>Khách hàng</th>
            <th>Loại</th>
            <th>Đăng ký lúc</th>
            <th>Ngày chờ</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(c, idx) in pagedCustomers"
            :key="c.id"
            class="zo-row"
            :class="[getRowUrgency(c), { 'zo-row--selected': selected.includes(c.id) }]"
            :style="{ animationDelay: `${idx * 35}ms` }"
          >
            <td class="td-check">
              <input type="checkbox" class="zo-checkbox" :value="c.id" v-model="selected" />
            </td>

            <td class="td-info">
              <div class="zo-customer">
                <div class="zo-avatar" :style="{ background: avatarColor(c.name) }">
                  {{ initials(c.name) }}
                </div>
                <div class="zo-customer__text">
                  <div class="zo-customer__name">{{ c.name }}</div>
                  <div class="zo-customer__email">{{ c.email }}</div>
                  <div class="zo-customer__phone">{{ c.phone || '—' }}</div>
                </div>
              </div>
            </td>

            <td class="td-type">
              <span class="zo-type-badge" :class="c.customerType?.toLowerCase()">
                {{ c.customerType === 'VIP' ? '⭐ VIP' : '👤 Regular' }}
              </span>
            </td>

            <td class="td-date">
              <div class="zo-date">{{ fmtDate(c.createdAt) }}</div>
              <div class="zo-date-rel">{{ fmtRelative(c.createdAt) }}</div>
            </td>

            <td class="td-wait">
              <div class="zo-wait" :class="waitClass(daysSince(c.createdAt))">
                <span class="zo-wait__num">{{ daysSince(c.createdAt) }}</span>
                <span class="zo-wait__unit">ngày</span>
              </div>
            </td>

            <td class="td-status">
              <div class="zo-status">
                <span class="zo-status__dot" :class="statusDotClass(c)"></span>
                <span class="zo-status__text">{{ statusLabel(c) }}</span>
              </div>
            </td>

            <td class="td-actions">
              <button
                class="zo-act-btn zo-act-btn--send"
                :disabled="sendingIds.has(c.id)"
                @click="sendSingle(c)"
                title="Gửi thông báo Welcome"
              >
                <span v-if="sendingIds.has(c.id)" class="zo-spinner"></span>
                <span v-else>📨</span>
                {{ sendingIds.has(c.id) ? 'Đang gửi...' : 'Gửi' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="filteredCustomers.length > pageSize" class="zo-pagination">
        <button class="zo-page-btn" :disabled="clientPage === 1" @click="clientPage--">‹</button>
        <button
          v-for="p in totalPages"
          :key="p"
          class="zo-page-btn"
          :class="{ active: clientPage === p }"
          @click="clientPage = p"
        >{{ p }}</button>
        <button class="zo-page-btn" :disabled="clientPage === totalPages" @click="clientPage++">›</button>
        <span class="zo-page-info">
          {{ (clientPage-1)*pageSize + 1 }}–{{ Math.min(clientPage*pageSize, filteredCustomers.length) }}
          / {{ filteredCustomers.length }}
        </span>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════════
         SEND DIALOG (bulk)
    ═══════════════════════════════════════════════════ -->
    <div v-if="sendDialog" class="zo-overlay" @click.self="sendDialog = false">
      <div class="zo-dialog">
        <div class="zo-dialog__header">
          <div class="zo-dialog__title">📨 Gửi thông báo chào mừng</div>
          <button class="zo-dialog__close" @click="sendDialog = false">✕</button>
        </div>

        <div class="zo-dialog__body">

          <!-- Target summary -->
          <div class="zo-target-summary">
            <div class="zo-target-summary__icon">👥</div>
            <div>
              <div class="zo-target-summary__num">{{ selected.length }}</div>
              <div class="zo-target-summary__label">khách được chọn sẽ nhận thông báo</div>
            </div>
          </div>

          <!-- Template selector -->
          <div class="zo-field">
            <label class="zo-label">Mẫu thông báo</label>
            <div class="zo-template-grid">
              <div
                v-for="tpl in templates"
                :key="tpl.id"
                class="zo-template-card"
                :class="{ active: form.templateId === tpl.id }"
                @click="selectTemplate(tpl)"
              >
                <div class="zo-template-card__icon">{{ tpl.icon }}</div>
                <div class="zo-template-card__name">{{ tpl.name }}</div>
                <div class="zo-template-card__desc">{{ tpl.desc }}</div>
              </div>
            </div>
          </div>

          <!-- Title -->
          <div class="zo-field">
            <label class="zo-label">Tiêu đề thông báo</label>
            <input v-model="form.title" class="zo-input" placeholder="Nhập tiêu đề..." />
          </div>

          <!-- Message -->
          <div class="zo-field">
            <label class="zo-label">Nội dung</label>
            <textarea
              v-model="form.message"
              class="zo-textarea"
              rows="5"
              placeholder="Nhập nội dung thông báo..."
            ></textarea>
            <div class="zo-field__hint">
              Dùng <code>{name}</code> để chèn tên khách hàng tự động
            </div>
          </div>

          <!-- Preview -->
          <div class="zo-preview">
            <div class="zo-preview__label">👁 Xem trước — như khách sẽ thấy</div>
            <div class="zo-preview__card">
              <div class="zo-preview__icon">{{ currentTemplate?.icon || '📢' }}</div>
              <div>
                <div class="zo-preview__title">{{ form.title || '(Chưa có tiêu đề)' }}</div>
                <div class="zo-preview__msg">
                  {{ (form.message || '(Chưa có nội dung)').replace(/\{name\}/g, 'Nguyễn Văn A') }}
                </div>
              </div>
            </div>
          </div>

        </div>

        <div class="zo-dialog__footer">
          <button class="zo-btn zo-btn--ghost" @click="sendDialog = false" :disabled="sending">Hủy</button>
          <button
            class="zo-btn zo-btn--primary"
            :disabled="!form.title || !form.message || sending"
            @click="sendBulk"
          >
            <span v-if="sending" class="zo-spinner"></span>
            <span v-else>📨</span>
            {{ sending
              ? `Đang gửi (${sentCount}/${selected.length})...`
              : `Gửi cho ${selected.length} khách` }}
          </button>
        </div>

        <!-- Progress bar when sending -->
        <div v-if="sending" class="zo-send-progress">
          <div
            class="zo-send-progress__bar"
            :style="{ width: `${Math.round((sentCount / selected.length) * 100)}%` }"
          ></div>
        </div>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════════
         TOAST
    ═══════════════════════════════════════════════════ -->
    <transition name="zo-toast-fade">
      <div v-if="resultToast.show" class="zo-toast" :class="`zo-toast--${resultToast.type}`">
        <span class="zo-toast__icon">{{ resultToast.type === 'success' ? '✅' : '⚠️' }}</span>
        <span class="zo-toast__msg">{{ resultToast.msg }}</span>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { customersApi } from "../../api/customers.api";
import http from "../../api/http";

// ── State ──────────────────────────────────────────────────────────
const loading       = ref(false);
const statsLoading  = ref(false);
const sending       = ref(false);
const sendDialog    = ref(false);
const customers     = ref([]);
const statsData     = ref({});
const selected      = ref([]);
const sendingIds    = ref(new Set());
const sentCount     = ref(0);
const search        = ref("");
const selectedDays  = ref(3);
const clientPage    = ref(1);
const pageSize      = 15;

const resultToast = ref({ show: false, type: "success", msg: "" });

const form = ref({
  templateId: "welcome",
  title: "",
  message: "",
});

// ── Templates ──────────────────────────────────────────────────────
const templates = [
  {
    id: "welcome",
    icon: "🎉",
    name: "Chào mừng",
    desc: "Mời mua lần đầu với ưu đãi",
    title: "🎉 Chào mừng bạn đến với cửa hàng!",
    message: `Xin chào {name}! 👋\n\nCảm ơn bạn đã đăng ký tài khoản tại cửa hàng chúng tôi.\n\n🛍️ Đặt đơn hàng đầu tiên ngay hôm nay để nhận ưu đãi đặc biệt dành cho khách mới!\n\nHãy khám phá ngay các sản phẩm laptop & phụ kiện chất lượng cao nhé! 💻`,
  },
  {
    id: "offer",
    icon: "💰",
    name: "Ưu đãi hấp dẫn",
    desc: "Voucher giảm giá kèm deadline",
    title: "💰 Ưu đãi đặc biệt chỉ dành riêng cho bạn!",
    message: `Xin chào {name}!\n\nChúng tôi nhận thấy bạn chưa thực hiện đơn hàng nào.\n\n🎁 Chúng tôi tặng bạn ưu đãi GIẢM GIÁ ĐẶC BIỆT cho lần mua đầu tiên!\n\n⏰ Ưu đãi có hạn — Đặt hàng ngay để không bỏ lỡ!\n\nKhám phá ngay sản phẩm tại cửa hàng của chúng tôi 🚀`,
  },
  {
    id: "nudge",
    icon: "🤝",
    name: "Hỗ trợ tư vấn",
    desc: "Mời chat với tư vấn viên",
    title: "🤝 Chúng tôi sẵn sàng hỗ trợ bạn!",
    message: `Xin chào {name}!\n\nBạn đang phân vân chưa biết chọn sản phẩm nào?\n\n💬 Đội ngũ tư vấn của chúng tôi luôn sẵn sàng giúp bạn tìm được chiếc laptop phù hợp nhất với nhu cầu và ngân sách.\n\n✅ Tư vấn miễn phí\n✅ Bảo hành chính hãng\n✅ Giao hàng nhanh\n\nLiên hệ ngay với chúng tôi nhé! 📞`,
  },
  {
    id: "custom",
    icon: "✏️",
    name: "Tùy chỉnh",
    desc: "Soạn nội dung riêng",
    title: "",
    message: "",
  },
];

const currentTemplate = computed(() =>
  templates.find((t) => t.id === form.value.templateId)
);

// ── Filter options ─────────────────────────────────────────────────
const dayOptions = [
  { value: 1,  label: "≥ 1 ngày" },
  { value: 3,  label: "≥ 3 ngày" },
  { value: 7,  label: "≥ 7 ngày" },
  { value: 14, label: "≥ 14 ngày" },
  { value: 30, label: "≥ 30 ngày" },
];

// ── Stat cards ─────────────────────────────────────────────────────
const statCards = computed(() => [
  {
    icon: "⏳",
    color: "blue",
    value: statsData.value.registeredOver3Days ?? "—",
    label: "Đăng ký ≥ 3 ngày",
    hint: "Chưa mua bất kỳ đơn nào",
  },
  {
    icon: "📅",
    color: "amber",
    value: statsData.value.registeredOver7Days ?? "—",
    label: "Đăng ký ≥ 7 ngày",
    hint: "Cần hỗ trợ tư vấn",
  },
  {
    icon: "🚨",
    color: "red",
    value: statsData.value.registeredOver30Days ?? "—",
    label: "Đăng ký ≥ 30 ngày",
    hint: "Nguy cơ mất khách cao",
  },
  {
    icon: "✅",
    color: "green",
    value: selected.value.length,
    label: "Đang chọn",
    hint: "Sẵn sàng gửi thông báo",
  },
]);

// ── Computed ───────────────────────────────────────────────────────
const filteredCustomers = computed(() => {
  const kw = search.value.trim().toLowerCase();
  if (!kw) return customers.value;
  return customers.value.filter((c) =>
    `${c.name} ${c.email} ${c.phone ?? ""}`.toLowerCase().includes(kw)
  );
});

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredCustomers.value.length / pageSize))
);

const pagedCustomers = computed(() => {
  const start = (clientPage.value - 1) * pageSize;
  return filteredCustomers.value.slice(start, start + pageSize);
});

const isAllSelected = computed(() =>
  filteredCustomers.value.length > 0 &&
  filteredCustomers.value.every((c) => selected.value.includes(c.id))
);

// ── Load data ──────────────────────────────────────────────────────
async function loadData() {
  loading.value = true;
  selected.value = [];
  try {
    const res = await customersApi.listZeroOrder(selectedDays.value);
    customers.value = Array.isArray(res.data) ? res.data : (res.data?.data ?? []);
  } catch (e) {
    console.error("Failed to load zero-order customers:", e);
    customers.value = [];
  } finally {
    loading.value = false;
  }
}

async function loadStats() {
  statsLoading.value = true;
  try {
    const res = await customersApi.getZeroOrderStats();
    statsData.value = res.data ?? {};
  } catch (e) {
    console.error("Failed to load stats:", e);
  } finally {
    statsLoading.value = false;
  }
}

function selectDays(d) {
  selectedDays.value = d;
  clientPage.value = 1;
  selected.value = [];
  loadData();
}

function selectAll() {
  if (isAllSelected.value) {
    selected.value = [];
  } else {
    selected.value = filteredCustomers.value.map((c) => c.id);
  }
}

// ── Template ───────────────────────────────────────────────────────
function selectTemplate(tpl) {
  form.value.templateId = tpl.id;
  if (tpl.id !== "custom") {
    form.value.title   = tpl.title;
    form.value.message = tpl.message;
  }
}

function openSendDialog() {
  selectTemplate(templates[0]);
  sendDialog.value = true;
}

// ── Send single ────────────────────────────────────────────────────
async function sendSingle(customer) {
  const newSet = new Set(sendingIds.value);
  newSet.add(customer.id);
  sendingIds.value = newSet;
  try {
    await http.post("/api/auth/notifications/send", {
      customerIds: [customer.id],
      title:   `🎉 Xin chào ${customer.name}!`,
      message: templates[0].message.replace(/\{name\}/g, customer.name),
      type:    "WELCOME",
    });
    showToast("success", `✅ Đã gửi thông báo tới ${customer.name}`);
  } catch (e) {
    showToast("error", `Gửi thất bại: ${e?.response?.data?.message ?? e.message}`);
  } finally {
    const s = new Set(sendingIds.value);
    s.delete(customer.id);
    sendingIds.value = s;
  }
}

// ── Send bulk ──────────────────────────────────────────────────────
async function sendBulk() {
  if (!form.value.title || !form.value.message) return;
  sending.value  = true;
  sentCount.value = 0;

  const ids    = [...selected.value];
  const batchSize = 10;
  let success  = 0;
  let fail     = 0;

  for (let i = 0; i < ids.length; i += batchSize) {
    const chunk = ids.slice(i, i + batchSize);
    const chunkCustomers = customers.value.filter((c) => chunk.includes(c.id));

    // Personalize mỗi khách — gửi song song trong batch
    const promises = chunkCustomers.map(async (c) => {
      try {
        await http.post("/api/auth/notifications/send", {
          customerIds: [c.id],
          title:   form.value.title,
          message: form.value.message.replace(/\{name\}/g, c.name),
          type:    "WELCOME",
        });
        success++;
      } catch {
        fail++;
      } finally {
        sentCount.value++;
      }
    });

    await Promise.all(promises);
  }

  sending.value   = false;
  sendDialog.value = false;
  selected.value   = [];

  if (fail === 0) {
    showToast("success", `✅ Đã gửi thành công ${success} thông báo!`);
  } else {
    showToast("error", `⚠️ Thành công: ${success} | Thất bại: ${fail}`);
  }

  await loadData();
}

// ── Toast ──────────────────────────────────────────────────────────
function showToast(type, msg) {
  resultToast.value = { show: true, type, msg };
  setTimeout(() => { resultToast.value.show = false; }, 4500);
}

// ── Helpers ────────────────────────────────────────────────────────
function daysSince(dateStr) {
  if (!dateStr) return 0;
  return Math.floor((Date.now() - new Date(dateStr).getTime()) / 86400000);
}

function fmtDate(d) {
  if (!d) return "—";
  return new Date(d).toLocaleDateString("vi-VN");
}

function fmtRelative(d) {
  if (!d) return "";
  const days = daysSince(d);
  if (days === 0) return "Hôm nay";
  if (days === 1) return "Hôm qua";
  return `${days} ngày trước`;
}

function initials(name) {
  if (!name) return "?";
  return name.trim().split(/\s+/).map((w) => w[0]).slice(-2).join("").toUpperCase();
}

const AVATAR_COLORS = [
  "#6366f1","#8b5cf6","#ec4899","#f59e0b",
  "#10b981","#3b82f6","#ef4444","#14b8a6",
];
function avatarColor(name) {
  if (!name) return AVATAR_COLORS[0];
  let h = 0;
  for (const c of name) h = (h * 31 + c.charCodeAt(0)) & 0xffffffff;
  return AVATAR_COLORS[Math.abs(h) % AVATAR_COLORS.length];
}

function getRowUrgency(c) {
  const d = daysSince(c.createdAt);
  if (d >= 30) return "zo-row--critical";
  if (d >= 7)  return "zo-row--warning";
  return "";
}

function waitClass(days) {
  if (days >= 30) return "zo-wait--critical";
  if (days >= 7)  return "zo-wait--warning";
  return "zo-wait--normal";
}

function statusDotClass(c) {
  const d = daysSince(c.createdAt);
  if (d >= 30) return "dot--red";
  if (d >= 7)  return "dot--amber";
  return "dot--green";
}

function statusLabel(c) {
  const d = daysSince(c.createdAt);
  if (d >= 30) return "Nguy cơ cao 🚨";
  if (d >= 7)  return "Cần tư vấn 📞";
  return "Mới đăng ký 🌱";
}

onMounted(() => {
  loadData();
  loadStats();
});
</script>

<style scoped>
/* ================================================================
   BASE
================================================================ */
.zo-page {
  min-height: 100vh;
  background: #f8f9fc;
  padding: 28px 32px;
  font-family: 'Segoe UI', system-ui, sans-serif;
  color: #1a1d2e;
}

/* ── HEADER ─────────────────────────────────────────────────────── */
.zo-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}
.zo-eyebrow {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .12em;
  text-transform: uppercase;
  color: #6366f1;
  margin-bottom: 6px;
}
.zo-title {
  font-size: 26px;
  font-weight: 900;
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #0f172a;
}
.zo-title__icon { font-size: 28px; }
.zo-subtitle { font-size: 13.5px; color: #64748b; margin: 0; }
.zo-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* ── BUTTONS ────────────────────────────────────────────────────── */
.zo-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border-radius: 9px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all .18s;
  white-space: nowrap;
}
.zo-btn:disabled { opacity: .45; cursor: not-allowed; pointer-events: none; }
.zo-btn--primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  box-shadow: 0 3px 12px rgba(99,102,241,.35);
}
.zo-btn--primary:hover { transform: translateY(-1px); box-shadow: 0 5px 18px rgba(99,102,241,.45); }
.zo-btn--ghost {
  background: #fff;
  color: #475569;
  border: 1.5px solid #e2e8f0;
}
.zo-btn--ghost:hover { background: #f1f5f9; }
.zo-btn--outline {
  background: transparent;
  color: #6366f1;
  border: 1.5px solid #6366f1;
}
.zo-btn--outline:hover { background: #eef2ff; }
.zo-btn__badge {
  background: #fff;
  color: #6366f1;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 800;
  padding: 1px 7px;
  min-width: 22px;
  text-align: center;
}

/* ── STATS ──────────────────────────────────────────────────────── */
.zo-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.zo-stat {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1.5px solid #e8eaf0;
  overflow: hidden;
  animation: zo-fade-up .5s both;
  transition: transform .2s, box-shadow .2s;
}
.zo-stat:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,.07); }
.zo-stat__icon  { font-size: 32px; line-height: 1; flex-shrink: 0; }
.zo-stat__value { font-size: 28px; font-weight: 900; line-height: 1; margin-bottom: 4px; }
.zo-stat__label { font-size: 13px; font-weight: 600; color: #374151; }
.zo-stat__hint  { font-size: 11.5px; color: #94a3b8; margin-top: 2px; }

.zo-stat--blue  { border-left: 4px solid #3b82f6; }
.zo-stat--blue .zo-stat__value  { color: #1d4ed8; }
.zo-stat--amber { border-left: 4px solid #f59e0b; }
.zo-stat--amber .zo-stat__value { color: #b45309; }
.zo-stat--red   { border-left: 4px solid #ef4444; }
.zo-stat--red .zo-stat__value   { color: #b91c1c; }
.zo-stat--green { border-left: 4px solid #10b981; }
.zo-stat--green .zo-stat__value { color: #065f46; }

/* ── FILTERS ────────────────────────────────────────────────────── */
.zo-filters {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border: 1.5px solid #e8eaf0;
  border-radius: 12px;
  padding: 12px 18px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.zo-filters__group  { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.zo-filters__label  {
  font-size: 12px; font-weight: 700; color: #64748b;
  text-transform: uppercase; letter-spacing: .05em; white-space: nowrap;
}
.zo-pills  { display: flex; gap: 6px; flex-wrap: wrap; }
.zo-pill {
  padding: 5px 13px;
  border-radius: 20px;
  font-size: 12.5px;
  font-weight: 600;
  border: 1.5px solid #e2e8f0;
  background: transparent;
  color: #475569;
  cursor: pointer;
  transition: all .15s;
}
.zo-pill:hover  { border-color: #6366f1; color: #6366f1; }
.zo-pill.active { background: #6366f1; border-color: #6366f1; color: #fff; }
.zo-filters__sep { width: 1px; height: 28px; background: #e2e8f0; flex-shrink: 0; }
.zo-search-wrap {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 180px;
  max-width: 300px;
}
.zo-search-icon { position: absolute; left: 11px; font-size: 14px; pointer-events: none; }
.zo-search {
  width: 100%;
  padding: 7px 32px 7px 32px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  color: #1a1d2e;
  outline: none;
  transition: border-color .15s;
  background: #f8fafc;
}
.zo-search:focus { border-color: #6366f1; background: #fff; }
.zo-search-clear {
  position: absolute; right: 9px;
  background: none; border: none; cursor: pointer;
  color: #94a3b8; font-size: 12px;
}
.zo-filters__count {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-left: auto;
}
.zo-count-num   { font-size: 22px; font-weight: 900; color: #6366f1; }
.zo-count-label { font-size: 12px; color: #94a3b8; }

/* ── TABLE ──────────────────────────────────────────────────────── */
.zo-table-wrap {
  background: #fff;
  border: 1.5px solid #e8eaf0;
  border-radius: 14px;
  overflow: hidden;
}
.zo-table { width: 100%; border-collapse: collapse; }
.zo-table thead th {
  background: #f8fafc;
  padding: 12px 14px;
  text-align: left;
  font-size: 11.5px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: .07em;
  color: #64748b;
  border-bottom: 1.5px solid #e8eaf0;
}
.th-check { width: 44px; text-align: center; }

.zo-row {
  transition: background .15s;
  animation: zo-row-in .3s both;
  border-bottom: 1px solid #f1f5f9;
}
.zo-row:last-child { border-bottom: none; }
.zo-row:hover     { background: #fafbff; }
.zo-row--selected { background: #eef2ff !important; }
.zo-row--warning  { border-left: 3px solid #f59e0b; }
.zo-row--critical { border-left: 3px solid #ef4444; }

.zo-table td { padding: 13px 14px; vertical-align: middle; }
.td-check    { text-align: center; }

/* Customer */
.zo-customer { display: flex; align-items: center; gap: 11px; }
.zo-avatar {
  width: 38px; height: 38px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 800; color: #fff; flex-shrink: 0;
}
.zo-customer__name  { font-size: 13.5px; font-weight: 700; color: #0f172a; }
.zo-customer__email { font-size: 12px; color: #64748b; }
.zo-customer__phone { font-size: 11.5px; color: #94a3b8; }

/* Type badge */
.zo-type-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
}
.zo-type-badge.vip     { background: #fef3c7; color: #92400e; }
.zo-type-badge.regular { background: #f1f5f9; color: #475569; }

/* Date */
.zo-date     { font-size: 13px; font-weight: 600; color: #374151; }
.zo-date-rel { font-size: 11.5px; color: #94a3b8; }

/* Wait */
.zo-wait {
  display: inline-flex;
  align-items: baseline;
  gap: 3px;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 800;
}
.zo-wait__num  { font-size: 16px; }
.zo-wait__unit { font-size: 11px; }
.zo-wait--normal   { background: #dcfce7; color: #166534; }
.zo-wait--warning  { background: #fef9c3; color: #854d0e; }
.zo-wait--critical { background: #fee2e2; color: #991b1b; }

/* Status */
.zo-status { display: flex; align-items: center; gap: 7px; }
.zo-status__dot {
  width: 8px; height: 8px; border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
  flex-shrink: 0;
}
.dot--green { background: #10b981; }
.dot--amber { background: #f59e0b; }
.dot--red   { background: #ef4444; }
.zo-status__text { font-size: 12.5px; font-weight: 600; color: #374151; }

/* Action btn */
.zo-act-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  border: none;
  transition: all .15s;
}
.zo-act-btn--send {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
}
.zo-act-btn--send:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(99,102,241,.4);
}
.zo-act-btn:disabled { opacity: .5; cursor: not-allowed; }

.zo-checkbox { width: 16px; height: 16px; cursor: pointer; accent-color: #6366f1; }

/* ── PAGINATION ─────────────────────────────────────────────────── */
.zo-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px;
  border-top: 1px solid #f1f5f9;
  flex-wrap: wrap;
}
.zo-page-btn {
  min-width: 34px; height: 34px;
  border-radius: 8px;
  border: 1.5px solid #e2e8f0;
  background: transparent;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;
  padding: 0 8px;
}
.zo-page-btn:hover:not(:disabled) { border-color: #6366f1; color: #6366f1; }
.zo-page-btn.active { background: #6366f1; border-color: #6366f1; color: #fff; }
.zo-page-btn:disabled { opacity: .4; cursor: not-allowed; }
.zo-page-info { font-size: 12.5px; color: #94a3b8; margin-left: 8px; }

/* ── EMPTY / LOADING ────────────────────────────────────────────── */
.zo-empty { text-align: center; padding: 64px 24px; }
.zo-empty__icon  { font-size: 56px; margin-bottom: 16px; }
.zo-empty__title { font-size: 18px; font-weight: 800; color: #0f172a; margin-bottom: 8px; }
.zo-empty__sub   { font-size: 14px; color: #64748b; }

.zo-loading { padding: 16px; }
.zo-row-skeleton {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
  animation: zo-fade-up .5s both;
}
.zo-skeleton {
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
  flex-shrink: 0;
}
.zo-skeleton--num    { width: 60px; height: 32px; }
.zo-skeleton--check  { width: 16px; height: 16px; border-radius: 4px; }
.zo-skeleton--avatar { width: 38px; height: 38px; border-radius: 50%; }
.zo-skeleton--text   { width: 160px; height: 14px; }
.zo-skeleton--sm     { width: 80px; height: 14px; }
.zo-skeleton--btn    { width: 70px; height: 30px; }

/* ── DIALOG ─────────────────────────────────────────────────────── */
.zo-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15,23,42,.55);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: zo-overlay-in .2s;
}
.zo-dialog {
  background: #fff;
  border-radius: 18px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 60px rgba(0,0,0,.25);
  animation: zo-dialog-in .25s cubic-bezier(.34,1.56,.64,1);
  position: relative;
}
.zo-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 24px 18px;
  border-bottom: 1.5px solid #f1f5f9;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
}
.zo-dialog__title { font-size: 18px; font-weight: 800; color: #0f172a; }
.zo-dialog__close {
  background: none; border: none; font-size: 18px; cursor: pointer;
  color: #94a3b8; padding: 4px 8px; border-radius: 6px; transition: background .15s;
}
.zo-dialog__close:hover { background: #f1f5f9; color: #475569; }
.zo-dialog__body {
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.zo-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1.5px solid #f1f5f9;
  position: sticky;
  bottom: 0;
  background: #fff;
}

/* Target summary */
.zo-target-summary {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #eef2ff;
  border: 1.5px solid #c7d2fe;
  border-radius: 12px;
  padding: 14px 18px;
}
.zo-target-summary__icon  { font-size: 28px; }
.zo-target-summary__num   { font-size: 26px; font-weight: 900; color: #4338ca; }
.zo-target-summary__label { font-size: 13px; color: #6366f1; }

/* Template grid */
.zo-template-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 8px;
}
.zo-template-card {
  border: 2px solid #e2e8f0;
  border-radius: 11px;
  padding: 13px 15px;
  cursor: pointer;
  transition: all .15s;
  background: #f8fafc;
}
.zo-template-card:hover  { border-color: #a5b4fc; background: #f5f3ff; }
.zo-template-card.active { border-color: #6366f1; background: #eef2ff; }
.zo-template-card__icon { font-size: 22px; margin-bottom: 5px; }
.zo-template-card__name { font-size: 13px; font-weight: 700; color: #1e293b; }
.zo-template-card__desc { font-size: 11.5px; color: #64748b; margin-top: 2px; }

/* Form */
.zo-field { display: flex; flex-direction: column; gap: 6px; }
.zo-label {
  font-size: 12.5px; font-weight: 700; color: #374151;
  text-transform: uppercase; letter-spacing: .05em;
}
.zo-input, .zo-textarea {
  width: 100%;
  padding: 10px 13px;
  border: 1.5px solid #e2e8f0;
  border-radius: 9px;
  font-size: 13.5px;
  color: #1a1d2e;
  outline: none;
  transition: border-color .15s;
  font-family: inherit;
  resize: vertical;
  background: #f8fafc;
  box-sizing: border-box;
}
.zo-input:focus, .zo-textarea:focus { border-color: #6366f1; background: #fff; }
.zo-field__hint { font-size: 11.5px; color: #94a3b8; }
.zo-field__hint code {
  background: #f1f5f9;
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 11px;
  color: #6366f1;
}

/* Preview */
.zo-preview {
  background: #f8fafc;
  border: 1.5px dashed #c7d2fe;
  border-radius: 12px;
  padding: 14px 16px;
}
.zo-preview__label {
  font-size: 11px; font-weight: 800; text-transform: uppercase;
  letter-spacing: .1em; color: #94a3b8; margin-bottom: 10px;
}
.zo-preview__card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
}
.zo-preview__icon  { font-size: 24px; flex-shrink: 0; }
.zo-preview__title { font-size: 13.5px; font-weight: 700; color: #0f172a; margin-bottom: 4px; }
.zo-preview__msg   { font-size: 12.5px; color: #64748b; white-space: pre-line; line-height: 1.5; }

/* Send progress */
.zo-send-progress { height: 4px; background: #e2e8f0; overflow: hidden; }
.zo-send-progress__bar {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: width .4s ease;
}

/* ── TOAST ──────────────────────────────────────────────────────── */
.zo-toast {
  position: fixed;
  bottom: 32px;
  right: 32px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 8px 30px rgba(0,0,0,.18);
  z-index: 9999;
  max-width: 380px;
}
.zo-toast--success { background: #dcfce7; color: #166534; border: 1.5px solid #bbf7d0; }
.zo-toast--error   { background: #fee2e2; color: #991b1b; border: 1.5px solid #fecaca; }
.zo-toast-fade-enter-active, .zo-toast-fade-leave-active { transition: all .35s; }
.zo-toast-fade-enter-from, .zo-toast-fade-leave-to { opacity: 0; transform: translateY(12px) scale(.96); }

/* ── SPINNER ────────────────────────────────────────────────────── */
.zo-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
  flex-shrink: 0;
}

/* ── ANIMATIONS ─────────────────────────────────────────────────── */
@keyframes zo-fade-up {
  from { opacity: 0; transform: translateY(14px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes zo-row-in {
  from { opacity: 0; transform: translateX(-8px); }
  to   { opacity: 1; transform: translateX(0); }
}
@keyframes zo-overlay-in { from { opacity: 0; } to { opacity: 1; } }
@keyframes zo-dialog-in {
  from { opacity: 0; transform: scale(.93) translateY(20px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}
@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: .5; transform: scale(.8); }
}
</style>