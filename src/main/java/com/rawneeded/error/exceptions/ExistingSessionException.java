package com.rawneeded.error.exceptions;

import lombok.Getter;

/**
 * Thrown when user tries to login but already has an active session elsewhere.
 * Frontend should show popup: "تسجيل الدخول هنا" or "الإبقاء على الجلسة"
 */
@Getter
public class ExistingSessionException extends AbstractException {
    public static final String ERROR_CODE = "513";

    public ExistingSessionException(String message) {
        super(message);
    }
}
