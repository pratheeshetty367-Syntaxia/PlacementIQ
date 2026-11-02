package com.placementiq.controller;
 
import com.placementiq.dto.UserProfileDto;
import com.placementiq.security.UserPrincipal;
import com.placementiq.service.UserService;
import com.placementiq.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
 
/**
 * Protected endpoints for reading and updating the logged-in user's profile.
 * The current user is resolved from the JWT via @AuthenticationPrincipal -
 * no user id is ever taken from the request body/path, which prevents a
 * client from reading/editing someone else's profile.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
 
    private final UserService userService;
 
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileDto profile = userService.getProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
 
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UserProfileDto dto) {
        UserProfileDto updated = userService.updateProfile(principal.getId(), dto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }
}
 
