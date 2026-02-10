package com.rawneeded.dto.RFQ;


import com.rawneeded.enumeration.LineStatus;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RFQOrderLineResponseDto {

    private String id;

    private String orderId;

    // Supplier details
    private String supplierId;
    private String supplierName;
    private String supplierOrganizationName;

    // Customer details
    private String customerOwnerId;
    private String customerOrganizationName;
    private String customerOrganizationCRN;

    private String productId;
    private String productName;
    private String productImage;
    private String unit;
    private String categoryId;
    private String subCategoryId;
    private Map<String, String> extraFieldValues;
    private Boolean manualOrder;
    private float quantity;

    private LineStatus status;
    
    private Boolean customerApproved;

    // Supplier Response
    private SupplierResponseOnOrderDTO supplierResponse;

    private String specialOfferId; // Flag to indicate if order line is from special offer
}
