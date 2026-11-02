import React, { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import AtsScoreCard from '../components/AtsScoreCard';
import AtsKeywordList from '../components/AtsKeywordList';
import AtsRecommendations from '../components/AtsRecommendations';
import AtsProfileSections from '../components/AtsProfileSections';
import {
  analyzeResume,
  getLatestAnalysis,
  getAtsStatus,
  extractErrorMessage,
} from '../api/atsService';
import './AtsAnalysis.css';

/**
 * Protected page for the ATS Resume Analysis module.
 *
 * It analyses the resume the student already uploaded in Part 2 - no file is
 * ever selected or sent here. On mount it loads two things in parallel: the
 * readiness status (is a resume present, is the AI configured) and the most
 * recent analysis, so a returning student sees their last result immediately
 * instead of a blank page.
 *
 * Screen states handled:
 *   loading    - initial fetch in flight
 *   no resume  - nothing uploaded yet, with a link to the resume page
 *   empty      - resume present but no analysis run yet
 *   analysing  - the OpenAI round trip, with an explanatory busy state
 *   result     - the full analysis
 *   error      - dismissible banner, analysis kept on screen if one exists
 */
const AtsAnalysis = () => {
  const [analysis, setAnalysis] = useState(null);
  const [status, setStatus] = useState({ resumeReady: false, aiConfigured: true });
  const [targetRole, setTargetRole] = useState('');

  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const loadInitialData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [statusResult, latestResult] = await Promise.all([
        getAtsStatus(),
        getLatestAnalysis(),
      ]);
      setStatus(statusResult.data || { resumeReady: false, aiConfigured: true });
      setAnalysis(latestResult.data || null);
      if (latestResult.data?.targetRole) {
        setTargetRole(latestResult.data.targetRole);
      }
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not load your ATS analysis.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadInitialData();
  }, [loadInitialData]);

  const handleAnalyze = async () => {
    setAnalyzing(true);
    setError('');
    setNotice('');
    try {
      const result = await analyzeResume(targetRole);
      setAnalysis(result.data);
      setStatus((previous) => ({ ...previous, hasPreviousAnalysis: true }));
      setNotice('Analysis complete. Scroll down for the full breakdown.');
    } catch (err) {
      setError(extractErrorMessage(err, 'The resume could not be analysed.'));
    } finally {
      setAnalyzing(false);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return '';
    return new Date(isoString).toLocaleString(undefined, {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // ---------------- loading ----------------
  if (loading) {
    return (
      <div className="ats-page">
        <div className="ats-loading">
          <div className="ats-spinner" />
          <span>Loading your ATS analysis...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="ats-page">
      <header className="ats-page-header">
        <h1>ATS Resume Analysis</h1>
        <p className="ats-page-subtitle">
          PlacementIQ reviews the resume you uploaded and reports the skills and keywords it
          finds, what is missing, and how to make the document easier for an Applicant
          Tracking System to read.
        </p>
      </header>

      {notice && (
        <div className="ats-notice" role="status">
          {notice}
          <button type="button" className="ats-dismiss" onClick={() => setNotice('')}>
            &times;
          </button>
        </div>
      )}

      {error && (
        <div className="ats-error" role="alert">
          {error}
          <button type="button" className="ats-dismiss" onClick={() => setError('')}>
            &times;
          </button>
        </div>
      )}

      {!status.aiConfigured && (
        <div className="ats-warning">
          AI analysis is not configured on this server. The OPENAI_API_KEY environment
          variable must be set before a resume can be analysed.
        </div>
      )}

      {/* ---------------- no resume uploaded ---------------- */}
      {!status.resumeReady ? (
        <div className="ats-card ats-empty-state">
          <div className="ats-empty-icon">📄</div>
          <h2 className="ats-empty-title">No analysable resume found</h2>
          <p className="ats-empty-text">
            The ATS analysis runs on the resume stored in your account. Upload a text-based
            PDF or DOCX resume first, then come back here to analyse it.
          </p>
          <Link to="/resume" className="ats-btn ats-btn-primary">
            Go to My Resume
          </Link>
        </div>
      ) : (
        <>
          {/* ---------------- analyse control ---------------- */}
          <section className="ats-card ats-control">
            <label className="ats-label" htmlFor="ats-target-role">
              Target role (optional)
            </label>
            <div className="ats-control-row">
              <input
                id="ats-target-role"
                type="text"
                className="ats-input"
                placeholder="e.g. Java Backend Developer"
                value={targetRole}
                maxLength={150}
                onChange={(e) => setTargetRole(e.target.value)}
                disabled={analyzing}
              />
              <button
                type="button"
                className="ats-btn ats-btn-primary"
                onClick={handleAnalyze}
                disabled={analyzing || !status.aiConfigured}
              >
                {analyzing ? 'Analyzing...' : analysis ? 'Re-analyze Resume' : 'Analyze Resume'}
              </button>
            </div>
            <p className="ats-help-text">
              Naming a role lets the analysis judge your keyword coverage against that role.
              Leave it blank for a general review.
            </p>
          </section>

          {/* ---------------- analysing ---------------- */}
          {analyzing && (
            <div className="ats-card ats-analyzing">
              <div className="ats-spinner" />
              <div>
                <strong>Analyzing your resume...</strong>
                <p className="ats-analyzing-text">
                  This usually takes 10 to 30 seconds. Please keep this page open.
                </p>
              </div>
            </div>
          )}

          {/* ---------------- results ---------------- */}
          {analysis && !analyzing && (
            <>
              <p className="ats-result-meta">
                Analysed <strong>{analysis.resumeFileName}</strong> on{' '}
                {formatDate(analysis.analyzedAt)}
                {analysis.modelUsed ? ` · model: ${analysis.modelUsed}` : ''}
              </p>

              <AtsScoreCard analysis={analysis} />

              <AtsKeywordList
                title="Extracted Skills"
                description="Skills PlacementIQ found stated in your resume."
                items={analysis.skills}
                variant="found"
                emptyMessage="No skills could be identified. Add a clearly headed Skills section listing your technologies."
              />

              <AtsKeywordList
                title="Keywords Found"
                description="ATS-relevant terms already present in your resume."
                items={analysis.keywords}
                variant="found"
                emptyMessage="No recognisable keywords were found in your resume."
              />

              <AtsKeywordList
                title="Missing / Recommended Keywords"
                description="Terms a recruiter would expect for this kind of role but which are absent. Add only those that genuinely apply to you."
                items={analysis.missingKeywords}
                variant="missing"
                emptyMessage="No obvious keyword gaps were identified."
              />

              <AtsRecommendations
                suggestions={analysis.suggestions}
                missingSections={analysis.missingSections}
              />

              <AtsProfileSections
                education={analysis.education}
                experience={analysis.experience}
                totalExperienceYears={analysis.totalExperienceYears}
              />
            </>
          )}

          {/* ---------------- no analysis yet ---------------- */}
          {!analysis && !analyzing && (
            <div className="ats-card ats-empty-state">
              <div className="ats-empty-icon">🔍</div>
              <h2 className="ats-empty-title">No analysis yet</h2>
              <p className="ats-empty-text">
                Your resume is ready to be analysed. Click <strong>Analyze Resume</strong> above
                to get your skills breakdown, keyword coverage and improvement recommendations.
              </p>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default AtsAnalysis;
