package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.order.OrderMessageRequestDto;
import com.rawneeded.dto.order.OrderMessageResponseDto;
import com.rawneeded.service.IOrderChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/orders")
public class OrderChatController {

    private final IOrderChatService orderChatService;

    @PostMapping("/{orderId}/messages")
    @Operation(
            summary = "Add a message to an order",
            description = "Add a chat message to an order"
    )
    public ResponseEntity<ResponsePayload> addMessage(
            @PathVariable String orderId,
            @Valid @RequestBody OrderMessageRequestDto request) {
        OrderMessageResponseDto message = orderChatService.addMessage(orderId, request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", message,
                        "message", "Message added successfully"
                ))
                .build());
    }

    @GetMapping("/{orderId}/messages")
    @Operation(
            summary = "Get all messages for an order",
            description = "Get all chat messages for an order"
    )
    public ResponseEntity<ResponsePayload> getOrderMessages(
            @PathVariable String orderId) {
        List<OrderMessageResponseDto> messages = orderChatService.getOrderMessages(orderId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", messages
                ))
                .build());
    }
}
