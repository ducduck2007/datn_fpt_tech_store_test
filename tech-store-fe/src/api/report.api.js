import http from "./http";

export default {

  revenueByChannel() {
    return http.get("/api/reports/revenue-by-channel");
  },

  revenueByDate() {
    return http.get("/api/reports/revenue-by-date");
  },

  ordersByStaff() {
    return http.get("/api/reports/orders-by-staff");
  },

  // Tổng hợp doanh thu theo trạng thái đơn hàng
  // Response: [{ status, orderCount, totalRevenue }]
  revenueByStatus() {
    return http.get("/api/reports/revenue-by-status");
  },

  // Thống kê trả hàng theo sản phẩm (loại trừ REJECTED)
  // Response: [{ productName, sku, variantName, returnCount, totalQty, totalRefund }]
  returnsByProduct() {
    return http.get("/api/reports/returns-by-product");
  },

};