package com.placementiq.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Carries the plain text extracted from the stored resume document.
 *
 * Kept separate from ResumeDto because the text can be several thousand
 * characters long and is only needed by the resume preview screen (and,
 * in later parts, by the ATS analysis and job-matching modules).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeTextDto {
 
    private Long resumeId;
    private String originalFileName;
    private int characterCount;
    private String extractedText;
}
