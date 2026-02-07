package com.rawneeded.error.exceptions;

/**
 * Thrown when the customer has no remaining searches or points.
 * Frontend can detect errorCode 518 to show "buy more searches" UI.
 */
public class NoSearchesQuotaException extends AbstractException {

    public static final String ERROR_CODE = "518";

    public NoSearchesQuotaException(String message) {
        super(message);
    }
}
