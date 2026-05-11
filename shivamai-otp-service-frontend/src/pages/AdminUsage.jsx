import { useEffect, useState, useMemo } from "react";
import { getUsage } from "../services/adminService";
import UsageChart from "../components/UsageChart";
import StatCard from "../components/StatCard";
import "../styles/dashboard.css";

export default function AdminUsage() {

  const [usage, setUsage] = useState([]);

  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [clientId, setClientId] = useState("");

  const [loading, setLoading] = useState(true);

  // ================= FETCH DATA =================
  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await getUsage();
        setUsage(res?.data?.data || []);
      } catch (err) {
        console.error("Failed to fetch usage:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // ================= FILTER LOGIC =================
  const filtered = useMemo(() => {
    let data = [...usage];

    if (clientId) {
      data = data.filter(u => u.clientId === clientId);
    }

    if (fromDate) {
      data = data.filter(u => u.date >= fromDate);
    }

    if (toDate) {
      data = data.filter(u => u.date <= toDate);
    }

    return data;
  }, [usage, clientId, fromDate, toDate]);

  // ================= KPI CALCULATIONS =================
  const totalRequests = filtered.reduce((sum, u) => sum + u.otpRequests, 0);

  const totalVerified = filtered.reduce((sum, u) => sum + u.otpVerified, 0);

  const totalFailed = totalRequests - totalVerified;

  const successRate =
    totalRequests > 0
      ? ((totalVerified / totalRequests) * 100).toFixed(1)
      : 0;

  // ================= CLIENT DROPDOWN =================
  const clients = [...new Set(usage.map(u => u.clientId))];

  // ================= UI =================

  if (loading) {
    return <div className="loading">Loading dashboard...</div>;
  }

  return (
    <div className="dashboard">

      {/* HEADER */}
      <div className="dashboard-header">
        <h2>Platform Analytics</h2>
      </div>

      {/* FILTER BAR */}
      <div className="filter-bar">

        <input
          type="date"
          value={fromDate}
          onChange={(e) => setFromDate(e.target.value)}
        />

        <input
          type="date"
          value={toDate}
          onChange={(e) => setToDate(e.target.value)}
        />

        <select
          value={clientId}
          onChange={(e) => setClientId(e.target.value)}
        >
          <option value="">All Clients</option>
          {clients.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>

      </div>

      {/* KPI CARDS */}
      <div className="card-grid">

        <StatCard title="Total OTPs" value={totalRequests} />

        <StatCard title="Verified" value={totalVerified} color="green" />

        <StatCard title="Failed" value={totalFailed} color="red" />

        <StatCard title="Success Rate %" value={successRate} />

      </div>

      {/* CHARTS */}
      <UsageChart data={filtered} />

    </div>
  );
}