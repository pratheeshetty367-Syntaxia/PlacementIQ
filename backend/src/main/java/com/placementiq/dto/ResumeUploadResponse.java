package com.placementiq.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Response payload for the upload and replace endpoints.
 *
 * Besides the stored resume metadata it reports whether an existing document
 * was replaced and whether text extraction succeeded, so the React upload
 * screen can show an accurate status message without issuing a second request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeUploadResponse {
 
    private ResumeDto resume;
    private boolean replacedExisting;
    private boolean textExtractionSucceeded;
    private String extractionNote;
}
