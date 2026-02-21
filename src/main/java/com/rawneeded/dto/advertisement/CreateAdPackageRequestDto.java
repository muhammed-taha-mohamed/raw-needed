package com.rawneeded.dto.advertisement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.rawneeded.model.AdSpecialOffer;

import java.math.BigDecimal;
import java.util.List;

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
    /** Extra price for featuring ad at top (EGP) */
    @Min(0)
    private BigDecimal featuredPrice;
    /** Special offers based on number of ads (discounts) */
    private List<AdSpecialOffer> specialOffers;
    private Boolean active;
    private Integer sortOrder;
}
