package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.service.ISupplierReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/reports/supplier")
public class SupplierReportController {

    private final ISupplierReportService supplierReportService;

    @GetMapping("/products")
    @Operation(summary = "Get supplier products for reports")
    public ResponseEntity<ResponsePayload> getSupplierProductsForReports() {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", supplierReportService.getSupplierProducts()
                ))
                .build());
    }

    @GetMapping("/product-card")
    @Operation(summary = "Get product card report")
    public ResponseEntity<ResponsePayload> getProductCardReport(@RequestParam String productId) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", supplierReportService.getProductCardReport(productId)
                ))
                .build());
    }

    @GetMapping("/sales-report")
    @Operation(summary = "Get supplier sales report")
    public ResponseEntity<ResponsePayload> getSalesReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", supplierReportService.getSalesReport(month, year)
                ))
                .build());
    }

    @GetMapping("/insights")
    @Operation(summary = "Get supplier insights report")
    public ResponseEntity<ResponsePayload> getInsightsReport() {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", supplierReportService.getInsightsReport()
                ))
                .build());
    }
}
