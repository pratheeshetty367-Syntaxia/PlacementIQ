package com.placementiq.service;
 
import com.placementiq.config.ResumeStorageProperties;
import com.placementiq.exception.ResumeStorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
 
/**
 * Handles the physical storage of resume files on the server's file system.
 *
 * Files are written to the directory configured by placementiq.resume.upload-dir.
 * Storing the binary on disk (and only its metadata in PostgreSQL) keeps the
 * database small and is the simplest workable approach for this project - no
 * external object storage service is introduced.
 *
 * Every stored file gets a random UUID-based name prefixed with the owner's
 * user id, so two users uploading "resume.pdf" never collide and a client can
 * never influence the path that is written to.
 */
@Service
@RequiredArgsConstructor
public class ResumeStorageService {
 
    private static final Logger log = LoggerFactory.getLogger(ResumeStorageService.class);
 
    private final ResumeStorageProperties properties;
 
    private Path rootLocation;
 
    /**
     * Resolves the configured directory to an absolute path and creates it
     * on application start-up if it does not exist yet.
     */
    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(properties.getUploadDir())
                    .toAbsolutePath()
                    .normalize();
            Files.createDirectories(this.rootLocation);
            log.info("Resume upload directory ready at {}", this.rootLocation);
        } catch (IOException ex) {
            throw new ResumeStorageException(
                    "Could not create the resume upload directory: " + properties.getUploadDir(), ex);
        }
    }
 
    /**
     * Writes the given bytes to disk under a newly generated, collision-free name.
     *
     * @param content   raw bytes of the uploaded document
     * @param userId    id of the owning user, used as a file name prefix
     * @param extension file extension without the dot, e.g. "pdf"
     * @return the generated file name (not the full path)
     */
    public String store(byte[] content, Long userId, String extension) {
        String storedFileName = "user_" + userId + "_" + UUID.randomUUID() + "." + extension;
        Path target = rootLocation.resolve(storedFileName).normalize();
 
        // Defensive check: the resolved target must stay inside the root directory.
        if (!target.getParent().equals(rootLocation)) {
            throw new ResumeStorageException("Resolved storage path is outside the upload directory.");
        }
 
        try {
            Files.write(target, content);
            return storedFileName;
        } catch (IOException ex) {
            throw new ResumeStorageException("Failed to store the resume file on the server.", ex);
        }
    }
 
    /**
     * Loads a stored file as a Spring Resource so it can be streamed back to
     * the browser by the download endpoint.
     */
    public Resource loadAsResource(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResumeStorageException("The stored resume file could not be read from the server.");
            }
            return resource;
        } catch (IOException ex) {
            throw new ResumeStorageException("The stored resume file could not be read from the server.", ex);
        }
    }
 
    /**
     * Deletes a stored file. Never throws when the file is already gone, so a
     * database record can always be cleaned up even if the disk copy is missing.
     */
    public void delete(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }
        try {
            Path file = rootLocation.resolve(storedFileName).normalize();
            boolean deleted = Files.deleteIfExists(file);
            if (!deleted) {
                log.warn("Resume file {} was already absent from disk.", storedFileName);
            }
        } catch (IOException ex) {
            log.error("Could not delete resume file {}: {}", storedFileName, ex.getMessage());
        }
    }
 
    /**
     * @return the absolute path of a stored file, persisted on the Resume entity.
     */
    public String absolutePathOf(String storedFileName) {
        return rootLocation.resolve(storedFileName).normalize().toString();
    }
}
