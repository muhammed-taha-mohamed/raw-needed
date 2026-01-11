package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.UserRequestDto;
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
@RequestMapping("api/v1/user")
@RestController
public class UserController {

    private final IUserService userService;

    // ================= CREATE =================

    @PostMapping("/create-staff-user")
    @Operation(
            summary = "Create a new staff user",
            description = "This API is used to register a new staff user"
    )
    public ResponseEntity<ResponsePayload> register(@RequestBody CreateStaffDto dto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userService.createStaffUser(dto)))
                .build()
        );
    }

    // ================= READ =================

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by id",
            description = "Fetch user details using userId"
    )
    public ResponseEntity<ResponsePayload> findById(@PathVariable String userId) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userService.findById(userId)))
                .build()
        );
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(
            summary = "Filter users by owner id",
            description = "Get paginated users list by ownerId"
    )
    public ResponseEntity<ResponsePayload> filterByOwnerId(
            @PathVariable String ownerId,
            Pageable pageable
    ) {
        Page<?> page = userService.filterByOwnerId(ownerId, pageable);

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", page))
                .build()
        );
    }

    // ================= UPDATE =================

    @PatchMapping("/{userId}")
    @Operation(
            summary = "Update user",
            description = "Update user data using userId"
    )
    public ResponseEntity<ResponsePayload> update(
            @PathVariable String userId,
            @RequestBody UserRequestDto dto
    ) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userService.update(userId, dto)))
                .build()
        );
    }

    // ================= DELETE =================

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Delete user",
            description = "Delete user by userId"
    )
    public ResponseEntity<ResponsePayload> delete(@PathVariable String userId) {
        userService.delete(userId);

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "User deleted successfully"))
                .build()
        );
    }
}
