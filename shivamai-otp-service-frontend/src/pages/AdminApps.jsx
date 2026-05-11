import { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  getAdminApps,
  activateApp,
  suspendApp,
} from "../services/adminService";
import "../styles/table.css";

export default function AdminApps() {

  const navigate = useNavigate();

  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [actionLoading, setActionLoading] = useState(null);

  // filters
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

  /* ================= FETCH ================= */

  useEffect(() => {
    let isMounted = true;

    const fetchApps = async () => {
      try {
        const res = await getAdminApps();
        if (isMounted) {
          setApps(res?.data?.data || []);
        }
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

  /* ================= FILTER ================= */

  const filtered = useMemo(() => {
    return apps.filter((app) => {

      const matchesSearch =
        app.appName.toLowerCase().includes(search.toLowerCase()) ||
        app.clientId.toLowerCase().includes(search.toLowerCase());

      const matchesStatus =
        !statusFilter || app.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [apps, search, statusFilter]);

  /* ================= ACTIONS ================= */

  const handleActivate = async (id) => {
    try {
      setActionLoading(id);

      await activateApp(id);

      setApps((prev) =>
        prev.map((app) =>
          app.id === id ? { ...app, status: "ACTIVE" } : app
        )
      );

    } catch (err) {
      alert(err?.response?.data?.message || "Activate failed");
    } finally {
      setActionLoading(null);
    }
  };

  const handleSuspend = async (id) => {
    try {
      setActionLoading(id);

      await suspendApp(id);

      setApps((prev) =>
        prev.map((app) =>
          app.id === id ? { ...app, status: "SUSPENDED" } : app
        )
      );

    } catch (err) {
      alert(err?.response?.data?.message || "Suspend failed");
    } finally {
      setActionLoading(null);
    }
  };

  /* ================= UI ================= */

  if (loading) return <p>Loading apps...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>

      <h2>Admin Apps</h2>

      {/* ================= FILTER BAR ================= */}

      <div className="filter-bar">

        <input
          type="text"
          placeholder="Search by name or client ID..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All Status</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="SUSPENDED">SUSPENDED</option>
        </select>

      </div>

      {/* ================= TABLE ================= */}

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

            {filtered.length === 0 ? (
              <tr>
                <td colSpan="4">No apps found</td>
              </tr>
            ) : (
              filtered.map((app) => (
                <tr
                  key={app.id}
                  onClick={() => navigate(`/admin/apps/${app.id}`)}
                  style={{ cursor: "pointer" }}
                >
                  <td>{app.appName}</td>

                  <td>{app.clientId}</td>

                  <td
                    className={`status-${app.status
                      .toLowerCase()
                      .replace(/_/g, "-")}`}
                  >
                    {app.status}
                  </td>

                  <td>
                    <div className="action-group">

                      {app.status === "ACTIVE" && (
                        <button
                          className="btn suspend"
                          disabled={actionLoading === app.id}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleSuspend(app.id);
                          }}
                        >
                          {actionLoading === app.id ? "..." : "Suspend"}
                        </button>
                      )}

                      {app.status === "SUSPENDED" && (
                        <button
                          className="btn approve"
                          disabled={actionLoading === app.id}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleActivate(app.id);
                          }}
                        >
                          {actionLoading === app.id ? "..." : "Activate"}
                        </button>
                      )}

                    </div>
                  </td>
                </tr>
              ))
            )}

          </tbody>
        </table>

      </div>

    </div>
  );
}