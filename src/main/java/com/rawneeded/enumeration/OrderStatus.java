package com.rawneeded.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {
    NEW,
    PARTIALLY_RESPONDED,
    COMPLETED,
    CANCELLED
}
