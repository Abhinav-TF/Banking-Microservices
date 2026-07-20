package com.tnf.walletservice.dto;

import java.math.BigDecimal;

/**
 * Body of {@code POST /wallets/{id}/add} and {@code POST /wallets/{id}/pay}.
 * The {@code > 0} rule is enforced in the service so it maps to 422
 * (InvalidAmountException) rather than a 400 bean-validation error.
 */
public record AmountRequest(BigDecimal amount) {
}
