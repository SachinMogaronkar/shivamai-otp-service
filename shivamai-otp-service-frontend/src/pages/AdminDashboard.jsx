import { useEffect, useState } from "react";
import {
  getAllDevelopers,
  getAdminApps,
  getUsage,
} from "../services/adminService";
import SystemStatus from "../components/SystemStatus";
import StatCard from "../components/StatCard";
import "../styles/dashboard.css";

export default function AdminDashboard() {

  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    const fetchDashboard = async () => {
      try {
        const [devRes, appRes, usageRes] = await Promise.all([
          getAllDevelopers(),
          getAdminApps(),
          getUsage(),
        ]);

        const developers = devRes?.data?.data || [];
        const apps = appRes?.data?.data || [];
        const usage = usageRes?.data?.data || [];

        const totalRequests = usage.reduce(
          (sum, u) => sum + u.otpRequests,
          0
        );

        const totalVerified = usage.reduce(
          (sum, u) => sum + u.otpVerified,
          0
        );

        const successRate =
          totalRequests > 0
            ? ((totalVerified / totalRequests) * 100).toFixed(1)
            : 0;

        if (isMounted) {
          setStats({
            developers: developers.length,
            activeDevelopers: developers.filter(d => d.status === "ACTIVE").length,

            apps: apps.length,
            activeApps: apps.filter(a => a.status === "ACTIVE").length,

            totalRequests,
            successRate,
          });
        }

      } catch (err) {
        if (isMounted) {
          setError(
            err?.response?.data?.message || "Failed to load dashboard"
          );
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchDashboard();
    return () => (isMounted = false);
  }, []);

  /* ================= UI ================= */

  if (loading) return <p>Loading dashboard...</p>;
  if (error) return <p>{error}</p>;
  if (!stats) return <p>No data available</p>;

  return (
    <div className="dashboard">

      {/* HEADER */}
      <div className="dashboard-header">
        <h2>Admin Dashboard</h2>
      </div>

      {/* SYSTEM STATUS */}
      <SystemStatus />

      {/* KPI CARDS */}
      <div className="card-grid">

        <StatCard title="Total Developers" value={stats.developers} />

        <StatCard
          title="Active Developers"
          value={stats.activeDevelopers}
          color="green"
        />

        <StatCard title="Total Apps" value={stats.apps} />

        <StatCard
          title="Active Apps"
          value={stats.activeApps}
          color="green"
        />

        <StatCard title="Total OTP Requests" value={stats.totalRequests} />

        <StatCard
          title="Success Rate %"
          value={stats.successRate}
        />

      </div>

    </div>
  );
}