package com.tnf.walletservice.dto;

import com.tnf.walletservice.entity.Provider;
import com.tnf.walletservice.entity.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletResponse(
        String id,
        String customerId,
        Provider provider,
        BigDecimal balance,
        BigDecimal maxLimit,
        Instant createdAt
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getCustomerId(),
                wallet.getProvider(),
                wallet.getBalance(),
                wallet.getMaxLimit(),
                wallet.getCreatedAt()
        );
    }
}
