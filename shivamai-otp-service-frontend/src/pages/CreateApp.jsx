import { useState } from "react";
import { createApp } from "../services/appService";
import "../styles/form.css";

export default function CreateApp() {
  const [appName, setAppName] = useState("");
  const [webhookUrl, setWebhookUrl] = useState("");

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleCreate = async () => {
    setError("");
    setResult(null);

    if (!appName || !webhookUrl) {
      setError("All fields are required");
      return;
    }

    setLoading(true);

    try {
      const res = await createApp({
        appName,
        webhookUrl,
      });

      const data = res.data.data;

      if (!data || !data.clientId || !data.secret) {
        throw new Error("Invalid response from server");
      }

      setResult(data);

      // reset inputs after success
      setAppName("");
      setWebhookUrl("");
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to create app");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container">
      <h2>Create App</h2>

      {error && <p style={{ color: "red" }}>{error}</p>}

      <input
        placeholder="App Name"
        value={appName}
        onChange={(e) => setAppName(e.target.value)}
      />

      <input
        placeholder="Webhook URL"
        value={webhookUrl}
        onChange={(e) => setWebhookUrl(e.target.value)}
      />

      <button onClick={handleCreate} disabled={loading}>
        {loading ? "Creating..." : "Create App"}
      </button>

      {result && (
        <div className="result-box">
          <h4>Credentials</h4>
          <p><b>Client ID:</b> {result.clientId}</p>
          <p><b>Client Secret:</b> {result.secret}</p>
        </div>
      )}
    </div>
  );
}