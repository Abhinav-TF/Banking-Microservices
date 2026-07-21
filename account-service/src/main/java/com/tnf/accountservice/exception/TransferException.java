package com.tnf.accountservice.exception;

/**
 * Thrown when a transfer fails AND the compensating rollback of the withdrawal also
 * fails, leaving the source account debited. Signals that manual reconciliation is needed.
 */
public class TransferException extends RuntimeException {
    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
