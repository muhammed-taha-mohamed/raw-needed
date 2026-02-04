package com.rawneeded.dto.RFQ;


import com.rawneeded.enumeration.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RFQOrderResponseDto {

    private String id;
    private String orderNumber;

    private String userId;
    private String userName;

    // Organization details
    private String ownerId;
    private String organizationName;
    private String organizationCRN;

    private OrderStatus status;

    private int numberOfLines;

    private LocalDateTime createdAt;

    private boolean createdByOwner;

    private String specialOfferId; // Flag to indicate if order is from special offer

}
