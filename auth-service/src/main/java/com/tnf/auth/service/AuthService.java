package com.tnf.auth.service;

import org.springframework.stereotype.Service;

import com.tnf.auth.client.CustomerClient;
import com.tnf.auth.client.dto.CustomerCreateRequest;
import com.tnf.auth.client.dto.CustomerDto;
import com.tnf.auth.dto.LoginRequest;
import com.tnf.auth.dto.LoginResponse;
import com.tnf.auth.dto.RegisterRequest;
import com.tnf.auth.dto.RegisterResponse;
import com.tnf.auth.entity.Credential;
import com.tnf.auth.exception.DuplicateCredentialException;
import com.tnf.auth.exception.InvalidCredentialsException;
import com.tnf.auth.repository.CredentialRepository;
import com.tnf.auth.security.JwtService;

@Service
public class AuthService {

    private final CredentialRepository repository;
    private final CustomerClient customerClient;
    private final JwtService jwtService;

    public AuthService(CredentialRepository repository, CustomerClient customerClient, JwtService jwtService) {
        this.repository = repository;
        this.customerClient = customerClient;
        this.jwtService = jwtService;
    }

    /**
     * Creates the customer (via customer-service) and stores the linked credential.
     * Password is stored as-is (plain text) by design for this iteration.
     */
    public RegisterResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateCredentialException("Customer with email " + request.getEmail() + " already exists");
        }

        CustomerDto customer = customerClient.createCustomer(
                new CustomerCreateRequest(request.getName(), request.getEmail(), request.getPhone()));

        Credential credential = new Credential(request.getEmail(), request.getPassword(), customer.getId());
        repository.save(credential);

        return new RegisterResponse(customer.getId(), customer.getEmail(), customer.getName());
    }

    public LoginResponse login(LoginRequest request) {
        Credential credential = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Plain-text comparison by design (demo only).
        if (!credential.getPassword().equals(request.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(credential.getCustomerId(), credential.getEmail());
        return new LoginResponse(token, "Bearer", jwtService.getExpiresInSeconds(), credential.getCustomerId());
    }
}
