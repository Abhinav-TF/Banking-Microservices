package com.tnf.customer.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tnf.customer.dto.CreateCustomerRequest;
import com.tnf.customer.dto.CustomerResponse;
import com.tnf.customer.exception.ForbiddenException;
import com.tnf.customer.exception.UnauthorizedException;
import com.tnf.customer.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(
            @PathVariable String id,
            @RequestHeader(value = "X-Customer-Id", required = false) String callerId) {
        // Trusted identity is injected by the API gateway after JWT validation.
        if (!StringUtils.hasText(callerId)) {
            throw new UnauthorizedException("Missing authenticated caller identity");
        }
        if (!callerId.equals(id)) {
            throw new ForbiddenException("You can only access your own customer record");
        }
        CustomerResponse response = customerService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Internal owner-validation endpoint for Account/Wallet services.
     * Intentionally NOT ownership-checked — these are service-to-service Feign calls (via Eureka,
     * bypassing the gateway) that carry no X-Customer-Id. Returns whether the customer exists.
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Map<String, Object>> exists(@PathVariable String id) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", id);
        body.put("exists", customerService.existsById(id));
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> list(
            @RequestParam(value = "excludeId", required = false) String excludeId,
            @RequestHeader(value = "X-Customer-Id", required = false) String callerId) {
        if (!StringUtils.hasText(callerId)) {
            throw new UnauthorizedException("Missing authenticated caller identity");
        }
        List<CustomerResponse> customers = StringUtils.hasText(excludeId)
                ? customerService.getAllExcept(excludeId)
                : customerService.getAll();
        return ResponseEntity.ok(customers);
    }
}
