package com.placementiq.controller;
 
import com.placementiq.security.UserPrincipal;
import com.placementiq.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.Map;
 
/**
 * Minimal protected endpoint used by the React Dashboard placeholder page
 * to confirm that a stored JWT is accepted by the backend.
 * Real placement-preparation/AI features (Part 2+) will extend this controller.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
 
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        Map<String, Object> summary = Map.of(
                "welcomeMessage", "Welcome back, " + principal.getFullName() + "!",
                "userId", principal.getId(),
                "modulesCompleted", 0,
                "modulesInProgress", 0
        );
 
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
 
