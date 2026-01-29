import http from "./http";
// 1. Đảm bảo tên biến là BASE_URL
const BASE_URL = "/api/auth/payments";

export const paymentsApi = {
  getAll() {
    return http.get(BASE_URL);
  },
  
  getById(paymentId) {
    // 2. Sửa từ ${API_BASE} thành ${BASE_URL}
    return http.get(`${BASE_URL}/${paymentId}`);
  },

  getDetail(paymentId) {
    // 3. Sửa từ ${API_BASE} thành ${BASE_URL}
    return http.get(`${BASE_URL}/${paymentId}/detail`);
  },

  getByOrder(orderId) {
    // 4. Sửa từ ${API_BASE} thành ${BASE_URL}
    return http.get(`${BASE_URL}/order/${orderId}`);
  },

  create(payload) {
    return http.post(BASE_URL, payload);
  },
};