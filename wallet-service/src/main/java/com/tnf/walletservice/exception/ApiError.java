package com.tnf.walletservice.exception;

import java.time.Instant;

/**
 * Uniform JSON error body returned by {@link GlobalExceptionHandler}.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message
) {
}
