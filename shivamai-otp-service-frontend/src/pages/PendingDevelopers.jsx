import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getPendingDevelopers,
  approveDeveloper,
  suspendDeveloper,
} from "../services/adminService";
import "../styles/table.css";

export default function PendingDevelopers() {

  const navigate = useNavigate();

  const [developers, setDevelopers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [actionLoading, setActionLoading] = useState(null);

  /* ================= FETCH ================= */

  useEffect(() => {
    let isMounted = true;

    const fetchDevelopers = async () => {
      try {
        const res = await getPendingDevelopers();
        if (isMounted) {
          setDevelopers(res?.data?.data || []);
        }
      } catch (err) {
        if (isMounted) {
          setError(
            err?.response?.data?.message ||
            "Failed to load developers"
          );
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchDevelopers();
    return () => (isMounted = false);
  }, []);

  /* ================= ACTIONS ================= */

  const handleApprove = async (id) => {
    try {
      setActionLoading(id);

      await approveDeveloper(id);

      // remove from pending list
      setDevelopers((prev) =>
        prev.filter((dev) => dev.id !== id)
      );

    } catch (err) {
      alert(err?.response?.data?.message || "Approve failed");
    } finally {
      setActionLoading(null);
    }
  };

  const handleSuspend = async (id) => {
    try {
      setActionLoading(id);

      await suspendDeveloper(id);

      // remove from pending list
      setDevelopers((prev) =>
        prev.filter((dev) => dev.id !== id)
      );

    } catch (err) {
      alert(err?.response?.data?.message || "Suspend failed");
    } finally {
      setActionLoading(null);
    }
  };

  /* ================= UI ================= */

  if (loading) return <p>Loading pending developers...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>

      <h2>Pending Developers</h2>

      <div className="table-container">

        <table className="table">
          <thead>
            <tr>
              <th>Email</th>
              <th>Verified</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>

            {developers.length === 0 ? (
              <tr>
                <td colSpan="3">No pending developers</td>
              </tr>
            ) : (
              developers.map((dev) => (
                <tr
                  key={dev.id}
                  onClick={() => navigate(`/admin/developers/${dev.id}`)}
                  style={{ cursor: "pointer" }}
                >

                  <td>{dev.identifier}</td>

                  <td>
                    {dev.emailVerified ? "Yes" : "No"}
                  </td>

                  <td>
                    <div className="action-group">

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