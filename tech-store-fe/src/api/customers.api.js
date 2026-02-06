import http from "./http";

export const customersApi = {
  create(payload) {
    return http.post("/api/auth/customers", payload);
  },
  listAll() {
    return http.get("/api/auth/customers");
  },
  listByType(type) {
    return http.get(`/api/auth/customers/type/${type}`);
  },
  update(id, payload) {
    return http.put(`/api/auth/customers/${id}`, payload);
  },
  remove(id) {
    return http.delete(`/api/auth/customers/${id}`);
  },
  getProfile() {
    return http.get("/api/auth/customers/me");
  },
  listActiveLast30Days() {
    return http.get("/api/auth/customers/active-last-30-days");
  },
   getLoyaltyHistory(customerId) {
    return http.get(`/api/auth/customers/${customerId}/loyalty-history`);
  },
  getTierHistory(customerId) {
    return http.get(`/api/auth/customers/${customerId}/tier-history`);
  }
};
