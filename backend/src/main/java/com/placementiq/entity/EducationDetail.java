package com.placementiq.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * One education entry recognised inside a resume.
 *
 * Declared @Embeddable rather than as its own entity because an education row
 * has no identity of its own - it only exists as part of the analysis that
 * produced it, and is stored in the ats_analysis_education collection table.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationDetail {
 
    @Column(name = "degree", length = 200)
    private String degree;
 
    @Column(name = "institution", length = 200)
    private String institution;
 
    /** Free text on purpose: resumes write this as "2024", "2021-2024" or "expected 2026". */
    @Column(name = "year", length = 60)
    private String year;
 
    /** CGPA/percentage exactly as written in the resume, or null when absent. */
    @Column(name = "score", length = 60)
    private String score;
}
