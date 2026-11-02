package com.placementiq.util;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
 
import java.time.LocalDateTime;
 
/**
 * Generic wrapper used for successful API responses so every endpoint
 * returns a predictable shape: { success, message, data, timestamp }.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
 
    private boolean success;
    private String message;
    private T data;
 
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
 
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
 
    public static <T> ApiResponse<T> success(T data) {
        return success("Request successful", data);
    }
}
 
