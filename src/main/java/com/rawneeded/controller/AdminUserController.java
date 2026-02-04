package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/admin/users")
@RestController
public class AdminUserController {

    private final IUserService userService;

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Get paginated list of all users (SUPER_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> getAllUsers(Pageable pageable) {
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", users))
                .build());
    }

    @PutMapping("/{userId}/activate")
    @Operation(
            summary = "Activate a user",
            description = "Activate a user account. If user is OWNER, all their staff members will also be activated (SUPER_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> activateUser(@PathVariable String userId) {
        UserResponseDto user = userService.activateUser(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", user,
                        "message", "User activated successfully"))
                .build());
    }

    @PutMapping("/{userId}/deactivate")
    @Operation(
            summary = "Deactivate a user",
            description = "Deactivate a user account. If user is OWNER, all their staff members will also be deactivated (SUPER_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> deactivateUser(@PathVariable String userId) {
        UserResponseDto user = userService.deactivateUser(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", user,
                        "message", "User deactivated successfully"))
                .build());
    }
}
