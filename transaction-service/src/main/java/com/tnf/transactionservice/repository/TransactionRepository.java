package com.tnf.transactionservice.repository;

import com.tnf.transactionservice.entities.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<String,Transaction> {
}
