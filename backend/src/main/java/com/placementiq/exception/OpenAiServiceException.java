package com.placementiq.exception;
 
import lombok.Getter;
import org.springframework.http.HttpStatus;
 
/**
 * Raised when the call to the OpenAI API cannot be completed or its reply
 * cannot be used: the key is missing, the request was rejected, the service
 * timed out or is unavailable, or the reply was not parseable JSON.
 *
 * It carries the HTTP status PlacementIQ should return to its own client, which
 * is not necessarily the status OpenAI returned - an OpenAI 429 becomes a 503
 * here, because from the student's point of view the analysis service is simply
 * busy. The GlobalExceptionHandler uses this status directly.
 */
@Getter
public class OpenAiServiceException extends RuntimeException {
 
    private final HttpStatus status;
 
    public OpenAiServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
 
    public OpenAiServiceException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
