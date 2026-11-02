package com.placementiq.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Data transfer object exposed via the profile endpoints.
 * Decouples the API contract from the UserProfile JPA entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
 
    private Long id;
    private String fullName;
    private String email;
    private String college;
    private String branch;
    private Integer graduationYear;
    private String skills;
    private String targetRole;
    private String resumeUrl;
    private String phoneNumber;
    private String bio;
}
 
