package com.placementiq.controller;
 
import com.placementiq.dto.AtsAnalysisDto;
import com.placementiq.dto.AtsAnalysisRequest;
import com.placementiq.dto.AtsAnalysisSummaryDto;
import com.placementiq.security.UserPrincipal;
import com.placementiq.service.AtsAnalysisService;
import com.placementiq.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
/**
 * Protected REST endpoints for the ATS Resume Analysis module.
 *
 * Every path here sits outside /api/auth/** and /api/public/**, so the Part 1
 * SecurityConfig rule anyRequest().authenticated() already protects them - no
 * security configuration change was needed for this part. Exactly as in
 * UserController and ResumeController, the acting user comes from
 * @AuthenticationPrincipal and never from the request.
 *
 * Base path: /api/ats
 *   POST   /api/ats/analyze          - run a new analysis on the stored resume
 *   GET    /api/ats/me/latest        - most recent analysis (data is null if none)
 *   GET    /api/ats/me/history       - compact list of every past run
 *   GET    /api/ats/me/{id}          - one past analysis in full
 *   DELETE /api/ats/me/{id}          - delete one past analysis
 *   GET    /api/ats/me/status        - readiness check for the React page
 */
@RestController
@RequestMapping("/api/ats")
@RequiredArgsConstructor
public class AtsAnalysisController {
 
    private final AtsAnalysisService atsAnalysisService;
 
    /**
     * Runs a fresh analysis. The body is optional - a client with no target role
     * to send may omit it entirely, which is why @RequestBody is not required.
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AtsAnalysisDto>> analyzeResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody(required = false) AtsAnalysisRequest request) {
 
        AtsAnalysisDto analysis = atsAnalysisService.analyzeResume(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume analysed successfully", analysis));
    }
 
    /**
     * Returns the most recent analysis, or 200 with null data when the student
     * has not run one yet. A missing analysis is a normal empty state rather
     * than an error, so it is deliberately not a 404.
     */
    @GetMapping("/me/latest")
    public ResponseEntity<ApiResponse<AtsAnalysisDto>> getLatestAnalysis(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        AtsAnalysisDto latest = atsAnalysisService.getLatestAnalysis(principal.getId())
                .orElse(null);
 
        if (latest == null) {
            return ResponseEntity.ok(
                    ApiResponse.<AtsAnalysisDto>success("No ATS analysis has been run yet", null));
        }
        return ResponseEntity.ok(ApiResponse.success(latest));
    }
 
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<List<AtsAnalysisSummaryDto>>> getHistory(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        List<AtsAnalysisSummaryDto> history = atsAnalysisService.getAnalysisHistory(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
 
    @GetMapping("/me/{analysisId}")
    public ResponseEntity<ApiResponse<AtsAnalysisDto>> getAnalysis(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long analysisId) {
 
        AtsAnalysisDto analysis = atsAnalysisService.getAnalysis(principal.getId(), analysisId);
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }
 
    @DeleteMapping("/me/{analysisId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnalysis(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long analysisId) {
 
        atsAnalysisService.deleteAnalysis(principal.getId(), analysisId);
        return ResponseEntity.ok(ApiResponse.success("Analysis deleted successfully", null));
    }
 
    /**
     * Lets the React page decide whether to enable the Analyze button and what
     * to explain to the student, without having to provoke an error first.
     */
    @GetMapping("/me/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        Map<String, Object> status = new HashMap<>();
        status.put("resumeReady", atsAnalysisService.canAnalyze(principal.getId()));
        status.put("aiConfigured", atsAnalysisService.isAiConfigured());
        status.put("hasPreviousAnalysis",
                atsAnalysisService.getLatestAnalysis(principal.getId()).isPresent());
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
