package com.collabera.consolebankapp.dto;

import java.math.BigDecimal;

import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.AccountType;

public record AccountResponse(
        String id,
        String accountNumber,
        String customerId,
        AccountType type,
        BigDecimal balance,
        BigDecimal interestRate) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getType(),
                account.getBalance(),
                account.getInterestRate());
    }
}
