import { useState, useEffect, useRef } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { verifyLogin, resendOtp } from "../services/authService";
import { jwtDecode } from "jwt-decode";
import "../styles/otp.css";

export default function VerifyLogin() {

  const [otp, setOtp] = useState(["", "", "", "", "", ""]);
  const inputsRef = useRef([]);

  const requestIdRef = useRef(null);

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [timer, setTimer] = useState(30);
  const [resending, setResending] = useState(false);

  const location = useLocation();
  const navigate = useNavigate();

  const identifier = location.state?.identifier;

  /* ================= INIT ================= */

  useEffect(() => {
    if (!identifier || !location.state?.requestId) {
      navigate("/auth");
      return;
    }

    requestIdRef.current = location.state.requestId;

  }, [location, identifier, navigate]);

  /* ================= TIMER ================= */

  useEffect(() => {
    if (timer <= 0) return;

    const interval = setInterval(() => {
      setTimer((t) => t - 1);
    }, 1000);

    return () => clearInterval(interval);
  }, [timer]);

  /* ================= OTP INPUT ================= */

  const handleChange = (value, index) => {
    if (!/^[0-9]?$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    if (value && index < 5) {
      inputsRef.current[index + 1].focus();
    }
  };

  const handleKeyDown = (e, index) => {
    if (e.key === "Backspace" && !otp[index] && index > 0) {
      inputsRef.current[index - 1].focus();
    }
  };

  const getOtpString = () => otp.join("");

  /* ================= VERIFY ================= */

  const handleVerify = async () => {

    setError("");

    const finalOtp = getOtpString();

    if (finalOtp.length !== 6) {
      setError("Enter complete OTP");
      return;
    }

    setLoading(true);

    try {

      const res = await verifyLogin({
        identifier,
        requestId: requestIdRef.current,
        otp: finalOtp
      });

      const token = res?.data?.data;

      const role = jwtDecode(token).role;

      localStorage.setItem("token", token);

      console.log("TOKEN:", token);
      console.log("ROLE:", role);

      if (role === "ADMIN") navigate("/admin/dashboard");
      else navigate("/dashboard");

    } catch (err) {
      setError(err?.response?.data?.message || "Verification failed");
    } finally {
      setLoading(false);
    }
  };

  /* ================= RESEND ================= */

  const handleResend = async () => {

    if (timer > 0) return;

    setResending(true);
    setError("");

    try {

      const res = await resendOtp({
        identifier,
        requestId: requestIdRef.current
      });

      const newRequestId = res?.data?.data?.requestId;

      if (newRequestId) {
        requestIdRef.current = newRequestId; // ✅ no re-render
      }

      setOtp(["", "", "", "", "", ""]);
      inputsRef.current[0]?.focus();

      setTimer(30);

    } catch (err) {
      setError(err?.response?.data?.message || "Resend failed");
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="otp-wrapper">

      <div className="otp-card">

        <h2>Verify OTP</h2>
        <p className="otp-sub">Enter the 6-digit code</p>

        {error && <div className="error-box">{error}</div>}

        <div className="otp-box-group">
          {otp.map((digit, index) => (
            <input
              key={index}
              ref={(el) => (inputsRef.current[index] = el)}
              value={digit}
              maxLength={1}
              onChange={(e) => handleChange(e.target.value, index)}
              onKeyDown={(e) => handleKeyDown(e, index)}
              className="otp-box"
            />
          ))}
        </div>

        <button
          className="submit-btn"
          onClick={handleVerify}
          disabled={loading}
        >
          {loading ? <span className="spinner" /> : "Verify OTP"}
        </button>

        <div className="resend-box">
          {timer > 0 ? (
            <span>Resend in {timer}s</span>
          ) : (
            <button
              className="resend-btn"
              onClick={handleResend}
              disabled={resending}
            >
              {resending ? "Sending..." : "Resend OTP"}
            </button>
          )}
        </div>

      </div>
    </div>
  );
}