<template>
  <div class="page-container">

    <div class="page-header">
      <h2>System Reports</h2>
    </div>

    <!-- Revenue by Channel -->
    <el-card class="box-card" shadow="never">
      <template #header>
        <span>Revenue by Channel</span>
      </template>

      <el-table
        :data="channelData"
        stripe
        style="width:100%"
      >

        <el-table-column
          prop="channel"
          label="Channel"
        />

        <el-table-column
          prop="revenue"
          label="Revenue"
        >
          <template #default="scope">
            {{ formatMoney(scope.row.revenue) }}
          </template>
        </el-table-column>

      </el-table>
    </el-card>


    <!-- Revenue by Date -->
    <el-card class="box-card" shadow="never" style="margin-top:20px">

      <template #header>
        <span>Revenue by Date</span>
      </template>

      <el-table
        :data="dateData"
        stripe
        style="width:100%"
      >

        <el-table-column
          prop="date"
          label="Date"
        />

        <el-table-column
          prop="revenue"
          label="Revenue"
        >
          <template #default="scope">
            {{ formatMoney(scope.row.revenue) }}
          </template>
        </el-table-column>

      </el-table>

    </el-card>


    <!-- Orders by Staff -->
    <el-card class="box-card" shadow="never" style="margin-top:20px">

      <template #header>
        <span>Orders by Staff</span>
      </template>

      <el-table
        :data="staffData"
        stripe
        style="width:100%"
      >

        <el-table-column
          prop="staff"
          label="Staff"
        />

        <el-table-column
          prop="orders"
          label="Orders"
        />

      </el-table>

    </el-card>

  </div>
</template>


<script setup>

import { ref, onMounted } from "vue"
import reportsApi from "../../api/report.api"

const channelData = ref([])
const dateData = ref([])
const staffData = ref([])

function formatMoney(v){
  return new Intl.NumberFormat("vi-VN").format(v)
}

onMounted(async ()=>{

  const ch = await reportsApi.revenueByChannel()
  channelData.value = ch.data

  const dt = await reportsApi.revenueByDate()
  dateData.value = dt.data

  const st = await reportsApi.ordersByStaff()
  staffData.value = st.data

})

</script>