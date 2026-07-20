package com.tnf.transactionservice.service;
import com.tnf.transactionservice.dto.TransactionRequestDto;
import com.tnf.transactionservice.entities.Transaction;
import com.tnf.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(TransactionRequestDto transactionRequestDto) {
        logger.info("Starting creation of transaction info for owner : {}",transactionRequestDto.getOwnerId());

        Transaction transaction = new Transaction(transactionRequestDto);

        logger.info("Saving Transaction in DB for owner : {}",transactionRequestDto.getOwnerId());

        return transactionRepository.save(transaction);
    }

    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    public List<Transaction> getTransactionByOwnerId(String ownerId) {
        logger.info("Starting getting transaction info for owner : {}",ownerId);

        List<Transaction> transactions = transactionRepository.findByOwnerId(ownerId);

        logger.info("Found {} transactions for owner : {}",transactions.size(),ownerId);

        return transactions;
    }




}
