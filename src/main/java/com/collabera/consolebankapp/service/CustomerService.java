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
import com.collabera.consolebankapp.repository.AccountRepository;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            UserCredentialRepository userCredentialRepository,
            PasswordEncoder passwordEncoder,
            AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
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

    @Transactional
    public Customer updateCustomer(String customerId, String username, String password) {
        String cleanCustomerId = requireText(customerId, "Customer id");
        if (username == null && password == null) {
            throw new IllegalArgumentException("A username or password update is required");
        }

        Customer customer = getCustomer(cleanCustomerId);
        UserCredential credential = userCredentialRepository.findByCustomerId(cleanCustomerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer credentials were not found"));

        if (username != null) {
            String cleanUsername = requireText(username, "Username");
            if (!cleanUsername.equalsIgnoreCase(customer.getUsername())) {
                if (customerRepository.existsByUsernameIgnoreCase(cleanUsername)
                        || userCredentialRepository.existsByUsernameIgnoreCase(cleanUsername)) {
                    throw new DuplicateResourceException("Username is already in use");
                }
            }
            customer.setUsername(cleanUsername);
            credential.setUsername(cleanUsername);
            customerRepository.save(customer);
        }

        if (password != null) {
            String cleanPassword = requireText(password, "Password");
            credential.setPasswordHash(passwordEncoder.encode(cleanPassword));
        }

        userCredentialRepository.save(credential);
        return customer;
    }

    @Transactional
    public void deleteCustomer(String customerId) {
        String cleanCustomerId = requireText(customerId, "Customer id");
        Customer customer = getCustomer(cleanCustomerId);

        accountRepository.deleteByCustomerId(cleanCustomerId);
        userCredentialRepository.deleteByCustomerId(cleanCustomerId);
        customerRepository.delete(customer);
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim();
    }
}
