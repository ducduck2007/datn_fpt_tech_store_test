import http from "./http";

export const promotionsApi = {
  create(payload) {
    return http.post("/api/promotions", payload);
  },
  list(activeOnly = false) {
    return http.get("/api/promotions", {
      params: { activeOnly: !!activeOnly },
    });
  },
  update(id, payload) {
    return http.put(`/api/promotions/${id}`, payload);
  },
  remove(id) {
    return http.delete(`/api/promotions/${id}`);
  },
};
