import http from "./http";

export const ordersApi = {
  create(payload) {
    // POST /api/orders (Customer)
    // Body: { customerId, paymentMethod, channel, notes, promotionCode?, items:[{ variantId, quantity }] }
    return http.post("/api/orders", payload);
  },

  listNew() {
    return http.get("/api/orders/new");
  },

  listProcessing() {
    return http.get("/api/orders/processing");
  },

  listPaid() {
    return http.get("/api/orders/paid");
  },

  listDelivered() {
    return http.get("/api/orders/delivered");
  },

  listShipping() {
    return http.get("/api/orders/shipping");
  },

  getByCustomer(customerId) {
    return http.get(`/api/orders/customer/${customerId}`);
  },

  getById(orderId) {
    return http.get(`/api/orders/${orderId}`);
  },

  update(orderId, payload) {
    // Body: { paymentMethod, notes }
    return http.put(`/api/orders/${orderId}`, payload);
  },

  cancel(orderId, reason) {
    return http.put(`/api/orders/${orderId}/cancel`, {
      reason: reason || "Customer cancelled",
    });
  },

  markAsShipping(orderId) {
    return http.put(`/api/orders/${orderId}/ship`);
  },

  markAsDelivered(orderId) {
    return http.put(`/api/orders/${orderId}/deliver`);
  },

  markAsProcessing(orderId) {
    return http.put(`/api/orders/${orderId}/process`);
  },

  remove(orderId) {
    return http.delete(`/api/orders/${orderId}`);
  },

  getMyOrders() {
    return http.get("/api/orders/my-orders");
  },

  getByDate(from, to) {
    return http.get("/api/orders/by-date", { params: { from, to } });
  },

  filter(customerId, from, to, channel) {
    return http.get("/api/orders/filter", {
      params: { customerId, from, to, channel },
    });
  },

  getRevenueByCustomer() {
    return http.get("/api/orders/revenue-by-customer");
  },

  getOrderDetail(orderId) {
    return http.get(`/api/orders/${orderId}`);
  },

  // ✅ THÊM MỚI: validate mã giảm giá trước khi tạo đơn
  // Trả về: { valid, discount, minOrder, message }
  validatePromoCode(code, orderTotal) {
    return http.get("/api/promotions/validate", {
      params: { code, orderTotal },
    });
  },
};
