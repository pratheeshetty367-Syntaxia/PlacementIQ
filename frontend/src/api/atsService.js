import axiosInstance from './axiosConfig';

/**
 * Thin service layer around the /api/ats endpoints, following the same pattern
 * as authService.js and resumeService.js: components never build URLs or call
 * axios directly.
 *
 * The shared axiosInstance already attaches the JWT from localStorage, so every
 * call here is authenticated automatically.
 *
 * Note the longer timeout on analyzeResume. An ATS analysis is a round trip to
 * OpenAI and routinely takes 10-30 seconds; the default axios timeout would
 * abort a perfectly healthy request.
 */

const ANALYZE_TIMEOUT_MS = 90000;

/** POST /api/ats/analyze - runs a fresh analysis of the stored resume. */
export const analyzeResume = async (targetRole) => {
  const body = targetRole && targetRole.trim() ? { targetRole: targetRole.trim() } : {};
  const response = await axiosInstance.post('/ats/analyze', body, {
    timeout: ANALYZE_TIMEOUT_MS,
  });
  return response.data;
};

/**
 * GET /api/ats/me/latest - the most recent analysis.
 * The backend answers 200 with data: null when nothing has been run yet, so a
 * null result here is the empty state rather than a failure.
 */
export const getLatestAnalysis = async () => {
  const response = await axiosInstance.get('/ats/me/latest');
  return response.data;
};

/** GET /api/ats/me/history - compact list of every past run. */
export const getAnalysisHistory = async () => {
  const response = await axiosInstance.get('/ats/me/history');
  return response.data;
};

/** GET /api/ats/me/{id} - one past analysis in full. */
export const getAnalysisById = async (analysisId) => {
  const response = await axiosInstance.get(`/ats/me/${analysisId}`);
  return response.data;
};

/** DELETE /api/ats/me/{id} - removes one past analysis. */
export const deleteAnalysis = async (analysisId) => {
  const response = await axiosInstance.delete(`/ats/me/${analysisId}`);
  return response.data;
};

/**
 * GET /api/ats/me/status - readiness check used to decide whether the Analyze
 * button should be enabled and what to explain to the student.
 */
export const getAtsStatus = async () => {
  const response = await axiosInstance.get('/ats/me/status');
  return response.data;
};

/**
 * Normalises any error from the calls above into a single display string,
 * using the ApiError contract the backend returns.
 */
export const extractErrorMessage = (error, fallback = 'Something went wrong. Please try again.') => {
  if (error?.code === 'ECONNABORTED') {
    return 'The analysis took too long to complete. Please try again.';
  }
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  if (error?.message === 'Network Error') {
    return 'Could not reach the server. Please check that the backend is running.';
  }
  return fallback;
};
