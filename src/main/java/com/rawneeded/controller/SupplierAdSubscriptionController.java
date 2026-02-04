package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.advertisement.AdSubscriptionResponseDto;
import com.rawneeded.dto.advertisement.CreateAdSubscriptionRequestDto;
import com.rawneeded.service.IAdSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/supplier/ad-subscriptions")
@RestController
public class SupplierAdSubscriptionController {

    private final IAdSubscriptionService adSubscriptionService;

    @GetMapping
    @Operation(summary = "Get my ad subscriptions", description = "Supplier: list all my ad subscription requests")
    public ResponseEntity<ResponsePayload> getMySubscriptions() {
        List<AdSubscriptionResponseDto> list = adSubscriptionService.getMySubscriptions();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", list))
                .build());
    }

    @PostMapping
    @Operation(summary = "Subscribe to ad package", description = "Supplier: request subscription to an ad package (optional payment proof)")
    public ResponseEntity<ResponsePayload> createSubscription(
            @Valid @RequestBody CreateAdSubscriptionRequestDto dto) {
        AdSubscriptionResponseDto created = adSubscriptionService.createSubscription(dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", created,
                        "message", "Ad subscription request submitted. Wait for admin approval."))
                .build());
    }
}
