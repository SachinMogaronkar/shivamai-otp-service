import { useEffect, useState, useMemo } from "react";
import { getApps, rotateSecret, deleteApp } from "../services/appService";
import "../styles/table.css";
import "../styles/dashboard.css";

export default function Apps() {

  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(null);

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

  useEffect(() => {
    let isMounted = true;

    const fetchApps = async () => {
      try {
        const res = await getApps();
        if (isMounted) setApps(res?.data?.data || []);
      } catch (err) {
        if (isMounted) {
          setError(err?.response?.data?.message || "Failed to load apps");
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchApps();
    return () => (isMounted = false);
  }, []);

  const filtered = useMemo(() => {
    let data = [...apps];

    if (search.trim()) {
      data = data.filter(app =>
        app.appName.toLowerCase().includes(search.toLowerCase()) ||
        app.clientId.toLowerCase().includes(search.toLowerCase())
      );
    }

    if (statusFilter) {
      data = data.filter(app => app.status === statusFilter);
    }

    return data;
  }, [apps, search, statusFilter]);

  /* ================= ROTATE ================= */

  const handleRotate = async (id) => {
    if (!window.confirm("Rotate secret? Old secret will stop working.")) return;

    try {
      setActionLoading(id);

      const res = await rotateSecret(id);
      const newSecret = res?.data?.data;

      alert(`New Secret: ${newSecret}`);

    } catch (err) {
      alert(err?.response?.data?.message || "Rotate failed");
    } finally {
      setActionLoading(null);
    }
  };

  /* ================= DELETE ================= */

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this app permanently?")) return;

    try {
      setActionLoading(id);

      await deleteApp(id);
      setApps(prev => prev.filter(app => app.id !== id));

    } catch (err) {
      alert(err?.response?.data?.message || "Delete failed");
    } finally {
      setActionLoading(null);
    }
  };

  if (loading) return <p>Loading apps...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>
      <h2>Your Apps</h2>

      {/* 🔥 SAME FILTER BAR AS ADMIN */}
      <div className="filter-bar">

        <input
          type="text"
          placeholder="Search app or client ID..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="SUSPENDED">Suspended</option>
        </select>

        <button
          onClick={() => {
            setSearch("");
            setStatusFilter("");
          }}
        >
          Reset
        </button>

      </div>

      <p>Showing {filtered.length} results</p>

      {filtered.length === 0 ? (
        <p>No apps found</p>
      ) : (
        <div className="table-container">
          <table className="table">

            <thead>
              <tr>
                <th>App Name</th>
                <th>Client ID</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              {filtered.map(app => (
                <tr key={app.id}>

                  <td>{app.appName}</td>
                  <td>{app.clientId}</td>

                  <td className={`status-${app.status.toLowerCase()}`}>
                    {app.status}
                  </td>

                  <td>
                    <div className="action-group">

                      <button
                        className="btn activate"
                        disabled={actionLoading === app.id}
                        onClick={() => handleRotate(app.id)}
                      >
                        {actionLoading === app.id ? "..." : "Rotate"}
                      </button>

                      <button
                        className="btn reject"
                        disabled={actionLoading === app.id}
                        onClick={() => handleDelete(app.id)}
                      >
                        {actionLoading === app.id ? "..." : "Delete"}
                      </button>

                    </div>
                  </td>

                </tr>
              ))}
            </tbody>

          </table>
        </div>
      )}
    </div>
  );
}