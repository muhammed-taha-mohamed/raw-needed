package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.service.ISupplierDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/supplier/dashboard")
@RestController
public class SupplierDashboardController {

    private final ISupplierDashboardService supplierDashboardService;

    @GetMapping("/stats")
    @Operation(
            summary = "Get supplier dashboard statistics",
            description = "Get comprehensive dashboard statistics for supplier based on ownerId from token"
    )
    public ResponseEntity<ResponsePayload> getDashboardStats() {
        DashboardStatsDto stats = supplierDashboardService.getDashboardStats();
        
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", stats))
                .build()
        );
    }
}
