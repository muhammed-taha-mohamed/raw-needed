package com.rawneeded.dto.product;

import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductResponseDTO {
    private String id;
    private String name;
    private String englishName;
    private String arabicName;
    private String origin;
    private String supplierId;
    private String supplierName;
    private boolean inStock;
    private Integer stockQuantity;
    private CategoryResponseDto category;
    private SubCategoryResponseDto subCategory;
    private String image;
    private String unit;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private Map<String, String> extraFieldValues;
}
