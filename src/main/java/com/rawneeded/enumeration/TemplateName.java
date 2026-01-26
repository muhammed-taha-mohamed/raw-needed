package com.rawneeded.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TemplateName {
    WELCOME_TEMPLATE("welcome-template.ftl"),
    LOGIN_OTP("login-otp-template.ftl"),
    ACTIVATE_ACCOUNT_OTP("activate-account-otp-template.ftl"),
    OWNERSHIP_TRANSFER_OTP("ownership-transfer-otp-template.ftl"),
    FORGET_PASSWORD_OTP("forget-password-otp-template.ftl"),
    FORGET_PASSWORD_HINT("forget-password-hint-template.ftl"),
    PAID_PRODUCT_EMAIL("paid-product-template.ftl"),
    QUOTATION_TEMPLATE("quotation-template.ftl"),
    ORDER_CREATED_SUPPLIER("order-created-supplier.ftl"),
    ORDER_CANCELLED_SUPPLIER("order-cancelled-supplier.ftl"),
    ORDER_APPROVED_SUPPLIER("order-approved-supplier.ftl"),
    ORDER_COMPLETED_CUSTOMER("order-completed-customer.ftl"),
    ORDER_REPLY_CUSTOMER("order-reply-customer.ftl"),
    ORDER_MESSAGE("order-message.ftl"),
    COMPLAINT_CREATED_ADMIN("complaint-created-admin.ftl"),
    COMPLAINT_REPLY_USER("complaint-reply-user.ftl"),
    COMPLAINT_CLOSED_USER("complaint-closed-user.ftl");

    private final String name;
}
