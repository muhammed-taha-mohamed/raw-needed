package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.product.CartDTO;
import com.rawneeded.service.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/cart")
public class CartController {

    private final ICartService cartService;

    @PostMapping("/{userId}")
    @Operation(summary = "Create a new cart for a user.",
            description = "This API is used to create a new cart for the user.")
    public ResponseEntity<ResponsePayload> createCart(@PathVariable String userId) {
        cartService.create(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Cart created successfully"))
                .build());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get the user's cart.",
            description = "This API is used to fetch the current cart of a user.")
    public ResponseEntity<ResponsePayload> getUserCart(@PathVariable String userId) {
        CartDTO cart = cartService.getUserCart(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", cart))
                .build());
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Clear the user's cart.",
            description = "This API is used to clear the user's cart.")
    public ResponseEntity<ResponsePayload> clearCart(@PathVariable String userId) {
        CartDTO clearedCart = cartService.clearCart(userId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", clearedCart))
                .build());
    }

    @PostMapping("/add-item")
    @Operation(summary = "Add item to the user's cart.",
            description = "This API is used to add a product to the user's cart.")
    public ResponseEntity<ResponsePayload> addItemToCart(@RequestParam String userId, @RequestParam String productId,@RequestParam float quantity) {
        CartDTO updatedCart = cartService.addItemToCart(userId, productId,quantity);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", updatedCart))
                .build());
    }
}
