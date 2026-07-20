package com.tnf.accountservice.entity;

public class TransactionDTO {
    private String ownerId;
    private String type;
    private String direction;
    private double amount;
    private double balanceAfter;

    public TransactionDTO() {
    }

    public TransactionDTO(String ownerId, String type, String direction, double amount, double balanceAfter) {
        this.ownerId = ownerId;
        this.type = type;
        this.direction = direction;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "ownerId='" + ownerId + '\'' +
                ", type='" + type + '\'' +
                ", direction='" + direction + '\'' +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                '}';
    }
}
