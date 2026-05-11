import { jwtDecode } from "jwt-decode";

export const getRoleFromToken = (token) => {
  try {
    const decoded = jwtDecode(token);
    return decoded.role || decoded.roles || null;

  } catch (err) {
    console.error("JWT decode failed:", err);
    return null;
  }
};

export const getToken = () => {
  return localStorage.getItem("adminToken") || localStorage.getItem("token");
};