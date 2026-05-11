import { useEffect, useState } from "react";
import { getSystemStatus } from "../services/adminService";
import "../styles/system.css";

export default function SystemStatus() {

  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    const fetchStatus = async () => {
      try {
        const res = await getSystemStatus();
        if (isMounted) setStatus(res?.data?.data);
      } catch {
        if (isMounted) {
          setStatus({
            status: "DOWN",
            components: {},
          });
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchStatus();
    return () => (isMounted = false);
  }, []);

  if (loading) {
    return <div className="system-card">Checking system...</div>;
  }

  if (!status) {
    return <div className="system-card">No status available</div>;
  }

  return (
    <div className="system-card">

      <h3>System Status</h3>

      <div className="system-main">
        <span className={`status-dot ${status.status.toLowerCase()}`} />
        <strong>{status.status}</strong>
      </div>

      <div className="system-list">
        {Object.entries(status.components || {}).map(([key, value]) => (
          <div key={key} className="system-item">
            <span>{key}</span>
            <span className={`status-text ${value.toLowerCase()}`}>
              {value}
            </span>
          </div>
        ))}
      </div>

    </div>
  );
}