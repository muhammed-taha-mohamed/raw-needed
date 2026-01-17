package com.rawneeded.dto.post;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateOfferRequest {
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Float price;
    
    @NotNull(message = "Available quantity is required")
    @Positive(message = "Available quantity must be positive")
    private Float availableQuantity;
    
    private String shippingInfo;
    
    private LocalDate estimatedDelivery;
    
    private String notes;
}
