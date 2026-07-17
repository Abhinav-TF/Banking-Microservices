package com.tnf.walletservice.exception;

/**
 * A PAYTM wallet credit would push the balance above its {@code maxLimit}.
 * Maps to 400 Bad Request (and, once Notification is wired in, a limit-exceeded log).
 */
public class MaxLimitExceededException extends RuntimeException {
    public MaxLimitExceededException(String message) {
        super(message);
    }
}
