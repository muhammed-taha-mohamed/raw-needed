package com.rawneeded.controller;

import com.rawneeded.dto.RFQ.CreateRFQRequest;
import com.rawneeded.dto.RFQ.RFQOrderLineResponseDto;
import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.dto.RFQ.SupplierResponseOnOrderDTO;
import com.rawneeded.service.IRFQService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("api/v1/rfq")
public class RFQController {

    private final IRFQService rfqService;

    // ===================== CLIENT =====================

    @PostMapping
    @Operation(
            summary = "BY Customer : Create new RFQ",
            description = "Submit cart items to create a new RFQ order"
    )
    public ResponseEntity<ResponsePayload> createRFQ(
            @RequestBody CreateRFQRequest request) {

        return ResponseEntity.ok(
                ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "data", rfqService.createRFQ(request)
                        ))
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    @Operation(
            summary = "Get RFQ order details",
            description = "Fetch RFQ order with all its lines"
    )
    public ResponseEntity<ResponsePayload> getRFQOrder(
            @PathVariable String orderId) {

        return ResponseEntity.ok(
                ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "data", rfqService.getOrderById(orderId)
                        ))
                        .build()
        );
    }

    @GetMapping("/{orderId}/lines")
    @Operation(
            summary = "Get RFQ order lines",
            description = "Fetch all RFQ order lines for a specific order"
    )
    public ResponseEntity<ResponsePayload> getOrderLines(
            @PathVariable String orderId) {

        List<RFQOrderLineResponseDto> lines =
                rfqService.getOrderLines(orderId);

        return ResponseEntity.ok(
                ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "data", lines
                        ))
                        .build()
        );
    }

    @DeleteMapping("/{orderId}")
    @Operation(
            summary = "Cancel RFQ order",
            description = "Cancel RFQ order before completion"
    )
    public ResponseEntity<ResponsePayload> cancelRFQ(
            @PathVariable String orderId) {

        rfqService.cancelRFQ(orderId);

        return ResponseEntity.ok(
                ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "message", "RFQ order cancelled successfully"
                        ))
                        .build()
        );
    }


    @GetMapping("by-creator")
    @Operation(
            summary = "Get all RFQ orders",
            description = "Fetch all RFQ orders"
    )
        public ResponseEntity<ResponsePayload> getAllRFQOrders(Pageable pageable ,
                                                               @RequestParam(required = false)OrderStatus status) {

        return ResponseEntity.ok(
                ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "data", rfqService.getMyOrders(pageable,status)
                        ))
                        .build()
        );
    }

    // ===================== SUPPLIER =====================

    @GetMapping("/supplier/offers")
    @Operation(
            summary = "Get supplier pending RFQ lines",
            description = "Fetch all pending RFQ lines for a supplier"
    )
    public ResponseEntity<ResponsePayload> getSupplierLines(Pageable pageable , @RequestParam LineStatus status) {

        Page<RFQOrderLineResponseDto> lines =
                rfqService.getSupplierLines(pageable,status);

        return ResponseEntity.ok(
                ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "data", lines
                        ))
                        .build()
        );
    }

    @PostMapping("/line/{lineId}/respond")
    @Operation(
            summary = "Supplier respond to RFQ line",
            description = "Supplier submits price and delivery details for an RFQ line"
    )
    public ResponseEntity<ResponsePayload> respondToRFQLine(
            @PathVariable String lineId,
            @RequestBody SupplierResponseOnOrderDTO response) {

        rfqService.respondToOrderLine(lineId, response);

        return ResponseEntity.ok(
                ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "message", "Supplier response submitted successfully"
                        ))
                        .build()
        );
    }
}
