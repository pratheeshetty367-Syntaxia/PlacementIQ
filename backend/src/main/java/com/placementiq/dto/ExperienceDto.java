package com.placementiq.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * One experience entry as exposed through the ATS analysis API.
 * Mirrors the ExperienceDetail embeddable.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceDto {
 
    private String jobTitle;
    private String organization;
    private String duration;
    private String description;
}
