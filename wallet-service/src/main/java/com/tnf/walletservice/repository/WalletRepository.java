package com.tnf.walletservice.repository;

import com.tnf.walletservice.entity.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WalletRepository extends MongoRepository<Wallet, String> {

    List<Wallet> findByCustomerId(String customerId);
}
