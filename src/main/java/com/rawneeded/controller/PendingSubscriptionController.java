package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
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
@RequestMapping("api/v1/admin/user-subscriptions")
@RestController
public class PendingSubscriptionController {

    private final IUserSubscriptionService userSubscriptionAdminService;

    @GetMapping("/pending")
    @Operation(summary = "Get all pending user subscriptions",
            description = "Retrieve all pending user subscriptions (SYSTEM_ADMIN only)")
    public ResponseEntity<ResponsePayload> getAllPendingUserSubscriptions(Pageable pageable) {
        Page<UserSubscriptionResponseDto> userSubscriptions = userSubscriptionAdminService.getAllPendingUserSubscriptions(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userSubscriptions))
                .build());
    }

    @PostMapping("/{userSubscriptionId}/approve")
    @Operation(summary = "Approve a user subscription",
            description = "Approve a user subscription and activate the user's account (SYSTEM_ADMIN only)")
    public ResponseEntity<ResponsePayload> approveUserSubscription(@PathVariable String userSubscriptionId) {
        UserSubscriptionResponseDto userSubscription = userSubscriptionAdminService.approveUserSubscription(userSubscriptionId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userSubscription,
                        "message", "User subscription approved successfully. User account activated."))
                .build());
    }

    @PostMapping("/{userSubscriptionId}/reject")
    @Operation(summary = "Reject a user subscription",
            description = "Reject a user subscription (SYSTEM_ADMIN only)")
    public ResponseEntity<ResponsePayload> rejectUserSubscription(
            @PathVariable String userSubscriptionId,
            @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "No reason provided");
        UserSubscriptionResponseDto userSubscription = userSubscriptionAdminService.rejectUserSubscription(userSubscriptionId, reason);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userSubscription,
                        "message", "User subscription rejected successfully."))
                .build());
    }
}
