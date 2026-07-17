package com.tnf.walletservice.exception;

/**
 * No wallet exists for the given id. Maps to 404 Not Found.
 */
public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}
