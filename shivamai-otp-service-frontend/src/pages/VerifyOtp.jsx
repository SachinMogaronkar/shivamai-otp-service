import { useState, useEffect } from "react";
import { verifyRegistration } from "../services/authService";
import { useLocation, useNavigate } from "react-router-dom";
import "../styles/form.css";

export default function VerifyOtp() {

  const [otp, setOtp] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const location = useLocation();
  const navigate = useNavigate();

  const identifier = location.state?.identifier;

  // 🔥 prevent direct access
  useEffect(() => {
    if (!identifier) {
      navigate("/register");
    }
  }, [identifier, navigate]);

  const handleVerify = async () => {

    setError("");

    const trimmedOtp = otp.trim();

    if (!trimmedOtp) {
      setError("Enter OTP");
      return;
    }

    setLoading(true);

    try {
      const res = await verifyRegistration({
        identifier,
        otp: trimmedOtp
      });

      const responseData = res?.data;

      if (!responseData) {
        throw new Error("Invalid server response");
      }

      // ✅ move to login after success
      navigate("/login");

    } catch (err) {

      const message =
        err?.response?.data?.message ||
        err?.message ||
        "Verification failed";

      setError(message);

    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container">
      <h2>Verify Registration OTP</h2>

      {error && <p className="error-text">{error}</p>}

      <p style={{ fontSize: "14px" }}>
        Identifier: <b>{identifier}</b>
      </p>

      <input
        placeholder="Enter OTP"
        value={otp}
        onChange={(e) => setOtp(e.target.value)}
      />

      <button onClick={handleVerify} disabled={loading}>
        {loading ? "Verifying..." : "Verify"}
      </button>
    </div>
  );
}