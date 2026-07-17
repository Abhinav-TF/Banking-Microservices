package com.tnf.walletservice.exception;

/**
 * Amount is not strictly positive. Maps to 422 Unprocessable Entity.
 */
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
