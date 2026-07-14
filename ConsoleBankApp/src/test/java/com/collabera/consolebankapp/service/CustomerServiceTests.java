package com.collabera.consolebankapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.collabera.consolebankapp.exception.DuplicateResourceException;
import com.collabera.consolebankapp.model.Customer;
import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.CustomerRepository;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    private CustomerService customerService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        customerService = new CustomerService(
                customerRepository,
                userCredentialRepository,
                passwordEncoder);
    }

    @Test
    void createsCustomerWithTrimmedUsername() {
        when(customerRepository.existsByUsernameIgnoreCase("customer1")).thenReturn(false);
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(new Customer("customer1"));
        when(userCredentialRepository.save(any(UserCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer customer = customerService.createCustomer("  customer1  ", "customer123");

        assertEquals("customer1", customer.getUsername());
        verify(customerRepository).save(any(Customer.class));
        verify(userCredentialRepository).save(org.mockito.ArgumentMatchers.argThat(credential ->
                credential.getUsername().equals("customer1")
                        && credential.getRole() == Role.CUSTOMER
                        && !credential.getPasswordHash().equals("customer123")
                        && passwordEncoder.matches("customer123", credential.getPasswordHash())));
    }

    @Test
    void rejectsDuplicateUsername() {
        when(customerRepository.existsByUsernameIgnoreCase("customer1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> customerService.createCustomer("customer1", "customer123"));
        verify(customerRepository, never()).save(any(Customer.class));
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }

    @Test
    void rejectsBlankUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer("   ", "customer123"));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void neverStoresThePlainTextPassword() {
        when(customerRepository.existsByUsernameIgnoreCase("customer1")).thenReturn(false);
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(new Customer("customer1"));

        customerService.createCustomer("customer1", "customer123");

        verify(userCredentialRepository).save(org.mockito.ArgumentMatchers.argThat(credential -> {
            assertNotEquals("customer123", credential.getPasswordHash());
            return true;
        }));
    }
}
