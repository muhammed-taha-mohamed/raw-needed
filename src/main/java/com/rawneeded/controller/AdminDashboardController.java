package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.service.IAdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/admin/dashboard")
@RestController
public class AdminDashboardController {

    private final IAdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    @Operation(
            summary = "Get admin dashboard statistics",
            description = "Get comprehensive dashboard statistics for admin user based on userId from token"
    )
    public ResponseEntity<ResponsePayload> getDashboardStats() {
        DashboardStatsDto stats = adminDashboardService.getDashboardStats();
        
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", stats))
                .build()
        );
    }
}
