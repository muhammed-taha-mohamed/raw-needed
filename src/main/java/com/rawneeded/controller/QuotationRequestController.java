package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.service.ICartService;
import com.rawneeded.service.IQuotationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/quotation")
public class QuotationRequestController {

    private final IQuotationService quotationService;

    @PostMapping("/send")
    @Operation(summary = "Send quotation request.",
            description = "This API is used to send a quotation request.")
    public ResponseEntity<ResponsePayload> sendQuotationRequest(@RequestParam String userId) {
        quotationService.sendQuotationRequests(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Quotation request sent successfully"))
                .build());
    }
}
