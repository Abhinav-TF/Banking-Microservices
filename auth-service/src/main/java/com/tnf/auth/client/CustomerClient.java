package com.tnf.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tnf.auth.client.dto.CustomerCreateRequest;
import com.tnf.auth.client.dto.CustomerDto;

/**
 * Feign client to customer-service, resolved by Eureka service name.
 * Internal call — bypasses the API gateway, so no JWT is attached.
 */
@FeignClient(name = "customer-service")
public interface CustomerClient {

    @PostMapping(value = "/customers", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerDto createCustomer(@RequestBody CustomerCreateRequest request);
}
