import { useEffect, useState } from "react";
import { getProfile } from "../services/appService";
import "../styles/dashboard.css";
import "../styles/developerprofile.css";

export default function DeveloperProfile() {

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    const fetchProfile = async () => {
      try {
        const res = await getProfile();
        if (isMounted) setProfile(res?.data?.data);
      } catch (err) {
        if (isMounted) {
          setError(err?.response?.data?.message || "Failed to load profile");
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchProfile();
    return () => (isMounted = false);
  }, []);

  if (loading) return <p>Loading profile...</p>;
  if (error) return <p>{error}</p>;
  if (!profile) return <p>No profile data</p>;

  return (
    <div className="profile">

      {/* HEADER */}
      <div className="profile-header">
        <div>
          <h2>Developer Profile</h2>
          <p className="profile-sub">Account overview and status</p>
        </div>

        <span className={`status-badge ${profile.status.toLowerCase()}`}>
          {profile.status}
        </span>
      </div>

      {/* MAIN CARD */}
      <div className="profile-card">

        <div className="profile-row">
          <span>Identifier</span>
          <strong>{profile.identifier}</strong>
        </div>

        <div className="profile-row">
          <span>Email Verified</span>
          <strong className={profile.emailVerified ? "text-green" : "text-red"}>
            {profile.emailVerified ? "Yes" : "No"}
          </strong>
        </div>

        <div className="profile-row">
          <span>Account Created</span>
          <strong>
            {new Date(profile.createdAt).toLocaleString()}
          </strong>
        </div>

      </div>

    </div>
  );
}