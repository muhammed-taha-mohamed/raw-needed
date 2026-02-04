package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.specialoffer.CreateSpecialOfferRequestDto;
import com.rawneeded.dto.specialoffer.SpecialOfferResponseDto;
import com.rawneeded.service.ISpecialOfferService;
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
@RequestMapping("api/v1/special-offers")
public class SpecialOfferController {

    private final ISpecialOfferService specialOfferService;

    @PostMapping
    @Operation(
            summary = "Create a special offer",
            description = "Supplier creates a special offer on a product (requires SUPPLIER_SPECIAL_OFFERS feature)"
    )
    public ResponseEntity<ResponsePayload> createOffer(@Valid @RequestBody CreateSpecialOfferRequestDto request) {
        SpecialOfferResponseDto offer = specialOfferService.createOffer(request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", offer,
                        "message", "Special offer created successfully"))
                .build());
    }

    @GetMapping("/my-offers")
    @Operation(
            summary = "Get my special offers",
            description = "Get all special offers created by the current supplier"
    )
    public ResponseEntity<ResponsePayload> getMyOffers(Pageable pageable) {
        Page<SpecialOfferResponseDto> offers = specialOfferService.getMyOffers(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", offers))
                .build());
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get all active special offers",
            description = "Get all active special offers (for customers, requires CUSTOMER_VIEW_SUPPLIER_OFFERS feature)"
    )
    public ResponseEntity<ResponsePayload> getAllActiveOffers(Pageable pageable) {
        Page<SpecialOfferResponseDto> offers = specialOfferService.getAllActiveOffers(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", offers))
                .build());
    }

    @PutMapping("/{offerId}")
    @Operation(
            summary = "Update a special offer",
            description = "Update an existing special offer"
    )
    public ResponseEntity<ResponsePayload> updateOffer(
            @PathVariable String offerId,
            @Valid @RequestBody CreateSpecialOfferRequestDto request) {
        SpecialOfferResponseDto offer = specialOfferService.updateOffer(offerId, request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", offer,
                        "message", "Special offer updated successfully"))
                .build());
    }

    @DeleteMapping("/{offerId}")
    @Operation(
            summary = "Delete a special offer",
            description = "Delete (deactivate) a special offer"
    )
    public ResponseEntity<ResponsePayload> deleteOffer(@PathVariable String offerId) {
        specialOfferService.deleteOffer(offerId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Special offer deleted successfully"))
                .build());
    }
}
