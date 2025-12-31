package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.service.IStaffService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/staff")
@RestController
public class StaffController {

    private final IStaffService staffService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "Create a staff member",
            description = "Create a new staff member (OWNER only)")
    @PreAuthorize("hasAnyRole('SUPPLIER_OWNER', 'CUSTOMER_OWNER')")
    public ResponseEntity<ResponsePayload> createStaff(
            @RequestBody CreateStaffDto dto,
            HttpServletRequest request) {
        String ownerId = jwtTokenProvider.getIdFromToken(
                extractToken(request.getHeader("Authorization")));
        dto.setOwnerId(ownerId);
        UserResponseDto staff = staffService.createStaff(dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", staff,
                        "message", "Staff member created successfully"))
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
