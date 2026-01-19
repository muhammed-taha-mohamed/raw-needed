package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.advertisement.AdvertisementResponseDto;
import com.rawneeded.dto.advertisement.CreateAdvertisementRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdvertisementRequestDto;
import com.rawneeded.service.IAdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/advertisements")
public class AdvertisementController {

    private final IAdvertisementService advertisementService;

    @PostMapping
    @Operation(
            summary = "Create a new advertisement",
            description = "User creates an advertisement (requires subscription with advertisements enabled)"
    )
    public ResponseEntity<ResponsePayload> createAdvertisement(
            @Valid @RequestBody CreateAdvertisementRequestDto request) {
        AdvertisementResponseDto advertisement = advertisementService.createAdvertisement(request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", advertisement,
                        "message", "Advertisement created successfully"
                ))
                .build());
    }

    @PutMapping("/{advertisementId}")
    @Operation(
            summary = "Update an advertisement",
            description = "User updates their own advertisement"
    )
    public ResponseEntity<ResponsePayload> updateAdvertisement(
            @PathVariable String advertisementId,
            @Valid @RequestBody UpdateAdvertisementRequestDto request) {
        AdvertisementResponseDto advertisement = advertisementService.updateAdvertisement(advertisementId, request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", advertisement,
                        "message", "Advertisement updated successfully"
                ))
                .build());
    }

    @DeleteMapping("/{advertisementId}")
    @Operation(
            summary = "Delete an advertisement",
            description = "User deletes their own advertisement"
    )
    public ResponseEntity<ResponsePayload> deleteAdvertisement(
            @PathVariable String advertisementId) {
        advertisementService.deleteAdvertisement(advertisementId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Advertisement deleted successfully"
                ))
                .build());
    }

    @GetMapping("/my-advertisements")
    @Operation(
            summary = "Get my advertisements",
            description = "Get all advertisements created by the current user"
    )
    public ResponseEntity<ResponsePayload> getMyAdvertisements() {
        List<AdvertisementResponseDto> advertisements = advertisementService.getMyAdvertisements();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", advertisements
                ))
                .build());
    }

    @GetMapping
    @Operation(
            summary = "Get all advertisements",
            description = "Get all active advertisements (paginated)"
    )
    public ResponseEntity<ResponsePayload> getAllAdvertisements(Pageable pageable) {
        Page<AdvertisementResponseDto> advertisements = advertisementService.getAllAdvertisements(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", advertisements
                ))
                .build());
    }

    @GetMapping("/{advertisementId}")
    @Operation(
            summary = "Get advertisement by ID",
            description = "Get a specific advertisement by its ID"
    )
    public ResponseEntity<ResponsePayload> getAdvertisementById(
            @PathVariable String advertisementId) {
        AdvertisementResponseDto advertisement = advertisementService.getAdvertisementById(advertisementId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", advertisement
                ))
                .build());
    }
}
