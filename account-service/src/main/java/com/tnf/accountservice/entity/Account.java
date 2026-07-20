package com.tnf.accountservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection =  "accounts")
public class Account {

    @Id
    private String id;

    @Indexed( unique = true)
    private int accountNumber;

    private String customerId;
    private String type;
    private double balance;
    private LocalDateTime createdAt;

    public Account() {
        createdAt = LocalDateTime.now();
    }

    public Account(String id, int accountNumber, String customerId, String type, double balance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.type = type;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
