package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.advertisement.AdPackageResponseDto;
import com.rawneeded.dto.advertisement.AdSettingsResponseDto;
import com.rawneeded.dto.advertisement.CreateAdPackageRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdPackageRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdSettingsRequestDto;
import com.rawneeded.service.IAdPackageService;
import com.rawneeded.service.IAdSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin: manage ad packages (days + price per ad) and featured price.
 */
@RestController
@RequestMapping("api/v1/admin/ad-packages")
@AllArgsConstructor
public class AdminAdPackageController {

    private final IAdPackageService adPackageService;
    private final IAdSettingsService adSettingsService;

    @GetMapping
    @Operation(summary = "List all ad packages (Admin)")
    public ResponseEntity<ResponsePayload> getAllPackages() {
        List<AdPackageResponseDto> list = adPackageService.getAllPackages();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", list))
                .build());
    }

    @PostMapping
    @Operation(summary = "Create ad package (Admin)")
    public ResponseEntity<ResponsePayload> createPackage(@Valid @RequestBody CreateAdPackageRequestDto dto) {
        AdPackageResponseDto created = adPackageService.createPackage(dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", created, "message", "Ad package created"))
                .build());
    }

    @PutMapping("/{packageId}")
    @Operation(summary = "Update ad package (Admin)")
    public ResponseEntity<ResponsePayload> updatePackage(
            @PathVariable String packageId,
            @Valid @RequestBody UpdateAdPackageRequestDto dto) {
        AdPackageResponseDto updated = adPackageService.updatePackage(packageId, dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", updated, "message", "Ad package updated"))
                .build());
    }

    @DeleteMapping("/{packageId}")
    @Operation(summary = "Delete ad package (Admin)")
    public ResponseEntity<ResponsePayload> deletePackage(@PathVariable String packageId) {
        adPackageService.deletePackage(packageId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "message", "Ad package deleted"))
                .build());
    }

    @GetMapping("/settings")
    @Operation(summary = "Get ad settings (featured price) (Admin)")
    public ResponseEntity<ResponsePayload> getSettings() {
        AdSettingsResponseDto settings = adSettingsService.getSettings();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", settings))
                .build());
    }

    @PutMapping("/settings")
    @Operation(summary = "Update ad settings - featured price (Admin)")
    public ResponseEntity<ResponsePayload> updateSettings(@Valid @RequestBody UpdateAdSettingsRequestDto dto) {
        AdSettingsResponseDto settings = adSettingsService.updateSettings(dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "data", settings, "message", "Ad settings updated"))
                .build());
    }
}
