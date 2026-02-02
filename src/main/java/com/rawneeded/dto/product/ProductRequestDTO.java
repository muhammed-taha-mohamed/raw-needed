package com.rawneeded.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductRequestDTO {
    private String name;
    private String origin;
    private String supplierId;
    private boolean inStock;
    private Integer stockQuantity;
    private String categoryId;
    private String subCategoryId;
    private String image;
    private String unit;
    private LocalDate productionDate;
    private LocalDate expirationDate;
}
