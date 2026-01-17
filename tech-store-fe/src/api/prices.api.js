import http from "./http";

export const pricesApi = {
  setVariantPrice(variantId, payload) {
    // POST /api/prices/variants/{variantId}
    return http.post(`/api/prices/variants/${variantId}`, payload);
  },
  listByProduct(productId) {
    // GET /api/prices/products/{productId}
    return http.get(`/api/prices/products/${productId}`);
  },
  updateHistory(id, payload) {
    // PUT /api/prices/history/{id}
    return http.put(`/api/prices/history/${id}`, payload);
  },
  removeHistory(id) {
    // DELETE /api/prices/history/{id}
    return http.delete(`/api/prices/history/${id}`);
  },
  getEffective(variantId) {
    // GET /api/prices/variants/{variantId}/effective
    return http.get(`/api/prices/variants/${variantId}/effective`);
  },
};
