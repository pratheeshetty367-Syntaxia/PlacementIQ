package com.placementiq.exception;
 
import lombok.Getter;
import org.springframework.http.HttpStatus;
 
/**
 * Generic application exception that carries the HTTP status that
 * should be returned to the client (e.g. 409 Conflict on duplicate email).
 */
@Getter
public class ApiException extends RuntimeException {
 
    private final HttpStatus status;
 
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
 
