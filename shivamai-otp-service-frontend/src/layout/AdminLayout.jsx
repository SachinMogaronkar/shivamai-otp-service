import { Outlet } from "react-router-dom";
import AdminSidebar from "../components/AdminSidebar";
import Navbar from "../components/Navbar";
import "../styles/layout.css";

export default function AdminLayout() {
  return (
    <div className="layout">

      <AdminSidebar />

      <div className="main">
        <Navbar />

        <div className="content">
          <Outlet />
        </div>
      </div>

    </div>
  );
}