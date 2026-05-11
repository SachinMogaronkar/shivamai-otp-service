import { useState } from "react";
import { login, register } from "../services/authService";
import { useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";
import trident from "../assets/trident.png";
import "../styles/auth.css";

export default function Auth() {

  const [mode, setMode] = useState("login");

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const handleSubmit = async () => {

    setError("");

    const id = identifier.trim();
    const pass = password.trim();

    if (!id || !pass) {
      setError("All fields required");
      return;
    }

    setLoading(true);

    try {

      if (mode === "login") {

        const res = await login({ identifier: id, password: pass });

        const requestId = res?.data?.data?.requestId;

        if (!requestId) {
          throw new Error("Invalid server response");
        }

        navigate("/verify-login", {
          state: { identifier: id, requestId }
        });

      } else {

        await register({ identifier: id, password: pass });

        navigate("/verify", {
          state: { identifier: id }
        });
      }

    } catch (err) {

      setError(
        err?.response?.data?.message ||
        err?.message ||
        "Something went wrong"
      );

    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">

      <div className="auth-left">
        <img src={trident} className="auth-trident" />
        <img src={logo} className="auth-logo" />
        <h1>ShivaMai</h1>
        <p>Secure • Verify • Connect</p>
      </div>

      <div className="auth-right">

        <div className="auth-card">

          <div className="auth-toggle">
            <button
              className={mode === "login" ? "active" : ""}
              onClick={() => setMode("login")}
            >
              Login
            </button>

            <button
              className={mode === "register" ? "active" : ""}
              onClick={() => setMode("register")}
            >
              Register
            </button>
          </div>

          <h2>
            {mode === "login" ? "Welcome Back" : "Create Account"}
          </h2>

          {error && <div className="error-box">{error}</div>}

          <input
            type="email"
            placeholder="Email"
            value={identifier}
            onChange={(e) => setIdentifier(e.target.value)}
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button
            className="submit-btn"
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? <span className="spinner" /> : (mode === "login" ? "Login" : "Register")}
          </button>

        </div>
      </div>
    </div>
  );
}