import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getDashboardSummary } from '../api/authService';
import './Dashboard.css';

/**
 * Dashboard shown right after login/registration.
 * Confirms end-to-end JWT auth by calling a protected backend endpoint and
 * links to the modules delivered so far (Resume Management, ATS Analysis).
 */
const Dashboard = () => {
  const { user } = useAuth();
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const result = await getDashboardSummary();
        setSummary(result.data);
      } catch (err) {
        setError('Could not load dashboard summary.');
      }
    };
    fetchSummary();
  }, []);

  const firstName = user?.fullName ? user.fullName.split(' ')[0] : '';

  return (
    <div className="dashboard-page">
      <header className="dashboard-hero">
        <p className="dashboard-eyebrow">Your preparation hub</p>
        <h1>Welcome back, {firstName}.</h1>
        <p className="dashboard-subtitle">Here is a quick snapshot of your placement preparation.</p>
      </header>

      {error && <div className="dashboard-error">{error}</div>}

      <div className="dashboard-cards">
        <div className="dashboard-card">
          <h3>Modules Completed</h3>
          <p className="dashboard-metric">{summary?.modulesCompleted ?? '--'}</p>
        </div>
        <div className="dashboard-card">
          <h3>Modules In Progress</h3>
          <p className="dashboard-metric">{summary?.modulesInProgress ?? '--'}</p>
        </div>
        <div className="dashboard-card">
          <h3>Account Email</h3>
          <p className="dashboard-metric-small">{user?.email}</p>
        </div>
      </div>

      <h2 className="dashboard-section-title">Start here</h2>
      <div className="dashboard-actions">
        <Link to="/resume" className="dashboard-action">
          <span className="dashboard-action-step">01</span>
          <strong>Manage your resume</strong>
          <span>Upload, replace or review the resume PlacementIQ will analyse.</span>
          <em>Open resume &rarr;</em>
        </Link>
        <Link to="/ats" className="dashboard-action dashboard-action-accent">
          <span className="dashboard-action-step">02</span>
          <strong>Run an ATS analysis</strong>
          <span>Get your skills, keyword gaps and improvement suggestions.</span>
          <em>Analyse now &rarr;</em>
        </Link>
      </div>

      <div className="dashboard-placeholder-note">
        More AI-powered preparation tools will appear here in the next parts of PlacementIQ.
      </div>
    </div>
  );
};

export default Dashboard;
