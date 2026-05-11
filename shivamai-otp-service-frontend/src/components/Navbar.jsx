import { useNavigate, useLocation } from "react-router-dom";
import trident from "../assets/trident.png";

export default function Navbar() {

  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.clear();
    navigate("/auth");
  };

  const getTitle = () => {
    const path = location.pathname;

    // ================= ADMIN =================
    if (path === "/admin") return "Pending Developers";
    if (path.includes("/admin/all")) return "All Developers";
    if (path.includes("/admin/webhooks")) return "Webhook Logs";
    if (path.includes("/admin/usage")) return "Analytics";
    if (path.includes("/admin/apps")) return "Apps Management";

    // ================= USER =================
    if (path === "/dashboard") return "Dashboard";
    if (path === "/apps") return "My Apps";
    if (path === "/create-app") return "Create App";
    if (path === "/profile") return "Profile";

    return "ShivaMai";
  };

  return (
    <div className="topbar">

      <div className="topbar-left">
        <img src={trident} alt="icon" className="top-icon" />
        <h3>{getTitle()}</h3>
      </div>

      <button className="logout-btn" onClick={handleLogout}>
        Logout
      </button>

    </div>
  );
}