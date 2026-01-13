const KEY = "access_token";

export const setAccessToken = (token) => localStorage.setItem(KEY, token);
export const getAccessToken = () => localStorage.getItem(KEY);
export const clearAccessToken = () => localStorage.removeItem(KEY);
