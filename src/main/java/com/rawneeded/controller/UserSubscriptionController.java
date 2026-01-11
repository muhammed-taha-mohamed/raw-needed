package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
import com.rawneeded.dto.subscription.CreateSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.service.IUserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/user-subscriptions")
@RestController
public class UserSubscriptionController {

    private final IUserSubscriptionService userSubscriptionService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/calculate-price")
    @Operation(summary = "Calculate subscription price",
            description = "Calculate the subscription price and available offers based on plan and number of users")
    public ResponseEntity<ResponsePayload> calculatePrice(@Valid @RequestBody CalculatePriceRequestDto requestDto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userSubscriptionService.calculatePrice(requestDto)))
                .build());
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit a user subscription",
            description = "Submit a user subscription for subscription activation")
    public ResponseEntity<ResponsePayload> submitUserSubscription(
            @RequestBody UserSubscriptionRequestDto requestDto,
            HttpServletRequest request) {

        UserSubscriptionResponseDto userSubscription = userSubscriptionService.submitUserSubscription(requestDto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userSubscription))
                .build());
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user subscription",
            description = "Delete a user subscription by its ID")
    public ResponseEntity<ResponsePayload> deleteUserSubscription(@PathVariable String id) {
        userSubscriptionService.deleteUserSubscription(id);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "User subscription deleted successfully."))
                .build());
    }


    @GetMapping("/my-subscription")
    @Operation(summary = "Get my active subscription",
            description = "Retrieve the active subscription for the current user")
    public ResponseEntity<ResponsePayload> getMySubscription() {
        UserSubscriptionResponseDto subscription = userSubscriptionService.getUserSubscription();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", subscription))
                .build());
    }


}
