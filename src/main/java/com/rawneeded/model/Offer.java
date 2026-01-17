package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Offer {
    private String id;
    
    private String postId;
    
    @DBRef
    private User offeredBy;
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
