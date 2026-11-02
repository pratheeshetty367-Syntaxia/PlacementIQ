import React from 'react';

/**
 * Renders the ATS-oriented improvement suggestions as a numbered list, and
 * below them any standard resume sections the analysis found to be missing
 * entirely.
 *
 * Suggestions are the most actionable part of the analysis, so they are
 * numbered rather than bulleted to signal an order to work through.
 */
const AtsRecommendations = ({ suggestions, missingSections }) => (
  <section className="ats-card">
    <h3 className="ats-section-title">
      Recommendations
      <span className="ats-count">{suggestions?.length ?? 0}</span>
    </h3>
    <p className="ats-section-description">
      Specific changes that would make this resume easier for an ATS to parse and for a
      recruiter to shortlist.
    </p>

    {suggestions && suggestions.length > 0 ? (
      <ol className="ats-suggestion-list">
        {suggestions.map((suggestion, index) => (
          <li key={index} className="ats-suggestion">
            {suggestion}
          </li>
        ))}
      </ol>
    ) : (
      <p className="ats-section-empty">No specific recommendations were returned for this resume.</p>
    )}

    {missingSections && missingSections.length > 0 && (
      <div className="ats-missing-sections">
        <h4 className="ats-subsection-title">Sections that appear to be missing</h4>
        <ul className="ats-chip-list">
          {missingSections.map((section) => (
            <li key={section} className="ats-chip ats-chip-missing">
              {section}
            </li>
          ))}
        </ul>
      </div>
    )}
  </section>
);

export default AtsRecommendations;
