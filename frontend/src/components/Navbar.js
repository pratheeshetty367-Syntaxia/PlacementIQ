import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const navClass = ({ isActive }) => (isActive ? 'active' : undefined);

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        <span className="navbar-logo">IQ</span>
        <span>Placement<em>IQ</em></span>
      </Link>
      <div className="navbar-links">
        {isAuthenticated ? (
          <>
            <span className="navbar-user">Hi, {user?.fullName}</span>
            <NavLink to="/dashboard" className={navClass}>Dashboard</NavLink>
            {/* ADDED IN PART 2 - entry point to the Resume Management page */}
            <NavLink to="/resume" className={navClass}>Resume</NavLink>
            {/* ADDED IN PART 3 - entry point to the ATS Analysis page */}
            <NavLink to="/ats" className={navClass}>ATS Analysis</NavLink>
            <button className="navbar-logout-btn" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <NavLink to="/login" className={navClass}>Login</NavLink>
            <NavLink to="/register" className={navClass}>Register</NavLink>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
