import http from "./http";

export const authApi = {
  register(payload) {
    // payload: { username, email, password }
    return http.post("/api/auth/register", payload);
  },
  login(payload) {
    // payload: { identifier, password }
    return http.post("/api/auth/login", payload);
  },

  // dùng để test endpoint cần auth (bạn đổi path theo API thật)
  testProtected(path = "/api/test/protected") {
    return http.get(path);
  }
};
