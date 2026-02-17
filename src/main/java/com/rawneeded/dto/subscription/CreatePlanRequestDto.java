package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.BillingFrequency;
import com.rawneeded.enumeration.PlanType;
import com.rawneeded.model.PlanFeature;
import com.rawneeded.model.ProductSearchesConfig;
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
    @jakarta.validation.constraints.PositiveOrZero(message = "Price per user must be zero or positive")
    private Double pricePerUser;
    
    private String description;
    
    @NotNull(message = "Billing frequency is required")
    private BillingFrequency billingFrequency;
    
    private List<SpecialOffer> specialOffers;
    
    @NotNull(message = "Plan type is required")
    private PlanType planType;
    
    // Features list with prices
    private List<PlanFeature> features;
    
    // For Customer plans: Product searches configuration
    private ProductSearchesConfig productSearchesConfig;

    private Boolean free;
    private Boolean exclusive;
}
