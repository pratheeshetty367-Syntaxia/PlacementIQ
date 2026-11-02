import React from 'react';

/**
 * Reusable chip list used for the three keyword-style sections: skills found,
 * keywords present, and missing/recommended keywords.
 *
 * The variant prop only changes the colour treatment - "found" for things the
 * resume already contains, "missing" for recommendations. Keeping one component
 * for all three keeps the sections visually consistent.
 */
const AtsKeywordList = ({ title, description, items, variant = 'found', emptyMessage }) => (
  <section className="ats-card">
    <h3 className="ats-section-title">
      {title}
      <span className="ats-count">{items?.length ?? 0}</span>
    </h3>
    {description && <p className="ats-section-description">{description}</p>}

    {items && items.length > 0 ? (
      <ul className="ats-chip-list">
        {items.map((item) => (
          <li key={item} className={`ats-chip ats-chip-${variant}`}>
            {item}
          </li>
        ))}
      </ul>
    ) : (
      <p className="ats-section-empty">{emptyMessage || 'Nothing to show here.'}</p>
    )}
  </section>
);

export default AtsKeywordList;
