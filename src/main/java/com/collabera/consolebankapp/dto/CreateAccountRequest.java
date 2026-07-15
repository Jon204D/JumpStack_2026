package com.collabera.consolebankapp.dto;

import java.math.BigDecimal;

import com.collabera.consolebankapp.model.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
        @NotBlank(message = "Account number is required")
        @Pattern(regexp = "[A-Za-z0-9-]+",
                message = "Account number can contain only letters, numbers, and hyphens")
        String accountNumber,

        @NotNull(message = "Account type is required")
        AccountType type,

        @NotNull(message = "Starting balance is required")
        @DecimalMin(value = "0.00", message = "Starting balance cannot be negative")
        @Digits(integer = 15, fraction = 2,
                message = "Starting balance cannot have more than two decimal places")
        BigDecimal startingBalance) {
}
