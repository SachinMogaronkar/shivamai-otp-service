import api from "../api/axios";

/* ================= AUTH ================= */

export const login = (data) =>
  api.post("/auth/login", data);

export const verifyLogin = (data) =>
  api.post("/auth/verify-login", data);

export const resendOtp = (data) =>
  api.post("/auth/resend-login", data);

/* ================= REGISTER ================= */

export const register = (data) =>
  api.post("/auth/register", data);

export const verifyRegistration = (data) =>
  api.post("/auth/verify-registration", data);

/* ================= LOGOUT ================= */

export const logout = () =>
  api.post("/auth/logout");