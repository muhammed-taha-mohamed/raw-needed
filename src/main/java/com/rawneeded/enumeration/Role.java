
package com.rawneeded.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    // Admin roles
    SYSTEM_ADMIN,
    SUPER_ADMIN,

    // Customer roles
    CUSTOMER_OWNER,
    CUSTOMER_STAFF,

    // Supplier roles
    SUPPLIER_OWNER,
    SUPPLIER_STAFF
}



