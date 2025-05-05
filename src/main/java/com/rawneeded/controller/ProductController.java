package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.product.ProductFilterDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/product")
public class ProductController {

    private final IProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product.",
            description = "This API is used to create a new product.")
    public ResponseEntity<ResponsePayload> createProduct(@RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", productService.create(dto)))
                .build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a product by ID.",
            description = "This API is used to update an existing product.")
    public ResponseEntity<ResponsePayload> updateProduct(@PathVariable String id, @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", productService.update(id, dto)))
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product by ID.",
            description = "This API is used to delete a product by its ID.")
    public ResponseEntity<ResponsePayload> deleteProduct(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "Product deleted successfully"))
                .build());
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter products.",
            description = "This API is used to filter products based on given criteria.")
    public ResponseEntity<ResponsePayload> filterProducts(@RequestBody ProductFilterDTO filterDTO, Pageable pageable) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", productService.filter(pageable, filterDTO)))
                .build());
    }
}
