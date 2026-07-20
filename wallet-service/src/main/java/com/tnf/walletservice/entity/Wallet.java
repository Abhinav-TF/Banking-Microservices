package com.tnf.walletservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "wallets")
@CompoundIndex(name = "ix_customer_provider", def = "{'customerId': 1, 'provider': 1}")
@NoArgsConstructor
@Getter
@Setter
public class Wallet {

    @Id
    private String id;

    private String customerId;
    private Provider provider;
    private BigDecimal balance;
    private BigDecimal maxLimit;
    private Instant createdAt;

    public Wallet(String customerId, Provider provider, BigDecimal maxLimit) {
        this.customerId = customerId;
        this.provider = provider;
        this.maxLimit = maxLimit;
        this.balance = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }
}
