package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierSpecialOffer {
    private String id;
    
    @DBRef
    private User supplier;
    private String supplierId;
    private String supplierName;
    private String supplierOrganizationName;
    
    @DBRef
    private Product product;
    private String productId;
    private String productName;
    private String productImage;
    
    private Double discountPercentage; // discount percentage
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
