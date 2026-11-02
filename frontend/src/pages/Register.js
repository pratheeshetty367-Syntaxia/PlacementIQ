import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthForm.css';

const Register = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setSubmitting(true);
    try {
      await register(fullName, email, password);
      navigate('/dashboard');
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed. Please try again.';
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
        <h2>Create your PlacementIQ account</h2>
        <p className="auth-sub">Create your account and start improving your resume.</p>

        {error && <div className="auth-error">{error}</div>}

        <label htmlFor="fullName">Full Name</label>
        <input
          id="fullName"
          type="text"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          placeholder="Jane Doe"
          required
        />

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
          placeholder="At least 6 characters"
          required
          minLength={6}
        />

        <label htmlFor="confirmPassword">Confirm Password</label>
        <input
          id="confirmPassword"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          placeholder="Re-enter your password"
          required
        />

        <button type="submit" disabled={submitting}>
          {submitting ? 'Creating account...' : 'Register'}
        </button>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Login here</Link>
        </p>
      </form>
      </div>
    </div>
  );
};

export default Register;

