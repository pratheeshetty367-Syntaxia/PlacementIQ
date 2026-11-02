import React from 'react';

/**
 * Shows the education and experience entries the analysis recognised inside the
 * resume, plus the estimated total years of experience.
 *
 * These are extractions, not judgements: if a row looks wrong here, it usually
 * means the resume itself is ambiguous, which is useful feedback in its own right.
 */
const AtsProfileSections = ({ education, experience, totalExperienceYears }) => (
  <>
    <section className="ats-card">
      <h3 className="ats-section-title">
        Education
        <span className="ats-count">{education?.length ?? 0}</span>
      </h3>
      <p className="ats-section-description">
        Education entries recognised in your resume.
      </p>

      {education && education.length > 0 ? (
        <ul className="ats-entry-list">
          {education.map((entry, index) => (
            <li key={index} className="ats-entry">
              <span className="ats-entry-title">{entry.degree || 'Qualification not stated'}</span>
              {entry.institution && <span className="ats-entry-sub">{entry.institution}</span>}
              <span className="ats-entry-meta">
                {[entry.year, entry.score].filter(Boolean).join(' · ')}
              </span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="ats-section-empty">
          No education section could be recognised. An ATS expects a clearly headed
          Education section — consider adding one.
        </p>
      )}
    </section>

    <section className="ats-card">
      <h3 className="ats-section-title">
        Experience
        <span className="ats-count">{experience?.length ?? 0}</span>
      </h3>
      <p className="ats-section-description">
        Work, internship and project entries recognised in your resume
        {typeof totalExperienceYears === 'number' && (
          <> — estimated total: <strong>{totalExperienceYears} year(s)</strong></>
        )}
        .
      </p>

      {experience && experience.length > 0 ? (
        <ul className="ats-entry-list">
          {experience.map((entry, index) => (
            <li key={index} className="ats-entry">
              <span className="ats-entry-title">{entry.jobTitle || 'Role not stated'}</span>
              {entry.organization && <span className="ats-entry-sub">{entry.organization}</span>}
              {entry.duration && <span className="ats-entry-meta">{entry.duration}</span>}
              {entry.description && <p className="ats-entry-description">{entry.description}</p>}
            </li>
          ))}
        </ul>
      ) : (
        <p className="ats-section-empty">
          No experience entries could be recognised. Internships and substantial
          academic projects both count — list them with dates.
        </p>
      )}
    </section>
  </>
);

export default AtsProfileSections;
