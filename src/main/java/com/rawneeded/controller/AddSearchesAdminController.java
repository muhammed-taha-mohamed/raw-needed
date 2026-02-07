package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.subscription.AddSearchesRequestDto;
import com.rawneeded.service.IUserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/admin/add-searches")
@RestController
public class AddSearchesAdminController {

    private final IUserSubscriptionService userSubscriptionService;

    @GetMapping("/pending")
    @Operation(summary = "List pending add-searches requests",
            description = "Get all pending requests to add more searches (admin only)")
    public ResponseEntity<ResponsePayload> getPendingAddSearches(Pageable pageable) {
        Page<AddSearchesRequestDto> page = userSubscriptionService.getPendingAddSearchesRequests(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", page))
                .build());
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Approve add-searches request",
            description = "Approve request and add searches to user subscription (admin only)")
    public ResponseEntity<ResponsePayload> approveAddSearches(@PathVariable String requestId) {
        AddSearchesRequestDto result = userSubscriptionService.approveAddSearchesRequest(requestId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", result,
                        "message", "Add searches request approved. Searches added to subscription."))
                .build());
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject add-searches request",
            description = "Reject add-searches request (admin only)")
    public ResponseEntity<ResponsePayload> rejectAddSearches(
            @PathVariable String requestId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        AddSearchesRequestDto result = userSubscriptionService.rejectAddSearchesRequest(requestId, reason);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", result,
                        "message", "Add searches request rejected."))
                .build());
    }
}
