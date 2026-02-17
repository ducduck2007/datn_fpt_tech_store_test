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
  },
  listByPointsRange(minPoints, maxPoints) {
    return http.get(`/api/auth/customers/points?min=${minPoints}&max=${maxPoints}`);
  },
  listByPointsRange(minPoints, maxPoints) {
    return http.get(`/api/auth/customers/points?min=${minPoints}&max=${maxPoints}`);
  },
  listByVipTier(tier) {
    return http.get(`/api/auth/customers/vip-tier/${tier}`);
  },
   listBySpendingRange(minSpent, maxSpent) {
    return http.get(`/api/auth/customers/spending?min=${minSpent}&max=${maxSpent}`);
  },
  
  listTopSpenders(limit = 10) {
    return http.get(`/api/auth/customers/top-spenders?limit=${limit}`);
  },
  
  listTopSpendersByVipTier(tier, limit = 10) {
    return http.get(`/api/auth/customers/vip-tier/${tier}/top-spenders?limit=${limit}`);
  },
  
  getSpendingStatistics() {
    return http.get(`/api/auth/customers/spending-stats`);
  },

  // Fixed duplicate method
  listByVipTierAndPoints(tier, minPoints, maxPoints) {
    return http.get(`/api/auth/customers/vip-tier/${tier}/points?min=${minPoints}&max=${maxPoints}`);
  },
  getSpinWheelStatus() {
    return http.get("/api/auth/customers/spin-wheel/status");
  },
  
  spinWheel() {
    return http.post("/api/auth/customers/spin-wheel/spin");
  },
  
  getSpinWheelHistory() {
    return http.get("/api/auth/customers/spin-wheel/history");
  },
  
  getSpinWheelPrizes() {
    return http.get("/api/auth/customers/spin-wheel/prizes");
  },
  // ✅ SPIN WHEEL
  getSpinWheelStatus() {
    return http.get("/api/auth/customers/spin-wheel/status");
  },

  spin() {
    return http.post("/api/auth/customers/spin-wheel/spin");
  },

  getSpinHistory() {
    return http.get("/api/auth/customers/spin-wheel/history");
  },

  getSpinPrizes() {
    return http.get("/api/auth/customers/spin-wheel/prizes");
  },
  getTierProgress() {
    return http.get("/api/auth/tier-progress/me");
  },
  checkTierProgressWithCart(cartTotal) {
    return http.post("/api/auth/tier-progress/check-cart", { cartTotal });
  },
  getCustomerTierProgress(customerId) {
    return http.get(`/api/auth/tier-progress/customer/${customerId}`);
  },
    getPayments(customerId) {
    return http.get(`/api/auth/customers/${customerId}/payments`);
  },
  getPaymentsIncludingDeleted(customerId) {
    return http.get(`/api/auth/customers/${customerId}/payments/all`);
  },
  getDeletedPayments(customerId) {
    return http.get(`/api/auth/customers/${customerId}/payments/deleted`);
  }
};
