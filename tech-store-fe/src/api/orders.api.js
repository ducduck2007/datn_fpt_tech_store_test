import http from "./http";

export const ordersApi = {
  create(payload) {
    // POST /api/orders (Customer)
    // Body: { customerId, paymentMethod, channel, notes, items:[{ variantId, quantity }] }
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

  // ✅ HỦY ĐỠN - CÓ THAM SỐ REASON
  // ✅ Gửi reason trong BODY, không phải query params
  cancel(orderId, reason) {
    return http.put(`/api/orders/${orderId}/cancel`, {
      reason: reason || "Customer cancelled",
    });
  },

  // ✅ CHUYỂN SANG SHIPPING
  markAsShipping(orderId) {
    return http.put(`/api/orders/${orderId}/ship`);
  },

  // ✅ ĐÁNH DẤU ĐÃ GIAO
  markAsDelivered(orderId) {
    return http.put(`/api/orders/${orderId}/deliver`);
  },

  remove(orderId) {
    return http.delete(`/api/orders/${orderId}`);
  },
  getMyOrders() {
    return http.get("/api/orders/my-orders");
  },

  // LẤY ĐƠN THEO KHOẢNG NGÀY
getByDate(from, to) {
  return http.get("/api/orders/by-date", {
    params: { from, to },
  });
},

// FILTER THEO KHÁCH HÀNG + NGÀY
filter(customerId, from, to) {
  return http.get("/api/orders/filter", {
    params: { customerId, from, to },
  });
},

// DOANH THU THEO KHÁCH HÀNG
getRevenueByCustomer() {
  return http.get("/api/orders/revenue-by-customer");
},
getOrderDetail(orderId) {
    return http.get(`/api/orders/${orderId}`);
  },
  getOrderDetail(orderId) {
    return http.get(`/api/orders/${orderId}`);
  }
};
