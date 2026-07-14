package com.collabera.consolebankapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class AccountTests {

    @Test
    void createsSavingsAccountWithExactMoneyValues() {
        Account account = new Account(
                "SAV001",
                "customer-1",
                AccountType.SAVINGS,
                new BigDecimal("500.00"),
                new BigDecimal("0.015"));

        assertEquals("SAV001", account.getAccountNumber());
        assertEquals("customer-1", account.getCustomerId());
        assertEquals(AccountType.SAVINGS, account.getType());
        assertEquals(new BigDecimal("500.00"), account.getBalance());
        assertEquals(new BigDecimal("0.015"), account.getInterestRate());
    }
}
