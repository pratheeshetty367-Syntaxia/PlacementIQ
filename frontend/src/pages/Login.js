import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthForm.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      const message = err.response?.data?.message || 'Invalid email or password';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <aside className="auth-aside">
        <div className="auth-aside-eyebrow">AI placement preparation</div>
        <h1>Turn your resume into an <em>offer letter</em>.</h1>
        <p>PlacementIQ reviews your resume the way a recruiter's screening tools do, then tells you exactly what to fix.</p>
        <ul className="auth-points">
          <li><span>1</span>Upload your resume once, keep it organised</li>
          <li><span>2</span>See skills, keywords and gaps in seconds</li>
          <li><span>3</span>Get specific, ATS-oriented suggestions</li>
        </ul>
      </aside>
      <div className="auth-main">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>Login to PlacementIQ</h2>
        <p className="auth-sub">Welcome back. Sign in to continue your preparation.</p>

        {error && <div className="auth-error">{error}</div>}

        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          required
        />

        <button type="submit" disabled={submitting}>
          {submitting ? 'Logging in...' : 'Login'}
        </button>

        <p className="auth-switch">
          Don't have an account? <Link to="/register">Register here</Link>
        </p>
      </form>
      </div>
    </div>
  );
};

export default Login;

