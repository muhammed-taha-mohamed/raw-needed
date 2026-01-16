package com.rawneeded.dto.product;

import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductResponseDTO {
    private String id;
    private String name;
    private String origin;
    private String supplierId;
    private String supplierName;
    private boolean inStock;
    private Integer stockQuantity;
    private CategoryResponseDto category;
    private SubCategoryResponseDto subCategory;
    private String image;
}
