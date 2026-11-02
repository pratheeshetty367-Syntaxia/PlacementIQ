package com.placementiq.exception;
 
/**
 * Thrown when a requested entity (User, UserProfile, etc.) cannot be found.
 * Mapped to HTTP 404 by the GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
 
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
 
