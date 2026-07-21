package com.tnf.accountservice.service;

import com.tnf.accountservice.entity.*;
import com.tnf.accountservice.exception.InsufficientBalanceException;
import com.tnf.accountservice.exception.InvalidAmountException;
import com.tnf.accountservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository repository;

    @Autowired
    private WebClient webClientBuilder;

    public Account createAccount(Account account){
        logger.info("creating account for customerId: {} ", account.getCustomerId());
        if (account.getCreatedAt() == null){
            account.setCreatedAt(LocalDateTime.now());
        }
        try{
            if (account.getBalance() <0) {
                throw new InvalidAmountException("Intial balance cannot be negative: " + account.getBalance());
            }
            else{
                return repository.save(account);
            }
        }
        catch (InvalidAmountException e){
            logger.error("error in creating account for customerId: {} ", account.getCustomerId() );
            return null;
        }
        catch (RuntimeException e){
            System.out.println(e);
            return null;
        }
    }

    // Find by Account id
    public Account getAccountById(String id){
        logger.info("finding account for accountId: {} ", id);
        return repository.findById(id).orElseThrow(() -> new RuntimeException("account not found"));
    }

    // Find by customer id
    public List<Account> getAccountsByCustomer(String customerId){
        logger.info("finding all the account for customerId: {} ", customerId);
        try {
            List<Account> foundAccounts = repository.findAll().stream()
                    .filter(c -> c.getCustomerId().equals(customerId)).toList();
            if (foundAccounts != null)
                return foundAccounts;
            else {
                throw new RuntimeException("Account not found");
            }
        }
        catch (RuntimeException e){
            System.out.println(e);
            return null;
        }
    }


    // Transaction Functions

    // Deposit
    public String deposit(String accountId, double amount){
        logger.info("finding account for accountId: {} ", accountId);
        Account account = repository.findById(accountId).orElseThrow(() -> new RuntimeException("account not found"));
        logger.info("Account found: {} ", accountId);

        if(amount > 0){
            account.setBalance(account.getBalance() + amount);
            // update the balance
            repository.save(account);

            // create new Transaction
            logger.info("new transaction creation");
            TransactionDTO tx = new TransactionDTO(account.getId(), account.getType(), "DEPOSIT", amount, account.getBalance());
            createTransaction(tx);
            logger.info("new tx: {}", tx);

            logger.info("{} - Updated balance: {} ", accountId, account.getBalance());
            return (accountId + " - updated balance: " + account.getBalance());
        }
        else {
            logger.error("Invalid amount");
            throw new InvalidAmountException( "invalid amount: " + amount);
        }
    }

    public String withdraw(String accountId, double amount){
        logger.info("finding account for accountId: {} ", accountId);
        Account account = repository.findById(accountId).orElseThrow(() -> new RuntimeException("account not found"));
        logger.info("Account found: {} ", accountId);

        if(amount > 0 && account.getBalance() >= amount){
            account.setBalance(account.getBalance() - amount);

            // update the balance
            repository.save(account);

            // create new Transaction
            logger.info("new transaction creation");
            TransactionDTO tx = new TransactionDTO(account.getId(), account.getType(), "WITHDRAW", amount, account.getBalance());
            createTransaction(tx);
            logger.info("new tx: {}", tx);

            logger.info("{} - Updated balance: {} ", accountId, account.getBalance());
            return (accountId + " - updated balance: " + account.getBalance());
        }
        else {
            logger.error("Insufficient Balance!");
            throw new InsufficientBalanceException( "Insufficient Balance : " + account.getBalance());
        }
    }

    public String transfer(Transfer transfer){
        withdraw(transfer.getSourceAccountId(), transfer.getAmount());
        deposit(transfer.getTargetAccountId(), transfer.getAmount());

        return ("Transfer from " + transfer.getSourceAccountId() + " --> " + transfer.getTargetAccountId() + " successful" );
    }

    // Helper Functions
    // POST Transaction
    private void createTransaction(TransactionDTO tx){
        try{
            String transactionServiceUrl = "http://TRANSACTION-SERVICE/transactions";

            logger.info("Calling Transaction Service: {}", transactionServiceUrl);
            webClientBuilder
                    .post()
                    .uri(transactionServiceUrl)
                    .bodyValue(tx)
                    .retrieve()
                    .bodyToMono(TransactionDTO.class)
                    .block();
        }
        catch (WebClientResponseException.NotFound e){
            logger.error("Transaction service error: {} - {}", e.getStatusCode(),e.getResponseBodyAsString());
        }
        catch (Exception e){
            logger.error("Transaction service call failed : {}", e.getMessage());
        }
    }
}
