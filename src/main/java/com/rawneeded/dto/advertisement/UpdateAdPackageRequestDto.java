package com.rawneeded.dto.advertisement;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAdPackageRequestDto {
    private String nameAr;
    private String nameEn;
    @Min(1)
    private Integer numberOfDays;
    @Min(0)
    private BigDecimal pricePerAd;
    private Boolean active;
    private Integer sortOrder;
}
