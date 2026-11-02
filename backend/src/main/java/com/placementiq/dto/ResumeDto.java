package com.placementiq.dto;
 
import com.placementiq.entity.Resume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDateTime;
 
/**
 * Data transfer object returned by the resume endpoints.
 *
 * Exposes only resume metadata - never the storage path on the server and
 * never the full extracted text - so the API contract stays decoupled from
 * the Resume JPA entity and from the server's file system layout.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDto {
 
    private Long id;
    private String originalFileName;
    private Resume.FileType fileType;
    private String contentType;
    private Long fileSize;
    private String fileSizeReadable;
    private boolean textExtracted;
    private int extractedCharacterCount;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}
