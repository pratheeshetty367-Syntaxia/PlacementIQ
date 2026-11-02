package com.placementiq.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * One education entry as exposed through the ATS analysis API.
 * Mirrors the EducationDetail embeddable, keeping the API contract
 * independent of the persistence mapping.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationDto {
 
    private String degree;
    private String institution;
    private String year;
    private String score;
}
