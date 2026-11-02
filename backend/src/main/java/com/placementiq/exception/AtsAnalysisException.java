package com.placementiq.exception;
 
/**
 * Raised when the stored resume cannot be analysed: it has no extracted text
 * (for example a scanned, image-only PDF), the text is too short to be a real
 * resume, or the request itself breaks a business rule.
 *
 * This is a client-correctable situation, so the GlobalExceptionHandler maps it
 * to 400 Bad Request with a message the React page shows directly.
 */
public class AtsAnalysisException extends RuntimeException {
 
    public AtsAnalysisException(String message) {
        super(message);
    }
}
