package com.rawneeded.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CartItemDTO {
    private String id;
    private String name;
    private String origin;
    private String unit;
    private String supplierId;
    private String supplierName;
    private boolean inStock;
    private float quantity;
    private String image;
    private String categoryId;
    private String subCategoryId;
    private Map<String, String> extraFieldValues;
    private String specialOfferId; // ID of special offer if this item is from a special offer
}
