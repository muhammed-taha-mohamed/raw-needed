package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.subscription.QuotationResponseDto;
import com.rawneeded.service.IAdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/admin")
@RestController
public class AdminController {

    private final IAdminService adminService;

    @GetMapping("/quotations/pending")
    @Operation(summary = "Get all pending quotations",
            description = "Retrieve all pending quotations (SYSTEM_ADMIN only)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ResponsePayload> getAllPendingQuotations() {
        List<QuotationResponseDto> quotations = adminService.getAllPendingQuotations();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", quotations))
                .build());
    }

    @PostMapping("/quotations/{quotationId}/approve")
    @Operation(summary = "Approve a quotation",
            description = "Approve a quotation and activate the owner's account (SYSTEM_ADMIN only)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ResponsePayload> approveQuotation(@PathVariable String quotationId) {
        QuotationResponseDto quotation = adminService.approveQuotation(quotationId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", quotation,
                        "message", "Quotation approved successfully. Owner account activated."))
                .build());
    }

    @PostMapping("/quotations/{quotationId}/reject")
    @Operation(summary = "Reject a quotation",
            description = "Reject a quotation (SYSTEM_ADMIN only)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ResponsePayload> rejectQuotation(
            @PathVariable String quotationId,
            @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "No reason provided");
        QuotationResponseDto quotation = adminService.rejectQuotation(quotationId, reason);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", quotation,
                        "message", "Quotation rejected successfully."))
                .build());
    }
}
