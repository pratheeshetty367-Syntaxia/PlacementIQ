package com.placementiq.exception;
 
/**
 * Thrown when the server itself fails to write, read or delete a resume file
 * on disk (permissions, missing directory, I/O error). This is a server-side
 * fault rather than a client mistake, so it is mapped to HTTP 500.
 */
public class ResumeStorageException extends RuntimeException {
 
    public ResumeStorageException(String message) {
        super(message);
    }
 
    public ResumeStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
