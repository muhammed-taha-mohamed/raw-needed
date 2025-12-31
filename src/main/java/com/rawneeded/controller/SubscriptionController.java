package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.subscription.QuotationRequestDto;
import com.rawneeded.dto.subscription.QuotationResponseDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.service.ISubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/subscriptions")
@RestController
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/plans")
    @Operation(summary = "Get all subscription plans",
            description = "Retrieve all available subscription plans")
    public ResponseEntity<ResponsePayload> getAllPlans() {
        List<SubscriptionPlanResponseDto> plans = subscriptionService.getAllPlans();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", plans))
                .build());
    }

    @GetMapping("/plans/{planId}")
    @Operation(summary = "Get subscription plan by ID",
            description = "Retrieve a specific subscription plan by its ID")
    public ResponseEntity<ResponsePayload> getPlanById(@PathVariable String planId) {
        SubscriptionPlanResponseDto plan = subscriptionService.getPlanById(planId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", plan))
                .build());
    }

    @PostMapping("/quotations")
    @Operation(summary = "Submit a quotation",
            description = "Submit a payment quotation for subscription activation")
    public ResponseEntity<ResponsePayload> submitQuotation(
            @ModelAttribute QuotationRequestDto requestDto,
            HttpServletRequest request) {
        String userId = jwtTokenProvider.getIdFromToken(
                extractToken(request.getHeader("Authorization")));
        QuotationResponseDto quotation = subscriptionService.submitQuotation(userId, requestDto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", quotation))
                .build());
    }

    @GetMapping("/quotations/my")
    @Operation(summary = "Get my quotation",
            description = "Retrieve the quotation submitted by the current user")
    public ResponseEntity<ResponsePayload> getMyQuotation(HttpServletRequest request) {
        String userId = jwtTokenProvider.getIdFromToken(
                extractToken(request.getHeader("Authorization")));
        QuotationResponseDto quotation = subscriptionService.getQuotationByOwnerId(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", quotation))
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
