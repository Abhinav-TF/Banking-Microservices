package com.tnf.walletservice.dto;

/**
 * Body of {@code POST /wallets/{id}/transfer} — both updated wallets.
 */
public record TransferResponse(
        WalletResponse source,
        WalletResponse target
) {
}
