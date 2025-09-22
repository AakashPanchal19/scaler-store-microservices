package com.scaler.store.user.controller;

import com.scaler.store.common.models.ApiResponse;
import com.scaler.store.user.dto.UpdateProfileRequest;
import com.scaler.store.user.dto.UserProfileResponse;
import com.scaler.store.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long resolvedId = null;
        if (headerUserId != null && !headerUserId.isBlank()) {
            resolvedId = Long.parseLong(headerUserId);
        } else if (paramUserId != null) {
            resolvedId = paramUserId;
        }

        if (resolvedId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }

        try {
            UserProfileResponse profile = userService.getProfile(resolvedId);
            return ResponseEntity.ok(ApiResponse.ok(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestBody UpdateProfileRequest request) {
        Long resolvedId = null;
        if (headerUserId != null && !headerUserId.isBlank()) {
            resolvedId = Long.parseLong(headerUserId);
        } else if (paramUserId != null) {
            resolvedId = paramUserId;
        }

        if (resolvedId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID not provided"));
        }

        try {
            UserProfileResponse updated = userService.updateProfile(resolvedId, request);
            return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable("id") Long id) {
        try {
            UserProfileResponse profile = userService.getProfile(id);
            return ResponseEntity.ok(ApiResponse.ok(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
