import http from "./http";

export const productsApi = {
  list(params = {}) {
    return http.get("/api/products", { params });
  },

  create(formData) {
    return http.post("/api/products", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  update(id, formData) {
    return http.put(`/api/products/${id}`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  remove(id) {
    return http.delete(`/api/products/${id}`);
  },
  
  get(id) {
    return http.get(`/api/products/${id}`);
  }
};