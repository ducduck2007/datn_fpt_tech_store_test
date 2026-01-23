import axios from "axios";
import http from "./http";
const BASE_URL = "/api/auth/payments";

export const paymentsApi = {
  /**
   * Lấy tất cả payments
   */
  getAll() {
    return http.get("/api/auth/payments");
  },
   create(payload) {
    return http.post("/api/auth/payments", payload);
  },
  };