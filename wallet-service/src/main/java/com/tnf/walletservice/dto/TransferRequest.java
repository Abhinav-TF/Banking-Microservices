package com.tnf.walletservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Body of {@code POST /wallets/{id}/transfer}. The {@code amount > 0} rule is
 * enforced in the service (maps to 422).
 */
public record TransferRequest(
        @NotBlank String targetWalletId,
        BigDecimal amount
) {
}
