package com.tnf.transactionservice.dto;


import java.math.BigDecimal;

public class TransactionRequestDto {

    private String ownerId;
    private String type;
    private String direction;
    private BigDecimal amount;
    private BigDecimal balanceAfter;

    public TransactionRequestDto() {
        super();
    }

    public TransactionRequestDto(String ownerId, String type, String direction, BigDecimal amount, BigDecimal balanceAfter) {
        super();
        this.ownerId = ownerId;
        this.type = type;
        this.direction = direction;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
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

    @Override
    public String toString() {
        return "TransactionRequestDto{" +
                "id='" + id + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", type='" + type + '\'' +
                ", direction='" + direction + '\'' +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                '}';
    }
}
