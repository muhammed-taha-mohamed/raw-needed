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
public class UpdateAdSettingsRequestDto {
    @Min(0)
    private BigDecimal featuredPrice;
}
