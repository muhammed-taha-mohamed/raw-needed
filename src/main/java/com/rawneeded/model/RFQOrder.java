package com.rawneeded.model;

import com.rawneeded.enumeration.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RFQOrder {

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
}
