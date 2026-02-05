import http from "./http";

export const returnsApi = {
  // Customer tạo yêu cầu trả hàng
  create(payload) {
    return http.post("/api/returns", payload);
  },

  // Lấy danh sách trả hàng của tôi
  getMyReturns() {
    return http.get("/api/returns/my-returns");
  },

  // Lấy trả hàng theo đơn hàng
  getByOrder(orderId) {
    return http.get(`/api/returns/order/${orderId}`);
  },

  // Chi tiết trả hàng
  getDetail(returnId) {
    return http.get(`/api/returns/${returnId}`);
  },

  // Hủy yêu cầu trả hàng (customer)
  cancel(returnId) {
    return http.delete(`/api/returns/${returnId}`);
  },

  // Admin/Staff
  getAll() {
    return http.get("/api/returns");
  },

  getPending() {
    return http.get("/api/returns/pending");
  },

  getApproved() {
    return http.get("/api/returns/approved");
  },

  getRejected() {
    return http.get("/api/returns/rejected");
  },

  approve(returnId) {
    return http.put(`/api/returns/${returnId}/approve`);
  },

  reject(returnId, reason) {
    return http.put(`/api/returns/${returnId}/reject`, { reason });
  }
};