package com.rawneeded.dto.advertisement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdPackageResponseDto {
    private String id;
    private String nameAr;
    private String nameEn;
    private int numberOfDays;
    private BigDecimal pricePerAd;
    private BigDecimal featuredPrice;
    private boolean active;
    private int sortOrder;
}
