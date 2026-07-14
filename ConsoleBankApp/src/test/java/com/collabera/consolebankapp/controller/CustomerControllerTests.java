package com.collabera.consolebankapp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.collabera.consolebankapp.exception.DuplicateResourceException;
import com.collabera.consolebankapp.exception.ResourceNotFoundException;
import com.collabera.consolebankapp.model.Customer;
import com.collabera.consolebankapp.security.BankAuthorizationService;
import com.collabera.consolebankapp.service.AccountService;
import com.collabera.consolebankapp.service.CustomerService;

class CustomerControllerTests {

    private CustomerService customerService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        customerService = org.mockito.Mockito.mock(CustomerService.class);
        AccountService accountService = org.mockito.Mockito.mock(AccountService.class);
        BankAuthorizationService authorizationService =
                org.mockito.Mockito.mock(BankAuthorizationService.class);
        CustomerController controller = new CustomerController(
                customerService, accountService, authorizationService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createsCustomer() throws Exception {
        when(customerService.createCustomer("customer1", "customer123"))
                .thenReturn(new Customer("customer1"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"customer1","password":"customer123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("customer1"));

        verify(customerService).createCustomer("customer1", "customer123");
    }

    @Test
    void reportsValidationErrors() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":"customer123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username").value("Username is required"));
    }

    @Test
    void reportsDuplicateUsernameAsConflict() throws Exception {
        when(customerService.createCustomer("customer1", "customer123"))
                .thenThrow(new DuplicateResourceException("Username is already in use"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"customer1","password":"customer123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already in use"));
    }

    @Test
    void requiresCustomerPassword() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"customer1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").value("Password is required"));
    }

    @Test
    void deletesCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/customer-1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer("customer-1");
    }

    @Test
    void reportsMissingCustomerDuringDelete() throws Exception {
        doThrow(new ResourceNotFoundException("Customer was not found"))
                .when(customerService).deleteCustomer("missing");

        mockMvc.perform(delete("/api/customers/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer was not found"));
    }
}
