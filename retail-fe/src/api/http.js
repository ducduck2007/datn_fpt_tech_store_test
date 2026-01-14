import axios from "axios";
import { AUTH_TOKEN_KEY, AUTH_USER_KEY } from "../stores/auth";

const http = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 15000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(AUTH_TOKEN_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem(AUTH_TOKEN_KEY);
      localStorage.removeItem(AUTH_USER_KEY);
      window.dispatchEvent(new CustomEvent("auth:logout", { detail: "401 Unauthorized" }));
    }
    return Promise.reject(err);
  }
);

export default http;
