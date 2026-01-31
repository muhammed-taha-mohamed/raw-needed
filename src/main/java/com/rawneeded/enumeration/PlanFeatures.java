package com.rawneeded.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanFeatures {
    
    // Supplier Features
    SUPPLIER_ADVERTISEMENTS("Advertisements", "الإعلانات", PlanType.SUPPLIER),
    SUPPLIER_PRIVATE_ORDERS("Private Orders", "الطلبات الخاصة", PlanType.SUPPLIER),
    SUPPLIER_SPECIAL_OFFERS("Special Offers", "العروض الخاصة", PlanType.SUPPLIER),
    SUPPLIER_ADVANCED_REPORTS("Advanced Reports", "التقارير المتقدمة", PlanType.SUPPLIER),
    
    // Customer Features
    CUSTOMER_PRIVATE_ORDERS("Private Orders", "الطلبات الخاصة", PlanType.CUSTOMER),
    CUSTOMER_RAW_MATERIALS_ADVANCE("Raw Materials Advance", "سلفة الخامات", PlanType.CUSTOMER),
    CUSTOMER_VIEW_SUPPLIER_OFFERS("View Supplier Special Offers", "ظهور العروض الخاصة للموردين", PlanType.CUSTOMER),
    CUSTOMER_ADVANCED_REPORTS("Advanced Reports", "التقارير المتقدمة", PlanType.CUSTOMER);

    private final String descriptionEn;
    private final String descriptionAr;
    private final PlanType planType;
}
