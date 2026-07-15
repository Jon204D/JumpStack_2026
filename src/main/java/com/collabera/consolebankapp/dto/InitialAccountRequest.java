package com.collabera.consolebankapp.dto;

import java.math.BigDecimal;

import com.collabera.consolebankapp.model.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record InitialAccountRequest(
        @NotNull(message = "Account type is required")
        AccountType type,

        @NotNull(message = "Starting balance is required")
        @DecimalMin(value = "0.00", message = "Starting balance cannot be negative")
        @Digits(integer = 15, fraction = 2,
                message = "Starting balance cannot have more than two decimal places")
        BigDecimal startingBalance) {
}
