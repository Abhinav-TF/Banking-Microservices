package com.tnf.transactionservice.repository;

import com.tnf.transactionservice.entities.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction,String> {

    List<Transaction> findByOwnerId(String email);
}
