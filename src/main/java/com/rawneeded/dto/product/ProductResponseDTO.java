package com.rawneeded.dto.product;

import com.rawneeded.enummeration.Category;
import com.rawneeded.enummeration.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;

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
    private String contactPersonName;
    private String contactPersonPhoneNumber;
    private boolean inStock;
    private Category category;
    private SubCategory subCategory;
}
