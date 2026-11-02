package com.placementiq.exception;
 
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
 
import java.time.LocalDateTime;
import java.util.Map;
 
/**
 * Uniform error response shape returned to API clients on failure,
 * so the React frontend can rely on a consistent error contract.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
 
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
 
