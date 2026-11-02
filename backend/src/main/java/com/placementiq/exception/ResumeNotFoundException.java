package com.placementiq.exception;
 
/**
 * Thrown when the authenticated user has no resume stored yet (or the stored
 * record has already been deleted). Extends ResourceNotFoundException so it is
 * picked up by the existing 404 handler in GlobalExceptionHandler without any
 * extra wiring, while still being catchable as a distinct resume-specific type.
 */
public class ResumeNotFoundException extends ResourceNotFoundException {
 
    public ResumeNotFoundException(String message) {
        super(message);
    }
}
