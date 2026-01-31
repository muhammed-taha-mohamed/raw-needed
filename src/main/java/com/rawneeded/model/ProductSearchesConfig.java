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
    private Integer from; // عدد السيرشات الأدنى
    private Integer to; // عدد السيرشات الأعلى (null يعني unlimited)
    private Boolean unlimited; // true إذا كان unlimited
    private Double pricePerSearch; // سعر السيرش الواحد
}
