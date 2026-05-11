import { jwtDecode } from "jwt-decode";

export const isTokenValid = (token, role) => {
  if (!token) return false;

  try {
    const decoded = jwtDecode(token);

    // ✅ allowed here (not React render)
    const now = Math.floor(Date.now() / 1000);

    if (decoded.exp && decoded.exp < now) {
      return false;
    }

    if (role && decoded.role !== role) {
      return false;
    }

    return true;

  } catch {
    return false;
  }
};