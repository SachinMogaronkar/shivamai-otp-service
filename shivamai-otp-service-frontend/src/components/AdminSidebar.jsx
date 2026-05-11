import { NavLink } from "react-router-dom";
import logo from "../assets/logo.png";

export default function AdminSidebar() {
  return (
    <aside className="sidebar">

      <div className="brand">
        <img src={logo} alt="logo" className="brand-logo" />
      </div>

      <nav className="nav">
        <NavLink to="/admin/dashboard">Dashboard</NavLink>
        <NavLink to="/admin/pending">Pending</NavLink>
        <NavLink to="/admin/all">Developers</NavLink>
        <NavLink to="/admin/apps">Apps</NavLink>
        <NavLink to="/admin/webhooks">Webhooks</NavLink>
        <NavLink to="/admin/usage">Analytics</NavLink>
      </nav>

    </aside>
  );
}