<template>
  <div class="order-dashboard">

    <!-- ══ HEADER ══ -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Order Dashboard</h1>
        <p class="page-sub">Tổng quan tình trạng đơn hàng theo thời gian thực</p>
      </div>
      <div class="header-actions">
        <el-select v-model="period" @change="fetchAll" class="period-select-el">
          <template #prefix>
            <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
              <rect x="1" y="2" width="11" height="10" rx="2" stroke="currentColor" stroke-width="1.3"/>
              <path d="M1 5.5h11M4 1v2M9 1v2" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
          </template>
          <el-option label="7 ngày qua"  value="7"  />
          <el-option label="30 ngày qua" value="30" />
          <el-option label="90 ngày qua" value="90" />
        </el-select>
        <button class="refresh-btn" @click="fetchAll" :disabled="loading">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" :class="{spin: loading}">
            <path d="M12.5 7A5.5 5.5 0 1 1 6.5 1.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M12.5 1.5v3.5H9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Refresh
        </button>
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon style="margin-bottom:16px" />

    <!-- ══ KPI CARDS ══ -->
    <div class="kpi-grid" v-loading="loading">
      <div class="kpi-card blue">
        <div class="kpi-icon"><IconBox /></div>
        <div class="kpi-body">
          <span class="kpi-label">Tổng đơn</span>
          <span class="kpi-value">{{ kpi.total.toLocaleString("vi-VN") }}</span>
        </div>
      </div>
      <div class="kpi-card green">
        <div class="kpi-icon"><IconCheck /></div>
        <div class="kpi-body">
          <span class="kpi-label">Hoàn thành</span>
          <span class="kpi-value">{{ kpi.completed.toLocaleString("vi-VN") }}</span>
        </div>
      </div>
      <div class="kpi-card red">
        <div class="kpi-icon"><IconX /></div>
        <div class="kpi-body">
          <span class="kpi-label">Huỷ</span>
          <span class="kpi-value">{{ kpi.cancelled.toLocaleString("vi-VN") }}</span>
        </div>
      </div>
      <div class="kpi-card purple">
        <div class="kpi-icon"><IconMoney /></div>
        <div class="kpi-body">
          <span class="kpi-label">Doanh thu</span>
          <span class="kpi-value">{{ formatVnd(kpi.revenue) }}</span>
        </div>
      </div>
    </div>

    <!-- ══ TRẠNG THÁI + KÊNH BÁN ══ -->
    <div class="section-row" v-loading="loading">

      <!-- Trạng thái đơn — card lưới -->
      <div class="breakdown-card">
        <div class="breakdown-header">
          <span class="breakdown-title">Theo trạng thái</span>
          <span class="breakdown-total">{{ kpi.total }} đơn</span>
        </div>
        <div class="status-grid">
          <div
            v-for="s in statusCards"
            :key="s.key"
            class="status-card"
            :style="{'--sc': s.color, '--sc-bg': s.bg}"
          >
            <div class="sc-dot"></div>
            <div class="sc-body">
              <span class="sc-label">{{ s.label }}</span>
              <span class="sc-count">{{ s.count }}</span>
            </div>
            <div class="sc-pct">{{ s.pct }}%</div>
          </div>
        </div>
      </div>

      <!-- Kênh bán — 2 card + mini bar -->
      <div class="breakdown-card">
        <div class="breakdown-header">
          <span class="breakdown-title">Theo kênh bán</span>
          <span class="breakdown-total">{{ kpi.total }} đơn</span>
        </div>
        <div class="channel-grid">
          <div
            v-for="c in channelCards"
            :key="c.key"
            class="channel-card"
            :style="{'--cc': c.color, '--cc-bg': c.bg}"
          >
            <div class="cc-header">
              <span class="cc-icon-wrap"><component :is="c.icon" /></span>
              <span class="cc-label">{{ c.label }}</span>
            </div>
            <span class="cc-count">{{ c.count }}</span>
            <span class="cc-revenue">{{ formatVnd(c.revenue) }}</span>
            <div class="cc-bar-track">
              <div class="cc-bar-fill" :style="{width: c.pct + '%'}"></div>
            </div>
            <span class="cc-pct">{{ c.pct }}% tổng đơn</span>
          </div>
        </div>

        <!-- Tỉ lệ hoàn thành / huỷ theo kênh -->
        <div class="channel-rate-table">
          <div class="crt-head">
            <span>Kênh</span><span>Hoàn thành</span><span>Huỷ</span><span>Tỉ lệ HT</span>
          </div>
          <div v-for="c in channelRates" :key="c.channel" class="crt-row">
            <span class="crt-channel">{{ c.channel }}</span>
            <span class="crt-done">{{ c.done }}</span>
            <span class="crt-cancel">{{ c.cancel }}</span>
            <span class="crt-rate" :class="c.rate >= 50 ? 'good' : 'bad'">{{ c.rate }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ══ CHARTS ROW 1: Đơn theo ngày + Online vs Offline ══ -->
    <div class="charts-2col" v-loading="loading">
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">Đơn hàng theo ngày</span>
          <div class="legend">
            <span class="dot blue"></span>Mới
            <span class="dot green"></span>Hoàn thành
            <span class="dot red"></span>Huỷ
          </div>
        </div>
        <div class="canvas-wrap"><canvas ref="lineCanvas"></canvas></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">Online vs Offline theo ngày</span>
          <div class="legend">
            <span class="dot blue"></span>Online
            <span class="dot indigo"></span>Offline
          </div>
        </div>
        <div class="canvas-wrap"><canvas ref="channelCanvas"></canvas></div>
      </div>
    </div>

    <!-- ══ CHARTS ROW 2: Doanh thu + AOV ══ -->
    <div class="charts-2col" v-loading="loading">
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">Doanh thu theo ngày</span>
        </div>
        <div class="canvas-wrap"><canvas ref="barCanvas"></canvas></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">Giá trị trung bình đơn (AOV)</span>
        </div>
        <div class="canvas-wrap"><canvas ref="aovCanvas"></canvas></div>
      </div>
    </div>

    <!-- ══ BOTTOM: Top 5 khách hàng + Đơn gần đây ══ -->
    <div class="charts-2col" v-loading="loading">
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">Top 5 khách hàng theo doanh thu</span>
        </div>
        <div class="canvas-wrap"><canvas ref="topCustCanvas"></canvas></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">Đơn gần đây</span>
          <router-link to="/system/orders/filter" class="view-all">Xem tất cả →</router-link>
        </div>
        <el-table :data="recentOrders" size="small" style="width:100%">
          <el-table-column prop="id" label="Mã" width="95" />
          <el-table-column label="Khách hàng" min-width="100">
            <template #default="{row}">
              {{ row.customerName || row.customer?.name || row.customer?.fullName || "—" }}
            </template>
          </el-table-column>
          <el-table-column label="Kênh" width="80">
            <template #default="{row}">
              <span class="channel-tag" :class="(row.channel||'').toLowerCase()">
                {{ row.channel || "—" }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="Tổng" width="105">
            <template #default="{row}">{{ formatVnd(row.totalAmount ?? row.total ?? 0) }}</template>
          </el-table-column>
          <el-table-column label="TT" width="105">
            <template #default="{row}">
              <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, h } from "vue";
import { ordersApi } from "../../api/orders.api";

// ── period / state ────────────────────────────────────────────────────────
const period  = ref("30");
const loading = ref(false);
const error   = ref("");

const kpi          = ref({ total:0, completed:0, cancelled:0, revenue:0 });
const statusCards  = ref([]);
const channelCards = ref([]);
const channelRates = ref([]);
const recentOrders = ref([]);

// ── canvas refs ───────────────────────────────────────────────────────────
const lineCanvas    = ref(null);
const channelCanvas = ref(null);
const barCanvas     = ref(null);
const aovCanvas     = ref(null);
const topCustCanvas = ref(null);
let charts = {};

// ── status config ─────────────────────────────────────────────────────────
const STATUS_META = [
  { key:"NEW",        label:"Mới",           color:"#f59e0b", bg:"rgba(245,158,11,.1)"  },
  { key:"PENDING",    label:"Chờ xử lý",     color:"#f97316", bg:"rgba(249,115,22,.1)"  },
  { key:"PROCESSING", label:"Đang xử lý",    color:"#3b82f6", bg:"rgba(59,130,246,.1)"  },
  { key:"SHIPPING",   label:"Đang giao",     color:"#8b5cf6", bg:"rgba(139,92,246,.1)"  },
  { key:"SHIPPED",    label:"Đã giao hàng",  color:"#06b6d4", bg:"rgba(6,182,212,.1)"   },
  { key:"DELIVERED",  label:"Hoàn thành",    color:"#10b981", bg:"rgba(16,185,129,.1)"  },
  { key:"COMPLETED",  label:"Đã hoàn tất",   color:"#059669", bg:"rgba(5,150,105,.1)"   },
  { key:"CANCELLED",  label:"Đã huỷ",        color:"#ef4444", bg:"rgba(239,68,68,.1)"   },
];
const STATUS_COLOR = Object.fromEntries(STATUS_META.map(s => [s.key, s.color]));

function isDone(s)      { return ["DELIVERED","COMPLETED"].includes((s||"").toUpperCase()); }
function isCancelled(s) { return (s||"").toUpperCase() === "CANCELLED"; }

// ── date helpers ──────────────────────────────────────────────────────────
function toISO(d) { return d.toISOString().slice(0,10); }
function buildDateRange(days) {
  const to = new Date(), from = new Date();
  from.setDate(from.getDate() - (days-1));
  return { from: toISO(from), to: toISO(to) };
}
function fmtDay(s) { const d=new Date(s); return `${d.getDate()}/${d.getMonth()+1}`; }

// ── process orders ────────────────────────────────────────────────────────
function processOrders(orders, from, to) {
  const total     = orders.length;
  const completed = orders.filter(o => isDone(o.status)).length;
  const cancelled = orders.filter(o => isCancelled(o.status)).length;
  const revenue   = orders.filter(o => !isCancelled(o.status))
                          .reduce((s,o) => s+(o.totalAmount??o.total??0), 0);
  kpi.value = { total, completed, cancelled, revenue };

  // ── Status cards ──
  const statusMap = {};
  for (const o of orders) {
    const k = (o.status||"UNKNOWN").toUpperCase();
    statusMap[k] = (statusMap[k]||0)+1;
  }
  statusCards.value = STATUS_META
    .filter(m => statusMap[m.key])
    .map(m => ({
      ...m,
      count: statusMap[m.key]||0,
      pct: total ? Math.round(((statusMap[m.key]||0)/total)*100) : 0,
    }));

  // ── Channel cards ──
  const ch = { ONLINE:{count:0,revenue:0}, OFFLINE:{count:0,revenue:0} };
  for (const o of orders) {
    const c = (o.channel||"ONLINE").toUpperCase();
    if (!ch[c]) ch[c] = {count:0,revenue:0};
    ch[c].count++;
    if (!isCancelled(o.status)) ch[c].revenue += (o.totalAmount??o.total??0);
  }
  channelCards.value = [
    { key:"ONLINE",  label:"Online",  color:"#3b82f6", bg:"rgba(59,130,246,.08)", icon:IconOnline,
      count:ch.ONLINE.count, revenue:ch.ONLINE.revenue,
      pct: total ? Math.round((ch.ONLINE.count/total)*100) : 0 },
    { key:"OFFLINE", label:"Offline", color:"#6366f1", bg:"rgba(99,102,241,.08)", icon:IconOffline,
      count:ch.OFFLINE.count, revenue:ch.OFFLINE.revenue,
      pct: total ? Math.round((ch.OFFLINE.count/total)*100) : 0 },
  ];

  // ── Channel rates ──
  channelRates.value = ["ONLINE","OFFLINE"].map(c => {
    const sub = orders.filter(o => (o.channel||"ONLINE").toUpperCase()===c);
    const done   = sub.filter(o => isDone(o.status)).length;
    const cancel = sub.filter(o => isCancelled(o.status)).length;
    return { channel:c, done, cancel, rate: sub.length ? Math.round((done/sub.length)*100) : 0 };
  });

  // ── Recent orders ──
  recentOrders.value = [...orders]
    .sort((a,b) => new Date(b.createdAt||b.orderDate||0)-new Date(a.createdAt||a.orderDate||0))
    .slice(0,8);

  // ── Build day buckets ──
  const days=[], cur=new Date(from), end=new Date(to);
  while (cur<=end) { days.push(toISO(new Date(cur))); cur.setDate(cur.getDate()+1); }

  const byDay={};
  for (const d of days) byDay[d]={new:0,completed:0,cancelled:0,revenue:0,online:0,offline:0,aovRevenue:0,aovCount:0};
  for (const o of orders) {
    const d=(o.createdAt||o.orderDate||"").slice(0,10);
    if (!byDay[d]) continue;
    byDay[d].new++;
    const ch2=(o.channel||"ONLINE").toUpperCase();
    if (ch2==="ONLINE") byDay[d].online++; else byDay[d].offline++;
    if (isDone(o.status))      { byDay[d].completed++; byDay[d].aovRevenue+=(o.totalAmount??o.total??0); byDay[d].aovCount++; }
    if (isCancelled(o.status)) byDay[d].cancelled++;
    if (!isCancelled(o.status)) byDay[d].revenue+=(o.totalAmount??o.total??0);
  }

  // Thin x-axis
  const maxT=18, step=days.length>maxT?Math.ceil(days.length/maxT):1;
  const sparse=days.filter((_,i)=>i%step===0||i===days.length-1);

  // Top 5 customers
  const custRev={};
  for (const o of orders) {
    if (isCancelled(o.status)) continue;
    const name=o.customerName||o.customer?.name||o.customer?.fullName||"Unknown";
    custRev[name]=(custRev[name]||0)+(o.totalAmount??o.total??0);
  }
  const top5=Object.entries(custRev).sort((a,b)=>b[1]-a[1]).slice(0,5);

  return {
    labels:    sparse.map(fmtDay),
    lineNew:       sparse.map(d=>byDay[d].new),
    lineCompleted: sparse.map(d=>byDay[d].completed),
    lineCancelled: sparse.map(d=>byDay[d].cancelled),
    barRevenue:    sparse.map(d=>byDay[d].revenue),
    chOnline:      sparse.map(d=>byDay[d].online),
    chOffline:     sparse.map(d=>byDay[d].offline),
    aov:           sparse.map(d=>byDay[d].aovCount?Math.round(byDay[d].aovRevenue/byDay[d].aovCount):0),
    top5Labels:    top5.map(x=>x[0]),
    top5Values:    top5.map(x=>x[1]),
  };
}

// ── build charts ──────────────────────────────────────────────────────────
async function buildCharts(cd) {
  Object.values(charts).forEach(c=>c?.destroy());
  charts={};

  let Chart, registerables;
  try   { ({Chart,registerables}=await import("chart.js")); }
  catch { ({Chart,registerables}=await import("https://cdn.jsdelivr.net/npm/chart.js@4/+esm")); }
  Chart.register(...registerables);

  const grid="#00000010", tick={size:11};
  const opts=(extra={})=>({
    responsive:true, maintainAspectRatio:false, animation:false,
    plugins:{legend:{display:false}},
    ...extra,
  });

  // 1. Line — orders per day
  charts.line = new Chart(lineCanvas.value, {
    type:"line",
    data:{ labels:cd.labels, datasets:[
      {label:"Mới",       data:cd.lineNew,       borderColor:"#3b82f6",bg:"rgba(59,130,246,.08)", tension:.35,fill:true,pointRadius:0,pointHitRadius:10,borderWidth:2},
      {label:"Hoàn thành",data:cd.lineCompleted, borderColor:"#10b981",bg:"rgba(16,185,129,.07)", tension:.35,fill:true,pointRadius:0,pointHitRadius:10,borderWidth:2},
      {label:"Huỷ",       data:cd.lineCancelled, borderColor:"#ef4444",bg:"rgba(239,68,68,.06)",  tension:.35,fill:true,pointRadius:0,pointHitRadius:10,borderWidth:2},
    ].map(d=>({...d,backgroundColor:d.bg}))},
    options:opts({scales:{x:{grid:{color:grid},ticks:{font:tick,maxRotation:0}},y:{grid:{color:grid},ticks:{font:tick},beginAtZero:true}}}),
  });

  // 2. Grouped bar — online vs offline
  charts.channel = new Chart(channelCanvas.value, {
    type:"bar",
    data:{ labels:cd.labels, datasets:[
      {label:"Online",  data:cd.chOnline,  backgroundColor:"#3b82f6", borderRadius:3, borderSkipped:false},
      {label:"Offline", data:cd.chOffline, backgroundColor:"#6366f1", borderRadius:3, borderSkipped:false},
    ]},
    options:opts({scales:{x:{grid:{display:false},ticks:{font:tick,maxRotation:0}},y:{grid:{color:grid},ticks:{font:tick},beginAtZero:true}}}),
  });

  // 3. Bar — revenue
  charts.bar = new Chart(barCanvas.value, {
    type:"bar",
    data:{ labels:cd.labels, datasets:[{label:"Doanh thu",data:cd.barRevenue,backgroundColor:"#3b82f6",hoverBackgroundColor:"#2563eb",borderRadius:4,borderSkipped:false}]},
    options:opts({scales:{x:{grid:{display:false},ticks:{font:tick,maxRotation:0}},y:{grid:{color:grid},ticks:{font:tick,callback:v=>formatM(v)},beginAtZero:true}}}),
  });

  // 4. Line — AOV
  charts.aov = new Chart(aovCanvas.value, {
    type:"line",
    data:{ labels:cd.labels, datasets:[{label:"AOV",data:cd.aov,borderColor:"#f59e0b",backgroundColor:"rgba(245,158,11,.08)",tension:.35,fill:true,pointRadius:0,pointHitRadius:10,borderWidth:2}]},
    options:opts({scales:{x:{grid:{color:grid},ticks:{font:tick,maxRotation:0}},y:{grid:{color:grid},ticks:{font:tick,callback:v=>formatM(v)},beginAtZero:true}}}),
  });

  // 5. Horizontal bar — top 5 customers
  charts.topCust = new Chart(topCustCanvas.value, {
    type:"bar",
    data:{
      labels:cd.top5Labels,
      datasets:[{label:"Doanh thu",data:cd.top5Values,
        backgroundColor:["#3b82f6","#6366f1","#8b5cf6","#ec4899","#f59e0b"],
        borderRadius:5,borderSkipped:false}],
    },
    options:opts({
      indexAxis:"y",
      scales:{
        x:{grid:{color:grid},ticks:{font:tick,callback:v=>formatM(v)},beginAtZero:true},
        y:{grid:{display:false},ticks:{font:{size:12}}},
      },
    }),
  });
}

// ── fetch ─────────────────────────────────────────────────────────────────
async function fetchAll() {
  loading.value = true;
  error.value   = "";
  try {
    const {from,to} = buildDateRange(Number(period.value));
    const res    = await ordersApi.filter(undefined, from+"T00:00:00Z", to+"T23:59:59Z", undefined, undefined);
    const orders = Array.isArray(res) ? res : (res?.data ?? res?.orders ?? res?.content ?? []);
    const cd     = processOrders(orders, from, to);
    await new Promise(r=>setTimeout(r,0));
    await buildCharts(cd);
  } catch(e) {
    error.value = e?.response?.data?.message || e?.message || "Không thể tải dữ liệu.";
  } finally {
    loading.value = false;
  }
}

onMounted(fetchAll);
onBeforeUnmount(()=>Object.values(charts).forEach(c=>c?.destroy()));

// ── utils ─────────────────────────────────────────────────────────────────
function formatVnd(n) {
  return new Intl.NumberFormat("vi-VN",{style:"currency",currency:"VND",maximumFractionDigits:0}).format(n||0);
}
function formatM(n) {
  if (n>=1_000_000_000) return (n/1_000_000_000).toFixed(1)+"B";
  if (n>=1_000_000)     return (n/1_000_000).toFixed(0)+"M";
  return n.toLocaleString("vi-VN");
}
function statusType(s) {
  return {DELIVERED:"success",COMPLETED:"success",CANCELLED:"danger",PENDING:"warning",NEW:"warning",SHIPPING:"info",SHIPPED:"info",PROCESSING:""}[(s||"").toUpperCase()]??"";
}

// ── icons ─────────────────────────────────────────────────────────────────
const IconBox    = {render:()=>h("svg",{viewBox:"0 0 16 16",fill:"none",width:18,height:18},[h("rect",{x:1,y:4,width:14,height:10,rx:2,stroke:"currentColor","stroke-width":1.4}),h("path",{d:"M5 4V3a3 3 0 0 1 6 0v1",stroke:"currentColor","stroke-width":1.4,"stroke-linecap":"round"})])};
const IconCheck  = {render:()=>h("svg",{viewBox:"0 0 16 16",fill:"none",width:18,height:18},[h("circle",{cx:8,cy:8,r:6.5,stroke:"currentColor","stroke-width":1.4}),h("path",{d:"M5 8l2.5 2.5L11 6",stroke:"currentColor","stroke-width":1.4,"stroke-linecap":"round","stroke-linejoin":"round"})])};
const IconX      = {render:()=>h("svg",{viewBox:"0 0 16 16",fill:"none",width:18,height:18},[h("circle",{cx:8,cy:8,r:6.5,stroke:"currentColor","stroke-width":1.4}),h("path",{d:"M5.5 5.5l5 5M10.5 5.5l-5 5",stroke:"currentColor","stroke-width":1.4,"stroke-linecap":"round"})])};
const IconMoney  = {render:()=>h("svg",{viewBox:"0 0 16 16",fill:"none",width:18,height:18},[h("rect",{x:1,y:3,width:14,height:10,rx:2,stroke:"currentColor","stroke-width":1.4}),h("circle",{cx:8,cy:8,r:2,stroke:"currentColor","stroke-width":1.4}),h("path",{d:"M1 6.5h14",stroke:"currentColor","stroke-width":1.4})])};
const IconOnline = {render:()=>h("svg",{viewBox:"0 0 16 16",fill:"none",width:14,height:14},[h("circle",{cx:8,cy:8,r:6.5,stroke:"currentColor","stroke-width":1.4}),h("path",{d:"M1.5 8h13M8 1.5a10 6.5 0 0 0 0 13M8 1.5a10 6.5 0 0 1 0 13",stroke:"currentColor","stroke-width":1.3})])};
const IconOffline= {render:()=>h("svg",{viewBox:"0 0 16 16",fill:"none",width:14,height:14},[h("path",{d:"M2 4h12v8H2z",rx:1,stroke:"currentColor","stroke-width":1.4}),h("path",{d:"M5 12v2M11 12v2M4 14h8",stroke:"currentColor","stroke-width":1.3,"stroke-linecap":"round"})])};
</script>

<style scoped>
* { box-sizing: border-box; }

.order-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 8px 48px;
  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
}

