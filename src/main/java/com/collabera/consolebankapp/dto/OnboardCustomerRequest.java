package com.collabera.consolebankapp.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OnboardCustomerRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username cannot exceed 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @NotNull(message = "Total starting balance is required")
        @DecimalMin(value = "0.00", message = "Total starting balance cannot be negative")
        @Digits(integer = 15, fraction = 2,
                message = "Total starting balance cannot have more than two decimal places")
        BigDecimal totalStartingBalance,

        @NotEmpty(message = "At least one initial account is required")
        @Size(max = 2, message = "No more than two initial accounts are allowed")
        List<@Valid InitialAccountRequest> accounts) {
}
