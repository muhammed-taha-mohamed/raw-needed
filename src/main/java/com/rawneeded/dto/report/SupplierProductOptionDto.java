package com.rawneeded.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProductOptionDto {
    private String id;
    private String name;
    private String image;
    private String unit;
}
