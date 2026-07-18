package com.tnf.transactionservice.service;
import com.tnf.transactionservice.dto.TransactionRequestDto;
import com.tnf.transactionservice.entities.Transaction;
import com.tnf.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(TransactionRequestDto transactionRequestDto) {
        try{
            logger.info("Starting creation of transaction info for owner : {}",transactionRequestDto.getOwnerId());

            Transaction transaction = new Transaction(transactionRequestDto);

            logger.info("Saving Transaction in DB for owner : {}",transactionRequestDto.getOwnerId());

            return transactionRepository.save(transaction);
        }
        catch (Exception e){
            logger.error("Error creating transaction info for owner : {}",transactionRequestDto.getOwnerId(),e);
            return null;
        }
    }



}
