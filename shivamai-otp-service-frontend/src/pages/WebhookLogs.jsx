import { useEffect, useState, useMemo } from "react";
import { getWebhookLogs } from "../services/adminService";
import "../styles/table.css";

export default function WebhookLogs() {

  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // filters
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

  /* ================= FETCH ================= */

  useEffect(() => {
    let isMounted = true;

    const fetchLogs = async () => {
      try {
        const res = await getWebhookLogs();
        if (isMounted) {
          setLogs(res?.data?.data || []);
        }
      } catch (err) {
        if (isMounted) {
          setError(
            err?.response?.data?.message || "Failed to load logs"
          );
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchLogs();
    return () => (isMounted = false);
  }, []);

  /* ================= FILTER ================= */

  const filtered = useMemo(() => {
    return logs.filter((log) => {

      const matchesSearch =
        log.url.toLowerCase().includes(search.toLowerCase());

      const matchesStatus =
        !statusFilter || log.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [logs, search, statusFilter]);

  /* ================= UI ================= */

  if (loading) return <p>Loading logs...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>

      <h2>Webhook Logs</h2>

      {/* ================= FILTER BAR ================= */}

      <div className="filter-bar">

        <input
          type="text"
          placeholder="Search by URL..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All Status</option>
          <option value="SUCCESS">SUCCESS</option>
          <option value="FAILED">FAILED</option>
          <option value="RETRYING">RETRYING</option>
        </select>

      </div>

      {/* ================= TABLE ================= */}

      <div className="table-container">

        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Status</th>
              <th>URL</th>
              <th>Retry</th>
              <th>Payload</th>
            </tr>
          </thead>

          <tbody>

            {filtered.length === 0 ? (
              <tr>
                <td colSpan="5">No logs found</td>
              </tr>
            ) : (
              filtered.map((log) => (
                <tr key={log.id}>

                  <td>{log.id}</td>

                  <td
                    className={`status-${log.status
                      .toLowerCase()
                      .replace(/_/g, "-")}`}
                  >
                    {log.status}
                  </td>

                  <td>{log.url}</td>

                  <td>{log.retryCount}</td>

                  <td>
                    <div className="payload-box">
                      {log.payload}
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