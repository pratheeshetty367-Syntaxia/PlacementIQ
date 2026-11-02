package com.placementiq.config;
 
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
 
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
 
/**
 * Type-safe binding for the "placementiq.resume.*" block of application.yml.
 *
 * Keeps the upload directory, the maximum accepted file size and the list of
 * allowed extensions in configuration rather than hard-coded in the service,
 * so they can be changed per environment without touching Java code.
 */
@Component
@ConfigurationProperties(prefix = "placementiq.resume")
@Getter
@Setter
public class ResumeStorageProperties {
 
    /** Directory in which uploaded resume files are stored. */
    private String uploadDir = "./uploads/resumes";
 
    /** Hard limit applied by the validator, in bytes (default 5 MB). */
    private long maxFileSizeBytes = 5_242_880L;
 
    /** Comma separated list of accepted file extensions, without the dot. */
    private String allowedExtensions = "pdf,docx";
 
    /**
     * @return the configured extensions as a lower-cased list, e.g. ["pdf", "docx"].
     */
    public List<String> getAllowedExtensionList() {
        return Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .filter(ext -> !ext.isEmpty())
                .map(ext -> ext.toLowerCase(Locale.ROOT))
                .toList();
    }
}
