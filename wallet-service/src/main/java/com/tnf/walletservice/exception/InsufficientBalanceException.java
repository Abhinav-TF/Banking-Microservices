package com.tnf.walletservice.exception;

/**
 * A pay/transfer would overdraw the wallet balance. Maps to 400 Bad Request.
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
