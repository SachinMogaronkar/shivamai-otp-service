import { useState } from "react";
import { requestOtp, verifyOtp } from "../services/clientService";
import "../styles/form.css";

export default function ClientTest() {
  const [clientId, setClientId] = useState("");
  const [secret, setSecret] = useState("");

  const [identifier, setIdentifier] = useState("");
  const [otp, setOtp] = useState("");
  const [requestId, setRequestId] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSendOtp = async () => {
    setError("");
    setSuccess("");

    if (!clientId || !secret || !identifier) {
      setError("Client ID, Secret and Identifier are required");
      return;
    }

    setLoading(true);

    try {
      const res = await requestOtp(clientId, secret, {
        identifier,
        type: "LOGIN",
      });

      const reqId = res.data.data.requestId;

      if (!reqId) {
        throw new Error("Invalid response from server");
      }

      setRequestId(reqId);
      setSuccess("OTP sent successfully");
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to send OTP");
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async () => {
    setError("");
    setSuccess("");

    if (!clientId || !secret || !identifier || !otp || !requestId) {
      setError("All fields including requestId are required");
      return;
    }

    setLoading(true);

    try {
      await verifyOtp(clientId, secret, {
        identifier,
        requestId,
        otp,
      });

      setSuccess("OTP verified successfully");
    } catch (err) {
      setError(err?.response?.data?.message || "Verification failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container">
      <h2>Client OTP Test</h2>

      {error && <p style={{ color: "red" }}>{error}</p>}
      {success && <p style={{ color: "green" }}>{success}</p>}

      <input
        placeholder="Client ID"
        value={clientId}
        onChange={(e) => setClientId(e.target.value)}
      />

      <input
        placeholder="Client Secret"
        value={secret}
        onChange={(e) => setSecret(e.target.value)}
      />

      <input
        placeholder="User Identifier (Email/Phone)"
        value={identifier}
        onChange={(e) => setIdentifier(e.target.value)}
      />

      <button onClick={handleSendOtp} disabled={loading}>
        {loading ? "Sending..." : "Send OTP"}
      </button>

      <input
        placeholder="Request ID"
        value={requestId}
        onChange={(e) => setRequestId(e.target.value)}
      />

      <input
        placeholder="Enter OTP"
        value={otp}
        onChange={(e) => setOtp(e.target.value)}
      />

      <button onClick={handleVerifyOtp} disabled={loading}>
        {loading ? "Verifying..." : "Verify OTP"}
      </button>
    </div>
  );
}