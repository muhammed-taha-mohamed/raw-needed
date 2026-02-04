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
public class AdSettingsResponseDto {
    private BigDecimal featuredPrice;
}
