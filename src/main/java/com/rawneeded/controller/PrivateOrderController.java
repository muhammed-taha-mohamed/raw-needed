package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.post.CreateOfferRequest;
import com.rawneeded.dto.private_order.CreatePrivateOrderRequest;
import com.rawneeded.dto.post.OfferResponseDto;
import com.rawneeded.dto.private_order.PrivateOrderResponseDto;
import com.rawneeded.dto.post.RespondToOfferRequest;
import com.rawneeded.service.IPrivateOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping({"api/v1/private-orders", "api/v1/clients-special-orders"})
public class PrivateOrderController {

    private final IPrivateOrderService privateOrderService;

    @PostMapping
    @Operation(summary = "Create a private order")
    public ResponseEntity<ResponsePayload> createPrivateOrder(@Valid @RequestBody CreatePrivateOrderRequest request) {
        PrivateOrderResponseDto privateOrder = privateOrderService.createPrivateOrder(request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", privateOrder, "message", "Private order created successfully"))
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all private orders (excluding current user's own)")
    public ResponseEntity<ResponsePayload> getAllPrivateOrders(Pageable pageable) {
        Page<PrivateOrderResponseDto> privateOrders = privateOrderService.getAllPrivateOrders(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", privateOrders))
                .build());
    }

    @GetMapping("/{privateOrderId}")
    @Operation(summary = "Get private order by ID")
    public ResponseEntity<ResponsePayload> getPrivateOrderById(@PathVariable String privateOrderId) {
        PrivateOrderResponseDto privateOrder = privateOrderService.getPrivateOrderById(privateOrderId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", privateOrder))
                .build());
    }

    @PostMapping("/{privateOrderId}/offers")
    @Operation(summary = "Create offer for private order")
    public ResponseEntity<ResponsePayload> createOffer(
            @PathVariable String privateOrderId,
            @Valid @RequestBody CreateOfferRequest request) {
        OfferResponseDto offer = privateOrderService.createOffer(privateOrderId, request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", offer, "message", "Offer created successfully"))
                .build());
    }

    @PostMapping("/offers/respond")
    @Operation(summary = "Respond to private order offer")
    public ResponseEntity<ResponsePayload> respondToOffer(
            @RequestParam String privateOrderId,
            @RequestParam String offerId,
            @Valid @RequestBody RespondToOfferRequest request) {
        OfferResponseDto offer = privateOrderService.respondToOffer(privateOrderId, offerId, request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", offer, "message", "Offer response submitted successfully"))
                .build());
    }

    @PutMapping("/{privateOrderId}/close")
    @Operation(summary = "Close private order")
    public ResponseEntity<ResponsePayload> closePrivateOrder(@PathVariable String privateOrderId) {
        PrivateOrderResponseDto privateOrder = privateOrderService.closePrivateOrder(privateOrderId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", privateOrder, "message", "Private order closed successfully"))
                .build());
    }

    @PutMapping("/{privateOrderId}/complete")
    @Operation(summary = "Complete private order")
    public ResponseEntity<ResponsePayload> completePrivateOrder(@PathVariable String privateOrderId) {
        PrivateOrderResponseDto privateOrder = privateOrderService.completePrivateOrder(privateOrderId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", privateOrder, "message", "Private order marked as completed successfully"))
                .build());
    }

    @GetMapping({"/my-private-orders", "/my-clients-special-orders"})
    @Operation(summary = "Get my private orders")
    public ResponseEntity<ResponsePayload> getMyPrivateOrders(Pageable pageable) {
        Page<PrivateOrderResponseDto> privateOrders = privateOrderService.getMyPrivateOrders(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", privateOrders))
                .build());
    }

    @GetMapping("/my-offers")
    @Operation(summary = "Get my offers")
    public ResponseEntity<ResponsePayload> getMyOffers(Pageable pageable) {
        Page<OfferResponseDto> offers = privateOrderService.getMyOffers(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", offers))
                .build());
    }
}
