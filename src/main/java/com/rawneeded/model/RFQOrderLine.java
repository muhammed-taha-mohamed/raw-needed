package com.rawneeded.model;

import com.rawneeded.dto.RFQ.SupplierResponseOnOrderDTO;
import com.rawneeded.enumeration.LineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RFQOrderLine {

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
    private float quantity;

    private LineStatus status;
    
    private Boolean customerApproved;

    // Special Offer
    private String specialOfferId; // ID of special offer if this order line is from a special offer

    // Supplier Response
    private SupplierResponseOnOrderDTO supplierResponse;
}
