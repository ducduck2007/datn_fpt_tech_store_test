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
  getById(orderId) {
    return http.get(`/api/orders/${orderId}`);
  },
  update(orderId, payload) {
    // Body: { paymentMethod, notes }
    return http.put(`/api/orders/${orderId}`, payload);
  },
  cancel(orderId) {
    return http.put(`/api/orders/${orderId}/cancel`);
  },
  remove(orderId) {
    return http.delete(`/api/orders/${orderId}`);
  },
  getMyOrders() {
    return http.get("/api/orders/my");
  },
};
