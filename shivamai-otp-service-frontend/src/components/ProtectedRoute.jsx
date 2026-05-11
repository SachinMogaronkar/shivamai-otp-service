import { Navigate } from "react-router-dom";
import { isTokenValid } from "../utils/authUtils";

export default function ProtectedRoute({ children, role }) {

  const token = localStorage.getItem("token");

  const valid = isTokenValid(token, role);

  if (!valid) {
    localStorage.removeItem("token");
    return <Navigate to="/auth" replace />;
  }

  return children;
}