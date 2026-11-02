import React, { useState } from 'react';

/**
 * Displays the metadata of the resume currently stored for the logged-in user,
 * plus the actions available on it: download the original file, show/hide the
 * extracted text, replace it, or delete it.
 *
 * Deletion asks for an inline confirmation first so a single misplaced click
 * cannot destroy the uploaded document.
 */
const ResumeDetails = ({
  resume,
  extractedText,
  textLoading,
  onDownload,
  onLoadText,
  onReplaceClick,
  onDelete,
  deleting,
  downloading,
}) => {
  const [showText, setShowText] = useState(false);
  const [confirmingDelete, setConfirmingDelete] = useState(false);

  const formatDate = (isoString) => {
    if (!isoString) {
      return '--';
    }
    const date = new Date(isoString);
    return date.toLocaleString(undefined, {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleToggleText = async () => {
    if (!showText && extractedText === null) {
      await onLoadText();
    }
    setShowText((previous) => !previous);
  };

  return (
    <div className="resume-details">
      <div className="resume-details-header">
        <div>
          <h2 className="resume-file-name">{resume.originalFileName}</h2>
          <span className={`resume-badge resume-badge-${resume.fileType.toLowerCase()}`}>
            {resume.fileType}
          </span>
        </div>
      </div>

      <div className="resume-meta-grid">
        <div className="resume-meta-item">
          <span className="resume-meta-label">File size</span>
          <span className="resume-meta-value">{resume.fileSizeReadable}</span>
        </div>
        <div className="resume-meta-item">
          <span className="resume-meta-label">Uploaded on</span>
          <span className="resume-meta-value">{formatDate(resume.uploadedAt)}</span>
        </div>
        <div className="resume-meta-item">
          <span className="resume-meta-label">Last updated</span>
          <span className="resume-meta-value">{formatDate(resume.updatedAt)}</span>
        </div>
        <div className="resume-meta-item">
          <span className="resume-meta-label">Text extracted</span>
          <span className="resume-meta-value">
            {resume.textExtracted
              ? `Yes (${resume.extractedCharacterCount} characters)`
              : 'No readable text found'}
          </span>
        </div>
      </div>

      <div className="resume-actions">
        <button
          type="button"
          className="resume-btn resume-btn-primary"
          onClick={onDownload}
          disabled={downloading}
        >
          {downloading ? 'Preparing...' : 'Download'}
        </button>
        <button
          type="button"
          className="resume-btn resume-btn-secondary"
          onClick={handleToggleText}
          disabled={!resume.textExtracted || textLoading}
        >
          {textLoading ? 'Loading text...' : showText ? 'Hide extracted text' : 'View extracted text'}
        </button>
        <button type="button" className="resume-btn resume-btn-secondary" onClick={onReplaceClick}>
          Replace
        </button>
        {confirmingDelete ? (
          <span className="resume-confirm">
            <span className="resume-confirm-text">Delete this resume?</span>
            <button
              type="button"
              className="resume-btn resume-btn-danger"
              onClick={onDelete}
              disabled={deleting}
            >
              {deleting ? 'Deleting...' : 'Yes, delete'}
            </button>
            <button
              type="button"
              className="resume-btn resume-btn-secondary"
              onClick={() => setConfirmingDelete(false)}
              disabled={deleting}
            >
              Keep it
            </button>
          </span>
        ) : (
          <button
            type="button"
            className="resume-btn resume-btn-danger"
            onClick={() => setConfirmingDelete(true)}
          >
            Delete
          </button>
        )}
      </div>

      {showText && (
        <div className="resume-text-panel">
          <h3>Extracted text</h3>
          <pre className="resume-text-content">{extractedText || 'No text available.'}</pre>
        </div>
      )}
    </div>
  );
};

export default ResumeDetails;
