package com.collabera.consolebankapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.collabera.consolebankapp.exception.DuplicateResourceException;
import com.collabera.consolebankapp.model.Customer;
import com.collabera.consolebankapp.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void createsCustomerWithTrimmedUsername() {
        when(customerRepository.existsByUsernameIgnoreCase("customer1")).thenReturn(false);
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer customer = customerService.createCustomer("  customer1  ");

        assertEquals("customer1", customer.getUsername());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void rejectsDuplicateUsername() {
        when(customerRepository.existsByUsernameIgnoreCase("customer1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> customerService.createCustomer("customer1"));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void rejectsBlankUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer("   "));
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