/* ══ HEADER ══ */
.page-header {
  display:flex; align-items:center; justify-content:space-between;
  margin-bottom:28px; flex-wrap:wrap; gap:12px;
}
.page-title { font-size:22px; font-weight:700; color:#0f172a; margin:0 0 3px; }
.page-sub   { font-size:13px; color:#64748b; margin:0; }
.header-actions { display:flex; gap:8px; align-items:center; }

/* Period select */
.period-select-el { width:148px; }
:deep(.period-select-el .el-select__wrapper) {
  height:36px!important; border-radius:9px!important;
  border:1px solid #e2e8f0!important; box-shadow:0 1px 3px rgba(0,0,0,.06)!important;
  background:#fff!important; padding:0 10px!important;
  font-size:13px!important; font-weight:500!important; color:#1e293b!important;
  transition:border-color .15s,box-shadow .15s!important;
}
:deep(.period-select-el .el-select__wrapper:hover)      { border-color:#94a3b8!important; box-shadow:0 2px 6px rgba(0,0,0,.09)!important; }
:deep(.period-select-el .el-select__wrapper.is-focused) { border-color:#3b82f6!important; box-shadow:0 0 0 3px rgba(59,130,246,.12)!important; }
:deep(.period-select-el .el-select__prefix)  { color:#64748b; margin-right:2px; }
:deep(.period-select-el .el-select__caret)   { color:#94a3b8!important; }

/* Refresh button */
.refresh-btn {
  display:flex; align-items:center; gap:7px;
  height:36px; padding:0 16px;
  background:#fff; border:1px solid #e2e8f0; border-radius:9px;
  font-size:13px; font-weight:500; color:#1e293b; font-family:inherit;
  cursor:pointer; box-shadow:0 1px 3px rgba(0,0,0,.06); transition:all .15s;
}
.refresh-btn:hover:not(:disabled) { background:#f8fafc; border-color:#94a3b8; box-shadow:0 2px 6px rgba(0,0,0,.09); }
.refresh-btn:active:not(:disabled){ background:#f1f5f9; transform:scale(.97); }
.refresh-btn:disabled { opacity:.6; cursor:not-allowed; }
@keyframes spin { to { transform:rotate(360deg); } }
.refresh-btn .spin { animation:spin .7s linear infinite; }

/* ══ KPI ══ */
.kpi-grid {
  display:grid; grid-template-columns:repeat(4,1fr); gap:14px; margin-bottom:16px;
}
@media(max-width:1024px){.kpi-grid{grid-template-columns:repeat(2,1fr);}}
@media(max-width:600px) {.kpi-grid{grid-template-columns:1fr;}}

.kpi-card {
  display:flex; align-items:center; gap:14px;
  background:#fff; border-radius:12px; padding:16px 18px;
  box-shadow:0 1px 4px rgba(0,0,0,.06); border:1px solid #e2e8f0;
  transition:transform .15s,box-shadow .15s;
}
.kpi-card:hover{transform:translateY(-2px);box-shadow:0 6px 18px rgba(0,0,0,.09);}
.kpi-icon{width:42px;height:42px;border-radius:10px;display:flex;align-items:center;justify-content:center;flex-shrink:0;}
.kpi-card.blue   .kpi-icon{background:rgba(59,130,246,.12); color:#3b82f6;}
.kpi-card.green  .kpi-icon{background:rgba(16,185,129,.12); color:#10b981;}
.kpi-card.red    .kpi-icon{background:rgba(239,68,68,.12);  color:#ef4444;}
.kpi-card.purple .kpi-icon{background:rgba(139,92,246,.12); color:#8b5cf6;}
.kpi-body  {display:flex;flex-direction:column;gap:2px;}
.kpi-label {font-size:12px;font-weight:500;color:#64748b;}
.kpi-value {font-size:20px;font-weight:700;color:#0f172a;line-height:1.2;}

/* ══ SECTION ROW: status + channel ══ */
.section-row {
  display:grid; grid-template-columns:1fr 1fr; gap:14px; margin-bottom:16px;
}
@media(max-width:900px){.section-row{grid-template-columns:1fr;}}

.breakdown-card {
  background:#fff; border-radius:12px; padding:18px 20px;
  box-shadow:0 1px 4px rgba(0,0,0,.06); border:1px solid #e2e8f0; min-width:0;
}
.breakdown-header {
  display:flex; align-items:center; justify-content:space-between; margin-bottom:14px;
}
.breakdown-title { font-size:14px; font-weight:600; color:#0f172a; }
.breakdown-total { font-size:12px; color:#94a3b8; font-weight:500; }

/* Status grid — card per status */
.status-grid {
  display:grid; grid-template-columns:repeat(3,1fr); gap:8px;
}
@media(max-width:1100px){.status-grid{grid-template-columns:repeat(2,1fr);}}

.status-card {
  display:flex; align-items:center; gap:8px;
  padding:9px 11px;
  background:var(--sc-bg);
  border:1px solid color-mix(in srgb, var(--sc) 20%, transparent);
  border-radius:9px;
  transition:transform .12s;
}
.status-card:hover { transform:translateY(-1px); }
.sc-dot {
  width:8px; height:8px; border-radius:50%; flex-shrink:0;
  background:var(--sc);
}
.sc-body { flex:1; min-width:0; display:flex; flex-direction:column; gap:1px; }
.sc-label { font-size:11px; color:#64748b; font-weight:500; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.sc-count { font-size:16px; font-weight:700; color:#0f172a; line-height:1.2; }
.sc-pct   { font-size:11px; font-weight:600; color:var(--sc); flex-shrink:0; }

/* Channel grid */
.channel-grid {
  display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:14px;
}
.channel-card {
  padding:12px 14px; border-radius:10px;
  background:var(--cc-bg);
  border:1px solid color-mix(in srgb, var(--cc) 18%, transparent);
  display:flex; flex-direction:column; gap:4px;
}
.cc-header { display:flex; align-items:center; gap:6px; margin-bottom:2px; }
.cc-icon-wrap { color:var(--cc); display:flex; }
.cc-label  { font-size:12px; font-weight:600; color:#475569; }
.cc-count  { font-size:22px; font-weight:700; color:#0f172a; line-height:1.1; }
.cc-revenue{ font-size:11.5px; color:#64748b; }
.cc-bar-track { height:4px; background:#e2e8f0; border-radius:99px; margin-top:4px; }
.cc-bar-fill  { height:100%; background:var(--cc); border-radius:99px; transition:width .4s; }
.cc-pct { font-size:11px; color:var(--cc); font-weight:600; }

/* Channel rate table */
.channel-rate-table { border-top:1px solid #f1f5f9; padding-top:12px; }
.crt-head, .crt-row {
  display:grid; grid-template-columns:80px 1fr 1fr 80px;
  font-size:12px; padding:5px 0;
}
.crt-head { color:#94a3b8; font-weight:600; font-size:11px; text-transform:uppercase; letter-spacing:.04em; border-bottom:1px solid #f1f5f9; padding-bottom:6px; margin-bottom:2px; }
.crt-row  { color:#475569; border-bottom:1px solid #f8fafc; align-items:center; }
.crt-channel { font-weight:600; color:#1e293b; }
.crt-done    { color:#10b981; font-weight:600; }
.crt-cancel  { color:#ef4444; font-weight:600; }
.crt-rate    { font-weight:700; font-size:13px; }
.crt-rate.good { color:#10b981; }
.crt-rate.bad  { color:#f59e0b; }

/* ══ CHART CARDS ══ */
.charts-2col {
  display:grid; grid-template-columns:1fr 1fr; gap:14px; margin-bottom:14px;
}
@media(max-width:900px){.charts-2col{grid-template-columns:1fr;}}

.chart-card {
  background:#fff; border-radius:12px; padding:18px 20px;
  box-shadow:0 1px 4px rgba(0,0,0,.06); border:1px solid #e2e8f0; min-width:0;
}
.chart-header {
  display:flex; align-items:center; justify-content:space-between; margin-bottom:12px;
}
.chart-title { font-size:14px; font-weight:600; color:#0f172a; }
.view-all { font-size:12px; color:#3b82f6; text-decoration:none; }
.view-all:hover { text-decoration:underline; }

.canvas-wrap { position:relative; height:200px; width:100%; }
.canvas-wrap canvas { position:absolute; inset:0; width:100%!important; height:100%!important; }

/* Legend */
.legend { display:flex; gap:10px; font-size:12px; color:#64748b; align-items:center; flex-wrap:wrap; }
.dot { display:inline-block; width:8px; height:8px; border-radius:50%; margin-right:3px; }
.dot.blue   { background:#3b82f6; }
.dot.green  { background:#10b981; }
.dot.red    { background:#ef4444; }
.dot.indigo { background:#6366f1; }

/* Channel tag in table */
.channel-tag {
  font-size:11px; font-weight:600; padding:2px 7px; border-radius:5px;
}
.channel-tag.online  { background:rgba(59,130,246,.1);  color:#3b82f6; }
.channel-tag.offline { background:rgba(99,102,241,.1);  color:#6366f1; }
</style>