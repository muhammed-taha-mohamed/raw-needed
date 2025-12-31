package com.rawneeded.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubscriptionPlanResponseDto {
    private String id;
    private String name;
    private double price;
    private int userLimit;
    private String description;
}

