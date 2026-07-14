package com.collabera.consolebankapp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import com.collabera.consolebankapp.model.Customer;
import com.collabera.consolebankapp.service.AccountService;
import com.collabera.consolebankapp.service.CustomerService;

class CustomerControllerTests {

    private CustomerService customerService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        customerService = org.mockito.Mockito.mock(CustomerService.class);
        AccountService accountService = org.mockito.Mockito.mock(AccountService.class);
        CustomerController controller = new CustomerController(customerService, accountService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createsCustomer() throws Exception {
        when(customerService.createCustomer("customer1"))
                .thenReturn(new Customer("customer1"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"customer1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("customer1"));

        verify(customerService).createCustomer("customer1");
    }

    @Test
    void reportsValidationErrors() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username").value("Username is required"));
    }

    @Test
    void reportsDuplicateUsernameAsConflict() throws Exception {
        when(customerService.createCustomer("customer1"))
                .thenThrow(new DuplicateResourceException("Username is already in use"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"customer1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already in use"));
    }
}
