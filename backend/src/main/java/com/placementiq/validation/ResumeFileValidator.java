package com.placementiq.validation;
 
import com.placementiq.config.ResumeStorageProperties;
import com.placementiq.entity.Resume;
import com.placementiq.exception.InvalidResumeFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;
import java.util.Locale;
import java.util.Set;
 
/**
 * Validates an uploaded resume before it is written to disk or persisted.
 *
 * Bean Validation annotations cannot express these rules (they apply to a
 * binary multipart part, not to a JSON field), so the checks live in a
 * dedicated @Component that the ResumeService calls on every upload/replace:
 *
 *  1. the multipart part is present and not empty
 *  2. the original file name exists and contains no path-traversal characters
 *  3. the extension is one of the configured allowed extensions
 *  4. the declared MIME type matches that extension
 *  5. the file size is within the configured limit
 *
 * Any failure raises InvalidResumeFileException, which the GlobalExceptionHandler
 * converts into a 400 Bad Request ApiError.
 */
@Component
@RequiredArgsConstructor
public class ResumeFileValidator {
 
    private static final Set<String> PDF_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/x-pdf"
    );
 
    private static final Set<String> DOCX_CONTENT_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/octet-stream" // some browsers send this for .docx
    );
 
    private final ResumeStorageProperties properties;
 
    /**
     * Runs every validation rule and returns the resolved document type.
     *
     * @param file the uploaded multipart part
     * @return PDF or DOCX
     * @throws InvalidResumeFileException if any rule fails
     */
    public Resume.FileType validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidResumeFileException("Please select a resume file to upload.");
        }
 
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
 
        if (!StringUtils.hasText(originalName)) {
            throw new InvalidResumeFileException("The uploaded file has no file name.");
        }
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new InvalidResumeFileException("The file name contains invalid path characters.");
        }
        if (originalName.length() > 255) {
            throw new InvalidResumeFileException("The file name is too long (maximum 255 characters).");
        }
 
        String extension = extractExtension(originalName);
        List<String> allowed = properties.getAllowedExtensionList();
        if (!allowed.contains(extension)) {
            throw new InvalidResumeFileException(
                    "Unsupported resume format '." + extension + "'. Allowed formats: "
                            + String.join(", ", allowed.stream().map(e -> "." + e).toList()) + ".");
        }
 
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new InvalidResumeFileException(
                    "The resume is too large. Maximum allowed size is "
                            + (properties.getMaxFileSizeBytes() / (1024 * 1024)) + " MB.");
        }
 
        Resume.FileType fileType = "pdf".equals(extension) ? Resume.FileType.PDF : Resume.FileType.DOCX;
        validateContentType(file.getContentType(), fileType);
        return fileType;
    }
 
    /**
     * Extracts the lower-cased extension of a file name, without the dot.
     */
    public String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new InvalidResumeFileException("The file must have an extension (.pdf or .docx).");
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
 
    /**
     * Cross-checks the browser-declared MIME type against the file extension,
     * so a renamed file (e.g. "virus.exe" saved as "cv.pdf") is rejected early.
     */
    private void validateContentType(String contentType, Resume.FileType fileType) {
        if (!StringUtils.hasText(contentType)) {
            // Nothing declared by the client; the extension check above already applied.
            return;
        }
        String normalized = contentType.toLowerCase(Locale.ROOT).trim();
        boolean matches = (fileType == Resume.FileType.PDF)
                ? PDF_CONTENT_TYPES.contains(normalized)
                : DOCX_CONTENT_TYPES.contains(normalized);
 
        if (!matches) {
            throw new InvalidResumeFileException(
                    "The file content type '" + contentType + "' does not match a " + fileType + " document.");
        }
    }
}
