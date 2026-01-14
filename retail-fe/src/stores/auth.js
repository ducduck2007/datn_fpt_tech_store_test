// src/stores/auth.js
export const AUTH_TOKEN_KEY = "access_token";
export const AUTH_USER_KEY = "auth_user";
export const LAST_RESPONSE_KEY = "last_auth_response";

export function decodeJwtPayload(jwt) {
  try {
    if (!jwt) return null;
    const parts = jwt.split(".");
    if (parts.length < 2) return null;

    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(
      base64.length + ((4 - (base64.length % 4)) % 4),
      "="
    );
    const json = atob(padded);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function getToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY) || "";
}

export function getUser() {
  try {
    const raw = localStorage.getItem(AUTH_USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function getRole() {
  const u = getUser();
  return u?.role || u?.data?.role || ""; // tuỳ backend trả về
}

export function setSession({ token, user }) {
  if (token) localStorage.setItem(AUTH_TOKEN_KEY, token);
  else localStorage.removeItem(AUTH_TOKEN_KEY);

  if (user) localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
  else localStorage.removeItem(AUTH_USER_KEY);
}

export function clearSession() {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_USER_KEY);
}

export function setLastAuthResponse(resData) {
  try {
    sessionStorage.setItem(LAST_RESPONSE_KEY, JSON.stringify(resData ?? null));
  } catch {
    // ignore
  }
}

export function getLastAuthResponse() {
  try {
    const raw = sessionStorage.getItem(LAST_RESPONSE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function clearLastAuthResponse() {
  sessionStorage.removeItem(LAST_RESPONSE_KEY);
}
