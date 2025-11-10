import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import ErrorBoundary from "./components/ErrorBoundary";
import Login from "./components/Login";
import Dashboard from "./components/Dashboard";
import Chat from "./components/Chat";
import "./App.css";

function App() {
  console.log("App component rendering...");
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    console.log("App useEffect running...");
    try {
      const token = localStorage.getItem("adminToken");
      console.log("Token found:", !!token);
      setIsAuthenticated(!!token);
    } catch (error) {
      console.error("Error checking authentication:", error);
    } finally {
      setLoading(false);
      console.log("Loading set to false");
    }
  }, []);

  if (loading) {
    return (
      <div style={{ 
        display: "flex", 
        justifyContent: "center", 
        alignItems: "center", 
        height: "100vh",
        width: "100vw",
        fontSize: "18px",
        color: "#666",
        backgroundColor: "#fff"
      }}>
        Loading...
      </div>
    );
  }

  return (
    <div style={{ height: "100%", width: "100%" }}>
      <ErrorBoundary>
        <Router>
          <Routes>
            <Route
              path="/login"
              element={
                isAuthenticated ? (
                  <Navigate to="/" replace />
                ) : (
                  <Login setIsAuthenticated={setIsAuthenticated} />
                )
              }
            />
            <Route
              path="/"
              element={
                isAuthenticated ? (
                  <Dashboard setIsAuthenticated={setIsAuthenticated} />
                ) : (
                  <Navigate to="/login" replace />
                )
              }
            />
            <Route
              path="/chat"
              element={
                isAuthenticated ? (
                  <Chat setIsAuthenticated={setIsAuthenticated} />
                ) : (
                  <Navigate to="/login" replace />
                )
              }
            />
          </Routes>
        </Router>
      </ErrorBoundary>
    </div>
  );
}

export default App;

