
import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import api from "../services/api";

export default function ProtectedRoute({ allowedRole }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    let active = true;

    async function verifySession() {
      try {
        const response = await api.get("/auth/me");

        if (!active) return;

        const currentUser = response.data;

        setUser(currentUser);
        setAuthenticated(true);

        localStorage.setItem("user", JSON.stringify(currentUser));
        localStorage.removeItem("token");
        sessionStorage.removeItem("token");

      } catch {
        if (!active) return;

        setAuthenticated(false);
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        sessionStorage.removeItem("token");

      } finally {
        if (active) setLoading(false);
      }
    }

    verifySession();

    return () => {
      active = false;
    };
  }, []);

  if (loading) {
    return <div className="auth-page">Verifying session...</div>;
  }

  if (!authenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRole && user.role !== allowedRole) {
    return (
      <Navigate
        to={user.role === "ADMIN" ? "/admin" : "/dashboard"}
        replace
      />
    );
  }

  return <Outlet />;
}
