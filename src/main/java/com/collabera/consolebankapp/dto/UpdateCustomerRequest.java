package com.collabera.consolebankapp.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @Pattern(regexp = ".*\\S.*", message = "Username cannot be blank")
        @Size(max = 50, message = "Username cannot exceed 50 characters")
        String username,

        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password) {
}
