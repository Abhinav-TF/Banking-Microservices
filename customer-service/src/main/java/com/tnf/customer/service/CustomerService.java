package com.tnf.customer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tnf.customer.dto.CreateCustomerRequest;
import com.tnf.customer.dto.CustomerResponse;
import com.tnf.customer.entity.Customer;
import com.tnf.customer.exception.CustomerNotFoundException;
import com.tnf.customer.exception.DuplicateCustomerException;
import com.tnf.customer.mapper.ReqToResMapper;
import com.tnf.customer.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public CustomerResponse create(CreateCustomerRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateCustomerException("Customer with email " + request.getEmail() + " already exists");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        
        Customer savedcus = repository.save(customer);
        return ReqToResMapper.mapToCustomerResponse(savedcus);
    }

    public CustomerResponse getById(String id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id " + id));
        return ReqToResMapper.mapToCustomerResponse(customer);
    }

    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    public List<CustomerResponse> getAll() {
        List<Customer> customers = repository.findAll();
        return customers.stream()
                .map(ReqToResMapper::mapToCustomerResponse)
                .toList();
    }

    public List<CustomerResponse> getAllExcept(String excludeId) {
        List<Customer> customers = repository.findByIdNot(excludeId);
        return customers.stream()
                .map(ReqToResMapper::mapToCustomerResponse)
                .toList();
    }
}
