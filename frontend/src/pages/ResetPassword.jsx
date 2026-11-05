
import { useState } from "react";
import { Link, useSearchParams, useNavigate } from "react-router-dom";
import api from "../services/api";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!token) {
      setError("Invalid or missing reset link.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);

    try {
      const response = await api.post("/auth/reset-password", {
        token,
        newPassword: password,
      });

      setMessage(response.data.message);

      setTimeout(() => navigate("/login"), 1800);
    } catch (err) {
      setError(
        err.response?.data?.error || "Unable to reset password."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div className="brand-mark">EL</div>
        <p className="eyebrow">EMPLOYEE LEAVE MANAGEMENT</p>
        <h1>Reset password</h1>
        <p className="subtitle">Create a new password for your account.</p>

        {message && <div className="success-message">{message}</div>}
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <label htmlFor="password">New password</label>
          <input
            id="password"
            type="password"
            placeholder="At least 8 characters"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            maxLength={72}
            required
          />

          <label htmlFor="confirmPassword">Confirm new password</label>
          <input
            id="confirmPassword"
            type="password"
            placeholder="Enter password again"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            minLength={8}
            maxLength={72}
            required
          />

          <button className="primary-button" type="submit" disabled={loading}>
            {loading ? "Updating..." : "Reset password"}
          </button>
        </form>

        <p className="auth-footer">
          <Link to="/login">Back to sign in</Link>
        </p>
      </section>
    </main>
  );
}
