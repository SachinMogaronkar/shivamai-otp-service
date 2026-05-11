import { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  getAllDevelopers,
  suspendDeveloper,
  approveDeveloper,
} from "../services/adminService";
import "../styles/table.css";

export default function AllDevelopers() {

  const navigate = useNavigate();

  const [developers, setDevelopers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [actionLoading, setActionLoading] = useState(null);

  // filters
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

  /* ================= FETCH ================= */

  useEffect(() => {
    let isMounted = true;

    const fetchData = async () => {
      try {
        const res = await getAllDevelopers();
        if (isMounted) {
          setDevelopers(res?.data?.data || []);
        }
      } catch (err) {
        if (isMounted) {
          setError(
            err?.response?.data?.message || "Failed to load developers"
          );
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchData();
    return () => (isMounted = false);
  }, []);

  /* ================= FILTER ================= */

  const filtered = useMemo(() => {
    return developers.filter((dev) => {

      const matchesSearch =
        dev.identifier.toLowerCase().includes(search.toLowerCase());

      const matchesStatus =
        !statusFilter || dev.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [developers, search, statusFilter]);

  /* ================= ACTIONS ================= */

  const handleSuspend = async (id) => {
    try {
      setActionLoading(id);

      await suspendDeveloper(id);

      setDevelopers((prev) =>
        prev.map((dev) =>
          dev.id === id ? { ...dev, status: "SUSPENDED" } : dev
        )
      );

    } catch (err) {
      alert(err?.response?.data?.message || "Suspend failed");
    } finally {
      setActionLoading(null);
    }
  };

  const handleApprove = async (id) => {
    try {
      setActionLoading(id);

      await approveDeveloper(id);

      setDevelopers((prev) =>
        prev.map((dev) =>
          dev.id === id ? { ...dev, status: "ACTIVE" } : dev
        )
      );

    } catch (err) {
      alert(err?.response?.data?.message || "Approve failed");
    } finally {
      setActionLoading(null);
    }
  };

  /* ================= UI ================= */

  if (loading) return <p>Loading developers...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>

      <h2>All Developers</h2>

      {/* ================= FILTER BAR ================= */}

      <div className="filter-bar">

        <input
          type="text"
          placeholder="Search by email..."
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
          <option value="PENDING_ADMIN_APPROVAL">PENDING</option>
        </select>

      </div>

      {/* ================= TABLE ================= */}

      <div className="table-container">

        <table className="table">
          <thead>
            <tr>
              <th>Email</th>
              <th>Status</th>
              <th>Verified</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>

            {filtered.length === 0 ? (
              <tr>
                <td colSpan="4">No developers found</td>
              </tr>
            ) : (
              filtered.map((dev) => (
                <tr
                  key={dev.id}
                  onClick={() => navigate(`/admin/developers/${dev.id}`)}
                  style={{ cursor: "pointer" }}
                >
                  <td>{dev.identifier}</td>

                  <td
                    className={`status-${dev.status
                      .toLowerCase()
                      .replace(/_/g, "-")}`}
                  >
                    {dev.status}
                  </td>

                  <td>
                    {dev.emailVerified ? "Yes" : "No"}
                  </td>

                  <td>
                    <div className="action-group">

                      {/* PENDING */}
                      {dev.status === "PENDING_ADMIN_APPROVAL" && (
                        <>
                          <button
                            className="btn approve"
                            disabled={actionLoading === dev.id}
                            onClick={(e) => {
                              e.stopPropagation();
                              handleApprove(dev.id);
                            }}
                          >
                            {actionLoading === dev.id ? "..." : "Approve"}
                          </button>

                          <button
                            className="btn suspend"
                            disabled={actionLoading === dev.id}
                            onClick={(e) => {
                              e.stopPropagation();
                              handleSuspend(dev.id);
                            }}
                          >
                            {actionLoading === dev.id ? "..." : "Reject"}
                          </button>
                        </>
                      )}

                      {/* ACTIVE */}
                      {dev.status === "ACTIVE" && (
                        <button
                          className="btn suspend"
                          disabled={actionLoading === dev.id}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleSuspend(dev.id);
                          }}
                        >
                          {actionLoading === dev.id ? "..." : "Suspend"}
                        </button>
                      )}

                      {/* SUSPENDED */}
                      {dev.status === "SUSPENDED" && (
                        <button
                          className="btn approve"
                          disabled={actionLoading === dev.id}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleApprove(dev.id);
                          }}
                        >
                          {actionLoading === dev.id ? "..." : "Activate"}
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