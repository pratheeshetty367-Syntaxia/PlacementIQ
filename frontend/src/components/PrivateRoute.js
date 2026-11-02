import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Wraps a protected page: if the user is not authenticated, redirect
 * them to /login. Otherwise render the requested page (children).
 */
const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="page-loading">Loading...</div>;
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

export default PrivateRoute;

