package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.BillingFrequency;
import com.rawneeded.model.SpecialOffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreatePlanRequestDto {
    @NotBlank(message = "Plan name is required")
    private String name;
    
    @NotNull(message = "Price per user is required")
    @Positive(message = "Price per user must be positive")
    private Double pricePerUser;
    
    private String description;
    
    @NotNull(message = "Billing frequency is required")
    private BillingFrequency billingFrequency;
    
    private List<SpecialOffer> specialOffers;
    

    private Boolean freeTrial;
}
