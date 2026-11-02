import axiosInstance from './axiosConfig';

/**
 * Thin service layer around the /api/resumes endpoints, following the same
 * pattern as authService.js: components never build URLs or call axios directly.
 *
 * The shared axiosInstance already attaches the JWT from localStorage to every
 * request, so these calls are authenticated automatically. For the multipart
 * uploads the JSON default Content-Type is overridden per request; axios then
 * lets the browser set the correct multipart boundary.
 */

const buildFormData = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return formData;
};

/** POST /api/resumes/upload - first-time upload (409 if one already exists). */
export const uploadResume = async (file, onUploadProgress) => {
  const response = await axiosInstance.post('/resumes/upload', buildFormData(file), {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress,
  });
  return response.data;
};

/** PUT /api/resumes/me - replace the currently stored resume. */
export const replaceResume = async (file, onUploadProgress) => {
  const response = await axiosInstance.put('/resumes/me', buildFormData(file), {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress,
  });
  return response.data;
};

/** GET /api/resumes/me - metadata of the logged-in user's resume (404 if none). */
export const getMyResume = async () => {
  const response = await axiosInstance.get('/resumes/me');
  return response.data;
};

/** GET /api/resumes/me/text - plain text extracted from the stored document. */
export const getMyResumeText = async () => {
  const response = await axiosInstance.get('/resumes/me/text');
  return response.data;
};

/** DELETE /api/resumes/me - remove the stored resume. */
export const deleteMyResume = async () => {
  const response = await axiosInstance.delete('/resumes/me');
  return response.data;
};

/**
 * GET /api/resumes/me/download - fetches the original file as a blob and
 * triggers a browser download. The request must go through axios (not a plain
 * link) so the Authorization header is attached.
 */
export const downloadMyResume = async (fallbackFileName = 'resume') => {
  const response = await axiosInstance.get('/resumes/me/download', {
    responseType: 'blob',
  });

  const fileName = parseFileName(response.headers['content-disposition']) || fallbackFileName;
  const blobUrl = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = blobUrl;
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(blobUrl);
};

/** Reads the file name out of a Content-Disposition response header. */
const parseFileName = (contentDisposition) => {
  if (!contentDisposition) {
    return null;
  }
  const utf8Match = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition);
  if (utf8Match) {
    return decodeURIComponent(utf8Match[1]);
  }
  const plainMatch = /filename="?([^";]+)"?/i.exec(contentDisposition);
  return plainMatch ? decodeURIComponent(plainMatch[1]) : null;
};

/**
 * Normalises any error thrown by the calls above into a single display string,
 * using the ApiError contract the backend returns ({ status, message, ... }).
 */
export const extractErrorMessage = (error, fallback = 'Something went wrong. Please try again.') => {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  if (error?.message === 'Network Error') {
    return 'Could not reach the server. Please check that the backend is running.';
  }
  return fallback;
};
