import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Resume from './pages/Resume';
import AtsAnalysis from './pages/AtsAnalysis';

/**
 * Root component: sets up the AuthProvider (global auth state),
 * the Navbar, and all client-side routes.
 * - "/login" and "/register" are public.
 * - "/dashboard", "/resume" and "/ats" are protected via PrivateRoute (require a valid JWT).
 */
function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="app-container">
          <Navbar />
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/dashboard"
              element={
                <PrivateRoute>
                  <Dashboard />
                </PrivateRoute>
              }
            />
            {/* ADDED IN PART 2 - Resume Management module */}
            <Route
              path="/resume"
              element={
                <PrivateRoute>
                  <Resume />
                </PrivateRoute>
              }
            />
            {/* ADDED IN PART 3 - ATS Resume Analysis module */}
            <Route
              path="/ats"
              element={
                <PrivateRoute>
                  <AtsAnalysis />
                </PrivateRoute>
              }
            />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
