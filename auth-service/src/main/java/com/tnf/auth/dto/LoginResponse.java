package com.tnf.auth.dto;

/** Returned on successful login. */
public class LoginResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private String customerId;

    public LoginResponse(String token, String tokenType, long expiresIn, String customerId) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.customerId = customerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
