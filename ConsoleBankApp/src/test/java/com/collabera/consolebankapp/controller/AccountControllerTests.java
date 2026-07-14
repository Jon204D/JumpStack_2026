package com.collabera.consolebankapp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.collabera.consolebankapp.exception.InsufficientFundsException;
import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.service.AccountService;

class AccountControllerTests {

    private AccountService accountService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        accountService = org.mockito.Mockito.mock(AccountService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void depositsIntoAccount() throws Exception {
        Account account = checkingAccount("125.50");
        when(accountService.deposit("CHK001", new BigDecimal("25.50")))
                .thenReturn(account);

        mockMvc.perform(post("/api/accounts/CHK001/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":25.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("CHK001"))
                .andExpect(jsonPath("$.balance").value(125.50));
    }

    @Test
    void transfersBetweenAccounts() throws Exception {
        mockMvc.perform(post("/api/accounts/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "CHK001",
                                  "destinationAccountNumber": "CHK002",
                                  "amount": 30.00
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(accountService).transfer("CHK001", "CHK002", new BigDecimal("30.00"));
    }

    @Test
    void rejectsInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/accounts/CHK001/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").value("Amount must be at least 0.01"));
    }

    @Test
    void reportsInsufficientFunds() throws Exception {
        when(accountService.withdraw("CHK001", new BigDecimal("200.00")))
                .thenThrow(new InsufficientFundsException("Insufficient balance"));

        mockMvc.perform(post("/api/accounts/CHK001/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    private Account checkingAccount(String balance) {
        return new Account(
                "CHK001",
                "customer-1",
                AccountType.CHECKING,
                new BigDecimal(balance),
                new BigDecimal("0.0100"));
    }
}
