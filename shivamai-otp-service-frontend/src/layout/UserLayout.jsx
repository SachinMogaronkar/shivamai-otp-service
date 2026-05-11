import { Outlet } from "react-router-dom";
import UserSidebar from "../components/UserSidebar";
import Navbar from "../components/Navbar";
import "../styles/layout.css";

export default function UserLayout() {
  return (
    <div className="layout">

      <UserSidebar />

      <div className="main">
        <Navbar />

        <div className="content">
          <Outlet />
        </div>
      </div>

    </div>
  );
}