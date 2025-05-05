package com.rawneeded.dto.product;

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
}
