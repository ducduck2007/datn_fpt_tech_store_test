// \retail-fe\src\api\auth.api.js
import http from "./http";

export const authApi = {
  register(payload) {
    return http.post("/api/auth/register", payload);
  },
  login(payload) {
    return http.post("/api/auth/login", payload);
  },
  testProtected(path = "/api/test/protected") {
    return http.get(path);
  },
};
