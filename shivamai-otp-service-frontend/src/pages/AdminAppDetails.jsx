import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getAdminAppById, activateApp, suspendApp } from "../services/adminService";
import "../styles/developerprofile.css";

export default function AdminAppDetails() {

  const { id } = useParams();

  const [app, setApp] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(false);

  /* ================= FETCH ================= */

  useEffect(() => {
    let isMounted = true;

    const fetchApp = async () => {
      try {
        const res = await getAdminAppById(id);
        if (isMounted) setApp(res?.data?.data);
      } catch (err) {
        if (isMounted) {
          setError(err?.response?.data?.message || "Failed to load app");
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchApp();
    return () => (isMounted = false);
  }, [id]);

  /* ================= ACTIONS ================= */

  const handleActivate = async () => {
    try {
      setActionLoading(true);
      await activateApp(id);

      setApp((prev) => ({
        ...prev,
        status: "ACTIVE",
      }));

    } catch (err) {
      alert(err?.response?.data?.message || "Activate failed");
    } finally {
      setActionLoading(false);
    }
  };

  const handleSuspend = async () => {
    try {
      setActionLoading(true);
      await suspendApp(id);

      setApp((prev) => ({
        ...prev,
        status: "SUSPENDED",
      }));

    } catch (err) {
      alert(err?.response?.data?.message || "Suspend failed");
    } finally {
      setActionLoading(false);
    }
  };

  /* ================= UI ================= */

  if (loading) return <p>Loading app...</p>;
  if (error) return <p>{error}</p>;
  if (!app) return <p>No app data</p>;

  return (
    <div className="profile">

      {/* HEADER */}
      <div className="profile-header">
        <div>
          <h2>App Details</h2>
          <p className="profile-sub">Application information and status</p>
        </div>

        <span className={`status-badge ${app.status.toLowerCase()}`}>
          {app.status}
        </span>
      </div>

      {/* CARD */}
      <div className="profile-card">

        <div className="profile-row">
          <span>ID</span>
          <strong>{app.id}</strong>
        </div>

        <div className="profile-row">
          <span>App Name</span>
          <strong>{app.appName}</strong>
        </div>

        <div className="profile-row">
          <span>Client ID</span>
          <strong>{app.clientId}</strong>
        </div>

        <div className="profile-row">
          <span>Status</span>
          <strong>{app.status}</strong>
        </div>

        <div className="profile-row">
          <span>Created At</span>
          <strong>
            {new Date(app.createdAt).toLocaleString()}
          </strong>
        </div>

      </div>

      {/* ACTIONS */}
      <div style={{ marginTop: "20px" }}>

        {app.status === "ACTIVE" && (
          <button
            className="btn suspend"
            onClick={handleSuspend}
            disabled={actionLoading}
          >
            {actionLoading ? "..." : "Suspend App"}
          </button>
        )}

        {app.status === "SUSPENDED" && (
          <button
            className="btn approve"
            onClick={handleActivate}
            disabled={actionLoading}
          >
            {actionLoading ? "..." : "Activate App"}
          </button>
        )}

      </div>

    </div>
  );
}