package com.rawneeded.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OfferResponseDto {
    private String id;
    private String postId;
    
    private String offeredById;
    private String offeredByName;
    private String offeredByOrganizationName;
    
    private Float price;
    private Float availableQuantity;
    private String shippingInfo;
    private LocalDate estimatedDelivery;
    private String notes;
    
    private Boolean accepted;
    private String responseMessage;
    private LocalDateTime respondedAt;
    
    private LocalDateTime createdAt;
}
