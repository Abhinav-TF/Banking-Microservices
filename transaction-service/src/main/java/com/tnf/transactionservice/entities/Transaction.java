package com.tnf.transactionservice.entities;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document( collection = "transactions" )
public class Transaction {

    @Id
    private String id;
    private String orderId;
    private String type;
    private String direction;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private LocalDateTime timestamp;

    public Transaction() {
        super();
    }

    public Transaction(String id, String orderId, String type, String direction, BigDecimal amount, BigDecimal balanceAfter) {
        super();
        this.id = id;
        this.orderId = orderId;
        this.type = type;
        this.direction = direction;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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
                ", orderId='" + orderId + '\'' +
                ", type='" + type + '\'' +
                ", direction='" + direction + '\'' +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                ", timestamp=" + timestamp +
                '}';
    }
}
