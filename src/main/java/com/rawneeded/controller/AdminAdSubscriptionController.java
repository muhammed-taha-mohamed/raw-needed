package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.advertisement.AdSubscriptionResponseDto;
import com.rawneeded.service.IAdSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/admin/ad-subscriptions")
@RestController
public class AdminAdSubscriptionController {

    private final IAdSubscriptionService adSubscriptionService;

    @GetMapping("/pending")
    @Operation(summary = "Get pending ad subscriptions", description = "Admin: list pending supplier ad subscription requests")
    public ResponseEntity<ResponsePayload> getPending(Pageable pageable) {
        Page<AdSubscriptionResponseDto> page = adSubscriptionService.getPendingSubscriptions(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", page))
                .build());
    }

    @GetMapping("/approved")
    @Operation(summary = "Get approved ad subscriptions", description = "Admin: list approved (active) supplier ad subscriptions")
    public ResponseEntity<ResponsePayload> getApproved(Pageable pageable) {
        Page<AdSubscriptionResponseDto> page = adSubscriptionService.getApprovedSubscriptions(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", page))
                .build());
    }

    @PostMapping("/{subscriptionId}/approve")
    @Operation(summary = "Approve ad subscription", description = "Admin: approve supplier ad subscription after payment verification")
    public ResponseEntity<ResponsePayload> approve(@PathVariable String subscriptionId) {
        AdSubscriptionResponseDto dto = adSubscriptionService.approve(subscriptionId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", dto,
                        "message", "Ad subscription approved. Supplier can now add advertisements."))
                .build());
    }

    @PostMapping("/{subscriptionId}/reject")
    @Operation(summary = "Reject ad subscription", description = "Admin: reject supplier ad subscription request")
    public ResponseEntity<ResponsePayload> reject(@PathVariable String subscriptionId) {
        AdSubscriptionResponseDto dto = adSubscriptionService.reject(subscriptionId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", dto,
                        "message", "Ad subscription rejected."))
                .build());
    }
}
