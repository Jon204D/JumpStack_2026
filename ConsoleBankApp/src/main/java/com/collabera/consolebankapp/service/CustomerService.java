package com.collabera.consolebankapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.collabera.consolebankapp.exception.DuplicateResourceException;
import com.collabera.consolebankapp.exception.ResourceNotFoundException;
import com.collabera.consolebankapp.model.Customer;
import com.collabera.consolebankapp.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(String username) {
        String cleanUsername = requireText(username, "Username");

        if (customerRepository.existsByUsernameIgnoreCase(cleanUsername)) {
            throw new DuplicateResourceException("Username is already in use");
        }

        return customerRepository.save(new Customer(cleanUsername));
    }

    public Customer getCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer was not found"));
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim();
    }
}
