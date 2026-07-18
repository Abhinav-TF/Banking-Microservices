package com.tnf.transactionservice.entities;


import com.tnf.transactionservice.dto.TransactionRequestDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document( collection = "transactions" )
public class Transaction {

    @Id
    private String id;
    private String ownerId;
    private String type;
    private String direction;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private LocalDateTime timestamp;

    public Transaction() {
        super();
    }

    public Transaction(String ownerId, String type, String direction, BigDecimal amount, BigDecimal balanceAfter) {
        super();
        this.ownerId = ownerId;
        this.type = type;
        this.direction = direction;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(TransactionRequestDto transactionRequestDto) {
        super();
        this.ownerId = transactionRequestDto.getOwnerId();
        this.type = transactionRequestDto.getType();
        this.direction = transactionRequestDto.getDirection();
        this.amount = transactionRequestDto.getAmount();
        this.balanceAfter = transactionRequestDto.getBalanceAfter();
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", type='" + type + '\'' +
                ", direction='" + direction + '\'' +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                ", timestamp=" + timestamp +
                '}';
    }
}
