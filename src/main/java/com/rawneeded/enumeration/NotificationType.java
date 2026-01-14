package com.rawneeded.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    ORDER_REPLY("Order Reply"),
    ORDER_CREATED("Order Created"),
    QUOTATION_SENT("Quotation Sent"),
    QUOTATION_ACCEPTED("Quotation Accepted"),
    QUOTATION_REJECTED("Quotation Rejected"),
    RFQ_RESPONSE("RFQ Response"),
    ORDER_STATUS_UPDATED("Order Status Updated"),
    GENERAL("General Notification");

    private final String description;
}
