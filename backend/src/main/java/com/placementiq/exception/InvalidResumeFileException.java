package com.placementiq.exception;
 
/**
 * Thrown by ResumeFileValidator when an uploaded file fails a business rule:
 * the part is missing or empty, the extension/MIME type is not an accepted
 * resume format, the file exceeds the configured size limit, or the file name
 * is unsafe. Mapped to HTTP 400 by the GlobalExceptionHandler.
 */
public class InvalidResumeFileException extends RuntimeException {
 
    public InvalidResumeFileException(String message) {
        super(message);
    }
}
