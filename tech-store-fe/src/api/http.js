import axios from "axios";
import { getToken, clearSession, clearLastAuthResponse } from "../stores/auth";

const http = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      clearSession();
      clearLastAuthResponse();
      window.dispatchEvent(
        new CustomEvent("auth:logout", { detail: "401 Unauthorized" })
      );
    }
    return Promise.reject(err);
  }
);

export default http;
