
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

export default function AdminDashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [leaves, setLeaves] = useState([]);
  const [filter, setFilter] = useState("ALL");
  const [typeFilter, setTypeFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [updatingId, setUpdatingId] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    const storedUser = localStorage.getItem("user");

    if (!token || !storedUser) {
      navigate("/login", { replace: true });
      return;
    }

    const parsedUser = JSON.parse(storedUser);

    if (parsedUser.role !== "ADMIN") {
      navigate("/dashboard", { replace: true });
      return;
    }

    const timer = setTimeout(() => {
      setUser(parsedUser);
      loadLeaves();
    }, 0);

    return () => clearTimeout(timer);
  }, [navigate]);

  async function loadLeaves() {
    setLoading(true);
    setError("");

    try {
      const response = await api.get("/admin/leaves");
      setLeaves(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setError(
        err.response?.data?.error ||
          err.response?.data?.message ||
          "Unable to load leave requests."
      );
    } finally {
      setLoading(false);
    }
  }

  async function updateStatus(id, status) {
    const action = status === "APPROVED" ? "approve" : "reject";

    if (!window.confirm(`Are you sure you want to ${action} this request?`)) {
      return;
    }

    setUpdatingId(id);
    setError("");
    setMessage("");

    try {
      await api.put(`/admin/leaves/${id}/status`, { status });
      setMessage(`Leave request ${action}d successfully.`);
      await loadLeaves();
    } catch (err) {
      setError(
        err.response?.data?.error ||
          err.response?.data?.message ||
          `Unable to ${action} this request.`
      );
    } finally {
      setUpdatingId(null);
    }
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/login", { replace: true });
  }

  const filteredLeaves = leaves.filter((leave) => {
    const matchesStatus =
      filter === "ALL" || leave.status === filter;
    const matchesType =
      typeFilter === "ALL" || leave.leaveType === typeFilter;

    return matchesStatus && matchesType;
  });

  const pendingCount = leaves.filter(
    (leave) => leave.status === "PENDING"
  ).length;

  const approvedCount = leaves.filter(
    (leave) => leave.status === "APPROVED"
  ).length;

  const rejectedCount = leaves.filter(
    (leave) => leave.status === "REJECTED"
  ).length;

  if (!user) return null;

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div className="dashboard-brand">
          <div className="brand-mark">EL</div>
          <div>
            <strong>LeaveFlow</strong>
            <span>Administrator Portal</span>
          </div>
        </div>

        <div className="header-user">
          <div className="avatar">A</div>
          <div className="header-user-details">
            <strong>{user.name}</strong>
            <span>Administrator</span>
          </div>
          <button className="logout-button" onClick={logout}>
            Sign out
          </button>
        </div>
      </header>

      <main className="dashboard-main">
        <section className="welcome-section">
          <div>
            <p className="eyebrow">ADMINISTRATION</p>
            <h1>Leave management</h1>
            <p className="subtitle">
              Review requests and manage employee time off.
            </p>
          </div>

          <div className="today-card">
            <span>REQUESTS TO REVIEW</span>
            <strong>{pendingCount} pending</strong>
            <small>Review outstanding leave applications</small>
          </div>
        </section>

        <section className="stats-grid">
          <div className="stat-card">
            <span className="stat-icon">◷</span>
            <p>Total requests</p>
            <strong>{leaves.length}</strong>
          </div>
          <div className="stat-card">
            <span className="stat-icon pending-icon">◌</span>
            <p>Pending</p>
            <strong>{pendingCount}</strong>
          </div>
          <div className="stat-card">
            <span className="stat-icon approved-icon">✓</span>
            <p>Approved</p>
            <strong>{approvedCount}</strong>
          </div>
          <div className="stat-card">
            <span className="stat-icon rejected-icon">×</span>
            <p>Rejected</p>
            <strong>{rejectedCount}</strong>
          </div>
        </section>

        {error && <div className="error-message">{error}</div>}
        {message && <div className="success-message">{message}</div>}

        <section className="leave-list-card">
          <div className="section-heading leave-list-heading">
            <div>
              <p className="eyebrow">TEAM ACTIVITY</p>
              <h2>Employee leave requests</h2>
            </div>

            <div className="admin-filters">
              <select
                className="filter-select"
                aria-label="Filter by status"
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
              >
                <option value="ALL">All statuses</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
              </select>

              <select
                className="filter-select"
                aria-label="Filter by leave type"
                value={typeFilter}
                onChange={(event) => setTypeFilter(event.target.value)}
              >
                <option value="ALL">All leave types</option>
                <option value="Casual">Casual</option>
                <option value="Sick">Sick</option>
                <option value="Annual">Annual</option>
                <option value="Unpaid">Unpaid</option>
                <option value="Other">Other</option>
              </select>

              <button className="logout-button" onClick={loadLeaves}>
                Refresh
              </button>
            </div>
          </div>

          {loading ? (
            <p className="empty-state">Loading leave requests...</p>
          ) : filteredLeaves.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">▤</div>
              <strong>No matching requests</strong>
              <p>Try changing the filters or refresh the list.</p>
            </div>
          ) : (
            <div className="leave-table-wrapper">
              <table className="leave-table">
                <thead>
                  <tr>
                    <th>Employee ID</th>
                    <th>Leave details</th>
                    <th>Dates</th>
                    <th>Status</th>
                    <th>Decision</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredLeaves.map((leave) => (
                    <tr key={leave.id}>
                      <td>{leave.userId}</td>
                      <td>
                        <strong>{leave.leaveType} Leave</strong>
                        <span className="table-secondary">
                          {leave.reason}
                        </span>
                      </td>
                      <td>
                        <span>{leave.fromDate}</span>
                        <span className="table-secondary">
                          to {leave.toDate}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`status-badge ${leave.status?.toLowerCase()}`}
                        >
                          {leave.status}
                        </span>
                      </td>
                      <td>
                        {leave.status === "PENDING" ? (
                          <div className="table-actions">
                            <button
                              className="text-button"
                              disabled={updatingId === leave.id}
                              onClick={() =>
                                updateStatus(leave.id, "APPROVED")
                              }
                            >
                              Approve
                            </button>
                            <button
                              className="text-button cancel-text"
                              disabled={updatingId === leave.id}
                              onClick={() =>
                                updateStatus(leave.id, "REJECTED")
                              }
                            >
                              Reject
                            </button>
                          </div>
                        ) : (
                          <span className="table-secondary">
                            Decision recorded
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>

      <footer className="dashboard-footer">
        LeaveFlow · Administrator Portal
      </footer>
    </div>
  );
}
