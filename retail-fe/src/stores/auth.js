import { ref } from "vue";

export const AUTH_TOKEN_KEY = "access_token";
export const AUTH_USER_KEY = "auth_user";
export const LAST_RESPONSE_KEY = "last_auth_response";

const S = {
  get(key) {
    return sessionStorage.getItem(key);
  },
  set(key, val) {
    sessionStorage.setItem(key, val);
  },
  remove(key) {
    sessionStorage.removeItem(key);
  },
};

function safeJsonParse(raw) {
  try {
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

// ✅ Reactive state (khởi tạo từ sessionStorage)
const tokenRef = ref(S.get(AUTH_TOKEN_KEY) || "");
const userRef = ref(safeJsonParse(S.get(AUTH_USER_KEY)));
const lastRespRef = ref(safeJsonParse(S.get(LAST_RESPONSE_KEY)));

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
    return JSON.parse(atob(padded));
  } catch {
    return null;
  }
}

export function getToken() {
  return tokenRef.value || "";
}

export function getUser() {
  return userRef.value || null;
}

export function getRole() {
  const u = userRef.value;
  return (u?.role || u?.data?.role || "").toString();
}

export function setSession({ token, user }) {
  if (token) {
    S.set(AUTH_TOKEN_KEY, token);
    tokenRef.value = token;
  } else {
    S.remove(AUTH_TOKEN_KEY);
    tokenRef.value = "";
  }

  if (user) {
    S.set(AUTH_USER_KEY, JSON.stringify(user));
    userRef.value = user;
  } else {
    S.remove(AUTH_USER_KEY);
    userRef.value = null;
  }
}

export function clearSession() {
  S.remove(AUTH_TOKEN_KEY);
  S.remove(AUTH_USER_KEY);
  tokenRef.value = "";
  userRef.value = null;
}

export function setLastAuthResponse(resData) {
  try {
    S.set(LAST_RESPONSE_KEY, JSON.stringify(resData ?? null));
    lastRespRef.value = resData ?? null;
  } catch {}
}

export function getLastAuthResponse() {
  return lastRespRef.value ?? null;
}

export function clearLastAuthResponse() {
  S.remove(LAST_RESPONSE_KEY);
  lastRespRef.value = null;
}
