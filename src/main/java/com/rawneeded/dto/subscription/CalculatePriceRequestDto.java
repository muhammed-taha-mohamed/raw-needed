package com.rawneeded.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalculatePriceRequestDto {
    private String planId;
    private int numberOfUsers;
}
