import React from "react";
import { useNavigate } from "react-router-dom";
import "./Dashboard.css";

const Dashboard = ({ setIsAuthenticated }) => {
  const navigate = useNavigate();
  const adminUser = JSON.parse(localStorage.getItem("adminUser") || "{}");

  const handleLogout = () => {
    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminUser");
    setIsAuthenticated(false);
  };

  // Fixed statistics data
  const stats = {
    totalUsers: 1250,
    totalBooks: 350,
    totalOrders: 2840,
    totalRevenue: 125000,
    activeConversations: 12,
    pendingConversations: 5,
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="header-content">
          <h1>BookChain Admin Dashboard</h1>
          <div className="header-actions">
            <span className="admin-name">Welcome, {adminUser.fullName || "Admin"}</span>
            <button onClick={() => navigate("/chat")} className="btn-primary">
              Go to Chat
            </button>
            <button onClick={handleLogout} className="btn-secondary">
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="dashboard-content">
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">👥</div>
            <div className="stat-info">
              <h3>Total Users</h3>
              <p className="stat-value">{stats.totalUsers.toLocaleString()}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">📚</div>
            <div className="stat-info">
              <h3>Total Books</h3>
              <p className="stat-value">{stats.totalBooks.toLocaleString()}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">🛒</div>
            <div className="stat-info">
              <h3>Total Orders</h3>
              <p className="stat-value">{stats.totalOrders.toLocaleString()}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">💰</div>
            <div className="stat-info">
              <h3>Total Revenue</h3>
              <p className="stat-value">${stats.totalRevenue.toLocaleString()}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">💬</div>
            <div className="stat-info">
              <h3>Active Conversations</h3>
              <p className="stat-value">{stats.activeConversations}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">⏳</div>
            <div className="stat-info">
              <h3>Pending Conversations</h3>
              <p className="stat-value">{stats.pendingConversations}</p>
            </div>
          </div>
        </div>

        <div className="quick-actions">
          <h2>Quick Actions</h2>
          <div className="actions-grid">
            <button onClick={() => navigate("/chat")} className="action-card">
              <span className="action-icon">💬</span>
              <span className="action-text">Manage Chat</span>
            </button>
            <button className="action-card" disabled>
              <span className="action-icon">📊</span>
              <span className="action-text">View Reports</span>
            </button>
            <button className="action-card" disabled>
              <span className="action-icon">📦</span>
              <span className="action-text">Manage Books</span>
            </button>
            <button className="action-card" disabled>
              <span className="action-icon">👤</span>
              <span className="action-text">Manage Users</span>
            </button>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;

