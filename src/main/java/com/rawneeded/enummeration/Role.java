
package com.rawneeded.enummeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    // Admin roles
    SUPER_ADMIN,

    // Customer roles
    CUSTOMER_OWNER,
    CUSTOMER_STAFF,

    // Supplier roles
    SUPPLIER_OWNER,
    SUPPLIER_STAFF
}



