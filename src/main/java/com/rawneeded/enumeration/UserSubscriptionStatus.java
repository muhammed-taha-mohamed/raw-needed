package com.rawneeded.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserSubscriptionStatus {
    PENDING,
    APPROVED,
    REJECTED
}
