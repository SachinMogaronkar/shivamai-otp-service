import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getDeveloperById } from "../services/adminService";
import "../styles/dashboard.css";
import "../styles/developerprofile.css";

export default function AdminDeveloperDetails() {

  const { id } = useParams();

  const [developer, setDeveloper] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    const fetchDeveloper = async () => {
      try {
        const res = await getDeveloperById(id);
        if (isMounted) setDeveloper(res?.data?.data);
      } catch (err) {
        if (isMounted) {
          setError(err?.response?.data?.message || "Failed to load developer");
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchDeveloper();
    return () => (isMounted = false);
  }, [id]);

  if (loading) return <p>Loading developer...</p>;
  if (error) return <p>{error}</p>;
  if (!developer) return <p>No data found</p>;

  return (
    <div className="profile">

      {/* HEADER */}
      <div className="profile-header">
        <div>
          <h2>Developer Details</h2>
          <p className="profile-sub">Full account information</p>
        </div>

        <span className={`status-badge ${developer.status.toLowerCase()}`}>
          {developer.status}
        </span>
      </div>

      {/* CARD */}
      <div className="profile-card">

        <div className="profile-row">
          <span>ID</span>
          <strong>{developer.id}</strong>
        </div>

        <div className="profile-row">
          <span>Email / Identifier</span>
          <strong>{developer.identifier}</strong>
        </div>

        <div className="profile-row">
          <span>Email Verified</span>
          <strong className={developer.emailVerified ? "text-green" : "text-red"}>
            {developer.emailVerified ? "Yes" : "No"}
          </strong>
        </div>

        <div className="profile-row">
          <span>Status</span>
          <strong>{developer.status}</strong>
        </div>

        <div className="profile-row">
          <span>Created At</span>
          <strong>
            {new Date(developer.createdAt).toLocaleString()}
          </strong>
        </div>

      </div>

    </div>
  );
}