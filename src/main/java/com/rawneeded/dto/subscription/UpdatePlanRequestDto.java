package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.BillingFrequency;
import com.rawneeded.enumeration.PlanType;
import com.rawneeded.model.SpecialOffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Positive;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdatePlanRequestDto {
    private String name;
    
    @Positive(message = "Price per user must be positive")
    private Double pricePerUser;
    
    private String description;
    
    private BillingFrequency billingFrequency;
    
    private List<SpecialOffer> specialOffers;
    
    private PlanType planType;
    
    private List<String> features;
    
    private Boolean exclusive;
    
    private Boolean hasAdvertisements;
}
