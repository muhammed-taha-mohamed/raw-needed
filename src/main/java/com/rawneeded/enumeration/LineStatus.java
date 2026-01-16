package com.rawneeded.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public enum LineStatus {
    PENDING,
    RESPONDED,
    APPROVED,
    REJECTED,
    COMPLETED
}