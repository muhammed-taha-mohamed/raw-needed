package com.rawneeded.model;

import com.rawneeded.enumeration.PlanFeatures;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PlanFeature {
    private PlanFeatures feature;
    private Double price; // سعر الفيتشر
}
