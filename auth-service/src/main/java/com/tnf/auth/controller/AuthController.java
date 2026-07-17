package com.tnf.auth.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tnf.auth.dto.LoginRequest;
import com.tnf.auth.dto.LoginResponse;
import com.tnf.auth.dto.RegisterRequest;
import com.tnf.auth.dto.RegisterResponse;
import com.tnf.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** Returns the authenticated principal, read from the gateway-injected headers. */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(
            @RequestHeader(value = "X-Customer-Id", required = false) String customerId,
            @RequestHeader(value = "X-Auth-Email", required = false) String email) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("customerId", customerId);
        body.put("email", email);
        return ResponseEntity.ok(body);
    }
}
