package com.rawneeded.dto.product;

import com.rawneeded.enummeration.Category;
import com.rawneeded.enummeration.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductFilterDTO {
    private String name;
    private String origin;
    private String supplierId;
    private Category category;
    private SubCategory subCategory;
}
