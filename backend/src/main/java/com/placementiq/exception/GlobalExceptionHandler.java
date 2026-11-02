package com.placementiq.exception;
 
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
 
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
 
/**
 * Intercepts exceptions thrown anywhere in the controller layer and
 * converts them into a consistent ApiError JSON response, instead of
 * leaking stack traces or default Spring error pages to the client.
 *
 * Part 2 added three resume-specific handlers (invalid file, storage failure,
 * upload too large / missing part). Part 3 adds two more for the ATS module:
 * AtsAnalysisException (400) and OpenAiServiceException, which carries its own
 * status so an upstream timeout or outage is reported as 504/503 rather than a
 * generic 500. ResumeNotFoundException and AtsAnalysisNotFoundException need no
 * handlers of their own because both extend ResourceNotFoundException and are
 * therefore already mapped to 404 below.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
 
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(error);
    }
 
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
 
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Invalid email or password")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
 
    // ------------------------------------------------------------------
    // ADDED IN PART 2 - Resume Management module
    // ------------------------------------------------------------------
 
    /**
     * Raised by ResumeFileValidator for an unsupported format, an oversized
     * file, a missing file or an unsafe file name. This is a client mistake,
     * so it becomes 400 Bad Request with a message the React page can display.
     */
    @ExceptionHandler(InvalidResumeFileException.class)
    public ResponseEntity<ApiError> handleInvalidResumeFile(InvalidResumeFileException ex,
                                                            HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
 
    /**
     * Raised when the server cannot write/read/delete the file on disk.
     * This is a server fault, so it becomes 500 Internal Server Error.
     */
    @ExceptionHandler(ResumeStorageException.class)
    public ResponseEntity<ApiError> handleResumeStorage(ResumeStorageException ex,
                                                        HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
 
    /**
     * Thrown by Spring's multipart resolver before the controller is reached
     * when the upload exceeds spring.servlet.multipart.max-file-size. Without
     * this handler the client would receive an opaque 500.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex,
                                                        HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase())
                .message("The uploaded resume is too large. Maximum allowed size is 5 MB.")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }
 
    /**
     * Thrown when a multipart request arrives without the expected "file" part.
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleMissingPart(MissingServletRequestPartException ex,
                                                      HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("No resume file was included in the request (expected form field 'file').")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
 
    // ------------------------------------------------------------------
    // ADDED IN PART 3 - ATS Resume Analysis module
    // ------------------------------------------------------------------
 
    /**
     * Raised when the stored resume cannot be analysed - typically because no
     * readable text could be extracted from it. The student can fix this by
     * uploading a text-based file, so it is a 400 with an actionable message.
     */
    @ExceptionHandler(AtsAnalysisException.class)
    public ResponseEntity<ApiError> handleAtsAnalysis(AtsAnalysisException ex,
                                                      HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
 
    /**
     * Raised for any failure of the OpenAI call: missing key, rejected key, rate
     * limiting, timeout, or an unreadable reply. The exception already carries
     * the status PlacementIQ should return (503, 502 or 504), so it is used
     * directly rather than being flattened to a generic 500.
     */
    @ExceptionHandler(OpenAiServiceException.class)
    public ResponseEntity<ApiError> handleOpenAiService(OpenAiServiceException ex,
                                                        HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(error);
    }
 
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
