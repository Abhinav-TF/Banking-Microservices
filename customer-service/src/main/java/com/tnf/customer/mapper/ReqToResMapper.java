package com.tnf.customer.mapper;

import com.tnf.customer.dto.CustomerResponse;
import com.tnf.customer.entity.Customer;

public class ReqToResMapper {
    public static CustomerResponse mapToCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone()
        );
    }
}
