package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.subscription.AddSearchesRequestDto;
import com.rawneeded.dto.subscription.AddSearchesSubmitDto;
import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
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

    @PostMapping("/add-searches")
    @Operation(summary = "Request to add more searches",
            description = "Submit a request to add more product searches to current subscription (partial renewal). Payment receipt is optional.")
    public ResponseEntity<ResponsePayload> submitAddSearches(@Valid @RequestBody AddSearchesSubmitDto dto) {
        AddSearchesRequestDto result = userSubscriptionService.submitAddSearchesRequest(dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", result))
                .build());
    }

    @GetMapping("/add-searches/price")
    @Operation(summary = "Calculate price for adding searches")
    public ResponseEntity<ResponsePayload> calculateAddSearchesPrice(@RequestParam int numberOfSearches) {
        double price = userSubscriptionService.calculateAddSearchesPrice(numberOfSearches);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", Map.of("totalPrice", price)))
                .build());
    }

}
