import React, { useCallback, useEffect, useState } from 'react';
import ResumeUpload from '../components/ResumeUpload';
import ResumeDetails from '../components/ResumeDetails';
import ResumeEmptyState from '../components/ResumeEmptyState';
import {
  getMyResume,
  getMyResumeText,
  uploadResume,
  replaceResume,
  deleteMyResume,
  downloadMyResume,
  extractErrorMessage,
} from '../api/resumeService';
import './Resume.css';

/**
 * Protected page that owns the whole Resume Management flow for the logged-in
 * student: view, upload, replace, download and delete.
 *
 * It is the single place that talks to resumeService, and it drives all four
 * screen states required by the module:
 *   loading  - while the initial GET /api/resumes/me is in flight
 *   empty    - the request came back 404 (nothing uploaded yet)
 *   loaded   - resume metadata is shown, with its actions
 *   error    - any non-404 failure, shown in a dismissible banner
 * plus an upload status message after every upload/replace/delete.
 */
const Resume = () => {
  const [resume, setResume] = useState(null);
  const [extractedText, setExtractedText] = useState(null);

  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [textLoading, setTextLoading] = useState(false);

  const [progress, setProgress] = useState(0);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('');
  const [replaceMode, setReplaceMode] = useState(false);

  /** Loads the current resume; a 404 simply means the empty state. */
  const loadResume = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const result = await getMyResume();
      setResume(result.data);
    } catch (err) {
      if (err?.response?.status === 404) {
        setResume(null);
      } else {
        setError(extractErrorMessage(err, 'Could not load your resume.'));
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadResume();
  }, [loadResume]);

  const handleProgress = (event) => {
    if (event.total) {
      setProgress(Math.round((event.loaded * 100) / event.total));
    }
  };

  const handleUpload = async (file) => {
    setUploading(true);
    setProgress(0);
    setError('');
    setStatus('');
    try {
      const result = replaceMode
        ? await replaceResume(file, handleProgress)
        : await uploadResume(file, handleProgress);

      const payload = result.data;
      setResume(payload.resume);
      setExtractedText(null);
      setReplaceMode(false);
      setStatus(
        `${payload.replacedExisting ? 'Resume replaced' : 'Resume uploaded'} successfully. ${
          payload.extractionNote || ''
        }`.trim()
      );
    } catch (err) {
      setError(extractErrorMessage(err, 'The resume could not be uploaded.'));
    } finally {
      setUploading(false);
      setProgress(0);
    }
  };

  const handleLoadText = async () => {
    setTextLoading(true);
    setError('');
    try {
      const result = await getMyResumeText();
      setExtractedText(result.data.extractedText);
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not load the extracted text.'));
    } finally {
      setTextLoading(false);
    }
  };

  const handleDownload = async () => {
    setDownloading(true);
    setError('');
    try {
      await downloadMyResume(resume?.originalFileName);
    } catch (err) {
      setError('The resume file could not be downloaded.');
    } finally {
      setDownloading(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    setError('');
    setStatus('');
    try {
      await deleteMyResume();
      setResume(null);
      setExtractedText(null);
      setReplaceMode(false);
      setStatus('Your resume has been deleted.');
    } catch (err) {
      setError(extractErrorMessage(err, 'The resume could not be deleted.'));
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="resume-page">
      <header className="resume-page-header">
        <h1>My Resume</h1>
        <p className="resume-page-subtitle">
          Keep one resume on file. PDF and DOCX documents up to 5 MB are supported.
        </p>
      </header>

      {status && (
        <div className="resume-status" role="status">
          {status}
          <button type="button" className="resume-dismiss" onClick={() => setStatus('')}>
            &times;
          </button>
        </div>
      )}

      {error && (
        <div className="resume-error" role="alert">
          {error}
          <button type="button" className="resume-dismiss" onClick={() => setError('')}>
            &times;
          </button>
        </div>
      )}

      {loading ? (
        <div className="resume-loading">
          <div className="resume-spinner" />
          <span>Loading your resume...</span>
        </div>
      ) : resume && !replaceMode ? (
        <ResumeDetails
          resume={resume}
          extractedText={extractedText}
          textLoading={textLoading}
          deleting={deleting}
          downloading={downloading}
          onDownload={handleDownload}
          onLoadText={handleLoadText}
          onReplaceClick={() => setReplaceMode(true)}
          onDelete={handleDelete}
        />
      ) : (
        <div className="resume-upload-section">
          {!resume && <ResumeEmptyState />}
          <ResumeUpload
            mode={replaceMode ? 'replace' : 'upload'}
            uploading={uploading}
            progress={progress}
            onUpload={handleUpload}
            onCancel={() => setReplaceMode(false)}
          />
        </div>
      )}
    </div>
  );
};

export default Resume;
