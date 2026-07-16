package com.collabera.consolebankapp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.collabera.consolebankapp.model.BankTransaction;
import com.collabera.consolebankapp.model.TransactionType;
import com.collabera.consolebankapp.service.AccountService;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTests {

    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TransactionController(accountService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsAllTransactionsForAdminAudit() throws Exception {
        BankTransaction transaction = new BankTransaction(
                TransactionType.TRANSFER,
                "CHK001",
                "SAV001",
                new BigDecimal("30.00"),
                Instant.parse("2026-07-14T12:00:00Z"));
        when(accountService.getAllTransactionHistory())
                .thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TRANSFER"))
                .andExpect(jsonPath("$[0].sourceAccountNumber").value("CHK001"))
                .andExpect(jsonPath("$[0].destinationAccountNumber").value("SAV001"))
                .andExpect(jsonPath("$[0].amount").value(30.00));
    }
}
