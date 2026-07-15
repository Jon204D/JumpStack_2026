package com.collabera.consolebankapp.dto;

import com.collabera.consolebankapp.model.Customer;

public record CustomerResponse(String id, String username) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getUsername());
    }
}
