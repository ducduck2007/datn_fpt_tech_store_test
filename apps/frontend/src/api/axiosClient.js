import axios from "axios";
import { getAccessToken, clearAccessToken } from "../storage/token";

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

// attach JWT
axiosClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// basic error handling
axiosClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      clearAccessToken();
      // có thể redirect login ở đây nếu muốn
    }
    return Promise.reject(err);
  }
);

export default axiosClient;
