import React from 'react';

/**
 * Shown when the backend reports that the logged-in user has not uploaded a
 * resume yet (HTTP 404 from GET /api/resumes/me).
 *
 * This is a normal, expected state rather than an error, so it is rendered as
 * friendly guidance above the upload control instead of a red error banner.
 */
const ResumeEmptyState = () => (
  <div className="resume-empty-state">
    <div className="resume-empty-icon">📄</div>
    <h2 className="resume-empty-title">No resume uploaded yet</h2>
    <p className="resume-empty-text">
      Upload your resume to keep it on file with your PlacementIQ account. We will read the text
      out of the document so it is ready for the AI-powered analysis features coming in the next
      parts of the platform.
    </p>
  </div>
);

export default ResumeEmptyState;
