package com.rawneeded.dto.specialoffer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSpecialOfferRequestDto {
    @NotNull(message = "Product ID is required")
    private String productId;
    
    @NotNull(message = "Discount percentage is required")
    @Positive(message = "Discount percentage must be positive")
    private Double discountPercentage;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
}
