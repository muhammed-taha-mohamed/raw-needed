package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.admin.AdminUserDetailsDto;
import com.rawneeded.dto.admin.CreateAdminDto;
import com.rawneeded.dto.admin.UpdateAdminDto;
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

    @GetMapping("/suppliers")
    @Operation(summary = "Get suppliers (SUPPLIER_OWNER only)", description = "Paginated list of supplier owners")
    public ResponseEntity<ResponsePayload> getSuppliers(Pageable pageable) {
        Page<UserResponseDto> users = userService.getSuppliers(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", users))
                .build());
    }

    @GetMapping("/customers")
    @Operation(summary = "Get customers (CUSTOMER_OWNER only)", description = "Paginated list of customer owners")
    public ResponseEntity<ResponsePayload> getCustomers(Pageable pageable) {
        Page<UserResponseDto> users = userService.getCustomers(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", users))
                .build());
    }

    @GetMapping("/{userId}/details")
    @Operation(summary = "Get user details for admin", description = "User with subscription and staff list (for owners)")
    public ResponseEntity<ResponsePayload> getUserDetails(@PathVariable String userId) {
        AdminUserDetailsDto dto = userService.getUserDetailsForAdmin(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", dto))
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

    @GetMapping("/admin-accounts")
    @Operation(
            summary = "Get admin users",
            description = "Get paginated list of ADMIN and SUPER_ADMIN users (SUPER_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> getAdminUsers(Pageable pageable) {
        Page<UserResponseDto> users = userService.getAdminUsers(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", users))
                .build());
    }

    @PostMapping("/admin-accounts")
    @Operation(
            summary = "Create admin user",
            description = "Create ADMIN or SUPER_ADMIN user (SUPER_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> createAdminUser(@RequestBody CreateAdminDto dto) {
        UserResponseDto user = userService.createAdminUser(dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", user,
                        "message", "Admin user created successfully"))
                .build());
    }

    @PutMapping("/admin-accounts/{userId}")
    @Operation(
            summary = "Update admin user",
            description = "Update ADMIN or SUPER_ADMIN user (SUPER_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> updateAdminUser(@PathVariable String userId, @RequestBody UpdateAdminDto dto) {
        UserResponseDto user = userService.updateAdminUser(userId, dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", user,
                        "message", "Admin user updated successfully"))
                .build());
    }

    @DeleteMapping("/admin-accounts/{userId}")
    @Operation(
            summary = "Delete admin user",
            description = "Delete ADMIN user (SUPER_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> deleteAdminUser(@PathVariable String userId) {
        userService.deleteAdminUser(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Admin user deleted successfully"))
                .build());
    }
}
