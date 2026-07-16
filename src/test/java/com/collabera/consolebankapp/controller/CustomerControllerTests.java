package com.collabera.consolebankapp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.security.BankAuthorizationService;
import com.collabera.consolebankapp.service.AccountService;
import com.collabera.consolebankapp.service.CustomerOnboardingResult;
import com.collabera.consolebankapp.service.CustomerOnboardingService;
import com.collabera.consolebankapp.service.CustomerService;

class CustomerControllerTests {

    private CustomerService customerService;
    private CustomerOnboardingService onboardingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        customerService = org.mockito.Mockito.mock(CustomerService.class);
        AccountService accountService = org.mockito.Mockito.mock(AccountService.class);
        onboardingService = org.mockito.Mockito.mock(CustomerOnboardingService.class);
        BankAuthorizationService authorizationService =
                org.mockito.Mockito.mock(BankAuthorizationService.class);
        CustomerController controller = new CustomerController(
                customerService,
                accountService,
                onboardingService,
                authorizationService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createsCustomer() throws Exception {
        Customer customer = new Customer("customer1");
        Account account = new Account(
                "CHK-1234567",
                "customer-1",
                AccountType.CHECKING,
                new java.math.BigDecimal("100.00"),
                new java.math.BigDecimal("0.0100"));
        when(onboardingService.onboard(
                org.mockito.ArgumentMatchers.eq("customer1"),
                org.mockito.ArgumentMatchers.eq("customer123"),
                org.mockito.ArgumentMatchers.eq(new java.math.BigDecimal("100.00")),
                org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(new CustomerOnboardingResult(customer, java.util.List.of(account)));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"customer1",
                                  "password":"customer123",
                                  "totalStartingBalance":100.00,
                                  "accounts":[
                                    {"type":"CHECKING","startingBalance":100.00}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customer.username").value("customer1"))
                .andExpect(jsonPath("$.accounts[0].accountNumber").value("CHK-1234567"));

        verify(onboardingService).onboard(
                org.mockito.ArgumentMatchers.eq("customer1"),
                org.mockito.ArgumentMatchers.eq("customer123"),
                org.mockito.ArgumentMatchers.eq(new java.math.BigDecimal("100.00")),
                org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void reportsValidationErrors() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"",
                                  "password":"customer123",
                                  "totalStartingBalance":100.00,
                                  "accounts":[
                                    {"type":"CHECKING","startingBalance":100.00}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username").value("Username is required"));
    }

    @Test
    void reportsDuplicateUsernameAsConflict() throws Exception {
        when(onboardingService.onboard(
                org.mockito.ArgumentMatchers.eq("customer1"),
                org.mockito.ArgumentMatchers.eq("customer123"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyList()))
                .thenThrow(new DuplicateResourceException("Username is already in use"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"customer1",
                                  "password":"customer123",
                                  "totalStartingBalance":100.00,
                                  "accounts":[
                                    {"type":"CHECKING","startingBalance":100.00}
                                  ]
                                }
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

    @Test
    void updatesCustomerCredentials() throws Exception {
        Customer customer = new Customer("customer2");
        when(customerService.updateCustomer(
                "customer-1",
                "customer2",
                "newPassword123"))
                .thenReturn(customer);

        mockMvc.perform(patch("/api/customers/customer-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"customer2",
                                  "password":"newPassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("customer2"));

        verify(customerService).updateCustomer(
                "customer-1",
                "customer2",
                "newPassword123");
    }

    @Test
    void rejectsShortReplacementPassword() throws Exception {
        mockMvc.perform(patch("/api/customers/customer-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password must be between 8 and 72 characters"));
    }

    @Test
    void reportsDuplicateUsernameDuringUpdate() throws Exception {
        when(customerService.updateCustomer("customer-1", "customer2", null))
                .thenThrow(new DuplicateResourceException("Username is already in use"));

        mockMvc.perform(patch("/api/customers/customer-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"customer2\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already in use"));
    }
}
