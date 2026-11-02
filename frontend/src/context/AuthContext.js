import React, { createContext, useContext, useState, useEffect } from 'react';
import { loginUser, registerUser, logoutUser } from '../api/authService';

const AuthContext = createContext(null);

const TOKEN_KEY = 'placementiq_token';
const USER_KEY = 'placementiq_user';

/**
 * Provides authentication state (current user, token) and actions
 * (login, register, logout) to the entire component tree.
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem(USER_KEY);
    const storedToken = localStorage.getItem(TOKEN_KEY);
    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const persistSession = (authResponse) => {
    const { token, ...userInfo } = authResponse;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo));
    setUser(userInfo);
  };

  const login = async (email, password) => {
    const result = await loginUser(email, password);
    persistSession(result.data);
    return result.data;
  };

  const register = async (fullName, email, password) => {
    const result = await registerUser(fullName, email, password);
    persistSession(result.data);
    return result.data;
  };

  const logout = async () => {
    try {
      await logoutUser();
    } catch (err) {
      // Even if the API call fails, clear local session state.
      console.error('Logout API call failed:', err);
    } finally {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      setUser(null);
    }
  };

  const value = {
    user,
    isAuthenticated: !!user,
    loading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

