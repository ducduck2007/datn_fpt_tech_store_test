import axios from "axios";

const http = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 15000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("access_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ✅ Nếu backend trả 401 (token hết hạn/invalid) => tự logout
http.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem("access_token");
      // phát event để UI cập nhật
      window.dispatchEvent(new CustomEvent("auth:logout", { detail: "401 Unauthorized" }));
    }
    return Promise.reject(err);
  }
);

export default http;
