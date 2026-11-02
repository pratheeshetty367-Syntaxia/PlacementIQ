package com.placementiq.controller;
 
import com.placementiq.dto.ResumeDto;
import com.placementiq.dto.ResumeTextDto;
import com.placementiq.dto.ResumeUploadResponse;
import com.placementiq.security.UserPrincipal;
import com.placementiq.service.ResumeService;
import com.placementiq.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Map;
 
/**
 * Protected REST endpoints for the Resume Management module.
 *
 * All endpoints require a valid JWT: SecurityConfig already secures every path
 * that is not under /api/auth/** or /api/public/**, so no extra configuration
 * is needed here. Exactly as in UserController, the acting user is resolved
 * from the token via @AuthenticationPrincipal and never from the request, which
 * makes it impossible to read or delete another student's resume.
 *
 * Base path: /api/resumes
 *   POST   /api/resumes/upload        - upload a resume (multipart/form-data)
 *   GET    /api/resumes/me            - resume metadata of the logged-in user
 *   GET    /api/resumes/me/text       - plain text extracted from the resume
 *   GET    /api/resumes/me/download   - download the original file
 *   GET    /api/resumes/me/exists     - lightweight existence check
 *   PUT    /api/resumes/me            - replace the stored resume
 *   DELETE /api/resumes/me            - delete the stored resume
 */
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
 
    private final ResumeService resumeService;
 
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeUploadResponse>> uploadResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
 
        ResumeUploadResponse response = resumeService.uploadResume(principal.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume uploaded successfully", response));
    }
 
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeUploadResponse>> replaceResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
 
        ResumeUploadResponse response = resumeService.replaceResume(principal.getId(), file);
        return ResponseEntity.ok(ApiResponse.success("Resume replaced successfully", response));
    }
 
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ResumeDto>> getMyResume(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        ResumeDto resume = resumeService.getResume(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(resume));
    }
 
    @GetMapping("/me/text")
    public ResponseEntity<ApiResponse<ResumeTextDto>> getMyResumeText(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        ResumeTextDto text = resumeService.getResumeText(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(text));
    }
 
    /**
     * Lets the React page decide between the "empty" and "loaded" states without
     * treating a 404 as an error.
     */
    @GetMapping("/me/exists")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> resumeExists(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        boolean exists = resumeService.hasResume(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("exists", exists)));
    }
 
    /**
     * Streams the original file back to the browser. The response is a raw file
     * download rather than the usual ApiResponse envelope, because the body is
     * the document itself.
     */
    @GetMapping("/me/download")
    public ResponseEntity<Resource> downloadMyResume(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        ResumeService.ResumeDownload download = resumeService.downloadResume(principal.getId());
        String encodedName = URLEncoder.encode(download.fileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
 
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName)
                .body(download.resource());
    }
 
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyResume(
            @AuthenticationPrincipal UserPrincipal principal) {
 
        resumeService.deleteResume(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully", null));
    }
}
