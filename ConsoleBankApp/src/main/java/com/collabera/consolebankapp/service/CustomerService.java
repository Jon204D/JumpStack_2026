package com.collabera.consolebankapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.collabera.consolebankapp.exception.DuplicateResourceException;
import com.collabera.consolebankapp.exception.ResourceNotFoundException;
import com.collabera.consolebankapp.model.Customer;
import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.CustomerRepository;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(
            CustomerRepository customerRepository,
            UserCredentialRepository userCredentialRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer createCustomer(String username, String password) {
        String cleanUsername = requireText(username, "Username");
        String cleanPassword = requireText(password, "Password");

        if (customerRepository.existsByUsernameIgnoreCase(cleanUsername)
                || userCredentialRepository.existsByUsernameIgnoreCase(cleanUsername)) {
            throw new DuplicateResourceException("Username is already in use");
        }

        Customer customer = customerRepository.save(new Customer(cleanUsername));
        UserCredential credential = new UserCredential(
                cleanUsername,
                passwordEncoder.encode(cleanPassword),
                Role.CUSTOMER,
                customer.getId());
        userCredentialRepository.save(credential);
        return customer;
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
