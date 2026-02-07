package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.dashboard.AdSubscriptionStatsDto;
import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.dto.dashboard.PendingCountsDto;
import com.rawneeded.dto.dashboard.SubscriptionSummaryDto;
import com.rawneeded.dto.dashboard.UserStatsDto;
import com.rawneeded.service.IAdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
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
                .build());
    }

    @GetMapping("/subscription-summary")
    @Operation(summary = "Get subscription summary for dashboard (total, active, pending, this month)")
    public ResponseEntity<ResponsePayload> getSubscriptionSummary() {
        SubscriptionSummaryDto dto = adminDashboardService.getSubscriptionSummary();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", dto))
                .build());
    }

    @GetMapping("/user-stats")
    @Operation(summary = "Get user counts (total, suppliers, customers)")
    public ResponseEntity<ResponsePayload> getUserStats() {
        UserStatsDto dto = adminDashboardService.getUserStats();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", dto))
                .build());
    }

    @GetMapping("/ad-subscription-stats")
    @Operation(summary = "Get ad subscription counts (total, active, pending)")
    public ResponseEntity<ResponsePayload> getAdSubscriptionStats() {
        AdSubscriptionStatsDto dto = adminDashboardService.getAdSubscriptionStats();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", dto))
                .build());
    }

    @GetMapping("/pending-counts")
    @Operation(summary = "Get pending counts for sidebar badges (subscriptions, ad subscriptions, add-searches)")
    public ResponseEntity<ResponsePayload> getPendingCounts() {
        PendingCountsDto dto = adminDashboardService.getPendingCounts();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", dto))
                .build());
    }

    @GetMapping("/historical-subscriptions")
    @Operation(summary = "Get historical (replaced) subscriptions list")
    public ResponseEntity<ResponsePayload> getHistoricalSubscriptions() {
        List<?> list = adminDashboardService.getHistoricalSubscriptions();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", list))
                .build());
    }
}
