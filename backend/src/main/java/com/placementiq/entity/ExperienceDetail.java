package com.placementiq.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * One work/internship/project experience entry recognised inside a resume.
 *
 * Like EducationDetail this is @Embeddable: it belongs to a single analysis and
 * is stored in the ats_analysis_experience collection table.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceDetail {
 
    @Column(name = "job_title", length = 200)
    private String jobTitle;
 
    @Column(name = "organization", length = 200)
    private String organization;
 
    /** Free text, e.g. "Jun 2024 - Aug 2024" or "6 months". */
    @Column(name = "duration", length = 120)
    private String duration;
 
    @Column(name = "description", length = 1000)
    private String description;
}
