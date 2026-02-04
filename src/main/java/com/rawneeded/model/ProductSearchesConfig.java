package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductSearchesConfig {
    private Integer from; // min search count
    private Integer to; // max search count (null means unlimited)
    private Boolean unlimited; // true if unlimited
    private Double pricePerSearch; // price per search
}
