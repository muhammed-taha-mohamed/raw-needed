package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.payment.CreatePaymentInfoRequestDto;
import com.rawneeded.dto.payment.PaymentInfoResponseDto;
import com.rawneeded.dto.payment.UpdatePaymentInfoRequestDto;
import com.rawneeded.enumeration.PaymentType;
import com.rawneeded.service.IPaymentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/admin/payment-info")
@RestController
public class PaymentInfoController {

    private final IPaymentInfoService paymentInfoService;

    @PostMapping
    @Operation(
            summary = "Create payment information",
            description = "Create a new payment information entry (SYSTEM_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> createPaymentInfo(
            @Valid @RequestBody CreatePaymentInfoRequestDto requestDto) {
        PaymentInfoResponseDto paymentInfo = paymentInfoService.createPaymentInfo(requestDto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", paymentInfo,
                        "message", "Payment information created successfully."))
                .build());
    }

    @PutMapping("/{paymentInfoId}")
    @Operation(
            summary = "Update payment information",
            description = "Update an existing payment information entry (SYSTEM_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> updatePaymentInfo(
            @PathVariable String paymentInfoId,
            @Valid @RequestBody UpdatePaymentInfoRequestDto requestDto) {
        PaymentInfoResponseDto paymentInfo = paymentInfoService.updatePaymentInfo(paymentInfoId, requestDto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", paymentInfo,
                        "message", "Payment information updated successfully."))
                .build());
    }

    @GetMapping("/{paymentInfoId}")
    @Operation(
            summary = "Get payment information by ID",
            description = "Retrieve a specific payment information entry by its ID"
    )
    public ResponseEntity<ResponsePayload> getPaymentInfoById(@PathVariable String paymentInfoId) {
        PaymentInfoResponseDto paymentInfo = paymentInfoService.getPaymentInfoById(paymentInfoId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", paymentInfo))
                .build());
    }

    @GetMapping
    @Operation(
            summary = "Get all payment information",
            description = "Retrieve all payment information entries"
    )
    public ResponseEntity<ResponsePayload> getAllPaymentInfos() {
        List<PaymentInfoResponseDto> paymentInfos = paymentInfoService.getAllPaymentInfos();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", paymentInfos))
                .build());
    }

    @GetMapping("/type/{paymentType}")
    @Operation(
            summary = "Get payment information by type",
            description = "Retrieve payment information entries filtered by type (BANK_ACCOUNT or ELECTRONIC_WALLET)"
    )
    public ResponseEntity<ResponsePayload> getPaymentInfosByType(@PathVariable PaymentType paymentType) {
        List<PaymentInfoResponseDto> paymentInfos = paymentInfoService.getPaymentInfosByType(paymentType);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", paymentInfos))
                .build());
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get active payment information",
            description = "Retrieve all active payment information entries"
    )
    public ResponseEntity<ResponsePayload> getActivePaymentInfos() {
        List<PaymentInfoResponseDto> paymentInfos = paymentInfoService.getActivePaymentInfos();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", paymentInfos))
                .build());
    }

    @DeleteMapping("/{paymentInfoId}")
    @Operation(
            summary = "Delete payment information",
            description = "Delete a payment information entry (SYSTEM_ADMIN only)"
    )
    public ResponseEntity<ResponsePayload> deletePaymentInfo(@PathVariable String paymentInfoId) {
        paymentInfoService.deletePaymentInfo(paymentInfoId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Payment information deleted successfully."))
                .build());
    }
}
