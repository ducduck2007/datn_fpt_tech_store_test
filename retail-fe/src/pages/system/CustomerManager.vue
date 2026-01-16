<template>
  <div class="customer-container">
    <div class="header-top">
      <h2>Quản Lý Khách Hàng</h2>
      <el-button type="primary" :icon="Plus" @click="openCreateModal">
        Thêm Khách Hàng
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="Tìm theo Tên, SĐT hoặc Email..."
            clearable
            :prefix-icon="Search"
          />
        </el-col>
        
        <el-col :span="6">
          <el-select v-model="filterType" placeholder="Tất cả loại khách" clearable style="width: 100%">
            <el-option label="Tất cả" value="" />
            <el-option label="Khách thường (Regular)" value="REGULAR" />
            <el-option label="Khách VIP" value="VIP" />
          </el-select>
        </el-col>

        <el-col :span="4">
          <el-button @click="resetFilters">Làm mới bộ lọc</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="box-card">
      <el-table 
        v-loading="loading" 
        :data="filteredData" 
        style="width: 100%" 
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="Họ và Tên" width="180" />
        <el-table-column prop="email" label="Email" width="200" />
        <el-table-column prop="phone" label="SĐT" width="120" />
        
        <el-table-column label="Loại Khách" width="150" align="center">
          <template #default="scope">
            <el-tag :type="getCustomerTypeTag(scope.row.customerType)">
              {{ scope.row.customerTypeDisplay || scope.row.customerType }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Hạng TV" width="120" align="center">
          <template #default="scope">
             <el-tag v-if="scope.row.vipTier" effect="dark" color="#gold">
               {{ scope.row.vipTierDisplay }}
             </el-tag>
             <span v-else class="text-gray">Thường</span>
          </template>
        </el-table-column>

        <el-table-column prop="totalSpent" label="Chi tiêu" width="120">
             <template #default="scope">
                {{ formatCurrency(scope.row.totalSpent) }}
             </template>
        </el-table-column>

        <el-table-column label="Thao tác" min-width="150" align="center">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="openEditModal(scope.row)">Sửa</el-button>
            <el-button 
              size="small" 
              type="danger" 
              :icon="Delete" 
              @click="handleDelete(scope.row.id)"
            >Xóa</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div style="margin-top: 15px; text-align: right; color: #666">
        Tổng số: {{ filteredData.length }} khách hàng
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? 'Cập nhật Khách Hàng' : 'Thêm mới Khách Hàng'"
      width="500px"
      @close="resetForm"
    >
      <el-form 
        ref="customerFormRef" 
        :model="formData" 
        :rules="rules" 
        label-width="120px" 
        status-icon
      >
        <el-form-item label="Họ và Tên" prop="fullName">
          <el-input v-model="formData.fullName" placeholder="Nhập họ tên" />
        </el-form-item>

        <el-form-item label="Email" prop="email">
          <el-input v-model="formData.email" placeholder="example@mail.com" />
        </el-form-item>

        <el-form-item label="Số điện thoại" prop="phone">
          <el-input v-model="formData.phone" placeholder="09xxxxxxxx" />
        </el-form-item>

        <el-form-item label="Ngày sinh" prop="birthDate">
          <el-date-picker
            v-model="formData.birthDate"
            type="date"
            placeholder="Chọn ngày sinh"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Địa chỉ" prop="address">
          <el-input v-model="formData.address" placeholder="Nhập địa chỉ" />
        </el-form-item>
        
        <el-form-item label="Ghi chú" prop="notes">
          <el-input v-model="formData.notes" type="textarea" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Hủy</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEditMode ? 'Cập nhật' : 'Lưu lại' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'

const API_URL = 'http://localhost:8080/api/auth/customers'; 

// --- State ---
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([]) 
const dialogVisible = ref(false)
const isEditMode = ref(false)
const customerFormRef = ref(null)
const currentId = ref(null)

// --- Filter State ---
const searchQuery = ref('')
const filterType = ref('') 

const formData = reactive({
  fullName: '',
  email: '',
  phone: '',
  birthDate: null,
  address: '',
  customerType: 'REGULAR',
  notes: ''
})

const rules = {
  fullName: [
    { required: true, message: 'Vui lòng nhập họ tên', trigger: 'blur' },
    { min: 2, message: 'Tên phải có ít nhất 2 ký tự', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
    { type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' },
    { pattern: /^[0-9]{10,11}$/, message: 'Số điện thoại phải là số (10-11 số)', trigger: 'blur' }
  ]
}

// --- Computed Filter ---
const filteredData = computed(() => {
  return tableData.value.filter(item => {
    // 1. Lọc text
    const query = searchQuery.value.toLowerCase()
    const matchName = item.name ? item.name.toLowerCase().includes(query) : false
    const matchEmail = item.email ? item.email.toLowerCase().includes(query) : false
    const matchPhone = item.phone ? item.phone.includes(query) : false
    const matchText = !query || matchName || matchEmail || matchPhone

    // 2. Lọc loại khách
    const matchType = !filterType.value || item.customerType === filterType.value

    return matchText && matchType
  })
})

// --- Methods ---

const fetchCustomers = async () => {
  loading.value = true
  try {
    const response = await axios.get(API_URL)
    // Lọc bỏ khách đã xóa (isActive=false)
    tableData.value = response.data.filter(customer => customer.isActive === true)
  } catch (error) {
    console.error(error)
    ElMessage.error('Không thể tải danh sách khách hàng')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  filterType.value = ''
}

const openCreateModal = () => {
  isEditMode.value = false
  currentId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditModal = (row) => {
  isEditMode.value = true
  currentId.value = row.id
  formData.fullName = row.name
  formData.email = row.email
  formData.phone = row.phone
  formData.birthDate = row.dateOfBirth
  formData.address = row.address
  formData.customerType = row.customerType 
  formData.notes = row.notes
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!customerFormRef.value) return
  await customerFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEditMode.value) {
          await axios.put(`${API_URL}/${currentId.value}`, formData)
          ElMessage.success('Cập nhật thành công!')
        } else {
          await axios.post(API_URL, formData)
          ElMessage.success('Thêm mới thành công!')
        }
        dialogVisible.value = false
        fetchCustomers() 
      } catch (error) {
        const msg = error.response?.data?.message || 'Có lỗi xảy ra'
        ElMessage.error(msg)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm(
    'Bạn có chắc chắn muốn xóa khách hàng này không?', 'Cảnh báo',
    { confirmButtonText: 'Xóa', cancelButtonText: 'Hủy', type: 'warning' }
  ).then(async () => {
      try {
        await axios.delete(`${API_URL}/${id}`)
        ElMessage.success('Đã xóa thành công')
        fetchCustomers()
      } catch (error) {
        ElMessage.error('Xóa thất bại')
      }
    }).catch(() => {})
}

const resetForm = () => {
  if (customerFormRef.value) customerFormRef.value.resetFields()
  formData.fullName = ''
  formData.email = ''
  formData.phone = ''
  formData.birthDate = null
  formData.address = ''
  formData.customerType = 'REGULAR'
  formData.notes = ''
}

const formatCurrency = (value) => {
  if (!value && value !== 0) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// Bỏ case WHOLESALE
const getCustomerTypeTag = (type) => {
  switch (type) {
    case 'VIP': return 'warning'
    default: return 'success' // REGULAR là màu xanh lá
  }
}

onMounted(() => {
  fetchCustomers()
})
</script>

<style scoped>
.customer-container {
  padding: 20px;
}
.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.filter-card {
  margin-bottom: 20px;
}
.text-gray {
  color: #909399;
  font-size: 12px;
}
</style>