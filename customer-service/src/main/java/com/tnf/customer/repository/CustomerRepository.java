package com.tnf.customer.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tnf.customer.entity.Customer;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    boolean existsByEmail(String email);
    List<Customer> findByIdNot(String excludeId);
}
