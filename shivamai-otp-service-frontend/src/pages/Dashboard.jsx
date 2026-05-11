import { useEffect, useState } from "react";
import { getApps } from "../services/appService";
import "../styles/dashboard.css";

export default function Dashboard() {

  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {

    const fetchApps = async () => {
      try {
        const res = await getApps();
        setApps(res?.data?.data || []);
      } catch (err) {
        setError(err?.response?.data?.message || "Failed to load apps");
      } finally {
        setLoading(false);
      }
    };

    fetchApps();

  }, []);

  const total = apps.length;
  const active = apps.filter(a => a.status === "ACTIVE").length;
  const suspended = apps.filter(a => a.status === "SUSPENDED").length;

  if (loading) return <p>Loading dashboard...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="dashboard">

      <div className="card-grid">
        <div className="card">
          <h3>Total Apps</h3>
          <p>{total}</p>
        </div>

        <div className="card green">
          <h3>Active Apps</h3>
          <p>{active}</p>
        </div>

        <div className="card red">
          <h3>Suspended</h3>
          <p>{suspended}</p>
        </div>
      </div>

      <div className="table-container">
        <h3>Recent Apps</h3>

        <table>
          <thead>
            <tr>
              <th>App Name</th>
              <th>Client ID</th>
              <th>Status</th>
            </tr>
          </thead>

          <tbody>
            {apps.slice(0, 5).map(app => (
              <tr key={app.id}>
                <td>{app.appName}</td>
                <td>{app.clientId}</td>
                <td>{app.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

    </div>
  );
}