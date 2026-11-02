package com.placementiq.controller;
 
import com.placementiq.dto.AuthResponse;
import com.placementiq.dto.LoginRequest;
import com.placementiq.dto.RegisterRequest;
import com.placementiq.service.AuthService;
import com.placementiq.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
/**
 * Public authentication endpoints: registration, login and logout.
 * These endpoints are explicitly permitted (not secured) in SecurityConfig
 * since a client cannot have a JWT before authenticating.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
 
    private final AuthService authService;
 
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }
 
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
 
    /**
     * Stateless JWT logout: since no server-side session/token store is used,
     * the client is responsible for discarding the token. This endpoint is
     * provided so the frontend has a single place to call, and so a future
     * token-blacklist strategy can be added here without an API contract change.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
 
