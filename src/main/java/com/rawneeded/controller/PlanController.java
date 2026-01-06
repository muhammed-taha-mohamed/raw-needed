package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.subscription.CreatePlanRequestDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;
import com.rawneeded.dto.subscription.UpdatePlanRequestDto;
import com.rawneeded.service.IPlanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/plans")
@RestController
public class PlanController {

    private final IPlanService planService;

    @GetMapping
    @Operation(summary = "Get all subscription plans",
            description = "Retrieve all available subscription plans")
    public ResponseEntity<ResponsePayload> getAllPlans() {
        List<SubscriptionPlanResponseDto> plans = planService.getAllPlans();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", plans))
                .build());
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get subscription plan by ID",
            description = "Retrieve a specific subscription plan by its ID")
    public ResponseEntity<ResponsePayload> getPlanById(@PathVariable String planId) {
        SubscriptionPlanResponseDto plan = planService.getPlanById(planId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", plan))
                .build());
    }

    @PostMapping
    @Operation(summary = "Create a subscription plan",
            description = "Create a new subscription plan (SYSTEM_ADMIN only)")
    public ResponseEntity<ResponsePayload> createPlan(
            @Valid @RequestBody CreatePlanRequestDto requestDto) {
        SubscriptionPlanResponseDto plan = planService.createPlan(requestDto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", plan,
                        "message", "Subscription plan created successfully."))
                .build());
    }

    @PutMapping("/{planId}")
    @Operation(summary = "Update a subscription plan",
            description = "Update an existing subscription plan (SYSTEM_ADMIN only)")
    public ResponseEntity<ResponsePayload> updatePlan(
            @PathVariable String planId,
            @Valid @RequestBody UpdatePlanRequestDto requestDto) {
        SubscriptionPlanResponseDto plan = planService.updatePlan(planId, requestDto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", plan,
                        "message", "Subscription plan updated successfully."))
                .build());
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "Delete a subscription plan",
            description = "Delete a subscription plan (SYSTEM_ADMIN only). Free trial plan cannot be deleted.")
    public ResponseEntity<ResponsePayload> deletePlan(@PathVariable String planId) {
        planService.deletePlan(planId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Subscription plan deleted successfully."))
                .build());
    }

    @PutMapping("/{planId}/activate")
    @Operation(summary = "Activate a subscription plan",
            description = "Activate a subscription plan (SYSTEM_ADMIN only)")
    public ResponseEntity<ResponsePayload> activatePlan(@PathVariable String planId) {
        SubscriptionPlanResponseDto plan = planService.activatePlan(planId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", plan,
                        "message", "Subscription plan activated successfully."))
                .build());
    }

    @PutMapping("/{planId}/deactivate")
    @Operation(summary = "Deactivate a subscription plan",
            description = "Deactivate a subscription plan (SYSTEM_ADMIN only). Free trial plan cannot be deactivated.")
    public ResponseEntity<ResponsePayload> deactivatePlan(@PathVariable String planId) {
        SubscriptionPlanResponseDto plan = planService.deactivatePlan(planId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", plan,
                        "message", "Subscription plan deactivated successfully."))
                .build());
    }
}
