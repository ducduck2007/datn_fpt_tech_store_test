// ADD: Pinia store quản lý trạng thái giỏ hàng (badge)

import { defineStore } from "pinia";
import { cartApi } from "../api/cart.api";

export const useCartStore = defineStore("cart", {
  state: () => ({
    count: 0 // ADD: số lượng item trong giỏ
  }),

  actions: {
    async addToCart(variantId, quantity = 1) {
      // ADD: validate FE trước
      if (!variantId || quantity <= 0) {
        throw new Error("Invalid variantId or quantity");
      }

      await cartApi.addItem({
  variantId: Number(variantId),   // ✅ ĐÚNG TÊN FIELD
  quantity: Number(quantity),
});

      // ADD: reload badge sau khi add thành công
      await this.refreshCount();
    },

    // =========================
    // CART BADGE
    // =========================
    async refreshCount() {
      const res = await cartApi.count();
      this.count = Number(res?.data || 0);
    },
  }
});
