package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.profile.UpdateProfileDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.service.IProfileService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/profile")
@RestController
public class ProfileController {

    private final IProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get user profile",
            description = "Retrieve the current user's profile information")
    public ResponseEntity<ResponsePayload> getProfile(HttpServletRequest request) {
        String userId = jwtTokenProvider.getIdFromToken(
                extractToken(request.getHeader("Authorization")));
        UserResponseDto profile = profileService.getProfile(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", profile))
                .build());
    }

    @PutMapping
    @Operation(summary = "Update user profile",
            description = "Update user profile including fullName, profileImage, password, and languagePreference")
    public ResponseEntity<ResponsePayload> updateProfile(
            @ModelAttribute UpdateProfileDto dto,
            HttpServletRequest request) {
        String userId = jwtTokenProvider.getIdFromToken(
                extractToken(request.getHeader("Authorization")));
        UserResponseDto profile = profileService.updateProfile(userId, dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", profile,
                        "message", "Profile updated successfully"))
                .build());
    }

    private String extractToken(String header) {
        String BEARER = "Bearer ";
        if (header != null && header.startsWith(BEARER)) {
            return header.substring(7);
        }
        return null;
    }
}
