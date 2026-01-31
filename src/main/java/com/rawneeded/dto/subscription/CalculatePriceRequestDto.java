package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.PlanFeatures;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalculatePriceRequestDto {
    private String planId;
    private int numberOfUsers;
    
    // For Customer plans: Number of product searches requested
    private Integer numberOfSearches;
    
    // Selected features
    private List<PlanFeatures> selectedFeatures;
}
