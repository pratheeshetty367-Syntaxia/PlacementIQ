package com.placementiq.service;
 
import com.placementiq.dto.ResumeDto;
import com.placementiq.dto.ResumeTextDto;
import com.placementiq.dto.ResumeUploadResponse;
import com.placementiq.entity.Resume;
import com.placementiq.entity.User;
import com.placementiq.exception.ApiException;
import com.placementiq.exception.ResumeNotFoundException;
import com.placementiq.exception.ResumeStorageException;
import com.placementiq.repository.ResumeRepository;
import com.placementiq.repository.UserRepository;
import com.placementiq.util.ResumeTextExtractor;
import com.placementiq.validation.ResumeFileValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException;
 
/**
 * Business logic for the Resume Management module.
 *
 * Every method takes the user id resolved from the validated JWT by the
 * controller, so a user can only ever create, read, replace or delete their
 * own resume - there is no code path that accepts a resume id from a client.
 *
 * Responsibilities:
 *  - validate the uploaded file (delegated to ResumeFileValidator)
 *  - write the binary to disk (delegated to ResumeStorageService)
 *  - extract the plain text (delegated to ResumeTextExtractor)
 *  - persist / update / delete the Resume metadata row
 *  - map entities to DTOs
 */
@Service
@RequiredArgsConstructor
public class ResumeService {
 
    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);
 
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeFileValidator resumeFileValidator;
    private final ResumeStorageService resumeStorageService;
    private final ResumeTextExtractor resumeTextExtractor;
 
    /**
     * Stores a resume for a user who does not have one yet.
     *
     * @throws ApiException 409 CONFLICT if a resume already exists, so that an
     *                      accidental double upload cannot silently discard the
     *                      previous document; the client must call replace instead.
     */
    @Transactional
    public ResumeUploadResponse uploadResume(Long userId, MultipartFile file) {
        if (resumeRepository.existsByUserId(userId)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A resume already exists for this account. Use the replace option to update it.");
        }
        return saveResume(userId, file, null);
    }
 
    /**
     * Replaces the authenticated user's existing resume with a new document.
     * The previously stored file is removed from disk and the same database row
     * is updated, so the one-resume-per-user rule always holds.
     */
    @Transactional
    public ResumeUploadResponse replaceResume(Long userId, MultipartFile file) {
        Resume existing = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResumeNotFoundException(
                        "No resume found for this account. Upload one first."));
        return saveResume(userId, file, existing);
    }
 
    /**
     * @return the metadata of the authenticated user's resume
     * @throws ResumeNotFoundException 404 when nothing has been uploaded yet
     */
    @Transactional(readOnly = true)
    public ResumeDto getResume(Long userId) {
        Resume resume = requireResume(userId);
        return toDto(resume);
    }
 
    /**
     * @return the plain text extracted from the stored document
     */
    @Transactional(readOnly = true)
    public ResumeTextDto getResumeText(Long userId) {
        Resume resume = requireResume(userId);
        String text = resume.getExtractedText() == null ? "" : resume.getExtractedText();
        return ResumeTextDto.builder()
                .resumeId(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .characterCount(text.length())
                .extractedText(text)
                .build();
    }
 
    /**
     * Loads the stored file for download, together with the metadata the
     * controller needs to build the Content-Disposition/Content-Type headers.
     */
    @Transactional(readOnly = true)
    public ResumeDownload downloadResume(Long userId) {
        Resume resume = requireResume(userId);
        Resource resource = resumeStorageService.loadAsResource(resume.getStoredFileName());
        return new ResumeDownload(resource, resume.getOriginalFileName(), resume.getContentType());
    }
 
    /**
     * Deletes both the database record and the file on disk.
     */
    @Transactional
    public void deleteResume(Long userId) {
        Resume resume = requireResume(userId);
        String storedFileName = resume.getStoredFileName();
        resumeRepository.delete(resume);
        resumeStorageService.delete(storedFileName);
        log.info("Deleted resume {} for user {}", storedFileName, userId);
    }
 
    /**
     * @return true if the authenticated user currently has a resume stored
     */
    @Transactional(readOnly = true)
    public boolean hasResume(Long userId) {
        return resumeRepository.existsByUserId(userId);
    }
 
    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------
 
    /**
     * Shared upload/replace routine: validate -> read bytes -> extract text ->
     * write to disk -> insert or update the metadata row.
     *
     * @param existing the row to update, or null to insert a new one
     */
    private ResumeUploadResponse saveResume(Long userId, MultipartFile file, Resume existing) {
        Resume.FileType fileType = resumeFileValidator.validate(file);
        String extension = fileType == Resume.FileType.PDF ? "pdf" : "docx";
 
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException ex) {
            throw new ResumeStorageException("Could not read the uploaded resume file.", ex);
        }
 
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResumeNotFoundException("User not found with id: " + userId));
 
        String extractedText = resumeTextExtractor.extract(content, fileType);
        String storedFileName = resumeStorageService.store(content, userId, extension);
 
        Resume resume = (existing != null) ? existing : Resume.builder().user(user).build();
        String previousStoredFileName = (existing != null) ? existing.getStoredFileName() : null;
 
        resume.setUser(user);
        resume.setOriginalFileName(file.getOriginalFilename());
        resume.setStoredFileName(storedFileName);
        resume.setStoragePath(resumeStorageService.absolutePathOf(storedFileName));
        resume.setFileType(fileType);
        resume.setContentType(resolveContentType(fileType));
        resume.setFileSize(file.getSize());
        resume.setExtractedText(extractedText);
 
        Resume saved = resumeRepository.save(resume);
 
        // Only remove the old file once the new record is safely in place.
        if (previousStoredFileName != null) {
            resumeStorageService.delete(previousStoredFileName);
        }
 
        boolean extracted = !extractedText.isBlank();
        return ResumeUploadResponse.builder()
                .resume(toDto(saved))
                .replacedExisting(existing != null)
                .textExtractionSucceeded(extracted)
                .extractionNote(extracted
                        ? "Text extracted successfully."
                        : "The file was stored, but no readable text could be extracted "
                          + "(the document may be a scanned image).")
                .build();
    }
 
    private Resume requireResume(Long userId) {
        return resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResumeNotFoundException(
                        "No resume has been uploaded for this account yet."));
    }
 
    /**
     * Normalises the stored content type so downloads always carry the correct
     * MIME type, even when the browser sent application/octet-stream.
     */
    private String resolveContentType(Resume.FileType fileType) {
        return fileType == Resume.FileType.PDF
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }
 
    private ResumeDto toDto(Resume resume) {
        String text = resume.getExtractedText() == null ? "" : resume.getExtractedText();
        return ResumeDto.builder()
                .id(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .fileType(resume.getFileType())
                .contentType(resume.getContentType())
                .fileSize(resume.getFileSize())
                .fileSizeReadable(humanReadableSize(resume.getFileSize()))
                .textExtracted(!text.isBlank())
                .extractedCharacterCount(text.length())
                .uploadedAt(resume.getUploadedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
 
    private String humanReadableSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0 KB";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
 
    /**
     * Small value holder returned by downloadResume so the controller can build
     * the response without touching the Resume entity or the storage layer.
     */
    public record ResumeDownload(Resource resource, String fileName, String contentType) {
    }
}
