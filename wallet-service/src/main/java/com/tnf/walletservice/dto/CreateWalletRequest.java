package com.tnf.walletservice.dto;

import com.tnf.walletservice.entity.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Body of {@code POST /wallets}. {@code maxLimit} is required for PAYTM
 * (enforced in the service, since the rule is provider-dependent).
 */
public record CreateWalletRequest(
        @NotBlank String customerId,
        @NotNull Provider provider,
        BigDecimal maxLimit
) {
}
