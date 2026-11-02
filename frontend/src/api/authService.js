import axiosInstance from './axiosConfig';

/**
 * Thin service layer around the /api/auth and /api/users endpoints.
 * Keeps components free of raw axios calls / URL strings.
 */

export const registerUser = async (fullName, email, password) => {
  const response = await axiosInstance.post('/auth/register', {
    fullName,
    email,
    password,
  });
  return response.data;
};

export const loginUser = async (email, password) => {
  const response = await axiosInstance.post('/auth/login', {
    email,
    password,
  });
  return response.data;
};

export const logoutUser = async () => {
  const response = await axiosInstance.post('/auth/logout');
  return response.data;
};

export const getMyProfile = async () => {
  const response = await axiosInstance.get('/users/me');
  return response.data;
};

export const getDashboardSummary = async () => {
  const response = await axiosInstance.get('/dashboard/summary');
  return response.data;
};

