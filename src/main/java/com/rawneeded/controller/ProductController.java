package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.product.ProductFilterDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/export-stock")
    @Operation(summary = "Export stock to Excel.",
            description = "This API is used to export supplier's stock to an Excel file.")
    public ResponseEntity<Resource> exportStock() {
        Resource resource = productService.exportStock();
        String filename = "stock-report-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/download-template")
    @Operation(summary = "Download products template.",
            description = "Supplier downloads Excel template with Categories Reference sheet. Fill products and upload via upload-products.")
    public ResponseEntity<Resource> downloadTemplate() {
        Resource resource = productService.downloadTemplate();
        String filename = "products-template.xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @PostMapping(value = "/upload-products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload products from Excel.",
            description = "Supplier uploads filled template Excel. Products are added to stock.")
    public ResponseEntity<ResponsePayload> uploadProducts(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", productService.uploadProducts(file)))
                .build());
    }
}
