import http from "./http";

export const productsApi = {
  create(form) {
    // multipart/form-data: name, sku, description, isVisible, categoryId, imageFile
    const fd = new FormData();
    fd.append("name", form.name || "");
    fd.append("sku", form.sku || "");
    fd.append("description", form.description || "");
    fd.append("isVisible", String(!!form.isVisible));
    fd.append("categoryId", String(form.categoryId || ""));
    if (form.imageFile) fd.append("imageFile", form.imageFile);

    return http.post("/api/products", fd, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
  list({ page = 0, categoryId } = {}) {
    // GET /api/products?page=0&categoryId=<id?>
    const params = { page };
    if (categoryId != null && categoryId !== "") params.categoryId = categoryId;
    return http.get("/api/products", { params });
  },
};
