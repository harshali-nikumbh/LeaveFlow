
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

const emptyForm = {
  leaveType: "Casual",
  fromDate: "",
  toDate: "",
  reason: "",
};

export default function EmployeeDashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [leaves, setLeaves] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [filter, setFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

useEffect(() => {
  const token = localStorage.getItem("token");
  const storedUser = localStorage.getItem("user");

  if (!token || !storedUser) {
    navigate("/login", { replace: true });
    return;
  }

  const parsedUser = JSON.parse(storedUser);

  if (parsedUser.role !== "EMPLOYEE") {
    navigate(parsedUser.role === "ADMIN" ? "/admin" : "/login", {
      replace: true,
    });
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
      const response = await api.get("/leaves");
      setLeaves(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setError(
        err.response?.data?.error ||
          err.response?.data?.message ||
          "Could not load leave requests. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  function resetForm() {
    setForm(emptyForm);
    setEditingId(null);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (form.toDate < form.fromDate) {
      setError("The end date cannot be before the start date.");
      return;
    }

    setSaving(true);

    try {
      if (editingId !== null) {
        await api.put(`/leaves/${editingId}`, form);
        setMessage("Leave request updated successfully.");
      } else {
        await api.post("/leaves", form);
        setMessage("Leave request submitted successfully.");
      }

      resetForm();
      await loadLeaves();
    } catch (err) {
      setError(
        err.response?.data?.error ||
          err.response?.data?.message ||
          "Unable to save your leave request."
      );
    } finally {
      setSaving(false);
    }
  }

  function startEditing(leave) {
    setEditingId(leave.id);
    setForm({
      leaveType: leave.leaveType,
      fromDate: leave.fromDate,
      toDate: leave.toDate,
      reason: leave.reason,
    });
    setError("");
    setMessage("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function cancelLeave(id) {
    const confirmed = window.confirm(
      "Are you sure you want to cancel this pending leave request?"
    );

    if (!confirmed) return;

    setError("");
    setMessage("");

    try {
      await api.delete(`/leaves/${id}`);
      setMessage("Leave request cancelled successfully.");
      await loadLeaves();
    } catch (err) {
      setError(
        err.response?.data?.error ||
          err.response?.data?.message ||
          "Unable to cancel this leave request."
      );
    }
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/login", { replace: true });
  }

  const filteredLeaves =
    filter === "ALL"
      ? leaves
      : leaves.filter((leave) => leave.status === filter);

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
            <span>Employee Portal</span>
          </div>
        </div>

        <div className="header-user">
          <div className="avatar">
            {user.name?.charAt(0)?.toUpperCase() || "E"}
          </div>
          <div className="header-user-details">
            <strong>{user.name}</strong>
            <span>Employee</span>
          </div>
          <button className="logout-button" onClick={logout}>
            Sign out
          </button>
        </div>
      </header>

      <main className="dashboard-main">
        <section className="welcome-section">
          <div>
            <p className="eyebrow">EMPLOYEE WORKSPACE</p>
            <h1>Hello, {user.name?.split(" ")[0]} !</h1>
            <p className="subtitle">
              Plan your time off and keep track of your leave requests.
            </p>
          </div>
          <div className="today-card">
            <span>YOUR LEAVE OVERVIEW</span>
            <strong>{leaves.length} requests</strong>
            <small>All your leave activity in one place</small>
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

        <section className="dashboard-content">
          <div className="leave-form-card">
            <div className="section-heading">
              <div>
                <p className="eyebrow">
                  {editingId !== null ? "UPDATE REQUEST" : "NEW REQUEST"}
                </p>
                <h2>
                  {editingId !== null ? "Edit leave" : "Apply for leave"}
                </h2>
              </div>
              <span className="form-heading-icon">＋</span>
            </div>

            <form onSubmit={handleSubmit} className="leave-form">
              <label htmlFor="leaveType">Leave type</label>
              <select
                id="leaveType"
                name="leaveType"
                value={form.leaveType}
                onChange={handleChange}
                required
              >
                <option value="Casual">Casual Leave</option>
                <option value="Sick">Sick Leave</option>
                <option value="Annual">Annual Leave</option>
                <option value="Unpaid">Unpaid Leave</option>
                <option value="Other">Other</option>
              </select>

              <div className="date-grid">
                <div>
                  <label htmlFor="fromDate">Start date</label>
                  <input
                    id="fromDate"
                    name="fromDate"
                    type="date"
                    value={form.fromDate}
                    min={
                      editingId !== null
                        ? undefined
                        : new Date().toLocaleDateString("en-CA")
                    }
                    onChange={handleChange}
                    required
                  />
                </div>

                <div>
                  <label htmlFor="toDate">End date</label>
                  <input
                    id="toDate"
                    name="toDate"
                    type="date"
                    value={form.toDate}
                    min={form.fromDate || undefined}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <label htmlFor="reason">Reason for leave</label>
              <textarea
                id="reason"
                name="reason"
                rows="4"
                maxLength="500"
                placeholder="Briefly explain the reason for your leave..."
                value={form.reason}
                onChange={handleChange}
                required
              />

              <button
                className="primary-button"
                type="submit"
                disabled={saving}
              >
                {saving
                  ? "Saving..."
                  : editingId !== null
                    ? "Save changes"
                    : "Submit request"}
              </button>

              {editingId !== null && (
                <button
                  className="secondary-button"
                  type="button"
                  onClick={resetForm}
                >
                  Cancel editing
                </button>
              )}
            </form>
          </div>

          <div className="leave-list-card">
            <div className="section-heading leave-list-heading">
              <div>
                <p className="eyebrow">YOUR ACTIVITY</p>
                <h2>Leave requests</h2>
              </div>

              <select
                aria-label="Filter leave requests by status"
                className="filter-select"
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
              >
                <option value="ALL">All statuses</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
              </select>
            </div>

            {loading ? (
              <p className="empty-state">Loading your requests...</p>
            ) : filteredLeaves.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">▤</div>
                <strong>No leave requests found</strong>
                <p>
                  {filter === "ALL"
                    ? "Submit your first request using the form."
                    : `You have no ${filter.toLowerCase()} requests.`}
                </p>
              </div>
            ) : (
              <div className="leave-table-wrapper">
                <table className="leave-table">
                  <thead>
                    <tr>
                      <th>Leave details</th>
                      <th>Dates</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredLeaves.map((leave) => (
                      <tr key={leave.id}>
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
                                onClick={() => startEditing(leave)}
                              >
                                Edit
                              </button>
                              <button
                                className="text-button cancel-text"
                                onClick={() => cancelLeave(leave.id)}
                              >
                                Cancel
                              </button>
                            </div>
                          ) : (
                            <span className="table-secondary">—</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </section>
      </main>

      <footer className="dashboard-footer">
        LeaveFlow · Employee Leave Management System
      </footer>
    </div>
  );
}
