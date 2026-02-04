package com.rawneeded.dto.advertisement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAdPackageRequestDto {
    private String nameAr;
    private String nameEn;
    @NotNull
    @Min(1)
    private Integer numberOfDays;
    /** Price per ad (EGP) */
    @NotNull
    @Min(0)
    private BigDecimal pricePerAd;
    private Boolean active;
    private Integer sortOrder;
}
