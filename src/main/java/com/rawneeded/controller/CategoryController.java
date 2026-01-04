package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.category.CategoryRequestDto;
import com.rawneeded.dto.category.SubCategoryRequestDto;
import com.rawneeded.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/category")
public class CategoryController {

    private final ICategoryService categoryService;

    // ================= Category =================

    @PostMapping
    @Operation(summary = "Create a new category",
            description = "This API is used to create a new category")
    public ResponseEntity<ResponsePayload> createCategory(
            @RequestBody CategoryRequestDto dto) {

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", categoryService.createCategory(dto)))
                .build());
    }

    @PatchMapping
    @Operation(summary = "Update a category",
            description = "This API is used to update an existing category")
    public ResponseEntity<ResponsePayload> updateCategory(
            @RequestParam String categoryId,
            @RequestBody CategoryRequestDto dto) {

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", categoryService.updateCategory(categoryId, dto)))
                .build());
    }

    @DeleteMapping
    @Operation(summary = "Delete a category",
            description = "This API is used to delete a category")
    public ResponseEntity<ResponsePayload> deleteCategory(
            @RequestParam String categoryId) {

        categoryService.deleteCategory(categoryId);

        return ResponseEntity.ok(ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "message", "Category deleted successfully"))
                .build());
    }

    @GetMapping
    @Operation(summary = "Get category by ID",
            description = "This API is used to get a category by its ID")
    public ResponseEntity<ResponsePayload> getCategoryById(
            @RequestParam String categoryId) {

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", categoryService.getCategoryById(categoryId)))
                .build());
    }

    @GetMapping("/all")
    @Operation(summary = "Get all categories",
            description = "This API is used to get all categories")
    public ResponseEntity<ResponsePayload> getAllCategories() {

        return ResponseEntity.ok(ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "data", categoryService.getAllCategories()))
                .build());
    }

    // ================= SubCategory =================

    @PostMapping("/sub-category")
    @Operation(summary = "Create a sub-category",
            description = "This API is used to create a sub-category under a category")
    public ResponseEntity<ResponsePayload> createSubCategory(@RequestBody SubCategoryRequestDto dto) {

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", categoryService.createSubCategory(dto)))
                .build());
    }

    @PatchMapping("/sub-category")
    @Operation(summary = "Update a sub-category",
            description = "This API is used to update an existing sub-category")
    public ResponseEntity<ResponsePayload> updateSubCategory(
            @RequestParam String subCategoryId,
            @RequestBody SubCategoryRequestDto dto) {

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", categoryService.updateSubCategory(subCategoryId, dto)))
                .build());
    }

    @DeleteMapping("/sub-category")
    @Operation(summary = "Delete a sub-category",
            description = "This API is used to delete a sub-category")
    public ResponseEntity<ResponsePayload> deleteSubCategory(
            @RequestParam String subCategoryId) {

        categoryService.deleteSubCategory(subCategoryId);

        return ResponseEntity.ok(ResponsePayload.builder()
                        .date(LocalDateTime.now())
                        .content(Map.of(
                                "success", true,
                                "message", "SubCategory deleted successfully"))
                .build());
    }

    @GetMapping("/sub-category")
    @Operation(summary = "Get sub-categories by category",
            description = "This API is used to get sub-categories under a category")
    public ResponseEntity<ResponsePayload> getSubCategoriesByCategory(
            @RequestParam String categoryId) {

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", categoryService.getSubCategoriesByCategoryId(categoryId)))
                .build());
    }
}
