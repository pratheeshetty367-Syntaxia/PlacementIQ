import React, { useRef, useState } from 'react';

/**
 * File picker used for both the first upload and the replace flow.
 *
 * Responsibilities:
 *  - lets the user pick a .pdf/.docx file (click or drag & drop)
 *  - performs the same client-side checks the backend validator enforces,
 *    so obvious mistakes are caught before a request is sent
 *  - shows the selected file, an upload progress bar and the busy state
 *
 * The actual API call is owned by the parent page (Resume.js) and passed in
 * as onUpload, keeping this component presentational.
 */

const ALLOWED_EXTENSIONS = ['pdf', 'docx'];
const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // must match placementiq.resume.max-file-size-bytes

const ResumeUpload = ({ mode = 'upload', uploading, progress, onUpload, onCancel }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [localError, setLocalError] = useState('');
  const [dragActive, setDragActive] = useState(false);
  const inputRef = useRef(null);

  const isReplaceMode = mode === 'replace';

  const validateFile = (file) => {
    const extension = file.name.split('.').pop().toLowerCase();
    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      return 'Only PDF and DOCX resumes are supported.';
    }
    if (file.size === 0) {
      return 'The selected file is empty.';
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
      return 'The selected file is larger than the 5 MB limit.';
    }
    return '';
  };

  const chooseFile = (file) => {
    if (!file) {
      return;
    }
    const validationError = validateFile(file);
    if (validationError) {
      setLocalError(validationError);
      setSelectedFile(null);
      return;
    }
    setLocalError('');
    setSelectedFile(file);
  };

  const handleInputChange = (e) => {
    chooseFile(e.target.files && e.target.files[0]);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragActive(false);
    if (uploading) {
      return;
    }
    chooseFile(e.dataTransfer.files && e.dataTransfer.files[0]);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    if (!uploading) {
      setDragActive(true);
    }
  };

  const handleSubmit = async () => {
    if (!selectedFile) {
      setLocalError('Please choose a file first.');
      return;
    }
    await onUpload(selectedFile);
    setSelectedFile(null);
    if (inputRef.current) {
      inputRef.current.value = '';
    }
  };

  const formatSize = (bytes) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  return (
    <div className="resume-upload">
      <div
        className={`resume-dropzone ${dragActive ? 'resume-dropzone-active' : ''} ${
          uploading ? 'resume-dropzone-disabled' : ''
        }`}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={() => setDragActive(false)}
        onClick={() => !uploading && inputRef.current && inputRef.current.click()}
      >
        <p className="resume-dropzone-title">
          {isReplaceMode ? 'Drop a new resume here' : 'Drop your resume here'}
        </p>
        <p className="resume-dropzone-subtitle">or click to browse &middot; PDF or DOCX &middot; up to 5 MB</p>
        <input
          ref={inputRef}
          type="file"
          accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
          onChange={handleInputChange}
          disabled={uploading}
          className="resume-file-input"
        />
      </div>

      {selectedFile && (
        <div className="resume-selected-file">
          <span className="resume-selected-name">{selectedFile.name}</span>
          <span className="resume-selected-size">{formatSize(selectedFile.size)}</span>
        </div>
      )}

      {localError && <div className="resume-error-inline">{localError}</div>}

      {uploading && (
        <div className="resume-progress">
          <div className="resume-progress-bar" style={{ width: `${progress}%` }} />
          <span className="resume-progress-label">Uploading... {progress}%</span>
        </div>
      )}

      <div className="resume-upload-actions">
        <button
          type="button"
          className="resume-btn resume-btn-primary"
          onClick={handleSubmit}
          disabled={uploading || !selectedFile}
        >
          {uploading
            ? 'Uploading...'
            : isReplaceMode
            ? 'Replace Resume'
            : 'Upload Resume'}
        </button>
        {isReplaceMode && (
          <button
            type="button"
            className="resume-btn resume-btn-secondary"
            onClick={onCancel}
            disabled={uploading}
          >
            Cancel
          </button>
        )}
      </div>
    </div>
  );
};

export default ResumeUpload;
