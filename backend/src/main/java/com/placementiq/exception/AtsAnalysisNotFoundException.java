package com.placementiq.exception;
 
/**
 * Raised when no analysis exists for the requested id, or when the requested
 * analysis does not belong to the authenticated user.
 *
 * Both cases deliberately produce the same 404: telling a caller that an
 * analysis exists but belongs to somebody else would itself leak information.
 * Extends ResourceNotFoundException so the existing 404 handler picks it up
 * with no extra wiring.
 */
public class AtsAnalysisNotFoundException extends ResourceNotFoundException {
 
    public AtsAnalysisNotFoundException(String message) {
        super(message);
    }
}
