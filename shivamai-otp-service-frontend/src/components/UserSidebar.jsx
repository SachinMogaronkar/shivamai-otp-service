import { NavLink } from "react-router-dom";
import logo from "../assets/logo.png";

export default function UserSidebar() {
  return (
    <aside className="sidebar">

      <div className="brand">
        <img src={logo} alt="logo" className="brand-logo" />
      </div>

      <nav className="nav">
        <NavLink to="/dashboard">Dashboard</NavLink>
        <NavLink to="/apps">My Apps</NavLink>
        <NavLink to="/create-app">Create App</NavLink>
        <NavLink to="/profile">Profile</NavLink>
      </nav>

    </aside>
  );
}