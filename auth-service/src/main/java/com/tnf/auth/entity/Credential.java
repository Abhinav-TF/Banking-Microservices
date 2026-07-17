package com.tnf.auth.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Login credential. <b>Password is stored in plain text by explicit design choice for this
 * iteration</b> — demo/learning only, never production. See microservices-design/auth-service/schema.md.
 */
@Document(collection = "credentials")
public class Credential {

    @Id
    private String id;

    @Indexed(unique = true, name = "uk_email")
    private String email;

    private String password;

    @Indexed(unique = true, name = "uk_customerId")
    private String customerId;

    private Instant createdAt;

    public Credential() {
        this.createdAt = Instant.now();
    }

    public Credential(String email, String password, String customerId) {
        this.email = email;
        this.password = password;
        this.customerId = customerId;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
