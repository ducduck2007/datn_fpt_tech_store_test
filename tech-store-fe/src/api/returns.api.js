import http from "./http";

export const returnsApi = {
  // Customer tạo yêu cầu trả hàng
  create(payload) {
    return http.post("/api/returns", payload);
  },

  // Lấy trả hàng theo đơn
  getByOrder(orderId) {
    return http.get(`/api/returns/order/${orderId}`);
  },

  // Chi tiết
  getDetail(returnId) {
    return http.get(`/api/returns/${returnId}`);
  },

  // Customer hủy yêu cầu trả hàng
  cancel(returnId) {
    return http.put(`/api/returns/${returnId}/cancel`);
  },

  // Staff / Admin
  getAll() {
    return http.get("/api/returns");
  },

  getPending() {
    return http.get("/api/returns/pending");
  },

  approve(returnId) {
    return http.put(`/api/returns/${returnId}/approve`);
  },

  reject(returnId, reason) {
    return http.put(`/api/returns/${returnId}/reject`, { reason });
  },

  getByStatus(status) {
    return http.get(`/api/returns/status/${status}`);
  },
};
