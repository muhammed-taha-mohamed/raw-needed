package com.rawneeded.enummeration;

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
    QUOTATION_TEMPLATE("quotation-template.ftl");


    private final String name;
}
