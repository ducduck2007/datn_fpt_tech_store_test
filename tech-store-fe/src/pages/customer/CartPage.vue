<template>
  <div class="container-xl">
    <el-card shadow="never">
      <h3>Giỏ hàng</h3>

      <el-table :data="items" border>
        <el-table-column prop="productName" label="Sản phẩm" />
        <el-table-column prop="price" label="Giá" />

        <el-table-column label="Số lượng">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              @change="updateQty(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="Hành động">
          <template #default="{ row }">
            <el-button type="danger" @click="removeItem(row)">
              Xóa
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="text-end mt-3">
        <el-button
          type="primary"
          :disabled="!items.length"
          @click="checkout"
        >
          Thanh toán
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
// ADD: Cart page
import { onMounted, ref } from "vue";
import { cartApi } from "../../api/cart.api";
import { useRouter } from "vue-router";

const router = useRouter();
const items = ref([]);

async function loadCart() {
  const res = await cartApi.getItems();
  items.value = res.data;
}

async function updateQty(item) {
  await cartApi.updateQuantity(item.cartItemId, item.quantity);
}

async function removeItem(item) {
  await cartApi.remove(item.cartItemId);
  await loadCart();
}

function checkout() {
  router.push("/orders/new");
}

onMounted(loadCart);
</script>
