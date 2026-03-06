import http from "./http";

export default {

  revenueByChannel(){
    return http.get("/api/reports/revenue-by-channel");
  },

  revenueByDate(){
    return http.get("/api/reports/revenue-by-date");
  },

  ordersByStaff(){
    return http.get("/api/reports/orders-by-staff");
  }

};