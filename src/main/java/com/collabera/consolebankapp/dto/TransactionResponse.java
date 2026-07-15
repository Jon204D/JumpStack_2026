package com.collabera.consolebankapp.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.collabera.consolebankapp.model.BankTransaction;
import com.collabera.consolebankapp.model.TransactionType;

public record TransactionResponse(
        String id,
        TransactionType type,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        Instant createdAt) {

    public static TransactionResponse from(BankTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getSourceAccountNumber(),
                transaction.getDestinationAccountNumber(),
                transaction.getAmount(),
                transaction.getCreatedAt());
    }
}
