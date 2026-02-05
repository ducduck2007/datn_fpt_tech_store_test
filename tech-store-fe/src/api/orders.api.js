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
  cancel(orderId, reason) {
    return http.put(`/api/orders/${orderId}/cancel`, null, {
      params: { reason: reason || 'Customer cancelled' }
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
    return http.get("/api/orders/my");
  },
};
