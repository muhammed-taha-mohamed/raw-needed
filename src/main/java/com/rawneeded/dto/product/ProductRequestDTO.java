package com.rawneeded.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductRequestDTO {
    private String name;
    private String origin;
    private String supplierId;
    private String contactPersonName;
    private String contactPersonPhoneNumber;
    private boolean inStock;
}
